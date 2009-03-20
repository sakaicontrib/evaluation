/**
 * $Id$
 * $URL$
 * TemplateItemUtils.java - evaluation - Feb 07, 2007 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

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
        } else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(templateItem.getItem().getClassification())) {
            return EvalConstants.ITEM_TYPE_MULTIPLECHOICE;
        } else if (EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(templateItem.getItem().getClassification())) {
            return EvalConstants.ITEM_TYPE_MULTIPLEANSWER;
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
     * Create an ordered list or templateItems (by displayOrder) from any collection of template items
     * 
     * @param templateItemsCollection any collection of {@link EvalTemplateItem}
     * @return a list of {@link EvalTemplateItem} in order by display order
     */
    public static List<EvalTemplateItem> makeTemplateItemsList(Collection<EvalTemplateItem> templateItemsCollection) {
        List<EvalTemplateItem> templateItemsList = new ArrayList<EvalTemplateItem>();

        for (EvalTemplateItem templateItem : templateItemsCollection) {
            templateItemsList.add(templateItem);
        }

        return orderTemplateItems(templateItemsList, false);
    }

    /**
     * Create an array of all the template item ids in a collection
     * 
     * @param templateItemsCollection any collection of {@link EvalTemplateItem}
     * @return an array in the order of the collection iterator
     */
    public static Long[] makeTemplateItemsIdsArray(Collection<EvalTemplateItem> templateItemsCollection) {
        List<Long> templateItemsIds = new ArrayList<Long>();

        for (EvalTemplateItem templateItem : templateItemsCollection) {
            templateItemsIds.add(templateItem.getId());
        }

        Long[] ids = templateItemsIds.toArray(new Long[] {});
        return ids;
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
            if ( itemTypeConstant.equals( templateItem.getCategory() ) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the list of {@link EvalTemplateItem} objects for a specific category,
     * guaranteed to return the items in the correct template item order
     * 
     * @param itemTypeConstant and ITEM_CATEGORY constant from {@link EvalConstants}
     * @param itemList a list of {@link EvalTemplateItem} objects
     * @return a list of {@link EvalTemplateItem} objects which have a specific category
     */
    public static List<EvalTemplateItem> getCategoryTemplateItems(String itemTypeConstant, List<EvalTemplateItem> templateItemsList) {    
        List<EvalTemplateItem> catItemsList = new ArrayList<EvalTemplateItem>();

        List<EvalTemplateItem> orderedItems = orderTemplateItems(templateItemsList, false);

        for (int i=0; i<orderedItems.size(); i++) {
            EvalTemplateItem templateItem = (EvalTemplateItem) orderedItems.get(i);
            if ( itemTypeConstant.equals( templateItem.getCategory() ) ) {
                catItemsList.add(templateItem);
            }
        }

        return catItemsList;
    }


    /**
     * Reorder a list of templateItems to be in the correct displayOrder,
     * this does not change the displayOrder values unless fixOrder is true, 
     * it simply places everything in the correct order in the returned list
     * 
     * @param templateItemsList a List of {@link EvalTemplateItem} objects from a template
     * @param fixOrder if true then this will correct the displayOrder (but not save it),
     * otherwise the templateItems are placed in the correct order in the list but displayOrder is not changed<br/>
     * <b>WARNING:</b> This MUST be the complete list of all templateItems in this template or this will corrupt the ordering badly!
     * @return a List of {@link EvalTemplateItem} objects
     */
    public static List<EvalTemplateItem> orderTemplateItems(List<EvalTemplateItem> templateItemsList, boolean fixOrder) {
        List<EvalTemplateItem> orderedItemsList = new ArrayList<EvalTemplateItem>();

        // get the ordered list of all non-children
        List<EvalTemplateItem> nonChildrenItems = getNonChildItems(templateItemsList);
        for (int i=0; i<nonChildrenItems.size(); i++) {
            EvalTemplateItem templateItem = (EvalTemplateItem) nonChildrenItems.get(i);
            String type = getTemplateItemType(templateItem);
            if (fixOrder) {
                templateItem.setDisplayOrder(i + 1);
            }
            orderedItemsList.add(templateItem);
            if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type)) {
                // get the ordered list of all non-children
                List<EvalTemplateItem> childrenItems = getChildItems(templateItemsList, templateItem.getId());
                for (int j=0; j<childrenItems.size(); j++) {
                    EvalTemplateItem childItem = (EvalTemplateItem) childrenItems.get(j);
                    if (fixOrder) {
                        childItem.setDisplayOrder(j + 1);
                    }
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
     * Check if templateItem is answerable (can be answered by a user taking an evaluation) or not
     * <b>NOTE</b> use {@link #getAnswerableTemplateItems(List)} to do a large set
     * @param templateItem a templateItem persistent object
     * @return true if the item is answerable, false otherwise
     */
    public static boolean isAnswerable(EvalTemplateItem templateItem) {
        boolean result = false;
        String type = getTemplateItemType(templateItem);
        if (EvalConstants.ITEM_TYPE_HEADER.equals(type) ||
                EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(type)) {
            result = false;
        } else {
            result = true;
        }
        return result;
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

        List<EvalTemplateItem> orderedItems = orderTemplateItems(templateItemsList, false);

        for (int i=0; i<orderedItems.size(); i++) {
            EvalTemplateItem templateItem = (EvalTemplateItem) orderedItems.get(i);
            if (! isAnswerable(templateItem)) {
                continue;
            }
            answerableItemsList.add(templateItem);
        }

        return answerableItemsList;
    }

    /**
     * Check if a templateItem can be required (must be answered by a user taking an evaluation
     * which does not allow blank responses) or not,
     * all answerable items that are not textual are requireable,
     * this is not checking against the evaluation settings and is only checking
     * the settings for this template item,
     * does not include compulsory check
     * <br/>
     * <b>NOTE</b> use {@link #getRequireableTemplateItems(List)} to do a large set
     * 
     * @param templateItem a templateItem persistent object
     * @return true if the item is requireable, false otherwise
     */
    public static boolean isRequireable(EvalTemplateItem templateItem) {
        // all answerable items that are not textual are requireable
        boolean result = false;
        if (isAnswerable(templateItem)) {
            String type = getTemplateItemType(templateItem);
            if ( EvalConstants.ITEM_TYPE_TEXT.equals(type) ) {
                result = false;
            } else {
                result = true;
            }
        }
        return result;
    }

    /**
     * Get the list of all templateItems which are requireable (must be answered is eval settings stipulate),
     * this will include any scaled items or items which are part of a block (but not a block parent),
     * this is not checking against the evaluation settings and is only checking
     * the settings for this template item
     * 
     * @param templateItemsList a List of {@link EvalTemplateItem} objects from a template
     * @return a List of {@link EvalTemplateItem} objects
     * @see #isRequireable(EvalTemplateItem)
     */
    public static List<EvalTemplateItem> getRequireableTemplateItems(List<EvalTemplateItem> templateItemsList) {       
        List<EvalTemplateItem> requireableItemsList = new ArrayList<EvalTemplateItem>();

        List<EvalTemplateItem> orderedItems = orderTemplateItems(templateItemsList, false);

        for (int i=0; i<orderedItems.size(); i++) {
            EvalTemplateItem templateItem = (EvalTemplateItem) orderedItems.get(i);
            if (! isRequireable(templateItem)) {
                continue;
            }
            requireableItemsList.add(templateItem);
        }
        return requireableItemsList;
    }

    /**
     * Check if a templateItem is compulsory (must be answered by a user taking an evaluation) or not,
     * this is checking the compulsory settings for this template item,
     * all answerable items that are marked as compulsory are included
     * <br/>
     * <b>NOTE</b> use {@link #getCompulsoryTemplateItems(List)} to do a large set
     * 
     * @param templateItem a templateItem persistent object
     * @return true if the item is compulsory, false otherwise
     */
    public static boolean isCompulsory(EvalTemplateItem templateItem) {
        // all answerable items that are not textual are required
        boolean result = false;
        if (isAnswerable(templateItem)) {
            String type = getTemplateItemType(templateItem);
            if ( EvalConstants.ITEM_TYPE_TEXT.equals(type) ) {
                result = false;
            } else if ( safeBool(templateItem.isCompulsory()) ) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Get the list of all templateItems which are compulsory (must be answered),
     * this will include any scaled items or items which are part of a block (but not a block parent)
     * 
     * @param templateItemsList a List of {@link EvalTemplateItem} objects from a template
     * @return a List of {@link EvalTemplateItem} objects
     * @see #isCompulsory(EvalTemplateItem)
     */
    public static List<EvalTemplateItem> getCompulsoryTemplateItems(List<EvalTemplateItem> templateItemsList) {       
        List<EvalTemplateItem> compulsoryItemsList = new ArrayList<EvalTemplateItem>();

        List<EvalTemplateItem> orderedItems = orderTemplateItems(templateItemsList, false);

        for (int i=0; i<orderedItems.size(); i++) {
            EvalTemplateItem templateItem = (EvalTemplateItem) orderedItems.get(i);
            if (! isCompulsory(templateItem)) {
                continue;
            }
            compulsoryItemsList.add(templateItem);
        }
        return compulsoryItemsList;
    }


    // BLOCKS

    /**
     * Check if templateItem is a block parent
     * <b>NOTE</b> use {@link #getChildItems(List, Long)} to get the child items for this block from a larger set
     * @param templateItem a templateItem persistent object
     * @return true if the item is a block parent, false otherwise
     */
    public static boolean isBlockParent(EvalTemplateItem templateItem) {
        // there is something odd here in that there appears to be two ways to identify a block parent
        boolean result = false;
        if ( templateItem.getBlockParent() != null && 
                templateItem.getBlockParent()) {
            result = true;
        } else if (EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals( getTemplateItemType(templateItem) )) {
            result = true;
        }
        return result;
    }

    /**
     * Check if a templateItem is a blockChild
     * <b>NOTE</b> use {@link #getChildItems(List, Long)} to get the child items for this block from a larger set
     * @param templateItem a templateItem persistent object
     * @return true if the item is a block child, false otherwise
     */
    public static boolean isBlockChild(EvalTemplateItem templateItem) {
        boolean result = false;
        if ( templateItem.getBlockParent() != null && 
                templateItem.getBlockParent() == false &&
                templateItem.getBlockId() != null) {
            result = true;
        }
        return result;
    }

    /**
     * filter out the Block child items, and only return non-child items, return them
     * in correctly sorted display order
     * 
     * @param tempItemsList a List of {@link EvalTemplateItem} objects in a template
     * @return a List of {@link EvalTemplateItem} objects without any block child objects
     */
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
     * return all the templateItems from a larger list for a specific nodeId<br/>
     * This is primarily useful for taking all the items in a template and splitting out the ones which are only associated with a specific node
     * 
     * @param templateItemsList a List of {@link EvalTemplateItem} objects from a template
     * @param hierarchyNodeId unique id for a hierarchy node, use null to indicate the top level of the hierarchy
     * @return a List of {@link EvalTemplateItem} objects or empty if none found
     */
    public static List<EvalTemplateItem> getNodeItems(List<EvalTemplateItem> templateItemsList, String hierarchyNodeId) {
        List<EvalTemplateItem> nodeItems = new ArrayList<EvalTemplateItem>();

        for (EvalTemplateItem templateItem : templateItemsList) {
            if (hierarchyNodeId == null) {
                if (templateItem.getHierarchyLevel().equals(EvalConstants.HIERARCHY_LEVEL_TOP)) {
                    nodeItems.add(templateItem);
                }
            } else {
                if ( templateItem.getHierarchyLevel().equals(EvalConstants.HIERARCHY_LEVEL_NODE) 
                        && templateItem.getHierarchyNodeId().equals(hierarchyNodeId) ) {
                    nodeItems.add(templateItem);
                }            
            }
        }

        // fix the order
        Collections.sort(nodeItems, new ComparatorsUtils.TemplateItemComparatorByOrder() );
        return nodeItems;
    }

    /**
     * Make a template item answer key which will uniquely identify the answer for a specific item
     * 
     * @param templateItemId a unique id for an {@link EvalTemplateItem}
     * @param associatedType the type associated with this TI (can be null)
     * @param associatedId the id of the thing associated with this TI (can be null)
     * @return a unique key for the answer to this templateItem
     */
    public static String makeTemplateItemAnswerKey(Long templateItemId, String associatedType, String associatedId) {
        if (templateItemId == null) {
            throw new IllegalArgumentException("templateItemId must be set");
        }
        if (EvalConstants.ITEM_CATEGORY_COURSE.equals(associatedType)) {
            associatedType = null;
            associatedId = null;
        }
        return templateItemId.toString() + "_" + associatedType + "_" + associatedId;
    }

    /**
     * Creates an {@link EvalTemplateItem} object from an {@link EvalItem} object by inferring
     * the necessary parameters for previewing or rendering when only an item is available, 
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

        EvalTemplateItem templateItem = new EvalTemplateItem(item.getOwner(), null, item, new Integer(0),
                item.getCategory(), EvalConstants.HIERARCHY_LEVEL_TOP, EvalConstants.HIERARCHY_NODE_ID_NONE);

        templateItem.setUsesNA(item.getUsesNA() == null ? Boolean.FALSE : item.getUsesNA());

        templateItem.setUsesComment(item.getUsesComment() == null ? Boolean.FALSE : item.getUsesComment());

        if ( EvalConstants.ITEM_TYPE_SCALED.equals(item.getClassification()) ) {
            if (item.getScaleDisplaySetting() == null) {
                templateItem.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED);
            } else {
                templateItem.setScaleDisplaySetting(item.getScaleDisplaySetting());
            }
        } else if ( EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(item.getClassification()) ||
                EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(item.getClassification()) ) {
            if (item.getScaleDisplaySetting() == null) {
                templateItem.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL);
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

    /**
     * Make a non-persistent copy of a persistent templateItem,
     * this is mostly a convenience method to reduce code duplication
     * 
     * @param original the original item to copy
     * @param toTemplate the template to copy this templateItem to
     * @param ownerId set as the owner of this copy
     * @param hidden if true then the resulting copy will be marked as hidden 
     * @return the copy of the templateItem (not persisted)
     */
    public static EvalTemplateItem makeCopyOfTemplateItem(EvalTemplateItem original, EvalTemplate toTemplate,
            String ownerId, boolean hidden) {
        EvalTemplateItem copy = new EvalTemplateItem(ownerId, toTemplate, original.getItem(), original.getDisplayOrder(), original.getCategory(),
                original.getHierarchyLevel(), original.getHierarchyNodeId(), original.getDisplayRows(), original.getScaleDisplaySetting(),
                original.getUsesNA(), original.getUsesComment(), false, null, null, original.getResultsSharing());
        // set the other copy fields correctly
        copy.setCopyOf(original.getId());
        copy.setHidden(hidden);
        copy.setCompulsory(original.isCompulsory());
        return copy;
    }

    /**
     * Validates an item based on its classification
     * @param item an item which is ready to be saved
     * @throws IllegalArgumentException if any parts of the item are set incorrectly
     */
    public static void validateItemByClassification(EvalItem item) {
        // check on ITEM_TYPE and invalid combinations of item values depending on the type
        if ( EvalConstants.ITEM_TYPE_SCALED.equals(item.getClassification()) ||
                EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(item.getClassification()) ||
                EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(item.getClassification()) ) {
            if (item.getScale() == null) {
                throw new IllegalArgumentException("Item scale must be specified for scaled/multiple type items");
            } else if (item.getScaleDisplaySetting() == null) {
                throw new IllegalArgumentException("Item scale display setting must be specified for scaled/multiple type items");
            } else if (item.getDisplayRows() != null) {
                throw new IllegalArgumentException("Item displayRows cannot be included for scaled/multiple type items");
            }
        } else if ( EvalConstants.ITEM_TYPE_TEXT.equals(item.getClassification()) ) {
            if (item.getDisplayRows() == null) {
                throw new IllegalArgumentException("Item display rows must be specified for text type items");
            } else if (item.getScale() != null) {
                throw new IllegalArgumentException("Item scale cannot be included for text type items");
            } else if (item.getScaleDisplaySetting() != null) {
                throw new IllegalArgumentException("Item scale display setting cannot be included for text type items");
            }
        } else if ( EvalConstants.ITEM_TYPE_HEADER.equals(item.getClassification()) ) {
            if (item.getScale() != null) {
                throw new IllegalArgumentException("Item scale cannot be included for header type items");
            } else if (item.getScaleDisplaySetting() != null) {
                throw new IllegalArgumentException("Item scale display setting cannot be included for header type items");
            } else if (item.getDisplayRows() != null) {
                throw new IllegalArgumentException("Item displayRows cannot be included for header type items");
            }
        } else if ( EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(item.getClassification()) ) {
            if (item.getScale() == null) {
                throw new IllegalArgumentException("Item scale must be specified for block parent type items");
            } else if (item.getScaleDisplaySetting() == null) {
                throw new IllegalArgumentException("Item scale display setting must be specified for block parent type items");
            } else if (item.getDisplayRows() != null) {
                throw new IllegalArgumentException("Item displayRows cannot be included for block parent type items");
            }
        } else {
            throw new IllegalArgumentException("Invalid item classification specified ("+item.getClassification()+"), you must use the ITEM_TYPE constants to indicate classification (and cannot use BLOCK_CHILD)");
        }
    }

    /**
     * Validate TemplateItem that is about to be saved
     * @param templateItem a template item with a valid item inside it
     * @throws IllegalArgumentException if any fields are set to invalid values
     */
    public static void validateTemplateItemByClassification(EvalTemplateItem templateItem) {
        EvalItem item = templateItem.getItem();
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null for a templateItem that is about to be saved");
        }
        // check on ITEM_TYPE and invalid combinations of item values depending on the type
        // inherit settings from item if not set correctly here
        if ( EvalConstants.ITEM_TYPE_SCALED.equals(item.getClassification()) ) {
            // check if this scaled item is a block item
            if (templateItem.getBlockParent() == null) {
                // not block item (block parent must be specified)
                // general scaled items checks
                validateItemByClassification(item);
                if (templateItem.getScaleDisplaySetting() == null) {
                    templateItem.setScaleDisplaySetting(item.getScaleDisplaySetting());
                }
                if (templateItem.getDisplayRows() != null) {
                    throw new IllegalArgumentException("Item displayRows must be null for scaled type items");
                }
                if (templateItem.getBlockId() != null) {
                    throw new IllegalArgumentException("Item blockid must be null for scaled type items");
                }
            } else {
                // this is related to a block
                if ( templateItem.getBlockParent() != null ) {
                    // this is a child block item
                    if (templateItem.getBlockId() == null) {
                        throw new IllegalArgumentException("Item blockid must be specified for child block items");
                    }
                    if (templateItem.getDisplayRows() != null) {
                        throw new IllegalArgumentException("Item displayRows must be null for block type items");
                    }
                }
            }
        } else if ( EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(item.getClassification()) ) {
            // this is a block parent item (created just to hold the block parent text)
            if (templateItem.getBlockParent() == null || !templateItem.getBlockParent().booleanValue() ) {
                throw new IllegalArgumentException("Template Item block parent must be TRUE for parent block item");
            }
            if (templateItem.getScaleDisplaySetting() == null) {
                throw new IllegalArgumentException("Template Item scale display setting must be included for parent block item");
            }
            if (templateItem.getBlockId() != null) {
                throw new IllegalArgumentException("Item blockid must be null for parent block item");
            }
            if (templateItem.getDisplayRows() != null) {
                throw new IllegalArgumentException("Item displayRows must be null for block type items");
            }
        } else if ( EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(item.getClassification()) ||
                EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(item.getClassification()) ) {
            // this is a multiple type item
            validateItemByClassification(item);
            if (templateItem.getScaleDisplaySetting() == null) {
                templateItem.setScaleDisplaySetting(item.getScaleDisplaySetting());
            }
            if (templateItem.getDisplayRows() != null) {
                throw new IllegalArgumentException("Item displayRows must be null for multiple type items");
            }
            if (templateItem.getBlockId() != null) {
                throw new IllegalArgumentException("Item blockid cannot be included for multiple type items");
            }
            if (templateItem.getBlockParent() != null) {
                throw new IllegalArgumentException("Item blockParent must be null for multiple type items");
            }
        } else if ( EvalConstants.ITEM_TYPE_TEXT.equals(item.getClassification()) ) {
            validateItemByClassification(item);
            if (templateItem.getDisplayRows() == null) {
                templateItem.setDisplayRows(item.getDisplayRows());
            }
            if (templateItem.getScaleDisplaySetting() != null) {
                throw new IllegalArgumentException("ScaleDisplaySetting cannot be included for text type items");
            }
            if (templateItem.getBlockId() != null) {
                throw new IllegalArgumentException("Item blockid cannot be included for text type items");
            }
            if (templateItem.getBlockParent() != null) {
                throw new IllegalArgumentException("Item blockParent must be null for text type items");
            }
        } else if ( EvalConstants.ITEM_TYPE_HEADER.equals(item.getClassification()) ) {
            validateItemByClassification(item);
            if (templateItem.getDisplayRows() != null) {
                throw new IllegalArgumentException("Item displayRows cannot be included for header type items");
            }
            if (templateItem.getScaleDisplaySetting() != null) {
                throw new IllegalArgumentException("ScaleDisplaySetting cannot be included for header type items");
            }
            if (templateItem.getBlockId() != null) {
                throw new IllegalArgumentException("Item blockid cannot be included for header type items");
            }
            if (templateItem.getBlockParent() != null) {
                throw new IllegalArgumentException("Item blockParent must be null for header type items");
            }
        } else {
            throw new IllegalArgumentException("Invalid item classification specified ("+item.getClassification()+"), you must use the ITEM_TYPE constants to indicate classification (and cannot use BLOCK)");
        }
    }

    /**
     * @param bool takes a Boolean and converts it to a boolean to ensure no null pointer exceptions
     * @return the boolean value of the Boolean or false if it is null
     */
    public static boolean safeBool(Boolean bool) {
        boolean result = false;
        if (bool != null) {
            result = bool.booleanValue();
        }
        return result;
    }

}      