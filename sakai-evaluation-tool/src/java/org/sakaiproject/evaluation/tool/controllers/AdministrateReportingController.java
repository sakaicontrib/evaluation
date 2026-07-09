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

import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/administrate_reporting")
public class AdministrateReportingController extends EvalControllerSupport {

    @GetMapping
    public String show(Model model) {
        checkAdmin();

        model.addAttribute("enableCsvExport",       Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_CSV_REPORT_EXPORT)));
        model.addAttribute("enableXlsExport",       Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_XLS_REPORT_EXPORT)));
        model.addAttribute("enablePdfExport",       Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_PDF_REPORT_EXPORT)));
        model.addAttribute("enableListOfTakers",    Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_LIST_OF_TAKERS_EXPORT)));
        model.addAttribute("enablePdfBanner",       Boolean.TRUE.equals(settings.get(EvalSettings.ENABLE_PDF_REPORT_BANNER)));
        Object bannerLoc = settings.get(EvalSettings.PDF_BANNER_IMAGE_LOCATION);
        model.addAttribute("pdfBannerLocation",     bannerLoc != null ? bannerLoc.toString() : "");

        return "administrate_reporting";
    }

    @PostMapping
    public String save(@RequestParam Map<String, String> params) {
        checkAdmin();

        settings.set(EvalSettings.ENABLE_CSV_REPORT_EXPORT,       params.containsKey("enableCsvExport"));
        settings.set(EvalSettings.ENABLE_XLS_REPORT_EXPORT,       params.containsKey("enableXlsExport"));
        settings.set(EvalSettings.ENABLE_PDF_REPORT_EXPORT,       params.containsKey("enablePdfExport"));
        settings.set(EvalSettings.ENABLE_LIST_OF_TAKERS_EXPORT,   params.containsKey("enableListOfTakers"));
        settings.set(EvalSettings.ENABLE_PDF_REPORT_BANNER,       params.containsKey("enablePdfBanner"));

        String loc = params.getOrDefault("pdfBannerLocation", "").trim();
        settings.set(EvalSettings.PDF_BANNER_IMAGE_LOCATION, loc.isEmpty() ? null : loc);

        log.info("Admin ({}) saved reporting settings", currentUserId());
        return "redirect:/administrate_reporting";
    }

    private void checkAdmin() {
        if (!isCurrentUserAdmin())
            throw new SecurityException("Non-admin users may not access this page");
    }
}