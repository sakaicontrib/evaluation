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
import java.util.Date;
import java.util.Locale;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
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
public class EvaluationSettingsController {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationService")
    private EvalEvaluationService evaluationService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationSetupService")
    private EvalEvaluationSetupService evaluationSetupService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalSettings")
    private EvalSettings settings;

    @Resource(name = "org.sakaiproject.evaluation.beans.EvalBeanUtils")
    private EvalBeanUtils evalBeanUtils;

    @GetMapping
    public String show(@RequestParam Long evaluationId, Locale locale, Model model) {
        String currentUserId = commonLogic.getCurrentUserId();

        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        if (eval == null) {
            throw new IllegalArgumentException("Evaluation not found: " + evaluationId);
        }
        if (!evaluationService.canControlEvaluation(currentUserId, evaluationId)) {
            throw new SecurityException("User does not have permission to modify evaluation: " + evaluationId);
        }

        String currentEvalState = evaluationService.returnAndFixEvalState(eval, true);
        boolean isPartial = EvalConstants.EVALUATION_STATE_PARTIAL.equals(eval.getState());
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        // Editability flags per state
        boolean titleEditable        = EvalUtils.checkStateBefore(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true);
        boolean instructionsEditable = EvalUtils.checkStateBefore(currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, true);
        boolean startDateEditable    = !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true);
        boolean dueDateEditable      = !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_GRACEPERIOD, true);
        boolean stopDateEditable     = !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, true);
        boolean viewDateEditable     = !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true);
        boolean authControlDisabled  = EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true);
        boolean instrOptDisabled     = EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_INQUEUE, true);
        boolean reminderDisabled     = EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_GRACEPERIOD, true);
        boolean respondentSettingsDisabled = EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true);
        boolean viewSettingsDisabled = EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true);

        // System settings
        Boolean useStopDate        = (Boolean) settings.get(EvalSettings.EVAL_USE_STOP_DATE);
        Boolean useViewDate        = (Boolean) settings.get(EvalSettings.EVAL_USE_VIEW_DATE);
        Boolean sameViewDates      = (Boolean) settings.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES);
        Boolean categoriesEnabled  = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_CATEGORIES);
        Boolean termIdsEnabled     = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_TERM_IDS);
        Boolean showHierarchy      = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
        Boolean consolidatedEmail  = (Boolean) settings.get(EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT);
        Boolean allowToggleMail    = (Boolean) settings.get(EvalSettings.ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION);
        Boolean studentUnanswered  = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
        Boolean studentModify      = (Boolean) settings.get(EvalSettings.STUDENT_MODIFY_RESPONSES);
        Boolean allRolesSetting    = (Boolean) settings.get(EvalSettings.ALLOW_ALL_SITE_ROLES_TO_RESPOND);
        Boolean studentViewSetting = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS);
        Boolean instrViewSetting   = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
        Boolean instrViewAllSetting= (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_ALL_RESULTS);
        Boolean compulsoryTextSetting = (Boolean) settings.get(EvalSettings.ENABLE_TEXT_ITEM_REQUIRED);
        String  instUseFromAbove   = (String) settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE);

        if (consolidatedEmail == null) consolidatedEmail = false;

        // instructorOpt: if fixed in settings, show text only
        boolean instrOptIsSelect = userAdmin && (instUseFromAbove == null ||
                EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE.equals(instUseFromAbove));
        String  instrOptFixedLabel = null;
        if (userAdmin && !instrOptIsSelect && instUseFromAbove != null) {
            for (int i = 0; i < EvalToolConstants.INSTRUCTOR_OPT_VALUES.length; i++) {
                if (EvalToolConstants.INSTRUCTOR_OPT_VALUES[i].equals(instUseFromAbove)) {
                    instrOptFixedLabel = EvalToolConstants.INSTRUCTOR_OPT_LABELS[i];
                    break;
                }
            }
        }

        // Format dates as strings for the datetime-local inputs
        DateFormat df   = DateFormat.getDateInstance(DateFormat.LONG, locale);
        DateFormat dtf  = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale);

        // Evaluation template
        EvalTemplate evalTemplate = eval.getTemplate();

        // Button text depending on the current state
        String saveButtonKey;
        if (isPartial) {
            saveButtonKey = "evalsettings.continue.assigning.link";
        } else {
            saveButtonKey = "evalsettings.save.settings.link";
        }

        model.addAttribute("eval",              eval);
        model.addAttribute("evaluationId",      evaluationId);
        model.addAttribute("isPartial",         isPartial);
        model.addAttribute("evalTemplate",      evalTemplate);
        model.addAttribute("userAdmin",         userAdmin);
        model.addAttribute("currentDate",       dtf.format(new Date()));

        // Editabilidad
        model.addAttribute("titleEditable",             titleEditable);
        model.addAttribute("instructionsEditable",      instructionsEditable);
        model.addAttribute("startDateEditable",         startDateEditable);
        model.addAttribute("dueDateEditable",           dueDateEditable);
        model.addAttribute("stopDateEditable",          stopDateEditable && useStopDate);
        model.addAttribute("viewDateEditable",          viewDateEditable && useViewDate);
        model.addAttribute("authControlDisabled",       authControlDisabled);
        model.addAttribute("instrOptDisabled",          instrOptDisabled);
        model.addAttribute("reminderDisabled",          reminderDisabled);
        model.addAttribute("respondentSettingsDisabled",respondentSettingsDisabled);
        model.addAttribute("viewSettingsDisabled",      viewSettingsDisabled);

        // System options
        model.addAttribute("useStopDate",       useStopDate);
        model.addAttribute("useViewDate",       useViewDate);
        model.addAttribute("sameViewDates",     sameViewDates);
        model.addAttribute("categoriesEnabled", categoriesEnabled);
        model.addAttribute("termIdsEnabled",    termIdsEnabled);
        model.addAttribute("showHierarchy",     showHierarchy);
        model.addAttribute("consolidatedEmail", consolidatedEmail);
        model.addAttribute("allowToggleMail",   allowToggleMail);
        model.addAttribute("instrOptIsSelect",  instrOptIsSelect);
        model.addAttribute("instrOptFixedLabel",instrOptFixedLabel);

        // Settings controlling checkbox visibility (null = configurable)
        model.addAttribute("studentUnansweredSetting", studentUnanswered);
        model.addAttribute("studentModifySetting",     studentModify);
        model.addAttribute("allRolesSetting",          allRolesSetting);
        model.addAttribute("studentViewSetting",       studentViewSetting);
        model.addAttribute("instrViewSetting",         instrViewSetting);
        model.addAttribute("instrViewAllSetting",      instrViewAllSetting);
        model.addAttribute("compulsoryTextSetting",    compulsoryTextSetting);
        model.addAttribute("sameViewDates",            sameViewDates);

        // Constants for selects
        model.addAttribute("resultsSharingValues",  EvalToolConstants.EVAL_RESULTS_SHARING_VALUES);
        model.addAttribute("resultsSharingLabels",  EvalToolConstants.EVAL_RESULTS_SHARING_LABELS_PROPS);
        model.addAttribute("authControlValues",     EvalToolConstants.AUTHCONTROL_VALUES);
        model.addAttribute("authControlLabels",     EvalToolConstants.AUTHCONTROL_LABELS);
        model.addAttribute("instrOptValues",        EvalToolConstants.INSTRUCTOR_OPT_VALUES);
        model.addAttribute("instrOptLabels",        EvalToolConstants.INSTRUCTOR_OPT_LABELS);
        model.addAttribute("reminderDaysValues",    EvalToolConstants.REMINDER_EMAIL_DAYS_VALUES);
        model.addAttribute("reminderDaysLabels",    EvalToolConstants.REMINDER_EMAIL_DAYS_LABELS);

        // Formatted dates for the datetime-local inputs
        model.addAttribute("startDateStr",      formatDate(eval.getStartDate()));
        model.addAttribute("dueDateStr",        formatDate(eval.getDueDate()));
        model.addAttribute("stopDateStr",       formatDate(eval.getStopDate()));
        model.addAttribute("viewDateStr",       formatDate(eval.getViewDate()));
        model.addAttribute("studentsDateStr",   formatDate(eval.getStudentsDate()));
        model.addAttribute("instructorsDateStr",formatDate(eval.getInstructorsDate()));

        model.addAttribute("saveButtonKey", saveButtonKey);

        return "evaluation_settings";
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
            RedirectAttributes redirectAttrs) {

        String currentUserId = commonLogic.getCurrentUserId();

        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        if (eval == null) {
            throw new IllegalArgumentException("Evaluation not found: " + evaluationId);
        }
        if (!evaluationService.canControlEvaluation(currentUserId, evaluationId)) {
            throw new SecurityException("Usuario sin permisos: " + evaluationId);
        }

        String currentEvalState = evaluationService.returnAndFixEvalState(eval, true);

        // Apply editable fields based on the current state
        if (EvalUtils.checkStateBefore(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            if (title != null) eval.setTitle(title);
        }
        if (EvalUtils.checkStateBefore(currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, true)) {
            eval.setInstructions(instructions);
        }
        if (!EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setStartDate(startDate);
        }
        if (!EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_GRACEPERIOD, true)) {
            eval.setDueDate(dueDate);
        }

        Boolean useStopDate = (Boolean) settings.get(EvalSettings.EVAL_USE_STOP_DATE);
        if (Boolean.TRUE.equals(useStopDate) &&
                !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_CLOSED, true)) {
            eval.setStopDate(stopDate);
        }

        Boolean useViewDate = (Boolean) settings.get(EvalSettings.EVAL_USE_VIEW_DATE);
        if (Boolean.TRUE.equals(useViewDate) &&
                !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true)) {
            eval.setViewDate(viewDate);
        }

        // Resultados
        if (resultsSharing != null) eval.setResultsSharing(resultsSharing);

        // Results checkboxes (only visible/editable when the setting is null = configurable)
        Boolean studentViewSetting = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS);
        if (studentViewSetting == null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true)) {
            eval.setStudentViewResults(Boolean.TRUE.equals(studentViewResults));
        } else if (studentViewSetting != null) {
            eval.setStudentViewResults(studentViewSetting);
        }

        Boolean instrViewSetting = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
        if (instrViewSetting == null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true)) {
            eval.setInstructorViewResults(Boolean.TRUE.equals(instructorViewResults));
        } else if (instrViewSetting != null) {
            eval.setInstructorViewResults(instrViewSetting);
        }

        Boolean instrViewAllSetting = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_ALL_RESULTS);
        if (instrViewAllSetting == null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true)) {
            eval.setInstructorViewAllResults(Boolean.TRUE.equals(instructorViewAllResults));
        } else if (instrViewAllSetting != null) {
            eval.setInstructorViewAllResults(instrViewAllSetting);
        }

        // Per-role view dates (only when sameViewDates is false)
        Boolean sameViewDates = (Boolean) settings.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES);
        if (!Boolean.TRUE.equals(sameViewDates)) {
            if (studentsDate != null)    eval.setStudentsDate(studentsDate);
            if (instructorsDate != null) eval.setInstructorsDate(instructorsDate);
        }

        // Respondents
        Boolean studentUnansweredSetting = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
        if (studentUnansweredSetting == null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setBlankResponsesAllowed(Boolean.TRUE.equals(blankResponsesAllowed));
        } else if (studentUnansweredSetting != null) {
            eval.setBlankResponsesAllowed(studentUnansweredSetting);
        }

        Boolean studentModifySetting = (Boolean) settings.get(EvalSettings.STUDENT_MODIFY_RESPONSES);
        if (studentModifySetting == null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setModifyResponsesAllowed(Boolean.TRUE.equals(modifyResponsesAllowed));
        } else if (studentModifySetting != null) {
            eval.setModifyResponsesAllowed(studentModifySetting);
        }

        Boolean allRolesSetting = (Boolean) settings.get(EvalSettings.ALLOW_ALL_SITE_ROLES_TO_RESPOND);
        if (!Boolean.FALSE.equals(allRolesSetting) && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setAllRolesParticipate(Boolean.TRUE.equals(allRolesParticipate));
        }

        Boolean compulsoryTextSetting = (Boolean) settings.get(EvalSettings.ENABLE_TEXT_ITEM_REQUIRED);
        if (compulsoryTextSetting == null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setCompulsoryTextItemsAllowed(Boolean.TRUE.equals(compulsoryTextItemsAllowed));
        }

        // Auth control
        if (authControl != null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setAuthControl(authControl);
        }

        // Instructor opt (admin only)
        if (commonLogic.isUserAdmin(currentUserId)) {
            String instUseFromAbove = (String) settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE);
            if (instUseFromAbove == null || EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE.equals(instUseFromAbove)) {
                if (instructorOpt != null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_INQUEUE, true)) {
                    eval.setInstructorOpt(instructorOpt);
                }
            } else {
                eval.setInstructorOpt(instUseFromAbove);
            }
        }

        // Notificaciones
        Boolean allowToggleMail = (Boolean) settings.get(EvalSettings.ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION);
        if (Boolean.TRUE.equals(allowToggleMail) && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
            eval.setSendAvailableNotifications(Boolean.TRUE.equals(sendAvailableNotifications));
        }

        Boolean consolidatedEmail = (Boolean) settings.get(EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT);
        if (!Boolean.TRUE.equals(consolidatedEmail)) {
            if (reminderDays != null && !EvalUtils.checkStateAfter(currentEvalState, EvalConstants.EVALUATION_STATE_GRACEPERIOD, true)) {
                eval.setReminderDays(reminderDays);
            }
            if (reminderFromEmail != null) {
                eval.setReminderFromEmail(reminderFromEmail);
            }
        }

        // Extras
        Boolean categoriesEnabled = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_CATEGORIES);
        if (Boolean.TRUE.equals(categoriesEnabled)) {
            eval.setEvalCategory(evalCategory);
        }

        Boolean termIdsEnabled = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_TERM_IDS);
        if (Boolean.TRUE.equals(termIdsEnabled)) {
            eval.setTermId(termId);
        }

        // Hierarchy
        Boolean showHierarchy = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
        if (Boolean.TRUE.equals(showHierarchy)) {
            eval.setSectionAwareness(sectionAwareness);
        }

        // Guardar (sin activar, false)
        try {
            evalBeanUtils.validateEvalCategory(eval.getEvalCategory());
            evaluationSetupService.saveEvaluation(eval, currentUserId, false);
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/evaluation_settings?evaluationId=" + evaluationId;
        }

        // Redirect based on the current state
        if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(eval.getState())) {
            return "redirect:/evaluation_assign?evaluationId=" + evaluationId;
        } else {
            return "redirect:/control_evaluations";
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        synchronized (DATE_FORMAT) {
            return DATE_FORMAT.format(date);
        }
    }
}
