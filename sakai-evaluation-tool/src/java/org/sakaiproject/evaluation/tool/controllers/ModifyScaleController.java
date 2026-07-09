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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Creation and editing of scales.
 * Spring MVC equivalent of ModifyScaleProducer.
 */
@Controller
@RequestMapping("/modify_scale")
public class ModifyScaleController extends EvalControllerSupport {

    @GetMapping
    public String show(@RequestParam(required = false) Long scaleId, Model model) {
        String currentUserId = currentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        List<String> options;
        String title = "";
        String ideal = EvalToolConstants.NULL; // "none" by default
        String sharing = EvalConstants.SHARING_PRIVATE;
        boolean canRemove = false;

        if (scaleId != null) {
            EvalScale scale = authoringService.getScaleById(scaleId);
            if (scale == null) {
                throw new IllegalArgumentException("Scale not found: " + scaleId);
            }
            title = scale.getTitle() != null ? scale.getTitle() : "";
            options = scale.getOptions() != null ? new ArrayList<>(scale.getOptions())
                                                 : new ArrayList<>(Arrays.asList(EvalToolConstants.DEFAULT_INITIAL_SCALE_VALUES));
            ideal = scale.getIdeal() != null ? scale.getIdeal() : EvalToolConstants.NULL;
            sharing = scale.getSharing() != null ? scale.getSharing() : EvalConstants.SHARING_PRIVATE;
            canRemove = authoringService.canRemoveScale(currentUserId, scaleId);
        } else {
            options = new ArrayList<>(Arrays.asList(EvalToolConstants.DEFAULT_INITIAL_SCALE_VALUES));
        }

        Integer minLength = (Integer) settings.get(EvalSettings.EVAL_MIN_LIST_LENGTH);
        Integer maxLength = (Integer) settings.get(EvalSettings.EVAL_MAX_LIST_LENGTH);
        if (minLength == null) minLength = 2;
        if (maxLength == null) maxLength = 20;

        // Build ideal radio data: value + i18n key pairs
        String[] idealValues = EvalToolConstants.SCALE_IDEA_VALUES;
        String[] idealLabels = EvalToolConstants.SCALE_IDEAL_LABELS;

        model.addAttribute("scaleId", scaleId);
        model.addAttribute("title", title);
        model.addAttribute("options", options);
        model.addAttribute("ideal", ideal);
        model.addAttribute("sharing", sharing);
        model.addAttribute("idealValues", idealValues);
        model.addAttribute("idealLabels", idealLabels);
        model.addAttribute("sharingValues", EvalToolConstants.SHARING_VALUES);
        model.addAttribute("sharingLabels", EvalToolConstants.SHARING_LABELS_PROPS);
        model.addAttribute("userAdmin", userAdmin);
        model.addAttribute("canRemove", canRemove);
        model.addAttribute("minLength", minLength);
        model.addAttribute("maxLength", maxLength);

        return "modify_scale";
    }

    @PostMapping
    public String save(@RequestParam(required = false) Long scaleId,
                       @RequestParam String title,
                       @RequestParam(required = false) String[] options,
                       @RequestParam(required = false) String ideal,
                       @RequestParam(required = false) String sharing,
                       RedirectAttributes redirectAttrs) {

        String currentUserId = currentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        // Build options list (filter empty trailing entries)
        List<String> optionList = new ArrayList<>();
        if (options != null) {
            for (String opt : options) {
                optionList.add(opt != null ? opt : "");
            }
        }
        if (optionList.isEmpty()) {
            optionList.add("");
            optionList.add("");
        }

        // Resolve ideal: "*NULL*" → null
        final String idealValue = (ideal == null || EvalToolConstants.NULL.equals(ideal)) ? null : ideal;
        final String currentUserId2 = currentUserId;
        final boolean userAdminValue = userAdmin;
        final String sharingParam = sharing;
        final String titleParam = title;
        final List<String> optionListFinal = optionList;

        daoInvoker.invokeTransactionalAccess(() -> {
            EvalScale scale;
            if (scaleId != null) {
                scale = authoringService.getScaleById(scaleId);
                if (scale == null) {
                    throw new IllegalArgumentException("Scale not found: " + scaleId);
                }
            } else {
                scale = new EvalScale(currentUserId2, titleParam, EvalConstants.SCALE_MODE_SCALE,
                        EvalConstants.SHARING_PRIVATE, Boolean.FALSE);
            }

            scale.setTitle(titleParam);
            scale.setOptions(optionListFinal);
            scale.setIdeal(idealValue);
            if (userAdminValue && sharingParam != null) {
                scale.setSharing(sharingParam);
            }

            authoringService.saveScale(scale, currentUserId2);
        });

        return "redirect:/control_scales";
    }
}