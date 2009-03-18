/******************************************************************************
 * ControlItemsProducer.java - created by aaronz on 20 May 2007
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

import java.util.List;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;

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
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * This lists items for users so they can add, modify, remove them
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ControlItemsProducer implements ViewComponentProducer {

    public static String VIEW_ID = "control_items";
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

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }



    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // page title
        UIMessage.make(tofill, "page-title", "controlitems.page.title");

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
            UIInternalLink.make(tofill, "begin-evaluation-link", 
                    UIMessage.make("starteval.page.title"), 
                    new EvalViewParameters(EvaluationCreateProducer.VIEW_ID, null));
        }


        UIMessage.make(tofill, "items-header", "controlitems.items.header");
        UIMessage.make(tofill, "items-description", "controlitems.items.description");
        UIMessage.make(tofill, "items-metadata-title", "controlitems.items.metadata.title");
        UIMessage.make(tofill, "items-action-title", "controlitems.items.action.title");
        UIMessage.make(tofill, "items-owner-title", "controlitems.items.owner.title");
        UIMessage.make(tofill, "items-expert-title", "controlitems.items.expert.title");		

        // use get form to submit the type of item to create
        UIMessage.make(tofill, "add-item-header", "controlitems.items.add");
        UIForm addItemForm = UIForm.make(tofill, "add-item-form", 
                new ItemViewParameters(ModifyItemProducer.VIEW_ID, null));
        UISelect.make(addItemForm, "item-classification-list", 
                EvalToolConstants.ITEM_SELECT_CLASSIFICATION_VALUES, 
                EvalToolConstants.ITEM_SELECT_CLASSIFICATION_LABELS, 
        "#{itemClassification}").setMessageKeys();
        UIMessage.make(addItemForm, "add-item-button", "controlitems.items.add.button");

        UIForm form = UIForm.make(tofill, "copyForm");

        // get items for the current user
        List<EvalItem> userItems = authoringService.getItemsForUser(currentUserId, null, null, userAdmin);
        if (userItems.size() > 0) {
            UIBranchContainer itemListing = UIBranchContainer.make(form, "item-listing:");

            for (int i = 0; i < userItems.size(); i++) {
                EvalItem item = (EvalItem) userItems.get(i);
                UIBranchContainer itemBranch = UIBranchContainer.make(itemListing, "item-row:", item.getId().toString());
                if (i % 2 == 0) {
                    itemBranch.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
                }

                UIOutput.make(itemBranch, "item-classification", item.getClassification());

                if (item.getScaleDisplaySetting() != null) {
                    String scaleDisplaySettingLabel = " - " + item.getScaleDisplaySetting();
                    UIOutput.make(itemBranch, "item-scale", scaleDisplaySettingLabel);
                }

                EvalUser owner = commonLogic.getEvalUserById( item.getOwner() );
                UIOutput.make(itemBranch, "item-owner", owner.displayName );
                if (item.getExpert().booleanValue() == true) {
                    // label expert items
                    UIMessage.make(itemBranch, "item-expert", "controlitems.expert.label");
                }

                UIVerbatim.make(itemBranch, "item-text", item.getItemText());

                UIInternalLink.make(itemBranch, "item-preview-link", UIMessage.make("general.command.preview"), 
                        new ItemViewParameters(PreviewItemProducer.VIEW_ID, item.getId(), (Long)null) );

                // local locked check is more efficient so do that first
                if ( !item.getLocked().booleanValue() && 
                        authoringService.canModifyItem(currentUserId, item.getId()) ) {
                    UIInternalLink.make(itemBranch, "item-modify-link", UIMessage.make("general.command.edit"), 
                            new ItemViewParameters(ModifyItemProducer.VIEW_ID, item.getId(), null));
                } else {
                    UIMessage.make(itemBranch, "item-modify-dummy", "general.command.edit");
                }

                // local locked check is more efficient so do that first
                if ( !item.getLocked().booleanValue() && 
                        authoringService.canRemoveItem(currentUserId, item.getId()) ) {
                    UIInternalLink.make(itemBranch, "item-remove-link", UIMessage.make("general.command.delete"), 
                            new ItemViewParameters(RemoveItemProducer.VIEW_ID, item.getId(), null));
                } else {
                    UIMessage.make(itemBranch, "item-remove-dummy", "general.command.delete");
                }

                // create the copy button/link
                UICommand copy = UICommand.make(itemBranch, "item-copy-link", UIMessage.make("general.copy"),
                "templateBBean.copyItem");
                copy.parameters.add(new UIELBinding("templateBBean.itemId", item.getId()));

            }
        } else {
            UIMessage.make(tofill, "no-items", "controlitems.items.none");
        }
    }

}
