package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.locators.HierarchyNodeLocator;
import org.sakaiproject.evaluation.tool.viewparams.ModifyHierarchyNodeParameters;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/*
 * This producer renders the form page for adding or modifying the properties
 * for an Evaluation Hierarchy Node. At this point these are limited to Title
 * and Abbreviation.
 * 
 * This producer handles both cases, new nodes and existing nodes, making use
 * of the EL syntax in HierarchyNodeLocator. 
 */
public class ModifyHierarchyNodeProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {

    public static final String VIEW_ID = "modify_hierarchy_node";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
        this.hierarchyLogic = hierarchyLogic;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
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
        if (!((Boolean) evalSettings.get(EvalSettings.DISABLE_ITEM_BANK))) {
            UIInternalLink.make(tofill, "control-items-link",
                    UIMessage.make("controlitems.page.title"), 
                    new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
        }
        UIInternalLink.make(tofill, "control-evaluations-link",
                UIMessage.make("controlevaluations.page.title"),
                new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));

        UIInternalLink.make(tofill, "hierarchy-toplink", 
                UIMessage.make("controlhierarchy.breadcrumb.title"), new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID));

        UIMessage.make(tofill, "page-title", "modifyhierarchynode.breadcrumb.title");

        //EvalHierarchyNode toEdit 
        ModifyHierarchyNodeParameters params = (ModifyHierarchyNodeParameters) viewparams;
        boolean addingChild = params.addingChild;
        EvalHierarchyNode node = hierarchyLogic.getNodeById(params.nodeId);

        String ELName = "";
        if (addingChild) {
            ELName = HierarchyNodeLocator.NEW_PREFIX + node.id;
            UIMessage.make(tofill, "modify-location-message", "modifyhierarchynode.add.location", new String[] {node.title});
        }
        else {
            ELName = node.id;
            UIMessage.make(tofill, "modify-location-message", "modifyhierarchynode.modify.location", new String[] {node.title});
        }

        /*
         * The Submission Form
         */
        UIForm form = UIForm.make(tofill, "modify-node-form");

        UIInput.make(form, "node-title", "hierNodeLocator."+ELName+".title");
        UIMessage.make(form, "title-label", "modifyhierarchynode.title.label");

        UIInput.make(form, "node-abbr", "hierNodeLocator."+ELName+".description");
        UIMessage.make(form, "abbreviation-label", "modifyhierarchynode.abbreviation.label");

        UICommand.make(form, "save-node-button", UIMessage.make("modifyhierarchynode.save"), "hierNodeLocator.saveAll");
        UIInternalLink.make(form, "cancel-link", UIMessage.make("modifyhierarchynode.cancel"), new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID));
    }

    public ViewParameters getViewParameters() {
        return new ModifyHierarchyNodeParameters();
    }

    @SuppressWarnings("unchecked")
    public List reportNavigationCases() {
        List cases = new ArrayList();
        cases.add(new NavigationCase(null, new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID)));
        return cases;
    }

}
