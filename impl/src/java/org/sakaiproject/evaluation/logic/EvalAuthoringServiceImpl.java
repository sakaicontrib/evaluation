/**
 * $Id$
 * $URL$
 * EvalAuthoringServiceImpl.java - evaluation - Jan 30, 2008 11:08:27 AM - azeckoski
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.exceptions.BlankRequiredFieldException;
import org.sakaiproject.evaluation.logic.externals.EvalSecurityChecksImpl;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;


/**
 * Implementation of the authoring logic
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalAuthoringServiceImpl implements EvalAuthoringService {

    private static Log log = LogFactory.getLog(EvalAuthoringServiceImpl.class);

    // Event names cannot be over 32 chars long     // max-32:12345678901234567890123456789012
    private final String EVENT_TEMPLATE_CREATE =             "eval.template.added";
    private final String EVENT_TEMPLATE_UPDATE =             "eval.template.updated";
    private final String EVENT_TEMPLATE_DELETE =             "eval.template.removed";

    private final String EVENT_SCALE_CREATE =                "eval.scale.added";
    private final String EVENT_SCALE_UPDATE =                "eval.scale.updated";
    private final String EVENT_SCALE_DELETE =                "eval.scale.removed";

    private final String EVENT_ITEM_CREATE =                 "eval.item.added";
    private final String EVENT_ITEM_UPDATE =                 "eval.item.updated";
    private final String EVENT_ITEM_DELETE =                 "eval.item.removed";

    private final String EVENT_TEMPLATEITEM_CREATE =         "eval.templateitem.added";
    private final String EVENT_TEMPLATEITEM_UPDATE =         "eval.templateitem.updated";
    private final String EVENT_TEMPLATEITEM_DELETE =         "eval.templateitem.removed";


    private EvaluationDao dao;
    public void setDao(EvaluationDao dao) {
        this.dao = dao;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    private EvalSecurityChecksImpl securityChecks;
    public void setSecurityChecks(EvalSecurityChecksImpl securityChecks) {
        this.securityChecks = securityChecks;
    }



    public void init() {
        // this method will help us to patch up the system if needed

        // fix up the scales with null modes
        List<EvalScale> scales = dao.findBySearch(EvalScale.class,
                new Search( new Restriction("mode", "", Restriction.NULL) ) );
        if (scales.size() > 0) {
            log.info("Found " + scales.size() + " scales with a null mode, fixing up data...");
            for (EvalScale scale : scales) {
                // set this to the default then
                scale.setMode(EvalConstants.SCALE_MODE_SCALE);
            }
            dao.saveSet(new HashSet<EvalScale>(scales));
            log.info("Fixed " + scales.size() + " scales with a null mode, set to default SCALE_MODE...");
        }
    }


    public EvalScale getScaleById(Long scaleId) {
        log.debug("scaleId: " + scaleId );
        // get the scale by passing in id
        EvalScale scale = (EvalScale) dao.findById(EvalScale.class, scaleId);
        return scale;
    }

    public EvalScale getScaleByEid(String eid) {
        log.debug("scale eid: " + eid);
        EvalScale evalScale = null;
        if (eid != null) {
            List<EvalScale> evalScales = dao.findBySearch(EvalScale.class, new Search("eid", eid));
            if (evalScales != null && evalScales.size() == 1) {
                evalScale = evalScales.get(0);
            }
        }
        return evalScale;
    }

    public void saveScale(EvalScale scale, String userId) {
        log.debug("userId: " + userId + ", scale: " + scale.getTitle());

        // set the date modified
        scale.setLastModified( new Date() );

        // check for null or length 0 or 1 options
        if (scale.getOptions() == null ||
                scale.getOptions().length <= 1) {
            throw new IllegalArgumentException("Scale options cannot be null and must have at least 2 items");
        }

        // check the sharing constants
        if (scale.getSharing() == null) {
            scale.setSharing(EvalConstants.SHARING_PRIVATE);
        }
        EvalUtils.validateSharingConstant(scale.getSharing());
        if ( EvalConstants.SHARING_OWNER.equals(scale.getSharing()) ) {
            throw new IllegalArgumentException("Invalid sharing constant ("+scale.getSharing()+") set for scale ("+scale.getTitle()+"), cannot use SHARING_OWNER");
        } else if ( EvalConstants.SHARING_PUBLIC.equals(scale.getSharing()) ) {
            // test if non-admin trying to set public sharing
            if (! commonLogic.isUserAdmin(userId) ) {
                throw new IllegalArgumentException("Only admins can set scale ("+scale.getTitle()+") sharing to public");
            }
        }

        // check locking not changed
        boolean newScale = true;
        if (scale.getId() != null) {
            newScale = false;
            //         // existing scale, don't allow change to locked setting
            //
            //         // TODO - this does not work, it just gets the persistent scale from memory -AZ
            //         EvalScale existingScale = getScaleOrFail(scale.getId());
            //
            //         if (! existingScale.getLocked().equals(scale.getLocked())) {
            //            throw new IllegalArgumentException("Cannot change locked setting on existing scale (" + scale.getId() + ")");
            //         }
        }

        // check perms and save
        if (securityChecks.checkUserControlScale(userId, scale)) {
            // fill in any default values and nulls here
            if (scale.getMode() == null) {
                // set this to the default then
                scale.setMode(EvalConstants.SCALE_MODE_SCALE);
            }

            if (scale.getLocked() == null) {
                scale.setLocked( Boolean.FALSE );
            }

            // replace adhoc default title with a unique title
            if (EvalConstants.SCALE_ADHOC_DEFAULT_TITLE.equals(scale.getTitle())) {
                scale.setTitle("adhoc-" + EvalUtils.makeUniqueIdentifier(100));
            }

            // check for required title
            if (EvalUtils.isBlank(scale.getTitle())) {
                throw new BlankRequiredFieldException("Cannot save a scale with a blank title", "title");
            } else {
                // cleanup for XSS scripting and strings
                scale.setTitle( commonLogic.cleanupUserStrings(scale.getTitle()) );
            }

            // now save the scale
            dao.save(scale);
            if (newScale) {
                commonLogic.registerEntityEvent(EVENT_SCALE_CREATE, scale);
            } else {
                commonLogic.registerEntityEvent(EVENT_SCALE_UPDATE, scale);
            }
            log.info("User ("+userId+") saved scale ("+scale.getId()+"), title: " + scale.getTitle());
            return;
        }

        // should not get here so die if we do
        throw new RuntimeException("User ("+userId+") could NOT save scale ("+scale.getId()+"), title: " + scale.getTitle());
    }


    public void deleteScale(Long scaleId, String userId) {
        log.debug("userId: " + userId + ", scaleId: " + scaleId );
        // get the scale by id
        EvalScale scale = getScaleOrFail(scaleId);

        // ADMIN CAN REMOVE EXPERT SCALES -AZ
        //    // cannot remove expert scales
        //    if (scale.getExpert().booleanValue()) {
        //    throw new IllegalStateException("Cannot remove expert scale: " + scaleId);
        //    }

        // check perms and remove
        if (securityChecks.checkUserControlScale(userId, scale)) {
            dao.delete(scale);
            commonLogic.registerEntityEvent(EVENT_SCALE_DELETE, scale);
            log.info("User ("+userId+") deleted scale ("+scale.getId()+"), title: " + scale.getTitle());
            return;
        }

        // should not get here so die if we do
        throw new RuntimeException("User ("+userId+") could NOT delete scale ("+scale.getId()+")");
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalScalesLogic#getScalesForUser(java.lang.String, java.lang.String)
     */
    public List<EvalScale> getScalesForUser(String userId, String sharingConstant) {
        log.debug("userId: " + userId + ", sharingConstant: " + sharingConstant );

        if (userId == null) {
            throw new IllegalArgumentException("Must include a userId");
        }

        // admin always gets all of the templates of a type
        if (commonLogic.isUserAdmin(userId)) {
            userId = null;
        }

        String[] sharingConstants = makeSharingConstantsArray(sharingConstant);

        // only get type standard templates
        String[] props = new String[] { "mode" };
        Object[] values = new Object[] { EvalConstants.SCALE_MODE_SCALE };
        int[] comparisons = new int[] { Restriction.EQUALS };

        String[] order = new String[] {"title"};
        String[] options = new String[] {"notHidden"};

        return dao.getSharedEntitiesForUser(EvalScale.class, userId, sharingConstants, 
                props, values, comparisons, order, options, 0, 0);
    }


    // PERMISSIONS

    public boolean canModifyScale(String userId, Long scaleId) {
        log.debug("userId: " + userId + ", scaleId: " + scaleId );
        // get the scale by id
        EvalScale scale = getScaleOrFail(scaleId);

        // check perms and locked
        boolean allowed = false;
        try {
            allowed = securityChecks.checkUserControlScale(userId, scale);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return allowed;
    }

    public boolean canRemoveScale(String userId, Long scaleId) {
        log.debug("userId: " + userId + ", scaleId: " + scaleId );
        // get the scale by id
        EvalScale scale = getScaleOrFail(scaleId);

        // can remove scales that are in use now
        //      // cannot remove scales that are in use
        //      if (dao.isUsedScale(scaleId)) {
        //         log.debug("Cannot remove scale ("+scaleId+") which is used in at least one item");
        //         return false;
        //      }

        // check perms and locked
        boolean allowed = false;
        try {
            allowed = securityChecks.checkUserControlScale(userId, scale);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return allowed;
    }

    /**
     * Get a scale by id or die if not found
     * @param scale
     * @return the scale
     * @throws IllegalArgumentException if no scale found
     */
    protected EvalScale getScaleOrFail(Long scaleId) {
        EvalScale scale = getScaleById(scaleId);
        if (scale == null) {
            throw new IllegalArgumentException("Cannot find scale with id: " + scaleId);
        }
        return scale;
    }



    // ITEMS


    public EvalItem getItemById(Long itemId) {
        log.debug("itemId:" + itemId);
        EvalItem item = (EvalItem) dao.findById(EvalItem.class, itemId);
        return item;
    }


    public EvalItem getItemByEid(String eid) {
        EvalItem evalItem = null;
        if (eid != null) {
            log.debug("eid: " + eid);
            List<EvalItem> evalItems = dao.findBySearch(EvalItem.class, new Search("eid", eid));
            if (evalItems != null && evalItems.size() == 1) {
                evalItem = evalItems.get(0);
            }
        }
        return evalItem;
    }


    public void saveItem(EvalItem item, String userId) {
        log.debug("item:" + item.getId() + ", userId:" + userId);

        // set the date modified
        item.setLastModified( new Date() );

        // check for required fields first
        if (EvalUtils.isBlank(item.getItemText())) {
            throw new BlankRequiredFieldException("Cannot save an item with a blank text", "itemText");
        }

        // validates the item based on the classification
        TemplateItemUtils.validateItemByClassification(item);

        // check the sharing constants
        EvalUtils.validateSharingConstant(item.getSharing());
        if ( EvalConstants.SHARING_OWNER.equals(item.getSharing()) ) {
            throw new IllegalArgumentException("Invalid sharing constant ("+item.getSharing()+") set for item ("+item.getItemText()+"), cannot use SHARING_OWNER");
        } else if ( EvalConstants.SHARING_PUBLIC.equals(item.getSharing()) ) {
            // test if non-admin trying to set public sharing
            if (! commonLogic.isUserAdmin(userId) ) {
                throw new IllegalArgumentException("Only admins can set item ("+item.getItemText()+") sharing to public");
            }
        }

        boolean newItem = true;
        if (item.getId() != null) {
            newItem = false;
            // existing item, don't allow change to locked setting
            // TODO this does not work, it just returns the same object that is already loaded
            EvalItem existingItem = getItemOrFail(item.getId());

            if (! existingItem.getLocked().equals(item.getLocked())) {
                throw new IllegalArgumentException("Cannot change locked setting on existing item (" + item.getId() + ")");
            }
        }

        if (securityChecks.checkUserControlItem(userId, item)) {
            // fill in the default settings for optional unspecified values
            if (item.getLocked() == null) {
                item.setLocked( Boolean.FALSE );
            }

            // check the NOT_AVAILABLE_ALLOWED system setting
            Boolean naAllowed = (Boolean) settings.get(EvalSettings.ENABLE_NOT_AVAILABLE);
            if (naAllowed.booleanValue()) {
                // can set NA
                if (item.getUsesNA() == null) {
                    item.setUsesNA( Boolean.FALSE );
                }
            } else {
                item.setUsesNA( Boolean.FALSE );
            }
            if (item.getCategory() == null) {
                item.setCategory( EvalConstants.ITEM_CATEGORY_COURSE );
            }

            if (item.isCompulsory() == null) 
                item.setCompulsory(false);
            // cleanup for XSS scripting and strings
            item.setItemText( commonLogic.cleanupUserStrings(item.getItemText()) );
            item.setDescription( commonLogic.cleanupUserStrings(item.getDescription()) );
            item.setExpertDescription( commonLogic.cleanupUserStrings(item.getExpertDescription()) );

            // save the item
            dao.save(item);
            if (newItem) {
                commonLogic.registerEntityEvent(EVENT_ITEM_CREATE, item);
            } else {
                commonLogic.registerEntityEvent(EVENT_ITEM_UPDATE, item);
            }
            log.info("User ("+userId+") saved item ("+item.getId()+"), title: " + item.getItemText());

            if (item.getLocked().booleanValue() == true && item.getScale() != null) {
                // lock associated scale
                log.info("Locking scale ("+item.getScale().getTitle()+") associated with new item ("+item.getId()+")");
                dao.lockScale( item.getScale(), Boolean.FALSE );
            }

            return;
        }

        // should not get here so die if we do
        throw new RuntimeException("User ("+userId+") could NOT save item ("+item.getId()+"), title: " + item.getItemText());
    }


    public void deleteItem(Long itemId, String userId) {
        log.debug("itemId:" + itemId + ", userId:" + userId);

        // get the item by id
        EvalItem item = getItemOrFail(itemId);

        // ADMIN USER CAN REMOVE EXPERT ITEMS -AZ
        //    // cannot remove expert items
        //    if (item.getExpert().booleanValue() == true) {
        //    throw new IllegalStateException("Cannot remove expert item ("+itemId+")");
        //    }

        if (securityChecks.checkUserControlItem(userId, item)) {

            EvalScale scale = item.getScale(); // LAZY LOAD
            String itemClassification = item.getClassification();
            dao.delete(item);
            commonLogic.registerEntityEvent(EVENT_ITEM_DELETE, item);
            log.info("User ("+userId+") removed item ("+item.getId()+"), title: " + item.getItemText());

            // unlock associated scales if there were any
            if (item.getLocked().booleanValue() && scale != null) {
                log.info("Unlocking associated scale ("+scale.getTitle()+") for removed item ("+itemId+")");
                dao.lockScale( scale, Boolean.FALSE );
            }

            // now we remove the scale if this is MC or MA
            if ( EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemClassification) ||
                    EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemClassification) ) {
                dao.delete(scale); // NOTE: does not use the main scale removal method since these are not really scales
            }

            return;
        }

        // should not get here so die if we do
        throw new RuntimeException("User ("+userId+") could NOT delete item ("+item.getId()+")");
    }


    public List<EvalItem> getItemsForUser(String userId, String sharingConstant, String filter, boolean includeExpert) {
        log.debug("sharingConstant:" + sharingConstant + ", userId:" + userId + ", filter:" + filter  + ", includeExpert:" + includeExpert);

        if (userId == null) {
            throw new IllegalArgumentException("Must include a userId");
        }

        // admin always gets all of the templates of a type
        if (commonLogic.isUserAdmin(userId)) {
            userId = null;
        }

        String[] sharingConstants = makeSharingConstantsArray(sharingConstant);

        // leave out the block parent items
        String[] props = new String[] { "classification" };
        Object[] values = new Object[] { EvalConstants.ITEM_TYPE_BLOCK_PARENT };
        int[] comparisons = new int[] { Restriction.NOT_EQUALS };

        if (!includeExpert) {
            props = ArrayUtils.appendArray(props, "expert");
            values = ArrayUtils.appendArray(values, Boolean.TRUE);
            comparisons = ArrayUtils.appendArray(comparisons, Restriction.NOT_EQUALS);
        }

        if (filter != null && filter.length() > 0) {
            props = ArrayUtils.appendArray(props, "itemText");
            values = ArrayUtils.appendArray(values, "%" + filter + "%");
            comparisons = ArrayUtils.appendArray(comparisons, Restriction.LIKE);
        }

        String[] order = new String[] {"id"};
        String[] options = new String[] {"notHidden"};

        return dao.getSharedEntitiesForUser(EvalItem.class, userId, sharingConstants, 
                props, values, comparisons, order, options, 0, 0);
    }


    public List<EvalItem> getItemsForTemplate(Long templateId, String userId) {
        log.debug("templateId:" + templateId + ", userId:" + userId);

        // TODO make this limit the items based on the user, currently it gets all items

        List<EvalItem> l = new ArrayList<EvalItem>();
        List<EvalTemplateItem> etis = getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {});
        for (EvalTemplateItem evalTemplateItem : etis) {
            l.add(evalTemplateItem.getItem());
        }
        return l;
    }



    public EvalTemplateItem getTemplateItemById(Long templateItemId) {
        log.debug("templateItemId:" + templateItemId);
        EvalTemplateItem templateItem = (EvalTemplateItem) dao.findById(EvalTemplateItem.class, templateItemId);
        if (templateItem != null) {
            if (TemplateItemUtils.isBlockParent(templateItem)) {
                // get the children
                List<EvalTemplateItem> children = getBlockChildTemplateItemsForBlockParent(templateItem.getId(), false);
                templateItem.childTemplateItems = children;
            }
        }
        return templateItem;
    }

    public EvalTemplateItem getTemplateItemByEid(String eid) {
        log.debug("templateItemEid:" + eid);
        EvalTemplateItem evalTemplateItem = null;
        if (eid != null) {
            List<EvalTemplateItem> evalTemplateItems = dao.findBySearch(EvalTemplateItem.class, new Search("eid", eid));
            if (evalTemplateItems != null && evalTemplateItems.size() == 1) {
                evalTemplateItem = evalTemplateItems.get(0);
            }
        }
        return evalTemplateItem;
    }


    @SuppressWarnings("unchecked")
    public void saveTemplateItem(EvalTemplateItem templateItem, String userId) {
        log.debug("templateItem:" + templateItem.getId() + ", userId:" + userId);

        // set the date modified
        templateItem.setLastModified( new Date() );

        // get item and check it
        EvalItem item = templateItem.getItem();
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        } else if (item.getId() == null) {
            throw new IllegalArgumentException("Item ("+item.getItemText()+") must already be saved");
        }

        // validate the fields of this template item based on the classification of the contained item
        TemplateItemUtils.validateTemplateItemByClassification(templateItem);

        // get template and check it
        EvalTemplate template = templateItem.getTemplate();
        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        } else if (template.getId() == null) {
            throw new IllegalArgumentException("Template ("+template.getTitle()+") must already be saved");
        }

        // check the template lock state and do not allow saves when template is locked
        if (template.getLocked().booleanValue()) {
            throw new IllegalStateException("This template ("+template.getId()+") is locked, templateItems and items cannot be changed");
        }

        // get the template items count to set display order for new templateItems
        if (templateItem.getId() == null) {
            if (TemplateItemUtils.isBlockParent(templateItem) 
                    && templateItem.getDisplayOrder() != null) {
                // if this a block parent then we allow the display order to be set
            } else {
                // new item
                int itemsCount = getItemCountForTemplate(template.getId());
                templateItem.setDisplayOrder( new Integer(itemsCount + 1) );
            }
        } else {
            // existing item
            // TODO - check if the display orders are set to a value that is used already?
        }

        fixUpTemplateItem(templateItem);

        if (securityChecks.checkUserControlTemplateItem(userId, templateItem)) {

            if (templateItem.getId() == null) {
                // if this is a new templateItem then associate it with 
                // the existing item and template and save all together
                Set[] entitySets = new HashSet[3];

                Set tiSet = new HashSet();
                tiSet.add(templateItem);
                entitySets[0] = tiSet;

                if (item.getTemplateItems() == null) {
                    item.setTemplateItems( new HashSet() );
                }
                item.getTemplateItems().add(templateItem);
                Set itemSet = new HashSet();
                itemSet.add(item);
                entitySets[1] = itemSet;

                if (template.getTemplateItems() == null) {
                    template.setTemplateItems( new HashSet() );
                }
                template.getTemplateItems().add(templateItem);
                Set templateSet = new HashSet();
                templateSet.add(template);
                entitySets[2] = templateSet;

                dao.saveMixedSet(entitySets);
                commonLogic.registerEntityEvent(EVENT_TEMPLATEITEM_CREATE, templateItem);
            } else {
                // existing item so just save it
                // TODO - make sure the item and template do not change for existing templateItems

                dao.save(templateItem);
                commonLogic.registerEntityEvent(EVENT_TEMPLATEITEM_UPDATE, templateItem);
            }

            // Should not be locking this here -AZ
            //       // lock related item and associated scales
            //       log.info("Locking item ("+item.getId()+") and associated scale");
            //       dao.lockItem(item, Boolean.TRUE);

            log.info("User ("+userId+") saved templateItem ("+templateItem.getId()+"), " +
                    "linked item (" + item.getId() +") and template ("+ template.getId()+")");
            return;
        }

        // should not get here so die if we do
        throw new RuntimeException("User ("+userId+") could NOT save templateItem ("+templateItem.getId()+")");
    }



    /**
     * Fixes up a templateItem before saving to ensure it is valid
     * 
     * @param templateItem
     */
    private void fixUpTemplateItem(EvalTemplateItem templateItem) {
        // set the default values for unspecified optional values
        EvalItem item = templateItem.getItem();
        if (templateItem.getCategory() == null) {
            if (item.getCategory() == null) {
                templateItem.setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
            } else {
                templateItem.setCategory(item.getCategory());
            }
        }
        if (templateItem.getResultsSharing() == null) {
            templateItem.setResultsSharing(EvalConstants.SHARING_PUBLIC);
        }
        Boolean naAllowed = (Boolean) settings.get(EvalSettings.ENABLE_NOT_AVAILABLE);
        if (naAllowed.booleanValue()) {
            // can set NA
            if (templateItem.getUsesNA() == null) {
                templateItem.setUsesNA(item.getUsesNA() == null ? Boolean.FALSE : item.getUsesNA());
            }
        } else {
            templateItem.setUsesNA( Boolean.FALSE );
        }
        Boolean usesComments = (Boolean) settings.get(EvalSettings.ENABLE_ITEM_COMMENTS);
        if (usesComments.booleanValue()) {
            // can use comments
            if (templateItem.getUsesComment() == null) {
                templateItem.setUsesComment(item.getUsesComment() == null ? Boolean.FALSE : item.getUsesComment());
            }
        } else {
            templateItem.setUsesComment( Boolean.FALSE );
        }
        // defaults for hierarchy level of template items
        if (templateItem.getHierarchyLevel() == null) {
            templateItem.setHierarchyLevel(EvalConstants.HIERARCHY_LEVEL_TOP);
        }
        if (templateItem.getHierarchyNodeId() == null) {
            templateItem.setHierarchyNodeId(EvalConstants.HIERARCHY_NODE_ID_NONE);
        }
    }


    /**
     * Used for determining next available displayOrder
     * 
     * @param templateId unique id for a template
     * @return a count of the non-child items in the template
     */
    protected int getItemCountForTemplate(Long templateId) {
        // only count items which are not children of a block
        int itemsCount = (int) dao.countBySearch(EvalTemplateItem.class, new Search(
                new Restriction[] {
                        new Restriction("template.id", templateId),
                        new Restriction("blockId", "", Restriction.NULL)
                }
        ));
        return itemsCount;
    }


    public void deleteTemplateItem(Long templateItemId, String userId) {
        log.debug("templateItemId:" + templateItemId + ", userId:" + userId);

        if (userId == null) {
            userId = commonLogic.getCurrentUserId();
        }

        // get the templateItem by id
        EvalTemplateItem templateItem = getTemplateItemOrFail(templateItemId);

        // check if this user is allowed to delete template items
        if (! securityChecks.checkUserControlTemplateItem(userId, templateItem)) {
            throw new SecurityException("User ("+userId+") cannot delete this template item ("+templateItemId+")");
        }

        // get a list of all template items in this template
        List<EvalTemplateItem> allTemplateItems = 
            getTemplateItemsForTemplate(templateItem.getTemplate().getId(), new String[] {}, new String[] {}, new String[] {});
        // get the list of items without child items included
        List<EvalTemplateItem> noChildList = TemplateItemUtils.getNonChildItems(allTemplateItems);

        // now remove the item and correct the display order
        int orderAdjust = 0;
        int removedItemDisplayOrder = 0;
        if (TemplateItemUtils.isBlockParent(templateItem)) {
            // remove the parent item and free up the child items into individual items if the block parent is removed
            removedItemDisplayOrder = templateItem.getDisplayOrder().intValue();
            List<EvalTemplateItem> childList = TemplateItemUtils.getChildItems(allTemplateItems, templateItem.getId());
            orderAdjust = childList.size();

            // delete parent template item and item
            Long itemId = templateItem.getItem().getId();
            simpleDeleteTemplateItem(templateItem);

            // if this parent is used elsewhere then this will cause exception - EVALSYS-559
            if (isUsedItem(itemId)) {
                log.info("Cannot remove block parent item ("+itemId+") - item is in use elsewhere");
            } else {
                try {
                    deleteItem(itemId, userId);
                } catch (SecurityException e) {
                    // this is ok, just means we are not allowed to remove the related item
                }
            }

            // modify block children template items
            for (int i = 0; i < childList.size(); i++) {
                EvalTemplateItem child = (EvalTemplateItem) childList.get(i);
                child.setBlockParent(null);
                child.setBlockId(null);
                child.setDisplayOrder(new Integer(removedItemDisplayOrder + i));
                saveTemplateItem(child, userId);
            }

        } else { // non-block cases
            removedItemDisplayOrder = templateItem.getDisplayOrder().intValue();
            simpleDeleteTemplateItem(templateItem);
        }

        // shift display-order of items below removed item
        for (int i = removedItemDisplayOrder; i < noChildList.size(); i++) {
            EvalTemplateItem ti = (EvalTemplateItem) noChildList.get(i);
            int order = ti.getDisplayOrder().intValue();
            if (order > removedItemDisplayOrder) {
                ti.setDisplayOrder(new Integer(order + orderAdjust - 1));
                saveTemplateItem(ti, userId);
            }
        }

        // fire event
        commonLogic.registerEntityEvent(EVENT_TEMPLATEITEM_DELETE, templateItem);
        // log
        log.info("Eval: User ("+userId+") deleted eval template item ("+templateItem.getId()+")");
    }

    /**
     * Just deletes the template item without messing around with checks which have already been done
     * @param templateItem a template item to remove
     */
    private void simpleDeleteTemplateItem(EvalTemplateItem templateItem) {
        if (templateItem != null) {
            Long itemId = templateItem.getItem().getId();
            EvalItem item = getItemById(itemId);
            // remove the templateItem and update all linkages
            dao.removeTemplateItems( new EvalTemplateItem[] {templateItem} );
            // attempt to unlock the related item
            dao.lockItem(item, Boolean.FALSE);
        }
    }


    public List<EvalTemplateItem> getTemplateItemsForTemplate(Long templateId, String[] nodeIds,
            String[] instructorIds, String[] groupIds) {
        log.debug("templateId:" + templateId);
        if (templateId == null) {
            throw new IllegalArgumentException("template id cannot be null");
        }
        return dao.getTemplateItemsByTemplate(templateId, nodeIds, instructorIds, groupIds);
    }

    public List<EvalTemplateItem> getTemplateItemsForEvaluation(Long evalId, String[] nodeIds,
            String[] instructorIds, String[] groupIds) {
        log.debug("evalId:" + evalId);
        if (evalId == null) {
            throw new IllegalArgumentException("evaluation id cannot be null");
        }
        return dao.getTemplateItemsByEvaluation(evalId, nodeIds, instructorIds, groupIds);
    }



    public List<EvalTemplateItem> getBlockChildTemplateItemsForBlockParent(Long parentId, boolean includeParent) {

        // get the templateItem by id to verify parent exists
        EvalTemplateItem templateItem = (EvalTemplateItem) dao.findById(EvalTemplateItem.class, parentId);
        if (templateItem == null) {
            throw new IllegalArgumentException("Cannot find block parent templateItem with id: " + parentId);
        }

        if (templateItem.getBlockParent() == null ||
                templateItem.getBlockParent().booleanValue() == false) {
            throw new IllegalArgumentException("Cannot request child block items for a templateItem which is not a block parent: " + templateItem.getId());
        }

        List<EvalTemplateItem> l = new ArrayList<EvalTemplateItem>();
        if (includeParent) {
            l.add(templateItem);
        }

        l.addAll( dao.findBySearch(EvalTemplateItem.class, 
                new Search( new Restriction("blockId", parentId), new Order("displayOrder") ) ) );

        return l;
    }


    public boolean canModifyItem(String userId, Long itemId) {
        log.debug("itemId:" + itemId + ", userId:" + userId);
        // get the item by id
        EvalItem item = getItemOrFail(itemId);

        // check perms and locked
        boolean allowed = false;
        try {
            allowed = securityChecks.checkUserControlItem(userId, item);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return allowed;
    }


    public boolean canRemoveItem(String userId, Long itemId) {
        log.debug("itemId:" + itemId + ", userId:" + userId);
        // get the item by id
        EvalItem item = getItemOrFail(itemId);

        // can remove items that are in use now
        //      // cannot remove items that are in use
        //      if (dao.isUsedItem(itemId)) {
        //         log.debug("Cannot remove item ("+itemId+") which is used in at least one template");
        //         return false;
        //      }

        // check perms and locked
        boolean allowed = false;
        try {
            allowed = securityChecks.checkUserControlItem(userId, item);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return allowed;
    }


    public boolean canControlTemplateItem(String userId, Long templateItemId) {
        log.debug("templateItemId:" + templateItemId + ", userId:" + userId);
        // get the template item by id
        EvalTemplateItem templateItem = getTemplateItemOrFail(templateItemId);

        // check perms and locked
        boolean allowed = false;
        try {
            allowed = securityChecks.checkUserControlTemplateItem(userId, templateItem);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return allowed;
    }


    /**
     * Get an item or throw exception if it does not exist
     * @param itemId
     * @return
     */
    private EvalItem getItemOrFail(Long itemId) {
        EvalItem item = getItemById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("Cannot find item with id: " + itemId);
        }
        return item;
    }

    /**
     * Get a template item or throw exception if it does not exist
     * @param templateItemId
     * @return
     */
    private EvalTemplateItem getTemplateItemOrFail(Long templateItemId) {
        EvalTemplateItem templateItem = getTemplateItemById(templateItemId);
        if (templateItem == null) {
            throw new IllegalArgumentException("Cannot find templateItem with id: " + templateItemId);
        }
        return templateItem;
    }



    // ITEM GROUPS


    public EvalItemGroup getItemGroupById(Long itemGroupId) {
        log.debug("itemGroupId:" + itemGroupId );
        EvalItemGroup ig = (EvalItemGroup) dao.findById(EvalItemGroup.class, itemGroupId);
        return ig;
    }


    public List<EvalItemGroup> getItemGroups(Long parentItemGroupId, String userId, boolean includeEmpty, boolean includeExpert) {
        log.debug("parentItemGroupId:" + parentItemGroupId + ", userId:" + userId + ", includeEmpty:" + includeEmpty + ", includeExpert:" + includeExpert);

        // check this parent is real
        if (parentItemGroupId != null) {
            getItemGroupOrFail(parentItemGroupId);
        }

        return dao.getItemGroups(parentItemGroupId, userId, includeEmpty, includeExpert);
    }


    public List<EvalItem> getItemsInItemGroup(Long itemGroupId, boolean expertOnly) {
        log.debug("parentItemGroupId:" + itemGroupId + ", expertOnly:" + expertOnly);

        // get the item group by id
        EvalItemGroup itemGroup = getItemGroupOrFail(itemGroupId);

        List<EvalItem> items = new ArrayList<EvalItem>();
        if ( itemGroup.getGroupItems() != null ) {
            items = new ArrayList<EvalItem>( itemGroup.getGroupItems() ); // LAZY LOAD
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


    public void saveItemGroup(EvalItemGroup itemGroup, String userId) {
        log.debug("itemGroup:" + itemGroup.getId() + ", userId:" + userId);

        // set the date modified
        itemGroup.setLastModified( new Date() );

        // fill in the default settings for optional unspecified values
        if ( itemGroup.getExpert() == null ) {
            itemGroup.setExpert( Boolean.FALSE );
        }

        // check only admin can create expert item groups
        if ( itemGroup.getExpert().booleanValue() && ! commonLogic.isUserAdmin(userId) ) {
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
        if (securityChecks.checkUserControlItemGroup(userId, itemGroup)) {
            dao.save(itemGroup);

            log.info("User ("+userId+") saved itemGroup ("+itemGroup.getId()+"), " + " of type ("+ itemGroup.getType()+")");
            return;     
        }

        // should not get here so die if we do
        throw new RuntimeException("User ("+userId+") could NOT save itemGroup ("+itemGroup.getId()+")");
    }


    public void removeItemGroup(Long itemGroupId, String userId, boolean removeNonEmptyGroup) {
        log.debug("itemGroupId:" + itemGroupId + ", userId:" + userId + ", removeNonEmptyGroup:" + removeNonEmptyGroup);

        // get the item by id
        EvalItemGroup itemGroup = getItemGroupOrFail(itemGroupId);

        // check user can create or update item group
        if (securityChecks.checkUserControlItemGroup(userId, itemGroup)) {

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


    // PERMISSIONS

    public boolean canUpdateItemGroup(String userId, Long itemGroupId) {
        log.debug("itemGroupId:" + itemGroupId + ", userId:" + userId);

        EvalItemGroup itemGroup = getItemGroupOrFail(itemGroupId);

        // check perms
        boolean allowed = false;
        try {
            allowed = securityChecks.checkUserControlItemGroup(userId, itemGroup);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return allowed;
    }


    public boolean canRemoveItemGroup(String userId, Long itemGroupId) {
        log.debug("itemGroupId:" + itemGroupId + ", userId:" + userId);

        EvalItemGroup itemGroup = getItemGroupOrFail(itemGroupId);

        // check perms
        boolean allowed = false;
        try {
            allowed = securityChecks.checkUserControlItemGroup(userId, itemGroup);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return allowed;
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


    /**
     * Get the itemGroup or throw exception if not found
     * @param itemGroupId
     * @return
     */
    private EvalItemGroup getItemGroupOrFail(Long itemGroupId) {
        EvalItemGroup itemGroup = getItemGroupById(itemGroupId);
        if (itemGroup == null) {
            throw new IllegalArgumentException("Cannot find parent itemGroup with id: " + itemGroupId);
        }
        return itemGroup;
    }




    // TEMPLATES

    public EvalTemplate getTemplateById(Long templateId) {
        log.debug("templateId: " + templateId);
        // get the template by id
        EvalTemplate template = (EvalTemplate) dao.findById(EvalTemplate.class, templateId);
        return template;
    }


    public EvalTemplate getTemplateByEid(String eid) {
        EvalTemplate evalTemplate = null;
        if (eid != null) {
            List<EvalTemplate> evalTemplates = dao.findBySearch(EvalTemplate.class, new Search("eid", eid));
            if (!evalTemplates.isEmpty()) {
                evalTemplate = evalTemplates.get(0);
            }
        }
        return evalTemplate;
    }


    public void saveTemplate(EvalTemplate template, String userId) {
        log.debug("template: " + template.getTitle() + ", userId: " + userId);

        boolean newTemplate = false;

        // set the date modified
        template.setLastModified( new Date() );

        // check for required fields first
        if (EvalUtils.isBlank(template.getTitle())) {
            throw new BlankRequiredFieldException("Cannot save a template with a blank title", "title");
        }

        // check the sharing constants
        EvalUtils.validateSharingConstant(template.getSharing());
        if ( EvalConstants.SHARING_OWNER.equals(template.getSharing()) ) {
            throw new IllegalArgumentException("Invalid sharing constant ("+template.getSharing()+") set for template ("+template.getTitle()+"), cannot use SHARING_OWNER");
        } else if ( EvalConstants.SHARING_PUBLIC.equals(template.getSharing()) ) {
            // test if non-admin trying to set public sharing
            String system_sharing = (String) settings.get(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY);
            if (! EvalConstants.SHARING_PUBLIC.equals(system_sharing) &&
                    ! commonLogic.isUserAdmin(userId) ) {
                throw new IllegalArgumentException("Only admins can set template ("+template.getTitle()+") sharing to public");
            }
        }

        if (template.getId() == null) {
            // new template
            newTemplate = true;
            if (! canCreateTemplate(userId)) {
                throw new SecurityException("User ("+userId+") cannot create templates, invalid permissions");
            }
        } else {
            // does not work with hibernate
            //         // existing template, don't allow change to locked setting
            //         EvalTemplate existingTemplate = getTemplateOrFail(template.getId());
            //
            //         if (! existingTemplate.getLocked().equals(template.getLocked())) {
            //            throw new IllegalArgumentException("Cannot change locked setting on existing template (" + template.getId() + ")");
            //         }
        }

        if (securityChecks.checkUserControlTemplate(userId, template)) {
            // fill in any default values and nulls here
            if (template.getLocked() == null) {
                template.setLocked( Boolean.FALSE );
            }

            // cleanup for XSS scripting and strings
            template.setTitle( commonLogic.cleanupUserStrings(template.getTitle()) );
            template.setDescription( commonLogic.cleanupUserStrings(template.getDescription()) );
            template.setExpertDescription( commonLogic.cleanupUserStrings(template.getExpertDescription()) );

            dao.save(template);
            log.info("User ("+userId+") saved template ("+template.getId()+"), title: " + template.getTitle());

            if (newTemplate) {
                commonLogic.registerEntityEvent(EVENT_TEMPLATE_CREATE, template);
            } else {
                commonLogic.registerEntityEvent(EVENT_TEMPLATE_UPDATE, template);
            }

            // validate and save all related template items
            validateTemplateItemsForTemplate(template.getId());

            if (template.getLocked().booleanValue() == true) {
                // lock template and associated items
                log.info("Locking template ("+template.getId()+") and associated items");
                dao.lockTemplate(template, Boolean.TRUE);
            }

            return;
        }

        // should not get here so die if we do
        throw new RuntimeException("User ("+userId+") could NOT save template ("+template.getId()+"), title: " + template.getTitle());
    }



    /**
     * Validates and saves all the template items related to this template
     * 
     * @param templateId the id of an {@link EvalTemplate}
     */
    private void validateTemplateItemsForTemplate(Long templateId) {
        List<EvalTemplateItem> templateItems = getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {});
        if (templateItems.size() > 0) {
            for (EvalTemplateItem templateItem : templateItems) {
                TemplateItemUtils.validateTemplateItemByClassification(templateItem);
            }
            List<EvalTemplateItem> orderedItems = TemplateItemUtils.orderTemplateItems(templateItems, true);
            Set<EvalTemplateItem> s = new HashSet<EvalTemplateItem>(orderedItems);
            dao.saveSet(s);
        }
    }


    @SuppressWarnings("unchecked")
    public void deleteTemplate(Long templateId, String userId) {
        log.debug("templateId: " + templateId + ", userId: " + userId);
        // get the template by id
        EvalTemplate template = getTemplateOrFail(templateId);

        // cannot remove expert templates
        if (template.getExpert().booleanValue() == true) {
            throw new IllegalStateException("Cannot remove expert template ("+templateId+")");
        }

        if (securityChecks.checkUserControlTemplate(userId, template)) {
            if (template.getLocked().booleanValue() == true) {
                // unlock template and associated items
                log.info("Unlocking template ("+template.getId()+") and associated items");
                dao.lockTemplate(template, Boolean.FALSE);
            }

            List<EvalTemplateItem> templateItems = getTemplateItemsForTemplate(template.getId(), new String[] {}, new String[] {}, new String[] {});
            if ( templateItems.size() > 0 ) {
                if (template.getCopyOf() != null 
                        || template.isHidden() == true) {
                    // this is a copy so remove all children (templateItems, items, and scales)
                    Set<EvalTemplateItem> TIset = new HashSet<EvalTemplateItem>(templateItems);
                    Set<EvalItem> itemSet = new HashSet<EvalItem>();
                    Set<EvalScale> scaleSet = new HashSet<EvalScale>();

                    // loop through the TIs and fill the other sets with the persistent objects (if they are copies)
                    for (EvalTemplateItem templateItem : templateItems) {
                        EvalItem item = templateItem.getItem();
                        if (item.getCopyOf() != null 
                                || item.isHidden() == true) {
                            itemSet.add(item);
                            EvalScale scale = item.getScale();
                            if (scale != null) {
                                if (scale.getCopyOf() != null 
                                        || scale.isHidden() == true) {
                                    scaleSet.add(scale);
                                }
                            }
                        }
                    }

                    // remove all the children in one large transaction
                    Set[] entitySets = new HashSet[3];
                    entitySets[0] = TIset;
                    entitySets[1] = itemSet;
                    entitySets[2] = scaleSet;
                    dao.deleteMixedSet(entitySets);
                } else {
                    // this is an original template so do not remove the children
                    EvalTemplateItem[] TIArray = templateItems.toArray(new EvalTemplateItem[templateItems.size()]);
                    // remove all associated templateItems (disassociate all items automatically)
                    dao.removeTemplateItems(TIArray);
                }
            }

            dao.delete(template);
            // fire the template deleted event
            commonLogic.registerEntityEvent(EVENT_TEMPLATE_DELETE, template);
            return;
        }

        // should not get here so die if we do
        throw new RuntimeException("User ("+userId+") could NOT delete template ("+templateId+")");
    }


    public List<EvalTemplate> getTemplatesForUser(String userId, String sharingConstant, boolean includeEmpty) {
        log.debug("sharingConstant: " + sharingConstant + ", userId: " + userId);

        /*
         * TODO - Hierarchy
         * visible and shared sharing methods are meant to work by relating the hierarchy level of 
         * the owner with the sharing setting in the template, however, that was when 
         * we assumed there would only be one level per user. That is no longer anything 
         * we have control over (since we depend on data that comes from another API) 
         * so we will have to add in a table which will track the hierarchy levels and
         * link them to the template. This will be a very simple but necessary table.
         */

        if (userId == null) {
            throw new IllegalArgumentException("Must include a userId");
        }

        // admin always gets all of the templates of a type
        if (commonLogic.isUserAdmin(userId)) {
            userId = null;
        }

        String[] sharingConstants = makeSharingConstantsArray(sharingConstant);

        // only get type standard templates
        String[] props = new String[] { "type" };
        Object[] values = new Object[] { EvalConstants.TEMPLATE_TYPE_STANDARD };
        int[] comparisons = new int[] { Restriction.EQUALS };

        String[] order = new String[] {"sharing","title"};
        String[] options = null;
        if (includeEmpty) {
            options = new String[] {"notHidden"};
        } else {
            options = new String[] {"notHidden", "notEmpty"};
        }

        return dao.getSharedEntitiesForUser(EvalTemplate.class, userId, sharingConstants, 
                props, values, comparisons, order, options, 0, 0);
    }



    /**
     * Takes a single sharing constant and turns it into an array of sharing constants
     * based on the rule that null/owner means private and public
     * 
     * @param sharingConstant
     * @return array of sharing constants
     */
    private String[] makeSharingConstantsArray(String sharingConstant) {
        if (sharingConstant != null) {
            EvalUtils.validateSharingConstant(sharingConstant);
        }
        String[] sharingConstants = new String[] {};
        if (EvalConstants.SHARING_PRIVATE.equals(sharingConstant)) {
            // do private templates only
            sharingConstants = new String[] {EvalConstants.SHARING_PRIVATE};
        } else if (EvalConstants.SHARING_PUBLIC.equals(sharingConstant)) {
            // do public templates only
            sharingConstants = new String[] {EvalConstants.SHARING_PUBLIC};
        } else if (sharingConstant == null || 
                EvalConstants.SHARING_OWNER.equals(sharingConstant)) {
            // do all templates visible to this user
            sharingConstants = new String[] {EvalConstants.SHARING_PRIVATE, EvalConstants.SHARING_PUBLIC};
        }
        return sharingConstants;
    }


    // PERMISSIONS


    public boolean canCreateTemplate(String userId) {
        log.debug("userId: " + userId);
        boolean allowed = false;
        if ( commonLogic.isUserAdmin(userId) ) {
            // the system super user can create templates always
            allowed = true;
        } else {
            /*
             * If the person is not an admin (super or any kind, currently we just have super admin) 
             * then system settings should be checked whether they can create templates 
             * or not - kahuja.
             * 
             * TODO - make this check system wide and not site/group specific - aaronz.
             */
            if ( ((Boolean)settings.get(EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS)).booleanValue() && 
                    commonLogic.countEvalGroupsForUser(userId, EvalConstants.PERM_WRITE_TEMPLATE) > 0 ) {
                allowed = true;
            }
        }
        return allowed;
    }


    public boolean canModifyTemplate(String userId, Long templateId) {
        log.debug("templateId: " + templateId + ", userId: " + userId);
        // get the template by id
        EvalTemplate template = getTemplateOrFail(templateId);

        // check perms and locked
        boolean allowed = false;
        try {
            allowed = securityChecks.checkUserControlTemplate(userId, template);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return allowed;
    }


    public boolean canRemoveTemplate(String userId, Long templateId) {
        log.debug("templateId: " + templateId + ", userId: " + userId);
        // get the template by id
        EvalTemplate template = getTemplateOrFail(templateId);

        // can remove templates that are in use now since it should not happen
        //      // cannot remove templates that are in use
        //      if (dao.isUsedTemplate(templateId)) {
        //         log.debug("Cannot remove template ("+templateId+") which is used in at least one evaluation");
        //         return false;
        //      }

        // check perms and locked
        boolean allowed = false;
        try {
            allowed = securityChecks.checkUserControlTemplate(userId, template);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
        return allowed;
    }



    /**
     * Get a template or throw exception
     * @param templateId
     * @return
     */
    private EvalTemplate getTemplateOrFail(Long templateId) {
        EvalTemplate template = getTemplateById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Cannot find template with id: " + templateId);
        }
        return template;
    }



    // COPYING

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalAuthoringService#copyScales(java.lang.Long[], java.lang.String, java.lang.String, boolean)
     */
    public Long[] copyScales(Long[] scaleIds, String title, String ownerId, boolean hidden) {
        if (scaleIds == null || scaleIds.length == 0) {
            throw new IllegalArgumentException("Invalid scaleIds array, cannot be null or empty");         
        }

        Set<EvalScale> copiedScales = copyScalesInternal(scaleIds, title, ownerId, hidden);

        Long[] copiedIds = new Long[copiedScales.size()];
        int counter = 0;
        for (EvalScale copiedScale : copiedScales) {
            copiedIds[counter] = copiedScale.getId();
            counter++;
        }
        return copiedIds;
    }

    /**
     * Internal method: allows us to get to the set of all copies
     * @return the set of copies of the scales
     */
    private Set<EvalScale> copyScalesInternal(Long[] scaleIds, String title, String ownerId,
            boolean hidden) {
        if (ownerId == null || ownerId.length() == 0) {
            throw new IllegalArgumentException("Invalid ownerId, cannot be null or empty string");         
        }

        Set<EvalScale> copiedScales = new HashSet<EvalScale>();
        if (scaleIds != null && scaleIds.length > 0) {
            scaleIds = ArrayUtils.unique(scaleIds);
            List<EvalScale> scales = dao.findBySearch(EvalScale.class, new Search("id", scaleIds));
            if (scales.size() != scaleIds.length) {
                throw new IllegalArgumentException("Invalid scaleIds in the scaleIds array: " + scaleIds);
            }

            for (EvalScale original : scales) {
                String newTitle = title;
                if (newTitle == null || newTitle.length() == 0) {
                    newTitle = original.getTitle() + " (copy)";
                }
                EvalScale copy = new EvalScale(ownerId, newTitle, original.getMode(), EvalConstants.SHARING_PRIVATE, 
                        false, null, original.getIdeal(), ArrayUtils.copy(original.getOptions()), 
                        false);
                copy.setCopyOf(original.getId());
                copy.setHidden(hidden);
                copiedScales.add(copy);
            }
            dao.saveSet(copiedScales);
        }
        return copiedScales;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalAuthoringService#copyItems(java.lang.Long[], java.lang.String, boolean, boolean)
     */
    public Long[] copyItems(Long[] itemIds, String ownerId, boolean hidden, boolean includeChildren) {
        if (itemIds == null || itemIds.length == 0) {
            throw new IllegalArgumentException("Invalid itemIds array, cannot be null or empty");         
        }

        Set<EvalItem> copiedItems = copyItemsInternal(itemIds, ownerId, hidden, includeChildren);

        Long[] copiedIds = new Long[copiedItems.size()];
        int counter = 0;
        for (EvalItem copiedItem : copiedItems) {
            copiedIds[counter] = copiedItem.getId();
            counter++;
        }
        return copiedIds;
    }

    /**
     * Internal method: allows us to get to the set of all copies
     * @return the set of copies of the items
     */
    private Set<EvalItem> copyItemsInternal(Long[] itemIds, String ownerId, boolean hidden,
            boolean includeChildren) {
        if (ownerId == null || ownerId.length() == 0) {
            throw new IllegalArgumentException("Invalid ownerId, cannot be null or empty string");         
        }

        Set<EvalItem> copiedItems = new HashSet<EvalItem>();
        if (itemIds != null && itemIds.length > 0) {
            itemIds = ArrayUtils.unique(itemIds);
            List<EvalItem> items = dao.findBySearch(EvalItem.class, new Search("id", itemIds));
            if (items.size() != itemIds.length) {
                throw new IllegalArgumentException("Invalid itemIds in array: " + itemIds);
            }

            for (EvalItem original : items) {
                EvalItem copy = new EvalItem(ownerId, original.getItemText(), original.getDescription(), EvalConstants.SHARING_PRIVATE,
                        original.getClassification(), false, null, original.getScale(), null, original.getUsesNA(), original.getUsesComment(),
                        false, original.getDisplayRows(), original.getScaleDisplaySetting(), original.getCategory(), false);
                // NOTE: no longer copying scales here - EVALSYS-689
                copy.setCopyOf(original.getId());
                copy.setHidden(hidden);
                copy.setCompulsory(original.isCompulsory());
                copiedItems.add(copy);
            }

            if (includeChildren) {
                // make a copy of all Scales and put them into the Items to replace the originals
                HashSet<Long> scaleIdSet = new HashSet<Long>();
                for (EvalItem item : copiedItems) {
                    if (item.getScale() != null) {
                        Long scaleId = item.getScale().getId();
                        scaleIdSet.add(scaleId);
                    }
                }
                Long[] scaleIds = scaleIdSet.toArray(new Long[scaleIdSet.size()]);
                // do the scales copy
                // https://bugs.caret.cam.ac.uk/browse/CTL-1531 - hide all the internal things which are copied (do not pass through the hidden variable)
                Set<EvalScale> copiedScales = copyScalesInternal(scaleIds, null, ownerId, true);
                HashMap<Long, EvalScale> originalIdToCopy = new HashMap<Long, EvalScale>(copiedItems.size());
                for (EvalScale scale : copiedScales) {
                    originalIdToCopy.put(scale.getCopyOf(), scale);
                }
                // insert the copied items into the copied template items (update the foreign keys when we save)
                for (EvalItem item : copiedItems) {
                    if (item.getScale() != null) {
                        Long scaleId = item.getScale().getId(); // original id
                        EvalScale copy = originalIdToCopy.get(scaleId);
                        if (copy != null) {
                            item.setScale(copy);
                        }
                    }
                }
            }
            dao.saveSet(copiedItems);
        }
        return copiedItems;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalAuthoringService#copyTemplateItems(java.lang.Long[], java.lang.String, boolean, java.lang.Long, boolean)
     */
    public Long[] copyTemplateItems(Long[] templateItemIds, String ownerId, boolean hidden, Long toTemplateId, boolean includeChildren) {
        if (ownerId == null || ownerId.length() == 0) {
            throw new IllegalArgumentException("Invalid ownerId, cannot be null or empty string");  
        }
        if (templateItemIds == null || templateItemIds.length == 0) {
            throw new IllegalArgumentException("Invalid templateItemIds array, cannot be null or empty");
        }

        templateItemIds = ArrayUtils.unique(templateItemIds);
        EvalTemplate toTemplate = null;
        if (toTemplateId != null) {
            toTemplate = getTemplateById(toTemplateId);
            if (toTemplate == null) {
                throw new IllegalArgumentException("Invalid toTemplateId, cannot find the template by this id: " + toTemplateId);
            }
        }

        List<EvalTemplateItem> templateItemsList = dao.findBySearch(EvalTemplateItem.class, new Search("id", templateItemIds));
        if (templateItemsList.size() != templateItemIds.length) {
            throw new IllegalArgumentException("Invalid templateItemIds in array: " + templateItemIds);
        }

        // now we check that copying into the originating template is correct and ensure the toTemplate is set
        if (toTemplate == null) {
            // all templateItems must be from the same template if this is the case
            for (EvalTemplateItem templateItem : templateItemsList) {
                Long templateId = templateItem.getTemplate().getId();
                if (toTemplate == null) {
                    toTemplate = getTemplateById(templateId);
                } else {
                    if (! toTemplate.getId().equals(templateId)) {
                        throw new IllegalArgumentException("All templateItems must be from the same template when doing a copy within a template, "
                                + "if you want to copy templateItems from multiple templates into the same templates they are currently in you must "
                                + "do it in batches where each set if from one template");
                    }
                }
            }
        }

        // sort the list of template items
        templateItemsList = TemplateItemUtils.orderTemplateItems(templateItemsList, false);

        int itemCount = 1; // start at display order 1
        if (toTemplateId == null) {
            // copying inside one template so start at the item count + 1
            // get the count of items in the destination template so we know where to start displayOrder from
            itemCount = getItemCountForTemplate(toTemplate.getId()) + 1;
        }

        /* http://bugs.sakaiproject.org/jira/browse/EVALSYS-689
         * need to track the copied items and scales to avoid copying them more than once
         */
        LinkedHashSet<EvalTemplateItem> copiedTemplateItems = new LinkedHashSet<EvalTemplateItem>(templateItemsList.size());

        // shallow copy all block parents first so we can know their new IDs, then later we will update them
        List<EvalTemplateItem> parentItems = TemplateItemUtils.getParentItems(templateItemsList);
        HashMap<Long, EvalTemplateItem> parentIdToCopy = new HashMap<Long, EvalTemplateItem>(parentItems.size());
        if (! parentItems.isEmpty()) {
            for (EvalTemplateItem original : parentItems) {
                Long originalBlockParentId = original.getId();
                List<EvalTemplateItem> childItems = TemplateItemUtils.getChildItems(templateItemsList, originalBlockParentId);
                if (childItems.size() > 0) {
                    // only copy this if it has children, lone parents do not get copied
                    EvalTemplateItem copy = copyTemplateItem(original, toTemplate, ownerId, hidden);
                    parentIdToCopy.put(originalBlockParentId, copy);
                }
            }
            HashSet<EvalTemplateItem> parentItemsToSave = new HashSet<EvalTemplateItem>( parentIdToCopy.values() );
            dao.saveSet( parentItemsToSave );
        }

        // check for block items
        List<EvalTemplateItem> nonChildItems = TemplateItemUtils.getNonChildItems(templateItemsList);

        // iterate though in display order and copy the template items
        int displayOrder = 0;
        for (EvalTemplateItem original : nonChildItems) {
            templateItemsList.remove(original); // take this out of the list
            if (TemplateItemUtils.isBlockParent(original)) {
                // this is a block parent so copy it and its children
                Long originalBlockParentId = original.getId();
                if (parentIdToCopy.containsKey(originalBlockParentId)) {
                    EvalTemplateItem copyParent = parentIdToCopy.get(originalBlockParentId);
                    copyParent.setDisplayOrder(itemCount + displayOrder); // fix up display order
                    copyParent.setBlockId(null);
                    copyParent.setBlockParent(true);
                    //dao.save(copyParent);
                    copiedTemplateItems.add(copyParent);
                    Long blockParentId = copyParent.getId();

                    // loop through and copy all the children and assign them to the parent
                    List<EvalTemplateItem> childItems = TemplateItemUtils.getChildItems(templateItemsList, originalBlockParentId);
                    for (int j = 0; j < childItems.size(); j++) {
                        EvalTemplateItem child = childItems.get(j);
                        templateItemsList.remove(child); // take this out of the list
                        // copy the child item
                        EvalTemplateItem copy = copyTemplateItem(child, toTemplate, ownerId, hidden);
                        copy.setDisplayOrder(j); // fix up display order
                        copy.setBlockId(blockParentId);
                        copy.setBlockParent(false);
                        //dao.save(copy);
                        copiedTemplateItems.add(copy);
                    }
                }
            } else {
                // not a block parent
                EvalTemplateItem copy = copyTemplateItem(original, toTemplate, ownerId, hidden);
                copy.setDisplayOrder(itemCount + displayOrder); // fix up display order
                //dao.save(copy);
                copiedTemplateItems.add(copy);
            }
            displayOrder++;
        }

        // now copy any remaining orphaned block children into normal items
        for (EvalTemplateItem original : templateItemsList) {
            displayOrder++;
            EvalTemplateItem copy = copyTemplateItem(original, toTemplate, ownerId, hidden);
            copy.setDisplayOrder(itemCount + displayOrder); // fix up display order
            //dao.save(copy);
            copiedTemplateItems.add(copy);
        }

        if (includeChildren) {
            // make a copy of all items and put them into the TIs to replace the originals
            HashSet<Long> itemIdSet = new HashSet<Long>();
            for (EvalTemplateItem eti : copiedTemplateItems) {
                if (eti.getItem() != null) {
                    Long itemId = eti.getItem().getId();
                    itemIdSet.add(itemId);
                }
            }
            Long[] itemIds = itemIdSet.toArray(new Long[itemIdSet.size()]);
            // do the items copy
            Set<EvalItem> copiedItems = copyItemsInternal(itemIds, ownerId, hidden, includeChildren);
            HashMap<Long, EvalItem> originalIdToCopy = new HashMap<Long, EvalItem>(copiedItems.size());
            for (EvalItem evalItem : copiedItems) {
                originalIdToCopy.put(evalItem.getCopyOf(), evalItem);
            }
            // insert the copied items into the copied template items (update the foreign keys when we save)
            for (EvalTemplateItem eti : copiedTemplateItems) {
                if (eti.getItem() != null) {
                    Long itemId = eti.getItem().getId(); // original id
                    EvalItem copy = originalIdToCopy.get(itemId);
                    if (copy != null) {
                        eti.setItem(copy);
                    }
                }
            }
        }
        // save the template items
        dao.saveSet(copiedTemplateItems);

        Long[] copiedIds = new Long[copiedTemplateItems.size()];
        int counter = 0;
        for (EvalTemplateItem copiedTemplateItem : copiedTemplateItems) {
            copiedIds[counter] = copiedTemplateItem.getId();
            counter++;
        }
        return copiedIds;
    }

    /**
     * Makes a non-persistent copy of a templateItem, nulls out the block fields,
     * inherits the display order from the original
     * 
     * @param original the original item to copy
     * @param toTemplate the template to copy this templateItem to
     * @param ownerId set as the owner of this copy
     * @param hidden if true then the resulting copy will be marked as hidden 
     * @return the copy of the templateItem (not persisted)
     */
    private EvalTemplateItem copyTemplateItem(EvalTemplateItem original, EvalTemplate toTemplate,
            String ownerId, boolean hidden) {
        EvalTemplateItem copy = TemplateItemUtils.makeCopyOfTemplateItem(original, toTemplate, ownerId, hidden);
        fixUpTemplateItem(copy); // fix up to ensure fields are set correctly
        return copy;
    }


    public Long copyTemplate(Long templateId, String title, String ownerId, boolean hidden, boolean includeChildren) {
        if (ownerId == null || ownerId.length() == 0) {
            throw new IllegalArgumentException("Invalid ownerId, cannot be null or empty string");         
        }
        if (templateId == null || templateId == 0) {
            throw new IllegalArgumentException("Invalid templateId, cannot be null or 0");         
        }

        EvalTemplate original = getTemplateById(templateId);
        if (original == null) {
            throw new IllegalArgumentException("Invalid templateId submitted ("+templateId+"), could not retrieve the template");
        }

        String newTitle = title;
        if (newTitle == null || newTitle.length() == 0) {
            newTitle = original.getTitle() + " (copy)";
        }
        EvalTemplate copy = new EvalTemplate(ownerId, original.getType(), newTitle, 
                original.getDescription(), EvalConstants.SHARING_PRIVATE, false, 
                null, null, false, false);
        // set the other copy fields
        copy.setCopyOf(original.getId());
        copy.setHidden(hidden);

        dao.save(copy);

        if (original.getTemplateItems() != null 
                && original.getTemplateItems().size() > 0) {
            // now copy the template items and save the new linkages
            Long[] originalTIIds = TemplateItemUtils.makeTemplateItemsIdsArray(original.getTemplateItems());
            // https://bugs.caret.cam.ac.uk/browse/CTL-1531 - hide all the internal things which are copied (do not pass through the hidden variable)
            Long[] templateItemIds = copyTemplateItems(originalTIIds, ownerId, true, copy.getId(), includeChildren);
            List<EvalTemplateItem> templateItemsList = dao.findBySearch(EvalTemplateItem.class, 
                    new Search("id", templateItemIds));
            copy.setTemplateItems( new HashSet<EvalTemplateItem>(templateItemsList) );
            dao.save(copy);
        }

        return copy.getId();
    }


    public List<EvalItem> getItemsUsingScale(Long scaleId) {
        if (getScaleById(scaleId) == null) {
            throw new IllegalArgumentException("Invalid scaleId, no scale found with this id: " + scaleId);
        }
        List<EvalItem> items = dao.findBySearch(EvalItem.class, new Search("scale.id", scaleId));
        return items;
    }


    public List<EvalTemplate> getTemplatesUsingItem(Long itemId) {
        if (getItemById(itemId) == null) {
            throw new IllegalArgumentException("Invalid itemId, no item found with this id: " + itemId);
        }
        List<EvalTemplateItem> templateItems = dao.findBySearch(EvalTemplateItem.class, new Search("item.id", itemId));
        Set<Long> ids = new HashSet<Long>();
        for (EvalTemplateItem templateItem : templateItems) {
            ids.add(templateItem.getTemplate().getId());
        }
        List<EvalTemplate> templates = new ArrayList<EvalTemplate>();
        for (Long templateId : ids) {
            templates.add( getTemplateById(templateId) );
        }
        return templates;
    }



    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalAuthoringService#getAutoUseTemplateItems(java.lang.String, java.lang.String, java.lang.String)
     */
    public List<EvalTemplateItem> getAutoUseTemplateItems(String templateAutoUseTag, String templateItemAutoUseTag, String itemAutoUseTag) {

        List<EvalTemplateItem> items = new ArrayList<EvalTemplateItem>();
        // first add in the templates items
        if (! EvalUtils.isBlank(templateAutoUseTag)) {
            List<EvalTemplate> templates = dao.findBySearch(EvalTemplate.class, 
                    new Search( new Restriction("autoUseTag", templateAutoUseTag), new Order("id"))
            );
            for (EvalTemplate template : templates) {
                List<EvalTemplateItem> templateItemsList = getTemplateItemsForTemplate(template.getId(), new String[] {}, null, null); // only hierarchy nodes
                items.addAll( TemplateItemUtils.orderTemplateItems(templateItemsList, false) );
            }
        }

        // now the template items (only if not already there)
        if (! EvalUtils.isBlank(templateItemAutoUseTag)) {
            List<EvalTemplateItem> templateItems = dao.findBySearch(EvalTemplateItem.class, 
                    new Search( 
                            new Restriction[] { new Restriction("autoUseTag", templateItemAutoUseTag) }, 
                            new Order[] { new Order("displayOrder"), new Order("id") }
                    )
            );
            for (EvalTemplateItem templateItem : templateItems) {
                if (! items.contains(templateItem)) {
                    items.add(templateItem);
                }
            }
        }

        // finally put in the items wrapper in a templateItem
        if (! EvalUtils.isBlank(itemAutoUseTag)) {
            List<EvalItem> evalItems = dao.findBySearch(EvalItem.class, 
                    new Search( new Restriction("autoUseTag", itemAutoUseTag), new Order("id"))
            );
            for (EvalItem evalItem : evalItems) {
                items.add( TemplateItemUtils.makeTemplateItem(evalItem) );
            }
        }

        return items;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.EvalAuthoringService#doAutoUseInsertion(java.lang.String, java.lang.Long, java.lang.String, boolean)
     */
    public List<EvalTemplateItem> doAutoUseInsertion(String autoUseTag, Long templateId, String insertionPointConstant, boolean saveAll) {
        List<EvalTemplateItem> allTemplateItems = null;
        // get all the autoUse items
        List<EvalTemplateItem> autoUseItems = getAutoUseTemplateItems(autoUseTag, autoUseTag, autoUseTag);
        if (autoUseItems.size() > 0) {
            log.info("Found "+autoUseItems.size()+" autoUse items to insert for tag (" + autoUseTag + ") into template (id="+templateId+")");
            allTemplateItems = new ArrayList<EvalTemplateItem>();
            EvalTemplate template = getTemplateOrFail(templateId);
            String ownerId = template.getOwner();

            // get all current template items sorted
            List<EvalTemplateItem> currentItems = null;
            List<EvalTemplateItem> currentTemplateItems = TemplateItemUtils.orderTemplateItems(
                    getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {}), false );
            if (saveAll) {
                currentItems = currentTemplateItems;
            } else {
                // make copies of all the current items as well since we are not saving them
                currentItems = new ArrayList<EvalTemplateItem>();
                for (EvalTemplateItem original : currentTemplateItems) {
                    EvalTemplateItem copy = TemplateItemUtils.makeCopyOfTemplateItem(original, template, template.getOwner(), true);
                    currentItems.add(copy);
                }
            }

            // copy and update all insertion items to have the correct value in the insertion field
            List<EvalTemplateItem> insertionItems = new ArrayList<EvalTemplateItem>();
            Long[] copiedTIIds = new Long[0];
            if (saveAll) {
                // filter out the non-persistent TIs and make lists of all ids per template
                List<EvalTemplateItem> unsavedTIs = new ArrayList<EvalTemplateItem>();
                Map<Long, List<Long>> templateToTIsMap = new HashMap<Long, List<Long>>();
                for (EvalTemplateItem templateItem : autoUseItems) {
                    Long templateItemId = templateItem.getId();
                    if (templateItem.getId() != null) {
                        Long currentTemplateId = templateItem.getTemplate().getId();
                        if (templateToTIsMap.containsKey(currentTemplateId)) {
                            templateToTIsMap.get(currentTemplateId).add(templateItemId);
                        } else {
                            List<Long> templateTIIds = new ArrayList<Long>();
                            templateTIIds.add(templateItemId);
                            templateToTIsMap.put(currentTemplateId, templateTIIds);
                        }
                    } else {
                        unsavedTIs.add(templateItem);
                    }
                }
                // use the copy method to make persistent copies of all template items and children
                for (Long currentTemplateId : templateToTIsMap.keySet()) {
                    List<Long> TIIds = templateToTIsMap.get(currentTemplateId);
                    Long[] copiedIds = copyTemplateItems(TIIds.toArray(new Long[] {}), ownerId, true, template.getId(), true);
                    copiedTIIds = ArrayUtils.appendArrays(copiedTIIds, copiedIds);
                }
                // fetch the new copies based on the ids
                List<EvalTemplateItem> templateItemsList = dao.findBySearch(EvalTemplateItem.class, 
                        new Search("id", copiedTIIds) );
                // now put the copied items into the list in the order of the copied ids
                for (int i = 0; i < copiedTIIds.length; i++) {
                    Long id = copiedTIIds[i];
                    for (EvalTemplateItem templateItem : templateItemsList) {
                        if (id.equals(templateItem.getId())) {
                            insertionItems.add(templateItem);
                            templateItemsList.remove(templateItem);
                            break;
                        }
                    }
                }
                // save all unsaved items
                for (EvalTemplateItem templateItem : unsavedTIs) {
                    templateItem.setTemplate(template);
                    templateItem.setOwner(ownerId);
                    saveTemplateItem(templateItem, ownerId);
                    insertionItems.add(templateItem);
                }
            } else {
                // just make a simple non-persistent copy of all the items
                for (EvalTemplateItem original : autoUseItems) {
                    EvalTemplateItem copy = TemplateItemUtils.makeCopyOfTemplateItem(original, template, template.getOwner(), true);
                    // preserve the block data
                    copy.setBlockId(original.getBlockId());
                    copy.setBlockParent(original.getBlockParent());
                    // now add in the autouse data
                    copy.setAutoUseInsertionTag(autoUseTag);
                    insertionItems.add(copy);
                }
            }

            // set the autoUse insertion value
            for (EvalTemplateItem insertedTemplateItem : insertionItems) {
                insertedTemplateItem.setAutoUseInsertionTag(autoUseTag);
            }

            if (EvalConstants.EVALUATION_AUTOUSE_INSERTION_BEFORE.equals(insertionPointConstant)) {
                // inserting autoUse items before the existing ones
                allTemplateItems.addAll(insertionItems);
                allTemplateItems.addAll(currentItems);
            } else if (EvalConstants.EVALUATION_AUTOUSE_INSERTION_AFTER.equals(insertionPointConstant)) {
                // inserting autoUse items after the existing ones
                allTemplateItems.addAll(currentItems);
                allTemplateItems.addAll(insertionItems);
            } else {
                throw new IllegalArgumentException("Do not know how to handle autoUse insertion point of: " + insertionPointConstant);
            }

            // now we update the displayOrders to the current list order (which should be correct)
            int displayOrder = 1;
            for (EvalTemplateItem templateItem : allTemplateItems) {
                if (! TemplateItemUtils.isBlockChild(templateItem)) {
                    // only update the order of non-block children
                    templateItem.setDisplayOrder(displayOrder++);
                }
            }

            // save if set and then return the list of all copied items with corrected display order
            if (saveAll) {
                // save all the templateItems
                Set<EvalTemplateItem> allItems = new HashSet<EvalTemplateItem>(allTemplateItems);
                dao.saveSet( allItems );
                // add the full list to the template and save it
                template.setTemplateItems( allItems );
                dao.save(template);
                log.info("Saved and inserted "+autoUseItems.size()+" autoUse items for tag (" + autoUseTag + ") into template (id="+templateId+")");
            }
        } else {
            log.info("No autoUse items can be found to insert for tag: " + autoUseTag);
        }
        return allTemplateItems;
    }


    /**
     * @param scaleId the unique id for an {@link EvalScale}
     * @return true if this scale is used in any items
     */
    public boolean isUsedItem(Long itemId) {
        return dao.isUsedItem(itemId);
    }

    /**
     * @param itemId the unique id for an {@link EvalItem}
     * @return true if this item is used in any template (i.e. connected to any template item)
     */
    public boolean isUsedScale(Long scaleId) {
        return dao.isUsedScale(scaleId);
    }

    /**
     * @param templateId the unique id for an {@link EvalTemplate}
     * @return true if this template is used in any evalautions
     */
    public boolean isUsedTemplate(Long templateId) {
        return dao.isUsedTemplate(templateId);
    }

}
