/******************************************************************************
 * PreviewItemProducer.java - created on Aug 21, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 * Rui Feng (fengr@vt.edu)
 * Aaron Zeckoski (aaronz@vt.edu) - project lead
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.params.TemplateItemViewParameters;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.utils.TemplateItemUtils;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handle previewing a single item<br/>
 * Refactored to use the item renderers by AZ
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Rui Feng (fengr@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class PreviewItemProducer implements ViewComponentProducer, ViewParamsReporter {
	public static final String VIEW_ID = "preview_item";
	public String getViewID() {
		return VIEW_ID;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	ItemRenderer itemRenderer;
	public void setItemRenderer(ItemRenderer itemRenderer) {
		this.itemRenderer = itemRenderer;
	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {	
		TemplateItemViewParameters templateItemViewParams = (TemplateItemViewParameters) viewparams;

		Long templateItemId = templateItemViewParams.templateItemId;
		// commented out since they were causing a warning -AZ
		//String templateItemOTPBinding="templateItemBeanLocator."+templateItemId;
		//String templateItemOTP=templateItemOTPBinding+".";			

		EvalTemplateItem templateItem = itemsLogic.getTemplateItemById(templateItemId);
		System.out.println("TEMPLATEITEMID:" + templateItemId + " TEMPLATEITEMTEXT:" + templateItem.getItem().getItemText());
		UIOutput.make(tofill, "modify-template-title","Modify Template");
		// TODO: exception: cannot get property
		//UIOutput.make(tofill, "modify-template-title", messageLocator.getMessage("templatemodify.page.title"));
		UIOutput.make(tofill, "preview-item-title", messageLocator.getMessage("previewitem.page.title"));

		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"), 
				new SimpleViewParameters(SummaryProducer.VIEW_ID));

		String itemTypeConstant = TemplateItemUtils.getTemplateItemType(templateItem);
		if( EvalConstants.ITEM_TYPE_BLOCK.equals(itemTypeConstant) ) {
			// TODO - add something to handle blocks
			throw new IllegalStateException("No code in place to handle block rendering for preview yet");

		} else {
			// use the renderer evolver
			itemRenderer.renderItem(tofill, "previewed-item:", null, templateItem, 0, false);
		}

		UIOutput.make(tofill, "close-button", messageLocator.getMessage("general.close.window.button"));

	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters() {
		return new TemplateItemViewParameters();
	}

}