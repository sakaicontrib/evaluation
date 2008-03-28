/**
 * $Id$
 * $URL$
 * TemplateItemGroup.java - evaluation - Mar 27, 2008 6:11:01 PM - azeckoski
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
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalTemplateItem;


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
