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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.providers.EvalGroupsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin_test_evalgroup_provider")
public class AdminTestEGProviderController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Autowired
    private ApplicationContext applicationContext;

    @Data
    public static class TestResult {
        private final String method;
        private final String result;
        private final long millis;
    }

    @GetMapping
    public String show(
            @RequestParam(required = false, defaultValue = "") String username,
            @RequestParam(required = false, defaultValue = "") String evalGroupId,
            Model model) {

        String currentUserId = commonLogic.getCurrentUserId();
        if (!commonLogic.isUserAdmin(currentUserId))
            throw new SecurityException("Non-admin users may not access this page");

        // Resolve provider
        String providerBeanName = EvalGroupsProvider.class.getName();
        if (!applicationContext.containsBean(providerBeanName)) {
            model.addAttribute("warningMessage", "No EvalGroupsProvider found... cannot test...");
            return "admin_test_evalgroup_provider";
        }
        EvalGroupsProvider provider = (EvalGroupsProvider) applicationContext.getBean(providerBeanName);

        // Resolve username / userId
        StringBuilder warnings = new StringBuilder();
        String resolvedUsername = username.trim();
        String resolvedGroupId = evalGroupId.trim();

        if (resolvedGroupId.isEmpty()) {
            warnings.append("No evalGroupId set.  ");
            resolvedGroupId = null;
        } else {
            String title = commonLogic.getDisplayTitle(resolvedGroupId);
            if ("--------".equals(title)) {
                warnings.append("Invalid evalGroupId (").append(resolvedGroupId).append("): cannot find title, reset to null.  ");
                resolvedGroupId = null;
            }
        }

        String userId;
        if (resolvedUsername.isEmpty()) {
            resolvedUsername = commonLogic.getUserUsername(currentUserId);
            userId = currentUserId;
            warnings.append("No username set: using current user.");
        } else {
            userId = commonLogic.getUserId(resolvedUsername);
            if (userId == null) {
                warnings.append("Invalid username (").append(resolvedUsername).append("): cannot find id, setting to current user id.");
                userId = currentUserId;
                resolvedUsername = commonLogic.getUserUsername(currentUserId);
            }
        }

        if (warnings.length() > 0) model.addAttribute("warningMessage", warnings.toString().trim());
        model.addAttribute("currentUsername", resolvedUsername);
        model.addAttribute("currentGroupId", resolvedGroupId != null ? resolvedGroupId : "");

        // Run tests
        List<TestResult> results = new ArrayList<>();
        String finalGroupId = resolvedGroupId;
        String finalUserId = userId;

        runGroupTest(results, provider, "getUserIdsForEvalGroups.PERM_BE_EVALUATED", finalGroupId,
                () -> collectionToString(provider.getUserIdsForEvalGroups(new String[]{finalGroupId}, EvalGroupsProvider.PERM_BE_EVALUATED)));
        runGroupTest(results, provider, "getUserIdsForEvalGroups.PERM_TA_ROLE", finalGroupId,
                () -> collectionToString(provider.getUserIdsForEvalGroups(new String[]{finalGroupId}, EvalGroupsProvider.PERM_TA_ROLE)));
        runGroupTest(results, provider, "getUserIdsForEvalGroups.PERM_TAKE_EVALUATION", finalGroupId,
                () -> collectionToString(provider.getUserIdsForEvalGroups(new String[]{finalGroupId}, EvalGroupsProvider.PERM_TAKE_EVALUATION)));

        runGroupTest(results, provider, "countUserIdsForEvalGroups.PERM_BE_EVALUATED", finalGroupId,
                () -> String.valueOf(provider.countUserIdsForEvalGroups(new String[]{finalGroupId}, EvalGroupsProvider.PERM_BE_EVALUATED)));
        runGroupTest(results, provider, "countUserIdsForEvalGroups.PERM_TA_ROLE", finalGroupId,
                () -> String.valueOf(provider.countUserIdsForEvalGroups(new String[]{finalGroupId}, EvalGroupsProvider.PERM_TA_ROLE)));
        runGroupTest(results, provider, "countUserIdsForEvalGroups.PERM_TAKE_EVALUATION", finalGroupId,
                () -> String.valueOf(provider.countUserIdsForEvalGroups(new String[]{finalGroupId}, EvalGroupsProvider.PERM_TAKE_EVALUATION)));

        runUserTest(results, "getEvalGroupsForUser.PERM_BE_EVALUATED",
                () -> collectionToString(provider.getEvalGroupsForUser(finalUserId, EvalGroupsProvider.PERM_BE_EVALUATED)));
        runUserTest(results, "getEvalGroupsForUser.PERM_TA_ROLE",
                () -> collectionToString(provider.getEvalGroupsForUser(finalUserId, EvalGroupsProvider.PERM_TA_ROLE)));
        runUserTest(results, "getEvalGroupsForUser.PERM_TAKE_EVALUATION",
                () -> collectionToString(provider.getEvalGroupsForUser(finalUserId, EvalGroupsProvider.PERM_TAKE_EVALUATION)));

        runUserTest(results, "countEvalGroupsForUser.PERM_BE_EVALUATED",
                () -> String.valueOf(provider.countEvalGroupsForUser(finalUserId, EvalGroupsProvider.PERM_BE_EVALUATED)));
        runUserTest(results, "countEvalGroupsForUser.PERM_TA_ROLE",
                () -> String.valueOf(provider.countEvalGroupsForUser(finalUserId, EvalGroupsProvider.PERM_TA_ROLE)));
        runUserTest(results, "countEvalGroupsForUser.PERM_TAKE_EVALUATION",
                () -> String.valueOf(provider.countEvalGroupsForUser(finalUserId, EvalGroupsProvider.PERM_TAKE_EVALUATION)));

        runGroupTest(results, provider, "getGroupByGroupId", finalGroupId,
                () -> provider.getGroupByGroupId(finalGroupId).toString());
        runGroupTest(results, provider, "isUserAllowedInGroup.PERM_BE_EVALUATED", finalGroupId,
                () -> String.valueOf(provider.isUserAllowedInGroup(finalUserId, EvalGroupsProvider.PERM_BE_EVALUATED, finalGroupId)));
        runGroupTest(results, provider, "isUserAllowedInGroup.PERM_TA_ROLE", finalGroupId,
                () -> String.valueOf(provider.isUserAllowedInGroup(finalUserId, EvalGroupsProvider.PERM_TA_ROLE, finalGroupId)));
        runGroupTest(results, provider, "isUserAllowedInGroup.PERM_TAKE_EVALUATION", finalGroupId,
                () -> String.valueOf(provider.isUserAllowedInGroup(finalUserId, EvalGroupsProvider.PERM_TAKE_EVALUATION, finalGroupId)));

        model.addAttribute("testResults", results);
        return "admin_test_evalgroup_provider";
    }

    private interface TestSupplier {
        String get();
    }

    private void runGroupTest(List<TestResult> results, EvalGroupsProvider provider, String method, String groupId, TestSupplier call) {
        if (groupId == null) {
            results.add(new TestResult(method, "SKIPPED: no evalGroupId set", 0));
            return;
        }
        long start = new Date().getTime();
        String result;
        try { result = call.get(); } catch (Exception e) { result = "ERROR: " + e.getMessage(); }
        results.add(new TestResult(method, result, new Date().getTime() - start));
    }

    private void runUserTest(List<TestResult> results, String method, TestSupplier call) {
        long start = new Date().getTime();
        String result;
        try { result = call.get(); } catch (Exception e) { result = "ERROR: " + e.getMessage(); }
        results.add(new TestResult(method, result, new Date().getTime() - start));
    }

    private String collectionToString(Collection<?> c) {
        StringBuilder sb = new StringBuilder();
        sb.append("(#:").append(c.size()).append(")");
        for (Iterator<?> iter = c.iterator(); iter.hasNext(); ) {
            sb.append(", ").append(iter.next().toString());
        }
        return sb.toString();
    }
}