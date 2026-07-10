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
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/modify_hierarchy_node_perms")
public class ModifyHierarchyNodePermsController extends EvalControllerSupport {

    @Data
    public static class UserPermRow {
        private final String userId;
        private final String displayName;
        private final String username;
        private final Set<String> perms;
        private final boolean currentUser;
    }

    @GetMapping
    public String show(@RequestParam String nodeId,
                       @RequestParam(value = "expanded", required = false) List<String> expanded,
                       Model model) {
        checkAdmin();
        EvalHierarchyNode node = hierarchyLogic.getNodeById(nodeId);
        Map<String, Set<String>> nodeUsersMap = hierarchyLogic.getUsersAndPermsForNodes(nodeId)
                .getOrDefault(nodeId, java.util.Collections.emptyMap());

        String currentUserId = currentUserId();
        List<UserPermRow> userRows = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : nodeUsersMap.entrySet()) {
            EvalUser user = commonLogic.getEvalUserById(entry.getKey());
            userRows.add(new UserPermRow(
                    user.userId,
                    user.displayName != null ? user.displayName : user.userId,
                    user.username != null ? user.username : user.userId,
                    entry.getValue(),
                    currentUserId.equals(user.userId)
            ));
        }

        model.addAttribute("nodeId",       nodeId);
        model.addAttribute("nodeTitle",    node.title);
        model.addAttribute("nodeDesc",     node.description);
        model.addAttribute("userRows",     userRows);
        model.addAttribute("permValues",   EvalToolConstants.HIERARCHY_PERM_VALUES);
        model.addAttribute("permLabels",   EvalToolConstants.HIERARCHY_PERM_LABELS);
        model.addAttribute("expanded",     expanded);
        return "modify_hierarchy_node_perms";
    }

    @PostMapping("/add")
    public String addUser(@RequestParam String nodeId,
                          @RequestParam String userEid,
                          @RequestParam(value = "perms", required = false) List<String> perms,
                          @RequestParam(value = "expanded", required = false) List<String> expanded,
                          RedirectAttributes ra) {
        checkAdmin();
        String userId = commonLogic.getUserId(userEid);
        if (userId == null) {
            ra.addFlashAttribute("errorMessage", "modifynodeperms.error.no.user.eid");
            return buildReturnUrl(nodeId, expanded);
        }
        if (perms == null || perms.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "modifynodeperms.error.no.perms.selected");
            return buildReturnUrl(nodeId, expanded);
        }
        Map<String, Set<String>> existing = hierarchyLogic.getUsersAndPermsForNodes(nodeId).getOrDefault(nodeId, java.util.Collections.emptyMap());
        if (existing.containsKey(userId)) {
            ra.addFlashAttribute("errorMessage", "modifynodeperms.error.user.has.perm");
            ra.addFlashAttribute("errorArgs", new Object[]{ userEid });
            return buildReturnUrl(nodeId, expanded);
        }
        for (String perm : perms) hierarchyLogic.assignUserNodePerm(userId, nodeId, perm, false);
        ra.addFlashAttribute("successMessage", "modifynodeperms.success.user.added");
        ra.addFlashAttribute("successArgs", new Object[]{ userEid });
        log.info("Admin ({}) added user {} with perms {} to node {}", currentUserId(), userEid, perms, nodeId);
        return buildReturnUrl(nodeId, expanded);
    }

    @PostMapping("/save")
    public String savePerms(@RequestParam String nodeId,
                            @RequestParam String userId,
                            @RequestParam(value = "perms", required = false) List<String> perms,
                            @RequestParam(value = "expanded", required = false) List<String> expanded,
                            RedirectAttributes ra) {
        checkAdmin();
        // Remove all existing perms for this user on this node
        for (String perm : EvalToolConstants.HIERARCHY_PERM_VALUES)
            hierarchyLogic.removeUserNodePerm(userId, nodeId, perm, false);
        // Re-assign selected perms
        if (perms != null) {
            for (String perm : perms) hierarchyLogic.assignUserNodePerm(userId, nodeId, perm, false);
        }
        EvalUser user = commonLogic.getEvalUserById(userId);
        ra.addFlashAttribute("successMessage", "modifynodeperms.success.perms.saved");
        ra.addFlashAttribute("successArgs", new Object[]{ user.username != null ? user.username : userId });
        log.info("Admin ({}) updated perms for user {} on node {}", currentUserId(), userId, nodeId);
        return buildReturnUrl(nodeId, expanded);
    }

    @PostMapping("/remove-user")
    public String removeUser(@RequestParam String nodeId,
                             @RequestParam String userId,
                             @RequestParam(value = "expanded", required = false) List<String> expanded,
                             RedirectAttributes ra) {
        checkAdmin();
        for (String perm : EvalToolConstants.HIERARCHY_PERM_VALUES)
            hierarchyLogic.removeUserNodePerm(userId, nodeId, perm, false);
        EvalUser user = commonLogic.getEvalUserById(userId);
        ra.addFlashAttribute("successMessage", "modifynodeperms.success.user.removed");
        ra.addFlashAttribute("successArgs", new Object[]{ user.username != null ? user.username : userId });
        log.info("Admin ({}) removed user {} from node {}", currentUserId(), userId, nodeId);
        return buildReturnUrl(nodeId, expanded);
    }

    private String buildReturnUrl(String nodeId, List<String> expanded) {
        String base = "redirect:/modify_hierarchy_node_perms?nodeId=" + nodeId;
        if (expanded != null && !expanded.isEmpty()) base += "&expanded=" + String.join("&expanded=", expanded);
        return base;
    }

    private void checkAdmin() {
        if (!isCurrentUserAdmin())
            throw new SecurityException("Non-admin users may not access this locator");
    }
}