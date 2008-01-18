
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.evaluation.logic.impl.scheduling;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.utils.SettingsLogicUtils;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * Schedule this job to run once daily when there are active evaluations, in order to send 
 * email notification once daily to each student with one or more outstanding evaluations.
 * 
 * TODO - Suppose tool is not on My Workspace - how does link get constructed
 * 		- Refactor Job so it delegates to a service to do the deed, and unit test the service methods.
 * 		- use ServerConfigurationService through ExternalLogic
 * 		- implement logic/test/stubs/EvalExternalLogicStub getMyworkspaceUrl(String), getProperty(java.lang.String) unit tests
 * 				EvalEmailLogicImplTest public boolean overrideDefaults()
 * 		- PreLoad consolidated templates
 * 		- if changed System Settings Notification Settings: sync reminder interval and count until evaluation is locked (write a bean guard?)
 * 		- make sure there are no circular dependencies
 * 		- refactor common code in reminder logic and available logic
 * 		- safe email testing needs to apply to either mode
 * 		- throttle and logging of recipients
 *		- protect the public api's EvalEvaluationsLogic.getActiveEvaluationsByAvailableEmailSent() - an isAdmin() function
 * 		- Logging metrics of email queue processing, toAddresses
 * 		- check permission EvalExternalLogicImpl - public boolean isUserAdmin(String userId) - run under admin(?)
		- check for valid settings prior to calling Job
 *
 * @author rwellis
 *
 */
public class EvalConsolidatedNotificationJob implements Job{
	
	private EvalEmailsLogic evalEmailsLogic;
	public void setEvalEmailsLogic(EvalEmailsLogic evalEmailsLogic) {
		this.evalEmailsLogic = evalEmailsLogic;
	}
	
	private EvalEvaluationsLogic evalEvaluationsLogic;
	public void setEvalEvaluationsLogic(EvalEvaluationsLogic evalEvaluationsLogic) {
		this.evalEvaluationsLogic = evalEvaluationsLogic;
	}
	
	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private EvalSettings evalSettings;
	public void setEvalSettings(EvalSettings evalSettings) {
		this.evalSettings = evalSettings;
	}

	private static Log log = LogFactory.getLog(EvalConsolidatedNotificationJob.class);
	private int daysUntilReminder;
	private int reminderInterval; //unit is day
	
	public void init() {
		if(log.isDebugEnabled()) log.debug("init()");
	}
	
	/**
	 * execute is the main method of a Quartz Job.
	 */
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			//check settings, etc.
			if(checkSettings()) {
				if(evalEvaluationsLogic.countActiveEvaluations() == 0) {
					if(log.isInfoEnabled()) log.info("There are no active evaluations.");
					return;
				}
			}
			else {
				log.error("Settings are inconsistent with job execution.");
				return;
			}
			
			//see if today is a reminder day
			reminderInterval = ((Integer)evalSettings.get(EvalSettings.CONSOLIDATED_REMINDER_INTERVAL)).intValue();
			daysUntilReminder = ((Integer)evalSettings.get(EvalSettings.DAYS_UNTIL_REMINDER)).intValue();
			if(daysUntilReminder == 0) {
				evalEmailsLogic.sendEvalConsolidatedReminder();
				daysUntilReminder = reminderInterval;//TODO need to keep in sync in System Settings UI as well
				updateConfig(EvalSettings.DAYS_UNTIL_REMINDER,daysUntilReminder);
			}
			else if(daysUntilReminder < 0) {
				//shouldn't happen
				log.error("Days until reminder is less than 0.");
				return;
			}
			else {
				daysUntilReminder = daysUntilReminder - 1;
				updateConfig(EvalSettings.DAYS_UNTIL_REMINDER,daysUntilReminder);
			}
			/* Note: available follows reminder so available's change in
			 * EvalEvaluation.avalableEmailSent won't trigger a reminder
			 */
			evalEmailsLogic.sendEvalConsolidatedAvailable();
		}
		catch(Exception e) {
			log.error("Error executing consolidated email job." + e);
			throw new JobExecutionException(e);
		}
	}
	
	/**
	 * Check that consolidated notification is set and there is work to do.
	 * 
	 * @return true if execution should proceed, false otherwise
	 */
	private boolean checkSettings() {
		boolean check = true;
		Boolean consolidatedNotification = (Boolean)evalSettings.get(EvalSettings.CONSOLIDATE_NOTIFICATION);
		if(consolidatedNotification == null) {
			log.error("EvalConsolidatedNotificationJob was called, but EvalSettings.CONSOLIDATE_NOTIFICATION is false or null.");
			check = false;
		}
		if(!consolidatedNotification.booleanValue()) {
			check = false;
		}
		Map<String,String> map = externalLogic.getNotificationSettings();
		if(map.get(EvalConstants.EMAIL_DELIVERY_OPTION_PROPERTY).equals(EvalConstants.EMAIL_DELIVERY_OPTION_NONE)
			&& map.get(EvalConstants.EMAIL_LOG_RECIPIENTS_PROPERTY).equalsIgnoreCase("false")) {
			log.warn("EvalConsolidatedNotificationJob was called, but EvalSettings are EMAIL_DELIVERY_OPTION_NONE and logging of email recipients false.");
			check = false;
		}
		return check;
	}
	
    private void updateConfig(String key, int value) {
        updateConfig(key, Integer.toString(value));
    }
    private void updateConfig(String key, String value) {
    	evalSettings.set(SettingsLogicUtils.getName(key),  value);
    }
	public void destroy() {
		
	}
}
