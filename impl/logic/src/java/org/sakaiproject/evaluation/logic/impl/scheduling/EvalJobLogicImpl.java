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
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.utils.EvalUtils;
import org.sakaiproject.time.api.TimeService;

/**
 * Handle job scheduling related to EvalEvaluation transition points.
 * Dates that have not passed may be changed, which might then require
 * rescheduling a job to keep jobs and EvalEvaulation dates in sync.
 * 
 * @author rwellis
 *
 */
public class EvalJobLogicImpl implements EvalJobLogic {
	
//	TODO track events
	
	private static Log log = LogFactory.getLog(EvalJobLogicImpl.class);
	
	private final String COMPONENT_ID = "evalScheduledInvocation";
	private final String SEPARATOR = "/";
	
	EvalEvaluation eval = null;
	
	private ScheduledInvocationManager scheduledInvocationManager;
	public void setScheduledInvocationManager(ScheduledInvocationManager scheduledInvocationManager) {
		this.scheduledInvocationManager = scheduledInvocationManager;
	}
	private TimeService timeService;
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}
	
	public void init()  {
		log.debug("init");
	}
	
	public EvalJobLogicImpl() {
		
	}
	
	/**
	 * See if there are any delayed invocations matching an EvalEvaluation and
	 * job type.
	 * 
	 * @param evalId the EvalEvaluation id
	 * @param jobType the type of job (from EvalConstants SCHEDULED_CMD constants)
	 * @return true if there are delayed invocations, false otherwise
	 */
	private boolean isDelayedInvocation(Long evalId, String jobType){
		
		//invocations are removed from the database after being run, so these are all pending
		boolean exists = false;
		String id = evalId.toString();
		String opaqueContext = id + SEPARATOR + jobType;
		if(scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext).length > 0)
			exists = true;
		return exists;
	}
	
	/**
	 * Reschedule delayed invocations if necessary
	 * 
	 * @param eval the EvalEvaluation
	 * @param jobType the type of job (from EvalConstants SCHEDULED_CMD constants)
	 * @param  correctDate the date when the job should be invoked
	 * 
	 */
	private void rescheduleDelayedInvocation(EvalEvaluation eval, String jobType, Date correctDate) {
		
		if(eval == null || jobType == null || correctDate == null) return;
		
		/* we don't reschedule reminders, because the active date won't change once it becomes active,
		 * and therefore reminder dates also remain fixed
		 */
		if(EvalConstants.SCHEDULED_CMD_SEND_REMINDER.equals(jobType)) return;
		
		//get the delayed invocation, a pea with .Date Date
		String id = eval.getId().toString();
		String opaqueContext = id + SEPARATOR + jobType;
		DelayedInvocation[] invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		
		//we expect at most one delayed invocation matching componentId and opaqueContxt
		if(invocations.length == 0) return;

		//check the date
		if(invocations.length == 1) {
			
			//if the dates differ
			if(invocations[0].date.compareTo(correctDate) != 0) {
				
					//remove the old invocation
					scheduledInvocationManager.deleteDelayedInvocation(invocations[0].uuid);
					
					//and schedule a new invocation
					scheduledInvocationManager.createDelayedInvocation(timeService.newTime(correctDate.getTime()), COMPONENT_ID, opaqueContext);
			}
		}
		if(invocations.length > 1) {
			log.warn(this + " multiple delayed invocations of componentId '" + COMPONENT_ID + "', opaqueContext '" + opaqueContext +"'");
		}
	}
	
	/**
	 * Remove delayed reminder invocations for an EvalEvaluation.
	 * 
	 * @param evalId the EvalEvaulation id
	 */
	private void removeReminders(Long evalId) {
		
		String opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.SCHEDULED_CMD_SEND_REMINDER;
		DelayedInvocation[] invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		for(int i = 0; i < invocations.length; i++) {
			scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
		}
	}
	
	/**
	 * Remove all ScheduledInvocationCammand jobs for an EvalEvaulation.
	 * 
	 * @param evalId the EvalEvaulation id
	 */
	private void removeAllScheduledInvocations(Long evalId) {

		//TODO be selective based on the state of the EvalEvaluation when deleted
		
		String opaqueContext = null;
		DelayedInvocation[] invocations = null;
		
		opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.SCHEDULED_CMD_FIX_STATE;
		invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		for(int i = 0; i < invocations.length; i++) {
			scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
		}
		opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.SCHEDULED_CMD_SEND_CREATED;
		invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		for(int i = 0; i < invocations.length; i++) {
			scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
		}
		opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.SCHEDULED_CMD_SEND_ACTIVE;
		invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		for(int i = 0; i < invocations.length; i++) {
			scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
		}
		opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.SCHEDULED_CMD_SEND_REMINDER;
		invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		for(int i = 0; i < invocations.length; i++) {
			scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
		}
		opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.SCHEDULED_CMD_SEND_VIEWABLE;
		invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		for(int i = 0; i < invocations.length; i++) {
			scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
		}
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#processNewEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)
	 */
	public void processNewEvaluation(EvalEvaluation eval) throws Exception {
		
		if(eval == null)
			throw new NullPointerException("Notification of a new evaluation failed, because the evaluation was null.");
		
		String state = EvalUtils.getEvaluationState(eval);
		if(state == null) 
			throw new NullPointerException("Notification of a new evaluation failed, because the evaluation state was null.");
		
		/*send created notification
		scheduledJobInvocation(eval.getId(), new Date(), EvalConstants.SCHEDULED_CMD_SEND_CREATED);
		*/
		
		//schedule change to active at start date
		scheduledJobInvocation(eval.getId(), eval.getStartDate(), EvalConstants.SCHEDULED_CMD_FIX_STATE);
	}


	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#processEvaluationChange(org.sakaiproject.evaluation.model.EvalEvaluation)
	 */
	public void processEvaluationChange(EvalEvaluation eval) throws Exception {
		log.debug(this + ".processEvaluationChange evalId: " + eval.getId().toString());

		if(eval == null)
			throw new NullPointerException("Notification of a new evaluation failed, because the evaluation id was null.");
		
		String state = EvalUtils.getEvaluationState(eval);
		if(EvalConstants.EVALUATION_STATE_UNKNOWN.equals(state)) {
			
			if(log.isWarnEnabled())
				log.warn(this + ".processEvaluationChange(Long "+  eval.getId().toString() + ") for " + eval.getTitle()  + ". Evaluation in UNKNOWN state");
			throw new Exception("Evaluation '"+eval.getTitle()+"' in UNKNOWN state");
		}
		try {
			if(EvalConstants.EVALUATION_STATE_INQUEUE.equals(eval.getState())) {
				
				//is there an active job invocation outstanding?
				if(isDelayedInvocation(eval.getId(), EvalConstants.SCHEDULED_CMD_SEND_ACTIVE)) {
					
					//make sure active job invocation date matches EvalEvaluation start date
					rescheduleDelayedInvocation(eval, EvalConstants.SCHEDULED_CMD_SEND_ACTIVE, eval.getStartDate());
				}
			}
			else if(EvalConstants.EVALUATION_STATE_ACTIVE.equals(eval.getState())) {
				
				//is there a reminder job invocation outstanding?
				if(!isDelayedInvocation(eval.getId(), EvalConstants.SCHEDULED_CMD_SEND_REMINDER)) {
					
					//pre-schedule a fixed number of reminders at a selected-number-of-days interval
					scheduleReminders(eval.getId());
				}
				
				//is there a due job invocation outstanding?
				if(isDelayedInvocation(eval.getId(), EvalConstants.SCHEDULED_CMD_FIX_STATE)) {
					
					//if so make sure due job invocation start date matches EvalEaluation due date
					rescheduleDelayedInvocation(eval, EvalConstants.SCHEDULED_CMD_FIX_STATE, eval.getDueDate());
				}
				else {
					
					//if not, create a delayed invocation for the eval.DueDate()
					scheduledJobInvocation(eval.getId(), eval.getDueDate(), EvalConstants.SCHEDULED_CMD_FIX_STATE);
				}
			}
			else if (EvalConstants.EVALUATION_STATE_DUE.equals(eval.getState())) {
				
				//is there a closed job invocation outstanding?
				if(isDelayedInvocation(eval.getId(), EvalConstants.SCHEDULED_CMD_FIX_STATE)) {
					
					//if so make sure closed job invocation start date matches EvalEvaluation stop date
					rescheduleDelayedInvocation(eval, EvalConstants.SCHEDULED_CMD_FIX_STATE, eval.getStopDate());
				}
				else {
					
					//if not, create a delayed invocation for the eval.StopDate
					scheduledJobInvocation(eval.getId(), eval.getStopDate(), EvalConstants.SCHEDULED_CMD_FIX_STATE);
				}
				
				//remove any remaining pre-scheduled reminders
				if(isDelayedInvocation(eval.getId(),EvalConstants.SCHEDULED_CMD_SEND_REMINDER)) {
					
					removeReminders(eval.getId());
				}
			}
			else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(eval.getState())) {
				
				//is there a viewable job invocation outstanding?
				if(isDelayedInvocation(eval.getId(), EvalConstants.SCHEDULED_CMD_SEND_VIEWABLE)) {
					
					//if so, make sure invocation start date matches EvalEvaluation stop date 
					rescheduleDelayedInvocation(eval, EvalConstants.SCHEDULED_CMD_SEND_VIEWABLE, eval.getViewDate());
				}
				else {
					
					//if not, create a delayed invocation to run on the view date
					scheduledJobInvocation(eval.getId(), eval.getViewDate(), EvalConstants.SCHEDULED_CMD_SEND_VIEWABLE);
	
				}
			}
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled())
				log.warn(this + ".processEvaluationChange(Long "+  eval.getId().toString() + ") for '" + eval.getTitle()  + "' " + e);
			throw new Exception("Evaluation '" + eval.getTitle() + "' " + e);
		}
	}
	
	/**
	 * Schedule a command using the ScheduledInvocationManager.
	 * When is specified by runDate, what by COMPONENT_ID, and what to do
	 * by opaqueContext. </br> OpaqueContext contains an EvalEvaluationId 
	 * and a method from EvalTransition.
	 *
	 *@param evaluationId the EvalEvaluation id
	 *@param runDate the Date when the command should be invoked
	 *@param jobType the type of job (from EvalConstants SCHEDULED_CMD constants)
	 */
	private void scheduledJobInvocation(Long evaluationId, Date runDate, String jobType) {
		
		String opaqueContext = evaluationId.toString() + SEPARATOR + jobType;
		scheduledInvocationManager.createDelayedInvocation(timeService.newTime(runDate.getTime()), COMPONENT_ID, opaqueContext);
	}
	
	/**
	 * Schedule a fixed number of reminder jobs to be run under the ScheduledInvocationManager.
	 * The ScheduledInvocationManager has no concept of repeating an invocation, so a number
	 * of reminders are pre-scheduled and then removed if still pending when the EvalEvaluation
	 * is due.
	 * 
	 * @param evaluationId the EvalEvaluation id
	 */
	private void scheduleReminders(Long evaluationId) {
		
		String opaqueContext = evaluationId.toString() + SEPARATOR + EvalConstants.SCHEDULED_CMD_SEND_REMINDER;
		int numberOfReminders = 3;
		long interval = 1000 * 60 * 60 * 24 * eval.getReminderDays().intValue();
		long runAt = eval.getStartDate().getTime();
		for(int i = 0; i < numberOfReminders; i++) {
			runAt = runAt + interval;
			scheduledInvocationManager.createDelayedInvocation(timeService.newTime(runAt), COMPONENT_ID, opaqueContext);
		}
	}

}

