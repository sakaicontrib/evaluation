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
 * Wizard step 2: choose objective. Equivalent of ExpertObjectiveProducer.
 * Also shows direct items of the category if any exist.
 */
@Slf4j
@Controller
@RequestMapping("/choose_expert_objective")
public class ChooseExpertObjectiveController extends EvalControllerSupport {

    @Data
    public static class ObjectiveRow {
        private final Long id;
        private final String title;
        private final String description;
        private final boolean odd;
    }

    @Data
    public static class DirectItemRow {
        private final Long id;
        private final String itemText;
        private final String scaleText;
        private final String expertDescription;
        private final boolean odd;
    }

    @GetMapping
    public String show(@RequestParam Long templateId,
                       @RequestParam Long categoryId,
                       Model model) {
        String currentUserId = currentUserId();
        EvalItemGroup category = authoringService.getItemGroupById(categoryId);

        // objectives under this category (non-empty only)
        List<EvalItemGroup> objectives = authoringService.getItemGroups(categoryId, currentUserId, false, true);
        List<ObjectiveRow> objectiveRows = new ArrayList<>();
        for (int i = 0; i < objectives.size(); i++) {
            EvalItemGroup obj = objectives.get(i);
            objectiveRows.add(new ObjectiveRow(obj.getId(), obj.getTitle(), obj.getDescription(), i % 2 != 0));
        }

        // direct items at category level
        List<EvalItem> directItems = authoringService.getItemsInItemGroup(categoryId, true);
        List<DirectItemRow> directItemRows = new ArrayList<>();
        for (int i = 0; i < directItems.size(); i++) {
            EvalItem item = directItems.get(i);
            String scaleText = item.getScale() != null ? ScaledUtils.makeScaleText(item.getScale(), 0) : "";
            directItemRows.add(new DirectItemRow(item.getId(), item.getItemText(), scaleText,
                    item.getExpertDescription(), i % 2 != 0));
        }

        model.addAttribute("templateId", templateId);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categoryTitle", category.getTitle());
        model.addAttribute("objectives", objectiveRows);
        model.addAttribute("directItems", directItemRows);
        model.addAttribute("hasObjectives", !objectiveRows.isEmpty());
        model.addAttribute("hasDirectItems", !directItemRows.isEmpty());
        model.addAttribute("showOr", !objectiveRows.isEmpty() && !directItemRows.isEmpty());
        return "choose_expert_objective";
    }
}