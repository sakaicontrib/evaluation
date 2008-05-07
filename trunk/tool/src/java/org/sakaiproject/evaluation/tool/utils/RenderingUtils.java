/**
 * $Id$
 * $URL$
 * HierarchyUtils.java - evaluation - Mar 28, 2008 5:20:49 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalTemplateItem;


/**
 * A class to keep sharing rendering logic in
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class RenderingUtils {
   
   /**
    * Gets all the hierarchy nodes that these template items are associated with in hierarchy order 
    * 
    * @param hierarchyLogic the hierarchy service
    * @param templateItems a list of template items (probably all template items for an evaluation)
    * @return a sorted list of nodes
    */
   public static List<EvalHierarchyNode> makeEvalNodesList(ExternalHierarchyLogic hierarchyLogic, List<EvalTemplateItem> templateItems) {
      Set<String> nodeIds = new HashSet<String>();
      for (EvalTemplateItem templateItem : templateItems) {
         if (EvalConstants.HIERARCHY_LEVEL_NODE.equals(templateItem.getHierarchyLevel())) {
            nodeIds.add(templateItem.getHierarchyNodeId());
         }
      }
      List<EvalHierarchyNode> hierarchyNodes = new ArrayList<EvalHierarchyNode>();
      if (nodeIds.size() > 0) {
         Set<EvalHierarchyNode> nodes = hierarchyLogic.getNodesByIds(nodeIds.toArray(new String[] {}));
         hierarchyNodes = hierarchyLogic.getSortedNodes(nodes);
      }
      return hierarchyNodes;
   }
}
