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
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.params.PreviewEvalParameters;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.utils.TemplateItemUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIColourDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This is the producer page for preview a template or evaluation
 * 
 * @author: Rui Feng (fengr@vt.edu)
 * @author: Kapil Ahuja (kahuja@vt.edu)
 */
public class PreviewEvalProducer implements ViewComponentProducer, ViewParamsReporter {

	public static final String VIEW_ID = "preview_eval";
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

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	private ItemRenderer itemRenderer;
	public void setItemRenderer(ItemRenderer itemRenderer) {
		this.itemRenderer = itemRenderer;
	}

	int displayedItems=1;//used to determine the number to display next to each item
	int colorCounter=0;//used to determine whether to color the background of an item
	/* 
	 * 1). Preview Template --getting data from DAO --DONE
	 * 2). Preview Evaluation -- by passing Evalution ID,course--TO BE DONE
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {

		//TODO - i18n UIBoundBoolean's for N/a

		UIMessage.make(tofill,"control-panel-title", "controlpanel.page.title");		
		UIMessage.make(tofill, "preview-eval-title", "previeweval.page.title");		

		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"), 
				new SimpleViewParameters(SummaryProducer.VIEW_ID));		

		PreviewEvalParameters previewEvalViewParams = (PreviewEvalParameters)viewparams;

		UIMessage.make(tofill, "evaluation-title-header", "previeweval.evaluation.title.header");
		UIMessage.make(tofill, "course-title-header", "previeweval.course.title.header");
		UIMessage.make(tofill, "instructions-title-header", "previeweval.instructions.title.header");

		if (previewEvalViewParams.originalPage != null){
			if (previewEvalViewParams.originalPage.equals(ModifyTemplateItemsProducer.VIEW_ID) || 
					previewEvalViewParams.originalPage.equals(EvaluationStartProducer.VIEW_ID) ){
				//get template ID
				UIMessage.make(tofill, "evalTitle", "previeweval.evaluation.title.default");//no evaluation title, use dummy
				UIMessage.make(tofill, "courseTitle", "previeweval.course.title.default");//no course title, use dummy
				UIMessage.make(tofill, "instruction", "previeweval.instructions.default");//no instruction, use dummy
			}

			if (previewEvalViewParams.originalPage.equals(ControlEvaluationsProducer.VIEW_ID)){

				//get eval ID, need to get assigned courses from eval ID
				if (previewEvalViewParams.evaluationId != null){

					EvalEvaluation eval = evalsLogic.getEvaluationById(previewEvalViewParams.evaluationId);
					UIOutput.make(tofill, "evalTitle", eval.getTitle());

					int count = evalsLogic.countEvaluationGroups(eval.getId());
					if (count > 1){
						UIOutput.make(tofill, "courseTitle",count+"courses");
					}else{
				
						Long[] evalIds = {eval.getId()};
						Map evalContexts = evalsLogic.getEvaluationGroups(evalIds, true);
						List contexts = (List) evalContexts.get(eval.getId());
						EvalGroup ctxt = (EvalGroup) contexts.get(0);
						String title = ctxt.title;
						UIOutput.make(tofill, "courseTitle",title);

					}
					if (eval.getInstructions() != null)
						UIVerbatim.make(tofill, "instruction", eval.getInstructions());
					else UIMessage.make(tofill, "instruction", "previeweval.instructions.default");
				}
			}

			if (previewEvalViewParams.originalPage.equals(SummaryProducer.VIEW_ID)){

				if (previewEvalViewParams.evaluationId != null){

					EvalEvaluation eval = evalsLogic.getEvaluationById(previewEvalViewParams.evaluationId);
					UIOutput.make(tofill, "evalTitle", eval.getTitle());

					if (previewEvalViewParams.context != null){
						//get course title from context
						UIOutput.make(tofill, "courseTitle",external.getDisplayTitle(previewEvalViewParams.context)); 
					}else{ //get evalID, need to get assigned courses from eval ID
						int count = evalsLogic.countEvaluationGroups(eval.getId());
						if (count > 1){
							UIOutput.make(tofill, "courseTitle",count+"courses");
						}else{
							Long[] evalIds = {eval.getId()};
							Map evalContexts = evalsLogic.getEvaluationGroups(evalIds, true);
							List contexts = (List) evalContexts.get(eval.getId());
							EvalGroup ctxt = (EvalGroup) contexts.get(0);
							String title = ctxt.title;
							UIOutput.make(tofill, "courseTitle",title);

						}
					}
					if (eval.getInstructions() != null)
						UIOutput.make(tofill, "instruction",eval.getInstructions());
					else UIMessage.make(tofill, "instruction", "previeweval.instructions.default");

				}
			} //end of: if (previewEvalViewParams.originalPage.equals(SummaryProducer.VIEW_ID))



			EvalTemplate template = null;
			template = templatesLogic.getTemplateById(previewEvalViewParams.templateId);
			//template = logic.getTemplateById(previewEvalViewParams.templateId);
			if (template.getDescription() != null){
				//show description
				UIBranchContainer showDescr = UIBranchContainer.make(tofill, "showDescription:");
				UIOutput.make(showDescr, "description", template.getDescription());
			}


			// get items(parent items, child items --need to set order
			List allItems = new ArrayList(template.getTemplateItems());

			/*
			 * With new table design, all the items(including child items) are included
			 * 
			 * */
			if (! allItems.isEmpty()) {
				//filter out the block child items, to get a list non-child items
				List ncItemsList = TemplateItemUtils.getNonChildItems(allItems);

				// check if there is any "Course" items or "Instructor" items;
				UIBranchContainer courseSection = null;
				UIBranchContainer instructorSection = null;

				
				if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_COURSE, ncItemsList))	{	
					colorCounter=0;
					courseSection = UIBranchContainer.make(tofill,"courseSection:");
					UIMessage.make(courseSection, "course-questions-header", "previeweval.course.questions.header"); 
					for (int i = 0; i < ncItemsList.size(); i++) {	
						EvalTemplateItem templateItem = (EvalTemplateItem) ncItemsList.get(i);
						
						String cat = templateItem.getItemCategory();
						if (cat == null) {
							throw new IllegalStateException("Template item with null category found: " + templateItem.getId() );
						} else if (EvalConstants.ITEM_CATEGORY_COURSE.equals(cat)) {
							doFillComponent(courseSection, templateItem, colorCounter);
							ncItemsList.remove(i);
							i--;
							colorCounter++;
						} 
					}// end of for loop	
				}

				if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, ncItemsList))	{	
					colorCounter=0;
					instructorSection = UIBranchContainer.make(tofill,"instructorSection:");
					UIMessage.make(instructorSection, "instructor-questions-header", "previeweval.instructor.questions.header"); 
					for (int i = 0; i < ncItemsList.size(); i++) {	
						EvalTemplateItem templateItem = (EvalTemplateItem) ncItemsList.get(i);
						
						String cat = templateItem.getItemCategory();
						if (cat == null) {
							throw new IllegalStateException("Template item with null category found: " + templateItem.getId() );
						} else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(cat)) {
							doFillComponent(instructorSection, templateItem, colorCounter);
							ncItemsList.remove(i);
							i--;
							colorCounter++;
						} 
					}// end of for loop		
				}
				if (ncItemsList.size() > 0) {
					throw new IllegalStateException("Items found with categories that are not rendered");
				}

				

			}//end of:if (! allItems.isEmpty())
		} //end of:if (previewEvalViewParams.originalPage != null)
	}

	
	/**
	 * @param section
	 * @param templateItem
	 * @param colorCounter
	 */
	private void doFillComponent(UIBranchContainer section, EvalTemplateItem templateItem, int colorCounter){
		UIBranchContainer itemsBranch = null;
		itemsBranch = UIBranchContainer.make(section, "itemrow:first", Integer.toString(displayedItems));

		// use the renderer evolver
		itemRenderer.renderItem(itemsBranch, "previewed-item:", null, templateItem, displayedItems, true);
		if(TemplateItemUtils.getTemplateItemType(templateItem).equals(EvalConstants.ITEM_TYPE_BLOCK_PARENT)){
			List childList = itemsLogic.getBlockChildTemplateItemsForBlockParent(templateItem.getId(), false);
			displayedItems += childList.size();
		} else if (TemplateItemUtils.getTemplateItemType(templateItem).equals(EvalConstants.ITEM_TYPE_HEADER)) { 
			// no change, do not count header
		} else {
			displayedItems++;
		}
		//increment by 1 if not block, else increment by num of block children

		if (colorCounter % 2 == 1) {
			itemsBranch.decorators = new DecoratorList( new UIColourDecorator(null,
					Color.decode(EvaluationConstant.LIGHT_GRAY_COLOR)) );
		}
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters() {
		return new PreviewEvalParameters();
	}

}
