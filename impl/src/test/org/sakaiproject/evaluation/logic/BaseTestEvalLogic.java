/**
 * $Id$
 * $URL$
 * BaseTestEvalLogic.java - evaluation - Feb 6, 2008 9:59:41 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestDataImpl;
import org.sakaiproject.evaluation.test.mocks.MockEvalExternalLogic;
import org.springframework.test.AbstractTransactionalSpringContextTests;


/**
 * This is the logic test base,
 * the other logic tests can extend this to avoid having as much duplicated code<br/>
 * This does the following:<br/>
 * Loads up the DAO and preloads the test data<br/>
 * Loads the following spring contexts: hibernate-test.xml, spring-hibernate.xml, logic-support.xml<br/>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public abstract class BaseTestEvalLogic extends AbstractTransactionalSpringContextTests {

   protected EvaluationDao evaluationDao;
   protected EvalCommonLogic commonLogic;
   protected MockEvalExternalLogic externalLogic;
   protected EvalTestDataLoad etdl;

   protected String[] getConfigLocations() {
      // point to the needed spring config files, must be on the classpath
      // (add component/src/webapp/WEB-INF to the build path in Eclipse),
      // they also need to be referenced in the project.xml file
      return new String[] {"hibernate-test.xml", "spring-hibernate.xml", "logic-support.xml"};
   }

   // run this before each test starts
   protected void onSetUpBeforeTransaction() throws Exception {

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
      assertTrue("Error preloading test data", evaluationDao.countAll(EvalEvaluation.class) > 0);

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

   // run this before each test starts and as part of the transaction
   protected void onSetUpInTransaction() {
      // preload additional data if desired

   }

}