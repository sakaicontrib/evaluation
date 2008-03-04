/**
 * EvaluationBean.java - evaluation - Oct 05, 2006 11:35:56 AM - kahuja
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.constant.EvalEmailConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.producers.ControlEvaluationsProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationAssignConfirmProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationAssignProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationSettingsProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationStartProducer;
import org.sakaiproject.evaluation.tool.producers.SummaryProducer;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.util.FormattedText;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * This is the backing bean of the evaluation process.
 * 
 * @author Rui Feng (fengr@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationBean {

   private static Log log = LogFactory.getLog(EvaluationBean.class);

   private EvalExternalLogic external;
   public void setExternal(EvalExternalLogic external) {
      this.external = external;
   }

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalEvaluationSetupService evaluationSetupService;
   public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
      this.evaluationSetupService = evaluationSetupService;
   }

   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }

   private ExternalHierarchyLogic hierarchyLogic;
   public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
      this.hierarchyLogic = logic;
   }

   private TargettedMessageList messages;
   public void setMessages(TargettedMessageList messages) {
      this.messages = messages;
   }

   private Locale locale;
   public void setLocale(Locale locale){
      this.locale=locale;
   }



   /*
    * VARIABLE DECLARATIONS 
    */
   public EvalEvaluation eval = new EvalEvaluation();
   public String[] selectedEvalGroupIds;
   public String[] selectedEvalHierarchyNodeIds;
   public Long templateId = new Long(1L);
   public Date startDate;
   public Date dueDate;
   public Date stopDate;
   public Date viewDate;
   public int[] enrollment;

   // These are for the Assign screen, which is now bound by UIBoundBooleans
   public Map<String, Boolean> selectedEvalGroupIDsMap = new HashMap<String,Boolean>();
   public Map<String, Boolean> selectedEvalHierarchyNodeIDsMap = new HashMap<String, Boolean>();

   /*
    * These 2 values are used for getting the email template as well as constant.
    */
   public String emailAvailableTxt;
   public String emailReminderTxt; 


   //TODO: need to merge with public field: eval, now use other string to avoid failing of other page 
   public Long evalId; 	//used to ELBinding to the evaluation ID to be removed on Control Panel
   public String tmplId; 	//used to ELBinding To the template ID to be removed on Control Panel 

   DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);


   public void init() {
      df = DateFormat.getDateInstance(DateFormat.LONG, locale);
   }


   /*
    * MAJOR METHOD DEFINITIONS
    */

   /**
    * Method binding to the "Continue to Settings" button on 
    * evaluation_start.html.
    * 
    * @return View id that sends the control to evaluation settings page.
    */
   public String continueToSettingsAction() {
      // Initializing all the bind variables used in EvaluationSettingsProducer. 
      Calendar calendar = new GregorianCalendar();
      calendar.setTime( new Date() );
      if (startDate == null) {
         startDate = calendar.getTime();
         log.info("Setting start date to default of: " + startDate);
      }

      calendar.add(Calendar.DATE, 1);
      if (dueDate == null) {
         Calendar cal = new GregorianCalendar();
         cal.setTime(calendar.getTime());
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         dueDate = cal.getTime();
         log.info("Setting due date to default of: " + dueDate);
      }

      // assign stop date to equal due date for now
      if (stopDate == null) {
         stopDate = dueDate;
         log.info("Setting stop date to default of: " + stopDate);
      }

      // assign default view date
      calendar.add(Calendar.DATE, 1);
      if (viewDate == null) {
         viewDate = calendar.getTime();
         log.info("Setting view date to default of: " + viewDate);
      }

      eval.setStudentsDate(viewDate);
      eval.setInstructorsDate(viewDate);

      // results viewable settings
      eval.setResultsSharing( EvalConstants.SHARING_VISIBLE );

      Boolean studentsView = (Boolean) settings.get(EvalSettings.STUDENT_VIEW_RESULTS);
      if (studentsView == null) { studentsView = false; }
      eval.studentViewResults = studentsView;

      Boolean instructorsView = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
      if (instructorsView == null) { instructorsView = false; }
      eval.instructorViewResults = instructorsView;

      // student completion settings
      Boolean blankAllowed = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
      if (blankAllowed == null) { blankAllowed = false; }
      eval.setBlankResponsesAllowed(blankAllowed);

      Boolean modifyAllowed = (Boolean) settings.get(EvalSettings.STUDENT_MODIFY_RESPONSES);
      if (modifyAllowed == null) { modifyAllowed = false; }
      eval.setModifyResponsesAllowed(modifyAllowed);

      eval.setUnregisteredAllowed(Boolean.FALSE);

      // admin settings
      String instOpt = (String) settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE);
      if (instOpt == null) { instOpt = EvalConstants.INSTRUCTOR_REQUIRED; }
      eval.setInstructorOpt(instOpt);

      // email settings
      emailAvailableTxt = null; // available template
      emailReminderTxt =  null; //reminder email

      eval.setReminderDays(new Integer(EvalToolConstants.REMINDER_EMAIL_DAYS_VALUES[1]));
      eval.setReminderFromEmail( (String) settings.get(EvalSettings.FROM_EMAIL_ADDRESS) );

      // set the template
      EvalTemplate template = (EvalTemplate) authoringService.getTemplateById(this.templateId);
      // this is OK to be null here since it will be when the eval is first created
      eval.setTemplate(template);

      // set the type to the default
      eval.setType(EvalConstants.EVALUATION_TYPE_EVALUATION);

      //returning the view id	
      return EvaluationSettingsProducer.VIEW_ID;
   }

   /**
    * Method binding to the "Continue to Assign to Courses" button on the 
    * evaluation_setting.html.
    * 
    * @return View id that sends the control to assign page.
    */
   public String continueAssigningAction()	{	

      //Clear the selected sites 
      selectedEvalGroupIds = null;
      return EvaluationAssignProducer.VIEW_ID;
   }

   /**
    * Method binding to the "Save Settings" button on the evaluation_setting.html.
    * 
    * @return View id that sends the control to control panel or summary.
    */
   public String saveSettingsAction() {	
      /*
       * If it is a queued evaluation then get value from startDate variable.
       * 
       * Else (for active evaluationSetupService), start sate is disabled and so there is a 
       * null value set in startDate variable. So pick value what was already there
       * in the eval object, but we need to convert the already stored date to 
       * java.util.Date format. This is because by default it is java.sql.Timestamp.
       */
      Date today = new Date();
      if (today.before(eval.getStartDate())) {
         //Do nothing as start date is properly set in the evaluation bean.
      } else {
         startDate = eval.getStartDate(); 
      }

      //Perform common tasks
      commonSaveTasks();

      //Need to fetch the object again as Hibernate session has expired
      EvalEvaluation evalInDB = evaluationService.getEvaluationById(eval.getId());
      String evalState = EvalUtils.getEvaluationState(evalInDB, false);

      // Now copying the data from eval to evalInDB (all fields we care to change must be added to this)
      if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(evalState)) {
         evalInDB.setStartDate(eval.getStartDate());
         evalInDB.setInstructorOpt(eval.getInstructorOpt());
         evalInDB.setAvailableEmailTemplate(eval.getAvailableEmailTemplate());
         evalInDB.setReminderFromEmail(eval.getReminderFromEmail());
         evalInDB.setBlankResponsesAllowed(eval.getBlankResponsesAllowed());
         evalInDB.setModifyResponsesAllowed(eval.getModifyResponsesAllowed());
         evalInDB.setUnregisteredAllowed(eval.getUnregisteredAllowed());
         evalInDB.setAuthControl(eval.getAuthControl());
         evalInDB.setType(eval.getType());
      }
      if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(evalState) ||
            EvalConstants.EVALUATION_STATE_ACTIVE.equals(evalState) ) {
         evalInDB.setStopDate(eval.getStopDate());
         evalInDB.setDueDate(eval.getDueDate());
         evalInDB.setReminderEmailTemplate(eval.getReminderEmailTemplate());
         evalInDB.setReminderDays(eval.getReminderDays());
      }

      // can be changed anytime
      evalInDB.setViewDate(eval.getViewDate());
      evalInDB.setStudentsDate(eval.getStudentsDate());
      evalInDB.setInstructorsDate(eval.getInstructorsDate());
      evalInDB.setResultsSharing(eval.getResultsSharing());
      evalInDB.setEvalCategory(eval.getEvalCategory());
      evalInDB.instructorViewResults = eval.instructorViewResults;
      evalInDB.studentViewResults = eval.studentViewResults;

      evaluationSetupService.saveEvaluation(evalInDB, external.getCurrentUserId());

      messages.addMessage( new TargettedMessage("evalsettings.updated.message",
            new Object[] { eval.getTitle() }, 
            TargettedMessage.SEVERITY_INFO));

      // now reset the eval item here
      clearEvaluation();
      return ControlEvaluationsProducer.VIEW_ID;
   }

   /**
    * Method binding to the "Cancel" button on the 
    * evaluation_setting.html when editing an existing
    * evaluation (that is not for new evaluation because
    * then the cancel button is just history.back).
    * 
    * @return View id that sends the control to 
    * 			control panel
    */
   public String cancelSettingsAction() {	

      // Reset the eval item here
      clearEvaluation();

      return ControlEvaluationsProducer.VIEW_ID;
   }	


   /**
    * Method binding to the "Cancel" button on the 
    * evaluation_assign.html.
    * 
    * @return View id that sends the control to summary page.
    */
   public String cancelAssignAction() {	
      return SummaryProducer.VIEW_ID;
   }

   /**
    * Method binding to the "Edit Settings" button 
    * on the evaluation_assign.html.
    * 
    * @return View id that sends the control to settings page.
    */
   public String backToSettingsAction() {	
      return EvaluationSettingsProducer.VIEW_ID;
   }


   /**
    * Method binding to the "Save Changes" button on the modify_email.html for 
    * the original link from email_available
    * 
    * @return String that is used to determine the place where control is to be sent
    * 			in ModifyEmailProducer (reportNavigationCases method)
    * @throws SecurityException 
    */
   public String saveAvailableEmailTemplate() throws SecurityException {

      /* 
       * Avoiding XSS attacks here.
       * TODO: Need to make nicer by sending a red message to the screen.
       * 
       * Note: The code here is similar to the one in saveReminderEmailTemplate
       * method, but the two cannot be merged efficiently as in one available 
       * email is changed and in other the reminder email (nevertheless
       * they can still be merged).
       */
      StringBuffer errorMessages = new StringBuffer();
      emailAvailableTxt = FormattedText.processFormattedText(emailAvailableTxt, errorMessages);

      if (errorMessages.length() != 0) {
         // invalid text in the email message so fail

         Long tempEvalId = eval.getId();

         // New evaluation
         if (tempEvalId == null) { 
            emailAvailableTxt = evaluationService.getDefaultEmailTemplate(EvalConstants.EMAIL_TEMPLATE_AVAILABLE).getMessage();
         }
         // Existing evaluation
         else { 
            eval = evaluationService.getEvaluationById(eval.getId());
            emailAvailableTxt = eval.getAvailableEmailTemplate().getMessage();
         }

         // Finally throw exception so that same page with same user text, is displayed
         throw new SecurityException("XSS attack possible! " + errorMessages);
      }

      return EvalConstants.EMAIL_TEMPLATE_AVAILABLE;
   }

   /**
    * Method binding to the "Save Changes" button on the modify_email.html for 
    * original link from email_reminder
    *
    * @return String that is used to determine the place where control is to be sent
    * 			in ModifyEmailProducer (reportNavigationCases method)
    * @throws SecurityException 
    */
   public String saveReminderEmailTemplate() throws SecurityException {

      // Avoiding XSS attacks
      // TODO: Need to make nicer by sending a red message to the screen.
      StringBuffer errorMessages = new StringBuffer();
      emailReminderTxt = FormattedText.processFormattedText(emailReminderTxt, errorMessages);

      if (errorMessages.length() != 0) {

         Long tempEvalId = eval.getId();

         // New evaluation
         if (tempEvalId == null) { 
            emailReminderTxt =  evaluationService.getDefaultEmailTemplate(EvalConstants.EMAIL_TEMPLATE_REMINDER).getMessage();//reminder email
         }
         // Existing evaluation
         else { 
            eval = evaluationService.getEvaluationById(eval.getId());
            emailReminderTxt = eval.getReminderEmailTemplate().getMessage();
         }

         // Finally throw exception so that same page with same user text, is displayed
         throw new SecurityException("XSS attack possible! " + errorMessages);
      }

      return EvalConstants.EMAIL_TEMPLATE_REMINDER;
   }

   /**
    * Method binding to the "Modify this Email Template" button on the preview_email.html for 
    * the original link from email_available.
    * 
    * @return String that is used to determine the place where control is to be sent
    * 			in PreviewEmailProducer (reportNavigationCases method)
    */
   public String modifyAvailableEmailTemplate(){
      return EvalConstants.EMAIL_TEMPLATE_AVAILABLE;
   }

   /**
    * Method binding to the "Modify this Email Template" button on the preview_email.html for 
    * the original link from email_reminder.
    * 
    * @return String that is used to determine the place where control is to be sent
    * 			in PreviewEmailProducer (reportNavigationCases method)
    */
   public String modifyReminderEmailTemplate(){
      return EvalConstants.EMAIL_TEMPLATE_REMINDER;
   }



   /**
    * Method binding to the "Save Assigned Courses" button 
    * on the evaluation_assign.html.
    * 
    * @return View id which goes to assign confirm page
    *          if atleast one course is selected.
    */
   public String confirmAssignCoursesAction() { 

      //TODO We have to populate the String Arrays with the Maps.
      // This is because of the need to use UIBoundBooleans
      List<String> groupIdCopy = new ArrayList<String>();
      List<String> hierNodeIdCopy = new ArrayList<String>();

      for (String groupID: selectedEvalGroupIDsMap.keySet()) {
         if (selectedEvalGroupIDsMap.get(groupID) == true) {
            groupIdCopy.add(groupID);
         }
      }

      for (String hierNodeID: selectedEvalHierarchyNodeIDsMap.keySet()) {
         if (selectedEvalHierarchyNodeIDsMap.get(hierNodeID) == true) {
            hierNodeIdCopy.add(hierNodeID);
         }
      }

      selectedEvalGroupIds = groupIdCopy.toArray(new String[]{});
      selectedEvalHierarchyNodeIds = hierNodeIdCopy.toArray(new String[] {});

      // make sure that the submitted nodes are valid
      Set<EvalHierarchyNode> nodes = null;
      if (selectedEvalHierarchyNodeIds.length > 0) {
         nodes = hierarchyLogic.getNodesByIds(selectedEvalHierarchyNodeIds);
         if (nodes.size() != selectedEvalHierarchyNodeIds.length) {
            throw new IllegalArgumentException("Invalid set of hierarchy node ids submitted which includes node Ids which are not in the hierarchy");
         }
      } else {
         nodes = new HashSet<EvalHierarchyNode>();
      }

      // At least 1 node or group checkbox must be checked
      if ( (selectedEvalGroupIds == null || selectedEvalGroupIds.length == 0) &&
            nodes.isEmpty() ) {
         messages.addMessage( new TargettedMessage("assigneval.invalid.selection",
               new Object[] {}, TargettedMessage.SEVERITY_ERROR));
         return "fail";
      }

      // add in the enrollment counts for the following page (seems odd to do this here -AZ)
      if (selectedEvalGroupIds != null && selectedEvalGroupIds.length > 0) {
         // get enrollments one by one
         enrollment = new int[selectedEvalGroupIds.length];
         for (int i = 0; i<selectedEvalGroupIds.length; i++) {
            Set<String> s = external.getUserIdsForEvalGroup(selectedEvalGroupIds[i], EvalConstants.PERM_TAKE_EVALUATION);
            enrollment[i] = s.size();           
         }
      }
      return EvaluationAssignConfirmProducer.VIEW_ID;
   }

   /**
    * Method binding to the "Done" button on evaluation_assign_confirm.html
    * When come from control panel then saveSettingsAction method is called.
    * 
    * @return view id telling RSF where to send the control
    */
   public String doneAssignmentAction() {	

      // make sure that the submitted nodes are valid
      Set<EvalHierarchyNode> nodes = null;
      if (selectedEvalHierarchyNodeIds.length > 0) {
         nodes = hierarchyLogic.getNodesByIds(selectedEvalHierarchyNodeIds);
         if (nodes.size() != selectedEvalHierarchyNodeIds.length) {
            throw new IllegalArgumentException("Invalid set of hierarchy node ids submitted which includes node Ids which are not in the hierarchy");
         }
      } else {
         nodes = new HashSet<EvalHierarchyNode>();
      }

      // need to load the template here before we try to save it because it is stale -AZ
      eval.setTemplate( authoringService.getTemplateById( eval.getTemplate().getId() ) );

      //The main evaluation section with all the settings.
      eval.setOwner(external.getCurrentUserId());

      //Perform common tasks
      commonSaveTasks();

      if ( (selectedEvalGroupIds != null && selectedEvalGroupIds.length > 0) 
            || ! nodes.isEmpty() ) {
         // save the evaluation
         eval.setState( EvalUtils.getEvaluationState(eval, true) ); // set the state according to dates only
         evaluationSetupService.saveEvaluation(eval, external.getCurrentUserId());

         // NOTE - this allows the evaluation to be saved with zero assign groups if this fails

         // expand the hierarchy to include all nodes below this one
         Set<String> allNodeIds = hierarchyLogic.getAllChildrenNodes(nodes, true);

         // save all the assignments (hierarchy and group)
         List<EvalAssignHierarchy> assignedHierList = 
            evaluationSetupService.addEvalAssignments(eval.getId(), 
                  allNodeIds.toArray(new String[allNodeIds.size()]), 
                  selectedEvalGroupIds);
         // failsafe check
         if (assignedHierList.isEmpty()) {
            evaluationSetupService.deleteEvaluation(eval.getId(), external.getCurrentUserId());
            throw new IllegalStateException("Invalid evaluation created with no assignments! Destroying evaluation: " + eval.getId());
         }
  
         messages.addMessage( new TargettedMessage("evaluationSetupService.add.message",
               new Object[] { eval.getTitle(), df.format(eval.getStartDate()) }, 
               TargettedMessage.SEVERITY_INFO));

         //now reset the eval item here
         clearEvaluation();
         return ControlEvaluationsProducer.VIEW_ID;
      } else {
         throw new IllegalStateException("Cannot save evaluation with zero assigned groups and nodes");
      }
   }

   /**
    * Method binding to "Change Assigned Courses" button on 
    * evaluation_assign_confirm page and link for courses assigned 
    * on control panel (for queued evaluationSetupService). 
    * 
    * @return View id that sends the control to assign page.
    */
   public String changeAssignedCourseAction(){

      //TODO: for quued evaluation coming from control panel page 
      return EvaluationAssignProducer.VIEW_ID;
   }

   /**
    * Makes a new evaluation object residing in this session bean.
    */	
   public void clearEvaluation(){
      this.eval = new EvalEvaluation();
      // set the state to partial
      this.eval.setState(EvalConstants.EVALUATION_STATE_PARTIAL);

      // reset some values in the bean

      // email settings
      emailAvailableTxt = null; // available template
      emailReminderTxt =  null; //reminder email

   }

   /**
    * Method binding to "Start Evaluation" link/commans on 
    * the Control Panel page.
    *
    * @return View id that sends the control to evaluation start page.
    */	
   public String startEvaluation(){

      /*
       * Start evaluation page can be reached from three different places:
       * (a) The "Modify Template" page by clicking on:
       * 		"Begin an evaluation using this template" internal link.
       * 
       * (b) The summary page by clicking on "Begin Evaluation" internal link.
       * 
       * (c) This one i.e. control panel page by clicking on "Start Evaluation" 
       *  	command button (that calls this method).
       *  
       * As bean cannot be cleared from internal links, so not clearing the 
       * bean here in command button too. Ideally this command button should 
       * also be converted to an internal link. 
       * 
       * Also, clearing bean at the above three places is not important as it 
       * is being cleared at three places where it is important:
       * - saveSettingsAction
       * - cancelSettingsAction
       * - doneAssignmentAction
       * 
       * kahuja on March 7th, 2007
       */

      return EvaluationStartProducer.VIEW_ID;
   }

   /**
    * This method prepares backing Bean data  for EditSettings page.
    * It binds to:
    * 1) Control Panel page: edit command button/link
    * 2) Summary page: evalAdminTitleLink for queued, active evaluation
    * 
    * @return View id sending the control to evaluation settings producer.
    */
   public String editEvalSettingAction(){	
      eval = evaluationService.getEvaluationById(eval.getId());
      evaluationService.returnAndFixEvalState(eval, true); // refresh and save the state

      startDate = eval.getStartDate();
      dueDate = eval.getDueDate();
      stopDate = eval.getStopDate();
      viewDate = eval.getViewDate();

      /*
       * If student/instructor date is not null then set the date and also 
       * make the checkbox checked. Else make the checkbox unchecked. 
       */
      eval.studentViewResults = eval.getStudentsDate() == null ? Boolean.FALSE: Boolean.TRUE;
      eval.instructorViewResults = eval.getInstructorsDate() == null ? Boolean.FALSE: Boolean.TRUE;

      // email templates (get the template text if set)
      emailAvailableTxt = eval.getAvailableEmailTemplate() != null ? eval.getAvailableEmailTemplate().getMessage() : null; // available template
      emailReminderTxt =  eval.getReminderEmailTemplate() != null ? eval.getReminderEmailTemplate().getMessage() : null;  //reminder email

      return EvaluationSettingsProducer.VIEW_ID;
   }

   /**
    * Method binding to control panel page "Assigned" Link/Command.
    * 
    * @return View id sending the control to assign confirm page.
    */
   public String evalAssigned() {
      eval = evaluationService.getEvaluationById(evalId);
      Map<Long, List<EvalAssignGroup>> evalAssignGroups = evaluationService.getEvaluationAssignGroups(new Long[] {evalId}, true);
      List<EvalAssignGroup> groups = evalAssignGroups.get(evalId);
      if (groups.size() > 0) {
         selectedEvalGroupIds = new String[groups.size()];
         for (int i =0; i< groups.size(); i++) {
            EvalAssignGroup eac = (EvalAssignGroup) groups.get(i);
            selectedEvalGroupIds[i] = eac.getEvalGroupId();
         }
      }

      enrollment =  new int[selectedEvalGroupIds.length];
      for(int i =0; i<selectedEvalGroupIds.length; i++){
         Set<String> s = external.getUserIdsForEvalGroup(selectedEvalGroupIds[i], EvalConstants.PERM_TAKE_EVALUATION);
         enrollment[i] = s.size();				
      }
      return EvaluationAssignConfirmProducer.VIEW_ID;
   }

   /**
    * Method binding to the "Cancel" button on the remove_evaluation.html.
    * 
    * @return View id sending the control to control panel page.
    */
   public String cancelRemoveEvalAction(){
      return ControlEvaluationsProducer.VIEW_ID;
   }

   /**
    * Method binding to the "Remove Evaluation" button  
    * on the remove_evalaution.html
    * 
    * @return View id sending the control to control panel page.
    */
   public String removeEvalAction(){
      evaluationSetupService.deleteEvaluation(evalId, external.getCurrentUserId());
      return ControlEvaluationsProducer.VIEW_ID;
   }


   //TODO: This is not the place for anything related to templates - kahuja (8th Feb 2007)
   //		Thus this method should be done using template bean locator.
   /**
    * Ultra simplified version of the remove template code
    * @return
    */
   public String removeTemplateAction() {
      String currentUserId = external.getCurrentUserId();
      Long templateId = new Long(tmplId);
      authoringService.deleteTemplate(templateId, currentUserId);
      return "success";
   }

   /**
    * Private method used to perform common tasks related to
    * saving settings for the first time (called from 
    * doneAssignmentAction method) and saving other times 
    * (called from saveSettingsAction method).  
    */
   private void commonSaveTasks() {

      boolean useStopDate = ((Boolean) settings.get(EvalSettings.EVAL_USE_STOP_DATE));
      boolean useViewDate = ((Boolean) settings.get(EvalSettings.EVAL_USE_VIEW_DATE));
      boolean useDateTime = ((Boolean) settings.get(EvalSettings.EVAL_USE_DATE_TIME));
      // Getting the system setting that tells what should be the minimum time difference between start date and due date.
      int minHoursDifference = ((Integer) settings.get(EvalSettings.EVAL_MIN_TIME_DIFF_BETWEEN_START_DUE)).intValue();

      if (eval.getResultsSharing() == null) {
         eval.setResultsSharing(EvalConstants.SHARING_VISIBLE);
      }

      eval.setStartDate(startDate);

      // force the due date to the end of the day if we are using dates only
      if (! useDateTime ) {
         dueDate = EvalUtils.getEndOfDayDate( dueDate );
      }
      eval.setDueDate(dueDate);

      if (! useStopDate) {
         // force stop date to due date if not in use
         stopDate = dueDate;
      }
      eval.setStopDate(stopDate);

      // set stop date to the due date if not set
      if (eval.getStopDate() == null) {
         // this is possible since the stopDate class variable might be set to null
         log.info("Setting the null stop date to the due date: " + dueDate);
         eval.setStopDate(dueDate);
      } else {
         // force the stop date to the end of the day if we are using dates only
         if (! useDateTime ) {
            log.info("Forcing date to end of day for non null stop date: " + eval.getStopDate());
            eval.setStopDate( EvalUtils.getEndOfDayDate( eval.getStopDate() ) );
         }
      }

      // Ensure minimum time difference between start and due/stop dates in eval - check this after the dates are set
      dueDate = EvalUtils.updateDueStopDates(eval, minHoursDifference);
      stopDate = eval.getStopDate(); // also update the global stopDate to the current one

      if (! useViewDate) {
         // force view date to due date + const mins if not in use
         viewDate = new Date( dueDate.getTime() + (1000 * 60 * EvalConstants.EVALUATION_TIME_TO_WAIT_MINS) );
      }
      eval.setViewDate(viewDate);

      /*
       * If "EVAL_USE_SAME_VIEW_DATES" system setting (admin setting) flag is set 
       * as true then don't look for student and instructor dates, instead make them
       * same as admin view date. If not then get the student and instructor view dates.
       */ 
      boolean sameViewDateForAll = ((Boolean) settings.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES));
      if (sameViewDateForAll) {
         if (eval.studentViewResults != null && eval.studentViewResults) {
            eval.setStudentsDate(viewDate);
         }
         if (eval.instructorViewResults != null && eval.instructorViewResults) {
            eval.setInstructorsDate(viewDate);
         }
      }

      // Email template section

      // Save email available template
      if (emailAvailableTxt != null) {
         EvalEmailTemplate availableTemplate = evaluationService.getDefaultEmailTemplate(EvalConstants.EMAIL_TEMPLATE_AVAILABLE);
         if (emailAvailableTxt.equals(availableTemplate.getMessage())) {
            // null it out as the template has not been modified
            availableTemplate = null;
         } else {
            availableTemplate = new EvalEmailTemplate(external.getCurrentUserId(), EvalConstants.EMAIL_TEMPLATE_AVAILABLE,
                  EvalEmailConstants.EMAIL_AVAILABLE_DEFAULT_SUBJECT, emailAvailableTxt);
            evaluationSetupService.saveEmailTemplate(availableTemplate, external.getCurrentUserId());
         }
         eval.setAvailableEmailTemplate(availableTemplate);
      }

      // Save the email reminder template
      if (emailReminderTxt != null) {
         EvalEmailTemplate reminderTemplate = evaluationService.getDefaultEmailTemplate(EvalConstants.EMAIL_TEMPLATE_REMINDER);
         if (emailReminderTxt.equals(reminderTemplate.getMessage())) {
            // null it out as the template has not been modified
            reminderTemplate = null;
         } else {
            reminderTemplate = new EvalEmailTemplate(external.getCurrentUserId(), EvalConstants.EMAIL_TEMPLATE_REMINDER,
                  EvalEmailConstants.EMAIL_REMINDER_DEFAULT_SUBJECT, emailReminderTxt);
            evaluationSetupService.saveEmailTemplate(reminderTemplate, external.getCurrentUserId());
         }
         eval.setReminderEmailTemplate(reminderTemplate);
      }

   }

}