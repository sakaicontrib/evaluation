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

import java.text.DateFormat;
import java.util.Locale;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
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
public class EvaluationNotificationsProducer implements ViewComponentProducer, ViewParamsReporter, ActionResultInterceptor {

    public static final String VIEW_ID = "evaluation_notifications";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private Locale locale;
    public void setLocale(Locale locale) {
        this.locale = locale;
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
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        // top links here
        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        // handle the input params for the view
        EvalViewParameters evalViewParameters = (EvalViewParameters) viewparams;
        if (evalViewParameters.evaluationId == null) {
            throw new IllegalArgumentException("The evaluationId must be set before accessing the send email view");
        }
        Long evaluationId = evalViewParameters.evaluationId;
        EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
        String evalGroupId = evalViewParameters.evalGroupId;

        String actionBean = "sendEmailsBean.";

        // begin page render
        UIInternalLink.make(tofill, "evalSettingsLink", UIMessage.make("evalsettings.page.title"),
                new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evaluationId) );

        UIMessage.make(tofill, "notifyEval", "evalnotify.eval.info", 
            new Object[] {eval.getTitle(), 
                dateFormat.format(eval.getStartDate()), 
                dateFormat.format(eval.getSafeDueDate())} );
        if (evalGroupId != null) {
            EvalGroup evalGroup = commonLogic.makeEvalGroupObject(evalGroupId);
            UIMessage.make(tofill, "notifyGroup", "evalnotify.group.info", 
                    new Object[] {evalGroup.title} );
        }
        UIInternalLink.make(tofill, "evalRespondersLink", UIMessage.make("evalresponders.page.title"),
                new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evaluationId, evalGroupId) );

        UIVerbatim.make(tofill, "email_templates_fieldhints", UIMessage.make("email.templates.field.names"));

        UIForm form = UIForm.make(tofill, "emailForm");

        // bind in the evaluationId (and group)
        form.parameters.add(new UIELBinding(actionBean + "evaluationId", evaluationId));
        if (evalGroupId != null) {
            form.parameters.add(new UIELBinding(actionBean + "evalGroupId", evalGroupId));
        }

        UIInput.make(form, "emailSubject", actionBean + "subject");
        UIInput.make(form, "emailMessage", actionBean + "message");

        UISelect.make(form, "emailSendTo", EvalToolConstants.EVAL_NOTIFICATION_VALUES, 
                EvalToolConstants.EVAL_NOTIFICATION_LABELS_PROPS, 
                actionBean + "sendTo").setMessageKeys();

        UICommand.make(form, "emailSubmit", UIMessage.make("evalnotify.send.emails"), 
                actionBean + "sendEmailAction");

    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.ActionResultInterceptor#interceptActionResult(uk.org.ponder.rsf.flow.ARIResult, uk.org.ponder.rsf.viewstate.ViewParameters, java.lang.Object)
     */
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        // handles the navigation cases and passing along data from view to view
        EvalViewParameters evp = (EvalViewParameters) incoming;
        EvalViewParameters outgoing = (EvalViewParameters) evp.copyBase(); // inherit all the incoming data
        if ("failure".equals(actionReturn)) {
            // failure just comes back here
            result.resultingView = outgoing;
        } else {
            // default
            result.resultingView = new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID);
        }
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new EvalViewParameters();
    }

}
