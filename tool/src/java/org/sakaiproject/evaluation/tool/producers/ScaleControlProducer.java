/******************************************************************************
 * ScaleControlProducer.java - created by kahuja@vt.edu
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
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.params.EvalScaleParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
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
public class ScaleControlProducer implements ViewComponentProducer, NavigationCaseReporter {

	public static final String VIEW_ID = "scale_control"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalScalesLogic scalesLogic;
	public void setScalesLogic(EvalScalesLogic scalesLogic) {
		this.scalesLogic = scalesLogic;
	}
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
	
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
		UIMessage.make(tofill, "scale-control-title", "scalecontrol.page.title"); //$NON-NLS-1$ //$NON-NLS-2$

		UIInternalLink.make(tofill, "add-new-scale-link", 					//$NON-NLS-1$ 
				UIMessage.make("scalecontrol.add.new.scale.link"), 			//$NON-NLS-1$
				new EvalScaleParameters(ScaleAddModifyProducer.VIEW_ID, 
				EvaluationConstant.NEW_SCALE));
		
		UIMessage.make(tofill, "scales-control-heading", "scalecontrol.page.heading"); //$NON-NLS-1$ //$NON-NLS-2$
		
		//Get all the scales that are owned by a user
		List scaleList = scalesLogic.getScalesForUser(currentUserId, null);
		for (int i = 0; i < scaleList.size(); ++i){

			EvalScale scale = (EvalScale) scaleList.get(i);
			UIBranchContainer listOfScales = UIBranchContainer.make(tofill, "verticalDisplay:");
			UIOutput.make(listOfScales, "scale-no", new Integer(i+1).toString()); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(listOfScales, "scale-title", scale.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
			
			/*
			 * If scale is locked do nothing. Else checking that whether 
			 * this user can control the scale for modification / delete.
			 * 
			 * Note that although canControlScale does a locked check,
			 * it is better to avoid a cycle by checking the local data
			 * i.e. getLocked() call.
			 */
			if (scale.getLocked().booleanValue()) {
				//do nothing
			}
			else {
				if (scalesLogic.canControlScale(currentUserId, scale.getId())) {
					
					UIInternalLink.make(listOfScales, "modify-sidelink", 				//$NON-NLS-1$ 
							UIMessage.make("scalecontrol.modify.link"), 				//$NON-NLS-1$
							new EvalScaleParameters(ScaleAddModifyProducer.VIEW_ID, scale.getId()));
					
					/*
					 * Expert scales cannot be deleted. In other words, only 
					 * non-expert scales can be deleted / removed. 
					 */ 
					if (scale.getExpert().booleanValue()) {
						//do nothing
					}
					else  {
						UIInternalLink.make(listOfScales, "remove-sidelink", 				//$NON-NLS-1$ 
								UIMessage.make("scalecontrol.remove.link"), 				//$NON-NLS-1$
								new EvalScaleParameters(RemoveScaleProducer.VIEW_ID, scale.getId()));
					}
				}
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
			
			//Based on the scale ideal value in the database, pick the corresponding from the messages file.
			if (scale.getIdeal() == null)
				UIMessage.make(listOfScales, "ideal-value", 
						"scalecontrol.ideal.scale.option.label.none");
			else if (scale.getIdeal().equals(EvalConstants.SCALE_IDEAL_MID))
				UIMessage.make(listOfScales, "ideal-value", 
						"scalecontrol.ideal.scale.option.label.mid");
			else if (scale.getIdeal().equals(EvalConstants.SCALE_IDEAL_HIGH))
				UIMessage.make(listOfScales, "ideal-value",
						"scalecontrol.ideal.scale.option.label.high");
			else 
				UIMessage.make(listOfScales, "ideal-value", 
						"scalecontrol.ideal.scale.option.label.low");
		 }
	}
	
	public List reportNavigationCases() {
		List i = new ArrayList();
		//TODO
		return i;
	}
}
