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
import java.util.List;
import java.util.Locale;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.tool.producers.TakeEvalProducer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;

public class EvaluateBoxRenderer {
    private DateFormat df;
    private Locale locale;
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    private EvalDeliveryService deliveryService;
    public void setDeliveryService(EvalDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalEvaluationSetupService evaluationSetupService;
    public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
        this.evaluationSetupService = evaluationSetupService;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private HumanDateRenderer humanDateRenderer;
    public void setHumanDateRenderer(HumanDateRenderer humanDateRenderer) {
        this.humanDateRenderer = humanDateRenderer;
    }

    public void init() {
        df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);        
    }

    public void renderBox(UIContainer tofill, String currentUserId) {
        List<EvalEvaluation> evalsToTake = evaluationSetupService.getEvaluationsForUser(currentUserId, true, null, null);
        String currentGroup = commonLogic.getCurrentEvalGroup();
        UIBranchContainer evalBC = UIBranchContainer.make(tofill, "evaluationsBox:");
        if (evalsToTake.size() > 0) {

            // build an array of evaluation ids
            Long[] evalIds = new Long[evalsToTake.size()];
            for (int i = 0; i < evalsToTake.size(); i++) {
                evalIds[i] = ((EvalEvaluation) evalsToTake.get(i)).getId();
            }

            List<EvalResponse> evalResponses = deliveryService.getEvaluationResponsesForUser(currentUserId, evalIds, null);

            // This container may want a rework.  "hello" is not required, just need a unique value
            // at the end.  Not sure a UIBranchContainer is the correct thing here; I was reusing
            // some old code.
            UIBranchContainer evalrow = UIBranchContainer.make(evalBC, "evaluationsList:", "hello");
            UIOutput.make(evalrow, "evaluationTitleTitle", "Evaluation");

            for( EvalEvaluation eval : evalsToTake )
            {
                // make sure state is up to date http://jira.sakaiproject.org/browse/EVALSYS-1013
                String evalState = evaluationService.returnAndFixEvalState(eval, true); 
                // skip evaluations that are in a non-active state
                if(! EvalConstants.EVALUATION_STATE_ACTIVE.equals(evalState)){
                    continue;
                }

                for (EvalAssignGroup eag : eval.getEvalAssignGroups()) {
                    EvalGroup group = commonLogic.makeEvalGroupObject(eag.getEvalGroupId());
                    if (EvalConstants.GROUP_TYPE_INVALID.equals(group.type)) {
                        continue; // skip processing for invalid groups
                    }

                    String groupId = group.evalGroupId;
                    String title = humanDateRenderer.renderEvalTitle(eval, group);// EvalUtils.makeMaxLengthString(group.title + " " + eval.getTitle() + " ", 50);
                    String status;

                    // find the object in the list matching the evalGroupId and evalId,
                    // leave as null if not found -AZ
                    EvalResponse response = null;
                    for (int k = 0; k < evalResponses.size(); k++) {
                        EvalResponse er = (EvalResponse) evalResponses.get(k);
                        if (groupId.equals(er.getEvalGroupId())
                            && eval.getId().equals(er.getEvaluation().getId())) {
                            response = er;
                            break;
                        }
                    }

                    if (groupId.equals(currentGroup)) {
                        // TODO - do something when the evalGroupId matches
                    }

                    UIBranchContainer evalcourserow = UIBranchContainer.make(evalrow, "evaluationsCourseList:", groupId);

                    // set status
                    if (response != null && response.getEndTime() != null) {
                        // there is a response for this eval/group
                        status = "summary.status.completed";
                        if (eval.getModifyResponsesAllowed()) {
                            // can modify responses so show the link still
                            // take eval link when pending
                            UIInternalLink.make(evalcourserow, "evaluationCourseLink", title,
                                                                                       new EvalViewParameters(TakeEvalProducer.VIEW_ID,
                                                                                               eval.getId(), response.getId(), groupId));
                        } else {
                            // show title only when completed and cannot
                            // modify
                            UIOutput.make(evalcourserow, "evaluationCourseLink_disabled", title);
                        }
                    } else if (response != null && response.getEndTime() == null) {
                        // there is an in progress for this eval/group
                        UIInternalLink.make(evalcourserow, "evaluationCourseLink", title,
                                                                                   new EvalViewParameters(TakeEvalProducer.VIEW_ID,
                                                                                           eval.getId(), response.getId(), groupId));
                        status = "summary.status.inprogress";
                    } else {
                        // no response yet for this eval/group
                        // take eval link when pending
                        UIInternalLink.make(evalcourserow, "evaluationCourseLink", title,
                                                                                   new EvalViewParameters(TakeEvalProducer.VIEW_ID, eval.getId(),
                                                                                           groupId));
                        status = "summary.status.pending";
                    }
                    UIMessage.make(evalcourserow, "evaluationCourseStatus", status);
                    // moved down here as requested by UI design
                    UIOutput.make(evalcourserow, "evaluationStartDate", df.format(eval.getStartDate()));
                    humanDateRenderer.renderDate(evalcourserow, "evaluationDueDate", eval.getDueDate());
                }
            }

        } else {
            UIMessage.make(tofill, "evaluationsNone", "summary.evaluations.none");
        }
    }

}
