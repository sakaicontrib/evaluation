/**
 * $Id$
 * $URL$
 * ModifyEmailProducer.java - evaluation - Feb 29, 2008 6:06:42 PM - azeckoski
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

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.locators.EmailTemplateWBL;
import org.sakaiproject.evaluation.tool.viewparams.EmailViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Page for Modifying Email templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ModifyEmailProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

   public static final String VIEW_ID = "modify_email";
   public String getViewID() {
      return VIEW_ID;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvaluationBean evaluationBean;
   public void setEvaluationBean(EvaluationBean evaluationBean) {
      this.evaluationBean = evaluationBean;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private String emailTemplateLocator = "emailTemplateWBL.";

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      // handle the input params for the view
      EmailViewParameters emailViewParams = (EmailViewParameters) viewparams;
      if (emailViewParams.templateId == null && emailViewParams.emailType == null) {
         throw new IllegalArgumentException("Either templateId or emailType must be set before accessing the preview email view");
      }
      String emailTemplateType = emailViewParams.emailType;

      String emailTemplateOTP = null;
      if (emailViewParams.templateId == null) {
         emailTemplateOTP = emailTemplateLocator + EmailTemplateWBL.NEW_1 + emailTemplateType + ".";
      } else {
         emailTemplateOTP = emailTemplateLocator + emailViewParams.templateId + ".";         
      }

      // local variables used in the render logic
      String currentUserId = externalLogic.getCurrentUserId();
      boolean userAdmin = externalLogic.isUserAdmin(currentUserId);

      if (! emailViewParams.inEval) {
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
         }

         UIInternalLink.make(tofill, "control-emailtemplates-link",
               UIMessage.make("controlemailtemplates.page.title"),
               new SimpleViewParameters(ControlEmailTemplatesProducer.VIEW_ID));
      }

      UIMessage.make(tofill, "modify-template-header", "modifyemail.modify.template.header", 
               new Object[] {emailTemplateType});

      UIVerbatim.make(tofill, "email_templates_fieldhints", UIMessage.make("email.templates.field.names"));

      UIForm form = UIForm.make(tofill, "emailTemplateForm");

      String inputBinding = null;
      String inputInitValue = null;
      String actionBinding = null;
      if (emailViewParams.inEval) {
         // TODO get rid of most of this once the EvaluationBean is dead
         // get the email template
         EvalEmailTemplate emailTemplate = null;
         if (emailViewParams.templateId != null) {
            emailTemplate = evaluationService.getEmailTemplate(emailViewParams.templateId);
         }
         if (emailTemplate == null 
               && emailViewParams.emailType != null) {
            emailTemplate = evaluationService.getDefaultEmailTemplate(emailViewParams.emailType);
         }

         if (EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailViewParams.emailType)) {
            actionBinding = "evaluationBean.saveAvailableEmailTemplate";
            inputBinding = "evaluationBean.emailAvailableTxt";
            inputInitValue = evaluationBean.emailAvailableTxt;
         } else if (EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailViewParams.emailType)) {
            actionBinding = "evaluationBean.saveReminderEmailTemplate";
            inputBinding = "evaluationBean.emailReminderTxt";
            inputInitValue = evaluationBean.emailReminderTxt;
         } else {
            throw new IllegalArgumentException("Unknown email template type for use in evaluation: " + emailTemplateType);
         }

         if (inputInitValue == null) {
            // if there is nothing in the bean then just use the defaults
            inputInitValue = emailTemplate.getMessage();
         }

         // add in the close window control
         UIMessage.make(tofill, "closeWindow", "general.close.window.button");
      } else {
         // not part of an evaluation so use the WBL
         inputBinding = emailTemplateOTP + "message";
         actionBinding = emailTemplateLocator + "saveAll";
         // also show the subject editor
         UIBranchContainer subjectBranch = UIBranchContainer.make(form, "showSubject:");
         UIInput.make(subjectBranch, "emailSubject", emailTemplateOTP + "subject");
         UIMessage.make(subjectBranch, "emailSubject_label", "modifyemail.template.subject");
      }
      UIInput.make(form, "emailMessage", inputBinding, inputInitValue);
      UICommand.make(form, "saveEmailTemplate", UIMessage.make("modifyemail.save.changes.link"), actionBinding);

   }

   @SuppressWarnings("unchecked")
   public List reportNavigationCases() {
      List i = new ArrayList();
      // this is kind of wacky
      i.add(new NavigationCase(EvalConstants.EMAIL_TEMPLATE_AVAILABLE, new EmailViewParameters(
            PreviewEmailProducer.VIEW_ID, null, EvalConstants.EMAIL_TEMPLATE_AVAILABLE, true)));
      i.add(new NavigationCase(EvalConstants.EMAIL_TEMPLATE_REMINDER, new EmailViewParameters(
            PreviewEmailProducer.VIEW_ID, null, EvalConstants.EMAIL_TEMPLATE_REMINDER, true)));
      // default return case
      i.add( new NavigationCase("", new SimpleViewParameters(ControlEmailTemplatesProducer.VIEW_ID)) );
      return i;
   }

   public ViewParameters getViewParameters() {
      return new EmailViewParameters();
   }

}
