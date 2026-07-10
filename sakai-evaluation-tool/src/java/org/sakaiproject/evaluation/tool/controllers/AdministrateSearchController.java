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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/administrate_search")
public class AdministrateSearchController extends EvalControllerSupport {

    public static final int PAGE_SIZE = 20;
    public static final int MAX_GROUP_SIZE = 200;

    @Data
    public static class EvalRow {
        private final Long evalId;
        private final Long templateId;
        private final String title;
        private final String groupText;
        private final int groupCount;
        private final String startDate;
        private final String dueDate;
        private final String owner;
    }

    @GetMapping
    public String show(
            @RequestParam(required = false, defaultValue = "") String searchString,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false) String searchByGroup,
            @RequestParam(required = false, defaultValue = "false") boolean searchGroups,
            Model model) {

        if (!isCurrentUserAdmin())
            throw new SecurityException("Non-admin users may not access this page");

        // The "searchByGroup" button present in the request indicates a group search
        if (searchByGroup != null) searchGroups = true;

        model.addAttribute("searchString", searchString);
        model.addAttribute("page", page);
        model.addAttribute("searchGroups", searchGroups);

        if (!searchString.trim().isEmpty()) {
            doSearch(searchString.trim(), page, searchGroups, model);
        }

        return "administrate_search";
    }

    private void doSearch(String searchString, int page, boolean searchGroups, Model model) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        int startResult = page * PAGE_SIZE;
        List<EvalEvaluation> evals;
        int count;
        boolean tooManyGroups = false;

        if (searchGroups) {
            List<String> groupIds = commonLogic.searchForEvalGroupIds(searchString, "title", 0, MAX_GROUP_SIZE);
            tooManyGroups = groupIds.size() >= MAX_GROUP_SIZE;
            evals = evaluationService.getEvaluationsForEvalGroups(groupIds.toArray(new String[0]), 0, 0);
            count = evals.size();
        } else {
            count = evaluationService.countEvaluations(searchString);
            evals = count > 0
                    ? evaluationService.getEvaluations(searchString, "title", startResult, PAGE_SIZE)
                    : new ArrayList<>();
        }

        if (count == 0) {
            model.addAttribute("noResults", true);
            return;
        }

        // Pagination
        if (!searchGroups && count > PAGE_SIZE) {
            int actualStart = startResult + 1;
            int actualEnd = startResult + evals.size();
            model.addAttribute("pagerMsg", new Object[]{ actualStart, actualEnd, count });
            model.addAttribute("hasPrev", page > 0);
            model.addAttribute("hasNext", count > startResult + PAGE_SIZE);
            model.addAttribute("prevPage", page - 1);
            model.addAttribute("nextPage", page + 1);
        }
        model.addAttribute("tooManyGroups", tooManyGroups);
        model.addAttribute("tooManyGroupsCount", evals.size());

        // Resolve owners in one batch
        List<String> ownerIds = evals.stream().map(EvalEvaluation::getOwner).distinct().collect(Collectors.toList());
        Map<String, EvalUser> ownerMap = new HashMap<>();
        for (EvalUser u : commonLogic.getEvalUsersByIds(ownerIds)) ownerMap.put(u.userId, u);

        // Resolve groups in one batch
        Long[] evalIds = evals.stream().map(EvalEvaluation::getId).toArray(Long[]::new);
        Map<Long, List<EvalAssignGroup>> groupsMap = evaluationService.getAssignGroupsForEvals(evalIds, false, false);

        List<EvalRow> rows = new ArrayList<>();
        for (EvalEvaluation eval : evals) {
            List<EvalAssignGroup> groups = groupsMap.getOrDefault(eval.getId(), new ArrayList<>());
            int groupCount = groups.size();
            String groupText = groupCount == 1
                    ? commonLogic.getDisplayTitle(groups.get(0).getEvalGroupId())
                    : null; // null → use i18n key with count in template

            EvalUser owner = ownerMap.get(eval.getOwner());
            String ownerStr = "-------";
            if (owner != null) {
                ownerStr = (owner.username != null && owner.sortName != null && !owner.username.equals(owner.sortName))
                        ? owner.sortName + " (" + owner.username + ")"
                        : (owner.username != null ? owner.username : owner.userId);
            }

            rows.add(new EvalRow(
                    eval.getId(),
                    eval.getTemplate() != null ? eval.getTemplate().getId() : null,
                    EvalUtils.makeMaxLengthString(eval.getTitle(), 70),
                    groupText,
                    groupCount,
                    eval.getStartDate() != null ? df.format(eval.getStartDate()) : "",
                    eval.getDueDate() != null ? df.format(eval.getDueDate()) : "",
                    ownerStr
            ));
        }
        model.addAttribute("evalRows", rows);
    }
}