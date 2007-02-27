/******************************************************************************
 * TakeEvalProducer.java - created on Sept 18, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Will Humphries (whumphri@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.LocalResponsesLogic;
import org.sakaiproject.evaluation.tool.params.EvalTakeViewParameters;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.utils.TemplateItemUtils;

import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIColourDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;


/**
 * This page is for a user with take evaluation permission to fill and submit the evaluation
 * 
 * @author: Will Humphries (whumphri@vt.edu)
 * @author: Kapil Ahuja (kahuja@vt.edu)
 */

public class TakeEvalProducer implements ViewComponentProducer,
	ViewParamsReporter, NavigationCaseReporter {

	public static final String VIEW_ID = "take_eval"; //$NON-NLS-1$
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

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	ItemRenderer itemRenderer;
	public void setItemRenderer(ItemRenderer itemRenderer) {
		this.itemRenderer = itemRenderer;
	}
	
	public ViewParameters getViewParameters() {
		return new EvalTakeViewParameters(VIEW_ID, null, null, null);
	}
	
	private LocalResponsesLogic localResponsesLogic;
	public void setLocalResponsesLogic(LocalResponsesLogic localResponsesLogic) {
		this.localResponsesLogic = localResponsesLogic;
	}

    String responseOTPBinding = "responseBeanLocator";
    String responseOTP = responseOTPBinding + ".";
    String newResponseOTPBinding = responseOTP + "new";
    String newResponseOTP = newResponseOTPBinding + ".";
  
    String responseAnswersOTPBinding = "responseAnswersBeanLocator";
    String responseAnswersOTP = responseAnswersOTPBinding + ".";
    String newResponseAnswersOTPBinding = responseAnswersOTP + "new";
    String newResponseAnswersOTP = newResponseAnswersOTPBinding + ".";    
    
    String evalOTPBinding="evaluationBeanLocator";
    String evalOTP = evalOTPBinding+".";
    Long responseId;
    Long evalId;
    String evalGroupId;

	int displayNumber=1;
	int renderedItemCount=0;
    
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		UIMessage.make(tofill, "take-eval-title", "takeeval.page.title"); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));			
		
		UIMessage.make(tofill, "eval-title-header", "takeeval.eval.title.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(tofill, "course-title-header", "takeeval.course.title.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(tofill, "instructions-header", "takeeval.instructions.header");	 //$NON-NLS-1$ //$NON-NLS-2$
		
		
		EvalTakeViewParameters evalTakeViewParams = (EvalTakeViewParameters) viewparams;
		EvalEvaluation eval = evalsLogic.getEvaluationById(evalTakeViewParams.evaluationId);
		responseId = evalTakeViewParams.responseId;
		evalId = evalTakeViewParams.evaluationId;
		evalGroupId = evalTakeViewParams.context;
		if(eval !=null && evalTakeViewParams.context != null){
			UIOutput.make(tofill, "evalTitle", eval.getTitle()); //$NON-NLS-1$
			//get course title: from sakaicontext to course title
			//String title = logic.getDisplayTitle(evalTakeViewParams.context);
			String title = external.getDisplayTitle(evalTakeViewParams.context); 
			
			UIOutput.make(tofill, "courseTitle", title); //$NON-NLS-1$
			EvalTemplate et = eval.getTemplate();
			if(et.getDescription() != null)
				UIOutput.make(tofill, "description", et.getDescription()); //$NON-NLS-1$
			else
				UIMessage.make(tofill, "description", "takeeval.description.filler"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		
		UIVerbatim.make(tofill, "evalInstruction", eval.getInstructions()); //$NON-NLS-1$
		
		
		UIForm form = UIForm.make(tofill, "evaluationForm"); //$NON-NLS-1$

		//Binding the EvalEvaluation object to the EvalEvaluation object in TakeEvaluationBean.
		form.parameters.add( new UIELBinding("#{takeEvalBean.eval}", new ELReference(evalOTP+eval.getId()))); //$NON-NLS-1$
		form.parameters.add( new UIELBinding("#{takeEvalBean.context}", evalTakeViewParams.context));
		
		EvalTemplate template = eval.getTemplate();

		// get items(parent items, child items --need to set order
		//List childItems = new ArrayList(template.getItems());	//List allItems = new ArrayList(template.getItems());
		List allItems = new ArrayList(template.getTemplateItems());
	
		//filter out the block child items, to get a list non-child items
		List ncItemsList = TemplateItemUtils.getNonChildItems(allItems);
		
		HashMap answerMap=null;
		if(responseId!=null) {
			answerMap = localResponsesLogic.getAnswersMapByTempItemAndAssociated(responseId);
		}
		
		// these will be used if there are any "Course" items or "Instructor" items, respectively
		UIBranchContainer courseSection = null;
		UIBranchContainer instructorSection = null;

		if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_COURSE, ncItemsList))	{	
			courseSection = UIBranchContainer.make(form,"courseSection:"); //$NON-NLS-1$
			UIMessage.make(courseSection, "course-questions-header", "takeeval.course.questions.header"); //$NON-NLS-1$ //$NON-NLS-2$			
			for (int i = 0; i <ncItemsList.size(); i++) {
				//EvalItem item1 = (EvalItem) ncItemsList.get(i);
				EvalTemplateItem tempItem1 = (EvalTemplateItem) ncItemsList.get(i);
				
				String cat = tempItem1.getItemCategory();
				UIBranchContainer radiobranch = null;
				
				if (cat.equals(EvalConstants.ITEM_CATEGORY_COURSE)) { //"Course"
					radiobranch = UIBranchContainer.make(courseSection, "itemrow:first", Integer.toString(i)); //$NON-NLS-1$
					if (i % 2 == 1) {
						radiobranch.decorators = new DecoratorList(new UIColourDecorator(null,Color.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));
					}
					renderItemPrep(radiobranch, form, tempItem1, answerMap, "null", "null");
				}
			}
			UIMessage.make(courseSection, "course-questions-header", "takeeval.course.questions.header"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, ncItemsList))	{	
			Set instructors = external.getUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_BE_EVALUATED);
			//for each instructor, make a branch containing all instructor questions
			for (Iterator it = instructors.iterator(); it.hasNext();) {
				String instructor = (String) it.next();
				instructorSection = UIBranchContainer.make(form,"instructorSection:", "inst"+displayNumber); //$NON-NLS-1$
				//UIOutput.make(instructorSection, "instructor-questions-header",UIMessage.make("takeeval.instructor.questions.header")+" "+external.getUserDisplayName(instructor));	
				UIMessage.make(instructorSection, "instructor-questions-header","takeeval.instructor.questions.header", new Object[] { external.getUserDisplayName(instructor) });
				//for each item in this evaluation
				for (int i = 0; i <ncItemsList.size(); i++) {
					EvalTemplateItem tempItem1 = (EvalTemplateItem) ncItemsList.get(i);
					String cat = tempItem1.getItemCategory();
					UIBranchContainer radiobranch = null;
					
					//if the given item is of type instructor, render it here
					if (cat != null && cat.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR)) { //"Instructor"
						radiobranch = UIBranchContainer.make(instructorSection,
								"itemrow:first", Integer.toString(i)); //$NON-NLS-1$
						if (i % 2 == 1) radiobranch.decorators = new DecoratorList(new UIColourDecorator(null, Color.decode(EvaluationConstant.LIGHT_GRAY_COLOR)));
						renderItemPrep(radiobranch, form, tempItem1, answerMap, cat, instructor);
					}
				} // end of for loop				
			}
		}
		UICommand.make(form, "submitEvaluation", UIMessage.make("takeeval.submit.button"), "#{takeEvalBean.submitEvaluation}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	} // end of method

	private void renderItemPrep(UIBranchContainer radiobranch, UIForm form, EvalTemplateItem tempItem1, HashMap answerMap, String itemCategory, String associatedId) {
	    //holds array of bindings for child block items
		String[] caOTP=null;					
	    //holds list of block child items if tempItem1 is a block parent
	    List childList=null;
	    //if tempItem1 is a blockParent
		if (tempItem1.getBlockParent() != null
				&& tempItem1.getBlockParent().booleanValue() == true) {
			//get the child items of tempItem1
			childList = itemsLogic.getBlockChildTemplateItemsForBlockParent(tempItem1.getId(), false);
			caOTP = new String[childList.size()];
			//for each child item, construct a binding
			for (int j = 0; j < childList.size(); j++) {
				EvalTemplateItem currChildItem = (EvalTemplateItem)childList.get(j);
				//set up OTP paths
			    if (responseId == null) {
			    	caOTP[j] = newResponseAnswersOTP + "new" + (renderedItemCount) +".";
			    }
				else {
					//if the user has answered this question before, point at their response
					EvalAnswer currAnswer=(EvalAnswer)answerMap.get(currChildItem.getId()+itemCategory+associatedId);
					
					//there is an existing response, but there is no answer
					if(currAnswer==null) {
						caOTP[j] = responseAnswersOTP + responseId + "." + "new" + (renderedItemCount) +".";
					}
					else {
						caOTP[j] = responseAnswersOTP + responseId + "." + currAnswer.getId() + ".";
					}
				}

				//bind the current EvalTemplateItem's EvalItem to the current EvalAnswer's EvalItem
				form.parameters.add( new UIELBinding
						(caOTP[j] + "templateItem",new ELReference("templateItemBeanLocator." + currChildItem.getId())) );	
				if(itemCategory.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR) | itemCategory.equals(EvalConstants.ITEM_CATEGORY_ENVIRONMENT)){
					//bind the current instructor id to the current EvalAnswer.associated
					form.parameters.add( new UIELBinding(caOTP[j] + "associatedId", associatedId) );						
				}	
				//set up binding for UISelect
				caOTP[j]+="numeric";  
				renderedItemCount++;
			}
		}
		//single item being rendered
		else if(tempItem1.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_SCALED) | 
				tempItem1.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)){
			//set up OTP paths for scaled/text type items
		    String currAnswerOTP;
		    if (responseId == null) {
		    	currAnswerOTP = newResponseAnswersOTP + "new" + renderedItemCount +".";
		    }
			else {
				//if the user has answered this question before, point at their response
				EvalAnswer currAnswer=(EvalAnswer)answerMap.get(tempItem1.getId()+"null"+"null");
				
				if(currAnswer==null) {
					currAnswerOTP = responseAnswersOTP + responseId + "." + "new" + (renderedItemCount) +".";
				}
				else {
					currAnswerOTP = responseAnswersOTP + responseId + "." + currAnswer.getId() + ".";
				}
			}
			//bind the current EvalTemplateItem's EvalItem to the current EvalAnswer's EvalItem
			form.parameters.add( new UIELBinding
					(currAnswerOTP + "templateItem",new ELReference("templateItemBeanLocator." + tempItem1.getId())) );	
			if(itemCategory.equals(EvalConstants.ITEM_CATEGORY_INSTRUCTOR) | itemCategory.equals(EvalConstants.ITEM_CATEGORY_ENVIRONMENT)){			
				//bind the current instructor id to the current EvalAnswer.associated
				form.parameters.add( new UIELBinding(currAnswerOTP + "associatedId", associatedId) );
			}
			//update curranswerOTP for binding the UI input element (UIInput, UISelect, etc.)
			if(tempItem1.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_SCALED)){
		    	caOTP = new String[] {currAnswerOTP+"numeric"};
		    }
			if(tempItem1.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_TEXT)) {
		    	caOTP = new String[] {currAnswerOTP+"text"};
		    }	
		}
		//***Render the item.***
		itemRenderer.renderItem(radiobranch, "rendered-item:", caOTP, tempItem1, displayNumber, false);

		//***Increment counters***
		//if we displayed a block, renderedItem has been incremented, increment displayNumber by the number of blockChildren
		if(tempItem1.getBlockParent() != null
				&& tempItem1.getBlockParent().booleanValue() == true){
			displayNumber += childList.size();
		}
		//if we displayed 1 item, increment by 1
		else if(tempItem1.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_TEXT) | 
				tempItem1.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_SCALED)){
			displayNumber++;
			renderedItemCount++;
		}
		
	}
	
	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();

		i.add(new NavigationCase("success", new SimpleViewParameters(SummaryProducer.VIEW_ID)));

		return i;
	}
}
