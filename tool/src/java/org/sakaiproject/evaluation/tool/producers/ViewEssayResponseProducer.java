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
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.params.EssayResponseParams;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;


import uk.org.ponder.messageutil.MessageLocator;
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
 * rendering the Short Answer/Essay part report of an evaluation
 * 
 * @author:Will Humphries (whumphri@vt.edu)
 * @author: Rui Feng (fengr@vt.edu)
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
	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}
	
	public ViewParameters getViewParameters() {
		return new EssayResponseParams(VIEW_ID, null, null);
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
			EvalItem item1 = (EvalItem) itemList.get(j);
			String category = item1.getCategory();
			String classification = item1.getClassification();
			if (bl && category.equals(EvalConstants.ITEM_CATEGORY_COURSE) 
					&& classification.equals(EvalConstants.ITEM_TYPE_TEXT)) {
				//"Course","Short Answer/Essay"
				rs = true;
				break;
			}

			if (bl == false && category.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR) 
					&& classification.equals(EvalConstants.ITEM_TYPE_TEXT)) {
				//"Instructor","Short Answer/Essay"
				rs = true;
				break;
			}
		}

		return rs;
	}	
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		
		UIOutput.make(tofill, "view-essay-title",  messageLocator.getMessage("viewessay.page.title"));
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),
				new SimpleViewParameters(SummaryProducer.VIEW_ID)); //$NON-NLS-1$ //$NON-NLS-2$

		EssayResponseParams essayResponseParams = (EssayResponseParams) viewparams;
		UIInternalLink.make(tofill, "viewReportLink", messageLocator.getMessage("viewreport.page.title"), 
				new EvalViewParameters(ViewReportProducer.VIEW_ID, 
					essayResponseParams.evalId, SummaryProducer.VIEW_ID));		
		//output single set of essay responses
		if(essayResponseParams.itemId != null){
			EvalItem myItem = itemsLogic.getItemById(essayResponseParams.itemId);//EvalItem myItem = logic.getItemById(essayResponseParams.itemId);
			String cat = myItem.getCategory();
			UIBranchContainer radiobranch = null;
			UIBranchContainer courseSection = null;
			UIBranchContainer instructorSection = null;
			if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_COURSE)) {//"Course"
				courseSection = UIBranchContainer.make(tofill,
				"courseSection:");	
				UIOutput.make(courseSection, "course-questions-header", messageLocator.getMessage("takeeval.course.questions.header")); //$NON-NLS-1$ //$NON-NLS-2$			
				radiobranch = UIBranchContainer.make(courseSection,
						"itemrow:first", Integer.toString(0));
				this.doFillComponent(myItem, essayResponseParams.evalId, 0, radiobranch,
						courseSection);
			} else if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR)) {//"Instructor"
				instructorSection = UIBranchContainer.make(tofill,"instructorSection:");		
				UIOutput.make(instructorSection, "instructor-questions-header", messageLocator.getMessage("takeeval.instructor.questions.header"));			 //$NON-NLS-1$ //$NON-NLS-2$		
				radiobranch = UIBranchContainer.make(instructorSection,
						"itemrow:first", Integer.toString(0));
				this.doFillComponent(myItem, essayResponseParams.evalId, 0, radiobranch,
						instructorSection);
			}

		}
		
		//prepare sets of responses for each essay question
		else if (essayResponseParams.evalId != null) {
			EvalEvaluation evaluation = evalsLogic.getEvaluationById(essayResponseParams.evalId);
			//EvalEvaluation evaluation = logic.getEvaluationById(essayResponseParams.evalId);
			// get template from DAO 
			EvalTemplate template = evaluation.getTemplate();

			// get items(parent items, child items --need to set order

			List childItems = new ArrayList(template.getItems());
			if (! childItems.isEmpty()) {
				//Collections.sort(childItems, new ReportItemOrderComparator());
				Collections.sort(childItems,new PreviewEvalProducer.EvaluationItemOrderComparator());
				// check if there is any "Course" items or "Instructor" items;
				UIBranchContainer courseSection = null;
				UIBranchContainer instructorSection = null;
				if (this.findItemCategory(true, childItems))
					courseSection = UIBranchContainer.make(tofill,
							"courseSection:");
				if (this.findItemCategory(false, childItems))
					instructorSection = UIBranchContainer.make(tofill,
							"instructorSection:");
				
				for (int i = 0; i < childItems.size(); i++) {
					EvalItem item1 = (EvalItem) childItems.get(i);

					String cat = item1.getCategory();
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

						this.doFillComponent(item1, evaluation.getId(), i, radiobranch,
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
						this.doFillComponent(item1, evaluation.getId(), i, radiobranch,
								instructorSection);
					}
				} // end of for loop

			}

		}
		
	}
	
	private void doFillComponent(EvalItem myItem, Long evalId, int i,
			UIBranchContainer radiobranch, UIContainer tofill) {

		if  (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
			//"Short Answer/Essay"
			UIBranchContainer essay = UIBranchContainer.make(radiobranch,
			"essayType:");
			UIOutput.make(essay, "itemNum", (new Integer(i + 1)).toString());
			UIOutput.make(essay, "itemText", myItem.getItemText());
			
			List itemAnswers= responsesLogic.getEvalAnswers(myItem.getId(), evalId);
			
				//count the number of answers that match this one
				for(int y=0; y<itemAnswers.size();y++){
			    	UIBranchContainer answerbranch = UIBranchContainer.make(essay, "answers:", Integer.toString(y));
					EvalAnswer curr=(EvalAnswer)itemAnswers.get(y);
					UIOutput.make(answerbranch, "answerNum", new Integer(y+1).toString());
					UIOutput.make(answerbranch, "itemAnswer", curr.getText());					
				}
		}
	}
	/*
	private static class ReportItemOrderComparator implements Comparator {
		public int compare(Object eval0, Object eval1) {
			// expects to get EvalItem objects
			return ((EvalItem)eval0).getId().
				compareTo(((EvalItem)eval1).getId());
		}
	}
	*/
	public List reportNavigationCases() {
		List i = new ArrayList();
		//TODO
		return i;
	}




}
