package org.sakaiproject.evaluation.tool.producers;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
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

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private HierarchyRenderUtil hierUtil;
    public void setHierarchyRenderUtil(HierarchyRenderUtil util) {
        hierUtil = util;
    }
    
    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

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

        // start rendering the hierarchy controls
        hierUtil.renderModifyHierarchyTree(tofill, "hierarchy-tree:", false, false, false);

        // done rendering the hierarchy controls
        UIInternalLink.make(tofill, "done-link", UIMessage.make("controlhierarchy.done"),
                new SimpleViewParameters(AdministrateProducer.VIEW_ID));

    }

}
