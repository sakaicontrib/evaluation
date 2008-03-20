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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.renderers.HierarchyTreeNodeSelectRenderer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;

import uk.org.ponder.htmlutil.HTMLUtil;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIOutputMany;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * assign an evaluation to groups and hierarchy nodes 
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Steve Githens (sgithens@caret.cam.ac.uk)
 */
public class EvaluationAssignProducer implements ViewComponentProducer, ViewParamsReporter, ActionResultInterceptor {

   public static final String VIEW_ID = "evaluation_assign";
   public String getViewID() {
      return VIEW_ID;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
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

   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      // local variables used in the render logic
      String currentUserId = externalLogic.getCurrentUserId();
      boolean userAdmin = externalLogic.isUserAdmin(currentUserId);
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

      EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;
      if (evalViewParams.evaluationId == null) {
         throw new IllegalArgumentException("Cannot access this view unless the evaluationId is set");
      }

      /**
       * This is the evaluation we are working with on this page,
       * this should ONLY be read from, do not change any of these fields
       */
      EvalEvaluation evaluation = evaluationService.getEvaluationById(evalViewParams.evaluationId);


      UIMessage.make(tofill, "assign-eval-edit-page-title", "assigneval.assign.page.title", new Object[] {evaluation.getTitle()});
      UIMessage.make(tofill, "assign-eval-instructions", "assigneval.assign.instructions", new Object[] {evaluation.getTitle()});

      // this is a get form which does not submit to a backing bean
      EvalViewParameters formViewParams = (EvalViewParameters) evalViewParams.copyBase();
      formViewParams.viewID = EvaluationAssignConfirmProducer.VIEW_ID;
      
      /* 
       * About this views form.
       * 
       * 
       */
      UIForm form = UIForm.make(tofill, "eval-assign-form", formViewParams);
      List<String> hierNodesLabels = new ArrayList<String>();
      List<String> hierNodesValues = new ArrayList<String>();
      UISelect hierarchyNodesSelect = UISelect.makeMultiple(form, "hierarchyNodeSelectHolder", 
              new String[] {}, new String[] {}, "selectedHierarchyNodeIDs", new String[] {});
      String hierNodesSelectID = hierarchyNodesSelect.getFullID();
      
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
       */
      StringBuilder initJS = new StringBuilder();

      /*
       * Selection GUI for Hierarchy Nodes and Evaluation Groups
       */
      List<EvalGroup> evalGroups = externalLogic.getEvalGroupsForUser(externalLogic.getCurrentUserId(), EvalConstants.PERM_BE_EVALUATED);
      if (evalGroups.size() > 0) {
         Map<String, EvalGroup> groupsMap = new HashMap<String, EvalGroup>();
         for (int i=0; i < evalGroups.size(); i++) {
            EvalGroup c = (EvalGroup) evalGroups.get(i);
            groupsMap.put(c.evalGroupId, c);
         }

         // Display the table for selecting hierarchy nodes
         Boolean showHierarchy = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
         if (showHierarchy) {
            UIOutput.make(form, "hierarchy-node-area");
            
            UIOutput hierarchyCheckbox = UIOutput.make(form, "use-hierarchynodes-checkbox");
            UIOutput hierarchyDiv = UIOutput.make(form, "hierarchy-assignment-area");
            initJS.append(HTMLUtil.emitJavascriptCall("EvalSystem.hideAndShowRegionWithCheckbox", 
                  new String[] {hierarchyDiv.getFullID(), hierarchyCheckbox.getFullID()}));
            
            hierUtil.renderSelectHierarchyNodesTree(form, "hierarchy-tree-select:", 
                    evalGroupsSelectID, hierNodesSelectID, evalGroupsLabels, evalGroupsValues,
                    hierNodesLabels, hierNodesValues);
         }

         // display checkboxes for selecting the non-hierarchy groups
         UIOutput evalGroupCheckbox = UIOutput.make(form, "use-evalgroups-checkbox");

         UIOutput evalGroupDiv = UIOutput.make(form, "evalgroups-assignment-area");
         initJS.append(HTMLUtil.emitJavascriptCall("EvalSystem.hideAndShowRegionWithCheckbox", 
               new String[] {evalGroupDiv.getFullID(), evalGroupCheckbox.getFullID()}));

         String[] nonAssignedEvalGroupIDs = getEvalGroupIDsNotAssignedInHierarchy(evalGroups).toArray(new String[] {});
         for (int i = 0; i < nonAssignedEvalGroupIDs.length; i++) {
            UIBranchContainer checkboxRow = UIBranchContainer.make(form, "groups:", i+"");
            if (i % 2 == 0) {
               checkboxRow.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
            }
            
            evalGroupsLabels.add(groupsMap.get(nonAssignedEvalGroupIDs[i]).title);
            evalGroupsValues.add(nonAssignedEvalGroupIDs[i]);
            
            UISelectChoice choice = UISelectChoice.make(checkboxRow, "evalGroupId", evalGroupsSelectID, evalGroupsLabels.size()-1);

            // get title from the map since it is faster
            UIOutput title = UIOutput.make(checkboxRow, "groupTitle", groupsMap.get(nonAssignedEvalGroupIDs[i]).title );
            UILabelTargetDecorator.targetLabel(title, choice); // make title a label for checkbox
         }
      } else {
         // TODO tell user there are no groups to assign to
      }

      /*
       * Selection GUI for new and existing ad-hoc groups.
       */
      Boolean useAdHocGroups = (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_GROUPS);
      if (useAdHocGroups) {
         UIOutput addhocGroupCheckbox = UIOutput.make(form, "use-adhocgroups-checkbox");
         UIOutput addhocGroupDiv = UIOutput.make(form, "newadhocgroup-assignment-area");

         initJS.append(HTMLUtil.emitJavascriptCall("EvalSystem.hideAndShowRegionWithCheckbox", 
               new String[] {addhocGroupDiv.getFullID(), addhocGroupCheckbox.getFullID()}));

         UIInput adhocGroupName = UIInput.make(form, "adhoc-group-name", "");
         UIInput adhocGroupEmails = UIInput.make(form, "adhoc-email-input", "");

         UIOutput saveEmailsButton = UIOutput.make(form, "adhoc-save-emails-button");
         UIOutput clearEmailsButton = UIOutput.make(form, "adhoc-clear-emails-button");
      }

      /*
       * TODO: If more than one course is selected and you come back to this page from confirm page,
       * then without changing the selection you again go to confirm page, you get a null pointer
       * that is created by RSF as:
       * 
       * 	"Error flattening value[Ljava.lang.String;@944d4a into class [Ljava.lang.String;
       * 	...
       *  java.lang.NullPointerException
       * 		at uk.org.ponder.arrayutil.ArrayUtil.lexicalCompare(ArrayUtil.java:205)
       *  	at uk.org.ponder.rsf.uitype.StringArrayUIType.valueUnchanged(StringArrayUIType.java:23)
       *  ..."
       */
      
      // Add all the groups and hierarchy nodes back to the UISelect Many's
      evalGroupsSelect.optionlist = UIOutputMany.make(evalGroupsValues.toArray(new String[] {}));
      evalGroupsSelect.optionnames = UIOutputMany.make(evalGroupsLabels.toArray(new String[] {}));

      hierarchyNodesSelect.optionlist = UIOutputMany.make(hierNodesValues.toArray(new String[] {}));
      hierarchyNodesSelect.optionnames = UIOutputMany.make(hierNodesLabels.toArray(new String[] {}));
      
      // all command buttons are just HTML now so no more bindings
      UIMessage.make(form, "cancel-button", "general.cancel.button");
      UIMessage.make(form, "confirmAssignCourses", "assigneval.save.assigned.button" );

//      UICommand.make(form, "editSettings", UIMessage.make("assigneval.edit.settings.button"), "#{evaluationBean.backToSettingsAction}");
//      UICommand.make(form, "confirmAssignCourses", UIMessage.make("assigneval.save.assigned.button"), "#{evaluationBean.confirmAssignCoursesAction}");

      // Setup JavaScript for the collapse able sections
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