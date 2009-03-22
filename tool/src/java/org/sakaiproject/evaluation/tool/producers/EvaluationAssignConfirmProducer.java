/**
 * $Id$
 * $URL$
 * EvaluationCreateProducer.java - evaluation - Oct 05, 2006 11:32:44 AM - kahuja
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.entity.AssignGroupEntityProvider;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Show the currently assigned courses or confirm the assignment and create the evaluation
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationAssignConfirmProducer implements ViewComponentProducer, ViewParamsReporter, ActionResultInterceptor {

    public static final String VIEW_ID = "evaluation_assign_confirm";
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

    private ExternalHierarchyLogic hierLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
        hierLogic = logic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private Locale locale;
    public void setLocale(Locale locale) {
        this.locale = locale;
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
     */
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;
        if (evalViewParams.evaluationId == null) {
            throw new IllegalArgumentException("Cannot access this view unless the evaluationId is set");
        }

        String actionBean = "setupEvalBean.";
        /**
         * This is the evaluation we are working with on this page,
         * this should ONLY be read from, do not change any of these fields
         */
        EvalEvaluation evaluation = evaluationService.getEvaluationById(evalViewParams.evaluationId);
        if (evaluation == null) {
            throw new IllegalArgumentException("Could not find evaluation with this id, invalid evaluation id:" + evalViewParams.evaluationId);
        }
        Long evaluationId = evalViewParams.evaluationId;

        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        DateFormat dtf = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);

        /*
         * top links here
         */
        UIInternalLink.make(tofill, "summary-link", 
                UIMessage.make("summary.page.title"), 
                new SimpleViewParameters(SummaryProducer.VIEW_ID));

        if (userAdmin) {
            UIInternalLink.make(tofill, "administrate-link", 
                    UIMessage.make("administrate.page.title"),
                    new SimpleViewParameters(AdministrateProducer.VIEW_ID));
            UIInternalLink.make(tofill, "control-scales-link",
                    UIMessage.make("controlscales.page.title"),
                    new SimpleViewParameters(ControlScalesProducer.VIEW_ID));
        }

        UIInternalLink.make(tofill, "control-evaluations-link",
                UIMessage.make("controlevaluations.page.title"),
                new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));

        UIInternalLink.make(tofill, "eval-settings-link",
                UIMessage.make("evalsettings.page.title"),
                new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evalViewParams.evaluationId) );
        if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(evaluation.getState())) {
            // creating a new eval
            UIMessage.make(tofill, "eval-start-text", "starteval.page.title");
        }


        // normal page content

        UIMessage.make(tofill, "evalAssignInfo", "evaluationassignconfirm.eval.assign.info", 
                new Object[] {evaluation.getTitle()});

        UIMessage.make(tofill, "evalAssignInstructions", "evaluationassignconfirm.eval.assign.instructions",
                new Object[] {df.format(evaluation.getStartDate())});

        // display info about the evaluation (dates and what not)
        UIOutput.make(tofill, "startDate", dtf.format(evaluation.getStartDate()) );

        if (evaluation.getDueDate() != null) {
            UIBranchContainer branch = UIBranchContainer.make(tofill, "showDueDate:");
            UIOutput.make(branch, "dueDate", dtf.format(evaluation.getDueDate()) );
        }

        if (evaluation.getStopDate() != null) {
            UIBranchContainer branch = UIBranchContainer.make(tofill, "showStopDate:");
            UIOutput.make(branch, "stopDate", dtf.format(evaluation.getStopDate()) );
        }

        if (evaluation.getViewDate() != null) {
            UIBranchContainer branch = UIBranchContainer.make(tofill, "showViewDate:");
            UIOutput.make(branch, "viewDate", dtf.format(evaluation.getViewDate()) );
        }

        // turn the selected groups into list of normal and adhoc groups
        List<EvalGroup> normalGroups = new ArrayList<EvalGroup>();
        List<EvalGroup> adhocGroups = new ArrayList<EvalGroup>();
        String[] selectedGroupIDs = evalViewParams.selectedGroupIDs;
        if (selectedGroupIDs != null 
                && selectedGroupIDs.length > 0) {
            for (int i = 0; i < selectedGroupIDs.length; ++i) {
                String evalGroupId = selectedGroupIDs[i];
                EvalGroup group = commonLogic.makeEvalGroupObject(evalGroupId);
                if (EvalConstants.GROUP_TYPE_ADHOC.equals(group.type)) {
                    adhocGroups.add(group);
                } else {
                    normalGroups.add(group);               
                }
            }
        }

        // get all evaluator user assignments to count the total enrollments
        HashMap<String, List<EvalAssignUser>> groupIdToEAUList = new HashMap<String, List<EvalAssignUser>>();
        List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(evaluationId, null, null, 
                EvalAssignUser.TYPE_EVALUATOR, null, null, null);
        for (EvalAssignUser evalAssignUser : userAssignments) {
            String groupId = evalAssignUser.getEvalGroupId();
            if (! groupIdToEAUList.containsKey(groupId)) {
                groupIdToEAUList.put(groupId, new ArrayList<EvalAssignUser>());
            }
            groupIdToEAUList.get(groupId).add(evalAssignUser);
        }

        // show the selected groups
        if (! normalGroups.isEmpty()) {
            UIBranchContainer groupsBranch = UIBranchContainer.make(tofill, "showSelectedGroups:");
            for (EvalGroup group : normalGroups) {
                String evalGroupId = group.evalGroupId;
                UIBranchContainer groupRow = UIBranchContainer.make(groupsBranch, "groups:", evalGroupId);
                UIOutput.make(groupRow, "groupTitle", group.title);
                if (evaluationId != null) {
                    // only add in this link if the evaluation exists
                    EvalAssignGroup assignGroup = evaluationService.getAssignGroupByEvalAndGroupId(evaluationId, evalGroupId);
                    if (assignGroup != null) {
                        Long assignGroupId = assignGroup.getId();
                        UILink.make(groupRow, "directGroupLink", UIMessage.make("evaluationassignconfirm.direct.link"), 
                                commonLogic.getEntityURL(AssignGroupEntityProvider.ENTITY_PREFIX, assignGroupId.toString()));
                    }
                }
                int enrollmentCount = groupIdToEAUList.get(evalGroupId) == null ? 0 : groupIdToEAUList.get(evalGroupId).size();
                UIOutput.make(groupRow, "enrollment", enrollmentCount + "");
            }
        } else {
            UIMessage.make(tofill, "noGroupsSelected", "evaluationassignconfirm.no.groups.selected");
        }

        // show the selected hierarchy nodes
        Boolean showHierarchy = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
        if (showHierarchy) {
            UIBranchContainer hierarchyBranch = UIBranchContainer.make(tofill, "showHierarchy:");
            String[] selectedNodeIDs = evalViewParams.selectedHierarchyNodeIDs;
            if (selectedNodeIDs != null 
                    && selectedNodeIDs.length > 0) {
                UIBranchContainer nodesBranch = UIBranchContainer.make(tofill, "showSelectedNodes:");
                for (int i = 0; i < selectedNodeIDs.length; i++ ) {
                    EvalHierarchyNode node = hierLogic.getNodeById(selectedNodeIDs[i]);

                    UIBranchContainer nodeRow = UIBranchContainer.make(nodesBranch, "nodes:");
                    UIOutput.make(nodeRow, "nodeTitle", node.title);
                    UIOutput.make(nodeRow, "nodeAbbr", node.description);
                }
            } else {
                UIMessage.make(hierarchyBranch, "noNodesSelected", "evaluationassignconfirm.no.nodes.selected");
            }
        }

        // show the selected adhoc groups
        Boolean showAdhocGroups = (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_GROUPS);
        if (showAdhocGroups) {
            UIBranchContainer adhocBranch = UIBranchContainer.make(tofill, "showAdhoc:");
            UIOutput.make(adhocBranch, "showAdhocGroups:");
            if (! adhocGroups.isEmpty()) {
                for (EvalGroup group : adhocGroups) {
                    String evalGroupId = group.evalGroupId;
                    UIBranchContainer groupRow = UIBranchContainer.make(adhocBranch, "groups:", evalGroupId);
                    UIOutput.make(groupRow, "groupTitle", group.title);
                    if (evaluationId != null) {
                        // only add in this link if the evaluation exists
                        EvalAssignGroup assignGroup = evaluationService.getAssignGroupByEvalAndGroupId(evaluationId, evalGroupId);
                        if (assignGroup != null) {
                            Long assignGroupId = assignGroup.getId();
                            UILink.make(groupRow, "directGroupLink", UIMessage.make("evaluationassignconfirm.direct.link"), 
                                    commonLogic.getEntityURL(AssignGroupEntityProvider.ENTITY_PREFIX, assignGroupId.toString()));
                        }
                    }
                    int enrollmentCount = groupIdToEAUList.get(evalGroupId) == null ? 0 : groupIdToEAUList.get(evalGroupId).size();
                    UIOutput.make(groupRow, "enrollment", enrollmentCount + "");
                }
            } else {
                UIMessage.make(adhocBranch, "noAdhocSelected", "evaluationassignconfirm.no.adhoc.selected");
            }
        }

        // show submit buttons for first time evaluation creation && active or earlier
        String evalState = EvalUtils.getEvaluationState( evaluation, false );
        if ( EvalUtils.checkStateBefore(evalState, EvalConstants.EVALUATION_STATE_ACTIVE, true) ) {
            // first time evaluation creation or still in queue
            UIBranchContainer showButtons = UIBranchContainer.make(tofill, "showButtons:");
            UIForm evalAssignForm = UIForm.make(showButtons, "evalAssignForm");
            UICommand.make(evalAssignForm, "doneAssignment", UIMessage.make("evaluationassignconfirm.done.button"), 
                    actionBean + "completeConfirmAction");
            UIMessage.make(evalAssignForm, "cancel-button", "evaluationassignconfirm.changes.assigned.courses.button");

            // bind in the selected nodes and groups
            evalAssignForm.parameters.add( new UIELBinding(actionBean + "selectedGroupIDs", 
                    evalViewParams.selectedGroupIDs) );
            evalAssignForm.parameters.add( new UIELBinding(actionBean + "selectedHierarchyNodeIDs", 
                    evalViewParams.selectedHierarchyNodeIDs) );

            // also bind the evaluation id
            evalAssignForm.parameters.add( new UIELBinding(actionBean + "evaluationId", evaluationId) );
        }
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.ActionResultInterceptor#interceptActionResult(uk.org.ponder.rsf.flow.ARIResult, uk.org.ponder.rsf.viewstate.ViewParameters, java.lang.Object)
     */
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        // handles the navigation cases and passing along data from view to view
        EvalViewParameters evp = (EvalViewParameters) incoming;
        Long evalId = evp.evaluationId;
        if ("evalSettings".equals(actionReturn)) {
            result.resultingView = new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evalId);
        } else if ("evalAssign".equals(actionReturn)) {
            result.resultingView = new EvalViewParameters(EvaluationAssignProducer.VIEW_ID, evalId);
        } else if ("evalConfirm".equals(actionReturn)) {
            result.resultingView = new EvalViewParameters(EvaluationAssignConfirmProducer.VIEW_ID, evalId);
        } else if ("controlEvals".equals(actionReturn)) {
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
