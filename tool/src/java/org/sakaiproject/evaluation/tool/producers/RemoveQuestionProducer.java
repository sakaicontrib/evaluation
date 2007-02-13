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

import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.params.TemplateItemViewParameters;


import uk.org.ponder.beanutil.BeanGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
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
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This page is to remove an Item(all kind of Item type) from DAO
 * 
 * @author: Rui Feng (fengr@vt.edu)
 */

public class RemoveQuestionProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {
	public static final String VIEW_ID = "remove_question"; //$NON-NLS-1$
	
	public String getViewID() {
		return VIEW_ID;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	
	private BeanGetter rbg;
	public void setRequestBeanGetter(BeanGetter rbg) {
	    this.rbg = rbg;
	 }

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	public Long templateId;
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {	

		TemplateItemViewParameters templateItemViewParams = (TemplateItemViewParameters) viewparams;
		
        Long templateItemId = templateItemViewParams.templateItemId;
        templateId=templateItemViewParams.templateId;
		String templateItemOTPBinding="templateItemBeanLocator."+templateItemId;
	    String templateItemOTP=templateItemOTPBinding+".";			
	    
        //EvalTemplateItem myTemplateItem=(EvalTemplateItem)rbg.getBean(templateItemOTPBinding);	
        EvalTemplateItem myTemplateItem=itemsLogic.getTemplateItemById(templateItemId);
        System.out.println("templateItemId"+templateItemId.toString());
        System.out.println("itemtext"+myTemplateItem.getItem().getItemText());
        
		UIOutput.make(tofill, "remove-question-title", messageLocator.getMessage("removequestion.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "modify-template-title", messageLocator.getMessage("modifytemplate.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));	
		
		UIForm form = UIForm.make(tofill, "removeQuestionForm"); //$NON-NLS-1$
		
		UIOutput.make(form, "remove-question-confirm-pre-name", messageLocator.getMessage("removequestion.confirm.pre.name")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(form, "remove-question-confirm-post-name", messageLocator.getMessage("removequestion.confirm.post.name")); //$NON-NLS-1$ //$NON-NLS-2$

		if(myTemplateItem.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_SCALED)){ //"Scaled/Survey"
			EvalScale  scale = myTemplateItem.getItem().getScale();
			String[] scaleOptions = scale.getOptions();
			int optionCount = scaleOptions.length;
			String scaleValues[] = new String[optionCount];
			String scaleLabels[] = new String[optionCount];
			
			String scaleDisplaySetting=myTemplateItem.getScaleDisplaySetting();
			if (scaleDisplaySetting==null)scaleDisplaySetting=myTemplateItem.getItem().getScaleDisplaySetting();
			System.out.println("scaledisplaySetting"+scaleDisplaySetting);
			if (scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_COMPACT)) { //"Compact"
				UIBranchContainer compact = UIBranchContainer.make(tofill, "compactDisplay:"); //$NON-NLS-1$

				//Item text
				UIOutput.make(compact, "queNo", myTemplateItem.getDisplayOrder().toString()); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(compact, "itemText", myTemplateItem.getItem().getItemText()); //$NON-NLS-1$ //$NON-NLS-2$
				
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
				
				Boolean usesNA=myTemplateItem.getUsesNA();
				if(usesNA==null)usesNA=myTemplateItem.getItem().getUsesNA();
				if(usesNA != null && usesNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(compact,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",usesNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
			}else if (scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED)) { //"Compact Colored"

				UIBranchContainer compactColored = UIBranchContainer.make(tofill, "compactDisplayColored:"); //$NON-NLS-1$

				//Item text
				UIOutput.make(compactColored, "queNo", myTemplateItem.getDisplayOrder().toString()); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(compactColored, "itemText", myTemplateItem.getItem().getItemText()); //$NON-NLS-1$ //$NON-NLS-2$
				
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
				
				Boolean usesNA=myTemplateItem.getUsesNA();
				if(usesNA==null)usesNA=myTemplateItem.getItem().getUsesNA();
				if(usesNA != null && usesNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(compactColored,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",usesNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}else if (scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL)) { //"Full"
				
				UIBranchContainer full = UIBranchContainer.make(tofill, "fullDisplay:"); //$NON-NLS-1$

				//Item text
				UIOutput.make(full, "queNo", myTemplateItem.getDisplayOrder().toString()); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(full, "itemText", myTemplateItem.getItem().getItemText()); //$NON-NLS-1$ //$NON-NLS-2$
				Boolean usesNA=myTemplateItem.getUsesNA();
				if(usesNA==null)usesNA=myTemplateItem.getItem().getUsesNA();
				if(usesNA != null && usesNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(full,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",usesNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				for (int count = 0; count < optionCount; count++) {

					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = scaleOptions[count];	
				}
				//Radio Buttons
				UISelect radios = UISelect.make(full, "dummyRadio", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radiobranch = UIBranchContainer.make(full, "scaleOptions:", Integer.toString(i)); //$NON-NLS-1$
					UISelectChoice.make(radiobranch, "dummyRadioValue", selectID, i); //$NON-NLS-1$
					UISelectLabel.make(radiobranch, "dummyRadioLabel", selectID, i); //$NON-NLS-1$
			    }
				
			}else if (scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED)) { //"Full Colored"
				
				UIBranchContainer fullColored = UIBranchContainer.make(tofill, "fullDisplayColored:"); //$NON-NLS-1$

				System.out.println("itemText"+myTemplateItem.getItem().getItemText());
				
				//Item text
				UIOutput.make(fullColored, "queNo", myTemplateItem.getDisplayOrder().toString()); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(fullColored, "itemText", myTemplateItem.getItem().getItemText()); //$NON-NLS-1$ //$NON-NLS-2$
				Boolean usesNA=myTemplateItem.getUsesNA();
				if(usesNA==null)usesNA=myTemplateItem.getItem().getUsesNA();
				if(usesNA != null && usesNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(fullColored,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",usesNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				//Get the scale ideal value (none, low, mid, high )
				String ideal = scale.getIdeal();

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

				UISelect radios = UISelect.make(fullColored, "dummyRadio", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
				
				radios.optionnames = UIOutputMany.make(scaleLabels);
				
			    String selectID = radios.getFullID();
			    for (int i = 0; i < scaleValues.length; ++i) 
			    {
					UIBranchContainer radiobranch = UIBranchContainer.make(fullColored, "scaleOptions:", Integer.toString(i)); //$NON-NLS-1$
					UISelectChoice.make(radiobranch, "dummyRadioValue", selectID, i); //$NON-NLS-1$
					UISelectLabel.make(radiobranch, "dummyRadioLabel", selectID, i); //$NON-NLS-1$
			    }
				
			}else if (scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED)) { //stepped
				
				UIBranchContainer stepped = UIBranchContainer.make(tofill, "steppedDisplay:"); //$NON-NLS-1$
				
				//Item text
				UIOutput.make(stepped, "queNo", myTemplateItem.getDisplayOrder().toString()); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(stepped, "itemText", myTemplateItem.getItem().getItemText()); //$NON-NLS-1$ //$NON-NLS-2$
				Boolean usesNA=myTemplateItem.getUsesNA();
				if(usesNA==null)usesNA=myTemplateItem.getItem().getUsesNA();
				if(usesNA != null && usesNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(stepped,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",usesNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				for (int count = 1; count <= optionCount; count++) {
					scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
					scaleLabels[optionCount - count] = scaleOptions[count-1];
				}
				
				//Radio Buttons
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
				
			}else if (scaleDisplaySetting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED)) { //"Stepped Colored"

				UIBranchContainer steppedColored = UIBranchContainer.make(tofill, "steppedDisplayColored:"); //$NON-NLS-1$

				//Item text
				UIOutput.make(steppedColored, "queNo", myTemplateItem.getDisplayOrder().toString()); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(steppedColored, "itemText", myTemplateItem.getItem().getItemText()); //$NON-NLS-1$ //$NON-NLS-2$
				Boolean usesNA=myTemplateItem.getUsesNA();
				if(usesNA==null)usesNA=myTemplateItem.getItem().getUsesNA();
				if(usesNA != null && usesNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(steppedColored,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",usesNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				//Get the scale ideal value (none, low, mid, high )
				String ideal = scale.getIdeal(); 
				
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
				UIOutput.make(vertical, "queNo", myTemplateItem.getDisplayOrder().toString()); //$NON-NLS-1$ //$NON-NLS-2$
				UIOutput.make(vertical, "itemText", myTemplateItem.getItem().getItemText()); //$NON-NLS-1$ //$NON-NLS-2$
				Boolean usesNA=myTemplateItem.getUsesNA();
				if(usesNA==null)usesNA=myTemplateItem.getItem().getUsesNA();
				if(usesNA != null && usesNA.booleanValue()== true){
					UIBranchContainer radiobranch3 = UIBranchContainer.make(vertical,"showNA:"); //$NON-NLS-1$
					UIBoundBoolean.make(radiobranch3, "itemNA",usesNA); //$NON-NLS-1$
					UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				for (int count = 0; count < optionCount; count++) {

					scaleValues[count] = new Integer(count).toString();
					scaleLabels[count] = scaleOptions[count];	
				}
				
				//Radio Buttons
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
		}else if(myTemplateItem.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)){ //"Short Answer/Essay"
			UIBranchContainer essay = UIBranchContainer.make(tofill, "essayType:"); //$NON-NLS-1$
			//Item text
			UIOutput.make(essay, "queNo", myTemplateItem.getDisplayOrder().toString()); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(essay, "itemText", myTemplateItem.getItem().getItemText()); //$NON-NLS-1$ //$NON-NLS-2$
			Boolean usesNA=myTemplateItem.getUsesNA();
			if(usesNA==null)usesNA=myTemplateItem.getItem().getUsesNA();
			if(usesNA != null && usesNA.booleanValue()== true){
				UIBranchContainer radiobranch3 = UIBranchContainer.make(essay,"showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA",usesNA); //$NON-NLS-1$
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			UIInput textarea = UIInput.make(essay,"essayBox",null ); //$NON-NLS-1$
			Map attrmap = new HashMap();
			
			Integer displayRows=myTemplateItem.getDisplayRows();
			if(displayRows==null)myTemplateItem.getItem().getDisplayRows();
			String rowNum; 
			if (displayRows == null)
				rowNum = EvaluationConstant.DEFAULT_ROWS.toString();
			else
				rowNum = displayRows.toString();
			
			attrmap.put("rows", rowNum); //$NON-NLS-1$
			textarea.decorators = new DecoratorList(new UIFreeAttributeDecorator(attrmap)); 
			
		}else if(myTemplateItem.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_HEADER)){ //"Text Header"
			UIBranchContainer header = UIBranchContainer.make(tofill, "headerType:"); //$NON-NLS-1$
			UIOutput.make(header, "queNo",myTemplateItem.getDisplayOrder().toString()); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(header, "itemText", myTemplateItem.getItem().getItemText()); //$NON-NLS-1$ //$NON-NLS-2$

		}
		UIOutput.make(form, "cancel-button", messageLocator.getMessage("general.cancel.button"));
		UICommand rmvBtn=UICommand.make(form, "removeQuestionAction", messageLocator.getMessage("removequestion.remove.button"),  //$NON-NLS-1$ //$NON-NLS-2$
				"#{itemsBean.removeItemAction}"); //$NON-NLS-1$
		rmvBtn.parameters.add(new UIELBinding("#{itemsBean.templateItem}", new ELReference(templateItemOTPBinding)));
	}

	public List reportNavigationCases() {
		List i = new ArrayList();
		
		i.add(new NavigationCase("removed", new EvalViewParameters(TemplateModifyProducer.VIEW_ID, templateId)));
		return i;
	}
	
	  public ViewParameters getViewParameters() {
		    return new TemplateItemViewParameters();
		  }
	  
}
