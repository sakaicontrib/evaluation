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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of ControlTemplatesProducer.
 * Lists the user's templates for adding, editing or deleting.
 */
@Slf4j
@Controller
@RequestMapping("/control_templates")
public class ControlTemplatesController {

    @Data
    public static class TemplateRow {
        Long    templateId;
        String  title;
        String  ownerName;
        String  lastModified;
        String  lastModifiedSort; // epoch ms for tablesorter
        boolean canModify;
        boolean canDelete;
        boolean canChown;
    }

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @GetMapping
    public String show(Locale locale, Model model) {

        String currentUserId = commonLogic.getCurrentUserId();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        List<EvalTemplate> templates = authoringService.getTemplatesForUser(currentUserId, null, true);
        List<TemplateRow> rows = new ArrayList<>();
        for (EvalTemplate template : templates) {
            TemplateRow row = new TemplateRow();
            row.setTemplateId(template.getId());
            row.setTitle(template.getTitle());
            row.setOwnerName(commonLogic.getEvalUserById(template.getOwner()).displayName);
            row.setLastModified(template.getLastModified() != null ? df.format(template.getLastModified()) : "");
            row.setLastModifiedSort(template.getLastModified() != null ? String.valueOf(template.getLastModified().getTime()) : "0");
            boolean controllable = !template.getLocked() &&
                    authoringService.canModifyTemplate(currentUserId, template.getId());
            row.setCanModify(controllable);
            row.setCanDelete(!template.getLocked() &&
                    authoringService.canRemoveTemplate(currentUserId, template.getId()));
            row.setCanChown(controllable);
            rows.add(row);
        }

        model.addAttribute("templateRows", rows);
        return "control_templates";
    }

    @PostMapping("/copy")
    public String copyTemplate(@RequestParam Long templateId) {
        String currentUserId = commonLogic.getCurrentUserId();
        authoringService.copyTemplate(templateId, null, currentUserId, false, true);
        return "redirect:/control_templates";
    }
}