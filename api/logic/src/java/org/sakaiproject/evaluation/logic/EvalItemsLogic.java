/******************************************************************************
 * EvalItemsLogic.java - created by aaronz@vt.edu on Dec 24, 2006
 *****************************************************************************/

package org.sakaiproject.evaluation.logic;

import java.util.List;

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
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
	 * Get an item by its external id<br/>
	 * An item represents a reusable question item in the system<br/>
	 * Note: An item eid is null except when the item was imported
	 * from an external system.
	 * 
	 * @param eid the id of an EvalItem object in an external system
	 * @return an {@link EvalItem} object or null if not found
	 */
	public EvalItem getItemByEid(String eid);

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
	public List<EvalItem> getItemsForUser(String userId, String sharingConstant, String filter, boolean includeExpert);

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
	public List<EvalItem> getItemsForTemplate(Long templateId, String userId);

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
	 * Get a template item by its external id<br/>
	 * Note: A template item eid is null except when the template item was imported
	 * from an external system.
	 * 
	 * @param eid the id of an EvalTemplateItem object in an external system
	 * @return an {@link EvalTemplateItem} object or null if not found
	 */
	public EvalTemplateItem getTemplateItemByEid(String eid);

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
    * Get all the templateItems (TIs) for a template based on a set of restrictions, this is primarily for
    * cases where you will be rendering a preview of an template<br/>
    * NOTE: You should use this in place of lazy loading the template items from the template
	 * 
	 * @param templateId the unique id of an {@link EvalTemplate} object
    * @param nodeIds the unique ids of a set of hierarchy nodes for which we 
    * want all associated template items, null excludes all TIs associated with nodes,
    * an empty array will include all TIs associated with nodes
    * @param instructorIds a set of internal userIds of instructors for instructor added items,
    * null will exclude all instructor added items, empty array will include all
    * @param groupIds the unique eval group ids associated with a set of TIs in this template
    * (typically items which are associated with a specific eval group),
    * null excludes all associated TIs, empty array includes all 
    * @return a list of {@link EvalTemplateItem} objects, ordered by displayOrder and template
	 */
	public List<EvalTemplateItem> getTemplateItemsForTemplate(Long templateId, String[] nodeIds, String[] instructorIds, String[] groupIds);

	/**
	 * Get all the templateItems for an evaluation based on a set of restrictions, this is primarily for
	 * cases where you will be rendering an evaluation for a user or possibly a preview of an evaluation<br/>
	 * NOTE: Use this instead of attempting to lazy load a ton of items from the templates
	 * 
    * @param evalId the unique id of an {@link EvalEvaluation} object
    * @param nodeIds the unique ids of a set of hierarchy nodes for which we 
    * want all associated template items, null excludes all TIs associated with nodes,
    * an empty array will include all TIs associated with nodes
    * @param instructorIds a set of internal userIds of instructors for instructor added items,
    * null will exclude all instructor added items, empty array will include all
    * @param groupIds the unique eval group ids associated with a set of TIs in this template
    * (typically items which are associated with a specific eval group),
    * null excludes all associated TIs, empty array includes all 
	 * @return a list of {@link EvalTemplateItem} objects, ordered by displayOrder and template
	 */
	public List<EvalTemplateItem> getTemplateItemsForEvaluation(Long evalId, String[] nodeIds, String[] instructorIds, String[] groupIds);

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
	public List<EvalTemplateItem> getBlockChildTemplateItemsForBlockParent(Long parentId, boolean includeParent);


	// PERMISSIONS

	/**
	 * Check if a user can modify a specific item,
	 * locked items cannot be modified in any way
	 * 
	 * @param userId the internal user id (not username)
	 * @param itemId the id of an {@link EvalItem} object
	 * @return true if user can modify this item, false otherwise
	 */
	public boolean canModifyItem(String userId, Long itemId);

	/**
	 * Check if a user can remove a specific item,
	 * locked items cannot be removed,
	 * items used in a template (with an associated templateItem) cannot be removed
	 * 
	 * @param userId the internal user id (not username)
	 * @param itemId the id of an {@link EvalItem} object
	 * @return true if user can remove this item, false otherwise
	 */
	public boolean canRemoveItem(String userId, Long itemId);


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
