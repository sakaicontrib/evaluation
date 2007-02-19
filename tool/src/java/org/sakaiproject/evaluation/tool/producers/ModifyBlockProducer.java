/******************************************************************************
 * ModifyBlockProducer.java - created by fengr@vt.edu on Oct 2, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.params.BlockIdsParameters;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;
import org.sakaiproject.evaluation.tool.utils.ItemBlockUtils;
import org.sakaiproject.evaluation.tool.utils.TemplateItemUtils;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
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
 * Page for Create, modify, preview, delete a Block type Item
 * 
 * @author: Rui Feng (fengr@vt.edu)
 */

public class ModifyBlockProducer implements ViewComponentProducer,
		ViewParamsReporter, DynamicNavigationCaseReporter {
	public static final String VIEW_ID = "modify_block"; //$NON-NLS-1$

	public String getViewID() {
		return VIEW_ID;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic(EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	private TextInputEvolver richTextEvolver;
	public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
		this.richTextEvolver = richTextEvolver;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		BlockIdsParameters evParameters = (BlockIdsParameters) viewparams;

		Long templateId = evParameters.templateId;

		System.out.println("templateId=" + evParameters.templateId);
		System.out.println("block item ids=" + evParameters.templateItemIds);

		boolean modify = false;// this variable indicate if it is for modify
		// existing Block
		boolean validScaleIds = true;// this variable indicate if the passed Ids
		// have the same scale
		Integer firstDO = null;// the first items's original displayOrder
		String templateItemIds = evParameters.templateItemIds;
		boolean createFromBlock = false;

		// analyze the string of templateItemIds
		String[] strIds = evParameters.templateItemIds.split(",");
		EvalTemplateItem templateItems[] = new EvalTemplateItem[strIds.length];
		for (int i = 0; i < strIds.length; i++) {
			System.out.println("checked id[" + i + "]=" + strIds[i]);
			templateItems[i] = itemsLogic
					.getTemplateItemById(Long.valueOf(strIds[i]));
		}

		firstDO = templateItems[0].getDisplayOrder();

		// check if it is to modify an existing block or creating a new one
		if (strIds.length == 1 && templateItems[0] != null)
			modify = true;

		// check if each templateItem has the same scale, otherwise show warning
		// text
		if (templateItems.length > 1) {
			Long scaleId = templateItems[0].getItem().getScale().getId();
			System.out.println("scale id[" + 0 + "]=" + scaleId.intValue());
			for (int i = 1; i < templateItems.length; i++) {
				Long myScaleId = templateItems[i].getItem().getScale().getId();
				System.out.println("scale id[" + i + "]=" + myScaleId.intValue());
				if (!myScaleId.equals(scaleId)) {
					validScaleIds = false;
					System.out.println("scale is not same");
					break;
				}
			}
		}

		if (!modify && validScaleIds) {// creating new block with the same scale
			// case
			boolean shift = false;
			// get the first Block ID if any, and shift it to the first of the
			// templateItems array
			for (int i = 0; i < templateItems.length; i++) {
				if (templateItems[i].getBlockParent() != null
						&& templateItems[i].getBlockParent().booleanValue() == true) {
					if (i > 0) {
						EvalTemplateItem tmpTI = templateItems[0];
						templateItems[0] = templateItems[i];
						templateItems[i] = tmpTI;
						shift = true;
					}
					createFromBlock = true;
					break;
				}
			}
			// reconstruct IDs to be passed
			if (shift) {
				templateItemIds = templateItems[0].getId().toString();
				for (int i = 1; i < templateItems.length; i++) {
					templateItemIds = templateItemIds + ","
							+ templateItems[i].getId().toString();
				}// end of for loop
			}
		}

		UIOutput.make(tofill, "modify-block-title", messageLocator
				.getMessage("modifyblock.page.title")); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "create-eval-title", messageLocator
				.getMessage("createeval.page.title")); //$NON-NLS-1$ //$NON-NLS-2$

		UIInternalLink.make(tofill,
				"summary-toplink", messageLocator.getMessage("summary.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));

		if (!validScaleIds) {
			// show error page with back button
			UIBranchContainer showError = UIBranchContainer
					.make(tofill, "errorPage:");
			UIOutput.make(showError, "errorMsg", messageLocator
					.getMessage("modifyblock.error.message"));
			UIOutput.make(showError, "back-button", messageLocator
					.getMessage("modifyblock.back.button"));

		} else {// render block page

			UIBranchContainer showBlock = UIBranchContainer
					.make(tofill, "blockPage:");
			UIForm form = UIForm.make(showBlock, "blockForm"); //$NON-NLS-1$

			UIOutput.make(form,
					"item-header", messageLocator.getMessage("modifyitem.item.header")); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(form, "itemNo", "1."); // TODO:
			UIOutput.make(form, "itemClassification", messageLocator
					.getMessage("modifytemplate.itemtype.block"));
			UIOutput.make(form, "added-by", messageLocator
					.getMessage("modifyitem.added.by"));
			UIOutput.make(form, "userInfo", external
					.getUserDisplayName(templateItems[0].getOwner()));
			// TODO: remove link

			UIOutput.make(form, "item-header-text-header", messageLocator
					.getMessage("modifyblock.item.header.text.header"));

			UIOutput.make(form, "scale-type-header", messageLocator
					.getMessage("modifyblock.scale.type.header"));
			UIOutput.make(form, "scaleLabel", templateItems[0].getItem().getScale()
					.getTitle());

			UIOutput.make(form, "add-na-header", messageLocator
					.getMessage("modifyitem.add.na.header")); //$NON-NLS-1$ //$NON-NLS-2$

			UIOutput.make(form, "ideal-coloring-header", messageLocator
					.getMessage("modifyblock.ideal.coloring.header")); //$NON-NLS-1$ //$NON-NLS-2$

			UIOutput.make(form, "item-category-header", messageLocator
					.getMessage("modifyitem.item.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(form, "course-category-header", messageLocator
					.getMessage("modifyitem.course.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
			UIOutput.make(form, "instructor-category-header", messageLocator
					.getMessage("modifyitem.instructor.category.header")); //$NON-NLS-1$ //$NON-NLS-2$
			// Radio Buttons for "Item Category"
			String[] courseCategoryList = {
					messageLocator.getMessage("modifyitem.course.category.header"),
					messageLocator.getMessage("modifyitem.instructor.category.header"), };

			UISelect radios = null;
			String itemPath = null;
			if (modify) {// modify existing block
				itemPath = "templateItemBeanLocator." + templateItems[0].getId();
				if (templateItems[0].getScaleDisplaySetting() != null
						&& templateItems[0].getScaleDisplaySetting().equals(
								EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED))
					UIBoundBoolean.make(form, "idealColor",
							"#{templateBBean.idealColor}", Boolean.TRUE);
				else
					UIBoundBoolean.make(form, "idealColor",
							"#{templateBBean.idealColor}", null);

				radios = UISelect.make(form, "item_category",
						EvaluationConstant.ITEM_CATEGORY_VALUES, courseCategoryList,
						"templateItemBeanLocator." + templateItems[0].getId()
								+ ".itemCategory", null);
			} else {// create new block
				// creat new block from multiple existing Block and other scaled item
				if (createFromBlock) {
					itemPath = "templateItemBeanLocator." + templateItems[0].getId();
					if (templateItems[0].getScaleDisplaySetting() != null
							&& templateItems[0].getScaleDisplaySetting().equals(
									EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED))
						UIBoundBoolean.make(form, "idealColor",
								"#{templateBBean.idealColor}", Boolean.TRUE);
					else
						UIBoundBoolean.make(form, "idealColor",
								"#{templateBBean.idealColor}", null);

					radios = UISelect.make(form, "item_category",
							EvaluationConstant.ITEM_CATEGORY_VALUES, courseCategoryList,
							"templateItemBeanLocator.new1." + "itemCategory", null);
				} else {
					// selected items are all normal scaled type
					itemPath = "templateItemBeanLocator.new1";
					UIBoundBoolean.make(form, "idealColor",
							"#{templateBBean.idealColor}", null);
					radios = UISelect.make(form, "item_category",
							EvaluationConstant.ITEM_CATEGORY_VALUES, courseCategoryList,
							"templateItemBeanLocator.new1." + "itemCategory", null);
				}
			}
			String itemPathD = itemPath + ".";
			UIInput itemtext = UIInput.make(form, "item_text:", itemPathD
					+ "item.itemText", null);
			richTextEvolver.evolveTextInput(itemtext);
			UIBoundBoolean.make(form, "item_NA", itemPathD + "usesNA", null);

			String selectID = radios.getFullID();
			UISelectChoice.make(form, "item_category_C", selectID, 0); //$NON-NLS-1$
			UISelectChoice.make(form, "item_category_I", selectID, 1);

			if (modify) {// for modify existing block item
				// get Block child item
				EvalTemplate template = templateItems[0].getTemplate();
				List l = itemsLogic.getTemplateItemsForTemplate(template.getId(), null);
				List childList = ItemBlockUtils.getChildItems(l, templateItems[0]
						.getId());
				for (int i = 0; i < childList.size(); i++) {
					EvalTemplateItem child = (EvalTemplateItem) childList.get(i);
					UIBranchContainer radiobranch = UIBranchContainer.make(form,
							"queRow:", Integer.toString(i)); //$NON-NLS-1$
					UIOutput.make(radiobranch, "childOrder", child.getDisplayOrder()
							.toString());
					// TODO: This should be rich text but it would seem no way there is
					// space. NB this is now serious security hole for HTML injection. 
					UIInput.make(radiobranch, "queText", null, child.getItem()
							.getItemText());
				}
			} else {
				if (createFromBlock) {// render the first block child , then others,
					// possibly other block child
					// TODO: wait for Aaron' s logic layer method to get child
					// TemplateItems providing parent ID
					List allTemplateItems = itemsLogic.getTemplateItemsForTemplate(
							templateId, null);
					int orderNo = 0;
					for (int i = 0; i < templateItems.length; i++) {
						if (TemplateItemUtils.getTemplateItemType(templateItems[i]).equals(
								EvalConstants.ITEM_TYPE_BLOCK)) {

							List childs = ItemBlockUtils.getChildItems(allTemplateItems,
									templateItems[i].getId());

							for (int k = 0; k < childs.size(); k++) {
								EvalTemplateItem myChild = (EvalTemplateItem) childs.get(k);
								UIBranchContainer radiobranch = UIBranchContainer.make(form,
										"queRow:", Integer.toString(orderNo)); //$NON-NLS-1$
								UIOutput.make(radiobranch, "childOrder", Integer
										.toString(orderNo + 1));
								// TODO: This should be rich text but it would seem no way there is
								// space. NB this is now serious security hole for HTML injection. 
								UIInput.make(radiobranch, "queText", null, myChild.getItem()
										.getItemText());
								orderNo++;
							}
						} else {// normal scale type
							UIBranchContainer radiobranch = UIBranchContainer.make(form,
									"queRow:", Integer.toString(orderNo)); //$NON-NLS-1$
							UIOutput.make(radiobranch, "childOrder", Integer
									.toString(orderNo + 1));
							UIInput.make(radiobranch, "queText", null, templateItems[i]
									.getItem().getItemText());
							orderNo++;
						}
					}

				} else {
					// selected items are all normal scaled type
					for (int i = 0; i < templateItems.length; i++) {
						UIBranchContainer radiobranch = UIBranchContainer.make(form,
								"queRow:", Integer.toString(i)); //$NON-NLS-1$
						UIOutput.make(radiobranch, "childOrder", Integer.toString(i + 1));
						UIInput.make(radiobranch, "queText", null, templateItems[i]
								.getItem().getItemText());
					}
				}

			}

			UIOutput.make(form, "cancel-button", messageLocator
					.getMessage("general.cancel.button"));
			UICommand saveCmd = UICommand.make(form, "saveBlockAction",
					messageLocator.getMessage("modifyitem.save.button"),
					"#{templateBBean.saveBlockItemAction}");
			saveCmd.parameters.add(new UIELBinding(
					"#{templateBBean.childTemplateItemIds}", templateItemIds));
			saveCmd.parameters.add(new UIELBinding(
					"#{templateBBean.originalDisplayOrder}", firstDO));

		}

	}

	public List reportNavigationCases() {
		List i = new ArrayList();

		i.add(new NavigationCase(PreviewItemProducer.VIEW_ID,
				new SimpleViewParameters(PreviewItemProducer.VIEW_ID)));
		i.add(new NavigationCase("success", new EvalViewParameters(
				TemplateModifyProducer.VIEW_ID, null)));

		return i;
	}

	public ViewParameters getViewParameters() {
		return new BlockIdsParameters();
	}

}
