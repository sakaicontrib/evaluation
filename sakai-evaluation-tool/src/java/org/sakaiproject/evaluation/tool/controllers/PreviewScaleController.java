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
import java.util.Locale;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;
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
 * Preview view for scales in all display modes.
 * Spring MVC equivalent of PreviewScaleProducer.
 */
@Slf4j
@Controller
@RequestMapping("/preview_scale")
public class PreviewScaleController {

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
    public static class ScaleSection {
        String displaySetting;
        String title;
        String questionText;
        // Compact/Full/Vertical/FullColored/Matrix options (normal order)
        List<OptionData> options;
        // Compact
        String startLabel;
        String endLabel;
        String startClass;
        String endClass;
        // Colored variants (Compact, Full, Stepped)
        String idealImageUrl;
        // Matrix
        String matrixLabelStart;
        String matrixLabelEnd;
        String matrixLabelMiddle;
        // Stepped (options in reverse order)
        List<SteppedRow> steppedRows;
    }

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String show(
            @RequestParam(required = false) Long scaleId,
            @RequestParam(required = false) String[] points,
            @RequestParam(required = false) String ideal,
            @RequestParam(required = false) String displaySetting,
            Model model,
            HttpServletRequest request) {

        EvalScale scale;
        if (scaleId != null) {
            scale = authoringService.getScaleById(scaleId);
        } else if (points != null) {
            scale = new EvalScale("admin", "Sample scale",
                    EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PUBLIC, false);
            scale.setId(12345L);
            scale.setIdeal(ideal != null ? ideal : EvalConstants.SCALE_IDEAL_NONE);
            scale.setOptions(Arrays.asList(points));
        } else {
            throw new IllegalArgumentException("Must provide scaleId or points[]");
        }

        Locale locale = RequestContextUtils.getLocale(request);
        List<ScaleSection> sections = new ArrayList<>();

        for (int i = 0; i < EvalToolConstants.SCALE_DISPLAY_SETTING_VALUES.length; i++) {
            String ds = EvalToolConstants.SCALE_DISPLAY_SETTING_VALUES[i];
            if (displaySetting != null && !displaySetting.equals(ds)) continue;

            String labelKey = EvalToolConstants.SCALE_DISPLAY_SETTING_LABELS_PROPS[i];
            String title = messageSource.getMessage(labelKey, null, locale);
            sections.add(buildSection(scale, ds, title));
        }

        model.addAttribute("sections", sections);
        return "preview_scale :: content";
    }

    private ScaleSection buildSection(EvalScale scale, String ds, String title) {
        ScaleSection s = new ScaleSection();
        s.setDisplaySetting(ds);
        s.setTitle(title);
        s.setQuestionText("Sample question text (" + title + ")");

        List<String> rawOptions = scale.getOptions();
        int n = rawOptions.size();

        boolean isCompact   = EvalConstants.ITEM_SCALE_DISPLAY_COMPACT.equals(ds)
                           || EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED.equals(ds);
        boolean isStepped   = EvalConstants.ITEM_SCALE_DISPLAY_STEPPED.equals(ds)
                           || EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(ds);
        boolean isMatrix    = EvalConstants.ITEM_SCALE_DISPLAY_MATRIX.equals(ds)
                           || EvalConstants.ITEM_SCALE_DISPLAY_MATRIX_COLORED.equals(ds);
        boolean isColored   = EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED.equals(ds)
                           || EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED.equals(ds)
                           || EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(ds)
                           || EvalConstants.ITEM_SCALE_DISPLAY_MATRIX_COLORED.equals(ds);

        if (isColored) {
            s.setIdealImageUrl(resolveContextUrl(EvalToolConstants.COLORED_IMAGE_URLS[ScaledUtils.idealIndex(scale)]));
        }

        if (isCompact) {
            s.setStartLabel(rawOptions.get(0));
            s.setEndLabel(rawOptions.get(n - 1));
            if (isColored) {
                s.setStartClass(ScaledUtils.getStartClass(scale));
                s.setEndClass(ScaledUtils.getEndClass(scale));
            }
            List<OptionData> opts = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                OptionData o = new OptionData();
                o.setIndex(j);
                o.setValue(String.valueOf(j));
                o.setLabel(" ");
                opts.add(o);
            }
            s.setOptions(opts);

        } else if (isStepped) {
            List<SteppedRow> rows = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                SteppedRow row = new SteppedRow();
                row.setLabel(rawOptions.get(n - 1 - j));
                row.setMiddleCount(j);
                row.setValue(String.valueOf(n - 1 - j));
                rows.add(row);
            }
            s.setSteppedRows(rows);
            List<OptionData> opts = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                OptionData o = new OptionData();
                o.setIndex(j);
                o.setValue(String.valueOf(n - 1 - j));
                o.setLabel(rawOptions.get(n - 1 - j));
                opts.add(o);
            }
            s.setOptions(opts);

        } else if (isMatrix) {
            List<String> headers = RenderingUtils.getMatrixLabels(rawOptions);
            s.setMatrixLabelStart(headers.get(0));
            s.setMatrixLabelEnd(headers.get(1));
            if (headers.size() >= 3) s.setMatrixLabelMiddle(headers.get(2));
            List<OptionData> opts = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                OptionData o = new OptionData();
                o.setIndex(j);
                o.setValue(String.valueOf(n - 1 - j));
                o.setLabel(String.valueOf(j + 1));
                opts.add(o);
            }
            s.setOptions(opts);

        } else {
            // Full, FullColored, Vertical
            List<OptionData> opts = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                OptionData o = new OptionData();
                o.setIndex(j);
                o.setValue(String.valueOf(j));
                o.setLabel(rawOptions.get(j));
                opts.add(o);
            }
            s.setOptions(opts);
        }

        return s;
    }

    private String resolveContextUrl(String url) {
        return url.replace("$context", "");
    }
}