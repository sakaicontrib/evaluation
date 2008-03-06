/**
 * $Id$
 * $URL$
 * EvalBeanUtils.java - evaluation - Feb 21, 2008 11:06:08 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.beans;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.EvalUtils;


/**
 * Utils which depend on some of the basic eval beans<br/>
 * <b>NOTE:</b> These utils require other spring beans and thus this class must be injected,
 * attempting to access this without injecting it will cause failures
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalBeanUtils {

   private static Log log = LogFactory.getLog(EvalBeanUtils.class);

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic external) {
      this.externalLogic = external;
   }

   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }


   /**
    * Determines if evaluation results can be viewed based on the minimum count of responses for the system
    * and the inputs, also checks if user is an admin (they can always view results)
    * 
    * @param responsesCount the current number of responses for an evaluation
    * @param enrollmentsCount the count of enrollments (can be 0 if anonymous or unknown)
    * @return number of responses needed before viewing is allowed, 0 indicates viewable now
    */
   public int getResponsesNeededToViewForResponseRate(int responsesCount, int enrollmentsCount) {
      int responsesNeeded = 1;
      if ( externalLogic.isUserAdmin( externalLogic.getCurrentUserId() ) ) {
         responsesNeeded = 0;
      } else {
         int minResponses = ((Integer) settings.get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS)).intValue();
         responsesNeeded = minResponses - responsesCount;
         if (responsesCount >= enrollmentsCount) {
            // special check to make sure the cases where there is a very small enrollment count is still ok
            responsesNeeded = 0;
         }
         if (responsesNeeded < 0) {
            responsesNeeded = 0;
         }
      }
      return responsesNeeded;
   }

   /**
    * General check for admin/owner permissions,
    * this will check to see if the provided userId is an admin and
    * also check if they are equal to the provided ownerId
    * 
    * @param userId internal user id
    * @param ownerId an internal user id
    * @return true if this user is admin or matches the owner user id
    */
   public boolean checkUserPermission(String userId, String ownerId) {
      boolean allowed = false;
      if ( externalLogic.isUserAdmin(userId) ) {
         allowed = true;
      } else if ( ownerId.equals(userId) ) {
         allowed = true;
      }
      return allowed;
   }

   /**
    * Sets all the system defaults for this evaluation object
    * and ensures all required fields are correctly set,
    * use this whenever you create a new evaluation
    * 
    * @param eval an {@link EvalEvaluation} object (can be persisted or new)
    * @param evaluationType a type constant of EvalConstants#EVALUATION_TYPE_*,
    * if left null then {@link EvalConstants#EVALUATION_TYPE_EVALUATION} is used
    */
   public void setEvaluationDefaults(EvalEvaluation eval, String evaluationType) {

      // set the type to the default to ensure not null
      if (eval.getType() == null) {
         eval.setType(EvalConstants.EVALUATION_TYPE_EVALUATION);
      }

      // set to the supplied type if supplied and do any special settings if needed based on the type
      if (evaluationType != null) {
         eval.setType(evaluationType);
      }

      // only do these for new evals
      if (eval.getId() == null) {
         if (eval.getState() == null) {
            eval.setState(EvalConstants.EVALUATION_STATE_PARTIAL);
         }
      }

      // make sure the dates are set
      Calendar calendar = new GregorianCalendar();
      calendar.setTime( new Date() );
      if (eval.getStartDate() == null) {
         eval.setStartDate(calendar.getTime());
         log.debug("Setting start date to default of: " + eval.getStartDate());
      } else {
         calendar.setTime(eval.getStartDate());
      }

      calendar.add(Calendar.DATE, 1);
      if (eval.getDueDate() == null) {
         // default the due date to the end of the start date + 1 day
         Calendar cal = new GregorianCalendar();
         cal.setTime(calendar.getTime());
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         eval.setDueDate(cal.getTime());
         log.debug("Setting due date to default of: " + eval.getDueDate());
      } else {
         calendar.setTime(eval.getDueDate());
      }

      // assign stop date to equal due date for now
      if (eval.getStopDate() == null) {
         eval.setStopDate(eval.getDueDate());
         log.debug("Setting stop date to default of: " + eval.getStopDate());
      }

      // assign default view date
      calendar.add(Calendar.DATE, 1);
      if (eval.getViewDate() == null) {
         // default the view date to the today + 2
         eval.setViewDate(calendar.getTime());
         log.debug("Setting view date to default of: " + eval.getViewDate());
      }

      // results viewable settings
      Date studentsDate = null;
      Boolean studentsView = (Boolean) settings.get(EvalSettings.STUDENT_VIEW_RESULTS);
      eval.studentViewResults = studentsView;
      if (studentsView == null || studentsView) {
         eval.studentViewResults = true;
         studentsDate = eval.getViewDate();
      }
      if (eval.getStudentsDate() == null) {
         eval.setStudentsDate(studentsDate);
      }
      if (eval.getStudentsDate() == null) {
         eval.studentViewResults = false;
      }

      Date instructorsDate = null;
      Boolean instructorsView = (Boolean) settings.get(EvalSettings.STUDENT_VIEW_RESULTS);
      eval.instructorViewResults = instructorsView;
      if (instructorsView == null || instructorsView) {
         instructorsDate = eval.getViewDate();
      }
      if (eval.getInstructorsDate() == null) {
         eval.setInstructorsDate(instructorsDate);
      }
      if (eval.getInstructorsDate() == null) {
         eval.instructorViewResults = false;
      }

      if (eval.getResultsSharing() == null) {
         eval.setResultsSharing( EvalConstants.SHARING_VISIBLE );
      }
      if (EvalConstants.SHARING_PRIVATE.equals(eval.getResultsSharing())) {
         eval.studentViewResults = false;
         eval.instructorViewResults = false;
      } else if (EvalConstants.SHARING_PUBLIC.equals(eval.getResultsSharing())) {
         eval.studentViewResults = true;
         eval.instructorViewResults = true;
         studentsDate = eval.getViewDate();
         eval.setStudentsDate(studentsDate);
         instructorsDate = eval.getViewDate();
         eval.setInstructorsDate(instructorsDate);
      }

      // student completion settings
      if (eval.getBlankResponsesAllowed() == null) {
         Boolean blankAllowed = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
         if (blankAllowed == null) { blankAllowed = false; }
         eval.setBlankResponsesAllowed(blankAllowed);
      }

      if (eval.getModifyResponsesAllowed() == null) {
         Boolean modifyAllowed = (Boolean) settings.get(EvalSettings.STUDENT_MODIFY_RESPONSES);
         if (modifyAllowed == null) { modifyAllowed = false; }
         eval.setModifyResponsesAllowed(modifyAllowed);
      }

      if (eval.getUnregisteredAllowed() == null) {
         eval.setUnregisteredAllowed(Boolean.FALSE);
      }

      // admin settings
      if (eval.getInstructorOpt() == null) {
         String instOpt = (String) settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE);
         if (instOpt == null) { instOpt = EvalConstants.INSTRUCTOR_REQUIRED; }
         eval.setInstructorOpt(instOpt);
      }
   }

   /**
    * Fixes up the evaluation dates so that the evaluation is assured to save with
    * valid dates, this should be run whenever the dates of an evaluation are being updated or changed
    * 
    * @param eval an {@link EvalEvaluation} object (can be persisted or new)
    */
   public void fixupEvaluationDates(EvalEvaluation eval) {
      boolean useStopDate = ((Boolean) settings.get(EvalSettings.EVAL_USE_STOP_DATE));
      boolean useViewDate = ((Boolean) settings.get(EvalSettings.EVAL_USE_VIEW_DATE));
      boolean useDateTime = ((Boolean) settings.get(EvalSettings.EVAL_USE_DATE_TIME));
      // Getting the system setting that tells what should be the minimum time difference between start date and due date.
      int minHoursDifference = ((Integer) settings.get(EvalSettings.EVAL_MIN_TIME_DIFF_BETWEEN_START_DUE)).intValue();

      Date now = new Date();
      if (eval.getStartDate() == null) {
         eval.setStartDate( now );
      }

      if (eval.getStartDate().after( now ) ) {
         // set the start date/time to now if immediate start is selected (custom start date is false) AND
         // the start date is NOT in the past
         if (! eval.customStartDate) {
            eval.setStartDate( now );
         }
      }

      // force the due date to the end of the day if we are using dates only
      if (! useDateTime ) {
         if (eval.getDueDate() != null) {
            eval.setDueDate( EvalUtils.getEndOfDayDate( eval.getDueDate() ) );
         }
      }

      if (! useStopDate) {
         // force stop date to due date if not in use
         eval.setStopDate(eval.getDueDate());
      }

      // set stop date to the due date if not set
      if (eval.getStopDate() == null) {
         // this is possible since the stopDate class variable might be set to null
         log.info("Setting the null stop date to the due date: " + eval.getDueDate());
         eval.setStopDate(eval.getDueDate());
      } else {
         // force the stop date to the end of the day if we are using dates only
         if (! useDateTime ) {
            if (eval.getStopDate() != null) {
               log.info("Forcing date to end of day for non null stop date: " + eval.getStopDate());
               eval.setStopDate( EvalUtils.getEndOfDayDate( eval.getStopDate() ) );
            }
         }
      }

      // Ensure minimum time difference between start and due/stop dates in eval - check this after the dates are set
      if (eval.getDueDate() != null) {
         EvalUtils.updateDueStopDates(eval, minHoursDifference);
      }

      if (! useViewDate) {
         if (eval.getDueDate() != null) {
            // force view date to due date + const mins if not in use
            eval.setViewDate( new Date( eval.getDueDate().getTime() + (1000 * 60 * EvalConstants.EVALUATION_TIME_TO_WAIT_MINS) ) );
         }
      }

      /*
       * If "EVAL_USE_SAME_VIEW_DATES" system setting (admin setting) flag is set 
       * as true then don't look for student and instructor dates, instead make them
       * same as admin view date. If not then keep the student and instructor view dates.
       */ 
      boolean sameViewDateForAll = ((Boolean) settings.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES));
      if (sameViewDateForAll) {
         if (eval.studentViewResults) {
            eval.setStudentsDate( eval.getViewDate() );
         }
         if (eval.instructorViewResults) {
            eval.setInstructorsDate( eval.getViewDate() );
         }
      }

      // force the student/instructor dates null based on the boolean settings
      if (! eval.studentViewResults) {
         eval.setStudentsDate(null);
      }
      if (! eval.instructorViewResults) {
         eval.setInstructorsDate(null);
      }
   }
   
   
}
