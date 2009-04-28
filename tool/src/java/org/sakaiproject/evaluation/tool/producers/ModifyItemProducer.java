/******************************************************************************
 * ModifyItemProducer.java - created by aaronz on 20 May 2007
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

import static org.sakaiproject.evaluation.utils.EvalUtils.safeBool;

import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.locators.ItemBeanWBL;
import org.sakaiproject.evaluation.tool.locators.TemplateItemWBL;
import org.sakaiproject.evaluation.tool.renderers.HierarchyNodeSelectorRenderer;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;
import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;
import org.sakaiproject.evaluation.utils.ArrayUtils;

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
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.evolvers.BoundedDynamicListInputEvolver;
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
@SuppressWarnings("deprecation")
public class ModifyItemProducer implements ViewComponentProducer, ViewParamsReporter, ActionResultInterceptor {

    public static final String VIEW_ID = "modify_item";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
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
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
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
            if (!((Boolean)settings.get(EvalSettings.DISABLE_ITEM_BANK))) {
                UIInternalLink.make(tofill, "control-items-link",
                        UIMessage.make("controlitems.page.title"), 
                        new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
            }
        } else {
            throw new SecurityException("User attempted to access " + 
                    VIEW_ID + " when they are not allowed");
        }

        if (beginEvaluation) {
            UIInternalLink.make(tofill, "control-evaluations-link",
                    UIMessage.make("controlevaluations.page.title"),
                    new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
        }

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

        EvalScale currentScale = null; // this is the current scale (if there is one)

        /* these keep track of whether items are locked, we are not tracking TIs because 
         * the user should not be able to get here if the template is locked, if they did then 
         * they cheated so they can get an exception, we don't track scales since if the item
         * is locked then the scale it also so neither one will be saved
         */
        boolean itemLocked = false;

        String scaleDisplaySetting = null; // the scale display setting for the item/TI
        String displayRows = null; // the number of rows to display for the text area
        Boolean usesNA = null; // whether or not the item uses the N/A option
        Boolean usesComment = null; // whether or not the item uses the comment option
        Boolean compulsory = null; //whether or no this question must be answered
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
            EvalUser owner = commonLogic.getEvalUserById( currentUserId );
            itemOwnerName = owner.displayName;
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
            EvalItem item = authoringService.getItemById(itemId);
            if (item == null) {
                throw new IllegalArgumentException("Invalid item id passed in by VP: " + itemId);
            }
            // set the common settings from the item
            scaleDisplaySetting = item.getScaleDisplaySetting();
            displayRows = item.getDisplayRows() != null ? item.getDisplayRows().toString() : null;
            usesNA = item.getUsesNA();
            usesComment = item.getUsesComment();
            compulsory = safeBool(item.isCompulsory());
            // if this is locked then we should probably be failing at this point
            itemLocked = item.getLocked() != null ? item.getLocked() : itemLocked;
            if (item.getScale() != null) {
                currentScale = item.getScale();
                scaleId = currentScale.getId();
            }

            EvalUser owner = commonLogic.getEvalUserById( item.getOwner() );
            itemOwnerName = owner.displayName;
            itemClassification = item.getClassification();
            itemOTP = "itemWBL." + itemId + ".";
            commonDisplayOTP = itemOTP;
        } else {
            // templateItemId is not null so we are modifying an existing template item
            EvalTemplateItem templateItem = authoringService.getTemplateItemById(templateItemId);
            if (templateItem == null) {
                throw new IllegalArgumentException("Invalid template item id passed in by VP: " + templateItemId);
            }
            // set the common settings from the TI
            scaleDisplaySetting = templateItem.getScaleDisplaySetting();
            displayRows = templateItem.getDisplayRows() != null ? templateItem.getDisplayRows().toString() : null;
            usesNA = templateItem.getUsesNA();
            usesComment = templateItem.getUsesComment();
            compulsory = safeBool(templateItem.isCompulsory());
            itemLocked = templateItem.getItem().getLocked() != null ? templateItem.getItem().getLocked() : itemLocked;
            if (templateItem.getItem().getScale() != null) {
                currentScale = templateItem.getItem().getScale();
                scaleId = currentScale.getId();
            }

            EvalUser owner = commonLogic.getEvalUserById( templateItem.getItem().getOwner() );
            itemOwnerName = owner.displayName;
            itemClassification = templateItem.getItem().getClassification();
            templateItemOTP = "templateItemWBL." + templateItemId + ".";
            itemOTP = templateItemOTP + "item.";
            commonDisplayOTP = templateItemOTP;
        }

        // now we begin with the rendering logic

        // display the breadcrumb bar
        if (templateId != null) {
            // creating template item
            UIInternalLink.make(tofill, "control-items-breadcrumb",
                    UIMessage.make("controltemplates.page.title"), 
                    new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));
            UIInternalLink.make(tofill, "modify-template-items",
                    UIMessage.make("modifytemplate.page.title"), 
                    new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, templateId));
            UIMessage.make(tofill, "cancel-button", "general.cancel.button");
        } else {
            UIInternalLink.make(tofill, "control-items-breadcrumb",
                    UIMessage.make("controlitems.page.title"), 
                    new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
            UIMessage.make(tofill, "cancel-button", "general.cancel.button");
        }
        UIMessage.make(tofill, "item-header", "modifyitem.item.header");

        // display item information
        String itemLabelKey = EvalToolConstants.UNKNOWN_KEY;
        for (int i = 0; i < EvalToolConstants.ITEM_CLASSIFICATION_VALUES.length; i++) {
            if (itemClassification.equals(EvalToolConstants.ITEM_CLASSIFICATION_VALUES[i])) {
                itemLabelKey = EvalToolConstants.ITEM_CLASSIFICATION_LABELS_PROPS[i];
                break;
            }
        }
        UIMessage.make(tofill, "item-classification", itemLabelKey);
        UIMessage.make(tofill, "added-by-item-owner", "modifyitem.item.added.by.owner", new Object[] {itemOwnerName});

        // show links if this item/templateItem exists
        if (templateItemId != null || itemId != null) {
            UIInternalLink.make(tofill, "item-preview-link", UIMessage.make("general.command.preview"), 
                    new ItemViewParameters(PreviewItemProducer.VIEW_ID, itemId, templateItemId) );
            if ( (itemId != null && authoringService.canRemoveItem(currentUserId, itemId)) || 
                    templateItemId != null && authoringService.canControlTemplateItem(currentUserId, templateItemId) ) {
                // item or templateItem is removable
                UIInternalLink.make(tofill, "item-remove-link", UIMessage.make("general.command.delete"), 
                        new ItemViewParameters(RemoveItemProducer.VIEW_ID, itemId, templateItemId));
            }
        }

        UIMessage.make(form, "item-text-header", "modifyitem.item.text.header");
        UIMessage.make(form, "item-text-instruction", "modifyitem.item.text.instruction");

        UIInput itemText = UIInput.make(form, "item-text", itemOTP + "itemText");
        if (itemLocked) {
            itemText.willinput = false;
            itemText.decorate(new UIDisabledDecorator());
        }

        if (EvalConstants.ITEM_TYPE_SCALED.equals(itemClassification)) {
            UIBranchContainer showItemScale = UIBranchContainer.make(form, "show-item-scale:");
            if (! itemLocked) {
                // SCALED items need to choose a scale
                List<EvalScale> scales = authoringService.getScalesForUser(currentUserId, null);
                if (scales.isEmpty()) {
                    throw new IllegalStateException("There are no scales available in the system for creating scaled items, please create at least one scale");
                }
                // add in the current scale to ensure it is a valid choice if it is missing - EVALSYS-716
                if (currentScale != null 
                        && currentScale.getId() != null
                        && ! scales.contains(currentScale)) {
                    scales.add(0, currentScale); // might want to copy the list and make a new one here
                }
                String[] scaleValues = ScaledUtils.getScaleValues(scales);
                UISelect scaleList = UISelect.make(showItemScale, "item-scale-list", 
                        scaleValues, 
                        ScaledUtils.getScaleLabels(scales), 
                        itemOTP + "scale.id",
                        scaleId != null ? scaleId.toString() : scaleValues[0]);
                scaleList.selection.mustapply = true; // this is required to ensure that the value gets passed even if it is not changed
                scaleList.selection.darreshaper = new ELReference("#{id-defunnel}");
            } else {
                // just display the name of the current scale without a select box
                UIOutput.make(showItemScale, "item-scale-current", ScaledUtils.makeScaleText(currentScale, 0) );
            }
        } else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemClassification) ||
                EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemClassification) ) {
            // MC/MA items need to create choices
            String scaleOTP = itemOTP + "scale."; // + (scaleId != null ? scaleId.toString() : ScaleBeanLocator.NEW_1) + ".";
            UIBranchContainer showItemChoices = UIBranchContainer.make(form, "show-item-choices:");
            boundedDynamicListInputEvolver.setLabels(
                    UIMessage.make("modifyscale.remove.scale.option.button"), 
                    UIMessage.make("modifyscale.add.scale.option.button"));
            boundedDynamicListInputEvolver.setMinimumLength(2);
            boundedDynamicListInputEvolver.setMaximumLength(40);

            UIInputMany modifypoints = UIInputMany.make(showItemChoices, 
                    "modify-scale-points:", scaleOTP + "options",
                    (scaleId == null ? EvalToolConstants.defaultInitialScaleValues : null) );
            boundedDynamicListInputEvolver.evolve(modifypoints);
            // force the scale to bind to adhoc mode
            form.parameters.add( new UIELBinding(scaleOTP + "mode", EvalConstants.SCALE_MODE_ADHOC) );
        }

        if (userAdmin && templateId == null) {
            // only show the item sharing options if the user is an admin AND we are modifying the item only
            UIBranchContainer showItemSharing = UIBranchContainer.make(form, "show-item-sharing:");
            UIMessage.make(showItemSharing, "item-sharing-header", "modifyitem.item.sharing.header");
            UISelect.make(showItemSharing, "item-sharing-list", 
                    EvalToolConstants.SHARING_VALUES, 
                    EvalToolConstants.SHARING_LABELS_PROPS, 
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
            UIInput.make(showItemExpert, "expert-desc", itemOTP + "expertDescription");
        }

        // Check to see if should show ITEM display hints
        boolean showItemDisplayHints = true;
        Boolean useCourseCategoryOnly = (Boolean) settings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY);
        if (useCourseCategoryOnly) {
            // bind explicitly to course/group category item and do not show the option
            form.parameters.add(
                    new UIELBinding(commonDisplayOTP + "category", EvalConstants.ITEM_CATEGORY_COURSE));
            // no display hints for header items if we can only use course category
            if (EvalConstants.ITEM_TYPE_HEADER.equals(itemClassification)) {
                showItemDisplayHints = false;
            }
        }

        // Show ITEM display hints
        if (showItemDisplayHints) {
            UIBranchContainer itemDisplayHintsBranch = UIBranchContainer.make(form, "showItemDisplayHints:");
            // default header/instructions are for required
            String headerKey = "modifyitem.display.header";
            String instructionKey = "modifyitem.display.instruction";
            if (templateId == null) {
                // call these hints if we are modifying item only
                headerKey = "modifyitem.display.hint.header";
                instructionKey = "modifyitem.display.hint.instruction";
            }
            UIMessage.make(itemDisplayHintsBranch, "item-display-hint-header", headerKey);
            UIMessage.make(itemDisplayHintsBranch, "item-display-hint-instruction", instructionKey);

            if (EvalConstants.ITEM_TYPE_SCALED.equals(itemClassification)) {
                renderScaleDisplaySelect(form, commonDisplayOTP, scaleDisplaySetting, 
                        EvalToolConstants.SCALE_DISPLAY_SETTING_VALUES, 
                        EvalToolConstants.SCALE_DISPLAY_SETTING_LABELS_PROPS);
            } else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemClassification) ||
                    EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemClassification) ) {
                renderScaleDisplaySelect(form, commonDisplayOTP, scaleDisplaySetting, 
                        EvalToolConstants.CHOICES_DISPLAY_SETTING_VALUES, 
                        EvalToolConstants.CHOICES_DISPLAY_SETTING_LABELS_PROPS);
            }

            if (EvalConstants.ITEM_TYPE_TEXT.equals(itemClassification)) {
                UIBranchContainer showResponseSize = UIBranchContainer.make(itemDisplayHintsBranch, "show-response-size:");
                UIMessage.make(showResponseSize, "item-response-size-header", "modifyitem.item.response.size.header");
                UISelect.make(showResponseSize, "item-response-size-list", 
                        EvalToolConstants.RESPONSE_SIZE_VALUES, 
                        EvalToolConstants.RESPONSE_SIZE_LABELS_PROPS,
                        commonDisplayOTP + "displayRows",
                        displayRows ).setMessageKeys();
            }

            if (! EvalConstants.ITEM_TYPE_HEADER.equals(itemClassification)) {
                // na option for all non-header items
                Boolean naAllowed = (Boolean) settings.get(EvalSettings.ENABLE_NOT_AVAILABLE);
                if (naAllowed) {
                    UIBranchContainer showNA = UIBranchContainer.make(itemDisplayHintsBranch, "showNA:");
                    UIBoundBoolean bb = UIBoundBoolean.make(showNA, "item-na", commonDisplayOTP + "usesNA", usesNA);
                    UIMessage.make(showNA,"item-na-header", "modifyitem.item.na.header")
                    .decorate( new UILabelTargetDecorator(bb) );
                }

                if (! EvalConstants.ITEM_TYPE_TEXT.equals(itemClassification)) {
                    // comments options for all non-text and non-header items
                    Boolean commentAllowed = (Boolean) settings.get(EvalSettings.ENABLE_ITEM_COMMENTS);
                    if (commentAllowed) {
                        UIBranchContainer showComment = UIBranchContainer.make(itemDisplayHintsBranch, "showItemComment:");
                        UIBoundBoolean bb = UIBoundBoolean.make(showComment, "item-comment", commonDisplayOTP + "usesComment", usesComment);
                        UIMessage.make(showComment,"item-comment-header", "modifyitem.item.comment.header")
                        .decorate( new UILabelTargetDecorator(bb) );
                    }
                }

                if (! EvalConstants.ITEM_TYPE_TEXT.equals(itemClassification)) {
                    //compulsory 
                    Boolean selectOptionsCompulsory = true;
                    if (selectOptionsCompulsory) {
                        UIBranchContainer showComp = UIBranchContainer.make(itemDisplayHintsBranch, "showItemCompulsory:");
                        UIBoundBoolean bb = UIBoundBoolean.make(showComp, "item-compulsory", commonDisplayOTP + "compulsory", compulsory);
                        UIMessage.make(showComp,"item-compulsory-header", "modifyitem.item.compulsory.header")
                        .decorate( new UILabelTargetDecorator(bb) );
                    }
                }
            }

            if (! useCourseCategoryOnly) {
                // show all category choices so the user can choose, default is course category
                UIBranchContainer showItemCategory = UIBranchContainer.make(itemDisplayHintsBranch, "showItemCategory:");
                UIMessage.make(showItemCategory, "item-category-header", "modifyitem.item.category.header");

                String[] categoryValues = EvalToolConstants.ITEM_CATEGORY_VALUES; 
                String[] categoryLabels = EvalToolConstants.ITEM_CATEGORY_LABELS_PROPS;
                // add in the TA category if enabled
                Boolean enableTA = (Boolean) settings.get(EvalSettings.ENABLE_ASSISTANT_CATEGORY);
                if ( enableTA.booleanValue() ) {
                    categoryValues = ArrayUtils.appendArray(categoryValues, EvalToolConstants.ITEM_CATEGORY_ASSISTANT);
                    categoryLabels = ArrayUtils.appendArray(categoryLabels, EvalToolConstants.ITEM_CATEGORY_ASSISTANT_LABEL);
                }
                UISelect radios = UISelect.make(showItemCategory, "item-category-list", 
                        categoryValues, categoryLabels,
                        commonDisplayOTP + "category").setMessageKeys();
                for (int i = 0; i < categoryValues.length; i++) {
                    UIBranchContainer radioBranch = UIBranchContainer.make(showItemCategory, "item-category-branch:", i+"");
                    UISelectChoice choice = UISelectChoice.make(radioBranch, "item-category-radio", radios.getFullID(), i);
                    UISelectLabel.make(radioBranch, "item-category-label", radios.getFullID(), i)
                    .decorate( new UILabelTargetDecorator(choice) );
                }
            }
        }

        if (templateId != null) {
            // ONLY DO THESE if we are working with a template and TemplateItem

            // hierarchy node selector control
            Boolean showHierarchyOptions = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
            if (showHierarchyOptions) {
                hierarchyNodeSelectorRenderer.renderHierarchyNodeSelector(form, "hierarchyNodeSelector:", templateItemOTP + "hierarchyNodeId", null);
            }

            /*
             * If the system setting (admin setting) for "EvalSettings.ITEM_USE_RESULTS_SHARING" is set as true then all
             * items default to "Public" and users can select Public or Private.
             * If it is set to false then it is forced to Private
             */
            Boolean useResultSharing = (Boolean) settings.get(EvalSettings.ITEM_USE_RESULTS_SHARING);
            if (useResultSharing) {
                // Means show both options (public & private)
                UIBranchContainer showItemResultSharing = UIBranchContainer.make(form, "showItemResultSharing:");
                UIMessage.make(showItemResultSharing, "item-results-sharing-header", "modifyitem.results.sharing.header");
                UIMessage.make(showItemResultSharing, "item-results-sharing-PU", "general.public");
                UIMessage.make(showItemResultSharing, "item-results-sharing-PR", "general.private");
                // Radio Buttons for "Result Sharing"
                String[] resultSharingList = { "general.public", "general.private" };
                UISelect radios = UISelect.make(showItemResultSharing, "item_results_sharing", EvalToolConstants.ITEM_RESULTS_SHARING_VALUES,
                        resultSharingList, templateItemOTP + "resultsSharing", null);

                String selectID = radios.getFullID();
                UISelectChoice.make(showItemResultSharing, "item_results_sharing_PU", selectID, 0);
                UISelectChoice.make(showItemResultSharing, "item_results_sharing_PR", selectID, 1);
            } else {
                // false so all questions are private by default (set the binding)
                form.parameters.add( 
                        new UIELBinding(templateItemOTP + "resultsSharing",
                                EvalToolConstants.ITEM_RESULTS_SHARING_VALUES[0]) );
            }
        }



        String saveBinding = null;
        if (templateId == null) {
            // only saving an item
            if (! itemLocked) {
                saveBinding = "#{templateBBean.saveItemAction}";
            }
        } else {
            if (! itemLocked) {
                // saving template item and item
                saveBinding = "#{templateBBean.saveBothAction}";
            } else {
                // only saving template item
                saveBinding = "#{templateBBean.saveTemplateItemAction}";
            }
        }
        if (saveBinding != null) {
            UICommand.make(form, "save-item-action", UIMessage.make("modifyitem.save.button"), saveBinding);
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
