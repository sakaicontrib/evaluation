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
public class AdministrateEmailController extends EvalControllerSupport {

    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm";
    private static final String RSF_DATE_FORMAT = "EEE MMM dd kk:mm:ss zzz yyyy";

    @GetMapping
    public String show(Model model) {
        checkAdmin();

        model.addAttribute("useConsolidated", Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT)));

        model.addAttribute("defaultReminderFrequency", toStr(settings.get(EvalSettings.DEFAULT_EMAIL_REMINDER_FREQUENCY)));
        model.addAttribute("enableJobCompletionEmail", Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_JOB_COMPLETION_EMAIL)));
        model.addAttribute("enableReminderStatus", Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_REMINDER_STATUS)));
        model.addAttribute("evalTimeToWaitSecs", toStr(settings.get(EvalSettings.EVALUATION_TIME_TO_WAIT_SECS)));
        model.addAttribute("allowEvalBeginEmail", Boolean.TRUE.equals(settings.get(EvalSettings.ALLOW_EVALSPECIFIC_TOGGLE_EMAIL_NOTIFICATION)));

        model.addAttribute("consolidatedSendAvailable", Boolean.TRUE.equals(settings.get(EvalSettings.CONSOLIDATED_EMAIL_NOTIFY_AVAILABLE)));
        model.addAttribute("forceSendAvailable", Boolean.TRUE.equals(settings.get(EvalSettings.CONSOLIDATED_FORCE_SEND_AVAILABLE_NOTIFICATION)));
        model.addAttribute("forceSendCreatedEmail", Boolean.TRUE.equals(settings.get(EvalSettings.CONSOLIDATED_FORCE_SEND_CREATED_EMAIL)));
        model.addAttribute("singleEmailReminderDays", toStr(settings.get(EvalSettings.SINGLE_EMAIL_REMINDER_DAYS)));
        model.addAttribute("consolidatedJobStartTime", toStr(settings.get(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_TIME)));
        model.addAttribute("consolidatedJobStartMinutes", toStr(settings.get(EvalSettings.CONSOLIDATED_EMAIL_DAILY_START_MINUTES)));
        model.addAttribute("logProgressEvery", toStr(settings.get(EvalSettings.LOG_PROGRESS_EVERY)));
        model.addAttribute("emailBatchSize", toStr(settings.get(EvalSettings.EMAIL_BATCH_SIZE)));
        model.addAttribute("emailWaitInterval", toStr(settings.get(EvalSettings.EMAIL_WAIT_INTERVAL)));

        model.addAttribute("sendSubmitted", Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_SUBMISSION_CONFIRMATION_EMAIL)));
        model.addAttribute("sendEvaluatee", Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_SUBMISSION_EVALUATEE_EMAIL)));
        model.addAttribute("useAdminFromEmail", Boolean.TRUE.equals(settings.get(EvalSettings.USE_ADMIN_AS_FROM_EMAIL)));

        Object fromEmail = settings.get(EvalSettings.FROM_EMAIL_ADDRESS);
        model.addAttribute("helpdeskEmail", fromEmail != null ? fromEmail.toString() : "");

        model.addAttribute("deliveryOption", toStr(settings.get(EvalSettings.EMAIL_DELIVERY_OPTION)));

        // Next reminder date: stored as Date or legacy String
        Object dateObj = settings.get(EvalSettings.NEXT_REMINDER_DATE);
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

        settings.set(EvalSettings.ENABLE_SINGLE_EMAIL_PER_STUDENT, "true".equals(params.get("useConsolidated")));

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
        settings.set(EvalSettings.FROM_EMAIL_ADDRESS, fromEmail.isEmpty() ? null : fromEmail);

        String delivery = params.get("deliveryOption");
        if (delivery != null) settings.set(EvalSettings.EMAIL_DELIVERY_OPTION, delivery);

        String dateStr = params.get("nextReminderDate");
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                settings.set(EvalSettings.NEXT_REMINDER_DATE, new SimpleDateFormat(DATETIME_FORMAT).parse(dateStr));
            } catch (ParseException e) {
                log.warn("Could not parse nextReminderDate: {}", dateStr);
            }
        }

        log.info("Admin ({}) saved email settings", currentUserId());
        return "redirect:/administrate_email";
    }

    private void checkAdmin() {
        if (!isCurrentUserAdmin())
            throw new SecurityException("Non-admin users may not access this page");
    }

    private void saveBool(String key, Map<String, String> params, String param) {
        settings.set(key, params.containsKey(param));
    }

    private void saveInt(String key, Map<String, String> params, String param) {
        String val = params.get(param);
        if (val != null && !val.isEmpty()) {
            try { settings.set(key, Integer.parseInt(val)); }
            catch (NumberFormatException ignored) {}
        }
    }

    private String toStr(Object val) {
        return val != null ? val.toString() : "";
    }
}