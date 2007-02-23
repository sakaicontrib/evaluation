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

import org.sakaiproject.evaluation.model.EvalAssignGroup;


/**
 * Handles all logic associated with processing eval group assignments
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalAssignsLogic {

	// ASSIGN COURSES

	/**
	 * Save or update the group assignment, used to make a linkage from
	 * an evaluation to an eval group (course, site, group, context, etc...),
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
	 * Remove the context assignment, used to make a linkage from
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
	 * Get the list of assigned groups for an evaluation id, this
	 * is how the evaluation is tied to users (users are associated with a group)
	 * 
	 * @param evaluationId the id of an EvalEvaluation object
	 * @return a List of EvalAssignContext objects
	 */
	public List getAssignGroupsByEvalId(Long evaluationId);

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
