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
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.entity.EvalCategoryEntityProvider;
import org.sakaiproject.evaluation.logic.exceptions.InvalidDatesException;
import org.sakaiproject.evaluation.logic.exceptions.InvalidEvalCategoryException;
import org.sakaiproject.evaluation.logic.model.EvalUser;
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

    private EvalCommonLogic commonLogic;   
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
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
        if ( commonLogic.isUserAdmin( commonLogic.getCurrentUserId() ) ) {
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
        if ( commonLogic.isUserAdmin(userId) ) {
            allowed = true;
        } else if ( ownerId.equals(userId) ) {
            allowed = true;
        }
        return allowed;
    }

    /**
     * Sets all the system defaults for this evaluation object
     * and ensures all required fields are correctly set,
     * use this whenever you create a new evaluation <br/>
     * This is guaranteed to be non-destructive (i.e. it will not replace existing values
     * but it will fixup any required fields which are nulls to default values),
     * will force null due date to be non-null but will respect the {@link EvalEvaluation#useDueDate} flag
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
        Date now =  new Date();
        calendar.setTime( now );
        if (eval.getStartDate() == null) {
            eval.setStartDate(now);
            log.debug("Setting start date to default of: " + eval.getStartDate());
        } else {
            calendar.setTime(eval.getStartDate());
        }

        if (eval.useDueDate != null && ! eval.useDueDate) {
            // allow evals which are open forever
            eval.setDueDate(null);
            eval.setStopDate(null);
            eval.setViewDate(null);
        } else {
            // using the due date
            calendar.add(Calendar.DATE, 1); // +1 day
            if (eval.getDueDate() == null) {
                // default the due date to the end of the start date + 1 day
                Date endOfDay = EvalUtils.getEndOfDayDate( calendar.getTime() );
                eval.setDueDate( endOfDay );
                log.debug("Setting due date to default of: " + eval.getDueDate());
            } else {
                calendar.setTime(eval.getDueDate());
            }

            boolean useStopDate;
            if (eval.useStopDate != null && ! eval.useStopDate) {
                useStopDate = false;
            } else {
                useStopDate = (Boolean) settings.get(EvalSettings.EVAL_USE_STOP_DATE);
            }
            if (useStopDate) {
                // assign stop date to equal due date for now
                if (eval.getStopDate() == null) {
                    eval.setStopDate(eval.getDueDate());
                    log.debug("Setting stop date to default of: " + eval.getStopDate());
                }
            } else {
                eval.setStopDate(null);
            }

            boolean useViewDate;
            if (eval.useViewDate != null && ! eval.useViewDate) {
                useViewDate = false;
            } else {
                useViewDate = (Boolean) settings.get(EvalSettings.EVAL_USE_VIEW_DATE);
            }
            if (useViewDate) {
                // assign default view date
                calendar.add(Calendar.DATE, 1);
                if (eval.getViewDate() == null) {
                    // default the view date to the today + 2
                    eval.setViewDate(calendar.getTime());
                    log.debug("Setting view date to default of: " + eval.getViewDate());
                }
            } else {
                eval.setViewDate(null);
            }
        }

        // handle the view dates default
        Boolean useSameViewDates = (Boolean) settings.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES);
        Date sharedDate = eval.getViewDate() == null ? eval.getDueDate() : eval.getViewDate();
        if (eval.getStudentsDate() == null || useSameViewDates) {
            eval.setStudentsDate(sharedDate);
        }
        if (eval.getInstructorsDate() == null || useSameViewDates) {
            eval.setInstructorsDate(sharedDate);
        }

        // results viewable settings
        Date studentsDate = null;
        Boolean studentsView = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS);
        if (studentsView != null) {
            eval.setStudentViewResults( studentsView );
        }

        Date instructorsDate = null;
        Boolean instructorsView = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
        if (instructorsView != null) {
            eval.setInstructorViewResults( instructorsView );
        }

        if (eval.getResultsSharing() == null) {
            eval.setResultsSharing( EvalConstants.SHARING_VISIBLE );
        }
        if (EvalConstants.SHARING_PRIVATE.equals(eval.getResultsSharing())) {
            eval.setStudentViewResults(  false );
            eval.setInstructorViewResults( false );
        } else if (EvalConstants.SHARING_PUBLIC.equals(eval.getResultsSharing())) {
            eval.setStudentViewResults( true );
            eval.setInstructorViewResults( true );
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

        // fix up the reminder days to the default
        if (eval.getReminderDays() == null) {
            Integer reminderDays = 1;
            Integer defaultReminderDays = (Integer) settings.get(EvalSettings.DEFAULT_EMAIL_REMINDER_FREQUENCY);
            if (defaultReminderDays != null) {
                reminderDays = defaultReminderDays;
            }
            eval.setReminderDays(reminderDays);
        }

        // set the reminder email address to the default
        if (eval.getReminderFromEmail() == null) {
            // email from address control
            String from = (String) settings.get(EvalSettings.FROM_EMAIL_ADDRESS);
            // https://bugs.caret.cam.ac.uk/browse/CTL-1525 - default to admin address if option set
            Boolean useAdminEmail = (Boolean) settings.get(EvalSettings.USE_ADMIN_AS_FROM_EMAIL);
            if (useAdminEmail) {
                // try to get the email address for the owner (eval admin)
                EvalUser owner = commonLogic.getEvalUserById(commonLogic.getCurrentUserId());
                if (owner != null 
                        && owner.email != null 
                        && ! "".equals(owner.email)) {
                    from = owner.email;
                }
            }
            eval.setReminderFromEmail(from);
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
     * valid dates, this should be run whenever the dates of an evaluation are being updated or changed <br/>
     * This is guaranteed to be non-destructive and should only fixup invalid or null fields and dates <br/>
     * handles setting date fields based on the useDueDate, etc. fields in the evaluation as well <br/>
     * 
     * @param eval an {@link EvalEvaluation} object (can be persisted or new)
     */
    public void fixupEvaluationDates(EvalEvaluation eval) {
        if (eval == null) {
            throw new IllegalArgumentException("eval must be set to fix dates");
        }
        boolean useDueDate = eval.useDueDate != null ? eval.useDueDate : true;
        boolean useStopDate;
        if (eval.useStopDate != null && ! eval.useStopDate) {
            useStopDate = false;
        } else {
            useStopDate = (Boolean) settings.get(EvalSettings.EVAL_USE_STOP_DATE);
        }
        boolean useViewDate;
        if (eval.useViewDate != null && ! eval.useViewDate) {
            useViewDate = false;
        } else {
            useViewDate = (Boolean) settings.get(EvalSettings.EVAL_USE_VIEW_DATE);
        }
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
            if (eval.customStartDate != null 
                    && ! eval.customStartDate) {
                eval.setStartDate( now );
            }
        }

        if (! useDueDate) {
            // null out the due/stop/view dates
            eval.setDueDate(null);
            eval.setStopDate(null);
            eval.setViewDate(null);
        }

        // ensure the due date comes after the start date
        if (eval.getDueDate() != null) {
            if (! eval.getDueDate().after(eval.getStartDate())) {
                Calendar cal = new GregorianCalendar();
                cal.setTime(eval.getStartDate());
                cal.add(Calendar.HOUR, 25);
                eval.setDueDate(cal.getTime());
            }
        }

        if (! useDateTime ) {
            // force the due date to the end of the day if we are using dates only AND eval is not due yet
            if (eval.getDueDate() != null) {
                if (EvalUtils.checkStateBefore(eval.getState(), EvalConstants.EVALUATION_STATE_GRACEPERIOD, false) ) {
                    log.info("Forcing date to end of day for non null due date: " + eval.getDueDate());
                    eval.setDueDate( EvalUtils.getEndOfDayDate( eval.getDueDate() ) );
                }
            }
        }

        if (! useStopDate) {
            // force stop date to null if not in use
            eval.setStopDate(null);
        }

        if (! useDateTime ) {
            // force the stop date to the end of the day if we are using dates only AND eval is not closed yet
            if (eval.getStopDate() != null) {
                if (EvalUtils.checkStateBefore(eval.getState(), EvalConstants.EVALUATION_STATE_CLOSED, false) ) {
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
            if (EvalUtils.checkStateBefore(eval.getState(), EvalConstants.EVALUATION_STATE_ACTIVE, false) ) {
                // force view date to null if not in use AND eval is still being created
                eval.setViewDate(null);
            }
        }

        if (! useDateTime ) {
            // force the view date to the end of the day if we are using dates only AND eval is not viewable yet
            if (eval.getViewDate() != null) {
                if (EvalUtils.checkStateBefore(eval.getState(), EvalConstants.EVALUATION_STATE_VIEWABLE, false) ) {
                    log.info("Forcing date to end of day for non null stop date: " + eval.getViewDate());
                    eval.setViewDate( EvalUtils.getEndOfDayDate( eval.getViewDate() ) );
                }
            }
        }

        if (eval.getViewDate() != null 
                && eval.getDueDate() != null) {
            if (eval.getViewDate().before(eval.getDueDate())) {
                // force view date to due date if it is before the due date
                eval.setViewDate( eval.getDueDate() );
            }
        }

        /*
         * If "EVAL_USE_SAME_VIEW_DATES" system setting (admin setting) flag is set 
         * as true then don't look for student and instructor dates, instead make them
         * same as admin view date. If not then keep the student and instructor view dates.
         */ 
        boolean sameViewDateForAll = ((Boolean) settings.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES));
        if (sameViewDateForAll) {
            if (eval.getStudentViewResults()) {
                eval.setStudentsDate( eval.getViewDate() );
            }
            if (eval.getInstructorViewResults()) {
                eval.setInstructorsDate( eval.getViewDate() );
            }
        }

        // force the student/instructor dates null based on the boolean settings
        if (! eval.getStudentViewResults()) {
            eval.setStudentsDate(null);
        }
        if (! eval.getInstructorViewResults()) {
            eval.setInstructorsDate(null);
        }
    }

    /**
     * Check the dates for an evaluation and throw an exception if they are invalid
     * (in an improper order)
     * @param evaluation any evaluation
     * @throws InvalidDatesException if the dates are invalid
     */
    public static void validateEvalDates(EvalEvaluation evaluation) {
        if (evaluation.getDueDate() != null) {
            if (evaluation.getStartDate().compareTo(evaluation.getDueDate()) >= 0) {
                throw new InvalidDatesException(
                        "due date (" + evaluation.getDueDate() +
                        ") must occur after start date (" + 
                        evaluation.getStartDate() + "), can occur on the same date but not at the same time",
                "dueDate");
            }

            if (evaluation.getStopDate() != null) {
                if (evaluation.getDueDate().compareTo(evaluation.getStopDate()) > 0 ) {
                    throw new InvalidDatesException(
                            "stop date (" + evaluation.getStopDate() +
                            ") must occur on or after due date (" + 
                            evaluation.getDueDate() + "), can be identical",
                    "stopDate");
                }
                if (evaluation.getViewDate() != null) {
                    if (evaluation.getViewDate().compareTo(evaluation.getStopDate()) < 0 ) {
                        throw new InvalidDatesException(
                                "view date (" + evaluation.getViewDate() +
                                ") must occur on or after stop date (" + 
                                evaluation.getStopDate() + "), can be identical",
                        "viewDate");
                    }
                }
            }

            if (evaluation.getViewDate() != null) {
                if (evaluation.getViewDate().compareTo(evaluation.getDueDate()) < 0 ) {
                    throw new InvalidDatesException(
                            "view date (" + evaluation.getViewDate() +
                            ") must occur on or after due date (" + 
                            evaluation.getDueDate() + "), can be identical",
                    "viewDate");
                }
            }
        }
    }
    
    /**
     * Tests validity of user-supplied evaluation category by attempting to create
     * the resulting entity URL. Throws an {@link InvalidEvalCategoryException} if 
     * the eval category contains any invalid characters (i.e. characters that are 
     * invalid for use in a URL).
     * 
     * @param evalCategory the eval category entered by the user
     * @return boolean indicating its validity
     */
    public void validateEvalCategory(String evalCategory) {
    	
    	try {
    		
    		if ((evalCategory != null) && (evalCategory.length() != 0))
    			commonLogic.getEntityURL(EvalCategoryEntityProvider.ENTITY_PREFIX, evalCategory);
    		
    	} catch (IllegalArgumentException ex) {
    		throw new InvalidEvalCategoryException(ex);
    	}
    	
    }
    
}
