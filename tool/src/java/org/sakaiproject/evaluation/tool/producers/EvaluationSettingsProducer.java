/******************************************************************************
 * EvaluationStartProducer.java - created by kahuja@vt.edu on Oct 05, 2006
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
package org.sakaiproject.evaluation.tool.producers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBoundList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
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
 * Setting misc options on a Evaluation page
 * 
 * @author:Kapil Ahuja (kahuja@vt.edu)
 * @author: Rui Feng (fengr@vt.edu)
 */

public class EvaluationSettingsProducer implements ViewComponentProducer, NavigationCaseReporter {
	public static final String VIEW_ID = "evaluation_settings"; //$NON-NLS-1$
	
	public String getViewID() {
		return VIEW_ID;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	
	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
    }
    
	private EvaluationBean evaluationBean;
	public void setEvaluationBean(EvaluationBean evaluationBean) {
		this.evaluationBean = evaluationBean;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		
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
		UIOutput.make(form, "eval-results-viewable-private", messageLocator.getMessage("evalsettings.results.viewable.private")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "private-warning-desc", messageLocator.getMessage("evalsettings.private.warning.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		UIBoundBoolean.make(form, "resultsPrivate", "#{evaluationBean.eval.resultsPrivate}", null); //$NON-NLS-1$ //$NON-NLS-2$
		
		/*
		 * Non Javadoc comment.
		 * Show the settings below only if "EvalSettings.STUDENT_VIEW_RESULTS" is set as configurable 
		 * i.e. null in the database OR is set as true in database.
		 */
		if ( settings.get(EvalSettings.STUDENT_VIEW_RESULTS) == null ||
				((Boolean)settings.get(EvalSettings.STUDENT_VIEW_RESULTS)).booleanValue() ) {

			UIBranchContainer showResultsToStudents = UIBranchContainer.make(form, "showResultsToStudents:"); //$NON-NLS-1$
			UIOutput.make(showResultsToStudents, "eval-results-viewable-students", messageLocator.getMessage("evalsettings.results.viewable.students"));		 //$NON-NLS-1$ //$NON-NLS-2$
			UIBoundBoolean.make(showResultsToStudents, "studentViewResults", "#{evaluationBean.studentViewResults}", null); //$NON-NLS-1$ //$NON-NLS-2$
			UIInput.make(showResultsToStudents, "studentsDate", "#{evaluationBean.studentsDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
			UILink.make(showResultsToStudents, "calenderImageForResultsToStudents", "$context/content/images/calendar.gif");		 //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		/*
		 * Non Javadoc comment.
		 * Show the settings below only if "EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS" is set as configurable 
		 * i.e. null in the database OR is set as true in database.
		 */
		if ( settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS) == null ||
				((Boolean)settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS)).booleanValue() ) {

			UIBranchContainer showResultsToInst = UIBranchContainer.make(form, "showResultsToInst:"); //$NON-NLS-1$
			UIOutput.make(showResultsToInst, "eval-results-viewable-instructors", messageLocator.getMessage("evalsettings.results.viewable.instructors")); //$NON-NLS-1$ //$NON-NLS-2$
			UIBoundBoolean.make(showResultsToInst, "instructorViewResults", "#{evaluationBean.instructorViewResults}", null); //$NON-NLS-1$ //$NON-NLS-2$
			UIInput.make(showResultsToInst, "instructorsDate", "#{evaluationBean.instructorsDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
			UILink.make(showResultsToInst, "calenderImageForResultsToInst", "$context/content/images/calendar.gif");		 //$NON-NLS-1$ //$NON-NLS-2$
		}

		UIOutput.make(form, "eval-results-viewable-admin-note", messageLocator.getMessage("evalsettings.results.viewable.admin.note")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "student-completion-settings-header", messageLocator.getMessage("evalsettings.student.completion.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$
		
		/*
		 * Non Javadoc comment.
		 * Show the settings below only if "EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED" is set as configurable 
		 * i.e. null in the database OR is set as true in database.
		 */
		if ( settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED) == null ||
				((Boolean)settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED)).booleanValue() ) {

			UIBranchContainer showBlankQuestionAllowedToStut = UIBranchContainer.make(form, "showBlankQuestionAllowedToStut:"); //$NON-NLS-1$
			UIOutput.make(showBlankQuestionAllowedToStut, "blank-responses-allowed-desc", messageLocator.getMessage("evalsettings.blank.responses.allowed.desc"));		 //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(showBlankQuestionAllowedToStut, "blank-responses-allowed-note", messageLocator.getMessage("evalsettings.blank.responses.allowed.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
			UIBoundBoolean.make(showBlankQuestionAllowedToStut, "blankResponsesAllowed", "#{evaluationBean.eval.blankResponsesAllowed}", null); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/*
		 * Non Javadoc comment.
		 * Show the settings below only if "EvalSettings.STUDENT_MODIFY_RESPONSES" is set as configurable 
		 * i.e. null in the database OR is set as true in database.
		 */
		if ( settings.get(EvalSettings.STUDENT_MODIFY_RESPONSES) == null ||
				((Boolean)settings.get(EvalSettings.STUDENT_MODIFY_RESPONSES)).booleanValue() ) {

			UIBranchContainer showModifyResponsesAllowedToStu = UIBranchContainer.make(form, "showModifyResponsesAllowedToStu:"); //$NON-NLS-1$
			UIOutput.make(showModifyResponsesAllowedToStu, "modify-responses-allowed-desc", messageLocator.getMessage("evalsettings.modify.responses.allowed.desc"));	 //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(showModifyResponsesAllowedToStu, "modify-responses-allowed-note", messageLocator.getMessage("evalsettings.modify.responses.allowed.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
			UIBoundBoolean.make(showModifyResponsesAllowedToStu, "modifyResponsesAllowed", "#{evaluationBean.eval.modifyResponsesAllowed}", null); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		//This options are commented for some time.
		if (false){
			UIBranchContainer showUnregAllowedOption = UIBranchContainer.make(form, "showUnregAllowedOption:"); //$NON-NLS-1$
			UIOutput.make(showUnregAllowedOption, "unregistered-allowed-desc", messageLocator.getMessage("evalsettings.unregistered.allowed.desc"));	 //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(showUnregAllowedOption, "unregistered-allowed-note", messageLocator.getMessage("evalsettings.unregistered.allowed.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
			UIBoundBoolean.make(showUnregAllowedOption, "unregisteredAllowed", "#{evaluationBean.eval.unregisteredAllowed}", null); //$NON-NLS-1$ //$NON-NLS-2$
		}

		UIOutput.make(form, "admin-settings-header", messageLocator.getMessage("evalsettings.admin.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "admin-settings-instructions", messageLocator.getMessage("evalsettings.admin.settings.instructions")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "instructor-opt-desc", messageLocator.getMessage("evalsettings.instructor.opt.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		
		/*
		 * Non Javadoc comment.
		 * If "EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE" is set as configurable 
		 * i.e. null in the database then show the instructor opt select box.
		 * Else just show the value as label. 
		 */
		if ( settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE) == null ) {

			UIBranchContainer showInstUseFromAboveOptions = UIBranchContainer.make(form, "showInstUseFromAboveOptions:"); //$NON-NLS-1$
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
			UIBranchContainer showInstUseFromAboveLabel = UIBranchContainer.make(form, "showInstUseFromAboveLabel:"); //$NON-NLS-1$
			String instUseFromAboveValue = (String) settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE);
			
			if ( (EvalConstants.INSTRUCTOR_OPT_IN).equals(instUseFromAboveValue) )
				instUseFromAboveValue = messageLocator.getMessage("evalsettings.instructors.label.opt.in"); //$NON-NLS-1$ 
			else if ( (EvalConstants.INSTRUCTOR_OPT_OUT).equals(instUseFromAboveValue) )
				instUseFromAboveValue = messageLocator.getMessage("evalsettings.instructors.label.opt.out"); //$NON-NLS-1$ 
			else
				instUseFromAboveValue = messageLocator.getMessage("evalsettings.instructors.label.required"); //$NON-NLS-1$ 
				
			UIOutput.make(showInstUseFromAboveLabel, "instUseFromAboveValue", instUseFromAboveValue); //$NON-NLS-1$ 
		}
			
		
		UIOutput.make(form, "eval-reminder-settings-header", messageLocator.getMessage("evalsettings.reminder.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "eval-available-mail-desc", messageLocator.getMessage("evalsettings.available.mail.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		UIInternalLink.make(form, "emailAvailable_link", messageLocator.getMessage("evalsettings.available.mail.link"), new EvalViewParameters(PreviewEmailProducer.VIEW_ID, null, "available"));	 //$NON-NLS-1$ //$NON-NLS-2$
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
		
		UIInternalLink.make(form, "emailReminder_link", messageLocator.getMessage("evalsettings.reminder.mail.link"), new EvalViewParameters(PreviewEmailProducer.VIEW_ID, null, "reminder"));	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		UIInput.make(form, "reminderFromEmail", "#{evaluationBean.eval.reminderFromEmail}", null); //$NON-NLS-1$ //$NON-NLS-2$
		
		/*
		 * if this evaluation is already saved, show "Save Settings" button
		 * else this is the "Continue to Assign to Courses" button
		 */
		if(evaluationBean.eval.getId() != null)
			UICommand.make(form, "continueAssigning", messageLocator.getMessage("evalsettings.save.settings.link"), "#{evaluationBean.saveSettingsAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		else 
			UICommand.make(form, "continueAssigning", messageLocator.getMessage("evalsettings.continue.assigning.link"), "#{evaluationBean.continueAssigningAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		UIOutput.make(form, "cancel-button", messageLocator.getMessage("general.cancel.button"));

	}
	
	public List reportNavigationCases() {
		List i = new ArrayList();
		
		i.add(new NavigationCase(EvaluationStartProducer.VIEW_ID, new EvalViewParameters(EvaluationStartProducer.VIEW_ID, null, EvaluationSettingsProducer.VIEW_ID)));
		i.add(new NavigationCase(ControlPanelProducer.VIEW_ID, new SimpleViewParameters(ControlPanelProducer.VIEW_ID)));
		i.add(new NavigationCase(EvaluationAssignProducer.VIEW_ID, new SimpleViewParameters(EvaluationAssignProducer.VIEW_ID)));
	
		return i;
	}
}
