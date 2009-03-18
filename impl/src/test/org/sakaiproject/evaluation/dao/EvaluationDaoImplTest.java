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

package org.sakaiproject.evaluation.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestDataImpl;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.genericdao.api.search.Restriction;
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
        // they also need to be referenced in the maven file
        return new String[] {"hibernate-test.xml", "spring-hibernate.xml"};
    }

    // run this before each test starts
    protected void onSetUpBeforeTransaction() throws Exception {
        // load the spring created dao class bean from the Spring Application Context
        evaluationDao = (EvaluationDao) applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationDao");
        if (evaluationDao == null) {
            throw new NullPointerException("DAO could not be retrieved from spring context");
        }

        // check the preloaded data
        assertTrue("Error preloading data", evaluationDao.countAll(EvalScale.class) > 0);

        // check the preloaded test data
        assertTrue("Error preloading test data", evaluationDao.countAll(EvalEvaluation.class) > 0);

        PreloadTestDataImpl ptd = (PreloadTestDataImpl) applicationContext.getBean("org.sakaiproject.evaluation.test.PreloadTestData");
        if (ptd == null) {
            throw new NullPointerException("PreloadTestDataImpl could not be retrieved from spring context");
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

        evalUnLocked = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, EvalTestDataLoad.MAINT_USER_ID, "Eval active not taken", null, 
                etdl.yesterday, etdl.tomorrow, etdl.tomorrow, etdl.threeDaysFuture, false, null,
                false, null, 
                EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_OPT_IN, new Integer(1), null, null, null, null,
                etdl.templatePublicUnused, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                EvalTestDataLoad.UNLOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null, null);
        evaluationDao.save( evalUnLocked );

    }

    /**
     * ADD unit tests below here, use testMethod as the name of the unit test,
     * Note that if a method is overloaded you should include the arguments in the
     * test name like so: testMethodClassInt (for method(Class, int);
     */

    public void testValidateDao() {
        assertNotNull(evaluationDao);
        List<EvalTemplate> templates = evaluationDao.findAll(EvalTemplate.class);
        assertNotNull( templates );
        assertTrue(templates.size() > 4);
        List<EvalAssignUser> assignUsers = evaluationDao.findAll(EvalAssignUser.class);
        assertNotNull( assignUsers );
        assertTrue(assignUsers.size() > 20);
    }

    public void testGetPaticipants() {
        List<EvalAssignUser> l = null;
        long start = 0l;

        // more testing at the higher level

        // get all participants for an evaluation
        start = System.currentTimeMillis();
        l = evaluationDao.getParticipantsForEval(etdl.evaluationActive.getId(), null, null, 
                null, null, null, null);
        System.out.println("Query executed in " + (System.currentTimeMillis()-start) + " ms");
        assertNotNull(l);
        assertEquals(2, l.size());

        // limit groups
        start = System.currentTimeMillis();
        l = evaluationDao.getParticipantsForEval(etdl.evaluationActive.getId(), null, new String[] {EvalTestDataLoad.SITE1_REF}, 
                null, null, null, null);
        System.out.println("Query executed in " + (System.currentTimeMillis()-start) + " ms");
        assertNotNull(l);
        assertEquals(2, l.size());

        start = System.currentTimeMillis();
        l = evaluationDao.getParticipantsForEval(etdl.evaluationActive.getId(), null, new String[] {EvalTestDataLoad.SITE2_REF}, 
                null, null, null, null);
        System.out.println("Query executed in " + (System.currentTimeMillis()-start) + " ms");
        assertNotNull(l);
        assertEquals(0, l.size());

        // get everyone who can take an evaluation
        start = System.currentTimeMillis();
        l = evaluationDao.getParticipantsForEval(etdl.evaluationActive.getId(), null, null, 
                EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        System.out.println("Query executed in " + (System.currentTimeMillis()-start) + " ms");
        assertNotNull(l);
        assertEquals(1, l.size());

        // get all the evals a user is assigned to
        start = System.currentTimeMillis();
        l = evaluationDao.getParticipantsForEval(null, EvalTestDataLoad.USER_ID, null, 
                EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        System.out.println("Query executed in " + (System.currentTimeMillis()-start) + " ms");
        assertNotNull(l);
        assertEquals(10, l.size());

        // get all active evals a user is assigned to
        start = System.currentTimeMillis();
        l = evaluationDao.getParticipantsForEval(null, EvalTestDataLoad.USER_ID, null, 
                EvalAssignUser.TYPE_EVALUATOR, null, null, EvalConstants.EVALUATION_STATE_ACTIVE);
        System.out.println("Query executed in " + (System.currentTimeMillis()-start) + " ms");
        assertNotNull(l);
        assertEquals(2, l.size());
    }

    public void testGetEvalsUserCanTake() {
        // get ones we can take
        List<EvalEvaluation> evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.USER_ID, true, true, false, 0, 0);
        assertNotNull(evals);
        assertEquals(1, evals.size());
        assertEquals(etdl.evaluationActive.getId(), evals.get(0).getId());

        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.STUDENT_USER_ID, true, true, false, 0, 0);
        assertNotNull(evals);
        assertEquals(0, evals.size());

        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.MAINT_USER_ID, true, true, false, 0, 0);
        assertNotNull(evals);
        assertEquals(0, evals.size());

        // admin normally takes none
        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.ADMIN_USER_ID, true, true, false, 0, 0);
        assertNotNull(evals);
        assertEquals(0, evals.size());

        // include anonymous
        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.USER_ID, true, true, null, 0, 0);
        assertNotNull(evals);
        assertEquals(2, evals.size());
        assertEquals(etdl.evaluationActive.getId(), evals.get(0).getId());
        assertEquals(etdl.evaluationActiveUntaken.getId(), evals.get(1).getId());

        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.STUDENT_USER_ID, true, true, null, 0, 0);
        assertNotNull(evals);
        assertEquals(1, evals.size());
        assertEquals(etdl.evaluationActiveUntaken.getId(), evals.get(0).getId());

        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.MAINT_USER_ID, true, true, null, 0, 0);
        assertNotNull(evals);
        assertEquals(1, evals.size());
        assertEquals(etdl.evaluationActiveUntaken.getId(), evals.get(0).getId());

        // TODO add assign groups support
        /**
        // testing instructor approval
        EvalAssignGroup eag = (EvalAssignGroup) evaluationDao.findById(EvalAssignGroup.class, etdl.assign1.getId());
        eag.setInstructorApproval(false); // make evaluationActive unapproved
        evaluationDao.save(eag);

        // get ones we can take
        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.USER_ID, true, true, false, 0, 0);
        assertNotNull(evals);
        assertEquals(0, evals.size());

        // include anonymous
        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.USER_ID, true, true, null, 0, 0);
        assertNotNull(evals);
        assertEquals(1, evals.size());
        assertEquals(etdl.evaluationActiveUntaken.getId(), evals.get(0).getId());
         **/        
    }

    public void testGetEvalsWithoutUserAssignments() {
        List<EvalEvaluation> evals = evaluationDao.getEvalsWithoutUserAssignments();
        assertNotNull(evals);
        assertTrue(evals.size() > 0);
    }

    public void testGetSharedEntitiesForUser() {
        List<EvalTemplate> l = null;
        List<Long> ids = null;

        // test using templates
        String[] props = new String[] { "type" };
        Object[] values = new Object[] { EvalConstants.TEMPLATE_TYPE_STANDARD };
        int[] comparisons = new int[] { Restriction.EQUALS };

        String[] order = new String[] {"sharing","title"};
        String[] options = new String[] {"notHidden"};
        String[] notEmptyOptions = new String[] {"notHidden", "notEmpty"};

        // all templates visible to user
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                EvalTestDataLoad.USER_ID, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, 
                props, values, comparisons, order, options, 0, 0);
        assertNotNull(l);
        assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.templatePublic.getId() ));
        assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
        assertTrue(ids.contains( etdl.templateUser.getId() ));
        assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
        assertTrue(ids.contains( etdl.templateEid.getId() ));

        // all templates visible to maint user
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                EvalTestDataLoad.MAINT_USER_ID, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, 
                props, values, comparisons, order, options, 0, 0);
        assertNotNull(l);
        assertEquals(4, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.templatePublic.getId() ));
        assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
        assertTrue(ids.contains( etdl.templateUnused.getId() ));
        assertTrue(ids.contains( etdl.templateEid.getId() ));

        // all templates owned by USER
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                EvalTestDataLoad.USER_ID, new String[] {EvalConstants.SHARING_PRIVATE}, 
                props, values, comparisons, order, options, 0, 0);
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.templateUser.getId() ));
        assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

        // all private templates
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE}, 
                props, values, comparisons, order, options, 0, 0);
        assertNotNull(l);
        assertEquals(6, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.templateAdmin.getId() ));
        assertTrue(ids.contains( etdl.templateAdminNoItems.getId() ));
        assertTrue(ids.contains( etdl.templateUnused.getId() ));
        assertTrue(ids.contains( etdl.templateUser.getId() ));
        assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
        assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));

        // all private non-empty templates
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE}, 
                props, values, comparisons, order, notEmptyOptions, 0, 0);
        assertNotNull(l);
        assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.templateAdmin.getId() ));
        assertTrue(ids.contains( etdl.templateUnused.getId() ));
        assertTrue(ids.contains( etdl.templateUser.getId() ));
        assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
        assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));

        // all public templates
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PUBLIC}, 
                props, values, comparisons, order, options, 0, 0);
        assertNotNull(l);
        assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.templatePublic.getId() ));
        assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
        assertTrue(ids.contains( etdl.templateEid.getId() ));

        // all templates (admin would use this)
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, 
                props, values, comparisons, order, options, 0, 0);
        assertNotNull(l);
        assertEquals(9, l.size());

        // all non-empty templates (admin would use this)
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, 
                props, values, comparisons, order, notEmptyOptions, 0, 0);
        assertNotNull(l);
        assertEquals(8, l.size());

        // no templates (no one should do this, it throws an exception)
        try {
            l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                    null, new String[] {}, 
                    props, values, comparisons, order, notEmptyOptions, 0, 0);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    public void testCountSharedEntitiesForUser() {
        int count = 0;

        // test using templates
        String[] props = new String[] { "type" };
        Object[] values = new Object[] { EvalConstants.TEMPLATE_TYPE_STANDARD };
        int[] comparisons = new int[] { Restriction.EQUALS };

        String[] options = new String[] {"notHidden"};
        String[] notEmptyOptions = new String[] {"notHidden", "notEmpty"};

        // all templates visible to user
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                EvalTestDataLoad.USER_ID, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, 
                props, values, comparisons, options);
        assertEquals(5, count);

        // all templates visible to maint user
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                EvalTestDataLoad.MAINT_USER_ID, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, 
                props, values, comparisons, options);
        assertEquals(4, count);

        // all templates owned by USER
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                EvalTestDataLoad.USER_ID, new String[] {EvalConstants.SHARING_PRIVATE}, 
                props, values, comparisons, options);
        assertEquals(2, count);

        // all private templates (admin only)
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE}, 
                props, values, comparisons, options);
        assertEquals(6, count);

        // all private non-empty templates (admin only)
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE}, 
                props, values, comparisons, notEmptyOptions);
        assertEquals(5, count);

        // all public templates
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PUBLIC}, 
                props, values, comparisons, options);
        assertEquals(3, count);

        // all templates (admin would use this)
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, 
                props, values, comparisons, options);
        assertEquals(9, count);

        // all non-empty templates (admin would use this)
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, 
                props, values, comparisons, notEmptyOptions);
        assertEquals(8, count);
    }

    public void testGetEvaluationsByEvalGroups() {
        List<EvalEvaluation> l = null;
        List<Long> ids = null;

        // testing instructor approval false
        EvalAssignGroup eag = (EvalAssignGroup) evaluationDao.findById(EvalAssignGroup.class, etdl.assign5.getId());
        eag.setInstructorApproval(false);
        evaluationDao.save(eag);

        // test getting all assigned evaluations for 2 sites
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, null, null, 0, 0);
        assertNotNull(l);
        assertEquals(6, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));
        assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

        // test getting all assigned (minus anonymous) evaluations for 2 sites
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, null, false, 0, 0);
        assertNotNull(l);
        assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));
        assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

        // test getting assigned evaluations by one evalGroupId
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF}, null, null, null, 0, 0);
        assertNotNull(l);
        assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));

        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE2_REF}, null, null, null, 0, 0);
        assertNotNull(l);
        assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(! ids.contains( etdl.evaluationActive.getId() ));
        assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

        // test getting by groupId and including anons (should not get any deleted or partial evals)
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF}, null, null, true, 0, 0);
        assertNotNull(l);
        assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));

        // test that the get active part works
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF}, true, null, null, 0, 0);
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE2_REF}, true, null, null, 0, 0);
        assertNotNull(l);
        assertEquals(0, l.size());

        // active minus anon
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF}, true, null, false, 0, 0);
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationActive.getId() ));

        // test that the get active plus anon works
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE2_REF}, true, null, true, 0, 0);
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

        // test getting from an invalid evalGroupId
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.INVALID_CONTEXT}, null, null, null, 0, 0);
        assertNotNull(l);
        assertEquals(0, l.size());		

        // test getting all anonymous evals
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {}, null, null, true, 0, 0);
        assertNotNull(l);
        assertEquals(1, l.size());		
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

        // testing getting no evals
        l = evaluationDao.getEvaluationsByEvalGroups(null, null, null, false, 0, 0);
        assertNotNull(l);
        assertEquals(0, l.size());

        // test unapproved assigned evaluations
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF}, null, false, null, 0, 0);
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));

        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, false, null, 0, 0);
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

        // test getting all APPROVED assigned evaluations
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF}, null, true, null, 0, 0);
        assertNotNull(l);
        assertEquals(4, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));

        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, true, null, 0, 0);
        assertNotNull(l);
        assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));

        //      // test getting taken evals only
        //      l = evaluationDao.getEvaluationsByEvalGroups(
        //            new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, null, null, true, EvalTestDataLoad.USER_ID, 0, 0);
        //      assertNotNull(l);
        //      assertEquals(3, l.size());
        //      ids = EvalTestDataLoad.makeIdList(l);
        //      assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        //      assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        //      assertTrue(ids.contains( etdl.evaluationViewable.getId() ));
        //
        //      l = evaluationDao.getEvaluationsByEvalGroups(
        //            new String[] {EvalTestDataLoad.SITE1_REF}, null, null, null, true, EvalTestDataLoad.USER_ID, 0, 0);
        //      assertNotNull(l);
        //      assertEquals(2, l.size());
        //      ids = EvalTestDataLoad.makeIdList(l);
        //      assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        //      assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        //
        //      l = evaluationDao.getEvaluationsByEvalGroups(
        //            new String[] {EvalTestDataLoad.SITE2_REF}, null, null, null, true, EvalTestDataLoad.USER_ID, 0, 0);
        //      assertNotNull(l);
        //      assertEquals(2, l.size());
        //      ids = EvalTestDataLoad.makeIdList(l);
        //      assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        //      assertTrue(ids.contains( etdl.evaluationViewable.getId() ));
        //
        //      // test getting untaken evals only
        //      l = evaluationDao.getEvaluationsByEvalGroups(
        //            new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, null, null, false, EvalTestDataLoad.USER_ID, 0, 0);
        //      assertNotNull(l);
        //      assertEquals(3, l.size());
        //      ids = EvalTestDataLoad.makeIdList(l);
        //      assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        //      assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        //      assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));
        //
        //      l = evaluationDao.getEvaluationsByEvalGroups(
        //            new String[] {EvalTestDataLoad.SITE2_REF}, null, null, null, false, EvalTestDataLoad.USER_ID, 0, 0);
        //      assertNotNull(l);
        //      assertEquals(4, l.size());
        //      ids = EvalTestDataLoad.makeIdList(l);
        //      assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        //      assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        //      assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        //      assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));

    }


    public void testGetEvaluationsForOwnerAndGroups() {
        List<EvalEvaluation> l = null;
        List<Long> ids = null;

        // test getting all evals
        l = evaluationDao.getEvaluationsForOwnerAndGroups(null, null, null, 0, 0, false);
        assertNotNull(l);
        assertEquals(9, l.size());
        // check the order
        ids = EvalTestDataLoad.makeIdList(l);
        assertEquals(ids.get(0), etdl.evaluationViewable.getId() );
        assertEquals(ids.get(1), etdl.evaluationClosed.getId() );
        assertEquals(ids.get(2), etdl.evaluationClosedUntaken.getId() );
        assertEquals(ids.get(3), etdl.evaluationActive.getId() );
        assertEquals(ids.get(4), etdl.evaluationProvided.getId() );

        // test getting all evals with limit
        l = evaluationDao.getEvaluationsForOwnerAndGroups(null, null, null, 0, 3, false);
        assertNotNull(l);
        assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        // check order and return values
        assertEquals(ids.get(0), etdl.evaluationViewable.getId() );
        assertEquals(ids.get(1), etdl.evaluationClosed.getId() );
        assertEquals(ids.get(2), etdl.evaluationClosedUntaken.getId() );

        l = evaluationDao.getEvaluationsForOwnerAndGroups(null, null, null, 2, 2, false);
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        // check order and return values
        assertEquals(ids.get(0), etdl.evaluationClosedUntaken.getId() );
        assertEquals(ids.get(1), etdl.evaluationActive.getId() );

        // test filtering by owner
        l = evaluationDao.getEvaluationsForOwnerAndGroups(EvalTestDataLoad.ADMIN_USER_ID, null, null, 0, 0, false);
        assertNotNull(l);
        assertEquals(4, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));
        assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

        l = evaluationDao.getEvaluationsForOwnerAndGroups(EvalTestDataLoad.USER_ID, null, null, 0, 0, false);
        assertNotNull(l);
        assertEquals(0, l.size());
        ids = EvalTestDataLoad.makeIdList(l);

        // test filtering by groups
        l = evaluationDao.getEvaluationsForOwnerAndGroups(null, 
                new String[] {EvalTestDataLoad.SITE1_REF}, null, 0, 0, false);
        assertNotNull(l);
        assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));
        assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));

        // test filtering by owner and groups
        l = evaluationDao.getEvaluationsForOwnerAndGroups(EvalTestDataLoad.ADMIN_USER_ID, 
                new String[] {EvalTestDataLoad.SITE1_REF}, null, 0, 0, false);
        assertNotNull(l);
        assertEquals(6, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));
        assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#getAnswers(java.lang.Long, java.lang.Long)}.
     */
    public void testGetAnswers() {
        Set<EvalAnswer> s = null;
        List<EvalAnswer> l = null;
        List<Long> ids = null;

        s = etdl.response2.getAnswers();
        assertNotNull(s);
        assertEquals(2, s.size());
        ids = EvalTestDataLoad.makeIdList(s);
        assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        assertTrue(ids.contains( etdl.answer2_5A.getId() ));

        // test getting all answers first
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), null, null);
        assertNotNull(l);
        assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        assertTrue(ids.contains( etdl.answer2_5A.getId() ));
        assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // restrict to template item
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), null, new Long[] {etdl.templateItem2A.getId()});
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // restrict to multiple template items
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), null, new Long[] {etdl.templateItem2A.getId(), etdl.templateItem5A.getId()});
        assertNotNull(l);
        assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        assertTrue(ids.contains( etdl.answer2_5A.getId() ));
        assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // test restricting to groups
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, null);
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        assertTrue(ids.contains( etdl.answer2_5A.getId() ));

        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, null);
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // test restricting to groups and TIs
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, new Long[] {etdl.templateItem2A.getId()});
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer2_2A.getId() ));

        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, new Long[] {etdl.templateItem2A.getId()});
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // test restricting to answers not in this group
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, new Long[] {etdl.templateItem5A.getId()});
        assertNotNull(l);
        assertEquals(0, l.size());

        // test template item that is not in this evaluation
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), null, new Long[] {etdl.templateItem1U.getId()});
        assertNotNull(l);
        assertEquals(0, l.size());

        // test invalid eval id returns nothing
        l = evaluationDao.getAnswers(EvalTestDataLoad.INVALID_LONG_ID, null, null);
        assertNotNull(l);
        assertEquals(0, l.size());
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#removeTemplateItems(org.sakaiproject.evaluation.model.EvalTemplateItem[])}.
     */
    public void testRemoveTemplateItems() {

        // test removing a single templateItem
        EvalTemplateItem eti1 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, etdl.templateItem1User.getId());

        // verify that the item/template link exists before removal
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

        // test removing templateItem OK
        evaluationDao.removeTemplateItems( new EvalTemplateItem[] {etdl.templateItem1User} );
        assertNull( evaluationDao.findById(EvalTemplateItem.class, etdl.templateItem1User.getId()) );

        // verify that the item/template link no longer exists
        assertNotNull( eti1.getItem().getTemplateItems() );
        assertNotNull( eti1.getTemplate().getTemplateItems() );
        assertFalse( eti1.getItem().getTemplateItems().isEmpty() );
        assertFalse( eti1.getTemplate().getTemplateItems().isEmpty() );
        assertEquals( itemsSize-1, eti1.getItem().getTemplateItems().size() );
        assertEquals( templatesSize-1, eti1.getTemplate().getTemplateItems().size() );
        assertTrue(! eti1.getItem().getTemplateItems().contains( eti1 ) );
        assertTrue(! eti1.getTemplate().getTemplateItems().contains( eti1 ) );

        // test removing a group of templateItems (item 3 and 5 from UnUsed)
        EvalTemplateItem eti3 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, etdl.templateItem3U.getId());
        EvalTemplateItem eti5 = (EvalTemplateItem) evaluationDao.findById(EvalTemplateItem.class, etdl.templateItem5U.getId());

        // verify that the item/template link exists before removal
        assertNotNull( eti3 );
        assertNotNull( eti3.getItem() );
        assertNotNull( eti3.getTemplate() );
        assertNotNull( eti3.getItem().getTemplateItems() );
        assertNotNull( eti3.getTemplate().getTemplateItems() );
        assertFalse( eti3.getItem().getTemplateItems().isEmpty() );
        assertFalse( eti3.getTemplate().getTemplateItems().isEmpty() );
        assertTrue( eti3.getItem().getTemplateItems().contains( eti3 ) );
        assertTrue( eti3.getTemplate().getTemplateItems().contains( eti3 ) );
        int itemsSize3 = eti3.getItem().getTemplateItems().size();

        assertNotNull( eti5 );
        assertNotNull( eti5.getItem() );
        assertNotNull( eti5.getTemplate() );
        assertNotNull( eti5.getItem().getTemplateItems() );
        assertNotNull( eti5.getTemplate().getTemplateItems() );
        assertFalse( eti5.getItem().getTemplateItems().isEmpty() );
        assertFalse( eti5.getTemplate().getTemplateItems().isEmpty() );
        assertTrue( eti5.getItem().getTemplateItems().contains( eti5 ) );
        assertTrue( eti5.getTemplate().getTemplateItems().contains( eti5 ) );
        int itemsSize5 = eti5.getItem().getTemplateItems().size();

        // test removing templateItem OK
        evaluationDao.removeTemplateItems( new EvalTemplateItem[] {etdl.templateItem3U, etdl.templateItem5U} );
        assertNull( evaluationDao.findById(EvalTemplateItem.class, etdl.templateItem3U.getId()) );
        assertNull( evaluationDao.findById(EvalTemplateItem.class, etdl.templateItem5U.getId()) );

        // verify that the item/template link no longer exists
        assertNotNull( eti3.getItem().getTemplateItems() );
        assertFalse( eti3.getItem().getTemplateItems().isEmpty() );
        assertEquals( itemsSize3-1, eti3.getItem().getTemplateItems().size() );
        assertTrue(! eti3.getItem().getTemplateItems().contains( eti3 ) );

        assertNotNull( eti5.getItem().getTemplateItems() );
        assertFalse( eti5.getItem().getTemplateItems().isEmpty() );
        assertEquals( itemsSize5-1, eti5.getItem().getTemplateItems().size() );
        assertTrue(! eti5.getItem().getTemplateItems().contains( eti5 ) );

        // should be only one items left in this template now
        assertNotNull( eti3.getTemplate().getTemplateItems() );
        assertEquals(1, eti3.getTemplate().getTemplateItems().size() );
        EvalTemplate template = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, eti3.getTemplate().getId());
        assertNotNull( template );
        assertNotNull( template.getTemplateItems() );
        assertEquals(1, template.getTemplateItems().size() );

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#getTemplateItemsByTemplate(java.lang.Long, java.lang.String[], java.lang.String[], java.lang.String[])}.
     */
    public void testGetTemplateItemsByTemplate() {
        List<EvalTemplateItem> l = null;
        List<Long> ids = null;

        // test the basic return of items in the template
        l = evaluationDao.getTemplateItemsByTemplate(etdl.templateAdmin.getId(), 
                null, null, null);
        assertNotNull(l);
        assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.templateItem2A.getId() ));
        assertTrue(ids.contains( etdl.templateItem3A.getId() ));
        assertTrue(ids.contains( etdl.templateItem5A.getId() ));

        // check that the return order is correct
        assertEquals( 1, ((EvalTemplateItem)l.get(0)).getDisplayOrder().intValue() );
        assertEquals( 2, ((EvalTemplateItem)l.get(1)).getDisplayOrder().intValue() );
        assertEquals( 3, ((EvalTemplateItem)l.get(2)).getDisplayOrder().intValue() );

        // test getting just the top level items
        l = evaluationDao.getTemplateItemsByTemplate(etdl.templateAdminComplex.getId(), 
                null, null, null);
        assertNotNull(l);
        assertEquals(0, l.size());

        // test getting instructor items
        l = evaluationDao.getTemplateItemsByTemplate(etdl.templateAdminComplex.getId(), 
                null, new String[] { EvalTestDataLoad.MAINT_USER_ID }, null);
        assertNotNull(l);
        assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.templateItem10AC1.getId() ));

        // test getting course items
        l = evaluationDao.getTemplateItemsByTemplate(etdl.templateAdminComplex.getId(), 
                null, null, 
                new String[] { EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF });
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.templateItem10AC2.getId() ));
        assertTrue(ids.contains( etdl.templateItem10AC3.getId() ));

        // test getting both together
        l = evaluationDao.getTemplateItemsByTemplate(etdl.templateAdminComplex.getId(), 
                null, new String[] { EvalTestDataLoad.MAINT_USER_ID }, 
                new String[] { EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF });
        assertNotNull(l);
        assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains( etdl.templateItem10AC1.getId() ));
        assertTrue(ids.contains( etdl.templateItem10AC2.getId() ));
        assertTrue(ids.contains( etdl.templateItem10AC3.getId() ));

        // test that a bunch of invalid stuff simply returns no results
        l = evaluationDao.getTemplateItemsByTemplate(etdl.templateAdminComplex.getId(), 
                new String[] { EvalTestDataLoad.INVALID_CONSTANT_STRING }, 
                new String[] { EvalTestDataLoad.INVALID_CONSTANT_STRING, EvalTestDataLoad.INVALID_CONSTANT_STRING }, 
                new String[] { EvalTestDataLoad.INVALID_CONSTANT_STRING, EvalTestDataLoad.INVALID_CONSTANT_STRING, EvalTestDataLoad.INVALID_CONSTANT_STRING });
        assertNotNull(l);
        assertEquals(0, l.size());

    }

    public void testGetResponseIds() {
        List<Long> l = null;

        l = evaluationDao.getResponseIds(etdl.evaluationClosed.getId(), null, null, null);
        assertNotNull(l);
        assertEquals(3, l.size());
        assertTrue( l.contains(etdl.response2.getId()) );
        assertTrue( l.contains(etdl.response3.getId()) );
        assertTrue( l.contains(etdl.response6.getId()) );

        l = evaluationDao.getResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, null);
        assertNotNull(l);
        assertEquals(3, l.size());
        assertTrue( l.contains(etdl.response2.getId()) );
        assertTrue( l.contains(etdl.response3.getId()) );
        assertTrue( l.contains(etdl.response6.getId()) );

        l = evaluationDao.getResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, null, null);
        assertNotNull(l);
        assertEquals(1, l.size());
        assertTrue( l.contains(etdl.response2.getId()) );

        // test invalid evalid
        l = evaluationDao.getResponseIds(EvalTestDataLoad.INVALID_LONG_ID, null, null, null);
        assertNotNull(l);
        assertEquals(0, l.size());

    }


    public void testRemoveResponses() {
        // check that response and answer are removed correctly
        int curR = evaluationDao.countAll(EvalResponse.class);
        int curA = evaluationDao.countAll(EvalAnswer.class);
        evaluationDao.removeResponses(new Long[] {etdl.response1.getId()});
        int remainR = evaluationDao.countAll(EvalResponse.class);
        int remainA = evaluationDao.countAll(EvalResponse.class);
        assertTrue(remainR < curR);
        assertTrue(remainA < curA);
        // stupid hibernate is making this test a pain -AZ
        //      assertNull( evaluationDao.findById(EvalResponse.class, etdl.response1.getId()) );
        //      assertNull( evaluationDao.findById(EvalAnswer.class, etdl.answer1_1.getId()) );

    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#getEvalCategories(String)}
     */
    public void testGetEvalCategories() {
        List<String> l = null;

        // test the basic return of categories
        l = evaluationDao.getEvalCategories(null);
        assertNotNull(l);
        assertEquals(2, l.size());
        assertTrue( l.contains(EvalTestDataLoad.EVAL_CATEGORY_1) );
        assertTrue( l.contains(EvalTestDataLoad.EVAL_CATEGORY_2) );

        // test the return of cats for a user
        l = evaluationDao.getEvalCategories(EvalTestDataLoad.MAINT_USER_ID);
        assertNotNull(l);
        assertEquals(1, l.size());
        assertTrue( l.contains(EvalTestDataLoad.EVAL_CATEGORY_1) );

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#getNodeIdForEvalGroup(java.lang.String)}.
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
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    //   public void testGetTemplateIdsForEvaluation() {
    //      List<Long> templateIds = null;
    //
    //      templateIds = evaluationDao.getTemplateIdForEvaluation(etdl.evaluationActive.getId());
    //      assertNotNull(templateIds);
    //      assertEquals(1, templateIds.size());
    //      assertTrue( templateIds.contains( etdl.templateUser.getId() ) );
    //
    //      templateIds = evaluationDao.getTemplateIdForEvaluation(etdl.evaluationClosed.getId());
    //      assertNotNull(templateIds);
    //      assertEquals(2, templateIds.size());
    //      assertTrue( templateIds.contains( etdl.templateAdmin.getId() ) );
    //      assertTrue( templateIds.contains( etdl.templateAdminComplex.getId() ) );
    //
    //      templateIds = evaluationDao.getTemplateIdForEvaluation(EvalTestDataLoad.INVALID_LONG_ID);
    //      assertNotNull(templateIds);
    //      assertEquals(0, templateIds.size());
    //   }

    public void testGetResponseUserIds() {
        Set<String> userIds = null;

        // check getting responders from complete evaluation
        userIds = evaluationDao.getResponseUserIds(etdl.evaluationClosed.getId(), null);
        assertNotNull(userIds);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));
        assertTrue(userIds.contains(EvalTestDataLoad.STUDENT_USER_ID));

        // test getting from subset of the groups
        userIds = evaluationDao.getResponseUserIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF});
        assertNotNull(userIds);
        assertEquals(1, userIds.size());
        assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));

        // test getting none
        userIds = evaluationDao.getResponseUserIds(etdl.evaluationActiveUntaken.getId(), null);
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

        // test using invalid group ids retrieves no results
        userIds = evaluationDao.getResponseUserIds(etdl.evaluationClosed.getId(), new String[] {"xxxxxx", "fakeyandnotreal"});
        assertNotNull(userIds);
        assertEquals(0, userIds.size());

    }

    public void testGetViewableEvalGroupIds() {
        Set<String> evalGroupIds = null;

        // check for groups that are fully enabled
        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationClosed.getId(), EvalAssignUser.TYPE_EVALUATEE, null);
        assertNotNull(evalGroupIds);
        assertEquals(1, evalGroupIds.size());
        assertTrue(evalGroupIds.contains(etdl.assign3.getEvalGroupId()));

        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationClosed.getId(), EvalAssignUser.TYPE_EVALUATOR, null);
        assertNotNull(evalGroupIds);
        assertEquals(1, evalGroupIds.size());
        assertTrue(evalGroupIds.contains(etdl.assign4.getEvalGroupId()));

        // check for mixture - not in the test data
//        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationNewAdmin.getId(), EvalAssignUser.TYPE_EVALUATEE, null);
//        assertNotNull(evalGroupIds);
//        assertEquals(2, evalGroupIds.size());
//        assertTrue(evalGroupIds.contains(etdl.assign7.getEvalGroupId()));
//        assertTrue(evalGroupIds.contains(etdl.assignGroupProvided.getEvalGroupId()));

        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationNewAdmin.getId(), EvalAssignUser.TYPE_EVALUATOR, null);
        assertNotNull(evalGroupIds);
        assertEquals(1, evalGroupIds.size());
        assertTrue(evalGroupIds.contains(etdl.assign6.getEvalGroupId()));

        // check for unassigned to return none
        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationNew.getId(), EvalAssignUser.TYPE_EVALUATEE, null);
        assertNotNull(evalGroupIds);
        assertEquals(0, evalGroupIds.size());

        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationNew.getId(), EvalAssignUser.TYPE_EVALUATOR, null);
        assertNotNull(evalGroupIds);
        assertEquals(0, evalGroupIds.size());

        // check that other perms return nothing
        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationNewAdmin.getId(), EvalAssignUser.TYPE_ASSISTANT, null);
        assertNotNull(evalGroupIds);
        assertEquals(0, evalGroupIds.size());

        // check for limits on the returns
        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationClosed.getId(), EvalAssignUser.TYPE_EVALUATEE, 
                new String[] {etdl.assign3.getEvalGroupId()});
        assertNotNull(evalGroupIds);
        assertEquals(1, evalGroupIds.size());
        assertTrue(evalGroupIds.contains(etdl.assign3.getEvalGroupId()));

        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationNewAdmin.getId(), EvalAssignUser.TYPE_EVALUATEE, 
                new String[] {etdl.assign7.getEvalGroupId()});
        assertNotNull(evalGroupIds);
        assertEquals(1, evalGroupIds.size());
        assertTrue(evalGroupIds.contains(etdl.assign7.getEvalGroupId()));

        // check for limits on the returns which limit it to none
        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationClosed.getId(), EvalAssignUser.TYPE_EVALUATEE, 
                new String[] {EvalTestDataLoad.INVALID_CONSTANT_STRING});
        assertNotNull(evalGroupIds);
        assertEquals(0, evalGroupIds.size());

        // check for null evaluation id
        try {
            evaluationDao.getViewableEvalGroupIds(null, EvalConstants.PERM_ASSIGN_EVALUATION, null);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }

    }


    public void testGetEvalAdhocGroupsByUserAndPerm() {
        List<EvalAdhocGroup> l = null;
        List<Long> ids = null;

        // make sure the group has the user
        EvalAdhocGroup checkGroup = (EvalAdhocGroup) evaluationDao.findById(EvalAdhocGroup.class, etdl.group2.getId());
        assertTrue( ArrayUtils.contains(checkGroup.getParticipantIds(), etdl.user3.getUserId()) );

        l = evaluationDao.getEvalAdhocGroupsByUserAndPerm(etdl.user3.getUserId(), EvalConstants.PERM_TAKE_EVALUATION);
        assertNotNull(l);
        assertEquals(1, l.size());
        assertEquals(etdl.group2.getId(), l.get(0).getId());

        l = evaluationDao.getEvalAdhocGroupsByUserAndPerm(EvalTestDataLoad.STUDENT_USER_ID, EvalConstants.PERM_TAKE_EVALUATION);
        assertNotNull(l);
        assertEquals(1, l.size());
        assertEquals(etdl.group1.getId(), l.get(0).getId());

        l = evaluationDao.getEvalAdhocGroupsByUserAndPerm(etdl.user1.getUserId(), EvalConstants.PERM_TAKE_EVALUATION);
        assertNotNull(l);
        assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        assertTrue(ids.contains(etdl.group1.getId()));
        assertTrue(ids.contains(etdl.group2.getId()));

        l = evaluationDao.getEvalAdhocGroupsByUserAndPerm(etdl.user2.getUserId(), EvalConstants.PERM_TAKE_EVALUATION);
        assertNotNull(l);
        assertEquals(0, l.size());

    }


    public void testIsUserAllowedInAdhocGroup() {
        boolean allowed = false;

        allowed = evaluationDao.isUserAllowedInAdhocGroup(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION, etdl.group2.getEvalGroupId());
        assertTrue(allowed);

        allowed = evaluationDao.isUserAllowedInAdhocGroup(EvalTestDataLoad.USER_ID, EvalConstants.PERM_BE_EVALUATED, etdl.group2.getEvalGroupId());
        assertFalse(allowed);

        allowed = evaluationDao.isUserAllowedInAdhocGroup(etdl.user1.getUserId(), EvalConstants.PERM_TAKE_EVALUATION, etdl.group1.getEvalGroupId());
        assertTrue(allowed);

        allowed = evaluationDao.isUserAllowedInAdhocGroup(etdl.user1.getUserId(), EvalConstants.PERM_BE_EVALUATED, etdl.group1.getEvalGroupId());
        assertFalse(allowed);

        allowed = evaluationDao.isUserAllowedInAdhocGroup(etdl.user2.getUserId(), EvalConstants.PERM_TAKE_EVALUATION, etdl.group1.getEvalGroupId());
        assertFalse(allowed);

        allowed = evaluationDao.isUserAllowedInAdhocGroup(etdl.user2.getUserId(), EvalConstants.PERM_BE_EVALUATED, etdl.group1.getEvalGroupId());
        assertFalse(allowed);
    }




    // LOCKING tests

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#lockScale(org.sakaiproject.evaluation.model.EvalScale, java.lang.Boolean)}.
     */
    public void testLockScale() {

        // check that locked scale gets unlocked (no locking item)
        assertTrue( scaleLocked.getLocked().booleanValue() );
        assertTrue( evaluationDao.lockScale( scaleLocked, Boolean.FALSE ) );
        assertFalse( scaleLocked.getLocked().booleanValue() );
        // check that unlocking an unlocked scale is not a problem
        assertFalse( evaluationDao.lockScale( scaleLocked, Boolean.FALSE ) );

        // check that locked scale that is locked by an item cannot be unlocked
        EvalScale scale1 = (EvalScale) evaluationDao.findById(EvalScale.class, etdl.scale1.getId());
        assertTrue( scale1.getLocked().booleanValue() );
        assertFalse( evaluationDao.lockScale( scale1, Boolean.FALSE ) );
        assertTrue( scale1.getLocked().booleanValue() );
        // check that locking a locked scale is not a problem
        assertFalse( evaluationDao.lockScale( scale1, Boolean.TRUE ) );

        // check that new scale cannot be unlocked
        try {
            evaluationDao.lockScale( 
                    new EvalScale(new Date(), 
                            EvalTestDataLoad.ADMIN_USER_ID, "new scale", 
                            EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PRIVATE, Boolean.FALSE),
                            Boolean.FALSE
            );
            fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#lockItem(org.sakaiproject.evaluation.model.EvalItem, java.lang.Boolean)}.
     */
    public void testLockItem() {

        // check that unlocked item gets locked (no scale)
        assertFalse( etdl.item7.getLocked().booleanValue() );
        assertTrue( evaluationDao.lockItem( etdl.item7, Boolean.TRUE ) );
        assertTrue( etdl.item7.getLocked().booleanValue() );

        // check that locked item does nothing bad if locked again (no scale, not used)
        assertTrue( itemLocked.getLocked().booleanValue() );
        assertFalse( evaluationDao.lockItem( itemLocked, Boolean.TRUE ) );
        assertTrue( itemLocked.getLocked().booleanValue() );

        // check that locked item gets unlocked (no scale, not used)
        assertTrue( itemLocked.getLocked().booleanValue() );
        assertTrue( evaluationDao.lockItem( itemLocked, Boolean.FALSE ) );
        assertFalse( itemLocked.getLocked().booleanValue() );

        // check that locked item that is locked by a template cannot be unlocked
        assertTrue( etdl.item1.getLocked().booleanValue() );
        assertFalse( evaluationDao.lockItem( etdl.item1, Boolean.FALSE ) );
        assertTrue( etdl.item1.getLocked().booleanValue() );

        // check that locked item that is locked by a template can be locked without exception
        assertTrue( etdl.item1.getLocked().booleanValue() );
        assertFalse( evaluationDao.lockItem( etdl.item1, Boolean.TRUE ) );
        assertTrue( etdl.item1.getLocked().booleanValue() );

        // verify that associated scale is unlocked
        assertFalse( itemUnlocked.getScale().getLocked().booleanValue() );

        // check that unlocked item gets locked (scale)
        assertFalse( itemUnlocked.getLocked().booleanValue() );
        assertTrue( evaluationDao.lockItem( itemUnlocked, Boolean.TRUE ) );
        assertTrue( itemUnlocked.getLocked().booleanValue() );

        // verify that associated scale gets locked
        assertTrue( itemUnlocked.getScale().getLocked().booleanValue() );

        // check that locked item gets unlocked (scale)
        assertTrue( itemUnlocked.getLocked().booleanValue() );
        assertTrue( evaluationDao.lockItem( itemUnlocked, Boolean.FALSE ) );
        assertFalse( itemUnlocked.getLocked().booleanValue() );

        // verify that associated scale gets unlocked
        assertFalse( itemUnlocked.getScale().getLocked().booleanValue() );

        // check that locked item gets unlocked (scale locked by another item)
        assertTrue( etdl.item4.getScale().getLocked().booleanValue() );
        assertTrue( evaluationDao.lockItem( etdl.item4, Boolean.TRUE ) );
        assertTrue( etdl.item4.getLocked().booleanValue() );

        assertTrue( evaluationDao.lockItem( etdl.item4, Boolean.FALSE ) );
        assertFalse( etdl.item4.getLocked().booleanValue() );

        // verify that associated scale does not get unlocked
        assertTrue( etdl.item4.getScale().getLocked().booleanValue() );

        // check that new item cannot be locked/unlocked
        try {
            evaluationDao.lockItem(
                    new EvalItem( new Date(), EvalTestDataLoad.ADMIN_USER_ID, 
                            "something", EvalConstants.SHARING_PRIVATE, 
                            EvalConstants.ITEM_TYPE_HEADER, Boolean.FALSE),
                            Boolean.TRUE);
            fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#lockTemplate(org.sakaiproject.evaluation.model.EvalTemplate, java.lang.Boolean)}.
     */
    public void testLockTemplate() {

        // check that unlocked template gets locked (no items)
        assertFalse( etdl.templateAdminNoItems.getLocked().booleanValue() );
        assertTrue( evaluationDao.lockTemplate( etdl.templateAdminNoItems, Boolean.TRUE ) );
        assertTrue( etdl.templateAdminNoItems.getLocked().booleanValue() );

        // check that locked template is ok with getting locked again (no problems)
        assertTrue( etdl.templateAdminNoItems.getLocked().booleanValue() );
        assertFalse( evaluationDao.lockTemplate( etdl.templateAdminNoItems, Boolean.TRUE ) );
        assertTrue( etdl.templateAdminNoItems.getLocked().booleanValue() );

        // check that locked template gets unlocked (no items)
        assertTrue( etdl.templateAdminNoItems.getLocked().booleanValue() );
        assertTrue( evaluationDao.lockTemplate( etdl.templateAdminNoItems, Boolean.FALSE ) );
        assertFalse( etdl.templateAdminNoItems.getLocked().booleanValue() );

        // check that locked template that is locked by an evaluation cannot be unlocked
        assertTrue( etdl.templateUser.getLocked().booleanValue() );
        assertFalse( evaluationDao.lockTemplate( etdl.templateUser, Boolean.FALSE ) );
        assertTrue( etdl.templateUser.getLocked().booleanValue() );

        // check that locked template that is locked by an evaluation can be locked without exception
        assertTrue( etdl.templateUser.getLocked().booleanValue() );
        assertFalse( evaluationDao.lockTemplate( etdl.templateUser, Boolean.TRUE ) );
        assertTrue( etdl.templateUser.getLocked().booleanValue() );

        // check that unlocked template gets locked (items)
        assertFalse( etdl.item6.getLocked().booleanValue() );
        assertFalse( etdl.templateUserUnused.getLocked().booleanValue() );
        assertTrue( evaluationDao.lockTemplate( etdl.templateUserUnused, Boolean.TRUE ) );
        assertTrue( etdl.templateUserUnused.getLocked().booleanValue() );

        // verify that related items are locked also
        assertTrue( etdl.item6.getLocked().booleanValue() );

        // check that locked template gets unlocked (items)
        assertTrue( etdl.templateUserUnused.getLocked().booleanValue() );
        assertTrue( evaluationDao.lockTemplate( etdl.templateUserUnused, Boolean.FALSE ) );
        assertFalse( etdl.templateUserUnused.getLocked().booleanValue() );

        // verify that related items are unlocked also
        assertFalse( etdl.item6.getLocked().booleanValue() );

        // check unlocked template with locked items can be locked
        assertFalse( etdl.templateUnused.getLocked().booleanValue() );
        assertTrue( evaluationDao.lockTemplate( etdl.templateUnused, Boolean.TRUE ) );
        assertTrue( etdl.templateUnused.getLocked().booleanValue() );

        // check that locked template gets unlocked (items locked by another template)
        assertTrue( etdl.item3.getLocked().booleanValue() );
        assertTrue( etdl.item5.getLocked().booleanValue() );
        assertTrue( etdl.templateUnused.getLocked().booleanValue() );
        assertTrue( evaluationDao.lockTemplate( etdl.templateUnused, Boolean.FALSE ) );
        assertFalse( etdl.templateUnused.getLocked().booleanValue() );

        // verify that associated items locked by other template do not get unlocked
        assertTrue( etdl.item3.getLocked().booleanValue() );
        assertTrue( etdl.item5.getLocked().booleanValue() );

        // check that new template cannot be locked/unlocked
        try {
            evaluationDao.lockTemplate(
                    new EvalTemplate(new Date(), EvalTestDataLoad.ADMIN_USER_ID, 
                            EvalConstants.TEMPLATE_TYPE_STANDARD, "new template one", 
                            "description", EvalConstants.SHARING_PRIVATE, EvalTestDataLoad.NOT_EXPERT, 
                            "expert desc", null, EvalTestDataLoad.LOCKED, false),
                            Boolean.TRUE);
            fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#lockEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)}.
     */
    public void testLockEvaluation() {

        // check that unlocked evaluation gets locked
        assertFalse( etdl.templatePublicUnused.getLocked().booleanValue() );
        assertFalse( evalUnLocked.getLocked().booleanValue() );
        assertTrue( evaluationDao.lockEvaluation( evalUnLocked, true ) );
        assertTrue( evalUnLocked.getLocked().booleanValue() );

        // verify that associated template gets locked
        assertTrue( etdl.templatePublicUnused.getLocked().booleanValue() );

        // now unlock the evaluation
        assertTrue( evalUnLocked.getLocked().booleanValue() );
        assertTrue( evaluationDao.lockEvaluation( evalUnLocked, false ) );
        assertFalse( evalUnLocked.getLocked().booleanValue() );

        // verify that associated template gets unlocked
        assertFalse( etdl.templatePublicUnused.getLocked().booleanValue() );

        // check that new evaluation cannot be locked
        try {
            evaluationDao.lockEvaluation(
                    new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, EvalTestDataLoad.MAINT_USER_ID, "Eval new", null, 
                            etdl.tomorrow, etdl.threeDaysFuture, etdl.threeDaysFuture, etdl.fourDaysFuture, false, null,
                            false, null, 
                            EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_OPT_IN, new Integer(1), null, null, null, null,
                            etdl.templatePublic, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                            EvalTestDataLoad.UNLOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null, null),
                            true
            );
            fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            assertNotNull(e);
        }

    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#isUsedScale(java.lang.Long)}.
     */
    public void testIsUsedScale() {
        assertTrue( evaluationDao.isUsedScale( etdl.scale1.getId() ) );
        assertTrue( evaluationDao.isUsedScale( etdl.scale2.getId() ) );
        assertFalse( evaluationDao.isUsedScale( etdl.scale3.getId() ) );
        assertFalse( evaluationDao.isUsedScale( etdl.scale4.getId() ) );
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#isUsedItem(java.lang.Long)}.
     */
    public void testIsUsedItem() {
        assertTrue( evaluationDao.isUsedItem( etdl.item1.getId() ) );
        assertTrue( evaluationDao.isUsedItem( etdl.item2.getId() ) );
        assertTrue( evaluationDao.isUsedItem( etdl.item3.getId() ) );
        assertFalse( evaluationDao.isUsedItem( etdl.item4.getId() ) );
        assertTrue( evaluationDao.isUsedItem( etdl.item5.getId() ) );
        assertTrue( evaluationDao.isUsedItem( etdl.item6.getId() ) );
        assertFalse( evaluationDao.isUsedItem( etdl.item7.getId() ) );
        assertFalse( evaluationDao.isUsedItem( etdl.item8.getId() ) );
        assertTrue( evaluationDao.isUsedItem( etdl.item9.getId() ) );
        assertTrue( evaluationDao.isUsedItem( etdl.item10.getId() ) );
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#isUsedTemplate(java.lang.Long)}.
     */
    public void testIsUsedTemplate() {
        assertTrue( evaluationDao.isUsedTemplate( etdl.templateAdmin.getId() ) );
        assertFalse( evaluationDao.isUsedTemplate( etdl.templateAdminBlock.getId() ) );
        assertFalse( evaluationDao.isUsedTemplate( etdl.templateAdminComplex.getId() ) );
        assertFalse( evaluationDao.isUsedTemplate( etdl.templateAdminNoItems.getId() ) );
        assertTrue( evaluationDao.isUsedTemplate( etdl.templatePublic.getId() ) );
        assertTrue( evaluationDao.isUsedTemplate( etdl.templatePublicUnused.getId() ) ); // used in this file
        assertFalse( evaluationDao.isUsedTemplate( etdl.templateUnused.getId() ) );
        assertTrue( evaluationDao.isUsedTemplate( etdl.templateUser.getId() ) );
        assertFalse( evaluationDao.isUsedTemplate( etdl.templateUserUnused.getId() ) );
    }


    public void testObtainLock() {
        // check I can get a lock
        assertTrue( evaluationDao.obtainLock("AZ.my.lock", "AZ1", 100) );

        // check someone else cannot get my lock
        assertFalse( evaluationDao.obtainLock("AZ.my.lock", "AZ2", 100) );

        // check I can get my own lock again
        assertTrue( evaluationDao.obtainLock("AZ.my.lock", "AZ1", 100) );

        // allow the lock to expire
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // nothing here but a fail
            fail("sleep interrupted?");
        }

        // check someone else can get my lock
        assertTrue( evaluationDao.obtainLock("AZ.my.lock", "AZ2", 100) );

        // check invalid arguments cause failure
        try {
            evaluationDao.obtainLock("AZ.my.lock", null, 1000);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
        try {
            evaluationDao.obtainLock(null, "AZ1", 1000);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    public void testReleaseLock() {

        // check I can get a lock
        assertTrue( evaluationDao.obtainLock("AZ.R.lock", "AZ1", 1000) );

        // check someone else cannot get my lock
        assertFalse( evaluationDao.obtainLock("AZ.R.lock", "AZ2", 1000) );

        // check I can release my lock
        assertTrue( evaluationDao.releaseLock("AZ.R.lock", "AZ1") );

        // check someone else can get my lock now
        assertTrue( evaluationDao.obtainLock("AZ.R.lock", "AZ2", 1000) );

        // check I cannot get the lock anymore
        assertFalse( evaluationDao.obtainLock("AZ.R.lock", "AZ1", 1000) );

        // check they can release it
        assertTrue( evaluationDao.releaseLock("AZ.R.lock", "AZ2") );

        // check invalid arguments cause failure
        try {
            evaluationDao.releaseLock("AZ.R.lock", null);
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
        try {
            evaluationDao.releaseLock(null, "AZ1");
            fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    /**
     * Add anything that supports the unit tests below here
     */

}
