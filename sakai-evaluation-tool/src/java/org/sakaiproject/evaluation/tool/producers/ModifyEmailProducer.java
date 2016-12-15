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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.tool.locators.EmailTemplateWBL;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EmailViewParameters;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Page for Modifying Email templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ModifyEmailProducer extends EvalCommonProducer implements ViewParamsReporter, ActionResultInterceptor {

   public static final String VIEW_ID = "modify_email";
   public String getViewID() {
      return VIEW_ID;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private static final String EMAIL_TEMPLATE_LOCATOR = "emailTemplateWBL.";
   
   private NavBarRenderer navBarRenderer;
   public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      // handle the input params for the view
      EmailViewParameters emailViewParams = (EmailViewParameters) viewparams;
      if (emailViewParams.templateId == null 
            && emailViewParams.emailType == null) {
         throw new IllegalArgumentException("Either templateId or emailType must be set before accessing the preview email view");
      }

      String actionBean = "setupEvalBean.";

      // form the proper OTP path
      boolean newEmailTemplate = true;
      String emailTemplateId = EmailTemplateWBL.NEW_1 + emailViewParams.emailType; // default is new one of the supplied type
      if (emailViewParams.templateId == null) {
         // see if we can get the id from the evaluation
         if (emailViewParams.evaluationId != null) {
            EvalEmailTemplate emailTemplate = evaluationService.getEmailTemplate(emailViewParams.evaluationId, emailViewParams.emailType);
            if (emailTemplate.getDefaultType() == null) {
               // found a non-default template
               emailTemplateId = emailTemplate.getId().toString();
               newEmailTemplate = false;
            }
         }
      } else {
         emailTemplateId = emailViewParams.templateId.toString();
         newEmailTemplate = false;
      }
      String emailTemplateOTP = EMAIL_TEMPLATE_LOCATOR + emailTemplateId + ".";

      if (emailViewParams.evaluationId == null) {
         /*
          * top links here
          */
    	  navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());
      }

      UIMessage.make(tofill, "modify-template-header", "modifyemail.modify.template.header", 
               new Object[] {emailViewParams.emailType});

      UIVerbatim.make(tofill, "email_templates_fieldhints", UIMessage.make("email.templates.field.names"));

      UIForm form = UIForm.make(tofill, "emailTemplateForm");

      String actionBinding;
      if (emailViewParams.evaluationId != null) {
         // bind in the evaluationId
         form.parameters.add(new UIELBinding(actionBean + "evaluationId", emailViewParams.evaluationId));
         actionBinding = actionBean + "saveAndAssignEmailTemplate";

         // check the type is set and not invalid
         if (emailViewParams.emailType == null) {
            throw new IllegalArgumentException("emailType must be set when working with an evaluation");            
         } else {
            if ( ! (EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailViewParams.emailType)
            		|| EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailViewParams.emailType)
            		|| EvalConstants.EMAIL_TEMPLATE_SUBMITTED.equals(emailViewParams.emailType)) ) {
               throw new IllegalArgumentException("Unknown email template type for use in evaluation: " + emailViewParams.emailType);
            }
         }

         // add in the close window control
         UIMessage.make(tofill, "closeWindow", "general.close.window.button");
         if (! newEmailTemplate) {
            // add in the reset to default if not a new email template
            UICommand resetCommand = UICommand.make(form, "resetEmailTemplate", UIMessage.make("modifyemail.reset.to.default.link"), 
                  actionBean + "resetToDefaultEmailTemplate");
            resetCommand.addParameter( new UIELBinding(actionBean + "emailTemplateType", emailViewParams.emailType) );
         }
      } else {
         // not part of an evaluation so use the WBL
         actionBinding = EMAIL_TEMPLATE_LOCATOR + "saveAll";
         // add in a cancel button
         UIMessage.make(form, "cancel-button", "general.cancel.button");
      }

      UIInput.make(form, "emailSubject", emailTemplateOTP + "subject");
      UIInput.make(form, "emailMessage", emailTemplateOTP + "message");

      UICommand.make(form, "saveEmailTemplate", UIMessage.make("modifyemail.save.changes.link"), actionBinding);

   }


   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.flow.ActionResultInterceptor#interceptActionResult(uk.org.ponder.rsf.flow.ARIResult, uk.org.ponder.rsf.viewstate.ViewParameters, java.lang.Object)
    */
   public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
      // handles the navigation cases and passing along data from view to view
      EmailViewParameters evp = (EmailViewParameters) incoming;
      EmailViewParameters outgoing = (EmailViewParameters) evp.copyBase(); // inherit all the incoming data
      if ("success".equals(actionReturn) 
            || "successAssign".equals(actionReturn) 
            || "successReset".equals(actionReturn) ) {
         outgoing.viewID = PreviewEmailProducer.VIEW_ID;
         result.resultingView = outgoing;
      } else if ("failure".equals(actionReturn)) {
         // failure just comes back here
         result.resultingView = outgoing;
      } else {
         // default
         result.resultingView = new SimpleViewParameters(ControlEmailTemplatesProducer.VIEW_ID);
      }
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
    */
   public ViewParameters getViewParameters() {
      return new EmailViewParameters();
   }

}
