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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.TemplateItemGroup;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
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
@RequestMapping("/evaluation_assignments")
public class EvaluationAssignmentsController extends EvalControllerSupport {
    @Data
    public static class GroupRow {
        private final String title;
        private final String type;
        private final String directLink;
        private final int enrollmentCount;
        private final int enrollmentInstructors;
        private final int enrollmentAssistants;
        private final boolean published;
    }

    @Data
    public static class HierNodeRow {
        private final String title;
        private final String description;
        private final List<GroupRow> groups;
    }

    @GetMapping
    public String show(@RequestParam Long evaluationId, Model model, HttpServletRequest request) {
        if (evaluationId == null)
            throw new IllegalArgumentException("evaluationId is required");

        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("evalTitle", eval.getTitle());

        // Link to modify assignments (only if state <= ACTIVE)
        String evalState = evaluationService.updateEvaluationState(evaluationId);
        boolean canModify = EvalUtils.checkStateBefore(evalState, EvalConstants.EVALUATION_STATE_ACTIVE, true);
        model.addAttribute("canModify", canModify);
        if (canModify) {
            model.addAttribute("modifyDate", df.format(eval.getStartDate()));
        }

        // Check template categories (inlined from RenderingUtils.extractCategoriesInTemplate)
        List<String> categories = extractCategoriesInTemplate(eval.getTemplate().getId());
        boolean hasInstructors = categories.contains(EvalConstants.ITEM_CATEGORY_INSTRUCTOR);
        boolean hasAssistants = categories.contains(EvalConstants.ITEM_CATEGORY_ASSISTANT);
        model.addAttribute("hasInstructors", hasInstructors);
        model.addAttribute("hasAssistants", hasAssistants);

        // Load assigned groups
        Map<Long, List<EvalAssignGroup>> groupsMap = evaluationService.getAssignGroupsForEvals(
                new Long[]{ evaluationId }, true, null);
        List<EvalAssignGroup> assignGroups = groupsMap.getOrDefault(evaluationId, new ArrayList<>());

        // Count evaluators per group
        List<EvalAssignUser> evaluators = evaluationService.getParticipantsForEval(
                evaluationId, null, null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Map<String, Integer> evaluatorCount = new HashMap<>();
        for (EvalAssignUser u : evaluators) {
            evaluatorCount.merge(u.getEvalGroupId(), 1, Integer::sum);
        }

        // Sort groups alphabetically
        Collections.sort(assignGroups, (a, b) -> {
            EvalGroup g1 = commonLogic.makeEvalGroupObject(a.getEvalGroupId());
            EvalGroup g2 = commonLogic.makeEvalGroupObject(b.getEvalGroupId());
            return g1.title.compareToIgnoreCase(g2.title);
        });

        int unpublishedCount = 0;
        List<GroupRow> groupRows = new ArrayList<>();
        for (EvalAssignGroup ag : assignGroups) {
            if (ag.getNodeId() != null) continue; // skip hierarchy-assigned groups
            String gid = ag.getEvalGroupId();
            EvalGroup group = commonLogic.makeEvalGroupObject(gid);
            boolean published = commonLogic.isEvalGroupPublished(gid);
            if (!published) unpublishedCount++;

            String directLink = request.getContextPath() + "/preview_eval?evaluationId=" + evaluationId
                    + "&evalGroupId=" + URLEncoder.encode(gid, StandardCharsets.UTF_8);

            int instructorCount = 0, assistantCount = 0;
            if (hasInstructors) {
                List<EvalAssignUser> inst = evaluationService.getParticipantsForEval(
                        evaluationId, null, new String[]{ gid }, EvalAssignUser.TYPE_EVALUATEE,
                        EvalAssignUser.STATUS_LINKED, null, null);
                instructorCount = inst != null ? inst.size() : 0;
            }
            if (hasAssistants) {
                List<EvalAssignUser> asst = evaluationService.getParticipantsForEval(
                        evaluationId, null, new String[]{ gid }, EvalAssignUser.TYPE_ASSISTANT,
                        EvalAssignUser.STATUS_LINKED, null, null);
                assistantCount = asst != null ? asst.size() : 0;
            }

            groupRows.add(new GroupRow(
                    group.title, group.type, directLink,
                    evaluatorCount.getOrDefault(gid, 0),
                    instructorCount, assistantCount, published
            ));
        }
        model.addAttribute("groupRows", groupRows);
        model.addAttribute("unpublishedCount", unpublishedCount);

        // Hierarchy section
        boolean showHierarchy = Boolean.TRUE.equals(settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS));
        model.addAttribute("showHierarchy", showHierarchy);

        if (showHierarchy) {
            List<EvalAssignHierarchy> assignHierarchies = evaluationService.getAssignHierarchyByEval(evaluationId);
            List<HierNodeRow> hierRows = new ArrayList<>();
            List<String> populated = new ArrayList<>();
            for (EvalAssignHierarchy ah : assignHierarchies) {
                if (populated.contains(ah.getNodeId())) continue;
                EvalHierarchyNode node = hierarchyLogic.getNodeById(ah.getNodeId());
                List<GroupRow> nodeGroups = new ArrayList<>();
                for (EvalAssignGroup ag : assignGroups) {
                    if (ah.getNodeId().equals(ag.getNodeId())) {
                        String gid = ag.getEvalGroupId();
                        EvalGroup g = commonLogic.makeEvalGroupObject(gid);
                        String dl = request.getContextPath() + "/preview_eval?evaluationId=" + evaluationId
                                + "&evalGroupId=" + URLEncoder.encode(gid, StandardCharsets.UTF_8);
                        nodeGroups.add(new GroupRow(g.title, g.type, dl,
                                evaluatorCount.getOrDefault(gid, 0), 0, 0, true));
                    }
                }
                hierRows.add(new HierNodeRow(node.title, node.description, nodeGroups));
                populated.add(ah.getNodeId());
            }
            model.addAttribute("hierRows", hierRows);
        }

        return "evaluation_assignments";
    }

    private List<String> extractCategoriesInTemplate(long templateId) {
        List<String> categories = new ArrayList<>();
        List<EvalTemplateItem> items = authoringService.getTemplateItemsForTemplate(
                templateId, new String[]{}, new String[]{}, new String[]{});
        Map<String, List<String>> associates = new HashMap<>();
        associates.put(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, List.of("fakeinstructor"));
        associates.put(EvalConstants.ITEM_CATEGORY_ASSISTANT, List.of("fakeAssistant"));
        TemplateItemDataList tidl = new TemplateItemDataList(items, null, associates, null);
        for (TemplateItemGroup tig : tidl.getTemplateItemGroups()) {
            if (EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType)) {
                categories.add(EvalConstants.ITEM_CATEGORY_COURSE);
            } else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(tig.associateType)) {
                categories.add(EvalConstants.ITEM_CATEGORY_INSTRUCTOR);
            } else if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(tig.associateType)) {
                categories.add(EvalConstants.ITEM_CATEGORY_ASSISTANT);
            }
        }
        return categories;
    }
}