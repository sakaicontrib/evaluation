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
package org.sakaiproject.evaluation.tool.locators;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.sakaiproject.api.app.scheduler.JobBeanWrapper;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.scheduling.ConsolidatedNotificationsJob;

public class EmailSettingsWBL extends SettingsWBL {
	
	public static final String JOB_GROUP_NAME = "org.sakaiproject.evaluation.tool.EmailSettingsWBL";
	public static final String SPRING_BEAN_NAME = JobBeanWrapper.SPRING_BEAN_NAME;

	private static final Log LOG = LogFactory.getLog(EmailSettingsWBL.class);
	
    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }
	
	protected final Object LOCK = new Object();
	protected boolean updateNeeded = false;
	
    public void set(String beanname, Object toset) {
    	super.set(beanname, toset);
    	if(EvalSettings.SINGLE_EMAIL_REMINDER_DAYS.equals(beanname) || 
    			EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_TIME.equals(beanname) || 
    			EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_MINUTES.equals(beanname)) {
        	LOG.info("set(" + beanname + "," + toset + ") ");
    		synchronized(LOCK) {
    			updateNeeded = true;
    		}
    	} 
    }
    
    public void saveSettings() {
    	LOG.info("saveSettings() -- Saving email settings ");
    	synchronized (LOCK) {
    		if(this.updateNeeded) {
    			this.scheduleJob();
    			this.updateNeeded = false;
    		}
    	}

    }

	protected void scheduleJob() {
		LOG.info("scheduleJob() -- Scheduling email job ");
		Map<String,Map<String,String>> cronJobs = this.commonLogic.getCronJobs(JOB_GROUP_NAME);
		for(Map.Entry<String, Map<String,String>> cronJob : cronJobs.entrySet()) {
			Map<String,String> details = cronJob.getValue();
			String jobName = details.get(EvalConstants.CRON_SCHEDULER_JOB_NAME);
			try {
				this.commonLogic.deleteCronJob(jobName,JOB_GROUP_NAME);
			} catch (Exception e) {
				LOG.info("Unable to delete cron job with group='" + JOB_GROUP_NAME + "' and name='" + jobName + "'");
			}
		}
		
		// create job
		Map<String,String> dataMap = new HashMap<>();
		dataMap.put(EvalConstants.CRON_SCHEDULER_TRIGGER_NAME, JOB_GROUP_NAME);
		dataMap.put(EvalConstants.CRON_SCHEDULER_TRIGGER_GROUP, JOB_GROUP_NAME);
		dataMap.put(EvalConstants.CRON_SCHEDULER_JOB_NAME, JOB_GROUP_NAME);
		dataMap.put(EvalConstants.CRON_SCHEDULER_JOB_GROUP, JOB_GROUP_NAME);
		
		dataMap.put(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION, this.calculateCronExpression());
		
		dataMap.put(SPRING_BEAN_NAME, ConsolidatedNotificationsJob.BEAN_NAME);

		Job jobClass = (Job) ComponentManager.get("org.sakaiproject.api.app.scheduler.JobBeanWrapper.ConsolidatedNotificationsJob");

		this.commonLogic.scheduleCronJob(jobClass.getClass(), dataMap);
		
		Date nextReminderDate = this.calculateNextReminderDate();
		evalSettings.set(EvalSettings.NEXT_REMINDER_DATE, nextReminderDate);
		
	}

	/*
	 * Calculates appropriate value for next reminder date based on old value for 
	 * next reminder date, reminder interval and start time.  If reminder interval 
	 * is zero (or negative or null, which should not be possible), the current value 
	 * of next reminder date is used.  
	 * 
	 */
	protected Date calculateNextReminderDate() {
		Integer reminderInterval = ((Integer) evalSettings.get(EvalSettings.SINGLE_EMAIL_REMINDER_DAYS));
        String nextReminderStr = (String) evalSettings.get(EvalSettings.NEXT_REMINDER_DATE);
        
		Date nextReminder;
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
		Integer startTime = (Integer) this.evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_TIME);
		Integer startMinute = (Integer) this.evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_MINUTES);
		
		Calendar cal = Calendar.getInstance();
		
		if(nextReminder != null) {
			cal.setTime(nextReminder);
		}
		
		if(reminderInterval != null && reminderInterval  > 0) {
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, startMinute);
			cal.set(Calendar.HOUR_OF_DAY, startTime);
			
			Calendar now = Calendar.getInstance();
			if(cal.before(now)) {
				cal.add(Calendar.DAY_OF_MONTH, reminderInterval);
			}
			if(cal.before(now)) {
				cal.set(Calendar.YEAR, now.get(Calendar.YEAR));
				cal.set(Calendar.MONTH, now.get(Calendar.MONTH));
				cal.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH) + 1);
			}
			
		}
		return cal.getTime();
	}

	protected String calculateCronExpression() {
		
		Integer startTime = (Integer) this.evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_TIME);
		Integer startMinute = (Integer) this.evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_MINUTES);
		
		StringBuilder buf = new StringBuilder();
		buf.append("0 ");
		buf.append(startMinute.toString());
		buf.append(" ");
		buf.append(startTime.toString());
		buf.append(" ? * * *");
		LOG.debug("emailSettings cronExpression == " + buf.toString());

		return buf.toString();
	}

}
