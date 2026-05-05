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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvalDaoInvoker;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Creation and editing of a template title/description.
 * Spring MVC equivalent of ModifyTemplateProducer.
 *
 * Modes:
 * - GET without templateId → new template (full page)
 * - GET with templateId → edit template (can be full-page or facebox)
 * - POST → saves and redirects to /modify_template_items?templateId=X
 *           if AJAX (facebox) returns a minimal success response
 */
@Controller
@RequestMapping("/modify_template")
public class ModifyTemplateController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalSettings")
    private EvalSettings settings;

    @Resource(name = "localTemplateLogic")
    private LocalTemplateLogic localTemplateLogic;

    @Resource(name = "org.sakaiproject.evaluation.dao.EvalDaoInvoker")
    private EvalDaoInvoker daoInvoker;

    @GetMapping
    public String show(@RequestParam(required = false) Long templateId,
                       Model model,
                       HttpServletRequest request) {

        boolean inFacebox = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        String title = "";
        String description = "";
        String currentSharing = EvalConstants.SHARING_PRIVATE;

        if (templateId != null) {
            EvalTemplate template = localTemplateLogic.fetchTemplate(templateId);
            title = template.getTitle() != null ? template.getTitle() : "";
            description = template.getDescription() != null ? template.getDescription() : "";
            currentSharing = template.getSharing() != null ? template.getSharing() : EvalConstants.SHARING_PRIVATE;
        }

        populateSharingModel(model, currentSharing);

        model.addAttribute("templateId", templateId);
        model.addAttribute("title", title);
        model.addAttribute("description", description);
        model.addAttribute("inFacebox", inFacebox);

        return inFacebox ? "modify_template :: basicForm" : "modify_template";
    }

    @PostMapping
    public String save(@RequestParam(required = false) Long templateId,
                       @RequestParam(defaultValue = "") String title,
                       @RequestParam(defaultValue = "") String description,
                       @RequestParam(required = false) String sharing,
                       @RequestParam(required = false) String fixedSharing,
                       HttpServletRequest request) {

        boolean inFacebox = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        final String effectiveTitle = title.trim();
        final String effectiveDesc = description;
        final boolean userAdmin = commonLogic.isUserAdmin(commonLogic.getCurrentUserId());
        // sharing param comes from select (admin); fixedSharing from hidden input (non-admin/fixed)
        final String sharingValue = resolveSharingToSave(
                sharing != null ? sharing : fixedSharing, userAdmin);

        final Long[] savedId = new Long[1];

        daoInvoker.invokeTransactionalAccess(() -> {
            EvalTemplate template;
            if (templateId != null) {
                template = localTemplateLogic.fetchTemplate(templateId);
            } else {
                template = localTemplateLogic.newTemplate();
            }
            if (!effectiveTitle.isEmpty()) {
                template.setTitle(effectiveTitle);
            }
            template.setDescription(effectiveDesc);
            template.setSharing(sharingValue);
            localTemplateLogic.saveTemplate(template);
            savedId[0] = template.getId();
        });

        if (inFacebox) {
            return "modify_template :: savedOk";
        }
        return "redirect:/modify_template_items?templateId=" + savedId[0];
    }

    private void populateSharingModel(Model model, String currentSharing) {
        String sharingValue = (String) settings.get(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY);
        boolean userAdmin = commonLogic.isUserAdmin(commonLogic.getCurrentUserId());

        boolean showSharingDropdown = false;
        String sharingLabelKey = null;

        if (EvalConstants.SHARING_OWNER.equals(sharingValue)) {
            if (userAdmin) {
                showSharingDropdown = true;
            } else {
                sharingLabelKey = "sharing.private";
            }
        } else if (EvalConstants.SHARING_PRIVATE.equals(sharingValue)) {
            sharingLabelKey = "sharing.private";
        } else if (EvalConstants.SHARING_PUBLIC.equals(sharingValue)) {
            sharingLabelKey = "sharing.public";
        }

        model.addAttribute("showSharingDropdown", showSharingDropdown);
        model.addAttribute("sharingLabelKey", sharingLabelKey);
        model.addAttribute("currentSharing", currentSharing);
        model.addAttribute("sharingValues", EvalToolConstants.SHARING_VALUES);
        model.addAttribute("sharingLabels", EvalToolConstants.SHARING_LABELS_PROPS);
        model.addAttribute("fixedSharingValue", showSharingDropdown ? null : sharingValue);
    }

    private String resolveSharingToSave(String sharingParam, boolean userAdmin) {
        String sharingValue = (String) settings.get(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY);
        if (EvalConstants.SHARING_OWNER.equals(sharingValue) && userAdmin && sharingParam != null) {
            return sharingParam;
        }
        return sharingValue != null ? sharingValue : EvalConstants.SHARING_PRIVATE;
    }
}