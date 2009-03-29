/**
 * $Id$
 * $URL$
 * EvaluationSettingsProducer.java - evaluation - Oct 05, 2006 11:32:44 AM - kahuja
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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.entity.EvalCategoryEntityProvider;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
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
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This producer is used to render the evaluation settings page,
 * it is used to control all the settings related to a single evaluation,
 * complete rewrite from the original version
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationSettingsProducer implements ViewComponentProducer, ViewParamsReporter, ActionResultInterceptor {

    public static final String VIEW_ID = "evaluation_settings";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
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
        if (evalViewParams.evaluationId == null) {
            throw new IllegalArgumentException("Cannot access this view unless the evaluationId is set");
        }

        String actionBean = "setupEvalBean.";
        String evaluationOTP = "evaluationBeanLocator." + evalViewParams.evaluationId + ".";
        /**
         * This is the evaluation we are working with on this page,
         * this should ONLY be read from, do not change any of these fields
         */
        EvalEvaluation evaluation = evaluationService.getEvaluationById(evalViewParams.evaluationId);
        String currentEvalState = evaluationService.returnAndFixEvalState(evaluation, true);

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean createTemplate = authoringService.canCreateTemplate(currentUserId);
        boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, locale);

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
            if (!((Boolean) settings.get(EvalSettings.DISABLE_ITEM_BANK))) {
                UIInternalLink.make(tofill, "control-items-link",
                        UIMessage.make("controlitems.page.title"), 
                        new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
            }
        }

        if (beginEvaluation) {
            UIInternalLink.make(tofill, "control-evaluations-link",
                    UIMessage.make("controlevaluations.page.title"),
                    new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
        } else {
            throw new SecurityException("User attempted to access " + 
                    VIEW_ID + " when they are not allowed");
        }

        UIInternalLink.make(tofill, "eval-settings-link",
                UIMessage.make("evalsettings.page.title"),
                new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evalViewParams.evaluationId) );
        if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(evaluation.getState())) {
            // creating a new eval
            UIMessage.make(tofill, "eval-start-text", "starteval.page.title");
        }


        UIForm form = UIForm.make(tofill, "evalSettingsForm");

        // REOPENING eval (SPECIAL CASE)
        Date reOpenDueDate = null;
        Date reOpenStopDate = null;
        boolean reOpening = false;
        if (evalViewParams.reOpening) {
            Boolean enableReOpen = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_REOPEN);
            if (enableReOpen) {
                // check if already active, do nothing if not closed
                if (EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, false)) {
                    // force the due and stop dates to something reasonable
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime( new Date() );
                    calendar.add(Calendar.DATE, 1);
                    reOpenDueDate = calendar.getTime();

                    Boolean useStopDate = (Boolean) settings.get(EvalSettings.EVAL_USE_STOP_DATE);
                    if (useStopDate) {
                        // assign stop date to equal due date for now
                        reOpenStopDate = calendar.getTime();
                    }

                    // finally force the state to appear active
                    currentEvalState = EvalConstants.EVALUATION_STATE_ACTIVE;
                    form.parameters.add( new UIELBinding(actionBean + "reOpening", true) ); // so we know we are reopening
                    reOpening = true;
                }
            }
        }


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
            // render controls for selecting a template since there is none yet
            // Make bottom table containing the list of templates if no template set
            if (evalViewParams.templateId == null) {
                // get the templates usable by this user
                List<EvalTemplate> templateList = 
                    authoringService.getTemplatesForUser(currentUserId, null, false);
                if (templateList.size() > 0) {
                    UIBranchContainer chooseTemplate = UIBranchContainer.make(form, "chooseTemplate:");

                    String[] values = new String[templateList.size()];
                    String[] labels = new String[templateList.size()];

                    // mostly copied from the previous view
                    UISelect radios = UISelect.make(chooseTemplate, "templateRadio", 
                            null, null, actionBean + "templateId", null);
                    String selectID = radios.getFullID();
                    for (int i = 0; i < templateList.size(); i++) {
                        EvalTemplate template = templateList.get(i);
                        values[i] = template.getId().toString();
                        labels[i] = template.getTitle();
                        UIBranchContainer radiobranch = 
                            UIBranchContainer.make(chooseTemplate, "templateOptions:", i + "");
                        UISelectChoice.make(radiobranch, "radioValue", selectID, i);
                        UISelectLabel.make(radiobranch, "radioLabel", selectID, i);
                        EvalUser owner = commonLogic.getEvalUserById( template.getOwner() );
                        UIOutput.make(radiobranch, "radioOwner", owner.displayName );
                        UIInternalLink.make(radiobranch, "viewPreview_link", 
                                UIMessage.make("starteval.view.preview.link"), 
                                new EvalViewParameters(PreviewEvalProducer.VIEW_ID, null, template.getId()) );
                    }
                    // need to assign the choices and labels at the end here since we used nulls at the beginning
                    radios.optionlist = UIOutputMany.make(values);
                    radios.optionnames = UIOutputMany.make(labels);
                } else {
                    throw new IllegalStateException("User got to evaluation settings when they have no access to any templates... " 
                            + "producer suicide was the only way out");
                }
            } else {
                // just bind in the template explicitly to the evaluation
                form.parameters.add( new UIELBinding(evaluationOTP + "template", new ELReference("templateBeanLocator." + evalViewParams.templateId)) );
            }
        } else {
            // render the controls for previewing and editing the existing template
            EvalTemplate template = authoringService.getTemplateById(evaluation.getTemplate().getId());
            UIBranchContainer showTemplateBranch = UIBranchContainer.make(tofill, "showTemplate:");
            UIMessage.make(showTemplateBranch, "eval_template_title", "evalsettings.template.title.display",
                    new Object[] { template.getTitle() });
            UIInternalLink.make(showTemplateBranch, "eval_template_preview_link", 
                    UIMessage.make("evalsettings.template.preview.link"), 
                    new EvalViewParameters(PreviewEvalProducer.VIEW_ID, null, template.getId()) );         
            if ( ! template.getLocked().booleanValue() &&
                    authoringService.canModifyTemplate(currentUserId, template.getId()) ) {
                UIInternalLink.make(showTemplateBranch, "eval_template_modify_link", UIMessage.make("general.command.edit"), 
                        new TemplateViewParameters( ModifyTemplateItemsProducer.VIEW_ID, template.getId() ));
            }
        }

        // EVALUATION INSTRUCTOR/TA SELECTION
        if((Boolean) settings.get(EvalSettings.ENABLE_INSTRUCTOR_ASSISTANT_SELECTION)){
            // radio buttons for the INSTRUCTOR selection options
            UIBranchContainer selectFieldSet = UIBranchContainer.make(form, "selectInstructorTA:");
            String[] selectValues = new String[] {
                    EvalAssignGroup.SELECTION_OPTION_ALL,
                    EvalAssignGroup.SELECTION_OPTION_ONE, 
                    EvalAssignGroup.SELECTION_OPTION_MULTIPLE};
            String savedSettingInstructor = EvalUtils.getSelectionSetting(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR, null, evaluation);
            UISelect selectInstructors = UISelect.make(selectFieldSet, "selectionRadioInstructors", 
                    selectValues, 
                    new String[] {"evalsettings.selection.instructor.all",
                    "evalsettings.selection.instructor.one",
            "evalsettings.selection.instructor.many"},
            actionBean + "selectionInstructors", savedSettingInstructor).setMessageKeys();
            String selectInstructorsId = selectInstructors.getFullID();
            for (int i = 0; i < selectValues.length; ++i) {
                UIBranchContainer radiobranch = UIBranchContainer.make(selectFieldSet, "selectInstructorsChoice:", i + "");
                UISelectChoice choice = UISelectChoice.make(radiobranch, "radioValue", selectInstructorsId, i);
                UISelectLabel.make(radiobranch, "radioLabel", selectInstructorsId, i)
                .decorate( new UILabelTargetDecorator(choice) );
            }
            // radio buttons for the TA selection options
            String savedAssistantInstructor = EvalUtils.getSelectionSetting(EvalAssignGroup.SELECTION_TYPE_ASSISTANT, null, evaluation);
            UISelect selectTAs = UISelect.make(selectFieldSet, "selectionRadioTAs", 
                    selectValues, 
                    new String[] {"evalsettings.selection.ta.all","evalsettings.selection.ta.one","evalsettings.selection.ta.many"},
                    actionBean + "selectionAssistants", savedAssistantInstructor).setMessageKeys();
            String selectTAsId = selectTAs.getFullID();
            for (int i = 0; i < selectValues.length; ++i) {
                UIBranchContainer radiobranch = UIBranchContainer.make(selectFieldSet, "selectTAsChoice:", i + "");
                UISelectChoice choice = UISelectChoice.make(radiobranch, "radioValue", selectTAsId, i);
                UISelectLabel.make(radiobranch, "radioLabel", selectTAsId, i)
                .decorate( new UILabelTargetDecorator(choice) );}
        }

        // EVALUATION DATES

        Date today = new Date();
        UIMessage.make(tofill, "current_date", "evalsettings.dates.current", 
                new Object[] { dateFormat.format(today), timeFormat.format(today) });

        // retrieve the global setting for use of date only or date and time picker
        Boolean useDateTime = (Boolean) settings.get(EvalSettings.EVAL_USE_DATE_TIME);

        // Start Date
        UIBranchContainer showStartDate = UIBranchContainer.make(form, "showStartDate:");
        generateDateSelector(showStartDate, "startDate", evaluationOTP + "startDate", 
                null, currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, useDateTime);

        // Due Date
        UIBranchContainer showDueDate = UIBranchContainer.make(form, "showDueDate:");
        generateDateSelector(showDueDate, "dueDate", evaluationOTP + "dueDate", 
                reOpenDueDate, currentEvalState, EvalConstants.EVALUATION_STATE_GRACEPERIOD, useDateTime);

        // Stop Date - Show the "Stop date" text box only if allowed in the System settings
        Boolean useStopDate = (Boolean) settings.get(EvalSettings.EVAL_USE_STOP_DATE);
        if (useStopDate) {
            UIBranchContainer showStopDate = UIBranchContainer.make(form, "showStopDate:");
            generateDateSelector(showStopDate, "stopDate", evaluationOTP + "stopDate", 
                    reOpenStopDate, currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, useDateTime);
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
            generateDateSelector(showViewDate, "viewDate", evaluationOTP + "viewDate", 
                    null, currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, useDateTime);
        }


        // all types of users view results on the same date or we can configure the results viewing separately
        boolean sameViewDateForAll = (Boolean) settings.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES);

        // Student view date
        Boolean studentViewResults = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS);
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
            if (instUseFromAboveValue == null || 
                    EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE.equals(instUseFromAboveValue)) {
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
                new EmailViewParameters(PreviewEmailProducer.VIEW_ID, null, EvalConstants.EMAIL_TEMPLATE_AVAILABLE, evaluation.getId()) );

        // email reminder control
        UISelect reminderDaysSelect = UISelect.make(form, "reminderDays", EvalToolConstants.REMINDER_EMAIL_DAYS_VALUES, 
                EvalToolConstants.REMINDER_EMAIL_DAYS_LABELS, evaluationOTP + "reminderDays").setMessageKeys();
        if ( EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_GRACEPERIOD, true) ) {
            RSFUtils.disableComponent(reminderDaysSelect);
        }

        // email reminder template link
        UIInternalLink.make(form, "emailReminder_link", UIMessage.make("evalsettings.reminder.mail.link"), 
                new EmailViewParameters(PreviewEmailProducer.VIEW_ID, null, EvalConstants.EMAIL_TEMPLATE_REMINDER, evaluation.getId()) );

        // email from address control
        //      String defaultEmail = (String) settings.get(EvalSettings.FROM_EMAIL_ADDRESS);
        //      // https://bugs.caret.cam.ac.uk/browse/CTL-1525 - default to admin address if option set
        //      Boolean useAdminEmail = (Boolean) settings.get(EvalSettings.USE_ADMIN_AS_FROM_EMAIL);
        //      if (useAdminEmail) {
        //          // try to get the email address for the owner (eval admin)
        //          EvalUser owner = commonLogic.getEvalUserById(currentUserId);
        //          if (owner != null) {
        //              defaultEmail = owner.email;
        //          }
        //      }
        UIMessage.make(form, "eval-from-email-note", "evalsettings.email.sent.from", 
                new Object[] {new ELReference(evaluationOTP + "reminderFromEmail")});
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
                            commonLogic.getEntityURL(EvalCategoryEntityProvider.ENTITY_PREFIX, evaluation.getEvalCategory()) )
                            .decorate( new UITooltipDecorator( UIMessage.make("general.direct.link.title") ) );
                }
            }
        }

        // EVAL SETTINGS SAVING CONTROLS
        // if this evaluation is already saved, show "Save Settings" button else this is the "Continue to Assign to Courses" button
        String messageKey = "evalsettings.save.settings.link";
        if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(evaluation.getState())) {
            messageKey = "evalsettings.continue.assigning.link";
        }
        if (reOpening) {
            messageKey = "evalsettings.reopening.eval.link";
        }
        UICommand.make(form, "continueAssigning", UIMessage.make(messageKey), actionBean + "completeSettingsAction");
        UIMessage.make(tofill, "cancel-button", "general.cancel.button");

        // this fills in the javascript call (areaId, selectId, selectValue, reminderId)
        // NOTE: RSF bug causes us to have to generate the ids manually (http://www.caret.cam.ac.uk/jira/browse/RSF-65)
        UIInitBlock.make(tofill, "initJavascript", "EvalSystem.initEvalSettings", 
                new Object[] {"evaluation_reminder_area", authControlSelect.getFullID() + "-selection", 
                EvalConstants.EVALUATION_AUTHCONTROL_NONE, reminderDaysSelect.getFullID() + "-selection"});

    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.ActionResultInterceptor#interceptActionResult(uk.org.ponder.rsf.flow.ARIResult, uk.org.ponder.rsf.viewstate.ViewParameters, java.lang.Object)
     */
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        // handles the navigation cases and passing along data from view to view
        EvalViewParameters evp = (EvalViewParameters) incoming;
        Long evalId = evp.evaluationId;
        if ("evalSettings".equals(actionReturn)) {
            result.resultingView = new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evalId);
        } else if ("evalAssign".equals(actionReturn)) {
            result.resultingView = new EvalViewParameters(EvaluationAssignProducer.VIEW_ID, evalId);
        } else if ("evalConfirm".equals(actionReturn)) {
            result.resultingView = new EvalViewParameters(EvaluationAssignConfirmProducer.VIEW_ID, evalId);
        } else if ("controlEvals".equals(actionReturn)) {
            result.resultingView = new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID);
        }
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
     * @param initValue null or an initial date value
     * @param currentEvalState
     * @param worksUntilState
     * @param useDateTime
     */
    private void generateDateSelector(UIBranchContainer parent, String rsfId, String binding, 
            Date initValue, String currentEvalState, String worksUntilState, boolean useDateTime) {
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
            dateevolver.evolveDateInput(datePicker, initValue);
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
