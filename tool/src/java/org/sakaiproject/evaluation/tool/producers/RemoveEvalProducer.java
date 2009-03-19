/**
 * $Id$
 * $URL$
 * RemoveEvalProducer.java - evaluation - Nov 16, 2006 11:32:44 AM - fengr
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This displays the evaluation removal options to the user,
 * rewrite of the original which is simpler and includes error checking
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class RemoveEvalProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {

    public static final String VIEW_ID = "remove_evaluation";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private Locale locale;
    public void setLocale(Locale locale){
        this.locale = locale;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        UIInternalLink.make(tofill, "summary-link", UIMessage.make("summary.page.title"),
                new SimpleViewParameters(SummaryProducer.VIEW_ID));

        UIInternalLink.make(tofill, "control-evaluations-link", UIMessage.make("controlevaluations.page.title"), 
                new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID) );

        EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;
        if (evalViewParams.evaluationId == null) {
            throw new IllegalArgumentException("Cannot access this view unless the evaluationId is set");
        }

        EvalEvaluation eval = evaluationService.getEvaluationById(evalViewParams.evaluationId);
        if (eval == null) {
            throw new RuntimeException("Cannot remove evaluation, no eval id found in passed in params, illegal access to page");
        }


        UIMessage.make(tofill, "remove-eval-confirm-name", 
                "removeeval.confirm.name", new Object[] { eval.getTitle() });

        UIOutput.make(tofill, "evalTitle", eval.getTitle());

        int count = evaluationService.countEvaluationGroups(eval.getId(), false);
        if (count > 1) {
            UIOutput.make(tofill, "evalAssigned", count + " groups");
        } else if (count == 1) {
            Long[] evalIds = { eval.getId() };
            Map<Long, List<EvalGroup>> evalGroups = evaluationService.getEvalGroupsForEval(evalIds, true, null);
            List<EvalGroup> groups = evalGroups.get(eval.getId());
            EvalGroup group = groups.get(0);
            String title = group.title;
            UIOutput.make(tofill, "evalAssigned", title);
        } else {
            UIMessage.make(tofill, "evalAssigned", "removeeval.assigned.none");
        }

        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        UIOutput.make(tofill, "evalStartDate", df.format(eval.getStartDate()));
        UIOutput.make(tofill, "evalDueDate", df.format(eval.getDueDate()));


        UIMessage.make(tofill, "cancel-command-link", "general.cancel.button");

        String actionBean = "setupEvalBean.";
        String actionBinding = "removeEvalAction";

        UIForm form = UIForm.make(tofill, "removeEvalForm");

        UICommand deleteCommand = UICommand.make(form, "remove-eval-command-link", 
                UIMessage.make("removeeval.remove.button"), actionBean + actionBinding);
        deleteCommand.parameters.add(new UIELBinding(actionBean + "evaluationId", eval.getId()));

    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
     */
    @SuppressWarnings("unchecked")
    public List reportNavigationCases() {
        List togo = new ArrayList();
        togo.add(new NavigationCase("success", new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID)));
        return togo;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new EvalViewParameters();
    }


}
