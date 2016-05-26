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
import java.util.Date;
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
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;

/**
 * BeEvaluatedBoxRender renders the list of evaluations where the user may be evaluated.
 * 
 * @author mgillian
 * @author azeckoski
 */
public class BeEvaluatedBoxRenderer {

    private DateFormat df;
    private boolean allowEmailStudents = false;
    private boolean allowViewResponders = false;

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
        allowEmailStudents = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS);
        allowViewResponders = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESPONDERS);
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
            List<EvalEvaluation> evalsInProgress = new ArrayList<>();
            List<EvalEvaluation> evalsClosed = new ArrayList<>();
            for (EvalEvaluation eval : evalsForInstructor) {
                if (EvalUtils.checkStateAfter(eval.getState(), EvalConstants.EVALUATION_STATE_CLOSED, true)) {
                    evalsClosed.add(eval);
                } else {
                    evalsInProgress.add(eval);
                }
            }

            // show a list of evals with 5 columns
            UIBranchContainer inProgressBC = UIBranchContainer.make(evalResponsesBC, "summary-be-evaluated-progress-header:");
            if (evalsInProgress.isEmpty()) {
                UIMessage.make(inProgressBC, "summary-be-evaluated-progress-none", "summary.be.evaluated.none");
                inProgressBC.decorate( new UIStyleDecorator("triangle-closed") ); // must match the existing CSS class
            } else {
                inProgressBC.decorate( new UIStyleDecorator("triangle-open") ); // must match the existing CSS class
                UIBranchContainer evaluatedInProgressBC = UIBranchContainer.make(evalResponsesBC, "evaluatedInProgress:");
                makeEvalsListTable(evalsInProgress, evaluatedInProgressBC);
            }

            // show a list of evals with 5 columns
            UIBranchContainer closedBC = UIBranchContainer.make(evalResponsesBC, "summary-be-evaluated-closed-header:");
            if (evalsClosed.isEmpty()) {
                UIMessage.make(closedBC, "summary-be-evaluated-closed-none", "summary.be.evaluated.none");
                closedBC.decorate( new UIStyleDecorator("triangle-closed") ); // must match the existing CSS class
            } else {
                closedBC.decorate( new UIStyleDecorator("triangle-open") ); // must match the existing CSS class
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
            boolean instViewResultsEval = evalBeanUtils.checkInstructorViewResultsForEval(eval, evalState);
            Date instViewDate = evalBeanUtils.getInstructorViewDateForEval(eval);

            // show one link per group assigned to in-queue, active or grace period eval
            List<EvalGroup> groups = eval.getEvalGroups();
            if (groups == null) {
                groups = new ArrayList<>();
            }
            for (EvalGroup group : groups) {
                UIBranchContainer evalrow = UIBranchContainer.make(evalResponseTable, "evalResponsesList:");
                humanDateRenderer.renderDate(evalrow, "evalResponsesStartDate", eval.getStartDate());
                humanDateRenderer.renderDate(evalrow, "evalResponsesDueDate", eval.getSafeDueDate());

                String title = humanDateRenderer.renderEvalTitle(eval, group);
                UIInternalLink.make(evalrow, "evalResponsesTitleLink_preview", title, 
                        new EvalViewParameters(PreviewEvalProducer.VIEW_ID, eval.getId(), group.evalGroupId));

                // NOTE: much of this code is replicated/derived from ControlEvaluationsProducer.java
                int responsesCount = deliveryService.countResponses(eval.getId(), group.evalGroupId, true);
                int enrollmentsCount = evaluationService.countParticipantsForEval(eval.getId(), new String[] { group.evalGroupId });
                int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
                int responsesRequired = ((Integer) settings.get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS));
                String responseString = EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount);

                // render the response rates depending on permissions
                RenderingUtils.renderReponseRateColumn(evalrow, eval.getId(), responsesNeeded, 
                        responseString, allowViewResponders, allowEmailStudents);

                // now render the results links depending on what the instructor is allowed to see
                RenderingUtils.renderResultsColumn(evalrow, eval, group, instViewDate, df, 
                        responsesNeeded, responsesRequired, instViewResultsEval);

            }// link per group
        }
    }

}
