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

import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalTemplateItem;


/**
 * This is a high level group of template items related to categories (e.g. Course, Instructor, ...),
 * these categories should receive special treatment in most cases<br/>
 * The structure goes {@link TemplateItemGroup} -> {@link HierarchyNodeGroup} -> {@link EvalTemplateItem}
 * <b>NOTE:</b> There will always be a top level {@link HierarchyNodeGroup} but it may have an empty list of
 * templateItems inside it
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

   public TemplateItemGroup() { }
   
   public TemplateItemGroup(String associateType, String associateId) {
      this.associateType = associateType;
      this.associateId = associateId;
   }

}
