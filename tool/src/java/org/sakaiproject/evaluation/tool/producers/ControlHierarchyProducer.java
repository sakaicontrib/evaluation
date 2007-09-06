package org.sakaiproject.evaluation.tool.producers;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
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

        /*
         * Done Link at bottom of page.
         */
        UIInternalLink.make(tofill, "done-link", UIMessage.make("controlhierarchy.done"), new SimpleViewParameters(AdministrateProducer.VIEW_ID));
        
    }

}
