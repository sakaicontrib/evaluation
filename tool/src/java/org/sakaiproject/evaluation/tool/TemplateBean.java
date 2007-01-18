/******************************************************************************
 * TemplateBean.java - created by fengr@vt.edu on Aug 21, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalScalesLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.logic.EvaluationLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.producers.ModifyBlockProducer;
import org.sakaiproject.evaluation.tool.producers.ModifyEssayProducer;
import org.sakaiproject.evaluation.tool.producers.ModifyHeaderProducer;
import org.sakaiproject.evaluation.tool.producers.PreviewEvalProducer;
import org.sakaiproject.evaluation.tool.producers.PreviewItemProducer;
import org.sakaiproject.evaluation.tool.producers.RemoveQuestionProducer;
import org.sakaiproject.evaluation.tool.producers.TemplateItemProducer;
import org.sakaiproject.evaluation.tool.producers.TemplateModifyProducer;
import org.sakaiproject.evaluation.tool.producers.TemplateProducer;


/**
 * This is the backing bean  of the evaluation system
 * 
 * @author Rui Feng (fengr@vt.edu),Kapil Ahuja (kahuja@vt.edu)
 */

public class TemplateBean {
	
	/*
	 * VARIABLE DECLARATIONS 
	 */
	private static Log log = LogFactory.getLog(TemplateBean.class);
	
	//The following fields below belong to template_title_desc.html
	public String title;
	public String description;
	public String modifier;
	
	/* 
	 * Point to the value of dropdown list (Scale/Survey,Question Block,TextHeader,etc) 
	 * on template_modify.html and also point to the top label field on tempalte_item.html
	 */
	public String itemClassification;
	
	//The following fields below belong to template_item.html
	public String itemText; 			//"Item Text" text area 
	private List scaleValues; 			//"Scale Type" drop down list
	private List scaleLabels; 			//"Scale Type" drop down list 
	public Long scaleId; 				//the actual value of the selected "Scale Type" dropdown list
	public String scaleDisplaySetting;	//the actual value of the selected "Scale Display Setting" dropdown list
	public Boolean itemNA; //the boolean value of "Add N/A (not available)" check box
	//selected radio button  value, by default the "Course" radio button is selected	
	public String itemCategory;
	public int currItemNo;				//the order number of the item. Also used in preview_item.html
										//there is a getter corresponding to a userId but no variable or setter

	//The following fields below belong to modify_essay.html
	public Integer displayRows;
	
	//	The following fields below belong to modify_block.html
	public Boolean idealColor;
	public List queList;
	public String currQueNo;
	
	/*
	 * TODO: TO BE REMOVED
	// The following variables used in TemplateModifierProducer.java
	public List itemDisplayList;
	private int itemDisplayListSize;
	*/
	public List itemsList; //to replace itemDisplayList which contain deprectaed ItemDisplay object
	private int itemsListSize; 
	
	public String currRowNo;			

	/*TODO: TO BE REMOVED

	public ItemDisplay itemDisplayPreview;
	*/
	
	//The following variables used in PreviewItemProducer.java
	public EvalItem itemPreview; //to replace itemDisplayPreview to remove deprectaed ItemDisplay object
	public EvalItem currentItem;

	public Long templateId;

	//The following fields only used in TemplateBean.java (that is this file only)
	private EvaluationLogic logic;
	
	public void setLogic(EvaluationLogic logic) {
		this.logic = logic;
	}
	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic( EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}
	
	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}
	
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
	
	private EvalScalesLogic scalesLogic;
	public void setScalesLogic(EvalScalesLogic scalesLogic) {
		this.scalesLogic = scalesLogic;
	}
	
	private EvalTemplate currTemplate;
	
	public EvalTemplate getCurrTemplate() {
		return currTemplate;
	}

	/*
	 * INITIALIZATION
	 */
	public void init() {
		if (logic == null) {
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
/*
	public int getItemDisplayListSize() {
		
		if(this.itemDisplayList == null) 
			this.itemDisplayListSize=0;
		else 
			this.itemDisplayListSize = this.itemDisplayList.size();
		
		return this.itemDisplayListSize;
	}
	*/
	/*No setItemDisplayListSize method needed*/
	
	public int getItemsListSize() {
		
		if(this.itemsList == null) 
			this.itemsListSize=0;
		else 
			this.itemsListSize = this.itemsList.size();
		
		return this.itemsListSize;
	}
	
	
	
	/*
	 * MAJOR METHOD DEFINITIONS
	 */

	public String modifyExistingTemplate() {
		
		currTemplate = templatesLogic.getTemplateById(templateId);//logic.getTemplateById(templateId);
		
		// making this template information current in the template bean
	//	itemDisplayList = new ArrayList(); //TODO: TO BE REMOVED
		itemsList = new ArrayList();
		
		Set items = new TreeSet(new PreviewEvalProducer.EvaluationItemOrderComparator());
		items.addAll(currTemplate.getItems());
		//System.out.println("template items size="+currTemplate.getItems().size());
		for (Iterator it = items.iterator(); it.hasNext();) {
			EvalItem evalItem = (EvalItem) it.next();
		//	ItemDisplay id = new ItemDisplay(evalItem);
		//	itemDisplayList.add(id);
			
			itemsList.add(evalItem);
			System.out.println("NO items in the itemsList="+itemsList.size());
		}
		
		//itemDisplayListSize = itemDisplayList.size();
		itemsListSize = itemsList.size();
		
		title = currTemplate.getTitle();
		description = currTemplate.getDescription();
		modifier = currTemplate.getSharing();
		currentItem = null;
		
		return TemplateModifyProducer.VIEW_ID;
	}

	
	/* 
	 * 1)this method is binding to Control Panel page: "Create new Template" command 
	 * 	button(link look)
	 * 2) this method is also binding to Summary page: "Create Template" Link
	 */
	public String createNewTemplate(){
		
		clearTemplate();
		
		return TemplateProducer.VIEW_ID;	
	}
	
	
	/*
	 * If the template is not saved, button will show text "continue and add question"
	 * method binding to the "continue and add question" button on template_title_description.html
	 * */
	public String createTemplateAction() {
		//log.warn("Creating template");
		//creating a template
		currTemplate = new EvalTemplate(new Date(), external.getCurrentUserId(),
				title, modifier, Boolean.FALSE);
		currTemplate.setDescription(description);
		currTemplate.setLocked(Boolean.FALSE);
/*
 * 		if (itemDisplayList == null)
			itemDisplayList = new ArrayList();
		else itemDisplayList.clear();
*/		
		if (itemsList == null)
			itemsList = new ArrayList();
		else itemsList.clear();
			
	    return TemplateModifyProducer.VIEW_ID;
	}
	
	
	/*
	 * If the template is not saved, button will show text "Save"
	 * method binding to the "Save" button on template_title_description.html
	*/
	public String saveTemplate(){   
		//TODO: hibernate bugs
		currTemplate=templatesLogic.getTemplateById(templateId);
		currTemplate.setTitle(title);
		currTemplate.setDescription(description);
		currTemplate.setSharing(modifier);
		templatesLogic.saveTemplate(currTemplate, external.getCurrentUserId());
		//logic.saveTemplate(currTemplate, logic.getCurrentUserId());
		
		return TemplateModifyProducer.VIEW_ID;	
	}
	
	
	//method binding to the "Add" button on template_modify.html
	public String addItemAction(){
				
		//reinitializing the parameters
		this.itemText = "";
		this.scaleId = null;
		this.scaleDisplaySetting = "";
		this.displayRows = null;
		this.itemNA = Boolean.FALSE;

		this.itemCategory = EvalConstants.ITEM_CATEGORY_COURSE;			
		this.currentItem = null;
		this.currItemNo = getItemsListSize() + 1;//this.getItemDisplayListSize() + 1;
		
		/*
		 * depending on the value of selection:"Scaled/Survey","Question Block","Short Answer/Essay",
		 * "Text Header" and "Expert", it will goes to different page
		 * */
		if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_TEXT)){
			//"Short Answer/Essay"
			this.displayRows = EvaluationConstant.DEFAULT_ROWS;
			return ModifyEssayProducer.VIEW_ID;
		}else if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_HEADER)){
			//"Text Header"
			return ModifyHeaderProducer.VIEW_ID;
		}else if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_BLOCK)){
			//"Question Block"
			this.idealColor = Boolean.FALSE;
			this.queList = new ArrayList();
			queList.add("");
			queList.add("");
			queList.add("");
			return ModifyBlockProducer.VIEW_ID;
		}else  //for "Scale/Suvey" type
			return TemplateItemProducer.VIEW_ID;

	}
	

	//method binding to the "Cancel" button on template_item.html 
	public String cancelItemAction(){
		
		return TemplateModifyProducer.VIEW_ID;
	}


	//method binding to the "preview" button on template_item.html
	public String previewItemAction(){
		//log.warn("Preview item");

		//creating an item
		/* TODO: TO BE REMOVED
		EvalItem tempItemForPreview = new EvalItem();		
		updateItemObject(tempItemForPreview);
		//set Item DisplayOrder
		tempItemForPreview.setDisplayOrder(new Integer(this.currItemNo));
		itemDisplayPreview = new ItemDisplay(tempItemForPreview);
		*/
		
		itemPreview = new EvalItem();
		updateItemObject(itemPreview);
		itemPreview.setDisplayOrder(new Integer(this.currItemNo));
		
		return PreviewItemProducer.VIEW_ID;
	}

	//method binding to the "Save" button on template_item.html
	public String saveItemAction(){

		/* 
		 * If: Coming to modify/update an item.
		 * Else: Coming to save/add an item.
		 */
		if(currentItem != null)	{
			//System.out.println("modify exsiting item, item id="+ currentItem.getId().longValue());
			
			currentItem = itemsLogic.getItemById(currentItem.getId());
			updateItemObject(currentItem);
			currentItem.setScaleDisplaySetting(scaleDisplaySetting);
			
			//Update the item in database
			//logic.saveItem(currentItem, logic.getCurrentUserId());
			itemsLogic.saveItem(currentItem, external.getCurrentUserId());
			//Update itemDisplayList (list of current items).
			//itemDisplayList.remove(Integer.parseInt(currRowNo));
			//itemDisplayList.add(Integer.parseInt(currRowNo), new ItemDisplay(currentItem));
			
			itemsList.remove(Integer.parseInt(currRowNo));
			itemsList.add(Integer.parseInt(currRowNo),currentItem);
		}
		else {
			if ( currTemplate.getId() == null ) 
				templatesLogic.saveTemplate(currTemplate, external.getCurrentUserId());
			
			currentItem = new EvalItem();
			updateItemObject(currentItem);
			currentItem.setScaleDisplaySetting(scaleDisplaySetting);
			currentItem.setSharing(modifier);
			currentItem.getTemplates().add(currTemplate);
			
			//set the display order.
			int orderNo = getItemsListSize();//this.getItemDisplayListSize();
			currentItem.setDisplayOrder(new Integer(orderNo + 1)); 
			itemsLogic.saveItem(currentItem, external.getCurrentUserId());
		/*
			if ( currTemplate.getId() == null ) {
				 currTemplate.getItems().add(currentItem);
				logic.batchSaveItemTemplate(currTemplate, currentItem);
				
				
			}
			else {
				itemsLogic.saveItem(currentItem, external.getCurrentUserId());
				//logic.saveItem(currentItem, logic.getCurrentUserId());			
			}
			*/
			
			//itemDisplayList.add(new ItemDisplay(currentItem));
			itemsList.add(currentItem);
		
		}
		
		return TemplateModifyProducer.VIEW_ID;
		
	}
    
	
	/*
	 * method binding to the hidden button with rsf:id="hiddenBtn" on template_modify page
	 * used to change displayOrder of each item when the onChange event of dropdown 
	 * 		(displayOrder #)on template_modify.gtml fired
	 */
	public String changeDisplayOrder(){
		
		//itemDisplayPreview = null;
		itemPreview =null;
		
		int size = getItemsListSize();//getItemDisplayListSize();  
		int[] orderArr = new int[size];	
		int missingOrderIndex = -1;
		int identicalOrderIndex = -1;
		//get the missingOrder index, so we know which dropdown list is changed
		for(int i = 0 ; i< size; i++){
			EvalItem myItem = (EvalItem)itemsList.get(i); //((ItemDisplay)this.itemDisplayList.get(i)).getItem();
			
			orderArr[i] = myItem.getDisplayOrder().intValue();
			if(orderArr[i] != (i+1))
				missingOrderIndex = i;						
		}
		//get the dropdownlist onChange selected value
		for(int j=0; j< size; j++){
			if((orderArr[j] == orderArr[missingOrderIndex] )&& ( j!=missingOrderIndex)){
				identicalOrderIndex = j;
				break;
				}
		}	
		//shift display order
		EvalItem onChangeItem = (EvalItem)itemsList.get(missingOrderIndex);//((ItemDisplay) this.itemDisplayList.get(missingOrderIndex)).getItem();
	
		//logic.saveItem(onChangeItem, logic.getCurrentUserId());
		itemsLogic.saveItem(onChangeItem, external.getCurrentUserId());
		if(identicalOrderIndex < missingOrderIndex){
			for(int k = missingOrderIndex -1; k >=identicalOrderIndex ; k--){
				EvalItem item1 = (EvalItem)itemsList.get(k);//((ItemDisplay)this.itemDisplayList.get(k)).getItem();
				
				item1.setDisplayOrder(new Integer(k+2));
				itemsLogic.saveItem(item1, external.getCurrentUserId());//logic.saveItem(item1, logic.getCurrentUserId());
				//itemDisplayList.set(k+1, new ItemDisplay(item1));
				itemsList.set(k+1, item1);
			}		
		}else{
				for(int k = missingOrderIndex +1;k <= identicalOrderIndex; k++){
					EvalItem item1 = (EvalItem)itemsList.get(k);//((ItemDisplay) itemDisplayList.get(k)).getItem();
				item1.setDisplayOrder(new Integer(k));
				//logic.saveItem(item1, logic.getCurrentUserId());
				itemsLogic.saveItem(item1, external.getCurrentUserId());
				//itemDisplayList.set(k-1, new ItemDisplay(item1));
				itemsList.set(k-1, item1);
			}	
		}
		//itemDisplayList.set(identicalOrderIndex, new ItemDisplay(onChangeItem));
		itemsList.set(identicalOrderIndex,onChangeItem);
		return TemplateModifyProducer.VIEW_ID;
	}
	
	
	//method binding to "preview" link on template_modify.html
	public String previewRowItemAction(){

		//get the rowNo which is ELBinding to the parameters of "preview" link
		int rowNo = -1;
		itemPreview = null;
		
		try{
			rowNo=Integer.parseInt(currRowNo);
		}catch(NumberFormatException fe){	
			log.fatal(fe);
		}
		System.out.println("after parsing:RowNo ="+ rowNo);
		
		if(rowNo == -1) 
			log.error("Error inside previewRowItemAction()");
		else{
			this.currItemNo = rowNo + 1;
			
			itemPreview = (EvalItem) itemsList.get(rowNo);
			this.itemText = itemPreview.getItemText();
			this.itemClassification = itemPreview.getClassification();
			this.itemNA = itemPreview.getUsesNA();
						
			if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_SCALED)){
				//"Scaled/Survey"
				this.scaleId = itemPreview.getScale().getId();
				this.scaleDisplaySetting = itemPreview.getScaleDisplaySetting();
				
			}else if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_BLOCK)){
				//"Question Block"
							this.scaleId = itemPreview.getScale().getId();	
				this.scaleDisplaySetting = itemPreview.getScaleDisplaySetting();
				this.itemNA = itemPreview.getUsesNA();
				
				this.queList = new ArrayList();
					Long parentID = itemPreview.getId();
				Integer blockID = new Integer(parentID.intValue());
				// TODO:
			/*
				List childItems = logic.findItem(blockID);
				if (childItems != null && childItems.size() > 0) {
					this.queList= new ArrayList();
					for (int j = 0; j < childItems.size(); j++) {
						EvalItem child = (EvalItem) childItems.get(j);
						this.queList.add(child.getItemText());
					}
				} 
				*/
			}else if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_TEXT)){
				//"Short Answer/Essay"
				this.displayRows = itemPreview.getDisplayRows();
			}
			
		}	
		
		return PreviewItemProducer.VIEW_ID;		
	}

	
	//method binding to the "modify" link on template_modify.html
	public String modifyRowItemAction(){

		//get the rowNo which is ELBinding to the parameters of "preview" link
		int rowNo = -1;
		
		try{
			rowNo = Integer.parseInt(currRowNo);
		}catch(NumberFormatException fe){	
			log.fatal(fe);
		}
		
		if(rowNo == -1) 
			log.error("Error inside modifyRowItemAction()");
		else currentItem = (EvalItem)itemsList.get(rowNo);	
	
		if(currentItem != null){ 	
			this.currItemNo = rowNo + 1;
	
			itemPreview = (EvalItem) itemsList.get(rowNo);
				
			this.itemText = currentItem.getItemText();
			this.itemClassification = currentItem.getClassification();
		
		    this.itemCategory = currentItem.getCategory();
			
		    this.scaleId = null;
			this.scaleDisplaySetting = "";
	    	this.displayRows = null;
	    	this.itemNA =null;
		    if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_SCALED)){
		    	//"Scaled/Survey"
		    	this.scaleId = currentItem.getScale().getId();
		    	this.scaleDisplaySetting = currentItem.getScaleDisplaySetting();
		    	this.itemNA = currentItem.getUsesNA();	
		    	
		    	return TemplateItemProducer.VIEW_ID;
			}else if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_TEXT)) {
				//"Short Answer/Essay"
				this.displayRows = currentItem.getDisplayRows();				
				this.itemNA = currentItem.getUsesNA();	
				
				return ModifyEssayProducer.VIEW_ID;
			}else if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_HEADER)){
				//"Text Header"
				return ModifyHeaderProducer.VIEW_ID;			
			}else if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_BLOCK)){
				//"Question Block"
				this.scaleId = currentItem.getScale().getId();
		    	this.scaleDisplaySetting = currentItem.getScaleDisplaySetting();
		     	this.itemNA = currentItem.getUsesNA();
		    	
		     	if(this.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED)){
		    		//"Stepped Colored"
		    		this.idealColor = Boolean.TRUE;
		    	}else 		
		    		this.idealColor = Boolean.FALSE;
		    	//get child items form DAO
		    	if(this.queList ==null){
		    		this.queList = new ArrayList();
		    	}else if( this.queList.size() > 0){
		    		this.queList.clear();
		    	}
		    	
		    	Integer blockID = new Integer(currentItem.getId().intValue());	
		    	//TODO:
		    	/*
		    	List childItems = logic.findItem(blockID);
		    	for(int i=0; i< childItems.size(); i++){
		    		EvalItem myItem = (EvalItem)childItems.get(i);
		    		this.queList.add(myItem.getItemText());
		    	}
		    	*/
		    	return ModifyBlockProducer.VIEW_ID;
			}
			
		}
		
		return TemplateItemProducer.VIEW_ID;
	}

	
	//method binding to the "Remove" link on template_modify.html
	public String removeRowItemAction(){

		//	get the rowNo which is ELBinding to the parameters of "preview" link
		int rowNo = -1;
		//itemDisplayPreview = null;
		itemPreview = null;
		
		try{
			rowNo=Integer.parseInt(currRowNo);
		}catch(NumberFormatException fe){	
			log.fatal(fe);
		}

		//if (itemDisplayList == null) {
		if (itemsList == null) {
			//log.error("itemDisplayList null");
			log.error("itemsList null");
		} else {
			//log.error("itemDisplayList not null");
			log.error("itemsList not null");
		}
		log.warn("remove item action, rowNo=" + rowNo);

		if(rowNo == -1) 
			log.error("Error inside removeRowItemAction()");
		else{
			this.currItemNo = rowNo + 1;
			
			//itemDisplayPreview=(ItemDisplay)this.itemDisplayList.get(rowNo);
			//this.itemText = itemDisplayPreview.getItem().getItemText();
			//this.itemClassification = itemDisplayPreview.getItem().getClassification();
			itemPreview = (EvalItem) itemsList.get(rowNo);
			itemText = itemPreview.getItemText();
			itemClassification = itemPreview.getClassification();
						
			if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_SCALED)){
				//"Scaled/Survey"
				//this.scaleId = itemDisplayPreview.getItem().getScale().getId();
				//this.scaleDisplaySetting = itemDisplayPreview.getItem().getScaleDisplaySetting();
				this.scaleId = itemPreview.getScale().getId();
				this.scaleDisplaySetting = itemPreview.getScaleDisplaySetting();
				
			}else if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_BLOCK)){
				//block
				//this.scaleId = itemDisplayPreview.getItem().getScale().getId();	
				//this.scaleDisplaySetting = itemDisplayPreview.getItem().getScaleDisplaySetting();
				//this.itemNA = itemDisplayPreview.getItem().getUsesNA();
				scaleId = itemPreview.getScale().getId();	
				scaleDisplaySetting = itemPreview.getScaleDisplaySetting();
				itemNA = itemPreview.getUsesNA();
				this.queList = new ArrayList();
				//Long parentID = itemDisplayPreview.getItem().getId();
				Long parentID = itemPreview.getId();
				Integer blockID = new Integer(parentID.intValue());
				//TODO:
				/*
				List childItems = logic.findItem(blockID);
				if (childItems != null && childItems.size() > 0) {
					this.queList= new ArrayList();
					for (int j = 0; j < childItems.size(); j++) {
						EvalItem child = (EvalItem) childItems.get(j);
						this.queList.add(child.getItemText());
					}
				}
				
				*/
			}

		}	
		
		return RemoveQuestionProducer.VIEW_ID;

	}
	
	//method binding to the "Cancel" button on remove_question.html
	public String cancelRemoveAction(){
		log.warn("cancel remove action");

		//this.itemDisplayPreview = null;
		itemPreview = null;
		
		return TemplateModifyProducer.VIEW_ID;		
	}
	
	
	//	method binding to the "Remove Question" button on remove_question.html
	public String removeQuestionAction(){
	/*
		if(this.itemDisplayPreview != null){
			EvalItem item = itemDisplayPreview.getItem();		
			//remove BLOCK child items
			if(item.getClassification().equals(EvalConstants.ITEM_TYPE_BLOCK)&& item.getBlockParent().booleanValue()==true){
				Integer id = new Integer(item.getId().intValue());
				//TODO: wait for aaron's logic layer method
				List childItems = logic.findItem(id);
				for(int i=0; i< childItems.size();i++){
					EvalItem cItem =(EvalItem) childItems.get(i);
					itemsLogic.deleteItem(cItem.getId(),external.getCurrentUserId());
					//logic.deleteItem(cItem,external.getCurrentUserId());
				}
			}
			itemsLogic.deleteItem(item.getId(), external.getCurrentUserId());
			//logic.deleteItem(item, external.getCurrentUserId());
			itemDisplayList.remove(this.itemDisplayPreview);			

			//update the other item's displayOrder ,save item,re-organize ItemDisplayList
			for(int i = this.currItemNo - 1 ; i< getItemDisplayListSize(); i++){
				EvalItem myItem = ((ItemDisplay)this.itemDisplayList.get(i)).getItem();
				myItem.setDisplayOrder(new Integer(i+1));
				itemsLogic.saveItem(myItem, external.getCurrentUserId());
				itemDisplayList.set(i, new ItemDisplay(myItem));
			}
		
		}
		this.itemDisplayPreview = null;
		*/
		
		if(itemPreview != null){
			//EvalItem item = itemDisplayPreview.getItem();		
			//remove BLOCK child items
			if(itemPreview.getClassification().equals(EvalConstants.ITEM_TYPE_BLOCK)&& itemPreview.getBlockParent().booleanValue()==true){
				Integer id = new Integer(itemPreview.getId().intValue());
				//TODO:
				/*
				List childItems = logic.findItem(id);
				for(int i=0; i< childItems.size();i++){
					EvalItem cItem =(EvalItem) childItems.get(i);
					itemsLogic.deleteItem(cItem.getId(),external.getCurrentUserId());
				}
				*/
			}
			itemsLogic.deleteItem(itemPreview.getId(), external.getCurrentUserId());
			itemsList.remove(itemPreview);			

			//update the other item's displayOrder ,save item,re-organize ItemDisplayList
			for(int i = this.currItemNo - 1 ; i< getItemsListSize(); i++){
				EvalItem myItem = (EvalItem)itemsList.get(i);
				myItem.setDisplayOrder(new Integer(i+1));
				itemsLogic.saveItem(myItem, external.getCurrentUserId());
				itemsList.set(i, myItem);
			}
		
		}
		itemPreview = null;
		return TemplateModifyProducer.VIEW_ID;
	}
	
	//method binding to the "Cancel" button on template_item.html 
	public String cancelEssayAction(){
	
		return TemplateModifyProducer.VIEW_ID;
	}
	
	//method binding to the "Preview" button on modify_essay.html
	public String previewEssayAction(){
	
		//creating an item
/*		EvalItem tempItemForPreview = new EvalItem();		
		this.scaleDisplaySetting = null;
		updateItemObject(tempItemForPreview);
		tempItemForPreview.setDisplayRows(this.displayRows);
		//set Item DisplayOrder
		tempItemForPreview.setDisplayOrder(new Integer(this.currItemNo));
		itemDisplayPreview = new ItemDisplay(tempItemForPreview);
*/
		itemPreview = new EvalItem();
		this.scaleDisplaySetting = null;
		updateItemObject(itemPreview);
		itemPreview.setDisplayRows(this.displayRows);
		//set Item DisplayOrder
		itemPreview.setDisplayOrder(new Integer(this.currItemNo));
		
		return PreviewItemProducer.VIEW_ID;
	}
	
	//	method binding to the "Save" button on modify_essay.html
	public String saveEssayAction(){

		if(currentItem != null)
		{	
			//System.out.println("modify exsiting essay item, item id="+ currentItem.getId().longValue());
			
			currentItem = itemsLogic.getItemById(currentItem.getId());
			updateItemObject(currentItem);
			currentItem.setDisplayRows(this.displayRows);
			//logic.saveItem(currentItem, logic.getCurrentUserId());
			itemsLogic.saveItem(currentItem, external.getCurrentUserId());
			
			//itemDisplayList.remove(Integer.parseInt(currRowNo));
			//itemDisplayList.add(Integer.parseInt(currRowNo), new ItemDisplay(currentItem));
			itemsList.remove(Integer.parseInt(currRowNo));
			itemsList.add(Integer.parseInt(currRowNo), currentItem);
		}
		else {					
			if ( currTemplate.getId() == null )
				templatesLogic.saveTemplate(currTemplate, external.getCurrentUserId());
				//logic.saveTemplate(currTemplate, logic.getCurrentUserId());

			currentItem = new EvalItem();
			this.scaleDisplaySetting = null;
			updateItemObject(currentItem);
			currentItem.setDisplayRows(this.displayRows);
			currentItem.setSharing(modifier);
			currentItem.getTemplates().add(currTemplate);
			
			//Save the item everytime	
			int orderNo = getItemsListSize();//this.getItemDisplayListSize();
			//set the display order for each item
			currentItem.setDisplayOrder(new Integer(orderNo + 1)); 
			//logic.saveItem(currentItem, logic.getCurrentUserId());			
			itemsLogic.saveItem(currentItem, external.getCurrentUserId());
			//itemDisplayList.add(new ItemDisplay(currentItem));
			itemsList.add(currentItem);
			
		}
		
		return TemplateModifyProducer.VIEW_ID;	
	}
	
	
	//method binding to the "Add Item To Block" button on modify_block.html
	public String addItemToBlock(){

		this.queList.add("");
		
		return ModifyBlockProducer.VIEW_ID;
	}
	
	
	//	method binding to the "Remove" button on modify_block.html
	public String removeItemFromBlock(){

		//get the rowNo which is ELBinding to the parameters of "preview" link
		int queNo = -1;	
		try{
			queNo=Integer.parseInt(currQueNo);
		}catch(NumberFormatException fe){	
			log.fatal(fe);
		}
		
		if(queNo == -1) 
			log.error("Error inside removeItemFromBlock()");
		else{ //should leave at least one text box
			if(this.queList.size()> 1)
			   this.queList.remove(queNo); 
		}
		
		return ModifyBlockProducer.VIEW_ID;
	}
	
	
	//method binding to the "Cancel" button on modify_block.html 
	public String cancelBlockAction(){
	
		return TemplateModifyProducer.VIEW_ID;
	}
	
	
	//method binding to the "Save" button on modify_block.html
	public String saveBlockAction(){
	
		if(currentItem != null){	
			
			if(idealColor != null && idealColor.booleanValue() == true)
				scaleDisplaySetting = "Stepped Colored";
			else 
				scaleDisplaySetting = "Stepped";
			
			//first delete all the old child items,
			Integer id = new Integer(currentItem.getId().intValue());
			//TODO: 
			/*
			List childItems = logic.findItem(id);
			for(int i=0; i< childItems.size();i++){
				EvalItem myItem =(EvalItem) childItems.get(i);
				//logic.deleteItem(myItem, logic.getCurrentUserId());
				itemsLogic.deleteItem(myItem.getId(), external.getCurrentUserId());
			}
			*/
			currentItem.setLastModified(new Date());
			currentItem.setItemText(itemText);
			currentItem.setScaleDisplaySetting(scaleDisplaySetting);
			currentItem.setCategory(itemCategory);
			currentItem.setUsesNA(itemNA);
			this.setScale(currentItem);
			//logic.saveItem(currentItem, logic.getCurrentUserId());
			itemsLogic.saveItem(currentItem, external.getCurrentUserId());
			//itemDisplayList.remove(Integer.parseInt(currRowNo));
			//itemDisplayList.add(Integer.parseInt(currRowNo), new ItemDisplay(currentItem));
			itemsList.remove(Integer.parseInt(currRowNo));
			itemsList.add(Integer.parseInt(currRowNo), currentItem);
			
			//update child items; add new items
			for(int j=0; j< this.queList.size(); j++){
				EvalItem tempItem = new EvalItem(new Date(), external.getCurrentUserId(),
						(String)queList.get(j), EvalConstants.SHARING_PRIVATE, 
						itemClassification, Boolean.FALSE);
				tempItem.setDisplayOrder(Integer.valueOf(j+1));
				tempItem.setBlockId(id);
				//logic.saveItem(tempItem, logic.getCurrentUserId());
				itemsLogic.saveItem(tempItem, external.getCurrentUserId());
			}
		}
		else {
			//validate each text box  should not be empty
			for(int j= 0; j < this.queList.size(); j++){
				String txt = (String)this.queList.get(j);
				if(txt== null || txt.length()==0)
					return ModifyBlockProducer.VIEW_ID;
			}
			
			if ( currTemplate.getId() == null ){
				//logic.saveTemplate(currTemplate, logic.getCurrentUserId());
				templatesLogic.saveTemplate(currTemplate, external.getCurrentUserId());
			}
	
			//set the header Item
			currentItem = new EvalItem();
			
			if(idealColor != null && idealColor.booleanValue() == true)
				scaleDisplaySetting = "Stepped Colored";
			else
				scaleDisplaySetting = "Stepped";
			
			updateItemObject(currentItem);
			currentItem.setBlockParent(Boolean.TRUE);
			setScale(currentItem);
			currentItem.getTemplates().add(currTemplate);
			int orderNo = getItemsListSize();//this.getItemDisplayListSize();
			currentItem.setDisplayOrder(new Integer(orderNo + 1)); 
			//logic.saveItem(currentItem, logic.getCurrentUserId());
			
			itemsLogic.saveItem(currentItem, external.getCurrentUserId());
			
			//itemDisplayList.add(new ItemDisplay(currentItem));		
			itemsList.add(currentItem);
			
			Integer parentId = new Integer(currentItem.getId().intValue());
			//save child items
			for(int i= 0; i < this.queList.size(); i++){
				EvalItem tempItem = new EvalItem(new Date(), external.getCurrentUserId(),
						(String)queList.get(i), EvalConstants.SHARING_PRIVATE, 
						itemClassification, Boolean.FALSE);
				tempItem.setDisplayOrder(Integer.valueOf(i+1));
				tempItem.setBlockId(parentId);
				//BLOCK CHILD ITEM need to be assocaite with Template
				tempItem.getTemplates().add(currTemplate);
				//logic.saveItem(tempItem, logic.getCurrentUserId());			
				itemsLogic.saveItem(tempItem, external.getCurrentUserId());
			}//end of for loop
		}
		
		return TemplateModifyProducer.VIEW_ID;
	}
	
	//method binding to the "Preview" button on modify_block.html
	public String previewBlockAction(){

		//	creating an item
/*		EvalItem tempItemForPreview = new EvalItem();
		updateItemObject(tempItemForPreview);
		if(idealColor != null && idealColor.booleanValue() == true)
			scaleDisplaySetting = "Stepped Colored";
		else
			scaleDisplaySetting = "Stepped";
		tempItemForPreview.setBlockParent(Boolean.TRUE);
		this.setScale(tempItemForPreview);
		//set Item DisplayOrder
		tempItemForPreview.setDisplayOrder(new Integer(this.currItemNo));
		itemDisplayPreview = new ItemDisplay(tempItemForPreview);
*/
		itemPreview = new EvalItem();
		updateItemObject(itemPreview);
		if(idealColor != null && idealColor.booleanValue() == true)
			scaleDisplaySetting = EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED;
		else
			scaleDisplaySetting = EvalConstants.ITEM_SCALE_DISPLAY_STEPPED;
		itemPreview.setBlockParent(Boolean.TRUE);
		this.setScale(itemPreview);
		//set Item DisplayOrder
		itemPreview.setDisplayOrder(new Integer(this.currItemNo));
		
		return PreviewItemProducer.VIEW_ID;
	}
	
	//method binding to the "Cancel" button on modify_header.html 
	public String cancelHeaderAction(){

		return TemplateModifyProducer.VIEW_ID;
	}
	
	//method binding to the "Save" button on modify_header.html
	public String saveHeaderAction(){

		if(currentItem != null)
		{	
			this.scaleDisplaySetting = null;		
			updateItemObject(currentItem);
			//logic.saveItem(currentItem, logic.getCurrentUserId());	
			itemsLogic.saveItem(currentItem, external.getCurrentUserId());
			//itemDisplayList.remove(Integer.parseInt(currRowNo));
			//itemDisplayList.add(Integer.parseInt(currRowNo), new ItemDisplay(currentItem));
			
			itemsList.remove(Integer.parseInt(currRowNo));
			itemsList.add(Integer.parseInt(currRowNo), currentItem);
		
		}
		else {					
			if ( currTemplate.getId() == null )
				templatesLogic.saveTemplate(currTemplate, external.getCurrentUserId());
				//logic.saveTemplate(currTemplate, logic.getCurrentUserId());
				
			
			currentItem = new EvalItem();
			this.scaleDisplaySetting = null;
			updateItemObject(currentItem);
			currentItem.getTemplates().add(currTemplate);
			
			//Save the item everytime	
			int orderNo = getItemsListSize();//this.getItemDisplayListSize();
			//set the display order for each item
			currentItem.setDisplayOrder(new Integer(orderNo + 1)); 
			//logic.saveItem(currentItem, logic.getCurrentUserId());
			itemsLogic.saveItem(currentItem, external.getCurrentUserId());
			
			//itemDisplayList.add(new ItemDisplay(currentItem));
			itemsList.add(currentItem);
		}
		
		return TemplateModifyProducer.VIEW_ID; 
	}
	
	//method binding to the "Preview" button on modify_header.html
	public String previewHeaderAction(){

	/*	EvalItem tempItemForPreview = new EvalItem();
		this.scaleDisplaySetting = null;
		updateItemObject(tempItemForPreview);
		//set Item DisplayOrder
		tempItemForPreview.setDisplayOrder(new Integer(this.currItemNo));
		itemDisplayPreview = new ItemDisplay(tempItemForPreview);
	*/
		itemPreview = new EvalItem();
		this.scaleDisplaySetting = null;
		updateItemObject(itemPreview);
		//set Item DisplayOrder
		itemPreview.setDisplayOrder(new Integer(this.currItemNo));
		
		return PreviewItemProducer.VIEW_ID;		
	}
	
	//initialize/clear variables when creating a new template
	public void clearTemplate(){

		title=null;
		description=null;
		modifier =null;
		currTemplate =null;
	}
	
	//TODO: This is not an efficient way of setting scale object
	private void setScale(EvalItem itemImpl) {
		List lst = scalesLogic.getScalesForUser(external.getCurrentUserId(), null);
		//List lst = logic.getScales(null);
		int count = 0;
		while (count < lst.size()) {
			EvalScale tempScale = (EvalScale)lst.get(count);
			
			if ( (tempScale.getId()).equals(scaleId) ) {
				itemImpl.setScale(tempScale);
				return;
			}
			count++;
		}
		throw new IllegalArgumentException("Could not find the scale id:" + scaleId);
	}

	
	private void updateItemObject (EvalItem tempItem) {
		
		tempItem.setLastModified(new Date());
		tempItem.setOwner(external.getCurrentUserId());
		tempItem.setItemText(itemText);
		// Description is nullable and only for parent item.
		tempItem.setExpert(Boolean.FALSE);
		// Display rows for later when explicit block added
		//tempItem.setScaleDisplaySetting(scaleDisplaySetting);
		tempItem.setCategory(itemCategory);
		tempItem.setLocked(Boolean.FALSE);
		//tempItem.setBlockParent(Boolean.FALSE);
		tempItem.setClassification(itemClassification);
		
		if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_HEADER))
			this.itemNA = null;
		
		if(this.itemClassification.equals(EvalConstants.ITEM_TYPE_SCALED))
			this.setScale(tempItem);	

		tempItem.setUsesNA(itemNA);
		
		/*
		 * Note: The parent_fk in ITEM table is for linking
		 * with the HIERARCHY table.
		 */		
	}

	//TODO: to be removed,
	//return BLOCK CHILD ITEMS specified BLOCK_ID 
/*	public List getChildItem(Integer blockId){
		log.warn("get child for block: " + blockId);

		return logic.findItem(blockId);
	}
*/
	
}

