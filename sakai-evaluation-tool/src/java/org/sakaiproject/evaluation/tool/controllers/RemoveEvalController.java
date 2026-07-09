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
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/remove_eval")
public class RemoveEvalController extends EvalControllerSupport {


    @GetMapping
    public String show(@RequestParam Long evaluationId, Model model) {
        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);

        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        String assigned;
        int count = evaluationService.countEvaluationGroups(eval.getId(), false);
        if (count > 1) {
            assigned = count + " groups";
        } else if (count == 1) {
            Map<Long, List<EvalGroup>> groups = evaluationService.getEvalGroupsForEval(
                    new Long[]{eval.getId()}, true, null);
            assigned = groups.get(eval.getId()).get(0).title;
        } else {
            assigned = null;
        }

        model.addAttribute("eval", eval);
        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("evalStartDate", df.format(eval.getStartDate()));
        model.addAttribute("evalDueDate", eval.getDueDate() != null ? df.format(eval.getDueDate()) : "");
        model.addAttribute("assigned", assigned);
        return "remove_eval";
    }

    @PostMapping
    public String remove(@RequestParam Long evaluationId) {
        String userId = currentUserId();
        evaluationSetupService.deleteEvaluation(evaluationId, userId);
        return "redirect:/control_evaluations";
    }
}