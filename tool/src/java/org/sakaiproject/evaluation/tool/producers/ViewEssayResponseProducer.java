/******************************************************************************
 * ViewEssayResponseProducer.java - created on Oct 05, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * 
 * Rui Feng (fengr@vt.edu)
 * Will Humphries (whumphri@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool.producers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.params.EssayResponseParams;
import org.sakaiproject.evaluation.tool.params.TemplateViewParameters;
import org.sakaiproject.evaluation.tool.utils.TemplateItemUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
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
 * rendering the Short Answer/Essay part report of an evaluation
 * 
 * @author: Rui Feng (fengr@vt.edu)
 * @author:Will Humphries (whumphri@vt.edu)
 */

public class ViewEssayResponseProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

	public static final String VIEW_ID = "view_essay_response";
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}
	private EvalEvaluationsLogic evalsLogic;
	public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
		this.evalsLogic = evalsLogic;
	}

	private EvalResponsesLogic responsesLogic;	
	public void setResponsesLogic(EvalResponsesLogic responsesLogic) {
		this.responsesLogic = responsesLogic;
	}

	public ViewParameters getViewParameters() {
		return new EssayResponseParams(VIEW_ID, null, null, null);
	}	



	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIMessage.make(tofill, "view-essay-title", "viewessay.page.title");								//$NON-NLS-1$ //$NON-NLS-2$
		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"),			//$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID)); 

		EssayResponseParams essayResponseParams = (EssayResponseParams) viewparams;
		UIInternalLink.make(tofill, "report-groups-title", UIMessage.make("reportgroups.page.title"), 	//$NON-NLS-1$ //$NON-NLS-2$ 
				new TemplateViewParameters(ChooseReportGroupsProducer.VIEW_ID, 
						essayResponseParams.evalId)); 
		
		UIInternalLink.make(tofill, "viewReportLink", UIMessage.make("viewreport.page.title"), 			//$NON-NLS-1$ //$NON-NLS-2$
				new TemplateViewParameters(ViewReportProducer.VIEW_ID, 
						essayResponseParams.evalId));	
		
		/*
		 * Note: The groups id's would always be passed
		 * whether it is for single item or all the items.
		 */ 
		
		//output single set of essay responses
		if(essayResponseParams.itemId != null){
			//we are actually passing EvalTemplateItem ID
			EvalTemplateItem myTempItem = itemsLogic.getTemplateItemById(essayResponseParams.itemId);
			EvalItem myItem = myTempItem.getItem();

			String cat = myTempItem.getItemCategory();

			UIBranchContainer radiobranch = null;
			UIBranchContainer courseSection = null;
			UIBranchContainer instructorSection = null;
			if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_COURSE)) {//"Course"
				courseSection = UIBranchContainer.make(tofill,
				"courseSection:");	
				UIMessage.make(courseSection, "course-questions-header", "takeeval.course.questions.header"); //$NON-NLS-1$ //$NON-NLS-2$			
				radiobranch = UIBranchContainer.make(courseSection,
						"itemrow:first", Integer.toString(0));
				this.doFillComponent(myItem, essayResponseParams.evalId, 0, essayResponseParams.groupIds, radiobranch,
						courseSection);
			} else if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR)) {//"Instructor"
				instructorSection = UIBranchContainer.make(tofill,"instructorSection:");		
				UIMessage.make(instructorSection, "instructor-questions-header","takeeval.instructor.questions.header");			 //$NON-NLS-1$ //$NON-NLS-2$		
				radiobranch = UIBranchContainer.make(instructorSection,
						"itemrow:first", Integer.toString(0));
				this.doFillComponent(myItem, essayResponseParams.evalId, 0, essayResponseParams.groupIds, radiobranch,
						instructorSection);
			}

		}

		//prepare sets of responses for each essay question
		else if (essayResponseParams.evalId != null) {
			EvalEvaluation evaluation = evalsLogic.getEvaluationById(essayResponseParams.evalId);
			// get template from DAO 
			EvalTemplate template = evaluation.getTemplate();

			// get items(parent items, child items --need to set order

			List allItems = new ArrayList(template.getTemplateItems());

			if (! allItems.isEmpty()) {
				List ncItemsList = TemplateItemUtils.getNonChildItems(allItems); //already sorted by displayOrder

				// check if there is any "Course" items or "Instructor" items;
				UIBranchContainer courseSection = null;
				UIBranchContainer instructorSection = null;

				if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_COURSE, ncItemsList))	{	
					courseSection = UIBranchContainer.make(tofill,"courseSection:"); //$NON-NLS-1$
				}

				if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, ncItemsList))	{	
					instructorSection = UIBranchContainer.make(tofill,"instructorSection:"); //$NON-NLS-1$
				}

				for (int i = 0; i < ncItemsList.size(); i++) {
					EvalTemplateItem tempItem1 = (EvalTemplateItem) ncItemsList.get(i);
					EvalItem item1 = tempItem1.getItem();
					String cat = tempItem1.getItemCategory();

					UIBranchContainer radiobranch = null;
					if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_COURSE) 
							&& item1.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)){
						//"Course","Short Answer/Essay"
						radiobranch = UIBranchContainer.make(courseSection,
								"itemrow:first", Integer.toString(i));
						if (i % 2 == 1)
							radiobranch.decorators = new DecoratorList(
									new UIColourDecorator(
											null,
											Color
											.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));

						this.doFillComponent(item1, evaluation.getId(), i, essayResponseParams.groupIds, radiobranch,
								courseSection);
					} else if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR) &&
							item1.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
						//"Instructor","Short Answer/Essay"
						radiobranch = UIBranchContainer.make(instructorSection,
								"itemrow:first", Integer.toString(i));
						if (i % 2 == 1)
							radiobranch.decorators = new DecoratorList(
									new UIColourDecorator(
											null,
											Color
											.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));
						this.doFillComponent(item1, evaluation.getId(), i, essayResponseParams.groupIds, radiobranch,
								instructorSection);
					}
				} // end of for loop

			}

		}

	}

	private void doFillComponent(EvalItem myItem, Long evalId, int i, String[] groupIds, 
			UIBranchContainer radiobranch, UIContainer tofill) {

		if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
			//"Short Answer/Essay"
			UIBranchContainer essay = UIBranchContainer.make(radiobranch,
			"essayType:");
			UIOutput.make(essay, "itemNum", (new Integer(i + 1)).toString());
			UIOutput.make(essay, "itemText", myItem.getItemText());

			List itemAnswers= responsesLogic.getEvalAnswers(myItem.getId(), evalId, groupIds);

			//count the number of answers that match this one
			for(int y=0; y<itemAnswers.size();y++){
				UIBranchContainer answerbranch = UIBranchContainer.make(essay, "answers:", Integer.toString(y));
				EvalAnswer curr=(EvalAnswer)itemAnswers.get(y);
				UIOutput.make(answerbranch, "answerNum", new Integer(y+1).toString());
				UIOutput.make(answerbranch, "itemAnswer", curr.getText());					
			}
		}
	}

	public List reportNavigationCases() {
		List i = new ArrayList();

		return i;
	}

}
