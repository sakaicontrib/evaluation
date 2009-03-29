/******************************************************************************
 * PreviewItemProducer.java - created on Aug 21, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 * Rui Feng (fengr@vt.edu)
 * Aaron Zeckoski (aaronz@vt.edu) - project lead
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handle previewing a single item<br/>
 * Rewritten to use the item renderers by AZ
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu) 
 */
public class PreviewItemProducer implements ViewComponentProducer, ViewParamsReporter {

    public static final String VIEW_ID = "preview_item";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private ItemRenderer itemRenderer;
    public void setItemRenderer(ItemRenderer itemRenderer) {
        this.itemRenderer = itemRenderer;
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {	

        // get templateItem to preview from VPs
        ItemViewParameters previewItemViewParams = (ItemViewParameters) viewparams;
        EvalTemplateItem templateItem = null;
        if (previewItemViewParams.templateItemId != null) {
            templateItem = authoringService.getTemplateItemById(previewItemViewParams.templateItemId);
        } else if (previewItemViewParams.itemId != null) {
            EvalItem item = authoringService.getItemById(previewItemViewParams.itemId);
            templateItem = TemplateItemUtils.makeTemplateItem(item);
        } else {
            throw new IllegalArgumentException("Must have itemId or templateItemId to do preview");
        }

        // use the renderer evolver to show the item
        List<EvalTemplateItem> templateItems = new ArrayList<EvalTemplateItem>();
        templateItems.add(templateItem);
        if (templateItem.childTemplateItems != null) {
            templateItems.addAll(templateItem.childTemplateItems);
        }
        TemplateItemDataList tidl = new TemplateItemDataList(templateItems, null, null, null);
        DataTemplateItem dti = tidl.getDataTemplateItem(templateItem.getId());
        itemRenderer.renderItem(tofill, "previewed-item:", null, templateItem, templateItem.getDisplayOrder(), true, 
                RenderingUtils.makeRenderProps(dti, null, null, null) );

        // render the close button
        UIMessage.make(tofill, "close-button", "general.close.window.button");
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new ItemViewParameters();
    }

}