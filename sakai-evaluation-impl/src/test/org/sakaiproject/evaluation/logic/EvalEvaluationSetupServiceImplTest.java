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
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.exceptions.BlankRequiredFieldException;
import org.sakaiproject.evaluation.logic.exceptions.InvalidDatesException;
import org.sakaiproject.evaluation.logic.externals.EvalSecurityChecksImpl;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.mocks.MockEvalJobLogic;
import org.sakaiproject.evaluation.test.mocks.MockExternalHierarchyLogic;
import org.sakaiproject.genericdao.api.search.Search;


/**
 * Test class for EvalEvaluationSetupServiceImpl
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalEvaluationSetupServiceImplTest extends BaseTestEvalLogic {

    protected EvalEvaluationSetupServiceImpl evaluationSetupService;
    private EvalEvaluationService evaluationService;
    private EvalAuthoringService authoringService;

    // run this before each test starts
    @Before
    public void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();

        // load up any other needed spring beans
        EvalSettings settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
        if (settings == null) {
            throw new NullPointerException("EvalSettings could not be retrieved from spring context");
        }

        EvalSecurityChecksImpl securityChecks = 
            (EvalSecurityChecksImpl) applicationContext.getBean("org.sakaiproject.evaluation.logic.externals.EvalSecurityChecks");
        if (securityChecks == null) {
            throw new NullPointerException("EvalSecurityChecksImpl could not be retrieved from spring context");
        }

        EvalBeanUtils evalBeanUtils = (EvalBeanUtils) applicationContext.getBean(EvalBeanUtils.class.getName());
        if (evalBeanUtils == null) {
            throw new NullPointerException("EvalEvaluationService could not be retrieved from spring context");
        }

        evaluationService = (EvalEvaluationService) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalEvaluationService");
        if (evaluationService == null) {
            throw new NullPointerException("EvalEvaluationService could not be retrieved from spring context");
        }

        // setup the mock objects if needed
        EvalEmailsLogicImpl emailsLogicImpl = new EvalEmailsLogicImpl();
        emailsLogicImpl.setEvaluationService(evaluationService);
        emailsLogicImpl.setCommonLogic(commonLogic);
        emailsLogicImpl.setSettings(settings);

        // create the other needed logic impls
        EvalAuthoringServiceImpl authoringServiceImpl = new EvalAuthoringServiceImpl();
        authoringServiceImpl.setDao(evaluationDao);
        authoringServiceImpl.setCommonLogic(commonLogic);
        authoringServiceImpl.setSecurityChecks(securityChecks);
        authoringServiceImpl.setSettings(settings);
        authoringService = authoringServiceImpl;


        // create and setup the object to be tested
        evaluationSetupService = new EvalEvaluationSetupServiceImpl();
        evaluationSetupService.setDao(evaluationDao);
        evaluationSetupService.setCommonLogic(commonLogic);
        evaluationSetupService.setHierarchyLogic( new MockExternalHierarchyLogic() );
        evaluationSetupService.setSettings(settings);
        evaluationSetupService.setSecurityChecks(securityChecks);
        evaluationSetupService.setEvaluationService(evaluationService);
        evaluationSetupService.setEvalJobLogic( new MockEvalJobLogic() ); // set to the mock object
        evaluationSetupService.setEmails(emailsLogicImpl);
        evaluationSetupService.setAuthoringService(authoringServiceImpl);
        evaluationSetupService.setEvalBeanUtils(evalBeanUtils);

    }

    /**
     * ADD unit tests below here, use testMethod as the name of the unit test,
     * Note that if a method is overloaded you should include the arguments in the
     * test name like so: testMethodClassInt (for method(Class, int);
     */


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#saveEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)}.
     */
    @Test
    public void testSaveEvaluation() {
        EvalEvaluation eval;

        // save a valid evaluation (all dates separate)
        eval = new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
                etdl.today, etdl.tomorrow, etdl.threeDaysFuture, etdl.fourDaysFuture, 
                EvalConstants.EVALUATION_STATE_INQUEUE, 
                EvalConstants.SHARING_VISIBLE, 1, etdl.templatePublic);
        evaluationSetupService.saveEvaluation( eval, EvalTestDataLoad.MAINT_USER_ID, false );
        EvalEvaluation checkEval = evaluationService.getEvaluationById(eval.getId());
        Assert.assertNotNull(checkEval);

        // check that the template was copied
        Assert.assertNotSame(etdl.templatePublic.getId(), eval.getTemplate().getId());
        Assert.assertTrue(eval.getTemplate().isHidden());
        Assert.assertNotNull(eval.getTemplate().getCopyOf());


        // save a valid evaluation in partial state
        EvalEvaluation partialEval = new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
                etdl.today, etdl.tomorrow, etdl.threeDaysFuture, etdl.fourDaysFuture, 
                EvalConstants.EVALUATION_STATE_PARTIAL, 
                EvalConstants.SHARING_VISIBLE, 1, etdl.templatePublic);
        evaluationSetupService.saveEvaluation( partialEval, EvalTestDataLoad.MAINT_USER_ID, false );
        checkEval = evaluationService.getEvaluationById(partialEval.getId());
        Assert.assertNotNull(checkEval);
        Assert.assertEquals(EvalConstants.EVALUATION_STATE_PARTIAL, partialEval.getState());

        // check that the template WAS copied
        EvalTemplate partialTemplate = partialEval.getTemplate();
        Assert.assertNotSame(etdl.templatePublic.getId(), partialTemplate.getId());
        Assert.assertEquals(etdl.templatePublic.getId(), partialTemplate.getCopyOf());
        Assert.assertNotNull(partialTemplate.getCopyOf());
        Assert.assertTrue(partialTemplate.isHidden());

        // now save the partial eval and complete
        evaluationSetupService.saveEvaluation( partialEval, EvalTestDataLoad.MAINT_USER_ID, true );
        Assert.assertEquals(EvalConstants.EVALUATION_STATE_INQUEUE, partialEval.getState());

        // save a valid evaluation (due and stop date identical), and create
        evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
                etdl.today, etdl.tomorrow, etdl.tomorrow, etdl.threeDaysFuture, 
                EvalConstants.EVALUATION_STATE_INQUEUE, 
                EvalConstants.SHARING_VISIBLE, 1, etdl.templatePublic), 
                EvalTestDataLoad.MAINT_USER_ID, true );

        // test view date can be same as due date and stop date can be null
        evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                EvalTestDataLoad.MAINT_USER_ID, "Eval stop date null", 
                etdl.today, etdl.tomorrow, null, etdl.tomorrow,
                EvalConstants.EVALUATION_STATE_INQUEUE, 
                EvalConstants.SHARING_VISIBLE, 1, etdl.templatePublic),
                EvalTestDataLoad.MAINT_USER_ID, false );

        // test start date can be the only one set
        evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                EvalTestDataLoad.MAINT_USER_ID, "Eval start date only", 
                etdl.today, null, null, null,
                EvalConstants.EVALUATION_STATE_INQUEUE, 
                EvalConstants.SHARING_VISIBLE, 1, etdl.templatePublic),
                EvalTestDataLoad.MAINT_USER_ID, true );

        // try to save invalid evaluations

        // try save evaluation with dates that are unset (null), start date must be set
        try {
            evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                    EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
                    null, null, null, null,
                    EvalConstants.EVALUATION_STATE_INQUEUE, 
                    EvalConstants.SHARING_VISIBLE, 1, etdl.templatePublic),
                    EvalTestDataLoad.MAINT_USER_ID, false );
            Assert.fail("Should have thrown exception");
        } catch (BlankRequiredFieldException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals("startDate", e.fieldName);
            //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
        }

        // try save evaluation with dates that are out of order
        // test due date must be after start date
        try {
            evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                    EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
                    etdl.threeDaysFuture, etdl.tomorrow, etdl.tomorrow, etdl.fourDaysFuture, 
                    EvalConstants.EVALUATION_STATE_INQUEUE, 
                    EvalConstants.SHARING_VISIBLE, 1, etdl.templatePublic),
                    EvalTestDataLoad.MAINT_USER_ID, false );
            Assert.fail("Should have thrown exception");
        } catch (InvalidDatesException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals("dueDate", e.dateField);
            //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
        }

        try {
            evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                    EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
                    etdl.tomorrow, etdl.tomorrow, etdl.tomorrow, etdl.fourDaysFuture, 
                    EvalConstants.EVALUATION_STATE_INQUEUE, 
                    EvalConstants.SHARING_VISIBLE, 1, etdl.templatePublic),
                    EvalTestDataLoad.MAINT_USER_ID, false );
            Assert.fail("Should have thrown exception");
        } catch (InvalidDatesException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals("dueDate", e.dateField);
            //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
        }

        // test stop date must be same as or after due date
        try {
            evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                    EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
                    etdl.today, etdl.threeDaysFuture, etdl.tomorrow, etdl.fourDaysFuture, 
                    EvalConstants.EVALUATION_STATE_INQUEUE, 
                    EvalConstants.SHARING_VISIBLE, 1, etdl.templatePublic),
                    EvalTestDataLoad.MAINT_USER_ID, false );
            Assert.fail("Should have thrown exception");
        } catch (InvalidDatesException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals("stopDate", e.dateField);
            //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
        }


        // try save new evaluation with dates that are in the past
        // test start date in the past
        EvalEvaluation testStartEval = new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
                etdl.yesterday, etdl.tomorrow, etdl.threeDaysFuture, etdl.fourDaysFuture,
                EvalConstants.EVALUATION_STATE_INQUEUE, 
                EvalConstants.SHARING_VISIBLE, 1, etdl.templatePublic);
        evaluationSetupService.saveEvaluation( testStartEval, EvalTestDataLoad.MAINT_USER_ID, false );
        Assert.assertNotNull(testStartEval.getId());
        Assert.assertTrue(testStartEval.getStartDate().compareTo(new Date()) <= 0);

        // test due date in the past
        try {
            evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                    EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
                    etdl.yesterday, etdl.yesterday, etdl.tomorrow, etdl.fourDaysFuture,
                    EvalConstants.EVALUATION_STATE_INQUEUE, 
                    EvalConstants.SHARING_VISIBLE, 1, etdl.templatePublic),
                    EvalTestDataLoad.MAINT_USER_ID, false );
            Assert.fail("Should have thrown exception");
        } catch (InvalidDatesException e) {
            Assert.assertNotNull(e);
            Assert.assertEquals("dueDate", e.dateField);
            //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
        }

        // test create eval when do not have permission (USER_ID)
        try {
            evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                    EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
                    etdl.today, etdl.tomorrow, etdl.threeDaysFuture, etdl.fourDaysFuture,
                    EvalConstants.EVALUATION_STATE_INQUEUE, 
                    EvalConstants.SHARING_VISIBLE, 1, etdl.templatePublic),
                    EvalTestDataLoad.USER_ID, false );
            Assert.fail("Should have thrown exception");
        } catch (SecurityException e) {
            Assert.assertNotNull(e);
            //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
        }

        // test saving an evaluation with an empty template Assert.fails
        try {
            evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                    EvalTestDataLoad.ADMIN_USER_ID, "Eval valid title", 
                    etdl.today, etdl.tomorrow, etdl.tomorrow, etdl.threeDaysFuture, 
                    EvalConstants.EVALUATION_STATE_INQUEUE, 
                    EvalConstants.SHARING_VISIBLE, 1, etdl.templateAdminNoItems), 
                    EvalTestDataLoad.ADMIN_USER_ID, false );
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
            //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
        }

        try {
            evaluationSetupService.saveEvaluation( new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                    EvalTestDataLoad.ADMIN_USER_ID, "Eval valid title", 
                    etdl.today, etdl.tomorrow, etdl.tomorrow, etdl.threeDaysFuture, 
                    EvalConstants.EVALUATION_STATE_INQUEUE, 
                    EvalConstants.SHARING_VISIBLE, 1, null), 
                    EvalTestDataLoad.ADMIN_USER_ID, false );
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
            //Assert.fail("Exception: " + e.getMessage()); // see why Assert.failing
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#deleteEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)}.
     */
    @Test
    public void testDeleteEvaluation() {
        // remove evaluation which has not started (uses 2 email templates)
        EvalEvaluation unStarted = evaluationDao.findById(EvalEvaluation.class, etdl.evaluationNew.getId());
        Assert.assertNotNull(unStarted);
        Long availableId = unStarted.getAvailableEmailTemplate().getId();
        Long reminderId = unStarted.getReminderEmailTemplate().getId();
        long countEmailTemplates = evaluationDao.countBySearch(EvalEmailTemplate.class,
                new Search("id", new Long[] {availableId, reminderId}) );
        Assert.assertEquals(2, countEmailTemplates);

        evaluationSetupService.deleteEvaluation(unStarted.getId(), EvalTestDataLoad.MAINT_USER_ID);
        EvalEvaluation eval = evaluationService.getEvaluationById(unStarted.getId());
        Assert.assertNull(eval);

        // check to make sure the associated email templates were also removed
        countEmailTemplates = evaluationDao.countBySearch(EvalEmailTemplate.class, 
                new Search("id", new Long[] {availableId, reminderId}) );
        Assert.assertEquals(0, countEmailTemplates);

        // attempt to remove evaluation which is not owned
        try {
            evaluationSetupService.deleteEvaluation(etdl.evaluationNewAdmin.getId(), EvalTestDataLoad.MAINT_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (SecurityException e) {
            Assert.assertNotNull(e);
        }

        // attempt to remove evaluation with assigned groups (check for cleanup)
        long countACs = evaluationDao.countBySearch(EvalAssignGroup.class, 
                new Search("evaluation.id", etdl.evaluationNewAdmin.getId()) );
        Assert.assertEquals(3, countACs);
        evaluationSetupService.deleteEvaluation(etdl.evaluationNewAdmin.getId(), EvalTestDataLoad.ADMIN_USER_ID);
        eval = evaluationService.getEvaluationById(etdl.evaluationNewAdmin.getId());
        Assert.assertNull(eval);
        countACs = evaluationDao.countBySearch(EvalAssignGroup.class, 
                new Search("evaluation.id", etdl.evaluationNewAdmin.getId()) );
        Assert.assertEquals(0, countACs);

        // attempt to remove evaluation which is active
        try {
            evaluationSetupService.deleteEvaluation(etdl.evaluationActive.getId(), EvalTestDataLoad.MAINT_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

        // attempt to remove evaluation which is completed but no responses (allowed)
        Long evalIdToRemove = etdl.evaluationClosedUntaken.getId();
        evaluationSetupService.deleteEvaluation(evalIdToRemove, EvalTestDataLoad.ADMIN_USER_ID);
        Assert.assertNull( evaluationDao.findById(EvalEvaluation.class, evalIdToRemove) );

        // remove eval with responses (check that it is not actually removed)
        evalIdToRemove = etdl.evaluationClosed.getId();
        evaluationSetupService.deleteEvaluation(evalIdToRemove, EvalTestDataLoad.ADMIN_USER_ID);
        EvalEvaluation deletedEval = evaluationService.getEvaluationById(evalIdToRemove);
        Assert.assertNotNull( deletedEval );
        Assert.assertEquals(EvalConstants.EVALUATION_STATE_DELETED, deletedEval.getState());

        // http://jira.sakaiproject.org/jira/browse/EVALSYS-485 - remove eval and copied template (no responses)
        // first create the evaluation which should copy the template
        EvalEvaluation evalTest485 = new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
                etdl.today, etdl.tomorrow, etdl.threeDaysFuture, etdl.fourDaysFuture, 
                EvalConstants.EVALUATION_STATE_INQUEUE, 
                EvalConstants.SHARING_VISIBLE, 1, etdl.templatePublic);
        evaluationSetupService.saveEvaluation( evalTest485, EvalTestDataLoad.MAINT_USER_ID, false );
        EvalEvaluation checkEval485 = evaluationService.getEvaluationById(evalTest485.getId());
        Assert.assertNotNull(checkEval485);
        Long templateId = checkEval485.getTemplate().getId();

        evalIdToRemove = evalTest485.getId();
        evaluationSetupService.deleteEvaluation(evalIdToRemove, EvalTestDataLoad.ADMIN_USER_ID);
        Assert.assertNull( evaluationService.getEvaluationById(evalIdToRemove) );
        Assert.assertNull( evaluationDao.findById(EvalTemplate.class, templateId) );

        // test for an invalid Eval that it does not cause an exception
        evaluationSetupService.deleteEvaluation( EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.MAINT_USER_ID);

        // http://jira.sakaiproject.org/jira/browse/EVALSYS-545 - removing partial eval will remove copied template as well
        // test that removing an eval with a copied template does not remove the template if still partial
        Long templateId545 = authoringService.copyTemplate(etdl.templatePublic.getId(), "copy 545", EvalTestDataLoad.MAINT_USER_ID, true, false);
        EvalTemplate template545 = authoringService.getTemplateById(templateId545);
        EvalEvaluation evalTest545 = new EvalEvaluation( EvalConstants.EVALUATION_TYPE_EVALUATION, 
                EvalTestDataLoad.MAINT_USER_ID, "Eval valid title", 
                etdl.today, etdl.tomorrow, etdl.threeDaysFuture, etdl.fourDaysFuture, 
                EvalConstants.EVALUATION_STATE_PARTIAL, 
                EvalConstants.SHARING_VISIBLE, 1, template545);
        evaluationSetupService.saveEvaluation( evalTest545, EvalTestDataLoad.MAINT_USER_ID, false ); // partial
        EvalEvaluation checkEval545 = evaluationService.getEvaluationById(evalTest545.getId());
        Assert.assertNotNull(checkEval545);
        Long checkEval545templateId = checkEval545.getTemplate().getId();
        Assert.assertEquals(templateId545, checkEval545templateId);

        evalIdToRemove = evalTest545.getId();
        evaluationSetupService.deleteEvaluation(evalIdToRemove, EvalTestDataLoad.ADMIN_USER_ID);
        Assert.assertNull( evaluationService.getEvaluationById(evalIdToRemove) );
        Assert.assertNotNull( evaluationDao.findById(EvalTemplate.class, checkEval545templateId) );

    }

    @Test
    public void testCloseEvaluation() {
        EvalEvaluation eval;

        // testing closing an active eval
        eval = evaluationSetupService.closeEvaluation(etdl.evaluationActive.getId(), EvalTestDataLoad.MAINT_USER_ID);
        Assert.assertNotNull(eval);
        Assert.assertEquals(etdl.evaluationActive.getId(), eval.getId());
        Assert.assertEquals(EvalConstants.EVALUATION_STATE_CLOSED, eval.getState());
        Assert.assertTrue(eval.getDueDate().getTime() < System.currentTimeMillis());

        // testing closing a viewable eval
        eval = evaluationSetupService.closeEvaluation(etdl.evaluationViewable.getId(), EvalTestDataLoad.MAINT_USER_ID);
        Assert.assertNotNull(eval);
        Assert.assertEquals(etdl.evaluationViewable.getId(), eval.getId());
        Assert.assertEquals(EvalConstants.EVALUATION_STATE_VIEWABLE, eval.getState());

        // test invalid id Assert.fails
        try {
            evaluationSetupService.closeEvaluation(EvalTestDataLoad.INVALID_LONG_ID, EvalTestDataLoad.ADMIN_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#getEvaluationsForUser(String, Boolean, Boolean, Boolean)}.
     */
    @Test
    public void testGetEvaluationsForUser() {
        List<EvalEvaluation> evals;
        List<Long> ids;

        // testing instructor approval
        EvalAssignGroup eag = (EvalAssignGroup) evaluationDao.findById(EvalAssignGroup.class, etdl.assign5.getId());
        eag.setInstructorApproval(false);
        evaluationDao.save(eag);

        // get all evaluations for user
        evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.USER_ID, null, null, true);
        Assert.assertNotNull(evals);
        Assert.assertEquals(6, evals.size());
        ids = EvalTestDataLoad.makeIdList(evals);
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosedUntaken.getId() ));

        // check sorting
        Date lastDate = null;
        for (int i=0; i<evals.size(); i++) {
            EvalEvaluation eval = (EvalEvaluation) evals.get(i);
            if (lastDate == null) {
                lastDate = eval.getDueDate();
            } else {
                if (lastDate.compareTo(eval.getDueDate()) <= 0) {
                    lastDate = eval.getDueDate();
                } else {
                    Assert.fail("Order Assert.failure:" + lastDate + " less than " + eval.getDueDate());
                }
            }
        }

        // test get for another user
        evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.STUDENT_USER_ID, null, null, true);
        Assert.assertNotNull(evals);
        Assert.assertEquals(4, evals.size());
        ids = EvalTestDataLoad.makeIdList(evals);
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

        // get only assigned evals for user
        evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.STUDENT_USER_ID, null, null, null);
        Assert.assertNotNull(evals);
        Assert.assertEquals(2, evals.size());
        ids = EvalTestDataLoad.makeIdList(evals);
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));

        // get all active evaluations for user
        evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.USER_ID, true, null, true);
        Assert.assertNotNull(evals);
        Assert.assertEquals(3, evals.size());
        ids = EvalTestDataLoad.makeIdList(evals);
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationGracePeriod.getId() ));

        // get assigned active evaluations for user
        evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.STUDENT_USER_ID, true, null, null);
        Assert.assertNotNull(evals);
        Assert.assertEquals(0, evals.size());

        // test active evals for another user
        evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.STUDENT_USER_ID, true, null, true);
        Assert.assertNotNull(evals);
        Assert.assertEquals(2, evals.size());
        ids = EvalTestDataLoad.makeIdList(evals);
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationGracePeriod.getId() ));

        // don't include taken evaluations
        evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.USER_ID, true, true, true);
        Assert.assertNotNull(evals);
        Assert.assertEquals(2, evals.size());
        ids = EvalTestDataLoad.makeIdList(evals);
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationGracePeriod.getId() ));

        // try to get for invalid user
        evals = evaluationSetupService.getEvaluationsForUser(EvalTestDataLoad.INVALID_USER_ID, true, null, true);
        Assert.assertNotNull(evals);
        Assert.assertEquals(2, evals.size());
        ids = EvalTestDataLoad.makeIdList(evals);
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationGracePeriod.getId() ));
    }
    
    /**
     * Test method for getEvaluationsForEvaluatee
     */
    @Test
    public void testGetEvaluationsForEvaluatee() {
    	List<EvalEvaluation> evals;
    	
    	evals = this.evaluationSetupService.getEvaluationsForEvaluatee(EvalTestDataLoad.MAINT_USER_ID, null);
    	Assert.assertNotNull(evals);
    	Assert.assertEquals(7, evals.size());
    	
    	for(EvalEvaluation eval : evals) {
            List<EvalAssignUser> assignUsers = evaluationService.getParticipantsForEval(eval.getId(), EvalTestDataLoad.MAINT_USER_ID, 
                    null, EvalAssignUser.TYPE_EVALUATEE, null, null, null);
            Assert.assertNotNull(assignUsers);
    	}

        evals = this.evaluationSetupService.getEvaluationsForEvaluatee(EvalTestDataLoad.MAINT_USER_ID, true);
        Assert.assertNotNull(evals);
        Assert.assertEquals(6, evals.size());

    	evals = this.evaluationSetupService.getEvaluationsForEvaluatee(EvalTestDataLoad.MAINT_USER_ID, false);
        Assert.assertNotNull(evals);
        Assert.assertEquals(4, evals.size());

    	evals = this.evaluationSetupService.getEvaluationsForEvaluatee(EvalTestDataLoad.STUDENT_USER_ID, null);
    	Assert.assertNotNull(evals);
    	Assert.assertEquals(0,evals.size());

    	evals = this.evaluationSetupService.getEvaluationsForEvaluatee(EvalTestDataLoad.USER_ID, null);
    	Assert.assertNotNull(evals);
    	Assert.assertEquals(0,evals.size());
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#getVisibleEvaluationsForUser(java.lang.String, boolean, boolean, boolean)}.
     */
    @Test
    public void testGetVisibleEvaluationsForUser() {
        // test getting visible evals for the maint user
        List<EvalEvaluation> evals;
        List<Long> ids;

        evals = evaluationSetupService.getVisibleEvaluationsForUser(EvalTestDataLoad.MAINT_USER_ID, false, false, false);
        Assert.assertNotNull(evals);
        Assert.assertEquals(4, evals.size());
        ids = EvalTestDataLoad.makeIdList(evals);
        Assert.assertTrue(ids.contains( etdl.evaluationNew.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActive.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationProvided.getId() ));

        // test getting visible evals for the admin user (should be all)
        evals = evaluationSetupService.getVisibleEvaluationsForUser(EvalTestDataLoad.ADMIN_USER_ID, false, false, false);
        Assert.assertNotNull(evals);
        Assert.assertEquals(18, evals.size());

        // test getting recent closed evals for the admin user
        evals = evaluationSetupService.getVisibleEvaluationsForUser(EvalTestDataLoad.ADMIN_USER_ID, true, false, false);
        Assert.assertNotNull(evals);
        Assert.assertEquals(17, evals.size());
        ids = EvalTestDataLoad.makeIdList(evals);
        Assert.assertTrue(! ids.contains( etdl.evaluationViewable.getId() ));

        // test getting visible evals for the normal user (should be none)
        evals = evaluationSetupService.getVisibleEvaluationsForUser(EvalTestDataLoad.USER_ID, false, false, false);
        Assert.assertNotNull(evals);
        Assert.assertEquals(0, evals.size());

    }


    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#getEvalCategories(java.lang.String)}.
     */
    @Test
    public void testGetEvalCategories() {
        String[] cats;

        // get all categories in the system
        cats = evaluationSetupService.getEvalCategories(null);
        Assert.assertNotNull(cats);
        Assert.assertEquals(2, cats.length);
        Assert.assertEquals(EvalTestDataLoad.EVAL_CATEGORY_1, cats[0]);
        Assert.assertEquals(EvalTestDataLoad.EVAL_CATEGORY_2, cats[1]);

        // get all categories for a user
        cats = evaluationSetupService.getEvalCategories(EvalTestDataLoad.ADMIN_USER_ID);
        Assert.assertNotNull(cats);
        Assert.assertEquals(2, cats.length);
        Assert.assertEquals(EvalTestDataLoad.EVAL_CATEGORY_1, cats[0]);
        Assert.assertEquals(EvalTestDataLoad.EVAL_CATEGORY_2, cats[1]);

        cats = evaluationSetupService.getEvalCategories(EvalTestDataLoad.MAINT_USER_ID);
        Assert.assertNotNull(cats);
        Assert.assertEquals(1, cats.length);
        Assert.assertEquals(EvalTestDataLoad.EVAL_CATEGORY_1, cats[0]);

        // get no categories for user with none
        cats = evaluationSetupService.getEvalCategories(EvalTestDataLoad.USER_ID);
        Assert.assertNotNull(cats);
        Assert.assertEquals(0, cats.length);

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#getEvaluationsByCategory(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testGetEvaluationsByCategory() {
        List<EvalEvaluation> evals;
        List<Long> ids;

        // get all evaluationSetupService for a category
        evals = evaluationSetupService.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_1, null);
        Assert.assertNotNull(evals);
        Assert.assertEquals(2, evals.size());
        ids = EvalTestDataLoad.makeIdList(evals);
        Assert.assertTrue(ids.contains( etdl.evaluationNewAdmin.getId() ));
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

        evals = evaluationSetupService.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_2, null);
        Assert.assertNotNull(evals);
        Assert.assertEquals(1, evals.size());
        ids = EvalTestDataLoad.makeIdList(evals);
        Assert.assertTrue(ids.contains( etdl.evaluationClosed.getId() ));

        // get evaluationSetupService for a category and user
        evals = evaluationSetupService.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_1, EvalTestDataLoad.USER_ID);
        Assert.assertNotNull(evals);
        Assert.assertEquals(1, evals.size());
        ids = EvalTestDataLoad.makeIdList(evals);
        Assert.assertTrue(ids.contains( etdl.evaluationActiveUntaken.getId() ));

        evals = evaluationSetupService.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_2, EvalTestDataLoad.USER_ID);
        Assert.assertNotNull(evals);
        Assert.assertEquals(0, evals.size());

        // get evaluationSetupService for invalid or non-existent category
        evals = evaluationSetupService.getEvaluationsByCategory(EvalTestDataLoad.INVALID_CONSTANT_STRING, null);
        Assert.assertNotNull(evals);
        Assert.assertEquals(0, evals.size());

        // get evaluationSetupService for invalid or non-existent user
        evals = evaluationSetupService.getEvaluationsByCategory(EvalTestDataLoad.EVAL_CATEGORY_1, null);
        Assert.assertNotNull(evals);

    }
    
    @Test
    public void testSaveEmailTemplate() {
        // test valid new saves
        evaluationSetupService.saveEmailTemplate( new EvalEmailTemplate( EvalTestDataLoad.MAINT_USER_ID,
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE, "subject", "a message"), 
                EvalTestDataLoad.MAINT_USER_ID);
        evaluationSetupService.saveEmailTemplate( new EvalEmailTemplate( EvalTestDataLoad.ADMIN_USER_ID,
                EvalConstants.EMAIL_TEMPLATE_CREATED, "subject", "another message"), 
                EvalTestDataLoad.ADMIN_USER_ID);

        // test saving new always nulls out the defaultType
        // the defaultType cannot be changed when saving
        // (default templates can only be set in the preloaded data for now)
        EvalEmailTemplate testTemplate = new EvalEmailTemplate( EvalTestDataLoad.ADMIN_USER_ID,
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE, "subject", "a message", 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE);
        evaluationSetupService.saveEmailTemplate( testTemplate, EvalTestDataLoad.ADMIN_USER_ID);
        Assert.assertNotNull( testTemplate.getId() );
        Assert.assertNull( testTemplate.getDefaultType() );

        // test invalid update to default template as non-admin
        EvalEmailTemplate defaultTemplate = 
            evaluationService.getDefaultEmailTemplate( EvalConstants.EMAIL_TEMPLATE_AVAILABLE );
        try {
            defaultTemplate.setMessage("new message for default");
            evaluationSetupService.saveEmailTemplate( defaultTemplate, 
                    EvalTestDataLoad.MAINT_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // ok as admin though
        evaluationSetupService.saveEmailTemplate( defaultTemplate, 
                EvalTestDataLoad.ADMIN_USER_ID);

        // test valid updates
        etdl.emailTemplate1.setMessage("new message 1");
        evaluationSetupService.saveEmailTemplate( etdl.emailTemplate1, EvalTestDataLoad.ADMIN_USER_ID);

        etdl.emailTemplate2.setMessage("new message 2");
        evaluationSetupService.saveEmailTemplate( etdl.emailTemplate2, EvalTestDataLoad.MAINT_USER_ID);

        // test user not has permission to update
        try {
            etdl.emailTemplate1.setMessage("new message 1");
            evaluationSetupService.saveEmailTemplate( etdl.emailTemplate1, 
                    EvalTestDataLoad.MAINT_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        try {
            etdl.emailTemplate2.setMessage("new message 2");
            evaluationSetupService.saveEmailTemplate( etdl.emailTemplate2, 
                    EvalTestDataLoad.USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // test associated eval is active but we can still update (relaxed perms)
        etdl.emailTemplate3.setMessage("new message 3");
        evaluationSetupService.saveEmailTemplate( etdl.emailTemplate3, 
                EvalTestDataLoad.MAINT_USER_ID);

    }

    @Test
    public void testRemoveEmailTemplate() {
        // check user cannot remove
        try {
            evaluationSetupService.removeEmailTemplate(etdl.emailTemplate2.getId(), EvalTestDataLoad.USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // test user can remove
        EvalEvaluation eval = (EvalEvaluation) evaluationDao.findById(EvalEvaluation.class, etdl.evaluationNew.getId());
        Assert.assertNotNull(eval.getReminderEmailTemplate());

        evaluationSetupService.removeEmailTemplate(etdl.emailTemplate2.getId(), EvalTestDataLoad.MAINT_USER_ID);

        Assert.assertNull(eval.getReminderEmailTemplate());
        Assert.assertNull( evaluationDao.findById(EvalEmailTemplate.class, etdl.emailTemplate2.getId()) );

        // test cannot remove default templates
        EvalEmailTemplate defaultTemplate = evaluationService.getDefaultEmailTemplate(EvalConstants.EMAIL_TEMPLATE_AVAILABLE);
        try {
            evaluationSetupService.removeEmailTemplate(defaultTemplate.getId(), EvalTestDataLoad.ADMIN_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

    }



    // GROUP ASSIGNMENTS
    @Ignore
    @Test
    public void testSaveAssignGroup() {

        // test adding evalGroupId to inqueue eval
        EvalAssignGroup eacNew = new EvalAssignGroup(EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF, 
                EvalConstants.GROUP_TYPE_SITE, etdl.evaluationNew, Boolean.FALSE, Boolean.TRUE, 
                Boolean.FALSE);
        evaluationSetupService.saveAssignGroup(eacNew, EvalTestDataLoad.MAINT_USER_ID);

        // check save worked
        List<EvalAssignGroup> l = evaluationDao.findBySearch(EvalAssignGroup.class, 
                new Search("evaluation.id", etdl.evaluationNew.getId()) );
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(l.contains(eacNew));

        // test adding evalGroupId to active eval
        EvalAssignGroup eacActive = new EvalAssignGroup(EvalConstants.GROUP_TYPE_SITE, 
                EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE2_REF, 
                etdl.evaluationActive, Boolean.FALSE, Boolean.TRUE, 
                Boolean.FALSE);
        evaluationSetupService.saveAssignGroup(eacActive, EvalTestDataLoad.MAINT_USER_ID);

        // check save worked
        l = evaluationDao.findBySearch(EvalAssignGroup.class, 
                new Search("evaluation.id", etdl.evaluationActive.getId()) );
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(l.contains(eacActive));

        // test modify safe part while active
        EvalAssignGroup testEac1 = (EvalAssignGroup) evaluationDao.
        findById( EvalAssignGroup.class, etdl.assign1.getId() );
        testEac1.setStudentsViewResults( Boolean.TRUE );
        evaluationSetupService.saveAssignGroup(testEac1, EvalTestDataLoad.MAINT_USER_ID);

        // test modify safe part while closed
        EvalAssignGroup testEac2 = (EvalAssignGroup) evaluationDao.
        findById( EvalAssignGroup.class, etdl.assign4.getId() );
        testEac2.setStudentsViewResults( Boolean.TRUE );
        evaluationSetupService.saveAssignGroup(testEac2, EvalTestDataLoad.MAINT_USER_ID);

        // test admin can modify un-owned evalGroupId
        EvalAssignGroup testEac3 = (EvalAssignGroup) evaluationDao.
        findById( EvalAssignGroup.class, etdl.assign6.getId() );
        testEac3.setStudentsViewResults( Boolean.TRUE );
        evaluationSetupService.saveAssignGroup(testEac3, EvalTestDataLoad.ADMIN_USER_ID);


        // test cannot add duplicate evalGroupId to in-queue eval
        try {
            evaluationSetupService.saveAssignGroup( new EvalAssignGroup(
                    EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF, 
                    EvalConstants.GROUP_TYPE_SITE, etdl.evaluationNew, Boolean.FALSE, Boolean.TRUE, 
                    Boolean.FALSE),
                    EvalTestDataLoad.MAINT_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // test cannot add duplicate evalGroupId to active eval
        try {
            evaluationSetupService.saveAssignGroup( new EvalAssignGroup(
                    EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF, 
                    EvalConstants.GROUP_TYPE_SITE, etdl.evaluationActive, Boolean.FALSE, Boolean.TRUE, 
                    Boolean.FALSE),
                    EvalTestDataLoad.MAINT_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // test user without perm cannot add evalGroupId to eval
        try {
            evaluationSetupService.saveAssignGroup( new EvalAssignGroup(
                    EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF, 
                    EvalConstants.GROUP_TYPE_SITE, etdl.evaluationNew, Boolean.FALSE, Boolean.TRUE, 
                    Boolean.FALSE), 
                    EvalTestDataLoad.USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // test cannot add evalGroupId to closed eval
        try {
            evaluationSetupService.saveAssignGroup( new EvalAssignGroup(
                    EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF, 
                    EvalConstants.GROUP_TYPE_SITE, etdl.evaluationViewable, Boolean.FALSE, Boolean.TRUE, 
                    Boolean.FALSE), 
                    EvalTestDataLoad.MAINT_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // test cannot modify non-owned evalGroupId
        try {
            etdl.assign7.setStudentsViewResults( Boolean.TRUE );
            evaluationSetupService.saveAssignGroup(etdl.assign7, EvalTestDataLoad.MAINT_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }


        // TODO - these tests cannot pass right now because of hibernate screwing us -AZ
        //    // test modify evalGroupId while new eval
        //    try {
        //       EvalAssignContext testEac4 = (EvalAssignContext) evaluationDao.
        //          findById( EvalAssignContext.class, etdl.assign6.getId() );
        //       testEac4.setContext( EvalTestDataLoad.CONTEXT3 );
        //       evaluationSetupService.saveAssignContext(testEac4, EvalTestDataLoad.MAINT_USER_ID);
        //       Assert.fail("Should have thrown exception");
        //    } catch (RuntimeException e) {
        //       Assert.assertNotNull(e);
        //    }
        //    
        //    // test modify evalGroupId while active eval
        //    try {
        //       EvalAssignContext testEac5 = (EvalAssignContext) evaluationDao.
        //          findById( EvalAssignContext.class, etdl.assign1.getId() );
        //       testEac5.setContext( EvalTestDataLoad.CONTEXT2 );
        //       evaluationSetupService.saveAssignContext(testEac5, EvalTestDataLoad.MAINT_USER_ID);
        //       Assert.fail("Should have thrown exception");
        //    } catch (RuntimeException e) {
        //       Assert.assertNotNull(e);
        //    }
        //
        //    // test modify evalGroupId while closed eval
        //    try {
        //       EvalAssignContext testEac6 = (EvalAssignContext) evaluationDao.
        //          findById( EvalAssignContext.class, etdl.assign4.getId() );
        //       testEac6.setContext( EvalTestDataLoad.CONTEXT1 );
        //       evaluationSetupService.saveAssignContext(testEac6, EvalTestDataLoad.MAINT_USER_ID);
        //       Assert.fail("Should have thrown exception");
        //    } catch (RuntimeException e) {
        //       Assert.assertNotNull(e);
        //    }

        // TODO - test that evaluation cannot be changed

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#deleteAssignGroup(java.lang.Long, java.lang.String)}.
     */
    @Test
    public void testDeleteAssignGroup() {
        // save some ACs to test removing
        EvalAssignGroup eac1 = new EvalAssignGroup(EvalTestDataLoad.MAINT_USER_ID, EvalTestDataLoad.SITE1_REF, 
                EvalConstants.GROUP_TYPE_SITE, etdl.evaluationNew, Boolean.FALSE, Boolean.TRUE, 
                Boolean.FALSE);
        EvalAssignGroup eac2 = new EvalAssignGroup(EvalTestDataLoad.ADMIN_USER_ID, EvalTestDataLoad.SITE2_REF, 
                EvalConstants.GROUP_TYPE_SITE, etdl.evaluationNew, Boolean.FALSE, Boolean.TRUE, 
                Boolean.FALSE);
        evaluationDao.save(eac1);
        evaluationDao.save(eac2);

        // check save worked
        List<EvalAssignGroup> l = evaluationDao.findBySearch(EvalAssignGroup.class, 
                new Search("evaluation.id", etdl.evaluationNew.getId()) );
        Assert.assertNotNull(l);
        Assert.assertEquals(2, l.size());
        Assert.assertTrue(l.contains(eac1));
        Assert.assertTrue(l.contains(eac2));

        // test can remove contexts from new Evaluation
        Long eacId = eac1.getId();
        evaluationSetupService.deleteAssignGroup( eacId, EvalTestDataLoad.MAINT_USER_ID );

        evaluationSetupService.deleteAssignGroup( etdl.assign8.getId(), EvalTestDataLoad.MAINT_USER_ID );

        // check save worked
        l = evaluationDao.findBySearch(EvalAssignGroup.class, 
                new Search("evaluation.id", etdl.evaluationNew.getId()) );
        Assert.assertNotNull(l);
        Assert.assertEquals(1, l.size());
        Assert.assertTrue(! l.contains(eac1));

        // test cannot remove evalGroupId from active eval
        try {
            evaluationSetupService.deleteAssignGroup( etdl.assign1.getId(), 
                    EvalTestDataLoad.MAINT_USER_ID );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // test cannot remove evalGroupId from closed eval
        try {
            evaluationSetupService.deleteAssignGroup( etdl.assign4.getId(), 
                    EvalTestDataLoad.MAINT_USER_ID );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // test cannot remove evalGroupId without permission
        try {
            evaluationSetupService.deleteAssignGroup( eac2.getId(), 
                    EvalTestDataLoad.USER_ID );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // test cannot remove evalGroupId without ownership
        try {
            evaluationSetupService.deleteAssignGroup( etdl.assign7.getId(), 
                    EvalTestDataLoad.MAINT_USER_ID );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        // test remove invalid eac
        try {
            evaluationSetupService.deleteAssignGroup( EvalTestDataLoad.INVALID_LONG_ID, 
                    EvalTestDataLoad.MAINT_USER_ID );
            Assert.fail("Should have thrown exception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

    }

    @Test
    public void testSetDefaults() {
        EvalAssignGroup eah = new EvalAssignGroup("az", "eag1", "Site", etdl.evaluationActive);

        // make sure it fills in nulls
        Assert.assertNull(eah.getInstructorApproval());
        Assert.assertNull(eah.getInstructorsViewResults());
        Assert.assertNull(eah.getStudentsViewResults());
        evaluationSetupService.setAssignmentDefaults(etdl.evaluationActive, eah);
        Assert.assertNotNull(eah.getInstructorApproval());
        Assert.assertNotNull(eah.getInstructorsViewResults());
        Assert.assertNotNull(eah.getStudentsViewResults());

        // make sure it does not wipe existing settings
        eah = new EvalAssignGroup("az", "eag1", "Site", etdl.evaluationActive, false, false, false);
        evaluationSetupService.setAssignmentDefaults(etdl.evaluationActive, eah);
        // TODO - temporary disable
        //      Assert.assertEquals(Boolean.FALSE, eah.getInstructorApproval());
        Assert.assertEquals(Boolean.FALSE, eah.getInstructorsViewResults());
        Assert.assertEquals(Boolean.FALSE, eah.getStudentsViewResults());

        eah = new EvalAssignGroup("az", "eag1", "Site", etdl.evaluationActive, true, true, true);
        evaluationSetupService.setAssignmentDefaults(etdl.evaluationActive, eah);
        Assert.assertEquals(Boolean.TRUE, eah.getInstructorApproval());
        Assert.assertEquals(Boolean.TRUE, eah.getInstructorsViewResults());
        Assert.assertEquals(Boolean.TRUE, eah.getStudentsViewResults());

    }

    @Test
    public void testAssignEmailTemplate() {
        EvalEvaluation eval;

        // check assigning works
        eval = evaluationService.getEvaluationById(etdl.evaluationActive.getId());
        Assert.assertNull(eval.getAvailableEmailTemplate());
        evaluationSetupService.assignEmailTemplate(etdl.emailTemplate1.getId(), etdl.evaluationActive.getId(), null, EvalTestDataLoad.ADMIN_USER_ID);
        eval = evaluationService.getEvaluationById(etdl.evaluationActive.getId());
        Assert.assertNotNull(eval.getAvailableEmailTemplate());
        Assert.assertEquals(etdl.emailTemplate1.getId(), eval.getAvailableEmailTemplate().getId());

        // check unassigning works
        evaluationSetupService.assignEmailTemplate(null, etdl.evaluationActive.getId(), EvalConstants.EMAIL_TEMPLATE_AVAILABLE, EvalTestDataLoad.ADMIN_USER_ID);
        eval = evaluationService.getEvaluationById(etdl.evaluationActive.getId());
        Assert.assertNull(eval.getAvailableEmailTemplate());

        // check default template unassigns
        EvalEmailTemplate defaultTemplate = evaluationService.getDefaultEmailTemplate(EvalConstants.EMAIL_TEMPLATE_AVAILABLE);
        Assert.assertNotNull(defaultTemplate);
        evaluationSetupService.assignEmailTemplate(defaultTemplate.getId(), etdl.evaluationNew.getId(), null, EvalTestDataLoad.ADMIN_USER_ID);
        eval = evaluationService.getEvaluationById(etdl.evaluationNew.getId());
        Assert.assertNull(eval.getAvailableEmailTemplate());

        // invalid evalid causes Assert.failure
        try {
            evaluationSetupService.assignEmailTemplate(etdl.emailTemplate1.getId(), EvalTestDataLoad.INVALID_LONG_ID, null, EvalTestDataLoad.ADMIN_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

        // invalid argument combination causes Assert.failure
        try {
            evaluationSetupService.assignEmailTemplate(null, etdl.evaluationActive.getId(), null, EvalTestDataLoad.ADMIN_USER_ID);
            Assert.fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#deleteUserAssignments(java.lang.Long, java.lang.Long[])}.
     */
    @Test
    public void testDeleteUserAssignments() {
        externalLogic.setCurrentUserId(EvalTestDataLoad.ADMIN_USER_ID);

        Long evaluationId = etdl.evaluationNewAdmin.getId();
        List<EvalAssignUser> assignUsers = evaluationService.getParticipantsForEval(evaluationId, null, 
                new String[] {EvalTestDataLoad.SITE1_REF}, null, null, null, null);
        Assert.assertNotNull(assignUsers);
        Assert.assertTrue(assignUsers.size() > 1);

        Long[] userAssignmentIds = new Long[assignUsers.size()];
        for (int i = 0; i < userAssignmentIds.length; i++) {
            EvalAssignUser evalAssignUser = assignUsers.get(i);
            userAssignmentIds[i] = evalAssignUser.getId();
        }
        evaluationSetupService.deleteUserAssignments(evaluationId, userAssignmentIds);

        assignUsers = evaluationService.getParticipantsForEval(evaluationId, null, 
                new String[] {EvalTestDataLoad.SITE1_REF}, null, null, null, null);
        Assert.assertNotNull(assignUsers);
        Assert.assertTrue(assignUsers.isEmpty());
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#saveUserAssignments(java.lang.Long, org.sakaiproject.evaluation.model.EvalAssignUser[])}.
     */
    @Test
    public void testSaveUserAssignments() {
        externalLogic.setCurrentUserId(EvalTestDataLoad.MAINT_USER_ID);
        Long evaluationId = etdl.evaluationNew.getId();
        List<EvalAssignUser> currentAssign = evaluationService.getParticipantsForEval(evaluationId, null, 
                new String[] {EvalTestDataLoad.SITE1_REF}, null, null, null, null);
        Assert.assertNotNull(currentAssign);
        Assert.assertTrue(currentAssign.isEmpty());

        EvalAssignUser[] assignUsers = new EvalAssignUser[] { 
                new EvalAssignUser(EvalTestDataLoad.USER_ID, EvalTestDataLoad.SITE1_REF),
                new EvalAssignUser(EvalTestDataLoad.STUDENT_USER_ID, EvalTestDataLoad.SITE1_REF)
        };
        evaluationSetupService.saveUserAssignments(evaluationId, assignUsers);

        currentAssign = evaluationService.getParticipantsForEval(evaluationId, null, 
                new String[] {EvalTestDataLoad.SITE1_REF}, null, null, null, null);
        Assert.assertNotNull(currentAssign);
        Assert.assertTrue(currentAssign.size() == 2);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#synchronizeUserAssignments(java.lang.Long, java.lang.String)}.
     */
    @Ignore
    @Test
    public void testSynchronizeUserAssignments() {
        Long evaluationId;
        List<EvalAssignUser> currentAssign;
        int currentSize;
        long currentEAUs;
        int newSize;
        long newEAUs;

        evaluationId = etdl.evaluationNewAdmin.getId();
        currentAssign = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(currentAssign);
        currentSize = currentAssign.size();
        Assert.assertTrue(currentSize > 0);
        currentEAUs = evaluationDao.countAll(EvalAssignUser.class);
        Assert.assertTrue(currentEAUs > 0);

        evaluationSetupService.synchronizeUserAssignments(evaluationId, null);

        currentAssign = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(currentAssign);
        newSize = currentAssign.size();
        Assert.assertTrue(newSize > 0);
        newEAUs = evaluationDao.countAll(EvalAssignUser.class);
        Assert.assertTrue(newEAUs > 0);

        // make sure the size does not change
        Assert.assertEquals(currentSize, newSize);
        Assert.assertEquals(currentEAUs, newEAUs);

        // also test with an ongoing one
        evaluationId = etdl.evaluationActive.getId();
        currentAssign = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(currentAssign);
        currentSize = currentAssign.size();
        Assert.assertTrue(currentSize > 0);
        currentEAUs = evaluationDao.countAll(EvalAssignUser.class);
        Assert.assertTrue(currentEAUs > 0);

        evaluationSetupService.synchronizeUserAssignments(evaluationId, null);

        currentAssign = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(currentAssign);
        newSize = currentAssign.size();
        Assert.assertTrue(newSize > 0);
        newEAUs = evaluationDao.countAll(EvalAssignUser.class);
        Assert.assertTrue(newEAUs > 0);

        // make sure the size does not change
        Assert.assertEquals(currentSize, newSize);
        Assert.assertEquals(currentEAUs, newEAUs);

        // now try it with a closed one (should Assert.fail)
        evaluationId = etdl.evaluationClosed.getId();
        try {
            evaluationSetupService.synchronizeUserAssignments(evaluationId, null);
            Assert.fail("Should have thrown exception");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e);
        }

    }
    
    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#synchronizeUserAssignments(java.lang.Long, java.lang.String)}.
     */
    @Ignore
    @Test
    public void testSynchronizeUserAssignments_noAssignments_notAllRolesParticipate() {
    	Long evaluationId;
        List<EvalAssignUser> evalEvaluatorAssignments;
        List<EvalAssignUser> evalAllAssignments;
        int evalEvaluatorSize;
        int evalTotalSize;
        long allAssignmentsSize;
        long newAllAssignmentsSize; 
        int newEvalEvaluatorSize;
        long newEvalTotalSize;

        EvalEvaluation testedEvaluation = etdl.evaluation_noAssignments_notAllRolesParticipate;
        
		evaluationId = testedEvaluation.getId();
        evalAllAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(evalAllAssignments);
        evalTotalSize = evalAllAssignments.size();
        evalEvaluatorAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(evalEvaluatorAssignments);
        evalEvaluatorSize = evalEvaluatorAssignments.size();
        
        allAssignmentsSize = evaluationDao.countAll(EvalAssignUser.class);

        evaluationSetupService.synchronizeUserAssignments(evaluationId, null);

        evalAllAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(evalAllAssignments);
        newEvalTotalSize = evalAllAssignments.size();
        
        evalEvaluatorAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(evalEvaluatorAssignments);        
        newEvalEvaluatorSize = evalEvaluatorAssignments.size();
        
        newAllAssignmentsSize = evaluationDao.countAll(EvalAssignUser.class);

        Assert.assertEquals(evalTotalSize, newEvalTotalSize);
        Assert.assertEquals(evalEvaluatorSize, newEvalEvaluatorSize);
        Assert.assertEquals(allAssignmentsSize, newAllAssignmentsSize);
    }
    
    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#synchronizeUserAssignments(java.lang.Long, java.lang.String)}.
     */
    @Ignore
    @Test
    public void testSynchronizeUserAssignments_noAssignments_allRolesParticipate() {
    	Long evaluationId;
        List<EvalAssignUser> evalEvaluatorAssignments;
        List<EvalAssignUser> evalAllAssignments;
        int evalEvaluatorSize;
        int evalTotalSize;
        long allAssignmentsSize;
        long newAllAssignmentsSize; 
        int newEvalEvaluatorSize;
        long newEvalTotalSize;

        EvalEvaluation testedEvaluation = etdl.evaluation_noAssignments_allRolesParticipate;
        
		evaluationId = testedEvaluation.getId();
        evalAllAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(evalAllAssignments);
        evalTotalSize = evalAllAssignments.size();
        evalEvaluatorAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(evalEvaluatorAssignments);
        evalEvaluatorSize = evalEvaluatorAssignments.size();
        
        allAssignmentsSize = evaluationDao.countAll(EvalAssignUser.class);

        evaluationSetupService.synchronizeUserAssignments(evaluationId, null);

        evalAllAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(evalAllAssignments);
        newEvalTotalSize = evalAllAssignments.size();
        
        evalEvaluatorAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(evalEvaluatorAssignments);        
        newEvalEvaluatorSize = evalEvaluatorAssignments.size();
        
        newAllAssignmentsSize = evaluationDao.countAll(EvalAssignUser.class);

        Assert.assertEquals(evalTotalSize, newEvalTotalSize);
        Assert.assertEquals(evalEvaluatorSize, newEvalEvaluatorSize);
        Assert.assertEquals(allAssignmentsSize, newAllAssignmentsSize);
    }
    
    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#synchronizeUserAssignments(java.lang.Long, java.lang.String)}.
     */
    @Ignore
    @Test
    public void testSynchronizeUserAssignments_simpleAssignments_allRolesParticipate() {
        Long evaluationId;
        List<EvalAssignUser> evalEvaluatorAssignments;
        List<EvalAssignUser> evalAllAssignments;
        int evalEvaluatorSize;
        int evalTotalSize;
        long allAssignmentsSize;
        long newAllAssignmentsSize;
        int newEvalEvaluatorSize;
        long newEvalTotalSize;

        evaluationId = etdl.evaluation_simpleAssignments_allRolesParticipate.getId();
        evalAllAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(evalAllAssignments);
        evalTotalSize = evalAllAssignments.size();
        evalEvaluatorAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(evalEvaluatorAssignments);
        evalEvaluatorSize = evalEvaluatorAssignments.size();
        
        allAssignmentsSize = evaluationDao.countAll(EvalAssignUser.class);

        evaluationSetupService.synchronizeUserAssignments(evaluationId, null);

        evalAllAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(evalAllAssignments);
        newEvalTotalSize = evalAllAssignments.size();
        
        evalEvaluatorAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(evalEvaluatorAssignments);        
        newEvalEvaluatorSize = evalEvaluatorAssignments.size();
        
        newAllAssignmentsSize = evaluationDao.countAll(EvalAssignUser.class);

        Assert.assertEquals(evalTotalSize, newEvalTotalSize);
        Assert.assertEquals(evalEvaluatorSize, newEvalEvaluatorSize);
        Assert.assertEquals(allAssignmentsSize, newAllAssignmentsSize);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#synchronizeUserAssignments(java.lang.Long, java.lang.String)}.
     */
    @Ignore
    @Test
    public void testSynchronizeUserAssignments_allRoleAssignments_allRolesParticipate() {
        Long evaluationId;
        List<EvalAssignUser> evalEvaluatorAssignments;
        List<EvalAssignUser> evalAllAssignments;
        int evalEvaluatorSize;
        int evalTotalSize;
        long allAssignmentsSize;
        int newEvalEvaluatorSize;
        long newEvalTotal;
        long newAllAssignmentsSize;

        EvalAssignGroup assignGroup = etdl.assign16;
        
        /*
         * Insert new evaluator as it is not part of the group list
         */
        EvalAssignUser userAssign = new EvalAssignUser(EvalTestDataLoad.MAINT_USER_ID_3, 
        		assignGroup.getEvalGroupId(), assignGroup.getOwner(), 
        		EvalAssignUser.TYPE_EVALUATOR, EvalAssignUser.STATUS_LINKED);
        
        userAssign.setEvaluation(assignGroup.getEvaluation());
        userAssign.setAssignGroupId(assignGroup.getId());
        
        evaluationDao.save(userAssign);
        
        evaluationId = etdl.evaluation_allRoleAssignments_allRolesParticipate.getId();
        evalAllAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(evalAllAssignments);
        evalTotalSize = evalAllAssignments.size();
        evalEvaluatorAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(evalEvaluatorAssignments);
        evalEvaluatorSize = evalEvaluatorAssignments.size();
        
        allAssignmentsSize = evaluationDao.countAll(EvalAssignUser.class);

        
        evaluationSetupService.synchronizeUserAssignments(evaluationId, null);

        evalAllAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(evalAllAssignments);
        newEvalTotal = evalAllAssignments.size();
        
        evalEvaluatorAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(evalEvaluatorAssignments);        
        newEvalEvaluatorSize = evalEvaluatorAssignments.size();
        
        newAllAssignmentsSize = evaluationDao.countAll(EvalAssignUser.class);

        Assert.assertEquals(evalTotalSize -1, newEvalTotal);
        Assert.assertEquals(evalEvaluatorSize-1, newEvalEvaluatorSize);
        Assert.assertEquals(allAssignmentsSize-1, newAllAssignmentsSize);
    }
    
    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#synchronizeUserAssignments(java.lang.Long, java.lang.String)}.
     */
    @Ignore
    @Test
    public void testSynchronizeUserAssignments_simpleAssignments_notAllRolesParticipate() {
    	Long evaluationId;
        List<EvalAssignUser> evalEvaluatorAssignments;
        List<EvalAssignUser> evalAllAssignments;
        int evalEvaluatorSize;
        int evalTotalSize;
        long allAssignmentsSize;
        long newAllAssignmentsSize;
        int newEvalEvaluatorSize;
        long newEvalTotalSize;

        EvalEvaluation testEvalEvaluation = etdl.evaluation_simpleAssignments_notAllRolesParticipate;
        
        evaluationId = testEvalEvaluation.getId();
        evalAllAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(evalAllAssignments);
        evalTotalSize = evalAllAssignments.size();
        evalEvaluatorAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(evalEvaluatorAssignments);
        evalEvaluatorSize = evalEvaluatorAssignments.size();
        
        allAssignmentsSize = evaluationDao.countAll(EvalAssignUser.class);

        evaluationSetupService.synchronizeUserAssignments(evaluationId, null);

        evalAllAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(evalAllAssignments);
        newEvalTotalSize = evalAllAssignments.size();
        
        evalEvaluatorAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(evalEvaluatorAssignments);        
        newEvalEvaluatorSize = evalEvaluatorAssignments.size();
        
        newAllAssignmentsSize = evaluationDao.countAll(EvalAssignUser.class);

        Assert.assertEquals(evalTotalSize, newEvalTotalSize);
        Assert.assertEquals(evalEvaluatorSize, newEvalEvaluatorSize);
        Assert.assertEquals(allAssignmentsSize, newAllAssignmentsSize);
    }
    
    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#synchronizeUserAssignments(java.lang.Long, java.lang.String)}.
     */
    @Ignore
    @Test
    public void testSynchronizeUserAssignments_allRoleAssignments_notAllRolesParticipate() {
        Long evaluationId;
        List<EvalAssignUser> evalEvaluatorAssignments;
        List<EvalAssignUser> evalAllAssignments;
        int evalEvaluatorSize;
        int evalTotalSize;
        long allAssignmentsSize;
        long newAllAssignmentsSize;
        int newEvalEvaluatorSize;
        long newEvalTotalSize;

        EvalAssignGroup assignGroup = etdl.assign17;
        
        /*
         * Insert new evaluator as it is not part of the group list
         */
        EvalAssignUser userAssign = new EvalAssignUser(EvalTestDataLoad.MAINT_USER_ID_3, 
        		assignGroup.getEvalGroupId(), assignGroup.getOwner(), 
        		EvalAssignUser.TYPE_EVALUATOR, EvalAssignUser.STATUS_LINKED);
        
        userAssign.setEvaluation(assignGroup.getEvaluation());
        userAssign.setAssignGroupId(assignGroup.getId());
        
        evaluationDao.save(userAssign);
        
        evaluationId = etdl.evaluation_allRoleAssignments_notAllRolesParticipate.getId();
        
        evalAllAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(evalAllAssignments);
        evalTotalSize = evalAllAssignments.size();
        
        evalEvaluatorAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(evalEvaluatorAssignments);
        evalEvaluatorSize = evalEvaluatorAssignments.size();
        
        allAssignmentsSize = evaluationDao.countAll(EvalAssignUser.class);
     
        evaluationSetupService.synchronizeUserAssignments(evaluationId, null);

        evalAllAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(evalAllAssignments);
        newEvalTotalSize = evalAllAssignments.size();
        
        evalEvaluatorAssignments = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        Assert.assertNotNull(evalEvaluatorAssignments);        
        newEvalEvaluatorSize = evalEvaluatorAssignments.size();
        
        newAllAssignmentsSize = evaluationDao.countAll(EvalAssignUser.class);

        Assert.assertEquals(evalTotalSize - 1, newEvalTotalSize);
        Assert.assertEquals(evalEvaluatorSize - 1, newEvalEvaluatorSize);
        Assert.assertEquals(allAssignmentsSize - 1, newAllAssignmentsSize);
    }        

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#synchronizeUserAssignmentsForced(org.sakaiproject.evaluation.model.EvalEvaluation, java.lang.String, boolean)}.
     */
    @Ignore
    @Test
    public void testSynchronizeUserAssignmentsForced() {
        Long evaluationId;
        EvalEvaluation evaluation;
        List<EvalAssignUser> currentAssign;
        int currentSize;
        long currentEAUs;
        int newSize;
        long newEAUs;

        evaluationId = etdl.evaluationActive.getId();
        currentAssign = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(currentAssign);
        currentSize = currentAssign.size();
        Assert.assertTrue(currentSize > 0);
        currentEAUs = evaluationDao.countAll(EvalAssignUser.class);
        Assert.assertTrue(currentEAUs > 0);

        evaluation = evaluationService.getEvaluationById(evaluationId);
        evaluationSetupService.synchronizeUserAssignmentsForced(evaluation, null, false);

        currentAssign = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(currentAssign);
        newSize = currentAssign.size();
        Assert.assertTrue(newSize > 0);
        newEAUs = evaluationDao.countAll(EvalAssignUser.class);
        Assert.assertTrue(newEAUs > 0);

        // make sure the size does not change
        Assert.assertEquals(currentSize, newSize);
        Assert.assertEquals(currentEAUs, newEAUs);

        // also test with an ongoing one
        evaluationId = etdl.evaluationClosed.getId();
        currentAssign = evaluationService.getParticipantsForEval(evaluationId, null, 
                null, null, null, null, null);
        Assert.assertNotNull(currentAssign);
        currentSize = currentAssign.size();
        Assert.assertTrue(currentSize > 0);
        currentEAUs = evaluationDao.countAll(EvalAssignUser.class);
        Assert.assertTrue(currentEAUs > 0);

        evaluation = evaluationService.getEvaluationById(evaluationId);
        evaluationSetupService.synchronizeUserAssignmentsForced(evaluation, null, false);
    }

    /**
     * Test method for {@link org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl#setEvalAssignments(java.lang.Long, java.lang.String[], java.lang.String[], boolean)}.
     */
    @Ignore
    @Test
    public void testSetEvalAssignments() {
        List<EvalAssignGroup> assignGroups;
        externalLogic.setCurrentUserId(EvalTestDataLoad.MAINT_USER_ID);
        Long evaluationId = etdl.evaluationNew.getId();
        assignGroups = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, false).get(evaluationId);
        Assert.assertNotNull(assignGroups);
        Assert.assertTrue(assignGroups.isEmpty());

        evaluationSetupService.setEvalAssignments(evaluationId, null, new String[] {EvalTestDataLoad.SITE1_REF}, true);

        assignGroups = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, false).get(evaluationId);
        Assert.assertNotNull(assignGroups);
        Assert.assertTrue(assignGroups.size() == 1);

        evaluationSetupService.setEvalAssignments(evaluationId, null, 
                new String[] {EvalTestDataLoad.SITE1_REF, EvalTestDataLoad.SITE2_REF}, true);

        assignGroups = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, false).get(evaluationId);
        Assert.assertNotNull(assignGroups);
        Assert.assertTrue(assignGroups.size() == 2);
    }
    
    @Ignore
    @Test
    public void testSetEvalAssignments_noAuthnNoGroups() {

        List<EvalAssignGroup> assignGroups;
        externalLogic.setCurrentUserId(EvalTestDataLoad.MAINT_USER_ID_3);
        Long evaluationId = etdl.evaluationPartial_noAuthNoGroups.getId();
        assignGroups = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, false).get(evaluationId);
        Assert.assertNotNull(assignGroups);
        Assert.assertTrue(assignGroups.isEmpty());

        evaluationSetupService.setEvalAssignments(evaluationId, null, null, false);

        assignGroups = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, false).get(evaluationId);
        Assert.assertNotNull(assignGroups);
        Assert.assertTrue(assignGroups.size() == 1);
        
        EvalAssignGroup evalAssignGroup = assignGroups.get(0);
        Assert.assertEquals(EvalTestDataLoad.MAINT_USER_ID_3, evalAssignGroup.getOwner());
        Assert.assertEquals(EvalConstants.GROUP_TYPE_PROVIDED, evalAssignGroup.getEvalGroupType());
        
        List<EvalAssignUser> participantsForEval = evaluationDao.getParticipantsForEval(evaluationId, null, null, null, 
        		null, null, null);
        
        Assert.assertEquals(1, participantsForEval.size());
        Assert.assertEquals(EvalTestDataLoad.MAINT_USER_ID_3, participantsForEval.get(0).getUserId());
    }

}
