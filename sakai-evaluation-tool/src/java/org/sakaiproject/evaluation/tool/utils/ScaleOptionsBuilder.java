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
package org.sakaiproject.evaluation.tool.utils;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;

import lombok.Data;

/**
 * Builds the scale option DTOs shared by the take and preview views.
 */
public final class ScaleOptionsBuilder {

    private ScaleOptionsBuilder() {
    }

    @Data
    public static class OptionData {
        int index;
        String value;
        String label;
        String matrixLegend;
        String matrixLegendAlign;
        boolean selected;
    }

    @Data
    public static class SteppedRow {
        String label;
        int middleCount;
        String value;
    }

    @Data
    public static class ScaleOptions {
        String displaySetting;
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
    }

    public interface ScaleOptionsTarget {
        void setOptions(List<OptionData> options);

        void setStartLabel(String startLabel);

        void setEndLabel(String endLabel);

        void setStartClass(String startClass);

        void setEndClass(String endClass);

        void setIdealImageUrl(String idealImageUrl);

        void setMatrixLabelStart(String matrixLabelStart);

        void setMatrixLabelEnd(String matrixLabelEnd);

        void setMatrixLabelMiddle(String matrixLabelMiddle);

        void setSteppedRows(List<SteppedRow> steppedRows);
    }

    public interface ItemScaleOptionsTarget extends ScaleOptionsTarget {
        void setScaleDisplaySetting(String scaleDisplaySetting);
    }

    public static ScaleOptions forTemplateItem(EvalTemplateItem templateItem) {
        String displaySetting = templateItem.getScaleDisplaySetting();
        if (displaySetting == null) {
            displaySetting = EvalConstants.ITEM_SCALE_DISPLAY_FULL;
        }
        return forScale(templateItem.getItem().getScale(), displaySetting);
    }

    public static ScaleOptions forChoiceTemplateItem(EvalTemplateItem templateItem, List<Integer> selectedAnswers) {
        String displaySetting = templateItem.getScaleDisplaySetting();
        if (displaySetting == null) {
            displaySetting = EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL;
        }

        ScaleOptions data = new ScaleOptions();
        data.setDisplaySetting(displaySetting);
        data.setOptions(buildChoiceOptions(templateItem.getItem().getScale().getOptions(), selectedAnswers));
        return data;
    }

    public static ScaleOptions forScale(EvalScale scale, String displaySetting) {
        List<String> rawOptions = scale.getOptions();
        int optionCount = rawOptions.size();

        ScaleOptions data = new ScaleOptions();
        data.setDisplaySetting(displaySetting);

        boolean colored = isColored(displaySetting);
        boolean compact = isCompact(displaySetting);
        boolean stepped = isStepped(displaySetting);
        boolean matrix = isMatrix(displaySetting);

        if (colored) {
            data.setIdealImageUrl(resolveContextUrl(EvalToolConstants.COLORED_IMAGE_URLS[ScaledUtils.idealIndex(scale)]));
        }

        if (compact) {
            data.setStartLabel(rawOptions.get(0));
            data.setEndLabel(rawOptions.get(optionCount - 1));
            if (colored) {
                data.setStartClass(ScaledUtils.getStartClass(scale));
                data.setEndClass(ScaledUtils.getEndClass(scale));
            }
            data.setOptions(buildCompactOptions(optionCount));

        } else if (stepped) {
            data.setSteppedRows(buildSteppedRows(rawOptions));
            data.setOptions(buildSteppedOptions(rawOptions));

        } else if (matrix) {
            List<String> headers = RenderingUtils.getMatrixLabels(rawOptions);
            data.setMatrixLabelStart(headers.get(0));
            data.setMatrixLabelEnd(headers.get(1));
            if (headers.size() >= 3) {
                data.setMatrixLabelMiddle(headers.get(2));
            }
            data.setOptions(buildMatrixOptions(rawOptions, headers));

        } else {
            data.setOptions(buildFullOptions(rawOptions));
        }

        return data;
    }

    public static void applyTo(ItemScaleOptionsTarget target, ScaleOptions options) {
        target.setScaleDisplaySetting(options.getDisplaySetting());
        applyRenderOptions(target, options);
    }

    public static void applyRenderOptions(ScaleOptionsTarget target, ScaleOptions options) {
        target.setOptions(options.getOptions());
        target.setStartLabel(options.getStartLabel());
        target.setEndLabel(options.getEndLabel());
        target.setStartClass(options.getStartClass());
        target.setEndClass(options.getEndClass());
        target.setIdealImageUrl(options.getIdealImageUrl());
        target.setMatrixLabelStart(options.getMatrixLabelStart());
        target.setMatrixLabelEnd(options.getMatrixLabelEnd());
        target.setMatrixLabelMiddle(options.getMatrixLabelMiddle());
        target.setSteppedRows(options.getSteppedRows());
    }

    private static List<OptionData> buildChoiceOptions(List<String> rawOptions, List<Integer> selectedAnswers) {
        List<OptionData> options = new ArrayList<>();
        for (int i = 0; i < rawOptions.size(); i++) {
            OptionData option = option(i, String.valueOf(i), rawOptions.get(i));
            option.setSelected(selectedAnswers != null && selectedAnswers.contains(i));
            options.add(option);
        }
        return options;
    }

    private static List<OptionData> buildCompactOptions(int optionCount) {
        List<OptionData> options = new ArrayList<>();
        for (int i = 0; i < optionCount; i++) {
            options.add(option(i, String.valueOf(i), " "));
        }
        return options;
    }

    private static List<SteppedRow> buildSteppedRows(List<String> rawOptions) {
        int optionCount = rawOptions.size();
        List<SteppedRow> rows = new ArrayList<>();
        for (int i = 0; i < optionCount; i++) {
            int reversedIndex = optionCount - 1 - i;
            SteppedRow row = new SteppedRow();
            row.setLabel(rawOptions.get(reversedIndex));
            row.setMiddleCount(i);
            row.setValue(String.valueOf(reversedIndex));
            rows.add(row);
        }
        return rows;
    }

    private static List<OptionData> buildSteppedOptions(List<String> rawOptions) {
        int optionCount = rawOptions.size();
        List<OptionData> options = new ArrayList<>();
        for (int i = 0; i < optionCount; i++) {
            int reversedIndex = optionCount - 1 - i;
            options.add(option(i, String.valueOf(reversedIndex), rawOptions.get(reversedIndex)));
        }
        return options;
    }

    private static List<OptionData> buildMatrixOptions(List<String> rawOptions, List<String> headers) {
        int optionCount = rawOptions.size();
        boolean numericScale = RenderingUtils.isNumericScale(rawOptions);
        int middleIndex = optionCount > 4 ? (optionCount - 1) / 2 : -1;
        List<OptionData> options = new ArrayList<>();

        for (int i = 0; i < optionCount; i++) {
            OptionData option = option(i, String.valueOf(optionCount - 1 - i), String.valueOf(i + 1));
            if (!numericScale) {
                if (i == 0) {
                    option.setMatrixLegend(headers.get(0));
                    option.setMatrixLegendAlign("left");
                } else if (i == optionCount - 1) {
                    option.setMatrixLegend(headers.get(1));
                    option.setMatrixLegendAlign("right");
                } else if (i == middleIndex) {
                    option.setMatrixLegend(headers.get(2));
                    option.setMatrixLegendAlign("center");
                }
            }
            options.add(option);
        }
        return options;
    }

    private static List<OptionData> buildFullOptions(List<String> rawOptions) {
        List<OptionData> options = new ArrayList<>();
        for (int i = 0; i < rawOptions.size(); i++) {
            options.add(option(i, String.valueOf(i), rawOptions.get(i)));
        }
        return options;
    }

    private static OptionData option(int index, String value, String label) {
        OptionData option = new OptionData();
        option.setIndex(index);
        option.setValue(value);
        option.setLabel(label);
        return option;
    }

    private static boolean isColored(String displaySetting) {
        return EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED.equals(displaySetting)
                || EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED.equals(displaySetting)
                || EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(displaySetting)
                || EvalConstants.ITEM_SCALE_DISPLAY_MATRIX_COLORED.equals(displaySetting);
    }

    private static boolean isCompact(String displaySetting) {
        return EvalConstants.ITEM_SCALE_DISPLAY_COMPACT.equals(displaySetting)
                || EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED.equals(displaySetting);
    }

    private static boolean isStepped(String displaySetting) {
        return EvalConstants.ITEM_SCALE_DISPLAY_STEPPED.equals(displaySetting)
                || EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED.equals(displaySetting);
    }

    private static boolean isMatrix(String displaySetting) {
        return EvalConstants.ITEM_SCALE_DISPLAY_MATRIX.equals(displaySetting)
                || EvalConstants.ITEM_SCALE_DISPLAY_MATRIX_COLORED.equals(displaySetting);
    }

    private static String resolveContextUrl(String url) {
        return url.replace("$context", "");
    }
}
