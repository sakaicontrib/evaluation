/******************************************************************************
 * TemplateModifyProducer.java - created on Aug 21, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu)
 * Antranig Basman (antranig@caret.cam.ac.uk)
 * Rui Feng (fengr@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.List;

import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;
import org.sakaiproject.evaluation.tool.params.BlockIdsParameters;
import org.sakaiproject.evaluation.tool.params.PreviewEvalParameters;
import org.sakaiproject.evaluation.tool.params.TemplateItemViewParameters;
import org.sakaiproject.evaluation.tool.params.TemplateViewParameters;
import org.sakaiproject.evaluation.tool.utils.TemplateItemUtils;

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
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;

/**
 * This is the main page for handling various operations to template, items,
 * 
 * @author: Aaron Zeckoski (aaronz@vt.edu)
 * @author: Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class ModifyTemplateItemsProducer implements ViewComponentProducer, ViewParamsReporter {

	public static final String VIEW_ID = "modify_template_items"; //$NON-NLS-1$
	public String getViewID() {
		return VIEW_ID;
	}

	public ViewParameters getViewParameters() {
		return new TemplateViewParameters();
	}

	private ViewStateHandler viewStateHandler;
	public void setViewStateHandler(ViewStateHandler viewStateHandler) {
		this.viewStateHandler = viewStateHandler;
	}

	private LocalTemplateLogic localTemplateLogic;
	public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
		this.localTemplateLogic = localTemplateLogic;
	}

	private ViewParameters deriveTarget(String viewID, Long templateId) {
		return new TemplateItemViewParameters(viewID, templateId, null);
	}

	private String[] convertViews(String[] viewIDs, Long templateId) {
		String[] togo = new String[viewIDs.length];
		for (int i = 0; i < viewIDs.length; ++i) {
			togo[i] = viewStateHandler
					.getFullURL(deriveTarget(viewIDs[i], templateId));
		}
		return togo;
	}

	/*
	 * 1) access this page through "Continue and Add Questions" button on Template
	 * page 2) access this page through links on Control Panel or other 3) access
	 * this page through "Save" button on Template page
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		TemplateViewParameters evalViewParams = (TemplateViewParameters) viewparams;
		Long templateId = evalViewParams.templateId;
		EvalTemplate template = localTemplateLogic.fetchTemplate(templateId);

		UIMessage.make(tofill, "modify-template-title", "modifytemplate.page.title"); //$NON-NLS-1$ //$NON-NLS-2$

		UIInternalLink.make(tofill,	"summary-toplink", UIMessage.make("summary.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));
		UIInternalLink.make(tofill, "preview_eval_link", 
				new PreviewEvalParameters(PreviewEvalProducer.VIEW_ID, null, templateId, null, ModifyTemplateItemsProducer.VIEW_ID));

		UIMessage.make(tofill, "preview-eval-desc",	"modifytemplate.preview.eval.desc"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(tofill, "add-item-note", "modifytemplate.add.item.note"); //$NON-NLS-1$ //$NON-NLS-2$

		String[] labels = new String[] {
				"modifytemplate.itemtype.scaled", 
				"modifytemplate.itemtype.text",
				"modifytemplate.itemtype.header", 
				"modifytemplate.itemtype.existing",
				"modifytemplate.itemtype.expert"
			};
		String[] viewIDs = { 
				ModifyScaledProducer.VIEW_ID,
				ModifyEssayProducer.VIEW_ID, 
				ModifyHeaderProducer.VIEW_ID,
				ExistingItemsProducer.VIEW_ID,
				ExpertCategoryProducer.VIEW_ID
			};
		String[] values = convertViews(viewIDs, templateId);

		UIForm form = UIForm.make(tofill, "modifyForm"); //$NON-NLS-1$
		UISelect.make(form, "itemClassification", values, labels, values[0], false).setMessageKeys(); //$NON-NLS-1$
		UICommand.make(form, "add_questions", UIMessage.make("modifytemplate.add.item.button")); //$NON-NLS-1$ //$NON-NLS-2$

		List itemList = localTemplateLogic.fetchTemplateItems(templateId);
		List templateItemsList = TemplateItemUtils.getNonChildItems(itemList);
		if (templateItemsList.isEmpty()) {
			UIMessage.make(tofill, "begin-eval-dummylink", "modifytemplate.begin.eval.link");
		} else {
			UIInternalLink.make(tofill, "begin_eval_link", new TemplateViewParameters(
					EvaluationStartProducer.VIEW_ID, templateId));
		}

		// TODO - this should be the actual level and not some made up string
		String currentLevel = "Current";
		UIMessage.make(tofill, "level-header", "modifytemplate.level.header", 
				new String[] {currentLevel, new Integer(templateItemsList.size()).toString(), });			

		UIMessage.make(tofill, "template-title-header", "modifytemplate.template.title.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput.make(tofill, "title", template.getTitle());

		UIInternalLink.make(tofill, "modify_title_desc_link", UIMessage.make("modifytemplate.modify.title.desc.link"),
				new TemplateViewParameters(ModifyTemplateProducer.VIEW_ID, templateId));

		if (template.getDescription() != null && !template.getDescription().trim().equals("")) {
			UIBranchContainer descbranch = UIBranchContainer.make(tofill, "description-switch:");
			UIMessage.make(descbranch, "description-header", "modifytemplate.description.header"); //$NON-NLS-1$ //$NON-NLS-2$
			UIVerbatim.make(descbranch, "description", template.getDescription());
		}


		UIForm form2 = UIForm.make(tofill, "modifyFormRows");
		
		UICommand saveReorderButton = UICommand.make(form2, "saveReorderButton", "#{templateBBean.saveReorder}");
		saveReorderButton.parameters.add(new UIELBinding("#{templateBBean.templateId}", templateId));
		
		if ((templateItemsList != null) && (templateItemsList.size() > 0)) {
			String sCurItemNum = null;
			String templateItemOTPBinding = null;
			String templateItemOTP = null;
			
			for (int i = 0; i < templateItemsList.size(); i++) {
				EvalTemplateItem templateItem = (EvalTemplateItem) templateItemsList.get(i);
				sCurItemNum = Integer.toString(i);
				templateItemOTPBinding = "templateItemBeanLocator." + templateItem.getId();
				templateItemOTP = templateItemOTPBinding + ".";
				
				UIBranchContainer itemBranch = UIBranchContainer.make(form2, "item-row:", sCurItemNum);
				
				// hidden item num
				UIInput.make(itemBranch, "hidden-item-num", templateItemOTP + "displayOrder", sCurItemNum);

				// only show Block Check box for scaled and block parents
				if ( templateItem.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_SCALED) ||
						templateItem.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_BLOCK_PARENT) ) {
					UIOutput checkBranch = UIOutput.make(itemBranch, "block-check-branch");
					UIBoundBoolean blockCB = UIBoundBoolean.make(itemBranch, "block-checkbox", Boolean.FALSE);
					// we have to force the id so the JS block checking can work
					String name = "block-" + templateItem.getItem().getScale().getId() + "-" + templateItem.getId();
					blockCB.decorators = new DecoratorList( new UIIDStrategyDecorator(name) );
					// have to force the target id so that the label for works 
					UILabelTargetDecorator uild = new UILabelTargetDecorator(blockCB);
					uild.targetFullID = name;
					checkBranch.decorators = new DecoratorList( uild );
					// tooltip
					blockCB.decorators.add( new UITooltipDecorator( UIMessage.make("modifytemplate.item.checkbox.title") ) );
				}

				UIOutput.make(itemBranch, "item-classification", templateItem.getItem().getClassification());

				if (templateItem.getScaleDisplaySetting() != null) {
					String scaleDisplaySettingLabel = " - " + templateItem.getScaleDisplaySetting();
					UIOutput.make(itemBranch, "scale-display", scaleDisplaySettingLabel);
				}

				UIInternalLink.make(itemBranch, "preview-row-item", UIMessage.make("modifytemplate.preview.link"), 
						new TemplateItemViewParameters(PreviewItemProducer.VIEW_ID, templateId, templateItem.getId()) );

				if ((templateItem.getBlockParent() != null) && (templateItem.getBlockParent().booleanValue() == true)) {
					// if it is a block item
					BlockIdsParameters target = new BlockIdsParameters(ModifyBlockProducer.VIEW_ID, templateId, templateItem.getId().toString());
					UIInternalLink.make(itemBranch, "modify-row-item", UIMessage.make("modifytemplate.modify.link"), target);
				} else {
					// it is a non-block item
					String targetView = EvaluationConstant.classificationToView(templateItem.getItem().getClassification());
					ViewParameters target = new TemplateItemViewParameters(targetView, templateItem.getTemplate().getId(), templateItem.getId());
					UIInternalLink.make(itemBranch, "modify-row-item", UIMessage.make("modifytemplate.modify.link"), target);
				}

				UIInternalLink.make(itemBranch,	"remove-row-item", UIMessage.make("modifytemplate.remove.link"),
						new TemplateItemViewParameters(RemoveQuestionProducer.VIEW_ID, templateId, templateItem.getId()) );


				// second line
				UIOutput.make(itemBranch, "item-num", new Integer(i + 1).toString());
				UIVerbatim.make(itemBranch, "item-text", templateItem.getItem().getItemText());

				if ( templateItem.getItem().getScale() != null ) {
					UIOutput.make(itemBranch, "scale-type", templateItem.getItem().getScale().getTitle());
				}

				if ((templateItem.getUsesNA() != null) && (templateItem.getUsesNA().booleanValue()) ) {
					UIMessage.make(itemBranch, "item-na", "viewitem.na.desc");
				}

				// block child items
				if ( templateItem.getBlockParent() != null && templateItem.getBlockParent().booleanValue() ) {
					List childList = TemplateItemUtils.getChildItems(itemList, templateItem.getId());
					if (childList.size() > 0) {
						UIBranchContainer blockChildren = UIBranchContainer.make(itemBranch, "block-children:");
						for (int k = 0; k < childList.size(); k++) {
							EvalTemplateItem child = (EvalTemplateItem) childList.get(k);
							UIBranchContainer childRow = UIBranchContainer.make(blockChildren, "child-item:", Integer.toString(k));
							UIOutput.make(childRow, "child-item-num", child.getDisplayOrder().toString());
							UIVerbatim.make(childRow, "child-item-text", child.getItem().getItemText());
						}
					} else {
						throw new IllegalStateException("Block parent with no items in it, id=" + templateItem.getId());
					}
				}
			}
			
		}
		
		/*
		UIForm form2 = UIForm.make(tofill, "modifyFormRows"); //$NON-NLS-1$
		UICommand reorder = UICommand.make(form2, "hiddenBtn",
				"#{templateBBean.saveReorder}");
		reorder.parameters.add(new UIELBinding("#{templateBBean.templateId}",
				templateId));

		if (templateItemsList != null && templateItemsList.size() > 0) {
			UIVerbatim.make(form2, "decorateSelects", HTMLUtil.emitJavascriptCall(
					"EvalSystem.decorateReorderSelects", new String[] { "",
							Integer.toString(templateItemsList.size()) }));

			String[] strArr = new String[templateItemsList.size()];
			for (int h = 0; h < templateItemsList.size(); h++) {
				strArr[h] = Integer.toString(h + 1);
			}

			String templateItemOTPBinding;
			String templateItemOTP;
			for (int i = 0; i < templateItemsList.size(); i++) {
				EvalTemplateItem myTemplateItem = (EvalTemplateItem) templateItemsList
						.get(i);
				EvalItem myItem = myTemplateItem.getItem();

				templateItemOTPBinding = "templateItemBeanLocator."
						+ myTemplateItem.getId();
				templateItemOTP = templateItemOTPBinding + ".";

				UIBranchContainer radiobranch = UIBranchContainer.make(form2,
						"itemrow:header", Integer.toString(i)); //$NON-NLS-1$
				UIMessage.make(radiobranch,
						"item-num-header", "modifytemplate.item.num.header"); //$NON-NLS-1$ //$NON-NLS-2$

				// only show Block Check box for scaled and block parents
				if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_SCALED) ||
						myItem.getClassification().equals(EvalConstants.ITEM_TYPE_BLOCK_PARENT) ) {
					UIBranchContainer rbShowBlockCB = UIBranchContainer.make(radiobranch,
							"showCB:");
					UIBoundBoolean blockCB = UIBoundBoolean.make(rbShowBlockCB,
							"blockCheckBox", Boolean.FALSE);
					Map attrmap = new HashMap();
					String name = "block-" + myTemplateItem.getItem().getScale().getId()
							+ "-" + myTemplateItem.getId();
					attrmap.put("id", name);
					blockCB.decorators = new DecoratorList(new UIFreeAttributeDecorator(
							attrmap));
				}

				// DISPLAY ORDER
				UISelect.make(radiobranch, "itemNum", strArr, templateItemOTP
						+ "displayOrder", null);

				String itemClassificationLabel = myTemplateItem.getItem()
						.getClassification();
				UIOutput.make(radiobranch, "itemClassificationLabel",
						itemClassificationLabel);

				String scaleDisplaySettingLabel = myTemplateItem
						.getScaleDisplaySetting();

				if (scaleDisplaySettingLabel != null)
					scaleDisplaySettingLabel = " - " + scaleDisplaySettingLabel;
				else
					scaleDisplaySettingLabel = "";
				UIOutput.make(radiobranch, "scaleDisplaySetting",
						scaleDisplaySettingLabel);

				UIInternalLink.make(radiobranch, "preview_row_item", 
						UIMessage.make("modifytemplate.preview.link"),
						new TemplateItemViewParameters(PreviewItemProducer.VIEW_ID,
								templateId, myTemplateItem.getId()));

				// if it is a Block item
				if (myTemplateItem.getBlockParent() != null
						&& myTemplateItem.getBlockParent().booleanValue() == true) {
					BlockIdsParameters target = new BlockIdsParameters(
							ModifyBlockProducer.VIEW_ID, templateId, myTemplateItem.getId()
									.toString());
					UIInternalLink.make(radiobranch, "modify_row_item", 
							UIMessage.make("modifytemplate.modify.link"), target);

				} else { // if it is non-block item
					String targetview = EvaluationConstant.classificationToView(myTemplateItem.getItem().getClassification());
					ViewParameters target = new TemplateItemViewParameters(targetview,
							myTemplateItem.getTemplate().getId(), myTemplateItem.getId());
					UIInternalLink.make(radiobranch, "modify_row_item", 
							UIMessage.make("modifytemplate.modify.link"), target);
				}

				UIInternalLink.make(radiobranch, "remove_row_item", 
						UIMessage.make("modifytemplate.remove.link"),
						new TemplateItemViewParameters(RemoveQuestionProducer.VIEW_ID,
								templateId, myTemplateItem.getId()));

				UIBranchContainer radiobranch2 = UIBranchContainer.make(form2,
						"itemrow:text", Integer.toString(i)); //$NON-NLS-1$
				UIOutput.make(radiobranch2, "queNo", Integer.toString(i + 1)); //$NON-NLS-1$
				UIVerbatim.make(radiobranch2, "itemText", myTemplateItem.getItem()
						.getItemText());

				String scaletitle = ""; //$NON-NLS-1$				

				if (myTemplateItem.getItem().getScale() != null)
					scaletitle = myTemplateItem.getItem().getScale().getTitle();
				UIOutput.make(radiobranch2, "scaleType", scaletitle); //$NON-NLS-1$

				if (!TemplateItemUtils.getTemplateItemType(myTemplateItem).equals(
						EvalConstants.ITEM_TYPE_HEADER)) {
					Boolean useNA = myTemplateItem.getUsesNA();
					if (useNA != null && useNA.booleanValue() == true) {
						UIBranchContainer radiobranch3 = UIBranchContainer.make(
								radiobranch2, "showNA:", Integer.toString(i)); //$NON-NLS-1$
						UIBoundBoolean.make(radiobranch3, "itemNA", useNA); //$NON-NLS-1$
						UIMessage.make(radiobranch3, "na-desc", "viewitem.na.desc"); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}

				// rendering block child items
				if (myTemplateItem.getBlockParent() != null
						&& myTemplateItem.getBlockParent().booleanValue() == true) {
					List childList = TemplateItemUtils.getChildItems(itemList, myTemplateItem
							.getId());
					for (int k = 0; k < childList.size(); k++) {
						UIBranchContainer childRow = UIBranchContainer.make(form2,
								"itemrow:blockItems", Integer.toString(k));
						EvalTemplateItem childTI = (EvalTemplateItem) childList.get(k);
						UIOutput.make(childRow, "childItemId", childTI.getDisplayOrder()
								.toString());
						UIVerbatim.make(childRow, "childItemText", childTI.getItem()
								.getItemText());
					}
				}
			}// end of for loop
		}
		*/
		
		// the create block form
		UIForm blockForm = UIForm.make(tofill, "createBlockForm",
				new BlockIdsParameters(ModifyBlockProducer.VIEW_ID, templateId, null));
		UICommand.make(blockForm, "createBlockBtn", 
				UIMessage.make("modifytemplate.createblock.button"), null);
	}

}
