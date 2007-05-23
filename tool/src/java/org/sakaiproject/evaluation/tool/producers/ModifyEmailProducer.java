/******************************************************************************
 * ModifyEmailProducer.java - created by fengr@vt.edu on Oct 24, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.viewparams.EmailViewParameters;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
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
 * Page for Modify Email template
 * 
 * @author: Rui Feng (fengr@vt.edu)
 */

public class ModifyEmailProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter { 
	
	public static final String VIEW_ID = "modify_email"; //$NON-NLS-1$

	public String getViewID() {
		return VIEW_ID;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIMessage.make(tofill, "modify-email-title", "modifyemail.page.title"); //$NON-NLS-1$ //$NON-NLS-2$
		UIMessage.make(tofill, "create-eval-title", "starteval.page.title"); //$NON-NLS-1$ //$NON-NLS-2$
		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"), new SimpleViewParameters(SummaryProducer.VIEW_ID));		 //$NON-NLS-1$ //$NON-NLS-2$
		
		
		UIMessage.make(tofill, "modify-template-header", "modifyemail.modify.template.header"); //$NON-NLS-1$ //$NON-NLS-2$
		UIForm form = UIForm.make(tofill, "emailTemplateForm"); //$NON-NLS-1$
		
		UIMessage.make(form, "modify-text-instructions", "modifyemail.modify.text.instructions"); //$NON-NLS-1$ //$NON-NLS-2$
		
		EmailViewParameters emailViewParams = (EmailViewParameters) viewparams;
		
		UIMessage.make(form, "close-button", "general.close.window.button");
		
		if( emailViewParams.emailType.equals(EvalConstants.EMAIL_TEMPLATE_AVAILABLE)){ //$NON-NLS-1$
			UIInput.make(form,"emailTemplate","#{evaluationBean.emailAvailableTxt}", null); //$NON-NLS-1$ //$NON-NLS-2$
			UICommand.make(form, "saveEmailTemplate",UIMessage.make("modifyemail.save.changes.link"), "#{evaluationBean.saveAvailableEmailTemplate}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		if( emailViewParams.emailType.equals(EvalConstants.EMAIL_TEMPLATE_REMINDER)){ //$NON-NLS-1$
			UIInput.make(form,"emailTemplate","#{evaluationBean.emailReminderTxt}", null); //$NON-NLS-1$ //$NON-NLS-2$
			UICommand.make(form, "saveEmailTemplate", UIMessage.make("modifyemail.save.changes.link"), "#{evaluationBean.saveReminderEmailTemplate}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

	}

	public List reportNavigationCases() {
		List i = new ArrayList();
		i.add(new NavigationCase(EvalConstants.EMAIL_TEMPLATE_AVAILABLE, 
            new EmailViewParameters(PreviewEmailProducer.VIEW_ID, null, EvalConstants.EMAIL_TEMPLATE_AVAILABLE))); //$NON-NLS-1$ //$NON-NLS-2$
		i.add(new NavigationCase(EvalConstants.EMAIL_TEMPLATE_REMINDER, 
            new EmailViewParameters(PreviewEmailProducer.VIEW_ID, null, EvalConstants.EMAIL_TEMPLATE_REMINDER))); //$NON-NLS-1$ //$NON-NLS-2$
		return i;
	}

	public ViewParameters getViewParameters() {
		return new EmailViewParameters();
	}		

}
