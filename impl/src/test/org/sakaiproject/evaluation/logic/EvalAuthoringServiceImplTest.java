/**
 * $Id$
 * $URL$
 * EvalAuthoringServiceImplTest.java - evaluation - Jan 30, 2008 12:05:10 PM - azeckoski
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.externals.EvalSecurityChecksImpl;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.genericdao.api.search.Search;


/**
 * Testing the authoring service
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalAuthoringServiceImplTest extends BaseTestEvalLogic {

   protected EvalAuthoringServiceImpl authoringService;

   // run this before each test starts
   protected void onSetUpBeforeTransaction() throws Exception {
      super.onSetUpBeforeTransaction();

      // load up any other needed spring beans
      EvalSettings settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
      if (settings == null) {
         throw new NullPointerException("EvalSettings could not be retrieved from spring context");
      }

      EvalSecurityChecksImpl securityChecks = 
         (EvalSecurityChecksImpl) applicationContext.getBean("org.sakaiproject.evaluation.logic.externals.EvalSecurityChecks");
      if (securityChecks == null) {
         throw new NullPointerException("EvalSecurityChecksImpl could not be retrieved from spring context");
      }

      // setup the mock objects if needed

      // create and setup the object to be tested
      authoringService = new EvalAuthoringServiceImpl();
      authoringService.setDao(evaluationDao);
      authoringService.setCommonLogic(commonLogic);
      authoringService.setSettings(settings);
      authoringService.setSecurityChecks(securityChecks);

   }


   /**
    * ADD unit tests below here, use testMethod as the name of the unit test,
    * Note that if a method is overloaded you should include the arguments in the
    * test name like so: testMethodClassInt (for method(Class, int);
    */

   public void testPreloadedItemGroupsData() {
      // check the full count of preloaded items
      assertEquals(17, evaluationDao.countAll(EvalItemGroup.class) );

      assertEquals(10, evaluationDao.countAll(EvalTemplate.class) );
      List<EvalTemplate> templates1 = evaluationDao.findAll(EvalTemplate.class);
      assertEquals(10, templates1.size());
   }

   public void testPreloadedItemData() {
      // this test is just making sure that we are actually linking the items
      // to the templates the way we think we are
      List<Long> ids = null;

      assertEquals(46, evaluationDao.countAll(EvalItem.class) );

      // check the full count of preloaded items
      assertEquals(20, evaluationDao.countAll(EvalTemplateItem.class) );

      EvalTemplate template = (EvalTemplate) 
      evaluationDao.findById(EvalTemplate.class, etdl.templateAdmin.getId());

      // No longer supporting this type of linkage between templates and items
//    Set items = template.getItems();
//    assertNotNull( items );
//    assertEquals(3, authoringService.size());

      Set<EvalTemplateItem> tItems = template.getTemplateItems();
      assertNotNull( tItems );
      assertEquals(3, tItems.size());
      ids = EvalTestDataLoad.makeIdList(tItems);
      assertTrue(ids.contains( etdl.templateItem2A.getId() ));
      assertTrue(ids.contains( etdl.templateItem3A.getId() ));
      assertTrue(ids.contains( etdl.templateItem5A.getId() ));
      // get the items from the templateItems
      List<Long> l = new ArrayList<Long>();
      for (Iterator<EvalTemplateItem> iter = tItems.iterator(); iter.hasNext();) {
         EvalTemplateItem eti = iter.next();
         assertTrue( eti.getItem() instanceof EvalItem );
         assertEquals(eti.getTemplate().getId(), template.getId());
         l.add(eti.getItem().getId());
      }
      assertTrue(l.contains( etdl.item2.getId() ));
      assertTrue(l.contains( etdl.item3.getId() ));
      assertTrue(l.contains( etdl.item5.getId() ));

      // test getting another set of items
      EvalItem item = (EvalItem) evaluationDao.findById(EvalItem.class, etdl.item1.getId());
      Set<EvalTemplateItem> itItems = item.getTemplateItems();
      assertNotNull( itItems );
      assertEquals(3, itItems.size());
      ids = EvalTestDataLoad.makeIdList(itItems);
      assertTrue(ids.contains( etdl.templateItem1P.getId() ));
      assertTrue(ids.contains( etdl.templateItem1U.getId() ));
      assertTrue(ids.contains( etdl.templateItem1User.getId() ));

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#getScaleById(java.lang.Long)}.
    */
   public void testGetScaleById() {
      EvalScale scale = null;

      scale = authoringService.getScaleById( etdl.scale1.getId() );
      assertNotNull(scale);
      assertEquals(etdl.scale1.getId(), scale.getId());

      scale = authoringService.getScaleById( etdl.scale2.getId() );
      assertNotNull(scale);
      assertEquals(etdl.scale2.getId(), scale.getId());

      // test get eval by invalid id
      scale = authoringService.getScaleById( EvalTestDataLoad.INVALID_LONG_ID );
      assertNull(scale);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#getScaleByEid(java.lang.String)}.
    */
   public void testGetScaleByEid() {
      EvalScale scale = null;

      //test getting scale having eid set
      scale = authoringService.getScaleByEid( etdl.scaleEid.getEid() );
      assertNotNull(scale);
      assertEquals(etdl.scaleEid.getEid(), scale.getEid());

      //test getting scale not having eid set returns null
      scale = authoringService.getScaleByEid( etdl.scale2.getEid() );
      assertNull(scale);

      // test getting scale by invalid eid returns null
      scale = authoringService.getScaleByEid( EvalTestDataLoad.INVALID_STRING_EID );
      assertNull(scale);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#saveScale(org.sakaiproject.evaluation.model.EvalScale, java.lang.String)}.
    */
   public void testSaveScale() {
      String[] options1 = {"Bad", "Average", "Good"};
      String test_title = "test scale title";

      // test saving a new valid scale
      authoringService.saveScale( new EvalScale( EvalTestDataLoad.MAINT_USER_ID, 
            test_title, EvalConstants.SCALE_MODE_SCALE, 
            EvalConstants.SHARING_PRIVATE, Boolean.FALSE, "description", 
            EvalConstants.SCALE_IDEAL_LOW, options1,
            EvalTestDataLoad.UNLOCKED), EvalTestDataLoad.MAINT_USER_ID);

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
      authoringService.saveScale(testScale2, EvalTestDataLoad.MAINT_USER_ID);

      assertEquals(4, testScale2.getOptions().length);
      testScale2.setOptions(options1);
      authoringService.saveScale(testScale2, EvalTestDataLoad.MAINT_USER_ID);
      assertEquals(3, testScale2.getOptions().length);

      // test admin can edit any scale
      testScale2.setIdeal(EvalConstants.SCALE_IDEAL_MID);
      authoringService.saveScale(testScale2, EvalTestDataLoad.ADMIN_USER_ID);

      // test that editing unowned scale causes permission failure
      try {
         testScale3.setIdeal(EvalConstants.SCALE_IDEAL_MID);
         authoringService.saveScale(testScale4, EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
      }

      // TODO - CANNOT RUN THIS TEST BECAUSE OF HIBERNATE ISSUE
//    // test that LOCKED cannot be changed to FALSE on existing scales
//    try {
//    testScale1.setLocked(Boolean.FALSE);
//    authoringService.saveScale(testScale1, EvalTestDataLoad.ADMIN_USER_ID);
//    fail("Should have thrown exception");
//    } catch (IllegalArgumentException e) {
//    assertNotNull(e);
//    fail(e.getMessage());
//    }

      // test editing LOCKED scale fails
      try {
         testScale1.setSharing(EvalConstants.SHARING_PRIVATE);
         authoringService.saveScale(testScale1, EvalTestDataLoad.ADMIN_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
      }

      // test that setting sharing to PUBLIC as non-admin fails
      try {
         testScale2.setSharing(EvalConstants.SHARING_PUBLIC);
         authoringService.saveScale(testScale2, EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test admin can set scales to public sharing
      testScale2.setSharing(EvalConstants.SHARING_PUBLIC);
      authoringService.saveScale(testScale2, EvalTestDataLoad.ADMIN_USER_ID);

      // test fails to save scale with null options
      try {
         authoringService.saveScale( new EvalScale( EvalTestDataLoad.MAINT_USER_ID, 
               "options are null", EvalConstants.SCALE_MODE_SCALE, 
               EvalConstants.SHARING_PRIVATE, Boolean.FALSE, "description", 
               EvalConstants.SCALE_IDEAL_LOW, null,
               EvalTestDataLoad.UNLOCKED), EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test CAN save scale with duplicate title
      authoringService.saveScale( new EvalScale( EvalTestDataLoad.MAINT_USER_ID, 
            test_title, EvalConstants.SCALE_MODE_SCALE, 
            EvalConstants.SHARING_PRIVATE, Boolean.FALSE, "description", 
            EvalConstants.SCALE_IDEAL_LOW, options1,
            EvalTestDataLoad.UNLOCKED), EvalTestDataLoad.MAINT_USER_ID);

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#deleteScale(java.lang.Long, java.lang.String)}.
    */
   public void testDeleteScale() {
      // test removing unowned scale fails
      try {
         authoringService.deleteScale(etdl.scale4.getId(), EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // test removing owned scale works
      authoringService.deleteScale(etdl.scale3.getId(), EvalTestDataLoad.MAINT_USER_ID);

      // test removing expert scale allowed
      authoringService.deleteScale(etdl.scale4.getId(), EvalTestDataLoad.ADMIN_USER_ID);

      // test removing locked scale fails
      try {
         authoringService.deleteScale(etdl.scale1.getId(), EvalTestDataLoad.ADMIN_USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // test invalid scale id fails
      try {
         authoringService.deleteScale(EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#getScalesForUser(java.lang.String, java.lang.String)}.
    */
   public void testGetScalesForUser() {
      List<EvalScale> l = null;
      List<Long> ids = null;
      // NOTE: 15 preloaded public scales to take into account currently
      int preloadedCount = 15;

      // get all visible scales (admin should see all)
      l = authoringService.getScalesForUser(EvalTestDataLoad.ADMIN_USER_ID, null);
      assertNotNull(l);
      assertEquals(5 + preloadedCount, l.size()); // include 15 preloaded
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.scale1.getId() ));
      assertTrue(ids.contains( etdl.scale2.getId() ));
      assertTrue(ids.contains( etdl.scale3.getId() ));

      assertTrue(ids.contains( etdl.scaleEid.getId() ));

      // get all visible scales (include maint owned and public)
      l = authoringService.getScalesForUser(EvalTestDataLoad.MAINT_USER_ID, null);
      assertNotNull(l);
      assertEquals(4 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.scale1.getId() ));
      assertTrue(ids.contains( etdl.scale2.getId() ));
      assertTrue(ids.contains( etdl.scale3.getId() ));
      assertTrue(! ids.contains( etdl.scale4.getId() ));

      assertTrue(ids.contains( etdl.scaleEid.getId() ));

      // get all visible scales (should only see public)
      l = authoringService.getScalesForUser(EvalTestDataLoad.USER_ID, null);
      assertNotNull(l);
      assertEquals(2 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.scale1.getId() ));
      assertTrue(! ids.contains( etdl.scale2.getId() ));
      assertTrue(! ids.contains( etdl.scale3.getId() ));

      assertTrue(ids.contains( etdl.scaleEid.getId() ));

      // attempt to get SHARING_OWNER scales (returns same as null)
      l = authoringService.getScalesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_OWNER);
      assertNotNull(l);
      assertEquals(5 + preloadedCount, l.size());

      // get all private scales (admin should see all private)
      l = authoringService.getScalesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_PRIVATE);
      assertNotNull(l);
      assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.scale2.getId() ));
      assertTrue(ids.contains( etdl.scale3.getId() ));
      assertTrue(ids.contains( etdl.scale4.getId() ));

      // check that the return order is correct
      assertEquals( etdl.scale2.getId(), ids.get(0) );
      assertEquals( etdl.scale3.getId(), ids.get(1) );
      assertEquals( etdl.scale4.getId(), ids.get(2) );

      // get all private scales (maint should see own only)
      l = authoringService.getScalesForUser(EvalTestDataLoad.MAINT_USER_ID, EvalConstants.SHARING_PRIVATE);
      assertNotNull(l);
      assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.scale2.getId() ));
      assertTrue(ids.contains( etdl.scale3.getId() ));

      // get all private scales (normal user should see none)
      l = authoringService.getScalesForUser(EvalTestDataLoad.USER_ID, EvalConstants.SHARING_PRIVATE);
      assertNotNull(l);
      assertEquals(0, l.size());

      // get all public scales (normal user should see all)
      l = authoringService.getScalesForUser(EvalTestDataLoad.USER_ID, EvalConstants.SHARING_PUBLIC);
      assertNotNull(l);
      assertEquals(2 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.scale1.getId() ));
      assertTrue(! ids.contains( etdl.scale2.getId() ));
      assertTrue(! ids.contains( etdl.scale3.getId() ));

      assertTrue(ids.contains( etdl.scaleEid.getId() ));

      // test getting invalid constant causes failure
      try {
         l = authoringService.getScalesForUser(EvalTestDataLoad.USER_ID, EvalTestDataLoad.INVALID_CONSTANT_STRING);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#canModifyScale(String, Long)}
    */
   public void testCanModifyScale() {
      // test can modify owned scale
      assertTrue( authoringService.canModifyScale(
            EvalTestDataLoad.MAINT_USER_ID,  etdl.scale3.getId()) );
      assertTrue( authoringService.canModifyScale(
            EvalTestDataLoad.ADMIN_USER_ID,  etdl.scale4.getId()) );

      // test can modify used scale
      assertTrue( authoringService.canModifyScale(
            EvalTestDataLoad.MAINT_USER_ID,  etdl.scale2.getId()) );

      // test admin user can override perms
      assertTrue( authoringService.canModifyScale(
            EvalTestDataLoad.ADMIN_USER_ID,  etdl.scale3.getId()) );

      // test cannot control unowned scale
      assertFalse( authoringService.canModifyScale(
            EvalTestDataLoad.MAINT_USER_ID,  etdl.scale4.getId()) );
      assertFalse( authoringService.canModifyScale(
            EvalTestDataLoad.USER_ID, etdl.scale3.getId()) );

      // test cannot modify locked scale
      assertFalse( authoringService.canModifyScale(
            EvalTestDataLoad.ADMIN_USER_ID,  etdl.scale1.getId()) );

      // test invalid scale id causes failure
      try {
         authoringService.canModifyScale(EvalTestDataLoad.ADMIN_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#canRemoveScale(String, Long)}
    */
   public void testCanRemoveScale() {
      // test can remove owned scale
      assertTrue( authoringService.canRemoveScale(
            EvalTestDataLoad.ADMIN_USER_ID,  etdl.scale4.getId()) );
      assertTrue( authoringService.canRemoveScale(
            EvalTestDataLoad.MAINT_USER_ID,  etdl.scale3.getId()) );

      // test cannot remove unowned scale
      assertFalse( authoringService.canRemoveScale(
            EvalTestDataLoad.MAINT_USER_ID,  etdl.scale4.getId()) );
      assertFalse( authoringService.canRemoveScale(
            EvalTestDataLoad.USER_ID, etdl.scale3.getId()) );

      // can remove items that are in use now
//      // test cannot remove unlocked used scale
//      assertFalse( authoringService.canRemoveScale(
//            EvalTestDataLoad.MAINT_USER_ID,  etdl.scale2.getId()) );
//
//      // test admin cannot remove unlocked used scale
//      assertFalse( authoringService.canRemoveScale(
//            EvalTestDataLoad.ADMIN_USER_ID,  etdl.scale2.getId()) );

      // test cannot remove locked scale
      assertFalse( authoringService.canRemoveScale(
            EvalTestDataLoad.ADMIN_USER_ID,  etdl.scale1.getId()) );

      // test invalid scale id causes failure
      try {
         authoringService.canRemoveScale(EvalTestDataLoad.ADMIN_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemById(java.lang.Long)}.
    */
   public void testGetItemById() {
      EvalItem item = null;

      // test getting valid items by id
      item = authoringService.getItemById( etdl.item1.getId() );
      assertNotNull(item);
      assertEquals(etdl.item1.getId(), item.getId());

      item = authoringService.getItemById( etdl.item5.getId() );
      assertNotNull(item);
      assertEquals(etdl.item5.getId(), item.getId());

      // test get eval by invalid id returns null
      item = authoringService.getItemById( EvalTestDataLoad.INVALID_LONG_ID );
      assertNull(item);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemByEid(java.lang.String)}.
    */
   public void testGetItemByEid() {
      EvalItem item = null;

      // test getting valid items having eid set
      item = authoringService.getItemByEid( etdl.item1Eid.getEid() );
      assertNotNull(item);
      assertEquals(etdl.item1Eid.getEid(), item.getEid());

      item = authoringService.getItemByEid( etdl.item2Eid.getEid() );
      assertNotNull(item);
      assertEquals(etdl.item2Eid.getEid(), item.getEid());

      item = authoringService.getItemByEid( etdl.item3Eid.getEid() );
      assertNotNull(item);
      assertEquals(etdl.item3Eid.getEid(), item.getEid());

      //test getting valid item not having eid set returns null
      item = authoringService.getItemByEid( etdl.item5.getEid() );
      assertNull(item);

      // test getting item by invalid id returns null
      item = authoringService.getItemByEid( EvalTestDataLoad.INVALID_STRING_EID );
      assertNull(item);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#saveItem(org.sakaiproject.evaluation.model.EvalItem, java.lang.String)}.
    */
   public void testSaveItem() {
      String test_text = "test item text";
      String test_desc = "test item description";

      // test saving a valid item
      authoringService.saveItem( new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
            test_text, test_desc, EvalConstants.SHARING_PRIVATE, 
            EvalConstants.ITEM_TYPE_SCALED, EvalTestDataLoad.NOT_EXPERT, 
            "expert desc", etdl.scale1, null, Boolean.FALSE,
            false, false, null, 
            EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.UNLOCKED), 
            EvalTestDataLoad.MAINT_USER_ID);

      // test saving valid item locked
      authoringService.saveItem( new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
            test_text, test_desc, EvalConstants.SHARING_PRIVATE, 
            EvalConstants.ITEM_TYPE_TEXT, EvalTestDataLoad.NOT_EXPERT, 
            "expert desc", null, null, null,
            false, false, new Integer(2), 
            null, EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.LOCKED), 
            EvalTestDataLoad.MAINT_USER_ID);

      // test saving valid item with no date, NA, and lock specified ok
      EvalItem eiTest1 = new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
            test_text, test_desc, EvalConstants.SHARING_PRIVATE, 
            EvalConstants.ITEM_TYPE_TEXT, EvalTestDataLoad.NOT_EXPERT, 
            "expert desc", null, null, null,
            false, false, new Integer(2), 
            null, EvalConstants.ITEM_CATEGORY_COURSE, null);
      authoringService.saveItem( eiTest1, 
            EvalTestDataLoad.MAINT_USER_ID);
      // make sure the values are filled in for us
      assertNotNull( eiTest1.getLastModified() );
      assertNotNull( eiTest1.getLocked() );
      assertNotNull( eiTest1.getUsesNA() );

      // test saving scaled item with no scale fails
      try {
         authoringService.saveItem( new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
               test_text, EvalConstants.SHARING_PRIVATE, 
               EvalConstants.ITEM_TYPE_SCALED, EvalTestDataLoad.NOT_EXPERT), 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test saving scaled item with scale set AND text size fails
      try {
         authoringService.saveItem( new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
               test_text, test_desc, EvalConstants.SHARING_PRIVATE, 
               EvalConstants.ITEM_TYPE_SCALED, EvalTestDataLoad.NOT_EXPERT, 
               "expert desc", etdl.scale2, null, Boolean.FALSE,
               false, false, new Integer(3), 
               EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.UNLOCKED),
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test saving text item with no text size fails
      try {
         authoringService.saveItem( new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
               test_text, EvalConstants.SHARING_PRIVATE, 
               EvalConstants.ITEM_TYPE_TEXT, EvalTestDataLoad.NOT_EXPERT), 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test saving text item with scale set fails
      try {
         authoringService.saveItem( new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
               test_text, test_desc, EvalConstants.SHARING_PRIVATE, 
               EvalConstants.ITEM_TYPE_TEXT, EvalTestDataLoad.NOT_EXPERT, 
               "expert desc", etdl.scale2, null, Boolean.FALSE,
               false, false, null, 
               EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.UNLOCKED),
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test saving header type item with scale or text size set fails
      try {
         authoringService.saveItem( new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
               test_text, test_desc, EvalConstants.SHARING_PRIVATE, 
               EvalConstants.ITEM_TYPE_HEADER, EvalTestDataLoad.NOT_EXPERT, 
               "expert desc", etdl.scale2, null, Boolean.FALSE,
               false, false, new Integer(3), 
               EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.UNLOCKED),
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
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
      authoringService.saveItem( testItem1, 
            EvalTestDataLoad.MAINT_USER_ID);

      // TODO - CANNOT RUN THIS TEST FOR NOW BECAUSE OF HIBERNATE
//    // test that LOCKED cannot be changed to FALSE on existing item
//    try {
//    testItem3.setLocked(Boolean.FALSE);
//    authoringService.saveItem( testItem3, 
//    EvalTestDataLoad.ADMIN_USER_ID);
//    fail("Should have thrown exception");
//    } catch (RuntimeException e) {
//    assertNotNull(e);
//    fail(e.getMessage()); // see why failing
//    }

      // test editing LOCKED item fails
      try {
         testItem4.setExpert(Boolean.FALSE);
         authoringService.saveItem( testItem4, 
               EvalTestDataLoad.ADMIN_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
      }

      // test admin can edit any item
      testItem2.setDescription("something admin new");
      authoringService.saveItem( testItem2, 
            EvalTestDataLoad.ADMIN_USER_ID);

      // test that editing unowned item causes permission failure
      try {
         testItem3.setDescription("something maint new");
         authoringService.saveItem( testItem3, 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
      }

      // test that setting sharing to PUBLIC as non-admin fails
      try {
         testItem1.setSharing(EvalConstants.SHARING_PUBLIC);
         authoringService.saveItem( testItem1, 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test admin can set sharing to public
      testItem1.setSharing(EvalConstants.SHARING_PUBLIC);
      authoringService.saveItem( testItem1, 
            EvalTestDataLoad.ADMIN_USER_ID);

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#deleteItem(java.lang.Long, java.lang.String)}.
    */
   public void testDeleteItem() {
      // test removing item without permissions fails
      try {
         authoringService.deleteItem(etdl.item7.getId(), 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
      }

      try {
         authoringService.deleteItem(etdl.item4.getId(), 
               EvalTestDataLoad.USER_ID);
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
      }

      // test removing locked item fails
      try {
         authoringService.deleteItem(etdl.item2.getId(), 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
      }

      try {
         authoringService.deleteItem(etdl.item1.getId(), 
               EvalTestDataLoad.ADMIN_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
      }

      // ADMIN CAN REMOVE EXPERT ITEMS NOW -AZ
//    // test cannot remove expert item
//    try {
//    authoringService.deleteItem(etdl.item6.getId(), 
//    EvalTestDataLoad.ADMIN_USER_ID);
//    fail("Should have thrown exception");
//    } catch (IllegalStateException e) {
//    assertNotNull(e);
//    }

      // test removing unused item OK
      authoringService.deleteItem(etdl.item4.getId(), 
            EvalTestDataLoad.MAINT_USER_ID);
      assertNull( authoringService.getItemById(etdl.item4.getId()) );

      authoringService.deleteItem(etdl.item7.getId(), 
            EvalTestDataLoad.ADMIN_USER_ID);
      assertNull( authoringService.getItemById(etdl.item7.getId()) );

      // this test makes no sense -AZ
//      // test removing an item that is in use is ok
//      authoringService.deleteItem(etdl.item6.getId(), 
//            EvalTestDataLoad.ADMIN_USER_ID);

      // test removing invalid item id fails
      try {
         authoringService.deleteItem(EvalTestDataLoad.INVALID_LONG_ID, 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

   }

   /**
    * This tests the ability to remove the item and scale at the same time
    */
   public void testDeleteItemAndScale() {
      // create a test MC item
      String[] options1 = {"one", "two", "three"};
      EvalScale scale1 = new EvalScale(EvalTestDataLoad.ADMIN_USER_ID, "Scale MC", EvalConstants.SCALE_MODE_ADHOC, 
            EvalConstants.SHARING_PRIVATE, false, "description", 
            null, options1, false);
      evaluationDao.save(scale1);

      EvalItem item1 = new EvalItem(EvalTestDataLoad.ADMIN_USER_ID, "mutli choice", EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_MULTIPLECHOICE, 
            false);
      item1.setScale(scale1);
      item1.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL);
      authoringService.saveItem(item1, EvalTestDataLoad.ADMIN_USER_ID);

      // check that the item and scale are saved
      assertNotNull( evaluationDao.findById(EvalScale.class, scale1.getId()) );
      assertNotNull( evaluationDao.findById(EvalItem.class, item1.getId()) );

      authoringService.deleteItem(item1.getId(), EvalTestDataLoad.ADMIN_USER_ID);

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
      l = authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, null, null, true);
      assertNotNull( l );
      assertEquals(13 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item1.getId() ));
      assertTrue(ids.contains( etdl.item2.getId() ));
      assertTrue(ids.contains( etdl.item3.getId() ));
      assertTrue(ids.contains( etdl.item4.getId() ));
      assertTrue(ids.contains( etdl.item5.getId() ));
      assertTrue(ids.contains( etdl.item6.getId() ));
      assertTrue(ids.contains( etdl.item7.getId() ));
      assertTrue(ids.contains( etdl.item8.getId() ));
      assertTrue(ids.contains( etdl.item10.getId() ));
      assertTrue(ids.contains( etdl.item11.getId() ));

      assertTrue(ids.contains( etdl.item1Eid.getId() ));
      assertTrue(ids.contains( etdl.item2Eid.getId() ));
      assertTrue(ids.contains( etdl.item3Eid.getId() ));

      // same as getting all items
      l = authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, 
            EvalConstants.SHARING_OWNER, null, true);
      assertNotNull( l );
      assertEquals(13 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item1.getId() ));
      assertTrue(ids.contains( etdl.item2.getId() ));
      assertTrue(ids.contains( etdl.item3.getId() ));
      assertTrue(ids.contains( etdl.item4.getId() ));
      assertTrue(ids.contains( etdl.item5.getId() ));
      assertTrue(ids.contains( etdl.item6.getId() ));
      assertTrue(ids.contains( etdl.item7.getId() ));
      assertTrue(ids.contains( etdl.item8.getId() ));
      assertTrue(ids.contains( etdl.item10.getId() ));
      assertTrue(ids.contains( etdl.item11.getId() ));

      assertTrue(ids.contains( etdl.item1Eid.getId() ));
      assertTrue(ids.contains( etdl.item2Eid.getId() ));
      assertTrue(ids.contains( etdl.item3Eid.getId() ));

      // test getting all items for the maint user
      l = authoringService.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, null, null, true);
      assertNotNull( l );
      assertEquals(12 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item1.getId() ));
      assertTrue(ids.contains( etdl.item2.getId() ));
      assertTrue(ids.contains( etdl.item3.getId() ));
      assertTrue(ids.contains( etdl.item4.getId() ));
      assertTrue(ids.contains( etdl.item5.getId() ));
      assertTrue(ids.contains( etdl.item6.getId() ));
      assertTrue(ids.contains( etdl.item8.getId() ));
      assertTrue(ids.contains( etdl.item10.getId() ));
      assertTrue(ids.contains( etdl.item11.getId() ));

      assertTrue(ids.contains( etdl.item1Eid.getId() ));
      assertTrue(ids.contains( etdl.item2Eid.getId() ));
      assertTrue(ids.contains( etdl.item3Eid.getId() ));

      // test getting all items for the maint user without expert items
      l = authoringService.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, null, null, false);
      assertNotNull( l );
      assertEquals(9, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item3.getId() ));
      assertTrue(ids.contains( etdl.item4.getId() ));
      assertTrue(ids.contains( etdl.item5.getId() ));
      assertTrue(ids.contains( etdl.item8.getId() ));
      assertTrue(ids.contains( etdl.item10.getId() ));
      assertTrue(ids.contains( etdl.item11.getId() ));

      assertTrue(ids.contains( etdl.item1Eid.getId() ));
      assertTrue(ids.contains( etdl.item2Eid.getId() ));
      assertTrue(ids.contains( etdl.item3Eid.getId() ));

      // test getting all items for the normal user
      l = authoringService.getItemsForUser(EvalTestDataLoad.USER_ID, null, null, true);
      assertNotNull( l );
      assertEquals(5 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item1.getId() ));
      assertTrue(ids.contains( etdl.item2.getId() ));

      assertTrue(ids.contains( etdl.item1Eid.getId() ));
      assertTrue(ids.contains( etdl.item2Eid.getId() ));
      assertTrue(ids.contains( etdl.item3Eid.getId() ));

      // test getting private items for the admin user (all private items)
      l = authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID,
            EvalConstants.SHARING_PRIVATE, null, true);
      assertNotNull( l );
      assertEquals(8, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item3.getId() ));
      assertTrue(ids.contains( etdl.item4.getId() ));
      assertTrue(ids.contains( etdl.item5.getId() ));
      assertTrue(ids.contains( etdl.item6.getId() ));
      assertTrue(ids.contains( etdl.item7.getId() ));
      assertTrue(ids.contains( etdl.item8.getId() ));
      assertTrue(ids.contains( etdl.item10.getId() ));
      assertTrue(ids.contains( etdl.item11.getId() ));

      // test getting private all private items with filter
      l = authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID,
            EvalConstants.SHARING_PRIVATE, "do you think", true);
      assertNotNull( l );
      assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item3.getId() ));
      assertTrue(ids.contains( etdl.item4.getId() ));
      assertTrue(ids.contains( etdl.item11.getId() ));

      // test getting private all private items, expert excluded
      l = authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID,
            EvalConstants.SHARING_PRIVATE, null, false);
      assertNotNull( l );
      assertEquals(7, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item3.getId() ));
      assertTrue(ids.contains( etdl.item4.getId() ));
      assertTrue(ids.contains( etdl.item5.getId() ));
      assertTrue(ids.contains( etdl.item7.getId() ));
      assertTrue(ids.contains( etdl.item8.getId() ));
      assertTrue(ids.contains( etdl.item10.getId() ));
      assertTrue(ids.contains( etdl.item11.getId() ));

      // test getting private items for the maint user
      l = authoringService.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, 
            EvalConstants.SHARING_PRIVATE, null, true);
      assertNotNull( l );
      assertEquals(7, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item3.getId() ));
      assertTrue(ids.contains( etdl.item4.getId() ));
      assertTrue(ids.contains( etdl.item5.getId() ));
      assertTrue(ids.contains( etdl.item6.getId() ));
      assertTrue(ids.contains( etdl.item8.getId() ));
      assertTrue(ids.contains( etdl.item10.getId() ));
      assertTrue(ids.contains( etdl.item11.getId() ));

      // test getting private items for the user
      l = authoringService.getItemsForUser(EvalTestDataLoad.USER_ID, 
            EvalConstants.SHARING_PRIVATE, null, true);
      assertNotNull( l );
      assertEquals(0, l.size());

      // test getting public items for the admin user
      l = authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, 
            EvalConstants.SHARING_PUBLIC, null, true);
      assertNotNull( l );
      assertEquals(5 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item1.getId() ));
      assertTrue(ids.contains( etdl.item2.getId() ));

      assertTrue(ids.contains( etdl.item1Eid.getId() ));
      assertTrue(ids.contains( etdl.item2Eid.getId() ));
      assertTrue(ids.contains( etdl.item3Eid.getId() ));

      // test getting public items for the maint user
      l = authoringService.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, 
            EvalConstants.SHARING_PUBLIC, null, true);
      assertNotNull( l );
      assertEquals(5 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item1.getId() ));
      assertTrue(ids.contains( etdl.item2.getId() ));

      assertTrue(ids.contains( etdl.item1Eid.getId() ));
      assertTrue(ids.contains( etdl.item2Eid.getId() ));
      assertTrue(ids.contains( etdl.item3Eid.getId() ));


      // test getting public items for the user
      l = authoringService.getItemsForUser(EvalTestDataLoad.USER_ID, 
            EvalConstants.SHARING_PUBLIC, null, true);
      assertNotNull( l );
      assertEquals(5 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item1.getId() ));
      assertTrue(ids.contains( etdl.item2.getId() ));

      assertTrue(ids.contains( etdl.item1Eid.getId() ));
      assertTrue(ids.contains( etdl.item2Eid.getId() ));
      assertTrue(ids.contains( etdl.item3Eid.getId() ));

      // test getting items for invalid user returns public only
      l = authoringService.getItemsForUser( EvalTestDataLoad.INVALID_USER_ID, null, null, true);
      assertNotNull( l );
      assertEquals(5 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item1.getId() ));
      assertTrue(ids.contains( etdl.item2.getId() ));

      assertTrue(ids.contains( etdl.item1Eid.getId() ));
      assertTrue(ids.contains( etdl.item2Eid.getId() ));
      assertTrue(ids.contains( etdl.item3Eid.getId() ));

      // test invalid sharing constant causes failure
      try {
         authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, 
               EvalTestDataLoad.INVALID_CONSTANT_STRING, null, true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemsForTemplate(java.lang.Long)}.
    */
   public void testGetItemsForTemplate() {
      List<EvalItem> l = null;
      List<Long> ids = null;

      // test getting all items by valid templates
      l = authoringService.getItemsForTemplate( etdl.templateAdmin.getId(), null );
      assertNotNull( l );
      assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item2.getId() ));
      assertTrue(ids.contains( etdl.item3.getId() ));
      assertTrue(ids.contains( etdl.item5.getId() ));

      // test getting all items by valid templates
      l = authoringService.getItemsForTemplate( etdl.templatePublic.getId(), null );
      assertNotNull( l );
      assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item1.getId() ));

      // test getting items from template with no items
      l = authoringService.getItemsForTemplate( etdl.templateAdminNoItems.getId(), null );
      assertNotNull( l );
      assertEquals(0, l.size());

      // test getting items for specific user returns correct items
      // admin should get all items
      l = authoringService.getItemsForTemplate( etdl.templateAdmin.getId(), 
            EvalTestDataLoad.ADMIN_USER_ID );
      assertNotNull( l );
      assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item2.getId() ));
      assertTrue(ids.contains( etdl.item3.getId() ));
      assertTrue(ids.contains( etdl.item5.getId() ));

      l = authoringService.getItemsForTemplate( etdl.templateUnused.getId(), 
            EvalTestDataLoad.ADMIN_USER_ID );
      assertNotNull( l );
      assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item1.getId() ));
      assertTrue(ids.contains( etdl.item3.getId() ));
      assertTrue(ids.contains( etdl.item5.getId() ));

      // owner should see all items
      l = authoringService.getItemsForTemplate( etdl.templateUnused.getId(), 
            EvalTestDataLoad.MAINT_USER_ID );
      assertNotNull( l );
      assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item1.getId() ));
      assertTrue(ids.contains( etdl.item3.getId() ));
      assertTrue(ids.contains( etdl.item5.getId() ));

      l = authoringService.getItemsForTemplate( etdl.templateUser.getId(), 
            EvalTestDataLoad.USER_ID );
      assertNotNull( l );
      assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item1.getId() ));
      assertTrue(ids.contains( etdl.item5.getId() ));

      // TODO - takers should see items at their level (one level) if they have access
      l = authoringService.getItemsForTemplate( etdl.templateUser.getId(), 
            EvalTestDataLoad.STUDENT_USER_ID );
      assertNotNull( l );
      assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.item1.getId() ));
      assertTrue(ids.contains( etdl.item5.getId() ));

      // TODO - add in tests that take the hierarchy into account

      // test getting items from invalid template fails
      l = authoringService.getItemsForTemplate( EvalTestDataLoad.INVALID_LONG_ID, null );
      assertNotNull( l );
      assertEquals(0, l.size());

      // TODO - MAKE this work later on
//    // test getting items for invalid user returns nothing
//    l = authoringService.getItemsForTemplate( etdl.templatePublic.getId(), 
//    EvalTestDataLoad.INVALID_USER_ID );
//    assertNotNull( l );
//    assertEquals(0, l.size());

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getTemplateItemById(java.lang.Long)}.
    */
   public void testGetTemplateItemById() {
      EvalTemplateItem templateItem = null;

      // test getting valid templateItems by id
      templateItem = authoringService.getTemplateItemById( etdl.templateItem1P.getId() );
      assertNotNull(templateItem);
      assertEquals(etdl.templateItem1P.getId(), templateItem.getId());

      templateItem = authoringService.getTemplateItemById( etdl.templateItem1User.getId() );
      assertNotNull(templateItem);
      assertEquals(etdl.templateItem1User.getId(), templateItem.getId());

      // test get eval by invalid id returns null
      templateItem = authoringService.getTemplateItemById( EvalTestDataLoad.INVALID_LONG_ID );
      assertNull(templateItem);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getTemplateItemById(java.lang.Long)}.
    */
   public void testGetTemplateItemByEid() {
      EvalTemplateItem templateItem = null;

      // test getting valid templateItems by eid
      templateItem = authoringService.getTemplateItemByEid( etdl.templateItem1Eid.getEid() );
      assertNotNull(templateItem);
      assertEquals(etdl.templateItem1Eid.getEid(), templateItem.getEid());

      templateItem = authoringService.getTemplateItemByEid( etdl.templateItem2Eid.getEid() );
      assertNotNull(templateItem);
      assertEquals(etdl.templateItem2Eid.getEid(), templateItem.getEid());

      templateItem = authoringService.getTemplateItemByEid( etdl.templateItem3Eid.getEid() );
      assertNotNull(templateItem);
      assertEquals(etdl.templateItem3Eid.getEid(), templateItem.getEid());

      //test getting valid template item not having eid set returns null
      templateItem = authoringService.getTemplateItemByEid( etdl.templateItem1User.getEid() );
      assertNull(templateItem);

      //test getting template item using invalid eid returns null
      templateItem = authoringService.getTemplateItemByEid( EvalTestDataLoad.INVALID_STRING_EID );
      assertNull(templateItem);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#saveTemplateItem(org.sakaiproject.evaluation.model.EvalTemplateItem, java.lang.String)}.
    */
   public void testSaveTemplateItem() {
      // load up a no items template to work with
      EvalTemplate noItems = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, etdl.templateAdminNoItems.getId());

      // test saving a new templateItem actually creates the linkage in the item and template
      EvalTemplateItem eiTest1 = new EvalTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
            noItems, etdl.item5, null, 
            EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
            EvalConstants.HIERARCHY_NODE_ID_NONE, new Integer(3),
            null, Boolean.FALSE, false, false, null, null, null);
      authoringService.saveTemplateItem( eiTest1, 
            EvalTestDataLoad.ADMIN_USER_ID);
      assertNotNull( eiTest1.getItem() );
     
      assertNotNull( eiTest1.getTemplate() );
      assertNotNull( eiTest1.getItem().getTemplateItems() );
      assertNotNull( eiTest1.getTemplate().getTemplateItems() );
      // verify items are there
      assertEquals( eiTest1.getItem().getId(), etdl.item5.getId() );
      assertEquals( eiTest1.getTemplate().getId(), noItems.getId() );
      // check if the templateItem is contained in the new sets
      assertEquals( 4, eiTest1.getItem().getTemplateItems().size() );
      assertEquals( 1, eiTest1.getTemplate().getTemplateItems().size() );
      assertTrue( eiTest1.getItem().getTemplateItems().contains(eiTest1) );
      assertTrue( eiTest1.getTemplate().getTemplateItems().contains(eiTest1) );

      // make sure the displayOrder is set correctly when null (to 1)
      assertEquals( 1, eiTest1.getDisplayOrder().intValue() );

      // test saving a valid templateItem
      authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
            noItems, etdl.item7, new Integer(2), 
            EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
            EvalConstants.HIERARCHY_NODE_ID_NONE, new Integer(3),
            null, Boolean.FALSE, false, false, null, null, null),
            EvalTestDataLoad.ADMIN_USER_ID);

      // test saving valid templateItem with locked item
      authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templateUnused, etdl.item2, new Integer(3), 
            EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
            EvalConstants.HIERARCHY_NODE_ID_NONE, null,
            EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, false, false, null, null, null),
            EvalTestDataLoad.MAINT_USER_ID);

      // test saving valid templateItem with empty required fields (inherit from item)
      EvalTemplateItem eiTest2 = new EvalTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
            noItems, etdl.item4, new Integer(99), 
            null, EvalConstants.HIERARCHY_LEVEL_TOP, 
            EvalConstants.HIERARCHY_NODE_ID_NONE, null,
            null, null, false, false, null, null, null);
      authoringService.saveTemplateItem( eiTest2, 
            EvalTestDataLoad.ADMIN_USER_ID);
      // make sure the values are filled in for us
      assertNotNull( eiTest2.getLastModified() );
      assertNotNull( eiTest2.getCategory() );
      assertNotNull( eiTest2.getScaleDisplaySetting() );
      assertNotNull( eiTest2.getUsesNA() );
      // make sure filled in values match the ones set in the item
      assertTrue( eiTest2.getCategory().equals(etdl.item4.getCategory()) );
      assertTrue( eiTest2.getScaleDisplaySetting().equals(etdl.item4.getScaleDisplaySetting()) );
      // not checking is UsesNA is equal because it is null in the item

      // make sure the displayOrder is set correctly (to 3) when set wrong
      assertEquals( 3, eiTest2.getDisplayOrder().intValue() );

      // test saving templateItem with no item fails
      try {
         authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
               etdl.templateUnused, null, new Integer(3), 
               EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
               EvalConstants.HIERARCHY_NODE_ID_NONE, null,
               EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, false, false, null, null, null),
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
      
      // test saving templateItem with no template fails
      try {
         authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
               null, etdl.item3, new Integer(3), 
               EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
               EvalConstants.HIERARCHY_NODE_ID_NONE, null,
               EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, false, false, null, null, null),
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test saving scaled item with text size set fails
      try {
         authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
               etdl.templateUnused, etdl.item4, new Integer(3), 
               EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
               EvalConstants.HIERARCHY_NODE_ID_NONE, new Integer(2),
               EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, false, false, null, null, null),
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test saving text item with scale display setting fails
      try {
         authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
               etdl.templateUnused, etdl.item6, new Integer(3), 
               EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
               EvalConstants.HIERARCHY_NODE_ID_NONE, new Integer(4),
               EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, false, false, null, null, null),
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // TODO - add logic to not allow an item to be associated with the same template twice?
//    // test saving header type item with scale setting or text size set fails
//    try {
//    authoringService.saveTemplateItem( new EvalTemplateItem( new Date(), 
//    EvalTestDataLoad.MAINT_USER_ID, etdl.templateUnused, etdl.item3, 
//    new Integer(3), EvalConstants.ITEM_CATEGORY_COURSE, 
//    null, EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, null, null),
//    EvalTestDataLoad.MAINT_USER_ID);
//    fail("Should have thrown exception");
//    } catch (IllegalArgumentException e) {
//    assertNotNull(e);
//    fail(e.getMessage()); // see why failing
//    }

      // test saving header type item with scale setting or text size set fails
      try {
         authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
               etdl.templateUnused, etdl.item8, new Integer(3), 
               EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
               EvalConstants.HIERARCHY_NODE_ID_NONE, new Integer(1),
               EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, false, false, null, null, null),
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
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
      testTemplateItem1.setCategory( EvalConstants.ITEM_CATEGORY_INSTRUCTOR );
      authoringService.saveTemplateItem( testTemplateItem1, EvalTestDataLoad.ADMIN_USER_ID );

      testTemplateItem2.setCategory( EvalConstants.ITEM_CATEGORY_INSTRUCTOR );
      authoringService.saveTemplateItem( testTemplateItem2, EvalTestDataLoad.MAINT_USER_ID );

      // TODO - CANNOT RUN THIS TEST FOR NOW BECAUSE OF HIBERNATE
//    // test that template and item cannot be changed on existing templateItem
//    try {
//    testTemplateItem1.setItem( etdl.item1 );
//    authoringService.saveTemplateItem( testTemplateItem1, EvalTestDataLoad.ADMIN_USER_ID );
//    fail("Should have thrown exception");
//    } catch (IllegalStateException e) {
//    assertNotNull(e);
//    fail(e.getMessage()); // see why failing
//    }

//    try {
//    testTemplateItem1.setTemplate( etdl.templateAdminNoItems );
//    authoringService.saveTemplateItem( testTemplateItem1, EvalTestDataLoad.ADMIN_USER_ID );
//    fail("Should have thrown exception");
//    } catch (IllegalStateException e) {
//    assertNotNull(e);
//    fail(e.getMessage()); // see why failing
//    }

      // test editing templateItem in LOCKED template fails
      try {
         testTemplateItem3.setCategory( EvalConstants.ITEM_CATEGORY_INSTRUCTOR );
         authoringService.saveTemplateItem( testTemplateItem3, EvalTestDataLoad.ADMIN_USER_ID );
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
      }

      try {
         testTemplateItem4.setCategory( EvalConstants.ITEM_CATEGORY_INSTRUCTOR );
         authoringService.saveTemplateItem( testTemplateItem4, EvalTestDataLoad.MAINT_USER_ID );
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
      }

      // test admin can edit any templateItem
      testTemplateItem2.setCategory( EvalConstants.ITEM_CATEGORY_ENVIRONMENT );
      authoringService.saveTemplateItem( testTemplateItem2, EvalTestDataLoad.ADMIN_USER_ID );

      // test that editing unowned templateItem causes permission failure
      try {
         testTemplateItem2.setCategory( EvalConstants.ITEM_CATEGORY_COURSE );
         authoringService.saveTemplateItem( testTemplateItem2, EvalTestDataLoad.USER_ID );
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
      }

      // test that editing unowned templateItem causes permission failure
      try {
         testTemplateItem1.setCategory( EvalConstants.ITEM_CATEGORY_COURSE );
         authoringService.saveTemplateItem( testTemplateItem1, EvalTestDataLoad.MAINT_USER_ID );
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#deleteTemplateItem(java.lang.Long, java.lang.String)}.
    */
   public void testDeleteTemplateItem() {
      // test removing templateItem without permissions fails
      try {
         authoringService.deleteTemplateItem(etdl.templateItem3PU.getId(), 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
      }

      try {
         authoringService.deleteTemplateItem(etdl.templateItem3U.getId(), 
               EvalTestDataLoad.USER_ID);
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
      }

      // test removing templateItem from locked template fails
      try {
         authoringService.deleteTemplateItem(etdl.templateItem1P.getId(), 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
      }

      try {
         authoringService.deleteTemplateItem(etdl.templateItem2A.getId(), 
               EvalTestDataLoad.ADMIN_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
      }

      // verify that the item/template link exists before removal
      EvalTemplateItem eti1 = authoringService.getTemplateItemById(etdl.templateItem3U.getId());
      assertNotNull( eti1 );
      assertNotNull( eti1.getItem() );
      assertNotNull( eti1.getTemplate() );
      assertNotNull( eti1.getItem().getTemplateItems() );
      assertNotNull( eti1.getTemplate().getTemplateItems() );
      assertFalse( eti1.getItem().getTemplateItems().isEmpty() );
      assertFalse( eti1.getTemplate().getTemplateItems().isEmpty() );
      assertTrue( eti1.getItem().getTemplateItems().contains( eti1 ) );
      assertTrue( eti1.getTemplate().getTemplateItems().contains( eti1 ) );
      int itemsSize = eti1.getItem().getTemplateItems().size();
      int templatesSize = eti1.getTemplate().getTemplateItems().size();

      // test removing unused templateItem OK
      authoringService.deleteTemplateItem(etdl.templateItem3U.getId(), 
            EvalTestDataLoad.MAINT_USER_ID);
      assertNull( authoringService.getTemplateItemById(etdl.templateItem3U.getId()) );

      // verify that the item/template link no longer exists
      assertNotNull( eti1.getItem().getTemplateItems() );
      assertNotNull( eti1.getTemplate().getTemplateItems() );
      assertFalse( eti1.getItem().getTemplateItems().isEmpty() );
      assertFalse( eti1.getTemplate().getTemplateItems().isEmpty() );
      assertEquals( itemsSize-1, eti1.getItem().getTemplateItems().size() );
      assertEquals( templatesSize-1, eti1.getTemplate().getTemplateItems().size() );
      assertTrue(! eti1.getItem().getTemplateItems().contains( eti1 ) );
      assertTrue(! eti1.getTemplate().getTemplateItems().contains( eti1 ) );

      authoringService.deleteTemplateItem(etdl.templateItem6UU.getId(), 
            EvalTestDataLoad.USER_ID);
      assertNull( authoringService.getTemplateItemById(etdl.templateItem6UU.getId()) );

      // test admin can remove unowned templateItem
      authoringService.deleteTemplateItem(etdl.templateItem5U.getId(), 
            EvalTestDataLoad.ADMIN_USER_ID);
      assertNull( authoringService.getTemplateItemById(etdl.templateItem5U.getId()) );

      // test removing invalid templateItem id fails
      try {
         authoringService.deleteTemplateItem(EvalTestDataLoad.INVALID_LONG_ID, 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getTemplateItemsForTemplate(java.lang.Long)}.
    */
   public void testGetTemplateItemsForTemplate() {
      List<EvalTemplateItem> l = null;
      List<Long> ids = null;

      // test getting all items by valid templates
      l = authoringService.getTemplateItemsForTemplate( etdl.templateAdmin.getId(), null, null, null );
      assertNotNull( l );
      assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateItem2A.getId() ));
      assertTrue(ids.contains( etdl.templateItem3A.getId() ));
      assertTrue(ids.contains( etdl.templateItem5A.getId() ));

      // check that the return order is correct
      assertEquals( 1, ((EvalTemplateItem)l.get(0)).getDisplayOrder().intValue() );
      assertEquals( 2, ((EvalTemplateItem)l.get(1)).getDisplayOrder().intValue() );
      assertEquals( 3, ((EvalTemplateItem)l.get(2)).getDisplayOrder().intValue() );

      // test getting all items by valid templates
      l = authoringService.getTemplateItemsForTemplate( etdl.templatePublic.getId(), null, null, null );
      assertNotNull( l );
      assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateItem1P.getId() ));

      // test getting items from template with no items
      l = authoringService.getTemplateItemsForTemplate( etdl.templateAdminNoItems.getId(), null, null, null );
      assertNotNull( l );
      assertEquals(0, l.size());

      // test getting items for specific user returns correct items
      // admin should get all items
      l = authoringService.getTemplateItemsForTemplate( etdl.templateAdmin.getId(), null, null, null );
      assertNotNull( l );
      assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateItem2A.getId() ));
      assertTrue(ids.contains( etdl.templateItem3A.getId() ));
      assertTrue(ids.contains( etdl.templateItem5A.getId() ));

      l = authoringService.getTemplateItemsForTemplate( etdl.templateUnused.getId(), null, null, null );
      assertNotNull( l );
      assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateItem1U.getId() ));
      assertTrue(ids.contains( etdl.templateItem3U.getId() ));
      assertTrue(ids.contains( etdl.templateItem5U.getId() ));

      // check that the return order is correct
      assertEquals( 1, ((EvalTemplateItem)l.get(0)).getDisplayOrder().intValue() );
      assertEquals( 2, ((EvalTemplateItem)l.get(1)).getDisplayOrder().intValue() );
      assertEquals( 3, ((EvalTemplateItem)l.get(2)).getDisplayOrder().intValue() );

      // owner should see all items
      l = authoringService.getTemplateItemsForTemplate( etdl.templateUnused.getId(), null, null, null );
      assertNotNull( l );
      assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateItem1U.getId() ));
      assertTrue(ids.contains( etdl.templateItem3U.getId() ));
      assertTrue(ids.contains( etdl.templateItem5U.getId() ));

      l = authoringService.getTemplateItemsForTemplate( etdl.templateUser.getId(), null, null, null );
      assertNotNull( l );
      assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateItem1User.getId() ));
      assertTrue(ids.contains( etdl.templateItem5User.getId() ));

      // TODO - takers should see items at their level (one level) if they have access
      l = authoringService.getTemplateItemsForTemplate( etdl.templateUser.getId(), null, null, null );
      assertNotNull( l );
      assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateItem1User.getId() ));
      assertTrue(ids.contains( etdl.templateItem5User.getId() ));

      // TODO - add in tests that take the hierarchy into account

      // test getting items from invalid template returns nothing
      l = authoringService.getTemplateItemsForTemplate( EvalTestDataLoad.INVALID_LONG_ID, null, null, null );
      assertNotNull( l );
      assertEquals(0, l.size());

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getBlockChildTemplateItemsForBlockParent(Long, boolean)}.
    */
   public void testGetBlockChildTemplateItemsForBlockParent() {
      List<EvalTemplateItem> l = null;
      List<Long> ids = null;

      // test getting child block items
      l = authoringService.getBlockChildTemplateItemsForBlockParent( etdl.templateItem9B.getId(), false );
      assertNotNull( l );
      assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateItem2B.getId() ));
      assertTrue(ids.contains( etdl.templateItem3B.getId() ));

      // check that the return order is correct
      assertEquals( 1, ((EvalTemplateItem)l.get(0)).getDisplayOrder().intValue() );
      assertEquals( 2, ((EvalTemplateItem)l.get(1)).getDisplayOrder().intValue() );

      // test getting child block items and parent
      l = authoringService.getBlockChildTemplateItemsForBlockParent( etdl.templateItem9B.getId(), true );
      assertNotNull( l );
      assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateItem9B.getId() ));
      assertTrue(ids.contains( etdl.templateItem2B.getId() ));
      assertTrue(ids.contains( etdl.templateItem3B.getId() ));

      // check that the return order is correct
      assertEquals( 1, ((EvalTemplateItem)l.get(0)).getDisplayOrder().intValue() );
      assertEquals( 1, ((EvalTemplateItem)l.get(1)).getDisplayOrder().intValue() );
      assertEquals( 2, ((EvalTemplateItem)l.get(2)).getDisplayOrder().intValue() );

      // test getting child items from invalid templateItem fails
      try {
         authoringService.getBlockChildTemplateItemsForBlockParent( EvalTestDataLoad.INVALID_LONG_ID, false );
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test getting child items from non-parent templateItem fails
      try {
         authoringService.getBlockChildTemplateItemsForBlockParent( etdl.templateItem2A.getId(), false );
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#canModifyItem(String, Long)}.
    */
   public void testCanModifyItem() {
      // test can control owned items
      assertTrue( authoringService.canModifyItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.item7.getId() ) );
      assertTrue( authoringService.canModifyItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.item4.getId() ) );

      // test admin user can override perms
      assertTrue( authoringService.canModifyItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.item4.getId() ) );

      // test cannot control unowned items
      assertFalse( authoringService.canModifyItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.item7.getId() ) );
      assertFalse( authoringService.canModifyItem( EvalTestDataLoad.USER_ID, 
            etdl.item4.getId() ) );

      // test cannot control locked items
      assertFalse( authoringService.canModifyItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.item1.getId() ) );
      assertFalse( authoringService.canModifyItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.item2.getId() ) );

      // test invalid item id causes failure
      try {
         authoringService.canModifyItem( EvalTestDataLoad.MAINT_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID );
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#canRemoveItem(String, Long)}.
    */
   public void testCanRemoveItem() {
      // test can remove owned items
      assertTrue( authoringService.canRemoveItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.item7.getId() ) );
      assertTrue( authoringService.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.item4.getId() ) );

      // test admin user can override perms
      assertTrue( authoringService.canRemoveItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.item4.getId() ) );

      // test cannot remove unowned items
      assertFalse( authoringService.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.item7.getId() ) );
      assertFalse( authoringService.canRemoveItem( EvalTestDataLoad.USER_ID, 
            etdl.item4.getId() ) );

      // can remove items that are in use now
//      // test cannot remove unlocked items that are in use in templates
//      assertFalse( authoringService.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
//            etdl.item6.getId() ) );
//      assertFalse( authoringService.canRemoveItem( EvalTestDataLoad.ADMIN_USER_ID, 
//            etdl.item9.getId() ) );

      // test cannot remove locked items
      assertFalse( authoringService.canRemoveItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.item1.getId() ) );
      assertFalse( authoringService.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.item2.getId() ) );

      // test invalid item id causes failure
      try {
         authoringService.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID );
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#canControlTemplateItem(java.lang.String, java.lang.Long)}.
    */
   public void testCanControlTemplateItem() {
      // test can control owned items
      assertTrue( authoringService.canControlTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateItem3PU.getId() ) );
      assertTrue( authoringService.canControlTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templateItem3U.getId() ) );

      // test admin user can override perms
      assertTrue( authoringService.canControlTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateItem3U.getId() ) );

      // test cannot control unowned items
      assertFalse( authoringService.canControlTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templateItem3PU.getId() ) );
      assertFalse( authoringService.canControlTemplateItem( EvalTestDataLoad.USER_ID, 
            etdl.templateItem3U.getId() ) );

      // test cannot control items locked by locked template
      assertFalse( authoringService.canControlTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateItem2A.getId() ) );
      assertFalse( authoringService.canControlTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templateItem1P.getId() ) );

      // test invalid item id causes failure
      try {
         authoringService.canControlTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID );
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }
   }



   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#getItemGroups(java.lang.Long, java.lang.String, boolean)}.
    */
   public void testGetItemGroups() {
      List<Long> ids = null;
      List<EvalItemGroup> eItems = null;

      // NOTE: preloaded groups to take into account

      // check all expert top level groups
      eItems = authoringService.getItemGroups(null, EvalTestDataLoad.ADMIN_USER_ID, true, true);
      assertNotNull( eItems );
      assertEquals(3 + 4, eItems.size()); // 4 preloaded top level expert groups
      ids = EvalTestDataLoad.makeIdList(eItems);
      assertTrue(ids.contains( etdl.categoryA.getId() ));
      assertTrue(ids.contains( etdl.categoryB.getId() ));
      assertTrue(ids.contains( etdl.categoryC.getId() ));
      assertTrue(! ids.contains( etdl.categoryD.getId() ));
      assertTrue(! ids.contains( etdl.objectiveA1.getId() ));
      assertTrue(! ids.contains( etdl.objectiveA2.getId() ));

      // check all non-expert top level groups
      eItems = authoringService.getItemGroups(null, EvalTestDataLoad.ADMIN_USER_ID, true, false);
      assertNotNull( eItems );
      assertEquals(1, eItems.size());
      ids = EvalTestDataLoad.makeIdList(eItems);
      assertTrue(! ids.contains( etdl.categoryA.getId() ));
      assertTrue(! ids.contains( etdl.categoryB.getId() ));
      assertTrue(! ids.contains( etdl.categoryC.getId() ));
      assertTrue(ids.contains( etdl.categoryD.getId() ));
      assertTrue(! ids.contains( etdl.objectiveA1.getId() ));
      assertTrue(! ids.contains( etdl.objectiveA2.getId() ));

      // check all contained groups (objectives) in a parent (category)
      eItems = authoringService.getItemGroups(etdl.categoryA.getId(), EvalTestDataLoad.ADMIN_USER_ID, true, true);
      assertNotNull( eItems );
      assertEquals(2, eItems.size());
      ids = EvalTestDataLoad.makeIdList(eItems);
      assertTrue(ids.contains( etdl.objectiveA1.getId() ));
      assertTrue(ids.contains( etdl.objectiveA2.getId() ));

      // check only non-empty top level groups
      eItems = authoringService.getItemGroups(null, EvalTestDataLoad.ADMIN_USER_ID, false, true);
      assertNotNull( eItems );
      assertEquals(2 + 4, eItems.size()); // 4 preloaded non-empty top level groups
      ids = EvalTestDataLoad.makeIdList(eItems);
      assertTrue(ids.contains( etdl.categoryA.getId() ));
      assertTrue(ids.contains( etdl.categoryB.getId() ));

      // check only non-empty contained groups
      eItems = authoringService.getItemGroups(etdl.categoryA.getId(), EvalTestDataLoad.ADMIN_USER_ID, false, true);
      assertNotNull( eItems );
      assertEquals(1, eItems.size());
      ids = EvalTestDataLoad.makeIdList(eItems);
      assertTrue(ids.contains( etdl.objectiveA1.getId() ));      

      // check trying to get groups from empty group
      eItems = authoringService.getItemGroups(etdl.categoryC.getId(), EvalTestDataLoad.ADMIN_USER_ID, false, true);
      assertNotNull( eItems );
      assertEquals(0, eItems.size());

      // test attempting to use invalid item group id
      try {
         eItems = authoringService.getItemGroups(EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.ADMIN_USER_ID, false, true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail(e.getMessage()); // check the reason for failure
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#getItemsInItemGroup(java.lang.Long, boolean)}.
    */
   public void testGetItemsInItemGroup() {
      List<Long> ids = null;
      List<EvalItem> eItems = null;

      // check items from a low level group
      eItems = authoringService.getItemsInItemGroup(etdl.objectiveA1.getId(), true);
      assertNotNull( eItems );
      assertEquals(2, eItems.size());
      ids = EvalTestDataLoad.makeIdList(eItems);
      assertTrue(ids.contains( etdl.item2.getId() ));
      assertTrue(ids.contains( etdl.item6.getId() ));

      // check items from a top level group
      eItems = authoringService.getItemsInItemGroup(etdl.categoryB.getId(), true);
      assertNotNull( eItems );
      assertEquals(1, eItems.size());
      ids = EvalTestDataLoad.makeIdList(eItems);
      assertTrue(ids.contains( etdl.item1.getId() ));

      // check items from an empty group
      eItems = authoringService.getItemsInItemGroup(etdl.objectiveA2.getId(), true);
      assertNotNull( eItems );
      assertEquals(0, eItems.size());

      // test attempting to use invalid item group id
      try {
         eItems = authoringService.getItemsInItemGroup(EvalTestDataLoad.INVALID_LONG_ID, true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

   }


   /**
    * Test method for {@link EvalExpertItemsLogicImpl#getItemGroupById(Long)}
    */
   public void testGetItemGroupById() {
      EvalItemGroup itemGroup = null;

      // test getting valid items by id
      itemGroup = authoringService.getItemGroupById( etdl.categoryA.getId() );
      assertNotNull(itemGroup);
      assertEquals(etdl.categoryA.getId(), itemGroup.getId());

      itemGroup = authoringService.getItemGroupById( etdl.objectiveA1.getId() );
      assertNotNull(itemGroup);
      assertEquals(etdl.objectiveA1.getId(), itemGroup.getId());

      // test get eval by invalid id returns null
      itemGroup = authoringService.getItemGroupById( EvalTestDataLoad.INVALID_LONG_ID );
      assertNull(itemGroup);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#saveItemGroup(org.sakaiproject.evaluation.model.EvalItemGroup, java.lang.String)}.
    */
   public void testSaveItemGroup() {

      // test create a valid group
      EvalItemGroup newCategory = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, 
            EvalConstants.ITEM_GROUP_TYPE_CATEGORY, "new category");
      authoringService.saveItemGroup(newCategory, EvalTestDataLoad.ADMIN_USER_ID);
      assertNotNull( newCategory.getId() );

      // check that defaults were filled in
      assertNotNull( newCategory.getLastModified() );
      assertNotNull( newCategory.getExpert() );
      assertEquals( newCategory.getExpert().booleanValue(), false );
      assertNull( newCategory.getParent() );

      // test that creating subgroup without parent causes failure
      EvalItemGroup newObjective = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, 
            EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE, "new objective");
      try {
         authoringService.saveItemGroup(newObjective, EvalTestDataLoad.ADMIN_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail(e.getMessage()); // check the reason for failure
      }

      // test create a valid subgroup
      newObjective.setParent( newCategory );
      authoringService.saveItemGroup(newObjective, EvalTestDataLoad.ADMIN_USER_ID);

      // test non-admin cannot create expert group
      EvalItemGroup newExpertGroup = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, 
            EvalConstants.ITEM_GROUP_TYPE_CATEGORY, "new expert");
      newExpertGroup.setExpert( Boolean.TRUE );
      try {
         authoringService.saveItemGroup(newExpertGroup, EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test admin can create expert group
      authoringService.saveItemGroup(newExpertGroup, EvalTestDataLoad.ADMIN_USER_ID);
      assertNotNull( newExpertGroup.getId() );

      // test creating invalid expert group type fails
      try {
         authoringService.saveItemGroup( 
               new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalTestDataLoad.INVALID_CONSTANT_STRING, "test",
                     "desc", Boolean.TRUE, null, null), 
                     EvalTestDataLoad.ADMIN_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test creating top level category with parent fails
      try {
         authoringService.saveItemGroup( 
               new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.ITEM_GROUP_TYPE_CATEGORY, "test",
                     "desc", Boolean.FALSE, newCategory, null), 
                     EvalTestDataLoad.ADMIN_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test trying to put objective as a top level category
      try {
         authoringService.saveItemGroup( 
               new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE, "test",
                     "desc", Boolean.FALSE, null, null), 
                     EvalTestDataLoad.ADMIN_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#removeItemGroup(java.lang.Long, java.lang.String, boolean)}.
    */
   public void testRemoveItemGroup() {

      // test cannot remove item groups without permission
      try {
         authoringService.removeItemGroup(etdl.categoryD.getId(), EvalTestDataLoad.MAINT_USER_ID, false);
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
         //fail(e.getMessage()); // check the reason for failure
      }

      // test can remove empty categories
      authoringService.removeItemGroup(etdl.categoryD.getId(), EvalTestDataLoad.ADMIN_USER_ID, false);
      assertNull( authoringService.getItemGroupById(etdl.categoryD.getId()) );

      // test cannot remove non-empty categories when flag set
      try {
         authoringService.removeItemGroup(etdl.categoryA.getId(), EvalTestDataLoad.ADMIN_USER_ID, false);
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
         //fail(e.getMessage()); // check the reason for failure
      }

      // test can remove non-empty categories when flag unset
      authoringService.removeItemGroup(etdl.categoryA.getId(), EvalTestDataLoad.ADMIN_USER_ID, true);
      assertNull( authoringService.getItemGroupById(etdl.categoryA.getId()) );

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#canUpdateItemGroup(String, Long)}.
    */
   public void testCanUpdateItemGroup() {
      // test can control owned items
      assertTrue( authoringService.canUpdateItemGroup(EvalTestDataLoad.ADMIN_USER_ID, etdl.categoryA.getId() ) );
      assertTrue( authoringService.canUpdateItemGroup(EvalTestDataLoad.ADMIN_USER_ID, etdl.objectiveA1.getId() ) );

      // test cannot control unowned items
      assertFalse( authoringService.canUpdateItemGroup(EvalTestDataLoad.MAINT_USER_ID, etdl.categoryA.getId() ) );
      assertFalse( authoringService.canUpdateItemGroup(EvalTestDataLoad.MAINT_USER_ID, etdl.objectiveA1.getId() ) );

      // test invalid item id causes failure
      try {
         authoringService.canUpdateItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalTestDataLoad.INVALID_LONG_ID );
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#canRemoveItemGroup(String, Long)}.
    */
   public void testCanRemoveItemGroup() {
      // test can control owned items
      assertTrue( authoringService.canRemoveItemGroup(EvalTestDataLoad.ADMIN_USER_ID, etdl.categoryA.getId() ) );
      assertTrue( authoringService.canRemoveItemGroup(EvalTestDataLoad.ADMIN_USER_ID, etdl.objectiveA1.getId() ) );

      // test cannot control unowned items
      assertFalse( authoringService.canRemoveItemGroup(EvalTestDataLoad.MAINT_USER_ID, etdl.categoryA.getId() ) );
      assertFalse( authoringService.canRemoveItemGroup(EvalTestDataLoad.MAINT_USER_ID, etdl.objectiveA1.getId() ) );

      // test invalid item id causes failure
      try {
         authoringService.canRemoveItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalTestDataLoad.INVALID_LONG_ID );
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

   }



   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#getTemplateById(java.lang.Long)}.
    */
   public void testGetTemplateById() {
      EvalTemplate template = null;

      // test getting valid templates by id
      template = authoringService.getTemplateById( etdl.templateAdmin.getId() );
      assertNotNull(template);
      assertEquals(etdl.templateAdmin.getId(), template.getId());

      template = authoringService.getTemplateById( etdl.templatePublic.getId() );
      assertNotNull(template);
      assertEquals(etdl.templatePublic.getId(), template.getId());

      // test get eval by invalid id returns null
      template = authoringService.getTemplateById( EvalTestDataLoad.INVALID_LONG_ID );
      assertNull(template);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#getTemplateByEid(java.lang.String)}.
    */
   public void testGetTemplateByEid() {
      EvalTemplate template = null;

      // test getting template having eid set
      template = authoringService.getTemplateByEid( etdl.templateEid.getEid() );
      assertNotNull(template);
      assertEquals(etdl.templateEid.getEid(), template.getEid());

      //test getting template having eid not set  returns null
      template = authoringService.getTemplateByEid( etdl.templatePublic.getEid() );
      assertNull(template);

      // test getting template by invalid id returns null
      template = authoringService.getTemplateByEid( EvalTestDataLoad.INVALID_STRING_EID );
      assertNull(template);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#saveTemplate(org.sakaiproject.evaluation.model.EvalTemplate, java.lang.String)}.
    */
   public void testSaveTemplate() {
      String test_title = "test template title";
      // test saving a valid template
      authoringService.saveTemplate( new EvalTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            EvalConstants.TEMPLATE_TYPE_STANDARD, 
            test_title, EvalConstants.SHARING_PRIVATE, 
            EvalTestDataLoad.NOT_EXPERT), 
            EvalTestDataLoad.MAINT_USER_ID);

      // test saving valid template locked
      authoringService.saveTemplate( new EvalTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
            EvalConstants.TEMPLATE_TYPE_STANDARD, 
            "admin test template", "desc", 
            EvalConstants.SHARING_PRIVATE, EvalTestDataLoad.EXPERT, "expert desc", 
            null, EvalTestDataLoad.LOCKED, false), 
            EvalTestDataLoad.ADMIN_USER_ID);

      // test user without perms cannot create template
      try {
         authoringService.saveTemplate( new EvalTemplate( EvalTestDataLoad.USER_ID, 
               EvalConstants.TEMPLATE_TYPE_STANDARD, 
               "user test title 1", EvalConstants.SHARING_PRIVATE, 
               EvalTestDataLoad.NOT_EXPERT), 
               EvalTestDataLoad.USER_ID);
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
      }

      // fetch templates to work with (for editing tests)
      EvalTemplate testTemplate1 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, 
            etdl.templatePublicUnused.getId());
      EvalTemplate testTemplate2 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, 
            etdl.templateUnused.getId());
      EvalTemplate testTemplate3 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, 
            etdl.templatePublic.getId());
      EvalTemplate testTemplate4 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, 
            etdl.templateUserUnused.getId());

      // test editing unlocked template
      testTemplate2.setDescription("something maint user new");
      authoringService.saveTemplate( testTemplate2, 
            EvalTestDataLoad.MAINT_USER_ID);

      // TODO - CANNOT RUN THIS TEST FOR NOW BECAUSE OF HIBERNATE
//    // test that LOCKED cannot be changed to FALSE on existing template
//    try {
//    testTemplate3.setLocked(Boolean.FALSE);
//    authoringService.saveTemplate( testTemplate3, 
//    EvalTestDataLoad.ADMIN_USER_ID);
//    fail("Should have thrown exception");
//    } catch (RuntimeException e) {
//    assertNotNull(e);
//    fail(e.getMessage()); // see why failing
//    }

      // test editing LOCKED template fails
      try {
         testTemplate3.setExpert(Boolean.FALSE);
         authoringService.saveTemplate( testTemplate3, 
               EvalTestDataLoad.ADMIN_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
      }

      // test admin can edit any template
      testTemplate1.setDescription("something admin new");
      authoringService.saveTemplate( testTemplate1, 
            EvalTestDataLoad.ADMIN_USER_ID);

      // test that editing unowned template causes permission failure
      try {
         testTemplate4.setDescription("something maint new");
         authoringService.saveTemplate( testTemplate4, 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
      }

      // test that setting sharing to PUBLIC as non-admin fails
      try {
         testTemplate1.setSharing(EvalConstants.SHARING_PUBLIC);
         authoringService.saveTemplate( testTemplate1, 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // test admin can set sharing to public
      testTemplate1.setSharing(EvalConstants.SHARING_PUBLIC);
      authoringService.saveTemplate( testTemplate1, 
            EvalTestDataLoad.ADMIN_USER_ID);

      // TODO - test cannot save template with no associated items

      // TODO - test saving template saves all associated items at same time

      // test CAN save 2 templates with same title
      authoringService.saveTemplate( new EvalTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            EvalConstants.TEMPLATE_TYPE_STANDARD, 
            test_title, EvalConstants.SHARING_PRIVATE, 
            EvalTestDataLoad.NOT_EXPERT), 
            EvalTestDataLoad.MAINT_USER_ID);

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#deleteTemplate(java.lang.Long, java.lang.String)}.
    */
   public void testDeleteTemplate() {
      // test removing template without permissions fails
      try {
         authoringService.deleteTemplate(etdl.templatePublicUnused.getId(), 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
      }

      try {
         authoringService.deleteTemplate(etdl.templateUnused.getId(), 
               EvalTestDataLoad.USER_ID);
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
      }

      // test removing locked template fails
      try {
         authoringService.deleteTemplate(etdl.templatePublic.getId(), 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
      }

      try {
         authoringService.deleteTemplate(etdl.templateAdmin.getId(), 
               EvalTestDataLoad.ADMIN_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
      }

      // test cannot remove expert template
      try {
         authoringService.deleteTemplate(etdl.templateUserUnused.getId(), 
               EvalTestDataLoad.ADMIN_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         assertNotNull(e);
      }

      // test removing unused template OK
      authoringService.deleteTemplate(etdl.templateUnused.getId(), 
            EvalTestDataLoad.MAINT_USER_ID);
      assertNull( authoringService.getTemplateById(etdl.templateUnused.getId()) );

      authoringService.deleteTemplate(etdl.templatePublicUnused.getId(), 
            EvalTestDataLoad.ADMIN_USER_ID);
      assertNull( authoringService.getTemplateById(etdl.templatePublicUnused.getId()) );

      // test removing invalid template id fails
      try {
         authoringService.deleteTemplate(EvalTestDataLoad.INVALID_LONG_ID, 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#getTemplatesForUser(String, String)}.
    */
   public void testGetTemplatesForUser() {
      List<EvalTemplate> l = null;
      List<Long> ids = null;
      // NOTE: No preloaded public templates to take into account right now

      // test getting all templates for admin user (should include all templates)
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, null, true);
      assertNotNull(l);
      assertEquals(9, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateAdmin.getId() ));
      assertTrue(ids.contains( etdl.templateAdminNoItems.getId() ));
      assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
      assertTrue(ids.contains( etdl.templatePublic.getId() ));
      assertTrue(ids.contains( etdl.templateUnused.getId() ));
      assertTrue(ids.contains( etdl.templateUser.getId() ));
      assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
      assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));

      assertTrue(ids.contains( etdl.templateEid.getId() ));

      // test getting all non-empty templates for admin user
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, null, false);
      assertNotNull(l);
      assertEquals(8, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(! ids.contains( etdl.templateAdminNoItems.getId() ));
      assertTrue(ids.contains( etdl.templateAdmin.getId() ));
      assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
      assertTrue(ids.contains( etdl.templatePublic.getId() ));
      assertTrue(ids.contains( etdl.templateUnused.getId() ));
      assertTrue(ids.contains( etdl.templateUser.getId() ));
      assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
      assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));

      assertTrue(ids.contains( etdl.templateEid.getId() ));

      // test getting all templates for maint user
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.MAINT_USER_ID, null, true);
      assertNotNull(l);
      assertEquals(4, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateUnused.getId() ));
      assertTrue(ids.contains( etdl.templatePublic.getId() ));
      assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));

      assertTrue(ids.contains( etdl.templateEid.getId() ));

      // test getting all templates for user
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.USER_ID, null, true);
      assertNotNull(l);
      assertEquals(5, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
      assertTrue(ids.contains( etdl.templatePublic.getId() ));
      assertTrue(ids.contains( etdl.templateUser.getId() ));
      assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

      assertTrue(ids.contains( etdl.templateEid.getId() ));

      // test using SHARING_OWNER same as null (getting all templates)
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_OWNER, true);
      assertNotNull(l);
      assertEquals(9, l.size());

      // test getting private templates for admin (admin should see all private)
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_PRIVATE, true);
      assertNotNull(l);
      assertEquals(6, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateAdmin.getId() ));
      assertTrue(ids.contains( etdl.templateAdminNoItems.getId() ));
      assertTrue(ids.contains( etdl.templateUnused.getId() ));
      assertTrue(ids.contains( etdl.templateUser.getId() ));
      assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
      assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));

      // test getting non-empty private templates for admin
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_PRIVATE, false);
      assertNotNull(l);
      assertEquals(5, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(! ids.contains( etdl.templateAdminNoItems.getId() ));
      assertTrue(ids.contains( etdl.templateAdmin.getId() ));
      assertTrue(ids.contains( etdl.templateUnused.getId() ));
      assertTrue(ids.contains( etdl.templateUser.getId() ));
      assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
      assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));

      // test getting private templates for maint user
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.MAINT_USER_ID, EvalConstants.SHARING_PRIVATE, true);
      assertNotNull(l);
      assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateUnused.getId() ));

      // test getting private templates for user
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.USER_ID, EvalConstants.SHARING_PRIVATE, true);
      assertNotNull(l);
      assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templateUser.getId() ));
      assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

      // test getting public templates only (normal user should see all)
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.USER_ID, EvalConstants.SHARING_PUBLIC, true);
      assertNotNull(l);
      assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      assertTrue(ids.contains( etdl.templatePublic.getId() ));
      assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));

      assertTrue(ids.contains( etdl.templateEid.getId() ));

      // test getting invalid constant causes failure
      try {
         l = authoringService.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, 
               EvalTestDataLoad.INVALID_CONSTANT_STRING, true);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#canModifyTemplate(String, Long)}.
    */
   public void testCanModifyTemplate() {
      // test can control owned templates
      assertTrue( authoringService.canModifyTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templatePublicUnused.getId() ) );
      assertTrue( authoringService.canModifyTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templateUnused.getId() ) );

      // test admin user can override perms
      assertTrue( authoringService.canModifyTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateUnused.getId() ) );

      // test cannot control unowned templates
      assertFalse( authoringService.canModifyTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templatePublicUnused.getId() ) );
      assertFalse( authoringService.canModifyTemplate( EvalTestDataLoad.USER_ID, 
            etdl.templateUnused.getId() ) );

      // test cannot control locked templates
      assertFalse( authoringService.canModifyTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templatePublic.getId() ) );
      assertFalse( authoringService.canModifyTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateAdmin.getId() ) );

      // test invalid template id causes failure
      try {
         authoringService.canModifyTemplate( EvalTestDataLoad.MAINT_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID );
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#canRemoveTemplate(String, Long)}.
    */
   public void testCanRemoveTemplate() {
      // test can remove owned templates
      assertTrue( authoringService.canRemoveTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templatePublicUnused.getId() ) );
      assertTrue( authoringService.canRemoveTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templateUnused.getId() ) );

      // test admin user can override perms
      assertTrue( authoringService.canRemoveTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateUnused.getId() ) );

      // test cannot remove unowned templates
      assertFalse( authoringService.canRemoveTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templatePublicUnused.getId() ) );
      assertFalse( authoringService.canRemoveTemplate( EvalTestDataLoad.USER_ID, 
            etdl.templateUnused.getId() ) );

      // test cannot remove templates that are in use
      assertFalse( authoringService.canRemoveTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templatePublicUnused.getId() ) );
      assertFalse( authoringService.canRemoveTemplate( EvalTestDataLoad.USER_ID, 
            etdl.templateUnused.getId() ) );

      // test cannot remove locked templates
      assertFalse( authoringService.canRemoveTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templatePublic.getId() ) );
      assertFalse( authoringService.canRemoveTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateAdmin.getId() ) );

      // test invalid template id causes failure
      try {
         authoringService.canRemoveTemplate( EvalTestDataLoad.MAINT_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID );
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#canCreateTemplate(java.lang.String)}.
    */
   public void testCanCreateTemplate() {
      // test admin can create templates
      assertTrue( authoringService.canCreateTemplate(EvalTestDataLoad.ADMIN_USER_ID) );

      // test maint user can create templates (user with special perms)
      assertTrue( authoringService.canCreateTemplate(EvalTestDataLoad.MAINT_USER_ID) );

      // test normal user cannot create templates
      assertFalse( authoringService.canCreateTemplate(EvalTestDataLoad.USER_ID) );

      // test invalid user cannot create templates
      assertFalse( authoringService.canCreateTemplate(EvalTestDataLoad.INVALID_USER_ID) );
   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalAuthoringServiceImpl#copyScales(java.lang.Long[], java.lang.String, boolean)}.
    */
   public void testCopyScales() {
      Long[] copiedIds = null;
      Long[] scaleIds = null;

      // copy a single scale
      EvalScale original = etdl.scale1;
      scaleIds = new Long[] {etdl.scale1.getId()};
      copiedIds = authoringService.copyScales(scaleIds, "new scale 1", EvalTestDataLoad.MAINT_USER_ID, true);
      assertNotNull(copiedIds);
      assertEquals(scaleIds.length, copiedIds.length);
      EvalScale copy1 = (EvalScale) evaluationDao.findById(EvalScale.class, copiedIds[0]);
      assertNotNull(copy1);

      // verify the copy worked
      // check the things that should differ
      assertNotSame(original.getId(), copy1.getId());
      assertEquals(original.getId(), copy1.getCopyOf());
      assertEquals("new scale 1", copy1.getTitle());
      assertEquals(EvalTestDataLoad.MAINT_USER_ID, copy1.getOwner());
      assertEquals(true, copy1.isHidden());
      assertEquals(Boolean.FALSE, copy1.getExpert());
      assertEquals(null, copy1.getExpertDescription());
      assertEquals(Boolean.FALSE, copy1.getLocked());
      assertEquals(EvalConstants.SHARING_PRIVATE, copy1.getSharing());

      // check the things that should match
      assertEquals(original.getIdeal(), copy1.getIdeal());
      assertEquals(original.getMode(), copy1.getMode());
      for (int i = 0; i < copy1.getOptions().length; i++) {
         assertEquals(original.getOptions()[i], copy1.getOptions()[i]);
      }


      // make sure title generation works
      scaleIds = new Long[] {etdl.scale1.getId()};
      copiedIds = authoringService.copyScales(scaleIds, "", EvalTestDataLoad.MAINT_USER_ID, false);
      assertNotNull(copiedIds);
      assertEquals(scaleIds.length, copiedIds.length);
      EvalScale copy2 = (EvalScale) evaluationDao.findById(EvalScale.class, copiedIds[0]);
      assertNotNull(copy2);
      assertNotNull(copy2.getTitle());

      // check we can copy a bunch of things
      scaleIds = new Long[] {etdl.scale2.getId(), etdl.scale3.getId(), etdl.scale4.getId()};
      copiedIds = authoringService.copyScales(scaleIds, null, EvalTestDataLoad.MAINT_USER_ID, false);
      assertNotNull(copiedIds);
      assertEquals(scaleIds.length, copiedIds.length);
      for (int i = 0; i < copiedIds.length; i++) {
         assertNotNull(evaluationDao.findById(EvalScale.class, copiedIds[i]));
      }

      // check that invalid scaleid causes death
      scaleIds = new Long[] {etdl.scale2.getId(), EvalTestDataLoad.INVALID_LONG_ID};
      try {
         copiedIds = authoringService.copyScales(scaleIds, null, EvalTestDataLoad.MAINT_USER_ID, false);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalAuthoringServiceImpl#copyItems(java.lang.Long[], java.lang.String, boolean, boolean)}.
    */
   public void testCopyItems() {
      Long[] copiedIds = null;
      Long[] itemIds = null;

      // copy a single item
      EvalItem original = etdl.item1;
      itemIds = new Long[] {original.getId()};
      copiedIds = authoringService.copyItems(itemIds, EvalTestDataLoad.MAINT_USER_ID, true, true);
      assertNotNull(copiedIds);
      assertEquals(itemIds.length, copiedIds.length);
      EvalItem copy1 = (EvalItem) evaluationDao.findById(EvalItem.class, copiedIds[0]);
      assertNotNull(copy1);

      // verify the copy worked
      // check the things that should differ
      assertNotSame(copy1.getId(), original.getId());
      assertEquals(copy1.getCopyOf(), original.getId());
      assertEquals(copy1.getOwner(), EvalTestDataLoad.MAINT_USER_ID);
      assertEquals(copy1.isHidden(), true);
      assertEquals(copy1.getExpert(), Boolean.FALSE);
      assertEquals(copy1.getExpertDescription(), null);
      assertEquals(copy1.getLocked(), Boolean.FALSE);
      assertEquals(copy1.getSharing(), EvalConstants.SHARING_PRIVATE);

      // check the things that should match
      assertEquals(copy1.getCategory(), original.getCategory());
      assertEquals(copy1.getClassification(), original.getClassification());
      assertEquals(copy1.getDescription(), original.getDescription());
      assertEquals(copy1.getDisplayRows(), original.getDisplayRows());
      assertEquals(copy1.getItemText(), original.getItemText());
      assertEquals(copy1.getScaleDisplaySetting(), original.getScaleDisplaySetting());
      assertEquals(copy1.getUsesNA(), original.getUsesNA());

      // check that the scale was copied correctly also
      assertNotNull(original.getScale());
      assertNotNull(copy1.getScale());
      assertNotSame(copy1.getScale().getId(), original.getScale().getId());
      for (int i = 0; i < copy1.getScale().getOptions().length; i++) {
         assertEquals(copy1.getScale().getOptions()[i], original.getScale().getOptions()[i]);
      }

      // now do a copy without children
      original = etdl.item1;
      itemIds = new Long[] {original.getId()};
      copiedIds = authoringService.copyItems(itemIds, EvalTestDataLoad.MAINT_USER_ID, true, false);
      assertNotNull(copiedIds);
      assertEquals(itemIds.length, copiedIds.length);
      EvalItem copy2 = (EvalItem) evaluationDao.findById(EvalItem.class, copiedIds[0]);
      assertNotNull(copy2);

      // verify the copy worked
      // check the things that should differ
      assertNotSame(copy2.getId(), original.getId());
      assertEquals(copy2.getCopyOf(), original.getId());
      assertEquals(copy2.getOwner(), EvalTestDataLoad.MAINT_USER_ID);
      assertEquals(copy2.isHidden(), true);
      assertEquals(copy2.getExpert(), Boolean.FALSE);
      assertEquals(copy2.getExpertDescription(), null);
      assertEquals(copy2.getLocked(), Boolean.FALSE);
      assertEquals(copy2.getSharing(), EvalConstants.SHARING_PRIVATE);

      // check the things that should match
      assertEquals(copy2.getCategory(), original.getCategory());
      assertEquals(copy2.getClassification(), original.getClassification());
      assertEquals(copy2.getDescription(), original.getDescription());
      assertEquals(copy2.getDisplayRows(), original.getDisplayRows());
      assertEquals(copy2.getItemText(), original.getItemText());
      assertEquals(copy2.getScaleDisplaySetting(), original.getScaleDisplaySetting());
      assertEquals(copy2.getUsesNA(), original.getUsesNA());

      // check that the scale was used but not copied
      assertNotNull(copy2.getScale());
      assertEquals(copy2.getScale().getId(), original.getScale().getId());



      // copy a single item (text item this time), true for child copy even though there are no children should be ok
      original = etdl.item5;
      itemIds = new Long[] {original.getId()};
      copiedIds = authoringService.copyItems(itemIds, EvalTestDataLoad.MAINT_USER_ID, true, true);
      assertNotNull(copiedIds);
      assertEquals(itemIds.length, copiedIds.length);
      EvalItem copy3 = (EvalItem) evaluationDao.findById(EvalItem.class, copiedIds[0]);
      assertNotNull(copy3);

      // verify the copy worked
      // check the things that should differ
      assertNotSame(copy3.getId(), original.getId());
      assertEquals(copy3.getCopyOf(), original.getId());
      assertEquals(copy3.getOwner(), EvalTestDataLoad.MAINT_USER_ID);
      assertEquals(copy3.isHidden(), true);
      assertEquals(copy3.getExpert(), Boolean.FALSE);
      assertEquals(copy3.getExpertDescription(), null);
      assertEquals(copy3.getLocked(), Boolean.FALSE);
      assertEquals(copy3.getSharing(), EvalConstants.SHARING_PRIVATE);

      // check the things that should match
      assertEquals(copy3.getCategory(), original.getCategory());
      assertEquals(copy3.getClassification(), original.getClassification());
      assertEquals(copy3.getDescription(), original.getDescription());
      assertEquals(copy3.getDisplayRows(), original.getDisplayRows());
      assertEquals(copy3.getItemText(), original.getItemText());
      assertEquals(copy3.getScaleDisplaySetting(), original.getScaleDisplaySetting());
      assertEquals(copy3.getUsesNA(), original.getUsesNA());

      // check we can copy a bunch of things
      itemIds = new Long[] {etdl.item2.getId(), etdl.item3.getId(), etdl.item4.getId()};
      copiedIds = authoringService.copyItems(itemIds, EvalTestDataLoad.MAINT_USER_ID, false, true);
      assertNotNull(copiedIds);
      assertEquals(itemIds.length, copiedIds.length);
      for (int i = 0; i < copiedIds.length; i++) {
         assertNotNull(evaluationDao.findById(EvalItem.class, copiedIds[i]));
      }

      // check we can copy a bunch of things (without children)
      itemIds = new Long[] {etdl.item2.getId(), etdl.item4.getId(), etdl.item6.getId()};
      copiedIds = authoringService.copyItems(itemIds, EvalTestDataLoad.MAINT_USER_ID, false, false);
      assertNotNull(copiedIds);
      assertEquals(itemIds.length, copiedIds.length);
      for (int i = 0; i < copiedIds.length; i++) {
         assertNotNull(evaluationDao.findById(EvalItem.class, copiedIds[i]));
      }

      // check that invalid itemId causes exception
      itemIds = new Long[] {etdl.item2.getId(), EvalTestDataLoad.INVALID_LONG_ID};
      try {
         copiedIds = authoringService.copyItems(itemIds, EvalTestDataLoad.MAINT_USER_ID, false, true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalAuthoringServiceImpl#copyTemplateItems(java.lang.Long[], java.lang.String, boolean, Long, boolean)}.
    */
   public void testCopyTemplateItems() {
      Long[] copiedIds = null;
      Long[] templateItemIds = null;

      // copy a single templateItem
      EvalTemplateItem original = etdl.templateItem1U;
      templateItemIds = new Long[] {original.getId()};
      copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, true, null, true);
      assertNotNull(copiedIds);
      assertEquals(templateItemIds.length, copiedIds.length);
      EvalTemplateItem copy1 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, copiedIds[0]);
      assertNotNull(copy1);

      // verify the copy worked
      // check the things that should differ
      assertNotSame(copy1.getId(), original.getId());
      assertEquals(original.getId(), copy1.getCopyOf());
      assertEquals(EvalTestDataLoad.MAINT_USER_ID, copy1.getOwner());
      assertEquals(true, copy1.isHidden());
      assertTrue(original.getDisplayOrder() < copy1.getDisplayOrder());

      // check the things that should match
      assertEquals(original.getBlockId(), copy1.getBlockId());
      assertEquals(original.getBlockParent(), copy1.getBlockParent());
      assertEquals(original.getCategory(), copy1.getCategory());
      assertEquals(original.getDisplayRows(), copy1.getDisplayRows());
      assertEquals(original.getHierarchyLevel(), copy1.getHierarchyLevel());
      assertEquals(original.getHierarchyNodeId(), copy1.getHierarchyNodeId());
      //assertEquals(original.getResultsSharing(), copy1.getResultsSharing()); // fixed up
      assertEquals(original.getScaleDisplaySetting(), copy1.getScaleDisplaySetting());
      assertEquals(original.getUsesNA(), copy1.getUsesNA());

      assertEquals(original.getTemplate().getId(), copy1.getTemplate().getId());

      // check that the item was copied correctly also
      assertNotNull(copy1.getItem());
      assertNotNull(original.getItem());
      assertNotSame(original.getItem().getId(), copy1.getItem().getId());
      assertEquals(original.getItem().getItemText(), copy1.getItem().getItemText());

      // now do a copy without children
      original = etdl.templateItem1P;
      templateItemIds = new Long[] {original.getId()};
      copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, true, null, false);
      assertNotNull(copiedIds);
      assertEquals(templateItemIds.length, copiedIds.length);
      EvalTemplateItem copy2 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, copiedIds[0]);
      assertNotNull(copy2);

      // verify the copy worked
      // check the things that should differ
      assertNotSame(copy2.getId(), original.getId());
      assertEquals(copy2.getCopyOf(), original.getId());
      assertEquals(copy2.getOwner(), EvalTestDataLoad.MAINT_USER_ID);
      assertEquals(copy2.isHidden(), true);
      assertTrue(original.getDisplayOrder() < copy2.getDisplayOrder());

      // check the things that should match
      assertEquals(copy2.getBlockId(), original.getBlockId());
      assertEquals(copy2.getBlockParent(), original.getBlockParent());
      assertEquals(copy2.getCategory(), original.getCategory());
      assertEquals(copy2.getDisplayRows(), original.getDisplayRows());
      assertEquals(copy2.getHierarchyLevel(), original.getHierarchyLevel());
      assertEquals(copy2.getHierarchyNodeId(), original.getHierarchyNodeId());
      assertEquals(copy2.getScaleDisplaySetting(), original.getScaleDisplaySetting());
      assertEquals(copy2.getUsesNA(), original.getUsesNA());

      assertEquals(copy2.getTemplate().getId(), original.getTemplate().getId());

      // check that the item was used but not copied
      assertNotNull(copy2.getItem());
      assertEquals(copy2.getItem().getId(), original.getItem().getId());


      // only 1 countable item in the block template
      assertEquals( 1, authoringService.getItemCountForTemplate(etdl.templateAdminBlock.getId()) );

      // test out copying a complete block
      templateItemIds = new Long[] {etdl.templateItem9B.getId(), etdl.templateItem2B.getId(), etdl.templateItem3B.getId()};
      copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, true, null, false);
      assertNotNull(copiedIds);
      assertEquals(templateItemIds.length, copiedIds.length);
      List<EvalTemplateItem> templateItems = evaluationDao.findBySearch(EvalTemplateItem.class, new Search("id", copiedIds) );
      assertNotNull(templateItems);
      assertEquals(templateItemIds.length, templateItems.size());

      // verify the copy worked
      Long blockParentId = null;
      for (EvalTemplateItem templateItem : templateItems) {
         assertNotNull(templateItem.getBlockParent());
         if (templateItem.getBlockParent()) {
            assertNull(templateItem.getBlockId());
            blockParentId = templateItem.getId();
         } else {
            assertNotNull(templateItem.getBlockId());            
         }
      }

      for (EvalTemplateItem templateItem : templateItems) {
         assertEquals(templateItem.getOwner(), EvalTestDataLoad.MAINT_USER_ID);
         assertEquals(templateItem.isHidden(), true);
         assertEquals(copy2.getTemplate().getId(), original.getTemplate().getId());
         if (templateItem.getBlockParent()) {
            // check the things that should differ
            assertNotSame(templateItem.getId(), etdl.templateItem9B.getId());
            assertEquals(templateItem.getCopyOf(), etdl.templateItem9B.getId());
         } else {
            // check the block is assigned correctly
            assertEquals(blockParentId, templateItem.getBlockId());
         }
      }

      // now 2 countable items in the block template
      assertEquals( 2, authoringService.getItemCountForTemplate(etdl.templateAdminBlock.getId()) );


      // now copy over to a new template
      original = etdl.templateItem1P;
      templateItemIds = new Long[] {original.getId()};
      copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, true, etdl.templateAdmin.getId(), false);
      assertNotNull(copiedIds);
      assertEquals(templateItemIds.length, copiedIds.length);
      EvalTemplateItem copy3 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, copiedIds[0]);
      assertNotNull(copy3);

      // check the template
      assertNotSame(copy3.getTemplate().getId(), original.getTemplate().getId());
      assertEquals(etdl.templateAdmin.getId(), copy3.getTemplate().getId());


      // check we can copy a bunch of things
      templateItemIds = new Long[] {etdl.templateItem2A.getId(), etdl.templateItem3A.getId(), etdl.templateItem5A.getId()};
      copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, false, null, true);
      assertNotNull(copiedIds);
      assertEquals(templateItemIds.length, copiedIds.length);
      for (int i = 0; i < copiedIds.length; i++) {
         assertNotNull(evaluationDao.findById(EvalTemplateItem.class, copiedIds[i]));
      }

      assertEquals( 2, authoringService.getItemCountForTemplate(etdl.templateUser.getId()) );

      // check we can copy a bunch of things (without children) into the same template
      templateItemIds = new Long[] {etdl.templateItem1User.getId(), etdl.templateItem5User.getId()};
      copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, false, null, false);
      assertNotNull(copiedIds);
      assertEquals(templateItemIds.length, copiedIds.length);
      for (int i = 0; i < copiedIds.length; i++) {
         assertNotNull(evaluationDao.findById(EvalTemplateItem.class, copiedIds[i]));
      }

      assertEquals( 4, authoringService.getItemCountForTemplate(etdl.templateUser.getId()) );

      // check that trying to do an inside copy of TIs from multiple templates causes failure
      templateItemIds = new Long[] {etdl.templateItem1P.getId(), etdl.templateItem2A.getId()};
      try {
         copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, false, null, true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }

      // check that invalid templateItemId causes exception
      templateItemIds = new Long[] {etdl.templateItem2A.getId(), EvalTestDataLoad.INVALID_LONG_ID};
      try {
         copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, false, null, true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalAuthoringServiceImpl#copyTemplate(java.lang.Long, java.lang.String, java.lang.String, boolean, boolean)}.
    */
   public void testCopyTemplate() {
      Long copiedId = null;

      // copy a single template with all children
      // (should create duplicates of all the items and scales)
      EvalTemplate original = etdl.templateUser;
      copiedId = authoringService.copyTemplate(original.getId(), "copy templateUser", EvalTestDataLoad.MAINT_USER_ID, true, true);
      assertNotNull(copiedId);
      EvalTemplate copy1 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, copiedId);
      assertNotNull(copy1);

      // verify the copy worked
      // check the things that should differ
      assertNotSame(original.getId(), copy1.getId());
      assertEquals(original.getId(), copy1.getCopyOf());
      assertEquals("copy templateUser", copy1.getTitle());
      assertEquals(EvalTestDataLoad.MAINT_USER_ID, copy1.getOwner());
      assertEquals(true, copy1.isHidden());
      assertEquals(Boolean.FALSE, copy1.getExpert());
      assertEquals(null, copy1.getExpertDescription());
      assertEquals(Boolean.FALSE, copy1.getLocked());
      assertEquals(EvalConstants.SHARING_PRIVATE, copy1.getSharing());

      // check the things that should match
      assertEquals(original.getDescription(), copy1.getDescription());
      assertEquals(original.getType(), copy1.getType());

      // make sure the template items copied
      assertEquals(original.getTemplateItems().size(), copy1.getTemplateItems().size());
      List<EvalTemplateItem> originalTIs = TemplateItemUtils.makeTemplateItemsList(original.getTemplateItems());
      List<EvalTemplateItem> copyTIs = TemplateItemUtils.makeTemplateItemsList(copy1.getTemplateItems());
      assertEquals(original.getTemplateItems().size(), originalTIs.size());
      assertEquals(originalTIs.size(), copyTIs.size());
      for (int i = 0; i < originalTIs.size(); i++) {
         EvalTemplateItem originalTI = originalTIs.get(i);
         EvalTemplateItem copyTI = copyTIs.get(i);
         assertNotSame(originalTI.getId(), copyTI.getId());
         assertEquals(originalTI.getDisplayOrder(), copyTI.getDisplayOrder());
         // now check the item underneath is a copy of the same item
         assertEquals(originalTI.getItem().getId(), copyTI.getItem().getCopyOf());
      }

      // test copying without children (all TIs have to be copies as they cannot be shared but the things underneath should not copy)
      original = etdl.templatePublic;
      copiedId = authoringService.copyTemplate(original.getId(), "", EvalTestDataLoad.MAINT_USER_ID, true, false);
      assertNotNull(copiedId);
      EvalTemplate copy2 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, copiedId);
      assertNotNull(copy2);

      // verify the copy worked
      // check the things that should differ
      assertNotSame(original.getId(), copy2.getId());
      assertEquals(original.getId(), copy2.getCopyOf());
      assertNotSame(original.getTitle(), copy2.getTitle());
      assertEquals(EvalTestDataLoad.MAINT_USER_ID, copy2.getOwner());
      assertEquals(true, copy2.isHidden());
      assertEquals(Boolean.FALSE, copy2.getExpert());
      assertEquals(null, copy2.getExpertDescription());
      assertEquals(Boolean.FALSE, copy2.getLocked());
      assertEquals(EvalConstants.SHARING_PRIVATE, copy2.getSharing());

      // check the things that should match
      assertEquals(original.getDescription(), copy2.getDescription());
      assertEquals(original.getType(), copy2.getType());

      // make sure the template items copied
      assertEquals(original.getTemplateItems().size(), copy2.getTemplateItems().size());
      originalTIs = TemplateItemUtils.makeTemplateItemsList(original.getTemplateItems());
      copyTIs = TemplateItemUtils.makeTemplateItemsList(copy2.getTemplateItems());
      assertEquals(original.getTemplateItems().size(), originalTIs.size());
      assertEquals(originalTIs.size(), copyTIs.size());
      for (int i = 0; i < originalTIs.size(); i++) {
         EvalTemplateItem originalTI = originalTIs.get(i);
         EvalTemplateItem copyTI = copyTIs.get(i);
         assertNotSame(originalTI.getId(), copyTI.getId());
         assertEquals(originalTI.getDisplayOrder(), copyTI.getDisplayOrder());
         // now check the item underneath is the same item (and not a copy)
         assertEquals(originalTI.getItem().getId(), copyTI.getItem().getId());
      }

      // make sure title generation works
      original = etdl.templateUnused;
      copiedId = authoringService.copyTemplate(original.getId(), "", EvalTestDataLoad.MAINT_USER_ID, true, true);
      assertNotNull(copiedId);
      EvalTemplate copy3 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, copiedId);
      assertNotNull(copy3);
      assertNotNull(copy3.getTitle());

      // check that invalid templateid causes death
      try {
         copiedId = authoringService.copyTemplate(EvalTestDataLoad.INVALID_LONG_ID, null, EvalTestDataLoad.MAINT_USER_ID, true, true);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }



   public void testGetItemsUsingScale() {
      List<EvalItem> items = null;

      items = authoringService.getItemsUsingScale(etdl.scale1.getId());
      assertNotNull(items);
      assertEquals(5, items.size());

      items = authoringService.getItemsUsingScale(etdl.scale4.getId());
      assertNotNull(items);
      assertEquals(0, items.size());

      // check that invalid id causes death
      try {
         items = authoringService.getItemsUsingScale(EvalTestDataLoad.INVALID_LONG_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }

   public void testGetTemplatesUsingItem() {
      List<EvalTemplate> templates = null;

      templates = authoringService.getTemplatesUsingItem(etdl.item1.getId());
      assertNotNull(templates);
      assertEquals(3, templates.size());

      templates = authoringService.getTemplatesUsingItem(etdl.item11.getId());
      assertNotNull(templates);
      assertEquals(0, templates.size());

      // check that invalid id causes death
      try {
         templates = authoringService.getTemplatesUsingItem(EvalTestDataLoad.INVALID_LONG_ID);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
      }
   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalAuthoringServiceImpl#getAutoUseTemplateItems(java.lang.String)}.
    */
   public void testGetAutoUseTemplateItems() {
      List<EvalTemplateItem> items = null;
      List<Long> ids = null;

      // positive tests
      items = authoringService.getAutoUseTemplateItems(EvalTestDataLoad.AUTO_USE_TAG, null, null);
      assertNotNull(items);
      assertEquals(3, items.size());
      ids = EvalTestDataLoad.makeIdList(items);
      assertEquals(etdl.templateItem1U.getId(), ids.get(0));
      assertEquals(etdl.templateItem3U.getId(), ids.get(1));
      assertEquals(etdl.templateItem5U.getId(), ids.get(2));

      items = authoringService.getAutoUseTemplateItems(null, EvalTestDataLoad.AUTO_USE_TAG, null);
      assertNotNull(items);
      assertEquals(2, items.size());
      ids = EvalTestDataLoad.makeIdList(items);
      assertEquals(etdl.templateItem2A.getId(), ids.get(0));
      assertEquals(etdl.templateItem6UU.getId(), ids.get(1));

      items = authoringService.getAutoUseTemplateItems(null, null, EvalTestDataLoad.AUTO_USE_TAG);
      assertNotNull(items);
      assertEquals(1, items.size());
      assertEquals(etdl.item4.getId(), items.get(0).getItem().getId());

      items = authoringService.getAutoUseTemplateItems(EvalTestDataLoad.AUTO_USE_TAG, EvalTestDataLoad.AUTO_USE_TAG, EvalTestDataLoad.AUTO_USE_TAG);
      assertNotNull(items);
      ids = EvalTestDataLoad.makeIdList(items);
      assertEquals(6, items.size());
      assertEquals(etdl.templateItem1U.getId(), ids.get(0));
      assertEquals(etdl.templateItem3U.getId(), ids.get(1));
      assertEquals(etdl.templateItem5U.getId(), ids.get(2));
      assertEquals(etdl.templateItem2A.getId(), ids.get(3));
      assertEquals(etdl.templateItem6UU.getId(), ids.get(4));
      assertNull(items.get(5).getId());


      // negative tests
      items = authoringService.getAutoUseTemplateItems(null, null, null);
      assertNotNull(items);
      assertEquals(0, items.size());

      items = authoringService.getAutoUseTemplateItems("UNKNOWN", null, null);
      assertNotNull(items);
      assertEquals(0, items.size());

      items = authoringService.getAutoUseTemplateItems("UNKNOWN", "UNKNOWN", "UNKNOWN");
      assertNotNull(items);
      assertEquals(0, items.size());

      // no exceptions thrown
   }

   public void testDoAutoUseInsertion() {
      List<EvalTemplateItem> items = null;
      List<EvalTemplateItem> currentItems = null;
      Long templateId = null;
      int displayOrder = 0;

      // check out the template first
      templateId = etdl.templateUser.getId();
      authoringService.getTemplateById(templateId).setLocked(false); // make this unlocked
      currentItems = authoringService.getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {});
      assertEquals(2, currentItems.size());

      // test insertion without save
      items = authoringService.doAutoUseInsertion(EvalTestDataLoad.AUTO_USE_TAG, templateId, EvalConstants.EVALUATION_AUTOUSE_INSERTION_AFTER, false);
      assertNotNull(items);
      assertEquals(8, items.size()); // + 6 autoUse items
      // check the order
      displayOrder = 1;
      for (EvalTemplateItem item : items) {
         if (displayOrder >= 3) {
            assertEquals(EvalTestDataLoad.AUTO_USE_TAG, item.getAutoUseInsertionTag());
         } else {
            assertNull(item.getAutoUseInsertionTag());
         }
         assertEquals(new Integer(displayOrder++), item.getDisplayOrder());
      }

      currentItems = authoringService.getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {});
      assertEquals(2, currentItems.size());

      // test insertion without save in different order
      items = authoringService.doAutoUseInsertion(EvalTestDataLoad.AUTO_USE_TAG, templateId, EvalConstants.EVALUATION_AUTOUSE_INSERTION_BEFORE, false);
      assertNotNull(items);
      assertEquals(8, items.size()); // + 6 autoUse items
      // check the order
      displayOrder = 1;
      for (EvalTemplateItem item : items) {
         // check for autoUse tag
         if (displayOrder <= 6) {
            assertEquals(EvalTestDataLoad.AUTO_USE_TAG, item.getAutoUseInsertionTag());
         } else {
            assertNull(item.getAutoUseInsertionTag());
         }
         assertEquals(new Integer(displayOrder++), item.getDisplayOrder());
      }

      // test insertion with save
      items = authoringService.doAutoUseInsertion(EvalTestDataLoad.AUTO_USE_TAG, templateId, EvalConstants.EVALUATION_AUTOUSE_INSERTION_BEFORE, true);
      assertNotNull(items);
      assertEquals(8, items.size()); // + 6 autoUse items
      displayOrder = 1;
      for (EvalTemplateItem item : items) {
         if (displayOrder <= 6) {
            assertEquals(EvalTestDataLoad.AUTO_USE_TAG, item.getAutoUseInsertionTag());
         } else {
            assertNull(item.getAutoUseInsertionTag());
         }
         assertEquals(new Integer(displayOrder++), item.getDisplayOrder());
      }

      currentItems = authoringService.getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {});
      assertEquals(8, currentItems.size());
      currentItems = TemplateItemUtils.orderTemplateItems(currentItems, false);
      for (int i = 0; i < items.size(); i++) {
         assertEquals(currentItems.get(i).getId(), items.get(i).getId());
      }      

      // test nothing to insert
      items = authoringService.doAutoUseInsertion("FAKE", templateId, EvalConstants.EVALUATION_AUTOUSE_INSERTION_AFTER, false);
      assertNull(items);

   }

}
