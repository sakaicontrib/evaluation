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
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.tool.viewparams.ModifyExpertItemParameters;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * EVALSYS-1026
 * Handles expert item group removal confirmation. Only a Category with no, 0, objectives
 * or items can be deleted.  An Objective can be delete, only if there are no, 0, items.
 * 
 * @author Rick Moyer (rmoyer@umd.edu)
 */
public class RemoveExpertItemProducer extends EvalCommonProducer implements ViewParamsReporter, NavigationCaseReporter {

   public static final String VIEW_ID = "remove_expert_item";
   public String getViewID() {
      return VIEW_ID;
   }

   private EvalCommonLogic external;
   public void setExternal(EvalCommonLogic external) {
      this.external = external;
   }

   private EvalAuthoringService authoringService;
   public void setAuthoringService(EvalAuthoringService authoringService) {
      this.authoringService = authoringService;
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      String beanBinding = "expertItemsBean.";
      String actionBinding = "removeExpertItem";

      // passed in values
      ModifyExpertItemParameters params = (ModifyExpertItemParameters) viewparams;
      
      Long eigId;
      if ( EvalConstants.ITEM_GROUP_TYPE_CATEGORY.equals(params.type)) {
      	eigId = params.categoryId;
      } else {
      	eigId = params.objectiveId;
      }

      EvalItemGroup eig = authoringService.getItemGroupById(eigId);

      // deletion message
      UIMessage.make(tofill, "removeexpertitem.confirm.text", 
            "removeexpertitem.confirm.text", new Object[] {eig.getTitle()});

      UIForm form = UIForm.make(tofill, "remove-expertitem-form");
      UICommand deleteCommand = UICommand.make(form, "remove-expertitem-remove-button", 
            UIMessage.make("removeexpertitem.remove.scale.button"), beanBinding + actionBinding);
      deleteCommand.parameters.add(new UIELBinding(beanBinding + "eigId", eigId));
      UIInternalLink.make(form, "cancel-link", UIMessage.make("modifyexpertitem.cancel"), new SimpleViewParameters(ControlExpertItemsProducer.VIEW_ID));
   }

   /* 
    * (non-Javadoc)
    * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public List reportNavigationCases() {
      List togo = new ArrayList();
      togo.add(new NavigationCase("success", new SimpleViewParameters(ControlExpertItemsProducer.VIEW_ID)));
      return togo;
   }

   public ViewParameters getViewParameters() {
      return new ModifyExpertItemParameters(VIEW_ID, null, null, EvalConstants.ITEM_GROUP_TYPE_CATEGORY, false);           	    	
   }
}
