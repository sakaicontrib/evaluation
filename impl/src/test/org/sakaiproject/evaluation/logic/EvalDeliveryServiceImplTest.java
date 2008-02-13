/**
 * $Id: EvalDeliveryServiceImplTest.java 1000 Dec 26, 2006 10:07:31 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvalDeliveryServiceImplTest.java - evaluation - Dec 25, 2006 10:07:31 AM - azeckoski
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
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.sakaiproject.evaluation.logic.EvalAuthoringServiceImpl;
import org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSecurityChecks;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.mocks.MockEvalExternalLogic;


/**
 * Test class for EvalDeliveryServiceImpl
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalDeliveryServiceImplTest extends BaseTestEvalLogic {

   protected EvalDeliveryServiceImpl responses;

   private EvalEvaluation evaluationClosedTwo;

   // run this before each test starts
   protected void onSetUpBeforeTransaction() throws Exception {
      super.onSetUpBeforeTransaction();

      // load up any other needed spring beans
      EvalSettings settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
      if (settings == null) {
         throw new NullPointerException("EvalSettings could not be retrieved from spring evalGroupId");
      }

      EvalSecurityChecks securityChecks = (EvalSecurityChecks) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSecurityChecks");
      if (securityChecks == null) {
         throw new NullPointerException("EvalSecurityChecks could not be retrieved from spring context");
      }

      EvalEvaluationService evaluationService = (EvalEvaluationService) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalEvaluationService");
      if (evaluationService == null) {
         throw new NullPointerException("EvalEvaluationService could not be retrieved from spring context");
      }

      // setup the mock objects if needed

      // create the other needed logic impls
      EvalAuthoringServiceImpl authoringServiceImpl = new EvalAuthoringServiceImpl();
      authoringServiceImpl.setDao(evaluationDao);
      authoringServiceImpl.setExternalLogic( new MockEvalExternalLogic() );
      authoringServiceImpl.setSettings(settings);
      authoringServiceImpl.setSecurityChecks(securityChecks);

      // create and setup the object to be tested
      responses = new EvalDeliveryServiceImpl();
      responses.setDao(evaluationDao);
      responses.setExternalLogic( new MockEvalExternalLogic() );
      responses.setEvaluationService(evaluationService);
      responses.setEvalSettings(settings);
      responses.setAuthoringService( authoringServiceImpl );
   }

   // run this before each test starts and as part of the transaction
   protected void onSetUpInTransaction() {
      // preload additional data if desired

      // Evaluation Complete (ended yesterday, viewable tomorrow), recent close
      evaluationClosedTwo = new EvalEvaluation(new Date(), 
            EvalTestDataLoad.ADMIN_USER_ID, "Eval closed two", null, 
            etdl.threeDaysAgo, etdl.yesterday, 
            etdl.yesterday, etdl.tomorrow, null, null,
            EvalConstants.EVALUATION_STATE_CLOSED, EvalConstants.INSTRUCTOR_OPT_IN, 
            Integer.valueOf(2), null, null, null, null, etdl.templateAdmin, null, null,
            Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, 
            EvalTestDataLoad.LOCKED,
            EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null);
      evaluationDao.save(evaluationClosedTwo);
   }



   /**
    * ADD unit tests below here, use testMethod as the name of the unit test,
    * Note that if a method is overloaded you should include the arguments in the
    * test name like so: testMethodClassInt (for method(Class, int);
    */


   public void testGetResponseById() {
      EvalResponse response = null;

      response = responses.getResponseById( etdl.response1.getId() );
      Assert.assertNotNull(response);
      Assert.assertEquals(etdl.response1.getId(), response.getId());

      response = responses.getResponseById( etdl.response2.getId() );
      Assert.assertNotNull(response);
      Assert.assertEquals(etdl.response2.getId(), response.getId());

      // test get eval by invalid id
      response = responses.getResponseById( EvalTestDataLoad.INVALID_LONG_ID );
      Assert.assertNull(response);
   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#getEvaluationResponses(java.lang.String, java.lang.Long[], boolean)}.
    */
   public void testGetEvaluationResponses() {
      List<EvalResponse> l = null;
      List<Long> ids = null;

      // retrieve one response for a normal user in one eval
      l = responses.getEvaluationResponses(EvalTestDataLoad.USER_ID, 
            new Long[] { etdl.evaluationActive.getId() }, true );
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.response1.getId() ));

      l = responses.getEvaluationResponses(EvalTestDataLoad.STUDENT_USER_ID, 
            new Long[] { etdl.evaluationClosed.getId() }, true );
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.response3.getId() ));

      // retrieve all responses for a normal user in one eval
      l = responses.getEvaluationResponses(EvalTestDataLoad.USER_ID, 
            new Long[] { etdl.evaluationClosed.getId() }, true );
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.response2.getId() ));
      Assert.assertTrue(ids.contains( etdl.response6.getId() ));

      // retrieve all responses for a normal user in mutliple evals
      l = responses.getEvaluationResponses(EvalTestDataLoad.USER_ID, 
            new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, true );
      Assert.assertNotNull(l);
      Assert.assertEquals(4, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.response1.getId() ));
      Assert.assertTrue(ids.contains( etdl.response2.getId() ));
      Assert.assertTrue(ids.contains( etdl.response4.getId() ));
      Assert.assertTrue(ids.contains( etdl.response6.getId() ));

      // attempt to retrieve all responses
      l = responses.getEvaluationResponses(EvalTestDataLoad.USER_ID, 
            new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, null );
      Assert.assertNotNull(l);
      Assert.assertEquals(4, l.size());

      // attempt to retrieve all incomplete responses (there are none)
      l = responses.getEvaluationResponses(EvalTestDataLoad.USER_ID, 
            new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, false );
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      // attempt to retrieve results for user that has no responses
      l = responses.getEvaluationResponses(EvalTestDataLoad.STUDENT_USER_ID, 
            new Long[] { etdl.evaluationActive.getId() }, true );
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      l = responses.getEvaluationResponses(EvalTestDataLoad.MAINT_USER_ID, 
            new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, true );
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      // test that admin can fetch all results for evaluationSetupService
      l = responses.getEvaluationResponses(EvalTestDataLoad.ADMIN_USER_ID, 
            new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, true );
      Assert.assertNotNull(l);
      Assert.assertEquals(6, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.response1.getId() ));
      Assert.assertTrue(ids.contains( etdl.response2.getId() ));
      Assert.assertTrue(ids.contains( etdl.response3.getId() ));
      Assert.assertTrue(ids.contains( etdl.response4.getId() ));
      Assert.assertTrue(ids.contains( etdl.response5.getId() ));
      Assert.assertTrue(ids.contains( etdl.response6.getId() ));

      l = responses.getEvaluationResponses(EvalTestDataLoad.ADMIN_USER_ID, 
            new Long[] { etdl.evaluationViewable.getId() }, true );
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.response4.getId() ));
      Assert.assertTrue(ids.contains( etdl.response5.getId() ));

      // check that empty array causes failure
      try {
         l = responses.getEvaluationResponses(EvalTestDataLoad.ADMIN_USER_ID, 
               new Long[] {}, true );
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      // check that invalid IDs cause failure
      try {
         l = responses.getEvaluationResponses(EvalTestDataLoad.ADMIN_USER_ID, 
               new Long[] { EvalTestDataLoad.INVALID_LONG_ID }, true );
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      try {
         l = responses.getEvaluationResponses(EvalTestDataLoad.ADMIN_USER_ID, 
               new Long[] { etdl.evaluationViewable.getId(), EvalTestDataLoad.INVALID_LONG_ID }, true );
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }


   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#countResponses(Long, String)}.
    */
   public void testCountResponses() {

      // test counts for all responses in various evaluationSetupService
      Assert.assertEquals(3, responses.countResponses( etdl.evaluationClosed.getId(), null, null) );
      Assert.assertEquals(2, responses.countResponses( etdl.evaluationViewable.getId(), null, null) );
      Assert.assertEquals(1, responses.countResponses( etdl.evaluationActive.getId(), null, null) );
      Assert.assertEquals(0, responses.countResponses( etdl.evaluationActiveUntaken.getId(), null, null) );

      // test counts limited by evalGroupId
      Assert.assertEquals(1, responses.countResponses( etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE1_REF, null) );
      Assert.assertEquals(0, responses.countResponses( etdl.evaluationViewable.getId(), EvalTestDataLoad.SITE1_REF, null) );
      Assert.assertEquals(1, responses.countResponses( etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF, null) );
      Assert.assertEquals(0, responses.countResponses( etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE1_REF, null) );

      Assert.assertEquals(2, responses.countResponses( etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE2_REF, null) );
      Assert.assertEquals(2, responses.countResponses( etdl.evaluationViewable.getId(), EvalTestDataLoad.SITE2_REF, null) );
      Assert.assertEquals(0, responses.countResponses( etdl.evaluationActive.getId(), EvalTestDataLoad.SITE2_REF, null) );
      Assert.assertEquals(0, responses.countResponses( etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE2_REF, null) );

      // test counts limited by completed
      Assert.assertEquals(3, responses.countResponses( etdl.evaluationClosed.getId(), null, true) );
      Assert.assertEquals(2, responses.countResponses( etdl.evaluationViewable.getId(), null, true) );
      Assert.assertEquals(1, responses.countResponses( etdl.evaluationActive.getId(), null, true) );
      Assert.assertEquals(0, responses.countResponses( etdl.evaluationActiveUntaken.getId(), null, true) );

      // test counts limited by incomplete
      Assert.assertEquals(0, responses.countResponses( etdl.evaluationClosed.getId(), null, false) );
      Assert.assertEquals(0, responses.countResponses( etdl.evaluationViewable.getId(), null, false) );
      Assert.assertEquals(0, responses.countResponses( etdl.evaluationActive.getId(), null, false) );
      Assert.assertEquals(0, responses.countResponses( etdl.evaluationActiveUntaken.getId(), null, false) );

      // check that invalid IDs cause failure
      try {
         responses.countResponses( EvalTestDataLoad.INVALID_LONG_ID, null, null );
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#getEvalAnswers(java.lang.Long, java.lang.Long)}.
    */
   public void testGetEvalAnswers() {
      List<EvalAnswer> l = null;
      List<Long> ids = null;

      // retrieve one answer for an eval
      l = responses.getEvalAnswers( etdl.item1.getId(), etdl.evaluationActive.getId(), null );
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.answer1_1.getId() ));

      l = responses.getEvalAnswers( etdl.item5.getId(), etdl.evaluationClosed.getId(), null );
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.answer2_5.getId() ));

      // retrieve multiple answers for an eval
      l = responses.getEvalAnswers( etdl.item1.getId(), etdl.evaluationViewable.getId(), null );
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.answer4_1.getId() ));
      Assert.assertTrue(ids.contains( etdl.answer5_1.getId() ));

      l = responses.getEvalAnswers( etdl.item2.getId(), etdl.evaluationClosed.getId(), null );
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      ids = EvalTestDataLoad.makeIdList(l);
      Assert.assertTrue(ids.contains( etdl.answer2_2.getId() ));
      Assert.assertTrue(ids.contains( etdl.answer3_2.getId() ));

      // retrieve no answers for an eval item
      l = responses.getEvalAnswers( etdl.item1.getId(), etdl.evaluationActiveUntaken.getId(), null );
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      // TODO - add checks which only retrieve partial results for an eval (limit eval groups)

      // TODO - check that invalid item/eval combinations cause failure?

      // check that invalid ids cause failure
      try {
         l = responses.getEvalAnswers( EvalTestDataLoad.INVALID_LONG_ID, etdl.evaluationActiveUntaken.getId(), null );
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

      try {
         l = responses.getEvalAnswers( etdl.item1.getId(), EvalTestDataLoad.INVALID_LONG_ID, null );
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#getEvalResponseIds(Long, String[])}.
    */
   public void testGetEvalResponseIds() {
      List<Long> l = null;

      // retrieve all response Ids for an evaluation
      l = responses.getEvalResponseIds(etdl.evaluationClosed.getId(), null, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(3, l.size());
      Assert.assertTrue(l.contains( etdl.response2.getId() ));
      Assert.assertTrue(l.contains( etdl.response3.getId() ));
      Assert.assertTrue(l.contains( etdl.response6.getId() ));

      l = responses.getEvalResponseIds(etdl.evaluationClosed.getId(), new String[] {}, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(3, l.size());
      Assert.assertTrue(l.contains( etdl.response2.getId() ));
      Assert.assertTrue(l.contains( etdl.response3.getId() ));
      Assert.assertTrue(l.contains( etdl.response6.getId() ));

      // retrieve all response Ids for an evaluation using all groups
      l = responses.getEvalResponseIds(etdl.evaluationClosed.getId(), 
            new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(3, l.size());
      Assert.assertTrue(l.contains( etdl.response2.getId() ));
      Assert.assertTrue(l.contains( etdl.response3.getId() ));
      Assert.assertTrue(l.contains( etdl.response6.getId() ));

      // test retrieval of all responses for an evaluation
      l = responses.getEvalResponseIds(etdl.evaluationClosed.getId(), 
            new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null);
      Assert.assertNotNull(l);
      Assert.assertEquals(3, l.size());

      // test retrieval of incomplete responses for an evaluation
      l = responses.getEvalResponseIds(etdl.evaluationClosed.getId(), 
            new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, false);
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      // retrieve all response Ids for an evaluation in one group only
      l = responses.getEvalResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      Assert.assertTrue(l.contains( etdl.response2.getId() ));

      // retrieve all response Ids for an evaluation in one group only
      l = responses.getEvalResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(2, l.size());
      Assert.assertTrue(l.contains( etdl.response3.getId() ));
      Assert.assertTrue(l.contains( etdl.response6.getId() ));

      l = responses.getEvalResponseIds(etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(1, l.size());
      Assert.assertTrue(l.contains( etdl.response1.getId() ));

      // try to get responses for an eval group that is not associated with this eval
      l = responses.getEvalResponseIds(etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      // try to get responses for an eval with no responses
      l = responses.getEvalResponseIds(etdl.evaluationActiveUntaken.getId(), null, true);
      Assert.assertNotNull(l);
      Assert.assertEquals(0, l.size());

      // check that invalid eval ids cause failure
      try {
         l = responses.getEvalResponseIds( EvalTestDataLoad.INVALID_LONG_ID, null, true);
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#saveResponse(org.sakaiproject.evaluation.model.EvalResponse, String)}.
    */
   public void testSaveResponse() {

      // test saving a response with no answers is ok
      EvalResponse responseNone = new EvalResponse( new Date(), EvalTestDataLoad.STUDENT_USER_ID, 
            EvalTestDataLoad.SITE1_REF, new Date(), etdl.evaluationActiveUntaken);
      responses.saveResponse( responseNone, EvalTestDataLoad.STUDENT_USER_ID);
      Assert.assertNotNull(responseNone.getId());

      // test saving a response when admin user is ok
      responses.saveResponse( new EvalResponse( new Date(), 
            EvalTestDataLoad.ADMIN_USER_ID, EvalTestDataLoad.SITE1_REF, 
            new Date(), etdl.evaluationActiveUntaken), 
            EvalTestDataLoad.ADMIN_USER_ID);

      // test saving a response for a closed evaluation fails
      try {
         responses.saveResponse( new EvalResponse( new Date(), 
               EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF, 
               new Date(), evaluationClosedTwo), 
               EvalTestDataLoad.USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      // test saving a response when user has no permission fails
      try {
         responses.saveResponse( new EvalResponse( new Date(), 
               EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE2_REF, 
               new Date(), etdl.evaluationActiveUntaken), 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      // test saving a response for an invalid evalGroupId fails (evalGroupId not assigned to this eval)
      try {
         responses.saveResponse( new EvalResponse( new Date(), 
               EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE3_REF, 
               new Date(), etdl.evaluationActive), 
               EvalTestDataLoad.USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      // TODO - make this work
//    // test saving a response with invalid answers fails (item2 not in evaluationActiveUntaken)
//    try {
//    EvalResponse responseInvAns = new EvalResponse( new Date(), EvalTestDataLoad.USER_ID, 
//    EvalTestDataLoad.CONTEXT1, new Date(), etdl.evaluationActiveUntaken);
//    responseInvAns.setAnswers( new HashSet() );
//    responseInvAns.getAnswers().add( 
//    new EvalAnswer( new Date(), etdl.item2, responseInvAns, "text", null) );
//    responses.saveResponse( responseInvAns, EvalTestDataLoad.USER_ID);
//    Assert.fail("Should have thrown exception");
//    } catch (IllegalArgumentException e) {
//    Assert.assertNotNull(e);
//    Assert.fail(e.getMessage());
//    }

      // test saving a response with valid answers is ok (make sure answers saved also)
      EvalResponse responseAns = new EvalResponse( new Date(), EvalTestDataLoad.USER_ID, 
            EvalTestDataLoad.SITE1_REF, new Date(), etdl.evaluationActiveUntaken);
      responseAns.setAnswers( new HashSet<EvalAnswer>() );
      EvalAnswer answer1_1 = new EvalAnswer( new Date(), etdl.templateItem1P, etdl.item1, responseAns, "text", null, null, null);
      responseAns.getAnswers().add( answer1_1 );
      responses.saveResponse( responseAns, EvalTestDataLoad.USER_ID);
      Assert.assertNotNull(responseAns.getId());
      Assert.assertNotNull(answer1_1.getId());

      // test updating an already created response is ok
      responseNone.setEndTime( new Date() );
      responses.saveResponse( responseNone, EvalTestDataLoad.STUDENT_USER_ID);

      // test adding answers to an already created response is ok
      // make sure the answer is saved also
      responseNone.setAnswers( new HashSet<EvalAnswer>() );
      EvalAnswer answer2_1 = new EvalAnswer( new Date(), etdl.templateItem1P, etdl.item1, responseNone, null, Integer.valueOf(1), null, null);
      responseNone.getAnswers().add( answer2_1 );
      responses.saveResponse( responseNone, EvalTestDataLoad.STUDENT_USER_ID);
      Assert.assertNotNull(answer2_1.getId());

      // TODO - cannot do this test until hibernate issue resolved
//    // test that changing anything else on the existing response fails
//    try {
//    responseNone.setContext( EvalTestDataLoad.CONTEXT3 );
//    responses.saveResponse( responseNone, EvalTestDataLoad.STUDENT_USER_ID);
//    Assert.fail("Should have thrown exception");
//    } catch (IllegalArgumentException e) {
//    Assert.assertNotNull(e);
//    Assert.fail(e.getMessage());
//    }

      // test saving a response without proper ownership fails
      try {
         responseNone.setEndTime( new Date() );
         responses.saveResponse( responseNone, EvalTestDataLoad.USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (SecurityException e) {
         Assert.assertNotNull(e);
      }

      // test saving a response when one exists fails
      try {
         responses.saveResponse( new EvalResponse( new Date(), 
               EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF, 
               new Date(), etdl.evaluationActive), 
               EvalTestDataLoad.USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      try {
         responses.saveResponse( new EvalResponse( new Date(), 
               EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE2_REF, 
               new Date(), etdl.evaluationActive), 
               EvalTestDataLoad.MAINT_USER_ID);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

   }

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#canModifyResponse(java.lang.String, java.lang.Long)}.
    */
   public void testCanModifyResponse() {

      // test owner can modify unlocked
      Assert.assertTrue( responses.canModifyResponse(
            EvalTestDataLoad.USER_ID, etdl.response1.getId()) );

      // test admin cannot override permissions
      Assert.assertFalse( responses.canModifyResponse(
            EvalTestDataLoad.ADMIN_USER_ID,	etdl.response1.getId()) );

      // test users without perms cannot modify
      Assert.assertFalse( responses.canModifyResponse(
            EvalTestDataLoad.MAINT_USER_ID,	etdl.response1.getId()) );
      Assert.assertFalse( responses.canModifyResponse(
            EvalTestDataLoad.STUDENT_USER_ID, etdl.response1.getId()) );

      // test no one can modify locked responses
      Assert.assertFalse( responses.canModifyResponse(
            EvalTestDataLoad.ADMIN_USER_ID,	etdl.response3.getId()) );
      Assert.assertFalse( responses.canModifyResponse(
            EvalTestDataLoad.STUDENT_USER_ID, etdl.response3.getId()) );

      // test invalid id causes failure
      try {
         responses.canModifyResponse( EvalTestDataLoad.ADMIN_USER_ID, 
               EvalTestDataLoad.INVALID_LONG_ID );
         Assert.fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         Assert.assertNotNull(e);
      }

   }

   public void testGetEvaluationResponseForUserAndGroup() {
      EvalResponse response = null;

      // check retrieving an existing responses
      response = responses.getEvaluationResponseForUserAndGroup(etdl.evaluationClosed.getId(), EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
      assertNotNull(response);
      assertEquals(etdl.response2.getId(), response.getId());

      // check creating a new response
      response = responses.getEvaluationResponseForUserAndGroup(etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
      assertNotNull(response);

      // test cannot create response for closed evaluation
      try {
         response = responses.getEvaluationResponseForUserAndGroup(etdl.evaluationClosed.getId(), EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

      // test invalid permissions to create response fails
      try {
         response = responses.getEvaluationResponseForUserAndGroup(etdl.evaluationActive.getId(), EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF);
         Assert.fail("Should have thrown exception");
      } catch (IllegalStateException e) {
         Assert.assertNotNull(e);
      }

   }

}
