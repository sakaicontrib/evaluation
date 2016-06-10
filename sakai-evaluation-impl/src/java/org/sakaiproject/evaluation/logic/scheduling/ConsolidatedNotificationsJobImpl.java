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
package org.sakaiproject.evaluation.logic.scheduling;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.evaluation.jobmonitor.JobStatusReporter;
import org.sakaiproject.evaluation.jobmonitor.LoggingJobStatusReporter;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalLockManager;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;

/**
 * ConsolidatedNotificationsJobImpl is the default mechanism for sending consolidated 
 * notifications about evaluations. It is invoked by a chron job if enabled and scheduled 
 * through the admin's "Control Email Settings" page in sakai.
 */
public class ConsolidatedNotificationsJobImpl implements ConsolidatedNotificationsJob {
	
	public static final String LOCK_CONSOLIDATED_EMAIL_JOB = "LOCK_CONSOLIDATED_EMAIL_JOB";
	private static final long TWO_HOURS = 2L * 60L * 60L * 1000L;

	private static final Log LOG = LogFactory.getLog(ConsolidatedNotificationsJobImpl.class);
	
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
	
	protected EvalLockManager lockManager;
	public void setEvalLockManager(EvalLockManager lockManager) {
		this.lockManager = lockManager;
	}
	
    protected JobStatusReporter jobStatusReporter;
    public void setJobStatusReporter(JobStatusReporter jobStatusReporter) {
    	LOG.info("setJobStatusReporter() jobStatusReporter == " + jobStatusReporter);
    	this.jobStatusReporter = jobStatusReporter;
    }
    
    protected String jobStatusReporterName;
    public void setJobStatusReporterName(String jobStatusReporterName) {
    	LOG.info("setJobStatusReporterName() jobStatusReporterName == " + jobStatusReporterName);
    	this.jobStatusReporterName = jobStatusReporterName;
    }
	
	public void init() {
		LOG.info("init()");
		
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
		LOG.info("execute()");
		
		// this server must get lock to do this job
		String serverId = this.externalLogic.getServerId();
		Boolean gotLock = lockManager.obtainLock(LOCK_CONSOLIDATED_EMAIL_JOB, serverId, TWO_HOURS);
		if(gotLock != null && gotLock) {
			try {
				
				Date beginTime = new Date();
			
				String jobId = this.jobStatusReporter.reportStarted("Email");
				this.jobStatusReporter.reportProgress(jobId, "server-id", serverId);
				
				Boolean sendAvailableEmails = (Boolean) this.evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE);
				if(sendAvailableEmails == null) {
					sendAvailableEmails = true;
				}
				
				if(sendAvailableEmails) {
					String[] recipients = this.emailLogic.sendConsolidatedAvailableNotifications(jobStatusReporter, jobId);
					if(recipients == null) {
						LOG.debug("announcements sent: 0");
					} else {
						LOG.debug("announcements sent: " + recipients.length);
					}
				}
		
				int reminderInterval = ((Integer) evalSettings.get(EvalSettings.SINGLE_EMAIL_REMINDER_DAYS));
				// check if reminders are to be sent
				if(reminderInterval > 0) {
					Date nextReminder;
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
					if (LOG.isInfoEnabled()) {
						LOG.info("Next reminder date is " + nextReminder + ".");
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
							LOG.debug("reminders sent: 0");
						} else {
							LOG.debug("reminders sent: " + recipients.length);
						}
						Calendar cal = Calendar.getInstance();
						cal.setTimeInMillis(tdate + reminderInterval * one_day);
						Integer startTime = (Integer) this.evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_TIME);
						Integer startMinute = (Integer) this.evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_MINUTES);
		
						if(startTime != null) {
							cal.set(Calendar.HOUR_OF_DAY, startTime);
							cal.set(Calendar.MINUTE, startMinute);
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
			} catch(Exception e) {
				LOG.warn("Error processing email job",e);
			} finally {
				// this server must release lock
				lockManager.releaseLock(LOCK_CONSOLIDATED_EMAIL_JOB, serverId);
			}
			
		}
	}

}
