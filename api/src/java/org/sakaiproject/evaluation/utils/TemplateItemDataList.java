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
 * An extension to list that allows us to easily get things like the total number of template items contained in this structure,
 * otherwise this basically behaves just like a normal list<br/>
 * We can also store extra data in the list (like the total list of template items)<br/>
 * <b>NOTE:</b> The size of this list is going to be the number of {@link TemplateItemGroup}s contained,
 * these indicate the number of associate groupings
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TemplateItemDataList extends ArrayList<TemplateItemGroup> {

   /**
    * This should be the complete DISPLAYORDERED list of all templateItems in this data structure
    */
   private List<EvalTemplateItem> allTemplateItems = null;
   /**
    * @return the complete DISPLAYORDERED list of all templateItems in this data structure
    */
   public List<EvalTemplateItem> getAllTemplateItems() {
      return allTemplateItems;
   }

   /**
    * The list of all hierarchy nodes which we are using with this set of templateItems
    */
   private List<EvalHierarchyNode> hierarchyNodes = null;
   /**
    * @return the list of hierarchy nodes we are working with, empty if there are none
    */
   public List<EvalHierarchyNode> getHierarchyNodes() {
      return hierarchyNodes;
   }

   /**
    * The list of all associates (defines the categories we are working with)
    */
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
      return this.size();
   }


   // INTERNAL processing methods

   protected void buildDataStructure() {
      if (allTemplateItems == null || hierarchyNodes == null || associates == null) {
         throw new IllegalArgumentException("null inputs are not allowed, empty lists are ok though");
      }

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
                  this.add(tig);

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


}
