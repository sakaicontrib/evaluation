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

import org.sakaiproject.evaluation.dao.EvaluationAdminSupportDao;
import org.sakaiproject.evaluation.dao.EvaluationAssignmentDao;
import org.sakaiproject.evaluation.dao.EvaluationAuthoringDao;
import org.sakaiproject.evaluation.dao.EvaluationConsolidatedEmailDao;
import org.sakaiproject.evaluation.dao.EvaluationEmailTemplateDao;
import org.sakaiproject.evaluation.dao.EvaluationDaoBase;
import org.sakaiproject.evaluation.dao.EvaluationQueryDao;
import org.sakaiproject.evaluation.dao.EvaluationResponseDao;
import org.sakaiproject.evaluation.dao.EvaluationSettingsDao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
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
 * DAO regression tests for evaluation DAO port methods.
 */
@Slf4j
public class EvaluationDaoPortMethodsAssignmentTest extends AbstractEvaluationDaoTest {

    @Test
    public void testEvaluationAndAssignmentLookups() {
        Assert.assertEquals(1, queryDao.countEvaluationById(etdl.evaluationActive.getId()));
        Assert.assertEquals(0, queryDao.countEvaluationById(EvalTestDataLoad.INVALID_LONG_ID));

        EvalEvaluation foundEvaluation = queryDao.getEvaluationByEid(etdl.evaluationProvided.getEid());
        Assert.assertNotNull(foundEvaluation);
        Assert.assertEquals(etdl.evaluationProvided.getId(), foundEvaluation.getId());
        Assert.assertNull(queryDao.getEvaluationByEid(EvalTestDataLoad.INVALID_STRING_EID));

        Assert.assertEquals(1, authoringDao.countTemplateById(etdl.templatePublic.getId()));
        Assert.assertEquals(0, authoringDao.countTemplateById(EvalTestDataLoad.INVALID_LONG_ID));
        Assert.assertEquals(2, queryDao.countEvaluationsByTemplateId(etdl.templatePublic.getId()));

        List<EvalEvaluation> evaluations = queryDao.getEvaluationsByTemplateId(etdl.templateUser.getId());
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(3, evaluations.size());
        List<Long> ids = EvalTestDataLoad.makeIdList(evaluations);
        Assert.assertTrue(ids.contains(etdl.evaluationActive.getId()));
        Assert.assertTrue(ids.contains(etdl.evaluationViewable.getId()));
        Assert.assertTrue(ids.contains(etdl.evaluationProvided.getId()));

        String termId = "dao-term-lookup";
        evalUnLocked.setTermId(termId);
        persistence.update(evalUnLocked);
        evaluations = queryDao.getEvaluationsByTermId(termId);
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(1, evaluations.size());
        Assert.assertEquals(evalUnLocked.getId(), evaluations.get(0).getId());

        evaluations = queryDao.getEvaluationsByState(EvalConstants.EVALUATION_STATE_ACTIVE);
        Assert.assertNotNull(evaluations);
        ids = EvalTestDataLoad.makeIdList(evaluations);
        Assert.assertTrue(ids.contains(etdl.evaluationActive.getId()));

        evaluations = queryDao.getEvaluationsNotViewableOrDeleted();
        Assert.assertNotNull(evaluations);
        ids = EvalTestDataLoad.makeIdList(evaluations);
        Assert.assertTrue(ids.contains(etdl.evaluationActive.getId()));
        Assert.assertTrue(ids.contains(etdl.evaluationClosed.getId()));
        Assert.assertFalse(ids.contains(etdl.evaluationViewable.getId()));
        Assert.assertFalse(ids.contains(etdl.evaluationDeleted.getId()));

        int evalTitleCount = authoringDao.countEvaluationsByTitle("%Eval%");
        Assert.assertTrue(evalTitleCount >= 11);
        evaluations = authoringDao.getEvaluationsByTitle("%Eval%", "title", 0, 5);
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(5, evaluations.size());
        evaluations = authoringDao.getEvaluationsByTitle("%Eval%", "title", 8, 5);
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(Math.min(5, evalTitleCount - 8), evaluations.size());

        EvalAssignUser assignUser = new EvalAssignUser(
                "dao-assign-user-eid-user",
                etdl.evaluationActiveUntaken,
                EvalTestDataLoad.SITE1_REF,
                EvalTestDataLoad.MAINT_USER_ID);
        assignUser.setEid("dao-assign-user-eid");
        persistence.save(assignUser);
        EvalAssignUser foundAssignUser = assignmentDao.getAssignUserByEid("dao-assign-user-eid");
        Assert.assertNotNull(foundAssignUser);
        Assert.assertEquals(assignUser.getId(), foundAssignUser.getId());
        Assert.assertNull(assignmentDao.getAssignUserByEid(EvalTestDataLoad.INVALID_STRING_EID));

        Assert.assertEquals(2, assignmentDao.countEvaluationGroups(etdl.evaluationClosed.getId(), false));
        Assert.assertEquals(0, assignmentDao.countEvaluationGroups(EvalTestDataLoad.INVALID_LONG_ID, false));

        EvalAssignGroup foundAssignGroup = assignmentDao.getAssignGroupByEid(etdl.assignGroupProvided.getEid());
        Assert.assertNotNull(foundAssignGroup);
        Assert.assertEquals(etdl.assignGroupProvided.getId(), foundAssignGroup.getId());
        Assert.assertNull(assignmentDao.getAssignGroupByEid(EvalTestDataLoad.INVALID_STRING_EID));

        Assert.assertEquals(1, assignmentDao.countParticipantsForEval(
                etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE1_REF}));
        Assert.assertEquals(0, assignmentDao.countParticipantsForEval(
                etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE2_REF}));

        List<EvalAssignGroup> assignGroups = assignmentDao.getApprovedAssignGroupsForEvaluation(
                etdl.evaluationClosed.getId(), null);
        Assert.assertNotNull(assignGroups);
        Assert.assertEquals(2, assignGroups.size());
        ids = EvalTestDataLoad.makeIdList(assignGroups);
        Assert.assertTrue(ids.contains(etdl.assign3.getId()));
        Assert.assertTrue(ids.contains(etdl.assign4.getId()));

        assignGroups = assignmentDao.getApprovedAssignGroupsForEvaluation(
                etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE1_REF);
        Assert.assertNotNull(assignGroups);
        Assert.assertEquals(1, assignGroups.size());
        Assert.assertEquals(etdl.assign3.getId(), assignGroups.get(0).getId());

        Assert.assertEquals(2, assignmentDao.countApprovedAssignGroupsForEvaluation(
                etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}));
        Assert.assertEquals(0, assignmentDao.countApprovedAssignGroupsForEvaluation(
                etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE4_REF}));

        foundAssignGroup = assignmentDao.getAssignGroupByEvalAndGroupId(
                etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE2_REF);
        Assert.assertNotNull(foundAssignGroup);
        Assert.assertEquals(etdl.assign4.getId(), foundAssignGroup.getId());
        Assert.assertNull(assignmentDao.getAssignGroupByEvalAndGroupId(
                etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE4_REF));

        List<EvalAssignHierarchy> assignHierarchies = assignmentDao.getAssignHierarchyByEval(etdl.evaluationActive.getId());
        Assert.assertNotNull(assignHierarchies);
        Assert.assertEquals(1, assignHierarchies.size());
        Assert.assertEquals(etdl.assignHier1.getId(), assignHierarchies.get(0).getId());

        assignGroups = assignmentDao.getAssignGroupsForEvals(
                new Long[] {etdl.evaluationNewAdmin.getId()}, false, null);
        Assert.assertNotNull(assignGroups);
        Assert.assertEquals(2, assignGroups.size());
        ids = EvalTestDataLoad.makeIdList(assignGroups);
        Assert.assertTrue(ids.contains(etdl.assign7.getId()));
        Assert.assertTrue(ids.contains(etdl.assignGroupProvided.getId()));

        assignGroups = assignmentDao.getAssignGroupsForEvals(
                new Long[] {etdl.evaluationNewAdmin.getId()}, true, null);
        Assert.assertNotNull(assignGroups);
        Assert.assertEquals(3, assignGroups.size());

        EvalAssignGroup directAssignGroup = new EvalAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID, "dao-direct-group", EvalConstants.GROUP_TYPE_SITE,
                evalUnLocked, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        EvalAssignGroup hierarchyAssignGroup = new EvalAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID, "dao-hierarchy-group", EvalConstants.GROUP_TYPE_SITE,
                evalUnLocked, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, "dao-node-id", null);
        persistence.save(directAssignGroup);
        persistence.save(hierarchyAssignGroup);

        assignGroups = assignmentDao.getAssignGroupsForEvals(new Long[] {evalUnLocked.getId()}, true, false);
        Assert.assertNotNull(assignGroups);
        Assert.assertEquals(1, assignGroups.size());
        Assert.assertEquals(directAssignGroup.getId(), assignGroups.get(0).getId());

        assignGroups = assignmentDao.getAssignGroupsForEvals(new Long[] {evalUnLocked.getId()}, true, true);
        Assert.assertNotNull(assignGroups);
        Assert.assertEquals(1, assignGroups.size());
        Assert.assertEquals(hierarchyAssignGroup.getId(), assignGroups.get(0).getId());

        EvalAssignUser savedUser = new EvalAssignUser(
                "dao-saved-user",
                "dao-direct-group",
                EvalTestDataLoad.MAINT_USER_ID,
                EvalAssignUser.TYPE_EVALUATOR,
                EvalAssignUser.STATUS_LINKED,
                evalUnLocked,
                directAssignGroup.getId());
        EvalAssignUser groupLinkedUser = new EvalAssignUser(
                "dao-group-linked-user",
                "dao-direct-group",
                EvalTestDataLoad.MAINT_USER_ID,
                EvalAssignUser.TYPE_EVALUATOR,
                EvalAssignUser.STATUS_LINKED,
                evalUnLocked,
                directAssignGroup.getId());
        EvalAssignUser groupUnlinkedUser = new EvalAssignUser(
                "dao-group-unlinked-user",
                "dao-direct-group",
                EvalTestDataLoad.MAINT_USER_ID,
                EvalAssignUser.TYPE_EVALUATOR,
                EvalAssignUser.STATUS_UNLINKED,
                evalUnLocked,
                directAssignGroup.getId());
        Set<EvalAssignUser> assignUsers = new HashSet<>();
        assignUsers.add(savedUser);
        assignUsers.add(groupLinkedUser);
        assignUsers.add(groupUnlinkedUser);
        assignmentDao.saveAssignUsers(assignUsers);

        Assert.assertNotNull(savedUser.getId());
        Assert.assertNotNull(groupLinkedUser.getId());
        Assert.assertNotNull(groupUnlinkedUser.getId());

        EvalAssignUser firstEvaluationUser = new EvalAssignUser(
                "dao-cross-eval-user",
                "dao-cross-eval-group",
                EvalTestDataLoad.MAINT_USER_ID,
                EvalAssignUser.TYPE_EVALUATOR,
                EvalAssignUser.STATUS_LINKED,
                etdl.evaluationNew,
                null);
        EvalAssignUser secondEvaluationUser = new EvalAssignUser(
                "dao-cross-eval-user",
                "dao-cross-eval-group",
                EvalTestDataLoad.MAINT_USER_ID,
                EvalAssignUser.TYPE_EVALUATOR,
                EvalAssignUser.STATUS_LINKED,
                evalUnLocked,
                null);
        assignmentDao.saveAssignUsers(Arrays.asList(firstEvaluationUser, secondEvaluationUser));

        Assert.assertNotNull(firstEvaluationUser.getId());
        Assert.assertNotNull(secondEvaluationUser.getId());
        Assert.assertNotEquals(firstEvaluationUser.getId(), secondEvaluationUser.getId());
        Assert.assertNotNull(persistence.findById(EvalAssignUser.class, firstEvaluationUser.getId()));
        Assert.assertNotNull(persistence.findById(EvalAssignUser.class, secondEvaluationUser.getId()));

        assignmentDao.deleteAssignUsersByIds(new Long[] {savedUser.getId()});
        Assert.assertNull(persistence.findById(EvalAssignUser.class, savedUser.getId()));

        int removedByGroup = assignmentDao.deleteAssignGroupAndLinkedUsers(
                directAssignGroup, EvalAssignUser.STATUS_UNLINKED);
        Assert.assertEquals(1, removedByGroup);
        Assert.assertNull(persistence.findById(EvalAssignGroup.class, directAssignGroup.getId()));
        Assert.assertNull(persistence.findById(EvalAssignUser.class, groupLinkedUser.getId()));
        Assert.assertNotNull(persistence.findById(EvalAssignUser.class, groupUnlinkedUser.getId()));

        EvalAssignHierarchy savedHierarchy = new EvalAssignHierarchy(
                EvalTestDataLoad.MAINT_USER_ID,
                "dao-save-node",
                evalUnLocked,
                Boolean.TRUE,
                Boolean.TRUE,
                Boolean.FALSE,
                null,
                null);
        EvalAssignGroup savedHierarchyGroup = new EvalAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID, "dao-save-node-group", EvalConstants.GROUP_TYPE_SITE,
                evalUnLocked, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, "dao-save-node", null);
        Set<EvalAssignHierarchy> hierarchySet = new HashSet<>();
        hierarchySet.add(savedHierarchy);
        Set<EvalAssignGroup> hierarchyGroupSet = new HashSet<>();
        hierarchyGroupSet.add(savedHierarchyGroup);
        assignmentDao.saveAssignHierarchyAndGroups(hierarchySet, hierarchyGroupSet);

        Assert.assertNotNull(savedHierarchy.getId());
        Assert.assertNotNull(savedHierarchyGroup.getId());

        assignHierarchies = assignmentDao.getAssignHierarchiesByIds(new Long[] {savedHierarchy.getId()});
        Assert.assertNotNull(assignHierarchies);
        ids = EvalTestDataLoad.makeIdList(assignHierarchies);
        Assert.assertTrue(ids.contains(savedHierarchy.getId()));
        Assert.assertTrue(assignmentDao.getAssignHierarchiesByIds(new Long[] {}).isEmpty());

        assignGroups = assignmentDao.getAssignGroupsByEvalAndNodeIds(
                evalUnLocked.getId(), new HashSet<>(Arrays.asList("dao-save-node")));
        Assert.assertNotNull(assignGroups);
        Assert.assertEquals(1, assignGroups.size());
        Assert.assertEquals(savedHierarchyGroup.getId(), assignGroups.get(0).getId());
        Assert.assertTrue(assignmentDao.getAssignGroupsByEvalAndNodeIds(evalUnLocked.getId(), new HashSet<>()).isEmpty());

        assignmentDao.deleteAssignHierarchyAndGroups(hierarchySet, hierarchyGroupSet);
        Assert.assertNull(persistence.findById(EvalAssignHierarchy.class, savedHierarchy.getId()));
        Assert.assertNull(persistence.findById(EvalAssignGroup.class, savedHierarchyGroup.getId()));

        EvalAssignUser cleanupUser = new EvalAssignUser(
                "dao-cleanup-user",
                evalUnLocked,
                "dao-cleanup-group",
                EvalTestDataLoad.MAINT_USER_ID);
        EvalAssignGroup cleanupGroup = new EvalAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID, "dao-cleanup-group", EvalConstants.GROUP_TYPE_SITE,
                evalUnLocked, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, null, null);
        EvalAssignHierarchy cleanupHierarchy = new EvalAssignHierarchy(
                EvalTestDataLoad.MAINT_USER_ID,
                "dao-cleanup-node",
                evalUnLocked,
                Boolean.TRUE,
                Boolean.TRUE,
                Boolean.FALSE,
                null,
                null);
        persistence.save(cleanupGroup);
        persistence.save(cleanupUser);
        persistence.save(cleanupHierarchy);

        Assert.assertEquals(1, assignmentDao.countAssignGroupsByEvalAndGroupId(evalUnLocked.getId(), "dao-cleanup-group"));
        assignmentDao.deleteAssignmentsForEvaluation(evalUnLocked.getId());
        Assert.assertEquals(0, assignmentDao.countAssignGroupsByEvalAndGroupId(evalUnLocked.getId(), "dao-cleanup-group"));
        Assert.assertTrue(assignmentDao.getParticipantsForEval(evalUnLocked.getId(), null, null, null, null, null, null).isEmpty());
        Assert.assertTrue(assignmentDao.getAssignHierarchyByEval(evalUnLocked.getId()).isEmpty());
    }

    @Test
    public void testSaveResponseAndAnswers() {
        EvalResponse response = new EvalResponse(
                EvalTestDataLoad.STUDENT_USER_ID,
                EvalTestDataLoad.SITE1_REF,
                etdl.evaluationActiveUntaken,
                new Date());
        Set<EvalAnswer> answers = new HashSet<>();
        EvalAnswer answer = new EvalAnswer(response, etdl.templateItem1P, etdl.item1, null, null, "text");
        answers.add(answer);
        response.setAnswers(answers);

        responseDao.saveResponseAndAnswers(response, answers, null);

        Assert.assertNotNull(response.getId());
        Assert.assertNotNull(answer.getId());

        Assert.assertNotNull(persistence.findById(EvalResponse.class, response.getId()));
        Assert.assertNotNull(persistence.findById(EvalAnswer.class, answer.getId()));

        Set<EvalAnswer> answersToDelete = new HashSet<>();
        answersToDelete.add(answer);
        responseDao.saveResponseAndAnswers(response, new HashSet<>(), answersToDelete);
        Assert.assertNull(persistence.findById(EvalAnswer.class, answer.getId()));

        EvalResponse completedResponse = new EvalResponse(
                "dao-completed-response-owner",
                EvalTestDataLoad.SITE1_REF,
                evalUnLocked,
                new Date(),
                new Date(),
                null);
        EvalResponse partialResponse = new EvalResponse(
                "dao-partial-response-owner",
                EvalTestDataLoad.SITE2_REF,
                evalUnLocked,
                new Date(),
                null,
                null);
        responseDao.saveResponseAndAnswers(completedResponse, null, null);
        responseDao.saveResponseAndAnswers(partialResponse, null, null);

        List<EvalResponse> responses = responseDao.getEvaluationResponses(evalUnLocked.getId(), null, null);
        Assert.assertEquals(2, responses.size());

        responses = responseDao.getEvaluationResponses(evalUnLocked.getId(), new String[] {}, null);
        Assert.assertEquals(2, responses.size());

        responses = responseDao.getEvaluationResponses(evalUnLocked.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, null);
        Assert.assertEquals(1, responses.size());
        Assert.assertEquals(completedResponse.getId(), responses.get(0).getId());

        responses = responseDao.getEvaluationResponses(evalUnLocked.getId(), new String[] {}, true);
        Assert.assertEquals(1, responses.size());
        Assert.assertEquals(completedResponse.getId(), responses.get(0).getId());

        responses = responseDao.getEvaluationResponses(evalUnLocked.getId(), new String[] {}, false);
        Assert.assertEquals(1, responses.size());
        Assert.assertEquals(partialResponse.getId(), responses.get(0).getId());

        Assert.assertEquals(2, responseDao.countEvaluationResponses(
                new Long[] {evalUnLocked.getId()}, null, new String[] {}, null));
        Assert.assertEquals(1, responseDao.countEvaluationResponses(
                new Long[] {evalUnLocked.getId()}, "dao-completed-response-owner", new String[] {}, true));
    }

    @Test
    public void testHierarchyTemplateItemLookupsAndBatchSave() {
        String nodeId = "dao-hierarchy-node";
        EvalTemplateItem templateItem = new EvalTemplateItem(
                EvalTestDataLoad.MAINT_USER_ID,
                etdl.templatePublic,
                etdl.item1,
                99,
                EvalConstants.ITEM_CATEGORY_COURSE,
                EvalConstants.HIERARCHY_LEVEL_NODE,
                nodeId);
        persistence.save(templateItem);

        List<EvalTemplateItem> templateItems = authoringDao.getTemplateItemsByHierarchyNodeId(nodeId);
        Assert.assertNotNull(templateItems);
        Assert.assertEquals(1, templateItems.size());
        Assert.assertEquals(templateItem.getId(), templateItems.get(0).getId());

        templateItems.get(0).setHierarchyLevel(EvalConstants.HIERARCHY_LEVEL_TOP);
        templateItems.get(0).setHierarchyNodeId(EvalConstants.HIERARCHY_NODE_ID_NONE);
        authoringDao.saveTemplateItems(new HashSet<>(templateItems));

        EvalTemplateItem updated = persistence.findById(EvalTemplateItem.class, templateItem.getId());
        Assert.assertEquals(EvalConstants.HIERARCHY_LEVEL_TOP, updated.getHierarchyLevel());
        Assert.assertEquals(EvalConstants.HIERARCHY_NODE_ID_NONE, updated.getHierarchyNodeId());
        Assert.assertTrue(authoringDao.getTemplateItemsByHierarchyNodeId(nodeId).isEmpty());
    }

    @Test
    public void testEvalGroupNodesByNodeIds() {
        EvalGroupNodes first = new EvalGroupNodes(new Date(), "dao-node-a", new ArrayList<>(Arrays.asList(EvalTestDataLoad.SITE1_REF)));
        EvalGroupNodes second = new EvalGroupNodes(new Date(), "dao-node-b", new ArrayList<>(Arrays.asList(EvalTestDataLoad.SITE2_REF)));
        persistence.save(first);
        persistence.save(second);

        List<EvalGroupNodes> groupNodes = groupNodeDao.getEvalGroupNodesByNodeIds(new String[] {"dao-node-b", "dao-node-a"});
        Assert.assertNotNull(groupNodes);
        Assert.assertEquals(2, groupNodes.size());
        Assert.assertEquals(first.getId(), groupNodes.get(0).getId());
        Assert.assertEquals(second.getId(), groupNodes.get(1).getId());

        Assert.assertTrue(groupNodeDao.getEvalGroupNodesByNodeIds(new String[] {"dao-node-missing"}).isEmpty());
        Assert.assertTrue(groupNodeDao.getEvalGroupNodesByNodeIds(new String[] {}).isEmpty());
    }

    @Test
    public void testAuthoringSimpleLookupsAndBatchHelpers() {
        EvalScale scale = new EvalScale(
                EvalTestDataLoad.ADMIN_USER_ID,
                "DAO lookup scale",
                EvalConstants.SCALE_MODE_SCALE,
                EvalConstants.SHARING_PRIVATE,
                EvalTestDataLoad.NOT_EXPERT,
                "description",
                EvalConstants.SCALE_IDEAL_NONE,
                new ArrayList<>(Arrays.asList("Low", "High")),
                EvalTestDataLoad.UNLOCKED);
        scale.setEid("dao-scale-eid");
        persistence.save(scale);

        Assert.assertEquals(scale.getId(), authoringDao.getScaleByEid("dao-scale-eid").getId());
        Assert.assertNull(authoringDao.getScaleByEid(EvalTestDataLoad.INVALID_STRING_EID));

        List<EvalScale> scalesWithNullMode = authoringDao.getScalesWithNullMode();
        Assert.assertNotNull(scalesWithNullMode);

        scale.setTitle("DAO lookup scale updated");
        authoringDao.saveScales(new HashSet<>(Arrays.asList(scale)));
        Assert.assertEquals("DAO lookup scale updated", authoringDao.getScaleByEid("dao-scale-eid").getTitle());
        Assert.assertEquals(scale.getId(), authoringDao.getScalesByIds(new Long[] {scale.getId()}).get(0).getId());
        Assert.assertTrue(authoringDao.getScalesByIds(new Long[] {}).isEmpty());

        itemUnlocked.setEid("dao-item-eid");
        persistence.save(itemUnlocked);
        Assert.assertEquals(itemUnlocked.getId(), authoringDao.getItemByEid("dao-item-eid").getId());
        Assert.assertNull(authoringDao.getItemByEid(EvalTestDataLoad.INVALID_STRING_EID));
        Assert.assertEquals(itemUnlocked.getId(), authoringDao.getItemsByIds(new Long[] {itemUnlocked.getId()}).get(0).getId());
        Assert.assertTrue(authoringDao.getItemsByIds(new Long[] {}).isEmpty());
        Assert.assertTrue(EvalTestDataLoad.makeIdList(authoringDao.getItemsUsingScale(etdl.scale2.getId())).contains(itemUnlocked.getId()));

        itemUnlocked.setItemText("DAO item updated");
        authoringDao.saveItems(new HashSet<>(Arrays.asList(itemUnlocked)));
        Assert.assertEquals("DAO item updated", ((EvalItem) persistence.findById(EvalItem.class, itemUnlocked.getId())).getItemText());

        EvalItemGroup itemGroup = new EvalItemGroup(
                EvalTestDataLoad.MAINT_USER_ID,
                EvalConstants.ITEM_GROUP_TYPE_CATEGORY,
                "DAO item group title",
                null,
                EvalTestDataLoad.NOT_EXPERT,
                null,
                null);
        persistence.save(itemGroup);
        Assert.assertEquals(itemGroup.getId(), authoringDao.getItemGroupByTitle("DAO item group title").getId());
        Assert.assertNull(authoringDao.getItemGroupByTitle("DAO missing item group title"));

        EvalItemGroup emptyTopGroup = new EvalItemGroup(
                EvalTestDataLoad.MAINT_USER_ID,
                EvalConstants.ITEM_GROUP_TYPE_CATEGORY,
                "DAO empty top item group",
                null,
                EvalTestDataLoad.NOT_EXPERT,
                null,
                null);
        persistence.save(emptyTopGroup);
        EvalItemGroup parentWithChild = new EvalItemGroup(
                EvalTestDataLoad.MAINT_USER_ID,
                EvalConstants.ITEM_GROUP_TYPE_CATEGORY,
                "DAO parent item group",
                null,
                EvalTestDataLoad.NOT_EXPERT,
                null,
                null);
        persistence.save(parentWithChild);
        EvalItemGroup childGroup = new EvalItemGroup(
                EvalTestDataLoad.MAINT_USER_ID,
                EvalConstants.ITEM_GROUP_TYPE_CATEGORY,
                "DAO child item group",
                null,
                EvalTestDataLoad.NOT_EXPERT,
                parentWithChild,
                null);
        persistence.save(childGroup);
        EvalItemGroup expertGroup = new EvalItemGroup(
                EvalTestDataLoad.MAINT_USER_ID,
                EvalConstants.ITEM_GROUP_TYPE_CATEGORY,
                "DAO expert item group",
                null,
                EvalTestDataLoad.EXPERT,
                null,
                null);
        persistence.save(expertGroup);

        List<Long> itemGroupIds = EvalTestDataLoad.makeIdList(authoringDao.getItemGroups(null, EvalTestDataLoad.MAINT_USER_ID, true, false));
        Assert.assertTrue(itemGroupIds.contains(emptyTopGroup.getId()));
        Assert.assertTrue(itemGroupIds.contains(parentWithChild.getId()));
        Assert.assertFalse(itemGroupIds.contains(expertGroup.getId()));

        itemGroupIds = EvalTestDataLoad.makeIdList(authoringDao.getItemGroups(null, EvalTestDataLoad.MAINT_USER_ID, false, false));
        Assert.assertFalse(itemGroupIds.contains(emptyTopGroup.getId()));
        Assert.assertTrue(itemGroupIds.contains(parentWithChild.getId()));

        itemGroupIds = EvalTestDataLoad.makeIdList(authoringDao.getItemGroups(parentWithChild.getId(), EvalTestDataLoad.MAINT_USER_ID, true, false));
        Assert.assertTrue(itemGroupIds.contains(childGroup.getId()));
        Assert.assertFalse(itemGroupIds.contains(parentWithChild.getId()));

        itemGroupIds = EvalTestDataLoad.makeIdList(authoringDao.getItemGroups(null, EvalTestDataLoad.MAINT_USER_ID, true, true));
        Assert.assertTrue(itemGroupIds.contains(expertGroup.getId()));
        Assert.assertFalse(itemGroupIds.contains(emptyTopGroup.getId()));

        EvalTemplateItem templateItem = new EvalTemplateItem(
                EvalTestDataLoad.MAINT_USER_ID,
                etdl.templatePublic,
                itemUnlocked,
                100,
                EvalConstants.ITEM_CATEGORY_COURSE,
                EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE);
        templateItem.setEid("dao-template-item-eid");
        persistence.save(templateItem);

        etdl.templatePublic.setEid("dao-template-eid");
        persistence.save(etdl.templatePublic);
        Assert.assertEquals(etdl.templatePublic.getId(), authoringDao.getTemplateByEid("dao-template-eid").getId());
        Assert.assertNull(authoringDao.getTemplateByEid(EvalTestDataLoad.INVALID_STRING_EID));

        List<EvalTemplate> autoUseTemplates = authoringDao.getTemplatesByAutoUseTag(EvalTestDataLoad.AUTO_USE_TAG);
        Assert.assertEquals(1, autoUseTemplates.size());
        Assert.assertEquals(etdl.templateUnused.getId(), autoUseTemplates.get(0).getId());

        Assert.assertEquals(templateItem.getId(), authoringDao.getTemplateItemByEid("dao-template-item-eid").getId());
        Assert.assertNull(authoringDao.getTemplateItemByEid(EvalTestDataLoad.INVALID_STRING_EID));
        Assert.assertEquals(templateItem.getId(), authoringDao.getTemplateItemsByIds(new Long[] {templateItem.getId()}).get(0).getId());
        Assert.assertTrue(authoringDao.getTemplateItemsByIds(new Long[] {}).isEmpty());
        List<EvalTemplateItem> autoUseTemplateItems = authoringDao.getTemplateItemsByAutoUseTag(EvalTestDataLoad.AUTO_USE_TAG);
        Assert.assertEquals(2, autoUseTemplateItems.size());
        Assert.assertEquals(etdl.templateItem2A.getId(), autoUseTemplateItems.get(0).getId());
        Assert.assertEquals(etdl.templateItem6UU.getId(), autoUseTemplateItems.get(1).getId());
        Assert.assertTrue(EvalTestDataLoad.makeIdList(authoringDao.getTemplatesUsingItem(itemUnlocked.getId()))
                .contains(etdl.templatePublic.getId()));
        Assert.assertNotNull(authoringDao.getOrphanedTemplateItems());

        List<EvalItem> autoUseItems = authoringDao.getItemsByAutoUseTag(EvalTestDataLoad.AUTO_USE_TAG);
        Assert.assertEquals(1, autoUseItems.size());
        Assert.assertEquals(etdl.item4.getId(), autoUseItems.get(0).getId());

        List<EvalScale> sharedScales = authoringDao.getScalesForUser(null,
                new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC});
        Assert.assertTrue(EvalTestDataLoad.makeIdList(sharedScales).contains(etdl.scale4.getId()));
        Assert.assertTrue(EvalTestDataLoad.makeIdList(sharedScales).contains(scale.getId()));
        sharedScales = authoringDao.getScalesForUser(EvalTestDataLoad.USER_ID,
                new String[] {EvalConstants.SHARING_PRIVATE});
        Assert.assertEquals(0, sharedScales.size());

        List<EvalItem> sharedItems = authoringDao.getItemsForUser(null,
                new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, null, true);
        Assert.assertTrue(EvalTestDataLoad.makeIdList(sharedItems).contains(etdl.item7.getId()));
        Assert.assertTrue(EvalTestDataLoad.makeIdList(sharedItems).contains(itemUnlocked.getId()));
        sharedItems = authoringDao.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID,
                new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, null, false);
        Assert.assertTrue(EvalTestDataLoad.makeIdList(sharedItems).contains(itemUnlocked.getId()));
        Assert.assertFalse(EvalTestDataLoad.makeIdList(sharedItems).contains(etdl.item6.getId()));
        sharedItems = authoringDao.getItemsForUser(null,
                new String[] {EvalConstants.SHARING_PRIVATE}, "do you think", true);
        Assert.assertEquals(3, sharedItems.size());

        List<EvalTemplate> sharedTemplates = authoringDao.getTemplatesForUser(null,
                new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, true);
        Assert.assertTrue(EvalTestDataLoad.makeIdList(sharedTemplates).contains(etdl.templateAdminNoItems.getId()));
        sharedTemplates = authoringDao.getTemplatesForUser(null,
                new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, false);
        Assert.assertFalse(EvalTestDataLoad.makeIdList(sharedTemplates).contains(etdl.templateAdminNoItems.getId()));
        sharedTemplates = authoringDao.getTemplatesForUser(EvalTestDataLoad.USER_ID,
                new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, true);
        Assert.assertEquals(5, sharedTemplates.size());

        EvalTemplateItem linkedTemplateItem = new EvalTemplateItem(
                EvalTestDataLoad.MAINT_USER_ID,
                etdl.templatePublic,
                itemUnlocked,
                102,
                EvalConstants.ITEM_CATEGORY_COURSE,
                EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE);
        authoringDao.saveTemplateItemWithLinks(linkedTemplateItem, itemUnlocked, etdl.templatePublic);
        Assert.assertNotNull(linkedTemplateItem.getId());

        EvalTemplateItem blockParent = new EvalTemplateItem(
                EvalTestDataLoad.MAINT_USER_ID,
                etdl.templatePublic,
                itemUnlocked,
                101,
                EvalConstants.ITEM_CATEGORY_COURSE,
                EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE);
        blockParent.setBlockParent(Boolean.TRUE);
        persistence.save(blockParent);

        EvalTemplateItem firstChild = new EvalTemplateItem(
                EvalTestDataLoad.MAINT_USER_ID,
                etdl.templatePublic,
                itemUnlocked,
                1,
                EvalConstants.ITEM_CATEGORY_COURSE,
                EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE);
        firstChild.setBlockId(blockParent.getId());
        EvalTemplateItem secondChild = new EvalTemplateItem(
                EvalTestDataLoad.MAINT_USER_ID,
                etdl.templatePublic,
                itemUnlocked,
                2,
                EvalConstants.ITEM_CATEGORY_COURSE,
                EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE);
        secondChild.setBlockId(blockParent.getId());
        persistence.save(firstChild);
        persistence.save(secondChild);

        Assert.assertTrue(authoringDao.countTopLevelTemplateItems(etdl.templatePublic.getId()) >= 2);
        Assert.assertEquals(2, authoringDao.countBlockChildTemplateItems(etdl.templatePublic.getId(), blockParent.getId()));

        List<EvalTemplateItem> childItems = authoringDao.getBlockChildTemplateItems(blockParent.getId());
        Assert.assertEquals(2, childItems.size());
        Assert.assertEquals(firstChild.getId(), childItems.get(0).getId());
        Assert.assertEquals(secondChild.getId(), childItems.get(1).getId());

        EvalItem deletedItem = new EvalItem(
                EvalTestDataLoad.MAINT_USER_ID,
                "DAO deleted item",
                EvalConstants.SHARING_PRIVATE,
                EvalConstants.ITEM_TYPE_HEADER,
                EvalTestDataLoad.NOT_EXPERT);
        persistence.save(deletedItem);
        authoringDao.deleteItems(new HashSet<>(Arrays.asList(deletedItem)));
        Assert.assertNull(persistence.findById(EvalItem.class, deletedItem.getId()));

        EvalScale deletedScale = new EvalScale(
                EvalTestDataLoad.ADMIN_USER_ID,
                "DAO deleted scale",
                EvalConstants.SCALE_MODE_SCALE,
                EvalConstants.SHARING_PRIVATE,
                EvalTestDataLoad.NOT_EXPERT,
                "description",
                EvalConstants.SCALE_IDEAL_NONE,
                new ArrayList<>(Arrays.asList("Low", "High")),
                EvalTestDataLoad.UNLOCKED);
        persistence.save(deletedScale);
        authoringDao.deleteScales(new HashSet<>(Arrays.asList(deletedScale)));
        Assert.assertNull(persistence.findById(EvalScale.class, deletedScale.getId()));
    }

}
