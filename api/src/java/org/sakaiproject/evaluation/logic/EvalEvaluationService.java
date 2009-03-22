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
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
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

    public static final String STATUS_ANY = "*";

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
     * Get a list of evaluationSetupService for a template id
     * 
     * @param templateId the id of an {@link EvalTemplate} object
     * @return a List of {@link EvalEvaluation} objects (empty if none exist)
     */
    public List<EvalEvaluation> getEvaluationsByTemplateId(Long templateId);

    /**
     * Count the number of evaluationSetupService for a template id
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
     * {@link org.sakaiproject.evaluation.constant.EvalConstants}
     */
    public String updateEvaluationState(Long evaluationId);

    /**
     * Fixes the state of an evaluation (if needed) and saves it,
     * will not save state for a new evaluation, this is very fast
     * and cheap to call if you are not saving state and pretty cheap
     * even if you are, it will ONLY update when there is a need<br/>
     * <b>NOTE:</b> this will not change the state if it is currently a special
     * state: {@link EvalConstants#EVALUATION_STATE_PARTIAL} or {@link EvalConstants#EVALUATION_STATE_DELETED}<br/>
     * <b>WARNING:</b> This is not really for general use but it needs to be
     * publicly exposed, use {@link #updateEvaluationState(Long)} instead
     * 
     * @param evaluation a persistent {@link EvalEvaluation} object
     * @param saveState if true, save the fixed eval state, else do not save,
     * this will not save the state if this evaluation is new and has never been saved
     * @return an EVALUATION_STATE constant from 
     * {@link org.sakaiproject.evaluation.constant.EvalConstants}
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
     * Use {@link #getEvaluationsForUser(String, Boolean, Boolean, Boolean)} if you are trying
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


    // USER ASSIGNMENTS

    /**
     * Get the list of all participants for an evaluation,
     * can limit it to a single group which is assigned to the evaluation and
     * can filter the results to only include some of the participants,
     * this should be used in all cases where  <br/>
     * Will not include any assignments with {@link EvalAssignUser#STATUS_REMOVED}
     * <br/>
     * You must include at least one of the following (non-null):
     * evaluationId OR userId
     * <br/> Uses the current user for permissions checks
     * 
     * @param evaluationId (OPTIONAL) the unique id of an {@link EvalEvaluation} object,
     * if this is null then assignments from any evaluation are returned
     * @param userId (OPTIONAL) limit the returned assignments to those for this user,
     * will return assignments for any user if this is null
     * @param evalGroupIds (OPTIONAL) an array of unique IDs for eval groups, 
     * if this is null or empty then results include participants from the entire evaluation,
     * NOTE: these ids are not validated
     * @param assignTypeConstant (OPTIONAL) a constant to indicate which types of assignment participants to include,
     * use the TYPE_* constants from {@link EvalAssignUser}, default (null) is to include all types of assignments
     * @param assignStatusConstant (OPTIONAL) a constant to indicate which status of assignment participants to include,
     * use the STATUS_* constants from {@link EvalAssignUser}, to include users with any status use {@link #STATUS_ANY}, 
     * default (null) is to include {@link EvalAssignUser#STATUS_LINKED} and {@link EvalAssignUser#STATUS_UNLINKED},
     * @param includeConstant (OPTIONAL) a constant to indicate what users should be retrieved, 
     * EVAL_INCLUDE_* from {@link EvalConstants}, default (null) is {@link EvalConstants#EVAL_INCLUDE_ALL},
     * <b>NOTE</b>: if this is non-null it will filter users to type {@link EvalAssignUser#TYPE_EVALUATOR} automatically
     * regardless of what the assignTypeConstant is set to
     * @param evalStateConstant (OPTIONAL) this is the state of the evals to limit the results to,
     * this should be one of the EVALUATION_STATE_* constants (e.g. {@link EvalConstants#EVALUATION_STATE_ACTIVE}),
     * if null then evaluations with any state are included
     * @return the list of user assignments ({@link EvalAssignUser} objects)
     * @throws IllegalArgumentException if all inputs are null or the inputs are invalid
     */
    public List<EvalAssignUser> getParticipantsForEval(Long evaluationId, String userId, String[] evalGroupIds, String assignTypeConstant, String assignStatusConstant, String includeConstant, String evalStateConstant);

    /**
     * Gets the total count of evaluator participants for an evaluation (will not include evaluatee or assistants) <br/>
     * Convenience method related to {@link #getParticipantsForEval(Long, String, String, String, String, String, String)} <br/>
     * <b>NOTE:</b> always returns 0 if the evaluation is anonymous
     *
     * @param evaluationId the id of an {@link EvalEvaluation} object
     * @param evalGroupIds an array of unique IDs for eval groups, 
     * if this is null or empty then results include participants from the entire evaluation,
     * NOTE: these ids are not validated
     * @return total number of participants which are taking this evaluation (unless anonymous, then 0)
     */
    public int countParticipantsForEval(Long evaluationId, String[] evalGroupIds);

    /**
     * Get the list of users who are taking an evaluation in a specific group
     * or leave out the group to get all users in the evaluation
     * 
     * @param evaluationId the unique id of an {@link EvalEvaluation} object
     * @param evalGroupId the internal unique ID for an evalGroup,
     * leave this null to include takers from the entire evaluation
     * @param includeConstant a constant to indicate what users should be retrieved, EVAL_INCLUDE_* from {@link EvalConstants}
     * @return a set of userIds (internal IDs)
     * @deprecated use {@link #getParticipantsForEval(Long, String, String, String, String, String, String)}
     */
    public Set<String> getUserIdsTakingEvalInGroup(Long evaluationId, String evalGroupId, String includeConstant);

    /**
     * Get an assign user by its unique id,
     * should mostly be used to determine if an assign user id is valid and 
     * to get the evaluation id related to it <br/>
     * <b>NOTE:</b> If you need to get the {@link EvalUser} then use common logic
     * 
     * @param assignUserId unique id for an {@link EvalAssignUser} entity
     * @return an assigned user object OR null if not found
     */
    public EvalAssignUser getAssignUserById(Long assignUserId);

    /**
     * Get the assign user associated with this external id<br/>
     * Note: An assign group eid is null except when the assignGroup
     * was imported from an external system.
     * <b>NOTE:</b> If you need to get the {@link EvalUser} then use common logic
     * 
     * @param eid the id of an assigned user in an external system
     * @return the assigned user object OR null if not found
     */
    public EvalAssignUser getAssignUserByEid(String eid);


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
    public EvalAssignGroup getAssignGroupByEvalAndGroupId(Long evaluationId, String evalGroupId);

    /**
     * Retrieve a single assign hierarchy item based on its uniqie id
     * @param assignHierarchyId unique id of {@link EvalAssignHierarchy} objects
     * @return the assigned hierarchy node or null if none found
     */
    public EvalAssignHierarchy getAssignHierarchyById(Long assignHierarchyId);

    /**
     * Get all the assigned hierarchy nodes by the evaluation they are associated with,
     * combine this with {@link #getAssignGroupsForEvals(Long[], boolean, Boolean)} to list the groups
     * which are associated with these nodes
     * 
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
     * @param includeHierarchyGroups if true then all groups which were added because a node was added will be included,
     * if false then only groups which were added directly will be included, 
     * if null then all (directly added and node added groups) will be included
     * @return a Map of evaluationId (Long) -> List of {@link EvalGroup} objects
     */
    public Map<Long, List<EvalGroup>> getEvalGroupsForEval(Long[] evaluationIds, boolean includeUnApproved, Boolean includeHierarchyGroups);

    /**
     * Get the list of assigned groups for an array of evaluation ids, this
     * is how the evaluation is tied to users (users are associated with a group),
     * same as {@link #getEvalGroupsForEval(Long[], boolean, Boolean)} but returns {@link EvalAssignGroup}s
     * 
     * @param evaluationIds an array of the ids of {@link EvalEvaluation} objects
     * @param includeUnApproved if true, include the evaluation groups which have not been instructor approved yet,
     * you should not include these when displaying evaluations to users to take or sending emails
     * @param includeHierarchyGroups if true then all groups which were added because a node was added will be included,
     * if false then only groups which were added directly will be included,
     * if null then all (directly added and node added groups) will be included
     * @return a Map of evaluationId (Long) -> List of {@link EvalAssignGroup} objects
     */
    public Map<Long, List<EvalAssignGroup>> getAssignGroupsForEvals(Long[] evaluationIds, boolean includeUnApproved, Boolean includeHierarchyGroups);

    /**
     * Count the number of eval groups assigned for an evaluation id
     * (this is much faster than the related method: {@link #getEvalGroupsForEval(Long[], boolean, Boolean)})
     * 
     * @param evaluationId the id of an {@link EvalEvaluation} object
     * @param includeUnApproved if true, include the evaluation groups which have not been instructor approved yet,
     * you should not include these when displaying evaluations to users to take or sending emails
     * @return the count of eval groups
     */
    public int countEvaluationGroups(Long evaluationId, boolean includeUnApproved);

    /**
     * Check if a GroupId is assigned to an evaluation<br/>
     * UMD - JIRA http://jira.sakaiproject.org/jira/browse/EVALSYS-588
     *
     * @param evaluationId unique id of an {@link EvalEvaluation}
     * @param evalGroupId the internal unique ID for an evalGroup
     * @return true if the evalGroupId is in the eval, otherwise return false
     */
    public boolean isEvalGroupValidForEvaluation(String evalGroupId, Long evaluationId);


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
    public EvalResponse getResponseForUserAndGroup(Long evaluationId, String userId, String evalGroupId);

    /**
     * Get the responses for the supplied evaluationSetupService for this user<br/>
     * Note that this can return multiple responses in the case where an evaluation
     * is assigned to multiple groups that this user is a member of,
     * in the case where no user is supplied<br/>
     * <b>Note:</b> If you just need the response IDs then use the faster
     * {@link #getEvaluationResponseIds(String, Long[], String, Boolean)}
     * <b>Note:</b> If you just need the count then use the much faster
     * {@link #countResponses(String, Long[], String, Boolean)}
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
    public List<EvalResponse> getResponses(String userId, Long[] evaluationIds, String[] evalGroupIds, Boolean completed);

    /**
     * Count the number of responses for evaluationSetupService,
     * can count responses for an entire evaluation regardless of eval group
     * or just responses for a specific group and/or user<br/>
     * This is a good method for checking to see if a user has responded<br/>
     * <b>Note:</b> If you need the responses then use the slower
     * {@link #getResponses(String, Long[], String[], Boolean)}
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
    public int countResponses(String userId, Long[] evaluationIds, String[] evalGroupIds, Boolean completed);

    /**
     * Get the response ids associated with an evaluation and particular eval groups,
     * you can choose to get all response ids or only the ones for complete/incomplete responses<br/>
     * <b>WARNING:</b> You should normally use {@link #getResponses(String, Long[], String[], Boolean)} or
     * {@link #countResponses(String, Long[], String[], Boolean)} instead of this
     * 
     * @param evaluationId the id of an EvalEvaluation object
     * @param evalGroupIds the internal eval group ids (represents a site or group),
     * if null or empty array, include count for all eval groups for this evaluation,
     * NOTE: these ids are not validated
     * @param completed if true only return the completed responses, if false only return the incomplete responses,
     * if null then return all responses
     * @return a list of response ids, in order by response id
     */
    public List<Long> getResponseIds(Long evaluationId, String[] evalGroupIds, Boolean completed);


    // PERMISSIONS

    /**
     * Does a simple permission check to see if a user can modify a response, 
     * also checks the evaluation state (only active or due states allow modify)<br/> 
     * Does NOT check if the user can take this evaluation<br/>
     * <b>Note:</b> Responses can never be removed via the APIs<br/>
     * <b>Note:</b> Any checks to see if a user can
     * take an evaluation should be done with canTakeEvaluation() in
     * the EvalEvaluationSetupService API
     * 
     * @param userId the internal user id (not username)
     * @param responseId the id of an {@link EvalResponse} object
     * @return true if the user can modify this response, false otherwise
     */
    public boolean canModifyResponse(String userId, Long responseId);



    // EMAIL TEMPLATES

    /**
     * Get a default email template by type, use the defaults as the basis for all
     * new templates that are created by users
     * 
     * @param emailTemplateTypeConstant a constant, use the EMAIL_TEMPLATE constants from 
     * {@link org.sakaiproject.evaluation.constant.EvalConstants} to indicate the type
     * @return the default email template matching the supplied type
     * @throws IllegalArgumentException if the template cannot be found
     */
    public EvalEmailTemplate getDefaultEmailTemplate(String emailTemplateTypeConstant);

    /**
     * Get an email template for an eval by type, will always return an email template
     * 
     * @param emailTemplateTypeConstant a constant, use the EMAIL_TEMPLATE constants from 
     * {@link org.sakaiproject.evaluation.constant.EvalConstants} to indicate the type
     * @return the email template of the supplied type for this eval
     */
    public EvalEmailTemplate getEmailTemplate(Long evaluationId, String emailTemplateTypeConstant);

    /**
     * Get an email template by its unique id
     * 
     * @param emailTemplateId a unique id for an {@link EvalEmailTemplate}
     * @return an email template or null if none found
     */
    public EvalEmailTemplate getEmailTemplate(Long emailTemplateId);

    /**
     * Get the email templates accessible to this user (with or without default templates)
     * 
     * @param userId the internal user id (not username)
     * @param emailTemplateTypeConstant a constant, use the EMAIL_TEMPLATE constants from 
     * {@link org.sakaiproject.evaluation.constant.EvalConstants} to indicate the type
     * @param includeDefaultsOnly if true then only default templates are returned (always visible to everyone),
     * if false then they are not included, if null then we get custom visible templates and default ones as well
     * @return a list of email templates
     */
    public List<EvalEmailTemplate> getEmailTemplatesForUser(String userId, String emailTemplateTypeConstant, Boolean includeDefaultsOnly);

    // PERMISSIONS

    /**
     * Check if a user can control (create, modify, or delete) an email template for the
     * given evaluation of the given template type, takes into account the permissions and 
     * current state of the evaluation
     * 
     * @param userId the internal user id (not username)
     * @param evaluationId the id of an EvalEvaluation object
     * @param emailTemplateTypeConstant a constant, use the EMAIL_TEMPLATE constants from 
     * {@link org.sakaiproject.evaluation.constant.EvalConstants} to indicate the type
     * @return true if the user can control the email template at this time, false otherwise
     */
    public boolean canControlEmailTemplate(String userId, Long evaluationId, String emailTemplateTypeConstant);

    /**
     * Check if a user can control (modify or delete) a given 
     * email template for the given evaluation,
     * takes into account the ownership, permissions, and current state of the evaluation
     * 
     * @param userId the internal user id (not username)
     * @param evaluationId the id of an EvalEvaluation object,
     * leave this null if the check is a general one and not evaluation related
     * @param emailTemplateId the id of an EvalEmailTemplate object
     * @return true if the user can control the email template at this time, false otherwise
     */
    public boolean canControlEmailTemplate(String userId, Long evaluationId, Long emailTemplateId);

}
