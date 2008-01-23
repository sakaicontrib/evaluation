/******************************************************************************
 * ModifyItemProducer.java - created by aaronz on 20 May 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
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

import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalScalesLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.locators.ItemBeanWBL;
import org.sakaiproject.evaluation.tool.locators.TemplateItemWBL;
import org.sakaiproject.evaluation.tool.renderers.HierarchyNodeSelectorRenderer;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;
import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInputMany;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.evolvers.BoundedDynamicListInputEvolver;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * View for handling the creation and modification of items and template items,
 * this is replacing all the separate views which used to exist and resulted in a lot of
 * code duplication
 * (the create block view remains separate)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ModifyItemProducer implements ViewComponentProducer, ViewParamsReporter, ActionResultInterceptor {

	public static final String VIEW_ID = "modify_item";
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic(EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	private EvalScalesLogic scalesLogic;
	public void setScalesLogic(EvalScalesLogic scalesLogic) {
		this.scalesLogic = scalesLogic;
	}

	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
	}

	private TextInputEvolver richTextEvolver;
	public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
		this.richTextEvolver = richTextEvolver;
	}

   private BoundedDynamicListInputEvolver boundedDynamicListInputEvolver;
   public void setBoundedDynamicListInputEvolver(BoundedDynamicListInputEvolver boundedDynamicListInputEvolver) {
      this.boundedDynamicListInputEvolver = boundedDynamicListInputEvolver;
   }

   private HierarchyNodeSelectorRenderer hierarchyNodeSelectorRenderer;
   public void setHierarchyNodeSelectorRenderer(
         HierarchyNodeSelectorRenderer hierarchyNodeSelectorRenderer) {
      this.hierarchyNodeSelectorRenderer = hierarchyNodeSelectorRenderer;
   }


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      // local variables used in the render logic
      String currentUserId = external.getCurrentUserId();
      boolean userAdmin = external.isUserAdmin(currentUserId);

      // create the form to allow submission of this item
      UIForm form = UIForm.make(tofill, "item-form");

		// variables and basic logic for determining what we are doing on this view
		ItemViewParameters ivp = (ItemViewParameters) viewparams;
		Long templateId = ivp.templateId; // if null then assume we are creating items for the item bank, else this is for a template
		Long itemId = ivp.itemId; // if null then we are creating a new item, else modifying existing item
		Long templateItemId = ivp.templateItemId; // if null then we are working with an item only, else we are working with a template item so we will get the item from this
      String itemClassification = ivp.itemClassification; // must be set if creating a new item

      String templateOTP = "templateBeanLocator."; // bind to the template via OTP
		String itemOTP = null; // bind to the item via OTP
      String templateItemOTP = null; // bind to the template item via OTP
      String commonDisplayOTP = null; // this will bind to either the item or the template item depending on which should save the common display information
		String itemOwnerName = null; // this is the name of the owner of the item

		String scaleDisplaySetting = null; // the scale display setting for the item/TI
		String displayRows = null; // the number of rows to display for the text area
		Boolean usesNA = null; // whether or not the item uses the N/A option
		Long scaleId = null; // this holds the current scale id if there is one
		
		// now we validate the incoming view params
		if (templateId == null && templateItemId != null) {
		   throw new IllegalArgumentException("templateId cannot be null when modifying template items, must pass in a valid template id");
		}

		if (templateItemId == null && itemId == null) {
		   // creating new item or template item
	      if ( itemClassification == null || itemClassification.equals("") ) {
	         throw new NullPointerException("itemClassification cannot be null or empty string for new items, must pass in a valid item type");
	      }
         itemOTP = "itemWBL." + ItemBeanWBL.NEW_1 + ".";
         commonDisplayOTP = itemOTP;
         itemOwnerName = external.getUserDisplayName(currentUserId);
         // check if we are operating in a template
         if (templateId != null) {
            // new template item in the current template
            templateItemOTP = "templateItemWBL." + TemplateItemWBL.NEW_1 + ".";
            itemOTP = templateItemOTP + "item.";
            commonDisplayOTP = templateItemOTP;
            // bind the template item to the current template
            form.parameters.add(
                  new UIELBinding(templateItemOTP + "template", ELReference.make(templateOTP + templateId)) );
         }
         // add binding for the item classification
         form.parameters.add(
               new UIELBinding(itemOTP + "classification", itemClassification) );
		} else if (templateItemId == null) {
		   // itemId is not null so we are modifying an existing item
         EvalItem item = itemsLogic.getItemById(itemId);
         if (item == null) {
            throw new IllegalArgumentException("Invalid item id passed in by VP: " + itemId);
         }
         // set the common settings from the item
         scaleDisplaySetting = item.getScaleDisplaySetting();
         displayRows = item.getDisplayRows() != null ? item.getDisplayRows().toString() : null;
         usesNA = item.getUsesNA();
         scaleId = item.getScale() != null ? item.getScale().getId() : null;

         itemOwnerName = external.getUserDisplayName(item.getOwner());
         itemClassification = item.getClassification();
         itemOTP = "itemWBL." + itemId + ".";
         commonDisplayOTP = itemOTP;
		} else {
		   // templateItemId is not null so we are modifying an existing template item
         EvalTemplateItem templateItem = itemsLogic.getTemplateItemById(templateItemId);
         if (templateItem == null) {
            throw new IllegalArgumentException("Invalid template item id passed in by VP: " + templateItemId);
         }
         // set the common settings from the TI
         scaleDisplaySetting = templateItem.getScaleDisplaySetting();
         displayRows = templateItem.getDisplayRows() != null ? templateItem.getDisplayRows().toString() : null;
         usesNA = templateItem.getUsesNA();
         scaleId = templateItem.getItem().getScale() != null ? templateItem.getItem().getScale().getId() : null;

         itemOwnerName = external.getUserDisplayName(templateItem.getItem().getOwner());
         itemClassification = templateItem.getItem().getClassification();
         templateItemOTP = "templateItemWBL." + templateItemId + ".";
         itemOTP = templateItemOTP + "item.";
         commonDisplayOTP = templateItemOTP;
		}

		// now we begin with the rendering logic
      UIMessage.make(tofill, "page-title", "modifyitem.page.title");

      // display the breadcrumb bar
      if (templateId == null) {
         // creating item only
         UIInternalLink.make(tofill, "summary-toplink", 
               UIMessage.make("summary.page.title"), 
            new SimpleViewParameters(SummaryProducer.VIEW_ID));
         UIInternalLink.make(tofill, "control-items-link",
               UIMessage.make("controlitems.page.title"), 
            new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
      } else {
         // creating template item
         UIInternalLink.make(tofill, "summary-toplink", 
               UIMessage.make("summary.page.title"), 
            new SimpleViewParameters(SummaryProducer.VIEW_ID));
         UIInternalLink.make(tofill, "control-items-link",
               UIMessage.make("controltemplates.page.title"), 
            new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
         UIInternalLink.make(tofill, "modify-template-items",
               UIMessage.make("modifytemplate.page.title"), 
            new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, templateId));
      }
		UIMessage.make(tofill, "item-header", "modifyitem.item.header");

		// display item information
		UIOutput.make(tofill, "item-classification", itemClassification, itemOTP + "classification");
		UIMessage.make(tofill, "added-by-item-owner", "modifyitem.item.added.by.owner", new Object[] {itemOwnerName});

		// show links if this item/templateItem exists
		if (templateItemId != null || itemId != null) {
			UIInternalLink.make(tofill, "item-preview-link", UIMessage.make("controlitems.preview.link"), 
					new ItemViewParameters(PreviewItemProducer.VIEW_ID, itemId, templateItemId) );
			if ( (itemId != null && itemsLogic.canRemoveItem(currentUserId, itemId)) || 
			      templateItemId != null && itemsLogic.canControlTemplateItem(currentUserId, templateItemId) ) {
	        	// item or templateItem is removable
				UIInternalLink.make(tofill, "item-remove-link", UIMessage.make("controlitems.remove.link"), 
						new ItemViewParameters(RemoveItemProducer.VIEW_ID, itemId, templateItemId));
			}
		}

		UIMessage.make(form, "item-text-header", "modifyitem.item.text.header");
		UIMessage.make(form, "item-text-instruction", "modifyitem.item.text.instruction");

		UIInput itemText = UIInput.make(form, "item-text:", itemOTP + "itemText");
		richTextEvolver.evolveTextInput( itemText );

		if (EvalConstants.ITEM_TYPE_SCALED.equals(itemClassification)) {
		   // SCALED items need to choose a scale
			UIBranchContainer showItemScale = UIBranchContainer.make(form, "show-item-scale:");
			UIMessage.make(showItemScale, "item-scale-header", "modifyitem.item.scale.header");
			List<EvalScale> scales = scalesLogic.getScalesForUser(currentUserId, null);
			if (scales.isEmpty()) {
			   throw new IllegalStateException("There are no scales available in the system for creating scaled items, please create at least one scale");
			}
			String[] scaleValues = ScaledUtils.getScaleValues(scales);
			UISelect scaleList = UISelect.make(showItemScale, "item-scale-list", 
			      scaleValues, 
					ScaledUtils.getScaleLabels(scales), 
					itemOTP + "scale.id",
					scaleId != null ? scaleId.toString() : scaleValues[0]);
         scaleList.selection.mustapply = true; // this is required to ensure that the value gets passed even if it is not changed
			scaleList.selection.darreshaper = new ELReference("#{id-defunnel}");

			renderScaleDisplaySelect(form, commonDisplayOTP, scaleDisplaySetting, 
			      EvaluationConstant.SCALE_DISPLAY_SETTING_VALUES, 
			      EvaluationConstant.SCALE_DISPLAY_SETTING_LABELS_PROPS);
		} else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemClassification) ||
		      EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemClassification) ) {
		   // MC/MA items need to create choices
		   String scaleOTP = itemOTP + "scale."; // + (scaleId != null ? scaleId.toString() : ScaleBeanLocator.NEW_1) + ".";
         UIBranchContainer showItemChoices = UIBranchContainer.make(form, "show-item-choices:");
         boundedDynamicListInputEvolver.setLabels(
               UIMessage.make("scaleaddmodify.remove.scale.option.button"), 
               UIMessage.make("scaleaddmodify.add.scale.option.button"));
         boundedDynamicListInputEvolver.setMinimumLength(2);
         boundedDynamicListInputEvolver.setMaximumLength(20);

         UIInputMany modifypoints = UIInputMany.make(showItemChoices, 
               "modify-scale-points:", scaleOTP + "options",
               (scaleId == null ? EvaluationConstant.defaultInitialScaleValues : null) );
         boundedDynamicListInputEvolver.evolve(modifypoints);

         renderScaleDisplaySelect(form, commonDisplayOTP, scaleDisplaySetting, 
               EvaluationConstant.CHOICES_DISPLAY_SETTING_VALUES, 
               EvaluationConstant.CHOICES_DISPLAY_SETTING_LABELS_PROPS);
		}

		if (userAdmin && templateId == null) {
			// only show the item sharing options if the user is an admin AND we are modifying the item only
			UIBranchContainer showItemSharing = UIBranchContainer.make(form, "show-item-sharing:");
			UIMessage.make(showItemSharing, "item-sharing-header", "modifyitem.item.sharing.header");
			UISelect.make(showItemSharing, "item-sharing-list", 
					EvaluationConstant.SHARING_VALUES, 
					EvaluationConstant.SHARING_LABELS_PROPS, 
					itemOTP + "sharing").setMessageKeys();
		} else {
			// not admin so set the sharing to private by default for now
			form.parameters.add( new UIELBinding(itemOTP + "sharing", EvalConstants.SHARING_PRIVATE) );
		}

		if (userAdmin && templateId == null) {
			// only show the expert items if the user is an admin AND we are modifying the item only
			UIBranchContainer showItemExpert = UIBranchContainer.make(form, "show-item-expert:");
			UIMessage.make(showItemExpert, "item-expert-header", "modifyitem.item.expert.header");
			UIMessage.make(showItemExpert, "item-expert-instruction", "modifyitem.item.expert.instruction");
			UIBoundBoolean.make(showItemExpert, "item-expert", itemOTP + "expert", null);

			UIMessage.make(showItemExpert, "expert-desc-header", "modifyitem.item.expert.desc.header");
			UIMessage.make(showItemExpert, "expert-desc-instruction", "modifyitem.item.expert.desc.instruction");
			UIInput expertDesc = UIInput.make(showItemExpert, "expert-desc:", itemOTP + "expertDescription");
			richTextEvolver.evolveTextInput( expertDesc );			
		}

      if (templateId == null) {
         // call these hints if we are modifying item only
   		UIMessage.make(tofill, "item-display-hint-header", "modifyitem.display.hint.header");
   		UIMessage.make(tofill, "item-display-hint-instruction", "modifyitem.display.hint.instruction");
      } else {
         // these are required if we are modifying template items
         UIMessage.make(tofill, "item-display-hint-header", "modifyitem.display.header");
         UIMessage.make(tofill, "item-display-hint-instruction", "modifyitem.display.instruction");         
      }

		if (EvalConstants.ITEM_TYPE_TEXT.equals(itemClassification)) {
			UIBranchContainer showResponseSize = UIBranchContainer.make(form, "show-response-size:");
			UIMessage.make(showResponseSize, "item-response-size-header", "modifyitem.item.response.size.header");
			UISelect.make(showResponseSize, "item-response-size-list", 
					EvaluationConstant.RESPONSE_SIZE_VALUES, 
					EvaluationConstant.RESPONSE_SIZE_LABELS_PROPS,
					commonDisplayOTP + "displayRows",
					displayRows ).setMessageKeys();
		}

		if (! EvalConstants.ITEM_TYPE_HEADER.equals(itemClassification)) {
         Boolean naAllowed = (Boolean) settings.get(EvalSettings.NOT_AVAILABLE_ALLOWED);
   		if (naAllowed != null && naAllowed) {
   			UIBranchContainer showNA = UIBranchContainer.make(form, "showNA:");
   			UIMessage.make(showNA,"item-na-header", "modifyitem.item.na.header");
   			UIBoundBoolean.make(showNA, "item-na", commonDisplayOTP + "usesNA", usesNA);
   		}
		}

		Boolean isDefaultCourse = (Boolean) settings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY);
		if (isDefaultCourse == null) {
			// Means show both options (course and instructor)
			UIBranchContainer showItemCategory = UIBranchContainer.make(form, "showItemCategory:");
			UIMessage.make(showItemCategory, "item-category-header", "modifyitem.item.category.header");
			UISelect radios = UISelect.make(showItemCategory, "item-category-list", 
					EvaluationConstant.ITEM_CATEGORY_VALUES, 
					EvaluationConstant.ITEM_CATEGORY_LABELS_PROPS,
					itemOTP + "category").setMessageKeys();
			for (int i = 0; i < EvaluationConstant.ITEM_CATEGORY_VALUES.length; i++) {
				UIBranchContainer radioBranch = UIBranchContainer.make(showItemCategory, "item-category-branch:", i+"");
				UISelectLabel.make(radioBranch, "item-category-label", radios.getFullID(), i);
				UISelectChoice.make(radioBranch, "item-category-radio", radios.getFullID(), i);
			}
		} else {
         // Course category if default, instructor otherwise
         // Do not show on the page, just bind it explicitly.
         form.parameters.add(
               new UIELBinding(itemOTP + "category",
                     EvaluationConstant.ITEM_CATEGORY_VALUES[isDefaultCourse.booleanValue() ? 0 : 1]));
		}

		if (templateId != null) {
		   // ONLY DO THESE if we are working with a template and TemplateItem
         /*
          * If the system setting (admin setting) for "EvalSettings.ITEM_USE_RESULTS_SHARING" is set as true then all
          * items default to "Public". If it is set to false, then all items can be selected as Public or Private.
          * If it is set to null then user is given the option to  choose between "Public" and "Private".
          */
         Boolean isDefaultResultSharing = (Boolean) settings.get(EvalSettings.ITEM_USE_RESULTS_SHARING);
         if (isDefaultResultSharing != null) {
            if (isDefaultResultSharing) {
               // Means show both options (public & private)
               UIBranchContainer showItemResultSharing = UIBranchContainer.make(form, "showItemResultSharing:");
               UIMessage.make(showItemResultSharing, "item-results-sharing-header", "modifyitem.results.sharing.header");
               UIMessage.make(showItemResultSharing, "item-results-sharing-PU", "item.results.sharing.public");
               UIMessage.make(showItemResultSharing, "item-results-sharing-PR", "item.results.sharing.private");
               // Radio Buttons for "Result Sharing"
               String[] resultSharingList = { "item.results.sharing.public", "item.results.sharing.private" };
               UISelect radios = UISelect.make(showItemResultSharing, "item_results_sharing", EvaluationConstant.ITEM_RESULTS_SHARING_VALUES,
                     resultSharingList, templateItemOTP + "resultsSharing", null);
   
               String selectID = radios.getFullID();
               UISelectChoice.make(showItemResultSharing, "item_results_sharing_PU", selectID, 0);
               UISelectChoice.make(showItemResultSharing, "item_results_sharing_PR", selectID, 1);
            } else {
               // false, so all questions are private by default           
               form.parameters.add(new UIELBinding(templateItemOTP + "resultsSharing",
                     EvaluationConstant.ITEM_RESULTS_SHARING_VALUES[isDefaultResultSharing ? 0 : 1]));
            }
         } else {
            // null so all questions are private by default       
            form.parameters.add(new UIELBinding(templateItemOTP + "resultsSharing",
                  EvaluationConstant.ITEM_RESULTS_SHARING_VALUES[0]));
         }

         // hierarchy node selector control
         Boolean showHierarchyOptions = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
         if (showHierarchyOptions != null && showHierarchyOptions) {
            hierarchyNodeSelectorRenderer.renderHierarchyNodeSelector(form, "hierarchyNodeSelector:", templateItemOTP + "hierarchyNodeId", null);
         }
      }
		
		UIMessage.make(form, "cancel-button", "general.cancel.button");
		if (templateId == null) {
		   // only saving an item
   		UICommand.make(form, "save-item-action", 
   				UIMessage.make("modifyitem.save.button"), "#{templateBBean.saveItemAction}");
		} else {
		   // saving template item and item
         UICommand.make(form, "save-item-action", 
               UIMessage.make("modifyitem.save.button"), "#{templateBBean.saveBothAction}");		   
		}
	}


	/**
    * @param form
    * @param commonDisplayOTP
    * @param scaleDisplaySetting
    * @param values
    * @param lables
    */
   private void renderScaleDisplaySelect(UIForm form, String commonDisplayOTP,
         String scaleDisplaySetting, String[] values, String[] lables) {
      UIBranchContainer showScaleDisplay = UIBranchContainer.make(form, "show-scale-display:");
      UIMessage.make(showScaleDisplay, "scale-display-header", "modifyitem.scale.display.header");
      UISelect.make(showScaleDisplay, "scale-display-list", 
      		values, lables, commonDisplayOTP + "scaleDisplaySetting",
      		scaleDisplaySetting).setMessageKeys();
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.flow.ActionResultInterceptor#interceptActionResult(uk.org.ponder.rsf.flow.ARIResult, uk.org.ponder.rsf.viewstate.ViewParameters, java.lang.Object)
    */
   public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
      ItemViewParameters ivp = (ItemViewParameters) incoming;
      if (ivp.templateId == null) {
         // go to the Items view if we are not working with a template currently
         result.resultingView = new SimpleViewParameters(ControlItemsProducer.VIEW_ID);
      } else {
         // go to the template items view if we are working with a template
         result.resultingView = new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, ivp.templateId);
      }
   }

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters() {
		return new ItemViewParameters();
	}

}