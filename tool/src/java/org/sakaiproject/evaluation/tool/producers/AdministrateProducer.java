/******************************************************************************
 * AdministrateProducer.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvaluationLogic;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles administration of the evaluation system
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class AdministrateProducer implements ViewComponentProducer, NavigationCaseReporter {

	public static final String VIEW_ID = "administrate"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}

	private EvaluationLogic logic;
	public void setLogic(EvaluationLogic logic) {
		this.logic = logic;
	}

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}	
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		String currentUserId = logic.getCurrentUserId();
		boolean userAdmin = external.isUserAdmin(currentUserId);
		
		/*
		 * top links here
		 */
		UIOutput.make(tofill, "administrate-title", messageLocator.getMessage("administrate.page.title") ); //$NON-NLS-1$ //$NON-NLS-2$
		if (userAdmin) {
			UIInternalLink.make(tofill, "control-panel-toplink", messageLocator.getMessage("controlpanel.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
					new SimpleViewParameters(ControlPanelProducer.VIEW_ID));
		}

		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID)); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "system-settings-instructions", messageLocator.getMessage("administrate.system.settings.instructions")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "instructors-eval-create-note", messageLocator.getMessage("administrate.instructors.eval.create.note")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "instructors-view-results-note", messageLocator.getMessage("administrate.instructors.view.results.note")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "instructors-email-students-note", messageLocator.getMessage("administrate.instructors.email.students.note")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "instructors-hierarchy-email-note", messageLocator.getMessage("administrate.instructors.hierarchy.email.note")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "instructors-num-questions-note", messageLocator.getMessage("administrate.instructors.num.questions.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(tofill, "student-settings-header", messageLocator.getMessage("administrate.student.settings.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "students-unanswered-note", messageLocator.getMessage("administrate.students.unanswered.note")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "students-modify-responses-note", messageLocator.getMessage("administrate.students.modify.responses.note")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "students-view-results-note", messageLocator.getMessage("administrate.students.view.results.note")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "administrator-settings-header", messageLocator.getMessage("administrate.admin.settings.header"));		 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "admin-hierarchy-num-questions-note", messageLocator.getMessage("administrate.admin.hierarchy.num.questions.note")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "admin-view-instructor-questions-note", messageLocator.getMessage("administrate.admin.view.instructor.questions.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "admin-super-modify-question-note", messageLocator.getMessage("administrate.admin.super.modify.question.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(tofill, "general-settings-header", messageLocator.getMessage("administrate.general.settings.header"));		 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "general-helpdesk-email-note", messageLocator.getMessage("administrate.general.helpdesk.email.note")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "general-responses-before-view-note", messageLocator.getMessage("administrate.general.responses.before.view.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "general-na-allowed-note", messageLocator.getMessage("administrate.general.na.allowed.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "general-max-questions-block-note", messageLocator.getMessage("administrate.general.max.questions.block.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		
		// TODO - permission-settings select, functionality and i18n
		
		UIOutput.make(tofill, "general-template-sharing-note", messageLocator.getMessage("administrate.general.template.sharing.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "general-default-question-category", messageLocator.getMessage("administrate.general.default.question.category.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "general-use-stop-date-note", messageLocator.getMessage("administrate.general.use.stop.date.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "general-expert-templates-note", messageLocator.getMessage("administrate.general.expert.templates.note"));	 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "general-expert-questions-note", messageLocator.getMessage("administrate.general.expert.questions.note"));		 //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "general-same-view-date-note", messageLocator.getMessage("administrate.general.same.view.date.note"));	 //$NON-NLS-1$ //$NON-NLS-2$

		// TODO - submit button i18n
		
		
		// TODO - add functionality
	}
	
	public List reportNavigationCases() {
		List i = new ArrayList();
		//TODO
		return i;
	}
}
