/**
 * $Id$
 * $URL$
 * TemplateItemEntityProviderImpl.java - evaluation - Jan 31, 2008 2:16:46 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.entity.TemplateItemEntityProvider;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.springframework.orm.hibernate3.HibernateTemplate;



/**
 * Implementation for the entity provider for template items (questions in a template)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TemplateItemEntityProviderImpl implements TemplateItemEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, Resolvable, Outputable, Deleteable {

	private static Log log = LogFactory.getLog(TemplateItemEntityProviderImpl.class);
	
   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }
   private DeveloperHelperService developerHelperService;
   public void setDeveloperHelperService(DeveloperHelperService developerHelperService){
	   this.developerHelperService = developerHelperService;
   }
   private EvalCommonLogic commonLogic;
   public void setCommonLogic(EvalCommonLogic commonLogic) {
      this.commonLogic = commonLogic;
   }


   public String getEntityPrefix() {
      return ENTITY_PREFIX;
   }

   public boolean entityExists(String id) {
      boolean exists = false;
      Long templateItemId;
      try {
         templateItemId = new Long(id);
         if (authoringService.getTemplateItemById(templateItemId) != null) {
            exists = true;
         }
      } catch (NumberFormatException e) {
         // invalid number so roll through to the false
         exists = false;
      }
      return exists;
   }


public Object getEntity(EntityReference ref) {
	// TODO Auto-generated method stub
	EvalTemplateItem item = authoringService.getTemplateItemById(new Long(ref.getId()));
	if(item != null && isAllowedAccessEvalTemplateItem(ref)){
		return (developerHelperService.cloneBean(item, 1, new String[]{})); 
	}
	else
		throw new IllegalArgumentException("id is invalid.");
	
}


public String[] getHandledOutputFormats() {
	// TODO Auto-generated method stub
	return new String[] {Formats.XML};
}

protected boolean isAllowedAccessEvalTemplateItem(EntityReference ref) {
    // check if the current user can access this
    String userRef = developerHelperService.getCurrentUserReference();
    if (userRef == null) {
        throw new SecurityException("Anonymous users may not view this Eval-item");
    } else {
        if (!developerHelperService.isUserAllowedInEntityReference(userRef, "VIEW", ref.getId())) {
            throw new SecurityException("This Eval-item is not accessible for the current user: " + userRef);
        }
    }
    return true;
}


public void deleteEntity(EntityReference ref, Map<String, Object> params) {
	String id = ref.getId();
	Iterator it = params.entrySet().iterator();
    while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry)it.next();
        //log.info(pairs.getKey() + " = " + pairs.getValue() + "     ");
    }

    if (id == null) {
        throw new IllegalArgumentException("The reference must include an id for deletes (id is currently null)");
    }
	if(isAllowedAccessEvalTemplateItem(ref)){
		//authoringService.deleteTemplateItem(new Long(ref.getId()), developerHelperService.getCurrentUserId());
		deleteTemplateItem(new Long(ref.getId()));
	}
	else
		throw new SecurityException("This Eval-item is not accessible for the current user. ");
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
   Hibernate.initialize(authoringService);
   if (! authoringService.canControlTemplateItem(currentUserId, templateItemId)) {
      throw new SecurityException("User ("+currentUserId+") cannot control this template item ("+templateItemId+")");
   }

   EvalTemplateItem templateItem = authoringService.getTemplateItemById(templateItemId);
   // get a list of all template items in this template
   List<EvalTemplateItem> allTemplateItems = 
      authoringService.getTemplateItemsForTemplate(templateItem.getTemplate().getId(), new String[] {}, new String[] {}, new String[] {});
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
      authoringService.deleteTemplateItem(templateItem.getId(), currentUserId);
      // if this parent is used elsewhere then this will cause exception - EVALSYS-559
      if (authoringService.isUsedItem(itemId)) {
         log.info("Cannot remove block parent item ("+itemId+") - item is in use elsewhere");
      } else {
         authoringService.deleteItem(itemId, currentUserId);
      }

      // modify block children template items
      for (int i = 0; i < childList.size(); i++) {
         EvalTemplateItem child = (EvalTemplateItem) childList.get(i);
         child.setBlockParent(null);
         child.setBlockId(null);
         child.setDisplayOrder(new Integer(removedItemDisplayOrder + i));
        // authoringService.saveTemplateItem(child, currentUserId);  //THROWS SESSION ERROr
      }

   } else { // non-block cases
      removedItemDisplayOrder = templateItem.getDisplayOrder().intValue();
      authoringService.deleteTemplateItem(templateItem.getId(), currentUserId);
   }

   // shift display-order of items below removed item
   for (int i = removedItemDisplayOrder; i < noChildList.size(); i++) {
      EvalTemplateItem ti = (EvalTemplateItem) noChildList.get(i);
      int order = ti.getDisplayOrder().intValue();
      if (order > removedItemDisplayOrder) {
         ti.setDisplayOrder(new Integer(order + orderAdjust - 1));
         //authoringService.saveTemplateItem(ti, currentUserId);  //THROWS SESSION ERROr
      }
   }
}


}
