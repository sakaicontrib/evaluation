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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.utils.EvalUtils;
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
@RequestMapping("/modify_adhoc_group")
public class ModifyAdhocGroupController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalSettings")
    private EvalSettings settings;

    @Data
    public static class MemberRow {
        private final String userId;
        private final String displayName;
        private final boolean adhocUser; // true = user created via email (no real account)
        private final String displayId;  // only for non-adhoc users
    }

    @GetMapping
    public String show(@RequestParam(required = false) Long adhocGroupId,
                       @RequestParam(required = false, defaultValue = "") String returnUrl,
                       Model model) {
        String currentUserId = commonLogic.getCurrentUserId();
        if (commonLogic.isUserAnonymous(currentUserId))
            throw new SecurityException("Anonymous users cannot access adhoc groups");

        model.addAttribute("adhocGroupId", adhocGroupId);
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("isNew", adhocGroupId == null);
        model.addAttribute("groupTitle", "");

        if (adhocGroupId != null) {
            EvalAdhocGroup group = commonLogic.getAdhocGroupById(adhocGroupId);
            if (group == null) throw new IllegalArgumentException("Adhoc group not found: " + adhocGroupId);
            if (!currentUserId.equals(group.getOwner()))
                throw new SecurityException("Only owners can modify adhocgroups: " + currentUserId);
            model.addAttribute("groupTitle", group.getTitle());

            List<String> participantIds = group.getParticipantIds();
            List<MemberRow> members = new ArrayList<>();
            if (participantIds != null && !participantIds.isEmpty()) {
                for (EvalUser u : commonLogic.getEvalUsersByIds(participantIds)) {
                    boolean isAdhoc = EvalConstants.USER_TYPE_INTERNAL.equals(u.type);
                    members.add(new MemberRow(u.userId,
                            isAdhoc ? u.username : u.displayName,
                            isAdhoc,
                            isAdhoc ? null : u.displayId));
                }
            }
            model.addAttribute("members", members);
        }
        return "modify_adhoc_group";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long adhocGroupId,
                       @RequestParam(defaultValue = "") String groupTitle,
                       @RequestParam(required = false, defaultValue = "") String newMembers,
                       @RequestParam(required = false, defaultValue = "") String returnUrl,
                       RedirectAttributes ra) {
        String currentUserId = commonLogic.getCurrentUserId();
        if (commonLogic.isUserAnonymous(currentUserId))
            throw new SecurityException("Anonymous users cannot create EvalAdhocGroups");

        if (groupTitle.trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "modifyadhocgroup.message.notitle");
            return redirectToForm(adhocGroupId, returnUrl);
        }

        EvalAdhocGroup group;
        if (adhocGroupId == null) {
            group = new EvalAdhocGroup(currentUserId, groupTitle.trim());
        } else {
            group = commonLogic.getAdhocGroupById(adhocGroupId);
            if (!currentUserId.equals(group.getOwner()))
                throw new SecurityException("Only owners can modify adhocgroups: " + currentUserId);
            group.setTitle(groupTitle.trim());
        }

        // Process the list of new members
        List<String> rejected = new ArrayList<>();
        List<String> alreadyIn = new ArrayList<>();
        List<String> toAdd = addMembersFromText(newMembers, group, rejected, alreadyIn);

        Set<String> all = new HashSet<>();
        if (group.getParticipantIds() != null) all.addAll(group.getParticipantIds());
        all.addAll(toAdd);
        group.setParticipantIds(new ArrayList<>(all));

        commonLogic.saveAdhocGroup(group);
        Long savedId = group.getId();

        ra.addFlashAttribute("successMessage", "modifyadhocgroup.message.savednewgroup");
        ra.addFlashAttribute("successArgs", new Object[]{ group.getTitle() });

        boolean useAdhocUsers = Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_ADHOC_USERS));
        if (!rejected.isEmpty()) {
            ra.addFlashAttribute("errorMessage",
                    useAdhocUsers ? "modifyadhocgroup.message.badusers" : "modifyadhocgroup.message.badusers.noadhocusers");
            ra.addFlashAttribute("errorArgs", new Object[]{ String.join(", ", rejected) });
        }
        if (!alreadyIn.isEmpty()) {
            ra.addFlashAttribute("infoMessage", "modifyadhocgroup.message.existingusers");
            ra.addFlashAttribute("infoArgs", new Object[]{ String.join(", ", alreadyIn) });
        }

        log.info("User ({}) saved adhoc group ({})", currentUserId, savedId);
        return "redirect:/modify_adhoc_group?adhocGroupId=" + savedId
                + (returnUrl.isEmpty() ? "" : "&returnUrl=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8));
    }

    @PostMapping("/remove")
    public String removeMember(@RequestParam Long adhocGroupId,
                               @RequestParam String adhocUserId,
                               @RequestParam(required = false, defaultValue = "") String returnUrl,
                               RedirectAttributes ra) {
        String currentUserId = commonLogic.getCurrentUserId();
        EvalAdhocGroup group = commonLogic.getAdhocGroupById(adhocGroupId);
        if (!currentUserId.equals(group.getOwner()))
            throw new SecurityException("Only owners can modify adhocgroups: " + currentUserId);

        List<String> participants = new ArrayList<>();
        for (String id : group.getParticipantIds()) {
            if (!id.equals(adhocUserId)) participants.add(id);
        }
        group.setParticipantIds(participants);
        commonLogic.saveAdhocGroup(group);

        EvalUser removed = commonLogic.getEvalUserById(adhocUserId);
        ra.addFlashAttribute("successMessage", "modifyadhocgroup.message.removeduser");
        ra.addFlashAttribute("successArgs", new Object[]{ removed.displayName });

        return "redirect:/modify_adhoc_group?adhocGroupId=" + adhocGroupId
                + (returnUrl.isEmpty() ? "" : "&returnUrl=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8));
    }

    private String redirectToForm(Long adhocGroupId, String returnUrl) {
        String base = "redirect:/modify_adhoc_group";
        if (adhocGroupId != null) base += "?adhocGroupId=" + adhocGroupId;
        if (!returnUrl.isEmpty()) base += (adhocGroupId != null ? "&" : "?")
                + "returnUrl=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
        return base;
    }

    private List<String> addMembersFromText(String text, EvalAdhocGroup group,
                                             List<String> rejected, List<String> alreadyIn) {
        List<String> toAdd = new ArrayList<>();
        if (text == null || EvalUtils.isBlank(text.trim())) return toAdd;

        boolean useAdhocUsers = Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_ADHOC_USERS));
        List<String> existing = group.getParticipantIds() != null ? group.getParticipantIds() : new ArrayList<>();

        for (String line : text.split("\n")) {
            String entry = line.trim();
            if (entry.isEmpty()) continue;

            String userId = null;
            String displayEntry = entry;

            // 1. Try as direct userId/EID
            String internalId = commonLogic.getUserId(entry);
            if (internalId == null) internalId = entry;
            EvalUser user = commonLogic.getEvalUserById(internalId);
            if (EvalConstants.USER_TYPE_EXTERNAL.equals(user.type) || EvalConstants.USER_TYPE_INTERNAL.equals(user.type)) {
                userId = user.userId;
                displayEntry = user.displayName;
            } else {
                // 2. Try as email of an existing user
                user = commonLogic.getEvalUserByEmail(entry);
                if (EvalConstants.USER_TYPE_EXTERNAL.equals(user.type) || EvalConstants.USER_TYPE_INTERNAL.equals(user.type)) {
                    userId = user.userId;
                    displayEntry = user.displayName;
                } else if (useAdhocUsers && EvalUtils.isValidEmail(entry)) {
                    // 3. Create adhoc user from email address
                    EvalAdhocUser adhocUser = new EvalAdhocUser(commonLogic.getCurrentUserId(), entry);
                    commonLogic.saveAdhocUser(adhocUser);
                    userId = adhocUser.getUserId();
                }
            }

            if (userId == null) {
                rejected.add(displayEntry);
            } else if (existing.contains(userId) || toAdd.contains(userId)) {
                alreadyIn.add(displayEntry);
            } else {
                toAdd.add(userId);
            }
        }
        return toAdd;
    }
}