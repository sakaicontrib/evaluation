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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of ChownEvaluationProducer.
 */
@Slf4j
@Controller
@RequestMapping("/chown_eval")
public class ChownEvalController extends EvalControllerSupport {

    @GetMapping
    public String show(@RequestParam Long evaluationId,
                       @RequestParam(required = false) String errorMessage,
                       Model model) {
        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        model.addAttribute("eval", eval);
        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("errorMessage", errorMessage);
        return "chown_eval";
    }

    @PostMapping
    public String chown(@RequestParam Long evaluationId,
                        @RequestParam String newOwner,
                        RedirectAttributes redirectAttributes) {

        String currentUserId = currentUserId();
        EvalEvaluation evaluation = evaluationService.getEvaluationById(evaluationId);

        // resolve new owner: try by username, then by internal id, then by email
        String newUserId = resolveUser(newOwner);
        if (newUserId == null) {
            redirectAttributes.addAttribute("evaluationId", evaluationId);
            redirectAttributes.addAttribute("errorMessage", "controltemplates.chown.nouser.message");
            return "redirect:/chown_eval";
        }

        if (!evaluationService.canControlEvaluation(currentUserId, evaluationId)
                || !evaluationService.canBeginEvaluation(newUserId)) {
            redirectAttributes.addAttribute("evaluationId", evaluationId);
            redirectAttributes.addAttribute("errorMessage", "controlevaluations.chown.permission.message");
            return "redirect:/chown_eval";
        }

        evaluationService.updateEvaluationOwner(evaluationId, newUserId);
        return "redirect:/control_evaluations";
    }

    private String resolveUser(String input) {
        String internalId = commonLogic.getUserId(input);
        if (internalId == null) internalId = input;
        EvalUser user = commonLogic.getEvalUserById(internalId);
        if (isValidUser(user)) return user.userId;
        user = commonLogic.getEvalUserByEmail(input);
        if (isValidUser(user)) return user.userId;
        return null;
    }

    private boolean isValidUser(EvalUser user) {
        return user != null && (EvalConstants.USER_TYPE_EXTERNAL.equals(user.type)
                || EvalConstants.USER_TYPE_INTERNAL.equals(user.type));
    }
}