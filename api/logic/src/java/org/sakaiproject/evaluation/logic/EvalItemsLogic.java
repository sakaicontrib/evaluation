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
import org.sakaiproject.evaluation.model.EvalTemplateItem;


/**
 * Handles all logic associated with the items and links to templates in the system
 * (Note for developers - do not modify this without permission from the project lead)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalItemsLogic {

	/**
	 * Get an item by its unique id<br/>
	 * An item represents a reusable question item in the system<br/>
	 * Note: if you need to get a group of items
	 * then use {@link #getItemsOwnedByUser(String)} or 
	 * use the template collection of templateItems to get the items
	 * 
	 * @param itemId the id of an EvalItem object
	 * @return an {@link EvalItem} object or null if not found
	 */
	public EvalItem getItemById(Long itemId);

	/**
	 * Save or update the item only if it is not locked, 
	 * use {@link #canControlItem(String, Long)} if you want to check
	 * if user has permission and avoid exceptions<br/>
	 * Validates that settings are correct for this type of item,
	 * enforces sharing constraints, and fills in the optional values with defaults
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
	 */
	public void deleteItem(Long itemId, String userId);

	/**
	 * Get a list of all the items visible to a specific user,
	 * can limit it to only items owned by that user or
	 * items by sharing level, can exclude expert items<br/>
	 * <b>Note:</b> Does not include any block items (parents)
	 * 
	 * @param userId the internal user id (not username)
	 * @param sharingConstant a SHARING constant from 
	 * {@link org.sakaiproject.evaluation.model.constant.EvalConstants},
	 * if null, return all items visible to the
	 * user, if set to a sharing constant then return just the visible
	 * items that match that sharing setting (can be used to get all
	 * items owned by this user for example)
	 * @param filter text which will filter the returned items to those which have
	 * a portion that matches this text, if null or blank string then matches all
	 * @param includeExpert if true, then include expert Items, if false, leave out expert items
	 * @return a list of {@link EvalItem} objects
	 */
	public List getItemsForUser(String userId, String sharingConstant, String filter, boolean includeExpert);

	/**
	 * Get a list of items in a template that are visible to a user, 
	 * most of the time you will want to get the items by getting the 
	 * templateItems from the template and then
	 * using that to get the items themselves or 
	 * using {@link #getTemplateItemsForTemplate(Long, String)},
	 * but if you do not have the template OR you need the items
	 * restricted to visibility to a specific user then use this method
	 * 
	 * @param templateId the unique id of an EvalTemplate object
	 * @param userId the internal user id (not username), if this is null then
	 * it will return all items in the template
	 * @return a list of {@link EvalItem} objects
	 */
	public List getItemsForTemplate(Long templateId, String userId);

	// TEMPLATE ITEMS

	/**
	 * Get a template item by its unique id<br/>
	 * A template item represents a specific instance of an item in a specific template<br/>
	 * Note: if you need to get a group of template items
	 * then use {@link #getTemplateItemsForTemplate(Long)}
	 * 
	 * @param templateItemId the id of an EvalTemplateItem object
	 * @return an {@link EvalTemplateItem} object or null if not found
	 */
	public EvalTemplateItem getTemplateItemById(Long templateItemId);

	/**
	 * Save a templateItem to create a link between an item and a template or
	 * update display settings for an item in a template,
	 * template items cannot be saved in locked templates, the item and template
	 * cannot be changed after the templateItem is created<br/>
	 * A template item represents a specific instance of an item in a specific template<br/>
	 * Validates display settings based on the type of item, creates the association between
	 * the template and this templateItem and the item and this templateItem,
	 * fills in default optional values, sets the display order correctly for newly
	 * created items (to the next available number)
	 * 
	 * @param templateItem a templateItem object to be saved
	 * @param userId the internal user id (not username)
	 */
	public void saveTemplateItem(EvalTemplateItem templateItem, String userId);

	/**
	 * Remove a template item (this removes the item from the template effectively),
	 * template items cannot be removed from locked templates,
	 * use {@link #canControlTemplateItem(String, Long)} to check if a
	 * user has permissions and avoid possible exceptions<br/>
	 * <b>Note:</b> This does not remove the associated item
	 * 
	 * @param templateItemId the id of an EvalTemplateItem object
	 * @param userId the internal user id (not username)
	 */
	public void deleteTemplateItem(Long templateItemId, String userId);

	/**
	 * Get all the templateItems for this template that are visible to a user, 
	 * most of the time you will want to just get the items by getting the 
	 * templateItems from the template itself,
	 * but if you do not have the template OR you need the templateItems
	 * restricted to visibility to a specific user then use this method
	 * 
	 * @param templateId the unique id of an EvalTemplate object
	 * @param userId the internal user id (not username), if this is null then
	 * it will return all items in the template
	 * @return a list of {@link EvalTemplateItem} objects, ordered by displayOrder
	 */
	public List getTemplateItemsForTemplate(Long templateId, String userId);


	// BLOCKS

	/**
	 * Get the child block templateItems for a parent templateItem,
	 * optionally include the parent templateItem,
	 * returns the items in display order (with parent first if requested)
	 * 
	 * @param parentId the unique id of the parent {@link EvalTemplateItem} object
	 * @param includeParent if false then only return child items, if true then return the entire block (parent and child items)
	 * @return a List of {@link EvalTemplateItem} objects
	 */
	public List getBlockChildTemplateItemsForBlockParent(Long parentId, boolean includeParent);

	// EXPERT ITEMS

	// TODO - stuff here! -AZ

	// PERMISSIONS

	/**
	 * Check if a user can control (update or delete) a specific item,
	 * locked items cannot be modified in any way
	 * 
	 * @param userId the internal user id (not username)
	 * @param itemId the id of an {@link EvalItem} object
	 * @return true if user can control this item, false otherwise
	 */
	public boolean canControlItem(String userId, Long itemId);

	/**
	 * Check if a user can control (update or delete) a specific templateItem,
	 * templateItems associated with a locked template cannot be modified
	 * 
	 * @param userId the internal user id (not username)
	 * @param templateItemId the id of an {@link EvalTemplateItem} object
	 * @return true if user can control this templateItem, false otherwise
	 */
	public boolean canControlTemplateItem(String userId, Long templateItemId);

}
