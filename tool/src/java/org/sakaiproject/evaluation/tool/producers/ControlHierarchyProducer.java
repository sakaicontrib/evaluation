package org.sakaiproject.evaluation.tool.producers;

import java.util.HashMap;
import java.util.Map;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.tool.utils.HierarchyRenderUtil;
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

/*
 * This producer renders GUI for viewing the Eval Hierarchy as well as 
 * modifying the hierarchy.  It can only be viewed by Sakai System Administrators.
 */
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
   
   private HierarchyRenderUtil hierUtil;
   public void setHierarchyRenderUtil(HierarchyRenderUtil util) {
       hierUtil = util;
   }

   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
      String currentUserId = external.getCurrentUserId();
      boolean userAdmin = external.isUserAdmin(currentUserId);

      if (!userAdmin) {
         // Security check and denial
         throw new SecurityException("Non-admin users may not access this page");
      }

      UIMessage.make(tofill, "body-title", "controlhierarchy.page.title");

      renderBreadcrumbs(tofill);

      translateTableHeaders(tofill);

      //EvalHierarchyNode root = hierarchyLogic.getRootLevelNode();
      //renderHierarchyNode(tofill, root, 0);
      hierUtil.renderModifyHierarchyTree(tofill, "heirarchy-tree:");

      UIInternalLink.make(tofill, "done-link", UIMessage.make("controlhierarchy.done"),
            new SimpleViewParameters(AdministrateProducer.VIEW_ID));

   }
   
   /*
    * top menu links and bread crumbs here
    * 
    * @param tofill
    */
   public void renderBreadcrumbs(UIContainer tofill) {
       UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"),
               new SimpleViewParameters(SummaryProducer.VIEW_ID));
       UIInternalLink.make(tofill, "administrate-toplink", UIMessage.make("administrate.page.title"),
               new SimpleViewParameters(AdministrateProducer.VIEW_ID));
       UIMessage.make(tofill, "page-title", "controlhierarchy.breadcrumb.title");
   }
   
   /*
    * Translate the table headers and any other decorations on or around the
    * table.
    * 
    * @param tofill
    */
   public void translateTableHeaders(UIContainer tofill) {
       UIMessage.make(tofill, "hierarchy-header", "controlhierarchy.table.heirarchy.header");
       UIMessage.make(tofill, "add-item-header", "controlhierarchy.table.additem.header");
       UIMessage.make(tofill, "modify-item-header", "controlhierarchy.table.modifyitem.header");
       UIMessage.make(tofill, "items-level-header", "controlhierarchy.table.itemslevel.header");
       UIMessage.make(tofill, "assign-groups-header", "controlhierarchy.table.assigngroups.header");
   }

}
