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
    * Get the list of {@link EvalTemplateItem} objects for a specific category,
    * guaranteed to return the items in the correct template item order
    * 
    * @param itemTypeConstant and ITEM_CATEGORY constant from {@link EvalConstants}
    * @param itemList a list of {@link EvalTemplateItem} objects
    * @return a list of {@link EvalTemplateItem} objects which have a specific category
    */
   public static List<EvalTemplateItem> getCategoryTemplateItems(String itemTypeConstant, List<EvalTemplateItem> templateItemsList) {    
      List<EvalTemplateItem> catItemsList = new ArrayList<EvalTemplateItem>();

      List<EvalTemplateItem> orderedItems = orderTemplateItems(templateItemsList);

      for (int i=0; i<orderedItems.size(); i++) {
         EvalTemplateItem templateItem = (EvalTemplateItem) orderedItems.get(i);
         if ( itemTypeConstant.equals( templateItem.getItemCategory() ) ) {
            catItemsList.add(templateItem);
         }
      }

      return catItemsList;
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

      List<EvalTemplateItem> orderedItems = orderTemplateItems(templateItemsList);

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
    * Check if templateItem is required (must be answered by a user taking an evaluation) or not
    * <b>NOTE</b> use {@link #getRequiredTemplateItems(List)} to do a large set
    * @param templateItem a templateItem persistent object
    * @return true if the item is required, false otherwise
    */
   public static boolean isRequired(EvalTemplateItem templateItem) {
      // all answerable items that are not textual are required
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
         if (! isRequired(templateItem)) {
            continue;
         }
         requiredItemsList.add(templateItem);
      }

      return requiredItemsList;
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
            new Integer(1), item.getCategory(), EvalConstants.HIERARCHY_LEVEL_TOP, EvalConstants.HIERARCHY_NODE_ID_NONE);

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

}      