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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.viewparams.EvalScaleParameters;
import org.sakaiproject.evaluation.utils.TemplateItemDataList;
import org.sakaiproject.evaluation.utils.TemplateItemDataList.DataTemplateItem;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIOutput;
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

    public MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator messageLocator) {
        this.messageLocator = messageLocator;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {	

        // make the map of display type keys and name
        LinkedHashMap<String, String> scaleDisplayMap = new LinkedHashMap<>();
        for (int i = 0; i < EvalToolConstants.SCALE_DISPLAY_SETTING_VALUES.length; i++) {
            scaleDisplayMap.put(EvalToolConstants.SCALE_DISPLAY_SETTING_VALUES[i], EvalToolConstants.SCALE_DISPLAY_SETTING_LABELS_PROPS[i]);
        }

        // get templateItem to preview from VPs
        EvalScaleParameters scaleViewParams = (EvalScaleParameters) viewparams;
        EvalTemplateItem templateItem = null;
        if (scaleViewParams.id != null || scaleViewParams.points != null) {
            EvalScale scale;
            if (scaleViewParams.id != null) {
                scale = authoringService.getScaleById(scaleViewParams.id);
            } else {
                scale = new EvalScale("admin", "Sample scale", EvalConstants.SCALE_MODE_SCALE, EvalConstants.SHARING_PUBLIC, false);
                scale.setId(12345l); // need a fake id
                scale.setIdeal(scaleViewParams.findIdeal());
                scale.setOptions(scaleViewParams.findPoints());
            }
            // make a fake item and template item
            EvalItem item = new EvalItem("admin", "Sample question text", EvalConstants.SHARING_PUBLIC, EvalConstants.ITEM_TYPE_SCALED, false);
            item.setId(123456l); // need a fake id
            item.setScale(scale);
            item.setScaleDisplaySetting(scaleViewParams.findDisplaySetting());
            templateItem = TemplateItemUtils.makeTemplateItem(item);
            templateItem.setId(1234567l); // need a fake id

        } else {
            throw new IllegalArgumentException("Must have scale points or scaleId to do preview");
        }

        if (scaleViewParams.displaySetting != null) {
            // trash all the mapped display settings except one if we sent in a sample one
            for (Iterator<Entry<String, String>> iterator = scaleDisplayMap.entrySet().iterator(); iterator.hasNext();) {
                Entry<String, String> entry = iterator.next();
                if (!entry.getKey().equals(scaleViewParams.displaySetting)) {
                    iterator.remove();
                }
            }
            if (scaleDisplayMap.isEmpty()) {
                // this should not really happen
                scaleDisplayMap.put(EvalToolConstants.SCALE_DISPLAY_SETTING_VALUES[0], EvalToolConstants.SCALE_DISPLAY_SETTING_LABELS_PROPS[0]);
            }
        }

        for (Entry<String, String> entry : scaleDisplayMap.entrySet()) {
            String displaySetting = entry.getKey();
            String displaySettingName = messageLocator.getMessage(entry.getValue());
            templateItem.setScaleDisplaySetting(displaySetting);
            // append the scale display setting to the item text
            templateItem.getItem().setItemText( "Sample question text ("+displaySettingName+")");
            // make a fake TIDL and DTI for rendering consistency
            List<EvalTemplateItem> templateItems = new ArrayList<>();
            templateItems.add(templateItem);
            TemplateItemDataList tidl = new TemplateItemDataList(templateItems, null, null, null);
            DataTemplateItem dti = tidl.getDataTemplateItem(templateItem.getId());

            // render the header for each item
            UIBranchContainer header = UIBranchContainer.make(tofill, "previewed:header");
            UIOutput.make(header, "previewed-title", displaySettingName);

            // use the renderer evolver to show the item
            itemRenderer.renderItem(tofill, "previewed:item", null, templateItem, templateItem.getDisplayOrder(), true, 
                    RenderingUtils.makeRenderProps(dti, null, null, null) );

        }
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