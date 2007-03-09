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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.externals.EvalNotificationLogic;
import org.sakaiproject.evaluation.logic.externals.JobSchedulerFacade;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.utils.EvalUtils;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;

/**
 * Logic and actions related to scheduling email notification sent
 * to those participating in an Evaluation and/or changing the state
 * of an Evaluation.
 * 
 * @author rwellis
 *
 */
public class EvalNotificationLogicImpl implements EvalNotificationLogic {
	private static Log log = LogFactory.getLog(EvalNotificationLogicImpl.class);
	
	EvalEvaluation eval = null;
	EvalNotificationImpl notificationJob = new EvalNotificationImpl();;
	
	//Spring injection
	private EvalEvaluationsLogic evalEvaluationsLogic;
	public void setEvalEvaluationsLogic(EvalEvaluationsLogic evalEvaluationsLogic) {
		this.evalEvaluationsLogic = evalEvaluationsLogic;
	}
	
	//Spring injection
	private JobSchedulerFacade jobSchedulerFacade;
	public void setJobSchedulerFacade(JobSchedulerFacade jobSchedulerFacade) {
		this.jobSchedulerFacade = jobSchedulerFacade;
	}

	public void init() throws SchedulerException {
		log.debug("init");
	}
	
	/**
	 * No argument constructor.
	 *
	 */
	public EvalNotificationLogicImpl() {
		
	}
	
	/**
	 * For a new Evaluation, take appropriate action with respect to scheduling jobs that make Evaluation 
	 * state change and send email notification. Thus begins the sequence of jobs during the life cycle of 
	 * an Evaluation.
	 * @param evalId The Evaluation identifier.
	 * @throws Exception
	 */
	public void processNewEvaluation(Long evalId) throws Exception {
		
		if(evalId == null)
			throw new NullPointerException("Notification of a new evaluation failed, because the evaluation id was null.");
		
		//Perform checks
		eval = evalEvaluationsLogic.getEvaluationById(evalId);
		if(eval == null)
		{
			throw new NullPointerException("Notification of a new evaluation failed, because the evaluation was null.");
		}
		String state = EvalUtils.getEvaluationState(eval);
		if(state == null)
		{
			throw new NullPointerException("Notification of a new evaluation failed, because the evaluation state was null.");
		}
		if (!EvalConstants.EVALUATION_STATE_INQUEUE.equals(state))
		{
			throw new Exception("Notification of a new evaluation failed, because the evaluation state was unknown.");
		}
		
		//Set up for jobs
		JobDetail jobDetailCreated = null, jobDetailActive = null;
		SimpleTrigger triggerCreated = null, triggerActive = null;
		Date startTimeCreated = null, startTimeActive = null;
		
		//Schedule a job to send notification on Evaluation creation ten minutes from now (presumably only useful with an Evaluation hierarchy.)
		jobDetailCreated = new JobDetail(evalId.toString(), EvalConstants.QRTZ_EVALUATION_NOTIFY_CREATED, notificationJob.getClass());
		jobDetailCreated.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_EVALUATION_ID,evalId);
		jobDetailCreated.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_JOB_TYPE, EvalConstants.QRTZ_EVALUATION_NOTIFY_CREATED);
		startTimeCreated = new Date();
		startTimeCreated.setTime(startTimeCreated.getTime() + 600000);
		triggerCreated = new SimpleTrigger(evalId.toString(), EvalConstants.QRTZ_EVALUATION_NOTIFY_CREATED, startTimeCreated);
		triggerCreated.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
		scheduleStatefulJob(jobDetailCreated, triggerCreated);
		
		//Q makes A - Schedule a job to cause a state transition to ACTIVE on the start date.
		jobDetailActive = new JobDetail(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_ACTIVE, notificationJob.getClass());
		jobDetailActive.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_EVALUATION_ID,evalId);
		jobDetailActive.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_JOB_TYPE, EvalConstants.QRTZ_EVALUATION_CHANGE_TO_ACTIVE);
		startTimeActive = new Date();
		startTimeActive.setTime(eval.getStartDate().getTime());
		triggerActive = new SimpleTrigger(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_ACTIVE, startTimeActive);
		triggerActive.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
		scheduleStatefulJob(jobDetailActive, triggerActive);
	}

	/** 
	 * Process Evaluation changes cause by editing and saving an Evaluation OR by a Quartz job running.
	 * Editing and saving an Evaluation might cause a date to change, affecting when a scheduled job
	 * should run. A job running might cause a state change in an Evaluation, requiring actions
	 * appropriate to the state change. Dispatch based on the cause of the Evaluation change and
	 * take appropriate actions.
	 * @param evalId The Evaluation identifier.
	 * @param calledFrom The name of the calling class.
	 */
	public void processEvaluationChange(Long evalId, String calledFrom) throws Exception {
		log.debug(this + ".processEvaluationChange evalId: " + evalId);
		
		//Perform checks
		if(evalId == null)
			throw new NullPointerException("Notification of a new evaluation failed, because the evaluation id was null.");
		if(calledFrom == null)
			throw new NullPointerException("Notification of a new evaluation failed, because the name of the calling Java class was null.");
		EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evalId);
		String state = EvalUtils.getEvaluationState(eval);
		if(EvalConstants.EVALUATION_STATE_UNKNOWN.equals(state)) {
			
			if(log.isWarnEnabled())
				log.warn(this + ".processEvaluationChange(Long "+  evalId.toString() + ",String "  + calledFrom+ ") for " + eval.getTitle()  + ". Evaluation in UNKNOWN state");
			throw new Exception("Evaluation '"+eval.getTitle()+"' in UNKNOWN state");
		}
		
		//Set up for jobs
		JobDetail jobDetailDue = null, jobDetailClosed = null, jobDetailViewable = null, jobDetailReminder = null;
		SimpleTrigger triggerDue = null, triggerClosed = null, triggerViewable = null, triggerReminder = null;
		Date startTimeDue = null, startTimeClosed = null, startTimeViewable = null, startTimeReminder = null, endTimeReminder;
		
		//Check whether the Evaluation change arises from an Evaluation edit and save or a scheduled job running.
		if(calledFrom.contains("EvalNotification")) {
			
			//Here a scheduled job ran, causing a state change transition
			if(EvalConstants.EVALUATION_STATE_ACTIVE.equals(eval.getState())) {
				
				//TODO Schedule job that sends notification that Evaluation is available
				//EvalConstants.JOB_TYPE_NOTIFY_AVAILABLE
				
				
				// A makes D - Schedule a job to cause a state change to DUE, and run it on the Evaluation's due date.
				jobDetailDue = new JobDetail(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_DUE, notificationJob.getClass());
				jobDetailDue.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_EVALUATION_ID,evalId);
				jobDetailDue.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_JOB_TYPE, EvalConstants.QRTZ_EVALUATION_CHANGE_TO_DUE);
				startTimeDue = new Date();
				startTimeDue.setTime(eval.getDueDate().getTime());
				triggerDue = new SimpleTrigger(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_DUE, startTimeDue);
				triggerDue.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
				scheduleStatefulJob(jobDetailDue, triggerDue);

				// A makes R - Schedule a job to remind non-responders to respond, and run it in the morning every getReminderDays().
				jobDetailReminder = new JobDetail(evalId.toString(), EvalConstants.QRTZ_EVALUATION_NOTIFY_REMINDER, notificationJob.getClass());
				jobDetailReminder.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_EVALUATION_ID,evalId);
				jobDetailReminder.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_JOB_TYPE,EvalConstants.QRTZ_EVALUATION_NOTIFY_REMINDER);
				startTimeReminder = new Date();
				long longRepeatInterval =  1000 * 60 * 60 * 24 * eval.getReminderDays().longValue();
				startTimeReminder.setTime(eval.getStartDate().getTime() + longRepeatInterval);
				endTimeReminder = new Date(eval.getStopDate().getTime());
				triggerReminder = new SimpleTrigger(evalId.toString(), EvalConstants.QRTZ_EVALUATION_NOTIFY_REMINDER, startTimeReminder, endTimeReminder, SimpleTrigger.REPEAT_INDEFINITELY, longRepeatInterval);
				triggerReminder.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
				scheduleStatefulJob(jobDetailReminder, triggerReminder);
			}
			else if (EvalConstants.EVALUATION_STATE_DUE.equals(eval.getState())) {
				
				//D makes C - Schedule job to cause state change to CLOSED run on the Evaluation's stop date
				jobDetailClosed = new JobDetail(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_CLOSED, notificationJob.getClass());
				jobDetailClosed.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_EVALUATION_ID,evalId);
				jobDetailClosed.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_JOB_TYPE,EvalConstants.QRTZ_EVALUATION_CHANGE_TO_CLOSED);
				startTimeClosed = new Date();
				startTimeClosed.setTime(eval.getStopDate().getTime());
				triggerClosed = new SimpleTrigger(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_CLOSED, startTimeClosed);
				triggerClosed.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
				scheduleStatefulJob(jobDetailClosed, triggerClosed);
			}
			else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(eval.getState())) {
				
				//C makes V - Schedule a job to cause a state transition to VIEWABLE, run on the Evaluation's view date.
				jobDetailViewable = new JobDetail(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_VIEWABLE, notificationJob.getClass());
				jobDetailViewable.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_EVALUATION_ID,evalId);
				jobDetailViewable.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_JOB_TYPE,EvalConstants.QRTZ_EVALUATION_CHANGE_TO_VIEWABLE);
				startTimeViewable = new Date();
				startTimeViewable.setTime(eval.getViewDate().getTime());
				triggerViewable = new SimpleTrigger(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_VIEWABLE, startTimeViewable);
				triggerViewable.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
				scheduleStatefulJob(jobDetailViewable, triggerViewable);
			}
			else if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(eval.getState())) {
				
				//Schedule a job that sends notification that an Evaluation's results are viewable.
				jobDetailViewable = new JobDetail(evalId.toString(), EvalConstants.QRTZ_EVALUATION_NOTIFY_VIEWABLE, notificationJob.getClass());
				jobDetailViewable.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_EVALUATION_ID,evalId);
				jobDetailViewable.getJobDataMap().put(EvalConstants.JOB_DATA_MAP_JOB_TYPE, EvalConstants.QRTZ_EVALUATION_NOTIFY_VIEWABLE);
				//TODO schedule this to run now
				triggerViewable = new SimpleTrigger(evalId.toString(), EvalConstants.QRTZ_EVALUATION_NOTIFY_VIEWABLE, startTimeViewable);
				triggerViewable.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
				scheduleStatefulJob(jobDetailViewable, triggerViewable);
			}
			
		}
		else if (calledFrom.contains("EvalEvaluationLogic")) {
			
			//Here a Evaluation was edited and saved.
			
			if(EvalConstants.EVALUATION_STATE_INQUEUE.equals(eval.getState())) {
				
				//Check if the Active state job needs rescheduling.
				if(jobSchedulerFacade.getScheduler().getTrigger(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_ACTIVE).getStartTime().compareTo(eval.getStartDate()) != 0) {
					
					//TODO date is different
				}
			}
			else if(EvalConstants.EVALUATION_STATE_ACTIVE.equals(eval.getState())) {
				
				//Check if the Due state job needs rescheduling.
				if(jobSchedulerFacade.getScheduler().getTrigger(evalId.toString(), EvalConstants.QRTZ_EVALUATION_CHANGE_TO_DUE).getStartTime().compareTo(eval.getStartDate()) != 0) {
					
					//TODO date is different
				}
				
			}
			else if (EvalConstants.EVALUATION_STATE_DUE.equals(eval.getState())) {
				
				//Check if the Close state job needs reschuling.
				
				//TODO date is different
				
			}
			else if(EvalConstants.EVALUATION_STATE_CLOSED.equals(eval.getState())) {
				
				//Check if the Viewavle state job needs reschuling.
				
				//TODO date is different
			}
			else if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(eval.getState())) {
				
				//TODO studentsDate, instructorsDate, and viewDate
			}
		}
		else {
			//TODO
		}
	}
		
	/**
	 * Schedule a Quartz job with the provided job parameters and schedule.
	 * @param jobDetail The job parameters.
	 * @param trigger The schedule for running the job.
	 * @throws SchedulerException
	 */
	private void scheduleStatefulJob(JobDetail jobDetail, SimpleTrigger trigger)
		throws SchedulerException
	{
		try
		{
			jobSchedulerFacade.getScheduler().scheduleJob(jobDetail, trigger);
		}
		catch(Exception e)
		{
			//TODO
		}
	}
}
