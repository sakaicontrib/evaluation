package org.sakaiproject.evaluation.logic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalLock;
import org.sakaiproject.evaluation.model.EvalQueuedEmail;
import org.sakaiproject.evaluation.model.EvalQueuedGroup;
import org.sakaiproject.evaluation.utils.TextTemplateLogicUtils;

import org.sakaiproject.genericdao.api.search.Search;


/**
 * Build and send available and reminder single email to assigned groups
 * that need email notification. Methods in this bean are intentionally
 * not under global transaction management.
 * 
 * @author rwellis
 *
 */
public class EvalSingleEmailLogicImpl implements EvalSingleEmailLogic {
	
	private static Log log = LogFactory.getLog(EvalSingleEmailLogicImpl.class);
	private static Log metric = LogFactory.getLog("metrics." + EvalSingleEmailLogicImpl.class.getName());
	
	private EvalCommonLogic commonLogic;
	public void setCommonLogic(EvalCommonLogic commonLogic) {
		this.commonLogic = commonLogic;
	}
	private EvaluationDao dao;
	public void setDao(EvaluationDao dao) {
		this.dao = dao;
	}
	private EvalEvaluationService evaluationService;
	public void setEvaluationService(EvalEvaluationService evaluationService) {
		this.evaluationService = evaluationService;
	}
	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
	}
	
	/**
	 * Initialize polling for rows in the holding tables.
	 */
	public void init() {
		String serverId = commonLogic.getConfigurationSetting(EvalExternalLogic.SETTING_SERVER_ID, "UNKNOWN_SERVER_ID");
		//clear email locks on startup
		List<EvalLock> locks = dao.obtainLocksForHolder(EvalConstants.EMAIL_LOCK_PREFIX, serverId);
		if(log.isDebugEnabled()) {
			log.debug(this + ".init() serverId '" + serverId + "' held " + locks.size() + " locks at startup. ");
		}
		if(locks != null && !locks.isEmpty()) {
			for(EvalLock lock:locks) {
				dao.releaseLock(lock.getName(), externalLogic.getCurrentUserId());
				if(log.isDebugEnabled()) {
					log.debug(this + ".init() lock '" + lock.getName() + "' held by '" + lock.getHolder() + " was released. ");
				}
			}
		}
		// if set initiate polling for rows in the holding tables
		if(((Integer)settings.get(settings.EMAIL_SEND_QUEUED_REPEAT_INTERVAL)) != null &&
			((Integer)settings.get(settings.EMAIL_SEND_QUEUED_START_INTERVAL)) != null) {
			//and settings aren't 0
			if((((Integer)settings.get(settings.EMAIL_SEND_QUEUED_REPEAT_INTERVAL)).intValue() != 0) &&
				((Integer)settings.get(settings.EMAIL_SEND_QUEUED_START_INTERVAL)).intValue() != 0) {
				//and window for sending is active
				if(((Boolean)settings.get(EvalSettings.EMAIL_SEND_QUEUED_ENABLED)) != null) {
					if(log.isInfoEnabled()) {
						log.info("EvalEmailLogicImpl.init() initiate polling for queued email. ");
					}
					// polling for queued groups and email
					initiateQueuedEmailTimer();
	    		  }
	    		  else {
	    			  if(log.isInfoEnabled())
	        			  log.info("EvalEmailLogicImpl.init() polling for queued email is disabled. ");
	    		  }
	    	  }
	    	  else {
	    		  if(log.isInfoEnabled())
	    			  log.info("EvalEmailLogicImpl.init() polling repeat or start interval is 0 (never)" +
	    			  		" so polling for queued email is disabled. ");
	    	  }
	      }
	      else {
				  if(log.isInfoEnabled())
	    			  log.info("EvalEmailLogicImpl.init() polling for queued email settings are null. ");
	      }
	  }
	
	/**
	 * Start polling for records in holding tables used to distribute the work in sending single
	 * emails among cluster nodes.  Locks are used to create non-overlapping sets of records that
	 * may be claimed to avoid more than one cluster node sending a specific email. Keep logging
	 * minimal because this task runs frequently.
	 */
	protected void initiateQueuedEmailTimer() {
		//hold lock for 48 hr so it isn't taken by another server before being released
		final long holdLock = 1000 * 60 * 60 * 48;
		// check the holding tables every EMAIL_SEND_QUEUED_REPEAT_INTERVAL minutes
		final long repeatInterval = 1000 * 60 * ((Integer)settings.get(settings.EMAIL_SEND_QUEUED_REPEAT_INTERVAL)).intValue();
		// first run after EMAIL_SEND_QUEUED_START_INTERVAL minutes and a random delay
		long startDelay =  (1000 * 60 * ((Integer)settings.get(settings.EMAIL_SEND_QUEUED_START_INTERVAL)).intValue())
						 + (1000 * 60 * new Random().nextInt(10));
		TimerTask runQueuedEmailTask = new TimerTask() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				if(dao.countQueuedEmail(null) == 0 && dao.countQueuedGroups(null) == 0) {
					return;
				}
				// get the id of this server (or cluster node)
				String serverId = commonLogic.getConfigurationSetting(EvalExternalLogic.SETTING_SERVER_ID, "UNKNOWN_SERVER_ID");
				if(isSingleEmailEnabled() && isSingleEmailRequired()) {
					// get assigned group members and build email to be sent
					buildSingleEmail(serverId, holdLock);
					// read and send email, with throttling if set
					sendSingleEmail(serverId, holdLock);
					// clean up email once it has all been sent
					cleanupSingleEmail(serverId, holdLock);
				}
	    	 }
	      };
	      Timer timer = new Timer(true);
	      if(log.isDebugEnabled()) {
	    	  log.debug(this + ".initiateQueuedEmailTimer(): initializing checking for queued email, first run in " + (startDelay/1000) + " seconds " +
	      		"and subsequent runs every " + (repeatInterval/1000) + " seconds. ");
	      }
	      timer.schedule(runQueuedEmailTask, startDelay, repeatInterval);
      }
	
	/**
	 * Delete records from the email and group holding tables.
	 * 
	 * @param serverId
	 * 			the identifier of this server
	 * @param holdLock
	 * 			the length of time this server holds a lock
	 */
	private void cleanupSingleEmail(String serverId, long holdLock) {
		// locks prevent more than one server from deleting the records in the holding tables
		String emailLockName = "remove_email_lock";
		String groupLockName = "remove_group_lock";
		Boolean unsent = new Boolean(false);
		Boolean unbuilt = new Boolean(false);
		Boolean logEmailRecipients = (Boolean) settings.get(EvalSettings.LOG_EMAIL_RECIPIENTS);
		// delete email
		Boolean lockObtained = dao.obtainLock(emailLockName, serverId, holdLock);
		if(lockObtained) {
			// get email marked as having been sent
			List<Long> ids = dao.getQueuedEmailIds();
			int total = ids.size();
			if(total > 0) {
				// if logging recipients do it before deleting email
				if(logEmailRecipients) {
					List recipients = dao.getQueuedEmailAddresses();
					if(recipients != null) {
						logRecipients(recipients);
					}
				}
				// remove email marked as having been sent
				dao.removeQueuedEmails();
				if(metric.isInfoEnabled()) {
					metric.info("metric " + this + ".cleanupSingleEmail():  " + serverId + ": deleted " + total + " emails from the EVAL_QUEUED_EMAIL table.");
				}
			}
			dao.releaseLock(emailLockName, serverId);
		}
		else {
			// someone else has the lock so let them delete email
			return;
		}
		// delete groups
		lockObtained = dao.obtainLock(groupLockName, serverId, holdLock);
		if(lockObtained) {
			// get groups marked as having had email built
			List<Long> ids = dao.getQueuedGroupIds();
			int total = ids.size();
			if(total > 0) {
				// set available email sent
				List<Long> evalIds = dao.getEvaluationIdsFromQueuedGroups(EvalConstants.SINGLE_EMAIL_AVAILABLE);
				for(Long id : evalIds) {
					EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, id);
					eval.setAvailableEmailSent(true);
					dao.save(eval);
				}
				// remove groups marked as having had email built
				dao.removeQueuedGroups();
				if(metric.isInfoEnabled()) {
					metric.info("metric " + this + ".cleanupSingleEmail():  " + serverId + ": deleted " + total + " groups from the EVAL_QUEUED_GROUP table.");
				}
			}
			dao.releaseLock(groupLockName, serverId);
			if (metric.isInfoEnabled()) {
				metric.info("metric " + this + ".cleanupSingleEmail(): Finished.");
			}
		}
		// the tables should be empty now
		if(dao.countQueuedEmail(null) > 0) {
			log.warn(this + ".cleanupSingleEmail(): EvalQueuedEmail object(s) in EVAL_QUEUED_EMAIL after clean up.");
		}
		if(dao.countQueuedGroups(null) > 0) {
			log.warn(this + ".cleanupSingleEmail(): EvalQueuedGroup object(s) in EVAL_QUEUED_GROUP after clean up.");
		}
	}
	
	/**
	 * Get an email based on its unique id
	 * 
	 * @param emailId 
	 * 			the unique id of an {@link EvalQueuedEmail} object
	 * @return 
	 * 			the email object or null if not found
	 */   
	private EvalQueuedEmail getQueuedEmailById(Long emailId) {
		EvalQueuedEmail email = (EvalQueuedEmail) dao.findById(EvalQueuedEmail.class, emailId);
		return email;
	}

	/**
     * Read the records in the queued group holding table and use each to generate queued email messages.
     * Mark records in the group holding table as corresponding email is built.
     * 
     * @param serverId
     * 			the identity of the server on which the TimerTask is running
     * @param holdLock
     * 			the length of time to hold the lock while sending email (essentially forever)
    */
	public void buildSingleEmail(String serverId, long holdLock) {

	   // a server holds one lock at a time
	   if(isLockHeld(EvalConstants.GROUP_LOCK_PREFIX, serverId)) {
		   if(log.isDebugEnabled()) {
			   log.debug(this + ".buildSingleEmail(): server " + serverId + " already holds a GROUP_LOCK: Done.");
		   }
		   return;
	   }
	   // no lock held so try to acquire one
	   if(log.isDebugEnabled()) {
    		log.debug(this + ".buildSingleEmail(): " + serverId +": server has no locks: trying to acquire one.");
	   }
	   int groupsProcessed = 0;
	   List<Long> groupIds = new ArrayList<Long>();
	   EvalQueuedGroup group = null;
	   EvalEvaluation eval = null;
	   Set<String> userIdsTakingEval = new HashSet<String>();
	   Map<String, String> replacementValues = new HashMap<String, String>();
	   String groupId = null;
	   Long availableEmailTemplate = null;
	   Long reminderEmailTemplate = null;
	   Boolean unbuilt = new Boolean(false);
		
	   // get locks in the EVAL_QUEUED_GROUP table
	   List<String> lockNames = dao.getQueuedGroupLocks(unbuilt);
	   if(log.isDebugEnabled()) {
    		log.debug(this + ".buildSingleEmail(): " + serverId + ": server found " + lockNames.size() + " lock names in the EVAL_QUEUED_GROUP table.");
	   }
	   
	   // go through the group locks trying to claim one
	   for(String lockName: lockNames) {
		   if(log.isDebugEnabled()) {
			   log.debug(this + ".buildSingleEmail(): " + serverId + ": server is trying to obtain lock " + lockName + ". ");
		   }
		   // try to get a lock
		   Boolean lockObtained = dao.obtainLock(lockName, serverId, holdLock);
		   
		   // only execute the code if we have an exclusive lock
		   if (lockObtained != null && lockObtained) {
			   if(log.isDebugEnabled()) {
				   log.debug(this + ".buildSingleEmail(): " + serverId + ": server obtained lock " + lockName + ". ");
			   }
			   groupIds.clear();
			   
			   // claim the groups associated with this lock
			   groupIds = dao.getQueuedGroupsByLockName(lockName);
			   
			   // log progress
			   if(metric.isInfoEnabled()) {
				   metric.info("metric " + this + ".buildSingleEmail(): " + serverId + ": server claimed " + groupIds.size() + " queued groups.");
			   }
			   for(Long id: groupIds) {
				   try {
					   groupId = id.toString();
					   group = dao.findById(EvalQueuedGroup.class, id);
					   eval = dao.findById(EvalEvaluation.class, group.getEvaluationId());
					   if(log.isDebugEnabled()) {
	               			log.debug(this + ".buildSingleEmail(): " + serverId + ": retrieved group " + group.getGroupId() + 
	               				" for evaluation " + group.getEvaluationId() + " and email type " + group.getEmailType());
					   }
					   
					   availableEmailTemplate = eval.getAvailableEmailTemplate().getId();
					   reminderEmailTemplate = eval.getReminderEmailTemplate().getId();
					  
					   // TODO getLockName duplication here and in EvalEmailLogicImpl
					   String emailLock = getLockName(EvalConstants.EMAIL_LOCK_PREFIX);
					   
					   if(EvalConstants.SINGLE_EMAIL_AVAILABLE.equals(group.getEmailType())) {
						   // get those in the group taking the evaluation
						   userIdsTakingEval.addAll(evaluationService
									.getUserIdsTakingEvalInGroup(group.getEvaluationId(), group.getGroupId(),EvalConstants.EVAL_INCLUDE_ALL));
						   
						   // customize email and save in holding table
						   customizeAndSave(userIdsTakingEval, emailLock, availableEmailTemplate, serverId);
						   // all email for this group written to holding table
					   }
					   else if(EvalConstants.SINGLE_EMAIL_REMINDER.equals(group.getEmailType())) {
						   // get those who haven't responded
						   userIdsTakingEval = evaluationService
									.getUserIdsTakingEvalInGroup(group.getEvaluationId(), group.getGroupId(), EvalConstants.EVAL_INCLUDE_NONTAKERS);
						   
						   // customize email and save in holding table
						   customizeAndSave(userIdsTakingEval, emailLock, reminderEmailTemplate, serverId);
						   // all email for this group written to holding table
					   }	   
					   Integer every = (Integer) settings.get(EvalSettings.LOG_PROGRESS_EVERY);
					   logSingleEmail(groupsProcessed, every, EvalConstants.SINGLE_EMAIL_BUILT);
					   group.setEmailBuilt(true);
					   dao.save(group);
					   groupsProcessed++;
					   if(log.isDebugEnabled()) {
						   log.debug(this + ".buildSingleEmail(): " + serverId + ": set email built to true in the EVAL_QUEUED_GROUP holding table." +
								   			group.getGroupId() + ". ");
					   }
				   }
				   catch(Exception e) {
					   log.error(this + ".buildSingleEmail(): group id '" + groupId + "' exception, continuing with next group id. " + e);
					   continue;
				   }
	
			   } // for the groups associated with the lock
			   dao.releaseLock(lockName, serverId);
			   if(log.isDebugEnabled()) {
				   log.debug(this + ".buildSingleEmail(): " + serverId + ": server released lock " + lockName + ". "); 
			   }
			   // quit after processing 1 lock
			   if(log.isDebugEnabled()) {
				   log.debug(this + ".buildSingleEmail(): " + serverId + ": quit after processing 1 lock.");
			   }
			   break;
		   }
		   else {
			   // if we didn't get a lock the first time try again
			   if(log.isDebugEnabled()) {
				   log.debug(this + ".buildSingleEmail(): " + serverId + ": server didn't obtained lock "
            												+ lockName + ". Trying to obtain the next lock.");
			   }
            }
       	}
      	if(log.isDebugEnabled()) {
    		log.debug(this + ".buildSingleEmail(): " + serverId + ": " + new Integer(groupsProcessed) + " group(s) processed. Done.");
      	}
  	}
	
	/**
	 * Customize the message and subject of email for an 
	 * individual user, and save in the email holding table.
	 * 
	 * @param userIdsTakingEval
	 * 			the individual users
	 * @param emailLock
	 * 			a lock to be associated with the queued email records
	 * @param template 
	 * 			the identity of the email template to customize
	 * 			
	 */
	protected void customizeAndSave(Set<String> userIdsTakingEval, String emailLock,
		Long emailTemplateId, String serverId) {
		if(userIdsTakingEval == null || emailLock == null || emailTemplateId == null) {
			log.error(this + ".customizeAndSave():  " + serverId + ": parameter(s) null.");
			return;
		}
		String toolTitle = externalLogic.getEvalToolTitle();
		String systemUrl = externalLogic.getServerUrl();
		EvalEmailTemplate emailTemplate = dao.findById(EvalEmailTemplate.class, emailTemplateId);
		int total = 0;
		for(String userId : userIdsTakingEval) {
			try {
				EvalUser user = externalLogic.getEvalUserById(userId);
				String url = externalLogic.getMyWorkspaceUrl(userId);
				String earliest = evaluationService.getEarliestDueDate(userId);
				saveEmail(user, url, systemUrl, earliest, emailTemplate, emailLock, toolTitle);
				total++;
			}
			catch(Exception e) {
				log.error(this + ".customizeAndSave():  " + serverId + ": exception, continuing with next user." + e);
				continue;
			}
		}
		if(metric.isInfoEnabled()) {
			metric.info("metric " + this + ".customizeAndSave():  " + serverId + ": " + total + " emails saved in EVAL_QUEUED_EMAIL table.");
		}
	}
	
	/**
	 * Save email content to be sent to a user in EVAL_QUEUED_EMAIL
	 * 
	 * @param user
	 * 			the use to whom email is addressed
	 * @param url
	 * 			the url link to the user's evaluations
	 * @param systemUrl
	 * 			the url link to the system
	 * @param earliest
	 * 			the earliest due date for the user's active evaluations
	 * @param emailTemplate
	 * 			the email template to use to generate the email content
	 * @param emailLock
	 * 			the lock preventing more than one server from sending the same email
	 * @param toolTitle
	 * 			the title of the evaluation tool
	 */
	protected void saveEmail(EvalUser user, String url, String systemUrl, String earliest, 
							EvalEmailTemplate emailTemplate, String emailLock, String toolTitle) {
		EvalQueuedEmail email = new EvalQueuedEmail();
		Map<String, String> replacementValues = new HashMap<String, String>();
		try {
			replacementValues = getReplacementValues(url, systemUrl, earliest, toolTitle);
			String message = makeEmailMessage(emailTemplate.getMessage(), replacementValues);
			String subject = makeEmailMessage(emailTemplate.getSubject(), replacementValues);
			email.setCreationDate(new Date());
			email.setMessage(message);
			email.setSubject(subject);
			email.setToAddress(user.email);
			email.setEmailLock(emailLock);
			email.setEmailTemplateId(emailTemplate.getId());
			// TODO plug in Task Status service
			email.setTSStreamId(new Long(1l));
			email.setEmailSent(new Boolean(false));
		
			// check row is unique
			long rows = dao.countBySearch(EvalQueuedEmail.class, new Search(new String[] {"toAddress","emailTemplateId"}, 
											new Object[] {email.getToAddress(), email.getEmailTemplateId()}));
			if(rows == 0) {
				dao.save(email);
				if(log.isDebugEnabled()) {
					log.debug(this + ".customizeAndSave(): saved: id '" + email.getId() + "' to '" + 
							   			email.getToAddress() + "' email template id '" + email.getEmailTemplateId() + "'");
				}
			}
			else {
				if(log.isDebugEnabled()) {
					log.debug(this + ".customizeAndSave(): found existing row for " + email.getToAddress() + 
									" email template id " + email.getEmailTemplateId());
				}
			}
		}
		catch(Exception e) {
			log.error(this + ".saveEmail() " + e);
		}
	}

	/**
	 * Get the values to be substituted in the email template.
	 * 
	 * @param userId
	 * 			the Sakai user id
	 * @return
	 * 			a map of substitution variables and their values
	 */
	private Map<String, String> getReplacementValues(String url, String systemUrl, String earliest, String toolTitle) {
		Map<String, String> replacementValues = new HashMap<String, String>();
		// direct link to summary page of tool on My Worksite
		replacementValues.put("MyWorkspaceDashboard", url);
		replacementValues.put("URLtoSystem", systemUrl);
		replacementValues.put("EarliestEvalDueDate", earliest);
		replacementValues.put("HelpdeskEmail", (String) settings.get(EvalSettings.FROM_EMAIL_ADDRESS));
		replacementValues.put("EvalToolTitle", toolTitle);
		replacementValues.put("EvalSite", EvalConstants.EVALUATION_TOOL_SITE);
		replacementValues.put("EvalCLE", EvalConstants.EVALUATION_CLE);
		return replacementValues;
	}

	/**
    * Read the records in the queued email holding table and send each as a single email message.
    * 
    * Re locking - Another server may acquire a lock if the repeatInterval is shorter than the 
    * TimerTask's processing time. This would defeat the purpose of a server holding a lock while 
    * processing a batch of email. We set the repeatInterval to a very high value and refresh 
    * the lock by obtaining it periodically until the TimerTask processing has finished as a 
    * precaution against the server losing its lock before finishing. 
    * 
    * TODO: CT-798 TQ: server id as holder of lock prevents re-obtaining lock after restart
    * 
    * @param serverId
    * 			the identity of the server on which the TimerTask is running
    * @param holdLock
    * 			the length of time to hold the lock while sending email (essentially forever)
    */
	public void sendSingleEmail(String serverId, long holdLock) {
		String emailId = null;
		int emailsProcessed = 0;
			   
	   // a server holds one lock at a time
	   if(isLockHeld(EvalConstants.EMAIL_LOCK_PREFIX, serverId)) {
		   if(log.isDebugEnabled()) {
			   log.debug(this + ".sendSingleEmail(): server " + serverId + " already holds EMAIL_LOCK: Done.");
		   }
		   return;
	   }
	   
	   // no lock held so try to acquire one
	   if(log.isDebugEnabled()) {
    		log.debug(this + ".sendSingleEmail(): " + serverId +": server has no locks: trying to acquire one.");
	   }
	   
	   List<Long> emailIds = new ArrayList<Long>();
	   EvalQueuedEmail email = null;
	   
	   // get locks in the queued email table
	   List<String> lockNames = dao.getQueuedEmailLocks();
	   if(log.isDebugEnabled()) {
    		log.debug(this + ".sendSingleEmail(): " + serverId + ": server found " + lockNames.size() + " lock names in the EVAL_QUEUED_EMAIL table.");
	   }
	   
	   for(String lockName: lockNames) {
		   if(log.isDebugEnabled()) {
			   log.debug(this + ".sendSingleEmail(): " + serverId + ": server is trying to obtain lock " + lockName + ". ");
		   }

		   Boolean lockObtained = dao.obtainLock(lockName, serverId, holdLock);
		   
		   // only execute the code if we have an exclusive lock
		   if (lockObtained != null && lockObtained) {
			   
			   // process notifications for this lock and then quit
			   if(log.isDebugEnabled()) {
				   log.debug(this + ".sendSingleEmail(): " + serverId + ": server obtained lock " + lockName + ". ");
			   }
			   emailIds.clear();
			   
			   // claim the emails associated with this lock
			   emailIds = dao.getQueuedEmailByLockName(lockName);
			   
			   // log progress
			   if(metric.isInfoEnabled()) {
				   metric.info("metric " + this + ".sendSingleEmail(): " + serverId + ": server claimed " + emailIds.size() + " queued emails.");
			   }
			   //Note: an array version of send should be faster
			   boolean deferExceptions = true;
			   for(Long id: emailIds) {
				   try {
					   emailId = id.toString();
					   email = dao.findById(EvalQueuedEmail.class, id);
					   if(log.isDebugEnabled()) {
	                		log.debug(this + ".sendSingleEmail(): " + serverId + ": retrieved queued email " + email.toString() + ". ");
					   }
					   String from = (String) settings.get(EvalSettings.FROM_EMAIL_ADDRESS);
					   String[] to = new String[]{email.getToAddress()};
					   // send email and update email sent flag
					   commonLogic.sendEmailsToAddresses(from, to, email.getSubject(), email.getMessage(), deferExceptions);
					   email.setEmailSent(Boolean.TRUE);
					   dao.save(email);
					   // log email if that is set
					   String deliveryOption = (String) settings.get(EvalSettings.EMAIL_DELIVERY_OPTION);
					   if (deliveryOption.equals(EvalConstants.EMAIL_DELIVERY_LOG)) {
						   log.info(this + ".sendSingleEmail(): " + serverId + ": sent email: " + email.toString());
					   }
					   emailsProcessed++;
					   Integer batch = (Integer) settings.get(EvalSettings.EMAIL_BATCH_SIZE);
					   Integer wait = (Integer) settings.get(EvalSettings.EMAIL_WAIT_INTERVAL);
					   // inject a wait if set
					   throttleDelivery(emailsProcessed, batch, wait);
					   Integer every = (Integer) settings.get(EvalSettings.LOG_PROGRESS_EVERY);
					   // log progress every so many if set
					   logSingleEmail(emailsProcessed, every, EvalConstants.SINGLE_EMAIL_SENT);
				   }
				   catch(Exception e) {
					   log.error(this + ".sendSingleEmail(): email id '" + emailId + "' exception, continuing with next email id. " + e);
					   continue;
				   }
			   }
			   
			   // release lock
			   dao.releaseLock(lockName, serverId);
			   if(log.isDebugEnabled()) {
				   log.debug(this + ".sendSingleEmail(): " + serverId + ": server released lock " + lockName + ". "); 
			   }
			   break;
            }
            else {
            	// didn't get a lock, try again
            	if(log.isDebugEnabled()) {
            		log.debug(this + ".sendSingleEmail(): " + serverId + ": server didn't obtained lock "
            												+ lockName + ". Trying to obtain the next lock.");
              	}
            }
       	}

	   if(log.isDebugEnabled()) {
		   log.debug(this + ".sendSingleEmail(): " + serverId + ": " + new Integer(emailsProcessed) + " emails sent. Done.");
	   }
	}
	
	/**
	 * Log progress processing single emails if metric logger is info enabled.
	 * 
	 * @param numProcessed
	 * 			the number processed so far (groups or emails)
	 * @param every
	 * 			the every so many when progress is logged
	 * @param action
	 * 			the action being logged EvalConstants.SINGLE_EMAIL_BUILT 
	 * 			or EvalConstants.SINGLE_EMAIL_SENT
	 */
	private void logSingleEmail(int numProcessed, Integer every, String action) {
		if(every != null && numProcessed > 0) {
			if(metric.isInfoEnabled()) {
				StringBuffer buf = new StringBuffer();
				if(every.intValue() > 0) {
					if ((numProcessed % every.intValue()) == 0) {
						buf.append(this + ".logSingleEmail(): metric " + new Long(numProcessed));
						if(EvalConstants.SINGLE_EMAIL_BUILT.equals(action)) {
							buf.append(" groups read.");
						}
						else if (EvalConstants.SINGLE_EMAIL_SENT.equals(action)) {
							buf.append(" emails sent.");
						}
						metric.info(new String(buf.toString()));
					}
				}
			}
		}
	}
	
	/**
	 * Wait a certain number of seconds after a certain number of emails have been sent.
	 * This should make it possible to moderate the server load spike that can occur if
	 * many evaluatees receive email and click on the embedded link at the same time.
	 * 
	 * @param numProcessed
	 * 			the number of emails sent so far
	 * @param batch
	 * 			the number of emails to send as a batch before waiting
	 * @param wait
	 * 			the length of time to wait
	 */
	private void throttleDelivery(int numProcessed, Integer batch, Integer wait) {
		if(batch != null && wait != null && numProcessed > 0) {
			if(wait.intValue() > 0) {
				if ((numProcessed % batch.intValue()) == 0) {
					if(log.isInfoEnabled()) {
						log.info(this + ".throttleEmailDelivery(): wait " + wait + " seconds.");
					}
					try {
						Thread.sleep(wait * 1000);
					} catch (Exception e) {
						log.error(this + ".throttleDelivery(): thread sleep interrupted.");
					}
				}
			}
		}
	}
	
	/**
	 * Check settings that can disable single email
	 * 
	 * @return
	 * 			true if single email is enabled, false otherwise
	 */
	protected boolean isSingleEmailEnabled() {
		boolean enabled = true;
		// check settings that can disable single email
		if(((Integer)settings.get(settings.EMAIL_SEND_QUEUED_REPEAT_INTERVAL)).intValue() == 0 ) {
			if(log.isDebugEnabled()) {
				log.debug(this + ".isSingleEmailEnabled(): EMAIL_SEND_QUEUED_REPEAT_INTERVAL = 0: Done. "); 
			}
			enabled = false;
		}
		if(((Integer)settings.get(settings.EMAIL_SEND_QUEUED_START_INTERVAL)).intValue() == 0 ) {
			if(log.isDebugEnabled()) {
				log.debug(this + ".isSingleEmailEnabled():  EMAIL_SEND_QUEUED_START_INTERVAL = 0: Done. "); 
			}
			enabled = false;
		}
		// check the enabling flag (can be set/unset using 2 registered Job Scheduler jobs)
		if(!(((Boolean)settings.get(EvalSettings.EMAIL_SEND_QUEUED_ENABLED))).booleanValue()) {
			if(log.isDebugEnabled()) {
				log.debug(this + ".isSingleEmailEnabled(): EMAIL_SEND_QUEUED_ENABLED = false: Done. "); 
			}
			enabled = false;
		}
		if(!enabled) {
			log.warn(this + ".isSingleEmailEnabled(): single email not enabled: Done.");
		}
		return enabled;
	}
	
	/**
	 * Check that there is a reason to process email. 
	 * 
	 * @return 
	 * 			true if send/log or log recipients, false otherwise
	 */
	protected boolean isSingleEmailRequired() {
		boolean workToDo = true;
		String deliveryOption = (String) settings.get(EvalSettings.EMAIL_DELIVERY_OPTION);
		Boolean logEmailRecipients = (Boolean) settings.get(EvalSettings.LOG_EMAIL_RECIPIENTS);
		if (deliveryOption.equals(EvalConstants.EMAIL_DELIVERY_NONE) && !logEmailRecipients.booleanValue()) {
			if (log.isDebugEnabled()) {
				log.debug(this + ".isSingleEmailRequired(): delivery option EMAIL_DELIVERY_NONE and not logging recipients. Done.");
			}
			workToDo = false;
		}
		if(!workToDo) {
			log.warn(this + ".isSingleEmailRequired(): not sending/logging or logging recipients: Done.");
		}
		return workToDo;
	}
	
	/**
	 * Check if this server holds a lock of the specified type. A server holds one lock at a time.
	 * 
	 * @param lockType
	 * 			either EvalConstants.EMAIL_LOCK_PREFIX or EvalConstants.GROUP_LOCK_PREFIX
	 * 			
	 * @param serverId
	 * 			the identity of the server
	 * @return
	 * 			true if a lock of the type is held by the server, false otherwise
	 */
	protected boolean isLockHeld(String lockType, String serverId) {
		boolean held = false;
		if(log.isDebugEnabled()) {
			log.debug(this + ".isLockHeld: " + serverId + ": checking if server holds a lock.");
		}
		List<EvalLock> locks = dao.obtainLocksForHolder(lockType, serverId);
		if(locks != null && !locks.isEmpty()) {
			if(log.isDebugEnabled()) {
				log.debug(this + ".isLockHeld: " + serverId + ": server has a lock. ");
				for(EvalLock lock:locks) {
					log.debug(this + ".isLockHeld: " + serverId + ": has lock " + lock.getName() + ". Done");
				}
			}
			held = true;;
		}
		return held;
	}
	
	/**
	 * Get the number to use in a lock name suffix.
	 * TODO duplicates code in EvalEmailLogic
	 * 
	 * @param start
	 * 			the starting value
	 * @param sets
	 * 			the EMAIL_LOCKS_SIZE setting
	 * @return
	 * 			the selected value
	 */
	protected Integer getLockNameSuffix(int start, int sets) {
		if(sets == 0) {
			return 0;
		}
		Integer set = start % sets;
		return set;
	}
	
	/**
	 * Cycle through a range of numbers creating unique lock names.
	 * TODO duplicates code in EvalEmailLogic
	 * 
	 * @param lockType
	 * 			EvalConstants.EMAIL_LOCK_PREFIX or EvalConstants.GROUP_LOCK_PREFIX
	 * @return
	 * 		a lock name based on settings
	 */
	private String getLockName(String lockType) {
		StringBuffer buf = new StringBuffer();
		buf.append(lockType);
		// use same number of locks for group and email
		int sets = ((Integer)settings.get(EvalSettings.EMAIL_LOCKS_SIZE)).intValue();
		// a random starting number
		int start = 0;
		if(sets > 0) {
			// int value between 0 (inclusive) and the value of EMAIL_LOCKS_SIZE (exclusive)
			start = new Random().nextInt(sets);
		}
		String lockName = lockType + getLockNameSuffix(start, sets).toString();
		start++;
		return lockName;
	}
	
	/**
	 * TODO duplicate method in EvalEmailLogic
	 * Builds the single email message from a template and a bunch of variables
	 * (passed in and otherwise)
	 * @param messageTemplate
	 * @param replacementValues a map of String -> String representing $keys in the template to replace with text values
	 * @return the processed message template with replacements and logic handled
	 */
	private String makeEmailMessage(String messageTemplate, Map<String, String> replacementValues) {
		return TextTemplateLogicUtils.processTextTemplate(messageTemplate, replacementValues);
	}
	
   /**
    * If metrics logger for class is set with info level logging,
    * log a sorted list of email recipients at end of job, 25 per line
    * 
    * @param toAddresses
    * 			the list of String email addresses of email recipients
    */
   private void logRecipients(List<String> toAddresses) {
	   if(toAddresses != null && !toAddresses.isEmpty()) {
		   StringBuffer sb = new StringBuffer();
		   String line = null;
		   boolean first = true;
		   // for each recipient
		   for(int i = 0; i < toAddresses.size(); i++) {
			   if(!first) {
				   sb.append(",");
			   }
			   // add a recipient
			   sb.append((String)toAddresses.get(i));
			   first = false;
			   // every 25
			   if((i+1) % 25 == 0) {
				   // write out a fixed number of names
				   line = sb.toString();
				   if(metric.isInfoEnabled()) {
					   metric.info("metric " + this + ".logRecipients():  email sent to [" + line + "]");
				   }
				   //write a line and empty the buffer
				   sb.setLength(0);
				   first = true;
			   }
		   }
		   //if anything hasn't been written do it now
		   if(sb.length() > 0) {
			   line = sb.toString();
			   if(metric.isInfoEnabled()) {
				   metric.info("metric " + this + ".logRecipients():  email sent to [" + line + "]");
			   }
		   }
	   }
   }
}
	   

