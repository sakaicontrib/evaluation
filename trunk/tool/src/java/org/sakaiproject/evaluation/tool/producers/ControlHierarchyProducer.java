package org.sakaiproject.evaluation.tool.producers;

import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.tool.utils.HierarchyRenderUtil;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
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

   private HierarchyRenderUtil hierUtil;
   public void setHierarchyRenderUtil(HierarchyRenderUtil util) {
      hierUtil = util;
   }

   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      UIMessage.make(tofill, "page-title", "controlhierarchy.breadcrumb.title");
      UIMessage.make(tofill, "body-title", "controlhierarchy.page.title");

      String currentUserId = external.getCurrentUserId();
      boolean userAdmin = external.isUserAdmin(currentUserId);

      if (!userAdmin) {
         // Security check and denial
         throw new SecurityException("Non-admin users may not access this page");
      }

      /*
       * top links here
       */
      UIInternalLink.make(tofill, "summary-link", 
            UIMessage.make("summary.page.title"), 
            new SimpleViewParameters(SummaryProducer.VIEW_ID));

      UIInternalLink.make(tofill, "administrate-link", 
            UIMessage.make("administrate.page.title"),
            new SimpleViewParameters(AdministrateProducer.VIEW_ID));
      UIInternalLink.make(tofill, "control-scales-link",
            UIMessage.make("controlscales.page.title"),
            new SimpleViewParameters(ControlScalesProducer.VIEW_ID));

      UIInternalLink.make(tofill, "control-templates-link",
            UIMessage.make("controltemplates.page.title"), 
            new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
      UIInternalLink.make(tofill, "control-items-link",
            UIMessage.make("controlitems.page.title"), 
            new SimpleViewParameters(ControlItemsProducer.VIEW_ID));

      hierUtil.renderModifyHierarchyTree(tofill, "heirarchy-tree:");

      UIInternalLink.make(tofill, "done-link", UIMessage.make("controlhierarchy.done"),
            new SimpleViewParameters(AdministrateProducer.VIEW_ID));

   }

}
