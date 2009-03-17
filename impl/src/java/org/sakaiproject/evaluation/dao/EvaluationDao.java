
package org.sakaiproject.evaluation.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.genericdao.api.GeneralGenericDao;

/**
 * Do NOT use this class outside the LOGIC layer
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface EvaluationDao extends GeneralGenericDao {

    /**
     * This method will check the database for inconsistencies (mostly as a result of upgrades),
     * and will apply any fixes that it can 
     */
    public void fixupDatabase();

    /**
     * Method to find all evals which have no user assignments
     * @return the list of all evaluations without any user assignments
     */
    public List<EvalEvaluation> getEvalsWithoutUserAssignments();

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
    public List<EvalAssignUser> getParticipantsForEval(Long evaluationId, String userId,
            String[] evalGroupIds, String assignTypeConstant, String assignStatusConstant, 
            String includeConstant, String evalStateConstant);

    /**
     * Returns all evaluations which the given user can take,
     * can also include anonymous evaluations and filter on active/approved
     * 
     * @param userId the internal user id for the user who we are checking evals they can take
     * @param activeOnly if true, only include active evaluations, 
     * if false only include inactive (inqueue, graceperiod, closed, viewable), 
     * if null, include all evaluations (except partial and deleted)
     * @param approvedOnly if true, include the evaluations for groups which have been instructor approved only,
     * if false, include evals for groups which have not been approved only,
     * if null, include approved and unapproved,
     * NOTE: you should not include unapproved when displaying evaluations to users to take or sending emails
     * @param includeAnonymous if true, only include evaluations authcontrol = anon, 
     * if false, include any evaluations with authcontrol != anon,
     * if null, include all evaluations regardless of authcontrol
     * @return a List of EvalEvaluation objects sorted by due date, title, and id
     */
    public List<EvalEvaluation> getEvalsUserCanTake(String userId, Boolean activeOnly,
            Boolean approvedOnly, Boolean includeAnonymous, int startResult, int maxResults);

    /**
     * A general method for fetching entities which are shared for a specific user,
     * this is abstracting the idea of ((private & owner) or (public)) and (other options)
     * 
     * @param <T>
     * @param entityClass the class of the entity to be retrieved
     * @param userId the internal user Id (of the owner),
     * null userId means return all private templates,
     * has no effect if private constant is not included in the sharingConstants list
     * @param sharingConstants an array of SHARING_ constants from {@link EvalConstants},
     * this cannot be null or empty
     * @param props an array of extra properties to compare to values
     * @param values an array of extra values
     * @param comparisons an array of extra comparisons
     * @param order a string array of property names to order by
     * @param options extra options which are specially handled: 
     * notHidden for scales/items/TIs/templates,
     * notEmpty for templates
     * @param start the returned entity to start with (for paging), 0 means start with the first one
     * @param limit the total number of entities to return, 0 means return all
     * @return a list of entities
     */
    public <T> List<T> getSharedEntitiesForUser(Class<T> entityClass, String userId, String[] sharingConstants,
            String[] props, Object[] values, int[] comparisons, String[] order, String[] options, int start, int limit);

    /**
     * A general method for counting entities which are shared for a specific user,
     * this is abstracting the idea of ((private & owner) or (public)) and (other options)
     * 
     * @param <T>
     * @param entityClass the class of the entity to be retrieved
     * @param userId the internal user Id (of the owner),
     * null userId means return all private templates,
     * has no effect if private constant is not included in the sharingConstants list
     * @param sharingConstants an array of SHARING_ constants from {@link EvalConstants},
     * this cannot be null or empty
     * @param props an array of extra properties to compare to values
     * @param values an array of extra values
     * @param comparisons an array of extra comparisons
     * @param options extra options which are specially handled: 
     * notHidden for scales/items/TIs/templates,
     * notEmpty for templates
     * @return a count of the matching entities
     * @see #getSharedEntitiesForUser(Class, String, String[], String[], Object[], int[], String[], String[])
     */
    public <T> int countSharedEntitiesForUser(Class<T> entityClass, String userId, String[] sharingConstants,
            String[] props, Object[] values, int[] comparisons, String[] options);

    /**
     * Returns all evaluation objects associated with the input groups,
     * can also include anonymous evaluations
     * 
     * @param evalGroupIds an array of eval group IDs to get associated evals for, 
     * can be empty or null but only anonymous evals will be returned
     * @param activeOnly if true, only include active evaluations, 
     * if false only include inactive (inqueue, graceperiod, closed, viewable), 
     * if null, include all evaluations (except partial and deleted)
     * @param approvedOnly if true, include the evaluations for groups which have been instructor approved only,
     * if false, include evals for groups which have not been approved only,
     * if null, include approved and unapproved,
     * NOTE: you should not include unapproved when displaying evaluations to users to take or sending emails
     * @param includeAnonymous if true, include assigned and anonymous evaluations (only anonymous evals if evalGroupIds is null), 
     * if false, only include assigned evals which are not also anonymous,
     * if null include only assigned evaluations
     * @param startResult 0 to start with the first result, otherwise start with this result number
     * @param maxResults 0 to return all results, otherwise limit the number of evals returned to this
     * @return a List of EvalEvaluation objects sorted by due date, title, and id
     */
    public List<EvalEvaluation> getEvaluationsByEvalGroups(String[] evalGroupIds,
            Boolean activeOnly, Boolean approvedOnly, Boolean includeAnonymous, int startResult, int maxResults);

    /**
     * Get a set of evaluations based on the owner and their groups
     * 
     * @param userId internal user id, owner of the evaluations, if null then do not filter on the owner id
     * @param evalGroupIds an array of eval group IDs to get associated evals for, can be empty or null to get all evals
     * @param recentClosedDate only return evaluations which closed after this date
     * @param startResult 0 to start with the first result, otherwise start with this result number
     * @param maxResults 0 to return all results, otherwise limit the number of evals returned to this
     * @param includePartial if true then partial evals are included, otherwise only fully created evals
     * @return a List of EvalEvaluation objects sorted by stop date, title, and id
     */
    public List<EvalEvaluation> getEvaluationsForOwnerAndGroups(String userId,
            String[] evalGroupIds, Date recentClosedDate, int startResult, int maxResults, boolean includePartial);

    /**
     * Returns all answers to the given item associated with 
     * responses which are associated with the given evaluation,
     * only returns the answers for completed responses
     * 
     * @param evalId the id of the evaluation you want answers from
     * @param evalGroupIds an array of eval group IDs to return answers for,
     * if null then just return answers for all groups
     * @param templateItemIds the ids of the template items you want answers for,
     * if null then return answers for all template items
     * @return a list of EvalAnswer objects or empty list if none found
     */
    public List<EvalAnswer> getAnswers(Long evalId, String[] evalGroupIds, Long[] templateItemIds);

    /**
     * Removes a group of templateItems and updates all related items 
     * and templates at the same time (inside one transaction)
     * 
     * @param templateItems the array of {@link EvalTemplateItem} to remove 
     */
    public void removeTemplateItems(EvalTemplateItem[] templateItems);

    /**
     * Get item groups contained within a specific group<br/>
     * <b>Note:</b> If parent is null then get all the highest level groups
     * 
     * @param parentItemGroupId the unique id of an {@link EvalItemGroup}, if null then get all the highest level groups
     * @param userId the internal user id (not username)
     * @param includeEmpty if true then include all groups (even those with nothing in them), else return only groups
     * which contain other groups or other items
     * @param includeExpert if true then include expert groups only, else include non-expert groups only
     * @return a List of {@link EvalItemGroup} objects, ordered by title alphabetically
     */
    public List<EvalItemGroup> getItemGroups(Long parentItemGroupId, String userId,
            boolean includeEmpty, boolean includeExpert);

    /**
     * Get all the templateItems for this template limited by the various hierarchy
     * settings specified, always returns the top hierarchy level set of items,
     * will include the template items limited by the various hierarchy levels and
     * ids of the parts of the nodes
     * 
     * @param templateId the unique id of an EvalTemplate object
     * @param nodeIds the unique ids of a set of hierarchy nodes for which we 
     * want all associated template items, null excludes all TIs associated with nodes,
     * an empty array will include all TIs associated with nodes
     * @param instructorIds a set of internal userIds of instructors for instructor added items,
     * null will exclude all instructor added items, empty array will include all
     * @param groupIds the unique eval group ids associated with a set of TIs in this template
     * (typically items which are associated with a specific eval group),
     * null excludes all associated TIs, empty array includes all 
     * @return a list of {@link EvalTemplateItem} objects, ordered by displayOrder
     */
    public List<EvalTemplateItem> getTemplateItemsByTemplate(Long templateId, String[] nodeIds,
            String[] instructorIds, String[] groupIds);

    /**
     * Get all the templateItems for this evaluation limited by the various hierarchy
     * settings specified, always returns the top hierarchy level set of items,
     * will include the template items limited by the various hierarchy levels and
     * ids of the parts of the nodes, should be ordered in the list by the proper display order
     * 
     * @param evalId the unique id of an {@link EvalEvaluation} object
     * @param nodeIds the unique ids of a set of hierarchy nodes for which we 
     * want all associated template items, null excludes all TIs associated with nodes,
     * an empty array will include all TIs associated with nodes
     * @param instructorIds a set of internal userIds of instructors for instructor added items,
     * null will exclude all instructor added items, empty array will include all
     * @param groupIds the unique eval group ids associated with a set of TIs in this template
     * (typically items which are associated with a specific eval group),
     * null excludes all associated TIs, empty array includes all 
     * @return a list of {@link EvalTemplateItem} objects, ordered by displayOrder and template
     */
    public List<EvalTemplateItem> getTemplateItemsByEvaluation(Long evalId, String[] nodeIds,
            String[] instructorIds, String[] groupIds);

    /**
     * Returns list of response ids for a given evaluation
     * and for specific groups and for specific users if desired,
     * can limit to only completed responses
     *
     * @param evalId the id of the evaluation you want the response ids for
     * @param evalGroupIds an array of eval group IDs to return response ids for,
     * if null or empty then return responses ids for all evalGroups associated with this eval
     * @param userIds an array of internal userIds to return responses for,
     * if null or empty then return responses ids for all users
     * @param completed if true only return the completed responses, 
     * if false only return the incomplete responses,
     * if null then return all responses
     * @return a list of response ids (Long) for {@link EvalResponse} objects
     */
    public List<Long> getResponseIds(Long evalId, String[] evalGroupIds, String[] userIds,
            Boolean completed);

    /**
     * Removes an array of responses and all their associated answers at
     * the same time (in a single transaction)<br/>
     * Use {@link #getResponseIds(Long, String[], String[], Boolean)} to get the set of responseIds to remove<br/>
     * <b>WARNING:</b> This does not check permissions for removal of responses so you should
     * be sure to check that responses can be removed (system setting) and that they can be removed for this evaluation and user
     * 
     * @param responseIds the array of ids for {@link EvalResponse} objects to remove
     * @throws Exception if there is a failure
     */
    public void removeResponses(Long[] responseIds);

    /**
     * Get a list of evaluation categories
     * 
     * @param userId the internal user id (not username), if null then return all categories
     * @return a list of {@link String}
     */
    public List<String> getEvalCategories(String userId);

    /**
     * Get the node which contains this evalgroup,
     * Note: this will always only return a single node so if an evalgroup is assigned to multiple
     * nodes then only the first one will be returned
     * @param evalGroupId a unique id for an eval group
     * @return a unique id for the containing node or null if none found
     */
    public String getNodeIdForEvalGroup(String evalGroupId);

    /**
     * Get all the users who have completely responded to an evaluation 
     * and optionally within group(s) assigned to that evaluation
     * 
     * @param evaluationId a unique id for an {@link EvalEvaluation}
     * @param evalGroupIds the unique eval group ids associated with this evaluation, 
     * can be null or empty to get all responses for this evaluation
     * @return a set of internal userIds
     */
    public Set<String> getResponseUserIds(Long evaluationId, String[] evalGroupIds);

    /**
     * Get all the evalGroupIds for an evaluation which are viewable by
     * the input permission,
     * can limit the eval groups to check by inputing an array of evalGroupIds<br/>
     * <b>NOTE:</b> If you input evalGroupIds then the returned set will always be
     * a subset (the same size or smaller) of the input
     * 
     * @param evaluationId a unique id for an {@link EvalEvaluation}
     * @param permissionConstant a permission constant which is 
     * {@link EvalConstants#PERM_BE_EVALUATED} for instructors/evaluatees OR
     * {@link EvalConstants#PERM_TAKE_EVALUATION} for students/evaluators,
     * other permissions will return no results
     * @param evalGroupIds the unique eval group ids associated with this evaluation, 
     * can be null or empty to get all ids for this evaluation
     * @return a set of eval group ids which allow viewing by the specified permission
     */
    public Set<String> getViewableEvalGroupIds(Long evaluationId, String permissionConstant,
            String[] evalGroupIds);

    /**
     * Get adhoc groups for a user and permission, 
     * this is a way to check the perms for a user
     * 
     * @param userId the internal user id (not username)
     * @param permissionConstant a permission constant which is 
     * {@link EvalConstants#PERM_BE_EVALUATED} for instructors/evaluatees OR
     * {@link EvalConstants#PERM_TAKE_EVALUATION} for students/evaluators,
     * other permissions will return no results
     * @return a list of adhoc groups for which this user has this permission
     */
    public List<EvalAdhocGroup> getEvalAdhocGroupsByUserAndPerm(String userId,
            String permissionConstant);

    /**
     * Check if a user has a specified permission/role within an adhoc group
     * 
     * @param userId the internal user id (not username)
     * @param permission a permission string PERM constant (from this API),
     * <b>Note</b>: only take evaluation and be evaluated are supported
     * @param evalGroupId the unique id of an eval group
     * @return true if allowed, false otherwise
     */
    public boolean isUserAllowedInAdhocGroup(String userId, String permissionConstant,
            String evalGroupId);

    /**
     * Set lock state if scale is not already at that lock state
     * 
     * @param scale
     * @param lockState if true then lock this scale, otherwise unlock it
     * @return true if success, false otherwise
     */
    public boolean lockScale(EvalScale scale, Boolean lockState);

    /**
     * Set lock state if item is not already at that lock state,
     * lock associated scale if it does not match OR
     * unlock associated scale if not locked by other item(s) 
     * 
     * @param item
     * @param lockState if true then lock this item, otherwise unlock it
     * @return true if success, false otherwise
     */
    public boolean lockItem(EvalItem item, Boolean lockState);

    /**
     * Set lock state if template is not already at that lock state,
     * lock associated item(s) if they do not match OR
     * unlock associated item(s) if not locked by other template(s) 
     * 
     * @param template
     * @param lockState if true then lock this template, otherwise unlock it
     * @return true if success, false otherwise
     */
    public boolean lockTemplate(EvalTemplate template, Boolean lockState);

    /**
     * Lock evaluation if not already locked,
     * lock associated template(s) if not locked OR
     * unlock associated template(s) if not locked by other evaluations
     * 
     * @param evaluation
     * @param lockState if true then lock this evaluations, otherwise unlock it
     * @return true if success, false otherwise
     */
    public boolean lockEvaluation(EvalEvaluation evaluation, Boolean lockState);

    /**
     * @param scaleId
     * @return true if this scale is used in any items
     */
    public boolean isUsedScale(Long scaleId);

    /**
     * @param itemId
     * @return true if this item is used in any template (via a template item)
     */
    public boolean isUsedItem(Long itemId);

    /**
     * @param templateId
     * @return true if this template is used in any evalautions
     */
    public boolean isUsedTemplate(Long templateId);

    /**
     * Allows a lock to be obtained that is system wide,
     * this is primarily for ensuring something runs on a single server only in a cluster<br/>
     * <b>NOTE:</b> This intentionally returns a null on failure rather than an exception since exceptions will
     * cause a rollback which makes the current session effectively dead, this also makes it impossible to 
     * control the failure so instead we return null as a marker
     * 
     * @param lockId the name of the lock which we are seeking
     * @param holderId a unique id for the holder of this lock (normally a server id)
     * @param timePeriod the length of time (in milliseconds) that the lock should be valid for,
     * set this very low for non-repeating processes (the length of time the process should take to run)
     * and the length of the repeat period plus the time to run the process for repeating jobs
     * @return true if a lock was obtained, false if not, null if failure
     */
    public Boolean obtainLock(String lockId, String executerId, long timePeriod);

    /**
     * Releases a lock that was being held,
     * this is useful if you know a server is shutting down and you want to release your locks early<br/>
     * <b>NOTE:</b> This intentionally returns a null on failure rather than an exception since exceptions will
     * cause a rollback which makes the current session effectively dead, this also makes it impossible to 
     * control the failure so instead we return null as a marker
     * 
     * @param lockId the name of the lock which we are seeking
     * @param holderId a unique id for the holder of this lock (normally a server id)
     * @return true if a lock was released, false if not, null if failure
     */
    public Boolean releaseLock(String lockId, String executerId);

}