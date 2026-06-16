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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.Data;

/**
 * Fourth (last) step of the wizard: assignment confirmation.
 * Spring MVC equivalent of EvaluationAssignConfirmProducer.
 *
 * GET: shows a summary of selected groups and dates.
 * POST: activates the evaluation and saves the assignments.
 */
@Controller
@RequestMapping("/evaluation_assign_confirm")
public class EvaluationAssignConfirmController extends EvalControllerSupport {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationSetupService")
    private EvalEvaluationSetupService evaluationSetupService;

    @Resource(name = "org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic")
    private ExternalHierarchyLogic hierarchyLogic;

    @Data
    public static class GroupConfirmRow {
        String evalGroupId;
        String title;
        int    enrollmentCount;
        String directLink; // may be null if no assignment has been saved yet
    }

    @GetMapping
    public String show(@RequestParam Long evaluationId,
                       @RequestParam(required = false) String[] selectedGroupIDs,
                       @RequestParam(required = false) String[] selectedHierarchyNodeIDs,
                       Locale locale, Model model) {

        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        if (eval == null) {
            throw new IllegalArgumentException("Evaluation not found: " + evaluationId);
        }

        DateFormat df  = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        DateFormat dtf = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
        boolean isPartial = EvalConstants.EVALUATION_STATE_PARTIAL.equals(eval.getState());

        if (selectedGroupIDs == null) selectedGroupIDs = new String[0];
        if (selectedHierarchyNodeIDs == null) selectedHierarchyNodeIDs = new String[0];

        // When accessed from control_evaluations (no groups in URL), load current assignments from DB
        if (selectedGroupIDs.length == 0 && !isPartial) {
            Map<Long, List<EvalAssignGroup>> existing =
                evaluationService.getAssignGroupsForEvals(new Long[]{evaluationId}, true, null);
            List<EvalAssignGroup> assignGroups = existing.get(evaluationId);
            if (assignGroups != null) {
                selectedGroupIDs = assignGroups.stream()
                    .map(EvalAssignGroup::getEvalGroupId)
                    .toArray(String[]::new);
            }
        }

        // Separate regular groups from adhoc groups
        List<GroupConfirmRow> normalRows = new ArrayList<>();
        List<GroupConfirmRow> adhocRows  = new ArrayList<>();

        for (String gid : selectedGroupIDs) {
            EvalGroup group = commonLogic.makeEvalGroupObject(gid);
            GroupConfirmRow row = new GroupConfirmRow();
            row.setEvalGroupId(gid);
            row.setTitle(group.title);
            row.setEnrollmentCount(
                commonLogic.countUserIdsForEvalGroup(gid, EvalConstants.PERM_TAKE_EVALUATION, eval.getSectionAwareness()));

            if (EvalConstants.GROUP_TYPE_ADHOC.equals(group.type)) {
                adhocRows.add(row);
            } else {
                normalRows.add(row);
            }
        }

        // Selected hierarchy nodes
        List<EvalHierarchyNode> selectedNodes = new ArrayList<>();
        for (String nodeId : selectedHierarchyNodeIDs) {
            EvalHierarchyNode node = hierarchyLogic.getNodeById(nodeId);
            if (node != null) selectedNodes.add(node);
        }

        model.addAttribute("eval",                       eval);
        model.addAttribute("evaluationId",               evaluationId);
        model.addAttribute("isPartial",                  isPartial);
        model.addAttribute("normalRows",                 normalRows);
        model.addAttribute("adhocRows",                  adhocRows);
        model.addAttribute("selectedNodes",              selectedNodes);
        model.addAttribute("selectedGroupIDs",           selectedGroupIDs);
        model.addAttribute("selectedHierarchyNodeIDs",   selectedHierarchyNodeIDs);
        model.addAttribute("startDate",                  dtf.format(eval.getStartDate()));
        model.addAttribute("dueDate",                    eval.getDueDate() != null ? dtf.format(eval.getDueDate()) : null);
        model.addAttribute("stopDate",                   eval.getStopDate() != null ? dtf.format(eval.getStopDate()) : null);
        model.addAttribute("viewDate",                   eval.getViewDate() != null ? dtf.format(eval.getViewDate()) : null);
        model.addAttribute("startDateFormatted",         df.format(eval.getStartDate()));
        model.addAttribute("canConfirm", EvalUtils.checkStateBefore(
            EvalUtils.getEvaluationState(eval, false), EvalConstants.EVALUATION_STATE_ACTIVE, true));

        return "evaluation_assign_confirm";
    }

    @PostMapping
    public String confirm(@RequestParam Long evaluationId,
                          @RequestParam(required = false) String[] selectedGroupIDs,
                          @RequestParam(required = false) String[] selectedHierarchyNodeIDs,
                          RedirectAttributes redirectAttrs) {

        if (selectedGroupIDs == null) selectedGroupIDs = new String[0];
        if (selectedHierarchyNodeIDs == null) selectedHierarchyNodeIDs = new String[0];

        String currentUserId = commonLogic.getCurrentUserId();
        final String[] finalGroupIDs = selectedGroupIDs;
        final String[] finalNodeIDs = selectedHierarchyNodeIDs;
        String[] redirectHolder = {null};

        daoInvoker.invokeTransactionalAccess(() -> {
            EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
            if (eval == null) {
                throw new IllegalArgumentException("Evaluation not found: " + evaluationId);
            }

            // Validate that at least 1 group or node is selected (except anonymous evaluations)
            boolean isAnonymous = EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl());
            if (!isAnonymous && finalGroupIDs.length == 0 && finalNodeIDs.length == 0) {
                redirectHolder[0] = "redirect:/evaluation_assign?evaluationId=" + evaluationId;
                return;
            }

            // Validate hierarchy nodes
            if (finalNodeIDs.length > 0) {
                Set<EvalHierarchyNode> nodes = hierarchyLogic.getNodesByIds(finalNodeIDs);
                if (nodes.size() != finalNodeIDs.length) {
                    throw new IllegalArgumentException("Invalid hierarchy node IDs were submitted");
                }
            }

            if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(eval.getState())) {
                // Activate the evaluation (true = transition from PARTIAL to the correct state)
                evaluationSetupService.saveEvaluation(eval, currentUserId, true);

                // Save assignments
                List<org.sakaiproject.evaluation.model.EvalAssignHierarchy> assignedList =
                    evaluationSetupService.setEvalAssignments(evaluationId, finalNodeIDs, finalGroupIDs, false);

                if (assignedList.isEmpty() && !isAnonymous) {
                    // Failsafe: no assignments were created, delete the evaluation
                    evaluationSetupService.deleteEvaluation(evaluationId, currentUserId);
                    throw new IllegalStateException(
                        "An evaluation was created with no group assignments. The evaluation has been deleted.");
                }
            } else {
                // Update assignments for an already-active evaluation
                evaluationSetupService.setEvalAssignments(evaluationId, finalNodeIDs, finalGroupIDs, false);
            }
        });

        if (redirectHolder[0] != null) {
            redirectAttrs.addFlashAttribute("errorMessage", "assigneval.invalid.selection");
            return redirectHolder[0];
        }
        return "redirect:/control_evaluations";
    }
}