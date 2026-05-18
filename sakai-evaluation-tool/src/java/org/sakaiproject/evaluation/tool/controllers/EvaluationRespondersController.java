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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of EvaluationRespondersProducer.
 */
@Slf4j
@Controller
@RequestMapping("/evaluation_responders")
public class EvaluationRespondersController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationService")
    private EvalEvaluationService evaluationService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalDeliveryService")
    private EvalDeliveryService deliveryService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalSettings")
    private EvalSettings settings;

    @Data
    public static class UserRow {
        private final String displayName;
        private final String username;
        private final String statusKey; // i18n key or null for "-"
    }

    @Data
    public static class GroupRow {
        private final String groupTitle;     // null → anonymous eval
        private final int responded;
        private final int assigned;
        private final String emailGroupId;   // null → no email link; group id for the notification URL
        private final List<UserRow> users;
    }

    @GetMapping
    public String show(@RequestParam Long evaluationId,
                       @RequestParam(required = false) String evalGroupId,
                       Model model) {

        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        if (evalGroupId != null && evalGroupId.isEmpty()) evalGroupId = null;

        if (!evaluationService.canControlEvaluation(currentUserId, evaluationId)) {
            throw new SecurityException("User not allowed to access responders for evaluation: " + evaluationId);
        }

        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        boolean evalAnonymous = EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl());
        boolean allowEmailStudents = Boolean.TRUE.equals(settings.get(EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS));
        int responsesRequired = (Integer) settings.get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS);


        String[] evalGroupIds = evalGroupId != null ? new String[]{evalGroupId} : null;

        // load all responses
        List<EvalResponse> responses = deliveryService.getEvaluationResponses(evaluationId, evalGroupIds, null);
        Map<String, Map<String, EvalResponse>> groupToUserResponses = new HashMap<>();
        for (EvalResponse r : responses) {
            groupToUserResponses.computeIfAbsent(r.getEvalGroupId(), k -> new HashMap<>())
                    .put(r.getOwner(), r);
        }

        boolean showStatus = (responses.size() >= responsesRequired) || true; // controlEval = true (already checked above)

        // build per-group data
        Map<String, List<EvalUser>> usersByGroupId = new HashMap<>();
        String statsAssigned;

        if (evalAnonymous) {
            statsAssigned = "--";
            List<EvalUser> users = new ArrayList<>();
            Map<String, EvalResponse> anonResponses = new HashMap<>();
            for (EvalResponse r : responses) {
                users.add(commonLogic.getEvalUserById(r.getOwner()));
                anonResponses.put(r.getOwner(), r);
            }
            usersByGroupId.put(EvalConstants.EVALUATION_AUTHCONTROL_NONE, users);
            // index anonymous responses under the same synthetic key so the status
            // lookup below finds them (responses are stored with their real evalGroupId,
            // which differs from the synthetic key used in usersByGroupId)
            groupToUserResponses.put(EvalConstants.EVALUATION_AUTHCONTROL_NONE, anonResponses);
        } else {
            List<EvalAssignUser> assigned = evaluationService.getParticipantsForEval(
                    evaluationId, null, evalGroupIds, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
            if (!assigned.isEmpty()) {
                // EvalAssignUser records already generated
                for (EvalAssignUser au : assigned) {
                    usersByGroupId.computeIfAbsent(au.getEvalGroupId(), k -> new ArrayList<>())
                            .add(commonLogic.getEvalUserById(au.getUserId()));
                }
                statsAssigned = String.valueOf(assigned.size());
            } else {
                // EvalAssignUser not yet generated — resolve from EvalAssignGroup
                Map<Long, List<EvalAssignGroup>> assignGroups = evaluationService
                        .getAssignGroupsForEvals(new Long[]{evaluationId}, false, null);
                List<EvalAssignGroup> groups = assignGroups.getOrDefault(evaluationId, new ArrayList<>());
                int totalUsers = 0;
                for (EvalAssignGroup ag : groups) {
                    String groupId = ag.getEvalGroupId();
                    if (evalGroupIds != null && !java.util.Arrays.asList(evalGroupIds).contains(groupId)) continue;
                    Set<String> userIds = commonLogic.getUserIdsForEvalGroup(groupId, EvalConstants.PERM_TAKE_EVALUATION, false);
                    List<EvalUser> users = commonLogic.getEvalUsersByIds(new ArrayList<>(userIds));
                    usersByGroupId.put(groupId, users);
                    totalUsers += users.size();
                }
                statsAssigned = String.valueOf(totalUsers);
            }
        }

        List<GroupRow> groupRows = new ArrayList<>();
        for (Map.Entry<String, List<EvalUser>> entry : usersByGroupId.entrySet()) {
            String groupId = entry.getKey();
            List<EvalUser> users = entry.getValue();
            Collections.sort(users, new EvalUser.SortNameComparator());

            // canControlEvaluation already passed at the top of this method
            boolean showEntryStatus = true;
            Map<String, EvalResponse> userResponses = groupToUserResponses.getOrDefault(groupId, new HashMap<>());

            String groupTitle = null;
            String emailGroupId = null;
            int assigned2 = users.size();

            if (!evalAnonymous) {
                groupTitle = commonLogic.makeEvalGroupObject(groupId).title;
                if (allowEmailStudents || userAdmin) {
                    emailGroupId = groupId;
                }
            }

            List<UserRow> userRows = new ArrayList<>();
            for (EvalUser u : users) {
                String statusKey;
                if ((showStatus && showEntryStatus) || userAdmin) {
                    EvalResponse resp = userResponses.get(u.userId);
                    if (resp == null) {
                        statusKey = "evalresponders.status.untaken";
                    } else if (resp.complete) {
                        statusKey = "evalresponders.status.complete";
                    } else {
                        statusKey = "evalresponders.status.incomplete";
                    }
                } else {
                    statusKey = null; // show "-"
                }
                userRows.add(new UserRow(u.displayName, u.username, statusKey));
            }

            groupRows.add(new GroupRow(groupTitle, userResponses.size(), assigned2, emailGroupId, userRows));
        }

        // global email link only makes sense when there are multiple groups
        boolean showGlobalEmail = (allowEmailStudents || userAdmin) && groupRows.size() > 1;

        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("evalGroupId", evalGroupId);
        model.addAttribute("evalTitle", eval.getTitle());
        model.addAttribute("evalStartDate", df.format(eval.getStartDate()));
        model.addAttribute("evalDueDate", df.format(eval.getSafeDueDate()));
        model.addAttribute("totalResponded", responses.size());
        model.addAttribute("totalAssigned", statsAssigned);
        model.addAttribute("showGlobalEmail", showGlobalEmail);
        model.addAttribute("evalAnonymous", evalAnonymous);
        model.addAttribute("groupRows", groupRows);
        return "evaluation_responders";
    }
}