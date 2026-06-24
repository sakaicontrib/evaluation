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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;
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
public class PreviewItemController {

    @Data
    public static class OptionData {
        int index;
        String value;
        String label;
        String matrixLegend;
        String matrixLegendAlign;
    }

    @Data
    public static class SteppedRow {
        String label;
        int middleCount;
        String value;
    }

    @Data
    public static class ItemPreviewData {
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

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

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

        if (EvalConstants.ITEM_TYPE_TEXT.equals(type)) {
            d.setDisplayRows(ti.getDisplayRows() != null ? ti.getDisplayRows() : 3);
            d.setUsesNA(Boolean.TRUE.equals(ti.getUsesNA()));

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
        boolean isMatrix  = EvalConstants.ITEM_SCALE_DISPLAY_MATRIX.equals(ds)
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
            boolean numericScale = RenderingUtils.isNumericScale(rawOptions);
            int middleIndex = n > 4 ? (n - 1) / 2 : -1;
            List<OptionData> opts = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                OptionData o = new OptionData();
                o.setIndex(j); o.setValue(String.valueOf(n - 1 - j)); o.setLabel(String.valueOf(j + 1));
                if (!numericScale) {
                    if (j == 0) { o.setMatrixLegend(headers.get(0)); o.setMatrixLegendAlign("left"); }
                    else if (j == n - 1) { o.setMatrixLegend(headers.get(1)); o.setMatrixLegendAlign("right"); }
                    else if (j == middleIndex) { o.setMatrixLegend(headers.get(2)); o.setMatrixLegendAlign("center"); }
                }
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

    private void populateChoiceData(ItemPreviewData d, EvalTemplateItem ti) {
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