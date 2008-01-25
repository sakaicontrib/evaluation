/**
 * $Id: EvalExpertItemsLogicImplTest.java 1000 Dec 26, 2006 10:07:31 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvalExpertItemsLogicImplTest.java - evaluation - Mar 6, 2007 10:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Academic Research in Educational Technologies
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

import junit.framework.Assert;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl;
import org.sakaiproject.evaluation.logic.test.mocks.MockEvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
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
		expertItems = new EvalExpertItemsLogicImpl();
		expertItems.setDao(evaluationDao);
		expertItems.setExternalLogic( new MockEvalExternalLogic() );

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
		List<Long> ids = null;
		List<EvalItemGroup> eItems = null;

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
      List<Long> ids = null;
      List<EvalItem> eItems = null;

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
	 * Test method for {@link EvalExpertItemsLogicImpl#getItemGroupById(Long)}
	 */
	public void testGetItemGroupById() {
		EvalItemGroup itemGroup = null;

		// test getting valid items by id
		itemGroup = expertItems.getItemGroupById( etdl.categoryA.getId() );
		Assert.assertNotNull(itemGroup);
		Assert.assertEquals(etdl.categoryA.getId(), itemGroup.getId());

		itemGroup = expertItems.getItemGroupById( etdl.objectiveA1.getId() );
		Assert.assertNotNull(itemGroup);
		Assert.assertEquals(etdl.objectiveA1.getId(), itemGroup.getId());

		// test get eval by invalid id returns null
		itemGroup = expertItems.getItemGroupById( EvalTestDataLoad.INVALID_LONG_ID );
		Assert.assertNull(itemGroup);
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#saveItemGroup(org.sakaiproject.evaluation.model.EvalItemGroup, java.lang.String)}.
	 */
	public void testSaveItemGroup() {

		// test create a valid group
		EvalItemGroup newCategory = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, 
				EvalConstants.ITEM_GROUP_TYPE_CATEGORY, "new category");
		expertItems.saveItemGroup(newCategory, EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNotNull( newCategory.getId() );

		// check that defaults were filled in
		Assert.assertNotNull( newCategory.getLastModified() );
		Assert.assertNotNull( newCategory.getExpert() );
		Assert.assertEquals( newCategory.getExpert().booleanValue(), false );
		Assert.assertNull( newCategory.getParent() );

		// test that creating subgroup without parent causes failure
		EvalItemGroup newObjective = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, 
				EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE, "new objective");
		try {
			expertItems.saveItemGroup(newObjective, EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
			//Assert.fail(e.getMessage()); // check the reason for failure
		}

		// test create a valid subgroup
		newObjective.setParent( newCategory );
		expertItems.saveItemGroup(newObjective, EvalTestDataLoad.ADMIN_USER_ID);

		// test non-admin cannot create expert group
		EvalItemGroup newExpertGroup = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, 
				EvalConstants.ITEM_GROUP_TYPE_CATEGORY, "new expert");
		newExpertGroup.setExpert( Boolean.TRUE );
		try {
			expertItems.saveItemGroup(newExpertGroup, EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test admin can create expert group
		expertItems.saveItemGroup(newExpertGroup, EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNotNull( newExpertGroup.getId() );

		// test creating invalid expert group type fails
		try {
			expertItems.saveItemGroup( 
				new EvalItemGroup(new Date(), EvalTestDataLoad.ADMIN_USER_ID, EvalTestDataLoad.INVALID_CONSTANT_STRING,
						"test", "desc", Boolean.TRUE, null, null), 
				EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test creating top level category with parent fails
		try {
			expertItems.saveItemGroup( 
				new EvalItemGroup(new Date(), EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.ITEM_GROUP_TYPE_CATEGORY,
						"test", "desc", Boolean.FALSE, newCategory, null), 
				EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test trying to put objective as a top level category
		try {
			expertItems.saveItemGroup( 
				new EvalItemGroup(new Date(), EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE,
						"test", "desc", Boolean.FALSE, null, null), 
				EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#removeItemGroup(java.lang.Long, java.lang.String, boolean)}.
	 */
	public void testRemoveItemGroup() {

		// test cannot remove item groups without permission
		try {
			expertItems.removeItemGroup(etdl.categoryD.getId(), EvalTestDataLoad.MAINT_USER_ID, false);
			Assert.fail("Should have thrown exception");
		} catch (SecurityException e) {
			Assert.assertNotNull(e);
			//Assert.fail(e.getMessage()); // check the reason for failure
		}

		// test can remove empty categories
		expertItems.removeItemGroup(etdl.categoryD.getId(), EvalTestDataLoad.ADMIN_USER_ID, false);
		Assert.assertNull( expertItems.getItemGroupById(etdl.categoryD.getId()) );

		// test cannot remove non-empty categories when flag set
		try {
			expertItems.removeItemGroup(etdl.categoryA.getId(), EvalTestDataLoad.ADMIN_USER_ID, false);
			Assert.fail("Should have thrown exception");
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
			//Assert.fail(e.getMessage()); // check the reason for failure
		}

		// test can remove non-empty categories when flag unset
		expertItems.removeItemGroup(etdl.categoryA.getId(), EvalTestDataLoad.ADMIN_USER_ID, true);
		Assert.assertNull( expertItems.getItemGroupById(etdl.categoryA.getId()) );

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#canUpdateItemGroup(String, Long)}.
	 */
	public void testCanUpdateItemGroup() {
		// test can control owned items
		Assert.assertTrue( expertItems.canUpdateItemGroup(EvalTestDataLoad.ADMIN_USER_ID, etdl.categoryA.getId() ) );
		Assert.assertTrue( expertItems.canUpdateItemGroup(EvalTestDataLoad.ADMIN_USER_ID, etdl.objectiveA1.getId() ) );

		// test cannot control unowned items
		Assert.assertFalse( expertItems.canUpdateItemGroup(EvalTestDataLoad.MAINT_USER_ID, etdl.categoryA.getId() ) );
		Assert.assertFalse( expertItems.canUpdateItemGroup(EvalTestDataLoad.MAINT_USER_ID, etdl.objectiveA1.getId() ) );

		// test invalid item id causes failure
		try {
			expertItems.canUpdateItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalTestDataLoad.INVALID_LONG_ID );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#canRemoveItemGroup(String, Long)}.
	 */
	public void testCanRemoveItemGroup() {
		// test can control owned items
		Assert.assertTrue( expertItems.canRemoveItemGroup(EvalTestDataLoad.ADMIN_USER_ID, etdl.categoryA.getId() ) );
		Assert.assertTrue( expertItems.canRemoveItemGroup(EvalTestDataLoad.ADMIN_USER_ID, etdl.objectiveA1.getId() ) );

		// test cannot control unowned items
		Assert.assertFalse( expertItems.canRemoveItemGroup(EvalTestDataLoad.MAINT_USER_ID, etdl.categoryA.getId() ) );
		Assert.assertFalse( expertItems.canRemoveItemGroup(EvalTestDataLoad.MAINT_USER_ID, etdl.objectiveA1.getId() ) );

		// test invalid item id causes failure
		try {
			expertItems.canRemoveItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalTestDataLoad.INVALID_LONG_ID );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

	}

}
