/******************************************************************************
 * EvalResponseLogic.java - created by aaronz@vt.edu on Dec 24, 2006
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

import org.sakaiproject.evaluation.model.EvalResponse;


/**
 * Handles all logic associated with responses and answers
 * (Note for developers - do not modify this without permission from the project lead)
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalResponsesLogic {

	/**
	 * Get the responses for the supplied evaluations for this user<br/>
	 * Note that this can return multiple responses in the case where an evaluation
	 * is assigned to multiple contexts that this user is part of, do not assume
	 * the order is always the same<br/>
	 * <b>Note:</b> If you just need the count then use the much faster
	 * {@link #countResponses(Long)}
	 * 
	 * @param userId the internal user id (not username)
	 * @param evaluationIds an array of the ids of EvalEvaluation objects
	 * @return a List of EvalResponse objects
	 */
	public List getEvaluationResponses(String userId, Long[] evaluationIds);

	/**
	 * Count the number of responses for an evaluation,
	 * can count responses for an entire evaluation regardless of context
	 * or just responses for a specific context
	 * 
	 * @param evaluationId the id of an EvalEvaluation object
	 * @param context the internal context (represents a site or group),
	 * if null, include count for all contexts
	 * @return the count of associated responses
	 */
	public int countResponses(Long evaluationId, String context);

	/**
	 * Get the answers associated with this item and with a response to this evaluation,
	 * (i.e. item answers submitted as part of a response to the given evaluation)
	 * 
	 * @param itemId the id of an EvalItem object
	 * @param evaluationId the id of an EvalEvaluation object
	 * @return a list of EvalAnswer objects
	 */
	public List getEvalAnswers(Long itemId, Long evaluationId);

	/**
	 * Saves a single response from a single user with all associated Answers,
	 * checks to make sure this user has not already saved this response or
	 * makes sure they are allowed to overwrite it, saves all associated
	 * answers at the same time to make sure the transaction succeeds<br/>
	 * This will also check to see if the user is allowed to save this response
	 * based on the current evaluation state, 
	 * also handles locking of associated evaluations<br/>
	 * When a user is taking an evaluation you should first create a response
	 * without any answers and with the end time set to null, once the user
	 * submits the evaluation you should save the response with the answers and
	 * with the end time set<br/>
	 * <b>Note:</b> You can only change the Answers and the start/end times when saving
	 * an existing response
	 * <b>Note:</b> You should set the end time to indicate that the response is
	 * complete, responses without an endtime are considered partially complete
	 * and should be ignored
	 * 
	 * @param response the response object to save, should be filled with answers
	 * @param userId the internal user id (not username)
	 */
	public void saveResponse(EvalResponse response, String userId);


	// PERMISSIONS

	/**
	 * Does a simple permission check to see if a user can modify a response, 
	 * also checks the evaluation state (only active or due states allow modify)<br/> 
	 * Does NOT check if the user can take this evaluation,
	 * use canTakeEvaluation in EvalEvaluationsLogic to check that<br/>
	 * <b>Note:</b> Responses can never be removed via the APIs<br/>
	 * <b>Note:</b> Any checks to see if a user can
	 * take an evaluation should be done with canTakeEvaluation() in
	 * the EvalEvaluationsLogic API
	 * 
	 * @param userId the internal user id (not username)
	 * @param responseId the id of an EvalResponse object
	 * @return true if the user can modify this response, false otherwise
	 */
	public boolean canModifyResponse(String userId, Long responseId);

}
