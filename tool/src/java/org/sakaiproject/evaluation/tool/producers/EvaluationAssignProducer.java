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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.entity.AdhocGroupEntityProvider;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.renderers.HierarchyTreeNodeSelectRenderer;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.AdhocGroupParams;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.request.EarlyRequestParser;
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
	
	private static Log log = LogFactory.getLog(EvaluationAssignProducer.class);

    public static final String VIEW_ID = "evaluation_assign";
    public String getViewID() {
        return VIEW_ID;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private ExternalHierarchyLogic hierarchyLogic;
    public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
        this.hierarchyLogic = logic;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic bean) {
        this.commonLogic = bean;
    }
    
    private Locale locale;
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    public MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator messageLocator) {
        this.messageLocator = messageLocator;
    }
    
    private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }
    
    private HierarchyTreeNodeSelectRenderer hierUtil;
    public void setHierarchyRenderUtil(HierarchyTreeNodeSelectRenderer util) {
        hierUtil = util;
    }
    
    private ViewStateHandler vsh;
    public void setViewStateHandler(ViewStateHandler vsh) {
        this.vsh = vsh;
    }
   
    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        // local variables used in the render logic
        String currentUserId = commonLogic.getCurrentUserId();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        DateFormat dtf = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale);

        navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());

        EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;
        if (evalViewParams.evaluationId == null) {
            throw new IllegalArgumentException("Cannot access this view unless the evaluationId is set");
        }

        /**
         * This is the evaluation we are working with on this page,
         * this should ONLY be read from, do not change any of these fields
         */
        EvalEvaluation evaluation = evaluationService.getEvaluationById(evalViewParams.evaluationId);
        
        //Are we using the selection options (UCT)?
        boolean useSelectionOptions = ((Boolean)settings.get(EvalSettings.ENABLE_INSTRUCTOR_ASSISTANT_SELECTION)).booleanValue();
        
        String actionBean = "setupEvalBean.";
        Boolean newEval = false;
        
        UIInternalLink.make(tofill, "eval-settings-link",
                UIMessage.make("evalsettings.page.title"),
                new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evalViewParams.evaluationId) );
        if (EvalConstants.EVALUATION_STATE_PARTIAL.equals(evaluation.getState())) {
            // creating a new eval
            UIMessage.make(tofill, "eval-start-text", "starteval.page.title");
            newEval = true;
        }

        UIMessage.make(tofill, "assign-eval-edit-page-title", "assigneval.assign.page.title", new Object[] {evaluation.getTitle()});
        UIMessage.make(tofill, "assign-eval-instructions", "assigneval.assign.instructions", new Object[] {evaluation.getTitle()});        

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
        UIForm form = UIForm.make(tofill, "eval-assign-form");

        // Things for building the UISelect of Hierarchy Node Checkboxes
        List<String> hierNodesLabels = new ArrayList<String>();
        List<String> hierNodesValues = new ArrayList<String>();
        UISelect hierarchyNodesSelect = UISelect.makeMultiple(form, "hierarchyNodeSelectHolder",
                new String[] {}, new String[] {}, (useSelectionOptions? actionBean : "") + "selectedHierarchyNodeIDs", new String[] {});
        String hierNodesSelectID = hierarchyNodesSelect.getFullID();

        // Things for building the UISelect of Eval Group Checkboxes
        List<String> evalGroupsLabels = new ArrayList<String>();
        List<String> evalGroupsValues = new ArrayList<String>();
        UISelect evalGroupsSelect = UISelect.makeMultiple(form, "evalGroupSelectHolder",
                new String[] {}, new String[] {}, (useSelectionOptions? actionBean : "") + "selectedGroupIDs", new String[] {});
        String evalGroupsSelectID = evalGroupsSelect.getFullID();

        /*
         * About the 4 collapsable areas.
         * 
         * What's happening here is that we have 4 areas: hierarchy, groups, 
         * new adhoc groups, and existing adhoc groups that can be hidden and selected
         * which a checkbox for each one.
         * 
         */
        
        Boolean useAdHocGroups = (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_GROUPS);
        Boolean showHierarchy = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
       
        // NOTE: this is the one place where the perms should be used instead of user assignments (there are no assignments yet) -AZ
        
        // get the current eval group id (ie: reference site id) that the user is in now
        String currentEvalGroupId = commonLogic.getCurrentEvalGroup();
        // get the current eval group id (ie: site id) that the user is in now
        String currentSiteId = EntityReference.getIdFromRef(currentEvalGroupId);

        // get the groups that this user is allowed to assign evals to
        List<EvalGroup> evalGroups = new ArrayList<EvalGroup>();
        // for backwards compatibility we will pull the list of groups the user is being evaluated in as well and merge it in
        List<EvalGroup> beEvalGroups = new ArrayList<EvalGroup>();
        
        Boolean isGroupFilterEnabled = (Boolean) settings.get(EvalSettings.ENABLE_FILTER_ASSIGNABLE_GROUPS);

        if( isGroupFilterEnabled ){
	        evalGroups = commonLogic.getFilteredEvalGroupsForUser(commonLogic.getCurrentUserId(), EvalConstants.PERM_ASSIGN_EVALUATION, currentSiteId);  
	        beEvalGroups = commonLogic.getFilteredEvalGroupsForUser(commonLogic.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED, currentSiteId);  
        }else{
        	evalGroups = commonLogic.getEvalGroupsForUser(commonLogic.getCurrentUserId(), EvalConstants.PERM_ASSIGN_EVALUATION);
        	beEvalGroups = commonLogic.getEvalGroupsForUser(commonLogic.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
        }
        
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

                addCollapseControl(tofill, hierarchyArea, "initJSHierarchyToggle",
                        "hierarchy-assignment-area", "hide-button", "show-button", true);

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
                addCollapseControl(tofill, evalgroupArea, "initJSGroupsToggle",
                        "evalgroups-assignment-area", "hide-button", "show-button", true);
            }
            
            String[] nonAssignedEvalGroupIDs = getEvalGroupIDsNotAssignedInHierarchy(evalGroups).toArray(new String[] {});
            
            List<EvalGroup> unassignedEvalGroups = new ArrayList<EvalGroup>();
            for (int i = 0; i < nonAssignedEvalGroupIDs.length; i++) {
                unassignedEvalGroups.add(groupsMap.get(nonAssignedEvalGroupIDs[i]));
            }
            // sort the list by title 
            Collections.sort(unassignedEvalGroups, new ComparatorsUtils.GroupComparatorByTitle());
            
            //Move current site to top of this list EVALSYS-762.
            EvalGroup currentGroup = null;
            int count2 = 0, found = 0;
            for ( EvalGroup group : unassignedEvalGroups ){
            	if ( group.evalGroupId.equals(currentEvalGroupId)){
            		currentGroup = group;
            		found = count2; // Save current group's list index so later we can remove it
            	}
            	count2 ++;
            }
            unassignedEvalGroups.remove(found);		
            unassignedEvalGroups.add(0, currentGroup);
            
			
            List<String> assignGroupsIds = new ArrayList<String>();
            String groupSelectionOTP = "assignGroupSelectionSettings.";
            if(! newEval){
            	Map<Long, List<EvalAssignGroup>> selectedGroupsMap = evaluationService.getAssignGroupsForEvals(new Long[] {evalViewParams.evaluationId}, true, null);
            	List<EvalAssignGroup> assignGroups = selectedGroupsMap.get(evalViewParams.evaluationId);
            	for(EvalAssignGroup assGroup: assignGroups){
            		assignGroupsIds.add(assGroup.getEvalGroupId());
            		
            		//Add group selection settings to form to support EVALSYS-778
            		if (useSelectionOptions){
	            		Map<String, String> selectionOptions = assGroup.getSelectionOptions();
	                    form.parameters.add(new UIELBinding(groupSelectionOTP + assGroup.getEvalGroupId().replaceAll("/site/", "") + ".instructor", selectionOptions.get(EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR)));
	                    form.parameters.add(new UIELBinding(groupSelectionOTP + assGroup.getEvalGroupId().replaceAll("/site/", "") + ".assistant", selectionOptions.get(EvalAssignGroup.SELECTION_TYPE_ASSISTANT)));
            		}
            	}
            }
                           
            int count = 0;
            int countUnpublishedGroups = 0;
            for (EvalGroup evalGroup : unassignedEvalGroups) {
            	if(evalGroup != null){
            		
            	String evalGroupId = evalGroup.evalGroupId;
            	
            	boolean hasEvaluators = true;
            	
            	if (! EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(evaluation.getAuthControl())){
                	int numEvaluatorsInSite = commonLogic.countUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_TAKE_EVALUATION);
                	hasEvaluators = numEvaluatorsInSite > 0;
            	}
            	
            	boolean isPublished = commonLogic.isEvalGroupPublished(evalGroupId);
            	            	
                UIBranchContainer checkboxRow = UIBranchContainer.make(evalgroupArea, "groups:", count+"");
                if (count % 2 == 0) {
                    checkboxRow.decorate( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
                }
                checkboxRow.decorate(new UIFreeAttributeDecorator("rel", count+"")); // table row counter for JS use in EVALSYS-618
                
                //keep deselected user info as a result of changes in EVALSYS-660
                Set<String> deselectedInsructorIds = new HashSet<String>();
                Set<String> deselectedAssistantIds = new HashSet<String>();
                
                if (useSelectionOptions){
	                
	                if (! newEval) {
	                //Get saved selection settings for this eval
	            	List<EvalAssignUser> deselectedInsructors = evaluationService.getParticipantsForEval(evalViewParams.evaluationId, null, new String[]{evalGroupId}, EvalAssignUser.TYPE_EVALUATEE, EvalAssignUser.STATUS_REMOVED, null, null);
	                List<EvalAssignUser> deselectedAssistants = evaluationService.getParticipantsForEval(evalViewParams.evaluationId, null, new String[]{evalGroupId}, EvalAssignUser.TYPE_ASSISTANT, EvalAssignUser.STATUS_REMOVED, null, null);
	               
	            	//check for already deselected users that match this groupId
	                for(EvalAssignUser deselectedUser:deselectedInsructors){
	                	deselectedInsructorIds.add(deselectedUser.getUserId());
	                }
	                for(EvalAssignUser deselectedUser:deselectedAssistants){
	                	deselectedAssistantIds.add(deselectedUser.getUserId());
	                }
	               
	                //Assign attribute to row to help JS set checkbox selection to true
	                if(assignGroupsIds.contains(evalGroupId)){
	                	checkboxRow.decorate(new UIStyleDecorator("selectedGroup"));
	                }
	                 
	                }else{
	                	//add blank selection options for this group for use by evalAssign.js
	                	form.parameters.add(new UIELBinding(groupSelectionOTP + evalGroupId.replaceAll("/site/", "") + ".instructor", ""));
	                    form.parameters.add(new UIELBinding(groupSelectionOTP + evalGroupId.replaceAll("/site/", "") + ".assistant", ""));	
	                }
                }
                
                evalGroupsLabels.add(evalGroup.title);
                evalGroupsValues.add(evalGroupId);

                String evalUsersLocator = "selectedEvaluationUsersLocator.";
                
                UISelectChoice choice = UISelectChoice.make(checkboxRow, "evalGroupId", evalGroupsSelectID, evalGroupsLabels.size()-1);
                
                if (! hasEvaluators){
                	choice.decorate( new UIDisabledDecorator() );
                }
                
                if (useSelectionOptions){
	                form.parameters.add(new UIELBinding(evalUsersLocator + evalGroupId.replaceAll("/site/", "")+".deselectedInstructors", deselectedInsructorIds!=null?deselectedInsructorIds.toArray(new String[deselectedInsructorIds.size()]):new String[]{}));
	                form.parameters.add(new UIELBinding(evalUsersLocator + evalGroupId.replaceAll("/site/", "")+".deselectedAssistants", deselectedAssistantIds!=null?deselectedAssistantIds.toArray(new String[deselectedAssistantIds.size()]):new String[]{}));
	                
	                //add ordering bindings
	                form.parameters.add(new UIELBinding(evalUsersLocator + evalGroupId.replaceAll("/site/", "") + ".orderingInstructors", new String[]{} ));
	                form.parameters.add(new UIELBinding(evalUsersLocator + evalGroupId.replaceAll("/site/", "") + ".orderingAssistants", new String[]{} ));
                }
                
                // get title from the map since it is faster
	            UIOutput title = UIOutput.make(checkboxRow, "groupTitle", evalGroup.title );
	            
	            if(! isPublished){
                	title.decorate( new UIStyleDecorator("elementAlertBack") );
                	countUnpublishedGroups ++;
                }
	            
	            if (useSelectionOptions){
		            if( hasEvaluators ){
		                int totalUsers = commonLogic.countUserIdsForEvalGroup(evalGroupId, EvalConstants.PERM_BE_EVALUATED);
		                if(totalUsers > 0){
		                	int currentUsers = deselectedInsructorIds.size() >= 0 ? ( totalUsers-deselectedInsructorIds.size() ) : totalUsers;
		                	UIInternalLink link = UIInternalLink.make(checkboxRow, "select-instructors", UIMessage.make("assignselect.instructors.select", 
		                			new Object[] {currentUsers,totalUsers}), 
		                			new EvalViewParameters(EvaluationAssignSelectProducer.VIEW_ID, evaluation.getId() ,evalGroupId, EvalAssignGroup.SELECTION_TYPE_INSTRUCTOR) );
		                	link.decorate(new UIStyleDecorator("addItem total:"+totalUsers));
		                	link.decorate(new UITooltipDecorator(messageLocator.getMessage("assignselect.instructors.page.title")));
		                }
		                totalUsers = commonLogic.countUserIdsForEvalGroup(evalGroup.evalGroupId, EvalConstants.PERM_ASSISTANT_ROLE);
		                if(totalUsers > 0){
		                	int currentUsers = deselectedAssistantIds.size() >= 0 ? ( totalUsers-deselectedAssistantIds.size() ) : totalUsers;
		                	UIInternalLink link = UIInternalLink.make(checkboxRow, "select-tas", UIMessage.make("assignselect.tas.select", 
		                			new Object[] {currentUsers,totalUsers}) , 
		                			new EvalViewParameters(EvaluationAssignSelectProducer.VIEW_ID, evaluation.getId() ,evalGroup.evalGroupId, EvalAssignGroup.SELECTION_TYPE_ASSISTANT) );
		                    link.decorate(new UIStyleDecorator("addItem total:"+totalUsers));
		                    link.decorate(new UITooltipDecorator(messageLocator.getMessage("assignselect.tas.page.title")));
		                }
	                }else{
	                	title.decorate( new UIStyleDecorator("instruction") );
	                	UIMessage.make(checkboxRow, "select-no", "assigneval.cannot.assign");
	                }
	            }
	            
                UILabelTargetDecorator.targetLabel(title, choice); // make title a label for checkbox
	                
                
                count++;
            }
            }
            if (countUnpublishedGroups > 0){
            	UIMessage.make(tofill, "assign-eval-instructions-group-notpublished", "assigneval.assign.instructions.notpublished");
            }
        } else {
            // TODO tell user there are no groups to assign to
        }
        
        /*
         * Area 3: Selection GUI for Adhoc Groups
         */
        String[] adhocGroupRowIds = new String[] {};
        if (useAdHocGroups) {
            UIBranchContainer adhocGroupsArea = UIBranchContainer.make(form, "use-adhoc-groups-area:");

            UIMessage.make(adhocGroupsArea, "adhoc-groups-deleted", "modifyadhocgroup.group.deleted");
            addCollapseControl(tofill, adhocGroupsArea, "initJSAdhocToggle",
                    "adhocgroups-assignment-area", "hide-button", "show-button", true);

            // Table of Existing adhoc groups for selection
            List<EvalAdhocGroup> myAdhocGroups = commonLogic.getAdhocGroupsForOwner(currentUserId);
            if (myAdhocGroups.size() > 0) {
                UIOutput.make(adhocGroupsArea, "adhoc-groups-table");

                ArrayList<String> adhocGroupRowIdsArray = new ArrayList<String>(myAdhocGroups.size());
                int count = 0;
                for (EvalAdhocGroup adhocGroup: myAdhocGroups) {
                    UIBranchContainer tableRow = UIBranchContainer.make(adhocGroupsArea, "groups:");
                    adhocGroupRowIdsArray.add(tableRow.getFullID());
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
                    // add delete option - https://bugs.caret.cam.ac.uk/browse/CTL-1310
                    if (currentUserId.equals(adhocGroup.getOwner()) || commonLogic.isUserAdmin(currentUserId)) {
                        String deleteLink = commonLogic.getEntityURL(AdhocGroupEntityProvider.ENTITY_PREFIX,
                                adhocGroup.getId().toString());
                        UILink.make(tableRow, "deleteGroupLink", UIMessage.make("general.command.delete"), deleteLink);
                    }
                    count ++;
                }
                adhocGroupRowIds = adhocGroupRowIdsArray.toArray(new String[adhocGroupRowIdsArray.size()]);
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
        
        form.parameters.add( new UIELBinding(actionBean + "evaluationId", evalViewParams.evaluationId) );
        
        UIMessage.make(form, "back-button", "general.back.button");
        
        if (useSelectionOptions){
        	UIOutput.make(tofill, "JS-facebox");
        	UIOutput.make(tofill, "JS-facebox-assign");
        	UIOutput.make(tofill, "JS-assign");
        	UIMessage.make(form, "select-column-title", "assignselect.page.column.title");
        	form.type = EarlyRequestParser.ACTION_REQUEST;
        	UICommand.make(form, "confirmAssignCourses", UIMessage.make("evaluationassignconfirm.done.button"),actionBean + "completeConfirmAction" );
        }else{
        	// this is a get form which does not submit to a backing bean
            EvalViewParameters formViewParams = (EvalViewParameters) evalViewParams.copyBase();
            formViewParams.viewID = EvaluationAssignConfirmProducer.VIEW_ID;
            form.viewparams = formViewParams;
        	form.type = EarlyRequestParser.RENDER_REQUEST;
        	 // all command buttons are just HTML now so no more bindings
        	UIMessage assignButton = UIMessage.make(form, "confirmAssignCourses", "assigneval.save.assigned.button" );

            // activate the adhoc groups deletion javascript
            if (useAdHocGroups) {
                UIInitBlock.make(tofill, "initJavaScriptAdhoc", "EvalSystem.initEvalAssignAdhocDelete",
                        new Object[] {adhocGroupRowIds});
            }

            // Error message to be triggered by javascript if users doesn't select anything
            // There is a 'evalgroupselect' class on each input checkbox that the JS
            // can check for now.
            UIMessage assignErrorDiv = UIMessage.make(tofill, "nogroups-error", "assigneval.invalid.selection");

            UIInitBlock.make(tofill, "initJavaScript", "EvalSystem.initEvalAssignValidation",
                    new Object[] {form.getFullID(), assignErrorDiv.getFullID(), assignButton.getFullID()});
        }
        
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
     */
    private void addCollapseControl(UIContainer tofill, UIContainer parent,
            String rsfId, String areaId, String hideId, String showId, boolean initialHide) {
        UIOutput hideControl = UIOutput.make(parent, hideId);
        UIOutput showControl = UIOutput.make(parent, showId);
        UIOutput areaDiv = UIOutput.make(parent, areaId);

        //makeToggle: function (showId, hideId, areaId, toggleClass, initialHide) {
        if (initialHide) {
            UIInitBlock.make(tofill, rsfId, "EvalSystem.makeToggle",
                    new Object[] { showControl.getFullID(),
                    hideControl.getFullID(),
                    areaDiv.getFullID(),
                    null,
                    initialHide
                }
            );
        } else {
            UIInitBlock.make(tofill, rsfId, "EvalSystem.makeToggle",
                    new Object[] { showControl.getFullID(),
                    hideControl.getFullID(),
                    areaDiv.getFullID() }
            );
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
