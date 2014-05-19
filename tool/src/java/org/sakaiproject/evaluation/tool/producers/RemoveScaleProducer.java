/******************************************************************************
 * RemoveScaleProducer.java - created by kahuja@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.tool.viewparams.EvalScaleParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handles scale removal confirmation.
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class RemoveScaleProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {

   public static final String VIEW_ID = "remove_scale";
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
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      String beanBinding = "templateBBean.";
      String actionBinding = "deleteScaleAction";

      /*
       * top menu links and bread crumbs here
       */
      UIInternalLink.make(tofill, "summary-link", 
            UIMessage.make("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID));
      UIInternalLink.make(tofill, "administrate-link", 
            UIMessage.make("administrate.page.title"), new SimpleViewParameters(AdministrateProducer.VIEW_ID));
      UIInternalLink.make(tofill, "control-scales-link",
            UIMessage.make("controlscales.page.title"), new SimpleViewParameters(ControlScalesProducer.VIEW_ID));

      // passed in values
      EvalScaleParameters evalScaleParams = (EvalScaleParameters) viewparams;
      Long scaleId = evalScaleParams.scaleId;

      EvalScale scale = authoringService.getScaleById(scaleId);

      // deletion message
      UIMessage.make(tofill, "removescale.confirm.text", 
            "removescale.confirm.text", new Object[] {scale.getTitle()});


      // in use message
      List<EvalItem> items = authoringService.getItemsUsingScale(scale.getId());
      if (items.size() > 0) {
         actionBinding = "hideScaleAction";
         UIBranchContainer inUseBranch = UIBranchContainer.make(tofill, "inUse:");
         UIMessage.make(inUseBranch, "inUseWarning", "removescale.inuse.warning", new Object[] {items.size()});
         for (EvalItem item : items) {
            UIBranchContainer itemsBranch = UIBranchContainer.make(inUseBranch, "items:");
            UIMessage.make(itemsBranch, "itemInfo", "removescale.inuse.info", new Object[] {item.getId(), 
                  item.getCategory(), external.getEvalUserById(item.getOwner()).displayName, item.getItemText()});
         }
      }

      UIMessage.make(tofill, "remove-scale-cancel-button", "general.cancel.button");

      UIForm form = UIForm.make(tofill, "remove-scale-form");
      UICommand deleteCommand = UICommand.make(form, "remove-scale-remove-button", 
            UIMessage.make("removescale.remove.scale.button"), beanBinding + actionBinding);
      deleteCommand.parameters.add(new UIELBinding(beanBinding + "scaleId", scaleId));
   }

   /* 
    * (non-Javadoc)
    * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
    */
   @SuppressWarnings("unchecked")
   public List reportNavigationCases() {
      List togo = new ArrayList();
      togo.add(new NavigationCase("success", new SimpleViewParameters(ControlScalesProducer.VIEW_ID)));
      return togo;
   }

   public ViewParameters getViewParameters() {
      return new EvalScaleParameters(VIEW_ID, null);
   }
}
