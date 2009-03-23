/******************************************************************************
 * ModifyBlockProducer.java - created by fengr@vt.edu on Oct 2, 2006
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.jfree.util.Log;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.locators.TemplateItemWBL;
import org.sakaiproject.evaluation.tool.renderers.HierarchyNodeSelectorRenderer;
import org.sakaiproject.evaluation.tool.viewparams.BlockIdsParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateItemViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.TemplateViewParameters;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;

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
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Page for Create, modify, preview, delete a Block type Item
 * 
 * @author Rui Feng (fengr@vt.edu)
 * @author "Rick Moyer" <rmoyer@umd.edu>
 */
public class ModifyBlockProducer implements ViewComponentProducer, ViewParamsReporter,
NavigationCaseReporter {

    public static final String VIEW_ID = "modify_block";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    private HierarchyNodeSelectorRenderer hierarchyNodeSelectorRenderer;
    public void setHierarchyNodeSelectorRenderer(
            HierarchyNodeSelectorRenderer hierarchyNodeSelectorRenderer) {
        this.hierarchyNodeSelectorRenderer = hierarchyNodeSelectorRenderer;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams,
            ComponentChecker checker) {
        BlockIdsParameters evParameters = (BlockIdsParameters) viewparams;
        Long templateId = evParameters.templateId;

        // this indicate if it is for modify existing Block
        boolean modify = false;
        // this indicate if the passed Ids have the same scale
        // boolean validScaleIds = true;

        boolean validChildsNo = true; // this is to enforce settings of maximun Number of child text
        // in a block

        EvalTemplateItem firstTemplateItem = null;

        // the first items's original displayOrder
        Integer originalDisplayOrder = null;

        //      boolean createFromBlock = false;

        // analyze the string of templateItemIds
        String[] templateItemIds = evParameters.templateItemIds.split(",");
        Log.info(templateItemIds);
        List<EvalTemplateItem> templateItemList = new ArrayList<EvalTemplateItem>(
                templateItemIds.length);
        List<EvalTemplateItem> blockItemList = new ArrayList<EvalTemplateItem>();

        for (int i = 0; i < templateItemIds.length; i++) {

            EvalTemplateItem item = authoringService.getTemplateItemById(Long
                    .valueOf(templateItemIds[i]));
            templateItemList.add(item);

            if (TemplateItemUtils.getTemplateItemType(templateItemList.get(i)).equals(
                    EvalConstants.ITEM_TYPE_BLOCK_PARENT))
                blockItemList.add(item);

        }

        firstTemplateItem = templateItemList.get(0);
        originalDisplayOrder = firstTemplateItem.getDisplayOrder();

        // check if it is to modify an existing block or creating a new one
        if ((templateItemIds.length == 1) && (templateItemList.get(0) != null))
            modify = true;

        // check if each templateItem has the same scale, otherwise show warning text
        // enforce system settings of maximum number of child items for new Block creation
        if (! modify) {
            int maxChildsNo = ((Integer) settings.get(EvalSettings.ITEMS_ALLOWED_IN_QUESTION_BLOCK)).intValue();
            // get actual total number of no-parent item(block childs + normal scaled type)
            int actualChildsNo = 0;
            for (int i = 0; i < templateItemIds.length; i++) {
                EvalTemplateItem item = templateItemList.get(i);
                if (TemplateItemUtils.getTemplateItemType(item).equals(
                        EvalConstants.ITEM_TYPE_BLOCK_PARENT)) {
                    // get number of childs
                    List<EvalTemplateItem> l = authoringService
                    .getBlockChildTemplateItemsForBlockParent(item.getId(), false);
                    actualChildsNo = actualChildsNo + l.size();
                } else {
                    actualChildsNo++;
                }
            } // end of for loop
            // System.out.println("total number of childsin a block=" + actualChildsNo + ", maximum
            // number of childs allowed in block=" + maxChildsNo);
            if (actualChildsNo > maxChildsNo) {
                validChildsNo = false;
            }
        }


        UIMessage.make(tofill, "modify-block-title", "modifyblock.page.title"); //$NON-NLS-1$ //$NON-NLS-2$
        UIMessage.make(tofill, "create-eval-title", "starteval.page.title"); //$NON-NLS-1$ //$NON-NLS-2$

        UIInternalLink.make(tofill, "summary-link", UIMessage.make("summary.page.title"), //$NON-NLS-1$ //$NON-NLS-2$
                new SimpleViewParameters(SummaryProducer.VIEW_ID));

        if (!validChildsNo) {
            // show error page with back button
            UIBranchContainer showError = UIBranchContainer.make(tofill, "errorPage:");
            UIMessage.make(showError, "errorMsg", "modifyblock.error.numberofblockChilds.message");
            UIMessage.make(showError, "back-button", "modifyblock.back.button");

        } else {
            // render block page
            UIForm form = UIForm.make(tofill, "blockForm"); //$NON-NLS-1$

            UIMessage.make(tofill, "modifyblock-page-title", "modifyblock.page.title");
            UIMessage.make(form, "modifyblock-items-list-title", "modifyblock.items.list.title"); //$NON-NLS-1$ //$NON-NLS-2$
            UIMessage.make(form, "modifyblock-items-list-instructions",
            "modifyblock.page.instructions");

            UIMessage.make(form, "item-header", "modifyblock.item.header");
            UIOutput.make(form, "itemNo", originalDisplayOrder.toString());

            UIMessage.make(form, "itemClassification", "modifytemplate.itemtype.block");
            UIMessage.make(form, "added-by", "modifyitem.added.by");
            EvalUser owner = commonLogic.getEvalUserById( firstTemplateItem.getOwner() );
            UIOutput.make(form, "userInfo", owner.displayName );
            // remove link

            if (modify) {
                UIBranchContainer showLink = UIBranchContainer.make(form, "showRemoveLink:");
                UIInternalLink.make(showLink, "remove_link", UIMessage
                        .make("general.command.split.block"), new TemplateItemViewParameters(
                                RemoveItemProducer.VIEW_ID, templateId, firstTemplateItem.getId()));
            }

            UIMessage.make(form, "item-header-text-header", "modifyblock.item.header.text.header");

            UIMessage.make(form, "scale-type-header", "modifyblock.scale.type.header");
            UIOutput.make(form, "scaleLabel", firstTemplateItem.getItem().getScale().getTitle());
            UIMessage.make(form, "ideal-coloring-header", "modifyblock.ideal.coloring.header"); //$NON-NLS-1$ //$NON-NLS-2$

            /*
             * If the system setting (admin setting) for
             * "EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY" is set as true then all items default to
             * "Course". If it is set to false, then all items default to "Instructor". If it is set to
             * null then user is given the option to choose between "Course" and "Instructor".
             */
            Boolean isDefaultCourse = (Boolean) settings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY);

            String itemPath = null, blockId = null;

            if (modify) { // modify existing block
                blockId = firstTemplateItem.getId().toString();
            } else { // create new block
                blockId = TemplateItemWBL.NEW_1;
            }

            itemPath = "templateItemWBL." + blockId;

            // this sets the ideal color setting
            if ((firstTemplateItem.getScaleDisplaySetting() != null)
                    && (firstTemplateItem.getScaleDisplaySetting()
                            .equals(EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED))) {
                UIBoundBoolean.make(form, "idealColor", "#{templateBBean.idealColor}", Boolean.TRUE);
            } else {
                UIBoundBoolean.make(form, "idealColor", "#{templateBBean.idealColor}", null);
            }

            /*
             * If the system setting (admin setting) for
             * "EvalSettings.NOT_AVAILABLE_ALLOWED" is set as true then only we need to show the
             * item_NA checkbox.
             */
            if (((Boolean) settings.get(EvalSettings.ENABLE_NOT_AVAILABLE)).booleanValue()) {
                UIBranchContainer showNA = UIBranchContainer.make(form, "showNA:");
                UIMessage.make(showNA, "add-na-header", "modifyitem.item.na.header");
                UIBoundBoolean.make(form, "item_NA", itemPath + ".usesNA", null);
            }

            // RENDER the category selector
            Boolean useCourseCategoryOnly = (Boolean) settings.get(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY);
            if (useCourseCategoryOnly) {
                form.parameters.add(new UIELBinding(itemPath + ".category",
                        EvalToolConstants.ITEM_CATEGORY_VALUES[isDefaultCourse.booleanValue() ? 0 : 1]));
            } else {
                // show all category choices so the user can choose, default is course category
                UIBranchContainer showItemCategory = UIBranchContainer.make(form, "showItemCategory:");
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
                		categoryValues, 
                		categoryLabels,
                        itemPath + ".category").setMessageKeys();
                for (int i = 0; i < categoryValues.length; i++) {
                    UIBranchContainer radioBranch = UIBranchContainer.make(showItemCategory, "item-category-branch:", i+"");
                    UISelectChoice choice = UISelectChoice.make(radioBranch, "item-category-radio", radios.getFullID(), i);
                    UISelectLabel.make(radioBranch, "item-category-label", radios.getFullID(), i)
                    .decorate( new UILabelTargetDecorator(choice) );
                }
            }

            // hierarchy node selector control
            Boolean showHierarchyOptions = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
            if (showHierarchyOptions) {
                hierarchyNodeSelectorRenderer.renderHierarchyNodeSelector(form,
                        "hierarchyNodeSelector:", itemPath + ".hierarchyNodeId", null);
            }


            // render the child items

            int blockItemListSize = blockItemList.size();

            if ((!modify) && (blockItemListSize > 0)) {

                String[] blockIds = new String[blockItemListSize + 1];
                String[] blockTextChoices = new String[blockItemListSize + 1];

                for (int i = 0; i < blockItemListSize; i++) {
                    EvalTemplateItem blockItem = blockItemList.get(i);
                    blockIds[i] = blockItem.getId().toString();
                    blockTextChoices[i] = blockItem.getItem().getItemText();
                }

                blockIds[blockItemListSize] = TemplateItemWBL.NEW_1;
                blockTextChoices[blockItemListSize] = "Enter new block text";

                UISelect blockTextSelect = UISelect.make(form, "block-text-select", blockIds,
                        blockTextChoices, "templateBBean.blockTextChoice", null);
                String selectIds = blockTextSelect.getFullID();

                for (int i = 0; i < blockIds.length; i++) {
                    UIBranchContainer blockTextChoice = UIBranchContainer.make(form,
                            "block-text-choice:", Integer.toString(i));
                    UISelectChoice.make(blockTextChoice, "block-text-radio", selectIds, i);
                    UIOutput.make(blockTextChoice, "block-text", blockTextChoices[i]);
                }

            }


            UIInput.make(form, "item-text", itemPath + ".item.itemText", null);
            //richTextEvolver.evolveTextInput(itemtext);


            // render the items below
            UIMessage.make(form, "items-header", "modifyitem.item.header");

            List<EvalTemplateItem> allTemplateItems = 
                authoringService.getTemplateItemsForTemplate(templateId, new String[] {}, new String[] {}, new String[] {});
            int orderNo = 0;
            for (int i = 0; i < templateItemIds.length; i++) {
                EvalTemplateItem item = templateItemList.get(i);
                if (TemplateItemUtils.getTemplateItemType(item).equals(
                        EvalConstants.ITEM_TYPE_BLOCK_PARENT)) { // item is a block parent
                    List<EvalTemplateItem> children = TemplateItemUtils.getChildItems(allTemplateItems,
                            item.getId());
                    // iterate through block children for the current block parent and emit each child item
                    for (int j = 0; j < children.size(); j++) {
                        EvalTemplateItem child = (EvalTemplateItem) children.get(j);
                        emitItem(form, child, orderNo + 1);
                        orderNo++;
                    }

                } else { // item is a normal scale type
                    emitItem(form, item, orderNo + 1);
                    orderNo++;
                }

            }

            UIInput.make(form, "ordered-child-ids", "#{templateBBean.orderedChildIds}");

            UIMessage.make(form, "cancel-button", "general.cancel.button");
            UICommand saveCmd = UICommand.make(form, "saveBlockAction", UIMessage
                    .make("modifyitem.save.button"), "#{templateBBean.saveBlockItemAction}");
            saveCmd.parameters.add(new UIELBinding("#{templateBBean.blockId}", blockId));
            saveCmd.parameters.add(new UIELBinding("#{templateBBean.templateItemIds}",
                    evParameters.templateItemIds));
            saveCmd.parameters.add(new UIELBinding("#{templateBBean.originalDisplayOrder}",
                    originalDisplayOrder));

        }

    }

    /**
     * @param tofill
     * @param templateItem
     * @param index
     */
    private void emitItem(UIContainer tofill, EvalTemplateItem templateItem, int index) {
        UIBranchContainer radiobranch = UIBranchContainer.make(tofill,
                "itemRow:", templateItem.getId().toString()); //$NON-NLS-1$
        UIOutput.make(radiobranch, "hidden-item-id", templateItem.getId().toString());
        UIOutput.make(radiobranch, "item-num", Integer.toString(index));
        UIVerbatim.make(radiobranch, "item-text", templateItem.getItem().getItemText());
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
     */
    @SuppressWarnings("unchecked")
    public List reportNavigationCases() {
        List i = new ArrayList();

        i.add(new NavigationCase(PreviewItemProducer.VIEW_ID, new SimpleViewParameters(
                PreviewItemProducer.VIEW_ID)));
        i.add(new NavigationCase("success", new TemplateViewParameters(
                ModifyTemplateItemsProducer.VIEW_ID, null)));

        return i;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new BlockIdsParameters();
    }

}
