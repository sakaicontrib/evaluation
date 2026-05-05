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

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/remove_item")
public class RemoveItemController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @GetMapping
    public String show(@RequestParam(required = false) Long itemId,
                       @RequestParam(required = false) Long templateItemId,
                       @RequestParam(required = false) Long templateId,
                       Model model) {

        model.addAttribute("itemId", itemId);
        model.addAttribute("templateItemId", templateItemId);
        model.addAttribute("templateId", templateId);

        if (templateItemId != null) {
            EvalTemplateItem ti = authoringService.getTemplateItemById(templateItemId);
            EvalItem item = ti.getItem();
            boolean isBlockParent = TemplateItemUtils.isBlockParent(ti);
            model.addAttribute("itemText", item.getItemText());
            model.addAttribute("isTemplateItem", true);
            model.addAttribute("isBlockParent", isBlockParent);
            if (templateId == null) {
                model.addAttribute("templateId", ti.getTemplate().getId());
            }
        } else if (itemId != null) {
            EvalItem item = authoringService.getItemById(itemId);
            model.addAttribute("itemText", item.getItemText());
            model.addAttribute("isTemplateItem", false);
            model.addAttribute("isBlockParent", false);
        } else {
            throw new IllegalArgumentException("itemId or templateItemId required");
        }
        return "remove_item";
    }

    @PostMapping
    public String remove(@RequestParam(required = false) Long itemId,
                         @RequestParam(required = false) Long templateItemId,
                         @RequestParam(required = false) Long templateId) {
        String userId = commonLogic.getCurrentUserId();
        if (templateItemId != null) {
            authoringService.deleteTemplateItem(templateItemId, userId);
            return "redirect:/modify_template_items?templateId=" + templateId;
        } else {
            authoringService.deleteItem(itemId, userId);
            return "redirect:/control_items";
        }
    }
}