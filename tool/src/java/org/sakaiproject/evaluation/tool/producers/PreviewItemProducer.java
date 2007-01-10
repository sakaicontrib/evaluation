/******************************************************************************
 * PreviewItemProducer.java - created by fengr@vt.edu on Aug 21, 2006
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

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.TemplateBean;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIColourDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Preview all kinds of Item: Block, Scaled/Survey, Short Answer/essay, Header type
 * 
 * @author: Rui Feng (fengr@vt.edu)
 * @author:Kapil Ahuja (kahuja@vt.edu)
 */

public class PreviewItemProducer implements ViewComponentProducer {
	public static final String VIEW_ID = "preview_item";
	
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
		UIOutput.make(tofill, "modify-template-title","Modify Template");
		//UIOutput.make(tofill, "modify-template-title", messageLocator.getMessage("templatemodify.page.title"));//TODO: exception: can not get property
		UIOutput.make(tofill, "preview-item-title", messageLocator.getMessage("previewitem.page.title"));
		
		
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"), 
				new SimpleViewParameters(SummaryProducer.VIEW_ID));		
		
		
		if(templateBean.itemClassification.equals(EvalConstants.ITEM_TYPE_SCALED)){//"Scaled/Survey"
			
			EvalScale  scale = templateBean.itemPreview.getScale();
			String[] scaleOptions = scale.getOptions();
			int optionCount = scaleOptions.length;
			String scaleValues[] = new String[optionCount];
			String scaleLabels[] = new String[optionCount];
			
			if (templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_COMPACT)) {//"Compact"
				
				UIBranchContainer compact = UIBranchContainer.make(tofill, "compactDisplay:");

				//Item text
				UIOutput.make(compact, "queNo",null,"#{templateBean.currItemNo}");
				UIOutput.make(compact, "itemText", null, "#{templateBean.itemText}");

				//Start label
				String compactDisplayStart = scaleOptions[0];		
				String compactDisplayEnd = scaleOptions[optionCount - 1];

				for (int count = 0; count < optionCount; count++) {
					
					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = " ";
				}
				
				//UIOutput.make(compact, "compactDisplayStart", (templateBean.itemDisplayPreview).getCompactDisplayStart());			
				UIOutput.make(compact, "compactDisplayStart",compactDisplayStart);
				
				//Radio Buttons
				//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
				//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();

				UISelect radios = UISelect.make(compact, "dummyRadio", scaleValues, scaleLabels, null, false);
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radiobranch = UIBranchContainer.make(compact, "scaleOptions:", Integer.toString(i));
					UISelectChoice.make(radiobranch, "dummyRadioValue", selectID, i);
			    }
				
				//End label
				//UIOutput.make(compact, "compactDisplayEnd", (templateBean.itemDisplayPreview).getCompactDisplayEnd());			
			    UIOutput.make(compact, "compactDisplayEnd", compactDisplayEnd);	
			    if(templateBean.itemNA !=null && templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(compact,"showNA:");
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA);
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));
				}
				
			}else if (templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED)) {//"Compact Colored"

				UIBranchContainer compactColored = UIBranchContainer.make(tofill, "compactDisplayColored:");

				//Item text
				UIOutput.make(compactColored, "queNo",null,"#{templateBean.currItemNo}");
				UIOutput.make(compactColored, "itemText", null, "#{templateBean.itemText}");
				
				//Get the scale ideal value (none, low, mid, high )
				//String ideal = templateBean.itemDisplayPreview.getItem().getScale().getIdeal(); 
				String ideal = scale.getIdeal();
				
				//Compact start and end label containers
				UIBranchContainer compactStartContainer = UIBranchContainer.make(compactColored, "compactStartContainer:");
				UIBranchContainer compactEndContainer = UIBranchContainer.make(compactColored, "compactEndContainer:");

				//Finding the colors if compact start and end labels
				Color startColor = null;
				Color endColor = null;

				//When no ideal is specified then just plain blue for both start and end
				if (ideal ==  null) {
					startColor = Color.decode(EvaluationConstant.BLUE_COLOR);
				    endColor =  Color.decode(EvaluationConstant.BLUE_COLOR);
				}
				else if (ideal.equals("low")) {
				    startColor =  Color.decode(EvaluationConstant.GREEN_COLOR);
				    endColor =  Color.decode(EvaluationConstant.RED_COLOR);
				}
				else if (ideal.equals("mid")) {
				    startColor =  Color.decode(EvaluationConstant.RED_COLOR);
				    endColor =  Color.decode(EvaluationConstant.RED_COLOR);
				}
				//This for the case when high is the ideal
				else {
				    startColor =  Color.decode(EvaluationConstant.RED_COLOR);
				    endColor =  Color.decode(EvaluationConstant.GREEN_COLOR);
				}
				
			    compactStartContainer.decorators = new DecoratorList(new UIColourDecorator(null, startColor));			
			    compactEndContainer.decorators = new DecoratorList(new UIColourDecorator(null, endColor));
		    
				//Start label
				String compactDisplayStart = scaleOptions[0];		
				String compactDisplayEnd = scaleOptions[optionCount - 1];
				for (int count = 0; count < optionCount; count++) {
					
					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = " ";
				}
			    
				//Compact start and end actual labels
				//UIOutput.make(compactStartContainer, "compactDisplayStart", (templateBean.itemDisplayPreview).getCompactDisplayStart());			
				//UIOutput.make(compactEndContainer, "compactDisplayEnd", (templateBean.itemDisplayPreview).getCompactDisplayEnd());
				UIOutput.make(compactStartContainer, "compactDisplayStart",compactDisplayStart);	
				UIOutput.make(compactEndContainer, "compactDisplayEnd", compactDisplayEnd);
				
			    //For the radio buttons
				UIBranchContainer compactRadioContainer = UIBranchContainer.make(compactColored, "compactRadioContainer:");
				
				String idealImage = "";
				if (ideal ==  null)
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
				else if (ideal.equals("low"))
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
				else if (ideal.equals("mid"))
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
				else
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];
				
				UILink.make(compactRadioContainer, "idealImage", idealImage);
				
				//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
				//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
				UISelect radios = UISelect.make(compactRadioContainer, "dummyRadio", scaleValues, scaleLabels, null, false);
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radioBranchFirst = UIBranchContainer.make(compactRadioContainer, "scaleOptionsFirst:", Integer.toString(i));
					UISelectChoice.make(radioBranchFirst, "dummyRadioValueFirst", selectID, i);

					UIBranchContainer radioBranchSecond = UIBranchContainer.make(compactRadioContainer, "scaleOptionsSecond:", Integer.toString(i));
					UISelectChoice.make(radioBranchSecond, "dummyRadioValueSecond", selectID, i);
			    }
				
			    if(templateBean.itemNA !=null && templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(compactColored,"showNA:");
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA);
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));

				}
			}else if (templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL)) {//"Full"
				
				UIBranchContainer full = UIBranchContainer.make(tofill, "fullDisplay:");

				//Item text
				UIOutput.make(full, "queNo",null,"#{templateBean.currItemNo}");
				UIOutput.make(full, "itemText", null, "#{templateBean.itemText}");
				if( templateBean.itemNA !=null && templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(full,"showNA:");
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA);
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));

				}
				//Radio Buttons
				//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
				//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
				for (int count = 0; count < optionCount; count++) {

					//TODO: Check if the order here is same as in SCALE_OPTIONS_INDEX in SCALE_OPTIONS table.
					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = scaleOptions[count];	
				}
				
				UISelect radios = UISelect.make(full, "dummyRadio", scaleValues, scaleLabels, null, false);				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radiobranch = UIBranchContainer.make(full, "scaleOptions:", Integer.toString(i));
					UISelectChoice.make(radiobranch, "dummyRadioValue", selectID, i);
					UISelectLabel.make(radiobranch, "dummyRadioLabel", selectID, i);
			    }
				
			}else if (templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED)) {//"Full Colored"
				
				UIBranchContainer fullColored = UIBranchContainer.make(tofill, "fullDisplayColored:");

				//Item text
				UIOutput.make(fullColored, "queNo",null,"#{templateBean.currItemNo}");
				UIOutput.make(fullColored, "itemText", null, "#{templateBean.itemText}");
				if(templateBean.itemNA !=null && templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(fullColored,"showNA:");
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA);
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));

				}
				//Get the scale ideal value (none, low, mid, high )
				//String ideal = templateBean.itemDisplayPreview.getItem().getScale().getIdeal(); 
				String ideal = scale.getIdeal(); 
				
				//Set the ideal image
				String idealImage = "";
				if (ideal ==  null)
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
				else if (ideal.equals("low"))
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
				else if (ideal.equals("mid"))
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
				else
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];
				
				UILink.make(fullColored, "idealImage", idealImage);
				UILink.make(fullColored, "idealImageSafari", idealImage);
				
				//Radio Buttons
				//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
				//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
				for (int count = 0; count < optionCount; count++) {

					//TODO: Check if the order here is same as in SCALE_OPTIONS_INDEX in SCALE_OPTIONS table.
					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = scaleOptions[count];	
				}
				
				UISelect radios = UISelect.make(fullColored, "dummyRadio", scaleValues, scaleLabels, null, false);
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radiobranch = UIBranchContainer.make(fullColored, "scaleOptions:", Integer.toString(i));
					UISelectChoice.make(radiobranch, "dummyRadioValue", selectID, i);
					UISelectLabel.make(radiobranch, "dummyRadioLabel", selectID, i);
			    }
				
			}else if (templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED)) {//"Stepped"
				
				UIBranchContainer stepped = UIBranchContainer.make(tofill, "steppedDisplay:");
				
				//Item text
				UIOutput.make(stepped, "queNo",null,"#{templateBean.currItemNo}");
				UIOutput.make(stepped, "itemText", null, "#{templateBean.itemText}");
				if(templateBean.itemNA !=null && templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(stepped,"showNA:");
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA);
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));

				}
				//Radio Buttons
				//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
				//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
				for (int count = 1; count <= optionCount; count++) {
					scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
					scaleLabels[optionCount - count] = scaleOptions[count-1];
				}
				
				UISelect radios = UISelect.make(stepped, "dummyRadio", scaleValues, scaleLabels, null, false);
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radioTopLabelBranch = UIBranchContainer.make(stepped, "scaleTopLabelOptions:", Integer.toString(i));
					UISelectLabel.make(radioTopLabelBranch, "dummyTopRadioLabel", selectID, i);
					UILink.make(radioTopLabelBranch, "cornerImage", EvaluationConstant.STEPPED_IMAGE_URLS[0]);

					//This branch container is created to help in creating the middle images after the LABEL
				    for (int k = 0; k < i; ++k) 
				    {
				    	UIBranchContainer radioTopLabelAfterBranch = UIBranchContainer.make(radioTopLabelBranch, "scaleTopLabelAfterOptions:", Integer.toString(k));
						UILink.make(radioTopLabelAfterBranch, "middleImage", EvaluationConstant.STEPPED_IMAGE_URLS[1]);
				    }

					UIBranchContainer radioBottomLabelBranch = UIBranchContainer.make(stepped, "scaleBottomLabelOptions:", Integer.toString(i));
					UILink.make(radioBottomLabelBranch, "bottomImage", EvaluationConstant.STEPPED_IMAGE_URLS[2]);
					
					UIBranchContainer radioValueBranch = UIBranchContainer.make(stepped, "scaleValueOptions:", Integer.toString(i));
					UISelectChoice.make(radioValueBranch, "dummyRadioValue", selectID, i);
			    }
				
			}else if (templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED)) {//"Stepped Colored"

				UIBranchContainer steppedColored = UIBranchContainer.make(tofill, "steppedDisplayColored:");

				//Item text
				UIOutput.make(steppedColored, "queNo",null,"#{templateBean.currItemNo}");
				UIOutput.make(steppedColored, "itemText", null, "#{templateBean.itemText}");
				if( templateBean.itemNA !=null && templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(steppedColored,"showNA:");
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA);
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));

				}
				//Get the scale ideal value (none, low, mid, high )
				//String ideal = templateBean.itemDisplayPreview.getItem().getScale().getIdeal();
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
				
				UILink.make(steppedColored, "idealImage", idealImage);
				
				//Radio Buttons
				//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
				//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
				for (int count = 1; count <= optionCount; count++) {
		
					scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
					scaleLabels[optionCount - count] = scaleOptions[count-1];
				}
				
				UISelect radios = UISelect.make(steppedColored, "dummyRadio", scaleValues, scaleLabels, null, false);
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer rowBranch = UIBranchContainer.make(steppedColored, "rowBranch:", Integer.toString(i));
					
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

					UIBranchContainer bottomLabelBranch = UIBranchContainer.make(steppedColored, "bottomLabelBranch:", Integer.toString(i));
					UILink.make(bottomLabelBranch, "bottomImage", EvaluationConstant.STEPPED_IMAGE_URLS[2]);
					
					UIBranchContainer radioBranchFirst = UIBranchContainer.make(steppedColored, "scaleOptionsFirst:", Integer.toString(i));
					UISelectChoice.make(radioBranchFirst, "dummyRadioValueFirst", selectID, i);

					UIBranchContainer radioBranchSecond = UIBranchContainer.make(steppedColored, "scaleOptionsSecond:", Integer.toString(i));
					UISelectChoice.make(radioBranchSecond, "dummyRadioValueSecond", selectID, i);
			    }
				
			}else {
				//This is for vertical			
				UIBranchContainer vertical = UIBranchContainer.make(tofill, "verticalDisplay:");
				//Item text
				UIOutput.make(vertical, "queNo",null,"#{templateBean.currItemNo}");
				UIOutput.make(vertical, "itemText", null, "#{templateBean.itemText}");
				if(templateBean.itemNA !=null &&templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(vertical,"showNA:");
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA);
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));

				}
				//Radio Buttons
				//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
				//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
				for (int count = 0; count < optionCount; count++) {

					//TODO: Check if the order here is same as in SCALE_OPTIONS_INDEX in SCALE_OPTIONS table.
					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = scaleOptions[count];	
				}
				
				UISelect radios = UISelect.make(vertical, "dummyRadio", scaleValues, scaleLabels, null, false);
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radiobranch = UIBranchContainer.make(vertical, "scaleOptions:", Integer.toString(i));
					UISelectChoice.make(radiobranch, "dummyRadioValue", selectID, i);
					UISelectLabel.make(radiobranch, "dummyRadioLabel", selectID, i);
			    }
				
			}
		}else if(templateBean.itemClassification.equals(EvalConstants.ITEM_TYPE_TEXT)){//"Short Answer/Essay"
			UIBranchContainer essay = UIBranchContainer.make(tofill, "essayType:");
			//Item text
			UIOutput.make(essay, "queNo",null,"#{templateBean.currItemNo}");
			UIOutput.make(essay, "itemText", null, "#{templateBean.itemText}");
			if(templateBean.itemNA !=null && templateBean.itemNA.booleanValue()== true){
				UIBranchContainer radiobranch3 = UIBranchContainer.make(essay,"showNA:");
				UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA);
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));

			}
			
			UIInput textarea = UIInput.make(essay,"essayBox",null );
			Map attrmap = new HashMap();
			String rowNum = templateBean.displayRows.toString();
			attrmap.put("rows", rowNum);
			textarea.decorators = new DecoratorList(new UIFreeAttributeDecorator(attrmap)); 
  
		}else if(templateBean.itemClassification.equals(EvalConstants.ITEM_TYPE_HEADER)){//"Text Header"
			UIBranchContainer header = UIBranchContainer.make(tofill, "headerType:");
			UIOutput.make(header, "queNo",null,"#{templateBean.currItemNo}");
			UIOutput.make(header, "itemText", null, "#{templateBean.itemText}");
	
		}else if(templateBean.itemClassification.equals(EvalConstants.ITEM_TYPE_BLOCK)
				&& templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED)){
			//"Question Block","Stepped"
			UIBranchContainer blockStepped = UIBranchContainer.make(tofill, "blockStepped:");
			
			UIOutput.make(blockStepped, "itemNo",null,"#{templateBean.currItemNo}");
			UIOutput.make(blockStepped, "itemText", null, "#{templateBean.itemText}");
			if(templateBean.itemNA !=null && templateBean.itemNA.booleanValue()== true){
				UIBranchContainer radiobranch3 = UIBranchContainer.make(blockStepped,"showNA:");
				UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA);
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));

			}
			//Radio Buttons
			//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
			//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
			EvalScale  scale = templateBean.itemPreview.getScale();
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
		    	for(int j = 0; j< templateBean.queList.size(); j++){
					UIBranchContainer queRow = UIBranchContainer.make(blockStepped,"queRow:", Integer.toString(j));
					String txt = (String)templateBean.queList.get(j);
					UIOutput.make(queRow,"queNo",Integer.toString(j+1));	
					UIOutput.make(queRow,"queText",txt);
					for(int k=0;k< scaleValues.length; k++){
						UIBranchContainer bc1 = UIBranchContainer.make(queRow, "scaleValueOptions:", Integer.toString(k));
						UISelectChoice.make(bc1, "dummyRadioValue", selectID, k);
					}
					
		    	}
		
		}
		else if(templateBean.itemClassification.equals(EvalConstants.ITEM_TYPE_BLOCK)&&
				templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED)){
			//"Question Block","Stepped Colored"
			UIBranchContainer blockSteppedColored = UIBranchContainer.make(tofill, "blockSteppedColored:");
			UIOutput.make(blockSteppedColored, "itemNo",null,"#{templateBean.currItemNo}");
			UIOutput.make(blockSteppedColored, "itemText", null, "#{templateBean.itemText}");
			if( templateBean.itemNA != null && templateBean.itemNA.booleanValue()== true){
				UIBranchContainer radiobranch3 = UIBranchContainer.make(blockSteppedColored,"showNA:");
				UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA);
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));

			}
			
			EvalScale  scale = templateBean.itemPreview.getScale();
			String[] scaleOptions = scale.getOptions();
			int optionCount = scaleOptions.length;
			String scaleValues[] = new String[optionCount];
			String scaleLabels[] = new String[optionCount];

			for (int count = 1; count <= optionCount; count++) {
				scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
				scaleLabels[optionCount - count] = scaleOptions[count-1];
			}
			//Get the scale ideal value (none, low, mid, high )
			//String ideal = templateBean.itemDisplayPreview.getItem().getScale().getIdeal();
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
		//	UILink.make(blockSteppedColored, "idealImage", idealImage);
			
			//Radio Buttons
			//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
			//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
			
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
		    for(int j = 0; j< templateBean.queList.size(); j++){
		    	
				UIBranchContainer queRow = UIBranchContainer.make(blockSteppedColored,"queRow:", Integer.toString(j));
				String txt = (String)templateBean.queList.get(j);
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
		
		UIOutput.make(tofill, "close-button", messageLocator.getMessage("general.close.window.button"));
		
	}
}