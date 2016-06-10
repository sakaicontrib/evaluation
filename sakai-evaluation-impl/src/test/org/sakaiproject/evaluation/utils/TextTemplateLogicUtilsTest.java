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

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Testing the template processing logic
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TextTemplateLogicUtilsTest extends TestCase {

   private static final String PLAIN_TEMPALTE = "This template has nothing in it that can be replaced and therefore should come out identical \n" +
      "to the one that was input. If it does not come out the same then this is sadly quite broken";

   private static final String SAMPLE1 = "This sample template has information that can be replaced. For example, this sentence:\n" +
   		"Welcome ${name}, Your email address is very special. It is ${email}. We like it so much we would like to hire the " +
   		"company you are working for (${company}) to do something for us.\n Sincerly, Some guy";
   private static final String RESULT1 = "This sample template has information that can be replaced. For example, this sentence:\n" +
         "Welcome Aaron Zeckoski, Your email address is very special. It is aaronz@vt.edu. We like it so much we would like to hire the " +
         "company you are working for (CARET, University of Cambridge) to do something for us.\n Sincerly, Some guy";

   private static final String SAMPLE2 = "This sample template has information that can be replaced. For example, this sentence:\n" +
         "Welcome ${name}, Your email address is very special. It is ${email}. We like it so much we would like to hire the " +
         "company you are working for (${company}) to do something for us.\n Sincerly, ${author}";

   public void runTestingProcessTextTemplate() {
      Map<String, String> replacementValues;
      String result;

      Map<String, String> rVals = new HashMap<>();
      rVals.put("name", "Aaron Zeckoski");
      rVals.put("email", "aaronz@vt.edu");
      rVals.put("company", "CARET, University of Cambridge");
      rVals.put("extra", "EXTRA");

      // make sure that a plain template remains unchanged
      replacementValues = rVals;
      result = TextTemplateLogicUtils.processTextTemplate(PLAIN_TEMPALTE, replacementValues);
      assertNotNull(result);
      assertEquals(PLAIN_TEMPALTE, result);

      // make sure that a plain template works with null replacement values
      replacementValues = null;
      result = TextTemplateLogicUtils.processTextTemplate(PLAIN_TEMPALTE, replacementValues);
      assertNotNull(result);
      assertEquals(PLAIN_TEMPALTE, result);

      // make sure a plain template works ok with empty replacement values
      replacementValues = new HashMap<>();
      result = TextTemplateLogicUtils.processTextTemplate(PLAIN_TEMPALTE, replacementValues);
      assertNotNull(result);
      assertEquals(PLAIN_TEMPALTE, result);

      // make sure a normal replacement works
      replacementValues = rVals;
      result = TextTemplateLogicUtils.processTextTemplate(SAMPLE1, replacementValues);
      assertNotNull(result);
      assertEquals(RESULT1, result);

      // check for expected failures
      try {
         TextTemplateLogicUtils.processTextTemplate(null, replacementValues);
         fail("Should not have gotten here");
      } catch (RuntimeException e) {
         assertNotNull(e.getMessage());
      }

      // processing template with a missing replacement value causes failure
      try {
         TextTemplateLogicUtils.processTextTemplate(SAMPLE2, replacementValues);
         fail("Should not have gotten here");
      } catch (RuntimeException e) {
         assertNotNull(e.getMessage());
      }

   }

   public void testFreemarkerTextTemplate() {
      TextTemplateLogicUtils.useFreemarker = true;
      TextTemplateLogicUtils.useVelocity = false;

      runTestingProcessTextTemplate();

      // test freemarker if statements
      String result;

      Map<String, String> replacementValues = new HashMap<>();
      replacementValues.put("name", "Aaron Zeckoski");
      replacementValues.put("ShowSomething", "false");

      String sampleIf = 
         "This sample template has information that can be optionally shown:\n" +
         "Welcome ${name}, You will optionally be shown something:\n" +
         "<#if (ShowSomething == \"true\")>\n" +
         "This is optionally shown\n" +
         "</#if>\n";
      String resultIf = 
         "This sample template has information that can be optionally shown:\n" +
         "Welcome Aaron Zeckoski, You will optionally be shown something:\n" +
         "This is optionally shown\n";
      String resultNotIf = 
         "This sample template has information that can be optionally shown:\n" +
         "Welcome Aaron Zeckoski, You will optionally be shown something:\n";

      result = TextTemplateLogicUtils.processTextTemplate(sampleIf, replacementValues);
      assertNotNull(result);
      assertEquals(resultNotIf, result);

      replacementValues.put("ShowSomething", "true");

      result = TextTemplateLogicUtils.processTextTemplate(sampleIf, replacementValues);
      assertNotNull(result);
      assertEquals(resultIf, result);
   }

   public void testVelocityTextTemplate() {
      TextTemplateLogicUtils.useFreemarker = false;
      TextTemplateLogicUtils.useVelocity = true;

      runTestingProcessTextTemplate();

      // test velocity if statements
      String result;

      Map<String, String> replacementValues = new HashMap<>();
      replacementValues.put("name", "Aaron Zeckoski");
      replacementValues.put("ShowSomething", "false");

      String sampleIf = 
         "This sample template has information that can be optionally shown:\n" +
         "Welcome ${name}, You will optionally be shown something:\n" +
         "#if ($ShowSomething == \"true\")\n" +
         "This is optionally shown\n" +
         "#end\n";
      String resultIf = 
         "This sample template has information that can be optionally shown:\n" +
         "Welcome Aaron Zeckoski, You will optionally be shown something:\n" +
         "This is optionally shown\n";
      String resultNotIf = 
         "This sample template has information that can be optionally shown:\n" +
         "Welcome Aaron Zeckoski, You will optionally be shown something:\n";

      result = TextTemplateLogicUtils.processTextTemplate(sampleIf, replacementValues);
      assertNotNull(result);
      assertEquals(resultNotIf, result);

      replacementValues.put("ShowSomething", "true");

      result = TextTemplateLogicUtils.processTextTemplate(sampleIf, replacementValues);
      assertNotNull(result);
      assertEquals(resultIf, result);

      // back to defaults (required for maven 2)
      TextTemplateLogicUtils.useFreemarker = true;
      TextTemplateLogicUtils.useVelocity = false;
   }

}
