/**
 * ExternalHierarchy.java - evaluation - 2007 Sep 5, 2007 11:23:05 AM - AZ
 */

package org.sakaiproject.evaluation.logic.externals;

import org.sakaiproject.evaluation.logic.model.HierarchyNode;
import org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider;


/**
 * This brings in the hierarchy information and gives us the ability to control the
 * hierarchy
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ExternalHierarchy extends EvalHierarchyProvider {

   /**
    * Add a new node to a hierarchy
    * 
    * @param parentNodeId the unique id for the parent of this node, can be null if this is the root or a top level node
    * @return the object representing the newly added node
    */
   public HierarchyNode addNode(String parentNodeId);

   /**
    * Remove a node from the hierarchy if it is possible,
    * nodes can only be removed if they have no children associations,
    * root nodes can never be removed,
    * exception occurs if these rules are violated
    * 
    * @param nodeId a unique id for a hierarchy node
    */
   public void removeNode(String nodeId);

   /**
    * Save meta data on a node, this has to be done separately from creating a node
    * 
    * @param nodeId a unique id for a hierarchy node
    * @param title the title of the node
    * @param description a description for this node
    * @return the object representing the updated node
    */
   public HierarchyNode updateNodeData(String nodeId, String title, String description);

}
