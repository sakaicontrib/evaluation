package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class ModifyHierarchyNodeGroupsProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {
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

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
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
                new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID));
    }

    public ViewParameters getViewParameters() {
        return new HierarchyNodeParameters();
    }

    @SuppressWarnings("unchecked")
    public List reportNavigationCases() {
        List cases = new ArrayList();
        cases.add(new NavigationCase(null, new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID)));
        return cases;
    }

}
