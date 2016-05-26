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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.locators.SettingsWBL;

import uk.org.ponder.dateutil.LocalSDF;

/**
 * OverwriteSettingHandler handles saving the the "override settings" uploaded by a user.
 * @author chasegawa
 */
public class OverwriteSettingHandler {
    private boolean isTernaryBoolean(String path) {
        boolean isTernary = false;
        for( String TERNARY_BOOLEAN_SETTINGS : EvalSettings.TERNARY_BOOLEAN_SETTINGS )
        {
            if( TERNARY_BOOLEAN_SETTINGS.equals( path ) )
            {
                isTernary = true;
                break;
            }
        }
        return isTernary;
    }

    /**
     * Update the settings using the values supplied from a user upload.
     * @return 
     */
    public String saveOverwriteSettings() {
        // Don't do anything if for some reason there are no settings.
        if (uploadedConfigValues.keySet().size() > 0) {
            for (String key : uploadedConfigValues.keySet()) {
                String value = uploadedConfigValues.get(key);
                // The SettingsWBL wont accept the actual value for the db, so we have to translate it so it can be re-translated
                if (isTernaryBoolean(key)) {
                    value = translateToTernaryValue(value);
                }
                // Make sure "null"s and empties go in as a true null        
                if (key.contains( "java.util.Date" )) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                    LocalSDF localSDF = new LocalSDF();
                    try {
                        value = localSDF.format(sdf.parse(value));
                    } catch (ParseException ignore) {
                    }
                }
                value = (StringUtils.isEmpty(value) || "null".equalsIgnoreCase(value)) ? null : value;
                settings.set(key, value);
            }
        }
        // Reset this now that we are done with it.
        uploadedConfigValues = new HashMap<>();
        settings.resetConfigCache();
        return "overwriteSuccess";
    }

    private SettingsWBL settings;
    public void setSettingsBean(SettingsWBL settings) {
        this.settings = settings;
    }

    private static HashMap<String, String> uploadedConfigValues = new HashMap<>();
    public void setUploadedConfigValues(HashMap<String, String> newValues) {
        OverwriteSettingHandler.uploadedConfigValues = newValues;
    }

    private String translateToTernaryValue(String value) {
        if ("true".equalsIgnoreCase(value)) return EvalToolConstants.ADMIN_BOOLEAN_YES;
        if ("false".equalsIgnoreCase(value)) return EvalToolConstants.ADMIN_BOOLEAN_NO;
        return EvalToolConstants.ADMIN_BOOLEAN_CONFIGURABLE;
    }
}
