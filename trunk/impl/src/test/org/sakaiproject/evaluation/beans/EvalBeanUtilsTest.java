/**
 * $Id$
 * $URL$
 * EvalBeanUtils.java - evaluation - Feb 25, 2008 10:46:20 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.beans;

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

   // run this before each test starts
   protected void onSetUpBeforeTransaction() throws Exception {
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
   public void testGetResponsesNeededToViewForResponseRate() {
      int systemResponseRate = (Integer) settings.get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS);
      int responsesCount = 0;
      int enrollmentsCount = 0;

      responsesCount = systemResponseRate + 2;
      enrollmentsCount = 10;
      assertTrue( evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount) == 0 );

      responsesCount = systemResponseRate - 2;
      enrollmentsCount = 10;
      assertTrue( evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount) > 0 );

      responsesCount = systemResponseRate - 2;
      enrollmentsCount = responsesCount;
      assertTrue( evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount) == 0 );

      responsesCount = systemResponseRate + 2;
      enrollmentsCount = 0;
      assertTrue( evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount) == 0 );

      responsesCount = systemResponseRate - 2;
      enrollmentsCount = 0;
      assertTrue( evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount) == 0 );

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.beans.EvalBeanUtils#checkUserPermission(java.lang.String, java.lang.String)}.
    */
   public void testCheckUserPermission() {

      assertTrue( evalBeanUtils.checkUserPermission("admin", "aaronz") );

      assertTrue( evalBeanUtils.checkUserPermission("aaronz", "aaronz") );

      assertFalse( evalBeanUtils.checkUserPermission("aaronz", "not-aaronz") );

   }

}
