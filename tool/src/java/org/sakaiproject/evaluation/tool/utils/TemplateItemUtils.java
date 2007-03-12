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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		} else if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(templateItem.getItem().getClassification())) {
			return EvalConstants.ITEM_TYPE_BLOCK_PARENT;
		} else if (EvalConstants.ITEM_TYPE_SCALED.equals(templateItem.getItem().getClassification())) {
			// scaled has a special case where it might be a block so check for this and handle it correctly
			if (templateItem.getBlockParent() != null) {
				// item is part of a block so must be a child
				return EvalConstants.ITEM_TYPE_BLOCK_CHILD;
			} else {
				// item is a normal scaled item
				return EvalConstants.ITEM_TYPE_SCALED;
			}
		} else {
			throw new IllegalStateException("Cannot identify this item classification:" + templateItem.getItem().getClassification());
		}
	}

	/**
	 * Check a list of {@link EvalTemplateItem} objects for a specific category
	 * 
	 * @param itemTypeConstant and ITEM_CATEGORY constant from {@link EvalConstants}
	 * @param itemList a list of {@link EvalTemplateItem} objects
	 * @return true if there is a templateItem in the list that matches the provided category
	 */
	public static boolean checkTemplateItemsCategoryExists(String itemTypeConstant, List itemList) {

		for (int i = 0; i < itemList.size(); i++) {
			EvalTemplateItem templateItem = (EvalTemplateItem) itemList.get(i);
			if ( itemTypeConstant.equals( templateItem.getItemCategory() ) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Reorder a list of templateItems to be in the correct displayOrder,
	 * this does not change the displayOrder values, it simply places everything in the
	 * correct order in the returned list
	 * 
	 * @param templateItemsList a List of {@link EvalTemplateItem} objects from a template
	 * @return a List of {@link EvalTemplateItem} objects
	 */
	public static List orderTemplateItems(List templateItemsList) {
		List orderedItemsList = new ArrayList();

		List nonChildrenItems = getNonChildItems(templateItemsList);
		for (int i=0; i<nonChildrenItems.size(); i++) {
			EvalTemplateItem templateItem = (EvalTemplateItem) nonChildrenItems.get(i);
			String type = getTemplateItemType(templateItem);
			orderedItemsList.add(templateItem);
			if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type)) {
				List childrenItems = getChildItems(templateItemsList, templateItem.getId());
				for (int j=0; j<childrenItems.size(); j++) {
					EvalTemplateItem childItem = (EvalTemplateItem) childrenItems.get(i);
					orderedItemsList.add(childItem);
				}
			}
		}

		return orderedItemsList;
	}


	/**
	 * Return a list of answerable items only in the correct order,
	 * does not include block parents or header items or any item that
	 * cannot be answered
	 * 
	 * @param tempItemsList a List of {@link EvalTemplateItem} objects from a template
	 * @return a List of {@link EvalTemplateItem} objects
	 */
	public static List getAnswerableTemplateItems(List templateItemsList) {		
		List answerableItemsList = new ArrayList();

		List orderedItems = orderTemplateItems(templateItemsList);

		for (int i=0; i<orderedItems.size(); i++) {
			EvalTemplateItem templateItem = (EvalTemplateItem) orderedItems.get(i);
			String type = getTemplateItemType(templateItem);
			if (EvalConstants.ITEM_TYPE_HEADER.equals(type)) {
				continue;
			}
			if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type)) {
				continue;
			}
			answerableItemsList.add(templateItem);
		}

		return answerableItemsList;
	}

	// BLOCKS

	/**
	 * filter out the Block child items, and only return non-child items, return then
	 * in correctly sorted display order
	 * 
	 * @param tempItemsList a List of {@link EvalTemplateItem} objects in a template
	 * @return a List of {@link EvalTemplateItem} objects without any block child objects
	 */
	public static List getNonChildItems(List templateItemsList) {
		List nonChildItemsList = new ArrayList();

		for (int i=0; i<templateItemsList.size(); i++) {
			EvalTemplateItem templateItem = (EvalTemplateItem) templateItemsList.get(i);
			String type = getTemplateItemType(templateItem);
			if (EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(type)) {
				continue;
			}
			nonChildItemsList.add(templateItem);
		}

		// fix the order
		Collections.sort(nonChildItemsList, 
				new ComparatorsUtils.TemplateItemComparatorByOrder() );
		return nonChildItemsList;
	}

	/**
	 * return the child items which are associated with a block parent Id in the correct
	 * display order
	 * 
	 * @param tempItemsList a List of {@link EvalTemplateItem} objects in a template
	 * @param blockParentId a unique identifier for an {@link EvalTemplateItem} which is a block parent
	 * @return a List of {@link EvalTemplateItem} objects or empty if none found
	 */
	public static List getChildItems(List templateItemsList, Long blockParentId) {
		List childItemsList = new ArrayList();

		for (int i=0; i<templateItemsList.size(); i++) {
			EvalTemplateItem templateItem = (EvalTemplateItem) templateItemsList.get(i);
			String type = getTemplateItemType(templateItem);
			if (EvalConstants.ITEM_TYPE_BLOCK_CHILD.equals(type)) {
				if ( blockParentId.equals(templateItem.getBlockId()) ) {
					childItemsList.add(templateItem);
				}
			}
		}

		// fix the order
		Collections.sort(childItemsList, 
				new ComparatorsUtils.TemplateItemComparatorByOrder() );
		return childItemsList;
	}

}
