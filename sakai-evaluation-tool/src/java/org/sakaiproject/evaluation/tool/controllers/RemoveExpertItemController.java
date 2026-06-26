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
 * Spring MVC equivalent of RemoveExpertItemProducer.
 */
@Slf4j
@Controller
@RequestMapping("/remove_expert_item")
public class RemoveExpertItemController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @GetMapping
    public String show(@RequestParam String type,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) Long objectiveId,
                       Model model) {

        boolean isCategory = EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(type);
        Long eigId = isCategory ? categoryId : objectiveId;
        EvalItemGroup group = authoringService.getItemGroupById(eigId);

        model.addAttribute("group", group);
        model.addAttribute("type", type);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("objectiveId", objectiveId);
        return "remove_expert_item";
    }

    @PostMapping
    public String remove(@RequestParam String type,
                         @RequestParam(required = false) Long categoryId,
                         @RequestParam(required = false) Long objectiveId) {

        boolean isCategory = EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(type);
        Long eigId = isCategory ? categoryId : objectiveId;
        authoringService.removeItemGroup(eigId, commonLogic.getCurrentUserId(), Boolean.FALSE);
        return "redirect:/control_expert_items";
    }
}