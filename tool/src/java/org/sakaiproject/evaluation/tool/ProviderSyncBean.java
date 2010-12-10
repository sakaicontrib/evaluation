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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
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
	
	private Log logger = LogFactory.getLog(ProviderSyncBean.class);
	
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

	public String cronExpression;
	
	public Boolean partial;
	public Boolean inqueue;
	public Boolean active;
	public Boolean graceperiod;
	public Boolean closed;
	
	public String syncServerId;
	
	public void init() {
		logger.info("init()");
	}
	
	public String scheduleSync() {
		logger.info("scheduleSync() ");
		boolean error = false;
		if(cronExpression == null || cronExpression.trim().equals("")) { 
			messages.addMessage(new TargettedMessage("administrate.sync.cronExpression.null", null, TargettedMessage.SEVERITY_ERROR));
			error = true;
			
		} else if(! CronExpression.isValidExpression(cronExpression)) {
			// send an error message through targeted messages
			messages.addMessage(new TargettedMessage("administrate.sync.cronExpression.err", new Object[]{ cronExpression }, TargettedMessage.SEVERITY_ERROR));
			error = true;
		} 

		StringBuilder stateList = null;
		if(! error) {
			stateList = new StringBuilder();
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
			if(stateList.length() == 0) {
				messages.addMessage(new TargettedMessage("administrate.sync.stateList.null", null, TargettedMessage.SEVERITY_ERROR));
				error = true;
			}
		}

		if(! error){
			try {
				String uniqueId = EvalUtils.makeUniqueIdentifier(99);
				String triggerName = uniqueId ;
				String triggerGroup = "org.sakaiproject.evaluation.tool.ProviderSyncBean";
				String jobName = uniqueId;
				String jobGroup = "org.sakaiproject.evaluation.tool.ProviderSyncBean";
				CronTrigger trigger = new CronTrigger(triggerName,triggerGroup,jobName,jobGroup,cronExpression);
				logger.info("Created trigger: " + trigger.getCronExpression());
				
				//ComponentManager componentManager = (ComponentManager) this.externalLogic.getBean(ComponentManager.class);
				Object jobClass = ComponentManager.get("org.sakaiproject.api.app.scheduler.JobBeanWrapper.GroupMembershipSync");
				// create job
				JobDataMap jobDataMap = new JobDataMap();
				jobDataMap.put("StatusList", stateList.toString());
				jobDataMap.put(org.sakaiproject.api.app.scheduler.JobBeanWrapper.SPRING_BEAN_NAME, "org.sakaiproject.evaluation.logic.scheduling.GroupMembershipSync");
				JobDetail jobDetail = new JobDetail();
				jobDetail.setJobDataMap(jobDataMap);
				jobDetail.setJobClass(jobClass.getClass());
				
				jobDetail.setName(jobName);
				jobDetail.setGroup(jobGroup);
				
				externalLogic.scheduleCronJob(trigger, jobDetail);
				
				messages.addMessage(new TargettedMessage("administrate.sync.job.success", null, TargettedMessage.SEVERITY_INFO));
				
			} catch (ParseException e) {
				// send an error message through targeted messages
				messages.addMessage(new TargettedMessage("administrate.sync.cronExpression.err", new Object[]{ cronExpression }, TargettedMessage.SEVERITY_ERROR));
				error = true;
			}
		}
		
		if(this.syncServerId != null) {
			this.evalSettings.set(EvalSettings.SYNC_SERVER, this.syncServerId);
		}
		
		logger.info("scheduleSync() error == " + error);
		return (error ? "failure" : "success");
	}
	
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
			
			return "success";
		} catch (Exception e) {
			logger.warn("error in updateSyncEvents() " + e);
		}
		
		// send error message
		messages.addMessage(new TargettedMessage("administrate.sync.event.failed", null, TargettedMessage.SEVERITY_ERROR));
		
		return "failure";
	}
	
	public String updateSync() {
		logger.info("updateSync() ");
		return "success";
	}
	
}
