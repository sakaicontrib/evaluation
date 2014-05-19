/******************************************************************************
 * ExpertItemsProducer.java - created by aaronz on 9 Mar 2007
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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;
import org.sakaiproject.evaluation.tool.viewparams.ExpertItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handles the expert items view
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ExpertItemsProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

    /**
     * Used for navigation within the system, this must match with the template name
     */
    public static final String VIEW_ID = "choose_expert_items"; //$NON-NLS-1$
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        ExpertItemViewParameters expertItemViewParameters = (ExpertItemViewParameters) viewparams;
        Long templateId = expertItemViewParameters.templateId;
        Long categoryId = expertItemViewParameters.categoryId;
        Long objectiveId = expertItemViewParameters.objectiveId;

        UIMessage.make(tofill, "page-title", "expert.items.page.title"); //$NON-NLS-1$ //$NON-NLS-2$

        UIInternalLink.make(tofill, "modify-template", UIMessage.make("modifytemplate.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
                new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, templateId) );

        // create a copy of the VP and then set it to the right view (to avoid corrupting the original)
        ExpertItemViewParameters eivp = (ExpertItemViewParameters) expertItemViewParameters.copyBase();
        eivp.viewID = ExpertCategoryProducer.VIEW_ID;

        UIMessage.make(tofill, "expert-items", "expert.expert.items");
        UIInternalLink.make(tofill, "expert-items-category-link", UIMessage.make("expert.category"), eivp ); //$NON-NLS-1$ //$NON-NLS-2$
        UIMessage.make(tofill, "expert-items-items", "expert.items");

        UIInternalLink.make(tofill, "choose-category-1-link", UIMessage.make("expert.choose.category"), eivp ); //$NON-NLS-1$ //$NON-NLS-2$

        eivp.viewID = ExpertObjectiveProducer.VIEW_ID;

        UIInternalLink.make(tofill, "expert-items-objective-link", UIMessage.make("expert.objective"), eivp ); //$NON-NLS-1$ //$NON-NLS-2$

        UIInternalLink.make(tofill, "choose-objective-2-link", UIMessage.make("expert.choose.objective"), eivp ); //$NON-NLS-1$ //$NON-NLS-2$
        UIMessage.make(tofill, "choose-items-3", "expert.choose.items");

        UIMessage.make(tofill, "category", "expert.category");
        EvalItemGroup category = authoringService.getItemGroupById(categoryId);
        UIOutput.make(tofill, "category-current", category.getTitle() );
        UIMessage.make(tofill, "objective", "expert.objective");
        EvalItemGroup objective = authoringService.getItemGroupById(objectiveId);
        UIOutput.make(tofill, "objective-current", objective.getTitle() );
        UIMessage.make(tofill, "items", "expert.items");
        UIMessage.make(tofill, "items-instructions", "expert.items.instructions");
        UIMessage.make(tofill, "expert-items-summary", "expert.items.summary");
        UIMessage.make(tofill, "description", "expert.description");

        UIForm form = UIForm.make(tofill, "insert-items-form");

        // loop through all expert items
        List<EvalItem> expertItems = authoringService.getItemsInItemGroup(objectiveId, true);
        for (int i = 0; i < expertItems.size(); i++) {
            EvalItem expertItem = (EvalItem) expertItems.get(i);
            UIBranchContainer items = UIBranchContainer.make(form, "expert-item-list:", expertItem.getId().toString());
            if (i % 2 == 0) {
                items.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
            }

            UIBoundBoolean checkbox = UIBoundBoolean.make(items, "insert-item-checkbox", "#{expertItemsBean.selectedIds." + expertItem.getId() + "}");
            UILabelTargetDecorator.targetLabel(UIOutput.make(items, "item-label"), checkbox);
            UIVerbatim.make(items, "item-text", expertItem.getItemText()); //$NON-NLS-1$
            if (expertItem.getScale() != null) {
                UIOutput.make(items, "item-scale", ScaledUtils.makeScaleText(expertItem.getScale(), 0)); //$NON-NLS-1$
            }
            if (expertItem.getExpertDescription() != null) {
                UIVerbatim.make(items, "item-expert-desc", expertItem.getExpertDescription()); //$NON-NLS-1$
            }
        }

        // create the cancel button
        UIMessage.make(tofill, "cancel-button", "expert.items.cancel");

        // create the Insert Items button
        UICommand addItemsCommand = UICommand.make(form, "insert-items-command", UIMessage.make("expert.items.insert"), //$NON-NLS-1$
        "#{expertItemsBean.processActionAddItems}"); //$NON-NLS-1$
        addItemsCommand.parameters.add(new UIELBinding("#{expertItemsBean.templateId}", templateId)); //$NON-NLS-1$
    }

    /* 
     * (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
     */
    @SuppressWarnings("unchecked")
    public List reportNavigationCases() {
        List i = new ArrayList();
        i.add(new NavigationCase("success", new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, null) ) );
        return i;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new ExpertItemViewParameters();
    }

}
