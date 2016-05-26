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
package org.sakaiproject.evaluation.tool;

import  org.sakaiproject.evaluation.constant.EvalConstants;

/**
 * This class holds the tool constants only, application data constants come from
 * the EvalConstants class in the model<br/>
 * NOTE: Make sure the constants are not already in the EvalConstants class
 * before adding them here
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalToolConstants {

    /**
     * This is the key which represents an unknown item
     */
    public static String UNKNOWN_KEY = "unknown.caps";

    /**
     * The values for all sharing menus
     */
    public static String[] SHARING_VALUES = new String[] {
        EvalConstants.SHARING_PRIVATE, 
        EvalConstants.SHARING_PUBLIC
        //    EvalConstants.SHARING_VISIBLE,
        //    EvalConstants.SHARING_SHARED
    };

    // should match with SHARING_VALUES
    public static String[] SHARING_LABELS_PROPS = new String[] {
        "sharing.private",
        "sharing.public"
        //    "sharing.visible",
        //    "sharing.shared"
    };

    //For template_modify and preview_item.html
    public static String[] STEPPED_IMAGE_URLS = new String[] {
        "$context/content/images/corner.gif",
        "$context/content/images/down-line.gif",
        "$context/content/images/down-arrow.gif" 
    };

    //For preview_item.html
    public static String[] COLORED_IMAGE_URLS = new String[] {
        "$context/content/images/ideal-none.gif",
        "$context/content/images/ideal-low.jpg",
        "$context/content/images/ideal-mid.jpg",
        "$context/content/images/ideal-high.jpg",
        "$context/content/images/ideal-outside.jpg"
    };

    // should match the images
    public static String BLUE_COLOR = "#d7ebf6";
    public static String GREEN_COLOR = "#8be8a2";
    public static String RED_COLOR = "#ff8ba0";

    public static String LIGHT_BLUE_COLOR = "#CCCCFF";
    public static String LIGHT_GRAY_COLOR = "#E1E1E1";

    // For pulldowns which show the scale display settings
    public static String[] SCALE_DISPLAY_SETTING_VALUES = new String[]{
        EvalConstants.ITEM_SCALE_DISPLAY_COMPACT,
        EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED,
        EvalConstants.ITEM_SCALE_DISPLAY_FULL,
        EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED,
        EvalConstants.ITEM_SCALE_DISPLAY_MATRIX,
        EvalConstants.ITEM_SCALE_DISPLAY_STEPPED,
        EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED,
        EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL
    };

    // should match the order of the array above, should include the properties strings only (no real labels)
    public static String[] SCALE_DISPLAY_SETTING_LABELS_PROPS = new String[] {
        "templateitem.scale.select.compact",
        "templateitem.scale.select.compactc",
        "templateitem.scale.select.full",
        "templateitem.scale.select.fullc",
        "templateitem.scale.select.matrix",
        "templateitem.scale.select.stepped",
        "templateitem.scale.select.steppedc",
        "templateitem.scale.select.vertical"
    };
    
    public static String[] SCALE_DISPLAY_GROUP_SETTING_VALUES = new String[] {
    	EvalConstants.ITEM_SCALE_DISPLAY_MATRIX,
    	EvalConstants.ITEM_SCALE_DISPLAY_STEPPED,
        EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED
    };
    
    public static String[] SCALE_DISPLAY_GROUP_SETTING_LABELS_PROPS = new String[] {
    	"templateitem.scale.select.matrix",
    	"templateitem.scale.select.stepped",
        "templateitem.scale.select.steppedc"
    };


    // For pulldowns which show the multiple choices display settings
    public static String[] CHOICES_DISPLAY_SETTING_VALUES = new String[]{
        EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL,
        EvalConstants.ITEM_SCALE_DISPLAY_FULL
    };

    // should match the order of the array above, should include the properties strings only (no real labels)
    public static String[] CHOICES_DISPLAY_SETTING_LABELS_PROPS = new String[] {
        "templateitem.scale.select.vertical",
        "templateitem.scale.select.full"
    };

    // used for the default category choices
    public static String[] ITEM_CATEGORY_VALUES = new String[] {
        EvalConstants.ITEM_CATEGORY_COURSE,
        EvalConstants.ITEM_CATEGORY_INSTRUCTOR
    };

    // should match ITEM_CATEGORY_VALUES
    public static String[] ITEM_CATEGORY_LABELS_PROPS = {
        "modifyitem.course.category",
        "modifyitem.instructor.category"
    };

    public static String ITEM_CATEGORY_ASSISTANT = EvalConstants.ITEM_CATEGORY_ASSISTANT;
    public static String ITEM_CATEGORY_ASSISTANT_LABEL = "modifyitem.ta.category";

    /**
     * Email Settings: Page pulldown constants for email processing type
     * <ol>
     * <li>Multiple emails per student - one email per evaluation response outstanding.</li>
     * <li>Single email per student - one email if any of a student's responses are outstanding.</li>
     * </ol>
     */
    public static String[] EMAIL_TYPE_VALUES = new String[] {
        "multiple", "single"};
    public static String[] EMAIL_TYPE_LABELS = new String[] {
        "Multiple emails per student - one email per response outstanding.",
        "Single email per student - one email if any of a student's responses are outstanding."
    };
    public static final String SINGLE_EMAIL = "single";
    public static final String MULTIPLE_EMAILS = "multiple";


    /**
     * Email Settings: Page pulldown constants for email delivery options
     * <ol>
     * <li>Send email. This mode should be used in production, when you do want to send email to real users.</li>
     * <li>Log email to the server log. This mode may be used in development to check the content of email messages.</li>
     * <li>Do not send email. This mode may be used for safer testing, when you don't want to accidentally send email to real users.</li>
     * </ol>
     */
    public static String[] EMAIL_DELIVERY_VALUES = new String[] {
        "send", "log", "none"};

    // FIXME this should not be done this way, UM should put this in the messages file -AZ
    public static String[] EMAIL_DELIVERY_LABELS = new String[] {
        "Send email. This mode should be used in production, when you do want to send email to real users.",
        "Log email to the server log. This mode may be used in development to check the content of email messages.", 
    "Do not send email. This mode may be used for safer testing, when you don't want to accidentally send email to real users."};

	public static final String[] PULLDOWN_HOUR_VALUES = new String[] {
		"0","1","2","3","4","5","6","7",
		"8","9","10","11","12","13","14","15",
		"16","17","18","19","20","21","22","23"
	};

	public static final String[] PULLDOWN_HOUR_LABELS = new String[] {
		"controlemail.start.hour.0","controlemail.start.hour.1","controlemail.start.hour.2","controlemail.start.hour.3",
		"controlemail.start.hour.4","controlemail.start.hour.5","controlemail.start.hour.6","controlemail.start.hour.7",
		"controlemail.start.hour.8","controlemail.start.hour.9","controlemail.start.hour.10","controlemail.start.hour.11",
		"controlemail.start.hour.12","controlemail.start.hour.13","controlemail.start.hour.14","controlemail.start.hour.15",
		"controlemail.start.hour.16","controlemail.start.hour.17","controlemail.start.hour.18","controlemail.start.hour.19",
		"controlemail.start.hour.20","controlemail.start.hour.21","controlemail.start.hour.22","controlemail.start.hour.23"
	};
	
	public static final String[] PULLDOWN_MINUTE_VALUES = new String[] {
		"0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"
	};

	public static final String[] PULLDOWN_MINUTE_LABELS = new String[] {
		"controlemail.start.minutes.0", "controlemail.start.minutes.5", "controlemail.start.minutes.10", 
		"controlemail.start.minutes.15", "controlemail.start.minutes.20", "controlemail.start.minutes.25", 
		"controlemail.start.minutes.30", "controlemail.start.minutes.35", "controlemail.start.minutes.40", 
		"controlemail.start.minutes.45", "controlemail.start.minutes.50", "controlemail.start.minutes.55"
	};


    /**
     * Evaluation/Email Settings: Page pulldown constants for reminder interval
     */
    public static final String[] REMINDER_EMAIL_DAYS_VALUES = new String[] {
        "0", "1", "2", "3", "4", "5", "6", "7", "-1" };

    public static final String[] REMINDER_EMAIL_DAYS_LABELS = { 
        "evalsettings.reminder.days.0", 
        "evalsettings.reminder.days.1", 
        "evalsettings.reminder.days.2",
        "evalsettings.reminder.days.3", 
        "evalsettings.reminder.days.4", 
        "evalsettings.reminder.days.5", 
        "evalsettings.reminder.days.6",
        "evalsettings.reminder.days.7",
        "evalsettings.reminder.days.-1"
    };

    /**
     * Defines the allowed values for the Integer constants in batch-related pulldowns
     */
    public static final String[] PULLDOWN_BATCH_VALUES = new String[] {
        "0", "10", "25", "50", "100", "250", "500", "750", "1000"};

    /**
     * Evaluation settings: Values for instructor options for using evaluationSetupService from above
     */
    public static final String[] INSTRUCTOR_OPT_VALUES = new String[] {
        EvalConstants.INSTRUCTOR_OPT_OUT, 
        EvalConstants.INSTRUCTOR_OPT_IN, 
        EvalConstants.INSTRUCTOR_REQUIRED
    };

    public static final String[] INSTRUCTOR_OPT_LABELS = { 
        "evalsettings.instructors.label.opt.out", 
        "evalsettings.instructors.label.opt.in",
        "evalsettings.instructors.label.required"
    };


    /**
     * Modify Essay: Page pulldown constants for response size
     */
    public static final String[] RESPONSE_SIZE_VALUES = new String[] {
        "2", 
        "3", 
        "4", 
        "5"
    };

    public static final String[] RESPONSE_SIZE_LABELS_PROPS = new String[] {
        "templateitem.response.select.size.2",
        "templateitem.response.select.size.3",
        "templateitem.response.select.size.4",
        "templateitem.response.select.size.5"
    };

    /**
     * The default number of rows to use when displaying a textarea type input box
     */
    public static final Integer DEFAULT_ROWS = 2;

    // For main administrative page
    /**
     * Defines the allowed values for the Integer constants in pulldowns
     */
    public static final String[] PULLDOWN_INTEGER_VALUES = new String[] {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "12", "14", "15", "18", "20", "21", "25", "28", "30", "50", "60", "90"};	

    /**
     * Defines the allowed values for minimum time difference (in hours) 
     * between start and due date of an evaluation.
     */
    public static final String[] MINIMUM_TIME_DIFFERENCE = new String[] {
        "0", "1", "2", "4", "8", "12", "16", "20", "24", "36", "48", "96"};	

    /**
     * Defines the allowed values for the auth control pulldown in the evaluation settings
     */
    public static final String[] AUTHCONTROL_VALUES = new String[] {
        EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ,
        //EvalConstants.EVALUATION_AUTHCONTROL_KEY, // CTL-563
        EvalConstants.EVALUATION_AUTHCONTROL_NONE
    };

    public static final String[] AUTHCONTROL_LABELS = { 
        "evalsettings.auth.control.label.required", 
        //"evalsettings.auth.control.label.key", // CTL-563
        "evalsettings.auth.control.label.none"
    };


    /**
     * Administrative (system settings) page, 
     * values corresponding to "Yes", "No", "Configurable"  
     */
    public static final String ADMIN_BOOLEAN_YES = "1";
    public static final String ADMIN_BOOLEAN_NO = "0";
    public static final String ADMIN_BOOLEAN_CONFIGURABLE = "-1";


    // TODO - this is needed to pretend to be null until RSF is fixed up in 0.7.3 (related change below)
    /**
     * This fills in for the real null since real null cannot be passed around
     */
    public static final String NULL = "*NULL*";


    /**
     * Ideal scale values radio buttons (scale add/modify)
     */
    public static final String[] SCALE_IDEA_VALUES = { 
        NULL, // EvalConstants.SCALE_IDEAL_NONE, TODO - undo this when RSF 0.7.3
        EvalConstants.SCALE_IDEAL_LOW, 
        EvalConstants.SCALE_IDEAL_HIGH,
        EvalConstants.SCALE_IDEAL_MID,
        EvalConstants.SCALE_IDEAL_OUTSIDE
    };

    /**
     * Ideal scale values radio button labels (scale add/modify)
     */
    public static final String[] SCALE_IDEAL_LABELS = {
        "controlscales.ideal.scale.option.label.none", 
        "controlscales.ideal.scale.option.label.low",
        "controlscales.ideal.scale.option.label.high", 
        "controlscales.ideal.scale.option.label.mid",
        "controlscales.ideal.scale.option.label.outside" 
    };

    /**
     * The initial values for the options of a scale which is being created
     */
    public static final String[] DEFAULT_INITIAL_SCALE_VALUES = new String[] {"",""};


    /**
     * Used for translating the types into I18n strings
     */
    public static final String[] ITEM_CLASSIFICATION_VALUES = new String[] {
        EvalConstants.ITEM_TYPE_BLOCK_CHILD,
        EvalConstants.ITEM_TYPE_BLOCK_PARENT,
        EvalConstants.ITEM_TYPE_SCALED,
        EvalConstants.ITEM_TYPE_MULTIPLECHOICE,
        EvalConstants.ITEM_TYPE_MULTIPLEANSWER,
        EvalConstants.ITEM_TYPE_TEXT,
        EvalConstants.ITEM_TYPE_HEADER,
    };

    public static final String[] ITEM_CLASSIFICATION_LABELS_PROPS = new String[] {
        "item.classification.scaled", 
        "item.classification.block", 
        "item.classification.scaled", 
        "item.classification.multichoice",
        "item.classification.multianswer",
        "item.classification.text",
        "item.classification.header"
    };

    /**
     * Values for rendering the items types for creating new items
     */
    public static final String[] ITEM_SELECT_CLASSIFICATION_VALUES = new String[] {
        EvalConstants.ITEM_TYPE_SCALED,
        EvalConstants.ITEM_TYPE_MULTIPLECHOICE,
        EvalConstants.ITEM_TYPE_MULTIPLEANSWER,
        EvalConstants.ITEM_TYPE_TEXT,
        EvalConstants.ITEM_TYPE_HEADER
    };

    public static final String[] ITEM_SELECT_CLASSIFICATION_LABELS = new String[] {
        "item.classification.scaled", 
        "item.classification.multichoice",
        "item.classification.multianswer",
        "item.classification.text",
        "item.classification.header"
    };

    /**
     * UMD Specific
     * values for item results sharing. 
     * updated from public/private to Administrative/Student EVALSYS-850
     */
    public static String[] ITEM_RESULTS_SHARING_VALUES = new String[] {
        EvalConstants.SHARING_ADMIN,
        EvalConstants.SHARING_STUDENT
    };

    // should match ITEM_RESULTS_SHARING_VALUES
    public static String[] ITEM_RESULTS_SHARING_LABELS_PROPS = {
    	"modifyitem.results.sharing.admin",
    	"modifyitem.results.sharing.student"
        //"general.public",
        //"general.private"
    };

    /**
     * values for evaluation results sharing.
     */
    public static String[] EVAL_RESULTS_SHARING_VALUES = new String[] {
        EvalConstants.SHARING_PRIVATE,
        EvalConstants.SHARING_VISIBLE,
        EvalConstants.SHARING_PUBLIC
    };

    // should match EVAL_RESULTS_SHARING_VALUES
    public static String[] EVAL_RESULTS_SHARING_LABELS_PROPS = {
        "general.private",
        "general.configurable",
        "general.public"
    };

    // The downloadable results reporting files have a maximum length, before they are chopped off.
    public static int EVAL_REPORTING_MAX_NAME_LENGTH = 40;

    /**
     * values for eval mail notification choices
     */
    public static String[] EVAL_NOTIFICATION_VALUES = new String[] {
        EvalConstants.EVAL_INCLUDE_NONTAKERS,
        EvalConstants.EVAL_INCLUDE_RESPONDENTS,
        EvalConstants.EVAL_INCLUDE_ALL
    };

    /**
     * values for eval mail notification choices
     */
    public static String[] EVAL_NOTIFICATION_LABELS_PROPS = {
        "evalnotify.send.to.non-respond",
        "evalnotify.send.to.responded",
        "evalnotify.send.to.all"
    };
    
    /**
     * This defines the set of all hierarchy permissions. Only the permissions in this
     * array will be displayed on the modify hierarchy node users screen.
     */
    public static final String[] HIERARCHY_PERM_VALUES = {
    	EvalConstants.HIERARCHY_PERM_VIEW_NODE_DATA,
    	EvalConstants.HIERARCHY_PERM_VIEW_TREE_DATA,
    	EvalConstants.HIERARCHY_PERM_CONTROL_NODE_DATA,
    	EvalConstants.HIERARCHY_PERM_CONTROL_TREE_DATA,
    	EvalConstants.HIERARCHY_PERM_ASSIGN_EVALUATION
    };
    
    public static final String[] HIERARCHY_PERM_LABELS = {
    	"modifynodeperms.perm.view.node",
    	"modifynodeperms.perm.view.tree",
    	"modifynodeperms.perm.control.node",
    	"modifynodeperms.perm.control.tree",
    	"modifynodeperms.perm.assign.eval",
    };

    /**
     * Defines the options for the site/section drop down
     */
    public static final String[] HIERARCHY_RULE_OPTION_VALUES =
    {
        EvalConstants.HIERARCHY_RULE_SITE,
        EvalConstants.HIERARCHY_RULE_SECTION
    };

    /**
     * Defines the labels for the site/section drop down
     */
    public static final String[] HIERARCHY_RULE_OPTION_LABELS =
    {
        "modifynoderules.criterion.site.title",
        "modifynoderules.criterion.section.title"
    };

    /**
     * Defines the options for rule qualifier drop down
     */
    public static final String[] HIERARCHY_RULE_QUALIFIER_VALUES = 
    {
        EvalConstants.HIERARCHY_QUALIFIER_CONTAINS,
        EvalConstants.HIERARCHY_QUALIFIER_STARTS_WITH,
        EvalConstants.HIERARCHY_QUALIFIER_ENDS_WITH,
        EvalConstants.HIERARCHY_QUALIFIER_IS
    };

    /**
     * Defines the labels for rule qualifier drop down
     */
    public static final String[] HIERARCHY_RULE_QUALIFIER_LABELS = 
    {
        "modifynoderules.criterion.options.contains",
        "modifynoderules.criterion.options.startsWith",
        "modifynoderules.criterion.options.endsWith",
        "modifynoderules.criterion.options.is"
    };
}
