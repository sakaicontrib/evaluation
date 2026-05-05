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
import java.util.Set;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.ReportingPermissions;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of ReportChooseGroupsProducer.
 * GET form that submits to /report_view with the selected groupIds.
 */
@Slf4j
@Controller
@RequestMapping("/report_groups")
public class ReportGroupsController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationService")
    private EvalEvaluationService evaluationService;

    @Resource(name = "org.sakaiproject.evaluation.logic.ReportingPermissions")
    private ReportingPermissions reportingPermissions;

    @Data
    public static class GroupOption {
        private final String id;
        private final String title;
    }

    @GetMapping
    public String show(@RequestParam Long evaluationId, Model model) {

        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("notAllowed", false);
        model.addAttribute("groups", new ArrayList<>());

        EvalEvaluation evaluation = evaluationService.getEvaluationById(evaluationId);
        Set<String> viewableGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(evaluation);

        if (viewableGroupIds.isEmpty()) {
            model.addAttribute("notAllowed", true);
            return "report_groups";
        }

        List<GroupOption> groups = new ArrayList<>();
        for (String groupId : viewableGroupIds) {
            String title = commonLogic.makeEvalGroupObject(groupId).title;
            groups.add(new GroupOption(groupId, title));
        }
        groups.sort((a, b) -> a.getTitle().compareTo(b.getTitle()));

        model.addAttribute("groups", groups);
        return "report_groups";
    }
}