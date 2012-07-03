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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.viewparams.EvalScaleParameters;
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
 * Handle previewing scales (with various display options)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class PreviewScaleProducer extends EvalCommonProducer implements ViewParamsReporter, ContentTypeReporter {

    public static final String VIEW_ID = "preview_scale";
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
        EvalScaleParameters scaleViewParams = (EvalScaleParameters) viewparams;
        EvalTemplateItem templateItem = null;
        if (scaleViewParams.scaleId != null || scaleViewParams.scale != null) {
            EvalScale scale;
            if (scaleViewParams.scaleId != null) {
                scale = authoringService.getScaleById(scaleViewParams.scaleId);
            } else {
                scale = scaleViewParams.scale;
            }
            // make a fake item and template item
            EvalItem item = new EvalItem("admin", "Sample question text", EvalConstants.SHARING_PUBLIC, EvalConstants.ITEM_TYPE_SCALED, false);
            item.setId(123456l); // need a fake id
            item.setScaleDisplaySetting(EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED);
            if (scaleViewParams.scaleDisplaySetting != null) {
                item.setScaleDisplaySetting(scaleViewParams.scaleDisplaySetting);
            }
            item.setScale(scale);
            templateItem = TemplateItemUtils.makeTemplateItem(item);
            templateItem.setId(1234567l); // need a fake id

        } else {
            throw new IllegalArgumentException("Must have scale or scaleId to do preview");
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
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new EvalScaleParameters();
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.content.ContentTypeReporter#getContentType()
     */
    public String getContentType() {
        return ContentTypeInfoRegistry.HTML_FRAGMENT;
    }

}