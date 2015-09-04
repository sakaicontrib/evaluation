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
package org.sakaiproject.evaluation.logic;

import org.sakaiproject.evaluation.constant.EvalConstants;


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
     * CONSTANT: Is the instructor allowed to view the results of all evaluations - {@link Boolean}, default True
     * <b>Note:</b> If this is NULL then the evaluation settings override, otherwise this overrides the evaluation setting
     */
    public static final String INSTRUCTOR_ALLOWED_VIEW_ALL_RESULTS = "INSTRUCTOR_ALLOWED_VIEW_ALL_RESULTS:java.lang.Boolean";
    
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
     * CONSTANT: Control whether instructor users have access to the list of responders for an evaluation - {@link Boolean}, default False
     */
    public static final String INSTRUCTOR_ALLOWED_VIEW_RESPONDERS = "INSTRUCTOR_ALLOWED_VIEW_RESPONDERS:java.lang.Boolean";

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
     * CONSTANT: Student is allowed to save a survey without submitting it.  A typical use case would be for a long
     * survey, the student can begin editing it, save their work, and then comeback to it at a later time without
     * having to reenter their information
     */
    public static final String STUDENT_SAVE_WITHOUT_SUBMIT = "STUDENT_SAVE_WITHOUT_SUBMIT:java.lang.Boolean";
    /**
     * CONSTANT: Student is presented with a Cancel button when taking an assessment.  If a Cancel button is pressed,
     * no assessment data is saved, and the user is returned to the dashboard.
     */
    public static final String STUDENT_CANCEL_ALLOWED = "STUDENT_CANCEL_ALLOWED:java.lang.Boolean";
    /**
     * CONSTANT: Student is allowed to view the results of the evaluation - {@link Boolean}, default False
     * <b>Note:</b> If this is NULL then the evaluation settings override, otherwise this overrides the evaluation setting
     */
    public static final String STUDENT_ALLOWED_VIEW_RESULTS = "STUDENT_ALLOWED_VIEW_RESULTS:java.lang.Boolean";
    
    /**
     * CONSTANT: Sakai admins are granted admin rights in the Evaluation system - {@link Boolean}, default True
     */
    public static final String ENABLE_SAKAI_ADMIN_ACCESS = "ENABLE_SAKAI_ADMIN_ACCESS:java.lang.Boolean";
    
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
    public static final String ENABLE_NOT_AVAILABLE = "ENABLE_NOT_AVAILABLE:java.lang.Boolean";
    /**
     * CONSTANT: Is the box showing the Evaluations I am creating or administrating visible? - {@link Boolean}, default True
     */
    public static final String ENABLE_ADMINISTRATING_BOX = "ENABLE_ADMINISTRATING_BOX:java.lang.Boolean";
    /**
     * CONSTANT: Is the box showing the Evals/Courses in Which I May Be Evaluated visible? - {@link Boolean}, default False
     */
    public static final String ENABLE_EVALUATEE_BOX = "ENABLE_EVALUATEE_BOX:java.lang.Boolean";
    /**
    * CONSTANT: Are the toplinks  My Evaluations, My Templates, My Items, My Scales visible? - {@link Boolean}, default True
    */
   public static final String ENABLE_MY_TOPLINKS = "ENABLE_MY_TOPLINKS:java.lang.Boolean";
   /**
    * Enable making a copy of a template when saving a new evaluation, this is configured
    * for the system, and will disable saving an evaluation with a copy of its template
    */
   public static final String ENABLE_TEMPLATE_COPYING = "ENABLE_TEMPLATE_COPYING:java.lang.Boolean";
   /**
     * Enable evaluation taker comments on all non-text answerable items, this is configured
     * on a per item basis, and will disable all comments if turned off
     */
    public static final String ENABLE_ITEM_COMMENTS = "ENABLE_ITEM_COMMENTS:java.lang.Boolean";
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
     * CONSTANT: Minimum length allowed for lists in a response - {@link Integer}, default 2<br/>
     * Needs to be greater than 0 and less than EVAL_MAX_LIST_LENGTH
     */
    public static final String EVAL_MIN_LIST_LENGTH = "EVAL_MIN_LIST_LENGTH:java.lang.Integer";

    /**
     * CONSTANT: Maximum length allowed for lists in a response - {@link Integer}, default 40<br/>
     * Needs to be greater than EVAL_MIN_LIST_LENGTH
     */
    public static final String EVAL_MAX_LIST_LENGTH = "EVAL_MAX_LIST_LENGTH:java.lang.Integer";

    /**
     * CONSTANT: How many days old can an eval be and still be recently closed (for the be evaluated box) - {@link Integer}, default 10<br/>
     * It must be less than or equal to this many days old to count as recent
     */
    public static final String EVAL_EVALUATEE_RECENTLY_CLOSED_DAYS = "EVAL_EVALUATEE_RECENTLY_CLOSED_DAYS:java.lang.Integer";

    /**
     * CONSTANT: Allow users to set the stop date when creating evaluations - {@link Boolean}, default False<br/>
     * <b>Note:</b> Stop date should default to null when it cannot be set
     */
    public static final String EVAL_USE_STOP_DATE = "EVAL_USE_STOP_DATE:java.lang.Boolean";
    /**
     * CONSTANT: Allow users to set the view date when creating evaluations - {@link Boolean}, default False<br/>
     * <b>Note:</b> View date should default to null when it cannot be set
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
     * CONSTANT: Customize the preview to show labels, titles and names associated with a specific group - {@link Boolean}, default False
     */
    public static final String ENABLE_GROUP_SPECIFIC_PREVIEW = "ENABLE_GROUP_SPECIFIC_PREVIEW:java.lang.Boolean";

    /**
     * CONSTANT: Allow CSV Export for Reporting - {@link Boolean}, default True
     */
    public static final String ENABLE_CSV_REPORT_EXPORT = "ENABLE_CSV_REPORT_EXPORT:java.lang.Boolean";
    /**
     * CONSTANT: Allow CSV export of list of takers {@link Boolean}, default True
     */
    public static final String ENABLE_LIST_OF_TAKERS_EXPORT = "ENABLE_LIST_OF_TAKERS_EXPORT:java.lang.Boolean";

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
     * CONSTANT: Show the textbox for entering evaluation term IDs when setting up an eval - {@link Boolean}, default False
     */
    public static final String ENABLE_EVAL_TERM_IDS = "ENABLE_EVAL_TERM_IDS:java.lang.Boolean";
    /**
     * CONSTANT: If this is activated then evaluation responses can be removed,
     * this is for removal of individual responses - {@link Boolean}, default False
     */
    public static final String ENABLE_EVAL_RESPONSE_REMOVAL = "ENABLE_EVAL_RESPONSE_REMOVAL:java.lang.Boolean";
    /**
     * Enable evaluation early closing, this allows the owners and admins to close evaluations early
     */
    public static final String ENABLE_EVAL_EARLY_CLOSE = "ENABLE_EVAL_EARLY_CLOSE:java.lang.Boolean";
    /**
     * Enable evaluation reopening, this allows the owners and admins to reopen an evaluation
     * after it closes
     */
    public static final String ENABLE_EVAL_REOPEN = "ENABLE_EVAL_REOPEN:java.lang.Boolean";

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
     * CONSTANT: Show the controls and the options for scheduling synchronization of group memberships with external provider - {@link Boolean}, default False
     */
    public static final String ENABLE_PROVIDER_SYNC = "ENABLE_PROVIDER_SYNC:java.lang.Boolean";

    /**
     * CONSTANT: Show the email controls and the email options for one-email-per-student notification - {@link Boolean}, default False
     */
    public static final String ENABLE_SINGLE_EMAIL_PER_STUDENT = "ENABLE_SINGLE_EMAIL_PER_STUDENT:java.lang.Boolean";

	/**
	 * CONSTANT: Enable an email to be sent at completion of an evaluation job - {@link Boolean}, default False
     */
    public static final String ENABLE_JOB_COMPLETION_EMAIL = "ENABLE_JOB_COMPLETION_EMAIL:java.lang.Boolean";
    
	/**
	 * CONSTANT: Enable a status of which group you are processing in the email reminder job - {@link Boolean}, default False
     */
    public static final String ENABLE_REMINDER_STATUS = "ENABLE_REMINDER_STATUS:java.lang.Boolean";
    
    /**
     * CONSTANT: Frequency of reminders when one email per student is set. - {@link Integer},default 0}
     */
    public static final String SINGLE_EMAIL_REMINDER_DAYS = "SINGLE_EMAIL_REMINDER_DAYS:java.lang.Integer";
    /**
     * CONSTANT: Email delivery options: EvalConstants.MAIL_DELIVERY_OPTION_NONE|EMAIL_DELIVERY_OPTION_SEND|EMAIL_DELIVERY_OPTION_LOG - {@link String}, 
     * default EMAIL_DELIVERY_OPTION_SEND
     */
    public static final String EMAIL_DELIVERY_OPTION = "EMAIL_DELIVERY_OPTION:java.lang.String";
    /**
     * CONSTANT: The number of emails to process in a batch before pausing EMAIL_WAIT_INTERVAL seconds. 0 causes 1 batch with all. - {@link Integer},default 0}
     */
    public static final String EMAIL_BATCH_SIZE = "EMAIL_BATCH_SIZE:java.lang.Integer";
    /**
     * CONSTANT: The number of seconds to wait before processing another EMAIL_BATCH_SIZE emails. 0 causes no wait between batches. - {@link Integer},default 0}
     */
    public static final String EMAIL_WAIT_INTERVAL = "EMAIL_WAIT_INTERVAL:java.lang.Integer";
    /**
     * CONSTANT: Log the email addresses of users receiving email. - {@link Boolean}, default False
     */
    public static final String LOG_EMAIL_RECIPIENTS = "LOG_EMAIL_RECIPIENTS:java.lang.Boolean";
    /**
     * CONSTANT: Log a message every LOG_PROGRESS_EVERY operations during batch operations. 0 cause no logging of progress messages.- {@link Integer},default 0}
     */
    public static final String LOG_PROGRESS_EVERY = "LOG_PROGRESS_EVERY:java.lang.Integer";

    /**
     * If this is enabled then we are allow the user to create their own adhoc groups and we search
     * the adhoc groups and treat them as normal eval groups, otherwise adhoc groups
     * are ignored and the user cannot create them
     */
    public static final String ENABLE_ADHOC_GROUPS = "ENABLE_ADHOC_GROUPS:java.lang.Boolean";
    /**
     * If this is enabled then we are allow the user to create their own adhoc users and we search
     * the adhoc users and treat them as normal eval users, otherwise adhoc users
     * are ignored and the user cannot create them<br/>
     * In other words, the user can only add existing users to their adhoc groups (if those are enabled),
     * it makes little sense to enable this if adhoc groups are off
     */
    public static final String ENABLE_ADHOC_USERS = "ENABLE_ADHOC_USERS:java.lang.Boolean";

    /**
     * If this is enabled then users will be allowed to use Item Bank features 
     */
    public static final String DISABLE_ITEM_BANK = "DISABLE_ITEM_BANK:java.lang.Boolean";

    /**
     * If this is enabled then users will be allowed group "rating questions" together into a question block
     */
    public static final String DISABLE_QUESTION_BLOCKS = "DISABLE_QUESTION_BLOCKS:java.lang.Boolean";

    /**
     * If this is enabled then the assistant category will be a valid choice
     */
    public static final String ENABLE_ASSISTANT_CATEGORY = "ENABLE_ASSISTANT_CATEGORY:java.lang.Boolean";

    /**
     * If this is true then the admin email address is used when sending eval emails,
     * otherwise the system help address (or the configured help address if set) will be used <br/>
     * https://bugs.caret.cam.ac.uk/browse/CTL-1525
     */
    public static final String USE_ADMIN_AS_FROM_EMAIL = "USE_ADMIN_AS_FROM_EMAIL:java.lang.Boolean";
    /**
     * CONSTANT: Default frequency of reminders when evals are created - {@link Integer},default 1}
     */
    public static final String DEFAULT_EMAIL_REMINDER_FREQUENCY = "DEFAULT_EMAIL_REMINDER_FREQUENCY:java.lang.Integer";
    /**
     * CONSTANT: Time from when evaluation is created until the first notification event is triggered - {@link Integer},default 300}
     * linked to {@link EvalConstants#EVALUATION_TIME_TO_WAIT_SECS}
     */
    public static final String EVALUATION_TIME_TO_WAIT_SECS = "EVALUATION_TIME_TO_WAIT_SECS:java.lang.Integer";
    
    /**
     * If this is enabled then users will be able to see and choose the instructor and assistant selection
     * choices in the eval settings and eval assign views, otherwise the default is used
     */
    public static final String ENABLE_INSTRUCTOR_ASSISTANT_SELECTION = "ENABLE_INSTRUCTOR_ASSISTANT_SELECTION:java.lang.Boolean";

    /**
     * Enable the check to filter out sites (groups) which are not published when selecting groups to assign. - {@link Boolean}, default False
     */
    public static final String ENABLE_SITE_GROUP_PUBLISH_CHECK = "ENABLE_SITE_GROUP_PUBLISH_CHECK:java.lang.Boolean";

    /**
     * If this is enabled/true then we only show evalgroups/sites like the current one in the Assign Eval to groups view
     */
    public static final String ENABLE_FILTER_ASSIGNABLE_GROUPS = "ENABLE_FILTER_ASSIGNABLE_GROUPS:java.lang.Boolean";
    
    /**
     * if this is enabled/true, users can decide to disable the email notification sent out when the eval begins
     */
    public static final String ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION = "ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION:java.lang.Boolean";
        
    public static final String ALLOW_ALL_SITE_ROLES_TO_RESPOND = "ALLOW_ALL_SITE_ROLES_TO_RESPOND:java.lang.Boolean";
    /**
     * if this is enabled/true, users who can see evaluation results will not be
     * restricted by a view/due/stop date 
     */
    public static final String VIEW_SURVEY_RESULTS_IGNORE_DATES = "VIEW_SURVEY_RESULTS_IGNORE_DATES:java.lang.Boolean";
    
    /**
     * CONSTANT: Send an email confirmation that a response to an evaluation was submitted (Instructors might require proof). - {@link Boolean}, default False
     */
    public static final String ENABLE_SUBMISSION_CONFIRMATION_EMAIL = "ENABLE_SUBMISSION_CONFIRMATION_EMAIL:java.lang.Boolean";
    
/**
     * CONSTANT: Send an email notification when an evaluation opens to evaluatees. - {@link Boolean}, default False
     */
    public static final String ENABLE_SUBMISSION_EVALUATEE_EMAIL = "ENABLE_SUBMISSION_EVALUATEE_EMAIL:java.lang.Boolean";
    
    /**
     * The time and date of the next scheduled reminder.
     */
	public static final String NEXT_REMINDER_DATE = "NEXT_REMINDER_DATE:java.util.Date";
	
	/**
	 * The hour of the day (24-hour clock, local time) at which processing of consolidated emails should begin each day. Default: 1 (meaning 1 a.m. local time)
	 */
	public static final String CONSOLIDATED_EMAIL_DAILY_START_TIME = "CONSOLIDATED_EMAIL_DAILY_START_TIME:java.lang.Integer";
	
	/**
	 * The minutes after the hour specified for the hour of the day at which processing of consolidated emails should begin.  Default: 10
	 */
	public static final String CONSOLIDATED_EMAIL_DAILY_START_MINUTES = "CONSOLIDATED_EMAIL_DAILY_START_MINUTES:java.lang.Integer";

	/**
	 * If this is true, available emails should be sent to notify recipients when evals open. Otherwise available emails will not be sent.
	 */
	public static final String CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE = "CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE:java.lang.Boolean";

	/**
	 * EVALSYS-1236
	 * If this is true, send a created e-mail, even if the instructor cannot 
	 * modify the evaluation.  Only applies if the email settings are set to 
	 * Consolidated emails
	 */
	public static final String CONSOLIDATED_FORCE_SEND_CREATED_EMAIL = "CONSOLIDATED_FORCE_SEND_CREATED_EMAIL:java.lang.Boolean";
	
	/**
	 * EVALSYS-1236
	 * If this is true, send an evaluation available e-mail.  Only applies if 
	 * the email settings are set to Consolidated emails.
	 */
	public static final String CONSOLIDATED_FORCE_SEND_AVAILABLE_NOTIFICATION = "CONSOLIDATED_FORCE_SEND_AVAILABLE_NOTIFICATION:java.lang.Boolean";
	
    /**
     * If true, memberships in EvalAssignGroup will be synchronized with the group provider when a new EvalAssignGroup is saved.  Default is true.
     */
	public static final String SYNC_USER_ASSIGNMENTS_ON_GROUP_SAVE = "SYNC_USER_ASSIGNMENTS_ON_GROUP_SAVE:java.lang.Boolean";
	
    /**
     * If true, memberships in EvalAssignGroup will be synchronized with the group provider when properties of an existing EvalAssignGroup are updated.  Default is false.
     */
	public static final String SYNC_USER_ASSIGNMENTS_ON_GROUP_UPDATE = "SYNC_USER_ASSIGNMENTS_ON_GROUP_UPDATE:java.lang.Boolean";
	
    /**
     * If true, memberships in EvalAssignGroup will be synchronized with the group provider when the state of an eval changes.  Default is false.
     */
	public static final String SYNC_USER_ASSIGNMENTS_ON_STATE_CHANGE = "SYNC_USER_ASSIGNMENTS_ON_STATE_CHANGE:java.lang.Boolean";
	
    /**
     * If true, memberships in all unassigned EvalAssignGroup will be synchronized with the group provider on server startup.  Default is true.
     */
	public static final String SYNC_UNASSIGNED_GROUPS_ON_STARTUP = "SYNC_UNASSIGNED_GROUPS_ON_STARTUP:java.lang.Boolean";
	
	/**
	 * If this is set, it identifies the server that should handle syncing of evaluations. Otherwise, more than one server may try to sync.  Default is null.
	 */
	public static final String SYNC_SERVER = "SYNC_SERVER:java.lang.String";

	/**
	 * If this is set then the local CSS file identified at the given path will be loaded on all pages,
	 * if not set or blank or null then this does nothing
	 */
    public static final String LOCAL_CSS_PATH = "LOCAL_CSS_PATH:java.lang.String";


    /**
     * This is here to identify all the boolean ONLY settings in the system,
     * this does not include Booleans which are configurable (i.e. can be set to null, true, or false)
     * but only includes the booleans which are always true or false
     */
    public static String[] BOOLEAN_SETTINGS = {
        ADMIN_VIEW_BELOW_RESULTS,
        ADMIN_VIEW_INSTRUCTOR_ADDED_RESULTS,
        ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION,
        CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE,
        CONSOLIDATED_FORCE_SEND_CREATED_EMAIL,
        CONSOLIDATED_FORCE_SEND_AVAILABLE_NOTIFICATION,
        DISABLE_ITEM_BANK,
        DISABLE_QUESTION_BLOCKS,
        DISPLAY_HIERARCHY_HEADERS,
        DISPLAY_HIERARCHY_OPTIONS,
        ENABLE_ADMINISTRATING_BOX,
        ENABLE_ADHOC_GROUPS,
        ENABLE_ADHOC_USERS,
        ENABLE_ASSISTANT_CATEGORY,
        ENABLE_CSV_REPORT_EXPORT,
        ENABLE_EVAL_CATEGORIES,
        ENABLE_EVAL_TERM_IDS,
        ENABLE_EVAL_EARLY_CLOSE,
        ENABLE_EVAL_REOPEN,
        ENABLE_EVAL_RESPONSE_REMOVAL,
        ENABLE_EVALUATEE_BOX,
        ENABLE_FILTER_ASSIGNABLE_GROUPS,
        ENABLE_GROUP_SPECIFIC_PREVIEW,
        ENABLE_IMPORTING,
        ENABLE_INSTRUCTOR_ASSISTANT_SELECTION,
        ENABLE_ITEM_COMMENTS,
        ENABLE_JOB_COMPLETION_EMAIL,
        ENABLE_LIST_OF_TAKERS_EXPORT,
        ENABLE_SAKAI_ADMIN_ACCESS,
        ENABLE_MY_TOPLINKS,
        ENABLE_NOT_AVAILABLE,
        ENABLE_PDF_REPORT_BANNER,
        ENABLE_PDF_REPORT_EXPORT,
        ENABLE_PROVIDER_SYNC,
        ENABLE_REMINDER_STATUS,
        ENABLE_SINGLE_EMAIL_PER_STUDENT,
        ENABLE_SITE_GROUP_PUBLISH_CHECK,
        ENABLE_SUBMISSION_CONFIRMATION_EMAIL,
        ENABLE_SUBMISSION_EVALUATEE_EMAIL,
        ENABLE_SUMMARY_SITES_BOX,
        ENABLE_TEMPLATE_COPYING,
        ENABLE_XLS_REPORT_EXPORT,
        EVAL_USE_DATE_TIME,
        EVAL_USE_SAME_VIEW_DATES,
        EVAL_USE_STOP_DATE,
        EVAL_USE_VIEW_DATE,
        INSTRUCTOR_ALLOWED_EMAIL_STUDENTS,
        INSTRUCTOR_ALLOWED_VIEW_RESPONDERS,
        INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS,
        ITEM_USE_RESULTS_SHARING,
        ITEM_USE_COURSE_CATEGORY_ONLY,
        LOG_EMAIL_RECIPIENTS,
        REQUIRE_COMMENTS_BLOCK,
        STUDENT_SAVE_WITHOUT_SUBMIT,
        STUDENT_CANCEL_ALLOWED,
        SYNC_USER_ASSIGNMENTS_ON_GROUP_SAVE,
        SYNC_USER_ASSIGNMENTS_ON_GROUP_UPDATE,
        SYNC_USER_ASSIGNMENTS_ON_STATE_CHANGE,
        SYNC_UNASSIGNED_GROUPS_ON_STARTUP,
        USE_ADMIN_AS_FROM_EMAIL,
        USE_EXPERT_ITEMS,
        USE_EXPERT_TEMPLATES,
        VIEW_SURVEY_RESULTS_IGNORE_DATES
    };

    /**
     * This is here to identify all the booleans in the system which are ternary booleans,
     * that is to say it can store true, false, and "configurable" (null),
     * all booleans should be indicated in either this or {@link #BOOLEAN_SETTINGS}
     */
    public static String[] TERNARY_BOOLEAN_SETTINGS = {
        ALLOW_ALL_SITE_ROLES_TO_RESPOND,
        INSTRUCTOR_ALLOWED_VIEW_RESULTS,
        INSTRUCTOR_ALLOWED_VIEW_ALL_RESULTS,
        STUDENT_ALLOWED_LEAVE_UNANSWERED,
        STUDENT_MODIFY_RESPONSES,
        STUDENT_ALLOWED_VIEW_RESULTS
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

    /**
     * Clear items from the settings cache <br/>
     * This will clear out the cache and reload all config settings if the settingConstant is null,
     * if not null then clean out a single item from the cache 
     * (it will be reloaded from the DB the next time someone attempts to fetch it)
     * 
     * @param settingConstant (OPTIONAL) a setting constant from {@link EvalSettings} OR null to clear all
     */
    public void resetCache(String settingConstant);

}
