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
import org.sakaiproject.evaluation.tool.params.TemplateViewParameters;

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
 * This page confirms that the user wants to remove an evaluation
 * 
 * @author: Rui Feng (fengr@vt.edu)
 */

public class RemoveEvalProducer implements ViewComponentProducer,ViewParamsReporter, NavigationCaseReporter {

	public static final String VIEW_ID = "remove_evaluation";
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
		UIMessage.make(tofill, "remove-eval-title", "removeeval.page.title");	

		
		TemplateViewParameters evalViewParams = (TemplateViewParameters) viewparams;
		
		if(evalViewParams.templateId !=null){
			EvalEvaluation eval = evalsLogic.getEvaluationById(evalViewParams.templateId);
			//EvalEvaluation eval = logic.getEvaluationById(evalViewParams.templateId);
			if(eval != null){
				UIForm form = UIForm.make(tofill, "removeEvalForm");
				UIMessage.make(form, "remove-eval-confirm-pre-name", "removeeval.confirm.pre.name");
				UIOutput.make(form, "evalTitle", eval.getTitle());
				UIMessage.make(form, "remove-eval-confirm-post-name", "removeeval.confirm.post.name");	
				UIMessage.make(form, "remove-eval-note", "removeeval.note");
				
				UIMessage.make(form, "eval-title-header","removeeval.title.header");
				UIMessage.make(form, "assigned-header", "removeeval.assigned.header");
				UIMessage.make(form, "start-date-header", "removeeval.start.date.header");
				UIMessage.make(form, "due-date-header", "removeeval.due.date.header");
				
				int count = evalsLogic.countEvaluationGroups(eval.getId());
				//int count = logic.countEvaluationContexts(eval.getId());
				if (count > 1)
					UIOutput.make(form, "evalAssigned", count + " courses");
				else{
					Long[] evalIds = {eval.getId()};
					Map evalContexts = evalsLogic.getEvaluationGroups(evalIds, true);
					List contexts = (List) evalContexts.get(eval.getId());
					EvalGroup ctxt = (EvalGroup) contexts.get(0);
					String title = ctxt.title;
					UIOutput.make(form, "evalAssigned",title);
				}
				
				DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
				
				UIOutput.make(form, "evalStartDate", df.format(eval.getStartDate()));
				UIOutput.make(form, "evalDueDate", df.format(eval.getDueDate()));
				
				UICommand.make(form, "cancelRemoveEvalAction", UIMessage.make("general.cancel.button"), 
						"#{evaluationBean.cancelRemoveEvalAction}");
				
				UICommand removeCmd = UICommand.make(form, "removeEvalAction", UIMessage.make("removeeval.remove.button"), 
									"#{evaluationBean.removeEvalAction}"); 
				removeCmd.parameters.add(new UIELBinding("#{evaluationBean.evalId}",eval.getId().toString()));				
			
			}
		}
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
		i.add(new NavigationCase(ControlEvaluationsProducer.VIEW_ID, new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID)));
		return i;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters() {
		return new TemplateViewParameters();
	}
	
	
}
