/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.tool.locators.HierarchyNodeLocator;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.HierarchyNodeParameters;
import org.sakaiproject.evaluation.tool.viewparams.ModifyHierarchyNodeParameters;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/*
 * This producer renders the form page for adding or modifying the properties
 * for an Evaluation Hierarchy Node. At this point these are limited to Title
 * and Abbreviation.
 * 
 * This producer handles both cases, new nodes and existing nodes, making use
 * of the EL syntax in HierarchyNodeLocator. 
 */
public class ModifyHierarchyNodeProducer extends EvalCommonProducer implements ViewParamsReporter, NavigationCaseReporter {

    public static final String VIEW_ID = "modify_hierarchy_node";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
        this.hierarchyLogic = hierarchyLogic;
    }

    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}
    
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
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
        
        UIInternalLink.make(tofill, "hierarchy-toplink", 
                UIMessage.make("controlhierarchy.breadcrumb.title"), new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID));

        UIMessage.make(tofill, "page-title", "modifyhierarchynode.breadcrumb.title");

        //EvalHierarchyNode toEdit 
        ModifyHierarchyNodeParameters params = (ModifyHierarchyNodeParameters) viewparams;
        boolean addingChild = params.addingChild;
        EvalHierarchyNode node = hierarchyLogic.getNodeById(params.nodeId);

        String ELName;
        if (addingChild) {
            ELName = HierarchyNodeLocator.NEW_PREFIX + node.id;
            UIMessage.make(tofill, "modify-location-message", "modifyhierarchynode.add.location", new String[] {node.title});
        }
        else {
            ELName = node.id;
            UIMessage.make(tofill, "modify-location-message", "modifyhierarchynode.modify.location", new String[] {node.title});
        }

        /*
         * The Submission Form
         */
        UIForm form = UIForm.make(tofill, "modify-node-form");

        UIInput.make(form, "node-title", "hierNodeLocator."+ELName+".title");
        UIMessage.make(form, "title-label", "modifyhierarchynode.title.label");

        UIInput.make(form, "node-abbr", "hierNodeLocator."+ELName+".description");
        UIMessage.make(form, "abbreviation-label", "modifyhierarchynode.abbreviation.label");

        UICommand.make(form, "save-node-button", UIMessage.make("modifyhierarchynode.save"), "hierNodeLocator.saveAll");
        UIInternalLink.make( form, "return-link", UIMessage.make( "controlhierarchy.return.link" ),
                new HierarchyNodeParameters( ControlHierarchyProducer.VIEW_ID, null, params.expanded ) );
    }

    public ViewParameters getViewParameters() {
        return new ModifyHierarchyNodeParameters();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List reportNavigationCases() {
        List cases = new ArrayList();
        cases.add(new NavigationCase(null, new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID)));
        return cases;
    }

}
