/**
 * $Id$
 * $URL$
 * EvalEvaluationSetupServiceImplTest.java - evaluation - Dec 26, 2006 10:07:31 AM - azeckoski
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

import java.util.Date;
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalSecurityChecksImpl;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.mocks.MockEvalExternalLogic;
import org.sakaiproject.evaluation.test.mocks.MockEvalJobLogic;
import org.sakaiproject.evaluation.test.mocks.MockExternalHierarchyLogic;


/**
 * Test class for EvalEvaluationSetupServiceImpl
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalEvaluationSetupServiceImplTest extends BaseTestEvalLogic {

   protected EvalEvaluationSetupServiceImpl evaluationSetupService;
   private EvalEvaluationService evaluationService;

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

      evaluationService = (EvalEvaluationService) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalEvaluationService");
      if (evaluationService == null) {
         throw new NullPointerException("EvalEvaluationService could not be retrieved from spring context");
      }

      // setup the mock objects if needed
      EvalEmailsLogicImpl emailsLogicImpl = new EvalEmailsLogicImpl();
      emailsLogicImpl.setEvaluationService(evaluationService);
      emailsLogicImpl.setExternalLogic( new MockEvalExternalLogic() );
      emailsLogicImpl.setSettings(settings);


      // create and setup the object to be tested
      evaluationSetupService = new EvalEvaluationSetupServiceImpl();
      evaluationSetupService.setDao(evaluationDao);
      evaluationSetupService.setExternalLogic( new MockEvalExternalLogic() );
      evaluationSetupService.setHierarchyLogic( new MockExternalHierarchyLogic() );
      evaluationSetupService.setSettings(settings);
      evaluationSetupService.setSecurityChecks(securityChecks);
      evaluationSetupService.setEvaluationService(evaluationService);
      evaluationSetupService.setEvalJobLogic( new MockEvalJobLogic() ); // set to the mock object
      evaluationSetupService.setEmails(emailsLogicImpl);

   }

   /**
    * ADD unit tests below here, use testMethod as the name of the unit test,
    * Note that if a method is overloaded you should include the arguments in the
    * test name like so: testMethodClassInt (for method(Class, int);
    */


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#saveEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)}.
    */
   public void testSaveEvaluation() {
      EvalEvaluation eval = null;

      // save a valid evaluation (all dates separate)
      eval = new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
            EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
            etdl.today, etdl.tomorrow, etdl.threeDaysFuture, etdl.fourDaysFuture, 
            EvalConstants.EVALUATION_STATE_INQUEUE, 
            EvalConstants.SHARING_VISIBLE, Integer.valueOf(1), etdl.templatePublic);
      evaluationSetupService.saveEvaluation( eval, EvalTestDataLoad.MAINT_USER_ID );
      EvalEvaluation checkEval = evaluationService.getEvaluationById(eval.getId());
      assertNotNull(checkEval);
      // check that entity equality works (no longer using this check)
      //assertEquals(eval, checkEval);

      // save a valid evaluation (due and stop date identical)
      evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
            EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
            etdl.today, etdl.tomorrow, etdl.tomorrow, etdl.threeDaysFuture, 
            EvalConstants.EVALUATION_STATE_INQUEUE, 
            EvalConstants.SHARING_VISIBLE, Integer.valueOf(1), etdl.templatePublic), 
            EvalTestDataLoad.MAINT_USER_ID );


      // test view date can be same as due date and stop date can be null
      evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
            EvalTestDataLoad.MAINT_USER_ID, "Eval stop date null", 
            etdl.today, etdl.tomorrow, null, etdl.tomorrow,
            EvalConstants.EVALUATION_STATE_INQUEUE, 
            EvalConstants.SHARING_VISIBLE, Integer.valueOf(1), etdl.templatePublic),
            EvalTestDataLoad.MAINT_USER_ID );

      // test start date can be the only one set
      evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
            EvalTestDataLoad.MAINT_USER_ID, "Eval start date only", 
            etdl.today, null, null, null,
            EvalConstants.EVALUATION_STATE_INQUEUE, 
            EvalConstants.SHARING_VISIBLE, Integer.valueOf(1), etdl.templatePublic),
            EvalTestDataLoad.MAINT_USER_ID );

      // try to save invalid evaluations

      // try save evaluation with dates that are unset (null), start date must be set
      try {
         evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
               EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
               null, null, null, null,
               EvalConstants.EVALUATION_STATE_INQUEUE, 
               EvalConstants.SHARING_VISIBLE, Integer.valueOf(1), etdl.templatePublic),
               EvalTestDataLoad.MAINT_USER_ID );
         fail("Should have thrown exception");
      } catch (NullPointerException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }

      // try save evaluation with dates that are out of order
      // test due date must be after start date
      try {
         evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
               EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
               etdl.threeDaysFuture, etdl.tomorrow, etdl.tomorrow, etdl.fourDaysFuture, 
               EvalConstants.EVALUATION_STATE_INQUEUE, 
               EvalConstants.SHARING_VISIBLE, Integer.valueOf(1), etdl.templatePublic),
               EvalTestDataLoad.MAINT_USER_ID );
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }

      try {
         evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
               EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
               etdl.tomorrow, etdl.tomorrow, etdl.tomorrow, etdl.fourDaysFuture, 
               EvalConstants.EVALUATION_STATE_INQUEUE, 
               EvalConstants.SHARING_VISIBLE, Integer.valueOf(1), etdl.templatePublic),
               EvalTestDataLoad.MAINT_USER_ID );
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }

      // test stop date must be same as or after due date
      try {
         evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
               EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
               etdl.today, etdl.threeDaysFuture, etdl.tomorrow, etdl.fourDaysFuture, 
               EvalConstants.EVALUATION_STATE_INQUEUE, 
               EvalConstants.SHARING_VISIBLE, Integer.valueOf(1), etdl.templatePublic),
               EvalTestDataLoad.MAINT_USER_ID );
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }


      // try save new evaluation with dates that are in the past
      // test start date in the past
      EvalEvaluation testStartEval = new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
            EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
            etdl.yesterday, etdl.tomorrow, etdl.threeDaysFuture, etdl.fourDaysFuture,
            EvalConstants.EVALUATION_STATE_INQUEUE, 
            EvalConstants.SHARING_VISIBLE, Integer.valueOf(1), etdl.templatePublic);
      evaluationSetupService.saveEvaluation( testStartEval, EvalTestDataLoad.MAINT_USER_ID );
      assertNotNull(testStartEval.getId());
      assertTrue(testStartEval.getStartDate().compareTo(new Date()) <= 0);

      // test due date in the past
      try {
         evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
               EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
               etdl.yesterday, etdl.yesterday, etdl.tomorrow, etdl.fourDaysFuture,
               EvalConstants.EVALUATION_STATE_INQUEUE, 
               EvalConstants.SHARING_VISIBLE, Integer.valueOf(1), etdl.templatePublic),
               EvalTestDataLoad.MAINT_USER_ID );
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }

      // test create eval when do not have permission (USER_ID)
      try {
         evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
               EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
               etdl.today, etdl.tomorrow, etdl.threeDaysFuture, etdl.fourDaysFuture,
               EvalConstants.EVALUATION_STATE_INQUEUE, 
               EvalConstants.SHARING_VISIBLE, Integer.valueOf(1), etdl.templatePublic),
               EvalTestDataLoad.USER_ID );
         fail("Should have thrown exception");
      } catch (SecurityException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }

      // test saving an evaluation with an empty template fails
      try {
         evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
               EvalTestDataLoad.ADMIN_USER_ID, "Eval valid title", 
               etdl.today, etdl.tomorrow, etdl.tomorrow, etdl.threeDaysFuture, 
               EvalConstants.EVALUATION_STATE_INQUEUE, 
               EvalConstants.SHARING_VISIBLE, Integer.valueOf(1), etdl.templateAdminNoItems), 
               EvalTestDataLoad.ADMIN_USER_ID );
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }

      try {
         evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
               EvalTestDataLoad.ADMIN_USER_ID, "Eval valid title", 
               etdl.today, etdl.tomorrow, etdl.tomorrow, etdl.threeDaysFuture, 
               EvalConstants.EVALUATION_STATE_INQUEUE, 
               EvalConstants.SHARING_VISIBLE, Integer.valueOf(1), null), 
               EvalTestDataLoad.ADMIN_USER_ID );
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e);
         //fail("Exception: " + e.getMessage()); // see why failing
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#deleteEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)}.
    */
   public void testDeleteEvaluation() {
      // remove evaluation which has not started (uses 2 email templates)
      Long availableId = etdl.evaluationNew.getAvailableEmailTemplate().getId();
      Long reminderId = etdl.evaluationNew.getReminderEmailTemplate().getId();
      int countEmailTemplates = evaluationDao.countByProperties(EvalEmailTemplate.class, 
            new String[] { "id" }, 
            new Object[] { new Long[] {availableId, reminderId} });
      assertEquals(2, countEmailTemplates);

      evaluationSetupService.deleteEvaluation(etdl.evaluationNew.getId(), EvalTestDataLoad.MAINT_USER_ID);
      EvalEvaluation eval = evaluationService.getEvaluationById(etdl.evaluationNew.getId());
      assertNull(eval);

      // check to make sure the associated email templates were also removed
      countEmailTemplates = evaluationDao.countByProperties(EvalEmailTemplate.class, 
            new String[] { "id" }, 
            new Object[] { new Long[] {availableId, reminderId} });
      assertEquals(0, countEmailTemplates);

      // attempt to remove evaluation which is not owned
      try {
         evaluationSetupService.deleteEvaluation(etdl.evaluationNewAdmin.getId(), EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // attempt to remove evaluation with assigned contexts (check for cleanup)
      int countACs = evaluationDao.countByProperties(EvalAssignGroup.class, 
            new String[] { "evaluation.id" }, 
            new Object[] { etdl.evaluationNewAdmin.getId() });
      assertEquals(3, countACs);
      evaluationSetupService.deleteEvaluation(etdl.evaluationNewAdmin.getId(), EvalTestDataLoad.ADMIN_USER_ID);
      eval = evaluationService.getEvaluationById(etdl.evaluationNewAdmin.getId());
      assertNull(eval);
      countACs = evaluationDao.countByProperties(EvalAssignGroup.class, 
            new String[] { "evaluation.id" }, 
            new Object[] { etdl.evaluationNewAdmin.getId() });
      assertEquals(0, countACs);

      // attempt to remove evaluation which is active
      try {
         evaluationSetupService.deleteEvaluation(etdl.evaluationActive.getId(), EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // attempt to remove evaluation which is completed
      try {
         evaluationSetupService.deleteEvaluation(etdl.evaluationClosed.getId(), EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // test for an invalid Eval that it does not cause an exception
      evaluationSetupService.deleteEvaluation( EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.MAINT_USER_ID);
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#getEvaluationsForUser(String, boolean, boolean)}.
    */
   public void testGetEvaluationsForUser() {
      List<EvalEvaluation> evals = null;
      List<Long> ids = null;

      // get all evaluationSetupService for user
      evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.USER_ID, false, false);
      assertNotNull(evals);
      assertEquals(5, evals.size());
      ids = EvalTestDataLoad.makeIdList(evals);
      assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
      assertTrue(ids.contains( etdl.evaluationActive.getId() ));
      assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
      assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
      assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

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
               fail("Order failure:" + lastDate + " less than " + eval.getDueDate());
            }
         }
      }

      // test get for another user
      evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.STUDENT_USER_ID, false, false);
      assertNotNull(evals);
      assertEquals(4, evals.size());
      ids = EvalTestDataLoad.makeIdList(evals);
      assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
      assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
      assertTrue(ids.contains( etdl.evaluationViewable.getId() ));
      assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

      // get all active evaluationSetupService for user
      evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.USER_ID, true, false);
      assertNotNull(evals);
      assertEquals(2, evals.size());
      ids = EvalTestDataLoad.makeIdList(evals);
      assertTrue(ids.contains( etdl.evaluationActive.getId() ));
      assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

      // test active evals for another user
      evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.STUDENT_USER_ID, true, false);
      assertNotNull(evals);
      assertEquals(1, evals.size());
      ids = EvalTestDataLoad.makeIdList(evals);
      assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

      // don't include taken evaluationSetupService
      evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.USER_ID, true, true);
      assertNotNull(evals);
      assertEquals(1, evals.size());
      ids = EvalTestDataLoad.makeIdList(evals);
      assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

      // try to get for invalid user
      evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.INVALID_USER_ID, false, false);
      assertNotNull(evals);
      assertEquals(1, evals.size());
      ids = EvalTestDataLoad.makeIdList(evals);
      assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#getVisibleEvaluationsForUser(java.lang.String, boolean, boolean)}.
    */
   public void testGetVisibleEvaluationsForUser() {
      // test getting visible evals for the maint user
      List<EvalEvaluation> evals = null;
      List<Long> ids = null;

      evals = evaluationSetupService.getVisibleEvaluationsForUser(EvalTestDataLoad.MAINT_USER_ID, false, false);
      assertNotNull(evals);
      assertEquals(4, evals.size());
      ids = EvalTestDataLoad.makeIdList(evals);
      assertTrue(ids.contains( etdl.evaluationNew.getId() ));
      assertTrue(ids.contains( etdl.evaluationActive.getId() ));
      assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
      assertTrue(ids.contains( etdl.evaluationProvided.getId() ));

      // test getting visible evals for the admin user (should be all)
      evals = evaluationSetupService.getVisibleEvaluationsForUser(EvalTestDataLoad.ADMIN_USER_ID, false, false);
      assertNotNull(evals);
      assertEquals(7, evals.size());

      // test getting recent closed evals for the admin user
      evals = evaluationSetupService.getVisibleEvaluationsForUser(EvalTestDataLoad.ADMIN_USER_ID, true, false);
      assertNotNull(evals);
      assertEquals(6, evals.size());
      ids = EvalTestDataLoad.makeIdList(evals);
      assertTrue(! ids.contains( etdl.evaluationViewable.getId() ));

      // test getting visible evals for the normal user (should be none)
      evals = evaluationSetupService.getVisibleEvaluationsForUser(EvalTestDataLoad.USER_ID, false, false);
      assertNotNull(evals);
      assertEquals(0, evals.size());

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#getEvalCategories(java.lang.String)}.
    */
   public void testGetEvalCategories() {
      String[] cats = null;

      // get all categories in the system
      cats = evaluationSetupService.getEvalCategories(null);
      assertNotNull(cats);
      assertEquals(2, cats.length);
      assertEquals(EvalTestDataLoad.EVAL_CATEGORY_1, cats[0]);
      assertEquals(EvalTestDataLoad.EVAL_CATEGORY_2, cats[1]);

      // get all categories for a user
      cats = evaluationSetupService.getEvalCategories(EvalTestDataLoad.ADMIN_USER_ID);
      assertNotNull(cats);
      assertEquals(2, cats.length);
      assertEquals(EvalTestDataLoad.EVAL_CATEGORY_1, cats[0]);
      assertEquals(EvalTestDataLoad.EVAL_CATEGORY_2, cats[1]);

      cats = evaluationSetupService.getEvalCategories(EvalTestDataLoad.MAINT_USER_ID);
      assertNotNull(cats);
      assertEquals(1, cats.length);
      assertEquals(EvalTestDataLoad.EVAL_CATEGORY_1, cats[0]);

      // get no categories for user with none
      cats = evaluationSetupService.getEvalCategories(EvalTestDataLoad.USER_ID);
      assertNotNull(cats);
      assertEquals(0, cats.length);

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#getEvaluationsByCategory(java.lang.String, java.lang.String)}.
    */
   public void testGetEvaluationsByCategory() {
      List<EvalEvaluation> evals = null;
      List<Long> ids = null;

      // get all evaluationSetupService for a category
      evals = evaluationSetupService.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_1, null);
      assertNotNull(evals);
      assertEquals(2, evals.size());
      ids = EvalTestDataLoad.makeIdList(evals);
      assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
      assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

      evals = evaluationSetupService.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_2, null);
      assertNotNull(evals);
      assertEquals(1, evals.size());
      ids = EvalTestDataLoad.makeIdList(evals);
      assertTrue(ids.contains( etdl.evaluationClosed.getId() ));

      // get evaluationSetupService for a category and user
      evals = evaluationSetupService.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_1, EvalTestDataLoad.USER_ID);
      assertNotNull(evals);
      assertEquals(1, evals.size());
      ids = EvalTestDataLoad.makeIdList(evals);
      assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

      evals = evaluationSetupService.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_2, EvalTestDataLoad.USER_ID);
      assertNotNull(evals);
      assertEquals(0, evals.size());

      // get evaluationSetupService for invalid or non-existent category
      evals = evaluationSetupService.getEvaluationsByCategory(EvalTestDataLoad.INVALID_CONSTANT_STRING, null);
      assertNotNull(evals);
      assertEquals(0, evals.size());

      // get evaluationSetupService for invalid or non-existent user
      evals = evaluationSetupService.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_1, null);
      assertNotNull(evals);

   }

   public void testSaveEmailTemplate() {
      // test valid new saves
      evaluationSetupService.saveEmailTemplate( new EvalEmailTemplate( EvalTestDataLoad.MAINT_USER_ID,
            EvalConstants.EMAIL_TEMPLATE_AVAILABLE, "subject", "a message"), 
            EvalTestDataLoad.MAINT_USER_ID);
      evaluationSetupService.saveEmailTemplate( new EvalEmailTemplate( EvalTestDataLoad.ADMIN_USER_ID,
            EvalConstants.EMAIL_TEMPLATE_CREATED, "subject", "another message"), 
            EvalTestDataLoad.ADMIN_USER_ID);

      // test saving new always nulls out the defaultType
      // the defaultType cannot be changed when saving
      // (default templates can only be set in the preloaded data for now)
      EvalEmailTemplate testTemplate = new EvalEmailTemplate( EvalTestDataLoad.ADMIN_USER_ID,
            EvalConstants.EMAIL_TEMPLATE_AVAILABLE, "subject", "a message", 
            EvalConstants.EMAIL_TEMPLATE_AVAILABLE);
      evaluationSetupService.saveEmailTemplate( testTemplate, EvalTestDataLoad.ADMIN_USER_ID);
      assertNotNull( testTemplate.getId() );
      assertNull( testTemplate.getDefaultType() );

      // test invalid update to default template as non-admin
      EvalEmailTemplate defaultTemplate = 
         evaluationService.getDefaultEmailTemplate( EvalConstants.EMAIL_TEMPLATE_AVAILABLE );
      try {
         defaultTemplate.setMessage("new message for default");
         evaluationSetupService.saveEmailTemplate( defaultTemplate, 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // ok as admin though
      evaluationSetupService.saveEmailTemplate( defaultTemplate, 
            EvalTestDataLoad.ADMIN_USER_ID);

      // test valid updates
      etdl.emailTemplate1.setMessage("new message 1");
      evaluationSetupService.saveEmailTemplate( etdl.emailTemplate1, EvalTestDataLoad.ADMIN_USER_ID);

      etdl.emailTemplate2.setMessage("new message 2");
      evaluationSetupService.saveEmailTemplate( etdl.emailTemplate2, EvalTestDataLoad.MAINT_USER_ID);

      // test user not has permission to update
      try {
         etdl.emailTemplate1.setMessage("new message 1");
         evaluationSetupService.saveEmailTemplate( etdl.emailTemplate1, 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }
      
      try {
         etdl.emailTemplate2.setMessage("new message 2");
         evaluationSetupService.saveEmailTemplate( etdl.emailTemplate2, 
               EvalTestDataLoad.USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // test associated eval is active but we can still update (relaxed perms)
      etdl.emailTemplate3.setMessage("new message 3");
      evaluationSetupService.saveEmailTemplate( etdl.emailTemplate3, 
            EvalTestDataLoad.MAINT_USER_ID);

   }

   public void testRemoveEmailTemplate() {
      // check user cannot remove
      try {
         evaluationSetupService.removeEmailTemplate(etdl.emailTemplate2.getId(), EvalTestDataLoad.USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // test user can remove
      EvalEvaluation eval = (EvalEvaluation) evaluationDao.findById(EvalEvaluation.class, etdl.evaluationNew.getId());
      assertNotNull(eval.getReminderEmailTemplate());

      evaluationSetupService.removeEmailTemplate(etdl.emailTemplate2.getId(), EvalTestDataLoad.MAINT_USER_ID);

      assertNull(eval.getReminderEmailTemplate());
      assertNull( evaluationDao.findById(EvalEmailTemplate.class, etdl.emailTemplate2.getId()) );

      // test cannot remove default templates
      EvalEmailTemplate defaultTemplate = evaluationService.getDefaultEmailTemplate(EvalConstants.EMAIL_TEMPLATE_AVAILABLE);
      try {
         evaluationSetupService.removeEmailTemplate(defaultTemplate.getId(), EvalTestDataLoad.ADMIN_USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

   }
   
   
   
   // GROUP ASSIGNMENTS

   @SuppressWarnings("unchecked")
   public void testSaveAssignGroup() {

      // test adding evalGroupId to inqueue eval
      EvalAssignGroup eacNew = new EvalAssignGroup(EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF, 
            EvalConstants.GROUP_TYPE_SITE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, 
            etdl.evaluationNew);
      evaluationSetupService.saveAssignGroup(eacNew, EvalTestDataLoad.MAINT_USER_ID);

      // check save worked
      List<EvalAssignGroup> l = evaluationDao.findByProperties(EvalAssignGroup.class, 
            new String[] {"evaluation.id"}, new Object[] {etdl.evaluationNew.getId()});
      assertNotNull(l);
      assertEquals(1, l.size());
      assertTrue(l.contains(eacNew));

      // test adding evalGroupId to active eval
      EvalAssignGroup eacActive = new EvalAssignGroup(EvalConstants.GROUP_TYPE_SITE, 
            EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE2_REF, 
            Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, 
            etdl.evaluationActive);
      evaluationSetupService.saveAssignGroup(eacActive, EvalTestDataLoad.MAINT_USER_ID);

      // check save worked
      l = evaluationDao.findByProperties(EvalAssignGroup.class, 
            new String[] {"evaluation.id"}, new Object[] {etdl.evaluationActive.getId()});
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(eacActive));

      // test modify safe part while active
      EvalAssignGroup testEac1 = (EvalAssignGroup) evaluationDao.
         findById( EvalAssignGroup.class, etdl.assign1.getId() );
      testEac1.setStudentsViewResults( Boolean.TRUE );
      evaluationSetupService.saveAssignGroup(testEac1, EvalTestDataLoad.MAINT_USER_ID);

      // test modify safe part while closed
      EvalAssignGroup testEac2 = (EvalAssignGroup) evaluationDao.
         findById( EvalAssignGroup.class, etdl.assign4.getId() );
      testEac2.setStudentsViewResults( Boolean.TRUE );
      evaluationSetupService.saveAssignGroup(testEac2, EvalTestDataLoad.MAINT_USER_ID);

      // test admin can modify un-owned evalGroupId
      EvalAssignGroup testEac3 = (EvalAssignGroup) evaluationDao.
         findById( EvalAssignGroup.class, etdl.assign6.getId() );
      testEac3.setStudentsViewResults( Boolean.TRUE );
      evaluationSetupService.saveAssignGroup(testEac3, EvalTestDataLoad.ADMIN_USER_ID);


      // test cannot add duplicate evalGroupId to in-queue eval
      try {
         evaluationSetupService.saveAssignGroup( new EvalAssignGroup(
               EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF, 
               EvalConstants.GROUP_TYPE_SITE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, 
               etdl.evaluationNew),
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // test cannot add duplicate evalGroupId to active eval
      try {
         evaluationSetupService.saveAssignGroup( new EvalAssignGroup(
               EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF, 
               EvalConstants.GROUP_TYPE_SITE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, 
               etdl.evaluationActive),
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // test user without perm cannot add evalGroupId to eval
      try {
         evaluationSetupService.saveAssignGroup( new EvalAssignGroup(
               EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF, 
               EvalConstants.GROUP_TYPE_SITE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, 
               etdl.evaluationNew), 
               EvalTestDataLoad.USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // test cannot add evalGroupId to closed eval
      try {
         evaluationSetupService.saveAssignGroup( new EvalAssignGroup(
               EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF, 
               EvalConstants.GROUP_TYPE_SITE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, 
               etdl.evaluationViewable), 
               EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // test cannot modify non-owned evalGroupId
      try {
         etdl.assign7.setStudentsViewResults( Boolean.TRUE );
         evaluationSetupService.saveAssignGroup(etdl.assign7, EvalTestDataLoad.MAINT_USER_ID);
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }


      // TODO - these tests cannot pass right now because of hibernate screwing us -AZ
//    // test modify evalGroupId while new eval
//    try {
//       EvalAssignContext testEac4 = (EvalAssignContext) evaluationDao.
//          findById( EvalAssignContext.class, etdl.assign6.getId() );
//       testEac4.setContext( EvalTestDataLoad.CONTEXT3 );
//       evaluationSetupService.saveAssignContext(testEac4, EvalTestDataLoad.MAINT_USER_ID);
//       fail("Should have thrown exception");
//    } catch (RuntimeException e) {
//       assertNotNull(e);
//    }
//    
//    // test modify evalGroupId while active eval
//    try {
//       EvalAssignContext testEac5 = (EvalAssignContext) evaluationDao.
//          findById( EvalAssignContext.class, etdl.assign1.getId() );
//       testEac5.setContext( EvalTestDataLoad.CONTEXT2 );
//       evaluationSetupService.saveAssignContext(testEac5, EvalTestDataLoad.MAINT_USER_ID);
//       fail("Should have thrown exception");
//    } catch (RuntimeException e) {
//       assertNotNull(e);
//    }
//
//    // test modify evalGroupId while closed eval
//    try {
//       EvalAssignContext testEac6 = (EvalAssignContext) evaluationDao.
//          findById( EvalAssignContext.class, etdl.assign4.getId() );
//       testEac6.setContext( EvalTestDataLoad.CONTEXT1 );
//       evaluationSetupService.saveAssignContext(testEac6, EvalTestDataLoad.MAINT_USER_ID);
//       fail("Should have thrown exception");
//    } catch (RuntimeException e) {
//       assertNotNull(e);
//    }

      // TODO - test that evaluation cannot be changed
      
   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#deleteAssignGroup(java.lang.Long, java.lang.String)}.
    */
   @SuppressWarnings("unchecked")
   public void testDeleteAssignGroup() {
      // save some ACs to test removing
      EvalAssignGroup eac1 = new EvalAssignGroup(EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF, 
            EvalConstants.GROUP_TYPE_SITE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, 
            etdl.evaluationNew);
      EvalAssignGroup eac2 = new EvalAssignGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalTestDataLoad.SITE2_REF, 
            EvalConstants.GROUP_TYPE_SITE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, 
            etdl.evaluationNew);
      evaluationDao.save(eac1);
      evaluationDao.save(eac2);

      // check save worked
      List<EvalAssignGroup> l = evaluationDao.findByProperties(EvalAssignGroup.class, 
            new String[] {"evaluation.id"}, new Object[] {etdl.evaluationNew.getId()});
      assertNotNull(l);
      assertEquals(2, l.size());
      assertTrue(l.contains(eac1));
      assertTrue(l.contains(eac2));

      // test can remove contexts from new Evaluation
      Long eacId = eac1.getId();
      evaluationSetupService.deleteAssignGroup( eacId, EvalTestDataLoad.MAINT_USER_ID );

      evaluationSetupService.deleteAssignGroup( etdl.assign6.getId(), EvalTestDataLoad.MAINT_USER_ID );

      // check save worked
      l = evaluationDao.findByProperties(EvalAssignGroup.class, 
            new String[] {"evaluation.id"}, new Object[] {etdl.evaluationNew.getId()});
      assertNotNull(l);
      assertEquals(1, l.size());
      assertTrue(! l.contains(eac1));

      // test cannot remove evalGroupId from active eval
      try {
         evaluationSetupService.deleteAssignGroup( etdl.assign1.getId(), 
               EvalTestDataLoad.MAINT_USER_ID );
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // test cannot remove evalGroupId from closed eval
      try {
         evaluationSetupService.deleteAssignGroup( etdl.assign4.getId(), 
               EvalTestDataLoad.MAINT_USER_ID );
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // test cannot remove evalGroupId without permission
      try {
         evaluationSetupService.deleteAssignGroup( eac2.getId(), 
               EvalTestDataLoad.USER_ID );
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // test cannot remove evalGroupId without ownership
      try {
         evaluationSetupService.deleteAssignGroup( etdl.assign7.getId(), 
               EvalTestDataLoad.MAINT_USER_ID );
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

      // test remove invalid eac
      try {
         evaluationSetupService.deleteAssignGroup( EvalTestDataLoad.INVALID_LONG_ID, 
               EvalTestDataLoad.MAINT_USER_ID );
         fail("Should have thrown exception");
      } catch (RuntimeException e) {
         assertNotNull(e);
      }

   }


   public void testSetDefaults() {
      EvalAssignGroup eah = new EvalAssignGroup("az", "eag1", "Site", etdl.evaluationActive);

      // make sure it fills in nulls
      assertNull(eah.getInstructorApproval());
      assertNull(eah.getInstructorsViewResults());
      assertNull(eah.getStudentsViewResults());
      evaluationSetupService.setDefaults(etdl.evaluationActive, eah);
      assertNotNull(eah.getInstructorApproval());
      assertNotNull(eah.getInstructorsViewResults());
      assertNotNull(eah.getStudentsViewResults());

      // make sure it does not wipe existing settings
      eah = new EvalAssignGroup("az", "eag1", "Site", false, false, false, etdl.evaluationActive);
      evaluationSetupService.setDefaults(etdl.evaluationActive, eah);
// TODO - temporary disable
//      assertEquals(Boolean.FALSE, eah.getInstructorApproval());
      assertEquals(Boolean.FALSE, eah.getInstructorsViewResults());
      assertEquals(Boolean.FALSE, eah.getStudentsViewResults());

      eah = new EvalAssignGroup("az", "eag1", "Site", true, true, true, etdl.evaluationActive);
      evaluationSetupService.setDefaults(etdl.evaluationActive, eah);
      assertEquals(Boolean.TRUE, eah.getInstructorApproval());
      assertEquals(Boolean.TRUE, eah.getInstructorsViewResults());
      assertEquals(Boolean.TRUE, eah.getStudentsViewResults());

   }

}
