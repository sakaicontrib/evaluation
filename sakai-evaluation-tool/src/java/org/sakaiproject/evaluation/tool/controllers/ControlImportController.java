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

import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.evaluation.logic.imports.EvalImportLogic;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/control_import")
public class ControlImportController extends EvalControllerSupport {
    // Retrieved via ComponentManager because they live in the RSF context, not the MVC one
    private EvalImportLogic getImportLogic() {
        return (EvalImportLogic) ComponentManager.get(EvalImportLogic.class.getName());
    }

    private ContentHostingService getChs() {
        return (ContentHostingService) ComponentManager.get(ContentHostingService.class.getName());
    }

    @GetMapping
    public String show(Model model) {
        checkAdmin();
        return "control_import";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("xmlFile") MultipartFile xmlFile,
                         RedirectAttributes ra) {
        checkAdmin();

        if (xmlFile == null || xmlFile.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "control.import.error.nofile");
            return "redirect:/control_import";
        }

        String resourceId = null;
        try {
            ContentHostingService chs = getChs();
            // Temporarily store in CHS as an attachment
            ContentResourceEdit resource = chs.addAttachmentResource(
                    xmlFile.getOriginalFilename() != null ? xmlFile.getOriginalFilename() : "import.xml");
            resource.setContent(xmlFile.getInputStream());
            resource.setContentType("text/xml");
            chs.commitResource(resource);
            resourceId = resource.getId();

            // Process the XML file
            List<String> messages = getImportLogic().load(resourceId);
            ra.addFlashAttribute("importMessages", messages);
            log.info("Admin ({}) imported XML data: {} messages", currentUserId(), messages.size());

        } catch (Exception e) {
            log.error("Error during XML data import", e);
            ra.addFlashAttribute("errorMessage", "control.import.error.exception");
            ra.addFlashAttribute("errorDetail", e.getMessage());
        } finally {
            // Clean up the temporary CHS resource
            if (resourceId != null) {
                try { getChs().removeResource(resourceId); }
                catch (Exception ignored) {}
            }
        }
        return "redirect:/control_import";
    }

    private void checkAdmin() {
        if (!isCurrentUserAdmin())
            throw new SecurityException("Non-admin users may not access this page");
    }
}