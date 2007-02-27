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



	private ItemRenderer itemRenderer;
	public void setItemRenderer(ItemRenderer itemRenderer) {
		this.itemRenderer = itemRenderer;
	}


	/* 
	 * 1). Preview Template --getting data from DAO --DONE
	 * 2). Preview Evalution -- by passing Evalution ID,course--TO BE DONE
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {

		//TODO - i18n UIBoundBoolean's for N/a

		UIMessage.make(tofill,"control-panel-title", "controlpanel.page.title");		 //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(tofill, "preview-eval-title", "previeweval.page.title");		 //$NON-NLS-1$ //$NON-NLS-2$

		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));		

		PreviewEvalParameters previewEvalViewParams = (PreviewEvalParameters)viewparams;

		UIMessage.make(tofill, "evaluation-title-header", "previeweval.evaluation.title.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(tofill, "course-title-header", "previeweval.course.title.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(tofill, "instructions-title-header", "previeweval.instructions.title.header"); //$NON-NLS-1$ //$NON-NLS-2$

		if (previewEvalViewParams.originalPage != null){
			if (previewEvalViewParams.originalPage.equals(ModifyTemplateItemsProducer.VIEW_ID) || 
					previewEvalViewParams.originalPage.equals(EvaluationStartProducer.VIEW_ID) ){
				//get template ID
				UIMessage.make(tofill, "evalTitle", "previeweval.evaluation.title.default");//no evaluation title, use dummy //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(tofill, "courseTitle", "previeweval.course.title.default");//no course title, use dummy //$NON-NLS-1$ //$NON-NLS-2$
				UIMessage.make(tofill, "instruction", "previeweval.instructions.default");//no instruction, use dummy //$NON-NLS-1$ //$NON-NLS-2$
			}

			if (previewEvalViewParams.originalPage.equals(ControlPanelProducer.VIEW_ID)){

				//get eval ID, need to get assigned courses from eval ID
				if (previewEvalViewParams.evaluationId != null){

					EvalEvaluation eval = evalsLogic.getEvaluationById(previewEvalViewParams.evaluationId);
					UIOutput.make(tofill, "evalTitle", eval.getTitle()); //$NON-NLS-1$

					int count = evalsLogic.countEvaluationGroups(eval.getId());
					if (count > 1){
						UIOutput.make(tofill, "courseTitle",count+"courses"); //$NON-NLS-1$ //$NON-NLS-2$
					}else{
						/*
						List acs = assignsLogic.getAssignGroupsByEvalId(eval.getId());
						EvalAssignGroup eac = (EvalAssignGroup) acs.get(0);
						String title =  external.getDisplayTitle( eac.getEvalGroupId());
						UIOutput.make(tofill, "courseTitle",title);*/
					
						Long[] evalIds = {eval.getId()};
						Map evalContexts = evalsLogic.getEvaluationGroups(evalIds, true);
						List contexts = (List) evalContexts.get(eval.getId());
						EvalGroup ctxt = (EvalGroup) contexts.get(0);
						String title = ctxt.title;
						UIOutput.make(tofill, "courseTitle",title);

					}
					if (eval.getInstructions() != null)
						UIVerbatim.make(tofill, "instruction", eval.getInstructions()); //$NON-NLS-1$
					else UIMessage.make(tofill, "instruction", "previeweval.instructions.default"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			if (previewEvalViewParams.originalPage.equals(SummaryProducer.VIEW_ID)){

				if (previewEvalViewParams.evaluationId != null){

					EvalEvaluation eval = evalsLogic.getEvaluationById(previewEvalViewParams.evaluationId);
					UIOutput.make(tofill, "evalTitle", eval.getTitle()); //$NON-NLS-1$

					if (previewEvalViewParams.context != null){
						//get course title from context
						UIOutput.make(tofill, "courseTitle",external.getDisplayTitle(previewEvalViewParams.context)); 
					}else{ //get evalID, need to get assigned courses from eval ID
						int count = evalsLogic.countEvaluationGroups(eval.getId());
						if (count > 1){
							UIOutput.make(tofill, "courseTitle",count+"courses"); //$NON-NLS-1$ //$NON-NLS-2$
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
						UIOutput.make(tofill, "instruction",eval.getInstructions()); //$NON-NLS-1$
					else UIMessage.make(tofill, "instruction", "previeweval.instructions.default"); //$NON-NLS-1$ //$NON-NLS-2$

				}
			} //end of: if (previewEvalViewParams.originalPage.equals(SummaryProducer.VIEW_ID))



			EvalTemplate template = null;
			template = templatesLogic.getTemplateById(previewEvalViewParams.templateId);
			//template = logic.getTemplateById(previewEvalViewParams.templateId);
			if (template.getDescription() != null){
				//show description
				UIBranchContainer showDescr = UIBranchContainer.make(tofill, "showDescription:"); //$NON-NLS-1$
				UIOutput.make(showDescr, "description", template.getDescription()); //$NON-NLS-1$
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
					courseSection = UIBranchContainer.make(tofill,"courseSection:"); //$NON-NLS-1$
					UIMessage.make(courseSection, "course-questions-header", "previeweval.course.questions.header");  //$NON-NLS-1$ //$NON-NLS-2$
				}

				if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, ncItemsList))	{	
					instructorSection = UIBranchContainer.make(tofill,"instructorSection:"); //$NON-NLS-1$
					UIMessage.make(instructorSection, "instructor-questions-header", "previeweval.instructor.questions.header");  //$NON-NLS-1$ //$NON-NLS-2$
				}

				for (int i = 0; i < ncItemsList.size(); i++) {	
					EvalTemplateItem templateItem = (EvalTemplateItem) ncItemsList.get(i);

					String cat = templateItem.getItemCategory();
					UIBranchContainer itemsBranch = null;
					if (cat == null) {
						throw new IllegalStateException("Template item with null category found: " + templateItem.getId() );
					} else if (EvalConstants.ITEM_CATEGORY_COURSE.equals(cat)) {
						itemsBranch = UIBranchContainer.make(courseSection,
								"itemrow:first", Integer.toString(i)); //$NON-NLS-1$

						//this.doFillComponent(templateItem, i, radiobranch,courseSection, allItems);

					} else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(cat)) {
						itemsBranch = UIBranchContainer.make(instructorSection,
								"itemrow:first", Integer.toString(i)); //$NON-NLS-1$

						//this.doFillComponent(templateItem, i, radiobranch,instructorSection,allItems);

					} else {
						throw new IllegalStateException("No handling for this category available: " + cat);
					}

					if ( itemsBranch != null ) {
						// use the renderer evolver
						itemRenderer.renderItem(itemsBranch, "previewed-item:", null, templateItem, i, true);

						if (i % 2 == 1) {
							itemsBranch.decorators = new DecoratorList( new UIColourDecorator(null,
									Color.decode(EvaluationConstant.LIGHT_GRAY_COLOR)) );
						}
					}
				} // end of for loop
			}//end of:if (! allItems.isEmpty())
		} //end of:if (previewEvalViewParams.originalPage != null)
	}


	public ViewParameters getViewParameters() {
		return new PreviewEvalParameters(VIEW_ID, null,null,null,null);
	}

}
