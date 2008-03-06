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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;
import org.sakaiproject.evaluation.tool.renderers.AddItemControlRenderer;
import org.sakaiproject.evaluation.tool.viewparams.BlockIdsParameters;
import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
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

/**
 * This is the main page for handling various operations to template, items,
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class ModifyTemplateItemsProducer implements ViewComponentProducer, ViewParamsReporter {

   public static final String VIEW_ID = "modify_template_items"; //$NON-NLS-1$
   public String getViewID() {
      return VIEW_ID;
   }

   private LocalTemplateLogic localTemplateLogic;
   public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
      this.localTemplateLogic = localTemplateLogic;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }

   private AddItemControlRenderer addItemControlRenderer;
   public void setAddItemControlRenderer(AddItemControlRenderer addItemControlRenderer) {
      this.addItemControlRenderer = addItemControlRenderer;
   }

   private EvalSettings evalSettings;
   public void setEvalSettings(EvalSettings settings) {
      this.evalSettings = settings;
   }

   private ExternalHierarchyLogic hierarchyLogic;
   public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
      this.hierarchyLogic = logic;
   }

   /*
    * 1) access this page through "Continue and Add Questions" button on Template
    * page 2) access this page through links on Control Panel or other 3) access
    * this page through "Save" button on Template page
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      // local variables used in the render logic
      String currentUserId = externalLogic.getCurrentUserId();
      boolean userAdmin = externalLogic.isUserAdmin(currentUserId);
      boolean createTemplate = authoringService.canCreateTemplate(currentUserId);
      boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);

      /*
       * top links here
       */
      UIInternalLink.make(tofill, "summary-link", 
            UIMessage.make("summary.page.title"), 
            new SimpleViewParameters(SummaryProducer.VIEW_ID));

      if (userAdmin) {
         UIInternalLink.make(tofill, "administrate-link", 
               UIMessage.make("administrate.page.title"),
               new SimpleViewParameters(AdministrateProducer.VIEW_ID));
         UIInternalLink.make(tofill, "control-scales-link",
               UIMessage.make("controlscales.page.title"),
               new SimpleViewParameters(ControlScalesProducer.VIEW_ID));
      }

      if (createTemplate) {
         UIInternalLink.make(tofill, "control-templates-link",
               UIMessage.make("controltemplates.page.title"), 
               new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
         UIInternalLink.make(tofill, "control-items-link",
               UIMessage.make("controlitems.page.title"), 
               new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
      } else {
         throw new SecurityException("User attempted to access " + 
               VIEW_ID + " when they are not allowed");
      }

      if (beginEvaluation) {
         UIInternalLink.make(tofill, "control-evaluations-link",
               UIMessage.make("controlevaluations.page.title"),
            new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
      }

      TemplateViewParameters evalViewParams = (TemplateViewParameters) viewparams;
      Long templateId = evalViewParams.templateId;
      EvalTemplate template = localTemplateLogic.fetchTemplate(templateId);

      // begin page rendering
      UIMessage.make(tofill, "modify-template-title", "modifytemplate.page.title");

      UIInternalLink.make(tofill, "preview_eval_link", UIMessage.make("modifytemplate.preview.eval.link"),
            new EvalViewParameters(PreviewEvalProducer.VIEW_ID, null, templateId))
            .decorate( new UITooltipDecorator( UIMessage.make("modifytemplate.preview.eval.link.title") ) );

      UIMessage.make(tofill, "preview-eval-desc",	"modifytemplate.preview.eval.desc");

      UILink.make(tofill, "preview-template-direct-link", UIMessage.make("general.direct.link"), 
            externalLogic.getEntityURL(template) )
            .decorate( new UITooltipDecorator( UIMessage.make("general.direct.link.title") ) );

      // get form to submit the type of item to create to the correct view
      UIMessage.make(tofill, "add-item-note", "modifytemplate.add.item.note");

      // create the choices for the pulldown
      ArrayList<ViewParameters> templateItemVPList = new ArrayList<ViewParameters>();
      ArrayList<String> templateItemLabelList = new ArrayList<String>();
      for (int i = 0; i < EvalToolConstants.ITEM_SELECT_CLASSIFICATION_VALUES.length; i++) {
         templateItemVPList.add( new ItemViewParameters(ModifyItemProducer.VIEW_ID, 
               EvalToolConstants.ITEM_SELECT_CLASSIFICATION_VALUES[i], templateId) );
         templateItemLabelList.add(EvalToolConstants.ITEM_SELECT_CLASSIFICATION_LABELS[i]);
      }

      // add in existing items selection
      templateItemVPList.add( new TemplateItemViewParameters(ExistingItemsProducer.VIEW_ID, templateId, null) );
      templateItemLabelList.add("item.classification.existing");

      // add in expert items choice if enabled
      Boolean useExpertItems = (Boolean) evalSettings.get(EvalSettings.USE_EXPERT_ITEMS);
      if (useExpertItems) {
         templateItemVPList.add( new TemplateItemViewParameters(ExpertCategoryProducer.VIEW_ID, templateId, null) );
         templateItemLabelList.add("item.classification.expert");
      }

      addItemControlRenderer.renderControl(tofill, "add-item-control:", 
            templateItemVPList.toArray(new ViewParameters[templateItemVPList.size()]), 
            templateItemLabelList.toArray(new String[templateItemLabelList.size()]), 
            UIMessage.make("modifytemplate.add.item.button"), templateId);

      List<EvalTemplateItem> itemList = localTemplateLogic.fetchTemplateItems(templateId);
      List<EvalTemplateItem> templateItemsList = TemplateItemUtils.getNonChildItems(itemList);
      if (templateItemsList.isEmpty()) {
         UIMessage.make(tofill, "begin-eval-dummylink", "modifytemplate.begin.eval.link");
      } else {
         UIInternalLink.make(tofill, "begin_eval_link", UIMessage.make("modifytemplate.begin.eval.link"), 
               new TemplateViewParameters(
                     EvaluationStartProducer.VIEW_ID, templateId)).decorators = 
                        new DecoratorList(new UITooltipDecorator(UIMessage.make("modifytemplate.begin.eval.link.title")));
      }

      // TODO - this should be the actual level and not some made up string
      String currentLevel = "Current";
      UIMessage.make(tofill, "level-header", "modifytemplate.level.header", 
            new String[] {currentLevel, new Integer(templateItemsList.size()).toString(), });			

      UIMessage.make(tofill, "template-title-header", "modifytemplate.template.title.header");
      UIOutput.make(tofill, "title", template.getTitle());

      UIInternalLink.make(tofill, "modify_title_desc_link", UIMessage.make("modifytemplate.modify.title.desc.link"),
            new TemplateViewParameters(ModifyTemplateProducer.VIEW_ID, templateId)).decorators = 
               new DecoratorList(new UITooltipDecorator(UIMessage.make("modifytemplate.modify.title.desc.link.title")));

      if (template.getDescription() != null && !template.getDescription().trim().equals("")) {
         UIBranchContainer descbranch = UIBranchContainer.make(tofill, "description-switch:");
         UIMessage.make(descbranch, "description-header", "modifytemplate.description.header");
         UIOutput.make(descbranch, "description", template.getDescription());
      }


      UIForm form2 = UIForm.make(tofill, "modifyFormRows");
      UICommand.make(form2, "hiddenBtn");
      form2.parameters.add(new UIELBinding("#{templateBBean.templateId}", templateId));

      UIMessage revertOrderButton = UIMessage.make(form2, "revertOrderButton", "modifytemplate.button.revert.order");
      revertOrderButton.decorators = new DecoratorList( new UITooltipDecorator( UIMessage.make("modifytemplate.button.revert.order.title") ) );
      UICommand saveReorderButton = UICommand.make(form2, "saveReorderButton", 
            UIMessage.make("modifytemplate.button.save.order"), "#{templateBBean.saveReorder}");
      saveReorderButton.parameters.add(new UIELBinding("#{templateBBean.templateId}", templateId));
      saveReorderButton.decorators = new DecoratorList( new UITooltipDecorator( UIMessage.make("modifytemplate.button.save.order.title") ) );

      UIMessage.make(form2, "orderingInstructions", "modifytemplate.instructions.reorder");

      if ((templateItemsList != null) && (templateItemsList.size() > 0)) {
         String sCurItemNum = null;
         String templateItemOTPBinding = null;
         String templateItemOTP = null;

         String[] itemNumArr = new String[templateItemsList.size()];
         for (int h = 0; h < templateItemsList.size(); h++) {
            itemNumArr[h] = Integer.toString(h + 1);
         }

         for (int i = 0; i < templateItemsList.size(); i++) {
            EvalTemplateItem templateItem = (EvalTemplateItem) templateItemsList.get(i);
            sCurItemNum = Integer.toString(i);
            templateItemOTPBinding = "templateItemWBL." + templateItem.getId();
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
               UIMessage.make(itemBranch, "check-input-label", "modifytemplate.check.label.title");
            } else {
               UIMessage.make(itemBranch, "check-placeholder", "modifytemplate.check.placeholder");
            }
            
            String itemLabelKey = EvalToolConstants.UNKNOWN_KEY;
            for (int j = 0; j < EvalToolConstants.ITEM_CLASSIFICATION_VALUES.length; j++) {
               if (templateItem.getItem().getClassification().equals(EvalToolConstants.ITEM_CLASSIFICATION_VALUES[j])) {
                  itemLabelKey = EvalToolConstants.ITEM_CLASSIFICATION_LABELS_PROPS[j];
                  break;
               }
            }
            UIMessage.make(itemBranch, "item-classification", itemLabelKey);

            if (templateItem.getScaleDisplaySetting() != null) {
               String scaleDisplaySettingLabel = " - " + templateItem.getScaleDisplaySetting();
               UIOutput.make(itemBranch, "scale-display", scaleDisplaySettingLabel);
            }

            /* Hierarchy Messages 
             * Only Display these if they are enabled in the preferences.
             */
            Boolean showHierarchy = (Boolean) evalSettings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
            if ( showHierarchy != null && showHierarchy.booleanValue() ) {
               UIMessage.make(itemBranch, "item-hierarchy-level-title", "modifytemplate.item.hierarchy.level.title");
               UIOutput.make(itemBranch, "item-hierarchy-level", templateItem.getHierarchyLevel());
               /* Don't show the Node Id if it's a top level item */
               if (!templateItem.getHierarchyLevel().equals(EvalConstants.HIERARCHY_LEVEL_TOP)) {
                  UIMessage.make(itemBranch, "item-hierarchy-nodeid-title", "modifytemplate.item.hierarchy.nodeid.title");
                  EvalHierarchyNode curnode = hierarchyLogic.getNodeById(templateItem.getHierarchyNodeId());
                  UIOutput.make(itemBranch, "item-hierarchy-nodeid", curnode.title);
               }
            }

            UIInternalLink.make(itemBranch, "preview-row-item", UIMessage.make("general.command.preview"), 
                  new ItemViewParameters(PreviewItemProducer.VIEW_ID, (Long) null, templateItem.getId()) );

            if ((templateItem.getBlockParent() != null) && (templateItem.getBlockParent().booleanValue() == true)) {
               // if it is a block item
               BlockIdsParameters target = new BlockIdsParameters(ModifyBlockProducer.VIEW_ID, templateId, templateItem.getId().toString());
               UIInternalLink.make(itemBranch, "modify-row-item", UIMessage.make("general.command.edit"), target);
            } else {
               // it is a non-block item
               ViewParameters target = new ItemViewParameters(ModifyItemProducer.VIEW_ID, 
                     templateItem.getItem().getClassification(), templateId, templateItem.getId());
               UIInternalLink.make(itemBranch, "modify-row-item", UIMessage.make("general.command.edit"), target);
            }

            UIInternalLink.make(itemBranch,	"remove-row-item", 
                  UIMessage.make("general.command.delete"),
                  new ItemViewParameters(RemoveItemProducer.VIEW_ID, (Long)null, templateItem.getId(), templateId) );


            // SECOND LINE

            UISelect orderPulldown = UISelect.make(itemBranch, "item-select", itemNumArr, templateItemOTP + "displayOrder", null);
            orderPulldown.decorators = new DecoratorList( new UITooltipDecorator( UIMessage.make("modifytemplate.select.order.title") ) );

            UIVerbatim.make(itemBranch, "item-text", templateItem.getItem().getItemText());

            String categoryMessage = "unknown.caps";
            if ( EvalConstants.ITEM_CATEGORY_COURSE.equals(templateItem.getCategory()) ) {
               categoryMessage = "modifyitem.course.category";
            } else if ( EvalConstants.ITEM_CATEGORY_INSTRUCTOR.equals(templateItem.getCategory()) ) {
               categoryMessage = "modifyitem.instructor.category";
            } else if ( EvalConstants.ITEM_CATEGORY_ENVIRONMENT.equals(templateItem.getCategory()) ) {
               categoryMessage = "modifyitem.environment.category";
            }
            UIMessage.make(itemBranch, "item-category", categoryMessage);
            UIMessage.make(tofill, "item-category-title", "modifytemplate.item.category.title");

            EvalUser owner = externalLogic.getEvalUserById( templateItem.getOwner() );
            UIOutput.make(itemBranch, "item-owner-name", owner.displayName);
            UIMessage.make(tofill, "item-owner-title", "modifytemplate.item.owner.title");

            Boolean useResultsSharing = (Boolean) evalSettings.get(EvalSettings.ITEM_USE_RESULTS_SHARING);
            if ( useResultsSharing != null && useResultsSharing.booleanValue() ) {
               // only show results sharing if it is being used
               UIMessage.make(tofill, "item-resultssharing-title", "modifytemplate.item.resultssharing.title");
               String resultsSharingMessage = "unknown.caps";
               if ( EvalConstants.SHARING_PUBLIC.equals(templateItem.getResultsSharing()) ) {
                  resultsSharingMessage = "general.public";
               } else if ( EvalConstants.SHARING_PRIVATE.equals(templateItem.getResultsSharing()) ) {
                  resultsSharingMessage = "general.private";
               }
               UIMessage.make(itemBranch, "item-results-sharing", resultsSharingMessage);
            }

            if ( EvalConstants.ITEM_TYPE_SCALED.equals(templateItem.getItem().getClassification()) &&
                  templateItem.getItem().getScale() != null ) {
               // only show the scale type of this is a scaled item
               UIMessage.make(itemBranch, "item-scale-type-title", "modifytemplate.item.scale.type.title");
               UIOutput.make(itemBranch, "scale-type", templateItem.getItem().getScale().getTitle());
            }

            if ((templateItem.getUsesNA() != null) && (templateItem.getUsesNA().booleanValue()) ) {
               UIMessage.make(itemBranch, "item-na-title", "modifytemplate.item.na.title");
               UIMessage.make(itemBranch, "item-na-value", "modifytemplate.item.na.value");
            }

            // block child items
            if ( templateItem.getBlockParent() != null && templateItem.getBlockParent().booleanValue() ) {
               List<EvalTemplateItem> childList = TemplateItemUtils.getChildItems(itemList, templateItem.getId());
               if (childList.size() > 0) {
                  UIBranchContainer blockChildren = UIBranchContainer.make(itemBranch, "block-children:");
                  for (int k = 0; k < childList.size(); k++) {
                     EvalTemplateItem child = childList.get(k);
                     UIBranchContainer childRow = UIBranchContainer.make(blockChildren, "child-item:", k+"");
                     UIOutput.make(childRow, "child-item-num", child.getDisplayOrder().toString());
                     UIVerbatim.make(childRow, "child-item-text", child.getItem().getItemText());
                  }
               } else {
                  throw new IllegalStateException("Block parent with no items in it, id=" + templateItem.getId());
               }
            }
         }

      }

      // this fills in the javascript call
      UIInitBlock.make(tofill, "decorateSelects", "EvalSystem.decorateReorderSelects", 
            new Object[] { "", Integer.toString(templateItemsList.size()) } );

      // the create block form
      UIForm blockForm = UIForm.make(tofill, "createBlockForm",
            new BlockIdsParameters(ModifyBlockProducer.VIEW_ID, templateId, null));
      UICommand createBlock = UICommand.make(blockForm, "createBlockBtn", UIMessage.make("modifytemplate.button.createblock") );
      createBlock.decorators = new DecoratorList( new UITooltipDecorator( UIMessage.make("modifytemplate.button.createblock.title") ) );

      UIMessage.make(form2, "blockInstructions", "modifytemplate.instructions.block");
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
    */
   public ViewParameters getViewParameters() {
      return new TemplateViewParameters();
   }

}
