
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

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
import org.sakaiproject.taskstream.client.TSSResponseApi;
import org.sakaiproject.taskstream.client.TaskStatusStandardValues;

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
	
	private static final Log log = LogFactory.getLog(EvalSingleEmailImpl.class);
	private static final Log metric = LogFactory.getLog("metrics." + EvalSingleEmailImpl.class.getName());
	private static final String EMAIL_STREAM_TAG = "Email";
	private static final String FIRST_NOTIFICATION = "first email notification";
	private static final String REMINDER = "reminder notification";
	private static final String SECONDS = "seconds";
	private static final String COUNT = "count";
	private static final String START = "start";
	private static final String END = "end";

	public void init() {
		if(log.isDebugEnabled()) log.debug("init()");
	}
	
	/**
	 * execute is the main method of a Quartz Job.
	 */
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		String streamUrl = null, entryUrl = null, entryTag = null, payload = null;
		Map<String, String> metrics = new HashMap<String, String>();
		DateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
		try {
			if (log.isInfoEnabled()) log.info("EvalSingleEmailImpl.execute() was called by the JobScheduler.");
			// job start
			long startTime = System.currentTimeMillis();
			try {
				// CREATED
				streamUrl = evaluationService.newTaskStatusStream(EMAIL_STREAM_TAG);
				
				// RUNNING
				reportRunning(streamUrl);
			}
			catch (Exception tss) {
				log.error(this + ".execute - task status " + tss);
			}
			if(log.isDebugEnabled()) log.debug(this + ".execute: Open TaskStatus stream Url " + streamUrl);
			if (checkSettings()) {
				if (!evaluationService
						.isEvaluationWithState(EvalConstants.EVALUATION_STATE_ACTIVE)) {
					if (log.isInfoEnabled())
						log.info("EvalSingleEmailImpl.execute() found no active evaluations.");
					
					// FINISHED
					reportFinished(streamUrl, startTime,
							formatter, "found no active evaluations.");
					return;
				}
			} else {
				log.error("EvalSingleEmailImpl.execute() settings are inconsistent with job execution.");
				
				// FINISHED
				reportFinished(streamUrl, startTime,
						formatter, "settings are inconsistent with job execution.");
				return;
			}
			Locale locale = new ResourceLoader().getLocale();
			
			//an option
			Boolean logEmailRecipients = (Boolean) evalSettings
					.get(EvalSettings.LOG_EMAIL_RECIPIENTS);
			String[] recipients = new String[] {};
			long start = 0, end = 0;
			// next reminder date
			Date reminderDate = (Date) evalSettings.get(EvalSettings.NEXT_REMINDER_DATE);
			if (log.isInfoEnabled()) log.info("Next reminder date is " + reminderDate + ".");
			//reminder interval unit is a day
			int reminderInterval = ((Integer) evalSettings
					.get(EvalSettings.REMINDER_INTERVAL_DAYS)).intValue();
			long day = 1000 * 60 * 60 * 24;
			// check if reminders are to be sent
			if(reminderInterval > 0) {
				// see if time is equal to or after reminder date
				long rdate = reminderDate.getTime();
				long tdate = System.currentTimeMillis();
				if (tdate >= rdate) {
					
					// today is a reminder day
					metrics = runReminderStep(streamUrl, formatter);
					
					try {
						// REMINDERS
						reportReminders(streamUrl, metrics);
						
						// REMINDER USERS
						reportReminderUsers(streamUrl, metrics);
						
						metrics.clear();
					}
					catch (Exception tss) {
						log.error(this + ".execute - task status " + tss);
					}
					
					// log email recipients
					if (logEmailRecipients.booleanValue()) {
						logEmailRecipients(recipients, REMINDER);
					}
					// set next reminder date 1 minute after midnight
					long nextReminder = getNextReminder(reminderInterval, day);
					updateConfig(EvalSettings.NEXT_REMINDER_DATE, new Date(nextReminder));
				}
				else {
					if (log.isInfoEnabled()) log.info("EvalSingleEmailImpl.execute() - today is not a reminder day.");
				}
			}
			
			// send any announcements
			metrics = runAnnouncementStep(streamUrl, formatter);
			
			try {
				// ANNOUNCEMENTS
				reportAnnouncements(streamUrl, metrics);
				
				// ANNOUNCEMENT USERS
				reportAnnouncementUsers(streamUrl, metrics);
			}
			catch(Exception tss) {
				log.error(this + ".execute - taskstatus " + tss);
			}

			// log email recipients
			if (logEmailRecipients.booleanValue()) logEmailRecipients(recipients, FIRST_NOTIFICATION);
	
			// FINISHED
			reportFinished(streamUrl, startTime, formatter, payload);
			
			if (log.isInfoEnabled()) log.info("EvalSingleEmailImpl.execute() finished.");
		} catch (Exception e) {
			log.error("EvalSingleEmailImpl.execute() job exception. " + e);
		}
		finally {
			
			// TODO move // FINISHED here
		}
	}

	/**
	 * Set the values of execution metrics for steps in the job
	 * 
	 * @param metrics
	 * @param seconds
	 * @param count
	 * @param startTime
	 * @param endTime
	 */
	private void setMetrics(Map<String, String> metrics, float seconds,
			Integer count, String startTime, String endTime) {
		metrics.put(COUNT, count.toString());
		metrics.put(SECONDS, (new Float(seconds)).toString());
		metrics.put(START, startTime);
		metrics.put(END, endTime);
	}
	
	/**
	 * Send announcements for evaluations that have become active
	 * 
	 * @param streamUrl
	 * @param formatter
	 * @return
	 */
	private Map<String, String> runAnnouncementStep(String streamUrl, DateFormat formatter) {
		/*
		 * Note:first announcements follow reminders so setting 
		 * 'first announcement sent' won't trigger a reminder
		 */
		Map<String, String> metrics = new HashMap<String, String>();
		Integer count = new Integer(0);
		long startTime = System.currentTimeMillis();
		Date d = new Date(startTime);
		String start = formatter.format(d);
		if(streamUrl == null) log.error(this + ".runAnnouncementStep() - TaskStatusServer streamUrl is null.");
		String[] recipients = evalEmailsLogic.sendEvalAvailableSingleEmail(streamUrl);
		count = new Integer(recipients.length);
		long endTime = System.currentTimeMillis();
		d = new Date(endTime);
		String end = formatter.format(d);
		long diff = endTime - startTime;
		float seconds = (float)diff/1000;
		setMetrics(metrics, seconds, count, start, end);
		return metrics;
	}
	
	/**
	 * Send reminders to take active evaluations previously announced
	 * 
	 * @param streamUrl
	 * @param formatter
	 * @return
	 */
	private Map<String, String> runReminderStep(String streamUrl, DateFormat formatter) {
		if (log.isInfoEnabled()) log.info("EvalSingleEmailImpl.execute() - today is a reminder day.");
		Map<String, String> metrics = new HashMap<String, String>();
		Integer count = new Integer(0);
		long startTime = System.currentTimeMillis();
		Date d = new Date(startTime);
		String start = formatter.format(d);
		if(streamUrl == null) log.error(this + ".runReminderStep() - TaskStatusServer streamUrl is null.");
		String[] recipients = evalEmailsLogic.sendEvalReminderSingleEmail(streamUrl);
		count = new Integer(recipients.length);
		long endTime = System.currentTimeMillis();
		d = new Date(endTime);
		String end = formatter.format(d);
		long diff = endTime - startTime;
		float seconds = (float)diff/1000;
		setMetrics(metrics, seconds, count, start, end);
		return metrics;
	}

	/**
	 * Mark the start of the job
	 * 
	 * @param streamUrl
	 */
	private void reportRunning(String streamUrl) {
		if(streamUrl != null) {
			try {
				String entryUrl = evaluationService.newTaskStatusEntry(streamUrl,
						URLEncoder.encode("no value", "UTF-8"), TaskStatusStandardValues.RUNNING.toString(), URLEncoder.encode("no value", "UTF-8"));
			}
			catch(Exception e) {
				log.error(this + ".reportRunning() " + e);
			}
		}
		else {
			log.error(this + ".reportRunning() - TaskStatusServer returned a null streamUrl.");
		}
	}

	/**
	 * Mark the end of the job noting its duration
	 * 
	 * @param streamUrl the URL of task status server
	 * @param startTime the time this job started
	 */
	private void reportFinished(String streamUrl, long startTime,
			DateFormat formatter, String payload) {
		if(streamUrl != null) {
			if(formatter != null) {
				Date d = new Date(startTime);
				String start = formatter.format(d);
				long endTime = System.currentTimeMillis();
				d = new Date(endTime);
				String end = formatter.format(d);
				long diff = endTime - startTime;
				float seconds = (float)diff/1000;
				String duration = (new Float(seconds)).toString();
				if(payload == null) {
					payload = "The email job took " + duration + " seconds to run. It kicked off at " + start + " and ended at " + end + ".";
				}
				String entryTag = "summary";
				try {
					String entryUrl = evaluationService.newTaskStatusEntry(streamUrl, entryTag, 
							TaskStatusStandardValues.FINISHED.toString(),  URLEncoder.encode(payload, "UTF-8"));
				}
				catch(Exception e) {
					log.error(this + ".reportFinished() " + e);
				}
			}
			else {
				log.error(this + ".reportFinished() - arguments null.");
			}
		}
		else {
			log.error(this + ".reportFinished() - TaskStatusServer returned a null streamUrl.");
		}
	}

	/**
	 * Report the number of users that received announcements 
	 * 
	 * @param streamUrl
	 */
	private void reportAnnouncementUsers(String streamUrl, Map<String, String> metrics) {
		if(streamUrl != null && metrics != null) {
			String payload = metrics.get(COUNT);
			String entryTag = "announcementUsers";
			try {
				String entryUrl = evaluationService.newTaskStatusEntry(streamUrl, 
						entryTag, TaskStatusStandardValues.RUNNING.toString(), URLEncoder.encode(payload, "UTF-8"));
			}
			catch(Exception e) {
				log.error(this + ".reportAnnouncementUsers() " + e);
			}
		}
		else {
			log.error(this + ".reportAnnouncementUsers() - argument(s) null.");
		}
	}

	/**
	 * Report the duration of sending announcements 
	 * 
	 * @param streamUrl
	 */
	private void reportAnnouncements(String streamUrl, Map<String, String> metrics) {
		if(streamUrl != null && metrics != null) {
			String payload = metrics.get(SECONDS) + " seconds from " + metrics.get(START) + " to " + metrics.get(END) + ".";
			String entryTag = "announcements";
			try {
				String entryUrl = evaluationService.newTaskStatusEntry(streamUrl, 
						"announcements", TaskStatusStandardValues.RUNNING.toString(), URLEncoder.encode(payload, "UTF-8"));
			}
			catch(Exception e) {
				log.error(this + ".reportAnnouncements() " + e);
			}
			if (metric.isInfoEnabled()) metric.info("metric EvalSingleEmailImpl.execute() It took "
							+ metrics.get(SECONDS) + " seconds to send " + FIRST_NOTIFICATION + "s to "
							+ metrics.get(COUNT) + " addresses.");
		}
		else {
			log.error(this + ".reportAnnouncements() - argument(s) null.");
		}
	}

	/**
	 * Report the number of users that received reminders 
	 * 
	 * @param streamUrl
	 */
	private void reportReminderUsers(String streamUrl, Map<String, String> metrics) {
		if(streamUrl != null && metrics != null) {
			String payload = metrics.get(COUNT); 
			String entryTag = "reminderUsers";
			try {
				String entryUrl = evaluationService.newTaskStatusEntry(streamUrl, 
						entryTag, TaskStatusStandardValues.RUNNING.toString(), URLEncoder.encode(payload, "UTF-8"));
			}
			catch(Exception e) {
				log.error(this + ".reportReminderUsers() " + e);
			}
		}
		else {
			log.error(this + ".reportReminderUsers() - argument(s) null.");
		}
	}

	/**
	 * Report the duration of sending reminders
	 * 
	 * @param streamUrl
	 */
	private void reportReminders(String streamUrl, Map<String, String> metrics) {
		if(streamUrl != null && metrics != null) {
			String payload = metrics.get(SECONDS) + " seconds from " + metrics.get(START) + " to " + metrics.get(END) + ".";
			String entryTag = "reminders";
			try {
				String entryUrl = evaluationService.newTaskStatusEntry(streamUrl, 
						entryTag, TaskStatusStandardValues.RUNNING.toString(), URLEncoder.encode(payload, "UTF-8"));
			}
			catch(Exception e) {
				log.error(this + ".reportReminders() " + e);
			}
			if (metric.isInfoEnabled()) metric.info("metric EvalSingleEmailImpl.execute() It took " + metrics.get(SECONDS)
							+ " seconds to send " + REMINDER + "s to "
							+ metrics.get(COUNT)  + " addresses.");
		}
		else {
			log.error(this + ".reportReminders() - argument(s) null.");
		}
	}
	
	/**
	 * Figure the next reminder date
	 * 
	 * @param reminderInterval
	 * @param day
	 * @return
	 */
	private long getNextReminder(int reminderInterval, long day) {
		long tdate;
		Calendar calendar = new GregorianCalendar();
		calendar.setTime( new Date() );
		calendar.set(Calendar.AM_PM, Calendar.AM);
		calendar.set(Calendar.MINUTE, 1);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.HOUR, 0);
		Date nextReminderDate = calendar.getTime();
		tdate = nextReminderDate.getTime();
		long nextReminder = tdate + (day * reminderInterval);
		return nextReminder;
	}
	
	/**
	 * Sort, format and log the email addresses returned
	 * 
	 * @param recipients the array of addresses
	 */
	private void logEmailRecipients(String[] recipients, String notification) {
		Arrays.sort(recipients);
		StringBuffer sb = new StringBuffer();
		String line = null;
		int size = recipients.length;
		int cnt = 0;
		//limit the line length written to the log
		for(int i = 0; i < size; i++) {
			if(cnt > 0)
				sb.append(",");
			sb.append(recipients[i]);
			cnt++;
			if((i+1) % 25 == 0) {
				line = sb.toString();
				if (metric.isInfoEnabled())
					metric.info("metric EvalSingleEmailImpl.logEmailRecipients() sent " + notification + " to "+ line + ".");
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
	 * Check that single email notification is set and there is work to do.
	 * 
	 * @return true if execution should proceed, false otherwise
	 */
	private boolean checkSettings() {
		boolean check = true;
		Boolean singleEmail = (Boolean)evalSettings.get(EvalSettings.ENABLE_SINGLE_EMAIL);
		if(singleEmail == null) {
			log.error("EvalSingleEmail was called, but EvalSettings.ENABLE_SINGLE_EMAIL was null.");
			check = false;
		}
		if(!singleEmail.booleanValue()) {
			log.error("EvalSingleEmail was called, but EvalSettings.ENABLE_SINGLE_EMAIL was false.");
			check = false;
		}
		if(((String)evalSettings.get(EvalSettings.EMAIL_DELIVERY_OPTION)).equals(EvalConstants.EMAIL_DELIVERY_NONE)) {
			if(!((Boolean)evalSettings.get(EvalSettings.LOG_EMAIL_RECIPIENTS)).booleanValue()) {
				log.warn("EvalSingleEmail was called, but EvalSettings.EMAIL_DELIVERY_OPTION is EMAIL_DELIVERY_NONE " +
						"and EvalSettings.LOG_EMAIL_RECIPIENTS is false: EvalSingleEmail has no work to do.");
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
