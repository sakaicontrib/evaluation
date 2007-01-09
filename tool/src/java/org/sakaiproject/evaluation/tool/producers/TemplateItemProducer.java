/******************************************************************************
 * TemplateItemProducer.java - created by fengr@vt.edu on Aug 21, 2006
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

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.TemplateBean;


import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Page for Create, modify,preview, delete a Scaled/Suvey type Item
 *
 * @author: Rui Feng (fengr@vt.edu)
 */

public class TemplateItemProducer implements ViewComponentProducer,NavigationCaseReporter {
	public static final String VIEW_ID = "template_item"; //$NON-NLS-1$

	private TemplateBean templateBean;

	public void setTemplateBean(TemplateBean templateBean) {
		this.templateBean = templateBean;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	public String getViewID() {
		return VIEW_ID;
	}


	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIOutput.make(tofill, "modify-item-title", messageLocator.getMessage("templateitem.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "create-eval-title", messageLocator.getMessage("createeval.page.title")); //$NON-NLS-1$ //$NON-NLS-2$

		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
							new SimpleViewParameters(SummaryProducer.VIEW_ID));

		UIForm form = UIForm.make(tofill, "itemForm"); //$NON-NLS-1$

		UIOutput.make(form, "item-header", messageLocator.getMessage("modifyitem.item.header"));	//TODO: exception: can not get property
		//UIOutput.make(form, "item-header","Item" );
		UIOutput.make(form,"itemNo",null,"#{templateBean.currItemNo}"); //$NON-NLS-1$ //$NON-NLS-2$

		UIOutput.make(form,"itemClassification",null,"#{templateBean.itemClassification}");
		UIOutput.make(form, "added-by", messageLocator.getMessage("modifyitem.added.by"));  //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "userInfo",null, "#{templateBean.userId}");
		
		if(templateBean.currentItem != null){
			UIBranchContainer showLink = UIBranchContainer.make(form, "showRemoveLink:");
			UIInternalLink.make(showLink, "remove_link", messageLocator.getMessage("modifyitem.remove.link"), new SimpleViewParameters("remove_question"));	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		UIOutput.make(form, "question-text-header", messageLocator.getMessage("modifyitem.question.text.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput.make(form,"item_text", "#{templateBean.itemText}"); //$NON-NLS-1$ //$NON-NLS-2$

		UIOutput.make(form, "scale-type-header", messageLocator.getMessage("templateitem.scale.type.header")); //$NON-NLS-1$ //$NON-NLS-2$
		//dropdown list for "Scale Type"
		UISelect combo = UISelect.make(form, "scaleList"); //$NON-NLS-1$
		combo.selection = new UIInput();
		combo.selection.valuebinding = new ELReference("#{templateBean.scaleId}"); //$NON-NLS-1$
		UIBoundList comboValues = new UIBoundList();
		comboValues.valuebinding=new ELReference("#{templateBean.scaleValues}"); //$NON-NLS-1$
		combo.optionlist = comboValues;
		UIBoundList comboNames = new UIBoundList();
		comboNames.valuebinding=new ELReference("#{templateBean.scaleLabels}"); //$NON-NLS-1$
		combo.optionnames = comboNames;

		UIOutput.make(form, "scale-display-header", messageLocator.getMessage("templateitem.scale.display.header")); //$NON-NLS-1$ //$NON-NLS-2$
		//drop down list for "Scale Display Setting"
		UISelect sl = UISelect.make(form, "scaleDisplaySetting"); //$NON-NLS-1$
		sl.selection = new UIInput();
		sl.selection.valuebinding = new ELReference("#{templateBean.scaleDisplaySetting}");
		UIBoundList slNames = new UIBoundList();
		slNames.setValue(EvaluationConstant.SCALE_DISPLAY_SETTING_VALUES_LABELS); //Need to pull strings from properties file
		sl.optionnames = slNames;
		UIBoundList slValues = new UIBoundList();
    	slValues.setValue(EvaluationConstant.SCALE_DISPLAY_SETTING_VALUES);
		sl.optionlist = slValues;

		UIOutput.make(form, "add-na-header", messageLocator.getMessage("modifyitem.add.na.header"));			 //$NON-NLS-1$ //$NON-NLS-2$
		UIBoundBoolean.make(form, "item_NA", "#{templateBean.itemNA}",null); //$NON-NLS-1$ //$NON-NLS-2$

		UIOutput.make(form, "item-category-header", messageLocator.getMessage("modifyitem.item.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "course-category-header", messageLocator.getMessage("modifyitem.course.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "instructor-category-header", messageLocator.getMessage("modifyitem.instructor.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
		//Radio Buttons for "Item Category"
		UISelect radios = UISelect.make(form, "item_category", EvaluationConstant.ITEM_CATEGORY_VALUES,
				EvaluationConstant.ITEM_CATEGORY_VALUES_LABELS, "#{templateBean.itemCategory}",null); //Need to pull strings from properties file

		String selectID = radios.getFullID();
		UISelectChoice.make(form, "item_category_C", selectID, 0); //$NON-NLS-1$
		UISelectChoice.make(form, "item_category_I", selectID, 1);	 //$NON-NLS-1$

		UICommand.make(form, "cancelItemAction",  messageLocator.getMessage("modifyitem.cancel.button"), "#{templateBean.cancelItemAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		UICommand.make(form, "saveItemAction",  messageLocator.getMessage("modifyitem.save.button"), "#{templateBean.saveItemAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		UICommand.make(form, "previewItemAction",  messageLocator.getMessage("modifyitem.preview.button"), "#{templateBean.previewItemAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	}

	public List reportNavigationCases() {
		List i = new ArrayList();

		i.add(new NavigationCase(PreviewItemProducer.VIEW_ID, new SimpleViewParameters(PreviewItemProducer.VIEW_ID)));
		i.add(new NavigationCase(TemplateModifyProducer.VIEW_ID, new SimpleViewParameters(TemplateModifyProducer.VIEW_ID)));

		return i;
	}


}