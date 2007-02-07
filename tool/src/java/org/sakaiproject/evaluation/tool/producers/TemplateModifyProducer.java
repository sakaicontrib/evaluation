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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;
import org.sakaiproject.evaluation.tool.params.BlockIdsParameters;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.params.PreviewEvalParameters;
import org.sakaiproject.evaluation.tool.params.TemplateItemViewParameters;
import org.sakaiproject.evaluation.tool.utils.ItemBlockUtils;

import uk.org.ponder.htmlutil.HTMLUtil;
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
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;


/**
 * This is the main page for handling various operations to template, items,
 * 
 * @author: Rui Feng (fengr@vt.edu)
 * @author: Kapil Ahuja (kahuja@vt.edu)
 */

public class TemplateModifyProducer implements ViewComponentProducer,
  ViewParamsReporter {
	
	public static final String VIEW_ID = "template_modify"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}
	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}	
	
	public ViewParameters getViewParameters() {
		return new EvalViewParameters();
	}
	
	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

    public void setViewStateHandler(ViewStateHandler viewStateHandler) {
      this.viewStateHandler = viewStateHandler;
    }
    
    private ViewStateHandler viewStateHandler;
	
    private ViewParameters deriveTarget(String viewID, Long templateId) {
      return new TemplateItemViewParameters(viewID, templateId, null);
    }
    private String[] convertViews(String[] viewIDs, Long templateId) {
      String[] togo = new String[viewIDs.length];
      for (int i = 0; i < viewIDs.length; ++ i) {
        togo[i] = viewStateHandler.getFullURL(deriveTarget(viewIDs[i], templateId));
      }
      return togo;
    }
    
	/*
	 *  1) access this page through "Continue and Add Questions" button on Template page
	 *  2) access this page through links on Control Panel or other
	 *  3) access this page through "Save" button on Template page
	 *  
	 * 
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;
		
	    System.out.println("templateBBean.templateId="+evalViewParams.templateId);
        Long templateId = evalViewParams.templateId;
        
		String templateOTPBinding = "templateBeanLocator." + templateId;
	    String templateOTP = templateOTPBinding+".";	
	    
		UIOutput.make(tofill, "modify-template-title", messageLocator.getMessage("modifytemplate.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
							new SimpleViewParameters(SummaryProducer.VIEW_ID));			
		
		UIForm form = UIForm.make(tofill, "modifyForm"); //$NON-NLS-1$
		
		//preview link
		UIInternalLink.make(form, "preview_eval_link", 
				new PreviewEvalParameters(PreviewEvalProducer.VIEW_ID, null,
						templateId,null, TemplateModifyProducer.VIEW_ID));
	
		UIOutput.make(form, "preview-eval-desc", messageLocator.getMessage("modifytemplate.preview.eval.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "add-item-note", messageLocator.getMessage("modifytemplate.add.item.note")); //$NON-NLS-1$ //$NON-NLS-2$
		
        String[] labels = 
        {
            messageLocator.getMessage("modifytemplate.itemtype.scaled"),
            messageLocator.getMessage("modifytemplate.itemtype.text"),
            messageLocator.getMessage("modifytemplate.itemtype.header"),
            messageLocator.getMessage("modifytemplate.itemtype.block"),
            messageLocator.getMessage("modifytemplate.itemtype.expert")
        };
        String[] viewIDs = {
            ModifyScaledProducer.VIEW_ID, 
            ModifyEssayProducer.VIEW_ID,
            ModifyHeaderProducer.VIEW_ID,
            ModifyBlockProducer.VIEW_ID, 
            ModifyScaledProducer.VIEW_ID // TODO: which view for this
        };
        String[] values = convertViews(viewIDs, templateId);
        
		//dropdown list
		UISelect.make(form, "itemClassification", values, labels,
            values[1], false); //$NON-NLS-1$
		
	    UICommand.make(form, "add_questions", messageLocator.getMessage("modifytemplate.add.item.button")); //$NON-NLS-1$ //$NON-NLS-2$
        
	    List l = itemsLogic.getTemplateItemsForTemplate(templateId,null);
        List templateItemsList = ItemBlockUtils.getNonChildItems(l);//need to get all nonchild items
        
		if (templateItemsList.isEmpty()){
          UIOutput.make(form, "begin-eval-dummylink", messageLocator.getMessage("modifytemplate.begin.eval.link")); 
		}
        else{
          UIInternalLink.make(form, "begin_eval_link", new EvalViewParameters(EvaluationStartProducer.VIEW_ID, 
                templateId));
		}
		
		UIOutput.make(form, "univ-level-header", messageLocator.getMessage("modifytemplate.univ.level.header")); //$NON-NLS-1$ //$NON-NLS-2$			

		UIOutput.make(form, "existing-items", messageLocator.getMessage("modifytemplate.existing.items")); //$NON-NLS-1$ //$NON-NLS-2$
	
		UIOutput.make(form, "template-title-header", messageLocator.getMessage("modifytemplate.template.title.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "title",null,templateOTP+"title");
		
		UIInternalLink.make(form, 
				"modify_title_desc_link", 
				messageLocator.getMessage("modifytemplate.modify.title.desc.link"),
				new EvalViewParameters(
						TemplateProducer.VIEW_ID, 
						templateId));
		
		UIOutput.make(form, "description-header", messageLocator.getMessage("modifytemplate.description.header")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "description",null, templateOTP+"description");	

		UIOutput.make(tofill, "eval-sample", messageLocator.getMessage("modifytemplate.eval.sample")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "course-sample", messageLocator.getMessage("modifytemplate.course.sample")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIForm form2 = UIForm.make(tofill, "modifyFormRows");	 //$NON-NLS-1$
		UICommand reorder = 
          UICommand.make(form2, "hiddenBtn", "#{templateBBean.saveReorder}");
        reorder.parameters.add(new UIELBinding("#{templateBBean.templateId}", 
            templateId));
		
		//UIOutput.make(form,"itemCount",null,"#{templateBBean.itemsListSize}");
		if (templateItemsList!= null && templateItemsList.size() >0) {
            UIVerbatim.make(form2, "decorateSelects", 
            HTMLUtil.emitJavascriptCall("EvalSystem.decorateReorderSelects", 
                    new String[]{"", Integer.toString(templateItemsList.size())} ));
          
			String[] strArr = new String[templateItemsList.size()];
		    for (int h = 0; h < templateItemsList.size(); h++){
		    	strArr[h] = Integer.toString(h+1);
		    }
		    		
		    String templateItemOTPBinding;
		    String templateItemOTP;
		    for (int i = 0; i < templateItemsList.size(); i++){
		    	EvalTemplateItem myTemplateItem = (EvalTemplateItem) templateItemsList.get(i);
		    	EvalItem myItem =  myTemplateItem.getItem();
		    	
		    	templateItemOTPBinding = "templateItemBeanLocator."+myTemplateItem.getId();
		    	templateItemOTP = templateItemOTPBinding+".";
		    	
		    	UIBranchContainer radiobranch = 
                   UIBranchContainer.make(form2,"itemrow:header", Integer.toString(i)); //$NON-NLS-1$
				UIOutput.make(radiobranch, "item-num-header", messageLocator.getMessage("modifytemplate.item.num.header")); //$NON-NLS-1$ //$NON-NLS-2$
				
				//only show Block Check box for scaled type(scale, block)
				if(myItem.getClassification().equals(EvalConstants.ITEM_TYPE_SCALED)){
					UIBranchContainer rbShowBlockCB = UIBranchContainer.make(radiobranch,"showCB:");
					UIBoundBoolean blockCB = UIBoundBoolean.make(rbShowBlockCB, "blockCheckBox", Boolean.FALSE); 
					Map attrmap = new HashMap();
					String name = "block-" + myTemplateItem.getItem().getScale().getId()
							+ "-" + myTemplateItem.getId() ;
					attrmap.put("id", name);
					blockCB.decorators = new DecoratorList(new UIFreeAttributeDecorator(attrmap)); 
				}
					
				//DISPLAY ORDER
				UISelect sl = UISelect.make(radiobranch, "itemNum");
				sl.selection = new UIInput();
				sl.selection.valuebinding = new ELReference(templateItemOTP+"displayOrder");
				UIBoundList slNames = new UIBoundList();
				slNames.setValue(strArr);
				sl.optionnames = slNames;
				UIBoundList slValues = new UIBoundList();
		    	slValues.setValue(strArr);
				sl.optionlist = slValues;
								
				//String itemClassificationLabel = (currItemDisplay.getItem()).getClassification();
				String itemClassificationLabel = myTemplateItem.getItem().getClassification();
				UIOutput.make(radiobranch,"itemClassificationLabel",itemClassificationLabel);
				//String scaleDisplaySettingLabel = (currItemDisplay.getItem()).getScaleDisplaySetting();
				String scaleDisplaySettingLabel = myTemplateItem.getScaleDisplaySetting();
				
				if(scaleDisplaySettingLabel !=null)
					scaleDisplaySettingLabel = "-" + scaleDisplaySettingLabel;
				else scaleDisplaySettingLabel = "";
				UIOutput.make(radiobranch,"scaleDisplaySetting",scaleDisplaySettingLabel);	
				
				//UICommand previewCmd=UICommand.make(radiobranch,"preview_row_item","#{templateBean.previewRowItemAction}");
				//previewCmd.parameters.add(new UIELBinding("#{itemsBean.currTemplateItemId}",Integer.toString(i)));
				UIInternalLink.make(radiobranch, 
						"preview_row_item", 
						messageLocator.getMessage("modifytemplate.preview.link"),
						new TemplateItemViewParameters(
								PreviewItemProducer.VIEW_ID, 
								templateId,
								myTemplateItem.getId()));
                
				//if it is a Block item
				if(myTemplateItem.getBlockParent() != null && myTemplateItem.getBlockParent().booleanValue()== true){
					BlockIdsParameters target = new BlockIdsParameters(ModifyBlockProducer.VIEW_ID,
							templateId,myTemplateItem.getId().toString());  
					UIInternalLink.make(radiobranch, "modify_row_item",
		                        messageLocator.getMessage("modifytemplate.modify.link"),target);
					  
				}else{	//if it is non-block item	              
					  String targetview = EvaluationConstant.classificationToView(myTemplateItem.getItem().getClassification());
		              ViewParameters target = 
		                  new TemplateItemViewParameters(targetview, myTemplateItem.getTemplate().getId(), myTemplateItem.getId()); 
		              UIInternalLink.make(radiobranch, "modify_row_item",
		                        messageLocator.getMessage("modifytemplate.modify.link"),
		                        target);
				}
			

				
				//UICommand removeCmd=UICommand.make(radiobranch,"remove_row_item","#{templateBean.removeRowItemAction}");
				//removeCmd.parameters.add(new UIELBinding("#{itemsBean.currTemplateItemId}",Integer.toString(i)));
                //System.out.println("tiid"+myTemplateItem.getId()+" itemtext: "+myTemplateItem.getItem().getItemText());
				UIInternalLink.make(radiobranch, 
						"remove_row_item", 
						messageLocator.getMessage("modifytemplate.remove.link"),
						new TemplateItemViewParameters(
								RemoveQuestionProducer.VIEW_ID, 
								templateId,
								myTemplateItem.getId()));
				
				UIBranchContainer radiobranch2 = UIBranchContainer.make(form2,"itemrow:text", Integer.toString(i)); //$NON-NLS-1$
				UIOutput.make(radiobranch2,"queNo",Integer.toString(i+1));	 //$NON-NLS-1$
				//UIOutput.make(radiobranch2,"itemText",currItemDisplay.getItem().getItemText());	
				UIOutput.make(radiobranch2,"itemText",myTemplateItem.getItem().getItemText());	
				
				String title = ""; //$NON-NLS-1$				
			//	if(currItemDisplay.getItem().getScale() != null)
			//		title = currItemDisplay.getItem().getScale().getTitle();
				if(myTemplateItem.getItem().getScale() != null)
					title = myTemplateItem.getItem().getScale().getTitle();
				UIOutput.make(radiobranch2,"scaleType",title); //$NON-NLS-1$
				
				//Boolean useNA= currItemDisplay.getItem().getUsesNA();
				Boolean useNA= myTemplateItem.getUsesNA();
				if(useNA != null && useNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(radiobranch2,"showNA:", Integer.toString(i)); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",useNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				//rendering block child items
				if(myTemplateItem.getBlockParent()!= null && myTemplateItem.getBlockParent().booleanValue()== true){
					Integer parentID = new Integer(myTemplateItem.getId().intValue());
					List childList = ItemBlockUtils.getChildItmes(l,parentID);
					for(int k=0; k< childList.size();k++){
						UIBranchContainer childRow = UIBranchContainer.make(form2,"itemrow:blockItems", Integer.toString(k));
						EvalTemplateItem childTI = (EvalTemplateItem)childList.get(k);
						UIOutput.make(childRow,"childItemId",childTI.getDisplayOrder().toString());	
						UIOutput.make(childRow,"childItemText",childTI.getItem().getItemText());
					}		
				}
		    }//end of for loop
		}
		//the create block form
		UIForm blockForm = UIForm.make(tofill, "createBlockForm",new BlockIdsParameters(ModifyBlockProducer.VIEW_ID,templateId));
		UICommand.make(blockForm, "createBlockBtn",messageLocator.getMessage("modifytemplate.createblock.button"),null);
	}

	private String modifyItemLinkType(EvalTemplateItem t){
		if(t.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)){
			return ModifyEssayProducer.VIEW_ID;
		}else if(t.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_HEADER)){
			//"Text Header"
			return ModifyHeaderProducer.VIEW_ID;
		}
		else if(t.getBlockParent()!=null){
			//"Question Block"
			return ModifyBlockProducer.VIEW_ID;
		}else  //for "Scale/Suvey" type
			return ModifyScaledProducer.VIEW_ID;
	}



	

/*	public List reportNavigationCases() {
		List i = new ArrayList();
		
		i.add(new NavigationCase(TemplateItemProducer.VIEW_ID, new SimpleViewParameters(TemplateItemProducer.VIEW_ID)));		
		i.add(new NavigationCase(ModifyEssayProducer.VIEW_ID, new SimpleViewParameters(ModifyEssayProducer.VIEW_ID)));
		i.add(new NavigationCase(ModifyHeaderProducer.VIEW_ID, new SimpleViewParameters(ModifyHeaderProducer.VIEW_ID)));
		i.add(new NavigationCase(ModifyBlockProducer.VIEW_ID, new SimpleViewParameters(ModifyBlockProducer.VIEW_ID)));
		
		i.add(new NavigationCase(TemplateModifyProducer.VIEW_ID, new SimpleViewParameters(TemplateModifyProducer.VIEW_ID)));
		i.add(new NavigationCase(PreviewItemProducer.VIEW_ID, new SimpleViewParameters(PreviewItemProducer.VIEW_ID)));
		i.add(new NavigationCase(RemoveQuestionProducer.VIEW_ID, new SimpleViewParameters(RemoveQuestionProducer.VIEW_ID)));
		
		return i;
	}*/



}




