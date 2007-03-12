/******************************************************************************
 * TemplateModifyProducer.java - created by fengr@vt.edu on Aug 21, 2006
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
 * Antranig Basman (antranig@caret.cam.ac.uk)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool.producers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;
import org.sakaiproject.evaluation.tool.params.BlockIdsParameters;
import org.sakaiproject.evaluation.tool.params.TemplateViewParameters;
import org.sakaiproject.evaluation.tool.params.PreviewEvalParameters;
import org.sakaiproject.evaluation.tool.params.TemplateItemViewParameters;
import org.sakaiproject.evaluation.tool.utils.RSFUtils;
import org.sakaiproject.evaluation.tool.utils.TemplateItemUtils;

import uk.org.ponder.htmlutil.HTMLUtil;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;

/**
 * This is the main page for handling various operations to template, items,
 * 
 * @author: Rui Feng (fengr@vt.edu)
 * @author: Kapil Ahuja (kahuja@vt.edu)
 * @author: Antranig Basman (antranig@caret.cam.ac.uk)
 */

public class ModifyTemplateItemsProducer implements ViewComponentProducer,
		ViewParamsReporter {

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
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {

		TemplateViewParameters evalViewParams = (TemplateViewParameters) viewparams;

		System.out.println("templateBBean.templateId=" + evalViewParams.templateId);
		Long templateId = evalViewParams.templateId;
		EvalTemplate template = localTemplateLogic.fetchTemplate(templateId);

		UIMessage.make(tofill, "modify-template-title", "modifytemplate.page.title"); //$NON-NLS-1$ //$NON-NLS-2$

		UIInternalLink.make(tofill,
				"summary-toplink", UIMessage.make("summary.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));

		UIForm form = UIForm.make(tofill, "modifyForm"); //$NON-NLS-1$

		// preview link
		UIInternalLink.make(tofill, "preview_eval_link", new PreviewEvalParameters(
				PreviewEvalProducer.VIEW_ID, null, templateId, null,
				ModifyTemplateItemsProducer.VIEW_ID));

		UIMessage.make(tofill, "preview-eval-desc",
				"modifytemplate.preview.eval.desc"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(form, "add-item-note", "modifytemplate.add.item.note"); //$NON-NLS-1$ //$NON-NLS-2$

		String[] labels = new String[] {
				"modifytemplate.itemtype.scaled", 
				"modifytemplate.itemtype.text",
				"modifytemplate.itemtype.header", 
				"modifytemplate.itemtype.expert"
			};
		String[] viewIDs = { 
				ModifyScaledProducer.VIEW_ID,
				ModifyEssayProducer.VIEW_ID, 
				ModifyHeaderProducer.VIEW_ID,
				ExpertCategoryProducer.VIEW_ID
			};
		String[] values = convertViews(viewIDs, templateId);

		// dropdown list
		UISelect.make(form, "itemClassification", values, labels, values[0], false).setMessageKeys(); //$NON-NLS-1$

		UICommand.make(form, "add_questions", UIMessage.make("modifytemplate.add.item.button")); //$NON-NLS-1$ //$NON-NLS-2$

		List l = localTemplateLogic.fetchTemplateItems(templateId);
		List templateItemsList = TemplateItemUtils.getNonChildItems(l);
		if (templateItemsList.isEmpty()) {
			UIMessage.make(tofill, "begin-eval-dummylink",
					"modifytemplate.begin.eval.link");
		} else {
			UIInternalLink.make(tofill, "begin_eval_link", new TemplateViewParameters(
					EvaluationStartProducer.VIEW_ID, templateId));
		}

		UIMessage.make(tofill,
				"univ-level-header", "modifytemplate.univ.level.header"); //$NON-NLS-1$ //$NON-NLS-2$			
		//get count of existing items
		Integer count = new Integer(templateItemsList.size());
		UIOutput.make(tofill,"itemCount",count.toString());
		UIMessage.make(tofill, "existing-items", "modifytemplate.existing.items"); //$NON-NLS-1$ //$NON-NLS-2$

		UIMessage header = UIMessage.make(tofill,
				"template-title-header", "modifytemplate.template.title.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIOutput title = UIOutput.make(tofill, "title", template.getTitle());
		RSFUtils.targetLabel(header, title);

		UIInternalLink.make(tofill, "modify_title_desc_link", 
				UIMessage.make("modifytemplate.modify.title.desc.link"),
				new TemplateViewParameters(ModifyTemplateProducer.VIEW_ID, templateId));
		if (template.getDescription() != null
				&& !template.getDescription().trim().equals("")) {
			UIBranchContainer descbranch = UIBranchContainer.make(tofill,
					"description-switch:");
			UIMessage descheader = UIMessage.make(descbranch,
					"description-header", "modifytemplate.description.header"); //$NON-NLS-1$ //$NON-NLS-2$
			UIVerbatim description = UIVerbatim.make(descbranch, "description",
					template.getDescription());
			RSFUtils.targetLabel(descheader, description);
		}

		UIMessage.make(tofill, "eval-sample", "modifytemplate.eval.sample"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(tofill, "course-sample", "modifytemplate.course.sample"); //$NON-NLS-1$ //$NON-NLS-2$

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

				// only show Block Check box for scaled type(scale, block)
				if (myItem.getClassification().equals(EvalConstants.ITEM_TYPE_SCALED)) {
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
					String targetview = EvaluationConstant
							.classificationToView(myTemplateItem.getItem()
									.getClassification());
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
					List childList = TemplateItemUtils.getChildItems(l, myTemplateItem
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
		// the create block form
		UIForm blockForm = UIForm.make(tofill, "createBlockForm",
				new BlockIdsParameters(ModifyBlockProducer.VIEW_ID, templateId, null));
		UICommand.make(blockForm, "createBlockBtn", 
				UIMessage.make("modifytemplate.createblock.button"), null);
	}

}
