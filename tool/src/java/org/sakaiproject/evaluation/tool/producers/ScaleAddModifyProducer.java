/******************************************************************************
 * ScaleAddModifyProducer.java - created by kahuja@vt.edu
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
import org.sakaiproject.evaluation.logic.EvalScalesLogic;
import org.sakaiproject.evaluation.model.EvalScale;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles scale addition, removal, and modification.
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 */
public class ScaleAddModifyProducer implements ViewComponentProducer, NavigationCaseReporter {

	// RSF specific
	public static final String VIEW_ID = "scale_add_modify"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}

	// Spring injection 
	private EvalScalesLogic scalesLogic;
	public void setScalesLogic(EvalScalesLogic scalesLogic) {
		this.scalesLogic = scalesLogic;
	}

	// Spring injection 
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
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

		/*
		 * top menu links and bread crumbs here
		 */
		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID)); //$NON-NLS-1$ //$NON-NLS-2$
		if (userAdmin) {
			UIInternalLink.make(tofill, "control-panel-toplink", UIMessage.make("controlpanel.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
					new SimpleViewParameters(ControlPanelProducer.VIEW_ID));
		}
		UIInternalLink.make(tofill, "administrate-toplink", UIMessage.make("administrate.page.title"), new SimpleViewParameters(AdministrateProducer.VIEW_ID)); //$NON-NLS-1$ //$NON-NLS-2$
		UIInternalLink.make(tofill, "scale-control-toplink", UIMessage.make("scalecontrol.page.title"), new SimpleViewParameters(ScaleControlProducer.VIEW_ID) ); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIMessage.make(tofill, "scale-add-modify-title", "scaleaddmodify.page.title"); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput.make(tofill, "title", "#{templateBean.title}");
		
		//Get all the scales that are owned by a user
		List scaleList = scalesLogic.getScalesForUser(currentUserId, null);
		for (int i = 0; i < scaleList.size(); ++i){

			EvalScale scale = (EvalScale) scaleList.get(i);
			UIBranchContainer listOfScales = UIBranchContainer.make(tofill, "verticalDisplay:");
			UIOutput.make(listOfScales, "scale-no", new Integer(i+1).toString()); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(listOfScales, "scale-title", scale.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
			
			//Use can modify / remove scale only if it is not locked.
			if (scalesLogic.canControlScale(currentUserId, scale.getId())) {
				UIMessage.make(listOfScales, "modify-sidelink","scalecontrol.modify.link"); //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(listOfScales, "remove-sidelink", "scalecontrol.remove.link"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			//Display the scale options vertically
			//ASCII value of 'a' = 97 so initial value is 96.
			char[] startOptionsNo = {96};
			for (int j = 0; j < scale.getOptions().length; ++j){
				UIBranchContainer scaleOptions = UIBranchContainer.make(listOfScales, "scaleOptions:");
				startOptionsNo[0]++;
				UIOutput.make(scaleOptions, "scale-option-no", new String(startOptionsNo)); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(scaleOptions, "scale-option-label", (scale.getOptions())[j]); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			UIMessage.make(listOfScales, "ideal-scale-point", "scalecontrol.ideal.scale.title"); //$NON-NLS-1$ //$NON-NLS-2$
			//If no ideal value is set then display "None" on webpage
			if (scale.getIdeal() == null)
				UIOutput.make(listOfScales, "ideal-value", "None");
			else
				UIOutput.make(listOfScales, "ideal-value", scale.getIdeal());
		 }
	}
	
	/* 
	 * (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
		return i;
	}
}
