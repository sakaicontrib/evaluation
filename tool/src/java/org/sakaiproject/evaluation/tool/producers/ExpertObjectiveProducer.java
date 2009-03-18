/******************************************************************************
 * ExpertObjectiveProducer.java - created by aaronz on 9 Mar 2007
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
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handles the expert objectives view
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ExpertObjectiveProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

    /**
     * Used for navigation within the system, this must match with the template name
     */
    public static final String VIEW_ID = "choose_expert_objective"; //$NON-NLS-1$
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean createTemplate = authoringService.canCreateTemplate(currentUserId);
        boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);

        /*
         * top links here
         */
        UIInternalLink.make(tofill, "summary-link", 
                UIMessage.make("summary.page.title"), 
                new SimpleViewParameters(SummaryProducer.VIEW_ID));

        if (userAdmin) {
            UIInternalLink.make(tofill, "administrate-link", 
                    UIMessage.make("administrate.page.title"),
                    new SimpleViewParameters(AdministrateProducer.VIEW_ID));
            UIInternalLink.make(tofill, "control-scales-link",
                    UIMessage.make("controlscales.page.title"),
                    new SimpleViewParameters(ControlScalesProducer.VIEW_ID));
        }

        if (createTemplate) {
            UIInternalLink.make(tofill, "control-templates-link",
                    UIMessage.make("controltemplates.page.title"), 
                    new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
            if (!((Boolean) evalSettings.get(EvalSettings.DISABLE_ITEM_BANK))) {
                UIInternalLink.make(tofill, "control-items-link",
                        UIMessage.make("controlitems.page.title"), 
                        new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
            }
        } else {
            throw new SecurityException("User attempted to access " + 
                    VIEW_ID + " when they are not allowed");
        }

        if (beginEvaluation) {
            UIInternalLink.make(tofill, "control-evaluations-link",
                    UIMessage.make("controlevaluations.page.title"),
                    new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
        }

        ExpertItemViewParameters expertItemViewParameters = (ExpertItemViewParameters) viewparams;
        Long templateId = expertItemViewParameters.templateId;
        Long categoryId = expertItemViewParameters.categoryId;

        UIMessage.make(tofill, "page-title", "expert.objective.page.title"); //$NON-NLS-1$ //$NON-NLS-2$

        UIInternalLink.make(tofill, "modify-template", UIMessage.make("modifytemplate.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
                new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, templateId) );

        // create a copy of the VP and then set it to the right view (to avoid corrupting the original)
        ExpertItemViewParameters eivp = (ExpertItemViewParameters) expertItemViewParameters.copyBase();
        eivp.viewID = ExpertCategoryProducer.VIEW_ID;

        UIMessage.make(tofill, "expert-items", "expert.expert.items");
        UIInternalLink.make(tofill, "expert-items-category-link", UIMessage.make("expert.category"), eivp ); //$NON-NLS-1$ //$NON-NLS-2$
        UIMessage.make(tofill, "expert-items-objective", "expert.objective");

        UIInternalLink.make(tofill, "choose-category-1-link", UIMessage.make("expert.choose.category"), eivp ); //$NON-NLS-1$ //$NON-NLS-2$
        UIMessage.make(tofill, "choose-objective-2", "expert.choose.objective");
        UIMessage.make(tofill, "choose-items-3", "expert.choose.items");

        UIMessage.make(tofill, "category", "expert.category");
        EvalItemGroup category = authoringService.getItemGroupById(categoryId);
        UIOutput.make(tofill, "category-current", category.getTitle() );

        // set the VP to the correct target for items
        eivp.viewID = ExpertItemsProducer.VIEW_ID;

        // loop through all non-empty expert objectives
        List<EvalItemGroup> expertObjectives = authoringService.getItemGroups(categoryId, currentUserId, false, true);
        if (expertObjectives.size() > 0) {
            UIBranchContainer.make(tofill, "objective-header:");

            UIMessage.make(tofill, "objective", "expert.objective");
            UIMessage.make(tofill, "objective-instructions", "expert.objective.instructions");
            UIMessage.make(tofill, "objective-list-summary", "expert.objective.summary");

            UIMessage.make(tofill, "description", "expert.description");

            for (int i = 0; i < expertObjectives.size(); i++) {
                EvalItemGroup objective = (EvalItemGroup) expertObjectives.get(i);
                UIBranchContainer objectives = UIBranchContainer.make(tofill, "expert-objective-list:", objective.getId().toString());
                if (i % 2 == 0) {
                    objectives.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
                }

                eivp.objectiveId = objective.getId();
                UIInternalLink.make(objectives, "objective-title-link", objective.getTitle(), eivp); //$NON-NLS-1$
                if (objective.getDescription() != null && objective.getDescription().length() > 0) {
                    UIVerbatim.make(objectives, "objective-description", objective.getDescription()); //$NON-NLS-1$
                } else {
                    UIMessage.make(objectives, "objective-no-description", "expert.no.description"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }

        List<EvalItem> expertItems = authoringService.getItemsInItemGroup(categoryId, true);

        if (expertObjectives.size() > 0 && expertItems.size() > 0) {
            UIBranchContainer branch = UIBranchContainer.make(tofill, "objective-or-choice:");
            UIMessage.make(branch, "objective-or-text", "expert.objective.or"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (expertItems.size() > 0) {
            UIBranchContainer.make(tofill, "items-header:");

            UIBranchContainer formBranch = UIBranchContainer.make(tofill, "form-branch:");
            UIForm form = UIForm.make(formBranch, "insert-items-form");

            UIMessage.make(form, "items", "expert.items");
            UIMessage.make(form, "items-instructions", "expert.items.instructions");
            UIMessage.make(form, "item-item-list-summary", "expert.items.summary");
            // loop through all expert items
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

            // create the cancel link
            UIMessage.make(tofill, "cancel-button", "expert.items.cancel");

            // create the Insert Items button
            UICommand addItemsCommand = UICommand.make(form, "insert-items-command", UIMessage.make("expert.items.insert"), //$NON-NLS-1$
            "#{expertItemsBean.processActionAddItems}"); //$NON-NLS-1$
            addItemsCommand.parameters.add(new UIELBinding("#{expertItemsBean.templateId}", templateId)); //$NON-NLS-1$
        } else {
            // create the cancel link
            UIMessage.make(tofill, "cancel-button", "expert.items.cancel");
        }
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
