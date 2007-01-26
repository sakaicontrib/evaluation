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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	
    private TemplateBeanLocator templateBeanLocator;

	public void setTemplateBeanLocator(TemplateBeanLocator templateBeanLocator) {
      this.templateBeanLocator = templateBeanLocator;
    }

    /**
	 * If the template is not saved, button will show text "continue and add question"
	 * method binding to the "continue and add question" button on template_title_description.html
	 * replaces TemplateBean.createTemplateAction, but template is added to db here.
	 * */	
	public String createTemplateAction() {
        templateBeanLocator.saveAll();
		return "success";
	}
	
	/**
	 * If the template is already stored, button will show text "Save"
	 * method binding to the "Save" button on template_title_description.html
	 * replaces TemplateBean.saveTemplate()
	*/
	public String updateTemplateTitleDesc() {
      templateBeanLocator.saveAll();
      return "success";
	}
	
	/**
	 * method binding to the hidden button with rsf:id="hiddenBtn" on template_modify page
	 * used to change displayOrder of each item when the onChange event of dropdown 
	 * 		(displayOrder #)on template_modify.gtml fired
	 */
	public String changeDisplayOrder(){
		//TODO - needs a rewrite
		System.out.println("Display order changed!");
		return "reordered";
	}

}