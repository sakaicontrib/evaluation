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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/remove_template")
public class RemoveTemplateController extends EvalControllerSupport {

    @GetMapping
    public String show(@RequestParam Long templateId, Model model) {
        String userId = currentUserId();
        EvalTemplate template = authoringService.getTemplateById(templateId);
        boolean canRemove = authoringService.canRemoveTemplate(userId, templateId);

        model.addAttribute("template", template);
        model.addAttribute("templateId", templateId);
        model.addAttribute("canRemove", canRemove);
        return "remove_template";
    }

    @PostMapping
    public String remove(@RequestParam Long templateId) {
        String userId = currentUserId();
        authoringService.deleteTemplate(templateId, userId);
        return "redirect:/control_templates";
    }
}