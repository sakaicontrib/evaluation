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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.ModifyExpertItemParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * EVALSYS-1026
 * This lists the item groups for users so they can add, modify, remove them
 *
 * @author Rick Moyer (rmoyer@umd.edu)
 */
public class ControlExpertItemsProducer extends EvalCommonProducer {

    public static String VIEW_ID = "control_expert_items";
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
    
    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // page title
        UIMessage.make(tofill, "page-title", "controlexpertitems.page.title");

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        //boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        /*
         * top links here
         */
        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        // use get form to submit the type of item to create
        UIMessage.make(tofill, "add-item-header", "controlitems.items.add");
        UIForm addItemForm = UIForm.make(tofill, "add-category-form", 
                new ModifyExpertItemParameters(ModifyExpertItemProducer.VIEW_ID, null, null, EvalConstants.ITEM_GROUP_TYPE_CATEGORY, true));
        UIMessage.make(addItemForm, "add-category-button", "controlexpertitems.add.button");

        UIForm form = UIForm.make(tofill, "expertForm");
        
        
        List<EvalItemGroup> itemGroups = authoringService.getAllItemGroups(currentUserId, true);
        int numItemGroups = itemGroups.size();
               
        if (numItemGroups > 0) {
            UIBranchContainer itemListing = UIBranchContainer.make(form, "expertitem-listing:");
            UIMessage.make(itemListing, "expertitems-category-title", "controlexpertitems.page.category");
            UIMessage.make(itemListing, "expertitems-objective-title", "controlexpertitems.page.objective");
            UIMessage.make(itemListing, "expertitems-description-title", "controlexpertitems.page.description");
            UIMessage.make(itemListing, "expertitems-items-title", "controlexpertitems.page.items");
            UIMessage.make(itemListing, "expertitems-action-title", "controlexpertitems.page.action");
            UIMessage.make(itemListing, "expertitems-owner-title", "controlexpertitems.page.owner");
            Long categoryId;
            Long objectiveId;
            
            for (int i = 0; i < numItemGroups; i++) {
            	EvalItemGroup eig = (EvalItemGroup) itemGroups.get(i);
             	
                UIBranchContainer itemBranch = UIBranchContainer.make(itemListing, "expertitem-row:", eig.getId().toString());
                if (i % 2 == 0) {
                    itemBranch.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
                }

                if ( EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(eig.getType())) {
                	 categoryId = eig.getId();
                	 UIOutput.make(itemBranch, "expertitem-cat", eig.getTitle());
                } else {
                	UIOutput.make(itemBranch, "expertitem-cat", " ");
                	categoryId = null;
                }	 
                if ( EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE.equals(eig.getType())) {
                	 UIOutput.make(itemBranch, "expertitem-obj", eig.getTitle());
                	 objectiveId = eig.getId();
                } else {
                	UIOutput.make(itemBranch, "expertitem-obj", " ");
                	objectiveId = null;
                }
                                	 
                UIOutput.make(itemBranch, "expertitem-description", eig.getDescription());
                
                UIOutput.make(itemBranch, "expertitem-items", " " + eig.getGroupItems().size() + " ");

                EvalUser owner = commonLogic.getEvalUserById( eig.getOwner() );
                UIOutput.make(itemBranch, "expertitem-owner", owner.displayName );
                              
            	UIInternalLink.make(itemBranch, "expertitem-edit-link", UIMessage.make("general.command.edit"), 
                        new ModifyExpertItemParameters(ModifyExpertItemProducer.VIEW_ID, categoryId, objectiveId, eig.getType(), false));
            	if (eig.getGroupItems().size()>0) {
            		UIInternalLink.make(itemBranch, "expertitem-preview-link", UIMessage.make("controlexpertitems.display.items"), 
                        new ModifyExpertItemParameters(PreviewExpertItemProducer.VIEW_ID, categoryId, objectiveId, eig.getType(), false));
            		UIOutput.make(itemBranch, "bar2", "|");
            	}
                if ((EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(eig.getType())) &&
                	(eig.getGroupItems().isEmpty())) {
                	UIInternalLink.make(itemBranch, "expertitem-add-objective-link", UIMessage.make("controlexpertitems.add.objective"), 
                        new ModifyExpertItemParameters(ModifyExpertItemProducer.VIEW_ID, categoryId, null, EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE, true));
                	UIOutput.make(itemBranch, "bar3", "|");
                }

                if (eig.getGroupItems().isEmpty()) {
                	if ( EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE.equals(eig.getType())) {
                    UIInternalLink.make(itemBranch, "expertitem-remove-link", UIMessage.make("general.command.delete"), 
                    		new ModifyExpertItemParameters(RemoveExpertItemProducer.VIEW_ID, null, objectiveId, EvalConstants.ITEM_GROUP_TYPE_OBJECTIVE, false));
                    UIOutput.make(itemBranch, "bar4", "|");
                	} else {
                		// make sure no objectives
                		List<EvalItemGroup> l = authoringService.getItemGroups(categoryId, currentUserId, true, true);
                	    if (l.isEmpty()) {
                            UIInternalLink.make(itemBranch, "expertitem-remove-link", UIMessage.make("general.command.delete"), 
                            		new ModifyExpertItemParameters(RemoveExpertItemProducer.VIEW_ID, categoryId, null, EvalConstants.ITEM_GROUP_TYPE_CATEGORY, false));
                            UIOutput.make(itemBranch, "bar4", "|");
                	    }
                	}
                }
            }
        } else {
            UIMessage.make(tofill, "no-items", "controlitems.items.none");
        }
    }

}
