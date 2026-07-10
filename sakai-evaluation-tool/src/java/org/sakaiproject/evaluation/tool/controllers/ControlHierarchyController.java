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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
@RequestMapping("/control_hierarchy")
public class ControlHierarchyController extends EvalControllerSupport {

    @Data
    public static class NodeRow {
        private final String nodeId;
        private final String title;
        private final int level;
        private final boolean expanded;
        private final int childCount;
        private final int groupCount;
        private final int userCount;
        private final int ruleCount;
        private final boolean canAddChild;
        private final boolean canRemove;
        private final List<String> expandWith;    // expanded list if this node is toggled open
        private final List<String> collapseWith;  // expanded list if this node is toggled closed
    }

    @GetMapping
    public String show(@RequestParam(value = "expanded", required = false) List<String> expanded, Model model) {
        checkAdmin();

        Set<String> expandedSet = expanded != null ? new HashSet<>(expanded) : new HashSet<>();

        EvalHierarchyNode root = hierarchyLogic.getRootLevelNode();
        Set<EvalHierarchyNode> allDescendants = hierarchyLogic.getChildNodes(root.id, false);

        // Build nodeId -> node map for fast lookup
        Map<String, EvalHierarchyNode> nodeMap = new HashMap<>();
        nodeMap.put(root.id, root);
        for (EvalHierarchyNode n : allDescendants) nodeMap.put(n.id, n);

        // Batch-fetch groups and users for all nodes
        String[] allIds = nodeMap.keySet().toArray(new String[0]);
        Map<String, Set<String>> groupsMap = hierarchyLogic.getEvalGroupsForNodes(allIds);
        Map<String, Map<String, Set<String>>> usersMap = hierarchyLogic.getUsersAndPermsForNodes(allIds);

        // Build flat list via depth-first traversal
        List<NodeRow> rows = new ArrayList<>();
        buildRows(rows, root, 0, expandedSet, groupsMap, usersMap, nodeMap);
        model.addAttribute("rows", rows);
        return "control_hierarchy";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam String nodeId,
                         @RequestParam(value = "expanded", required = false) List<String> expanded) {
        checkAdmin();
        hierarchyLogic.removeNode(nodeId);
        log.info("Admin ({}) removed hierarchy node {}", currentUserId(), nodeId);
        if (expanded != null && !expanded.isEmpty()) {
            return "redirect:/control_hierarchy?expanded=" + String.join("&expanded=", expanded);
        }
        return "redirect:/control_hierarchy";
    }

    private void buildRows(List<NodeRow> rows, EvalHierarchyNode node, int level,
                           Set<String> expandedSet,
                           Map<String, Set<String>> groupsMap,
                           Map<String, Map<String, Set<String>>> usersMap,
                           Map<String, EvalHierarchyNode> nodeMap) {

        int groupCount = groupsMap.getOrDefault(node.id, Collections.emptySet()).size();
        int userCount  = usersMap.getOrDefault(node.id, Collections.emptyMap()).size();
        int childCount = (int) node.directChildNodeIds.stream().filter(nodeMap::containsKey).count();

        int ruleCount = 0;
        try { ruleCount = hierarchyLogic.getRulesByNodeID(Long.parseLong(node.id)).size(); }
        catch (Exception ignored) {}

        boolean isExpanded = expandedSet.contains(node.id);

        // Toggle lists for the expand/collapse link
        List<String> expandWith = new ArrayList<>(expandedSet);
        if (!expandWith.contains(node.id)) expandWith.add(node.id);
        List<String> collapseWith = expandedSet.stream()
                .filter(id -> !id.equals(node.id)).collect(Collectors.toList());

        rows.add(new NodeRow(
                node.id,
                node.title != null ? node.title : "—",
                level,
                isExpanded,
                childCount,
                groupCount,
                userCount,
                ruleCount,
                groupCount == 0,           // canAddChild: only if no groups assigned
                childCount == 0 && groupCount == 0, // canRemove
                expandWith,
                collapseWith
        ));

        // Recurse into direct children if this node is expanded
        if (isExpanded) {
            List<EvalHierarchyNode> children = node.directChildNodeIds.stream()
                    .filter(nodeMap::containsKey)
                    .map(nodeMap::get)
                    .sorted()
                    .collect(Collectors.toList());
            for (EvalHierarchyNode child : children) {
                buildRows(rows, child, level + 1, expandedSet, groupsMap, usersMap, nodeMap);
            }
        }
    }

    private void checkAdmin() {
        if (!isCurrentUserAdmin())
            throw new SecurityException("Non-admin users may not access this page");
    }
}