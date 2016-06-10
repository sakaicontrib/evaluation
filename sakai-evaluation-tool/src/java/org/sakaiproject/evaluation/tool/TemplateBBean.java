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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.exceptions.BlankRequiredFieldException;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.locators.ItemBeanWBL;
import org.sakaiproject.evaluation.tool.locators.ScaleBeanLocator;
import org.sakaiproject.evaluation.tool.locators.TemplateBeanLocator;
import org.sakaiproject.evaluation.tool.locators.TemplateItemWBL;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * This request-scope bean handles template creation and modification and actions related
 * to templates, template items, and items
 * 
 * @author Will Humphries (whumphri@vt.edu)
 * @author Antranig Basman
 * @author Rui Feng (fengr@vt.edu)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TemplateBBean {

    private static final Log LOG = LogFactory.getLog(TemplateBBean.class);

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private LocalTemplateLogic localTemplateLogic;
    public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
        this.localTemplateLogic = localTemplateLogic;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private TemplateBeanLocator templateBeanLocator;
    public void setTemplateBeanLocator(TemplateBeanLocator templateBeanLocator) {
        this.templateBeanLocator = templateBeanLocator;
    }

    private ItemBeanWBL itemBeanWBL;
    public void setItemBeanWBL(ItemBeanWBL itemBeanWBL) {
        this.itemBeanWBL = itemBeanWBL;
    }

    private TemplateItemWBL templateItemWBL;
    public void setTemplateItemWBL(TemplateItemWBL templateItemWBL) {
        this.templateItemWBL = templateItemWBL;
    }

    private ScaleBeanLocator scaleBeanLocator;
    public void setScaleBeanLocator(ScaleBeanLocator scaleBeanLocator) {
        this.scaleBeanLocator = scaleBeanLocator;
    }

    private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
        this.messages = messages;
    }


    // public pea type variables

    public Long templateId;
    public Long itemId;
    public Long[] itemIds;
    public Long scaleId;

    public Boolean idealColor;
    public Integer originalDisplayOrder;

    public String blockId;
    public String blockTextChoice;
    public String orderedChildIds;
    public String templateItemIds;
    public String templateOwner;
    
    public Long groupItemId;


    // TEMPLATES

    /**
     * If the template is already stored, button will show text "Save" method
     * binding to the "Save" button on template_title_description.html replaces
     * TemplateBean.saveTemplate()
     * @return 
     */
    public String updateTemplateTitleDesc() {
        LOG.debug("update template title/desc");
        try {
            templateBeanLocator.saveAll();
        } catch (BlankRequiredFieldException e) {
            messages.addMessage( new TargettedMessage(e.messageKey, 
                    new Object[] { e.fieldName }, TargettedMessage.SEVERITY_ERROR));
            throw new RuntimeException(e); // should not be needed but it is
        }
        messages.addMessage( new TargettedMessage("modifytemplatetitledesc.success.user.message", 
                new Object[] {}, TargettedMessage.SEVERITY_INFO) );
        return "success";
    }

    /**
     * Make a copy of a template at the users request,
     * templateId must be set
     * @return 
     */
    public String copyTemplate() {
        LOG.debug("make a copy of a template ("+templateId+") at the users request");
        String ownerId = commonLogic.getCurrentUserId();
        Long copiedId = authoringService.copyTemplate(templateId, null, ownerId, false, true);
        messages.addMessage( new TargettedMessage("controltemplates.copy.user.message", 
                new Object[] {templateId, copiedId}, TargettedMessage.SEVERITY_INFO) );
        return "success";
    }

    /**
     * Remove a template and create a user message,
     * templateId must be set
     * @return 
     */
    public String removeTemplate() {
        String ownerId = commonLogic.getCurrentUserId();
        EvalTemplate template = authoringService.getTemplateById(templateId);
        authoringService.deleteTemplate(templateId, ownerId);
        messages.addMessage( new TargettedMessage("controltemplates.remove.user.message", 
                new Object[] {template.getTitle()}, TargettedMessage.SEVERITY_INFO) );
        return "success";
    }
    
    /**
     * Chown a template and create a user message,
     * templateId must be set
     * @return 
     */
    public String chownTemplate() {
        String ownerId = commonLogic.getCurrentUserId();
        String userId = commonLogic.getUserId(templateOwner);
        if (null == userId) {
        	return "failed";
        }
        
        EvalTemplate template = authoringService.getTemplateById(templateId);

        if (!authoringService.canCreateTemplate(userId) ||
        		!authoringService.canModifyTemplate(ownerId, templateId)) {
        	messages.addMessage( new TargettedMessage("controltemplates.chown.permission.message", 
        			new Object[] {template.getTitle(), templateOwner}, TargettedMessage.SEVERITY_ERROR) );
        	return "failed";
        }
        
        authoringService.copyTemplate(templateId, 
        		template.getTitle()+ " (shared)", userId, false, true);
        authoringService.deleteTemplate(templateId, ownerId);

        messages.addMessage( new TargettedMessage("controltemplates.chown.user.message", 
                new Object[] {template.getTitle(), templateOwner}, TargettedMessage.SEVERITY_INFO) );
        return "success";
    }

    // ITEMS

    public String saveItemAction() {
        LOG.info("save item");
        try {
            itemBeanWBL.saveAll();
        } catch (BlankRequiredFieldException e) {
            messages.addMessage( new TargettedMessage(e.messageKey, 
                    new Object[] { e.fieldName }, TargettedMessage.SEVERITY_ERROR));
            throw new RuntimeException(e); // should not be needed but it is
        }
        return "success";
    }

    /**
     * Make a copy of an item at the users request,
     * itemId must be set
     * @return 
     */
    public String copyItem() {
        LOG.debug("make a copy of an item ("+itemId+") at the users request");
        String ownerId = commonLogic.getCurrentUserId();
        Long[] copiedIds = authoringService.copyItems(new Long[] {itemId}, ownerId, false, false);
        messages.addMessage( new TargettedMessage("controlitems.copy.user.message", 
                new Object[] {itemId, copiedIds[0]}, TargettedMessage.SEVERITY_INFO) );
        return "success";
    }


    /**
     * Make a copy of a template item at the users request,
     * itemId must be set
     * @return 
     */
    public String copyTemplateItems() {
        LOG.debug("make a copy of a template item ("+itemIds+") at the users request");
        String ownerId = commonLogic.getCurrentUserId();
        Long[] copiedIds = authoringService.copyTemplateItems(itemIds, ownerId, false, templateId, true);
        messages.addMessage( new TargettedMessage("modifytemplate.copy.user.message", 
                new Object[] {}, TargettedMessage.SEVERITY_INFO) );
        LOG.debug("Copied: " + copiedIds[0].toString());
        return "success";
    }

    /**
     * hide the item (instead of removing it)
     * @return 
     */
    public String hideItemAction() {
        LOG.debug("hide item");
        localTemplateLogic.hideItem(itemId);
        messages.addMessage( new TargettedMessage("removeitem.removed.user.message", 
                new Object[] {itemId}, TargettedMessage.SEVERITY_INFO) );
        return "success";
    }

    /**
     * remove the item (does perm check)
     * @return 
     */
    public String deleteItemAction() {
        LOG.debug("delete item");
        localTemplateLogic.deleteItem(itemId);
        messages.addMessage( new TargettedMessage("removeitem.removed.user.message", 
                new Object[] {itemId}, TargettedMessage.SEVERITY_INFO) );
        return "success";
    }


    // TEMPLATE ITEMS

    public String saveTemplateItemAction() {
        LOG.debug("save template item");
        try {
            templateItemWBL.saveAll();
        } catch (BlankRequiredFieldException e) {
            messages.addMessage( new TargettedMessage(e.messageKey, 
                    new Object[] { e.fieldName }, TargettedMessage.SEVERITY_ERROR));
            throw new RuntimeException(e); // should not be needed but it is
        }
        return "success";
    }


    public String saveBothAction() {
        LOG.info("save template item and item");
        try {
            return templateItemWBL.saveBoth();  //returns the item's Id
        } catch (BlankRequiredFieldException e) {
            messages.addMessage( new TargettedMessage(e.messageKey, 
                    new Object[] { e.fieldName }, TargettedMessage.SEVERITY_ERROR));
            throw new RuntimeException(e); // should not be needed but it is
        }
    }


    // SCALES

    /**
     * Saves a scale (does a check of perms)
     * @return 
     */
    public String saveScaleAction() {
        LOG.debug("save scale");
        try {
            scaleBeanLocator.saveAll();
        } catch (BlankRequiredFieldException e) {
            messages.addMessage( new TargettedMessage(e.messageKey, 
                    new Object[] { e.fieldName }, TargettedMessage.SEVERITY_ERROR));
            throw new RuntimeException(e); // should not be needed but it is
        }
        return "success";
    }

    /**
     * Deletes a scale (does a check of perms)
     * @return 
     */
    public String deleteScaleAction() {
        LOG.debug("delete scale");
        scaleBeanLocator.deleteScale(scaleId);
        messages.addMessage( new TargettedMessage("removescale.removed.user.message", 
                new Object[] {scaleId}, TargettedMessage.SEVERITY_INFO) );
        return "success";
    }

    /**
     * Hides a scale instead of removing it (does a check of perms)
     * @return 
     */
    public String hideScaleAction() {
        LOG.debug("hide scale");
        localTemplateLogic.hideScale(scaleId);
        messages.addMessage( new TargettedMessage("removescale.removed.user.message", 
                new Object[] {scaleId}, TargettedMessage.SEVERITY_INFO) );
        return "success";
    }

    /**
     * Make a copy of a scale at the users request,
     * scaleId must be set
     * @return 
     */
    public String copyScale() {
        LOG.debug("make a copy of a scale ("+scaleId+") at the users request");
        String ownerId = commonLogic.getCurrentUserId();
        Long[] copiedIds = authoringService.copyScales(new Long[] {scaleId}, null, ownerId, false);
        messages.addMessage( new TargettedMessage("controlscales.copy.user.message", 
                new Object[] {scaleId, copiedIds[0]}, TargettedMessage.SEVERITY_INFO) );
        return "success";
    }


    // BLOCKS and REORDERING

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
        LOG.info("save items reordering");
        Map<String, EvalTemplateItem> delivered = templateItemWBL.getDeliveredBeans();
        List<EvalTemplateItem> l = authoringService.getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {});
        List<EvalTemplateItem> ordered = TemplateItemUtils.getNonChildItems(l);
        for (int i = 1; i <= ordered.size();) {
            EvalTemplateItem item = (EvalTemplateItem) ordered.get(i - 1);
            int itnum = item.getDisplayOrder();
            if (i < ordered.size()) {
                EvalTemplateItem next = (EvalTemplateItem) ordered.get(i);
                int nextnum = next.getDisplayOrder();
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
        // this will seem a little odd but we are saving the template to validate the order of all templateItems
        localTemplateLogic.saveTemplate(localTemplateLogic.fetchTemplate(templateId));
    }

    private void emit(EvalTemplateItem toemit, int outindex) {
        LOG.debug("EvalTemplateItem toemit: " + toemit.getId() + ", outindex: " + outindex);
        toemit.setDisplayOrder(outindex);
        localTemplateLogic.saveTemplateItem(toemit);
    }


    /**
     * Action to save a block type item,
     * this is stupidly complex now and entails changing the order of items in the template,
     * copying various settings around and fixing up child items to inherit from the parent items<br/>
     * This logic is getting complex enough that it probably needs to be broken out at some point
     * 
     * @return 'success' if all is ok
     */
    public String saveBlockItemAction() {
        LOG.debug("Save Block items");

        Map<String, EvalTemplateItem> delivered = templateItemWBL.getDeliveredBeans();
        
        // Note: Arrays.asList() produces lists that do not support add() or remove(), however set() is supported
        // We may want to change this to ArrayList. (i.e. new ArrayList(Arrays.asList(...)))
        List<String> orderedChildIdList = Arrays.asList(orderedChildIds.split(","));
        List<String> templateItemIdList = Arrays.asList(templateItemIds.split(","));
        
        /*
         * UMD Specific
         * EVALSYS-850
         */
        int adminCt=0;
   	 	int studentCt=0;

        for (String itemID : templateItemIdList) {
            EvalTemplateItem templateItem = authoringService.getTemplateItemById(Long.valueOf(itemID));        	
        	 if (templateItem.getResultsSharing().equals(EvalConstants.SHARING_ADMIN)) {
        		 adminCt++;
        	 } else if (templateItem.getResultsSharing().equals(EvalConstants.SHARING_STUDENT)) {
        		 studentCt++;
        	 }
        }
        LOG.info("admin cnt: " + adminCt);
        LOG.info("student cnt: " + studentCt);
         
        
        if (blockId.equals(TemplateItemWBL.NEW_1)) { // create new block
            EvalTemplateItem parent = (EvalTemplateItem) delivered.get(TemplateItemWBL.NEW_1);
            if (parent != null) {
                EvalTemplateItem templateItem;
                if (blockTextChoice != null && 
                        (! blockTextChoice.equals(TemplateItemWBL.NEW_1)) ) {
                    templateItem = authoringService.getTemplateItemById(Long.valueOf(blockTextChoice));
                    parent.getItem().setItemText(templateItem.getItem().getItemText());
                } else {
                    templateItem = authoringService.getTemplateItemById(Long.valueOf(orderedChildIdList.get(0)));
                }

                parent.setTemplate(templateItem.getTemplate());
                parent.getItem().setScale(templateItem.getItem().getScale());
                parent.getItem().setScaleDisplaySetting(parent.getScaleDisplaySetting());

                parent.setBlockParent(Boolean.TRUE);
                parent.getItem().setClassification(EvalConstants.ITEM_TYPE_BLOCK_PARENT);
                parent.getItem().setSharing(parent.getTemplate().getSharing());
                parent.getItem().setUsesNA(parent.getUsesNA());
                parent.getItem().setCategory(parent.getCategory());
            	parent.getItem().setScaleDisplaySetting(parent.getScaleDisplaySetting());
                
                /*
                 * UMD specific
                 * EVALSYS-850
                 */
                if ((adminCt > 0) && (studentCt > 0)) {
                	parent.setResultsSharing(EvalConstants.SHARING_BOTH);
                } else if ((adminCt > 0) && (studentCt == 0)) {
                	parent.setResultsSharing(EvalConstants.SHARING_ADMIN);
                } else if ((adminCt == 0) && (studentCt > 0)) {
                	parent.setResultsSharing(EvalConstants.SHARING_STUDENT);
                }

                try {
                    localTemplateLogic.saveItem(parent.getItem());
                    localTemplateLogic.saveTemplateItem(parent);
                } catch (BlankRequiredFieldException e) {
                    messages.addMessage( new TargettedMessage(e.messageKey, 
                            new Object[] { e.fieldName }, TargettedMessage.SEVERITY_ERROR));
                    throw new RuntimeException(e); // should not be needed but it is
                }
            } else {
                parent = authoringService.getTemplateItemById(Long.valueOf(blockTextChoice));
            }

            // set parent's display order to the original display order of the first selected template item
            parent.setDisplayOrder(originalDisplayOrder);

            List<EvalTemplateItem> allTemplateItems = authoringService.getTemplateItemsForTemplate(parent.getTemplate().getId(), null, null, null);
            Long parentId = parent.getId();
            List<EvalTemplateItem> blockChildren;

            // if parent is in templateItemIdList (i.e. it is not a newly created item), then put it at the front of the list
            // so that it doesn't have to iterate through all the children that will be added to it (assuming it is late in the late in the list)

            if (parentId.toString().equals(blockTextChoice)) {
                templateItemIdList.set(templateItemIdList.indexOf(parentId.toString()), templateItemIdList.get(0));
                templateItemIdList.set(0, parentId.toString());
            }

            // iterate through templateItemIdList and add all non-block items (including the children of block items in templateItemIdList) 
            for (String itemID : templateItemIdList) {
                EvalTemplateItem templateItem = authoringService.getTemplateItemById(Long.valueOf(itemID));
                if (TemplateItemUtils.getTemplateItemType(templateItem).equals(EvalConstants.ITEM_TYPE_BLOCK_PARENT)) {
                    // this is a block (the parent) being combined into the new block
                    blockChildren = TemplateItemUtils.getChildItems(allTemplateItems, templateItem.getId());
                    for (EvalTemplateItem child : blockChildren) {
                        child.setBlockId(parentId);
                        child.setDisplayOrder(orderedChildIdList.indexOf(child.getId().toString()) + 1);
                        localTemplateLogic.saveTemplateItem(child);
                    }

                    // delete remaining block parent if it is not the parent (only applicable if parent is not newly created)
                    if (parent != templateItem) {
                        localTemplateLogic.deleteTemplateItem(templateItem.getId());
                    }

                } else {
                    // this is a child item in the new block
                    templateItem.setBlockParent(Boolean.FALSE);
                    templateItem.setBlockId(parentId);
                    templateItem.setDisplayOrder(orderedChildIdList.indexOf(itemID) + 1);
                    templateItem.setCategory(parent.getCategory()); // set the child category to the parent category EVALSYS-441
                    templateItem.setUsesNA(parent.getUsesNA()); // child inherits parent NA setting EVALSYS-549
                    // children have to inherit the parent hierarchy settings
                    templateItem.setHierarchyLevel(parent.getHierarchyLevel());
                    templateItem.setHierarchyNodeId(parent.getHierarchyNodeId());
                    localTemplateLogic.saveTemplateItem(templateItem);
                }

            } // end for

            // shifting the order of the other items in the template
            allTemplateItems = authoringService.getTemplateItemsForTemplate(parent.getTemplate().getId(), null, null, null);
            List<EvalTemplateItem> nonChildList = TemplateItemUtils.getNonChildItems(allTemplateItems);

            if ((originalDisplayOrder < nonChildList.size()) && 
                    (Objects.equals( nonChildList.get(originalDisplayOrder).getId(), parentId ))) {
                nonChildList.remove(originalDisplayOrder.intValue());
                nonChildList.add(originalDisplayOrder - 1, parent);
            }

            for (int i = originalDisplayOrder; i < nonChildList.size(); i++) {
                EvalTemplateItem templateItem = nonChildList.get(i);
                if (templateItem.getDisplayOrder() != (i + 1)) {
                    templateItem.setDisplayOrder(i + 1);
                    localTemplateLogic.saveTemplateItem(templateItem);
                }
            }

        } else { // modify block
            // update the parent
            EvalTemplateItem parent = authoringService.getTemplateItemById(Long.valueOf(blockId));
            parent.getItem().setScaleDisplaySetting(parent.getScaleDisplaySetting());
            try {
                localTemplateLogic.saveItem(parent.getItem());
                localTemplateLogic.saveTemplateItem(parent);
            } catch (BlankRequiredFieldException e) {
                messages.addMessage( new TargettedMessage(e.messageKey, 
                        new Object[] { e.fieldName }, TargettedMessage.SEVERITY_ERROR));
                throw new RuntimeException(e); // should not be needed but it is
            }

            // update the children
            List<EvalTemplateItem> blockChildren = authoringService.getBlockChildTemplateItemsForBlockParent(parent.getId(), false);
            for (EvalTemplateItem child : blockChildren) {
                child.setDisplayOrder(orderedChildIdList.indexOf(child.getId().toString()) + 1);
                child.setCategory(parent.getCategory()); // EVALSYS-441
                child.setUsesNA(parent.getUsesNA()); // child inherits parent NA setting EVALSYS-549
                // children have to inherit the parent hierarchy settings
                child.setHierarchyLevel(parent.getHierarchyLevel());
                child.setHierarchyNodeId(parent.getHierarchyNodeId());
                localTemplateLogic.saveTemplateItem(child);
            }
            return parent.getId().toString();
        }
        return "success";
    }
    
    public String saveTemplateItemToGroupAction(){
    	LOG.debug("save template item to group");
    	if(groupItemId != null){
	        try {
	            templateItemWBL.saveToGroup(groupItemId);
	        } catch (BlankRequiredFieldException e) {
	            messages.addMessage( new TargettedMessage(e.messageKey, 
	                    new Object[] { e.fieldName }, TargettedMessage.SEVERITY_ERROR));
	            throw new RuntimeException(e); // should not be needed but it is
	        }
    	}else{
    		throw new RuntimeException(); // should not be needed but it is
    	}
    	return "success";
    }

}