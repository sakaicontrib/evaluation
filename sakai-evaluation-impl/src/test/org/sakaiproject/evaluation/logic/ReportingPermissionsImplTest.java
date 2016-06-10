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

import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
   @Before
   public void onSetUpBeforeTransaction() throws Exception {
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

   @After
   public void onTearDownAfterTransaction() throws Exception {
      // restore the settings
      settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, instructorViewResults);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, studentViewResults);
   };


   /**
    * TEST methods
    */

   // protected methods
   @Test
   public void testGetEvalGroupIdsForUserRole() {
      Set<String> evalGroupIds;

      // check for active eval
      evalGroupIds = reportingPermissions.getEvalGroupIdsForUserRole(etdl.evaluationActive.getId(), 
            EvalTestDataLoad.MAINT_USER_ID, null, true);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());
      Assert.assertTrue( evalGroupIds.contains(etdl.assign1.getEvalGroupId()) );

      evalGroupIds = reportingPermissions.getEvalGroupIdsForUserRole(etdl.evaluationActive.getId(), 
            EvalTestDataLoad.MAINT_USER_ID, null, false);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(0, evalGroupIds.size());

      // check for closed eval
      evalGroupIds = reportingPermissions.getEvalGroupIdsForUserRole(etdl.evaluationClosed.getId(), 
            EvalTestDataLoad.MAINT_USER_ID, null, true);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());
      Assert.assertTrue( evalGroupIds.contains(etdl.assign3.getEvalGroupId()) );

      evalGroupIds = reportingPermissions.getEvalGroupIdsForUserRole(etdl.evaluationClosed.getId(), 
            EvalTestDataLoad.USER_ID, null, false);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());
      Assert.assertTrue( evalGroupIds.contains(etdl.assign4.getEvalGroupId()) );

      // check eval with no groups
      evalGroupIds = reportingPermissions.getEvalGroupIdsForUserRole(etdl.evaluationNew.getId(), 
            EvalTestDataLoad.ADMIN_USER_ID, null, false);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(0, evalGroupIds.size());

   }
   
   @Test
   public void testGetViewableGroupsForEvalAndUserByRole() {
      Set<String> evalGroupIds;
      EvalEvaluation eval;

      settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, true);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, true);

      eval = evaluationService.getEvaluationById(etdl.evaluationClosed.getId());
      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.MAINT_USER_ID, null);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());
      Assert.assertTrue( evalGroupIds.contains(etdl.assign3.getEvalGroupId()) );

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.USER_ID, null);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());
      Assert.assertTrue( evalGroupIds.contains(etdl.assign4.getEvalGroupId()) );

      settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, false);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, false);

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.MAINT_USER_ID, null);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(0, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.USER_ID, null);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(0, evalGroupIds.size());

      settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, null);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, null);

      // set so it cannot be viewed
      eval.setInstructorViewResults(false);
      eval.setStudentViewResults(false);

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.MAINT_USER_ID, null);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(0, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.USER_ID, null);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(0, evalGroupIds.size());

      // set so it can be viewed
      eval.setInstructorViewResults(true);
      eval.setStudentViewResults(true);

      // so getViewableGroupsForEvalAndUserByRole seems pretty clear - if instructorAllowedViewResults == true, then the user is 
      // added to results, so I've changed the Assert.assertEquals to 1
      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.MAINT_USER_ID, null);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.USER_ID, null);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());

      // set the state so it can be viewed
      eval.setState(EvalConstants.EVALUATION_STATE_VIEWABLE);
      
      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.MAINT_USER_ID, null);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());
      Assert.assertTrue( evalGroupIds.contains(etdl.assign3.getEvalGroupId()) );

      evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.USER_ID, null);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());
      Assert.assertTrue( evalGroupIds.contains(etdl.assign4.getEvalGroupId()) );

   }

   @Test
   public void testGetViewableGroupsForEvalAndUserByRole_activeIgnoreViewDates() {
	  Set<String> evalGroupIds;
	  EvalEvaluation eval;

	  settings.set(EvalSettings.VIEW_SURVEY_RESULTS_IGNORE_DATES, true);
	  /*
	   * set instructor and student view results to configurable
	   * they are set to true on the evaluations
	   */	  
	  settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, null);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, null);

	  //test STATE = active 
	  //test maintain user can view active evaluation 
	  eval = evaluationService.getEvaluationById(etdl.evaluationActive_viewIgnoreDates.getId());
	  evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.EVALSYS_1007_MAINT_USER_ID_01, null);
	  Assert.assertNotNull(evalGroupIds);
	  Assert.assertEquals(1, evalGroupIds.size());
	  Assert.assertTrue( evalGroupIds.contains(etdl.evalsys_1007_assign03.getEvalGroupId()) );

	  //test normal user can view active evaluation
	  evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.EVALSYS_1007_USER_ID_01, null);
	  Assert.assertNotNull(evalGroupIds);
	  Assert.assertEquals(1, evalGroupIds.size());
	  Assert.assertTrue( evalGroupIds.contains(etdl.evalsys_1007_assign03.getEvalGroupId()) );
   }

   @Test
   public void testGetViewableGroupsForEvalAndUserByRole_dueIgnoreViewDates() {
	  Set<String> evalGroupIds;
	  EvalEvaluation eval;

	  settings.set(EvalSettings.VIEW_SURVEY_RESULTS_IGNORE_DATES, true);
	  /*
	   * set instructor and student view results to configurable
	   * they are set to true on the evaluations
	   */	  
	  settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, null);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, null);
	  
	  //test STATE = due 
	  //test maintain user can view active evaluation 
	  eval = evaluationService.getEvaluationById(etdl.evaluationDue_viewIgnoreDates.getId());
	  evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.EVALSYS_1007_MAINT_USER_ID_01, null);
	  Assert.assertNotNull(evalGroupIds);
	  Assert.assertEquals(1, evalGroupIds.size());
	  Assert.assertTrue( evalGroupIds.contains(etdl.evalsys_1007_assign02.getEvalGroupId()) );

	  //test normal user can view active evaluation
	  evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.EVALSYS_1007_USER_ID_01, null);
	  Assert.assertNotNull(evalGroupIds);
	  Assert.assertEquals(1, evalGroupIds.size());
	  Assert.assertTrue( evalGroupIds.contains(etdl.evalsys_1007_assign02.getEvalGroupId()) );
   }

   @Test
   public void testGetViewableGroupsForEvalAndUserByRole_closedIgnoreViewDates() {
	
	  Set<String> evalGroupIds;
	  EvalEvaluation eval;

	  settings.set(EvalSettings.VIEW_SURVEY_RESULTS_IGNORE_DATES, true);
	  /*
	   * set instructor and student view results to configurable
	   * they are set to true on the evaluations
	   */	  
	  settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, null);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, null);
	  
	  //test STATE = closed 
	  //test maintain user can view active evaluation 
	  eval = evaluationService.getEvaluationById(etdl.evaluationClosed_viewIgnoreDates.getId());
	  evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.EVALSYS_1007_MAINT_USER_ID_01, null);
	  Assert.assertNotNull(evalGroupIds);
	  Assert.assertEquals(1, evalGroupIds.size());
	  Assert.assertTrue( evalGroupIds.contains(etdl.evalsys_1007_assign01.getEvalGroupId()) );

	  //test normal user can view active evaluation
	  evalGroupIds = reportingPermissions.getViewableGroupsForEvalAndUserByRole(eval, EvalTestDataLoad.EVALSYS_1007_USER_ID_01, null);
	  Assert.assertNotNull(evalGroupIds);
	  Assert.assertEquals(1, evalGroupIds.size());
	  Assert.assertTrue( evalGroupIds.contains(etdl.evalsys_1007_assign01.getEvalGroupId()) );
   }

   // public methods

   @Test
   public void testCanViewEvaluationResponses() {
      boolean allowed;

      settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, null);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, null);

      // admin can always view results
      externalLogicMock.setCurrentUserId(EvalTestDataLoad.ADMIN_USER_ID);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationNew, null);
      Assert.assertTrue(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationActive, null);
      Assert.assertTrue(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationClosed, null);
      Assert.assertTrue(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationViewable, null);
      Assert.assertTrue(allowed);

      externalLogicMock.setCurrentUserId(EvalTestDataLoad.MAINT_USER_ID);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationNew, null);
      Assert.assertTrue(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationActive, null);
      Assert.assertTrue(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationClosed, null);
      Assert.assertFalse(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationViewable, null);
      Assert.assertTrue(allowed);

      externalLogicMock.setCurrentUserId(EvalTestDataLoad.USER_ID);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationNew, null);
      Assert.assertFalse(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationActive, null);
      Assert.assertFalse(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationClosed, null);
      Assert.assertFalse(allowed);
      allowed = reportingPermissions.canViewEvaluationResponses(etdl.evaluationViewable, null);
      Assert.assertTrue(allowed);

   }
   @Test
   public void testChooseGroupsPartialCheckEvalEvaluation() {
      Set<String> evalGroupIds;

      settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, null);
      settings.set(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS, null);

      externalLogicMock.setCurrentUserId(EvalTestDataLoad.ADMIN_USER_ID);
      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationActive);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationClosed);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(2, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationViewable);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());

      externalLogicMock.setCurrentUserId(EvalTestDataLoad.MAINT_USER_ID);
      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationActive);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationClosed);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(0, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationViewable);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());

      externalLogicMock.setCurrentUserId(EvalTestDataLoad.USER_ID);
      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationActive);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(0, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationClosed);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(0, evalGroupIds.size());

      evalGroupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationViewable);
      Assert.assertNotNull(evalGroupIds);
      Assert.assertEquals(1, evalGroupIds.size());

   }

   @Test
   public void testChooseGroupsPartialCheckLong() {
      // NOTE: this is a passthrough to the other method so just test that it works
      Set<String> groupIds = reportingPermissions.getResultsViewableEvalGroupIdsForCurrentUser(etdl.evaluationViewable.getId());
      Assert.assertNotNull(groupIds);
   }

}
