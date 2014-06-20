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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.HierarchyNodeParameters;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class ModifyHierarchyNodeGroupsProducer extends EvalCommonProducer implements ViewParamsReporter, NavigationCaseReporter {
    public static final String VIEW_ID = "modify_hierarchy_node_groups";
    
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
	
    public String getViewID() {
        return VIEW_ID;
    }

    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        if (!userAdmin) {
            // Security check and denial
            throw new SecurityException("Non-admin users may not access this locator");
        }

        /*
         * top links here
         */
        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        HierarchyNodeParameters params = (HierarchyNodeParameters) viewparams;
        String nodeId = params.nodeId;
        EvalHierarchyNode evalNode = hierarchyLogic.getNodeById(params.nodeId);

        // NOTE: This appears to be a legitimate use of the the perms check - maybe should use the user assignments? -AZ
        List<EvalGroup> evalGroups = commonLogic.getEvalGroupsForUser(commonLogic.getAdminUserId(), EvalConstants.PERM_BE_EVALUATED);
        
        // Add the groups provided by our external hierarchy, if any...
        Set<String> hierarchyEvalGroupIds = hierarchyLogic.getEvalGroupsForNode(params.nodeId);
        
        for (String hierarchyEvalGroupId : hierarchyEvalGroupIds) {
            EvalGroup c = null;
            try {
                c = commonLogic.makeEvalGroupObject("/site/"+hierarchyEvalGroupId.substring(6));
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }
            if (c != null) {
                int dupe = 0;
                for (EvalGroup evalGroup : evalGroups) {
                    if (evalGroup.title.equals(c.title)) {
                        dupe = 1;
                    }
                }
                if (dupe == 1) {
                    //System.out.println(hierarchyEvalGroupId+" is already in the list, so I won't add it.");
                } else {
                    evalGroups.add(c);
                    //System.out.println("Have added "+hierarchyEvalGroupId+"to list of evalgroups.");
                }
            } else {
                System.out.println("Could not get an evalgroup for "+hierarchyEvalGroupId);
            }
        }

        Collections.sort(evalGroups, new Comparator<EvalGroup>() {
            public int compare(final EvalGroup e1, final EvalGroup e2) {
                return e1.title.compareTo(e2.title);
            }
        });

        /*
         * Page titles and instructions, top menu links and bread crumbs here
         */
        UIInternalLink.make(tofill, "hierarchy-toplink", UIMessage.make("controlhierarchy.breadcrumb.title"), new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID));

        UIMessage.make(tofill, "page-title", "hierarchynode.groups.breadcrumb.title");

        UIMessage.make(tofill, "assign-groups-title","hierarchynode.groups.body.title", new String[] {evalNode.title});

        UIMessage.make(tofill, "select-header", "hierarchynode.groups.table.select");
        UIMessage.make(tofill, "title-header", "hierarchynode.groups.table.title");

        UIForm form = UIForm.make(tofill, "assign-groups-form");
        for (EvalGroup group: evalGroups) {
            UIBranchContainer tablerow = UIBranchContainer.make(form, "group-row:");
            UIBoundBoolean.make(tablerow, "group-checkbox", "hierNodeGroupsLocator."+nodeId+"."+group.evalGroupId);
            UIOutput.make(tablerow, "group-title", group.title);
        }

        UICommand.make(form, "save-groups-button", UIMessage.make("hierarchynode.groups.save"),
        "hierNodeGroupsLocator.saveAll");
        UIInternalLink.make(form, "cancel-link", UIMessage.make("hierarchynode.groups.cancel"),
                new HierarchyNodeParameters(ControlHierarchyProducer.VIEW_ID, null, params.expanded));
    }

    public ViewParameters getViewParameters() {
        return new HierarchyNodeParameters();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List reportNavigationCases() {
        List cases = new ArrayList();
        cases.add(new NavigationCase(null, new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID)));
        return cases;
    }

}
