/******************************************************************************
 * TemplateBBean.java - created by whumphri@vt.edu on Jan 16, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Antranig Basman
 * Rui Feng
 * Will Humphries (whumphri@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.utils.ItemBlockUtils;

/**
 * This request-scope bean handles template creation and modification.
 * 
 * @author Antranig Basman
 * @author Rui Feng
 * @author will Humphries (whumphri@vt.edu)
 */

public class TemplateBBean {

	private static Log log = LogFactory.getLog(TemplateBean.class);

	private TemplateBeanLocator templateBeanLocator;
	public void setTemplateBeanLocator(TemplateBeanLocator templateBeanLocator) {
		this.templateBeanLocator = templateBeanLocator;
	}

	private TemplateItemBeanLocator templateItemBeanLocator;
	public void setTemplateItemBeanLocator(
			TemplateItemBeanLocator templateItemBeanLocator) {
		this.templateItemBeanLocator = templateItemBeanLocator;
	}

	private LocalTemplateLogic localTemplateLogic;
	public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
		this.localTemplateLogic = localTemplateLogic;
	}
	
	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}
	
	/*
	private EvalScalesLogic scalesLogic;
	public void setScalesLogic(EvalScalesLogic scalesLogic) {
		this.scalesLogic = scalesLogic;
	}*/
	
	public Long templateId;
	public Boolean idealColor;
	public Long scaleId;
	public Integer originalDisplayOrder;
	public String childTemplateItemIds;
	
	/**
	 * If the template is not saved, button will show text "continue and add
	 * question" method binding to the "continue and add question" button on
	 * template_title_description.html replaces TemplateBean.createTemplateAction,
	 * but template is added to db here.
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

	private void emit(EvalTemplateItem toemit, int outindex) {
		log.debug("EvalTemplateItem toemit: " + toemit.getId() + ", outindex: " + outindex);
		toemit.setDisplayOrder(new Integer(outindex));
		localTemplateLogic.saveTemplateItem(toemit);
	}

	/**
	 * NB - this implementation depends on Hibernate reference equality semantics!!
	 * Guarantees output sequence is consecutive without duplicates, and will
	 * prefer honoring user sequence requests so long as they are not inconsistent.
	 */
	public void saveReorder() {
		log.debug("save items reordering");
		Map delivered = templateItemBeanLocator.getDeliveredBeans();
		List ordered = localTemplateLogic.fetchTemplateItems(templateId);
		for (int i = 1; i <= ordered.size();) {
			EvalTemplateItem item = (EvalTemplateItem) ordered.get(i - 1);
			int itnum = item.getDisplayOrder().intValue();
			if (i < ordered.size()) {
				EvalTemplateItem next = (EvalTemplateItem) ordered.get(i);
				int nextnum = next.getDisplayOrder().intValue();
				if (itnum == nextnum) {
					if (delivered.containsValue(item) ^ (itnum == i)) {
						emit(next, i++); emit(item, i++); continue;
					}
					else {
						emit(item, i++); emit(next, i++); continue;
					}
				}
			}
			emit(item, i++);
		}
	}

	public String saveBlockItemAction(){
		log.debug("Save Block items");		
		//get the first child' Scale ID, TemplateId, displayOrder
		String[] strIds = childTemplateItemIds.split(",");
	
		
		Map delivered = templateItemBeanLocator.getDeliveredBeans();
		if(strIds.length >1){//creating new Block
			//save Block parent
			EvalTemplateItem first = itemsLogic.getTemplateItemById(Long.valueOf(strIds[0]));
			Integer originalDO = first.getDisplayOrder();
			EvalTemplate template = first.getTemplate();
			
			EvalTemplateItem parent = (EvalTemplateItem)delivered.get(TemplateItemBeanLocator.NEW_1);		
			
			parent.setTemplate(template);	
			//System.out.println("orginal display order="+first.getDisplayOrder());		
			parent.setDisplayOrder(originalDO);
			parent.getItem().setScale(first.getItem().getScale());
			
			parent.setBlockParent(Boolean.TRUE);
			parent.getItem().setClassification(EvalConstants.ITEM_TYPE_SCALED);
			parent.getItem().setSharing(parent.getTemplate().getSharing());
			if(idealColor != null && idealColor == Boolean.TRUE){
				parent.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED);
				parent.getItem().setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED);
			}else{ 
				parent.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED);
				parent.getItem().setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED);
			}

			localTemplateLogic.saveItem(parent.getItem());
			localTemplateLogic.saveTemplateItem(parent);
			
			//Save Block Child
			System.out.println("parentId="+ parent.getId());
			Integer parentId = new Integer(parent.getId().intValue());
			for(int i=0; i<strIds.length; i++){
				EvalTemplateItem child = itemsLogic.getTemplateItemById(Long.valueOf(strIds[i]));
				if(child.getBlockParent() != Boolean.FALSE) child.setBlockParent(Boolean.FALSE);
				child.setDisplayOrder(new Integer(i+1));
				child.setBlockId(parentId);
			//should child scaledisplaySetting, useNA, itemCategory been reset?-check with Aaron
			}
			
			//shifting all the others's order
			List allTemplateItems = itemsLogic.getTemplateItemsForTemplate(template.getId(), null);
			List noChildList = ItemBlockUtils.getNonChildItems(allTemplateItems);
			for(int i=0; i<noChildList.size();i++){
				EvalTemplateItem  eti =(EvalTemplateItem)noChildList.get(i);
				if(eti.getDisplayOrder().intValue() != (i+1)){
					eti.setDisplayOrder(new Integer(i+1));
					localTemplateLogic.saveTemplateItem(eti);
				}
			}
						
		}else {//modify existing Block
			EvalTemplateItem parent = (EvalTemplateItem)delivered.get(strIds[0]);	
			if(parent!= null){
				if(idealColor != null){//only reset when this feild is changed
					if(idealColor == Boolean.TRUE){
						parent.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED);
						parent.getItem().setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED);
					}else{
						parent.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED);
						parent.getItem().setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED);
					}
				}
				localTemplateLogic.saveItem(parent.getItem());
				localTemplateLogic.saveTemplateItem(parent);	
			}
		}
	
		return "success";	
	}
}