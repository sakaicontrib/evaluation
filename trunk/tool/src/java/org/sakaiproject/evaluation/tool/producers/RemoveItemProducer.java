/******************************************************************************
 * RemoveItemProducer.java - created by aaronz on 21 May 2007
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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIDeletionBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handles rendering view for confirmation and removal of items and template items
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class RemoveItemProducer implements ViewComponentProducer, ViewParamsReporter, ActionResultInterceptor {

   public static final String VIEW_ID = "remove_item";
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

      UIMessage.make(tofill, "page-title", "removeitem.page.title");

      UIInternalLink.make(tofill, "summary-link", 
            UIMessage.make("summary.page.title"), 
            new SimpleViewParameters(SummaryProducer.VIEW_ID));
      UIInternalLink.make(tofill, "control-templates-link",
            UIMessage.make("controltemplates.page.title"), 
            new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
      UIInternalLink.make(tofill, "control-items-link",
            UIMessage.make("controlitems.page.title"), 
            new SimpleViewParameters(ControlItemsProducer.VIEW_ID));

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

      String itemOTPBinding = null;
      if (item == null) {
         // we are removing a template item
         itemOTPBinding = "templateItemWBL."+templateItem.getId();
         UIInternalLink.make(tofill, "items-templates-link", 
               UIMessage.make("modifytemplate.page.title"), 
               new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, templateItem.getTemplate().getId() ) );
         UIMessage.make(tofill, "remove-item-confirm-text", "removeitem.templateitem.confirmation",
               new Object[] {templateItem.getDisplayOrder(), templateItem.getTemplate().getTitle()});
         if (TemplateItemUtils.getTemplateItemType(templateItem).equals(EvalConstants.ITEM_TYPE_BLOCK_PARENT)) {
            UIMessage.make(tofill, "remove-item-block-text", "removeitem.block.text");
         }
      } else {
         // we are removing an item
         itemOTPBinding = "itemWBL."+item.getId();
         UIInternalLink.make(tofill, "items-templates-link", 
               UIMessage.make("controlitems.page.title"), 
               new SimpleViewParameters(ControlItemsProducer.VIEW_ID) );
         UIMessage.make(tofill, "remove-item-confirm-text", "removeitem.item.confirmation");
      }

      // use the renderer evolver to show the item
      itemRenderer.renderItem(tofill, "item-to-remove:", null, templateItem, 0, true);

      UIMessage.make(tofill, "cancel-command-link", "general.cancel.button");

      // remove the items using a OTP deletion binding
      UIForm form = UIForm.make(tofill, "remove-item-form");
      UICommand removeButton = UICommand.make(form, "remove-item-command-link", 
            UIMessage.make("removeitem.remove.item.button"));
      removeButton.parameters.add(new UIDeletionBinding(itemOTPBinding));
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
