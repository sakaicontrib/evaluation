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
package org.sakaiproject.evaluation.tool.controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Second step of the wizard: evaluation settings.
 * Spring MVC equivalent of EvaluationSettingsProducer.
 */
@Controller
@RequestMapping("/evaluation_settings")
public class EvaluationSettingsController extends EvalControllerSupport {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    private static class EvaluationSettingsDisplayState {
        String currentEvalState;
        boolean reOpening;
        Date reOpenDueDate;
    }

    private static class EvaluationSettingsForm {
        String title;
        String instructions;
        Date startDate;
        Date dueDate;
        Date stopDate;
        Date viewDate;
        String resultsSharing;
        Boolean studentViewResults;
        Date studentsDate;
        Boolean instructorViewResults;
        Boolean instructorViewAllResults;
        Date instructorsDate;
        Boolean blankResponsesAllowed;
        Boolean modifyResponsesAllowed;
        Boolean allRolesParticipate;
        Boolean compulsoryTextItemsAllowed;
        Boolean sectionAwareness;
        String authControl;
        String instructorOpt;
        Boolean sendAvailableNotifications;
        Integer reminderDays;
        String reminderFromEmail;
        String evalCategory;
        String termId;
        boolean reOpening;

        EvaluationSettingsForm(String title, String instructions, Date startDate, Date dueDate, Date stopDate,
                Date viewDate, String resultsSharing, Boolean studentViewResults, Date studentsDate,
                Boolean instructorViewResults, Boolean instructorViewAllResults, Date instructorsDate,
                Boolean blankResponsesAllowed, Boolean modifyResponsesAllowed, Boolean allRolesParticipate,
                Boolean compulsoryTextItemsAllowed, Boolean sectionAwareness, String authControl, String instructorOpt,
                Boolean sendAvailableNotifications, Integer reminderDays, String reminderFromEmail, String evalCategory,
                String termId, boolean reOpening) {
            this.title = title;
            this.instructions = instructions;
            this.startDate = startDate;
            this.dueDate = dueDate;
            this.stopDate = stopDate;
            this.viewDate = viewDate;
            this.resultsSharing = resultsSharing;
            this.studentViewResults = studentViewResults;
            this.studentsDate = studentsDate;
            this.instructorViewResults = instructorViewResults;
            this.instructorViewAllResults = instructorViewAllResults;
            this.instructorsDate = instructorsDate;
            this.blankResponsesAllowed = blankResponsesAllowed;
            this.modifyResponsesAllowed = modifyResponsesAllowed;
            this.allRolesParticipate = allRolesParticipate;
            this.compulsoryTextItemsAllowed = compulsoryTextItemsAllowed;
            this.sectionAwareness = sectionAwareness;
            this.authControl = authControl;
            this.instructorOpt = instructorOpt;
            this.sendAvailableNotifications = sendAvailableNotifications;
            this.reminderDays = reminderDays;
            this.reminderFromEmail = reminderFromEmail;
            this.evalCategory = evalCategory;
            this.termId = termId;
            this.reOpening = reOpening;
        }
    }


    @GetMapping
    public String show(@RequestParam Long evaluationId,
                       @RequestParam(required = false, defaultValue = "false") boolean reopen,
                       Locale locale, Model model) {
        String currentUserId = currentUserId();

        EvalEvaluation eval = getControlledEvaluation(evaluationId, currentUserId, "modify");
        EvaluationSettingsDisplayState displayState = buildDisplayState(eval, reopen);
        boolean isPartial = EvalConstants.EVALUATION_STATE_PARTIAL.equals(eval.getState());
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        DateFormat dtf  = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale);
        EvalTemplate evalTemplate = eval.getTemplate();

        model.addAttribute("eval",              eval);
        model.addAttribute("evaluationId",      evaluationId);
        model.addAttribute("isPartial",         isPartial);
        model.addAttribute("evalTemplate",      evalTemplate);
        model.addAttribute("userAdmin",         userAdmin);
        model.addAttribute("currentDate",       dtf.format(new Date()));

        addEditabilityModel(model, displayState.currentEvalState);
        addSystemSettingsModel(model, userAdmin);
        addSelectConstantsModel(model);
        addDateModel(model, eval, displayState);
        model.addAttribute("reOpening",         displayState.reOpening);
        model.addAttribute("saveButtonKey",      getSaveButtonKey(isPartial, displayState.reOpening));

        return "evaluation_settings";
    }

    private EvalEvaluation getControlledEvaluation(Long evaluationId, String currentUserId, String action) {
        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        if (eval == null) {
            throw new IllegalArgumentException("Evaluation not found: " + evaluationId);
        }
        if (!evaluationService.canControlEvaluation(currentUserId, evaluationId)) {
            throw new SecurityException("User does not have permission to " + action + " evaluation: " + evaluationId);
        }
        return eval;
    }

    private EvaluationSettingsDisplayState buildDisplayState(EvalEvaluation eval, boolean reopen) {
        EvaluationSettingsDisplayState state = new EvaluationSettingsDisplayState();
        state.currentEvalState = evaluationService.returnAndFixEvalState(eval, true);
        if (reopen) {
            Boolean enableReOpen = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_REOPEN);
            if (Boolean.TRUE.equals(enableReOpen) &&
                    EvalUtils.checkStateAfter(state.currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, false)) {
                state.currentEvalState = EvalConstants.EVALUATION_STATE_ACTIVE;
                state.reOpening = true;
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, 2);
                cal.add(Calendar.MINUTE, -1);
                state.reOpenDueDate = cal.getTime();
            }
        }
        return state;
    }

    private void addEditabilityModel(Model model, String currentEvalState) {
        Boolean useStopDate = (Boolean) settings.get(EvalSettings.EVAL_USE_STOP_DATE);
        Boolean useViewDate = (Boolean) settings.get(EvalSettings.EVAL_USE_VIEW_DATE);

        model.addAttribute("titleEditable",
                EvalUtils.checkStateBefore(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true));
        model.addAttribute("instructionsEditable",
                EvalUtils.checkStateBefore(currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, true));
        model.addAttribute("startDateEditable",
                !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true));
        model.addAttribute("dueDateEditable",
                !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_GRACEPERIOD, true));
        model.addAttribute("stopDateEditable",
                !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, true) && Boolean.TRUE.equals(useStopDate));
        model.addAttribute("viewDateEditable",
                !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true) && Boolean.TRUE.equals(useViewDate));
        model.addAttribute("authControlDisabled",
                EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true));
        model.addAttribute("instrOptDisabled",
                EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_INQUEUE, true));
        model.addAttribute("reminderDisabled",
                EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_GRACEPERIOD, true));
        model.addAttribute("respondentSettingsDisabled",
                EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true));
        model.addAttribute("viewSettingsDisabled",
                EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true));
    }

    private void addSystemSettingsModel(Model model, boolean userAdmin) {
        Boolean consolidatedEmail = (Boolean) settings.get(EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT);
        Boolean submissionEmailEnabled = (Boolean) settings.get(EvalSettings.ENABLE_SUBMISSION_CONFIRMATION_EMAIL);
        Boolean sameViewDates = (Boolean) settings.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES);
        String instUseFromAbove = (String) settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE);

        boolean instrOptIsSelect = userAdmin && (instUseFromAbove == null ||
                EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE.equals(instUseFromAbove));
        model.addAttribute("useStopDate",       settings.get(EvalSettings.EVAL_USE_STOP_DATE));
        model.addAttribute("useViewDate",       settings.get(EvalSettings.EVAL_USE_VIEW_DATE));
        model.addAttribute("sameViewDates",     sameViewDates);
        model.addAttribute("categoriesEnabled", settings.get(EvalSettings.ENABLE_EVAL_CATEGORIES));
        model.addAttribute("termIdsEnabled",    settings.get(EvalSettings.ENABLE_EVAL_TERM_IDS));
        model.addAttribute("showHierarchy",     settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS));
        model.addAttribute("consolidatedEmail", Boolean.TRUE.equals(consolidatedEmail));
        model.addAttribute("allowToggleMail",   settings.get(EvalSettings.ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION));
        model.addAttribute("submissionEmailEnabled", Boolean.TRUE.equals(submissionEmailEnabled));
        model.addAttribute("instrOptIsSelect",  instrOptIsSelect);
        model.addAttribute("instrOptFixedLabel", getInstructorOptFixedLabel(userAdmin, instrOptIsSelect, instUseFromAbove));
        model.addAttribute("studentUnansweredSetting", settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED));
        model.addAttribute("studentModifySetting",     settings.get(EvalSettings.STUDENT_MODIFY_RESPONSES));
        model.addAttribute("allRolesSetting",          settings.get(EvalSettings.ALLOW_ALL_SITE_ROLES_TO_RESPOND));
        model.addAttribute("studentViewSetting",       settings.get(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS));
        model.addAttribute("instrViewSetting",         settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS));
        model.addAttribute("instrViewAllSetting",      settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_ALL_RESULTS));
        model.addAttribute("compulsoryTextSetting",    settings.get(EvalSettings.ENABLE_TEXT_ITEM_REQUIRED));
    }

    private String getInstructorOptFixedLabel(boolean userAdmin, boolean instrOptIsSelect, String instUseFromAbove) {
        if (!userAdmin || instrOptIsSelect || instUseFromAbove == null) {
            return null;
        }
        for (int i = 0; i < EvalToolConstants.INSTRUCTOR_OPT_VALUES.length; i++) {
            if (EvalToolConstants.INSTRUCTOR_OPT_VALUES[i].equals(instUseFromAbove)) {
                return EvalToolConstants.INSTRUCTOR_OPT_LABELS[i];
            }
        }
        return null;
    }

    private void addSelectConstantsModel(Model model) {
        model.addAttribute("resultsSharingValues",  EvalToolConstants.EVAL_RESULTS_SHARING_VALUES);
        model.addAttribute("resultsSharingLabels",  EvalToolConstants.EVAL_RESULTS_SHARING_LABELS_PROPS);
        model.addAttribute("authControlValues",     EvalToolConstants.AUTHCONTROL_VALUES);
        model.addAttribute("authControlLabels",     EvalToolConstants.AUTHCONTROL_LABELS);
        model.addAttribute("instrOptValues",        EvalToolConstants.INSTRUCTOR_OPT_VALUES);
        model.addAttribute("instrOptLabels",        EvalToolConstants.INSTRUCTOR_OPT_LABELS);
        model.addAttribute("reminderDaysValues",    EvalToolConstants.REMINDER_EMAIL_DAYS_VALUES);
        model.addAttribute("reminderDaysLabels",    EvalToolConstants.REMINDER_EMAIL_DAYS_LABELS);
    }

    private void addDateModel(Model model, EvalEvaluation eval, EvaluationSettingsDisplayState displayState) {
        model.addAttribute("startDateStr",      formatDate(eval.getStartDate()));
        model.addAttribute("dueDateStr",        displayState.reOpening ? formatDate(displayState.reOpenDueDate) : formatDate(eval.getDueDate()));
        model.addAttribute("stopDateStr",       formatDate(eval.getStopDate()));
        model.addAttribute("viewDateStr",       formatDate(eval.getViewDate()));
        model.addAttribute("studentsDateStr",   formatDate(eval.getStudentsDate()));
        model.addAttribute("instructorsDateStr",formatDate(eval.getInstructorsDate()));
    }

    private String getSaveButtonKey(boolean isPartial, boolean reOpening) {
        if (reOpening) {
            return "evalsettings.reopening.eval.link";
        }
        return isPartial ? "evalsettings.continue.assigning.link" : "evalsettings.save.settings.link";
    }

    @PostMapping
    public String save(
            @RequestParam Long evaluationId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String instructions,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date dueDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date stopDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date viewDate,
            @RequestParam(required = false) String resultsSharing,
            @RequestParam(required = false) Boolean studentViewResults,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date studentsDate,
            @RequestParam(required = false) Boolean instructorViewResults,
            @RequestParam(required = false) Boolean instructorViewAllResults,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date instructorsDate,
            @RequestParam(required = false) Boolean blankResponsesAllowed,
            @RequestParam(required = false) Boolean modifyResponsesAllowed,
            @RequestParam(required = false) Boolean allRolesParticipate,
            @RequestParam(required = false) Boolean compulsoryTextItemsAllowed,
            @RequestParam(required = false) Boolean sectionAwareness,
            @RequestParam(required = false) String authControl,
            @RequestParam(required = false) String instructorOpt,
            @RequestParam(required = false) Boolean sendAvailableNotifications,
            @RequestParam(required = false) Integer reminderDays,
            @RequestParam(required = false) String reminderFromEmail,
            @RequestParam(required = false) String evalCategory,
            @RequestParam(required = false) String termId,
            @RequestParam(required = false, defaultValue = "false") boolean reOpening,
            RedirectAttributes redirectAttrs) {

        String currentUserId = currentUserId();

        if (!evaluationService.canControlEvaluation(currentUserId, evaluationId)) {
            throw new SecurityException("User does not have permission to control evaluation: " + evaluationId);
        }

        EvaluationSettingsForm form = new EvaluationSettingsForm(title, instructions, startDate, dueDate, stopDate,
                viewDate, resultsSharing, studentViewResults, studentsDate, instructorViewResults,
                instructorViewAllResults, instructorsDate, blankResponsesAllowed, modifyResponsesAllowed,
                allRolesParticipate, compulsoryTextItemsAllowed, sectionAwareness, authControl, instructorOpt,
                sendAvailableNotifications, reminderDays, reminderFromEmail, evalCategory, termId, reOpening);

        String evalState;
        try {
            evalState = saveEvaluationSettings(evaluationId, currentUserId, form);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/evaluation_settings?evaluationId=" + evaluationId;
        }

        if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(evalState)) {
            return "redirect:/evaluation_assign?evaluationId=" + evaluationId;
        } else {
            return "redirect:/control_evaluations";
        }
    }

    private String saveEvaluationSettings(Long evaluationId, String currentUserId, EvaluationSettingsForm form) {
        String[] evalStateHolder = {null};
        daoInvoker.invokeTransactionalAccess(() -> {
            EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
            if (eval == null) {
                throw new IllegalArgumentException("Evaluation not found: " + evaluationId);
            }

            String currentEvalState = evaluationService.returnAndFixEvalState(eval, true);
            if (form.reOpening && EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, false)) {
                currentEvalState = EvalConstants.EVALUATION_STATE_ACTIVE;
            }

            applyEditableFields(eval, form, currentEvalState);
            applyResultsSettings(eval, form, currentEvalState);
            applyRespondentSettings(eval, form, currentEvalState);
            applyAdminSettings(eval, form, currentEvalState, currentUserId);
            applyNotificationSettings(eval, form, currentEvalState);
            applyExtraSettings(eval, form);

            evalBeanUtils.validateEvalCategory(eval.getEvalCategory());
            evaluationSetupService.saveEvaluation(eval, currentUserId, false);
            evalStateHolder[0] = eval.getState();
        });
        return evalStateHolder[0];
    }

    private void applyEditableFields(EvalEvaluation eval, EvaluationSettingsForm form, String currentEvalState) {
        if (EvalUtils.checkStateBefore(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true) && form.title != null) {
            eval.setTitle(form.title);
        }
        if (EvalUtils.checkStateBefore(currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, true)) {
            eval.setInstructions(form.instructions);
        }
        if (!EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setStartDate(form.startDate);
        }
        if (!EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_GRACEPERIOD, true)) {
            eval.setDueDate(form.dueDate);
        }

        Boolean useStopDate = (Boolean) settings.get(EvalSettings.EVAL_USE_STOP_DATE);
        if (Boolean.TRUE.equals(useStopDate) &&
                !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, true)) {
            eval.setStopDate(form.stopDate);
        }

        Boolean useViewDate = (Boolean) settings.get(EvalSettings.EVAL_USE_VIEW_DATE);
        if (Boolean.TRUE.equals(useViewDate) &&
                !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true)) {
            eval.setViewDate(form.viewDate);
        }
        if (form.authControl != null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setAuthControl(form.authControl);
        }
    }

    private void applyResultsSettings(EvalEvaluation eval, EvaluationSettingsForm form, String currentEvalState) {
        if (form.resultsSharing != null) {
            eval.setResultsSharing(form.resultsSharing);
        }

        Boolean studentViewSetting = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS);
        if (studentViewSetting == null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true)) {
            eval.setStudentViewResults(Boolean.TRUE.equals(form.studentViewResults));
        } else if (studentViewSetting != null) {
            eval.setStudentViewResults(studentViewSetting);
        }

        Boolean instrViewSetting = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
        if (instrViewSetting == null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true)) {
            eval.setInstructorViewResults(Boolean.TRUE.equals(form.instructorViewResults));
        } else if (instrViewSetting != null) {
            eval.setInstructorViewResults(instrViewSetting);
        }

        Boolean instrViewAllSetting = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_ALL_RESULTS);
        if (instrViewAllSetting == null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true)) {
            eval.setInstructorViewAllResults(Boolean.TRUE.equals(form.instructorViewAllResults));
        } else if (instrViewAllSetting != null) {
            eval.setInstructorViewAllResults(instrViewAllSetting);
        }

        Boolean sameViewDates = (Boolean) settings.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES);
        if (!Boolean.TRUE.equals(sameViewDates)) {
            if (form.studentsDate != null) {
                eval.setStudentsDate(form.studentsDate);
            }
            if (form.instructorsDate != null) {
                eval.setInstructorsDate(form.instructorsDate);
            }
        }
    }

    private void applyRespondentSettings(EvalEvaluation eval, EvaluationSettingsForm form, String currentEvalState) {
        Boolean studentUnansweredSetting = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
        if (studentUnansweredSetting == null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setBlankResponsesAllowed(Boolean.TRUE.equals(form.blankResponsesAllowed));
        } else if (studentUnansweredSetting != null) {
            eval.setBlankResponsesAllowed(studentUnansweredSetting);
        }

        Boolean studentModifySetting = (Boolean) settings.get(EvalSettings.STUDENT_MODIFY_RESPONSES);
        if (studentModifySetting == null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setModifyResponsesAllowed(Boolean.TRUE.equals(form.modifyResponsesAllowed));
        } else if (studentModifySetting != null) {
            eval.setModifyResponsesAllowed(studentModifySetting);
        }

        Boolean allRolesSetting = (Boolean) settings.get(EvalSettings.ALLOW_ALL_SITE_ROLES_TO_RESPOND);
        if (!Boolean.FALSE.equals(allRolesSetting) && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setAllRolesParticipate(Boolean.TRUE.equals(form.allRolesParticipate));
        }

        Boolean compulsoryTextSetting = (Boolean) settings.get(EvalSettings.ENABLE_TEXT_ITEM_REQUIRED);
        if (compulsoryTextSetting == null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setCompulsoryTextItemsAllowed(Boolean.TRUE.equals(form.compulsoryTextItemsAllowed));
        }
    }

    private void applyAdminSettings(EvalEvaluation eval, EvaluationSettingsForm form, String currentEvalState, String currentUserId) {
        if (!commonLogic.isUserAdmin(currentUserId)) {
            return;
        }
        String instUseFromAbove = (String) settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE);
        if (instUseFromAbove == null || EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE.equals(instUseFromAbove)) {
            if (form.instructorOpt != null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_INQUEUE, true)) {
                eval.setInstructorOpt(form.instructorOpt);
            }
        } else {
            eval.setInstructorOpt(instUseFromAbove);
        }
    }

    private void applyNotificationSettings(EvalEvaluation eval, EvaluationSettingsForm form, String currentEvalState) {
        Boolean allowToggleMail = (Boolean) settings.get(EvalSettings.ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION);
        if (Boolean.TRUE.equals(allowToggleMail) && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setSendAvailableNotifications(Boolean.TRUE.equals(form.sendAvailableNotifications));
        }

        Boolean consolidatedEmail = (Boolean) settings.get(EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT);
        if (!Boolean.TRUE.equals(consolidatedEmail)) {
            if (form.reminderDays != null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_GRACEPERIOD, true)) {
                eval.setReminderDays(form.reminderDays);
            }
            if (form.reminderFromEmail != null) {
                eval.setReminderFromEmail(form.reminderFromEmail);
            }
        }
    }

    private void applyExtraSettings(EvalEvaluation eval, EvaluationSettingsForm form) {
        Boolean categoriesEnabled = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_CATEGORIES);
        if (Boolean.TRUE.equals(categoriesEnabled)) {
            eval.setEvalCategory(form.evalCategory);
        }

        Boolean termIdsEnabled = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_TERM_IDS);
        if (Boolean.TRUE.equals(termIdsEnabled)) {
            eval.setTermId(form.termId);
        }

        Boolean showHierarchy = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
        if (Boolean.TRUE.equals(showHierarchy)) {
            eval.setSectionAwareness(form.sectionAwareness);
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        synchronized (DATE_FORMAT) {
            return DATE_FORMAT.format(date);
        }
    }
}
