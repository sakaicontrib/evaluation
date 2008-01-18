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
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.logic.utils.EvalUtils;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.time.api.TimeService;

/**
 * Handle job scheduling related to EvalEvaluation state transitions.</br>
 * Dates that have not passed may be changed, which might then require
 * rescheduling a job to keep jobs and EvalEvaluation dates in sync.
 * 
 * @author rwellis
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalJobLogicImpl implements EvalJobLogic {

	private static Log log = LogFactory.getLog(EvalJobLogicImpl.class);
	
	//the component scheduled by the ScheduledInvocationManager
	private final String COMPONENT_ID = "org.sakaiproject.evaluation.logic.externals.EvalScheduledInvocation";

	private final String SEPARATOR = "/";           // max-32:12345678901234567890123456789012
	private final String EVENT_EMAIL_REMINDER              = "evaluation.email.reminder";

	private EvalEmailsLogic emails;
	public void setEmails(EvalEmailsLogic emails) {
		this.emails = emails;
	}
	
	private EvalEvaluationsLogic evalEvaluationsLogic;
	public void setEvalEvaluationsLogic(EvalEvaluationsLogic evalEvaluationsLogic) {
		this.evalEvaluationsLogic = evalEvaluationsLogic;
	}
	
	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	private ScheduledInvocationManager scheduledInvocationManager;
	public void setScheduledInvocationManager(ScheduledInvocationManager scheduledInvocationManager) {
		this.scheduledInvocationManager = scheduledInvocationManager;
	}
	
	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
	}
	
	private TimeService timeService;
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}
	
	public void init()  {
		log.debug("EvalJobLogicImpl.init()");
	}
	
	public EvalJobLogicImpl() {
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
		
		if(log.isDebugEnabled())
			log.debug("EvalJobLogicImpl.checkInvocationDate(" + eval.getId() + "," + jobType + "," + correctDate);
		
		//reminders are treated in processEvaluationChange() EvalConstants.EVALUATION_STATE_ACTIVE
		if(EvalConstants.JOB_TYPE_REMINDER.equals(jobType)) return;
		
		//get the delayed invocation, a pea with .Date Date
		String id = eval.getId().toString();
		String opaqueContext = id + SEPARATOR + jobType;
		DelayedInvocation[] invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		
		//if there are no invocations, return
		if(invocations == null || invocations.length == 0) {
			return;
		}
		else if(invocations.length == 1) {
			//we expect at most one delayed invocation matching componentId and opaqueContext
			
			//if the dates differ
			if(invocations[0].date.compareTo(correctDate) != 0) {
				
				//remove the old invocation
				scheduledInvocationManager.deleteDelayedInvocation(invocations[0].uuid);
				if(log.isDebugEnabled())
					log.debug("EvalJobLogicImpl.checkInvocationDate remove the old invocation " + invocations[0].uuid + "," + invocations[0].contextId + "," + invocations[0].date);
				
				//and schedule a new invocation
				scheduledInvocationManager.createDelayedInvocation(timeService.newTime(correctDate.getTime()), COMPONENT_ID, opaqueContext);
				if(log.isDebugEnabled())
					log.debug("EvalJobLogicImpl.checkInvocationDate and schedule a new invocation " + correctDate + "," + COMPONENT_ID + "," + opaqueContext + ")");
				
				//the due date was changed, so reminder might need to be removed
				if(EvalConstants.JOB_TYPE_DUE.equals(jobType)) {
					fixReminder(eval.getId());
				}
			}
		}
		else {
			log.warn(this + ".checkInvocationDate: multiple delayed invocations of componentId '" + COMPONENT_ID + "', opaqueContext '" + opaqueContext +"'");
		}
	}
	
	/**
	 * Remove reminder if the due date now comes before the reminder or reminder days was changed to 0
	 * 
	 * @param evalId the EvalEvaluation id
	 */
	private void fixReminder(Long evaluationId) {
		EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evaluationId);
		String opaqueContext = evaluationId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_REMINDER;
		DelayedInvocation[] invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		if(invocations != null && invocations.length == 1) {
			DelayedInvocation reminder = invocations[0];
			Date reminderAt = reminder.date;
			if(eval.getReminderDays().intValue() == 0 || reminderAt.after(eval.getDueDate())) {
				//remove reminder
				scheduledInvocationManager.deleteDelayedInvocation(reminder.uuid);
				if(log.isDebugEnabled())
					log.debug("EvalJobLogicImpl.fixReminders remove reminder after the due date " + reminder.uuid + "," + reminder.contextId + "," + reminder.date);
			}
		}
	}
	
	/**
	 * Remove the EvalScheduledInvocation for an EvalEvaluation reminder
	 * 
	 * @param evalId the EvalEvaluation id
	 */
	public void removeScheduledReminder(Long evalId) {
		
		if(evalId == null) return;
		String userId = externalLogic.getCurrentUserId();
		if(evalEvaluationsLogic.canRemoveEvaluation(userId, evalId)) {
			String opaqueContext = evalId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_REMINDER;
			deleteInvocation(opaqueContext);
		}
	}
	
	/**
	 * Remove all EvalScheduledInvocations for an EvalEvaluation
	 * 
	 * @param evalId the EvalEvaluation id
	 */
	public void removeScheduledInvocations(Long evalId) {
		
		if(evalId == null) return;
		String userId = externalLogic.getCurrentUserId();
		if(evalEvaluationsLogic.canRemoveEvaluation(userId, evalId)) {
		
			//TODO be selective based on the state of the EvalEvaluation when deleted
			String opaqueContext = null;
			String prefix = evalId.toString() + SEPARATOR;
			opaqueContext =  prefix + EvalConstants.JOB_TYPE_CREATED;
			deleteInvocation(opaqueContext);
			opaqueContext =  prefix + EvalConstants.JOB_TYPE_ACTIVE;
			deleteInvocation(opaqueContext);
			opaqueContext =  prefix+ EvalConstants.JOB_TYPE_DUE;
			deleteInvocation(opaqueContext);
			opaqueContext =  prefix+ EvalConstants.JOB_TYPE_CLOSED;
			deleteInvocation(opaqueContext);
			opaqueContext =  prefix+ EvalConstants.JOB_TYPE_REMINDER;
			deleteInvocation(opaqueContext);
			opaqueContext =  prefix + EvalConstants.JOB_TYPE_VIEWABLE;
			deleteInvocation(opaqueContext);
			opaqueContext =  prefix+ EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS;
			deleteInvocation(opaqueContext);
			opaqueContext =  prefix+ EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS;
			deleteInvocation(opaqueContext);
		}
	}

	/**
	 * Delete the EvalScheduledInvocation identified by EvalEvaluation id and jobType, if found
	 * 
	 * @param opaqueContext the EvalEvaluation id/jobType
	 */
	private void deleteInvocation(String opaqueContext) {
		if(opaqueContext == null || opaqueContext == "") return;
		DelayedInvocation[] invocations;
		invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		if(invocations != null ) {
			if(invocations.length == 1) {
				scheduledInvocationManager.deleteDelayedInvocation(invocations[0].uuid);
			}
			else if(invocations.length > 1){
				log.warn(this + ".deleteInvocation(" + opaqueContext  + ") multiple invocations were found.");
				//duplicate opaqueContext records would need to be deleted from db using sql
				throw new RuntimeException(opaqueContext  + " multiple invocations were found; duplicates need to be removed.");
			}
		}
	}


	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#processNewEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)
	 */
	public void processNewEvaluation(EvalEvaluation eval) {
		
		if(eval == null)
			throw new NullPointerException("Notification of a new evaluation failed, because the evaluation was null.");
		
		if(log.isDebugEnabled())
			log.debug("EvalJobLogicImpl.processNewEvaluation(" + eval.getId() + ")");
		
		String state = EvalUtils.getEvaluationState(eval);
		if(state == null) 
			throw new NullPointerException("Notification of a new evaluation failed, because the evaluation state was null.");
		
		//send created email if instructor can add questions or opt-in or opt-out
		int instructorAdds = ((Integer)settings.get(EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER)).intValue();
		if(instructorAdds > 0 || !eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_REQUIRED)) {
			
			/* Note: email cannot be sent at this point, because it precedes saveAssignGroup,
			 * so we schedule email for ten minutes from now, also giving instructor ten minutes 
			 * to delete the evaluation and its notification
			 */
			long runAt = new Date().getTime() + (1000 * 60 * 10);
			scheduleJob(eval.getId(), new Date(runAt), EvalConstants.JOB_TYPE_CREATED);
		}
		scheduleJob(eval.getId(), eval.getStartDate(), EvalConstants.JOB_TYPE_ACTIVE);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#processEvaluationChange(org.sakaiproject.evaluation.model.EvalEvaluation)
	 */
	public void processEvaluationChange(EvalEvaluation eval) {
		
		if(log.isDebugEnabled())
			log.debug("EvalJobLogicImpl.processEvaluationChange(" + eval.getId() + ")");
		
		//checks
		if(eval == null) return;
		String state = EvalUtils.getEvaluationState(eval);
		if(EvalConstants.EVALUATION_STATE_UNKNOWN.equals(state)) {
			
			if(log.isWarnEnabled())
				log.warn(this + ".processEvaluationChange(Long "+  eval.getId().toString() + ") for " + eval.getTitle()  + ". Evaluation in UNKNOWN state");
			throw new RuntimeException("Evaluation '"+eval.getTitle()+"' in UNKNOWN state");
		}
		try {
			if(EvalConstants.EVALUATION_STATE_INQUEUE.equals(eval.getState())) {

				//make sure scheduleActive job invocation date matches EvalEvaluation start date
				checkInvocationDate(eval, EvalConstants.JOB_TYPE_ACTIVE, eval.getStartDate());
			}
			else if(EvalConstants.EVALUATION_STATE_ACTIVE.equals(eval.getState())) {
				
				//make sure a change in Reminder interval is handled
				removeScheduledReminder(eval.getId());
				if(eval.getReminderDays().intValue() != 0) {
					scheduleReminder(eval.getId());
				}

				/* make sure scheduleDue job invocation start date matches EvalEaluation due date
				 * and moving the due date is reflected in reminder
				 */
				checkInvocationDate(eval, EvalConstants.JOB_TYPE_DUE, eval.getDueDate());
			}
			else if (EvalConstants.EVALUATION_STATE_DUE.equals(eval.getState())) {

				//make sure scheduleClosed job invocation start date matches EvalEvaluation stop date
				checkInvocationDate(eval, EvalConstants.JOB_TYPE_CLOSED, eval.getStopDate());
			}
			else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(eval.getState())) {

				//make sure scheduleView job invocation start date matches EvalEvaluation view date 
				checkInvocationDate(eval, EvalConstants.JOB_TYPE_VIEWABLE, eval.getViewDate());
				
				//make sure scheduleView By Instructors job invocation start date matches EvalEvaluation instructor's date
				checkInvocationDate(eval, EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS, eval.getInstructorsDate());
				
				//make sure scheduleView By Students job invocation start date matches EvalEvaluation student's date
				checkInvocationDate(eval, EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS, eval.getStudentsDate());
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(log.isWarnEnabled())
				log.warn(this + ".processEvaluationChange("+  eval.getId() + ") for '" + eval.getTitle()  + "' " + e);
			throw new RuntimeException("Evaluation '" + eval.getTitle() + "' " + e);
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
	public void scheduleJob(Long evaluationId, Date runDate, String jobType) {
		if(evaluationId == null || runDate == null || jobType == null) {
			if(log.isErrorEnabled())
				log.error(this + ".scheduleJob null parameter");
			//TODO: throw exception
			return;
		}
		if(log.isDebugEnabled())
			log.debug("EvalJobLogicImpl.scheduleJob(" + evaluationId + "," + runDate + "," + jobType + ")");
		try {
			String opaqueContext = evaluationId.toString() + SEPARATOR + jobType;
			scheduledInvocationManager.createDelayedInvocation(timeService.newTime(runDate.getTime()), COMPONENT_ID, opaqueContext);
			if(log.isDebugEnabled())
				log.debug("EvalJobLogicImpl.scheduleJob scheduledInvocationManager.createDelayedInvocation(" + 
						timeService.newTime(runDate.getTime()) + "," + COMPONENT_ID + "," +  opaqueContext + ")");
		}
		catch(Exception e) {
			log.error(this + ".scheduleJob(" + evaluationId + "," + runDate.toString() + "," + jobType + ") " + e);
		}
	}
	
	/**
	 * Schedule reminders to be run under the ScheduledInvocationManager.</br>
	 * If under these conditions there is time to send a reminder before the due date, schedule one.
	 *  <ul>
	 *  	<li>Schedule a reminder, if necessary (i.e., one is not already scheduled for another group, 
	 *  resulting in notification of all groups), when there is a late notification after opt-in at the start date 
	 *  in EvalAssignsLogicImpl.saveAssignGroup() - a special case,</li>
	 *  	<li>Schedule a reminder when EvalJobLogicImpl.jobAction runs JOB_TYPE_ACTIVE - the 1st reminder, 
	 *  scheduled for start date + reminder interval,</li>
	 *   	<li>Schedule a reminder when EvalJobLogicImpl.jobAcion runs JOB_TYPE_REMINDER - the next reminder, 
	 *   scheduled when a reminder job has run and there is time remaining before the due date to schedule another reminder,</li>
	 *   	<li>Schedule a reminder when EvalJobLogicImpl.processEvaluationChange is called while EVAL_STATE_ACTIVE - i.e., 
	 *  when someone edited the settings of an active evaluation, possibly making the due date earlier or later and 
	 *  affecting an already scheduled reminder or one that now needs to be scheduled.</li>
	 *  </ul>
	 * @param evaluationId the EvalEvaluation id
	 */
	 public void scheduleReminder(Long evaluationId) {
		 //we're depending on reminders going out on time
		if(evaluationId == null) {
			log.error(this + ".scheduleReminder(): null evaluationId");
			throw new RuntimeException("Exception scheduling reminder: null evaluationId");
		}
		try {
			EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evaluationId);
			if(eval == null) {
				log.error(this + ".scheduleReminder(): null EvalEvaluation eval");
				throw new RuntimeException("Exception scheduling reminder for evaluation '" + evaluationId +"': evaluation was not found");
			}
			String opaqueContext = evaluationId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_REMINDER;
			long scheduleAt = getReminderTime(eval);
			if(scheduleAt != 0 && eval.getState().equals(EvalConstants.EVALUATION_STATE_ACTIVE)) {
				scheduledInvocationManager.createDelayedInvocation(timeService.newTime(scheduleAt), COMPONENT_ID, opaqueContext);
				if(log.isDebugEnabled())
					log.debug("EvalJobLogicImpl.scheduleReminders(" + evaluationId + ") - scheduledInvocationManager.createDelayedInvocation( " + 
							timeService.newTime(scheduleAt) + "," + 	COMPONENT_ID + "," + opaqueContext);
			}
		}
		catch(Exception e) {
			log.error(this + ".scheduleReminder(evaluationId '" + evaluationId + "'): " + e);
			throw new RuntimeException("Exception scheduling reminder for evaluation '" + evaluationId +"' " + e);
		}
	}
	
	/**
	 * Get the time to use in scheduling a reminder, which is the first reminder interval after the start date that is in the future
	 * 
	 * @param eval the EvalEvaluation
	 * @return the value to use in setting date of reminder or 0 if no reminder should be scheduled
	 */
	private long getReminderTime(EvalEvaluation eval){
		long reminderTime = 0;
		if(eval != null) {
			long startTime = eval.getStartDate().getTime();
			long dueTime = eval.getDueDate().getTime();
			long interval = 1000 * 60 * 60 * 24 * eval.getReminderDays().intValue();
			long now = new Date().getTime();
			long available = dueTime - now;
			//we'll say the future starts in 15 minutes
			if(interval != 0 && available > interval) {
				reminderTime = startTime + interval;
				while(reminderTime < now + (1000 * 60 * 15)) {
					reminderTime = reminderTime + interval;
				}
			}
		}
		return reminderTime;
	}
	
	/**
	 * Check if a job of a given type for a given evaluation is scheduled. 
	 * At most one job of a given type is scheduled per evaluation.
	 * 
	 * @param evaluationId  the EvalEvaluation id
	 * @param jobType
	 * @return
	 */
	public boolean isJobTypeScheduled(Long evaluationId, String jobType) {
		if(evaluationId == null || jobType == null) {
			log.warn(this + ".isJobTypeScheduled called with null parameter(s).");
			return false;
		}
		if(!isValidJobType(jobType)) {
			log.warn(this + ".isJobTypeScheduled called with invalid jobType '" + jobType + "'.");
			return false;
		}
		//make sure there is an evaluation
		EvalEvaluation eval = null;
		try
		{
			eval = evalEvaluationsLogic.getEvaluationById(evaluationId);
		}
		catch(Exception e) {
			log.warn(this + ".isJobTypeScheduled evaluation id '" + evaluationId + "' " + e);
			return false;
		}
		if(eval == null) {
			log.warn(this + ".isJobTypeScheduled no evaluation having id '" + evaluationId + "' was found.");
			return false;
		}
		boolean isScheduled = false;
		DelayedInvocation[] invocations;
		String opaqueContext = evaluationId.toString() + SEPARATOR + jobType;
		invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
		if(invocations != null ) {
			if(invocations.length == 1) {
				isScheduled = true;
			}
			else if(invocations.length > 1){
				log.warn(this + ".isJobTypeScheduled(" + opaqueContext  + ") multiple invocations were found.");
				isScheduled = true;
			}  
		}
		return isScheduled;
	}
	
	/**
	 * Check whether the job type is valid
	 * 
	 * @param jobType
	 * @return true if contained in EvalConstants, false otherwise
	 */
	public boolean isValidJobType(String jobType) {
		boolean isValid = false;
		if(jobType == null) return isValid;
		if(jobType.equals(EvalConstants.JOB_TYPE_ACTIVE) ||
				jobType.equals(EvalConstants.JOB_TYPE_CLOSED) ||
				jobType.equals(EvalConstants.JOB_TYPE_CREATED) ||
				jobType.equals(EvalConstants.JOB_TYPE_DUE) ||
				jobType.equals(EvalConstants.JOB_TYPE_REMINDER) ||
				jobType.equals(EvalConstants.JOB_TYPE_VIEWABLE) ||
				jobType.equals(EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS) ||
				jobType.equals(EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS))
			isValid = true;
		return isValid;
	}
	

	/**
	 * Send email to evaluation participants that an evaluation 
	 * is available for taking by clicking the contained URL
	 * 
	 * @param evalId the EvalEvaluation id
	 */
	public void sendAvailableEmail(Long evalId) {
		//For now, we always want to include the evaluatees in the evaluations
		boolean includeEvaluatees = true;
		try {
			String[] sentMessages = emails.sendEvalAvailableNotifications(evalId, includeEvaluatees);
			if(log.isDebugEnabled())
				log.debug("EvalJobLogicImpl.sendAvailableEmail(" + evalId + ")" + " sentMessages: " + sentMessages.toString());
		}
		catch(Exception e) {
			log.error(this + ".sendAvailableEmail(" + evalId + ")" + e);
		}
	}

	/**
	 *  Send email that an evaluation has been created</br>
	 *  
	 * @param evalId the EvalEvaluation id
	 */
	public void sendCreatedEmail(Long evalId) {
		boolean includeOwner = true;
		try {
			String[] sentMessages = emails.sendEvalCreatedNotifications(evalId, includeOwner);
			if(log.isDebugEnabled())
				log.debug("EvalJobLogicImpl.sendCreatedEmail(" + evalId + ")" + " sentMessages: " + sentMessages.toString());
		}
		catch(Exception e) {
			log.error(this + ".sendCreatedEmail(" + evalId + ")" + e);
		}
	}

	/**
	 * Send a reminder that an evaluation is available 
	 * to those who have not responded
	 * 
	 * @param evalId the EvalEvaluation id
	 */
	public void sendReminderEmail(Long evalId) {
		try {
			EvalEvaluation eval = evalEvaluationsLogic.getEvaluationById(evalId);
			if(eval.getState().equals(EvalConstants.EVALUATION_STATE_ACTIVE) &&
					eval.getReminderDays().intValue() != 0) {
				externalLogic.registerEntityEvent(EVENT_EMAIL_REMINDER, eval);
				String includeConstant = EvalConstants.EMAIL_INCLUDE_NONTAKERS;
				String[] sentMessages = emails.sendEvalReminderNotifications(evalId, includeConstant);
				if(log.isDebugEnabled())
					log.debug("EvalJobLogicImpl.sendReminderEmail(" + evalId + ")" + " sentMessages: " + sentMessages.toString());
			}
		}
		catch(Exception e) {
			log.error(this + ".sendReminderEmail(" + evalId + ")" + e);
		}
	}

	/**
	 * Send email that the results of an evaluation may be viewed now.</br>
	 * Notification may be sent to owner only, instructors and students together or
	 * separately.
	 * 
	 * @param evalId the EvalEvaluation id
	 * @param the job type fom EvalConstants
	 */
	public void sendViewableEmail(Long evalId, String jobType, Boolean resultsPrivate) {
		/*
		 * TODO when booleans below are set dynamically, replace the use of job type to distinguish
		 * recipients with the setting of these parameters before calling emails.sendEvalResultsNotifications().
		 * Then one job type JOB_TYPE_VIEWABLE can be scheduled as needed.
		 */
		boolean includeEvaluatees = true;
		boolean includeAdmins = true;
		
		try {
			//if results are private, only send notification to owner
			if(resultsPrivate.booleanValue()) {
				includeEvaluatees = false;
				includeAdmins = false;
				emails.sendEvalResultsNotifications(jobType, evalId, includeEvaluatees, includeAdmins);
				if(log.isDebugEnabled())
					log.debug("EvalJobLogicImpl.sendViewableEmail(" + evalId + "," + jobType + ", resultsPrivate " + resultsPrivate + ")");
			}
			else {
				emails.sendEvalResultsNotifications(jobType, evalId, includeEvaluatees, includeAdmins);
				if(log.isDebugEnabled())
					log.debug("EvalJobLogicImpl.sendViewableEmail(" + evalId + "," + jobType + ", resultsPrivate " + resultsPrivate + ")");
			}
		}
		catch(Exception e) {
			log.error(this + ".sendViewableEmail(" + evalId + "," +  jobType + "," + includeAdmins + ")" + e);
		}
	}

	public void removeScheduledInvocations(EvalEvaluation eval) {
		// TODO Auto-generated method stub
		
	}

	public void scheduleLateOptInNotification(Long evaluationId, String evalGroupId) {
		emails.sendEvalAvailableGroupNotification(evaluationId, evalGroupId);
	}
}

