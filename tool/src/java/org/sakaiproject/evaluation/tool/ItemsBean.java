/******************************************************************************
 * ItemsBean.java - created by whumphri@vt.edu on Jan 16, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Will Humphries (whumphri@vt.edu)
 * Rui Feng (fengr@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalScalesLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.producers.ModifyEssayProducer;
import org.sakaiproject.evaluation.tool.producers.ModifyHeaderProducer;
import org.sakaiproject.evaluation.tool.producers.TemplateItemProducer;
import org.sakaiproject.evaluation.tool.producers.TemplateModifyProducer;

/**
 * This request-scope bean handles item creation and modification.
 * 
 * @author Will Humphries (whumphri@vt.edu)
 * @author Rui Feng (fengr@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 */

public class ItemsBean {
	
	/*
	 * VARIABLE DECLARATIONS 
	 */
	private static Log log = LogFactory.getLog(TemplateBean.class);

	/** 
	 * Point to the value of dropdown list (Scale/Survey,Question Block,TextHeader,etc) 
	 * on template_modify.html and also point to the top label field on tempalte_item.html
	 */

	public EvalTemplateItem templateItem;
	
	//The following fields below belong to template_item.html
	

	
	/** "Item Text" text area */
	public Long scaleId;
	private List scaleValues; 			//"Scale Type" drop down list
	private List scaleLabels; 			//"Scale Type" drop down list 
	
	//	The following fields below belong to modify_block.html
	public Boolean idealColor;
	public List queList;
	public String currQueNo;
	
	public String currRowNo;	
	
	public String classification;
	public Long templateId;
	
	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}
	
	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic( EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}
	
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
	
	private EvalScalesLogic scalesLogic;


	
	public void setScalesLogic(EvalScalesLogic scalesLogic) {
		this.scalesLogic = scalesLogic;
	}
	
	public ItemsBean() {
		templateItem = new EvalTemplateItem();
		templateItem.setItem(new EvalItem());
		templateItem.setTemplate(new EvalTemplate());
	}

	/*
	 * INITIALIZATION
	 */
	public void init() {
		if (itemsLogic == null || external==null || scalesLogic==null) {
			throw new NullPointerException("logic is null");
		}
	}
	
	/*
	 * SETTER AND GETTER DEFINITIONS
	 */
	
	public List getScaleValues() {
		//get scale values and labels from DAO
		List lst = null;
		String scaleOptionsStr = "";
		this.scaleValues = new ArrayList();
		this.scaleLabels = new ArrayList();
		
		lst = scalesLogic.getScalesForUser(external.getCurrentUserId(), null);//logic.getScales(Boolean.FALSE);	
		for (int count=0; count < lst.size(); count++) {					
			scaleOptionsStr = "";
			String[] scaleOptionsArr = ((EvalScale)lst.get(count)).getOptions();
			for (int innerCount = 0; innerCount < scaleOptionsArr.length; innerCount++) {

				if (scaleOptionsStr == "")
					scaleOptionsStr = scaleOptionsArr[innerCount];
				else
					scaleOptionsStr = scaleOptionsStr + ", " + scaleOptionsArr[innerCount];
				
			}//end of inner for
			
			EvalScale sl=(EvalScale)lst.get(count);					
			this.scaleValues.add((sl.getId()).toString());			
			this.scaleLabels.add(scaleOptionsArr.length + 
		             			" pt - " + 
		             			sl.getTitle() + 
		             			" ("  + scaleOptionsStr + ")");			
			
		} //end of outer loop
		return scaleValues;
	}

	public void setScaleValues(List scaleValues) {
		this.scaleValues = scaleValues;
	}
	
	public List getScaleLabels() {
		/* 
		 * make sure if getScaleValue() was called first, getScaleLabels() 
		 * will not be called again  
		 * 
		 * */
		if (scaleLabels == null)
			 getScaleValues();
			
		return scaleLabels;
	}

	public void setScaleLabels(List scaleLabels) {
		this.scaleLabels = scaleLabels;
	}
	
	//method binding to the "Add" button on template_modify.html
	public String addItemAction(){
		if(templateItem.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)){
			//"Short Answer/Essay"
			return "new-item:::"+ModifyEssayProducer.VIEW_ID;
		}else if(templateItem.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_HEADER)){
			//"Text Header"
			return "new-item:::"+ModifyHeaderProducer.VIEW_ID;
		}/*TODO-How do we handle blocks here
		else if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_BLOCK)){
			//"Question Block"
			this.idealColor = Boolean.FALSE;
			this.queList = new ArrayList();
			queList.add("");
			queList.add("");
			queList.add("");
			return ModifyBlockProducer.VIEW_ID;
		}*/else  //for "Scale/Suvey" type
		System.out.println("We're heading to EvalARI");
			return "new-item:::"+TemplateItemProducer.VIEW_ID;
		
	}
	
	public String modifyRowItemAction(){
		System.out.println("we're here");
		templateItem=itemsLogic.getTemplateItemById(templateItem.getId());
		if(templateItem.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)){
			return "mod-item:::"+ModifyEssayProducer.VIEW_ID;
		}
		else if(templateItem.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_HEADER)){
			//"Text Header"
			return "mod-item:::"+ModifyHeaderProducer.VIEW_ID;
		}
		else return "mod-item:::"+TemplateItemProducer.VIEW_ID;
	}
	
	public String cancelItemAction(){return null;}
	public String previewItemAction(){return null;}
	public String saveItemAction(){
		System.out.println("We're in save item action");
		templateItem.getItem().setScaleDisplaySetting(templateItem.getScaleDisplaySetting());
		templateItem.setDisplayOrder(new Integer(itemsLogic.getTemplateItemsForTemplate(templateId, external.getCurrentUserId()).size()));
		templateItem.setTemplate(templatesLogic.getTemplateById(templateId));
		templateItem.getItem().setSharing(templateItem.getTemplate().getSharing());
		templateItem.getItem().setScale(scalesLogic.getScaleById(scaleId));
		itemsLogic.saveItem(templateItem.getItem(), external.getCurrentUserId());
		itemsLogic.saveTemplateItem(templateItem, external.getCurrentUserId());
		return "item-created:::"+TemplateModifyProducer.VIEW_ID;
		}
	
	public void newItemInit(Long templateId, String classification) {
		templateItem.setDisplayOrder(new Integer(itemsLogic.getTemplateItemsForTemplate(templateId, external.getCurrentUserId()).size()));
		templateItem.getItem().setClassification(classification);
		this.templateId=templateId;
	}

	public void fetchTemplateItem(Long templateItemId) {
		templateItem=itemsLogic.getTemplateItemById(templateItemId);
		
	}
	
}