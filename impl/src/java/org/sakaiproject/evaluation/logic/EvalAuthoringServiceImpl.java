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

   private EvalExternalLogic external;
   public void setExternalLogic(EvalExternalLogic external) {
      this.external = external;
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

      // check for non-null values which can be inferred
      if (scale.getMode() == null) {
         // set this to the default then
         scale.setMode(EvalConstants.SCALE_MODE_SCALE);
      }

      if (scale.getSharing() == null) {
         scale.setSharing(EvalConstants.SHARING_PRIVATE);
      }

      // check the sharing constants
      EvalUtils.validateSharingConstant(scale.getSharing());
      if ( EvalConstants.SHARING_OWNER.equals(scale.getSharing()) ) {
         throw new IllegalArgumentException("Invalid sharing constant ("+scale.getSharing()+") set for scale ("+scale.getTitle()+"), cannot use SHARING_OWNER");
      } else if ( EvalConstants.SHARING_PUBLIC.equals(scale.getSharing()) ) {
         // test if non-admin trying to set public sharing
         if (! external.isUserAdmin(userId) ) {
            throw new IllegalArgumentException("Only admins can set scale ("+scale.getTitle()+") sharing to public");
         }
      }

      // check locking not changed
      boolean newScale = true;
      if (scale.getId() != null) {
         newScale = false;
         // existing scale, don't allow change to locked setting

         // TODO - this does not work, it just gets the persistent scale from memory -AZ
         EvalScale existingScale = getScaleOrFail(scale.getId());

         if (! existingScale.getLocked().equals(scale.getLocked())) {
            throw new IllegalArgumentException("Cannot change locked setting on existing scale (" + scale.getId() + ")");
         }
      }

      // fill in any default values and nulls here
      if (scale.getLocked() == null) {
         scale.setLocked( Boolean.FALSE );
      }

      // replace adhoc default title with a unique title
      if (EvalConstants.SCALE_ADHOC_DEFAULT_TITLE.equals(scale.getTitle())) {
         scale.setTitle("adhoc-" + EvalUtils.makeUniqueIdentifier(100));
      }

      // check perms and save
      if (securityChecks.checkUserControlScale(userId, scale)) {
         dao.save(scale);
         if (newScale) {
            external.registerEntityEvent(EVENT_SCALE_CREATE, scale);
         } else {
            external.registerEntityEvent(EVENT_SCALE_UPDATE, scale);
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
         external.registerEntityEvent(EVENT_SCALE_DELETE, scale);
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

      List<EvalScale> l = new ArrayList<EvalScale>();

      // get admin state
      boolean isAdmin = external.isUserAdmin(userId);

      boolean getPublic = false;
      boolean getPrivate = false;

      // check the sharingConstant param
      if (sharingConstant != null) {
         EvalUtils.validateSharingConstant(sharingConstant);
      }

      if ( sharingConstant == null || 
            EvalConstants.SHARING_OWNER.equals(sharingConstant) ) {
         // return all scales visible to this user
         getPublic = true;
         getPrivate = true;
      } else if ( EvalConstants.SHARING_PRIVATE.equals(sharingConstant) ) {
         // return only private scales visible to this user
         getPrivate = true;
      } else if ( EvalConstants.SHARING_PUBLIC.equals(sharingConstant) ) {
         // return all public scales
         getPublic = true;
      }

      // handle private sharing items
      if (getPrivate) {
         String[] props;
         Object[] values;
         int[] comps;
         if (isAdmin) {
            props = new String[] { "mode", "sharing" };
            values = new Object[] { EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PRIVATE };
            comps = new int[] { ByPropsFinder.EQUALS , ByPropsFinder.EQUALS };
         } else {
            props = new String[] { "mode", "sharing", "owner" };
            values = new Object[] { EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PRIVATE, userId };            
            comps = new int[] { ByPropsFinder.EQUALS, ByPropsFinder.EQUALS, ByPropsFinder.EQUALS };
         }
         l.addAll( dao.findByProperties(EvalScale.class, 
               props, 
               values,
               comps,
               new String[] {"title"}) );
      }

      // handle public sharing items
      if (getPublic) {
         l.addAll( dao.findByProperties(EvalScale.class, 
               new String[] { "mode", "sharing" }, 
               new Object[] { EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PUBLIC },
               new int[] { ByPropsFinder.EQUALS, ByPropsFinder.EQUALS },
               new String[] {"title"}) );
      }

      return l;
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

      // cannot remove scales that are in use
      if (dao.isUsedScale(scaleId)) {
         log.debug("Cannot remove scale ("+scaleId+") which is used in at least one item");
         return false;
      }

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

      // validates the item based on the classification
      TemplateItemUtils.validateItemByClassification(item);

      // check the sharing constants
      EvalUtils.validateSharingConstant(item.getSharing());
      if ( EvalConstants.SHARING_OWNER.equals(item.getSharing()) ) {
         throw new IllegalArgumentException("Invalid sharing constant ("+item.getSharing()+") set for item ("+item.getItemText()+"), cannot use SHARING_OWNER");
      } else if ( EvalConstants.SHARING_PUBLIC.equals(item.getSharing()) ) {
         // test if non-admin trying to set public sharing
         if (! external.isUserAdmin(userId) ) {
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

      // fill in the default settings for optional unspecified values
      if (item.getLocked() == null) {
         item.setLocked( Boolean.FALSE );
      }
      // check the NOT_AVAILABLE_ALLOWED system setting
      Boolean naAllowed = (Boolean) settings.get(EvalSettings.NOT_AVAILABLE_ALLOWED);
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

      if (securityChecks.checkUserControlItem(userId, item)) {
         dao.save(item);
         if (newItem) {
            external.registerEntityEvent(EVENT_ITEM_CREATE, item);
         } else {
            external.registerEntityEvent(EVENT_ITEM_UPDATE, item);
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
         external.registerEntityEvent(EVENT_ITEM_DELETE, item);
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


   @SuppressWarnings("unchecked")
   public List<EvalItem> getItemsForUser(String userId, String sharingConstant, String filter, boolean includeExpert) {
      log.debug("sharingConstant:" + sharingConstant + ", userId:" + userId + ", filter:" + filter  + ", includeExpert:" + includeExpert);

      List<EvalItem> l = new ArrayList<EvalItem>();

      // get admin state
      boolean isAdmin = external.isUserAdmin(userId);

      boolean getPublic = false;
      boolean getPrivate = false;

      // check the sharingConstant param
      if (sharingConstant != null) {
         EvalUtils.validateSharingConstant(sharingConstant);
      }

      if ( sharingConstant == null || 
            EvalConstants.SHARING_OWNER.equals(sharingConstant) ) {
         // return all items visible to this user
         getPublic = true;
         getPrivate = true;
      } else if ( EvalConstants.SHARING_PRIVATE.equals(sharingConstant) ) {
         // return only private items visible to this user
         getPrivate = true;
      } else if ( EvalConstants.SHARING_PUBLIC.equals(sharingConstant) ) {
         // return all public items
         getPublic = true;
      }

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

      // handle private sharing items
      if (getPrivate) {
         String[] privateProps = ArrayUtils.appendArray(props, "sharing");
         Object[] privateValues = ArrayUtils.appendArray(values, EvalConstants.SHARING_PRIVATE);
         int[] privateComparisons = ArrayUtils.appendArray(comparisons, ByPropsFinder.EQUALS);

         if (!isAdmin) {
            privateProps = ArrayUtils.appendArray(privateProps, "owner");
            privateValues = ArrayUtils.appendArray(privateValues, userId);
            privateComparisons = ArrayUtils.appendArray(privateComparisons, ByPropsFinder.EQUALS);
         }

         l.addAll( dao.findByProperties(EvalItem.class, privateProps, privateValues, privateComparisons, new String[] {"id"}) );
      }

      // handle public sharing items
      if (getPublic) {
         String[] publicProps = ArrayUtils.appendArray(props, "sharing");
         Object[] publicValues = ArrayUtils.appendArray(values, EvalConstants.SHARING_PUBLIC);
         int[] publicComparisons = ArrayUtils.appendArray(comparisons, ByPropsFinder.EQUALS);

         l.addAll( dao.findByProperties(EvalItem.class, publicProps, publicValues, publicComparisons, new String[] {"id"}) );
      }

      return ArrayUtils.removeDuplicates(l); // remove duplicates from the list
   }


   public List<EvalItem> getItemsForTemplate(Long templateId, String userId) {
      log.debug("templateId:" + templateId + ", userId:" + userId);

      // TODO make this limit the items based on the user

      List<EvalItem> l = new ArrayList<EvalItem>();
      List<EvalTemplateItem> etis = getTemplateItemsForTemplate(templateId, null, null, null);
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
         if (templateItem.getBlockParent() != null &&
               templateItem.getBlockParent().booleanValue() &&
               templateItem.getDisplayOrder() != null) {
            // if this a block parent then we allow the display order to be set
         } else {
            // new item
            int itemsCount = 0;
            if (template.getTemplateItems() != null) {
               // TODO - write a DAO method to do this faster
               for (Iterator iter = template.getTemplateItems().iterator(); iter.hasNext();) {
                  EvalTemplateItem eti = (EvalTemplateItem) iter.next();
                  if (eti.getBlockId() == null) {
                     // only count items which are not children of a block
                     itemsCount++;
                  }
               }
               //itemsCount = template.getTemplateItems().size();
            }
            templateItem.setDisplayOrder( new Integer(itemsCount + 1) );
         }
      } else {
         // existing item
         // TODO - check if the display orders are set to a value that is used already?
      }

      // set the default values for unspecified optional values
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
      Boolean naAllowed = (Boolean) settings.get(EvalSettings.NOT_AVAILABLE_ALLOWED);
      if (naAllowed.booleanValue()) {
         // can set NA
         if (templateItem.getUsesNA() == null) {
            templateItem.setUsesNA( Boolean.FALSE );
         }
      } else {
         templateItem.setUsesNA( Boolean.FALSE );
      }
      // defaults for hierarchy level of template items
      if (templateItem.getHierarchyLevel() == null) {
         templateItem.setHierarchyLevel(EvalConstants.HIERARCHY_LEVEL_TOP);
      }
      if (templateItem.getHierarchyNodeId() == null) {
         templateItem.setHierarchyNodeId(EvalConstants.HIERARCHY_NODE_ID_NONE);
      }

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
            external.registerEntityEvent(EVENT_TEMPLATEITEM_CREATE, templateItem);
         } else {
            // existing item so just save it
            // TODO - make sure the item and template do not change for existing templateItems

            dao.save(templateItem);
            external.registerEntityEvent(EVENT_TEMPLATEITEM_UPDATE, templateItem);
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
         external.registerEntityEvent(EVENT_TEMPLATEITEM_DELETE, templateItem);
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

      // cannot remove items that are in use
      if (dao.isUsedItem(itemId)) {
         log.debug("Cannot remove item ("+itemId+") which is used in at least one template");
         return false;
      }

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

      // check the sharing constants
      EvalUtils.validateSharingConstant(template.getSharing());
      if ( EvalConstants.SHARING_OWNER.equals(template.getSharing()) ) {
         throw new IllegalArgumentException("Invalid sharing constant ("+template.getSharing()+") set for template ("+template.getTitle()+"), cannot use SHARING_OWNER");
      } else if ( EvalConstants.SHARING_PUBLIC.equals(template.getSharing()) ) {
         // test if non-admin trying to set public sharing
         String system_sharing = (String) settings.get(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY);
         if (! EvalConstants.SHARING_PUBLIC.equals(system_sharing) &&
               ! external.isUserAdmin(userId) ) {
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
         // existing template, don't allow change to locked setting
         EvalTemplate existingTemplate = getTemplateOrFail(template.getId());

         if (! existingTemplate.getLocked().equals(template.getLocked())) {
            throw new IllegalArgumentException("Cannot change locked setting on existing template (" + template.getId() + ")");
         }
      }

      // fill in any default values and nulls here
      if (template.getLocked() == null) {
         template.setLocked( Boolean.FALSE );
      }

      if (securityChecks.checkUserControlTemplate(userId, template)) {
         dao.save(template);
         log.info("User ("+userId+") saved template ("+template.getId()+"), title: " + template.getTitle());

         if (newTemplate) {
            external.registerEntityEvent(EVENT_TEMPLATE_CREATE, template);
         } else {
            external.registerEntityEvent(EVENT_TEMPLATE_UPDATE, template);
         }

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

         if ( template.getTemplateItems().size() > 0 ) {
            // remove all associated templateItems (disassociate all items automatically)
            EvalTemplateItem[] templateItems = (EvalTemplateItem[]) 
            template.getTemplateItems().toArray( new EvalTemplateItem[] {} ); // LAZY LOAD
            dao.removeTemplateItems(templateItems);
         }

         dao.delete(template);
         // fire the template deleted event
         external.registerEntityEvent(EVENT_TEMPLATE_DELETE, template);
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

      // check sharing constant
      if (sharingConstant != null) {
         EvalUtils.validateSharingConstant(sharingConstant);
      }

      // admin always gets all of the templates of a type
      if (external.isUserAdmin(userId)) {
         userId = null;
      }

      String[] sharingConstants = new String[] {};
      if (EvalConstants.SHARING_PRIVATE.equals(sharingConstant)) {
         // do private templates only
      } else if (EvalConstants.SHARING_PUBLIC.equals(sharingConstant)) {
         // do public templates only
         sharingConstants = new String[] {EvalConstants.SHARING_PUBLIC};
         userId = "";
      } else if (sharingConstant == null || 
            EvalConstants.SHARING_OWNER.equals(sharingConstant)) {
         // do all templates visible to this user
         sharingConstants = new String[] {EvalConstants.SHARING_PUBLIC};
      }

      return dao.getVisibleTemplates(userId, sharingConstants, includeEmpty);
   }


   // PERMISSIONS


   public boolean canCreateTemplate(String userId) {
      log.debug("userId: " + userId);
      boolean allowed = false;
      if ( external.isUserAdmin(userId) ) {
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
               external.countEvalGroupsForUser(userId, EvalConstants.PERM_WRITE_TEMPLATE) > 0 ) {
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

      // cannot remove templates that are in use
      if (dao.isUsedTemplate(templateId)) {
         log.debug("Cannot remove template ("+templateId+") which is used in at least one evaluation");
         return false;
      }

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

}
