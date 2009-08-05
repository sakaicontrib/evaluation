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
import java.util.HashMap;
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
	@EntityCustomAction(action=CUSTOM_TEMPLATE_ITEMS_REORDER,viewKey=EntityView.VIEW_NEW)
	public void saveTemplateItemsOrdering(EntityView view, Map<String, Object> params) {
		Object ids = params.get(key_ordered_Ids);
		String currentUserId = commonLogic.getCurrentUserId();
		Map<Long, Integer> orderedMap = new HashMap<Long, Integer>();
		
		if ( ids != null ){
			String orderedChildIds = ids.toString();
			List<String> orderedChildIdList = Arrays.asList(orderedChildIds.split(","));
	        for( String itemId : orderedChildIdList ){
				int itemPosition = orderedChildIdList.indexOf( itemId ) + 1;
				orderedMap.put( Long.parseLong(itemId), itemPosition);
			}
			authoringService.saveTemplateItemOrder(orderedMap, currentUserId);
		}else{
			throw new IllegalArgumentException("No ordered Ids to process.");
		}
	}

}
