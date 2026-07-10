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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalScale;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of ControlScalesProducer.
 * Lists the user's scales for editing, deleting or copying.
 */
@Slf4j
@Controller
@RequestMapping("/control_scales")
public class ControlScalesController extends EvalControllerSupport {

    @Data
    public static class OptionEntry {
        String letter;
        String text;
    }

    @Data
    public static class ScaleRow {
        Long              scaleId;
        int               number;
        String            title;
        List<OptionEntry> options;
        String            idealKey;   // i18n key for the ideal point
        boolean           canModify;
        boolean           canDelete;
    }

    @GetMapping
    public String show(Model model) {

        String currentUserId = currentUserId();

        List<EvalScale> scales = authoringService.getScalesForUser(currentUserId, null);
        List<ScaleRow> rows = new ArrayList<>();
        for (int i = 0; i < scales.size(); i++) {
            EvalScale scale = scales.get(i);
            ScaleRow row = new ScaleRow();
            row.setScaleId(scale.getId());
            row.setNumber(i + 1);
            row.setTitle(scale.getTitle());
            List<String> rawOptions = scale.getOptions();
            List<OptionEntry> optionEntries = new ArrayList<>();
            if (rawOptions != null) {
                for (int j = 0; j < rawOptions.size(); j++) {
                    OptionEntry entry = new OptionEntry();
                    entry.setLetter(String.valueOf((char) ('a' + j)));
                    entry.setText(rawOptions.get(j));
                    optionEntries.add(entry);
                }
            }
            row.setOptions(optionEntries);
            row.setIdealKey(resolveIdealKey(scale.getIdeal()));
            row.setCanModify(!scale.getLocked() &&
                    authoringService.canModifyScale(currentUserId, scale.getId()));
            row.setCanDelete(!scale.getLocked() &&
                    authoringService.canRemoveScale(currentUserId, scale.getId()));
            rows.add(row);
        }

        model.addAttribute("scaleRows", rows);
        return "control_scales";
    }

    @PostMapping("/copy")
    public String copyScale(@RequestParam Long scaleId) {
        String currentUserId = currentUserId();
        authoringService.copyScales(new Long[]{scaleId}, null, currentUserId, false);
        return "redirect:/control_scales";
    }

    private String resolveIdealKey(String ideal) {
        if (ideal == null)                                    return "controlscales.ideal.scale.option.label.none";
        if (EvalConstants.SCALE_IDEAL_MID.equals(ideal))     return "controlscales.ideal.scale.option.label.mid";
        if (EvalConstants.SCALE_IDEAL_HIGH.equals(ideal))    return "controlscales.ideal.scale.option.label.high";
        if (EvalConstants.SCALE_IDEAL_LOW.equals(ideal))     return "controlscales.ideal.scale.option.label.low";
        if (EvalConstants.SCALE_IDEAL_OUTSIDE.equals(ideal)) return "controlscales.ideal.scale.option.label.outside";
        return "unknown.caps";
    }
}