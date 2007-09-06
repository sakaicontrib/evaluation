package org.sakaiproject.evaluation.tool.producers;

import java.util.List;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class ImportErrorProducer implements 
ViewComponentProducer, 
ViewParamsReporter, 
NavigationCaseReporter
{
	
	/**
	 * This is used for navigation within the system.
	 */
	public static final String VIEW_ID = "import_error";
	public String getViewID() {
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		// TODO Auto-generated method stub
		
	}

	public ViewParameters getViewParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	public List reportNavigationCases() {
		// TODO Auto-generated method stub
		return null;
	}

}
