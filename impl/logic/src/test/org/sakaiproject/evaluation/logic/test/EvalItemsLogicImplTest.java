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
import org.sakaiproject.evaluation.logic.EvalSettings;
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
		EvalSettings settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
		if (settings == null) {
			throw new NullPointerException("EvalSettings could not be retrieved from spring evalGroupId");
		}

		// setup the mock objects if needed

		// create and setup the object to be tested
		items = new EvalItemsLogicImpl();
		items.setDao(evaluationDao);
		items.setExternalLogic( new EvalExternalLogicStub() );
		items.setEvalSettings(settings);

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

		Assert.assertEquals(46, evaluationDao.countAll(EvalItem.class) );

		// check the full count of preloaded items
		Assert.assertEquals(19, evaluationDao.countAll(EvalTemplateItem.class) );

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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemByEid(java.lang.String)}.
	 */
	public void testGetItemByEid() {
		EvalItem item = null;

		// test getting valid items having eid set
		item = items.getItemByEid( etdl.item1Eid.getEid() );
		Assert.assertNotNull(item);
		Assert.assertEquals(etdl.item1Eid.getEid(), item.getEid());
		
		item = items.getItemByEid( etdl.item2Eid.getEid() );
		Assert.assertNotNull(item);
		Assert.assertEquals(etdl.item2Eid.getEid(), item.getEid());
		
		item = items.getItemByEid( etdl.item3Eid.getEid() );
		Assert.assertNotNull(item);
		Assert.assertEquals(etdl.item3Eid.getEid(), item.getEid());

		//test getting valid item not having eid set returns null
		item = items.getItemByEid( etdl.item5.getEid() );
		Assert.assertNull(item);

		// test getting item by invalid id returns null
		item = items.getItemByEid( EvalTestDataLoad.INVALID_STRING_EID );
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
				EvalTestDataLoad.NOT_EXPERT, "expert desc", etdl.scale1, null,
				Boolean.FALSE, null, EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, 
				EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.UNLOCKED), 
				EvalTestDataLoad.MAINT_USER_ID);

		// test saving valid item locked
		items.saveItem( new EvalItem( new Date(), 
				EvalTestDataLoad.MAINT_USER_ID, test_text, test_desc, 
				EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_TEXT, 
				EvalTestDataLoad.NOT_EXPERT, "expert desc", null, null,
				null, new Integer(2), null, 
				EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.LOCKED), 
				EvalTestDataLoad.MAINT_USER_ID);

		// test saving valid item with no date, NA, and lock specified ok
		EvalItem eiTest1 = new EvalItem( null, 
				EvalTestDataLoad.MAINT_USER_ID, test_text, test_desc, 
				EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_TEXT, 
				EvalTestDataLoad.NOT_EXPERT, "expert desc", null, null,
				null, new Integer(2), null, 
				EvalConstants.ITEM_CATEGORY_COURSE, null);
		items.saveItem( eiTest1, 
				EvalTestDataLoad.MAINT_USER_ID);
		// make sure the values are filled in for us
		Assert.assertNotNull( eiTest1.getLastModified() );
		Assert.assertNotNull( eiTest1.getLocked() );
		Assert.assertNotNull( eiTest1.getUsesNA() );

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
					EvalTestDataLoad.NOT_EXPERT, "expert desc", etdl.scale2, null,
					Boolean.FALSE, new Integer(3), EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, 
					EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.UNLOCKED),
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
					EvalTestDataLoad.NOT_EXPERT, "expert desc", etdl.scale2, null,
					Boolean.FALSE, null, EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, 
					EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.UNLOCKED),
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
					EvalTestDataLoad.NOT_EXPERT, "expert desc", etdl.scale2, null,
					Boolean.FALSE, new Integer(3), EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, 
					EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.UNLOCKED),
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

		// ADMIN CAN REMOVE EXPERT ITEMS NOW -AZ
//		// test cannot remove expert item
//		try {
//			items.deleteItem(etdl.item6.getId(), 
//					EvalTestDataLoad.ADMIN_USER_ID);
//			Assert.fail("Should have thrown exception");
//		} catch (IllegalStateException e) {
//			Assert.assertNotNull(e);
//		}

		// test removing expert item ok for admin
		items.deleteItem(etdl.item6.getId(), 
				EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNull( items.getItemById(etdl.item6.getId()) );

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
	 * This tests the ability to remove the item and scale at the same time
	 */
	public void testDeleteItemAndScale() {
	   // create a test MC item
      String[] options1 = {"one", "two", "three"};
      EvalScale scale1 = new EvalScale(new Date(), EvalTestDataLoad.ADMIN_USER_ID, "Scale MC", 
            EvalConstants.SCALE_MODE_ADHOC, EvalConstants.SHARING_PRIVATE, false, 
            "description", null, options1, false);
      evaluationDao.save(scale1);

	   EvalItem item1 = new EvalItem(new Date(), EvalTestDataLoad.ADMIN_USER_ID, "mutli choice", EvalConstants.SHARING_PRIVATE, 
	         EvalConstants.ITEM_TYPE_MULTIPLECHOICE, false);
	   item1.setScale(scale1);
	   item1.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL);
	   items.saveItem(item1, EvalTestDataLoad.ADMIN_USER_ID);

	   // check that the item and scale are saved
	   assertNotNull( evaluationDao.findById(EvalScale.class, scale1.getId()) );
      assertNotNull( evaluationDao.findById(EvalItem.class, item1.getId()) );

      items.deleteItem(item1.getId(), EvalTestDataLoad.ADMIN_USER_ID);

      // not check that they are both gone
      assertNull( evaluationDao.findById(EvalItem.class, item1.getId()) );
      assertNull( evaluationDao.findById(EvalScale.class, scale1.getId()) );

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemsForUser(java.lang.String, java.lang.String)}.
	 */
	public void testGetItemsForUser() {
		List<EvalItem> l = null;
		List<Long> ids = null;
		// NOTE: 32 preloaded public expert items to take into account currently
		int preloadedCount = 32;

		// test getting all items for the admin user
		l = items.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, null, null, true);
		Assert.assertNotNull( l );
		Assert.assertEquals(13 + preloadedCount, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item4.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));
		Assert.assertTrue(ids.contains( etdl.item6.getId() ));
		Assert.assertTrue(ids.contains( etdl.item7.getId() ));
		Assert.assertTrue(ids.contains( etdl.item8.getId() ));
		Assert.assertTrue(ids.contains( etdl.item10.getId() ));
		Assert.assertTrue(ids.contains( etdl.item11.getId() ));
		
		Assert.assertTrue(ids.contains( etdl.item1Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3Eid.getId() ));

		// same as getting all items
		l = items.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, 
				EvalConstants.SHARING_OWNER, null, true);
		Assert.assertNotNull( l );
		Assert.assertEquals(13 + preloadedCount, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item4.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));
		Assert.assertTrue(ids.contains( etdl.item6.getId() ));
		Assert.assertTrue(ids.contains( etdl.item7.getId() ));
		Assert.assertTrue(ids.contains( etdl.item8.getId() ));
		Assert.assertTrue(ids.contains( etdl.item10.getId() ));
		Assert.assertTrue(ids.contains( etdl.item11.getId() ));
		
		Assert.assertTrue(ids.contains( etdl.item1Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3Eid.getId() ));

		// test getting all items for the maint user
		l = items.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, null, null, true);
		Assert.assertNotNull( l );
		Assert.assertEquals(12 + preloadedCount, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item4.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));
		Assert.assertTrue(ids.contains( etdl.item6.getId() ));
		Assert.assertTrue(ids.contains( etdl.item8.getId() ));
		Assert.assertTrue(ids.contains( etdl.item10.getId() ));
		Assert.assertTrue(ids.contains( etdl.item11.getId() ));
		
		Assert.assertTrue(ids.contains( etdl.item1Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3Eid.getId() ));

		// test getting all items for the maint user without expert items
		l = items.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, null, null, false);
		Assert.assertNotNull( l );
		Assert.assertEquals(9, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item4.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));
		Assert.assertTrue(ids.contains( etdl.item8.getId() ));
		Assert.assertTrue(ids.contains( etdl.item10.getId() ));
		Assert.assertTrue(ids.contains( etdl.item11.getId() ));
		
		Assert.assertTrue(ids.contains( etdl.item1Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3Eid.getId() ));

		// test getting all items for the normal user
		l = items.getItemsForUser(EvalTestDataLoad.USER_ID, null, null, true);
		Assert.assertNotNull( l );
		Assert.assertEquals(5 + preloadedCount, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		
		Assert.assertTrue(ids.contains( etdl.item1Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3Eid.getId() ));

		// test getting private items for the admin user (all private items)
		l = items.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID,
				EvalConstants.SHARING_PRIVATE, null, true);
		Assert.assertNotNull( l );
		Assert.assertEquals(8, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item4.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));
		Assert.assertTrue(ids.contains( etdl.item6.getId() ));
		Assert.assertTrue(ids.contains( etdl.item7.getId() ));
		Assert.assertTrue(ids.contains( etdl.item8.getId() ));
		Assert.assertTrue(ids.contains( etdl.item10.getId() ));
		Assert.assertTrue(ids.contains( etdl.item11.getId() ));

		// test getting private all private items with filter
		l = items.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID,
				EvalConstants.SHARING_PRIVATE, "do you think", true);
		Assert.assertNotNull( l );
		Assert.assertEquals(3, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item4.getId() ));
		Assert.assertTrue(ids.contains( etdl.item11.getId() ));

		// test getting private all private items, expert excluded
		l = items.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID,
				EvalConstants.SHARING_PRIVATE, null, false);
		Assert.assertNotNull( l );
		Assert.assertEquals(7, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item4.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));
		Assert.assertTrue(ids.contains( etdl.item7.getId() ));
		Assert.assertTrue(ids.contains( etdl.item8.getId() ));
		Assert.assertTrue(ids.contains( etdl.item10.getId() ));
		Assert.assertTrue(ids.contains( etdl.item11.getId() ));

		// test getting private items for the maint user
		l = items.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, 
				EvalConstants.SHARING_PRIVATE, null, true);
		Assert.assertNotNull( l );
		Assert.assertEquals(7, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item3.getId() ));
		Assert.assertTrue(ids.contains( etdl.item4.getId() ));
		Assert.assertTrue(ids.contains( etdl.item5.getId() ));
		Assert.assertTrue(ids.contains( etdl.item6.getId() ));
		Assert.assertTrue(ids.contains( etdl.item8.getId() ));
		Assert.assertTrue(ids.contains( etdl.item10.getId() ));
		Assert.assertTrue(ids.contains( etdl.item11.getId() ));

		// test getting private items for the user
		l = items.getItemsForUser(EvalTestDataLoad.USER_ID, 
				EvalConstants.SHARING_PRIVATE, null, true);
		Assert.assertNotNull( l );
		Assert.assertEquals(0, l.size());

		// test getting public items for the admin user
		l = items.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, 
				EvalConstants.SHARING_PUBLIC, null, true);
		Assert.assertNotNull( l );
		Assert.assertEquals(5 + preloadedCount, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		
		Assert.assertTrue(ids.contains( etdl.item1Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3Eid.getId() ));

		// test getting public items for the maint user
		l = items.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, 
				EvalConstants.SHARING_PUBLIC, null, true);
		Assert.assertNotNull( l );
		Assert.assertEquals(5 + preloadedCount, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		
		Assert.assertTrue(ids.contains( etdl.item1Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3Eid.getId() ));
		
		
		// test getting public items for the user
		l = items.getItemsForUser(EvalTestDataLoad.USER_ID, 
				EvalConstants.SHARING_PUBLIC, null, true);
		Assert.assertNotNull( l );
		Assert.assertEquals(5 + preloadedCount, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		
		Assert.assertTrue(ids.contains( etdl.item1Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3Eid.getId() ));

		// test getting items for invalid user returns public only
		l = items.getItemsForUser( EvalTestDataLoad.INVALID_USER_ID, null, null, true);
		Assert.assertNotNull( l );
		Assert.assertEquals(5 + preloadedCount, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.item1.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2.getId() ));
		
		Assert.assertTrue(ids.contains( etdl.item1Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item2Eid.getId() ));
		Assert.assertTrue(ids.contains( etdl.item3Eid.getId() ));

		// test invalid sharing constant causes failure
		try {
			items.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, 
					EvalTestDataLoad.INVALID_CONSTANT_STRING, null, true);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemsForTemplate(java.lang.Long)}.
	 */
	public void testGetItemsForTemplate() {
		List<EvalItem> l = null;
		List<Long> ids = null;

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
      l = items.getItemsForTemplate( EvalTestDataLoad.INVALID_LONG_ID, null );
      Assert.assertNotNull( l );
      Assert.assertEquals(0, l.size());

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
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getTemplateItemById(java.lang.Long)}.
	 */
	public void testGetTemplateItemByEid() {
		EvalTemplateItem templateItem = null;

		// test getting valid templateItems by eid
		templateItem = items.getTemplateItemByEid( etdl.templateItem1Eid.getEid() );
		Assert.assertNotNull(templateItem);
		Assert.assertEquals(etdl.templateItem1Eid.getEid(), templateItem.getEid());

		templateItem = items.getTemplateItemByEid( etdl.templateItem2Eid.getEid() );
		Assert.assertNotNull(templateItem);
		Assert.assertEquals(etdl.templateItem2Eid.getEid(), templateItem.getEid());
		
		templateItem = items.getTemplateItemByEid( etdl.templateItem3Eid.getEid() );
		Assert.assertNotNull(templateItem);
		Assert.assertEquals(etdl.templateItem3Eid.getEid(), templateItem.getEid());
		
		//test getting valid template item not having eid set returns null
		templateItem = items.getTemplateItemByEid( etdl.templateItem1User.getEid() );
		Assert.assertNull(templateItem);

		//test getting template item using invalid eid returns null
		templateItem = items.getTemplateItemByEid( EvalTestDataLoad.INVALID_STRING_EID );
		Assert.assertNull(templateItem);
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#saveTemplateItem(org.sakaiproject.evaluation.model.EvalTemplateItem, java.lang.String)}.
	 */
	public void testSaveTemplateItem() {
		// load up a no items template to work with
		EvalTemplate noItems = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, etdl.templateAdminNoItems.getId());

		// test saving a new templateItem actually creates the linkage in the item and template
		EvalTemplateItem eiTest1 = new EvalTemplateItem( null, 
				EvalTestDataLoad.ADMIN_USER_ID, noItems, etdl.item5, 
				null, EvalConstants.ITEM_CATEGORY_COURSE, 
				EvalConstants.HIERARCHY_LEVEL_TOP, EvalConstants.HIERARCHY_NODE_ID_NONE,
				new Integer(3), null, Boolean.FALSE, null, null, null);
		items.saveTemplateItem( eiTest1, 
				EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNotNull( eiTest1.getItem() );
		Assert.assertNotNull( eiTest1.getTemplate() );
		Assert.assertNotNull( eiTest1.getItem().getTemplateItems() );
		Assert.assertNotNull( eiTest1.getTemplate().getTemplateItems() );
		// verify items are there
		Assert.assertEquals( eiTest1.getItem().getId(), etdl.item5.getId() );
		Assert.assertEquals( eiTest1.getTemplate().getId(), noItems.getId() );
		// check if the templateItem is contained in the new sets
		Assert.assertEquals( 4, eiTest1.getItem().getTemplateItems().size() );
		Assert.assertEquals( 1, eiTest1.getTemplate().getTemplateItems().size() );
		Assert.assertTrue( eiTest1.getItem().getTemplateItems().contains(eiTest1) );
		Assert.assertTrue( eiTest1.getTemplate().getTemplateItems().contains(eiTest1) );

		// make sure the displayOrder is set correctly when null (to 1)
		Assert.assertEquals( 1, eiTest1.getDisplayOrder().intValue() );

		// test saving a valid templateItem
		items.saveTemplateItem( new EvalTemplateItem( new Date(), 
				EvalTestDataLoad.ADMIN_USER_ID, noItems, etdl.item7, 
				new Integer(2), EvalConstants.ITEM_CATEGORY_COURSE, 
				EvalConstants.HIERARCHY_LEVEL_TOP, EvalConstants.HIERARCHY_NODE_ID_NONE,
				new Integer(3), null, Boolean.FALSE, null, null, null),
			EvalTestDataLoad.ADMIN_USER_ID);

		// test saving valid templateItem with locked item
		items.saveTemplateItem( new EvalTemplateItem( new Date(), 
				EvalTestDataLoad.MAINT_USER_ID, etdl.templateUnused, etdl.item2, 
				new Integer(3), EvalConstants.ITEM_CATEGORY_COURSE, 
				EvalConstants.HIERARCHY_LEVEL_TOP, EvalConstants.HIERARCHY_NODE_ID_NONE,
				null, EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, null, null, null),
			EvalTestDataLoad.MAINT_USER_ID);

		// test saving valid templateItem with empty required fields (inherit from item)
		EvalTemplateItem eiTest2 = new EvalTemplateItem( null, 
				EvalTestDataLoad.ADMIN_USER_ID, noItems, etdl.item4, 
				new Integer(99), null, 
				EvalConstants.HIERARCHY_LEVEL_TOP, EvalConstants.HIERARCHY_NODE_ID_NONE,
				null, null, null, null, null, null);
		items.saveTemplateItem( eiTest2, 
				EvalTestDataLoad.ADMIN_USER_ID);
		// make sure the values are filled in for us
		Assert.assertNotNull( eiTest2.getLastModified() );
		Assert.assertNotNull( eiTest2.getItemCategory() );
		Assert.assertNotNull( eiTest2.getScaleDisplaySetting() );
		Assert.assertNotNull( eiTest2.getUsesNA() );
		// make sure filled in values match the ones set in the item
		Assert.assertTrue( eiTest2.getItemCategory().equals(etdl.item4.getCategory()) );
		Assert.assertTrue( eiTest2.getScaleDisplaySetting().equals(etdl.item4.getScaleDisplaySetting()) );
		// not checking is UsesNA is equal because it is null in the item

		// make sure the displayOrder is set correctly (to 3) when set wrong
		Assert.assertEquals( 3, eiTest2.getDisplayOrder().intValue() );

		// test saving templateItem with no item fails
		try {
			items.saveTemplateItem( new EvalTemplateItem( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, etdl.templateUnused, null, 
					new Integer(3), EvalConstants.ITEM_CATEGORY_COURSE, 
					EvalConstants.HIERARCHY_LEVEL_TOP, EvalConstants.HIERARCHY_NODE_ID_NONE,
					null, EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, null, null, null),
				EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test saving templateItem with no template fails
		try {
			items.saveTemplateItem( new EvalTemplateItem( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, null, etdl.item3, 
					new Integer(3), EvalConstants.ITEM_CATEGORY_COURSE, 
					EvalConstants.HIERARCHY_LEVEL_TOP, EvalConstants.HIERARCHY_NODE_ID_NONE,
					null, EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, null, null, null),
				EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test saving scaled item with text size set fails
		try {
			items.saveTemplateItem( new EvalTemplateItem( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, etdl.templateUnused, etdl.item4, 
					new Integer(3), EvalConstants.ITEM_CATEGORY_COURSE, 
					EvalConstants.HIERARCHY_LEVEL_TOP, EvalConstants.HIERARCHY_NODE_ID_NONE,
					new Integer(2), EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, null, null, null),
				EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test saving text item with scale display setting fails
		try {
			items.saveTemplateItem( new EvalTemplateItem( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, etdl.templateUnused, etdl.item6, 
					new Integer(3), EvalConstants.ITEM_CATEGORY_COURSE, 
					EvalConstants.HIERARCHY_LEVEL_TOP, EvalConstants.HIERARCHY_NODE_ID_NONE,
					new Integer(4), EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, null, null, null),
				EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// TODO - add logic to not allow an item to be associated with the same template twice?
//		// test saving header type item with scale setting or text size set fails
//		try {
//			items.saveTemplateItem( new EvalTemplateItem( new Date(), 
//					EvalTestDataLoad.MAINT_USER_ID, etdl.templateUnused, etdl.item3, 
//					new Integer(3), EvalConstants.ITEM_CATEGORY_COURSE, 
//					null, EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, null, null),
//				EvalTestDataLoad.MAINT_USER_ID);
//			Assert.fail("Should have thrown exception");
//		} catch (IllegalArgumentException e) {
//			Assert.assertNotNull(e);
//			Assert.fail(e.getMessage()); // see why failing
//		}

		// test saving header type item with scale setting or text size set fails
		try {
			items.saveTemplateItem( new EvalTemplateItem( new Date(), 
					EvalTestDataLoad.MAINT_USER_ID, etdl.templateUnused, etdl.item8, 
					new Integer(3), EvalConstants.ITEM_CATEGORY_COURSE, 
					EvalConstants.HIERARCHY_LEVEL_TOP, EvalConstants.HIERARCHY_NODE_ID_NONE,
					new Integer(1), EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, null, null, null),
				EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// fetch items to work with (for editing tests)
		EvalTemplateItem testTemplateItem1 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, 
				etdl.templateItem3PU.getId()); // ADMIN, editable
		EvalTemplateItem testTemplateItem2 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, 
				etdl.templateItem3U.getId()); // MAINT, editable
		EvalTemplateItem testTemplateItem3 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, 
				etdl.templateItem3A.getId()); // ADMIN, uneditable
		EvalTemplateItem testTemplateItem4 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, 
				etdl.templateItem1P.getId()); // MAINT, uneditable

		// test editing templateItem not in LOCKED templateItem
		testTemplateItem1.setItemCategory( EvalConstants.ITEM_CATEGORY_INSTRUCTOR );
		items.saveTemplateItem( testTemplateItem1, EvalTestDataLoad.ADMIN_USER_ID );

		testTemplateItem2.setItemCategory( EvalConstants.ITEM_CATEGORY_INSTRUCTOR );
		items.saveTemplateItem( testTemplateItem2, EvalTestDataLoad.MAINT_USER_ID );

		// TODO - CANNOT RUN THIS TEST FOR NOW BECAUSE OF HIBERNATE
//		// test that template and item cannot be changed on existing templateItem
//		try {
//			testTemplateItem1.setItem( etdl.item1 );
//			items.saveTemplateItem( testTemplateItem1, EvalTestDataLoad.ADMIN_USER_ID );
//			Assert.fail("Should have thrown exception");
//		} catch (IllegalStateException e) {
//			Assert.assertNotNull(e);
//			Assert.fail(e.getMessage()); // see why failing
//		}
//
//		try {
//			testTemplateItem1.setTemplate( etdl.templateAdminNoItems );
//			items.saveTemplateItem( testTemplateItem1, EvalTestDataLoad.ADMIN_USER_ID );
//			Assert.fail("Should have thrown exception");
//		} catch (IllegalStateException e) {
//			Assert.assertNotNull(e);
//			Assert.fail(e.getMessage()); // see why failing
//		}

		// test editing templateItem in LOCKED template fails
		try {
			testTemplateItem3.setItemCategory( EvalConstants.ITEM_CATEGORY_INSTRUCTOR );
			items.saveTemplateItem( testTemplateItem3, EvalTestDataLoad.ADMIN_USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		try {
			testTemplateItem4.setItemCategory( EvalConstants.ITEM_CATEGORY_INSTRUCTOR );
			items.saveTemplateItem( testTemplateItem4, EvalTestDataLoad.MAINT_USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		// test admin can edit any templateItem
		testTemplateItem2.setItemCategory( EvalConstants.ITEM_CATEGORY_ENVIRONMENT );
		items.saveTemplateItem( testTemplateItem2, EvalTestDataLoad.ADMIN_USER_ID );

		// test that editing unowned templateItem causes permission failure
		try {
			testTemplateItem2.setItemCategory( EvalConstants.ITEM_CATEGORY_COURSE );
			items.saveTemplateItem( testTemplateItem2, EvalTestDataLoad.USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (SecurityException e) {
			Assert.assertNotNull(e);
		}

		// test that editing unowned templateItem causes permission failure
		try {
			testTemplateItem1.setItemCategory( EvalConstants.ITEM_CATEGORY_COURSE );
			items.saveTemplateItem( testTemplateItem1, EvalTestDataLoad.MAINT_USER_ID );
			Assert.fail("Should have thrown exception");
		} catch (SecurityException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#deleteTemplateItem(java.lang.Long, java.lang.String)}.
	 */
	public void testDeleteTemplateItem() {
		// test removing templateItem without permissions fails
		try {
			items.deleteTemplateItem(etdl.templateItem3PU.getId(), 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (SecurityException e) {
			Assert.assertNotNull(e);
		}

		try {
			items.deleteTemplateItem(etdl.templateItem3U.getId(), 
					EvalTestDataLoad.USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (SecurityException e) {
			Assert.assertNotNull(e);
		}

		// test removing templateItem from locked template fails
		try {
			items.deleteTemplateItem(etdl.templateItem1P.getId(), 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		try {
			items.deleteTemplateItem(etdl.templateItem2A.getId(), 
					EvalTestDataLoad.ADMIN_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalStateException e) {
			Assert.assertNotNull(e);
		}

		// verify that the item/template link exists before removal
		EvalTemplateItem eti1 = items.getTemplateItemById(etdl.templateItem3U.getId());
		Assert.assertNotNull( eti1 );
		Assert.assertNotNull( eti1.getItem() );
		Assert.assertNotNull( eti1.getTemplate() );
		Assert.assertNotNull( eti1.getItem().getTemplateItems() );
		Assert.assertNotNull( eti1.getTemplate().getTemplateItems() );
		Assert.assertFalse( eti1.getItem().getTemplateItems().isEmpty() );
		Assert.assertFalse( eti1.getTemplate().getTemplateItems().isEmpty() );
		Assert.assertTrue( eti1.getItem().getTemplateItems().contains( eti1 ) );
		Assert.assertTrue( eti1.getTemplate().getTemplateItems().contains( eti1 ) );
		int itemsSize = eti1.getItem().getTemplateItems().size();
		int templatesSize = eti1.getTemplate().getTemplateItems().size();

		// test removing unused templateItem OK
		items.deleteTemplateItem(etdl.templateItem3U.getId(), 
				EvalTestDataLoad.MAINT_USER_ID);
		Assert.assertNull( items.getTemplateItemById(etdl.templateItem3U.getId()) );

		// verify that the item/template link no longer exists
		Assert.assertNotNull( eti1.getItem().getTemplateItems() );
		Assert.assertNotNull( eti1.getTemplate().getTemplateItems() );
		Assert.assertFalse( eti1.getItem().getTemplateItems().isEmpty() );
		Assert.assertFalse( eti1.getTemplate().getTemplateItems().isEmpty() );
		Assert.assertEquals( itemsSize-1, eti1.getItem().getTemplateItems().size() );
		Assert.assertEquals( templatesSize-1, eti1.getTemplate().getTemplateItems().size() );
		Assert.assertTrue(! eti1.getItem().getTemplateItems().contains( eti1 ) );
		Assert.assertTrue(! eti1.getTemplate().getTemplateItems().contains( eti1 ) );

		items.deleteTemplateItem(etdl.templateItem6UU.getId(), 
				EvalTestDataLoad.USER_ID);
		Assert.assertNull( items.getTemplateItemById(etdl.templateItem6UU.getId()) );

		// test admin can remove unowned templateItem
		items.deleteTemplateItem(etdl.templateItem5U.getId(), 
				EvalTestDataLoad.ADMIN_USER_ID);
		Assert.assertNull( items.getTemplateItemById(etdl.templateItem5U.getId()) );

		// test removing invalid templateItem id fails
		try {
			items.deleteTemplateItem(EvalTestDataLoad.INVALID_LONG_ID, 
					EvalTestDataLoad.MAINT_USER_ID);
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getTemplateItemsForTemplate(java.lang.Long)}.
	 */
	public void testGetTemplateItemsForTemplate() {
		List<EvalTemplateItem> l = null;
		List<Long> ids = null;

		// test getting all items by valid templates
		l = items.getTemplateItemsForTemplate( etdl.templateAdmin.getId(), null, null, null );
		Assert.assertNotNull( l );
		Assert.assertEquals(3, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateItem2A.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem3A.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem5A.getId() ));

		// check that the return order is correct
		Assert.assertEquals( 1, ((EvalTemplateItem)l.get(0)).getDisplayOrder().intValue() );
		Assert.assertEquals( 2, ((EvalTemplateItem)l.get(1)).getDisplayOrder().intValue() );
		Assert.assertEquals( 3, ((EvalTemplateItem)l.get(2)).getDisplayOrder().intValue() );

		// test getting all items by valid templates
		l = items.getTemplateItemsForTemplate( etdl.templatePublic.getId(), null, null, null );
		Assert.assertNotNull( l );
		Assert.assertEquals(1, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateItem1P.getId() ));

		// test getting items from template with no items
		l = items.getTemplateItemsForTemplate( etdl.templateAdminNoItems.getId(), null, null, null );
		Assert.assertNotNull( l );
		Assert.assertEquals(0, l.size());

		// test getting items for specific user returns correct items
		// admin should get all items
		l = items.getTemplateItemsForTemplate( etdl.templateAdmin.getId(), null, null, null );
		Assert.assertNotNull( l );
		Assert.assertEquals(3, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateItem2A.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem3A.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem5A.getId() ));

		l = items.getTemplateItemsForTemplate( etdl.templateUnused.getId(), null, null, null );
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateItem3U.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem5U.getId() ));

		// check that the return order is correct
		Assert.assertEquals( 1, ((EvalTemplateItem)l.get(0)).getDisplayOrder().intValue() );
		Assert.assertEquals( 2, ((EvalTemplateItem)l.get(1)).getDisplayOrder().intValue() );

		// owner should see all items
		l = items.getTemplateItemsForTemplate( etdl.templateUnused.getId(), null, null, null );
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateItem3U.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem5U.getId() ));

		l = items.getTemplateItemsForTemplate( etdl.templateUser.getId(), null, null, null );
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateItem1User.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem5User.getId() ));

		// TODO - takers should see items at their level (one level) if they have access
		l = items.getTemplateItemsForTemplate( etdl.templateUser.getId(), null, null, null );
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateItem1User.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem5User.getId() ));

		// TODO - add in tests that take the hierarchy into account

		// test getting items from invalid template returns nothing
      l = items.getTemplateItemsForTemplate( EvalTestDataLoad.INVALID_LONG_ID, null, null, null );
      Assert.assertNotNull( l );
      Assert.assertEquals(0, l.size());

	}


	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getBlockChildTemplateItemsForBlockParent(Long, boolean)}.
	 */
	public void testGetBlockChildTemplateItemsForBlockParent() {
		List<EvalTemplateItem> l = null;
		List<Long> ids = null;

		// test getting child block items
		l = items.getBlockChildTemplateItemsForBlockParent( etdl.templateItem9B.getId(), false );
		Assert.assertNotNull( l );
		Assert.assertEquals(2, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateItem2B.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem3B.getId() ));

		// check that the return order is correct
		Assert.assertEquals( 1, ((EvalTemplateItem)l.get(0)).getDisplayOrder().intValue() );
		Assert.assertEquals( 2, ((EvalTemplateItem)l.get(1)).getDisplayOrder().intValue() );

		// test getting child block items and parent
		l = items.getBlockChildTemplateItemsForBlockParent( etdl.templateItem9B.getId(), true );
		Assert.assertNotNull( l );
		Assert.assertEquals(3, l.size());
		ids = EvalTestDataLoad.makeIdList(l);
		Assert.assertTrue(ids.contains( etdl.templateItem9B.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem2B.getId() ));
		Assert.assertTrue(ids.contains( etdl.templateItem3B.getId() ));

		// check that the return order is correct
		Assert.assertEquals( 1, ((EvalTemplateItem)l.get(0)).getDisplayOrder().intValue() );
		Assert.assertEquals( 1, ((EvalTemplateItem)l.get(1)).getDisplayOrder().intValue() );
		Assert.assertEquals( 2, ((EvalTemplateItem)l.get(2)).getDisplayOrder().intValue() );

		// test getting child items from invalid templateItem fails
		try {
			items.getBlockChildTemplateItemsForBlockParent( EvalTestDataLoad.INVALID_LONG_ID, false );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

		// test getting child items from non-parent templateItem fails
		try {
			items.getBlockChildTemplateItemsForBlockParent( etdl.templateItem2A.getId(), false );
			Assert.fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			Assert.assertNotNull(e);
		}

	}


	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#canModifyItem(String, Long)}.
	 */
	public void testCanModifyItem() {
		// test can control owned items
		Assert.assertTrue( items.canModifyItem( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.item7.getId() ) );
		Assert.assertTrue( items.canModifyItem( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.item4.getId() ) );

		// test admin user can override perms
		Assert.assertTrue( items.canModifyItem( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.item4.getId() ) );

		// test cannot control unowned items
		Assert.assertFalse( items.canModifyItem( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.item7.getId() ) );
		Assert.assertFalse( items.canModifyItem( EvalTestDataLoad.USER_ID, 
				etdl.item4.getId() ) );

		// test cannot control locked items
		Assert.assertFalse( items.canModifyItem( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.item1.getId() ) );
		Assert.assertFalse( items.canModifyItem( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.item2.getId() ) );

		// test invalid item id causes failure
		try {
			items.canModifyItem( EvalTestDataLoad.MAINT_USER_ID, 
					EvalTestDataLoad.INVALID_LONG_ID );
			Assert.fail("Should have thrown exception");
		} catch (RuntimeException e) {
			Assert.assertNotNull(e);
		}
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#canRemoveItem(String, Long)}.
	 */
	public void testCanRemoveItem() {
		// test can remove owned items
		Assert.assertTrue( items.canRemoveItem( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.item7.getId() ) );
		Assert.assertTrue( items.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.item4.getId() ) );

		// test admin user can override perms
		Assert.assertTrue( items.canRemoveItem( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.item4.getId() ) );

		// test cannot remove unowned items
		Assert.assertFalse( items.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.item7.getId() ) );
		Assert.assertFalse( items.canRemoveItem( EvalTestDataLoad.USER_ID, 
				etdl.item4.getId() ) );

		// test cannot remove unlocked items that are in use in templates
		Assert.assertFalse( items.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.item6.getId() ) );
		Assert.assertFalse( items.canRemoveItem( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.item9.getId() ) );

		// test cannot remove locked items
		Assert.assertFalse( items.canRemoveItem( EvalTestDataLoad.ADMIN_USER_ID, 
				etdl.item1.getId() ) );
		Assert.assertFalse( items.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
				etdl.item2.getId() ) );

		// test invalid item id causes failure
		try {
			items.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
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
