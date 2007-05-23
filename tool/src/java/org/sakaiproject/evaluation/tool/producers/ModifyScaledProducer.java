/******************************************************************************
 * ModifyScaledProducerProducer.java - created by fengr@vt.edu on Aug 21, 2006
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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.ItemsBean;
import org.sakaiproject.evaluation.tool.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.tool.viewparams.TemplateItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;

import uk.org.ponder.rsf.components.ELReference;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBoundList;
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
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.DynamicNavigationCaseReporter;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Page for Create, modify,preview, delete a Scaled/Suvey type Item
 * 
 * @author: Rui Feng (fengr@vt.edu)
 */

public class ModifyScaledProducer implements ViewComponentProducer,
		ViewParamsReporter, DynamicNavigationCaseReporter {
	public static final String VIEW_ID = "modify_scaled"; //$NON-NLS-1$


	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private EvalTemplatesLogic templatesLogic;
	public void setTemplatesLogic(EvalTemplatesLogic templatesLogic) {
		this.templatesLogic = templatesLogic;
	}

	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
	}

	public String getViewID() {
		return VIEW_ID;
	}

	private TextInputEvolver richTextEvolver;
	public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
		this.richTextEvolver = richTextEvolver;
	}

	// TODO-shouldn't need these in producer, necessary to preload scaleId until
	// we have a darreshaper
	private ItemsBean itemsBean;
	public void setItemsBean(ItemsBean itemsBean) {
		this.itemsBean = itemsBean;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic(EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		TemplateItemViewParameters templateItemViewParams = (TemplateItemViewParameters) viewparams;

		String templateItemOTP = null;
		String templateItemOTPBinding = null;
		Long templateId = templateItemViewParams.templateId;
		Long templateItemId = templateItemViewParams.templateItemId;

		EvalTemplate template = templatesLogic.getTemplateById(templateId);

		if (templateItemId != null) {
			templateItemOTPBinding = "templateItemWBL." + templateItemId;
		} else {
			templateItemOTPBinding = "templateItemWBL.new1";
		}
		templateItemOTP = templateItemOTPBinding + ".";

		// TODO - replace with darreshaper
		if (templateItemId != null) {
			EvalTemplateItem myTemplateItem = itemsLogic
					.getTemplateItemById(templateItemId);
			if (myTemplateItem.getItem().getScale() != null) {
				itemsBean.scaleId = itemsLogic.getTemplateItemById(templateItemId)
						.getItem().getScale().getId();
			}
		}
		

		UIMessage.make(tofill, "modify-item-title", "templateitem.page.title"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(tofill, "create-eval-title", "starteval.page.title"); //$NON-NLS-1$ //$NON-NLS-2$

		UIInternalLink.make(tofill,"summary-toplink", UIMessage.make("summary.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));

		UIForm form = UIForm.make(tofill, "itemForm"); //$NON-NLS-1$

		UIMessage.make(form, "item-header", "modifyitem.item.header");

		if(templateItemId != null){			
			
			EvalTemplateItem ti = itemsLogic.getTemplateItemById(templateItemId);
			UIOutput.make(form, "itemNo",ti.getDisplayOrder().toString()); //$NON-NLS-1$ //$NON-NLS-2$
			
		}else{
			List l = itemsLogic.getTemplateItemsForTemplate(templateId, external.getCurrentUserId(), null);
			List templateItemsList = TemplateItemUtils.getNonChildItems(l);			
			Integer no = new Integer(templateItemsList.size()+1);
			UIOutput.make(form, "itemNo",no.toString());
		}
		
		UIOutput.make(form, "itemClassification", EvalConstants.ITEM_TYPE_SCALED);
		UIMessage.make(form,
				"added-by", "modifyitem.added.by"); //$NON-NLS-1$ //$NON-NLS-2$

		UIOutput.make(form, "userInfo", external.getUserDisplayName(template
				.getOwner()));

		if (templateItemId != null) {
			UIBranchContainer showLink = UIBranchContainer.make(form, "showRemoveLink:");
			UIInternalLink.make(showLink, "remove_link", UIMessage.make("modifytemplate.remove.link"),
					new TemplateItemViewParameters(RemoveItemProducer.VIEW_ID,
							templateId, templateItemId));
		}

		UIMessage.make(form, "question-text-header","modifyitem.question.text.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIInput itemText = UIInput.make(form,
				"item_text:", templateItemOTP + "item.itemText"); //$NON-NLS-1$ //$NON-NLS-2$
		richTextEvolver.evolveTextInput(itemText);

		UIMessage.make(form, "scale-type-header","templateitem.scale.type.header"); //$NON-NLS-1$ //$NON-NLS-2$
		// dropdown list for "Scale Type"
		UISelect combo = UISelect.make(form, "scaleList"); //$NON-NLS-1$
		combo.selection = new UIInput();
		combo.selection.valuebinding = new ELReference("#{itemsBean.scaleId}"); //$NON-NLS-1$
		UIBoundList comboValues = new UIBoundList();
		comboValues.valuebinding = new ELReference("#{itemsBean.scaleValues}"); //$NON-NLS-1$
		combo.optionlist = comboValues;
		UIBoundList comboNames = new UIBoundList();
		comboNames.valuebinding = new ELReference("#{itemsBean.scaleLabels}"); //$NON-NLS-1$
		combo.optionnames = comboNames;

		UIMessage.make(form, "scale-display-header", "templateitem.scale.display.header"); //$NON-NLS-1$ //$NON-NLS-2$
		// drop down list for "Scale Display Setting"
		UISelect.make(form, "scaleDisplaySetting",
				EvaluationConstant.SCALE_DISPLAY_SETTING_VALUES, 
				EvaluationConstant.SCALE_DISPLAY_SETTING_LABELS_PROPS,
				templateItemOTP + "scaleDisplaySetting", null).setMessageKeys(); //$NON-NLS-1$

		/*
		 * (non-javadoc) If the system setting (admin setting) for
		 * "EvalSettings.NOT_AVAILABLE_ALLOWED" is set as true then only we need to
		 * show the item_NA checkbox.
		 */
		if (((Boolean) settings.get(EvalSettings.NOT_AVAILABLE_ALLOWED))
				.booleanValue()) {
			UIBranchContainer showNA = UIBranchContainer.make(form, "showNA:"); //$NON-NLS-1$
			UIMessage.make(showNA,"add-na-header", "modifyitem.add.na.header"); //$NON-NLS-1$ //$NON-NLS-2$
			UIBoundBoolean.make(showNA, "item_NA", templateItemOTP + "usesNA", null); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/*
		 * (non-javadoc) If the system setting (admin setting) for
		 * "EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY" is set as true then all
		 * items default to "Course". If it is set to false, then all items default
		 * to "Instructor". If it is set to null then user is given the option to
		 * choose between "Course" and "Instructor".
		 */
		Boolean isDefaultCourse = (Boolean) settings
				.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY);
		// Means show both options (course and instructor)
		if (isDefaultCourse == null) {

			UIBranchContainer showItemCategory = UIBranchContainer.make(form, "showItemCategory:"); //$NON-NLS-1$
			UIMessage.make(showItemCategory, "item-category-header", "modifyitem.item.category.header"); //$NON-NLS-1$ //$NON-NLS-2$
			UIMessage.make(showItemCategory, "course-category-header", "modifyitem.course.category.header"); //$NON-NLS-1$ //$NON-NLS-2$
			UIMessage.make(showItemCategory, "instructor-category-header","modifyitem.instructor.category.header"); //$NON-NLS-1$ //$NON-NLS-2$

			// Radio Buttons for "Item Category"
			String[] courseCategoryList = {
					"modifyitem.course.category.header",
					"modifyitem.instructor.category.header" };
			UISelect radios = UISelect.make(showItemCategory, "item_category",
					EvaluationConstant.ITEM_CATEGORY_VALUES, courseCategoryList,
					templateItemOTP + "itemCategory", null);

			String selectID = radios.getFullID();
			UISelectChoice.make(showItemCategory, "item_category_C", selectID, 0); //$NON-NLS-1$
			UISelectChoice.make(showItemCategory, "item_category_I", selectID, 1); //$NON-NLS-1$
		} else {
			// Course category if default, instructor otherwise
			// Do not show on the page, just bind it explicitly.
			form.parameters.add(new UIELBinding(templateItemOTP + "itemCategory",
					EvaluationConstant.ITEM_CATEGORY_VALUES[isDefaultCourse
							.booleanValue() ? 0 : 1])); //$NON-NLS-1$
		}

		UIMessage.make(form, "cancel-button","general.cancel.button");

		UICommand saveCmd = UICommand.make(form, "saveItemAction", UIMessage.make("modifyitem.save.button"), "#{itemsBean.saveItemAction}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		saveCmd.parameters.add(new UIELBinding(templateItemOTP
				+ "item.classification", EvalConstants.ITEM_TYPE_SCALED));
		saveCmd.parameters.add(new UIELBinding("#{itemsBean.templateItem}",
				new ELReference(templateItemOTPBinding)));
		saveCmd.parameters.add(new UIELBinding("#{itemsBean.templateId}",
				templateId));

		// TODO-Preview new/modified items
		/*
		 * UICommand.make(form, "previewItemAction", messageLocator
		 * .getMessage("modifyitem.preview.button"),
		 * "#{itemsBean.previewItemAction}"); //$NON-NLS-1$ //$NON-NLS-2$
		 * //$NON-NLS-3$
		 */}

	public List reportNavigationCases() {
		List i = new ArrayList();
		// TODO: Preview navigation is incorrect
		i.add(new NavigationCase(PreviewItemProducer.VIEW_ID,
				new SimpleViewParameters(PreviewItemProducer.VIEW_ID)));
		i.add(new NavigationCase("success", new TemplateViewParameters(
				ModifyTemplateItemsProducer.VIEW_ID, null)));

		return i;
	}

	public ViewParameters getViewParameters() {
		return new TemplateItemViewParameters();
	}

}