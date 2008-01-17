/******************************************************************************
 * EvalScalesLogicImplTest.java - created by aaronz@vt.edu on Dec 29, 2006
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

import junit.framework.Assert;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl;
import org.sakaiproject.evaluation.logic.test.stubs.EvalExternalLogicStub;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestData;
import org.springframework.test.AbstractTransactionalSpringContextTests;


/**
 * Test class for EvalScalesLogicImpl<br/>
 * Note for testing, there are currently 15 preloaded public scales owned by the admin user
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalScalesLogicImplTest extends AbstractTransactionalSpringContextTests {

	protected EvalScalesLogicImpl scales;

	private EvaluationDao evaluationDao;
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
			throw new NullPointerException("EvaluationDao could not be retrieved from spring evalGroupId");
		}

		// check the preloaded data
		Assert.assertTrue("Error preloading data", evaluationDao.countAll(EvalScale.class) > 0);

		// check the preloaded test data
		Assert.assertTrue("Error preloading test data", evaluationDao.countAll(EvalEvaluation.class) > 0);

		PreloadTestData ptd = (PreloadTestData) applicationContext.getBean("org.sakaiproject.evaluation.test.PreloadTestData");
		if (ptd == null) {
			throw new NullPointerException("PreloadTestData could not be retrieved from spring evalGroupId");
		}

		// get test objects
		etdl = ptd.getEtdl();

		// load up any other needed spring beans

		// setup the mock objects if needed

		// create and setup the object to be tested
		scales = new EvalScalesLogicImpl();
		scales.setDao(evaluationDao);
		scales.setExternalLogic( new EvalExternalLogicStub() );

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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#getScaleById(java.lang.Long)}.
	 */
	public void testGetScaleById() {
		EvalScale scale = null;

		scale = scales.getScaleById( etdl.scale1.getId() );
		Assert.assertNotNull(scale);
		Assert.assertEquals(etdl.scale1.getId(), scale.getId());

		scale = scales.getScaleById( etdl.scale2.getId() );
		Assert.assertNotNull(scale);
		Assert.assertEquals(etdl.scale2.getId(), scale.getId());

		// test get eval by invalid id
		scale = scales.getScaleById( EvalTestDataLoad.INVALID_LONG_ID );
		Assert.assertNull(scale);
	}
	
	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#getScaleByEid(java.lang.String)}.
	 */
	public void testGetScaleByEid() {
		EvalScale scale = null;

		//test getting scale having eid set
		scale = scales.getScaleByEid( etdl.scaleEid.getEid() );
		Assert.assertNotNull(scale);
		Assert.assertEquals(etdl.scaleEid.getEid(), scale.getEid());

		//test getting scale not having eid set returns null
		scale = scales.getScaleByEid( etdl.scale2.getEid() );
		Assert.assertNull(scale);

		// test getting scale by invalid eid returns null
		scale = scales.getScaleByEid( EvalTestDataLoad.INVALID_STRING_EID );
		Assert.assertNull(scale);
	}
	
	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#saveScale(org.sakaiproject.evaluation.model.EvalScale, java.lang.String)}.
	 */
	public void testSaveScale() {
		String[] options1 = {"Bad", "Average", "Good"};
		String test_title = "test scale title";

		// test saving a new valid scale
		scales.saveScale( new EvalScale( new Date(), 
				EvalTestDataLoad.MAINT_USER_ID, test_title, 
				EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PRIVATE, Boolean.FALSE, 
				"description", EvalConstants.SCALE_IDEAL_LOW,
				options1, EvalTestDataLoad.UNLOCKED), EvalTestDataLoad.MAINT_USER_ID);

		// fetch scales to work with
		EvalScale testScale1 = (EvalScale) evaluationDao.findById(EvalScale.class, 
				etdl.scale1.getId());
		EvalScale testScale2 = (EvalScale) evaluationDao.findById(EvalScale.class, 
				etdl.scale2.getId());
		EvalScale testScale3 = (EvalScale) evaluationDao.findById(EvalScale.class, 
				etdl.scale3.getId());
		EvalScale testScale4 = (EvalScale) evaluationDao.findById(EvalScale.class, 
				etdl.scale4.getId());

		// test editing unlocked scale
		testScale2.setSharing(EvalConstants.SHARING_SHARED);
		scales.saveScale(testScale2, EvalTestDataLoad.MAINT_USER_ID);

		Assert.assertEquals(4, testScale2.getOptions().length);
		testScale2.setOptions(options1);
		scales.saveScale(testScale2, EvalTestDataLoad.MAINT_USER_ID);
		Assert.assertEquals(3, testScale2.getOptions().length);

		// test admin can edit any scale
		testScale2.setIdeal(EvalConstants.SCALE_IDEAL_MID);
		scales.saveScale(testScale2, EvalTestDataLoad.ADMIN_USER_ID);

		// test that editing unowned scale causes permission failure
		try {
			testScale3.setIdeal(EvalConstants.SCALE_IDEAL_MID);
			scales.saveScale(testScale4, EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (SecurityException e) {
			Assert.assertNotNull(e);
		}

		// TODO - CANNOT RUN THIS TEST BECAUSE OF HIBERNATE ISSUE
//		// test that LOCKED cannot be changed to FALSE on existing scales
//		try {
//			testScale1.setLocked(Boolean.FALSE);
//			scales.saveScale(testScale1, EvalTestDataLoad.ADMIN_USER_ID);
//			Assert.fail("Should have thrown exception");
//		} catch (IllegalArgumentException e) {
//			Assert.assertNotNull(e);
//			Assert.fail(e.getMessage());
//		}

		// test editing LOCKED scale fails
		try {
			testScale1.setSharing(EvalConstants.SHARING_PRIVATE);
			scales.saveScale(testScale1, EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		// test that setting sharing to PUBLIC as non-admin fails
		try {
			testScale2.setSharing(EvalConstants.SHARING_PUBLIC);
			scales.saveScale(testScale2, EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test admin can set scales to public sharing
		testScale2.setSharing(EvalConstants.SHARING_PUBLIC);
		scales.saveScale(testScale2, EvalTestDataLoad.ADMIN_USER_ID);

		// test fails to save scale with null options
		try {
			scales.saveScale( new EvalScale( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, "options are null", 
					EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PRIVATE, Boolean.FALSE, 
					"description", EvalConstants.SCALE_IDEAL_LOW,
					null, EvalTestDataLoad.UNLOCKED), EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test fails to save scale with duplicate title
		try {
			scales.saveScale( new EvalScale( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, test_title, 
					EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PRIVATE, Boolean.FALSE, 
					"description", EvalConstants.SCALE_IDEAL_LOW,
					options1, EvalTestDataLoad.UNLOCKED), EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#deleteScale(java.lang.Long, java.lang.String)}.
	 */
	public void testDeleteScale() {
		// test removing unowned scale fails
		try {
			scales.deleteScale(etdl.scale4.getId(), EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// test removing owned scale works
		scales.deleteScale(etdl.scale3.getId(), EvalTestDataLoad.MAINT_USER_ID);

		// test removing expert scale allowed
		scales.deleteScale(etdl.scale4.getId(), EvalTestDataLoad.ADMIN_USER_ID);

		// test removing locked scale fails
		try {
			scales.deleteScale(etdl.scale1.getId(), EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

		// test invalid scale id fails
		try {
			scales.deleteScale(EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#getScalesForUser(java.lang.String, java.lang.String)}.
	 */
	public void testGetScalesForUser() {
		List l = null;
		List ids = null;
		// NOTE: 15 preloaded public scales to take into account currently
		int preloadedCount = 15;

		// get all visible scales (admin should see all)
		l = scales.getScalesForUser(EvalTestDataLoad.ADMIN_USER_ID, null);
		Assert.assertNotNull(l);
		Assert.assertEquals(5 + preloadedCount, l.size()); // include 15 preloaded
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.scale1.getId() ));
		Assert.assertTrue(ids.contains( etdl.scale2.getId() ));
		Assert.assertTrue(ids.contains( etdl.scale3.getId() ));
		
		Assert.assertTrue(ids.contains( etdl.scaleEid.getId() ));

		// get all visible scales (include maint owned and public)
		l = scales.getScalesForUser(EvalTestDataLoad.MAINT_USER_ID, null);
		Assert.assertNotNull(l);
		Assert.assertEquals(4 + preloadedCount, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.scale1.getId() ));
		Assert.assertTrue(ids.contains( etdl.scale2.getId() ));
		Assert.assertTrue(ids.contains( etdl.scale3.getId() ));
		Assert.assertTrue(! ids.contains( etdl.scale4.getId() ));
		
		Assert.assertTrue(ids.contains( etdl.scaleEid.getId() ));

		// get all visible scales (should only see public)
		l = scales.getScalesForUser(EvalTestDataLoad.USER_ID, null);
		Assert.assertNotNull(l);
		Assert.assertEquals(2 + preloadedCount, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.scale1.getId() ));
		Assert.assertTrue(! ids.contains( etdl.scale2.getId() ));
		Assert.assertTrue(! ids.contains( etdl.scale3.getId() ));
		
		Assert.assertTrue(ids.contains( etdl.scaleEid.getId() ));

		// attempt to get SHARING_OWNER scales (returns same as null)
		l = scales.getScalesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_OWNER);
		Assert.assertNotNull(l);
		Assert.assertEquals(5 + preloadedCount, l.size());

		// get all private scales (admin should see all private)
		l = scales.getScalesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_PRIVATE);
		Assert.assertNotNull(l);
		Assert.assertEquals(3, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.scale2.getId() ));
		Assert.assertTrue(ids.contains( etdl.scale3.getId() ));
		Assert.assertTrue(ids.contains( etdl.scale4.getId() ));

		// check that the return order is correct
		Assert.assertEquals( etdl.scale2.getId(), ids.get(0) );
		Assert.assertEquals( etdl.scale3.getId(), ids.get(1) );
		Assert.assertEquals( etdl.scale4.getId(), ids.get(2) );

		// get all private scales (maint should see own only)
		l = scales.getScalesForUser(EvalTestDataLoad.MAINT_USER_ID, EvalConstants.SHARING_PRIVATE);
		Assert.assertNotNull(l);
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.scale2.getId() ));
		Assert.assertTrue(ids.contains( etdl.scale3.getId() ));

		// get all private scales (normal user should see none)
		l = scales.getScalesForUser(EvalTestDataLoad.USER_ID, EvalConstants.SHARING_PRIVATE);
		Assert.assertNotNull(l);
		Assert.assertEquals(0, l.size());

		// get all public scales (normal user should see all)
		l = scales.getScalesForUser(EvalTestDataLoad.USER_ID, EvalConstants.SHARING_PUBLIC);
		Assert.assertNotNull(l);
		Assert.assertEquals(2 + preloadedCount, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.scale1.getId() ));
		Assert.assertTrue(! ids.contains( etdl.scale2.getId() ));
		Assert.assertTrue(! ids.contains( etdl.scale3.getId() ));
		
		Assert.assertTrue(ids.contains( etdl.scaleEid.getId() ));

		// test getting invalid constant causes failure
		try {
			l = scales.getScalesForUser(EvalTestDataLoad.USER_ID, EvalTestDataLoad.INVALID_CONSTANT_STRING);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#canModifyScale(String, Long)}
	 */
	public void testCanModifyScale() {
		// test can modify owned scale
		Assert.assertTrue( scales.canModifyScale(
				EvalTestDataLoad.MAINT_USER_ID,	etdl.scale3.getId()) );
		Assert.assertTrue( scales.canModifyScale(
				EvalTestDataLoad.ADMIN_USER_ID,	etdl.scale4.getId()) );

		// test can modify used scale
		Assert.assertTrue( scales.canModifyScale(
				EvalTestDataLoad.MAINT_USER_ID,	etdl.scale2.getId()) );

		// test admin user can override perms
		Assert.assertTrue( scales.canModifyScale(
				EvalTestDataLoad.ADMIN_USER_ID,	etdl.scale3.getId()) );

		// test cannot control unowned scale
		Assert.assertFalse( scales.canModifyScale(
				EvalTestDataLoad.MAINT_USER_ID,	etdl.scale4.getId()) );
		Assert.assertFalse( scales.canModifyScale(
				EvalTestDataLoad.USER_ID, etdl.scale3.getId()) );

		// test cannot modify locked scale
		Assert.assertFalse( scales.canModifyScale(
				EvalTestDataLoad.ADMIN_USER_ID,	etdl.scale1.getId()) );

		// test invalid scale id causes failure
		try {
			scales.canModifyScale(EvalTestDataLoad.ADMIN_USER_ID,	
					EvalTestDataLoad.INVALID_LONG_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#canRemoveScale(String, Long)}
	 */
	public void testCanRemoveScale() {
		// test can remove owned scale
		Assert.assertTrue( scales.canRemoveScale(
				EvalTestDataLoad.ADMIN_USER_ID,	etdl.scale4.getId()) );
		Assert.assertTrue( scales.canRemoveScale(
				EvalTestDataLoad.MAINT_USER_ID,	etdl.scale3.getId()) );

		// test cannot remove unowned scale
		Assert.assertFalse( scales.canRemoveScale(
				EvalTestDataLoad.MAINT_USER_ID,	etdl.scale4.getId()) );
		Assert.assertFalse( scales.canRemoveScale(
				EvalTestDataLoad.USER_ID, etdl.scale3.getId()) );

		// test cannot remove unlocked used scale
		Assert.assertFalse( scales.canRemoveScale(
				EvalTestDataLoad.MAINT_USER_ID,	etdl.scale2.getId()) );

		// test admin cannot remove unlocked used scale
		Assert.assertFalse( scales.canRemoveScale(
				EvalTestDataLoad.ADMIN_USER_ID,	etdl.scale2.getId()) );

		// test cannot remove locked scale
		Assert.assertFalse( scales.canRemoveScale(
				EvalTestDataLoad.ADMIN_USER_ID,	etdl.scale1.getId()) );

		// test invalid scale id causes failure
		try {
			scales.canRemoveScale(EvalTestDataLoad.ADMIN_USER_ID,	
					EvalTestDataLoad.INVALID_LONG_ID);
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}

	}

}
