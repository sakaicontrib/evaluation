/******************************************************************************
 * ExpertCategoryProducer.java - created by aaronz on 8 Mar 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.List;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.ExpertItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handles the expert category view
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ExpertCategoryProducer implements ViewComponentProducer, ViewParamsReporter {

    /**
     * Used for navigation within the system, this must match with the template name
     */
    public static final String VIEW_ID = "choose_expert_category"; //$NON-NLS-1$
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

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();

        /*
         * top links here
         */
        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        ExpertItemViewParameters expertItemViewParameters = (ExpertItemViewParameters) viewparams;
        Long templateId = expertItemViewParameters.templateId;

        UIMessage.make(tofill, "page-title", "expert.category.page.title"); //$NON-NLS-1$ //$NON-NLS-2$

        UIInternalLink.make(tofill, "modify-template", UIMessage.make("modifytemplate.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
                new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, templateId) );

        // create a copy of the VP and then set it to the right view (to avoid corrupting the original)
        ExpertItemViewParameters eivp = (ExpertItemViewParameters) expertItemViewParameters.copyBase();

        UIMessage.make(tofill, "expert-items-category", "expert.category");

        UIMessage.make(tofill, "choose-category-1", "expert.choose.category");
        UIMessage.make(tofill, "choose-objective-2", "expert.choose.objective");
        UIMessage.make(tofill, "choose-items-3", "expert.choose.items");

        UIMessage.make(tofill, "category", "expert.category");
        UIMessage.make(tofill, "category-instructions", "expert.category.instructions");
        UIMessage.make(tofill, "category-list-summary", "expert.category.list.summary");

        UIMessage.make(tofill, "description", "expert.description");

        // set the VP to the correct target for objectives
        eivp.viewID = ExpertObjectiveProducer.VIEW_ID;

        // loop through all non-empty expert categories
        List<EvalItemGroup> expertCategories = authoringService.getItemGroups(null, currentUserId, false, true);
        for (int i = 0; i < expertCategories.size(); i++) {
            EvalItemGroup category = (EvalItemGroup) expertCategories.get(i);
            UIBranchContainer categories = UIBranchContainer.make(tofill, "expert-category-list:", category.getId().toString());
            if (i % 2 == 0) {
                categories.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
            }

            eivp.categoryId = category.getId();
            UIInternalLink.make(categories, "category-title-link", category.getTitle(), eivp); //$NON-NLS-1$
            if (category.getDescription() != null && category.getDescription().length() > 0) {
                UIVerbatim.make(categories, "category-description", category.getDescription()); //$NON-NLS-1$
            } else {
                UIMessage.make(categories, "category-no-description", "expert.no.description"); //$NON-NLS-1$
            }
        }

        // create the cancel button
        UIMessage.make(tofill, "cancel-button", "expert.items.cancel");
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new ExpertItemViewParameters();
    }

}
