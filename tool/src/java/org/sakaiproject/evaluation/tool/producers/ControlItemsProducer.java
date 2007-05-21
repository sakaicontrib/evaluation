/******************************************************************************
 * ControlItemsProducer.java - created by aaronz on 20 May 2007
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

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.tool.renderers.AddItemControlRenderer;
import org.sakaiproject.evaluation.tool.viewparams.PreviewItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This lists items for users so they can add, modify, remove them
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ControlItemsProducer implements ViewComponentProducer {

	public static String VIEW_ID = "control_items";
	public String getViewID() {
		return VIEW_ID;
	}


	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}
	
	private EvalEvaluationsLogic evaluationsLogic;
	public void setEvaluationsLogic(EvalEvaluationsLogic evaluationsLogic) {
		this.evaluationsLogic = evaluationsLogic;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic(EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	private AddItemControlRenderer addItemControlRenderer;
	public void setAddItemControlRenderer(AddItemControlRenderer addItemControlRenderer) {
		this.addItemControlRenderer = addItemControlRenderer;
	}

	private Locale locale;
	public void setLocale(Locale locale) {
		this.locale = locale;
	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		// local variables used in the render logic
		String currentUserId = external.getCurrentUserId();
		boolean userAdmin = external.isUserAdmin(currentUserId);
		boolean createTemplate = templatesLogic.canCreateTemplate(currentUserId);
		boolean beginEvaluation = evaluationsLogic.canBeginEvaluation(currentUserId);
		// use a date which is related to the current users locale
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

		// page title
		UIMessage.make(tofill, "page-title", "controlitems.page.title");

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
		}

		if (createTemplate) {
			UIInternalLink.make(tofill, "control-templates-link", 
					UIMessage.make("controltemplates.page.title"), 
				new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
		}

		if (beginEvaluation) {
			UIInternalLink.make(tofill, "control-evaluations-link", 
					UIMessage.make("controlevaluations.page.title"), 
				new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
			UIInternalLink.make(tofill, "begin-evaluation-link", 
					UIMessage.make("beginevaluation.page.title"), 
				new TemplateViewParameters(EvaluationStartProducer.VIEW_ID, null));
		}

		UIMessage.make(tofill, "items-header", "controlitems.items.header");
		UIMessage.make(tofill, "items-description", "controlitems.items.description");

		UIMessage.make(tofill, "add-item-header", "controlitems.items.add");
		String[] viewIDs = { 
				ModifyScaledProducer.VIEW_ID,
				ModifyEssayProducer.VIEW_ID, 
				ModifyHeaderProducer.VIEW_ID
			};
		String[] labels = new String[] {
				"modifytemplate.itemtype.scaled", 
				"modifytemplate.itemtype.text",
				"modifytemplate.itemtype.header"
			};
		addItemControlRenderer.renderControl(tofill, "add-item-control:", viewIDs, labels, 
				UIMessage.make("modifytemplate.add.item.button"), null);

		// get items for the current user
		List userItems = itemsLogic.getItemsForUser(currentUserId, null, null, false);
		if (userItems.size() > 0) {
			UIBranchContainer itemListing = UIBranchContainer.make(tofill, "item-listing:");

			for (int i = 0; i < userItems.size(); i++) {
				EvalItem item = (EvalItem) userItems.get(i);
				UIBranchContainer itemBranch = UIBranchContainer.make(itemListing, "item-row:", item.getId().toString());
				if (i % 2 == 0) {
					itemBranch.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
				}

				UIOutput.make(itemBranch, "item-classification", item.getClassification());

				if (item.getScaleDisplaySetting() != null) {
					String scaleDisplaySettingLabel = " - " + item.getScaleDisplaySetting();
					UIOutput.make(itemBranch, "item-scale", scaleDisplaySettingLabel);
				}

				UIInternalLink.make(itemBranch, "item-preview-link", UIMessage.make("controlitems.preview.link"), 
						new PreviewItemViewParameters(PreviewItemProducer.VIEW_ID, item.getId(), null) );

				UIOutput.make(itemBranch, "item-owner", external.getUserDisplayName( item.getOwner()) );
				UIOutput.make(itemBranch, "item-text", item.getItemText());

				if ( itemsLogic.canControlItem(currentUserId, item.getId()) ) {
                	// item controllable
					// TODO - make this link work when we have somewhere for it to go
					UIInternalLink.make(itemBranch, "item-remove-link", UIMessage.make("controlitems.remove.link"), 
							new SimpleViewParameters(SummaryProducer.VIEW_ID));
					// TODO - make this link work when we have somewhere for it to go
					UIInternalLink.make(itemBranch, "item-modify-link", UIMessage.make("controlitems.modify.link"), 
							new SimpleViewParameters(SummaryProducer.VIEW_ID));
				} else {
                	// item not controllable
					UIMessage.make(itemBranch, "item-remove-dummy", "controlitems.remove.link");
					UIMessage.make(itemBranch, "item-modify-dummy", "controlitems.modify.link");
				}
			}
		} else {
			UIMessage.make(tofill, "no-items", "controlitems.items.none");
		}
	}

}
