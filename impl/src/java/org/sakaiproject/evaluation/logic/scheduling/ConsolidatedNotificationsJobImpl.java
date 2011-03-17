/**
 * 
 */
package org.sakaiproject.evaluation.logic.scheduling;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.evaluation.jobmonitor.JobStatusReporter;
import org.sakaiproject.evaluation.jobmonitor.LoggingJobStatusReporter;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;

/**
 * 
 */
public class ConsolidatedNotificationsJobImpl implements ConsolidatedNotificationsJob {
	
	Log log = LogFactory.getLog(ConsolidatedNotificationsJobImpl.class);
	
	protected EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	protected EvalEmailsLogic emailLogic;
	public void setEmailLogic(EvalEmailsLogic emailLogic) {
		this.emailLogic = emailLogic;
	}
	
	protected EvalSettings evalSettings;
	public void setEvalSettings(EvalSettings evalSettings) {
		this.evalSettings = evalSettings;
	}
	
    protected JobStatusReporter jobStatusReporter;
    public void setJobStatusReporter(JobStatusReporter jobStatusReporter) {
    	log.info("setJobStatusReporter() jobStatusReporter == " + jobStatusReporter);
    	this.jobStatusReporter = jobStatusReporter;
    }
    
    protected String jobStatusReporterName;
    public void setJobStatusReporterName(String jobStatusReporterName) {
    	log.info("setJobStatusReporterName() jobStatusReporterName == " + jobStatusReporterName);
    	this.jobStatusReporterName = jobStatusReporterName;
    }
	
	public void init() {
		log.info("init()");
		
        if(jobStatusReporter == null) {
        	if(jobStatusReporterName != null) {
        		this.jobStatusReporter = this.externalLogic.getBean(JobStatusReporter.class);
        	}
        }
        if(jobStatusReporter == null) {
        	jobStatusReporter = new LoggingJobStatusReporter();

        }
	}

	/*
	 * (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		log.info("execute()");
		
		Date beginTime = new Date();
	
		String jobId = this.jobStatusReporter.reportStarted("Email");
		
		Boolean sendAvailableEmails = (Boolean) this.evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE);
		if(sendAvailableEmails == null) {
			sendAvailableEmails = new Boolean(true);
		}
		
		if(sendAvailableEmails.booleanValue()) {
			String[] recipients = this.emailLogic.sendConsolidatedAvailableNotifications(jobStatusReporter, jobId);
			if(recipients == null) {
				log.debug("announcements sent: 0");
			} else {
				log.debug("announcements sent: " + recipients.length);
			}
		}

		int reminderInterval = ((Integer) evalSettings.get(EvalSettings.SINGLE_EMAIL_REMINDER_DAYS)).intValue();
		// check if reminders are to be sent
		if(reminderInterval > 0) {
			Date nextReminder = null;
			String nextReminderStr = (String) evalSettings.get(EvalSettings.NEXT_REMINDER_DATE);
			if(nextReminderStr == null || nextReminderStr.trim().equals("")) {
				nextReminder = new Date();
			} else {
		        DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy"); //DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.FULL);
				try {
					nextReminder = df.parse( nextReminderStr );
				} catch (ParseException e) {
					// Use current date
					nextReminder = new Date();
				}
			}
			if (log.isInfoEnabled()) {
				log.info("Next reminder date is " + nextReminder + ".");
			}
			//reminder interval unit is a day
			long one_hour = 1000L * 60L * 60L;
			long one_day = one_hour * 24L;
			// see if time is equal to or after reminder date (+/- six hours)
			long rdate = nextReminder.getTime();
			long tdate = System.currentTimeMillis();
			if (tdate >= (rdate - 6L * one_hour)) {
				

				String[] recipients = this.emailLogic.sendConsolidatedReminderNotifications(jobStatusReporter, jobId);
				if(recipients == null) {
					log.debug("reminders sent: 0");
				} else {
					log.debug("reminders sent: " + recipients.length);
				}
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(tdate + reminderInterval * one_day);
				Integer startTime = (Integer) this.evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_TIME);
				Integer startMinute = (Integer) this.evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_MINUTES);

				if(startTime != null) {
					cal.set(Calendar.HOUR_OF_DAY, startTime.intValue());
					cal.set(Calendar.MINUTE, startMinute.intValue());
					cal.set(Calendar.SECOND, 0);
				}
				this.evalSettings.set(EvalSettings.NEXT_REMINDER_DATE, cal.getTime());
				
				Date endTime = new Date();
				
				//"FINISHED" "summary" The email job took <elapsed-time> seconds to run. It kicked off at <begin-time> and ended at <begin-time>.
				
				StringBuilder buf = new StringBuilder();
				DateFormat df = DateFormat.getTimeInstance();
				long seconds = endTime.getTime() - beginTime.getTime();
				long milliseconds = seconds % 1000;
				seconds = seconds / 1000;
				
				buf.append("The email job took ");
				buf.append(seconds);
				buf.append(".");
				if(milliseconds < 10) {
					buf.append("00");
				} else if (milliseconds < 100) {
					buf.append("0");
				}
				buf.append(milliseconds);
				buf.append(" seconds to run. It kicked off at ");
				buf.append(df.format(beginTime));
				buf.append(" and ended at ");
				buf.append(df.format(endTime));
				buf.append(".");
				
				jobStatusReporter.reportFinished(jobId, false, "finished", buf.toString());
			}
		}
		
	}

}
