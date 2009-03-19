/**
 * $Id$
 * $URL$
 * EvalConstants.java - evaluation - April 11, 2006 11:06:08 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.constant;

/**
 * Stores constants for use through the Evaluation services, logic layer, and dao layer
 * Render constants should not be stored here
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalConstants {

    /**
     * The current version of the application for updating purposes,
     * this should always be the NEXT version of the tool if this is trunk code
     */
    public static String APP_VERSION = "1.2.2-RC3"; 

    public static String SVN_REVISION = "$Revision$";
    public static String SVN_LAST_UPDATE = "$Date$";


    /**
     * Evaluation type: EVALUATION This is the standard type of evaluation for course/program assessment
     */
    public static final String EVALUATION_TYPE_EVALUATION = "Evaluation";
    /**
     * Evaluation type: SURVEY This is the survey type for use in non-critical assessment or surveying
     */
    public static final String EVALUATION_TYPE_SURVEY = "Survey";
    /**
     * Evaluation type: POLL This is the poll type for use single item polling
     */
    public static final String EVALUATION_TYPE_POLL = "Poll";


    /**
     * Eval Takers: Include all users who have not taken the evaluation yet
     */
    public final static String EVAL_INCLUDE_NONTAKERS = "nontakers";
    /**
     * Eval Takers: Include all users who have responded to the evaluation
     */
    public final static String EVAL_INCLUDE_RESPONDENTS = "respondents";
    /**
     * Eval Takers: Include all users for the evaluation
     */
    public final static String EVAL_INCLUDE_ALL = "all";


    /**
     * Evaluation state: Cannot determine the state, this evaluation is invalid in some way
     */
    public static final String EVALUATION_STATE_UNKNOWN = "UNKNOWN";
    /**
     * Evaluation state: evaluation is being created but user has not completed the creation
     * process, evaluations in the partial state will be purged out of the system if they are
     * not completed to UnQueue within 24 hours
     * <br/>States: Partial -> InQueue -> Active -> GracePeriod -> Closed -> Viewable (-> Deleted)
     */
    public static final String EVALUATION_STATE_PARTIAL = "Partial";
    /**
     * Evaluation state: evaluation has not started yet, should be the state of evaluations
     * when they are first created, can make any change to the evaluation while in this state
     * <br/>States: Partial -> InQueue -> Active -> GracePeriod -> Closed -> Viewable (-> Deleted)
     */
    public static final String EVALUATION_STATE_INQUEUE = "InQueue";
    /**
     * Evaluation state: evaluation is currently running, users can take the evaluation,
     * start date cannot be modified anymore, email templates cannot be modified,
     * cannot unlink groups of takers at this point, can still add in groups
     * <br/>States: Partial -> InQueue -> Active -> GracePeriod -> Closed -> Viewable (-> Deleted)
     */
    public static final String EVALUATION_STATE_ACTIVE = "Active";
    /**
     * Evaluation state: evaluation is over but not technically, no more notifications
     * will be displayed and links are no longer shown, however, takers
     * can still complete the evaluation until the state changes to closed,
     * evaluations in Due status are shown as closed in the interface
     * <br/>States: Partial -> InQueue -> Active -> GracePeriod -> Closed -> Viewable (-> Deleted)
     */
    public static final String EVALUATION_STATE_GRACEPERIOD = "Due";
    /**
     * Evaluation state: evaluation is over and closed,
     * users cannot take evaluation anymore, no changes can be made to the evaluation
     * except to adjust the results view date, cannot add or remove groups of takers
     * <br/>States: Partial -> InQueue -> Active -> GracePeriod -> Closed -> Viewable (-> Deleted)
     */
    public static final String EVALUATION_STATE_CLOSED = "Closed";
    /**
     * Evaluation state: evaluation is over and closed and results are generally viewable,
     * no changes can be made to the evaluation at all
     * <br/>States: Partial -> InQueue -> Active -> GracePeriod -> Closed -> Viewable (-> Deleted)
     */
    public static final String EVALUATION_STATE_VIEWABLE = "Viewable";
    /**
     * Evaluation state: evaluation is removed and can no longer be accessed by the user,
     * evaluation data is maintained while in this state and locked templates/items/scales
     * cannot unlock but evaluation data is maintained<br/>
     * Ideally we will figure out a way to unlock the items (perhaps by only maintaining generated
     * reports?)
     * <br/>States: Partial -> InQueue -> Active -> GracePeriod -> Closed -> Viewable (-> Deleted)
     */
    public static final String EVALUATION_STATE_DELETED = "Deleted";

    /**
     * This defines the correct order to move through the evaluation states
     * <br/>States: Partial -> InQueue -> Active -> GracePeriod -> Closed -> Viewable (-> Deleted)
     */
    public static final String[] STATE_ORDER = {
        EVALUATION_STATE_PARTIAL,
        EVALUATION_STATE_INQUEUE,
        EVALUATION_STATE_ACTIVE,
        EVALUATION_STATE_GRACEPERIOD,
        EVALUATION_STATE_CLOSED,
        EVALUATION_STATE_VIEWABLE,
        EVALUATION_STATE_DELETED
    };


    /**
     * Evaluation authentication control: Authentication required to access this evaluation
     */
    public static final String EVALUATION_AUTHCONTROL_AUTH_REQ = "AUTH";
    /**
     * Evaluation authentication control: Authentication key required to access this evaluation (no central auth needed)
     */
    public static final String EVALUATION_AUTHCONTROL_KEY = "KEY";
    /**
     * Evaluation authentication control: Authentication not required to access this evaluation (anonymous allowed)
     */
    public static final String EVALUATION_AUTHCONTROL_NONE = "NONE";

    /**
     * AutoUse items will be inserted before existing items when they are added
     * to the template for the evaluation
     */
    public static final String EVALUATION_AUTOUSE_INSERTION_BEFORE = "Before";
    /**
     * AutoUse items will be inserted after existing items when they are added
     * to the template for the evaluation
     */
    public static final String EVALUATION_AUTOUSE_INSERTION_AFTER = "After";


    /**
     * Template type: this is a normal type of template created by a user
     * and used to start evaluations
     */
    public final static String TEMPLATE_TYPE_STANDARD = "standard";
    /**
     * Template type: this is an added items template and only used for storing
     * items added to an evaluation, it cannot be used to start an evaluation
     */
    public final static String TEMPLATE_TYPE_ADDED = "added";

    /**
     * Hierarchy node: this is a special case id which indicates that
     * this is not assigned to a node in the hierarchy
     */
    public final static String HIERARCHY_NODE_ID_NONE = "000topid";
    /**
     * Hierarchy level: this is a special case level which indicates that
     * these things are at the top level of the hierarchy (nothing is above),
     * use the constant as the node id {@link #HIERARCHY_NODE_ID_NONE}
     */
    public final static String HIERARCHY_LEVEL_TOP = "toplevel";
    /**
     * Hierarchy level: this is the non-special case level that all items 
     * not at the special levels for instructor added items should
     * have, the node id would be an actual hierarchy node id
     */
    public final static String HIERARCHY_LEVEL_NODE = "nodelevel";
    /**
     * Hierarchy level: instructor level instructor added items exist at this special level,
     * the userId of the instructor should be used as the node id
     */
    public final static String HIERARCHY_LEVEL_INSTRUCTOR = "instructor";
    /**
     * Hierarchy level: group level instructor added items exist at this at this special level,
     * the groupId of the group should be used as the node id
     */
    public final static String HIERARCHY_LEVEL_GROUP = "group";


    /**
     * Hierarchy Permission:
     * User may view all data at the associated hierarchy node
     * but none of the data below this node,
     * user has no power to change any data, data includes: 
     * evaluations, eval results, templates, items, scales
     */
    public static final String HIERARCHY_PERM_VIEW_NODE_DATA = "HierarchyViewNodeData";
    /**
     * Hierarchy Permission:
     * User may view all data at the associated hierarchy node and below,
     * user has no power to change any data, data includes: 
     * evaluations, eval results, templates, items, scales
     */
    public static final String HIERARCHY_PERM_VIEW_TREE_DATA = "HierarchyViewTreeData";
    /**
     * Hierarchy Permission:
     * User may control all templates data at the associated hierarchy node only
     * (not including any data associated with a node below this one),
     * this includes templates, items, and scales
     */ 
    public static final String HIERARCHY_PERM_CONTROL_NODE_DATA = "HierarchyControlNodeData";
    /**
     * Hierarchy Permission:
     * User may control all templates data at the associated hierarchy node and below,
     * this includes templates, items, and scales
     */ 
    public static final String HIERARCHY_PERM_CONTROL_TREE_DATA = "HierarchyControlTreeData";
    /**
     * Hierarchy Permission:
     * User may assign an evaluation to the associated hierarchy node and any 
     * nodes or eval groups below it, user may also control any existing evalaution
     * at this current hierarchy node (but not below it)
     */
    public static final String HIERARCHY_PERM_ASSIGN_EVALUATION = "HierarchyAssignEval";


    /**
     * Permission: User can create, update, delete evaluation templates
     */
    public final static String PERM_WRITE_TEMPLATE = "eval.write.template";
    /**
     * Permission: User can assign evaluations to any groups they have this permission in,
     * it means they can see the groups in the listing when assigning the eval to users via groups<br/>
     * NOTE: users cannot create evals unless they have this permission in at least one site
     */
    public final static String PERM_ASSIGN_EVALUATION = "eval.assign.evaluation";
    /**
     * Permission: User can be evaluated for any group they have this permission in,
     * NOTE: this used to be combined with {@link #PERM_EVALUATE_GROUP}
     */
    public final static String PERM_BE_EVALUATED = "eval.be.evaluated";
    /**
     * Permission: User can take an evaluation for any group they have this permission in
     */
    public final static String PERM_TAKE_EVALUATION = "eval.take.evaluation";
    /**
     * SPECIAL CASE
     * Permission: User is marked as an assistant in a section/course/group,
     * this is using the permission which was defined by the Sakai sections stuff
     * http://bugs.sakaiproject.org/jira/browse/EVALSYS-345
     */
    public final static String PERM_ASSISTANT_ROLE = "section.role.ta";


    /**
     * EvalUser class: Unknown type
     */
    public final static String USER_TYPE_UNKNOWN = "Unknown";
    /**
     * EvalUser class: Invalid user (this user could not be found in the system)
     */
    public final static String USER_TYPE_INVALID = "Invalid";
    /**
     * EvalUser class: Anonymous user who does not have an account or is not identified
     */
    public final static String USER_TYPE_ANONYMOUS = "Anonymous";
    /**
     * EvalUser class: Internal user, this is probably an ad-hoc user or maybe the admin user
     */
    public final static String USER_TYPE_INTERNAL = "Internal";
    /**
     * EvalUser class: External user, this user comes in via a provider or from Sakai
     */
    public final static String USER_TYPE_EXTERNAL = "External";


    /**
     * EvalGroup class: Unknown type
     */
    public final static String GROUP_TYPE_UNKNOWN = "Unknown";
    /**
     * EvalGroup class: Invalid group (this group could not be found in the system)
     */
    public final static String GROUP_TYPE_INVALID = "Invalid";
    /**
     * EvalGroup class: Site type (represents a course or project site)
     */
    public final static String GROUP_TYPE_SITE = "Site";
    /**
     * EvalGroup class: Group type (represents a subgroup within a site)
     */
    public final static String GROUP_TYPE_GROUP = "Group";
    /**
     * EvalGroup class: Provided type (represents an eval group from a provider)
     */
    public final static String GROUP_TYPE_PROVIDED = "Provided";
    /**
     * EvalGroup class: Adhoc type (represents an adhoc group of users)
     */
    public final static String GROUP_TYPE_ADHOC = "Adhoc";

    /**
     * Scale ideal setting: no selection of this scale is the ideal one
     */
    public static final String SCALE_IDEAL_NONE = null;
    /**
     * Scale ideal setting: the lowest (first) selection of this scale is the ideal one
     */
    public static final String SCALE_IDEAL_LOW = "low";
    /**
     * Scale ideal setting: the middle selection of this scale is the ideal one
     */
    public static final String SCALE_IDEAL_MID = "mid";
    /**
     * Scale ideal setting: the highest (last) selection of this scale is the ideal one
     */
    public static final String SCALE_IDEAL_HIGH = "high";
    /**
     * Scale ideal setting: the lowest (first) or highest (last) selections of this scale are the ideal ones
     */
    public static final String SCALE_IDEAL_OUTSIDE = "outside";

    /**
     * Scale mode: SCALE - this scale is a reusable scale which is used in SCALED items,
     * this is the default and will appear in scale dropdowns
     */
    public static final String SCALE_MODE_SCALE = "scale";
    /**
     * Scale mode: ADHOC - this scale is an adhoc set of text data that are used for a MultipleChoice or MultipleAnswer item,
     * this will not appear in any lists and is not shareable
     */
    public static final String SCALE_MODE_ADHOC = "adhoc";


    /**
     * This is the default title to use for adhoc scales (which do not use the title for identification),
     * this will be replaced when the scale is saved by a unique one
     */
    public static final String SCALE_ADHOC_DEFAULT_TITLE = "*AdhocScale*";


    /**
     * Item type (itemClassification) setting:
     * This is a scaled (likert) type item<br/>
     * <b>Note:</b> Scaled items could be a block type item, 
     * blocks are a special item type which defines
     * a chunk of items which all use the same scale
     */
    public static final String ITEM_TYPE_SCALED = "Scaled";
    /**
     * Item type (itemClassification) setting:
     * This is a multiple choice type item<br/>
     * it uses an adhoc scale (though it can use any scale),
     * only one answer may be selected
     */
    public static final String ITEM_TYPE_MULTIPLECHOICE = "MultipleChoice";
    /**
     * Item type (itemClassification) setting:
     * This is a multiple answer type item<br/>
     * it uses an adhoc scale (though it can use any scale),
     * zero to many answers may be selected
     */
    public static final String ITEM_TYPE_MULTIPLEANSWER = "MultipleAnswer";
    /**
     * Item type (itemClassification) setting:
     * This is a textual/essay type item
     */
    public static final String ITEM_TYPE_TEXT = "Essay";
    /**
     * Item type (itemClassification) setting:
     * This is a header type item, it is only used to customize the look of the
     * template by providing for a place to add instructions or divisions or titles,
     * does not count as an actual question item
     */
    public static final String ITEM_TYPE_HEADER = "Header";
    /**
     * Item type (itemClassification) setting:
     * <b>Note:</b> This is a special type for rendering blocks only 
     * and identifies this as a block parent, generally this should
     * only be set when creating item blocks and should not be used otherwise
     * (see implementation notes for details on blocks)
     */
    public static final String ITEM_TYPE_BLOCK_PARENT = "BlockParent";
    /**
     * Item type (itemClassification) setting:
     * <b>Note:</b> This is a special type for identifying block children only, 
     * if you attempt to save an item or templateItem with this type 
     * it will fail, only use this in the presentation layer
     * (see implementation notes for details on blocks)
     */
    public static final String ITEM_TYPE_BLOCK_CHILD = "BlockChild";


    /**
     * Item category (category) setting:
     * This item is in the course category and will be listed like 
     * normal when the evaluation is rendered for the takers
     */
    public static final String ITEM_CATEGORY_COURSE = "Course";
    /**
     * Item category (category) setting:
     * This item is in the instructor category and will be repeated
     * for each user who can be evaluated in the evaluation group when the 
     * evaluation is rendered for the takers, the user id of the instructor
     * should be bound to associated answers
     */
    public static final String ITEM_CATEGORY_INSTRUCTOR = "Instructor";
    /**
     * Item category (category) setting:
     * This item is in the assistant (teaching/lab) category and will be repeated
     * for each user who is marked as an assistant
     */
    public static final String ITEM_CATEGORY_ASSISTANT = "Assistant";
    /**
     * Item category (category) setting:
     * This item is in the environment category and will be repeated
     * for each environment setup for the evaluation group when the 
     * evaluation is rendered for the takers, a unique id for that
     * environment should be bound to associated answers
     */
    public static final String ITEM_CATEGORY_ENVIRONMENT = "Environment";

    /**
     * Defines the correct rendering order for the item categories,
     * also defines the complete list of item categories<br/>
     * <b>NOTE:</b> make sure all valid item categories are listed here
     */
    public static final String[] ITEM_CATEGORY_ORDER = {
        ITEM_CATEGORY_COURSE,
        ITEM_CATEGORY_INSTRUCTOR,
        ITEM_CATEGORY_ASSISTANT,
        ITEM_CATEGORY_ENVIRONMENT
    };


    /**
     * Item scale display (scaleDisplaySetting) setting:
     * Compact scale is displayed left to right on a single line with labels on each end
     */
    public static final String ITEM_SCALE_DISPLAY_COMPACT = "Compact";
    /**
     * Item scale display (scaleDisplaySetting) setting:
     * Compact scale is displayed left to right on a single line with labels on each end, 
     * colors are applied based on the Scale ideal
     */
    public static final String ITEM_SCALE_DISPLAY_COMPACT_COLORED = "CompactColored";
    /**
     * Item scale display (scaleDisplaySetting) setting:
     * Full scale is displayed left to right on a single line with labels above each point
     */
    public static final String ITEM_SCALE_DISPLAY_FULL = "Full";
    /**
     * Item scale display (scaleDisplaySetting) setting:
     * Full scale is displayed left to right on a single line with labels above each point, 
     * colors are applied based on the Scale ideal
     */
    public static final String ITEM_SCALE_DISPLAY_FULL_COLORED = "FullColored";
    /**
     * Item scale display (scaleDisplaySetting) setting:
     * Stepped scale is displayed left to right on a single line with labels in a stepped pattern above the points
     */
    public static final String ITEM_SCALE_DISPLAY_STEPPED = "Stepped";
    /**
     * Item scale display (scaleDisplaySetting) setting:
     * Stepped scale is displayed left to right on a single line with labels in a stepped pattern above the points,
     * colors are applied based on the Scale ideal
     */
    public static final String ITEM_SCALE_DISPLAY_STEPPED_COLORED = "SteppedColored";
    /**
     * Item scale display (scaleDisplaySetting) setting:
     * Vertical scale is displayed top to bottom with the labels to the right of each point,
     * NO colored option is available with this type of scale
     */
    public static final String ITEM_SCALE_DISPLAY_VERTICAL = "Vertical";

    /**
     * Template/Item shared setting: Template/Item is controlled by the owner<br/>
     * (This is a special setting and only used for setting the system setting, don't
     * store this setting in a template or item, it makes no sense)<br/>
     * When checking for the system setting, this one allows users to choose the sharing
     * for their template
     */
    public static final String SHARING_OWNER = "owner";
    /**
     * Template/Item shared setting: Template/Item is visible to the owner only (and super admin)
     */
    public static final String SHARING_PRIVATE = "private";
    /**
     * Template/Item shared setting: Template/Item is visible to owner and any eval admins,
     * this is also the configurable option for sharing results
     */
    public static final String SHARING_VISIBLE = "visible";
    /**
     * Template/Item shared setting: Template/Item is visible to owner and any admins at the same level in the hierarchy
     */
    public static final String SHARING_SHARED = "shared";
    /**
     * Template/Item shared setting: Template/Item is visible to owner and anyone else in the system
     * (only a super admin can make a template or item public)
     */
    public static final String SHARING_PUBLIC = "public";

    /**
     * Evaluation instructorOpt setting:
     * Instructors do not use the evaluation assigned from above by default,
     * they must take an action to opt in for the evaluation 
     */
    public static final String INSTRUCTOR_OPT_IN = "optIn";
    /**
     * Evaluation instructorOpt setting:
     * Instructors do use the evaluation assigned from above by default,
     * they must take an action to opt out for the evaluation 
     */
    public static final String INSTRUCTOR_OPT_OUT = "optOut";
    /**
     * Evaluation instructorOpt setting:
     * Instructors must use the evaluation assigned from above by default,
     * they cannot take an action to opt out for the evaluation and must use it
     */
    public static final String INSTRUCTOR_REQUIRED = "Required";


    /**
     * EmailTemplate type setting:
     * This identifies a template as the "evaluation created template",
     * used when the evaluation is first created to notify evaluatees that
     * they may add items to the evaluation and inform them as to when the
     * evaluation starts
     */
    public static final String EMAIL_TEMPLATE_CREATED = "Created";	
    /**
     * EmailTemplate type setting:
     * This identifies a template as the "evaluation available template",
     * used when the evaluation is available for users to take
     */
    public static final String EMAIL_TEMPLATE_AVAILABLE = "Available";
    /**
     * EmailTemplate type setting:
     * This identifies a template as the "instructor must opt in for availability template",
     * used when the evaluation is available for users to take
     */
    public static final String EMAIL_TEMPLATE_AVAILABLE_OPT_IN = "OptIn";
    /**
     * EmailTemplate type setting:
     * This identifies a template as the "evaluation reminder template", 
     * used when the evaluation reminder is sent to non-respondent users
     */
    public static final String EMAIL_TEMPLATE_REMINDER = "Reminder";
    /**
     * EmailTemplate type setting:
     * This identifies a template as the "evaluation results template",
     * used when the evaluation results are ready to view
     */
    public static final String EMAIL_TEMPLATE_RESULTS = "Results";
    /**
     * EmailTemplate type setting:
     * This identifies a template as the one-email-per-user evaluation-is-available template,
     * used when the evaluation is available to be taken.
     */
    public static final String EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE = "ConsolidatedAvailable";
    /**
     * EmailTemplate type setting:
     * This identifies a template as the one-email-per-user evaluation reminder template,
     * used when evaluation responses are outstanding.
     */
    public static final String EMAIL_TEMPLATE_CONSOLIDATED_REMINDER = "ConsolidatedReminder";
    /**
     * EmailTemplate type setting:
     * This identifies a template as the one-email-per-user evaluation-is-available subject template,
     * used with the "single-email-per-user" email notification option
     */
    public static final String EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE_SUBJECT = "ConsolidatedAvailSubject";
    /**
     * EmailTemplate type setting:
     * This identifies a template as the one-email-per-user evaluation reminder subject template,
     * used with the "single-email-per-user" email notification option
     */
    public static final String EMAIL_TEMPLATE_CONSOLIDATED_REMINDER_SUBJECT = "ConsolidatedReminderSubject";
    /**
     * Email Delivery option setting:
     * This value is pre-loaded as the default option for email delivery.
     */
    public static final String EMAIL_DELIVERY_DEFAULT = "send";

    /**
     * Email Delivery option setting:
     * This value send email to the Sakai email system. Use this value when you want to send email to users.
     */
    public static final String EMAIL_DELIVERY_SEND = "send";
    /**
     * Email Delivery option setting:
     * This value writes email to the server log. Use this value when you want to check the content of email.
     */
    public static final String EMAIL_DELIVERY_LOG = "log";
    /**
     * Email Delivery option setting:
     * This value prevents email from being sent or logged. Use this value when you want to test safely by disabling sending of email.
     */
    public static final String EMAIL_DELIVERY_NONE = "none";

    /**
     * ItemGroup Type: Category (root group type)<br/>
     * Can contain Objective type groups or Items, must have no parent groups<br/>
     * Category => Objective => Item
     */
    public static final String ITEM_GROUP_TYPE_CATEGORY = "ItemGroupCategory";
    /**
     * ItemGroup Type: Objective (subgroup of Category)<br/>
     * Can contain Items, must have at least one category parent group<br/>
     * Category => Objective => Item
     */
    public static final String ITEM_GROUP_TYPE_OBJECTIVE = "ItemGroupObjective";

    /**
     * The numeric value stored when a student responds N/A
     */
    public static final Integer NA_VALUE = -1;
    /**
     * The number stored for no numeric answer
     */
    public static final Integer NO_NUMERIC_ANSWER = -2;
    /**
     * The number stored for no text answer
     */
    public static final String NO_TEXT_ANSWER = "";
    /**
     * The code string stored for no multiple answer
     */
    public static final String NO_MULTIPLE_ANSWER = "";

    /**
     * ScheduledInvocationManager: ScheduledInvocationCommand jobType
     */
    public static final String JOB_TYPE_CREATED = "scheduledCreated";
    /**
     * ScheduledInvocationManager: ScheduledInvocationCommand jobType
     */
    public static final String JOB_TYPE_ACTIVE = "scheduledActive";
    /**
     * ScheduledInvocationManager: ScheduledInvocationCommand jobType
     */
    public static final String JOB_TYPE_DUE = "scheduledDue";
    /**
     * ScheduledInvocationManager: ScheduledInvocationCommand jobType
     */
    public static final String JOB_TYPE_CLOSED = "scheduledClosed";
    /**
     * ScheduledInvocationManager: ScheduledInvocationCommand jobType
     * When separate student and instructor view date is not set
     */
    public static final String JOB_TYPE_VIEWABLE = "scheduledViewable";
    /**
     * ScheduledInvocationManager: ScheduledInvocationCommand jobType
     * When separate instructor view date is set
     */
    public static final String JOB_TYPE_VIEWABLE_INSTRUCTORS = "scheduledViewableInstructors";
    /**
     * ScheduledInvocationManager: ScheduledInvocationCommand jobType
     * When separate student view date is set
     */
    public static final String JOB_TYPE_VIEWABLE_STUDENTS = "scheduledViewableStudents";
    /**
     * ScheduledInvocationManager: ScheduledInvocationCommand jobType
     */
    public static final String JOB_TYPE_REMINDER = "scheduledReminder";

}
