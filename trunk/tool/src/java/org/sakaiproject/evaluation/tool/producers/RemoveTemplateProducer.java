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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
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
 * @author Aaron Zeckosi [rewrite]
 * @author Rui Feng (fengr@vt.edu)
 */
public class RemoveTemplateProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

	public static final String VIEW_ID = "remove_template";
	public String getViewID(){
		return VIEW_ID;
	}

	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }

	
	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		String currentUserId = externalLogic.getCurrentUserId();

		UIMessage.make(tofill, "remove-template-title", "removetemplate.page.title");
		UIMessage.make(tofill, "control-panel-title","modifytemplate.page.title");
		
		UIInternalLink.make(tofill, "summary-link", UIMessage.make("summary.page.title"), 
				new SimpleViewParameters(SummaryProducer.VIEW_ID));	

		UIInternalLink.make(tofill, "control-templates-link",
				UIMessage.make("controltemplates.page.title"), 
			new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));

		TemplateViewParameters evalViewParams = (TemplateViewParameters) viewparams;
		
		if (evalViewParams.templateId != null) {
			EvalTemplate template = authoringService.getTemplateById(evalViewParams.templateId);

			if (authoringService.canRemoveTemplate(currentUserId, template.getId())) {
				// Can remove template
				UIBranchContainer removeDiv = UIBranchContainer.make(tofill,"removeDiv:");
				UIMessage.make(removeDiv, "remove-template-confirm-text", "removetemplate.confirm.text", new Object[] {template.getTitle()});
				UIMessage.make(tofill, "cancel-command-link", "general.cancel.button");

				UIForm form = UIForm.make(removeDiv, "remove-template-form");
				UICommand removeCmd = UICommand.make(form, "remove-template-button", 
						UIMessage.make("removetemplate.remove.button"), 
						"#{evaluationBean.removeTemplateAction}");
				removeCmd.parameters.add(new UIELBinding("#{evaluationBean.tmplId}", template.getId().toString()));
			} else {
				// cannot remove for some reason
				UIMessage.make(tofill, "cannot-remove-message", "removetemplate.noremove.text", new Object[] {template.getTitle()});
			}
		}
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
		i.add(new NavigationCase("success", new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID)));
		return i;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters() {
		return new TemplateViewParameters();
	}

}
