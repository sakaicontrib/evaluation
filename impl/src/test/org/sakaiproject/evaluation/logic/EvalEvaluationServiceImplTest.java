/**
 * $Id$
 * $URL$
 * EvalEvaluationServiceImplTest.java - evaluation - Jan 28, 2008 6:01:13 PM - azeckoski
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

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    protected EvalEvaluationServiceImpl evaluationService;
    protected EvalSettings settings;

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
    public void testGetEvaluationById() {
        EvalEvaluation eval = evaluationService.getEvaluationById(etdl.evaluationActive.getId());
        assertNotNull(eval);
        assertNotNull(eval.getBlankResponsesAllowed());
        assertNotNull(eval.getModifyResponsesAllowed());
        assertNotNull(eval.getResultsSharing());
        assertNotNull(eval.getUnregisteredAllowed());
        assertEquals(etdl.evaluationActive.getId(), eval.getId());

        eval = evaluationService.getEvaluationById(etdl.evaluationNew.getId());
        assertNotNull(eval);
        assertEquals(etdl.evaluationNew.getId(), eval.getId());

        // test get eval by invalid id
        eval = evaluationService.getEvaluationById( EvalTestDataLoad.INVALID_LONG_ID );
        assertNull(eval);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#checkEvaluationExists(java.lang.Long)}.
     */
    public void testCheckEvaluationExists() {
        // positive
        assertTrue(evaluationService.checkEvaluationExists(etdl.evaluationActive.getId()));
        assertTrue(evaluationService.checkEvaluationExists(etdl.evaluationClosed.getId()));

        // negative
        assertFalse(evaluationService.checkEvaluationExists(EvalTestDataLoad.INVALID_LONG_ID));

        // exception
        try {
            evaluationService.checkEvaluationExists(null);
            fail("Should have thrown exception");
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#countEvaluationsByTemplateId(java.lang.Long)}.
     */
    public void testCountEvaluationsByTemplateId() {
        // test valid template ids
        int count = evaluationService.countEvaluationsByTemplateId( etdl.templatePublic.getId() );
        assertEquals(2, count);

        count = evaluationService.countEvaluationsByTemplateId( etdl.templateUser.getId() );
        assertEquals(3, count);

        // test no evaluationSetupService for a template
        count = evaluationService.countEvaluationsByTemplateId( etdl.templateUnused.getId() );
        assertEquals(0, count);

        // test invalid template id
        try {
            count = evaluationService.countEvaluationsByTemplateId( EvalTestDataLoad.INVALID_LONG_ID );
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getEvaluationByEid(java.lang.String)}.
     */
    public void testGetEvaluationByEid() {
        EvalEvaluation evaluation = null;

        // test getting evaluation having eid set
        evaluation = evaluationService.getEvaluationByEid( etdl.evaluationProvided.getEid() );
        assertNotNull(evaluation);
        assertEquals(etdl.evaluationProvided.getEid(), evaluation.getEid());

        //test getting evaluation having eid not set  returns null
        evaluation = evaluationService.getEvaluationByEid( etdl.evaluationActive.getEid() );
        assertNull(evaluation);

        // test getting evaluation by invalid eid returns null
        evaluation = evaluationService.getEvaluationByEid( EvalTestDataLoad.INVALID_STRING_EID );
        assertNull(evaluation);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getEvaluationsByTemplateId(java.lang.Long)}.
     */
    public void testGetEvaluationsByTemplateId() {
        List<EvalEvaluation> l = null;
        List<Long> ids = null;

        // test valid template ids
        l = evaluationService.getEvaluationsByTemplateId( etdl.templatePublic.getId() );
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationNew.getId() ));
        assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

        l = evaluationService.getEvaluationsByTemplateId( etdl.templateUser.getId() );
        assertNotNull(l);
        assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        assertTrue(ids.contains( etdl.evaluationViewable.getId() ));
        assertTrue(ids.contains( etdl.evaluationProvided.getId() ));

        // test no evaluationSetupService for a template
        l = evaluationService.getEvaluationsByTemplateId( etdl.templateUnused.getId() );
        assertNotNull(l);
        assertTrue(l.isEmpty());

        // test invalid template id
        try {
            l = evaluationService.getEvaluationsByTemplateId( EvalTestDataLoad.INVALID_LONG_ID );
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#updateEvaluationState(java.lang.Long)}.
     */
    public void testUpdateEvaluationStateLong() {
        assertEquals( evaluationService.updateEvaluationState( etdl.evaluationNew.getId() ), EvalConstants.EVALUATION_STATE_INQUEUE );
        assertEquals( evaluationService.updateEvaluationState( etdl.evaluationActive.getId() ), EvalConstants.EVALUATION_STATE_ACTIVE );
        assertEquals( evaluationService.updateEvaluationState( etdl.evaluationClosed.getId() ), EvalConstants.EVALUATION_STATE_CLOSED );
        assertEquals( evaluationService.updateEvaluationState( etdl.evaluationViewable.getId() ), EvalConstants.EVALUATION_STATE_VIEWABLE );

        try {
            evaluationService.updateEvaluationState( EvalTestDataLoad.INVALID_LONG_ID );
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }

        // TODO - add tests for changing state when checked
    }

    @SuppressWarnings("deprecation")
    public void testGetUserIdsTakingEvalInGroup() {
        Set<String> userIds = null;

        // get all users
        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationClosed.getId(), 
                EvalTestDataLoad.SITE2_REF, EvalConstants.EVAL_INCLUDE_ALL);
        assertNotNull(userIds);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));
        assertTrue(userIds.contains(EvalTestDataLoad.STUDENT_USER_ID));

        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationClosed.getId(), 
                EvalTestDataLoad.SITE1_REF, EvalConstants.EVAL_INCLUDE_ALL);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));

        // get users who have taken
        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationClosed.getId(), 
                EvalTestDataLoad.SITE2_REF, EvalConstants.EVAL_INCLUDE_RESPONDENTS);
        assertNotNull(userIds);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));
        assertTrue(userIds.contains(EvalTestDataLoad.STUDENT_USER_ID));

        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationClosed.getId(), 
                EvalTestDataLoad.SITE1_REF, EvalConstants.EVAL_INCLUDE_RESPONDENTS);
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));

        // get non takers
        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationClosed.getId(), 
                EvalTestDataLoad.SITE2_REF, EvalConstants.EVAL_INCLUDE_NONTAKERS);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationClosed.getId(), 
                EvalTestDataLoad.SITE1_REF, EvalConstants.EVAL_INCLUDE_NONTAKERS);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        userIds = evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationNewAdmin.getId(), 
                EvalTestDataLoad.SITE2_REF, EvalConstants.EVAL_INCLUDE_NONTAKERS);
        assertNotNull(userIds);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));
        assertTrue(userIds.contains(EvalTestDataLoad.STUDENT_USER_ID));

        // invalid constant causes failure
        try {
            evaluationService.getUserIdsTakingEvalInGroup(etdl.evaluationNewAdmin.getId(), 
                    EvalTestDataLoad.SITE2_REF, EvalTestDataLoad.INVALID_CONSTANT_STRING);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

    }


    public void testGetParticipants() {
        List<EvalAssignUser> l = null;

        // do the typical use cases first
        
        // get all participants for an evaluation
        l = evaluationService.getParticipantsForEval(etdl.evaluationActive.getId(), null, null, 
                null, null, null, null);
        assertNotNull(l);
        assertEquals(2, l.size());

        // get everyone who can take an evaluation
        l = evaluationService.getParticipantsForEval(etdl.evaluationActive.getId(), null, null, 
                EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        assertNotNull(l);
        assertEquals(1, l.size());
      
        // get all the evals a user is assigned to
        l = evaluationService.getParticipantsForEval(null, EvalTestDataLoad.USER_ID, null, 
                EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        assertNotNull(l);
        assertEquals(10, l.size());

        // get all active evals a user is assigned to
        l = evaluationService.getParticipantsForEval(null, EvalTestDataLoad.USER_ID, null, 
                EvalAssignUser.TYPE_EVALUATOR, null, null, EvalConstants.EVALUATION_STATE_ACTIVE);
        assertNotNull(l);
        assertEquals(2, l.size());

        // now just do a couple of proofs of concept
        l = evaluationService.getParticipantsForEval(etdl.evaluationClosed.getId(), null, null, null, null, null, null);
        assertNotNull(l);
        assertEquals(5, l.size());

        l = evaluationService.getParticipantsForEval(etdl.evaluationClosed.getId(), null, new String[] {EvalTestDataLoad.SITE2_REF}, null, null, null, null);
        assertNotNull(l);
        assertEquals(3, l.size());

        l = evaluationService.getParticipantsForEval(null, EvalTestDataLoad.STUDENT_USER_ID, null, null, null, null, null);
        assertNotNull(l);
        assertEquals(3, l.size());

        try {
            l = evaluationService.getParticipantsForEval(null, null, null, null, null, null, null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    public void testCanBeginEvaluation() {
        assertTrue( evaluationService.canBeginEvaluation(EvalTestDataLoad.ADMIN_USER_ID) );
        assertTrue( evaluationService.canBeginEvaluation(EvalTestDataLoad.MAINT_USER_ID) );
        assertFalse( evaluationService.canBeginEvaluation(EvalTestDataLoad.USER_ID) );
        assertFalse( evaluationService.canBeginEvaluation(EvalTestDataLoad.INVALID_USER_ID) );
    }

    public void testCanTakeEvaluation() {
        // test able to take untaken eval
        assertTrue( evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE1_REF) );
        // test able to take eval in evalGroupId not taken in yet
        assertTrue( evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE1_REF) );
        // test admin can always take
        assertTrue( evaluationService.canTakeEvaluation( EvalTestDataLoad.ADMIN_USER_ID, 
                etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF) );
        // anonymous can always be taken
        assertTrue( evaluationService.canTakeEvaluation( EvalTestDataLoad.MAINT_USER_ID, 
                etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE1_REF) );

        // test not able to take
        // not assigned to this group
        assertFalse( evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.SITE2_REF) );
        // already taken
        assertFalse( evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF) );
        // not assigned to this evalGroupId
        assertFalse( evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActive.getId(), EvalTestDataLoad.SITE2_REF) );
        // cannot take evaluation (no perm)
        assertFalse( evaluationService.canTakeEvaluation( EvalTestDataLoad.MAINT_USER_ID, 
                etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF) );

        // test invalid information
        assertFalse( evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.INVALID_CONTEXT) );
        assertFalse( evaluationService.canTakeEvaluation( EvalTestDataLoad.INVALID_USER_ID, 
                etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF) );

        try {
            evaluationService.canTakeEvaluation( EvalTestDataLoad.USER_ID, 
                    EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.SITE1_REF);
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }

    }

    public void testCanControlEvaluation() {
        // test can control
        assertTrue( evaluationService.canControlEvaluation(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId() ) );

        // test can control (admin user id)
        assertTrue( evaluationService.canControlEvaluation(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId() ) );

        // test cannot control (non owner)
        assertFalse( evaluationService.canControlEvaluation(
                EvalTestDataLoad.USER_ID, etdl.evaluationNew.getId() ) );

        // test can control (active)
        assertTrue( evaluationService.canControlEvaluation(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationActive.getId() ) );

        // test can control (closed and viewable)
        assertTrue( evaluationService.canControlEvaluation(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationViewable.getId() ) );
    }

    public void testCanRemoveEvaluation() {
        // test can remove
        assertTrue( evaluationService.canRemoveEvaluation(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId() ) );

        // test can remove (admin user id)
        assertTrue( evaluationService.canRemoveEvaluation(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId() ) );

        // test cannot remove (non owner)
        assertFalse( evaluationService.canRemoveEvaluation(
                EvalTestDataLoad.USER_ID, etdl.evaluationNew.getId() ) );

        // test cannot remove (active)
        assertFalse( evaluationService.canRemoveEvaluation(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationActive.getId() ) );

        // test can remove (closed and viewable)
        assertTrue( evaluationService.canRemoveEvaluation(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationViewable.getId() ) );
    }

    public void testCountParticipantsForEval() {
        int count = 0;

        // check active returns all enrollments count
        count = evaluationService.countParticipantsForEval(etdl.evaluationClosed.getId(), null);
        assertEquals(3, count);

        count = evaluationService.countParticipantsForEval(etdl.evaluationActive.getId(), null);
        assertEquals(1, count);

        // check anon returns 0
        count = evaluationService.countParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null);
        assertEquals(0, count);
    }


    // EVAL AND GROUP ASSIGNS

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#countEvaluationGroups(java.lang.Long, boolean)}.
     */
    public void testCountEvaluationGroups() {
        int count = evaluationService.countEvaluationGroups( etdl.evaluationClosed.getId(), false );
        assertEquals(2, count);

        count = evaluationService.countEvaluationGroups( etdl.evaluationActive.getId(), false );
        assertEquals(1, count);

        // test no assigned contexts
        count = evaluationService.countEvaluationGroups( etdl.evaluationNew.getId(), false );
        assertEquals(0, count);

        // test invalid
        count = evaluationService.countEvaluationGroups( EvalTestDataLoad.INVALID_LONG_ID, false );
        assertEquals(0, count);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getAssignGroupById(java.lang.Long)}.
     */
    public void testGetAssignGroupById() {
        EvalAssignGroup assignGroup = null;

        // test getting valid items by id
        assignGroup = evaluationService.getAssignGroupById( etdl.assign1.getId() );
        assertNotNull(assignGroup);
        assertEquals(etdl.assign1.getId(), assignGroup.getId());

        assignGroup = evaluationService.getAssignGroupById( etdl.assign2.getId() );
        assertNotNull(assignGroup);
        assertEquals(etdl.assign2.getId(), assignGroup.getId());

        // test get eval by invalid id returns null
        assignGroup = evaluationService.getAssignGroupById( EvalTestDataLoad.INVALID_LONG_ID );
        assertNull(assignGroup);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getAssignGroupByEid(java.lang.String)}.
     */
    public void testGetAssignGroupByEid() {
        EvalAssignGroup assignGroupProvided = null;

        // test getting assignGroup having eid set
        assignGroupProvided = evaluationService.getAssignGroupByEid( etdl.assignGroupProvided.getEid() );
        assertNotNull(assignGroupProvided);
        assertEquals(etdl.assignGroupProvided.getEid(), assignGroupProvided.getEid());

        //test getting assignGroup having eid not set  returns null
        assignGroupProvided = evaluationService.getAssignGroupByEid( etdl.assign7.getEid() );
        assertNull(assignGroupProvided);

        // test getting assignGroup by invalid eid returns null
        assignGroupProvided = evaluationService.getAssignGroupByEid( EvalTestDataLoad.INVALID_STRING_EID );
        assertNull(assignGroupProvided);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getAssignGroupId(java.lang.Long, java.lang.String)}.
     */
    public void testGetAssignGroupId() {
        Long assignGroupId = null;

        // test getting valid items by id
        assignGroupId = evaluationService.getAssignGroupId( etdl.evaluationActive.getId(), EvalTestDataLoad.SITE1_REF );
        assertNotNull(assignGroupId);
        assertEquals(etdl.assign1.getId(), assignGroupId);

        assignGroupId = evaluationService.getAssignGroupId( etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE2_REF );
        assertNotNull(assignGroupId);
        assertEquals(etdl.assign4.getId(), assignGroupId);

        // test invalid evaluation/group mixture returns null
        assignGroupId = evaluationService.getAssignGroupId( etdl.evaluationActive.getId(), EvalTestDataLoad.SITE2_REF );
        assertNull("Found an id?: " + assignGroupId, assignGroupId);

        assignGroupId = evaluationService.getAssignGroupId( etdl.evaluationViewable.getId(), EvalTestDataLoad.SITE1_REF );
        assertNull(assignGroupId);

        // test get by invalid id returns null
        assignGroupId = evaluationService.getAssignGroupId( EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.INVALID_CONSTANT_STRING );
        assertNull(assignGroupId);
    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getAssignHierarchyById(java.lang.Long)}.
     */
    public void testGetAssignHierarchyById() {
        EvalAssignHierarchy eah = null;

        eah = evaluationService.getAssignHierarchyById(etdl.assignHier1.getId());
        assertNotNull(eah);
        assertEquals(etdl.assignHier1.getId(), eah.getId());

        eah = evaluationService.getAssignHierarchyById(EvalTestDataLoad.INVALID_LONG_ID);
        assertNull(eah);

        try {
            evaluationService.getAssignHierarchyById(null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getAssignHierarchyByEval(java.lang.Long)}.
     */
    public void testGetAssignHierarchyByEval() {
        List<EvalAssignHierarchy> eahs = null;

        eahs = evaluationService.getAssignHierarchyByEval(etdl.evaluationActive.getId());
        assertNotNull(eahs);
        assertEquals(1, eahs.size());
        assertEquals(etdl.assignHier1.getId(), eahs.get(0).getId());

        eahs = evaluationService.getAssignHierarchyByEval(etdl.evaluationNew.getId());
        assertNotNull(eahs);
        assertEquals(0, eahs.size());

        try {
            evaluationService.getAssignHierarchyByEval(null);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getEvalGroupsForEval(java.lang.Long[], boolean, Boolean)}.
     */
    public void testGetEvaluationGroups() {
        Map<Long, List<EvalGroup>> m = evaluationService.getEvalGroupsForEval( 
                new Long[] { etdl.evaluationClosed.getId() }, true, null );
        assertNotNull(m);
        List<EvalGroup> evalGroups = m.get( etdl.evaluationClosed.getId() );
        assertNotNull(evalGroups);
        assertEquals(2, evalGroups.size());
        assertTrue( evalGroups.get(0) instanceof EvalGroup );
        assertTrue( evalGroups.get(1) instanceof EvalGroup );

        m = evaluationService.getEvalGroupsForEval( 
                new Long[] { etdl.evaluationActive.getId() }, true, null );
        assertNotNull(m);
        evalGroups = m.get( etdl.evaluationActive.getId() );
        assertNotNull(evalGroups);
        assertEquals(1, evalGroups.size());
        assertTrue( evalGroups.get(0) instanceof EvalGroup );
        assertEquals( EvalTestDataLoad.SITE1_REF, ((EvalGroup) evalGroups.get(0)).evalGroupId );

        // test no assigned contexts
        m = evaluationService.getEvalGroupsForEval( 
                new Long[] { etdl.evaluationNew.getId() }, true, null );
        assertNotNull(m);
        evalGroups = m.get( etdl.evaluationNew.getId() );
        assertNotNull(evalGroups);
        assertEquals(0, evalGroups.size());

        // test invalid
        m = evaluationService.getEvalGroupsForEval( 
                new Long[] { EvalTestDataLoad.INVALID_LONG_ID }, true, null );
        assertNotNull(m);
        evalGroups = m.get( EvalTestDataLoad.INVALID_LONG_ID );
        assertNotNull(evalGroups);
        assertEquals(0, evalGroups.size());
    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getAssignGroupsForEvals(java.lang.Long[], boolean, Boolean)}.
     */
    public void testGetEvaluationAssignGroups() {
        // this is mostly tested above
        Map<Long, List<EvalAssignGroup>> m = evaluationService.getAssignGroupsForEvals( 
                new Long[] { etdl.evaluationClosed.getId() }, true, null );
        assertNotNull(m);
        List<EvalAssignGroup> eags = m.get( etdl.evaluationClosed.getId() );
        assertNotNull(eags);
        assertEquals(2, eags.size());
        assertTrue( eags.get(0) instanceof EvalAssignGroup );
        assertTrue( eags.get(1) instanceof EvalAssignGroup );
        assertEquals(etdl.assign3.getId(), eags.get(0).getId());
        assertEquals(etdl.assign4.getId(), eags.get(1).getId());
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#canCreateAssignEval(java.lang.String, java.lang.Long)}.
     */
    public void testCanCreateAssignEval() {
        // test can create an AC in new
        assertTrue( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.evaluationNew.getId()) );
        assertTrue( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.evaluationNew.getId()) );
        assertTrue( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.evaluationNewAdmin.getId()) );
        assertTrue( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.evaluationActive.getId()) );
        assertTrue( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.evaluationActive.getId()) );

        // test cannot create AC in closed evals
        assertFalse( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.evaluationClosed.getId()) );
        assertFalse( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.evaluationViewable.getId()) );

        // test cannot create AC without perms
        assertFalse( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.USER_ID, etdl.evaluationNew.getId()) );
        assertFalse( evaluationService.canCreateAssignEval(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.evaluationNewAdmin.getId()) );

        // test invalid evaluation id
        try {
            evaluationService.canCreateAssignEval(
                    EvalTestDataLoad.MAINT_USER_ID,  EvalTestDataLoad.INVALID_LONG_ID );
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#canDeleteAssignGroup(java.lang.String, java.lang.Long)}.
     */
    public void testCanDeleteAssignGroup() {
        // test can remove an AC in new eval
        assertTrue( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.assign8.getId()) );
        assertTrue( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.assign8.getId()) );
        assertTrue( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.assign7.getId()) );

        // test cannot remove AC from running evals
        assertFalse( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID, etdl.assign1.getId()) );
        assertFalse( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID, etdl.assign4.getId()) );
        assertFalse( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.assign3.getId()) );
        assertFalse( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.assign5.getId()) );

        // test cannot remove without permission
        assertFalse( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.USER_ID, etdl.assign6.getId()) );
        assertFalse( evaluationService.canDeleteAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID, etdl.assign7.getId()) );

        // test invalid evaluation id
        try {
            evaluationService.canDeleteAssignGroup(
                    EvalTestDataLoad.MAINT_USER_ID,  EvalTestDataLoad.INVALID_LONG_ID );
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }
    }



    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getResponseById(java.lang.Long)}.
     */
    public void testGetResponseById() {
        EvalResponse response = null;

        response = evaluationService.getResponseById( etdl.response1.getId() );
        assertNotNull(response);
        assertEquals(etdl.response1.getId(), response.getId());

        response = evaluationService.getResponseById( etdl.response2.getId() );
        assertNotNull(response);
        assertEquals(etdl.response2.getId(), response.getId());

        // test get eval by invalid id
        response = evaluationService.getResponseById( EvalTestDataLoad.INVALID_LONG_ID );
        assertNull(response);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getResponseForUserAndGroup(java.lang.Long, java.lang.String, java.lang.String)}.
     */
    public void testGetEvaluationResponseForUserAndGroup() {
        EvalResponse response = null;

        // check retrieving an existing responses
        response = evaluationService.getResponseForUserAndGroup(etdl.evaluationClosed.getId(), EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
        assertNotNull(response);
        assertEquals(etdl.response2.getId(), response.getId());

        // check creating a new response
        response = evaluationService.getResponseForUserAndGroup(etdl.evaluationActiveUntaken.getId(), EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
        assertNull(response);

        // test invalid params fails
        try {
            evaluationService.getResponseForUserAndGroup(EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    public void testGetEvalResponseIds() {
        List<Long> l = null;

        // retrieve all response Ids for an evaluation
        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), null, true);
        assertNotNull(l);
        assertEquals(3, l.size());
        assertTrue(l.contains( etdl.response2.getId() ));
        assertTrue(l.contains( etdl.response3.getId() ));
        assertTrue(l.contains( etdl.response6.getId() ));

        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), new String[] {}, true);
        assertNotNull(l);
        assertEquals(3, l.size());
        assertTrue(l.contains( etdl.response2.getId() ));
        assertTrue(l.contains( etdl.response3.getId() ));
        assertTrue(l.contains( etdl.response6.getId() ));

        // retrieve all response Ids for an evaluation using all groups
        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), 
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, true);
        assertNotNull(l);
        assertEquals(3, l.size());
        assertTrue(l.contains( etdl.response2.getId() ));
        assertTrue(l.contains( etdl.response3.getId() ));
        assertTrue(l.contains( etdl.response6.getId() ));

        // test retrieval of all responses for an evaluation
        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), 
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null);
        assertNotNull(l);
        assertEquals(3, l.size());

        // test retrieval of incomplete responses for an evaluation
        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), 
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, false);
        assertNotNull(l);
        assertEquals(0, l.size());

        // retrieve all response Ids for an evaluation in one group only
        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, true);
        assertNotNull(l);
        assertEquals(1, l.size());
        assertTrue(l.contains( etdl.response2.getId() ));

        // retrieve all response Ids for an evaluation in one group only
        l = evaluationService.getResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, true);
        assertNotNull(l);
        assertEquals(2, l.size());
        assertTrue(l.contains( etdl.response3.getId() ));
        assertTrue(l.contains( etdl.response6.getId() ));

        l = evaluationService.getResponseIds(etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, true);
        assertNotNull(l);
        assertEquals(1, l.size());
        assertTrue(l.contains( etdl.response1.getId() ));

        // try to get responses for an eval group that is not associated with this eval
        l = evaluationService.getResponseIds(etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, true);
        assertNotNull(l);
        assertEquals(0, l.size());

        // try to get responses for an eval with no responses
        l = evaluationService.getResponseIds(etdl.evaluationActiveUntaken.getId(), null, true);
        assertNotNull(l);
        assertEquals(0, l.size());

        // check that invalid eval ids cause failure
        try {
            l = evaluationService.getResponseIds( EvalTestDataLoad.INVALID_LONG_ID, null, true);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#getResponses(java.lang.String, java.lang.Long[], java.lang.String, java.lang.Boolean)}.
     */
    public void testGetEvaluationResponses() {
        List<EvalResponse> l = null;
        List<Long> ids = null;

        // retrieve response objects for all fields known
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, new String[] {EvalTestDataLoad.SITE1_REF}, true);
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response2.getId() ));

        // retrieve all responses for a user in an evaluation
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, null, true);
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response2.getId() ));
        assertTrue(ids.contains( etdl.response6.getId() ));

        // retrieve one response for a normal user in one eval
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId() }, null, true );
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response1.getId() ));

        l = evaluationService.getResponses(EvalTestDataLoad.STUDENT_USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, null, true );
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response3.getId() ));

        // check that empty array is ok for eval group ids
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId() }, new String[] {}, true );
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response1.getId() ));

        // retrieve all responses for a normal user in one eval
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, null, true );
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response2.getId() ));
        assertTrue(ids.contains( etdl.response6.getId() ));

        // limit retrieval by eval groups ids
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationClosed.getId() }, new String[] {EvalTestDataLoad.SITE1_REF}, true );
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response2.getId() ));

        // retrieve all responses for a normal user in mutliple evals
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, null, true );
        assertNotNull(l);
        assertEquals(4, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response1.getId() ));
        assertTrue(ids.contains( etdl.response2.getId() ));
        assertTrue(ids.contains( etdl.response4.getId() ));
        assertTrue(ids.contains( etdl.response6.getId() ));

        // attempt to retrieve all responses
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, null, null );
        assertNotNull(l);
        assertEquals(4, l.size());

        // attempt to retrieve all incomplete responses (there are none)
        l = evaluationService.getResponses(EvalTestDataLoad.USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, null, false );
        assertNotNull(l);
        assertEquals(0, l.size());

        // attempt to retrieve results for user that has no responses
        l = evaluationService.getResponses(EvalTestDataLoad.STUDENT_USER_ID, 
                new Long[] { etdl.evaluationActive.getId() }, null, true );
        assertNotNull(l);
        assertEquals(0, l.size());

        l = evaluationService.getResponses(EvalTestDataLoad.MAINT_USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, null, true );
        assertNotNull(l);
        assertEquals(0, l.size());

        // test that admin can fetch all results for evaluationSetupService
        l = evaluationService.getResponses(EvalTestDataLoad.ADMIN_USER_ID, 
                new Long[] { etdl.evaluationActive.getId(), etdl.evaluationClosed.getId(), etdl.evaluationViewable.getId() }, null, true );
        assertNotNull(l);
        assertEquals(6, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response1.getId() ));
        assertTrue(ids.contains( etdl.response2.getId() ));
        assertTrue(ids.contains( etdl.response3.getId() ));
        assertTrue(ids.contains( etdl.response4.getId() ));
        assertTrue(ids.contains( etdl.response5.getId() ));
        assertTrue(ids.contains( etdl.response6.getId() ));

        l = evaluationService.getResponses(EvalTestDataLoad.ADMIN_USER_ID, 
                new Long[] { etdl.evaluationViewable.getId() }, null, true );
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.response4.getId() ));
        assertTrue(ids.contains( etdl.response5.getId() ));

        // check that empty array causes failure
        try {
            l = evaluationService.getResponses(EvalTestDataLoad.ADMIN_USER_ID, 
                    new Long[] {}, null, true );
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        // check that null evalids cause failure
        try {
            l = evaluationService.getResponses(EvalTestDataLoad.ADMIN_USER_ID, 
                    null, null, true );
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#countResponses(java.lang.String, java.lang.Long[], java.lang.String, java.lang.Boolean)}.
     */
    public void testCountEvaluationResponses() {
        // test counts for all responses in various evaluationSetupService
        assertEquals(3, evaluationService.countResponses(null, new Long[] { etdl.evaluationClosed.getId() }, null, null) );
        assertEquals(2, evaluationService.countResponses(null, new Long[] { etdl.evaluationViewable.getId() }, null, null) );
        assertEquals(1, evaluationService.countResponses(null, new Long[] { etdl.evaluationActive.getId() }, null, null) );
        assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActiveUntaken.getId() }, null, null) );

        // limit by user
        assertEquals(3, evaluationService.countResponses(EvalTestDataLoad.ADMIN_USER_ID, new Long[] { etdl.evaluationClosed.getId() }, null, null) );
        assertEquals(2, evaluationService.countResponses(EvalTestDataLoad.USER_ID, new Long[] { etdl.evaluationClosed.getId() }, null, null) );
        assertEquals(1, evaluationService.countResponses(EvalTestDataLoad.STUDENT_USER_ID, new Long[] { etdl.evaluationClosed.getId() }, null, null) );
        assertEquals(0, evaluationService.countResponses(EvalTestDataLoad.MAINT_USER_ID, new Long[] { etdl.evaluationClosed.getId() }, null, null) );

        // test counts limited by evalGroupId
        assertEquals(1, evaluationService.countResponses(null, new Long[] { etdl.evaluationClosed.getId() }, 
                new String[] { EvalTestDataLoad.SITE1_REF }, null) );
        assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationViewable.getId() }, 
                new String[] { EvalTestDataLoad.SITE1_REF }, null) );
        assertEquals(1, evaluationService.countResponses(null, new Long[] { etdl.evaluationActive.getId() }, 
                new String[] { EvalTestDataLoad.SITE1_REF }, null) );
        assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActiveUntaken.getId() }, 
                new String[] { EvalTestDataLoad.SITE1_REF }, null) );

        assertEquals(2, evaluationService.countResponses(null, new Long[] { etdl.evaluationClosed.getId() }, 
                new String[] { EvalTestDataLoad.SITE2_REF }, null) );
        assertEquals(2, evaluationService.countResponses(null, new Long[] { etdl.evaluationViewable.getId() }, 
                new String[] { EvalTestDataLoad.SITE2_REF }, null) );
        assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActive.getId() }, 
                new String[] { EvalTestDataLoad.SITE2_REF }, null) );
        assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActiveUntaken.getId() }, 
                new String[] { EvalTestDataLoad.SITE2_REF }, null) );

        // test counts limited by completed
        assertEquals(3, evaluationService.countResponses(null, new Long[] { etdl.evaluationClosed.getId() }, null, true) );
        assertEquals(2, evaluationService.countResponses(null, new Long[] { etdl.evaluationViewable.getId() }, null, true) );
        assertEquals(1, evaluationService.countResponses(null, new Long[] { etdl.evaluationActive.getId() }, null, true) );
        assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActiveUntaken.getId() }, null, true) );

        // test counts limited by incomplete
        assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationClosed.getId() }, null, false) );
        assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationViewable.getId() }, null, false) );
        assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActive.getId() }, null, false) );
        assertEquals(0, evaluationService.countResponses(null, new Long[] { etdl.evaluationActiveUntaken.getId() }, null, false) );

        // check that empty array causes failure
        try {
            evaluationService.countResponses(null, new Long[] {}, null, true);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

        // check that null evalids cause failure
        try {
            evaluationService.countResponses(null, null, null, true);
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl#canModifyResponse(java.lang.String, java.lang.Long)}.
     */
    public void testCanModifyResponse() {
        // test owner can modify unlocked
        assertTrue( evaluationService.canModifyResponse(
                EvalTestDataLoad.USER_ID, etdl.response1.getId()) );

        // test admin cannot override permissions
        assertFalse( evaluationService.canModifyResponse(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.response1.getId()) );

        // test users without perms cannot modify
        assertFalse( evaluationService.canModifyResponse(
                EvalTestDataLoad.MAINT_USER_ID,  etdl.response1.getId()) );
        assertFalse( evaluationService.canModifyResponse(
                EvalTestDataLoad.STUDENT_USER_ID, etdl.response1.getId()) );

        // test no one can modify locked responses
        assertFalse( evaluationService.canModifyResponse(
                EvalTestDataLoad.ADMIN_USER_ID,  etdl.response3.getId()) );
        assertFalse( evaluationService.canModifyResponse(
                EvalTestDataLoad.STUDENT_USER_ID, etdl.response3.getId()) );

        // test invalid id causes failure
        try {
            evaluationService.canModifyResponse( EvalTestDataLoad.ADMIN_USER_ID, 
                    EvalTestDataLoad.INVALID_LONG_ID );
            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    // EMAIL TEMPLATES

    public void testGetEmailTemplatesForUser() {
        List<EvalEmailTemplate> l = null;

        // get all templates
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, null, null);
        assertNotNull(l);
        assertEquals(10, l.size());

        // get only default templates
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, null, true);
        assertNotNull(l);
        assertEquals(7, l.size());
        for (EvalEmailTemplate emailTemplate : l) {
            assertNotNull(emailTemplate.getDefaultType());
        }

        // get only non-default templates
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, null, false);
        assertNotNull(l);
        assertEquals(3, l.size());
        for (EvalEmailTemplate emailTemplate : l) {
            assertNull(emailTemplate.getDefaultType());
        }

        // get specific type of template only
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.EMAIL_TEMPLATE_REMINDER, null);
        assertNotNull(l);
        assertEquals(3, l.size());
        for (EvalEmailTemplate emailTemplate : l) {
            assertEquals(EvalConstants.EMAIL_TEMPLATE_REMINDER, emailTemplate.getType());
        }

        // should only get the default
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.EMAIL_TEMPLATE_REMINDER, true);
        assertNotNull(l);
        assertEquals(1, l.size());
        assertNotNull(l.get(0).getDefaultType());
        assertEquals(EvalConstants.EMAIL_TEMPLATE_REMINDER, l.get(0).getType());

        // should only get the non defaults
        l = evaluationService.getEmailTemplatesForUser(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.EMAIL_TEMPLATE_REMINDER, false);
        assertNotNull(l);
        assertEquals(2, l.size());

        // TODO check permissions for non-admin

    }

    public void testGetEmailTemplateById() {
        EvalEmailTemplate emailTemplate = evaluationService.getEmailTemplate(etdl.emailTemplate1.getId());
        assertNotNull(emailTemplate);
        assertEquals(etdl.emailTemplate1.getId(), emailTemplate.getId());
    }

    public void testGetDefaultEmailTemplate() {
        EvalEmailTemplate emailTemplate = null;

        // test getting the templates
        emailTemplate = evaluationService.getDefaultEmailTemplate( 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE );
        assertNotNull(emailTemplate);
        assertEquals( EvalConstants.EMAIL_TEMPLATE_AVAILABLE, 
                emailTemplate.getDefaultType() );
        assertEquals( EvalEmailConstants.EMAIL_AVAILABLE_DEFAULT_TEXT,
                emailTemplate.getMessage() );

        emailTemplate = evaluationService.getDefaultEmailTemplate( 
                EvalConstants.EMAIL_TEMPLATE_REMINDER );
        assertNotNull(emailTemplate);
        assertEquals( EvalConstants.EMAIL_TEMPLATE_REMINDER, 
                emailTemplate.getDefaultType() );
        assertEquals( EvalEmailConstants.EMAIL_REMINDER_DEFAULT_TEXT,
                emailTemplate.getMessage() );

        // test invalid constant causes failure
        try {
            emailTemplate = evaluationService.getDefaultEmailTemplate( EvalTestDataLoad.INVALID_CONSTANT_STRING );
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }

    }


    public void testGetEmailTemplate() {
        EvalEmailTemplate emailTemplate = null;

        // test getting the templates
        emailTemplate = evaluationService.getEmailTemplate(etdl.evaluationActive.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE );
        assertNotNull(emailTemplate);
        assertEquals( EvalEmailConstants.EMAIL_AVAILABLE_DEFAULT_TEXT,
                emailTemplate.getMessage() );

        emailTemplate = evaluationService.getEmailTemplate(etdl.evaluationActive.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER );
        assertNotNull(emailTemplate);
        assertEquals( "Email Template 3", emailTemplate.getMessage() );

        // test invalid constant causes failure
        try {
            emailTemplate = evaluationService.getEmailTemplate( EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.INVALID_CONSTANT_STRING );
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }

    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#canControlEmailTemplate(java.lang.String, java.lang.Long, int)}.
     */
    public void testCanControlEmailTemplateStringLongInt() {
        // test valid email template control perms when none assigned
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNewAdmin.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNewAdmin.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );

        // user does not have perm for this eval
        assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNewAdmin.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
        assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.USER_ID, etdl.evaluationNewAdmin.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );

        // test when template has some assigned already
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );

        // test admin overrides perms
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );

        // test not has permission
        assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNewAdmin.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
        assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
        assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.USER_ID, etdl.evaluationNew.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );

        // test CAN when evaluation is running (active+)
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationActive.getId(), 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE) );
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationActive.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );

        // check admin CAN override for running evals
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationClosed.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationClosed.getId(), 
                EvalConstants.EMAIL_TEMPLATE_REMINDER) );

        // check invalid evaluation id causes failure
        try {
            evaluationService.canControlEmailTemplate(
                    EvalTestDataLoad.ADMIN_USER_ID, 
                    EvalTestDataLoad.INVALID_LONG_ID, 
                    EvalConstants.EMAIL_TEMPLATE_REMINDER);
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEmailsLogicImpl#canControlEmailTemplate(java.lang.String, java.lang.Long, java.lang.Long)}.
     */
    public void testCanControlEmailTemplateStringLongLong() {
        // test valid email template control perms when none assigned
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId(), 
                etdl.emailTemplate1.getId()) );
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationNew.getId(), 
                etdl.emailTemplate2.getId()) );
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId(), 
                etdl.emailTemplate2.getId()) );

        // test with null eval id
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, null, etdl.emailTemplate2.getId()) );
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, null, etdl.emailTemplate2.getId()) );

        assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, null, etdl.emailTemplate1.getId()) );
        assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.USER_ID, null, etdl.emailTemplate2.getId()) );

        // test not has permissions
        assertFalse( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationNew.getId(), 
                etdl.emailTemplate1.getId()) );

        // test valid and active eval allowed
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, etdl.evaluationActive.getId(), 
                etdl.emailTemplate3.getId()) );

        // make sure admin CAN override for active eval
        assertTrue( evaluationService.canControlEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, etdl.evaluationActive.getId(), 
                etdl.emailTemplate3.getId()) );

        // check invalid evaluation id causes failure
        try {
            evaluationService.canControlEmailTemplate(
                    EvalTestDataLoad.ADMIN_USER_ID, 
                    EvalTestDataLoad.INVALID_LONG_ID, 
                    etdl.emailTemplate1.getId() );
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }

        // check invalid email template id causes failure
        try {
            evaluationService.canControlEmailTemplate(
                    EvalTestDataLoad.ADMIN_USER_ID, 
                    etdl.evaluationNew.getId(), 
                    EvalTestDataLoad.INVALID_LONG_ID );
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }

        // check non-matching evaluation and template causes failure
        try {
            evaluationService.canControlEmailTemplate(
                    EvalTestDataLoad.ADMIN_USER_ID, 
                    etdl.evaluationNew.getId(), 
                    etdl.emailTemplate3.getId() );
            fail("Should have thrown exception");
        } catch (RuntimeException e) {
            assertNotNull(e);
        }

    }

}
