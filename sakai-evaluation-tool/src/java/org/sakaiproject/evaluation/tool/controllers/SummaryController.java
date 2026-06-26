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
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of SummaryProducer + EvaluateBoxRenderer + BeEvaluatedBoxRenderer.
 * Main landing page of the tool.
 */
@Slf4j
@Controller
@RequestMapping("/summary")
public class SummaryController {

    // DTOs for passing data to the template -----------------------------------------------------------

    @Data
    public static class EvalTakeRow {
        Long evalId;
        String evalGroupId;
        Long responseId;
        String title;
        String statusKey;   // clave i18n: summary.status.pending/inprogress/completed
        String startDate;
        String dueDate;
        boolean linkEnabled;    // false = display as text (completed without modification)
    }

    @Data
    public static class EvalResponseRow {
        Long evalId;
        String evalGroupId;
        String title;
        String startDate;
        String dueDate;
        String responseRate; // "X of Y"
        boolean showResultsLink;
        boolean showRespondentsLink;
    }

    // Servicios -----------------------------------------------------------------------------------

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

    // Handler -------------------------------------------------------------------------------------

    @GetMapping
    public String show(Locale locale, Model model) {

        String userId = commonLogic.getCurrentUserId();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        // --- Permisos generales ---
        boolean isAdmin     = commonLogic.isUserAdmin(userId);
        boolean canCreate   = authoringService.canCreateTemplate(userId);
        boolean canBegin    = evaluationService.canBeginEvaluation(userId);

        // --- Settings ---
        boolean hideQuestionBank = (Boolean) settings.get(EvalSettings.DISABLE_ITEM_BANK);
        boolean showMyToplinks   = (Boolean) settings.get(EvalSettings.ENABLE_MY_TOPLINKS);
        boolean showEvaluateeBox = (Boolean) settings.get(EvalSettings.ENABLE_EVALUATEE_BOX);
        Boolean showAdminBoxSetting = (Boolean) settings.get(EvalSettings.ENABLE_ADMINISTRATING_BOX);
        boolean showAdminBox  = Boolean.TRUE.equals(showAdminBoxSetting);
        boolean showSitesBox  = (Boolean) settings.get(EvalSettings.ENABLE_SUMMARY_SITES_BOX);

        model.addAttribute("isAdmin",         isAdmin);
        model.addAttribute("canCreate",       canCreate);
        model.addAttribute("canBegin",        canBegin);
        model.addAttribute("showMyToplinks",  showMyToplinks);
        model.addAttribute("hideQuestionBank", hideQuestionBank);

        // --- "Evaluations I need to complete" box (EvaluateBox) ---
        List<EvalTakeRow> evalsToTake = buildEvalsToTake(userId, df);
        model.addAttribute("evalsToTake", evalsToTake);

        // --- "Evaluations where I can be evaluated" box (BeEvaluatedBox) ---
        List<EvalResponseRow> evalsInProgress = new ArrayList<>();
        List<EvalResponseRow> evalsClosed     = new ArrayList<>();
        if (showEvaluateeBox) {
            buildBeEvaluatedLists(userId, df, evalsInProgress, evalsClosed);
        }
        model.addAttribute("showEvaluateeBox", showEvaluateeBox);
        model.addAttribute("evalsInProgress",  evalsInProgress);
        model.addAttribute("evalsClosed",      evalsClosed);

        // --- Administration box (AdminBox) - simplified ---
        model.addAttribute("showAdminBox", showAdminBox);

        // --- Cuadro Sites ---
        if (showSitesBox) {
            List<EvalGroup> evaluatedGroups = commonLogic.getEvalGroupsForUser(userId, EvalConstants.PERM_BE_EVALUATED);
            List<EvalGroup> evaluateGroups  = commonLogic.getEvalGroupsForUser(userId, EvalConstants.PERM_TAKE_EVALUATION);
            model.addAttribute("evaluatedGroups", evaluatedGroups);
            model.addAttribute("evaluateGroups",  evaluateGroups);
        }
        model.addAttribute("showSitesBox", showSitesBox);

        return "summary";
    }

    // Private methods ----------------------------------------------------------------------------

    /** Logic extracted from EvaluateBoxRenderer.renderBox() */
    private List<EvalTakeRow> buildEvalsToTake(String userId, DateFormat df) {
        List<EvalTakeRow> rows = new ArrayList<>();
        List<EvalEvaluation> evals = evaluationSetupService.getEvaluationsForUser(userId, true, null, null);
        if (evals.isEmpty()) {
            return rows;
        }

        Long[] evalIds = evals.stream().map(EvalEvaluation::getId).toArray(Long[]::new);
        List<EvalResponse> responses = deliveryService.getEvaluationResponsesForUser(userId, evalIds, null);

        for (EvalEvaluation eval : evals) {
            String state = evaluationService.returnAndFixEvalState(eval, true);
            if (!EvalConstants.EVALUATION_STATE_ACTIVE.equals(state)) {
                continue;
            }
            for (EvalAssignGroup eag : eval.getEvalAssignGroups()) {
                EvalGroup group = commonLogic.makeEvalGroupObject(eag.getEvalGroupId());
                if (EvalConstants.GROUP_TYPE_INVALID.equals(group.type)) {
                    continue;
                }

                EvalResponse response = findResponse(responses, eval.getId(), group.evalGroupId);
                EvalTakeRow row = new EvalTakeRow();
                row.setEvalId(eval.getId());
                row.setEvalGroupId(group.evalGroupId);
                row.setTitle(group.title + " - " + eval.getTitle());
                row.setStartDate(df.format(eval.getStartDate()));
                Date dueDate = eval.getSafeDueDate() != null ? eval.getSafeDueDate() : eval.getDueDate();
                row.setDueDate(dueDate != null ? df.format(dueDate) : "");

                if (response != null && response.getEndTime() != null) {
                    row.setStatusKey("summary.status.completed");
                    row.setLinkEnabled(Boolean.TRUE.equals(eval.getModifyResponsesAllowed()));
                    row.setResponseId(response.getId());
                } else if (response != null) {
                    row.setStatusKey("summary.status.inprogress");
                    row.setLinkEnabled(true);
                    row.setResponseId(response.getId());
                } else {
                    row.setStatusKey("summary.status.pending");
                    row.setLinkEnabled(true);
                }
                rows.add(row);
            }
        }
        return rows;
    }

    /** Logic extracted from BeEvaluatedBoxRenderer.renderBox() */
    private void buildBeEvaluatedLists(String userId, DateFormat df,
            List<EvalResponseRow> evalsInProgress, List<EvalResponseRow> evalsClosed) {

        List<EvalEvaluation> evals = evaluationSetupService.getEvaluationsForEvaluatee(userId, true);
        if (evals == null || evals.isEmpty()) {
            return;
        }
        evals = EvalUtils.sortClosedEvalsToEnd(evals);

        for (EvalEvaluation eval : evals) {
            boolean closed = EvalUtils.checkStateAfter(eval.getState(), EvalConstants.EVALUATION_STATE_CLOSED, true);
            List<EvalGroup> groups = eval.getEvalGroups();
            if (groups == null) groups = new ArrayList<>();

            for (EvalGroup group : groups) {
                EvalResponseRow row = new EvalResponseRow();
                row.setEvalId(eval.getId());
                row.setEvalGroupId(group.evalGroupId);
                row.setTitle(group.title + " - " + eval.getTitle());
                row.setStartDate(df.format(eval.getStartDate()));
                Date dueDate = eval.getSafeDueDate() != null ? eval.getSafeDueDate() : eval.getDueDate();
                row.setDueDate(dueDate != null ? df.format(dueDate) : "");

                int responsesCount  = deliveryService.countResponses(eval.getId(), group.evalGroupId, true);
                int enrollmentsCount = evaluationService.countParticipantsForEval(eval.getId(), new String[]{group.evalGroupId});
                row.setResponseRate(EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount));

                String evalState = commonLogic.calculateViewability(eval.getState());
                row.setShowResultsLink(evalBeanUtils.checkInstructorViewResultsForEval(eval, evalState));

                List<EvalGroup> allowedGroups = commonLogic.getEvalGroupsForUser(userId, EvalConstants.PERM_VIEW_RESPONDERS);
                row.setShowRespondentsLink(allowedGroups.stream().anyMatch(g -> g.evalGroupId.equals(group.evalGroupId)));

                if (closed) {
                    evalsClosed.add(row);
                } else {
                    evalsInProgress.add(row);
                }
            }
        }
    }

    private EvalResponse findResponse(List<EvalResponse> responses, Long evalId, String groupId) {
        for (EvalResponse r : responses) {
            if (groupId.equals(r.getEvalGroupId()) && evalId.equals(r.getEvaluation().getId())) {
                return r;
            }
        }
        return null;
    }
}