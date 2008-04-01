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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.exceptions.BlankRequiredFieldException;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
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
import org.sakaiproject.genericdao.api.finders.ByPropsFinder;


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

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic external) {
      this.externalLogic = external;
   }

   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }

   private EvalSecurityChecksImpl securityChecks;
   public void setSecurityChecks(EvalSecurityChecksImpl securityChecks) {
      this.securityChecks = securityChecks;
   }



   @SuppressWarnings("unchecked")
   public void init() {
      // this method will help us to patch up the system if needed

      // fix up the scales with null modes
      List<EvalScale> scales = dao.findByProperties(EvalScale.class,
            new String[] { "mode" }, 
            new Object[] { "" },
            new int[] { ByPropsFinder.NULL});
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

   @SuppressWarnings("unchecked")
   public EvalScale getScaleByEid(String eid) {
      log.debug("scale eid: " + eid);
      EvalScale evalScale = null;
      if (eid != null) {
         List<EvalScale> evalScales = dao.findByProperties(EvalScale.class,
               new String[] { "eid" }, new Object[] { eid });
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
         if (! externalLogic.isUserAdmin(userId) ) {
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
            scale.setTitle( externalLogic.cleanupUserStrings(scale.getTitle()) );
         }

         // now save the scale
         dao.save(scale);
         if (newScale) {
            externalLogic.registerEntityEvent(EVENT_SCALE_CREATE, scale);
         } else {
            externalLogic.registerEntityEvent(EVENT_SCALE_UPDATE, scale);
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
         externalLogic.registerEntityEvent(EVENT_SCALE_DELETE, scale);
         log.info("User ("+userId+") deleted scale ("+scale.getId()+"), title: " + scale.getTitle());
         return;
      }

      // should not get here so die if we do
      throw new RuntimeException("User ("+userId+") could NOT delete scale ("+scale.getId()+")");
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvalScalesLogic#getScalesForUser(java.lang.String, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   public List<EvalScale> getScalesForUser(String userId, String sharingConstant) {
      log.debug("userId: " + userId + ", sharingConstant: " + sharingConstant );

      if (userId == null) {
         throw new IllegalArgumentException("Must include a userId");
      }

      // admin always gets all of the templates of a type
      if (externalLogic.isUserAdmin(userId)) {
         userId = null;
      }

      String[] sharingConstants = makeSharingConstantsArray(sharingConstant);

      // only get type standard templates
      String[] props = new String[] { "mode" };
      Object[] values = new Object[] { EvalConstants.SCALE_MODE_SCALE };
      int[] comparisons = new int[] { ByPropsFinder.EQUALS };

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
         throw new IllegalArgumentException("Cannot find scale with id: " + scale.getId());
      }
      return scale;
   }



   // ITEMS


   public EvalItem getItemById(Long itemId) {
      log.debug("itemId:" + itemId);
      EvalItem item = (EvalItem) dao.findById(EvalItem.class, itemId);
      return item;
   }


   @SuppressWarnings("unchecked")
   public EvalItem getItemByEid(String eid) {
      EvalItem evalItem = null;
      if (eid != null) {
         log.debug("eid: " + eid);
         List<EvalItem> evalItems = dao.findByProperties(EvalItem.class,
               new String[] { "eid" }, new Object[] { eid });
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
         if (! externalLogic.isUserAdmin(userId) ) {
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

         // cleanup for XSS scripting and strings
         item.setItemText( externalLogic.cleanupUserStrings(item.getItemText()) );
         item.setDescription( externalLogic.cleanupUserStrings(item.getDescription()) );
         item.setExpertDescription( externalLogic.cleanupUserStrings(item.getExpertDescription()) );

         // save the item
         dao.save(item);
         if (newItem) {
            externalLogic.registerEntityEvent(EVENT_ITEM_CREATE, item);
         } else {
            externalLogic.registerEntityEvent(EVENT_ITEM_UPDATE, item);
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
         externalLogic.registerEntityEvent(EVENT_ITEM_DELETE, item);
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
      if (externalLogic.isUserAdmin(userId)) {
         userId = null;
      }

      String[] sharingConstants = makeSharingConstantsArray(sharingConstant);

      // leave out the block parent items
      String[] props = new String[] { "classification" };
      Object[] values = new Object[] { EvalConstants.ITEM_TYPE_BLOCK_PARENT };
      int[] comparisons = new int[] { ByPropsFinder.NOT_EQUALS };

      if (!includeExpert) {
         props = ArrayUtils.appendArray(props, "expert");
         values = ArrayUtils.appendArray(values, Boolean.TRUE);
         comparisons = ArrayUtils.appendArray(comparisons, ByPropsFinder.NOT_EQUALS);
      }

      if (filter != null && filter.length() > 0) {
         props = ArrayUtils.appendArray(props, "itemText");
         values = ArrayUtils.appendArray(values, "%" + filter + "%");
         comparisons = ArrayUtils.appendArray(comparisons, ByPropsFinder.LIKE);
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
      return templateItem;
   }


   @SuppressWarnings("unchecked")
   public EvalTemplateItem getTemplateItemByEid(String eid) {
      log.debug("templateItemEid:" + eid);
      EvalTemplateItem evalTemplateItem = null;
      if (eid != null) {
         List<EvalTemplateItem> evalTemplateItems = dao.findByProperties(
               EvalTemplateItem.class, new String[] { "eid" },
               new Object[] { eid });
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
            externalLogic.registerEntityEvent(EVENT_TEMPLATEITEM_CREATE, templateItem);
         } else {
            // existing item so just save it
            // TODO - make sure the item and template do not change for existing templateItems

            dao.save(templateItem);
            externalLogic.registerEntityEvent(EVENT_TEMPLATEITEM_UPDATE, templateItem);
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
            templateItem.setUsesNA( Boolean.FALSE );
         }
      } else {
         templateItem.setUsesNA( Boolean.FALSE );
      }
      Boolean usesComments = (Boolean) settings.get(EvalSettings.ENABLE_ITEM_COMMENTS);
      if (usesComments.booleanValue()) {
         // can use comments
         if (templateItem.getUsesComment() == null) {
            templateItem.setUsesComment( Boolean.FALSE );
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
      int itemsCount = dao.countByProperties(EvalTemplateItem.class, 
            new String[] {"template.id", "blockId"}, 
            new Object[] {templateId, ""},
            new int[] {EvaluationDao.EQUALS, EvaluationDao.NULL});
// OLD way
//            int itemsCount = 0;
//            if (template.getTemplateItems() != null) {
//               // TODO - write a DAO method to do this faster
//               for (Iterator iter = template.getTemplateItems().iterator(); iter.hasNext();) {
//                  EvalTemplateItem eti = (EvalTemplateItem) iter.next();
//                  if (eti.getBlockId() == null) {
//                     // only count items which are not children of a block
//                     itemsCount++;
//                  }
//               }
//            }
      return itemsCount;
   }


   public void deleteTemplateItem(Long templateItemId, String userId) {
      log.debug("templateItemId:" + templateItemId + ", userId:" + userId);

      // get the templateItem by id
      EvalTemplateItem templateItem = getTemplateItemOrFail(templateItemId);

      // check if this templateItem can be removed (checks if associated template is locked)
      if (securityChecks.checkUserControlTemplateItem(userId, templateItem)) {
         EvalItem item = getItemById(templateItem.getItem().getId());
         // remove the templateItem and update all linkages
         dao.removeTemplateItems( new EvalTemplateItem[] {templateItem} );
         // attempt to unlock the related item
         dao.lockItem(item, Boolean.FALSE);
         // fire event
         externalLogic.registerEntityEvent(EVENT_TEMPLATEITEM_DELETE, templateItem);
         return;
      }

      // should not get here so die if we do
      throw new RuntimeException("User ("+userId+") could NOT delete template-item linkage ("+templateItem.getId()+")");
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



   @SuppressWarnings("unchecked")
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

      l.addAll( dao.findByProperties(EvalTemplateItem.class, 
            new String[] { "blockId" }, 
            new Object[] { parentId },
            new int[] { ByPropsFinder.EQUALS },
            new String[] { "displayOrder" }) );

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
      if ( itemGroup.getExpert().booleanValue() && ! externalLogic.isUserAdmin(userId) ) {
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


   @SuppressWarnings("unchecked")
   public EvalTemplate getTemplateByEid(String eid) {
      EvalTemplate evalTemplate = null;
      if (eid != null) {
         List<EvalTemplate> evalTemplates = dao.findByProperties(EvalTemplate.class,
               new String[] { "eid" }, new Object[] { eid });
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
               ! externalLogic.isUserAdmin(userId) ) {
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
         template.setTitle( externalLogic.cleanupUserStrings(template.getTitle()) );
         template.setDescription( externalLogic.cleanupUserStrings(template.getDescription()) );
         template.setExpertDescription( externalLogic.cleanupUserStrings(template.getExpertDescription()) );

         dao.save(template);
         log.info("User ("+userId+") saved template ("+template.getId()+"), title: " + template.getTitle());

         if (newTemplate) {
            externalLogic.registerEntityEvent(EVENT_TEMPLATE_CREATE, template);
         } else {
            externalLogic.registerEntityEvent(EVENT_TEMPLATE_UPDATE, template);
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
         externalLogic.registerEntityEvent(EVENT_TEMPLATE_DELETE, template);
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
      if (externalLogic.isUserAdmin(userId)) {
         userId = null;
      }

      String[] sharingConstants = makeSharingConstantsArray(sharingConstant);

      // only get type standard templates
      String[] props = new String[] { "type" };
      Object[] values = new Object[] { EvalConstants.TEMPLATE_TYPE_STANDARD };
      int[] comparisons = new int[] { ByPropsFinder.EQUALS };

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
      if ( externalLogic.isUserAdmin(userId) ) {
         // the system super user can create templates always
         allowed = true;
      } else {
         /*
          * If the person is not an admin (super or any kind, currently we just have super admin) 
          * then system settings should be checked whether they can create templates 
          * or not - kahuja.
          * 
          * TODO - make this check system wide and not evalGroupId specific - aaronz.
          */
         if ( ((Boolean)settings.get(EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS)).booleanValue() && 
               externalLogic.countEvalGroupsForUser(userId, EvalConstants.PERM_WRITE_TEMPLATE) > 0 ) {
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

   @SuppressWarnings("unchecked")
   public Long[] copyScales(Long[] scaleIds, String title, String ownerId, boolean hidden) {
      if (ownerId == null || ownerId.length() == 0) {
         throw new IllegalArgumentException("Invalid ownerId, cannot be null or empty string");         
      }
      if (scaleIds == null || scaleIds.length == 0) {
         throw new IllegalArgumentException("Invalid scaleIds array, cannot be null or empty");         
      }

      List<EvalScale> scales = dao.findByProperties(EvalScale.class, new String[] {"id"}, new Object[] { scaleIds });
      if (scales.size() != scaleIds.length) {
         throw new IllegalArgumentException("Invalid scaleIds in the scaleIds array: " + scaleIds);
      }

      Set<EvalScale> copiedScales = new HashSet<EvalScale>();
      for (EvalScale original : scales) {
         String newTitle = title;
         if (newTitle == null || newTitle.length() == 0) {
            newTitle = original.getTitle() + " (copy)";
         }
         EvalScale copy = new EvalScale(new Date(), ownerId, newTitle, original.getMode(), 
               EvalConstants.SHARING_PRIVATE, false, null, original.getIdeal(), 
               ArrayUtils.copy(original.getOptions()), false);
         copy.setCopyOf(original.getId());
         copy.setHidden(hidden);
         copiedScales.add(copy);
      }
      dao.saveSet(copiedScales);

      Long[] copiedIds = new Long[copiedScales.size()];
      int counter = 0;
      for (EvalScale copiedScale : copiedScales) {
         copiedIds[counter] = copiedScale.getId();
         counter++;
      }
      return copiedIds;
   }

   @SuppressWarnings("unchecked")
   public Long[] copyItems(Long[] itemIds, String ownerId, boolean hidden, boolean includeChildren) {
      if (ownerId == null || ownerId.length() == 0) {
         throw new IllegalArgumentException("Invalid ownerId, cannot be null or empty string");         
      }
      if (itemIds == null || itemIds.length == 0) {
         throw new IllegalArgumentException("Invalid itemIds array, cannot be null or empty");         
      }

      List<EvalItem> items = dao.findByProperties(EvalItem.class, new String[] {"id"}, new Object[] { itemIds });
      if (items.size() != itemIds.length) {
         throw new IllegalArgumentException("Invalid itemIds in array: " + itemIds);
      }

      Set<EvalItem> copiedItems = new HashSet<EvalItem>();
      for (EvalItem original : items) {
         EvalItem copy = new EvalItem(new Date(), ownerId, original.getItemText(), original.getDescription(),
               EvalConstants.SHARING_PRIVATE, original.getClassification(), false, null, null, null, original.getUsesNA(),
               original.getUsesComment(), original.getDisplayRows(), original.getScaleDisplaySetting(), original.getCategory(), false);
         if (original.getScale() != null) {
            // This could be more efficient if we stored up the scaleIds and mapped them 
            // and then copied them all at once and then reassigned them but the code would be a lot harder to read
            EvalScale scale = null;
            if (includeChildren) {
               Long[] scaleIds = copyScales(new Long[] {original.getScale().getId()}, null, ownerId, hidden);
               scale = getScaleById(scaleIds[0]);
            } else {
               scale = original.getScale();
            }
            copy.setScale(scale);
         }
         copy.setCopyOf(original.getId());
         copy.setHidden(hidden);
         copiedItems.add(copy);
      }
      dao.saveSet(copiedItems);

      Long[] copiedIds = new Long[copiedItems.size()];
      int counter = 0;
      for (EvalItem copiedItem : copiedItems) {
         copiedIds[counter] = copiedItem.getId();
         counter++;
      }
      return copiedIds;
   }

   @SuppressWarnings("unchecked")
   public Long[] copyTemplateItems(Long[] templateItemIds, String ownerId, boolean hidden, Long toTemplateId, boolean includeChildren) {
      if (ownerId == null || ownerId.length() == 0) {
         throw new IllegalArgumentException("Invalid ownerId, cannot be null or empty string");  
      }
      if (templateItemIds == null || templateItemIds.length == 0) {
         throw new IllegalArgumentException("Invalid templateItemIds array, cannot be null or empty");
      }

      EvalTemplate toTemplate = null;
      if (toTemplateId != null) {
         toTemplate = getTemplateById(toTemplateId);
         if (toTemplate == null) {
            throw new IllegalArgumentException("Invalid toTemplateId, cannot find the template by this id: " + toTemplateId);
         }
      }

      List<EvalTemplateItem> templateItemsList = dao.findByProperties(EvalTemplateItem.class, new String[] {"id"}, new Object[] { templateItemIds });
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
                  throw new IllegalArgumentException("All templateItems must be from the same template when doing a copies within a template,"
                           + " if you want to copy templateItems from multiple templates into the same templates they are currently in you must "
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

      // check for block items
      List<EvalTemplateItem> nonChildItems = TemplateItemUtils.getNonChildItems(templateItemsList);

      // iterate though in display order and copy the template items
      int counter = 0;
      Long[] copiedIds = new Long[templateItemsList.size()];
      for (int i = 0; i < nonChildItems.size(); i++) {
         EvalTemplateItem original = nonChildItems.get(i);
         if (TemplateItemUtils.isBlockParent(original)) {
            // this is a block parent so copy it and its children
            Long originalBlockParentId = original.getId();
            templateItemsList.remove(original); // take this out of the list
            // copy and save the parent
            EvalTemplateItem copyParent = copyTemplateItem(original, toTemplate, ownerId, hidden, includeChildren);
            copyParent.setDisplayOrder(itemCount + i); // fix up display order
            copyParent.setBlockId(null);
            copyParent.setBlockParent(true);
            dao.save(copyParent);
            copiedIds[counter++] = copyParent.getId();
            Long blockParentId = copyParent.getId();

            // loop through and copy all the children and assign them to the parent
            List<EvalTemplateItem> childItems = TemplateItemUtils.getChildItems(templateItemsList, originalBlockParentId);
            for (int j = 0; j < childItems.size(); j++) {
               EvalTemplateItem child = childItems.get(j);
               templateItemsList.remove(child); // take this out of the list
               // copy the child item
               EvalTemplateItem copy = copyTemplateItem(child, toTemplate, ownerId, hidden, includeChildren);
               copy.setDisplayOrder(j); // fix up display order
               copy.setBlockId(blockParentId);
               copy.setBlockParent(false);
               dao.save(copy);
               copiedIds[counter++] = copy.getId();
            }
         } else {
            // not in a block
            EvalTemplateItem copy = copyTemplateItem(original, toTemplate, ownerId, hidden, includeChildren);
            copy.setDisplayOrder(itemCount + i); // fix up display order
            dao.save(copy);
            copiedIds[counter++] = copy.getId();
         }
      }
      return copiedIds;
   }

   /**
    * Makes a non-persistent copy of a templateItem, nulls out the block fields,
    * inherits the display order from the original
    * 
    * @param original the original item to copy
    * @param toTemplate
    * @param ownerId
    * @param hidden
    * @param includeChildren
    * @return the copy of the templateItem (not persisted)
    */
   private EvalTemplateItem copyTemplateItem(EvalTemplateItem original, EvalTemplate toTemplate,
         String ownerId, boolean hidden, boolean includeChildren) {
      EvalTemplateItem copy = new EvalTemplateItem(new Date(), ownerId, toTemplate, null, original.getDisplayOrder(),
            original.getCategory(), original.getHierarchyLevel(), original.getHierarchyNodeId(), original.getDisplayRows(),
            original.getScaleDisplaySetting(), original.getUsesNA(), original.getUsesComment(), null, null, original.getResultsSharing());
      // copy the item as well if needed
      EvalItem item = null;
      if (includeChildren) {
         Long[] itemIds = copyItems(new Long[] {original.getItem().getId()}, ownerId, hidden, includeChildren);
         item = getItemById(itemIds[0]);
      } else {
         item = original.getItem();
      }
      copy.setItem(item);
      // set the other copy fields correctly
      copy.setCopyOf(original.getId());
      copy.setHidden(hidden);
      fixUpTemplateItem(copy); // fix up to ensure fields are set correctly
      return copy;
   }


   @SuppressWarnings("unchecked")
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
      EvalTemplate copy = new EvalTemplate(new Date(), ownerId, original.getType(), 
            newTitle, original.getDescription(), EvalConstants.SHARING_PRIVATE, 
            false, null, null, false, false);
      // set the other copy fields
      copy.setCopyOf(original.getId());
      copy.setHidden(hidden);

      dao.save(copy);

      if (original.getTemplateItems() != null 
            && original.getTemplateItems().size() > 0) {
         // now copy the template items and save the new linkages
         Long[] originalTIIds = TemplateItemUtils.makeTemplateItemsIdsArray(original.getTemplateItems());
         Long[] templateItemIds = copyTemplateItems(originalTIIds, ownerId, hidden, copy.getId(), includeChildren);
         List<EvalTemplateItem> templateItemsList = dao.findByProperties(EvalTemplateItem.class, new String[] {"id"}, new Object[] { templateItemIds });
         copy.setTemplateItems( new HashSet<EvalTemplateItem>(templateItemsList) );
         dao.save(copy);
      }

      return copy.getId();
   }


   @SuppressWarnings("unchecked")
   public List<EvalItem> getItemsUsingScale(Long scaleId) {
      if (getScaleById(scaleId) == null) {
         throw new IllegalArgumentException("Invalid scaleId, no scale found with this id: " + scaleId);
      }
      List<EvalItem> items = dao.findByProperties(EvalItem.class, new String[] {"scale.id"}, new Object[] { scaleId });
      return items;
   }


   @SuppressWarnings("unchecked")
   public List<EvalTemplate> getTemplatesUsingItem(Long itemId) {
      if (getItemById(itemId) == null) {
         throw new IllegalArgumentException("Invalid itemId, no item found with this id: " + itemId);
      }
      List<EvalTemplateItem> templateItems = dao.findByProperties(EvalTemplateItem.class, new String[] {"item.id"}, new Object[] { itemId });
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

}
