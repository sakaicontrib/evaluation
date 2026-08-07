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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;

/**
 * Manages items within a template (reorder, add, remove).
 * Spring MVC equivalent of ModifyTemplateItemsProducer.
 */
@Controller
@RequestMapping("/modify_template_items")
public class ModifyTemplateItemsController extends EvalControllerSupport {

    private static final String BACK_URL_SESSION_KEY = "modifyTemplateItemsBackUrl";

    // Item-editing sub-screens that redirect back here on their own (not real origins):
    // their Referer must never overwrite the backUrl captured from the real entry point.
    private static final String[] CHILD_SCREEN_PATHS = {
        "/modify_item", "/remove_item", "/unblock_item", "/modify_block",
        "/choose_existing_items", "/choose_expert_items",
        "/choose_expert_objective", "/choose_expert_category",
        // "/modify_template" also matches "/modify_template_items" itself (substring),
        // covering both the template-info edit screen and self-referencing reloads
        // (e.g. the partial item-row refresh, EVALSYS-878) - neither is a real origin.
        "/modify_template"
    };

    /**
     * Resolves where the "Back" button should go: the page modify_template_items was
     * originally opened from (My Templates, or Evaluation Settings while building an
     * eval), not the item sub-screen that just redirected back here. Stored in the
     * session because each item add/edit/remove/unblock action is its own full-page
     * redirect back to this URL, so browser history (history.go(-1)) would land on that
     * sub-screen instead.
     */
    private String resolveBackUrl(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String referer = request.getHeader("Referer");
        boolean refererIsChildScreen = false;
        if (referer != null) {
            for (String childPath : CHILD_SCREEN_PATHS) {
                if (referer.contains(childPath)) {
                    refererIsChildScreen = true;
                    break;
                }
            }
        }
        if (referer != null && !refererIsChildScreen) {
            session.setAttribute(BACK_URL_SESSION_KEY, referer);
        }
        return (String) session.getAttribute(BACK_URL_SESSION_KEY);
    }

    @Data
    public static class TemplateItemRow {
        Long templateItemId;
        Long itemId;
        int displayOrder;
        String classification;
        String classificationLabelKey;
        String itemTextShort;
        String itemTextFull;
        boolean longText;
        String scaleDisplaySetting;
        boolean showCategory;
        String categoryLabelKey;
        boolean showHierarchy;
        String hierarchyNodeTitle;
        boolean showResultsSharing;
        String resultsSharingKey;
        boolean blockParent;
        boolean showBlockCheckbox;
        String blockCheckboxId;
        Long scaleId;
        String scaleTitle;
        boolean usesNA;
        boolean usesComment;
        List<ChildItemRow> children = new ArrayList<>();
    }

    @Data
    public static class ChildItemRow {
        Long templateItemId;
        int displayOrder;
        String itemText;
    }

    @GetMapping
    public String show(@RequestParam Long templateId,
                       @RequestParam(required = false) Long templateItemId,
                       Model model, HttpServletRequest request) {

        model.addAttribute("backUrl", resolveBackUrl(request));

        String currentUserId = currentUserId();
        EvalTemplate template = localTemplateLogic.fetchTemplate(templateId);
        List<EvalTemplateItem> allItems = localTemplateLogic.fetchTemplateItems(templateId);

        // EVALSYS-878: partial mode — return only one item row for JS update
        boolean showTemplateItemOnly = (templateItemId != null);
        List<EvalTemplateItem> itemList = allItems;
        if (showTemplateItemOnly) {
            EvalTemplateItem target = null;
            for (EvalTemplateItem ti : allItems) {
                if (ti.getId().equals(templateItemId)) {
                    target = ti;
                    break;
                }
            }
            if (target == null) {
                throw new IllegalArgumentException("templateItemId not found: " + templateItemId);
            }
            itemList = new ArrayList<>(TemplateItemUtils.getChildItems(allItems, templateItemId));
            itemList.add(target);
        }

        List<EvalTemplateItem> topLevelItems = TemplateItemUtils.getNonChildItems(itemList);

        boolean showHierarchy = Boolean.TRUE.equals(settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS));
        boolean showResultsSharing = Boolean.TRUE.equals(settings.get(EvalSettings.ITEM_USE_RESULTS_SHARING));
        boolean useExpertItems = Boolean.TRUE.equals(settings.get(EvalSettings.USE_EXPERT_ITEMS));
        boolean beginEvalEnabled = !topLevelItems.isEmpty() && evaluationService.canBeginEvaluation(currentUserId);

        String siteId = commonLogic.getContentCollectionId(commonLogic.getCurrentEvalGroup());

        List<TemplateItemRow> rows = new ArrayList<>();
        for (int i = 0; i < topLevelItems.size(); i++) {
            EvalTemplateItem ti = topLevelItems.get(i);
            TemplateItemRow row = new TemplateItemRow();
            row.setTemplateItemId(ti.getId());
            row.setItemId(ti.getItem().getId());
            row.setDisplayOrder(i + 1);

            String classification = ti.getItem().getClassification();
            row.setClassification(classification);

            // Classification label key
            String labelKey = EvalToolConstants.UNKNOWN_KEY;
            for (int j = 0; j < EvalToolConstants.ITEM_CLASSIFICATION_VALUES.length; j++) {
                if (classification.equals(EvalToolConstants.ITEM_CLASSIFICATION_VALUES[j])) {
                    labelKey = EvalToolConstants.ITEM_CLASSIFICATION_LABELS_PROPS[j];
                    break;
                }
            }
            row.setClassificationLabelKey(labelKey);

            // Item text — plain text, truncated at 150 chars
            String plainText = commonLogic.makePlainTextFromHTML(ti.getItem().getItemText());
            if (plainText == null) plainText = "";
            row.setLongText(plainText.length() > 150);
            row.setItemTextShort(plainText.length() > 150 ? plainText.substring(0, 150) : plainText);
            row.setItemTextFull(plainText);

            // Scale display
            row.setScaleDisplaySetting(ti.getScaleDisplaySetting());

            // Category (only show non-Course categories)
            boolean showCat = ti.getCategory() != null
                    && !EvalConstants.ITEM_CATEGORY_COURSE.equals(ti.getCategory());
            row.setShowCategory(showCat);
            if (showCat) {
                row.setCategoryLabelKey(RenderingUtils.getCategoryLabelKey(ti.getCategory()));
            }

            // Hierarchy node
            boolean showHier = showHierarchy
                    && ti.getHierarchyLevel() != null
                    && !EvalConstants.HIERARCHY_LEVEL_TOP.equals(ti.getHierarchyLevel());
            row.setShowHierarchy(showHier);
            if (showHier) {
                EvalHierarchyNode node = hierarchyLogic.getNodeById(ti.getHierarchyNodeId());
                row.setHierarchyNodeTitle(node != null ? node.title : ti.getHierarchyNodeId());
            }

            // Results sharing
            row.setShowResultsSharing(showResultsSharing);
            if (showResultsSharing && ti.getResultsSharing() != null) {
                row.setResultsSharingKey(resolveResultsSharingKey(ti.getResultsSharing()));
            }

            // Block parent
            boolean isBlockParent = TemplateItemUtils.isBlockParent(ti);
            row.setBlockParent(isBlockParent);

            // Block checkbox (Scaled or BlockParent items can be grouped)
            boolean showCb = EvalConstants.ITEM_TYPE_SCALED.equals(classification) || isBlockParent;
            row.setShowBlockCheckbox(showCb);
            if (showCb && ti.getItem().getScale() != null && ti.getItem().getScale().getId() != null) {
                Long scaleId = ti.getItem().getScale().getId();
                row.setScaleId(scaleId);
                row.setBlockCheckboxId("block-" + scaleId + "-" + ti.getId());
            }

            // Scale title (for Scaled items)
            if (EvalConstants.ITEM_TYPE_SCALED.equals(classification) && ti.getItem().getScale() != null) {
                row.setScaleTitle(ti.getItem().getScale().getTitle());
            }

            // Options flags
            row.setUsesNA(Boolean.TRUE.equals(ti.getUsesNA()));
            row.setUsesComment(Boolean.TRUE.equals(ti.getUsesComment()));

            // Block children
            if (isBlockParent) {
                List<EvalTemplateItem> childItems = TemplateItemUtils.getChildItems(allItems, ti.getId());
                for (int j = 0; j < childItems.size(); j++) {
                    EvalTemplateItem child = childItems.get(j);
                    ChildItemRow childRow = new ChildItemRow();
                    childRow.setTemplateItemId(child.getId());
                    childRow.setDisplayOrder(j + 1);
                    String childText = commonLogic.makePlainTextFromHTML(child.getItem().getItemText());
                    childRow.setItemText(childText != null ? childText : "");
                    row.getChildren().add(childRow);
                }
            }

            rows.add(row);
        }

        model.addAttribute("templateId", templateId);
        model.addAttribute("templateTitle", template.getTitle());
        model.addAttribute("templateDescription", template.getDescription());
        model.addAttribute("showDescription",
                template.getDescription() != null && !template.getDescription().trim().isEmpty());
        model.addAttribute("siteId", siteId);
        model.addAttribute("items", rows);
        model.addAttribute("itemCount", topLevelItems.size());
        model.addAttribute("beginEvalEnabled", beginEvalEnabled);
        model.addAttribute("useExpertItems", useExpertItems);
        model.addAttribute("addItemValues", EvalToolConstants.ITEM_SELECT_CLASSIFICATION_VALUES);
        model.addAttribute("addItemLabels", EvalToolConstants.ITEM_SELECT_CLASSIFICATION_LABELS);
        model.addAttribute("showTemplateItemOnly", showTemplateItemOnly);

        return "modify_template_items";
    }

    private String resolveResultsSharingKey(String sharing) {
        if (EvalConstants.SHARING_PUBLIC.equals(sharing))  return "general.public";
        if (EvalConstants.SHARING_PRIVATE.equals(sharing)) return "general.private";
        if (EvalConstants.SHARING_ADMIN.equals(sharing))   return "modifyitem.results.sharing.admin.short";
        if (EvalConstants.SHARING_STUDENT.equals(sharing)) return "modifyitem.results.sharing.student";
        if (EvalConstants.SHARING_BOTH.equals(sharing))    return "modifyitem.results.sharing.both";
        return "unknown.caps";
    }
}