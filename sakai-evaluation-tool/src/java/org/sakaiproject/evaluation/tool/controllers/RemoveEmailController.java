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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.extern.slf4j.Slf4j;

/**
 * Deletes an email template. Spring MVC equivalent of remove_email (RSF action).
 */
@Slf4j
@Controller
@RequestMapping("/remove_email")
public class RemoveEmailController extends EvalControllerSupport {

    @PostMapping
    public String remove(@RequestParam Long emailTemplateId) {
        evaluationSetupService.removeEmailTemplate(emailTemplateId, currentUserId());
        return "redirect:/control_email_templates";
    }
}