/**
 * AdministrateProducer.java - evaluation - Sep 18, 2006 11:35:56 AM - azeckoski
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.producers;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.EvalToolConstants;

import uk.org.ponder.beanutil.PathUtil;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBoundList;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles administration of the evaluation system
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class AdministrateProducer implements ViewComponentProducer {

    /**
     * This is used for navigation within the system.
     */
    public static final String VIEW_ID = "administrate";
    public String getViewID() {
        return VIEW_ID;
    }

    // Spring injection 
    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    // Used to prepare the path for WritableBeanLocator
    public static final String ADMIN_WBL = "settingsBean";

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        if (! userAdmin) {
            // Security check and denial
            throw new SecurityException("Non-admin users may not access this page");
        }

        UIMessage.make(tofill, "administrate-title", "administrate.page.title");

        UIMessage.make(tofill, "app_version_revision", "administrate.version.revision", 
                new Object[] {EvalConstants.APP_VERSION, EvalConstants.SVN_REVISION, EvalConstants.SVN_LAST_UPDATE});

        // TOP LINKS
        UIInternalLink.make(tofill, "administrate-link",
                UIMessage.make("administrate.page.title"),
                new SimpleViewParameters(AdministrateProducer.VIEW_ID));

        UIInternalLink.make(tofill, "summary-link", 
                UIMessage.make("summary.page.title"), 
                new SimpleViewParameters(SummaryProducer.VIEW_ID));

        UIInternalLink.make(tofill, "control-evaluations-link",
                UIMessage.make("controlevaluations.page.title"), 
                new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));

        UIInternalLink.make(tofill, "control-templates-link",
                UIMessage.make("controltemplates.page.title"), 
                new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));

        if (!((Boolean)evalSettings.get(EvalSettings.DISABLE_ITEM_BANK))) {
            UIInternalLink.make(tofill, "control-items-link",
                    UIMessage.make("controlitems.page.title"),
                    new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
        }

        UIInternalLink.make(tofill, "control-scales-link",
                UIMessage.make("controlscales.page.title"),
                new SimpleViewParameters(ControlScalesProducer.VIEW_ID));

        UIInternalLink.make(tofill, "control-emailtemplates-link",
                UIMessage.make("controlemailtemplates.page.title"),
                new SimpleViewParameters(ControlEmailTemplatesProducer.VIEW_ID));

        // BREADCRUMBS
        UIInternalLink.make(tofill, "control-email-toplink",
                UIMessage.make("controlemail.page.title"),
                new SimpleViewParameters(AdministrateEmailProducer.VIEW_ID));

        UIInternalLink.make(tofill, "control-reporting-toplink", 
                UIMessage.make("administrate.top.control.reporting"),
                new SimpleViewParameters(AdministrateReportingProducer.VIEW_ID));

        UIInternalLink.make(tofill, "test-evalgroupprovider-toplink",
                UIMessage.make("admintesteg.page.title"),
                new SimpleViewParameters(AdminTestEGProviderProducer.VIEW_ID));           

        // only show Control Importing if enabled
        Boolean enableImporting = (Boolean) evalSettings.get(EvalSettings.ENABLE_IMPORTING);
        if (enableImporting) {
            UIInternalLink.make(tofill, "control-import-toplink",
                    UIMessage.make("administrate.top.import.data"),
                    new SimpleViewParameters(ControlImportProducer.VIEW_ID));
        }

        // only show Control Hierarchy if enabled
        Boolean useHierarchyFeatures = (Boolean) evalSettings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
        if (useHierarchyFeatures) {
            UIInternalLink.make(tofill, "control-hierarchy-toplink", 
                    UIMessage.make("administrate.top.control.hierarchy"),
                    new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID));
        }

        //alert user to current email setting
        Object[] params = new Object[]{"",""};
        String delivery = (String)evalSettings.get(EvalSettings.EMAIL_DELIVERY_OPTION);
        if(EvalConstants.EMAIL_DELIVERY_SEND.equals(delivery)) {
            params[0] = "SEND email";
            params[1] = "PRODUCTION";
        }
        else if (EvalConstants.EMAIL_DELIVERY_LOG.equals(delivery)) {
            params[0] = "LOG email";
            params[1] = "DEVELOPMENT";
        }
        else if(EvalConstants.EMAIL_DELIVERY_NONE.equals(delivery)) {
            params[0] = "NOT send email";
            params[1] = "TEST";
        }
        UIMessage.make(tofill, "email-delivery", "administrate.email.delivery", params);

        //System Settings
        UIForm form = UIForm.make(tofill, "basic-form");		
        UIMessage.make(form, "system-settings-header", "administrate.system.settings.header");
        UIMessage.make(form, "system-settings-instructions","administrate.system.settings.instructions");

        //Instructor Settings
        UIMessage.make(form, "instructor-settings-header", "administrate.instructor.settings.header");
        makeBoolean(form, "instructors-eval-create", EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS); 
        UIMessage.make(form, "instructors-eval-create-note", "administrate.instructors.eval.create.note");

        //Select for whether instructors can view results or not
        String[] administrateConfigurableLabels = 
        {
                "administrate.true.label",
                "administrate.false.label",
                "general.configurable"
        };
        String[] administrateConfigurableValues = 
        {
                EvalToolConstants.ADMIN_BOOLEAN_YES,
                EvalToolConstants.ADMIN_BOOLEAN_NO,
                EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE
        };		

        makeSelect(form, "instructors-view-results", 
                administrateConfigurableValues, 
                administrateConfigurableLabels, 
                EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, true);
        UIMessage.make(form, "instructors-view-results-note", "administrate.instructors.view.results.note");

        makeBoolean(form, "instructors-email-students", EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS); 
        UIMessage.make(form, "instructors-email-students-note", "administrate.instructors.email.students.note");

        /*
         * (non-Javadoc)
         * Select for whether instructors must use evaluationSetupService from above.
         * 
         * Note: The values should be irrespective of i18n as they are stored in database. 
         * Here the 4th values that is "messageLocator.getMessage("general.configurable")"
         * is actually converted to a NULL inside SettingsWBL.java so does not matter to be 
         * language specific here.
         */
        String[] hierarchyOptionValues = 
        {
                EvalConstants.INSTRUCTOR_OPT_IN,
                EvalConstants.INSTRUCTOR_OPT_OUT,
                EvalConstants.INSTRUCTOR_REQUIRED,
                EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE 
        };
        String[] hierarchyOptionLabels = 
        {
                "evalsettings.instructors.label.opt.in",
                "evalsettings.instructors.label.opt.out",
                "evalsettings.instructors.label.required",
                "general.configurable"
        };
        makeSelect(form, "instructors-hierarchy", 
                hierarchyOptionValues, 
                hierarchyOptionLabels, 
                EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE, true);
        UIMessage.make(form, "instructors-hierarchy-note", "administrate.instructors.hierarchy.note");

        //Select for number of questions instructors can add
        makeSelect(form, "instructors-num-questions", 
                EvalToolConstants.PULLDOWN_INTEGER_VALUES, 
                EvalToolConstants.PULLDOWN_INTEGER_VALUES, 
                EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER, false);
        UIMessage.make(form, "instructors-num-questions-note", "administrate.instructors.num.questions.note");

        // Student Settings
        UIMessage.make(form, "student-settings-header", "administrate.student.settings.header");

        //Select for whether students can leave questions unanswered or not
        makeSelect(form, "students-unanswered",	//$NON-NLS-1$ 
                administrateConfigurableValues, 
                administrateConfigurableLabels, 
                EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED, true); 
        UIMessage.make(form, "students-unanswered-note", "administrate.students.unanswered.note");

        //Select for whether student can modify responses upto due date
        makeSelect(form, "students-modify-responses", 
                administrateConfigurableValues, 
                administrateConfigurableLabels, 
                EvalSettings.STUDENT_MODIFY_RESPONSES, true); 
        UIMessage.make(form, "students-modify-responses-note", "administrate.students.modify.responses.note");

        //Select for whether students can view results
        makeSelect(form, "students-view-results",
                administrateConfigurableValues, 
                administrateConfigurableLabels, 
                EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, true);
        UIMessage.make(form, "students-view-results-note","administrate.students.view.results.note");

        // Administrator Settings
        UIMessage.make(form, "administrator-settings-header","administrate.admin.settings.header");		

        //Select for number of questions admins can add
        makeSelect(form, "admin-hierarchy-num-questions",
                EvalToolConstants.PULLDOWN_INTEGER_VALUES,
                EvalToolConstants.PULLDOWN_INTEGER_VALUES,
                EvalSettings.ADMIN_ADD_ITEMS_NUMBER, false); 
        UIMessage.make(form, "admin-hierarchy-num-questions-note", "administrate.admin.hierarchy.num.questions.note");

        makeBoolean(form, "admin-view-instructor-added-results", EvalSettings.ADMIN_VIEW_INSTRUCTOR_ADDED_RESULTS); 
        UIMessage.make(form, "admin-view-instructor-added-results-note", "administrate.admin.view.instructor.added.results.note");		
        makeBoolean(form, "admin-view-below-results", EvalSettings.ADMIN_VIEW_BELOW_RESULTS); 
        UIMessage.make(form, "admin-view-below-results-note","administrate.admin.view.below.results.note");		

        // HIERARCHY settings
        makeBoolean(form, "general-display-hierarchy-options", EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
        UIMessage.make(form, "general-display-hierarchy-options-note", "administrate.general.show.hierarchy.information");

        makeBoolean(form, "hierarchy-display-node-headers", EvalSettings.DISPLAY_HIERARCHY_HEADERS);
        UIMessage.make(form, "hierarchy-display-node-headers-note", "administrate.hierarchy-display-node-headers-note");


        // GENERAL settings

        // Select for number of responses before results could be viewed
        makeSelect(form, "general-responses-before-view",  
                EvalToolConstants.PULLDOWN_INTEGER_VALUES,
                EvalToolConstants.PULLDOWN_INTEGER_VALUES,
                EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS, false);

        makeBoolean(form, "general-na-allowed", EvalSettings.ENABLE_NOT_AVAILABLE);

        // Select for maximum number of questions in a block
        makeSelect(form, "general-max-questions-block",	//$NON-NLS-1$
                EvalToolConstants.PULLDOWN_INTEGER_VALUES,
                EvalToolConstants.PULLDOWN_INTEGER_VALUES,
                EvalSettings.ITEMS_ALLOWED_IN_QUESTION_BLOCK, false);

        // Select for template sharing and visibility settings
        String[] sharingValues = new String[] {
                EvalConstants.SHARING_OWNER,
                EvalConstants.SHARING_PRIVATE,
                EvalConstants.SHARING_PUBLIC
        };
        String[] sharingLabels = new String[] {
                "administrate.sharing.owner",
                "sharing.private",
                "sharing.public"
        };
        makeSelect(form, "general-template-sharing",  
                sharingValues, 
                sharingLabels, 
                EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY, true);

        makeBoolean(form, "general-use-date-time",  EvalSettings.EVAL_USE_DATE_TIME);
        makeBoolean(form, "general-use-stop-date", EvalSettings.EVAL_USE_STOP_DATE); 
        makeBoolean(form, "general-use-view-date", EvalSettings.EVAL_USE_VIEW_DATE); 
        makeBoolean(form, "general-same-view-date",  EvalSettings.EVAL_USE_SAME_VIEW_DATES);

        makeBoolean(form, "general-enable-sites-summary", EvalSettings.ENABLE_SUMMARY_SITES_BOX);
        makeBoolean(form, "general-use-eval-category", EvalSettings.ENABLE_EVAL_CATEGORIES);
        makeBoolean(form, "general-enable-response-removal", EvalSettings.ENABLE_EVAL_RESPONSE_REMOVAL);

        makeBoolean(form, "general-default-question-category",  EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY);

        makeBoolean(form, "general-expert-templates", EvalSettings.USE_EXPERT_TEMPLATES);
        makeBoolean(form, "general-expert-questions", EvalSettings.USE_EXPERT_ITEMS);	

        makeBoolean(form, "general-enable-adhoc-groups", EvalSettings.ENABLE_ADHOC_GROUPS);
        makeBoolean(form, "general-enable-adhoc-users", EvalSettings.ENABLE_ADHOC_USERS); 

        makeBoolean(form, "general-enable-eval-close", EvalSettings.ENABLE_EVAL_EARLY_CLOSE); 
        makeBoolean(form, "general-enable-eval-reopen", EvalSettings.ENABLE_EVAL_REOPEN); 

        makeBoolean(form, "general-enable-item-comments", EvalSettings.ENABLE_ITEM_COMMENTS);

        //    makeBoolean(form, "general-require-comments-block",  EvalSettings.REQUIRE_COMMENTS_BLOCK);

        //Number of days old can an eval be and still be recently closed
        makeSelect(form, "general-eval-closed-still-recent",
                EvalToolConstants.PULLDOWN_INTEGER_VALUES,
                EvalToolConstants.PULLDOWN_INTEGER_VALUES,
                EvalSettings.EVAL_RECENTLY_CLOSED_DAYS, false); 
        UIMessage.make(form, "general-eval-closed-still-recent-note","administrate.general.eval.closed.still.recent.note");

        //Minimum time difference (in hours) between the start date and due date
        makeSelect(form, "general-mim-time-diff-between-dates",
                EvalToolConstants.MINIMUM_TIME_DIFFERENCE,
                EvalToolConstants.MINIMUM_TIME_DIFFERENCE,
                EvalSettings.EVAL_MIN_TIME_DIFF_BETWEEN_START_DUE, false); 
        UIMessage.make(form, "general-mim-time-diff-between-dates-note", "administrate.general.eval.mim.time.diff.between.dates");

        // INSTITUTION SPECIFIC SETTINGS
        makeBoolean(form, "general-item-results-sharing",  EvalSettings.ITEM_USE_RESULTS_SHARING);
        makeBoolean(form, "general-enable-importing",  EvalSettings.ENABLE_IMPORTING);
        makeBoolean(form, "general-disable-item-bank", EvalSettings.DISABLE_ITEM_BANK);
        makeBoolean(form, "general-disable-question-blocks", EvalSettings.DISABLE_QUESTION_BLOCKS);
        makeBoolean(form, "general-enable-ta-category", EvalSettings.ENABLE_ASSISTANT_CATEGORY);
        makeBoolean(form, "general-enable-selections", EvalSettings.ENABLE_INSTRUCTOR_ASSISTANT_SELECTION);
        // Save settings button
        // NB no action now required
        UICommand.make(form, "saveSettings",UIMessage.make("administrate.save.settings.button"), null);	
    }

    /**
     * Construct a UIBoundBoolean administrative setting component
     * 
     * @param parent the containing UIContainer
     * @param ID the component's RSF id
     * @param adminkey the administrative setting constant in org.sakaiproject.evaluation.logic.EvalSettings
     */
    public static UIBoundBoolean makeBoolean(UIContainer parent, String ID, String adminkey) {
        // Must use "composePath" here since admin keys currently contain periods
        UIBoundBoolean bb = UIBoundBoolean.make(parent, ID, adminkey == null ? null : PathUtil.composePath(ADMIN_WBL, adminkey));
        return bb;
    }

    /**
     * Construct a UISelect administrative setting component
     * 
     * Values and labels are contained in org.sakaiproject.evaluation.tool.EvalToolConstants.<br />
     * Administrative setting constant in org.sakaiproject.evaluation.logic.EvalSettings<br />
     * 
     * @param parent the containing UIContainer
     * @param ID the component's RSF id
     * @param values the options from which the the user has to choose
     * @param labels the labels for the options from which the user has to choose
     * @param adminkey the administrative setting constant 
     * @param message use message properties if true, do not use message properties if false
     */
    public static UISelect makeSelect(UIContainer parent, String ID, String[] values, String[] labels, String adminkey, boolean message) {
        UISelect selection = UISelect.make(parent, ID); 
        selection.selection = new UIInput();
        if (adminkey != null) {
            selection.selection.valuebinding = new ELReference(PathUtil.composePath(ADMIN_WBL, adminkey));
        }
        UIBoundList selectvalues = new UIBoundList();
        selectvalues.setValue(values);
        selection.optionlist = selectvalues;
        UIBoundList selectlabels = new UIBoundList();
        selectlabels.setValue(labels);
        selection.optionnames = selectlabels;   

        if (message) {
            selection.setMessageKeys();
        }
        return selection;
    }

    /**
     * Construct a UIInput administrative setting component
     * 
     * Administrative setting constant in org.sakaiproject.evaluation.logic.EvalSettings.<br />
     * 
     * @param parent the containing UIContainer
     * @param ID the component's RSF id
     * @param adminkey the administrative setting constant
     */
    public static UIInput makeInput(UIContainer parent, String ID, String adminkey) {
        UIInput input = UIInput.make(parent, ID, PathUtil.composePath(ADMIN_WBL, adminkey));
        return input;
    }
}
