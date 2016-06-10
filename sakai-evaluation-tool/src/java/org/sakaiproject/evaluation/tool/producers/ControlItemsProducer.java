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

import java.util.List;
import java.util.Objects;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
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
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * This lists items for users so they can add, modify, remove them
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ControlItemsProducer extends EvalCommonProducer {

    public static String VIEW_ID = "control_items";
    public String getViewID() {
        return VIEW_ID;
    }
    
    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings evalSettings) {
        this.evalSettings = evalSettings;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalAuthoringService authoringService;
    public void setAuthoringService(EvalAuthoringService authoringService) {
        this.authoringService = authoringService;
    }
    
    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // page title
        UIMessage.make(tofill, "page-title", "controlitems.page.title");

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        /*
         * top links here
         */
        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        // EVALSYS-1026
        Boolean useExpertItems = (Boolean) evalSettings.get(EvalSettings.USE_EXPERT_ITEMS);
        if (useExpertItems) {
        	UIInternalLink.make(tofill, "expert-items-link", 
                UIMessage.make("controlexpertitems.page.title"), 
                new SimpleViewParameters(ControlExpertItemsProducer.VIEW_ID)); 
        }
        List<EvalItemGroup> itemGroups = authoringService.getAllItemGroups(currentUserId, true);

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
                
                EvalItemGroup evalItemGroup = new EvalItemGroup();
                if (item.getExpert() == true) {
                    // label expert items
                    UIMessage.make(itemBranch, "item-expert", "controlitems.expert.label");
                    for (int j = 0; j < itemGroups.size(); j++) {
                		EvalItemGroup eig = (EvalItemGroup) itemGroups.get(j);
                		// loop through all expert items
                		boolean foundItemGroup = false;
                		if (!foundItemGroup) {
                			List<EvalItem> expertItems = authoringService.getItemsInItemGroup(eig.getId(), true);
                			for (int k = 0; k < expertItems.size(); k++) {
                				EvalItem expertItem = (EvalItem) expertItems.get(k);
                				if (Objects.equals( expertItem.getId(), item.getId() )) {
                					evalItemGroup = eig;
                					foundItemGroup = true;
                				}	
                			}
                		}
                    }
                    if ( EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE.equals(evalItemGroup.getType())) {
            			// get category for the objective
            			EvalItemGroup ig = evalItemGroup.getParent();
            			UIOutput.make(itemBranch, "item-expert-cat", ig.getTitle() + " : ");
            			UIOutput.make(itemBranch, "item-expert-obj", evalItemGroup.getTitle());
            		} else {
            			// expert item is only a cat
            			UIOutput.make(itemBranch, "item-expert-cat", evalItemGroup.getTitle());
            		}
            	}

                UIVerbatim.make(itemBranch, "item-text", item.getItemText());

                UIInternalLink.make(itemBranch, "item-preview-link", UIMessage.make("general.command.preview"), 
                        new ItemViewParameters(PreviewItemProducer.VIEW_ID, item.getId(), (Long)null) );

                // local locked check is more efficient so do that first
                if ( !item.getLocked() && 
                        authoringService.canModifyItem(currentUserId, item.getId()) ) {
                    UIInternalLink.make(itemBranch, "item-modify-link", UIMessage.make("general.command.edit"), 
                            new ItemViewParameters(ModifyItemProducer.VIEW_ID, item.getId(), null));
                } else {
                    UIMessage.make(itemBranch, "item-modify-dummy", "general.command.edit");
                }

                // local locked check is more efficient so do that first
                if ( !item.getLocked() && 
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
