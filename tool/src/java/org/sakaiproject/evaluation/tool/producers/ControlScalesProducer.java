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
public class ControlScalesProducer implements ViewComponentProducer, NavigationCaseReporter {

	public static final String VIEW_ID = "control_scales";
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

		/*
		 * top menu links and bread crumbs here
		 */
		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID));
		UIInternalLink.make(tofill, "administrate-toplink", UIMessage.make("administrate.page.title"), new SimpleViewParameters(AdministrateProducer.VIEW_ID));
		UIMessage.make(tofill, "page-title", "scalecontrol.page.title");

		UIInternalLink.make(tofill, "add-new-scale-link", 
				UIMessage.make("scalecontrol.add.new.scale.link"), 
				new EvalScaleParameters(ScaleAddModifyProducer.VIEW_ID, null));

		UIMessage.make(tofill, "scales-control-heading", "scalecontrol.page.heading");

		//Get all the scales that are owned by a user
		List scaleList = scalesLogic.getScalesForUser(currentUserId, null);
		for (int i = 0; i < scaleList.size(); ++i) {

			EvalScale scale = (EvalScale) scaleList.get(i);

// NOTE - thise code was here to vet the new scales code, it passed this test -AZ
//			if (i == 0) {
//				System.out.println("Changing scale: " + scale.getTitle() + ":" + scale.getOptions().length);
//				long random = Math.round( Math.random() * 10 );
//				long random2 = Math.round( Math.random() * 3 );
//				String[] options;
//				if (random2 <= 1) {
//					options = new String[] {"az1"+random, "az2"+random, "az3"+random};
//				} else if (random2 <= 2) {
//					options = new String[] {"az1"+random, "az2"+random, "az3"+random, "az4"+random, "az5"+random};
//				} else {
//					options = new String[] {"az1"+random, "az2"+random, "az3"+random, "az4"+random, "az5"+random, "az6"+random, "az7"+random};
//				}
//				scale.setOptions(options);
//				scalesLogic.saveScale(scale, currentUserId);
//				System.out.println("Changed scale: " + scale.getTitle() + ":" + scale.getOptions().length);
//			}

			UIBranchContainer listOfScales = UIBranchContainer.make(tofill, "verticalDisplay:");
			UIOutput.make(listOfScales, "scale-no", new Integer(i + 1).toString());
			UIOutput.make(listOfScales, "scale-title", scale.getTitle());

			/*
			 * If scale is locked do nothing. Else checking that whether 
			 * this user can control the scale for modification / delete.
			 * 
			 * Note that although canControlScale does a locked check,
			 * it is more efficient to avoid a cycle by checking the local data first (i.e. getLocked() call)
			 */
			if (! scale.getLocked().booleanValue()) {
				if (scalesLogic.canControlScale(currentUserId, scale.getId())) {
					UIInternalLink.make(listOfScales, "modify-sidelink", 
							UIMessage.make("scalecontrol.modify.link"), 
							new EvalScaleParameters(ScaleAddModifyProducer.VIEW_ID, scale.getId()));
					UIInternalLink.make(listOfScales, "remove-sidelink", 
							UIMessage.make("scalecontrol.remove.link"), 
							new EvalScaleParameters(RemoveScaleProducer.VIEW_ID, scale.getId()));
				}
			}

			// Display the scale options vertically
			// ASCII value of 'a' = 97 so initial value is 96.
			// This is kinda weird, not sure it is really needed -AZ
			char[] startOptionsNo = { 96 };
			for (int j = 0; j < scale.getOptions().length; ++j) {
				UIBranchContainer scaleOptions = UIBranchContainer.make(listOfScales, "scaleOptions:");
				startOptionsNo[0]++;
				UIOutput.make(scaleOptions, "scale-option-no", new String(startOptionsNo));
				UIOutput.make(scaleOptions, "scale-option-label", (scale.getOptions())[j]);
			}

			UIMessage.make(listOfScales, "ideal-scale-point", "scalecontrol.ideal.scale.title");

			// Based on the scale ideal value, pick the corresponding i18n message
			if (scale.getIdeal() == null)
				UIMessage.make(listOfScales, "ideal-value", "scalecontrol.ideal.scale.option.label.none");
			else if (scale.getIdeal().equals(EvalConstants.SCALE_IDEAL_MID))
				UIMessage.make(listOfScales, "ideal-value", "scalecontrol.ideal.scale.option.label.mid");
			else if (scale.getIdeal().equals(EvalConstants.SCALE_IDEAL_HIGH))
				UIMessage.make(listOfScales, "ideal-value", "scalecontrol.ideal.scale.option.label.high");
			else if (scale.getIdeal().equals(EvalConstants.SCALE_IDEAL_LOW))
				UIMessage.make(listOfScales, "ideal-value", "scalecontrol.ideal.scale.option.label.low");
			else if (scale.getIdeal().equals(EvalConstants.SCALE_IDEAL_OUTSIDE))
				UIMessage.make(listOfScales, "ideal-value", "scalecontrol.ideal.scale.option.label.outside");
			else
				UIMessage.make(listOfScales, "ideal-value", "unknown.caps");
		}
	}

	public List reportNavigationCases() {
		List i = new ArrayList();
		//TODO
		return i;
	}
}
