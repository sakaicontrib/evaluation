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
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.constant.EvalEmailConstants;
import org.sakaiproject.evaluation.logic.externals.EvalSecurityChecksImpl;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;


/**
 * Tests for the EvalEvaluationServiceImpl
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalEvaluationServiceImplTest extends BaseTestEvalLogic {

    private static final Log LOG = LogFactory.getLog( EvalEvaluationServiceImplTest.class );
    protected EvalEvaluationServiceImpl evaluationService;
    protected EvalSettings settings;

    // run this before each test starts
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

        // setup the mock objects if needed

        // create and setup the object to be tested
        evaluationService = new EvalEvaluationServiceImpl();
        evaluationService.setDao(evaluationDao);
        evaluationService.setCommonLogic(commonLogic);
        evaluationService.setSecurityChecks(securityChecks);
        evaluationService.setSettings(settings);

    }


    /**
     * ADD unit tests below here, use testMethod as the name of the unit test,
     * Note that if a method is overloaded you should include the arguments in the
     * test name like so: testMethodClassInt (for method(Class, int);
     */


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getEvaluationById(java.lang.Long)}.
     */
    @Test
    public void testGetEvaluationById() {
        EvalEvaluation eval = evaluationService.getEvaluationById(etdl.evaluationActive.getId());
        Assert.assertNotNull(eval);
        Assert.assertNotNull(eval.getBlankResponsesAllowed());
        Assert.assertNotNull(eval.getModifyResponsesAllowed());
        Assert.assertNotNull(eval.getResultsSharing());
        Assert.assertNotNull(eval.getUnregisteredAllowed());
        Assert.assertEquals(etdl.evaluationActive.getId(), eval.getId());

        eval = evaluationService.getEvaluationById(etdl.evaluationNew.getId());
        Assert.assertNotNull(eval);
        Assert.assertEquals(etdl.evaluationNew.getId(), eval.getId());

        // test get eval by invalid id
        eval = evaluationService.getEvaluationById( EvalTestDataLoad.INVALID_LONG_ID );
        Assert.assertNull(eval);
    }

    @Test
    public void testCacheRetrievalOfEvals() {
        LOG.debug("CACHE: Testing lots of retrievals in a row");
        EvalEvaluation eval;

        eval = evaluationService.getEvaluationById(etdl.evaluationActive.getId());
        Assert.assertNotNull(eval);

        eval = evaluationService.getEvaluationById(EvalTestDataLoad.INVALID_LONG_ID);
        Assert.assertNull(eval);

        eval = evaluationService.getEvaluationById(etdl.evaluationActive.getId());
        Assert.assertNotNull(eval);

        eval = evaluationService.getEvaluationById(etdl.evaluationActive.getId());
        Assert.assertNotNull(eval);

        eval = evaluationService.getEvaluationById(EvalTestDataLoad.INVALID_LONG_ID);
        Assert.assertNull(eval);

        eval = evaluationService.getEvaluationById(etdl.evaluationActive.getId());
        Assert.assertNotNull(eval);

        eval = evaluationService.getEvaluationById(etdl.evaluationActive.getId());
        Assert.assertNotNull(eval);

        LOG.debug("CACHE: Should have been 3 showSQL log lines");
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#checkEvaluationExists(java.lang.Long)}.
     */
    @Test
    public void testCheckEvaluationExists() {
        // positive
        Assert.assertTrue(evaluationService.checkEvaluationExists(etdl.evaluationActive.getId()));
        Assert.assertTrue(evaluationService.checkEvaluationExists(etdl.evaluationClosed.getId()));

        // negative
        Assert.assertFalse(evaluationService.checkEvaluationExists(EvalTestDataLoad.INVALID_LONG_ID));

        // exception
        try {
            evaluationService.checkEvaluationExists(null);
            Assert.fail("Should have thrown exception");
        } catch (NullPointerException e) {
            Assert.assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#countEvaluationsByTemplateId(java.lang.Long)}.
     */
    @Test
    public void testCountEvaluationsByTemplateId() {
        // test valid template ids
        int count = evaluationService.countEvaluationsByTemplateId( etdl.templatePublic.getId() );
        Assert.assertEquals(2, count);

        count = evaluationService.countEvaluationsByTemplateId( etdl.templateUser.getId() );
        Assert.assertEquals(3, count);

        // test no evaluationSetupService for a template
        count = evaluationService.countEvaluationsByTemplateId( etdl.templateUnused.getId() );
        Assert.assertEquals(0, count);

        // test invalid template id
        try {
            evaluationService.countEvaluationsByTemplateId( EvalTestDataLoad.INVALID_LONG_ID );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getEvaluationByEid(java.lang.String)}.
     */
    @Test
    public void testGetEvaluationByEid() {
        EvalEvaluation evaluation;

        // test getting evaluation having eid set
        evaluation = evaluationService.getEvaluationByEid( etdl.evaluationProvided.getEid() );
        Assert.assertNotNull(evaluation);
        Assert.assertEquals(etdl.evaluationProvided.getEid(), evaluation.getEid());

        //test getting evaluation having eid not set  returns null
        evaluation = evaluationService.getEvaluationByEid( etdl.evaluationActive.getEid() );
        Assert.assertNull(evaluation);

        // test getting evaluation by invalid eid returns null
        evaluation = evaluationService.getEvaluationByEid( EvalTestDataLoad.INVALID_STRING_EID );
        Assert.assertNull(evaluation);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getEvaluationsByTemplateId(java.lang.Long)}.
     */
    @Test
    public void testGetEvaluationsByTemplateId() {
        List<EvalEvaluation> l;
        List<Long> ids;

        // test valid template ids
        l = evaluationService.getEvaluationsByTemplateId( etdl.templatePublic.getId() );
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationNew.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

        l = evaluationService.getEvaluationsByTemplateId( etdl.templateUser.getId() );
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationProvided.getId() ));

        // test no evaluationSetupService for a template
        l = evaluationService.getEvaluationsByTemplateId( etdl.templateUnused.getId() );
        Assert.assertNotNull(l);
        Assert.assertTrue(l.isEmpty());

        // test invalid template id
        try {
            evaluationService.getEvaluationsByTemplateId( EvalTestDataLoad.INVALID_LONG_ID );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#updateEvaluationState(java.lang.Long)}.
     */
    @Test
    public void testUpdateEvaluationStateLong() {
        Assert.assertEquals(EvalConstants.EVALUATION_STATE_INQUEUE, evaluationService.updateEvaluationState( etdl.evaluationNew.getId() ) );
        Assert.assertEquals(EvalConstants.EVALUATION_STATE_ACTIVE, evaluationService.updateEvaluationState( etdl.evaluationActive.getId() ) );
        Assert.assertEquals(EvalConstants.EVALUATION_STATE_CLOSED, evaluationService.updateEvaluationState( etdl.evaluationClosed.getId() ) );
        Assert.assertEquals(EvalConstants.EVALUATION_STATE_VIEWABLE, evaluationService.updateEvaluationState( etdl.evaluationViewable.getId() ) );

        try {
            evaluationService.updateEvaluationState( EvalTestDataLoad.INVALID_LONG_ID );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // TODO - add tests for changing state when checked
    }    

    @SuppressWarnings("deprecation")
    @Test
    public void testGetUserIdsTakingEvalInGroup() {
        Set<String> userIds;

        // get all users
        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationClosed.getId(), 
                EvalTestDataLoad.SITE2_REF, EvalConstants.EVAL_INCLUDE_ALL);
        Assert.assertNotNull(userIds);
        Assert.assertEquals(2, userIds.size());
        Assert.assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));
        Assert.assertTrue(userIds.contains(EvalTestDataLoad.STUDENT_USER_ID));

        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationClosed.getId(), 
                EvalTestDataLoad.SITE1_REF, EvalConstants.EVAL_INCLUDE_ALL);
        Assert.assertNotNull(userIds);
        Assert.assertEquals(1, userIds.size());
        Assert.assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));

        // get users who have taken
        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationClosed.getId(), 
                EvalTestDataLoad.SITE2_REF, EvalConstants.EVAL_INCLUDE_RESPONDENTS);
        Assert.assertNotNull(userIds);
        Assert.assertEquals(2, userIds.size());
        Assert.assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));
        Assert.assertTrue(userIds.contains(EvalTestDataLoad.STUDENT_USER_ID));

        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationClosed.getId(), 
                EvalTestDataLoad.SITE1_REF, EvalConstants.EVAL_INCLUDE_RESPONDENTS);
        Assert.assertNotNull(userIds);
        Assert.assertEquals(1, userIds.size());
        Assert.assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));

        // get non takers
        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationClosed.getId(), 
                EvalTestDataLoad.SITE2_REF, EvalConstants.EVAL_INCLUDE_NONTAKERS);
        Assert.assertNotNull(userIds);
        Assert.assertEquals(0, userIds.size());

        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationClosed.getId(), 
                EvalTestDataLoad.SITE1_REF, EvalConstants.EVAL_INCLUDE_NONTAKERS);
        Assert.assertNotNull(userIds);
        Assert.assertEquals(0, userIds.size());

        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationNewAdmin.getId(), 
                EvalTestDataLoad.SITE2_REF, EvalConstants.EVAL_INCLUDE_NONTAKERS);
        Assert.assertNotNull(userIds);
        Assert.assertEquals(2, userIds.size());
        Assert.assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));
        Assert.assertTrue(userIds.contains(EvalTestDataLoad.STUDENT_USER_ID));

        // invalid constant causes Assert.failure
        try {
            evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationNewAdmin.getId(), 
                    EvalTestDataLoad.SITE2_REF, EvalTestDataLoad.INVALID_CONSTANT_STRING);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

    }

    @Test
    public void testGetParticipants() {
        List<EvalAssignUser> l;

        // do the typical use cases first
        
        // get all participants for an evaluation
        l = evaluationService.getParticipantsForEval(etdl.evaluationActive.getId(), null, null, 
                null, null, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());

        // get everyone who can take an evaluation
        l = evaluationService.getParticipantsForEval(etdl.evaluationActive.getId(), null, null, 
                EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
      
        // get all the evals a user is assigned to
        l = evaluationService.getParticipantsForEval(null, EvalTestDataLoad.USER_ID, null, 
                EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(11, l.size());

        // get all active evals a user is assigned to
        l = evaluationService.getParticipantsForEval(null, EvalTestDataLoad.USER_ID, null, 
                EvalAssignUser.TYPE_EVALUATOR, null, null, EvalConstants.EVALUATION_STATE_ACTIVE);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());

        // now just do a couple of proofs of concept
        l = evaluationService.getParticipantsForEval(etdl.evaluationClosed.getId(), null, null, null, null, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(5, l.size());

        l = evaluationService.getParticipantsForEval(etdl.evaluationClosed.getId(), null, new String[] {EvalTestDataLoad.SITE2_REF}, null, null, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());

        l = evaluationService.getParticipantsForEval(null, EvalTestDataLoad.STUDENT_USER_ID, null, null, null, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());

        try {
            evaluationService.getParticipantsForEval(null, null, null, null, null, null, null);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testCanBeginEvaluation() {
        Assert.assertTrue( evaluationService.canBeginEvaluation(EvalTestDataLoad.ADMIN_USER_ID) );
        Assert.assertTrue( evaluationService.canBeginEvaluation(EvalTestDataLoad.MAINT_USER_ID) );
        Assert.assertFalse( evaluationService.canBeginEvaluation(EvalTestDataLoad.USER_ID) );
        Assert.assertFalse( evaluationService.canBeginEvaluation(EvalTestDataLoad.INVALID_USER_ID) );
    }

    @Test
    public void testCanTakeEvaluation() {
        // test able to take untaken eval
        Assert.assertTrue( evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE1_REF) );
        // test able to take eval in evalGroupId not taken in yet
        Assert.assertTrue( evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE1_REF) );
        // test admin can always take
        Assert.assertTrue( evaluationService.canTakeEvaluation( EvalTestDataLoad.ADMIN_USER_ID, 
                etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF) );
        // anonymous can always be taken
        Assert.assertTrue( evaluationService.canTakeEvaluation( EvalTestDataLoad.MAINT_USER_ID, 
                etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE1_REF) );

        // test not able to take
        // not assigned to this group
        Assert.assertFalse( evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE2_REF) );
        // already taken
        Assert.assertFalse( evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF) );
        // not assigned to this evalGroupId
        Assert.assertFalse( evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActive.getId(), EvalTestDataLoad.SITE2_REF) );
        // cannot take evaluation (no perm)
        Assert.assertFalse( evaluationService.canTakeEvaluation( EvalTestDataLoad.MAINT_USER_ID, 
                etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF) );

        // test invalid information
        Assert.assertFalse( evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.INVALID_CONTEXT) );
        Assert.assertFalse( evaluationService.canTakeEvaluation( EvalTestDataLoad.INVALID_USER_ID, 
                etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF) );

        try {
            evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                    EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.SITE1_REF);
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

    }

    @Test
    public void testCanControlEvaluation() {
        // test can control
        Assert.assertTrue( evaluationService.canControlEvaluation(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId() ) );

        // test can control (admin user id)
        Assert.assertTrue( evaluationService.canControlEvaluation(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId() ) );

        // test cannot control (non owner)
        Assert.assertFalse( evaluationService.canControlEvaluation(
                EvalTestDataLoad.USER_ID, etdl.evaluationNew.getId() ) );

        // test can control (active)
        Assert.assertTrue( evaluationService.canControlEvaluation(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationActive.getId() ) );

        // test can control (closed and viewable)
        Assert.assertTrue( evaluationService.canControlEvaluation(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationViewable.getId() ) );
    }

    @Test
    public void testCanRemoveEvaluation() {
        // test can remove
        Assert.assertTrue( evaluationService.canRemoveEvaluation(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId() ) );

        // test can remove (admin user id)
        Assert.assertTrue( evaluationService.canRemoveEvaluation(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId() ) );

        // test cannot remove (non owner)
        Assert.assertFalse( evaluationService.canRemoveEvaluation(
                EvalTestDataLoad.USER_ID, etdl.evaluationNew.getId() ) );

        // test cannot remove (active)
        Assert.assertFalse( evaluationService.canRemoveEvaluation(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationActive.getId() ) );

        // test can remove (closed and viewable)
        Assert.assertTrue( evaluationService.canRemoveEvaluation(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationViewable.getId() ) );
    }

    @Test
    public void testCountParticipantsForEval() {
        int count;

        // check active returns all enrollments count
        count = evaluationService.countParticipantsForEval(etdl.evaluationClosed.getId(), null);
        Assert.assertEquals(3, count);

        count = evaluationService.countParticipantsForEval(etdl.evaluationActive.getId(), null);
        Assert.assertEquals(1, count);

        // check anon returns 0
        count = evaluationService.countParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null);
        Assert.assertEquals(0, count);
    }


    // EVAL AND GROUP ASSIGNS

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#countEvaluationGroups(java.lang.Long, boolean)}.
     */
    @Test
    public void testCountEvaluationGroups() {
        int count = evaluationService.countEvaluationGroups( etdl.evaluationClosed.getId(), false );
        Assert.assertEquals(2, count);

        count = evaluationService.countEvaluationGroups( etdl.evaluationActive.getId(), false );
        Assert.assertEquals(1, count);

        // test no assigned contexts
        count = evaluationService.countEvaluationGroups( etdl.evaluationNew.getId(), false );
        Assert.assertEquals(0, count);

        // test invalid
        count = evaluationService.countEvaluationGroups( EvalTestDataLoad.INVALID_LONG_ID, false );
        Assert.assertEquals(0, count);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getAssignGroupById(java.lang.Long)}.
     */
    @Test
    public void testGetAssignGroupById() {
        EvalAssignGroup assignGroup;

        // test getting valid items by id
        assignGroup = evaluationService.getAssignGroupById( etdl.assign1.getId() );
        Assert.assertNotNull(assignGroup);
        Assert.assertEquals(etdl.assign1.getId(), assignGroup.getId());

        assignGroup = evaluationService.getAssignGroupById( etdl.assign2.getId() );
        Assert.assertNotNull(assignGroup);
        Assert.assertEquals(etdl.assign2.getId(), assignGroup.getId());

        // test get eval by invalid id returns null
        assignGroup = evaluationService.getAssignGroupById( EvalTestDataLoad.INVALID_LONG_ID );
        Assert.assertNull(assignGroup);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getAssignGroupByEid(java.lang.String)}.
     */
    @Test
    public void testGetAssignGroupByEid() {
        EvalAssignGroup assignGroupProvided;

        // test getting assignGroup having eid set
        assignGroupProvided = evaluationService.getAssignGroupByEid( etdl.assignGroupProvided.getEid() );
        Assert.assertNotNull(assignGroupProvided);
        Assert.assertEquals(etdl.assignGroupProvided.getEid(), assignGroupProvided.getEid());

        //test getting assignGroup having eid not set  returns null
        assignGroupProvided = evaluationService.getAssignGroupByEid( etdl.assign7.getEid() );
        Assert.assertNull(assignGroupProvided);

        // test getting assignGroup by invalid eid returns null
        assignGroupProvided = evaluationService.getAssignGroupByEid( EvalTestDataLoad.INVALID_STRING_EID );
        Assert.assertNull(assignGroupProvided);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getAssignGroupByEvalAndGroupId(java.lang.Long, java.lang.String)}.
     */
    @Test
    public void testGetAssignGroupId() {
        Long assignGroupId;
        EvalAssignGroup eag;

        // test getting valid items by id
        assignGroupId = evaluationService.getAssignGroupByEvalAndGroupId( etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF ).getId();
        Assert.assertNotNull(assignGroupId);
        Assert.assertEquals(etdl.assign1.getId(), assignGroupId);

        assignGroupId = evaluationService.getAssignGroupByEvalAndGroupId( etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE2_REF ).getId();
        Assert.assertNotNull(assignGroupId);
        Assert.assertEquals(etdl.assign4.getId(), assignGroupId);

        // test invalid evaluation/group mixture returns null
        eag = evaluationService.getAssignGroupByEvalAndGroupId( etdl.evaluationActive.getId(), EvalTestDataLoad.SITE2_REF );
        Assert.assertNull("Found an id?: " + eag, eag);

        eag = evaluationService.getAssignGroupByEvalAndGroupId( etdl.evaluationViewable.getId(), EvalTestDataLoad.SITE1_REF );
        Assert.assertNull(eag);

        // test get by invalid id returns null
        eag = evaluationService.getAssignGroupByEvalAndGroupId( EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.INVALID_CONSTANT_STRING );
        Assert.assertNull(eag);
    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getAssignHierarchyById(java.lang.Long)}.
     */
    @Test
    public void testGetAssignHierarchyById() {
        EvalAssignHierarchy eah;

        eah = evaluationService.getAssignHierarchyById(etdl.assignHier1.getId());
        Assert.assertNotNull(eah);
        Assert.assertEquals(etdl.assignHier1.getId(), eah.getId());

        eah = evaluationService.getAssignHierarchyById(EvalTestDataLoad.INVALID_LONG_ID);
        Assert.assertNull(eah);

        try {
            evaluationService.getAssignHierarchyById(null);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getAssignHierarchyByEval(java.lang.Long)}.
     */
    @Test
    public void testGetAssignHierarchyByEval() {
        List<EvalAssignHierarchy> eahs;

        eahs = evaluationService.getAssignHierarchyByEval(etdl.evaluationActive.getId());
        Assert.assertNotNull(eahs);
        Assert.assertEquals(1, eahs.size());
        Assert.assertEquals(etdl.assignHier1.getId(), eahs.get(0).getId());

        eahs = evaluationService.getAssignHierarchyByEval(etdl.evaluationNew.getId());
        Assert.assertNotNull(eahs);
        Assert.assertEquals(0, eahs.size());

        try {
            evaluationService.getAssignHierarchyByEval(null);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getEvalGroupsForEval(java.lang.Long[], boolean, Boolean)}.
     */
    @Test
    public void testGetEvaluationGroups() {
        Map<Long, List<EvalGroup>> m = evaluationService.getEvalGroupsForEval( 
                new Long[] { etdl.evaluationClosed.getId() }, true, null );
        Assert.assertNotNull(m);
        List<EvalGroup> evalGroups = m.get( etdl.evaluationClosed.getId() );
        Assert.assertNotNull(evalGroups);
        Assert.assertEquals(2, evalGroups.size());
        Assert.assertTrue( evalGroups.get(0) instanceof EvalGroup );
        Assert.assertTrue( evalGroups.get(1) instanceof EvalGroup );

        m = evaluationService.getEvalGroupsForEval( 
                new Long[] { etdl.evaluationActive.getId() }, true, null );
        Assert.assertNotNull(m);
        evalGroups = m.get( etdl.evaluationActive.getId() );
        Assert.assertNotNull(evalGroups);
        Assert.assertEquals(1, evalGroups.size());
        Assert.assertTrue( evalGroups.get(0) instanceof EvalGroup );
        Assert.assertEquals( EvalTestDataLoad.SITE1_REF, ((EvalGroup) evalGroups.get(0)).evalGroupId );

        // test no assigned contexts
        m = evaluationService.getEvalGroupsForEval( 
                new Long[] { etdl.evaluationNew.getId() }, true, null );
        Assert.assertNotNull(m);
        evalGroups = m.get( etdl.evaluationNew.getId() );
        Assert.assertNotNull(evalGroups);
        Assert.assertEquals(0, evalGroups.size());

        // test invalid
        m = evaluationService.getEvalGroupsForEval( 
                new Long[] { EvalTestDataLoad.INVALID_LONG_ID }, true, null );
        Assert.assertNotNull(m);
        evalGroups = m.get( EvalTestDataLoad.INVALID_LONG_ID );
        Assert.assertNotNull(evalGroups);
        Assert.assertEquals(0, evalGroups.size());
    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getAssignGroupsForEvals(java.lang.Long[], boolean, Boolean)}.
     */
    @Test
    public void testGetEvaluationAssignGroups() {
        // this is mostly tested above
        Map<Long, List<EvalAssignGroup>> m = evaluationService.getAssignGroupsForEvals( 
                new Long[] { etdl.evaluationClosed.getId() }, true, null );
        Assert.assertNotNull(m);
        List<EvalAssignGroup> eags = m.get( etdl.evaluationClosed.getId() );
        Assert.assertNotNull(eags);
        Assert.assertEquals(2, eags.size());
        Assert.assertTrue( eags.get(0) instanceof EvalAssignGroup );
        Assert.assertTrue( eags.get(1) instanceof EvalAssignGroup );
        Assert.assertEquals(etdl.assign3.getId(), eags.get(0).getId());
        Assert.assertEquals(etdl.assign4.getId(), eags.get(1).getId());
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#canCreateAssignEval(java.lang.String, java.lang.Long)}.
     */
    @Test
    public void testCanCreateAssignEval() {
        // test can create an AC in new
        Assert.assertTrue( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.evaluationNew.getId()) );
        Assert.assertTrue( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.evaluationNew.getId()) );
        Assert.assertTrue( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.evaluationNewAdmin.getId()) );
        Assert.assertTrue( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.evaluationActive.getId()) );
        Assert.assertTrue( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.evaluationActive.getId()) );

        // test cannot create AC in closed evals
        Assert.assertFalse( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.evaluationClosed.getId()) );
        Assert.assertFalse( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.evaluationViewable.getId()) );

        // test cannot create AC without perms
        Assert.assertFalse( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.USER_ID, etdl.evaluationNew.getId()) );
        Assert.assertFalse( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.evaluationNewAdmin.getId()) );

        // test invalid evaluation id
        try {
            evaluationService.canCreateAssignEval(
                    EvalTestDataLoad.MAINT_USER_ID,  EvalTestDataLoad.INVALID_LONG_ID );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#canDeleteAssignGroup(java.lang.String, java.lang.Long)}.
     */
    @Test
    public void testCanDeleteAssignGroup() {
        // test can remove an AC in new eval
        Assert.assertTrue( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.assign8.getId()) );
        Assert.assertTrue( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.assign8.getId()) );
        Assert.assertTrue( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.assign7.getId()) );

        // test cannot remove AC from running evals
        Assert.assertFalse( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID, etdl.assign1.getId()) );
        Assert.assertFalse( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID, etdl.assign4.getId()) );
        Assert.assertFalse( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.assign3.getId()) );
        Assert.assertFalse( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.assign5.getId()) );

        // test cannot remove without permission
        Assert.assertFalse( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.USER_ID, etdl.assign6.getId()) );
        Assert.assertFalse( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID, etdl.assign7.getId()) );

        // test invalid evaluation id
        try {
            evaluationService.canDeleteAssignGroup(
                    EvalTestDataLoad.MAINT_USER_ID,  EvalTestDataLoad.INVALID_LONG_ID );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }
    }



    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getResponseById(java.lang.Long)}.
     */
    @Test
    public void testGetResponseById() {
        EvalResponse response;

        response = evaluationService.getResponseById( etdl.response1.getId() );
        Assert.assertNotNull(response);
        Assert.assertEquals(etdl.response1.getId(), response.getId());

        response = evaluationService.getResponseById( etdl.response2.getId() );
        Assert.assertNotNull(response);
        Assert.assertEquals(etdl.response2.getId(), response.getId());

        // test get eval by invalid id
        response = evaluationService.getResponseById( EvalTestDataLoad.INVALID_LONG_ID );
        Assert.assertNull(response);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getResponseForUserAndGroup(java.lang.Long, java.lang.String, java.lang.String)}.
     */
    @Test
    public void testGetEvaluationResponseForUserAndGroup() {
        EvalResponse response;

        // check retrieving an existing responses
        response = evaluationService.getResponseForUserAndGroup(etdl.evaluationClosed.getId(), EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
        Assert.assertNotNull(response);
        Assert.assertEquals(etdl.response2.getId(), response.getId());

        // check creating a new response
        response = evaluationService.getResponseForUserAndGroup(etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
        Assert.assertNull(response);

        // test invalid params Assert.fails
        try {
            evaluationService.getResponseForUserAndGroup(EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetEvalResponseIds() {
        List<Long> l;

        // retrieve all response Ids for an evaluation
        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertTrue(l.contains( etdl.response2.getId() ));
        Assert.assertTrue(l.contains( etdl.response3.getId() ));
        Assert.assertTrue(l.contains( etdl.response6.getId() ));

        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), new String[] {}, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertTrue(l.contains( etdl.response2.getId() ));
        Assert.assertTrue(l.contains( etdl.response3.getId() ));
        Assert.assertTrue(l.contains( etdl.response6.getId() ));

        // retrieve all response Ids for an evaluation using all groups
        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), 
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertTrue(l.contains( etdl.response2.getId() ));
        Assert.assertTrue(l.contains( etdl.response3.getId() ));
        Assert.assertTrue(l.contains( etdl.response6.getId() ));

        // test retrieval of all responses for an evaluation
        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), 
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());

        // test retrieval of incomplete responses for an evaluation
        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), 
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // retrieve all response Ids for an evaluation in one group only
        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(l.contains( etdl.response2.getId() ));

        // retrieve all response Ids for an evaluation in one group only
        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(l.contains( etdl.response3.getId() ));
        Assert.assertTrue(l.contains( etdl.response6.getId() ));

        l = evaluationService.getResponseIds(etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(l.contains( etdl.response1.getId() ));

        // try to get responses for an eval group that is not associated with this eval
        l = evaluationService.getResponseIds(etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // try to get responses for an eval with no responses
        l = evaluationService.getResponseIds(etdl.evaluationActiveUntaken.getId(), null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // check that invalid eval ids cause Assert.failure
        try {
            evaluationService.getResponseIds( EvalTestDataLoad.INVALID_LONG_ID, null, true);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getResponses(java.lang.String, java.lang.Long[], java.lang.String, java.lang.Boolean)}.
     */
    @Test
    public void testGetEvaluationResponses() {
        List<EvalResponse> l;
        List<Long> ids;

        // retrieve response objects for all fields known
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, new String[] {EvalTestDataLoad.SITE1_REF}, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response2.getId() ));

        // retrieve all responses for a user in an evaluation
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, null, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response2.getId() ));
        Assert.assertTrue(ids.contains( etdl.response6.getId() ));

        // retrieve one response for a normal user in one eval
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId() }, null, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response1.getId() ));

        l = evaluationService.getResponses(EvalTestDataLoad.STUDENT_USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, null, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response3.getId() ));

        // check that empty array is ok for eval group ids
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId() }, new String[] {}, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response1.getId() ));

        // retrieve all responses for a normal user in one eval
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, null, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response2.getId() ));
        Assert.assertTrue(ids.contains( etdl.response6.getId() ));

        // limit retrieval by eval groups ids
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, new String[] {EvalTestDataLoad.SITE1_REF}, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response2.getId() ));

        // retrieve all responses for a normal user in mutliple evals
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, null, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(4, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response1.getId() ));
        Assert.assertTrue(ids.contains( etdl.response2.getId() ));
        Assert.assertTrue(ids.contains( etdl.response4.getId() ));
        Assert.assertTrue(ids.contains( etdl.response6.getId() ));

        // attempt to retrieve all responses
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, null, null );
        Assert.assertNotNull(l);
        Assert.assertEquals(4, l.size());

        // attempt to retrieve all incomplete responses (there are none)
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, null, false );
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // attempt to retrieve results for user that has no responses
        l = evaluationService.getResponses(EvalTestDataLoad.STUDENT_USER_ID, 
                new Long[] { etdl.evaluationActive.getId() }, null, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        l = evaluationService.getResponses(EvalTestDataLoad.MAINT_USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, null, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // test that admin can fetch all results for evaluationSetupService
        l = evaluationService.getResponses(EvalTestDataLoad.ADMIN_USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, null, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(6, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response1.getId() ));
        Assert.assertTrue(ids.contains( etdl.response2.getId() ));
        Assert.assertTrue(ids.contains( etdl.response3.getId() ));
        Assert.assertTrue(ids.contains( etdl.response4.getId() ));
        Assert.assertTrue(ids.contains( etdl.response5.getId() ));
        Assert.assertTrue(ids.contains( etdl.response6.getId() ));

        l = evaluationService.getResponses(EvalTestDataLoad.ADMIN_USER_ID, 
                new Long[] { etdl.evaluationViewable.getId() }, null, true );
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.response4.getId() ));
        Assert.assertTrue(ids.contains( etdl.response5.getId() ));

        // check that empty array causes Assert.failure
        try {
            evaluationService.getResponses(EvalTestDataLoad.ADMIN_USER_ID, 
                    new Long[] {}, null, true );
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

        // check that null evalids cause Assert.failure
        try {
            evaluationService.getResponses(EvalTestDataLoad.ADMIN_USER_ID, 
                    null, null, true );
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#countResponses(java.lang.String, java.lang.Long[], java.lang.String, java.lang.Boolean)}.
     */
    @Test
    public void testCountEvaluationResponses() {
        // test counts for all responses in various evaluationSetupService
        Assert.assertEquals(3, evaluationService.countResponses(null, new Long[] { etdl.evaluationClosed.getId() }, null, null) );
        Assert.assertEquals(2, evaluationService.countResponses(null, new Long[] { etdl.evaluationViewable.getId() }, null, null) );
        Assert.assertEquals(1, evaluationService.countResponses(null, new Long[] { etdl.evaluationActive.getId() }, null, null) );
        Assert.assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActiveUntaken.getId() }, null, null) );

        // limit by user
        Assert.assertEquals(3, evaluationService.countResponses(EvalTestDataLoad.ADMIN_USER_ID, new Long[] { etdl.evaluationClosed.getId() }, null, null) );
        Assert.assertEquals(2, evaluationService.countResponses(EvalTestDataLoad.USER_ID, new Long[] { etdl.evaluationClosed.getId() }, null, null) );
        Assert.assertEquals(1, evaluationService.countResponses(EvalTestDataLoad.STUDENT_USER_ID, new Long[] { etdl.evaluationClosed.getId() }, null, null) );
        Assert.assertEquals(0, evaluationService.countResponses(EvalTestDataLoad.MAINT_USER_ID, new Long[] { etdl.evaluationClosed.getId() }, null, null) );

        // test counts limited by evalGroupId
        Assert.assertEquals(1, evaluationService.countResponses(null, new Long[] { etdl.evaluationClosed.getId() }, 
                new String[] { EvalTestDataLoad.SITE1_REF }, null) );
        Assert.assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationViewable.getId() }, 
                new String[] { EvalTestDataLoad.SITE1_REF }, null) );
        Assert.assertEquals(1, evaluationService.countResponses(null, new Long[] { etdl.evaluationActive.getId() }, 
                new String[] { EvalTestDataLoad.SITE1_REF }, null) );
        Assert.assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActiveUntaken.getId() }, 
                new String[] { EvalTestDataLoad.SITE1_REF }, null) );

        Assert.assertEquals(2, evaluationService.countResponses(null, new Long[] { etdl.evaluationClosed.getId() }, 
                new String[] { EvalTestDataLoad.SITE2_REF }, null) );
        Assert.assertEquals(2, evaluationService.countResponses(null, new Long[] { etdl.evaluationViewable.getId() }, 
                new String[] { EvalTestDataLoad.SITE2_REF }, null) );
        Assert.assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActive.getId() }, 
                new String[] { EvalTestDataLoad.SITE2_REF }, null) );
        Assert.assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActiveUntaken.getId() }, 
                new String[] { EvalTestDataLoad.SITE2_REF }, null) );

        // test counts limited by completed
        Assert.assertEquals(3, evaluationService.countResponses(null, new Long[] { etdl.evaluationClosed.getId() }, null, true) );
        Assert.assertEquals(2, evaluationService.countResponses(null, new Long[] { etdl.evaluationViewable.getId() }, null, true) );
        Assert.assertEquals(1, evaluationService.countResponses(null, new Long[] { etdl.evaluationActive.getId() }, null, true) );
        Assert.assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActiveUntaken.getId() }, null, true) );

        // test counts limited by incomplete
        Assert.assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationClosed.getId() }, null, false) );
        Assert.assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationViewable.getId() }, null, false) );
        Assert.assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActive.getId() }, null, false) );
        Assert.assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActiveUntaken.getId() }, null, false) );

        // check that empty array causes Assert.failure
        try {
            evaluationService.countResponses(null, new Long[] {}, null, true);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

        // check that null evalids cause Assert.failure
        try {
            evaluationService.countResponses(null, null, null, true);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#canModifyResponse(java.lang.String, java.lang.Long)}.
     */
    @Test
    public void testCanModifyResponse() {
        // test owner can modify unlocked
        Assert.assertTrue( evaluationService.canModifyResponse(
                EvalTestDataLoad.USER_ID, etdl.response1.getId()) );

        // test admin cannot override permissions
        Assert.assertFalse( evaluationService.canModifyResponse(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.response1.getId()) );

        // test users without perms cannot modify
        Assert.assertFalse( evaluationService.canModifyResponse(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.response1.getId()) );
        Assert.assertFalse( evaluationService.canModifyResponse(
                EvalTestDataLoad.STUDENT_USER_ID, etdl.response1.getId()) );

        // test no one can modify locked responses
        Assert.assertFalse( evaluationService.canModifyResponse(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.response3.getId()) );
        Assert.assertFalse( evaluationService.canModifyResponse(
                EvalTestDataLoad.STUDENT_USER_ID, etdl.response3.getId()) );

        // test invalid id causes Assert.failure
        try {
            evaluationService.canModifyResponse( EvalTestDataLoad.ADMIN_USER_ID, 
                    EvalTestDataLoad.INVALID_LONG_ID );
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }

    // EMAIL TEMPLATES

    @Test
    public void testGetEmailTemplatesForUser() {
        List<EvalEmailTemplate> l;

        // get all templates
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, null, null);
        Assert.assertNotNull(l);
        // EVALSYS-1179 added submitted-confirmation email, increasing total count to 14
	// Additional submitted-confirmation email for evaluation, increasing total count to 15
        // EVALSYS-1456 added evalautees email
        // EVALSYS-1403 added evaluation specific submission confirmation
        Assert.assertEquals(16, l.size());

        // get only default templates
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, null, true);
        Assert.assertNotNull(l);
        // EVALSYS-1179 added submitted-confirmation email, increasing default count to 9
        // EVALSYS-1456 added evalautees email
        Assert.assertEquals(10, l.size());
        for (EvalEmailTemplate emailTemplate : l) {
            Assert.assertNotNull(emailTemplate.getDefaultType());
        }

        // get only non-default templates
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, null, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(6, l.size());
        for (EvalEmailTemplate emailTemplate : l) {
            Assert.assertNull(emailTemplate.getDefaultType());
        }

        // get specific type of template only
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.EMAIL_TEMPLATE_REMINDER, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        for (EvalEmailTemplate emailTemplate : l) {
            Assert.assertEquals(EvalConstants.EMAIL_TEMPLATE_REMINDER, emailTemplate.getType());
        }

        // should only get the default
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.EMAIL_TEMPLATE_REMINDER, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertNotNull(l.get(0).getDefaultType());
        Assert.assertEquals(EvalConstants.EMAIL_TEMPLATE_REMINDER, l.get(0).getType());

        // should only get the non defaults
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.EMAIL_TEMPLATE_REMINDER, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());

        // EVALSYS-1179 added test cases for retrieving default submitted-confirmation email template (should be one)
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.EMAIL_TEMPLATE_SUBMITTED, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertNotNull(l.get(0).getDefaultType());
        Assert.assertEquals(EvalConstants.EMAIL_TEMPLATE_SUBMITTED, l.get(0).getType());

        // EVALSYS-1179 added test cases for retrieving non-default submitted-confirmation email template (should be 1)
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.EMAIL_TEMPLATE_SUBMITTED, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());

        // TODO check permissions for non-admin

    }

    @Test
    public void testGetEmailTemplateById() {
        EvalEmailTemplate emailTemplate = evaluationService.getEmailTemplate(etdl.emailTemplate1.getId());
        Assert.assertNotNull(emailTemplate);
        Assert.assertEquals(etdl.emailTemplate1.getId(), emailTemplate.getId());
    }

    @Test
    public void testGetDefaultEmailTemplate() {
        EvalEmailTemplate emailTemplate;

        // test getting the templates
        emailTemplate = evaluationService.getDefaultEmailTemplate( 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE );
        Assert.assertNotNull(emailTemplate);
        Assert.assertEquals( EvalConstants.EMAIL_TEMPLATE_AVAILABLE, 
                emailTemplate.getDefaultType() );
        Assert.assertEquals( EvalEmailConstants.EMAIL_AVAILABLE_DEFAULT_TEXT,
                emailTemplate.getMessage() );

        emailTemplate = evaluationService.getDefaultEmailTemplate( 
                EvalConstants.EMAIL_TEMPLATE_REMINDER );
        Assert.assertNotNull(emailTemplate);
        Assert.assertEquals( EvalConstants.EMAIL_TEMPLATE_REMINDER, 
                emailTemplate.getDefaultType() );
        Assert.assertEquals( EvalEmailConstants.EMAIL_REMINDER_DEFAULT_TEXT,
                emailTemplate.getMessage() );

        // test invalid constant causes Assert.failure
        try {
            evaluationService.getDefaultEmailTemplate( EvalTestDataLoad.INVALID_CONSTANT_STRING );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

    }

    @Test
    public void testGetEmailTemplate() {
        EvalEmailTemplate emailTemplate;

        // test getting the templates
        emailTemplate = evaluationService.getEmailTemplate(etdl.evaluationActive.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE );
        Assert.assertNotNull(emailTemplate);
        Assert.assertEquals( EvalEmailConstants.EMAIL_AVAILABLE_DEFAULT_TEXT,
                emailTemplate.getMessage() );

        emailTemplate = evaluationService.getEmailTemplate(etdl.evaluationActive.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER );
        Assert.assertNotNull(emailTemplate);
        Assert.assertEquals( "Email Template 3", emailTemplate.getMessage() );

        // test invalid constant causes Assert.failure
        try {
            evaluationService.getEmailTemplate( EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.INVALID_CONSTANT_STRING );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }
    }

    // test for new method EvalEvaluationService.getEmailTemplateByEid()
    // http://jira.sakaiproject.org/browse/EVALSYS-851
    @Test
    public void testGetEmailTemplateByEid() {
    	List<EvalEmailTemplate> templates = new ArrayList<>();
    	templates.add(etdl.emailTemplate1);
    	templates.add(etdl.emailTemplate2);
    	templates.add(etdl.emailTemplate3);
    	
    	for(EvalEmailTemplate template : templates) {
        	EvalEmailTemplate emailTemplate;
    		try {
    			emailTemplate = this.evaluationService.getEmailTemplateByEid(template.getEid());
    		} catch(Exception e) {
    			emailTemplate = null;
    			Assert.fail("Should not have thrown exception");
    		}
        	Assert.assertNotNull(emailTemplate);
        	Assert.assertEquals(template.getId(), emailTemplate.getId());
        	Assert.assertEquals(template.getEid(), emailTemplate.getEid());
    	}
    }
    
    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#canControlEmailTemplate(java.lang.String, java.lang.Long, int)}.
     */
    @Test
    public void testCanControlEmailTemplateStringLongInt() {
        // test valid email template control perms when none assigned
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNewAdmin.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNewAdmin.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );

        // user does not have perm for this eval
        Assert.assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNewAdmin.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
        Assert.assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.USER_ID, etdl.evaluationNewAdmin.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );

        // test when template has some assigned already
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );

        // test admin overrides perms
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );

        // test not has permission
        Assert.assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNewAdmin.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
        Assert.assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
        Assert.assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.USER_ID, etdl.evaluationNew.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );

        // test CAN when evaluation is running (active+)
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationActive.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationActive.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );

        // check admin CAN override for running evals
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationClosed.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationClosed.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );

        // check invalid evaluation id causes Assert.failure
        try {
            evaluationService.canControlEmailTemplate(
                    EvalTestDataLoad.ADMIN_USER_ID, 
                    EvalTestDataLoad.INVALID_LONG_ID, 
                    EvalConstants.EMAIL_TEMPLATE_REMINDER);
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#canControlEmailTemplate(java.lang.String, java.lang.Long, java.lang.Long)}.
     */
    @Test
    public void testCanControlEmailTemplateStringLongLong() {
        // test valid email template control perms when none assigned
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId(), 
                etdl.emailTemplate1.getId()) );
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId(), 
                etdl.emailTemplate2.getId()) );
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId(), 
                etdl.emailTemplate2.getId()) );

        // test with null eval id
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, null, etdl.emailTemplate2.getId()) );
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, null, etdl.emailTemplate2.getId()) );

        Assert.assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, null, etdl.emailTemplate1.getId()) );
        Assert.assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.USER_ID, null, etdl.emailTemplate2.getId()) );

        // test not has permissions
        Assert.assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId(), 
                etdl.emailTemplate1.getId()) );

        // test valid and active eval allowed
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationActive.getId(), 
                etdl.emailTemplate3.getId()) );

        // make sure admin CAN override for active eval
        Assert.assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationActive.getId(), 
                etdl.emailTemplate3.getId()) );

        // check invalid evaluation id causes Assert.failure
        try {
            evaluationService.canControlEmailTemplate(
                    EvalTestDataLoad.ADMIN_USER_ID, 
                    EvalTestDataLoad.INVALID_LONG_ID, 
                    etdl.emailTemplate1.getId() );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // check invalid email template id causes Assert.failure
        try {
            evaluationService.canControlEmailTemplate(
                    EvalTestDataLoad.ADMIN_USER_ID, 
                    etdl.evaluationNew.getId(), 
                    EvalTestDataLoad.INVALID_LONG_ID );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // check non-matching evaluation and template causes Assert.failure
        try {
            evaluationService.canControlEmailTemplate(
                    EvalTestDataLoad.ADMIN_USER_ID, 
                    etdl.evaluationNew.getId(), 
                    etdl.emailTemplate3.getId() );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

    }

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationService#countEvaluations(java.lang.String)}.
	 */
    @Test
	public void testCountEvaluations()
	{
		String searchString01 = "Eval";
		int count01 = this.evaluationService.countEvaluations(searchString01);
		Assert.assertEquals(11,count01);
		
		String searchString02 = "active";
		int count02 = this.evaluationService.countEvaluations(searchString02);
		Assert.assertEquals(2,count02);
		
		String searchString03 = "No evaluation found";
		int count03 = this.evaluationService.countEvaluations(searchString03);
		Assert.assertEquals(0,count03);
		
		
	}

	/**
	 * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationService#getEvaluations(java.lang.String, java.lang.String, int, int)}.
	 */
    @Test
	public void testGetEvaluations()
	{
		String searchString = "Eval";
		String order = "title";
		
		List<EvalEvaluation> list01 = this.evaluationService.getEvaluations(searchString, order, 0, 5);
		Assert.assertEquals(5, list01.size());
		
		List<EvalEvaluation> list02 = this.evaluationService.getEvaluations(searchString, order, 4, 5);
		Assert.assertEquals(5, list02.size());
		
		List<EvalEvaluation> list03 = this.evaluationService.getEvaluations(searchString, order, 8, 5);
		Assert.assertEquals(3, list03.size());
		
		List<EvalEvaluation> list04 = this.evaluationService.getEvaluations(searchString, order, 0, 25);
		Assert.assertEquals(11, list04.size());

		List<EvalEvaluation> list05 = this.evaluationService.getEvaluations(searchString, order, 4, 25);
		Assert.assertEquals(7, list05.size());
	}

}
