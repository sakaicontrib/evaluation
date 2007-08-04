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

package org.sakaiproject.evaluation.logic.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sakaiproject.evaluation.model.EvalItem;
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
	public static boolean checkTemplateItemsCategoryExists(String itemTypeConstant, List<EvalTemplateItem> itemList) {
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
	public static List<EvalTemplateItem> orderTemplateItems(List<EvalTemplateItem> templateItemsList) {
		List<EvalTemplateItem> orderedItemsList = new ArrayList<EvalTemplateItem>();

		List<EvalTemplateItem> nonChildrenItems = getNonChildItems(templateItemsList);
		for (int i=0; i<nonChildrenItems.size(); i++) {
			EvalTemplateItem templateItem = (EvalTemplateItem) nonChildrenItems.get(i);
			String type = getTemplateItemType(templateItem);
			orderedItemsList.add(templateItem);
			if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type)) {
				List<EvalTemplateItem> childrenItems = getChildItems(templateItemsList, templateItem.getId());
				for (int j=0; j<childrenItems.size(); j++) {
					EvalTemplateItem childItem = (EvalTemplateItem) childrenItems.get(j);
					orderedItemsList.add(childItem);
				}
			}
		}

		// need to have a special case here which will keep this method 
		// from losing child items when the parent is not included
		if (orderedItemsList.size() < templateItemsList.size()) {
		   for (EvalTemplateItem templateItem : templateItemsList) {
            if (! orderedItemsList.contains(templateItem)) {
               orderedItemsList.add(templateItem);
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
	 * @param templateItemsList a List of {@link EvalTemplateItem} objects from a template
	 * @return a List of {@link EvalTemplateItem} objects
	 */
	public static List<EvalTemplateItem> getAnswerableTemplateItems(List<EvalTemplateItem> templateItemsList) {		
		List<EvalTemplateItem> answerableItemsList = new ArrayList<EvalTemplateItem>();

		List<EvalTemplateItem> orderedItems = orderTemplateItems(templateItemsList);

		for (int i=0; i<orderedItems.size(); i++) {
			EvalTemplateItem templateItem = (EvalTemplateItem) orderedItems.get(i);
			String type = getTemplateItemType(templateItem);
            if (EvalConstants.ITEM_TYPE_HEADER.equals(type) ||
                    EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type)) {
                continue;
            }
			answerableItemsList.add(templateItem);
		}

		return answerableItemsList;
	}

    /**
     * Get the list of all templateItems which are required (must be answered),
     * this will include any scaled items or items which are part of a block (but not a block parent)
     * @param templateItemsList a List of {@link EvalTemplateItem} objects from a template
     * @return a List of {@link EvalTemplateItem} objects
     */
    public static List<EvalTemplateItem> getRequiredTemplateItems(List<EvalTemplateItem> templateItemsList) {       
        List<EvalTemplateItem> requiredItemsList = new ArrayList<EvalTemplateItem>();

        List<EvalTemplateItem> orderedItems = orderTemplateItems(templateItemsList);

        for (int i=0; i<orderedItems.size(); i++) {
            EvalTemplateItem templateItem = (EvalTemplateItem) orderedItems.get(i);
            String type = getTemplateItemType(templateItem);
            if (EvalConstants.ITEM_TYPE_HEADER.equals(type) ||
                    EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type) ||
                    EvalConstants.ITEM_TYPE_TEXT.equals(type)) {
                continue;
            }
            requiredItemsList.add(templateItem);
        }

        return requiredItemsList;
    }

	// BLOCKS

	/**
	 * filter out the Block child items, and only return non-child items, return then
	 * in correctly sorted display order
	 * 
	 * @param tempItemsList a List of {@link EvalTemplateItem} objects in a template
	 * @return a List of {@link EvalTemplateItem} objects without any block child objects
	 */
	@SuppressWarnings("unchecked")
    public static List<EvalTemplateItem> getNonChildItems(List<EvalTemplateItem> templateItemsList) {
		List<EvalTemplateItem> nonChildItemsList = new ArrayList<EvalTemplateItem>();

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
	@SuppressWarnings("unchecked")
    public static List<EvalTemplateItem> getChildItems(List<EvalTemplateItem> templateItemsList, Long blockParentId) {
		List<EvalTemplateItem> childItemsList = new ArrayList<EvalTemplateItem>();

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

	/**
	 * Creates an {@link EvalTemplateItem} object from an {@link EvalItem} object by inferring
	 * the necessary paramters for previewing or rendering when only an item is available, 
	 * does NOT create a persistent object<br/>
	 * NOTE: template is set to null
	 * 
	 * @param item any item object (could be persistent)
	 * @return a non-persistent template item object
	 */
	public static EvalTemplateItem makeTemplateItem(EvalItem item) {
		if (item == null) {
			throw new IllegalArgumentException("Cannot create template item from null item");
		}

		EvalTemplateItem templateItem = new EvalTemplateItem(item.getLastModified(), item.getOwner(), null, item,
				new Integer(1), item.getCategory(), EvalConstants.HIERARCHY_LEVEL_TOP, EvalConstants.HIERARCHY_NODE_ID_TOP);

        if (item.getUsesNA() == null) {
            templateItem.setUsesNA(Boolean.TRUE);
        } else {
            templateItem.setUsesNA(item.getUsesNA());
        }

        if ( EvalConstants.ITEM_TYPE_SCALED.equals(item.getClassification()) ) {
            if (item.getScaleDisplaySetting() == null) {
                templateItem.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED);
            } else {
                templateItem.setScaleDisplaySetting(item.getScaleDisplaySetting());
            }
		} else if ( EvalConstants.ITEM_TYPE_TEXT.equals(item.getClassification()) ) {
            if (item.getDisplayRows() == null) {
                templateItem.setDisplayRows(new Integer(3));
            } else {
                templateItem.setDisplayRows(item.getDisplayRows());
            }
		}
		return templateItem;
	}

}
