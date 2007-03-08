/******************************************************************************
 * RemoveScaleProducer.java - created by kahuja@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.LocalScaleLogic;
import org.sakaiproject.evaluation.tool.params.EvalScaleParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handles scale removal confirmation.
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 */
public class RemoveScaleProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {

	// RSF specific
	public static final String VIEW_ID = "remove_scale"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}

	// Spring injection 
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
	
    private LocalScaleLogic localScaleLogic;
    public void setLocalScaleLogic(LocalScaleLogic localScaleLogic) {
      this.localScaleLogic = localScaleLogic;
    }
    
	/* 
	 * (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, 
	 * 																uk.org.ponder.rsf.viewstate.ViewParameters, 
	 * 																uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		String currentUserId = external.getCurrentUserId();
		boolean userAdmin = external.isUserAdmin(currentUserId);

		if (! userAdmin) {
			// Security check and denial
			throw new SecurityException("Non-admin users may not access this page");
		}
		
		EvalScaleParameters evalScaleParams = (EvalScaleParameters) viewparams;
		Long scaleId = evalScaleParams.scaleId;
		
		/*
		 * Fetching the scale from LocalScaleLogic just because 
		 * we need the number of scale options.
		 */ 
		EvalScale scale = localScaleLogic.fetchScale(scaleId);
		
		/*
		 * top menu links and bread crumbs here
		 */
		UIInternalLink.make(tofill, "summary-toplink", 											//$NON-NLS-1$ 
				UIMessage.make("summary.page.title"), 											//$NON-NLS-1$ 
				new SimpleViewParameters(SummaryProducer.VIEW_ID)); 							//$NON-NLS-1$ 
		if (userAdmin) {
			UIInternalLink.make(tofill, "control-panel-toplink",								//$NON-NLS-1$  
					UIMessage.make("controlpanel.page.title"), 									//$NON-NLS-1$ 
					new SimpleViewParameters(ControlPanelProducer.VIEW_ID));					//$NON-NLS-1$ 
		}
		UIInternalLink.make(tofill, "administrate-toplink",										//$NON-NLS-1$  
				UIMessage.make("administrate.page.title"), 										//$NON-NLS-1$ 
				new SimpleViewParameters(AdministrateProducer.VIEW_ID)); 						//$NON-NLS-1$ 
		
		UIInternalLink.make(tofill, "scale-control-toplink", 									//$NON-NLS-1$ 
				UIMessage.make("scalecontrol.page.title"), 										//$NON-NLS-1$ 
				new SimpleViewParameters(ScaleControlProducer.VIEW_ID) ); 						//$NON-NLS-1$ 
		
		// Page title
		UIMessage.make(tofill, "remove-scale-title", 											//$NON-NLS-1$ 
				"scaleremove.page.title"); 														//$NON-NLS-1$
		
		UIForm form = UIForm.make(tofill, "remove-scale-form"); 								//$NON-NLS-1$		

		UIMessage.make(form, "remove-scale-confirm-pre-name", 									//$NON-NLS-1$ 
				"scaleremove.confirm.pre.name"); 												//$NON-NLS-1$
		
		UIOutput.make(form, "scale-title-displayed", scale.getTitle());							//$NON-NLS-1$

		UIMessage.make(form, "remove-scale-confirm-post-name", 									//$NON-NLS-1$ 
				"scaleremove.confirm.post.name"); 												//$NON-NLS-1$

		UICommand.make(form, "remove-scale-cancel-button", 										//$NON-NLS-1$
				UIMessage.make("scaleremove.cancel.button"));									//$NON-NLS-1$

		UICommand deleteCommand = UICommand.make(form, "remove-scale-remove-button", 										//$NON-NLS-1$
				UIMessage.make("scaleremove.remove.scale.button"),								//$NON-NLS-1$
				"#{scaleBean.deleteScale}");   													//$NON-NLS-1$
		
		deleteCommand.parameters.add(new UIELBinding("#{scaleBean.scaleId}", scaleId));
	}
	
	/* 
	 * (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List togo = new ArrayList();
		togo.add(new NavigationCase("success", new SimpleViewParameters(ScaleControlProducer.VIEW_ID)));
		return togo;
	}
	
	public ViewParameters getViewParameters() {
		return new EvalScaleParameters(VIEW_ID, null);
	}	
}
