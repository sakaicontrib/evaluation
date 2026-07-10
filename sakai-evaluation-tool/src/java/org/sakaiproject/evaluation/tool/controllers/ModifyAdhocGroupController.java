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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
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
public class ModifyAdhocGroupController extends EvalControllerSupport {

    private static class InvalidMembersException extends RuntimeException {
    }

    private static class LockedGroupException extends RuntimeException {
    }

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
        String currentUserId = currentUserId();
        if (commonLogic.isUserAnonymous(currentUserId))
            throw new SecurityException("Anonymous users cannot access adhoc groups");

        // Flash attributes set by POST on validation error to pre-fill the form
        String prefillTitle = (String) model.asMap().get("prefillTitle");
        String prefillMembers = (String) model.asMap().getOrDefault("prefillMembers", "");

        model.addAttribute("adhocGroupId", adhocGroupId);
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("isNew", adhocGroupId == null);
        model.addAttribute("prefillMembers", prefillMembers);

        if (adhocGroupId != null) {
            EvalAdhocGroup group = commonLogic.getAdhocGroupById(adhocGroupId);
            if (group == null) throw new IllegalArgumentException("Adhoc group not found: " + adhocGroupId);
            if (!currentUserId.equals(group.getOwner()))
                throw new SecurityException("Only owners can modify adhocgroups: " + currentUserId);
            if (isGroupLocked(group)) {
                model.addAttribute("errorMessage", "controladhocgroups.group.locked.tooltip");
                model.addAttribute("readOnly", true);
            }
            model.addAttribute("groupTitle", prefillTitle != null ? prefillTitle : group.getTitle());

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
        } else {
            model.addAttribute("groupTitle", prefillTitle != null ? prefillTitle : "");
        }
        return "modify_adhoc_group";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Long adhocGroupId,
                       @RequestParam(defaultValue = "") String groupTitle,
                       @RequestParam(required = false, defaultValue = "") String newMembers,
                       @RequestParam(required = false, defaultValue = "") String returnUrl,
                       RedirectAttributes ra) {
        String currentUserId = currentUserId();
        if (commonLogic.isUserAnonymous(currentUserId))
            throw new SecurityException("Anonymous users cannot create EvalAdhocGroups");

        String trimmedTitle = groupTitle.trim();
        if (trimmedTitle.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "modifyadhocgroup.message.notitle");
            ra.addFlashAttribute("prefillMembers", newMembers);
            return redirectToForm(adhocGroupId, returnUrl);
        }

        // Duplicate title check (case-insensitive, excluding the group being edited)
        for (EvalAdhocGroup existing : commonLogic.getAdhocGroupsForOwner(currentUserId)) {
            if (existing.getTitle().equalsIgnoreCase(trimmedTitle)
                    && !existing.getId().equals(adhocGroupId)) {
                ra.addFlashAttribute("errorMessage", "modifyadhocgroup.message.duplicatetitle");
                ra.addFlashAttribute("prefillTitle", groupTitle);
                ra.addFlashAttribute("prefillMembers", newMembers);
                return redirectToForm(adhocGroupId, returnUrl);
            }
        }

        List<String> rejected = new ArrayList<>();
        List<String> alreadyIn = new ArrayList<>();
        Long[] savedId = new Long[1];
        String[] savedTitle = new String[1];

        try {
            daoInvoker.invokeTransactionalAccess(() -> {
                EvalAdhocGroup group;
                if (adhocGroupId == null) {
                    group = new EvalAdhocGroup(currentUserId, trimmedTitle);
                } else {
                    group = commonLogic.getAdhocGroupById(adhocGroupId);
                    if (group == null) throw new IllegalArgumentException("Adhoc group not found: " + adhocGroupId);
                    if (!currentUserId.equals(group.getOwner()))
                        throw new SecurityException("Only owners can modify adhocgroups: " + currentUserId);
                    if (isGroupLocked(group)) {
                        throw new LockedGroupException();
                    }
                    group.setTitle(trimmedTitle);
                }

                // Process members before saving so invalid users abort the whole transaction.
                List<String> toAdd = addMembersFromText(newMembers, group, rejected, alreadyIn);
                if (!rejected.isEmpty()) {
                    throw new InvalidMembersException();
                }

                Set<String> all = new HashSet<>();
                if (group.getParticipantIds() != null) all.addAll(group.getParticipantIds());
                all.addAll(toAdd);
                group.setParticipantIds(new ArrayList<>(all));

                commonLogic.saveAdhocGroup(group);
                savedId[0] = group.getId();
                savedTitle[0] = group.getTitle();
            });
        } catch (InvalidMembersException e) {
            boolean useAdhocUsers = Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_ADHOC_USERS));
            ra.addFlashAttribute("errorMessage",
                    useAdhocUsers ? "modifyadhocgroup.message.badusers" : "modifyadhocgroup.message.badusers.noadhocusers");
            ra.addFlashAttribute("errorArgs", new Object[]{ String.join(", ", rejected) });
            ra.addFlashAttribute("prefillTitle", groupTitle);
            ra.addFlashAttribute("prefillMembers", newMembers);
            return redirectToForm(adhocGroupId, returnUrl);
        } catch (LockedGroupException e) {
            ra.addFlashAttribute("errorMessage", "controladhocgroups.group.locked.tooltip");
            return redirectToForm(adhocGroupId, returnUrl);
        }

        if (!alreadyIn.isEmpty()) {
            ra.addFlashAttribute("infoMessage", "modifyadhocgroup.message.existingusers");
            ra.addFlashAttribute("infoArgs", new Object[]{ String.join(", ", alreadyIn) });
        }

        log.info("User ({}) saved adhoc group ({})", currentUserId, savedId[0]);
        if (returnUrl.isEmpty()) {
            ra.addFlashAttribute("successMessage", "controladhocgroups.group.saved");
            ra.addFlashAttribute("successArgs", new Object[]{ savedTitle[0] });
            return "redirect:/control_adhoc_groups";
        }
        ra.addFlashAttribute("successMessage", "modifyadhocgroup.message.savednewgroup");
        ra.addFlashAttribute("successArgs", new Object[]{ savedTitle[0] });
        return "redirect:" + returnUrl;
    }

    @PostMapping("/remove")
    public String removeMember(@RequestParam Long adhocGroupId,
                               @RequestParam String adhocUserId,
                               @RequestParam(required = false, defaultValue = "") String returnUrl,
                               RedirectAttributes ra) {
        String currentUserId = currentUserId();
        String[] removedDisplayName = new String[1];
        boolean[] locked = new boolean[1];

        daoInvoker.invokeTransactionalAccess(() -> {
            EvalAdhocGroup group = commonLogic.getAdhocGroupById(adhocGroupId);
            if (group == null) throw new IllegalArgumentException("Adhoc group not found: " + adhocGroupId);
            if (!currentUserId.equals(group.getOwner()))
                throw new SecurityException("Only owners can modify adhocgroups: " + currentUserId);
            if (isGroupLocked(group)) {
                locked[0] = true;
                return;
            }

            List<String> participants = new ArrayList<>();
            if (group.getParticipantIds() != null) {
                for (String id : group.getParticipantIds()) {
                    if (!id.equals(adhocUserId)) participants.add(id);
                }
            }
            group.setParticipantIds(participants);
            commonLogic.saveAdhocGroup(group);

            EvalUser removed = commonLogic.getEvalUserById(adhocUserId);
            removedDisplayName[0] = removed.displayName;
        });

        if (locked[0]) {
            ra.addFlashAttribute("errorMessage", "controladhocgroups.group.locked.tooltip");
            return "redirect:/modify_adhoc_group?adhocGroupId=" + adhocGroupId
                    + (returnUrl.isEmpty() ? "" : "&returnUrl=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8));
        }

        ra.addFlashAttribute("successMessage", "modifyadhocgroup.message.removeduser");
        ra.addFlashAttribute("successArgs", new Object[]{ removedDisplayName[0] });

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
                    EvalAdhocUser adhocUser = new EvalAdhocUser(currentUserId(), entry);
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

    private boolean isGroupLocked(EvalAdhocGroup group) {
        List<EvalEvaluation> evals = evaluationService.getEvaluationsForEvalGroups(
                new String[]{group.getEvalGroupId()}, 0, 0);
        for (EvalEvaluation eval : evals) {
            if (ControlAdhocGroupsController.isLockedState(evaluationService.updateEvaluationState(eval.getId())))
                return true;
        }
        return false;
    }
}
