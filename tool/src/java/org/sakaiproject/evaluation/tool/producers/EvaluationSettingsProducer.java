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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.params.EmailViewParameters;
import org.sakaiproject.evaluation.tool.params.TemplateViewParameters;

import uk.org.ponder.arrayutil.ArrayUtil;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.evolvers.DateInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * This producer is used to render the evaluation settings page. It is used when
 * user either creates a new evaluation (coming forward from "Start Evaluation"
 * page or coming backward from "Assign Evaluation to courses" page) or from
 * control panel to edit the existing settings.
 * 
 * @author:Kapil Ahuja (kahuja@vt.edu)
 * @author:Rui Feng (fengr@vt.edu)
 */
public class EvaluationSettingsProducer implements ViewComponentProducer, NavigationCaseReporter {

	public static final String VIEW_ID = "evaluation_settings";
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
	}

	private EvaluationBean evaluationBean;
	public void setEvaluationBean(EvaluationBean evaluationBean) {
		this.evaluationBean = evaluationBean;
	}

	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	private DateInputEvolver dateevolver;
	public void setDateEvolver(DateInputEvolver dateevolver) {
		this.dateevolver = dateevolver;
	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		// Displaying the top link, page title, page description, and creating the
		// HTML form
		UIMessage.make(tofill, "eval-settings-title", "evalsettings.page.title");
		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"),
				new SimpleViewParameters(SummaryProducer.VIEW_ID));

		UIForm form = UIForm.make(tofill, "evalSettingsForm");
		UIMessage.make(form, "settings-desc-header", "evalsettings.settings.desc.header");
		UIOutput.make(form, "evaluationTitle", null, "#{evaluationBean.eval.title}");

		UIMessage.make(form, "eval-dates-header", "evalsettings.dates.header");
		UIMessage.make(form, "eval-start-date-header", "evalsettings.start.date.header");
		UIMessage.make(form, "eval-start-date-desc", "evalsettings.start.date.desc");

		Date today = new Date();
		UIInput startDate = UIInput.make(form, "startDate:", "#{evaluationBean.startDate}");	
		if (evaluationBean.eval.getId() != null) {
			// queued evalution
			if (today.before(evaluationBean.eval.getStartDate())) {
				UIInput.make(form, "evalStatus", null, "queued");
			}
			// started evaluation
			else {
				startDate.decorators = new DecoratorList(new UIDisabledDecorator());
				UIInput.make(form, "evalStatus", null, "active");
			}
		} else {
			UIInput.make(form, "evalStatus", null, "new");
		}
		dateevolver.evolveDateInput(startDate, evaluationBean.startDate);

		UIMessage.make(form, "eval-due-date-header", "evalsettings.due.date.header");
		UIMessage.make(form, "eval-due-date-desc", "evalsettings.due.date.desc");
		UIInput dueDate = UIInput.make(form, "dueDate:", "#{evaluationBean.dueDate}");
		dateevolver.evolveDateInput(dueDate, evaluationBean.dueDate);

		// Show the "Stop date" text box only if allowed in the System settings
		if (((Boolean) settings.get(EvalSettings.EVAL_USE_STOP_DATE)).booleanValue()) {
			UIBranchContainer showStopDate = UIBranchContainer.make(form, "showStopDate:");
			UIMessage.make(showStopDate, "eval-stop-date-header", "evalsettings.stop.date.header");
			UIMessage.make(showStopDate, "eval-stop-date-desc", "evalsettings.stop.date.desc");
			UIInput stopDate = UIInput.make(showStopDate, "stopDate:", "#{evaluationBean.stopDate}");
			dateevolver.evolveDateInput(stopDate, evaluationBean.stopDate);
		}

		UIMessage.make(form, "eval-view-date-header", "evalsettings.view.date.header");
		UIMessage.make(form, "eval-view-date-desc", "evalsettings.view.date.desc");
		UIInput viewDate = UIInput.make(form, "viewDate:", "#{evaluationBean.viewDate}");
		dateevolver.evolveDateInput(viewDate, evaluationBean.viewDate);


		UIMessage.make(form, "eval-results-viewable-header", "evalsettings.results.viewable.header");
		UIMessage.make(form, "eval-results-viewable-private-start", "evalsettings.results.viewable.private.start");
		UIMessage.make(form, "eval-results-viewable-private-middle", "evalsettings.results.viewable.private.middle");
		UIOutput.make(form, "userInfo", externalLogic.getUserDisplayName(externalLogic.getCurrentUserId()));
		UIMessage.make(form, "private-warning-desc", "evalsettings.private.warning.desc");
		UIBoundBoolean.make(form, "resultsPrivate", "#{evaluationBean.eval.resultsPrivate}");

		/*
		 * (non-Javadoc) Variable is used to decide whether to show the view date
		 * textbox for student and instructor separately or not.
		 */
		boolean sameViewDateForAll = ((Boolean) settings.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES)).booleanValue();
		/*
		 * (non-Javadoc) If "EvalSettings.STUDENT_VIEW_RESULTS" is set as
		 * configurable i.e. NULL in the database OR is set as TRUE in database,
		 * then show the checkbox. Else do not show the checkbox and just bind the
		 * value to FALSE.
		 */
		Boolean tempValue = (Boolean) settings.get(EvalSettings.STUDENT_VIEW_RESULTS);
		if (tempValue == null || tempValue.booleanValue()) {

			UIBranchContainer showResultsToStudents = UIBranchContainer.make(form, "showResultsToStudents:");
			UIMessage.make(showResultsToStudents, "eval-results-viewable-students", "evalsettings.results.viewable.students");

			// If system setting was null implies normal checkbox. If it was TRUE then
			// a disabled selected checkbox
			if (tempValue == null) {
				UIBoundBoolean.make(showResultsToStudents, "studentViewResults", "#{evaluationBean.studentViewResults}", tempValue);
			} else {
				// Display only (disabled) and selected check box
				UIBoundBoolean stuViewCheckbox = UIBoundBoolean.make(showResultsToStudents, "studentViewResults", tempValue);
				setDisabledAttribute(stuViewCheckbox);

				// As we have disabled the check box => RSF will not bind the value =>
				// binding it explicitly.
				form.parameters.add(new UIELBinding("#{evaluationBean.studentViewResults}", tempValue));
			}

			// If same view date all then show a label else show a text box.
			if (sameViewDateForAll) {
				UIMessage.make(showResultsToStudents, "eval-results-stu-inst-date-label", "evalsettings.results.stu.inst.date.label");
			} else {
				UIInput studentsDate = UIInput.make(showResultsToStudents, "studentsDate:", "#{evaluationBean.studentsDate}");
				dateevolver.evolveDateInput(studentsDate, evaluationBean.studentsDate);
			}
		} else {
			form.parameters.add(new UIELBinding("#{evaluationBean.studentViewResults}", Boolean.FALSE));
		}
		/*
		 * (non-javadoc) If "EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS" is set as
		 * configurable i.e. NULL in the database OR is set as TRUE in database,
		 * then show the checkbox. Else do not show the checkbox and just bind the
		 * value to FALSE.
		 */
		tempValue = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
		if (tempValue == null || tempValue.booleanValue()) {

			UIBranchContainer showResultsToInst = UIBranchContainer.make(form, "showResultsToInst:");
			UIMessage.make(showResultsToInst, "eval-results-viewable-instructors", "evalsettings.results.viewable.instructors");

			// If system setting was null implies normal checkbox. If it was TRUE then
			// a disabled selected checkbox
			if (tempValue == null) {
				UIBoundBoolean.make(showResultsToInst, "instructorViewResults", "#{evaluationBean.instructorViewResults}", tempValue);
			} else {
				// Display only (disabled) and selected check box
				UIBoundBoolean instViewCheckbox = UIBoundBoolean.make(showResultsToInst, "instructorViewResults", tempValue);
				setDisabledAttribute(instViewCheckbox);

				// As we have disabled the check box => RSF will not bind the value =>
				// binding it explicitly.
				form.parameters.add(new UIELBinding("#{evaluationBean.instructorViewResults}", tempValue));
			}

			// If same view date all then show a label else show a text box.
			if (sameViewDateForAll) {
				UIMessage.make(showResultsToInst, "eval-results-stu-inst-date-label", "evalsettings.results.stu.inst.date.label");
			} else {
				UIInput instructorsDate = UIInput.make(showResultsToInst, "instructorsDate:", "#{evaluationBean.instructorsDate}");
				dateevolver.evolveDateInput(instructorsDate, evaluationBean.instructorsDate);
			}
		} else {
			form.parameters.add(new UIELBinding("#{evaluationBean.instructorViewResults}", Boolean.FALSE));
		}

		UIMessage.make(form, "eval-results-viewable-admin-note", "evalsettings.results.viewable.admin.note");

		/*
		 * (non-javadoc) If "EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED" is set
		 * as configurable i.e. NULL in the database OR is set as TRUE in database,
		 * then show the checkbox. Else do not show the checkbox and just bind the
		 * value to FALSE.
		 * 
		 * Note: The variable showStudentCompletionHeader is used to show
		 * "student-completion-settings-header" It is true only if either of the
		 * three "if's" below are evaluated to true.
		 */
		boolean showStudentCompletionHeader = false;
		tempValue = (Boolean) settings.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
		if (tempValue == null || tempValue.booleanValue()) {

			showStudentCompletionHeader = true;
			UIBranchContainer showBlankQuestionAllowedToStut = UIBranchContainer.make(form, "showBlankQuestionAllowedToStut:");
			UIMessage.make(showBlankQuestionAllowedToStut, "blank-responses-allowed-desc", "evalsettings.blank.responses.allowed.desc");
			UIMessage.make(showBlankQuestionAllowedToStut, "blank-responses-allowed-note", "evalsettings.blank.responses.allowed.note");

			// If system setting was null implies normal checkbox. If it was TRUE then
			// a disabled selected checkbox
			if (tempValue == null) {
				UIBoundBoolean.make(showBlankQuestionAllowedToStut, "blankResponsesAllowed", "#{evaluationBean.eval.blankResponsesAllowed}", tempValue);
			} else {
				// Display only (disabled) and selected check box
				UIBoundBoolean stuLeaveUnanswered = UIBoundBoolean.make(showBlankQuestionAllowedToStut, "blankResponsesAllowed", tempValue);
				setDisabledAttribute(stuLeaveUnanswered);

				// As we have disabled the check box => RSF will not bind the value =>
				// binding it explicitly.
				form.parameters.add(new UIELBinding("#{evaluationBean.eval.blankResponsesAllowed}", tempValue));
			}
		} else {
			form.parameters.add(new UIELBinding("#{evaluationBean.eval.blankResponsesAllowed}", Boolean.FALSE));
		}

		/*
		 * (non-javadoc) If "EvalSettings.STUDENT_MODIFY_RESPONSES" is set as
		 * configurable i.e. NULL in the database OR is set as TRUE in database,
		 * then show the checkbox. Else do not show the checkbox and just bind the
		 * value to FALSE.
		 */
		tempValue = (Boolean) settings.get(EvalSettings.STUDENT_MODIFY_RESPONSES);
		if (tempValue == null || tempValue.booleanValue()) {

			showStudentCompletionHeader = true;
			UIBranchContainer showModifyResponsesAllowedToStu = UIBranchContainer.make(form, "showModifyResponsesAllowedToStu:");
			UIMessage.make(showModifyResponsesAllowedToStu, "modify-responses-allowed-desc", "evalsettings.modify.responses.allowed.desc");
			UIMessage.make(showModifyResponsesAllowedToStu, "modify-responses-allowed-note", "evalsettings.modify.responses.allowed.note");

			// If system setting was null implies normal checkbox. If it was TRUE then
			// a disabled selected checkbox
			if (tempValue == null) {
				UIBoundBoolean.make(showModifyResponsesAllowedToStu, "modifyResponsesAllowed", "#{evaluationBean.eval.modifyResponsesAllowed}", tempValue);
			} else {
				// Display only (disabled) and selected check box
				UIBoundBoolean stuModifyResponses = UIBoundBoolean.make(showModifyResponsesAllowedToStu, "modifyResponsesAllowed", tempValue);
				setDisabledAttribute(stuModifyResponses);

				// As we have disabled the check box => RSF will not bind the value =>
				// binding it explicitly.
				form.parameters.add(new UIELBinding("#{evaluationBean.eval.modifyResponsesAllowed}", tempValue));
			}
		} else {
			form.parameters.add(new UIELBinding("#{evaluationBean.eval.modifyResponsesAllowed}", Boolean.FALSE));
		}
		// This options are commented for some time.
		if (false) {
			showStudentCompletionHeader = true;
			UIBranchContainer showUnregAllowedOption = UIBranchContainer.make(form, "showUnregAllowedOption:");
			UIMessage.make(showUnregAllowedOption, "unregistered-allowed-desc", "evalsettings.unregistered.allowed.desc");
			UIMessage.make(showUnregAllowedOption, "unregistered-allowed-note", "evalsettings.unregistered.allowed.note");
			UIBoundBoolean.make(showUnregAllowedOption, "unregisteredAllowed", "#{evaluationBean.eval.unregisteredAllowed}");
		}
		/*
		 * (non-javadoc) Continued from the note above, that is show
		 * "student-completion-settings-header" only if there are any student
		 * completion settings to be displayed on the page.
		 */
		if (showStudentCompletionHeader) {
			UIBranchContainer showStudentCompletionDiv = UIBranchContainer.make(form, "showStudentCompletionDiv:");
			UIMessage.make(showStudentCompletionDiv, "student-completion-settings-header", "evalsettings.student.completion.settings.header");
		}
		/*
		 * (non-javadoc) If the person is an admin (any kind), then only we need to
		 * show these instructor opt in/out settings.
		 */
		if (externalLogic.isUserAdmin(externalLogic.getCurrentUserId())) {
			UIBranchContainer showInstUseFromAbove = UIBranchContainer.make(form, "showInstUseFromAbove:");
			UIMessage.make(showInstUseFromAbove, "admin-settings-header", "evalsettings.admin.settings.header");
			UIMessage.make(showInstUseFromAbove, "admin-settings-instructions", "evalsettings.admin.settings.instructions");
			UIMessage.make(showInstUseFromAbove, "instructor-opt-desc", "evalsettings.instructor.opt.desc");
			/*
			 * (non-javadoc) If "EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE" is
			 * set as configurable i.e. NULL in the database then show the instructor
			 * opt select box. Else just show the value as label.
			 */
			String[] instructorOptLabels = { "evalsettings.instructors.label.opt.out", "evalsettings.instructors.label.opt.in",
					"evalsettings.instructors.label.required" };
			if (settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE) == null) {
				UIBranchContainer showInstUseFromAboveOptions = UIBranchContainer.make(showInstUseFromAbove, "showInstUseFromAboveOptions:");

				UISelect.make(showInstUseFromAboveOptions, "instructorOpt", EvaluationConstant.INSTRUCTOR_OPT_VALUES, instructorOptLabels,
						"#{evaluationBean.eval.instructorOpt}").setMessageKeys();
			} else {
				UIBranchContainer showInstUseFromAboveLabel = UIBranchContainer.make(showInstUseFromAbove, "showInstUseFromAboveLabel:");
				String instUseFromAboveValue = (String) settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE);
				int index = ArrayUtil.indexOf(EvaluationConstant.INSTRUCTOR_OPT_VALUES, instUseFromAboveValue);
				String instUseFromAboveLabel = instructorOptLabels[index];
				// Displaying the label corresponding to INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE value set as system property
				UIMessage.make(showInstUseFromAboveLabel, "instUseFromAboveLabel", instUseFromAboveLabel);
				// Doing the binding of this INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE value so that it can be saved in the database
				form.parameters.add(new UIELBinding("#{evaluationBean.eval.instructorOpt}", instUseFromAboveValue));
			}
		}

		UIMessage.make(form, "eval-reminder-settings-header", "evalsettings.reminder.settings.header");
		UIInternalLink.make(form, "emailAvailable_link", UIMessage.make("evalsettings.available.mail.link"), new EmailViewParameters(
				PreviewEmailProducer.VIEW_ID, null, EvalConstants.EMAIL_TEMPLATE_AVAILABLE));
		UIMessage.make(form, "eval-available-mail-desc", "evalsettings.available.mail.desc");
		UIMessage.make(form, "reminder-noresponders-header", "evalsettings.reminder.noresponders.header");

		String[] reminderEmailDaysLabels = { "evalsettings.reminder.days.0", "evalsettings.reminder.days.1", "evalsettings.reminder.days.2",
				"evalsettings.reminder.days.3", "evalsettings.reminder.days.4", "evalsettings.reminder.days.5", "evalsettings.reminder.days.6",
				"evalsettings.reminder.days.7" };
		// Reminder email select box
		UISelect.make(form, "reminderDays", 
				EvaluationConstant.REMINDER_EMAIL_DAYS_VALUES, reminderEmailDaysLabels, 
				"#{evaluationBean.eval.reminderDays}").setMessageKeys();

		UIInternalLink.make(form, "emailReminder_link", UIMessage.make("evalsettings.reminder.mail.link"), new EmailViewParameters(
				PreviewEmailProducer.VIEW_ID, null, EvalConstants.EMAIL_TEMPLATE_REMINDER));
		UIMessage.make(form, "eval-reminder-mail-desc", "evalsettings.reminder.mail.desc");
		UIMessage.make(form, "eval-from-email-header", "evalsettings.from.email.header");
		UIInput.make(form, "reminderFromEmail", "#{evaluationBean.eval.reminderFromEmail}");

		// if this evaluation is already saved, show "Save Settings" button else this is the "Continue to Assign to Courses" button
		if (evaluationBean.eval.getId() == null) {
			UICommand.make(form, "continueAssigning", UIMessage.make("evalsettings.continue.assigning.link"), "#{evaluationBean.continueAssigningAction}");

			UIBranchContainer firstTime = UIBranchContainer.make(form, "firstTime:");
			UIMessage.make(firstTime, "cancel-button", "general.cancel.button");

		} else {
			UICommand.make(form, "continueAssigning", UIMessage.make("evalsettings.save.settings.link"), "#{evaluationBean.saveSettingsAction}");

			UIBranchContainer subsequentTimes = UIBranchContainer.make(form, "subsequentTimes:");
			UICommand.make(subsequentTimes, "cancel-button", UIMessage.make("general.cancel.button"), "#{evaluationBean.cancelSettingsAction}");
		}
	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
    // Physical templateId is filled in by global Interceptor
		i.add(new NavigationCase(EvaluationStartProducer.VIEW_ID, new TemplateViewParameters(EvaluationStartProducer.VIEW_ID, null)));
		i.add(new NavigationCase(ControlEvaluationsProducer.VIEW_ID, new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID)));
		i.add(new NavigationCase(EvaluationAssignProducer.VIEW_ID, new SimpleViewParameters(EvaluationAssignProducer.VIEW_ID)));

		return i;
	}


	/**
	 * This method is used to make the checkbox appear disabled whereever needed
	 * @param checkbox
	 */
	private void setDisabledAttribute(UIBoundBoolean checkbox) {
		checkbox.decorators = new DecoratorList(new UIDisabledDecorator());
	}

}
