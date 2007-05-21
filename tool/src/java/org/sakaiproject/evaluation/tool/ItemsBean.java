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
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

/**
 * This request-scope bean handles item creation and modification.
 * 
 * @author Will Humphries (whumphri@vt.edu)
 * @author Rui Feng (fengr@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @deprecated This needs to be completely removed -AZ
 */
public class ItemsBean {

	/*
	 * VARIABLE DECLARATIONS
	 */
	private static Log log = LogFactory.getLog(ItemsBean.class);

	/**
	 * Point to the value of dropdown list (Scale/Survey,Question
	 * Block,TextHeader,etc) on template_modify.html and also point to the top
	 * label field on tempalte_item.html
	 */

	public EvalTemplateItem templateItem;

	// The following fields below belong to template_item.html
	public Long scaleId;
	private List scaleValues; // "Scale Type" drop down list
	private List scaleLabels; // "Scale Type" drop down list

	// The following fields below belong to modify_block.html
	public Boolean idealColor;
	public List queList;
	public String currQueNo;
	public String currRowNo;
	public String classification;
	public Long templateId;


	private EvalItemsLogic itemsLogic;
	public void setItemsLogic(EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
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
		log.debug("INIT");
	}

	// GETTERS and SETTERS
	public List getScaleValues() {
		// get scale values and labels from DAO
		List list = null;
		String scaleOptionsStr = "";
		this.scaleValues = new ArrayList();
		this.scaleLabels = new ArrayList();

		list = scalesLogic.getScalesForUser(external.getCurrentUserId(), null);// logic.getScales(Boolean.FALSE);
		for (int count = 0; count < list.size(); count++) {
			scaleOptionsStr = "";
			String[] scaleOptionsArr = ((EvalScale) list.get(count)).getOptions();
			for (int innerCount = 0; innerCount < scaleOptionsArr.length; innerCount++) {

				if (scaleOptionsStr == "")
					scaleOptionsStr = scaleOptionsArr[innerCount];
				else
					scaleOptionsStr = scaleOptionsStr + ", " + scaleOptionsArr[innerCount];

			} // end of inner for

			EvalScale sl = (EvalScale) list.get(count);
			this.scaleValues.add((sl.getId()).toString());
			this.scaleLabels.add(scaleOptionsArr.length + " pt - " + sl.getTitle() + " (" + scaleOptionsStr + ")");

		} // end of outer loop
		return scaleValues;
	}

	public void setScaleValues(List scaleValues) {
		this.scaleValues = scaleValues;
	}

	public List getScaleLabels() {
		// make sure if getScaleValue() was called first, getScaleLabels() will not be called again
		if (scaleLabels == null)
			getScaleValues();

		return scaleLabels;
	}

	public void setScaleLabels(List scaleLabels) {
		this.scaleLabels = scaleLabels;
	}


	// ACTION METHODS

	public String cancelItemAction() {
		return "cancel";
	}

	public String previewItemAction() {
		return null;
	}

	public String cancelRemoveAction() {
		return null;
	}

	public String saveItemAction() {
		templateItem.getItem().setScaleDisplaySetting(templateItem.getScaleDisplaySetting());
		templateItem.getItem().setUsesNA(templateItem.getUsesNA());
		templateItem.setTemplate(templatesLogic.getTemplateById(templateId));
		templateItem.getItem().setSharing(templateItem.getTemplate().getSharing());
		templateItem.getItem().setCategory(templateItem.getItemCategory());
		if (scaleId != null)
			templateItem.getItem().setScale(scalesLogic.getScaleById(scaleId));
		itemsLogic.saveItem(templateItem.getItem(), external.getCurrentUserId());
		itemsLogic.saveTemplateItem(templateItem, external.getCurrentUserId());
		return "success";
	}

	public void newItemInit(Long templateId, String classification) {
		templateItem.getItem().setClassification(classification);
		this.templateId = templateId;
	}

	public void fetchTemplateItem(Long templateItemId) {
		templateItem = itemsLogic.getTemplateItemById(templateItemId);
	}

}