/******************************************************************************
 * TemplateItemUtils.java - created by aaronz@vt.edu on Feb 7, 2007
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

package org.sakaiproject.evaluation.tool.utils;

import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * Utilities for dealing with templateItem objects
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TemplateItemUtils {

	/**
	 * Return a constant from {@link EvalConstants} indicating the type of item for
	 * this specific templateItem
	 * 
	 * @param templateItem a templateItem persistent object
	 * @return an ITEM_TYPE string from {@link EvalConstants}
	 */
	public static String getTemplateItemType(EvalTemplateItem templateItem) {
		if (EvalConstants.ITEM_TYPE_HEADER.equals(templateItem.getItem().getClassification())) {
			return EvalConstants.ITEM_TYPE_HEADER;
		} else if (EvalConstants.ITEM_TYPE_TEXT.equals(templateItem.getItem().getClassification())) {
			return EvalConstants.ITEM_TYPE_TEXT;
		} else if (EvalConstants.ITEM_TYPE_SCALED.equals(templateItem.getItem().getClassification())) {
			// scaled has a special case where it might be a block so check for this and handle it correctly
			if (templateItem.getBlockParent() != null) {
				// item is part of a block
				if (templateItem.getBlockParent().booleanValue()) {
					// this is a block parent so handle it a special way
					return EvalConstants.ITEM_TYPE_BLOCK;
				} else {
					// this is a block child so die
					throw new IllegalArgumentException("Cannot render block child items alone, they are rendered with the parent when it gets rendered");
				}
			} else {
				// item is a normal scaled item
				return EvalConstants.ITEM_TYPE_SCALED;
			}
		} else {
			throw new IllegalStateException("Cannot identify this item classification:" + templateItem.getItem().getClassification());
		}
	}

}
