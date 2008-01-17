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
import org.sakaiproject.evaluation.model.EvalAnswer;
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
 * Testing for the Evaluation Data Access Layer
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationDaoImplTest extends AbstractTransactionalSpringContextTests {

   protected EvaluationDao evaluationDao;

   private EvalTestDataLoad etdl;

   private EvalScale scaleLocked;
   private EvalItem itemLocked;
   private EvalItem itemUnlocked;
   private EvalEvaluation evalUnLocked;

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
         throw new NullPointerException("DAO could not be retrieved from spring evalGroupId");
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

      // init the test class if needed

   }

   // run this before each test starts and as part of the transaction
   protected void onSetUpInTransaction() {
      // preload additional data if desired
      String[] optionsA = {"Male", "Female", "Unknown"};
      scaleLocked = new EvalScale(new Date(), EvalTestDataLoad.ADMIN_USER_ID, "Scale Alpha", 
            EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PRIVATE, EvalTestDataLoad.NOT_EXPERT, 
            "description", EvalConstants.SCALE_IDEAL_NONE, optionsA, EvalTestDataLoad.LOCKED);
      evaluationDao.save( scaleLocked );

      itemLocked = new EvalItem(new Date(), EvalTestDataLoad.MAINT_USER_ID, "Header type locked", 
            EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_HEADER, EvalTestDataLoad.NOT_EXPERT);
      itemLocked.setLocked(EvalTestDataLoad.LOCKED);
      evaluationDao.save( itemLocked );

      itemUnlocked = new EvalItem(new Date(), EvalTestDataLoad.MAINT_USER_ID, "Header type locked", 
            EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_HEADER, EvalTestDataLoad.NOT_EXPERT);
      itemUnlocked.setScale(etdl.scale2);
      itemUnlocked.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL );
      itemUnlocked.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
      itemUnlocked.setLocked(EvalTestDataLoad.UNLOCKED);
      evaluationDao.save( itemUnlocked );

      evalUnLocked = new EvalEvaluation(new Date(), EvalTestDataLoad.MAINT_USER_ID, "Eval active not taken", null, 
            etdl.yesterday, etdl.tomorrow, etdl.tomorrow, etdl.threeDaysFuture, null, null,
            EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.INSTRUCTOR_OPT_IN, 
            new Integer(1), null, null, null, null, etdl.templatePublicUnused, null, null,
            Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, EvalTestDataLoad.UNLOCKED,
            EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null);
      evaluationDao.save( evalUnLocked );

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
      List<EvalTemplate> l = null;
      List<Long> ids = null;

      // all templates visible to user
      l = evaluationDao.getVisibleTemplates(EvalTestDataLoad.USER_ID, 
            new String[] {EvalConstants.SHARING_PUBLIC}, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(5, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
      Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

      // all templates visible to maint user
      l = evaluationDao.getVisibleTemplates(EvalTestDataLoad.MAINT_USER_ID,
            new String[] {EvalConstants.SHARING_PUBLIC}, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(4, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
      Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

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
      Assert.assertEquals(6, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateAdmin.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateAdminNoItems.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));

      // all private non-empty templates
      l = evaluationDao.getVisibleTemplates(null,
            new String[] {}, false);
      Assert.assertNotNull(l);
      Assert.assertEquals(5, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateAdmin.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));

      // all public templates
      l = evaluationDao.getVisibleTemplates("", 
            new String[] {EvalConstants.SHARING_PUBLIC}, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
      Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

      // all templates (admin would use this)
      l = evaluationDao.getVisibleTemplates(null, 
            new String[] {EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(9, l.size());

      // all non-empty templates (admin would use this)
      l = evaluationDao.getVisibleTemplates(null, 
            new String[] {EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, false);
      Assert.assertNotNull(l);
      Assert.assertEquals(8, l.size());

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
      Assert.assertEquals(5, count);

      // all templates visible to maint user
      count = evaluationDao.countVisibleTemplates(EvalTestDataLoad.MAINT_USER_ID, 
            new String[] {EvalConstants.SHARING_PUBLIC}, true);
      Assert.assertEquals(4, count);

      // all templates owned by USER
      count = evaluationDao.countVisibleTemplates(EvalTestDataLoad.USER_ID, 
            new String[] {}, true);
      Assert.assertEquals(2, count);

      // all private templates (admin only)
      count = evaluationDao.countVisibleTemplates(null, 
            new String[] {}, true);
      Assert.assertEquals(6, count);

      // all private non-empty templates (admin only)
      count = evaluationDao.countVisibleTemplates(null, 
            new String[] {}, false);
      Assert.assertEquals(5, count);

      // all public templates
      count = evaluationDao.countVisibleTemplates("", new String[] {EvalConstants.SHARING_PUBLIC}, true);
      Assert.assertEquals(3, count);

      // all templates (admin would use this)
      count = evaluationDao.countVisibleTemplates(null, 
            new String[] {EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, true);
      Assert.assertEquals(9, count);

      // all non-empty templates (admin would use this)
      count = evaluationDao.countVisibleTemplates(null, 
            new String[] {EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, false);
      Assert.assertEquals(8, count);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#getActiveEvaluationsByContexts(java.lang.String[])}.
    */
   public void testGetActiveEvaluationsByContexts() {
      Set<EvalEvaluation> s = null;
      List<Long> ids = null;

      // test getting evaluations by evalGroupId
      s = evaluationDao.getEvaluationsByEvalGroups(
            new String[] {EvalTestDataLoad.SITE1_REF}, false, true, false);
      Assert.assertNotNull(s);
      Assert.assertEquals(4, s.size());
      ids = EvalTestDataLoad.makeIdList(s);
      Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
      Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
      Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
      Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));

      s = evaluationDao.getEvaluationsByEvalGroups(
            new String[] {EvalTestDataLoad.SITE2_REF}, false, true, false);
      Assert.assertNotNull(s);
      Assert.assertEquals(3, s.size());
      ids = EvalTestDataLoad.makeIdList(s);
      Assert.assertTrue(! ids.contains( etdl.evaluationActive.getId() ));
      Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
      Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
      Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

      s = evaluationDao.getEvaluationsByEvalGroups(
            new String[] {"invalid evalGroupId"}, false, true, false);
      Assert.assertNotNull(s);
      Assert.assertEquals(0, s.size());

      // test that the get active part works
      s = evaluationDao.getEvaluationsByEvalGroups(
            new String[] {EvalTestDataLoad.SITE1_REF}, true, true, false);
      Assert.assertNotNull(s);
      Assert.assertEquals(2, s.size());
      ids = EvalTestDataLoad.makeIdList(s);
      Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
      Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

      s = evaluationDao.getEvaluationsByEvalGroups(
            new String[] {EvalTestDataLoad.SITE2_REF}, true, true, false);
      Assert.assertNotNull(s);
      Assert.assertEquals(0, s.size());

      // test getting from an invalid evalGroupId
      s = evaluationDao.getEvaluationsByEvalGroups(
            new String[] {EvalTestDataLoad.INVALID_CONTEXT}, true, true, false);
      Assert.assertNotNull(s);
      Assert.assertEquals(0, s.size());		

      // test getting all anonymous evals
      s = evaluationDao.getEvaluationsByEvalGroups(
            new String[] {}, false, false, true);
      Assert.assertNotNull(s);
      Assert.assertEquals(1, s.size());		
      ids = EvalTestDataLoad.makeIdList(s);
      Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

      // test invalid
      try {
         s = evaluationDao.getEvaluationsByEvalGroups(null, false, true, false);
         Assert.fail("Should have thrown an exception");
      } catch (RuntimeException e) {
         Assert.assertNotNull(e);
      }
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#getAnswers(java.lang.Long, java.lang.Long)}.
    */
   public void testGetAnswers() {
      Set<EvalAnswer> s = null;
      List<EvalAnswer> l = null;
      List<Long> ids = null;

      s = etdl.response2.getAnswers();
      Assert.assertNotNull(s);
      Assert.assertEquals(2, s.size());
      ids = EvalTestDataLoad.makeIdList(s);
      Assert.assertTrue(ids.contains( etdl.answer2_2.getId() ));
      Assert.assertTrue(ids.contains( etdl.answer2_5.getId() ));

      l = evaluationDao.getAnswers(etdl.item2.getId(), etdl.evaluationClosed.getId(), null);
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.answer2_2.getId() ));
      Assert.assertTrue(ids.contains( etdl.answer3_2.getId() ));

      // test restricting to groups
      l = evaluationDao.getAnswers(etdl.item2.getId(), etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF});
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.answer2_2.getId() ));

      l = evaluationDao.getAnswers(etdl.item2.getId(), etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF});
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.answer3_2.getId() ));

      l = evaluationDao.getAnswers(etdl.item5.getId(), etdl.evaluationClosed.getId(), null);
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.answer2_5.getId() ));

      // test item that is not in this evaluation
      l = evaluationDao.getAnswers(etdl.item3.getId(), etdl.evaluationClosed.getId(), null);
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      // test invalid eval id
      // TODO - this should probably throw an exception
      l = evaluationDao.getAnswers(etdl.item1.getId(), Long.valueOf(999), null);
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      // test invalid item id
      // TODO - this should probably throw an exception
      l = evaluationDao.getAnswers(Long.valueOf(999), etdl.evaluationClosed.getId(), null);
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#removeTemplateItems(org.sakaiproject.evaluation.model.EvalTemplateItem[])}.
    */
   public void testRemoveTemplateItems() {

      // test removing a single templateItem
      EvalTemplateItem eti1 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, etdl.templateItem1User.getId());

      // verify that the item/template link exists before removal
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

      // test removing templateItem OK
      evaluationDao.removeTemplateItems( new EvalTemplateItem[] {etdl.templateItem1User} );
      Assert.assertNull( evaluationDao.findById(EvalTemplateItem.class, etdl.templateItem1User.getId()) );

      // verify that the item/template link no longer exists
      Assert.assertNotNull( eti1.getItem().getTemplateItems() );
      Assert.assertNotNull( eti1.getTemplate().getTemplateItems() );
      Assert.assertFalse( eti1.getItem().getTemplateItems().isEmpty() );
      Assert.assertFalse( eti1.getTemplate().getTemplateItems().isEmpty() );
      Assert.assertEquals( itemsSize-1, eti1.getItem().getTemplateItems().size() );
      Assert.assertEquals( templatesSize-1, eti1.getTemplate().getTemplateItems().size() );
      Assert.assertTrue(! eti1.getItem().getTemplateItems().contains( eti1 ) );
      Assert.assertTrue(! eti1.getTemplate().getTemplateItems().contains( eti1 ) );

      // test removing a group of templateItems (item 3 and 5 from UnUsed)
      EvalTemplateItem eti3 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, etdl.templateItem3U.getId());
      EvalTemplateItem eti5 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, etdl.templateItem5U.getId());

      // verify that the item/template link exists before removal
      Assert.assertNotNull( eti3 );
      Assert.assertNotNull( eti3.getItem() );
      Assert.assertNotNull( eti3.getTemplate() );
      Assert.assertNotNull( eti3.getItem().getTemplateItems() );
      Assert.assertNotNull( eti3.getTemplate().getTemplateItems() );
      Assert.assertFalse( eti3.getItem().getTemplateItems().isEmpty() );
      Assert.assertFalse( eti3.getTemplate().getTemplateItems().isEmpty() );
      Assert.assertTrue( eti3.getItem().getTemplateItems().contains( eti3 ) );
      Assert.assertTrue( eti3.getTemplate().getTemplateItems().contains( eti3 ) );
      int itemsSize3 = eti3.getItem().getTemplateItems().size();

      Assert.assertNotNull( eti5 );
      Assert.assertNotNull( eti5.getItem() );
      Assert.assertNotNull( eti5.getTemplate() );
      Assert.assertNotNull( eti5.getItem().getTemplateItems() );
      Assert.assertNotNull( eti5.getTemplate().getTemplateItems() );
      Assert.assertFalse( eti5.getItem().getTemplateItems().isEmpty() );
      Assert.assertFalse( eti5.getTemplate().getTemplateItems().isEmpty() );
      Assert.assertTrue( eti5.getItem().getTemplateItems().contains( eti5 ) );
      Assert.assertTrue( eti5.getTemplate().getTemplateItems().contains( eti5 ) );
      int itemsSize5 = eti5.getItem().getTemplateItems().size();

      // test removing templateItem OK
      evaluationDao.removeTemplateItems( new EvalTemplateItem[] {etdl.templateItem3U, etdl.templateItem5U} );
      Assert.assertNull( evaluationDao.findById(EvalTemplateItem.class, etdl.templateItem3U.getId()) );
      Assert.assertNull( evaluationDao.findById(EvalTemplateItem.class, etdl.templateItem5U.getId()) );

      // verify that the item/template link no longer exists
      Assert.assertNotNull( eti3.getItem().getTemplateItems() );
      Assert.assertFalse( eti3.getItem().getTemplateItems().isEmpty() );
      Assert.assertEquals( itemsSize3-1, eti3.getItem().getTemplateItems().size() );
      Assert.assertTrue(! eti3.getItem().getTemplateItems().contains( eti3 ) );

      Assert.assertNotNull( eti5.getItem().getTemplateItems() );
      Assert.assertFalse( eti5.getItem().getTemplateItems().isEmpty() );
      Assert.assertEquals( itemsSize5-1, eti5.getItem().getTemplateItems().size() );
      Assert.assertTrue(! eti5.getItem().getTemplateItems().contains( eti5 ) );

      // should be no items left in this template now
      Assert.assertNotNull( eti3.getTemplate().getTemplateItems() );
      Assert.assertTrue( eti3.getTemplate().getTemplateItems().isEmpty() );
      EvalTemplate template = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, eti3.getTemplate().getId());
      Assert.assertNotNull( template );
      Assert.assertNotNull( template.getTemplateItems() );
      Assert.assertTrue( template.getTemplateItems().isEmpty() );

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#getTemplateItemsByTemplate(java.lang.Long, java.lang.String[], java.lang.String[], java.lang.String[])}.
    */
   public void testGetTemplateItemsByTemplate() {
      List<EvalTemplateItem> l = null;
      List<Long> ids = null;

      // test the basic return of items in the template
      l = evaluationDao.getTemplateItemsByTemplate(etdl.templateAdmin.getId(), 
            null, null, null);
      Assert.assertNotNull(l);
      Assert.assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateItem2A.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem3A.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem5A.getId() ));

      // check that the return order is correct
      Assert.assertEquals( 1, ((EvalTemplateItem)l.get(0)).getDisplayOrder().intValue() );
      Assert.assertEquals( 2, ((EvalTemplateItem)l.get(1)).getDisplayOrder().intValue() );
      Assert.assertEquals( 3, ((EvalTemplateItem)l.get(2)).getDisplayOrder().intValue() );

      // test getting just the top level items
      l = evaluationDao.getTemplateItemsByTemplate(etdl.templateAdminComplex.getId(), 
            null, null, null);
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      // test getting instructor items
      l = evaluationDao.getTemplateItemsByTemplate(etdl.templateAdminComplex.getId(), 
            null, new String[] { EvalTestDataLoad.MAINT_USER_ID }, null);
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateItem10AC1.getId() ));

      // test getting course items
      l = evaluationDao.getTemplateItemsByTemplate(etdl.templateAdminComplex.getId(), 
            null, null, 
            new String[] { EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF });
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateItem10AC2.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem10AC3.getId() ));

      // test getting both together
      l = evaluationDao.getTemplateItemsByTemplate(etdl.templateAdminComplex.getId(), 
            null, new String[] { EvalTestDataLoad.MAINT_USER_ID }, 
            new String[] { EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF });
      Assert.assertNotNull(l);
      Assert.assertEquals(3, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.templateItem10AC1.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem10AC2.getId() ));
      Assert.assertTrue(ids.contains( etdl.templateItem10AC3.getId() ));

      // test that a bunch of invalid stuff simply returns no results
      l = evaluationDao.getTemplateItemsByTemplate(etdl.templateAdminComplex.getId(), 
            new String[] { EvalTestDataLoad.INVALID_CONSTANT_STRING }, 
            new String[] { EvalTestDataLoad.INVALID_CONSTANT_STRING, EvalTestDataLoad.INVALID_CONSTANT_STRING }, 
            new String[] { EvalTestDataLoad.INVALID_CONSTANT_STRING, EvalTestDataLoad.INVALID_CONSTANT_STRING, EvalTestDataLoad.INVALID_CONSTANT_STRING });
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

   }

   public void testGetResponseIds() {
      List<Long> l = null;

      l = evaluationDao.getResponseIds(etdl.evaluationClosed.getId(), null);
      Assert.assertNotNull(l);
      Assert.assertEquals(3, l.size());
      Assert.assertTrue( l.contains(etdl.response2.getId()) );
      Assert.assertTrue( l.contains(etdl.response3.getId()) );
      Assert.assertTrue( l.contains(etdl.response6.getId()) );

      l = evaluationDao.getResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF});
      Assert.assertNotNull(l);
      Assert.assertEquals(3, l.size());
      Assert.assertTrue( l.contains(etdl.response2.getId()) );
      Assert.assertTrue( l.contains(etdl.response3.getId()) );
      Assert.assertTrue( l.contains(etdl.response6.getId()) );

      l = evaluationDao.getResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF});
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      Assert.assertTrue( l.contains(etdl.response2.getId()) );

      // test invalid evalid
      l = evaluationDao.getResponseIds(EvalTestDataLoad.INVALID_LONG_ID, null);
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#getEvalCategories(String)}
    */
   public void testGetEvalCategories() {
      List<String> l = null;

      // test the basic return of categories
      l = evaluationDao.getEvalCategories(null);
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      Assert.assertTrue( l.contains(EvalTestDataLoad.EVAL_CATEGORY_1) );
      Assert.assertTrue( l.contains(EvalTestDataLoad.EVAL_CATEGORY_2) );

      // test the return of cats for a user
      l = evaluationDao.getEvalCategories(EvalTestDataLoad.MAINT_USER_ID);
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      Assert.assertTrue( l.contains(EvalTestDataLoad.EVAL_CATEGORY_1) );

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#getNodeIdForEvalGroup(java.lang.String)}.
    */
   public void testGetNodeIdForEvalGroup() {
      String nodeId = null; 

      nodeId = evaluationDao.getNodeIdForEvalGroup(EvalTestDataLoad.SITE1_REF);
      assertNotNull(nodeId);
      assertEquals(EvalTestDataLoad.NODE_ID1, nodeId);

      nodeId = evaluationDao.getNodeIdForEvalGroup(EvalTestDataLoad.SITE2_REF);
      assertNotNull(nodeId);
      assertEquals(EvalTestDataLoad.NODE_ID1, nodeId);

      nodeId = evaluationDao.getNodeIdForEvalGroup(EvalTestDataLoad.SITE3_REF);
      assertNotNull(nodeId);
      assertEquals(EvalTestDataLoad.NODE_ID2, nodeId);

      nodeId = evaluationDao.getNodeIdForEvalGroup("xxxxxxxxxxxxxxxxx");
      assertNull(nodeId);
   }

   public void testGetTemplateItemsByEvaluation() {
      List<EvalTemplateItem> templateItems = null;
      
      templateItems = evaluationDao.getTemplateItemsByEvaluation(etdl.evaluationActive.getId(), null, null, null);
      assertNotNull(templateItems);
      assertEquals(2, templateItems.size());

      templateItems = evaluationDao.getTemplateItemsByEvaluation(etdl.evaluationClosed.getId(), null, null, null);
      assertNotNull(templateItems);
      assertEquals(3, templateItems.size());

      try {
         templateItems = evaluationDao.getTemplateItemsByEvaluation(EvalTestDataLoad.INVALID_LONG_ID, null, null, null);
         Assert.fail("Should have thrown an exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }
   }

   public void testGetTemplateIdsForEvaluation() {
      List<Long> templateIds = null;

      templateIds = evaluationDao.getTemplateIdsForEvaluation(etdl.evaluationActive.getId());
      assertNotNull(templateIds);
      assertEquals(1, templateIds.size());
      assertTrue( templateIds.contains( etdl.templateUser.getId() ) );

      templateIds = evaluationDao.getTemplateIdsForEvaluation(etdl.evaluationClosed.getId());
      assertNotNull(templateIds);
      assertEquals(2, templateIds.size());
      assertTrue( templateIds.contains( etdl.templateAdmin.getId() ) );
      assertTrue( templateIds.contains( etdl.templateAdminComplex.getId() ) );

      templateIds = evaluationDao.getTemplateIdsForEvaluation(EvalTestDataLoad.INVALID_LONG_ID);
      assertNotNull(templateIds);
      assertEquals(0, templateIds.size());
   }



   // LOCKING tests

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#lockScale(org.sakaiproject.evaluation.model.EvalScale, java.lang.Boolean)}.
    */
   public void testLockScale() {

      // check that locked scale gets unlocked (no locking item)
      Assert.assertTrue( scaleLocked.getLocked().booleanValue() );
      Assert.assertTrue( evaluationDao.lockScale( scaleLocked, Boolean.FALSE ) );
      Assert.assertFalse( scaleLocked.getLocked().booleanValue() );
      // check that unlocking an unlocked scale is not a problem
      Assert.assertFalse( evaluationDao.lockScale( scaleLocked, Boolean.FALSE ) );

      // check that locked scale that is locked by an item cannot be unlocked
      EvalScale scale1 = (EvalScale) evaluationDao.findById(EvalScale.class, etdl.scale1.getId());
      Assert.assertTrue( scale1.getLocked().booleanValue() );
      Assert.assertFalse( evaluationDao.lockScale( scale1, Boolean.FALSE ) );
      Assert.assertTrue( scale1.getLocked().booleanValue() );
      // check that locking a locked scale is not a problem
      Assert.assertFalse( evaluationDao.lockScale( scale1, Boolean.TRUE ) );

      // check that new scale cannot be unlocked
      try {
         evaluationDao.lockScale( 
               new EvalScale(new Date(), 
                     EvalTestDataLoad.ADMIN_USER_ID, "new scale", 
                     EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PRIVATE, Boolean.FALSE),
                     Boolean.FALSE
         );
         Assert.fail("Should have thrown an exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#lockItem(org.sakaiproject.evaluation.model.EvalItem, java.lang.Boolean)}.
    */
   public void testLockItem() {

      // check that unlocked item gets locked (no scale)
      Assert.assertFalse( etdl.item7.getLocked().booleanValue() );
      Assert.assertTrue( evaluationDao.lockItem( etdl.item7, Boolean.TRUE ) );
      Assert.assertTrue( etdl.item7.getLocked().booleanValue() );

      // check that locked item does nothing bad if locked again (no scale, not used)
      Assert.assertTrue( itemLocked.getLocked().booleanValue() );
      Assert.assertFalse( evaluationDao.lockItem( itemLocked, Boolean.TRUE ) );
      Assert.assertTrue( itemLocked.getLocked().booleanValue() );

      // check that locked item gets unlocked (no scale, not used)
      Assert.assertTrue( itemLocked.getLocked().booleanValue() );
      Assert.assertTrue( evaluationDao.lockItem( itemLocked, Boolean.FALSE ) );
      Assert.assertFalse( itemLocked.getLocked().booleanValue() );

      // check that locked item that is locked by a template cannot be unlocked
      Assert.assertTrue( etdl.item1.getLocked().booleanValue() );
      Assert.assertFalse( evaluationDao.lockItem( etdl.item1, Boolean.FALSE ) );
      Assert.assertTrue( etdl.item1.getLocked().booleanValue() );

      // check that locked item that is locked by a template can be locked without exception
      Assert.assertTrue( etdl.item1.getLocked().booleanValue() );
      Assert.assertFalse( evaluationDao.lockItem( etdl.item1, Boolean.TRUE ) );
      Assert.assertTrue( etdl.item1.getLocked().booleanValue() );

      // verify that associated scale is unlocked
      Assert.assertFalse( itemUnlocked.getScale().getLocked().booleanValue() );

      // check that unlocked item gets locked (scale)
      Assert.assertFalse( itemUnlocked.getLocked().booleanValue() );
      Assert.assertTrue( evaluationDao.lockItem( itemUnlocked, Boolean.TRUE ) );
      Assert.assertTrue( itemUnlocked.getLocked().booleanValue() );

      // verify that associated scale gets locked
      Assert.assertTrue( itemUnlocked.getScale().getLocked().booleanValue() );

      // check that locked item gets unlocked (scale)
      Assert.assertTrue( itemUnlocked.getLocked().booleanValue() );
      Assert.assertTrue( evaluationDao.lockItem( itemUnlocked, Boolean.FALSE ) );
      Assert.assertFalse( itemUnlocked.getLocked().booleanValue() );

      // verify that associated scale gets unlocked
      Assert.assertFalse( itemUnlocked.getScale().getLocked().booleanValue() );

      // check that locked item gets unlocked (scale locked by another item)
      Assert.assertTrue( etdl.item4.getScale().getLocked().booleanValue() );
      Assert.assertTrue( evaluationDao.lockItem( etdl.item4, Boolean.TRUE ) );
      Assert.assertTrue( etdl.item4.getLocked().booleanValue() );

      Assert.assertTrue( evaluationDao.lockItem( etdl.item4, Boolean.FALSE ) );
      Assert.assertFalse( etdl.item4.getLocked().booleanValue() );

      // verify that associated scale does not get unlocked
      Assert.assertTrue( etdl.item4.getScale().getLocked().booleanValue() );

      // check that new item cannot be locked/unlocked
      try {
         evaluationDao.lockItem(
               new EvalItem( new Date(), EvalTestDataLoad.ADMIN_USER_ID, 
                     "something", EvalConstants.SHARING_PRIVATE, 
                     EvalConstants.ITEM_TYPE_HEADER, Boolean.FALSE),
                     Boolean.TRUE);
         Assert.fail("Should have thrown an exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#lockTemplate(org.sakaiproject.evaluation.model.EvalTemplate, java.lang.Boolean)}.
    */
   public void testLockTemplate() {

      // check that unlocked template gets locked (no items)
      Assert.assertFalse( etdl.templateAdminNoItems.getLocked().booleanValue() );
      Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateAdminNoItems, Boolean.TRUE ) );
      Assert.assertTrue( etdl.templateAdminNoItems.getLocked().booleanValue() );

      // check that locked template is ok with getting locked again (no problems)
      Assert.assertTrue( etdl.templateAdminNoItems.getLocked().booleanValue() );
      Assert.assertFalse( evaluationDao.lockTemplate( etdl.templateAdminNoItems, Boolean.TRUE ) );
      Assert.assertTrue( etdl.templateAdminNoItems.getLocked().booleanValue() );

      // check that locked template gets unlocked (no items)
      Assert.assertTrue( etdl.templateAdminNoItems.getLocked().booleanValue() );
      Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateAdminNoItems, Boolean.FALSE ) );
      Assert.assertFalse( etdl.templateAdminNoItems.getLocked().booleanValue() );

      // check that locked template that is locked by an evaluation cannot be unlocked
      Assert.assertTrue( etdl.templateUser.getLocked().booleanValue() );
      Assert.assertFalse( evaluationDao.lockTemplate( etdl.templateUser, Boolean.FALSE ) );
      Assert.assertTrue( etdl.templateUser.getLocked().booleanValue() );

      // check that locked template that is locked by an evaluation can be locked without exception
      Assert.assertTrue( etdl.templateUser.getLocked().booleanValue() );
      Assert.assertFalse( evaluationDao.lockTemplate( etdl.templateUser, Boolean.TRUE ) );
      Assert.assertTrue( etdl.templateUser.getLocked().booleanValue() );

      // check that unlocked template gets locked (items)
      Assert.assertFalse( etdl.item6.getLocked().booleanValue() );
      Assert.assertFalse( etdl.templateUserUnused.getLocked().booleanValue() );
      Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateUserUnused, Boolean.TRUE ) );
      Assert.assertTrue( etdl.templateUserUnused.getLocked().booleanValue() );

      // verify that related items are locked also
      Assert.assertTrue( etdl.item6.getLocked().booleanValue() );

      // check that locked template gets unlocked (items)
      Assert.assertTrue( etdl.templateUserUnused.getLocked().booleanValue() );
      Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateUserUnused, Boolean.FALSE ) );
      Assert.assertFalse( etdl.templateUserUnused.getLocked().booleanValue() );

      // verify that related items are unlocked also
      Assert.assertFalse( etdl.item6.getLocked().booleanValue() );

      // check unlocked template with locked items can be locked
      Assert.assertFalse( etdl.templateUnused.getLocked().booleanValue() );
      Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateUnused, Boolean.TRUE ) );
      Assert.assertTrue( etdl.templateUnused.getLocked().booleanValue() );

      // check that locked template gets unlocked (items locked by another template)
      Assert.assertTrue( etdl.item3.getLocked().booleanValue() );
      Assert.assertTrue( etdl.item5.getLocked().booleanValue() );
      Assert.assertTrue( etdl.templateUnused.getLocked().booleanValue() );
      Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateUnused, Boolean.FALSE ) );
      Assert.assertFalse( etdl.templateUnused.getLocked().booleanValue() );

      // verify that associated items locked by other template do not get unlocked
      Assert.assertTrue( etdl.item3.getLocked().booleanValue() );
      Assert.assertTrue( etdl.item5.getLocked().booleanValue() );

      // check that new template cannot be locked/unlocked
      try {
         evaluationDao.lockTemplate(
               new EvalTemplate(new Date(), EvalTestDataLoad.ADMIN_USER_ID, 
                     EvalConstants.TEMPLATE_TYPE_STANDARD, "new template one", 
                     "description", EvalConstants.SHARING_PRIVATE, EvalTestDataLoad.NOT_EXPERT, 
                     "expert desc", null, EvalTestDataLoad.LOCKED),
                     Boolean.TRUE);
         Assert.fail("Should have thrown an exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#lockEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)}.
    */
   public void testLockEvaluation() {

      // check that unlocked evaluation gets locked
      Assert.assertFalse( etdl.templatePublicUnused.getLocked().booleanValue() );
      Assert.assertFalse( evalUnLocked.getLocked().booleanValue() );
      Assert.assertTrue( evaluationDao.lockEvaluation( evalUnLocked ) );
      Assert.assertTrue( evalUnLocked.getLocked().booleanValue() );

      // verify that associated template gets locked
      Assert.assertTrue( etdl.templatePublicUnused.getLocked().booleanValue() );

      // check that new evaluation cannot be locked
      try {
         evaluationDao.lockEvaluation(
               new EvalEvaluation(new Date(), EvalTestDataLoad.MAINT_USER_ID, "Eval new", null, 
                     etdl.tomorrow, etdl.threeDaysFuture, etdl.threeDaysFuture, etdl.fourDaysFuture, null, null,
                     EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.INSTRUCTOR_OPT_IN, 
                     new Integer(1), null, null, null, null, etdl.templatePublic, null, null,
                     Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, EvalTestDataLoad.UNLOCKED,
                     EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null)
         );
         Assert.fail("Should have thrown an exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#isUsedScale(java.lang.Long)}.
    */
   public void testIsUsedScale() {
      Assert.assertTrue( evaluationDao.isUsedScale( etdl.scale1.getId() ) );
      Assert.assertTrue( evaluationDao.isUsedScale( etdl.scale2.getId() ) );
      Assert.assertFalse( evaluationDao.isUsedScale( etdl.scale3.getId() ) );
      Assert.assertFalse( evaluationDao.isUsedScale( etdl.scale4.getId() ) );
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#isUsedItem(java.lang.Long)}.
    */
   public void testIsUsedItem() {
      Assert.assertTrue( evaluationDao.isUsedItem( etdl.item1.getId() ) );
      Assert.assertTrue( evaluationDao.isUsedItem( etdl.item2.getId() ) );
      Assert.assertTrue( evaluationDao.isUsedItem( etdl.item3.getId() ) );
      Assert.assertFalse( evaluationDao.isUsedItem( etdl.item4.getId() ) );
      Assert.assertTrue( evaluationDao.isUsedItem( etdl.item5.getId() ) );
      Assert.assertTrue( evaluationDao.isUsedItem( etdl.item6.getId() ) );
      Assert.assertFalse( evaluationDao.isUsedItem( etdl.item7.getId() ) );
      Assert.assertFalse( evaluationDao.isUsedItem( etdl.item8.getId() ) );
      Assert.assertTrue( evaluationDao.isUsedItem( etdl.item9.getId() ) );
      Assert.assertTrue( evaluationDao.isUsedItem( etdl.item10.getId() ) );
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.dao.impl.EvaluationDaoImpl#isUsedTemplate(java.lang.Long)}.
    */
   public void testIsUsedTemplate() {
      Assert.assertTrue( evaluationDao.isUsedTemplate( etdl.templateAdmin.getId() ) );
      Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateAdminBlock.getId() ) );
      Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateAdminComplex.getId() ) );
      Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateAdminNoItems.getId() ) );
      Assert.assertTrue( evaluationDao.isUsedTemplate( etdl.templatePublic.getId() ) );
      Assert.assertTrue( evaluationDao.isUsedTemplate( etdl.templatePublicUnused.getId() ) ); // used in this file
      Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateUnused.getId() ) );
      Assert.assertTrue( evaluationDao.isUsedTemplate( etdl.templateUser.getId() ) );
      Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateUserUnused.getId() ) );
   }


   /**
    * Add anything that supports the unit tests below here
    */

}
