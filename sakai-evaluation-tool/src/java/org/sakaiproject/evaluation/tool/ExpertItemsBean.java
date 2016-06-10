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
package org.sakaiproject.evaluation.tool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

/**
 * The backing bean for expert and existing items adding
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ExpertItemsBean {

    private static final Log LOG = LogFactory.getLog(ExpertItemsBean.class);

    public Map<String, Boolean> selectedIds = new HashMap<>();
    public Long templateId;

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }
    
    /*
     * Members destined for EL bindings.
     */
    public Long eigId;
    public Long eigParentId;
    public boolean eigIsNew;
    public String eigType;
    public String eigTitle;
    public String eigDesc;

    public ExpertItemsBean() { }

    /**
     * Creates templateItems to add items to a template
     * @return the status location
     */
    public String processActionAddItems() {
        LOG.debug("in process action add items, selectedItems=" + selectedIds.size());

        String currentUserId = commonLogic.getCurrentUserId();
        String hierarchyLevel = EvalConstants.HIERARCHY_LEVEL_TOP;
        String hierarchyNodeId = EvalConstants.HIERARCHY_NODE_ID_NONE;

        EvalTemplate template = authoringService.getTemplateById(templateId);
        if (EvalConstants.TEMPLATE_TYPE_ADDED.equals( template.getType() )) {
            // TODO change the level and node based on current settings
            hierarchyLevel = EvalConstants.HIERARCHY_LEVEL_INSTRUCTOR;
            hierarchyNodeId = currentUserId;
        }

        for (Iterator<String> iter = selectedIds.keySet().iterator(); iter.hasNext(); ) {
            Long itemId = new Long(iter.next());
            EvalItem item = authoringService.getItemById(itemId);
            if (item == null) {
                LOG.error("Invalid item id: " + itemId);
                continue;
            }
            LOG.debug("Checking to add item:" + itemId);
            if (Objects.equals( selectedIds.get(itemId.toString()), Boolean.TRUE )) {
                // make the template item based on the item and default settings
                EvalTemplateItem templateItem = TemplateItemUtils.makeTemplateItem(item);
                templateItem.setOwner(currentUserId);
                templateItem.setTemplate(template);
                templateItem.setHierarchyLevel(hierarchyLevel);
                templateItem.setHierarchyNodeId(hierarchyNodeId);
                // save the template item
                authoringService.saveTemplateItem(templateItem, currentUserId);
                LOG.info("Added new item (" + item.getId() + ") to template (" + template.getId() + ") via templateItem (" + templateItem.getId() + ")");
            }
        }

        return "success";
    }

    public String controlExpertItem() {
    	
    	// TODO need checkbox for expert or just assume?
    	
    	String currentUserId = commonLogic.getCurrentUserId();
    	
    	EvalItemGroup eig;
    	if (eigIsNew) {
    		 if ( EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(eigType)) {
    			 eig = new EvalItemGroup(currentUserId, 
    					 eigType, eigTitle, eigDesc, Boolean.TRUE, null, null);
    		 } else {
    			 EvalItemGroup parentIg = authoringService.getItemGroupById(eigParentId);
    			 eig = new EvalItemGroup(currentUserId, 
    					 eigType, eigTitle, eigDesc, Boolean.TRUE, parentIg, null);
    		 }
    	} else {
    		eig = authoringService.getItemGroupById(eigId);
    		eig.setTitle(eigTitle);
    		eig.setDescription(eigDesc);
    	}
    	authoringService.saveItemGroup(eig, currentUserId);
    		 
    		 
    	return "success";
    	
    }
    
    public String removeExpertItem() {

    	authoringService.removeItemGroup(eigId, commonLogic.getCurrentUserId(), Boolean.FALSE);
    	
    	return "success";
    	
    }
}
