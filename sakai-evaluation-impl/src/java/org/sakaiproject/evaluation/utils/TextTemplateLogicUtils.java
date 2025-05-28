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
package org.sakaiproject.evaluation.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;


import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a weird location but it will have to do for now,
 * this handles processing of text templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Slf4j
public class TextTemplateLogicUtils {

    public static final Object LOCK = new Object();

    public final static int TOTAL_RESET_COUNT = 500;
    /**
     * Used to track and reset the processor every totalResetCount runs
     */
    public static AtomicInteger resetCounter = new AtomicInteger(0);

    /**
     * Handles the replacement of the variable strings within textual templates and
     * also allows the setting of variables for the control of logical branching within
     * the text template as well<br/>
     * Uses and expects freemarker (http://freemarker.org/) style templates 
     * (that is using ${name} as the marker for a replacement)<br/>
     * 
     * @param textTemplate a freemarker style text template,
     * cannot be null or empty string
     * @param replacementValues a set of replacement values which are in the map like so:<br/>
     * key => value (String => Object)<br/>
     * username => aaronz<br/>
     * course_title => Math 1001 Differential Equations<br/>
     * @return the processed template
     */
    public static String processTextTemplate(String textTemplate, Map<String, String> replacementValues) {
        if (replacementValues == null || replacementValues.isEmpty()) {
            return textTemplate;
        }

        if (textTemplate == null || textTemplate.equals("")) {
            throw new IllegalArgumentException("The textTemplate cannot be null or empty string, " +
            "please pass in at least something in the template or do not call this method");
        }

        return processFreemarkerTextTemplate(textTemplate, replacementValues);
    }

    private static Configuration freemarkerConfig = null;
    private static String processFreemarkerTextTemplate(String textTemplate, Map<String, String> replacementValues) {
        // setup freemarker if it is not already done
        synchronized (LOCK) {
            if (freemarkerConfig == null || resetCounter.getAndAdd(1) > TOTAL_RESET_COUNT) {
                freemarkerConfig = new Configuration();
                // Specify how templates will see the data-model
                freemarkerConfig.setObjectWrapper(new DefaultObjectWrapper());
                resetCounter.getAndSet(0);
                log.info("Constructed new freemarker template for template processing");
            }
        }

        // get the template
        Template template;
        try {
            template = new Template("textProcess", new StringReader(textTemplate), freemarkerConfig);
            if (replacementValues == null) return textTemplate;
        } catch (IOException e) {
            throw new RuntimeException("Failure while creating freemarker template", e);
        }

        Writer output = new StringWriter();
        try {
            template.process(replacementValues, output);
        } catch (TemplateException e) {
            throw new RuntimeException("Failure while processing freemarker template", e);
        } catch (IOException e) {
            throw new RuntimeException("Failure while sending freemarker output to stream", e);
        }

        return output.toString();
    }
    
    public static boolean checkTextTemplate(String textTemplate) {

        if (textTemplate == null || textTemplate.equals("")) {
        	return false;
        }
        
        try
        {
        	String validTemplate = processFreemarkerTextTemplate(textTemplate, null);
        }
        catch (RuntimeException e)
        {
        	return false;
        }
        return true;
    }


}
