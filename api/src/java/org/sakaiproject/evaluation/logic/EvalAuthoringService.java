/**
 * $Id$
 * $URL$
 * EvalAuthoringService.java - evaluation - Jan 30, 2008 11:02:36 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic;

import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;


/**
 * Handles all template/item/scale authoring<br/>
 * Allows the creation of all the things that can be put together to create an evalaution,
 * this does not handle the creation of the evaluation itself, that is handled in the
 * setup/construction service
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface EvalAuthoringService {

   // SCALES

   /**
    * Get a scale by its unique id<br/>
    * <b>Note:</b> You should get the scale from the item most of the time<br/>
    * <b>Note:</b> If you need to get a group of scales
    * then use {@link #getScalesForUser(String, String)}
    * 
    * @param scaleId the id of an EvalScale object
    * @return the item or null if not found
    */
   public EvalScale getScaleById(Long scaleId);
   
   /**
    * Get a scale by its external id<br/>
    * Note: A scale eid is null except when the scale was imported
    * from an external system.
    * 
    * @param eid the id of an EvalScale object in an external system
    * @return an {@link EvalScale} object or null if not found
    */
   public EvalScale getScaleByEid(String eid);

   /**
    * Create or update a scale (update only if it is not locked),
    * cannot change locked setting for existing scales,
    * 
    * 
    * @param scale a scale object to be saved
    * @param userId the internal user id (not username)
    * @throws UniqueFieldException is this scale title is already in use and this is not an adhoc scale
    */
   public void saveScale(EvalScale scale, String userId);

   /**
    * Deletes a stored scale, locked scales cannot be removed,
    * use {@link #canControlScale(String, Long)} if you want to 
    * check if a user has permission and avoid possible exceptions
    * 
    * @param scaleId the id of an EvalScale object
    * @param userId the internal user id (not username)
    */
   public void deleteScale(Long scaleId, String userId);

   /**
    * Get evaluation scales that are visible to the supplied user
    * (includes owned scales and public or shared scales), can
    * optionally get just owned scales, scales are sorted
    * 
    * @param userId the internal user id (not username)
    * @param sharingConstant a SHARING constant from 
    * {@link org.sakaiproject.evaluation.constant.EvalConstants},
    * if null, return all scales visible to the
    * user, if set to a sharing constant then return just the visible
    * scales that match that sharing setting (can be used to get all
    * scales owned by this user for example)
    * @return a List of EvalScale objects (in alpha order with private scales, then public, then others)
    */
   public List<EvalScale> getScalesForUser(String userId, String sharingConstant);


   // PERMISSIONS

   /**
    * Check if a user can modify a specific scale,
    * locked scales cannot be modified in any way
    * 
    * @param userId the internal user id (not username)
    * @param scaleId the id of an EvalScale object
    * @return true if user can modify this scale, false otherwise
    */
   public boolean canModifyScale(String userId, Long scaleId);

   /**
    * Check if a user can remove a specific scale,
    * locked scales cannot be removed,
    * scales that are used in an item cannot be removed
    * 
    * @param userId the internal user id (not username)
    * @param scaleId the id of an EvalScale object
    * @return true if user can remove this scale, false otherwise
    */
   public boolean canRemoveScale(String userId, Long scaleId);

   // ITEMS

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
    * {@link org.sakaiproject.evaluation.constant.EvalConstants},
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
    * Handles the removal of a templateItem, includes security check and 
    * takes care of reordering or other items in the template<br/>
    * Blocks: splits up the block and removes the parent item if a block parent is selected for removal <br/>
    * Also attempts to remove the associated item if it is no longer being used <br/>
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

   // ITEM GROUPS

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

   // TEMPLATES

   /**
    * Get the template associated with this template id
    * 
    * @param templateId the unique id of a template
    * @return the template object or null if not found
    */
   public EvalTemplate getTemplateById(Long templateId);
   
   /**
    * Get the template associated with this external id<br/>
    * Note: A template eid is null except when the template
    * was imported from an external system.
    * 
    * @param eid the id of a template in an external system
    * @return the template object or null if not found
    */
   public EvalTemplate getTemplateByEid(String eid);

   /**
    * Save or update the template only if it is not locked<br/>
    * Locks any associated items if this template is locked<br/>
    * Use {@link #canCreateTemplate(String)} or {@link #canControlTemplate(String, Long)}
    * to check if user can save template and avoid exceptions<br/>
    * Also will validate associated templateItems and ensure the ordering is valid
    * 
    * @param template the object to be saved
    * @param userId the internal user id (not username)
    * @throws UniqueFieldException is this template title is already in use
    */
   public void saveTemplate(EvalTemplate template, String userId);

   /**
    * Delete the template only if it is not locked and not expert,
    * also removes all associated templateItems and unlinks associated items<br/>
    * Unlocks any associated items that are not being used in other locked templates<br/>
    * Use {@link #canControlTemplate(String, Long)} to check if
    * the user can control this template and avoid exceptions<br/>
    * <b>NOTE:</b> This will also remove all the children items and scales if this
    * template is detected to be a hidden copy (indicating no one can see or use
    * it other than the associated evaluation which would have locked it)
    * 
    * @param template the object to be removed
    * @param userId the internal user id (not username)
    */
   public void deleteTemplate(Long templateId, String userId);

   /**
    * Get all accessible templates for a user, this includes all private templates, 
    * and public templates as desired<br/>
    * (currently does not include shared or visible templates)
    * 
    * @param userId the internal user id (not username)
    * @param sharingConstant a SHARING constant from 
    * {@link org.sakaiproject.evaluation.constant.EvalConstants},
    * if null, return all templates visible to the
    * user, if set to a sharing constant then return just the visible
    * templates that match that sharing setting (can be used to get all
    * templates owned by this user for example)
    * @param includeEmpty if true then include templates with no items in them, else
    * only return templates that have items
    * @return a list of EvalTemplate objects
    */
   public List<EvalTemplate> getTemplatesForUser(String userId, String sharingConstant, boolean includeEmpty);


   // PERMISSIONS

   /**
    * Check if a user can create templates (should check system wide)
    * 
    * @param userId the internal user id (not username)
    * @return true if the user can create templates, false otherwise
    */
   public boolean canCreateTemplate(String userId);

   /**
    * Check if a user can modify a template,
    * locked templates cannot be modified
    * 
    * @param userId the internal user id (not username)
    * @param templateId the id of an {@link EvalTemplate} object
    * @return true if the user can control the template, false otherwise
    */
   public boolean canModifyTemplate(String userId, Long templateId);

   /**
    * Check if a user can delete a template,
    * locked templates or those used in an evaluation cannot be removed
    * 
    * @param userId the internal user id (not username)
    * @param templateId the id of an {@link EvalTemplate} object
    * @return true if the user can remove the template, false otherwise
    */
   public boolean canRemoveTemplate(String userId, Long templateId);



   // COPYING

   /**
    * Makes a complete copy of a template (also copies all related templateItems), 
    * can also copy items, and scales if specified,
    * does not copy expert indication and forces the sharing to be set to private
    * <br/>
    * Permission checks should be performed to ensure that the current user can view the entities that they want to copy,
    * there is no need to check if they can modify them though as the original entities will not be changed<br/>
    * Validates order or template items
    * 
    * @param templateId the unique id of an {@link EvalTemplate} object to make a copy of
    * @param title the new title to assign to the copied template (will generate a title if this is blank or empty)
    * @param ownerId the internal user id of the user who will be the owner of the copies,
    * will use the current owner if this is null
    * @param hidden if this true then the copy will be hidden from control views by default,
    * else it will be shown in control views (internal copies should be hidden, user triggered copies should not)
    * @param includeChildren if true then all items and scales are also copied (templateItems are always copied),
    * if false the ids are used but copies are not made (in general you should copy all children when duplicating
    * but you probably only need to copy the object when the user makes a copy)
    * @return the unique id of the copy of the template
    * @throws IllegalArgumentException if the template id is invalid
    */
   public Long copyTemplate(Long templateId, String title, String ownerId, boolean hidden, boolean includeChildren);

   /**
    * Makes a copy of a set of template items,
    * also copies all related items and scales if specified
    * <br/>
    * <b>WARNING:</b> Be careful about copying blocks, you must include the complete set of 
    * templateItem ids when copying blocks or the copy will fail to work, block copying will
    * be handled specially so if you want to copy items which are part of a block without the block parent
    * you can do it but they will end up outside the block
    * <br/>
    * Permission checks should be performed to ensure that the current user can view the entities that they want to copy,
    * there is no need to check if they can modify them though as the original entities will not be changed
    * 
    * @param templateItemIds the unique ids of {@link EvalTemplateItem}s to make copies of
    * @param ownerId the internal user id of the user who will be the owner of the copies,
    * will use the current owner if this is null
    * @param hidden if this true then the copy will be hidden from control views by default,
    * else it will be shown in control views (internal copies should be hidden, user triggered copies should not)
    * @param toTemplateId this is the id of the template to copy these templateItems to,
    * if this is null then items will be copied to the same template they are currently in
    * @param includeChildren if true then all items and scales are also copied,
    * if false the ids are used but copies are not made (in general you should copy all children when duplicating
    * but you probably only need to copy the object when the user makes a copy)
    * @return the unique ids of the copies
    * @throws IllegalArgumentException if any of the ids are invalid
    */
   public Long[] copyTemplateItems(Long[] templateItemIds, String ownerId, boolean hidden, Long toTemplateId, boolean includeChildren);

   /**
    * Makes a copy of a set of items,
    * also copies all related scales if specified,
    * does not copy expert indication and forces the sharing to be set to private
    * <br/>
    * Permission checks should be performed to ensure that the current user can view the entities that they want to copy,
    * there is no need to check if they can modify them though as the original entities will not be changed
    * 
    * @param itemIds the unique ids of {@link EvalItem}s to make copies of
    * @param ownerId the internal user id of the user who will be the owner of the copies,
    * will use the current owner if this is null
    * @param hidden if this true then the copy will be hidden from control views by default,
    * else it will be shown in control views (internal copies should be hidden, user triggered copies should not)
    * @param includeChildren if true then all scales are also copied,
    * if false the ids are used but copies are not made (in general you should copy all children when duplicating
    * but you probably only need to copy the object when the user makes a copy)
    * @return the unique ids of the copies
    * @throws IllegalArgumentException if any of the ids are invalid
    */
   public Long[] copyItems(Long[] itemIds, String ownerId, boolean hidden, boolean includeChildren);

   /**
    * Makes a copy of a set of scales,
    * does not copy expert indication and forces the sharing to be set to private
    * <br/>
    * Permission checks should be performed to ensure that the current user can view the entities that they want to copy,
    * there is no need to check if they can modify them though as the original entities will not be changed
    * 
    * @param scaleIds the unique ids of {@link EvalScale}s to make copies of
    * @param title the new title to assign to the copied scales (will generate a title if this is blank or empty),
    * note that this will assign the same title to all the copied scales so be careful here
    * @param ownerId the internal user id of the user who will be the owner of the copies,
    * will use the current owner if this is null
    * @param hidden if this true then the copy will be hidden from control views by default,
    * else it will be shown in control views (internal copies should be hidden, user triggered copies should not)
    * @return the unique ids of the copies
    * @throws IllegalArgumentException if any of the ids are invalid
    */
   public Long[] copyScales(Long[] scaleIds, String title, String ownerId, boolean hidden);

   
   // IN-USE / USING checks

   /**
    * Get the list of all the items which are using a scale 
    * 
    * @param scaleId the unique id of an {@link EvalScale}
    * @return the list of all {@link EvalItem}s which are using this scale (or empty if none found)
    */
   public List<EvalItem> getItemsUsingScale(Long scaleId);

   /**
    * Get the list of all the templates which are using an item
    * 
    * @param itemId the unique id of an {@link EvalItem}
    * @return the list of all {@link EvalTemplate}s which are using this item (or empty if none found)
    */
   public List<EvalTemplate> getTemplatesUsingItem(Long itemId);

   // AUTO USE tags

   /**
    * Find all templates, templaeItems, and items with the given autoUseTag on them,
    * items from the templates found will be returned (without the template but in the correct order),
    * {@link EvalItem}s will be wrapped in a non-persisted {@link EvalTemplateItem},
    * will not return 2 copies of the same templateItem but if an item is used in a templateItem
    * then you may end up with 2 templateItems that appear to be the same<br/>
    * <b>NOTE:</b> does not check if this authoring object is visible so care should be taken to
    * control which items/template items/templates are allowed to have autoUseTags placed on them
    * 
    * @param templateAutoUseTag the autoUseTag field from {@link EvalTemplate},
    * will return all the templateItems from any templates with this tag,
    * null means skip checking for templates
    * @param templateItemAutoUseTag the autoUseTag field from {@link EvalTemplateItem},
    * will return all the templateItems with this tag,
    * null means skip checking for templateItems
    * @param itemAutoUseTag  the autoUseTag field from {@link EvalItem},
    * will return all the items with this tag (wrapped in a non-persisted templateItem),
    * null means skip checking for items
    * @return a list of template items
    */
   public List<EvalTemplateItem> getAutoUseTemplateItems(String templateAutoUseTag, String templateItemAutoUseTag, String itemAutoUseTag);

   /**
    * Insert all the autoUse items that have the given tag into the template identified by the given id,
    * this will automatically fixup the ordering and handle all the saves (if saveAll is set to true)<br/>
    * <b>NOTE:</b> saveAll allows you to simply retrieve the template with the autoUse items inserted (for preview purposes),
    * these templateItems are not appropriate for copying and only appropriate for previewing purposes
    * 
    * @param autoUseTag the autoUse tag which will be used to find items to insert into this template
    * @param templateId the unique id of an {@link EvalTemplate}
    * @param insertionPointConstant the constant to indicate where to insert the items in the template, 
    * example: {@link EvalConstants#EVALUATION_AUTOUSE_INSERTION_AFTER}
    * @param saveAll if this is true then the items will be inserted and the template will be updated and saved,
    * if this is false then no changes will be made to persistent objects and instead you receive back a list of
    * copies which have not been saved (and should not be saved)
    * @return a list of all templateItems in correct order after the insertion IF one was made, 
    * otherwise this will return null if no insertions were made
    */
   public List<EvalTemplateItem> doAutoUseInsertion(String autoUseTag, Long templateId, String insertionPointConstant, boolean saveAll);

   // IN USE checks

   /**
    * @param scaleId the unique id for an {@link EvalScale}
    * @return true if this scale is used in any items
    */
   public boolean isUsedScale(Long scaleId);

   /**
    * @param itemId the unique id for an {@link EvalItem}
    * @return true if this item is used in any template (i.e. connected to any template item)
    */
   public boolean isUsedItem(Long itemId);

   /**
    * @param templateId the unique id for an {@link EvalTemplate}
    * @return true if this template is used in any evalautions
    */
   public boolean isUsedTemplate(Long templateId);
   
   /**
    * Save the specified template items in the order they appear in the {@link Map}, security is checked against passed user id
    * @param orderingMap Item ids and corresponding positions to set
    * @param currentUserId the internal user id (not username)
    */
   public void saveTemplateItemOrder(Map<Long, Integer> orderingMap, String currentUserId);

}
