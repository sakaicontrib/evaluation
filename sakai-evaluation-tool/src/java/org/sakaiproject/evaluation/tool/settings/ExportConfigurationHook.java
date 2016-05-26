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

package org.sakaiproject.evaluation.tool.settings;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalSettings;

/**
 * ExportConfigurationHook handles the request from the system to export the current Evaluation settings as a properties
 * file.
 * 
 * @author chasegawa
 */
public class ExportConfigurationHook {
    public static final String VIEW_ID = "export_settings";
    private static final Log LOG = LogFactory.getLog(ExportConfigurationHook.class);

    /**
     * This is required in order to avoid runtime reflection error.
     */
    public void doNothing() {
    }

    private String getEvalSettingConstValueByFieldName(String propertyFieldName) {
        try {
            return EvalSettings.class.getDeclaredField(propertyFieldName).get(String.class).toString();
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            LOG.warn( e );
            return "Error getting value";
        }
    }

    /**
     * @return The list of Strings that is the names of all the individual properties fields in EvalSettings.java
     */
    private ArrayList<String> getSortedEvalSettingsPropertyFieldNames() {
        ArrayList<String> result = new ArrayList<>();
        Field[] evalSettingFields = EvalSettings.class.getFields();
        Arrays.sort(evalSettingFields, (Field o1, Field o2) -> o1.getName().compareTo(o2.getName()));
        for (Field field : evalSettingFields) {
            // Ignore the arrays of String in EvalSettings (they just aggregate the types) and just get the String constants
            if (String.class.equals(field.getType())) {
                result.add(field.getName());
            }
        }
        return result;
    }

    public boolean handle() throws ParseException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"evalSettings.properties\"");
        ArrayList<String> propertyFieldNames = getSortedEvalSettingsPropertyFieldNames();
        ServletOutputStream out;
        try {
            out = response.getOutputStream();
            for (String propertyFieldName : propertyFieldNames) {
                String constValue = getEvalSettingConstValueByFieldName(propertyFieldName);
                out.write(constValue.getBytes());
                out.write("=".getBytes());
                Object settingVal = evalSettings.get(constValue);
                // When there is no value, we need to output something or the properties file will not work
                out.write(null == settingVal ? "null".getBytes() : settingVal.toString().getBytes());
                out.write("\n".getBytes());
            }
        } catch (IOException e) {
            LOG.error("Error producing output stream for evalSettings.properties", e);
            return false;
        }
        return true;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    private HttpServletResponse response;
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }
}
