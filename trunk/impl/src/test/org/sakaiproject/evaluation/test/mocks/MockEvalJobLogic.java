/**
 * MockEvalJobLogic.java - evaluation - Jan 25, 2008 10:07:31 AM - azeckoski
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.test.mocks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;


/**
 * This is a stub to let us test the classes that depend on this.
 * The majority of these methods have no real returns so it is easy to simply pretend
 * like they are working.
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class MockEvalJobLogic implements EvalJobLogic {

   private static Log log = LogFactory.getLog(MockEvalJobLogic.class);

   public void jobAction(Long evaluationId, String jobType) {
      log.info("MOCK: jobAction(evaluationId="+evaluationId+", jobType="+jobType+")");
      // pretend all is ok and do nothing
   }

   public void processEvaluationStateChange(Long evaluationId, String actionState) {
      log.info("MOCK: jobAction(evaluationId="+evaluationId+", actionState="+actionState+")");
      // pretend all is ok and do nothing
   }

}
