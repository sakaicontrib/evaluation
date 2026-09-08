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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
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
@RequestMapping("/remove_eval")
public class RemoveEvalController extends EvalControllerSupport {

    @Data
    public static class AdhocGroupRow {
        Long   adhocGroupId;
        String title;
    }

    @GetMapping
    public String show(@RequestParam Long evaluationId, Model model) {
        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);

        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        String assigned;
        int count = evaluationService.countEvaluationGroups(eval.getId(), false);
        Map<Long, List<EvalGroup>> groupsByEval = evaluationService.getEvalGroupsForEval(
                new Long[]{eval.getId()}, true, null);
        List<EvalGroup> groups = groupsByEval.get(eval.getId());
        if (count > 1) {
            assigned = count + " groups";
        } else if (count == 1) {
            assigned = groups.get(0).title;
        } else {
            assigned = null;
        }

        // Adhoc groups assigned to this evaluation that would be left unused by any
        // evaluation once this one is removed - offered as an opt-in cleanup rather than
        // guessed at automatically, since there is no reliable way to tell a group the
        // system auto-created apart from one the user deliberately picked or reused.
        List<AdhocGroupRow> orphanedAdhocGroups = new ArrayList<>();
        if (groups != null) {
            for (EvalGroup group : groups) {
                if (EvalConstants.GROUP_TYPE_ADHOC.equals(group.type)) {
                    List<EvalEvaluation> evalsUsingGroup = evaluationService.getEvaluationsForEvalGroups(
                            new String[] {group.evalGroupId}, 0, 0);
                    boolean wouldBecomeUnused = evalsUsingGroup.stream()
                            .allMatch(e -> e.getId().equals(evaluationId));
                    if (wouldBecomeUnused) {
                        AdhocGroupRow row = new AdhocGroupRow();
                        row.setAdhocGroupId(EvalAdhocGroup.getIdFromAdhocEvalGroupId(group.evalGroupId));
                        row.setTitle(group.title);
                        orphanedAdhocGroups.add(row);
                    }
                }
            }
        }

        model.addAttribute("eval", eval);
        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("evalStartDate", df.format(eval.getStartDate()));
        model.addAttribute("evalDueDate", eval.getDueDate() != null ? df.format(eval.getDueDate()) : "");
        model.addAttribute("assigned", assigned);
        model.addAttribute("orphanedAdhocGroups", orphanedAdhocGroups);
        return "remove_eval";
    }

    @PostMapping
    public String remove(@RequestParam Long evaluationId,
                          @RequestParam(required = false) Long[] deleteAdhocGroupIds) {
        String userId = currentUserId();
        evaluationSetupService.deleteEvaluation(evaluationId, userId);

        if (deleteAdhocGroupIds != null) {
            for (Long adhocGroupId : deleteAdhocGroupIds) {
                removeIfStillUnused(evaluationId, adhocGroupId);
            }
        }
        return "redirect:/control_evaluations";
    }

    /**
     * Re-checks the group is still unused right before deleting it (something may have
     * changed between the confirmation page being shown and this submit), same caution
     * already used by ControlAdhocGroupsController before a manual delete.
     */
    private void removeIfStillUnused(Long evaluationId, Long adhocGroupId) {
        EvalAdhocGroup adhocGroup = commonLogic.getAdhocGroupById(adhocGroupId);
        if (adhocGroup == null) {
            return;
        }
        List<EvalEvaluation> stillUsing = evaluationService.getEvaluationsForEvalGroups(
                new String[] {adhocGroup.getEvalGroupId()}, 0, 0);
        if (!stillUsing.isEmpty()) {
            log.info("Skipped removing adhoc group (" + adhocGroupId + ") after deleting evaluation ("
                    + evaluationId + "): it is now used by another evaluation");
            return;
        }
        try {
            commonLogic.deleteAdhocGroup(adhocGroupId);
            log.info("Removed adhoc group (" + adhocGroupId + ") at the user's request while deleting evaluation ("
                    + evaluationId + ")");
        } catch (SecurityException e) {
            log.warn("Could not remove adhoc group (" + adhocGroupId + "): " + e.getMessage());
        }
    }
}