/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
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
    
    private final static String KEY_ORDERED_IDS = "orderedIds";

  //parameter name keys for {@link modifyBlockItems} method
    private final static String KEY_BLOCK_ID = "blockid";
    private final static String KEY_ITEMS_TO_ADD = "additems";

  //parameter name key for {@link unblock} method
    private final static String KEY_ITEM_ID = "itemid";

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

    //Custom action to handle /eval-templateitem/template-items-reorder
    @EntityCustomAction(action = CUSTOM_TEMPLATE_ITEMS_REORDER, viewKey = EntityView.VIEW_NEW)
    public void saveTemplateItemsOrdering(EntityView view, Map<String, Object> params) {
        Object ids = params.get(KEY_ORDERED_IDS);
        if (ids != null && !"".equals(ids)) {
            String orderedChildIds = ids.toString().trim();
            if (!"".equals(orderedChildIds)) {
                String currentUserId = commonLogic.getCurrentUserId();
                Map<Long, Integer> orderedMap = new HashMap<>();
                List<String> orderedChildIdList = Arrays.asList(orderedChildIds.split(","));
                for (String itemId : orderedChildIdList) {
                    int itemPosition = orderedChildIdList.indexOf(itemId) + 1;
                    orderedMap.put(Long.parseLong(itemId), itemPosition);
                }
                authoringService.saveTemplateItemOrder(orderedMap, currentUserId);
            } else {
                throw new IllegalArgumentException("No ordered Ids to process (string had only whitespace).");
            }
        } else {
            throw new IllegalArgumentException("No ordered Ids to process (blank or null).");
        }
    }

	//Custom method to handle /eval-templateitem/modify-block-items
	@EntityCustomAction(action=CUSTOM_TEMPLATE_ITEMS_BLOCK,viewKey=EntityView.VIEW_NEW)
	public void modifyBlockItems(EntityView view, Map<String, Object> params) {
		Long blockId = Long.parseLong( params.get(KEY_BLOCK_ID).toString() );
		String currentUserId = commonLogic.getCurrentUserId();
		String itemsToAddParams = params.get(KEY_ITEMS_TO_ADD).toString();
		List<String> itemsToAdd = Arrays.asList(itemsToAddParams.split(","));
		
		EvalTemplateItem parent = authoringService.getTemplateItemById(blockId);
		List<EvalTemplateItem> children = authoringService.getBlockChildTemplateItemsForBlockParent(blockId, false);
		List<EvalTemplateItem> orderedChildren = TemplateItemUtils.getChildItems(children, blockId);
		
		//update children order value to reflect the display order
		int orderCurrentChildren = 1;
		for ( EvalTemplateItem child : orderedChildren){
			child.setDisplayOrder(orderCurrentChildren);
            authoringService.saveTemplateItem(child, currentUserId);
            orderCurrentChildren ++;
		}
		int orderNewChildren = 1;
		for ( String itemIdstring : itemsToAdd){
			Long itemId = Long.parseLong(itemIdstring);
			EvalTemplateItem child = authoringService.getTemplateItemById(itemId);
			child.setBlockParent(Boolean.FALSE);
			child.setBlockId(blockId);
			child.setDisplayOrder(orderedChildren.size() + orderNewChildren);
            child.setCategory(parent.getCategory()); // EVALSYS-441
            child.setUsesNA(parent.getUsesNA()); // child inherits parent NA setting EVALSYS-549
            // children have to inherit the parent hierarchy settings
            child.setHierarchyLevel(parent.getHierarchyLevel());
            child.setHierarchyNodeId(parent.getHierarchyNodeId());
            authoringService.saveTemplateItem(child, currentUserId);   
            orderNewChildren ++;
		}
	}
	
	//Custom method to handle /eval-templateitem/unblock
	@EntityCustomAction(action=CUSTOM_TEMPLATE_ITEMS_UNBLOCK,viewKey=EntityView.VIEW_NEW)
	public void unblock(EntityView view, Map<String, Object> params) {
		Object rawId = params.get(KEY_ITEM_ID);
		if( rawId !=null ){
			Long itemId = Long.parseLong( rawId.toString() );
			String currentUserId = commonLogic.getCurrentUserId();
			EvalTemplateItem templateItem = authoringService.getTemplateItemById( itemId );
			if (TemplateItemUtils.isBlockChild(templateItem)){
				templateItem.setBlockParent(null);
				templateItem.setBlockId(null);
				templateItem.setDisplayOrder(null);  //saving item without order will put it at the bottom of the template.
				authoringService.saveTemplateItem(templateItem, currentUserId);
			}else{
				throw new IllegalStateException("Template item "+ itemId +" is not part of a group!");
			}
		}else{
			throw new IllegalArgumentException("No item Id to process.");
		}
		
	}
}
