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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.entity.EvalCategoryEntityProvider;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of ControlEvaluationsProducer.
 * Shows the user's evaluations grouped by state.
 */
@Slf4j
@Controller
@RequestMapping("/control_evaluations")
public class ControlEvaluationsController {

    // DTO ------------------------------------------------------------------------------------

    /**
     * Modes for the response-rate column.
     * RESPONDERS  → link to the respondents list
     * TEXT        → plain text (no permission or threshold not met)
     */
    public enum RateMode { RESPONDERS, TEXT }

    /**
     * Modes for the results/report column.
     * LINK             → link to view the report
     * DATE             → date only (not yet visible due to date)
     * RESPONSES        → "After N more responses"
     * DATE_RESPONSES   → "Date: if at least N responses" (active eval)
     */
    public enum ReportMode { LINK, DATE, RESPONSES, DATE_RESPONSES }

    @Data
    public static class EvalRow {
        Long    evalId;
        String  title;
        String  ownerName;
        String  startDate;
        String  startDateSort;  // epoch ms for tablesorter
        String  dueDate;
        String  dueDateSort;    // epoch ms for tablesorter
        String  lastModified;       // only for partial evaluations
        String  lastModifiedSort;   // epoch ms for tablesorter
        // Direct URL to the evaluation and category
        String  directUrl;
        String  categoryLabel;
        String  categoryUrl;
        // Assigned groups
        String  groupsLabel;    // group title (1 group) or "N groups" when multiple
        boolean groupsIsLink;   // false = display as text (read-only admin viewing another's eval)
        boolean groupsInvalid;  // true when the single assigned group no longer exists
        // Actions available to the current user
        boolean canEdit;
        boolean canDelete;
        boolean canEarlyClose;
        boolean canReopen;
        boolean canNotify;
        boolean canChown;
        // Response rate
        String   responseRateText;
        RateMode responseRateMode;
        int      responsesNeeded;
        // Report / results
        ReportMode reportMode;
        String     reportText;  // text to display based on reportMode
    }

    // Services ------------------------------------------------------------------------------

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationService")
    private EvalEvaluationService evaluationService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationSetupService")
    private EvalEvaluationSetupService evaluationSetupService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalDeliveryService")
    private EvalDeliveryService deliveryService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalSettings")
    private EvalSettings settings;

    @Resource(name = "org.sakaiproject.evaluation.beans.EvalBeanUtils")
    private EvalBeanUtils evalBeanUtils;

    @Resource(name = "messageSource")
    private MessageSource messageSource;

    // Handlers -------------------------------------------------------------------------------

    @GetMapping
    public String show(@RequestParam(defaultValue = "6") int maxAgeToDisplay,
                       Locale locale, Model model) {

        String currentUserId = commonLogic.getCurrentUserId();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);

        boolean userReadonlyAdmin = commonLogic.isUserReadonlyAdmin(currentUserId);
        boolean isUserAdmin       = commonLogic.isUserAdmin(currentUserId);
        boolean earlyCloseAllowed = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_EARLY_CLOSE);
        boolean reopeningAllowed  = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_REOPEN);
        boolean viewResultsIgnoreDates = (Boolean) settings.get(EvalSettings.VIEW_SURVEY_RESULTS_IGNORE_DATES);
        int responsesRequired = (Integer) settings.get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS);
        boolean checkUnpublished = (Boolean) settings.get(EvalSettings.ENABLE_SITE_GROUP_PUBLISH_CHECK);

        // Fetch and classify evaluations
        List<EvalEvaluation> partialEvals = new ArrayList<>();
        List<EvalEvaluation> inqueueEvals = new ArrayList<>();
        List<EvalEvaluation> activeEvals  = new ArrayList<>();
        List<EvalEvaluation> closedEvals  = new ArrayList<>();

        List<Long> takableEvaluationIds = new ArrayList<>();

        List<EvalEvaluation> evals = evaluationSetupService.getVisibleEvaluationsForUser(
                currentUserId, false, false, true, maxAgeToDisplay);

        for (EvalEvaluation eval : evals) {
            String state = evaluationService.updateEvaluationState(eval.getId());
            if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(state)) {
                partialEvals.add(eval);
            } else if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(state)) {
                inqueueEvals.add(eval);
                takableEvaluationIds.add(eval.getId());
            } else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(state) ||
                       EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(state)) {
                activeEvals.add(eval);
                takableEvaluationIds.add(eval.getId());
            } else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(state) ||
                       EvalConstants.EVALUATION_STATE_VIEWABLE.equals(state)) {
                closedEvals.add(eval);
            }
        }

        // Verificar grupos no publicados (UM specific)
        Map<Long, List<EvalAssignGroup>> takableAssignGroups = new HashMap<>();
        int countUnpublishedGroups = 0;
        if (checkUnpublished && !takableEvaluationIds.isEmpty()) {
            takableAssignGroups = evaluationService.getAssignGroupsForEvals(
                    takableEvaluationIds.toArray(new Long[0]), true, null);
        }

        // Build rows for each section
        List<EvalRow> partialRows  = new ArrayList<>();
        List<EvalRow> inqueueRows  = new ArrayList<>();
        List<EvalRow> activeRows   = new ArrayList<>();
        List<EvalRow> closedRows   = new ArrayList<>();

        for (EvalEvaluation eval : partialEvals) {
            EvalRow row = new EvalRow();
            row.setEvalId(eval.getId());
            row.setTitle(eval.getTitle());
            row.setOwnerName(commonLogic.getEvalUserById(eval.getOwner()).displayName);
            row.setLastModified(eval.getLastModified() != null ? df.format(eval.getLastModified()) : "");
            row.setLastModifiedSort(eval.getLastModified() != null ? String.valueOf(eval.getLastModified().getTime()) : "0");
            row.setCanEdit(true);
            row.setCanDelete(true);
            row.setCanChown(true);
            partialRows.add(row);
        }

        for (EvalEvaluation eval : inqueueEvals) {
            EvalRow row = buildCommonRow(eval, df, locale, currentUserId, userReadonlyAdmin, isUserAdmin,
                    earlyCloseAllowed, reopeningAllowed, viewResultsIgnoreDates, responsesRequired, false);

            // Verificar grupos no publicados
            if (checkUnpublished) {
                List<EvalAssignGroup> assignGroups = takableAssignGroups.get(eval.getId());
                if (assignGroups != null) {
                    int unpublished = 0;
                    for (EvalAssignGroup ag : assignGroups) {
                        if (!commonLogic.isEvalGroupPublished(ag.getEvalGroupId())) {
                            unpublished++;
                        }
                    }
                    if (unpublished > 0) {
                        countUnpublishedGroups++;
                    }
                }
            }
            inqueueRows.add(row);
        }

        for (EvalEvaluation eval : activeEvals) {
            EvalRow row = buildCommonRow(eval, df, locale, currentUserId, userReadonlyAdmin, isUserAdmin,
                    earlyCloseAllowed, reopeningAllowed, viewResultsIgnoreDates, responsesRequired, true);
            activeRows.add(row);
        }

        for (EvalEvaluation eval : closedEvals) {
            EvalRow row = buildCommonRow(eval, df, locale, currentUserId, userReadonlyAdmin, isUserAdmin,
                    earlyCloseAllowed, reopeningAllowed, viewResultsIgnoreDates, responsesRequired, false);
            row.setCanChown(false); // chown not available for closed evals
            closedRows.add(row);
        }

        // Navegar
        boolean canBegin = authoringService.canCreateTemplate(currentUserId);

        model.addAttribute("partialRows",           partialRows);
        model.addAttribute("inqueueRows",           inqueueRows);
        model.addAttribute("activeRows",            activeRows);
        model.addAttribute("closedRows",            closedRows);
        model.addAttribute("countUnpublishedGroups", countUnpublishedGroups);
        model.addAttribute("canBegin",              canBegin);
        model.addAttribute("earlyCloseAllowed",     earlyCloseAllowed);
        model.addAttribute("partialCleanupDays",    EvalConstants.EVALUATION_PARTIAL_CLEANUP_DAYS);
        model.addAttribute("maxAgeToDisplay",       maxAgeToDisplay);
        model.addAttribute("RateMode",              RateMode.class);
        model.addAttribute("ReportMode",            ReportMode.class);

        return "control_evaluations";
    }

    /** Closes an active evaluation early and redirects back. */
    @PostMapping("/close")
    public String closeEval(@RequestParam Long evaluationId,
                            @RequestParam(defaultValue = "6") int maxAgeToDisplay) {
        String currentUserId = commonLogic.getCurrentUserId();
        evaluationSetupService.closeEvaluation(evaluationId, currentUserId);
        return "redirect:/control_evaluations?maxAgeToDisplay=" + maxAgeToDisplay;
    }

    // Private methods -----------------------------------------------------------------------

    private EvalRow buildCommonRow(EvalEvaluation eval, DateFormat df, Locale locale,
            String currentUserId, boolean userReadonlyAdmin, boolean isUserAdmin,
            boolean earlyCloseAllowed, boolean reopeningAllowed,
            boolean viewResultsIgnoreDates, int responsesRequired,
            boolean isActive) {

        EvalRow row = new EvalRow();
        row.setEvalId(eval.getId());
        row.setTitle(eval.getTitle());
        row.setOwnerName(commonLogic.getEvalUserById(eval.getOwner()).displayName);

        row.setStartDate(eval.getStartDate() != null ? df.format(eval.getStartDate()) : "");
        row.setStartDateSort(eval.getStartDate() != null ? String.valueOf(eval.getStartDate().getTime()) : "0");
        Date dueDate = eval.getSafeDueDate() != null ? eval.getSafeDueDate() : eval.getDueDate();
        row.setDueDate(dueDate != null ? df.format(dueDate) : "");
        row.setDueDateSort(dueDate != null ? String.valueOf(dueDate.getTime()) : "0");

        // Direct URLs
        row.setDirectUrl(commonLogic.getEntityURL(eval));
        if (eval.getEvalCategory() != null) {
            row.setCategoryLabel(shortenText(eval.getEvalCategory(), 20));
            row.setCategoryUrl(commonLogic.getEntityURL(EvalCategoryEntityProvider.ENTITY_PREFIX, eval.getEvalCategory()));
        }

        // Groups
        int groupsCount = evaluationService.countEvaluationGroups(eval.getId(), false);
        boolean ownerOrNotReadonly = !userReadonlyAdmin || currentUserId.equals(eval.getOwner());
        row.setGroupsIsLink(ownerOrNotReadonly);
        if (groupsCount == 1) {
            String title = getTitleForFirstEvalGroup(eval.getId());
            if (title == null || title.startsWith("** INVALID:")) {
                row.setGroupsInvalid(true);
            } else {
                row.setGroupsLabel(title);
            }
        } else {
            row.setGroupsLabel(messageSource.getMessage(
                    "controlevaluations.eval.groups.link", new Object[]{groupsCount}, locale));
        }

        // Actions (only if not a foreign read-only admin)
        if (ownerOrNotReadonly) {
            row.setCanEdit(true);
            row.setCanDelete(evaluationService.canRemoveEvaluation(currentUserId, eval.getId())
                    && !eval.getLocked());
            row.setCanNotify(true);
            if (isActive) {
                row.setCanEarlyClose(earlyCloseAllowed);
            } else {
                row.setCanReopen(reopeningAllowed);
            }
            row.setCanChown(!isActive); // chown available for inqueue; not for active evals in the original
        }

        // Response rate (only for active and closed, not inqueue where there is no data yet)
        if (!isActive || true) { // siempre calcular
            int responsesCount  = deliveryService.countResponses(eval.getId(), null, true);
            int enrollmentsCount = evaluationService.countParticipantsForEval(eval.getId(), null);
            int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
            String responseString = EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount);

            row.setResponseRateText(responseString);
            row.setResponsesNeeded(responsesNeeded);

            // Determine the response rate link mode: owner/instructor can always see
            // responders once the minimum response threshold is met
            boolean showRespondersLink = responsesNeeded == 0 && ownerOrNotReadonly;
            row.setResponseRateMode(showRespondersLink ? RateMode.RESPONDERS : RateMode.TEXT);

            // Determine the report mode
            buildReportMode(row, eval, df, currentUserId, isUserAdmin, viewResultsIgnoreDates,
                    responsesRequired, responsesNeeded);
        }

        return row;
    }

    private void buildReportMode(EvalRow row, EvalEvaluation eval, DateFormat df,
            String currentUserId, boolean isUserAdmin,
            boolean viewResultsIgnoreDates, int responsesRequired, int responsesNeeded) {

        String evalState = EvalUtils.getEvaluationState(eval, true);
        boolean evalOpen = EvalUtils.checkStateBefore(evalState, EvalConstants.EVALUATION_STATE_CLOSED, false);

        Date viewDate = eval.getSafeViewDate();
        if (viewDate == null) viewDate = new Date(Long.MAX_VALUE);
        String viewableDate = df.format(viewDate);

        // Determine whether the instructor can view results
        boolean viewResultsEval = viewResultsIgnoreDates;

        // For closed evals, results are visible if state is VIEWABLE or later
        if (!viewResultsEval && !evalOpen) {
            viewResultsEval = EvalUtils.checkStateAfter(evalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true);
        }

        if (!viewResultsEval) {
            // Check if this user (instructor/evaluatee) has special permission
            Boolean instructorAllowedViewResults = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
            boolean instructorViewResults = (instructorAllowedViewResults == null || instructorAllowedViewResults);

            if (instructorViewResults) {
                List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(
                        eval.getId(), currentUserId, null, null, null, null, null);
                boolean isEvaluatee = false;
                for (EvalAssignUser eau : userAssignments) {
                    if (EvalAssignUser.TYPE_EVALUATEE.equals(eau.getType())) {
                        isEvaluatee = true;
                        break;
                    }
                }
                if (isEvaluatee || isUserAdmin) {
                    if ((eval.getInstructorViewResults() &&
                            (eval.getOwner().equals(currentUserId) || isUserAdmin)) ||
                            eval.getInstructorViewAllResults()) {
                        viewResultsEval = true;
                        if (eval.getInstructorsDate() != null) {
                            viewDate = eval.getInstructorsDate();
                            viewableDate = df.format(viewDate);
                        }
                    }
                }
            }
        }

        Date now = new Date();
        if (evalOpen && !viewResultsEval && responsesNeeded > 0) {
            row.setReportMode(ReportMode.DATE_RESPONSES);
            row.setReportText(viewableDate + "|" + responsesRequired);
        } else if (responsesNeeded > 0) {
            row.setReportMode(ReportMode.RESPONSES);
            row.setReportText(String.valueOf(responsesNeeded));
        } else if (now.before(viewDate) || !viewResultsEval) {
            row.setReportMode(ReportMode.DATE);
            row.setReportText(viewableDate);
        } else {
            row.setReportMode(ReportMode.LINK);
            row.setReportText("");
        }
    }

    private String getTitleForFirstEvalGroup(Long evaluationId) {
        Map<Long, List<EvalAssignGroup>> evalAssignGroups =
                evaluationService.getAssignGroupsForEvals(new Long[]{evaluationId}, true, null);
        List<EvalAssignGroup> groups = evalAssignGroups.get(evaluationId);
        if (groups == null || groups.isEmpty()) return "";
        return commonLogic.getDisplayTitle(groups.get(0).getEvalGroupId());
    }

    private String shortenText(String text, int length) {
        if (text != null && text.length() > length) {
            return text.substring(0, length - 3) + "...";
        }
        return text;
    }
}