/**
 * $Id$
 * $URL$
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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.exceptions.ResponseSaveException;
import org.sakaiproject.evaluation.logic.externals.EvalSecurityChecksImpl;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.mocks.MockEvalJobLogic;
import org.sakaiproject.evaluation.test.mocks.MockExternalHierarchyLogic;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;


/**
 * Test class for EvalDeliveryServiceImpl
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalDeliveryServiceImplTest extends BaseTestEvalLogic {

    protected EvalDeliveryServiceImpl deliveryService;
    protected EvalSettings settings;
    protected EvalEvaluationSetupServiceImpl evaluationSetupService; // needed to load up evals before test

    private EvalEvaluation evaluationClosedTwo;
    private EvalEvaluation evaluationActiveTwo;
    private EvalEvaluation evaluationActiveThree;

    // run this before each test starts
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();

        // load up any other needed spring beans
        settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
        if (settings == null) {
            throw new NullPointerException("EvalSettings could not be retrieved from spring evalGroupId");
        }

        EvalSecurityChecksImpl securityChecks = 
            (EvalSecurityChecksImpl) applicationContext.getBean("org.sakaiproject.evaluation.logic.externals.EvalSecurityChecks");
        if (securityChecks == null) {
            throw new NullPointerException("EvalSecurityChecksImpl could not be retrieved from spring context");
        }

        EvalEvaluationService evaluationService = (EvalEvaluationService) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalEvaluationService");
        if (evaluationService == null) {
            throw new NullPointerException("EvalEvaluationService could not be retrieved from spring context");
        }

        // setup the mock objects if needed
        MockExternalHierarchyLogic hierarchyLogic = new MockExternalHierarchyLogic();

        // create the other needed logic impls
        EvalAuthoringServiceImpl authoringServiceImpl = new EvalAuthoringServiceImpl();
        authoringServiceImpl.setDao(evaluationDao);
        authoringServiceImpl.setCommonLogic(commonLogic);
        authoringServiceImpl.setSettings(settings);
        authoringServiceImpl.setSecurityChecks(securityChecks);

        EvalEmailsLogicImpl emailsLogic = new EvalEmailsLogicImpl();
        emailsLogic.setCommonLogic(commonLogic);
        emailsLogic.setEvaluationService(evaluationService);
        emailsLogic.setSettings(settings);

        evaluationSetupService = new EvalEvaluationSetupServiceImpl();
        evaluationSetupService.setAuthoringService(authoringServiceImpl);
        evaluationSetupService.setCommonLogic(commonLogic);
        evaluationSetupService.setDao(evaluationDao);
        evaluationSetupService.setEmails(emailsLogic);
        evaluationSetupService.setEvalJobLogic( new MockEvalJobLogic() );
        evaluationSetupService.setEvaluationService(evaluationService);
        evaluationSetupService.setHierarchyLogic(hierarchyLogic);
        evaluationSetupService.setSecurityChecks(securityChecks);
        evaluationSetupService.setSettings(settings);

        // create and setup the object to be tested
        deliveryService = new EvalDeliveryServiceImpl();
        deliveryService.setDao(evaluationDao);
        deliveryService.setCommonLogic(commonLogic);
        deliveryService.setHierarchyLogic( hierarchyLogic );
        deliveryService.setEvaluationService(evaluationService);
        deliveryService.setSettings(settings);
        deliveryService.setAuthoringService( authoringServiceImpl );
    }

    // run this before each test starts and as part of the transaction
    protected void onSetUpInTransaction() {
        // preload additional data if desired

        // Evaluation Complete (ended yesterday, viewable tomorrow), recent close
        evaluationClosedTwo = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, 
                EvalTestDataLoad.ADMIN_USER_ID, "Eval closed two", null, 
                etdl.threeDaysAgo, etdl.yesterday, 
                etdl.yesterday, etdl.tomorrow, false, null,
                false, null, 
                EvalConstants.EVALUATION_STATE_CLOSED, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_OPT_IN, Integer.valueOf(2), null, null, null, null,
                etdl.templateAdmin, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                EvalTestDataLoad.LOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null);
        evaluationDao.save(evaluationClosedTwo);

        // Evaluation Active Two (ends today), viewable tomorrow, all requireable items are required
        evaluationActiveTwo = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, 
                EvalTestDataLoad.MAINT_USER_ID, "Eval active two", null, 
                etdl.yesterday, etdl.today, etdl.today, etdl.tomorrow, false, null, false, null, 
                EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.SHARING_VISIBLE, 
                EvalConstants.INSTRUCTOR_OPT_IN, new Integer(0), null, null, null, null,
                etdl.templateUnused, null, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE,
                Boolean.FALSE, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null);
        evaluationDao.save(evaluationActiveTwo);

        EvalAssignGroup assign2 = new EvalAssignGroup( EvalTestDataLoad.MAINT_USER_ID, 
                EvalTestDataLoad.SITE1_REF, EvalConstants.GROUP_TYPE_SITE, 
                evaluationActiveTwo, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        evaluationSetupService.saveAssignGroup(assign2, EvalTestDataLoad.MAINT_USER_ID);

        // Evaluation Active Three (ends today), viewable tomorrow, only compulsory items are required
        evaluationActiveThree = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, 
                EvalTestDataLoad.MAINT_USER_ID, "Eval active three", null, 
                etdl.yesterday, etdl.today, etdl.today, etdl.tomorrow, false, null, false, null, 
                EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.SHARING_VISIBLE, 
                EvalConstants.INSTRUCTOR_OPT_IN, new Integer(0), null, null, null, null,
                etdl.templateUnused, null, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE,
                Boolean.FALSE, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null);
        evaluationDao.save(evaluationActiveThree);

        EvalAssignGroup assign3 = new EvalAssignGroup( EvalTestDataLoad.MAINT_USER_ID, 
                EvalTestDataLoad.SITE1_REF, EvalConstants.GROUP_TYPE_SITE, 
                evaluationActiveThree, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        evaluationSetupService.saveAssignGroup(assign3, EvalTestDataLoad.MAINT_USER_ID);

    }



    /**
     * ADD unit tests below here, use testMethod as the name of the unit test,
     * Note that if a method is overloaded you should include the arguments in the
     * test name like so: testMethodClassInt (for method(Class, int);
     */


    public void testGetResponseById() {
        EvalResponse response = null;

        response = deliveryService.getResponseById( etdl.response1.getId() );
        assertNotNull(response);
        assertEquals(etdl.response1.getId(), response.getId());

        response = deliveryService.getResponseById( etdl.response2.getId() );
        assertNotNull(response);
        assertEquals(etdl.response2.getId(), response.getId());

        // test get eval by invalid id
        response = deliveryService.getResponseById( EvalTestDataLoad.INVALID_LONG_ID );
        assertNull(response);
    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#getEvaluationResponsesForUser(java.lang.String, java.lang.Long[], boolean)}.
     */
    public void testGetEvaluationResponses() {
        List<EvalResponse> l = null;
        List<Long> ids = null;

        // retrieve one response for a normal user in one eval
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId() }, true );
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response1.getId() ));

        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.STUDENT_USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, true );
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response3.getId() ));

        // retrieve all responses for a normal user in one eval
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, true );
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response2.getId() ));
        assertTrue(ids.contains( etdl.response6.getId() ));

        // retrieve all responses for a normal user in mutliple evals
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, true );
        assertNotNull(l);
        assertEquals(4, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response1.getId() ));
        assertTrue(ids.contains( etdl.response2.getId() ));
        assertTrue(ids.contains( etdl.response4.getId() ));
        assertTrue(ids.contains( etdl.response6.getId() ));

        // attempt to retrieve all responses
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, null );
        assertNotNull(l);
        assertEquals(4, l.size());

        // attempt to retrieve all incomplete responses (there are none)
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, false );
        assertNotNull(l);
        assertEquals(0, l.size());

        // attempt to retrieve results for user that has no responses
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.STUDENT_USER_ID, 
                new Long[] { etdl.evaluationActive.getId() }, true );
        assertNotNull(l);
        assertEquals(0, l.size());

        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.MAINT_USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, true );
        assertNotNull(l);
        assertEquals(0, l.size());

        // test that admin can fetch all results for evaluationSetupService
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.ADMIN_USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, true );
        assertNotNull(l);
        assertEquals(6, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response1.getId() ));
        assertTrue(ids.contains( etdl.response2.getId() ));
        assertTrue(ids.contains( etdl.response3.getId() ));
        assertTrue(ids.contains( etdl.response4.getId() ));
        assertTrue(ids.contains( etdl.response5.getId() ));
        assertTrue(ids.contains( etdl.response6.getId() ));

        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.ADMIN_USER_ID, 
                new Long[] { etdl.evaluationViewable.getId() }, true );
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response4.getId() ));
        assertTrue(ids.contains( etdl.response5.getId() ));

        // check that empty array causes failure
        try {
            l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.ADMIN_USER_ID, 
                    new Long[] {}, true );
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        // check that invalid IDs cause failure
        try {
            l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.ADMIN_USER_ID, 
                    new Long[] { EvalTestDataLoad.INVALID_LONG_ID }, true );
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        try {
            l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.ADMIN_USER_ID, 
                    new Long[] { etdl.evaluationViewable.getId(), EvalTestDataLoad.INVALID_LONG_ID }, true );
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#countResponses(Long, String)}.
     */
    public void testCountResponses() {

        // test counts for all responses in various evaluationSetupService
        assertEquals(3, deliveryService.countResponses( etdl.evaluationClosed.getId(), null, null) );
        assertEquals(2, deliveryService.countResponses( etdl.evaluationViewable.getId(), null, null) );
        assertEquals(1, deliveryService.countResponses( etdl.evaluationActive.getId(), null, null) );
        assertEquals(0, deliveryService.countResponses( etdl.evaluationActiveUntaken.getId(), null, null) );

        // test counts limited by evalGroupId
        assertEquals(1, deliveryService.countResponses( etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE1_REF, null) );
        assertEquals(0, deliveryService.countResponses( etdl.evaluationViewable.getId(), EvalTestDataLoad.SITE1_REF, null) );
        assertEquals(1, deliveryService.countResponses( etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF, null) );
        assertEquals(0, deliveryService.countResponses( etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE1_REF, null) );

        assertEquals(2, deliveryService.countResponses( etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE2_REF, null) );
        assertEquals(2, deliveryService.countResponses( etdl.evaluationViewable.getId(), EvalTestDataLoad.SITE2_REF, null) );
        assertEquals(0, deliveryService.countResponses( etdl.evaluationActive.getId(), EvalTestDataLoad.SITE2_REF, null) );
        assertEquals(0, deliveryService.countResponses( etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE2_REF, null) );

        // test counts limited by completed
        assertEquals(3, deliveryService.countResponses( etdl.evaluationClosed.getId(), null, true) );
        assertEquals(2, deliveryService.countResponses( etdl.evaluationViewable.getId(), null, true) );
        assertEquals(1, deliveryService.countResponses( etdl.evaluationActive.getId(), null, true) );
        assertEquals(0, deliveryService.countResponses( etdl.evaluationActiveUntaken.getId(), null, true) );

        // test counts limited by incomplete
        assertEquals(0, deliveryService.countResponses( etdl.evaluationClosed.getId(), null, false) );
        assertEquals(0, deliveryService.countResponses( etdl.evaluationViewable.getId(), null, false) );
        assertEquals(0, deliveryService.countResponses( etdl.evaluationActive.getId(), null, false) );
        assertEquals(0, deliveryService.countResponses( etdl.evaluationActiveUntaken.getId(), null, false) );

        // check that invalid IDs cause failure
        try {
            deliveryService.countResponses( EvalTestDataLoad.INVALID_LONG_ID, null, null );
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#getEvalAnswers(java.lang.Long, java.lang.Long)}.
     */
    public void testGetEvalAnswers() {
        List<EvalAnswer> l = null;
        List<Long> ids = null;

        // test getting all answers first
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), null, null);
        assertNotNull(l);
        assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        assertTrue(ids.contains( etdl.answer2_5A.getId() ));
        assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // restrict to template item
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), null, new Long[] {etdl.templateItem2A.getId()});
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // restrict to multiple template items
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), null, new Long[] {etdl.templateItem2A.getId(), etdl.templateItem5A.getId()});
        assertNotNull(l);
        assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        assertTrue(ids.contains( etdl.answer2_5A.getId() ));
        assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // test restricting to groups
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, null);
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        assertTrue(ids.contains( etdl.answer2_5A.getId() ));

        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, null);
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // test restricting to groups and TIs
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, new Long[] {etdl.templateItem2A.getId()});
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer2_2A.getId() ));

        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, new Long[] {etdl.templateItem2A.getId()});
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // test restricting to answers not in this group
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, new Long[] {etdl.templateItem5A.getId()});
        assertNotNull(l);
        assertEquals(0, l.size());

        // test template item that is not in this evaluation
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), null, new Long[] {etdl.templateItem1U.getId()});
        assertNotNull(l);
        assertEquals(0, l.size());

        // check that invalid ids cause failure
        try {
            l = deliveryService.getAnswersForEval( EvalTestDataLoad.INVALID_LONG_ID, null, null );
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#getEvalResponseIds(Long, String[])}.
     */
    public void testGetEvalResponseIds() {
        List<Long> l = null;

        // retrieve all response Ids for an evaluation
        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), null, true);
        assertNotNull(l);
        assertEquals(3, l.size());
        assertTrue(l.contains( etdl.response2.getId() ));
        assertTrue(l.contains( etdl.response3.getId() ));
        assertTrue(l.contains( etdl.response6.getId() ));

        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), new String[] {}, true);
        assertNotNull(l);
        assertEquals(3, l.size());
        assertTrue(l.contains( etdl.response2.getId() ));
        assertTrue(l.contains( etdl.response3.getId() ));
        assertTrue(l.contains( etdl.response6.getId() ));

        // retrieve all response Ids for an evaluation using all groups
        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), 
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, true);
        assertNotNull(l);
        assertEquals(3, l.size());
        assertTrue(l.contains( etdl.response2.getId() ));
        assertTrue(l.contains( etdl.response3.getId() ));
        assertTrue(l.contains( etdl.response6.getId() ));

        // test retrieval of all responses for an evaluation
        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), 
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null);
        assertNotNull(l);
        assertEquals(3, l.size());

        // test retrieval of incomplete responses for an evaluation
        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), 
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, false);
        assertNotNull(l);
        assertEquals(0, l.size());

        // retrieve all response Ids for an evaluation in one group only
        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, true);
        assertNotNull(l);
        assertEquals(1, l.size());
        assertTrue(l.contains( etdl.response2.getId() ));

        // retrieve all response Ids for an evaluation in one group only
        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, true);
        assertNotNull(l);
        assertEquals(2, l.size());
        assertTrue(l.contains( etdl.response3.getId() ));
        assertTrue(l.contains( etdl.response6.getId() ));

        l = deliveryService.getEvalResponseIds(etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, true);
        assertNotNull(l);
        assertEquals(1, l.size());
        assertTrue(l.contains( etdl.response1.getId() ));

        // try to get responses for an eval group that is not associated with this eval
        l = deliveryService.getEvalResponseIds(etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, true);
        assertNotNull(l);
        assertEquals(0, l.size());

        // try to get responses for an eval with no responses
        l = deliveryService.getEvalResponseIds(etdl.evaluationActiveUntaken.getId(), null, true);
        assertNotNull(l);
        assertEquals(0, l.size());

        // check that invalid eval ids cause failure
        try {
            l = deliveryService.getEvalResponseIds( EvalTestDataLoad.INVALID_LONG_ID, null, true);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#saveResponse(org.sakaiproject.evaluation.model.EvalResponse, String)}.
     */
    public void testSaveResponse() {

        // test saving a response with no answers is ok
        EvalResponse responseNone = new EvalResponse( EvalTestDataLoad.STUDENT_USER_ID, EvalTestDataLoad.SITE1_REF, 
                etdl.evaluationActiveUntaken, new Date());
        deliveryService.saveResponse( responseNone, EvalTestDataLoad.STUDENT_USER_ID);
        assertNotNull(responseNone.getId());

        // test saving a response when admin user is ok
        deliveryService.saveResponse( new EvalResponse( EvalTestDataLoad.ADMIN_USER_ID, 
                EvalTestDataLoad.SITE1_REF, etdl.evaluationActiveUntaken, 
                new Date()), 
                EvalTestDataLoad.ADMIN_USER_ID);

        // test saving a response for a closed evaluation fails
        try {
            deliveryService.saveResponse( new EvalResponse( EvalTestDataLoad.USER_ID, 
                    EvalTestDataLoad.SITE1_REF, evaluationClosedTwo, 
                    new Date()), 
                    EvalTestDataLoad.USER_ID);
            fail("Should have thrown exception");
        } catch (IllegalStateException e) {
            assertNotNull(e);
        }

        // test saving a response when user has no permission fails
        try {
            deliveryService.saveResponse( new EvalResponse( EvalTestDataLoad.MAINT_USER_ID, 
                    EvalTestDataLoad.SITE2_REF, etdl.evaluationActiveUntaken, 
                    new Date()), 
                    EvalTestDataLoad.MAINT_USER_ID);
            fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            assertNotNull(e);
            assertEquals(ResponseSaveException.TYPE_CANNOT_TAKE_EVAL, e.type);
        }

        // test saving a response for an invalid evalGroupId fails (evalGroupId not assigned to this eval)
        try {
            deliveryService.saveResponse( new EvalResponse( EvalTestDataLoad.USER_ID, 
                    EvalTestDataLoad.SITE3_REF, etdl.evaluationActive, 
                    new Date()), 
                    EvalTestDataLoad.USER_ID);
            fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            assertNotNull(e);
            assertEquals(ResponseSaveException.TYPE_CANNOT_TAKE_EVAL, e.type);
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
        //    fail("Should have thrown exception");
        //    } catch (IllegalArgumentException e) {
        //    assertNotNull(e);
        //    fail(e.getMessage());
        //    }

        // test saving a response with valid answers is ok (make sure answers saved also)
        EvalResponse responseAns = new EvalResponse( EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF, 
                etdl.evaluationActiveUntaken, new Date());
        responseAns.setAnswers( new HashSet<EvalAnswer>() );
        EvalAnswer answer1_1 = new EvalAnswer( responseAns, etdl.templateItem1P, etdl.item1, null, null, "text");
        responseAns.getAnswers().add( answer1_1 );
        deliveryService.saveResponse( responseAns, EvalTestDataLoad.USER_ID);
        assertNotNull(responseAns.getId());
        assertNotNull(answer1_1.getId());

        // test updating an already created response is ok
        responseNone.setEndTime( new Date() );
        deliveryService.saveResponse( responseNone, EvalTestDataLoad.STUDENT_USER_ID);

        // test adding answers to an already created response is ok
        // make sure the answer is saved also
        responseNone.setAnswers( new HashSet<EvalAnswer>() );
        EvalAnswer answer2_1 = new EvalAnswer( responseNone, etdl.templateItem1P, etdl.item1, null, null, Integer.valueOf(1));
        responseNone.getAnswers().add( answer2_1 );
        deliveryService.saveResponse( responseNone, EvalTestDataLoad.STUDENT_USER_ID);
        assertNotNull(answer2_1.getId());

        // force the system setting to be cleared so the eval setting takes over
        settings.set(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED, null);


        // test saving a response without all required answers (eval set to all required, TI_1_1 is required)
        EvalResponse responseRequired = new EvalResponse( EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF, 
                evaluationActiveTwo, new Date());
        responseRequired.setEndTime( new Date() );
        responseRequired.setAnswers( new HashSet<EvalAnswer>() );
        EvalAnswer RA_3_3 = new EvalAnswer( responseRequired, etdl.templateItem3U, etdl.item3, null, null, 2 );
        responseRequired.getAnswers().add( RA_3_3 );
        try {
            deliveryService.saveResponse( responseRequired, EvalTestDataLoad.USER_ID);
            fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            assertNotNull(e);
            assertEquals(ResponseSaveException.TYPE_MISSING_REQUIRED_ANSWERS, e.type);
            assertEquals(1, e.missingItemAnswerKeys.length);
            String aKey = TemplateItemUtils.makeTemplateItemAnswerKey(etdl.templateItem1U.getId(), EvalConstants.ITEM_CATEGORY_INSTRUCTOR, EvalTestDataLoad.MAINT_USER_ID);
            assertEquals(aKey, e.missingItemAnswerKeys[0]);
        }
        assertNull(responseRequired.getId());
        assertNull(RA_3_3.getId());

        // test saving with course related instead of instructor related item (invalid item)
        EvalAnswer RA_1_1 = new EvalAnswer( responseRequired, etdl.templateItem1U, etdl.item1, null, null, 2 );
        responseRequired.getAnswers().add( RA_1_1 );
        try {
            deliveryService.saveResponse( responseRequired, EvalTestDataLoad.USER_ID);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
        responseRequired.getAnswers().remove( RA_1_1 );
        assertNull(responseRequired.getId());
        assertNull(RA_1_1.getId());
        assertNull(RA_3_3.getId());


        // test saving a response when answers are required (leaving out non-required answers)
        RA_1_1 = new EvalAnswer( responseRequired, etdl.templateItem1U, etdl.item1, 
                EvalTestDataLoad.MAINT_USER_ID, EvalConstants.ITEM_CATEGORY_INSTRUCTOR, 2 );
        responseRequired.getAnswers().add( RA_1_1 );
        responseRequired.setEndTime( new Date() );
        deliveryService.saveResponse( responseRequired, EvalTestDataLoad.USER_ID);
        assertNotNull(responseRequired.getId());
        assertNotNull(RA_1_1.getId());
        assertNotNull(RA_3_3.getId());


        // test saving a response without all compulsory items (TI_1_1 is compulsory)
        EvalResponse responseCompulsory = new EvalResponse( EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF, 
                evaluationActiveThree, new Date() );
        responseCompulsory.setEndTime( new Date() );
        responseCompulsory.setAnswers( new HashSet<EvalAnswer>() );
        EvalAnswer CA_3_3 = new EvalAnswer( responseCompulsory, etdl.templateItem3U, etdl.item3, null, null, 2 );
        responseCompulsory.getAnswers().add( CA_3_3 );
        try {
            deliveryService.saveResponse( responseCompulsory, EvalTestDataLoad.USER_ID);
            fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            assertNotNull(e);
            assertEquals(ResponseSaveException.TYPE_MISSING_REQUIRED_ANSWERS, e.type);
            assertEquals(1, e.missingItemAnswerKeys.length);
            String aKey = TemplateItemUtils.makeTemplateItemAnswerKey(etdl.templateItem1U.getId(), EvalConstants.ITEM_CATEGORY_INSTRUCTOR, EvalTestDataLoad.MAINT_USER_ID);
            assertEquals(aKey, e.missingItemAnswerKeys[0]);
        }
        assertNull(responseCompulsory.getId());
        assertNull(CA_3_3.getId());

        // now make sure we can save it if we add in the required stuff
        EvalAnswer CA_1_1 = new EvalAnswer( responseCompulsory, etdl.templateItem1U, etdl.item1, 
                EvalTestDataLoad.MAINT_USER_ID, EvalConstants.ITEM_CATEGORY_INSTRUCTOR, 2 );
        responseCompulsory.getAnswers().add( CA_1_1 );
        responseCompulsory.setEndTime( new Date() );
        deliveryService.saveResponse( responseCompulsory, EvalTestDataLoad.USER_ID);
        assertNotNull(responseCompulsory.getId());
        assertNotNull(CA_1_1.getId());
        assertNotNull(CA_3_3.getId());


        // TODO - cannot do this test until hibernate issue resolved
        //    // test that changing anything else on the existing response fails
        //    try {
        //    responseNone.setContext( EvalTestDataLoad.CONTEXT3 );
        //    responses.saveResponse( responseNone, EvalTestDataLoad.STUDENT_USER_ID);
        //    fail("Should have thrown exception");
        //    } catch (IllegalArgumentException e) {
        //    assertNotNull(e);
        //    fail(e.getMessage());
        //    }

        // test saving a response without proper ownership fails
        try {
            responseNone.setEndTime( new Date() );
            deliveryService.saveResponse( responseNone, EvalTestDataLoad.USER_ID);
            fail("Should have thrown exception");
        } catch (SecurityException e) {
            assertNotNull(e);
        }

        // test saving a response when one exists fails
        try {
            deliveryService.saveResponse( new EvalResponse( EvalTestDataLoad.USER_ID, 
                    EvalTestDataLoad.SITE1_REF, etdl.evaluationActive, 
                    new Date()), 
                    EvalTestDataLoad.USER_ID);
            fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            assertNotNull(e);
            assertEquals(ResponseSaveException.TYPE_CANNOT_TAKE_EVAL, e.type);
        }

        try {
            deliveryService.saveResponse( new EvalResponse( EvalTestDataLoad.MAINT_USER_ID, 
                    EvalTestDataLoad.SITE2_REF, etdl.evaluationActive, 
                    new Date()), 
                    EvalTestDataLoad.MAINT_USER_ID);
            fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            assertNotNull(e);
            assertEquals(ResponseSaveException.TYPE_CANNOT_TAKE_EVAL, e.type);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#canModifyResponse(java.lang.String, java.lang.Long)}.
     */
    public void testCanModifyResponse() {

        // test owner can modify unlocked
        assertTrue( deliveryService.canModifyResponse(
                EvalTestDataLoad.USER_ID, etdl.response1.getId()) );

        // test admin cannot override permissions
        assertFalse( deliveryService.canModifyResponse(
                EvalTestDataLoad.ADMIN_USER_ID,	etdl.response1.getId()) );

        // test users without perms cannot modify
        assertFalse( deliveryService.canModifyResponse(
                EvalTestDataLoad.MAINT_USER_ID,	etdl.response1.getId()) );
        assertFalse( deliveryService.canModifyResponse(
                EvalTestDataLoad.STUDENT_USER_ID, etdl.response1.getId()) );

        // test no one can modify locked responses
        assertFalse( deliveryService.canModifyResponse(
                EvalTestDataLoad.ADMIN_USER_ID,	etdl.response3.getId()) );
        assertFalse( deliveryService.canModifyResponse(
                EvalTestDataLoad.STUDENT_USER_ID, etdl.response3.getId()) );

        // test invalid id causes failure
        try {
            deliveryService.canModifyResponse( EvalTestDataLoad.ADMIN_USER_ID, 
                    EvalTestDataLoad.INVALID_LONG_ID );
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

    }

    public void testGetEvaluationResponseForUserAndGroup() {
        EvalResponse response = null;

        // check retrieving an existing responses
        response = deliveryService.getEvaluationResponseForUserAndGroup(etdl.evaluationClosed.getId(), EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
        assertNotNull(response);
        assertEquals(etdl.response2.getId(), response.getId());

        // check creating a new response
        response = deliveryService.getEvaluationResponseForUserAndGroup(etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
        assertNotNull(response);

        // test cannot create response for closed evaluation
        try {
            response = deliveryService.getEvaluationResponseForUserAndGroup(etdl.evaluationClosed.getId(), EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF);
            fail("Should have thrown exception");
        } catch (IllegalStateException e) {
            assertNotNull(e);
        }

        // test invalid permissions to create response fails
        try {
            response = deliveryService.getEvaluationResponseForUserAndGroup(etdl.evaluationActive.getId(), EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF);
            fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            assertNotNull(e);
            assertEquals(ResponseSaveException.TYPE_CANNOT_TAKE_EVAL, e.type);
        }

    }

}
