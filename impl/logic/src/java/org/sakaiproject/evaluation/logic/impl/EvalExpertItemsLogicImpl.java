/******************************************************************************
 * EvalExpertItemsLogicImpl.java - created by aaronz on 6 Mar 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalExpertItemsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;

/**
 * Implementation for the expert items api
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalExpertItemsLogicImpl implements EvalExpertItemsLogic {

	private static Log log = LogFactory.getLog(EvalItemsLogicImpl.class);

	private EvaluationDao dao;
	public void setDao(EvaluationDao dao) {
		this.dao = dao;
	}

	private EvalExternalLogic external;
	public void setExternalLogic(EvalExternalLogic external) {
		this.external = external;
	}


	// INIT method
	public void init() {
		log.debug("Init");
	}



	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#getItemGroups(java.lang.Long, java.lang.String, boolean)
	 */
	public List getItemGroups(Long parentItemGroupId, String userId, boolean includeEmpty, boolean includeExpert) {
		log.debug("parentItemGroupId:" + parentItemGroupId + ", userId:" + userId + ", includeEmpty:" + includeEmpty + ", includeExpert:" + includeExpert);

		// check this parent is real
		if (parentItemGroupId != null) {
			EvalItemGroup itemGroup = (EvalItemGroup) dao.findById(EvalItemGroup.class, parentItemGroupId);
			if (itemGroup == null) {
				throw new IllegalArgumentException("Cannot find parent itemGroup with id: " + parentItemGroupId);
			}
		}

//		List groups = new ArrayList();
//
//		if (parentItemGroupId == null) {
//			// get all top level groups
//			groups = dao.findByProperties(EvalItemGroup.class, 
//					new String[] { "parent", "expert" }, 
//					new Object[] { "", new Boolean(includeExpert) }, 
//					new int[] { ByPropsFinder.NULL, ByPropsFinder.EQUALS }, 
//					new String[] { "title" } );				
//
//		} else {
//			groups = dao.findByProperties(EvalItemGroup.class, 
//					new String[] { "parent.id", "expert" }, 
//					new Object[] { parentItemGroupId, new Boolean(includeExpert) }, 
//					new int[] { ByPropsFinder.EQUALS, ByPropsFinder.EQUALS }, 
//					new String[] { "title" } );
//
//		}
//
//		if (!includeEmpty) {
//
//			groups = dao.findByProperties(EvalItemGroup.class, 
//					new String[] { "parent.id", "expert" }, 
//					new Object[] { parentItemGroupId, new Boolean(includeExpert) }, 
//					new int[] { ByPropsFinder.EQUALS, ByPropsFinder.EQUALS }, 
//					new String[] { "title" } );
//
//			// get rid of the empty item groups
//			for (Iterator iter = groups.iterator(); iter.hasNext();) {
//				EvalItemGroup itemGroup = (EvalItemGroup) iter.next();
//				if ( (itemGroup.getGroupItems() == null || itemGroup.getGroupItems().isEmpty()) ) {
//					iter.remove();
//				}
//			}
//		}

		return dao.getItemGroups(parentItemGroupId, userId, includeEmpty, includeExpert);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#getItemsInItemGroup(java.lang.Long, boolean)
	 */
	public List getItemsInItemGroup(Long itemGroupId, boolean expertOnly) {
		log.debug("parentItemGroupId:" + itemGroupId + ", expertOnly:" + expertOnly);

		// get the item group by id
		EvalItemGroup itemGroup = (EvalItemGroup) dao.findById(EvalItemGroup.class, itemGroupId);
		if (itemGroup == null) {
			throw new IllegalArgumentException("Cannot find parent itemGroup with id: " + itemGroupId);
		}

		List items = new ArrayList();
		if ( itemGroup.getGroupItems() != null ) {
			items = new ArrayList( itemGroup.getGroupItems() );
			Collections.sort(items, new ItemComparatorById() );
		}

		if (expertOnly) {
			// get rid of the non-expert items
			for (Iterator iter = items.iterator(); iter.hasNext();) {
				EvalItem item = (EvalItem) iter.next();
				if (! item.getExpert().booleanValue()) {
					iter.remove();
				}
			}			
		}

		return items;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#createItemGroup(org.sakaiproject.evaluation.model.EvalItemGroup, java.lang.String, java.lang.Long)
	 */
	public void createItemGroup(EvalItemGroup itemGroup, String userId, Long parentCategoryId) {
		log.debug("itemGroup:" + itemGroup.getTitle() + ", userId:" + userId + ", parentCategoryId:" + parentCategoryId);

		// set the date modified
		itemGroup.setLastModified( new Date() );

		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#updateItemGroup(org.sakaiproject.evaluation.model.EvalItemGroup, java.lang.String)
	 */
	public void updateItemGroup(EvalItemGroup itemGroup, String userId) {
		log.debug("itemGroup:" + itemGroup.getId() + ", userId:" + userId);

		// set the date modified
		itemGroup.setLastModified( new Date() );

		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#removeItemGroup(java.lang.Long, java.lang.String, boolean)
	 */
	public boolean removeItemGroup(Long itemGroupId, String userId,	boolean removeNonEmptyGroup) {
		log.debug("itemGroupId:" + itemGroupId + ", userId:" + userId + ", removeNonEmptyGroup:" + removeNonEmptyGroup);

		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#canControlItemGroup(java.lang.String, java.lang.Long)
	 */
	public boolean canControlItemGroup(String userId, Long itemGroupId) {
		log.debug("itemGroupId:" + itemGroupId + ", userId:" + userId);

		// TODO Auto-generated method stub
		return false;
	}

	
	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#addGroupsToItemGroup(java.lang.Long, java.lang.Long[])
	 */
	public boolean addGroupsToItemGroup(Long parentItemGroupId, Long[] childItemGroupIds) {
		log.debug("parentItemGroupId:" + parentItemGroupId + ", childItemGroupIds length:" + childItemGroupIds.length);

		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#addItemsToItemGroup(java.lang.Long, java.lang.Long[])
	 */
	public boolean addItemsToItemGroup(Long itemGroupId, Long[] itemIds) {
		log.debug("itemGroupId:" + itemGroupId + ", itemIds length:" + itemIds.length);

		// TODO Auto-generated method stub
		return false;
	}


	/**
	 * static class to sort EvalTemplateItem objects by DisplayOrder
	 */
	public static class ItemGroupComparatorByTitle implements Comparator {
		public int compare(Object ig0, Object ig1) {
			// expects to get EvalItemGroup object, compare by title
			return ( (EvalItemGroup) ig0).getTitle().
				compareTo( ( (EvalItemGroup) ig1).getTitle() );
		}
	}

	/**
	 * static class to sort EvalItem objects by Id
	 */
	public static class ItemComparatorById implements Comparator {
		public int compare(Object item0, Object item1) {
			// expects to get EvalItem objects, compare by Id
			return ( (EvalItem) item0).getId().
				compareTo( ( (EvalItem) item1).getId() );
		}
	}

}
