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

import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of ControlEmailTemplatesProducer.
 * Lists the user's email templates.
 */
@Slf4j
@Controller
@RequestMapping("/control_email_templates")
public class ControlEmailTemplatesController extends EvalControllerSupport {

    private static final String DEFAULTS = "defaults";
    private static final String OTHERS   = "others";

    @Data
    public static class EmailTemplateRow {
        Long    templateId;
        int     number;
        String  defaultType;
        String  subject;
        String  messageHtml;  // line breaks already converted to &lt;br/>
        String  type;
        boolean canControl;
        boolean canRemove;   // only if it is not a default template and the user can manage it
    }

    @GetMapping
    public String show(@RequestParam(defaultValue = DEFAULTS) String switcher, Model model) {

        String currentUserId = currentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        boolean showDefaults = false;
        if (userAdmin) {
            showDefaults = DEFAULTS.equals(switcher);
        }

        List<EvalEmailTemplate> templatesList = evaluationService.getEmailTemplatesForUser(currentUserId, null, showDefaults);
        List<EmailTemplateRow> rows = new ArrayList<>();
        for (int i = 0; i < templatesList.size(); i++) {
            EvalEmailTemplate et = templatesList.get(i);
            // non-admins never see default (system) templates in this list
            if (!userAdmin && et.getDefaultType() != null) continue;
            EmailTemplateRow row = new EmailTemplateRow();
            row.setTemplateId(et.getId());
            row.setNumber(i + 1);
            row.setDefaultType(et.getDefaultType());
            row.setSubject(et.getSubject());
            String msg = et.getMessage() != null ? et.getMessage() : "";
            row.setMessageHtml(msg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\r\n", "<br/>").replace("\n", "<br/>"));
            row.setType(et.getType());
            boolean canControl = evaluationService.canControlEmailTemplate(currentUserId, null, et.getId());
            row.setCanControl(canControl);
            row.setCanRemove(canControl && et.getDefaultType() == null);
            rows.add(row);
        }

        model.addAttribute("emailTemplateRows", rows);
        model.addAttribute("userAdmin", userAdmin);
        model.addAttribute("switcher", switcher);
        model.addAttribute("DEFAULTS", DEFAULTS);
        model.addAttribute("OTHERS", OTHERS);
        return "control_email_templates";
    }
}