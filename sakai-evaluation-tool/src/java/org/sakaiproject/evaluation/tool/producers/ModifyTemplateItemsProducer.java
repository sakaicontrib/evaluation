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
import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;
import org.sakaiproject.evaluation.tool.renderers.AddItemControlRenderer;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.viewparams.BlockIdsParameters;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;
import org.sakaiproject.util.FormattedText;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIIDStrategyDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This is the main page for handling various operations to template, items,
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class ModifyTemplateItemsProducer extends EvalCommonProducer implements ViewParamsReporter {

    public static final String VIEW_ID = "modify_template_items"; //$NON-NLS-1$
    public String getViewID() {
        return VIEW_ID;
    }

    private LocalTemplateLogic localTemplateLogic;
    public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
        this.localTemplateLogic = localTemplateLogic;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
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
    
    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator messageLocator) {
        this.messageLocator = messageLocator;
    }
    
    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

    /*
     * 1) access this page through "Continue and Add Questions" button on Template
     * page 2) access this page through links on Control Panel or other 3) access
     * this page through "Save" button on Template page
     */
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean createTemplate = authoringService.canCreateTemplate(currentUserId);
        boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);

        TemplateViewParameters evalViewParams = (TemplateViewParameters) viewparams;
        Long templateId = evalViewParams.templateId;
        EvalTemplate template = localTemplateLogic.fetchTemplate(templateId);
        Long templateItemId = evalViewParams.templateItemId == null ? null : evalViewParams.templateItemId;
        boolean showTemplateItemOnly = templateItemId != null;
        
        List<EvalTemplateItem> itemList = localTemplateLogic.fetchTemplateItems(templateId);
        
        if (showTemplateItemOnly){
        	//we are requesting one template item for use by UI JS renderer EVALSYS-878
        	EvalTemplateItem templateItem = null;
        	for (int i=0; i<itemList.size(); i++) {
                EvalTemplateItem templateItemInitemList = (EvalTemplateItem) itemList.get(i);
                if (templateItemInitemList.getId().equals(templateItemId)) {
                	templateItem = templateItemInitemList;
                }
            }
        	if( templateItem != null){
	        	itemList = TemplateItemUtils.getChildItems(itemList, templateItemId);
	        	itemList.add(templateItem);   
        	}else{
        		throw new IllegalArgumentException("A template id AND template item id needs to be provided.");
        	}
        }

        List<EvalTemplateItem> templateItemsList = TemplateItemUtils.getNonChildItems(itemList);

        if (!showTemplateItemOnly){
	        /*
	         * top links here
	         */
        	navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());
	        // begin page rendering
	
	        UIOutput.make(tofill, "site-id", commonLogic.getContentCollectionId(commonLogic.getCurrentEvalGroup()));
	        UIMessage.make(tofill, "modify-template-title", "modifytemplate.page.title");
	
	        UIInternalLink.make(tofill, "preview_eval_link", UIMessage.make("modifytemplate.preview.eval.link"),
	                new EvalViewParameters(PreviewEvalProducer.VIEW_ID, null, templateId))
	                .decorate( new UITooltipDecorator( UIMessage.make("modifytemplate.preview.eval.link.title") ) );
	
	        UIMessage.make(tofill, "preview-eval-desc",	"modifytemplate.preview.eval.desc");
	
	        UILink.make(tofill, "preview-template-direct-link", UIMessage.make("general.direct.link"), 
	                commonLogic.getEntityURL(template) )
	                .decorate( new UITooltipDecorator( UIMessage.make("general.direct.link.title") ) );
	
	        // get form to submit the type of item to create to the correct view
	        UIMessage.make(tofill, "add-item-note", "modifytemplate.add.item.note");
	
	        // create the choices for the pulldown
	        ArrayList<ViewParameters> templateItemVPList = new ArrayList<>();
	        ArrayList<String> templateItemLabelList = new ArrayList<>();
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
	
	        EvalUser TempOwner = commonLogic.getEvalUserById( template.getOwner() );
	        UIOutput.make(tofill, "template-owner", TempOwner.displayName );
	
	
	        addItemControlRenderer.renderControl(tofill, "add-item-control:", 
	                templateItemVPList.toArray(new ViewParameters[templateItemVPList.size()]), 
	                templateItemLabelList.toArray(new String[templateItemLabelList.size()]), 
	                UIMessage.make("modifytemplate.add.item.button"), templateId);
	
	        if (templateItemsList.isEmpty()) {
	            UIMessage.make(tofill, "begin-eval-dummylink", "modifytemplate.begin.eval.link");
	        } else {
	            UIInternalLink evalLink = UIInternalLink.make(tofill, "begin_eval_link", UIMessage.make("modifytemplate.begin.eval.link"), 
	                    new EvalViewParameters(EvaluationCreateProducer.VIEW_ID, null, templateId));
	            evalLink.decorators = new DecoratorList( new UITooltipDecorator(UIMessage.make("modifytemplate.begin.eval.link.title")));
	        }
	
	        // TODO - this should be the actual level and not some made up string
	        String currentLevel = "Current";
	        UIMessage.make(tofill, "level-header-level", "modifytemplate.level.header.level", 
	                new String[] {currentLevel});			
	        UIOutput.make(tofill, "level-header-number", Integer.toString(templateItemsList.size()) );			
	        UIMessage.make(tofill, "level-header-items", "modifytemplate.level.header.items");			
	
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
        }


        UIForm form2 = UIForm.make(tofill, "modifyFormRows");
        if (!showTemplateItemOnly){
		    UICommand.make(form2, "hiddenBtn");
		    form2.parameters.add(new UIELBinding("#{templateBBean.templateId}", templateId));
		
		    UIMessage revertOrderButton = UIMessage.make(form2, "revertOrderButton", "modifytemplate.button.revert.order");
		    revertOrderButton.decorators = new DecoratorList( new UITooltipDecorator( UIMessage.make("modifytemplate.button.revert.order.title") ) );
		    UICommand saveReorderButton = UICommand.make(form2, "saveReorderButton", 
		            UIMessage.make("modifytemplate.button.save.order"), "#{templateBBean.saveReorder}");
		    saveReorderButton.parameters.add(new UIELBinding("#{templateBBean.templateId}", templateId));
		    saveReorderButton.decorators = new DecoratorList( new UITooltipDecorator( UIMessage.make("modifytemplate.button.save.order.title") ) );
		
		    UIMessage.make(form2, "orderingInstructions", "modifytemplate.instructions.reorder");
        }

        String sCurItemNum = null;
        int blockChildNum = 0;

        if ((templateItemsList != null) && (templateItemsList.size() > 0)) {

            String templateItemOTPBinding;
            String templateItemOTP;

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
                
                // Add item type and id to item tag only if this is a block item
                if ( templateItem.getItem().getClassification().equals(EvalConstants.ITEM_TYPE_BLOCK_PARENT) ){
                	itemBranch.decorate( new UIFreeAttributeDecorator("name", EvalConstants.ITEM_TYPE_BLOCK_PARENT.toLowerCase() + "-" +templateItem.getId() ) );
                }
                
                // hidden item num
                UIInput.make(itemBranch, "hidden-item-num", templateItemOTP + "displayOrder", sCurItemNum);
                UIOutput.make(itemBranch, "template-item-id", templateItem.getId() + "");
                if (templateItem.getItem().getClassification() != null){
                	UIOutput.make(itemBranch, "item-classification", templateItem.getItem().getClassification());
                }

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
                UIMessage.make(itemBranch, "item-classification-label", itemLabelKey);

                if (templateItem.getScaleDisplaySetting() != null) {
                    String scaleDisplaySettingLabel = " - " + templateItem.getScaleDisplaySetting();
                    UIOutput.make(itemBranch, "scale-display", scaleDisplaySettingLabel);
                }
                
                if (templateItem.getCategory() != null && ! EvalConstants.ITEM_CATEGORY_COURSE.equals(templateItem.getCategory())){
                	String[] category = new String[] { messageLocator.getMessage( RenderingUtils.getCategoryLabelKey(templateItem.getCategory()) ) };
                	UIMessage.make(itemBranch, "item-category", "modifytemplate.item.category.title", category)
                		.decorate(new UITooltipDecorator(UIMessage.make("modifytemplate.item.category.tooltip", category)));
                }

                UIInternalLink.make(itemBranch, "preview-row-item",
                        new ItemViewParameters(PreviewItemProducer.VIEW_ID, (Long) null, templateItem.getId()) )
                        .decorate(new UITooltipDecorator(UIMessage.make("modifytemplate.item.preview")));

                if ((templateItem.getBlockParent() != null) && (templateItem.getBlockParent() == true)) {
                    // if it is a block item
                    BlockIdsParameters target = new BlockIdsParameters(ModifyBlockProducer.VIEW_ID, templateId, templateItem.getId().toString());
                    UIInternalLink.make(itemBranch, "modify-row-item", target)
                    .decorate(new UITooltipDecorator(UIMessage.make("modifytemplate.item.edit")));
                } else {
                    //it is a non-block item
                    ViewParameters target = new ItemViewParameters(ModifyItemProducer.VIEW_ID, 
                            templateItem.getItem().getClassification(), templateId, templateItem.getId());
                    UIInternalLink.make(itemBranch, "modify-row-item", target)
                    	.decorate(new UITooltipDecorator(UIMessage.make("modifytemplate.item.edit")));
                }

                if(TemplateItemUtils.isBlockParent(templateItem)){
                    UIInternalLink unblockItem = UIInternalLink.make(itemBranch,	"remove-row-item-unblock", UIMessage.make("modifytemplate.group.ungroup"),
                            new ItemViewParameters(RemoveItemProducer.VIEW_ID, (Long)null, templateItem.getId(), templateId) );
                    unblockItem.decorate(new UIFreeAttributeDecorator("templateItemId", templateItem.getId().toString()));
                    unblockItem.decorate(new UIFreeAttributeDecorator("templateId", templateId.toString()));
                    unblockItem.decorate(new UIFreeAttributeDecorator("OTP", templateItemOTPBinding));
                }
                else{
                    UIInternalLink removeItem = UIInternalLink.make(itemBranch,	"remove-row-item",
                            new ItemViewParameters(RemoveItemProducer.VIEW_ID, (Long)null, templateItem.getId(), templateId) );
                    removeItem.decorate(new UIFreeAttributeDecorator("templateItemId", templateItem.getId().toString()));
                    removeItem.decorate(new UIFreeAttributeDecorator("templateId", templateId.toString()));
                    removeItem.decorate(new UIFreeAttributeDecorator("OTP", templateItemOTPBinding));
                    removeItem.decorate(new UITooltipDecorator(UIMessage.make("modifytemplate.item.delete")));
                }
                
                // SECOND LINE

                UISelect orderPulldown = UISelect.make(itemBranch, "item-select", itemNumArr, templateItemOTP + "displayOrder", Integer.toString(i + 1));
                orderPulldown.decorators = new DecoratorList( new UITooltipDecorator( UIMessage.make("modifytemplate.select.order.title") ) );

                String formattedText = FormattedText.convertFormattedTextToPlaintext(templateItem.getItem().getItemText());

                UIBranchContainer branchText = UIBranchContainer.make(itemBranch, "item-text:");
                UIBranchContainer branchTextHidden = UIBranchContainer.make(itemBranch, "item-text-hidden:");
                UIVerbatim itemText;
                if(formattedText.length() < 150){
                    itemText = UIVerbatim.make(branchText, "item-text-short", formattedText);
                }else{
                    itemText = UIVerbatim.make(branchText, "item-text-short", formattedText.substring(0, 150));
                    UIOutput.make(branchText, "item-text-control");
                    UIVerbatim.make(branchTextHidden, "item-text-long", formattedText);
                    UIOutput.make(branchTextHidden, "item-text-hidden-control");
                }

                if(TemplateItemUtils.isBlockParent(templateItem)){
                    itemText.decorators = new DecoratorList(new UIStyleDecorator("itemBlockRow"));
                    UIOutput.make(branchText, "item-text-block");
                }

                /* Hierarchy Messages 
                 * Only Display these if they are enabled in the preferences.
                 */
                Boolean showHierarchy = (Boolean) evalSettings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
                if ( showHierarchy ) {
                    /* Don't show the Node Id icon if it's a top level item */
                    if (!templateItem.getHierarchyLevel().equals(EvalConstants.HIERARCHY_LEVEL_TOP)) {
                        EvalHierarchyNode curnode = hierarchyLogic.getNodeById(templateItem.getHierarchyNodeId());
                        UILink.make(itemBranch, "item-hierarchy")
                        	.decorate( new UITooltipDecorator( messageLocator.getMessage("modifytemplate.item.hierarchy.nodeid.title") + curnode.title ));
                    }
                }
                /**
                 * UMD Specific
                 * changed wording EVALSYS-850
                 */
                Boolean useResultsSharing = (Boolean) evalSettings.get(EvalSettings.ITEM_USE_RESULTS_SHARING);
                if ( useResultsSharing ) {
                    // only show results sharing if it is being used
                    String resultsSharingMessage = "unknown.caps";
                    if ( EvalConstants.SHARING_PUBLIC.equals(templateItem.getResultsSharing()) ) {
                        resultsSharingMessage = "general.public";
                    } else if ( EvalConstants.SHARING_PRIVATE.equals(templateItem.getResultsSharing()) ) {
                        resultsSharingMessage = "general.private";
                    }  else if ( EvalConstants.SHARING_ADMIN.equals(templateItem.getResultsSharing()) ) {
                        resultsSharingMessage = "modifyitem.results.sharing.admin.short";
                    } else if ( EvalConstants.SHARING_STUDENT.equals(templateItem.getResultsSharing()) ) {
                        resultsSharingMessage = "modifyitem.results.sharing.student";
                    }  else if ( EvalConstants.SHARING_BOTH.equals(templateItem.getResultsSharing()) ) {
                        resultsSharingMessage = "modifyitem.results.sharing.both";
                    }
                    UIMessage.make(itemBranch, "item-results-sharing", resultsSharingMessage);
                }

                if ( EvalConstants.ITEM_TYPE_SCALED.equals(templateItem.getItem().getClassification()) &&
                        templateItem.getItem().getScale() != null ) {
                    // only show the scale type of this is a scaled item
                    UIMessage.make(itemBranch, "item-scale-type-title", "modifytemplate.item.scale.type.title");
                    UIOutput.make(itemBranch, "scale-type", templateItem.getItem().getScale().getTitle());
                }

                // display item options
                boolean showOptions = false;
                if ( templateItem.getUsesNA() != null 
                        && templateItem.getUsesNA() ) {
                    UIMessage.make(itemBranch, "item-na-enabled", "modifytemplate.item.na.note");
                    showOptions = true;
                }

                if ( templateItem.getUsesComment() != null 
                        && templateItem.getUsesComment() ) {
                    UIMessage.make(itemBranch, "item-comment-enabled", "modifytemplate.item.comment.note");
                    showOptions = true;
                }

                if (showOptions) {
                    UIMessage.make(itemBranch, "item-options", "modifytemplate.item.options");               
                }

                // block child items
                if ( TemplateItemUtils.isBlockParent(templateItem) ) {
                    List<EvalTemplateItem> childList = TemplateItemUtils.getChildItems(itemList, templateItem.getId());
                    if (childList.size() > 0) {
                        UIBranchContainer blockChildren = UIBranchContainer.make(itemBranch, "block-children:", Integer.toString(blockChildNum));
                        UIMessage.make(itemBranch, "modifyblock-items-list-instructions",
                        "modifyblock.page.instructions");
                        blockChildNum++;
                        int orderNo = 0;

                        // iterate through block children for the current block parent and emit each child item
                        for (int j = 0; j < childList.size(); j++) {
                            EvalTemplateItem child = (EvalTemplateItem) childList.get(j);
                            emitItem(itemBranch, child, orderNo + 1, templateId, templateItemOTPBinding);
                            orderNo++;
                        }


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

        if (!showTemplateItemOnly){
	        //this outputs the total number of rows
	        UIVerbatim.make(tofill, "total-rows", (sCurItemNum + 1));
	
	        // the create block form
	        UIForm blockForm = UIForm.make(tofill, "createBlockForm",
	                new BlockIdsParameters(ModifyBlockProducer.VIEW_ID, templateId, null));
	        UICommand createBlock = UICommand.make(blockForm, "createBlockBtn", UIMessage.make("modifytemplate.button.createblock") );
	        createBlock.decorators = new DecoratorList( new UITooltipDecorator( UIMessage.make("modifytemplate.button.createblock.title") ) );
	
	        UIMessage.make(form2, "blockInstructions", "modifytemplate.instructions.block");
        }
    }

    /**
     * @param tofill
     * @param templateItem
     * @param index
     * @param templateItemOTPBinding 
     * @param templateId 
     */
    private void emitItem(UIContainer tofill, EvalTemplateItem templateItem, int index, Long templateId, String templateItemOTPBinding ) {
        UIBranchContainer radiobranch = UIBranchContainer.make(tofill,
                "itemRowBlock:", templateItem.getId().toString()); //$NON-NLS-1$
        UIOutput.make(radiobranch, "hidden-item-id", templateItem.getId().toString());
        UIOutput.make(radiobranch, "item-block-num", Integer.toString(index));
        UIVerbatim.make(radiobranch, "item-block-text", FormattedText.convertFormattedTextToPlaintext(templateItem.getItem().getItemText()));
        UIInternalLink removeChildItem = UIInternalLink.make(radiobranch,	"child-remove-item", 
                new ItemViewParameters(RemoveItemProducer.VIEW_ID, (Long)null, templateItem.getId(), templateId) );
        removeChildItem.decorate(new UIFreeAttributeDecorator("templateItemId", templateItem.getId().toString()));
        removeChildItem.decorate(new UIFreeAttributeDecorator("templateId", templateId.toString()));
        removeChildItem.decorate(new UIFreeAttributeDecorator("OTP", templateItemOTPBinding));
        removeChildItem.decorate(new UITooltipDecorator(UIMessage.make("modifytemplate.item.delete")));

        UIInternalLink.make(radiobranch,	"child-ungroup-item", UIMessage.make("modifytemplate.group.ungroup"),
                new ItemViewParameters(RemoveItemProducer.VIEW_ID, (Long)null, templateItem.getId(), templateId) );
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new TemplateViewParameters();
    }

}
