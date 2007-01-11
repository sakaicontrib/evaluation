/******************************************************************************
 * TemplateModifyProducer.java - created by fengr@vt.edu on Aug 21, 2006
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

import org.sakaiproject.evaluation.logic.EvalItemsLogic;

import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.ItemDisplay;
import org.sakaiproject.evaluation.tool.TemplateBean;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.params.PreviewEvalParameters;


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
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;


/**
 * This is the main page for handling various operations to template, items,
 * 
 * @author: Rui Feng (fengr@vt.edu)
 * @author: Kapil Ahuja (kahuja@vt.edu)
 */

public class TemplateModifyProducer implements ViewComponentProducer,NavigationCaseReporter{
	
	public static final String VIEW_ID = "template_modify"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}
	
	private TemplateBean templateBean;
	
	public void setTemplateBean(TemplateBean templateBean) {
		this.templateBean = templateBean;
	}
	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}	
	
	/*
	 *  1) access this page through "Continue and Add Questions" button on Template page
	 *  2) access this page through links on Control Panel or other
	 *  3) access this page through "Save" button on Template page
	 *  
	 *  TODO: remove usage of ItemDisplay
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIOutput.make(tofill, "modify-template-title", messageLocator.getMessage("modifytemplate.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
							new SimpleViewParameters(SummaryProducer.VIEW_ID));			
		
		UIForm form = UIForm.make(tofill, "modifyForm"); //$NON-NLS-1$
		
		//preview link
		UIInternalLink.make(form, "preview_eval_link", 
				new PreviewEvalParameters(PreviewEvalProducer.VIEW_ID, null,
						templateBean.getCurrTemplate().getId(),null, TemplateModifyProducer.VIEW_ID));
	
		UIOutput.make(form, "preview-eval-desc", messageLocator.getMessage("modifytemplate.preview.eval.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "add-item-note", messageLocator.getMessage("modifytemplate.add.item.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
		//dropdown list
		UISelect combo = UISelect.make(form, "itemClassification"); //$NON-NLS-1$
		combo.selection = new UIInput();
		combo.selection.valuebinding = new ELReference("#{templateBean.itemClassification}");	
		//combo.selection.valuebinding = new ELReference("#{templateBean.item.classification}");	
		UIBoundList comboNames = new UIBoundList();
		String[] itemClassificationList = 
		{
			messageLocator.getMessage("modifytemplate.itemtype.scaled"),
			messageLocator.getMessage("modifytemplate.itemtype.text"),
			messageLocator.getMessage("modifytemplate.itemtype.header"),
			messageLocator.getMessage("modifytemplate.itemtype.block"),
			messageLocator.getMessage("modifytemplate.itemtype.expert")
		};
		
		comboNames.setValue(itemClassificationList);
		combo.optionnames = comboNames;
		UIBoundList comboValues = new UIBoundList();
		comboValues.setValue(EvaluationConstant.ITEM_CLASSIFICATION_VALUES);
		combo.optionlist = comboValues;	
		
		//command button:"Add"
		UICommand.make(form, "add_questions", messageLocator.getMessage("modifytemplate.add.item.button"), "#{templateBean.addItemAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
		if(templateBean.getItemDisplayListSize() > 0){
			UIInternalLink.make(form, "begin_eval_link", new EvalViewParameters(EvaluationStartProducer.VIEW_ID, 
						templateBean.getCurrTemplate().getId(), TemplateModifyProducer.VIEW_ID));
		}else{
			UIOutput.make(form, "begin-eval-dummylink", messageLocator.getMessage("modifytemplate.begin.eval.link"));	
		}
		
		UIOutput.make(form, "univ-level-header", messageLocator.getMessage("modifytemplate.univ.level.header")); //$NON-NLS-1$ //$NON-NLS-2$			
		UIOutput.make(form,"itemCount",null,"#{templateBean.itemDisplayListSize}");
		UIOutput.make(form, "existing-items", messageLocator.getMessage("modifytemplate.existing.items")); //$NON-NLS-1$ //$NON-NLS-2$
	
		UIOutput.make(form, "template-title-header", messageLocator.getMessage("modifytemplate.template.title.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "title",null,"#{templateBean.title}");
		
		UIInternalLink.make(form, 
				"modify_title_desc_link", 
				messageLocator.getMessage("modifytemplate.modify.title.desc.link"),
				new EvalViewParameters(
						TemplateProducer.VIEW_ID, 
						templateBean.getCurrTemplate().getId(), 
						TemplateModifyProducer.VIEW_ID));
		
		UIOutput.make(form, "description-header", messageLocator.getMessage("modifytemplate.description.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "description",null, "#{templateBean.description}");	

		UIOutput.make(tofill, "eval-sample", messageLocator.getMessage("modifytemplate.eval.sample")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "course-sample", messageLocator.getMessage("modifytemplate.course.sample")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIForm form2 = UIForm.make(tofill, "modifyFormRows");	 //$NON-NLS-1$
		UICommand.make(form2,"hiddenBtn","#{templateBean.changeDisplayOrder}"); 
		
		//display each item in the template	
		List l= templateBean.itemDisplayList;
		if (l!=null && l.size() >0) {
			String[] strArr = new String[l.size()];
		    for(int h=0; h<l.size();h++){
		    	strArr[h]= Integer.toString(h+1);
		    }
		    			
		    for(int i=0;i<l.size();i++){
		    	ItemDisplay currItemDisplay=(ItemDisplay) l.get(i);
		    	UIBranchContainer radiobranch = UIBranchContainer.make(form2,"itemrow:header", Integer.toString(i)); //$NON-NLS-1$
				UIOutput.make(radiobranch, "item-num-header", messageLocator.getMessage("modifytemplate.item.num.header")); //$NON-NLS-1$ //$NON-NLS-2$
			
				//DISPLAY ORDER
				UISelect sl = UISelect.make(radiobranch, "itemNum");
				sl.selection = new UIInput();
				sl.selection.valuebinding = new ELReference("#{templateBean.itemDisplayList." + i +".item.displayOrder"+"}");
				UIBoundList slNames = new UIBoundList();
				slNames.setValue(strArr);
				sl.optionnames = slNames;
				UIBoundList slValues = new UIBoundList();
		    	slValues.setValue(strArr);
				sl.optionlist = slValues;
								
				String itemClassificationLabel = (currItemDisplay.getItem()).getClassification();
				UIOutput.make(radiobranch,"itemClassificationLabel",itemClassificationLabel);
				String scaleDisplaySettingLabel = (currItemDisplay.getItem()).getScaleDisplaySetting();
				if(scaleDisplaySettingLabel !=null)
					scaleDisplaySettingLabel = "-" + scaleDisplaySettingLabel;
				else scaleDisplaySettingLabel = "";
				UIOutput.make(radiobranch,"scaleDisplaySetting",scaleDisplaySettingLabel);	
				
				UICommand previewCmd=UICommand.make(radiobranch,"preview_row_item","#{templateBean.previewRowItemAction}");
				previewCmd.parameters.add(new UIELBinding("#{templateBean.currRowNo}",Integer.toString(i)));

				UICommand modifyCmd=UICommand.make(radiobranch,"modify_row_item","#{templateBean.modifyRowItemAction}");
				modifyCmd.parameters.add(new UIELBinding("#{templateBean.currRowNo}",Integer.toString(i)));

				UICommand removeCmd=UICommand.make(radiobranch,"remove_row_item","#{templateBean.removeRowItemAction}");
				removeCmd.parameters.add(new UIELBinding("#{templateBean.currRowNo}",Integer.toString(i)));
	
				UIBranchContainer radiobranch2 = UIBranchContainer.make(form2,"itemrow:text", Integer.toString(i)); //$NON-NLS-1$
				UIOutput.make(radiobranch2,"queNo",Integer.toString(i+1));	 //$NON-NLS-1$
				UIOutput.make(radiobranch2,"itemText",currItemDisplay.getItem().getItemText());	
				
				String title = ""; //$NON-NLS-1$
				if(currItemDisplay.getItem().getScale() != null)
					title = currItemDisplay.getItem().getScale().getTitle();
				UIOutput.make(radiobranch2,"scaleType",title); //$NON-NLS-1$
				
				Boolean useNA= currItemDisplay.getItem().getUsesNA();
				if(useNA != null && useNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(radiobranch2,"showNA:", Integer.toString(i)); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",useNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
		/*TODO: wait for aaron's logic method
				if(currItemDisplay.getItem().getBlockParent().booleanValue()== true){
					//get child items
					Long parentID = currItemDisplay.getItem().getId();
					Integer blockID = new Integer(parentID.intValue());
					List childItems = logic.findItem(blockID);
					if(childItems != null && childItems.size()>0){
						for(int k =0; k< childItems.size(); k++){
							UIBranchContainer childRow = UIBranchContainer.make(form2,"itemrow:blockItems", Integer.toString(k));
							EvalItem child = (EvalItem) childItems.get(k);
							UIOutput.make(childRow,"childItemId",Integer.toString(k+1));	
							UIOutput.make(childRow,"childItemText",child.getItemText());	
						}
					}
				}		
				
				*/
	
		    }//end of for loop
		}
		
	}


	public List reportNavigationCases() {
		List i = new ArrayList();
		
		i.add(new NavigationCase(TemplateItemProducer.VIEW_ID, new SimpleViewParameters(TemplateItemProducer.VIEW_ID)));		
		i.add(new NavigationCase(ModifyEssayProducer.VIEW_ID, new SimpleViewParameters(ModifyEssayProducer.VIEW_ID)));
		i.add(new NavigationCase(ModifyHeaderProducer.VIEW_ID, new SimpleViewParameters(ModifyHeaderProducer.VIEW_ID)));
		i.add(new NavigationCase(ModifyBlockProducer.VIEW_ID, new SimpleViewParameters(ModifyBlockProducer.VIEW_ID)));
		
		i.add(new NavigationCase(TemplateModifyProducer.VIEW_ID, new SimpleViewParameters(TemplateModifyProducer.VIEW_ID)));
		i.add(new NavigationCase(PreviewItemProducer.VIEW_ID, new SimpleViewParameters(PreviewItemProducer.VIEW_ID)));
		i.add(new NavigationCase(RemoveQuestionProducer.VIEW_ID, new SimpleViewParameters(RemoveQuestionProducer.VIEW_ID)));
		
		return i;
	}

}




