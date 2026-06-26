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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Wizard step 3: select items to insert. Equivalent of ExpertItemsProducer.
 */
@Slf4j
@Controller
@RequestMapping("/choose_expert_items")
public class ChooseExpertItemsController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @Data
    public static class ItemRow {
        private final Long id;
        private final String itemText;
        private final String scaleText;
        private final String expertDescription;
        private final boolean odd;
    }

    @GetMapping
    public String show(@RequestParam Long templateId,
                       @RequestParam Long categoryId,
                       @RequestParam Long objectiveId,
                       Model model) {

        EvalItemGroup category = authoringService.getItemGroupById(categoryId);
        EvalItemGroup objective = authoringService.getItemGroupById(objectiveId);
        List<EvalItem> items = authoringService.getItemsInItemGroup(objectiveId, true);

        List<ItemRow> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            EvalItem item = items.get(i);
            String scaleText = item.getScale() != null ? ScaledUtils.makeScaleText(item.getScale(), 0) : "";
            rows.add(new ItemRow(item.getId(), item.getItemText(), scaleText, item.getExpertDescription(), i % 2 != 0));
        }

        model.addAttribute("templateId", templateId);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("objectiveId", objectiveId);
        model.addAttribute("categoryTitle", category.getTitle());
        model.addAttribute("objectiveTitle", objective.getTitle());
        model.addAttribute("items", rows);
        return "choose_expert_items";
    }

    @PostMapping
    public String insert(@RequestParam Long templateId,
                         @RequestParam(required = false) String[] selectedIds) {

        if (selectedIds == null || selectedIds.length == 0) {
            return "redirect:/modify_template_items?templateId=" + templateId;
        }

        String currentUserId = commonLogic.getCurrentUserId();
        EvalTemplate template = authoringService.getTemplateById(templateId);

        String hierarchyLevel = EvalConstants.HIERARCHY_LEVEL_TOP;
        String hierarchyNodeId = EvalConstants.HIERARCHY_NODE_ID_NONE;
        if (EvalConstants.TEMPLATE_TYPE_ADDED.equals(template.getType())) {
            hierarchyLevel = EvalConstants.HIERARCHY_LEVEL_INSTRUCTOR;
            hierarchyNodeId = currentUserId;
        }

        for (String idStr : selectedIds) {
            Long itemId = Long.parseLong(idStr);
            EvalItem item = authoringService.getItemById(itemId);
            if (item == null) continue;
            EvalTemplateItem templateItem = TemplateItemUtils.makeTemplateItem(item);
            templateItem.setOwner(currentUserId);
            templateItem.setTemplate(template);
            templateItem.setHierarchyLevel(hierarchyLevel);
            templateItem.setHierarchyNodeId(hierarchyNodeId);
            authoringService.saveTemplateItem(templateItem, currentUserId);
            log.info("Added expert item ({}) to template ({})", itemId, templateId);
        }

        return "redirect:/modify_template_items?templateId=" + templateId;
    }
}