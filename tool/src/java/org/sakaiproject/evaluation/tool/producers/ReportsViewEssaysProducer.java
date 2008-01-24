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
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.viewparams.EssayResponseParams;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;
import org.sakaiproject.util.FormattedText;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIColourDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
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

public class ReportsViewEssaysProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

	public static final String VIEW_ID = "view_essay_response";
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
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



	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		String currentUserId = externalLogic.getCurrentUserId();

		UIMessage.make(tofill, "view-essay-title", "viewessay.page.title");								
		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"),			
				new SimpleViewParameters(SummaryProducer.VIEW_ID)); 

		EssayResponseParams essayResponseParams = (EssayResponseParams) viewparams;
		UIInternalLink.make(tofill, "report-groups-title", UIMessage.make("reportgroups.page.title"), 	 
				new TemplateViewParameters(ReportChooseGroupsProducer.VIEW_ID, 
						essayResponseParams.evalId)); 
		
		UIInternalLink.make(tofill, "viewReportLink", UIMessage.make("viewreport.page.title"), 			
				new ReportParameters(ReportsViewingProducer.VIEW_ID, 
						essayResponseParams.evalId, essayResponseParams.groupIds));	

		// Note: The groups id's should always be passed whether it is for single item or all the items

		if (essayResponseParams.evalId != null) {
			EvalEvaluation evaluation = evalsLogic.getEvaluationById(essayResponseParams.evalId);

			// do a permission check
			if (! currentUserId.equals(evaluation.getOwner()) &&
					! externalLogic.isUserAdmin(currentUserId)) { // TODO - this check is no good, we need a real one -AZ
				throw new SecurityException("Invalid user attempting to access reports page: " + currentUserId);
			}

			// get template from DAO 
			EvalTemplate template = evaluation.getTemplate();

			//output single set of essay responses
			if (essayResponseParams.itemId != null) {
				//we are actually passing EvalTemplateItem ID
				EvalTemplateItem myTempItem = itemsLogic.getTemplateItemById(essayResponseParams.itemId);
				EvalItem myItem = myTempItem.getItem();

				String cat = myTempItem.getItemCategory();

				UIBranchContainer radiobranch = null;
				UIBranchContainer courseSection = null;
				UIBranchContainer instructorSection = null;
				if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_COURSE)) {//"Course"
					courseSection = UIBranchContainer.make(tofill, "courseSection:");	
					UIMessage.make(courseSection, "course-questions-header", "takeeval.group.questions.header"); 			
					radiobranch = UIBranchContainer.make(courseSection, "itemrow:first", "0");
					this.doFillComponent(myItem, essayResponseParams.evalId, 0, essayResponseParams.groupIds, 
							radiobranch, courseSection);
				} else if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR)) {//"Instructor"
					instructorSection = UIBranchContainer.make(tofill,"instructorSection:");		
					UIMessage.make(instructorSection, "instructor-questions-header","takeeval.instructor.questions.header");			 		
					radiobranch = UIBranchContainer.make(instructorSection, "itemrow:first", "0");
					this.doFillComponent(myItem, essayResponseParams.evalId, 0, essayResponseParams.groupIds, 
							radiobranch, instructorSection);
				}
			} else {
				// get all items since one is not specified
				List allItems = new ArrayList(template.getTemplateItems());

				if (! allItems.isEmpty()) {
					List ncItemsList = TemplateItemUtils.getNonChildItems(allItems); //already sorted by displayOrder

					// check if there are any "Course" items or "Instructor" items;
					UIBranchContainer courseSection = null;
					UIBranchContainer instructorSection = null;

					if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_COURSE, ncItemsList))	{	
						courseSection = UIBranchContainer.make(tofill, "courseSection:"); //$NON-NLS-1$
					}

					if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, ncItemsList))	{	
						instructorSection = UIBranchContainer.make(tofill, "instructorSection:"); //$NON-NLS-1$
					}

					for (int i = 0; i < ncItemsList.size(); i++) {
						EvalTemplateItem tempItem1 = (EvalTemplateItem) ncItemsList.get(i);
						EvalItem item1 = tempItem1.getItem();
						String cat = tempItem1.getItemCategory();

						UIBranchContainer radiobranch = null;
						if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_COURSE) 
								&& item1.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)){
							//"Course","Short Answer/Essay"
							radiobranch = UIBranchContainer.make(courseSection,	"itemrow:first", i+"");
							// need the alt row highlights between essays, not groups of essays
							// if (i % 2 == 1)
							//	radiobranch.decorators = new DecoratorList(
							//			new UIColourDecorator(null, Color.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));

							this.doFillComponent(item1, evaluation.getId(), i, essayResponseParams.groupIds, 
									radiobranch, courseSection);
						} else if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR) &&
								item1.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
							//"Instructor","Short Answer/Essay"
							radiobranch = UIBranchContainer.make(instructorSection,	"itemrow:first", i+"");
							// need the alt row highlights between essays, not groups of essays
							// if (i % 2 == 1)
							//	radiobranch.decorators = new DecoratorList(
							//			new UIColourDecorator(null,	Color.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));
							this.doFillComponent(item1, evaluation.getId(), i, essayResponseParams.groupIds, 
									radiobranch, instructorSection);
						}
					} // end of for loop
				}
			}
		}
	}

	/**
	 * @param myItem
	 * @param evalId
	 * @param i
	 * @param groupIds
	 * @param radiobranch
	 * @param tofill
	 */
	private void doFillComponent(EvalItem myItem, Long evalId, int i, String[] groupIds, 
			UIBranchContainer radiobranch, UIContainer tofill) {

		if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
			//"Short Answer/Essay"
			UIBranchContainer essay = UIBranchContainer.make(radiobranch, "essayType:");
			UIOutput.make(essay, "itemNum", (i+1)+"");
			UIOutput.make(essay, "itemText", FormattedText.convertFormattedTextToPlaintext(myItem.getItemText()));

			List itemAnswers= responsesLogic.getEvalAnswers(myItem.getId(), evalId, groupIds);

			//count the number of answers that match this one
			for (int y=0; y < itemAnswers.size(); y++){
				UIBranchContainer answerbranch = UIBranchContainer.make(essay, "answers:", y+"");
                if (y % 2 == 1)
                	answerbranch.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class

				EvalAnswer curr=(EvalAnswer)itemAnswers.get(y);
				UIOutput.make(answerbranch, "answerNum", new Integer(y+1).toString());
				UIOutput.make(answerbranch, "itemAnswer", curr.getText());					
			}
		}
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
		return i;
	}

}
