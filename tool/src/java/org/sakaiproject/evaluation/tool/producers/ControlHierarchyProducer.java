
package org.sakaiproject.evaluation.tool.producers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.tool.viewparams.HierarchyNodeParameters;
import org.sakaiproject.evaluation.tool.viewparams.ModifyHierarchyNodeParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIDeletionBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class ControlHierarchyProducer implements ViewComponentProducer {

   public static final String VIEW_ID = "control_hierarchy";
   public String getViewID() {
      return VIEW_ID;
   }

   private EvalExternalLogic external;
   public void setExternal(EvalExternalLogic external) {
      this.external = external;
   }

   private ExternalHierarchyLogic hierarchyLogic;
   public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
      this.hierarchyLogic = hierarchyLogic;
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
      UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"),
            new SimpleViewParameters(SummaryProducer.VIEW_ID));
      UIInternalLink.make(tofill, "administrate-toplink", UIMessage.make("administrate.page.title"),
            new SimpleViewParameters(AdministrateProducer.VIEW_ID));
      UIMessage.make(tofill, "page-title", "controlhierarchy.breadcrumb.title");

      EvalHierarchyNode root = hierarchyLogic.getRootLevelNode();
      renderHierarchyNode(tofill, root, 0);
      /*
       * Done Link at bottom of page.
       */
      UIInternalLink.make(tofill, "done-link", UIMessage.make("controlhierarchy.done"),
            new SimpleViewParameters(AdministrateProducer.VIEW_ID));

   }

   public void renderHierarchyNode(UIContainer tofill, EvalHierarchyNode node, int level) {
      System.out.println("Node: " + node);

      String title = node.title != null ? node.title : "Null Title?";
      UIBranchContainer tableRow = UIBranchContainer.make(tofill, "hierarchy-level-row:");
      UIOutput name = UIOutput.make(tableRow, "node-name", title);
      Map attr = new HashMap();
      attr.put("style", "text-indent:" + (level*2) + "em");
      name.decorate(new UIFreeAttributeDecorator(attr));
      UIInternalLink.make(tableRow, "add-child-link", new ModifyHierarchyNodeParameters(ModifyHierarchyNodeProducer.VIEW_ID, node.id, true));
      UIInternalLink.make(tableRow, "modify-node-link", new ModifyHierarchyNodeParameters(ModifyHierarchyNodeProducer.VIEW_ID, node.id, false));
      UIInternalLink.make(tableRow, "assign-groups-link", new HierarchyNodeParameters(ModifyHierarchyNodeGroupsProducer.VIEW_ID, node.id));
      
      //node.directChildNodeIds
      //Set<EvalHierarchyNode> children = hierarchyLogic.getChildNodes(node.id, true);

      if (node.directChildNodeIds.size() > 0) {
         UIOutput.make(tableRow, "number-children", node.directChildNodeIds.size() + "");
      } else {
         UIForm removeForm = UIForm.make(tableRow, "remove-node-form");
         UICommand removeButton = UICommand.make(removeForm, "remove-node-button", UIMessage.make("controlhierarchy.remove"));
         removeButton.parameters.add(new UIDeletionBinding("hierNodeLocator."+node.id));
      }

      for (String childId : node.directChildNodeIds) {
         renderHierarchyNode(tofill, hierarchyLogic.getNodeById(childId), level+1);
      }
   }
}
