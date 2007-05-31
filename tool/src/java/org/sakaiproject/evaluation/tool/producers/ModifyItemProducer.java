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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalScalesLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.locators.ItemBeanWBL;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;
import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;

import uk.org.ponder.rsf.components.ParameterList;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * View for handling the creation and modification of items (not template items)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ModifyItemProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {

	public static final String VIEW_ID = "modify_item";
	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ViewComponentProducer#getViewID()
	 */
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


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		// local variables used in the render logic
		String currentUserId = external.getCurrentUserId();
		boolean userAdmin = external.isUserAdmin(currentUserId);

		UIMessage.make(tofill, "page-title", "modifyitem.page.title");

		UIInternalLink.make(tofill, "summary-toplink", 
				UIMessage.make("summary.page.title"), 
			new SimpleViewParameters(SummaryProducer.VIEW_ID));
		UIInternalLink.make(tofill, "control-items-link",
				UIMessage.make("controlitems.page.title"), 
			new SimpleViewParameters(ControlItemsProducer.VIEW_ID));

		ItemViewParameters itemViewParams = (ItemViewParameters) viewparams;
		Long itemId = null;
		String itemOTPBinding = null;
		String itemOwner = null;
		String itemClassification = null;
		if (itemViewParams.itemId == null) {
			// creating a new item
			if (itemViewParams.itemClassification == null) {
				throw new NullPointerException("itemClassification cannot be null for new items, must pass in a valid item type");
			}
			itemOTPBinding = "itemWBL." + ItemBeanWBL.NEW_1;
			itemOwner = external.getUserDisplayName(currentUserId);
			itemClassification = itemViewParams.itemClassification;
		} else {
			// modifying an existing item (check if item is not null)
			itemId = itemViewParams.itemId;
			EvalItem item = itemsLogic.getItemById(itemId);
			if (item == null) {
				throw new IllegalArgumentException("Invalid item id passed in by VP: " + itemId);
			}
			itemOwner = external.getUserDisplayName(item.getOwner());
			itemClassification = item.getClassification();
			itemOTPBinding = "itemWBL." + itemId;
		}

		if (itemClassification == null || itemClassification.equals("")) {
			throw new NullPointerException("itemClassification cannot be null or empty string for items");
		}

		UIMessage.make(tofill, "item-header", "modifyitem.item.header");
		UIOutput.make(tofill, "item-classification", itemViewParams.itemClassification, itemOTPBinding + ".classification");
		UIMessage.make(tofill, "added-by-item-owner", "modifyitem.item.added.by.owner", new Object[] {itemOwner});

		// show links if this item exists
		if (itemId != null) {
			UIInternalLink.make(tofill, "item-preview-link", UIMessage.make("controlitems.preview.link"), 
					new ItemViewParameters(PreviewItemProducer.VIEW_ID, itemId, (Long)null) );
			if ( itemsLogic.canRemoveItem(currentUserId, itemId) ) {
	        	// item removable
				UIInternalLink.make(tofill, "item-remove-link", UIMessage.make("controlitems.remove.link"), 
						new ItemViewParameters(RemoveItemProducer.VIEW_ID, itemId, (Long)null));
			}
		}

		// create the form to allow submission of this item
		UIForm form = UIForm.make(tofill, "item-form");
		// add binding for the item classification
		form.parameters = new ParameterList( 
				new UIELBinding(itemOTPBinding + ".classification", itemClassification) );

		UIMessage.make(form, "item-text-header", "modifyitem.item.text.header");
		UIMessage.make(form, "item-text-instruction", "modifyitem.item.text.instruction");

		UIInput itemText = UIInput.make(form, "item-text:", itemOTPBinding + ".itemText");
		richTextEvolver.evolveTextInput( itemText );

		if (EvalConstants.ITEM_TYPE_SCALED.equals(itemClassification)) {
			UIBranchContainer showItemScale = UIBranchContainer.make(form, "show-item-scale:");
			UIMessage.make(showItemScale, "item-scale-header", "modifyitem.item.scale.header");
			List scales = scalesLogic.getScalesForUser(currentUserId, null);
			UISelect.make(showItemScale, "item-scale-list", 
					ScaledUtils.getScaleValues(scales), 
					ScaledUtils.getScaleLabels(scales), 
					itemOTPBinding + ".scale.id");
			//scale.selection.darreshaper = new ELReference("#{id-defunnel}");

			UIBranchContainer showScaleDisplay = UIBranchContainer.make(form, "show-scale-display:");
			UIMessage.make(showScaleDisplay, "scale-display-header", "modifyitem.scale.display.header");
			UISelect.make(showScaleDisplay, "scale-display-list", 
					EvaluationConstant.SCALE_DISPLAY_SETTING_VALUES, 
					EvaluationConstant.SCALE_DISPLAY_SETTING_LABELS_PROPS, 
					itemOTPBinding + ".scaleDisplaySetting").setMessageKeys();
		}

		if (userAdmin) {
			// only show the item sharing options if the user is an admin
			UIBranchContainer showItemSharing = UIBranchContainer.make(tofill, "show-item-sharing:");
			UIMessage.make(showItemSharing, "item-sharing-header", "modifyitem.item.sharing.header");
			UISelect.make(showItemSharing, "item-sharing-list", 
					EvaluationConstant.SHARING_VALUES, 
					EvaluationConstant.SHARING_LABELS_PROPS, 
					itemOTPBinding + ".sharing").setMessageKeys();
		} else {
			// not admin so set the sharing to private by default for now
			form.parameters.add( new UIELBinding(itemOTPBinding + ".sharing", EvalConstants.SHARING_PRIVATE) );
		}

		if (userAdmin) {
			// only show the expert items if the user is an admin
			UIBranchContainer showItemExpert = UIBranchContainer.make(tofill, "show-item-expert:");
			UIMessage.make(showItemExpert, "item-expert-header", "modifyitem.item.expert.header");
			UIMessage.make(showItemExpert, "item-expert-instruction", "modifyitem.item.expert.instruction");
			UIBoundBoolean.make(showItemExpert, "item-expert", itemOTPBinding + ".expert", null);

			UIMessage.make(showItemExpert, "expert-desc-header", "modifyitem.item.expert.desc.header");
			UIMessage.make(showItemExpert, "expert-desc-instruction", "modifyitem.item.expert.desc.instruction");
			UIInput expertDesc = UIInput.make(showItemExpert, "expert-desc:", itemOTPBinding + ".expertDescription");
			richTextEvolver.evolveTextInput( expertDesc );			
		}

		UIMessage.make(form, "item-display-hint-header", "modifyitem.display.hint.header");
		UIMessage.make(form, "item-display-hint-instruction", "modifyitem.display.hint.instruction");

		if (EvalConstants.ITEM_TYPE_TEXT.equals(itemClassification)) {
			UIBranchContainer showResponseSize = UIBranchContainer.make(tofill, "show-response-size:");
			UIMessage.make(showResponseSize, "item-response-size-header", "modifyitem.item.response.size.header");
			UISelect.make(showResponseSize, "item-response-size-list", 
					EvaluationConstant.RESPONSE_SIZE_VALUES, 
					EvaluationConstant.RESPONSE_SIZE_LABELS_PROPS,
					itemOTPBinding + ".displayRows").setMessageKeys();
		}

		if (((Boolean)settings.get(EvalSettings.NOT_AVAILABLE_ALLOWED)).booleanValue() == true) {
			UIBranchContainer showNA = UIBranchContainer.make(form, "showNA:");
			UIMessage.make(showNA,"item-na-header", "modifyitem.item.na.header");
			UIBoundBoolean.make(showNA, "item-na", itemOTPBinding + ".usesNA", null);
		}

		Boolean isDefaultCourse = (Boolean) settings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY);
		if (isDefaultCourse == null) {
			// Means show both options (course and instructor)

			UIBranchContainer showItemCategory = UIBranchContainer.make(form, "showItemCategory:");
			UIMessage.make(showItemCategory, "item-category-header", "modifyitem.item.category.header");
			UISelect radios = UISelect.make(showItemCategory, "item-category-list", 
					EvaluationConstant.ITEM_CATEGORY_VALUES, 
					EvaluationConstant.ITEM_CATEGORY_LABELS_PROPS,
					itemOTPBinding + ".category").setMessageKeys();
			for (int i = 0; i < EvaluationConstant.ITEM_CATEGORY_VALUES.length; i++) {
				UIBranchContainer radioBranch = UIBranchContainer.make(showItemCategory, "item-category-branch:", i+"");
				UISelectLabel.make(radioBranch, "item-category-label", radios.getFullID(), i);
				UISelectChoice.make(radioBranch, "item-category-radio", radios.getFullID(), i);
			}
		} else {
			// Course category if unable to set
			form.parameters.add(
					new UIELBinding(itemOTPBinding + ".category", EvalConstants.ITEM_CATEGORY_COURSE));
		}

		UIMessage.make(form, "cancel-button", "general.cancel.button");
		UICommand.make(form, "save-item-action", 
				UIMessage.make("modifyitem.save.button"), "#{templateBBean.saveItemAction}");
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List i = new ArrayList();
		i.add(new NavigationCase("success", new SimpleViewParameters(ControlItemsProducer.VIEW_ID)));
		return i;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters() {
		return new ItemViewParameters();
	}

}