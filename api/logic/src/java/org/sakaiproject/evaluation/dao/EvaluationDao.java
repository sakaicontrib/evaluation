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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
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
	 * @param userId - Sakai internal user id, owner of the private templates to be selected,
	 * if it is null then all "Private" templates returned, if empty string then no private templates
	 * @param sharingConstants an array of sharing constants (private, public, etc) to define 
	 * what to include in the return
	 * @param includeEmpty if true then include templates with no items in them, else only return 
	 * templates with at least one item
	 * @return the count of accessible EvalTemplates for this user
	 */
	public int countVisibleTemplates(String userId, String[] sharingConstants, boolean includeEmpty);

	/**
	 * Returns all evaluation objects associated with the input contexts
	 * 
	 * @param evalGroupIds an array of eval group IDs to return active evals for
	 * @param activeOnly if true, only include active evaluations, if false, include all evaluations
	 * @param includeUnApproved if true, include the evaluations for contexts which have not been instructor approved yet,
	 * you should not include these when displaying evaluations to users to take or sending emails
	 * @param includeAnonymous if true then include evaluations which can be taken anonymously (since these are accessible
	 * to any user), if false, only include evals with require keys or authentication
	 * @return a Set of EvalEvaluation objects which are active
	 */
	public Set<EvalEvaluation> getEvaluationsByEvalGroups(String[] evalGroupIds, boolean activeOnly, boolean includeUnApproved, boolean includeAnonymous);

	/**
	 * Returns all evaluation objects
	 * 
	 * @param params
	 * @return
	 */
	public List<EvalEvaluation> getEvaluations();
	
	/**
	 * Returns evaluation objects for a group
	 * 
	 * @param groupId the group id of an EvalAssignGroup
	 * @return a list of evaluations
	 */
	public List<EvalEvaluation> getEvaluationsByGroupId(String groupId);
	
	/**
	 * Returns all evaluation objects associated with the params properties and values
	 * 
	 * @param params
	 * @return
	 */
	public List<EvalEvaluation> getEvaluations(Map<String, Object> params);
	
	/**
	 * Returns all eval assign group objects
	 * 
	 * @return a list of EvalAssignGroup objects or empty list if none found
	 */
	public List<EvalAssignGroup> getEvalAssignGroups();

	/**
	 * Returns all answers to the given item associated with 
	 * responses which are associated with the given evaluation
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
	 * and for specific groups.
	 *
	 * @param evalId the id of the evaluation you want response ids for
	 * @param evalGroupIds an array of eval group IDs to return response ids for,
	 * if null or empty then just return all response ids for this evaluatio
	 * @return a list of response ids (Long)
	 */
	public List<Long> getResponseIds(Long evalId, String[] evalGroupIds);
	
	//TODO migrate umich to above method thru logic api
	public List<EvalResponse> getResponses(EvalEvaluation eval, String groupId);

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
    * @param evaluationId a unique id for an evaluation
    * @return a list of template ids for {@link EvalTemplate} objects
    */
   public List<Long> getTemplateIdsForEvaluation(Long evaluationId);
   
	/**
	 * Get the evaluation ids for evaluations for which available email has or has not been sent
	 * 
	 * @param availableEmailSent if Boolean.TRUE return count of evaluations for which available email has been sent,
	 * if Boolean.FALSE return count of evaluations for which available email has not been sent, and if null return
	 * a count of all active evaluations
	 * @return the evaluations meeting the criterion
	 */
	public Long[] getActiveEvaluationIdsByAvailableEmailSent(Boolean availableEmailSent);


	// LOCKING METHODS

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
	 * lock associated template if not locked
	 * <b>Note:</b> Evaluations cannot be unlocked currently
	 * (since responses cannot be removed)
	 * 
	 * @param evaluation
	 * @return true if success, false otherwise
	 */
	public boolean lockEvaluation(EvalEvaluation evaluation);


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
