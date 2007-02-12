/******************************************************************************
 * PreviewItemProducer.java - created on Aug 21, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 * Rui Feng (fengr@vt.edu)
 * Aaron Zeckoski (aaronz@vt.edu) - project lead
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.List;

import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.params.TemplateItemViewParameters;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.utils.TemplateItemUtils;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handle previewing a single item<br/>
 * Refactored to use the item renderers by AZ
 * 
 * @author Rui Feng (fengr@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu) 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class PreviewItemProducer implements ViewComponentProducer, ViewParamsReporter {
	public static final String VIEW_ID = "preview_item";
	public String getViewID() {
		return VIEW_ID;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	ItemRenderer itemRenderer;
	public void setItemRenderer(ItemRenderer itemRenderer) {
		this.itemRenderer = itemRenderer;
	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {	
		TemplateItemViewParameters templateItemViewParams = (TemplateItemViewParameters) viewparams;

		Long templateItemId = templateItemViewParams.templateItemId;		

		EvalTemplateItem templateItem = itemsLogic.getTemplateItemById(templateItemId);
	
		UIOutput.make(tofill, "modify-template-title","Modify Template");
		UIOutput.make(tofill, "modify-template-title", messageLocator.getMessage("modifytemplate.page.title"));
		UIOutput.make(tofill, "preview-item-title", messageLocator.getMessage("previewitem.page.title"));

		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"), 
				new SimpleViewParameters(SummaryProducer.VIEW_ID));

		String itemTypeConstant = TemplateItemUtils.getTemplateItemType(templateItem);
		if( EvalConstants.ITEM_TYPE_BLOCK.equals(itemTypeConstant) ) {
			// handle blocks
			if(templateItem.getScaleDisplaySetting().equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED)){
				UIBranchContainer blockStepped = UIBranchContainer.make(tofill, "blockStepped:");
				
				UIOutput.make(blockStepped, "itemNo",templateItem.getDisplayOrder().toString());
				UIOutput.make(blockStepped, "itemText", templateItem.getItem().getItemText());
				
				Boolean usesNA = templateItem.getUsesNA();
				if(usesNA != null && usesNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(blockStepped,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",usesNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				//Radio Buttons
				EvalScale  scale = templateItem.getItem().getScale();
				String[] scaleOptions = scale.getOptions();
				int optionCount = scaleOptions.length;
				String scaleValues[] = new String[optionCount];
				String scaleLabels[] = new String[optionCount];

				for (int count = 1; count <= optionCount; count++) {
					scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
					scaleLabels[optionCount - count] = scaleOptions[count-1];
				}
				
				UISelect radios = UISelect.make(blockStepped, "dummyRadio", scaleValues, scaleLabels, null, false);
				radios.optionnames = UIOutputMany.make(scaleLabels);		
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radioTopLabelBranch = UIBranchContainer.make(blockStepped, "scaleTopLabelOptions:", Integer.toString(i));
					UISelectLabel.make(radioTopLabelBranch, "dummyTopRadioLabel", selectID, i);
					UILink.make(radioTopLabelBranch, "cornerImage", EvaluationConstant.STEPPED_IMAGE_URLS[0]);
					//This branch container is created to help in creating the middle images after the LABEL
				    for (int k = 0; k < i; ++k) {
				    	UIBranchContainer radioTopLabelAfterBranch = UIBranchContainer.make(radioTopLabelBranch, "scaleTopLabelAfterOptions:", Integer.toString(k));
						UILink.make(radioTopLabelAfterBranch, "middleImage", EvaluationConstant.STEPPED_IMAGE_URLS[1]);
				    }
					UIBranchContainer radioBottomLabelBranch = UIBranchContainer.make(blockStepped, "scaleBottomLabelOptions:", Integer.toString(i));
					UILink.make(radioBottomLabelBranch, "bottomImage", EvaluationConstant.STEPPED_IMAGE_URLS[2]);				
			    }	
					//get child block item text
			    	List childList = itemsLogic.getBlockChildTemplateItemsForBlockParent(templateItem.getId(), false);
			    	for(int j = 0; j< childList.size(); j++){
						UIBranchContainer queRow = UIBranchContainer.make(blockStepped,"queRow:", Integer.toString(j));
						EvalTemplateItem child = (EvalTemplateItem)childList.get(j);
						String txt = child.getItem().getItemText();
						UIOutput.make(queRow,"queNo",Integer.toString(j+1));	
						UIOutput.make(queRow,"queText",txt);
						for(int k=0;k< scaleValues.length; k++){
							UIBranchContainer bc1 = UIBranchContainer.make(queRow, "scaleValueOptions:", Integer.toString(k));
							UISelectChoice.make(bc1, "dummyRadioValue", selectID, k);
						}
						
			    	}
			}else if(templateItem.getScaleDisplaySetting().equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED)){
				UIBranchContainer blockSteppedColored = UIBranchContainer.make(tofill, "blockSteppedColored:");
				
				UIOutput.make(blockSteppedColored, "itemNo",templateItem.getDisplayOrder().toString());
				UIOutput.make(blockSteppedColored, "itemText", templateItem.getItem().getItemText());
				
				Boolean usesNA = templateItem.getUsesNA();
				if(usesNA != null && usesNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(blockSteppedColored,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",usesNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				
				EvalScale  scale = templateItem.getItem().getScale();
				String[] scaleOptions = scale.getOptions();
				int optionCount = scaleOptions.length;
				String scaleValues[] = new String[optionCount];
				String scaleLabels[] = new String[optionCount];

				for (int count = 1; count <= optionCount; count++) {
					scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
					scaleLabels[optionCount - count] = scaleOptions[count-1];
				}
				//Get the scale ideal value (none, low, mid, high )
				String ideal = scale.getIdeal();
				
				String idealImage = "";
				if (ideal ==  null)
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
				else if (ideal.equals("low"))
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
				else if (ideal.equals("mid"))
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
				else
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];
				
				//Radio Buttons
				
				UISelect radios = UISelect.make(blockSteppedColored, "dummyRadio", scaleValues, scaleLabels, null, false);
				radios.optionnames = UIOutputMany.make(scaleLabels);
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) {
					UIBranchContainer rowBranch = UIBranchContainer.make(blockSteppedColored, "rowBranch:", Integer.toString(i));				
				    //Actual label
					UISelectLabel.make(rowBranch, "topLabel", selectID, i);				
					//Corner Image
					UILink.make(rowBranch, "cornerImage", EvaluationConstant.STEPPED_IMAGE_URLS[0]);
					//This branch container is created to help in creating the middle images after the LABEL
				    for (int k = 0; k < i; ++k) 
				    {
				    	UIBranchContainer afterTopLabelBranch = UIBranchContainer.make(rowBranch, "afterTopLabelBranch:", Integer.toString(k));
						UILink.make(afterTopLabelBranch, "middleImage", EvaluationConstant.STEPPED_IMAGE_URLS[1]);
				    }

					UIBranchContainer bottomLabelBranch = UIBranchContainer.make(blockSteppedColored, "bottomLabelBranch:", Integer.toString(i));
					UILink.make(bottomLabelBranch, "bottomImage", EvaluationConstant.STEPPED_IMAGE_URLS[2]);
					
			    }	    
				//get child block item text
		    	//List allItems = itemsLogic.getTemplateItemsForTemplate(templateItem.getTemplate().getId(), null);
		    	//List childList = ItemBlockUtils.getChildItems(allItems,templateItem.getId());
			    List childList = itemsLogic.getBlockChildTemplateItemsForBlockParent(templateItem.getId(), false);
			    for(int j = 0; j< childList.size(); j++){
			    	
					UIBranchContainer queRow = UIBranchContainer.make(blockSteppedColored,"queRow:", Integer.toString(j));
					EvalTemplateItem child = (EvalTemplateItem)childList.get(j);
					String txt = child.getItem().getItemText();
					UIOutput.make(queRow,"queNo",Integer.toString(j+1));	
					UIOutput.make(queRow,"queText",txt);
					UILink.make(queRow, "idealImage", idealImage);
					
					for(int k=0;k< scaleValues.length; k++){
						UIBranchContainer radioBranchFirst = UIBranchContainer.make(queRow, "scaleOptionsFirst:", Integer.toString(k));
						
						UISelectChoice.make(radioBranchFirst, "dummyRadioValueFirst", selectID, k);
						UIBranchContainer radioBranchSecond = UIBranchContainer.make(queRow, "scaleOptionsSecond:", Integer.toString(k));
						UISelectChoice.make(radioBranchSecond, "dummyRadioValueSecond", selectID, k);
					}
				}
				
				
				
			}		
			//throw new IllegalStateException("No code in place to handle block rendering for preview yet");

		} else {
			// use the renderer evolver
			itemRenderer.renderItem(tofill, "previewed-item:", null, templateItem, 0, true);
		}

		UIOutput.make(tofill, "close-button", messageLocator.getMessage("general.close.window.button"));

	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters() {
		return new TemplateItemViewParameters();
	}

}