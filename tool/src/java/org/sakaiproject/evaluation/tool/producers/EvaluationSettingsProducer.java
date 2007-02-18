/******************************************************************************
 * EvaluationSettingsProducer.java - created by kahuja@vt.edu on Oct 05, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 * Rui Feng (fengr@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool.producers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.params.EmailViewParameters;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBoundList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * This producer is used to render the evaluation settings page.
 * It is used when user either creates a new evaluation (coming
 * forward from "Start Evaluation" page or coming backward from
 * "Assign Evaluation to courses" page) or from control panel to
 * edit the existing settings.
 * 
 * @author:Kapil Ahuja (kahuja@vt.edu)
 * @author:Rui Feng (fengr@vt.edu)
 */
public class EvaluationSettingsProducer implements ViewComponentProducer, NavigationCaseReporter {

	/**
	 * This is used for navigation within the system.
	 */
	public static final String VIEW_ID = "evaluation_settings"; //$NON-NLS-1$

	/**
	 * Used to return the view id of this producer. The view id is used for 
	 * navigation within the system.
	 * @return view id of this producer
	 */
	public String getViewID() {
		return VIEW_ID;
	}

	// Spring injection 
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	
	// Spring injection 
	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
    }
    
	// Spring injection 
	private EvaluationBean evaluationBean;
	public void setEvaluationBean(EvaluationBean evaluationBean) {
		this.evaluationBean = evaluationBean;
	}
	
	// Spring injection 
	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, 
	 * 																uk.org.ponder.rsf.viewstate.ViewParameters, 
	 * 																uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		
		//Displaying the top link, page title, page description, and creating the HTML form 
		UIOutput.make(tofill, "eval-settings-title", messageLocator.getMessage("evalsettings.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
							new SimpleViewParameters(SummaryProducer.VIEW_ID));	
		
		UIForm form = UIForm.make(tofill, "evalSettingsForm"); //$NON-NLS-1$
		UIOutput.make(form, "settings-desc-header", messageLocator.getMessage("evalsettings.settings.desc.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "evaluationTitle", null, "#{evaluationBean.eval.title}"); //$NON-NLS-1$ //$NON-NLS-2$
		
		//if this evaluation is already started, user can not change start date
		Date today = new Date();
		Calendar calendar = new GregorianCalendar();	
		calendar.setTime(today);
		calendar.add(Calendar.DATE, -1);
		Date dummyDate = calendar.getTime();
		SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy"); //$NON-NLS-1$
		UIInput.make(form, "dummyToday",null,simpleDate.format(dummyDate)); //$NON-NLS-1$
		
		UIOutput.make(form, "eval-dates-header", messageLocator.getMessage("evalsettings.dates.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "eval-start-date-header", messageLocator.getMessage("evalsettings.start.date.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "eval-start-date-desc", messageLocator.getMessage("evalsettings.start.date.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		
		if(evaluationBean.eval.getId()!= null){

			//queued evalution
			if( today.before(evaluationBean.eval.getStartDate()) ) {
				UIInput.make(form, "startDate", "#{evaluationBean.startDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
				UIInput.make(form, "evalStatus",null,"queued"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			//started evaluation
			else { 
				UIInput startDateInput = UIInput.make(form, "startDate", "#{evaluationBean.startDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
				Map attrmap = new HashMap();
				attrmap.put("disabled", "disabled"); //$NON-NLS-1$ //$NON-NLS-2$
				startDateInput.decorators = new DecoratorList(new UIFreeAttributeDecorator(attrmap)); 
				UIInput.make(form, "evalStatus",null,"active"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else {
			UIInput.make(form, "startDate", "#{evaluationBean.startDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
			UIInput.make(form, "evalStatus",null,"new"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		//Show the "Stop date" text box only if it is set as yes in the System settings
		if ( ((Boolean)settings.get(EvalSettings.EVAL_USE_STOP_DATE)).booleanValue() ) {
			UIBranchContainer showStopDate = UIBranchContainer.make(form, "showStopDate:"); //$NON-NLS-1$
			UIOutput.make(showStopDate, "eval-stop-date-header", messageLocator.getMessage("evalsettings.stop.date.header"));			 //$NON-NLS-1$ //$NON-NLS-2$
			UIInput.make(showStopDate, "stopDate", "#{evaluationBean.stopDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
			UILink.make(showStopDate, "calenderImageForStop", "$context/content/images/calendar.gif");		 //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		UIOutput.make(form, "eval-due-date-header", messageLocator.getMessage("evalsettings.due.date.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput.make(form, "dueDate", "#{evaluationBean.dueDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "eval-view-date-header", messageLocator.getMessage("evalsettings.view.date.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "eval-view-date-desc", messageLocator.getMessage("evalsettings.view.date.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput.make(form, "viewDate", "#{evaluationBean.viewDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
		UILink.make(form, "calenderImage", "$context/content/images/calendar.gif");		 //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(form, "eval-results-viewable-header", messageLocator.getMessage("evalsettings.results.viewable.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "eval-results-viewable-private-start", messageLocator.getMessage("evalsettings.results.viewable.private.start")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "eval-results-viewable-private-middle", messageLocator.getMessage("evalsettings.results.viewable.private.middle")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "userInfo", externalLogic.getUserDisplayName(externalLogic.getCurrentUserId()));	 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "private-warning-desc", messageLocator.getMessage("evalsettings.private.warning.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		UIBoundBoolean.make(form, "resultsPrivate", "#{evaluationBean.eval.resultsPrivate}", null); //$NON-NLS-1$ //$NON-NLS-2$

		/* 
		 * (non-Javadoc)
		 * Variable is used to decide whether to show the view date textbox for student 
		 * and instructor separately or not.
		 */
		boolean sameViewDateForAll = ((Boolean) settings.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES)).booleanValue();
		
		/* 
		 * (non-Javadoc)
		 * If "EvalSettings.STUDENT_VIEW_RESULTS" is set as configurable i.e. NULL 
		 * in the database OR is set as TRUE in database, then show the checkbox.
		 * Else do not show the checkbox and just bind the value to FALSE.
		 */
		Boolean tempValue = (Boolean) settings.get(EvalSettings.STUDENT_VIEW_RESULTS);
		if ( tempValue == null || tempValue.booleanValue() ) {

			UIBranchContainer showResultsToStudents = UIBranchContainer.make(form, "showResultsToStudents:"); 	//$NON-NLS-1$
			UIOutput.make(showResultsToStudents, "eval-results-viewable-students", messageLocator.getMessage("evalsettings.results.viewable.students")); //$NON-NLS-1$ //$NON-NLS-2$
			
			//If system setting was null implies normal checkbox. If it was TRUE then a disabled selected checkbox 
			if (tempValue == null) {
				UIBoundBoolean.make(showResultsToStudents, "studentViewResults", "#{evaluationBean.studentViewResults}", tempValue); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				//Display only (disabled) and selected check box 
				UIBoundBoolean stuViewCheckbox = UIBoundBoolean.make(showResultsToStudents, "studentViewResults", tempValue); //$NON-NLS-1$ 
				setDisabledAttribute(stuViewCheckbox);

				// As we have disabled the check box => RSF will not bind the value => binding it explicitly.
				form.parameters.add(new UIELBinding("#{evaluationBean.studentViewResults}", tempValue)); //$NON-NLS-1$
			}
			
			//If same view date all then show a label else show a text box.
			if ( sameViewDateForAll ) {
				UIBranchContainer showResultsToStuLabel = UIBranchContainer.make(showResultsToStudents, "showResultsToStuLabel:"); 	//$NON-NLS-1$
				UIOutput.make(showResultsToStuLabel, "eval-results-stu-inst-date-label", messageLocator.getMessage("evalsettings.results.stu.inst.date.label")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				UIBranchContainer showResultsToStuDate = UIBranchContainer.make(showResultsToStudents, "showResultsToStuDate:");	//$NON-NLS-1$
				UIInput.make(showResultsToStuDate, "studentsDate", "#{evaluationBean.studentsDate}", null); 						//$NON-NLS-1$ //$NON-NLS-2$
				UILink.make(showResultsToStuDate, "calenderImageForResultsToStudents", "$context/content/images/calendar.gif");		//$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else {
			form.parameters.add(new UIELBinding("#{evaluationBean.studentViewResults}", Boolean.FALSE)); //$NON-NLS-1$
		}
		
		/*
		 * (non-javadoc) 
		 * If "EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS" is set as configurable i.e. NULL 
		 * in the database OR is set as TRUE in database, then show the checkbox.
		 * Else do not show the checkbox and just bind the value to FALSE.
		 */
		tempValue = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
		if ( tempValue == null || tempValue.booleanValue() ) {

			UIBranchContainer showResultsToInst = UIBranchContainer.make(form, "showResultsToInst:");	//$NON-NLS-1$
			UIOutput.make(showResultsToInst, "eval-results-viewable-instructors", messageLocator.getMessage("evalsettings.results.viewable.instructors")); //$NON-NLS-1$ //$NON-NLS-2$
			
			//If system setting was null implies normal checkbox. If it was TRUE then a disabled selected checkbox 
			if (tempValue == null) {
				UIBoundBoolean.make(showResultsToInst, "instructorViewResults", "#{evaluationBean.instructorViewResults}", tempValue); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				//Display only (disabled) and selected check box 
				UIBoundBoolean instViewCheckbox = UIBoundBoolean.make(showResultsToInst, "instructorViewResults", tempValue); //$NON-NLS-1$ 
				setDisabledAttribute(instViewCheckbox);

				// As we have disabled the check box => RSF will not bind the value => binding it explicitly.
				form.parameters.add(new UIELBinding("#{evaluationBean.instructorViewResults}", tempValue)); //$NON-NLS-1$
			}

			//If same view date all then show a label else show a text box.
			if ( sameViewDateForAll ) {
				UIBranchContainer showResultsToInstLabel = UIBranchContainer.make(showResultsToInst, "showResultsToInstLabel:"); 	//$NON-NLS-1$
				UIOutput.make(showResultsToInstLabel, "eval-results-stu-inst-date-label", messageLocator.getMessage("evalsettings.results.stu.inst.date.label")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				UIBranchContainer showResultsToInstDate = UIBranchContainer.make(showResultsToInst, "showResultsToInstDate:"); 	//$NON-NLS-1$
				UIInput.make(showResultsToInstDate, "instructorsDate", "#{evaluationBean.instructorsDate}", null); 				//$NON-NLS-1$ //$NON-NLS-2$
				UILink.make(showResultsToInstDate, "calenderImageForResultsToInst", "$context/content/images/calendar.gif"); 	//$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else {
			form.parameters.add(new UIELBinding("#{evaluationBean.instructorViewResults}", Boolean.FALSE)); //$NON-NLS-1$
		}

		UIOutput.make(form, "eval-results-viewable-admin-note", messageLocator.getMessage("evalsettings.results.viewable.admin.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		/*
		 * (non-javadoc) 
		 * If "EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED" is set as configurable i.e. NULL 
		 * in the database OR is set as TRUE in database, then show the checkbox.
		 * Else do not show the checkbox and just bind the value to FALSE.
		 * 
		 * Note: The variable showStudentCompletionHeader is used to show "student-completion-settings-header"
		 * It is true only if either of the three "if's" below are evaluated to true.
		 */
		boolean showStudentCompletionHeader = false;
		tempValue = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
		if ( tempValue == null || tempValue.booleanValue() ) {

			showStudentCompletionHeader = true;
			UIBranchContainer showBlankQuestionAllowedToStut = UIBranchContainer.make(form, "showBlankQuestionAllowedToStut:"); //$NON-NLS-1$
			UIOutput.make(showBlankQuestionAllowedToStut, "blank-responses-allowed-desc", messageLocator.getMessage("evalsettings.blank.responses.allowed.desc")); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(showBlankQuestionAllowedToStut, "blank-responses-allowed-note", messageLocator.getMessage("evalsettings.blank.responses.allowed.note")); //$NON-NLS-1$ //$NON-NLS-2$
			
			//If system setting was null implies normal checkbox. If it was TRUE then a disabled selected checkbox 
			if (tempValue == null) {
				UIBoundBoolean.make(showBlankQuestionAllowedToStut, "blankResponsesAllowed", "#{evaluationBean.eval.blankResponsesAllowed}", tempValue); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				//Display only (disabled) and selected check box 
				UIBoundBoolean stuLeaveUnanswered = UIBoundBoolean.make(showBlankQuestionAllowedToStut, "blankResponsesAllowed", tempValue); //$NON-NLS-1$
				setDisabledAttribute(stuLeaveUnanswered);

				// As we have disabled the check box => RSF will not bind the value => binding it explicitly.
				form.parameters.add(new UIELBinding("#{evaluationBean.eval.blankResponsesAllowed}", tempValue)); //$NON-NLS-1$
			}
		}
		else {
			form.parameters.add(new UIELBinding("#{evaluationBean.eval.blankResponsesAllowed}", Boolean.FALSE)); //$NON-NLS-1$
		}

		/*
		 * (non-javadoc) 
		 * If "EvalSettings.STUDENT_MODIFY_RESPONSES" is set as configurable i.e. NULL 
		 * in the database OR is set as TRUE in database, then show the checkbox.
		 * Else do not show the checkbox and just bind the value to FALSE.
		 */
		tempValue = (Boolean) settings.get(EvalSettings.STUDENT_MODIFY_RESPONSES);
		if ( tempValue == null || tempValue.booleanValue() ) {

			showStudentCompletionHeader = true;
			UIBranchContainer showModifyResponsesAllowedToStu = UIBranchContainer.make(form, "showModifyResponsesAllowedToStu:"); //$NON-NLS-1$
			UIOutput.make(showModifyResponsesAllowedToStu, "modify-responses-allowed-desc", messageLocator.getMessage("evalsettings.modify.responses.allowed.desc"));	 //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(showModifyResponsesAllowedToStu, "modify-responses-allowed-note", messageLocator.getMessage("evalsettings.modify.responses.allowed.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
			
			//If system setting was null implies normal checkbox. If it was TRUE then a disabled selected checkbox 
			if (tempValue == null) {
				UIBoundBoolean.make(showModifyResponsesAllowedToStu, "modifyResponsesAllowed", "#{evaluationBean.eval.modifyResponsesAllowed}", tempValue); //$NON-NLS-1$ //$NON-NLS-2$
			}
			else {
				//Display only (disabled) and selected check box 
				UIBoundBoolean stuModifyResponses = UIBoundBoolean.make(showModifyResponsesAllowedToStu, "modifyResponsesAllowed", tempValue); //$NON-NLS-1$
				setDisabledAttribute(stuModifyResponses);

				// As we have disabled the check box => RSF will not bind the value => binding it explicitly.
				form.parameters.add(new UIELBinding("#{evaluationBean.eval.modifyResponsesAllowed}", tempValue)); //$NON-NLS-1$
			}
		}
		else {
			form.parameters.add(new UIELBinding("#{evaluationBean.eval.modifyResponsesAllowed}", Boolean.FALSE)); //$NON-NLS-1$
		}
		
		//This options are commented for some time.
		if (false){

			showStudentCompletionHeader = true;
			UIBranchContainer showUnregAllowedOption = UIBranchContainer.make(form, "showUnregAllowedOption:"); //$NON-NLS-1$
			UIOutput.make(showUnregAllowedOption, "unregistered-allowed-desc", messageLocator.getMessage("evalsettings.unregistered.allowed.desc"));	 //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(showUnregAllowedOption, "unregistered-allowed-note", messageLocator.getMessage("evalsettings.unregistered.allowed.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
			UIBoundBoolean.make(showUnregAllowedOption, "unregisteredAllowed", "#{evaluationBean.eval.unregisteredAllowed}", null); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/*
		 * (non-javadoc) 
		 * Continued from the note above, that is show "student-completion-settings-header" 
		 * only if there are any student completion settings to be displayed on the page.
		 */
		if (showStudentCompletionHeader) {
			UIBranchContainer showStudentCompletionDiv = UIBranchContainer.make(form, "showStudentCompletionDiv:"); //$NON-NLS-1$
			UIOutput.make(showStudentCompletionDiv, "student-completion-settings-header", messageLocator.getMessage("evalsettings.student.completion.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/*
		 * (non-javadoc)
		 * If the person is an admin (any kind), then only we need to show
		 * these instructor opt in/out settings.
		 */
		if ( externalLogic.isUserAdmin(externalLogic.getCurrentUserId()) ) {
			
			UIBranchContainer showInstUseFromAbove = UIBranchContainer.make(form, "showInstUseFromAbove:"); //$NON-NLS-1$
			UIOutput.make(showInstUseFromAbove, "admin-settings-header", messageLocator.getMessage("evalsettings.admin.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(showInstUseFromAbove, "admin-settings-instructions", messageLocator.getMessage("evalsettings.admin.settings.instructions")); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(showInstUseFromAbove, "instructor-opt-desc", messageLocator.getMessage("evalsettings.instructor.opt.desc")); //$NON-NLS-1$ //$NON-NLS-2$

			/*
			 * (non-javadoc) 
			 * If "EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE" is set as configurable 
			 * i.e. NULL in the database then show the instructor opt select box.
			 * Else just show the value as label. 
			 */
			if ( settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE) == null ) {
	
				UIBranchContainer showInstUseFromAboveOptions = UIBranchContainer.make(showInstUseFromAbove, "showInstUseFromAboveOptions:"); //$NON-NLS-1$
				UISelect inst = UISelect.make(showInstUseFromAboveOptions, "instructorOpt"); //$NON-NLS-1$
				inst.selection = new UIInput();
				inst.selection.valuebinding = new ELReference("#{evaluationBean.eval.instructorOpt}"); //$NON-NLS-1$
				UIBoundList instValues = new UIBoundList();
				instValues.setValue(EvaluationConstant.INSTRUCTOR_OPT_VALUES);
				inst.optionlist = instValues;
				String[] instructorOptLabels = 
				{
					messageLocator.getMessage("evalsettings.instructors.label.opt.in"),
					messageLocator.getMessage("evalsettings.instructors.label.opt.out"),
					messageLocator.getMessage("evalsettings.instructors.label.required")
				};
				UIBoundList instNames = new UIBoundList();
				instNames.setValue(instructorOptLabels);
				inst.optionnames = instNames;
			}
			else {
				UIBranchContainer showInstUseFromAboveLabel = UIBranchContainer.make(showInstUseFromAbove, "showInstUseFromAboveLabel:"); //$NON-NLS-1$
				String instUseFromAboveValue = (String) settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE);
				String instUseFromAboveLabel;
				
				if ( (EvalConstants.INSTRUCTOR_OPT_IN).equals(instUseFromAboveValue) )
					instUseFromAboveLabel = messageLocator.getMessage("evalsettings.instructors.label.opt.in"); //$NON-NLS-1$ 
				else if ( (EvalConstants.INSTRUCTOR_OPT_OUT).equals(instUseFromAboveValue) )
					instUseFromAboveLabel = messageLocator.getMessage("evalsettings.instructors.label.opt.out"); //$NON-NLS-1$ 
				else
					instUseFromAboveLabel = messageLocator.getMessage("evalsettings.instructors.label.required"); //$NON-NLS-1$ 
					
				/*
				 * (non-javadoc) 
				 * Displaying the label corresponding to INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE 
				 * value set as system property. 
				 */
				UIOutput.make(showInstUseFromAboveLabel, "instUseFromAboveLabel", instUseFromAboveLabel); //$NON-NLS-1$ 
	
				/*
				 * (non-javadoc) 
				 * Doing the binding of this INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE 
				 * value so that it can be saved in the database 
				 */
				form.parameters.add(new UIELBinding("#{evaluationBean.eval.instructorOpt}", instUseFromAboveValue)); //$NON-NLS-1$  
			}
		} 
		
		UIOutput.make(form, "eval-reminder-settings-header", messageLocator.getMessage("evalsettings.reminder.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIInternalLink.make(form, "emailAvailable_link", messageLocator.getMessage("evalsettings.available.mail.link"), 
            new EmailViewParameters(PreviewEmailProducer.VIEW_ID, null, 
                EvalConstants.EMAIL_TEMPLATE_AVAILABLE));	 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "eval-available-mail-desc", messageLocator.getMessage("evalsettings.available.mail.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "reminder-noresponders-header", messageLocator.getMessage("evalsettings.reminder.noresponders.header")); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Reminder email select box
		UISelect reminder = UISelect.make(form, "reminderDays"); //$NON-NLS-1$
		reminder.selection = new UIInput();
		reminder.selection.valuebinding = new ELReference("#{evaluationBean.eval.reminderDays}"); //$NON-NLS-1$
		UIBoundList reminderValues = new UIBoundList();
		reminderValues.setValue(EvaluationConstant.REMINDER_EMAIL_DAYS_VALUES);
		reminder.optionlist = reminderValues;
		
		String[] reminderEmailDaysLabels = 
		{
			messageLocator.getMessage("evalsettings.reminder.days.0"),
			messageLocator.getMessage("evalsettings.reminder.days.1"),
			messageLocator.getMessage("evalsettings.reminder.days.2"),
			messageLocator.getMessage("evalsettings.reminder.days.3"),
			messageLocator.getMessage("evalsettings.reminder.days.4"),
			messageLocator.getMessage("evalsettings.reminder.days.5"),
			messageLocator.getMessage("evalsettings.reminder.days.6"),
			messageLocator.getMessage("evalsettings.reminder.days.7")
		};
		UIBoundList reminderNames = new UIBoundList();
		reminderNames.setValue(reminderEmailDaysLabels);
		reminder.optionnames = reminderNames;
		
		UIInternalLink.make(form, "emailReminder_link", messageLocator.getMessage("evalsettings.reminder.mail.link"), 
            new EmailViewParameters(PreviewEmailProducer.VIEW_ID, null, EvalConstants.EMAIL_TEMPLATE_REMINDER));	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		UIOutput.make(form, "eval-reminder-mail-desc", messageLocator.getMessage("evalsettings.reminder.mail.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "eval-from-email-header", messageLocator.getMessage("evalsettings.from.email.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput.make(form, "reminderFromEmail", "#{evaluationBean.eval.reminderFromEmail}", null); //$NON-NLS-1$ //$NON-NLS-2$
		
		/*
		 * (non-javadoc) 
		 * if this evaluation is already saved, show "Save Settings" button
		 * else this is the "Continue to Assign to Courses" button
		 */
		if(evaluationBean.eval.getId() != null)
			UICommand.make(form, "continueAssigning", messageLocator.getMessage("evalsettings.save.settings.link"), "#{evaluationBean.saveSettingsAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		else 
			UICommand.make(form, "continueAssigning", messageLocator.getMessage("evalsettings.continue.assigning.link"), "#{evaluationBean.continueAssigningAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		UIOutput.make(form, "cancel-button", messageLocator.getMessage("general.cancel.button"));

	}
	
	/* 
	 * (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
		
		i.add(new NavigationCase(EvaluationStartProducer.VIEW_ID, 
            new EvalViewParameters(EvaluationStartProducer.VIEW_ID, null)));
		i.add(new NavigationCase(ControlPanelProducer.VIEW_ID, new SimpleViewParameters(ControlPanelProducer.VIEW_ID)));
		i.add(new NavigationCase(EvaluationAssignProducer.VIEW_ID, new SimpleViewParameters(EvaluationAssignProducer.VIEW_ID)));
	
		return i;
	}

	/*
	 * (non-Javadoc)
	 * This method is used to make the checkbox appear disabled
	 * where ever needed.
	 */
	private void setDisabledAttribute(UIBoundBoolean checkbox) {
		Map attrmap = new HashMap();
		attrmap.put("disabled", "true");
		checkbox.decorators = new DecoratorList(new UIFreeAttributeDecorator(attrmap)); 
	}
	
}
