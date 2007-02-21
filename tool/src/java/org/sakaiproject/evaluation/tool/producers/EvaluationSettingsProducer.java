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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.params.EmailViewParameters;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;

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
public class EvaluationSettingsProducer implements ViewComponentProducer,
		NavigationCaseReporter {
	public static final String VIEW_ID = "evaluation_settings"; //$NON-NLS-1$

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer,
	 *      uk.org.ponder.rsf.viewstate.ViewParameters,
	 *      uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {

		// Displaying the top link, page title, page description, and creating the
		// HTML form
		UIOutput.make(tofill, "eval-settings-title", "evalsettings.page.title"); //$NON-NLS-1$ //$NON-NLS-2$
		UIInternalLink.make(tofill, "summary-toplink", "summary.page.title", //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));

		UIForm form = UIForm.make(tofill, "evalSettingsForm"); //$NON-NLS-1$
		UIMessage.make(form, "settings-desc-header",
				"evalsettings.settings.desc.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput
				.make(form, "evaluationTitle", null, "#{evaluationBean.eval.title}"); //$NON-NLS-1$ //$NON-NLS-2$

		Date today = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(today);

		UIMessage.make(form, "eval-dates-header", "evalsettings.dates.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(form,
				"eval-start-date-header", "evalsettings.start.date.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage
				.make(form, "eval-start-date-desc", "evalsettings.start.date.desc"); //$NON-NLS-1$ //$NON-NLS-2$

		UIInput startDate = UIInput.make(form,
				"startDate:", "#{evaluationBean.startDate}", null); //$NON-NLS-1$ //$NON-NLS-2$	
		if (evaluationBean.eval.getId() != null) {
			// queued evalution
			if (today.before(evaluationBean.eval.getStartDate())) {
				UIInput.make(form, "evalStatus", null, "queued"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			// started evaluation
			else {
				startDate.decorators = new DecoratorList(new UIDisabledDecorator());
				UIInput.make(form, "evalStatus", null, "active"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			UIInput.make(form, "evalStatus", null, "new"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		dateevolver.evolveDateInput(startDate, calendar.getTime());

	  calendar.add(Calendar.DATE, 1);
		// Show the "Stop date" text box only if it is set as yes in the System
		// settings
		if (((Boolean) settings.get(EvalSettings.EVAL_USE_STOP_DATE))
				.booleanValue()) {
			UIBranchContainer showStopDate = UIBranchContainer.make(form,
					"showStopDate:"); //$NON-NLS-1$
			UIMessage.make(showStopDate,
					"eval-stop-date-header", "evalsettings.stop.date.header"); //$NON-NLS-1$ //$NON-NLS-2$
			UIInput stopDate = UIInput.make(showStopDate, "stopDate:", "#{evaluationBean.stopDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
			dateevolver.evolveDateInput(stopDate, calendar.getTime());
		}
	
	  calendar.add(Calendar.DATE, 1);
		UIMessage
				.make(form, "eval-due-date-header", "evalsettings.due.date.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput dueDate = UIInput.make(form, "dueDate:", "#{evaluationBean.dueDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
		dateevolver.evolveDateInput(dueDate, calendar.getTime());
		
		calendar.add(Calendar.DATE, 1);
		UIMessage.make(form,
				"eval-view-date-header", "evalsettings.view.date.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(form, "eval-view-date-desc", "evalsettings.view.date.desc"); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput viewDate = UIInput.make(form, "viewDate:", "#{evaluationBean.viewDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
		dateevolver.evolveDateInput(viewDate, calendar.getTime());
		

		UIMessage.make(form,
				"eval-results-viewable-header", "evalsettings.results.viewable.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(form, "eval-results-viewable-private-start",
				"evalsettings.results.viewable.private.start"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(form, "eval-results-viewable-private-middle",
				"evalsettings.results.viewable.private.middle"); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "userInfo", externalLogic
				.getUserDisplayName(externalLogic.getCurrentUserId())); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(form,
				"private-warning-desc", "evalsettings.private.warning.desc"); //$NON-NLS-1$ //$NON-NLS-2$
		UIBoundBoolean.make(form,
				"resultsPrivate", "#{evaluationBean.eval.resultsPrivate}", null); //$NON-NLS-1$ //$NON-NLS-2$

		/*
		 * (non-Javadoc) Variable is used to decide whether to show the view date
		 * textbox for student and instructor separately or not.
		 */
		boolean sameViewDateForAll = ((Boolean) settings
				.get(EvalSettings.EVAL_USE_SAME_VIEW_DATES)).booleanValue();
		/*
		 * (non-Javadoc) If "EvalSettings.STUDENT_VIEW_RESULTS" is set as
		 * configurable i.e. NULL in the database OR is set as TRUE in database,
		 * then show the checkbox. Else do not show the checkbox and just bind the
		 * value to FALSE.
		 */
		Boolean tempValue = (Boolean) settings
				.get(EvalSettings.STUDENT_VIEW_RESULTS);
		if (tempValue == null || tempValue.booleanValue()) {

			UIBranchContainer showResultsToStudents = UIBranchContainer.make(form,
					"showResultsToStudents:"); //$NON-NLS-1$
			UIMessage.make(showResultsToStudents, "eval-results-viewable-students",
					"evalsettings.results.viewable.students"); //$NON-NLS-1$ //$NON-NLS-2$

			// If system setting was null implies normal checkbox. If it was TRUE then
			// a disabled selected checkbox
			if (tempValue == null) {
				UIBoundBoolean.make(showResultsToStudents, "studentViewResults",
						"#{evaluationBean.studentViewResults}", tempValue); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				// Display only (disabled) and selected check box
				UIBoundBoolean stuViewCheckbox = UIBoundBoolean.make(
						showResultsToStudents, "studentViewResults", tempValue); //$NON-NLS-1$
				setDisabledAttribute(stuViewCheckbox);

				// As we have disabled the check box => RSF will not bind the value =>
				// binding it explicitly.
				form.parameters.add(new UIELBinding(
						"#{evaluationBean.studentViewResults}", tempValue)); //$NON-NLS-1$
			}

			// If same view date all then show a label else show a text box.
			if (sameViewDateForAll) {
				UIMessage.make(showResultsToStudents,
						"eval-results-stu-inst-date-label",
						"evalsettings.results.stu.inst.date.label"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				UIInput studentsDate = UIInput.make(showResultsToStudents,
						"studentsDate:", "#{evaluationBean.studentsDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
				dateevolver.evolveDateInput(studentsDate, calendar.getTime());
			}
		} else {
			form.parameters.add(new UIELBinding(
					"#{evaluationBean.studentViewResults}", Boolean.FALSE)); //$NON-NLS-1$
		}
		/*
		 * (non-javadoc) If "EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS" is set as
		 * configurable i.e. NULL in the database OR is set as TRUE in database,
		 * then show the checkbox. Else do not show the checkbox and just bind the
		 * value to FALSE.
		 */
		tempValue = (Boolean) settings
				.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
		if (tempValue == null || tempValue.booleanValue()) {

			UIBranchContainer showResultsToInst = UIBranchContainer.make(form,
					"showResultsToInst:"); //$NON-NLS-1$
			UIMessage.make(showResultsToInst, "eval-results-viewable-instructors",
					"evalsettings.results.viewable.instructors"); //$NON-NLS-1$ //$NON-NLS-2$

			// If system setting was null implies normal checkbox. If it was TRUE then
			// a disabled selected checkbox
			if (tempValue == null) {
				UIBoundBoolean.make(showResultsToInst, "instructorViewResults",
						"#{evaluationBean.instructorViewResults}", tempValue); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				// Display only (disabled) and selected check box
				UIBoundBoolean instViewCheckbox = UIBoundBoolean.make(
						showResultsToInst, "instructorViewResults", tempValue); //$NON-NLS-1$ 
				setDisabledAttribute(instViewCheckbox);

				// As we have disabled the check box => RSF will not bind the value =>
				// binding it explicitly.
				form.parameters.add(new UIELBinding(
						"#{evaluationBean.instructorViewResults}", tempValue)); //$NON-NLS-1$
			}

			// If same view date all then show a label else show a text box.
			if (sameViewDateForAll) {
				UIBranchContainer showResultsToInstLabel = UIBranchContainer.make(
						showResultsToInst, "showResultsToInstLabel:"); //$NON-NLS-1$
				UIMessage.make(showResultsToInstLabel,
						"eval-results-stu-inst-date-label",
						"evalsettings.results.stu.inst.date.label"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				UIBranchContainer showResultsToInstDate = UIBranchContainer.make(
						showResultsToInst, "showResultsToInstDate:"); //$NON-NLS-1$
				UIInput instructorsDate = UIInput.make(showResultsToInstDate,
						"instructorsDate:", "#{evaluationBean.instructorsDate}", null); //$NON-NLS-1$ //$NON-NLS-2$
				dateevolver.evolveDateInput(instructorsDate, calendar.getTime());
			}
		} else {
			form.parameters.add(new UIELBinding(
					"#{evaluationBean.instructorViewResults}", Boolean.FALSE)); //$NON-NLS-1$
		}

		UIMessage.make(form, "eval-results-viewable-admin-note",
				"evalsettings.results.viewable.admin.note"); //$NON-NLS-1$ //$NON-NLS-2$

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
		tempValue = (Boolean) settings
				.get(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED);
		if (tempValue == null || tempValue.booleanValue()) {

			showStudentCompletionHeader = true;
			UIBranchContainer showBlankQuestionAllowedToStut = UIBranchContainer
					.make(form, "showBlankQuestionAllowedToStut:"); //$NON-NLS-1$
			UIMessage.make(showBlankQuestionAllowedToStut,
					"blank-responses-allowed-desc",
					"evalsettings.blank.responses.allowed.desc"); //$NON-NLS-1$ //$NON-NLS-2$
			UIMessage.make(showBlankQuestionAllowedToStut,
					"blank-responses-allowed-note",
					"evalsettings.blank.responses.allowed.note"); //$NON-NLS-1$ //$NON-NLS-2$

			// If system setting was null implies normal checkbox. If it was TRUE then
			// a disabled selected checkbox
			if (tempValue == null) {
				UIBoundBoolean.make(showBlankQuestionAllowedToStut,
						"blankResponsesAllowed",
						"#{evaluationBean.eval.blankResponsesAllowed}", tempValue); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				// Display only (disabled) and selected check box
				UIBoundBoolean stuLeaveUnanswered = UIBoundBoolean.make(
						showBlankQuestionAllowedToStut, "blankResponsesAllowed", tempValue); //$NON-NLS-1$
				setDisabledAttribute(stuLeaveUnanswered);

				// As we have disabled the check box => RSF will not bind the value =>
				// binding it explicitly.
				form.parameters.add(new UIELBinding(
						"#{evaluationBean.eval.blankResponsesAllowed}", tempValue)); //$NON-NLS-1$
			}
		} else {
			form.parameters.add(new UIELBinding(
					"#{evaluationBean.eval.blankResponsesAllowed}", Boolean.FALSE)); //$NON-NLS-1$
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
			UIBranchContainer showModifyResponsesAllowedToStu = UIBranchContainer
					.make(form, "showModifyResponsesAllowedToStu:"); //$NON-NLS-1$
			UIMessage.make(showModifyResponsesAllowedToStu,
					"modify-responses-allowed-desc",
					"evalsettings.modify.responses.allowed.desc"); //$NON-NLS-1$ //$NON-NLS-2$
			UIMessage.make(showModifyResponsesAllowedToStu,
					"modify-responses-allowed-note",
					"evalsettings.modify.responses.allowed.note"); //$NON-NLS-1$ //$NON-NLS-2$

			// If system setting was null implies normal checkbox. If it was TRUE then
			// a disabled selected checkbox
			if (tempValue == null) {
				UIBoundBoolean.make(showModifyResponsesAllowedToStu,
						"modifyResponsesAllowed",
						"#{evaluationBean.eval.modifyResponsesAllowed}", tempValue); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				// Display only (disabled) and selected check box
				UIBoundBoolean stuModifyResponses = UIBoundBoolean.make(
						showModifyResponsesAllowedToStu,
						"modifyResponsesAllowed", tempValue); //$NON-NLS-1$
				setDisabledAttribute(stuModifyResponses);

				// As we have disabled the check box => RSF will not bind the value =>
				// binding it explicitly.
				form.parameters.add(new UIELBinding(
						"#{evaluationBean.eval.modifyResponsesAllowed}", tempValue)); //$NON-NLS-1$
			}
		} else {
			form.parameters.add(new UIELBinding(
					"#{evaluationBean.eval.modifyResponsesAllowed}", Boolean.FALSE)); //$NON-NLS-1$
		}
		// This options are commented for some time.
		if (false) {
			showStudentCompletionHeader = true;
			UIBranchContainer showUnregAllowedOption = UIBranchContainer.make(form,
					"showUnregAllowedOption:"); //$NON-NLS-1$
			UIMessage.make(showUnregAllowedOption, "unregistered-allowed-desc",
					"evalsettings.unregistered.allowed.desc"); //$NON-NLS-1$ //$NON-NLS-2$
			UIMessage.make(showUnregAllowedOption, "unregistered-allowed-note",
					"evalsettings.unregistered.allowed.note"); //$NON-NLS-1$ //$NON-NLS-2$
			UIBoundBoolean.make(showUnregAllowedOption, "unregisteredAllowed",
					"#{evaluationBean.eval.unregisteredAllowed}", null); //$NON-NLS-1$ //$NON-NLS-2$
		}
		/*
		 * (non-javadoc) Continued from the note above, that is show
		 * "student-completion-settings-header" only if there are any student
		 * completion settings to be displayed on the page.
		 */
		if (showStudentCompletionHeader) {
			UIBranchContainer showStudentCompletionDiv = UIBranchContainer.make(form,
					"showStudentCompletionDiv:"); //$NON-NLS-1$
			UIMessage.make(showStudentCompletionDiv,
					"student-completion-settings-header",
					"evalsettings.student.completion.settings.header"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		/*
		 * (non-javadoc) If the person is an admin (any kind), then only we need to
		 * show these instructor opt in/out settings.
		 */
		if (externalLogic.isUserAdmin(externalLogic.getCurrentUserId())) {
			UIBranchContainer showInstUseFromAbove = UIBranchContainer.make(form,
					"showInstUseFromAbove:"); //$NON-NLS-1$
			UIMessage.make(showInstUseFromAbove,
					"admin-settings-header", "evalsettings.admin.settings.header"); //$NON-NLS-1$ //$NON-NLS-2$
			UIMessage.make(showInstUseFromAbove, "admin-settings-instructions",
					"evalsettings.admin.settings.instructions"); //$NON-NLS-1$ //$NON-NLS-2$
			UIMessage.make(showInstUseFromAbove, "instructor-opt-desc",
					"evalsettings.instructor.opt.desc"); //$NON-NLS-1$ //$NON-NLS-2$
			/*
			 * (non-javadoc) If "EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE" is
			 * set as configurable i.e. NULL in the database then show the instructor
			 * opt select box. Else just show the value as label.
			 */
			String[] instructorOptLabels = { "evalsettings.instructors.label.opt.in",
					"evalsettings.instructors.label.opt.out",
					"evalsettings.instructors.label.required" };
			if (settings.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE) == null) {
				UIBranchContainer showInstUseFromAboveOptions = UIBranchContainer.make(
						showInstUseFromAbove, "showInstUseFromAboveOptions:"); //$NON-NLS-1$

				UISelect.make(showInstUseFromAboveOptions, "instructorOpt",
						EvaluationConstant.INSTRUCTOR_OPT_VALUES, instructorOptLabels,
						"#{evaluationBean.eval.instructorOpt}", null).setMessageKeys(); //$NON-NLS-1$
			} else {
				UIBranchContainer showInstUseFromAboveLabel = UIBranchContainer.make(
						showInstUseFromAbove, "showInstUseFromAboveLabel:"); //$NON-NLS-1$
				String instUseFromAboveValue = (String) settings
						.get(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE);
				int index = ArrayUtil.indexOf(EvaluationConstant.INSTRUCTOR_OPT_VALUES,
						instUseFromAboveValue);
				String instUseFromAboveLabel = instructorOptLabels[index];
				/*
				 * (non-javadoc) Displaying the label corresponding to
				 * INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE value set as system property.
				 */
				UIMessage.make(showInstUseFromAboveLabel,
						"instUseFromAboveLabel", instUseFromAboveLabel); //$NON-NLS-1$ 
				/*
				 * (non-javadoc) Doing the binding of this
				 * INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE value so that it can be saved in
				 * the database
				 */
				form.parameters.add(new UIELBinding(
						"#{evaluationBean.eval.instructorOpt}", instUseFromAboveValue)); //$NON-NLS-1$  
			}
		}

		UIMessage.make(form, "eval-reminder-settings-header",
				"evalsettings.reminder.settings.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIInternalLink.make(form, "emailAvailable_link", UIMessage
				.make("evalsettings.available.mail.link"), new EmailViewParameters(
				PreviewEmailProducer.VIEW_ID, null,
				EvalConstants.EMAIL_TEMPLATE_AVAILABLE)); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(form, "eval-available-mail-desc",
				"evalsettings.available.mail.desc"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(form, "reminder-noresponders-header",
				"evalsettings.reminder.noresponders.header"); //$NON-NLS-1$ //$NON-NLS-2$

		String[] reminderEmailDaysLabels = { "evalsettings.reminder.days.0",
				"evalsettings.reminder.days.1", "evalsettings.reminder.days.2",
				"evalsettings.reminder.days.3", "evalsettings.reminder.days.4",
				"evalsettings.reminder.days.5", "evalsettings.reminder.days.6",
				"evalsettings.reminder.days.7" };
		// Reminder email select box
		UISelect.make(form, "reminderDays",
				EvaluationConstant.REMINDER_EMAIL_DAYS_VALUES, reminderEmailDaysLabels,
				"#{evaluationBean.eval.reminderDays}", null).setMessageKeys(); //$NON-NLS-1$

		UIInternalLink.make(form, "emailReminder_link", UIMessage
				.make("evalsettings.reminder.mail.link"), new EmailViewParameters(
				PreviewEmailProducer.VIEW_ID, null,
				EvalConstants.EMAIL_TEMPLATE_REMINDER)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		UIMessage.make(form,
				"eval-reminder-mail-desc", "evalsettings.reminder.mail.desc"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(form,
				"eval-from-email-header", "evalsettings.from.email.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput.make(form,
				"reminderFromEmail", "#{evaluationBean.eval.reminderFromEmail}", null); //$NON-NLS-1$ //$NON-NLS-2$

		/*
		 * (non-javadoc) if this evaluation is already saved, show "Save Settings"
		 * button else this is the "Continue to Assign to Courses" button
		 */
		if (evaluationBean.eval.getId() == null) {
			UICommand.make(form, "continueAssigning", UIMessage
					.make("evalsettings.continue.assigning.link"),
					"#{evaluationBean.continueAssigningAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			UICommand.make(form, "continueAssigning", UIMessage
					.make("evalsettings.save.settings.link"),
					"#{evaluationBean.saveSettingsAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$	
		}
		UIMessage.make(form, "cancel-button", "general.cancel.button");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();

		i.add(new NavigationCase(EvaluationStartProducer.VIEW_ID,
				new EvalViewParameters(EvaluationStartProducer.VIEW_ID, null)));
		i.add(new NavigationCase(ControlPanelProducer.VIEW_ID,
				new SimpleViewParameters(ControlPanelProducer.VIEW_ID)));
		i.add(new NavigationCase(EvaluationAssignProducer.VIEW_ID,
				new SimpleViewParameters(EvaluationAssignProducer.VIEW_ID)));

		return i;
	}

	/*
	 * (non-Javadoc) This method is used to make the checkbox appear disabled
	 * where ever needed.
	 */
	private void setDisabledAttribute(UIBoundBoolean checkbox) {
		checkbox.decorators = new DecoratorList(new UIDisabledDecorator());
	}

}
