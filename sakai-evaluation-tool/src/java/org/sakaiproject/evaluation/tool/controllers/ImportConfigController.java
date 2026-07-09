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
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/import_config")
@SessionAttributes("uploadedConfig")
public class ImportConfigController extends EvalControllerSupport {

    @Data
    public static class SettingRow {
        private final String propertyName;
        private final String currentValue;
        private final String incomingValue; // empty if no file has been uploaded
    }

    @ModelAttribute("uploadedConfig")
    public Map<String, String> uploadedConfig() {
        return new HashMap<>();
    }

    @GetMapping
    public String show(@ModelAttribute("uploadedConfig") Map<String, String> uploadedConfig, Model model) {
        checkAdmin();
        model.addAttribute("settingRows", buildRows(uploadedConfig));
        model.addAttribute("hasUpload", !uploadedConfig.isEmpty());
        return "import_config";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("configFile") MultipartFile configFile,
                         @ModelAttribute("uploadedConfig") Map<String, String> uploadedConfig) {
        checkAdmin();
        uploadedConfig.clear();
        if (!configFile.isEmpty()) {
            try {
                Properties props = new Properties();
                props.load(configFile.getInputStream());
                props.forEach((k, v) -> uploadedConfig.put(k.toString(), v.toString()));
                log.info("Admin ({}) uploaded config file with {} entries", currentUserId(), uploadedConfig.size());
            } catch (IOException e) {
                log.error("Error reading uploaded config file", e);
            }
        }
        return "redirect:/import_config";
    }

    @PostMapping("/overwrite")
    public String overwrite(@ModelAttribute("uploadedConfig") Map<String, String> uploadedConfig,
                            SessionStatus sessionStatus) {
        checkAdmin();
        for (Map.Entry<String, String> entry : uploadedConfig.entrySet()) {
            String key = entry.getKey();
            String rawValue = entry.getValue();
            try {
                settings.set(key, parseValue(key, rawValue));
            } catch (Exception e) {
                log.warn("Could not set setting {}: {}", key, e.getMessage());
            }
        }
        settings.resetCache(null);
        sessionStatus.setComplete();
        log.info("Admin ({}) overwrote {} settings from uploaded config", currentUserId(), uploadedConfig.size());
        return "redirect:/administrate";
    }

    private List<SettingRow> buildRows(Map<String, String> uploadedConfig) {
        List<SettingRow> rows = new ArrayList<>();
        Field[] fields = EvalSettings.class.getFields();
        Arrays.sort(fields, (a, b) -> a.getName().compareTo(b.getName()));
        for (Field field : fields) {
            if (!String.class.equals(field.getType())) continue;
            try {
                String key = field.get(null).toString();
                Object current = settings.get(key);
                String currentStr = current != null ? current.toString() : "null";
                String incoming = uploadedConfig.getOrDefault(key, "");
                rows.add(new SettingRow(key, currentStr, incoming));
            } catch (IllegalAccessException e) {
                log.warn("Cannot access EvalSettings field {}", field.getName());
            }
        }
        return rows;
    }

    private Object parseValue(String key, String rawValue) {
        if (rawValue == null || "null".equalsIgnoreCase(rawValue.trim())) return null;
        String val = rawValue.trim();
        if (key.contains(":java.lang.Boolean")) return Boolean.parseBoolean(val);
        if (key.contains(":java.lang.Integer")) {
            try { return Integer.parseInt(val); } catch (NumberFormatException e) { return null; }
        }
        if (key.contains(":java.util.Date")) {
            try { return new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy").parse(val); }
            catch (ParseException e) { return null; }
        }
        return val; // String
    }

    private void checkAdmin() {
        if (!isCurrentUserAdmin())
            throw new SecurityException("Non-admin users may not access this page");
    }
}