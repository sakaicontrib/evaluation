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

import static org.sakaiproject.evaluation.utils.EvalUtils.safeBool;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
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
import uk.org.ponder.rsf.evolvers.BoundedDynamicListInputEvolver;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
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
public class ModifyItemProducer extends EvalCommonProducer implements ViewParamsReporter, ActionResultInterceptor {

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

    private TextInputEvolver richTextEvolver;
    public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
        this.richTextEvolver = richTextEvolver;
    }
    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        // create the form to allow submission of this item
        UIForm form = UIForm.make(tofill, "item-form");

        // variables and basic logic for determining what we are doing on this view
        ItemViewParameters ivp = (ItemViewParameters) viewparams;
        Long templateId = ivp.templateId; // if null then assume we are creating items for the item bank, else this is for a template
        Long itemId = ivp.itemId; // if null then we are creating a new item, else modifying existing item
        Long templateItemId = ivp.templateItemId; // if null then we are working with an item only, else we are working with a template item so we will get the item from this
        String itemClassification = ivp.itemClassification; // must be set if creating a new item
        Long groupItemId = ivp.groupItemId; // if set and item is NEW, inherit parent settings and save item into parent group. New feature in EVALSYS-812

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
        Long itemGroupId = null; // this holds the current eval item group id if there is one - EVALSYS-1026


        Boolean isGrouped = (groupItemId != null && groupItemId == -1l ); //We are working with an existing child item
        Boolean isGroupable = ( ! isGrouped && groupItemId != null);

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

            //>>>
            if ( isGroupable ){         	
                // New child item
                List<EvalTemplateItem> groupedItemList = authoringService.getBlockChildTemplateItemsForBlockParent(groupItemId, false);
                EvalTemplateItem itemClone = groupedItemList.get(1);
                EvalItem item = itemClone.getItem();
                form.parameters.add(
                        new UIELBinding(itemOTP + "sharing", item.getSharing()) );
                form.parameters.add(
                        new UIELBinding(itemOTP + "scale.id", item.getScale().getId()) );
                form.parameters.add(
                        new UIELBinding(itemOTP + "scaleDisplaySetting", item.getScaleDisplaySetting()) );
                form.parameters.add(
                        new UIELBinding(itemOTP + "category", item.getCategory()) );
                form.parameters.add(
                        new UIELBinding(itemOTP + "compulsory", item.isCompulsory()) );
                form.parameters.add(
                        new UIELBinding(itemOTP + "usesComment", item.getUsesComment()) );
                form.parameters.add(
                        new UIELBinding(itemOTP + "usesNA", item.getUsesNA()) );
                form.parameters.add(
                        new UIELBinding(itemOTP + "classification", item.getClassification()) );

                // add group item id
                form.parameters.add(
                        new UIELBinding("templateBBean.groupItemId", groupItemId) );
            }else{
                // add binding for the item classification
                form.parameters.add(
                        new UIELBinding(itemOTP + "classification", itemClassification) );
            }
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
        } else if ( ! isGrouped ){
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
        }else  if (isGrouped){
            // Editing an existing child item
            EvalTemplateItem templateItem = authoringService.getTemplateItemById(templateItemId);
            if (templateItem == null) {
                throw new IllegalArgumentException("Invalid template item id passed in by VP: " + templateItemId);
            }
            EvalUser itemOwner = commonLogic.getEvalUserById( templateItem.getItem().getOwner() );
            itemOwnerName = itemOwner.displayName;
            itemClassification = templateItem.getItem().getClassification();
            templateItemOTP = "templateItemWBL." + templateItemId + ".";
            itemOTP = templateItemOTP + "item.";
            commonDisplayOTP = templateItemOTP;
            compulsory = safeBool(templateItem.isCompulsory());

            form.parameters.add(
                    new UIELBinding(itemOTP + "sharing", templateItem.getItem().getSharing()) );
            form.parameters.add(
                    new UIELBinding(itemOTP + "scale.id", templateItem.getItem().getScale().getId()) );
            form.parameters.add(
                    new UIELBinding(itemOTP + "scaleDisplaySetting", templateItem.getItem().getScaleDisplaySetting()) );
            form.parameters.add(
                    new UIELBinding(itemOTP + "category", templateItem.getCategory()) );
            form.parameters.add(
                    new UIELBinding(itemOTP + "usesComment", templateItem.getUsesComment()) );
            form.parameters.add(
                    new UIELBinding(itemOTP + "usesNA", templateItem.getUsesNA()) );
            form.parameters.add(
                    new UIELBinding(itemOTP + "classification", templateItem.getItem().getClassification()) );
            form.parameters.add(
                    new UIELBinding(itemOTP + "displayRows", templateItem.getDisplayRows() != null ? templateItem.getDisplayRows().toString() : null ) );


        }

        // now we begin with the rendering logic

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

        UIInput itemText = UIInput.make(form, "item-text:", itemOTP + "itemText");
        richTextEvolver.evolveTextInput(itemText);
        if (itemLocked) {
            itemText.willinput = false;
            itemText.decorate(new UIDisabledDecorator());
        }

        if (EvalConstants.ITEM_TYPE_SCALED.equals(itemClassification) && ! isGroupable && ! isGrouped) {
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
                    (scaleId == null ? EvalToolConstants.DEFAULT_INITIAL_SCALE_VALUES : null) );
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
        } else if(! isGroupable){
            // not admin so set the sharing to private by default for now
            form.parameters.add( new UIELBinding(itemOTP + "sharing", EvalConstants.SHARING_PRIVATE) );
        }

        List<EvalItemGroup> itemGroups = authoringService.getAllItemGroups(currentUserId, true);
        UIBranchContainer showItemExpert = UIBranchContainer.make(form, "show-item-expert:");

        if (userAdmin && templateId == null && ! isGroupable) {
            // only show the expert items if the user is an admin AND we are modifying the item only
            //UIBranchContainer showItemExpert = UIBranchContainer.make(form, "show-item-expert:");
            Boolean useExpertItems = (Boolean) settings.get(EvalSettings.USE_EXPERT_ITEMS);
            if (useExpertItems) {                      
                UIMessage.make(showItemExpert, "item-expert-header", "modifyitem.item.expert.header");
                UIMessage.make(showItemExpert, "item-expert-instruction", "modifyitem.item.expert.instruction");
                UIBoundBoolean.make(showItemExpert, "item-expert", itemOTP + "expert", null);

                /*
                 *  EVALSYS-1026
                 *  Creating combo box for expert item groups and matching the item to the 
                 *  item group category or objective
                 */
                //Boolean useExpertItems = (Boolean) settings.get(EvalSettings.USE_EXPERT_ITEMS);
                //if (useExpertItems) {                      
                ArrayList<String> listExpertCat = new ArrayList<>();
                ArrayList<String> listExpertValues = new ArrayList<>();
                listExpertCat.add("None");
                listExpertValues.add("0");
                for (int i = 0; i < itemGroups.size(); i++) {
                    EvalItemGroup eig = (EvalItemGroup) itemGroups.get(i);
                    // loop through all expert items
                    boolean foundItemGroup = false;
                    if (!foundItemGroup) {
                        List<EvalItem> expertItems = authoringService.getItemsInItemGroup(eig.getId(), true);
                        for (int j = 0; j < expertItems.size(); j++) {
                            EvalItem expertItem = (EvalItem) expertItems.get(j);
                            if (Objects.equals( expertItem.getId(), itemId )) {
                                itemGroupId = eig.getId();
                                foundItemGroup = true;
                            }	
                        }
                    }

                    if ( EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(eig.getType())) {
                        listExpertCat.add(eig.getTitle());
                        listExpertValues.add(eig.getId().toString());
                    } else {
                        listExpertCat.add("..." + eig.getTitle());
                        listExpertValues.add(eig.getId().toString());
                    }
                }
                String[] expertValues = listExpertValues.toArray(new String[]{});
                UISelect expertList = UISelect.make(
                        showItemExpert, "item-expert-list", 
                        expertValues, 
                        listExpertCat.toArray(new String[]{}), 
                        itemOTP+"itemGroupId",
                        itemGroupId != null ? itemGroupId.toString() : expertValues[0]);
                expertList.selection.mustapply = true; // this is required to ensure that the value gets passed even if it is not changed            

                UIMessage.make(showItemExpert, "expert-desc-header", "modifyitem.item.expert.desc.header");
                UIMessage.make(showItemExpert, "expert-desc-instruction", "modifyitem.item.expert.desc.instruction");
                UIMessage.make(showItemExpert, "expert-itemgroup-header", "modifyitem.item.expert.itemgroup.header");
                UIInput.make(showItemExpert, "expert-desc", itemOTP + "expertDescription");
            }
        } else {
            // if an expert item, must carry eval item group along with it.
            itemGroupId = new Long(0);
            if (templateItemId != null) {
                EvalTemplateItem templateItem = authoringService.getTemplateItemById(templateItemId);
                for (int i = 0; i < itemGroups.size(); i++) {
                    EvalItemGroup eig = (EvalItemGroup) itemGroups.get(i);
                    // loop through all expert items
                    boolean foundItemGroup = false;
                    if (!foundItemGroup) {
                        List<EvalItem> expertItems = authoringService.getItemsInItemGroup(eig.getId(), true);
                        for (int j = 0; j < expertItems.size(); j++) {
                            EvalItem expertItem = (EvalItem) expertItems.get(j);
                            if (Objects.equals( expertItem.getId(), templateItem.getItem().getId() )) {
                                itemGroupId = eig.getId();
                                foundItemGroup = true;
                            }	
                        }
                    }
                } 
            }
            UIInput.make(showItemExpert, "expertitem-eigId", itemOTP+"itemGroupId", itemGroupId.toString());
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
            if(! isGroupable && ! isGrouped ){
                UIMessage.make(itemDisplayHintsBranch, "item-display-hint-instruction", instructionKey);
            }

            if (EvalConstants.ITEM_TYPE_SCALED.equals(itemClassification)  && ! isGroupable && ! isGrouped ) {
                renderScaleDisplaySelect(form, commonDisplayOTP, scaleDisplaySetting, 
                        EvalToolConstants.SCALE_DISPLAY_SETTING_VALUES, 
                        EvalToolConstants.SCALE_DISPLAY_SETTING_LABELS_PROPS);
            } else if (EvalConstants.ITEM_TYPE_MULTIPLECHOICE.equals(itemClassification) ||
                    EvalConstants.ITEM_TYPE_MULTIPLEANSWER.equals(itemClassification)  && ! isGroupable && ! isGrouped ) {
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
                if (naAllowed && ! isGroupable && ! isGrouped ) {
                    UIBranchContainer showNA = UIBranchContainer.make(itemDisplayHintsBranch, "showNA:");
                    UIBoundBoolean.make(showNA, "item-na", commonDisplayOTP + "usesNA", usesNA);
                    UIMessage.make(showNA,"item-na-header", "modifyitem.item.na.header");
                }

                if (! EvalConstants.ITEM_TYPE_TEXT.equals(itemClassification) && ! isGroupable && ! isGrouped ) {
                    // comments options for all non-text and non-header items
                    Boolean commentAllowed = (Boolean) settings.get(EvalSettings.ENABLE_ITEM_COMMENTS);
                    if (commentAllowed) {
                        UIBranchContainer showComment = UIBranchContainer.make(itemDisplayHintsBranch, "showItemComment:");
                        UIBoundBoolean.make(showComment, "item-comment", commonDisplayOTP + "usesComment", usesComment);
                        UIMessage.make(showComment,"item-comment-header", "modifyitem.item.comment.header");
                    }
                }

                if (! EvalConstants.ITEM_TYPE_TEXT.equals(itemClassification)) {
                    //compulsory 
                    Boolean selectOptionsCompulsory = true;
                    if (selectOptionsCompulsory) {
                        UIBranchContainer showComp = UIBranchContainer.make(itemDisplayHintsBranch, "showItemCompulsory:");
                        UIBoundBoolean.make(showComp, "item-compulsory", commonDisplayOTP + "compulsory", compulsory);
                        UIMessage.make(showComp,"item-compulsory-header", "modifyitem.item.compulsory.header");
                    }
                }
            }

            if (! useCourseCategoryOnly && ! isGroupable && ! isGrouped ) {
                // show all category choices so the user can choose, default is course category
                UIBranchContainer showItemCategory = UIBranchContainer.make(itemDisplayHintsBranch, "showItemCategory:");
                UIMessage.make(showItemCategory, "item-category-header", "modifyitem.item.category.header");

                String[] categoryValues = EvalToolConstants.ITEM_CATEGORY_VALUES; 
                String[] categoryLabels = EvalToolConstants.ITEM_CATEGORY_LABELS_PROPS;
                // add in the TA category if enabled
                Boolean enableTA = (Boolean) settings.get(EvalSettings.ENABLE_ASSISTANT_CATEGORY);
                if ( enableTA ) {
                    categoryValues = ArrayUtils.appendArray(categoryValues, EvalToolConstants.ITEM_CATEGORY_ASSISTANT);
                    categoryLabels = ArrayUtils.appendArray(categoryLabels, EvalToolConstants.ITEM_CATEGORY_ASSISTANT_LABEL);
                }
                UISelect radios = UISelect.make(showItemCategory, "item-category-list", 
                        categoryValues, categoryLabels,
                        commonDisplayOTP + "category").setMessageKeys();
                for (int i = 0; i < categoryValues.length; i++) {
                    UIBranchContainer radioBranch = UIBranchContainer.make(showItemCategory, "item-category-branch:", i+"");
                    UISelectChoice.make(radioBranch, "item-category-radio", radios.getFullID(), i);
                    UISelectLabel.make(radioBranch, "item-category-label", radios.getFullID(), i);
                }
            }
        }

        if (templateId != null) {
            // ONLY DO THESE if we are working with a template and TemplateItem

            // hierarchy node selector control
            Boolean showHierarchyOptions = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
            if (showHierarchyOptions  && ! isGroupable && ! isGrouped ) {
                // commented out until someone can make this interface more robust
                //hierarchyNodeSelectorRenderer.renderHierarchyNodeSelector(form, "hierarchyNodeSelector:", templateItemOTP + "hierarchyNodeId", null);
            }

            /*
             * UMD Specific
             * If the system setting (admin setting) for "EvalSettings.ITEM_USE_RESULTS_SHARING" is set as true then all
             * items default to "Administrative" and users can select Administrative or Student.
             * If it is set to false then it is forced to Administrative
             */
            Boolean useResultSharing = (Boolean) settings.get(EvalSettings.ITEM_USE_RESULTS_SHARING);
            if (useResultSharing) {	// && ! isGroupable && ! isGrouped ) {
                // Means show both options (Administrative & Student)
                UIBranchContainer showItemResultSharing = UIBranchContainer.make(form, "showItemResultSharing:");
                UIMessage.make(showItemResultSharing, "item-results-sharing-header", "modifyitem.results.sharing.header");
                UIMessage.make(showItemResultSharing, "item-results-sharing-AD", "modifyitem.results.sharing.admin");
                UIMessage.make(showItemResultSharing, "item-results-sharing-ST", "modifyitem.results.sharing.student");
                // Radio Buttons for "Result Sharing"
                String[] resultSharingList = { "modifyitem.results.sharing.admin", "modifyitem.results.sharing.student" };
                UISelect radios = UISelect.make(showItemResultSharing, "item_results_sharing", EvalToolConstants.ITEM_RESULTS_SHARING_VALUES,
                        resultSharingList, templateItemOTP + "resultsSharing", null);

                String selectID = radios.getFullID();
                UISelectChoice.make(showItemResultSharing, "item_results_sharing_AD", selectID, 0);
                UISelectChoice.make(showItemResultSharing, "item_results_sharing_ST", selectID, 1);
            } else {
                // false so all questions are private by default (set the binding)
                form.parameters.add( 
                        new UIELBinding(templateItemOTP + "resultsSharing",
                                EvalConstants.SHARING_PUBLIC));
            }

            // hierarchy node selector control
            //           Boolean showHierarchyOptions = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
            //           if (showHierarchyOptions) {
            //               hierarchyNodeSelectorRenderer.renderHierarchyNodeSelector(form, "hierarchyNodeSelector:", templateItemOTP + "hierarchyNodeId", null);
            //           }

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
            } else{
                // only saving template item
                saveBinding = "#{templateBBean.saveTemplateItemAction}";
            }
        }

        if(isGroupable){
            // saving template item straight into a group
            saveBinding = "#{templateBBean.saveTemplateItemToGroupAction}";
        }
        if (saveBinding != null) {
            UICommand.make(form, "save-item-action", UIMessage.make("modifyitem.save.button"), saveBinding);
        }

        UICommand.make( form, "cancel-button", UIMessage.make( "general.cancel.button" ) );
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
        }else{        
            if(actionReturn != null){
                try{
                    Long itemId = Long.parseLong(actionReturn.toString());
                    result.resultingView = new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, ivp.templateId, itemId);
                }catch(NumberFormatException e){
                    if ("success".equals(actionReturn.toString())){
                        result.resultingView = new TemplateViewParameters(ModifyTemplateItemsProducer.VIEW_ID, ivp.templateId);
                    }else{
                        //This is an unexpected return string, possibly an error. So return an error view:
                        result.resultingView = new SimpleViewParameters(MessagesProducer.VIEW_ID);
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new ItemViewParameters();      
    }
}
