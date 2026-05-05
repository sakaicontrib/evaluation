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
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of ControlExpertItemsProducer.
 */
@Slf4j
@Controller
@RequestMapping("/control_expert_items")
public class ControlExpertItemsController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @Data
    public static class GroupRow {
        private final Long id;
        private final String type;
        private final String catTitle;  // null if objective
        private final String objTitle;  // null if category
        private final String description;
        private final int itemCount;
        private final String ownerName;
        private final boolean odd;
        private final String editUrl;
        private final String previewUrl;       // null if no items
        private final String addObjectiveUrl;  // null if not applicable
        private final String deleteUrl;        // null if not deletable
    }

    @GetMapping
    public String show(Model model) {
        String currentUserId = commonLogic.getCurrentUserId();
        List<EvalItemGroup> groups = authoringService.getAllItemGroups(currentUserId, true);

        List<GroupRow> rows = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            EvalItemGroup g = groups.get(i);
            boolean isCategory = EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(g.getType());
            Long catId = isCategory ? g.getId() : null;
            Long objId = isCategory ? null : g.getId();

            int itemCount = g.getGroupItems() != null ? g.getGroupItems().size() : 0;
            String ownerName = commonLogic.getEvalUserById(g.getOwner()).displayName;

            String editUrl = "/modify_expert_item?type=" + g.getType()
                    + (isCategory ? "&categoryId=" + g.getId() : "&objectiveId=" + g.getId())
                    + "&isNew=false";
            String previewUrl = itemCount > 0
                    ? "/preview_expert_items?type=" + g.getType()
                      + (isCategory ? "&categoryId=" + g.getId() : "&objectiveId=" + g.getId())
                    : null;
            String addObjectiveUrl = null;
            String deleteUrl = null;

            if (isCategory) {
                if (itemCount == 0) {
                    addObjectiveUrl = "/modify_expert_item?type=" + EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE
                            + "&categoryId=" + g.getId() + "&isNew=true";
                    List<EvalItemGroup> objectives = authoringService.getItemGroups(g.getId(), currentUserId, true, true);
                    if (objectives.isEmpty()) {
                        deleteUrl = "/remove_expert_item?type=" + g.getType() + "&categoryId=" + g.getId();
                    }
                }
            } else {
                if (itemCount == 0) {
                    deleteUrl = "/remove_expert_item?type=" + g.getType() + "&objectiveId=" + g.getId();
                }
            }

            rows.add(new GroupRow(g.getId(), g.getType(),
                    isCategory ? g.getTitle() : null,
                    isCategory ? null : g.getTitle(),
                    g.getDescription(), itemCount, ownerName, i % 2 != 0,
                    editUrl, previewUrl, addObjectiveUrl, deleteUrl));
        }

        model.addAttribute("rows", rows);
        model.addAttribute("hasRows", !rows.isEmpty());
        return "control_expert_items";
    }
}