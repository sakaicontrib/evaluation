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

import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
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

			if( today.before(evaluationBean.eval.getStartDate())){
				//queued evalution
				UIInput.make(form, "startDate", "#{evaluationBean.startDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
				
				UIInput.make(form, "evalStatus",null,"queued"); //$NON-NLS-1$ //$NON-NLS-2$
			}else{ //started evaluation
				UIInput startDateInput = UIInput.make(form, "startDate", "#{evaluationBean.startDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
				Map attrmap = new HashMap();
				attrmap.put("disabled", "disabled"); //$NON-NLS-1$ //$NON-NLS-2$
				startDateInput.decorators = new DecoratorList(new UIFreeAttributeDecorator(attrmap)); 
				
				UIInput.make(form, "evalStatus",null,"active"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}else {
			UIInput.make(form, "startDate", "#{evaluationBean.startDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
			
			UIInput.make(form, "evalStatus",null,"new"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		//Stop date was a CU thing. So right now not shown on the webpage.
		if (false) {
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
		
		UIOutput.make(form, "eval-results-viewable-students", messageLocator.getMessage("evalsettings.results.viewable.students"));		 //$NON-NLS-1$ //$NON-NLS-2$
		UIBoundBoolean.make(form, "studentViewResults", "#{evaluationBean.studentViewResults}", null); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(form, "eval-results-viewable-instructors", messageLocator.getMessage("evalsettings.results.viewable.instructors")); //$NON-NLS-1$ //$NON-NLS-2$
		UIBoundBoolean.make(form, "instructorViewResults", "#{evaluationBean.instructorViewResults}", null); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput.make(form, "studentsDate", "#{evaluationBean.studentsDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput.make(form, "instructorsDate", "#{evaluationBean.instructorsDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "eval-results-viewable-admin-note", messageLocator.getMessage("evalsettings.results.viewable.admin.note")); //$NON-NLS-1$ //$NON-NLS-2$

		UIOutput.make(form, "student-completion-settings-header", messageLocator.getMessage("evalsettings.student.completion.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "blank-responses-allowed-desc", messageLocator.getMessage("evalsettings.blank.responses.allowed.desc"));		 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "blank-responses-allowed-note", messageLocator.getMessage("evalsettings.blank.responses.allowed.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "modify-responses-allowed-desc", messageLocator.getMessage("evalsettings.modify.responses.allowed.desc"));	 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "modify-responses-allowed-note", messageLocator.getMessage("evalsettings.modify.responses.allowed.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "unregistered-allowed-desc", messageLocator.getMessage("evalsettings.unregistered.allowed.desc"));	 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "unregistered-allowed-note", messageLocator.getMessage("evalsettings.unregistered.allowed.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
		UIBoundBoolean.make(form, "blankResponsesAllowed", "#{evaluationBean.eval.blankResponsesAllowed}", null); //$NON-NLS-1$ //$NON-NLS-2$
		UIBoundBoolean.make(form, "modifyResponsesAllowed", "#{evaluationBean.eval.modifyResponsesAllowed}", null); //$NON-NLS-1$ //$NON-NLS-2$
		UIBoundBoolean.make(form, "unregisteredAllowed", "#{evaluationBean.eval.unregisteredAllowed}", null); //$NON-NLS-1$ //$NON-NLS-2$

		UIOutput.make(form, "admin-settings-header", messageLocator.getMessage("evalsettings.admin.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "admin-settings-instructions", messageLocator.getMessage("evalsettings.admin.settings.instructions")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "instructor-opt-desc", messageLocator.getMessage("evalsettings.instructor.opt.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Instructor opt select box
		UISelect inst = UISelect.make(form, "instructorOpt"); //$NON-NLS-1$
		inst.selection = new UIInput();
		inst.selection.valuebinding = new ELReference("#{evaluationBean.eval.instructorOpt}"); //$NON-NLS-1$
		UIBoundList instValues = new UIBoundList();
		instValues.setValue(EvaluationConstant.INSTRUCTOR_OPT_VALUES);
		inst.optionlist = instValues;
		UIBoundList instNames = new UIBoundList();
		instNames.setValue(EvaluationConstant.INSTRUCTOR_OPT_LABELS);
		inst.optionnames = instNames;
		
		
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
		UIBoundList reminderNames = new UIBoundList();
		reminderNames.setValue(EvaluationConstant.REMINDER_EMAIL_DAYS_LABELS);
		reminder.optionnames = reminderNames;
		
		UIInternalLink.make(form, "emailReminder_link", messageLocator.getMessage("evalsettings.reminder.mail.link"), new EvalViewParameters(PreviewEmailProducer.VIEW_ID, null, "reminder"));	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		
		UIInput.make(form, "reminderFromEmail", "#{evaluationBean.eval.reminderFromEmail}", null); //$NON-NLS-1$ //$NON-NLS-2$
		
		//	UICommand.make(form, "cancelSetting", "#{evaluationBean.cancelSettingAction}");
		/*
		 * TODO: check with Aaron
		 * if this evaluation is already saved, show "Save Settings" button
		 * else this is the "Continue to Assign to Courses" button
		 */
		if(evaluationBean.eval.getId() != null)
			UICommand.make(form, "continueAssigning", messageLocator.getMessage("evalsettings.save.settings.link"), "#{evaluationBean.saveSettingsAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		else 
			UICommand.make(form, "continueAssigning", messageLocator.getMessage("evalsettings.continue.assigning.link"), "#{evaluationBean.continueAssigningAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		//UICommand.make(form, "saveSettings", "Save Settings", "#{evaluationBean.saveSettingsAction}");

	}
	
	public List reportNavigationCases() {
		List i = new ArrayList();
		
		i.add(new NavigationCase(EvaluationStartProducer.VIEW_ID, new EvalViewParameters(EvaluationStartProducer.VIEW_ID, null, EvaluationSettingsProducer.VIEW_ID)));
		
		i.add(new NavigationCase(ControlPanelProducer.VIEW_ID, new SimpleViewParameters(ControlPanelProducer.VIEW_ID)));
		i.add(new NavigationCase(EvaluationAssignProducer.VIEW_ID, new SimpleViewParameters(EvaluationAssignProducer.VIEW_ID)));
	
		return i;
	}
}
