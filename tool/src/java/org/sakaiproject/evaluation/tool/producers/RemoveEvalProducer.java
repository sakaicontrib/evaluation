/******************************************************************************
 * RemoveEvalProducer.java - created by fengr@vt.edu on Nov 16, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This page is to remove a evaluation from DAO
 * 
 * @author: Rui Feng (fengr@vt.edu)
 */

public class RemoveEvalProducer implements ViewComponentProducer,ViewParamsReporter, NavigationCaseReporter {

	public static final String VIEW_ID = "remove_evaluation"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}
	
	private EvalEvaluationsLogic evalsLogic;
	public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
		this.evalsLogic = evalsLogic;
	}
		
	private Locale locale;
	public void setLocale(Locale locale){
		this.locale=locale;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {	
		/*
		 * TODO: 
		 * 
		 * 1) check if current user is the owner or higher level admin,
		 * 2)double check if the evaluation is queued for future??? if this page is 
				accessed from page other than ControlPanel
		 */
		UIMessage.make(tofill, "remove-eval-title", "removeeval.page.title");	 //$NON-NLS-1$ //$NON-NLS-2$

		
		EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;
		
		if(evalViewParams.templateId !=null){
			EvalEvaluation eval = evalsLogic.getEvaluationById(evalViewParams.templateId);
			//EvalEvaluation eval = logic.getEvaluationById(evalViewParams.templateId);
			if(eval != null){
				UIForm form = UIForm.make(tofill, "removeEvalForm"); //$NON-NLS-1$
				UIMessage.make(form, "remove-eval-confirm-pre-name", "removeeval.confirm.pre.name"); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(form, "evalTitle", eval.getTitle()); //$NON-NLS-1$
				UIMessage.make(form, "remove-eval-confirm-post-name", "removeeval.confirm.post.name");	 //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(form, "remove-eval-note", "removeeval.note"); //$NON-NLS-1$ //$NON-NLS-2$
				
				UIMessage.make(form, "eval-title-header","removeeval.title.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(form, "assigned-header", "removeeval.assigned.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(form, "start-date-header", "removeeval.start.date.header"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(form, "due-date-header", "removeeval.due.date.header"); //$NON-NLS-1$ //$NON-NLS-2$
				
				int count = evalsLogic.countEvaluationGroups(eval.getId());
				//int count = logic.countEvaluationContexts(eval.getId());
				if (count > 1)
					UIOutput.make(form, "evalAssigned", count + " courses"); //$NON-NLS-1$ //$NON-NLS-2$
				else{
					Long[] evalIds = {eval.getId()};
					Map evalContexts = evalsLogic.getEvaluationGroups(evalIds, true);
					List contexts = (List) evalContexts.get(eval.getId());
					EvalGroup ctxt = (EvalGroup) contexts.get(0);
					String title = ctxt.title;
					UIOutput.make(form, "evalAssigned",title);
				}
				
				DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
				
				UIOutput.make(form, "evalStartDate", df.format(eval.getStartDate())); //$NON-NLS-1$
				UIOutput.make(form, "evalDueDate", df.format(eval.getDueDate())); //$NON-NLS-1$
				
				UICommand.make(form, "cancelRemoveEvalAction", UIMessage.make("general.cancel.button"),  //$NON-NLS-1$ //$NON-NLS-2$
						"#{evaluationBean.cancelRemoveEvalAction}"); //$NON-NLS-1$
				
				UICommand removeCmd = UICommand.make(form, "removeEvalAction", UIMessage.make("removeeval.remove.button"),  //$NON-NLS-1$ //$NON-NLS-2$
									"#{evaluationBean.removeEvalAction}");  //$NON-NLS-1$
				removeCmd.parameters.add(new UIELBinding("#{evaluationBean.evalId}",eval.getId().toString()));				 //$NON-NLS-1$
			
			}
		}
	}

	public List reportNavigationCases() {
		List i = new ArrayList();
		i.add(new NavigationCase(ControlPanelProducer.VIEW_ID, new SimpleViewParameters(ControlPanelProducer.VIEW_ID)));

		return i;
	}

	public ViewParameters getViewParameters() {
		return new EvalViewParameters();
	}
	
	
}
