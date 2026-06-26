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

import javax.annotation.Resource;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of ShowEvalCategoryProducer.
 * Shows all evaluations in a category and their associated groups.
 */
@Slf4j
@Controller
@RequestMapping("/show_eval_category")
public class ShowEvalCategoryController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationService")
    private EvalEvaluationService evaluationService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationSetupService")
    private EvalEvaluationSetupService evaluationSetupService;

    @GetMapping
    public String show(@RequestParam String evalCategory,
                       @RequestParam(required = false, defaultValue = "false") boolean external,
                       Locale locale,
                       Model model) {

        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        model.addAttribute("evalCategory", evalCategory);
        model.addAttribute("external", external);

        List<EvalEvaluation> evals = evaluationSetupService.getEvaluationsByCategory(evalCategory, null);

        if (!evals.isEmpty()) {
            Long[] evalIds = evals.stream().map(EvalEvaluation::getId).toArray(Long[]::new);
            Map<Long, List<EvalGroup>> groupsByEval = evaluationService.getEvalGroupsForEval(evalIds, false, null);

            // Enrich each evaluation with its updated state and groups
            for (EvalEvaluation eval : evals) {
                String state = evaluationService.updateEvaluationState(eval.getId());
                eval.setState(state); // reuse the state field from the model
            }

            model.addAttribute("evals", evals);
            model.addAttribute("groupsByEval", groupsByEval);
            model.addAttribute("df", df);
            model.addAttribute("ACTIVE", EvalConstants.EVALUATION_STATE_ACTIVE);
            model.addAttribute("INVALID", EvalConstants.GROUP_TYPE_INVALID);
        } else {
            model.addAttribute("evals", List.of());
        }

        return "show_eval_category";
    }
}