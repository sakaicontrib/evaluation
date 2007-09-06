/**
 * ExternalHierarchy.java - evaluation - 2007 Sep 5, 2007 11:23:05 AM - AZ
 */

package org.sakaiproject.evaluation.logic.externals;

import java.util.Set;

import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider;
import org.sakaiproject.evaluation.model.constant.EvalConstants;


/**
 * This brings in the hierarchy information and gives us the ability to control the
 * hierarchy
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ExternalHierarchyLogic extends EvalHierarchyProvider {

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
    * exception occurs if these rules are violated
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
    * Set permissions for a user for a node
    * 
    * @param userId the internal user id (not username)
    * @param nodeId a unique id for a hierarchy node
    * @param hierarchyPermConstant a HIERARCHY_PERM constant from {@link EvalConstants},
    * if this is set to null then remove all permissions for this user from this node
    */
   public void assignUserNodePerm(String userId, String nodeId, String hierarchyPermConstant);

   /**
    * Set this set to be the set of eval group ids associated with this node,
    * can also be used to clear the associated ids
    * 
    * @param nodeId a unique id for a hierarchy node
    * @param evalGroupIds the unique IDs of eval groups to associate, if this is an
    * empty set then this will clear the associations so there are no groups associated
    */
   public void setEvalGroupsForNode(String nodeId, Set<String> evalGroupIds);

}
