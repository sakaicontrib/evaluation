/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.tool;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronExpression;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.scheduling.GroupMembershipSync;
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
	
	public static final long MILLISECONDS_PER_MINUTE = 60L * 1000L;
	public static final long DELAY_IN_MINUTES = 2L;

	private static final Log LOG = LogFactory.getLog(ProviderSyncBean.class);
	
	private static final String SPACE = " ";

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
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

    public String fullJobName;
	public String cronExpression;
	
	public Boolean partial;
	public Boolean inqueue;
	public Boolean active;
	public Boolean graceperiod;
	public Boolean closed;
	
	public Integer tab;
	
	public String syncServerId;
	
	public void init() {
		LOG.info("init()");
	}
	
	public String scheduleSync() {
		LOG.info("scheduleSync() ");
		
		
		boolean error = scheduleCronJob();
		
		if(this.syncServerId != null) {
			this.evalSettings.set(EvalSettings.SYNC_SERVER, this.syncServerId);
		}
		
		LOG.info("scheduleSync() error == " + error);
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
			Map<String,String> dataMap = new HashMap<>();
			String uniqueId = EvalUtils.makeUniqueIdentifier(99);
			dataMap.put(EvalConstants.CRON_SCHEDULER_TRIGGER_NAME, uniqueId);
			dataMap.put(EvalConstants.CRON_SCHEDULER_TRIGGER_GROUP, JOB_GROUP_NAME);
			dataMap.put(EvalConstants.CRON_SCHEDULER_JOB_NAME, uniqueId);
			dataMap.put(EvalConstants.CRON_SCHEDULER_JOB_GROUP, JOB_GROUP_NAME);
			
			dataMap.put(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION, this.cronExpression);
			
			dataMap.put(EvalConstants.CRON_SCHEDULER_SPRING_BEAN_NAME, GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_BEAN_NAME);
			dataMap.put(GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST, states);

			commonLogic.scheduleCronJob("org.sakaiproject.api.app.scheduler.JobBeanWrapper.GroupMembershipSync", dataMap);
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
		if(partial != null && partial) {
			stateList.append(EvalConstants.EVALUATION_STATE_PARTIAL);
			stateList.append(" ");
		}
		if(inqueue != null && inqueue) {
			stateList.append(EvalConstants.EVALUATION_STATE_INQUEUE);
			stateList.append(" ");
		}
		if(active != null && active) {
			stateList.append(EvalConstants.EVALUATION_STATE_ACTIVE);
			stateList.append(" ");
		}
		if(graceperiod != null && graceperiod) {
			stateList.append(EvalConstants.EVALUATION_STATE_GRACEPERIOD);
			stateList.append(" ");
		}
		if(closed != null && closed) {
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
			LOG.warn("error in updateSyncEvents() " + e);
		}
		
		// send error message
		messages.addMessage(new TargettedMessage("administrate.sync.event.failed", null, TargettedMessage.SEVERITY_ERROR));
		
		return RESULT_FAILURE;
	}
	
	public String updateSync() {
		LOG.info("updateSync(" + this.fullJobName + ") ");
		boolean success = false;
		Map<String,Map<String,String>> cronJobs = this.commonLogic.getCronJobs(JOB_GROUP_NAME);
		if(fullJobName == null || fullJobName.trim().equals("")) {
			this.messages.addMessage(new TargettedMessage("administrate.sync.update.null", null, TargettedMessage.SEVERITY_ERROR));
		} else if(cronJobs == null || cronJobs.get(fullJobName) == null) {
			this.messages.addMessage(new TargettedMessage("administrate.sync.update.failed", new Object[]{fullJobName}, TargettedMessage.SEVERITY_ERROR));
		} else {
			Map<String,String> job = cronJobs.get(fullJobName);
			if(job == null || job.get(EvalConstants.CRON_SCHEDULER_JOB_NAME) == null || job.get(EvalConstants.CRON_SCHEDULER_JOB_GROUP) == null) {
				// error
				this.messages.addMessage(new TargettedMessage("administrate.sync.update.failure", new Object[]{job.get(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION), job.get(GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST)}, TargettedMessage.SEVERITY_ERROR));					
			} else {
				success = this.commonLogic.deleteCronJob(job.get(EvalConstants.CRON_SCHEDULER_JOB_NAME), job.get(EvalConstants.CRON_SCHEDULER_JOB_GROUP));
				if(success) {
					success = this.scheduleCronJob();
				}
				
				if(success) {
					this.messages.addMessage(new TargettedMessage("administrate.sync.update.succeeded", new Object[]{this.cronExpression.trim(), this.getStateValues()}, TargettedMessage.SEVERITY_INFO));			
				} else {
					this.messages.addMessage(new TargettedMessage("administrate.sync.update.failure", new Object[]{job.get(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION), job.get(GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST)}, TargettedMessage.SEVERITY_ERROR));					
				}
			}
		}
		
		if(this.syncServerId != null) {
			this.evalSettings.set(EvalSettings.SYNC_SERVER, this.syncServerId);
		}
		
		return (success ? RESULT_SUCCESS : RESULT_FAILURE);
	}
	
	public String deleteSync() {
		LOG.info("deleteSync(" + this.fullJobName + ")");
		boolean success = false;
		Map<String,Map<String,String>> cronJobs = this.commonLogic.getCronJobs(JOB_GROUP_NAME);
		if(fullJobName == null || fullJobName.trim().equals("")) {
			this.messages.addMessage(new TargettedMessage("administrate.sync.delete.null", null, TargettedMessage.SEVERITY_ERROR));
		} else if(cronJobs == null || cronJobs.get(fullJobName) == null) {
			this.messages.addMessage(new TargettedMessage("administrate.sync.delete.failed", new Object[]{fullJobName}, TargettedMessage.SEVERITY_ERROR));
		} else {
			Map<String,String> job = cronJobs.get(fullJobName);
			if(job == null || job.get(EvalConstants.CRON_SCHEDULER_JOB_NAME) == null || job.get(EvalConstants.CRON_SCHEDULER_JOB_GROUP) == null) {
				// error
				this.messages.addMessage(new TargettedMessage("administrate.sync.delete.failure", new Object[]{job.get(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION), job.get(GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST)}, TargettedMessage.SEVERITY_ERROR));					
			} else {
				success = this.commonLogic.deleteCronJob(job.get(EvalConstants.CRON_SCHEDULER_JOB_NAME), job.get(EvalConstants.CRON_SCHEDULER_JOB_GROUP));
				if(success) {
					this.messages.addMessage(new TargettedMessage("administrate.sync.delete.succeeded", new Object[]{job.get(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION).trim(), job.get(GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST).trim()}, TargettedMessage.SEVERITY_INFO));			
				} else {
					this.messages.addMessage(new TargettedMessage("administrate.sync.delete.failure", new Object[]{job.get(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION), job.get(GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST)}, TargettedMessage.SEVERITY_ERROR));					
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
		LOG.info("quickSync() ");
		boolean success;
		
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
		LOG.info("quickSync() cronExpression == " + cronExpression);
		
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
