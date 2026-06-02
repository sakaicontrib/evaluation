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
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;

/**
 * Create or edit an item block.
 * Spring MVC equivalent of ModifyBlockProducer.
 *
 * Modos GET:
 *  - templateItemIds with a single ID → edit existing block
 *  - templateItemIds with multiple IDs → create a new block with those items
 *
 * El formulario es cargado en un facebox desde modify_template_items.
 */
@Controller
@RequestMapping("/modify_block")
public class ModifyBlockController extends EvalControllerSupport {

    @Resource(name = "localTemplateLogic")
    private LocalTemplateLogic localTemplateLogic;

    @Data
    public static class ChildItemRow {
        private final Long templateItemId;
        private final String itemText;
        private final int orderNum;
    }

    @Data
    public static class BlockTextChoice {
        private final String id;
        private final String text;
    }

    @GetMapping
    public String show(@RequestParam Long templateId,
                       @RequestParam String templateItemIds,
                       Model model,
                       HttpServletRequest request) {

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        String[] ids = templateItemIds.split(",");
        List<EvalTemplateItem> templateItemList = new ArrayList<>();
        List<EvalTemplateItem> blockItemList = new ArrayList<>();

        for (String id : ids) {
            EvalTemplateItem item = authoringService.getTemplateItemById(Long.valueOf(id.trim()));
            templateItemList.add(item);
            if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(TemplateItemUtils.getTemplateItemType(item))) {
                blockItemList.add(item);
            }
        }

        EvalTemplateItem firstItem = templateItemList.get(0);
        boolean modify = (ids.length == 1);
        int originalDisplayOrder = firstItem.getDisplayOrder();

        // Validate child count for new block creation
        boolean validChildsNo = true;
        if (!modify) {
            int maxChildsNo = (Integer) settings.get(EvalSettings.ITEMS_ALLOWED_IN_QUESTION_BLOCK);
            int actualChildsNo = 0;
            for (EvalTemplateItem item : templateItemList) {
                if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(TemplateItemUtils.getTemplateItemType(item))) {
                    actualChildsNo += authoringService.getBlockChildTemplateItemsForBlockParent(item.getId(), false).size();
                } else {
                    actualChildsNo++;
                }
            }
            if (actualChildsNo > maxChildsNo) {
                validChildsNo = false;
            }
        }

        model.addAttribute("validChildsNo", validChildsNo);
        model.addAttribute("templateId", templateId);
        model.addAttribute("templateItemIds", templateItemIds);
        model.addAttribute("modify", modify);
        model.addAttribute("originalDisplayOrder", originalDisplayOrder);

        if (!validChildsNo) {
            return isAjax ? "modify_block :: blockContent" : "modify_block";
        }

        // blockId: existing parent ID or "new1"
        String blockId = modify ? firstItem.getId().toString() : "new1";
        model.addAttribute("blockId", blockId);

        // Owner display name
        model.addAttribute("ownerDisplayName", firstItem.getOwner());

        // Scale title from first item
        String scaleTitle = "";
        if (firstItem.getItem() != null && firstItem.getItem().getScale() != null) {
            scaleTitle = firstItem.getItem().getScale().getTitle();
        }
        model.addAttribute("scaleTitle", scaleTitle);

        // Current values (for edit mode) or defaults (for create mode)
        String currentScaleDisplay = EvalConstants.ITEM_SCALE_DISPLAY_MATRIX;
        boolean currentUsesNA = false;
        String currentCategory = EvalConstants.ITEM_CATEGORY_COURSE;

        if (modify && !blockItemList.isEmpty()) {
            EvalTemplateItem parent = blockItemList.get(0);
            currentScaleDisplay = parent.getScaleDisplaySetting() != null
                    ? parent.getScaleDisplaySetting() : EvalConstants.ITEM_SCALE_DISPLAY_MATRIX;
            currentUsesNA = Boolean.TRUE.equals(parent.getUsesNA());
            currentCategory = parent.getCategory() != null ? parent.getCategory() : EvalConstants.ITEM_CATEGORY_COURSE;
        }

        model.addAttribute("currentScaleDisplay", currentScaleDisplay);
        model.addAttribute("currentUsesNA", currentUsesNA);
        model.addAttribute("currentCategory", currentCategory);

        // Current item text (for edit mode)
        String currentItemText = "";
        if (modify && firstItem.getItem() != null) {
            currentItemText = firstItem.getItem().getItemText() != null ? firstItem.getItem().getItemText() : "";
        }
        model.addAttribute("currentItemText", currentItemText);
        model.addAttribute("inModal", isAjax);

        // Scale display options
        model.addAttribute("scaleDisplayValues", EvalToolConstants.SCALE_DISPLAY_GROUP_SETTING_VALUES);
        model.addAttribute("scaleDisplayLabels", EvalToolConstants.SCALE_DISPLAY_GROUP_SETTING_LABELS_PROPS);

        // Category settings
        Boolean useCourseCategoryOnly = (Boolean) settings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY);
        boolean showCategorySelector = !Boolean.TRUE.equals(useCourseCategoryOnly);
        model.addAttribute("showCategorySelector", showCategorySelector);
        if (showCategorySelector) {
            String[] catValues = EvalToolConstants.ITEM_CATEGORY_VALUES;
            String[] catLabels = EvalToolConstants.ITEM_CATEGORY_LABELS_PROPS;
            Boolean enableTA = (Boolean) settings.get(EvalSettings.ENABLE_ASSISTANT_CATEGORY);
            if (Boolean.TRUE.equals(enableTA)) {
                catValues = append(catValues, EvalToolConstants.ITEM_CATEGORY_ASSISTANT);
                catLabels = append(catLabels, EvalToolConstants.ITEM_CATEGORY_ASSISTANT_LABEL);
            }
            model.addAttribute("categoryValues", catValues);
            model.addAttribute("categoryLabels", catLabels);
        } else {
            // Fixed category
            String fixedCategory = Boolean.TRUE.equals(useCourseCategoryOnly)
                    ? EvalConstants.ITEM_CATEGORY_COURSE : EvalConstants.ITEM_CATEGORY_INSTRUCTOR;
            model.addAttribute("fixedCategory", fixedCategory);
        }

        // NA setting
        boolean enableNA = Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_NOT_AVAILABLE));
        model.addAttribute("enableNA", enableNA);

        // Block text choices (only when creating and there are existing blocks in selection)
        List<BlockTextChoice> blockTextChoices = new ArrayList<>();
        if (!modify && !blockItemList.isEmpty()) {
            for (EvalTemplateItem blockItem : blockItemList) {
                blockTextChoices.add(new BlockTextChoice(
                        blockItem.getId().toString(),
                        blockItem.getItem() != null ? blockItem.getItem().getItemText() : ""));
            }
        }
        model.addAttribute("blockTextChoices", blockTextChoices);

        // Child items list (flattened)
        List<ChildItemRow> childRows = new ArrayList<>();
        List<EvalTemplateItem> allTemplateItems = authoringService.getTemplateItemsForTemplate(
                templateId, new String[]{}, new String[]{}, new String[]{});
        int orderNum = 0;
        for (EvalTemplateItem item : templateItemList) {
            if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(TemplateItemUtils.getTemplateItemType(item))) {
                List<EvalTemplateItem> children = TemplateItemUtils.getChildItems(allTemplateItems, item.getId());
                for (EvalTemplateItem child : children) {
                    orderNum++;
                    childRows.add(new ChildItemRow(child.getId(),
                            child.getItem() != null ? child.getItem().getItemText() : "", orderNum));
                }
            } else {
                orderNum++;
                childRows.add(new ChildItemRow(item.getId(),
                        item.getItem() != null ? item.getItem().getItemText() : "", orderNum));
            }
        }
        model.addAttribute("childRows", childRows);

        // Show split-block link only in modify mode
        model.addAttribute("showSplitLink", modify);

        return isAjax ? "modify_block :: blockContent" : "modify_block";
    }

    @PostMapping
    public String save(@RequestParam Long templateId,
                       @RequestParam String templateItemIds,
                       @RequestParam String blockId,
                       @RequestParam Integer originalDisplayOrder,
                       @RequestParam(required = false) String orderedChildIds,
                       @RequestParam(value = "item-text::input", required = false) String itemText,
                       @RequestParam(required = false) String scaleDisplaySetting,
                       @RequestParam(required = false) Boolean usesNA,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String blockTextChoice,
                       HttpServletRequest request) {

        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        boolean isNewBlock = "new1".equals(blockId);
        Long[] parentIdHolder = {null};

        daoInvoker.invokeTransactionalAccess(() -> {
            if (!isNewBlock) {
                parentIdHolder[0] = doModifyBlock(blockId, orderedChildIds, itemText, scaleDisplaySetting, usesNA, category);
            } else {
                parentIdHolder[0] = doCreateBlock(templateId, templateItemIds, orderedChildIds, itemText,
                        scaleDisplaySetting, usesNA, category, blockTextChoice, originalDisplayOrder);
            }
        });
        Long parentId = parentIdHolder[0];

        if (isAjax) {
            return "item_saved";
        }
        if (!isNewBlock) {
            return "redirect:/modify_template_items?templateId=" + templateId + "&templateItemId=" + parentId;
        }
        return "redirect:/modify_template_items?templateId=" + templateId;
    }

    private Long doModifyBlock(String blockId, String orderedChildIds, String itemText,
                               String scaleDisplaySetting, Boolean usesNA, String category) {
        EvalTemplateItem parent = authoringService.getTemplateItemById(Long.valueOf(blockId));

        if (scaleDisplaySetting != null) {
            parent.setScaleDisplaySetting(scaleDisplaySetting);
            parent.getItem().setScaleDisplaySetting(scaleDisplaySetting);
        }
        boolean useNA = Boolean.TRUE.equals(usesNA);
        parent.setUsesNA(useNA);
        parent.getItem().setUsesNA(useNA);
        if (category != null) {
            parent.setCategory(category);
            parent.getItem().setCategory(category);
        }
        if (itemText != null && !itemText.trim().isEmpty()) {
            parent.getItem().setItemText(itemText);
        }

        localTemplateLogic.saveItem(parent.getItem());
        localTemplateLogic.saveTemplateItem(parent);

        // Update children display order
        if (orderedChildIds != null && !orderedChildIds.isEmpty()) {
            List<String> orderedList = Arrays.asList(orderedChildIds.split(","));
            List<EvalTemplateItem> blockChildren =
                    authoringService.getBlockChildTemplateItemsForBlockParent(parent.getId(), false);
            for (EvalTemplateItem child : blockChildren) {
                int idx = orderedList.indexOf(child.getId().toString());
                if (idx >= 0) {
                    child.setDisplayOrder(idx + 1);
                }
                child.setCategory(parent.getCategory());
                child.setUsesNA(parent.getUsesNA());
                child.setHierarchyLevel(parent.getHierarchyLevel());
                child.setHierarchyNodeId(parent.getHierarchyNodeId());
                localTemplateLogic.saveTemplateItem(child);
            }
        }

        return parent.getId();
    }

    private Long doCreateBlock(Long templateId, String templateItemIds, String orderedChildIds,
                               String itemText, String scaleDisplaySetting, Boolean usesNA,
                               String category, String blockTextChoice, int originalDisplayOrder) {

        List<String> templateItemIdList = Arrays.asList(templateItemIds.split(","));
        List<String> orderedChildIdList = orderedChildIds != null
                ? Arrays.asList(orderedChildIds.split(",")) : templateItemIdList;

        // Resolve text source
        EvalTemplateItem textSourceItem = null;
        if (blockTextChoice != null && !"new1".equals(blockTextChoice)) {
            textSourceItem = authoringService.getTemplateItemById(Long.valueOf(blockTextChoice));
        }

        // Get first real child for template/scale reference
        EvalTemplateItem referenceItem = authoringService.getTemplateItemById(
                Long.valueOf(orderedChildIdList.get(0).trim()));

        // Create new parent template item
        EvalTemplateItem parent = localTemplateLogic.newTemplateItem();
        parent.setTemplate(referenceItem.getTemplate());
        parent.getItem().setScale(referenceItem.getItem().getScale());

        String resolvedText = textSourceItem != null
                ? textSourceItem.getItem().getItemText()
                : (itemText != null ? itemText : "");
        parent.getItem().setItemText(resolvedText);

        boolean useNA = Boolean.TRUE.equals(usesNA);
        String resolvedCategory = category != null ? category : EvalConstants.ITEM_CATEGORY_COURSE;
        String resolvedScale = scaleDisplaySetting != null
                ? scaleDisplaySetting : EvalConstants.ITEM_SCALE_DISPLAY_MATRIX;

        parent.setBlockParent(Boolean.TRUE);
        parent.getItem().setClassification(EvalConstants.ITEM_TYPE_BLOCK_PARENT);
        parent.getItem().setSharing(referenceItem.getTemplate().getSharing());
        parent.setUsesNA(useNA);
        parent.getItem().setUsesNA(useNA);
        parent.setCategory(resolvedCategory);
        parent.getItem().setCategory(resolvedCategory);
        parent.setScaleDisplaySetting(resolvedScale);
        parent.getItem().setScaleDisplaySetting(resolvedScale);

        localTemplateLogic.saveItem(parent.getItem());
        localTemplateLogic.saveTemplateItem(parent);

        parent.setDisplayOrder(originalDisplayOrder);

        // Assign children
        List<EvalTemplateItem> allTemplateItems = authoringService.getTemplateItemsForTemplate(
                templateId, null, null, null);
        Long parentId = parent.getId();

        // Put the parent at the front of iteration if it's in the list
        if (blockTextChoice != null && !"new1".equals(blockTextChoice)
                && templateItemIdList.contains(blockTextChoice)) {
            templateItemIdList = new ArrayList<>(templateItemIdList);
            int idx = templateItemIdList.indexOf(blockTextChoice);
            if (idx > 0) {
                ((List<String>) templateItemIdList).set(idx, templateItemIdList.get(0));
                ((List<String>) templateItemIdList).set(0, blockTextChoice);
            }
        }

        for (String itemId : templateItemIdList) {
            EvalTemplateItem ti = authoringService.getTemplateItemById(Long.valueOf(itemId.trim()));
            if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(TemplateItemUtils.getTemplateItemType(ti))) {
                List<EvalTemplateItem> children = TemplateItemUtils.getChildItems(allTemplateItems, ti.getId());
                for (EvalTemplateItem child : children) {
                    child.setBlockId(parentId);
                    int childIdx = orderedChildIdList.indexOf(child.getId().toString());
                    child.setDisplayOrder(childIdx >= 0 ? childIdx + 1 : 1);
                    localTemplateLogic.saveTemplateItem(child);
                }
                if (!ti.getId().equals(parentId)) {
                    localTemplateLogic.deleteTemplateItem(ti.getId());
                }
            } else {
                ti.setBlockParent(Boolean.FALSE);
                ti.setBlockId(parentId);
                int childIdx = orderedChildIdList.indexOf(itemId.trim());
                ti.setDisplayOrder(childIdx >= 0 ? childIdx + 1 : 1);
                ti.setCategory(resolvedCategory);
                ti.setUsesNA(useNA);
                ti.setHierarchyLevel(parent.getHierarchyLevel());
                ti.setHierarchyNodeId(parent.getHierarchyNodeId());
                localTemplateLogic.saveTemplateItem(ti);
            }
        }

        // Fix display orders of remaining non-child items
        allTemplateItems = authoringService.getTemplateItemsForTemplate(templateId, null, null, null);
        List<EvalTemplateItem> nonChildList = TemplateItemUtils.getNonChildItems(allTemplateItems);
        for (int i = originalDisplayOrder; i < nonChildList.size(); i++) {
            EvalTemplateItem ti = nonChildList.get(i);
            if (ti.getDisplayOrder() != (i + 1)) {
                ti.setDisplayOrder(i + 1);
                localTemplateLogic.saveTemplateItem(ti);
            }
        }

        return parentId;
    }

    private static String[] append(String[] arr, String value) {
        String[] result = Arrays.copyOf(arr, arr.length + 1);
        result[arr.length] = value;
        return result;
    }
}