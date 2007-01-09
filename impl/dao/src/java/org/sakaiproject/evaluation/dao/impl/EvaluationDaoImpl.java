/******************************************************************************
 * EvaluationDaoImpl.java - created by aaronz@vt.edu on Aug 21, 2006
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

package org.sakaiproject.evaluation.dao.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.model.EvalAssignContext;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.model.utils.EvalUtils;
import org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao;
import org.springframework.dao.DataAccessException;


/**
 * Implementations for any methods from the EvaluationDao interface
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationDaoImpl extends HibernateCompleteGenericDao implements
		EvaluationDao {

	private static Log log = LogFactory.getLog(EvaluationDaoImpl.class);

	public void init() {
		log.debug("init");
	}

	/**
	 * Construct the HQL to do the templates query based on booleans and userId
	 * @param userId the Sakai internal user Id (of the owner)
	 * @param publicTemplates
	 * @param visibleTemplates
	 * @param sharedTemplates
	 * @return an HQL query string
	 */
	private String makeTemplateHQL(String userId, boolean publicTemplates,
			boolean visibleTemplates, boolean sharedTemplates) {

		/**
		 * TODO - Hierarchy
		 * visible and shared sharing methods are meant to work by relating the hierarchy level of 
		 * the owner with the sharing setting in the template, however, that was when 
		 * we assumed there would only be one level per user. That is no longer anything 
		 * we have control over (since we depend on data that comes from another API) 
		 * so we will have to add in a table which will track the hierarchy levels and
		 * link them to the template. This will be a very simple but necessary table.
		 */

		boolean atleastOnePredicate = false;
		StringBuffer query = new StringBuffer("from EvalTemplate where");

		if (userId == null) {
			// null userId means return all private templates
			query.append(" sharing = '" + EvalConstants.SHARING_PRIVATE + "' ");
			atleastOnePredicate = true;
		} else if ("".equals(userId) ) {
			// blank userId means no private templates
		} else {
			// all private templates based on the userId (owner)
			query.append(" (sharing = '" + EvalConstants.SHARING_PRIVATE + "' and owner = '" + userId + "') ");
			atleastOnePredicate = true;
		}

		if (visibleTemplates) {
			if (atleastOnePredicate)
				query.append(" or ");

			query.append(" sharing = '" + EvalConstants.SHARING_VISIBLE + "' ");
			atleastOnePredicate = true;
		}

		if (sharedTemplates) {
			if (atleastOnePredicate)
				query.append(" or ");

			query.append(" sharing = '" + EvalConstants.SHARING_SHARED + "' ");
			atleastOnePredicate = true;
		}

		if (publicTemplates) {
			if (atleastOnePredicate)
				query.append(" or ");

			query.append(" sharing = '" + EvalConstants.SHARING_PUBLIC + "'");
		}

		return query.toString();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#getTemplates(java.lang.String, boolean, boolean, boolean)
	 */
	public List getVisibleTemplates(String userId, boolean publicTemplates) {

		/*
		 * TO BE TESTED - 17 Oct 2006 - KAPIL DetachedCriteria dc =
		 * DetachedCriteria.forClass(Template.class);
		 * 
		 * if (userId != null) { dc.add(Restrictions.eq("sharing",
		 * PRIVATE_MODIFIER)); dc.add(Restrictions.conjunction());
		 * dc.add(Restrictions.eq("owner", userId)); }
		 * 
		 * if (visibleTemplates) { dc.add(Restrictions.disjunction());
		 * dc.add(Restrictions.eq("sharing", VISIBLE_MODIFIER)); }
		 * 
		 * if (sharedTemplates) { dc.add(Restrictions.disjunction());
		 * dc.add(Restrictions.eq("sharing", SHARED_MODIFIER)); }
		 * 
		 * if (publicTemplates) { dc.add(Restrictions.disjunction());
		 * dc.add(Restrictions.eq("sharing", PUBLIC_MODIFIER)); }
		 * 
		 * TO BE TESTED return getHibernateTemplate().findByCriteria(dc);
		 */

		String hqlQuery = makeTemplateHQL(userId, publicTemplates, false, false);
		List l = new ArrayList();
		try {
			l = getHibernateTemplate().find(hqlQuery);
		} catch (DataAccessException e) {
			// this may appear to be a swallowed error, but it is actually intended behavior
			log.error("Invalid argument combination (most likely you tried to request no items) caused failure");
		}
		return l;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#countVisibleTemplates(java.lang.String, boolean)
	 */
	public int countVisibleTemplates(String userId, boolean publicTemplates) {
		String hqlQuery = makeTemplateHQL(userId, publicTemplates, false, false);
		int count = 0;
		try {
			count = count(hqlQuery);
		} catch (DataAccessException e) {
			// this may appear to be a swallowed error, but it is actually intended behavior
			log.error("Invalid argument combination (most likely you tried to request no items) caused failure", e);
		}
		return count;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#getEvaluationsByContexts(java.lang.String[], boolean, boolean)
	 */
	public Set getEvaluationsByContexts(String[] contexts, boolean activeOnly) {
		Set evals = new TreeSet(new EvaluationDateComparator());
		if (contexts.length > 0) {
			DetachedCriteria dc = DetachedCriteria.forClass(EvalAssignContext.class)
				.add( Property.forName("context").in(contexts));

			List assignedCourses = getHibernateTemplate().findByCriteria(dc);
			for (int i=0;i<assignedCourses.size();i++) {
				EvalAssignContext ac = (EvalAssignContext) assignedCourses.get(i);
				// Note: This is inefficient, it is still retrieving ALL of the evaluations
				EvalEvaluation eval = ac.getEvaluation();
				if (activeOnly) {
					// only return the active evaluations
					if ( EvalUtils.getEvaluationState(eval) == 
							EvalConstants.EVALUATION_STATE_ACTIVE ) {
						evals.add(ac.getEvaluation());
					}
				} else {
					// return all evaluations
					evals.add(ac.getEvaluation());
				}
			}
		}
		return evals;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#getAnswers(java.lang.String, java.lang.String)
	 *
	 * SQL - $ indicates the variable must be inserted into the string
	 * select from eval_answer where item_fk=$itemId and response_fk in 
	 * (select id from eval_response where evaluation_fk=$evalId)
	 */
	public List getAnswers(Long itemId, Long evalId) {
		 // from EvalAnswer where item_fk=itemId and response_fk in 
		 // (select id from eval_response where eval_response.evaluation_fk=$evaluationId)
		StringBuffer hqlQuery = new StringBuffer ("from EvalAnswer where item_fk=");
		hqlQuery.append(itemId.toString());
		hqlQuery.append(" and response_fk in (select evalr.id from EvalResponse as evalr where evaluation_fk=");
		hqlQuery.append(evalId.toString());
		hqlQuery.append(") order by response_fk");
		return getHibernateTemplate().find(hqlQuery.toString());
	}


	private static class EvaluationDateComparator implements Comparator {
		public int compare(Object eval0, Object eval1) {
			// expects to get Evaluation objects
			return ((EvalEvaluation)eval0).getDueDate().
				compareTo(((EvalEvaluation)eval1).getDueDate());
		}
	}

}
