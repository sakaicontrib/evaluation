/**
 * $Id$
 * $URL$
 * ExternalHierarchy.java - evaluation - Sep 5, 2007 11:23:05 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.externals;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.providers.EvalHierarchyProvider;


/**
 * This brings in the hierarchy information and gives us the ability to control the
 * hierarchy
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ExternalHierarchyLogic extends EvalHierarchyProvider, ExternalHierarchyPermissions {

   /**
    * Add a new node to a hierarchy
    * 
    * @param parentNodeId the unique id for the parent of this node, can be null if this is the root or a top level node
    * @return the object representing the newly added node
    */
   public EvalHierarchyNode addNode(String parentNodeId);

   /**
    * Remove a node from the hierarchy if it is possible,
    * nodes can only be removed if they have no children associations,
    * root nodes can never be removed,
    * nodes which have evalgroups assigned to them cannot be removed,
    * exception occurs if these rules are violated
    * <br/>NOTE: this will remove associated template items from their association
    * with the node and place them back in the default level
    * 
    * @param nodeId a unique id for a hierarchy node
    * @return the object representing the parent of the removed node
    */
   public EvalHierarchyNode removeNode(String nodeId);

   /**
    * Save meta data on a node, this has to be done separately from creating a node
    * 
    * @param nodeId a unique id for a hierarchy node
    * @param title the title of the node
    * @param description a description for this node
    * @return the object representing the updated node
    */
   public EvalHierarchyNode updateNodeData(String nodeId, String title, String description);

   /**
    * Set this set to be the set of eval group ids associated with this node,
    * can also be used to clear the associated ids
    * 
    * @param nodeId a unique id for a hierarchy node
    * @param evalGroupIds the unique IDs of eval groups to associate, if this is an
    * empty set then this will clear the associations so there are no groups associated
    */
   public void setEvalGroupsForNode(String nodeId, Set<String> evalGroupIds);

   /**
    * Get all the children node ids which exist under the set of supplied nodes
    * @param nodes a set of eval hierarchy nodes
    * @param includeSuppliedNodeIds include the nodes ids of the supplied collection of nodes 
    * @return a set of all unique child nodes
    */
   public Set<String> getAllChildrenNodes(Collection<EvalHierarchyNode> nodes, boolean includeSuppliedNodeIds);

   /**
    * Create a sorted list of nodes based on a set of input nodes,
    * list goes from root (or highest parent) down to the bottom most node
    * 
    * @param nodes a collection of nodes
    * @return a list of {@link EvalHierarchyNode}
    */
   public List<EvalHierarchyNode> getSortedNodes(Collection<EvalHierarchyNode> nodes);

}
