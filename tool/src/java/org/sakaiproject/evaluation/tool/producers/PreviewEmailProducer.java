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

import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.tool.locators.LineBreakResolver;
import org.sakaiproject.evaluation.tool.viewparams.EmailViewParameters;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * View for previewing email templates
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class PreviewEmailProducer implements ViewComponentProducer, ViewParamsReporter {

    public static final String VIEW_ID = "preview_email";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }


    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        EmailViewParameters emailViewParams = (EmailViewParameters) viewparams;
        if (emailViewParams.templateId == null 
                && emailViewParams.emailType == null) {
            throw new IllegalArgumentException("At least templateId or emailType must be set before accessing the preview email view");
        }

        UIVerbatim.make(tofill, "email_templates_fieldhints", UIMessage.make("email.templates.field.names"));

        // get the email template by the templateId or from the evaluation
        EvalEmailTemplate emailTemplate = null;
        if (emailViewParams.templateId != null) {
            emailTemplate = evaluationService.getEmailTemplate(emailViewParams.templateId);
        }
        if (emailTemplate == null 
                && emailViewParams.emailType != null) {
            // get either the template associated with the eval or the default one
            emailTemplate = evaluationService.getEmailTemplate(emailViewParams.evaluationId, emailViewParams.emailType);
        }

        if (emailViewParams.evaluationId != null) {
            // we are working with an evaluation so add edit controls

            // use a get form to submit to the editing page
            UIForm form = UIForm.make(tofill, "previewEmailForm",
                    new EmailViewParameters(ModifyEmailProducer.VIEW_ID, emailViewParams.templateId, 
                            emailViewParams.emailType, emailViewParams.evaluationId) );
            UIMessage.make(form, "modifyEmailTemplate", "previewemail.modify.button");
        }

        UIOutput.make(tofill, "emailSubject", emailTemplate.getSubject() );
        UIVerbatim.make(tofill, "emailMessage", new LineBreakResolver().resolveBean(emailTemplate.getMessage()) );

        UIMessage.make(tofill, "close-button", "general.close.window.button");
    }

    public ViewParameters getViewParameters() {
        return new EmailViewParameters();
    }
}
