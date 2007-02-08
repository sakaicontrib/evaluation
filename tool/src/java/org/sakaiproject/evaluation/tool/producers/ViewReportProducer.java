/******************************************************************************
 * ViewReportProducer.java - created by whumphri@vt.edu on Oct 05, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Will Humphries (whumphri@vt.edu)
 * Rui Feng (fengr@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool.producers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.params.CSVReportViewParams;
import org.sakaiproject.evaluation.tool.params.EssayResponseParams;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.utils.ItemBlockUtils;


import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIColourDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * rendering the report of an evaluation
 * 
 * @author:Will Humphries (whumphri@vt.edu)
 * @author: Rui Feng (fengr@vt.edu)
 */

public class ViewReportProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

	public static final String VIEW_ID = "view_report"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalEvaluationsLogic evalsLogic;
	public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
		this.evalsLogic = evalsLogic;
	}
	
	private EvalResponsesLogic responsesLogic;	
	public void setResponsesLogic(EvalResponsesLogic responsesLogic) {
		this.responsesLogic = responsesLogic;
	}
	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	
	public ViewParameters getViewParameters() {
		return new EvalViewParameters(VIEW_ID, null);
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
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		UIOutput.make(tofill, "view-report-title", messageLocator.getMessage("viewreport.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID)); //$NON-NLS-1$ //$NON-NLS-2$

		EvalViewParameters previewEvalViewParams = (EvalViewParameters) viewparams;
		if (previewEvalViewParams.templateId != null) {
			UIInternalLink.make(tofill, "fullEssayResponse", messageLocator.getMessage("viewreport.view.essays"), new EssayResponseParams(ViewEssayResponseProducer.VIEW_ID, previewEvalViewParams.templateId)); //$NON-NLS-1$ //$NON-NLS-2$
			EvalEvaluation evaluation = evalsLogic.getEvaluationById(previewEvalViewParams.templateId);//logic.getEvaluationById(previewEvalViewParams.templateId);
			// get template from DAO 
			EvalTemplate template = evaluation.getTemplate();
			UIInternalLink.make(tofill, "csvResultsReport", messageLocator.getMessage("viewreport.view.csv"), new CSVReportViewParams("csvResultsReport", template.getId(), previewEvalViewParams.templateId)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			// get items(parent items, child items --need to set order

			//List childItems = new ArrayList(template.getItems());
			//List allItems = new ArrayList(template.getItems());
			List allItems = new ArrayList(template.getTemplateItems());
			
			if (! allItems.isEmpty()) {
				
				//filter out the block child items, to get a list non-child items
				List ncItemsList = ItemBlockUtils.getNonChildItems(allItems);
				//Collections.sort(childItems, new ReportItemOrderComparator());
				
				Collections.sort(ncItemsList,new PreviewEvalProducer.EvaluationItemOrderComparator());
				
				// check if there is any "Course" items or "Instructor" items;
				UIBranchContainer courseSection = null;
				UIBranchContainer instructorSection = null;
				if (this.findItemCategory(true,ncItemsList)){
					courseSection = UIBranchContainer.make(tofill,"courseSection:"); //$NON-NLS-1$
					UIOutput.make(courseSection, "report-course-questions", messageLocator.getMessage("viewreport.itemlist.coursequestions")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (this.findItemCategory(false, ncItemsList))
				{	instructorSection = UIBranchContainer.make(tofill,"instructorSection:"); //$NON-NLS-1$
					UIOutput.make(instructorSection, "report-instructor-questions", messageLocator.getMessage("viewreport.itemlist.instructorquestions")); //$NON-NLS-1$ //$NON-NLS-2$
				}
				for (int i = 0; i < ncItemsList.size(); i++) {
					//EvalItem item1 = (EvalItem) ncItemsList.get(i);
					EvalTemplateItem tempItem1 = (EvalTemplateItem) ncItemsList.get(i);
					
					//String cat = item1.getCategory();
					String cat = tempItem1.getItemCategory();
					UIBranchContainer radiobranch = null;
					if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_COURSE)) { //"Course"
						radiobranch = UIBranchContainer.make(courseSection,
								"itemrow:first", Integer.toString(i)); //$NON-NLS-1$
						if (i % 2 == 1)
							radiobranch.decorators = new DecoratorList(
									new UIColourDecorator(
											null,
											Color
													.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));

						this.doFillComponent(tempItem1, evaluation.getId(), i, radiobranch,
								courseSection,allItems);
					} else if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR)) { //"Instructor"
						radiobranch = UIBranchContainer.make(instructorSection,
								"itemrow:first", Integer.toString(i)); //$NON-NLS-1$
						if (i % 2 == 1)
							radiobranch.decorators = new DecoratorList(
									new UIColourDecorator(
											null,
											Color
													.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));
						this.doFillComponent(tempItem1, evaluation.getId(), i, radiobranch,
								instructorSection,allItems);
					}
				} // end of for loop

			}

		}
		
	}
	
	private void doFillComponent(EvalTemplateItem myTempItem, Long evalId, int i,
			UIBranchContainer radiobranch, UIContainer tofill,List itemsList) {

		EvalItem myItem = myTempItem.getItem();
		
		if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_SCALED)) { //"Scaled/Survey"

	/*		ItemDisplay itemDisplay = new ItemDisplay(myItem);
			String values[] = itemDisplay.getScaleValues();			
			String labels[] = myItem.getScale().getOptions();
		*/
			EvalScale  scale =  myItem.getScale();
			String[] scaleOptions = scale.getOptions();
			int optionCount = scaleOptions.length;
		//	String scaleValues[] = new String[optionCount];
			String scaleLabels[] = new String[optionCount];
			
			//String setting = myItem.getScaleDisplaySetting();
			//Boolean useNA = myItem.getUsesNA();
			Boolean useNA = myTempItem.getUsesNA();
			
			UIBranchContainer scaledSurvey = UIBranchContainer.make(radiobranch,
			"scaledSurvey:"); //$NON-NLS-1$

			UIOutput.make(scaledSurvey, "itemNum", (new Integer(i + 1)) //$NON-NLS-1$
				.toString());
			UIOutput.make(scaledSurvey, "itemText", myItem.getItemText());	 //$NON-NLS-1$
			
			if (useNA.booleanValue() == true) {
				UIBranchContainer radiobranch3 = UIBranchContainer.make(
						scaledSurvey, "showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
			}		
			
			List itemAnswers = responsesLogic.getEvalAnswers(myItem.getId(), evalId);//logic.getEvalAnswers(myItem.getId(), evalId);
			
		    for (int x = 0; x < scaleLabels.length; ++x) 
		    {
		    	UIBranchContainer answerbranch = UIBranchContainer.make(scaledSurvey, "answers:", Integer.toString(x)); //$NON-NLS-1$
				UIOutput.make(answerbranch, "responseText", scaleLabels[x], (new Integer(x)).toString()); //$NON-NLS-1$
				int answers=0;
				//count the number of answers that match this one
				for(int y=0; y<itemAnswers.size();y++){
					EvalAnswer curr=(EvalAnswer)itemAnswers.get(y);
					if(curr.getNumeric().intValue()==x){
						answers++;
					}
				}
				UIOutput.make(answerbranch, "responseTotal", (new Integer(answers)).toString(), (new Integer(x)).toString());				 //$NON-NLS-1$
		    }


		} else if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_BLOCK)) {		 //$NON-NLS-1$

			UIBranchContainer block = UIBranchContainer.make(radiobranch,"block:"); //$NON-NLS-1$
			UIOutput.make(block, "itemNum", (new Integer(i + 1)).toString()); //$NON-NLS-1$
			UIOutput.make(block, "itemText", myItem.getItemText()); //$NON-NLS-1$
			
			//Boolean useNA = myItem.getUsesNA();
			Boolean useNA = myTempItem.getUsesNA();
			if (useNA.booleanValue() == true) {
				UIBranchContainer radiobranch3 = UIBranchContainer.make(block,
						"showNA:"); //$NON-NLS-1$
				UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
			}
			// Radio Buttons
			
/*			ItemDisplay itemDisplay = new ItemDisplay(myItem);
			String values[] = itemDisplay.getScaleValues();
			String labels[] = myItem.getScale().getOptions();
			*/
			EvalScale  scale = myItem.getScale();
			String[] scaleOptions = scale.getOptions();
			int optionCount = scaleOptions.length;
			String scaleValues[] = new String[optionCount];
			String scaleLabels[] = new String[optionCount];

			for (int count = 1; count <= optionCount; count++) {
				scaleValues[optionCount - count] = new Integer(optionCount - count).toString();
				scaleLabels[optionCount - count] = scaleOptions[count-1];
			}
			
			//render answer options
			for(int p=0; p < scaleLabels.length; ++p){
			UIBranchContainer responseTexts = UIBranchContainer.make(
					block, "responseTexts:", Integer.toString(p));			
			UIOutput.make(responseTexts, "responseText", scaleLabels[p], (new Integer(p).toString())); //$NON-NLS-1$
			}

			// get child block item text
			if (myTempItem.getBlockParent()!=null && myTempItem.getBlockParent().booleanValue() == true) {
				Long parentID = myTempItem.getId();
				Integer blockID = new Integer(parentID.intValue());
				
				//List childItems = logic.findItem(blockID);
				List childItems = ItemBlockUtils.getChildItmes(itemsList, blockID);
				if (childItems != null && childItems.size() > 0) {
					for (int j = 0; j < childItems.size(); j++) {
						UIBranchContainer queRow = UIBranchContainer.make(
								block, "queRow:", Integer.toString(j)); //$NON-NLS-1$
	
						EvalTemplateItem tempItemChild = (EvalTemplateItem) childItems.get(j);
						EvalItem child = tempItemChild.getItem();
						
						UIOutput.make(queRow, "queNo", Integer.toString(j + 1)); //$NON-NLS-1$
						UIOutput.make(queRow, "queText", child.getItemText()); //$NON-NLS-1$
						//List itemAnswers=logic.getEvalAnswers(child.getId(), evalId);
						List itemAnswers = responsesLogic.getEvalAnswers(child.getId(), evalId);
						
					    for (int x = 0; x < scaleLabels.length; ++x) 
					    {
					    	UIBranchContainer answerbranch = UIBranchContainer.make(queRow, "answers:", Integer.toString(x)); //$NON-NLS-1$
							int answers=0;
							//count the number of answers that match this one
							for(int y=0; y<itemAnswers.size();y++){
								EvalAnswer curr=(EvalAnswer)itemAnswers.get(y);
								if(curr.getNumeric().intValue()==x){
									answers++;
								}
							}
							UIOutput.make(answerbranch, "responseTotal", (new Integer(answers)).toString(), (new Integer(x)).toString());				 //$NON-NLS-1$
					    }
					}
				}// end of if

			} // end of get child block item
			
		} else if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) { //"Short Answer/Essay"
			UIBranchContainer essay = UIBranchContainer.make(radiobranch,
			"essayType:"); //$NON-NLS-1$
			UIOutput.make(essay, "itemNum", (new Integer(i + 1)).toString()); //$NON-NLS-1$
			UIOutput.make(essay, "itemText", myItem.getItemText()); //$NON-NLS-1$
			//should pass EValTemplateItem Id instead of EvalItem Id
			//UIInternalLink.make(essay, "essayResponse", new EssayResponseParams(ViewEssayResponseProducer.VIEW_ID,evalId,myItem.getId()));		
			UIInternalLink.make(essay, "essayResponse", new EssayResponseParams(ViewEssayResponseProducer.VIEW_ID,evalId,myTempItem.getId()));
						 //$NON-NLS-1$
		} else if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_HEADER)) { //"Text Header"
			UIBranchContainer header = UIBranchContainer.make(radiobranch,
			"headerType:"); //$NON-NLS-1$
			UIOutput.make(header, "itemNum", (new Integer(i + 1)).toString()); //$NON-NLS-1$
			UIOutput.make(header, "itemText", myItem.getItemText()); //$NON-NLS-1$
		}
	}
	
	public List reportNavigationCases() {
		List i = new ArrayList();

		return i;
	}




}
