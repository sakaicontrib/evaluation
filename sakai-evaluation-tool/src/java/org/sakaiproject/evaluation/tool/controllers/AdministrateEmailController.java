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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/administrate_email")
public class AdministrateEmailController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalSettings")
    private EvalSettings evalSettings;

    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm";
    private static final String RSF_DATE_FORMAT = "EEE MMM dd kk:mm:ss zzz yyyy";

    @GetMapping
    public String show(Model model) {
        checkAdmin();

        model.addAttribute("useConsolidated", Boolean.TRUE.equals(evalSettings.get(EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT)));

        model.addAttribute("defaultReminderFrequency", toStr(evalSettings.get(EvalSettings.DEFAULT_EMAIL_REMINDER_FREQUENCY)));
        model.addAttribute("enableJobCompletionEmail", Boolean.TRUE.equals(evalSettings.get(EvalSettings.ENABLE_JOB_COMPLETION_EMAIL)));
        model.addAttribute("enableReminderStatus", Boolean.TRUE.equals(evalSettings.get(EvalSettings.ENABLE_REMINDER_STATUS)));
        model.addAttribute("evalTimeToWaitSecs", toStr(evalSettings.get(EvalSettings.EVALUATION_TIME_TO_WAIT_SECS)));
        model.addAttribute("allowEvalBeginEmail", Boolean.TRUE.equals(evalSettings.get(EvalSettings.ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION)));

        model.addAttribute("consolidatedSendAvailable", Boolean.TRUE.equals(evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE)));
        model.addAttribute("forceSendAvailable", Boolean.TRUE.equals(evalSettings.get(EvalSettings.CONSOLIDATED_FORCE_SEND_AVAILABLE_NOTIFICATION)));
        model.addAttribute("forceSendCreatedEmail", Boolean.TRUE.equals(evalSettings.get(EvalSettings.CONSOLIDATED_FORCE_SEND_CREATED_EMAIL)));
        model.addAttribute("singleEmailReminderDays", toStr(evalSettings.get(EvalSettings.SINGLE_EMAIL_REMINDER_DAYS)));
        model.addAttribute("consolidatedJobStartTime", toStr(evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_TIME)));
        model.addAttribute("consolidatedJobStartMinutes", toStr(evalSettings.get(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_MINUTES)));
        model.addAttribute("logProgressEvery", toStr(evalSettings.get(EvalSettings.LOG_PROGRESS_EVERY)));
        model.addAttribute("emailBatchSize", toStr(evalSettings.get(EvalSettings.EMAIL_BATCH_SIZE)));
        model.addAttribute("emailWaitInterval", toStr(evalSettings.get(EvalSettings.EMAIL_WAIT_INTERVAL)));

        model.addAttribute("sendSubmitted", Boolean.TRUE.equals(evalSettings.get(EvalSettings.ENABLE_SUBMISSION_CONFIRMATION_EMAIL)));
        model.addAttribute("sendEvaluatee", Boolean.TRUE.equals(evalSettings.get(EvalSettings.ENABLE_SUBMISSION_EVALUATEE_EMAIL)));
        model.addAttribute("useAdminFromEmail", Boolean.TRUE.equals(evalSettings.get(EvalSettings.USE_ADMIN_AS_FROM_EMAIL)));

        Object fromEmail = evalSettings.get(EvalSettings.FROM_EMAIL_ADDRESS);
        model.addAttribute("helpdeskEmail", fromEmail != null ? fromEmail.toString() : "");

        model.addAttribute("deliveryOption", toStr(evalSettings.get(EvalSettings.EMAIL_DELIVERY_OPTION)));

        // Next reminder date: stored as Date or legacy String
        Object dateObj = evalSettings.get(EvalSettings.NEXT_REMINDER_DATE);
        Date nextReminder;
        if (dateObj instanceof Date) {
            nextReminder = (Date) dateObj;
        } else if (dateObj instanceof String && !((String) dateObj).trim().isEmpty()) {
            try {
                nextReminder = new SimpleDateFormat(RSF_DATE_FORMAT, Locale.US).parse((String) dateObj);
            } catch (ParseException e) {
                nextReminder = new Date();
            }
        } else {
            nextReminder = new Date();
        }
        model.addAttribute("nextReminderDate", new SimpleDateFormat(DATETIME_FORMAT).format(nextReminder));

        // Data for selects
        model.addAttribute("reminderDayValues", EvalToolConstants.REMINDER_EMAIL_DAYS_VALUES);
        model.addAttribute("hourValues", IntStream.rangeClosed(0, 23).boxed().collect(Collectors.toList()));
        model.addAttribute("minuteValues", Arrays.asList(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55));
        model.addAttribute("batchValues", EvalToolConstants.PULLDOWN_BATCH_VALUES);
        model.addAttribute("deliveryValues", EvalToolConstants.EMAIL_DELIVERY_VALUES);
        model.addAttribute("deliveryLabels", EvalToolConstants.EMAIL_DELIVERY_LABELS);

        return "administrate_email";
    }

    @PostMapping
    public String save(@RequestParam Map<String, String> params) {
        checkAdmin();

        evalSettings.set(EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT, "true".equals(params.get("useConsolidated")));

        saveBool(EvalSettings.ENABLE_JOB_COMPLETION_EMAIL, params, "enableJobCompletionEmail");
        saveBool(EvalSettings.ENABLE_REMINDER_STATUS, params, "enableReminderStatus");
        saveBool(EvalSettings.ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION, params, "allowEvalBeginEmail");
        saveBool(EvalSettings.CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE, params, "consolidatedSendAvailable");
        saveBool(EvalSettings.CONSOLIDATED_FORCE_SEND_AVAILABLE_NOTIFICATION, params, "forceSendAvailable");
        saveBool(EvalSettings.CONSOLIDATED_FORCE_SEND_CREATED_EMAIL, params, "forceSendCreatedEmail");
        saveBool(EvalSettings.ENABLE_SUBMISSION_CONFIRMATION_EMAIL, params, "sendSubmitted");
        saveBool(EvalSettings.ENABLE_SUBMISSION_EVALUATEE_EMAIL, params, "sendEvaluatee");
        saveBool(EvalSettings.USE_ADMIN_AS_FROM_EMAIL, params, "useAdminFromEmail");

        saveInt(EvalSettings.DEFAULT_EMAIL_REMINDER_FREQUENCY, params, "defaultReminderFrequency");
        saveInt(EvalSettings.EVALUATION_TIME_TO_WAIT_SECS, params, "evalTimeToWaitSecs");
        saveInt(EvalSettings.SINGLE_EMAIL_REMINDER_DAYS, params, "singleEmailReminderDays");
        saveInt(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_TIME, params, "consolidatedJobStartTime");
        saveInt(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_MINUTES, params, "consolidatedJobStartMinutes");
        saveInt(EvalSettings.LOG_PROGRESS_EVERY, params, "logProgressEvery");
        saveInt(EvalSettings.EMAIL_BATCH_SIZE, params, "emailBatchSize");
        saveInt(EvalSettings.EMAIL_WAIT_INTERVAL, params, "emailWaitInterval");

        String fromEmail = params.getOrDefault("helpdeskEmail", "").trim();
        evalSettings.set(EvalSettings.FROM_EMAIL_ADDRESS, fromEmail.isEmpty() ? null : fromEmail);

        String delivery = params.get("deliveryOption");
        if (delivery != null) evalSettings.set(EvalSettings.EMAIL_DELIVERY_OPTION, delivery);

        String dateStr = params.get("nextReminderDate");
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                evalSettings.set(EvalSettings.NEXT_REMINDER_DATE, new SimpleDateFormat(DATETIME_FORMAT).parse(dateStr));
            } catch (ParseException e) {
                log.warn("Could not parse nextReminderDate: {}", dateStr);
            }
        }

        log.info("Admin ({}) saved email settings", commonLogic.getCurrentUserId());
        return "redirect:/administrate_email";
    }

    private void checkAdmin() {
        if (!commonLogic.isUserAdmin(commonLogic.getCurrentUserId()))
            throw new SecurityException("Non-admin users may not access this page");
    }

    private void saveBool(String key, Map<String, String> params, String param) {
        evalSettings.set(key, params.containsKey(param));
    }

    private void saveInt(String key, Map<String, String> params, String param) {
        String val = params.get(param);
        if (val != null && !val.isEmpty()) {
            try { evalSettings.set(key, Integer.parseInt(val)); }
            catch (NumberFormatException ignored) {}
        }
    }

    private String toStr(Object val) {
        return val != null ? val.toString() : "";
    }
}