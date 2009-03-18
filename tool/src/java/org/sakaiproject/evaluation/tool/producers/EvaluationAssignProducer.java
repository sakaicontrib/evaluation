/**
 * EvaluationAssignProducer.java - evaluation - Sep 18, 2006 11:35:56 AM - azeckoski
 * $URL$
 * $Id$
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.renderers.HierarchyTreeNodeSelectRenderer;
import org.sakaiproject.evaluation.tool.viewparams.AdhocGroupParams;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;

import uk.org.ponder.htmlutil.HTMLUtil;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;

/**
 * View for assigning an evaluation to groups and hierarchy nodes. 
 * 
 * This Producer has instance variables for tracking state of the view, and
 * should never be reused or used as a singleton.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Steve Githens (sgithens@caret.cam.ac.uk)
 */
@SuppressWarnings("deprecation")
public class EvaluationAssignProducer implements ViewComponentProducer, ViewParamsReporter, ActionResultInterceptor {

    public static final String VIEW_ID = "evaluation_assign";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

    private HierarchyTreeNodeSelectRenderer hierUtil;
    public void setHierarchyRenderUtil(HierarchyTreeNodeSelectRenderer util) {
        hierUtil = util;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
        this.hierarchyLogic = logic;
    }

    private ViewStateHandler vsh;
    public void setViewStateHandler(ViewStateHandler vsh) {
        this.vsh = vsh;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic bean) {
        this.commonLogic = bean;
    }

    /**
     * Instance Variables for building up rendering information.
     */
    private StringBuilder initJS = new StringBuilder();

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();

        // render top links
        renderTopLinks(tofill, currentUserId);

        EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;
        if (evalViewParams.evaluationId == null) {
            throw new IllegalArgumentException("Cannot access this view unless the evaluationId is set");
        }

        /**
         * This is the evaluation we are working with on this page,
         * this should ONLY be read from, do not change any of these fields
         */
        EvalEvaluation evaluation = evaluationService.getEvaluationById(evalViewParams.evaluationId);

        UIInternalLink.make(tofill, "eval-settings-link",
                UIMessage.make("evalsettings.page.title"),
                new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evalViewParams.evaluationId) );
        if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(evaluation.getState())) {
            // creating a new eval
            UIMessage.make(tofill, "eval-start-text", "starteval.page.title");
        }

        UIMessage.make(tofill, "assign-eval-edit-page-title", "assigneval.assign.page.title", new Object[] {evaluation.getTitle()});
        UIMessage.make(tofill, "assign-eval-instructions", "assigneval.assign.instructions", new Object[] {evaluation.getTitle()});

        // this is a get form which does not submit to a backing bean
        EvalViewParameters formViewParams = (EvalViewParameters) evalViewParams.copyBase();
        formViewParams.viewID = EvaluationAssignConfirmProducer.VIEW_ID;

        /* 
         * About this form.
         * 
         * This is a GET form that has 2 UISelects, one for Hierarchy Nodes, and
         * one for Eval Groups (which includes adhoc groups).  They are interspered
         * and mixed together. In order to do this easily we pass in empty String
         * arrays for the option values and labels in the UISelects. This is partially
         * because rendering each individual checkbox requires and integer indicating
         * it's position, and this view is too complicated to generate these arrays
         * ahead of time.  So we generate the String Arrays on the fly, using the list.size()-1
         * at each point to get this index.  Then at the very end we update the UISelect's
         * with the appropriate optionlist and optionnames. This actually works 
         * really good and the wizard feels much smoother than it did with the 
         * old session bean.
         * 
         * Also see the comments on HierarchyTreeNodeSelectRenderer. 
         * 
         */
        UIForm form = UIForm.make(tofill, "eval-assign-form", formViewParams);

        // Things for building the UISelect of Hierarchy Node Checkboxes
        List<String> hierNodesLabels = new ArrayList<String>();
        List<String> hierNodesValues = new ArrayList<String>();
        UISelect hierarchyNodesSelect = UISelect.makeMultiple(form, "hierarchyNodeSelectHolder", 
                new String[] {}, new String[] {}, "selectedHierarchyNodeIDs", new String[] {});
        String hierNodesSelectID = hierarchyNodesSelect.getFullID();

        // Things for building the UISelect of Eval Group Checkboxes
        List<String> evalGroupsLabels = new ArrayList<String>();
        List<String> evalGroupsValues = new ArrayList<String>();
        UISelect evalGroupsSelect = UISelect.makeMultiple(form, "evalGroupSelectHolder", 
                new String[] {}, new String[] {}, "selectedGroupIDs", new String[] {});
        String evalGroupsSelectID = evalGroupsSelect.getFullID();

        /*
         * About the 4 collapsable areas.
         * 
         * What's happening here is that we have 4 areas: hierarchy, groups, 
         * new adhoc groups, and existing adhoc groups that can be hidden and selected
         * which a checkbox for each one.  I'm not using the UIInitBlock at the moment
         * because it doesn't seem to take arrays for the javascript arguments (and keep 
         * them as javascript arrays).  So we are just putting the javascript initialization
         * here and running it at the bottom of the page.
         * 
         */
        Boolean useAdHocGroups = (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_GROUPS);
        Boolean showHierarchy = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);

        // NOTE: this is the one place where the perms should be used instead of user assignments (there are no assignments yet) -AZ

        // get the groups that this user is allowed to assign evals to
        List<EvalGroup> evalGroups = commonLogic.getEvalGroupsForUser(commonLogic.getCurrentUserId(), EvalConstants.PERM_ASSIGN_EVALUATION);

        // for backwards compatibility we will pull the list of groups the user is being evaluated in as well and merge it in
        List<EvalGroup> beEvalGroups = commonLogic.getEvalGroupsForUser(commonLogic.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
        for (EvalGroup evalGroup : beEvalGroups) {
            if (! evalGroups.contains(evalGroup)) {
                evalGroups.add(evalGroup);
            }
        }

        if (evalGroups.size() > 0) {
            Map<String, EvalGroup> groupsMap = new HashMap<String, EvalGroup>();
            for (int i=0; i < evalGroups.size(); i++) {
                EvalGroup c = (EvalGroup) evalGroups.get(i);
                groupsMap.put(c.evalGroupId, c);
            }

            /*
             * Area 1. Selection GUI for Hierarchy Nodes and Evaluation Groups
             */

            if (showHierarchy) {
                UIBranchContainer hierarchyArea = UIBranchContainer.make(form, "hierarchy-node-area:");

                addCollapseControl(hierarchyArea, "hierarchy-assignment-area", "hide-button", "show-button");

                hierUtil.renderSelectHierarchyNodesTree(hierarchyArea, "hierarchy-tree-select:", 
                        evalGroupsSelectID, hierNodesSelectID, evalGroupsLabels, evalGroupsValues,
                        hierNodesLabels, hierNodesValues);
            }

            /*
             * Area 2. display checkboxes for selecting the non-hierarchy groups
             */
            UIBranchContainer evalgroupArea = UIBranchContainer.make(form, "evalgroups-area:");

            // If both the hierarchy and adhoc groups are disabled, don't hide the
            // selection area and don't make it collapsable, since it will be the
            // only thing on the screen.
            if (! showHierarchy && ! useAdHocGroups) {
                UIOutput.make(evalgroupArea, "evalgroups-assignment-area");
            }
            else {
                addCollapseControl(evalgroupArea, "evalgroups-assignment-area", "hide-button", "show-button");
            }

            String[] nonAssignedEvalGroupIDs = getEvalGroupIDsNotAssignedInHierarchy(evalGroups).toArray(new String[] {});
            List<EvalGroup> unassignedEvalGroups = new ArrayList<EvalGroup>();
            for (int i = 0; i < nonAssignedEvalGroupIDs.length; i++) {
                unassignedEvalGroups.add(groupsMap.get(nonAssignedEvalGroupIDs[i]));
            }
            // sort the list by title
            Collections.sort(unassignedEvalGroups, new ComparatorsUtils.GroupComparatorByTitle());

            int count = 0;
            for (EvalGroup evalGroup : unassignedEvalGroups) {
                UIBranchContainer checkboxRow = UIBranchContainer.make(evalgroupArea, "groups:", count+"");
                if (count % 2 == 0) {
                    checkboxRow.decorate( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
                }

                evalGroupsLabels.add(evalGroup.title);
                evalGroupsValues.add(evalGroup.evalGroupId);

                UISelectChoice choice = UISelectChoice.make(checkboxRow, "evalGroupId", evalGroupsSelectID, evalGroupsLabels.size()-1);

                // get title from the map since it is faster
                UIOutput title = UIOutput.make(checkboxRow, "groupTitle", evalGroup.title );
                UILabelTargetDecorator.targetLabel(title, choice); // make title a label for checkbox

                count++;
            }
        } else {
            // TODO tell user there are no groups to assign to
        }

        /*
         * Area 3: Selection GUI for Adhoc Groups
         */
        if (useAdHocGroups) {
            UIBranchContainer adhocGroupsArea = UIBranchContainer.make(form, "use-adhoc-groups-area:");

            addCollapseControl(adhocGroupsArea, "evalgroups-assignment-area", "hide-button", "show-button");

            // Table of Existing adhoc groups for selection
            List<EvalAdhocGroup> myAdhocGroups = commonLogic.getAdhocGroupsForOwner(currentUserId);
            if (myAdhocGroups.size() > 0) {
                UIOutput.make(adhocGroupsArea, "adhoc-groups-table");

                int count = 0;
                for (EvalAdhocGroup adhocGroup: myAdhocGroups) {
                    UIBranchContainer tableRow = UIBranchContainer.make(adhocGroupsArea, "groups:");
                    if (count % 2 == 0) {
                        tableRow.decorate( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
                    }

                    evalGroupsLabels.add(adhocGroup.getTitle());
                    evalGroupsValues.add(adhocGroup.getEvalGroupId());

                    UISelectChoice choice = UISelectChoice.make(tableRow, "evalGroupId", evalGroupsSelectID, evalGroupsLabels.size()-1);

                    // get title from the map since it is faster
                    UIOutput title = UIOutput.make(tableRow, "groupTitle", adhocGroup.getTitle() );
                    UILabelTargetDecorator.targetLabel(title, choice); // make title a label for checkbox

                    // Link to allow editing an existing group
                    UIInternalLink.make(tableRow, "editGroupLink", UIMessage.make("assigneval.page.adhocgroups.editgrouplink"),
                            new AdhocGroupParams(ModifyAdhocGroupProducer.VIEW_ID, adhocGroup.getId(), vsh.getFullURL(evalViewParams)));
                    
                    count ++;
                }
            }
            UIInternalLink.make(adhocGroupsArea, "new-adhocgroup-link", UIMessage.make("assigneval.page.adhocgroups.newgrouplink"),
                    new AdhocGroupParams(ModifyAdhocGroupProducer.VIEW_ID, null, vsh.getFullURL(evalViewParams)));
        }

        // Add all the groups and hierarchy nodes back to the UISelect Many's. see
        // the large comment further up.
        evalGroupsSelect.optionlist = UIOutputMany.make(evalGroupsValues.toArray(new String[] {}));
        evalGroupsSelect.optionnames = UIOutputMany.make(evalGroupsLabels.toArray(new String[] {}));

        hierarchyNodesSelect.optionlist = UIOutputMany.make(hierNodesValues.toArray(new String[] {}));
        hierarchyNodesSelect.optionnames = UIOutputMany.make(hierNodesLabels.toArray(new String[] {}));

        // all command buttons are just HTML now so no more bindings
        UIMessage.make(form, "back-button", "general.back.button");
        UIMessage assignButton = UIMessage.make(form, "confirmAssignCourses", "assigneval.save.assigned.button" );

        // Error message to be triggered by javascript if users doesn't select anything
        // There is a 'evalgroupselect' class on each input checkbox that the JS 
        // can check for now.
        UIMessage assignErrorDiv = UIMessage.make(tofill, "nogroups-error", "assigneval.invalid.selection");
        initJS.append(HTMLUtil.emitJavascriptCall("EvalSystem.initEvalAssignValidation", 
                new String[] {form.getFullID(), assignErrorDiv.getFullID(), assignButton.getFullID()}));

        // Setup JavaScript for the collapseable sections
        UIVerbatim.make(tofill, "initJavaScript", initJS.toString());
    }

    /**
     * I think this is getting all the groupIds that are not currently assigned to nodes in the hierarchy
     * 
     * @param evalGroups the list of eval groups to check in
     * @return the set of evalGroupsIds from the input list of evalGroups which are not assigned to hierarchy nodes
     */
    protected Set<String> getEvalGroupIDsNotAssignedInHierarchy(List<EvalGroup> evalGroups) {
        // TODO - we probably need a method to simply get all assigned groupIds in the hierarchy to make this a bit faster

        // 1. All the Evaluation Group IDs in a set
        Set<String> evalGroupIDs = new HashSet<String>();
        for (EvalGroup evalGroup: evalGroups) {
            evalGroupIDs.add(evalGroup.evalGroupId);
        }

        // 2. All the Evaluation Group IDs that are assigned to Hierarchy Nodes
        EvalHierarchyNode rootNode = hierarchyLogic.getRootLevelNode();
        String[] rootNodeChildren = rootNode.childNodeIds.toArray(new String[] {});
        if (rootNodeChildren.length > 0) {
            Map<String,Set<String>> assignedGroups = hierarchyLogic.getEvalGroupsForNodes(rootNodeChildren);

            Set<String> hierAssignedGroupIDs = new HashSet<String>();
            for (String key: assignedGroups.keySet()) {
                hierAssignedGroupIDs.addAll(assignedGroups.get(key));
            }
            // 3. Remove all EvalGroup IDs that have been assigned to 
            evalGroupIDs.removeAll(hierAssignedGroupIDs);
        }


        return evalGroupIDs;
    }


    /**
     * Taking the parent container and rsf:id's for the collapsed area and tags
     * to show and hide, creates the necessary javascript.  The Javascript is
     * appended to the instance variable holding the javascript that will be 
     * rendered at the bottom of the page for javascript initialization.
     * 
     * @param tofill
     * @param areaId
     * @param hideId
     * @param showId
     */
    private void addCollapseControl(UIContainer tofill, String areaId, String hideId,
            String showId) {
        UIOutput hideControl = UIOutput.make(tofill, hideId);
        UIOutput showControl = UIOutput.make(tofill, showId);

        //makeToggle: function (showId, hideId, areaId, toggleClass, initialHide) {
        UIOutput areaDiv = UIOutput.make(tofill, areaId);
        initJS.append(HTMLUtil.emitJavascriptCall("EvalSystem.makeToggle", 
                new String[] { showControl.getFullID(),hideControl.getFullID(),
                areaDiv.getFullID(),null,"true"}));
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

    /**
     * Renders the usual action breadcrumbs at the top.
     * 
     * @param tofill
     * @param currentUserId
     */
    private void renderTopLinks(UIContainer tofill, String currentUserId) {
        boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
        boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);

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

        if (beginEvaluation) {
            UIInternalLink.make(tofill, "control-evaluations-link",
                    UIMessage.make("controlevaluations.page.title"),
                    new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
        } else {
            throw new SecurityException("User attempted to access " + 
                    VIEW_ID + " when they are not allowed");
        }
    }

}