/******************************************************************************
 * PreviewEvalProducer.java - created by fengr@vt.edu on Sept 18, 2006
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.logic.model.Context;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.params.PreviewEvalParameters;
import org.sakaiproject.evaluation.tool.utils.ItemBlockUtils;


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
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This is the producer page for preview a template or evaluation
 * 
 * @author: Rui Feng (fengr@vt.edu)
 * @author:Kapil Ahuja (kahuja@vt.edu)
 */

public class PreviewEvalProducer implements ViewComponentProducer,
ViewParamsReporter {

public static final String VIEW_ID = "preview_eval"; //$NON-NLS-1$
public String getViewID() {
	return VIEW_ID;
}

private EvalEvaluationsLogic evalsLogic;
public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
	this.evalsLogic = evalsLogic;
}

private EvalExternalLogic external;
public void setExternal(EvalExternalLogic external) {
	this.external = external;
}

private EvalTemplatesLogic templatesLogic;
public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
	this.templatesLogic = templatesLogic;
}

private MessageLocator messageLocator;
public void setMessageLocator(MessageLocator messageLocator) {
	this.messageLocator = messageLocator;
}

public ViewParameters getViewParameters() {
	//return new EvalViewParameters(VIEW_ID, null, null);
	return new PreviewEvalParameters(VIEW_ID, null,null,null,null);
}

/**
 * 
 * @param bl=
 *            true: test if there is any "Course" item
 * @param bl=
 *            false: test if there is any "Instructor" item
 */
private boolean findItemCategory(boolean bl, List itemList) {
	boolean rs = false;

	for (int j = 0; j < itemList.size(); j++) {
		EvalTemplateItem tempItem1 = (EvalTemplateItem) itemList.get(j);
		String category = tempItem1.getItemCategory();
		if (bl && category.equals(EvalConstants.ITEM_CATEGORY_COURSE)) { //"Course"
			rs = true;
			break;
		}

		if (bl == false && category.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR)) { //"Instructor"
			rs = true;
			break;
		}
	}

	return rs;
}

/* 
 * 1). Preview Template --getting data from DAO --DONE
 * 2). Preview Evalution -- by passing Evalution ID,course--TO BE DONE
 */
public void fillComponents(UIContainer tofill, ViewParameters viewparams,
		ComponentChecker checker) {
	
	//TODO - i18n UIBoundBoolean's for N/a
	
	UIOutput.make(tofill,"control-panel-title", messageLocator.getMessage("controlpanel.page.title"));		 //$NON-NLS-1$ //$NON-NLS-2$
	UIOutput.make(tofill, "preview-eval-title", messageLocator.getMessage("previeweval.page.title"));		 //$NON-NLS-1$ //$NON-NLS-2$
	
	UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
			new SimpleViewParameters(SummaryProducer.VIEW_ID));		
	
	PreviewEvalParameters previewEvalViewParams = (PreviewEvalParameters)viewparams;

	UIOutput.make(tofill, "evaluation-title-header", messageLocator.getMessage("previeweval.evaluation.title.header")); //$NON-NLS-1$ //$NON-NLS-2$
	UIOutput.make(tofill, "course-title-header", messageLocator.getMessage("previeweval.course.title.header")); //$NON-NLS-1$ //$NON-NLS-2$
	UIOutput.make(tofill, "instructions-title-header", messageLocator.getMessage("previeweval.instructions.title.header")); //$NON-NLS-1$ //$NON-NLS-2$
	
	if(previewEvalViewParams.originalPage != null){
		if(previewEvalViewParams.originalPage.equals(TemplateModifyProducer.VIEW_ID) || 
				previewEvalViewParams.originalPage.equals(EvaluationStartProducer.VIEW_ID) ){
			//get template ID
			UIOutput.make(tofill, "evalTitle", messageLocator.getMessage("previeweval.evaluation.title.default"));//no evaluation title, use dummy //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(tofill, "courseTitle", messageLocator.getMessage("previeweval.course.title.default"));//no course title, use dummy //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(tofill, "instruction", messageLocator.getMessage("previeweval.instructions.default"));//no instruction, use dummy //$NON-NLS-1$ //$NON-NLS-2$
		}
	
		if(previewEvalViewParams.originalPage.equals(ControlPanelProducer.VIEW_ID)){
			
			//get eval ID, need to get assigned courses from eval ID
			if(previewEvalViewParams.evaluationId != null){
				
				EvalEvaluation eval = evalsLogic.getEvaluationById(previewEvalViewParams.evaluationId);
				UIOutput.make(tofill, "evalTitle", eval.getTitle()); //$NON-NLS-1$
				
				int count = evalsLogic.countEvaluationContexts(eval.getId());
				if (count > 1){
					UIOutput.make(tofill, "courseTitle",count+"courses"); //$NON-NLS-1$ //$NON-NLS-2$
				}else{
					Long[] evalIds = {eval.getId()};
					Map evalContexts = evalsLogic.getEvaluationContexts(evalIds);
					List contexts = (List) evalContexts.get(eval.getId());
					Context ctxt = (Context) contexts.get(0);
					String title = ctxt.title;
					UIOutput.make(tofill, "courseTitle",title);
					//UIOutput.make(tofill, "courseTitle",logic.getCourseTitle(eval.getId()));
			
				}
				if(eval.getInstructions() != null)
					UIOutput.make(tofill, "instruction",eval.getInstructions()); //$NON-NLS-1$
				else UIOutput.make(tofill, "instruction", messageLocator.getMessage("previeweval.instructions.default")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
		if(previewEvalViewParams.originalPage.equals(SummaryProducer.VIEW_ID)){
			
			if(previewEvalViewParams.evaluationId != null){
		
				EvalEvaluation eval = evalsLogic.getEvaluationById(previewEvalViewParams.evaluationId);
				UIOutput.make(tofill, "evalTitle", eval.getTitle()); //$NON-NLS-1$
				
				if(previewEvalViewParams.context != null){
					//get course title from context
					UIOutput.make(tofill, "courseTitle",external.getDisplayTitle(previewEvalViewParams.context)); 
				}else{ //get evalID, need to get assigned courses from eval ID
					int count = evalsLogic.countEvaluationContexts(eval.getId());
					if (count > 1){
						UIOutput.make(tofill, "courseTitle",count+"courses"); //$NON-NLS-1$ //$NON-NLS-2$
					}else{
						Long[] evalIds = {eval.getId()};
						Map evalContexts = evalsLogic.getEvaluationContexts(evalIds);
						List contexts = (List) evalContexts.get(eval.getId());
						Context ctxt = (Context) contexts.get(0);
						String title = ctxt.title;
						UIOutput.make(tofill, "courseTitle",title);
						//UIOutput.make(tofill, "courseTitle",logic.getCourseTitle(eval.getId())); //$NON-NLS-1$
					}
				}
				if(eval.getInstructions() != null)
					UIOutput.make(tofill, "instruction",eval.getInstructions()); //$NON-NLS-1$
				else UIOutput.make(tofill, "instruction", messageLocator.getMessage("previeweval.instructions.default")); //$NON-NLS-1$ //$NON-NLS-2$

			}
		} //end of: if(previewEvalViewParams.originalPage.equals(SummaryProducer.VIEW_ID))
		
		
		
		EvalTemplate template = null;
		template = templatesLogic.getTemplateById(previewEvalViewParams.templateId);
		//template = logic.getTemplateById(previewEvalViewParams.templateId);
		if(template.getDescription() != null){
			  //show description
			UIBranchContainer showDescr = UIBranchContainer.make(tofill, "showDescription:"); //$NON-NLS-1$
			UIOutput.make(showDescr, "description", template.getDescription()); //$NON-NLS-1$
		 }
		

		// get items(parent items, child items --need to set order
		//List allItems = new ArrayList(template.getItems());
		List allItems = new ArrayList(template.getTemplateItems());
		
		/*
		 * With new table design, all the items(including child items) are pull out
		 * 
		 * */
		if (! allItems.isEmpty()) {
			//filter out the block child items, to get a list non-child items
			List ncItemsList = ItemBlockUtils.getNonChildItems(allItems);
			
			//Collections.sort(allItems, new EvaluationItemOrderComparator());
			Collections.sort(ncItemsList, new EvaluationItemOrderComparator());		
			
			// check if there is any "Course" items or "Instructor" items;
			UIBranchContainer courseSection = null;
			UIBranchContainer instructorSection = null;
			//if (this.findItemCategory(true, allItems))	{
			if (this.findItemCategory(true, ncItemsList))	{	
				courseSection = UIBranchContainer.make(tofill,"courseSection:"); //$NON-NLS-1$
				UIOutput.make(courseSection, "course-questions-header", messageLocator.getMessage("previeweval.course.questions.header"));  //$NON-NLS-1$ //$NON-NLS-2$
			}
			//if (this.findItemCategory(false, allItems)){
			if (this.findItemCategory(false,ncItemsList)){	
				instructorSection = UIBranchContainer.make(tofill,"instructorSection:"); //$NON-NLS-1$
				UIOutput.make(instructorSection, "instructor-questions-header", messageLocator.getMessage("previeweval.instructor.questions.header"));  //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			//for (int i = 0; i < allItems.size(); i++) {
			for (int i = 0; i < ncItemsList.size(); i++) {	
				//EvalItem item1 = (EvalItem) allItems.get(i);
				EvalTemplateItem tempItem1 = (EvalTemplateItem) ncItemsList.get(i);
			//	EvalItem item1 = (EvalItem) tempItem1.getItem();
				
				//String cat = item1.getCategory();
				String cat = tempItem1.getItemCategory();
				UIBranchContainer radiobranch = null;
				if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_COURSE)) { //"Course"
					radiobranch = UIBranchContainer.make(courseSection,
								"itemrow:first", Integer.toString(i)); //$NON-NLS-1$
						
					if (i % 2 == 1)
						radiobranch.decorators = new DecoratorList(new UIColourDecorator(null,
											Color.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));

					this.doFillComponent(tempItem1, i, radiobranch,courseSection, allItems);
						
				} else if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR)) { //"Instructor"
					radiobranch = UIBranchContainer.make(instructorSection,
								"itemrow:first", Integer.toString(i)); //$NON-NLS-1$
						
					if (i % 2 == 1)
						radiobranch.decorators = new DecoratorList(new UIColourDecorator(null,
											Color.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));
						
					this.doFillComponent(tempItem1, i, radiobranch,instructorSection,allItems);
				}
			} // end of for loop

		}//end of:if (! allItems.isEmpty())
		
	} //end of:if(previewEvalViewParams.originalPage != null)
	
	
} // end of method


private void doFillComponent(EvalTemplateItem myTempItem, int i,
		UIBranchContainer radiobranch, UIContainer tofill, List itemsList) {
	
	EvalItem myItem = myTempItem.getItem();
	
	if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_SCALED)) { //scaled

		EvalScale  scale =  myItem.getScale();
		String[] scaleOptions = scale.getOptions();
		int optionCount = scaleOptions.length;
		String scaleValues[] = new String[optionCount];
		String scaleLabels[] = new String[optionCount];
		
		
		//String setting = myItem.getScaleDisplaySetting();	
		//Boolean useNA = myItem.getUsesNA();
		String setting = myTempItem.getScaleDisplaySetting();
		Boolean useNA = myTempItem.getUsesNA();

		if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_COMPACT)) { //compact
			UIBranchContainer compact = UIBranchContainer.make(radiobranch,"compactDisplay:"); 
			
			String compactDisplayStart = scaleOptions[0];		
			String compactDisplayEnd = scaleOptions[optionCount - 1];

			for (int count = 0; count < optionCount; count++) {
				
				scaleValues[count] = new Integer(count).toString();
				scaleLabels[count] = " ";
			}
			
			UIOutput.make(compact, "itemNum", (new Integer(i + 1)) //$NON-NLS-1$
					.toString());
			UIOutput.make(compact, "itemText", myItem.getItemText()); //$NON-NLS-1$
			UIOutput.make(compact, "compactDisplayStart", compactDisplayStart);// Radio Buttons
			UISelect radios = UISelect.make(compact, "dummyRadio", scaleValues,scaleLabels, null, false);
			radios.optionnames = UIOutputMany.make(scaleLabels);

			String selectID = radios.getFullID();
			for (int j = 0; j < scaleValues.length; ++j) {
				UIBranchContainer rb = UIBranchContainer.make(compact,
						"scaleOptions:", Integer.toString(j)); //$NON-NLS-1$
				UISelectChoice.make(rb, "dummyRadioValue", selectID, j); //$NON-NLS-1$
			}
			UIOutput.make(compact, "compactDisplayEnd", compactDisplayEnd);

			if (useNA.booleanValue() == true) {
				UIBranchContainer radiobranch3 = UIBranchContainer.make(
						compact, "showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));
			}

		} else if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED)) { //$NON-NLS-1$
			UIBranchContainer compactColored = UIBranchContainer.make(
					radiobranch, "compactDisplayColored:"); //$NON-NLS-1$

			UIOutput.make(compactColored, "itemNum", (new Integer(i + 1)) //$NON-NLS-1$
					.toString());
			UIOutput.make(compactColored, "itemText", myItem.getItemText()); //$NON-NLS-1$
			// Get the scale ideal value (none, low, mid, high )
			String ideal = scale.getIdeal();
			// Compact start and end label containers
			UIBranchContainer compactStartContainer = UIBranchContainer
					.make(compactColored, "compactStartContainer:"); //$NON-NLS-1$
			UIBranchContainer compactEndContainer = UIBranchContainer.make(
					compactColored, "compactEndContainer:"); //$NON-NLS-1$
			// Finding the colors if compact start and end labels
			Color startColor = null;
			Color endColor = null;

			// When no ideal is specified then just plain blue for both
			// start and end
			if (ideal == null) {
				startColor = Color.decode(EvaluationConstant.BLUE_COLOR);
				endColor = Color.decode(EvaluationConstant.BLUE_COLOR);
			} else if (ideal.equals("low")) { //$NON-NLS-1$
				startColor = Color.decode(EvaluationConstant.GREEN_COLOR);
				endColor = Color.decode(EvaluationConstant.RED_COLOR);
			} else if (ideal.equals("mid")) { //$NON-NLS-1$
				startColor = Color.decode(EvaluationConstant.RED_COLOR);
				endColor = Color.decode(EvaluationConstant.RED_COLOR);
			}
			// This for the case when high is the ideal
			else {
				startColor = Color.decode(EvaluationConstant.RED_COLOR);
				endColor = Color.decode(EvaluationConstant.GREEN_COLOR);
			}

			compactStartContainer.decorators = new DecoratorList(
					new UIColourDecorator(null, startColor));
			compactEndContainer.decorators = new DecoratorList(
					new UIColourDecorator(null, endColor));

			String compactDisplayStart = scaleOptions[0];		
			String compactDisplayEnd = scaleOptions[optionCount - 1];
			for (int count = 0; count < optionCount; count++) {
				
				scaleValues[count] = new Integer(count).toString();
				scaleLabels[count] = " ";
			}
			// Compact start and end actual labels
			UIOutput.make(compactStartContainer, "compactDisplayStart", compactDisplayStart);
			UIOutput.make(compactEndContainer, "compactDisplayEnd",compactDisplayEnd);

			// For the radio buttons
			UIBranchContainer compactRadioContainer = UIBranchContainer
					.make(compactColored, "compactRadioContainer:"); //$NON-NLS-1$

			String idealImage = ""; //$NON-NLS-1$
			if (ideal == null)
				idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
			else if (ideal.equals("low")) //$NON-NLS-1$
				idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
			else if (ideal.equals("mid")) //$NON-NLS-1$
				idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
			else
				idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];

			UILink.make(compactRadioContainer, "idealImage", idealImage); //$NON-NLS-1$

			UISelect radios = UISelect.make(compactRadioContainer,
					"dummyRadio", scaleValues, scaleLabels, null, false); //$NON-NLS-1$
			radios.optionnames = UIOutputMany.make(scaleLabels);
			String selectID = radios.getFullID();
			for (int j = 0; j < scaleValues.length; ++j) {
				UIBranchContainer radioBranchFirst = UIBranchContainer
						.make(compactRadioContainer, "scaleOptionsFirst:",
								Integer.toString(j));
				UISelectChoice.make(radioBranchFirst,
						"dummyRadioValueFirst", selectID, j); //$NON-NLS-1$

				UIBranchContainer radioBranchSecond = UIBranchContainer
						.make(compactRadioContainer, "scaleOptionsSecond:",
								Integer.toString(j));
				UISelectChoice.make(radioBranchSecond,
						"dummyRadioValueSecond", selectID, j); //$NON-NLS-1$
			}

			if (useNA.booleanValue() == true) {
				UIBranchContainer radiobranch3 = UIBranchContainer.make(
						compactColored, "showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));				
			}

		} else if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL) || setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED)
				|| setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL)) { //full, full colored, vertical
			
			for (int count = 0; count < optionCount; count++) {

				scaleValues[count] = new Integer(count).toString();
				scaleLabels[count] = scaleOptions[count];	
			}
			
			UIBranchContainer fullFirst = UIBranchContainer.make(
					radiobranch, "fullType:"); //$NON-NLS-1$
			UIOutput.make(fullFirst, "itemNum", (new Integer(i + 1)) //$NON-NLS-1$
					.toString());
			UIOutput.make(fullFirst, "itemText", myItem.getItemText()); //$NON-NLS-1$
			if (useNA.booleanValue() == true) {
				UIBranchContainer radiobranch3 = UIBranchContainer.make(
						fullFirst, "showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));				
			}
			// display next row
			UIBranchContainer radiobranchFullRow = UIBranchContainer.make(
					tofill, "itemrow:second", Integer.toString(i)); //$NON-NLS-1$
			// Setting the row background color for even numbered rows.
			if (i % 2 == 1)
				radiobranchFullRow.decorators = new DecoratorList(
						new UIColourDecorator(
								null,
								Color
										.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));

			//if (setting.equals("Full")) 
			if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_FULL)) { 
				UIBranchContainer full = UIBranchContainer.make(
						radiobranchFullRow, "fullDisplay:"); //$NON-NLS-1$
				// Radio Buttons
				UISelect radios = UISelect.make(full, "dummyRadio", scaleValues,scaleLabels, null, false);
				radios.optionnames = UIOutputMany.make(scaleLabels);
				String selectID = radios.getFullID();
				for (int j = 0; j < scaleValues.length; ++j) {
					UIBranchContainer radiobranchNested = UIBranchContainer.make(full, "scaleOptions:", Integer.toString(j));
					UISelectChoice.make(radiobranchNested,
							"dummyRadioValue", selectID, j); //$NON-NLS-1$
					UISelectLabel.make(radiobranchNested,
							"dummyRadioLabel", selectID, j); //$NON-NLS-1$
				}
			} //else if (setting.equals("Vertical")) 
			else if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL)){
				UIBranchContainer vertical = UIBranchContainer.make(
						radiobranchFullRow, "verticalDisplay:"); //$NON-NLS-1$
				UISelect radios = UISelect.make(vertical, "dummyRadio",scaleValues, scaleLabels, null, false);
				radios.optionnames = UIOutputMany.make(scaleLabels);
				String selectID = radios.getFullID();
				for (int j = 0; j < scaleValues.length; ++j) {
					UIBranchContainer radiobranchInside = UIBranchContainer
							.make(vertical, "scaleOptions:", Integer.toString(j));
					UISelectChoice.make(radiobranchInside,
							"dummyRadioValue", selectID, j); //$NON-NLS-1$
					UISelectLabel.make(radiobranchInside,
							"dummyRadioLabel", selectID, j); //$NON-NLS-1$
				}
			} else {// for "Full Colored"
				UIBranchContainer fullColored = UIBranchContainer.make(
						radiobranchFullRow, "fullDisplayColored:"); //$NON-NLS-1$
				// Get the scale ideal value (none, low, mid, high )
				String ideal = scale.getIdeal();

				// Set the ideal image
				String idealImage = ""; //$NON-NLS-1$
				if (ideal == null)
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
				else if (ideal.equals("low")) //$NON-NLS-1$
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
				else if (ideal.equals("mid")) //$NON-NLS-1$
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
				else
					idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];

				UILink.make(fullColored, "idealImage", idealImage); //$NON-NLS-1$
				// UILink.make(fullColored, "idealImageSafari", idealImage);
				UISelect radios = UISelect.make(fullColored, "dummyRadio",scaleValues, scaleLabels, null, false);

				radios.optionnames = UIOutputMany.make(scaleLabels);

				String selectID = radios.getFullID();
				for (int j = 0; j < scaleValues.length; ++j) {
					UIBranchContainer radiobranchInside = UIBranchContainer
							.make(fullColored, "scaleOptions:", Integer.toString(j));
					UISelectChoice.make(radiobranchInside,
							"dummyRadioValue", selectID, j); //$NON-NLS-1$
					UISelectLabel.make(radiobranchInside,
							"dummyRadioLabel", selectID, j); //$NON-NLS-1$
				}
			}

		}// else if (setting.equals("Stepped")) { //$NON-NLS-1$
		 else if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED)) {
			UIBranchContainer stepped = UIBranchContainer.make(radiobranch,
					"steppedDisplay:"); //$NON-NLS-1$

			UIOutput.make(stepped, "itemNum", (new Integer(i + 1)) //$NON-NLS-1$
					.toString());
			UIOutput.make(stepped, "itemText", myItem.getItemText()); //$NON-NLS-1$

			if (useNA.booleanValue() == true) {
				UIBranchContainer radiobranch3 = UIBranchContainer.make(
						stepped, "showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));				
			}
			
			for (int count = 1; count <= optionCount; count++) {
				scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
				scaleLabels[optionCount - count] = scaleOptions[count-1];
			}
			
			UISelect radios = UISelect.make(stepped, "dummyRadio", scaleValues,scaleLabels, null, false);
			radios.optionnames = UIOutputMany.make(scaleLabels);
			String selectID = radios.getFullID();

			for (int j = 0; j < scaleValues.length; ++j) {
				UIBranchContainer radioTopLabelBranch = UIBranchContainer
						.make(stepped, "scaleTopLabelOptions:", Integer.toString(j));
				UISelectLabel.make(radioTopLabelBranch,
						"dummyTopRadioLabel", selectID, j); //$NON-NLS-1$
				UILink.make(radioTopLabelBranch, "cornerImage",EvaluationConstant.STEPPED_IMAGE_URLS[0]);

				// This branch container is created to help in creating the
				// middle images after the LABEL
				for (int k = 0; k < j; ++k) {
					UIBranchContainer radioTopLabelAfterBranch = UIBranchContainer
							.make(radioTopLabelBranch,
									"scaleTopLabelAfterOptions:", Integer.toString(k));
					UILink.make(radioTopLabelAfterBranch, "middleImage",
							EvaluationConstant.STEPPED_IMAGE_URLS[1]);
				}

				UIBranchContainer radioBottomLabelBranch = UIBranchContainer
						.make(stepped, "scaleBottomLabelOptions:", Integer.toString(j));
				UILink.make(radioBottomLabelBranch, "bottomImage", //$NON-NLS-1$
						EvaluationConstant.STEPPED_IMAGE_URLS[2]);

				UIBranchContainer radioValueBranch = UIBranchContainer
						.make(stepped, "scaleValueOptions:", Integer.toString(j));
				UISelectChoice.make(radioValueBranch, "dummyRadioValue",selectID, j);
			}

		} //else if (setting.equals("Stepped Colored")) { //$NON-NLS-1$
		 else if (setting.equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED)) {	
			 UIBranchContainer steppedColored = UIBranchContainer.make(
					radiobranch, "steppedDisplayColored:"); //$NON-NLS-1$

			UIOutput.make(steppedColored, "itemNum", (new Integer(i + 1)).toString());
			UIOutput.make(steppedColored, "itemText", myItem.getItemText()); //$NON-NLS-1$

			if (useNA.booleanValue() == true) {
				UIBranchContainer radiobranch3 = UIBranchContainer.make(
						steppedColored, "showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
				UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));				
			}
			// Get the scale ideal value (none, low, mid, high )
			String ideal = scale.getIdeal();
			String idealImage = ""; //$NON-NLS-1$
			if (ideal == null)
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
			UISelect radios = UISelect.make(steppedColored, "dummyRadio",scaleValues, scaleLabels, null, false);
			radios.optionnames = UIOutputMany.make(scaleLabels);
			String selectID = radios.getFullID();
			for (int j = 0; j < scaleValues.length; ++j) {
				UIBranchContainer rowBranch = UIBranchContainer.make(
						steppedColored, "rowBranch:", Integer.toString(j)); //$NON-NLS-1$
				// Actual label
				UISelectLabel.make(rowBranch, "topLabel", selectID, j); //$NON-NLS-1$

				// Corner Image
				UILink.make(rowBranch, "cornerImage",EvaluationConstant.STEPPED_IMAGE_URLS[0]);

				// This branch container is created to help in creating the
				// middle images after the LABEL
				for (int k = 0; k < j; ++k) {
					UIBranchContainer afterTopLabelBranch = UIBranchContainer
							.make(rowBranch, "afterTopLabelBranch:",Integer.toString(k));
					UILink.make(afterTopLabelBranch, "middleImage",EvaluationConstant.STEPPED_IMAGE_URLS[1]);
				}

				UIBranchContainer bottomLabelBranch = UIBranchContainer
						.make(steppedColored, "bottomLabelBranch:", Integer.toString(j));
				UILink.make(bottomLabelBranch, "bottomImage", //$NON-NLS-1$
						EvaluationConstant.STEPPED_IMAGE_URLS[2]);

				UIBranchContainer radioBranchFirst = UIBranchContainer
						.make(steppedColored, "scaleOptionsFirst:", Integer.toString(j));
				UISelectChoice.make(radioBranchFirst,
						"dummyRadioValueFirst", selectID, j); //$NON-NLS-1$

				UIBranchContainer radioBranchSecond = UIBranchContainer
						.make(steppedColored, "scaleOptionsSecond:",Integer.toString(j));
				UISelectChoice.make(radioBranchSecond,
						"dummyRadioValueSecond", selectID, j); //$NON-NLS-1$
			}

		}

	} else if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_BLOCK)
			&& myTempItem.getScaleDisplaySetting().equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED)) { //"Question Block" "Stepped" type
		
		UIBranchContainer block = UIBranchContainer.make(radiobranch,
				"blockStepped:"); //$NON-NLS-1$
		UIOutput.make(block, "itemNum", (new Integer(i + 1)).toString()); //$NON-NLS-1$
		UIOutput.make(block, "itemText", myItem.getItemText()); //$NON-NLS-1$
		//Boolean useNA = myItem.getUsesNA();
		Boolean useNA = myTempItem.getUsesNA();
		if (useNA.booleanValue() == true) {
			UIBranchContainer radiobranch3 = UIBranchContainer.make(block,
					"showNA:"); //$NON-NLS-1$
			UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
			UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));			
		}

		EvalScale  scale = myItem.getScale();
		String[] scaleOptions = scale.getOptions();
		int optionCount = scaleOptions.length;
		String scaleValues[] = new String[optionCount];
		String scaleLabels[] = new String[optionCount];

		for (int count = 1; count <= optionCount; count++) {
			scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
			scaleLabels[optionCount - count] = scaleOptions[count-1];
		}
		
		UISelect radios = UISelect.make(block, "dummyRadio", scaleValues,scaleLabels, null, false);
		radios.optionnames = UIOutputMany.make(scaleLabels);
		String selectID = radios.getFullID();
		for (int j = 0; j < scaleValues.length; ++j) {
			UIBranchContainer radioTopLabelBranch = UIBranchContainer.make(
					block, "scaleTopLabelOptions:", Integer.toString(j)); //$NON-NLS-1$
			UISelectLabel.make(radioTopLabelBranch, "dummyTopRadioLabel", //$NON-NLS-1$
					selectID, j);
			UILink.make(radioTopLabelBranch, "cornerImage", //$NON-NLS-1$
					EvaluationConstant.STEPPED_IMAGE_URLS[0]);
			// This branch container is created to help in creating the
			// middle images after the LABEL
			for (int k = 0; k < j; ++k) {
				UIBranchContainer radioTopLabelAfterBranch = UIBranchContainer
						.make(radioTopLabelBranch,
								"scaleTopLabelAfterOptions:", Integer.toString(k));
				UILink.make(radioTopLabelAfterBranch, "middleImage",
						EvaluationConstant.STEPPED_IMAGE_URLS[1]);
			}
			UIBranchContainer radioBottomLabelBranch = UIBranchContainer
					.make(block, "scaleBottomLabelOptions:", Integer.toString(j));
			UILink.make(radioBottomLabelBranch, "bottomImage", EvaluationConstant.STEPPED_IMAGE_URLS[2]);
		}
		// get child block item text

		if (myTempItem.getBlockParent()!=null && myTempItem.getBlockParent().booleanValue() == true) {
			Long parentID = myTempItem.getId();
			Integer blockID = new Integer(parentID.intValue());
			//List childItems = logic.findItem(blockID);
			//get child items associated with this Block parent ID
			List childItems = ItemBlockUtils.getChildItmes(itemsList, blockID);
			
			if (childItems != null && childItems.size() > 0) {
				for (int j = 0; j < childItems.size(); j++) {
					UIBranchContainer queRow = UIBranchContainer.make(
							block, "queRow:", Integer.toString(j)); //$NON-NLS-1$
					EvalTemplateItem tempItemChild = (EvalTemplateItem) childItems.get(j);
					EvalItem child = tempItemChild.getItem();
					
					UIOutput.make(queRow, "queNo", Integer.toString(j + 1)); //$NON-NLS-1$
					UIOutput.make(queRow, "queText", child.getItemText()); //$NON-NLS-1$
					for (int k = 0; k < scaleValues.length; k++) {
						UIBranchContainer bc1 = UIBranchContainer.make(
								queRow, "scaleValueOptions:", Integer //$NON-NLS-1$
										.toString(k));
						UISelectChoice.make(bc1, "dummyRadioValue", //$NON-NLS-1$
								selectID, k);
					}
				}
			}// end of if

		} // end of get child block item

	} else if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_BLOCK) 
			&& myTempItem.getScaleDisplaySetting().equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED)) { //"Question Block","Stepped Colored"

		UIBranchContainer blockSteppedColored = UIBranchContainer.make(
				radiobranch, "blockSteppedColored:"); //$NON-NLS-1$
		UIOutput.make(blockSteppedColored, "itemNum", (new Integer(i + 1)) //$NON-NLS-1$
				.toString());
		UIOutput
				.make(blockSteppedColored, "itemText", myItem.getItemText()); //$NON-NLS-1$
		//Boolean useNA = myItem.getUsesNA();
		Boolean useNA = myTempItem.getUsesNA();
		
		if (useNA.booleanValue() == true) {
			UIBranchContainer radiobranch3 = UIBranchContainer.make(
					blockSteppedColored, "showNA:"); //$NON-NLS-1$
			UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
			UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));
		}
		
		EvalScale  scale = myItem.getScale();
		String[] scaleOptions = scale.getOptions();
		int optionCount = scaleOptions.length;
		String scaleValues[] = new String[optionCount];
		String scaleLabels[] = new String[optionCount];

		for (int count = 1; count <= optionCount; count++) {
			scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
			scaleLabels[optionCount - count] = scaleOptions[count-1];
		}
		
		// Get the scale ideal value (none, low, mid, high )
		String ideal = scale.getIdeal();
		
		String idealImage = ""; 
		if (ideal == null)
			idealImage = EvaluationConstant.COLORED_IMAGE_URLS[0];
		else if (ideal.equals("low"))
			idealImage = EvaluationConstant.COLORED_IMAGE_URLS[1];
		else if (ideal.equals("mid")) 
			idealImage = EvaluationConstant.COLORED_IMAGE_URLS[2];
		else
			idealImage = EvaluationConstant.COLORED_IMAGE_URLS[3];

		UISelect radios = UISelect.make(blockSteppedColored, "dummyRadio",scaleValues, scaleLabels, null, false);
		radios.optionnames = UIOutputMany.make(scaleLabels);
		String selectID = radios.getFullID();
		for (int j = 0; j < scaleValues.length; ++j) {
			UIBranchContainer rowBranch = UIBranchContainer.make(
					blockSteppedColored, "rowBranch:", Integer.toString(j)); //$NON-NLS-1$
			// Actual label
			UISelectLabel.make(rowBranch, "topLabel", selectID, j); //$NON-NLS-1$
			// Corner Image
			UILink.make(rowBranch, "cornerImage",EvaluationConstant.STEPPED_IMAGE_URLS[0]);
			// This branch container is created to help in creating the
			// middle images after the LABEL
			for (int k = 0; k < j; ++k) {
				UIBranchContainer afterTopLabelBranch = UIBranchContainer
						.make(rowBranch, "afterTopLabelBranch:", Integer.toString(k));
				UILink.make(afterTopLabelBranch, "middleImage",
						EvaluationConstant.STEPPED_IMAGE_URLS[1]);
			}

			UIBranchContainer bottomLabelBranch = UIBranchContainer.make(
					blockSteppedColored, "bottomLabelBranch:", Integer.toString(j));
			UILink.make(bottomLabelBranch, "bottomImage",EvaluationConstant.STEPPED_IMAGE_URLS[2]);

		}

		// get child block item text
		if (myTempItem.getBlockParent()!= null && myTempItem.getBlockParent().booleanValue() == true) {
			Long parentID = myTempItem.getId();
			Integer blockID = new Integer(parentID.intValue());
			//List childItems = logic.findItem(blockID);
			List childItems = ItemBlockUtils.getChildItmes(itemsList, blockID);
			
			if (childItems != null && childItems.size() > 0) {
				for (int j = 0; j < childItems.size(); j++) {
					UIBranchContainer queRow = UIBranchContainer.make(
							blockSteppedColored, "queRow:", Integer //$NON-NLS-1$
									.toString(j));
					EvalTemplateItem tempItemChild = (EvalTemplateItem) childItems.get(j);
					EvalItem child = tempItemChild.getItem();
					
					UIOutput.make(queRow, "queNo", Integer.toString(j + 1)); //$NON-NLS-1$
					UIOutput.make(queRow, "queText", child.getItemText()); //$NON-NLS-1$
					UILink.make(queRow, "idealImage", idealImage); //$NON-NLS-1$
					for (int k = 0; k < scaleValues.length; k++) {
						UIBranchContainer radioBranchFirst = UIBranchContainer
								.make(queRow, "scaleOptionsFirst:", Integer //$NON-NLS-1$
										.toString(k));
						UISelectChoice.make(radioBranchFirst,
								"dummyRadioValueFirst", selectID, k); //$NON-NLS-1$

						UIBranchContainer radioBranchSecond = UIBranchContainer
								.make(queRow, "scaleOptionsSecond:", //$NON-NLS-1$
										Integer.toString(k));
						UISelectChoice.make(radioBranchSecond,
								"dummyRadioValueSecond", selectID, k); //$NON-NLS-1$
					}
				}
			}// end of if

		} // end of get child block item

	}// else if (myItem.getClassification().equals("Short Answer/Essay")) { //$NON-NLS-1$
	else if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
		UIBranchContainer essay = UIBranchContainer.make(radiobranch,
				"essayType:"); //$NON-NLS-1$
		UIOutput.make(essay, "itemNum", (new Integer(i + 1)).toString()); //$NON-NLS-1$
		UIOutput.make(essay, "itemText", myItem.getItemText()); //$NON-NLS-1$
		//Boolean useNA = myItem.getUsesNA();
		Boolean useNA = myTempItem.getUsesNA();
		if (useNA.booleanValue() == true) {
			UIBranchContainer radiobranch3 = UIBranchContainer.make(essay,
					"showNA:"); //$NON-NLS-1$
			UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
			UIOutput.make(radiobranch3, "na-desc", messageLocator.getMessage("viewitem.na.desc"));
		}

		UIInput textarea = UIInput.make(essay, "essayBox", null); //$NON-NLS-1$
		Map attrmap = new HashMap();
		String rowNum = myTempItem.getDisplayRows().toString();
		attrmap.put("rows", rowNum); //$NON-NLS-1$
		textarea.decorators = new DecoratorList(
				new UIFreeAttributeDecorator(attrmap));

	} //else if (myItem.getClassification().equals("Text Header")) { //$NON-NLS-1$
	else if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_HEADER)) { 
		UIBranchContainer header = UIBranchContainer.make(radiobranch,
				"headerType:"); //$NON-NLS-1$
		UIOutput.make(header, "itemNum", (new Integer(i + 1)).toString()); //$NON-NLS-1$
		UIOutput.make(header, "itemText", myItem.getItemText()); //$NON-NLS-1$

	}
	
} // end of method

public static class EvaluationItemOrderComparator implements Comparator {
	public int compare(Object eval0, Object eval1) {
		// expects to get EvalItem objects, compare by displayOrder
		return ((EvalTemplateItem)eval0).getDisplayOrder().
		compareTo(((EvalTemplateItem)eval1).getDisplayOrder());
		//return ((EvalItem)eval0).getId().
		//	compareTo(((EvalItem)eval1).getId());
		
	}
}
}
