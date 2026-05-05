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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/evaluation_assign_select")
public class EvaluationAssignSelectController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationService")
    private EvalEvaluationService evaluationService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationSetupService")
    private EvalEvaluationSetupService evaluationSetupService;

    @Data
    public static class UserRow {
        private final String userId;
        private final String username;
        private final String sortName;
        private final boolean selected;
    }

    @GetMapping
    public String show(@RequestParam Long evaluationId,
                       @RequestParam String evalGroupId,
                       @RequestParam String evalCategory,
                       Model model) {

        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        String groupTitle = commonLogic.getDisplayTitle(evalGroupId);

        boolean isInstructor = EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR.equals(evalCategory);
        boolean isAssistant  = EvalAssignGroup.SELECTION_TYPE_ASSISTANT.equals(evalCategory);
        if (!isInstructor && !isAssistant) {
            throw new InvalidParameterException("Unknown evalCategory: " + evalCategory);
        }

        // Users in the group with the required permission
        String perm = isInstructor ? EvalConstants.PERM_BE_EVALUATED : EvalConstants.PERM_ASSISTANT_ROLE;
        Set<String> userIds = commonLogic.getUserIdsForEvalGroup(evalGroupId, perm, eval.getSectionAwareness());
        List<EvalUser> evalUsers = commonLogic.getEvalUsersByIds(new ArrayList<>(userIds));
        Collections.sort(evalUsers, new EvalUser.SortNameComparator());

        // IDs of users already linked (STATUS_LINKED) for this group and type
        String assignType = isInstructor ? EvalAssignUser.TYPE_EVALUATEE : EvalAssignUser.TYPE_ASSISTANT;
        List<EvalAssignUser> assignUsers = evaluationService.getParticipantsForEval(
                evaluationId, null, new String[]{ evalGroupId }, assignType,
                EvalAssignUser.STATUS_LINKED, null, null);
        List<String> linkedIds = new ArrayList<>();
        for (EvalAssignUser au : assignUsers) linkedIds.add(au.getUserId());

        List<UserRow> rows = new ArrayList<>();
        for (EvalUser u : evalUsers) {
            // If no users are linked yet, all are selected by default
            boolean selected = linkedIds.isEmpty() || linkedIds.contains(u.userId);
            rows.add(new UserRow(u.userId, u.username, u.sortName, selected));
        }
        model.addAttribute("userRows", rows);

        // Current selection option for the group
        String currentEvalState = evaluationService.updateEvaluationState(evaluationId);
        String selectionOption = EvalUtils.getSelectionSetting(evalCategory, null, eval);
        if (EvalUtils.checkStateBefore(currentEvalState, EvalConstants.EVALUATION_STATE_PARTIAL, true)) {
            selectionOption = EvalAssignGroup.SELECTION_OPTION_MULTIPLE;
        }
        model.addAttribute("selectionOption", selectionOption);
        model.addAttribute("selectionOptionAll",      EvalAssignGroup.SELECTION_OPTION_ALL);
        model.addAttribute("selectionOptionOne",      EvalAssignGroup.SELECTION_OPTION_ONE);
        model.addAttribute("selectionOptionMultiple", EvalAssignGroup.SELECTION_OPTION_MULTIPLE);

        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("evalGroupId",  evalGroupId);
        model.addAttribute("evalCategory", evalCategory);
        model.addAttribute("isInstructor", isInstructor);
        model.addAttribute("groupTitle",   groupTitle);
        model.addAttribute("evalTitle",    eval.getTitle());

        return "evaluation_assign_select :: content";
    }

    @PostMapping("/save")
    public void save(@RequestParam Long evaluationId,
                     @RequestParam String evalGroupId,
                     @RequestParam String evalCategory,
                     @RequestParam(required = false, defaultValue = "") String selectionOption,
                     @RequestParam(value = "selectedUserIds", required = false) List<String> selectedUserIds,
                     HttpServletResponse response) {

        String currentUserId = commonLogic.getCurrentUserId();
        boolean isInstructor = EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR.equals(evalCategory);
        String assignType    = isInstructor ? EvalAssignUser.TYPE_EVALUATEE : EvalAssignUser.TYPE_ASSISTANT;

        // 1. Save selectionOption on EvalAssignGroup
        if (!selectionOption.isEmpty()) {
            Map<Long, List<EvalAssignGroup>> groupsMap = evaluationService.getAssignGroupsForEvals(
                    new Long[]{ evaluationId }, true, null);
            List<EvalAssignGroup> groups = groupsMap.get(evaluationId);
            if (groups != null) {
                for (EvalAssignGroup ag : groups) {
                    if (evalGroupId.equals(ag.getEvalGroupId())) {
                        ag.setSelectionOption(evalCategory, selectionOption);
                        evaluationSetupService.saveAssignGroup(ag, currentUserId);
                        break;
                    }
                }
            }
        }

        // 2. Update EvalAssignUser statuses
        List<EvalAssignUser> allUsers = evaluationService.getParticipantsForEval(
                evaluationId, null, new String[]{ evalGroupId }, assignType,
                EvalEvaluationService.STATUS_ANY, null, null);

        List<EvalAssignUser> toSave = new ArrayList<>();
        for (EvalAssignUser au : allUsers) {
            boolean nowSelected = selectedUserIds != null && selectedUserIds.contains(au.getUserId());
            String newStatus = nowSelected ? EvalAssignUser.STATUS_LINKED : EvalAssignUser.STATUS_REMOVED;
            if (!newStatus.equals(au.getStatus())) {
                au.setStatus(newStatus);
                toSave.add(au);
            }
        }
        if (!toSave.isEmpty()) {
            evaluationSetupService.saveUserAssignments(evaluationId, toSave.toArray(new EvalAssignUser[0]));
        }

        log.info("User ({}) saved assign-select for eval={}, group={}, type={}", currentUserId, evaluationId, evalGroupId, evalCategory);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
