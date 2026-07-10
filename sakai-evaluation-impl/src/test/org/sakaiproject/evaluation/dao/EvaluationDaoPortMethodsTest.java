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
public class EvaluationDaoPortMethodsTest extends AbstractEvaluationDaoTest {

    @Test
    public void testValidateDao() {
        Assert.assertNotNull(persistence);
        List<EvalTemplate> templates = persistence.findAll(EvalTemplate.class);
        Assert.assertNotNull( templates );
        Assert.assertTrue(templates.size() > 4);
        List<EvalAssignUser> assignUsers = persistence.findAll(EvalAssignUser.class);
        Assert.assertNotNull( assignUsers );
        Assert.assertTrue(assignUsers.size() > 20);
        persistence.findAll(EvalEmailTemplate.class);
    }

    @Test
    public void testDeleteObjectAcceptsHibernateProxy() {
        EvalTemplate template = new EvalTemplate(EvalTestDataLoad.ADMIN_USER_ID,
                EvalConstants.TEMPLATE_TYPE_STANDARD, "DAO proxy delete template",
                EvalConstants.SHARING_PRIVATE, EvalTestDataLoad.NOT_EXPERT);
        template.setEid("dao-proxy-delete-template");
        persistence.save(template);
        Long templateId = template.getId();

        EvalTemplate templateProxy = loadUninitializedProxy(EvalTemplate.class, templateId);
        Assert.assertTrue(templateProxy instanceof HibernateProxy);

        persistence.delete(templateProxy);
        Session session = currentSession();
        session.flush();
        session.clear();

        Assert.assertNull(persistence.findById(EvalTemplate.class, templateId));
    }

    @Test
    public void testDeleteObjectHandlesDetachedDuplicateInSession() {
        EvalTemplate detachedTemplate = new EvalTemplate(EvalTestDataLoad.ADMIN_USER_ID,
                EvalConstants.TEMPLATE_TYPE_STANDARD, "DAO detached delete template",
                EvalConstants.SHARING_PRIVATE, EvalTestDataLoad.NOT_EXPERT);
        detachedTemplate.setEid("dao-detached-delete-template");
        persistence.save(detachedTemplate);
        Long templateId = detachedTemplate.getId();

        Session session = currentSession();
        session.flush();
        session.evict(detachedTemplate);

        EvalTemplate managedTemplate = persistence.findById(EvalTemplate.class, templateId);
        Assert.assertNotNull(managedTemplate);
        Assert.assertNotSame(detachedTemplate, managedTemplate);
        Assert.assertFalse(session.contains(detachedTemplate));
        Assert.assertTrue(session.contains(managedTemplate));

        persistence.delete(detachedTemplate);
        session.flush();
        session.clear();

        Assert.assertNull(persistence.findById(EvalTemplate.class, templateId));
    }

    @Test
    public void testCountDistinctGroupsInConsolidatedEmailMapping() {
        consolidatedEmailDao.resetConsolidatedEmailRecipients();
        Long assignUserId = persistence.findAll(EvalAssignUser.class).get(0).getId();
        persistence.save(emailProcessingData(assignUserId, "dao-email-processing-user-1", "dao-email-processing-group-1"));
        persistence.save(emailProcessingData(assignUserId, "dao-email-processing-user-2", "dao-email-processing-group-1"));
        persistence.save(emailProcessingData(assignUserId, "dao-email-processing-user-3", "dao-email-processing-group-2"));

        Assert.assertEquals(2, consolidatedEmailDao.countDistinctGroupsInConsolidatedEmailMapping());
    }

    private EvalEmailProcessingData emailProcessingData(Long assignUserId, String userId, String groupId) {
        EvalEmailProcessingData data = new EvalEmailProcessingData();
        data.setEauId(assignUserId);
        data.setUserId(userId);
        data.setGroupId(groupId);
        data.setEmailTemplateId(etdl.emailTemplate1.getId());
        data.setEvalId(etdl.evaluationActive.getId());
        data.setEvalDueDate(etdl.evaluationActive.getDueDate());
        return data;
    }

    @Test
    public void testEvalConfigLookups() {
        String configName = "dao.config.lookup.test";
        EvalConfig config = new EvalConfig(configName, "test value");
        persistence.save(config);

        Assert.assertTrue(settingsDao.countEvalConfigs() > 0);

        EvalConfig found = settingsDao.getEvalConfigByName(configName);
        Assert.assertNotNull(found);
        Assert.assertEquals(configName, found.getName());
        Assert.assertEquals("test value", found.getValue());

        Assert.assertNull(settingsDao.getEvalConfigByName("dao.config.lookup.missing"));

        List<EvalConfig> configs = settingsDao.getAllEvalConfigs();
        Assert.assertNotNull(configs);
        Assert.assertTrue(configs.size() >= settingsDao.countEvalConfigs());
        Assert.assertTrue(configs.stream().anyMatch(c -> configName.equals(c.getName())));

        Assert.assertEquals(1, settingsDao.countEvalConfigsByNames(new String[] {configName, "dao.config.lookup.missing"}));
        Assert.assertEquals(0, settingsDao.countEvalConfigsByNames(new String[] {}));
    }

    @Test
    public void testPreloadLookups() {
        Assert.assertTrue(authoringDao.countEvalScales() > 0);
        Assert.assertTrue(authoringDao.countEvalItems() > 0);
        Assert.assertTrue(authoringDao.countEvalItemGroups() > 0);

        int defaultEmailTemplateCount = emailTemplateDao.countDefaultEmailTemplates();
        Assert.assertTrue(defaultEmailTemplateCount > 0);

        List<EvalEmailTemplate> defaultEmailTemplates = emailTemplateDao.getDefaultEmailTemplates();
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
        persistence.save(ownerTemplate);
        persistence.save(otherOwnerTemplate);
        persistence.save(defaultTemplate);

        List<EvalEmailTemplate> templates = emailTemplateDao.getEmailTemplates(null, templateType, null);
        Assert.assertNotNull(templates);
        Assert.assertEquals(3, templates.size());

        templates = emailTemplateDao.getEmailTemplates(EvalTestDataLoad.MAINT_USER_ID, templateType, null);
        Assert.assertNotNull(templates);
        Assert.assertEquals(1, templates.size());
        Assert.assertEquals(ownerTemplate.getId(), templates.get(0).getId());

        templates = emailTemplateDao.getEmailTemplates(null, templateType, true);
        Assert.assertNotNull(templates);
        Assert.assertEquals(1, templates.size());
        Assert.assertEquals(defaultTemplate.getId(), templates.get(0).getId());

        templates = emailTemplateDao.getEmailTemplates(null, templateType, false);
        Assert.assertNotNull(templates);
        Assert.assertEquals(2, templates.size());

        EvalEmailTemplate foundDefault = emailTemplateDao.getDefaultEmailTemplate(defaultType);
        Assert.assertNotNull(foundDefault);
        Assert.assertEquals(defaultTemplate.getId(), foundDefault.getId());

        EvalEmailTemplate foundByEid = emailTemplateDao.getEmailTemplateByEid("dao-email-template-owner");
        Assert.assertNotNull(foundByEid);
        Assert.assertEquals(ownerTemplate.getId(), foundByEid.getId());

        List<EvalEvaluation> evaluations = emailTemplateDao.getEvaluationsUsingEmailTemplate(
                etdl.emailTemplate1.getId(), EvalConstants.EMAIL_TEMPLATE_AVAILABLE);
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(1, evaluations.size());
        Assert.assertEquals(etdl.evaluationNew.getId(), evaluations.get(0).getId());
        Assert.assertEquals(1, emailTemplateDao.countEvaluationsUsingEmailTemplate(
                etdl.emailTemplate1.getId(), EvalConstants.EMAIL_TEMPLATE_AVAILABLE));

        evaluations = emailTemplateDao.getEvaluationsUsingEmailTemplate(
                etdl.emailTemplate3.getId(), EvalConstants.EMAIL_TEMPLATE_REMINDER);
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(1, evaluations.size());
        Assert.assertEquals(etdl.evaluationActive.getId(), evaluations.get(0).getId());
        Assert.assertEquals(1, emailTemplateDao.countEvaluationsUsingEmailTemplate(
                etdl.emailTemplate3.getId(), EvalConstants.EMAIL_TEMPLATE_REMINDER));

        evaluations = emailTemplateDao.getEvaluationsUsingEmailTemplate(
                etdl.emailTemplate6.getId(), EvalConstants.EMAIL_TEMPLATE_SUBMITTED);
        Assert.assertNotNull(evaluations);
        Assert.assertEquals(1, evaluations.size());
        Assert.assertEquals(etdl.evaluationActive.getId(), evaluations.get(0).getId());
        Assert.assertEquals(1, emailTemplateDao.countEvaluationsUsingEmailTemplate(
                etdl.emailTemplate6.getId(), EvalConstants.EMAIL_TEMPLATE_SUBMITTED));

        Set<EvalEmailTemplate> templatesToDelete = new HashSet<>();
        templatesToDelete.add(ownerTemplate);
        templatesToDelete.add(otherOwnerTemplate);
        emailTemplateDao.deleteEmailTemplates(templatesToDelete);

        Assert.assertNull(persistence.findById(EvalEmailTemplate.class, ownerTemplate.getId()));
        Assert.assertNull(persistence.findById(EvalEmailTemplate.class, otherOwnerTemplate.getId()));
        Assert.assertNotNull(persistence.findById(EvalEmailTemplate.class, defaultTemplate.getId()));

        Assert.assertNull(emailTemplateDao.getDefaultEmailTemplate("dao.email.template.missing.default"));
        Assert.assertNull(emailTemplateDao.getEmailTemplateByEid("dao-email-template-missing"));
    }

    @Test
    public void testEvalAdminLookups() {
        String userId = "dao-admin-user";
        EvalAdmin admin = new EvalAdmin(userId, new Date(), EvalTestDataLoad.ADMIN_USER_ID);
        persistence.save(admin);

        EvalAdmin found = adminSupportDao.getEvalAdminByUserId(userId);
        Assert.assertNotNull(found);
        Assert.assertEquals(userId, found.getUserId());
        Assert.assertEquals(EvalTestDataLoad.ADMIN_USER_ID, found.getAssignorUserId());

        Assert.assertNull(adminSupportDao.getEvalAdminByUserId("dao-admin-missing"));

        List<EvalAdmin> admins = adminSupportDao.getAllEvalAdmins();
        Assert.assertNotNull(admins);
        Assert.assertTrue(admins.stream().anyMatch(a -> userId.equals(a.getUserId())));
    }

    @Test
    public void testHierarchyRuleLookups() {
        Long nodeId = 987654321L;
        EvalHierarchyRule rule = new EvalHierarchyRule(nodeId, "subject=%biology%", "include");
        persistence.save(rule);

        EvalHierarchyRule found = adminSupportDao.getHierarchyRuleById(rule.getId());
        Assert.assertNotNull(found);
        Assert.assertEquals(nodeId, found.getNodeID());
        Assert.assertEquals("subject=%biology%", found.getRule());
        Assert.assertEquals("include", found.getOpt());

        Assert.assertNull(adminSupportDao.getHierarchyRuleById(-987654321L));

        List<EvalHierarchyRule> nodeRules = adminSupportDao.getHierarchyRulesByNodeId(nodeId);
        Assert.assertNotNull(nodeRules);
        Assert.assertTrue(nodeRules.stream().anyMatch(r -> rule.getId().equals(r.getId())));

        List<EvalHierarchyRule> allRules = adminSupportDao.getAllHierarchyRules();
        Assert.assertNotNull(allRules);
        Assert.assertTrue(allRules.stream().anyMatch(r -> rule.getId().equals(r.getId())));
    }

    @Test
    public void testDeleteHierarchyRules() {
        EvalHierarchyRule first = new EvalHierarchyRule(111111111L, "subject=%history%", "include");
        EvalHierarchyRule second = new EvalHierarchyRule(111111111L, "subject=%math%", "exclude");
        EvalHierarchyRule retained = new EvalHierarchyRule(222222222L, "subject=%biology%", "include");
        persistence.save(first);
        persistence.save(second);
        persistence.save(retained);

        Set<EvalHierarchyRule> rules = new HashSet<>();
        rules.add(first);
        rules.add(second);
        adminSupportDao.deleteHierarchyRules(rules);

        Assert.assertNull(adminSupportDao.getHierarchyRuleById(first.getId()));
        Assert.assertNull(adminSupportDao.getHierarchyRuleById(second.getId()));
        Assert.assertNotNull(adminSupportDao.getHierarchyRuleById(retained.getId()));
    }

    @Test
    public void testAdhocUserLookups() {
        EvalAdhocUser byUsername = adminSupportDao.getAdhocUserByUsername(etdl.user1.getUsername());
        Assert.assertNotNull(byUsername);
        Assert.assertEquals(etdl.user1.getId(), byUsername.getId());

        Assert.assertNull(adminSupportDao.getAdhocUserByUsername(EvalTestDataLoad.INVALID_CONSTANT_STRING));

        EvalAdhocUser byEmail = adminSupportDao.getAdhocUserByEmail(etdl.user2.getEmail());
        Assert.assertNotNull(byEmail);
        Assert.assertEquals(etdl.user2.getId(), byEmail.getId());

        Assert.assertNull(adminSupportDao.getAdhocUserByEmail("missing@example.edu"));

        List<EvalAdhocUser> allUsers = adminSupportDao.getAllAdhocUsers();
        Assert.assertNotNull(allUsers);
        Assert.assertEquals(3, allUsers.size());

        List<EvalAdhocUser> users = adminSupportDao.getAdhocUsersByIds(new Long[] {etdl.user1.getId(), etdl.user3.getId()});
        Assert.assertNotNull(users);
        Assert.assertEquals(2, users.size());
        List<Long> ids = EvalTestDataLoad.makeIdList(users);
        Assert.assertTrue(ids.contains(etdl.user1.getId()));
        Assert.assertTrue(ids.contains(etdl.user3.getId()));

        Assert.assertTrue(adminSupportDao.getAdhocUsersByIds(new Long[] {}).isEmpty());
    }

    @Test
    public void testAdhocGroupsForOwnerLookup() {
        List<EvalAdhocGroup> groups = adminSupportDao.getAdhocGroupsForOwner(EvalTestDataLoad.MAINT_USER_ID);
        Assert.assertNotNull(groups);
        Assert.assertEquals(1, groups.size());
        Assert.assertEquals(etdl.group2.getId(), groups.get(0).getId());

        groups = adminSupportDao.getAdhocGroupsForOwner(EvalTestDataLoad.USER_ID);
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

        Assert.assertEquals(3, queryDao.countEvaluationsByIds(evaluationIds));
        Assert.assertEquals(1, queryDao.countEvaluationById(etdl.evaluationActive.getId()));
        Assert.assertEquals(0, queryDao.countEvaluationById(EvalTestDataLoad.INVALID_LONG_ID));

        List<EvalResponse> responses = responseDao.getEvaluationResponsesForUserAndGroup(
                etdl.evaluationActive.getId(), EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF);
        Assert.assertNotNull(responses);
        Assert.assertEquals(1, responses.size());
        Assert.assertEquals(etdl.response1.getId(), responses.get(0).getId());

        responses = responseDao.getEvaluationResponsesForUser(evaluationIds, EvalTestDataLoad.USER_ID, true);
        Assert.assertNotNull(responses);
        Assert.assertEquals(4, responses.size());
        List<Long> ids = EvalTestDataLoad.makeIdList(responses);
        Assert.assertTrue(ids.contains(etdl.response1.getId()));
        Assert.assertTrue(ids.contains(etdl.response2.getId()));
        Assert.assertTrue(ids.contains(etdl.response4.getId()));
        Assert.assertTrue(ids.contains(etdl.response6.getId()));

        responses = responseDao.getEvaluationResponsesForUser(evaluationIds, null, true);
        Assert.assertNotNull(responses);
        Assert.assertEquals(6, responses.size());

        responses = responseDao.getEvaluationResponsesForUser(evaluationIds, EvalTestDataLoad.USER_ID, false);
        Assert.assertNotNull(responses);
        Assert.assertTrue(responses.isEmpty());

        Assert.assertEquals(3, responseDao.countResponses(etdl.evaluationClosed.getId(), null, null));
        Assert.assertEquals(1, responseDao.countResponses(etdl.evaluationClosed.getId(), EvalTestDataLoad.SITE1_REF, null));
        Assert.assertEquals(3, responseDao.countResponses(etdl.evaluationClosed.getId(), null, true));
        Assert.assertEquals(0, responseDao.countResponses(etdl.evaluationClosed.getId(), null, false));

        responses = responseDao.getEvaluationResponses(etdl.evaluationClosed.getId(), null, true);
        Assert.assertNotNull(responses);
        Assert.assertEquals(3, responses.size());

        responses = responseDao.getEvaluationResponses(etdl.evaluationClosed.getId(), new String[] {EvalTestDataLoad.SITE2_REF}, true);
        Assert.assertNotNull(responses);
        Assert.assertEquals(2, responses.size());

        responses = responseDao.getEvaluationResponses(
                evaluationIds, EvalTestDataLoad.USER_ID, new String[] {EvalTestDataLoad.SITE2_REF}, true);
        Assert.assertNotNull(responses);
        Assert.assertEquals(2, responses.size());
        ids = EvalTestDataLoad.makeIdList(responses);
        Assert.assertTrue(ids.contains(etdl.response4.getId()));
        Assert.assertTrue(ids.contains(etdl.response6.getId()));

        responses = responseDao.getEvaluationResponses(evaluationIds, null, null, true);
        Assert.assertNotNull(responses);
        Assert.assertEquals(6, responses.size());

        Assert.assertEquals(2, responseDao.countEvaluationResponses(
                evaluationIds, EvalTestDataLoad.USER_ID, new String[] {EvalTestDataLoad.SITE2_REF}, true));
        Assert.assertEquals(6, responseDao.countEvaluationResponses(evaluationIds, null, null, true));
        Assert.assertEquals(0, responseDao.countEvaluationResponses(evaluationIds, EvalTestDataLoad.USER_ID, null, false));
    }

}

