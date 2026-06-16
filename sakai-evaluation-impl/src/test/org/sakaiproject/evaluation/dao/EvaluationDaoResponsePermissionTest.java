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
 * DAO regression tests for EvaluationDaoResponsePermission.
 */
@Slf4j
public class EvaluationDaoResponsePermissionTest extends AbstractEvaluationDaoTest {

    @Test
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

        // should be only one items left in this template now
        Assert.assertNotNull( eti3.getTemplate().getTemplateItems() );
        Assert.assertEquals(1, eti3.getTemplate().getTemplateItems().size() );
        EvalTemplate template = (EvalTemplate) evaluationDao.findById(EvalTemplate.class, eti3.getTemplate().getId());
        Assert.assertNotNull( template );
        Assert.assertNotNull( template.getTemplateItems() );
        Assert.assertEquals(1, template.getTemplateItems().size() );

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#getTemplateItemsByTemplate(java.lang.Long, java.lang.String[], java.lang.String[], java.lang.String[])}.
     */
    @Test
    public void testGetTemplateItemsByTemplate() {
        List<EvalTemplateItem> l;
        List<Long> ids;

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

    @Test
    public void testGetResponseIds() {
        List<Long> l;

        l = evaluationDao.getResponseIds(etdl.evaluationClosed.getId(), null, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertTrue( l.contains(etdl.response2.getId()) );
        Assert.assertTrue( l.contains(etdl.response3.getId()) );
        Assert.assertTrue( l.contains(etdl.response6.getId()) );

        l = evaluationDao.getResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        Assert.assertTrue( l.contains(etdl.response2.getId()) );
        Assert.assertTrue( l.contains(etdl.response3.getId()) );
        Assert.assertTrue( l.contains(etdl.response6.getId()) );

        l = evaluationDao.getResponseIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue( l.contains(etdl.response2.getId()) );

        // test invalid evalid
        l = evaluationDao.getResponseIds(EvalTestDataLoad.INVALID_LONG_ID, null, null, null);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

    }

    @Test
    public void testRemoveResponses() {
        // check that response and answer are removed correctly
        int curR = evaluationDao.countAll(EvalResponse.class);
        int curA = evaluationDao.countAll(EvalAnswer.class);
        evaluationDao.removeResponses(new Long[] {etdl.response1.getId()});
        int remainR = evaluationDao.countAll(EvalResponse.class);
        int remainA = evaluationDao.countAll(EvalResponse.class);
        Assert.assertTrue(remainR < curR);
        Assert.assertTrue(remainA < curA);
        // stupid hibernate is making this test a pain -AZ
        //      Assert.assertNull( evaluationDao.findById(EvalResponse.class, etdl.response1.getId()) );
        //      Assert.assertNull( evaluationDao.findById(EvalAnswer.class, etdl.answer1_1.getId()) );

    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#getEvalCategories(String)}
     */
    @Test
    public void testGetEvalCategories() {
        List<String> l;

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

        List<EvalEvaluation> evals = evaluationDao.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_2);
        Assert.assertNotNull(evals);
        Assert.assertFalse(evals.isEmpty());
        List<Long> ids = EvalTestDataLoad.makeIdList(evals);
        Assert.assertTrue(ids.contains(etdl.evaluationClosed.getId()));
        Assert.assertFalse(ids.contains(etdl.evaluationViewable.getId()));
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#getNodeIdForEvalGroup(java.lang.String)}.
     */
    @Test
    public void testGetNodeIdForEvalGroup() {
        String nodeId; 

        nodeId = evaluationDao.getNodeIdForEvalGroup(EvalTestDataLoad.SITE1_REF);
        Assert.assertNotNull(nodeId);
        Assert.assertEquals(EvalTestDataLoad.NODE_ID1, nodeId);

        nodeId = evaluationDao.getNodeIdForEvalGroup(EvalTestDataLoad.SITE2_REF);
        Assert.assertNotNull(nodeId);
        Assert.assertEquals(EvalTestDataLoad.NODE_ID1, nodeId);

        nodeId = evaluationDao.getNodeIdForEvalGroup(EvalTestDataLoad.SITE3_REF);
        Assert.assertNotNull(nodeId);
        Assert.assertEquals(EvalTestDataLoad.NODE_ID2, nodeId);

        nodeId = evaluationDao.getNodeIdForEvalGroup("xxxxxxxxxxxxxxxxx");
        Assert.assertNull(nodeId);
    }

    @Test
    public void testGetTemplateItemsByEvaluation() {
        List<EvalTemplateItem> templateItems;

        templateItems = evaluationDao.getTemplateItemsByEvaluation(etdl.evaluationActive.getId(), null, null, null);
        Assert.assertNotNull(templateItems);
        Assert.assertEquals(2, templateItems.size());

        templateItems = evaluationDao.getTemplateItemsByEvaluation(etdl.evaluationClosed.getId(), null, null, null);
        Assert.assertNotNull(templateItems);
        Assert.assertEquals(3, templateItems.size());

        try {
            evaluationDao.getTemplateItemsByEvaluation(EvalTestDataLoad.INVALID_LONG_ID, null, null, null);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }

    //   public void testGetTemplateIdsForEvaluation() {
    //      List<Long> templateIds = null;
    //
    //      templateIds = evaluationDao.getTemplateIdForEvaluation(etdl.evaluationActive.getId());
    //      Assert.assertNotNull(templateIds);
    //      Assert.assertEquals(1, templateIds.size());
    //      Assert.assertTrue( templateIds.contains( etdl.templateUser.getId() ) );
    //
    //      templateIds = evaluationDao.getTemplateIdForEvaluation(etdl.evaluationClosed.getId());
    //      Assert.assertNotNull(templateIds);
    //      Assert.assertEquals(2, templateIds.size());
    //      Assert.assertTrue( templateIds.contains( etdl.templateAdmin.getId() ) );
    //      Assert.assertTrue( templateIds.contains( etdl.templateAdminComplex.getId() ) );
    //
    //      templateIds = evaluationDao.getTemplateIdForEvaluation(EvalTestDataLoad.INVALID_LONG_ID);
    //      Assert.assertNotNull(templateIds);
    //      Assert.assertEquals(0, templateIds.size());
    //   }

    @Test
    public void testGetResponseUserIds() {
        Set<String> userIds;

        // check getting responders from complete evaluation
        userIds = evaluationDao.getResponseUserIds(etdl.evaluationClosed.getId(), null, true);
        Assert.assertNotNull(userIds);
        Assert.assertEquals(2, userIds.size());
        Assert.assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));
        Assert.assertTrue(userIds.contains(EvalTestDataLoad.STUDENT_USER_ID));

        // check getting incomplete responders from complete evaluation
        userIds = evaluationDao.getResponseUserIds(etdl.evaluationClosed.getId(), null, false);
        Assert.assertNotNull(userIds);
        Assert.assertEquals(0, userIds.size());

        // check getting all responders from complete evaluation
        userIds = evaluationDao.getResponseUserIds(etdl.evaluationClosed.getId(), null, null);
        Assert.assertNotNull(userIds);
        Assert.assertEquals(2, userIds.size());
        Assert.assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));
        Assert.assertTrue(userIds.contains(EvalTestDataLoad.STUDENT_USER_ID));

        // test getting from subset of the groups
        userIds = evaluationDao.getResponseUserIds(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, true);
        Assert.assertNotNull(userIds);
        Assert.assertEquals(1, userIds.size());
        Assert.assertTrue(userIds.contains(EvalTestDataLoad.USER_ID));

        // test getting none
        userIds = evaluationDao.getResponseUserIds(etdl.evaluationActiveUntaken.getId(), null, true);
        Assert.assertNotNull(userIds);
        Assert.assertEquals(0, userIds.size());

        // test using invalid group ids retrieves no results
        userIds = evaluationDao.getResponseUserIds(etdl.evaluationClosed.getId(), new String[] {"xxxxxx", "fakeyandnotreal"}, true);
        Assert.assertNotNull(userIds);
        Assert.assertEquals(0, userIds.size());

    }

    @Test
    public void testGetViewableEvalGroupIds() {
        Set<String> evalGroupIds;

        // check for groups that are fully enabled
        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationClosed.getId(), EvalAssignUser.TYPE_EVALUATEE, null);
        Assert.assertNotNull(evalGroupIds);
        Assert.assertEquals(1, evalGroupIds.size());
        Assert.assertTrue(evalGroupIds.contains(etdl.assign3.getEvalGroupId()));

        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationClosed.getId(), EvalAssignUser.TYPE_EVALUATOR, null);
        Assert.assertNotNull(evalGroupIds);
        Assert.assertEquals(1, evalGroupIds.size());
        Assert.assertTrue(evalGroupIds.contains(etdl.assign4.getEvalGroupId()));

        // check for mixture - not in the test data
        //        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationNewAdmin.getId(), EvalAssignUser.TYPE_EVALUATEE, null);
        //        Assert.assertNotNull(evalGroupIds);
        //        Assert.assertEquals(2, evalGroupIds.size());
        //        Assert.assertTrue(evalGroupIds.contains(etdl.assign7.getEvalGroupId()));
        //        Assert.assertTrue(evalGroupIds.contains(etdl.assignGroupProvided.getEvalGroupId()));

        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationNewAdmin.getId(), EvalAssignUser.TYPE_EVALUATOR, null);
        Assert.assertNotNull(evalGroupIds);
        Assert.assertEquals(1, evalGroupIds.size());
        Assert.assertTrue(evalGroupIds.contains(etdl.assign6.getEvalGroupId()));

        // check for unassigned to return none
        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationNew.getId(), EvalAssignUser.TYPE_EVALUATEE, null);
        Assert.assertNotNull(evalGroupIds);
        Assert.assertEquals(0, evalGroupIds.size());

        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationNew.getId(), EvalAssignUser.TYPE_EVALUATOR, null);
        Assert.assertNotNull(evalGroupIds);
        Assert.assertEquals(0, evalGroupIds.size());

        // check that other perms return nothing
        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationNewAdmin.getId(), EvalAssignUser.TYPE_ASSISTANT, null);
        Assert.assertNotNull(evalGroupIds);
        Assert.assertEquals(0, evalGroupIds.size());

        // check for limits on the returns
        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationClosed.getId(), EvalAssignUser.TYPE_EVALUATEE, 
                new String[] {etdl.assign3.getEvalGroupId()});
        Assert.assertNotNull(evalGroupIds);
        Assert.assertEquals(1, evalGroupIds.size());
        Assert.assertTrue(evalGroupIds.contains(etdl.assign3.getEvalGroupId()));

        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationNewAdmin.getId(), EvalAssignUser.TYPE_EVALUATEE, 
                new String[] {etdl.assign7.getEvalGroupId()});
        Assert.assertNotNull(evalGroupIds);
        Assert.assertEquals(1, evalGroupIds.size());
        Assert.assertTrue(evalGroupIds.contains(etdl.assign7.getEvalGroupId()));

        // check for limits on the returns which limit it to none
        evalGroupIds = evaluationDao.getViewableEvalGroupIds(etdl.evaluationClosed.getId(), EvalAssignUser.TYPE_EVALUATEE, 
                new String[] {EvalTestDataLoad.INVALID_CONSTANT_STRING});
        Assert.assertNotNull(evalGroupIds);
        Assert.assertEquals(0, evalGroupIds.size());

        // check for null evaluation id
        try {
            evaluationDao.getViewableEvalGroupIds(null, EvalConstants.PERM_ASSIGN_EVALUATION, null);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

    }

    @Test
    public void testGetEvalAdhocGroupsByUserAndPerm() {
        List<EvalAdhocGroup> l;
        List<Long> ids;

        // make sure the group has the user
        EvalAdhocGroup checkGroup = (EvalAdhocGroup) evaluationDao.findById(EvalAdhocGroup.class, etdl.group2.getId());
        Assert.assertTrue( checkGroup.getParticipantIds().contains( etdl.user3.getUserId()) );

        l = evaluationDao.getEvalAdhocGroupsByUserAndPerm(etdl.user3.getUserId(), EvalConstants.PERM_TAKE_EVALUATION);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(etdl.group2.getId(), l.get(0).getId());

        l = evaluationDao.getEvalAdhocGroupsByUserAndPerm(EvalTestDataLoad.STUDENT_USER_ID, EvalConstants.PERM_TAKE_EVALUATION);
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(etdl.group1.getId(), l.get(0).getId());

        l = evaluationDao.getEvalAdhocGroupsByUserAndPerm(etdl.user1.getUserId(), EvalConstants.PERM_TAKE_EVALUATION);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains(etdl.group1.getId()));
        Assert.assertTrue(ids.contains(etdl.group2.getId()));

        l = evaluationDao.getEvalAdhocGroupsByUserAndPerm(etdl.user2.getUserId(), EvalConstants.PERM_TAKE_EVALUATION);
        Assert.assertNotNull(l);
        Assert.assertEquals(0, l.size());

    }

    @Test
    public void testIsUserAllowedInAdhocGroup() {
        boolean allowed;

        allowed = evaluationDao.isUserAllowedInAdhocGroup(EvalTestDataLoad.USER_ID, EvalConstants.PERM_TAKE_EVALUATION, etdl.group2.getEvalGroupId());
        Assert.assertTrue(allowed);

        allowed = evaluationDao.isUserAllowedInAdhocGroup(EvalTestDataLoad.USER_ID, EvalConstants.PERM_BE_EVALUATED, etdl.group2.getEvalGroupId());
        Assert.assertFalse(allowed);

        allowed = evaluationDao.isUserAllowedInAdhocGroup(etdl.user1.getUserId(), EvalConstants.PERM_TAKE_EVALUATION, etdl.group1.getEvalGroupId());
        Assert.assertTrue(allowed);

        allowed = evaluationDao.isUserAllowedInAdhocGroup(etdl.user1.getUserId(), EvalConstants.PERM_BE_EVALUATED, etdl.group1.getEvalGroupId());
        Assert.assertFalse(allowed);

        allowed = evaluationDao.isUserAllowedInAdhocGroup(etdl.user2.getUserId(), EvalConstants.PERM_TAKE_EVALUATION, etdl.group1.getEvalGroupId());
        Assert.assertFalse(allowed);

        allowed = evaluationDao.isUserAllowedInAdhocGroup(etdl.user2.getUserId(), EvalConstants.PERM_BE_EVALUATED, etdl.group1.getEvalGroupId());
        Assert.assertFalse(allowed);
    }




    // LOCKING tests

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#lockScale(org.sakaiproject.evaluation.model.EvalScale, java.lang.Boolean)}.
     */
}
