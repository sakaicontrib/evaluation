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
package org.sakaiproject.evaluation.logic.externals;

import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;


/**
 * This allows for the hierarchy data to also contain permissions
 *
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ExternalHierarchyPermissions {

    /**
     * Determine if a user has a specific hierarchy permission at a specific hierarchy node,
     * a permission key can be any string though it will most likely be from a relatively small set
     * 
     * @param userId the internal user id (not username)
     * @param nodeId a unique id for a hierarchy node
     * @param hierarchyPermConstant a HIERARCHY_PERM constant from {@link EvalConstants},
     * if this is set to null then remove all permissions for this user from this node
     * @return true if the user has this permission, false otherwise
     */
    public boolean checkUserNodePerm(String userId, String nodeId, String hierarchyPermConstant);

    // ASSIGN

    /**
     * Assign the given permission to a user for the given hierarchy node,
     * can cascade the permission downward if desired
     * 
     * @param userId the internal user id (not username)
     * @param nodeId a unique id for a hierarchy node
     * @param hierarchyPermConstant a HIERARCHY_PERM constant from {@link EvalConstants},
     * if this is set to null then remove all permissions for this user from this node
     * @param cascade if true then the permission is assigned to all nodes below this one as well,
     * if false it is only assigned to this node
     */
    public void assignUserNodePerm(String userId, String nodeId, String hierarchyPermConstant, boolean cascade);

    /**
     * Remove a permission for a user from the given hierarchy node,
     * can cascade the permission downward if desired
     * 
     * @param userId the internal user id (not username)
     * @param nodeId a unique id for a hierarchy node
     * @param hierarchyPermConstant a HIERARCHY_PERM constant from {@link EvalConstants},
     * if this is set to null then remove all permissions for this user from this node
     * @param cascade if true then the permission is removed from all nodes below this one as well,
     * if false it is only removed from this node
     */
    public void removeUserNodePerm(String userId, String nodeId, String hierarchyPermConstant, boolean cascade);

    // NODES

    /**
     * Get all the userIds for users which have a specific permission in a set of
     * hierarchy nodes, this can be used to check one node or many nodes as needed
     * 
     * @param nodeIds an array of unique ids for hierarchy nodes
     * @param hierarchyPermConstant a HIERARCHY_PERM constant from {@link EvalConstants},
     * if this is set to null then remove all permissions for this user from this node
     * @return a set of userIds (not username/eid)
     */
    public Set<String> getUserIdsForNodesPerm(String[] nodeIds, String hierarchyPermConstant);

    /**
     * Get the hierarchy nodes which a user has a specific permission in,
     * this is used to find a set of nodes which a user should be able to see and to build
     * the list of hierarchy nodes a user has a given permission in
     * 
     * @param userId the internal user id (not username)
     * @param hierarchyPermConstant a HIERARCHY_PERM constant from {@link EvalConstants},
     * if this is set to null then remove all permissions for this user from this node
     * @return a Set of {@link EvalHierarchyNode} objects
     */
    public Set<EvalHierarchyNode> getNodesForUserPerm(String userId, String hierarchyPermConstant);

    /**
     * Get the set of all permissions which a user has on a node or group of nodes,
     * NOTE: this will get the set of ALL permissions inclusively for the given nodeIds
     * so nodes in the set which a user has no permissions on will not cause this to return no permissions,
     * example: for given user: nodeA(perm1, perm2), nodeB(perm1), nodeC(perm2), nodeD() : returns: (perm1, perm2)
     * 
     * @param userId the internal user id (not username)
     * @param nodeIds an array of unique ids for hierarchy nodes
     * @return the set of permission keys which exist on any of the given nodes
     */
    public Set<String> getPermsForUserNodes(String userId, String[] nodeIds);

    /**
     * Get all the users and permissions currently assigned to nodes,
     * the returned map will always contain every passed in nodeId as a key
     * <br/>
     * This is not super efficient by itself so it should not used when other methods are sufficient,
     * however, it is actually much better than calling the other methods repeatedly so this is primarily
     * for use in administrative interfaces
     * 
     * @param nodeIds an array of unique ids for hierarchy nodes
     * @return the map of nodeId -> (map of userId -> Set(permission))
     */
    public Map<String, Map<String, Set<String>>> getUsersAndPermsForNodes(String... nodeIds);

    /**
     * Get all the nodeIds and permissions for the given userIds,
     * the returned map will always contain every userId that was passed in as a key
     * 
     * @param userIds an array of unique ids for users (internal id, not eid)
     * @return the map of userId -> (map of nodeId -> Set(permission))
     */
    public Map<String, Map<String, Set<String>>> getNodesAndPermsForUser(String... userIds);

}
