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
package org.sakaiproject.evaluation.logic;

import org.junit.Assert;
import org.junit.Before;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestDataImpl;
import org.sakaiproject.evaluation.test.mocks.MockEvalExternalLogic;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;


/**
 * This is the logic test base,
 * the other logic tests can extend this to avoid having as much duplicated code<br/>
 * This does the following:<br/>
 * Loads up the DAO and preloads the test data<br/>
 * Loads the following spring contexts: hibernate-test.xml, spring-hibernate.xml, logic-support.xml<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@DirtiesContext
@ContextConfiguration(locations={
		"/hibernate-test.xml",
		"classpath:org/sakaiproject/evaluation/spring-hibernate.xml",
		"classpath:org/sakaiproject/evaluation/logic-support.xml"})
public abstract class BaseTestEvalLogic extends AbstractTransactionalJUnit4SpringContextTests {

   protected EvaluationDao evaluationDao;
   protected EvalCommonLogic commonLogic;
   protected MockEvalExternalLogic externalLogic;
   protected EvalTestDataLoad etdl;

   @Before
   public void onSetUpBeforeTransaction() throws Exception {

      // load the spring created dao class bean from the Spring Application Context
      evaluationDao = (EvaluationDao) applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationDao");
      if (evaluationDao == null) {
         throw new NullPointerException("DAO could not be retrieved from spring context");
      }

      externalLogic = (MockEvalExternalLogic) applicationContext.getBean("org.sakaiproject.evaluation.logic.externals.EvalExternalLogic");
      if (externalLogic == null) {
         throw new NullPointerException("externalLogic could not be retrieved from spring context");
      }

      commonLogic = (EvalCommonLogic) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalCommonLogic");
      if (commonLogic == null) {
         throw new NullPointerException("commonLogic could not be retrieved from spring context");
      }

      // check the preloaded test data
      Assert.assertTrue("Error preloading test data", evaluationDao.countAll(EvalEvaluation.class) > 0);

      PreloadTestDataImpl ptd = (PreloadTestDataImpl) applicationContext.getBean("org.sakaiproject.evaluation.test.PreloadTestData");
      if (ptd == null) {
         throw new NullPointerException("PreloadTestDataImpl could not be retrieved from spring context");
      }

      // get test objects
      etdl = ptd.getEtdl();
      if (etdl.scale1.getId() == null) {
         throw new IllegalStateException("Failure in loadup of data");
      }

   }
}
