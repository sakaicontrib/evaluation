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

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of ModifyEmailProducer.
 */
@Slf4j
@Controller
@RequestMapping("/modify_email")
public class ModifyEmailController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationService")
    private EvalEvaluationService evaluationService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationSetupService")
    private EvalEvaluationSetupService evaluationSetupService;

    @GetMapping
    public String show(@RequestParam(required = false) Long templateId,
                       @RequestParam(required = false) String emailType,
                       @RequestParam(required = false) Long evaluationId,
                       Model model) {

        EvalEmailTemplate template = loadTemplate(templateId, emailType, evaluationId);
        boolean isNew = (template.getId() == null);
        boolean canReset = (evaluationId != null && !isNew && template.getDefaultType() == null);

        model.addAttribute("template", template);
        model.addAttribute("templateId", template.getId());
        model.addAttribute("emailType", emailType);
        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("canReset", canReset);
        model.addAttribute("standalone", evaluationId == null);
        return "modify_email";
    }

    @PostMapping
    public String save(@RequestParam(required = false) Long templateId,
                       @RequestParam(required = false) String emailType,
                       @RequestParam(required = false) Long evaluationId,
                       @RequestParam String subject,
                       @RequestParam String message) {

        String userId = commonLogic.getCurrentUserId();
        EvalEmailTemplate template = loadTemplate(templateId, emailType, evaluationId);
        template.setSubject(subject);
        template.setMessage(message);
        if (template.getType() == null && emailType != null) {
            template.setType(emailType);
        }
        evaluationSetupService.saveEmailTemplate(template, userId);

        if (evaluationId != null) {
            evaluationSetupService.assignEmailTemplate(template.getId(), evaluationId, null, userId);
            return "redirect:/preview_email?templateId=" + template.getId()
                    + (emailType != null ? "&emailType=" + emailType : "")
                    + "&evaluationId=" + evaluationId;
        }
        return "redirect:/control_email_templates";
    }

    @PostMapping("/reset")
    public String reset(@RequestParam Long evaluationId,
                        @RequestParam String emailType) {
        String userId = commonLogic.getCurrentUserId();
        evaluationSetupService.assignEmailTemplate(null, evaluationId, emailType, userId);
        return "redirect:/preview_email?emailType=" + emailType + "&evaluationId=" + evaluationId;
    }

    private EvalEmailTemplate loadTemplate(Long templateId, String emailType, Long evaluationId) {
        if (templateId != null) {
            return evaluationService.getEmailTemplate(templateId);
        }
        EvalEmailTemplate defaultTemplate = null;
        if (evaluationId != null && emailType != null) {
            EvalEmailTemplate t = evaluationService.getEmailTemplate(evaluationId, emailType);
            if (t != null && t.getDefaultType() == null) {
                return t; // non-default template assigned to this eval
            }
            defaultTemplate = t; // save default content for pre-population
        }
        // new template, pre-populated from default if available
        EvalEmailTemplate t = new EvalEmailTemplate();
        t.setOwner(commonLogic.getCurrentUserId());
        if (emailType != null) t.setType(emailType);
        if (defaultTemplate != null) {
            t.setSubject(defaultTemplate.getSubject());
            t.setMessage(defaultTemplate.getMessage());
        }
        return t;
    }
}