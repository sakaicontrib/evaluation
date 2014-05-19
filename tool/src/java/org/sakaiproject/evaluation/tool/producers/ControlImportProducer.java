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

import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;

import uk.ac.cam.caret.sakai.rsf.helper.HelperViewParameters;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
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

public class ControlImportProducer implements ViewComponentProducer, ViewParamsReporter,
        NavigationCaseReporter {

    // helper tool
    public static final String HELPER = "sakai.filepicker";

    /**
     * This is used for navigation within the system.
     */
    public static final String VIEW_ID = "control_import";

    public String getViewID() {
        return VIEW_ID;
    }

    // Spring injection
    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private SessionManager sessionManager;
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }
    
    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

    public void fillComponents(UIContainer tofill, ViewParameters viewparams,
            ComponentChecker checker) {

        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        if (!userAdmin) {
            // Security check and denial
            throw new SecurityException("Non-admin users may not access this page");
        }

        /*
         * top links here
         */
        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        // parameters for helper
        ToolSession toolSession = sessionManager.getCurrentToolSession();
        toolSession.setAttribute(FilePickerHelper.FILE_PICKER_TITLE_TEXT, "XML File Data Import");
        toolSession.setAttribute(FilePickerHelper.FILE_PICKER_INSTRUCTION_TEXT,
                "Please select an XML data file from which to read data.");
        toolSession.setAttribute(FilePickerHelper.FILE_PICKER_MAX_ATTACHMENTS,
                FilePickerHelper.CARDINALITY_SINGLE);
        // rsf:id helper-id
        UIOutput.make(tofill, HelperViewParameters.HELPER_ID, HELPER);
        // rsf:id helper-binding method binding
        UICommand.make(tofill, HelperViewParameters.POST_HELPER_BINDING, "#{importBean.process}");
    }

    @SuppressWarnings("unchecked")
    public List reportNavigationCases() {
        List l = new ArrayList();
        l.add(new NavigationCase("importing",
                new SimpleViewParameters(AdministrateProducer.VIEW_ID)));
        // TODO intercepter to configure display of error message?
        l.add(new NavigationCase("no-reference", new SimpleViewParameters(
                ImportErrorProducer.VIEW_ID)));
        l.add(new NavigationCase("permission-exception", new SimpleViewParameters(
                ImportErrorProducer.VIEW_ID)));
        l.add(new NavigationCase("idunused-exception", new SimpleViewParameters(
                ImportErrorProducer.VIEW_ID)));
        l.add(new NavigationCase("type-exception", new SimpleViewParameters(
                ImportErrorProducer.VIEW_ID)));
        l.add(new NavigationCase("exception", new SimpleViewParameters(
                        ImportErrorProducer.VIEW_ID)));
        return l;
    }

    public ViewParameters getViewParameters() {
        return new HelperViewParameters();
    }
}
