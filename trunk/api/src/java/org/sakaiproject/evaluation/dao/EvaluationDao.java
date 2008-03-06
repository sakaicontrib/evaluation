/******************************************************************************
 * EvaluationDao.java - created by aaronz@vt.edu on Aug 21, 2006
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

package org.sakaiproject.evaluation.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.genericdao.api.CompleteGenericDao;

/**
 * This is the more specific Evaluation data access interface,
 * it should contain specific DAO methods, the generic ones
 * are included from the GenericDao already<br/>
 * <br/>
 * <b>LOCKING methods note:</b><br/>
 * The locking logic is designed to make it easier to know if an entity should or should not be changed or removed<br/> 
 * Locked entities can never be removed via the APIs and should not be removed with direct access to the DB<br/>
 * Locking handled as indicated here:<br/>
 * http://bugs.sakaiproject.org/confluence/display/EVALSYS/Evaluation+Implementation
 * 
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvaluationDao extends CompleteGenericDao {

	/**
	 * Find templates visible for a user, only includes standard type templates,
	 * (does not include templates that hold added items, 
	 * "private" and owned by someone, "public", (shared and visible not handled yet)
	 * 
	 * @param userId Sakai internal user id, owner of the private templates to be selected,
	 * if it is null then all "Private" templates returned, if empty string then no private templates
	 * @param sharingConstants an array of sharing constants (private, public, etc) to define 
	 * what to include in the return
	 * @param includeEmpty if true then include templates with no items in them, else only return 
	 * templates with at least one item
	 * @return a List of EvalTemplate objects, ordered by sharing and title alphabetic
	 */
	public List<EvalTemplate> getVisibleTemplates(String userId, String[] sharingConstants, boolean includeEmpty);

	/**
	 * Count the templates that are visible to a user
	 * 
	 * @param userId internal user id, owner of the private templates to be selected,
	 * if it is null then all "Private" templates returned, if empty string then no private templates
	 * @param sharingConstants an array of sharing constants (private, public, etc) to define 
	 * what to include in the return
	 * @param includeEmpty if true then include templates with no items in them, else only return 
	 * templates with at least one item
	 * @return the count of accessible EvalTemplates for this user
	 */
	public int countVisibleTemplates(String userId, String[] sharingConstants, boolean includeEmpty);

	/**
    * Removes a group of templateItems and updates all related items 
    * and templates at the same time (inside one transaction)
    * 
    * @param templateItems the array of {@link EvalTemplateItem} to remove 
    */
   public void removeTemplateItems(EvalTemplateItem[] templateItems);

	/**
	 * Returns all evaluation objects associated with the input groups,
	 * can also include anonymous evaluations
	 * 
	 * @param evalGroupIds an array of eval group IDs to get associated evals for, 
	 * can be empty or null but only anonymous evals will be returned
	 * @param activeOnly if true, only include active evaluations, if false, include all evaluations
	 * @param includeUnApproved if true, include the evaluations for groups which have not been instructor approved yet,
	 * you should not include these when displaying evaluations to users to take or sending emails
	 * @param includeAnonymous if true then include evaluations which can be taken anonymously (since these are accessible
	 * to any user in any group), if false, only include evals which require keys or authentication
	 * @return a List of EvalEvaluation objects sorted by due date, title, and id
	 */
	public List<EvalEvaluation> getEvaluationsByEvalGroups(String[] evalGroupIds, boolean activeOnly, boolean includeUnApproved, boolean includeAnonymous);

	/**
	 * @param userId internal user id, owner of the evaluations, if null then do not filter on the owner id
	 * @param evalGroupIds an array of eval group IDs to get associated evals for, can be empty or null to get all evals
	 * @param recentClosedDate only return evaluations which closed after this date
    * @param startResult 0 to start with the first result, otherwise start with this result number
	 * @param maxResults 0 to return all results, otherwise limit the number of evals returned to this
	 * @return a List of EvalEvaluation objects sorted by stop date, title, and id
	 */
	public List<EvalEvaluation> getEvaluationsForOwnerAndGroups(String userId, String[] evalGroupIds, Date recentClosedDate, int startResult, int maxResults);

	/**
	 * Returns all answers to the given item associated with 
	 * responses which are associated with the given evaluation,
	 * only returns the answers for completed responses
	 *
	 * @param itemId the id of the item you want answers for
	 * @param evalId the id of the evaluation you want answers from
	 * @param evalGroupIds an array of eval group IDs to return answers for,
	 * if null then just return all answers for this evaluation
	 * @return a list of EvalAnswer objects or empty list if none found
	 */
	public List<EvalAnswer> getAnswers(Long itemId, Long evalId, String[] evalGroupIds);

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
	public List<Long> getResponseIds(Long evalId, String[] evalGroupIds, String[] userIds, Boolean completed);

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
	public List<EvalItemGroup> getItemGroups(Long parentItemGroupId, String userId, boolean includeEmpty, boolean includeExpert);

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
	public List<EvalTemplateItem> getTemplateItemsByTemplate(Long templateId, String[] nodeIds, String[] instructorIds, String[] groupIds);

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
   public List<EvalTemplateItem> getTemplateItemsByEvaluation(Long evalId, String[] nodeIds, String[] instructorIds, String[] groupIds);

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
    * Get all the templates ids associated with an evaluation
    * 
    * @param evaluationId a unique id for an {@link EvalEvaluation}
    * @return a list of template ids for {@link EvalTemplate} objects
    */
   public List<Long> getTemplateIdsForEvaluation(Long evaluationId);

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
   public Set<String> getViewableEvalGroupIds(Long evaluationId, String permissionConstant, String[] evalGroupIds);

   // LOCK METHODS

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
   public Boolean obtainLock(String lockId, String holderId, long timePeriod);

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
   public Boolean releaseLock(String lockId, String holderId);

   /**
    * Allows for an easy way to run some code ONLY if a lock can be obtained by
    * the executer, if the lock can be obtained then the Runnable is executed,
    * if not, then nothing happens<br/>
    * This is primarily useful for ensuring only a single server in the cluster
    * is executing some code<br/>
    * <b>NOTE:</b> This intentionally returns a null on failure rather than an exception since exceptions will
    * cause a rollback which makes the current session effectively dead, this also makes it impossible to 
    * control the failure so instead we return null as a marker
    * @param lockId the name of the lock which we are seeking
    * @param executerId a unique id for the executer of this code (normally a server id)
    * @param toExecute a {@link Runnable} which will have the run method executed if a lock can be obtained
    * 
    * @return true if the code was executed, false if someone else has the lock, null if there was a failure
    */
   public Boolean lockAndExecuteRunnable(String lockId, String executerId, Runnable toExecute);


	// DATA LOCKING METHODS

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


	// IN-USE METHODS

	/**
	 * Check if this scale is used by any items
	 * 
	 * @param scaleId the unique id for an {@link EvalScale} object
	 * @return true if used, false otherwise
	 */
	public boolean isUsedScale(Long scaleId);

	/**
	 * Check if this item is used by any templates
	 * 
	 * @param itemId the unique id for an {@link EvalItem} object
	 * @return true if used, false otherwise
	 */
	public boolean isUsedItem(Long itemId);

	/**
	 * Check if this template is used by any evaluations
	 * 
	 * @param templateId the unique id for an {@link EvalTemplate} object
	 * @return true if used, false otherwise
	 */
	public boolean isUsedTemplate(Long templateId);

}
