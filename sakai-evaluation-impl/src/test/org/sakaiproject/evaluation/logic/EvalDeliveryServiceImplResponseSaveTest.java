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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.jobmonitor.JobStatusReporter;
import org.sakaiproject.evaluation.logic.exceptions.ResponseSaveException;
import org.sakaiproject.evaluation.logic.externals.EvalSecurityChecksImpl;
import org.sakaiproject.evaluation.logic.model.EvalEmailMessage;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.mocks.MockExternalHierarchyLogic;

/**
 * Focused response-save regression tests for the GenericDAO removal.
 */
public class EvalDeliveryServiceImplResponseSaveTest extends BaseTestEvalLogic {

    private EvalDeliveryServiceImpl deliveryService;
    private EvalSettings settings;
    private RecordingEmailsLogic emailsLogic;

    @Before
    @Override
    public void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();

        settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
        EvalEvaluationService evaluationService = (EvalEvaluationService) applicationContext.getBean(
                "org.sakaiproject.evaluation.logic.EvalEvaluationService");
        EvalSecurityChecksImpl securityChecks = (EvalSecurityChecksImpl) applicationContext.getBean(
                "org.sakaiproject.evaluation.logic.externals.EvalSecurityChecks");

        EvalAuthoringServiceImpl authoringService = new EvalAuthoringServiceImpl();
        authoringService.setDao(evaluationDao);
        authoringService.setCommonLogic(commonLogic);
        authoringService.setSettings(settings);
        authoringService.setSecurityChecks(securityChecks);

        settings.set(EvalSettings.ENABLE_SUBMISSION_CONFIRMATION_EMAIL, Boolean.FALSE);

        emailsLogic = new RecordingEmailsLogic();

        deliveryService = new EvalDeliveryServiceImpl();
        deliveryService.setDao(evaluationDao);
        deliveryService.setCommonLogic(commonLogic);
        deliveryService.setEvaluationService(evaluationService);
        deliveryService.setSettings(settings);
        deliveryService.setAuthoringService(authoringService);
        deliveryService.setHierarchyLogic(new MockExternalHierarchyLogic());
        deliveryService.setEmailsLogic(emailsLogic);
    }

    @Test
    public void testSaveResponsePartialThenComplete() {
        EvalResponse response = new EvalResponse(
                EvalTestDataLoad.STUDENT_USER_ID,
                EvalTestDataLoad.SITE1_REF,
                etdl.evaluationActiveUntaken,
                new Date());

        deliveryService.saveResponse(response, EvalTestDataLoad.STUDENT_USER_ID);

        Assert.assertNotNull(response.getId());
        Assert.assertNull(response.getEndTime());
        Assert.assertTrue(response.getAnswers().isEmpty());

        response.setEndTime(new Date());
        response.setAnswers(new HashSet<>());
        EvalAnswer answer = new EvalAnswer(response, etdl.templateItem1P, etdl.item1, null, null, "service response text");
        response.getAnswers().add(answer);

        deliveryService.saveResponse(response, EvalTestDataLoad.STUDENT_USER_ID);

        Assert.assertNotNull(answer.getId());
        Assert.assertNotNull(evaluationDao.findById(EvalResponse.class, response.getId()));
        Assert.assertNotNull(evaluationDao.findById(EvalAnswer.class, answer.getId()));
    }

    @Test
    public void testSaveResponseRejectsWrongOwnerUpdate() {
        EvalResponse response = new EvalResponse(
                EvalTestDataLoad.STUDENT_USER_ID,
                EvalTestDataLoad.SITE1_REF,
                etdl.evaluationActiveUntaken,
                new Date());
        deliveryService.saveResponse(response, EvalTestDataLoad.STUDENT_USER_ID);

        response.setEndTime(new Date());
        try {
            deliveryService.saveResponse(response, EvalTestDataLoad.USER_ID);
            Assert.fail("Wrong user should not be able to update another user's response");
        } catch (SecurityException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testSaveResponseRejectsMissingRequiredAnswers() {
        EvalEvaluation evaluation = createActiveAssignedEvaluation("service required response", Boolean.FALSE);
        settings.set(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED, null);

        EvalResponse response = new EvalResponse(
                EvalTestDataLoad.USER_ID,
                EvalTestDataLoad.SITE1_REF,
                evaluation,
                new Date());
        response.setEndTime(new Date());
        response.setAnswers(new HashSet<>());
        response.getAnswers().add(new EvalAnswer(response, etdl.templateItem3U, etdl.item3, null, null, 2));

        try {
            deliveryService.saveResponse(response, EvalTestDataLoad.USER_ID);
            Assert.fail("Missing required answer should prevent completed response save");
        } catch (ResponseSaveException e) {
            Assert.assertEquals(ResponseSaveException.TYPE_MISSING_REQUIRED_ANSWERS, e.type);
        }

        Assert.assertNull(response.getId());
    }

    @Test
    public void testSaveResponseRejectsBlankCompletedResponse() {
        EvalEvaluation evaluation = createActiveAssignedEvaluation("service blank response", Boolean.TRUE);
        settings.set(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED, null);

        EvalResponse response = new EvalResponse(
                EvalTestDataLoad.USER_ID,
                EvalTestDataLoad.SITE1_REF,
                evaluation,
                new Date());
        response.setEndTime(new Date());
        response.setAnswers(new HashSet<>());

        try {
            deliveryService.saveResponse(response, EvalTestDataLoad.USER_ID);
            Assert.fail("Blank completed response should not save when compulsory answers are missing");
        } catch (ResponseSaveException e) {
            Assert.assertEquals(ResponseSaveException.TYPE_BLANK_RESPONSE, e.type);
        }

        Assert.assertNull(response.getId());
    }

    @Test
    public void testSaveResponseConfirmationEmailFailureDoesNotRollbackResponse() {
        settings.set(EvalSettings.ENABLE_SUBMISSION_CONFIRMATION_EMAIL, Boolean.TRUE);
        emailsLogic.throwOnSubmission = true;

        EvalResponse partialResponse = new EvalResponse(
                EvalTestDataLoad.STUDENT_USER_ID,
                EvalTestDataLoad.SITE1_REF,
                etdl.evaluationActiveUntaken,
                new Date());

        deliveryService.saveResponse(partialResponse, EvalTestDataLoad.STUDENT_USER_ID);

        Assert.assertNotNull(partialResponse.getId());
        Assert.assertEquals(0, emailsLogic.submissionConfirmationCount);

        partialResponse.setEndTime(new Date());
        partialResponse.setAnswers(new HashSet<>());
        EvalAnswer answer = new EvalAnswer(
                partialResponse,
                etdl.templateItem1P,
                etdl.item1,
                null,
                null,
                "complete despite email failure");
        partialResponse.getAnswers().add(answer);

        deliveryService.saveResponse(partialResponse, EvalTestDataLoad.STUDENT_USER_ID);

        Assert.assertEquals(1, emailsLogic.submissionConfirmationCount);
        Assert.assertNotNull(partialResponse.getId());
        Assert.assertNotNull(answer.getId());
        Assert.assertNotNull(evaluationDao.findById(EvalResponse.class, partialResponse.getId()));
        Assert.assertNotNull(evaluationDao.findById(EvalAnswer.class, answer.getId()));
    }

    @Test
    public void testSaveResponseMultipleAnswerUpdateClearAndNA() {
        EvalTemplateItem multipleAnswerTemplateItem = createMultipleAnswerTemplateItem();
        EvalEvaluation evaluation = createActiveAssignedEvaluation("service multiple answer response", Boolean.TRUE);

        EvalResponse response = new EvalResponse(
                EvalTestDataLoad.USER_ID,
                EvalTestDataLoad.SITE1_REF,
                evaluation,
                new Date());
        response.setAnswers(new HashSet<>());

        EvalAnswer answer = new EvalAnswer(response, multipleAnswerTemplateItem, multipleAnswerTemplateItem.getItem());
        answer.multipleAnswers = new Integer[] {3, 1};
        response.getAnswers().add(answer);

        deliveryService.saveResponse(response, EvalTestDataLoad.USER_ID);

        Assert.assertNotNull(response.getId());
        Assert.assertNotNull(answer.getId());
        Assert.assertEquals(":1:3:", findPersistedAnswer(answer).getMultiAnswerCode());

        answer.multipleAnswers = new Integer[] {2};
        deliveryService.saveResponse(response, EvalTestDataLoad.USER_ID);

        Assert.assertEquals(":2:", findPersistedAnswer(answer).getMultiAnswerCode());

        answer.setNumeric(null);
        answer.setText(null);
        answer.setMultiAnswerCode(null);
        answer.multipleAnswers = new Integer[0];
        deliveryService.saveResponse(response, EvalTestDataLoad.USER_ID);

        Assert.assertTrue(response.getAnswers().isEmpty());
        Assert.assertNull(evaluationDao.findById(EvalAnswer.class, answer.getId()));

        EvalAnswer notApplicableAnswer = new EvalAnswer(response, multipleAnswerTemplateItem, multipleAnswerTemplateItem.getItem());
        notApplicableAnswer.multipleAnswers = new Integer[] {1, 2};
        notApplicableAnswer.NA = true;
        response.getAnswers().add(notApplicableAnswer);

        deliveryService.saveResponse(response, EvalTestDataLoad.USER_ID);

        EvalAnswer savedNotApplicableAnswer = findPersistedAnswer(notApplicableAnswer);
        Assert.assertEquals(EvalConstants.NA_VALUE, savedNotApplicableAnswer.getNumeric());
        Assert.assertEquals(EvalConstants.NO_MULTIPLE_ANSWER, savedNotApplicableAnswer.getMultiAnswerCode());
    }

    private EvalEvaluation createActiveAssignedEvaluation(String title, Boolean blankResponsesAllowed) {
        EvalEvaluation evaluation = new EvalEvaluation(
                EvalConstants.EVALUATION_TYPE_EVALUATION,
                EvalTestDataLoad.MAINT_USER_ID,
                title,
                null,
                etdl.yesterday,
                etdl.tomorrow,
                etdl.tomorrow,
                etdl.threeDaysFuture,
                false,
                null,
                false,
                null,
                EvalConstants.EVALUATION_STATE_ACTIVE,
                EvalConstants.SHARING_VISIBLE,
                EvalConstants.INSTRUCTOR_OPT_IN,
                0,
                null,
                null,
                null,
                null,
                etdl.templateUnused,
                null,
                blankResponsesAllowed,
                Boolean.TRUE,
                Boolean.FALSE,
                Boolean.FALSE,
                EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ,
                null,
                null);
        evaluationDao.save(evaluation);

        EvalAssignGroup assignGroup = new EvalAssignGroup(
                EvalTestDataLoad.MAINT_USER_ID,
                EvalTestDataLoad.SITE1_REF,
                EvalConstants.GROUP_TYPE_SITE,
                evaluation,
                Boolean.TRUE,
                Boolean.TRUE,
                Boolean.FALSE);
        evaluationDao.save(assignGroup);

        EvalAssignUser assignUser = new EvalAssignUser(
                EvalTestDataLoad.USER_ID,
                EvalTestDataLoad.SITE1_REF,
                EvalTestDataLoad.MAINT_USER_ID,
                EvalAssignUser.TYPE_EVALUATOR,
                EvalAssignUser.STATUS_LINKED,
                evaluation,
                assignGroup.getId());
        evaluationDao.save(assignUser);
        EvalAssignUser evaluateeUser = new EvalAssignUser(
                EvalTestDataLoad.MAINT_USER_ID,
                EvalTestDataLoad.SITE1_REF,
                EvalTestDataLoad.MAINT_USER_ID,
                EvalAssignUser.TYPE_EVALUATEE,
                EvalAssignUser.STATUS_LINKED,
                evaluation,
                assignGroup.getId());
        evaluationDao.save(evaluateeUser);
        return evaluation;
    }

    private EvalTemplateItem createMultipleAnswerTemplateItem() {
        EvalItem multipleAnswerItem = new EvalItem(
                EvalTestDataLoad.MAINT_USER_ID,
                "Multiple answer regression item",
                EvalConstants.SHARING_PRIVATE,
                EvalConstants.ITEM_TYPE_MULTIPLEANSWER,
                Boolean.FALSE);
        multipleAnswerItem.setScale(etdl.scale1);
        multipleAnswerItem.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
        multipleAnswerItem.setUsesNA(Boolean.TRUE);
        multipleAnswerItem.setLocked(Boolean.FALSE);
        evaluationDao.save(multipleAnswerItem);

        EvalTemplateItem templateItem = new EvalTemplateItem(
                EvalTestDataLoad.MAINT_USER_ID,
                etdl.templateUnused,
                multipleAnswerItem,
                99,
                EvalConstants.ITEM_CATEGORY_COURSE,
                EvalConstants.HIERARCHY_LEVEL_TOP,
                EvalConstants.HIERARCHY_NODE_ID_NONE,
                null,
                null,
                Boolean.TRUE,
                Boolean.FALSE,
                Boolean.FALSE,
                Boolean.FALSE,
                null,
                null);
        evaluationDao.save(templateItem);
        etdl.templateUnused.getTemplateItems().add(templateItem);
        return templateItem;
    }

    private EvalAnswer findPersistedAnswer(EvalAnswer answer) {
        EvalAnswer persistedAnswer = (EvalAnswer) evaluationDao.findById(EvalAnswer.class, answer.getId());
        Assert.assertNotNull(persistedAnswer);
        return persistedAnswer;
    }

    private static final class RecordingEmailsLogic implements EvalEmailsLogic {

        private int submissionConfirmationCount;
        private boolean throwOnSubmission;

        public String sendEvalSubmissionConfirmationEmail(String userId, Long evalId) {
            submissionConfirmationCount++;
            if (throwOnSubmission) {
                throw new RuntimeException("expected confirmation email failure");
            }
            return userId + "@example.edu";
        }

        public String[] sendEmailMessages(String message, String subject, Long evaluationId, String[] evalGroupIds,
                String includeConstant) {
            return new String[0];
        }

        public EvalEmailMessage makeEmailMessage(String messageTemplate, String subjectTemplate, EvalEvaluation eval,
                EvalGroup group) {
            return null;
        }

        public EvalEmailMessage makeEmailMessage(String messageTemplate, String subjectTemplate, EvalEvaluation eval,
                EvalGroup group, String includeConstant) {
            return null;
        }

        public String[] sendEvalCreatedNotifications(Long evaluationId, boolean includeOwner) {
            return new String[0];
        }

        public String[] sendEvalAvailableNotifications(Long evaluationId, boolean includeEvaluatees) {
            return new String[0];
        }

        public String[] sendEvalAvailableGroupNotification(Long evaluationId, String evalGroupId) {
            return new String[0];
        }

        public String[] sendEvalReminderNotifications(Long evaluationId, String includeConstant) {
            return new String[0];
        }

        public String[] sendEvalResultsNotifications(Long evaluationId, boolean includeEvaluatees,
                boolean includeAdmins, String jobType) {
            return new String[0];
        }

        public String[] sendConsolidatedReminderNotifications(JobStatusReporter jobStatusReporter, String jobId) {
            return new String[0];
        }

        public String[] sendConsolidatedAvailableNotifications(JobStatusReporter jobStatusReporter, String jobId) {
            return new String[0];
        }
    }
}
