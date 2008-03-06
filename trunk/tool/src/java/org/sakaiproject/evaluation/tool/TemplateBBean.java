/**
 * TemplateBBean.java - evaluation - Jan 16, 2007 11:35:56 AM - azeckoski
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.locators.ItemBeanWBL;
import org.sakaiproject.evaluation.tool.locators.TemplateBeanLocator;
import org.sakaiproject.evaluation.tool.locators.TemplateItemWBL;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

/**
 * This request-scope bean handles template creation and modification and actions related
 * to templates, template items, and items
 * 
 * @author Will Humphries (whumphri@vt.edu)
 * @author Antranig Basman
 * @author Rui Feng (fengr@vt.edu)
 */
public class TemplateBBean {

   private static Log log = LogFactory.getLog(TemplateBBean.class);

   private LocalTemplateLogic localTemplateLogic;
   public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
      this.localTemplateLogic = localTemplateLogic;
   }

   private ItemBeanWBL itemBeanWBL;
   public void setItemBeanWBL(ItemBeanWBL itemBeanWBL) {
      this.itemBeanWBL = itemBeanWBL;
   }

   private TemplateItemWBL templateItemWBL;
   public void setTemplateItemWBL(TemplateItemWBL templateItemWBL) {
      this.templateItemWBL = templateItemWBL;
   }

   private TemplateBeanLocator templateBeanLocator;
   public void setTemplateBeanLocator(TemplateBeanLocator templateBeanLocator) {
      this.templateBeanLocator = templateBeanLocator;
   }

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }



   public Long templateId;
   public Boolean idealColor;
   public Integer originalDisplayOrder;
   public String blockId;
   public String blockTextChoice;
   public String orderedChildIds;
   public String templateItemIds;


   // TEMPLATES
   /**
    * If the template is not saved, button will show text "continue and add
    * question" method binding to the "continue and add question" button on
    * template_title_description.html replaces
    * TemplateBean.createTemplateAction, but template is added to db here.
    */
   public String createTemplateAction() {
      log.debug("create template");
      templateBeanLocator.saveAll();
      return "success";
   }

   /**
    * If the template is already stored, button will show text "Save" method
    * binding to the "Save" button on template_title_description.html replaces
    * TemplateBean.saveTemplate()
    */
   public String updateTemplateTitleDesc() {
      log.debug("update template title/desc");
      templateBeanLocator.saveAll();
      return "success";
   }


   // ITEMS

   public String saveItemAction() {
      log.info("save item");
      itemBeanWBL.saveAll();
      return "success";
   }


   // TEMPLATE ITEMS

   public String saveTemplateItemAction() {
      log.debug("save template item");
      templateItemWBL.saveAll();
      return "success";
   }


   public String saveBothAction() {
      log.info("save template item and item");
      templateItemWBL.saveBoth();
      return "success";
   }

   private void emit(EvalTemplateItem toemit, int outindex) {
      log.debug("EvalTemplateItem toemit: " + toemit.getId() + ", outindex: " + outindex);
      toemit.setDisplayOrder(new Integer(outindex));
      localTemplateLogic.saveTemplateItem(toemit);
   }

   /**
    * NB - this implementation depends on Hibernate reference equality
    * semantics!! Guarantees output sequence is consecutive without duplicates,
    * and will prefer honoring user sequence requests so long as they are not
    * inconsistent.
    */
   // TODO: This method needs to be invoked via a BeanGuard, trapping any
   // access to templateItemWBL.*.displayOrder
   // Current Jquery implementation is only working as a result of auto-commit
   // bug in DAO wrapper implementation.
   public void saveReorder() { 
      log.info("save items reordering");
      Map<String, EvalTemplateItem> delivered = templateItemWBL.getDeliveredBeans();
      List<EvalTemplateItem> l = authoringService.getTemplateItemsForTemplate(templateId, null, null, null);
      List<EvalTemplateItem> ordered = TemplateItemUtils.getNonChildItems(l);
      for (int i = 1; i <= ordered.size();) {
         EvalTemplateItem item = (EvalTemplateItem) ordered.get(i - 1);
         int itnum = item.getDisplayOrder().intValue();
         if (i < ordered.size()) {
            EvalTemplateItem next = (EvalTemplateItem) ordered.get(i);
            int nextnum = next.getDisplayOrder().intValue();
            // only make a write or adjustment if we would be about to commit two
            // items with the same index. 
            if (itnum == nextnum) {
               // if the user requested this item XOR it is in the right place,
               // emit this one second. That is, if the user wants it here and there
               // is no conflict, write it here.
               if (delivered.containsValue(item) ^ (itnum == i)) {
                  emit(next, i++);
                  emit(item, i++);
                  continue;
               } 
               else {
                  emit(item, i++);
                  emit(next, i++);
                  continue;
               }
            }
         }
         emit(item, i++);
      }
   }

   /**
    * Action to save a block type item
    * 
    * @return
    */
   public String saveBlockItemAction() {
      log.debug("Save Block items");

      Map<String, EvalTemplateItem> delivered = templateItemWBL.getDeliveredBeans();

      // Note: Arrays.asList() produces lists that do not support add() or remove(), however set() is supported
      // We may want to change this to ArrayList. (i.e. new ArrayList(Arrays.asList(...)))
      List<String> orderedChildIdList = Arrays.asList(orderedChildIds.split(","));
      List<String> templateItemIdList = Arrays.asList(templateItemIds.split(","));

      if (blockId.equals(TemplateItemWBL.NEW_1)) { // create new block
         EvalTemplateItem parent = (EvalTemplateItem) delivered.get(TemplateItemWBL.NEW_1);
         if (parent != null) {
            EvalTemplateItem templateItem = null;
            if (blockTextChoice != null && 
                  (! blockTextChoice.equals(TemplateItemWBL.NEW_1)) ) {
               templateItem = authoringService.getTemplateItemById(Long.valueOf(blockTextChoice));
               parent.getItem().setItemText(templateItem.getItem().getItemText());
            } else {
               templateItem = authoringService.getTemplateItemById(Long.valueOf(orderedChildIdList.get(0)));
            }

            parent.setTemplate(templateItem.getTemplate());
            parent.getItem().setScale(templateItem.getItem().getScale());

            parent.setBlockParent(Boolean.TRUE);
            parent.getItem().setClassification(EvalConstants.ITEM_TYPE_BLOCK_PARENT);
            parent.getItem().setSharing(parent.getTemplate().getSharing());
            parent.getItem().setUsesNA(parent.getUsesNA());
            parent.getItem().setCategory(parent.getCategory());
            setIdealColorForBlockParent(parent);

            localTemplateLogic.saveItem(parent.getItem());
            localTemplateLogic.saveTemplateItem(parent);
         } else {
            parent = authoringService.getTemplateItemById(Long.valueOf(blockTextChoice));
         }

         // set parent's display order to the original display order of the first selected template item
         parent.setDisplayOrder(originalDisplayOrder);

         List<EvalTemplateItem> allTemplateItems = authoringService.getTemplateItemsForTemplate(parent.getTemplate().getId(), null, null, null);
         Long parentId = parent.getId();
         List<EvalTemplateItem> blockChildren = null;

         // if parent is in templateItemIdList (i.e. it is not a newly created item), then put it at the front of the list
         // so that it doesn't have to iterate through all the children that will be added to it (assuming it is late in the late in the list)

         if (parentId.toString().equals(blockTextChoice)) {
            templateItemIdList.set(templateItemIdList.indexOf(parentId.toString()), templateItemIdList.get(0));
            templateItemIdList.set(0, parentId.toString());
         }

         // iterate through templateItemIdList and add all non-block items (including the children of block items in templateItemIdList) 
         for (String itemId : templateItemIdList) {
            EvalTemplateItem templateItem = authoringService.getTemplateItemById(Long.valueOf(itemId));
            if (TemplateItemUtils.getTemplateItemType(templateItem).equals(EvalConstants.ITEM_TYPE_BLOCK_PARENT)) { // block parent
               blockChildren = TemplateItemUtils.getChildItems(allTemplateItems, templateItem.getId());
               for (EvalTemplateItem child : blockChildren) {
                  child.setBlockId(parentId);
                  child.setDisplayOrder(new Integer(orderedChildIdList.indexOf(child.getId().toString()) + 1));
                  localTemplateLogic.saveTemplateItem(child);
               }

               // delete remaining block parent if it is not the parent (only applicable if parent is not newly created)
               if (parent != templateItem) {
                  localTemplateLogic.deleteTemplateItem(templateItem.getId());
               }

            } else { // normal scale type
               templateItem.setBlockParent(Boolean.FALSE);
               templateItem.setBlockId(parentId);
               templateItem.setDisplayOrder(new Integer(orderedChildIdList.indexOf(itemId) + 1));
               localTemplateLogic.saveTemplateItem(templateItem);
            }

         } // end for

         // shifting the order of the other items in the template
         allTemplateItems = authoringService.getTemplateItemsForTemplate(parent.getTemplate().getId(), null, null, null);
         List<EvalTemplateItem> nonChildList = TemplateItemUtils.getNonChildItems(allTemplateItems);

         if ((originalDisplayOrder.intValue() < nonChildList.size()) && 
               (nonChildList.get(originalDisplayOrder.intValue()).getId() == parentId)) {
            nonChildList.remove(originalDisplayOrder.intValue());
            nonChildList.add(originalDisplayOrder - 1, parent);
         }

         for (int i = originalDisplayOrder.intValue(); i < nonChildList.size(); i++) {
            EvalTemplateItem templateItem = nonChildList.get(i);
            if (templateItem.getDisplayOrder().intValue() != (i + 1)) {
               templateItem.setDisplayOrder(new Integer(i + 1));
               localTemplateLogic.saveTemplateItem(templateItem);
            }
         }

      } else { // modify block
         EvalTemplateItem parent = authoringService.getTemplateItemById(Long.valueOf(blockId));
         saveBlockChildrenOrder(parent.getId(), orderedChildIdList);
         setIdealColorForBlockParent(parent);
         localTemplateLogic.saveItem(parent.getItem());
         localTemplateLogic.saveTemplateItem(parent);
      }

      return "success";
   }

   private void saveBlockChildrenOrder(Long parentId, List<String> orderedChildIdList) {

      List<EvalTemplateItem> blockChildren = authoringService.getBlockChildTemplateItemsForBlockParent(parentId, false);

      for (EvalTemplateItem child : blockChildren) {
         child.setDisplayOrder(new Integer(orderedChildIdList.indexOf(child.getId().toString()) + 1));
         localTemplateLogic.saveTemplateItem(child);
      }

   }

   private void setIdealColorForBlockParent(EvalTemplateItem parent) {
      /*
		if (idealColor != null) { // only reset when this field is changed
			if (idealColor == Boolean.TRUE) {
				item.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED);
				item.getItem().setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED);
			} else {
				item.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED);
				item.getItem().setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED);
			}
		}
       */

      if ((idealColor != null) && (idealColor == Boolean.TRUE)) {
         parent.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED);
         parent.getItem().setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED);
      } else {
         parent.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED);
         parent.getItem().setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED);
      }

   }

}