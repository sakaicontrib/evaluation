/**
 * $Id$
 * $URL$
 * ReportingPermissionsImplTest.java - evaluation - Apr 4, 2008 12:09:58 PM - azeckoski
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

import java.util.Set;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.test.EvalTestDataLoad;
import org.sakaiproject.evaluation.test.mocks.MockEvalExternalLogic;


/**
 * Tests for the special reporting permissions checks
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ReportingPermissionsImplTest extends BaseTestEvalLogic {

   protected ReportingPermissionsImpl reportingPermissions;
   private EvalEvaluationService evaluationService;
   private EvalSettings settings;
   private MockEvalExternalLogic externalLogicMock;

   private Boolean instructorViewResults = null;
   private Boolean studentViewResults = null;

   // run this before each test starts
   @Override
   protected void onSetUpBeforeTransaction() throws Exception {
      super.onSetUpBeforeTransaction();

      // load up any other needed spring beans
      settings = (EvalSettings) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalSettings");
      if (settings == null) {
         throw new NullPointerException("EvalSettings could not be retrieved from spring context");
      }

      EvalBeanUtils evalBeanUtils = (EvalBeanUtils) applicationContext.getBean("org.sakaiproject.evaluation.beans.EvalBeanUtils");
      if (evalBeanUtils == null) {
         throw new NullPointerException("EvalBeanUtils could not be retrieved from spring context");
      }

      evaluationService = (EvalEvaluationService) applicationContext.getBean("org.sakaiproject.evaluation.logic.EvalEvaluationService");
      if (evaluationService == null) {
         throw new NullPointerException("EvalEvaluationService could not be retrieved from spring context");
      }

      // setup the mock objects if needed
      externalLogicMock = (MockEvalExternalLogic) externalLogic;
      
      //create and setup the object to be tested
      reportingPermissions = new ReportingPermissionsImpl();
      reportingPermissions.setDao(evaluationDao);
      reportingPermissions.setEvalBeanUtils(evalBeanUtils);
      reportingPermissions.setEvalSettings(settings);
      reportingPermissions.setEvaluationService(evaluationService);
      reportingPermissions.setCommonLogic(commonLogic);

      // store the current settings so we can muck around with them
      instructorViewResults = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
      studentViewResults = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS);
   }

   @Override
   protected void onTearDownAfterTransaction() throws Exception {
      super.onTearDownAfterTransaction();
      
      // restore the settings
      settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, instructorViewResults);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, studentViewResults);
   };


   /**
    * TEST methods
    */

   // protected methods

   public void testGetEvalGroupIdsForUserRole() {
      Set<String> evalGroupIds = null;

      // check for active eval
      evalGroupIds = reportingPermissions.getEvalGroupIdsForUserRole(etdl.evaluationActive.getId(), 
            EvalTestDataLoad.MAINT_USER_ID, null, true);
      assertNotNull(evalGroupIds);
      assertEquals(1, evalGroupIds.size());
      assertTrue( evalGroupIds.contains(etdl.assign1.getEvalGroupId()) );

      evalGroupIds = reportingPermissions.getEvalGroupIdsForUserRole(etdl.evaluationActive.getId(), 
            EvalTestDataLoad.MAINT_USER_ID, null, false);
      assertNotNull(evalGroupIds);
      assertEquals(0, evalGroupIds.size());

      // check for closed eval
      evalGroupIds = reportingPermissions.getEvalGroupIdsForUserRole(etdl.evaluationClosed.getId(), 
            EvalTestDataLoad.MAINT_USER_ID, null, true);
      assertNotNull(evalGroupIds);
      assertEquals(1, evalGroupIds.size());
      assertTrue( evalGroupIds.contains(etdl.assign3.getEvalGroupId()) );

      evalGroupIds = reportingPermissions.getEvalGroupIdsForUserRole(etdl.evaluationClosed.getId(), 
            EvalTestDataLoad.USER_ID, null, false);
      assertNotNull(evalGroupIds);
      assertEquals(1, evalGroupIds.size());
      assertTrue( evalGroupIds.contains(etdl.assign4.getEvalGroupId()) );

      // check eval with no groups
      evalGroupIds = reportingPermissions.getEvalGroupIdsForUserRole(etdl.evaluationNew.getId(), 
            EvalTestDataLoad.ADMIN_USER_ID, null, false);
      assertNotNull(evalGroupIds);
      assertEquals(0, evalGroupIds.size());

   }

   public void testGetViewableGroupsForEvalAndUserByRole() {
      Set<String> evalGroupIds = null;
      EvalEvaluation eval = null;

      settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, true);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, true);

      eval = evaluationService.getEvaluationById(etdl.evaluationClosed.getId());
      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.MAINT_USER_ID, null);
      assertNotNull(evalGroupIds);
      assertEquals(1, evalGroupIds.size());
      assertTrue( evalGroupIds.contains(etdl.assign3.getEvalGroupId()) );

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.USER_ID, null);
      assertNotNull(evalGroupIds);
      assertEquals(1, evalGroupIds.size());
      assertTrue( evalGroupIds.contains(etdl.assign4.getEvalGroupId()) );

      settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, false);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, false);

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.MAINT_USER_ID, null);
      assertNotNull(evalGroupIds);
      assertEquals(0, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.USER_ID, null);
      assertNotNull(evalGroupIds);
      assertEquals(0, evalGroupIds.size());

      settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, null);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, null);

      // set so it cannot be viewed
      eval.setInstructorViewResults(false);
      eval.setStudentViewResults(false);

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.MAINT_USER_ID, null);
      assertNotNull(evalGroupIds);
      assertEquals(0, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.USER_ID, null);
      assertNotNull(evalGroupIds);
      assertEquals(0, evalGroupIds.size());

      // set so it can be viewed
      eval.setInstructorViewResults(true);
      eval.setStudentViewResults(true);

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.MAINT_USER_ID, null);
      assertNotNull(evalGroupIds);
      assertEquals(0, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.USER_ID, null);
      assertNotNull(evalGroupIds);
      assertEquals(0, evalGroupIds.size());

      // set the state so it can be viewed
      eval.setState(EvalConstants.EVALUATION_STATE_VIEWABLE);
      
      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.MAINT_USER_ID, null);
      assertNotNull(evalGroupIds);
      assertEquals(1, evalGroupIds.size());
      assertTrue( evalGroupIds.contains(etdl.assign3.getEvalGroupId()) );

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.USER_ID, null);
      assertNotNull(evalGroupIds);
      assertEquals(1, evalGroupIds.size());
      assertTrue( evalGroupIds.contains(etdl.assign4.getEvalGroupId()) );

   }


   // public methods

   public void testCanViewEvaluationResponses() {
      boolean allowed = false;

      settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, null);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, null);

      // admin can always view results
      externalLogicMock.setCurrentUserId(EvalTestDataLoad.ADMIN_USER_ID);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationNew, null);
      assertTrue(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationActive, null);
      assertTrue(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationClosed, null);
      assertTrue(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationViewable, null);
      assertTrue(allowed);

      externalLogicMock.setCurrentUserId(EvalTestDataLoad.MAINT_USER_ID);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationNew, null);
      assertTrue(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationActive, null);
      assertTrue(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationClosed, null);
      assertFalse(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationViewable, null);
      assertTrue(allowed);

      externalLogicMock.setCurrentUserId(EvalTestDataLoad.USER_ID);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationNew, null);
      assertFalse(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationActive, null);
      assertFalse(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationClosed, null);
      assertFalse(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationViewable, null);
      assertTrue(allowed);

   }

   public void testChooseGroupsPartialCheckEvalEvaluation() {
      Set<String> evalGroupIds = null;

      settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, null);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, null);

      externalLogicMock.setCurrentUserId(EvalTestDataLoad.ADMIN_USER_ID);
      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationActive);
      assertNotNull(evalGroupIds);
      assertEquals(1, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationClosed);
      assertNotNull(evalGroupIds);
      assertEquals(2, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationViewable);
      assertNotNull(evalGroupIds);
      assertEquals(1, evalGroupIds.size());

      externalLogicMock.setCurrentUserId(EvalTestDataLoad.MAINT_USER_ID);
      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationActive);
      assertNotNull(evalGroupIds);
      assertEquals(1, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationClosed);
      assertNotNull(evalGroupIds);
      assertEquals(0, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationViewable);
      assertNotNull(evalGroupIds);
      assertEquals(1, evalGroupIds.size());

      externalLogicMock.setCurrentUserId(EvalTestDataLoad.USER_ID);
      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationActive);
      assertNotNull(evalGroupIds);
      assertEquals(0, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationClosed);
      assertNotNull(evalGroupIds);
      assertEquals(0, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationViewable);
      assertNotNull(evalGroupIds);
      assertEquals(1, evalGroupIds.size());

   }

   public void testChooseGroupsPartialCheckLong() {
      // NOTE: this is a passthrough to the other method so just test that it works
      Set<String> groupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationViewable.getId());
      assertNotNull(groupIds);
   }

}
