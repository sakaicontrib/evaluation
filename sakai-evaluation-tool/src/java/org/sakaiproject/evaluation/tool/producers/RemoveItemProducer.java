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

import java.util.List;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIDeletionBinding;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handles rendering view for confirmation and removal of items and template items
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class RemoveItemProducer extends EvalCommonProducer implements ViewParamsReporter, ActionResultInterceptor {

    public static final String VIEW_ID = "remove_item";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
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

        UIMessage.make(tofill, "page-title", "removeitem.page.title");

        // get templateItem to preview from VPs
        ItemViewParameters itemViewParams = (ItemViewParameters) viewparams;
        EvalTemplateItem templateItem = null;
        EvalItem item = null;
        if (itemViewParams.templateItemId != null) {
            templateItem = authoringService.getTemplateItemById(itemViewParams.templateItemId);
        } else if (itemViewParams.itemId != null) {
            item = authoringService.getItemById(itemViewParams.itemId);
            templateItem = TemplateItemUtils.makeTemplateItem(item);
        } else {
            throw new IllegalArgumentException("Must have itemId or templateItemId to do removal");
        }

        if (templateItem == null) {
            throw new IllegalStateException("Unable to retrieve a templateItem using item=" + 
                    itemViewParams.itemId + " and templateItem=" + itemViewParams.templateItemId);
        }

        String beanBinding = "templateBBean.";
        String actionBinding = "deleteItemAction";

        int displayNum = 1;
        String itemOTPBinding;
        if (item == null) {
            // we are removing a template item
            itemOTPBinding = "templateItemWBL."+templateItem.getId();
            UIInternalLink.make(tofill, "items-templates-link", 
                    UIMessage.make("modifytemplate.page.title"), 
                    new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, templateItem.getTemplate().getId() ) );
            UIInternalLink.make(tofill, "cancel-command-link", UIMessage.make("general.cancel.button"), new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, templateItem.getTemplate().getId()));
            UIMessage.make(tofill, "remove-item-confirm-text", "removeitem.templateitem.confirmation",
                    new Object[] {templateItem.getDisplayOrder(), templateItem.getTemplate().getTitle()});
            if (TemplateItemUtils.isBlockParent(templateItem)) {
                UIMessage.make(tofill, "remove-item-block-text", "removeitem.block.text");
            }
            if (templateItem.getDisplayOrder() != null) {
                displayNum = templateItem.getDisplayOrder();
            }
        } else {
            // we are removing an item
            itemOTPBinding = "itemWBL."+item.getId();
            UIInternalLink.make(tofill, "items-templates-link", 
                    UIMessage.make("controlitems.page.title"), 
                    new SimpleViewParameters(ControlItemsProducer.VIEW_ID) );
            UIInternalLink.make(tofill, "cancel-command-link", UIMessage.make("general.cancel.button"), new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
            UIMessage.make(tofill, "remove-item-confirm-text", "removeitem.item.confirmation");

            // in use message
            List<EvalTemplate> templates = authoringService.getTemplatesUsingItem(item.getId());
            if (templates.size() > 0) {
                actionBinding = "hideItemAction";
                UIBranchContainer inUseBranch = UIBranchContainer.make(tofill, "inUse:");
                UIMessage.make(inUseBranch, "inUseWarning", "removeitem.inuse.warning", new Object[] {templates.size()});
                for (EvalTemplate template : templates) {
                    UIBranchContainer itemsBranch = UIBranchContainer.make(inUseBranch, "items:");
                    UIMessage.make(itemsBranch, "itemInfo", "removeitem.inuse.info", new Object[] {template.getId(), 
                            commonLogic.getEvalUserById(template.getOwner()).displayName, template.getTitle()});
                }
            }
        }

        // use the renderer evolver to show the item
        itemRenderer.renderItem(tofill, "item-to-remove:", null, templateItem, displayNum, true, null);

        UIForm form = UIForm.make(tofill, "remove-item-form");

        if (item == null) {
            // removing TI so just use the OTP deletion binding
            UICommand removeButton = UICommand.make(form, "remove-item-command-link", 
                    UIMessage.make("removeitem.remove.item.button"));
            removeButton.parameters.add(new UIDeletionBinding(itemOTPBinding));
        } else {
            // removing or hiding an item
            UICommand deleteCommand = UICommand.make(form, "remove-item-command-link", 
                    UIMessage.make("removeitem.remove.item.button"), beanBinding + actionBinding);
            deleteCommand.parameters.add(new UIELBinding(beanBinding + "itemId", item.getId()));
        }

        UICommand.make( form, "cancel-button", UIMessage.make( "general.cancel.button" ) );
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new ItemViewParameters();
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.ActionResultInterceptor#interceptActionResult(uk.org.ponder.rsf.flow.ARIResult, uk.org.ponder.rsf.viewstate.ViewParameters, java.lang.Object)
     */
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        ItemViewParameters itemViewParams = (ItemViewParameters) incoming;
        if (itemViewParams.templateItemId != null) {
            result.resultingView = new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, itemViewParams.templateId);
        } else if (itemViewParams.itemId != null) {
            result.resultingView = new SimpleViewParameters(ControlItemsProducer.VIEW_ID);			
        }
    }

}
