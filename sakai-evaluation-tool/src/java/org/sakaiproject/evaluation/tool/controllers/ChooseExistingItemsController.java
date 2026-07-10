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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;

/**
 * Select existing items to add to a template.
 * Spring MVC equivalent of ExistingItemsProducer + ExpertItemsBean.processActionAddItems().
 */
@Controller
@RequestMapping("/choose_existing_items")
public class ChooseExistingItemsController extends EvalControllerSupport {

    @Data
    public static class ItemRow {
        private final Long itemId;
        private final String itemText;
        private final String scaleInfo;
        private final boolean odd;
    }

    @GetMapping
    public String show(@RequestParam Long templateId,
                       @RequestParam(required = false, defaultValue = "") String searchString,
                       Model model) {

        String currentUserId = currentUserId();
        String filter = searchString.isEmpty() ? null : searchString;
        List<EvalItem> items = authoringService.getItemsForUser(currentUserId, null, filter, false);

        List<ItemRow> itemRows = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            EvalItem item = items.get(i);
            String scaleInfo;
            if (item.getScale() != null) {
                StringBuilder sb = new StringBuilder(item.getScale().getTitle()).append(" (");
                List<String> opts = item.getScale().getOptions();
                for (int j = 0; j < opts.size(); j++) {
                    if (j > 0) sb.append(",");
                    sb.append(opts.get(j));
                }
                sb.append(")");
                scaleInfo = sb.toString();
            } else {
                scaleInfo = item.getClassification();
            }
            itemRows.add(new ItemRow(item.getId(), item.getItemText(), scaleInfo, i % 2 == 0));
        }

        model.addAttribute("templateId", templateId);
        model.addAttribute("searchString", searchString);
        model.addAttribute("hasSearch", !searchString.isEmpty());
        model.addAttribute("itemRows", itemRows);
        model.addAttribute("hasItems", !itemRows.isEmpty());
        return "choose_existing_items";
    }

    @PostMapping
    public String insert(@RequestParam Long templateId,
                         @RequestParam(required = false) List<Long> selectedIds) {

        if (selectedIds != null && !selectedIds.isEmpty()) {
            String currentUserId = currentUserId();
            EvalTemplate template = authoringService.getTemplateById(templateId);

            String hierarchyLevel = EvalConstants.HIERARCHY_LEVEL_TOP;
            String hierarchyNodeId = EvalConstants.HIERARCHY_NODE_ID_NONE;
            if (EvalConstants.TEMPLATE_TYPE_ADDED.equals(template.getType())) {
                hierarchyLevel = EvalConstants.HIERARCHY_LEVEL_INSTRUCTOR;
                hierarchyNodeId = currentUserId;
            }

            for (Long itemId : selectedIds) {
                EvalItem item = authoringService.getItemById(itemId);
                if (item == null) continue;
                EvalTemplateItem templateItem = TemplateItemUtils.makeTemplateItem(item);
                templateItem.setOwner(currentUserId);
                templateItem.setTemplate(template);
                templateItem.setHierarchyLevel(hierarchyLevel);
                templateItem.setHierarchyNodeId(hierarchyNodeId);
                authoringService.saveTemplateItem(templateItem, currentUserId);
            }
        }

        return "redirect:/modify_template_items?templateId=" + templateId;
    }
}