/******************************************************************************
 * ShowEvalCategoryProducer.java - created by aaronz on 29 May 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.EvalCategoryViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Shows categories of evaluationSetupService and all related groups
 * Provides easy access to a series of evaluationSetupService
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ShowEvalCategoryProducer implements ViewComponentProducer, ViewParamsReporter  {

    public static final String VIEW_ID = "show_eval_category";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalEvaluationSetupService evaluationSetupService;
    public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
        this.evaluationSetupService = evaluationSetupService;
    }

    private Locale locale;
    public void setLocale(Locale locale) {
        this.locale = locale;
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // use a date which is related to the current users locale
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);

        UIInternalLink.make(tofill, "summary-link", 
                UIMessage.make("summary.page.title"), 
                new SimpleViewParameters(SummaryProducer.VIEW_ID));

        EvalCategoryViewParameters ecvp = (EvalCategoryViewParameters) viewparams;
        String evalCategory = ecvp.evalCategory;

        // page title
        UIMessage.make(tofill, "page-title", "showevalcategory.page.title", new Object[] {evalCategory});

        UIOutput.make(tofill, "eval-category", evalCategory);
        UIMessage.make(tofill, "eval-category-instructions", "showevalcategory.evaluation.instructions");

        // show the list of evaluations
        List<EvalEvaluation> evals = evaluationSetupService.getEvaluationsByCategory(evalCategory, null);
        if (evals.size() > 0) {
            // get an array of evaluation ids
            Long[] evalIds = new Long[evals.size()];
            for (int i=0; i<evals.size(); i++) {
                EvalEvaluation eval = (EvalEvaluation) evals.get(i);
                evalIds[i] = eval.getId();
            }

            // get all the associated groups in one big chunk (for speed)
            Map<Long, List<EvalGroup>> m = evaluationService.getEvalGroupsForEval(evalIds, false, null);

            // display each evaluation in this category
            for (int i=0; i<evals.size(); i++) {
                EvalEvaluation eval = (EvalEvaluation) evals.get(i);
                Long evaluationId = eval.getId();
                String evalStatus = evaluationService.updateEvaluationState(evaluationId); // make sure state is up to date
                UIBranchContainer evalsBranch = UIBranchContainer.make(tofill, "evaluations-list:", evaluationId.toString() );
                UIMessage.make(evalsBranch, "evaluation-header", "showevalcategory.evaluation.header");
                UIOutput.make(evalsBranch, "evaluation-title", eval.getTitle() );
                UIMessage.make(evalsBranch, "evaluation-dates", "showevalcategory.evaluation.dates", 
                        new Object[] { df.format(eval.getStartDate()), df.format(eval.getDueDate())	});

                List<EvalGroup> evalGroups = m.get(evaluationId);
                // display the groups for this evaluation
                if (evalGroups.size() > 0) {
                    for (int j=0; j<evalGroups.size(); j++) {
                        EvalGroup group = (EvalGroup) evalGroups.get(j);
                        if (EvalConstants.GROUP_TYPE_INVALID.equals(group.type)) {
                            continue; // skip processing for invalid groups on this screen
                        }

                        String evalGroupId = group.evalGroupId;
                        UIBranchContainer groupsBranch = UIBranchContainer.make(evalsBranch, "eval-group-list:", evalGroupId);
                        if (j % 2 == 0) {
                            groupsBranch.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
                        }
                        if (evalStatus.equals(EvalConstants.EVALUATION_STATE_ACTIVE)) {
                            UIInternalLink.make(groupsBranch, "eval-group-link", group.title, 
                                    new EvalViewParameters(TakeEvalProducer.VIEW_ID, evaluationId, evalGroupId, evalCategory));
                        } else {
                            // just show title
                            UIOutput.make(groupsBranch, "eval-group-title", group.title );
                        }
                    }
                } else {
                    // create a link to the evaluation directly if no groups
                    if (evalStatus.equals(EvalConstants.EVALUATION_STATE_ACTIVE)) {
                        UIInternalLink.make(evalsBranch, "evaluation-take-link", UIMessage.make("showevalcategory.evaluation.take.eval.link"), 
                                new EvalViewParameters(TakeEvalProducer.VIEW_ID, evaluationId, (String) null, evalCategory) );
                    }
                }
            }
        } else {
            UIMessage.make(tofill, "user-message", "showevalcategory.no.evaluations");
        }

    }

    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
     */
    public ViewParameters getViewParameters() {
        return new EvalCategoryViewParameters();
    }

}
