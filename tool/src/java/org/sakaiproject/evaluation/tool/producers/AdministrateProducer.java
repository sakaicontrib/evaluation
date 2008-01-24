/******************************************************************************
 * AdministrateProducer.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 * Antranig Basman (antranig@caret.cam.ac.uk)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;

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
   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }
   
   private EvalSettings evalSettings;
   public void setEvalSettings(EvalSettings evalSettings) {
      this.evalSettings = evalSettings;
   }

   // Used to prepare the path for WritableBeanLocator
   private String ADMIN_WBL = "settingsBean";


   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
      String currentUserId = externalLogic.getCurrentUserId();
      boolean userAdmin = externalLogic.isUserAdmin(currentUserId);

      if (! userAdmin) {
         // Security check and denial
         throw new SecurityException("Non-admin users may not access this page");
      }

      /*
       * top links here
       */
      UIMessage.make(tofill, "administrate-title", "administrate.page.title");
      UIInternalLink.make(tofill, "summary-link", 
            UIMessage.make("summary.page.title"), 
            new SimpleViewParameters(SummaryProducer.VIEW_ID));

      UIInternalLink.make(tofill, "control-templates-link",
            UIMessage.make("controltemplates.page.title"), 
            new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));

      UIInternalLink.make(tofill, "control-evaluations-link",
            UIMessage.make("controlevaluations.page.title"), 
            new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));

      UIInternalLink.make(tofill, "control-scales-toplink",UIMessage.make("administrate.top.control.scales"),
            new SimpleViewParameters(ControlScalesProducer.VIEW_ID));

      UIInternalLink.make(tofill, "control-import-toplink",UIMessage.make("administrate.top.import.data"),
            new SimpleViewParameters(ControlImportProducer.VIEW_ID));

      UIInternalLink.make(tofill, "control-reporting-toplink", UIMessage.make("administrate.top.control.reporting"),
            new SimpleViewParameters(AdministrateReportingProducer.VIEW_ID));
      
      /*
       * Only Show Control Hierarchy if the Hierarchy Display Options are on.
       */
      Boolean useHierarchyFeatures = (Boolean) evalSettings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
      if (useHierarchyFeatures != null && useHierarchyFeatures == true) {
          UIInternalLink.make(tofill, "control-hierarchy-toplink", UIMessage.make("administrate.top.control.hierarchy"),
                new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID));
      }

      UIInternalLink.make(tofill, "test-evalgroupprovider-toplink",UIMessage.make("admintesteg.page.title"),
            new SimpleViewParameters(AdminTestEGProviderProducer.VIEW_ID));           

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
            "administrate.configurable.label"
      };
      String[] administrateConfigurableValues = 
      {
            EvaluationConstant.ADMIN_BOOLEAN_YES,
            EvaluationConstant.ADMIN_BOOLEAN_NO,
            EvaluationConstant.ADMIN_BOOLEAN_CONFIGURABLE
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
       * Select for whether instructors must use evaluations from above.
       * 
       * Note: The values should be irrespective of i18n as they are stored in database. 
       * Here the 4th values that is "messageLocator.getMessage("administrate.configurable.label")"
       * is actually converted to a NULL inside SettingsWBL.java so does not matter to be 
       * language specific here.
       */
      String[] hierarchyOptionValues = 
      {
            EvalConstants.INSTRUCTOR_OPT_IN,
            EvalConstants.INSTRUCTOR_OPT_OUT,
            EvalConstants.INSTRUCTOR_REQUIRED,
            EvaluationConstant.ADMIN_BOOLEAN_CONFIGURABLE 
      };
      String[] hierarchyOptionLabels = 
      {
            "evalsettings.instructors.label.opt.in",
            "evalsettings.instructors.label.opt.out",
            "evalsettings.instructors.label.required",
            "administrate.configurable.label"
      };
      makeSelect(form, "instructors-hierarchy", 
            hierarchyOptionValues, 
            hierarchyOptionLabels, 
            EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE, true);
      UIMessage.make(form, "instructors-hierarchy-note", "administrate.instructors.hierarchy.note");

      //Select for number of questions intructors can add
      makeSelect(form, "instructors-num-questions", 
            EvaluationConstant.PULLDOWN_INTEGER_VALUES, 
            EvaluationConstant.PULLDOWN_INTEGER_VALUES, 
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
            EvalSettings.STUDENT_VIEW_RESULTS, true);
      UIMessage.make(form, "students-view-results-note","administrate.students.view.results.note");

      // Administrator Settings
      UIMessage.make(form, "administrator-settings-header","administrate.admin.settings.header");		

      //Select for number of questions admins can add
      makeSelect(form, "admin-hierarchy-num-questions",
            EvaluationConstant.PULLDOWN_INTEGER_VALUES,
            EvaluationConstant.PULLDOWN_INTEGER_VALUES,
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
      UIMessage.make(form, "general-settings-header","administrate.general.settings.header");		
      makeInput(form, "general-helpdesk-email", EvalSettings.FROM_EMAIL_ADDRESS);
      UIMessage.make(form, "general-helpdesk-email-note", "administrate.general.helpdesk.email.note");

      // Select for number of responses before results could be viewed
      makeSelect(form, "general-responses-before-view",  
            EvaluationConstant.PULLDOWN_INTEGER_VALUES,
            EvaluationConstant.PULLDOWN_INTEGER_VALUES,
            EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS, false);
      UIMessage.make(form, "general-responses-before-view-note", "administrate.general.responses.before.view.note");		

      makeBoolean(form, "general-na-allowed", EvalSettings.NOT_AVAILABLE_ALLOWED); 
      UIMessage.make(form, "general-na-allowed-note", "administrate.general.na.allowed.note");	

      // Select for maximum number of questions in a block
      makeSelect(form, "general-max-questions-block",	//$NON-NLS-1$
            EvaluationConstant.PULLDOWN_INTEGER_VALUES,
            EvaluationConstant.PULLDOWN_INTEGER_VALUES,
            EvalSettings.ITEMS_ALLOWED_IN_QUESTION_BLOCK, false);
      UIMessage.make(form, "general-max-questions-block-note", "administrate.general.max.questions.block.note");		

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
      UIMessage.make(form, "general-template-sharing-note","administrate.general.template.sharing.note");		

      //Select for whether question category would be course or instructor
      makeSelect(form, "general-default-question", 
            administrateConfigurableValues, 
            administrateConfigurableLabels, 
            EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY, true);
      UIMessage.make(form, "general-default-question-category", "administrate.general.default.question.category.note");	

      makeBoolean(form, "general-use-stop-date", EvalSettings.EVAL_USE_STOP_DATE); 
      UIMessage.make(form, "general-use-stop-date-note","administrate.general.use.stop.date.note");		
      makeBoolean(form, "general-expert-templates", EvalSettings.USE_EXPERT_TEMPLATES); 
      UIMessage.make(form, "general-expert-templates-note","administrate.general.expert.templates.note");	
      makeBoolean(form, "general-expert-questions", EvalSettings.USE_EXPERT_ITEMS); 
      UIMessage.make(form, "general-expert-questions-note", "administrate.general.expert.questions.note");		
      makeBoolean(form, "general-same-view-date",  EvalSettings.EVAL_USE_SAME_VIEW_DATES); 
      UIMessage.make(form, "general-same-view-date-note", "administrate.general.same.view.date.note");	
      makeBoolean(form, "general-require-comments-block",  EvalSettings.REQUIRE_COMMENTS_BLOCK); 
      UIMessage.make(form, "general-require-comments-block-note", "administrate.general.require.comments.block.note");	

      makeBoolean(form, "general-item-results-sharing-block",  EvalSettings.ITEM_USE_RESULTS_SHARING); 
      UIMessage.make(form, "general-item-results-sharing-block-note", "administrate.general.item.sharing.block.note");	

      //Number of days old can an eval be and still be recently closed
      makeSelect(form, "general-eval-closed-still-recent",
            EvaluationConstant.PULLDOWN_INTEGER_VALUES,
            EvaluationConstant.PULLDOWN_INTEGER_VALUES,
            EvalSettings.EVAL_RECENTLY_CLOSED_DAYS, false); 
      UIMessage.make(form, "general-eval-closed-still-recent-note","administrate.general.eval.closed.still.recent.note");

      //Minimum time difference (in hours) between the start date and due date
      makeSelect(form, "general-mim-time-diff-between-dates",
            EvaluationConstant.MINIMUM_TIME_DIFFERENCE,
            EvaluationConstant.MINIMUM_TIME_DIFFERENCE,
            EvalSettings.EVAL_MIN_TIME_DIFF_BETWEEN_START_DUE, false); 
      UIMessage.make(form, "general-mim-time-diff-between-dates-note", "administrate.general.eval.mim.time.diff.between.dates");

      // Save settings button
      // NB no action now required
      UICommand.make(form, "saveSettings",UIMessage.make("administrate.save.settings.button"), null);	
   }

   /*
    * (non-Javadoc)
    * This method is used to render checkboxes.
    */
   private void makeBoolean(UIContainer parent, String ID, String adminkey) {
      // Must use "composePath" here since admin keys currently contain periods
      UIBoundBoolean.make(parent, ID, adminkey == null? null : PathUtil.composePath(ADMIN_WBL, adminkey)); 
   }

   /*
    * (non-Javadoc)
    * This is a common method used to render dropdowns (select boxes).
    */
   private void makeSelect(UIContainer parent, String ID, String[] values, String[] labels, String adminkey, boolean message) {
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

      if (message)
         selection.setMessageKeys();
   }

   /*
    * (non-Javadoc)
    * This is a common method used to render text boxes.
    */
   private void makeInput(UIContainer parent, String ID, String adminkey) {
      UIInput.make(parent, ID, PathUtil.composePath(ADMIN_WBL, adminkey));
   }
}
