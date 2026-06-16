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
package org.sakaiproject.evaluation.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.model.EvalAdmin;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalConfig;
import org.sakaiproject.evaluation.model.EvalEmailProcessingData;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalGroupNodes;
import org.sakaiproject.evaluation.model.EvalHierarchyRule;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.PreloadTestDataImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import lombok.extern.slf4j.Slf4j;


/**
 * DAO regression tests for evaluation query methods.
 */
@Slf4j
public class EvaluationDaoQueryMethodsTest extends AbstractEvaluationDaoTest {

    @Test
    public void testGetParticipants() {
        List<EvalAssignUser> l;
        long start;

        // more testing at the higher level

        // get all participants for an evaluation
        start = System.currentTimeMillis();
        l = evaluationDao.getParticipantsForEval(etdl.evaluationActive.getId(), null, null, 
                null, null, null, null);
        log.debug("Query executed in " + (System.currentTimeMillis()-start) + " ms");
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());

        // limit groups
        start = System.currentTimeMillis();
        l = evaluationDao.getParticipantsForEval(etdl.evaluationActive.getId(), null, new String[] {EvalTestDataLoad.SITE1_REF}, 
                null, null, null, null);
        log.debug("Query executed in " + (System.currentTimeMillis()-start) + " ms");
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActive.getId(), null, new String[] {EvalTestDataLoad.SITE2_REF}, 
                null, null, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // get everyone who can take an evaluation
        start = System.currentTimeMillis();
        l = evaluationDao.getParticipantsForEval(etdl.evaluationActive.getId(), null, null, 
                EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        log.debug("Query executed in " + (System.currentTimeMillis()-start) + " ms");
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());

        // get all the evals a user is assigned to
        start = System.currentTimeMillis();
        l = evaluationDao.getParticipantsForEval(null, EvalTestDataLoad.USER_ID, null, 
                EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        log.debug("Query executed in " + (System.currentTimeMillis()-start) + " ms");
        Assert.assertNotNull(l);
        Assert.assertEquals(11, l.size());

        // get all active evals a user is assigned to
        l = evaluationDao.getParticipantsForEval(null, EvalTestDataLoad.USER_ID, null, 
                EvalAssignUser.TYPE_EVALUATOR, null, null, EvalConstants.EVALUATION_STATE_ACTIVE);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());

        // test the way that the reminders email gets participants
        //evaluationService.getParticipantsForEval(evaluationId, null, limitGroupIds, null, null, includeConstant, null);
        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_NONTAKERS, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_IN_PROGRESS, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActive.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_NONTAKERS, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActive.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_IN_PROGRESS, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());


        // test fetching various sets of participants (using the untaken eval)
        // first it has to have 3 users assigned to it so we assign 2 more (EvalTestDataLoad.USER_ID is already assigned)
        evaluationDao.save( new EvalAssignUser(EvalTestDataLoad.USER_ID_4, etdl.evaluationActiveUntaken, EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.MAINT_USER_ID) );
        evaluationDao.save( new EvalAssignUser(EvalTestDataLoad.USER_ID_5, etdl.evaluationActiveUntaken, EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.MAINT_USER_ID) );

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_ALL, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_NONTAKERS, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_IN_PROGRESS, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_RESPONDENTS, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // add in a saved response
        EvalResponse r1 = new EvalResponse(EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE2_REF, etdl.evaluationActiveUntaken, new Date(), null, null);
        r1.setAnswers( new HashSet<>() );
        evaluationDao.save(r1);

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_ALL, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_NONTAKERS, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_IN_PROGRESS, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_RESPONDENTS, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // add in a completed response
        EvalResponse r2 = new EvalResponse(EvalTestDataLoad.USER_ID_4, EvalTestDataLoad.SITE2_REF, etdl.evaluationActiveUntaken, etdl.yesterday, new Date(), null);
        r2.setAnswers( new HashSet<>() );
        evaluationDao.save(r2);

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_ALL, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_NONTAKERS, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_IN_PROGRESS, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());

        l = evaluationDao.getParticipantsForEval(etdl.evaluationActiveUntaken.getId(), null, null, 
                null, null, EvalConstants.EVAL_INCLUDE_RESPONDENTS, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());

    }

    @Test
    public void testGetEvalsUserCanTake() {
        // get ones we can take
        List<EvalEvaluation> evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.USER_ID, true, true, false, 0, 0);
        Assert.assertNotNull(evals);
        Assert.assertEquals(1, evals.size());
        Assert.assertEquals(etdl.evaluationActive.getId(), evals.get(0).getId());

        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.STUDENT_USER_ID, true, true, false, 0, 0);
        Assert.assertNotNull(evals);
        Assert.assertEquals(0, evals.size());

        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.MAINT_USER_ID, true, true, false, 0, 0);
        Assert.assertNotNull(evals);
        Assert.assertEquals(0, evals.size());

        // admin normally takes none
        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.ADMIN_USER_ID, true, true, false, 0, 0);
        Assert.assertNotNull(evals);
        Assert.assertEquals(0, evals.size());

        // include anonymous
        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.USER_ID, true, true, null, 0, 0);
        Assert.assertNotNull(evals);
        Assert.assertEquals(2, evals.size());
        Assert.assertEquals(etdl.evaluationActive.getId(), evals.get(0).getId());
        Assert.assertEquals(etdl.evaluationActiveUntaken.getId(), evals.get(1).getId());

        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.STUDENT_USER_ID, true, true, null, 0, 0);
        Assert.assertNotNull(evals);
        Assert.assertEquals(1, evals.size());
        Assert.assertEquals(etdl.evaluationActiveUntaken.getId(), evals.get(0).getId());

        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.MAINT_USER_ID, true, true, null, 0, 0);
        Assert.assertNotNull(evals);
        Assert.assertEquals(1, evals.size());
        Assert.assertEquals(etdl.evaluationActiveUntaken.getId(), evals.get(0).getId());

        // TODO add assign groups support
        /**
        // testing instructor approval
        EvalAssignGroup eag = (EvalAssignGroup) evaluationDao.findById(EvalAssignGroup.class, etdl.assign1.getId());
        eag.setInstructorApproval(false); // make evaluationActive unapproved
        evaluationDao.save(eag);

        // get ones we can take
        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.USER_ID, true, true, false, 0, 0);
        Assert.assertNotNull(evals);
        Assert.assertEquals(0, evals.size());

        // include anonymous
        evals = evaluationDao.getEvalsUserCanTake(EvalTestDataLoad.USER_ID, true, true, null, 0, 0);
        Assert.assertNotNull(evals);
        Assert.assertEquals(1, evals.size());
        Assert.assertEquals(etdl.evaluationActiveUntaken.getId(), evals.get(0).getId());
         **/        
    }

    @Test
    public void testGetEvalsWithoutUserAssignments() {
        List<EvalEvaluation> evals = evaluationDao.getEvalsWithoutUserAssignments();
        Assert.assertNotNull(evals);
        Assert.assertTrue(evals.size() > 0);
    }

    @Test
    public void testGetTemplatesForUserSharingVariants() {
        List<EvalTemplate> l;
        List<Long> ids;

        String[] privateAndPublic = new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC};
        String[] privateOnly = new String[] {EvalConstants.SHARING_PRIVATE};
        String[] publicOnly = new String[] {EvalConstants.SHARING_PUBLIC};
        String[] allSharing = new String[] {
                EvalConstants.SHARING_PRIVATE,
                EvalConstants.SHARING_PUBLIC,
                EvalConstants.SHARING_SHARED,
                EvalConstants.SHARING_VISIBLE
        };

        // all templates visible to user
        l = evaluationDao.getTemplatesForUser(EvalTestDataLoad.USER_ID, privateAndPublic, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
        Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

        // all templates visible to maint user
        l = evaluationDao.getTemplatesForUser(EvalTestDataLoad.MAINT_USER_ID, privateAndPublic, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(4, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
        Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

        // all templates owned by USER
        l = evaluationDao.getTemplatesForUser(EvalTestDataLoad.USER_ID, privateOnly, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

        // all private templates
        l = evaluationDao.getTemplatesForUser(null, privateOnly, true);
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

        // all private non-empty templates
        l = evaluationDao.getTemplatesForUser(null, privateOnly, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.templateAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));

        // all public templates
        l = evaluationDao.getTemplatesForUser(null, publicOnly, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
        Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

        // all templates (admin would use this)
        l = evaluationDao.getTemplatesForUser(null, allSharing, true);
        Assert.assertNotNull(l);
        Assert.assertEquals(11, l.size());

        // all non-empty templates (admin would use this)
        l = evaluationDao.getTemplatesForUser(null, allSharing, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(8, l.size());

        // no templates (no one should do this, it throws an exception)
        try {
            evaluationDao.getTemplatesForUser(null, new String[] {}, true);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testCountTemplatesForUserSharingVariants() {
        int count;

        String[] privateAndPublic = new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC};
        String[] privateOnly = new String[] {EvalConstants.SHARING_PRIVATE};
        String[] publicOnly = new String[] {EvalConstants.SHARING_PUBLIC};
        String[] allSharing = new String[] {
                EvalConstants.SHARING_PRIVATE,
                EvalConstants.SHARING_PUBLIC,
                EvalConstants.SHARING_SHARED,
                EvalConstants.SHARING_VISIBLE
        };

        // all templates visible to user
        count = evaluationDao.countTemplatesForUser(EvalTestDataLoad.USER_ID, privateAndPublic, true);
        Assert.assertEquals(5, count);

        // all templates visible to maint user
        count = evaluationDao.countTemplatesForUser(EvalTestDataLoad.MAINT_USER_ID, privateAndPublic, true);
        Assert.assertEquals(4, count);

        // all templates owned by USER
        count = evaluationDao.countTemplatesForUser(EvalTestDataLoad.USER_ID, privateOnly, true);
        Assert.assertEquals(2, count);

        // all private templates (admin only)
        count = evaluationDao.countTemplatesForUser(null, privateOnly, true);
        Assert.assertEquals(8, count);

        // all private non-empty templates (admin only)
        count = evaluationDao.countTemplatesForUser(null, privateOnly, false);
        Assert.assertEquals(5, count);

        // all public templates
        count = evaluationDao.countTemplatesForUser(null, publicOnly, true);
        Assert.assertEquals(3, count);

        // all templates (admin would use this)
        count = evaluationDao.countTemplatesForUser(null, allSharing, true);
        Assert.assertEquals(11, count);

        // all non-empty templates (admin would use this)
        count = evaluationDao.countTemplatesForUser(null, allSharing, false);
        Assert.assertEquals(8, count);
    }

    @Test
    public void testGetEvaluationsByEvalGroups() {
        List<EvalEvaluation> l;
        List<Long> ids;

        // testing instructor approval false
        EvalAssignGroup eag = (EvalAssignGroup) evaluationDao.findById(EvalAssignGroup.class, etdl.assign5.getId());
        eag.setInstructorApproval(false);
        evaluationDao.save(eag);

        // test getting all assigned evaluations for 2 sites
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, null, null, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(7, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

        // test getting all assigned (minus anonymous) evaluations for 2 sites
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, null, false, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

        // test getting assigned evaluations by one evalGroupId
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF}, null, null, null, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(6, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));

        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE2_REF}, null, null, null, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(! ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

        // test getting by groupId and including anons (should not get any deleted or partial evals)
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF}, null, null, true, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(6, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));

        // test that the get active part works
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF}, true, null, null, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationGracePeriod.getId() ));

        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE2_REF}, true, null, null, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // active minus anon
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF}, true, null, false, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));

        // test that the get active plus anon works
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE2_REF}, true, null, true, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationGracePeriod.getId() ));

        // test getting from an invalid evalGroupId
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.INVALID_CONTEXT}, null, null, null, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());		

        // test getting all anonymous evals
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {}, null, null, true, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());		
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

        // testing getting no evals
        l = evaluationDao.getEvaluationsByEvalGroups(null, null, null, false, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // test unapproved assigned evaluations
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF}, null, false, null, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));

        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, false, null, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

        // test getting all APPROVED assigned evaluations
        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF}, null, true, null, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));

        l = evaluationDao.getEvaluationsByEvalGroups(
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, true, null, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(6, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));

        //      // test getting taken evals only
        //      l = evaluationDao.getEvaluationsByEvalGroups(
        //            new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, null, null, true, EvalTestDataLoad.USER_ID, 0, 0);
        //      Assert.assertNotNull(l);
        //      Assert.assertEquals(3, l.size());
        //      ids = EvalTestDataLoad.makeIdList(l);
        //      Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        //      Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        //      Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));
        //
        //      l = evaluationDao.getEvaluationsByEvalGroups(
        //            new String[] {EvalTestDataLoad.SITE1_REF}, null, null, null, true, EvalTestDataLoad.USER_ID, 0, 0);
        //      Assert.assertNotNull(l);
        //      Assert.assertEquals(2, l.size());
        //      ids = EvalTestDataLoad.makeIdList(l);
        //      Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        //      Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        //
        //      l = evaluationDao.getEvaluationsByEvalGroups(
        //            new String[] {EvalTestDataLoad.SITE2_REF}, null, null, null, true, EvalTestDataLoad.USER_ID, 0, 0);
        //      Assert.assertNotNull(l);
        //      Assert.assertEquals(2, l.size());
        //      ids = EvalTestDataLoad.makeIdList(l);
        //      Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        //      Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));
        //
        //      // test getting untaken evals only
        //      l = evaluationDao.getEvaluationsByEvalGroups(
        //            new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, null, null, false, EvalTestDataLoad.USER_ID, 0, 0);
        //      Assert.assertNotNull(l);
        //      Assert.assertEquals(3, l.size());
        //      ids = EvalTestDataLoad.makeIdList(l);
        //      Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        //      Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        //      Assert.assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));
        //
        //      l = evaluationDao.getEvaluationsByEvalGroups(
        //            new String[] {EvalTestDataLoad.SITE2_REF}, null, null, null, false, EvalTestDataLoad.USER_ID, 0, 0);
        //      Assert.assertNotNull(l);
        //      Assert.assertEquals(4, l.size());
        //      ids = EvalTestDataLoad.makeIdList(l);
        //      Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        //      Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        //      Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        //      Assert.assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));

    }

    @Test
    public void testGetEvaluationsForOwnerAndGroups() {
        List<EvalEvaluation> l;
        List<Long> ids;

        // test getting all evals
        l = evaluationDao.getEvaluationsForOwnerAndGroups(null, null, null, 0, 0, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(19, l.size());
        // check the order
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertEquals(ids.get(0), etdl.evaluationViewable.getId());
        Assert.assertEquals(ids.get(1), etdl.evaluationClosed_viewIgnoreDates.getId());
        Assert.assertEquals(ids.get(2), etdl.evaluationClosed.getId());
        Assert.assertEquals(ids.get(3), etdl.evaluationClosedUntaken.getId());
        Assert.assertEquals(ids.get(4), etdl.evaluationGracePeriod.getId());
        Assert.assertEquals(ids.get(5), etdl.evaluationDue_viewIgnoreDates.getId());
        Assert.assertEquals(ids.get(6), etdl.evaluationActive.getId());
        Assert.assertEquals(ids.get(7), etdl.evaluationProvided.getId());
        Assert.assertEquals(ids.get(8), etdl.evaluationActiveUntaken.getId());
        Assert.assertEquals(ids.get(9), evalUnLocked.getId());
        Assert.assertEquals(ids.get(10), etdl.evaluationActive_viewIgnoreDates.getId());
        Assert.assertEquals(ids.get(11), etdl.evaluationNewAdmin.getId());
        Assert.assertEquals(ids.get(12), etdl.evaluationNew.getId());
        Assert.assertEquals(ids.get(13), etdl.evaluation_allRoleAssignments_allRolesParticipate.getId());
        Assert.assertEquals(ids.get(14), etdl.evaluation_allRoleAssignments_notAllRolesParticipate.getId());
        Assert.assertEquals(ids.get(15), etdl.evaluation_noAssignments_allRolesParticipate.getId());
        Assert.assertEquals(ids.get(16), etdl.evaluation_noAssignments_notAllRolesParticipate.getId());
        Assert.assertEquals(ids.get(17), etdl.evaluation_simpleAssignments_allRolesParticipate.getId());
        Assert.assertEquals(ids.get(18), etdl.evaluation_simpleAssignments_notAllRolesParticipate.getId());

        // test getting all evals with limit
        l = evaluationDao.getEvaluationsForOwnerAndGroups(null, null, null, 0, 4, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(4, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        // check order and return values
        Assert.assertEquals(ids.get(0), etdl.evaluationViewable.getId() );
        Assert.assertEquals(ids.get(1), etdl.evaluationClosed_viewIgnoreDates.getId() );
        Assert.assertEquals(ids.get(2), etdl.evaluationClosed.getId() );
        Assert.assertEquals(ids.get(3), etdl.evaluationClosedUntaken.getId() );

        l = evaluationDao.getEvaluationsForOwnerAndGroups(null, null, null, 3, 5, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        // check order and return values
        Assert.assertEquals(ids.get(0), etdl.evaluationClosedUntaken.getId() );
        Assert.assertEquals(ids.get(1), etdl.evaluationGracePeriod.getId() );
        Assert.assertEquals(ids.get(2), etdl.evaluationDue_viewIgnoreDates.getId() );
        Assert.assertEquals(ids.get(3), etdl.evaluationActive.getId() );
        Assert.assertEquals(ids.get(4), etdl.evaluationProvided.getId() );

        // test filtering by owner
        l = evaluationDao.getEvaluationsForOwnerAndGroups(EvalTestDataLoad.ADMIN_USER_ID, null, null, 0, 0, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationGracePeriod.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

        l = evaluationDao.getEvaluationsForOwnerAndGroups(EvalTestDataLoad.USER_ID, null, null, 0, 0, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());
        EvalTestDataLoad.makeIdList(l);

        // test filtering by groups
        l = evaluationDao.getEvaluationsForOwnerAndGroups(null, 
                new String[] {EvalTestDataLoad.SITE1_REF}, null, 0, 0, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(6, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationGracePeriod.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));

        // test filtering by owner and groups
        l = evaluationDao.getEvaluationsForOwnerAndGroups(EvalTestDataLoad.ADMIN_USER_ID, 
                new String[] {EvalTestDataLoad.SITE1_REF}, null, 0, 0, false);
        Assert.assertNotNull(l);
        Assert.assertEquals(7, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationGracePeriod.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationViewable.getId() ));

    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#getAnswers(java.lang.Long, java.lang.Long)}.
     */
    @Test
    public void testGetAnswers() {
        Set<EvalAnswer> s;
        List<EvalAnswer> l;
        List<Long> ids;

        s = etdl.response2.getAnswers();
        Assert.assertNotNull(s);
        Assert.assertEquals(2, s.size());
        ids = EvalTestDataLoad.makeIdList(s);
        Assert.assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        Assert.assertTrue(ids.contains( etdl.answer2_5A.getId() ));

        // test getting all answers first
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        Assert.assertTrue(ids.contains( etdl.answer2_5A.getId() ));
        Assert.assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // restrict to template item
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), null, new Long[] {etdl.templateItem2A.getId()});
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        Assert.assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // restrict to multiple template items
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), null, new Long[] {etdl.templateItem2A.getId(), etdl.templateItem5A.getId()});
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        Assert.assertTrue(ids.contains( etdl.answer2_5A.getId() ));
        Assert.assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // test restricting to groups
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer2_2A.getId() ));
        Assert.assertTrue(ids.contains( etdl.answer2_5A.getId() ));

        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // test restricting to groups and TIs
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, new Long[] {etdl.templateItem2A.getId()});
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer2_2A.getId() ));

        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, new Long[] {etdl.templateItem2A.getId()});
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.answer3_2A.getId() ));

        // test restricting to answers not in this group
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, new Long[] {etdl.templateItem5A.getId()});
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // test template item that is not in this evaluation
        l = evaluationDao.getAnswers(etdl.evaluationClosed.getId(), null, new Long[] {etdl.templateItem1U.getId()});
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

        // test invalid eval id returns nothing
        l = evaluationDao.getAnswers(EvalTestDataLoad.INVALID_LONG_ID, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#removeTemplateItems(org.sakaiproject.evaluation.model.EvalTemplateItem[])}.
     */
}
