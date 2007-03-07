/******************************************************************************
 * EvalExpertItemsLogicImplTest.java - created by aaronz on 6 Mar 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
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

import java.util.List;

import junit.framework.Assert;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl;
import org.sakaiproject.evaluation.logic.test.stubs.EvalExternalLogicStub;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestData;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Test cases for the expert items hibernate implementation
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalExpertItemsLogicImplTest extends AbstractTransactionalSpringContextTests {

	protected EvalExpertItemsLogicImpl expertItems;

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

		// setup the mock objects if needed

		// create and setup the object to be tested
		expertItems = new EvalExpertItemsLogicImpl();
		expertItems.setDao(evaluationDao);
		expertItems.setExternalLogic( new EvalExternalLogicStub() );

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

	public void testPreloadedData() {
		// check the full count of preloaded items
		Assert.assertEquals(17, evaluationDao.countAll(EvalItemGroup.class) );
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#getItemGroups(java.lang.Long, java.lang.String, boolean)}.
	 */
	public void testGetItemGroups() {
		List ids = null;
		List eItems = null;

		// NOTE: preloaded groups to take into account

		// check all expert top level groups
		eItems = expertItems.getItemGroups(null, EvalTestDataLoad.ADMIN_USER_ID, true, true);
		Assert.assertNotNull( eItems );
		Assert.assertEquals(3 + 4, eItems.size()); // 4 preloaded top level expert groups
		ids = EvalTestDataLoad.makeIdList(eItems);
		Assert.assertTrue(ids.contains( etdl.categoryA.getId() ));
		Assert.assertTrue(ids.contains( etdl.categoryB.getId() ));
		Assert.assertTrue(ids.contains( etdl.categoryC.getId() ));
		Assert.assertTrue(! ids.contains( etdl.categoryD.getId() ));
		Assert.assertTrue(! ids.contains( etdl.objectiveA1.getId() ));
		Assert.assertTrue(! ids.contains( etdl.objectiveA2.getId() ));

		// check all non-expert top level groups
		eItems = expertItems.getItemGroups(null, EvalTestDataLoad.ADMIN_USER_ID, true, false);
		Assert.assertNotNull( eItems );
		Assert.assertEquals(1, eItems.size());
		ids = EvalTestDataLoad.makeIdList(eItems);
		Assert.assertTrue(! ids.contains( etdl.categoryA.getId() ));
		Assert.assertTrue(! ids.contains( etdl.categoryB.getId() ));
		Assert.assertTrue(! ids.contains( etdl.categoryC.getId() ));
		Assert.assertTrue(ids.contains( etdl.categoryD.getId() ));
		Assert.assertTrue(! ids.contains( etdl.objectiveA1.getId() ));
		Assert.assertTrue(! ids.contains( etdl.objectiveA2.getId() ));

		// check all contained groups (objectives) in a parent (category)
		eItems = expertItems.getItemGroups(etdl.categoryA.getId(), EvalTestDataLoad.ADMIN_USER_ID, true, true);
		Assert.assertNotNull( eItems );
		Assert.assertEquals(2, eItems.size());
		ids = EvalTestDataLoad.makeIdList(eItems);
		Assert.assertTrue(ids.contains( etdl.objectiveA1.getId() ));
		Assert.assertTrue(ids.contains( etdl.objectiveA2.getId() ));

		// check only non-empty top level groups
		eItems = expertItems.getItemGroups(null, EvalTestDataLoad.ADMIN_USER_ID, false, true);
		Assert.assertNotNull( eItems );
		Assert.assertEquals(2 + 4, eItems.size()); // 4 preloaded non-empty top level groups
		ids = EvalTestDataLoad.makeIdList(eItems);
		Assert.assertTrue(ids.contains( etdl.categoryA.getId() ));
		Assert.assertTrue(ids.contains( etdl.categoryB.getId() ));

		// check only non-empty contained groups
		eItems = expertItems.getItemGroups(etdl.categoryA.getId(), EvalTestDataLoad.ADMIN_USER_ID, false, true);
		Assert.assertNotNull( eItems );
		Assert.assertEquals(1, eItems.size());
		ids = EvalTestDataLoad.makeIdList(eItems);
		Assert.assertTrue(ids.contains( etdl.objectiveA1.getId() ));		

		// check trying to get groups from empty group
		eItems = expertItems.getItemGroups(etdl.categoryC.getId(), EvalTestDataLoad.ADMIN_USER_ID, false, true);
		Assert.assertNotNull( eItems );
		Assert.assertEquals(0, eItems.size());

		// test attempting to use invalid item group id
		try {
			eItems = expertItems.getItemGroups(EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.ADMIN_USER_ID, false, true);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
			//Assert.fail(e.getMessage()); // check the reason for failure
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#getItemsInItemGroup(java.lang.Long, boolean)}.
	 */
	public void testGetItemsInItemGroup() {
		List ids = null;
		List eItems = null;

		// check items from a low level group
		eItems = expertItems.getItemsInItemGroup(etdl.objectiveA1.getId(), true);
		Assert.assertNotNull( eItems );
		Assert.assertEquals(2, eItems.size());
		ids = EvalTestDataLoad.makeIdList(eItems);
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		Assert.assertTrue(ids.contains( etdl.item6.getId() ));

		// check items from a top level group
		eItems = expertItems.getItemsInItemGroup(etdl.categoryB.getId(), true);
		Assert.assertNotNull( eItems );
		Assert.assertEquals(1, eItems.size());
		ids = EvalTestDataLoad.makeIdList(eItems);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));

		// check items from an empty group
		eItems = expertItems.getItemsInItemGroup(etdl.objectiveA2.getId(), true);
		Assert.assertNotNull( eItems );
		Assert.assertEquals(0, eItems.size());

		// test attempting to use invalid item group id
		try {
			eItems = expertItems.getItemsInItemGroup(EvalTestDataLoad.INVALID_LONG_ID, true);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#createItemGroup(org.sakaiproject.evaluation.model.EvalItemGroup, java.lang.String, java.lang.Long)}.
	 */
	public void testCreateItemGroup() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#updateItemGroup(org.sakaiproject.evaluation.model.EvalItemGroup, java.lang.String)}.
	 */
	public void testUpdateItemGroup() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#removeItemGroup(java.lang.Long, java.lang.String, boolean)}.
	 */
	public void testRemoveItemGroup() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#canControlItemGroup(java.lang.String, java.lang.Long)}.
	 */
	public void testCanControlItemGroup() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#addGroupsToItemGroup(java.lang.Long, java.lang.Long[])}.
	 */
	public void testAddGroupsToItemGroup() {
		//fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#addItemsToItemGroup(java.lang.Long, java.lang.Long[])}.
	 */
	public void testAddItemsToItemGroup() {
		//fail("Not yet implemented"); // TODO
	}

}
