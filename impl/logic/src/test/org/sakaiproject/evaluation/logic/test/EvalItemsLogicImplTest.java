/******************************************************************************
 * EvalItemsLogicImplTest.java - created by aaronz@vt.edu
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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl;
import org.sakaiproject.evaluation.logic.test.stubs.EvalExternalLogicStub;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestData;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * Test class for EvalItemsLogicImpl
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalItemsLogicImplTest extends AbstractTransactionalSpringContextTests {

	protected EvalItemsLogicImpl items;

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
		items = new EvalItemsLogicImpl();
		items.setDao(evaluationDao);
		items.setExternalLogic( new EvalExternalLogicStub() );

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
		// this test is just making sure that hibernate is actually linking the items
		// to the templates the way we think it is
		List ids = null;

		// check the full count of perloaded items
		Assert.assertEquals(10, evaluationDao.countAll(EvalTemplateItem.class) );

		EvalTemplate template = (EvalTemplate) 
			evaluationDao.findById(EvalTemplate.class, etdl.templateAdmin.getId());

		// No longer supporting this type of linkage between templates and items
//		Set items = template.getItems();
//		Assert.assertNotNull( items );
//		Assert.assertEquals(3, items.size());

		Set tItems = template.getTemplateItems();
		Assert.assertNotNull( tItems );
		Assert.assertEquals(3, tItems.size());
		ids = EvalTestDataLoad.makeIdList(tItems);
		Assert.assertTrue(ids.contains( etdl.templateItem2A.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem3A.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem5A.getId() ));
		// get the items from the templateItems
		List l = new ArrayList();
		for (Iterator iter = tItems.iterator(); iter.hasNext();) {
			EvalTemplateItem eti = (EvalTemplateItem) iter.next();
			Assert.assertTrue( eti.getItem() instanceof EvalItem );
			Assert.assertEquals(eti.getTemplate().getId(), template.getId());
			l.add(eti.getItem().getId());
		}
		Assert.assertTrue(l.contains( etdl.item2.getId() ));
		Assert.assertTrue(l.contains( etdl.item3.getId() ));
		Assert.assertTrue(l.contains( etdl.item5.getId() ));

		// test getting another set of items
		EvalItem item = (EvalItem) evaluationDao.findById(EvalItem.class, etdl.item1.getId());
		Set itItems = item.getTemplateItems();
		Assert.assertNotNull( itItems );
		Assert.assertEquals(2, itItems.size());
		ids = EvalTestDataLoad.makeIdList(itItems);
		Assert.assertTrue(ids.contains( etdl.templateItem1P.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem1User.getId() ));

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemById(java.lang.Long)}.
	 */
	public void testGetItemById() {
		EvalItem item = null;

		// test getting valid items by id
		item = items.getItemById( etdl.item1.getId() );
		Assert.assertNotNull(item);
		Assert.assertEquals(etdl.item1.getId(), item.getId());

		item = items.getItemById( etdl.item5.getId() );
		Assert.assertNotNull(item);
		Assert.assertEquals(etdl.item5.getId(), item.getId());

		// test get eval by invalid id returns null
		item = items.getItemById( EvalTestDataLoad.INVALID_LONG_ID );
		Assert.assertNull(item);
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#saveItem(org.sakaiproject.evaluation.model.EvalItem, java.lang.String)}.
	 */
	public void testSaveItem() {
		String test_text = "test item text";
		String test_desc = "test item description";

		// test saving a valid item
		items.saveItem( new EvalItem( new Date(), 
				EvalTestDataLoad.MAINT_USER_ID, test_text, test_desc, 
				EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_SCALED, 
				EvalTestDataLoad.NOT_EXPERT, "expert desc", Boolean.FALSE, null, 
				EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, EvalConstants.ITEM_CATEGORY_COURSE, 
				null, null, null, etdl.scale1, null, null, null, EvalTestDataLoad.UNLOCKED), 
				EvalTestDataLoad.MAINT_USER_ID);

		// test saving valid item locked
		items.saveItem( new EvalItem( new Date(), 
				EvalTestDataLoad.MAINT_USER_ID, test_text, test_desc, 
				EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_TEXT, 
				EvalTestDataLoad.NOT_EXPERT, "expert desc", Boolean.FALSE, new Integer(2), 
				null, EvalConstants.ITEM_CATEGORY_COURSE, 
				null, null, null, null, null, null, null, EvalTestDataLoad.LOCKED), 
				EvalTestDataLoad.MAINT_USER_ID);

		// test saving valid item with no date and lock specified ok
		EvalItem eiTest1 = new EvalItem( null, 
				EvalTestDataLoad.MAINT_USER_ID, test_text, test_desc, 
				EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_TEXT, 
				EvalTestDataLoad.NOT_EXPERT, "expert desc", Boolean.FALSE, new Integer(2), 
				null, EvalConstants.ITEM_CATEGORY_COURSE, 
				null, null, null, null, null, null, null, null);
		items.saveItem( eiTest1, 
				EvalTestDataLoad.MAINT_USER_ID);
		// make sure the values are filled in for us
		Assert.assertNotNull( eiTest1.getLastModified() );
		Assert.assertNotNull( eiTest1.getLocked() );

		// test saving scaled item with no scale fails
		try {
			items.saveItem( new EvalItem( null, 
					EvalTestDataLoad.MAINT_USER_ID, test_text, 
					EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_SCALED, 
					EvalTestDataLoad.NOT_EXPERT), 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test saving scaled item with scale set AND text size fails
		try {
			items.saveItem( new EvalItem( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, test_text, test_desc, 
					EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_SCALED, 
					EvalTestDataLoad.NOT_EXPERT, "expert desc", Boolean.FALSE, new Integer(3), 
					EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, EvalConstants.ITEM_CATEGORY_COURSE, 
					null, null, null, etdl.scale2, null, null, null, EvalTestDataLoad.UNLOCKED), 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test saving text item with no text size fails
		try {
			items.saveItem( new EvalItem( null, 
					EvalTestDataLoad.MAINT_USER_ID, test_text, 
					EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_TEXT, 
					EvalTestDataLoad.NOT_EXPERT), 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test saving text item with scale set fails
		try {
			items.saveItem( new EvalItem( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, test_text, test_desc, 
					EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_TEXT, 
					EvalTestDataLoad.NOT_EXPERT, "expert desc", Boolean.FALSE, new Integer(3), 
					EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, EvalConstants.ITEM_CATEGORY_COURSE, 
					null, null, null, etdl.scale2, null, null, null, EvalTestDataLoad.UNLOCKED), 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test saving header type item with scale or text size set fails
		try {
			items.saveItem( new EvalItem( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, test_text, test_desc, 
					EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_HEADER, 
					EvalTestDataLoad.NOT_EXPERT, "expert desc", Boolean.FALSE, new Integer(3), 
					EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, EvalConstants.ITEM_CATEGORY_COURSE, 
					null, null, null, etdl.scale2, null, null, null, EvalTestDataLoad.UNLOCKED), 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test saving block item without block id fails
		// NOTE: BLOCK ITEMS must have a scale specified
		try {
			items.saveItem( new EvalItem( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, test_text, test_desc, 
					EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_BLOCK, 
					EvalTestDataLoad.NOT_EXPERT, "expert desc", Boolean.FALSE, null, 
					EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, EvalConstants.ITEM_CATEGORY_COURSE, 
					null, null, null, etdl.scale1, null, null, null, EvalTestDataLoad.UNLOCKED), 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// fetch items to work with (for editing tests)
		EvalItem testItem1 = (EvalItem) evaluationDao.findById(EvalItem.class, 
				etdl.item4.getId());
		EvalItem testItem2 = (EvalItem) evaluationDao.findById(EvalItem.class, 
				etdl.item6.getId());
		EvalItem testItem3 = (EvalItem) evaluationDao.findById(EvalItem.class, 
				etdl.item7.getId());
		EvalItem testItem4 = (EvalItem) evaluationDao.findById(EvalItem.class, 
				etdl.item1.getId());

		// test editing unlocked item
		testItem1.setDescription("something maint user new");
		items.saveItem( testItem1, 
				EvalTestDataLoad.MAINT_USER_ID);

		// TODO - CANNOT RUN THIS TEST FOR NOW BECAUSE OF HIBERNATE
//		// test that LOCKED cannot be changed to FALSE on existing item
//		try {
//			testItem3.setLocked(Boolean.FALSE);
//			items.saveItem( testItem3, 
//					EvalTestDataLoad.ADMIN_USER_ID);
//			Assert.fail("Should have thrown exception");
//		} catch (RuntimeException e) {
//			Assert.assertNotNull(e);
//			Assert.fail(e.getMessage()); // see why failing
//		}

		// test editing LOCKED item fails
		try {
			testItem4.setExpert(Boolean.FALSE);
			items.saveItem( testItem4, 
					EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		// test admin can edit any item
		testItem2.setDescription("something admin new");
		items.saveItem( testItem2, 
				EvalTestDataLoad.ADMIN_USER_ID);

		// test that editing unowned item causes permission failure
		try {
			testItem3.setDescription("something maint new");
			items.saveItem( testItem3, 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (SecurityException e) {
			Assert.assertNotNull(e);
		}

		// test that setting sharing to PUBLIC as non-admin fails
		try {
			testItem1.setSharing(EvalConstants.SHARING_PUBLIC);
			items.saveItem( testItem1, 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test admin can set sharing to public
		testItem1.setSharing(EvalConstants.SHARING_PUBLIC);
		items.saveItem( testItem1, 
				EvalTestDataLoad.ADMIN_USER_ID);

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#deleteItem(java.lang.Long, java.lang.String)}.
	 */
	public void testDeleteItem() {
		// test removing item without permissions fails
		try {
			items.deleteItem(etdl.item7.getId(), 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (SecurityException e) {
			Assert.assertNotNull(e);
		}

		try {
			items.deleteItem(etdl.item4.getId(), 
					EvalTestDataLoad.USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (SecurityException e) {
			Assert.assertNotNull(e);
		}

		// test removing locked item fails
		try {
			items.deleteItem(etdl.item2.getId(), 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		try {
			items.deleteItem(etdl.item1.getId(), 
					EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		// test cannot remove expert item
		try {
			items.deleteItem(etdl.item6.getId(), 
					EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		// test removing unused item OK
		items.deleteItem(etdl.item4.getId(), 
				EvalTestDataLoad.MAINT_USER_ID);
		Assert.assertNull( items.getItemById(etdl.item4.getId()) );

		items.deleteItem(etdl.item7.getId(), 
				EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNull( items.getItemById(etdl.item7.getId()) );

		// test removing invalid item id fails
		try {
			items.deleteItem(EvalTestDataLoad.INVALID_LONG_ID, 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemsForUser(java.lang.String, java.lang.String)}.
	 */
	public void testGetItemsForUser() {
		List l = null;
		List ids = null;

		// test getting all items for the admin user
		l = items.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, null);
		Assert.assertNotNull( l );
		Assert.assertEquals(7, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item4.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));
		Assert.assertTrue(ids.contains( etdl.item6.getId() ));
		Assert.assertTrue(ids.contains( etdl.item7.getId() ));

		// same as getting all items
		l = items.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, 
				EvalConstants.SHARING_OWNER);
		Assert.assertNotNull( l );
		Assert.assertEquals(7, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item4.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));
		Assert.assertTrue(ids.contains( etdl.item6.getId() ));
		Assert.assertTrue(ids.contains( etdl.item7.getId() ));

		// test getting all items for the maint user
		l = items.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, null);
		Assert.assertNotNull( l );
		Assert.assertEquals(6, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item4.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));
		Assert.assertTrue(ids.contains( etdl.item6.getId() ));

		// test getting all items for the normal user
		l = items.getItemsForUser(EvalTestDataLoad.USER_ID, null);
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));

		// test getting private items for the admin user (all private items)
		l = items.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID,
				EvalConstants.SHARING_PRIVATE);
		Assert.assertNotNull( l );
		Assert.assertEquals(5, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item4.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));
		Assert.assertTrue(ids.contains( etdl.item6.getId() ));
		Assert.assertTrue(ids.contains( etdl.item7.getId() ));

		// test getting private items for the maint user
		l = items.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, 
				EvalConstants.SHARING_PRIVATE);
		Assert.assertNotNull( l );
		Assert.assertEquals(4, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item4.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));
		Assert.assertTrue(ids.contains( etdl.item6.getId() ));

		// test getting private items for the user
		l = items.getItemsForUser(EvalTestDataLoad.USER_ID, 
				EvalConstants.SHARING_PRIVATE);
		Assert.assertNotNull( l );
		Assert.assertEquals(0, l.size());

		// test getting public items for the admin user
		l = items.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, 
				EvalConstants.SHARING_PUBLIC);
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));

		// test getting public items for the maint user
		l = items.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, 
				EvalConstants.SHARING_PUBLIC);
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));

		// test getting public items for the user
		l = items.getItemsForUser(EvalTestDataLoad.USER_ID, 
				EvalConstants.SHARING_PUBLIC);
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));

		// test getting items for invalid user returns public only
		l = items.getItemsForUser( EvalTestDataLoad.INVALID_USER_ID, null );
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));

		// test invalid sharing constant causes failure
		try {
			items.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, 
					EvalTestDataLoad.INVALID_CONSTANT_STRING );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemsForTemplate(java.lang.Long)}.
	 */
	public void testGetItemsForTemplate() {
		List l = null;
		List ids = null;

		// test getting all items by valid templates
		l = items.getItemsForTemplate( etdl.templateAdmin.getId(), null );
		Assert.assertNotNull( l );
		Assert.assertEquals(3, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));

		// test getting all items by valid templates
		l = items.getItemsForTemplate( etdl.templatePublic.getId(), null );
		Assert.assertNotNull( l );
		Assert.assertEquals(1, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));

		// test getting items from template with no items
		l = items.getItemsForTemplate( etdl.templateAdminNoItems.getId(), null );
		Assert.assertNotNull( l );
		Assert.assertEquals(0, l.size());

		// test getting items for specific user returns correct items
		// admin should get all items
		l = items.getItemsForTemplate( etdl.templateAdmin.getId(), 
				EvalTestDataLoad.ADMIN_USER_ID );
		Assert.assertNotNull( l );
		Assert.assertEquals(3, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));

		l = items.getItemsForTemplate( etdl.templateUnused.getId(), 
				EvalTestDataLoad.ADMIN_USER_ID );
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));

		// owner should see all items
		l = items.getItemsForTemplate( etdl.templateUnused.getId(), 
				EvalTestDataLoad.MAINT_USER_ID );
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));

		l = items.getItemsForTemplate( etdl.templateUser.getId(), 
				EvalTestDataLoad.USER_ID );
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));

		// TODO - takers should see items at their level (one level) if they have access
		l = items.getItemsForTemplate( etdl.templateUser.getId(), 
				EvalTestDataLoad.STUDENT_USER_ID );
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));

		// TODO - add in tests that take the hierarchy into account

		// test getting items from invalid template fails
		try {
			items.getItemsForTemplate( EvalTestDataLoad.INVALID_LONG_ID, null );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// TODO - MAKE this work later on
//		// test getting items for invalid user returns nothing
//		l = items.getItemsForTemplate( etdl.templatePublic.getId(), 
//				EvalTestDataLoad.INVALID_USER_ID );
//		Assert.assertNotNull( l );
//		Assert.assertEquals(0, l.size());

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getTemplateItemById(java.lang.Long)}.
	 */
	public void testGetTemplateItemById() {
		EvalTemplateItem templateItem = null;

		// test getting valid templateItems by id
		templateItem = items.getTemplateItemById( etdl.templateItem1P.getId() );
		Assert.assertNotNull(templateItem);
		Assert.assertEquals(etdl.templateItem1P.getId(), templateItem.getId());

		templateItem = items.getTemplateItemById( etdl.templateItem1User.getId() );
		Assert.assertNotNull(templateItem);
		Assert.assertEquals(etdl.templateItem1User.getId(), templateItem.getId());

		// test get eval by invalid id returns null
		templateItem = items.getTemplateItemById( EvalTestDataLoad.INVALID_LONG_ID );
		Assert.assertNull(templateItem);
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#saveTemplateItem(org.sakaiproject.evaluation.model.EvalTemplateItem, java.lang.String)}.
	 */
	public void testSaveTemplateItem() {
//		 TODO fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#deleteTemplateItem(java.lang.Long, java.lang.String)}.
	 */
	public void testDeleteTemplateItem() {
//		 TODO fail("Not yet implemented");
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getTemplateItemsForTemplate(java.lang.Long)}.
	 */
	public void testGetTemplateItemsForTemplate() {
//		 TODO fail("Not yet implemented");
	}


	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getNextBlockId()}.
	 */
	public void testGetNextBlockId() {
//		 TODO fail("Not yet implemented");
	}


	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#canControlItem(java.lang.String, java.lang.Long)}.
	 */
	public void testCanControlItem() {
		// test can control owned items
		Assert.assertTrue( items.canControlItem( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.item7.getId() ) );
		Assert.assertTrue( items.canControlItem( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.item4.getId() ) );

		// test admin user can override perms
		Assert.assertTrue( items.canControlItem( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.item4.getId() ) );

		// test cannot control unowned items
		Assert.assertFalse( items.canControlItem( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.item7.getId() ) );
		Assert.assertFalse( items.canControlItem( EvalTestDataLoad.USER_ID, 
				etdl.item4.getId() ) );

		// test cannot control locked items
		Assert.assertFalse( items.canControlItem( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.item1.getId() ) );
		Assert.assertFalse( items.canControlItem( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.item2.getId() ) );

		// test invalid item id causes failure
		try {
			items.canControlItem( EvalTestDataLoad.MAINT_USER_ID, 
					EvalTestDataLoad.INVALID_LONG_ID );
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#canControlTemplateItem(java.lang.String, java.lang.Long)}.
	 */
	public void testCanControlTemplateItem() {
		// test can control owned items
		Assert.assertTrue( items.canControlTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.templateItem3PU.getId() ) );
		Assert.assertTrue( items.canControlTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.templateItem3U.getId() ) );

		// test admin user can override perms
		Assert.assertTrue( items.canControlTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.templateItem3U.getId() ) );

		// test cannot control unowned items
		Assert.assertFalse( items.canControlTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.templateItem3PU.getId() ) );
		Assert.assertFalse( items.canControlTemplateItem( EvalTestDataLoad.USER_ID, 
				etdl.templateItem3U.getId() ) );

		// test cannot control items locked by locked template
		Assert.assertFalse( items.canControlTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.templateItem2A.getId() ) );
		Assert.assertFalse( items.canControlTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.templateItem1P.getId() ) );

		// test invalid item id causes failure
		try {
			items.canControlTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
					EvalTestDataLoad.INVALID_LONG_ID );
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}
	}

}
