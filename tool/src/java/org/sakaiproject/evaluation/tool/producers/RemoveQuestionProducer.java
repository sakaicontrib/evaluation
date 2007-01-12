/******************************************************************************
 * RemoveQuestionProducer.java - created by fengr@vt.edu on Sep 26, 2006
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.TemplateBean;


import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
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
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * This page is to remove an Item(all kind of Item type) from DAO
 * 
 * @author: Rui Feng (fengr@vt.edu)
 */

public class RemoveQuestionProducer implements ViewComponentProducer, NavigationCaseReporter {
	public static final String VIEW_ID = "remove_question"; //$NON-NLS-1$
	
	public String getViewID() {
		return VIEW_ID;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	
	private TemplateBean templateBean;
	public void setTemplateBean(TemplateBean templateBean) {
		this.templateBean = templateBean;
	}


	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {	

		UIOutput.make(tofill, "remove-question-title", messageLocator.getMessage("removequestion.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "modify-template-title", messageLocator.getMessage("modifytemplate.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));	
		
		UIForm form = UIForm.make(tofill, "removeQuestionForm"); //$NON-NLS-1$
		
		UIOutput.make(form, "remove-question-confirm-pre-name", messageLocator.getMessage("removequestion.confirm.pre.name")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "remove-question-confirm-post-name", messageLocator.getMessage("removequestion.confirm.post.name")); //$NON-NLS-1$ //$NON-NLS-2$
		
		if(templateBean.itemClassification.equals(EvalConstants.ITEM_TYPE_SCALED)){ //"Scaled/Survey"
			EvalScale  scale = templateBean.itemPreview.getScale();
			String[] scaleOptions = scale.getOptions();
			int optionCount = scaleOptions.length;
			String scaleValues[] = new String[optionCount];
			String scaleLabels[] = new String[optionCount];
			
			
			if (templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_COMPACT)) { //"Compact"
				
				UIBranchContainer compact = UIBranchContainer.make(tofill, "compactDisplay:"); //$NON-NLS-1$

				//Item text
				UIOutput.make(compact, "queNo",null,"#{templateBean.currItemNo}"); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(compact, "itemText", null, "#{templateBean.itemText}"); //$NON-NLS-1$ //$NON-NLS-2$
				
				//Start label
				String compactDisplayStart = scaleOptions[0];		
				String compactDisplayEnd = scaleOptions[optionCount - 1];

				for (int count = 0; count < optionCount; count++) {
					
					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = " ";
				}
				//Start label	
				UIOutput.make(compact, "compactDisplayStart",compactDisplayStart);			 //$NON-NLS-1$
				
				//Radio Buttons
				
				UISelect radios = UISelect.make(compact, "dummyRadio", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radiobranch = UIBranchContainer.make(compact, "scaleOptions:", Integer.toString(i)); //$NON-NLS-1$
					UISelectChoice.make(radiobranch, "dummyRadioValue", selectID, i); //$NON-NLS-1$
			    }
				
				//End label
				UIOutput.make(compact, "compactDisplayEnd", compactDisplayEnd);			 //$NON-NLS-1$
				
				if(templateBean.itemNA != null && templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(compact,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
			}else if (templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED)) { //"Compact Colored"

				UIBranchContainer compactColored = UIBranchContainer.make(tofill, "compactDisplayColored:"); //$NON-NLS-1$

				//Item text
				UIOutput.make(compactColored, "queNo",null,"#{templateBean.currItemNo}"); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(compactColored, "itemText", null, "#{templateBean.itemText}"); //$NON-NLS-1$ //$NON-NLS-2$
				
				//Get the scale ideal value (none, low, mid, high )
				String ideal = scale.getIdeal(); 

				//Compact start and end label containers
				UIBranchContainer compactStartContainer = UIBranchContainer.make(compactColored, "compactStartContainer:"); //$NON-NLS-1$
				UIBranchContainer compactEndContainer = UIBranchContainer.make(compactColored, "compactEndContainer:"); //$NON-NLS-1$

				//Finding the colors if compact start and end labels
				Color startColor = null;
				Color endColor = null;

				//When no ideal is specified then just plain blue for both start and end
				if (ideal ==  null) {
					startColor = Color.decode(EvaluationConstant.BLUE_COLOR);
				    endColor =  Color.decode(EvaluationConstant.BLUE_COLOR);
				}
				else if (ideal.equals("low")) { //$NON-NLS-1$
				    startColor =  Color.decode(EvaluationConstant.GREEN_COLOR);
				    endColor =  Color.decode(EvaluationConstant.RED_COLOR);
				}
				else if (ideal.equals("mid")) { //$NON-NLS-1$
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
			   
			    String compactDisplayStart = scaleOptions[0];		
				String compactDisplayEnd = scaleOptions[optionCount - 1];
				for (int count = 0; count < optionCount; count++) {
					
					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = " ";
				}
			    
				//Compact start and end actual labels
				UIOutput.make(compactStartContainer, "compactDisplayStart", compactDisplayStart);			 //$NON-NLS-1$
				UIOutput.make(compactEndContainer, "compactDisplayEnd", compactDisplayEnd); //$NON-NLS-1$
				
			    //For the radio buttons
				UIBranchContainer compactRadioContainer = UIBranchContainer.make(compactColored, "compactRadioContainer:"); //$NON-NLS-1$
				
				String idealImage = ""; //$NON-NLS-1$
				if (ideal ==  null)
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
				else if (ideal.equals("low")) //$NON-NLS-1$
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
				else if (ideal.equals("mid")) //$NON-NLS-1$
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
				else
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];
				
				UILink.make(compactRadioContainer, "idealImage", idealImage); //$NON-NLS-1$
				
			//	String values [] = (templateBean.itemDisplayPreview).getScaleValues();
			//	String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
				UISelect radios = UISelect.make(compactRadioContainer, "dummyRadio", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radioBranchFirst = UIBranchContainer.make(compactRadioContainer, "scaleOptionsFirst:", Integer.toString(i)); //$NON-NLS-1$
					UISelectChoice.make(radioBranchFirst, "dummyRadioValueFirst", selectID, i); //$NON-NLS-1$

					UIBranchContainer radioBranchSecond = UIBranchContainer.make(compactRadioContainer, "scaleOptionsSecond:", Integer.toString(i)); //$NON-NLS-1$
					UISelectChoice.make(radioBranchSecond, "dummyRadioValueSecond", selectID, i); //$NON-NLS-1$
			    }
				
			    if(templateBean.itemNA != null && templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(compactColored,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}else if (templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL)) { //"Full"
				
				UIBranchContainer full = UIBranchContainer.make(tofill, "fullDisplay:"); //$NON-NLS-1$

				//Item text
				UIOutput.make(full, "queNo",null,"#{templateBean.currItemNo}"); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(full, "itemText", null, "#{templateBean.itemText}"); //$NON-NLS-1$ //$NON-NLS-2$
				if(templateBean.itemNA != null && templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(full,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				for (int count = 0; count < optionCount; count++) {

					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = scaleOptions[count];	
				}
				//Radio Buttons
				//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
				//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
				UISelect radios = UISelect.make(full, "dummyRadio", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radiobranch = UIBranchContainer.make(full, "scaleOptions:", Integer.toString(i)); //$NON-NLS-1$
					UISelectChoice.make(radiobranch, "dummyRadioValue", selectID, i); //$NON-NLS-1$
					UISelectLabel.make(radiobranch, "dummyRadioLabel", selectID, i); //$NON-NLS-1$
			    }
				
			}else if (templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED)) { //"Full Colored"
				
				UIBranchContainer fullColored = UIBranchContainer.make(tofill, "fullDisplayColored:"); //$NON-NLS-1$

				//Item text
				UIOutput.make(fullColored, "queNo",null,"#{templateBean.currItemNo}"); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(fullColored, "itemText", null, "#{templateBean.itemText}"); //$NON-NLS-1$ //$NON-NLS-2$
				if(templateBean.itemNA != null && templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(fullColored,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				//Get the scale ideal value (none, low, mid, high )
				String ideal = scale.getIdeal();//templateBean.itemDisplayPreview.getItem().getScale().getIdeal(); 

				//Set the ideal image
				String idealImage = ""; //$NON-NLS-1$
				if (ideal ==  null)
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
				else if (ideal.equals("low")) //$NON-NLS-1$
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
				else if (ideal.equals("mid")) //$NON-NLS-1$
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
				else
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];
				
				UILink.make(fullColored, "idealImage", idealImage); //$NON-NLS-1$
				UILink.make(fullColored, "idealImageSafari", idealImage); //$NON-NLS-1$
				
				//Radio Buttons
				for (int count = 0; count < optionCount; count++) {

					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = scaleOptions[count];	
				}
				//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
				//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
				UISelect radios = UISelect.make(fullColored, "dummyRadio", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radiobranch = UIBranchContainer.make(fullColored, "scaleOptions:", Integer.toString(i)); //$NON-NLS-1$
					UISelectChoice.make(radiobranch, "dummyRadioValue", selectID, i); //$NON-NLS-1$
					UISelectLabel.make(radiobranch, "dummyRadioLabel", selectID, i); //$NON-NLS-1$
			    }
				
			}else if (templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED)) { //stepped
				
				UIBranchContainer stepped = UIBranchContainer.make(tofill, "steppedDisplay:"); //$NON-NLS-1$
				
				//Item text
				UIOutput.make(stepped, "queNo",null,"#{templateBean.currItemNo}"); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(stepped, "itemText", null, "#{templateBean.itemText}"); //$NON-NLS-1$ //$NON-NLS-2$
				if(templateBean.itemNA != null && templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(stepped,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				for (int count = 1; count <= optionCount; count++) {
					scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
					scaleLabels[optionCount - count] = scaleOptions[count-1];
				}
				
				//Radio Buttons
				//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
				//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
				UISelect radios = UISelect.make(stepped, "dummyRadio", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radioTopLabelBranch = UIBranchContainer.make(stepped, "scaleTopLabelOptions:", Integer.toString(i)); //$NON-NLS-1$
					UISelectLabel.make(radioTopLabelBranch, "dummyTopRadioLabel", selectID, i); //$NON-NLS-1$
					UILink.make(radioTopLabelBranch, "cornerImage", EvaluationConstant.STEPPED_IMAGE_URLS[0]); //$NON-NLS-1$

					//This branch container is created to help in creating the middle images after the LABEL
				    for (int k = 0; k < i; ++k) 
				    {
				    	UIBranchContainer radioTopLabelAfterBranch = UIBranchContainer.make(radioTopLabelBranch, "scaleTopLabelAfterOptions:", Integer.toString(k)); //$NON-NLS-1$
						UILink.make(radioTopLabelAfterBranch, "middleImage", EvaluationConstant.STEPPED_IMAGE_URLS[1]); //$NON-NLS-1$
				    }

					UIBranchContainer radioBottomLabelBranch = UIBranchContainer.make(stepped, "scaleBottomLabelOptions:", Integer.toString(i)); //$NON-NLS-1$
					UILink.make(radioBottomLabelBranch, "bottomImage", EvaluationConstant.STEPPED_IMAGE_URLS[2]); //$NON-NLS-1$
					
					UIBranchContainer radioValueBranch = UIBranchContainer.make(stepped, "scaleValueOptions:", Integer.toString(i)); //$NON-NLS-1$
					UISelectChoice.make(radioValueBranch, "dummyRadioValue", selectID, i); //$NON-NLS-1$
			    }
				
			}else if (templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED)) { //"Stepped Colored"

				UIBranchContainer steppedColored = UIBranchContainer.make(tofill, "steppedDisplayColored:"); //$NON-NLS-1$

				//Item text
				UIOutput.make(steppedColored, "queNo",null,"#{templateBean.currItemNo}"); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(steppedColored, "itemText", null, "#{templateBean.itemText}"); //$NON-NLS-1$ //$NON-NLS-2$
				if(templateBean.itemNA != null && templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(steppedColored,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				//Get the scale ideal value (none, low, mid, high )
				String ideal = scale.getIdeal(); //templateBean.itemDisplayPreview.getItem().getScale().getIdeal();
				
				String idealImage = ""; //$NON-NLS-1$
				if (ideal ==  null)
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
				else if (ideal.equals("low")) //$NON-NLS-1$
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
				else if (ideal.equals("mid")) //$NON-NLS-1$
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
				else
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];
				
				UILink.make(steppedColored, "idealImage", idealImage); //$NON-NLS-1$
				
				for (int count = 1; count <= optionCount; count++) {
					
					scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
					scaleLabels[optionCount - count] = scaleOptions[count-1];
				}
				//Radio Buttons
				//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
				//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
				UISelect radios = UISelect.make(steppedColored, "dummyRadio", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer rowBranch = UIBranchContainer.make(steppedColored, "rowBranch:", Integer.toString(i)); //$NON-NLS-1$
					
				    //Actual label
					UISelectLabel.make(rowBranch, "topLabel", selectID, i); //$NON-NLS-1$
					
					//Corner Image
					UILink.make(rowBranch, "cornerImage", EvaluationConstant.STEPPED_IMAGE_URLS[0]); //$NON-NLS-1$

					//This branch container is created to help in creating the middle images after the LABEL
				    for (int k = 0; k < i; ++k) 
				    {
				    	UIBranchContainer afterTopLabelBranch = UIBranchContainer.make(rowBranch, "afterTopLabelBranch:", Integer.toString(k)); //$NON-NLS-1$
						UILink.make(afterTopLabelBranch, "middleImage", EvaluationConstant.STEPPED_IMAGE_URLS[1]); //$NON-NLS-1$
				    }

					UIBranchContainer bottomLabelBranch = UIBranchContainer.make(steppedColored, "bottomLabelBranch:", Integer.toString(i)); //$NON-NLS-1$
					UILink.make(bottomLabelBranch, "bottomImage", EvaluationConstant.STEPPED_IMAGE_URLS[2]); //$NON-NLS-1$
					
					UIBranchContainer radioBranchFirst = UIBranchContainer.make(steppedColored, "scaleOptionsFirst:", Integer.toString(i)); //$NON-NLS-1$
					UISelectChoice.make(radioBranchFirst, "dummyRadioValueFirst", selectID, i); //$NON-NLS-1$

					UIBranchContainer radioBranchSecond = UIBranchContainer.make(steppedColored, "scaleOptionsSecond:", Integer.toString(i)); //$NON-NLS-1$
					UISelectChoice.make(radioBranchSecond, "dummyRadioValueSecond", selectID, i); //$NON-NLS-1$
			    }
				
			}else {
				//This is for vertical			
				UIBranchContainer vertical = UIBranchContainer.make(tofill, "verticalDisplay:"); //$NON-NLS-1$
				//Item text
				UIOutput.make(vertical, "queNo",null,"#{templateBean.currItemNo}"); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(vertical, "itemText", null, "#{templateBean.itemText}"); //$NON-NLS-1$ //$NON-NLS-2$
				if(templateBean.itemNA != null && templateBean.itemNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(vertical,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				for (int count = 0; count < optionCount; count++) {

					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = scaleOptions[count];	
				}
				
				//Radio Buttons
				//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
				//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
				UISelect radios = UISelect.make(vertical, "dummyRadio", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radiobranch = UIBranchContainer.make(vertical, "scaleOptions:", Integer.toString(i)); //$NON-NLS-1$
					UISelectChoice.make(radiobranch, "dummyRadioValue", selectID, i); //$NON-NLS-1$
					UISelectLabel.make(radiobranch, "dummyRadioLabel", selectID, i); //$NON-NLS-1$
			    }
				
			}
		}else if(templateBean.itemClassification.equals(EvalConstants.ITEM_TYPE_TEXT)){ //"Short Answer/Essay"
			UIBranchContainer essay = UIBranchContainer.make(tofill, "essayType:"); //$NON-NLS-1$
			//Item text
			UIOutput.make(essay, "queNo",null,"#{templateBean.currItemNo}"); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(essay, "itemText", null, "#{templateBean.itemText}"); //$NON-NLS-1$ //$NON-NLS-2$
			if(templateBean.itemNA != null && templateBean.itemNA.booleanValue()== true){
				UIBranchContainer radiobranch3 = UIBranchContainer.make(essay,"showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA); //$NON-NLS-1$
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			UIInput textarea = UIInput.make(essay,"essayBox",null ); //$NON-NLS-1$
			Map attrmap = new HashMap();
			
			String rowNum; 
			if (templateBean.displayRows == null)
				rowNum = EvaluationConstant.DEFAULT_ROWS.toString();
			else
				rowNum = templateBean.displayRows.toString();
			
			attrmap.put("rows", rowNum); //$NON-NLS-1$
			textarea.decorators = new DecoratorList(new UIFreeAttributeDecorator(attrmap)); 
			
		}else if(templateBean.itemClassification.equals(EvalConstants.ITEM_TYPE_HEADER)){ //"Text Header"
			UIBranchContainer header = UIBranchContainer.make(tofill, "essayType:"); //$NON-NLS-1$
			UIOutput.make(header, "queNo",null,"#{templateBean.currItemNo}"); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(header, "itemText", null, "#{templateBean.itemText}"); //$NON-NLS-1$ //$NON-NLS-2$
	
		}else if(templateBean.itemClassification.equals(EvalConstants.ITEM_TYPE_BLOCK)&& 
				templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED)){ //"Question Block","Stepped"
			UIBranchContainer blockStepped = UIBranchContainer.make(tofill, "blockStepped:"); //$NON-NLS-1$
			
			UIOutput.make(blockStepped, "itemNo",null,"#{templateBean.currItemNo}"); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(blockStepped, "itemText", null, "#{templateBean.itemText}"); //$NON-NLS-1$ //$NON-NLS-2$
			if(templateBean.itemNA != null && templateBean.itemNA.booleanValue()== true){
				UIBranchContainer radiobranch3 = UIBranchContainer.make(blockStepped,"showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA); //$NON-NLS-1$
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
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
			
			//Radio Buttons
			//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
			//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
			
			UISelect radios = UISelect.make(blockStepped, "dummyRadio", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
			radios.optionnames = UIOutputMany.make(scaleLabels);		
		    String selectID = radios.getFullID();
		    for (int i = 0; i < scaleValues.length; ++i) 
		    {
				UIBranchContainer radioTopLabelBranch = UIBranchContainer.make(blockStepped, "scaleTopLabelOptions:", Integer.toString(i)); //$NON-NLS-1$
				UISelectLabel.make(radioTopLabelBranch, "dummyTopRadioLabel", selectID, i); //$NON-NLS-1$
				UILink.make(radioTopLabelBranch, "cornerImage", EvaluationConstant.STEPPED_IMAGE_URLS[0]); //$NON-NLS-1$
				//This branch container is created to help in creating the middle images after the LABEL
			    for (int k = 0; k < i; ++k) {
			    	UIBranchContainer radioTopLabelAfterBranch = UIBranchContainer.make(radioTopLabelBranch, "scaleTopLabelAfterOptions:", Integer.toString(k)); //$NON-NLS-1$
					UILink.make(radioTopLabelAfterBranch, "middleImage", EvaluationConstant.STEPPED_IMAGE_URLS[1]); //$NON-NLS-1$
			    }
				UIBranchContainer radioBottomLabelBranch = UIBranchContainer.make(blockStepped, "scaleBottomLabelOptions:", Integer.toString(i)); //$NON-NLS-1$
				UILink.make(radioBottomLabelBranch, "bottomImage", EvaluationConstant.STEPPED_IMAGE_URLS[2]);				 //$NON-NLS-1$
		    }	
				//get child block item text
			for(int j = 0; j< templateBean.queList.size(); j++){
					UIBranchContainer queRow = UIBranchContainer.make(blockStepped,"queRow:", Integer.toString(j)); //$NON-NLS-1$
					String txt = (String)templateBean.queList.get(j);
					UIOutput.make(queRow,"queNo",Integer.toString(j+1));	 //$NON-NLS-1$
					UIOutput.make(queRow,"queText",txt); //$NON-NLS-1$
					for(int k=0;k< scaleValues.length; k++){
						UIBranchContainer bc1 = UIBranchContainer.make(queRow, "scaleValueOptions:", Integer.toString(k)); //$NON-NLS-1$
						UISelectChoice.make(bc1, "dummyRadioValue", selectID, k); //$NON-NLS-1$
					}
					
			}
		}else if(templateBean.itemClassification.equals(EvalConstants.ITEM_TYPE_BLOCK)&& 
				templateBean.scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED)){ //"Question Block","Stepped Colored"
			UIBranchContainer blockSteppedColored = UIBranchContainer.make(tofill, "blockSteppedColored:"); //$NON-NLS-1$
			UIOutput.make(blockSteppedColored, "itemNo",null,"#{templateBean.currItemNo}"); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(blockSteppedColored, "itemText", null, "#{templateBean.itemText}"); //$NON-NLS-1$ //$NON-NLS-2$
			if(templateBean.itemNA != null && templateBean.itemNA.booleanValue()== true){
				UIBranchContainer radiobranch3 = UIBranchContainer.make(blockSteppedColored,"showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA",templateBean.itemNA); //$NON-NLS-1$
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
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
			String ideal = scale.getIdeal(); //templateBean.itemDisplayPreview.getItem().getScale().getIdeal();
			String idealImage = ""; //$NON-NLS-1$
			if (ideal ==  null)
				idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
			else if (ideal.equals("low")) //$NON-NLS-1$
				idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
			else if (ideal.equals("mid")) //$NON-NLS-1$
				idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
			else
				idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];
		//	UILink.make(blockSteppedColored, "idealImage", idealImage);
			
			//Radio Buttons
			//String values [] = (templateBean.itemDisplayPreview).getScaleValues();
			//String labels [] = (templateBean.itemDisplayPreview).getScaleLabels();
			UISelect radios = UISelect.make(blockSteppedColored, "dummyRadio", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
			radios.optionnames = UIOutputMany.make(scaleLabels);
		    String selectID = radios.getFullID();
		    for (int i = 0; i < scaleValues.length; ++i) {
				UIBranchContainer rowBranch = UIBranchContainer.make(blockSteppedColored, "rowBranch:", Integer.toString(i));				 //$NON-NLS-1$
			    //Actual label
				UISelectLabel.make(rowBranch, "topLabel", selectID, i);				 //$NON-NLS-1$
				//Corner Image
				UILink.make(rowBranch, "cornerImage", EvaluationConstant.STEPPED_IMAGE_URLS[0]); //$NON-NLS-1$
				//This branch container is created to help in creating the middle images after the LABEL
			    for (int k = 0; k < i; ++k) 
			    {
			    	UIBranchContainer afterTopLabelBranch = UIBranchContainer.make(rowBranch, "afterTopLabelBranch:", Integer.toString(k)); //$NON-NLS-1$
					UILink.make(afterTopLabelBranch, "middleImage", EvaluationConstant.STEPPED_IMAGE_URLS[1]); //$NON-NLS-1$
			    }

				UIBranchContainer bottomLabelBranch = UIBranchContainer.make(blockSteppedColored, "bottomLabelBranch:", Integer.toString(i)); //$NON-NLS-1$
				UILink.make(bottomLabelBranch, "bottomImage", EvaluationConstant.STEPPED_IMAGE_URLS[2]); //$NON-NLS-1$
				
		    }
		    
			//get child block item text
		    for(int j = 0; j< templateBean.queList.size(); j++){
		    	
				UIBranchContainer queRow = UIBranchContainer.make(blockSteppedColored,"queRow:", Integer.toString(j)); //$NON-NLS-1$
				String txt = (String)templateBean.queList.get(j);
				UIOutput.make(queRow,"queNo",Integer.toString(j+1));	 //$NON-NLS-1$
				UIOutput.make(queRow,"queText",txt); //$NON-NLS-1$
				UILink.make(queRow, "idealImage", idealImage); //$NON-NLS-1$
				for(int k=0;k< scaleValues.length; k++){
					UIBranchContainer radioBranchFirst = UIBranchContainer.make(queRow, "scaleOptionsFirst:", Integer.toString(k)); //$NON-NLS-1$
					UISelectChoice.make(radioBranchFirst, "dummyRadioValueFirst", selectID, k); //$NON-NLS-1$

					UIBranchContainer radioBranchSecond = UIBranchContainer.make(queRow, "scaleOptionsSecond:", Integer.toString(k)); //$NON-NLS-1$
					UISelectChoice.make(radioBranchSecond, "dummyRadioValueSecond", selectID, k); //$NON-NLS-1$
				}
			}
			
		}
		
		UICommand.make(form, "cancelRemoveAction", messageLocator.getMessage("general.cancel.button"),  //$NON-NLS-1$ //$NON-NLS-2$
				"#{templateBean.cancelRemoveAction}"); //$NON-NLS-1$
		UICommand.make(form, "removeQuestionAction", messageLocator.getMessage("removequestion.remove.button"),  //$NON-NLS-1$ //$NON-NLS-2$
				"#{templateBean.removeQuestionAction}"); //$NON-NLS-1$
		
	}

	public List reportNavigationCases() {
		List i = new ArrayList();
		
		i.add(new NavigationCase(TemplateModifyProducer.VIEW_ID, new SimpleViewParameters(TemplateModifyProducer.VIEW_ID)));
		return i;
	}
	
	
}
