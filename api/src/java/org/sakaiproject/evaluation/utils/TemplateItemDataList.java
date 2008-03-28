/**
 * $Id$
 * $URL$
 * TemplateItemDataList.java - evaluation - Mar 28, 2008 8:58:12 AM - azeckoski
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalTemplateItem;


/**
 * A special data structure for wrapping template items that allows us to easily get things 
 * like the total number of template items contained in this structure and meta data about them<br/>
 * We can also store extra data in the list (like the total list of template items)<br/>
 * <b>NOTE:</b> The size of this list is going to be the number of {@link DataTemplateItem}s contained,
 * these indicate the total number of rendered items and not the total count of TemplateItems
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TemplateItemDataList {

   private List<DataTemplateItem> dataTemplateItems = null;
   /**
    * This is the main method you should call after creating this data structure,
    * it will return the complete list of all the special 
    * @return the complete DISPLAYORDERED list of all dataTemplateItems in this data structure
    */
   public List<DataTemplateItem> getDataTemplateItems() {
      if (dataTemplateItems == null) {
         buildFlatDataList();
      }
      return dataTemplateItems;
   }

   private List<TemplateItemGroup> templateItemGroups = null;
   /**
    * @return the list of all {@link TemplateItemGroup}s in this structure
    */
   public List<TemplateItemGroup> getTemplateItemGroups() {
      if (templateItemGroups == null) {
         buildDataStructure();
      }
      return templateItemGroups;
   }

   private List<EvalTemplateItem> allTemplateItems = null;
   /**
    * @return the complete DISPLAYORDERED list of all templateItems in this data structure
    */
   public List<EvalTemplateItem> getAllTemplateItems() {
      return allTemplateItems;
   }

   private List<EvalHierarchyNode> hierarchyNodes = null;
   /**
    * @return the list of hierarchy nodes we are working with, empty if there are none
    */
   public List<EvalHierarchyNode> getHierarchyNodes() {
      return hierarchyNodes;
   }

   private Map<String, List<String>> associates = null;
   /**
    * @return the map of associate types -> lists of ids for that type,
    * will always include at least one entry: EvalConstants.ITEM_CATEGORY_COURSE -> List[null]
    */
   public Map<String, List<String>> getAssociates() {
      return associates;
   }

   /**
    * Generate the rendering data structure for working with template items,
    * this is primarily used for sorting and grouping the template items properly<br/>
    * <b>NOTE:</b> There will always be a course category {@link TemplateItemGroup} as long
    * as there were some TemplateItems supplied but it may have nothing in it so you should
    * check to see if the internal lists are empty
    * 
    * @param allTemplateItems the list of all template items to be placed into the structure,
    * this must not be null and must include at least one {@link EvalTemplateItem}
    * @param hierarchyNodes the list of all hierarchy nodes for grouping (list order will be used),
    * this can be null if we are not using hierarchy or there are no hierarchy nodes in use
    * @param associates a map of associate type -> list of ids of that type,
    * this defines the categories of items we are working with and segments them,
    * e.g. {@link EvalConstants#ITEM_CATEGORY_INSTRUCTOR} => [aaronz,sgtithens],
    * normally only the instructors,
    * can be null if there are no categories in use (course category is always used)
    */
   public TemplateItemDataList(List<EvalTemplateItem> allTemplateItems,
         List<EvalHierarchyNode> hierarchyNodes, Map<String, List<String>> associates) {
      if (allTemplateItems == null || allTemplateItems.size() == 0) {
         throw new IllegalArgumentException("You must supply a non-null list of at least one template item to use this structure");
      }

      this.allTemplateItems = TemplateItemUtils.orderTemplateItems(allTemplateItems, false);

      if (hierarchyNodes != null) {
         this.hierarchyNodes = hierarchyNodes;
      } else {
         this.hierarchyNodes = new ArrayList<EvalHierarchyNode>();
      }

      if (associates != null) {
         this.associates = associates;
      } else {
         this.associates = new HashMap<String, List<String>>();
      }

      // ensure there is at least the default course category
      if (! this.associates.containsKey(EvalConstants.ITEM_CATEGORY_COURSE)) {
         List<String> courses = new ArrayList<String>();
         courses.add(null);
         this.associates.put(EvalConstants.ITEM_CATEGORY_COURSE, courses);
      }

      buildDataStructure();
   }

   // PUBLIC data methods

   public int getTemplateItemsCount() {
      return allTemplateItems.size();
   }

   private int nonChildItemsCount = 0;
   /**
    * @return the count of all non-block-child template items
    */
   public int getNonChildItemsCount() {
      return nonChildItemsCount;
   }

   private List<String> associateTypes = new ArrayList<String>();
   /**
    * @return the correctly ordered list of all associate types for these template items
    */
   public List<String> getAssociateTypes() {
      return associateTypes;
   }

   /**
    * @return the count of the number of associate groupings,
    * should always be 1 or greater
    */
   public int getAssociateGroupingsCount() {
      return templateItemGroups.size();
   }

   // BUILD the flat list and return it

   /**
    * This turns the data structure into a flattened list of {@link DataTemplateItem}s
    */
   protected void buildFlatDataList() {
      if (templateItemGroups == null) {
         buildDataStructure();
      }

      dataTemplateItems = new ArrayList<DataTemplateItem>();
      // loop through and build the flattened list
      for (int i = 0; i < templateItemGroups.size(); i++) {
         TemplateItemGroup tig = templateItemGroups.get(i);
         for (int j = 0; j < tig.hierarchyNodeGroups.size(); j++) {
            HierarchyNodeGroup hng = tig.hierarchyNodeGroups.get(j);
            for (int k = 0; k < hng.templateItems.size(); k++) {
               EvalTemplateItem templateItem = hng.templateItems.get(k);
               DataTemplateItem dti = new DataTemplateItem(templateItem, tig.associateType, tig.associateId, hng.node);
               if (k == 0) {
                  dti.isFirstInNode = true;
                  if (j == 0) {
                     dti.isFirstInAssociated = true;
                  }
               }
               dataTemplateItems.add(dti);
            }
         }
      }
   }

   // INTERNAL processing methods

   /**
    * This processes the data in the structure and builds up a nested list structure so the TIs are properly grouped
    */
   protected void buildDataStructure() {
      if (allTemplateItems == null || hierarchyNodes == null || associates == null) {
         throw new IllegalArgumentException("null inputs are not allowed, empty lists are ok though");
      }

      templateItemGroups = new ArrayList<TemplateItemGroup>();
      if (allTemplateItems.size() > 0) {
         // filter out the block child items, to get a list of non-child items
         List<EvalTemplateItem> nonChildItemsList = TemplateItemUtils.getNonChildItems(this.allTemplateItems);
         nonChildItemsCount = nonChildItemsList.size();

         // turn the map keys into a properly sorted list of types
         this.associateTypes = new ArrayList<String>();
         for (int i = 0; i < EvalConstants.ITEM_CATEGORY_ORDER.length; i++) {
            if (associates.containsKey(EvalConstants.ITEM_CATEGORY_ORDER[i])) {
               associateTypes.add(EvalConstants.ITEM_CATEGORY_ORDER[i]);
            }
         }

         // loop through the associates
         for (String associateType : associateTypes) {
            List<String> associateIds = associates.get(associateType);
            // get all the template items for this category
            List<EvalTemplateItem> categoryNonChildItemsList = TemplateItemUtils.getCategoryTemplateItems(associateType, nonChildItemsList);
            if (categoryNonChildItemsList.size() > 0) {
               // assume the associateIds are in the correct render order
               for (String associateId : associateIds) {
                  // handle the data creation for this associateId
                  TemplateItemGroup tig = new TemplateItemGroup(associateType, associateId);
                  tig.hierarchyNodeGroups = new ArrayList<HierarchyNodeGroup>();
                  templateItemGroups.add(tig);

                  // now handle the hierarchy levels
                  // top level first
                  List<EvalTemplateItem> templateItems = TemplateItemUtils.getNodeItems(categoryNonChildItemsList, null);
                  if (templateItems.size() > 0) {
                     addNodeTemplateItems(tig, null, templateItems);
                  }

                  // then do the remaining nodes in the order supplied
                  for (EvalHierarchyNode evalNode: hierarchyNodes) {
                     templateItems = TemplateItemUtils.getNodeItems(categoryNonChildItemsList, evalNode.id);
                     if (templateItems.size() > 0) {
                        addNodeTemplateItems(tig, evalNode, templateItems);
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * Add the template items for this node to a newly create HNG and attach it to the TIG
    * 
    * @param tig
    * @param evalNode can be null to indicate the top level
    * @param templateItems
    */
   private void addNodeTemplateItems(TemplateItemGroup tig, EvalHierarchyNode evalNode,
         List<EvalTemplateItem> templateItems) {
      HierarchyNodeGroup hng = new HierarchyNodeGroup(evalNode);
      hng.templateItems = templateItems;
      tig.hierarchyNodeGroups.add(hng);
   }


   // INNER classes

   /**
    * This is a template item with lots of extra meta data so that it can be easily determined where it
    * goes in the processing order and whether it is in the hierarchy or associated with nodes<br/>
    * <b>NOTE:</b> The same template item can belong to many DataTemplateItems
    * 
    * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
    */
   public class DataTemplateItem {

      /**
       * The template item for this data object
       */
      public EvalTemplateItem templateItem;
      /**
       * The type (category) of this template item,
       * this will usually be like {@link EvalConstants#ITEM_CATEGORY_INSTRUCTOR},
       * required and cannot be null
       */
      public String associateType;
      /**
       * The optional ID of the thing that is associated with this type for this template item,
       * e.g. if this is an instructor it would be their internal userId<br/>
       * can be null
       */
      public String associateId;
      /**
       * The hierarchy node associated with this template items,
       * null indicates this is the top level and not a node at all
       */
      public EvalHierarchyNode node;
      /**
       * true if this is the first item for its associated type and id
       */
      public boolean isFirstInAssociated = false;
      /**
       * true if this is the first item for the hierarchy node (or for the top level)
       */
      public boolean isFirstInNode = false;
      /**
       * this will be null if this is not a block parent,
       * if this is a block parent then this will be a list of all block children in displayOrder 
       */
      public List<EvalTemplateItem> blockChildItems;

      public DataTemplateItem(EvalTemplateItem templateItem, String associateType,
            String associateId, EvalHierarchyNode node) {
         this.templateItem = templateItem;
         this.associateType = associateType;
         this.associateId = associateId;
         this.node = node;
         if (TemplateItemUtils.isBlockParent(templateItem)) {
            blockChildItems = TemplateItemUtils.getChildItems(allTemplateItems, templateItem.getId());
         }
      }

   }

   /**
    * This is a high level group of template items related to categories (e.g. Course, Instructor, ...),
    * these categories should receive special treatment in most cases<br/>
    * The structure goes {@link TemplateItemGroup} -> {@link HierarchyNodeGroup} -> {@link EvalTemplateItem}
    * <b>NOTE:</b> There will always be a top level {@link HierarchyNodeGroup} but it may have an empty list of
    * templateItems inside it<br/>
    * Normally you would want to iterate through the {@link #hierarchyNodeGroups} and 
    * 
    * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
    */
   public class TemplateItemGroup {

      /**
       * The type (category) of this TIG,
       * this will usually be like {@link EvalConstants#ITEM_CATEGORY_INSTRUCTOR},
       * required and cannot be null
       */
      public String associateType;
      /**
       * The optional ID of the thing that is associated with this type,
       * e.g. if this is an instructor it would be their internal userId<br/>
       * can be null
       */
      public String associateId;
      /**
       * The list of hierarchical node groups within this group,
       * ordered correctly for display and reporting
       */
      public List<HierarchyNodeGroup> hierarchyNodeGroups;

      /**
       * @return the count of all template items contained in this group,
       * does not include block children
       */
      public int getTemplateItemsCount() {
         int total = 0;
         for (HierarchyNodeGroup hng : hierarchyNodeGroups) {
            total += hng.templateItems.size();
         }
         return total;
      }

      /**
       * @return the list of all template items in this group in displayOrder,
       * does not include block children
       */
      public List<EvalTemplateItem> getTemplateItems() {
         List<EvalTemplateItem> tis = new ArrayList<EvalTemplateItem>();
         for (HierarchyNodeGroup hng : hierarchyNodeGroups) {
            tis.addAll(hng.templateItems);
         }
         return tis;
      }

      public TemplateItemGroup(String associateType, String associateId) {
         this.associateType = associateType;
         this.associateId = associateId;
         hierarchyNodeGroups = new ArrayList<HierarchyNodeGroup>();
      }

      public TemplateItemGroup(String associateType, String associateId, List<HierarchyNodeGroup> hierarchyNodeGroups) {
         this.associateType = associateType;
         this.associateId = associateId;
         this.hierarchyNodeGroups = hierarchyNodeGroups;
      }
   }

   /**
    * This is a high level group of hierarchy nodes which contain template items,
    * these nodes should receive special treatment in most cases<br/>
    * The structure goes {@link TemplateItemGroup} -> {@link HierarchyNodeGroup} -> {@link EvalTemplateItem}<br/>
    * Normally you would want to iterate over the {@link #templateItems} and process each one in order,
    * don't forget to handle the special case of the block parents
    * 
    * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
    */
   public class HierarchyNodeGroup {

      /**
       * The hierarchy node associated with this group of template items,
       * null indicates this is the top level and all TIs here are associated with the top level and not a node at all
       */
      public EvalHierarchyNode node;
      /**
       * The list of template items within this group (will not include block-child items),
       * ordered correctly for display and reporting
       */
      public List<EvalTemplateItem> templateItems;

      public HierarchyNodeGroup(EvalHierarchyNode node) {
         this.node = node;
         templateItems = new ArrayList<EvalTemplateItem>();
      }

      public HierarchyNodeGroup(EvalHierarchyNode node, List<EvalTemplateItem> templateItems) {
         this.node = node;
         this.templateItems = templateItems;
      }

   }

}
