/**
 * $Id$
 * $URL$
 * EvalJobLogicImpl.java - evaluation - Aug 28, 2007 10:07:31 AM - rwellis
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.scheduling;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.time.api.TimeService;

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

   protected static Log log = LogFactory.getLog(EvalJobLogicImpl.class);

   // the component scheduled by the ScheduledInvocationManager
   protected final String COMPONENT_ID = "org.sakaiproject.evaluation.logic.externals.EvalScheduledInvocation";

   protected final String SEPARATOR = "/";            // max-32:12345678901234567890123456789012
   protected final String EVENT_EVAL_VIEWABLE_INSTRUCTORS =    "eval.state.viewable.inst";
   protected final String EVENT_EVAL_VIEWABLE_STUDENTS =       "eval.state.viewable.stud";

   protected EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   protected EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   protected EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }

   protected EvalEmailsLogic emails;
   public void setEmails(EvalEmailsLogic emails) {
      this.emails = emails;
   }

   // FIXME - remove dependency on Sakai API
   protected ScheduledInvocationManager scheduledInvocationManager;
   public void setScheduledInvocationManager(ScheduledInvocationManager scheduledInvocationManager) {
      this.scheduledInvocationManager = scheduledInvocationManager;
   }

   // FIXME - remove dependency on Sakai API
   protected TimeService timeService;
   public void setTimeService(TimeService timeService) {
      this.timeService = timeService;
   }

   public void init() {
      log.debug("EvalJobLogicImpl.init()");
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
         for (int i = 0; i < JOB_TYPES.length; i++) {
            if (jobType.equals(JOB_TYPES[i])) {
               isValid = true;
               break;
            }
         }
      }
      return isValid;
   }

   public void processEvaluationStateChange(Long evaluationId, String actionState) {
      if (log.isDebugEnabled())
         log.debug("EvalJobLogicImpl.processEvaluationStateChange(" + evaluationId + ") and state=" + actionState);
      if (evaluationId == null || actionState == null) {
         throw new NullPointerException("both evaluationId ("+evaluationId+") and actionState ("+actionState+") must be set");
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
         throw new NullPointerException("both evaluationId ("+evaluationId+") and jobType ("+jobType+") must be set");
      }

      /*
       * Note: If interactive response time is too slow waiting for mail to be sent, sending mail
       * could be done as another type of job run by the scheduler in a separate thread.
       */

      EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
      if (eval == null) {
         // this eval was purged out so wipe out all the jobs
         log.info("Could not find evaluation ("+evaluationId+") for jobAction ("+jobType+"), " +
         		"purging out all jobs related to this evaluation...");
         removeScheduledInvocations(evaluationId);
      }

      // fix up EvalEvaluation state (also persists it)
      String evalState = evaluationService.returnAndFixEvalState(eval, true);
      Date now = new Date();

      // dispatch to send email and/or schedule jobs based on jobType
      if (EvalConstants.JOB_TYPE_CREATED.equals(jobType)) {
         // if opt-in, opt-out, or questions addable, notify instructors
         sendCreatedEmail(evaluationId);

      } else if (EvalConstants.JOB_TYPE_ACTIVE.equals(jobType)) {
         sendAvailableEmail(evaluationId);
         if (eval.getDueDate() != null) {
            // might not have a due date set
            scheduleJob(eval.getId(), eval.getDueDate(), EvalConstants.JOB_TYPE_DUE);
         }
         int reminderDays = eval.getReminderDays() == null ? 0 : eval.getReminderDays();
         if (reminderDays > 0) {
            scheduleReminder(eval.getId());
         }

      } else if (EvalConstants.JOB_TYPE_REMINDER.equals(jobType)) {
         int reminderDays = eval.getReminderDays() == null ? 0 : eval.getReminderDays();
         if (evalState.equals(EvalConstants.EVALUATION_STATE_ACTIVE)
               && reminderDays > 0) {
            if (eval.getDueDate() == null 
                  || eval.getDueDate().after(now)) {
               sendReminderEmail(evaluationId);
               scheduleReminder(evaluationId);
            }
         }

      } else if (EvalConstants.JOB_TYPE_DUE.equals(jobType)) {
         if (log.isDebugEnabled())
            log.debug("EvalJobLogicImpl.jobAction scheduleJob(" + eval.getId() + ","
                  + eval.getStopDate() + "," + EvalConstants.JOB_TYPE_CLOSED + ")");
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
         if (eval.getViewDate() != null) {
            scheduleJob(eval.getId(), eval.getViewDate(), EvalConstants.JOB_TYPE_VIEWABLE);
            if (! EvalConstants.SHARING_PRIVATE.equals(eval.getResultsSharing()) ) {
               if (eval.getInstructorsDate() != null) {
                  Date instructorViewDate = eval.getInstructorsDate();
                  // schedule results viewable by instructors notification
                  scheduleJob(eval.getId(), instructorViewDate,
                        EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS);
               }
               if (eval.getStudentsDate() != null) {
                  Date studentViewDate = eval.getStudentsDate();
                  // schedule results viewable by students notification
                  scheduleJob(eval.getId(), studentViewDate,
                        EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS);
               }
            }
         } else {
            // viewable now
            scheduleJob(eval.getId(), now, EvalConstants.JOB_TYPE_VIEWABLE);
         }

      } else if (EvalConstants.JOB_TYPE_VIEWABLE.equals(jobType)) {
         // send results viewable notification to owner if protected, or all if not
         sendViewableEmail(evaluationId, jobType, EvalConstants.SHARING_PRIVATE.equals(eval.getResultsSharing()) );

      } else if (EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS.equals(jobType)) {
         externalLogic.registerEntityEvent(EVENT_EVAL_VIEWABLE_INSTRUCTORS, eval);
         // send results viewable notification to owner if protected, or all if not
         sendViewableEmail(evaluationId, jobType, EvalConstants.SHARING_PRIVATE.equals(eval.getResultsSharing()) );

      } else if (EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS.equals(jobType)) {
         externalLogic.registerEntityEvent(EVENT_EVAL_VIEWABLE_STUDENTS, eval);
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
    * Handles the processing of new evaluationSetupService
    * @param eval
    */
   protected void processNewEvaluation(EvalEvaluation eval) {
      // send created email if instructor can add questions or opt-in or opt-out
      Integer instAddItemsNum = (Integer) settings.get(EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER);
      if (instAddItemsNum == null) instAddItemsNum = 0;
      if ( instAddItemsNum > 0 || 
            !eval.getInstructorOpt().equals(EvalConstants.INSTRUCTOR_REQUIRED)) {
         /*
          * Note: email should NOT be sent at this point, so we
          * schedule email for EvalConstants.EVALUATION_TIME_TO_WAIT_MINS minutes from now, 
          * giving instructor time to delete the evaluation and its notification
          */
         long runAt = new Date().getTime() + (1000 * 60 * EvalConstants.EVALUATION_TIME_TO_WAIT_MINS);
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
         String userId = externalLogic.getCurrentUserId();
         if (! evaluationService.canControlEvaluation(userId, evaluationId)) {
            throw new SecurityException("User ("+userId+") not allowed to remove sceduled jobs for evaluation: " + evaluationId);
         }
      }

      // TODO be selective based on the state of the EvalEvaluation when deleted
      String opaqueContext = null;
      String prefix = evaluationId.toString() + SEPARATOR;
      for (int i = 0; i < JOB_TYPES.length; i++) {
         opaqueContext = prefix + JOB_TYPES[i];
         deleteInvocation(opaqueContext);
      }
   }


   /**
    * Handle a change in the evaluation which may effect scheduled events
    * @param eval
    * FIXME - this needs to be able to correctly handle the cases where due/stop/view dates are null
    */
   protected void processEvaluationChange(EvalEvaluation eval) {
      // make sure the state is up to date
      String state = evaluationService.updateEvaluationState(eval.getId());
      if (EvalConstants.EVALUATION_STATE_UNKNOWN.equals(state)) {
         log.warn(this + ".processEvaluationChange(Long " + eval.getId().toString() + ") for "
               + eval.getTitle() + ". Evaluation in UNKNOWN state");
         throw new RuntimeException("Evaluation '" + eval.getTitle() + "' in UNKNOWN state");
      }

      if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(eval.getState())) {
         // make sure scheduleActive job invocation date matches EvalEvaluation start date
         checkInvocationDate(eval, EvalConstants.JOB_TYPE_ACTIVE, eval.getStartDate());

      } else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(eval.getState())) {
         // make sure a change in Reminder interval is handled
         removeScheduledReminder(eval.getId());
         if (eval.getReminderDays().intValue() != 0) {
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

         // make sure scheduleView By Instructors job invocation start date matches
         // EvalEvaluation instructor's date
         checkInvocationDate(eval, EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS, 
               eval.getInstructorsDate());

         // make sure scheduleView By Students job invocation start date matches EvalEvaluation
         // student's date
         checkInvocationDate(eval, EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS, eval
               .getStudentsDate());
      }
   }

   /**
    * Delete the EvalScheduledInvocation identified by EvalEvaluation id and jobType, if found
    * 
    * @param opaqueContext
    *           the EvalEvaluation id/jobType
    */
   protected void deleteInvocation(String opaqueContext) {
      if (opaqueContext == null || opaqueContext == "") {
         return; // FIXME - silently exiting is not so great....
      }
      DelayedInvocation[] invocations;
      invocations = scheduledInvocationManager.findDelayedInvocations(COMPONENT_ID, opaqueContext);
      if (invocations != null) {
         if (invocations.length == 1) {
            scheduledInvocationManager.deleteDelayedInvocation(invocations[0].uuid);
         } else if (invocations.length > 1) {
            log.warn(this + ".deleteInvocation(" + opaqueContext
                  + ") multiple invocations were found.");
            // duplicate opaqueContext records would need to be deleted from db using sql
            throw new RuntimeException(opaqueContext
                  + " multiple invocations were found; duplicates need to be removed.");
         }
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

      if (eval == null || jobType == null || correctDate == null)
         return;

      if (log.isDebugEnabled())
         log.debug("EvalJobLogicImpl.checkInvocationDate(" + eval.getId() + "," + jobType + ","
               + correctDate);

      // reminders are treated in processEvaluationChange() EvalConstants.EVALUATION_STATE_ACTIVE
      if (EvalConstants.JOB_TYPE_REMINDER.equals(jobType))
         return;

      // get the delayed invocation, a pea with .Date Date
      String id = eval.getId().toString();
      String opaqueContext = id + SEPARATOR + jobType;
      DelayedInvocation[] invocations = scheduledInvocationManager.findDelayedInvocations(
            COMPONENT_ID, opaqueContext);

      // if there are no invocations, return
      if (invocations == null || invocations.length == 0) {
         return;
      } else if (invocations.length == 1) {
         // we expect at most one delayed invocation matching componentId and opaqueContext

         // if the dates differ
         if (invocations[0].date.compareTo(correctDate) != 0) {

            // remove the old invocation
            scheduledInvocationManager.deleteDelayedInvocation(invocations[0].uuid);
            if (log.isDebugEnabled())
               log.debug("EvalJobLogicImpl.checkInvocationDate remove the old invocation "
                     + invocations[0].uuid + "," + invocations[0].contextId + ","
                     + invocations[0].date);

            // and schedule a new invocation
            scheduledInvocationManager.createDelayedInvocation(timeService.newTime(correctDate
                  .getTime()), COMPONENT_ID, opaqueContext);
            if (log.isDebugEnabled())
               log.debug("EvalJobLogicImpl.checkInvocationDate and schedule a new invocation "
                     + correctDate + "," + COMPONENT_ID + "," + opaqueContext + ")");

            // the due date was changed, so reminder might need to be removed
            if (EvalConstants.JOB_TYPE_DUE.equals(jobType)) {
               fixReminder(eval.getId());
            }
         }
      } else {
         log.warn(this + ".checkInvocationDate: multiple delayed invocations of componentId '"
               + COMPONENT_ID + "', opaqueContext '" + opaqueContext + "'");
      }
   }

   /**
    * Remove reminder if the due date now comes before the reminder or 
    * reminder days was changed to 0
    * 
    * @param evalId
    *           the EvalEvaluation id
    */
   protected void fixReminder(Long evaluationId) {
      EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
      String opaqueContext = evaluationId.toString() + SEPARATOR + EvalConstants.JOB_TYPE_REMINDER;
      DelayedInvocation[] invocations = scheduledInvocationManager.findDelayedInvocations(
            COMPONENT_ID, opaqueContext);
      if (invocations != null && invocations.length == 1) {
         DelayedInvocation reminder = invocations[0];
         Date reminderAt = reminder.date;
         if (eval.getReminderDays().intValue() == 0 || reminderAt.after(eval.getDueDate())) {
            // remove reminder
            scheduledInvocationManager.deleteDelayedInvocation(reminder.uuid);
            if (log.isDebugEnabled())
               log.debug("EvalJobLogicImpl.fixReminders remove reminder after the due date "
                     + reminder.uuid + "," + reminder.contextId + "," + reminder.date);
         }
      }
   }

   /**
    * Remove the EvalScheduledInvocation for an EvalEvaluation reminder
    * 
    * @param evaluationId
    *           the EvalEvaluation id
    */
   protected void removeScheduledReminder(Long evaluationId) {
      if (evaluationId == null)
         return; // FIXME throw exceptions or AT LEAST log warnings here
      String userId = externalLogic.getCurrentUserId();
      if (evaluationService.canControlEvaluation(userId, evaluationId)) {
         // DelayedInvocation[] invocations = null;
         String opaqueContext = evaluationId.toString() + SEPARATOR
         + EvalConstants.JOB_TYPE_REMINDER;
         deleteInvocation(opaqueContext);
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
         if (log.isErrorEnabled())
            log.error(this + ".scheduleJob null parameter");
         // TODO: throw exception
         return;
      }
      if (log.isDebugEnabled())
         log.debug("EvalJobLogicImpl.scheduleJob(" + evaluationId + "," + runDate + "," + jobType + ")");
      try {
         String opaqueContext = evaluationId.toString() + SEPARATOR + jobType;
         scheduledInvocationManager.createDelayedInvocation(timeService.newTime(runDate.getTime()),
               COMPONENT_ID, opaqueContext);
         if (log.isDebugEnabled())
            log.debug("EvalJobLogicImpl.scheduleJob scheduledInvocationManager.createDelayedInvocation("
                  + timeService.newTime(runDate.getTime()) + ","
                  + COMPONENT_ID + "," + opaqueContext + ")");
      } catch (Exception e) {
         log.error(this + ".scheduleJob(" + evaluationId + "," + runDate.toString() + "," + jobType + ") " + e);
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
         log.error(this + ".scheduleReminder(): null evaluationId");
         throw new RuntimeException("Exception scheduling reminder: null evaluationId");
      }
      try {
         EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
         if (eval == null) {
            log.error(this + ".scheduleReminder(): null EvalEvaluation eval");
            throw new RuntimeException("Exception scheduling reminder for evaluation '"
                  + evaluationId + "': evaluation was not found");
         }
         String opaqueContext = evaluationId.toString() + SEPARATOR
         + EvalConstants.JOB_TYPE_REMINDER;
         long scheduleAt = getReminderTime(eval);
         if (scheduleAt != 0 && eval.getState().equals(EvalConstants.EVALUATION_STATE_ACTIVE)) {
            scheduledInvocationManager.createDelayedInvocation(timeService.newTime(scheduleAt),
                  COMPONENT_ID, opaqueContext);
            if (log.isDebugEnabled())
               log.debug("EvalJobLogicImpl.scheduleReminders(" + evaluationId
                     + ") - scheduledInvocationManager.createDelayedInvocation( "
                     + timeService.newTime(scheduleAt) + "," + COMPONENT_ID + "," + opaqueContext);
         }
      } catch (Exception e) {
         log.error(this + ".scheduleReminder(evaluationId '" + evaluationId + "'): " + e);
         throw new RuntimeException("Exception scheduling reminder for evaluation '" + evaluationId
               + "' " + e);
      }
   }

   /**
    * Get the time to use in scheduling a reminder, which is the first reminder interval after the
    * start date that is in the future
    * 
    * @param eval
    *           the EvalEvaluation
    * @return the value to use in setting date of reminder or 0 if no reminder should be scheduled
    */
   protected long getReminderTime(EvalEvaluation eval) {
      long reminderTime = 0;
      if (eval != null) {
         long startTime = eval.getStartDate().getTime();
         long dueTime = eval.getDueDate().getTime();
         long interval = 1000 * 60 * 60 * 24 * eval.getReminderDays().intValue();
         long now = new Date().getTime();
         long available = dueTime - now;
         // we'll say the future starts in 15 minutes
         if (interval != 0 && available > interval) {
            reminderTime = startTime + interval;
            while (reminderTime < now + (1000 * 60 * 15)) {
               reminderTime = reminderTime + interval;
            }
         }
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
      try {
         String[] sentMessages = emails.sendEvalAvailableNotifications(evalId, includeEvaluatees);
         if (log.isDebugEnabled())
            log.debug("EvalJobLogicImpl.sendAvailableEmail(" + evalId + ")" + " sentMessages: "
                  + sentMessages.toString());
      } catch (Exception e) {
         log.error(this + ".sendAvailableEmail(" + evalId + ")" + e);
      }
   }

   /**
    * Send email that an evaluation has been created</br>
    * 
    * @param evalId
    *           the EvalEvaluation id
    */
   protected void sendCreatedEmail(Long evalId) {
      boolean includeOwner = true;
      try {
         String[] sentMessages = emails.sendEvalCreatedNotifications(evalId, includeOwner);
         if (log.isDebugEnabled())
            log.debug("EvalJobLogicImpl.sendCreatedEmail(" + evalId + ")" + " sentMessages: "
                  + sentMessages.toString());
      } catch (Exception e) {
         log.error(this + ".sendCreatedEmail(" + evalId + ")" + e);
      }
   }

   /**
    * Send a reminder that an evaluation is available to those who have not responded
    * 
    * @param evalId
    *           the EvalEvaluation id
    */
   protected void sendReminderEmail(Long evalId) {
      try {
         EvalEvaluation eval = evaluationService.getEvaluationById(evalId);
         if (eval.getState().equals(EvalConstants.EVALUATION_STATE_ACTIVE)
               && eval.getReminderDays().intValue() != 0) {
            String includeConstant = EvalConstants.EVAL_INCLUDE_NONTAKERS;
            String[] sentMessages = emails.sendEvalReminderNotifications(evalId, includeConstant);
            if (log.isDebugEnabled())
               log.debug("EvalJobLogicImpl.sendReminderEmail(" + evalId + ")" + " sentMessages: "
                     + sentMessages.toString());
         }
      } catch (Exception e) {
         log.error(this + ".sendReminderEmail(" + evalId + ")" + e);
      }
   }

   /**
    * Send email that the results of an evaluation may be viewed now.</br> Notification may be sent
    * to owner only, instructors and students together or separately.
    * 
    * @param evalId
    *           the EvalEvaluation id
    * @param the
    *           job type fom EvalConstants
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
      if (log.isDebugEnabled()) {
         log.debug("EvalJobLogicImpl.sendViewableEmail(" + evalId + "," + jobType
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
