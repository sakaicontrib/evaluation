/******************************************************************************
 * ExpertObjectiveProducer.java - created by aaronz on 9 Mar 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
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

import org.sakaiproject.evaluation.logic.EvalExpertItemsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.tool.params.ExpertItemViewParameters;
import org.sakaiproject.evaluation.tool.params.TemplateViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
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
public class ExpertObjectiveProducer implements ViewComponentProducer, ViewParamsReporter {

	/**
	 * Used for navigation within the system, this must match with the template name
	 */
	public static final String VIEW_ID = "choose_expert_objective"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}

	// Spring injection
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private EvalExpertItemsLogic expertItemsLogic;
	public void setExpertItemsLogic(EvalExpertItemsLogic expertItemsLogic) {
		this.expertItemsLogic = expertItemsLogic;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		String currentUserId = external.getCurrentUserId();

		ExpertItemViewParameters expertItemViewParameters = (ExpertItemViewParameters) viewparams;
		Long templateId = expertItemViewParameters.templateId;
		Long categoryId = expertItemViewParameters.categoryId;

		UIMessage.make(tofill, "page-title", "expert.objective.page.title"); //$NON-NLS-1$ //$NON-NLS-2$

		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));
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
		EvalItemGroup category = expertItemsLogic.getItemGroupById(categoryId);
		UIOutput.make(tofill, "category-current", category.getTitle() );
		UIMessage.make(tofill, "objective", "expert.objective");
		UIMessage.make(tofill, "objective-instructions", "expert.objective.instructions");

		UIMessage.make(tofill, "description", "expert.description");

		// set the VP to the correct target for items
		eivp.viewID = ExpertItemsProducer.VIEW_ID;

		// loop through all non-empty expert objectives
		List expertObjectives = expertItemsLogic.getItemGroups(categoryId, currentUserId, false, true);
		for (int i = 0; i < expertObjectives.size(); i++) {
			EvalItemGroup objective = (EvalItemGroup) expertObjectives.get(i);
			UIBranchContainer objectives = UIBranchContainer.make(tofill, "expert-objective-list:", objective.getId().toString());
			eivp.objectiveId = objective.getId();
			UIInternalLink.make(objectives, "objective-title-link", objective.getTitle(), eivp); //$NON-NLS-1$
			if (objective.getDescription() != null && objective.getDescription().length() > 0) {
				UIOutput.make(objectives, "objective-description", objective.getDescription()); //$NON-NLS-1$
			} else {
				UIMessage.make(objectives, "objective-no-description", "expert.no.description"); //$NON-NLS-1$
			}
		}

		// create the cancel link
		UIInternalLink.make(tofill, "cancel-expert-items", UIMessage.make("expert.items.cancel"), 
				new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, templateId) );
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters() {
		return new ExpertItemViewParameters();
	}

}
