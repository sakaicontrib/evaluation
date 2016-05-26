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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.tool.producers.ImportConfigProducer;
import org.springframework.web.multipart.MultipartFile;

/**
 * Parses the incoming properties file and then supplies the results to the classes that need to handle the results.
 * 
 * @author chasegawa
 */
public class ConfigSettingsPropertiesFileParser {
    private static final Log LOG = LogFactory.getLog(ConfigSettingsPropertiesFileParser.class);

    /**
     * @return "uploadSuccess" if the parsing was completed without issue. Otherwise, return "uploadFailure".
     */
    public String parse() {
        MultipartFile file = (MultipartFile) multipartMap.get("configFile");
        HashMap<String, String> result = new HashMap<>();
        /* We manually parse the properties file rather than using the java.util.Properties due to the keys having a ':' in
         * the string (':' is one of the three valid separator values in the Java properties file definition).
         */
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            while (reader.ready()) {
                String linein = reader.readLine();
                String[] keyValuePair = linein.split("=");
                result.put(keyValuePair[0], keyValuePair[1]);
            }
        } catch (IOException ioe) {
            LOG.error("Error reading uploaded properties file for overwrite settings", ioe);
            return "uploadFailure";
        } finally {
            IOUtils.closeQuietly(reader);
        }
        // Update the producer so it can show the results on the html template
        importConfigProducer.setUploadedConfigValues(result);
        // Update the handler so the settings can be saved if the user chooses to do so
        overwriteSettingHandler.setUploadedConfigValues(result);
        return "uploadSuccess";
    }

    private ImportConfigProducer importConfigProducer;

    public void setImportConfigProducer(ImportConfigProducer importConfigProducer) {
        this.importConfigProducer = importConfigProducer;
    }

    @SuppressWarnings("rawtypes")
    private Map multipartMap;

    @SuppressWarnings("rawtypes")
    public void setMultipartMap(Map multipartMap) {
        this.multipartMap = multipartMap;
    }

    private OverwriteSettingHandler overwriteSettingHandler;

    public void setOverwriteSettingHandler(OverwriteSettingHandler overwriteSettingConfiguration) {
        this.overwriteSettingHandler = overwriteSettingConfiguration;
    }
}
