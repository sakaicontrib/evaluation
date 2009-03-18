/******************************************************************************
 * ExistingItemsProducer.java - created by aaronz on 12 Mar 2007
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
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.tool.viewparams.ChooseItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
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
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handles the choose existing items view
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ExistingItemsProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

    /**
     * Used for navigation within the system, this must match with the template name
     */
    public static final String VIEW_ID = "choose_existing_items";
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


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();

        ChooseItemViewParameters itemViewParameters = (ChooseItemViewParameters) viewparams;
        Long templateId = itemViewParameters.templateId;
        String searchString = itemViewParameters.searchString;
        if (searchString == null) searchString = "";

        UIInternalLink.make(tofill, "modify-items-link", UIMessage.make("items.page.title"), //$NON-NLS-2$
                new SimpleViewParameters(ControlItemsProducer.VIEW_ID));

        UIInternalLink.make(tofill, "modify-template-link", UIMessage.make("modifytemplate.page.title"), //$NON-NLS-2$
                new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, templateId) );

        if (searchString.length() > 0) {
            UIMessage.make(tofill, "search-current", "existing.items");
            UIOutput.make(tofill, "search-current-value", searchString);
        }

        // loop through all existing items
        List<EvalItem> existingItems = authoringService.getItemsForUser(currentUserId, null, null, false);
        UIForm form = UIForm.make(tofill, "insert-items-form");

        if (existingItems.size() > 0) {
            UIForm searchForm = UIForm.make(tofill, "search-form", itemViewParameters);
            UIMessage.make(searchForm, "search-command", "items.search.command" );
            UIInput.make(searchForm, "search-box", "#{searchString}");

            for (int i = 0; i < existingItems.size(); i++) {
                EvalItem item = (EvalItem) existingItems.get(i);
                UIBranchContainer items = UIBranchContainer.make(form, "item-list:", item.getId().toString());
                if (i % 2 == 0) {
                    items.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
                }

                UIBoundBoolean checkbox = UIBoundBoolean.make(items, "insert-item-checkbox", "#{expertItemsBean.selectedIds." + item.getId() + "}");
                UILabelTargetDecorator.targetLabel(UIOutput.make(items, "item-label"), checkbox);
                UIVerbatim.make(items, "item-text", item.getItemText());
                if (item.getScale() != null) {
                    String scaleText = item.getScale().getTitle() + " (";
                    for (int j = 0; j < item.getScale().getOptions().length; j++) {
                        scaleText += (j==0?"":",") + item.getScale().getOptions()[j];
                    }
                    scaleText += ")";
                    UIOutput.make(items, "item-scale", scaleText);
                } else {
                    UIOutput.make(items, "item-scale", item.getClassification());
                }
                if (item.getDescription() != null) {
                    UIOutput.make(items, "item-desc", item.getExpertDescription());
                }
            }

            // create the Insert Items button
            UICommand addItemsCommand = UICommand.make(form, "insert-items-command", UIMessage.make("expert.items.insert"),
            "#{expertItemsBean.processActionAddItems}");
            addItemsCommand.parameters.add(new UIELBinding("#{expertItemsBean.templateId}", templateId));

            // create the top cancel link
            UIInternalLink.make(form, "cancel-items", UIMessage.make("items.cancel"), 
                    new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, templateId) );
        } else {
            UIMessage.make(form, "no-items", "no.list.items");
        }

        // create the bottom cancel link
        UIInternalLink.make(form, "cancel-items-bottom", UIMessage.make("items.cancel"), 
                new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, templateId) );
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
        return new ChooseItemViewParameters();
    }

}
