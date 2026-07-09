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

import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Wizard step 1: choose expert item category. Equivalent of ExpertCategoryProducer.
 */
@Slf4j
@Controller
@RequestMapping("/choose_expert_category")
public class ChooseExpertCategoryController extends EvalControllerSupport {

    @Data
    public static class CategoryRow {
        private final Long id;
        private final String title;
        private final String description;
        private final boolean odd;
    }

    @GetMapping
    public String show(@RequestParam Long templateId, Model model) {
        String currentUserId = currentUserId();
        // non-empty categories only (includeEmpty=false, includeExpert=true)
        List<EvalItemGroup> categories = authoringService.getItemGroups(null, currentUserId, false, true);

        List<CategoryRow> rows = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            EvalItemGroup cat = categories.get(i);
            rows.add(new CategoryRow(cat.getId(), cat.getTitle(), cat.getDescription(), i % 2 != 0));
        }

        model.addAttribute("templateId", templateId);
        model.addAttribute("categories", rows);
        return "choose_expert_category";
    }
}