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

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.ItemsBean;
import org.sakaiproject.evaluation.tool.TemplateBean;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.params.TemplateItemViewParameters;


import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
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
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Page for Create, modify,preview, delete a Scaled/Suvey type Item
 *
 * @author: Rui Feng (fengr@vt.edu)
 */

public class TemplateItemProducer implements ViewComponentProducer,ViewParamsReporter {
	public static final String VIEW_ID = "template_item"; //$NON-NLS-1$

	private TemplateBean templateBean;

	private Long templateItemId;
	private Long templateId;
	
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
	
	public void setTemplateBean(TemplateBean templateBean) {
		this.templateBean = templateBean;
	}

	private ItemsBean itemsBean;
	public void setItemsBean(ItemsBean itemsBean) {
		this.itemsBean = itemsBean;
	}
	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	public String getViewID() {
		return VIEW_ID;
	}


	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		TemplateItemViewParameters templateItemViewParams = (TemplateItemViewParameters) viewparams;
		
		String templateItemOTP=null;
		String templateItemOTPBinding=null;
		if(templateItemViewParams.templateItemId!=null){
		    templateItemId=templateItemViewParams.templateItemId;
			templateItemOTPBinding="templateItemBeanLocator."+templateItemId;
		}
		else{
			templateItemOTPBinding="templateItemBeanLocator.new1";
			
		}
	    templateItemOTP=templateItemOTPBinding+".";	
	    templateId=templateItemViewParams.templateId;
		
	/*	EvalTemplateItem templateItem=new EvalTemplateItem();
		templateItem.setItem(new EvalItem());

		if(templateItemViewParams.templateItemId==null){
			System.out.println(templateItemViewParams.templateId);
			templateItem.getItem().setClassification(EvalConstants.ITEM_TYPE_SCALED);
			templateItem.setDisplayOrder(new Integer(itemsLogic.getTemplateItemsForTemplate(templateItemViewParams.templateId, external.getCurrentUserId()).size()));
			templateItem.setOwner(external.getCurrentUserId());
			itemsBean.newItemInit(templateItemViewParams.templateId, EvalConstants.ITEM_TYPE_SCALED);			
		}
		else{
			templateItem = itemsLogic.getTemplateItemById(templateItemId);
			
			itemsBean.fetchTemplateItem(templateItemViewParams.templateItemId);
		}*/
		
		UIOutput.make(tofill, "modify-item-title", messageLocator.getMessage("templateitem.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "create-eval-title", messageLocator.getMessage("createeval.page.title")); //$NON-NLS-1$ //$NON-NLS-2$

		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
							new SimpleViewParameters(SummaryProducer.VIEW_ID));

		UIForm form = UIForm.make(tofill, "itemForm"); //$NON-NLS-1$

		UIOutput.make(form, "item-header", messageLocator.getMessage("modifyitem.item.header"));	//TODO: exception: can not get property
		//UIOutput.make(form, "item-header","Item" );
		UIOutput.make(form,"itemNo",null,"1."); //$NON-NLS-1$ //$NON-NLS-2$

		UIOutput.make(form,"itemClassification",null,"Scaled");
		UIOutput.make(form, "added-by", messageLocator.getMessage("modifyitem.added.by"));  //$NON-NLS-1$ //$NON-NLS-2$
		/** TODO - fetch id of owner */
		UIOutput.make(form, "userInfo",null, templateItemOTP+"owner");
		
		if(templateItemViewParams.templateItemId != null){
			UIBranchContainer showLink = UIBranchContainer.make(form, "showRemoveLink:");
			UIInternalLink.make(showLink, "remove_link", messageLocator.getMessage("modifyitem.remove.link"), new SimpleViewParameters("remove_question"));	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		UIOutput.make(form, "question-text-header", messageLocator.getMessage("modifyitem.question.text.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput.make(form,"item_text", templateItemOTP+"item.itemText"); //$NON-NLS-1$ //$NON-NLS-2$

		UIOutput.make(form, "scale-type-header", messageLocator.getMessage("templateitem.scale.type.header")); //$NON-NLS-1$ //$NON-NLS-2$
		//dropdown list for "Scale Type"
		UISelect combo = UISelect.make(form, "scaleList"); //$NON-NLS-1$
		combo.selection = new UIInput();
		combo.selection.valuebinding = new ELReference("#{itemsBean.scaleId}"); //$NON-NLS-1$
		UIBoundList comboValues = new UIBoundList();
		comboValues.valuebinding=new ELReference("#{itemsBean.scaleValues}"); //$NON-NLS-1$
		combo.optionlist = comboValues;
		UIBoundList comboNames = new UIBoundList();
		comboNames.valuebinding=new ELReference("#{itemsBean.scaleLabels}"); //$NON-NLS-1$
		combo.optionnames = comboNames;

		UIOutput.make(form, "scale-display-header", messageLocator.getMessage("templateitem.scale.display.header")); //$NON-NLS-1$ //$NON-NLS-2$
		//drop down list for "Scale Display Setting"
		UISelect sl = UISelect.make(form, "scaleDisplaySetting"); //$NON-NLS-1$
		sl.selection = new UIInput();
		sl.selection.valuebinding = new ELReference(templateItemOTP+"scaleDisplaySetting");
		
		String[] scaleLabelList = 
		{
			messageLocator.getMessage("templateitem.scale.select.compact"),
			messageLocator.getMessage("templateitem.scale.select.compactc"),
			messageLocator.getMessage("templateitem.scale.select.full"),
			messageLocator.getMessage("templateitem.scale.select.fullc"),
			messageLocator.getMessage("templateitem.scale.select.stepped"),
			messageLocator.getMessage("templateitem.scale.select.steppedc"),
			messageLocator.getMessage("templateitem.scale.select.vertical")
		};
		UIBoundList slNames = new UIBoundList();
		slNames.setValue(scaleLabelList);
		sl.optionnames = slNames;
		UIBoundList slValues = new UIBoundList();
    	slValues.setValue(EvaluationConstant.SCALE_DISPLAY_SETTING_VALUES);
		sl.optionlist = slValues;

		UIOutput.make(form, "add-na-header", messageLocator.getMessage("modifyitem.add.na.header"));			 //$NON-NLS-1$ //$NON-NLS-2$
		UIBoundBoolean.make(form, "item_NA", templateItemOTP+"item.usesNA",null); //$NON-NLS-1$ //$NON-NLS-2$

		UIOutput.make(form, "item-category-header", messageLocator.getMessage("modifyitem.item.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "course-category-header", messageLocator.getMessage("modifyitem.course.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "instructor-category-header", messageLocator.getMessage("modifyitem.instructor.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
		//Radio Buttons for "Item Category"
		String[] courseCategoryList = 
		{
			messageLocator.getMessage("modifyitem.course.category.header"),
			messageLocator.getMessage("modifyitem.instructor.category.header"),
		};
		UISelect radios = UISelect.make(form, "item_category", EvaluationConstant.ITEM_CATEGORY_VALUES,
				courseCategoryList, templateItemOTP+"itemCategory",null);
		
		String selectID = radios.getFullID();
		UISelectChoice.make(form, "item_category_C", selectID, 0); //$NON-NLS-1$
		UISelectChoice.make(form, "item_category_I", selectID, 1);	 //$NON-NLS-1$

		UICommand.make(form, "cancelItemAction",  messageLocator.getMessage("general.cancel.button"), "#{itemsBean.cancelItemAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		UICommand saveCmd = UICommand.make(form, "saveItemAction",  messageLocator.getMessage("modifyitem.save.button"), "#{itemsBean.saveItemAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		//saveCmd.parameters.add(new UIELBinding(templateItemOTP+"template",templatesLogic.getTemplateById(templateId)));
		saveCmd.parameters.add(new UIELBinding(templateItemOTP+"item.classification",EvalConstants.ITEM_TYPE_SCALED));	
		saveCmd.parameters.add(new UIELBinding("#{itemsBean.templateItem}",new ELReference(templateItemOTPBinding)));
		saveCmd.parameters.add(new UIELBinding("#{itemsBean.templateId}",templateId));

		
		UICommand.make(form, "previewItemAction",  messageLocator.getMessage("modifyitem.preview.button"), "#{itemsBean.previewItemAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	}

/*	public List reportNavigationCases() {
		List i = new ArrayList();

		i.add(new NavigationCase(PreviewItemProducer.VIEW_ID, new SimpleViewParameters(PreviewItemProducer.VIEW_ID)));
		i.add(new NavigationCase(TemplateModifyProducer.VIEW_ID, new SimpleViewParameters(TemplateModifyProducer.VIEW_ID)));

		return i;
	}*/

	public ViewParameters getViewParameters() {
		return new TemplateItemViewParameters(VIEW_ID, null, null, null);
	}




}