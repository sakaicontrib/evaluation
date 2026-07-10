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

import org.sakaiproject.evaluation.model.EvalTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of ChownTemplateProducer.
 */
@Slf4j
@Controller
@RequestMapping("/chown_template")
public class ChownTemplateController extends EvalControllerSupport {

    @GetMapping
    public String show(@RequestParam Long templateId,
                       @RequestParam(required = false) String errorMessage,
                       Model model) {
        String currentUserId = currentUserId();
        EvalTemplate template = authoringService.getTemplateById(templateId);
        boolean canModify = authoringService.canModifyTemplate(currentUserId, templateId);

        model.addAttribute("template", template);
        model.addAttribute("templateId", templateId);
        model.addAttribute("canModify", canModify);
        model.addAttribute("errorMessage", errorMessage);
        return "chown_template";
    }

    @PostMapping
    public String chown(@RequestParam Long templateId,
                        @RequestParam String newOwner,
                        RedirectAttributes redirectAttributes) {

        String currentUserId = currentUserId();
        EvalTemplate template = authoringService.getTemplateById(templateId);

        String newUserId = commonLogic.getUserId(newOwner);
        if (newUserId == null) {
            redirectAttributes.addAttribute("templateId", templateId);
            redirectAttributes.addAttribute("errorMessage", "controltemplates.chown.nouser.message");
            return "redirect:/chown_template";
        }

        if (!authoringService.canCreateTemplate(newUserId)
                || !authoringService.canModifyTemplate(currentUserId, templateId)) {
            redirectAttributes.addAttribute("templateId", templateId);
            redirectAttributes.addAttribute("errorMessage", "controltemplates.chown.permission.message");
            return "redirect:/chown_template";
        }

        authoringService.copyTemplate(templateId, template.getTitle() + " (shared)", newUserId, false, true);
        authoringService.deleteTemplate(templateId, currentUserId);
        return "redirect:/control_templates";
    }
}