/******************************************************************************
 * EvalExpertItemLogic.java - created by aaronz on 5 Mar 2007
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

package org.sakaiproject.evaluation.logic;

import java.util.List;

import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * Handles the logic related to expert items including fetching existing items and creating new ones
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalExpertItemsLogic {

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
	 * Get all items contained within an item group (this does NOT retrieve items contained within subgroups of the given group)
	 * 
	 * @param itemGroupId the unique id of an {@link EvalItemGroup}
	 * @param includeExpert if true then include expert groups only, else include non-expert groups only
	 * @return a List of {@link EvalItem} objects, ordered by title alphabetically
	 */
	public List<EvalItem> getItemsInItemGroup(Long itemGroupId, boolean includeExpert);


	/**
	 * @param itemGroupId the unique id of an {@link EvalItemGroup}
	 * @return an {@link EvalItemGroup} object or null if none found
	 */
	public EvalItemGroup getItemGroupById(Long itemGroupId);

	/**
	 * Save a new item group (category, objective, etc.)<br/>
	 * All aspects of the group can be changed at any time, note that there are special rules about the
	 * various group types (indicated by {@link EvalConstants#ITEM_GROUP_TYPE}) which must be adhered to
	 * 
	 * @param itemGroup a previously persisted {@link EvalItemGroup} object
	 * @param userId the internal user id (not username)
	 */
	public void saveItemGroup(EvalItemGroup itemGroup, String userId);

	/**
	 * Remove an item group<br/>
	 * Note that this will also remove all subgroups (does NOT remove any items contained within the group)
	 * 
	 * @param itemGroupId the unique id of an {@link EvalItemGroup}
	 * @param userId the internal user id (not username)
	 * @param removeNonEmptyGroup if true, then remove groups which contain items, otherwise do not remove
	 * groups which contain items
	 */
	public void removeItemGroup(Long itemGroupId, String userId, boolean removeNonEmptyGroup);

	/**
	 * Check if a user can update a specific item group
	 * 
	 * @param userId the internal user id (not username)
	 * @param itemGroupId the unique id of an {@link EvalItemGroup}
	 * @return true if the item group can be updated by this user, false otherwise
	 */
	public boolean canUpdateItemGroup(String userId, Long itemGroupId);

	/**
	 * Check if a user can remove a specific item group, 
	 * cannot remove groups which are not empty
	 * 
	 * @param userId the internal user id (not username)
	 * @param itemGroupId the unique id of an {@link EvalItemGroup}
	 * @return true if the item group can be removed by this user, false otherwise
	 */
	public boolean canRemoveItemGroup(String userId, Long itemGroupId);

}