/**
 * $Id$
 * $URL$
 * EvalSettings.java - evaluation - Dec 25, 2006 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic;


/**
 * This allows access to and control of all system settings for the evaluation
 * system<br/>
 * <p>
 * INSTRUCTOR is a user with the be.evaluated permission set<br/>
 * STUDENT is a user with the take.evaluation permission set<br/>
 * ADMIN is a user with the administrate permission set (or Sakai super admin)<br/>
 * </p>
 * <b>IMPORTANT NOTE FOR DEVELOPERS:</b><br/>
 * If you add any settings to this file which are boolean or ternary you MUST add them to the
 * list of constants ({@link #BOOLEAN_SETTINGS} or {@link #TERNARY_BOOLEAN_SETTINGS}), you should also make sure that
 * you add the items to the preloader so the settings are preloaded correctly
 * <br/>
 * Usage: The public static final variables should be used when doing gets or sets of values (use the ones marked CONSTANT).<br/>
 * Example:<br/>
 * String s = (String) evalSettings.get(EvaluationSettings.FROM_EMAIL_ADDRESS);<br/>
 * <br/>
 * <b>Note:</b> Meant to be used internally in the evaluation system app only
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalSettings {

   /**
    * CONSTANT: Is the instructor allowed to create evaluations - {@link Boolean}, default True
    */
   public static final String INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS = "INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS:java.lang.Boolean";
   /**
    * CONSTANT: Is the instructor allowed to view the results of evaluations - {@link Boolean}, default True
    * <b>Note:</b> If this is NULL then the evaluation settings override, otherwise this overrides the evaluation setting
    */
   public static final String INSTRUCTOR_ALLOWED_VIEW_RESULTS = "INSTRUCTOR_ALLOWED_VIEW_RESULTS:java.lang.Boolean";
   /**
    * CONSTANT: Is the instructor allowed to send email reminders to students - {@link Boolean}, default True
    */
   public static final String INSTRUCTOR_ALLOWED_EMAIL_STUDENTS = "INSTRUCTOR_ALLOWED_EMAIL_STUDENTS:java.lang.Boolean";
   /**
    * CONSTANT: Does the instructor have to use evaluations from above in the hierarchy - {@link String}, default INSTRUCTOR_OPT_OUT
    * <b>Note:</b> If this is NULL then the evaluation settings override (Configurable), otherwise this overrides the evaluation setting
    */
   public static final String INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE = "INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE:java.lang.String";
   /**
    * CONSTANT: How many items is the instructor allowed to add to an evaluation from above in the hierarchy - {@link Integer}, default 5
    */
   public static final String INSTRUCTOR_ADD_ITEMS_NUMBER = "INSTRUCTOR_ADD_ITEMS_NUMBER:java.lang.Integer";

   /**
    * CONSTANT: Student is allowed to leave questions unanswered (this only affects multiple choice items) - {@link Boolean}, default True
    * <b>Note:</b> If this is NULL then the evaluation settings override, otherwise this overrides the evaluation setting
    */
   public static final String STUDENT_ALLOWED_LEAVE_UNANSWERED = "STUDENT_ALLOWED_LEAVE_UNANSWERED:java.lang.Boolean";
   /**
    * CONSTANT: Student is allowed to modify their responses after they have submitted the evaluation but before the due date - {@link Boolean}, default False
    * <b>Note:</b> If this is NULL then the evaluation settings override, otherwise this overrides the evaluation setting
    */
   public static final String STUDENT_MODIFY_RESPONSES = "STUDENT_MODIFY_RESPONSES:java.lang.Boolean";
   /**
    * CONSTANT: Student is allowed to view the results of the evaluation - {@link Boolean}, default False
    * <b>Note:</b> If this is NULL then the evaluation settings override, otherwise this overrides the evaluation setting
    */
   public static final String STUDENT_VIEW_RESULTS = "STUDENT_VIEW_RESULTS:java.lang.Boolean";

   /**
    * CONSTANT: Admin is allowed to add this many items to an evaluation from above in the hierarchy - {@link Integer}, default 5
    */
   public static final String ADMIN_ADD_ITEMS_NUMBER = "ADMIN_ADD_ITEMS_NUMBER:java.lang.Integer";
   /**
    * CONSTANT: Admin is allowed to view results from items added below them in the hierarchy - {@link Boolean}, default False
    */
   public static final String ADMIN_VIEW_BELOW_RESULTS = "ADMIN_VIEW_BELOW_RESULTS:java.lang.Boolean";
   /**
    * CONSTANT: Admin is allowed to view results from items added by instructors below them in the hierarchy - {@link Boolean}, default False
    */
   public static final String ADMIN_VIEW_INSTRUCTOR_ADDED_RESULTS = "ADMIN_VIEW_INSTRUCTOR_ADDED_RESULTS:java.lang.Boolean";

   /**
    * CONSTANT: This is the standard from email address used when sending reminders - {@link String}, default "helpdesk@institution.edu"
    */
   public static final String FROM_EMAIL_ADDRESS = "FROM_EMAIL_ADDRESS:java.lang.String";
   /**
    * CONSTANT: Number of responses required before results are visible - {@link Integer}, default 5
    */
   public static final String RESPONSES_REQUIRED_TO_VIEW_RESULTS = "RESPONSES_REQUIRED_TO_VIEW_RESULTS:java.lang.Integer";
   /**
    * CONSTANT: Are users allowed to use Not Available in templates and evaluations - {@link Boolean}, default True
    */
   public static final String NOT_AVAILABLE_ALLOWED = "NOT_AVAILABLE_ALLOWED:java.lang.Boolean";
   /**
    * CONSTANT: Require a comments block to be included in every evaluation - {@link Boolean}, default True
    */
   public static final String REQUIRE_COMMENTS_BLOCK = "REQUIRE_COMMENTS_BLOCK:java.lang.Boolean";
   /**
    * CONSTANT: How many items are allowed to be used with a question block - {@link Integer}, default 10
    */
   public static final String ITEMS_ALLOWED_IN_QUESTION_BLOCK = "ITEMS_ALLOWED_IN_QUESTION_BLOCK:java.lang.Integer";
   /**
    * CONSTANT: What is the standard setting for sharing templates across the system - {@link String}, default SHARING_OWNER<br/>
    * <b>Note:</b> Use the EvalConstants.SHARING constants when setting or comparing this value
    */
   public static final String TEMPLATE_SHARING_AND_VISIBILITY = "TEMPLATE_SHARING_AND_VISIBILITY:java.lang.String";
   /**
    * CONSTANT: Allow users to use expert templates - {@link Boolean}, default True
    */
   public static final String USE_EXPERT_TEMPLATES = "USE_EXPERT_TEMPLATES:java.lang.Boolean";
   /**
    * CONSTANT: Allow users to use expert items - {@link Boolean}, default True
    */
   public static final String USE_EXPERT_ITEMS = "USE_EXPERT_ITEMS:java.lang.Boolean";
   /**
    * CONSTANT: If true then all item categories default to course, 
    * if false then allow item authors to select a category for items - {@link Boolean}, default False<br/>
    */
   public static final String ITEM_USE_COURSE_CATEGORY_ONLY = "ITEM_USE_COURSE_CATEGORY_ONLY:java.lang.Boolean";

   /**
    * CONSTANT: How many days old can an eval be and still be recently closed - {@link Integer}, default 10<br/>
    * It must be less than or equal to this many days old to count as recent
    */
   public static final String EVAL_RECENTLY_CLOSED_DAYS = "EVAL_RECENTLY_CLOSED_DAYS:java.lang.Integer";
   /**
    * CONSTANT: Allow users to set the stop date when creating evaluations - {@link Boolean}, default False<br/>
    * <b>Note:</b> Stop date should default to the due date when the user cannot set it
    */
   public static final String EVAL_USE_STOP_DATE = "EVAL_USE_STOP_DATE:java.lang.Boolean";
   /**
    * CONSTANT: Allow users to set the view date when creating evaluations - {@link Boolean}, default False<br/>
    * <b>Note:</b> View date should default to the due date + 1 minute when the user cannot set it
    */
   public static final String EVAL_USE_VIEW_DATE = "EVAL_USE_VIEW_DATE:java.lang.Boolean";
   /**
    * CONSTANT: Users must use same view dates for all users looking at evaluation results - {@link Boolean}, default True<br/>
    * <b>Note:</b> If this is FALSE/NULL then the custom date boxes appear and are used (otherwise only the global view date is used)
    */
   public static final String EVAL_USE_SAME_VIEW_DATES = "EVAL_USE_SAME_VIEW_DATES:java.lang.Boolean";
   /**
    * CONSTANT: Minimum time difference (in hours) allowed between start date and 
    * due date of the evaluation - {@link Integer}, default 4<br/>
    */
   public static final String EVAL_MIN_TIME_DIFF_BETWEEN_START_DUE = "EVAL_MIN_TIME_DIFF_BETWEEN_START_DUE:java.lang.Integer";
   /**
    * CONSTANT: Allow users to choose a time as well as a date for various eval dates - {@link Boolean}, default False<br/>
    */
   public static final String EVAL_USE_DATE_TIME = "EVAL_USE_DATE_TIME:java.lang.Boolean";

   /**
    * CONSTANT: Should we display Hierarchy Options and Information in the User Interface - {@link Boolean}, default False
    */
   public static final String DISPLAY_HIERARCHY_OPTIONS = "DISPLAY_HIERARCHY_OPTIONS:java.lang.Boolean";
   /**
    * CONSTANT: Display hierarchical nodes names as headers in the take/preview eval views - {@link Boolean}, default False
    */
   public static final String DISPLAY_HIERARCHY_HEADERS = "DISPLAY_HIERARCHY_HEADERS:java.lang.Boolean";

   /**
    * CONSTANT: Allow CSV Export for Reporting - {@link Boolean}, default True
    */
   public static final String ENABLE_CSV_REPORT_EXPORT = "ENABLE_CSV_REPORT_EXPORT:java.lang.Boolean";
   /**
    * CONSTANT: Allow XLS Export for Reporting - {@link Boolean}, default True
    */
   public static final String ENABLE_XLS_REPORT_EXPORT = "ENABLE_XLS_REPORT_EXPORT:java.lang.Boolean";
   /**
    * CONSTANT: Allow PDF Export for Reporting - {@link Boolean}, default False
    */
   public static final String ENABLE_PDF_REPORT_EXPORT = "ENABLE_PDF_REPORT_EXPORT:java.lang.Boolean";
   /**
    * CONSTANT: Use an Image banner on PDF Report Exports - {@link Boolean}, default False
    */
   public static final String ENABLE_PDF_REPORT_BANNER = "ENABLE_PDF_REPORT_BANNER:java.lang.Boolean";
   /**
    * CONSTANT: Location of image in Sakai Resources to use for PDF Report Banner - {@link String}, default null
    */
   public static final String PDF_BANNER_IMAGE_LOCATION = "PDF_BANNER_IMAGE_LOCATION:java.lang.String";

   /**
    * CONSTANT: Show the sites box with all the sites that the user can take evals in or
    * be evaluated in on the summary page - {@link Boolean}, default False
    */
   public static final String ENABLE_SUMMARY_SITES_BOX = "ENABLE_SUMMARY_SITES_BOX:java.lang.Boolean";
   /**
    * CONSTANT: Show the textbox for entering evaluation categories when setting up an eval - {@link Boolean}, default False
    */
   public static final String ENABLE_EVAL_CATEGORIES = "ENABLE_EVAL_CATEGORIES:java.lang.Boolean";
   /**
    * CONSTANT: If this is activated then evaluation responses can be removed - {@link Boolean}, default False
    */
   public static final String ENABLE_EVAL_RESPONSE_REMOVAL = "ENABLE_EVAL_RESPONSE_REMOVAL:java.lang.Boolean";

   /**
    * CONSTANT: Can the item results sharing be set for template items - {@link Boolean}, default False,
    * this is a special UMD flag that is used for their external reporting system
    */
   public static final String ITEM_USE_RESULTS_SHARING = "ITEM_USE_RESULTS_SHARING:java.lang.Boolean";
   /**
    * CONSTANT: Show the import controls and the importing options for importing templates/items/scales/evals - {@link Boolean}, default False
    */
   public static final String ENABLE_IMPORTING = "ENABLE_IMPORTING:java.lang.Boolean";


   /**
    * This is here to identify all the boolean ONLY settings in the system,
    * this does not include Booleans which are configurable (i.e. can be set to null, true, or false)
    * but only includes the booleans which are always true or false
    */
   public static String[] BOOLEAN_SETTINGS = {
      ADMIN_VIEW_BELOW_RESULTS,
      ADMIN_VIEW_INSTRUCTOR_ADDED_RESULTS,
      DISPLAY_HIERARCHY_HEADERS,
      DISPLAY_HIERARCHY_OPTIONS,
      ENABLE_CSV_REPORT_EXPORT,
      ENABLE_EVAL_CATEGORIES,
      ENABLE_EVAL_RESPONSE_REMOVAL,
      ENABLE_IMPORTING,
      ENABLE_PDF_REPORT_BANNER,
      ENABLE_PDF_REPORT_EXPORT,
      ENABLE_SUMMARY_SITES_BOX,
      ENABLE_XLS_REPORT_EXPORT,
      EVAL_USE_DATE_TIME,
      EVAL_USE_SAME_VIEW_DATES,
      EVAL_USE_STOP_DATE,
      EVAL_USE_VIEW_DATE,
      INSTRUCTOR_ALLOWED_EMAIL_STUDENTS,
      INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS,
      ITEM_USE_RESULTS_SHARING,
      ITEM_USE_COURSE_CATEGORY_ONLY,
      NOT_AVAILABLE_ALLOWED,
      REQUIRE_COMMENTS_BLOCK,
      USE_EXPERT_ITEMS,
      USE_EXPERT_TEMPLATES
   };

   /**
    * This is here to identify all the booleans in the system which are ternary booleans,
    * that is to say it can store true, false, and "configurable" (null),
    * all booleans should be indicated in either this or {@link #BOOLEAN_SETTINGS}
    */
   public static String[] TERNARY_BOOLEAN_SETTINGS = {
      INSTRUCTOR_ALLOWED_VIEW_RESULTS,
      STUDENT_ALLOWED_LEAVE_UNANSWERED,
      STUDENT_MODIFY_RESPONSES,
      STUDENT_VIEW_RESULTS
   };

   /**
    * This event name indicates will be used whenever a setting was changed
    */
   public static String EVENT_SET_ONE_CONFIG = "setEvalConfigOne";
   /**
    * This event name indicates a large scale change of many settings at once
    */
   public static String EVENT_SET_MANY_CONFIG = "setEvalConfigMany";

   /**
    * Allows for getting the value of a system setting based on the constant,
    * do not forget to cast the returned Object based on the constant comment<br/>
    * Example:<br/>
    * String s = (String) evalSettings.get(EvaluationSettings.FROM_EMAIL_ADDRESS);<br/>
    * (evalSettings is an injected {@link EvalSettings} bean)
    * 
    * @param settingConstant a constant from this interface
    * @return the actual value of the setting referred to by this constant, 
    * you should typecast the returned object (type will be the same as the constant),
    * null if the object cannot be found
    */
   public Object get(String settingConstant);

   /**
    * Allows for setting the value of a system setting based on the constant<br/>
    * <b>Note:</b> Setting a value to null will remove it and effectively set it to unspecified<br/>
    * Example:<br/>
    * boolean b = evalSettings.set(EvaluationSettings.FROM_EMAIL_ADDRESS, "aaronz@vt.edu");<br/>
    * (evalSettings is an injected {@link EvalSettings} bean)
    * 
    * @param settingConstant a constant from this interface
    * @param settingValue the value of the setting referred to by this constant (type must match the constant)
    * @return true if the setting could be saved, false if not, throws runtime exceptions if invalid input
    */
   public boolean set(String settingConstant, Object settingValue);

}
