/******************************************************************************
 * EvalItemsLogic.java - created by aaronz@vt.edu on Dec 24, 2006
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

package org.sakaiproject.evaluation.logic;

import java.util.List;

import org.sakaiproject.evaluation.model.EvalItem;


/**
 * Handles all logic associated with the items and links to templates in the system
 * (Note for developers - do not modify this without permission from the project lead)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalItemsLogic {

	/**
	 * Get an item by its unique id<br/> 
	 * Note: if you need to get a group of items
	 * then use {@link #getItemsOwnedByUser(String)} or use the template
	 * collection of template items
	 * 
	 * @param itemId the id of an EvalItem object
	 * @return the item or null if not found
	 */
	public EvalItem getItemById(Long itemId);

	/**
	 * Save or update the item only if it is not locked, 
	 * use {@link #canControlItem(String, Long)} if you want to check
	 * if user has permission and avoid exceptions
	 * 
	 * @param item an item object to be saved
	 * @param userId the internal user id (not username)
	 */
	public void saveItem(EvalItem item, String userId);

	/**
	 * Deletes a stored item, locked and expert items cannot be removed, 
	 * use {@link #canControlItem(String, Long)} if you want to check
	 * if user has permission and avoid exceptions
	 * 
	 * @param itemId the id of an EvalItem object
	 * @param userId the internal user id (not username)
	 * @return true if the item was found and removed, false otherwise
	 */
	public boolean deleteItem(Long itemId, String userId);

	/**
	 * Get a list of all the items visible to a specific user,
	 * can limit it to only items owned by that user
	 * 
	 * @param userId the internal user id (not username)
	 * @param sharingConstant a SHARING constant from 
	 * {@link org.sakaiproject.evaluation.model.constant.EvalConstants},
	 * if null, return all items visible to the
	 * user, if set to a sharing constant then return just the visible
	 * items that match that sharing setting (can be used to get all
	 * items owned by this user for example)
	 * @return a list of EvalItem objects
	 */
	public List getItemsForUser(String userId, String sharingConstant);


	// PERMISSIONS

	/**
	 * Check if a user can control (update or delete) a specific item,
	 * locked items cannot be modified in any way
	 * 
	 * @param userId the internal user id (not username)
	 * @param itemId the id of an EvalItem object
	 * @return true if user can control this item, false otherwise
	 */
	public boolean canControlItem(String userId, Long itemId);

}
