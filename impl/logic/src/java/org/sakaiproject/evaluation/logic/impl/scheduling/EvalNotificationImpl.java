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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.externals.EvalNotificationLogic;
import org.sakaiproject.evaluation.logic.externals.JobSchedulerFacade;
import org.sakaiproject.evaluation.logic.impl.EvalAssignsLogicImpl;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * Take action based on job type, to change Evaluation state and process change
 * or send email notification.
 * @author rwellis
 *
 */
public class EvalNotificationImpl implements StatefulJob {
	
	private static Log log = LogFactory.getLog(EvalAssignsLogicImpl.class);
	
	//TODO See Josh's example of Spring injection into Quartz job with ScheduleManager in CM code
	
	//Spring injection
	private EvalEvaluationsLogic evalEvaluationsLogic;
	public void setEvalEvaluationsLogic(EvalEvaluationsLogic evalEvaluationsLogic) {
		this.evalEvaluationsLogic = evalEvaluationsLogic;
	}
	
	//Spring injection
	private EvalEmailsLogic emails;
	public void setEvalEmailsLogic(EvalEmailsLogic emails) {
		this.emails = emails;
	}
	
	//Spring injection
	private JobSchedulerFacade jobSchedulerFacade;
	public void setJobSchedulerFacade(JobSchedulerFacade jobSchedulerFacade) {
		this.jobSchedulerFacade = jobSchedulerFacade;
	}
	
	//Spring injection
	private EvalNotificationLogic evalNotificationLogic;
	public void setEvalNotificationLogic(EvalNotificationLogic evalNotificationLogic) {
		this.evalNotificationLogic = evalNotificationLogic;
	}
	
	/**
	 * In a Quartz job, execute is the main method. Dispatch to actions based on job type.
	 * @throws JobExecutionException 
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try
		{
			//Perform context check
			if(context == null) {
				if(log.isWarnEnabled())
					log.warn(this + ".execute(JobExecutionContext context) context is null.");
				throw new JobExecutionException("JobExecutionContext was null.");
			}
			
			//Job parameters
			Long evalId = new Long(context.getJobDetail().getJobDataMap().getLong(EvalConstants.JOB_DATA_MAP_EVALUATION_ID));
			String jobType = context.getJobDetail().getJobDataMap().getString(EvalConstants.JOB_DATA_MAP_JOB_TYPE);
			
			//Perform parameter checks
			if(evalId == null || jobType == null){
				if(log.isWarnEnabled())
					log.warn(this + ".execute(JobExecutionContext context) One or more required job parameters was null.");
				throw new JobExecutionException("One or more required job parameters was null.");
			}
			EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evalId);
			if(eval == null) {
				if(log.isWarnEnabled())
					log.warn(this + ".execute(JobExecutionContext context) Evaluation with id '" + 
							evalId.toString() + "' was null.");
				throw new JobExecutionException("Evaluation with id '" + 
						evalId.toString() + "' was null.");
			}
			
			//TODO refactor extract method below
			
			//What type of job is this?
			if(EvalConstants.QRTZ_EVALUATION_NOTIFY_CREATED.equals(jobType))
			{
				//A job to send notification that an Evaluation is in the queue (i.e., has been created.)
				boolean includeOwner = true;
				emails.sendEvalCreatedNotifications(evalId, includeOwner);
			}
			else if (EvalConstants.QRTZ_EVALUATION_CHANGE_TO_ACTIVE.equals(jobType))
			{
				//A job to cause a state transition to ACTIVE and call processEvaluationChange().
				eval.setState(EvalConstants.EVALUATION_STATE_ACTIVE);
				//TODO Persist evaluation change
				try {
					evalNotificationLogic.processEvaluationChange(evalId, this.getClass().getName());
				}
				catch(Exception e) {
					if(log.isWarnEnabled())
						log.warn(this + ".execute(JobExecutionContext context) Job exception calling EvalNotificationLogic method processEvaluationChange(" + 
								evalId.toString() + "," + this.getClass().getName() + "). " + e);
					throw new JobExecutionException("Job exception calling EvalNotificationLogic method processEvaluationChange(" + 
							evalId.toString() + "," + this.getClass().getName() + "). " + e);
				}
			}
			else if (EvalConstants.QRTZ_EVALUATION_NOTIFY_AVAILABLE.equals(jobType))
			{
				//A job to send notification that an Evaluation is available.
				boolean includeEvaluatees = true;
				emails.sendEvalAvailableNotifications(evalId, includeEvaluatees);
			}
			else if (EvalConstants.QRTZ_EVALUATION_NOTIFY_REMINDER.equals(jobType))
			{
				//A job to send reminders to non-responders that an Evaluation is available.
				/* if EvalEvaluation.getState() = Activeg et the list of non-responders
				Send Reminder to non-respondent students (non-respondent students needs to be determined at the time the job runs) */
				if(EvalConstants.EVALUATION_STATE_ACTIVE.equals(eval.getState())) {
					//TODO includeConstant?
					String includeConstant = "";
					emails.sendEvalReminderNotifications(evalId, includeConstant);
					//TODO emails.sendEvalEminderNotifications not yet implemented
				}
			}
			else if (EvalConstants.QRTZ_EVALUATION_CHANGE_TO_DUE.equals(jobType))
			{
				//A job to cause a state change to DUE and call processEvaluationChange().
				eval.setState(EvalConstants.EVALUATION_STATE_DUE);
				//TODO Persist evaluation change
				try {
					evalNotificationLogic.processEvaluationChange(evalId, this.getClass().getName());
				}
				catch(Exception e) {
					if(log.isWarnEnabled())
						log.warn(this + ".execute(JobExecutionContext context) Job exception calling EvalNotificationLogic method processEvaluationChange(" + 
								evalId.toString() + "," + this.getClass().getName() + "). " + e);
					throw new JobExecutionException("Job exception calling EvalNotificationLogic method processEvaluationChange(" + 
							evalId.toString() + "," + this.getClass().getName() + "). " + e);
				}
			}
			else if (EvalConstants.QRTZ_EVALUATION_CHANGE_TO_CLOSED.equals(jobType))
			{
				//A job to cause a state change to CLOSED and call processEvaluationChange().
				eval.setState(EvalConstants.EVALUATION_STATE_CLOSED);
				//TODO Persist evaluation change
				try {
					evalNotificationLogic.processEvaluationChange(evalId, this.getClass().getName());
				}
				catch(Exception e) {
					if(log.isWarnEnabled())
						log.warn(this + ".execute(JobExecutionContext context) Job exception calling EvalNotificationLogic method processEvaluationChange(" + 
								evalId.toString() + "," + this.getClass().getName() + "). " + e);
					throw new JobExecutionException("Job exception calling EvalNotificationLogic method processEvaluationChange(" + 
							evalId.toString() + "," + this.getClass().getName() + "). " + e);
				}
			}
			else if (EvalConstants.QRTZ_EVALUATION_CHANGE_TO_VIEWABLE.equals(jobType))
			{
				//A job to cause a state change to VIEWABLE and call processEvaluationChange().
				eval.setState(EvalConstants.EVALUATION_STATE_VIEWABLE);
				//TODO Persist evaluation change
				//TODO studentsDate, instructorsDate, and viewDate
				try {
					evalNotificationLogic.processEvaluationChange(evalId, this.getClass().getName());
				}
				catch(Exception e) {
					if(log.isWarnEnabled())
						log.warn(this + ".execute(JobExecutionContext context) Job exception calling EvalNotificationLogic method processEvaluationChange(" + 
								evalId.toString() + "," + this.getClass().getName() + "). " + e);
					throw new JobExecutionException("Job exception calling EvalNotificationLogic method processEvaluationChange(" + 
							evalId.toString() + "," + this.getClass().getName() + "). " + e);
				}
			}
			else if (EvalConstants.QRTZ_EVALUATION_NOTIFY_VIEWABLE.equals(jobType))
			{
				//A job to send notification that an Evaluation's results are viewable.
				boolean includeEvaluatees = true;
				boolean includeAdmins = true;
				emails.sendEvalResultsNotifications(evalId, includeEvaluatees, includeAdmins);
			}
			else if (EvalConstants.QRTZ_EVALUATION_CLEAR_SCHEDULER.equals(jobType))
			{
				/* A job to remove an Evaluation's jobs from the scheduler. 
				 * Triggers should be removed automatically by Quartz after 
				 * jobs have run.  This is clean up done after the Evaluation
				 * process has completed.
				 */
				
				//TODO refactor extract method (called for each job group type)!
				
				try {
					jobSchedulerFacade.getScheduler().deleteJob(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_ACTIVE);
				}
				catch(Exception e) {
					if(log.isWarnEnabled())
						log.warn(this + ".execute(JobExecutionContext context) Job exception calling SchedulerFacade.getScheduler().deleteJob("  + 
								evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
					throw new JobExecutionException("Job exception calling SchedulerFacade.getScheduler().deleteJob(" + 
							 EvalConstants.QRTZ_EVALUATION_CHANGE_TO_ACTIVE + "," + evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
				}
				try {
					jobSchedulerFacade.getScheduler().deleteJob(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_CLOSED);
				}
				catch(Exception e) {
					if(log.isWarnEnabled())
						log.warn(this + ".execute(JobExecutionContext context) Job exception calling SchedulerFacade.getScheduler().deleteJob("  + 
								evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
					throw new JobExecutionException("Job exception calling SchedulerFacade.getScheduler().deleteJob(" + 
							 EvalConstants.QRTZ_EVALUATION_CHANGE_TO_CLOSED + "," + evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
				}
				try {
					jobSchedulerFacade.getScheduler().deleteJob(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_DUE);
				}
				catch(Exception e) {
					if(log.isWarnEnabled())
						log.warn(this + ".execute(JobExecutionContext context) Job exception calling SchedulerFacade.getScheduler().deleteJob("  + 
								evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
					throw new JobExecutionException("Job exception calling SchedulerFacade.getScheduler().deleteJob(" + 
							 EvalConstants.QRTZ_EVALUATION_CHANGE_TO_DUE + "," + evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
				}
				try {
					jobSchedulerFacade.getScheduler().deleteJob(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_VIEWABLE);
				}
				catch(Exception e) {
					if(log.isWarnEnabled())
						log.warn(this + ".execute(JobExecutionContext context) Job exception calling SchedulerFacade.getScheduler().deleteJob("  + 
								evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
					throw new JobExecutionException("Job exception calling SchedulerFacade.getScheduler().deleteJob(" + 
							 EvalConstants.QRTZ_EVALUATION_CHANGE_TO_VIEWABLE + "," + evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
				}
				try {
					jobSchedulerFacade.getScheduler().deleteJob(evalId.toString(), EvalConstants.QRTZ_EVALUATION_NOTIFY_AVAILABLE);
				}
				catch(Exception e) {
					if(log.isWarnEnabled())
						log.warn(this + ".execute(JobExecutionContext context) Job exception calling SchedulerFacade.getScheduler().deleteJob("  + 
								evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
					throw new JobExecutionException("Job exception calling SchedulerFacade.getScheduler().deleteJob(" + 
							 EvalConstants.QRTZ_EVALUATION_NOTIFY_AVAILABLE + "," + evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
				}
				try {
					jobSchedulerFacade.getScheduler().deleteJob(evalId.toString(), EvalConstants.QRTZ_EVALUATION_NOTIFY_CREATED);
				}
				catch(Exception e) {
					if(log.isWarnEnabled())
						log.warn(this + ".execute(JobExecutionContext context) Job exception calling SchedulerFacade.getScheduler().deleteJob("  + 
								evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
					throw new JobExecutionException("Job exception calling SchedulerFacade.getScheduler().deleteJob(" + 
							 EvalConstants.QRTZ_EVALUATION_NOTIFY_CREATED + "," + evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
				}
				try {
					jobSchedulerFacade.getScheduler().deleteJob(evalId.toString(), EvalConstants.QRTZ_EVALUATION_NOTIFY_REMINDER);
				}
				catch(Exception e) {
					if(log.isWarnEnabled())
						log.warn(this + ".execute(JobExecutionContext context) Job exception calling SchedulerFacade.getScheduler().deleteJob("  + 
								evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
					throw new JobExecutionException("Job exception calling SchedulerFacade.getScheduler().deleteJob(" + 
							 EvalConstants.QRTZ_EVALUATION_NOTIFY_REMINDER + "," + evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
				}
				try {
					jobSchedulerFacade.getScheduler().deleteJob(evalId.toString(), EvalConstants.QRTZ_EVALUATION_NOTIFY_VIEWABLE);
				}
				catch(Exception e) {
					if(log.isWarnEnabled())
						log.warn(this + ".execute(JobExecutionContext context) Job exception calling SchedulerFacade.getScheduler().deleteJob("  + 
								evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
					throw new JobExecutionException("Job exception calling SchedulerFacade.getScheduler().deleteJob(" + 
							 EvalConstants.QRTZ_EVALUATION_NOTIFY_VIEWABLE + "," + evalId.toString() + ") for evaluation '" + eval.getTitle() + "' " + e);
				}
				
				//TODO EvalConstants.QRTZ_EVALUATION_CLEAR_SCHEDULER
			}
			else
			{
				if(log.isWarnEnabled())
					log.warn(this + ".execute(JobExecutionContext context) Unrecognized jobType.");
				throw new JobExecutionException("Unrecognized jobType.");
			}
		}
		catch(JobExecutionException e){
			if(log.isWarnEnabled())
				log.warn(this + ".execute(JobExecutionContext context) " + e);
			throw new JobExecutionException(e);
		}
	}
}
