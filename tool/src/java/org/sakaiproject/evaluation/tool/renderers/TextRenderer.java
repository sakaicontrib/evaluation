/******************************************************************************
 * TextRenderer.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.renderers;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;

/**
 * This handles the rendering of text type items
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TextRenderer implements ItemRenderer {

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	/**
	 * This identifies the template component associated with this renderer
	 */
	public static final String COMPONENT_ID = "render-text-item:";

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#renderItem(uk.org.ponder.rsf.components.UIContainer, java.lang.String, org.sakaiproject.evaluation.model.EvalTemplateItem, int, boolean)
	 */
	public UIJointContainer renderItem(UIContainer parent, String ID, String binding, EvalTemplateItem templateItem, int displayNumber, boolean disabled) {
		UIJointContainer container = new UIJointContainer(parent, ID, COMPONENT_ID);

		UIOutput.make(container, "itemNum", displayNumber>0?displayNumber+"":"0" ); //$NON-NLS-1$
		UIOutput.make(container, "itemText", templateItem.getItem().getItemText()); //$NON-NLS-1$
		if ( templateItem.getUsesNA().booleanValue() ) {
			UIBranchContainer NAbranch = UIBranchContainer.make(container, "showNA:"); //$NON-NLS-1$
			UIBoundBoolean.make(NAbranch, "itemNA", templateItem.getUsesNA()); //$NON-NLS-1$
			UIOutput.make(NAbranch, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		}

// Do this stuff outside of this render block in the producer -AZ
//		// Bind item id to list of items in evaluation bean.
//		form.parameters.add( new UIELBinding
//				(currAnswerOTP + "item",new ELReference(templateItemOTP+"item")) );				
//		totalItemsAdded++;

		UIInput textarea = UIInput.make(container, "essayBox", binding); //$NON-NLS-1$ //$NON-NLS-2$

		Map attrmap = new HashMap();
		attrmap.put("rows", templateItem.getDisplayRows().toString()); //$NON-NLS-1$
		if (disabled) {
			attrmap.put("disabled", "true"); //$NON-NLS-1$ //$NON-NLS-2$		
		}
		textarea.decorators = new DecoratorList( new UIFreeAttributeDecorator(attrmap) );

		return container;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#getRenderType()
	 */
	public String getRenderType() {
		return EvalConstants.ITEM_TYPE_TEXT;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#getRenderedBlockChildItemIds()
	 */
	public Long[] getRenderedBlockChildItemIds() {
		return new Long[] {};
	}

}
