/**
 * $Id$
 * $URL$
 * HierarchyNodeGroup.java - evaluation - Mar 27, 2008 6:18:50 PM - azeckoski
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

import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalTemplateItem;


/**
 * This is a high level group of hierarchy nodes which contain template items,
 * these nodes should receive special treatment in most cases<br/>
 * The structure goes {@link TemplateItemGroup} -> {@link HierarchyNodeGroup} -> {@link EvalTemplateItem}<br/>
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

   public HierarchyNodeGroup() { }

   public HierarchyNodeGroup(EvalHierarchyNode node) {
      this.node = node;
   }
  
}
