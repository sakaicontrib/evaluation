package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.viewparams.HierarchyNodeParameters;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
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

    private EvalExternalLogic external;
    public void setExternal(EvalExternalLogic external) {
       this.external = external;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
       this.hierarchyLogic = hierarchyLogic;
    }
    
    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        String currentUserId = external.getCurrentUserId();
        boolean userAdmin = external.isUserAdmin(currentUserId);

        if (!userAdmin) {
            // Security check and denial
            throw new SecurityException("Non-admin users may not access this locator");
        }
        
        HierarchyNodeParameters params = (HierarchyNodeParameters) viewparams;
        String nodeId = params.nodeId;
        
        List<EvalGroup> evalGroups = external.getEvalGroupsForUser("admin", EvalConstants.PERM_BE_EVALUATED);
        
        
        UIForm form = UIForm.make(tofill, "assign-groups-form");
        for (EvalGroup group: evalGroups) {
            UIBranchContainer tablerow = UIBranchContainer.make(form, "group-row:");
            UIBoundBoolean.make(tablerow, "group-checkbox", "hierNodeGroupsLocator."+nodeId+"."+group.evalGroupId);
            UIOutput.make(tablerow, "group-title", group.title);
        }
        
        UICommand.make(form, "save-groups-button", "hierNodeGroupsLocatorInvoker.saveAll");
        UIInternalLink.make(form, "cancel-link", new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID));
    }

    public ViewParameters getViewParameters() {
        return new HierarchyNodeParameters();
    }

    public List reportNavigationCases() {
        List cases = new ArrayList();
        cases.add(new NavigationCase(null, new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID)));
        return cases;
    }

    //List evalGroups = externalLogic.getEvalGroupsForUser(externalLogic.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);

}
