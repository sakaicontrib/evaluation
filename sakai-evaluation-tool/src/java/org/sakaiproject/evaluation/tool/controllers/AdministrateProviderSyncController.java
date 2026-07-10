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

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.CronExpression;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.scheduling.GroupMembershipSync;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/administrate_provider_sync")
public class AdministrateProviderSyncController extends EvalControllerSupport {

    private static final String JOB_GROUP = "org.sakaiproject.evaluation.tool.ProviderSyncBean";
    private static final long DELAY_MILLIS = 2L * 60L * 1000L;

    @GetMapping
    public String show(@RequestParam(required = false, defaultValue = "0") int tab, Model model) {
        checkAdmin();
        loadModel(model);
        model.addAttribute("initialTab", tab);
        return "administrate_provider_sync";
    }

    // Tab 1: save event settings
    @PostMapping("/events")
    public String saveEvents(@RequestParam(required = false) Boolean syncUnassignedOnStartup,
                             @RequestParam(required = false) Boolean syncOnStateChange,
                             @RequestParam(required = false) Boolean syncOnGroupSave,
                             @RequestParam(required = false) Boolean syncOnGroupUpdate,
                             @RequestParam(required = false) String syncServerId,
                             RedirectAttributes ra) {
        checkAdmin();
        if (syncUnassignedOnStartup != null)
            settings.set(EvalSettings.SYNC_UNASSIGNED_GROUPS_ON_STARTUP, syncUnassignedOnStartup);
        if (syncOnStateChange != null)
            settings.set(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_STATE_CHANGE, syncOnStateChange);
        if (syncOnGroupSave != null)
            settings.set(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_GROUP_SAVE, syncOnGroupSave);
        if (syncOnGroupUpdate != null)
            settings.set(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_GROUP_UPDATE, syncOnGroupUpdate);
        if (syncServerId != null && !syncServerId.isEmpty())
            settings.set(EvalSettings.SYNC_SERVER, syncServerId);
        ra.addFlashAttribute("successMessage", "administrate.sync.event.saved");
        log.info("Admin ({}) saved sync event settings", currentUserId());
        return "redirect:/administrate_provider_sync?tab=0";
    }

    // Tab 2: create a new cron job
    @PostMapping("/schedule")
    public String schedule(@RequestParam(defaultValue = "") String cronExpression,
                           @RequestParam(value = "states", required = false) List<String> states,
                           @RequestParam(required = false) String syncServerId,
                           RedirectAttributes ra) {
        checkAdmin();
        String stateList = states != null ? String.join(" ", states) : "";
        String error = validateCron(cronExpression, stateList);
        if (error != null) {
            ra.addFlashAttribute("errorMessage", error);
            return "redirect:/administrate_provider_sync?tab=1";
        }
        if (syncServerId != null && !syncServerId.isEmpty())
            settings.set(EvalSettings.SYNC_SERVER, syncServerId);
        scheduleCronJob(cronExpression.trim(), stateList.trim());
        ra.addFlashAttribute("successMessage", "administrate.sync.job.success");
        log.info("Admin ({}) scheduled sync: cron={} states={}", currentUserId(), cronExpression, stateList);
        return "redirect:/administrate_provider_sync?tab=1";
    }

    // Tab 2: update an existing cron job
    @PostMapping("/update")
    public String update(@RequestParam String fullJobName,
                         @RequestParam(defaultValue = "") String cronExpression,
                         @RequestParam(value = "states", required = false) List<String> states,
                         @RequestParam(required = false) String syncServerId,
                         RedirectAttributes ra) {
        checkAdmin();
        String stateList = states != null ? String.join(" ", states) : "";
        String error = validateCron(cronExpression, stateList);
        if (error != null) {
            ra.addFlashAttribute("errorMessage", error);
            return "redirect:/administrate_provider_sync?tab=1";
        }
        Map<String, Map<String, String>> cronJobs = commonLogic.getCronJobs(JOB_GROUP);
        Map<String, String> job = cronJobs != null ? cronJobs.get(fullJobName) : null;
        if (job != null) {
            commonLogic.deleteCronJob(job.get(EvalConstants.CRON_SCHEDULER_JOB_NAME),
                    job.get(EvalConstants.CRON_SCHEDULER_JOB_GROUP));
        }
        if (syncServerId != null && !syncServerId.isEmpty())
            settings.set(EvalSettings.SYNC_SERVER, syncServerId);
        scheduleCronJob(cronExpression.trim(), stateList.trim());
        log.info("Admin ({}) updated sync job {}", currentUserId(), fullJobName);
        return "redirect:/administrate_provider_sync?tab=1";
    }

    // Delete cron job
    @PostMapping("/delete")
    public String delete(@RequestParam String fullJobName, RedirectAttributes ra) {
        checkAdmin();
        Map<String, Map<String, String>> cronJobs = commonLogic.getCronJobs(JOB_GROUP);
        Map<String, String> job = cronJobs != null ? cronJobs.get(fullJobName) : null;
        if (job != null) {
            boolean ok = commonLogic.deleteCronJob(job.get(EvalConstants.CRON_SCHEDULER_JOB_NAME),
                    job.get(EvalConstants.CRON_SCHEDULER_JOB_GROUP));
            if (ok) {
                ra.addFlashAttribute("successMessage", "administrate.sync.delete.succeeded");
                ra.addFlashAttribute("successArgs", new Object[]{
                        job.get(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION),
                        job.get(GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST)});
            }
        }
        return "redirect:/administrate_provider_sync?tab=1";
    }

    // Tab 3: quick sync (runs within 2 minutes)
    @PostMapping("/quick")
    public String quickSync(@RequestParam(value = "states", required = false) List<String> states,
                            @RequestParam(required = false) String syncServerId,
                            RedirectAttributes ra) {
        checkAdmin();
        String stateList = states != null ? String.join(" ", states) : "";
        if (stateList.trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "administrate.sync.stateList.null");
            return "redirect:/administrate_provider_sync?tab=2";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis() + DELAY_MILLIS);
        String cron = cal.get(Calendar.SECOND) + " " + cal.get(Calendar.MINUTE) + " " +
                cal.get(Calendar.HOUR_OF_DAY) + " " + cal.get(Calendar.DAY_OF_MONTH) + " " +
                (cal.get(Calendar.MONTH) + 1) + " ? " + cal.get(Calendar.YEAR);
        if (syncServerId != null && !syncServerId.isEmpty())
            settings.set(EvalSettings.SYNC_SERVER, syncServerId);
        scheduleCronJob(cron, stateList.trim());
        ra.addFlashAttribute("successMessage", "administrate.sync.quick.success");
        ra.addFlashAttribute("successArgs", new Object[]{"2", stateList});
        log.info("Admin ({}) quick sync scheduled: states={}", currentUserId(), stateList);
        return "redirect:/administrate_provider_sync?tab=2";
    }

    private void loadModel(Model model) {
        model.addAttribute("syncUnassignedOnStartup",
                Boolean.TRUE.equals(settings.get(EvalSettings.SYNC_UNASSIGNED_GROUPS_ON_STARTUP)));
        model.addAttribute("syncOnStateChange",
                Boolean.TRUE.equals(settings.get(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_STATE_CHANGE)));
        model.addAttribute("syncOnGroupSave",
                Boolean.TRUE.equals(settings.get(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_GROUP_SAVE)));
        model.addAttribute("syncOnGroupUpdate",
                Boolean.TRUE.equals(settings.get(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_GROUP_UPDATE)));
        Object syncServer = settings.get(EvalSettings.SYNC_SERVER);
        model.addAttribute("syncServerId", syncServer != null ? syncServer.toString() : "");

        List<String> serverIds = commonLogic.getServers();
        model.addAttribute("serverIds", serverIds);

        Map<String, Map<String, String>> cronJobs = commonLogic.getCronJobs(JOB_GROUP);
        model.addAttribute("cronJobs", cronJobs != null ? cronJobs : new HashMap<>());
        model.addAttribute("CRON_EXPRESSION_KEY", EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION);
        model.addAttribute("STATE_LIST_KEY", GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST);

        // Pairs [state value, i18n key] for the checkboxes
        model.addAttribute("stateOptions", java.util.Arrays.asList(
            new String[]{EvalConstants.EVALUATION_STATE_PARTIAL,     "state.label.partial"},
            new String[]{EvalConstants.EVALUATION_STATE_INQUEUE,     "state.label.inqueue"},
            new String[]{EvalConstants.EVALUATION_STATE_ACTIVE,      "state.label.active"},
            new String[]{EvalConstants.EVALUATION_STATE_GRACEPERIOD, "state.label.graceperiod"},
            new String[]{EvalConstants.EVALUATION_STATE_CLOSED,      "state.label.closed"}
        ));
    }

    private String validateCron(String cronExpression, String stateList) {
        if (cronExpression == null || cronExpression.trim().isEmpty())
            return "administrate.sync.cronExpression.null";
        if (!CronExpression.isValidExpression(cronExpression.trim()))
            return "administrate.sync.cronExpression.err";
        if (stateList == null || stateList.trim().isEmpty())
            return "administrate.sync.stateList.null";
        return null;
    }

    private void scheduleCronJob(String cronExpression, String stateList) {
        String uniqueId = EvalUtils.makeUniqueIdentifier(99);
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put(EvalConstants.CRON_SCHEDULER_TRIGGER_NAME,  uniqueId);
        dataMap.put(EvalConstants.CRON_SCHEDULER_TRIGGER_GROUP, JOB_GROUP);
        dataMap.put(EvalConstants.CRON_SCHEDULER_JOB_NAME,      uniqueId);
        dataMap.put(EvalConstants.CRON_SCHEDULER_JOB_GROUP,     JOB_GROUP);
        dataMap.put(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION, cronExpression);
        dataMap.put(EvalConstants.CRON_SCHEDULER_SPRING_BEAN_NAME, GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_BEAN_NAME);
        dataMap.put(GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST, stateList);
        commonLogic.scheduleCronJob(GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_BEAN_NAME, dataMap);
    }

    private void checkAdmin() {
        if (!isCurrentUserAdmin())
            throw new SecurityException("Non-admin users may not access this page");
    }
}