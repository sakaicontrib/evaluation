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
import org.sakaiproject.tool.api.SessionManager;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * This presents a list of chosen evaluations, elements of which may 
 * be de-selected before routing their responses to the ContentHostingService.
 *
 * @author rwellis
 */
public class ConfirmExportResponsesProducer implements ViewComponentProducer,
	NavigationCaseReporter{
	
	/**
	 * This is used for navigation within the system.
	 */
	public static final String VIEW_ID = "confirm-export-responses";

	public String getViewID() {
		return VIEW_ID;
	}
	
	// Spring injection 
	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		
	     String currentUserId = externalLogic.getCurrentUserId();
	      boolean userAdmin = externalLogic.isUserAdmin(currentUserId);

	      if (! userAdmin) {
	         // Security check and denial
	         throw new SecurityException("Non-admin users may not access this page");
	      }

	      /*
	       * top links here
	       */
	      UIMessage.make(tofill, "confirm-export-responses-title", "confirm.export.resonses.page.title");
	      
	      // page title
	      UIMessage.make(tofill, "page-title", "confirm.export.resonses.page.title");
	}

	public List reportNavigationCases() {
		List l = new ArrayList();
		
		//cancel & continue
		l.add(new NavigationCase(AdministrateProducer.VIEW_ID, new SimpleViewParameters(AdministrateProducer.VIEW_ID)));
		
		return l;
	}

}
