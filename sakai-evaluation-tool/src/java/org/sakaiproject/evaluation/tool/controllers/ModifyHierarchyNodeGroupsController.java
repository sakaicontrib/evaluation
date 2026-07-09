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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/modify_hierarchy_node_groups")
public class ModifyHierarchyNodeGroupsController extends EvalControllerSupport {

    @Data
    public static class GroupRow {
        private final String evalGroupId;
        private final String title;
        private final String sections;
        private final boolean selected;
    }

    @GetMapping
    public String show(@RequestParam String nodeId,
                       @RequestParam(value = "expanded", required = false) List<String> expanded,
                       Model model) {
        checkAdmin();
        EvalHierarchyNode node = hierarchyLogic.getNodeById(nodeId);
        Set<String> assignedGroupIds = hierarchyLogic.getEvalGroupsForNode(nodeId);

        List<EvalGroup> allGroups = commonLogic.getEvalGroupsForUser(
                commonLogic.getAdminUserId(), EvalConstants.PERM_BE_EVALUATED);
        // Include groups assigned via hierarchy that might not be in allGroups
        for (String hierarchyGid : assignedGroupIds) {
            try {
                EvalGroup g = commonLogic.makeEvalGroupObject(
                        EvalConstants.GROUP_ID_SITE_PREFIX + hierarchyGid.substring(EvalConstants.GROUP_ID_SITE_PREFIX.length()));
                if (g != null && allGroups.stream().noneMatch(eg -> eg.title.equals(g.title))) {
                    allGroups.add(g);
                }
            } catch (Exception ignored) {}
        }
        Collections.sort(allGroups, (a, b) -> a.title.compareTo(b.title));

        List<GroupRow> rows = new ArrayList<>();
        for (EvalGroup g : allGroups) {
            List<Section> sections = hierarchyLogic.getSectionsUnderEvalGroup(g.evalGroupId);
            String sectionStr = sections.stream().map(Section::getTitle).reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
            rows.add(new GroupRow(g.evalGroupId, g.title, sectionStr, assignedGroupIds.contains(g.evalGroupId)));
        }

        model.addAttribute("nodeId",    nodeId);
        model.addAttribute("nodeTitle", node.title);
        model.addAttribute("groupRows", rows);
        model.addAttribute("expanded",  expanded);
        return "modify_hierarchy_node_groups";
    }

    @PostMapping("/save")
    public String save(@RequestParam String nodeId,
                       @RequestParam(value = "selectedGroups", required = false) List<String> selectedGroups,
                       @RequestParam(value = "expanded", required = false) List<String> expanded) {
        checkAdmin();
        Set<String> toAssign = selectedGroups != null ? new HashSet<>(selectedGroups) : new HashSet<>();
        hierarchyLogic.setEvalGroupsForNode(nodeId, toAssign);
        log.info("Admin ({}) set {} groups for hierarchy node {}", currentUserId(), toAssign.size(), nodeId);
        return buildReturnUrl(expanded);
    }

    private String buildReturnUrl(List<String> expanded) {
        if (expanded == null || expanded.isEmpty()) return "redirect:/control_hierarchy";
        return "redirect:/control_hierarchy?expanded=" + String.join("&expanded=", expanded);
    }

    private void checkAdmin() {
        if (!isCurrentUserAdmin())
            throw new SecurityException("Non-admin users may not access this locator");
    }
}