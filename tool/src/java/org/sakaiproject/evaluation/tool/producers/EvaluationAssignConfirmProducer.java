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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationBean;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Confirmation of assign an evaluation to courses
 * 
 * @author:Kapil Ahuja (kahuja@vt.edu)
 * @author: Rui Feng (fengr@vt.edu)
 */
public class EvaluationAssignConfirmProducer implements ViewComponentProducer, NavigationCaseReporter {

	public static final String VIEW_ID = "evaluation_assign_confirm";
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private EvaluationBean evaluationBean;
	public void setEvaluationBean(EvaluationBean evaluationBean) {
		this.evaluationBean = evaluationBean;
	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIMessage.make(tofill, "confirm-assignment-title", "evaluationassignconfirm.page.title"); //$NON-NLS-2$

		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"), //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));

		UIMessage.make(tofill, "create-evaluation-title", "createeval.page.title"); //$NON-NLS-2$
		UIMessage.make(tofill, "assign-evaluation-title", "assigneval.page.title"); //$NON-NLS-2$
		UIMessage.make(tofill, "confirm-assignment-title", "evaluationassignconfirm.page.title"); //$NON-NLS-2$

		UIOutput.make(tofill, "evaluationTitle", null, "#{evaluationBean.eval.title}"); //$NON-NLS-2$
		//Get selected id's and all id + title map.
		String[] selectedIds = evaluationBean.selectedSakaiSiteIds;
		int[] enrollment = evaluationBean.enrollment;

		List evaluatedContexts = external.getEvalGroupsForUser(external.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
		Map allIdTitleMap = new HashMap(); //Map allIdTitleMap = evaluationBean.getSites();
		for (int i = 0; i < evaluatedContexts.size(); i++) {
			EvalGroup c = (EvalGroup) evaluatedContexts.get(i);
			//ids[i] = c.context;
			//labels[i] = c.title;
			allIdTitleMap.put(c.evalGroupId, c.title);
		}
		UIMessage.make(tofill, "eval-assign-desc-prename", "evaluationassignconfirm.eval.assign.desc.prename"); //$NON-NLS-2$
		UIMessage.make(tofill, "eval-assign-desc-postname", "evaluationassignconfirm.eval.assign.desc.postname"); //$NON-NLS-2$
		UIMessage.make(tofill, "courses-selected-header", "evaluationassignconfirm.courses.selected.header"); //$NON-NLS-2$
		UIMessage.make(tofill, "title-header", "evaluationassignconfirm.title.header"); //$NON-NLS-2$
		UIMessage.make(tofill, "enrollment-header", "evaluationassignconfirm.enrollment.header"); //$NON-NLS-2$

		for (int i = 0; i < selectedIds.length; ++i) {
			UIBranchContainer siteRow = UIBranchContainer.make(tofill, "sites:");
			UIOutput.make(siteRow, "siteTitle", (String) allIdTitleMap.get(selectedIds[i]));

			UIOutput.make(siteRow, "enrollment", "" + enrollment[i] + ""); //$NON-NLS-2$ //$NON-NLS-3$
		}

		/*
		 * show submit buttons for first time evaluation creation && Queued Evaluation case
		 * 
		 * */
		if (evaluationBean.eval.getId() == null) {//first time evaluation creation
			showButtonsForm(tofill);

		} else {
			//check if evaluation	is queued;Closed,started evaluation can not be changed
			Date today = new Date();
			Date startDate = evaluationBean.eval.getStartDate();
			if (today.before(startDate)) {
				showButtonsForm(tofill);
			}
		}

	}

	private void showButtonsForm(UIContainer tofill) {
		UIBranchContainer showButtons = UIBranchContainer.make(tofill, "showButtons:");
		UIForm evalAssignForm = UIForm.make(showButtons, "evalAssignForm");
		UICommand.make(evalAssignForm, "doneAssignment", UIMessage.make("evaluationassignconfirm.done.button"), "#{evaluationBean.doneAssignmentAction}"); //$NON-NLS-2$ //$NON-NLS-3$
		UICommand.make(evalAssignForm, "changeAssignedCourse", UIMessage.make("evaluationassignconfirm.changes.assigned.courses.button"), //$NON-NLS-2$
				"#{evaluationBean.changeAssignedCourseAction}");
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
		i.add(new NavigationCase(ControlEvaluationsProducer.VIEW_ID, new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID)));
		i.add(new NavigationCase(EvaluationAssignProducer.VIEW_ID, new SimpleViewParameters(EvaluationAssignProducer.VIEW_ID)));
		return i;
	}
}
