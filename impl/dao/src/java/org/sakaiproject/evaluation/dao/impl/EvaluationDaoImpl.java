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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.utils.ArrayUtils;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.model.utils.EvalUtils;
import org.sakaiproject.genericdao.hibernate.HibernateCompleteGenericDao;
import org.springframework.dao.DataAccessException;


/**
 * Implementations for any methods from the EvaluationDao interface<br/>
 * Includes locking method implementations
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationDaoImpl
	extends HibernateCompleteGenericDao
		implements EvaluationDao {

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
			new StringBuffer("from EvalTemplate as template where template.type = '" + EvalConstants.TEMPLATE_TYPE_STANDARD + "' ");

		// do not include templates which have no items in them
		if (!includeEmpty) {
			query.append(" and template.templateItems.size > 0 and ");
		} else {
			query.append(" and ");
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
	public Set getEvaluationsByEvalGroups(String[] evalGroupIds, boolean activeOnly, boolean includeUnApproved) {
		Set evals = new TreeSet(new EvaluationDateComparator());
		if (evalGroupIds.length > 0) {
			DetachedCriteria dc = DetachedCriteria.forClass(EvalAssignGroup.class)
				.add( Property.forName("evalGroupId").in(evalGroupIds));

			List assignedCourses = getHibernateTemplate().findByCriteria(dc);
			for (int i=0;i<assignedCourses.size();i++) {
				// Note: This is inefficient, it is still retrieving ALL of the evaluations
				EvalAssignGroup ac = (EvalAssignGroup) assignedCourses.get(i);
				if (includeUnApproved ||
						ac.getInstructorApproval().booleanValue()) {
					// only include approved evals or all if requested
					EvalEvaluation eval = ac.getEvaluation();
					if (activeOnly) {
						// only return the active evaluations
						if ( EvalConstants.EVALUATION_STATE_ACTIVE.equals( EvalUtils.getEvaluationState(eval) ) ) {
							evals.add(ac.getEvaluation());
						}
					} else {
						// return all evaluations
						evals.add(ac.getEvaluation());
					}
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
	public List getAnswers(Long itemId, Long evalId, String[] evalGroupIds) {
		String groupsHQL = "";
		if (evalGroupIds != null && evalGroupIds.length > 0) {
			groupsHQL = " and response.evalGroupId in " + arrayToCommaString(evalGroupIds);
		}

		String hqlQuery = "from EvalAnswer as answer where answer.item.id='" + itemId.toString() + "'" +
			" and answer.response.id in " +
			"(select response.id from EvalResponse as response where response.evaluation.id='" + evalId.toString() + "'" + 
			groupsHQL + " order by response.id)";
		return getHibernateTemplate().find( hqlQuery );
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


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#getItemGroups(java.lang.Long, java.lang.String, boolean, boolean)
	 */
	public List getItemGroups(Long parentItemGroupId, String userId, boolean includeEmpty, boolean includeExpert) {

		DetachedCriteria dc = DetachedCriteria.forClass(EvalItemGroup.class)
			.add( Expression.eq("expert", new Boolean(includeExpert) ) );

		if (parentItemGroupId == null) {
			dc.add( Expression.isNull("parent") );
		} else {
			dc.add( Property.forName("parent.id").eq(parentItemGroupId) );
		}

		if (!includeEmpty) {
			String hqlQuery = "select distinct eig.parent.id from EvalItemGroup eig where eig.parent is not null";
			List parentIds = getHibernateTemplate().find( hqlQuery );

			// only include categories with items OR groups using them as a parent
			dc.add( Restrictions.disjunction()
				.add( Property.forName("groupItems").isNotEmpty() )
				.add( Property.forName("id").in( parentIds ) )
			);
		}

		dc.addOrder( Order.asc("title") );

		return getHibernateTemplate().findByCriteria(dc);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#getTemplateItemsByTemplate(java.lang.Long, java.lang.String[], java.lang.String[], java.lang.String[])
	 */
	public List getTemplateItemsByTemplate(Long templateId, String[] nodeIds, String[] instructorIds, String[] groupIds) {

		String hql = "from EvalTemplateItem ti where ti.template.id = ? " +
				"and (ti.hierarchyLevel = ?";
		Object[] params = new Object[] {templateId, EvalConstants.HIERARCHY_LEVEL_TOP};

		if (nodeIds != null && nodeIds.length > 0) {
			hql += " or (ti.hierarchyLevel = ? and ti.hierarchyNodeId in " + 
				arrayToCommaString(nodeIds) + " ) ";
			params = ArrayUtils.appendArray(params, EvalConstants.HIERARCHY_LEVEL_NODE);
		}

		if (instructorIds != null && instructorIds.length > 0) {
			hql += " or (ti.hierarchyLevel = ? and ti.hierarchyNodeId in " + 
				arrayToCommaString(instructorIds) + " ) ";
			params = ArrayUtils.appendArray(params, EvalConstants.HIERARCHY_LEVEL_INSTRUCTOR);
		}

		if (groupIds != null && groupIds.length > 0) {
			hql += " or (ti.hierarchyLevel = ? and ti.hierarchyNodeId in " + 
				arrayToCommaString(groupIds) + " ) ";
			params = ArrayUtils.appendArray(params, EvalConstants.HIERARCHY_LEVEL_GROUP);
		}

		hql += ") order by ti.displayOrder";
		System.out.println("HQL: " + hql);
		for (int i = 0; i < params.length; i++) {
			System.out.println(i + "=" + params[i]);			
		}

		return getHibernateTemplate().find(hql, params);

//		DetachedCriteria dc = DetachedCriteria.forClass(EvalTemplateItem.class)
//			.add( Expression.eq("template.id", templateId) );
//
//		dc.add( Property.forName("hierarchyLevel").eq(EvalConstants.HIERARCHY_LEVEL_TOP) );
//
//		if (nodeIds != null && nodeIds.length > 0) {
//			dc.add( Restrictions.conjunction()
//					.add( Property.forName("hierarchyLevel").eq(EvalConstants.HIERARCHY_LEVEL_NODE) )
//					.add( Property.forName("hierarchyNodeId").in( nodeIds ) )
//				);
//		}
//
//		if (instructorIds != null && instructorIds.length > 0) {
//			dc.add( Restrictions.conjunction()
//					.add( Property.forName("hierarchyLevel").eq(EvalConstants.HIERARCHY_LEVEL_INSTRUCTOR) )
//					.add( Property.forName("hierarchyNodeId").in( instructorIds ) )
//				);
//		}
//
//		if (groupIds != null && groupIds.length > 0) {
//			dc.add( Restrictions.conjunction()
//					.add( Property.forName("hierarchyLevel").eq(EvalConstants.HIERARCHY_LEVEL_GROUP) )
//					.add( Property.forName("hierarchyNodeId").in( groupIds ) )
//				);
//		}
//	
//		dc.addOrder( Order.asc("displayOrder") );

	}

//	public Integer getNextBlockId() {
//		String hqlQuery = "select max(item.blockId) from EvalItem item";
//		Integer max = (Integer) getHibernateTemplate().iterate(hqlQuery).next();
//		if (max == null) {
//			return new Integer(0);
//		}
//		return new Integer(max.intValue() + 1);
//	}

	// LOCKING METHODS

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#unlockScale(org.sakaiproject.evaluation.model.EvalScale)
	 */
	public boolean unlockScale(EvalScale scale) {
		log.info("scale:" + scale.getId());
		if (scale.getId() == null) {
			throw new IllegalStateException("Cannot unlock an unsaved scale object");
		}

		if (! scale.getLocked().booleanValue()) {
			// already unlocked
			return false;
		} else {
			DetachedCriteria dc = DetachedCriteria.forClass(EvalItem.class)
				.add( Restrictions.eq( "locked", Boolean.TRUE ) )
				.add( Restrictions.eq( "scale.id", scale.getId() ) )
				.setProjection( Projections.rowCount() );
			if ( ((Integer) getHibernateTemplate().findByCriteria( dc ).get(0)).intValue() > 0 ) {
				// this is locked by something, we cannot unlock it
				log.info("Cannot unlock scale ("+scale.getId()+"), it is locked elsewhere");
				return false;
			}

			scale.setLocked( Boolean.FALSE );
			getHibernateTemplate().update( scale );
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#lockItem(org.sakaiproject.evaluation.model.EvalItem, java.lang.Boolean)
	 */
	public boolean lockItem(EvalItem item, Boolean lockState) {
		log.info("item:" + item.getId() + ", lockState:" + lockState);
		if (item.getId() == null) {
			throw new IllegalStateException("Cannot change lock state on an unsaved item object");
		}

		if (lockState.booleanValue()) {
			// locking this item
			if (item.getLocked().booleanValue()) {
				// already locked, no change
				return false;
			} else {
				// lock item and associated scale (if set)
				item.setLocked( Boolean.TRUE );
				if (item.getScale() != null) {
					EvalScale scale = item.getScale();
					if (! scale.getLocked().booleanValue()) {
						scale.setLocked( Boolean.TRUE );
						getHibernateTemplate().update( scale );
					}
				}
				getHibernateTemplate().update( item );
				return true;
			}
		} else {
			// unlocking this item
			if (! item.getLocked().booleanValue()) {
				// already unlocked, no change
				return false;
			} else {
				// unlock item (if not locked elsewhere)
				String hqlQuery = "from EvalTemplateItem as ti where ti.item.id = '" + item.getId() + "' and ti.template.locked = true";
				if ( count(hqlQuery) > 0 ) {
					// this is locked by something, we cannot unlock it
					log.info("Cannot unlock item ("+item.getId()+"), it is locked elsewhere");
					return false;
				}

				// unlock item
				item.setLocked( Boolean.FALSE );
				getHibernateTemplate().update( item );

				// unlock associated scale if there is one
				if (item.getScale() != null) {
					unlockScale( item.getScale() );
				}

				return true;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#lockTemplate(org.sakaiproject.evaluation.model.EvalTemplate, java.lang.Boolean)
	 */
	public boolean lockTemplate(EvalTemplate template, Boolean lockState) {
		log.info("template:" + template.getId() + ", lockState:" + lockState);
		if (template.getId() == null) {
			throw new IllegalStateException("Cannot change lock state on an unsaved template object");
		}

		if (lockState.booleanValue()) {
			// locking this template
			if (template.getLocked().booleanValue()) {
				// already locked, no change
				return false;
			} else {
				// lock template and associated items (if set)
				template.setLocked( Boolean.TRUE );
				if (template.getTemplateItems() != null && 
						template.getTemplateItems().size() > 0) {
					// loop through and lock all related items
					for (Iterator iter = template.getTemplateItems().iterator(); iter.hasNext();) {
						EvalTemplateItem eti = (EvalTemplateItem) iter.next();
						lockItem(eti.getItem(), Boolean.TRUE);
					}
				}
				getHibernateTemplate().update( template );
				return true;
			}
		} else {
			// unlocking this template
			if (! template.getLocked().booleanValue()) {
				// already unlocked, no change
				return false;
			} else {
				// unlock template (if not locked elsewhere)
				String hqlQuery = "from EvalEvaluation as eval where eval.template.id = '" + template.getId() + "' and eval.locked = true";
				if ( count(hqlQuery) > 0 ) {
					// this is locked by something, we cannot unlock it
					log.info("Cannot unlock template ("+template.getId()+"), it is locked elsewhere");
					return false;
				}

				// unlock template
				template.setLocked( Boolean.FALSE );
				getHibernateTemplate().update( template );

				// unlock associated items if there are any
				if (template.getTemplateItems() != null && 
						template.getTemplateItems().size() > 0) {
					// loop through and unlock all related items
					for (Iterator iter = template.getTemplateItems().iterator(); iter.hasNext();) {
						EvalTemplateItem eti = (EvalTemplateItem) iter.next();
						lockItem(eti.getItem(), Boolean.FALSE);
					}
				}

				return true;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#lockEvaluation(org.sakaiproject.evaluation.model.EvalEvaluation)
	 */
	public boolean lockEvaluation(EvalEvaluation evaluation) {
		log.info("evaluation:" + evaluation.getId());
		if (evaluation.getId() == null) {
			throw new IllegalStateException("Cannot change lock state on an unsaved evaluation object");
		}

		if (evaluation.getLocked().booleanValue()) {
			// already locked, no change
			return false;
		} else {
			// lock evaluation and associated templatea
			EvalTemplate template = evaluation.getTemplate();
			if (! template.getLocked().booleanValue()) {
				lockTemplate(template, Boolean.TRUE);
			}

			EvalTemplate addedTemplate = evaluation.getAddedTemplate();
			if (addedTemplate != null &&
					! addedTemplate.getLocked().booleanValue()) {
				lockTemplate(addedTemplate, Boolean.TRUE);
			}

			evaluation.setLocked( Boolean.TRUE );
			getHibernateTemplate().update( evaluation );
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.dao.EvaluationDao#getResponseIds(java.lang.Long, java.lang.String[])
	 *
	 * SQL - $ indicates the variable must be inserted into the string
	 * select id from eval_response where evaluation_fk=$evalId
	 */
	public List getResponseIds(Long evalId, String[] evalGroupIds) {
		String groupsHQL = "";
		if (evalGroupIds != null && evalGroupIds.length > 0) {
			groupsHQL = " and response.evalGroupId in " + arrayToCommaString(evalGroupIds);
		}

		String hqlQuery = "SELECT response.id from EvalResponse as response where response.evaluation.id='" + evalId.toString() + "'" + 
			groupsHQL + " order by response.id";
		return getHibernateTemplate().find( hqlQuery );
	}

	/**
	 * produce a comma demlimited string like "item1','item2','item3" from a string array
	 * 
	 * @param array any array of String
	 * @return string like "item1','item2','item3"
	 */
	private String arrayToCommaString(String[] array) {
		String cds = "('";
		for (int i = 0; i < array.length; i++) {
			if (i > 0)
				cds += "','" + array[i];
			else
				cds += array[i];
		}
		cds += "')";
		return cds;
	}

	/**
	 * 
	 *
	 * @author Aaron Zeckoski (aaronz@vt.edu)
	 */
	private static class EvaluationDateComparator implements Comparator {
		public int compare(Object eval0, Object eval1) {
			// expects to get Evaluation objects
			return ((EvalEvaluation)eval0).getDueDate().
				compareTo(((EvalEvaluation)eval1).getDueDate());
		}
	}

}
