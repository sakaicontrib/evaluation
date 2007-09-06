package org.sakaiproject.evaluation.tool.producers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import org.sakaiproject.evaluation.logic.model.HierarchyNode;

public class ControlHierarchyProducer implements ViewComponentProducer {
    public static final String VIEW_ID = "control_hierarchy";

    public String getViewID() {
        return VIEW_ID;
    }

    private EvalExternalLogic external;
    public void setExternal(EvalExternalLogic external) {
        this.external = external;
    }
    
    private EvalHierarchyProvider hierarchyProvider;
    public void setEvalHierarchyProvider(EvalHierarchyProvider provider) {
        this.hierarchyProvider = provider;
    }

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
        UIMessage.make(tofill, "page-title", "controlhierarchy.breadcrumb.title");

        HierarchyNode root = hierarchyProvider.getRootLevelNode();
        renderHierarchyNode(tofill, root);
        /*
         * Done Link at bottom of page.
         */
        UIInternalLink.make(tofill, "done-link", UIMessage.make("controlhierarchy.done"), new SimpleViewParameters(AdministrateProducer.VIEW_ID));
        
    }

    public void renderHierarchyNode(UIContainer tofill, HierarchyNode node) {
        System.out.println("Node: " + node.level + ", " + node.id + ", " + node.name);
        
        UIBranchContainer tableRow = UIBranchContainer.make(tofill, "hierarchy-level-row:");
        UIOutput name = UIOutput.make(tableRow, "node-name", node.name);
        Map attr = new HashMap();
        attr.put("style", "text-indent:"+node.level+"em");
        name.decorate(new UIFreeAttributeDecorator(attr));
        UIInternalLink.make(tableRow, "add-child-link", new SimpleViewParameters(VIEW_ID));
        UIInternalLink.make(tableRow, "modify-node-link", new SimpleViewParameters(VIEW_ID));
        
        Set<HierarchyNode> children = hierarchyProvider.getChildNodes(node.id);
        
        if (children.size() > 0) {
            UIOutput.make(tableRow, "number-children", children.size()+"");
        }
        else {
            UIForm removeForm = UIForm.make(tableRow, "remove-node-form");
            UICommand.make(removeForm, "remove-node-button", "");
        }
        
        for (HierarchyNode child: children) {
            renderHierarchyNode(tofill, child);
        }
    }
}
