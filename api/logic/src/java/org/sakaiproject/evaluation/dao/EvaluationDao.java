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
import java.util.Set;

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
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
	 * Find templates visible for a user based on 4 predicates:
	 * "private" and owned by someone, "public", 
	 * (shared and visible not handled yet)
	 * 
	 * @param userId Sakai internal user id, owner of the private templates to be selected,
	 * if it is null then all "Private" templates returned, if empty string then no private templates
	 * @param sharingConstants an array of sharing constants (private, public, etc) to define 
	 * what to include in the return
	 * @param includeEmpty if true then include templates with no items in them, else only return 
	 * templates with at least one item
	 * @return a List of EvalTemplate objects, ordered by sharing and title alphabetic
	 */
	public List getVisibleTemplates(String userId, String[] sharingConstants, boolean includeEmpty);

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
	 * @param contexts an array of Sakai contexts to return active evals for
	 * @param activeOnly if true, only include active evaluations, if false, include all evaluations
	 * @param includeUnApproved if true, include the evaluations for contexts which have not been instructor approved yet,
	 * you should not include these when displaying evaluations to users to take or sending emails
	 * @return a Set of EvalEvaluation objects which are active
	 */
	public Set getEvaluationsByContexts(String[] contexts, boolean activeOnly, boolean includeUnApproved);

	/**
	 * Returns all answers to the given item associated with 
	 * responses which are associated with the given evaluation
	 *
	 * @param itemId the id of the item you want answers for
	 * @param evalId the id of the evaluation you want answers from
	 * @return a list of EvalAnswer objects or empty list if none found
	 */
	public List getAnswers(Long itemId, Long evalId);

	/**
	 * Removes a group of templateItems and updates all related items 
	 * and templates at the same time (inside one transaction)
	 * 
	 * @param templateItems the array of {@link EvalTemplateItem} to remove 
	 */
	public void removeTemplateItems(EvalTemplateItem[] templateItems);

	// LOCKING METHODS

	/**
	 * End of the chain, logic is very simple, 
	 * set unlock state if scale is not already unlocked and if there
	 * are no items that are locking it<br/>
	 * <b>Note:</b> scales cannot be locked directly
	 * 
	 * @param scale
	 * @return true if success, false otherwise
	 */
	public boolean unlockScale(EvalScale scale);

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

}
