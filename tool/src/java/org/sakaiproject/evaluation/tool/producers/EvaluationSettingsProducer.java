/******************************************************************************
 * EvaluationSettingsProducer.java - created by kahuja@vt.edu on Oct 05, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 * Rui Feng (fengr@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.entity.EvalCategoryEntityProvider;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.utils.RSFUtils;
import org.sakaiproject.evaluation.tool.viewparams.EmailViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.arrayutil.ArrayUtil;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITextDimensionsDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This producer is used to render the evaluation settings page. It is used when
 * user either creates a new evaluation (coming forward from "Start Evaluation"
 * page or coming backward from "Assign Evaluation to courses" page) or from
 * control panel to edit the existing settings.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Rui Feng (fengr@vt.edu)
 */
public class EvaluationSettingsProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

   public static final String VIEW_ID = "evaluation_settings";
   public String getViewID() {
      return VIEW_ID;
   }

   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }

   private EvaluationBean evaluationBean;
   public void setEvaluationBean(EvaluationBean evaluationBean) {
      this.evaluationBean = evaluationBean;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }

   private FormatAwareDateInputEvolver dateevolver;
   public void setDateEvolver(FormatAwareDateInputEvolver dateevolver) {
      this.dateevolver = dateevolver;
   }

   private TextInputEvolver richTextEvolver;
   public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
      this.richTextEvolver = richTextEvolver;
   }

   private Locale locale;
   public void setLocale(Locale locale) {
      this.locale = locale;
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;

      // local variables used in the render logic
      String currentUserId = externalLogic.getCurrentUserId();
      boolean userAdmin = externalLogic.isUserAdmin(currentUserId);
      boolean createTemplate = authoringService.canCreateTemplate(currentUserId);
      boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);

      DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
      DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);

      String evalBeanOTP = "evaluationBean.";
      String evaluationOTP = evalBeanOTP + "eval.";
      /**
       * This is the evaluation we are working with on this page,
       * this should ONLY be read to, do not change any of these fields
       */
      EvalEvaluation evaluation = evaluationBean.eval;
      String currentEvalState = evaluationService.returnAndFixEvalState(evaluation, true);

      /*
       * top links here
       */
      UIInternalLink.make(tofill, "summary-link", 
            UIMessage.make("summary.page.title"), 
            new SimpleViewParameters(SummaryProducer.VIEW_ID));

      if (userAdmin) {
         UIInternalLink.make(tofill, "administrate-link", 
               UIMessage.make("administrate.page.title"),
               new SimpleViewParameters(AdministrateProducer.VIEW_ID));
         UIInternalLink.make(tofill, "control-scales-link",
               UIMessage.make("controlscales.page.title"),
               new SimpleViewParameters(ControlScalesProducer.VIEW_ID));
      }

      if (createTemplate) {
         UIInternalLink.make(tofill, "control-templates-link",
               UIMessage.make("controltemplates.page.title"), 
               new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
         UIInternalLink.make(tofill, "control-items-link",
               UIMessage.make("controlitems.page.title"), 
               new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
      }

      if (beginEvaluation) {
         UIInternalLink.make(tofill, "control-evaluations-link",
               UIMessage.make("controlevaluations.page.title"),
               new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
      } else {
         throw new SecurityException("User attempted to access " + 
               VIEW_ID + " when they are not allowed");
      }


      UIForm form = UIForm.make(tofill, "evalSettingsForm");

      // EVALUATION TITLE/INSTRUCTIONS

      if ( EvalUtils.checkStateBefore(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true) ) {
         UIInput.make(form, "title", evaluationOTP + "title");
      } else {
         UIOutput.make(tofill, "title_disabled", evaluation.getTitle());
      }

      if ( EvalUtils.checkStateBefore(currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, true) ) {
         UIInput instructionsInput = UIInput.make(form, "instructions:", evaluationOTP + "instructions");
         instructionsInput.decorators = new DecoratorList(new UITextDimensionsDecorator(60, 4));
         richTextEvolver.evolveTextInput(instructionsInput);
      } else {
         UIVerbatim.make(tofill, "instructions_disabled", evaluation.getInstructions());
      }

      // only put up the controls/auto binding if this is not already set
      if (evaluation.getTemplate() == null) {
         // Make bottom table containing the list of templates if no template set
         if (evalViewParams.templateId == null) {
            // get the templates usable by this user
            List<EvalTemplate> templateList = 
               authoringService.getTemplatesForUser(currentUserId, null, false);
            if (templateList.size() > 0) {
               UIBranchContainer chooseTemplate = UIBranchContainer.make(form, "chooseTemplate:");

               String[] values = new String[templateList.size()];
               String[] labels = new String[templateList.size()];

               UISelect radios = UISelect.make(chooseTemplate, "templateRadio", 
                     null, null, evalBeanOTP + "templateId", null);
               String selectID = radios.getFullID();
               for (int i = 0; i < templateList.size(); i++) {
                  EvalTemplate template = templateList.get(i);
                  values[i] = template.getId().toString();
                  labels[i] = template.getTitle();
                  UIBranchContainer radiobranch = 
                     UIBranchContainer.make(chooseTemplate, "templateOptions:", i + "");
                  UISelectChoice.make(radiobranch, "radioValue", selectID, i);
                  UISelectLabel.make(radiobranch, "radioLabel", selectID, i);
                  EvalUser owner = externalLogic.getEvalUserById( template.getOwner() );
                  UIOutput.make(radiobranch, "radioOwner", owner.displayName );
                  UIInternalLink.make(radiobranch, "viewPreview_link", 
                        UIMessage.make("starteval.view.preview.link"), 
                        new EvalViewParameters(PreviewEvalProducer.VIEW_ID, null, template.getId()) );
               }
               // need to assign the choices and labels at the end here since we used nulls at the beginning
               radios.optionlist = UIOutputMany.make(values);
               radios.optionnames = UIOutputMany.make(labels);
            } else {
               throw new IllegalStateException("User got to evaluation settings when they have no access to any templates... " +
               "producer suicide was the only way out");
            }
         } else {
            form.parameters.add(new UIELBinding(evalBeanOTP + "templateId", evalViewParams.templateId));
         }
      } else {
         EvalTemplate template = authoringService.getTemplateById(evaluation.getTemplate().getId());
         UIBranchContainer showTemplateBranch = UIBranchContainer.make(tofill, "showTemplate:");
         UIMessage.make(showTemplateBranch, "eval_template_title", "evalsettings.template.title.display",
               new Object[] { template.getTitle() });
         UIInternalLink.make(showTemplateBranch, "eval_template_preview_link", 
               UIMessage.make("evalsettings.template.preview.link"), 
               new EvalViewParameters(PreviewEvalProducer.VIEW_ID, null, template.getId()) );         
      }


      // EVALUATION DATES

      Date today = new Date();
      UIMessage.make(tofill, "current_date", "evalsettings.dates.current", 
            new Object[] { dateFormat.format(today), timeFormat.format(today) });

      // retrieve the global setting for use of date only or date and time picker
      Boolean useDateTime = (Boolean) settings.get(EvalSettings.EVAL_USE_DATE_TIME);

      // Start Date
      UIBranchContainer showStartDate = UIBranchContainer.make(form, "showStartDate:");
      generateDateSelector(showStartDate, "startDate", evalBeanOTP + "startDate", 
            currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, useDateTime);

      // Due Date
      UIBranchContainer showDueDate = UIBranchContainer.make(form, "showDueDate:");
      generateDateSelector(showDueDate, "dueDate", evalBeanOTP + "dueDate", 
            currentEvalState, EvalConstants.EVALUATION_STATE_GRACEPERIOD, useDateTime);

      // Stop Date - Show the "Stop date" text box only if allowed in the System settings
      Boolean useStopDate = (Boolean) settings.get(EvalSettings.EVAL_USE_STOP_DATE);
      if (useStopDate) {
         UIBranchContainer showStopDate = UIBranchContainer.make(form, "showStopDate:");
         generateDateSelector(showStopDate, "stopDate", evalBeanOTP + "stopDate", 
               currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, useDateTime);
      }

      // EVALUATION RESULTS VIEWING/SHARING

      // radio buttons for the results sharing options
      UISelect resultsSharingRadios = UISelect.make(form, "dummyRadioSharing", 
            EvalToolConstants.EVAL_RESULTS_SHARING_VALUES, 
            EvalToolConstants.EVAL_RESULTS_SHARING_LABELS_PROPS,
            evaluationOTP + "resultsSharing", null).setMessageKeys();
      String resultsSharingId = resultsSharingRadios.getFullID();
      for (int i = 0; i < EvalToolConstants.EVAL_RESULTS_SHARING_VALUES.length; ++i) {
         UIBranchContainer radiobranch = UIBranchContainer.make(form, "resultsSharingChoice:", i + "");
         UISelectChoice choice = UISelectChoice.make(radiobranch, "radioValue", resultsSharingId, i);
         UISelectLabel.make(radiobranch, "radioLabel", resultsSharingId, i)
         .decorate( new UILabelTargetDecorator(choice) );
      }


      // show the view date only if allowed by system settings
      if (((Boolean) settings.get(EvalSettings.EVAL_USE_VIEW_DATE)).booleanValue()) {
         UIBranchContainer showViewDate = UIBranchContainer.make(form, "showViewDate:");
         generateDateSelector(showViewDate, "viewDate", evalBeanOTP + "viewDate", 
               currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, useDateTime);
      }


      // all types of users view results on the same date or we can configure the results viewing separately
      boolean sameViewDateForAll = (Boolean) settings.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES);

      // Student view date
      Boolean studentViewResults = (Boolean) settings.get(EvalSettings.STUDENT_VIEW_RESULTS);
      UIBranchContainer showResultsToStudents = UIBranchContainer.make(form, "showResultsToStudents:");
      generateSettingsControlledCheckbox(showResultsToStudents, "studentViewResults", 
            evaluationOTP + "studentViewResults", studentViewResults, form, 
            EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true) );
      generateViewDateControl(showResultsToStudents, "studentsViewDate", 
            evaluationOTP + "studentsDate", studentViewResults, useDateTime, sameViewDateForAll);

      // Instructor view date
      Boolean instructorViewResults = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
      UIBranchContainer showResultsToInst = UIBranchContainer.make(form, "showResultsToInst:");
      generateSettingsControlledCheckbox(showResultsToInst, "instructorViewResults", 
            evaluationOTP + "instructorViewResults", instructorViewResults, form, 
            EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true) );
      generateViewDateControl(showResultsToInst, "instructorsViewDate", 
            evaluationOTP + "instructorsDate", instructorViewResults, useDateTime, sameViewDateForAll);


      // RESPONDENT SETTINGS

      // Student Allowed Leave Unanswered
      Boolean studentUnanswersAllowed = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
      UIBranchContainer showBlankQuestionAllowedToStut = UIBranchContainer.make(form, "showBlankQuestionAllowedToStut:");
      generateSettingsControlledCheckbox(showBlankQuestionAllowedToStut, 
            "blankResponsesAllowed", evaluationOTP + "blankResponsesAllowed", studentUnanswersAllowed, form, 
            EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true) );


      // Student Modify Responses
      Boolean studentModifyReponses = (Boolean) settings.get(EvalSettings.STUDENT_MODIFY_RESPONSES);
      UIBranchContainer showModifyResponsesAllowedToStu = UIBranchContainer.make(form, "showModifyResponsesAllowedToStu:");
      generateSettingsControlledCheckbox(showModifyResponsesAllowedToStu, 
            "modifyResponsesAllowed", evaluationOTP + "modifyResponsesAllowed", studentModifyReponses, form,
            EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true) );



      // ADMIN SETTINGS SECTION

      UISelect authControlSelect = UISelect.make(form, "auth-control-choose", EvalToolConstants.AUTHCONTROL_VALUES, 
            EvalToolConstants.AUTHCONTROL_LABELS, evaluationOTP + "authControl").setMessageKeys();
      if ( EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true) ) {
         RSFUtils.disableComponent(authControlSelect);
      }

      if (userAdmin) {
         // If the person is an admin (any kind), then we need to show these instructor opt in/out settings

         UIMessage.make(form, "instructor-opt-instructions", "evalsettings.admin.settings.instructions");
         UIMessage.make(form, "instructor-opt-header", "evalsettings.instructor.opt.desc");

         // If "EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE" is set as configurable 
         // i.e. NULL in the database then show the instructor opt select box. Else just show the value as label
         String instUseFromAboveValue = (String) settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE);
         if (instUseFromAboveValue == null) {
            UISelect instOpt = UISelect.make(form, "instructorOpt", EvalToolConstants.INSTRUCTOR_OPT_VALUES, 
                  EvalToolConstants.INSTRUCTOR_OPT_LABELS, evaluationOTP + "instructorOpt").setMessageKeys();
            if ( EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_INQUEUE, true) ) {
               RSFUtils.disableComponent(instOpt);
            }
         } else {
            int index = ArrayUtil.indexOf(EvalToolConstants.INSTRUCTOR_OPT_VALUES, instUseFromAboveValue);
            String instUseFromAboveLabel = EvalToolConstants.INSTRUCTOR_OPT_LABELS[index];
            // Displaying the label corresponding to INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE value set as system property
            UIMessage.make(form, "instructorOptLabel", instUseFromAboveLabel);
            // Doing the binding of this INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE value so that it can be saved in the database
            form.parameters.add(new UIELBinding(evaluationOTP + "instructorOpt", instUseFromAboveValue));
         }
      }


      // EVALUATION REMINDERS SECTION

      // email available template link
      UIInternalLink.make(form, "emailAvailable_link", UIMessage.make("evalsettings.available.mail.link"), 
            new EmailViewParameters(PreviewEmailProducer.VIEW_ID, null, EvalConstants.EMAIL_TEMPLATE_AVAILABLE, true) );

      // email reminder control
      UISelect reminderDaysSelect = UISelect.make(form, "reminderDays", EvalToolConstants.REMINDER_EMAIL_DAYS_VALUES, 
            EvalToolConstants.REMINDER_EMAIL_DAYS_LABELS, evaluationOTP + "reminderDays").setMessageKeys();
      if ( EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_GRACEPERIOD, true) ) {
         RSFUtils.disableComponent(reminderDaysSelect);
      }

      // email reminder template link
      UIInternalLink.make(form, "emailReminder_link", UIMessage.make("evalsettings.reminder.mail.link"), 
            new EmailViewParameters(PreviewEmailProducer.VIEW_ID, null, EvalConstants.EMAIL_TEMPLATE_REMINDER, true) );

      // email from address control
      String defaultEmail = (String) settings.get(EvalSettings.FROM_EMAIL_ADDRESS);
      UIMessage.make(form, "eval-from-email-note", "evalsettings.email.sent.from", new String[] {defaultEmail});
      UIInput.make(form, "reminderFromEmail", evaluationOTP + "reminderFromEmail");


      // EVALUATION EXTRAS SECTION
      Boolean categoriesEnabled = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_CATEGORIES);
      if (categoriesEnabled) {
         UIBranchContainer extrasBranch = UIBranchContainer.make(form, "showEvalExtras:");

         // eval category
         if (categoriesEnabled) {
            UIBranchContainer categoryBranch = UIBranchContainer.make(extrasBranch, "showCategory:");
            UIInput.make(categoryBranch, "eval-category", evaluationOTP + "evalCategory");
            if (evaluation.getEvalCategory() != null) {
               UILink.make(categoryBranch, "eval-category-direct-link", UIMessage.make("general.direct.link"), 
                     externalLogic.getEntityURL(EvalCategoryEntityProvider.ENTITY_PREFIX, evaluation.getEvalCategory()) )
                     .decorate( new UITooltipDecorator( UIMessage.make("general.direct.link.title") ) );
            }
         }
      }

      // EVAL SETTINGS SAVING CONTROLS
      // if this evaluation is already saved, show "Save Settings" button else this is the "Continue to Assign to Courses" button
      if (evaluation.getId() == null) {
         UICommand.make(form, "continueAssigning", UIMessage.make("evalsettings.continue.assigning.link"), evalBeanOTP + "continueAssigningAction");

         UIBranchContainer firstTime = UIBranchContainer.make(form, "firstTime:");
         UIMessage.make(firstTime, "cancel-button", "general.cancel.button");

      } else {
         UICommand.make(form, "continueAssigning", UIMessage.make("evalsettings.save.settings.link"), evalBeanOTP + "saveSettingsAction");

         UIBranchContainer subsequentTimes = UIBranchContainer.make(form, "subsequentTimes:");
         UICommand.make(subsequentTimes, "cancel-button", UIMessage.make("general.cancel.button"), evalBeanOTP + "cancelSettingsAction");
      }

      // this fills in the javascript call (areaId, selectId, selectValue, reminderId)
      // NOTE: RSF bug causes us to have to generate the ids manually (http://www.caret.cam.ac.uk/jira/browse/RSF-65)
      UIInitBlock.make(tofill, "initJavascript", "EvalSystem.initEvalSettings", 
            new Object[] {"evaluation_reminder_area", authControlSelect.getFullID() + "-selection", 
            EvalConstants.EVALUATION_AUTHCONTROL_NONE, reminderDaysSelect.getFullID() + "-selection"});

   }


   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
    */
   @SuppressWarnings("unchecked")
   public List reportNavigationCases() {
      List i = new ArrayList();
      // Physical templateId is filled in by global Interceptor
      i.add(new NavigationCase(EvaluationStartProducer.VIEW_ID, new TemplateViewParameters(EvaluationStartProducer.VIEW_ID, null)));
      i.add(new NavigationCase(ControlEvaluationsProducer.VIEW_ID, new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID)));
      i.add(new NavigationCase(EvaluationAssignProducer.VIEW_ID, new SimpleViewParameters(EvaluationAssignProducer.VIEW_ID)));

      return i;
   }


   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
    */
   public ViewParameters getViewParameters() {
      return new EvalViewParameters();
   }



   /**
    * Generate a checkbox which is controlled by a system boolean setting,
    * if setting is configurable (null) then show the checkbox, else disable the checkbox<br/>
    * <b>NOTE:</b> the label for this checkbox must use an rsf:id of (rsfId + "_label")<br/>
    * <b>NOTE:</b> the message key for the label must be ("evalsettings." + rsfId + ".label")
    * 
    * @param parent the parent container
    * @param rsfId the rsf id of the checkbox
    * @param binding the EL binding for this control value
    * @param systemSetting the system setting value which controls this checkbox
    * @param form the form which the control is part of
    * @param disabled if true then disable the control, else it is enabled (for disabling based on state)
    */
   protected void generateSettingsControlledCheckbox(UIContainer parent, String rsfId, 
         String binding, Boolean systemSetting, UIForm form, boolean disabled) {
      if (systemSetting == null) {
         UIBoundBoolean checkbox = UIBoundBoolean.make(parent, rsfId, binding);
         UIMessage.make(parent, rsfId + "_label", "evalsettings." + rsfId + ".label")
                 .decorate( new UILabelTargetDecorator(checkbox) );
         if (disabled) {
            RSFUtils.disableComponent(checkbox); // disable the control
         }
      } else {
         // bind the value explicitly
         form.parameters.add(new UIELBinding(binding, systemSetting));
         // now render the appropriate messages
         if (systemSetting) {
            UIMessage.make(parent, rsfId + "_label", "evalsettings." + rsfId + ".label");
         } else {
            UIMessage.make(parent, rsfId + "_label", "evalsettings." + rsfId + ".disabled");
         }
      }
   }


   /**
    * Generates the date picker control for the standard evaluation dates
    * 
    * @param parent the parent container
    * @param rsfId the rsf id of the checkbox
    * @param binding the EL binding for this control value
    * @param currentEvalState
    * @param worksUntilState
    * @param useDateTime
    */
   private void generateDateSelector(UIBranchContainer parent, String rsfId, String binding, 
         String currentEvalState, String worksUntilState, boolean useDateTime) {
      if ( EvalUtils.checkStateAfter(currentEvalState, worksUntilState, true) ) {
         String suffix = ".date";
         if (useDateTime) {
            suffix = ".time";
         }
         UIOutput.make(parent, rsfId + "_disabled", null, binding)
            .resolver = new ELReference("dateResolver." + suffix);
      } else {
         UIInput datePicker = UIInput.make(parent, rsfId + ":", binding); 
         if (useDateTime) {
            dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);         
         } else {
            dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);        
         }
         dateevolver.evolveDateInput(datePicker);
      }
   }

   /**
    * Reduces code duplication<br/>
    * This will render the view date picker control or a message depending on various system settings<br/>
    * <b>NOTE:</b> the rsfId should not include the ":" even though it must appear in the template with a colon<br/>
    * <b>NOTE:</b> the label for this message must use an rsf:id of (rsfId + "_label")<br/>
    * 
    * @param parent the parent container
    * @param rsfId the rsf id of the checkbox
    * @param binding the EL binding for this control value
    * @param viewResultsSetting
    * @param useDateTime
    * @param sameViewDateForAll
    */
   protected void generateViewDateControl(UIBranchContainer parent, String rsfId, 
         String binding, Boolean viewResultsSetting, Boolean useDateTime, boolean sameViewDateForAll) {
      if (viewResultsSetting == null || viewResultsSetting) {
         // only show something if this on or configurable
         if (sameViewDateForAll) {
            // just show the text to the user since all view dates are the same AND the system setting forces this
            UIMessage.make(parent, rsfId + "_label", "evalsettings.view.results.date.label");
         } else {
            // allow them to choose the date using a date picker
            UIInput dateInput = UIInput.make(parent, rsfId + ":", binding);
            if (useDateTime) {
               dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);         
            } else {
               dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);        
            }
            dateevolver.evolveDateInput(dateInput);
         }         
      }
   }

}
