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
import org.sakaiproject.evaluation.dao.EvalDaoInvoker;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import lombok.Data;

/**
 * Creation and editing of items and template items.
 * Spring MVC equivalent of ModifyItemProducer.
 *
 * Casos soportados:
 * - New item bank entry (itemClassification, no templateId)
 * - New template item (itemClassification + templateId)
 * - Edit item bank entry (itemId, no templateId)
 * - Edit template item (templateItemId + templateId)
 */
@Controller
@RequestMapping("/modify_item")
public class ModifyItemController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalSettings")
    private EvalSettings settings;

    @Resource(name = "localTemplateLogic")
    private LocalTemplateLogic localTemplateLogic;

    @Resource(name = "org.sakaiproject.evaluation.dao.EvalDaoInvoker")
    private EvalDaoInvoker daoInvoker;

    @Data
    public static class ExpertGroupOption {
        Long id;
        String label;
    }

    @GetMapping
    public String show(@RequestParam(required = false) Long itemId,
                       @RequestParam(required = false) Long templateItemId,
                       @RequestParam(required = false) Long templateId,
                       @RequestParam(required = false) String itemClassification,
                       Model model,
                       HttpServletRequest request) {

        boolean inFacebox = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        model.addAttribute("inFacebox", inFacebox);

        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        // ---- Resolve which object is being edited ----
        EvalItem item = null;
        EvalTemplateItem templateItem = null;
        boolean itemLocked = false;
        Long scaleId = null;
        String scaleDisplaySetting = null;
        String displayRows = null;
        Boolean usesNA = null;
        Boolean usesComment = null;
        Boolean compulsory = null;
        String category = EvalConstants.ITEM_CATEGORY_COURSE;
        String resultsSharing = EvalConstants.SHARING_ADMIN;
        String ownerName = commonLogic.getEvalUserById(currentUserId).displayName;

        if (templateItemId != null) {
            // Edit existing template item
            templateItem = authoringService.getTemplateItemById(templateItemId);
            if (templateItem == null) {
                throw new IllegalArgumentException("Template item not found: " + templateItemId);
            }
            item = templateItem.getItem();
            itemClassification = item.getClassification();
            itemLocked = item.getLocked() != null && item.getLocked();
            ownerName = commonLogic.getEvalUserById(item.getOwner()).displayName;
            scaleDisplaySetting = templateItem.getScaleDisplaySetting();
            displayRows = templateItem.getDisplayRows() != null ? templateItem.getDisplayRows().toString() : null;
            usesNA = templateItem.getUsesNA();
            usesComment = templateItem.getUsesComment();
            compulsory = templateItem.isCompulsory();
            category = templateItem.getCategory() != null ? templateItem.getCategory() : category;
            resultsSharing = templateItem.getResultsSharing() != null ? templateItem.getResultsSharing() : resultsSharing;
            if (item.getScale() != null) scaleId = item.getScale().getId();

        } else if (itemId != null) {
            // Edit item bank entry
            item = authoringService.getItemById(itemId);
            if (item == null) {
                throw new IllegalArgumentException("Item not found: " + itemId);
            }
            itemClassification = item.getClassification();
            itemLocked = item.getLocked() != null && item.getLocked();
            ownerName = commonLogic.getEvalUserById(item.getOwner()).displayName;
            scaleDisplaySetting = item.getScaleDisplaySetting();
            displayRows = item.getDisplayRows() != null ? item.getDisplayRows().toString() : null;
            usesNA = item.getUsesNA();
            usesComment = item.getUsesComment();
            compulsory = item.isCompulsory();
            category = item.getCategory() != null ? item.getCategory() : category;
            if (item.getScale() != null) scaleId = item.getScale().getId();

        } else {
            // New item
            if (itemClassification == null || itemClassification.isEmpty()) {
                throw new IllegalArgumentException("itemClassification required for new items");
            }
        }

        // ---- Item text ----
        String itemText = (item != null && item.getItemText() != null) ? item.getItemText() : "";
        String sharing = (item != null && item.getSharing() != null) ? item.getSharing() : EvalConstants.SHARING_PRIVATE;
        boolean itemExpert = (item != null) && Boolean.TRUE.equals(item.getExpert());
        String expertDescription = (item != null && item.getExpertDescription() != null) ? item.getExpertDescription() : "";
        Long itemGroupId = (item != null && item.getItemGroupId() != null) ? item.getItemGroupId() : 0L;

        // ---- Scale for SCALED items ----
        List<EvalScale> availableScales = null;
        List<String> scaleLabels = new ArrayList<>();
        List<String> scaleValues = new ArrayList<>();
        if (EvalConstants.ITEM_TYPE_SCALED.equals(itemClassification) && !itemLocked) {
            availableScales = authoringService.getScalesForUser(currentUserId, null);
            if (item != null && item.getScale() != null && item.getScale().getId() != null
                    && !availableScales.contains(item.getScale())) {
                availableScales.add(0, item.getScale());
            }
            if (availableScales != null) {
                for (EvalScale s : availableScales) {
                    if (s.getId() != null) {
                        scaleValues.add(s.getId().toString());
                        scaleLabels.add(ScaledUtils.makeScaleText(s, 90));
                    }
                }
            }
        }

        // ---- Options for MC/MA items ----
        List<String> choiceOptions = new ArrayList<>(Arrays.asList(EvalToolConstants.DEFAULT_INITIAL_SCALE_VALUES));
        if ((EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemClassification) ||
             EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemClassification)) && item != null) {
            EvalScale adhocScale = item.getScale();
            if (adhocScale != null && adhocScale.getOptions() != null) {
                choiceOptions = new ArrayList<>(adhocScale.getOptions());
            }
        }

        // ---- Settings that control field visibility ----
        Boolean useCourseCategoryOnly = (Boolean) settings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY);
        Boolean naAllowed = (Boolean) settings.get(EvalSettings.ENABLE_NOT_AVAILABLE);
        Boolean commentAllowed = (Boolean) settings.get(EvalSettings.ENABLE_ITEM_COMMENTS);
        Boolean useExpertItems = (Boolean) settings.get(EvalSettings.USE_EXPERT_ITEMS);
        Boolean useResultSharing = (Boolean) settings.get(EvalSettings.ITEM_USE_RESULTS_SHARING);
        Boolean enableTA = (Boolean) settings.get(EvalSettings.ENABLE_ASSISTANT_CATEGORY);
        Boolean enableTextRequired = (Boolean) settings.get(EvalSettings.ENABLE_TEXT_ITEM_REQUIRED);

        boolean showItemDisplayHints = true;
        if (Boolean.TRUE.equals(useCourseCategoryOnly) && EvalConstants.ITEM_TYPE_HEADER.equals(itemClassification)) {
            showItemDisplayHints = false;
        }

        // Compulsory: text items only if setting allows
        boolean showCompulsory = !EvalConstants.ITEM_TYPE_HEADER.equals(itemClassification);
        if (showCompulsory && EvalConstants.ITEM_TYPE_TEXT.equals(itemClassification)) {
            showCompulsory = (enableTextRequired == null || Boolean.TRUE.equals(enableTextRequired));
        }

        // Categories
        String[] categoryValues = EvalToolConstants.ITEM_CATEGORY_VALUES;
        String[] categoryLabels = EvalToolConstants.ITEM_CATEGORY_LABELS_PROPS;
        if (Boolean.TRUE.equals(enableTA)) {
            categoryValues = appendString(categoryValues, EvalToolConstants.ITEM_CATEGORY_ASSISTANT);
            categoryLabels = appendString(categoryLabels, EvalToolConstants.ITEM_CATEGORY_ASSISTANT_LABEL);
        }

        // ---- Expert item groups ----
        List<ExpertGroupOption> expertGroups = new ArrayList<>();
        expertGroups.add(buildOption(0L, "None"));
        if (userAdmin && templateId == null && Boolean.TRUE.equals(useExpertItems)) {
            List<EvalItemGroup> itemGroups = authoringService.getAllItemGroups(currentUserId, true);
            for (EvalItemGroup eig : itemGroups) {
                String prefix = EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(eig.getType()) ? "" : "...";
                expertGroups.add(buildOption(eig.getId(), prefix + eig.getTitle()));
            }
        }

        // ---- Permisos ----
        boolean canRemove = false;
        if (itemId != null) {
            canRemove = authoringService.canRemoveItem(currentUserId, itemId);
        } else if (templateItemId != null) {
            canRemove = authoringService.canControlTemplateItem(currentUserId, templateItemId);
        }

        // ---- i18n for classification ----
        String classificationLabelKey = EvalToolConstants.UNKNOWN_KEY;
        for (int i = 0; i < EvalToolConstants.ITEM_CLASSIFICATION_VALUES.length; i++) {
            if (itemClassification.equals(EvalToolConstants.ITEM_CLASSIFICATION_VALUES[i])) {
                classificationLabelKey = EvalToolConstants.ITEM_CLASSIFICATION_LABELS_PROPS[i];
                break;
            }
        }

        // ---- Current scale name for locked items ----
        String currentScaleText = null;
        if (itemLocked && item != null && item.getScale() != null) {
            currentScaleText = ScaledUtils.makeScaleText(item.getScale(), 0);
        }

        // ---- Poblar modelo ----
        model.addAttribute("itemId",              itemId);
        model.addAttribute("templateItemId",      templateItemId);
        model.addAttribute("templateId",          templateId);
        model.addAttribute("itemClassification",  itemClassification);
        model.addAttribute("classificationLabelKey", classificationLabelKey);
        model.addAttribute("ownerName",           ownerName);
        model.addAttribute("itemText",            itemText);
        model.addAttribute("itemLocked",          itemLocked);
        model.addAttribute("userAdmin",           userAdmin);
        model.addAttribute("sharing",             sharing);
        model.addAttribute("itemExpert",          itemExpert);
        model.addAttribute("expertDescription",   expertDescription);
        model.addAttribute("itemGroupId",         itemGroupId);
        model.addAttribute("expertGroups",        expertGroups);
        model.addAttribute("canRemove",           canRemove);

        // Scale
        model.addAttribute("showItemScale",
            EvalConstants.ITEM_TYPE_SCALED.equals(itemClassification) && !itemLocked);
        model.addAttribute("scaleValues",         scaleValues);
        model.addAttribute("scaleLabels",         scaleLabels);
        model.addAttribute("currentScaleId",      scaleId != null ? scaleId.toString() : null);
        model.addAttribute("currentScaleText",    currentScaleText);
        model.addAttribute("showItemScaleLocked",
            EvalConstants.ITEM_TYPE_SCALED.equals(itemClassification) && itemLocked);

        // Choices (MC/MA)
        boolean isChoice = EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemClassification)
                        || EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemClassification);
        model.addAttribute("showItemChoices",     isChoice);
        model.addAttribute("choiceOptions",       choiceOptions);

        // Sharing
        model.addAttribute("showItemSharing",     userAdmin && templateId == null);
        model.addAttribute("sharingValues",       EvalToolConstants.SHARING_VALUES);
        model.addAttribute("sharingLabels",       EvalToolConstants.SHARING_LABELS_PROPS);

        // Expert
        model.addAttribute("showItemExpert",
            userAdmin && templateId == null && Boolean.TRUE.equals(useExpertItems));

        // Display hints
        model.addAttribute("showItemDisplayHints", showItemDisplayHints);
        boolean showScaleDisplay = EvalConstants.ITEM_TYPE_SCALED.equals(itemClassification) && !itemLocked;
        boolean showChoicesDisplay = isChoice && !itemLocked;
        model.addAttribute("showScaleDisplay",    showScaleDisplay);
        model.addAttribute("showChoicesDisplay",  showChoicesDisplay);
        model.addAttribute("scaleDisplayValues",  EvalToolConstants.SCALE_DISPLAY_SETTING_VALUES);
        model.addAttribute("scaleDisplayLabels",  EvalToolConstants.SCALE_DISPLAY_SETTING_LABELS_PROPS);
        model.addAttribute("choicesDisplayValues",EvalToolConstants.CHOICES_DISPLAY_SETTING_VALUES);
        model.addAttribute("choicesDisplayLabels",EvalToolConstants.CHOICES_DISPLAY_SETTING_LABELS_PROPS);
        model.addAttribute("scaleDisplaySetting", scaleDisplaySetting);
        model.addAttribute("showResponseSize",    EvalConstants.ITEM_TYPE_TEXT.equals(itemClassification));
        model.addAttribute("responseSizeValues",  EvalToolConstants.RESPONSE_SIZE_VALUES);
        model.addAttribute("responseSizeLabels",  EvalToolConstants.RESPONSE_SIZE_LABELS_PROPS);
        model.addAttribute("displayRows",         displayRows != null ? displayRows : "2");
        model.addAttribute("showNA",              !EvalConstants.ITEM_TYPE_HEADER.equals(itemClassification) && Boolean.TRUE.equals(naAllowed));
        model.addAttribute("usesNA",              usesNA);
        model.addAttribute("showComment",         !EvalConstants.ITEM_TYPE_HEADER.equals(itemClassification)
                                                && !EvalConstants.ITEM_TYPE_TEXT.equals(itemClassification)
                                                && Boolean.TRUE.equals(commentAllowed));
        model.addAttribute("usesComment",         usesComment);
        model.addAttribute("showCompulsory",      showCompulsory);
        model.addAttribute("compulsory",          compulsory);
        model.addAttribute("showCategory",        !Boolean.TRUE.equals(useCourseCategoryOnly));
        model.addAttribute("category",            category);
        model.addAttribute("categoryValues",      categoryValues);
        model.addAttribute("categoryLabels",      categoryLabels);

        // Results sharing (only when editing inside a template)
        model.addAttribute("showResultsSharing",  templateId != null && Boolean.TRUE.equals(useResultSharing));
        model.addAttribute("resultsSharing",      resultsSharing);
        model.addAttribute("resultsSharingValues",EvalToolConstants.ITEM_RESULTS_SHARING_VALUES);
        model.addAttribute("resultsSharingLabels",EvalToolConstants.ITEM_RESULTS_SHARING_LABELS_PROPS);

        return inFacebox ? "modify_item :: itemContent" : "modify_item";
    }

    @PostMapping
    public String save(@RequestParam(required = false) Long itemId,
                       @RequestParam(required = false) Long templateItemId,
                       @RequestParam(required = false) Long templateId,
                       @RequestParam String itemClassification,
                       @RequestParam String itemText,
                       @RequestParam(required = false) String sharing,
                       @RequestParam(required = false) Long scaleId,
                       @RequestParam(required = false) String[] options,
                       @RequestParam(required = false) String scaleDisplaySetting,
                       @RequestParam(required = false) String displayRows,
                       @RequestParam(required = false) Boolean usesNA,
                       @RequestParam(required = false) Boolean usesComment,
                       @RequestParam(required = false) Boolean compulsory,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) Boolean itemExpert,
                       @RequestParam(required = false) String expertDescription,
                       @RequestParam(required = false) Long itemGroupId,
                       @RequestParam(required = false) String resultsSharing,
                       HttpServletRequest request,
                       Model model) {

        boolean inFacebox = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        final String currentUserId = commonLogic.getCurrentUserId();
        final boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        final boolean inTemplate = (templateId != null);

        // Pre-build options list outside the transaction (no DB access needed)
        final List<String> optList = new ArrayList<>();
        if (options != null) {
            for (String opt : options) optList.add(opt != null ? opt : "");
        }
        if (optList.isEmpty()) { optList.add(""); optList.add(""); }

        // Results sharing setting (no DB access needed)
        final Boolean useResultSharing = (Boolean) settings.get(EvalSettings.ITEM_USE_RESULTS_SHARING);

        // Wrap all Hibernate operations in a single transaction so loaded entities
        // are not detached when save is called (mirrors RSF BeanFetchBracketer behaviour)
        daoInvoker.invokeTransactionalAccess(() -> {
            // ---- Get or create the EvalItem ----
            EvalItem item;
            if (itemId != null) {
                item = authoringService.getItemById(itemId);
                if (item == null) throw new IllegalArgumentException("Item not found: " + itemId);
            } else if (templateItemId != null) {
                EvalTemplateItem ti0 = authoringService.getTemplateItemById(templateItemId);
                if (ti0 == null) throw new IllegalArgumentException("Template item not found: " + templateItemId);
                item = ti0.getItem();
            } else {
                // New item
                item = new EvalItem(currentUserId, itemText, EvalConstants.SHARING_PRIVATE, itemClassification, Boolean.FALSE);
                item.setCategory(category != null ? category : EvalConstants.ITEM_CATEGORY_COURSE);
            }

            // ---- Basic item fields ----
            boolean itemLocked = item.getLocked() != null && item.getLocked();
            if (!itemLocked) {
                item.setItemText(itemText);
                item.setClassification(itemClassification);

                // Sharing (admin only, in item bank mode)
                if (userAdmin && !inTemplate) {
                    item.setSharing(sharing != null ? sharing : EvalConstants.SHARING_PRIVATE);
                } else if (!inTemplate && item.getId() == null) {
                    item.setSharing(EvalConstants.SHARING_PRIVATE);
                }

                // Expert (admin only, in item bank mode)
                if (userAdmin && !inTemplate) {
                    item.setExpert(Boolean.TRUE.equals(itemExpert));
                    item.setExpertDescription(expertDescription);
                    item.setItemGroupId(itemGroupId != null && itemGroupId > 0 ? itemGroupId : null);
                }

                // Scale for SCALED items
                if (EvalConstants.ITEM_TYPE_SCALED.equals(itemClassification) && scaleId != null) {
                    EvalScale scale = authoringService.getScaleById(scaleId);
                    item.setScale(scale);
                }

                // Adhoc scale for MC/MA items
                if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemClassification) ||
                    EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemClassification)) {
                    EvalScale adhocScale = item.getScale();
                    if (adhocScale == null || adhocScale.getId() == null) {
                        adhocScale = new EvalScale(currentUserId, null, EvalConstants.SCALE_MODE_ADHOC,
                                EvalConstants.SHARING_PRIVATE, Boolean.FALSE);
                        item.setScale(adhocScale);
                    }
                    adhocScale.setOptions(optList);
                }

                // For TEXT and HEADER: clear the scale
                if (EvalConstants.ITEM_TYPE_TEXT.equals(itemClassification) ||
                    EvalConstants.ITEM_TYPE_HEADER.equals(itemClassification)) {
                    item.setScale(null);
                }

                // scaleDisplaySetting must always be set on the item — the service
                // validation checks the item field regardless of whether it is also
                // stored on the templateItem.
                item.setScaleDisplaySetting(scaleDisplaySetting);

                // Remaining display hints only apply when not in a template
                // (in-template display hints are stored on the templateItem below)
                if (!inTemplate) {
                    item.setDisplayRows(displayRows != null ? Integer.valueOf(displayRows) : null);
                    item.setUsesNA(usesNA);
                    item.setUsesComment(usesComment);
                    item.setCompulsory(Boolean.TRUE.equals(compulsory));
                    item.setCategory(category != null ? category : EvalConstants.ITEM_CATEGORY_COURSE);
                }
            }

            // ---- Guardar item ----
            localTemplateLogic.saveItem(item);

            // ---- Gestionar templateItem ----
            if (inTemplate) {
                EvalTemplateItem ti;
                if (templateItemId != null) {
                    ti = authoringService.getTemplateItemById(templateItemId);
                } else {
                    // Nuevo templateItem
                    EvalTemplate template = authoringService.getTemplateById(templateId);
                    ti = localTemplateLogic.newTemplateItem();
                    ti.setTemplate(template);
                    ti.setItem(item);
                }

                // Display hints on the templateItem
                if (!itemLocked) {
                    ti.setScaleDisplaySetting(scaleDisplaySetting);
                    ti.setDisplayRows(displayRows != null ? Integer.valueOf(displayRows) : null);
                    ti.setUsesNA(usesNA);
                    ti.setUsesComment(usesComment);
                    ti.setCompulsory(Boolean.TRUE.equals(compulsory));
                    ti.setCategory(category != null ? category : EvalConstants.ITEM_CATEGORY_COURSE);
                }

                // Results sharing
                if (Boolean.TRUE.equals(useResultSharing) && resultsSharing != null) {
                    ti.setResultsSharing(resultsSharing);
                } else {
                    ti.setResultsSharing(EvalConstants.SHARING_PUBLIC);
                }

                localTemplateLogic.saveTemplateItem(ti);
            }
        });

        if (inFacebox) {
            return "item_saved";
        }
        if (inTemplate) {
            return "redirect:/modify_template_items?templateId=" + templateId;
        }
        return "redirect:/control_items";
    }

    // Helpers

    private static ExpertGroupOption buildOption(Long id, String label) {
        ExpertGroupOption opt = new ExpertGroupOption();
        opt.setId(id);
        opt.setLabel(label);
        return opt;
    }

    private static String[] appendString(String[] arr, String s) {
        String[] result = Arrays.copyOf(arr, arr.length + 1);
        result[arr.length] = s;
        return result;
    }
}