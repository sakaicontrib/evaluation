/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

    private static final Log LOG = LogFactory.getLog( EvalAuthoringServiceImplTest.class );

   protected EvalAuthoringServiceImpl authoringService;

   // run this before each test starts
   @Before
   public void onSetUpBeforeTransaction() throws Exception {
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

   @Test
   public void testPreloadedItemGroupsData() {
      // check the full count of preloaded items
      Assert.assertEquals(18, evaluationDao.countAll(EvalItemGroup.class) );

      Assert.assertEquals(12, evaluationDao.countAll(EvalTemplate.class) );
      List<EvalTemplate> templates1 = evaluationDao.findAll(EvalTemplate.class);
      Assert.assertEquals(12, templates1.size());
   }

   @Test
   public void testPreloadedItemData() {
      // this test is just making sure that we are actually linking the items
      // to the templates the way we think we are
      List<Long> ids;

      Assert.assertEquals(46, evaluationDao.countAll(EvalItem.class) );

      // check the full count of preloaded items
      Assert.assertEquals(20, evaluationDao.countAll(EvalTemplateItem.class) );

      EvalTemplate template = (EvalTemplate) 
      evaluationDao.findById(EvalTemplate.class, etdl.templateAdmin.getId());

      // No longer supporting this type of linkage between templates and items
//    Set items = template.getItems();
//    Assert.assertNotNull( items );
//    Assert.assertEquals(3, authoringService.size());

      Set<EvalTemplateItem> tItems = template.getTemplateItems();
      Assert.assertNotNull( tItems );
      Assert.assertEquals(3, tItems.size());
      ids = EvalTestDataLoad.makeIdList(tItems);
      Assert.assertTrue(ids.contains( etdl.templateItem2A.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem3A.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem5A.getId() ));
      // get the items from the templateItems
      List<Long> l = new ArrayList<>();
      for( EvalTemplateItem eti : tItems ) {
           Assert.assertTrue( eti.getItem() instanceof EvalItem );
           Assert.assertEquals(eti.getTemplate().getId(), template.getId());
           l.add(eti.getItem().getId());
       }
      Assert.assertTrue(l.contains( etdl.item2.getId() ));
      Assert.assertTrue(l.contains( etdl.item3.getId() ));
      Assert.assertTrue(l.contains( etdl.item5.getId() ));

      // test getting another set of items
      EvalItem item = (EvalItem) evaluationDao.findById(EvalItem.class, etdl.item1.getId());
      Set<EvalTemplateItem> itItems = item.getTemplateItems();
      Assert.assertNotNull( itItems );
      Assert.assertEquals(3, itItems.size());
      ids = EvalTestDataLoad.makeIdList(itItems);
      Assert.assertTrue(ids.contains( etdl.templateItem1P.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem1U.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem1User.getId() ));

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#getScaleById(java.lang.Long)}.
    */
   @Test
   public void testGetScaleById() {
      EvalScale scale;

      scale = authoringService.getScaleById( etdl.scale1.getId() );
      Assert.assertNotNull(scale);
      Assert.assertEquals(etdl.scale1.getId(), scale.getId());

      scale = authoringService.getScaleById( etdl.scale2.getId() );
      Assert.assertNotNull(scale);
      Assert.assertEquals(etdl.scale2.getId(), scale.getId());

      // test get eval by invalid id
      scale = authoringService.getScaleById( EvalTestDataLoad.INVALID_LONG_ID );
      Assert.assertNull(scale);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#getScaleByEid(java.lang.String)}.
    */
   @Test
   public void testGetScaleByEid() {
      EvalScale scale;

      //test getting scale having eid set
      scale = authoringService.getScaleByEid( etdl.scaleEid.getEid() );
      Assert.assertNotNull(scale);
      Assert.assertEquals(etdl.scaleEid.getEid(), scale.getEid());

      //test getting scale not having eid set returns null
      scale = authoringService.getScaleByEid( etdl.scale2.getEid() );
      Assert.assertNull(scale);

      // test getting scale by invalid eid returns null
      scale = authoringService.getScaleByEid( EvalTestDataLoad.INVALID_STRING_EID );
      Assert.assertNull(scale);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#saveScale(org.sakaiproject.evaluation.model.EvalScale, java.lang.String)}.
    */
   @Test
   public void testSaveScale() {
      String[] options1 = {"Bad", "Average", "Good"};
      String[] options2 = {"Bad", "Average", "Good"};
      String[] options3 = {"Bad", "Average", "Good"};
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

      Assert.assertEquals(4, testScale2.getOptions().length);
      testScale2.setOptions(options2);
      authoringService.saveScale(testScale2, EvalTestDataLoad.MAINT_USER_ID);
      Assert.assertEquals(3, testScale2.getOptions().length);

      // test admin can edit any scale
      testScale2.setIdeal(EvalConstants.SCALE_IDEAL_MID);
      authoringService.saveScale(testScale2, EvalTestDataLoad.ADMIN_USER_ID);

      // test that editing unowned scale causes permission Assert.failure
      try {
         testScale3.setIdeal(EvalConstants.SCALE_IDEAL_MID);
         authoringService.saveScale(testScale4, EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
      }

      // TODO - CANNOT RUN THIS TEST BECAUSE OF HIBERNATE ISSUE
//    // test that LOCKED cannot be changed to FALSE on existing scales
//    try {
//    testScale1.setLocked(Boolean.FALSE);
//    authoringService.saveScale(testScale1, EvalTestDataLoad.ADMIN_USER_ID);
//    Assert.fail("Should have thrown exception");
//    } catch (IllegalArgumentException e) {
//    Assert.assertNotNull(e);
//    Assert.fail(e.getMessage());
//    }

      // test editing LOCKED scale Assert.fails
      try {
         testScale1.setSharing(EvalConstants.SHARING_PRIVATE);
         authoringService.saveScale(testScale1, EvalTestDataLoad.ADMIN_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      // test that setting sharing to PUBLIC as non-admin Assert.fails
      try {
         testScale2.setSharing(EvalConstants.SHARING_PUBLIC);
         authoringService.saveScale(testScale2, EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // test admin can set scales to public sharing
      testScale2.setSharing(EvalConstants.SHARING_PUBLIC);
      authoringService.saveScale(testScale2, EvalTestDataLoad.ADMIN_USER_ID);

      // test Assert.fails to save scale with null options
      try {
         authoringService.saveScale( new EvalScale( EvalTestDataLoad.MAINT_USER_ID, 
               "options are null", EvalConstants.SCALE_MODE_SCALE, 
               EvalConstants.SHARING_PRIVATE, Boolean.FALSE, "description", 
               EvalConstants.SCALE_IDEAL_LOW, null,
               EvalTestDataLoad.UNLOCKED), EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // test CAN save scale with duplicate title
      authoringService.saveScale( new EvalScale( EvalTestDataLoad.MAINT_USER_ID, 
            test_title, EvalConstants.SCALE_MODE_SCALE, 
            EvalConstants.SHARING_PRIVATE, Boolean.FALSE, "description", 
            EvalConstants.SCALE_IDEAL_LOW, options3,
            EvalTestDataLoad.UNLOCKED), EvalTestDataLoad.MAINT_USER_ID);

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#deleteScale(java.lang.Long, java.lang.String)}.
    */
   @Test
   public void testDeleteScale() {
      // test removing unowned scale Assert.fails
      try {
         authoringService.deleteScale(etdl.scale4.getId(), EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (RuntimeException e) {
         Assert.assertNotNull(e);
      }

      // test removing owned scale works
      authoringService.deleteScale(etdl.scale3.getId(), EvalTestDataLoad.MAINT_USER_ID);

      // test removing expert scale allowed
      authoringService.deleteScale(etdl.scale4.getId(), EvalTestDataLoad.ADMIN_USER_ID);

      // test removing locked scale Assert.fails
      try {
         authoringService.deleteScale(etdl.scale1.getId(), EvalTestDataLoad.ADMIN_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (RuntimeException e) {
         Assert.assertNotNull(e);
      }

      // test invalid scale id Assert.fails
      try {
         authoringService.deleteScale(EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (RuntimeException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#getScalesForUser(java.lang.String, java.lang.String)}.
    */
   @Test
   public void testGetScalesForUser() {
      List<EvalScale> l;
      List<Long> ids;
      // NOTE: 15 preloaded public scales to take into account currently
      int preloadedCount = 15;

      // get all visible scales (admin should see all)
      l = authoringService.getScalesForUser(EvalTestDataLoad.ADMIN_USER_ID, null);
      Assert.assertNotNull(l);
      Assert.assertEquals(5 + preloadedCount, l.size()); // include 15 preloaded
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.scale1.getId() ));
      Assert.assertTrue(ids.contains( etdl.scale2.getId() ));
      Assert.assertTrue(ids.contains( etdl.scale3.getId() ));

      Assert.assertTrue(ids.contains( etdl.scaleEid.getId() ));

      // get all visible scales (include maint owned and public)
      l = authoringService.getScalesForUser(EvalTestDataLoad.MAINT_USER_ID, null);
      Assert.assertNotNull(l);
      Assert.assertEquals(4 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.scale1.getId() ));
      Assert.assertTrue(ids.contains( etdl.scale2.getId() ));
      Assert.assertTrue(ids.contains( etdl.scale3.getId() ));
      Assert.assertTrue(! ids.contains( etdl.scale4.getId() ));

      Assert.assertTrue(ids.contains( etdl.scaleEid.getId() ));

      // get all visible scales (should only see public)
      l = authoringService.getScalesForUser(EvalTestDataLoad.USER_ID, null);
      Assert.assertNotNull(l);
      Assert.assertEquals(2 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.scale1.getId() ));
      Assert.assertTrue(! ids.contains( etdl.scale2.getId() ));
      Assert.assertTrue(! ids.contains( etdl.scale3.getId() ));

      Assert.assertTrue(ids.contains( etdl.scaleEid.getId() ));

      // attempt to get SHARING_OWNER scales (returns same as null)
      l = authoringService.getScalesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_OWNER);
      Assert.assertNotNull(l);
      Assert.assertEquals(5 + preloadedCount, l.size());

      // get all private scales (admin should see all private)
      l = authoringService.getScalesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_PRIVATE);
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
      l = authoringService.getScalesForUser(EvalTestDataLoad.MAINT_USER_ID, EvalConstants.SHARING_PRIVATE);
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.scale2.getId() ));
      Assert.assertTrue(ids.contains( etdl.scale3.getId() ));

      // get all private scales (normal user should see none)
      l = authoringService.getScalesForUser(EvalTestDataLoad.USER_ID, EvalConstants.SHARING_PRIVATE);
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      // get all public scales (normal user should see all)
      l = authoringService.getScalesForUser(EvalTestDataLoad.USER_ID, EvalConstants.SHARING_PUBLIC);
      Assert.assertNotNull(l);
      Assert.assertEquals(2 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.scale1.getId() ));
      Assert.assertTrue(! ids.contains( etdl.scale2.getId() ));
      Assert.assertTrue(! ids.contains( etdl.scale3.getId() ));

      Assert.assertTrue(ids.contains( etdl.scaleEid.getId() ));

      // test getting invalid constant causes Assert.failure
      try {
         authoringService.getScalesForUser(EvalTestDataLoad.USER_ID, EvalTestDataLoad.INVALID_CONSTANT_STRING);
         Assert.fail("Should have thrown exception");
      } catch (RuntimeException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#canModifyScale(String, Long)}
    */
   @Test
   public void testCanModifyScale() {
      // test can modify owned scale
      Assert.assertTrue( authoringService.canModifyScale(
            EvalTestDataLoad.MAINT_USER_ID,  etdl.scale3.getId()) );
      Assert.assertTrue( authoringService.canModifyScale(
            EvalTestDataLoad.ADMIN_USER_ID,  etdl.scale4.getId()) );

      // test can modify used scale
      Assert.assertTrue( authoringService.canModifyScale(
            EvalTestDataLoad.MAINT_USER_ID,  etdl.scale2.getId()) );

      // test admin user can override perms
      Assert.assertTrue( authoringService.canModifyScale(
            EvalTestDataLoad.ADMIN_USER_ID,  etdl.scale3.getId()) );

      // test cannot control unowned scale
      Assert.assertFalse( authoringService.canModifyScale(
            EvalTestDataLoad.MAINT_USER_ID,  etdl.scale4.getId()) );
      Assert.assertFalse( authoringService.canModifyScale(
            EvalTestDataLoad.USER_ID, etdl.scale3.getId()) );

      // test cannot modify locked scale
      Assert.assertFalse( authoringService.canModifyScale(
            EvalTestDataLoad.ADMIN_USER_ID,  etdl.scale1.getId()) );

      // test invalid scale id causes Assert.failure
      try {
         authoringService.canModifyScale(EvalTestDataLoad.ADMIN_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID);
         Assert.fail("Should have thrown exception");
      } catch (RuntimeException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalScalesLogicImpl#canRemoveScale(String, Long)}
    */
   @Test
   public void testCanRemoveScale() {
      // test can remove owned scale
      Assert.assertTrue( authoringService.canRemoveScale(
            EvalTestDataLoad.ADMIN_USER_ID,  etdl.scale4.getId()) );
      Assert.assertTrue( authoringService.canRemoveScale(
            EvalTestDataLoad.MAINT_USER_ID,  etdl.scale3.getId()) );

      // test cannot remove unowned scale
      Assert.assertFalse( authoringService.canRemoveScale(
            EvalTestDataLoad.MAINT_USER_ID,  etdl.scale4.getId()) );
      Assert.assertFalse( authoringService.canRemoveScale(
            EvalTestDataLoad.USER_ID, etdl.scale3.getId()) );

      // can remove items that are in use now
//      // test cannot remove unlocked used scale
//      Assert.assertFalse( authoringService.canRemoveScale(
//            EvalTestDataLoad.MAINT_USER_ID,  etdl.scale2.getId()) );
//
//      // test admin cannot remove unlocked used scale
//      Assert.assertFalse( authoringService.canRemoveScale(
//            EvalTestDataLoad.ADMIN_USER_ID,  etdl.scale2.getId()) );

      // test cannot remove locked scale
      Assert.assertFalse( authoringService.canRemoveScale(
            EvalTestDataLoad.ADMIN_USER_ID,  etdl.scale1.getId()) );

      // test invalid scale id causes Assert.failure
      try {
         authoringService.canRemoveScale(EvalTestDataLoad.ADMIN_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID);
         Assert.fail("Should have thrown exception");
      } catch (RuntimeException e) {
         Assert.assertNotNull(e);
      }

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemById(java.lang.Long)}.
    */
   @Test
   public void testGetItemById() {
      EvalItem item;

      // test getting valid items by id
      item = authoringService.getItemById( etdl.item1.getId() );
      Assert.assertNotNull(item);
      Assert.assertEquals(etdl.item1.getId(), item.getId());

      item = authoringService.getItemById( etdl.item5.getId() );
      Assert.assertNotNull(item);
      Assert.assertEquals(etdl.item5.getId(), item.getId());

      // test get eval by invalid id returns null
      item = authoringService.getItemById( EvalTestDataLoad.INVALID_LONG_ID );
      Assert.assertNull(item);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemByEid(java.lang.String)}.
    */
   @Test
   public void testGetItemByEid() {
      EvalItem item;

      // test getting valid items having eid set
      item = authoringService.getItemByEid( etdl.item1Eid.getEid() );
      Assert.assertNotNull(item);
      Assert.assertEquals(etdl.item1Eid.getEid(), item.getEid());

      item = authoringService.getItemByEid( etdl.item2Eid.getEid() );
      Assert.assertNotNull(item);
      Assert.assertEquals(etdl.item2Eid.getEid(), item.getEid());

      item = authoringService.getItemByEid( etdl.item3Eid.getEid() );
      Assert.assertNotNull(item);
      Assert.assertEquals(etdl.item3Eid.getEid(), item.getEid());

      //test getting valid item not having eid set returns null
      item = authoringService.getItemByEid( etdl.item5.getEid() );
      Assert.assertNull(item);

      // test getting item by invalid id returns null
      item = authoringService.getItemByEid( EvalTestDataLoad.INVALID_STRING_EID );
      Assert.assertNull(item);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#saveItem(org.sakaiproject.evaluation.model.EvalItem, java.lang.String)}.
    */
   @Test
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
            false, false, 2, 
            null, EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.LOCKED), 
            EvalTestDataLoad.MAINT_USER_ID);

      // test saving valid item with no date, NA, and lock specified ok
      EvalItem eiTest1 = new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
            test_text, test_desc, EvalConstants.SHARING_PRIVATE, 
            EvalConstants.ITEM_TYPE_TEXT, EvalTestDataLoad.NOT_EXPERT, 
            "expert desc", null, null, null,
            false, false, 2, 
            null, EvalConstants.ITEM_CATEGORY_COURSE, null);
      authoringService.saveItem( eiTest1, 
            EvalTestDataLoad.MAINT_USER_ID);
      // make sure the values are filled in for us
      Assert.assertNotNull( eiTest1.getLastModified() );
      Assert.assertNotNull( eiTest1.getLocked() );
      Assert.assertNotNull( eiTest1.getUsesNA() );

      // test saving scaled item with no scale Assert.fails
      try {
         authoringService.saveItem( new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
               test_text, EvalConstants.SHARING_PRIVATE, 
               EvalConstants.ITEM_TYPE_SCALED, EvalTestDataLoad.NOT_EXPERT), 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // test saving scaled item with scale set AND text size Assert.fails
      try {
         authoringService.saveItem( new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
               test_text, test_desc, EvalConstants.SHARING_PRIVATE, 
               EvalConstants.ITEM_TYPE_SCALED, EvalTestDataLoad.NOT_EXPERT, 
               "expert desc", etdl.scale2, null, Boolean.FALSE,
               false, false, 3, 
               EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.UNLOCKED),
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // test saving text item with no text size Assert.fails
      try {
         authoringService.saveItem( new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
               test_text, EvalConstants.SHARING_PRIVATE, 
               EvalConstants.ITEM_TYPE_TEXT, EvalTestDataLoad.NOT_EXPERT), 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // test saving text item with scale set Assert.fails
      try {
         authoringService.saveItem( new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
               test_text, test_desc, EvalConstants.SHARING_PRIVATE, 
               EvalConstants.ITEM_TYPE_TEXT, EvalTestDataLoad.NOT_EXPERT, 
               "expert desc", etdl.scale2, null, Boolean.FALSE,
               false, false, null, 
               EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.UNLOCKED),
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // test saving header type item with scale or text size set Assert.fails
      try {
         authoringService.saveItem( new EvalItem( EvalTestDataLoad.MAINT_USER_ID, 
               test_text, test_desc, EvalConstants.SHARING_PRIVATE, 
               EvalConstants.ITEM_TYPE_HEADER, EvalTestDataLoad.NOT_EXPERT, 
               "expert desc", etdl.scale2, null, Boolean.FALSE,
               false, false, 3, 
               EvalConstants.ITEM_SCALE_DISPLAY_COMPACT, EvalConstants.ITEM_CATEGORY_COURSE, EvalTestDataLoad.UNLOCKED),
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
      authoringService.saveItem( testItem1, 
            EvalTestDataLoad.MAINT_USER_ID);

      // TODO - CANNOT RUN THIS TEST FOR NOW BECAUSE OF HIBERNATE
//    // test that LOCKED cannot be changed to FALSE on existing item
//    try {
//    testItem3.setLocked(Boolean.FALSE);
//    authoringService.saveItem( testItem3, 
//    EvalTestDataLoad.ADMIN_USER_ID);
//    Assert.fail("Should have thrown exception");
//    } catch (RuntimeException e) {
//    Assert.assertNotNull(e);
//    Assert.fail(e.getMessage()); // see why Assert.failing
//    }

      // test editing LOCKED item Assert.fails
      try {
         testItem4.setExpert(Boolean.FALSE);
         authoringService.saveItem( testItem4, 
               EvalTestDataLoad.ADMIN_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      // test admin can edit any item
      testItem2.setDescription("something admin new");
      authoringService.saveItem( testItem2, 
            EvalTestDataLoad.ADMIN_USER_ID);

      // test that editing unowned item causes permission Assert.failure
      try {
         testItem3.setDescription("something maint new");
         authoringService.saveItem( testItem3, 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
      }

      // test that setting sharing to PUBLIC as non-admin Assert.fails
      try {
         testItem1.setSharing(EvalConstants.SHARING_PUBLIC);
         authoringService.saveItem( testItem1, 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // test admin can set sharing to public
      testItem1.setSharing(EvalConstants.SHARING_PUBLIC);
      authoringService.saveItem( testItem1, 
            EvalTestDataLoad.ADMIN_USER_ID);

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#deleteItem(java.lang.Long, java.lang.String)}.
    */
   @Test
   public void testDeleteItem() {
      // test removing item without permissions Assert.fails
      try {
         authoringService.deleteItem(etdl.item7.getId(), 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
      }

      try {
         authoringService.deleteItem(etdl.item4.getId(), 
               EvalTestDataLoad.USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
      }

      // test removing locked item Assert.fails
      try {
         authoringService.deleteItem(etdl.item2.getId(), 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      try {
         authoringService.deleteItem(etdl.item1.getId(), 
               EvalTestDataLoad.ADMIN_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      // ADMIN CAN REMOVE EXPERT ITEMS NOW -AZ
//    // test cannot remove expert item
//    try {
//    authoringService.deleteItem(etdl.item6.getId(), 
//    EvalTestDataLoad.ADMIN_USER_ID);
//    Assert.fail("Should have thrown exception");
//    } catch (IllegalStateException e) {
//    Assert.assertNotNull(e);
//    }

      // test removing unused item OK
      authoringService.deleteItem(etdl.item4.getId(), 
            EvalTestDataLoad.MAINT_USER_ID);
      Assert.assertNull( authoringService.getItemById(etdl.item4.getId()) );

      authoringService.deleteItem(etdl.item7.getId(), 
            EvalTestDataLoad.ADMIN_USER_ID);
      Assert.assertNull( authoringService.getItemById(etdl.item7.getId()) );

      // this test makes no sense -AZ
//      // test removing an item that is in use is ok
//      authoringService.deleteItem(etdl.item6.getId(), 
//            EvalTestDataLoad.ADMIN_USER_ID);

      // test removing invalid item id Assert.fails
      try {
         authoringService.deleteItem(EvalTestDataLoad.INVALID_LONG_ID, 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * This tests the ability to remove the item and scale at the same time
    */
   @Test
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
      Assert.assertNotNull( evaluationDao.findById(EvalScale.class, scale1.getId()) );
      Assert.assertNotNull( evaluationDao.findById(EvalItem.class, item1.getId()) );

      authoringService.deleteItem(item1.getId(), EvalTestDataLoad.ADMIN_USER_ID);

      // not check that they are both gone
      Assert.assertNull( evaluationDao.findById(EvalItem.class, item1.getId()) );
      Assert.assertNull( evaluationDao.findById(EvalScale.class, scale1.getId()) );

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemsForUser(java.lang.String, java.lang.String)}.
    */
   @Test
   public void testGetItemsForUser() {
      List<EvalItem> l;
      List<Long> ids;
      // NOTE: 32 preloaded public expert items to take into account currently
      int preloadedCount = 32;

      // test getting all items for the admin user
      l = authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, null, null, true);
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
      l = authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, 
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
      l = authoringService.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, null, null, true);
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
      l = authoringService.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, null, null, false);
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
      l = authoringService.getItemsForUser(EvalTestDataLoad.USER_ID, null, null, true);
      Assert.assertNotNull( l );
      Assert.assertEquals(5 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.item1.getId() ));
      Assert.assertTrue(ids.contains( etdl.item2.getId() ));

      Assert.assertTrue(ids.contains( etdl.item1Eid.getId() ));
      Assert.assertTrue(ids.contains( etdl.item2Eid.getId() ));
      Assert.assertTrue(ids.contains( etdl.item3Eid.getId() ));

      // test getting private items for the admin user (all private items)
      l = authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID,
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
      l = authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID,
            EvalConstants.SHARING_PRIVATE, "do you think", true);
      Assert.assertNotNull( l );
      Assert.assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.item3.getId() ));
      Assert.assertTrue(ids.contains( etdl.item4.getId() ));
      Assert.assertTrue(ids.contains( etdl.item11.getId() ));

      // test getting private all private items, expert excluded
      l = authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID,
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
      l = authoringService.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, 
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
      l = authoringService.getItemsForUser(EvalTestDataLoad.USER_ID, 
            EvalConstants.SHARING_PRIVATE, null, true);
      Assert.assertNotNull( l );
      Assert.assertEquals(0, l.size());

      // test getting public items for the admin user
      l = authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, 
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
      l = authoringService.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID, 
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
      l = authoringService.getItemsForUser(EvalTestDataLoad.USER_ID, 
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
      l = authoringService.getItemsForUser( EvalTestDataLoad.INVALID_USER_ID, null, null, true);
      Assert.assertNotNull( l );
      Assert.assertEquals(5 + preloadedCount, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.item1.getId() ));
      Assert.assertTrue(ids.contains( etdl.item2.getId() ));

      Assert.assertTrue(ids.contains( etdl.item1Eid.getId() ));
      Assert.assertTrue(ids.contains( etdl.item2Eid.getId() ));
      Assert.assertTrue(ids.contains( etdl.item3Eid.getId() ));

      // test invalid sharing constant causes Assert.failure
      try {
         authoringService.getItemsForUser(EvalTestDataLoad.ADMIN_USER_ID, 
               EvalTestDataLoad.INVALID_CONSTANT_STRING, null, true);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getItemsForTemplate(java.lang.Long)}.
    */
   @Test
   public void testGetItemsForTemplate() {
      List<EvalItem> l;
      List<Long> ids;

      // test getting all items by valid templates
      l = authoringService.getItemsForTemplate( etdl.templateAdmin.getId(), null );
      Assert.assertNotNull( l );
      Assert.assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.item2.getId() ));
      Assert.assertTrue(ids.contains( etdl.item3.getId() ));
      Assert.assertTrue(ids.contains( etdl.item5.getId() ));

      // test getting all items by valid templates
      l = authoringService.getItemsForTemplate( etdl.templatePublic.getId(), null );
      Assert.assertNotNull( l );
      Assert.assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.item1.getId() ));

      // test getting items from template with no items
      l = authoringService.getItemsForTemplate( etdl.templateAdminNoItems.getId(), null );
      Assert.assertNotNull( l );
      Assert.assertEquals(0, l.size());

      // test getting items for specific user returns correct items
      // admin should get all items
      l = authoringService.getItemsForTemplate( etdl.templateAdmin.getId(), 
            EvalTestDataLoad.ADMIN_USER_ID );
      Assert.assertNotNull( l );
      Assert.assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.item2.getId() ));
      Assert.assertTrue(ids.contains( etdl.item3.getId() ));
      Assert.assertTrue(ids.contains( etdl.item5.getId() ));

      l = authoringService.getItemsForTemplate( etdl.templateUnused.getId(), 
            EvalTestDataLoad.ADMIN_USER_ID );
      Assert.assertNotNull( l );
      Assert.assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.item1.getId() ));
      Assert.assertTrue(ids.contains( etdl.item3.getId() ));
      Assert.assertTrue(ids.contains( etdl.item5.getId() ));

      // owner should see all items
      l = authoringService.getItemsForTemplate( etdl.templateUnused.getId(), 
            EvalTestDataLoad.MAINT_USER_ID );
      Assert.assertNotNull( l );
      Assert.assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.item1.getId() ));
      Assert.assertTrue(ids.contains( etdl.item3.getId() ));
      Assert.assertTrue(ids.contains( etdl.item5.getId() ));

      l = authoringService.getItemsForTemplate( etdl.templateUser.getId(), 
            EvalTestDataLoad.USER_ID );
      Assert.assertNotNull( l );
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.item1.getId() ));
      Assert.assertTrue(ids.contains( etdl.item5.getId() ));

      // TODO - takers should see items at their level (one level) if they have access
      l = authoringService.getItemsForTemplate( etdl.templateUser.getId(), 
            EvalTestDataLoad.STUDENT_USER_ID );
      Assert.assertNotNull( l );
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.item1.getId() ));
      Assert.assertTrue(ids.contains( etdl.item5.getId() ));

      // TODO - add in tests that take the hierarchy into account

      // test getting items from invalid template Assert.fails
      l = authoringService.getItemsForTemplate( EvalTestDataLoad.INVALID_LONG_ID, null );
      Assert.assertNotNull( l );
      Assert.assertEquals(0, l.size());

      // TODO - MAKE this work later on
//    // test getting items for invalid user returns nothing
//    l = authoringService.getItemsForTemplate( etdl.templatePublic.getId(), 
//    EvalTestDataLoad.INVALID_USER_ID );
//    Assert.assertNotNull( l );
//    Assert.assertEquals(0, l.size());

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getTemplateItemById(java.lang.Long)}.
    */
   @Test
   public void testGetTemplateItemById() {
      EvalTemplateItem templateItem;

      // test getting valid templateItems by id
      templateItem = authoringService.getTemplateItemById( etdl.templateItem1P.getId() );
      Assert.assertNotNull(templateItem);
      Assert.assertEquals(etdl.templateItem1P.getId(), templateItem.getId());

      templateItem = authoringService.getTemplateItemById( etdl.templateItem1User.getId() );
      Assert.assertNotNull(templateItem);
      Assert.assertEquals(etdl.templateItem1User.getId(), templateItem.getId());

      // test get eval by invalid id returns null
      templateItem = authoringService.getTemplateItemById( EvalTestDataLoad.INVALID_LONG_ID );
      Assert.assertNull(templateItem);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getTemplateItemById(java.lang.Long)}.
    */
   @Test
   public void testGetTemplateItemByEid() {
      EvalTemplateItem templateItem;

      // test getting valid templateItems by eid
      templateItem = authoringService.getTemplateItemByEid( etdl.templateItem1Eid.getEid() );
      Assert.assertNotNull(templateItem);
      Assert.assertEquals(etdl.templateItem1Eid.getEid(), templateItem.getEid());

      templateItem = authoringService.getTemplateItemByEid( etdl.templateItem2Eid.getEid() );
      Assert.assertNotNull(templateItem);
      Assert.assertEquals(etdl.templateItem2Eid.getEid(), templateItem.getEid());

      templateItem = authoringService.getTemplateItemByEid( etdl.templateItem3Eid.getEid() );
      Assert.assertNotNull(templateItem);
      Assert.assertEquals(etdl.templateItem3Eid.getEid(), templateItem.getEid());

      //test getting valid template item not having eid set returns null
      templateItem = authoringService.getTemplateItemByEid( etdl.templateItem1User.getEid() );
      Assert.assertNull(templateItem);

      //test getting template item using invalid eid returns null
      templateItem = authoringService.getTemplateItemByEid( EvalTestDataLoad.INVALID_STRING_EID );
      Assert.assertNull(templateItem);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#saveTemplateItem(org.sakaiproject.evaluation.model.EvalTemplateItem, java.lang.String)}.
    */
   @Test
   public void testSaveTemplateItem() {

      // load up a no items template to work with
      EvalTemplate noItems = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, etdl.templateAdminNoItems.getId());

      // test saving a new templateItem actually creates the linkage in the item and template
      EvalTemplateItem eiTest1 = new EvalTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
            noItems, etdl.item5, null, 
            EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
            EvalConstants.HIERARCHY_NODE_ID_NONE, 3,
            null, Boolean.FALSE, false, false, null, null, null);
      authoringService.saveTemplateItem( eiTest1, 
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
      etdl.templateUnused.setLocked(false); // why is this needed?
      authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
            noItems, etdl.item7, 2, 
            EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
            EvalConstants.HIERARCHY_NODE_ID_NONE, 3,
            null, Boolean.FALSE, false, false, null, null, null),
            EvalTestDataLoad.ADMIN_USER_ID);

      LOG.debug("ZZZZZZZZZZZZZZ template: " + etdl.templateUnused.getId() + ":" + etdl.templateUnused.getLocked());

      // test saving valid templateItem with locked item
      authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templateUnused, etdl.item2, 3, 
            EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
            EvalConstants.HIERARCHY_NODE_ID_NONE, null,
            EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, false, false, null, null, null),
            EvalTestDataLoad.MAINT_USER_ID);

      // test saving valid templateItem with empty required fields (inherit from item)
      EvalTemplateItem eiTest2 = new EvalTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
            noItems, etdl.item4, 99, 
            null, EvalConstants.HIERARCHY_LEVEL_TOP, 
            EvalConstants.HIERARCHY_NODE_ID_NONE, null,
            null, null, false, false, null, null, null);
      authoringService.saveTemplateItem( eiTest2, 
            EvalTestDataLoad.ADMIN_USER_ID);
      // make sure the values are filled in for us
      Assert.assertNotNull( eiTest2.getLastModified() );
      Assert.assertNotNull( eiTest2.getCategory() );
      Assert.assertNotNull( eiTest2.getScaleDisplaySetting() );
      Assert.assertNotNull( eiTest2.getUsesNA() );
      // make sure filled in values match the ones set in the item
      Assert.assertTrue( eiTest2.getCategory().equals(etdl.item4.getCategory()) );
      Assert.assertTrue( eiTest2.getScaleDisplaySetting().equals(etdl.item4.getScaleDisplaySetting()) );
      // not checking is UsesNA is equal because it is null in the item

      // make sure the displayOrder is set correctly (to 3) when set wrong
      Assert.assertEquals( 3, eiTest2.getDisplayOrder().intValue() );

      // test saving templateItem with no item Assert.fails
      try {
         authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
               etdl.templateUnused, null, 3, 
               EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
               EvalConstants.HIERARCHY_NODE_ID_NONE, null,
               EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, false, false, null, null, null),
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }
      
      // test saving templateItem with no template Assert.fails
      try {
         authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
               null, etdl.item3, 3, 
               EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
               EvalConstants.HIERARCHY_NODE_ID_NONE, null,
               EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, false, false, null, null, null),
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // test saving scaled item with text size set Assert.fails
      try {
         authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
               etdl.templateUnused, etdl.item4, 3, 
               EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
               EvalConstants.HIERARCHY_NODE_ID_NONE, 2,
               EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, false, false, null, null, null),
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // test saving text item with scale display setting Assert.fails
      try {
         authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
               etdl.templateUnused, etdl.item6, 3, 
               EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
               EvalConstants.HIERARCHY_NODE_ID_NONE, 4,
               EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, false, false, null, null, null),
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // TODO - add logic to not allow an item to be associated with the same template twice?
//    // test saving header type item with scale setting or text size set Assert.fails
//    try {
//    authoringService.saveTemplateItem( new EvalTemplateItem( new Date(), 
//    EvalTestDataLoad.MAINT_USER_ID, etdl.templateUnused, etdl.item3, 
//    new Integer(3), EvalConstants.ITEM_CATEGORY_COURSE, 
//    null, EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, null, null),
//    EvalTestDataLoad.MAINT_USER_ID);
//    Assert.fail("Should have thrown exception");
//    } catch (IllegalArgumentException e) {
//    Assert.assertNotNull(e);
//    Assert.fail(e.getMessage()); // see why Assert.failing
//    }

      // test saving header type item with scale setting or text size set Assert.fails
      try {
         authoringService.saveTemplateItem( new EvalTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
               etdl.templateUnused, etdl.item8, 3, 
               EvalConstants.ITEM_CATEGORY_COURSE, EvalConstants.HIERARCHY_LEVEL_TOP, 
               EvalConstants.HIERARCHY_NODE_ID_NONE, 1,
               EvalConstants.ITEM_SCALE_DISPLAY_FULL, Boolean.TRUE, false, false, null, null, null),
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
      testTemplateItem1.setCategory( EvalConstants.ITEM_CATEGORY_INSTRUCTOR );
      authoringService.saveTemplateItem( testTemplateItem1, EvalTestDataLoad.ADMIN_USER_ID );

      testTemplateItem2.setCategory( EvalConstants.ITEM_CATEGORY_INSTRUCTOR );
      authoringService.saveTemplateItem( testTemplateItem2, EvalTestDataLoad.MAINT_USER_ID );

      // TODO - CANNOT RUN THIS TEST FOR NOW BECAUSE OF HIBERNATE
//    // test that template and item cannot be changed on existing templateItem
//    try {
//    testTemplateItem1.setItem( etdl.item1 );
//    authoringService.saveTemplateItem( testTemplateItem1, EvalTestDataLoad.ADMIN_USER_ID );
//    Assert.fail("Should have thrown exception");
//    } catch (IllegalStateException e) {
//    Assert.assertNotNull(e);
//    Assert.fail(e.getMessage()); // see why Assert.failing
//    }

//    try {
//    testTemplateItem1.setTemplate( etdl.templateAdminNoItems );
//    authoringService.saveTemplateItem( testTemplateItem1, EvalTestDataLoad.ADMIN_USER_ID );
//    Assert.fail("Should have thrown exception");
//    } catch (IllegalStateException e) {
//    Assert.assertNotNull(e);
//    Assert.fail(e.getMessage()); // see why Assert.failing
//    }

      // test editing templateItem in LOCKED template Assert.fails
      try {
         testTemplateItem3.setCategory( EvalConstants.ITEM_CATEGORY_INSTRUCTOR );
         authoringService.saveTemplateItem( testTemplateItem3, EvalTestDataLoad.ADMIN_USER_ID );
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      try {
         testTemplateItem4.setCategory( EvalConstants.ITEM_CATEGORY_INSTRUCTOR );
         authoringService.saveTemplateItem( testTemplateItem4, EvalTestDataLoad.MAINT_USER_ID );
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      // test admin can edit any templateItem
      testTemplateItem2.setCategory( EvalConstants.ITEM_CATEGORY_ENVIRONMENT );
      authoringService.saveTemplateItem( testTemplateItem2, EvalTestDataLoad.ADMIN_USER_ID );

      // test that editing unowned templateItem causes permission Assert.failure
      try {
         testTemplateItem2.setCategory( EvalConstants.ITEM_CATEGORY_COURSE );
         authoringService.saveTemplateItem( testTemplateItem2, EvalTestDataLoad.USER_ID );
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
      }

      // test that editing unowned templateItem causes permission Assert.failure
      try {
         testTemplateItem1.setCategory( EvalConstants.ITEM_CATEGORY_COURSE );
         authoringService.saveTemplateItem( testTemplateItem1, EvalTestDataLoad.MAINT_USER_ID );
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#deleteTemplateItem(java.lang.Long, java.lang.String)}.
    */
   @Test
   public void testDeleteTemplateItem() {
      // test removing templateItem without permissions Assert.fails
      try {
         authoringService.deleteTemplateItem(etdl.templateItem3PU.getId(), 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
      }

      try {
         authoringService.deleteTemplateItem(etdl.templateItem3U.getId(), 
               EvalTestDataLoad.USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
      }

      // test removing templateItem from locked template Assert.fails
      try {
         authoringService.deleteTemplateItem(etdl.templateItem1P.getId(), 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      try {
         authoringService.deleteTemplateItem(etdl.templateItem2A.getId(), 
               EvalTestDataLoad.ADMIN_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      // verify that the item/template link exists before removal
      EvalTemplateItem eti1 = authoringService.getTemplateItemById(etdl.templateItem3U.getId());
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
      authoringService.deleteTemplateItem(etdl.templateItem3U.getId(), 
            EvalTestDataLoad.MAINT_USER_ID);
      Assert.assertNull( authoringService.getTemplateItemById(etdl.templateItem3U.getId()) );

      // verify that the item/template link no longer exists
      Assert.assertNotNull( eti1.getItem().getTemplateItems() );
      Assert.assertNotNull( eti1.getTemplate().getTemplateItems() );
      Assert.assertFalse( eti1.getItem().getTemplateItems().isEmpty() );
      Assert.assertFalse( eti1.getTemplate().getTemplateItems().isEmpty() );
      Assert.assertEquals( itemsSize-1, eti1.getItem().getTemplateItems().size() );
      Assert.assertEquals( templatesSize-1, eti1.getTemplate().getTemplateItems().size() );
      Assert.assertTrue(! eti1.getItem().getTemplateItems().contains( eti1 ) );
      Assert.assertTrue(! eti1.getTemplate().getTemplateItems().contains( eti1 ) );

      authoringService.deleteTemplateItem(etdl.templateItem6UU.getId(), 
            EvalTestDataLoad.USER_ID);
      Assert.assertNull( authoringService.getTemplateItemById(etdl.templateItem6UU.getId()) );

      // test admin can remove unowned templateItem
      authoringService.deleteTemplateItem(etdl.templateItem5U.getId(), 
            EvalTestDataLoad.ADMIN_USER_ID);
      Assert.assertNull( authoringService.getTemplateItemById(etdl.templateItem5U.getId()) );

      // test removing invalid templateItem id Assert.fails
      try {
         authoringService.deleteTemplateItem(EvalTestDataLoad.INVALID_LONG_ID, 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getTemplateItemsForTemplate(java.lang.Long)}.
    */
   @Test
   public void testGetTemplateItemsForTemplate() {
      List<EvalTemplateItem> l;
      List<Long> ids;

      // test getting all items by valid templates
      l = authoringService.getTemplateItemsForTemplate( etdl.templateAdmin.getId(), null, null, null );
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
      l = authoringService.getTemplateItemsForTemplate( etdl.templatePublic.getId(), null, null, null );
      Assert.assertNotNull( l );
      Assert.assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateItem1P.getId() ));

      // test getting items from template with no items
      l = authoringService.getTemplateItemsForTemplate( etdl.templateAdminNoItems.getId(), null, null, null );
      Assert.assertNotNull( l );
      Assert.assertEquals(0, l.size());

      // test getting items for specific user returns correct items
      // admin should get all items
      l = authoringService.getTemplateItemsForTemplate( etdl.templateAdmin.getId(), null, null, null );
      Assert.assertNotNull( l );
      Assert.assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateItem2A.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem3A.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem5A.getId() ));

      l = authoringService.getTemplateItemsForTemplate( etdl.templateUnused.getId(), null, null, null );
      Assert.assertNotNull( l );
      Assert.assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateItem1U.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem3U.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem5U.getId() ));

      // check that the return order is correct
      Assert.assertEquals( 1, ((EvalTemplateItem)l.get(0)).getDisplayOrder().intValue() );
      Assert.assertEquals( 2, ((EvalTemplateItem)l.get(1)).getDisplayOrder().intValue() );
      Assert.assertEquals( 3, ((EvalTemplateItem)l.get(2)).getDisplayOrder().intValue() );

      // owner should see all items
      l = authoringService.getTemplateItemsForTemplate( etdl.templateUnused.getId(), null, null, null );
      Assert.assertNotNull( l );
      Assert.assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateItem1U.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem3U.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem5U.getId() ));

      l = authoringService.getTemplateItemsForTemplate( etdl.templateUser.getId(), null, null, null );
      Assert.assertNotNull( l );
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateItem1User.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem5User.getId() ));

      // TODO - takers should see items at their level (one level) if they have access
      l = authoringService.getTemplateItemsForTemplate( etdl.templateUser.getId(), null, null, null );
      Assert.assertNotNull( l );
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateItem1User.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem5User.getId() ));

      // TODO - add in tests that take the hierarchy into account

      // test getting items from invalid template returns nothing
      l = authoringService.getTemplateItemsForTemplate( EvalTestDataLoad.INVALID_LONG_ID, null, null, null );
      Assert.assertNotNull( l );
      Assert.assertEquals(0, l.size());

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#getBlockChildTemplateItemsForBlockParent(Long, boolean)}.
    */
   @Test
   public void testGetBlockChildTemplateItemsForBlockParent() {
      List<EvalTemplateItem> l;
      List<Long> ids;

      // test getting child block items
      l = authoringService.getBlockChildTemplateItemsForBlockParent( etdl.templateItem9B.getId(), false );
      Assert.assertNotNull( l );
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateItem2B.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem3B.getId() ));

      // check that the return order is correct
      Assert.assertEquals( 1, ((EvalTemplateItem)l.get(0)).getDisplayOrder().intValue() );
      Assert.assertEquals( 2, ((EvalTemplateItem)l.get(1)).getDisplayOrder().intValue() );

      // test getting child block items and parent
      l = authoringService.getBlockChildTemplateItemsForBlockParent( etdl.templateItem9B.getId(), true );
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

      // test getting child items from invalid templateItem Assert.fails
      try {
         authoringService.getBlockChildTemplateItemsForBlockParent( EvalTestDataLoad.INVALID_LONG_ID, false );
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // test getting child items from non-parent templateItem Assert.fails
      try {
         authoringService.getBlockChildTemplateItemsForBlockParent( etdl.templateItem2A.getId(), false );
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#canModifyItem(String, Long)}.
    */
   @Test
   public void testCanModifyItem() {
      // test can control owned items
      Assert.assertTrue( authoringService.canModifyItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.item7.getId() ) );
      Assert.assertTrue( authoringService.canModifyItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.item4.getId() ) );

      // test admin user can override perms
      Assert.assertTrue( authoringService.canModifyItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.item4.getId() ) );

      // test cannot control unowned items
      Assert.assertFalse( authoringService.canModifyItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.item7.getId() ) );
      Assert.assertFalse( authoringService.canModifyItem( EvalTestDataLoad.USER_ID, 
            etdl.item4.getId() ) );

      // test cannot control locked items
      Assert.assertFalse( authoringService.canModifyItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.item1.getId() ) );
      Assert.assertFalse( authoringService.canModifyItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.item2.getId() ) );

      // test invalid item id causes Assert.failure
      try {
         authoringService.canModifyItem( EvalTestDataLoad.MAINT_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID );
         Assert.fail("Should have thrown exception");
      } catch (RuntimeException e) {
         Assert.assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#canRemoveItem(String, Long)}.
    */
   @Test
   public void testCanRemoveItem() {
      // test can remove owned items
      Assert.assertTrue( authoringService.canRemoveItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.item7.getId() ) );
      Assert.assertTrue( authoringService.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.item4.getId() ) );

      // test admin user can override perms
      Assert.assertTrue( authoringService.canRemoveItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.item4.getId() ) );

      // test cannot remove unowned items
      Assert.assertFalse( authoringService.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.item7.getId() ) );
      Assert.assertFalse( authoringService.canRemoveItem( EvalTestDataLoad.USER_ID, 
            etdl.item4.getId() ) );

      // can remove items that are in use now
//      // test cannot remove unlocked items that are in use in templates
//      Assert.assertFalse( authoringService.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
//            etdl.item6.getId() ) );
//      Assert.assertFalse( authoringService.canRemoveItem( EvalTestDataLoad.ADMIN_USER_ID, 
//            etdl.item9.getId() ) );

      // test cannot remove locked items
      Assert.assertFalse( authoringService.canRemoveItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.item1.getId() ) );
      Assert.assertFalse( authoringService.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.item2.getId() ) );

      // test invalid item id causes Assert.failure
      try {
         authoringService.canRemoveItem( EvalTestDataLoad.MAINT_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID );
         Assert.fail("Should have thrown exception");
      } catch (RuntimeException e) {
         Assert.assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalItemsLogicImpl#canControlTemplateItem(java.lang.String, java.lang.Long)}.
    */
   @Test
   public void testCanControlTemplateItem() {
      // test can control owned items
      Assert.assertTrue( authoringService.canControlTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateItem3PU.getId() ) );
      Assert.assertTrue( authoringService.canControlTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templateItem3U.getId() ) );

      // test admin user can override perms
      Assert.assertTrue( authoringService.canControlTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateItem3U.getId() ) );

      // test cannot control unowned items
      Assert.assertFalse( authoringService.canControlTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templateItem3PU.getId() ) );
      Assert.assertFalse( authoringService.canControlTemplateItem( EvalTestDataLoad.USER_ID, 
            etdl.templateItem3U.getId() ) );

      // test cannot control items locked by locked template
      Assert.assertFalse( authoringService.canControlTemplateItem( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateItem2A.getId() ) );
      Assert.assertFalse( authoringService.canControlTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templateItem1P.getId() ) );

      // test invalid item id causes Assert.failure
      try {
         authoringService.canControlTemplateItem( EvalTestDataLoad.MAINT_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID );
         Assert.fail("Should have thrown exception");
      } catch (RuntimeException e) {
         Assert.assertNotNull(e);
      }
   }



   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#getItemGroups(java.lang.Long, java.lang.String, boolean)}.
    */
   @Test
   public void testGetItemGroups() {
      List<Long> ids;
      List<EvalItemGroup> eItems;

      // NOTE: preloaded groups to take into account

      // check all expert top level groups
      eItems = authoringService.getItemGroups(null, EvalTestDataLoad.ADMIN_USER_ID, true, true);
      Assert.assertNotNull( eItems );
      Assert.assertEquals(4 + 4, eItems.size()); // 4 preloaded top level expert groups
      ids = EvalTestDataLoad.makeIdList(eItems);
      Assert.assertTrue(ids.contains( etdl.categoryA.getId() ));
      Assert.assertTrue(ids.contains( etdl.categoryB.getId() ));
      Assert.assertTrue(ids.contains( etdl.categoryC.getId() ));
      Assert.assertTrue(! ids.contains( etdl.categoryD.getId() ));
      Assert.assertTrue(! ids.contains( etdl.objectiveA1.getId() ));
      Assert.assertTrue(! ids.contains( etdl.objectiveA2.getId() ));

      // check all non-expert top level groups
      eItems = authoringService.getItemGroups(null, EvalTestDataLoad.ADMIN_USER_ID, true, false);
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
      eItems = authoringService.getItemGroups(etdl.categoryA.getId(), EvalTestDataLoad.ADMIN_USER_ID, true, true);
      Assert.assertNotNull( eItems );
      Assert.assertEquals(2, eItems.size());
      ids = EvalTestDataLoad.makeIdList(eItems);
      Assert.assertTrue(ids.contains( etdl.objectiveA1.getId() ));
      Assert.assertTrue(ids.contains( etdl.objectiveA2.getId() ));

      // check only non-empty top level groups
      eItems = authoringService.getItemGroups(null, EvalTestDataLoad.ADMIN_USER_ID, false, true);
      Assert.assertNotNull( eItems );
      Assert.assertEquals(2 + 4, eItems.size()); // 4 preloaded non-empty top level groups
      ids = EvalTestDataLoad.makeIdList(eItems);
      Assert.assertTrue(ids.contains( etdl.categoryA.getId() ));
      Assert.assertTrue(ids.contains( etdl.categoryB.getId() ));

      // check only non-empty contained groups
      eItems = authoringService.getItemGroups(etdl.categoryA.getId(), EvalTestDataLoad.ADMIN_USER_ID, false, true);
      Assert.assertNotNull( eItems );
      Assert.assertEquals(1, eItems.size());
      ids = EvalTestDataLoad.makeIdList(eItems);
      Assert.assertTrue(ids.contains( etdl.objectiveA1.getId() ));      

      // check trying to get groups from empty group
      eItems = authoringService.getItemGroups(etdl.categoryC.getId(), EvalTestDataLoad.ADMIN_USER_ID, false, true);
      Assert.assertNotNull( eItems );
      Assert.assertEquals(0, eItems.size());

      // test attempting to use invalid item group id
      try {
         authoringService.getItemGroups(EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.ADMIN_USER_ID, false, true);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
         //Assert.fail(e.getMessage()); // check the reason for Assert.failure
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#getItemsInItemGroup(java.lang.Long, boolean)}.
    */
   @Test
   public void testGetItemsInItemGroup() {
      List<Long> ids;
      List<EvalItem> eItems;

      // check items from a low level group
      eItems = authoringService.getItemsInItemGroup(etdl.objectiveA1.getId(), true);
      Assert.assertNotNull( eItems );
      Assert.assertEquals(2, eItems.size());
      ids = EvalTestDataLoad.makeIdList(eItems);
      Assert.assertTrue(ids.contains( etdl.item2.getId() ));
      Assert.assertTrue(ids.contains( etdl.item6.getId() ));

      // check items from a top level group
      eItems = authoringService.getItemsInItemGroup(etdl.categoryB.getId(), true);
      Assert.assertNotNull( eItems );
      Assert.assertEquals(1, eItems.size());
      ids = EvalTestDataLoad.makeIdList(eItems);
      Assert.assertTrue(ids.contains( etdl.item1.getId() ));

      // check items from an empty group
      eItems = authoringService.getItemsInItemGroup(etdl.objectiveA2.getId(), true);
      Assert.assertNotNull( eItems );
      Assert.assertEquals(0, eItems.size());

      // test attempting to use invalid item group id
      try {
         authoringService.getItemsInItemGroup(EvalTestDataLoad.INVALID_LONG_ID, true);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }


   /**
    * Test method for {@link EvalExpertItemsLogicImpl#getItemGroupById(Long)}
    */
   @Test
   public void testGetItemGroupById() {
      EvalItemGroup itemGroup;

      // test getting valid items by id
      itemGroup = authoringService.getItemGroupById( etdl.categoryA.getId() );
      Assert.assertNotNull(itemGroup);
      Assert.assertEquals(etdl.categoryA.getId(), itemGroup.getId());

      itemGroup = authoringService.getItemGroupById( etdl.objectiveA1.getId() );
      Assert.assertNotNull(itemGroup);
      Assert.assertEquals(etdl.objectiveA1.getId(), itemGroup.getId());

      // test get eval by invalid id returns null
      itemGroup = authoringService.getItemGroupById( EvalTestDataLoad.INVALID_LONG_ID );
      Assert.assertNull(itemGroup);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#saveItemGroup(org.sakaiproject.evaluation.model.EvalItemGroup, java.lang.String)}.
    */
   @Test
   public void testSaveItemGroup() {

      // test create a valid group
      EvalItemGroup newCategory = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, 
            EvalConstants.ITEM_GROUP_TYPE_CATEGORY, "new category");
      authoringService.saveItemGroup(newCategory, EvalTestDataLoad.ADMIN_USER_ID);
      Assert.assertNotNull( newCategory.getId() );

      // check that defaults were filled in
      Assert.assertNotNull( newCategory.getLastModified() );
      Assert.assertNotNull( newCategory.getExpert() );
      Assert.assertEquals( newCategory.getExpert(), Boolean.FALSE );
      Assert.assertNull( newCategory.getParent() );

      // test that creating subgroup without parent causes Assert.failure
      EvalItemGroup newObjective = new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, 
            EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE, "new objective");
      try {
         authoringService.saveItemGroup(newObjective, EvalTestDataLoad.ADMIN_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
         //Assert.fail(e.getMessage()); // check the reason for Assert.failure
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
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // test admin can create expert group
      authoringService.saveItemGroup(newExpertGroup, EvalTestDataLoad.ADMIN_USER_ID);
      Assert.assertNotNull( newExpertGroup.getId() );

      // test creating invalid expert group type Assert.fails
      try {
         authoringService.saveItemGroup( 
               new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalTestDataLoad.INVALID_CONSTANT_STRING, "test",
                     "desc", Boolean.TRUE, null, null), 
                     EvalTestDataLoad.ADMIN_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // test creating top level category with parent Assert.fails
      try {
         authoringService.saveItemGroup( 
               new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.ITEM_GROUP_TYPE_CATEGORY, "test",
                     "desc", Boolean.FALSE, newCategory, null), 
                     EvalTestDataLoad.ADMIN_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // test trying to put objective as a top level category
      try {
         authoringService.saveItemGroup( 
               new EvalItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE, "test",
                     "desc", Boolean.FALSE, null, null), 
                     EvalTestDataLoad.ADMIN_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#removeItemGroup(java.lang.Long, java.lang.String, boolean)}.
    */
   @Test
   public void testRemoveItemGroup() {

      // test cannot remove item groups without permission
      try {
         authoringService.removeItemGroup(etdl.categoryD.getId(), EvalTestDataLoad.MAINT_USER_ID, false);
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
         //Assert.fail(e.getMessage()); // check the reason for Assert.failure
      }

      // test can remove empty categories
      authoringService.removeItemGroup(etdl.categoryD.getId(), EvalTestDataLoad.ADMIN_USER_ID, false);
      Assert.assertNull( authoringService.getItemGroupById(etdl.categoryD.getId()) );

      // test cannot remove non-empty categories when flag set
      try {
         authoringService.removeItemGroup(etdl.categoryA.getId(), EvalTestDataLoad.ADMIN_USER_ID, false);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
         //Assert.fail(e.getMessage()); // check the reason for Assert.failure
      }

      // test can remove non-empty categories when flag unset
      authoringService.removeItemGroup(etdl.categoryA.getId(), EvalTestDataLoad.ADMIN_USER_ID, true);
      Assert.assertNull( authoringService.getItemGroupById(etdl.categoryA.getId()) );

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#canUpdateItemGroup(String, Long)}.
    */
   @Test
   public void testCanUpdateItemGroup() {
      // test can control owned items
      Assert.assertTrue( authoringService.canUpdateItemGroup(EvalTestDataLoad.ADMIN_USER_ID, etdl.categoryA.getId() ) );
      Assert.assertTrue( authoringService.canUpdateItemGroup(EvalTestDataLoad.ADMIN_USER_ID, etdl.objectiveA1.getId() ) );

      // test cannot control unowned items
      Assert.assertFalse( authoringService.canUpdateItemGroup(EvalTestDataLoad.MAINT_USER_ID, etdl.categoryA.getId() ) );
      Assert.assertFalse( authoringService.canUpdateItemGroup(EvalTestDataLoad.MAINT_USER_ID, etdl.objectiveA1.getId() ) );

      // test invalid item id causes Assert.failure
      try {
         authoringService.canUpdateItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalTestDataLoad.INVALID_LONG_ID );
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalExpertItemsLogicImpl#canRemoveItemGroup(String, Long)}.
    */
   @Test
   public void testCanRemoveItemGroup() {
      // test can control owned items
      Assert.assertTrue( authoringService.canRemoveItemGroup(EvalTestDataLoad.ADMIN_USER_ID, etdl.categoryA.getId() ) );
      Assert.assertTrue( authoringService.canRemoveItemGroup(EvalTestDataLoad.ADMIN_USER_ID, etdl.objectiveA1.getId() ) );

      // test cannot control unowned items
      Assert.assertFalse( authoringService.canRemoveItemGroup(EvalTestDataLoad.MAINT_USER_ID, etdl.categoryA.getId() ) );
      Assert.assertFalse( authoringService.canRemoveItemGroup(EvalTestDataLoad.MAINT_USER_ID, etdl.objectiveA1.getId() ) );

      // test invalid item id causes Assert.failure
      try {
         authoringService.canRemoveItemGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalTestDataLoad.INVALID_LONG_ID );
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }



   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#getTemplateById(java.lang.Long)}.
    */
   @Test
   public void testGetTemplateById() {
      EvalTemplate template;

      // test getting valid templates by id
      template = authoringService.getTemplateById( etdl.templateAdmin.getId() );
      Assert.assertNotNull(template);
      Assert.assertEquals(etdl.templateAdmin.getId(), template.getId());

      template = authoringService.getTemplateById( etdl.templatePublic.getId() );
      Assert.assertNotNull(template);
      Assert.assertEquals(etdl.templatePublic.getId(), template.getId());

      // test get eval by invalid id returns null
      template = authoringService.getTemplateById( EvalTestDataLoad.INVALID_LONG_ID );
      Assert.assertNull(template);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#getTemplateByEid(java.lang.String)}.
    */
   @Test
   public void testGetTemplateByEid() {
      EvalTemplate template;

      // test getting template having eid set
      template = authoringService.getTemplateByEid( etdl.templateEid.getEid() );
      Assert.assertNotNull(template);
      Assert.assertEquals(etdl.templateEid.getEid(), template.getEid());

      //test getting template having eid not set  returns null
      template = authoringService.getTemplateByEid( etdl.templatePublic.getEid() );
      Assert.assertNull(template);

      // test getting template by invalid id returns null
      template = authoringService.getTemplateByEid( EvalTestDataLoad.INVALID_STRING_EID );
      Assert.assertNull(template);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#saveTemplate(org.sakaiproject.evaluation.model.EvalTemplate, java.lang.String)}.
    */
   @Test
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
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
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
//    Assert.fail("Should have thrown exception");
//    } catch (RuntimeException e) {
//    Assert.assertNotNull(e);
//    Assert.fail(e.getMessage()); // see why Assert.failing
//    }

      // test editing LOCKED template Assert.fails
      try {
         testTemplate3.setExpert(Boolean.FALSE);
         authoringService.saveTemplate( testTemplate3, 
               EvalTestDataLoad.ADMIN_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      // test admin can edit any template
      testTemplate1.setDescription("something admin new");
      authoringService.saveTemplate( testTemplate1, 
            EvalTestDataLoad.ADMIN_USER_ID);

      // test that editing unowned template causes permission Assert.failure
      try {
         testTemplate4.setDescription("something maint new");
         authoringService.saveTemplate( testTemplate4, 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
      }

      // test that setting sharing to PUBLIC as non-admin Assert.fails
      try {
         testTemplate1.setSharing(EvalConstants.SHARING_PUBLIC);
         authoringService.saveTemplate( testTemplate1, 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
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
   @Test
   public void testDeleteTemplate() {
      // test removing template without permissions Assert.fails
      try {
         authoringService.deleteTemplate(etdl.templatePublicUnused.getId(), 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
      }

      try {
         authoringService.deleteTemplate(etdl.templateUnused.getId(), 
               EvalTestDataLoad.USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
      }

      // test removing locked template Assert.fails
      try {
         authoringService.deleteTemplate(etdl.templatePublic.getId(), 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      try {
         authoringService.deleteTemplate(etdl.templateAdmin.getId(), 
               EvalTestDataLoad.ADMIN_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      // test cannot remove expert template
      try {
         authoringService.deleteTemplate(etdl.templateUserUnused.getId(), 
               EvalTestDataLoad.ADMIN_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      // test removing unused template OK
      authoringService.deleteTemplate(etdl.templateUnused.getId(), 
            EvalTestDataLoad.MAINT_USER_ID);
      Assert.assertNull( authoringService.getTemplateById(etdl.templateUnused.getId()) );

      authoringService.deleteTemplate(etdl.templatePublicUnused.getId(), 
            EvalTestDataLoad.ADMIN_USER_ID);
      Assert.assertNull( authoringService.getTemplateById(etdl.templatePublicUnused.getId()) );

      // test removing invalid template id Assert.fails
      try {
         authoringService.deleteTemplate(EvalTestDataLoad.INVALID_LONG_ID, 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#getTemplatesForUser(String, String)}.
    */
   @Test
   public void testGetTemplatesForUser() {
      List<EvalTemplate> l;
      List<Long> ids;
      // NOTE: No preloaded public templates to take into account right now

      // test getting all templates for admin user (should include all templates)
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, null, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(11, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateAdmin.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateAdminNoItems.getId() ));
      Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUser_4.getId() ));
      Assert.assertTrue(ids.contains( etdl.evalsys_1007_templateUser01.getId() ));

      Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

      // test getting all non-empty templates for admin user
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, null, false);
      Assert.assertNotNull(l);
      Assert.assertEquals(8, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(! ids.contains( etdl.templateAdminNoItems.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateAdmin.getId() ));
      Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));

      Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

      // test getting all templates for maint user
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.MAINT_USER_ID, null, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(4, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
      Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));

      Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

      // test getting all templates for user
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.USER_ID, null, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(5, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

      Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

      // test using SHARING_OWNER same as null (getting all templates)
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_OWNER, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(11, l.size());

      // test getting private templates for admin (admin should see all private)
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_PRIVATE, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(8, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateAdmin.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateAdminNoItems.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUser_4.getId() ));
      Assert.assertTrue(ids.contains( etdl.evalsys_1007_templateUser01.getId() ));

      // test getting non-empty private templates for admin
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.SHARING_PRIVATE, false);
      Assert.assertNotNull(l);
      Assert.assertEquals(5, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(! ids.contains( etdl.templateAdminNoItems.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateAdmin.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));

      // test getting private templates for maint user
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.MAINT_USER_ID, EvalConstants.SHARING_PRIVATE, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));

      // test getting private templates for user
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.USER_ID, EvalConstants.SHARING_PRIVATE, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

      // test getting public templates only (normal user should see all)
      l = authoringService.getTemplatesForUser(EvalTestDataLoad.USER_ID, EvalConstants.SHARING_PUBLIC, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
      Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));

      Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

      // test getting invalid constant causes Assert.failure
      try {
         authoringService.getTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, 
               EvalTestDataLoad.INVALID_CONSTANT_STRING, true);
         Assert.fail("Should have thrown exception");
      } catch (RuntimeException e) {
         Assert.assertNotNull(e);
      }

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#canModifyTemplate(String, Long)}.
    */
   @Test
   public void testCanModifyTemplate() {
      // test can control owned templates
      Assert.assertTrue( authoringService.canModifyTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templatePublicUnused.getId() ) );
      Assert.assertTrue( authoringService.canModifyTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templateUnused.getId() ) );

      // test admin user can override perms
      Assert.assertTrue( authoringService.canModifyTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateUnused.getId() ) );

      // test cannot control unowned templates
      Assert.assertFalse( authoringService.canModifyTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templatePublicUnused.getId() ) );
      Assert.assertFalse( authoringService.canModifyTemplate( EvalTestDataLoad.USER_ID, 
            etdl.templateUnused.getId() ) );

      // test cannot control locked templates
      Assert.assertFalse( authoringService.canModifyTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templatePublic.getId() ) );
      Assert.assertFalse( authoringService.canModifyTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateAdmin.getId() ) );

      // test invalid template id causes Assert.failure
      try {
         authoringService.canModifyTemplate( EvalTestDataLoad.MAINT_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID );
         Assert.fail("Should have thrown exception");
      } catch (RuntimeException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#canRemoveTemplate(String, Long)}.
    */
   @Test
   public void testCanRemoveTemplate() {
      // test can remove owned templates
      Assert.assertTrue( authoringService.canRemoveTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templatePublicUnused.getId() ) );
      Assert.assertTrue( authoringService.canRemoveTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templateUnused.getId() ) );

      // test admin user can override perms
      Assert.assertTrue( authoringService.canRemoveTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateUnused.getId() ) );

      // test cannot remove unowned templates
      Assert.assertFalse( authoringService.canRemoveTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templatePublicUnused.getId() ) );
      Assert.assertFalse( authoringService.canRemoveTemplate( EvalTestDataLoad.USER_ID, 
            etdl.templateUnused.getId() ) );

      // test cannot remove templates that are in use
      Assert.assertFalse( authoringService.canRemoveTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templatePublicUnused.getId() ) );
      Assert.assertFalse( authoringService.canRemoveTemplate( EvalTestDataLoad.USER_ID, 
            etdl.templateUnused.getId() ) );

      // test cannot remove locked templates
      Assert.assertFalse( authoringService.canRemoveTemplate( EvalTestDataLoad.MAINT_USER_ID, 
            etdl.templatePublic.getId() ) );
      Assert.assertFalse( authoringService.canRemoveTemplate( EvalTestDataLoad.ADMIN_USER_ID, 
            etdl.templateAdmin.getId() ) );

      // test invalid template id causes Assert.failure
      try {
         authoringService.canRemoveTemplate( EvalTestDataLoad.MAINT_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID );
         Assert.fail("Should have thrown exception");
      } catch (RuntimeException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.EvalTemplatesLogicImpl#canCreateTemplate(java.lang.String)}.
    */
   @Test
   public void testCanCreateTemplate() {
      // test admin can create templates
      Assert.assertTrue( authoringService.canCreateTemplate(EvalTestDataLoad.ADMIN_USER_ID) );

      // test maint user can create templates (user with special perms)
      Assert.assertTrue( authoringService.canCreateTemplate(EvalTestDataLoad.MAINT_USER_ID) );

      // test normal user cannot create templates
      Assert.assertFalse( authoringService.canCreateTemplate(EvalTestDataLoad.USER_ID) );

      // test invalid user cannot create templates
      Assert.assertFalse( authoringService.canCreateTemplate(EvalTestDataLoad.INVALID_USER_ID) );
   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalAuthoringServiceImpl#copyScales(java.lang.Long[], java.lang.String, boolean)}.
    */
   @Test
   public void testCopyScales() {
      Long[] copiedIds;
      Long[] scaleIds;

      // copy a single scale
      EvalScale original = etdl.scale1;
      scaleIds = new Long[] {etdl.scale1.getId()};
      copiedIds = authoringService.copyScales(scaleIds, "new scale 1", EvalTestDataLoad.MAINT_USER_ID, true);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(scaleIds.length, copiedIds.length);
      EvalScale copy1 = (EvalScale) evaluationDao.findById(EvalScale.class, copiedIds[0]);
      Assert.assertNotNull(copy1);

      // verify the copy worked
      // check the things that should differ
      Assert.assertNotSame(original.getId(), copy1.getId());
      Assert.assertEquals(original.getId(), copy1.getCopyOf());
      Assert.assertEquals("new scale 1", copy1.getTitle());
      Assert.assertEquals(EvalTestDataLoad.MAINT_USER_ID, copy1.getOwner());
      Assert.assertEquals(true, copy1.isHidden());
      Assert.assertEquals(Boolean.FALSE, copy1.getExpert());
      Assert.assertEquals(null, copy1.getExpertDescription());
      Assert.assertEquals(Boolean.FALSE, copy1.getLocked());
      Assert.assertEquals(EvalConstants.SHARING_PRIVATE, copy1.getSharing());

      // check the things that should match
      Assert.assertEquals(original.getIdeal(), copy1.getIdeal());
      Assert.assertEquals(original.getMode(), copy1.getMode());
      for (int i = 0; i < copy1.getOptions().length; i++) {
         Assert.assertEquals(original.getOptions()[i], copy1.getOptions()[i]);
      }


      // make sure title generation works
      scaleIds = new Long[] {etdl.scale1.getId()};
      copiedIds = authoringService.copyScales(scaleIds, "", EvalTestDataLoad.MAINT_USER_ID, false);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(scaleIds.length, copiedIds.length);
      EvalScale copy2 = (EvalScale) evaluationDao.findById(EvalScale.class, copiedIds[0]);
      Assert.assertNotNull(copy2);
      Assert.assertNotNull(copy2.getTitle());

      // check we can copy a bunch of things
      scaleIds = new Long[] {etdl.scale2.getId(), etdl.scale3.getId(), etdl.scale4.getId()};
      copiedIds = authoringService.copyScales(scaleIds, null, EvalTestDataLoad.MAINT_USER_ID, false);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(scaleIds.length, copiedIds.length);
      for( Long copiedId : copiedIds ) {
         Assert.assertNotNull(evaluationDao.findById(EvalScale.class, copiedId));
       }

      // check that invalid scaleid causes death
      scaleIds = new Long[] {etdl.scale2.getId(), EvalTestDataLoad.INVALID_LONG_ID};
      try {
         authoringService.copyScales(scaleIds, null, EvalTestDataLoad.MAINT_USER_ID, false);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalAuthoringServiceImpl#copyItems(java.lang.Long[], java.lang.String, boolean, boolean)}.
    */
   @Test
   public void testCopyItems() {
      Long[] copiedIds;
      Long[] itemIds;

      // copy a single item
      EvalItem original = etdl.item1;
      itemIds = new Long[] {original.getId()};
      copiedIds = authoringService.copyItems(itemIds, EvalTestDataLoad.MAINT_USER_ID, true, true);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(itemIds.length, copiedIds.length);
      EvalItem copy1 = (EvalItem) evaluationDao.findById(EvalItem.class, copiedIds[0]);
      Assert.assertNotNull(copy1);

      // verify the copy worked
      // check the things that should differ
      Assert.assertNotSame(copy1.getId(), original.getId());
      Assert.assertEquals(copy1.getCopyOf(), original.getId());
      Assert.assertEquals(EvalTestDataLoad.MAINT_USER_ID, copy1.getOwner());
      Assert.assertEquals(copy1.isHidden(), true);
      Assert.assertEquals(copy1.getExpert(), Boolean.FALSE);
      Assert.assertEquals(copy1.getExpertDescription(), null);
      Assert.assertEquals(copy1.getLocked(), Boolean.FALSE);
      Assert.assertEquals(EvalConstants.SHARING_PRIVATE, copy1.getSharing());

      // check the things that should match
      Assert.assertEquals(copy1.getCategory(), original.getCategory());
      Assert.assertEquals(copy1.getClassification(), original.getClassification());
      Assert.assertEquals(copy1.getDescription(), original.getDescription());
      Assert.assertEquals(copy1.getDisplayRows(), original.getDisplayRows());
      Assert.assertEquals(copy1.getItemText(), original.getItemText());
      Assert.assertEquals(copy1.getScaleDisplaySetting(), original.getScaleDisplaySetting());
      Assert.assertEquals(copy1.getUsesNA(), original.getUsesNA());

      // check that the scale was copied correctly also
      Assert.assertNotNull(original.getScale());
      Assert.assertNotNull(copy1.getScale());
      Assert.assertNotSame(copy1.getScale().getId(), original.getScale().getId());
      for (int i = 0; i < copy1.getScale().getOptions().length; i++) {
         Assert.assertEquals(copy1.getScale().getOptions()[i], original.getScale().getOptions()[i]);
      }

      // now do a copy without children
      original = etdl.item1;
      itemIds = new Long[] {original.getId()};
      copiedIds = authoringService.copyItems(itemIds, EvalTestDataLoad.MAINT_USER_ID, true, false);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(itemIds.length, copiedIds.length);
      EvalItem copy2 = (EvalItem) evaluationDao.findById(EvalItem.class, copiedIds[0]);
      Assert.assertNotNull(copy2);

      // verify the copy worked
      // check the things that should differ
      Assert.assertNotSame(copy2.getId(), original.getId());
      Assert.assertEquals(copy2.getCopyOf(), original.getId());
      Assert.assertEquals(EvalTestDataLoad.MAINT_USER_ID, copy2.getOwner());
      Assert.assertEquals(copy2.isHidden(), true);
      Assert.assertEquals(copy2.getExpert(), Boolean.FALSE);
      Assert.assertEquals(copy2.getExpertDescription(), null);
      Assert.assertEquals(copy2.getLocked(), Boolean.FALSE);
      Assert.assertEquals(EvalConstants.SHARING_PRIVATE, copy2.getSharing());

      // check the things that should match
      Assert.assertEquals(copy2.getCategory(), original.getCategory());
      Assert.assertEquals(copy2.getClassification(), original.getClassification());
      Assert.assertEquals(copy2.getDescription(), original.getDescription());
      Assert.assertEquals(copy2.getDisplayRows(), original.getDisplayRows());
      Assert.assertEquals(copy2.getItemText(), original.getItemText());
      Assert.assertEquals(copy2.getScaleDisplaySetting(), original.getScaleDisplaySetting());
      Assert.assertEquals(copy2.getUsesNA(), original.getUsesNA());

      // check that the scale was used but not copied
      Assert.assertNotNull(copy2.getScale());
      Assert.assertEquals(copy2.getScale().getId(), original.getScale().getId());



      // copy a single item (text item this time), true for child copy even though there are no children should be ok
      original = etdl.item5;
      itemIds = new Long[] {original.getId()};
      copiedIds = authoringService.copyItems(itemIds, EvalTestDataLoad.MAINT_USER_ID, true, true);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(itemIds.length, copiedIds.length);
      EvalItem copy3 = (EvalItem) evaluationDao.findById(EvalItem.class, copiedIds[0]);
      Assert.assertNotNull(copy3);

      // verify the copy worked
      // check the things that should differ
      Assert.assertNotSame(copy3.getId(), original.getId());
      Assert.assertEquals(copy3.getCopyOf(), original.getId());
      Assert.assertEquals(EvalTestDataLoad.MAINT_USER_ID, copy3.getOwner());
      Assert.assertEquals(copy3.isHidden(), true);
      Assert.assertEquals(copy3.getExpert(), Boolean.FALSE);
      Assert.assertEquals(copy3.getExpertDescription(), null);
      Assert.assertEquals(copy3.getLocked(), Boolean.FALSE);
      Assert.assertEquals(EvalConstants.SHARING_PRIVATE, copy3.getSharing());

      // check the things that should match
      Assert.assertEquals(copy3.getCategory(), original.getCategory());
      Assert.assertEquals(copy3.getClassification(), original.getClassification());
      Assert.assertEquals(copy3.getDescription(), original.getDescription());
      Assert.assertEquals(copy3.getDisplayRows(), original.getDisplayRows());
      Assert.assertEquals(copy3.getItemText(), original.getItemText());
      Assert.assertEquals(copy3.getScaleDisplaySetting(), original.getScaleDisplaySetting());
      Assert.assertEquals(copy3.getUsesNA(), original.getUsesNA());

      // check we can copy a bunch of things
      itemIds = new Long[] {etdl.item2.getId(), etdl.item3.getId(), etdl.item4.getId()};
      copiedIds = authoringService.copyItems(itemIds, EvalTestDataLoad.MAINT_USER_ID, false, true);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(itemIds.length, copiedIds.length);
      for( Long copiedId : copiedIds ) {
         Assert.assertNotNull(evaluationDao.findById(EvalItem.class, copiedId));
      }

      // check we can copy a bunch of things (without children)
      itemIds = new Long[] {etdl.item2.getId(), etdl.item4.getId(), etdl.item6.getId()};
      copiedIds = authoringService.copyItems(itemIds, EvalTestDataLoad.MAINT_USER_ID, false, false);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(itemIds.length, copiedIds.length);
      for( Long copiedId : copiedIds ) {
         Assert.assertNotNull(evaluationDao.findById(EvalItem.class, copiedId));
       }

      // check that invalid itemId causes exception
      itemIds = new Long[] {etdl.item2.getId(), EvalTestDataLoad.INVALID_LONG_ID};
      try {
         authoringService.copyItems(itemIds, EvalTestDataLoad.MAINT_USER_ID, false, true);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }
   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalAuthoringServiceImpl#copyTemplateItems(java.lang.Long[], java.lang.String, boolean, Long, boolean)}.
    */
   @Test
   public void testCopyTemplateItems() {
      Long[] copiedIds;
      Long[] templateItemIds;

      // copy a single templateItem
      EvalTemplateItem original = etdl.templateItem1U;
      templateItemIds = new Long[] {original.getId()};
      copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, true, null, true);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(templateItemIds.length, copiedIds.length);
      EvalTemplateItem copy1 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, copiedIds[0]);
      Assert.assertNotNull(copy1);

      // verify the copy worked
      // check the things that should differ
      Assert.assertNotSame(copy1.getId(), original.getId());
      Assert.assertEquals(original.getId(), copy1.getCopyOf());
      Assert.assertEquals(EvalTestDataLoad.MAINT_USER_ID, copy1.getOwner());
      Assert.assertEquals(true, copy1.isHidden());
      Assert.assertTrue(original.getDisplayOrder() < copy1.getDisplayOrder());

      // check the things that should match
      Assert.assertEquals(original.getBlockId(), copy1.getBlockId());
      Assert.assertEquals(original.getBlockParent(), copy1.getBlockParent());
      Assert.assertEquals(original.getCategory(), copy1.getCategory());
      Assert.assertEquals(original.getDisplayRows(), copy1.getDisplayRows());
      Assert.assertEquals(original.getHierarchyLevel(), copy1.getHierarchyLevel());
      Assert.assertEquals(original.getHierarchyNodeId(), copy1.getHierarchyNodeId());
      //Assert.assertEquals(original.getResultsSharing(), copy1.getResultsSharing()); // fixed up
      Assert.assertEquals(original.getScaleDisplaySetting(), copy1.getScaleDisplaySetting());
      Assert.assertEquals(original.getUsesNA(), copy1.getUsesNA());

      Assert.assertEquals(original.getTemplate().getId(), copy1.getTemplate().getId());

      // check that the item was copied correctly also
      Assert.assertNotNull(copy1.getItem());
      Assert.assertNotNull(original.getItem());
      Assert.assertNotSame(original.getItem().getId(), copy1.getItem().getId());
      Assert.assertEquals(original.getItem().getItemText(), copy1.getItem().getItemText());

      // now do a copy without children
      original = etdl.templateItem1P;
      templateItemIds = new Long[] {original.getId()};
      copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, true, null, false);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(templateItemIds.length, copiedIds.length);
      EvalTemplateItem copy2 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, copiedIds[0]);
      Assert.assertNotNull(copy2);

      // verify the copy worked
      // check the things that should differ
      Assert.assertNotSame(copy2.getId(), original.getId());
      Assert.assertEquals(copy2.getCopyOf(), original.getId());
      Assert.assertEquals(EvalTestDataLoad.MAINT_USER_ID, copy2.getOwner());
      Assert.assertEquals(copy2.isHidden(), true);
      Assert.assertTrue(original.getDisplayOrder() < copy2.getDisplayOrder());

      // check the things that should match
      Assert.assertEquals(copy2.getBlockId(), original.getBlockId());
      Assert.assertEquals(copy2.getBlockParent(), original.getBlockParent());
      Assert.assertEquals(copy2.getCategory(), original.getCategory());
      Assert.assertEquals(copy2.getDisplayRows(), original.getDisplayRows());
      Assert.assertEquals(copy2.getHierarchyLevel(), original.getHierarchyLevel());
      Assert.assertEquals(copy2.getHierarchyNodeId(), original.getHierarchyNodeId());
      Assert.assertEquals(copy2.getScaleDisplaySetting(), original.getScaleDisplaySetting());
      Assert.assertEquals(copy2.getUsesNA(), original.getUsesNA());

      Assert.assertEquals(copy2.getTemplate().getId(), original.getTemplate().getId());

      // check that the item was used but not copied
      Assert.assertNotNull(copy2.getItem());
      Assert.assertEquals(copy2.getItem().getId(), original.getItem().getId());


      // only 1 countable item in the block template
      Assert.assertEquals( 1, authoringService.getItemCountForTemplate(etdl.templateAdminBlock.getId()) );

      // test out copying a complete block
      templateItemIds = new Long[] {etdl.templateItem9B.getId(), etdl.templateItem2B.getId(), etdl.templateItem3B.getId()};
      copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, true, null, false);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(templateItemIds.length, copiedIds.length);
      List<EvalTemplateItem> templateItems = evaluationDao.findBySearch(EvalTemplateItem.class, new Search("id", copiedIds) );
      Assert.assertNotNull(templateItems);
      Assert.assertEquals(templateItemIds.length, templateItems.size());

      // verify the copy worked
      Long blockParentId = null;
      for (EvalTemplateItem templateItem : templateItems) {
         Assert.assertNotNull(templateItem.getBlockParent());
         if (templateItem.getBlockParent()) {
            Assert.assertNull(templateItem.getBlockId());
            blockParentId = templateItem.getId();
         } else {
            Assert.assertNotNull(templateItem.getBlockId());            
         }
      }

      for (EvalTemplateItem templateItem : templateItems) {
         Assert.assertEquals(EvalTestDataLoad.MAINT_USER_ID, templateItem.getOwner());
         Assert.assertEquals(templateItem.isHidden(), true);
         Assert.assertEquals(copy2.getTemplate().getId(), original.getTemplate().getId());
         if (templateItem.getBlockParent()) {
            // check the things that should differ
            Assert.assertNotSame(templateItem.getId(), etdl.templateItem9B.getId());
            Assert.assertEquals(templateItem.getCopyOf(), etdl.templateItem9B.getId());
         } else {
            // check the block is assigned correctly
            Assert.assertEquals(blockParentId, templateItem.getBlockId());
         }
      }

      // now 2 countable items in the block template
      Assert.assertEquals( 2, authoringService.getItemCountForTemplate(etdl.templateAdminBlock.getId()) );


      // now copy over to a new template
      original = etdl.templateItem1P;
      templateItemIds = new Long[] {original.getId()};
      copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, true, etdl.templateAdmin.getId(), false);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(templateItemIds.length, copiedIds.length);
      EvalTemplateItem copy3 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, copiedIds[0]);
      Assert.assertNotNull(copy3);

      // check the template
      Assert.assertNotSame(copy3.getTemplate().getId(), original.getTemplate().getId());
      Assert.assertEquals(etdl.templateAdmin.getId(), copy3.getTemplate().getId());


      // check we can copy a bunch of things
      templateItemIds = new Long[] {etdl.templateItem2A.getId(), etdl.templateItem3A.getId(), etdl.templateItem5A.getId()};
      copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, false, null, true);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(templateItemIds.length, copiedIds.length);
      for( Long copiedId : copiedIds ) {
         Assert.assertNotNull(evaluationDao.findById(EvalTemplateItem.class, copiedId));
       }

      Assert.assertEquals( 2, authoringService.getItemCountForTemplate(etdl.templateUser.getId()) );

      // check we can copy a bunch of things (without children) into the same template
      templateItemIds = new Long[] {etdl.templateItem1User.getId(), etdl.templateItem5User.getId()};
      copiedIds = authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, false, null, false);
      Assert.assertNotNull(copiedIds);
      Assert.assertEquals(templateItemIds.length, copiedIds.length);
      for( Long copiedId : copiedIds ) {
         Assert.assertNotNull(evaluationDao.findById(EvalTemplateItem.class, copiedId));
      }

      Assert.assertEquals( 4, authoringService.getItemCountForTemplate(etdl.templateUser.getId()) );

      // check that trying to do an inside copy of TIs from multiple templates causes Assert.failure
      templateItemIds = new Long[] {etdl.templateItem1P.getId(), etdl.templateItem2A.getId()};
      try {
         authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, false, null, true);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // check that invalid templateItemId causes exception
      templateItemIds = new Long[] {etdl.templateItem2A.getId(), EvalTestDataLoad.INVALID_LONG_ID};
      try {
         authoringService.copyTemplateItems(templateItemIds, EvalTestDataLoad.MAINT_USER_ID, false, null, true);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalAuthoringServiceImpl#copyTemplate(java.lang.Long, java.lang.String, java.lang.String, boolean, boolean)}.
    */
   @Test
   public void testCopyTemplate() {
      Long copiedId;

      // copy a single template with all children
      // (should create duplicates of all the items and scales)
      EvalTemplate original = etdl.templateUser;
      copiedId = authoringService.copyTemplate(original.getId(), "copy templateUser", EvalTestDataLoad.MAINT_USER_ID, true, true);
      Assert.assertNotNull(copiedId);
      EvalTemplate copy1 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, copiedId);
      Assert.assertNotNull(copy1);

      // verify the copy worked
      // check the things that should differ
      Assert.assertNotSame(original.getId(), copy1.getId());
      Assert.assertEquals(original.getId(), copy1.getCopyOf());
      Assert.assertEquals("copy templateUser", copy1.getTitle());
      Assert.assertEquals(EvalTestDataLoad.MAINT_USER_ID, copy1.getOwner());
      Assert.assertEquals(true, copy1.isHidden());
      Assert.assertEquals(Boolean.FALSE, copy1.getExpert());
      Assert.assertEquals(null, copy1.getExpertDescription());
      Assert.assertEquals(Boolean.FALSE, copy1.getLocked());
      Assert.assertEquals(EvalConstants.SHARING_PRIVATE, copy1.getSharing());

      // check the things that should match
      Assert.assertEquals(original.getDescription(), copy1.getDescription());
      Assert.assertEquals(original.getType(), copy1.getType());

      // make sure the template items copied
      Assert.assertEquals(original.getTemplateItems().size(), copy1.getTemplateItems().size());
      List<EvalTemplateItem> originalTIs = TemplateItemUtils.makeTemplateItemsList(original.getTemplateItems());
      List<EvalTemplateItem> copyTIs = TemplateItemUtils.makeTemplateItemsList(copy1.getTemplateItems());
      Assert.assertEquals(original.getTemplateItems().size(), originalTIs.size());
      Assert.assertEquals(originalTIs.size(), copyTIs.size());
      for (int i = 0; i < originalTIs.size(); i++) {
         EvalTemplateItem originalTI = originalTIs.get(i);
         EvalTemplateItem copyTI = copyTIs.get(i);
         Assert.assertNotSame(originalTI.getId(), copyTI.getId());
         Assert.assertEquals(originalTI.getDisplayOrder(), copyTI.getDisplayOrder());
         // now check the item underneath is a copy of the same item
         Assert.assertEquals(originalTI.getItem().getId(), copyTI.getItem().getCopyOf());
      }

      // test copying without children (all TIs have to be copies as they cannot be shared but the things underneath should not copy)
      original = etdl.templatePublic;
      copiedId = authoringService.copyTemplate(original.getId(), "", EvalTestDataLoad.MAINT_USER_ID, true, false);
      Assert.assertNotNull(copiedId);
      EvalTemplate copy2 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, copiedId);
      Assert.assertNotNull(copy2);

      // verify the copy worked
      // check the things that should differ
      Assert.assertNotSame(original.getId(), copy2.getId());
      Assert.assertEquals(original.getId(), copy2.getCopyOf());
      Assert.assertNotSame(original.getTitle(), copy2.getTitle());
      Assert.assertEquals(EvalTestDataLoad.MAINT_USER_ID, copy2.getOwner());
      Assert.assertEquals(true, copy2.isHidden());
      Assert.assertEquals(Boolean.FALSE, copy2.getExpert());
      Assert.assertEquals(null, copy2.getExpertDescription());
      Assert.assertEquals(Boolean.FALSE, copy2.getLocked());
      Assert.assertEquals(EvalConstants.SHARING_PRIVATE, copy2.getSharing());

      // check the things that should match
      Assert.assertEquals(original.getDescription(), copy2.getDescription());
      Assert.assertEquals(original.getType(), copy2.getType());

      // make sure the template items copied
      Assert.assertEquals(original.getTemplateItems().size(), copy2.getTemplateItems().size());
      originalTIs = TemplateItemUtils.makeTemplateItemsList(original.getTemplateItems());
      copyTIs = TemplateItemUtils.makeTemplateItemsList(copy2.getTemplateItems());
      Assert.assertEquals(original.getTemplateItems().size(), originalTIs.size());
      Assert.assertEquals(originalTIs.size(), copyTIs.size());
      for (int i = 0; i < originalTIs.size(); i++) {
         EvalTemplateItem originalTI = originalTIs.get(i);
         EvalTemplateItem copyTI = copyTIs.get(i);
         Assert.assertNotSame(originalTI.getId(), copyTI.getId());
         Assert.assertEquals(originalTI.getDisplayOrder(), copyTI.getDisplayOrder());
         // now check the item underneath is the same item (and not a copy)
         Assert.assertEquals(originalTI.getItem().getId(), copyTI.getItem().getId());
      }

      // make sure title generation works
      original = etdl.templateUnused;
      copiedId = authoringService.copyTemplate(original.getId(), "", EvalTestDataLoad.MAINT_USER_ID, true, true);
      Assert.assertNotNull(copiedId);
      EvalTemplate copy3 = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, copiedId);
      Assert.assertNotNull(copy3);
      Assert.assertNotNull(copy3.getTitle());

      // check that invalid templateid causes death
      try {
         authoringService.copyTemplate(EvalTestDataLoad.INVALID_LONG_ID, null, EvalTestDataLoad.MAINT_USER_ID, true, true);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }
   }


   @Test
   public void testGetItemsUsingScale() {
      List<EvalItem> items;

      items = authoringService.getItemsUsingScale(etdl.scale1.getId());
      Assert.assertNotNull(items);
      Assert.assertEquals(5, items.size());

      items = authoringService.getItemsUsingScale(etdl.scale4.getId());
      Assert.assertNotNull(items);
      Assert.assertEquals(0, items.size());

      // check that invalid id causes death
      try {
         authoringService.getItemsUsingScale(EvalTestDataLoad.INVALID_LONG_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }
   }
   
   @Test
   public void testGetTemplatesUsingItem() {
      List<EvalTemplate> templates;

      templates = authoringService.getTemplatesUsingItem(etdl.item1.getId());
      Assert.assertNotNull(templates);
      Assert.assertEquals(3, templates.size());

      templates = authoringService.getTemplatesUsingItem(etdl.item11.getId());
      Assert.assertNotNull(templates);
      Assert.assertEquals(0, templates.size());

      // check that invalid id causes death
      try {
         authoringService.getTemplatesUsingItem(EvalTestDataLoad.INVALID_LONG_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }
   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalAuthoringServiceImpl#getAutoUseTemplateItems(java.lang.String)}.
    */
   @Test
   public void testGetAutoUseTemplateItems() {
      List<EvalTemplateItem> items;
      List<Long> ids;

      // positive tests
      items = authoringService.getAutoUseTemplateItems(EvalTestDataLoad.AUTO_USE_TAG, null, null);
      Assert.assertNotNull(items);
      Assert.assertEquals(3, items.size());
      ids = EvalTestDataLoad.makeIdList(items);
      Assert.assertEquals(etdl.templateItem1U.getId(), ids.get(0));
      Assert.assertEquals(etdl.templateItem3U.getId(), ids.get(1));
      Assert.assertEquals(etdl.templateItem5U.getId(), ids.get(2));

      items = authoringService.getAutoUseTemplateItems(null, EvalTestDataLoad.AUTO_USE_TAG, null);
      Assert.assertNotNull(items);
      Assert.assertEquals(2, items.size());
      ids = EvalTestDataLoad.makeIdList(items);
      Assert.assertEquals(etdl.templateItem2A.getId(), ids.get(0));
      Assert.assertEquals(etdl.templateItem6UU.getId(), ids.get(1));

      items = authoringService.getAutoUseTemplateItems(null, null, EvalTestDataLoad.AUTO_USE_TAG);
      Assert.assertNotNull(items);
      Assert.assertEquals(1, items.size());
      Assert.assertEquals(etdl.item4.getId(), items.get(0).getItem().getId());

      items = authoringService.getAutoUseTemplateItems(EvalTestDataLoad.AUTO_USE_TAG, EvalTestDataLoad.AUTO_USE_TAG, EvalTestDataLoad.AUTO_USE_TAG);
      Assert.assertNotNull(items);
      ids = EvalTestDataLoad.makeIdList(items);
      Assert.assertEquals(6, items.size());
      Assert.assertEquals(etdl.templateItem1U.getId(), ids.get(0));
      Assert.assertEquals(etdl.templateItem3U.getId(), ids.get(1));
      Assert.assertEquals(etdl.templateItem5U.getId(), ids.get(2));
      Assert.assertEquals(etdl.templateItem2A.getId(), ids.get(3));
      Assert.assertEquals(etdl.templateItem6UU.getId(), ids.get(4));
      Assert.assertNull(items.get(5).getId());


      // negative tests
      items = authoringService.getAutoUseTemplateItems(null, null, null);
      Assert.assertNotNull(items);
      Assert.assertEquals(0, items.size());

      items = authoringService.getAutoUseTemplateItems("UNKNOWN", null, null);
      Assert.assertNotNull(items);
      Assert.assertEquals(0, items.size());

      items = authoringService.getAutoUseTemplateItems("UNKNOWN", "UNKNOWN", "UNKNOWN");
      Assert.assertNotNull(items);
      Assert.assertEquals(0, items.size());

      // no exceptions thrown
   }
   
   @Test
   public void testDoAutoUseInsertion() {
      List<EvalTemplateItem> items;
      List<EvalTemplateItem> currentItems;
      Long templateId;
      int displayOrder;

      // check out the template first
      templateId = etdl.templateUser.getId();
      authoringService.getTemplateById(templateId).setLocked(false); // make this unlocked
      currentItems = authoringService.getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {});
      Assert.assertEquals(2, currentItems.size());

      // test insertion without save
      items = authoringService.doAutoUseInsertion(EvalTestDataLoad.AUTO_USE_TAG, templateId, EvalConstants.EVALUATION_AUTOUSE_INSERTION_AFTER, false);
      Assert.assertNotNull(items);
      Assert.assertEquals(8, items.size()); // + 6 autoUse items
      // check the order
      displayOrder = 1;
      for (EvalTemplateItem item : items) {
         if (displayOrder >= 3) {
            Assert.assertEquals(EvalTestDataLoad.AUTO_USE_TAG, item.getAutoUseInsertionTag());
         } else {
            Assert.assertNull(item.getAutoUseInsertionTag());
         }
         Assert.assertEquals(new Integer(displayOrder++), item.getDisplayOrder());
      }

      currentItems = authoringService.getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {});
      Assert.assertEquals(2, currentItems.size());

      // test insertion without save in different order
      items = authoringService.doAutoUseInsertion(EvalTestDataLoad.AUTO_USE_TAG, templateId, EvalConstants.EVALUATION_AUTOUSE_INSERTION_BEFORE, false);
      Assert.assertNotNull(items);
      Assert.assertEquals(8, items.size()); // + 6 autoUse items
      // check the order
      displayOrder = 1;
      for (EvalTemplateItem item : items) {
         // check for autoUse tag
         if (displayOrder <= 6) {
            Assert.assertEquals(EvalTestDataLoad.AUTO_USE_TAG, item.getAutoUseInsertionTag());
         } else {
            Assert.assertNull(item.getAutoUseInsertionTag());
         }
         Assert.assertEquals(new Integer(displayOrder++), item.getDisplayOrder());
      }

      // test insertion with save
      items = authoringService.doAutoUseInsertion(EvalTestDataLoad.AUTO_USE_TAG, templateId, EvalConstants.EVALUATION_AUTOUSE_INSERTION_BEFORE, true);
      Assert.assertNotNull(items);
      Assert.assertEquals(8, items.size()); // + 6 autoUse items
      displayOrder = 1;
      for (EvalTemplateItem item : items) {
         if (displayOrder <= 6) {
            Assert.assertEquals(EvalTestDataLoad.AUTO_USE_TAG, item.getAutoUseInsertionTag());
         } else {
            Assert.assertNull(item.getAutoUseInsertionTag());
         }
         Assert.assertEquals(new Integer(displayOrder++), item.getDisplayOrder());
      }

      currentItems = authoringService.getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {});
      Assert.assertEquals(8, currentItems.size());
      currentItems = TemplateItemUtils.orderTemplateItems(currentItems, false);
      for (int i = 0; i < items.size(); i++) {
         Assert.assertEquals(currentItems.get(i).getId(), items.get(i).getId());
      }      

      // test nothing to insert
      items = authoringService.doAutoUseInsertion("FAKE", templateId, EvalConstants.EVALUATION_AUTOUSE_INSERTION_AFTER, false);
      Assert.assertNull(items);

   }

}
