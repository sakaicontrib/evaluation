/**
 * $Id$
 * $URL$
 * PreviewEmailProducer.java - evaluation - Mar 4, 2008 10:53:24 AM - azeckoski
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
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.locators.LineBreakResolver;
import org.sakaiproject.evaluation.tool.viewparams.EmailViewParameters;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * View for previewing email templates
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class PreviewEmailProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

   public static final String VIEW_ID = "preview_email";
   public String getViewID() {
      return VIEW_ID;
   }

   private EvaluationBean evaluationBean;
   public void setEvaluationBean(EvaluationBean evaluationBean) {
      this.evaluationBean = evaluationBean;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }


   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      EmailViewParameters emailViewParams = (EmailViewParameters) viewparams;
      if (emailViewParams.templateId == null && emailViewParams.emailType == null) {
         throw new IllegalArgumentException("Either templateId or emailType must be set before accessing the preview email view");
      }

      UIForm form = UIForm.make(tofill, "previewEmailForm");

      UIVerbatim.make(tofill, "email_templates_fieldhints", UIMessage.make("email.templates.field.names"));

      // get the email template
      EvalEmailTemplate emailTemplate = null;
      if (emailViewParams.templateId != null) {
         emailTemplate = evaluationService.getEmailTemplate(emailViewParams.templateId);
      }
      if (emailTemplate == null 
            && emailViewParams.emailType != null) {
         emailTemplate = evaluationService.getDefaultEmailTemplate(emailViewParams.emailType);
      }
      String emailMessage = emailTemplate.getMessage();

      if (emailViewParams.inEval) {
         // try to get the email text from the evaluation bean
         String actionBinding = null;
         if (EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailViewParams.emailType)) {
            actionBinding = "#{evaluationBean.modifyAvailableEmailTemplate}";
            emailMessage = evaluationBean.emailAvailableTxt;
         } else if (EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailViewParams.emailType)) {
            actionBinding = "#{evaluationBean.modifyReminderEmailTemplate}";
            emailMessage = evaluationBean.emailReminderTxt;
         }

         if (emailMessage == null) {
            // if there is nothing in the bean then just use the defaults
            emailMessage = emailTemplate.getMessage();
         }

         // TODO switch this over to a link once EvaluationBean is dead
         //UIInternalLink.make(form, "modify_link", UIMessage.make("previewemail.modify.button"), new EmailViewParameters(ModifyEmailProducer.VIEW_ID, emailViewParams.templateId, emailViewParams.emailType, emailViewParams.inEval) );
         UICommand.make(form, "modifyEmailTemplate", UIMessage.make("previewemail.modify.button"), actionBinding);
      }

      UIOutput.make(form, "emailSubject", emailTemplate.getSubject() );
      UIVerbatim.make(form, "emailMessage", new LineBreakResolver().resolveBean(emailMessage) );

      UIMessage.make(form, "close-button", "general.close.window.button");
   }

   @SuppressWarnings("unchecked")
   public List reportNavigationCases() {
      List i = new ArrayList();
      i.add( new NavigationCase(EvalConstants.EMAIL_TEMPLATE_AVAILABLE, new EmailViewParameters(
            ModifyEmailProducer.VIEW_ID, null, EvalConstants.EMAIL_TEMPLATE_AVAILABLE, true)) );
      i.add( new NavigationCase(EvalConstants.EMAIL_TEMPLATE_REMINDER, new EmailViewParameters(
            ModifyEmailProducer.VIEW_ID, null, EvalConstants.EMAIL_TEMPLATE_REMINDER, true)) );
      return i;
   }

   public ViewParameters getViewParameters() {
      return new EmailViewParameters();
   }
}
