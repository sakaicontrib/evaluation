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
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of PreviewExpertItemProducer.
 */
@Slf4j
@Controller
@RequestMapping("/preview_expert_items")
public class PreviewExpertItemController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @Data
    public static class ItemRow {
        private final String itemText;
        private final String scaleOrType;
        private final String expertDescription;
        private final boolean odd;
    }

    @GetMapping
    public String show(@RequestParam String type,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) Long objectiveId,
                       Model model) {

        boolean isCategory = EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(type);
        Long eigId = isCategory ? categoryId : objectiveId;
        EvalItemGroup group = authoringService.getItemGroupById(eigId);

        List<EvalItem> items = authoringService.getItemsInItemGroup(eigId, true);
        List<ItemRow> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            EvalItem item = items.get(i);
            String scaleOrType = item.getScale() != null
                    ? ScaledUtils.makeScaleText(item.getScale(), 0)
                    : item.getClassification();
            rows.add(new ItemRow(item.getItemText(), scaleOrType, item.getExpertDescription(), i % 2 != 0));
        }

        model.addAttribute("group", group);
        model.addAttribute("isCategory", isCategory);
        model.addAttribute("type", type);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("objectiveId", objectiveId);
        model.addAttribute("items", rows);
        return "preview_expert_items";
    }
}