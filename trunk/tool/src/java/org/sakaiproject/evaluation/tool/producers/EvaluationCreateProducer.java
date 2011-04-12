/**
 * $Id$
 * $URL$
 * EvaluationCreateProducer.java - evaluation - Mar 19, 2008 11:32:44 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.locators.EvaluationBeanLocator;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UISelectLabel;
import uk.org.ponder.rsf.components.decorators.UITextDimensionsDecorator;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;


/**
 * This is the view which begins the evaluation creation process (for starting/beginning evaluations)
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvaluationCreateProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {

   public static final String VIEW_ID = "evaluation_create";
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

   private TextInputEvolver richTextEvolver;
   public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
      this.richTextEvolver = richTextEvolver;
   }
   
   private NavBarRenderer navBarRenderer;
   public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}
   
   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      // local variables used in the render logic
      String currentUserId = commonLogic.getCurrentUserId();
      
      /*
       * top links here
       */
      navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

      EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;

      String actionBean = "setupEvalBean.";
      String evaluationOTP = "evaluationBeanLocator." + EvaluationBeanLocator.NEW_1 + ".";

      UIForm form = UIForm.make(tofill, "start_eval_form");

      UIInput.make(form, "title", evaluationOTP + ".title");
      UIInput instructions = UIInput.make(form, "instructions:", evaluationOTP + ".instructions");
      instructions.decorate( new UITextDimensionsDecorator(100, 4) );
      richTextEvolver.evolveTextInput(instructions);

      // Make bottom table containing the list of templates if no template set
      if (evalViewParams.templateId == null) {
         // get the templates usable by this user
         List<EvalTemplate> templateList = 
            authoringService.getTemplatesForUser(currentUserId, null, false);
         if (templateList.size() > 0) {
            UIBranchContainer chooseTemplate = UIBranchContainer.make(form, "chooseTemplate:");

            String[] values = new String[templateList.size()];
            String[] labels = new String[templateList.size()];

            UISelect radios = UISelect.make(chooseTemplate, "templateRadio", 
                  null, null, actionBean + "templateId", 
                  templateList.get(0).getId().toString()); // default template choice is the first one
            //radios.selection.darreshaper = new ELReference("id-defunnel"); // use the defunneler to bind the template directly
            String selectID = radios.getFullID();
            for (int i = 0; i < templateList.size(); i++) {
               EvalTemplate template = templateList.get(i);
               values[i] = template.getId().toString();
               labels[i] = template.getTitle();
               UIBranchContainer radiobranch = 
                  UIBranchContainer.make(chooseTemplate, "templateOptions:", i + "");
               UISelectChoice.make(radiobranch, "radioValue", selectID, i);
               UISelectLabel.make(radiobranch, "radioLabel", selectID, i);
               EvalUser owner = commonLogic.getEvalUserById( template.getOwner() );
               UIOutput.make(radiobranch, "radioOwner", owner.displayName );
               UIInternalLink.make(radiobranch, "viewPreview_link", 
                     UIMessage.make("starteval.view.preview.link"), 
                     new EvalViewParameters(PreviewEvalProducer.VIEW_ID, null, template.getId()) );
            }
            // need to assign the choices and labels at the end here since we used nulls at the beginning
            radios.optionlist = UIOutputMany.make(values);
            radios.optionnames = UIOutputMany.make(labels);
         } else {
            throw new IllegalStateException("User got to evaluation settings when they have no access to any templates... " 
                  + "producer suicide was the only way out");
         }
      } else {
         // just bind in the template explicitly
         form.parameters.add(new UIELBinding(actionBean + "templateId", evalViewParams.templateId));
//         form.parameters.add( new UIELBinding(evaluationOTP + "template", new ELReference("templateBeanLocator." + evalViewParams.templateId)) );
         // display the info about the template
         EvalTemplate template = authoringService.getTemplateById(evalViewParams.templateId);
         UIBranchContainer showTemplateBranch = UIBranchContainer.make(tofill, "showTemplate:");
         UIMessage.make(showTemplateBranch, "eval_template_title", "evalsettings.template.title.display",
               new Object[] { template.getTitle() });
         UIInternalLink.make(showTemplateBranch, "eval_template_preview_link", 
               UIMessage.make("evalsettings.template.preview.link"), 
               new EvalViewParameters(PreviewEvalProducer.VIEW_ID, null, template.getId()) );         
      }

      UIMessage.make(form, "cancel-button", "general.cancel.button");
      UICommand.make(form, "continueToSettings", UIMessage.make("starteval.continue.settings.link"), 
            actionBean + "completeCreateAction");

   }


   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
    */
   @SuppressWarnings("unchecked")
   public List reportNavigationCases() {
      List togo = new ArrayList();
      // the evaluationId should get filled in by the org.sakaiproject.evaluation.tool.wrapper.EvalActionResultInterceptor.java
      togo.add( new NavigationCase("evalSettings", new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, null)) );
      return togo;
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
    */
   public ViewParameters getViewParameters() {
      return new EvalViewParameters();
   }

}
