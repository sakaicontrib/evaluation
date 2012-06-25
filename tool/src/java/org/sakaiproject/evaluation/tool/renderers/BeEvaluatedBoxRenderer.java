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

package org.sakaiproject.evaluation.tool.renderers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.producers.EvaluationRespondersProducer;
import org.sakaiproject.evaluation.tool.producers.PreviewEvalProducer;
import org.sakaiproject.evaluation.tool.producers.ReportChooseGroupsProducer;
import org.sakaiproject.evaluation.tool.producers.ReportsViewingProducer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;

/**
 * BeEvaluatedBoxRender renders the list of evaluations where the user may be evaluated.
 * 
 * @author mgillian
 * @author azeckoski
 */
public class BeEvaluatedBoxRenderer {

    private DateFormat df;
    private boolean allowListOfTakers = false;

    private Locale locale;
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationSetupService evaluationSetupService;
    public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
        this.evaluationSetupService = evaluationSetupService;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private HumanDateRenderer humanDateRenderer;
    public void setHumanDateRenderer(HumanDateRenderer humanDateRenderer) {
        this.humanDateRenderer = humanDateRenderer;
    }

    private EvalDeliveryService deliveryService;
    public void setDeliveryService(EvalDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    private EvalBeanUtils evalBeanUtils;
    public void setEvalBeanUtils(EvalBeanUtils evalBeanUtils) {
        this.evalBeanUtils = evalBeanUtils;
    }

    public void init() {
        df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        allowListOfTakers = (Boolean) settings.get(EvalSettings.ENABLE_LIST_OF_TAKERS_EXPORT);
    }

    public void renderBox(UIContainer tofill, String currentUserId) {
        // need to determine if there are any evals in which the user can be evaluated
        // this will not return any deleted or partial evals but it will return closed ones so you need to check which ones to show depending on the use cases
        List<EvalEvaluation> evalsForInstructor = this.evaluationSetupService.getEvaluationsForEvaluatee(currentUserId, true);
        if (evalsForInstructor == null || evalsForInstructor.isEmpty()) {
            // no evals found so show nothing
        } else {
            // evals found; show the widget
            UIBranchContainer evalResponsesBC = UIBranchContainer.make(tofill, "evalResponsesBox:");

            // re-sort the evals so closed ones are at the end
            evalsForInstructor = EvalUtils.sortClosedEvalsToEnd(evalsForInstructor);
            // split into "in progress" and "closed"
            List<EvalEvaluation> evalsInProgress = new ArrayList<EvalEvaluation>();
            List<EvalEvaluation> evalsClosed = new ArrayList<EvalEvaluation>();
            for (EvalEvaluation eval : evalsForInstructor) {
                if (EvalUtils.checkStateAfter(eval.getState(), EvalConstants.EVALUATION_STATE_CLOSED, true)) {
                    evalsClosed.add(eval);
                } else {
                    evalsInProgress.add(eval);
                }
            }

            // show a list of evals with 5 columns
            if (evalsInProgress.isEmpty()) {
                UIMessage.make(evalResponsesBC, "summary-be-evaluated-progress-none", "summary.be.evaluated.none");
            } else {
                UIBranchContainer evaluatedInProgressBC = UIBranchContainer.make(evalResponsesBC, "evaluatedInProgress:");
                makeEvalsListTable(evalsInProgress, evaluatedInProgressBC);
            }

            // show a list of evals with 5 columns
            if (evalsClosed.isEmpty()) {
                UIMessage.make(evalResponsesBC, "summary-be-evaluated-closed-none", "summary.be.evaluated.none");
            } else {
                UIBranchContainer evaluatedClosedBC = UIBranchContainer.make(evalResponsesBC, "evaluatedClosed:");
                makeEvalsListTable(evalsClosed, evaluatedClosedBC);
            }

        }// there are evals for instructor
    }

    private void makeEvalsListTable(List<EvalEvaluation> evals, UIBranchContainer container) {
        // add in the table heading
        UIBranchContainer evalResponseTable = UIBranchContainer.make(container, "evalResponseTable:");
        for (EvalEvaluation eval : evals) {
            // set display values for this eval
            String evalState = commonLogic.calculateViewability(eval.getState());
            // show one link per group assigned to in-queue, active or grace period eval
            List<EvalGroup> groups = eval.getEvalGroups();
            if (groups == null) {
                groups = new ArrayList<EvalGroup>();
            }
            for (EvalGroup group : groups) {
                UIBranchContainer evalrow = UIBranchContainer.make(evalResponseTable, "evalResponsesList:");
                UIOutput.make(evalrow, "evalResponsesStartDate", df.format(eval.getStartDate()));
                humanDateRenderer.renderDate(evalrow, "evalResponsesDueDate", eval.getDueDate());

                String title = EvalUtils.makeMaxLengthString(group.title + " " + eval.getTitle() + " ", 50);
                UIInternalLink.make(evalrow, "evalResponsesTitleLink_preview", title, 
                        new EvalViewParameters(PreviewEvalProducer.VIEW_ID, eval.getId(), group.evalGroupId));

                // NOTE: much of this code is replicated/derived from ControlEvaluationsProducer.java
                int responsesCount = deliveryService.countResponses(eval.getId(), group.evalGroupId, true);
                int enrollmentsCount = evaluationService.countParticipantsForEval(eval.getId(), new String[] { group.evalGroupId });
                int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
                String responseString = EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount);
                UIOutput.make(evalrow, "evalResponsesStatsDisplay", responseString);

                // now handle the results viewing flags (i.e. filter out evals the instructor should not see)
                boolean instViewResultsEval = evalBeanUtils.checkInstructorViewResultsForEval(eval);

                makeDateComponent(evalrow, eval, evalState, "evalResponsesDateLabel", "evalResponsesDate", "evalResponsesStatus", 
                        instViewResultsEval, responsesCount, enrollmentsCount, responsesNeeded);

                if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(EvalUtils.getEvaluationState(eval, false)) ) {
                    if ( responsesNeeded == 0 ) {
                        // have enough responses and this is viewable
                        UIInternalLink.make(evalrow, "evalReportDisplayLink", UIMessage.make("summary.responses.report.link"), 
                                new ReportParameters(ReportsViewingProducer.VIEW_ID, eval.getId(), new String[] { group.evalGroupId }));
                        if (allowListOfTakers) {
                            // also show the list of respondents if that option is enabled
                            UIInternalLink.make(evalrow, "evalRespondentsDisplayLink", UIMessage.make("summary.responses.respondents.link"), 
                                    new EvalViewParameters( EvaluationRespondersProducer.VIEW_ID, eval.getId(), group.evalGroupId ) );
                        }
                    } else {
                        // cannot view yet, more responses needed
                        UIMessage resultOutput = UIMessage.make(evalrow, "evalReportDisplay", "controlevaluations.eval.report.awaiting.responses", 
                                new Object[] { responsesNeeded });
                        // indicate the viewable date as well
                        resultOutput.decorate(new UITooltipDecorator(
                                UIMessage.make("controlevaluations.eval.report.viewable.on", new Object[] { df.format(eval.getSafeViewDate()) }) ));
                    }
                } else {
                    // just display the date at which results will be viewable
                    String viewableDate = df.format(eval.getSafeViewDate());
                    UIOutput resultOutput = UIOutput.make(evalrow, "evalReportDisplay", viewableDate );
                    if ( responsesNeeded == 0 ) {
                        // just show date if we have enough responses
                        resultOutput.decorate(new UITooltipDecorator(
                                UIMessage.make("controlevaluations.eval.report.viewable.on", new Object[] { viewableDate }) ));
                    } else {
                        // show if responses are still needed
                        resultOutput.decorate(new UITooltipDecorator( 
                                UIMessage.make("controlevaluations.eval.report.awaiting.responses", new Object[] { responsesNeeded }) ));
                    }
                }

            }// link per group
        }
    }

    /**
     * @param evalrow
     * @param eval
     * @param evalState
     * @param evalDateLabel
     * @param evalDateItem
     * @param evalStatusItem
     * @param instViewResultsEval if true, instructor can view results for this eval, else instructor cannot view them
     * @param responsesCount
     * @param enrollmentsCount
     * @param responsesNeeded
     */
    protected void makeDateComponent(UIContainer evalrow, EvalEvaluation eval, String evalState, String evalDateLabel, String evalDateItem,
            String evalStatusItem, boolean instViewResultsEval, int responsesCount, int enrollmentsCount, int responsesNeeded) {
        // use a date which is related to the current users locale
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        if (EvalConstants.EVALUATION_STATE_INQUEUE.equals(evalState)) {
            // If we are in the queue we are yet to start, so say when we will
            UIMessage.make(evalrow, evalDateLabel, "summary.label.starts");
            UIOutput.make(evalrow, evalDateItem, df.format(eval.getStartDate()));

        } else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(evalState)) {
            // Active evaluations can either be open forever or close at some point:
            if (eval.getDueDate() != null) {
                UIMessage.make(evalrow, evalDateLabel, "summary.label.due");
                UIOutput.make(evalrow, evalDateItem, df.format(eval.getDueDate()));
                // Should probably add something here if there's a grace period
            } else {
                UIMessage.make(evalrow, evalDateLabel, "summary.label.nevercloses");
            }

        } else if (EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(evalState)) {
            // Evaluations can have a grace period, if so that must close at some point;
            // Grace periods never remain open forever
            UIMessage.make(evalrow, evalDateLabel, "summary.label.gracetill");
            UIOutput.make(evalrow, evalDateItem, df.format(eval.getSafeStopDate()));

        } else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(evalState)) {
            // if an evaluation is closed then ViewDate must have been set
            UIMessage.make(evalrow, evalDateLabel, "summary.label.resultsviewableon");
            UIOutput.make(evalrow, evalDateItem, df.format(eval.getSafeViewDate()));

        } else if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalState)) {
            // if an evaluation is viewable we have to check the instructor view date
            UIMessage.make(evalrow, evalDateLabel, "summary.label.resultsviewablesince");
            UIOutput.make(evalrow, evalDateItem, df.format(eval.getSafeViewDate()));

        } else {
            UIMessage.make(evalrow, evalDateLabel, "summary.label.fallback");
            UIOutput.make(evalrow, evalDateItem, df.format(eval.getStartDate()));
        }
        // SPECIAL handling for viewing results
        if (instViewResultsEval) {
            // results can be viewed
            if (responsesNeeded == 0) {
                UIInternalLink.make(evalrow, "viewReportLink", UIMessage.make("viewreport.page.title"), new ReportParameters(
                        ReportChooseGroupsProducer.VIEW_ID, eval.getId()));
            } else {
                UIMessage.make(evalrow, evalStatusItem, "summary.status." + evalState).decorate(
                        new UITooltipDecorator(UIMessage.make("controlevaluations.eval.report.awaiting.responses", new Object[] { responsesNeeded })));
            }
        } else {
            // fallback when results cannot be viewed
            UIMessage.make(evalrow, evalStatusItem, "summary.status." + evalState);
        }
    }
}
