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
import java.util.HashMap;

import java.util.List;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalAssignsLogic;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignContext;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
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
 * This is the backing bean of the create evaluation process
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

	// Used on assign confirm page. That is saved to avoid going to database again.
//	private Map siteIdsTitle;			

	//Used to link the proper template object with the evaluation 
	private List listOfTemplates;
	
	//TODO: need to merge with public field: eval, now use other string to avoid failing of other page 
	public Long evalId; //used to ELBinding to the evaluation ID to be removed on Control Panel
	public String tmplId; //used to ELBinding To the template ID to be removed on Control Panel 

	private static Log log = LogFactory.getLog(EvaluationBean.class);
	
/*	//Indicate the latest origination of EvalStart page
	private String evalStartFlag ;
	public void setEvalStartFlag(String flag){
		this.evalStartFlag = flag;
	}
*/
/*
	private EvaluationLogic logic;
	public void setLogic(EvaluationLogic logic) {
		this.logic = logic;
	}
*/
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
	
	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic( EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}
	
	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}
	
	private EvalEvaluationsLogic evalsLogic;
	public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
		this.evalsLogic = evalsLogic;
	}
	
	private EvalAssignsLogic assignsLogic;
	public void setAssignsLogic(EvalAssignsLogic assignsLogic) {
		this.assignsLogic = assignsLogic;
	}
	
	private EvalResponsesLogic responsesLogic;	
	public void setResponsesLogic(EvalResponsesLogic responsesLogic) {
		this.responsesLogic = responsesLogic;
	}
	
	private EvalEmailsLogic emailsLogic;	
	public void setEmailsLogic(EvalEmailsLogic emailsLogic) {
		this.emailsLogic = emailsLogic;
	}
	
	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
	}
	
	/*
	 * INITIALIZATION
	 */
	public void init() {
		
		//no more TemplateBean - wdh
		//log.debug("Initializing the TemplateBean....");
		
		/*
		if (logic == null) {
			throw new NullPointerException("logic is null");
		}*/
	}

	
	/*
	 * MAJOR METHOD DEFINITIONS
	 */
	
	 // @deprecated - will be removed before release
/*	public List getTemplatesToDisplay() {
		//Get the list of templates from the logic layer.
		return logic.getTemplatesToDisplay(logic.getCurrentUserId());
	}
*/
	//method binding to the "Cancel" button on evaluation_start.html
/*	REMOVED
 * public String cancelEvalStartAction(){
		if(this.evalStartFlag!=null && this.evalStartFlag.equals(TemplateModifyProducer.VIEW_ID))
			return TemplateModifyProducer.VIEW_ID;		
		else 
			return SummaryProducer.VIEW_ID;//default coming from Summary page
		
	}
*/	
	
	//method binding to the "Continue to Settings" button on evaluation_start.html
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
	

	//method binding to the "Continue to Assign to Courses" button on the evaluation_setting.html
	public String continueAssigningAction()	{	
		
		//Clear the selected sites 
		selectedSakaiSiteIds = null;
		
		return EvaluationAssignProducer.VIEW_ID;
	}

	//method binding to the "Save Settings" button on the evaluation_setting.html
	public String saveSettingsAction() {	
		//TODO - Save the settings only
	
		eval.setLastModified(new Date());
		/*
		 * if evaluation is active, startDate is disabled, and so it is null value
		 * only save startDate for queued evaluation
		 */
		Date today = new Date();
		if(today.before(eval.getStartDate())){
			eval.setStartDate(changeStringToDate(this.startDate));
		}
		
		eval.setDueDate(changeStringToDate(this.dueDate));
		eval.setViewDate(changeStringToDate(this.viewDate));
		eval.setStudentsDate(changeStringToDate(this.studentsDate));
		eval.setInstructorsDate(changeStringToDate(this.instructorsDate));

		//This was needed by columbia so on HTML page not shown. Here making equal to due date. 
		eval.setStopDate(eval.getDueDate());
	
		if (eval.getResultsPrivate() == null)
			eval.setResultsPrivate(Boolean.FALSE);
		
		if (eval.getBlankResponsesAllowed() == null)
			eval.setBlankResponsesAllowed(Boolean.FALSE);
		
		if (eval.getModifyResponsesAllowed() == null)
			eval.setModifyResponsesAllowed(Boolean.FALSE);
		
		if (eval.getUnregisteredAllowed() == null)
			eval.setUnregisteredAllowed(Boolean.FALSE);
		
		//logic.saveEvaluation(eval, logic.getCurrentUserId());
		/*
		 * check if start date is the same as today's date, set startDate as today's date time, 
		 * as when we parse the string to a date, the time filed by default is zero
		 * */
		checkEvalStartDate(eval);

		//See the commend with the method at the end of this class.
		checkDueDate();
		
		evalsLogic.saveEvaluation(eval, external.getCurrentUserId());
		
	    return ControlPanelProducer.VIEW_ID;
	}
	
	//method binding to the "Cancel" button on the evaluation_assign.html
	public String cancelAssignAction() {	
	    return SummaryProducer.VIEW_ID;
	}
	
	//method binding to the "Edit Settings" button on the evaluation_assign.html
	public String backToSettingsAction() {	
	    return EvaluationSettingsProducer.VIEW_ID;
	}

	//method binding to the "Save Assigned Courses" button on the evaluation_assign.html
	public String confirmAssignCoursesAction() {	

		//At least 1 site check box need to be checked.
		if (selectedSakaiSiteIds == null || selectedSakaiSiteIds.length == 0) {
			return EvaluationAssignProducer.VIEW_ID;
		}
		else {
			//enrollment = logic.getEnrollment(selectedSakaiSiteIds);
			//get enrollemnt on by one 
			enrollment =  new int[selectedSakaiSiteIds.length];
			for(int i =0; i<selectedSakaiSiteIds.length; i++){
				Set s = external.getUserIdsForContext(selectedSakaiSiteIds[i], EvalConstants.PERM_TAKE_EVALUATION);
				enrollment[i] = s.size();				
			}

			return EvaluationAssignConfirmProducer.VIEW_ID;
		}
	}

	/*
	 * method binding to the "Save Changes" button on the modify_email.html for 
	 * 	the original link from email_available
	 */
	public String saveAvailableEmailTemplate(){
		return "available";
	}

	/*
	 * method binding to the "Save Changes" button on the modify_email.html for 
	 * 	original link from email_reminder
	 */
	public String saveReminderEmailTemplate(){
		return "reminder";
	}
	
	/*
	 * method binding to the "Modify this Email Template" button on the preview_email.html for 
	 * 	the original link from email_available
	 */
	public String modifyAvailableEmailTemplate(){
		return "available";
	}
	

	/*
	 * method binding to the "Modify this Email Template" button on the preview_email.html for 
	 * 	the original link from email_reminder
	 */
	public String modifyReminderEmailTemplate(){
		return "reminder";
	}

	
	
	  //@deprecated This is a pointless passthrough method	 
/*	public Map getSites() {
		siteIdsTitle = logic.getSites();
		return siteIdsTitle;
	}
*/
	/**
	 * Method binding to the "Done" button on evaluation_assign_confirm.html
	 * When come from control panel then saveSettingsAction method is called. 
	 */
	public String doneAssignmentAction() {	

		// need to load the template here before we try to save it because it is stale -AZ
		eval.setTemplate( templatesLogic.getTemplateById( eval.getTemplate().getId() ) );

		//The main evaluation section with all the settings.
		eval.setLastModified(new Date());
		eval.setOwner(external.getCurrentUserId());
		eval.setStartDate(changeStringToDate(startDate));
		eval.setDueDate(changeStringToDate(dueDate));
		eval.setStopDate(eval.getDueDate());            //needed by columbia so as of now making equal to due date
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
	
	//method binding to evaluation_assign_confirm page: "Change Assigned Courses" button
	public String changeAssignedCourseAction(){
		//TODO: for quued evaluation coming from control panel page 
		return EvaluationAssignProducer.VIEW_ID;
	}
	
	public void clearEvaluation(){
		this.eval = new EvalEvaluation(); 
	}
	
	//method binding to "Start Evaluation" link/commans on Control Panel Page
	public String startEvaluation(){
		
		clearEvaluation();
		
		return EvaluationStartProducer.VIEW_ID;
	}
	
	/*
	 * method binding to
	 * 1) Control Panel page: edit command button/link
	 * 2) Summary page: evalAdminTitleLink for queued, active evaluation
	 * 
	 * This method is prepareing backing Bean data  for EditSettings page
	 */
	
	public String editEvalSettingAction(){
	
		//eval = logic.getEvaluationById(eval.getId());
		eval = evalsLogic.getEvaluationById(eval.getId());
			
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		//not -null field
		startDate = df.format(eval.getStartDate()); 
		stopDate = df.format(eval.getStopDate());   
		dueDate = df.format(eval.getDueDate());
		viewDate = df.format(eval.getViewDate());
		if(eval.getStudentsDate() != null) 
			studentsDate = df.format(eval.getStudentsDate());
		if(eval.getInstructorsDate() != null)
			instructorsDate = df.format(eval.getInstructorsDate());
		

		return EvaluationSettingsProducer.VIEW_ID;
	}
	
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

