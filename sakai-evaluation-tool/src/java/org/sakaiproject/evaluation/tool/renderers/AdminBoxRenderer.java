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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.producers.ControlEvaluationsProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationSettingsProducer;
import org.sakaiproject.evaluation.tool.producers.PreviewEvalProducer;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * AdminBoxRender renders the list of Evaluations that the user administers.
 * 
 * @author mgillian
 * @author azeckoski
 */
public class AdminBoxRenderer {

    private static final Log LOG = LogFactory.getLog(AdminBoxRenderer.class);

    private DateFormat df;

    private Locale locale;

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationSetupService evaluationSetupService;
    public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
        this.evaluationSetupService = evaluationSetupService;
    }

    private EvalDeliveryService deliveryService;
    public void setDeliveryService(EvalDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalBeanUtils evalBeanUtils;
    public void setEvalBeanUtils(EvalBeanUtils evalBeanUtils) {
       this.evalBeanUtils = evalBeanUtils;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
       this.settings = settings;
    }

    private HumanDateRenderer humanDateRenderer;
    public void setHumanDateRenderer(HumanDateRenderer humanDateRenderer) {
        this.humanDateRenderer = humanDateRenderer;
    }

    public void init() {
        df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
    }

    public static final String COMPONENT_ID = "evalAdminBox:";

    public UIJointContainer renderItem(UIContainer parent, String ID) {
        UIJointContainer container = new UIJointContainer(parent, ID, COMPONENT_ID);
        renderBox(container);
        return container;
    }

    private void renderBox(UIContainer tofill) {
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean userReadonlyAdmin = commonLogic.isUserReadonlyAdmin(currentUserId);
        boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("currentUserId=" + currentUserId + ", userAdmin=" + userAdmin + ", userReadonlyAdmin=" + userReadonlyAdmin + ", beginEvaluation=" + beginEvaluation);
        }

        List<EvalEvaluation> evals = evaluationSetupService.getVisibleEvaluationsForUser(currentUserId, true, false, false);

        /*
         * If the person is an admin, then just point new evals to existing object. If the person is
         * not an admin then only show owned evals
         * 
         * NOTE: no longer showing evals they do not own (removed the
         * INSTRUCTOR_ALLOWED_VIEW_RESULTS handling)
         * 
         * EVALSYS-1437: readonly admin can view evals like a normal admin can
         */

        if (!userAdmin && !userReadonlyAdmin) {
            List<EvalEvaluation> newEvals = new ArrayList<>();
            if (LOG.isDebugEnabled()) {
                LOG.debug("non-admin special case: " + evals.size() + " evals, " + EvalUtils.getEvalIdsFromEvaluations(evals));
            }
            for (EvalEvaluation evaluation : evals) {
                // Add the owned evals ONLY
                if (currentUserId.equals(evaluation.getOwner())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("non-admin special case: OWNER, id=" + evaluation.getId());
                    }
                    newEvals.add(evaluation);
                }
            }
            evals = newEvals;
        }

        if (!evals.isEmpty()) {
            // sort evaluations by due date (newest first)
            Collections.sort(evals, new ComparatorsUtils.EvaluationDueDateComparator());
            evals = EvalUtils.sortClosedEvalsToEnd(evals);

            boolean viewResultsIgnoreDates = (Boolean) settings.get(EvalSettings.VIEW_SURVEY_RESULTS_IGNORE_DATES);
            int responsesRequired = ((Integer) settings.get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS));

            UIBranchContainer evalAdminBC = UIBranchContainer.make(tofill, "evalAdminBoxContents:");
            // Temporary fix for http://www.caret.cam.ac.uk/jira/browse/CTL-583
            // (need to send them to the eval control page eventually) -AZ
            if (beginEvaluation) {
                UIInternalLink.make(evalAdminBC, "evaladmin-title-link", UIMessage.make("summary.evaluations.admin"), new SimpleViewParameters(
                        ControlEvaluationsProducer.VIEW_ID));
            } else {
                UIMessage.make(evalAdminBC, "evaladmin-title", "summary.evaluations.admin");
            }
            UIForm evalAdminForm = UIForm.make(evalAdminBC, "evalAdminForm");

            // get the eval groups
            Long[] evalIds = new Long[evals.size()];
            int i = 0;
            for (EvalEvaluation eval : evals) {
                evalIds[i++] = eval.getId();
            }
            // WARNING: this retrieves ALL groups for the evaluation so it is ONLY safe for eval
            // admins
            Map<Long, List<EvalGroup>> evalGroups = evaluationService.getEvalGroupsForEval(evalIds, false, null);

            for( EvalEvaluation eval : evals )
            {
                String evalState = evaluationService.returnAndFixEvalState(eval, true);
                evalState = commonLogic.calculateViewability(evalState);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("eval=" + eval.getId() + ", state=" + evalState + ", title=" + eval.getTitle());
                }

                // 1) if a evaluation is queued, title link go to EditSettings page with populated
                // data
                // 2) if a evaluation is active, title link go to EditSettings page with populated
                // data but start date should be disabled
                // 3) if a evaluation is closed, title link go to previewEval page with populated
                // data
                List<EvalGroup> groups = evalGroups.get(eval.getId());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("eval (" + eval.getId() + ") groups (" + groups.size() + "): " + EvalUtils.getGroupIdsFromGroups(groups));
                }
                for (EvalGroup group : groups) {
                    UIBranchContainer evalrow = UIBranchContainer.make(evalAdminForm, "evalAdminList:", eval.getId().toString());

                    String title = humanDateRenderer.renderEvalTitle(eval, group);
                    
                    if(userReadonlyAdmin) {
                        // only ever show the preview
                        UIInternalLink evalTitleLink = UIInternalLink.make(evalrow, "evalAdminTitleLink_preview", title, new EvalViewParameters(
                                PreviewEvalProducer.VIEW_ID, eval.getId(), eval.getTemplate().getId()));
                        evalTitleLink.decorate( new UITooltipDecorator( UIMessage.make("controlevaluations.eval.title.tooltip")) );
                    } else {
                        if (EvalUtils.checkStateAfter(evalState, EvalConstants.EVALUATION_STATE_CLOSED, true)) {
                            // only preview after the eval closes
                            UIInternalLink evalTitleLink = UIInternalLink.make(evalrow, "evalAdminTitleLink_preview", title, new EvalViewParameters(
                                    PreviewEvalProducer.VIEW_ID, eval.getId(), eval.getTemplate().getId()));
                            evalTitleLink.decorate( new UITooltipDecorator( UIMessage.make("controlevaluations.eval.title.tooltip")) );
                        } else {
                            // edit while the eval is open
                            UIInternalLink.make(evalrow, "evalAdminTitleLink_edit", title, new EvalViewParameters(
                                    EvaluationSettingsProducer.VIEW_ID, eval.getId()));
                        }
                    }

                    humanDateRenderer.renderDate(evalrow, "evalAdminStartDate", eval.getStartDate());
                    humanDateRenderer.renderDate(evalrow, "evalAdminDueDate", eval.getSafeDueDate());

                    String[] groupIds = { group.evalGroupId };
                    int responsesCount = deliveryService.countResponses(eval.getId(), group.evalGroupId, true);
                    int enrollmentsCount = evaluationService.countParticipantsForEval(eval.getId(), groupIds);
                    int responsesNeeded = evalBeanUtils.getResponsesNeededToViewForResponseRate(responsesCount, enrollmentsCount);
                    String responseString = EvalUtils.makeResponseRateStringFromCounts(responsesCount, enrollmentsCount);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("group responses=" + responsesCount + ", enrollments=" + enrollmentsCount + ", str=" + responseString);
                    }

                    boolean allowedViewResponders = true;
                    boolean allowedEmailStudents = true;
                    if(userReadonlyAdmin) {
                        allowedViewResponders = false;
                        allowedEmailStudents = false;
                    }
                    RenderingUtils.renderReponseRateColumn(evalrow, eval.getId(), responsesNeeded,
                                                                                  responseString, allowedViewResponders, allowedEmailStudents);
                    
                    // owner can view the results but only early IF the setting is enabled
                    boolean viewResultsEval = viewResultsIgnoreDates ? true : EvalUtils.checkStateAfter(evalState, EvalConstants.EVALUATION_STATE_VIEWABLE, true);
                    // now render the results links depending on what the user is allowed to see
                    RenderingUtils.renderResultsColumn(evalrow, eval, null, eval.getSafeViewDate(), df,
                                                                                                    responsesNeeded, responsesRequired, viewResultsEval);
                }
            }
        }
    }
}
