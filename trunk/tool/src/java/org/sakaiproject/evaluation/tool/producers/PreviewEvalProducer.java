/******************************************************************************
 * PreviewEvalProducer.java - recreated by aaronz on 30 May 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * View for previewing a template or evaluation
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class PreviewEvalProducer implements ViewComponentProducer, ViewParamsReporter {

    public static final String VIEW_ID = "preview_eval";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalExternalLogic external;
    public void setExternal(EvalExternalLogic external) {
        this.external = external;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
       this.evaluationService = evaluationService;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
       this.authoringService = authoringService;
    }

    private ItemRenderer itemRenderer;
    public void setItemRenderer(ItemRenderer itemRenderer) {
        this.itemRenderer = itemRenderer;
    }

    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator messageLocator) {
        this.messageLocator = messageLocator;
    }

    List<EvalTemplateItem> allItems = new ArrayList<EvalTemplateItem>();
    int displayedItems = 1; //  determines the number to display next to each item
    int colorCounter = 0; // used to determine whether to color the background of an item

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // NOTE: the logic in this page was based on where the link originated (which is a terrible way to do things) and has
        // been changed to instead be based on the incoming view parameters, as a result, this is mostly a rewrite
        // so the original authors have been removed -AZ

        String currentUserId = external.getCurrentUserId();

        EvalViewParameters previewEvalViewParams = (EvalViewParameters)viewparams;
        if (previewEvalViewParams.evaluationId == null && 
                previewEvalViewParams.templateId == null) {
            throw new IllegalArgumentException("Must specify template id or evaluation id, both cannot be null");
        }

        Long evaluationId = previewEvalViewParams.evaluationId;
        Long templateId = previewEvalViewParams.templateId;
        String evalGroupId = previewEvalViewParams.evalGroupId;
        EvalEvaluation eval = null;
        EvalTemplate template = null;

        if (evaluationId == null) {
            // previewing a template
            UIMessage.make(tofill, "preview-title", "previeweval.template.title");
            // load up the template
            template = authoringService.getTemplateById(templateId);
            // create a fake evaluation
            eval = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, currentUserId, messageLocator.getMessage("previeweval.evaluation.title.default"), 
                    new Date(), new Date(), new Date(), new Date(), EvalConstants.EVALUATION_STATE_INQUEUE, EvalConstants.SHARING_VISIBLE,
                    new Integer(1), template);
            eval.setInstructions(messageLocator.getMessage("previeweval.instructions.default"));
        } else {
            // previewing an evaluation
            UIMessage.make(tofill, "preview-title", "previeweval.evaluation.title");
            UIMessage.make(tofill, "preview-title-prefix", "previeweval.evaluation.title.prefix");
            // load the real evaluation and template
            eval = evaluationService.getEvaluationById(evaluationId);
            template = eval.getTemplate();
        }

        UIMessage.make(tofill, "eval-title-header", "takeeval.eval.title.header");
        UIOutput.make(tofill, "evalTitle", eval.getTitle());

        UIBranchContainer groupTitle = UIBranchContainer.make(tofill, "show-group-title:");
        UIMessage.make(groupTitle, "group-title-header", "takeeval.group.title.header");
        if (evalGroupId == null) {
            UIMessage.make(groupTitle, "group-title", "previeweval.course.title.default");
        } else {
            UIOutput.make(groupTitle, "group-title", external.getDisplayTitle(evalGroupId) );
        }

        // show instructions if not null
        if (eval.getInstructions() != null) {
            UIBranchContainer instructions = UIBranchContainer.make(tofill, "show-eval-instructions:");
            UIMessage.make(instructions, "eval-instructions-header", "takeeval.instructions.header");	
            UIVerbatim.make(instructions, "eval-instructions", eval.getInstructions());
        }

        // TODO - rendering logic is identical to take evals page, should be put into a common util class -AZ

        // get items(parent items, child items --need to set order
        allItems = new ArrayList<EvalTemplateItem>(template.getTemplateItems()); // LAZY LOAD
        if (! allItems.isEmpty()) {
            // filter out the block child items, to get a list of ordered non-child items
            List<EvalTemplateItem> ncItemsList = TemplateItemUtils.getNonChildItems(allItems);

            // check if there are any "Course" items or "Instructor" items;
            UIBranchContainer courseSection = null;
            UIBranchContainer instructorSection = null;

            if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_COURSE, ncItemsList)) {	
                colorCounter=0;
                courseSection = UIBranchContainer.make(tofill,"courseSection:");
                UIMessage.make(courseSection, "course-questions-header", "previeweval.course.questions.header"); 
                for (int i = 0; i < ncItemsList.size(); i++) {	
                    EvalTemplateItem templateItem = (EvalTemplateItem) ncItemsList.get(i);

                    String cat = templateItem.getCategory();
                    if (cat == null) {
                        throw new IllegalStateException("Template item with null category found: " + templateItem.getId() );
                    } else if (EvalConstants.ITEM_CATEGORY_COURSE.equals(cat)) {
                        doFillComponent(courseSection, templateItem, colorCounter);
                        ncItemsList.remove(i);
                        i--;
                        colorCounter++;
                    } 
                }
            }

            if (TemplateItemUtils.checkTemplateItemsCategoryExists(EvalConstants.ITEM_CATEGORY_INSTRUCTOR, ncItemsList)) {	
                colorCounter=0;
                instructorSection = UIBranchContainer.make(tofill,"instructorSection:");
                UIMessage.make(instructorSection, "instructor-questions-header", "previeweval.instructor.questions.header"); 
                for (int i = 0; i < ncItemsList.size(); i++) {	
                    EvalTemplateItem templateItem = (EvalTemplateItem) ncItemsList.get(i);

                    String cat = templateItem.getCategory();
                    if (cat == null) {
                        throw new IllegalStateException("Template item with null category found: " + templateItem.getId() );
                    } else if (EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(cat)) {
                        doFillComponent(instructorSection, templateItem, colorCounter);
                        ncItemsList.remove(i);
                        i--;
                        colorCounter++;
                    } 
                }	
            }

            // strangely we are removing items from the list as we go and then dying if things are left out -AZ
            if (ncItemsList.size() > 0) {
                throw new IllegalStateException("Items found with categories that are not rendered");
            }

        }

    }


    /**
     * @param section
     * @param templateItem
     * @param colorCounter
     */
    private void doFillComponent(UIBranchContainer section, EvalTemplateItem templateItem, int colorCounter) {
        UIBranchContainer itemsBranch = null;
        itemsBranch = UIBranchContainer.make(section, "itemrow:first", Integer.toString(displayedItems));

        // use the renderer evolver
        itemRenderer.renderItem(itemsBranch, "previewed-item:", null, templateItem, displayedItems, true);
        if(TemplateItemUtils.getTemplateItemType(templateItem).equals(EvalConstants.ITEM_TYPE_BLOCK_PARENT)){
            List<EvalTemplateItem> childList = TemplateItemUtils.getChildItems(allItems, templateItem.getId());
            displayedItems += childList.size();
        } else if (TemplateItemUtils.getTemplateItemType(templateItem).equals(EvalConstants.ITEM_TYPE_HEADER)) { 
            // no change, do not count header
        } else {
            displayedItems++;
        }
        //increment by 1 if not block, else increment by num of block children

        if (colorCounter % 2 == 1) {
            itemsBranch.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") );// must match the existing CSS class 
        }
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new EvalViewParameters();
    }

}
