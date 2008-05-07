/**
 * $Id$
 * $URL$
 * TextTemplateLogicUtilsTest.java - evaluation - Jan 16, 2008 5:12:13 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.utils;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.sakaiproject.evaluation.utils.TextTemplateLogicUtils;


/**
 * Testing the template processing logic
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TextTemplateLogicUtilsTest extends TestCase {

   private String plainTemplate = "This template has nothing in it that can be replaced and therefore should come out identical \n" +
      "to the one that was input. If it does not come out the same then this is sadly quite broken";

   private String sample1 = "This sample template has information that can be replaced. For example, this sentence:\n" +
   		"Welcome ${name}, Your email address is very special. It is ${email}. We like it so much we would like to hire the " +
   		"company you are working for (${company}) to do something for us.\n Sincerly, Some guy";
   private String result1 = "This sample template has information that can be replaced. For example, this sentence:\n" +
         "Welcome Aaron Zeckoski, Your email address is very special. It is aaronz@vt.edu. We like it so much we would like to hire the " +
         "company you are working for (CARET, University of Cambridge) to do something for us.\n Sincerly, Some guy";

   private String sample2 = "This sample template has information that can be replaced. For example, this sentence:\n" +
         "Welcome ${name}, Your email address is very special. It is ${email}. We like it so much we would like to hire the " +
         "company you are working for (${company}) to do something for us.\n Sincerly, ${author}";

   public void runTestingProcessTextTemplate() {
      Map<String, String> replacementValues = null;
      String result = null;

      Map<String, String> rVals = new HashMap<String, String>();
      rVals.put("name", "Aaron Zeckoski");
      rVals.put("email", "aaronz@vt.edu");
      rVals.put("company", "CARET, University of Cambridge");
      rVals.put("extra", "EXTRA");

      // make sure that a plain template remains unchanged
      replacementValues = rVals;
      result = TextTemplateLogicUtils.processTextTemplate(plainTemplate, replacementValues);
      assertNotNull(result);
      assertEquals(plainTemplate, result);

      // make sure that a plain template works with null replacement values
      replacementValues = null;
      result = TextTemplateLogicUtils.processTextTemplate(plainTemplate, replacementValues);
      assertNotNull(result);
      assertEquals(plainTemplate, result);

      // make sure a plain template works ok with empty replacement values
      replacementValues = new HashMap<String, String>();
      result = TextTemplateLogicUtils.processTextTemplate(plainTemplate, replacementValues);
      assertNotNull(result);
      assertEquals(plainTemplate, result);

      // make sure a normal replacement works
      replacementValues = rVals;
      result = TextTemplateLogicUtils.processTextTemplate(sample1, replacementValues);
      assertNotNull(result);
      assertEquals(result1, result);

      // check for expected failures
      try {
         result = TextTemplateLogicUtils.processTextTemplate(null, replacementValues);
         fail("Should not have gotten here");
      } catch (RuntimeException e) {
         assertNotNull(e.getMessage());
      }

      // processing template with a missing replacement value causes failure
      try {
         result = TextTemplateLogicUtils.processTextTemplate(sample2, replacementValues);
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
      String result = null;

      Map<String, String> replacementValues = new HashMap<String, String>();
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
      String result = null;

      Map<String, String> replacementValues = new HashMap<String, String>();
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
