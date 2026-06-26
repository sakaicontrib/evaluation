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

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.model.EvalEmailMessage;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
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

/**
 * Spring MVC equivalent of EvaluationNotificationsProducer.
 */
@Slf4j
@Controller
@RequestMapping("/evaluation_notifications")
public class EvaluationNotificationsController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationService")
    private EvalEvaluationService evaluationService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEmailsLogic")
    private EvalEmailsLogic emailsLogic;

    @Data
    public static class SendToOption {
        private final String value;
        private final String labelKey;
    }

    @GetMapping
    public String show(@RequestParam Long evaluationId,
                       @RequestParam(required = false) String evalGroupId,
                       @RequestParam(required = false) String errorMessage,
                       Model model) {

        String currentUserId = commonLogic.getCurrentUserId();
        if (evalGroupId != null && evalGroupId.isEmpty()) evalGroupId = null;

        if (!evaluationService.canControlEvaluation(currentUserId, evaluationId)) {
            throw new SecurityException("User not allowed to access notifications for evaluation: " + evaluationId);
        }

        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());

        String groupTitle = null;
        if (evalGroupId != null) {
            groupTitle = commonLogic.makeEvalGroupObject(evalGroupId).title;
        }

        List<SendToOption> sendToOptions = new ArrayList<>();
        for (int i = 0; i < EvalToolConstants.EVAL_NOTIFICATION_VALUES.length; i++) {
            sendToOptions.add(new SendToOption(
                    EvalToolConstants.EVAL_NOTIFICATION_VALUES[i],
                    EvalToolConstants.EVAL_NOTIFICATION_LABELS_PROPS[i]));
        }

        model.addAttribute("evaluationId", evaluationId);
        model.addAttribute("evalGroupId", evalGroupId);
        model.addAttribute("evalTitle", eval.getTitle());
        model.addAttribute("evalStartDate", df.format(eval.getStartDate()));
        model.addAttribute("evalDueDate", df.format(eval.getSafeDueDate()));
        model.addAttribute("groupTitle", groupTitle);
        model.addAttribute("sendToOptions", sendToOptions);
        model.addAttribute("errorMessage", errorMessage);
        return "evaluation_notifications";
    }

    @PostMapping
    public String send(@RequestParam Long evaluationId,
                       @RequestParam(required = false) String evalGroupId,
                       @RequestParam(required = false) String subject,
                       @RequestParam(required = false) String message,
                       @RequestParam(required = false) String sendTo,
                       RedirectAttributes redirectAttributes) {

        if (EvalUtils.isBlank(subject) || EvalUtils.isBlank(message) || EvalUtils.isBlank(sendTo)) {
            redirectAttributes.addAttribute("evaluationId", evaluationId);
            if (evalGroupId != null) redirectAttributes.addAttribute("evalGroupId", evalGroupId);
            redirectAttributes.addAttribute("errorMessage", "evalnotify.all.required");
            return "redirect:/evaluation_notifications";
        }

        String[] evalGroupIds = evalGroupId != null ? new String[]{evalGroupId} : null;
        EvalEvaluation evaluation = evaluationService.getEvaluationById(evaluationId);
        EvalEmailMessage emailMessage = emailsLogic.makeEmailMessage(message, subject, evaluation, null, null);
        String[] sent = emailsLogic.sendEmailMessages(emailMessage.message, emailMessage.subject,
                evaluationId, evalGroupIds, sendTo);

        log.info("Sent {} notification emails for evaluation {}", sent.length, evaluationId);
        return "redirect:/control_evaluations";
    }
}