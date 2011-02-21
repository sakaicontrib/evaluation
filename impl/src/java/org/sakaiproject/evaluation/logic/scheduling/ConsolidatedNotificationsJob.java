/**
 * 
 */
package org.sakaiproject.evaluation.logic.scheduling;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;

/**
 * 
 */
public class ConsolidatedNotificationsJob implements Job {
	
	Log log = LogFactory.getLog(ConsolidatedNotificationsJob.class);
	
	EvalEmailsLogic emailLogic;
	public void setEvalEmailsLogic(EvalEmailsLogic emailLogic) {
		this.emailLogic = emailLogic;
	}
	
	protected EvalSettings evalSettings;
	public void setEvalSettings(EvalSettings evalSettings) {
		this.evalSettings = evalSettings;
	}

	/*
	 * (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		log.debug("execute()");

		Boolean logRecipients = (Boolean) this.evalSettings.get(EvalSettings.LOG_EMAIL_RECIPIENTS);
		if(logRecipients == null) {
			logRecipients = new Boolean(false);
		}

		int reminderInterval = ((Integer) evalSettings.get(EvalSettings.SINGLE_EMAIL_REMINDER_DAYS)).intValue();
		// check if reminders are to be sent
		if(reminderInterval > 0) {
			Date reminderDate = (Date) evalSettings.get(EvalSettings.NEXT_REMINDER_DATE);
			if (log.isInfoEnabled()) {
				log.info("Next reminder date is " + reminderDate + ".");
			}
			//reminder interval unit is a day
			long one_hour = 1000L * 60L * 60L;
			long one_day = one_hour * 24L;
			// see if time is equal to or after reminder date (+/- six hours)
			long rdate = reminderDate.getTime();
			long tdate = System.currentTimeMillis();
			if (tdate >= (rdate - 6L * one_hour)) {
				

				String[] recipients = this.emailLogic.sendConsolidatedReminderNotifications();
				if(recipients == null) {
					log.debug("reminders sent: 0");
				} else {
					log.debug("reminders sent: " + recipients.length);
					if(logRecipients.booleanValue() && log.isInfoEnabled()) {
						this.logRecipients("reminders", recipients);
					}
				}
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(tdate + reminderInterval * one_day);
				Integer startTime = (Integer) this.evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_TIME);
				if(startTime != null) {
					cal.set(Calendar.HOUR_OF_DAY, startTime.intValue());
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
				}
				this.evalSettings.set(EvalSettings.NEXT_REMINDER_DATE, cal.getTime());
			}
		}
		String[] recipients = this.emailLogic.sendConsolidatedAvailableNotifications();
		if(recipients == null) {
			log.debug("announcements sent: 0");
		} else {
			log.debug("announcements sent: " + recipients.length);
			if(logRecipients.booleanValue() && log.isInfoEnabled()) {
				this.logRecipients("announcements", recipients);
			}
		}
		
	}

	/*
	 * 
	 */
	protected void logRecipients(String emailType, String[] recipients) {
		StringBuilder buf = new StringBuilder();
		buf.append(emailType);
		buf.append(" sent to ");
		buf.append(recipients.length);
		buf.append(" recipients: ");
		boolean first = true;
		for(String recipient : recipients) {
			if(! first) {
				buf.append(", ");
			} else {
				first = false;
			}
			buf.append(recipient);
		}
		log.info(buf.toString());
	}

}
