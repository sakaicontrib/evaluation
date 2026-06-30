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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor that adds to all Thymeleaf views:
 * - skinRepo / skinDefault: CSS paths for the Sakai skin
 * - mainFrameId: iframe ID for setMainFrameHeight()
 * - navItems: main navigation menu items
 */
public class SakaiSkinInterceptor implements HandlerInterceptor {

    @Resource(name = "org.sakaiproject.component.api.ServerConfigurationService")
    private ServerConfigurationService serverConfigurationService;

    @Resource(name = "org.sakaiproject.tool.api.ToolManager")
    private ToolManager toolManager;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    private EvalAuthoringService authoringService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationService")
    private EvalEvaluationService evaluationService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalSettings")
    private EvalSettings evalSettings;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {

        if (modelAndView == null || modelAndView.getViewName() == null
                || modelAndView.getViewName().startsWith("redirect:")) {
            return;
        }

        // Skin CSS paths
        String skinRepo    = serverConfigurationService.getString("skin.repo", "/library/skin");
        String skinDefault = serverConfigurationService.getString("skin.default", "default-skin");
        modelAndView.addObject("skinRepo",    skinRepo);
        modelAndView.addObject("skinDefault", skinDefault);

        // iframe ID for setMainFrameHeight()
        String mainFrameId = "";
        try {
            Placement placement = toolManager.getCurrentPlacement();
            if (placement != null) {
                mainFrameId = "Main" + placement.getId().replace("-", "x");
            }
        } catch (Exception e) {
            // outside of a tool context (direct access)
        }
        modelAndView.addObject("mainFrameId", mainFrameId);

        // Sakai head scripts (required for CKEditor and other widgets)
        modelAndView.addObject("sakaiHtmlHead", (String) request.getAttribute("sakai.html.head"));

        // Navigation menu
        String currentPath = request.getServletPath(); // e.g. "/summary"
        modelAndView.addObject("navItems", buildNavItems(currentPath));
    }

    private List<NavItem> buildNavItems(String currentPath) {
        List<NavItem> items = new ArrayList<>();
        String currentUserId = commonLogic.getCurrentUserId();
        boolean isAdmin      = commonLogic.isUserAdmin(currentUserId);
        boolean canCreate    = authoringService.canCreateTemplate(currentUserId);
        boolean canBegin     = evaluationService.canBeginEvaluation(currentUserId);
        boolean hideItems    = (Boolean) evalSettings.get(EvalSettings.DISABLE_ITEM_BANK);
        boolean showToplinks = (Boolean) evalSettings.get(EvalSettings.ENABLE_MY_TOPLINKS);

        if (isAdmin) {
            items.add(new NavItem("/administrate", "administrate.page.title", currentPath.startsWith("/administrate")));
        }
        items.add(new NavItem("/summary", "summary.page.title", "/summary".equals(currentPath)));

        if (showToplinks || isAdmin) {
            if (isAdmin || canBegin) {
                items.add(new NavItem("/control_evaluations", "controlevaluations.page.title", currentPath.startsWith("/control_evaluations")));
            }
            if (isAdmin || canCreate) {
                items.add(new NavItem("/control_templates", "controltemplates.page.title", currentPath.startsWith("/control_templates")));
                if (isAdmin || !hideItems) {
                    items.add(new NavItem("/control_items", "controlitems.page.title", currentPath.startsWith("/control_items")));
                }
                items.add(new NavItem("/control_scales", "controlscales.page.title", currentPath.startsWith("/control_scales")));
            }
            if (isAdmin || canBegin) {
                items.add(new NavItem("/control_email_templates", "controlemailtemplates.page.title", currentPath.startsWith("/control_email_templates")));
            }
            if (Boolean.TRUE.equals(evalSettings.get(EvalSettings.ENABLE_ADHOC_GROUPS)) && (isAdmin || canBegin)) {
                items.add(new NavItem("/control_adhoc_groups", "controladhocgroups.page.title", currentPath.startsWith("/control_adhoc_groups")));
            }
        }
        return items;
    }

    public static class NavItem {
        public final String  url;
        public final String  messageKey;
        public final boolean active;
        public NavItem(String url, String messageKey, boolean active) {
            this.url = url; this.messageKey = messageKey; this.active = active;
        }
    }
}
