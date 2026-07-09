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
import org.sakaiproject.evaluation.dao.EvaluationAdminSupportDao;
import org.sakaiproject.evaluation.dao.EvaluationAssignmentDao;
import org.sakaiproject.evaluation.dao.EvaluationAuthoringDao;
import org.sakaiproject.evaluation.dao.EvaluationConsolidatedEmailDao;
import org.sakaiproject.evaluation.dao.EvaluationDaoBase;
import org.sakaiproject.evaluation.dao.EvaluationEmailTemplateDao;
import org.sakaiproject.evaluation.dao.EvaluationLockDao;
import org.sakaiproject.evaluation.dao.EvaluationQueryDao;
import org.sakaiproject.evaluation.dao.EvaluationResponseDao;
import org.sakaiproject.evaluation.dao.EvaluationSettingsDao;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.test.EvalTestDaoFixture;
import org.sakaiproject.evaluation.test.EvaluationServiceTestConfiguration;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestDataImpl;
import org.sakaiproject.evaluation.test.mocks.MockEvalExternalLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;


/**
 * This is the logic test base,
 * the other logic tests can extend this to avoid having as much duplicated code<br/>
 * This does the following:<br/>
 * Loads up the DAO and preloads the test data<br/>
 * Loads the evaluation service test configuration<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
@DirtiesContext
@ContextConfiguration(classes = EvaluationServiceTestConfiguration.class)
public abstract class BaseTestEvalLogic extends AbstractTransactionalJUnit4SpringContextTests {

   @Autowired
   protected EvalTestDaoFixture daos;

   protected EvaluationDaoBase persistence;
   protected EvaluationSettingsDao settingsDao;
   protected EvaluationEmailTemplateDao emailTemplateDao;
   protected EvaluationAuthoringDao authoringDao;
   protected EvaluationAdminSupportDao adminSupportDao;
   protected EvaluationResponseDao responseDao;
   protected EvaluationAssignmentDao assignmentDao;
   protected EvaluationQueryDao queryDao;
   protected EvaluationLockDao lockDao;
   protected EvaluationConsolidatedEmailDao consolidatedEmailDao;

   @Autowired
   @Qualifier("org.sakaiproject.evaluation.logic.EvalCommonLogic")
   protected EvalCommonLogic commonLogic;
   @Autowired
   @Qualifier("org.sakaiproject.evaluation.logic.externals.EvalExternalLogic")
   protected MockEvalExternalLogic externalLogic;
   @Autowired
   @Qualifier("org.sakaiproject.evaluation.test.PreloadTestData")
   private PreloadTestDataImpl preloadTestData;
   protected EvalTestDataLoad etdl;

   @Before
   public void onSetUpBeforeTransaction() throws Exception {
      bindDaoFixture(daos);

      // check the preloaded test data
      Assert.assertTrue("Error preloading test data", persistence.countAll(EvalEvaluation.class) > 0);

      // get test objects
      etdl = preloadTestData.getEtdl();
      if (etdl.scale1.getId() == null) {
         throw new IllegalStateException("Failure in loadup of data");
      }

   }

   protected void bindDaoFixture(EvalTestDaoFixture fixture) {
      persistence = fixture.persistence;
      settingsDao = fixture.settingsDao;
      emailTemplateDao = fixture.emailTemplateDao;
      authoringDao = fixture.authoringDao;
      adminSupportDao = fixture.adminSupportDao;
      responseDao = fixture.responseDao;
      assignmentDao = fixture.assignmentDao;
      queryDao = fixture.queryDao;
      lockDao = fixture.lockDao;
      consolidatedEmailDao = fixture.consolidatedEmailDao;
   }
}
