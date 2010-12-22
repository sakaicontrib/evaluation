/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2010 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.evaluation.tool;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;


/**
 * ProviderSyncBean
 *
 */
public class ProviderSyncBean {
	
	public static final String JOB_GROUP_NAME = "org.sakaiproject.evaluation.tool.ProviderSyncBean";
	
	public static final String RESULT_FAILURE = "failure";
	public static final String RESULT_SUCCESS = "success";
	
	public static final String SPRING_BEAN_NAME = JobBeanWrapper.SPRING_BEAN_NAME;
	public static final long MILLISECONDS_PER_MINUTE = 60L * 1000L;
	public static final long DELAY_IN_MINUTES = 2L;

	private Log logger = LogFactory.getLog(ProviderSyncBean.class);
	
	private static final String SPACE = " ";

    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }
    
    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings settings) {
        this.evalSettings = settings;
    }

    private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
        this.messages = messages;
    }
    
    public Boolean syncUnassignedOnStartup;
    public Boolean syncOnStateChange;
    public Boolean syncOnGroupSave;
    public Boolean syncOnGroupUpdate;

    public String triggerName;
	public String cronExpression;
	
	public Boolean partial;
	public Boolean inqueue;
	public Boolean active;
	public Boolean graceperiod;
	public Boolean closed;
	
	public Integer tab;
	
	public String syncServerId;
	
	public void init() {
		logger.info("init()");
	}
	
	public String scheduleSync() {
		logger.info("scheduleSync() ");
		boolean error = scheduleCronJob();
		
		if(this.syncServerId != null) {
			this.evalSettings.set(EvalSettings.SYNC_SERVER, this.syncServerId);
		}
		
		logger.info("scheduleSync() error == " + error);
		return (error ? RESULT_FAILURE : RESULT_SUCCESS);
	}

	/**
	 * Create a new cron job and register it with the job scheduler. Parameters (cron expression and eval states) are read from the
	 * values updated in the request.
	 *  
	 * @return true if the method successfully scheduled the cron job, and false if some error prevented the cron job from being scheduled.
	 */
	protected boolean scheduleCronJob() {
		boolean error = false;
		if(cronExpression == null || cronExpression.trim().equals("")) { 
			//messages.addMessage(new TargettedMessage("administrate.sync.cronExpression.null", null, TargettedMessage.SEVERITY_ERROR));
			error = true;
			
		} else if(! CronExpression.isValidExpression(cronExpression)) {
			// send an error message through targeted messages
			//messages.addMessage(new TargettedMessage("administrate.sync.cronExpression.err", new Object[]{ cronExpression.trim() }, TargettedMessage.SEVERITY_ERROR));
			error = true;
		} 

		String states = null;
		if(! error) {
			
			states = getStateValues();
			if(states == null || states.trim().equals("")) {
				//messages.addMessage(new TargettedMessage("administrate.sync.stateList.null", null, TargettedMessage.SEVERITY_ERROR));
				error = true;
			}
		}

		if(! error){
			try {
				String uniqueId = EvalUtils.makeUniqueIdentifier(99);
				String triggerName = uniqueId;
				String triggerGroup = "org.sakaiproject.evaluation.tool.ProviderSyncBean";
				String jobName = uniqueId;
				CronTrigger trigger = new CronTrigger(triggerName,triggerGroup,jobName,JOB_GROUP_NAME,cronExpression);
				logger.info("Created trigger: " + trigger.getCronExpression());
				
				//ComponentManager componentManager = (ComponentManager) this.externalLogic.getBean(ComponentManager.class);
				Object jobClass = ComponentManager.get("org.sakaiproject.api.app.scheduler.JobBeanWrapper.GroupMembershipSync");
				// create job
				JobDataMap jobDataMap = new JobDataMap();
				jobDataMap.put(EvalConstants.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST, states);
				jobDataMap.put(SPRING_BEAN_NAME, EvalConstants.GROUP_MEMBERSHIP_SYNC_BEAN_NAME);
				JobDetail jobDetail = new JobDetail();
				jobDetail.setJobDataMap(jobDataMap);
				jobDetail.setJobClass(jobClass.getClass());
				
				jobDetail.setName(jobName);
				jobDetail.setGroup(JOB_GROUP_NAME);
				
				externalLogic.scheduleCronJob(trigger, jobDetail);
				
				//messages.addMessage(new TargettedMessage("administrate.sync.job.success", null, TargettedMessage.SEVERITY_INFO));
				
			} catch (ParseException e) {
				// send an error message through targeted messages
				//messages.addMessage(new TargettedMessage("administrate.sync.cronExpression.err", new Object[]{ cronExpression.trim() }, TargettedMessage.SEVERITY_ERROR));
				error = true;
			}
		}
		return !error;
	}

	/**
	 * Read the values of eval states from the request and return them as a space-delimited string.
	 * 
	 * @return
	 */
	protected String getStateValues() {
		String states;
		StringBuilder stateList = new StringBuilder();
		if(partial != null && partial.booleanValue()) {
			stateList.append(EvalConstants.EVALUATION_STATE_PARTIAL);
			stateList.append(" ");
		}
		if(inqueue != null && inqueue.booleanValue()) {
			stateList.append(EvalConstants.EVALUATION_STATE_INQUEUE);
			stateList.append(" ");
		}
		if(active != null && active.booleanValue()) {
			stateList.append(EvalConstants.EVALUATION_STATE_ACTIVE);
			stateList.append(" ");
		}
		if(graceperiod != null && graceperiod.booleanValue()) {
			stateList.append(EvalConstants.EVALUATION_STATE_GRACEPERIOD);
			stateList.append(" ");
		}
		if(closed != null && closed.booleanValue()) {
			stateList.append(EvalConstants.EVALUATION_STATE_CLOSED);
			stateList.append(" ");
		}
		states = stateList.toString();
		return states;
	}
	
	/**
	 * 
	 * @return
	 */
	public String updateSyncEvents() {
		try {
			if(syncUnassignedOnStartup == null) {
				// no change
			} else {
				evalSettings.set(EvalSettings.SYNC_UNASSIGNED_GROUPS_ON_STARTUP, syncUnassignedOnStartup);
			} 
			
			if(syncOnStateChange == null) {
				// no change
			} else {
				evalSettings.set(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_STATE_CHANGE, syncOnStateChange);
			} 
			
			if(syncOnGroupSave == null) {
				// no change
			} else {
				evalSettings.set(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_GROUP_SAVE, syncOnGroupSave);
			} 
			
			if(syncOnGroupUpdate == null) {
				// no change
			} else {
				evalSettings.set(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_GROUP_UPDATE, syncOnGroupUpdate);
			} 
			
			if(this.syncServerId != null) {
				this.evalSettings.set(EvalSettings.SYNC_SERVER, this.syncServerId);
			}

			// send success message through targeted messages
			messages.addMessage(new TargettedMessage("administrate.sync.event.saved", null, TargettedMessage.SEVERITY_INFO));
			
			return RESULT_SUCCESS;
		} catch (Exception e) {
			logger.warn("error in updateSyncEvents() " + e);
		}
		
		// send error message
		messages.addMessage(new TargettedMessage("administrate.sync.event.failed", null, TargettedMessage.SEVERITY_ERROR));
		
		return RESULT_FAILURE;
	}
	
	public String updateSync() {
		logger.info("updateSync(" + this.triggerName + ") ");
		boolean success = false;
		Map<String,Map<String,String>> cronJobs = this.externalLogic.getCronJobs(JOB_GROUP_NAME, new String[]{EvalConstants.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST});
		if(triggerName == null || triggerName.trim().equals("")) {
			this.messages.addMessage(new TargettedMessage("administrate.sync.update.null", null, TargettedMessage.SEVERITY_ERROR));
		} else if(cronJobs == null || cronJobs.get(triggerName) == null) {
			this.messages.addMessage(new TargettedMessage("administrate.sync.update.failed", new Object[]{triggerName}, TargettedMessage.SEVERITY_ERROR));
		} else {
			Map<String,String> job = cronJobs.get(triggerName);
			if(job == null || job.get("job.name") == null || job.get("job.group") == null) {
				// error
				this.messages.addMessage(new TargettedMessage("administrate.sync.update.failure", new Object[]{job.get("trigger.cronExpression"), job.get(EvalConstants.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST)}, TargettedMessage.SEVERITY_ERROR));					
			} else {
				success = this.externalLogic.deleteCronJob(job.get("job.name"), job.get("job.group"));
				if(success) {
					success = this.scheduleCronJob();
				}
				
				if(success) {
					this.messages.addMessage(new TargettedMessage("administrate.sync.update.succeeded", new Object[]{this.cronExpression.trim(), this.getStateValues()}, TargettedMessage.SEVERITY_INFO));			
				} else {
					this.messages.addMessage(new TargettedMessage("administrate.sync.update.failure", new Object[]{job.get("trigger.cronExpression"), job.get(EvalConstants.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST)}, TargettedMessage.SEVERITY_ERROR));					
				}
			}
		}
		
		if(this.syncServerId != null) {
			this.evalSettings.set(EvalSettings.SYNC_SERVER, this.syncServerId);
		}
		
		return (success ? RESULT_SUCCESS : RESULT_FAILURE);
	}
	
	public String deleteSync() {
		logger.info("deleteSync(" + this.triggerName + ")");
		boolean success = false;
		Map<String,Map<String,String>> cronJobs = this.externalLogic.getCronJobs(JOB_GROUP_NAME, new String[]{EvalConstants.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST});
		if(triggerName == null || triggerName.trim().equals("")) {
			this.messages.addMessage(new TargettedMessage("administrate.sync.delete.null", null, TargettedMessage.SEVERITY_ERROR));
		} else if(cronJobs == null || cronJobs.get(triggerName) == null) {
			this.messages.addMessage(new TargettedMessage("administrate.sync.delete.failed", new Object[]{triggerName}, TargettedMessage.SEVERITY_ERROR));
		} else {
			Map<String,String> job = cronJobs.get(triggerName);
			if(job == null || job.get("job.name") == null || job.get("job.group") == null) {
				// error
				this.messages.addMessage(new TargettedMessage("administrate.sync.delete.failure", new Object[]{job.get("trigger.cronExpression"), job.get(EvalConstants.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST)}, TargettedMessage.SEVERITY_ERROR));					
			} else {
				success = this.externalLogic.deleteCronJob(job.get("job.name"), job.get("job.group"));
				if(success) {
					this.messages.addMessage(new TargettedMessage("administrate.sync.delete.succeeded", new Object[]{job.get("trigger.cronExpression").trim(), job.get(EvalConstants.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST).trim()}, TargettedMessage.SEVERITY_INFO));			
				} else {
					this.messages.addMessage(new TargettedMessage("administrate.sync.delete.failure", new Object[]{job.get("trigger.cronExpression"), job.get(EvalConstants.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST)}, TargettedMessage.SEVERITY_ERROR));					
				}
			}
		}
		
		return (success ? RESULT_SUCCESS : RESULT_FAILURE);
	}
	
	/**
	 * Schedule a sync of evals by state in the very near future.  The sync is scheduled using 
	 * a cron job with a time a specific number of minutes from the surrent time, where the delay 
	 * is determined by the constant DELAY_IN_MINUTES.  The list of eval states is determined by 
	 * values in the request, as in the scheduleSync method. The sync is done on the default 
	 * server, which may be updated in processing this request.  
	 *   
	 * @return
	 */
	public String quickSync() {
		logger.info("quickSync() ");
		boolean success = false;
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis() + DELAY_IN_MINUTES * MILLISECONDS_PER_MINUTE);
		StringBuilder buf = new StringBuilder();
		buf.append(cal.get(Calendar.SECOND));
		buf.append(SPACE);
		buf.append(cal.get(Calendar.MINUTE));
		buf.append(SPACE);
		buf.append(cal.get(Calendar.HOUR_OF_DAY));
		buf.append(SPACE);
		buf.append(cal.get(Calendar.DAY_OF_MONTH));
		buf.append(SPACE);
		buf.append(cal.get(Calendar.MONTH) + 1);
		buf.append(SPACE);
		buf.append('?');
		buf.append(SPACE);
		buf.append(cal.get(Calendar.YEAR));
		this.cronExpression = buf.toString();
		logger.info("quickSync() cronExpression == " + cronExpression);
		
		if(this.syncServerId != null) {
			this.evalSettings.set(EvalSettings.SYNC_SERVER, this.syncServerId);
		}		
		
		success = this.scheduleCronJob();
		if(success) {
			this.messages.addMessage(new TargettedMessage("administrate.sync.quick.success", new Object[]{Long.toString(DELAY_IN_MINUTES), this.getStateValues()}, TargettedMessage.SEVERITY_INFO));			
		} else {
			this.messages.addMessage(new TargettedMessage("administrate.sync.quick.failure", null, TargettedMessage.SEVERITY_ERROR));			
		}
		
		return (success ? RESULT_SUCCESS : RESULT_FAILURE );
	}
}
