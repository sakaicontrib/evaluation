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

import java.util.Objects;

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
public class ModifyEmailController extends EvalControllerSupport {


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

        String userId = currentUserId();
        Long[] savedTemplateId = new Long[1];

        daoInvoker.invokeTransactionalAccess(() -> {
            EvalEmailTemplate template = loadTemplate(templateId, emailType, evaluationId);

            // A brand-new template pre-populated from the default: if the user submits it
            // without changing anything, skip saving so we don't permanently clone the
            // default template for every evaluation that never customizes its emails.
            boolean unchanged = template.getId() == null
                    && textEquals(template.getSubject(), subject)
                    && textEquals(template.getMessage(), message);

            if (!unchanged) {
                template.setSubject(subject);
                template.setMessage(message);
                if (template.getType() == null && emailType != null) {
                    template.setType(emailType);
                }
                evaluationSetupService.saveEmailTemplate(template, userId);

                if (evaluationId != null) {
                    evaluationSetupService.assignEmailTemplate(template.getId(), evaluationId, null, userId);
                }
            }

            savedTemplateId[0] = template.getId();
        });

        if (evaluationId != null) {
            StringBuilder redirect = new StringBuilder("redirect:/preview_email?evaluationId=").append(evaluationId);
            if (savedTemplateId[0] != null) {
                redirect.append("&templateId=").append(savedTemplateId[0]);
            }
            if (emailType != null) {
                redirect.append("&emailType=").append(emailType);
            }
            return redirect.toString();
        }
        return "redirect:/control_email_templates";
    }

    /**
     * Compares text ignoring line-ending differences: browsers normalize textarea
     * line breaks to CRLF on submission while stored templates use LF.
     */
    private boolean textEquals(String a, String b) {
        String na = a != null ? a.replace("\r\n", "\n") : null;
        String nb = b != null ? b.replace("\r\n", "\n") : null;
        return Objects.equals(na, nb);
    }

    @PostMapping("/reset")
    public String reset(@RequestParam Long evaluationId,
                        @RequestParam String emailType) {
        String userId = currentUserId();
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
        t.setOwner(currentUserId());
        if (emailType != null) t.setType(emailType);
        if (defaultTemplate != null) {
            t.setSubject(defaultTemplate.getSubject());
            t.setMessage(defaultTemplate.getMessage());
        }
        return t;
    }
}
