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

   private static final Log LOG = LogFactory.getLog(MockEvalJobLogic.class);

   public void jobAction(Long evaluationId, String jobType) {
      LOG.info("MOCK: jobAction(evaluationId="+evaluationId+", jobType="+jobType+")");
      // pretend all is ok and do nothing
   }

   public void processEvaluationStateChange(Long evaluationId, String actionState) {
      LOG.info("MOCK: jobAction(evaluationId="+evaluationId+", actionState="+actionState+")");
      // pretend all is ok and do nothing
   }

}
