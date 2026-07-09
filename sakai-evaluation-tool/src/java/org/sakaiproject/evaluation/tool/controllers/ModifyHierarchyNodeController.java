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

import java.util.List;

import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/modify_hierarchy_node")
public class ModifyHierarchyNodeController extends EvalControllerSupport {

    @GetMapping
    public String show(@RequestParam String nodeId,
                       @RequestParam(required = false, defaultValue = "false") boolean addingChild,
                       @RequestParam(value = "expanded", required = false) List<String> expanded,
                       Model model) {
        checkAdmin();
        EvalHierarchyNode node = hierarchyLogic.getNodeById(nodeId);
        model.addAttribute("nodeId",      nodeId);
        model.addAttribute("addingChild", addingChild);
        model.addAttribute("nodeTitle",   node.title != null ? node.title : "");
        model.addAttribute("expanded",    expanded);
        if (addingChild) {
            model.addAttribute("locationMsg", new Object[]{ node.title });
        } else {
            model.addAttribute("nodeCurrentTitle", node.title != null ? node.title : "");
            model.addAttribute("nodeDescription",  node.description != null ? node.description : "");
            model.addAttribute("locationMsg", new Object[]{ node.title });
        }
        return "modify_hierarchy_node";
    }

    @PostMapping("/save")
    public String save(@RequestParam String nodeId,
                       @RequestParam(required = false, defaultValue = "false") boolean addingChild,
                       @RequestParam(defaultValue = "") String title,
                       @RequestParam(defaultValue = "") String description,
                       @RequestParam(value = "expanded", required = false) List<String> expanded) {
        checkAdmin();
        String currentUserId = currentUserId();
        if (addingChild) {
            EvalHierarchyNode newNode = hierarchyLogic.addNode(nodeId);
            hierarchyLogic.updateNodeData(newNode.id, title.trim(), description.trim());
            log.info("Admin ({}) added child node under {} with title '{}'", currentUserId, nodeId, title);
        } else {
            hierarchyLogic.updateNodeData(nodeId, title.trim(), description.trim());
            log.info("Admin ({}) updated hierarchy node {} with title '{}'", currentUserId, nodeId, title);
        }
        return buildReturnUrl(expanded);
    }

    private String buildReturnUrl(List<String> expanded) {
        if (expanded == null || expanded.isEmpty()) return "redirect:/control_hierarchy";
        return "redirect:/control_hierarchy?expanded=" + String.join("&expanded=", expanded);
    }

    private void checkAdmin() {
        if (!isCurrentUserAdmin())
            throw new SecurityException("Non-admin users may not access this page");
    }
}