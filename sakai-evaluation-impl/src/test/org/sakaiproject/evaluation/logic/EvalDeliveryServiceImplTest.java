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

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
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
    @Ignore
    @Before
    public void onSetUpBeforeTransaction() throws Exception {
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

        // Evaluation Complete (ended yesterday, viewable tomorrow), recent close
        evaluationClosedTwo = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, 
                EvalTestDataLoad.ADMIN_USER_ID, "Eval closed two", null, 
                etdl.threeDaysAgo, etdl.yesterday, 
                etdl.yesterday, etdl.tomorrow, false, null,
                false, null, 
                EvalConstants.EVALUATION_STATE_CLOSED, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_OPT_IN, 2, null, null, null, null,
                etdl.templateAdmin, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                EvalTestDataLoad.LOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null);
        evaluationDao.save(evaluationClosedTwo);

        // Evaluation Active Two (ends today), viewable tomorrow, all requireable items are required
        evaluationActiveTwo = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, 
                EvalTestDataLoad.MAINT_USER_ID, "Eval active two", null, 
                etdl.yesterday, etdl.today, etdl.today, etdl.tomorrow, false, null, false, null, 
                EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.SHARING_VISIBLE, 
                EvalConstants.INSTRUCTOR_OPT_IN, 0, null, null, null, null,
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
                EvalConstants.INSTRUCTOR_OPT_IN, 0, null, null, null, null,
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

    @Ignore
    @Test
    public void testGetResponseById() {
        EvalResponse response;

        response = deliveryService.getResponseById( etdl.response1.getId() );
        Assert.assertNotNull(response);
        Assert.assertEquals(etdl.response1.getId(), response.getId());

        response = deliveryService.getResponseById( etdl.response2.getId() );
        Assert.assertNotNull(response);
        Assert.assertEquals(etdl.response2.getId(), response.getId());

        // test get eval by invalid id
        response = deliveryService.getResponseById( EvalTestDataLoad.INVALID_LONG_ID );
        Assert.assertNull(response);
    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#getEvaluationResponsesForUser(java.lang.String, java.lang.Long[], boolean)}.
     */
    @Ignore
    @Test
    public void testGetEvaluationResponses() {
        List<EvalResponse> l;
        List<Long> ids;

        // retrieve one response for a normal user in one eval
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId() }, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response1.getId() ));

        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.STUDENT_USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response3.getId() ));

        // retrieve all responses for a normal user in one eval
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response2.getId() ));
        Assert.assertTrue(ids.contains( etdl.response6.getId() ));

        // retrieve all responses for a normal user in mutliple evals
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(4, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response1.getId() ));
        Assert.assertTrue(ids.contains( etdl.response2.getId() ));
        Assert.assertTrue(ids.contains( etdl.response4.getId() ));
        Assert.assertTrue(ids.contains( etdl.response6.getId() ));

        // attempt to retrieve all responses
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, null );
        Assert.assertNotNull(l);
        Assert.assertEquals(4, l.size());

        // attempt to retrieve all incomplete responses (there are none)
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, false );
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // attempt to retrieve results for user that has no responses
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.STUDENT_USER_ID, 
                new Long[] { etdl.evaluationActive.getId() }, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.MAINT_USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // test that admin can fetch all results for evaluationSetupService
        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.ADMIN_USER_ID, 
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

        l = deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.ADMIN_USER_ID, 
                new Long[] { etdl.evaluationViewable.getId() }, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response4.getId() ));
        Assert.assertTrue(ids.contains( etdl.response5.getId() ));

        // check that empty array causes Assert.failure
        try {
            deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.ADMIN_USER_ID, 
                    new Long[] {}, true );
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

        // check that invalid IDs cause Assert.failure
        try {
            deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.ADMIN_USER_ID, 
                    new Long[] { EvalTestDataLoad.INVALID_LONG_ID }, true );
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

        try {
            deliveryService.getEvaluationResponsesForUser(EvalTestDataLoad.ADMIN_USER_ID, 
                    new Long[] { etdl.evaluationViewable.getId(), EvalTestDataLoad.INVALID_LONG_ID }, true );
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#countResponses(Long, String)}.
     */
    @Ignore
    @Test
    public void testCountResponses() {

        // test counts for all responses in various evaluationSetupService
        Assert.assertEquals(3, deliveryService.countResponses( etdl.evaluationClosed.getId(), null, null) );
        Assert.assertEquals(2, deliveryService.countResponses( etdl.evaluationViewable.getId(), null, null) );
        Assert.assertEquals(1, deliveryService.countResponses( etdl.evaluationActive.getId(), null, null) );
        Assert.assertEquals(0, deliveryService.countResponses( etdl.evaluationActiveUntaken.getId(), null, null) );

        // test counts limited by evalGroupId
        Assert.assertEquals(1, deliveryService.countResponses( etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE1_REF, null) );
        Assert.assertEquals(0, deliveryService.countResponses( etdl.evaluationViewable.getId(), EvalTestDataLoad.SITE1_REF, null) );
        Assert.assertEquals(1, deliveryService.countResponses( etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF, null) );
        Assert.assertEquals(0, deliveryService.countResponses( etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE1_REF, null) );

        Assert.assertEquals(2, deliveryService.countResponses( etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE2_REF, null) );
        Assert.assertEquals(2, deliveryService.countResponses( etdl.evaluationViewable.getId(), EvalTestDataLoad.SITE2_REF, null) );
        Assert.assertEquals(0, deliveryService.countResponses( etdl.evaluationActive.getId(), EvalTestDataLoad.SITE2_REF, null) );
        Assert.assertEquals(0, deliveryService.countResponses( etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE2_REF, null) );

        // test counts limited by completed
        Assert.assertEquals(3, deliveryService.countResponses( etdl.evaluationClosed.getId(), null, true) );
        Assert.assertEquals(2, deliveryService.countResponses( etdl.evaluationViewable.getId(), null, true) );
        Assert.assertEquals(1, deliveryService.countResponses( etdl.evaluationActive.getId(), null, true) );
        Assert.assertEquals(0, deliveryService.countResponses( etdl.evaluationActiveUntaken.getId(), null, true) );

        // test counts limited by incomplete
        Assert.assertEquals(0, deliveryService.countResponses( etdl.evaluationClosed.getId(), null, false) );
        Assert.assertEquals(0, deliveryService.countResponses( etdl.evaluationViewable.getId(), null, false) );
        Assert.assertEquals(0, deliveryService.countResponses( etdl.evaluationActive.getId(), null, false) );
        Assert.assertEquals(0, deliveryService.countResponses( etdl.evaluationActiveUntaken.getId(), null, false) );

        // check that invalid IDs cause Assert.failure
        try {
            deliveryService.countResponses( EvalTestDataLoad.INVALID_LONG_ID, null, null );
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#getEvalAnswers(java.lang.Long, java.lang.Long)}.
     */
    @Ignore
    @Test
    public void testGetEvalAnswers() {
        List<EvalAnswer> l;
        List<Long> ids;

        // test getting all answers first
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        Assert.assertTrue(ids.contains( etdl.answer2_5A.getId() ));
        Assert.assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // restrict to template item
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), null, new Long[] {etdl.templateItem2A.getId()});
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        Assert.assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // restrict to multiple template items
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), null, new Long[] {etdl.templateItem2A.getId(), etdl.templateItem5A.getId()});
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        Assert.assertTrue(ids.contains( etdl.answer2_5A.getId() ));
        Assert.assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // test restricting to groups
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        Assert.assertTrue(ids.contains( etdl.answer2_5A.getId() ));

        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // test restricting to groups and TIs
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, new Long[] {etdl.templateItem2A.getId()});
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer2_2A.getId() ));

        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, new Long[] {etdl.templateItem2A.getId()});
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // test restricting to answers not in this group
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, new Long[] {etdl.templateItem5A.getId()});
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // test template item that is not in this evaluation
        l = deliveryService.getAnswersForEval(etdl.evaluationClosed.getId(), null, new Long[] {etdl.templateItem1U.getId()});
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // check that invalid ids cause Assert.failure
        try {
            deliveryService.getAnswersForEval( EvalTestDataLoad.INVALID_LONG_ID, null, null );
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#getEvalResponseIds(Long, String[])}.
     */
    @Ignore
    @Test
    public void testGetEvalResponseIds() {
        List<Long> l;

        // retrieve all response Ids for an evaluation
        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertTrue(l.contains( etdl.response2.getId() ));
        Assert.assertTrue(l.contains( etdl.response3.getId() ));
        Assert.assertTrue(l.contains( etdl.response6.getId() ));

        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), new String[] {}, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertTrue(l.contains( etdl.response2.getId() ));
        Assert.assertTrue(l.contains( etdl.response3.getId() ));
        Assert.assertTrue(l.contains( etdl.response6.getId() ));

        // retrieve all response Ids for an evaluation using all groups
        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), 
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertTrue(l.contains( etdl.response2.getId() ));
        Assert.assertTrue(l.contains( etdl.response3.getId() ));
        Assert.assertTrue(l.contains( etdl.response6.getId() ));

        // test retrieval of all responses for an evaluation
        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), 
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());

        // test retrieval of incomplete responses for an evaluation
        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), 
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // retrieve all response Ids for an evaluation in one group only
        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(l.contains( etdl.response2.getId() ));

        // retrieve all response Ids for an evaluation in one group only
        l = deliveryService.getEvalResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(l.contains( etdl.response3.getId() ));
        Assert.assertTrue(l.contains( etdl.response6.getId() ));

        l = deliveryService.getEvalResponseIds(etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(l.contains( etdl.response1.getId() ));

        // try to get responses for an eval group that is not associated with this eval
        l = deliveryService.getEvalResponseIds(etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // try to get responses for an eval with no responses
        l = deliveryService.getEvalResponseIds(etdl.evaluationActiveUntaken.getId(), null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // check that invalid eval ids cause Assert.failure
        try {
            deliveryService.getEvalResponseIds( EvalTestDataLoad.INVALID_LONG_ID, null, true);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#saveResponse(org.sakaiproject.evaluation.model.EvalResponse, String)}.
     */
    @Ignore
    @Test
    public void testSaveResponse() {

        // test saving a response with no answers is ok
        EvalResponse responseNone = new EvalResponse( EvalTestDataLoad.STUDENT_USER_ID, EvalTestDataLoad.SITE1_REF, 
                etdl.evaluationActiveUntaken, new Date());
        deliveryService.saveResponse( responseNone, EvalTestDataLoad.STUDENT_USER_ID);
        Assert.assertNotNull(responseNone.getId());
        
        // test saving a response for a closed evaluation in grace period ok
        deliveryService.saveResponse( new EvalResponse( EvalTestDataLoad.STUDENT_USER_ID, 
        		EvalTestDataLoad.SITE1_REF, 
        		etdl.evaluationGracePeriod, 
        		new Date()), 
        		EvalTestDataLoad.STUDENT_USER_ID);
        
        // test saving a response when admin user is ok
        deliveryService.saveResponse( new EvalResponse( EvalTestDataLoad.ADMIN_USER_ID, 
                EvalTestDataLoad.SITE1_REF, etdl.evaluationActiveUntaken, 
                new Date()), 
                EvalTestDataLoad.ADMIN_USER_ID);

        // test saving a response for a closed evaluation Assert.fails
        try {
            deliveryService.saveResponse( new EvalResponse( EvalTestDataLoad.USER_ID, 
                    EvalTestDataLoad.SITE1_REF, evaluationClosedTwo, 
                    new Date()), 
                    EvalTestDataLoad.USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

        // test saving a response when user has no permission Assert.fails
        try {
            deliveryService.saveResponse( new EvalResponse( EvalTestDataLoad.MAINT_USER_ID, 
                    EvalTestDataLoad.SITE2_REF, etdl.evaluationActiveUntaken, 
                    new Date()), 
                    EvalTestDataLoad.MAINT_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(ResponseSaveException.TYPE_CANNOT_TAKE_EVAL, e.type);
        }

        // test saving a response for an invalid evalGroupId Assert.fails (evalGroupId not assigned to this eval)
        try {
            deliveryService.saveResponse( new EvalResponse( EvalTestDataLoad.USER_ID, 
                    EvalTestDataLoad.SITE3_REF, etdl.evaluationActive, 
                    new Date()), 
                    EvalTestDataLoad.USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(ResponseSaveException.TYPE_CANNOT_TAKE_EVAL, e.type);
        }

        // TODO - make this work
        //    // test saving a response with invalid answers Assert.fails (item2 not in evaluationActiveUntaken)
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
        EvalResponse responseAns = new EvalResponse( EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF, 
                etdl.evaluationActiveUntaken, new Date());
        responseAns.setAnswers( new HashSet<>() );
        EvalAnswer answer1_1 = new EvalAnswer( responseAns, etdl.templateItem1P, etdl.item1, null, null, "text");
        responseAns.getAnswers().add( answer1_1 );
        deliveryService.saveResponse( responseAns, EvalTestDataLoad.USER_ID);
        Assert.assertNotNull(responseAns.getId());
        Assert.assertNotNull(answer1_1.getId());

        // test updating an already created response is ok
        responseNone.setEndTime( new Date() );
        deliveryService.saveResponse( responseNone, EvalTestDataLoad.STUDENT_USER_ID);

        // test adding answers to an already created response is ok
        // make sure the answer is saved also
        responseNone.setAnswers( new HashSet<>() );
        EvalAnswer answer2_1 = new EvalAnswer( responseNone, etdl.templateItem1P, etdl.item1, null, null, 1);
        responseNone.getAnswers().add( answer2_1 );
        deliveryService.saveResponse( responseNone, EvalTestDataLoad.STUDENT_USER_ID);
        Assert.assertNotNull(answer2_1.getId());

        // force the system setting to be cleared so the eval setting takes over
        settings.set(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED, null);


        // test saving a response without all required answers (eval set to all required, TI_1_1 is required)
        EvalResponse responseRequired = new EvalResponse( EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF, 
                evaluationActiveTwo, new Date());
        responseRequired.setEndTime( new Date() );
        responseRequired.setAnswers( new HashSet<>() );
        EvalAnswer RA_3_3 = new EvalAnswer( responseRequired, etdl.templateItem3U, etdl.item3, null, null, 2 );
        responseRequired.getAnswers().add( RA_3_3 );
        try {
            deliveryService.saveResponse( responseRequired, EvalTestDataLoad.USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(ResponseSaveException.TYPE_MISSING_REQUIRED_ANSWERS, e.type);
            Assert.assertEquals(1, e.missingItemAnswerKeys.length);
            String aKey = TemplateItemUtils.makeTemplateItemAnswerKey(etdl.templateItem1U.getId(), EvalConstants.ITEM_CATEGORY_INSTRUCTOR, EvalTestDataLoad.MAINT_USER_ID);
            Assert.assertEquals(aKey, e.missingItemAnswerKeys[0]);
        }
        Assert.assertNull(responseRequired.getId());
        Assert.assertNull(RA_3_3.getId());

        // test saving with course related instead of instructor related item (invalid item)
        EvalAnswer RA_1_1 = new EvalAnswer( responseRequired, etdl.templateItem1U, etdl.item1, null, null, 2 );
        responseRequired.getAnswers().add( RA_1_1 );
        try {
            deliveryService.saveResponse( responseRequired, EvalTestDataLoad.USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        responseRequired.getAnswers().remove( RA_1_1 );
        Assert.assertNull(responseRequired.getId());
        Assert.assertNull(RA_1_1.getId());
        Assert.assertNull(RA_3_3.getId());


        // test saving a response when answers are required (leaving out non-required answers)
        RA_1_1 = new EvalAnswer( responseRequired, etdl.templateItem1U, etdl.item1, 
                EvalTestDataLoad.MAINT_USER_ID, EvalConstants.ITEM_CATEGORY_INSTRUCTOR, 2 );
        responseRequired.getAnswers().add( RA_1_1 );
        responseRequired.setEndTime( new Date() );
        deliveryService.saveResponse( responseRequired, EvalTestDataLoad.USER_ID);
        Assert.assertNotNull(responseRequired.getId());
        Assert.assertNotNull(RA_1_1.getId());
        Assert.assertNotNull(RA_3_3.getId());


        // test saving a response without all compulsory items (TI_1_1 is compulsory)
        EvalResponse responseCompulsory = new EvalResponse( EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF, 
                evaluationActiveThree, new Date() );
        responseCompulsory.setEndTime( new Date() );
        responseCompulsory.setAnswers( new HashSet<>() );

        // first try with a blank one to make sure it Assert.fails
        try {
            deliveryService.saveResponse( responseCompulsory, EvalTestDataLoad.USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(ResponseSaveException.TYPE_BLANK_RESPONSE, e.type);
            Assert.assertEquals(1, e.missingItemAnswerKeys.length);
            String aKey = TemplateItemUtils.makeTemplateItemAnswerKey(etdl.templateItem1U.getId(), EvalConstants.ITEM_CATEGORY_INSTRUCTOR, EvalTestDataLoad.MAINT_USER_ID);
            Assert.assertEquals(aKey, e.missingItemAnswerKeys[0]);
        }
        Assert.assertNull(responseCompulsory.getId());

        // now try with one answer that is not compulsory (still Assert.fail)
        EvalAnswer CA_3_3 = new EvalAnswer( responseCompulsory, etdl.templateItem3U, etdl.item3, null, null, 2 );
        responseCompulsory.getAnswers().add( CA_3_3 );
        try {
            deliveryService.saveResponse( responseCompulsory, EvalTestDataLoad.USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(ResponseSaveException.TYPE_MISSING_REQUIRED_ANSWERS, e.type);
            Assert.assertEquals(1, e.missingItemAnswerKeys.length);
            String aKey = TemplateItemUtils.makeTemplateItemAnswerKey(etdl.templateItem1U.getId(), EvalConstants.ITEM_CATEGORY_INSTRUCTOR, EvalTestDataLoad.MAINT_USER_ID);
            Assert.assertEquals(aKey, e.missingItemAnswerKeys[0]);
        }
        Assert.assertNull(responseCompulsory.getId());
        Assert.assertNull(CA_3_3.getId());

        // now make sure we can save it if we add in the required stuff
        EvalAnswer CA_1_1 = new EvalAnswer( responseCompulsory, etdl.templateItem1U, etdl.item1, 
                EvalTestDataLoad.MAINT_USER_ID, EvalConstants.ITEM_CATEGORY_INSTRUCTOR, 2 );
        responseCompulsory.getAnswers().add( CA_1_1 );
        responseCompulsory.setEndTime( new Date() );
        deliveryService.saveResponse( responseCompulsory, EvalTestDataLoad.USER_ID);
        Assert.assertNotNull(responseCompulsory.getId());
        Assert.assertNotNull(CA_1_1.getId());
        Assert.assertNotNull(CA_3_3.getId());


        // TODO - cannot do this test until hibernate issue resolved
        //    // test that changing anything else on the existing response Assert.fails
        //    try {
        //    responseNone.setContext( EvalTestDataLoad.CONTEXT3 );
        //    responses.saveResponse( responseNone, EvalTestDataLoad.STUDENT_USER_ID);
        //    Assert.fail("Should have thrown exception");
        //    } catch (IllegalArgumentException e) {
        //    Assert.assertNotNull(e);
        //    Assert.fail(e.getMessage());
        //    }

        // test saving a response without proper ownership Assert.fails
        try {
            responseNone.setEndTime( new Date() );
            deliveryService.saveResponse( responseNone, EvalTestDataLoad.USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (SecurityException e) {
            Assert.assertNotNull(e);
        }

        // test saving a response when one exists Assert.fails
        try {
            deliveryService.saveResponse( new EvalResponse( EvalTestDataLoad.USER_ID, 
                    EvalTestDataLoad.SITE1_REF, etdl.evaluationActive, 
                    new Date()), 
                    EvalTestDataLoad.USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(ResponseSaveException.TYPE_CANNOT_TAKE_EVAL, e.type);
        }

        try {
            deliveryService.saveResponse( new EvalResponse( EvalTestDataLoad.MAINT_USER_ID, 
                    EvalTestDataLoad.SITE2_REF, etdl.evaluationActive, 
                    new Date()), 
                    EvalTestDataLoad.MAINT_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(ResponseSaveException.TYPE_CANNOT_TAKE_EVAL, e.type);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl#canModifyResponse(java.lang.String, java.lang.Long)}.
     */
    @Ignore
    @Test
    public void testCanModifyResponse() {

        // test owner can modify unlocked
        Assert.assertTrue( deliveryService.canModifyResponse(
                EvalTestDataLoad.USER_ID, etdl.response1.getId()) );

        // test admin cannot override permissions
        Assert.assertFalse( deliveryService.canModifyResponse(
                EvalTestDataLoad.ADMIN_USER_ID,	etdl.response1.getId()) );

        // test users without perms cannot modify
        Assert.assertFalse( deliveryService.canModifyResponse(
                EvalTestDataLoad.MAINT_USER_ID,	etdl.response1.getId()) );
        Assert.assertFalse( deliveryService.canModifyResponse(
                EvalTestDataLoad.STUDENT_USER_ID, etdl.response1.getId()) );

        // test no one can modify locked responses
        Assert.assertFalse( deliveryService.canModifyResponse(
                EvalTestDataLoad.ADMIN_USER_ID,	etdl.response3.getId()) );
        Assert.assertFalse( deliveryService.canModifyResponse(
                EvalTestDataLoad.STUDENT_USER_ID, etdl.response3.getId()) );

        // test invalid id causes Assert.failure
        try {
            deliveryService.canModifyResponse( EvalTestDataLoad.ADMIN_USER_ID, 
                    EvalTestDataLoad.INVALID_LONG_ID );
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

    }

    @Ignore
    @Test
    public void testGetEvaluationResponseForUserAndGroup() {
        EvalResponse response;

        // check retrieving an existing responses
        response = deliveryService.getEvaluationResponseForUserAndGroup(etdl.evaluationClosed.getId(), EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
        Assert.assertNotNull(response);
        Assert.assertEquals(etdl.response2.getId(), response.getId());

        // check creating a new response
        response = deliveryService.getEvaluationResponseForUserAndGroup(etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
        Assert.assertNotNull(response);

        // test cannot create response for closed evaluation
        try {
            deliveryService.getEvaluationResponseForUserAndGroup(etdl.evaluationClosed.getId(), EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF);
            Assert.fail("Should have thrown exception");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

        // test invalid permissions to create response Assert.fails
        try {
            deliveryService.getEvaluationResponseForUserAndGroup(etdl.evaluationActive.getId(), EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF);
            Assert.fail("Should have thrown exception");
        } catch (ResponseSaveException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals(ResponseSaveException.TYPE_CANNOT_TAKE_EVAL, e.type);
        }

    }

}
