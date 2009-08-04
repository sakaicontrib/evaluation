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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Deleteable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

/**
 * Implementation for the entity provider for template items (questions in a template)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TemplateItemEntityProviderImpl implements TemplateItemEntityProvider,
        CoreEntityProvider, AutoRegisterEntityProvider, Resolvable, Outputable, Deleteable, ActionsExecutable {

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private DeveloperHelperService developerHelperService;
    public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
        this.developerHelperService = developerHelperService;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }
    
    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }
    
    private final static String key_template_itemId = "templateItemId";
    private final static String key_template_id = "templateId";
    private final static String key_ordered_Ids = "orderedIds";

    public boolean entityExists(String id) {
        boolean exists = false;
        Long templateItemId;
        try {
            templateItemId = Long.valueOf(id);
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
        // check if the current user can access this
        String userRef = developerHelperService.getCurrentUserReference();
        if (userRef == null) {
            throw new SecurityException("Anonymous users may not access template items directly, acessing TI: " + ref);
        }
        Long templateItemId = getIdFromRef(ref);
        EvalTemplateItem item = authoringService.getTemplateItemById(templateItemId);
        if (item != null) {
            EvalTemplateItem clone = developerHelperService.cloneBean(item, 1, new String[] {});
            return clone;
        } else {
            throw new IllegalArgumentException("id is invalid.");
        }
    }

    public String[] getHandledOutputFormats() {
        return new String[] { Formats.XML, Formats.JSON };
    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        Long templateItemId = getIdFromRef(ref);
        String currentUserId = commonLogic.getCurrentUserId();
        // throws SecurityException if not allowed
        authoringService.deleteTemplateItem(templateItemId, currentUserId);
    }

    /**
     * Extract a numeric id from the ref if possible
     * @param ref the entity reference
     * @return the Long number version of the id
     * @throws IllegalArgumentException if the number cannot be extracted
     */
    protected Long getIdFromRef(EntityReference ref) {
        Long id = null;
        String refId = ref.getId();
        if (refId != null) {
            try {
                id = Long.valueOf(refId);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number found in reference ("+ref+") id: " + e);
            }
        } else {
            throw new IllegalArgumentException("No id in reference ("+ref+") id, cannot extract numeric id");
        }
        return id;
    }
    
  //Custom action to handle /eval-templateitem/block-reorder
	@EntityCustomAction(action=CUSTOM_BLOCK_REORDER,viewKey=EntityView.VIEW_NEW)
	public void saveBlockItemsOrdering(EntityView view, Map<String, Object> params) {
		Object id = params.get(key_template_itemId);
		Object ids = params.get(key_ordered_Ids);
		String currentUserId = commonLogic.getCurrentUserId();
		if ( id != null && ids != null ){
			Long templateItemId = Long.parseLong( id.toString() );
			String orderedChildIds = ids.toString();
			List<String> orderedChildIdList = Arrays.asList(orderedChildIds.split(","));
	        List<EvalTemplateItem> blockChildren = authoringService.getBlockChildTemplateItemsForBlockParent(templateItemId, false);
	        for (EvalTemplateItem child : blockChildren) {
	            child.setDisplayOrder(new Integer(orderedChildIdList.indexOf(child.getId().toString()) + 1));
	            authoringService.saveTemplateItem(child, currentUserId);
	        }
		}else{
			throw new IllegalArgumentException("No ordered Ids to process.");
		}
	}

	//Custom action to handle /eval-templateitem/template-reorder
	@EntityCustomAction(action=CUSTOM_TEMPLATE_REORDER,viewKey=EntityView.VIEW_NEW)
	public void saveTemplateItemsOrdering(EntityView view, Map<String, Object> params) {
		Object id = params.get(key_template_id);
		Object ids = params.get(key_ordered_Ids);
		String currentUserId = commonLogic.getCurrentUserId();
		if ( id != null && ids != null ){
			Long templateId = Long.parseLong( id.toString() );
			String orderedItemIds = ids.toString();
			List<String> orderedItemIdsList = Arrays.asList(orderedItemIds.split(","));
			
			
	        
			List<EvalTemplateItem> templateItemsForTemplate = authoringService.getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {});
	        List<EvalTemplateItem> ordered = TemplateItemUtils.getNonChildItems(templateItemsForTemplate);
	        
	        for( EvalTemplateItem templateItem : ordered ){
	        	Long itemId = templateItem.getId();
	        	templateItem.setDisplayOrder( new Integer(orderedItemIdsList.indexOf(itemId.toString() + 1)) );
	        	System.out.println("Set item: " + itemId + " to number: " +  orderedItemIdsList.indexOf(itemId.toString() + 1));
	        	authoringService.saveTemplateItem(templateItem, currentUserId);
	        }
	        
	        /*
	        
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
	            
	            System.out.println("EvalTemplateItem toemit: " + item.getId() + ", outindex: " + i++);
	            item.setDisplayOrder(new Integer(i++));
	            localTemplateLogic.saveTemplateItem(item);
	            
	            
	        }*/
	        // this will seem a little odd but we are saving the template to validate the order of all templateItems
	       // authoringService.saveTemplate(localTemplateLogic.fetchTemplate(templateId));
	        
	        
	        
			
		}else{
			throw new IllegalArgumentException("No ordered Ids to process.");
		}
	}

}
