/**********************************************************************************
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
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.model.utils.EvalUtils;
import org.sakaiproject.time.api.TimeService;

/**
 * Handle job scheduling related to EvalEvaluation state transitions.</br>
 * Dates that have not passed may be changed, which might then require
 * rescheduling a job to keep jobs and EvalEvaluation dates in sync.
 * 
 * @author rwellis
 *
 */
public class EvalJobLogicImpl implements EvalJobLogic {
	
//	TODO jleasia: track events

	private static Log log = LogFactory.getLog(EvalJobLogicImpl.class);
	
	//the component scheduled by the ScheduledInvocationManager
	private final String COMPONENT_ID = "org.sakaiproject.evaluation.logic.externals.EvalScheduledInvocation";

	private final String SEPARATOR = "/";
	
	private EvalEmailsLogic emails;
	public void setEmails(EvalEmailsLogic emails) {
		this.emails = emails;
	}
	private EvalEvaluationsLogic evalEvaluationsLogic;
	public void setEvalEvaluationsLogic(EvalEvaluationsLogic evalEvaluationsLogic) {
		this.evalEvaluationsLogic = evalEvaluationsLogic;
	}
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
	 * See if there is a component invocation matching an EvalEvaluation
	 * and job type scheduled through the ScheduledInvocationManager.</br>
	 * Invocations are removed from the database after 
	 * being run, so any that are found are for pending jobs.
	 * 
	 * @param evalId the EvalEvaluation id
	 * @param jobType the type of job (from EvalConstants SCHEDULED_CMD constants)
	 * @return true if there are delayed invocations, false otherwise
	 */
	private boolean isPending(Long evalId, String jobType){
		boolean exists = false;
		String id = evalId.toString();
		String opaqueContext = id + SEPARATOR + jobType;
		if(scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext).length > 0)
			exists = true;
		log.debug(this + ".isPending for evalId " + evalId +  " jobType " + jobType + " is " + exists);
		return exists;
	}
	
	/**
	 * Compare the date when a job will be invoked with the 
	 * EvalEvaluation date to see if the job needs to be rescheduled.
	 * 
	 * @param eval the EvalEvaluation
	 * @param jobType the type of job (refer to EvalConstants)
	 * @param  correctDate the date when the job should be invoked
	 * 
	 */
	private void checkInvocationDate(EvalEvaluation eval, String jobType, Date correctDate) {
		
		if(eval == null || jobType == null || correctDate == null) return;
		
		/* We don't reschedule reminders, because the active date won't change 
		 * once an evaluation becomes active, and therefore reminder dates also 
		 * remain fixed. We do add or remove reminders if the due date is moved
		 * forward or backward.
		 */
		if(EvalConstants.JOB_TYPE_REMINDER.equals(jobType)) return;
		
		//get the delayed invocation, a pea with .Date Date
		String id = eval.getId().toString();
		String opaqueContext = id + SEPARATOR + jobType;
		DelayedInvocation[] invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		
		//if there are none, return
		if(invocations.length == 0) {
			return;
		}
		else if(invocations.length == 1) {
			//we expect at most one delayed invocation matching componentId and opaqueContxt
			
			//if the dates differ
			if(invocations[0].date.compareTo(correctDate) != 0) {
				
				//remove the old invocation
				scheduledInvocationManager.deleteDelayedInvocation(invocations[0].uuid);
				log.debug(this + ".checkInvocationDate for eval " + eval.getTitle() + " jobType " + jobType + " correctDate " + correctDate.toString() + ": old invocation deleted");
				
				//and schedule a new invocation
				scheduledInvocationManager.createDelayedInvocation(timeService.newTime(correctDate.getTime()), COMPONENT_ID, opaqueContext);
				log.debug(this + ".checkInvocationDate for eval " + eval.getTitle() + " jobType " + jobType + " correctDate " + correctDate.toString() + ": new invocation created");
				
				//the due date was changed, so reminders might need to be added or removed
				if(EvalConstants.JOB_TYPE_DUE.equals(jobType)) {
					fixReminders(eval.getId());
				}
			}
		}
		else {
			log.warn(this + ".checkInvocationDate: multiple delayed invocations of componentId '" + COMPONENT_ID + "', opaqueContext '" + opaqueContext +"'");
		}
	}
	
	/**
	 * Add or remove reminders if the due date is moved forward or back while in the active state
	 * 
	 * @param evalId the EvalEvaluation id
	 */
	private void fixReminders(Long evaluationId) {
		//TODO refactor with scheduleReminders
		EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evaluationId);
		String opaqueContext = evaluationId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_REMINDER;
		DelayedInvocation lastReminder = null;
		long start = 0;
		
		//if the due date is sooner, any reminders after the due date should be removed
		if(isPending(evaluationId, EvalConstants.JOB_TYPE_REMINDER)) {
			DelayedInvocation[] invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
			lastReminder = invocations[0];
			for(int i = 0; i < invocations.length; i++) {
				
				//remove reminders after the due date
				DelayedInvocation invocation = invocations[i];
				Date runAt = invocation.date;
				if(runAt.after(eval.getDueDate())) {
					scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
					log.debug(this + ".fixReminders for eval " + evaluationId + ": removed reminder scheduled at " + runAt.toString());
				}
				else {
					if(invocation.date.after(lastReminder.date)) {
						lastReminder = invocation;
					}
				}
			}
		}
		
		//if the due date is later, it might be necessary to schedule more reminders
		if(lastReminder != null) {
			//start at the last reminder
			start = lastReminder.date.getTime();
		}
		else {
			//start at the current time
			start = timeService.newTime().getTime();
		}
		long due = eval.getDueDate().getTime();	
		long available = due - start;
		long interval = 1000 * 60 * 60 * 24 * eval.getReminderDays().intValue();
		long numberOfReminders = available/interval;
		long runAt = start;
		
		//schedule more reminders
		for(int i = 0; i < numberOfReminders; i++) {
			if(runAt + interval < due) {
				runAt = runAt + interval;
				scheduledInvocationManager.createDelayedInvocation(timeService.newTime(runAt), COMPONENT_ID, opaqueContext);
				log.debug(this + ".fixReminders for eval " + evaluationId + ": reminder scheduled for " + (new Date(runAt)).toString());
			}
		}
	}
	
	/**
	 * Remove all ScheduledInvocationCammand jobs for an EvalEvaluation
	 * 
	 * @param evalId the EvalEvaluation id
	 */
	private void removeAllScheduledInvocations(Long evalId) {
		
		//TODO be selective based on the state of the EvalEvaluation when deleted
		String opaqueContext = null;
		DelayedInvocation[] invocations = null;
		
		opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_ACTIVE;
		invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		for(int i = 0; i < invocations.length; i++) {
			scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
		}
		opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_DUE;
		invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		for(int i = 0; i < invocations.length; i++) {
			scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
		}
		opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_CLOSED;
		invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		for(int i = 0; i < invocations.length; i++) {
			scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
		}
		opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_REMINDER;
		invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		for(int i = 0; i < invocations.length; i++) {
			scheduledInvocationManager.deleteDelayedInvocation(invocations[i].uuid);
		}
		opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_VIEWABLE;
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
		
		//Note: email cannot be sent at this point because it precedes saveAssignGroup
		//sendCreatedEmail(eval.getId());

		scheduleJob(eval.getId(), eval.getStartDate(), EvalConstants.JOB_TYPE_ACTIVE);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#processEvaluationChange(org.sakaiproject.evaluation.model.EvalEvaluation)
	 */
	public void processEvaluationChange(EvalEvaluation eval) throws Exception {
		log.debug(this + ".processEvaluationChange evalId: " + eval.getId().toString());
		
		//checks
		if(eval == null) return;
		String state = EvalUtils.getEvaluationState(eval);
		if(EvalConstants.EVALUATION_STATE_UNKNOWN.equals(state)) {
			
			if(log.isWarnEnabled())
				log.warn(this + ".processEvaluationChange(Long "+  eval.getId().toString() + ") for " + eval.getTitle()  + ". Evaluation in UNKNOWN state");
			throw new Exception("Evaluation '"+eval.getTitle()+"' in UNKNOWN state");
		}
		try {
			if(EvalConstants.EVALUATION_STATE_INQUEUE.equals(eval.getState())) {
				
				//if there an active job invocation outstanding
				if(isPending(eval.getId(), EvalConstants.JOB_TYPE_ACTIVE)) {
					
					//make sure active job invocation date matches EvalEvaluation start date
					checkInvocationDate(eval, EvalConstants.JOB_TYPE_ACTIVE, eval.getStartDate());
				}
			}
			else if(EvalConstants.EVALUATION_STATE_ACTIVE.equals(eval.getState())) {
				
				//if there a due job invocation outstanding
				if(isPending(eval.getId(), EvalConstants.JOB_TYPE_DUE)) {
					
					/* make sure due job invocation start date matches EvalEaluation due date
					 * and moving the due date is reflected in reminders
					 */
					checkInvocationDate(eval, EvalConstants.JOB_TYPE_DUE, eval.getDueDate());
				}
			}
			else if (EvalConstants.EVALUATION_STATE_DUE.equals(eval.getState())) {

				//if there a closed job invocation outstanding
				if(isPending(eval.getId(), EvalConstants.JOB_TYPE_CLOSED)) {
					
					//make sure closed job invocation start date matches EvalEvaluation stop date
					checkInvocationDate(eval, EvalConstants.JOB_TYPE_CLOSED, eval.getStopDate());
				}
			}
			else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(eval.getState())) {

				//if there a viewable job invocation outstanding1
				if(isPending(eval.getId(), EvalConstants.JOB_TYPE_VIEWABLE)) {
					
					//make sure invocation start date matches EvalEvaluation view date 
					checkInvocationDate(eval, EvalConstants.JOB_TYPE_VIEWABLE, eval.getViewDate());
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
	 * Schedule a job using the ScheduledInvocationManager.</br>
	 * "When" is specified by runDate, "what" by componentId, and "what to do"
	 * by opaqueContext. OpaqueContext contains an EvalEvaluationId 
	 * and a jobType from EvalConstants, which is used to keep track of
	 * pending jobs and reschedule or remove jobs when necessary.
	 *
	 *@param evaluationId the id of an EvalEvaluation
	 *@param runDate the Date when the command should be invoked
	 *@param jobType the type of job, from EvalConstants
	 */
	private void scheduleJob(Long evaluationId, Date runDate, String jobType) {
		String opaqueContext = evaluationId.toString() + SEPARATOR + jobType;
		scheduledInvocationManager.createDelayedInvocation(timeService.newTime(runDate.getTime()), COMPONENT_ID, opaqueContext);
	}
	
	/**
	 * Schedule reminders to be run under the ScheduledInvocationManager.</br>
	 * The ScheduledInvocationManager has no concept of repeating an invocation, so a number
	 * of reminders are pre-scheduled.
	 * 
	 * @param evaluationId the EvalEvaluation id
	 */
	private void scheduleReminders(Long evaluationId) {
		
		EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evaluationId);
		String opaqueContext = evaluationId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_REMINDER;
		
		//schedule reminders at selected intervals while the evaluation is available
		long start = eval.getStartDate().getTime();
		long due = eval.getDueDate().getTime();
		long available = due - start;
		long interval = 1000 * 60 * 60 * 24 * eval.getReminderDays().intValue();
		long numberOfReminders = available/interval;
		long runAt = eval.getStartDate().getTime();
		for(int i = 0; i < numberOfReminders; i++) {
			if(runAt + interval < due) {
				runAt = runAt + interval;
				scheduledInvocationManager.createDelayedInvocation(timeService.newTime(runAt), COMPONENT_ID, opaqueContext);
				log.debug("reminder scheduled for " + (new Date(runAt)).toString());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#jobAction(java.lang.Long)
	 */
	public void jobAction(Long evaluationId) {
		
		/* Note: If interactive response time is too slow waiting for
		 * mail to be sent, sending mail could be done as another type
		 * of job run by the scheduler in a separate thread.
		 */
		log.debug(this + "jobAction for " + evaluationId);
		EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evaluationId);
		
		//fix EvalEvaluation state
		String state = evalEvaluationsLogic.getEvaluationState(evaluationId);
		log.debug(this + "state set to " + state);
		
		//send email and/or schedule jobs
		if(EvalConstants.EVALUATION_STATE_ACTIVE.equals(state)) {
			
			sendAvailableEmail(evaluationId);
			scheduleJob(eval.getId(), eval.getDueDate(), EvalConstants.JOB_TYPE_DUE);
			scheduleReminders(eval.getId());
		}
		else if(EvalConstants.EVALUATION_STATE_DUE.equals(state)) {
			
			scheduleJob(eval.getId(), eval.getStopDate(), EvalConstants.JOB_TYPE_CLOSED);
		}
		else if(EvalConstants.EVALUATION_STATE_CLOSED.equals(state)) {
			scheduleJob(eval.getId(), eval.getViewDate(), EvalConstants.JOB_TYPE_VIEWABLE);
		}
		else if(EvalConstants.EVALUATION_STATE_VIEWABLE.equals(state)) {
			sendViewableEmail(evaluationId);
		}
	}
	
	/**
	 * Send email to evaluation participants that an evaluation 
	 * is available for taking by clicking the contained URL
	 * 
	 * @param evalId the EvalEvaluation id
	 */
	public void sendAvailableEmail(Long evalId) {
		boolean includeEvaluatees = true;
		String[] sentMessages = emails.sendEvalAvailableNotifications(evalId, includeEvaluatees);
		log.debug(this + ".sendAvailableEmail: for evalId " + evalId + " sentMessages " + sentMessages.toString());
	}

	/**
	 *  Send email that an evaluation has been created</br>
	 *  not implemented
	 *  
	 * @param evalId the EvalEvaluation id
	 */
	public void sendCreatedEmail(Long evalId) {
		boolean includeOwner = true;
		String[] sentMessages = emails.sendEvalCreatedNotifications(evalId, includeOwner);
		log.debug(this + ".sendCreatedEmail: for evalId " + evalId + " sentMessages " + sentMessages.toString());
	}

	/**
	 * Send a reminder that an evaluation is available 
	 * for taking to those who have not responded
	 * 
	 * @param evalId the EvalEvaluation id
	 */
	public void sendReminderEmail(Long evalId) {
		String includeConstant = EvalConstants.EMAIL_INCLUDE_ALL;
		String[] sentMessages = emails.sendEvalReminderNotifications(evalId, includeConstant);
		log.debug(this + ".sendReminderEmail: for evalId " + evalId + " sentMessages " + sentMessages.toString());
	}

	/**
	 * Send email that the results of an evaluation may be viewed now.
	 * 
	 * @param evalId the EvalEvaluation id
	 */
	public void sendViewableEmail(Long evalId) {
		boolean includeEvaluatees = true;
		boolean includeAdmins = true;
		String[] sentMessages = emails.sendEvalResultsNotifications(evalId, includeEvaluatees, includeAdmins);
		log.debug(this + ".sendViewableEmail: for evalId " + evalId + " sentMessages " + sentMessages.toString());
	}
}
