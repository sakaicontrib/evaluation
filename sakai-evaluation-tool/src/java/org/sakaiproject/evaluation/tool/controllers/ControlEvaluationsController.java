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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.entity.EvaluationEntityProvider;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
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
public class ControlEvaluationsController extends EvalControllerSupport {

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

    private static class ControlDisplaySettings {
        boolean userReadonlyAdmin;
        boolean isUserAdmin;
        boolean earlyCloseAllowed;
        boolean reopeningAllowed;
        boolean viewResultsIgnoreDates;
        int responsesRequired;
        boolean checkUnpublished;
    }

    private static class EvaluationBuckets {
        List<EvalEvaluation> partialEvals = new ArrayList<>();
        List<EvalEvaluation> inqueueEvals = new ArrayList<>();
        List<EvalEvaluation> activeEvals  = new ArrayList<>();
        List<EvalEvaluation> closedEvals  = new ArrayList<>();
        List<Long> takableEvaluationIds = new ArrayList<>();
    }

    private static class EvaluationRows {
        List<EvalRow> partialRows = new ArrayList<>();
        List<EvalRow> inqueueRows = new ArrayList<>();
        List<EvalRow> activeRows  = new ArrayList<>();
        List<EvalRow> closedRows  = new ArrayList<>();
        int countUnpublishedGroups;
    }

    // Services ------------------------------------------------------------------------------


    @Resource(name = "messageSource")
    private MessageSource messageSource;

    // Handlers -------------------------------------------------------------------------------

    @GetMapping
    public String show(@RequestParam(defaultValue = "6") int maxAgeToDisplay,
                       @RequestParam(required = false) String category,
                       Locale locale, Model model,
                       HttpServletRequest request) {

        String contextPath = request.getContextPath();
        String currentUserId = currentUserId();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);

        ControlDisplaySettings displaySettings = loadControlDisplaySettings(currentUserId);
        List<EvalEvaluation> evals = getVisibleEvaluations(currentUserId, maxAgeToDisplay, category);
        EvaluationBuckets buckets = bucketEvaluations(evals);
        Map<Long, List<EvalAssignGroup>> takableAssignGroups = loadTakableAssignGroups(displaySettings, buckets);
        EvaluationRows rows = buildEvaluationRows(buckets, takableAssignGroups, displaySettings,
                df, locale, currentUserId, contextPath);
        boolean canBegin = authoringService.canCreateTemplate(currentUserId);

        addControlEvaluationsModel(model, rows, canBegin, displaySettings.earlyCloseAllowed, maxAgeToDisplay);

        return "control_evaluations";
    }

    /** Closes an active evaluation early and redirects back. */
    @PostMapping("/close")
    public String closeEval(@RequestParam Long evaluationId,
                            @RequestParam(defaultValue = "6") int maxAgeToDisplay) {
        String currentUserId = currentUserId();
        evaluationSetupService.closeEvaluation(evaluationId, currentUserId);
        return "redirect:/control_evaluations?maxAgeToDisplay=" + maxAgeToDisplay;
    }

    // Private methods -----------------------------------------------------------------------

    private ControlDisplaySettings loadControlDisplaySettings(String currentUserId) {
        ControlDisplaySettings displaySettings = new ControlDisplaySettings();
        displaySettings.userReadonlyAdmin = commonLogic.isUserReadonlyAdmin(currentUserId);
        displaySettings.isUserAdmin = commonLogic.isUserAdmin(currentUserId);
        displaySettings.earlyCloseAllowed = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_EARLY_CLOSE);
        displaySettings.reopeningAllowed = (Boolean) settings.get(EvalSettings.ENABLE_EVAL_REOPEN);
        displaySettings.viewResultsIgnoreDates = (Boolean) settings.get(EvalSettings.VIEW_SURVEY_RESULTS_IGNORE_DATES);
        displaySettings.responsesRequired = (Integer) settings.get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS);
        displaySettings.checkUnpublished = (Boolean) settings.get(EvalSettings.ENABLE_SITE_GROUP_PUBLISH_CHECK);
        return displaySettings;
    }

    private List<EvalEvaluation> getVisibleEvaluations(String currentUserId, int maxAgeToDisplay, String category) {
        List<EvalEvaluation> evals = evaluationSetupService.getVisibleEvaluationsForUser(
                currentUserId, false, false, true, maxAgeToDisplay);
        if (category == null || category.isEmpty()) {
            return evals;
        }
        return evals.stream()
                .filter(e -> category.equals(e.getEvalCategory()))
                .collect(java.util.stream.Collectors.toList());
    }

    private EvaluationBuckets bucketEvaluations(List<EvalEvaluation> evals) {
        EvaluationBuckets buckets = new EvaluationBuckets();
        for (EvalEvaluation eval : evals) {
            String state = evaluationService.updateEvaluationState(eval.getId());
            if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(state)) {
                buckets.partialEvals.add(eval);
            } else if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(state)) {
                buckets.inqueueEvals.add(eval);
                buckets.takableEvaluationIds.add(eval.getId());
            } else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(state) ||
                       EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(state)) {
                buckets.activeEvals.add(eval);
                buckets.takableEvaluationIds.add(eval.getId());
            } else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(state) ||
                       EvalConstants.EVALUATION_STATE_VIEWABLE.equals(state)) {
                buckets.closedEvals.add(eval);
            }
        }
        return buckets;
    }

    private Map<Long, List<EvalAssignGroup>> loadTakableAssignGroups(ControlDisplaySettings displaySettings,
            EvaluationBuckets buckets) {
        if (!displaySettings.checkUnpublished || buckets.takableEvaluationIds.isEmpty()) {
            return new HashMap<>();
        }
        return evaluationService.getAssignGroupsForEvals(
                buckets.takableEvaluationIds.toArray(new Long[0]), true, null);
    }

    private EvaluationRows buildEvaluationRows(EvaluationBuckets buckets, Map<Long, List<EvalAssignGroup>> takableAssignGroups,
            ControlDisplaySettings displaySettings, DateFormat df, Locale locale, String currentUserId, String contextPath) {
        EvaluationRows rows = new EvaluationRows();
        for (EvalEvaluation eval : buckets.partialEvals) {
            rows.partialRows.add(buildPartialRow(eval, df));
        }
        for (EvalEvaluation eval : buckets.inqueueEvals) {
            rows.inqueueRows.add(buildCommonRow(eval, df, locale, currentUserId,
                    displaySettings.userReadonlyAdmin, displaySettings.isUserAdmin,
                    displaySettings.earlyCloseAllowed, displaySettings.reopeningAllowed,
                    displaySettings.viewResultsIgnoreDates, displaySettings.responsesRequired, false, contextPath));
            if (hasUnpublishedGroup(eval, takableAssignGroups, displaySettings.checkUnpublished)) {
                rows.countUnpublishedGroups++;
            }
        }
        for (EvalEvaluation eval : buckets.activeEvals) {
            rows.activeRows.add(buildCommonRow(eval, df, locale, currentUserId,
                    displaySettings.userReadonlyAdmin, displaySettings.isUserAdmin,
                    displaySettings.earlyCloseAllowed, displaySettings.reopeningAllowed,
                    displaySettings.viewResultsIgnoreDates, displaySettings.responsesRequired, true, contextPath));
        }
        for (EvalEvaluation eval : buckets.closedEvals) {
            EvalRow row = buildCommonRow(eval, df, locale, currentUserId,
                    displaySettings.userReadonlyAdmin, displaySettings.isUserAdmin,
                    displaySettings.earlyCloseAllowed, displaySettings.reopeningAllowed,
                    displaySettings.viewResultsIgnoreDates, displaySettings.responsesRequired, false, contextPath);
            row.setCanChown(false);
            rows.closedRows.add(row);
        }
        return rows;
    }

    private EvalRow buildPartialRow(EvalEvaluation eval, DateFormat df) {
        EvalRow row = new EvalRow();
        row.setEvalId(eval.getId());
        row.setTitle(eval.getTitle());
        row.setOwnerName(commonLogic.getEvalUserById(eval.getOwner()).displayName);
        row.setLastModified(eval.getLastModified() != null ? df.format(eval.getLastModified()) : "");
        row.setLastModifiedSort(eval.getLastModified() != null ? String.valueOf(eval.getLastModified().getTime()) : "0");
        row.setCanEdit(true);
        row.setCanDelete(true);
        row.setCanChown(true);
        return row;
    }

    private boolean hasUnpublishedGroup(EvalEvaluation eval, Map<Long, List<EvalAssignGroup>> takableAssignGroups,
            boolean checkUnpublished) {
        if (!checkUnpublished) {
            return false;
        }
        List<EvalAssignGroup> assignGroups = takableAssignGroups.get(eval.getId());
        if (assignGroups == null) {
            return false;
        }
        for (EvalAssignGroup ag : assignGroups) {
            if (!commonLogic.isEvalGroupPublished(ag.getEvalGroupId())) {
                return true;
            }
        }
        return false;
    }

    private void addControlEvaluationsModel(Model model, EvaluationRows rows,
            boolean canBegin, boolean earlyCloseAllowed, int maxAgeToDisplay) {
        model.addAttribute("partialRows",            rows.partialRows);
        model.addAttribute("inqueueRows",            rows.inqueueRows);
        model.addAttribute("activeRows",             rows.activeRows);
        model.addAttribute("closedRows",             rows.closedRows);
        model.addAttribute("countUnpublishedGroups", rows.countUnpublishedGroups);
        model.addAttribute("canBegin",               canBegin);
        model.addAttribute("earlyCloseAllowed",      earlyCloseAllowed);
        model.addAttribute("partialCleanupDays",     EvalConstants.EVALUATION_PARTIAL_CLEANUP_DAYS);
        model.addAttribute("maxAgeToDisplay",        maxAgeToDisplay);
        model.addAttribute("RateMode",               RateMode.class);
        model.addAttribute("ReportMode",             ReportMode.class);
    }

    private EvalRow buildCommonRow(EvalEvaluation eval, DateFormat df, Locale locale,
            String currentUserId, boolean userReadonlyAdmin, boolean isUserAdmin,
            boolean earlyCloseAllowed, boolean reopeningAllowed,
            boolean viewResultsIgnoreDates, int responsesRequired,
            boolean isActive, String contextPath) {

        EvalRow row = new EvalRow();
        row.setEvalId(eval.getId());
        row.setTitle(eval.getTitle());
        row.setOwnerName(commonLogic.getEvalUserById(eval.getOwner()).displayName);

        row.setStartDate(eval.getStartDate() != null ? df.format(eval.getStartDate()) : "");
        row.setStartDateSort(eval.getStartDate() != null ? String.valueOf(eval.getStartDate().getTime()) : "0");
        Date dueDate = eval.getSafeDueDate() != null ? eval.getSafeDueDate() : eval.getDueDate();
        row.setDueDate(dueDate != null ? df.format(dueDate) : "");
        row.setDueDateSort(dueDate != null ? String.valueOf(dueDate.getTime()) : "0");

        // Direct URL: opens in a new tab, so suppress back/breadcrumb with external=true.
        // For NONE-auth-control (anonymous) evaluations this needs to be the actual
        // take_eval link, reachable without a Sakai login - which /preview_eval (a normal
        // portal tool URL) cannot do. Use the EntityBroker URL instead: it goes through
        // EvalAnonymousAccessProvider, which redirects straight to take_eval for these.
        if (EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl())) {
            row.setDirectUrl(commonLogic.getEntityURL(EvaluationEntityProvider.ENTITY_PREFIX, eval.getId().toString()));
        } else {
            row.setDirectUrl(contextPath + "/preview_eval?evaluationId=" + eval.getId() + "&external=true");
        }
        if (eval.getEvalCategory() != null) {
            row.setCategoryLabel(shortenText(eval.getEvalCategory(), 20));
            try {
                row.setCategoryUrl(contextPath + "/control_evaluations?category="
                        + URLEncoder.encode(eval.getEvalCategory(), StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                row.setCategoryUrl(contextPath + "/control_evaluations");
            }
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

        // Response rate — calculated for all states (inqueue, active, closed)
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
            if (reportingPermissions.canViewResultsAsInstructorIgnoringDates(eval, currentUserId)) {
                viewResultsEval = true;
                if (eval.getInstructorsDate() != null) {
                    viewDate = eval.getInstructorsDate();
                    viewableDate = df.format(viewDate);
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
