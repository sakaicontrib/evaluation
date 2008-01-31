/**
 * $Id: EvalEvaluationSetupServiceImplTest.java 1000 Dec 26, 2006 10:07:31 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvalEvaluationSetupServiceImplTest.java - evaluation - Dec 26, 2006 10:07:31 AM - azeckoski
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

import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl;
import org.sakaiproject.evaluation.logic.impl.EvalSecurityChecks;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestData;
import org.sakaiproject.evaluation.test.mocks.MockEvalExternalLogic;
import org.sakaiproject.evaluation.test.mocks.MockEvalJobLogic;
import org.springframework.test.AbstractTransactionalSpringContextTests;


/**
 * Test class for EvalEvaluationSetupServiceImpl
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalEvaluationSetupServiceImplTest extends AbstractTransactionalSpringContextTests {

	protected EvalEvaluationSetupServiceImpl evaluations;

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
			throw new NullPointerException("EvaluationDao could not be retrieved from spring context");
		}

		// check the preloaded data
		Assert.assertTrue("Error preloading data", evaluationDao.countAll(EvalScale.class) > 0);

		// check the preloaded test data
		Assert.assertTrue("Error preloading test data", evaluationDao.countAll(EvalEvaluation.class) > 0);

		PreloadTestData ptd = (PreloadTestData) applicationContext.getBean("org.sakaiproject.evaluation.test.PreloadTestData");
		if (ptd == null) {
			throw new NullPointerException("PreloadTestData could not be retrieved from spring context");
		}

		// get test objects
		etdl = ptd.getEtdl();

		// load up any other needed spring beans
		EvalSettings settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
		if (settings == null) {
			throw new NullPointerException("EvalSettings could not be retrieved from spring context");
		}

      EvalSecurityChecks securityChecks = (EvalSecurityChecks) applicationContext.getBean("org.sakaiproject.evaluation.logic.impl.EvalSecurityChecks");
      if (securityChecks == null) {
         throw new NullPointerException("EvalSecurityChecks could not be retrieved from spring context");
      }

      EvalEvaluationService evaluationService = (EvalEvaluationService) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalEvaluationService");
      if (evaluationService == null) {
         throw new NullPointerException("EvalEvaluationService could not be retrieved from spring context");
      }

		// setup the mock objects if needed

		// create and setup the object to be tested
		evaluations = new EvalEvaluationSetupServiceImpl();
		evaluations.setDao(evaluationDao);
		evaluations.setExternalLogic( new MockEvalExternalLogic() );
		evaluations.setSettings(settings);
      evaluations.setSecurityChecks(securityChecks);
      evaluations.setEvaluationService(evaluationService);
		evaluations.setEvalJobLogic( new MockEvalJobLogic() ); // set to the mock object

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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#getEvaluationByEid(java.lang.String)}.
	 */
	public void testGetEvaluationByEid() {
		EvalEvaluation evaluation = null;

		// test getting evaluation having eid set
		evaluation = evaluations.getEvaluationByEid( etdl.evaluationProvided.getEid() );
		Assert.assertNotNull(evaluation);
		Assert.assertEquals(etdl.evaluationProvided.getEid(), evaluation.getEid());

		//test getting evaluation having eid not set  returns null
		evaluation = evaluations.getEvaluationByEid( etdl.evaluationActive.getEid() );
		Assert.assertNull(evaluation);

		// test getting evaluation by invalid eid returns null
		evaluation = evaluations.getEvaluationByEid( EvalTestDataLoad.INVALID_STRING_EID );
		Assert.assertNull(evaluation);
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#getEvaluationById(java.lang.Long)}.
	 * This method is first because other methods use the one it tests so it has to pass first
	 */
	public void testGetEvaluationById() {
		EvalEvaluation eval = evaluations.getEvaluationById(etdl.evaluationActive.getId());
		Assert.assertNotNull(eval);
		Assert.assertNotNull(eval.getBlankResponsesAllowed());
		Assert.assertNotNull(eval.getModifyResponsesAllowed());
		Assert.assertNotNull(eval.getResultsPrivate());
		Assert.assertNotNull(eval.getUnregisteredAllowed());
		Assert.assertEquals(etdl.evaluationActive.getId(), eval.getId());

		eval = evaluations.getEvaluationById(etdl.evaluationNew.getId());
		Assert.assertNotNull(eval);
		Assert.assertEquals(etdl.evaluationNew.getId(), eval.getId());

		// test get eval by invalid id
		eval = evaluations.getEvaluationById( EvalTestDataLoad.INVALID_LONG_ID );
		Assert.assertNull(eval);
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#saveEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)}.
	 */
	public void testSaveEvaluation() {
		EvalEvaluation eval = null;

//		Assert.fail(
//				" -15 days: " + etdl.fifteenDaysAgo +
//				", -4days: " + etdl.fourDaysAgo +
//				", -3days: " + etdl.threeDaysAgo +
//				", yesterday: " + etdl.yesterday +
//				", today: " + etdl.today +
//				", tomorrow:" + etdl.tomorrow +
//				", 3days:" + etdl.threeDaysFuture +
//				", 4days:" + etdl.fourDaysFuture
//			);

		// save a valid evaluation (all dates separate)
		eval = new EvalEvaluation( new Date(), 
				EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
				etdl.today, etdl.tomorrow, etdl.threeDaysFuture, etdl.fourDaysFuture, 
				EvalConstants.EVALUATION_STATE_INQUEUE, 
				Integer.valueOf(1), etdl.templatePublic);
		evaluations.saveEvaluation( eval, EvalTestDataLoad.MAINT_USER_ID );
		EvalEvaluation checkEval = evaluations.getEvaluationById(eval.getId());
		Assert.assertNotNull(checkEval);
		// check that entity equality works (no longer using this check)
		//Assert.assertEquals(eval, checkEval);

		// save a valid evaluation (due and stop date identical)
		evaluations.saveEvaluation( new EvalEvaluation( new Date(), 
				EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
				etdl.today, etdl.tomorrow, etdl.tomorrow, etdl.threeDaysFuture, 
				EvalConstants.EVALUATION_STATE_INQUEUE, 
				Integer.valueOf(1), etdl.templatePublic), 
			EvalTestDataLoad.MAINT_USER_ID );

		// try to save invalid evaluations

		// try save evaluation with dates that are unset (null)
		// test stop date must be set
		try {
			evaluations.saveEvaluation( new EvalEvaluation( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
					etdl.today, etdl.tomorrow, null, etdl.fourDaysFuture,
					EvalConstants.EVALUATION_STATE_INQUEUE, 
					Integer.valueOf(1), etdl.templatePublic),
				EvalTestDataLoad.MAINT_USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (NullPointerException e) {
			Assert.assertNotNull(e);
			//Assert.fail("Exception: " + e.getMessage()); // see why failing
		}

		// test due date must be set
		try {
			evaluations.saveEvaluation( new EvalEvaluation( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
					etdl.today, null, etdl.threeDaysFuture, etdl.fourDaysFuture,
					EvalConstants.EVALUATION_STATE_INQUEUE, 
					Integer.valueOf(1), etdl.templatePublic),
				EvalTestDataLoad.MAINT_USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (NullPointerException e) {
			Assert.assertNotNull(e);
			//Assert.fail("Exception: " + e.getMessage()); // see why failing
		}

		// try save evaluation with dates that are out of order
		// test due date must be after start date
		try {
			evaluations.saveEvaluation( new EvalEvaluation( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
					etdl.threeDaysFuture, etdl.tomorrow, etdl.tomorrow, etdl.fourDaysFuture, 
					EvalConstants.EVALUATION_STATE_INQUEUE, 
					Integer.valueOf(1), etdl.templatePublic),
				EvalTestDataLoad.MAINT_USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
			//Assert.fail("Exception: " + e.getMessage()); // see why failing
		}

		try {
			evaluations.saveEvaluation( new EvalEvaluation( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
					etdl.tomorrow, etdl.tomorrow, etdl.tomorrow, etdl.fourDaysFuture, 
					EvalConstants.EVALUATION_STATE_INQUEUE, 
					Integer.valueOf(1), etdl.templatePublic),
				EvalTestDataLoad.MAINT_USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
			//Assert.fail("Exception: " + e.getMessage()); // see why failing
		}

		// test stop date must be same as or after due date
		try {
			evaluations.saveEvaluation( new EvalEvaluation( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
					etdl.today, etdl.threeDaysFuture, etdl.tomorrow, etdl.fourDaysFuture, 
					EvalConstants.EVALUATION_STATE_INQUEUE, 
					Integer.valueOf(1), etdl.templatePublic),
				EvalTestDataLoad.MAINT_USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
			//Assert.fail("Exception: " + e.getMessage()); // see why failing
		}

		// test view date must be after stop date and due date
		try {
			evaluations.saveEvaluation( new EvalEvaluation( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
					etdl.today, etdl.tomorrow, etdl.tomorrow, etdl.tomorrow,
					EvalConstants.EVALUATION_STATE_INQUEUE, 
					Integer.valueOf(1), etdl.templatePublic),
				EvalTestDataLoad.MAINT_USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
			//Assert.fail("Exception: " + e.getMessage()); // see why failing
		}

		// try save new evaluation with dates that are in the past
		// test start date in the past
		try {
			evaluations.saveEvaluation( new EvalEvaluation( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
					etdl.yesterday, etdl.tomorrow, etdl.threeDaysFuture, etdl.fourDaysFuture,
					EvalConstants.EVALUATION_STATE_INQUEUE, 
					Integer.valueOf(1), etdl.templatePublic),
				EvalTestDataLoad.MAINT_USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
			//Assert.fail("Exception: " + e.getMessage()); // see why failing
		}

		// test due date in the past
		try {
			evaluations.saveEvaluation( new EvalEvaluation( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
					etdl.yesterday, etdl.yesterday, etdl.tomorrow, etdl.fourDaysFuture,
					EvalConstants.EVALUATION_STATE_INQUEUE, 
					Integer.valueOf(1), etdl.templatePublic),
				EvalTestDataLoad.MAINT_USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
			//Assert.fail("Exception: " + e.getMessage()); // see why failing
		}

		// test create eval when do not have permission (USER_ID)
		try {
			evaluations.saveEvaluation( new EvalEvaluation( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
					etdl.today, etdl.tomorrow, etdl.threeDaysFuture, etdl.fourDaysFuture,
					EvalConstants.EVALUATION_STATE_INQUEUE, 
					Integer.valueOf(1), etdl.templatePublic),
				EvalTestDataLoad.USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (SecurityException e) {
			Assert.assertNotNull(e);
			//Assert.fail("Exception: " + e.getMessage()); // see why failing
		}

		// test saving an evaluation with an empty template fails
		try {
			evaluations.saveEvaluation( new EvalEvaluation( new Date(), 
					EvalTestDataLoad.ADMIN_USER_ID, "Eval valid title", 
					etdl.today, etdl.tomorrow, etdl.tomorrow, etdl.threeDaysFuture, 
					EvalConstants.EVALUATION_STATE_INQUEUE, 
					Integer.valueOf(1), etdl.templateAdminNoItems), 
				EvalTestDataLoad.ADMIN_USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
			//Assert.fail("Exception: " + e.getMessage()); // see why failing
		}

		try {
			evaluations.saveEvaluation( new EvalEvaluation( new Date(), 
					EvalTestDataLoad.ADMIN_USER_ID, "Eval valid title", 
					etdl.today, etdl.tomorrow, etdl.tomorrow, etdl.threeDaysFuture, 
					EvalConstants.EVALUATION_STATE_INQUEUE, 
					Integer.valueOf(1), null), 
				EvalTestDataLoad.ADMIN_USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
			//Assert.fail("Exception: " + e.getMessage()); // see why failing
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#deleteEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)}.
	 */
	public void testDeleteEvaluation() {
		// remove evaluation which has not started (uses 2 email templates)
		Long availableId = etdl.evaluationNew.getAvailableEmailTemplate().getId();
		Long reminderId = etdl.evaluationNew.getReminderEmailTemplate().getId();
		int countEmailTemplates = evaluationDao.countByProperties(EvalEmailTemplate.class, 
				new String[] { "id" }, 
				new Object[] { new Long[] {availableId, reminderId} });
		Assert.assertEquals(2, countEmailTemplates);

		evaluations.deleteEvaluation(etdl.evaluationNew.getId(), EvalTestDataLoad.MAINT_USER_ID);
		EvalEvaluation eval = evaluations.getEvaluationById(etdl.evaluationNew.getId());
		Assert.assertNull(eval);

		// check to make sure the associated email templates were also removed
		countEmailTemplates = evaluationDao.countByProperties(EvalEmailTemplate.class, 
				new String[] { "id" }, 
				new Object[] { new Long[] {availableId, reminderId} });
		Assert.assertEquals(0, countEmailTemplates);

		// attempt to remove evaluation which is not owned
		try {
			evaluations.deleteEvaluation(etdl.evaluationNewAdmin.getId(), EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// attempt to remove evaluation with assigned contexts (check for cleanup)
		int countACs = evaluationDao.countByProperties(EvalAssignGroup.class, 
				new String[] { "evaluation.id" }, 
				new Object[] { etdl.evaluationNewAdmin.getId() });
		Assert.assertEquals(3, countACs);
		evaluations.deleteEvaluation(etdl.evaluationNewAdmin.getId(), EvalTestDataLoad.ADMIN_USER_ID);
		eval = evaluations.getEvaluationById(etdl.evaluationNewAdmin.getId());
		Assert.assertNull(eval);
		countACs = evaluationDao.countByProperties(EvalAssignGroup.class, 
				new String[] { "evaluation.id" }, 
				new Object[] { etdl.evaluationNewAdmin.getId() });
		Assert.assertEquals(0, countACs);

		// attempt to remove evaluation which is active
		try {
			evaluations.deleteEvaluation(etdl.evaluationActive.getId(), EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// attempt to remove evaluation which is completed
		try {
			evaluations.deleteEvaluation(etdl.evaluationClosed.getId(), EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// test for an invalid Eval that it does not cause an exception
		evaluations.deleteEvaluation( EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.MAINT_USER_ID);
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#getEvaluationsByTemplateId(java.lang.Long)}.
	 */
	public void testGetEvaluationsByTemplateId() {
		List<EvalEvaluation> l = null;
		List<Long> ids = null;

		// test valid template ids
		l = evaluations.getEvaluationsByTemplateId( etdl.templatePublic.getId() );
		Assert.assertNotNull(l);
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.evaluationNew.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

		l = evaluations.getEvaluationsByTemplateId( etdl.templateUser.getId() );
		Assert.assertNotNull(l);
		Assert.assertEquals(3, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationProvided.getId() ));

		// test no evaluations for a template
		l = evaluations.getEvaluationsByTemplateId( etdl.templateUnused.getId() );
		Assert.assertNotNull(l);
		Assert.assertTrue(l.isEmpty());

		// test invalid template id
		try {
			l = evaluations.getEvaluationsByTemplateId( EvalTestDataLoad.INVALID_LONG_ID );
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#countEvaluationsByTemplateId(java.lang.Long)}.
	 */
	public void testCountEvaluationsByTemplateId() {
		// test valid template ids
		int count = evaluations.countEvaluationsByTemplateId( etdl.templatePublic.getId() );
		Assert.assertEquals(2, count);

		count = evaluations.countEvaluationsByTemplateId( etdl.templateUser.getId() );
		Assert.assertEquals(3, count);

		// test no evaluations for a template
		count = evaluations.countEvaluationsByTemplateId( etdl.templateUnused.getId() );
		Assert.assertEquals(0, count);

		// test invalid template id
		try {
			count = evaluations.countEvaluationsByTemplateId( EvalTestDataLoad.INVALID_LONG_ID );
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#canRemoveEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation, java.lang.String)}.
	 */
	public void testCanRemoveEvaluation() {
		// test can remove
		Assert.assertTrue( evaluations.canRemoveEvaluation(
				EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId() ) );

		// test can remove (admin user id)
		Assert.assertTrue( evaluations.canRemoveEvaluation(
				EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId() ) );

		// test cannot remove (non owner)
		Assert.assertFalse( evaluations.canRemoveEvaluation(
				EvalTestDataLoad.USER_ID, etdl.evaluationNew.getId() ) );

		// test cannot remove (active)
		Assert.assertFalse( evaluations.canRemoveEvaluation(
				EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationActive.getId() ) );

		// test cannot remove (closed and viewable)
		Assert.assertFalse( evaluations.canRemoveEvaluation(
				EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationViewable.getId() ) );
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#getEvaluationsForUser(String, boolean, boolean)}.
	 */
	public void testGetEvaluationsForUser() {
		List<EvalEvaluation> evals = null;
      List<Long> ids = null;

		// get all evaluations for user
		evals = evaluations.getEvaluationsForUser(EvalTestDataLoad.USER_ID, false, false);
		Assert.assertNotNull(evals);
		Assert.assertEquals(5, evals.size());
		ids = EvalTestDataLoad.makeIdList(evals);
		Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

		// check sorting
		Date lastDate = null;
		for (int i=0; i<evals.size(); i++) {
			EvalEvaluation eval = (EvalEvaluation) evals.get(i);
			if (lastDate == null) {
				lastDate = eval.getDueDate();
			} else {
				if (lastDate.compareTo(eval.getDueDate()) <= 0) {
					lastDate = eval.getDueDate();
				} else {
					Assert.fail("Order failure:" + lastDate + " less than " + eval.getDueDate());
				}
			}
		}
		
		// test get for another user
		evals = evaluations.getEvaluationsForUser(EvalTestDataLoad.STUDENT_USER_ID, false, false);
		Assert.assertNotNull(evals);
		Assert.assertEquals(3, evals.size());
		ids = EvalTestDataLoad.makeIdList(evals);
		Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

		// get all active evaluations for user
		evals = evaluations.getEvaluationsForUser(EvalTestDataLoad.USER_ID, true, false);
		Assert.assertNotNull(evals);
		Assert.assertEquals(2, evals.size());
		ids = EvalTestDataLoad.makeIdList(evals);
		Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

		// test active evals for another user
		evals = evaluations.getEvaluationsForUser(EvalTestDataLoad.STUDENT_USER_ID, true, false);
		Assert.assertNotNull(evals);
		Assert.assertEquals(0, evals.size());

		// don't include taken evaluations
		evals = evaluations.getEvaluationsForUser(EvalTestDataLoad.USER_ID, true, true);
		Assert.assertNotNull(evals);
		Assert.assertEquals(1, evals.size());
		ids = EvalTestDataLoad.makeIdList(evals);
		Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

		// try to get for invalid user
		evals = evaluations.getEvaluationsForUser(EvalTestDataLoad.INVALID_USER_ID, false, false);
		Assert.assertNotNull(evals);
		Assert.assertEquals(0, evals.size());
}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#getVisibleEvaluationsForUser(java.lang.String, boolean, boolean)}.
	 */
	public void testGetVisibleEvaluationsForUser() {
		// test getting visible evals for the maint user
      List<EvalEvaluation> evals = null;
      List<Long> ids = null;

		evals = evaluations.getVisibleEvaluationsForUser(EvalTestDataLoad.MAINT_USER_ID, false, false);
		Assert.assertNotNull(evals);
		Assert.assertEquals(4, evals.size());
		ids = EvalTestDataLoad.makeIdList(evals);
		Assert.assertTrue(ids.contains( etdl.evaluationNew.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationProvided.getId() ));

		// test getting visible evals for the admin user (should be all)
		evals = evaluations.getVisibleEvaluationsForUser(EvalTestDataLoad.ADMIN_USER_ID, false, false);
		Assert.assertNotNull(evals);
		Assert.assertEquals(7, evals.size());

		// test getting recent closed evals for the admin user
		evals = evaluations.getVisibleEvaluationsForUser(EvalTestDataLoad.ADMIN_USER_ID, true, false);
		Assert.assertNotNull(evals);
		Assert.assertEquals(6, evals.size());
		ids = EvalTestDataLoad.makeIdList(evals);
		Assert.assertTrue(! ids.contains( etdl.evaluationViewable.getId() ));

		// test getting visible evals for the normal user (should be none)
		evals = evaluations.getVisibleEvaluationsForUser(EvalTestDataLoad.USER_ID, false, false);
		Assert.assertNotNull(evals);
		Assert.assertEquals(0, evals.size());

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#canBeginEvaluation(java.lang.String)}.
	 */
	public void testCanBeginEvaluation() {
		Assert.assertTrue( evaluations.canBeginEvaluation(EvalTestDataLoad.ADMIN_USER_ID) );
		Assert.assertTrue( evaluations.canBeginEvaluation(EvalTestDataLoad.MAINT_USER_ID) );
		Assert.assertFalse( evaluations.canBeginEvaluation(EvalTestDataLoad.USER_ID) );
		Assert.assertFalse( evaluations.canBeginEvaluation(EvalTestDataLoad.INVALID_USER_ID) );
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#canTakeEvaluation(java.lang.String, java.lang.Long, java.lang.String)}.
	 */
	public void testCanTakeEvaluation() {
		// test able to take untaken eval
		Assert.assertTrue( evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
				etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE1_REF) );
		// test able to take eval in evalGroupId not taken in yet
		Assert.assertTrue( evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
				etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE1_REF) );
		// test admin can always take
		Assert.assertTrue( evaluations.canTakeEvaluation( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF) );
        // anonymous can always be taken
        Assert.assertTrue( evaluations.canTakeEvaluation( EvalTestDataLoad.MAINT_USER_ID, 
                etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE1_REF) );

		// test not able to take
        // not assigned to this group
        Assert.assertFalse( evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE2_REF) );
		// already taken
		Assert.assertFalse( evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
				etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF) );
		// not assigned to this evalGroupId
		Assert.assertFalse( evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
				etdl.evaluationActive.getId(), EvalTestDataLoad.SITE2_REF) );
		// cannot take evaluation (no perm)
		Assert.assertFalse( evaluations.canTakeEvaluation( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF) );

		// test invalid information
        Assert.assertFalse( evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.INVALID_CONTEXT) );
		Assert.assertFalse( evaluations.canTakeEvaluation( EvalTestDataLoad.INVALID_USER_ID, 
				etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF) );

		try {
			evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
					EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.SITE1_REF);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}
		
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#countEvaluationGroups(java.lang.Long)}.
	 */
	public void testCountEvaluationContexts() {
		int count = evaluations.countEvaluationGroups( etdl.evaluationClosed.getId() );
		Assert.assertEquals(2, count);

		count = evaluations.countEvaluationGroups( etdl.evaluationActive.getId() );
		Assert.assertEquals(1, count);

		// test no assigned contexts
		count = evaluations.countEvaluationGroups( etdl.evaluationNew.getId() );
		Assert.assertEquals(0, count);

		// test invalid
		count = evaluations.countEvaluationGroups( EvalTestDataLoad.INVALID_LONG_ID );
		Assert.assertEquals(0, count);
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#updateEvaluationState(org.sakaiproject.evaluation.model.EvalEvaluation)}.
	 */
	public void testGetEvaluationState() {
		Assert.assertEquals( evaluations.updateEvaluationState( etdl.evaluationNew.getId() ), EvalConstants.EVALUATION_STATE_INQUEUE );
		Assert.assertEquals( evaluations.updateEvaluationState( etdl.evaluationActive.getId() ), EvalConstants.EVALUATION_STATE_ACTIVE );
		Assert.assertEquals( evaluations.updateEvaluationState( etdl.evaluationClosed.getId() ), EvalConstants.EVALUATION_STATE_CLOSED );
		Assert.assertEquals( evaluations.updateEvaluationState( etdl.evaluationViewable.getId() ), EvalConstants.EVALUATION_STATE_VIEWABLE );

		try {
			evaluations.updateEvaluationState( EvalTestDataLoad.INVALID_LONG_ID );
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// TODO - add tests for changing state when checked
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#getEvaluationContexts(java.lang.Long[])}.
	 */
	public void testGetEvaluationContexts() {
	   Map<Long, List<EvalGroup>> m = evaluations.getEvaluationGroups( 
				new Long[] { etdl.evaluationClosed.getId() }, true );
		Assert.assertNotNull(m);
		List<EvalGroup> evalGroups = m.get( etdl.evaluationClosed.getId() );
		Assert.assertNotNull(evalGroups);
		Assert.assertEquals(2, evalGroups.size());
		Assert.assertTrue( evalGroups.get(0) instanceof EvalGroup );
		Assert.assertTrue( evalGroups.get(1) instanceof EvalGroup );

		m = evaluations.getEvaluationGroups( 
				new Long[] { etdl.evaluationActive.getId() }, true );
		Assert.assertNotNull(m);
		evalGroups = m.get( etdl.evaluationActive.getId() );
		Assert.assertNotNull(evalGroups);
		Assert.assertEquals(1, evalGroups.size());
		Assert.assertTrue( evalGroups.get(0) instanceof EvalGroup );
		Assert.assertEquals( EvalTestDataLoad.SITE1_REF, ((EvalGroup) evalGroups.get(0)).evalGroupId );

		// test no assigned contexts
		m = evaluations.getEvaluationGroups( 
				new Long[] { etdl.evaluationNew.getId() }, true );
		Assert.assertNotNull(m);
		evalGroups = m.get( etdl.evaluationNew.getId() );
		Assert.assertNotNull(evalGroups);
		Assert.assertEquals(0, evalGroups.size());

		// test invalid
		m = evaluations.getEvaluationGroups( 
				new Long[] { EvalTestDataLoad.INVALID_LONG_ID }, true );
		Assert.assertNotNull(m);
		evalGroups = m.get( EvalTestDataLoad.INVALID_LONG_ID );
		Assert.assertNotNull(evalGroups);
		Assert.assertEquals(0, evalGroups.size());
	}


	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#getEvalCategories(java.lang.String)}.
	 */
	public void testGetEvalCategories() {
		String[] cats = null;

		// get all categories in the system
		cats = evaluations.getEvalCategories(null);
		Assert.assertNotNull(cats);
		Assert.assertEquals(2, cats.length);
		Assert.assertEquals(EvalTestDataLoad.EVAL_CATEGORY_1, cats[0]);
		Assert.assertEquals(EvalTestDataLoad.EVAL_CATEGORY_2, cats[1]);

		// get all categories for a user
		cats = evaluations.getEvalCategories(EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNotNull(cats);
		Assert.assertEquals(2, cats.length);
		Assert.assertEquals(EvalTestDataLoad.EVAL_CATEGORY_1, cats[0]);
		Assert.assertEquals(EvalTestDataLoad.EVAL_CATEGORY_2, cats[1]);

		cats = evaluations.getEvalCategories(EvalTestDataLoad.MAINT_USER_ID);
		Assert.assertNotNull(cats);
		Assert.assertEquals(1, cats.length);
		Assert.assertEquals(EvalTestDataLoad.EVAL_CATEGORY_1, cats[0]);

		// get no categories for user with none
		cats = evaluations.getEvalCategories(EvalTestDataLoad.USER_ID);
		Assert.assertNotNull(cats);
		Assert.assertEquals(0, cats.length);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationSetupServiceImpl#getEvaluationsByCategory(java.lang.String, java.lang.String)}.
	 */
	public void testGetEvaluationsByCategory() {
      List<EvalEvaluation> evals = null;
      List<Long> ids = null;

		// get all evaluations for a category
		evals = evaluations.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_1, null);
		Assert.assertNotNull(evals);
		Assert.assertEquals(2, evals.size());
		ids = EvalTestDataLoad.makeIdList(evals);
		Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

		evals = evaluations.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_2, null);
		Assert.assertNotNull(evals);
		Assert.assertEquals(1, evals.size());
		ids = EvalTestDataLoad.makeIdList(evals);
		Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));

		// get evaluations for a category and user
		evals = evaluations.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_1, EvalTestDataLoad.USER_ID);
		Assert.assertNotNull(evals);
		Assert.assertEquals(1, evals.size());
		ids = EvalTestDataLoad.makeIdList(evals);
		Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

		evals = evaluations.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_2, EvalTestDataLoad.USER_ID);
		Assert.assertNotNull(evals);
		Assert.assertEquals(0, evals.size());

		// get evaluations for invalid or non-existent category
		evals = evaluations.getEvaluationsByCategory(EvalTestDataLoad.INVALID_CONSTANT_STRING, null);
		Assert.assertNotNull(evals);
		Assert.assertEquals(0, evals.size());

		// get evaluations for invalid or non-existent user
		evals = evaluations.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_1, null);
		Assert.assertNotNull(evals);

	}

}
