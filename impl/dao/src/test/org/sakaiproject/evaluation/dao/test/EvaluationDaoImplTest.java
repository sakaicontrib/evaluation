/******************************************************************************
 * EvaluationDaoImplTest.java - created by aaronz@vt.edu
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

package org.sakaiproject.evaluation.dao.test;

import java.util.Date;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestData;
import org.springframework.test.AbstractTransactionalSpringContextTests;


/**
 * Testing for the Evaluation Data Access Layer
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationDaoImplTest extends AbstractTransactionalSpringContextTests {

	protected EvaluationDao evaluationDao;

	private EvalTestDataLoad etdl;

	protected String[] getConfigLocations() {
		// point to the needed spring config files, must be on the classpath
		// (add component/src/webapp/WEB-INF to the build path in Eclipse),
		// they also need to be referenced in the project.xml file
		return new String[] {"hibernate-test.xml", "spring-hibernate.xml"};
	}

	// run this before each test starts
	protected void onSetUpBeforeTransaction() throws Exception {
		// load the spring created dao class bean from the Spring Application Context
		evaluationDao = (EvaluationDao) applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationDao");
		if (evaluationDao == null) {
			throw new NullPointerException("DAO could not be retrieved from spring context");
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

		// init the test class if needed
		
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
	 * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#getVisibleTemplates(java.lang.String, boolean, boolean, boolean)}.
	 */
	public void testGetVisibleTemplates() {
		List l = null;
		List ids = null;

		// all templates visible to user
		l = evaluationDao.getVisibleTemplates(EvalTestDataLoad.USER_ID, 
				new String[] {EvalConstants.SHARING_PUBLIC}, true);
		Assert.assertNotNull(l);
		Assert.assertEquals(4, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
		Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

		// all templates visible to maint user
		l = evaluationDao.getVisibleTemplates(EvalTestDataLoad.MAINT_USER_ID,
				new String[] {EvalConstants.SHARING_PUBLIC}, true);
		Assert.assertNotNull(l);
		Assert.assertEquals(3, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
		Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));

		// all templates owned by USER
		l = evaluationDao.getVisibleTemplates(EvalTestDataLoad.USER_ID,
				new String[] {}, true);
		Assert.assertNotNull(l);
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

		// all private templates
		l = evaluationDao.getVisibleTemplates(null,
				new String[] {}, true);
		Assert.assertNotNull(l);
		Assert.assertEquals(5, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateAdmin.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateAdminNoItems.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

		// all private non-empty templates
		l = evaluationDao.getVisibleTemplates(null,
				new String[] {}, false);
		Assert.assertNotNull(l);
		Assert.assertEquals(4, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateAdmin.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

		// all public templates
		l = evaluationDao.getVisibleTemplates("", 
				new String[] {EvalConstants.SHARING_PUBLIC}, true);
		Assert.assertNotNull(l);
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
		Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));

		// all templates (admin would use this)
		l = evaluationDao.getVisibleTemplates(null, 
				new String[] {EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, true);
		Assert.assertNotNull(l);
		Assert.assertEquals(7, l.size());

		// all non-empty templates (admin would use this)
		l = evaluationDao.getVisibleTemplates(null, 
				new String[] {EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, false);
		Assert.assertNotNull(l);
		Assert.assertEquals(6, l.size());

		// no templates (no one should do this, it throws an exception)
		l = evaluationDao.getVisibleTemplates("", new String[] {}, true);
		Assert.assertNotNull(l);
		Assert.assertEquals(0, l.size());
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#countVisibleTemplates(java.lang.String, boolean, boolean, boolean)}.
	 */
	public void testCountVisibleTemplates() {
		// all templates visible to user
		int count = evaluationDao.countVisibleTemplates(EvalTestDataLoad.USER_ID, 
				new String[] {EvalConstants.SHARING_PUBLIC}, true);
		Assert.assertEquals(4, count);

		// all templates visible to maint user
		count = evaluationDao.countVisibleTemplates(EvalTestDataLoad.MAINT_USER_ID, 
				new String[] {EvalConstants.SHARING_PUBLIC}, true);
		Assert.assertEquals(3, count);

		// all templates owned by USER
		count = evaluationDao.countVisibleTemplates(EvalTestDataLoad.USER_ID, 
				new String[] {}, true);
		Assert.assertEquals(2, count);

		// all private templates (admin only)
		count = evaluationDao.countVisibleTemplates(null, 
				new String[] {}, true);
		Assert.assertEquals(5, count);

		// all private non-empty templates (admin only)
		count = evaluationDao.countVisibleTemplates(null, 
				new String[] {}, false);
		Assert.assertEquals(4, count);

		// all public templates
		count = evaluationDao.countVisibleTemplates("", new String[] {EvalConstants.SHARING_PUBLIC}, true);
		Assert.assertEquals(2, count);

		// all templates (admin would use this)
		count = evaluationDao.countVisibleTemplates(null, 
				new String[] {EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, true);
		Assert.assertEquals(7, count);

		// all non-empty templates (admin would use this)
		count = evaluationDao.countVisibleTemplates(null, 
				new String[] {EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, false);
		Assert.assertEquals(6, count);
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#getActiveEvaluationsByContexts(java.lang.String[])}.
	 */
	public void testGetActiveEvaluationsByContexts() {
		Set s = null;
		List ids = null;

		// test getting evaluations by context
		s = evaluationDao.getEvaluationsByContexts(
				new String[] {EvalTestDataLoad.CONTEXT1}, false);
		Assert.assertNotNull(s);
		Assert.assertEquals(4, s.size());
		ids = EvalTestDataLoad.makeIdList(s);
		Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));

		s = evaluationDao.getEvaluationsByContexts(
				new String[] {EvalTestDataLoad.CONTEXT2}, false);
		Assert.assertNotNull(s);
		Assert.assertEquals(3, s.size());
		ids = EvalTestDataLoad.makeIdList(s);
		Assert.assertTrue(! ids.contains( etdl.evaluationActive.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

		s = evaluationDao.getEvaluationsByContexts(
				new String[] {"invalid context"}, false);
		Assert.assertNotNull(s);
		Assert.assertEquals(0, s.size());

		// test that the get active part works
		s = evaluationDao.getEvaluationsByContexts(
				new String[] {EvalTestDataLoad.CONTEXT1}, true);
		Assert.assertNotNull(s);
		Assert.assertEquals(2, s.size());
		ids = EvalTestDataLoad.makeIdList(s);
		Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
		Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

		s = evaluationDao.getEvaluationsByContexts(
				new String[] {EvalTestDataLoad.CONTEXT2}, true);
		Assert.assertNotNull(s);
		Assert.assertEquals(0, s.size());

		// test getting from an invalid context
		s = evaluationDao.getEvaluationsByContexts(
				new String[] {EvalTestDataLoad.INVALID_CONTEXT}, true);
		Assert.assertNotNull(s);
		Assert.assertEquals(0, s.size());		

		// test invalid
		try {
			s = evaluationDao.getEvaluationsByContexts(null, false);
			Assert.fail("Should have thrown an exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#getAnswers(java.lang.Long, java.lang.Long)}.
	 */
	public void testGetAnswers() {
		Set s = null;
		List l = null;
		List ids = null;
		
		s = etdl.response2.getAnswers();
		Assert.assertNotNull(s);
		Assert.assertEquals(2, s.size());
		ids = EvalTestDataLoad.makeIdList(s);
		Assert.assertTrue(ids.contains( etdl.answer2_2.getId() ));
		Assert.assertTrue(ids.contains( etdl.answer2_5.getId() ));

		l = evaluationDao.getAnswers(etdl.item2.getId(), etdl.evaluationClosed.getId());
		Assert.assertNotNull(l);
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.answer2_2.getId() ));
		Assert.assertTrue(ids.contains( etdl.answer3_2.getId() ));

		l = evaluationDao.getAnswers(etdl.item5.getId(), etdl.evaluationClosed.getId());
		Assert.assertNotNull(l);
		Assert.assertEquals(1, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.answer2_5.getId() ));

		// test item that is not in this evaluation
		l = evaluationDao.getAnswers(etdl.item3.getId(), etdl.evaluationClosed.getId());
		Assert.assertNotNull(l);
		Assert.assertEquals(0, l.size());

		// test invalid eval id
		// TODO - this should probably throw an exception
		l = evaluationDao.getAnswers(etdl.item1.getId(), Long.valueOf(999));
		Assert.assertNotNull(l);
		Assert.assertEquals(0, l.size());

		// test invalid item id
		// TODO - this should probably throw an exception
		l = evaluationDao.getAnswers(Long.valueOf(999), etdl.evaluationClosed.getId());
		Assert.assertNotNull(l);
		Assert.assertEquals(0, l.size());
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#getNextBlockId()}.
	 */
	public void testGetNextBlockId() {
		Integer blockId = evaluationDao.getNextBlockId();
		Assert.assertNotNull(blockId);
		Assert.assertTrue(blockId.intValue() >= 0);

		EvalItem item = new EvalItem( new Date(), "AZ", "text", "sharing", "classification", Boolean.FALSE);
		item.setBlockId( blockId );
		evaluationDao.save( item );

		Integer blockId2 = evaluationDao.getNextBlockId();
		Assert.assertNotNull(blockId2);
		Assert.assertTrue(blockId2.intValue() >= 0);
		Assert.assertTrue(blockId2.compareTo(blockId) > 0);

		EvalItem item2 = new EvalItem( new Date(), "AZ", "text", "sharing", "classification", Boolean.FALSE);
		item.setBlockId( blockId2 );
		evaluationDao.save( item2 );

		Integer blockId3 = evaluationDao.getNextBlockId();
		Assert.assertNotNull(blockId3);
		Assert.assertTrue(blockId3.intValue() >= 0);
		Assert.assertTrue(blockId3.compareTo(blockId2) > 0);
	}

	/**
	 * Add anything that supports the unit tests below here
	 */

}
