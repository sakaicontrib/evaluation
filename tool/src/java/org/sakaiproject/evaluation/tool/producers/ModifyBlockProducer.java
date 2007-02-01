/******************************************************************************
 * ModifyBlockProducer.java - created by fengr@vt.edu on Oct 2, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.TemplateBean;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.params.TemplateItemViewParameters;


import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundBoolean;
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
import uk.org.ponder.rsf.flow.jsfnav.DynamicNavigationCaseReporter;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Page for Create, modify, preview, delete a Block type Item
 * 
 * @author: Rui Feng (fengr@vt.edu)
 */

public class ModifyBlockProducer implements ViewComponentProducer,ViewParamsReporter,DynamicNavigationCaseReporter{
	public static final String VIEW_ID = "modify_block"; //$NON-NLS-1$

	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}	
	
	public String getViewID() {
		return VIEW_ID;
	}
	
	// Permissible since is a request-scope producer. Accessed from NavigationCases
	private Long templateId; 

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {	
		TemplateItemViewParameters templateItemViewParams = (TemplateItemViewParameters) viewparams;

	    String templateItemOTP = null;
	    String templateItemOTPBinding = null;
	    templateId = templateItemViewParams.templateId;
	    Long templateItemId = templateItemViewParams.templateItemId;

	    if (templateItemId != null) {
	      templateItemOTPBinding = "templateItemBeanLocator." + templateItemId;
	    }
	    else {
	      templateItemOTPBinding = "templateItemBeanLocator.new1";
	    }
	    templateItemOTP = templateItemOTPBinding + ".";
	  
	    //TODO - replace with darreshaper
	    if(templateItemId!=null){
		    /*TODO
		      
		     EvalTemplateItem myTemplateItem=itemsLogic.getTemplateItemById(templateItemId);
		    if(myTemplateItem.getItem().getScale()!=null){
		    	itemsBean.scaleId=itemsLogic.getTemplateItemById(templateItemId).getItem().getScale().getId();
		    }
		    */
	    }
		
		UIOutput.make(tofill, "modify-block-title", messageLocator.getMessage("modifyblock.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "create-eval-title", messageLocator.getMessage("createeval.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
							new SimpleViewParameters(SummaryProducer.VIEW_ID));			
		
		UIForm form = UIForm.make(tofill, "blockForm"); //$NON-NLS-1$
		
		UIOutput.make(form, "item-header", messageLocator.getMessage("modifyitem.item.header")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(form,"itemNo","1."); //TODO:
		UIOutput.make(form,"itemClassification",messageLocator.getMessage("modifytemplate.itemtype.block"));
		
		
		UIOutput.make(form, "added-by", messageLocator.getMessage("modifyitem.added.by")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "userInfo", null, templateItemOTP + "owner");
		
		//TODO: should pass the item ID to RemoveQuestion page
		if (templateItemId != null) {
			UIBranchContainer showLink = UIBranchContainer.make(form, "showRemoveLink:");
			UIInternalLink.make(showLink, "remove_link", messageLocator.getMessage("modifyitem.remove.link"), 
					new SimpleViewParameters( "remove_question")); 
		}
		
		UIOutput.make(form, "item-header-text-header", messageLocator.getMessage("modifyblock.item.header.text.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput.make(form,"item_text", templateItemOTP + "item.itemText"); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(form, "scale-type-header", messageLocator.getMessage("modifyblock.scale.type.header")); //$NON-NLS-1$ //$NON-NLS-2$
		//dropdown list for "Scale Type"
		UISelect combo = UISelect.make(form, "scaleList"); //$NON-NLS-1$
		combo.selection = new UIInput();
		combo.selection.valuebinding = new ELReference("#{itemsBean.scaleId}"); //$NON-NLS-1$
		UIBoundList comboValues = new UIBoundList();
		comboValues.valuebinding=new ELReference("#{itemsBean.scaleValues}"); //$NON-NLS-1$
		combo.optionlist = comboValues;
		UIBoundList comboNames = new UIBoundList();
		comboNames.valuebinding=new ELReference("#{itemsBean.scaleLabels}");
		combo.optionnames = comboNames;
		
		UIOutput.make(form,"add-na-header",messageLocator.getMessage("modifyitem.add.na.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIBoundBoolean.make(form, "item_NA", templateItemOTP + "item.usesNA", null); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(form,"ideal-coloring-header",messageLocator.getMessage("modifyblock.ideal.coloring.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIBoundBoolean.make(form, "idealColor", "#{itemsBean.idealColor}",null); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(form,"item-category-header",messageLocator.getMessage("modifyitem.item.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form,"course-category-header",messageLocator.getMessage("modifyitem.course.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form,"instructor-category-header",messageLocator.getMessage("modifyitem.instructor.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
		//		Radio Buttons for "Item Category"
		String[] courseCategoryList = 
		{
			messageLocator.getMessage("modifyitem.course.category.header"),
			messageLocator.getMessage("modifyitem.instructor.category.header"),
		};
	    UISelect radios = UISelect.make(form, "item_category",
	            EvaluationConstant.ITEM_CATEGORY_VALUES, courseCategoryList,
	            templateItemOTP + "itemCategory", null);

		String selectID = radios.getFullID();
		UISelectChoice.make(form, "item_category_C", selectID, 0); //$NON-NLS-1$
		UISelectChoice.make(form, "item_category_I", selectID, 1);	 //$NON-NLS-1$
		
		UIOutput.make(form,"items-header","Items:"); 
/*	TODO	
		int queNo =  templateBean.queList.size();
		for(int i=0;i< queNo; i++){		
			UIBranchContainer radiobranch = UIBranchContainer.make(form,"queRow:",Integer.toString(i)); //$NON-NLS-1$
			UIInput.make(radiobranch,"queText","#{templateBean.queList." + i +"}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			//UICommand removeQueCmd=UICommand.make(radiobranch,"removeQue","Remove");
			UICommand removeQueCmd = UICommand.make(radiobranch,"removeQue",messageLocator.getMessage("modifyitem.remove.link"), "#{templateBean.removeItemFromBlock}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
			removeQueCmd.parameters.add(new UIELBinding("#{templateBean.currQueNo}",Integer.toString(i))); //$NON-NLS-1$

		}
*/
		for(int i=0;i< 3; i++){		
			UIBranchContainer radiobranch = UIBranchContainer.make(form,"queRow:",Integer.toString(i)); //$NON-NLS-1$
			UIInput.make(radiobranch,"queText",null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			//UICommand removeQueCmd=UICommand.make(radiobranch,"removeQue","Remove");
			UIOutput.make(radiobranch,"removeQue",messageLocator.getMessage("modifyitem.remove.link")); 
		}
		
	/*	TODO
	 * UICommand.make(form, "addItemToBlock", messageLocator.getMessage("modifyblock.add.item.to.block.button"), "#{templateBean.addItemToBlock}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	*/
		UIOutput.make(form, "addItemToBlock", messageLocator.getMessage("modifyblock.add.item.to.block.button"));
		
		UIOutput.make(form, "cancel-button", messageLocator.getMessage("general.cancel.button"));
		UICommand saveCmd = UICommand.make(form, "saveBlockAction", messageLocator.getMessage("modifyitem.save.button"), 
				"#{itemsBean.saveBlockItemAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		saveCmd.parameters.add(new UIELBinding(templateItemOTP
			        + "item.classification", EvalConstants.ITEM_TYPE_SCALED));
			    saveCmd.parameters.add(new UIELBinding("#{itemsBean.templateItem}",
			        new ELReference(templateItemOTPBinding)));
			    saveCmd.parameters.add(new UIELBinding("#{itemsBean.templateId}",
			        templateId));
		/*TODO: preview new/modified Block item
		 * 
		UICommand.make(form, "previewBlockAction", messageLocator.getMessage("modifyitem.preview.button"), "#{templateBean.previewBlockAction}"); 
		*/
	}

	public List reportNavigationCases() {
		List i = new ArrayList();
		
		i.add(new NavigationCase(PreviewItemProducer.VIEW_ID, new SimpleViewParameters(PreviewItemProducer.VIEW_ID)));
		i.add(new NavigationCase("success",
		        new EvalViewParameters(TemplateModifyProducer.VIEW_ID, templateId)));

		return i;
	}

	public ViewParameters getViewParameters() {
		return new TemplateItemViewParameters();
	}


}
