/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
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
public class ChownTemplateProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

	public static final String VIEW_ID = "chown_template";
	public String getViewID(){
		return VIEW_ID;
	}

	private EvalCommonLogic commonLogic;
	public void setCommonLogic(EvalCommonLogic commonLogic) {
		this.commonLogic = commonLogic;
	}

	private EvalAuthoringService authoringService;
	public void setAuthoringService(EvalAuthoringService authoringService) {
		this.authoringService = authoringService;
	}
	
	private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}
	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		String currentUserId = commonLogic.getCurrentUserId();

		String actionBean = "templateBBean.";

		UIMessage.make(tofill, "chown-template-title", "chowntemplate.page.title");
		UIMessage.make(tofill, "control-panel-title","modifytemplate.page.title");

		navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

		TemplateViewParameters evalViewParams = (TemplateViewParameters) viewparams;
		
		if (evalViewParams.templateId != null) {
			EvalTemplate template = authoringService.getTemplateById(evalViewParams.templateId);

			if (authoringService.canModifyTemplate(currentUserId, template.getId())) {
				// Can remove template
				UIBranchContainer chownDiv = UIBranchContainer.make(tofill,"chownDiv:");
				UIMessage.make(chownDiv, "chown-template-confirm-text", "chowntemplate.confirm.text", new Object[] {template.getTitle()});
				UIMessage.make(tofill, "cancel-command-link", "general.cancel.button");

				UIForm form = UIForm.make(chownDiv, "chown-template-form");
				UIMessage.make(form, "chown-template-newownerlabel", "chowntemplate.chown.label");
				UIInput.make(form, "chown-template-newowner", (actionBean + "templateOwner"));
				UICommand chownCmd = UICommand.make(form, "chown-template-button", 
						UIMessage.make("chowntemplate.chown.button"), actionBean + "chownTemplate");
				chownCmd.parameters.add(new UIELBinding(actionBean + "templateId", template.getId().toString()));
			} else {
				// cannot remove for some reason
				UIMessage.make(tofill, "cannot-chown-message", "chowntemplate.nochown.text", new Object[] {template.getTitle()});
			}
		} else {
		   throw new IllegalArgumentException("templateId must be set for this view");
		}
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
   public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> i = new ArrayList<>();
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
