/******************************************************************************
 * PreviewEmailProducer.java - created by fengr@vt.edu on Oct 24, 2006
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

import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.params.EvalViewParameters;


import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Preview email template page
 * 
 * @author: Rui Feng (fengr@vt.edu)
 */

public class PreviewEmailProducer implements ViewComponentProducer,NavigationCaseReporter, ViewParamsReporter {

	private final static String BREAK = "<br/>";  //$NON-NLS-1$
	private EvaluationBean evaluationBean;
	public static final String VIEW_ID = "preview_email"; //$NON-NLS-1$
	
	public void setEvaluationBean(EvaluationBean evaluationBean) {
		this.evaluationBean = evaluationBean;
	}

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		this.messageLocator = messageLocator;
	}	
	
	public String getViewID() {
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

		UIOutput.make(tofill, "preview-email-title", messageLocator.getMessage("previewemail.page.title"));	 //$NON-NLS-1$ //$NON-NLS-2$
		UIInternalLink.make(tofill, "summary-toplink", messageLocator.getMessage("summary.page.title"),  //$NON-NLS-1$ //$NON-NLS-2$
				new SimpleViewParameters(SummaryProducer.VIEW_ID));			
		UIOutput.make(tofill, "create-eval-title", messageLocator.getMessage("createeval.page.title"));	 //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(tofill, "preview-email-header", messageLocator.getMessage("previewemail.header")); //$NON-NLS-1$ //$NON-NLS-2$
		
		EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;
		
		String emailText = null;
		
		UIForm form = UIForm.make(tofill, "previewEmailForm"); //$NON-NLS-1$
			
		UIOutput.make(form, "preview-email-desc", messageLocator.getMessage("previewemail.desc")); //$NON-NLS-1$ //$NON-NLS-2$
		
		UIOutput.make(form, "close-button", messageLocator.getMessage("general.close.window.button"));
		
		if( evalViewParams.originalPage.equals("available")){ //$NON-NLS-1$
			UICommand.make(form, "modifyEmailTemplate", messageLocator.getMessage("previewemail.modify.button"), "#{evaluationBean.modifyAvailableEmailTemplate}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			emailText = evaluationBean.emailAvailableTxt;
			emailText = emailText.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
			emailText = emailText.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
			emailText = emailText.replaceAll("\n", BREAK); //$NON-NLS-1$
			UIVerbatim.make(form,"previewEmailText", emailText); //$NON-NLS-1$
		}
		if( evalViewParams.originalPage.equals("reminder")){ //$NON-NLS-1$
			UICommand.make(form, "modifyEmailTemplate", messageLocator.getMessage("previewemail.modify.button"), "#{evaluationBean.modifyReminderEmailTemplate}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			emailText = evaluationBean.emailReminderTxt;
			emailText = emailText.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
			emailText = emailText.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
			emailText = emailText.replaceAll("\n", BREAK); //$NON-NLS-1$
			UIVerbatim.make(form,"previewEmailText", emailText); //$NON-NLS-1$
		}
	}

	public List reportNavigationCases() {
		List i = new ArrayList();
		
		i.add(new NavigationCase("available", new EvalViewParameters(ModifyEmailProducer.VIEW_ID, null, "available"))); //$NON-NLS-1$ //$NON-NLS-2$
		i.add(new NavigationCase("reminder", new EvalViewParameters(ModifyEmailProducer.VIEW_ID, null, "reminder"))); //$NON-NLS-1$ //$NON-NLS-2$
		return i;
	}

	public ViewParameters getViewParameters() {
		return new EvalViewParameters(VIEW_ID, null, null);
	}	
}
