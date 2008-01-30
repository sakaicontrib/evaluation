/**
 * $Id$
 * $URL$
 * EvalEvaluationService.java - evaluation - Jan 28, 2008 5:30:21 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic;

import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplate;


/**
 * This contains simple logic for retrieving evaluations and evaluation related objects
 * like eval/assign groups and responses, the basic permissions handling is included as well<br/>
 * The more complex logic related to writing and deleting is in the higher level services 
 * (which include some passthroughs) like authoring, setup, and delivery
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface EvalEvaluationService {

   // EVALUATIONS

   /**
    * Get an evaluation based on its unique id
    * 
    * @param evaluationId the unique id of an {@link EvalEvaluation} object
    * @return the evaluation object or null if not found
    */   
   public EvalEvaluation getEvaluationById(Long evaluationId);

   /**
    * Check to see if an evaluation exists with the following id
    * 
    * @param evaluationId the unique id of an {@link EvalEvaluation} object
    * @return true if there is an evaluation with this id, false otherwise
    */
   public boolean checkEvaluationExists(Long evaluationId);

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

   // EVALUATION STATES

   /**
    * Find the current state (in queue, active, closed, etc.) 
    * of the supplied evaluation, this should be used before attempting to
    * delete or save an evaluation as the state determines the updates that
    * can be performed on the evaluation, this will also update the
    * state of the evaluation if the stored state does not match the actual
    * state as determined by dates
    * 
    * @param evaluationId the id of an EvalEvaluation object
    * @return an EVALUATION_STATE constant from 
    * {@link org.sakaiproject.evaluation.model.constant.EvalConstants}
    */
   public String updateEvaluationState(Long evaluationId);

   /**
    * Fixes the state of an evaluation (if needed) and saves it,
    * will not save state for a new evaluation, this is very fast
    * and cheap to call if you are not saving state and pretty cheap
    * even if you are, it will ONLY update when there is a need<br/>
    * <b>WARNING:</b> This is not really for general use but it needs to be
    * publicly exposed, use {@link #updateEvaluationState(Long)} instead
    * 
    * @param evaluation a persistent {@link EvalEvaluation} object
    * @param saveState if true, save the fixed eval state, else do not save
    * @return an EVALUATION_STATE constant from 
    * {@link org.sakaiproject.evaluation.model.constant.EvalConstants}
    */
   public String returnAndFixEvalState(EvalEvaluation evaluation, boolean saveState);

   // EVALUATION PERMISSIONS

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

   /**
    * Test if an evaluation can be controlled at this time by this user, 
    * this tests the dates of the evaluation against the user permissions,
    * ignores locking
    * 
    * @param userId the internal user id (not username)
    * @param evaluationId the id of an {@link EvalEvaluation} object
    * @return true if the evaluation can be modified, false otherwise
    */
   public boolean canControlEvaluation(String userId, Long evaluationId);

   /**
    * Test if an evaluation can be removed at this time by this user, 
    * this tests the dates of the evaluation against the removal logic
    * and the user permissions, takes locking into account (locked
    * evals cannot be removed normally)
    * 
    * @param userId the internal user id (not username)
    * @param evaluationId the id of an {@link EvalEvaluation} object
    * @return true if the evaluation can be removed, false otherwise
    */
   public boolean canRemoveEvaluation(String userId, Long evaluationId);


   // GROUPS (EVAL AND ASSIGN)

   /**
    * Get an assign group by its unique id,
    * should mostly be used to determine if an assign group id is valid and 
    * to get the evaluation id related to it<br/>
    * <b>NOTE:</b> If you need to get the {@link EvalGroup} then use
    * external logic
    * 
    * @param assignGroupId unique id for an {@link EvalAssignGroup} entity
    * @return an assign group entity or null if not found
    */
   public EvalAssignGroup getAssignGroupById(Long assignGroupId);

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
    * Get the unique id of an {@link EvalAssignGroup} based on the unique id
    * of an {@link EvalEvaluation} and an eval group id (the unique id
    * of an {@link EvalGroup})
    * 
    * @param evaluationId unique id of an {@link EvalEvaluation}
    * @param evalGroupId the internal unique ID for an evalGroup
    * @return a unique id for an {@link EvalAssignGroup} or null if it cannot be found for this evaluation
    */
   public Long getAssignGroupId(Long evaluationId, String evalGroupId);

   /**
    * Retrieve a single assign hierarchy item based on its uniqie id
    * @param assignHierarchyId unique id of {@link EvalAssignHierarchy} objects
    * @return the assigned hierarchy node or null if none found
    */
   public EvalAssignHierarchy getAssignHierarchyById(Long assignHierarchyId);

   /**
    * Get all the assigned hierarchy nodes by the evaluation they are associated with
    * @param evaluationId unique id of an {@link EvalEvaluation}, this should be used for
    * managing the list of assign hierarchies
    * @return a list of all the hierarchy assignments for an evaluation, empty if none found
    */
   public List<EvalAssignHierarchy> getAssignHierarchyByEval(Long evaluationId);

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
    * Get the list of assigned groups for an array of evaluation ids, this
    * is how the evaluation is tied to users (users are associated with a group),
    * same as {@link #getEvaluationGroups(Long[], boolean)} but returns {@link EvalAssignGroup}s
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

   // RESPONSES
   
   /**
    * Get a response by its unique id<br/>
    * A response represents a single user response to an evaluation in a specific evalGroupId<br/>
    * Note: this should mostly be used for OTP and not for normal fetching which
    * should use the other methods in this API
    * 
    * @param responseId the id of an EvalResponse object
    * @return an {@link EvalResponse} object or null if not found
    */
   public EvalResponse getResponseById(Long responseId);

   /**
    * Get the response for the supplied evaluation and group for the supplied user
    * 
    * @param evaluationId the unique id for an {@link EvalEvaluation}
    * @param userId the internal user id (not username)
    * @param evalGroupId the internal evalGroupId (represents a site or group)
    * @return an {@link EvalResponse} object or null if none exists
    */
   public EvalResponse getEvaluationResponseForUserAndGroup(Long evaluationId, String userId, String evalGroupId);

   /**
    * Get the responses for the supplied evaluations for this user<br/>
    * Note that this can return multiple responses in the case where an evaluation
    * is assigned to multiple groups that this user is a member of,
    * in the case where no user is supplied<br/>
    * <b>Note:</b> If you just need the response IDs then use the faster
    * {@link #getEvaluationResponseIds(String, Long[], String, Boolean)}
    * <b>Note:</b> If you just need the count then use the much faster
    * {@link #countEvaluationResponses(String, Long[], String, Boolean)}
    * 
    * @param userId the internal user id (not username),
    * if null or admin user then return responses for all users
    * @param evaluationIds an array of the ids of EvalEvaluation objects,
    * must contain at least one eval id,
    * NOTE: these ids are not validated
    * @param evalGroupIds an array of unique ids for eval groups, 
    * if null or empty then get all responses for all groups,
    * NOTE: these ids are not validated
    * @param completed if true only return the completed responses, 
    * if false only return the incomplete responses,
    * if null then return all responses
    * @return a List of {@link EvalResponse} objects
    */
   public List<EvalResponse> getEvaluationResponses(String userId, Long[] evaluationIds, String[] evalGroupIds, Boolean completed);

   /**
    * Count the number of responses for evaluations,
    * can count responses for an entire evaluation regardless of eval group
    * or just responses for a specific group and/or user<br/>
    * This is a good method for checking to see if a user has responded<br/>
    * <b>Note:</b> If you need the responses then use the slower
    * {@link #getEvaluationResponses(String, Long[], String[], Boolean)}
    * 
    * @param userId the internal user id (not username),
    * if null or admin user then return responses for all users
    * @param evaluationIds an array of the ids of EvalEvaluation objects,
    * must contain at least one eval id,
    * NOTE: these ids are not validated
    * @param evalGroupIds an array of unique ids for eval groups, 
    * if null or empty then get all responses for all groups,
    * NOTE: these ids are not validated
    * @return the count of associated responses
    */
   public int countEvaluationResponses(String userId, Long[] evaluationIds, String[] evalGroupIds, Boolean completed);

   /**
    * Get the response ids associated with an evaluation and particular eval groups,
    * you can choose to get all response ids or only the ones for complete/incomplete responses<br/>
    * <b>WARNING:</b> You should normally use {@link #getEvaluationResponses(String, Long[], String[], Boolean)} or
    * {@link #countEvaluationResponses(String, Long[], String[], Boolean)} instead of this
    * 
    * @param evaluationId the id of an EvalEvaluation object
    * @param evalGroupIds the internal eval group ids (represents a site or group),
    * if null or empty array, include count for all eval groups for this evaluation,
    * NOTE: these ids are not validated
    * @param completed if true only return the completed responses, if false only return the incomplete responses,
    * if null then return all responses
    * @return a list of response ids, in order by response id
    */
   public List<Long> getEvalResponseIds(Long evaluationId, String[] evalGroupIds, Boolean completed);

   /**
    * Get the answers associated with this item and with a response to this evaluation,
    * (i.e. item answers submitted as part of a response to the given evaluation) within
    * the given evalGroupsIds, only gets answers from completed responses
    * 
    * @param itemId the id of an {@link EvalItem} object
    * @param evaluationId the id of an {@link EvalEvaluation} object
    * @param evalGroupIds the internal eval group ids (represents a site or group),
    * if null, include count for all eval groups for this evaluation
    * @return a list of {@link EvalAnswer} objects
    */
   public List<EvalAnswer> getEvalAnswers(Long itemId, Long evaluationId, String[] evalGroupIds);


   // PERMISSIONS

   /**
    * Does a simple permission check to see if a user can modify a response, 
    * also checks the evaluation state (only active or due states allow modify)<br/> 
    * Does NOT check if the user can take this evaluation<br/>
    * <b>Note:</b> Responses can never be removed via the APIs<br/>
    * <b>Note:</b> Any checks to see if a user can
    * take an evaluation should be done with canTakeEvaluation() in
    * the EvalEvaluationsLogic API
    * 
    * @param userId the internal user id (not username)
    * @param responseId the id of an {@link EvalResponse} object
    * @return true if the user can modify this response, false otherwise
    */
   public boolean canModifyResponse(String userId, Long responseId);


}
