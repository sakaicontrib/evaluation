/******************************************************************************
 * EvalHierarchyProvider.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.providers;

import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.HierarchyNode;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * This interface provides methods to get hierarchical data into evaluation system
 * for use in determining the structure above evaluation groups related to
 * adminstration and access to data and control of evaluations for certain
 * subsets of eval groups<br/>
 * This interface can and should be implemented and then spring loaded to allow
 * an institution to bring in external hierarchy data, there is currently no other
 * way to have hierarchical data in the evaluation system as no internal structure
 * exists<br/>
 * <br/>
 * The spring bean must have an id that matches the fully qualified classname for this interface<br/>
 * Example:
 * <xmp>
 * <bean id="org.sakaiproject.evaluation.logic.providers.EvalHierarchyProvider"
 * 		class="org.sakaiproject.yourproject.impl.EvalHierarchyProviderImpl">
 * </bean>
 * </xmp>
 * <br/>
 * The 3 permissions this provider has to deal with are:<br/>
 * {@link EvalConstants#HIERARCHY_PERM_ASSIGN_EVALUATION} - user can assign evals
 * {@link EvalConstants#HIERARCHY_PERM_CONTROL_TEMPLATES} - user can edit and control templates
 * {@link EvalConstants#HIERARCHY_PERM_VIEW_DATA} - user can view results and templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalHierarchyProvider {

	/**
	 * Get the hierarchy root node
	 * 
	 * @return the {@link HierarchyNode} representing the root of the hierarchy
	 */
	public HierarchyNode getRootLevelNode();

	/**
	 * Get the parent node for a specific node, 
	 * returns null if this is the root node
	 * 
	 * @param nodeId a unique id for a hierarchy node
	 * @return a {@link HierarchyNode} object representing the parent node
	 */
	public HierarchyNode getParentNode(String nodeId);

	/**
	 * Get all children nodes for this node in the hierarchy, 
	 * will return no nodes if this is not a parent node
	 * 
	 * @param nodeId a unique id for a hierarchy node
	 * @return a Set of {@link HierarchyNode} objects representing all children nodes for the specified parent,
	 * empty set if no children found
	 */
	public Set getChildNodes(String nodeId);

	/**
	 * Get all the userIds for users which have a specific permission in a set of
	 * hierarchy nodes, this can be used to check one node or many nodes as needed<br/>
	 * The actual permissions this should handle are shown at the top of this class
	 * 
	 * @param nodeIds an array of unique ids for hierarchy nodes
	 * @param hierarchyPermConstant a HIERARCHY_PERM constant from {@link EvalConstants}
	 * @return an array of userIds
	 */
	public String[] getUserIdsForNodesPerm(String[] nodeIds, String hierarchyPermConstant);

	/**
	 * Get the hierarchy nodes which a user has a specific permission in,
	 * this is used to find a set of nodes which a user should be able to see and to build,
	 * the list of hierarchy for selecting eval groups to assign evaluations to
	 * <br/>The actual permissions this should handle are shown at the top of this class
	 * 
	 * @param userId the internal user id (not username)
	 * @param hierarchyPermConstant a HIERARCHY_PERM constant from {@link EvalConstants}
	 * @return a Set of {@link HierarchyNode} objects
	 */
	public Set getNodesForUserPerm(String userId, String hierarchyPermConstant);

	/**
	 * Determine if a user has a specific hierarchy permission at a specific hierarchy node
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
	 * @return a List of {@link HierarchyNode} objects (ordered from root to evalgroup)
	 */
	public List getNodesAboveEvalGroup(String evalGroupId);

	/**
	 * Get the set of eval groups beneath a specific hierarachy node, note that this should only
	 * include the eval groups directly beneath this node and not any groups that are under
	 * child nodes of this node
	 * 
	 * @param nodeId a unique id for a hierarchy node
	 * @return a Set of {@link EvalGroup} objects representing the eval groups beneath this hierarchy node
	 */
	public Set getEvalGroupsForNode(String nodeId);

}
