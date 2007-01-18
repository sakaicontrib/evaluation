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

import org.sakaiproject.genericdao.api.CompleteGenericDao;

/**
 * This is the more specific Evaluation data access interface,
 * it should contain specific DAO methods, the generic ones
 * are included from the GenericDao already
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
	 * @return a List of EvalTemplate objects
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
	 * @return a Set of EvalEvaluation objects which are active
	 */
	public Set getEvaluationsByContexts(String[] contexts, boolean activeOnly);

	/**
	 * Returns all answers to the given item associated with 
	 * responses which are associated with the given evaluation
	 *
	 * @param itemId the id of the item you want answers for
	 * @param evalId the id of the evaluation you want answers from
	 * @return a list of EvalAnswer objects or empty list if none found
	 */
	public List getAnswers(Long itemId, Long evalId);


}
