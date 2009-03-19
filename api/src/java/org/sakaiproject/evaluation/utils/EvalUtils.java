/**
 * $Id$
 * $URL$
 * EvalUtils.java - evaluation - Dec 28, 2006 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;


/**
 * This class contains various model related utils needed in many areas
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalUtils {

    private static Log log = LogFactory.getLog(EvalUtils.class);

    /**
     * Get the state of an evaluation object<br/>
     * StartDate > DueDate (null) >= StopDate (null) >= ViewDate (null) >= student/instructorViewDate (null)<br/>
     * States: Partial -> InQueue -> Active -> GracePeriod -> Closed -> Viewable (-> Deleted)
     * 
     * @param eval the evaluation object
     * @param noSpecial if this is true then the special (PARTIAL, DELETED) states will not be returned,
     * otherwise it will return the state as determined by the state settings,
     * use this to override the check when you want to get the state according to the dates
     * @return the EVALUATION_STATE constant
     */
    public static String getEvaluationState(EvalEvaluation eval, boolean noSpecial) {
        if (eval == null) {
            throw new NullPointerException("getEvaluationState: Evaluation must not be null");
        }
        Date today = new Date();
        String state = EvalConstants.EVALUATION_STATE_UNKNOWN;
        try {
            if (! noSpecial) {
                // handle the 2 special case states first
                if (eval.getId() == null ||
                        EvalConstants.EVALUATION_STATE_PARTIAL.equals(eval.getState())) {
                    state = EvalConstants.EVALUATION_STATE_PARTIAL;
                } else if (EvalConstants.EVALUATION_STATE_DELETED.equals(eval.getState())) {
                    state = EvalConstants.EVALUATION_STATE_DELETED;
                }
            }
            if (noSpecial || EvalConstants.EVALUATION_STATE_UNKNOWN.equals(state)) {
                // now determine the state based on dates
                if ( eval.getStartDate().after(today) ) {
                    state = EvalConstants.EVALUATION_STATE_INQUEUE;
                } else if ( eval.getDueDate() == null 
                        || eval.getDueDate().after(today) ) {
                    // we are stuck in active state until a due date is set
                    state = EvalConstants.EVALUATION_STATE_ACTIVE;
                } else if ( eval.getStopDate() != null 
                        && eval.getStopDate().after(today) ) {
                    state = EvalConstants.EVALUATION_STATE_GRACEPERIOD;
                } else {
                    // we know we are past the stop date at this point so we have to be closed or viewable
                    if ( eval.getViewDate() == null) {
                        // if view date is not set so we go straight to viewing state
                        state = EvalConstants.EVALUATION_STATE_VIEWABLE;
                    } else {
                        if ( eval.getViewDate().after(today) ) {
                            state = EvalConstants.EVALUATION_STATE_CLOSED;
                        } else {
                            state = EvalConstants.EVALUATION_STATE_VIEWABLE;
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            state = EvalConstants.EVALUATION_STATE_UNKNOWN;
        }
        return state;
    }

    /**
     * Allows checking the order of states (firstState AFTER secondState)<br/>
     * States: Partial -> InQueue -> Active -> GracePeriod -> Closed -> Viewable (-> Deleted)
     * 
     * @param firstState the first state constant to check
     * @param secondState the second state constant to check
     * @param includeSame if true then true will be returned if the firstState = secondState,
     * otherwise false is returned when the states are equal
     * @return true if the firstState is after the secondState, 
     * false if the firstState is before the secondState
     */
    public static boolean checkStateAfter(String firstState, String secondState, boolean includeSame) {
        boolean isAfterState = false;
        if (firstState.equals(secondState)) {
            isAfterState = includeSame;
        } else {
            int firstNum = stateToNumber(firstState);
            int secondNum = stateToNumber(secondState);
            if (firstNum > secondNum) {
                isAfterState = true;
            }
        }
        return isAfterState;
    }

    /**
     * Allows checking the order of states (firstState BEFORE secondState)<br/>
     * States: Partial -> InQueue -> Active -> GracePeriod -> Closed -> Viewable (-> Deleted)
     * 
     * @param firstState the first state constant to check
     * @param secondState the second state constant to check
     * @param includeSame if true then true will be returned if the checkState = currentState,
     * otherwise false is returned when the states are equal
     * @return true if the firstState is before the secondState, 
     * false if the firstState is after the secondState
     */
    public static boolean checkStateBefore(String firstState, String secondState, boolean includeSame) {
        boolean isBeforeState = false;
        if (firstState.equals(secondState)) {
            isBeforeState = includeSame;
        } else {
            int firstNum = stateToNumber(firstState);
            int secondNum = stateToNumber(secondState);
            if (firstNum < secondNum) {
                isBeforeState = true;
            }
        }
        return isBeforeState;
    }

    private static HashMap<String, Integer> stateNumbers = null;
    /**
     * Defines the ORDER of the states<br/>
     * States: Partial -> InQueue -> Active -> GracePeriod -> Closed -> Viewable (-> Deleted)
     * 
     * @param stateConstant
     * @return a number indicating the position
     */
    private static int stateToNumber(String stateConstant) {
        // maybe should do this with a map or something
        int value = -1;
        if (stateNumbers == null) {
            stateNumbers = new HashMap<String, Integer>();
            for (int i = 0; i < EvalConstants.STATE_ORDER.length; i++) {
                stateNumbers.put(EvalConstants.STATE_ORDER[i], i);
            }
        }
        if (stateNumbers.containsKey(stateConstant)) {
            value = stateNumbers.get(stateConstant);
        }
        return value;
    }

    /**
     * Checks if a string is a valid stateConstant
     * 
     * @param stateConstant one of the EVALUATION_STATE_* constants (e.g. {@link EvalConstants#EVALUATION_STATE_ACTIVE}),
     * @return true if this constant is valid
     * @throws IllegalArgumentException if the constant is not one of the ones from {@link EvalConstants}
     */
    public static boolean validateStateConstant(String stateConstant) {
        if ( EvalConstants.EVALUATION_STATE_ACTIVE.equals(stateConstant) ||
                EvalConstants.EVALUATION_STATE_CLOSED.equals(stateConstant) ||
                EvalConstants.EVALUATION_STATE_DELETED.equals(stateConstant) ||
                EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(stateConstant) ||
                EvalConstants.EVALUATION_STATE_INQUEUE.equals(stateConstant) ||
                EvalConstants.EVALUATION_STATE_PARTIAL.equals(stateConstant) ||
                EvalConstants.EVALUATION_STATE_VIEWABLE.equals(stateConstant)) {
            // all is ok
        } else {
            throw new IllegalArgumentException("Invalid sharing constant ("+stateConstant+"), " +
            "must be one of EvalConstants.EVALUATION_STATE_*");
        }
        return true;
    }

    /**
     * Checks if a sharing constant is valid or null
     * 
     * @param sharingConstant a sharing constant from EvalConstants.SHARING_*
     * @throws IllegalArgumentException is the constant is null or does not match the set
     */
    public static boolean validateSharingConstant(String sharingConstant) {
        if ( EvalConstants.SHARING_OWNER.equals(sharingConstant) ||
                EvalConstants.SHARING_PRIVATE.equals(sharingConstant) ||
                EvalConstants.SHARING_PUBLIC.equals(sharingConstant) ||
                EvalConstants.SHARING_SHARED.equals(sharingConstant) ||
                EvalConstants.SHARING_VISIBLE.equals(sharingConstant)) {
            // all is ok
        } else {
            throw new IllegalArgumentException("Invalid sharing constant ("+sharingConstant+"), " +
            "must be one of EvalConstants.SHARING_*");
        }
        return true;
    }

    /**
     * Checks if an email include constant is valid or null
     * 
     * @param includeConstant an email include constant from EvalConstants.EMAIL_INCLUDE_*
     * @throws IllegalArgumentException is the constant is null or does not match the set
     */
    public static boolean validateEmailIncludeConstant(String includeConstant) {
        if (EvalConstants.EVAL_INCLUDE_ALL.equals(includeConstant) ||
                EvalConstants.EVAL_INCLUDE_NONTAKERS.equals(includeConstant) ||
                EvalConstants.EVAL_INCLUDE_RESPONDENTS.equals(includeConstant) ) {
            // all is ok
        } else {
            throw new IllegalArgumentException("Invalid include constant ("+includeConstant+"), " +
            "must use one of the ones from EvalConstants.EMAIL_INCLUDE_*");
        }
        return true;
    }


    /**
     * Ensures that there is minimum number of hours difference between
     * the start date of the evaluation and due/stop date of the evaluation,
     * updates the due/stop/view dates as needed, will not move dates unless they have to be moved,
     * stop date will be set to the due date if necessary and view date will be set to the stop date + 1 second<br/>
     * This will now ensure that the dates are actually set before attempting to read them
     * 
     * @param eval {@link EvalEvaluation} object that contains both start and due date.
     * @param minHoursLong the minimum number of hours between the startdate and the duedate,
     * usually would come from a system setting {@link EvalSettings#EVAL_MIN_TIME_DIFF_BETWEEN_START_DUE}
     * @return the current Due Date (updated if necessary)
     */
    public static Date updateDueStopDates(EvalEvaluation eval, int minHoursLong) {
        /*
         * If the difference between start date and due date is less than
         * the minimum value set as system settings, then update the due date
         * to reflect this minimum time difference. After that update the 
         * stop and view date also.
         */
        if (minHoursLong > 0 
                && eval.getDueDate() != null
                && minHoursLong > 0) {
            if (getHoursDifference(eval.getStartDate(), eval.getDueDate()) < minHoursLong) {

                // Update due date
                Date newDueDate = new Date( eval.getStartDate().getTime() + (1000l * 60l * 60l * (long)minHoursLong) );
                log.info("Fixing eval (" + eval.getId() + ") due date from " + eval.getDueDate() + " to " + newDueDate);
                eval.setDueDate(newDueDate);

                // Update stop date if needed
                if (eval.getStopDate() != null) {
                    if (eval.getStopDate().before(eval.getDueDate())) {
                        eval.setStopDate(eval.getDueDate());
                    }
                }

                // Update view date if needed
                if (eval.getViewDate() != null) {
                    if (eval.getViewDate().equals(eval.getStopDate()) ||
                            eval.getViewDate().before(eval.getStopDate()) ) {
                        Date newView = new Date( eval.getStopDate().getTime() + 5000 );
                        log.info("Fixing the view date from " + eval.getViewDate() + " to " + newView);
                        eval.setViewDate(newView);
                    }
                }
            }
        }
        return eval.getDueDate();
    }

    /**
     * Set the time portion to the end of the day instead (23:59), this is to avoid confusion for users
     * when setting the evaluationSetupService to end on a certain date and having them end in the first minute of the day instead of
     * at the end of the day
     * Note: This may lead to a nasty bug if anyone ever attempts to explicitly set the time for the stop and due dates
     * 
     * @param d a {@link java.util.Date}
     * @return a {@link java.util.Date} which has the time portion set to the end of the day or the original Date
     */
    public static Date getEndOfDayDate(Date d) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        log.info("Setting a date to the end of the day from " + d + " to " + cal.getTime());
        return cal.getTime();
    }

    /**
     * Check if the time portion of a date is set to midnight and return true if it is
     * 
     * @param d a {@link Date} object
     * @return true if time is midnight (00:00:00), false otherwise
     * @deprecated No longer used
     */
    public static boolean isTimeMidnight(Date d) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(d);
        if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0) {
            return true;
        }
        return false;
    }

    /**
     * Find the number of hours between 2 dates,
     * they should be in date order if you want a positive result,
     * otherwise the result will be negative
     * 
     * @param date1 the first date
     * @param date2 the second date
     * @return number of hours (can be negative, will round)
     */
    public static int getHoursDifference(Date date1, Date date2) {
        long millisecondsDifference = date2.getTime() - date1.getTime();
        long difference = millisecondsDifference / (60l * 60l * 1000l);
        return (int) difference;
    }

    /**
     * Creates a unique title for an adhoc scale
     * @return a unique scale title
     */
    public static String makeUniqueIdentifier(int maxLength) {
        String newTitle = UUID.randomUUID().toString();
        if (newTitle.length() > maxLength) {
            newTitle = newTitle.substring(0, maxLength);
        }
        return newTitle;
    }

    /**
     * Ensures that a string does not exceed a certain length, if it does then
     * the "..." is appended in the return
     * 
     * @param str any string
     * @param maxLength
     * @return a string which does not exceed the max length
     */
    public static String makeMaxLengthString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (maxLength > 3) {
            if (str.length() > maxLength) {
                str = str.substring(0, maxLength - 2) + "...";
            }
        }
        return str;
    }

    /**
     * Check if an email address is valid (you should trim it before you send it along)
     * 
     * @param email an email address
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        boolean valid = false;
        if (email != null && email.length() > 5) {
            valid = email.matches("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,6})$");
        }
        return valid;
    }

    public static String ENDING_P_SPACE_TAGS = "<p>&nbsp;</p>";
    public static String STARTING_P_TAG = "<p>";
    public static String ENDING_P_TAG = "</p>";

    /**
     * Attempts to remove all unnecessary P tags from html strings
     * 
     * @param cleanup an html string to cleanup
     * @return the cleaned up string
     */
    public static String cleanupHtmlPtags(String cleanup) {
        if (cleanup == null) {
            // nulls are ok
            return null;
        } else if (cleanup.trim().length() == 0) {
            // nothing to do
            return cleanup;
        }
        cleanup = cleanup.trim();

        if (cleanup.length() > ENDING_P_SPACE_TAGS.length()) {
            // (remove trailing blank lines)
            // - While (cleanup ends with "<p>&nbsp;</p>") remove trailing "<p>&nbsp;</p>".
            while (cleanup.toLowerCase().endsWith(ENDING_P_SPACE_TAGS)) {
                // chop off the end
                cleanup = cleanup.substring(0, cleanup.length() - ENDING_P_SPACE_TAGS.length()).trim();
            }
        }

        if (cleanup.length() > (STARTING_P_TAG.length() + ENDING_P_TAG.length())) {
            // (remove a single set of <p> tags)
            // if cleanup starts with "<p>" and cleanup ends with "</p>" and, remove leading "<p>" and trailing "</p>" from cleanup
            String lcCheck = cleanup.toLowerCase();
            if (lcCheck.startsWith(STARTING_P_TAG) 
                    && lcCheck.endsWith(ENDING_P_TAG)) {
                if (lcCheck.indexOf(STARTING_P_TAG, STARTING_P_TAG.length()) == -1 
                        && lcCheck.lastIndexOf(ENDING_P_TAG, lcCheck.length() - ENDING_P_TAG.length() - 1) == -1) {
                    // chop off the front and end P tags
                    cleanup = cleanup.substring(STARTING_P_TAG.length(), cleanup.length() - ENDING_P_TAG.length()).trim();
                }
            }
        }

        return cleanup;
    }

    /**
     * Get a map of answers for the given response, where the key to
     * access a given response is the unique pairing of templateItemId and
     * the associated field of the answer (instructor id, environment key, etc.)
     * 
     * @param response the response we want to get the answers for
     * @return a hashmap of answers, where key = templateItemId + answer.associatedType + answer.associatedId
     */
    public static Map<String, EvalAnswer> getAnswersMapByTempItemAndAssociated(EvalResponse response) {
        Map<String, EvalAnswer> map = new HashMap<String, EvalAnswer>();
        Set<EvalAnswer> answers = response.getAnswers();
        for (Iterator<EvalAnswer> it = answers.iterator(); it.hasNext();) {
            EvalAnswer answer = it.next();
            // decode the stored answers into the int array
            answer.multipleAnswers = EvalUtils.decodeMultipleAnswers(answer.getMultiAnswerCode());
            // decode the NA value
            decodeAnswerNA(answer);
            // place the answers into a map which uses the TI, assocType, and assocId as a key
            String key = TemplateItemUtils.makeTemplateItemAnswerKey(answer.getTemplateItem().getId(), 
                    answer.getAssociatedType(), answer.getAssociatedId());
            map.put(key, answer);
        }
        return map;
    }

    public static String SEPARATOR = ":";

    /**
     * Encodes an array of integers into a string so it can be stored in a format like so:
     * :0:1:4:7:<br/>It will sort the numbers in ascending order<br/>
     * This allows searches to continue to work without use having to resort to another table<br/>
     * Pairs with the {@link #decodeMultipleAnswers(String)} method
     * 
     * @param answerKeys an array of integers, can be null or empty
     * @return the encoded string, will be null if the input is null
     */
    public static String encodeMultipleAnswers(Integer[] answerKeys) {
        String encoded = null;
        if (answerKeys != null && answerKeys.length > 0) {
            Arrays.sort(answerKeys); // sort the keys first
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < answerKeys.length; i++) {
                sb.append(SEPARATOR);
                sb.append(answerKeys[i]);
            }
            sb.append(SEPARATOR);
            encoded = sb.toString();
        }
        return encoded;
    }

    /**
     * Decodes an encoded multiple answer string (e.g. :1:3:) into an array of integers<br/>
     * Array will always be returned in sorted order<br/>
     * Behavior is undefined if the string is not encoded correctly as this method will not
     * attempt to validate the string beforehand
     * 
     * @param encodedAnswers a string encoded using {@link #encodeMultipleAnswers(Integer[])}
     * @return the decoded array of integers or an empty array if the encoded string is empty
     * @throws IllegalArgumentException if the string cannot be decoded correctly
     */
    public static Integer[] decodeMultipleAnswers(String encodedAnswers) {
        Integer[] decoded = null;
        if (encodedAnswers != null && encodedAnswers.length() > 0) {
            if (encodedAnswers.startsWith(SEPARATOR) &&
                    encodedAnswers.endsWith(SEPARATOR)) {
                String[] split = encodedAnswers.split(SEPARATOR);
                if (split.length > 1) {
                    decoded = new Integer[split.length - 1];
                    for (int i = 1; i < (split.length); i++) {
                        if ("".equals(split[i])) {
                            throw new IllegalArgumentException("This encoded string ("+encodedAnswers+") is invalid, it must have integers in it, example: :0:3:4:");
                        }
                        decoded[i-1] = Integer.valueOf(split[i]).intValue();
                    }
                    Arrays.sort(decoded); // make sure it is sorted before returning the array
                }
            } else {
                throw new IllegalArgumentException("This encoded string ("+encodedAnswers+") is invalid, must adhere to the right format, example: :0:3:4:");
            }
        }
        if (decoded == null) {
            decoded = new Integer[0];
        }
        return decoded;
    }

    /**
     * Sets the persistent fields of this answer based on the NA setting in the
     * non-persistent field {@link EvalAnswer#NA}<br/>
     * <b>NOTE:</b> This is not always safe to run as it changes the values of
     * the persistent object so be careful
     * 
     * @param answer an {@link EvalAnswer} (saved or new)
     * @return true if this answer is NA, false otherwise
     */
    public static boolean encodeAnswerNA(EvalAnswer answer) {
        if (answer == null) {
            throw new IllegalArgumentException("answer cannot be null");
        }
        boolean notApplicable = answer.NA;
        if (notApplicable) {
            answer.setNumeric(EvalConstants.NA_VALUE);
            answer.setText(null);
            answer.setMultiAnswerCode(null);
        }
        return notApplicable;
    }

    /**
     * Sets the non-persistent field {@link EvalAnswer#NA} of this answer 
     * to the correct value based on the values of the persistent fields<br/>
     * <b>NOTE:</b> This is always safe to run as it does not change the values of
     * the persistent objects
     * 
     * @param answer an {@link EvalAnswer} (saved or new)
     * @return true if this answer is NA, false otherwise
     */
    public static boolean decodeAnswerNA(EvalAnswer answer) {
        if (answer == null) {
            throw new IllegalArgumentException("answer cannot be null");
        }
        boolean notApplicable = false;
        if (EvalConstants.NA_VALUE.equals(answer.getNumeric()) ) {
            notApplicable = true;
        } else {
            notApplicable = false;
        }
        answer.NA = notApplicable;
        return notApplicable;
    }

    /**
     * Get the Evaluation Response Rate as a human readable string. This is typically
     * used for getting the response rate of an <em>active</em> or <em>closed</em> evaluation. This includes
     * the percentage. The string will typically look something like 11% ( 3 / 98 )<br/>
     * Get the counts from {@link EvalDeliveryService#countResponses(Long, String, Boolean)} and
     * {@link EvalEvaluationService#countParticipantsForEval(Long, String)}
     * 
     * @param responsesCount number of responses
     * @param enrollmentsCount number of total enrollments or 0 if unknown
     * @return Human readable string with participant response rate.
     */
    public static String makeResponseRateStringFromCounts(int responsesCount, int enrollmentsCount) {
        String returnString = null;
        if (enrollmentsCount > 0) {
            long percentage = Math.round( (((float)responsesCount) / (float)enrollmentsCount) * 100.0 );
            returnString = percentage + "%  ( " + responsesCount + " / " + enrollmentsCount + " )";
        } else {
            return responsesCount + " / --";
        }
        return returnString;
    }

    /**
     * Check if a string is blank,
     * if blank then return true
     * 
     * @param value any string
     * @return true if this is blank
     */
    public static boolean isBlank(String value) {
        boolean blank = false;
        if (value == null) {
            blank = true;
        } else {
            if (value.trim().length() == 0) {
                blank = true;
            }
        }
        return blank;
    }

    /**
     * Get a due date from an evaluation and ensure the date is not null,
     * note that the date will be null when no due date is set yet
     * 
     * @param evaluation an evaluation
     * @return a due date (even if due date is not set the due date will be 7 days from now)
     */
    public static Date getSafeDueDate(EvalEvaluation eval) {
        if (eval == null) {
            throw new IllegalArgumentException("evaluation must not be null");
        }
        Date dueDate = eval.getDueDate() != null ? eval.getDueDate() : new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7));
        return dueDate;
    }



    /**
     * Takes 2 lists of group types, {@link EvalGroup} and {@link EvalAssignGroup}, and
     * merges the groups in common and then returns an array of the common groups,
     * comparison is on the evalGroupId
     * 
     * @param evalGroups a list of {@link EvalGroup}
     * @param assignGroups a list of {@link EvalAssignGroup}
     * @return an array of the groups that are in common between the 2 lists
     */
    public static EvalGroup[] getGroupsInCommon(List<EvalGroup> evalGroups, List<EvalAssignGroup> assignGroups) {
        List<EvalGroup> groups = new ArrayList<EvalGroup>();
        for (int i=0; i<evalGroups.size(); i++) {
            EvalGroup group = (EvalGroup) evalGroups.get(i);
            for (int j=0; j<assignGroups.size(); j++) {
                EvalAssignGroup assignGroup = (EvalAssignGroup) assignGroups.get(j);
                if (group.evalGroupId.equals(assignGroup.getEvalGroupId())) {
                    groups.add(group);
                    break;
                }
            }
        }
        return (EvalGroup[]) groups.toArray(new EvalGroup[] {});
    }

    /**
     * Converts a collection of user assignments into a set of userIds,
     * simple convenience method to avoid writing the same code over and over
     * 
     * @param userAssignments a collection of user assignments
     * @return the set of all unique userIds in the assignments
     */
    public static Set<String> getUserIdsFromUserAssignments(Collection<EvalAssignUser> userAssignments) {
        Set<String> s;
        if (userAssignments == null) {
            s = new HashSet<String>(0);
        } else {
            s = new LinkedHashSet<String>(userAssignments.size()); // maintain order
            for (EvalAssignUser evalAssignUser : userAssignments) {
                if (evalAssignUser.getUserId() != null) {
                    s.add(evalAssignUser.getUserId());
                }
            }
        }
        return s;
    }

    /**
     * Converts a collection of user assignments into a set of evalGroupIds,
     * simple convenience method to avoid writing the same code over and over
     * 
     * @param userAssignments a collection of user assignments
     * @return the set of all unique groupIds in the assignments
     */
    public static Set<String> getGroupIdsFromUserAssignments(Collection<EvalAssignUser> userAssignments) {
        Set<String> s;
        if (userAssignments == null) {
            s = new HashSet<String>(0);
        } else {
            s = new LinkedHashSet<String>(userAssignments.size()); // maintain order
            for (EvalAssignUser evalAssignUser : userAssignments) {
                if (evalAssignUser.getEvalGroupId() != null) {
                    s.add(evalAssignUser.getEvalGroupId());
                }
            }
        }
        return s;
    }

    /**
     * Creates eval groups from groupIds
     * 
     * @param evalGroupIds a collection of evalGroupId (unique ids for eval groups)
     * @param commonLogic the common logic service
     * @return the list of EvalGroup objects for the given collection of evalGroupIds
     */
    public static List<EvalGroup> makeGroupsFromGroupsIds(Collection<String> evalGroupIds, EvalCommonLogic commonLogic) {
        List<EvalGroup> l = new ArrayList<EvalGroup>();
        if (evalGroupIds != null && evalGroupIds.size() > 0) {
            for (String evalGroupId : evalGroupIds) {
                EvalGroup group = commonLogic.makeEvalGroupObject(evalGroupId);
                l.add(group);
            }
        }
        return l;
    }

}
