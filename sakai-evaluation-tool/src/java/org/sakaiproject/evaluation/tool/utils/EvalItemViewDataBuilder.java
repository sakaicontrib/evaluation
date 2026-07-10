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
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Builds shared item view data for take and preview controllers.
 */
public final class EvalItemViewDataBuilder {

    private EvalItemViewDataBuilder() {
    }

    public static EvalItemViewData build(EvalTemplateItem ti, int displayNumber) {
        EvalItemViewData d = new EvalItemViewData();
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
            populateChoiceData(d, ti, null);
        } else if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type)) {
            populateScaleData(d, ti);
        }
        return d;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TakeEvalItemData extends EvalItemViewData {
        int answerIndex;
        Long templateItemId;
        Long existingAnswerId;
        String associatedId;
        String associatedType;
        Integer currentNumeric;
        String currentText;
        boolean currentNA;
        String currentComment;
        List<Integer> currentMultipleAnswers;

        public String getInputKey() {
            return associatedId != null
                    ? templateItemId + "_" + associatedId.replaceAll("[^a-zA-Z0-9]", "_")
                    : String.valueOf(templateItemId);
        }
    }

    public static <T extends TakeEvalItemData> T buildForTake(EvalTemplateItem ti, int displayNumber,
            String associatedId, String associatedType, int answerIndex, EvalAnswer existing, T target) {
        EvalItemViewData base = build(ti, displayNumber);
        copyDisplayFields(base, target);
        target.setAnswerIndex(answerIndex);
        target.setTemplateItemId(ti.getId());
        target.setAssociatedId(associatedId);
        target.setAssociatedType(associatedType);
        if (existing != null) {
            target.setExistingAnswerId(existing.getId());
            target.setCurrentNumeric(existing.getNumeric());
            target.setCurrentText(existing.getText());
            target.setCurrentNA(EvalUtils.decodeAnswerNA(existing));
            target.setCurrentComment(existing.getComment());
            if (existing.multipleAnswers != null) {
                List<Integer> maList = new ArrayList<>();
                for (Integer v : existing.multipleAnswers) {
                    if (v != null) {
                        maList.add(v);
                    }
                }
                target.setCurrentMultipleAnswers(maList);
            }
            if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(target.getItemType())
                    || EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(target.getItemType())) {
                populateChoiceData(target, ti, target.getCurrentMultipleAnswers());
            }
        }
        return target;
    }

    public static void populateScaleData(EvalItemViewData d, EvalTemplateItem ti) {
        ScaleOptionsBuilder.applyTo(d, ScaleOptionsBuilder.forTemplateItem(ti));
    }

    public static void populateChoiceData(EvalItemViewData d, EvalTemplateItem ti, List<Integer> currentMultipleAnswers) {
        ScaleOptionsBuilder.applyTo(d, ScaleOptionsBuilder.forChoiceTemplateItem(ti, currentMultipleAnswers));
        d.setUsesNA(Boolean.TRUE.equals(ti.getUsesNA()));
    }

    private static void copyDisplayFields(EvalItemViewData from, EvalItemViewData to) {
        to.setItemType(from.getItemType());
        to.setItemText(from.getItemText());
        to.setDisplayNumber(from.getDisplayNumber());
        to.setDisplayRows(from.getDisplayRows());
        to.setUsesNA(from.isUsesNA());
        to.setUsesComment(from.isUsesComment());
        to.setCompulsory(from.isCompulsory());
        to.setScaleDisplaySetting(from.getScaleDisplaySetting());
        to.setOptions(from.getOptions());
        to.setStartLabel(from.getStartLabel());
        to.setEndLabel(from.getEndLabel());
        to.setStartClass(from.getStartClass());
        to.setEndClass(from.getEndClass());
        to.setIdealImageUrl(from.getIdealImageUrl());
        to.setMatrixLabelStart(from.getMatrixLabelStart());
        to.setMatrixLabelEnd(from.getMatrixLabelEnd());
        to.setMatrixLabelMiddle(from.getMatrixLabelMiddle());
        to.setSteppedRows(from.getSteppedRows());
        to.setChildItems(from.getChildItems());
        to.setOdd(from.isOdd());
    }
}
