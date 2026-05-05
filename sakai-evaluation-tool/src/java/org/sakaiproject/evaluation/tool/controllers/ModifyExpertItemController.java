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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of ModifyExpertItemProducer.
 */
@Slf4j
@Controller
@RequestMapping("/modify_expert_item")
public class ModifyExpertItemController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @GetMapping
    public String show(@RequestParam String type,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) Long objectiveId,
                       @RequestParam(defaultValue = "false") boolean isNew,
                       Model model) {

        String currentUserId = commonLogic.getCurrentUserId();
        if (!commonLogic.isUserAdmin(currentUserId)) {
            throw new SecurityException("Non-admin users may not access this page");
        }

        boolean isCategory = EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(type);
        EvalItemGroup group = null;
        String parentTitle = null;

        if (!isNew) {
            Long eigId = isCategory ? categoryId : objectiveId;
            group = authoringService.getItemGroupById(eigId);
        } else if (!isCategory && categoryId != null) {
            parentTitle = authoringService.getItemGroupById(categoryId).getTitle();
        }

        model.addAttribute("type", type);
        model.addAttribute("isCategory", isCategory);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("objectiveId", objectiveId);
        model.addAttribute("isNew", isNew);
        model.addAttribute("group", group);
        model.addAttribute("parentTitle", parentTitle);
        return "modify_expert_item";
    }

    @PostMapping
    public String save(@RequestParam String type,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) Long objectiveId,
                       @RequestParam(required = false) Long parentId,
                       @RequestParam(defaultValue = "false") boolean isNew,
                       @RequestParam String title,
                       @RequestParam(required = false) String description) {

        String currentUserId = commonLogic.getCurrentUserId();
        boolean isCategory = EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(type);
        EvalItemGroup group;

        if (isNew) {
            if (isCategory) {
                group = new EvalItemGroup(currentUserId, type, title, description, Boolean.TRUE, null, null);
            } else {
                EvalItemGroup parent = authoringService.getItemGroupById(parentId != null ? parentId : categoryId);
                group = new EvalItemGroup(currentUserId, type, title, description, Boolean.TRUE, parent, null);
            }
        } else {
            Long eigId = isCategory ? categoryId : objectiveId;
            group = authoringService.getItemGroupById(eigId);
            group.setTitle(title);
            group.setDescription(description);
        }

        authoringService.saveItemGroup(group, currentUserId);
        return "redirect:/control_expert_items";
    }
}