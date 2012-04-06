/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2012 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the
"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Michael Gillian (mgillian@unicon.net)
 **********************************************************************************/

package org.sakaiproject.evaluation.tool.renderers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.sakaiproject.evaluation.tool.producers.PreviewEvalProducer;
import org.sakaiproject.evaluation.tool.producers.ReportChooseGroupsProducer;
import org.sakaiproject.evaluation.tool.producers.ReportsViewingProducer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
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
    }

    public void renderBox(UIContainer tofill, String currentUserId) {
        boolean evalsToShow = false;
        // need to determine if there are any evals in which the user can be evaluated
        // this will not return any deleted or partial evals but it will return closed ones so you need to check which ones to show depending on the use cases
        List<EvalEvaluation> evalsForInstructor = this.evaluationSetupService.getEvaluationsForEvaluatee(currentUserId);
        if (evalsForInstructor == null || evalsForInstructor.isEmpty()) {
            // no evals found so show nothing
        } else {
            // evals found; show the widget
            UIBranchContainer evalResponsesBC = UIBranchContainer.make(tofill, "evalResponsesBox:");
            UIForm evalResponsesForm = UIForm.make(evalResponsesBC, "evalResponsesForm");

            UIBranchContainer evalResponseTable = null;

            // re-sort the evals so closed ones are at the end
            evalsForInstructor = EvalUtils.sortClosedEvalsToEnd(evalsForInstructor);

            // show a list of evals with four columns:
            for (EvalEvaluation eval : evalsForInstructor) {
                evalsToShow = true;
                if (evalResponseTable == null) {
                    evalResponseTable = UIBranchContainer.make(evalResponsesForm, "evalResponseTable:");
                    // show four column headings

                }
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

                    int responsesCount = deliveryService.countResponses(eval.getId(), group.evalGroupId, true);
                    int enrollmentsCount = evaluationService.countParticipantsForEval(eval.getId(), new String[] { group.evalGroupId });
                    int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
                    String responseString = EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount);

                    // now handle the results viewing flags (i.e. filter out evals the instructor should not see)
                    boolean instViewResultsEval = evalBeanUtils.checkInstructorViewResultsForEval(eval);

                    makeDateComponent(evalrow, eval, evalState, "evalResponsesDateLabel", "evalResponsesDate", "evalResponsesStatus", 
                            instViewResultsEval, responsesCount, enrollmentsCount, responsesNeeded);

                    if (EvalUtils.checkStateAfter(evalState, EvalConstants.EVALUATION_STATE_CLOSED, true)) {
                        if (responsesNeeded == 0) {
                            UIInternalLink.make(evalrow, "evalResponsesDisplayLink", UIMessage.make("controlevaluations.eval.responses.inline",
                                    new Object[] { responseString }), new ReportParameters(ReportsViewingProducer.VIEW_ID, eval.getId(),
                                    new String[] { group.evalGroupId }));
                        } else {
                            UIOutput responseOutput = UIOutput.make(evalrow, "evalResponsesDisplay", responseString);
                            responseOutput.decorate(new UITooltipDecorator(UIMessage.make("controlevaluations.eval.report.awaiting.responses",
                                    new Object[] { responsesNeeded })));
                        }
                    } else {
                        UIOutput.make(evalrow, "evalResponsesDisplay", responseString);
                    }
                }// link per group
            }// for evals iterator
            if (!evalsToShow) {
                UIMessage.make(evalResponsesForm, "summary-be-evaluated-none", "summary.be.evaluated.none");
            }
        }// there are evals for instructor
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
            // If we are in the queue we are yet to start,
            // so say when we will
            UIMessage.make(evalrow, evalDateLabel, "summary.label.starts");
            UIOutput.make(evalrow, evalDateItem, df.format(eval.getStartDate()));

            UIMessage.make(evalrow, evalStatusItem, "summary.status." + evalState);
        } else if (EvalConstants.EVALUATION_STATE_ACTIVE.equals(evalState)) {
            // Active evaluations can either be open forever or close at
            // some point:
            if (eval.getDueDate() != null) {
                UIMessage.make(evalrow, evalDateLabel, "summary.label.due");
                UIOutput.make(evalrow, evalDateItem, df.format(eval.getDueDate()));
                // Should probably add something here if there's a grace period
            } else {
                UIMessage.make(evalrow, evalDateLabel, "summary.label.nevercloses");
            }

            UIMessage.make(evalrow, evalStatusItem, "summary.status." + evalState);
        } else if (EvalConstants.EVALUATION_STATE_GRACEPERIOD.equals(evalState)) {
            // Evaluations can have a grace period, if so that must close at some point;
            // Grace periods never remain open forever
            UIMessage.make(evalrow, evalDateLabel, "summary.label.gracetill");
            UIOutput.make(evalrow, evalDateItem, df.format(eval.getSafeStopDate()));

            UIMessage.make(evalrow, evalStatusItem, "summary.status." + evalState);
        } else if (EvalConstants.EVALUATION_STATE_CLOSED.equals(evalState)) {
            // if an evaluation is closed then it is not yet viewable and ViewDate must have been set
            UIMessage.make(evalrow, evalDateLabel, "summary.label.resultsviewableon");
            UIOutput.make(evalrow, evalDateItem, df.format(eval.getSafeViewDate()));

            UIMessage.make(evalrow, evalStatusItem, "summary.status." + evalState);
        } else if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals(evalState)) {
            // if an evaluation is viewable we have to check the instructor view date
            UIMessage.make(evalrow, evalDateLabel, "summary.label.resultsviewablesince");
            UIOutput.make(evalrow, evalDateItem, df.format(eval.getSafeViewDate()));

            if (responsesNeeded == 0) {
                UIInternalLink.make(evalrow, "viewReportLink", UIMessage.make("viewreport.page.title"), new ReportParameters(
                        ReportChooseGroupsProducer.VIEW_ID, eval.getId()));
            } else {
                UIMessage.make(evalrow, evalStatusItem, "summary.status." + evalState).decorate(
                        new UITooltipDecorator(UIMessage.make("controlevaluations.eval.report.awaiting.responses", new Object[] { responsesNeeded })));
            }
        } else {
            UIMessage.make(evalrow, evalDateLabel, "summary.label.fallback");
            UIOutput.make(evalrow, evalDateItem, df.format(eval.getStartDate()));
        }
    }
}
