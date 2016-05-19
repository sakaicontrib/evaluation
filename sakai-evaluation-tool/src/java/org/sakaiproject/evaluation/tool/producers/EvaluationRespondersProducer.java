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
package org.sakaiproject.evaluation.tool.producers;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Page for Modifying Email templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationRespondersProducer extends EvalCommonProducer implements ViewParamsReporter, ActionResultInterceptor {

    public static final String VIEW_ID = "evaluation_responders";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalDeliveryService deliveryService;
    public void setDeliveryService(EvalDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
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
    public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

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
        boolean evalAnonymous = EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl());
        boolean controlEval = evaluationService.canControlEvaluation(currentUserId, evaluationId);

        boolean allowEmailStudents = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS);
        boolean allowViewResponders = (Boolean) settings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESPONDERS);
        int responsesRequired = ((Integer) settings.get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS));
        boolean currentUserViewResponses = controlEval || allowViewResponders;

        // get the lists of participants and responses
        String[] evalGroupIds = null;
        if (evalGroupId != null) {
            evalGroupIds = new String[] {evalGroupId};
        }

        HashMap<String, List<EvalUser>> usersByGroupId = new HashMap<>();
        List<EvalResponse> responses = deliveryService.getEvaluationResponses(evaluationId, 
                evalGroupIds, null);
        HashMap<String, Map<String, EvalResponse>> groupToUserResponses = new HashMap<>();
        for (EvalResponse response : responses) {
            String groupId = response.getEvalGroupId();
            if (! groupToUserResponses.containsKey(groupId)) {
                HashMap<String, EvalResponse> userResponses = new HashMap<>();
                groupToUserResponses.put(groupId, userResponses);
            }
            String userId = response.getOwner();
            groupToUserResponses.get(groupId).put(userId, response);
        }
        boolean showStatus = ((responses.size() >= responsesRequired) || currentUserViewResponses);

        String statsAssigned;
        if (evalAnonymous) {
            // SPECIAL CASE: no group separation for anonymous evals so we list the users who have responded only
            statsAssigned = "--";
            ArrayList<EvalUser> users = new ArrayList<>();
            for (EvalResponse response : responses) {
                String userId = response.getOwner();
                EvalUser user = commonLogic.getEvalUserById(userId);
                users.add(user);
            }
            usersByGroupId.put(EvalConstants.EVALUATION_AUTHCONTROL_NONE, users);
        } else {
            List<EvalAssignUser> assignedUsers = evaluationService.getParticipantsForEval(evaluationId, 
                    null, evalGroupIds, EvalAssignUser.TYPE_EVALUATOR, null, null, null);
            statsAssigned = String.valueOf(assignedUsers.size());
            for (EvalAssignUser assignUser : assignedUsers) {
                String userId = assignUser.getUserId();
                String groupId = assignUser.getEvalGroupId();
                if (! usersByGroupId.containsKey(groupId)) {
                    ArrayList<EvalUser> users = new ArrayList<>();
                    usersByGroupId.put(groupId, users);
                }
                EvalUser user = commonLogic.getEvalUserById(userId);
                usersByGroupId.get(groupId).add(user);
            }
        }

        
        // begin page render
        UIInternalLink.make(tofill, "evalSettingsLink", UIMessage.make("evalsettings.page.title"),
                new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evaluationId) );

        UIMessage.make(tofill, "responseEval", "evalresponders.eval.info", 
                new Object[] {eval.getTitle(), 
                dateFormat.format(eval.getStartDate()), 
                dateFormat.format(eval.getSafeDueDate())} );
        UIMessage.make(tofill, "responseStats", "evalresponders.stats", 
                new Object[] {responses.size(), statsAssigned} );

        if (allowEmailStudents || userAdmin) {
            UIInternalLink.make(tofill, "responseEmailLink", 
                    UIMessage.make("evalresponders.notifications.link", new Object[] {eval.getTitle()}),
                    new EvalViewParameters(EvaluationNotificationsProducer.VIEW_ID, evaluationId) );
        }

        UIInternalLink.make(tofill, "evalRespondersLink", UIMessage.make("evalresponders.page.title"),
                new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evaluationId, evalGroupId) );

        for (Entry<String, List<EvalUser>> entry : usersByGroupId.entrySet()) {
            UIBranchContainer groupBranch = UIBranchContainer.make(tofill, "responseGroups:");
            String groupId = entry.getKey();
            Map<String, EvalResponse> userResponses = groupToUserResponses.get(groupId);
            if (userResponses == null) {
                userResponses = new HashMap<>(0);
            }
            List<EvalUser> users = entry.getValue();
            if (! evalAnonymous) {
                EvalGroup group = commonLogic.makeEvalGroupObject(groupId);
                UIMessage.make(groupBranch, "responseGroupTitle", "evalresponders.group.title", 
                        new Object[] {group.title});
                UIMessage.make(groupBranch, "responseGroupStats", "evalresponders.stats", 
                        new Object[] {userResponses.size(), users.size()});
                if (allowEmailStudents || userAdmin) {
                    UIInternalLink.make(groupBranch, "responseGroupEmailLink", 
                            UIMessage.make("evalresponders.notifications.link", new Object[] {group.title}),
                            new EvalViewParameters(EvaluationNotificationsProducer.VIEW_ID, evaluationId, groupId) );
                }
            }
            // display the list of respondents
            // sort the list of users
            Collections.sort(users, new EvalUser.SortNameComparator());
            UIBranchContainer showResponsesBranch = UIBranchContainer.make(groupBranch, "showGroupResponses:");
            for (EvalUser evalUser : users) {
                UIBranchContainer userResponseBranch = UIBranchContainer.make(showResponsesBranch, "responses:");
                UIOutput.make(userResponseBranch, "responseUser", evalUser.displayName + "(" + evalUser.username + ")");
                if (showStatus) {
                    String messagekey = "evalresponders.status.untaken"; // untaken (no response)
                    EvalResponse response = userResponses.get(evalUser.userId);
                    if (response != null) {
                        if (response.complete) {
                            messagekey = "evalresponders.status.complete";
                        } else {
                            messagekey = "evalresponders.status.incomplete";
                        }
                    }
                    UIMessage.make(userResponseBranch, "responseStatus", messagekey);
                } else if (currentUserViewResponses) {
                    // user can view but not enough responses yet
                    UIMessage.make(userResponseBranch, "responseStatus", "controlevaluations.eval.report.after.responses", 
                            new Object[] {responsesRequired});
                } else {
                    // user cannot view - period
                    UIOutput.make(userResponseBranch, "responseStatus", "-");
                }
            }
        }
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
