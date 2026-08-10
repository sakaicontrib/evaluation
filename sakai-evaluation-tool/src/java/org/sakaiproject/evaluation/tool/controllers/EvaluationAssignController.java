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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.TemplateItemGroup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;

/**
 * Third step of the wizard: group assignment.
 * Spring MVC equivalent of EvaluationAssignProducer (caso useSelectionOptions=false).
 *
 * The form is a GET that sends selectedGroupIDs to /evaluation_assign_confirm.
 */
@Controller
@RequestMapping("/evaluation_assign")
public class EvaluationAssignController extends EvalControllerSupport {

    @Data
    public static class GroupRow {
        String evalGroupId;
        Long adhocGroupId; // only for adhoc groups
        String title;
        boolean hasEvaluators;
        boolean isPublished;
        boolean preSelected; // for editing (already-active eval with assigned groups)
    }

    @GetMapping
    public String show(@RequestParam Long evaluationId, Locale locale, Model model) {
        String currentUserId = currentUserId();

        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        if (eval == null) {
            throw new IllegalArgumentException("Evaluation not found: " + evaluationId);
        }

        DateFormat df  = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        DateFormat dtf = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);
        boolean isPartial = EvalConstants.EVALUATION_STATE_PARTIAL.equals(eval.getState());

        // Previously assigned groups (for evaluations being edited)
        Set<String> alreadyAssignedGroupIds = new HashSet<>();
        if (!isPartial) {
            Map<Long, List<EvalAssignGroup>> assignedMap =
                evaluationService.getAssignGroupsForEvals(new Long[]{evaluationId}, true, null);
            List<EvalAssignGroup> assigned = assignedMap.get(evaluationId);
            if (assigned != null) {
                for (EvalAssignGroup ag : assigned) {
                    alreadyAssignedGroupIds.add(ag.getEvalGroupId());
                }
            }
        }

        // Get groups where the user can assign evaluations
        String currentEvalGroupId = commonLogic.getCurrentEvalGroup();
        String currentSiteId = EntityReference.getIdFromRef(currentEvalGroupId);

        Boolean isGroupFilterEnabled = (Boolean) settings.get(EvalSettings.ENABLE_FILTER_ASSIGNABLE_GROUPS);
        List<EvalGroup> assignEvalGroups;
        List<EvalGroup> beEvalGroups;
        if (Boolean.TRUE.equals(isGroupFilterEnabled)) {
            assignEvalGroups = commonLogic.getFilteredEvalGroupsForUser(currentUserId, EvalConstants.PERM_ASSIGN_EVALUATION, currentSiteId);
            beEvalGroups     = commonLogic.getFilteredEvalGroupsForUser(currentUserId, EvalConstants.PERM_BE_EVALUATED, currentSiteId);
        } else {
            assignEvalGroups = commonLogic.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_ASSIGN_EVALUATION);
            beEvalGroups     = commonLogic.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_BE_EVALUATED);
        }

        // Merge both lists removing adhoc groups and duplicates
        List<EvalGroup> evalGroups = new ArrayList<>();
        for (EvalGroup g : assignEvalGroups) {
            if (!evalGroups.contains(g) && !EvalConstants.GROUP_TYPE_ADHOC.equals(g.type)) {
                evalGroups.add(g);
            }
        }
        for (EvalGroup g : beEvalGroups) {
            if (!evalGroups.contains(g) && !EvalConstants.GROUP_TYPE_ADHOC.equals(g.type)) {
                evalGroups.add(g);
            }
        }

        // Sort by title, placing the current group first
        Collections.sort(evalGroups, new ComparatorsUtils.GroupComparatorByTitle());
        for (int i = 0; i < evalGroups.size(); i++) {
            if (evalGroups.get(i).evalGroupId.equals(currentEvalGroupId)) {
                EvalGroup current = evalGroups.remove(i);
                evalGroups.add(0, current);
                break;
            }
        }

        // Build rows
        boolean checkEvaluators = !EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl());
        Map<String, Boolean> hasEvaluatorsMap = Collections.emptyMap();
        if (checkEvaluators) {
            List<String> groupIds = new ArrayList<>();
            for (EvalGroup g : evalGroups) {
                groupIds.add(g.evalGroupId);
            }
            // EVALSYS-1615: single bulk lookup instead of one countUserIdsForEvalGroup() call per group,
            // which does not scale to users with hundreds of assignable groups. Only the yes/no answer is
            // needed here, so hasUserIdsForEvalGroups() also stops resolving a group's users as soon as it
            // finds one real evaluator, instead of resolving every user in the group.
            hasEvaluatorsMap = commonLogic.hasUserIdsForEvalGroups(
                groupIds, EvalConstants.PERM_TAKE_EVALUATION, eval.getSectionAwareness());
        }
        List<GroupRow> groupRows = new ArrayList<>();
        for (EvalGroup g : evalGroups) {
            GroupRow row = new GroupRow();
            row.setEvalGroupId(g.evalGroupId);
            row.setTitle(g.title);
            row.setPublished(commonLogic.isEvalGroupPublished(g.evalGroupId));
            row.setPreSelected(alreadyAssignedGroupIds.contains(g.evalGroupId));
            boolean hasEvaluators = !checkEvaluators || hasEvaluatorsMap.getOrDefault(g.evalGroupId, false);
            row.setHasEvaluators(hasEvaluators);
            groupRows.add(row);
        }

        // Count unpublished groups
        long unpublishedCount = groupRows.stream().filter(r -> !r.isPublished()).count();

        // Adhoc groups (if the setting allows it)
        boolean enableAdhocGroups = Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_ADHOC_GROUPS));
        List<GroupRow> adhocGroupRows = new ArrayList<>();
        if (enableAdhocGroups) {
            List<EvalAdhocGroup> myAdhocGroups = commonLogic.getAdhocGroupsForOwner(currentUserId);
            for (EvalAdhocGroup adhocGroup : myAdhocGroups) {
                GroupRow row = new GroupRow();
                row.setEvalGroupId(adhocGroup.getEvalGroupId());
                row.setAdhocGroupId(adhocGroup.getId());
                row.setTitle(adhocGroup.getTitle());
                row.setPublished(true);
                row.setPreSelected(alreadyAssignedGroupIds.contains(adhocGroup.getEvalGroupId()));
                boolean hasEvaluators = true;
                if (!EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl())) {
                    int num = commonLogic.countUserIdsForEvalGroup(
                        adhocGroup.getEvalGroupId(), EvalConstants.PERM_TAKE_EVALUATION, eval.getSectionAwareness());
                    hasEvaluators = num > 0;
                }
                row.setHasEvaluators(hasEvaluators);
                adhocGroupRows.add(row);
            }
        }

        // Detect instructor/TA categories in the template
        boolean enableInstructorAssistantSelection = Boolean.TRUE.equals(
                settings.get(EvalSettings.ENABLE_INSTRUCTOR_ASSISTANT_SELECTION));
        List<String> categories = extractCategoriesInTemplate(eval.getTemplate().getId());
        boolean hasInstructors = categories.contains(EvalConstants.ITEM_CATEGORY_INSTRUCTOR);
        boolean hasAssistants  = categories.contains(EvalConstants.ITEM_CATEGORY_ASSISTANT);
        model.addAttribute("enableInstructorAssistantSelection", enableInstructorAssistantSelection);
        model.addAttribute("hasInstructors", hasInstructors);
        model.addAttribute("hasAssistants",  hasAssistants);
        model.addAttribute("selectionTypeInstructor", EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR);
        model.addAttribute("selectionTypeAssistant",  EvalAssignGroup.SELECTION_TYPE_ASSISTANT);

        // Relative URL (no leading slash) so the portal resolves it correctly
        String currentUrl = "evaluation_assign?evaluationId=" + evaluationId;

        model.addAttribute("eval",              eval);
        model.addAttribute("evaluationId",      evaluationId);
        model.addAttribute("isPartial",         isPartial);
        model.addAttribute("groupRows",         groupRows);
        model.addAttribute("adhocGroupRows",    adhocGroupRows);
        model.addAttribute("enableAdhocGroups", enableAdhocGroups);
        model.addAttribute("currentUrl",        currentUrl);
        model.addAttribute("unpublishedCount",  unpublishedCount);
        model.addAttribute("startDate",         dtf.format(eval.getStartDate()));
        model.addAttribute("dueDate",           eval.getDueDate() != null ? dtf.format(eval.getDueDate()) : null);
        model.addAttribute("stopDate",          eval.getStopDate() != null ? dtf.format(eval.getStopDate()) : null);
        model.addAttribute("viewDate",          eval.getViewDate() != null ? dtf.format(eval.getViewDate()) : null);

        return "evaluation_assign";
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
            if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(tig.associateType))
                categories.add(EvalConstants.ITEM_CATEGORY_INSTRUCTOR);
            else if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(tig.associateType))
                categories.add(EvalConstants.ITEM_CATEGORY_ASSISTANT);
        }
        return categories;
    }
}