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
 * Testing for the Evaluation Data Access Layer
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@ContextConfiguration(locations={
		"/hibernate-test.xml",
		"classpath:org/sakaiproject/evaluation/spring-hibernate.xml"})
@Slf4j
public class EvaluationDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {

    protected EvaluationDao evaluationDao;

    private EvalTestDataLoad etdl;

    private EvalScale scaleLocked;
    private EvalItem itemLocked;
    private EvalItem itemUnlocked;
    private EvalEvaluation evalUnLocked;

    protected static final long MILLISECONDS_PER_DAY = 24L * 60L * 60L * 1000L;

    // run this before each test starts
    @Before
    public void onSetUpBeforeTransaction() throws Exception {
        // load the spring created dao class bean from the Spring Application Context
        evaluationDao = (EvaluationDao) applicationContext.getBean("org.sakaiproject.evaluation.dao.EvaluationDao");
        if (evaluationDao == null) {
            throw new NullPointerException("DAO could not be retrieved from spring context");
        }

        // check the preloaded data
        Assert.assertTrue("Error preloading data", evaluationDao.countAll(EvalScale.class) > 0);

        // check the preloaded test data
        Assert.assertTrue("Error preloading test data", evaluationDao.countAll(EvalEvaluation.class) > 0);

        PreloadTestDataImpl ptd = (PreloadTestDataImpl) applicationContext.getBean("org.sakaiproject.evaluation.test.PreloadTestData");
        if (ptd == null) {
            throw new NullPointerException("PreloadTestDataImpl could not be retrieved from spring context");
        }

        // get test objects
        etdl = ptd.getEtdl();

        // preload additional data if desired
        List<String> optionsA = new ArrayList<String>( Arrays.asList("Male", "Female", "Unknown"));
        scaleLocked = new EvalScale(EvalTestDataLoad.ADMIN_USER_ID, "Scale Alpha", EvalConstants.SCALE_MODE_SCALE, 
                EvalConstants.SHARING_PRIVATE, EvalTestDataLoad.NOT_EXPERT, "description", 
                EvalConstants.SCALE_IDEAL_NONE, optionsA, EvalTestDataLoad.LOCKED);
        evaluationDao.save( scaleLocked );

        itemLocked = new EvalItem(EvalTestDataLoad.MAINT_USER_ID, "Header type locked", EvalConstants.SHARING_PRIVATE, 
                EvalConstants.ITEM_TYPE_HEADER, EvalTestDataLoad.NOT_EXPERT);
        itemLocked.setLocked(EvalTestDataLoad.LOCKED);
        evaluationDao.save( itemLocked );

        itemUnlocked = new EvalItem(EvalTestDataLoad.MAINT_USER_ID, "Header type locked", EvalConstants.SHARING_PRIVATE, 
                EvalConstants.ITEM_TYPE_HEADER, EvalTestDataLoad.NOT_EXPERT);
        itemUnlocked.setScale(etdl.scale2);
        itemUnlocked.setScaleDisplaySetting( EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL );
        itemUnlocked.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        itemUnlocked.setLocked(EvalTestDataLoad.UNLOCKED);
        evaluationDao.save( itemUnlocked );

        evalUnLocked = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, EvalTestDataLoad.MAINT_USER_ID, "Eval active not taken", null, 
                etdl.yesterday, etdl.tomorrow, etdl.tomorrow, etdl.threeDaysFuture, false, null,
                false, null, 
                EvalConstants.EVALUATION_STATE_ACTIVE, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_OPT_IN, 1, null, null, null, null,
                etdl.templatePublicUnused, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                EvalTestDataLoad.UNLOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null);

        evaluationDao.save( evalUnLocked );

    }

    /**
     * ADD unit tests below here, use testMethod as the name of the unit test,
     * Note that if a method is overloaded you should include the arguments in the
     * test name like so: testMethodClassInt (for method(Class, int);
     */

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
    public void testGetSharedEntitiesForUser() {
        List<EvalTemplate> l;
        List<Long> ids;

        // test using templates
        String[] props = new String[] { "type" };
        Object[] values = new Object[] { EvalConstants.TEMPLATE_TYPE_STANDARD };
        int[] comparisons = new int[] { EvaluationDao.COMPARE_EQUALS };

        String[] order = new String[] {"sharing","title"};
        String[] options = new String[] {"notHidden"};
        String[] notEmptyOptions = new String[] {"notHidden", "notEmpty"};

        // all templates visible to user
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                EvalTestDataLoad.USER_ID, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, 
                props, values, comparisons, order, options, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
        Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

        // all templates visible to maint user
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                EvalTestDataLoad.MAINT_USER_ID, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, 
                props, values, comparisons, order, options, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(4, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
        Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

        // all templates owned by USER
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                EvalTestDataLoad.USER_ID, new String[] {EvalConstants.SHARING_PRIVATE}, 
                props, values, comparisons, order, options, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));

        // all private templates
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE}, 
                props, values, comparisons, order, options, 0, 0);
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
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE}, 
                props, values, comparisons, order, notEmptyOptions, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(5, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.templateAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUser.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateUserUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateAdminBlock.getId() ));

        // all public templates
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PUBLIC}, 
                props, values, comparisons, order, options, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(3, l.size());
        ids = EvalTestDataLoad.makeIdList(l);
        Assert.assertTrue(ids.contains( etdl.templatePublic.getId() ));
        Assert.assertTrue(ids.contains( etdl.templatePublicUnused.getId() ));
        Assert.assertTrue(ids.contains( etdl.templateEid.getId() ));

        // all templates (admin would use this)
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, 
                props, values, comparisons, order, options, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(11, l.size());

        // all non-empty templates (admin would use this)
        l = evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, 
                props, values, comparisons, order, notEmptyOptions, 0, 0);
        Assert.assertNotNull(l);
        Assert.assertEquals(8, l.size());

        // no templates (no one should do this, it throws an exception)
        try {
            evaluationDao.getSharedEntitiesForUser(EvalTemplate.class, 
                    null, new String[] {}, 
                    props, values, comparisons, order, notEmptyOptions, 0, 0);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testCountSharedEntitiesForUser() {
        int count;

        // test using templates
        String[] props = new String[] { "type" };
        Object[] values = new Object[] { EvalConstants.TEMPLATE_TYPE_STANDARD };
        int[] comparisons = new int[] { EvaluationDao.COMPARE_EQUALS };

        String[] options = new String[] {"notHidden"};
        String[] notEmptyOptions = new String[] {"notHidden", "notEmpty"};

        // all templates visible to user
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                EvalTestDataLoad.USER_ID, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, 
                props, values, comparisons, options);
        Assert.assertEquals(5, count);

        // all templates visible to maint user
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                EvalTestDataLoad.MAINT_USER_ID, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC}, 
                props, values, comparisons, options);
        Assert.assertEquals(4, count);

        // all templates owned by USER
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                EvalTestDataLoad.USER_ID, new String[] {EvalConstants.SHARING_PRIVATE}, 
                props, values, comparisons, options);
        Assert.assertEquals(2, count);

        // all private templates (admin only)
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE}, 
                props, values, comparisons, options);
        Assert.assertEquals(8, count);

        // all private non-empty templates (admin only)
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE}, 
                props, values, comparisons, notEmptyOptions);
        Assert.assertEquals(5, count);

        // all public templates
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PUBLIC}, 
                props, values, comparisons, options);
        Assert.assertEquals(3, count);

        // all templates (admin would use this)
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, 
                props, values, comparisons, options);
        Assert.assertEquals(11, count);

        // all non-empty templates (admin would use this)
        count = evaluationDao.countSharedEntitiesForUser(EvalTemplate.class, 
                null, new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC, EvalConstants.SHARING_SHARED, EvalConstants.SHARING_VISIBLE}, 
                props, values, comparisons, notEmptyOptions);
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
    @Test
    public void testLockScale() {

        // check that locked scale gets unlocked (no locking item)
        Assert.assertTrue(scaleLocked.getLocked() );
        Assert.assertTrue( evaluationDao.lockScale( scaleLocked, Boolean.FALSE ) );
        Assert.assertFalse(scaleLocked.getLocked() );
        // check that unlocking an unlocked scale is not a problem
        Assert.assertFalse( evaluationDao.lockScale( scaleLocked, Boolean.FALSE ) );

        // check that locked scale that is locked by an item cannot be unlocked
        EvalScale scale1 = (EvalScale) evaluationDao.findById(EvalScale.class, etdl.scale1.getId());
        Assert.assertTrue(scale1.getLocked() );
        Assert.assertFalse( evaluationDao.lockScale( scale1, Boolean.FALSE ) );
        Assert.assertTrue(scale1.getLocked() );
        // check that locking a locked scale is not a problem
        Assert.assertFalse( evaluationDao.lockScale( scale1, Boolean.TRUE ) );

        // check that new scale cannot be unlocked
        try {
            evaluationDao.lockScale( 
                    new EvalScale(EvalTestDataLoad.ADMIN_USER_ID, 
                            "new scale", EvalConstants.SCALE_MODE_SCALE, 
                            EvalConstants.SHARING_PRIVATE, Boolean.FALSE),
                            Boolean.FALSE
            );
            Assert.fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#lockItem(org.sakaiproject.evaluation.model.EvalItem, java.lang.Boolean)}.
     */
    @Test
    public void testLockItem() {

        // check that unlocked item gets locked (no scale)
        Assert.assertFalse(etdl.item7.getLocked() );
        Assert.assertTrue( evaluationDao.lockItem( etdl.item7, Boolean.TRUE ) );
        Assert.assertTrue(etdl.item7.getLocked() );

        // check that locked item does nothing bad if locked again (no scale, not used)
        Assert.assertTrue(itemLocked.getLocked() );
        Assert.assertFalse( evaluationDao.lockItem( itemLocked, Boolean.TRUE ) );
        Assert.assertTrue(itemLocked.getLocked() );

        // check that locked item gets unlocked (no scale, not used)
        Assert.assertTrue(itemLocked.getLocked() );
        Assert.assertTrue( evaluationDao.lockItem( itemLocked, Boolean.FALSE ) );
        Assert.assertFalse( itemLocked.getLocked() );

        // check that locked item that is locked by a template cannot be unlocked
        Assert.assertTrue( etdl.item1.getLocked() );
        Assert.assertFalse( evaluationDao.lockItem( etdl.item1, Boolean.FALSE ) );
        Assert.assertTrue( etdl.item1.getLocked() );

        // check that locked item that is locked by a template can be locked without exception
        Assert.assertTrue( etdl.item1.getLocked() );
        Assert.assertFalse( evaluationDao.lockItem( etdl.item1, Boolean.TRUE ) );
        Assert.assertTrue( etdl.item1.getLocked() );

        // verify that associated scale is unlocked
        Assert.assertFalse( itemUnlocked.getScale().getLocked() );

        // check that unlocked item gets locked (scale)
        Assert.assertFalse( itemUnlocked.getLocked() );
        Assert.assertTrue( evaluationDao.lockItem( itemUnlocked, Boolean.TRUE ) );
        Assert.assertTrue( itemUnlocked.getLocked() );

        // verify that associated scale gets locked
        Assert.assertTrue( itemUnlocked.getScale().getLocked() );

        // check that locked item gets unlocked (scale)
        Assert.assertTrue( itemUnlocked.getLocked() );
        Assert.assertTrue( evaluationDao.lockItem( itemUnlocked, Boolean.FALSE ) );
        Assert.assertFalse( itemUnlocked.getLocked() );

        // verify that associated scale gets unlocked
        Assert.assertFalse( itemUnlocked.getScale().getLocked() );

        // check that locked item gets unlocked (scale locked by another item)
        Assert.assertTrue( etdl.item4.getScale().getLocked() );
        Assert.assertTrue( evaluationDao.lockItem( etdl.item4, Boolean.TRUE ) );
        Assert.assertTrue( etdl.item4.getLocked() );

        Assert.assertTrue( evaluationDao.lockItem( etdl.item4, Boolean.FALSE ) );
        Assert.assertFalse( etdl.item4.getLocked() );

        // verify that associated scale does not get unlocked
        Assert.assertTrue( etdl.item4.getScale().getLocked() );

        // check that new item cannot be locked/unlocked
        try {
            evaluationDao.lockItem(
                    new EvalItem( EvalTestDataLoad.ADMIN_USER_ID, "something", 
                            EvalConstants.SHARING_PRIVATE, EvalConstants.ITEM_TYPE_HEADER, 
                            Boolean.FALSE),
                            Boolean.TRUE);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#lockTemplate(org.sakaiproject.evaluation.model.EvalTemplate, java.lang.Boolean)}.
     */
    @Test
    public void testLockTemplate() {

        // check that unlocked template gets locked (no items)
        Assert.assertFalse( etdl.templateAdminNoItems.getLocked() );
        Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateAdminNoItems, Boolean.TRUE ) );
        Assert.assertTrue( etdl.templateAdminNoItems.getLocked() );

        // check that locked template is ok with getting locked again (no problems)
        Assert.assertTrue( etdl.templateAdminNoItems.getLocked() );
        Assert.assertFalse( evaluationDao.lockTemplate( etdl.templateAdminNoItems, Boolean.TRUE ) );
        Assert.assertTrue( etdl.templateAdminNoItems.getLocked() );

        // check that locked template gets unlocked (no items)
        Assert.assertTrue( etdl.templateAdminNoItems.getLocked() );
        Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateAdminNoItems, Boolean.FALSE ) );
        Assert.assertFalse( etdl.templateAdminNoItems.getLocked() );

        // check that locked template that is locked by an evaluation cannot be unlocked
        Assert.assertTrue( etdl.templateUser.getLocked() );
        Assert.assertFalse( evaluationDao.lockTemplate( etdl.templateUser, Boolean.FALSE ) );
        Assert.assertTrue( etdl.templateUser.getLocked() );

        // check that locked template that is locked by an evaluation can be locked without exception
        Assert.assertTrue( etdl.templateUser.getLocked() );
        Assert.assertFalse( evaluationDao.lockTemplate( etdl.templateUser, Boolean.TRUE ) );
        Assert.assertTrue( etdl.templateUser.getLocked() );

        // check that unlocked template gets locked (items)
        Assert.assertFalse( etdl.item6.getLocked() );
        Assert.assertFalse( etdl.templateUserUnused.getLocked() );
        Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateUserUnused, Boolean.TRUE ) );
        Assert.assertTrue( etdl.templateUserUnused.getLocked() );

        // verify that related items are locked also
        Assert.assertTrue( etdl.item6.getLocked() );

        // check that locked template gets unlocked (items)
        Assert.assertTrue( etdl.templateUserUnused.getLocked() );
        Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateUserUnused, Boolean.FALSE ) );
        Assert.assertFalse( etdl.templateUserUnused.getLocked() );

        // verify that related items are unlocked also
        Assert.assertFalse( etdl.item6.getLocked() );

        // check unlocked template with locked items can be locked
        Assert.assertFalse( etdl.templateUnused.getLocked() );
        Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateUnused, Boolean.TRUE ) );
        Assert.assertTrue( etdl.templateUnused.getLocked() );

        // check that locked template gets unlocked (items locked by another template)
        Assert.assertTrue( etdl.item3.getLocked() );
        Assert.assertTrue( etdl.item5.getLocked() );
        Assert.assertTrue( etdl.templateUnused.getLocked() );
        Assert.assertTrue( evaluationDao.lockTemplate( etdl.templateUnused, Boolean.FALSE ) );
        Assert.assertFalse( etdl.templateUnused.getLocked() );

        // verify that associated items locked by other template do not get unlocked
        Assert.assertTrue( etdl.item3.getLocked() );
        Assert.assertTrue( etdl.item5.getLocked() );

        // check that new template cannot be locked/unlocked
        try {
            evaluationDao.lockTemplate(
                    new EvalTemplate(EvalTestDataLoad.ADMIN_USER_ID, EvalConstants.TEMPLATE_TYPE_STANDARD, 
                            "new template one", "description", 
                            EvalConstants.SHARING_PRIVATE, EvalTestDataLoad.NOT_EXPERT, "expert desc", 
                            null, EvalTestDataLoad.LOCKED, false),
                            Boolean.TRUE);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#lockEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)}.
     */
    @Test
    public void testLockEvaluation() {

        // check that unlocked evaluation gets locked
        Assert.assertFalse( etdl.templatePublicUnused.getLocked() );
        Assert.assertFalse( evalUnLocked.getLocked() );
        Assert.assertTrue( evaluationDao.lockEvaluation( evalUnLocked, true ) );
        Assert.assertTrue( evalUnLocked.getLocked() );

        // verify that associated template gets locked
        Assert.assertTrue( etdl.templatePublicUnused.getLocked() );

        // now unlock the evaluation
        Assert.assertTrue( evalUnLocked.getLocked() );
        Assert.assertTrue( evaluationDao.lockEvaluation( evalUnLocked, false ) );
        Assert.assertFalse( evalUnLocked.getLocked() );

        // verify that associated template gets unlocked
        Assert.assertFalse( etdl.templatePublicUnused.getLocked() );

        // check that new evaluation cannot be locked
        try {
            evaluationDao.lockEvaluation(
                    new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, EvalTestDataLoad.MAINT_USER_ID, "Eval new", null, 
                            etdl.tomorrow, etdl.threeDaysFuture, etdl.threeDaysFuture, etdl.fourDaysFuture, false, null,
                            false, null, 
                            EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.SHARING_VISIBLE, EvalConstants.INSTRUCTOR_OPT_IN, 1, null, null, null, null,
                            etdl.templatePublic, null, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE,
                            EvalTestDataLoad.UNLOCKED, EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ, null, null),
                            true
            );
            Assert.fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#isUsedScale(java.lang.Long)}.
     */
    @Test
    public void testIsUsedScale() {
        Assert.assertTrue( evaluationDao.isUsedScale( etdl.scale1.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedScale( etdl.scale2.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedScale( etdl.scale3.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedScale( etdl.scale4.getId() ) );
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#isUsedItem(java.lang.Long)}.
     */
    @Test
    public void testIsUsedItem() {
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item1.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item2.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item3.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedItem( etdl.item4.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item5.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item6.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedItem( etdl.item7.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedItem( etdl.item8.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item9.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedItem( etdl.item10.getId() ) );
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.dao.EvaluationDaoImpl#isUsedTemplate(java.lang.Long)}.
     */
    @Test
    public void testIsUsedTemplate() {
        Assert.assertTrue( evaluationDao.isUsedTemplate( etdl.templateAdmin.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateAdminBlock.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateAdminComplex.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateAdminNoItems.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedTemplate( etdl.templatePublic.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedTemplate( etdl.templatePublicUnused.getId() ) ); // used in this file
        Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateUnused.getId() ) );
        Assert.assertTrue( evaluationDao.isUsedTemplate( etdl.templateUser.getId() ) );
        Assert.assertFalse( evaluationDao.isUsedTemplate( etdl.templateUserUnused.getId() ) );
    }

    @Test
    public void testObtainLock() {
        // check I can get a lock
        Assert.assertTrue( evaluationDao.obtainLock("AZ.my.lock", "AZ1", 100) );

        // check someone else cannot get my lock
        Assert.assertFalse( evaluationDao.obtainLock("AZ.my.lock", "AZ2", 100) );

        // check I can get my own lock again
        Assert.assertTrue( evaluationDao.obtainLock("AZ.my.lock", "AZ1", 100) );

        // allow the lock to expire
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // nothing here but a Assert.fail
            Assert.fail("sleep interrupted?");
        }

        // check someone else can get my lock
        Assert.assertTrue( evaluationDao.obtainLock("AZ.my.lock", "AZ2", 100) );

        // check invalid arguments cause Assert.failure
        try {
            evaluationDao.obtainLock("AZ.my.lock", null, 1000);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        try {
            evaluationDao.obtainLock(null, "AZ1", 1000);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testReleaseLock() {

        // check I can get a lock
        Assert.assertTrue( evaluationDao.obtainLock("AZ.R.lock", "AZ1", 1000) );

        // check someone else cannot get my lock
        Assert.assertFalse( evaluationDao.obtainLock("AZ.R.lock", "AZ2", 1000) );

        // check I can release my lock
        Assert.assertTrue( evaluationDao.releaseLock("AZ.R.lock", "AZ1") );

        // check someone else can get my lock now
        Assert.assertTrue( evaluationDao.obtainLock("AZ.R.lock", "AZ2", 1000) );

        // check I cannot get the lock anymore
        Assert.assertFalse( evaluationDao.obtainLock("AZ.R.lock", "AZ1", 1000) );

        // check they can release it
        Assert.assertTrue( evaluationDao.releaseLock("AZ.R.lock", "AZ2") );

        // check invalid arguments cause Assert.failure
        try {
            evaluationDao.releaseLock("AZ.R.lock", null);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        try {
            evaluationDao.releaseLock(null, "AZ1");
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testGetConsolidatedEmailMapping() {

        // when no emails have been sent, selecting email recipients in any of several ways should return 1 
        int count = this.evaluationDao.selectConsolidatedEmailRecipients(true, (Date) null, false, (Date) null, EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE);
        Assert.assertEquals(1, count);
        int deletions = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(1, deletions);
        count = this.evaluationDao.selectConsolidatedEmailRecipients(true, new Date(), false, (Date) null, EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE);
        Assert.assertEquals(1, count);
        deletions = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(1, deletions);

        // there should be two new evals ready to send announcements to, selected because the value of availableEmailSent is null
        int count1 = this.evaluationDao.selectConsolidatedEmailRecipients(true, (Date) null, false, (Date) null, EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE);
        Assert.assertEquals(1,count1);
        List<Map<String,Object>> mapping1 = this.evaluationDao.getConsolidatedEmailMapping(true, 100, 0);
        Assert.assertNotNull(mapping1);
        Assert.assertEquals(1, mapping1.size());
        int deletions1 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(1, deletions1);

        // Since those announcements have been sent, there should be none yet to be sent
        int count2 = this.evaluationDao.selectConsolidatedEmailRecipients(true, (Date) null, false, (Date) null, EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE);
        Assert.assertEquals(0,count2);
        List<Map<String,Object>> mapping2 = this.evaluationDao.getConsolidatedEmailMapping(true, 100, 0);
        Assert.assertNotNull(mapping2);
        Assert.assertEquals(0, mapping2.size());
        int deletions2 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(0, deletions2);

        // if we search for notices to be sent and ignore the date, we should find them again
        int count3 = this.evaluationDao.selectConsolidatedEmailRecipients(false, (Date) null, false, (Date) null, EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE);
        Assert.assertEquals(1,count3);
        List<Map<String,Object>> mapping3 = this.evaluationDao.getConsolidatedEmailMapping(true, 100, 0);
        Assert.assertNotNull(mapping3);
        Assert.assertEquals(1, mapping3.size());
        int deletions3 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(1, deletions3);

        // if we search for evals needing reminders based on whether an announcement or reminder has been sent in the past day, we should find none 
        int count4 = this.evaluationDao.selectConsolidatedEmailRecipients(true, new Date(), true, new Date(), EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER);
        Assert.assertEquals(0,count4);
        List<Map<String,Object>> mapping4 = this.evaluationDao.getConsolidatedEmailMapping(false, 100, 0);
        Assert.assertNotNull(mapping4);
        Assert.assertEquals(0, mapping4.size());
        int deletions4 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(0, deletions4);

        // if we search for evals needing reminders based on whether a reminder has been sent in the past day (ignoring when announcements were sent) we find 2
        int count5 = this.evaluationDao.selectConsolidatedEmailRecipients(false, (Date) null, true, new Date(), EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER);
        Assert.assertEquals(1,count5);
        List<Map<String,Object>> mapping5 = this.evaluationDao.getConsolidatedEmailMapping(false, 100, 0);
        Assert.assertNotNull(mapping5);
        Assert.assertEquals(1, mapping5.size());
        int deletions5 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(1, deletions5);

        // if we do the same search again, we find 0 because they have just been sent 
        int count6 = this.evaluationDao.selectConsolidatedEmailRecipients(false, (Date) null, true, new Date(), EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER);
        Assert.assertEquals(0,count6);
        List<Map<String,Object>> mapping6 = this.evaluationDao.getConsolidatedEmailMapping(false, 100, 0);
        Assert.assertNotNull(mapping6);
        Assert.assertEquals(0, mapping6.size());
        int deletions6 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(0, deletions6);

        // if we search for evals needing reminders as if it were tomorrow, we should find 1
        int count7 = this.evaluationDao.selectConsolidatedEmailRecipients(false, (Date) null, true, new Date(System.currentTimeMillis() + MILLISECONDS_PER_DAY), EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER);
        Assert.assertEquals(1,count7);
        List<Map<String,Object>> mapping7 = this.evaluationDao.getConsolidatedEmailMapping(false, 100, 0);
        Assert.assertNotNull(mapping7);
        Assert.assertEquals(1, mapping7.size());
        int deletions7 = this.evaluationDao.resetConsolidatedEmailRecipients();
        Assert.assertEquals(1, deletions7);

        // if we search for evals needing reminders as if it were tomorrow, we should find 1
        int count8 = this.evaluationDao.selectConsolidatedEmailRecipients(false, (Date) null, true, new Date(System.currentTimeMillis() + MILLISECONDS_PER_DAY), EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER);
        Assert.assertEquals(1,count8);
        List<EvalEmailProcessingData> list = evaluationDao.findAll(EvalEmailProcessingData.class);
        Assert.assertNotNull(list);
        Assert.assertEquals(1,list.size());

        // now if we reset the queue and update "completedDate" for the EvalAssignUser associated with that user and eval, we should find no evals needing notifications
        this.evaluationDao.resetConsolidatedEmailRecipients();
        EvalEmailProcessingData eepd = list.get(0);
        // update EvalAssignUser with completed time
        EvalEvaluation evaluation = evaluationDao.findById(EvalEvaluation.class, eepd.getEvalId());
        List<EvalAssignUser> eaus = evaluationDao.findAll(EvalAssignUser.class);
        Assert.assertNotNull(eaus);
        List<EvalAssignUser> matchingAssignUsers = evaluationDao.getParticipantsForEval(evaluation.getId(), eepd.getUserId(),
                new String[] {eepd.getGroupId()}, null, EvalEvaluationService.STATUS_ANY, null, null);
        Assert.assertEquals(1, matchingAssignUsers.size());
        EvalAssignUser eau = matchingAssignUsers.get(0);
        Assert.assertNotNull(eau);
        Long eauId = eau.getId();
        Assert.assertNotNull(eauId);
        eau.setCompletedDate(new Date());
        evaluationDao.update(eau);
        EvalAssignUser eau0 = evaluationDao.findById(EvalAssignUser.class, eau.getId());
        Assert.assertNotNull(eau0);
        Assert.assertNotNull(eau0.getCompletedDate());
        int count9 = this.evaluationDao.selectConsolidatedEmailRecipients(false, (Date) null, true, new Date(System.currentTimeMillis() + MILLISECONDS_PER_DAY), EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER);
        Assert.assertEquals(0,count9);
        List<Map<String,Object>> mapping9 = this.evaluationDao.getConsolidatedEmailMapping(false, 100, 0);
        Assert.assertNotNull(mapping9);
        Assert.assertEquals(0, mapping9.size());
    }

    /**
     * testResponsesSavedInProgress checks to see how many responses have been saved but not submitted
     * for both active and inactive evaluations.  This depends on evaluations being open or closed and 
     * having an incomplete response associated with it.
     */
    @Test
    public void testGetResponsesSavedInProgress() {
        // add an incomplete response to an active evaluation
        EvalResponse responseActive = etdl.response1;
        EvalEvaluation evalActive = etdl.evaluationActive;
        responseActive.setEndTime(null);
        responseActive.setEvaluation(evalActive);
        this.evaluationDao.save(responseActive);    	

        // add an incomplete response to a closed evaluation
        EvalResponse responseInactive = etdl.response2;
        EvalEvaluation evalInactive = etdl.evaluationClosed;
        responseInactive.setEndTime(null);
        responseInactive.setEvaluation(evalInactive);
        this.evaluationDao.save(responseInactive);

        List<EvalResponse> evalOpenResponses = this.evaluationDao.getResponsesSavedInProgress(true);
        Assert.assertNotNull(evalOpenResponses);
        Assert.assertEquals(1, evalOpenResponses.size());
        Assert.assertEquals(evalOpenResponses.get(0).getId(), responseActive.getId());

        List<EvalResponse> evalClosedResponses = this.evaluationDao.getResponsesSavedInProgress(false);
        Assert.assertNotNull(evalClosedResponses);
        Assert.assertEquals(1, evalClosedResponses.size());
        Assert.assertEquals(evalClosedResponses.get(0).getId(), responseInactive.getId());
    }

    /**
     * Add anything that supports the unit tests below here
     */

}
