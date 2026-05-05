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

import javax.annotation.Resource;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;

/**
 * First step of the evaluation creation wizard.
 * Spring MVC equivalent of EvaluationCreateProducer.
 */
@Controller
@RequestMapping("/evaluation_create")
public class EvaluationCreateController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationSetupService")
    private EvalEvaluationSetupService evaluationSetupService;

    @Resource(name = "org.sakaiproject.evaluation.beans.EvalBeanUtils")
    private EvalBeanUtils evalBeanUtils;

    // DTO for template row in the selection table
    @Data
    public static class TemplateRow {
        Long id;
        String title;
        String ownerName;
    }

    @GetMapping
    public String show(@RequestParam(required = false) Long templateId, Model model) {
        String currentUserId = commonLogic.getCurrentUserId();

        List<EvalTemplate> templates = authoringService.getTemplatesForUser(currentUserId, null, false);

        List<TemplateRow> templateRows = new ArrayList<>();
        for (EvalTemplate t : templates) {
            TemplateRow row = new TemplateRow();
            row.setId(t.getId());
            row.setTitle(t.getTitle());
            row.setOwnerName(commonLogic.getEvalUserById(t.getOwner()).displayName);
            templateRows.add(row);
        }

        model.addAttribute("templateRows", templateRows);
        model.addAttribute("templateId", templateId);

        if (templateId != null) {
            model.addAttribute("selectedTemplate", authoringService.getTemplateById(templateId));
        }

        // If there are no templates the user should not reach here, but as a safeguard:
        model.addAttribute("hasTemplates", !templateRows.isEmpty());

        return "evaluation_create";
    }

    @PostMapping
    public String create(@RequestParam(required = false) String title,
                         @RequestParam(required = false) String instructions,
                         @RequestParam Long templateId) {
        String currentUserId = commonLogic.getCurrentUserId();

        // Create a new evaluation in PARTIAL state (same process as EvaluationBeanLocator)
        EvalEvaluation eval = new EvalEvaluation(
                EvalConstants.EVALUATION_TYPE_EVALUATION,
                currentUserId,
                title,
                null,
                EvalConstants.EVALUATION_STATE_PARTIAL,
                null, null, null
        );
        eval.setInstructions(instructions);

        // Apply default values (same as EvaluationBeanLocator.saveAll for new*)
        evalBeanUtils.setEvaluationDefaults(eval, EvalConstants.EVALUATION_TYPE_EVALUATION);

        // Assign template
        EvalTemplate template = authoringService.getTemplateById(templateId);
        eval.setTemplate(template);

        // Save in PARTIAL state (false = do not activate)
        evaluationSetupService.saveEvaluation(eval, currentUserId, false);

        return "redirect:/evaluation_settings?evaluationId=" + eval.getId();
    }
}
