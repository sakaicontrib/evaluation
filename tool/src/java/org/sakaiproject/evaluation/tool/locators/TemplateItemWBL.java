/**
 * TemplateItemWBL.java - evaluation - Jan 23, 2007 11:35:56 AM - whumphri@vt.edu
 * $URL: https://source.sakaiproject.org/contrib $
 * $Id: Locator.java 11234 Oct 29, 2007 11:35:56 AM azeckoski $
 **************************************************************************
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;

import uk.org.ponder.beanutil.WriteableBeanLocator;

/**
 * This is the OTP bean used to locate {@link EvalTemplateItem}s
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Will Humphries
 */
public class TemplateItemWBL implements WriteableBeanLocator {
   public static final String NEW_PREFIX = "new";
   public static String NEW_1 = NEW_PREFIX +"1";

   private LocalTemplateLogic localTemplateLogic;
   public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
      this.localTemplateLogic = localTemplateLogic;
   }

   // keep track of all template items that have been delivered during this request
   private Map<String, EvalTemplateItem> delivered = new HashMap<String, EvalTemplateItem>();

   /* (non-Javadoc)
    * @see uk.org.ponder.beanutil.BeanLocator#locateBean(java.lang.String)
    */
   public Object locateBean(String name) {
      EvalTemplateItem togo = delivered.get(name);
      if (togo == null) {
         if (name.startsWith(NEW_PREFIX)) {
            togo = localTemplateLogic.newTemplateItem();
         } else {
            togo = localTemplateLogic.fetchTemplateItem(new Long(name));
         }
         delivered.put(name, togo);
      }
      return togo;
   }

   public Map<String, EvalTemplateItem> getDeliveredBeans() {
      return delivered;
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.beanutil.WriteableBeanLocator#remove(java.lang.String)
    */
   public boolean remove(String beanname) {
      Long templateItemId = Long.valueOf(beanname);
      localTemplateLogic.deleteTemplateItem(templateItemId);
      delivered.remove(beanname);
      return true;
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.beanutil.WriteableBeanLocator#set(java.lang.String, java.lang.Object)
    */
   public void set(String beanname, Object toset) {
      throw new UnsupportedOperationException("Not implemented");
   }

   /**
    * saves all delivered template items in this request, 
    * also saves the associated new items (does not save any associated existing items)
    */
   public void saveAll() {
      for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
         String key = it.next();
         EvalTemplateItem templateItem = (EvalTemplateItem) delivered.get(key);
         if (key.startsWith(NEW_PREFIX)) {
            // add in extra logic needed for new template items here
            if (templateItem.getItem().getId() == null) {
               prepNewItem(templateItem);
               // save the item
               localTemplateLogic.saveItem( templateItem.getItem() );
            }
         }
         localTemplateLogic.saveTemplateItem(templateItem);
      }
   }

   /**
    * saves all delivered template items and the associated items (new or existing)
    */
   public void saveBoth() {
      for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
         String key = it.next();
         EvalTemplateItem templateItem = (EvalTemplateItem) delivered.get(key);
         if (key.startsWith(NEW_PREFIX)) {
            // add in extra logic needed for new template items here
            // prep the item and template item to be saved if the item is new
            if (templateItem.getItem().getId() == null) {
               prepNewItem(templateItem);
            }
         }
         // save the item
         localTemplateLogic.saveItem( templateItem.getItem() );
         // then save the templateItem
         localTemplateLogic.saveTemplateItem(templateItem);
      }
   }

   /**
    * prepare the new item to be saved
    * @param templateItem
    */
   private void prepNewItem(EvalTemplateItem templateItem) {
      // new item with our new template item so set the values in the new item
      templateItem.getItem().setScaleDisplaySetting(templateItem.getScaleDisplaySetting());
      templateItem.getItem().setUsesNA(templateItem.getUsesNA());
      templateItem.getItem().setDisplayRows(templateItem.getDisplayRows());
      if (templateItem.getTemplate() != null) {
         templateItem.getItem().setCategory(templateItem.getItemCategory());
         templateItem.getItem().setSharing(templateItem.getTemplate().getSharing());
      } else {
         // defaults if template is not connected yet
         templateItem.getItem().setCategory(EvalConstants.ITEM_CATEGORY_COURSE);
         templateItem.getItem().setSharing(EvalConstants.SHARING_PRIVATE);
      }
   }

}