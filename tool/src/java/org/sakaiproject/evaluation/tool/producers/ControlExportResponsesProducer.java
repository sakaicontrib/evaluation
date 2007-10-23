/**********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This presents components for selecting eval groups and evaluations
 * to include in routing of responses to the ContentHostingService,
 * for distribution through email or writing to a content resource.
 *
 * @author rwellis
 */
public class ControlExportResponsesProducer implements ViewComponentProducer, 
	ViewParamsReporter, 
	NavigationCaseReporter {
	
	/**
	 * This is used for navigation within the system.
	 */
	public static final String VIEW_ID = "control_export_responses";

	public String getViewID() {
		return VIEW_ID;
	}
	
	// Spring injection 
	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		
		/*
		 * TODO selection criteria for evaluations to include, optional params like "include dept"
		 */
	     String currentUserId = externalLogic.getCurrentUserId();
	     boolean userAdmin = externalLogic.isUserAdmin(currentUserId);
	     if (! userAdmin) {
	         // Security check and denial
	         throw new SecurityException("Non-admin users may not access this page");
	     }
	     
	     /*
	      * top links here
	      */
	     UIMessage.make(tofill, "control-export-responses-title", "control.export.resonses.page.title");
	     // page title
	     UIMessage.make(tofill, "page-title", "control.export.resonses.page.title");
	     
	     //form
	     UIForm form = UIForm.make(tofill, "control-export-form");
	     UICommand.make(form, "write-content-resource",UIMessage.make("control.export.responses.button"), 
	    		 "#{exportBean.writeContentResource}");
	     UICommand.make(form, "email-content-resource",UIMessage.make("control.email.responses.button"), 
	    		 "#{exportBean.emailContentResource}");
	     UICommand.make(form, "clear-lock",UIMessage.make("control.export.clear.lock.button"), 
		 "#{exportBean.clearLock}");
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List<NavigationCase> l = new ArrayList();
		l.add(new NavigationCase(AdministrateProducer.VIEW_ID, new SimpleViewParameters(AdministrateProducer.VIEW_ID)));
		l.add(new NavigationCase(SummaryProducer.VIEW_ID, new SimpleViewParameters(SummaryProducer.VIEW_ID)));
		return l;
	}

	public ViewParameters getViewParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
