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
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.utils.ScaleOptionsBuilder;
import org.sakaiproject.evaluation.tool.utils.ScaleOptionsBuilder.OptionData;
import org.sakaiproject.evaluation.tool.utils.ScaleOptionsBuilder.SteppedRow;
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

/**
 * Preview view for an item.
 * Spring MVC equivalent of PreviewItemProducer.
 * Devuelve un fragmento HTML para cargarse en facebox.
 */
@Slf4j
@Controller
@RequestMapping("/preview_item")
public class PreviewItemController extends EvalControllerSupport {

    @Data
    public static class ItemPreviewData implements ScaleOptionsBuilder.ItemScaleOptionsTarget {
        String itemType;
        String itemText;
        int displayNumber;

        // TEXT
        int displayRows;
        boolean usesNA;

        // SCALED — single mode
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

        // BLOCK — hijos
        List<ItemPreviewData> childItems;
    }
    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String show(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long templateItemId,
            @RequestParam(required = false) String scaleDisplay,
            @RequestParam(required = false) Boolean na,
            @RequestParam(required = false) Boolean compulsory,
            @RequestParam(required = false) Boolean showComment,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) Integer textLines,
            Model model,
            HttpServletRequest request) {

        EvalTemplateItem templateItem;
        if (templateItemId != null) {
            templateItem = authoringService.getTemplateItemById(templateItemId);
        } else if (itemId != null) {
            EvalItem item = authoringService.getItemById(itemId);
            templateItem = TemplateItemUtils.makeTemplateItem(item);
            if (templateItem.getId() == null) templateItem.setId(item.getId());
        } else {
            throw new IllegalArgumentException("Must provide itemId or templateItemId");
        }

        // Apply parameter overrides (for live preview from modify_item)
        if (scaleDisplay != null) templateItem.setScaleDisplaySetting(scaleDisplay);
        if (na != null)          templateItem.setUsesNA(na);
        if (compulsory != null)  templateItem.setCompulsory(compulsory);
        if (showComment != null) templateItem.setUsesComment(showComment);
        if (text != null)        templateItem.getItem().setItemText(text);
        if (textLines != null)   templateItem.setDisplayRows(textLines);

        Locale locale = RequestContextUtils.getLocale(request);
        ItemPreviewData data = buildPreviewData(templateItem, 1, locale);
        model.addAttribute("item", data);
        return "preview_item :: content";
    }

    private ItemPreviewData buildPreviewData(EvalTemplateItem ti, int displayNumber, Locale locale) {
        ItemPreviewData d = new ItemPreviewData();
        String type = TemplateItemUtils.getTemplateItemType(ti);
        d.setItemType(type);
        d.setItemText(ti.getItem().getItemText());
        d.setDisplayNumber(displayNumber);
        d.setUsesNA(Boolean.TRUE.equals(ti.getUsesNA()));

        if (EvalConstants.ITEM_TYPE_TEXT.equals(type)) {
            d.setDisplayRows(ti.getDisplayRows() != null ? ti.getDisplayRows() : 3);

        } else if (EvalConstants.ITEM_TYPE_SCALED.equals(type)) {
            populateScaleData(d, ti);

        } else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(type)
                || EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(type)) {
            populateChoiceData(d, ti);

        } else if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type)) {
            populateScaleData(d, ti);
            List<ItemPreviewData> children = new ArrayList<>();
            if (ti.childTemplateItems != null) {
                int childNum = 1;
                for (EvalTemplateItem child : ti.childTemplateItems) {
                    children.add(buildPreviewData(child, childNum++, locale));
                }
            }
            d.setChildItems(children);
        }
        // HEADER and ITEM_TYPE_BLOCK_CHILD: itemText alone is sufficient

        return d;
    }

    private void populateScaleData(ItemPreviewData d, EvalTemplateItem ti) {
        ScaleOptionsBuilder.applyTo(d, ScaleOptionsBuilder.forTemplateItem(ti));
    }

    private void populateChoiceData(ItemPreviewData d, EvalTemplateItem ti) {
        ScaleOptionsBuilder.applyTo(d, ScaleOptionsBuilder.forChoiceTemplateItem(ti, null));
        d.setUsesNA(Boolean.TRUE.equals(ti.getUsesNA()));
    }
}
