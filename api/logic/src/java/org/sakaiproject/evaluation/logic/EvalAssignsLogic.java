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
 * Handles all logic associated with processing context assignments
 * (Note for developers - do not modify this without permission from the project lead)
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalAssignsLogic {

	// ASSIGN COURSES

	/**
	 * Save or update the context assignment, used to make a linkage from
	 * an evaluation to a context (course, site, group, etc...),
	 * cannot add assignments if the evaluation is closed<br/>
	 * <b>Note:</b> cannot change the context or the evaluation once the object is created,
	 * you can change any other property at any time<br/>
	 * Use {@link #canCreateAssignEval(String, Long)} or 
	 * {@link #canControlAssignContext(String, Long)} to check 
	 * if user can do this and avoid possible exceptions
	 * 
	 * @param assignContext the object to save, represents a link from a single context to an evaluation
	 * @param userId the internal user id (not username)
	 */
	public void saveAssignContext(EvalAssignGroup assignContext, String userId);

	/**
	 * Remove the context assignment, used to make a linkage from
	 * an evaluation to a context (course, site, group, etc...),
	 * represents a link from a single context to an evaluation,
	 * can only remove assignments if the evaluation is still in queue,
	 * also removes the evaluation if there are no assignments remaining<br/>
	 * Use {@link #canControlAssignContext(String, Long)} to check if user can do this
	 * and avoid possible exceptions
	 * 
	 * @param assignContextId the if of an EvalAssignContext object to remove
	 * @param userId the internal user id (not username)
	 */
	public void deleteAssignContext(Long assignContextId, String userId);

	/**
	 * Get the list of assigned contexts for an evaluation id, this
	 * is how the evaluation is tied to users (users are associated with a context)
	 * 
	 * @param evaluationId the id of an EvalEvaluation object
	 * @return a List of EvalAssignContext objects
	 */
	public List getAssignContextsByEvalId(Long evaluationId);

	// PERMISSIONS

	/**
	 * Can the user create context assignments in the given evaluation, 
	 * checks the evaluation to see if any contexts can be created in it at this time,
	 * also checks that the user has permissions to create contexts (like ownership)
	 * 
	 * @param userId the internal user id (not username)
	 * @param evaluationId the id of an EvalEvaluation object
	 * @return true if the user can create a context assignment, false otherwise
	 */
	public boolean canCreateAssignEval(String userId, Long evaluationId);

	/**
	 * Can the user remove the given context assignment,
	 * checks the associated evaluation state and permissions 
	 * to see if the assigned context can be removed
	 * 
	 * @param userId the internal user id (not username)
	 * @param assignContextId the id of an EvalAssignContext object
	 * @return true if the user can remove this context assignment, false otherwise
	 */
	public boolean canDeleteAssignContext(String userId, Long assignContextId);
}
