
/******************************************************************************
 * EvalEvaluationLogic.java - created by aaronz@vt.edu on Dec 24, 2006
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

import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;


/**
 * Handles all logic associated with processing Evaluations
 * (Note for developers - do not modify this without permission from the project lead)
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalEvaluationsLogic {

	/**
	 * Save or update an evaluation to persistent storage,
	 * checks that dates are appropriate and validates settings,
	 * use {@link #updateEvaluationState(Long)} to check the state
	 * if you want to avoid possible exceptions<br/>
	 * Evaluations can be saved with the email templates as null and will use the
	 * default templates in this circumstance<br/>
	 * <b>Note:</b> Do NOT attempt to save an evaluation with a null template
	 * or a template that contains no items<br/>
	 * <b>Note about dates</b>:<br/>
	 * Start date - eval becomes active on this date, cannot change start date once it passes, 
	 * most parts of evaluation cannot change on this date, no assigned contexts can be modified<br/>
	 * Due date - eval is reported to be closed after this date passes (interface and email), 
	 * cannot change due date once it passes, cannot assign new contexts once this passes<br/>
	 * Stop date - eval is actually closed after this date passes, cannot change stop date once it passes,
	 * no changes to evaluation after this date EXCEPT adjusting the view dates<br/>
	 * View date - eval results visible on this date<br/>
	 * (currently times are taken into account, so if you want to close an evaluation at the
	 * end of a date, make sure to set the time to midnight)
	 * 
	 * @param evaluation evaluation object to save
	 * @param userId the internal user id (not username)
	 */
	public void saveEvaluation(EvalEvaluation evaluation, String userId);

	/**
	 * Delete an evaluation from persistent storage,
	 * evaluations that are active or completed cannot be deleted,
	 * use {@link #canRemoveEvaluation(String, EvalEvaluation)} to check if
	 * the evaluation can be removed if you want to avoid possible exceptions,
	 * removes all associated course assignments and email templates 
	 * (if they are not default or associated with other evaluations)
	 * 
	 * @param evaluationId the id of an {@link EvalEvaluation} object
	 * @param userId the internal user id (not username)
	 */
	public void deleteEvaluation(Long evaluationId, String userId);
	
	/**
	 * Get a list of {@link EvalEvaluation} objects with selected property values
	 * Note: to be implemented
	 * 
	 * @param params EvalEvaluation property name and value
	 * @param userId the internal user id (not username)
	 * @return a List of {@link EvalEvaluation} objects (empty if none exist)
	 */
	public List<EvalEvaluation> getEvaluations(Map<String, Object> params, String userId);
	
	/**
	 * Get all {@link EvalEvaluation} objects
	 * 
	 * @param userId the internal user id (not username)
	 * @return a List of {@link EvalEvaluation} objects (empty if none exist)
	 */
	public List<EvalEvaluation> getEvaluations(String userId);
	
	/**
	 * Get the evaluation associated with this external id<br/>
	 * Note: An evaluation eid is null except when the evaluation
	 * was imported from an external system.
	 * 
	 * @param eid the id of an evaluation in an external system
	 * @return the evaluation object or null if not found
	 */
	public EvalEvaluation getEvaluationByEid(String eid);
	
	/**
	 * Get all {@link EvalEvaluation} objects for a group
	 * 
	 * @param groupId the group Id
	 * @return a List of {@link EvalEvaluation} objects (empty if none exist)
	 */
	public List<EvalEvaluation> getEvaluationsByGroupId(String groupId);

	/**
	 * Get an evaluation based on its unique id
	 * 
	 * @param evaluationId the unique id of an {@link EvalEvaluation} object
	 * @return the evaluation object or null if not found
	 */	
	public EvalEvaluation getEvaluationById(Long evaluationId);

	/**
	 * Get a list of evaluations for a template id
	 * 
	 * @param templateId the id of an {@link EvalTemplate} object
	 * @return a List of {@link EvalEvaluation} objects (empty if none exist)
	 */
	public List<EvalEvaluation> getEvaluationsByTemplateId(Long templateId);

	/**
	 * Count the number of evaluations for a template id
	 * 
	 * @param templateId the id of an {@link EvalTemplate} object
	 * @return the count of {@link EvalEvaluation} objects
	 */
	public int countEvaluationsByTemplateId(Long templateId);
	
	/**
	 * Count the number of evaluations with state equal to EvalConstants.EVALUATION_STATE_ACTIVE
	 * 
	 * @return the count of {@link EvalEvaluation} objects
	 */
	public int countActiveEvaluations();

	/**
	 * Get the evaluations that are currently visible to a user, this should be used
	 * to determine evaluations that are visible from an administrative perspective,
	 * can limit to recently closed only (closed within 10 days)
	 * 
	 * @param userId the internal user id (not username)
	 * @param recentOnly if true return recently closed evaluations only 
	 * (still returns all active and in queue evaluations), if false return all closed evaluations
	 * @param showNotOwned if true for a non-admin user, then return all 
	 * evaluations which are both owned and not-owned, else only return the owned evaluations.
	 * @return a List of {@link EvalEvaluation} objects
	 */
	public List<EvalEvaluation> getVisibleEvaluationsForUser(String userId, boolean recentOnly, boolean showNotOwned);

	/**
	 * Get all evaluations that can be taken by this user,
	 * can include only active and only untaken if desired
	 * 
	 * @param userId the internal user id (not username)
	 * @param activeOnly if true, only include active evaluations, if false, include all evaluations
	 * @param untakenOnly if true, include only the evaluations which have NOT been taken, 
	 * if false, include all evaluations
	 * @return a List of {@link EvalEvaluation} objects (sorted by DueDate)
	 */
	public List<EvalEvaluation> getEvaluationsForUser(String userId, boolean activeOnly, boolean untakenOnly);
	
	/**
	 * Get the evaluation ids for evaluations for which available email has or has not been sent
	 * 
	 * @param availableEmailSent if Boolean.TRUE return count of evaluations for which available email has been sent,
	 * if Boolean.FALSE return count of evaluations for which available email has not been sent, and if null return
	 * a count of all active evaluations
	 * @return the evaluations meeting the criterion
	 */
	public Long[] getActiveEvaluationIdsByAvailableEmailSent(Boolean availableEmailSent);


	// EVAL GROUPS

	/**
	 * Get a map of the {@link EvalGroup}s for an array of evaluation ids, this
	 * is how the evaluation is tied to users (users are associated with a group)
	 * 
	 * @param evaluationIds an array of the ids of {@link EvalEvaluation} objects
	 * @param includeUnApproved if true, include the evaluation contexts which have not been instructor approved yet,
	 * you should not include these when displaying evaluations to users to take or sending emails
	 * @return a Map of evaluationId (Long) -> List of {@link EvalGroup} objects
	 */
	public Map<Long, List<EvalGroup>> getEvaluationGroups(Long[] evaluationIds, boolean includeUnApproved);

	/**
	 * Get the list of assigned groups for an evaluation id, this
	 * is how the evaluation is tied to users (users are associated with a group)
	 * 
	 * @param evaluationIds an array of the ids of {@link EvalEvaluation} objects
	 * @param includeUnApproved if true, include the evaluation contexts which have not been instructor approved yet,
	 * you should not include these when displaying evaluations to users to take or sending emails
	 * @return a Map of evaluationId (Long) -> List of {@link EvalAssignGroup} objects
	 */
	public Map<Long, List<EvalAssignGroup>> getEvaluationAssignGroups(Long[] evaluationIds, boolean includeUnApproved);

	/**
	 * Count the number of eval groups assigned for an evaluation id
	 * (this is much faster than the related method: {@link #getEvaluationGroups(Long[], boolean)})
	 * 
	 * @param evaluationId the id of an {@link EvalEvaluation} object
	 * @return the count of eval groups
	 */
	public int countEvaluationGroups(Long evaluationId);


	// PERMISSIONS

	/**
	 * Check if this user can begin a new evaluation (administratively), 
	 * this checks if this user can access any templates and
	 * also checks if they have permission to begin an evaluation in any contexts<br/>
	 * <b>Note:</b> this is an expensive check so be careful when using it,
	 * Only includes non-empty templates
	 * 
	 * @param userId the internal user id (not username)
	 * @return true if the user can begin an evaluation, false otherwise
	 */
	public boolean canBeginEvaluation(String userId);

	/**
	 * Find the current state (in queue, active, closed, etc.) 
	 * of the supplied evaluation, this should be used before attempting to
	 * delete or save an evaluation as the state determines the updates that
	 * can be performed on the evaluation, this will also update the
	 * state of the evaluation if the stored state does not match the actual
	 * state as determined by dates
	 * 
	 * @param evaluationId the id of an EvalEvaluation object
	 * @param userId the internal user id (not username)
	 * @return an EVALUATION_STATE constant from 
	 * {@link org.sakaiproject.evaluation.model.constant.EvalConstants}
	 */
	public String updateEvaluationState(Long evaluationId);

	/**
	 * Test if an evaluation can be removed at this time by this user, 
	 * this tests the dates of the evaluation against the removal logic
	 * and the user permissions
	 * 
	 * @param userId the internal user id (not username)
	 * @param evaluationId the id of an EvalEvaluation object
	 * @return true if the evaluation can be removed, false otherwise
	 */
	public boolean canRemoveEvaluation(String userId, Long evaluationId);

	/**
	 * Check if a user can take an evaluation in the supplied evalGroupId,
	 * the check includes testing if an entry exists already and if the
	 * user is allowed to modify their entry<br/>
	 * This check should be used on any page which presents the user with
	 * an evaluation to take (fill out)<br/>
	 * This will also do a simpler check to see if a user can take an evaluation
	 * without knowing the group (simply leave evalGroupId null), this check will
	 * only tell you if the user is in at least one valid group for this evaluation
	 * <br/> 
	 * Use {@link #getEvaluationsForUser(String, boolean, boolean)} if you are trying
	 * to determine which "take evaluation" links to show a user
	 * 
	 * @param userId the internal user id (not username)
	 * @param evaluationId unique id of the evaluation
	 * @param evalGroupId the internal evalGroupId (represents a site or group), 
	 * can be null if you want to do a simpler check for the user taking this evaluation (less efficient)
	 * @return true if the user can take the evaluation, false otherwise
	 */
	public boolean canTakeEvaluation(String userId, Long evaluationId, String evalGroupId);


	// EVAL CATEGORIES

	/**
	 * Get all current evalaution cateogries in the system,
	 * evaluation categories allow the evaluation owner to categorize their evaluations
	 * 
	 * @param userId the internal user id (not username), may be null, if not null then only
	 * get the categories for evaluations owned by this user (i.e. categories they created)
	 * @return an array of categories, sorted in alphabetic order
	 */
	public String[] getEvalCategories(String userId);

	/**
	 * Get all evaluations which are tagged with a specific category
	 * 
	 * @param evalCategory a string representing an evaluation category
	 * @param userId the internal user id (not username), may be null, if not null then only
	 * get the evaluations in this category which are accessible to this user
	 * @return a list of {@link EvalEvaluation} objects
	 */
	public List<EvalEvaluation> getEvaluationsByCategory(String evalCategory, String userId);

}
