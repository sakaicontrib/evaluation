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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring MVC equivalent of AdministrateProducer.
 */
@Slf4j
@Controller
@RequestMapping("/administrate")
public class AdministrateController {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    private EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalSettings")
    private EvalSettings evalSettings;

    /** Settings that are ternary (true/false/null → "1"/"0"/"-1") */
    private static final Set<String> TERNARY = new HashSet<>(Arrays.asList(EvalSettings.TERNARY_BOOLEAN_SETTINGS));

    /** Settings that are stored as Integer from selects/inputs */
    private static final Set<String> INTEGER_SETTINGS = new HashSet<>(Arrays.asList(
            EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER,
            EvalSettings.ADMIN_ADD_ITEMS_NUMBER,
            EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS,
            EvalSettings.ITEMS_ALLOWED_IN_QUESTION_BLOCK,
            EvalSettings.EVAL_MIN_TIME_DIFF_BETWEEN_START_DUE,
            EvalSettings.EVAL_EVALUATEE_RECENTLY_CLOSED_DAYS,
            EvalSettings.EVAL_RECENTLY_CLOSED_DAYS,
            EvalSettings.EVAL_MIN_LIST_LENGTH,
            EvalSettings.EVAL_MAX_LIST_LENGTH
    ));

    /** All boolean checkbox settings (simple true/false, not ternary) */
    private static final String[] BOOLEAN_CHECKBOXES = {
            EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS,
            EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS,
            EvalSettings.ENABLE_PROVIDER_SYNC,
            EvalSettings.ADMIN_VIEW_INSTRUCTOR_ADDED_RESULTS,
            EvalSettings.ADMIN_VIEW_BELOW_RESULTS,
            EvalSettings.DISPLAY_HIERARCHY_OPTIONS,
            EvalSettings.DISPLAY_HIERARCHY_HEADERS,
            EvalSettings.ENABLE_EVALUATEE_BOX,
            EvalSettings.ENABLE_ADMINISTRATING_BOX,
            EvalSettings.ENABLE_SUMMARY_SITES_BOX,
            EvalSettings.ENABLE_NOT_AVAILABLE,
            EvalSettings.ENABLE_TEMPLATE_COPYING,
            EvalSettings.EVAL_USE_DATE_TIME,
            EvalSettings.EVAL_USE_STOP_DATE,
            EvalSettings.EVAL_USE_VIEW_DATE,
            EvalSettings.EVAL_USE_SAME_VIEW_DATES,
            EvalSettings.ENABLE_MY_TOPLINKS,
            EvalSettings.ENABLE_EVAL_CATEGORIES,
            EvalSettings.ENABLE_EVAL_TERM_IDS,
            EvalSettings.ENABLE_EVAL_RESPONSE_REMOVAL,
            EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY,
            EvalSettings.USE_EXPERT_TEMPLATES,
            EvalSettings.USE_EXPERT_ITEMS,
            EvalSettings.VIEW_SURVEY_RESULTS_IGNORE_DATES,
            EvalSettings.ENABLE_ADHOC_GROUPS,
            EvalSettings.ENABLE_ADHOC_USERS,
            EvalSettings.ENABLE_EVAL_EARLY_CLOSE,
            EvalSettings.ENABLE_EVAL_REOPEN,
            EvalSettings.ENABLE_ITEM_COMMENTS,
            EvalSettings.ENABLE_GROUP_SPECIFIC_PREVIEW,
            EvalSettings.ITEM_USE_RESULTS_SHARING,
            EvalSettings.ENABLE_IMPORTING,
            EvalSettings.DISABLE_ITEM_BANK,
            EvalSettings.DISABLE_QUESTION_BLOCKS,
            EvalSettings.ENABLE_ASSISTANT_CATEGORY,
            EvalSettings.ENABLE_INSTRUCTOR_ASSISTANT_SELECTION,
            EvalSettings.ENABLE_SITE_GROUP_PUBLISH_CHECK,
            EvalSettings.STUDENT_SAVE_WITHOUT_SUBMIT,
            EvalSettings.STUDENT_CANCEL_ALLOWED
    };

    @GetMapping
    public String show(Model model) {
        String currentUserId = commonLogic.getCurrentUserId();
        if (!commonLogic.isUserAdmin(currentUserId)) {
            throw new SecurityException("Non-admin users may not access this page");
        }

        // Load all settings into model
        for (String key : BOOLEAN_CHECKBOXES) {
            model.addAttribute(toAttr(key), Boolean.TRUE.equals(evalSettings.get(key)));
        }
        for (String key : TERNARY) {
            model.addAttribute(toAttr(key), toTernaryValue(evalSettings.get(key)));
        }
        for (String key : INTEGER_SETTINGS) {
            Object val = evalSettings.get(key);
            model.addAttribute(toAttr(key), val != null ? val.toString() : "");
        }
        model.addAttribute(toAttr(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE),
                toMustUseValue(evalSettings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE)));
        model.addAttribute(toAttr(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY),
                evalSettings.get(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY));
        model.addAttribute(toAttr(EvalSettings.LOCAL_CSS_PATH),
                EvalUtils.safeStringSetting(evalSettings, EvalSettings.LOCAL_CSS_PATH));

        // Email delivery mode — message key "administrate.email.delivery" takes {0}=mode, {1}=env
        String delivery = (String) evalSettings.get(EvalSettings.EMAIL_DELIVERY_OPTION);
        String deliveryMode, deliveryEnv;
        if (EvalConstants.EMAIL_DELIVERY_SEND.equals(delivery)) { deliveryMode = "SEND email";   deliveryEnv = "PRODUCTION"; }
        else if (EvalConstants.EMAIL_DELIVERY_LOG.equals(delivery)) { deliveryMode = "LOG email"; deliveryEnv = "DEVELOPMENT"; }
        else { deliveryMode = "NOT send email"; deliveryEnv = "TEST"; }
        model.addAttribute("emailDeliveryMode", deliveryMode);
        model.addAttribute("emailDeliveryEnv", deliveryEnv);

        // Sub-page link visibility
        model.addAttribute("enableProviderSync", Boolean.TRUE.equals(evalSettings.get(EvalSettings.ENABLE_PROVIDER_SYNC)));
        model.addAttribute("enableImporting", Boolean.TRUE.equals(evalSettings.get(EvalSettings.ENABLE_IMPORTING)));
        model.addAttribute("useHierarchy", Boolean.TRUE.equals(evalSettings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS)));

        // Select option values for template
        model.addAttribute("ADMIN_BOOLEAN_YES", EvalToolConstants.ADMIN_BOOLEAN_YES);
        model.addAttribute("ADMIN_BOOLEAN_NO", EvalToolConstants.ADMIN_BOOLEAN_NO);
        model.addAttribute("ADMIN_BOOLEAN_CONFIGURABLE", EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE);
        model.addAttribute("pulldownValues", EvalToolConstants.PULLDOWN_INTEGER_VALUES);
        model.addAttribute("minimumTimeDiff", EvalToolConstants.MINIMUM_TIME_DIFFERENCE);
        model.addAttribute("appVersion", EvalConstants.APP_VERSION);
        model.addAttribute("svnRevision", EvalConstants.SVN_REVISION);
        return "administrate";
    }

    @PostMapping
    public String save(@RequestParam Map<String, String> params) {
        String currentUserId = commonLogic.getCurrentUserId();
        if (!commonLogic.isUserAdmin(currentUserId)) {
            throw new SecurityException("Non-admin users may not access this page");
        }

        // Save boolean checkboxes (absent = false)
        for (String key : BOOLEAN_CHECKBOXES) {
            boolean value = params.containsKey(toParam(key));
            evalSettings.set(key, value);
        }

        // Save ternary selects
        for (String key : TERNARY) {
            String val = params.get(toParam(key));
            if (val != null) evalSettings.set(key, fromTernaryValue(val));
        }

        // Save integer settings
        for (String key : INTEGER_SETTINGS) {
            String val = params.get(toParam(key));
            if (val != null && !val.isEmpty()) {
                try { evalSettings.set(key, Integer.parseInt(val)); } catch (NumberFormatException ignored) {}
            }
        }

        // Save special string selects
        String mustUse = params.get(toParam(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE));
        if (mustUse != null) {
            evalSettings.set(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE,
                    EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE.equals(mustUse) ? null : mustUse);
        }

        String sharing = params.get(toParam(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY));
        if (sharing != null) evalSettings.set(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY, sharing);

        // Save string input
        String cssPath = params.getOrDefault(toParam(EvalSettings.LOCAL_CSS_PATH), "");
        evalSettings.set(EvalSettings.LOCAL_CSS_PATH, cssPath.isEmpty() ? null : cssPath);

        // Sanity check min/max list length
        Integer minList = getInteger(params, EvalSettings.EVAL_MIN_LIST_LENGTH);
        Integer maxList = getInteger(params, EvalSettings.EVAL_MAX_LIST_LENGTH);
        if (minList != null && maxList != null) {
            if (minList < 1) minList = 1;
            if (maxList < minList) maxList = minList;
            if (minList > maxList) minList = maxList;
            evalSettings.set(EvalSettings.EVAL_MIN_LIST_LENGTH, minList);
            evalSettings.set(EvalSettings.EVAL_MAX_LIST_LENGTH, maxList);
        }

        log.info("Admin ({}) saved system settings", currentUserId);
        return "redirect:/administrate";
    }

    @PostMapping("/reset")
    public String resetCache() {
        evalSettings.resetCache(null);
        return "redirect:/administrate";
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Strip type suffix from EvalSettings constant (e.g. "FOO:java.lang.Boolean" → "FOO") */
    private String cleanKey(String key) {
        int idx = key.indexOf(':');
        return idx >= 0 ? key.substring(0, idx) : key;
    }

    /** Convert EvalSettings constant to model attribute name */
    private String toAttr(String key) {
        return "s_" + cleanKey(key).replace('.', '_').replace('-', '_');
    }

    /** Convert EvalSettings constant to form param name */
    private String toParam(String key) {
        return cleanKey(key);
    }

    /** Get current ternary value as "1"/"0"/"-1" */
    private String toTernaryValue(Object val) {
        if (val == null) return EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE;
        if (Boolean.TRUE.equals(val)) return EvalToolConstants.ADMIN_BOOLEAN_YES;
        return EvalToolConstants.ADMIN_BOOLEAN_NO;
    }

    /** Convert ternary form value back to Boolean/null */
    private Object fromTernaryValue(String val) {
        if (EvalToolConstants.ADMIN_BOOLEAN_YES.equals(val)) return Boolean.TRUE;
        if (EvalToolConstants.ADMIN_BOOLEAN_NO.equals(val)) return Boolean.FALSE;
        return null;
    }

    private String toMustUseValue(Object val) {
        if (val == null) return EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE;
        return val.toString();
    }

    private Integer getInteger(Map<String, String> params, String key) {
        String val = params.get(toParam(key));
        if (val == null || val.isEmpty()) return null;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return null; }
    }
}