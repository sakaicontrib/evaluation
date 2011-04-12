/******************************************************************************
 * ModifyExpertItemProducer.java - created by rmoyer@umd.edu
 * 
 * Copyright (c) 2010 University of Maryland, College Park
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rick Moyer (rmoyer@umd.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.tool.ExpertItemsBean;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.ModifyExpertItemParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
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

/**
 * EVALSYS-1026
 * This producer renders the form page for the adding or modifying of Expert Item Group
 * Categories or Objectives. At this point these are limited to Title and Description.
 * 
 * @author Rick Moyer (rmoyer@umd.edu) 
 */
public class ModifyExpertItemProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {

    public static final String VIEW_ID = "modify_expert_item";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
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
        UIInternalLink.make(tofill, "hierarchy-toplink", 
                UIMessage.make("controlhierarchy.breadcrumb.title"), new SimpleViewParameters(ControlExpertItemsProducer.VIEW_ID));

        UIMessage.make(tofill, "page-title", "modifyexpertitem.page.title");
        
        String eigBean = "expertItemsBean.";

        //EvalItemGroup toEdit 
        ModifyExpertItemParameters params = (ModifyExpertItemParameters) viewparams;
        Boolean bIsNew = new Boolean(params.isNew);
        
        UIForm form = UIForm.make(tofill, "modify-expertitem-form");
        
        Long eigId;
        UIInput.make(form, "expertitem-new", "expertItemsBean.eigIsNew", bIsNew.toString());
        UIInput.make(form, "expertitem-type", "expertItemsBean.eigType", params.type);
        if ( EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(params.type)) {
        	eigId = params.categoryId;
        	UIMessage.make(form, "title-label", "modifyexpertitem.title.cat.label");
        } else {
        	eigId = params.objectiveId;
        	UIMessage.make(form, "title-label", "modifyexpertitem.title.obj.label");
        }
        
        EvalItemGroup eig;
        if (params.isNew) {
        	eig = new EvalItemGroup();
        	if ( EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE.equals(params.type)) {
        		EvalItemGroup eigParent = authoringService.getItemGroupById(params.categoryId);
        		UIMessage.make(form, "category-label", "modifyexpertitem.title.cat.label");
        		UIOutput.make(form, "category", eigParent.getTitle());
        		UIInput.make(form, "expertitem-parent", "expertItemsBean.eigParentId", params.categoryId.toString());
        	}
        } else {
        	UIInput.make(form, "expertitem-eigId", "expertItemsBean.eigId", eigId.toString());
        	eig = authoringService.getItemGroupById(eigId);
        }
        
        /*
         * The Submission Form
         */
        
        UIInput.make(form, "expertitem-title", "expertItemsBean.eigTitle", eig.getTitle());

        UIInput.make(form, "expertitem-desc", "expertItemsBean.eigDesc", eig.getDescription());
        UIMessage.make(form, "description-label", "modifyexpertitem.description.label");

        UICommand.make(form, "save-expertitem-button", UIMessage.make("modifyexpertitem.save"), "expertItemsBean.controlExpertItem");
        UIInternalLink.make(form, "cancel-link", UIMessage.make("modifyexpertitem.cancel"), new SimpleViewParameters(ControlExpertItemsProducer.VIEW_ID));
    }

    public ViewParameters getViewParameters() {
        return new ModifyExpertItemParameters();
    }

    @SuppressWarnings("unchecked")
    public List reportNavigationCases() {
        List cases = new ArrayList();
        cases.add(new NavigationCase(null, new SimpleViewParameters(ControlExpertItemsProducer.VIEW_ID)));
        return cases;
    }

}