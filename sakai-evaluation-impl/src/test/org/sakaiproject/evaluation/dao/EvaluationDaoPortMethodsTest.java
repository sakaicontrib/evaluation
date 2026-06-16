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
 * DAO regression tests for evaluation DAO port methods.
 */
@Slf4j
public class EvaluationDaoPortMethodsTest extends AbstractEvaluationDaoTest {

    @Test
    public void testValidateDao() {
        Assert.assertNotNull(evaluationDao);
        List<EvalTemplate> templates = evaluationDao.findAll(EvalTemplate.class);
        Assert.assertNotNull( templates );
        Assert.assertTrue(templates.size() > 4);
        List<EvalAssignUser> assignUsers = evaluationDao.findAll(EvalAssignUser.class);
        Assert.assertNotNull( assignUsers );
        Assert.assertTrue(assignUsers.size() > 20);
        evaluationDao.findAll(EvalEmailTemplate.class);
    }

    @Test
    public void testEvalConfigLookups() {
        String configName = "dao.config.lookup.test";
        EvalConfig config = new EvalConfig(configName, "test value");
        evaluationDao.save(config);

        Assert.assertTrue(evaluationDao.countEvalConfigs() > 0);

        EvalConfig found = evaluationDao.getEvalConfigByName(configName);
        Assert.assertNotNull(found);
        Assert.assertEquals(configName, found.getName());
        Assert.assertEquals("test value", found.getValue());

        Assert.assertNull(evaluationDao.getEvalConfigByName("dao.config.lookup.missing"));

        List<EvalConfig> configs = evaluationDao.getAllEvalConfigs();
        Assert.assertNotNull(configs);
        Assert.assertTrue(configs.size() >= evaluationDao.countEvalConfigs());
        Assert.assertTrue(configs.stream().anyMatch(c -> configName.equals(c.getName())));

        Assert.assertEquals(1, evaluationDao.countEvalConfigsByNames(new String[] {configName, "dao.config.lookup.missing"}));
        Assert.assertEquals(0, evaluationDao.countEvalConfigsByNames(new String[] {}));
    }

    @Test
    public void testPreloadLookups() {
        Assert.assertTrue(evaluationDao.countEvalScales() > 0);
        Assert.assertTrue(evaluationDao.countEvalItems() > 0);
        Assert.assertTrue(evaluationDao.countEvalItemGroups() > 0);

        int defaultEmailTemplateCount = evaluationDao.countDefaultEmailTemplates();
        Assert.assertTrue(defaultEmailTemplateCount > 0);

        List<EvalEmailTemplate> defaultEmailTemplates = evaluationDao.getDefaultEmailTemplates();
        Assert.assertNotNull(defaultEmailTemplates);
        Assert.assertEquals(defaultEmailTemplateCount, defaultEmailTemplates.size());
        Assert.assertTrue(defaultEmailTemplates.stream().allMatch(t -> t.getDefaultType() != null));
    }

    @Test
    public void testEmailTemplateLookups() {
        String templateType = "dao.email.template.type";
        String defaultType = "dao.email.template.default";
        EvalEmailTemplate ownerTemplate = new EvalEmailTemplate(
                EvalTestDataLoad.MAINT_USER_ID, templateType, "Owner Subject", "Owner Message");
        ownerTemplate.setEid("dao-email-template-owner");
        EvalEmailTemplate otherOwnerTemplate = new EvalEmailTemplate(
                EvalTestDataLoad.USER_ID, templateType, "Other Subject", "Other Message");
        EvalEmailTemplate defaultTemplate = new EvalEmailTemplate(
                EvalTestDataLoad.ADMIN_USER_ID, templateType, "Default Subject", "Default Message", defaultType);
        evaluationDao.save(ownerTemplate);
        evaluationDao.save(otherOwnerTemplate);
        evaluationDao.save(defaultTemplate);

        List<EvalEmailTemplate> templates = evaluationDao.getEmailTemplates(null, templateType, null);
        Assert.assertNotNull(templates);
        Assert.assertEquals(3, templates.size());

        templates = evaluationDao.getEmailTemplates(EvalTestDataLoad.MAINT_USER_ID, templateType, null);
        Assert.assertNotNull(templates);
        Assert.assertEquals(1, templates.size());
        Assert.assertEquals(ownerTemplate.getId(), templates.get(0).getId());

        templates = evaluationDao.getEmailTemplates(null, templateType, true);
        Assert.assertNotNull(templates);
        Assert.assertEquals(1, templates.size());
        Assert.assertEquals(defaultTemplate.getId(), templates.get(0).getId());

        templates = evaluationDao.getEmailTemplates(null, templateType, false);
        Assert.assertNotNull(templates);
        Assert.assertEquals(2, templates.size());

        EvalEmailTemplate foundDefault = evaluationDao.getDefaultEmailTemplate(defaultType);
        Assert.assertNotNull(foundDefault);
        Assert.assertEquals(defaultTemplate.getId(), foundDefault.getId());

        EvalEmailTemplate foundByEid = evaluationDao.getEmailTemplateByEid("dao-email-template-owner");
        Assert.assertNotNull(foundByEid);
        Assert.assertEquals(ownerTemplate.getId(), foundByEid.getId());

        List<EvalEvaluation> evaluations = evaluationDao.getEvaluationsUsingEmailTemplate(
                etdl.emailTemplate1.getId(), EvalConstants.EMAIL_TEMPLATE_AVAILABLE);
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(1, evaluations.size());
        Assert.assertEquals(etdl.evaluationNew.getId(), evaluations.get(0).getId());
        Assert.assertEquals(1, evaluationDao.countEvaluationsUsingEmailTemplate(
                etdl.emailTemplate1.getId(), EvalConstants.EMAIL_TEMPLATE_AVAILABLE));

        evaluations = evaluationDao.getEvaluationsUsingEmailTemplate(
                etdl.emailTemplate3.getId(), EvalConstants.EMAIL_TEMPLATE_REMINDER);
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(1, evaluations.size());
        Assert.assertEquals(etdl.evaluationActive.getId(), evaluations.get(0).getId());
        Assert.assertEquals(1, evaluationDao.countEvaluationsUsingEmailTemplate(
                etdl.emailTemplate3.getId(), EvalConstants.EMAIL_TEMPLATE_REMINDER));

        evaluations = evaluationDao.getEvaluationsUsingEmailTemplate(
                etdl.emailTemplate6.getId(), EvalConstants.EMAIL_TEMPLATE_SUBMITTED);
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(1, evaluations.size());
        Assert.assertEquals(etdl.evaluationActive.getId(), evaluations.get(0).getId());
        Assert.assertEquals(1, evaluationDao.countEvaluationsUsingEmailTemplate(
                etdl.emailTemplate6.getId(), EvalConstants.EMAIL_TEMPLATE_SUBMITTED));

        Set<EvalEmailTemplate> templatesToDelete = new HashSet<>();
        templatesToDelete.add(ownerTemplate);
        templatesToDelete.add(otherOwnerTemplate);
        evaluationDao.deleteEmailTemplates(templatesToDelete);

        Assert.assertNull(evaluationDao.findById(EvalEmailTemplate.class, ownerTemplate.getId()));
        Assert.assertNull(evaluationDao.findById(EvalEmailTemplate.class, otherOwnerTemplate.getId()));
        Assert.assertNotNull(evaluationDao.findById(EvalEmailTemplate.class, defaultTemplate.getId()));

        Assert.assertNull(evaluationDao.getDefaultEmailTemplate("dao.email.template.missing.default"));
        Assert.assertNull(evaluationDao.getEmailTemplateByEid("dao-email-template-missing"));
    }

    @Test
    public void testEvalAdminLookups() {
        String userId = "dao-admin-user";
        EvalAdmin admin = new EvalAdmin(userId, new Date(), EvalTestDataLoad.ADMIN_USER_ID);
        evaluationDao.save(admin);

        EvalAdmin found = evaluationDao.getEvalAdminByUserId(userId);
        Assert.assertNotNull(found);
        Assert.assertEquals(userId, found.getUserId());
        Assert.assertEquals(EvalTestDataLoad.ADMIN_USER_ID, found.getAssignorUserId());

        Assert.assertNull(evaluationDao.getEvalAdminByUserId("dao-admin-missing"));

        List<EvalAdmin> admins = evaluationDao.getAllEvalAdmins();
        Assert.assertNotNull(admins);
        Assert.assertTrue(admins.stream().anyMatch(a -> userId.equals(a.getUserId())));
    }

    @Test
    public void testHierarchyRuleLookups() {
        Long nodeId = 987654321L;
        EvalHierarchyRule rule = new EvalHierarchyRule(nodeId, "subject=%biology%", "include");
        evaluationDao.save(rule);

        EvalHierarchyRule found = evaluationDao.getHierarchyRuleById(rule.getId());
        Assert.assertNotNull(found);
        Assert.assertEquals(nodeId, found.getNodeID());
        Assert.assertEquals("subject=%biology%", found.getRule());
        Assert.assertEquals("include", found.getOpt());

        Assert.assertNull(evaluationDao.getHierarchyRuleById(-987654321L));

        List<EvalHierarchyRule> nodeRules = evaluationDao.getHierarchyRulesByNodeId(nodeId);
        Assert.assertNotNull(nodeRules);
        Assert.assertTrue(nodeRules.stream().anyMatch(r -> rule.getId().equals(r.getId())));

        List<EvalHierarchyRule> allRules = evaluationDao.getAllHierarchyRules();
        Assert.assertNotNull(allRules);
        Assert.assertTrue(allRules.stream().anyMatch(r -> rule.getId().equals(r.getId())));
    }

    @Test
    public void testDeleteHierarchyRules() {
        EvalHierarchyRule first = new EvalHierarchyRule(111111111L, "subject=%history%", "include");
        EvalHierarchyRule second = new EvalHierarchyRule(111111111L, "subject=%math%", "exclude");
        EvalHierarchyRule retained = new EvalHierarchyRule(222222222L, "subject=%biology%", "include");
        evaluationDao.save(first);
        evaluationDao.save(second);
        evaluationDao.save(retained);

        Set<EvalHierarchyRule> rules = new HashSet<>();
        rules.add(first);
        rules.add(second);
        evaluationDao.deleteHierarchyRules(rules);

        Assert.assertNull(evaluationDao.getHierarchyRuleById(first.getId()));
        Assert.assertNull(evaluationDao.getHierarchyRuleById(second.getId()));
        Assert.assertNotNull(evaluationDao.getHierarchyRuleById(retained.getId()));
    }

    @Test
    public void testAdhocUserLookups() {
        EvalAdhocUser byUsername = evaluationDao.getAdhocUserByUsername(etdl.user1.getUsername());
        Assert.assertNotNull(byUsername);
        Assert.assertEquals(etdl.user1.getId(), byUsername.getId());

        Assert.assertNull(evaluationDao.getAdhocUserByUsername(EvalTestDataLoad.INVALID_CONSTANT_STRING));

        EvalAdhocUser byEmail = evaluationDao.getAdhocUserByEmail(etdl.user2.getEmail());
        Assert.assertNotNull(byEmail);
        Assert.assertEquals(etdl.user2.getId(), byEmail.getId());

        Assert.assertNull(evaluationDao.getAdhocUserByEmail("missing@example.edu"));

        List<EvalAdhocUser> allUsers = evaluationDao.getAllAdhocUsers();
        Assert.assertNotNull(allUsers);
        Assert.assertEquals(3, allUsers.size());

        List<EvalAdhocUser> users = evaluationDao.getAdhocUsersByIds(new Long[] {etdl.user1.getId(), etdl.user3.getId()});
        Assert.assertNotNull(users);
        Assert.assertEquals(2, users.size());
        List<Long> ids = EvalTestDataLoad.makeIdList(users);
        Assert.assertTrue(ids.contains(etdl.user1.getId()));
        Assert.assertTrue(ids.contains(etdl.user3.getId()));

        Assert.assertTrue(evaluationDao.getAdhocUsersByIds(new Long[] {}).isEmpty());
    }

    @Test
    public void testAdhocGroupsForOwnerLookup() {
        List<EvalAdhocGroup> groups = evaluationDao.getAdhocGroupsForOwner(EvalTestDataLoad.MAINT_USER_ID);
        Assert.assertNotNull(groups);
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals(etdl.group2.getId(), groups.get(0).getId());

        groups = evaluationDao.getAdhocGroupsForOwner(EvalTestDataLoad.USER_ID);
        Assert.assertNotNull(groups);
        Assert.assertTrue(groups.isEmpty());
    }

    @Test
    public void testResponseLookups() {
        Long[] evaluationIds = new Long[] {
                etdl.evaluationActive.getId(),
                etdl.evaluationClosed.getId(),
                etdl.evaluationViewable.getId()
        };

        Assert.assertEquals(3, evaluationDao.countEvaluationsByIds(evaluationIds));
        Assert.assertEquals(1, evaluationDao.countEvaluationById(etdl.evaluationActive.getId()));
        Assert.assertEquals(0, evaluationDao.countEvaluationById(EvalTestDataLoad.INVALID_LONG_ID));

        List<EvalResponse> responses = evaluationDao.getEvaluationResponsesForUserAndGroup(
                etdl.evaluationActive.getId(), EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
        Assert.assertNotNull(responses);
        Assert.assertEquals(1, responses.size());
        Assert.assertEquals(etdl.response1.getId(), responses.get(0).getId());

        responses = evaluationDao.getEvaluationResponsesForUser(evaluationIds, EvalTestDataLoad.USER_ID, true);
        Assert.assertNotNull(responses);
        Assert.assertEquals(4, responses.size());
        List<Long> ids = EvalTestDataLoad.makeIdList(responses);
        Assert.assertTrue(ids.contains(etdl.response1.getId()));
        Assert.assertTrue(ids.contains(etdl.response2.getId()));
        Assert.assertTrue(ids.contains(etdl.response4.getId()));
        Assert.assertTrue(ids.contains(etdl.response6.getId()));

        responses = evaluationDao.getEvaluationResponsesForUser(evaluationIds, null, true);
        Assert.assertNotNull(responses);
        Assert.assertEquals(6, responses.size());

        responses = evaluationDao.getEvaluationResponsesForUser(evaluationIds, EvalTestDataLoad.USER_ID, false);
        Assert.assertNotNull(responses);
        Assert.assertTrue(responses.isEmpty());

        Assert.assertEquals(3, evaluationDao.countResponses(etdl.evaluationClosed.getId(), null, null));
        Assert.assertEquals(1, evaluationDao.countResponses(etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE1_REF, null));
        Assert.assertEquals(3, evaluationDao.countResponses(etdl.evaluationClosed.getId(), null, true));
        Assert.assertEquals(0, evaluationDao.countResponses(etdl.evaluationClosed.getId(), null, false));

        responses = evaluationDao.getEvaluationResponses(etdl.evaluationClosed.getId(), null, true);
        Assert.assertNotNull(responses);
        Assert.assertEquals(3, responses.size());

        responses = evaluationDao.getEvaluationResponses(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, true);
        Assert.assertNotNull(responses);
        Assert.assertEquals(2, responses.size());

        responses = evaluationDao.getEvaluationResponses(
                evaluationIds, EvalTestDataLoad.USER_ID, new String[] {EvalTestDataLoad.SITE2_REF}, true);
        Assert.assertNotNull(responses);
        Assert.assertEquals(2, responses.size());
        ids = EvalTestDataLoad.makeIdList(responses);
        Assert.assertTrue(ids.contains(etdl.response4.getId()));
        Assert.assertTrue(ids.contains(etdl.response6.getId()));

        responses = evaluationDao.getEvaluationResponses(evaluationIds, null, null, true);
        Assert.assertNotNull(responses);
        Assert.assertEquals(6, responses.size());

        Assert.assertEquals(2, evaluationDao.countEvaluationResponses(
                evaluationIds, EvalTestDataLoad.USER_ID, new String[] {EvalTestDataLoad.SITE2_REF}, true));
        Assert.assertEquals(6, evaluationDao.countEvaluationResponses(evaluationIds, null, null, true));
        Assert.assertEquals(0, evaluationDao.countEvaluationResponses(evaluationIds, EvalTestDataLoad.USER_ID, null, false));
    }

    @Test
    public void testEvaluationAndAssignmentLookups() {
        Assert.assertEquals(1, evaluationDao.countEvaluationById(etdl.evaluationActive.getId()));
        Assert.assertEquals(0, evaluationDao.countEvaluationById(EvalTestDataLoad.INVALID_LONG_ID));

        EvalEvaluation foundEvaluation = evaluationDao.getEvaluationByEid(etdl.evaluationProvided.getEid());
        Assert.assertNotNull(foundEvaluation);
        Assert.assertEquals(etdl.evaluationProvided.getId(), foundEvaluation.getId());
        Assert.assertNull(evaluationDao.getEvaluationByEid(EvalTestDataLoad.INVALID_STRING_EID));

        Assert.assertEquals(1, evaluationDao.countTemplateById(etdl.templatePublic.getId()));
        Assert.assertEquals(0, evaluationDao.countTemplateById(EvalTestDataLoad.INVALID_LONG_ID));
        Assert.assertEquals(2, evaluationDao.countEvaluationsByTemplateId(etdl.templatePublic.getId()));

        List<EvalEvaluation> evaluations = evaluationDao.getEvaluationsByTemplateId(etdl.templateUser.getId());
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(3, evaluations.size());
        List<Long> ids = EvalTestDataLoad.makeIdList(evaluations);
        Assert.assertTrue(ids.contains(etdl.evaluationActive.getId()));
        Assert.assertTrue(ids.contains(etdl.evaluationViewable.getId()));
        Assert.assertTrue(ids.contains(etdl.evaluationProvided.getId()));

        String termId = "dao-term-lookup";
        evalUnLocked.setTermId(termId);
        evaluationDao.update(evalUnLocked);
        evaluations = evaluationDao.getEvaluationsByTermId(termId);
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(1, evaluations.size());
        Assert.assertEquals(evalUnLocked.getId(), evaluations.get(0).getId());

        evaluations = evaluationDao.getEvaluationsByState(EvalConstants.EVALUATION_STATE_ACTIVE);
        Assert.assertNotNull(evaluations);
        ids = EvalTestDataLoad.makeIdList(evaluations);
        Assert.assertTrue(ids.contains(etdl.evaluationActive.getId()));

        evaluations = evaluationDao.getEvaluationsNotViewableOrDeleted();
        Assert.assertNotNull(evaluations);
        ids = EvalTestDataLoad.makeIdList(evaluations);
        Assert.assertTrue(ids.contains(etdl.evaluationActive.getId()));
        Assert.assertTrue(ids.contains(etdl.evaluationClosed.getId()));
        Assert.assertFalse(ids.contains(etdl.evaluationViewable.getId()));
        Assert.assertFalse(ids.contains(etdl.evaluationDeleted.getId()));

        int evalTitleCount = evaluationDao.countEvaluationsByTitle("%Eval%");
        Assert.assertTrue(evalTitleCount >= 11);
        evaluations = evaluationDao.getEvaluationsByTitle("%Eval%", "title", 0, 5);
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(5, evaluations.size());
        evaluations = evaluationDao.getEvaluationsByTitle("%Eval%", "title", 8, 5);
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(Math.min(5, evalTitleCount - 8), evaluations.size());

        EvalAssignUser assignUser = new EvalAssignUser(
                "dao-assign-user-eid-user",
                etdl.evaluationActiveUntaken,
                EvalTestDataLoad.SITE1_REF,
                EvalTestDataLoad.MAINT_USER_ID);
        assignUser.setEid("dao-assign-user-eid");
        evaluationDao.save(assignUser);
        EvalAssignUser foundAssignUser = evaluationDao.getAssignUserByEid("dao-assign-user-eid");
        Assert.assertNotNull(foundAssignUser);
        Assert.assertEquals(assignUser.getId(), foundAssignUser.getId());
        Assert.assertNull(evaluationDao.getAssignUserByEid(EvalTestDataLoad.INVALID_STRING_EID));

        Assert.assertEquals(2, evaluationDao.countEvaluationGroups(etdl.evaluationClosed.getId(), false));
        Assert.assertEquals(0, evaluationDao.countEvaluationGroups(EvalTestDataLoad.INVALID_LONG_ID, false));

        EvalAssignGroup foundAssignGroup = evaluationDao.getAssignGroupByEid(etdl.assignGroupProvided.getEid());
        Assert.assertNotNull(foundAssignGroup);
        Assert.assertEquals(etdl.assignGroupProvided.getId(), foundAssignGroup.getId());
        Assert.assertNull(evaluationDao.getAssignGroupByEid(EvalTestDataLoad.INVALID_STRING_EID));

        Assert.assertEquals(1, evaluationDao.countParticipantsForEval(
                etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE1_REF}));
        Assert.assertEquals(0, evaluationDao.countParticipantsForEval(
                etdl.evaluationActive.getId(), new String[] {EvalTestDataLoad.SITE2_REF}));

        List<EvalAssignGroup> assignGroups = evaluationDao.getApprovedAssignGroupsForEvaluation(
                etdl.evaluationClosed.getId(), null);
        Assert.assertNotNull(assignGroups);
        Assert.assertEquals(2, assignGroups.size());
        ids = EvalTestDataLoad.makeIdList(assignGroups);
        Assert.assertTrue(ids.contains(etdl.assign3.getId()));
        Assert.assertTrue(ids.contains(etdl.assign4.getId()));

        assignGroups = evaluationDao.getApprovedAssignGroupsForEvaluation(
                etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE1_REF);
        Assert.assertNotNull(assignGroups);
        Assert.assertEquals(1, assignGroups.size());
        Assert.assertEquals(etdl.assign3.getId(), assignGroups.get(0).getId());

        Assert.assertEquals(2, evaluationDao.countApprovedAssignGroupsForEvaluation(
                etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}));
        Assert.assertEquals(0, evaluationDao.countApprovedAssignGroupsForEvaluation(
                etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE4_REF}));

        foundAssignGroup = evaluationDao.getAssignGroupByEvalAndGroupId(
                etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE2_REF);
        Assert.assertNotNull(foundAssignGroup);
        Assert.assertEquals(etdl.assign4.getId(), foundAssignGroup.getId());
        Assert.assertNull(evaluationDao.getAssignGroupByEvalAndGroupId(
                etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE4_REF));

        List<EvalAssignHierarchy> assignHierarchies = evaluationDao.getAssignHierarchyByEval(etdl.evaluationActive.getId());
        Assert.assertNotNull(assignHierarchies);
        Assert.assertEquals(1, assignHierarchies.size());
        Assert.assertEquals(etdl.assignHier1.getId(), assignHierarchies.get(0).getId());

        assignGroups = evaluationDao.getAssignGroupsForEvals(
                new Long[] {etdl.evaluationNewAdmin.getId()}, false, null);
        Assert.assertNotNull(assignGroups);
        Assert.assertEquals(2, assignGroups.size());
        ids = EvalTestDataLoad.makeIdList(assignGroups);
        Assert.assertTrue(ids.contains(etdl.assign7.getId()));
        Assert.assertTrue(ids.contains(etdl.assignGroupProvided.getId()));

        assignGroups = evaluationDao.getAssignGroupsForEvals(
                new Long[] {etdl.evaluationNewAdmin.getId()}, true, null);
        Assert.assertNotNull(assignGroups);
        Assert.assertEquals(3, assignGroups.size());

        EvalAssignGroup directAssignGroup = new EvalAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID, "dao-direct-group", EvalConstants.GROUP_TYPE_SITE,
                evalUnLocked, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE);
        EvalAssignGroup hierarchyAssignGroup = new EvalAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID, "dao-hierarchy-group", EvalConstants.GROUP_TYPE_SITE,
                evalUnLocked, Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, "dao-node-id", null);
        evaluationDao.save(directAssignGroup);
        evaluationDao.save(hierarchyAssignGroup);

        assignGroups = evaluationDao.getAssignGroupsForEvals(new Long[] {evalUnLocked.getId()}, true, false);
        Assert.assertNotNull(assignGroups);
        Assert.assertEquals(1, assignGroups.size());
        Assert.assertEquals(directAssignGroup.getId(), assignGroups.get(0).getId());

        assignGroups = evaluationDao.getAssignGroupsForEvals(new Long[] {evalUnLocked.getId()}, true, true);
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
        evaluationDao.saveAssignUsers(assignUsers);

        Assert.assertNotNull(savedUser.getId());
        Assert.assertNotNull(groupLinkedUser.getId());
        Assert.assertNotNull(groupUnlinkedUser.getId());

        evaluationDao.deleteAssignUsersByIds(new Long[] {savedUser.getId()});
        Assert.assertNull(evaluationDao.findById(EvalAssignUser.class, savedUser.getId()));

        int removedByGroup = evaluationDao.deleteAssignUsersByAssignGroupIdExcludingStatus(
                directAssignGroup.getId(), EvalAssignUser.STATUS_UNLINKED);
        Assert.assertEquals(1, removedByGroup);
        Assert.assertNull(evaluationDao.findById(EvalAssignUser.class, groupLinkedUser.getId()));
        Assert.assertNotNull(evaluationDao.findById(EvalAssignUser.class, groupUnlinkedUser.getId()));

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
        evaluationDao.saveAssignHierarchyAndGroups(hierarchySet, hierarchyGroupSet);

        Assert.assertNotNull(savedHierarchy.getId());
        Assert.assertNotNull(savedHierarchyGroup.getId());

        assignHierarchies = evaluationDao.getAssignHierarchiesByIds(new Long[] {savedHierarchy.getId()});
        Assert.assertNotNull(assignHierarchies);
        ids = EvalTestDataLoad.makeIdList(assignHierarchies);
        Assert.assertTrue(ids.contains(savedHierarchy.getId()));
        Assert.assertTrue(evaluationDao.getAssignHierarchiesByIds(new Long[] {}).isEmpty());

        assignGroups = evaluationDao.getAssignGroupsByEvalAndNodeIds(
                evalUnLocked.getId(), new HashSet<>(Arrays.asList("dao-save-node")));
        Assert.assertNotNull(assignGroups);
        Assert.assertEquals(1, assignGroups.size());
        Assert.assertEquals(savedHierarchyGroup.getId(), assignGroups.get(0).getId());
        Assert.assertTrue(evaluationDao.getAssignGroupsByEvalAndNodeIds(evalUnLocked.getId(), new HashSet<>()).isEmpty());

        evaluationDao.deleteAssignHierarchyAndGroups(hierarchySet, hierarchyGroupSet);
        Assert.assertNull(evaluationDao.findById(EvalAssignHierarchy.class, savedHierarchy.getId()));
        Assert.assertNull(evaluationDao.findById(EvalAssignGroup.class, savedHierarchyGroup.getId()));

        EvalAssignUser cleanupUser = new EvalAssignUser(
                "dao-cleanup-user",
                evalUnLocked,
                "dao-direct-group",
                EvalTestDataLoad.MAINT_USER_ID);
        EvalAssignHierarchy cleanupHierarchy = new EvalAssignHierarchy(
                EvalTestDataLoad.MAINT_USER_ID,
                "dao-cleanup-node",
                evalUnLocked,
                Boolean.TRUE,
                Boolean.TRUE,
                Boolean.FALSE,
                null,
                null);
        evaluationDao.save(cleanupUser);
        evaluationDao.save(cleanupHierarchy);

        Assert.assertEquals(1, evaluationDao.countAssignGroupsByEvalAndGroupId(evalUnLocked.getId(), "dao-direct-group"));
        evaluationDao.deleteAssignmentsForEvaluation(evalUnLocked.getId());
        Assert.assertEquals(0, evaluationDao.countAssignGroupsByEvalAndGroupId(evalUnLocked.getId(), "dao-direct-group"));
        Assert.assertTrue(evaluationDao.getParticipantsForEval(evalUnLocked.getId(), null, null, null, null, null, null).isEmpty());
        Assert.assertTrue(evaluationDao.getAssignHierarchyByEval(evalUnLocked.getId()).isEmpty());
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

        evaluationDao.saveResponseAndAnswers(response, answers);

        Assert.assertNotNull(response.getId());
        Assert.assertNotNull(answer.getId());

        Assert.assertNotNull(evaluationDao.findById(EvalResponse.class, response.getId()));
        Assert.assertNotNull(evaluationDao.findById(EvalAnswer.class, answer.getId()));

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
        evaluationDao.saveResponseAndAnswers(completedResponse, null);
        evaluationDao.saveResponseAndAnswers(partialResponse, null);

        List<EvalResponse> responses = evaluationDao.getEvaluationResponses(evalUnLocked.getId(), null, null);
        Assert.assertEquals(2, responses.size());

        responses = evaluationDao.getEvaluationResponses(evalUnLocked.getId(), new String[] {}, null);
        Assert.assertEquals(2, responses.size());

        responses = evaluationDao.getEvaluationResponses(evalUnLocked.getId(), new String[] {EvalTestDataLoad.SITE1_REF}, null);
        Assert.assertEquals(1, responses.size());
        Assert.assertEquals(completedResponse.getId(), responses.get(0).getId());

        responses = evaluationDao.getEvaluationResponses(evalUnLocked.getId(), new String[] {}, true);
        Assert.assertEquals(1, responses.size());
        Assert.assertEquals(completedResponse.getId(), responses.get(0).getId());

        responses = evaluationDao.getEvaluationResponses(evalUnLocked.getId(), new String[] {}, false);
        Assert.assertEquals(1, responses.size());
        Assert.assertEquals(partialResponse.getId(), responses.get(0).getId());

        Assert.assertEquals(2, evaluationDao.countEvaluationResponses(
                new Long[] {evalUnLocked.getId()}, null, new String[] {}, null));
        Assert.assertEquals(1, evaluationDao.countEvaluationResponses(
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
        evaluationDao.save(templateItem);

        List<EvalTemplateItem> templateItems = evaluationDao.getTemplateItemsByHierarchyNodeId(nodeId);
        Assert.assertNotNull(templateItems);
        Assert.assertEquals(1, templateItems.size());
        Assert.assertEquals(templateItem.getId(), templateItems.get(0).getId());

        templateItems.get(0).setHierarchyLevel(EvalConstants.HIERARCHY_LEVEL_TOP);
        templateItems.get(0).setHierarchyNodeId(EvalConstants.HIERARCHY_NODE_ID_NONE);
        evaluationDao.saveTemplateItems(new HashSet<>(templateItems));

        EvalTemplateItem updated = evaluationDao.findById(EvalTemplateItem.class, templateItem.getId());
        Assert.assertEquals(EvalConstants.HIERARCHY_LEVEL_TOP, updated.getHierarchyLevel());
        Assert.assertEquals(EvalConstants.HIERARCHY_NODE_ID_NONE, updated.getHierarchyNodeId());
        Assert.assertTrue(evaluationDao.getTemplateItemsByHierarchyNodeId(nodeId).isEmpty());
    }

    @Test
    public void testEvalGroupNodesByNodeIds() {
        EvalGroupNodes first = new EvalGroupNodes(new Date(), "dao-node-a", new ArrayList<>(Arrays.asList(EvalTestDataLoad.SITE1_REF)));
        EvalGroupNodes second = new EvalGroupNodes(new Date(), "dao-node-b", new ArrayList<>(Arrays.asList(EvalTestDataLoad.SITE2_REF)));
        evaluationDao.save(first);
        evaluationDao.save(second);

        List<EvalGroupNodes> groupNodes = evaluationDao.getEvalGroupNodesByNodeIds(new String[] {"dao-node-b", "dao-node-a"});
        Assert.assertNotNull(groupNodes);
        Assert.assertEquals(2, groupNodes.size());
        Assert.assertEquals(first.getId(), groupNodes.get(0).getId());
        Assert.assertEquals(second.getId(), groupNodes.get(1).getId());

        Assert.assertTrue(evaluationDao.getEvalGroupNodesByNodeIds(new String[] {"dao-node-missing"}).isEmpty());
        Assert.assertTrue(evaluationDao.getEvalGroupNodesByNodeIds(new String[] {}).isEmpty());
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
        evaluationDao.save(scale);

        Assert.assertEquals(scale.getId(), evaluationDao.getScaleByEid("dao-scale-eid").getId());
        Assert.assertNull(evaluationDao.getScaleByEid(EvalTestDataLoad.INVALID_STRING_EID));

        List<EvalScale> scalesWithNullMode = evaluationDao.getScalesWithNullMode();
        Assert.assertNotNull(scalesWithNullMode);

        scale.setTitle("DAO lookup scale updated");
        evaluationDao.saveScales(new HashSet<>(Arrays.asList(scale)));
        Assert.assertEquals("DAO lookup scale updated", evaluationDao.getScaleByEid("dao-scale-eid").getTitle());
        Assert.assertEquals(scale.getId(), evaluationDao.getScalesByIds(new Long[] {scale.getId()}).get(0).getId());
        Assert.assertTrue(evaluationDao.getScalesByIds(new Long[] {}).isEmpty());

        itemUnlocked.setEid("dao-item-eid");
        evaluationDao.save(itemUnlocked);
        Assert.assertEquals(itemUnlocked.getId(), evaluationDao.getItemByEid("dao-item-eid").getId());
        Assert.assertNull(evaluationDao.getItemByEid(EvalTestDataLoad.INVALID_STRING_EID));
        Assert.assertEquals(itemUnlocked.getId(), evaluationDao.getItemsByIds(new Long[] {itemUnlocked.getId()}).get(0).getId());
        Assert.assertTrue(evaluationDao.getItemsByIds(new Long[] {}).isEmpty());
        Assert.assertTrue(EvalTestDataLoad.makeIdList(evaluationDao.getItemsUsingScale(etdl.scale2.getId())).contains(itemUnlocked.getId()));

        itemUnlocked.setItemText("DAO item updated");
        evaluationDao.saveItems(new HashSet<>(Arrays.asList(itemUnlocked)));
        Assert.assertEquals("DAO item updated", ((EvalItem) evaluationDao.findById(EvalItem.class, itemUnlocked.getId())).getItemText());

        EvalItemGroup itemGroup = new EvalItemGroup(
                EvalTestDataLoad.MAINT_USER_ID,
                EvalConstants.ITEM_GROUP_TYPE_CATEGORY,
                "DAO item group title",
                null,
                EvalTestDataLoad.NOT_EXPERT,
                null,
                null);
        evaluationDao.save(itemGroup);
        Assert.assertEquals(itemGroup.getId(), evaluationDao.getItemGroupByTitle("DAO item group title").getId());
        Assert.assertNull(evaluationDao.getItemGroupByTitle("DAO missing item group title"));

        EvalItemGroup emptyTopGroup = new EvalItemGroup(
                EvalTestDataLoad.MAINT_USER_ID,
                EvalConstants.ITEM_GROUP_TYPE_CATEGORY,
                "DAO empty top item group",
                null,
                EvalTestDataLoad.NOT_EXPERT,
                null,
                null);
        evaluationDao.save(emptyTopGroup);
        EvalItemGroup parentWithChild = new EvalItemGroup(
                EvalTestDataLoad.MAINT_USER_ID,
                EvalConstants.ITEM_GROUP_TYPE_CATEGORY,
                "DAO parent item group",
                null,
                EvalTestDataLoad.NOT_EXPERT,
                null,
                null);
        evaluationDao.save(parentWithChild);
        EvalItemGroup childGroup = new EvalItemGroup(
                EvalTestDataLoad.MAINT_USER_ID,
                EvalConstants.ITEM_GROUP_TYPE_CATEGORY,
                "DAO child item group",
                null,
                EvalTestDataLoad.NOT_EXPERT,
                parentWithChild,
                null);
        evaluationDao.save(childGroup);
        EvalItemGroup expertGroup = new EvalItemGroup(
                EvalTestDataLoad.MAINT_USER_ID,
                EvalConstants.ITEM_GROUP_TYPE_CATEGORY,
                "DAO expert item group",
                null,
                EvalTestDataLoad.EXPERT,
                null,
                null);
        evaluationDao.save(expertGroup);

        List<Long> itemGroupIds = EvalTestDataLoad.makeIdList(evaluationDao.getItemGroups(null, EvalTestDataLoad.MAINT_USER_ID, true, false));
        Assert.assertTrue(itemGroupIds.contains(emptyTopGroup.getId()));
        Assert.assertTrue(itemGroupIds.contains(parentWithChild.getId()));
        Assert.assertFalse(itemGroupIds.contains(expertGroup.getId()));

        itemGroupIds = EvalTestDataLoad.makeIdList(evaluationDao.getItemGroups(null, EvalTestDataLoad.MAINT_USER_ID, false, false));
        Assert.assertFalse(itemGroupIds.contains(emptyTopGroup.getId()));
        Assert.assertTrue(itemGroupIds.contains(parentWithChild.getId()));

        itemGroupIds = EvalTestDataLoad.makeIdList(evaluationDao.getItemGroups(parentWithChild.getId(), EvalTestDataLoad.MAINT_USER_ID, true, false));
        Assert.assertTrue(itemGroupIds.contains(childGroup.getId()));
        Assert.assertFalse(itemGroupIds.contains(parentWithChild.getId()));

        itemGroupIds = EvalTestDataLoad.makeIdList(evaluationDao.getItemGroups(null, EvalTestDataLoad.MAINT_USER_ID, true, true));
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
        evaluationDao.save(templateItem);

        etdl.templatePublic.setEid("dao-template-eid");
        evaluationDao.save(etdl.templatePublic);
        Assert.assertEquals(etdl.templatePublic.getId(), evaluationDao.getTemplateByEid("dao-template-eid").getId());
        Assert.assertNull(evaluationDao.getTemplateByEid(EvalTestDataLoad.INVALID_STRING_EID));

        List<EvalTemplate> autoUseTemplates = evaluationDao.getTemplatesByAutoUseTag(EvalTestDataLoad.AUTO_USE_TAG);
        Assert.assertEquals(1, autoUseTemplates.size());
        Assert.assertEquals(etdl.templateUnused.getId(), autoUseTemplates.get(0).getId());

        Assert.assertEquals(templateItem.getId(), evaluationDao.getTemplateItemByEid("dao-template-item-eid").getId());
        Assert.assertNull(evaluationDao.getTemplateItemByEid(EvalTestDataLoad.INVALID_STRING_EID));
        Assert.assertEquals(templateItem.getId(), evaluationDao.getTemplateItemsByIds(new Long[] {templateItem.getId()}).get(0).getId());
        Assert.assertTrue(evaluationDao.getTemplateItemsByIds(new Long[] {}).isEmpty());
        List<EvalTemplateItem> autoUseTemplateItems = evaluationDao.getTemplateItemsByAutoUseTag(EvalTestDataLoad.AUTO_USE_TAG);
        Assert.assertEquals(2, autoUseTemplateItems.size());
        Assert.assertEquals(etdl.templateItem2A.getId(), autoUseTemplateItems.get(0).getId());
        Assert.assertEquals(etdl.templateItem6UU.getId(), autoUseTemplateItems.get(1).getId());
        Assert.assertTrue(EvalTestDataLoad.makeIdList(evaluationDao.getTemplatesUsingItem(itemUnlocked.getId()))
                .contains(etdl.templatePublic.getId()));
        Assert.assertNotNull(evaluationDao.getOrphanedTemplateItems());

        List<EvalItem> autoUseItems = evaluationDao.getItemsByAutoUseTag(EvalTestDataLoad.AUTO_USE_TAG);
        Assert.assertEquals(1, autoUseItems.size());
        Assert.assertEquals(etdl.item4.getId(), autoUseItems.get(0).getId());

        List<EvalScale> sharedScales = evaluationDao.getScalesForUser(null,
                new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC});
        Assert.assertTrue(EvalTestDataLoad.makeIdList(sharedScales).contains(etdl.scale4.getId()));
        Assert.assertTrue(EvalTestDataLoad.makeIdList(sharedScales).contains(scale.getId()));
        sharedScales = evaluationDao.getScalesForUser(EvalTestDataLoad.USER_ID,
                new String[] {EvalConstants.SHARING_PRIVATE});
        Assert.assertEquals(0, sharedScales.size());

        List<EvalItem> sharedItems = evaluationDao.getItemsForUser(null,
                new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, null, true);
        Assert.assertTrue(EvalTestDataLoad.makeIdList(sharedItems).contains(etdl.item7.getId()));
        Assert.assertTrue(EvalTestDataLoad.makeIdList(sharedItems).contains(itemUnlocked.getId()));
        sharedItems = evaluationDao.getItemsForUser(EvalTestDataLoad.MAINT_USER_ID,
                new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, null, false);
        Assert.assertTrue(EvalTestDataLoad.makeIdList(sharedItems).contains(itemUnlocked.getId()));
        Assert.assertFalse(EvalTestDataLoad.makeIdList(sharedItems).contains(etdl.item6.getId()));
        sharedItems = evaluationDao.getItemsForUser(null,
                new String[] {EvalConstants.SHARING_PRIVATE}, "do you think", true);
        Assert.assertEquals(3, sharedItems.size());

        List<EvalTemplate> sharedTemplates = evaluationDao.getTemplatesForUser(null,
                new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, true);
        Assert.assertTrue(EvalTestDataLoad.makeIdList(sharedTemplates).contains(etdl.templateAdminNoItems.getId()));
        sharedTemplates = evaluationDao.getTemplatesForUser(null,
                new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, false);
        Assert.assertFalse(EvalTestDataLoad.makeIdList(sharedTemplates).contains(etdl.templateAdminNoItems.getId()));
        sharedTemplates = evaluationDao.getTemplatesForUser(EvalTestDataLoad.USER_ID,
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
        evaluationDao.saveTemplateItemWithLinks(linkedTemplateItem, itemUnlocked, etdl.templatePublic);
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
        evaluationDao.save(blockParent);

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
        evaluationDao.save(firstChild);
        evaluationDao.save(secondChild);

        Assert.assertTrue(evaluationDao.countTopLevelTemplateItems(etdl.templatePublic.getId()) >= 2);
        Assert.assertEquals(2, evaluationDao.countBlockChildTemplateItems(etdl.templatePublic.getId(), blockParent.getId()));

        List<EvalTemplateItem> childItems = evaluationDao.getBlockChildTemplateItems(blockParent.getId());
        Assert.assertEquals(2, childItems.size());
        Assert.assertEquals(firstChild.getId(), childItems.get(0).getId());
        Assert.assertEquals(secondChild.getId(), childItems.get(1).getId());

        EvalItem deletedItem = new EvalItem(
                EvalTestDataLoad.MAINT_USER_ID,
                "DAO deleted item",
                EvalConstants.SHARING_PRIVATE,
                EvalConstants.ITEM_TYPE_HEADER,
                EvalTestDataLoad.NOT_EXPERT);
        evaluationDao.save(deletedItem);
        evaluationDao.deleteItems(new HashSet<>(Arrays.asList(deletedItem)));
        Assert.assertNull(evaluationDao.findById(EvalItem.class, deletedItem.getId()));

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
        evaluationDao.save(deletedScale);
        evaluationDao.deleteScales(new HashSet<>(Arrays.asList(deletedScale)));
        Assert.assertNull(evaluationDao.findById(EvalScale.class, deletedScale.getId()));
    }

}
