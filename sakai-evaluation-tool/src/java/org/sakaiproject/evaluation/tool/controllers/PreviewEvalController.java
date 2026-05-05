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

import javax.annotation.Resource;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;
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
public class PreviewEvalController {

    // ---- DTOs ----------------------------------------------------------------

    @Data
    public static class OptionData {
        int index;
        String value;
        String label;
    }

    @Data
    public static class SteppedRow {
        String label;
        int middleCount;
        String value;
    }

    @Data
    public static class ItemData {
        String itemType;
        String itemText;
        int displayNumber;
        int displayRows;
        boolean usesNA;
        boolean usesComment;
        boolean compulsory;
        String scaleDisplaySetting;
        List<OptionData> options;
        String startLabel;
        String endLabel;
        String startClass;
        String endClass;
        String idealImageUrl;
        String matrixLabelStart;
        String matrixLabelEnd;
        String matrixLabelMiddle;
        List<SteppedRow> steppedRows;
        List<ItemData> childItems;
        boolean odd;
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

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationService")
    private EvalEvaluationService evaluationService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalSettings")
    private EvalSettings evalSettings;

    @Resource(name = "org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic")
    private ExternalHierarchyLogic hierarchyLogic;

    @Autowired
    private MessageSource messageSource;

    // ---- Handler -------------------------------------------------------------

    @GetMapping
    public String show(
            @RequestParam(required = false) Long evaluationId,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false, defaultValue = "false") boolean external,
            Model model,
            HttpServletRequest request) {

        if (evaluationId == null && templateId == null) {
            throw new IllegalArgumentException("Must specify templateId or evaluationId");
        }

        Locale locale = RequestContextUtils.getLocale(request);
        String currentUserId = commonLogic.getCurrentUserId();

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
        Boolean useGroupSpecificPreview = (Boolean) evalSettings.get(EvalSettings.ENABLE_GROUP_SPECIFIC_PREVIEW);
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
                Boolean taEnabled = (Boolean) evalSettings.get(EvalSettings.ENABLE_ASSISTANT_CATEGORY);
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
        boolean useCourseOnly = Boolean.TRUE.equals(evalSettings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY));
        boolean showHierHeaders = Boolean.TRUE.equals(evalSettings.get(EvalSettings.DISPLAY_HIERARCHY_HEADERS));

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
        ItemData d = new ItemData();
        String type = TemplateItemUtils.getTemplateItemType(ti);
        d.setItemType(type);
        d.setItemText(ti.getItem().getItemText());
        d.setDisplayNumber(displayNumber);
        d.setUsesNA(Boolean.TRUE.equals(ti.getUsesNA()));
        d.setUsesComment(Boolean.TRUE.equals(ti.getUsesComment()));
        d.setCompulsory(Boolean.TRUE.equals(ti.isCompulsory()));

        if (EvalConstants.ITEM_TYPE_TEXT.equals(type)) {
            d.setDisplayRows(ti.getDisplayRows() != null ? ti.getDisplayRows() : 3);

        } else if (EvalConstants.ITEM_TYPE_SCALED.equals(type)) {
            populateScaleData(d, ti);

        } else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(type)
                || EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(type)) {
            populateChoiceData(d, ti);

        } else if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type)) {
            populateScaleData(d, ti);
            List<ItemData> children = new ArrayList<>();
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

    private void populateScaleData(ItemData d, EvalTemplateItem ti) {
        EvalScale scale = ti.getItem().getScale();
        List<String> rawOptions = scale.getOptions();
        int n = rawOptions.size();

        String ds = ti.getScaleDisplaySetting();
        if (ds == null) ds = EvalConstants.ITEM_SCALE_DISPLAY_FULL;
        d.setScaleDisplaySetting(ds);

        boolean isColored = EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED.equals(ds)
                || EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED.equals(ds)
                || EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(ds)
                || EvalConstants.ITEM_SCALE_DISPLAY_MATRIX_COLORED.equals(ds);
        boolean isCompact = EvalConstants.ITEM_SCALE_DISPLAY_COMPACT.equals(ds)
                || EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED.equals(ds);
        boolean isStepped = EvalConstants.ITEM_SCALE_DISPLAY_STEPPED.equals(ds)
                || EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(ds);
        boolean isMatrix = EvalConstants.ITEM_SCALE_DISPLAY_MATRIX.equals(ds)
                || EvalConstants.ITEM_SCALE_DISPLAY_MATRIX_COLORED.equals(ds);

        if (isColored) {
            d.setIdealImageUrl(resolveContextUrl(EvalToolConstants.COLORED_IMAGE_URLS[ScaledUtils.idealIndex(scale)]));
        }

        if (isCompact) {
            d.setStartLabel(rawOptions.get(0));
            d.setEndLabel(rawOptions.get(n - 1));
            if (isColored) {
                d.setStartClass(ScaledUtils.getStartClass(scale));
                d.setEndClass(ScaledUtils.getEndClass(scale));
            }
            List<OptionData> opts = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                OptionData o = new OptionData();
                o.setIndex(j); o.setValue(String.valueOf(j)); o.setLabel(" ");
                opts.add(o);
            }
            d.setOptions(opts);

        } else if (isStepped) {
            List<SteppedRow> rows = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                SteppedRow row = new SteppedRow();
                row.setLabel(rawOptions.get(n - 1 - j));
                row.setMiddleCount(j);
                row.setValue(String.valueOf(n - 1 - j));
                rows.add(row);
            }
            d.setSteppedRows(rows);
            List<OptionData> opts = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                OptionData o = new OptionData();
                o.setIndex(j); o.setValue(String.valueOf(n - 1 - j)); o.setLabel(rawOptions.get(n - 1 - j));
                opts.add(o);
            }
            d.setOptions(opts);

        } else if (isMatrix) {
            List<String> headers = RenderingUtils.getMatrixLabels(rawOptions);
            d.setMatrixLabelStart(headers.get(0));
            d.setMatrixLabelEnd(headers.get(1));
            if (headers.size() >= 3) d.setMatrixLabelMiddle(headers.get(2));
            List<OptionData> opts = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                OptionData o = new OptionData();
                o.setIndex(j); o.setValue(String.valueOf(n - 1 - j)); o.setLabel(String.valueOf(j + 1));
                opts.add(o);
            }
            d.setOptions(opts);

        } else {
            // Full, FullColored, Vertical
            List<OptionData> opts = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                OptionData o = new OptionData();
                o.setIndex(j); o.setValue(String.valueOf(j)); o.setLabel(rawOptions.get(j));
                opts.add(o);
            }
            d.setOptions(opts);
        }
    }

    private void populateChoiceData(ItemData d, EvalTemplateItem ti) {
        EvalScale scale = ti.getItem().getScale();
        List<String> rawOptions = scale.getOptions();
        String ds = ti.getScaleDisplaySetting();
        if (ds == null) ds = EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL;
        d.setScaleDisplaySetting(ds);
        d.setUsesNA(Boolean.TRUE.equals(ti.getUsesNA()));
        List<OptionData> opts = new ArrayList<>();
        for (int j = 0; j < rawOptions.size(); j++) {
            OptionData o = new OptionData();
            o.setIndex(j); o.setValue(String.valueOf(j)); o.setLabel(rawOptions.get(j));
            opts.add(o);
        }
        d.setOptions(opts);
    }

    private String resolveContextUrl(String url) {
        return url.replace("$context", "");
    }
}