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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAdmin;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/control_eval_admin")
public class ControlEvalAdminController extends EvalControllerSupport {

    @Data
    public static class AdminRow {
        private final String userId;
        private final String displayName;
        private final String username;
        private final String assignorUsername;
        private final String assignDateStr;
        private final boolean currentUser;
    }

    @GetMapping
    public String show(Model model) {
        String currentUserId = currentUserId();
        if (!commonLogic.isUserAdmin(currentUserId))
            throw new SecurityException("Users who are not assigned as eval admins may not access this page");

        List<EvalAdmin> adminList = commonLogic.getEvalAdmins();

        // Build userId → EvalUser maps for admins and their assignors
        List<String> adminIds = adminList.stream().map(EvalAdmin::getUserId).collect(Collectors.toList());
        List<String> assignorIds = adminList.stream().map(EvalAdmin::getAssignorUserId).collect(Collectors.toList());

        Map<String, EvalUser> userMap = toMap(commonLogic.getEvalUsersByIds(adminIds));
        Map<String, EvalUser> assignorMap = toMap(commonLogic.getEvalUsersByIds(assignorIds));

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        List<AdminRow> rows = new ArrayList<>();
        for (EvalAdmin admin : adminList) {
            EvalUser user = userMap.getOrDefault(admin.getUserId(), new EvalUser(admin.getUserId(), EvalUser.USER_TYPE_UNKNOWN, null));
            EvalUser assignor = assignorMap.getOrDefault(admin.getAssignorUserId(), new EvalUser(admin.getAssignorUserId(), EvalUser.USER_TYPE_UNKNOWN, null));
            String dateStr = admin.getAssignDate() != null ? df.format(admin.getAssignDate()) : "";
            rows.add(new AdminRow(
                    admin.getUserId(),
                    user.displayName != null ? user.displayName : admin.getUserId(),
                    user.username != null ? user.username : admin.getUserId(),
                    assignor.username != null ? assignor.username : admin.getAssignorUserId(),
                    dateStr,
                    currentUserId.equals(admin.getUserId())
            ));
        }
        model.addAttribute("adminRows", rows);

        boolean sakaiAdminAccessEnabled = Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_SAKAI_ADMIN_ACCESS));
        model.addAttribute("sakaiAdminAccessEnabled", sakaiAdminAccessEnabled);
        model.addAttribute("isEvalAdmin", commonLogic.isUserEvalAdmin(currentUserId));

        if (sakaiAdminAccessEnabled) {
            List<EvalUser> sakaiAdmins = commonLogic.getSakaiAdmins();
            model.addAttribute("sakaiAdmins", sakaiAdmins);
        }

        return "control_eval_admin";
    }

    @PostMapping("/assign")
    public String assign(@RequestParam String userEid, RedirectAttributes ra) {
        String currentUserId = currentUserId();
        if (!commonLogic.isUserAdmin(currentUserId))
            throw new SecurityException("Users who are not eval admins cannot assign other users as eval admins");

        String assigneeUserId = commonLogic.getUserId(userEid);
        if (assigneeUserId == null) {
            ra.addFlashAttribute("errorMessage", "controlevaladmin.message.error.not.found");
            ra.addFlashAttribute("errorArgs", new Object[]{ userEid });
        } else {
            try {
                commonLogic.assignEvalAdmin(assigneeUserId, currentUserId);
                ra.addFlashAttribute("successMessage", "controlevaladmin.message.success.assigning");
                ra.addFlashAttribute("successArgs", new Object[]{ userEid });
                log.info("Admin ({}) assigned ({}) as eval admin", currentUserId, userEid);
            } catch (IllegalArgumentException ex) {
                ra.addFlashAttribute("errorMessage", "controlevaladmin.message.error.already.assigned");
                ra.addFlashAttribute("errorArgs", new Object[]{ userEid });
                log.error(ex.getLocalizedMessage(), ex);
            }
        }
        return "redirect:/control_eval_admin";
    }

    @PostMapping("/unassign")
    public String unassign(@RequestParam String userId, RedirectAttributes ra) {
        String currentUserId = currentUserId();
        if (!commonLogic.isUserAdmin(currentUserId))
            throw new SecurityException("Users who are not eval admins cannot unassign other users as eval admins");

        String userEid = commonLogic.getUserUsername(userId);
        try {
            commonLogic.unassignEvalAdmin(userId);
            ra.addFlashAttribute("successMessage", "controlevaladmin.message.success.unassigning");
            ra.addFlashAttribute("successArgs", new Object[]{ userEid });
            log.info("Admin ({}) unassigned ({}) as eval admin", currentUserId, userEid);
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", "controlevaladmin.message.error.unassigning");
            ra.addFlashAttribute("errorArgs", new Object[]{ userEid });
            log.error(ex.getLocalizedMessage(), ex);
        }
        return "redirect:/control_eval_admin";
    }

    @PostMapping("/toggle")
    public String toggle(RedirectAttributes ra) {
        String currentUserId = currentUserId();
        if (!commonLogic.isUserEvalAdmin(currentUserId))
            throw new SecurityException("Only eval admins can enable/disable sakai admin access");

        boolean current = Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_SAKAI_ADMIN_ACCESS));
        settings.set(EvalSettings.ENABLE_SAKAI_ADMIN_ACCESS, !current);
        if (current) {
            ra.addFlashAttribute("successMessage", "controlevaladmin.message.sakai.admin.access.disabled");
            log.info("Sakai admin access to evaluation system is disabled by {}", currentUserId);
        } else {
            ra.addFlashAttribute("successMessage", "controlevaladmin.message.sakai.admin.access.enabled");
            log.info("Sakai admin access to evaluation system is enabled by {}", currentUserId);
        }
        return "redirect:/control_eval_admin";
    }

    private Map<String, EvalUser> toMap(List<EvalUser> users) {
        Map<String, EvalUser> map = new HashMap<>();
        for (EvalUser u : users) map.put(u.userId, u);
        return map;
    }
}