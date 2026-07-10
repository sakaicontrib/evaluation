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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.utils.EvalItemViewData;
import org.sakaiproject.evaluation.tool.utils.EvalItemViewDataBuilder;
import org.sakaiproject.evaluation.tool.utils.ScaleOptionsBuilder;
import org.sakaiproject.evaluation.tool.utils.ScaleOptionsBuilder.OptionData;
import org.sakaiproject.evaluation.tool.utils.ScaleOptionsBuilder.SteppedRow;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.HierarchyNodeGroup;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.TemplateItemGroup;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

/**
 * Preview view for an evaluation or template.
 * Spring MVC equivalent of PreviewEvalProducer.
 */
@Slf4j
@Controller
@RequestMapping("/preview_eval")
public class PreviewEvalController extends EvalControllerSupport {

    // ---- DTOs ----------------------------------------------------------------

    public static class ItemData extends EvalItemViewData {
    }

    @Data
    public static class NodeGroupData {
        String nodeTitle;
        List<ItemData> items = new ArrayList<>();
    }

    @Data
    public static class CategorySection {
        String header;
        List<NodeGroupData> nodeGroups = new ArrayList<>();
    }

    // ---- Services ------------------------------------------------------------


    @Autowired
    private MessageSource messageSource;

    // ---- Handler -------------------------------------------------------------

    @GetMapping
    public String show(
            @RequestParam(required = false) Long evaluationId,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) String evalGroupId,
            @RequestParam(required = false, defaultValue = "false") boolean external,
            Model model,
            HttpServletRequest request) {

        if (evaluationId == null && templateId == null) {
            throw new IllegalArgumentException("Must specify templateId or evaluationId");
        }

        Locale locale = RequestContextUtils.getLocale(request);
        String currentUserId = currentUserId();

        // ---- Build eval object (real or fake) --------------------------------
        EvalEvaluation eval;
        boolean isTemplatePreview = (evaluationId == null);

        if (isTemplatePreview) {
            EvalTemplate template = authoringService.getTemplateById(templateId);
            eval = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, currentUserId,
                    messageSource.getMessage("previeweval.evaluation.title.default", null, locale),
                    new Date(), new Date(), new Date(), new Date(),
                    EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.SHARING_VISIBLE, 1, template);
            eval.setInstructions(messageSource.getMessage("previeweval.instructions.default", null, locale));
        } else {
            eval = evaluationService.getEvaluationById(evaluationId);
            templateId = eval.getTemplate().getId();
        }

        model.addAttribute("eval", eval);
        model.addAttribute("isTemplatePreview", isTemplatePreview);
        model.addAttribute("external", external);

        // ---- Determine group for group-specific preview ----------------------
        EvalAssignGroup group = null;
        String groupDisplayTitle = null;
        if (evalGroupId != null && evaluationId != null) {
            // Direct link: use the explicitly provided group
            String title = commonLogic.getDisplayTitle(evalGroupId);
            groupDisplayTitle = (title != null) ? title : evalGroupId;
            Map<Long, List<EvalAssignGroup>> gm = evaluationService
                    .getAssignGroupsForEvals(new Long[]{evaluationId}, false, false);
            List<EvalAssignGroup> gl = gm.get(evaluationId);
            if (gl != null) {
                for (EvalAssignGroup ag : gl) {
                    if (evalGroupId.equals(ag.getEvalGroupId())) {
                        group = ag;
                        break;
                    }
                }
            }
        } else {
            Boolean useGroupSpecificPreview = (Boolean) settings.get(EvalSettings.ENABLE_GROUP_SPECIFIC_PREVIEW);
            if (useGroupSpecificPreview != null && useGroupSpecificPreview && evaluationId != null) {
                int groupCount = evaluationService.countEvaluationGroups(evaluationId, true);
                if (groupCount == 1) {
                    Map<Long, List<EvalAssignGroup>> groupMap = evaluationService
                            .getAssignGroupsForEvals(new Long[]{evaluationId}, false, false);
                    List<EvalAssignGroup> groups = groupMap.get(evaluationId);
                    if (groups != null && !groups.isEmpty()) {
                        EvalAssignGroup candidate = groups.get(0);
                        String gid = candidate.getEvalGroupId();
                        if (gid != null) {
                            String title = commonLogic.getDisplayTitle(gid);
                            if (title != null) {
                                group = candidate;
                                groupDisplayTitle = title;
                            } else {
                                groupDisplayTitle = gid;
                            }
                        }
                    }
                }
            }
        }

        if (groupDisplayTitle == null) {
            groupDisplayTitle = messageSource.getMessage("previeweval.course.title.default", null, locale);
        }
        model.addAttribute("groupDisplayTitle", groupDisplayTitle);

        // ---- Build TIDL ------------------------------------------------------
        TemplateItemDataList tidl = null;
        if (group == null) {
            List<EvalTemplateItem> allItems = authoringService.getTemplateItemsForTemplate(
                    templateId, new String[]{}, new String[]{}, new String[]{});
            if (!allItems.isEmpty()) {
                List<EvalHierarchyNode> hierarchyNodes = TemplateItemDataList.makeEvalNodesList(allItems, hierarchyLogic);
                List<String> instructors = new ArrayList<>();
                instructors.add("fake1");
                instructors.add("fake2");
                Map<String, List<String>> associates = new HashMap<>();
                associates.put(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, instructors);
                Boolean taEnabled = (Boolean) settings.get(EvalSettings.ENABLE_ASSISTANT_CATEGORY);
                if (Boolean.TRUE.equals(taEnabled)) {
                    List<String> tas = new ArrayList<>();
                    tas.add("fake1");
                    tas.add("fake2");
                    associates.put(EvalConstants.ITEM_CATEGORY_ASSISTANT, tas);
                }
                tidl = new TemplateItemDataList(allItems, hierarchyNodes, associates, null);
            }
        } else {
            tidl = new TemplateItemDataList(evaluationId, group.getEvalGroupId(),
                    evaluationService, authoringService, hierarchyLogic, null);
        }

        if (tidl == null) {
            model.addAttribute("categorySections", new ArrayList<>());
            model.addAttribute("noItems", true);
            return "preview_eval";
        }

        // ---- Render items into DTO list --------------------------------------
        boolean useCourseOnly = Boolean.TRUE.equals(settings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY));
        boolean showHierHeaders = Boolean.TRUE.equals(settings.get(EvalSettings.DISPLAY_HIERARCHY_HEADERS));

        List<CategorySection> sections = new ArrayList<>();
        int countInstructors = 0;
        int countAssistants = 0;
        int displayNumber = 1;

        for (TemplateItemGroup tig : tidl.getTemplateItemGroups()) {
            CategorySection section = new CategorySection();

            // Category header
            if (EvalConstants.ITEM_CATEGORY_COURSE.equals(tig.associateType) && !useCourseOnly) {
                section.setHeader(messageSource.getMessage("takeeval.group.questions.header", null, locale));
            } else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(tig.associateType)) {
                String name;
                if (group == null) {
                    name = tig.associateId.equals("fake2")
                            ? messageSource.getMessage("previeweval.instructor.2", null, locale)
                            : messageSource.getMessage("previeweval.instructor.1", null, locale);
                } else {
                    EvalUser user = commonLogic.getEvalUserById(tig.associateId);
                    name = user.displayName;
                }
                section.setHeader(messageSource.getMessage("takeeval.instructor.questions.header",
                        new Object[]{name}, locale));
                countInstructors++;
            } else if (EvalConstants.ITEM_CATEGORY_ASSISTANT.equals(tig.associateType)) {
                String name;
                if (group == null) {
                    name = tig.associateId.equals("fake2")
                            ? messageSource.getMessage("previeweval.ta.2", null, locale)
                            : messageSource.getMessage("previeweval.ta.1", null, locale);
                } else {
                    EvalUser user = commonLogic.getEvalUserById(tig.associateId);
                    name = user.displayName;
                }
                section.setHeader(messageSource.getMessage("takeeval.assistant.questions.header",
                        new Object[]{name}, locale));
                countAssistants++;
            } else {
                // course category when useCourseOnly — no header needed, set empty
                section.setHeader("");
            }

            for (HierarchyNodeGroup hng : tig.hierarchyNodeGroups) {
                NodeGroupData ngd = new NodeGroupData();
                if (hng.node != null && showHierHeaders) {
                    ngd.setNodeTitle(hng.node.title);
                }
                List<DataTemplateItem> dtis = hng.getDataTemplateItems(false);
                int rowIndex = 0;
                for (DataTemplateItem dti : dtis) {
                    ItemData itemData = buildItemData(dti.templateItem, displayNumber, locale);
                    itemData.setOdd(rowIndex % 2 != 0);
                    ngd.getItems().add(itemData);
                    // increment display number
                    if (!TemplateItemUtils.isAnswerable(dti.templateItem)) {
                        if (dti.blockChildItems != null) {
                            displayNumber += dti.blockChildItems.size();
                        }
                    } else {
                        displayNumber++;
                    }
                    rowIndex++;
                }
                section.getNodeGroups().add(ngd);
            }
            sections.add(section);
        }

        model.addAttribute("categorySections", sections);
        model.addAttribute("noItems", false);
        model.addAttribute("showGroupsNote", countInstructors > 0 || countAssistants > 0);

        return "preview_eval";
    }

    // ---- Item building -------------------------------------------------------

    private ItemData buildItemData(EvalTemplateItem ti, int displayNumber, Locale locale) {
        EvalItemViewData base = EvalItemViewDataBuilder.build(ti, displayNumber);
        ItemData d = new ItemData();
        d.setItemType(base.getItemType());
        d.setItemText(base.getItemText());
        d.setDisplayNumber(base.getDisplayNumber());
        d.setDisplayRows(base.getDisplayRows());
        d.setUsesNA(base.isUsesNA());
        d.setUsesComment(base.isUsesComment());
        d.setCompulsory(base.isCompulsory());
        d.setScaleDisplaySetting(base.getScaleDisplaySetting());
        d.setOptions(base.getOptions());
        d.setStartLabel(base.getStartLabel());
        d.setEndLabel(base.getEndLabel());
        d.setStartClass(base.getStartClass());
        d.setEndClass(base.getEndClass());
        d.setIdealImageUrl(base.getIdealImageUrl());
        d.setMatrixLabelStart(base.getMatrixLabelStart());
        d.setMatrixLabelEnd(base.getMatrixLabelEnd());
        d.setMatrixLabelMiddle(base.getMatrixLabelMiddle());
        d.setSteppedRows(base.getSteppedRows());

        if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(d.getItemType())) {
            List<EvalItemViewData> children = new ArrayList<>();
            if (ti.childTemplateItems != null) {
                int childNum = displayNumber;
                for (EvalTemplateItem child : ti.childTemplateItems) {
                    children.add(buildItemData(child, childNum++, locale));
                }
            }
            d.setChildItems(children);
        }
        return d;
    }
}
