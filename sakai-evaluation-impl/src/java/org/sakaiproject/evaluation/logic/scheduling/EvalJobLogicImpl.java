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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.logic.model.EvalScheduledJob;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.ArrayUtils;

/**
 * Handle job scheduling related to EvalEvaluation state transitions.</br> Dates that have not
 * passed may be changed, which might then require rescheduling a job to keep jobs and
 * EvalEvaluation dates in sync.
 * <b>NOTE:</b> this is a BACKUP service and should only depend on LOWER and BOTTOM services
 * (and maybe other BACKUP services if necessary)
 * 
 * @author rwellis
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalJobLogicImpl implements EvalJobLogic {

    private static final Log LOG = LogFactory.getLog(EvalJobLogicImpl.class);

    // max length for events                           // max-32:12345678901234567890123456789012
    protected final String EVENT_EVAL_VIEWABLE_INSTRUCTORS =    "eval.state.viewable.inst";
    protected final String EVENT_EVAL_VIEWABLE_STUDENTS =       "eval.state.viewable.stud";

    protected EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    protected EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }
    
    private EvalEvaluationSetupService evaluationSetupService;
    public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
        this.evaluationSetupService = evaluationSetupService;
    }

    protected EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    protected EvalEmailsLogic emails;
    public void setEmails(EvalEmailsLogic emails) {
        this.emails = emails;
    }



    /**
     * Check whether the job type is valid
     * 
     * @param jobType
     * @return true if contained in EvalConstants, false otherwise
     */
    public static boolean isValidJobType(String jobType) {
        boolean isValid = false;
        if (jobType != null) {
            for( String JOB_TYPES1 : JOB_TYPES )
            {
                if( jobType.equals( JOB_TYPES1 ) )
                {
                    isValid = true;
                    break;
                }
            }
        }
        return isValid;
    }

    public void processEvaluationStateChange(Long evaluationId, String actionState) {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("EvalJobLogicImpl.processEvaluationStateChange(" + evaluationId + ") and state=" + actionState);
        }
        if (evaluationId == null || actionState == null) {
            throw new NullPointerException("processEvaluationStateChange: both evaluationId ("+evaluationId+") and actionState ("+actionState+") must be set");
        }

        if (EvalJobLogic.ACTION_CREATE.equals(actionState)) {
            EvalEvaluation eval = getEvaluationOrFail(evaluationId);
            processNewEvaluation(eval);

        } else if (EvalJobLogic.ACTION_DELETE.equals(actionState)) {
            removeScheduledInvocations(evaluationId);

        } else if (EvalJobLogic.ACTION_UPDATE.equals(actionState)) {
            EvalEvaluation eval = getEvaluationOrFail(evaluationId);
            processEvaluationChange(eval);

        } else {
            throw new IllegalArgumentException("Invalid actionState constant, must be one of the EvalJobLogic.ACTION* ones instead of: " + actionState);
        }
    }


    /* 
     * FIXME - this method needs to be able to correctly recover from unexpected state changes,
     * it currently expects that the evaluation is in the right state for the job that was called
     * to run on it, however, it will die in all sorts of odd ways if the state is not as expected
     */
    public void jobAction(Long evaluationId, String jobType) {
        if (evaluationId == null || jobType == null) {
            throw new NullPointerException("jobAction: both evaluationId ("+evaluationId+") and jobType ("+jobType+") must be set");
        }

        /*
         * Note: If interactive response time is too slow waiting for mail to be sent, sending mail
         * could be done as another type of job run by the scheduler in a separate thread.
         */

        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        if (eval == null) {
            // this eval was purged out so wipe out all the jobs
            LOG.info("Could not find evaluation ("+evaluationId+") for jobAction ("+jobType+"), " +
            "purging out all jobs related to this evaluation...");
            removeScheduledInvocations(evaluationId);
            return;
        }

        // fix up EvalEvaluation state (also persists it)
        String evalState = evaluationService.returnAndFixEvalState(eval, true);
        Date now = new Date();

		Boolean useConsolidatedNotifications = (Boolean) this.settings.get(EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT);

        // EVALSYS-1236
        // send out the evaluation available e-mail, even if useConsolidatedNotification is true
        boolean forceSendAvailableNotification = (Boolean) this.settings.get(EvalSettings.CONSOLIDATED_FORCE_SEND_AVAILABLE_NOTIFICATION);
        // dispatch to send email and/or schedule jobs based on jobType but if 
		// consolidated emails are enabled, no scheduled emails are not enabled
		if (EvalConstants.JOB_TYPE_CREATED.equals(jobType)) {
            // if opt-in, opt-out, or questions addable, notify instructors
            sendCreatedEmail(evaluationId);

        } else if (EvalConstants.JOB_TYPE_ACTIVE.equals(jobType)) {
            // Consider flag wrt sending a mass email notifying users of opening. Send mail if flag is not set.
            boolean sendMail = ( eval.getSendAvailableNotifications() == null ? true : eval.getSendAvailableNotifications() ) 
            				&& (! useConsolidatedNotifications || forceSendAvailableNotification);
            if (sendMail){
                sendAvailableEmail(evaluationId);
            }
            if (eval.getDueDate() != null) {
                // might not have a due date set
                scheduleJob(eval.getId(), eval.getDueDate(), EvalConstants.JOB_TYPE_DUE);
            }
            int reminderDays = eval.getReminderDaysInt();
            if (reminderDays != 0 && ! useConsolidatedNotifications) {
                scheduleReminder(eval.getId());
            }

        } else if (EvalConstants.JOB_TYPE_REMINDER.equals(jobType) && ! useConsolidatedNotifications) {
        	
            int reminderDays = eval.getReminderDaysInt();
            if (evalState.equals(EvalConstants.EVALUATION_STATE_ACTIVE)
                    && reminderDays != 0
                    && EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ.equals(eval.getAuthControl())) {
                if (eval.getDueDate() == null 
                        || eval.getDueDate().after(now)) {
                    sendReminderEmail(evaluationId);
                    scheduleReminder(evaluationId);
                }
            }

        } else if (EvalConstants.JOB_TYPE_DUE.equals(jobType)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("EvalJobLogicImpl.jobAction scheduleJob(" + eval.getId() + ","
                        + eval.getStopDate() + "," + EvalConstants.JOB_TYPE_CLOSED + ")");
            }
            if (eval.getDueDate() != null) {
                if ( eval.getStopDate() != null &&
                        ! eval.getStopDate().equals(eval.getDueDate())) {
                    scheduleJob(eval.getId(), eval.getStopDate(), EvalConstants.JOB_TYPE_CLOSED);
                } else {
                    if (eval.getDueDate().before(now)) {
                        // due date is passed so close out immediately
                        scheduleJob(eval.getId(), now, EvalConstants.JOB_TYPE_CLOSED);
                    } else {
                        // due date in future so schedule a job
                        scheduleJob(eval.getId(), eval.getDueDate(), EvalConstants.JOB_TYPE_CLOSED);
                    }
                }
            }

        } else if (EvalConstants.JOB_TYPE_CLOSED.equals(jobType)) {
            // schedule results viewable by owner - admin notification
            Date viewDate = eval.getViewDate() == null ? now : eval.getViewDate();
            scheduleJob(eval.getId(), viewDate, EvalConstants.JOB_TYPE_VIEWABLE);

            if (! EvalConstants.SHARING_PRIVATE.equals(eval.getResultsSharing()) ) {
                if (eval.getInstructorViewResults()) {
                    Date instructorViewDate = eval.getInstructorsDate() == null ? now : eval.getInstructorsDate();
                    // schedule results viewable by instructors notification
                    scheduleJob(eval.getId(), instructorViewDate, EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS);
                }
                if (eval.getStudentViewResults()) {
                    Date studentViewDate = eval.getStudentsDate() == null ? now : eval.getStudentsDate();
                    // schedule results viewable by students notification
                    scheduleJob(eval.getId(), studentViewDate, EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS);
                }
            }

        } else if (EvalConstants.JOB_TYPE_VIEWABLE.equals(jobType)) {
            // send results viewable notification to owner if protected, or all if not
            sendViewableEmail(evaluationId, jobType, EvalConstants.SHARING_PRIVATE.equals(eval.getResultsSharing()) );

        } else if (EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS.equals(jobType)) {
            commonLogic.registerEntityEvent(EVENT_EVAL_VIEWABLE_INSTRUCTORS, eval);
            // send results viewable notification to owner if protected, or all if not
            sendViewableEmail(evaluationId, jobType, EvalConstants.SHARING_PRIVATE.equals(eval.getResultsSharing()) );

        } else if (EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS.equals(jobType)) {
            commonLogic.registerEntityEvent(EVENT_EVAL_VIEWABLE_STUDENTS, eval);
            // send results viewable notification to owner if protected, or all if not
            sendViewableEmail(evaluationId, jobType, EvalConstants.SHARING_PRIVATE.equals(eval.getResultsSharing()) );
        }

    }





    // PRIVATE METHODS


	/**
     * @param evaluationId
     * @return
     */
    protected EvalEvaluation getEvaluationOrFail(Long evaluationId) {
        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        if (eval == null) {
            throw new IllegalArgumentException("Cannot find evaluation for invalid evaluationId: " + evaluationId);
        }
        return eval;
    }


    /**
     * Handles the processing of new evaluation only if not in partial state
     * 
     * @param eval
     * @throws IllegalStateException if eval is in partial state
     */
    protected void processNewEvaluation(EvalEvaluation eval) {
        String state = evaluationService.returnAndFixEvalState(eval, false);
        if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(state)) {
            throw new IllegalStateException("Cannot process new evaluation that is in partial state, state must be after partial");
        }

        // EVALSYS-1236
        // send the instructors a created e-mail, even if they are not allowed to edit it, because instAddItemNum is 0
        // and instructorOpt is "Required" (which it is hard-coded at this point to be "Required")
        boolean forceSendCreatedNotification = (Boolean) this.settings.get(EvalSettings.CONSOLIDATED_FORCE_SEND_CREATED_EMAIL);
        // send created email if instructor can add questions or opt-in or opt-out
        Integer instAddItemsNum = (Integer) settings.get(EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER);
        if (instAddItemsNum == null) instAddItemsNum = 0;
        if ( instAddItemsNum > 0 || 
                !eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_REQUIRED) ||
                forceSendCreatedNotification) {
            // http://bugs.sakaiproject.org/jira/browse/EVALSYS-507
            int timeToWaitSecs = 300;
            Integer ttws = (Integer) settings.get(EvalSettings.EVALUATION_TIME_TO_WAIT_SECS);
            if (ttws != null && ttws > 0) {
                timeToWaitSecs = ttws;
            }
            /*
             * Note: email should NOT be sent at this point, so we
             * schedule email for EvalConstants.EVALUATION_TIME_TO_WAIT_MINS minutes from now, 
             * giving instructor time to delete the evaluation and its notification
             */
            long runAt = new Date().getTime() + (1000 * timeToWaitSecs);
            scheduleJob(eval.getId(), new Date(runAt), EvalConstants.JOB_TYPE_CREATED);
        }
        scheduleJob(eval.getId(), eval.getStartDate(), EvalConstants.JOB_TYPE_ACTIVE);
    }


    /**
     * Remove all scheduled jobs related to this evaluation
     * @param evaluationId
     */
    protected void removeScheduledInvocations(Long evaluationId) {
        // if the eval is already gone then we are not concerned with a security check
        if (evaluationService.checkEvaluationExists(evaluationId)) {
            // check perms if this evaluation exists
            String userId = commonLogic.getAdminUserId(); // commonLogic.getCurrentUserId(); // use admin for this 
            if (! evaluationService.canControlEvaluation(userId, evaluationId)) {
                throw new SecurityException("User ("+userId+") not allowed to remove scheduled jobs for evaluation: " + evaluationId);
            }
        }

        // TODO be selective based on the state of the EvalEvaluation when deleted
        for( String JOB_TYPES1 : JOB_TYPES )
        {
            deleteInvocation( evaluationId, JOB_TYPES1 );
        }
    }


    /**
     * Handle a change in the evaluation which may effect scheduled events
     * @param eval
     * FIXME - this needs to be able to correctly handle the cases where due/stop/view dates are null
     */
    protected void processEvaluationChange(EvalEvaluation eval) {
        // make sure the state is up to date
        String state = evaluationService.returnAndFixEvalState(eval, false);
        if (EvalConstants.EVALUATION_STATE_UNKNOWN.equals(state)) {
            LOG.warn(this + ".processEvaluationChange(Long " + eval.getId().toString() + ") for "
                    + eval.getTitle() + ". Evaluation in UNKNOWN state");
            throw new RuntimeException("Evaluation '" + eval.getTitle() + "' in UNKNOWN state");
        }
        
        Boolean syncGroupAssignments = (Boolean) this.settings.get(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_STATE_CHANGE);
        if (syncGroupAssignments == null) {
        	// use default value, false
        	syncGroupAssignments = false;
        }

        if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(state)) {
            // do nothing for partial state evals, they are not created yet and should have nothing scheduled

        } else if (EvalConstants.EVALUATION_STATE_DELETED.equals(eval.getState())) {
            // remove all remaining events for this eval
            removeScheduledInvocations(eval.getId());
            // do not sync assignments if new state is deleted
            syncGroupAssignments = false;

        } else if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(eval.getState())) {
            // make sure scheduleActive job invocation date matches EvalEvaluation start date
            checkInvocationDate(eval, EvalConstants.JOB_TYPE_ACTIVE, eval.getStartDate());

        } else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(eval.getState())) {
            // make sure a change in Reminder interval is handled
            removeScheduledReminder(eval.getId());
            if (eval.getReminderDaysInt() != 0) {
                scheduleReminder(eval.getId());
            }

            /*
             * make sure scheduleDue job invocation start date matches EvalEaluation due date and
             * moving the due date is reflected in reminder
             */
            checkInvocationDate(eval, EvalConstants.JOB_TYPE_DUE, eval.getDueDate());

        } else if (EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(eval.getState())) {
            // make sure scheduleClosed job invocation start date matches EvalEvaluation stop date
            checkInvocationDate(eval, EvalConstants.JOB_TYPE_CLOSED, eval.getStopDate());

        } else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(eval.getState())) {
            // make sure scheduleView job invocation start date matches EvalEvaluation view date
            checkInvocationDate(eval, EvalConstants.JOB_TYPE_VIEWABLE, eval.getViewDate());

            // make sure scheduleView By Instructors job invocation start date matches EvalEvaluation instructor's date
            if (eval.getInstructorViewResults()) {
                checkInvocationDate(eval, EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS, eval.getInstructorsDate());
            } else {
                // not allowed so remove the job
                EvalScheduledJob[] jobs = commonLogic.findScheduledJobs(eval.getId(), EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS);
                for (EvalScheduledJob evalScheduledJob : jobs) {
                    commonLogic.deleteScheduledJob(evalScheduledJob.uuid);               
                }
            }

            // make sure scheduleView By Students job invocation start date matches EvalEvaluation student's date
            if (eval.getStudentViewResults()) {
                checkInvocationDate(eval, EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS, eval.getStudentsDate());
            } else {
                // not allowed so remove the job
                EvalScheduledJob[] jobs = commonLogic.findScheduledJobs(eval.getId(), EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS);
                for (EvalScheduledJob evalScheduledJob : jobs) {
                    commonLogic.deleteScheduledJob(evalScheduledJob.uuid);               
                }            
            }
            // do not sync assignments if new state is closed
            syncGroupAssignments = false;

        } else if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(eval.getState())) {
            // do not sync assignments if new state is viewable
            syncGroupAssignments = false;
        }
        
        if (syncGroupAssignments) {
        	this.evaluationSetupService.synchronizeUserAssignments(eval.getId(), null);
        }
    }

    /**
     * Delete the EvalScheduledInvocation identified by EvalEvaluation id and jobType, if found
     * 
     * @param evaluationId
     * @param jobType the job type from constants
     */
    protected void deleteInvocation(Long evaluationId, String jobType) {
        if (evaluationId == null || jobType == null) {
            throw new IllegalArgumentException("Invalid call to deleteInvocation, cannot have null evalId or jobType");
        }
        EvalScheduledJob[] jobs = commonLogic.findScheduledJobs(evaluationId, jobType);
        for( EvalScheduledJob job : jobs )
        {
            commonLogic.deleteScheduledJob( job.uuid );
        }
    }

    /**
     * Compare the date when a job will be invoked with the EvalEvaluation date to see if the job
     * needs to be rescheduled.
     * 
     * @param eval
     *           the EvalEvaluation
     * @param jobType
     *           the type of job (refer to EvalConstants)
     * @param correctDate
     *           the date when the job should be invoked
     */
    protected void checkInvocationDate(EvalEvaluation eval, String jobType, Date correctDate) {

        if (eval == null || jobType == null) {
            // FIXME - this is dangerous as there is no indication that this method did nothing, should be a failure -AZ
            LOG.warn("checkInvocationDate(eval ("+eval+") or jobType ("+jobType+") are null, cannot proceed with check");
            return;
        }

        if (correctDate == null) {
            // FIXME this can happen sometimes (any date other than startdate can be null), not sure how you may want to handle it -AZ
            LOG.warn("checkInvocationDate(eval=" + eval.getId() + ", jobType=" + jobType + " :: null correctDate, cannot proceed with check");
            return;
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("EvalJobLogicImpl.checkInvocationDate(" + eval.getId() + "," + jobType + "," + correctDate);
        }

        // reminders are treated in processEvaluationChange() EvalConstants.EVALUATION_STATE_ACTIVE
        if (EvalConstants.JOB_TYPE_REMINDER.equals(jobType))
            return;

        // get the jobs for this eval
        EvalScheduledJob[] jobs = commonLogic.findScheduledJobs(eval.getId(), jobType);

        // if there are no invocations, return
        if (jobs.length == 0) {
            // FIXME why return here? if no job was found should we not create one? -AZ
        } else {
            // we expect one delayed invocation matching componentId and opaqueContext so remove any extras
            cleanupExtraJobs(jobs);

            EvalScheduledJob job = jobs[0];

            // if the dates differ
            if (job.date.compareTo(correctDate) != 0) {
                // remove the old invocation
                commonLogic.deleteScheduledJob(job.uuid);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("EvalJobLogicImpl.checkInvocationDate remove the old invocation "
                            + job.uuid + "," + job.contextId + "," + job.date);
                }

                // and schedule a new invocation
                String newJobId = commonLogic.createScheduledJob(correctDate, eval.getId(), jobType);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("EvalJobLogicImpl.checkInvocationDate and schedule a new invocation: "
                            + newJobId + ", date=" + correctDate + "," + eval.getId() + "," + jobType + ")");
                }

                // the due date was changed, so reminder might need to be removed
                if (EvalConstants.JOB_TYPE_DUE.equals(jobType)) {
                    fixReminder(eval.getId());
                }
            }
        }
    }


    /**
     * Will get rid of any extra jobs that were returned for a single search,
     * we should normally only get one job back so this will remove the extras
     * 
     * @param jobs
     */
    private void cleanupExtraJobs(EvalScheduledJob[] jobs) {
        if (jobs.length > 1) {
            for (int i = 0; i < jobs.length; i++) {
                if (i > 0) {
                    commonLogic.deleteScheduledJob(jobs[i].uuid);
                }
            }
        }
    }

    /**
     * If there is a reminder then remove reminder if the due date now comes before the reminder or 
     * reminder days was changed to 0, otherwise do nothing,
     * updated to also handle the special 24h case
     * 
     * @param evaluationId the unique id of an {@link EvalEvaluation}
     */
    protected void fixReminder(Long evaluationId) {
        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);

        EvalScheduledJob[] jobs = commonLogic.findScheduledJobs(evaluationId, EvalConstants.JOB_TYPE_REMINDER);
        if (jobs.length > 0) {
            cleanupExtraJobs(jobs);
            EvalScheduledJob job = jobs[0];

            Date reminderAt = job.date;
            int reminderDays = eval.getReminderDaysInt();
            if (reminderDays == 0 
                    || reminderAt.after(eval.getSafeDueDate())) {
                // remove reminder
                commonLogic.deleteScheduledJob(job.uuid);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("EvalJobLogicImpl.fixReminders remove reminder after the due date "
                            + job.uuid + "," + job.contextId + "," + job.date);
                }
            } else {
                // 24 hour special case and normal case (we just trash the existing job and remake it to be safe)
                if (eval.getDueDate() != null) {
                    // delete the existing job
                    commonLogic.deleteScheduledJob(job.uuid);
                    // schedule a new one
                    scheduleReminder(evaluationId);
                }
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("EvalJobLogicImpl.fixReminders could not find any reminders for eval: " + evaluationId);
            }
        }
    }

    /**
     * Remove the EvalScheduledInvocation for an eval reminder,
     * this will remove any scheduled reminder jobs for the given evaluation id
     * 
     * @param evaluationId the EvalEvaluation id
     */
    protected void removeScheduledReminder(Long evaluationId) {
        if (evaluationId == null) {
            LOG.warn("removeScheduledReminder(evaluationId is null, cannot proceed");
            return; // FIXME throw exceptions or AT LEAST log warnings here
        }

        String userId = commonLogic.getAdminUserId(); // commonLogic.getCurrentUserId(); // use admin for this 
        if (evaluationService.canControlEvaluation(userId, evaluationId)) {
            deleteInvocation(evaluationId, EvalConstants.JOB_TYPE_REMINDER);
        }
    }

    /**
     * Schedule a job using the ScheduledInvocationManager.</br> "When" is specified by runDate,
     * "what" by componentId, and "what to do" by opaqueContext. OpaqueContext contains an
     * EvalEvaluationId and a jobType from EvalConstants, which is used to keep track of pending jobs
     * and reschedule or remove jobs when necessary.
     * 
     * @param evaluationId
     *           the id of an EvalEvaluation
     * @param runDate
     *           the Date when the command should be invoked
     * @param jobType
     *           the type of job, from EvalConstants
     */
    protected void scheduleJob(Long evaluationId, Date runDate, String jobType) {
        if (evaluationId == null || runDate == null || jobType == null) {
            throw new IllegalArgumentException("null arguments for scheduleJob(" + evaluationId + "," + runDate + "," + jobType + ")");
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("EvalJobLogicImpl.scheduleJob(" + evaluationId + "," + runDate + "," + jobType + ")");
        }

        String newJobId = commonLogic.createScheduledJob(runDate, evaluationId, jobType);

        if (LOG.isDebugEnabled()) {
            LOG.debug("EvalJobLogicImpl.scheduleJob scheduledInvocationManager.createDelayedInvocation("
                    + runDate + "," + evaluationId + "," + jobType + "), new jobid = " + newJobId);
        }
    }

    /**
     * Schedule reminders to be run under the ScheduledInvocationManager.</br> If under these
     * conditions there is time to send a reminder before the due date, schedule one.
     * <ul>
     * <li>Schedule a reminder, if necessary (i.e., one is not already scheduled for another group,
     * resulting in notification of all groups), when there is a late notification after opt-in at
     * the start date in EvalEvaluationSetupServiceImpl.saveAssignGroup() - a special case,</li>
     * <li>Schedule a reminder when EvalJobLogicImpl.jobAction runs JOB_TYPE_ACTIVE - the 1st
     * reminder, scheduled for start date + reminder interval,</li>
     * <li>Schedule a reminder when EvalJobLogicImpl.jobAcion runs JOB_TYPE_REMINDER - the next
     * reminder, scheduled when a reminder job has run and there is time remaining before the due
     * date to schedule another reminder,</li>
     * <li>Schedule a reminder when EvalJobLogicImpl.processEvaluationChange is called while
     * EVAL_STATE_ACTIVE - i.e., when someone edited the settings of an active evaluation, possibly
     * making the due date earlier or later and affecting an already scheduled reminder or one that
     * now needs to be scheduled.</li>
     * </ul>
     * 
     * @param evaluationId
     *           the EvalEvaluation id
     */
    protected void scheduleReminder(Long evaluationId) {
        // we're depending on reminders going out on time
        if (evaluationId == null) {
            throw new RuntimeException("Exception scheduling reminder: " + this + ".scheduleReminder(): null evaluationId");
        }

        EvalEvaluation eval = getEvaluationOrFail(evaluationId);
        String state = evaluationService.returnAndFixEvalState(eval, false);

        long scheduleAt = getReminderTime(eval);

        if ( scheduleAt != 0 
                && EvalConstants.EVALUATION_STATE_ACTIVE.equals(state) ) {
            commonLogic.createScheduledJob(new Date(scheduleAt), evaluationId, EvalConstants.JOB_TYPE_REMINDER);
            LOG.info("Scheduling a reminder ("+new Date(scheduleAt)+") for the evaluation ("+eval.getTitle()+") ["+evaluationId+"]");

            if (LOG.isDebugEnabled()) {
                LOG.debug("EvalJobLogicImpl.scheduleReminders(" + evaluationId
                        + ") - scheduledInvocationManager.createDelayedInvocation( "
                        + new Date(scheduleAt) + "," + evaluationId + "," + EvalConstants.JOB_TYPE_REMINDER);
            }
        }
    }

    /**
     * Get the time (in ms) to use in scheduling the next reminder for this evaluation, 
     * which is typically the first reminder interval after the start date that is in the future
     * 
     * @param eval the EvalEvaluation to get the next reminder time for
     * @return the value to use in setting date of reminder OR 0 if no reminder should be scheduled
     */
    protected long getReminderTime(EvalEvaluation eval) {
        if (eval == null) {
            throw new IllegalArgumentException("eval cannot be null");
        }
        long reminderTime = 0;

        long startTime = eval.getStartDate().getTime();
        long now = System.currentTimeMillis();
        // due date is one week in the future if it is not set
        long dueTime = eval.getSafeDueDate().getTime();
        long available = dueTime - now;

        int reminderDays = eval.getReminderDaysInt();
        if (reminderDays > 0) {
            long interval = (1000l * 60l * 60l * 24l) * (long)reminderDays;
            // we'll say the future starts in 3 minutes, this is a user buffer
            if (interval != 0 
                    && available > interval) {
                reminderTime = startTime + interval;
                while (reminderTime < now + (1000l * 60l * 3l)) {
                    reminderTime = reminderTime + interval;
                }
            }
        } else if (reminderDays == -1) {
            // NOTE: this is the special 24 hours before the end of the eval case
            boolean dueDateSet = eval.getDueDate() != null;
            // only send 24h reminder if the eval is due to end and not continuous
            if (dueDateSet) {
                // set the reminder time to the dueTime - 24 hours
                reminderTime = dueTime - (1000l * 60l * 60l * 24l);
                if (reminderTime < now) {
                    // the reminder is in the past, just warn and be done
                    LOG.info("Trying to schedule 24h reminder in the past ("+new Date(reminderTime)+"), no reminder will be sent for eval ("+eval.getTitle()+") ["+eval.getId()+"]");
                    reminderTime = 0;
                } else {
                    LOG.info("Scheduling a reminder ("+new Date(reminderTime)+") for 24h before the end ("+new Date(dueTime)+") of the evaluation ("+eval.getTitle()+") ["+eval.getId()+"]");
                }
            }
        } else if (reminderDays == -2) {
            // NOTE: special case for testing
            reminderTime = System.currentTimeMillis() + (1000l * 60l * 5l); // reminders every 5 minutes
            LOG.warn("REMINDERS TESTING ONLY (-2): Scheduling a reminder in the near future ("+new Date(reminderTime)+") for testing: due date ("+new Date(dueTime)+") of the evaluation ("+eval.getTitle()+") ["+eval.getId()+"]");
        }
        return reminderTime;
    }


    // TODO recommend removing these send email methods and calling the methods in emailsLogic directly

    /**
     * Send email to evaluation participants that an evaluation is available for taking by clicking
     * the contained URL
     * 
     * @param evalId
     *           the EvalEvaluation id
     */
    protected void sendAvailableEmail(Long evalId) {
        // For now, we always want to include the evaluatees in the evaluationSetupService
        boolean includeEvaluatees = true;
        String[] sentMessages = emails.sendEvalAvailableNotifications(evalId, includeEvaluatees);
        if (LOG.isDebugEnabled())
        {
            LOG.debug("EvalJobLogicImpl.sendAvailableEmail(" + evalId + ")" + " sentMessages: " + ArrayUtils.arrayToString(sentMessages));
        }
    }

    /**
     * Send email that an evaluation has been created</br>
     * 
     * @param evalId the EvalEvaluation id
     */
    protected void sendCreatedEmail(Long evalId) {
        boolean includeOwner = true;
        String[] sentMessages = emails.sendEvalCreatedNotifications(evalId, includeOwner);
        if (LOG.isDebugEnabled())
        {
            LOG.debug("EvalJobLogicImpl.sendCreatedEmail(" + evalId + ")" + " sentMessages: " + ArrayUtils.arrayToString(sentMessages));
        }
    }

    /**
     * Send a reminder that an evaluation is available to those who have not responded
     * 
     * @param evaluationId the EvalEvaluation id
     */
    protected void sendReminderEmail(Long evaluationId) {
        EvalEvaluation eval = getEvaluationOrFail(evaluationId);
        if (eval.getState().equals(EvalConstants.EVALUATION_STATE_ACTIVE)
                && eval.getReminderDaysInt() != 0) {
            String[] sentMessages = emails.sendEvalReminderNotifications(evaluationId, EvalConstants.EVAL_INCLUDE_NONTAKERS);
            if (LOG.isDebugEnabled()) {
                LOG.debug("sendReminderEmail(" + evaluationId + ")" + " sentMessages: " + ArrayUtils.arrayToString(sentMessages));
            }
            boolean saveWithoutSubmit = (Boolean) settings.get(EvalSettings.STUDENT_SAVE_WITHOUT_SUBMIT);
            if (saveWithoutSubmit) {
                String[] sentMessagesInProgress = emails.sendEvalReminderNotifications(evaluationId, EvalConstants.EVAL_INCLUDE_IN_PROGRESS);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("sendReminderEmail(" + evaluationId + ")" + " sentMessagesInProgress: " + ArrayUtils.arrayToString(sentMessagesInProgress));
                }
            } else {
                LOG.debug("sendReminderEmail(" + evaluationId + "), No in progress reminders sent (saveWithoutSubmit is disabled)");
            }
        } else {
            LOG.debug("sendReminderEmail(" + evaluationId + "), no reminder sent (state="+eval.getState()+", reminderDays="+eval.getReminderDaysInt()+")");
        }
    }

    /**
     * Send email that the results of an evaluation may be viewed now.</br> Notification may be sent
     * to owner only, instructors and students together or separately.
     * 
     * @param evalId
     *           the EvalEvaluation id
     * @param jobType
     *           the job type fom EvalConstants
     * @param resultsPrivate
     */
    protected void sendViewableEmail(Long evalId, String jobType, Boolean resultsPrivate) {
        /*
         * TODO when booleans below are set dynamically, replace the use of job type to distinguish
         * recipients with the setting of these parameters before calling
         * emails.sendEvalResultsNotifications(). Then one job type JOB_TYPE_VIEWABLE can be scheduled
         * as needed.
         */
        boolean includeEvaluatees = true;
        boolean includeAdmins = true;
        // if results are protected, only send notification to owner
        if (resultsPrivate) {
            includeEvaluatees = false;
            includeAdmins = false;
        }
        emails.sendEvalResultsNotifications(evalId, includeEvaluatees, includeAdmins, jobType);
        if (LOG.isDebugEnabled()) {
            LOG.debug("EvalJobLogicImpl.sendViewableEmail(" + evalId + "," + jobType
                    + ", resultsPrivate " + resultsPrivate + ")");
        }
    }

    /**
     * Check if a job of a given type for a given evaluation is scheduled. At most one job of a given
     * type is scheduled per evaluation.
     * 
     * @param evaluationId
     *           the EvalEvaluation id
     * @param jobType
     * @return
     */
    /*****************
   protected boolean isJobTypeScheduled(Long evaluationId, String jobType) {
      if (evaluationId == null || jobType == null) {
         log.warn(this + ".isJobTypeScheduled called with null parameter(s).");
         return false;
      }
      if (!isValidJobType(jobType)) {
         log.warn(this + ".isJobTypeScheduled called with invalid jobType '" + jobType + "'.");
         return false;
      }
      // make sure there is an evaluation
      EvalEvaluation eval = null;
      try {
         eval = evaluationService.getEvaluationById(evaluationId);
      } catch (Exception e) {
         log.warn(this + ".isJobTypeScheduled evaluation id '" + evaluationId + "' " + e);
         return false;
      }
      if (eval == null) {
         log.warn(this + ".isJobTypeScheduled no evaluation having id '" + evaluationId
               + "' was found.");
         return false;
      }
      boolean isScheduled = false;
      DelayedInvocation[] invocations;
      String opaqueContext = evaluationId.toString() + SEPARATOR + jobType;
      invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
      if (invocations != null) {
         if (invocations.length == 1) {
            isScheduled = true;
         } else if (invocations.length > 1) {
            log.warn(this + ".isJobTypeScheduled(" + opaqueContext
                  + ") multiple invocations were found.");
            isScheduled = true;
         }
      }
      return isScheduled;
   }
     ****************/

}
