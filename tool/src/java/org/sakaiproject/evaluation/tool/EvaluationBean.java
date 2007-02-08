/******************************************************************************
 * EvaluationBean.java - created by kahuja@vt.edu on Oct 05, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalAssignsLogic;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalAssignContext;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.producers.ControlPanelProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationAssignConfirmProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationAssignProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationSettingsProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationStartProducer;
import org.sakaiproject.evaluation.tool.producers.PreviewEvalProducer;
import org.sakaiproject.evaluation.tool.producers.SummaryProducer;
import org.sakaiproject.evaluation.tool.utils.ItemBlockUtils;

/**
 * This is the backing bean of the evaluation process.
 * 
 * @author Rui Feng (fengr@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationBean {
	
	/*
	 * VARIABLE DECLARATIONS 
	 */
	public EvalEvaluation eval = new EvalEvaluation();
	public String[] selectedSakaiSiteIds;
	public Long templateId = new Long(1L);
	public String startDate;
	public String stopDate;
	public String dueDate;
	public String viewDate;
	public String studentsDate;
	public String instructorsDate;
	public int[] enrollment;
	
	/*
	 * These 2 values are bound here because they are not in Evaluation POJO 
	 * and we need to store them when coming back to settings page from assign page.
	 */
	public Boolean studentViewResults;
	public Boolean instructorViewResults;

	/*
	 * These 2 values are used for getting the email template as well as constant.
	 */
	public String emailAvailableTxt;
	public String emailReminderTxt; 

	//Used to link the proper template object with the evaluation 
	private List listOfTemplates;
	
	//TODO: need to merge with public field: eval, now use other string to avoid failing of other page 
	public Long evalId; 	//used to ELBinding to the evaluation ID to be removed on Control Panel
	public String tmplId; 	//used to ELBinding To the template ID to be removed on Control Panel 

	private static Log log = LogFactory.getLog(EvaluationBean.class);

	//Spring injection
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
	
	//Spring injection
	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic( EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}
	
	//Spring injection
	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}
	
	//Spring injection
	private EvalEvaluationsLogic evalsLogic;
	public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
		this.evalsLogic = evalsLogic;
	}
	
	//Spring injection
	private EvalAssignsLogic assignsLogic;
	public void setAssignsLogic(EvalAssignsLogic assignsLogic) {
		this.assignsLogic = assignsLogic;
	}
	
	//Spring injection
	private EvalEmailsLogic emailsLogic;	
	public void setEmailsLogic(EvalEmailsLogic emailsLogic) {
		this.emailsLogic = emailsLogic;
	}
	
	//Spring injection
	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
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
		
		/*
		 * Initializing all the bind variables used in EvaluationSettingsProducer. 
		 */
		SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy");
		startDate = simpleDate.format(new Date());
		dueDate = "MM/DD/YYYY";
		stopDate = "MM/DD/YYYY";
		viewDate = "MM/DD/YYYY";
		
		//results viewable settings
		eval.setResultsPrivate(Boolean.FALSE);
		studentViewResults = Boolean.FALSE;
		studentsDate = "MM/DD/YYYY";
		instructorViewResults = Boolean.TRUE;
		instructorsDate = "MM/DD/YYYY";
		
		//student completion settings
		eval.setBlankResponsesAllowed(Boolean.TRUE);
		eval.setModifyResponsesAllowed(Boolean.FALSE);
		eval.setUnregisteredAllowed(Boolean.FALSE);
		
		//admin settings
		eval.setInstructorOpt(null);
		
		//email settings
		emailAvailableTxt = emailsLogic.getDefaultEmailTemplate(EvalConstants.EMAIL_TEMPLATE_AVAILABLE).getMessage();// available template
		eval.setReminderDays(new Integer(EvaluationConstant.REMINDER_EMAIL_DAYS_VALUES[1]));
		emailReminderTxt =  emailsLogic.getDefaultEmailTemplate(EvalConstants.EMAIL_TEMPLATE_REMINDER).getMessage();;//reminder email
		String s = (String) settings.get(EvalSettings.FROM_EMAIL_ADDRESS);
		eval.setReminderFromEmail(s);

		//find the template associated with this evaluation
		listOfTemplates = templatesLogic.getTemplatesForUser(external.getCurrentUserId(), null, false);
		int count = 0;
		if (listOfTemplates != null) {
			while (count < listOfTemplates.size()) {
				EvalTemplate temp = (EvalTemplate)listOfTemplates.get(count);
				if (temp.getId().longValue()== this.templateId.longValue()){
					eval.setTemplate(temp);
					break;
				}
				count++;
			}
		} 
		
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
		selectedSakaiSiteIds = null;
		return EvaluationAssignProducer.VIEW_ID;
	}

	/**
	 * Method binding to the "Save Settings" button on the 
	 * evaluation_setting.html.
	 * 
	 * @return View id that sends the control to control panel 
	 * 			or summary.
	 */
	public String saveSettingsAction() {	

		eval.setLastModified(new Date());
		
		/*
		 * If it is a queued evaluation then get value from startDate variable.
		 * 
		 * Else (for active evaluations), start sate is disabled and so there is a 
		 * null value set in startDate variable. So pick value what was already there
		 * in the eval object, but we need to convert the already stored date to 
		 * java.util.Date format. This is because by default it is java.sql.Timestamp.
		 */
		Date today = new Date();
		if (today.before(eval.getStartDate())) {
			eval.setStartDate(changeStringToDate(startDate));
		}
		else {
			Date startDateFromUtil = eval.getStartDate();
			eval.setStartDate(startDateFromUtil);
		}
		
		eval.setDueDate(changeStringToDate(dueDate));
		eval.setViewDate(changeStringToDate(viewDate));
		eval.setStudentsDate(changeStringToDate(studentsDate));
		eval.setInstructorsDate(changeStringToDate(instructorsDate));
	
		/*
		 * check if start date is the same as today's date, set startDate as today's date time, 
		 * as when we parse the string to a date, the time filed by default is zero
		 */
		checkEvalStartDate(eval);

		/*
		 * If the due date is same as start date then we need to make the time of due date 
		 * to be 1 second more that start date.
		 * (for details see the comment at the start of checkDueDate() method)
		 */		
		checkDueDate();
		
		// Needed by columbia so as of now making equal to due date		
		eval.setStopDate(eval.getDueDate());
		
		//Need to fetch the object again as Hibernate session has expired
		EvalEvaluation evalInDB = evalsLogic.getEvaluationById(eval.getId());
		
		//Now copying the data from eval to evalInDB
		evalInDB.setStartDate(eval.getStartDate());
		evalInDB.setStopDate(eval.getStopDate());
		evalInDB.setDueDate(eval.getDueDate());
		evalInDB.setViewDate(eval.getViewDate());

		evalInDB.setResultsPrivate(eval.getResultsPrivate());
		evalInDB.setStudentsDate(eval.getStudentsDate());
		evalInDB.setInstructorsDate(eval.getInstructorsDate());
		
		evalInDB.setBlankResponsesAllowed(eval.getBlankResponsesAllowed());
		evalInDB.setModifyResponsesAllowed(eval.getModifyResponsesAllowed());
		evalInDB.setUnregisteredAllowed(eval.getUnregisteredAllowed());

		evalInDB.setInstructorOpt(eval.getInstructorOpt());

		evalInDB.setAvailableEmailTemplate(eval.getAvailableEmailTemplate());
		evalInDB.setReminderFromEmail(eval.getReminderFromEmail());
		evalInDB.setReminderEmailTemplate(eval.getReminderEmailTemplate());
		evalInDB.setReminderDays(eval.getReminderDays());
		
		evalInDB.setLastModified(eval.getLastModified());

		evalsLogic.saveEvaluation(evalInDB, external.getCurrentUserId());
	    return ControlPanelProducer.VIEW_ID;
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
	 * Method binding to the "Save Assigned Courses" button 
	 * on the evaluation_assign.html.
	 * 
	 * @return View id which either sends the control to assign page 
	 * 			(if no courses are selected) or to assign confirm page
	 * 			if atleast one course is selected.
	 */
	public String confirmAssignCoursesAction() {	

		//At least 1 site check box need to be checked.
		if (selectedSakaiSiteIds == null || selectedSakaiSiteIds.length == 0) {
			return EvaluationAssignProducer.VIEW_ID;
		}
		else {
			//get enrollemnt on by one 
			enrollment =  new int[selectedSakaiSiteIds.length];
			for(int i =0; i<selectedSakaiSiteIds.length; i++){
				Set s = external.getUserIdsForContext(selectedSakaiSiteIds[i], EvalConstants.PERM_TAKE_EVALUATION);
				enrollment[i] = s.size();				
			}
			return EvaluationAssignConfirmProducer.VIEW_ID;
		}
	}

	/**
	 * Method binding to the "Save Changes" button on the modify_email.html for 
	 * the original link from email_available
	 * 
	 * @return String that is used to determine the place where control is to be sent
	 * 			in ModifyEmailProducer (reportNavigationCases method)
	 */
	public String saveAvailableEmailTemplate(){
		return "available";
	}

	/**
	 * Method binding to the "Save Changes" button on the modify_email.html for 
	 * original link from email_reminder
	 *
	 * @return String that is used to determine the place where control is to be sent
	 * 			in ModifyEmailProducer (reportNavigationCases method)
	 */
	public String saveReminderEmailTemplate(){
		return "reminder";
	}
	
	/**
	 * Method binding to the "Modify this Email Template" button on the preview_email.html for 
	 * the original link from email_available.
	 * 
	 * @return String that is used to determine the place where control is to be sent
	 * 			in PreviewEmailProducer (reportNavigationCases method)
	 */
	public String modifyAvailableEmailTemplate(){
		return "available";
	}
	

	/**
	 * Method binding to the "Modify this Email Template" button on the preview_email.html for 
	 * the original link from email_reminder.
	 * 
	 * @return String that is used to determine the place where control is to be sent
	 * 			in PreviewEmailProducer (reportNavigationCases method)
	 */
	public String modifyReminderEmailTemplate(){
		return "reminder";
	}

	/**
	 * Method binding to the "Done" button on evaluation_assign_confirm.html
	 * When come from control panel then saveSettingsAction method is called.
	 * 
	 * @return view id telling RSF where to send the control
	 */
	public String doneAssignmentAction() {	

		// need to load the template here before we try to save it because it is stale -AZ
		eval.setTemplate( templatesLogic.getTemplateById( eval.getTemplate().getId() ) );

		//The main evaluation section with all the settings.
		eval.setLastModified(new Date());
		eval.setOwner(external.getCurrentUserId());
		eval.setStartDate(changeStringToDate(startDate));
		eval.setDueDate(changeStringToDate(dueDate));
		eval.setViewDate(changeStringToDate(viewDate));

		/*
		 * If "EVAL_USE_SAME_VIEW_DATES" system setting (admin setting) flag is set 
		 * as true then don't look for student and instructor dates, instead make them
		 * same as admin view date. If not then get the student and instructor view dates.
		 */ 
		boolean sameViewDateForAll = ((Boolean) settings.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES)).booleanValue();
		if (sameViewDateForAll) {
			
			if (studentViewResults.booleanValue())
				eval.setStudentsDate(changeStringToDate(viewDate));
			
			if (instructorViewResults.booleanValue())
				eval.setInstructorsDate(changeStringToDate(viewDate));
		}
		else {
			
			if (studentViewResults.booleanValue())
				eval.setStudentsDate(changeStringToDate(studentsDate));
			
			if (instructorViewResults.booleanValue())
				eval.setInstructorsDate(changeStringToDate(instructorsDate));
		}

		// Email template section
		EvalEmailTemplate availableTemplate, reminderTemplate;

		//Save email available template
		availableTemplate = emailsLogic.getDefaultEmailTemplate(EvalConstants.EMAIL_TEMPLATE_AVAILABLE);
		if ( emailAvailableTxt.equals(availableTemplate.getMessage()) ) {
			//do nothing as the template has not been modified.
		} 
		else {
			availableTemplate = new EvalEmailTemplate(new Date(), external.getCurrentUserId(), emailAvailableTxt);
			emailsLogic.saveEmailTemplate(availableTemplate, external.getCurrentUserId());
		}
		eval.setAvailableEmailTemplate(availableTemplate);
		
		//Save the email reminder template
		reminderTemplate = emailsLogic.getDefaultEmailTemplate(EvalConstants.EMAIL_TEMPLATE_REMINDER);
		if ( emailReminderTxt.equals(reminderTemplate.getMessage()) ) {
			//do nothing as the template has not been modified.
		}
		else {
			reminderTemplate = new EvalEmailTemplate(new Date(), external.getCurrentUserId(), emailReminderTxt);
			emailsLogic.saveEmailTemplate(reminderTemplate, external.getCurrentUserId());
		}
		eval.setReminderEmailTemplate(reminderTemplate);
	
		/*
		 * check if start date is the same as today's date, set startDate as today's date & time, 
		 * as when we parse the string to a date, the time filed by default is zero
		 */
		checkEvalStartDate(eval);

		/*
		 * If the due date is same as start date then we need to make the time of due date 
		 * to be 1 second more that start date.
		 * (for details see the comment at the start of checkDueDate() method)
		 */		
		checkDueDate();

		// Needed by columbia so as of now making equal to due date
		eval.setStopDate(eval.getDueDate());            

		//save the evaluation
		evalsLogic.saveEvaluation(eval, external.getCurrentUserId());
		
		//now save the selected contexts
		for (int count = 0; count < this.selectedSakaiSiteIds.length; count++) {
			EvalAssignContext assignCourse = new EvalAssignContext(new Date(), 
					external.getCurrentUserId(), selectedSakaiSiteIds[count], 
					Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, eval);
			assignsLogic.saveAssignContext(assignCourse, external.getCurrentUserId());
		}

		//now reset the eval item here
		eval = new EvalEvaluation();
	    return ControlPanelProducer.VIEW_ID;
	}
	
	/**
	 * Method binding to "Change Assigned Courses" button on 
	 * evaluation_assign_confirm page and link for courses assigned 
	 * on control panel (for queued evaluations). 
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
	}
	
	/**
	 * Method binding to "Start Evaluation" link/commans on 
	 * the Control Panel page.
	 *
	 * @return View id that sends the control to evaluation start page.
	 */	
	public String startEvaluation(){
		clearEvaluation();
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
	
		eval = evalsLogic.getEvaluationById(eval.getId());
			
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		startDate = df.format(eval.getStartDate()); 
		stopDate = df.format(eval.getStopDate());   
		dueDate = df.format(eval.getDueDate());
		viewDate = df.format(eval.getViewDate());
		
		/*
		 * If student date is not null then set the date and also 
		 * make the checkbox checked. Else make the checkbox unchecked. 
		 */
		if (eval.getStudentsDate() != null) { 
			studentViewResults = Boolean.TRUE;
			studentsDate = df.format(eval.getStudentsDate());
		}
		else {
			studentViewResults = Boolean.FALSE;
			studentsDate = "MM/DD/YYYY";
		}
		
		/*
		 * If instructor date is not null then set the date and also 
		 * make the checkbox checked. Else make the checkbox unchecked. 
		 */
		if (eval.getInstructorsDate() != null) {
			instructorViewResults = Boolean.TRUE;
			instructorsDate = df.format(eval.getInstructorsDate());
		}
		else {
			instructorViewResults = Boolean.FALSE;
			instructorsDate = "MM/DD/YYYY";
		}

		return EvaluationSettingsProducer.VIEW_ID;
	}

	
	
	
	/*
	 * TO BE CLEANED FROM THIS POINT ONWARDS (GOING DOWN).
	 * ABOVE IS CLEANED AND COMMENTED - kahuja (7th Feb 2007).
	 */
	
	//method binding to control panel page "Assigned" Link/Command
	public String evalAssigned(){		
		
		//eval = logic.getEvaluationById(eval.getId());
		eval = evalsLogic.getEvaluationById(evalId);
		
		//List l = logic.getAssignContextsByEvalId(eval.getId());
		List l = assignsLogic.getAssignContextsByEvalId(eval.getId());
		
		if(l!=null && l.size() >0){
			selectedSakaiSiteIds = new String[l.size()];
			for(int i =0; i< l.size(); i++){
				EvalAssignContext eac = (EvalAssignContext) l.get(i);
				selectedSakaiSiteIds[i] = eac.getContext();
			}
		}
	
		//enrollment = logic.getEnrollment(selectedSakaiSiteIds);
		enrollment =  new int[selectedSakaiSiteIds.length];
		for(int i =0; i<selectedSakaiSiteIds.length; i++){
			Set s = external.getUserIdsForContext(selectedSakaiSiteIds[i], EvalConstants.PERM_TAKE_EVALUATION);
			enrollment[i] = s.size();				
		}

		return EvaluationAssignConfirmProducer.VIEW_ID;
	}
	
	//method binding to the "Cancel" button  on the remove_evaluation.html
	public String cancelRemoveEvalAction(){
		
		
		return ControlPanelProducer.VIEW_ID;
	}
	
	//method binding to the "Remove Evaluation" button  on the remove_evalaution.html
	public String removeEvalAction(){
		log.warn("remove evaluation action");
		
		Long id = new Long("-1");
		EvalEvaluation eval1 =null;
		try{
			id = evalId;
		}catch(NumberFormatException fe){	
			log.fatal(fe);
		}
		if(id.intValue()== -1) 
			log.error("Error inside removeEvalAction()");
		else
			//eval1 = logic.getEvaluationById(id);
			eval1 = evalsLogic.getEvaluationById(id);
		/**
		 * WARNING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		 * ***************************************
		 * This removal of contexts associated with the deleted evaluation should
		 * be happening at the logic layer so there are not errors like this
		 * one! For example, you are removing the contexts here but you should
		 * be also removing the email templates. You also are not checking whether
		 * this evaluation should be removed, it looks like you are allowing it
		 * to be removed regardless of the fact that it may have associated
		 * responses and answers (which is a HUGE problem) -AZ
		 * Remove this logic and use the appropriate method in the logic layer
		 * *************************************** 
		 */

	/*	if(eval1 != null){
			//delete evaluation, need to deleted entry in AssignCourse Table
			//List l = logic.getAssignContextsByEvalId(eval1.getId());
			List l = assignsLogic.getAssignContextsByEvalId(eval1.getId());
			if(l !=null && l.size() >0){
				for (int i=0; i<l.size(); i++) {
					EvalAssignContext eac = (EvalAssignContext) l.get(i);
					//logic.deleteAssignContext(eac.getId(), logic.getCurrentUserId());
					assignsLogic.deleteAssignContext(eac.getId(), external.getCurrentUserId());
				}
			}
			//logic.deleteEvaluation(eval1.getId(), logic.getCurrentUserId());
			evalsLogic.deleteEvaluation(eval1.getId(), external.getCurrentUserId());
		}
		*/		
		//ONLY Queued Evaluations has delete link
	
		evalsLogic.deleteEvaluation(eval1.getId(), external.getCurrentUserId());
		
		return ControlPanelProducer.VIEW_ID;
	}
	
	//TODO: TO BE REMOVED:method binding to Summary page :evalAdminTitleLink for closed evaluation
	public String previewEvalAction(){
	
		return PreviewEvalProducer.VIEW_ID;
	}
	
	//method binding to the "Cancel" button  on the remove_template.html
	public String cancelRemoveTemplateAction(){
		
		return ControlPanelProducer.VIEW_ID;
	}
	
	//method binding to the "Remove Template" button  on the remove_template.html
	public String removeTemplateAction(){

		String currentUserId = external.getCurrentUserId();
	
		Long id = new Long("-1");
		EvalTemplate template1 =null;
		try{
			id = Long.valueOf(tmplId);
		}catch(NumberFormatException fe){	
			log.fatal(fe);
		}
		if(id.intValue()== -1) 
			log.error("Error inside removeEvalAction()");
		else
			//template1 = logic.getTemplateById(id);
			template1 = templatesLogic.getTemplateById(id);
		
		if(template1 != null){
			//delete template, need to deleted items entry
			//Set items = template1.getItems();//get all the items
			/*
			List allItems = new ArrayList(template1.getItems());			
			//filter out the block child items, to get a list non-child items
			List ncItemsList = ItemBlockUtils.getNonChildItems(allItems);
			
			for(int i = 0; i < ncItemsList.size(); i++){
				EvalItem item = (EvalItem) ncItemsList.get(i);
				
				//need to check if it is a Block parent, delete child items first
				if(item.getClassification().equals(EvalConstants.ITEM_TYPE_BLOCK) && 
						item.getBlockParent().booleanValue()== true){
					Long parentID = item.getId();
					Integer blockID = new Integer(parentID.intValue());
				
					//List childItems = logic.findItem(blockID);
					List childItems = ItemBlockUtils.getChildItmes(allItems, blockID);
					if(childItems !=null && childItems.size() >0 ){
						for(int k=0; k< childItems.size();k++){
							EvalItem cItem = (EvalItem) childItems.get(k);
							//logic.deleteItem(cItem.getId(), currentUserId);
							itemsLogic.deleteItem(cItem.getId(), currentUserId);
						}
					}
				}//end of check: block child
				itemsLogic.deleteItem(item.getId(), currentUserId);
				*/
			List allItems = new ArrayList(template1.getTemplateItems());			
			//filter out the block child items, to get a list non-child items
			List ncItemsList = ItemBlockUtils.getNonChildItems(allItems);
			
			for(int i = 0; i < ncItemsList.size(); i++){
				EvalTemplateItem tempItem = (EvalTemplateItem) ncItemsList.get(i);
				EvalItem item1 = tempItem.getItem();
				//need to check if it is a Block parent, delete child items first
				if(item1.getClassification().equals(EvalConstants.ITEM_TYPE_BLOCK) && 
						tempItem.getBlockParent().booleanValue()== true){
					Long parentID = tempItem.getId();
					Integer blockID = new Integer(parentID.intValue());
				
					//List childItems = logic.findItem(blockID);
					List childItems = ItemBlockUtils.getChildItmes(allItems, blockID);
					if(childItems !=null && childItems.size() >0 ){
						for(int k=0; k< childItems.size();k++){
							EvalTemplateItem tItem = (EvalTemplateItem) childItems.get(k);
							//logic.deleteItem(cItem.getId(), currentUserId);
							itemsLogic.deleteTemplateItem(tItem.getId(), currentUserId);
						}
					}
				}//end of check: block child
			
				//logic.deleteItem(item, currentUserId);
				itemsLogic.deleteTemplateItem(tempItem.getId(), currentUserId);
			}
			//logic.deleteTemplate(template1);
			templatesLogic.deleteTemplate(template1.getId(), currentUserId);
		}
		return ControlPanelProducer.VIEW_ID;
	}
	
	
	private Date changeStringToDate (String dateStr) {
		Date returnDate = null;
		
		if(dateStr != null && dateStr.length()>0) {	
			try{
				DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
				returnDate = formatter.parse(dateStr);
			}
			catch (ParseException e) 	{	
				log.error(e);
			}
		}
		return returnDate;
	} 
	
	
	private void checkEvalStartDate(EvalEvaluation myEval){
		
		/*
		 * check if start date is the same as today's date, set startDate as today's date time, 
		 * as when we parse the string to a date, the time filed by default is
		 * */
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(myEval.getStartDate());
		int year_start = calendar.get(Calendar.YEAR);
		int month_start = calendar.get(Calendar.MONTH);
		int day_start = calendar.get(Calendar.DAY_OF_MONTH);
	
		Date today = new Date();
		calendar.setTime(today);
		int year_today = calendar.get(Calendar.YEAR);
		int month_today = calendar.get(Calendar.MONTH);
		int day_today = calendar.get(Calendar.DAY_OF_MONTH);
	
		if(year_start == year_today && month_start == month_today && day_start == day_today) {
			
			/*
			 * Non-javadoc comment.
			 * EVALSYS-8: We do not need to add these 5 minutes. This is because
			 * the buffer check done by Aaron in logic layer is to prevent the 
			 * user from entering dates that are in too much in past - kahuja.   
			 */
			//need to set time a little big later than new Date(), otherwise exception
			//calendar.add(Calendar.MINUTE, 5);
			
			myEval.setStartDate(calendar.getTime());		
		}	
	}
	
	/*
	 * Non-Javadoc comments:
	 * EVALSYS-7: We are not taking time from the user for any dates (start, due, view),
	 * but for the start date we are adding the time in above checkEvalStartDate method. 
	 * In logic layer there is a check that due date should not be before start date. 
	 * Thus, if due date is same as start date then we need to make the time of due date 
	 * to be 1 second more that start date - kahuja.
	 * 
	 */
	private void checkDueDate() {

		Calendar calendarStart = new GregorianCalendar();
		calendarStart.setTime(eval.getStartDate());
		int year_start = calendarStart.get(Calendar.YEAR);
		int month_start = calendarStart.get(Calendar.MONTH);
		int day_start = calendarStart.get(Calendar.DAY_OF_MONTH);
		
		Calendar calendarDue = new GregorianCalendar();
		calendarDue.setTime(eval.getDueDate());
		int year_due = calendarDue.get(Calendar.YEAR);
		int month_due = calendarDue.get(Calendar.MONTH);
		int day_due = calendarDue.get(Calendar.DAY_OF_MONTH);
		
		if ( year_start == year_due && month_start == month_due && day_start == day_due ){
			calendarDue.setTime(eval.getStartDate());
			calendarDue.add(Calendar.SECOND, 1);
			eval.setDueDate(calendarDue.getTime());
			eval.setStopDate(eval.getDueDate());
		}
	}
}

