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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.springframework.context.MessageSource;
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

    private static final String DEFAULTS  = "defaults";
    private static final String OTHERS    = "others";
    private static final int    PAGE_SIZE = 25;
    private static final int    PREVIEW_MAX_CHARS = 200;

    @Resource(name = "messageSource")
    private MessageSource messageSource;

    @Data
    public static class EmailTemplateRow {
        Long    templateId;
        int     number;
        String  defaultType;
        String  typeLabel;    // localized display label for the template's type
        String  subject;
        String  messageHtml;      // preview, truncated, line breaks already converted to &lt;br/>
        String  messageFullHtml;  // untruncated, for the "Preview" modal
        String  type;
        boolean canControl;
        boolean canRemove;    // only if it is not a default template and the user can manage it
        Long    evaluationId;      // first evaluation using this template, if any (personal templates only)
        String  evaluationTitle;
        int     extraEvaluationsCount; // more evaluations beyond the first one, if any (rare)
    }

    @GetMapping
    public String show(@RequestParam(defaultValue = DEFAULTS) String switcher,
                        @RequestParam(defaultValue = "0") int page,
                        Locale locale,
                        Model model) {

        String currentUserId = currentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        boolean showDefaults = false;
        if (userAdmin) {
            showDefaults = DEFAULTS.equals(switcher);
        }

        int startResult = page * PAGE_SIZE;
        List<EvalEmailTemplate> templatesList = evaluationService.getEmailTemplatesForUser(
                currentUserId, null, showDefaults, startResult, PAGE_SIZE);
        int totalCount = evaluationService.countEmailTemplatesForUser(currentUserId, null, showDefaults);

        // Batch-resolve which evaluation uses each personal template, grouped by type so this is at
        // most 3 extra queries per page (one per email type present), never one per row.
        Map<String, List<Long>> idsByType = new HashMap<>();
        for (EvalEmailTemplate et : templatesList) {
            if (et.getDefaultType() == null) {
                idsByType.computeIfAbsent(et.getType(), k -> new ArrayList<>()).add(et.getId());
            }
        }
        Map<Long, List<EvalEvaluation>> evaluationsByTemplateId = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : idsByType.entrySet()) {
            evaluationsByTemplateId.putAll(
                    evaluationService.getEvaluationsUsingEmailTemplates(entry.getValue(), entry.getKey()));
        }

        List<EmailTemplateRow> rows = new ArrayList<>();
        for (int i = 0; i < templatesList.size(); i++) {
            EvalEmailTemplate et = templatesList.get(i);
            // non-admins never see default (system) templates in this list
            if (!userAdmin && et.getDefaultType() != null) continue;
            EmailTemplateRow row = new EmailTemplateRow();
            row.setTemplateId(et.getId());
            row.setNumber(startResult + i + 1);
            row.setDefaultType(et.getDefaultType());
            row.setTypeLabel(resolveTypeLabel(et.getType(), locale));
            row.setSubject(et.getSubject());
            row.setMessageHtml(previewHtml(et.getMessage()));
            row.setMessageFullHtml(escapeHtml(et.getMessage()));
            row.setType(et.getType());
            boolean canControl = evaluationService.canControlEmailTemplate(currentUserId, null, et.getId());
            row.setCanControl(canControl);
            row.setCanRemove(canControl && et.getDefaultType() == null);

            List<EvalEvaluation> evals = evaluationsByTemplateId.get(et.getId());
            if (evals != null && !evals.isEmpty()) {
                row.setEvaluationId(evals.get(0).getId());
                row.setEvaluationTitle(evals.get(0).getTitle());
                row.setExtraEvaluationsCount(evals.size() - 1);
            }

            rows.add(row);
        }

        model.addAttribute("emailTemplateRows", rows);
        model.addAttribute("userAdmin", userAdmin);
        model.addAttribute("switcher", switcher);
        model.addAttribute("DEFAULTS", DEFAULTS);
        model.addAttribute("OTHERS", OTHERS);

        model.addAttribute("page", page);
        if (totalCount > PAGE_SIZE) {
            int actualStart = totalCount == 0 ? 0 : startResult + 1;
            int actualEnd = Math.min(startResult + PAGE_SIZE, totalCount);
            model.addAttribute("pagerMsg", new Object[]{ actualStart, actualEnd, totalCount });
            model.addAttribute("hasPrev", page > 0);
            model.addAttribute("hasNext", totalCount > startResult + PAGE_SIZE);
            model.addAttribute("prevPage", page - 1);
            model.addAttribute("nextPage", page + 1);
        }

        return "control_email_templates";
    }

    /**
     * Localizes an EvalConstants.EMAIL_TEMPLATE_* type value (e.g. "Available") into a
     * human-readable label. The message key is built from the type value itself (spaces
     * stripped, e.g. "Available Evaluatee" -> "controlemailtemplates.type.AvailableEvaluatee")
     * since type and defaultType share the same raw values for every seeded default template.
     */
    private String resolveTypeLabel(String type, Locale locale) {
        String key = "controlemailtemplates.type." + (type != null ? type.replace(" ", "") : "");
        return messageSource.getMessage(key, null, type, locale);
    }

    /**
     * HTML-escapes and truncates the message body for the list preview - the full text is only
     * shown in the "Preview" modal; showing the whole email inline made the table unreadable.
     */
    private String previewHtml(String message) {
        String msg = message != null ? message : "";
        boolean truncated = msg.length() > PREVIEW_MAX_CHARS;
        if (truncated) {
            msg = msg.substring(0, PREVIEW_MAX_CHARS);
        }
        return truncated ? escapeHtml(msg) + "&hellip;" : escapeHtml(msg);
    }

    private String escapeHtml(String message) {
        String msg = message != null ? message : "";
        return msg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\r\n", "<br/>").replace("\n", "<br/>");
    }
}