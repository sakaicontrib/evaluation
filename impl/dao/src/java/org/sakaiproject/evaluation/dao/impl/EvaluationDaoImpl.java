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
import java.util.HashSet;
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
import org.sakaiproject.evaluation.model.EvalTemplateItem;
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
	private String makeTemplateHQL(String userId, boolean includeEmpty,  
			boolean publicTemplates, boolean visibleTemplates, boolean sharedTemplates) {

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
		StringBuffer query = 
			new StringBuffer("from EvalTemplate as template");

		// do not include templates which have no items in them
		if (!includeEmpty) {
			query.append(" where template.templateItems.size > 0 and ");
		} else {
			query.append(" where ");
		}

		if (userId == null) {
			// null userId means return all private templates
			if (atleastOnePredicate)
				query.append(" or ");
			atleastOnePredicate = true;

			query.append(" template.sharing = '" + EvalConstants.SHARING_PRIVATE + "' ");
		} else if ("".equals(userId) ) {
			// blank userId means no private templates
		} else {
			// all private templates based on the userId (owner)
			if (atleastOnePredicate)
				query.append(" or ");
			atleastOnePredicate = true;

			query.append(" (template.sharing = '" + EvalConstants.SHARING_PRIVATE + "' and template.owner = '" + userId + "') ");
		}

		if (publicTemplates) {
			if (atleastOnePredicate)
				query.append(" or ");
			atleastOnePredicate = true;

			query.append(" template.sharing = '" + EvalConstants.SHARING_PUBLIC + "'");
		}

//		if (visibleTemplates) {
//			if (atleastOnePredicate)
//				query.append(" or ");
//			atleastOnePredicate = true;
//
//			query.append(" template.sharing = '" + EvalConstants.SHARING_VISIBLE + "' ");
//		}
//
//		if (sharedTemplates) {
//			if (atleastOnePredicate)
//				query.append(" or ");
//			atleastOnePredicate = true;
//
//			query.append(" template.sharing = '" + EvalConstants.SHARING_SHARED + "' ");
//		}

		return query.toString();
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#countVisibleTemplates(java.lang.String, java.lang.String[], boolean)
	 */
	public int countVisibleTemplates(String userId, String[] sharingConstants, boolean includeEmpty) {
		boolean publicTemplates = false;
		for (int i = 0; i < sharingConstants.length; i++) {
			String sharingConstant = sharingConstants[i];
			if (EvalConstants.SHARING_PRIVATE.equals(sharingConstant)) {
				if (userId == null || userId.equals("")) {
					throw new IllegalArgumentException("Must specify a userId when requesting private templates");
				}
			} else if (EvalConstants.SHARING_PUBLIC.equals(sharingConstant)) {
				publicTemplates = true;
			}
		}
		String hqlQuery = makeTemplateHQL(userId, includeEmpty, publicTemplates, false, false);

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
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#getVisibleTemplates(java.lang.String, java.lang.String[], boolean)
	 */
	public List getVisibleTemplates(String userId, String[] sharingConstants, boolean includeEmpty) {
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

		boolean publicTemplates = false;
		for (int i = 0; i < sharingConstants.length; i++) {
			String sharingConstant = sharingConstants[i];
			if (EvalConstants.SHARING_PRIVATE.equals(sharingConstant)) {
				if (userId == null || userId.equals("")) {
					throw new IllegalArgumentException("Must specify a userId when requesting private templates");
				}
			} else if (EvalConstants.SHARING_PUBLIC.equals(sharingConstant)) {
				publicTemplates = true;
			}
		}
		String hqlQuery = makeTemplateHQL(userId, includeEmpty, publicTemplates, false, false);

		// add ordering to returned values
		hqlQuery += " order by template.sharing, template.title";

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


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#removeTemplateItems(org.sakaiproject.evaluation.model.EvalTemplateItem[])
	 */
	public void removeTemplateItems(EvalTemplateItem[] templateItems) {
		log.debug("Removing "+templateItems.length+" template items");
		Set deleteTemplateItems = new HashSet();

		for (int i = 0; i < templateItems.length; i++) {
			EvalTemplateItem eti = (EvalTemplateItem) getHibernateTemplate().merge( templateItems[i] );
			deleteTemplateItems.add(eti);

			eti.getItem().getTemplateItems().remove(eti);
			eti.getTemplate().getTemplateItems().remove(eti);
			getHibernateTemplate().update(eti);
		}

		// do the actual deletes
		getHibernateTemplate().deleteAll(deleteTemplateItems);
		log.info("Removed "+deleteTemplateItems.size()+" template items");
	}


//	public Integer getNextBlockId() {
//		String hqlQuery = "select max(item.blockId) from EvalItem item";
//		Integer max = (Integer) getHibernateTemplate().iterate(hqlQuery).next();
//		if (max == null) {
//			return new Integer(0);
//		}
//		return new Integer(max.intValue() + 1);
//	}



	private static class EvaluationDateComparator implements Comparator {
		public int compare(Object eval0, Object eval1) {
			// expects to get Evaluation objects
			return ((EvalEvaluation)eval0).getDueDate().
				compareTo(((EvalEvaluation)eval1).getDueDate());
		}
	}

}
