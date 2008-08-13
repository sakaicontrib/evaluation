
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

package org.sakaiproject.evaluation.logic.scheduling;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.utils.SettingsLogicUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.util.ResourceLoader;

/**
 * Schedule this job to run once daily when there are active evaluations, in order to send 
 * email notification once daily to each student with one or more outstanding evaluations.
 * 
 * TODO - Suppose tool is not on My Workspace - how does link get constructed
 * 		- Refactor Job so it delegates to a service to do the deed, and unit test the service methods.
 * 		- use ServerConfigurationService through ExternalLogic
 * 		- implement logic/test/stubs/EvalExternalLogicStub getMyworkspaceUrl(String), getProperty(java.lang.String) unit tests
 * 				EvalEmailLogicImplTest public boolean overrideDefaults()
 * 		- make sure there are no circular dependencies
 * 		- refactor common code in reminder logic and available logic
 *		- protect the public api's EvalEvaluationsLogic.getActiveEvaluationsByAvailableEmailSent() - an isAdmin() function
 * 		- check for valid settings prior to calling Job
 * 		- check permission EvalExternalLogicImpl - public boolean isUserAdmin(String userId) - run under admin(?)
 *
 * @author rwellis
 *
 */
public class EvalSingleEmailImpl implements Job{
	
	private EvalEmailsLogic evalEmailsLogic;
	public void setEvalEmailsLogic(EvalEmailsLogic evalEmailsLogic) {
		this.evalEmailsLogic = evalEmailsLogic;
	}
	
	private EvalSettings evalSettings;
	public void setEvalSettings(EvalSettings evalSettings) {
		this.evalSettings = evalSettings;
	}
	
	private EvalEvaluationService evaluationService;
	public void setEvaluationService(EvalEvaluationService evaluationService) {
		this.evaluationService = evaluationService;
	}
	
	private static Log log = LogFactory.getLog(EvalSingleEmailImpl.class);
	private static final Log metric = LogFactory.getLog("metrics." + EvalSingleEmailImpl.class.getName());
	
	private int reminderInterval; //unit is a day
	private Boolean logEmailRecipients; //optional
	
	public void init() {
		if(log.isDebugEnabled()) log.debug("init()");
	}
	
	/**
	 * execute is the main method of a Quartz Job.
	 */
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			if (metric.isInfoEnabled())
				metric
						.info("Metric EvalSingleEmailImpl.execute() was called by the Job Scheduler.");
			if (checkSettings()) {
				if (!evaluationService
						.isEvaluationWithState(EvalConstants.EVALUATION_STATE_ACTIVE)) {
					if (metric.isInfoEnabled())
						metric
								.info("Metric EvalSingleEmailImpl.execute() found no active evaluations.");
					return;
				}
			} else {
				log.error("Settings are inconsistent with job execution.");
				return;
			}
			Locale locale = new ResourceLoader().getLocale();
			logEmailRecipients = (Boolean) evalSettings
					.get(EvalSettings.LOG_EMAIL_RECIPIENTS);
			String[] recipients = new String[] {};
			long start, end;
			float seconds;
			// next reminder date
			Date reminderDate = (Date) evalSettings.get(EvalSettings.NEXT_REMINDER_DATE);
			reminderInterval = ((Integer) evalSettings
					.get(EvalSettings.REMINDER_INTERVAL_DAYS)).intValue();
			long day = 1000 * 60 * 60 * 24;
			// check if reminders are to be sent
			if(reminderInterval > 0) {
				// see if time is equal to or after reminder date
				long rdate = reminderDate.getTime();
				long tdate = System.currentTimeMillis();
				if (tdate >= rdate) {
					// today is a reminder day
					start = System.currentTimeMillis();
					recipients = evalEmailsLogic.sendEvalReminderSingleEmail();
					end = System.currentTimeMillis();
					seconds = (end - start) / 1000;
					if (metric.isInfoEnabled())
						metric
								.info("Metric EvalEmailsLogic.sendEvalReminderSingleEmail() took "
										+ seconds
										+ " seconds to send email to "
										+ recipients.length + " addresses.");
					// log email recipients
					if (logEmailRecipients.booleanValue()) {
						logEmailRecipients(recipients);
					}
					// set next reminder date
					long nextReminder = tdate + (day * reminderInterval);
					updateConfig(EvalSettings.NEXT_REMINDER_DATE, new Date(nextReminder));
				}
			}
			recipients = new String[] {};
			start = System.currentTimeMillis();
			/*
			 * Note: available follows reminder so available's setting
			 * EvalEvaluation.avalableEmailSent won't trigger a reminder
			 */
			recipients = evalEmailsLogic.sendEvalAvailableSingleEmail();
			end = System.currentTimeMillis();
			seconds = (end - start) / 1000;
			if (metric.isInfoEnabled())
				metric
						.info("Metric EvalEmailsLogic.sendEvalAvailableSingleEmail() took "
								+ seconds
								+ " seconds to send email to "
								+ recipients.length + " addresses.");

			// log email recipients
			if (logEmailRecipients.booleanValue())
				logEmailRecipients(recipients);
		} catch (Exception e) {
			log.error("Error executing consolidated email job." + e);
			throw new JobExecutionException(e);
		}
		if (metric.isInfoEnabled())
			metric
					.info("Metric EvalSingleEmailImpl.execute() normal execution completed.");
	}
	
	/**
	 * Sort, format and log the email addresses returned
	 * 
	 * @param recipients the array of addresses
	 */
	private void logEmailRecipients(String[] recipients) {
		Arrays.sort(recipients);
		StringBuffer sb = new StringBuffer();
		String line = null;
		int size = recipients.length;
		int cnt = 0;
		for(int i = 0; i < size; i++) {
			if(cnt > 0)
				sb.append(",");
			sb.append(recipients[i]);
			cnt++;
			//TODO the line limit if any is certainly bigger than this
			if((i+1) % 10 == 0) {
				line = sb.toString();
				if (log.isWarnEnabled())
					log.warn("Metric: email sent to " + line);
				//write a line and empty the buffer
				sb.setLength(0);
				cnt = 0;
			}
		}
		// if anything hasn't been written out, do it now
		if(sb.length() > 0) {
			line = sb.toString();
		}
	}
	
	/**
	 * Check that consolidated notification is set and there is work to do.
	 * 
	 * @return true if execution should proceed, false otherwise
	 */
	private boolean checkSettings() {
		boolean check = true;
		Boolean singleEmail = (Boolean)evalSettings.get(EvalSettings.ENABLE_SINGLE_EMAIL);
		if(singleEmail == null) {
			log.error("EvalSingleEmailImpl.execute() was called but EvalSettings.ENABLE_SINGLE_EMAIL was null.");
			check = false;
		}
		if(!singleEmail.booleanValue()) {
			log.error("EvalSingleEmailImpl.execute() was called but EvalSettings.ENABLE_SINGLE_EMAIL was false.");
			check = false;
		}
		if(((String)evalSettings.get(EvalSettings.EMAIL_DELIVERY_OPTION)).equals(EvalConstants.EMAIL_DELIVERY_NONE)) {
			if(!((Boolean)evalSettings.get(EvalSettings.LOG_EMAIL_RECIPIENTS)).booleanValue()) {
				log.warn("EvalSingleEmailImpl.execute() was called but EvalSettings.EMAIL_DELIVERY_OPTION is EMAIL_DELIVERY_NONE and EvalSettings.LOG_EMAIL_RECIPIENTS is false. There is nothing to do.");
				check = false;
			}
		}
		return check;
	}
	
	private void updateConfig(String key, Date date) {
		updateConfig(key, SettingsLogicUtils.getStringFromDate(date));
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
