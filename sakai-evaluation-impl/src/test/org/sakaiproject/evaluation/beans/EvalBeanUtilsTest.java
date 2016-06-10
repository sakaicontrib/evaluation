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
package org.sakaiproject.evaluation.beans;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.evaluation.logic.BaseTestEvalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;

/**
 * Testing the bean utils
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalBeanUtilsTest extends BaseTestEvalLogic {

   protected EvalBeanUtils evalBeanUtils;
   protected EvalSettings settings;
   
   public EvalBeanUtilsTest() {
   	
   }

   // run this before each test starts
   @Before
   public void onSetUpBeforeTransaction() throws Exception {
      super.onSetUpBeforeTransaction();

      // load up any other needed spring beans
      settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
      if (settings == null) {
         throw new NullPointerException("EvalSettings could not be retrieved from spring context");
      }

      // setup the mock objects if needed

      // create and setup the object to be tested
      evalBeanUtils = new EvalBeanUtils();
      evalBeanUtils.setCommonLogic(commonLogic);
      evalBeanUtils.setSettings(settings);

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.beans.EvalBeanUtils#getResponsesNeededToViewForResponseRate(int, int)}.
    */
   @Test
   public void testGetResponsesNeededToViewForResponseRate() {
      int systemResponseRate = (Integer) settings.get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS);
      int responsesCount;
      int enrollmentsCount;

      responsesCount = systemResponseRate + 2;
      enrollmentsCount = 10;
      Assert.assertTrue( evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount) == 0 );

      responsesCount = systemResponseRate - 2;
      enrollmentsCount = 10;
      Assert.assertTrue( evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount) > 0 );

      responsesCount = systemResponseRate - 2;
      enrollmentsCount = responsesCount;
      Assert.assertTrue( evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount) == 0 );

      responsesCount = systemResponseRate + 2;
      enrollmentsCount = 0;
      Assert.assertTrue( evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount) == 0 );

      responsesCount = systemResponseRate - 2;
      enrollmentsCount = 0;
      Assert.assertTrue( evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount) == 0 );

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.beans.EvalBeanUtils#checkUserPermission(java.lang.String, java.lang.String)}.
    */
   @Test
   public void testCheckUserPermission() {

	  Assert.assertTrue( evalBeanUtils.checkUserPermission("admin", "aaronz") );

	  Assert.assertTrue( evalBeanUtils.checkUserPermission("aaronz", "aaronz") );

	  Assert.assertFalse( evalBeanUtils.checkUserPermission("aaronz", "not-aaronz") );

   }

}
