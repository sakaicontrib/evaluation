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
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.utils.ScaledUtils;
import org.sakaiproject.evaluation.tool.viewparams.ModifyExpertItemParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * EVALSYS-1026
 * This producer displays all the items within an item group.
 * 
 * @author Rick Moyer (rmoyer@umd.edu) 
 */
public class PreviewExpertItemProducer extends EvalCommonProducer implements ViewParamsReporter, NavigationCaseReporter {

    public static final String VIEW_ID = "preview_expert_items";
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

    	UIMessage.make(tofill, "page-title", "previewexpertitem.page.title");
    	
    	ModifyExpertItemParameters params = (ModifyExpertItemParameters) viewparams;

        Long eigId;
        
    	UIBranchContainer header = UIBranchContainer.make(tofill, "expert-item-header-row:");
        if ( EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(params.type)) {
        	eigId = params.categoryId;
        	EvalItemGroup eig = authoringService.getItemGroupById(eigId);
        	UIMessage.make(header, "title-label", "modifyexpertitem.title.cat.label");
        	UIOutput.make(header, "title", eig.getTitle());
        } else {
        	eigId = params.objectiveId;
        	EvalItemGroup eig = authoringService.getItemGroupById(eigId);
        	UIMessage.make(header, "title-label", "modifyexpertitem.title.obj.label");
        	UIOutput.make(header, "title", eig.getTitle());
        }

        // loop through all expert items
        List<EvalItem> expertItems = authoringService.getItemsInItemGroup(eigId, true);
        for (int i = 0; i < expertItems.size(); i++) {
            EvalItem expertItem = (EvalItem) expertItems.get(i);
            
           	UIBranchContainer items = UIBranchContainer.make(tofill, "display-expert-item-list:", expertItem.getId().toString());
           	if (i % 2 == 0) {
           		items.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
           	}
            	
           	UIOutput.make(items, "display-item-text", expertItem.getItemText()); 
            if (expertItem.getScale() != null) {
               	UIOutput.make(items, "display-item-scale", ScaledUtils.makeScaleText(expertItem.getScale(), 0)); 
            } else {
            	UIOutput.make(items, "display-item-scale", expertItem.getClassification());
            }
            if (expertItem.getExpertDescription() != null) {
               	UIVerbatim.make(items, "display-item-expert-desc", expertItem.getExpertDescription()); 
            }

        }

        // create the cancel button
        UIInternalLink.make(tofill, "cancel-link", UIMessage.make("previewexpertitem.cancel"), new SimpleViewParameters(ControlExpertItemsProducer.VIEW_ID));


    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new ModifyExpertItemParameters();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List reportNavigationCases() {
        List cases = new ArrayList();
        cases.add(new NavigationCase(null, new SimpleViewParameters(ControlExpertItemsProducer.VIEW_ID)));
        return cases;
    }
}