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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/control_adhoc_groups")
public class ControlAdhocGroupsController extends EvalControllerSupport {

    @Data
    public static class EvalInfo {
        private final Long evalId;
        private final String title;
        private final String stateKey;
        private final boolean locked;
    }

    @Data
    public static class GroupRow {
        private final Long groupId;
        private final String title;
        private final int memberCount;
        private final List<EvalInfo> evaluations;
        private final boolean canEdit;
        private final boolean canDelete;
    }

    @GetMapping
    public String show(Model model) {
        String userId = commonLogic.getCurrentUserId();
        if (commonLogic.isUserAnonymous(userId))
            throw new SecurityException("Anonymous users cannot access adhoc groups");

        if (!Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_ADHOC_GROUPS)))
            return "redirect:/summary";

        List<EvalAdhocGroup> groups = commonLogic.getAdhocGroupsForOwner(userId);
        List<GroupRow> rows = new ArrayList<>();

        for (EvalAdhocGroup group : groups) {
            List<EvalEvaluation> evals = evaluationService.getEvaluationsForEvalGroups(
                    new String[]{group.getEvalGroupId()}, 0, 0);

            boolean anyLocked = false;
            List<EvalInfo> evalInfos = new ArrayList<>();
            for (EvalEvaluation eval : evals) {
                String state = evaluationService.updateEvaluationState(eval.getId());
                boolean locked = isLockedState(state);
                if (locked) anyLocked = true;
                evalInfos.add(new EvalInfo(eval.getId(), eval.getTitle(),
                        "state.label." + state.toLowerCase(), locked));
            }

            int memberCount = group.getParticipantIds() != null ? group.getParticipantIds().size() : 0;
            rows.add(new GroupRow(group.getId(), group.getTitle(), memberCount, evalInfos,
                    !anyLocked, evals.isEmpty()));
        }

        model.addAttribute("groupRows", rows);
        return "control_adhoc_groups";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long adhocGroupId, RedirectAttributes ra) {
        String userId = commonLogic.getCurrentUserId();

        EvalAdhocGroup group = commonLogic.getAdhocGroupById(adhocGroupId);
        if (group == null)
            throw new IllegalArgumentException("Adhoc group not found: " + adhocGroupId);
        if (!userId.equals(group.getOwner()))
            throw new SecurityException("Only the owner can delete an adhoc group: " + userId);

        List<EvalEvaluation> evals = evaluationService.getEvaluationsForEvalGroups(
                new String[]{group.getEvalGroupId()}, 0, 0);
        for (EvalEvaluation eval : evals) {
            if (isLockedState(evaluationService.updateEvaluationState(eval.getId()))) {
                ra.addFlashAttribute("errorMessage", "controladhocgroups.cannot.delete.locked");
                return "redirect:/control_adhoc_groups";
            }
        }

        commonLogic.deleteAdhocGroup(adhocGroupId);
        log.info("User ({}) deleted adhoc group ({})", userId, adhocGroupId);
        ra.addFlashAttribute("successMessage", "controladhocgroups.deleted");
        return "redirect:/control_adhoc_groups";
    }

    static boolean isLockedState(String state) {
        return EvalConstants.EVALUATION_STATE_ACTIVE.equals(state)
                || EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(state)
                || EvalConstants.EVALUATION_STATE_CLOSED.equals(state)
                || EvalConstants.EVALUATION_STATE_VIEWABLE.equals(state);
    }
}