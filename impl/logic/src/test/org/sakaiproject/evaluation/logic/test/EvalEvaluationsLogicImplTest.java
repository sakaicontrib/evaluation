/******************************************************************************
 * EvalEvaluationsLogicImplTest.java - created by aaronz@vt.edu on Dec 26, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.test.stubs.EvalExternalLogicStub;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestData;
import org.springframework.test.AbstractTransactionalSpringContextTests;


/**
 * Test class for EvalEvaluationsLogicImpl
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalEvaluationsLogicImplTest extends AbstractTransactionalSpringContextTests {

	protected EvalEvaluationsLogicImpl evaluations;

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

		// setup the mock objects if needed

		// create and setup the object to be tested
		evaluations = new EvalEvaluationsLogicImpl();
		evaluations.setDao(evaluationDao);
		evaluations.setExternalLogic( new EvalExternalLogicStub() );
		evaluations.setSettings(settings);

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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl#getEvaluationById(java.lang.Long)}.
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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl#saveEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)}.
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
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl#deleteEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)}.
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
		Assert.assertEquals(2, countACs);
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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl#getEvaluationsByTemplateId(java.lang.Long)}.
	 */
	public void testGetEvaluationsByTemplateId() {
		List l = null;
		List ids = null;

		// test valid template ids
		l = evaluations.getEvaluationsByTemplateId( etdl.templatePublic.getId() );
		Assert.assertNotNull(l);
		Assert.assertEquals(3, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.evaluationNew.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

		l = evaluations.getEvaluationsByTemplateId( etdl.templateUser.getId() );
		Assert.assertNotNull(l);
		Assert.assertEquals(1, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl#countEvaluationsByTemplateId(java.lang.Long)}.
	 */
	public void testCountEvaluationsByTemplateId() {
		// test valid template ids
		int count = evaluations.countEvaluationsByTemplateId( etdl.templatePublic.getId() );
		Assert.assertEquals(3, count);

		count = evaluations.countEvaluationsByTemplateId( etdl.templateUser.getId() );
		Assert.assertEquals(1, count);

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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl#canRemoveEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation, java.lang.String)}.
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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl#getEvaluationsForUser(String, boolean, boolean)}.
	 */
	public void testGetEvaluationsForUser() {
		List evals = null;
		List ids = null;

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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl#getVisibleEvaluationsForUser(java.lang.String, boolean)}.
	 */
	public void testGetVisibleEvaluationsForUser() {
		// test getting visible evals for the maint user
		List evals = null;
		List ids = null;

		evals = evaluations.getVisibleEvaluationsForUser(EvalTestDataLoad.MAINT_USER_ID, false);
		Assert.assertNotNull(evals);
		Assert.assertEquals(3, evals.size());
		ids = EvalTestDataLoad.makeIdList(evals);
		Assert.assertTrue(ids.contains( etdl.evaluationNew.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

		// test getting visible evals for the admin user (should be all)
		evals = evaluations.getVisibleEvaluationsForUser(EvalTestDataLoad.ADMIN_USER_ID, false);
		Assert.assertNotNull(evals);
		Assert.assertEquals(6, evals.size());

		// test getting recent closed evals for the admin user
		evals = evaluations.getVisibleEvaluationsForUser(EvalTestDataLoad.ADMIN_USER_ID, true);
		Assert.assertNotNull(evals);
		Assert.assertEquals(5, evals.size());
		ids = EvalTestDataLoad.makeIdList(evals);
		Assert.assertTrue(! ids.contains( etdl.evaluationViewable.getId() ));

		// test getting visible evals for the normal user (should be none)
		evals = evaluations.getVisibleEvaluationsForUser(EvalTestDataLoad.USER_ID, false);
		Assert.assertNotNull(evals);
		Assert.assertEquals(0, evals.size());

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl#canBeginEvaluation(java.lang.String)}.
	 */
	public void testCanBeginEvaluation() {
		Assert.assertTrue( evaluations.canBeginEvaluation(EvalTestDataLoad.ADMIN_USER_ID) );
		Assert.assertTrue( evaluations.canBeginEvaluation(EvalTestDataLoad.MAINT_USER_ID) );
		Assert.assertFalse( evaluations.canBeginEvaluation(EvalTestDataLoad.USER_ID) );
		Assert.assertFalse( evaluations.canBeginEvaluation(EvalTestDataLoad.INVALID_USER_ID) );
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl#canTakeEvaluation(java.lang.String, java.lang.Long, java.lang.String)}.
	 */
	public void testCanTakeEvaluation() {
		// test able to take untaken eval
		Assert.assertTrue( evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
				etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.CONTEXT1) );
		// test able to take eval in context not taken in yet
		Assert.assertTrue( evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
				etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.CONTEXT1) );
		// test admin can always take
		Assert.assertTrue( evaluations.canTakeEvaluation( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.evaluationActive.getId(), EvalTestDataLoad.CONTEXT1) );

		// test not able to take
		// already taken
		Assert.assertFalse( evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
				etdl.evaluationActive.getId(), EvalTestDataLoad.CONTEXT1) );
		// not assigned to this context
		Assert.assertFalse( evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
				etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.CONTEXT2) );
		Assert.assertFalse( evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
				etdl.evaluationActive.getId(), EvalTestDataLoad.CONTEXT2) );
		// cannot take evaluation (no perm)
		Assert.assertFalse( evaluations.canTakeEvaluation( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.CONTEXT1) );
		Assert.assertFalse( evaluations.canTakeEvaluation( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.evaluationActive.getId(), EvalTestDataLoad.CONTEXT1) );

		// test invalid information
		Assert.assertFalse( evaluations.canTakeEvaluation( EvalTestDataLoad.INVALID_USER_ID, 
				etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.CONTEXT1) );
		Assert.assertFalse( evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
				etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.INVALID_CONTEXT) );

		try {
			evaluations.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
					EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.CONTEXT1);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}
		
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl#countEvaluationGroups(java.lang.Long)}.
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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl#getEvaluationState(org.sakaiproject.evaluation.model.EvalEvaluation)}.
	 */
	public void testGetEvaluationState() {
		Assert.assertEquals( evaluations.getEvaluationState( etdl.evaluationNew.getId() ), EvalConstants.EVALUATION_STATE_INQUEUE );
		Assert.assertEquals( evaluations.getEvaluationState( etdl.evaluationActive.getId() ), EvalConstants.EVALUATION_STATE_ACTIVE );
		Assert.assertEquals( evaluations.getEvaluationState( etdl.evaluationClosed.getId() ), EvalConstants.EVALUATION_STATE_CLOSED );
		Assert.assertEquals( evaluations.getEvaluationState( etdl.evaluationViewable.getId() ), EvalConstants.EVALUATION_STATE_VIEWABLE );

		try {
			evaluations.getEvaluationState( EvalTestDataLoad.INVALID_LONG_ID );
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// TODO - add tests for changing state when checked
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl#getEvaluationContexts(java.lang.Long[])}.
	 */
	public void testGetEvaluationContexts() {
		Map m = evaluations.getEvaluationGroups( 
				new Long[] { etdl.evaluationClosed.getId() }, true );
		Assert.assertNotNull(m);
		List contexts = (List) m.get( etdl.evaluationClosed.getId() );
		Assert.assertNotNull(contexts);
		Assert.assertEquals(2, contexts.size());
		Assert.assertTrue( contexts.get(0) instanceof EvalGroup );
		Assert.assertTrue( contexts.get(1) instanceof EvalGroup );

		m = evaluations.getEvaluationGroups( 
				new Long[] { etdl.evaluationActive.getId() }, true );
		Assert.assertNotNull(m);
		contexts = (List) m.get( etdl.evaluationActive.getId() );
		Assert.assertNotNull(contexts);
		Assert.assertEquals(1, contexts.size());
		Assert.assertTrue( contexts.get(0) instanceof EvalGroup );
		Assert.assertEquals( EvalTestDataLoad.CONTEXT1, ((EvalGroup) contexts.get(0)).evalGroupId );

		// test no assigned contexts
		m = evaluations.getEvaluationGroups( 
				new Long[] { etdl.evaluationNew.getId() }, true );
		Assert.assertNotNull(m);
		contexts = (List) m.get( etdl.evaluationNew.getId() );
		Assert.assertNotNull(contexts);
		Assert.assertEquals(0, contexts.size());

		// test invalid
		m = evaluations.getEvaluationGroups( 
				new Long[] { EvalTestDataLoad.INVALID_LONG_ID }, true );
		Assert.assertNotNull(m);
		contexts = (List) m.get( EvalTestDataLoad.INVALID_LONG_ID );
		Assert.assertNotNull(contexts);
		Assert.assertEquals(0, contexts.size());
	}

}
