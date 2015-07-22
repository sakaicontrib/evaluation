/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.providers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;

/**
 * This interface provides methods to get hierarchical data into evaluation system
 * for use in determining the structure above evaluation groups related to
 * adminstration and access to data and control of evaluationSetupService for certain
 * subsets of eval groups<br/>
 * This interface can and should be implemented and then spring loaded to allow
 * an institution to bring in external hierarchy data, there is currently no other
 * way to have hierarchical data in the evaluation system as no internal structure
 * exists<br/>
 * <br/>
 * The spring bean must have an id that matches the fully qualified classname for this interface<br/>
 * Example:
 * <xmp>
 * <bean id="org.sakaiproject.evaluation.providers.EvalHierarchyProvider"
 * 		class="org.sakaiproject.yourproject.impl.EvalHierarchyProviderImpl">
 * </bean>
 * </xmp>
 * <br/>
 * The permissions this provider has to deal with are:<br/>
 * {@link EvalConstants#HIERARCHY_PERM_ASSIGN_EVALUATION} - user can assign evals
 * {@link EvalConstants#HIERARCHY_PERM_CONTROL_NODE_DATA} - user can edit and control data at one node
 * {@link EvalConstants#HIERARCHY_PERM_CONTROL_TREE_DATA} - user can edit and control data below this node
 * {@link EvalConstants#HIERARCHY_PERM_VIEW_NODE_DATA} - user can view results and data at one node
 * {@link EvalConstants#HIERARCHY_PERM_VIEW_TREE_DATA} - user can view results and data below this node
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalHierarchyProvider {

   /**
    * Get the hierarchy root node of the eval hierarchy
    * 
    * @return the {@link EvalHierarchyNode} representing the root of the hierarchy
    * @throws IllegalStateException if no node can be obtained
    */
   public EvalHierarchyNode getRootLevelNode();

   /**
    * Get the node object for a specific node id
    * 
    * @param nodeId a unique id for a hierarchy node
    * @return a {@link EvalHierarchyNode} object or null if none found
    */
   public EvalHierarchyNode getNodeById(String nodeId);

   /**
    * Get a set of nodes based on an array of nodeIds,
    * allows efficient lookup of nodes
    * 
    * @param nodeIds unique ids for hierarchy nodes
    * @return a set of {@link EvalHierarchyNode} objects based on the given ids
    */
   public Set<EvalHierarchyNode> getNodesByIds(String[] nodeIds);

   /**
    * Get all children nodes for this node in the hierarchy, 
    * will return no nodes if this is not a parent node
    * 
    * @param nodeId a unique id for a hierarchy node
    * @param directOnly if true then only include the nodes 
    * which are directly connected to this node, 
    * else return every node that is a child of this node
    * @return a Set of {@link EvalHierarchyNode} objects representing 
    * all children nodes for the specified parent,
    * empty set if no children found
    */
   public Set<EvalHierarchyNode> getChildNodes(String nodeId, boolean directOnly);


   /**
    * Get all the userIds for users which have a specific permission in a set of
    * hierarchy nodes, this can be used to check one node or many nodes as needed,
    * <br/>The actual permissions this should handle are shown at the top of this class
    * 
    * @param nodeIds an array of unique ids for hierarchy nodes
    * @param hierarchyPermConstant a HIERARCHY_PERM constant from {@link EvalConstants}
    * @return a set of userIds (not username/eid)
    */
   public Set<String> getUserIdsForNodesPerm(String[] nodeIds, String hierarchyPermConstant);

   /**
    * Get the hierarchy nodes which a user has a specific permission in,
    * this is used to find a set of nodes which a user should be able to see and to build
    * the list of hierarchy nodes for selecting eval groups to assign evaluations to,
    * <br/>The actual permissions this should handle are shown at the top of this class
    * 
    * @param userId the internal user id (not username)
    * @param hierarchyPermConstant a HIERARCHY_PERM constant from {@link EvalConstants}
    * @return a Set of {@link EvalHierarchyNode} objects
    */
   public Set<EvalHierarchyNode> getNodesForUserPerm(String userId, String hierarchyPermConstant);

   /**
    * Determine if a user has a specific hierarchy permission at a specific hierarchy node,
    * <br/>The actual permissions this should handle are shown at the top of this class
    * 
    * @param userId the internal user id (not username)
    * @param nodeId a unique id for a hierarchy node
    * @param hierarchyPermConstant a HIERARCHY_PERM constant from {@link EvalConstants}
    * @return true if the user has this permission, false otherwise
    */
   public boolean checkUserNodePerm(String userId, String nodeId, String hierarchyPermConstant);


   /**
    * Gets the list of nodes in the path from an eval group to the root node,
    * should be in order with the first node being the root node and the last node being
    * the parent node for the given eval group
    *  
    * @param evalGroupId the unique ID of an eval group
    * @return a List of {@link EvalHierarchyNode} objects (ordered from root to evalgroup)
    */
   public List<EvalHierarchyNode> getNodesAboveEvalGroup(String evalGroupId);

   /**
    * Get the set of eval group ids beneath a specific hierarchy node, note that this should only
    * include the eval groups directly beneath this node and not any groups that are under
    * child nodes of this node<br/>
    * Note: this will not fail if the nodeId is invalid, it will just return no results<br/>
    * Convenience method for {@link #getEvalGroupsForNodes(String[])}
    * 
    * @param nodeId a unique id for a hierarchy node
    * @return a Set of eval group ids representing the eval groups beneath this hierarchy node
    */
   public Set<String> getEvalGroupsForNode(String nodeId);

   /**
    * Get the set of eval group ids beneath a set of hierarchy nodes, note that this should only
    * include the eval groups directly beneath these nodes and not any groups that are under
    * child nodes of this node<br/>
    * Note: this will not fail if the nodeId is invalid, it will just return no results,
    * an empty array of nodeids will return an empty map
    * 
    * @param nodeIds a set of unique ids for hierarchy nodes
    * @return a Map of nodeId -> a set of eval group ids representing the eval groups beneath that node
    */
   public Map<String, Set<String>> getEvalGroupsForNodes(String[] nodeIds);

   /**
    * Get the count of the number of eval groups assigned to each node in a group of nodes
    * @param nodeIds an array of unique ids for hierarchy nodes
    * @return a map of nodeId -> number of eval groups
    */
   public Map<String, Integer> countEvalGroupsForNodes(String[] nodeIds);

}
