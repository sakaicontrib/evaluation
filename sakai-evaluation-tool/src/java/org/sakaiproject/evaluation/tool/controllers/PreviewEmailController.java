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

import javax.annotation.Resource;

import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of PreviewEmailProducer.
 */
@Slf4j
@Controller
@RequestMapping("/preview_email")
public class PreviewEmailController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationService")
    private EvalEvaluationService evaluationService;

    @GetMapping
    public String show(@RequestParam(required = false) Long templateId,
                       @RequestParam(required = false) String emailType,
                       @RequestParam(required = false) Long evaluationId,
                       Model model) {

        EvalEmailTemplate template = null;
        if (templateId != null) {
            template = evaluationService.getEmailTemplate(templateId);
        }
        if (template == null && emailType != null) {
            template = evaluationService.getEmailTemplate(evaluationId, emailType);
        }

        String messageHtml = template != null && template.getMessage() != null
                ? template.getMessage().replaceAll("(\r\n|\r|\n)", "<br/>")
                : "";

        model.addAttribute("template", template);
        model.addAttribute("templateId", templateId);
        model.addAttribute("emailType", emailType);
        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("messageHtml", messageHtml);
        return "preview_email";
    }
}