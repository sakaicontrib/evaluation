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

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.HierarchyNodeRule;
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
@RequestMapping("/modify_hierarchy_node_rules")
public class ModifyHierarchyNodeRulesController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic")
    private ExternalHierarchyLogic hierLogic;

    @Data
    public static class RuleRow {
        private final Long ruleId;
        private final String option;
        private final String qualifier;
        private final String ruleText;
    }

    @GetMapping
    public String show(@RequestParam String nodeId,
                       @RequestParam(value = "expanded", required = false) List<String> expanded,
                       Model model) {
        checkAdmin();
        EvalHierarchyNode node = hierLogic.getNodeById(nodeId);
        List<HierarchyNodeRule> rawRules;
        try { rawRules = hierLogic.getRulesByNodeID(Long.parseLong(nodeId)); }
        catch (Exception e) { rawRules = Collections.emptyList(); }

        List<RuleRow> rules = new java.util.ArrayList<>();
        for (HierarchyNodeRule r : rawRules) {
            rules.add(new RuleRow(
                    r.getId(),
                    r.getOption(),
                    hierLogic.determineQualifierFromRuleText(r.getRule()),
                    hierLogic.removeQualifierFromRuleText(r.getRule())
            ));
        }

        model.addAttribute("nodeId",         nodeId);
        model.addAttribute("nodeTitle",      node.title);
        model.addAttribute("nodeDesc",       node.description);
        model.addAttribute("rules",          rules);
        model.addAttribute("optionValues",   EvalToolConstants.HIERARCHY_RULE_OPTION_VALUES);
        model.addAttribute("optionLabels",   EvalToolConstants.HIERARCHY_RULE_OPTION_LABELS);
        model.addAttribute("qualifierValues",EvalToolConstants.HIERARCHY_RULE_QUALIFIER_VALUES);
        model.addAttribute("qualifierLabels",EvalToolConstants.HIERARCHY_RULE_QUALIFIER_LABELS);
        model.addAttribute("expanded",       expanded);
        return "modify_hierarchy_node_rules";
    }

    @PostMapping("/add")
    public String addRule(@RequestParam String nodeId,
                          @RequestParam(defaultValue = "") String ruleText,
                          @RequestParam(defaultValue = "") String qualifier,
                          @RequestParam(defaultValue = "") String option,
                          @RequestParam(value = "expanded", required = false) List<String> expanded,
                          RedirectAttributes ra) {
        checkAdmin();
        if (ruleText.trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "modifynoderules.error.no.rule.text");
            return buildReturnUrl(nodeId, expanded);
        }
        try {
            hierLogic.assignNodeRule(ruleText.trim(), qualifier, option, Long.parseLong(nodeId));
            log.info("Admin ({}) added rule to node {}", commonLogic.getCurrentUserId(), nodeId);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "modifynoderules.error.rule.exists.text");
        }
        return buildReturnUrl(nodeId, expanded);
    }

    @PostMapping("/update")
    public String updateRule(@RequestParam String nodeId,
                             @RequestParam Long ruleId,
                             @RequestParam(defaultValue = "") String ruleText,
                             @RequestParam(defaultValue = "") String qualifier,
                             @RequestParam(defaultValue = "") String option,
                             @RequestParam(value = "expanded", required = false) List<String> expanded) {
        checkAdmin();
        hierLogic.updateNodeRule(ruleId, ruleText.trim(), qualifier, option, Long.parseLong(nodeId));
        log.info("Admin ({}) updated rule {} on node {}", commonLogic.getCurrentUserId(), ruleId, nodeId);
        return buildReturnUrl(nodeId, expanded);
    }

    @PostMapping("/remove")
    public String removeRule(@RequestParam String nodeId,
                             @RequestParam Long ruleId,
                             @RequestParam(value = "expanded", required = false) List<String> expanded) {
        checkAdmin();
        hierLogic.removeNodeRule(ruleId);
        log.info("Admin ({}) removed rule {} from node {}", commonLogic.getCurrentUserId(), ruleId, nodeId);
        return buildReturnUrl(nodeId, expanded);
    }

    private String buildReturnUrl(String nodeId, List<String> expanded) {
        String base = "redirect:/modify_hierarchy_node_rules?nodeId=" + nodeId;
        if (expanded != null && !expanded.isEmpty()) base += "&expanded=" + String.join("&expanded=", expanded);
        return base;
    }

    private void checkAdmin() {
        if (!commonLogic.isUserAdmin(commonLogic.getCurrentUserId()))
            throw new SecurityException("Non-admin users may not access this locator");
    }
}