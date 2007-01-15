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
	 */
	public void deleteItem(Long itemId, String userId);

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
	 * @return a list of {@link EvalItem} objects
	 */
	public List getItemsForUser(String userId, String sharingConstant);

	/**
	 * Get a list of items in a template that are visible to a user, 
	 * most of the time you will want to get the items by getting the 
	 * templateItems from the template and then
	 * using that to get the items themselves or 
	 * using {@link #getTemplateItemsForTemplate(Long)},
	 * but if you do not have the template and do not need the template items
	 * then use this method
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
	 * Save a template item to create a link between an item and a template or
	 * update display settings for an item in a template,
	 * template items cannot be saved in locked templates<br/>
	 * A template item represents a specific instance of an item in a specific template
	 * 
	 * @param templateItem a templateItem object to be saved
	 * @param userId the internal user id (not username)
	 */
	public void saveTemplateItem(EvalTemplateItem templateItem, String userId);

	/**
	 * Remove a template item (this removes the item from the template effectively),
	 * template items cannot be removed from locked templates,
	 * use {@link #canControlTemplateItem(String, Long)} to check if a
	 * user has permissions and avoid possible exceptions
	 * <b>Note:</b> This does not remove the associated item
	 * 
	 * @param templateItemId the id of an EvalTemplateItem object
	 * @param userId the internal user id (not username)
	 */
	public void deleteTemplateItem(Long templateItemId, String userId);

	/**
	 * Get all the templateItems for this template, most of the time you will want to
	 * get the items by getting the templateItems from the template,
	 * but if you do not have the template and do not need the template items
	 * then use this method
	 * 
	 * @param templateId the unique id of an EvalTemplate object
	 * @return a list of {@link EvalTemplateItem} objects
	 */
	public List getTemplateItemsForTemplate(Long templateId);


	// BLOCK

	/**
	 * Gets the next unique unused block id that is available
	 * 
	 * @return the next block id
	 */
	public Integer getNextBlockId();


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
