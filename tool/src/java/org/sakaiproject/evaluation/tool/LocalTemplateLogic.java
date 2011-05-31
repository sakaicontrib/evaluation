/**
 * LocalTemplateLogic.java - evaluation - 23 Jan 2007 11:35:56 AM - antranig
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

/**
 * Local template local abstraction to allow for default values and central point of access for all things
 * related to creating items and templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class LocalTemplateLogic {

   private EvalCommonLogic commonLogic;
   public void setCommonLogic(EvalCommonLogic commonLogic) {
      this.commonLogic = commonLogic;
   }

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }

   /*
    * Real methods below
    */

   // TEMPLATES

   public EvalTemplate fetchTemplate(Long templateId) {
      return authoringService.getTemplateById(templateId);
   }

   public void saveTemplate(EvalTemplate tosave) {
      authoringService.saveTemplate(tosave, commonLogic.getCurrentUserId());
   }

   public EvalTemplate newTemplate() {
      EvalTemplate currTemplate = new EvalTemplate(commonLogic.getCurrentUserId(), 
            EvalConstants.TEMPLATE_TYPE_STANDARD, null, 
            "private", Boolean.FALSE);
      currTemplate.setDescription(""); // Note- somehow gives DataIntegrityViolation if null
      return currTemplate;
   }


   // TEMPLATE ITEMS

   public EvalTemplateItem fetchTemplateItem(Long itemId) {
      return authoringService.getTemplateItemById(itemId);
   }

   /**
    * Get the list of all non-instructor and non-group template items for this template
    * This probably needs to handle instructor at some point (i.e. it should display the instructor added items possibly)
    * 
    * @param templateId
    * @return the ETI objects
    */
   public List<EvalTemplateItem> fetchTemplateItems(Long templateId) {
      if (templateId == null) {
         return new ArrayList<EvalTemplateItem>();
      } else {
         return authoringService.getTemplateItemsForTemplate(templateId, new String[] {}, null, null);
      }
   }

   public EvalTemplateItem newTemplateItem() {
      String level = EvalConstants.HIERARCHY_LEVEL_TOP;
      String nodeId = EvalConstants.HIERARCHY_NODE_ID_NONE;

      // TODO - this should respect the current level the user is at

      // TODO currently creating a fake template (newTemplate()) so the bind does not fail, this should supposedly use a defunneler
      EvalTemplateItem newTemplateItem = new EvalTemplateItem( commonLogic.getCurrentUserId(), 
            newTemplate(), newItem(), null, EvalToolConstants.ITEM_CATEGORY_VALUES[0], 
            level, nodeId);
      newTemplateItem.setUsesNA(new Boolean(false));
      return newTemplateItem;
   }

   /**
    * Saves the templateItem (does not save the associated item)
    * @param templateItem
    */
   public void saveTemplateItem(EvalTemplateItem templateItem) {
      /* This is a temporary hack that is only good while we are only using TOP LEVEL and NODE LEVEL.
       * Basically, we're putting everything in one combo box and this is a good way to check to see if
       * it's the top node.  Otherwise the user selected a node id so it must be at the NODE LEVEL since
       * we don't support the other levels yet.
       */
      if (templateItem.getHierarchyNodeId() != null && !templateItem.getHierarchyNodeId().equals("")
            && !templateItem.getHierarchyNodeId().equals(EvalConstants.HIERARCHY_NODE_ID_NONE)) {
         templateItem.setHierarchyLevel(EvalConstants.HIERARCHY_LEVEL_NODE);
      }
      else if (templateItem.getHierarchyNodeId() != null && !templateItem.getHierarchyNodeId().equals("")
            && templateItem.getHierarchyNodeId().equals(EvalConstants.HIERARCHY_NODE_ID_NONE)) {
         templateItem.setHierarchyLevel(EvalConstants.HIERARCHY_LEVEL_TOP);
      }

      if (templateItem.getItem() == null) {
         // failure to associate an item with this templateItem
         throw new IllegalStateException("No item is associated with this templateItem (the item is null) so it cannot be saved");
      }
      authoringService.saveTemplateItem(templateItem, commonLogic.getCurrentUserId());
   }


   /**
    * Handles the removal of a templateItem, includes security check and 
    * takes care of reordering or other items in the template<br/>
    * Blocks: splits up the block and removes the parent item if a block parent is selected for removal
    * 
    * @param templateItemId a unique id of an {@link EvalTemplateItem}
    */
   public void deleteTemplateItem(Long templateItemId) {
      String currentUserId = commonLogic.getCurrentUserId();
      authoringService.deleteTemplateItem(templateItemId, currentUserId);
   }


   // ITEMS

   public EvalItem fetchItem(Long itemId) {
      return authoringService.getItemById(itemId);
   }

   public void saveItem(EvalItem item) {
      connectScaleToItem(item);
      authoringService.saveItem(item, commonLogic.getCurrentUserId());
   }

   /**
    * Hides an item (this will make the item inaccessible to users)
    * 
    * @param itemId the unique id of an item
    */
   public void hideItem(Long itemId) {
      EvalItem item = fetchItem(itemId);
      item.setHidden(true);
      authoringService.saveItem(item, commonLogic.getCurrentUserId());
   }

   public void deleteItem(Long id) {
      authoringService.deleteItem(id, commonLogic.getCurrentUserId());
   }

   public EvalItem newItem() {
      EvalItem newItem = new EvalItem(commonLogic.getCurrentUserId(), "", EvalConstants.SHARING_PRIVATE, 
            "", Boolean.FALSE);
      newItem.setCategory( EvalConstants.ITEM_CATEGORY_COURSE ); // default category
      newItem.setScale(newScale()); // create a holder for a new scale which will get overwritten or cleared out if not used
      return newItem;
   }


   // SCALES

   public EvalScale fetchScale(Long scaleId) {
      EvalScale scale = authoringService.getScaleById(scaleId);
      // TODO - hopefully this if block is only needed temporarily until RSF 0.7.3
      if (scale.getIdeal() == null) {
         scale.setIdeal(EvalToolConstants.NULL);
      }
      return scale;
   }

   public void saveScale(EvalScale scale) {
      // TODO - hopefully this if block is only needed temporarily until RSF 0.7.3
      if (scale.getIdeal() != null &&
            scale.getIdeal().equals(EvalToolConstants.NULL)) {
         scale.setIdeal(null);
      }
      authoringService.saveScale(scale, commonLogic.getCurrentUserId());
   }

   /**
    * Hides a scale (this will make the scale inaccessible to users)
    * 
    * @param scaleId the unique id of a scale
    */
   public void hideScale(Long scaleId) {
      EvalScale scale = fetchScale(scaleId);
      scale.setHidden(true);
      authoringService.saveScale(scale, commonLogic.getCurrentUserId());
   }

   public void deleteScale(Long id) {
      authoringService.deleteScale(id, commonLogic.getCurrentUserId());
   }

   public EvalScale newScale() {
      EvalScale currScale = new EvalScale(commonLogic.getCurrentUserId(), 
            null, EvalConstants.SCALE_MODE_SCALE, 
            EvalConstants.SHARING_PRIVATE, Boolean.FALSE);
      currScale.setOptions(EvalToolConstants.defaultInitialScaleValues);
      currScale.setIdeal(EvalToolConstants.NULL); // TODO - temp until RSF 0.7.3
      return currScale;
   }


   // UTILITY METHODS

   /**
    * This will connect an item to a scale and save the scale if it is new (not persistent yet),
    * the default values for the new adhoc scale will be set,
    * this will also clear out the scale if it is not used for this type of item
    * @param item an item to be saved, can be persistent or new
    */
   private void connectScaleToItem(EvalItem item) {
      // this is here to cleanup the fake scale in case it was not needed or load a real one
      if (item.getScale() != null) {
         // only process scales for the types that use them
         if ( EvalConstants.ITEM_TYPE_SCALED.equals(item.getClassification()) ||
               EvalConstants.ITEM_TYPE_BLOCK_PARENT.equals(item.getClassification()) || // http://www.caret.cam.ac.uk/jira/browse/CTL-557
               EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(item.getClassification()) || 
               EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(item.getClassification()) ) {
            // for multiple type items we need to save the scale
            if ( EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(item.getClassification()) || 
                  EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(item.getClassification()) ) {
               // only make the connection for new (non-persistent) scales
               EvalScale scale = item.getScale();
               if (scale.getId() == null) {
                  // this is a new scale which must be saved so it can be saved with the item

                  // set up default values for new scales
                  if (scale.getMode() == null) {
                     scale.setMode(EvalConstants.SCALE_MODE_ADHOC);
                  }
                  if (scale.getOwner() == null) {
                     if (item.getOwner() != null) {
                        scale.setOwner(item.getOwner());
                     } else {
                        scale.setOwner(commonLogic.getCurrentUserId());
                     }
                  }
                  if (scale.getTitle() == null) {
                     scale.setTitle(EvalConstants.SCALE_ADHOC_DEFAULT_TITLE);
                  }
               }
               // new and existing scales need to be saved
               saveScale(scale);
            } else {
               // scaled/block parent item so don't save the scale
               if (item.getScale().getId() != null && 
                     item.getScale().getOwner() == null) {
                  // this is an existing scale and we need to turn the fake one into a real one so hibernate can make the connection
                  item.setScale(authoringService.getScaleById(item.getScale().getId()));
               }
            }
         } else {
            // null out the scale as it is not used for this type of item
            item.setScale(null);            
         }
      }
   }


   /**
    * TODO - this should use the defunneler -AZ (so says antranig)
    * This is here to fix up a templateItem which is not actually correctly connected via the foreign key,
    * it is not ideal but it works
    * @param tosave
    */
// private void connectTemplateToTI(EvalTemplateItem tosave) {
// if (tosave.getTemplate() != null) {
// Long templateId = tosave.getTemplate().getId();
// if (templateId != null) {
// // this lookup is needed so hibernate can make the connection
// tosave.setTemplate(authoringService.getTemplateById(templateId));
// } else {
// // the template was not set correctly so we have to die
// throw new NullPointerException("id is not set for the template for this templateItem (" + tosave +
// "), all templateItems must be associated with an existing template");
// }
// }
// }

}
