/**
 * $Id: EvalExpertItemsLogicImpl.java 1000 Dec 25, 2006 10:07:31 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvalExpertItemsLogicImpl.java - evaluation - Mar 06, 2007 10:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalExpertItemsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.utils.ComparatorsUtils;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

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
	public List<EvalItemGroup> getItemGroups(Long parentItemGroupId, String userId, boolean includeEmpty, boolean includeExpert) {
		log.debug("parentItemGroupId:" + parentItemGroupId + ", userId:" + userId + ", includeEmpty:" + includeEmpty + ", includeExpert:" + includeExpert);

		// check this parent is real
		if (parentItemGroupId != null) {
			EvalItemGroup itemGroup = (EvalItemGroup) dao.findById(EvalItemGroup.class, parentItemGroupId);
			if (itemGroup == null) {
				throw new IllegalArgumentException("Cannot find parent itemGroup with id: " + parentItemGroupId);
			}
		}

		return dao.getItemGroups(parentItemGroupId, userId, includeEmpty, includeExpert);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#getItemsInItemGroup(java.lang.Long, boolean)
	 */
	public List<EvalItem> getItemsInItemGroup(Long itemGroupId, boolean expertOnly) {
		log.debug("parentItemGroupId:" + itemGroupId + ", expertOnly:" + expertOnly);

		// get the item group by id
		EvalItemGroup itemGroup = (EvalItemGroup) dao.findById(EvalItemGroup.class, itemGroupId);
		if (itemGroup == null) {
			throw new IllegalArgumentException("Cannot find parent itemGroup with id: " + itemGroupId);
		}

      List<EvalItem> items = new ArrayList<EvalItem>();
		if ( itemGroup.getGroupItems() != null ) {
			items = new ArrayList<EvalItem>( itemGroup.getGroupItems() );
			Collections.sort(items, new ComparatorsUtils.ItemComparatorById() );
		}

		if (expertOnly) {
			// get rid of the non-expert items
			for (Iterator<EvalItem> iter = items.iterator(); iter.hasNext();) {
				EvalItem item = (EvalItem) iter.next();
				if (! item.getExpert().booleanValue()) {
					iter.remove();
				}
			}			
		}

		return items;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#getItemGroupById(java.lang.Long)
	 */
	public EvalItemGroup getItemGroupById(Long itemGroupId) {
		log.debug("itemGroupId:" + itemGroupId );

		return (EvalItemGroup) dao.findById(EvalItemGroup.class, itemGroupId);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#saveItemGroup(org.sakaiproject.evaluation.model.EvalItemGroup, java.lang.String)
	 */
	public void saveItemGroup(EvalItemGroup itemGroup, String userId) {
		log.debug("itemGroup:" + itemGroup.getId() + ", userId:" + userId);

		// set the date modified
		itemGroup.setLastModified( new Date() );

		// fill in the default settings for optional unspecified values
		if ( itemGroup.getExpert() == null ) {
			itemGroup.setExpert( Boolean.FALSE );
		}

		// check only admin can create expert item groups
		if ( itemGroup.getExpert().booleanValue() && ! external.isUserAdmin(userId) ) {
			throw new IllegalArgumentException("Only admins can create expert item groups");
		}

		// check that the type is valid
		if ( itemGroup.getType() == null ) {
			throw new IllegalArgumentException("Item group type cannot be null");
		}
		if ( itemGroup.getExpert().booleanValue() && ! checkItemGroupType( itemGroup.getType() ) ) {
			throw new IllegalArgumentException("Invalid item group type for expert group: " + itemGroup.getType() );
		}

		// check that the parent is set correctly
		if ( EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE.equals( itemGroup.getType() ) && itemGroup.getParent() == null ) {
			throw new IllegalArgumentException("Cannot have a null parent for an objective type item group: " + itemGroup.getType() );
		} else if ( EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals( itemGroup.getType() ) && itemGroup.getParent() != null ) {
			throw new IllegalArgumentException("Cannot have a parent for a category type item group: " + itemGroup.getType() );
		}

		// check user can create or update item group
		if (checkUserControlItemGroup(userId, itemGroup)) {
			dao.save(itemGroup);

			log.info("User ("+userId+") saved itemGroup ("+itemGroup.getId()+"), " + " of type ("+ itemGroup.getType()+")");
			return;		
		}

		// should not get here so die if we do
		throw new RuntimeException("User ("+userId+") could NOT save itemGroup ("+itemGroup.getId()+")");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#removeItemGroup(java.lang.Long, java.lang.String, boolean)
	 */
	public void removeItemGroup(Long itemGroupId, String userId, boolean removeNonEmptyGroup) {
		log.debug("itemGroupId:" + itemGroupId + ", userId:" + userId + ", removeNonEmptyGroup:" + removeNonEmptyGroup);

		// get the item by id
		EvalItemGroup itemGroup = (EvalItemGroup) dao.findById(EvalItemGroup.class, itemGroupId);
		if (itemGroup == null) {
			throw new IllegalArgumentException("Cannot find item group item with id: " + itemGroupId);
		}

		// check user can create or update item group
		if (checkUserControlItemGroup(userId, itemGroup)) {

			if (! removeNonEmptyGroup) {
				// not empty cannot be removed
			   List<EvalItemGroup> l = dao.getItemGroups(itemGroup.getId(), userId, true, true);
				if (l.size() > 0) {
					throw new IllegalStateException("Cannot remove non-empty item group: " + itemGroupId);
				}
			}

			dao.delete(itemGroup);

			log.info("User ("+userId+") removed itemGroup ("+itemGroup.getId()+"), " + " of type ("+ itemGroup.getType()+")");
			return;		
		}

		// should not get here so die if we do
		throw new RuntimeException("User ("+userId+") could NOT remove itemGroup ("+itemGroup.getId()+")");
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#canUpdateItemGroup(java.lang.String, java.lang.Long)
	 */
	public boolean canUpdateItemGroup(String userId, Long itemGroupId) {
		log.debug("itemGroupId:" + itemGroupId + ", userId:" + userId);

		// get the item by id
		EvalItemGroup itemGroup = (EvalItemGroup) dao.findById(EvalItemGroup.class, itemGroupId);
		if (itemGroup == null) {
			throw new IllegalArgumentException("Cannot find item group item with id: " + itemGroupId);
		}

		// check perms
		try {
			return checkUserControlItemGroup(userId, itemGroup);
		} catch (RuntimeException e) {
			log.info(e.getMessage());
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalExpertItemsLogic#canRemoveItemGroup(java.lang.String, java.lang.Long)
	 */
	public boolean canRemoveItemGroup(String userId, Long itemGroupId) {
		log.debug("itemGroupId:" + itemGroupId + ", userId:" + userId);

		// get the item by id
		EvalItemGroup itemGroup = (EvalItemGroup) dao.findById(EvalItemGroup.class, itemGroupId);
		if (itemGroup == null) {
			throw new IllegalArgumentException("Cannot find item group item with id: " + itemGroupId);
		}

		// check perms
		try {
			return checkUserControlItemGroup(userId, itemGroup);
		} catch (RuntimeException e) {
			log.info(e.getMessage());
		}
		return false;
	}


	/**
	 * Check if an item group type is valid
	 * @param itemGroupTypeConstant
	 * @return true if valid, false otherwise
	 */
	public static boolean checkItemGroupType(String itemGroupTypeConstant) {
		if ( EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(itemGroupTypeConstant) ||
				EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE.equals(itemGroupTypeConstant) ) {
			return true;
		}
		return false;
	}


	protected boolean checkUserControlItemGroup(String userId, EvalItemGroup itemGroup) {
		log.debug("itemGroup: " + itemGroup.getId() + ", userId: " + userId);

		// check ownership or super user
		if ( external.isUserAdmin(userId) ) {
			return true;
		} else if ( itemGroup.getOwner().equals(userId) ) {
			return true;
		} else {
			throw new SecurityException("User ("+userId+") cannot control item group ("+itemGroup.getId()+") without permissions");
		}
	}

}
