/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
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
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.content.ContentTypeInfoRegistry;
import uk.org.ponder.rsf.content.ContentTypeReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handle previewing a single item<br/>
 * Rewritten to use the item renderers by AZ
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu) 
 */
public class PreviewItemProducer extends EvalCommonProducer implements ViewParamsReporter, ContentTypeReporter {

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
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {	

        // get templateItem to preview from VPs
        ItemViewParameters previewItemViewParams = (ItemViewParameters) viewparams;
        EvalTemplateItem templateItem = null;
        if (previewItemViewParams.templateItemId != null) {
            templateItem = authoringService.getTemplateItemById(previewItemViewParams.templateItemId);

        } else if (previewItemViewParams.itemId != null) {
            EvalItem item = authoringService.getItemById(previewItemViewParams.itemId);
            templateItem = TemplateItemUtils.makeTemplateItem(item);
            if (templateItem.getId() == null){
                templateItem.setId(item.getId());
            }

        } else {
            throw new IllegalArgumentException("Must have itemId or templateItemId to do preview");
        }

        // override values if they are set (this is for live previews)
        if (previewItemViewParams.scaleDisplay != null) {
            templateItem.setScaleDisplaySetting(previewItemViewParams.scaleDisplay);
        }
        if (previewItemViewParams.na != null) {
            templateItem.setUsesNA(previewItemViewParams.na);
        }
        if (previewItemViewParams.compulsory != null) {
            templateItem.setCompulsory(previewItemViewParams.compulsory);
        }
        if (previewItemViewParams.showComment != null) {
            templateItem.setUsesComment(previewItemViewParams.showComment);
        }
        if (previewItemViewParams.text != null) {
            templateItem.getItem().setItemText(previewItemViewParams.text);
        }
        if (previewItemViewParams.textLines != null) {
            templateItem.setDisplayRows(previewItemViewParams.textLines);
        }

        // use the renderer evolver to show the item
        List<EvalTemplateItem> templateItems = new ArrayList<>();
        templateItems.add(templateItem);
        if (templateItem.childTemplateItems != null) {
            templateItems.addAll(templateItem.childTemplateItems);
        }
        TemplateItemDataList tidl = new TemplateItemDataList(templateItems, null, null, null);
        DataTemplateItem dti = tidl.getDataTemplateItem(templateItem.getId());

        itemRenderer.renderItem(tofill, "previewed-item:", null, templateItem, templateItem.getDisplayOrder(), true, 
                RenderingUtils.makeRenderProps(dti, null, null, null) );

        // render the close button
        //UIMessage.make(tofill, "close-button", "general.close.window.button");
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new ItemViewParameters();
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.content.ContentTypeReporter#getContentType()
     */
    public String getContentType() {
        return ContentTypeInfoRegistry.HTML_FRAGMENT;
    }

}