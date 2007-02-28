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
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

import uk.org.ponder.beanutil.PathUtil;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
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
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
	
	// Used to prepare the path for WritableBeanLocator
    private String SCALES_WBL = "scalesBean";

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
		UIMessage.make(tofill, "scale-add-modify-title", 										//$NON-NLS-1$ 
				"scaleaddmodify.page.title"); 													//$NON-NLS-1$
		
		/*
		 * Title and remove - we don't need a locked check here because
		 * a person can come on this page only if the scale is not locked.
		 */  
		UIMessage.make(tofill, "scale-title-note", 												//$NON-NLS-1$ 
				"scaleaddmodify.scale.title.note");	 											//$NON-NLS-1$

		//TODO
		makeInput(tofill, "scale-title", "1");  											//$NON-NLS-1$ //$NON-NLS-2$
		
		/*
		//TODO
		UIInternalLink.make(tofill, "scale-remove-link", 										//$NON-NLS-1$ 
				UIMessage.make("scaleaddmodify.remove.scale.link"), 							//$NON-NLS-1$
				PreviewEmailProducer.VIEW_ID);
		
		//TODO
		EvalScale scale = null;
		for (int j = 0; j < scale.getOptions().length; ++j){
			UIBranchContainer scaleOptions = UIBranchContainer.make(tofill, "scaleOptions:"); 	//$NON-NLS-1$
			UIOutput.make(scaleOptions, "scale-option-label", (scale.getOptions())[j]); 		//$NON-NLS-1$
			
			//TODO
			UICommand.make(tofill, "scale-remove-option", 										//$NON-NLS-1$
					UIMessage.make("scaleaddmodify.remove.scale.option.button"),				//$NON-NLS-1$
					"#{evaluationBean.saveSettingsAction}");   									//$NON-NLS-1$											
		}
		
		//TODO
		UICommand.make(tofill, "scale-add-point", 												//$NON-NLS-1$
				UIMessage.make("scaleaddmodify.add.scale.option.button"),						//$NON-NLS-1$
				"#{evaluationBean.saveSettingsAction}");   										//$NON-NLS-1$		
		
		UIMessage.make(tofill, "ideal-note-start", 												//$NON-NLS-1$ 
				"scaleaddmodify.scale.ideal.note.start"); 										//$NON-NLS-1$

		UIMessage.make(tofill, "ideal-note-main-text", 											//$NON-NLS-1$ 
				"scaleaddmodify.scale.ideal.note.main.text"); 									//$NON-NLS-1$

		//Ideal scale values radio buttons
		String[] scaleIdealValues = {
				EvalConstants.SCALE_IDEAL_NONE,
				EvalConstants.SCALE_IDEAL_LOW,
				EvalConstants.SCALE_IDEAL_HIGH,
				EvalConstants.SCALE_IDEAL_MID};

		String[] scaleIdealLabels = {
			"scalecontrol.ideal.scale.option.label.none",
			"scalecontrol.ideal.scale.option.label.low",
			"scalecontrol.ideal.scale.option.label.high",
			"scalecontrol.ideal.scale.option.label.mid" 
		};
		
		//TODO
		UISelect radios = UISelect.make(tofill, "scaleIdealValues",
				scaleIdealValues, scaleIdealLabels,
				null, null).setMessageKeys();
		
		radios.optionnames = UIOutputMany.make(scaleIdealLabels);

	    String selectID = radios.getFullID();
	    for (int i = 0; i < scaleIdealValues.length; ++i) {
			UIBranchContainer radiobranch = UIBranchContainer.make(tofill, 
					"scaleIdealValues:", Integer.toString(i)); 									//$NON-NLS-1$
			UISelectChoice.make(radiobranch, "scale-ideal-value", selectID, i); 				//$NON-NLS-1$
			UISelectLabel.make(radiobranch, "scale-ideal-label", selectID, i); 					//$NON-NLS-1$
	    }
	    */
		
		//TODO
		//makeBoolean(tofill, "scale-hidden", "SCALE_HIDDEN"); 									//$NON-NLS-1$ //$NON-NLS-2$
		
		/*
		UIMessage.make(tofill, "scale-hidden-note", 											//$NON-NLS-1$ 
				"scaleaddmodify.scale.hidden.note"); 											//$NON-NLS-1$

		//TODO
		UICommand.make(tofill, "scale-add-modify-cancel-button", 								//$NON-NLS-1$
				UIMessage.make("scaleaddmodify.cancel.button"),									//$NON-NLS-1$
				"#{evaluationBean.saveSettingsAction}");   										//$NON-NLS-1$											

		//TODO
		UICommand.make(tofill, "scale-add-modify-save-button", 									//$NON-NLS-1$
				UIMessage.make("scaleaddmodify.save.scale.button"),								//$NON-NLS-1$
				"#{evaluationBean.saveSettingsAction}");   										//$NON-NLS-1$
		*/											

	}
	
	/* 
	 * (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
		return i;
	}
	
	/*
	 * (non-Javadoc)
	 * This method is used to render checkboxes.
	 */
    private void makeBoolean(UIContainer parent, String ID, String adminkey) {
      // Must use "composePath" here since admin keys currently contain periods
      UIBoundBoolean.make(parent, ID, adminkey == null? null : PathUtil.composePath(SCALES_WBL, adminkey)); 
    }
	
	/*
	 * (non-Javadoc)
	 * This is a common method used to render text boxes.
	 */
    private void makeInput(UIContainer parent, String ID, String adminkey) {
      UIInput.make(parent, ID, PathUtil.composePath(SCALES_WBL, adminkey));
    }	
}
