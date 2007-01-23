/******************************************************************************
 * TemplateBean.java - created by whumphri@vt.edu on Jan 16, 2007
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
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.producers.TemplateModifyProducer;

/**
 * This request-scope bean handles template creation and modification.
 * 
 * @author will Humphries (whumphri@vt.edu), Rui Feng (fengr@vt.edu), Kapil Ahuja (kahuja@vt.edu)
 */

public class TemplateBBean {
	
	/*
	 * VARIABLE DECLARATIONS 
	 */
	private static Log log = LogFactory.getLog(TemplateBean.class);
	
	//The following fields below belong to template_title_desc.html
	public String title;
	public String description;
	public String modifier;
	
	public Long templateId;
	
	public List templateItemsList; //to replace itemDisplayList which contain deprectaed ItemDisplay object
	
	private EvalItemsLogic itemsLogic;
	public void setItemsLogic(EvalItemsLogic itemsLogic) {
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
	
	private EvalTemplate currTemplate;
	
	public EvalTemplate getCurrTemplate() {
		return currTemplate;
	}
	public void setCurrTemplate(EvalTemplate c){
		currTemplate=c;
	}
	
	/*
	 * INITIALIZATION
	 */
	public void init() {
		if (templatesLogic == null) {
			throw new NullPointerException("logic is null");
		}
	}
	
	public int getTemplateItemsListSize() {
		return templateItemsList.size();
	}
	
	public boolean fetchCurrTemplate(Long tId){
		currTemplate=templatesLogic.getTemplateById(tId);
		if(currTemplate!=null){
			templateId=tId;
			if (templateItemsList == null)
				templateItemsList = new ArrayList();
			else templateItemsList.clear();		
			templateItemsList=itemsLogic.getTemplateItemsForTemplate(templateId, external.getCurrentUserId());
			return true;			
		}
		else{return false;}
	}
	
	/**
	 * If the template is not saved, button will show text "continue and add question"
	 * method binding to the "continue and add question" button on template_title_description.html
	 * replaces TemplateBean.createTemplateAction, but template is added to db here.
	 * */	
	public String createTemplateAction(){   
		EvalTemplate currTemplate = new EvalTemplate(new Date(), external.getCurrentUserId(),
				title, modifier, Boolean.FALSE);
		currTemplate.setDescription(description);
		currTemplate.setSharing(modifier);
		currTemplate.setLocked(Boolean.FALSE);
		
		templatesLogic.saveTemplate(currTemplate, external.getCurrentUserId());
		templateId=currTemplate.getId();
		return "intercept-append-tId:::"+TemplateModifyProducer.VIEW_ID;
	}
	
	/**
	 * If the template is already stored, button will show text "Save"
	 * method binding to the "Save" button on template_title_description.html
	 * replaces TemplateBean.saveTemplate()
	*/
	public String updateTemplateTitleDesc(){
		if(currTemplate.getLocked()==null)	currTemplate.setLocked(Boolean.FALSE);
		templatesLogic.saveTemplate(currTemplate, external.getCurrentUserId());
		templateId=currTemplate.getId();
		return "intercept-append-tId:::"+TemplateModifyProducer.VIEW_ID;
	}
	
	/**
	 * method binding to the hidden button with rsf:id="hiddenBtn" on template_modify page
	 * used to change displayOrder of each item when the onChange event of dropdown 
	 * 		(displayOrder #)on template_modify.gtml fired
	 */
	public String changeDisplayOrder(){
		//TODO - needs a rewrite
		return TemplateModifyProducer.VIEW_ID;
	}

}