/******************************************************************************
 * EvalAssignLogic.java - created by aaronz@vt.edu on Dec 27, 2006
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

package org.sakaiproject.evaluation.logic;

import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEvaluation;


/**
 * Handles all logic associated with processing eval group assignments
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalAssignsLogic {

   // ASSIGN COURSES

   /**
    * Save or update the group assignment, used to make a linkage from
    * an evaluation to an eval group (course, site, group, evalGroupId, etc...),
    * cannot add assignments if the evaluation is closed<br/>
    * <b>Note:</b> cannot change the group or the evaluation once the object is created,
    * you can change any other property at any time<br/>
    * Use {@link #canCreateAssignEval(String, Long)} or 
    * {@link #canControlAssignGroup(String, Long)} to check 
    * if user can do this and avoid possible exceptions
    * 
    * @param assignGroup the object to save, represents a link from a single group to an evaluation
    * @param userId the internal user id (not username)
    */
   public void saveAssignGroup(EvalAssignGroup assignGroup, String userId);

   /**
    * Remove the evalGroupId assignment, used to make a linkage from
    * an evaluation to an eval group (course, site, group, etc...),
    * represents a link from a single group to an evaluation,
    * can only remove assignments if the evaluation is still in queue,
    * also removes the evaluation if there are no assignments remaining<br/>
    * Use {@link #canControlAssignGroup(String, Long)} to check if user can do this
    * and avoid possible exceptions
    * 
    * @param assignGroupId the id of an {@link EvalAssignGroup} object to remove
    * @param userId the internal user id (not username)
    */
   public void deleteAssignGroup(Long assignGroupId, String userId);
   
	/**
	 * Get the assign group associated with this external id<br/>
	 * Note: An assign group eid is null except when the assignGroup
	 * was imported from an external system.
	 * 
	 * @param eid the id of an assign group in an external system
	 * @return the assign group object or null if not found
	 */
	public EvalAssignGroup getAssignGroupByEid(String eid);

   /**
    * Get an assign group by its unique id,
    * should mostly be used to determine if an assign group id is valid and 
    * to get the evaluation related to it
    * 
    * @param assignGroupId unique id for an {@link EvalAssignGroup} entity
    * @return an assign group entity or null if not found
    */
   public EvalAssignGroup getAssignGroupById(Long assignGroupId);

   /**
    * Get the unique id of an {@link EvalAssignGroup} based on the unique id
    * of an {@link EvalEvaluation} and an eval group id
    * 
    * @param evaluationId unique id of an {@link EvalEvaluation}
    * @param evalGroupId the internal unique ID for an evalGroup
    * @return a unique id for an {@link EvalAssignGroup}
    */
   public Long getAssignGroupId(Long evaluationId, String evalGroupId);


   // HIERARCHY LOGIC

   /**
    * Assigns hierarchy nodes and/or evalgroups to an evaluation and therefore assigns all evalgroups that are located
    * at that hierarchy node, this will not include groups below or above this node so if you want
    * to assign the nodes below you will need to include them in the array
    * @param evaluationId unique id of an {@link EvalEvaluation}
    * @param nodeIds unique IDs of a set of hierarchy nodes (null if none to assign)
    * @param evalGroupIds the internal unique IDs for a set of evalGroups (null if none to assign)
    * @return a list of the persisted hierarchy assignments (nodes and groups together)
    */
   public List<EvalAssignHierarchy> addEvalAssignments(Long evaluationId, String[] nodeIds, String[] evalGroupIds);

   /**
    * Remove all assigned hierarchy nodes with the unique ids specified,
    * also cleanup all the assign groups that are associated underneath these hierarchy nodes
    * @param assignHierarchyIds unique ids of {@link EvalAssignHierarchy} objects
    */
   public void deleteAssignHierarchyNodesById(Long[] assignHierarchyIds);

   /**
    * Retrieve a single assign hierarchy item based on its uniqie id
    * @param assignHierarchyId unique id of {@link EvalAssignHierarchy} objects
    * @return the assigned hierarchy node or null if none found
    */
   public EvalAssignHierarchy getAssignHierarchyById(Long assignHierarchyId);

   /**
    * Get all the assigned hierarchy nodes by the evaluation they are associated with
    * @param evaluationId unique id of an {@link EvalEvaluation}
    * @return a list of all the hierarchy assignments for an evaluation, empty if none found
    */
   public List<EvalAssignHierarchy> getAssignHierarchyByEval(Long evaluationId);

   /**
    * Get all the directly assigned eval groups by the evaluation they are associated with,
    * does not include groups which are associated with nodes, this should be used for
    * managing the list of groups only, fetching the groups for an evaluation is handled by methods
    * in the evaluations logic
    * @param evaluationId unique id of an {@link EvalEvaluation}
    * @return a list of all the hierarchy groups directly assigned for an evaluation, empty if none found
    */
   public List<EvalAssignGroup> getAssignGroupsByEval(Long evaluationId);


   // PERMISSIONS

   /**
    * Can the user create group assignments in the given evaluation, 
    * checks the evaluation to see if any groups can be created in it at this time,
    * also checks that the user has permissions to create groups (like ownership)
    * 
    * @param userId the internal user id (not username)
    * @param evaluationId the id of an EvalEvaluation object
    * @return true if the user can create a group assignment, false otherwise
    */
   public boolean canCreateAssignEval(String userId, Long evaluationId);

   /**
    * Can the user remove the given group assignment,
    * checks the associated evaluation state and permissions 
    * to see if the assigned group can be removed
    * 
    * @param userId the internal user id (not username)
    * @param assignGroupId the id of an {@link EvalAssignGroup} object
    * @return true if the user can remove this group assignment, false otherwise
    */
   public boolean canDeleteAssignGroup(String userId, Long assignGroupId);

}
