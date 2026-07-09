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

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/export_settings")
public class ExportSettingsController extends EvalControllerSupport {

    @GetMapping
    public void export(HttpServletResponse response) throws IOException {
        if (!isCurrentUserAdmin())
            throw new SecurityException("Non-admin users may not access this page");

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"settings.properties\"");

        Field[] fields = EvalSettings.class.getFields();
        Arrays.sort(fields, (a, b) -> a.getName().compareTo(b.getName()));

        PrintWriter out = response.getWriter();
        for (Field field : fields) {
            if (!String.class.equals(field.getType())) continue;
            try {
                String key = field.get(null).toString();
                Object val = settings.get(key);
                out.println(key + "=" + (val != null ? val.toString() : "null"));
            } catch (IllegalAccessException e) {
                log.warn("Cannot access field {}", field.getName());
            }
        }
    }
}