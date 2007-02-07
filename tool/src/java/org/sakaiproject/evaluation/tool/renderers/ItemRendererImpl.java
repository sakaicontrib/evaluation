/******************************************************************************
 * ItemRendererImpl.java - created by aaronz@vt.edu
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

import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIJointContainer;

/**
 * The implementation for the ItemRenderer class
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ItemRendererImpl implements ItemRenderer {

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.tool.renderers.ItemRenderer#renderItem(org.sakaiproject.evaluation.model.EvalTemplateItem, uk.org.ponder.rsf.components.UIContainer, java.lang.Integer, boolean)
	 */
	public UIJointContainer renderItem(EvalTemplateItem templateItem, UIContainer tofill, Integer displayNumber, boolean disabled) {
		// figure out the type of item and then call the appropriate renderer

		if (EvalConstants.ITEM_TYPE_HEADER.equals(templateItem.getItem().getClassification())) {
			
		} else if (EvalConstants.ITEM_TYPE_TEXT.equals(templateItem.getItem().getClassification())) {
			
		} else if (EvalConstants.ITEM_TYPE_SCALED.equals(templateItem.getItem().getClassification())) {
			// scaled has a special case where it might be a block so check for this and handle it correctly
			if (templateItem.getBlockParent() != null) {
				// item is part of a block
				if (templateItem.getBlockParent().booleanValue()) {
					// this is a block parent so handle it a special way
				} else {
					// this is a block child so die
					throw new IllegalArgumentException("Cannot render block child items alone, they are rendered with the parent when it gets rendered");
				}
			} else {
				// item is a normal scaled item
			}
		} else {
			throw new IllegalStateException("Cannot identify this item classification:" + templateItem.getItem().getClassification());
		}

		// TODO Auto-generated method stub
		return null;
	}

}
