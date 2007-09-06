
package org.sakaiproject.evaluation.tool.producers;

import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
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
      renderHierarchyNode(tofill, root);
      /*
       * Done Link at bottom of page.
       */
      UIInternalLink.make(tofill, "done-link", UIMessage.make("controlhierarchy.done"),
            new SimpleViewParameters(AdministrateProducer.VIEW_ID));

   }

   public void renderHierarchyNode(UIContainer tofill, EvalHierarchyNode node) {
      System.out.println("Node: " + node);

      UIBranchContainer tableRow = UIBranchContainer.make(tofill, "hierarchy-level-row:");
      UIOutput name = UIOutput.make(tableRow, "node-name", node.title);
      // TODO - put this back in somehow? -AZ
//      Map attr = new HashMap();
//      attr.put("style", "text-indent:" + node.level + "em");
//      name.decorate(new UIFreeAttributeDecorator(attr));
      UIInternalLink.make(tableRow, "add-child-link", new SimpleViewParameters(VIEW_ID));
      UIInternalLink.make(tableRow, "modify-node-link", new SimpleViewParameters(VIEW_ID));

      Set<EvalHierarchyNode> children = hierarchyLogic.getChildNodes(node.id, true);

      if (children.size() > 0) {
         UIOutput.make(tableRow, "number-children", children.size() + "");
      } else {
         UIForm removeForm = UIForm.make(tableRow, "remove-node-form");
         UICommand.make(removeForm, "remove-node-button", "");
      }

      for (EvalHierarchyNode child : children) {
         renderHierarchyNode(tofill, child);
      }
   }
}
