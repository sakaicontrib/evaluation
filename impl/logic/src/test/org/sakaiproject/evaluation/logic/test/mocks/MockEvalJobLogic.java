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

package org.sakaiproject.evaluation.logic.test.mocks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;


/**
 * This is a stub to let us test the classes that depend on this.
 * The majority of these methods have no real returns so it is easy to simply pretend
 * like they are working.
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class MockEvalJobLogic implements EvalJobLogic {

   private static Log log = LogFactory.getLog(MockEvalJobLogic.class);

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#isJobTypeScheduled(java.lang.Long, java.lang.String)
    */
   public boolean isJobTypeScheduled(Long evaluationId, String jobType) {
      log.info("MOCK: isJobTypeScheduled(evaluationId="+evaluationId+", jobType="+jobType+")");
      return true;
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#jobAction(java.lang.Long, java.lang.String)
    */
   public void jobAction(Long evaluationId, String jobType) {
      log.info("MOCK: jobAction(evaluationId="+evaluationId+", jobType="+jobType+")");
      // pretend all is ok and do nothing
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#processEvaluationChange(org.sakaiproject.evaluation.model.EvalEvaluation)
    */
   public void processEvaluationChange(EvalEvaluation eval) {
      log.info("MOCK: processEvaluationChange(eval="+eval.getTitle()+")");
      // pretend all is ok and do nothing
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#processNewEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)
    */
   public void processNewEvaluation(EvalEvaluation eval) {
      log.info("MOCK: processNewEvaluation(eval="+eval.getTitle()+")");
      // pretend all is ok and do nothing
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#removeScheduledInvocations(java.lang.Long)
    */
   public void removeScheduledInvocations(Long evaluationId) {
      log.info("MOCK: removeScheduledInvocations(evaluationId="+evaluationId+")");
      // pretend all is ok and do nothing
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.externals.EvalJobLogic#scheduleReminder(java.lang.Long)
    */
   public void scheduleReminder(Long evaluationId) {
      log.info("MOCK: scheduleReminder(evaluationId="+evaluationId+")");
      // pretend all is ok and do nothing
   }

}
