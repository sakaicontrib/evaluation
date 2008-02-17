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

import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;
import org.sakaiproject.evaluation.tool.viewparams.EvalScaleParameters;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
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
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handles scale removal confirmation.
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 */
public class RemoveScaleProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {

	// RSF specific
	public static final String VIEW_ID = "remove_scale";
	public String getViewID() {
		return VIEW_ID;
	}

	// Spring injection 
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private LocalTemplateLogic localTemplateLogic;
	public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
		this.localTemplateLogic = localTemplateLogic;
	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		String currentUserId = external.getCurrentUserId();
		boolean userAdmin = external.isUserAdmin(currentUserId);

		if (!userAdmin) {
			// Security check and denial
			throw new SecurityException("Non-admin users may not access this page");
		}

		EvalScaleParameters evalScaleParams = (EvalScaleParameters) viewparams;
		Long scaleId = evalScaleParams.scaleId;

		/*
		 * Fetching the scale from LocalScaleLogic just because 
		 * we need the number of scale options.
		 */
		EvalScale scale = localTemplateLogic.fetchScale(scaleId);

		/*
		 * top menu links and bread crumbs here
		 */
		UIInternalLink.make(tofill, "summary-link", UIMessage.make("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID));
		UIInternalLink.make(tofill, "administrate-link", UIMessage.make("administrate.page.title"), new SimpleViewParameters(AdministrateProducer.VIEW_ID));
		UIInternalLink.make(tofill, "scale-control-toplink", UIMessage.make("controlscales.page.title"), new SimpleViewParameters(ControlScalesProducer.VIEW_ID));

		// Page title
		UIMessage.make(tofill, "page-title", "removescale.page.title");

        UIMessage.make(tofill, "removescale.confirm.text", 
                "removescale.confirm.text", new Object[] {scale.getTitle()});


		UIMessage.make(tofill, "remove-scale-cancel-button", "general.cancel.button");

        UIForm form = UIForm.make(tofill, "remove-scale-form");
		UICommand deleteCommand = UICommand.make(form, "remove-scale-remove-button", 
				UIMessage.make("removescale.remove.scale.button"), "#{scaleBean.deleteScaleAction}");
		deleteCommand.parameters.add(new UIELBinding("#{scaleBean.scaleId}", scaleId));
	}

	/* 
	 * (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List togo = new ArrayList();
		togo.add(new NavigationCase("success", new SimpleViewParameters(ControlScalesProducer.VIEW_ID)));
		return togo;
	}

	public ViewParameters getViewParameters() {
		return new EvalScaleParameters(VIEW_ID, null);
	}
}
