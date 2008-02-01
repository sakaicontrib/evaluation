/**
 * $Id: EvalEmailsLogicImplTest.java 1000 Dec 29, 2006 10:07:31 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvalEmailsLogicImplTest.java - evaluation - Dec 29, 2006 10:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.test;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.impl.EvalEmailsLogicImpl;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestData;
import org.sakaiproject.evaluation.test.mocks.MockEvalExternalLogic;
import org.springframework.test.AbstractTransactionalSpringContextTests;


/**
 * EvalEmailsLogicImpl test class
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalEmailsLogicImplTest extends AbstractTransactionalSpringContextTests {

	protected EvalEmailsLogicImpl emailsLogic;

	private EvaluationDao evaluationDao;
	private EvalTestDataLoad etdl;

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
			throw new NullPointerException("EvaluationDao could not be retrieved from spring evalGroupId");
		}

		// check the preloaded data
		assertTrue("Error preloading data", evaluationDao.countAll(EvalScale.class) > 0);

		// check the preloaded test data
		assertTrue("Error preloading test data", evaluationDao.countAll(EvalEvaluation.class) > 0);

		PreloadTestData ptd = (PreloadTestData) applicationContext.getBean("org.sakaiproject.evaluation.test.PreloadTestData");
		if (ptd == null) {
			throw new NullPointerException("PreloadTestData could not be retrieved from spring evalGroupId");
		}

		// get test objects
		etdl = ptd.getEtdl();

		// load up any other needed spring beans
		EvalSettings settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
		if (settings == null) {
			throw new NullPointerException("EvalSettings could not be retrieved from spring evalGroupId");
		}

      EvalEvaluationService evaluationService = (EvalEvaluationService) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalEvaluationService");
      if (evaluationService == null) {
         throw new NullPointerException("EvalEvaluationService could not be retrieved from spring context");
      }

		// setup the mock objects if needed

		// create and setup the object to be tested
		emailsLogic = new EvalEmailsLogicImpl();
		emailsLogic.setExternalLogic( new MockEvalExternalLogic() );
		emailsLogic.setEvaluationService(evaluationService);
		emailsLogic.setSettings(settings);

	}

	// run this before each test starts and as part of the transaction
	protected void onSetUpInTransaction() {
		// preload additional data if desired
		
	}

	/**
	 * ADD unit tests below here, use testMethod as the name of the unit test,
	 * Note that if a method is overloaded you should include the arguments in the
	 * test name like so: testMethodClassInt (for method(Class, int);
	 */


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEmailsLogicImpl#sendEvalCreatedNotifications(java.lang.Long, boolean)}.
    */
   public void testSendEvalCreatedNotifications() {
      String[] sentEmails = null;

      // test send to just the evaluatees
      sentEmails = emailsLogic.sendEvalCreatedNotifications(etdl.evaluationViewable.getId(), false);
      assertNotNull(sentEmails);
      assertEquals(1, sentEmails.length);

      // test send to evaluatees and owner
      sentEmails = emailsLogic.sendEvalCreatedNotifications(etdl.evaluationViewable.getId(), true);
      assertNotNull(sentEmails);
      assertEquals(2, sentEmails.length);

      // test that invalid evaluation id causes failure
      try {
         emailsLogic.sendEvalCreatedNotifications(EvalTestDataLoad.INVALID_LONG_ID, false);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }
   }

   /**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEmailsLogicImpl#sendEvalAvailableNotifications(java.lang.Long, boolean)}.
	 */
	public void testSendEvalAvailableNotifications() {
      String[] sentEmails = null;

      // test send to just the evaluators
      sentEmails = emailsLogic.sendEvalAvailableNotifications(etdl.evaluationNewAdmin.getId(), false);
		assertNotNull(sentEmails);
      assertEquals(2, sentEmails.length);

      // test send to evaluators and evaluatees
      sentEmails = emailsLogic.sendEvalAvailableNotifications(etdl.evaluationNewAdmin.getId(), true);
      assertNotNull(sentEmails);
      assertEquals(3, sentEmails.length);

      // test that invalid evaluation id causes failure
      try {
         emailsLogic.sendEvalAvailableNotifications(EvalTestDataLoad.INVALID_LONG_ID, false);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }
	}

	// FIXME add remaining tests here

}
