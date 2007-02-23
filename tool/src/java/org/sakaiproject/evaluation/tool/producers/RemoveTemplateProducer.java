/******************************************************************************
 * RemoveTemplateProducer.java - created by fengr@vt.edu on Nov 16, 2006
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
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
 * This page allows the user to remove templates
 * 
 * @author: Rui Feng (fengr@vt.edu)
 */

public class RemoveTemplateProducer implements ViewComponentProducer, NavigationCaseReporter,ViewParamsReporter{
	public static final String VIEW_ID = "remove_template"; //$NON-NLS-1$
	public String getViewID(){
		return VIEW_ID;
	}

	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}

	private EvalEvaluationsLogic evalsLogic;
	public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
		this.evalsLogic = evalsLogic;
	}

	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
	}

	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		/*
		 * 1)checked with Aaron, if a user(owner) can not delete template which is  associated
			with Queued Evalution.
		 *  2)check with Aaron if super Admin could delete normal user's template which is not associated
		 *  with any evaluation , and case a template with Queued evaluation
		 *  3) super Admin delete template with started evaluation
		 *  4) about template with closed evaluation case
		 */
		
		UIMessage.make(tofill, "remove-template-title", "removetemplate.page.title"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(tofill, "control-panel-title","modifytemplate.page.title"); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));	

		EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;
		
		if(evalViewParams.templateId !=null){
			
			EvalTemplate template = templatesLogic.getTemplateById(evalViewParams.templateId);
			if(template != null){
				int count = evalsLogic.countEvaluationsByTemplateId(template.getId());
				if(count > 0) {
					//Can not delete section
					UIBranchContainer noRemoveDiv = UIBranchContainer.make(tofill,"noRemoveDiv:"); //$NON-NLS-1$
					UIMessage.make(noRemoveDiv, "warning-header", "removetemplate.warning.header"); //$NON-NLS-1$ //$NON-NLS-2$
					UIMessage.make(noRemoveDiv, "noremove-note-start", "removetemplate.noremove.note.pre.name"); //$NON-NLS-1$ //$NON-NLS-2$
					UIMessage.make(noRemoveDiv, "noremove-note-middle", "removetemplate.noremove.note.post.name"); //$NON-NLS-1$ //$NON-NLS-2$
					
					String sEmail = (String) settings.get(EvalSettings.FROM_EMAIL_ADDRESS);
					UILink.make(noRemoveDiv, "noremove-note-email", sEmail, "mailto:" + sEmail);
					
					UIMessage.make(noRemoveDiv, "noremove-note-end", "removetemplate.noremove.note.end"); //$NON-NLS-1$ //$NON-NLS-2$
					UIMessage.make(noRemoveDiv, "eval-title-header", "removetemplate.eval.title.header"); //$NON-NLS-1$ //$NON-NLS-2$
					UIMessage.make(noRemoveDiv, "assigned-header", "removetemplate.assigned.header"); //$NON-NLS-1$ //$NON-NLS-2$
					UIMessage.make(noRemoveDiv, "start-date-header", "removetemplate.start.date.header"); //$NON-NLS-1$ //$NON-NLS-2$
					UIMessage.make(noRemoveDiv, "due-date-header", "removetemplate.due.date.header"); //$NON-NLS-1$ //$NON-NLS-2$
					
					UIOutput.make(noRemoveDiv, "NoTemplateTitle", template.getTitle()); //$NON-NLS-1$
					
					//get related evaluations
					List l = evalsLogic.getEvaluationsByTemplateId(template.getId());
					if(l != null && l.size() >0){
						DateFormat df = new SimpleDateFormat("MM/dd/yyyy"); //$NON-NLS-1$
						for(int i =0; i< l.size(); i++){
							UIBranchContainer evalsRb = UIBranchContainer.make(noRemoveDiv, "evalList:", Integer //$NON-NLS-1$
									.toString(i));
							EvalEvaluation eval1 = (EvalEvaluation) l.get(i);
							UIOutput.make(evalsRb, "evalTitle", eval1.getTitle()); //$NON-NLS-1$

							int ct = evalsLogic.countEvaluationGroups(eval1.getId());
							if (ct > 1)
								UIOutput.make(evalsRb, "evalAssigned", count + " courses"); //$NON-NLS-1$ //$NON-NLS-2$
							else{
								Long[] evalIds = {eval1.getId()};
								Map evalContexts = evalsLogic.getEvaluationGroups(evalIds, true);
								List contexts = (List) evalContexts.get(eval1.getId());
								EvalGroup ctxt = (EvalGroup) contexts.get(0);
								String title = ctxt.title;
								UIOutput.make(evalsRb, "evalAssigned",title);
							}

							UIOutput.make(evalsRb, "evalStartDate", df.format(eval1.getStartDate())); //$NON-NLS-1$
							UIOutput.make(evalsRb, "evalDueDate", df.format(eval1.getDueDate())); //$NON-NLS-1$
						}//end of for loop
					}//end of if block
					
					UIMessage.make(noRemoveDiv, "cancel-button", "general.cancel.button");

					
				}else{
					//Can delete section: if there is no evaluation associated with this template
					//first delete items associated with this template
					UIBranchContainer removeDiv = UIBranchContainer.make(tofill,"removeDiv:"); //$NON-NLS-1$
					UIForm form = UIForm.make(removeDiv, "removeTemplateForm"); //$NON-NLS-1$
					UIMessage.make(form, "remove-template-confirm-pre-name", "removetemplate.confirm.pre.name"); //$NON-NLS-1$ //$NON-NLS-2$
					UIMessage.make(form, "remove-template-confirm-post-name", "removetemplate.confirm.post.name"); //$NON-NLS-1$ //$NON-NLS-2$
					UIOutput.make(form, "templateTitle", template.getTitle()); //$NON-NLS-1$
					UICommand.make(form, "cancelRemoveTemplateAction", UIMessage.make("general.cancel.button"),  //$NON-NLS-1$ //$NON-NLS-2$
							"#{evaluationBean.cancelRemoveTemplateAction}"); //$NON-NLS-1$
					UICommand removeCmd = UICommand.make(form, "removeTemplateAction", UIMessage.make("removetemplate.remove.button"),  //$NON-NLS-1$ //$NON-NLS-2$
										"#{evaluationBean.removeTemplateAction}"); //$NON-NLS-1$
					
					removeCmd.parameters.add(new UIELBinding("#{evaluationBean.tmplId}",template.getId().toString())); //$NON-NLS-1$

				}

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
