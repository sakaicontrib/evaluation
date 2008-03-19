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
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.tool.EvaluationBean;
import org.sakaiproject.evaluation.tool.utils.HierarchyRenderUtil;

import uk.org.ponder.htmlutil.HTMLUtil;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * assign an evaluation to groups and hierarchy nodes 
 * 
 * @author Steve Githens (sgithens@caret.cam.ac.uk)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationAssignProducer implements ViewComponentProducer, NavigationCaseReporter {

   public static final String VIEW_ID = "evaluation_assign";
   public String getViewID() {
      return VIEW_ID;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvaluationBean evaluationBean;
   public void setEvaluationBean(EvaluationBean evaluationBean) {
      this.evaluationBean = evaluationBean;
   }

   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }

   private HierarchyRenderUtil hierUtil;
   public void setHierarchyRenderUtil(HierarchyRenderUtil util) {
      hierUtil = util;
   }

   private ExternalHierarchyLogic hierarchyLogic;
   public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
      this.hierarchyLogic = logic;
   }

   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      UIMessage.make(tofill, "page-title", "assigneval.page.title");

      UIInternalLink.make(tofill, "summary-link", UIMessage.make("summary.page.title"),
            new SimpleViewParameters(SummaryProducer.VIEW_ID));	

      UIMessage.make(tofill, "create-eval-title", "starteval.page.title");
      UIMessage.make(tofill, "eval-settings-title", "evalsettings.page.title");

      UIMessage.make(tofill, "assign-eval-edit-page-title", "assigneval.assign.page.title", new Object[] {evaluationBean.eval.getTitle()});
      UIMessage.make(tofill, "assign-eval-instructions", "assigneval.assign.instructions", new Object[] {evaluationBean.eval.getTitle()});

      UIForm form = UIForm.make(tofill, "eval-assign-form");

      UIMessage.make(form, "name-header", "assigneval.name.header");
      UIMessage.make(form, "select-header", "assigneval.select.header");		

      /*
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
            UIBoundBoolean hierarchyCheckbox = UIBoundBoolean.make(form, "use-hierarchynodes-checkbox");
            UIMessage.make(form, "assign-hierarchy-title", "assigneval.page.hier.title");
            
            UIOutput hierarchyDiv = UIOutput.make(form, "hierarchy-assignment-area");
            
            initJS.append(HTMLUtil.emitJavascriptCall("EvalSystem.hideAndShowRegionWithCheckbox", 
                  new String[] {hierarchyDiv.getFullID(), hierarchyCheckbox.getFullID()}));
            hierUtil.renderSelectHierarchyNodesTree(form, "hierarchy-tree-select:", "", "" );
         }

         // display checkboxes for selecting the non-hierarchy groups
         UIBoundBoolean evalGroupCheckbox = UIBoundBoolean.make(form, "use-evalgroups-checkbox");
         UIMessage.make(form, "assign-evalgroups-title", "assigneval.page.groups.title");
         UIOutput evalGroupDiv = UIOutput.make(form, "evalgroups-assignment-area");
         initJS.append(HTMLUtil.emitJavascriptCall("EvalSystem.hideAndShowRegionWithCheckbox", 
               new String[] {evalGroupDiv.getFullID(), evalGroupCheckbox.getFullID()}));
         
         String[] nonAssignedEvalGroupIDs = getEvalGroupIDsNotAssignedInHierarchy(evalGroups).toArray(new String[] {});
         for (int i = 0; i < nonAssignedEvalGroupIDs.length; i++) {
            UIBranchContainer checkboxRow = UIBranchContainer.make(form, "sites:", i+"");
            if (i % 2 == 0) {
               checkboxRow.decorators = new DecoratorList( new UIStyleDecorator("itemsListOddLine") ); // must match the existing CSS class
            }
            UIBoundBoolean checkbox = UIBoundBoolean.make(checkboxRow, "siteId", 
                  "evaluationBean.selectedEvalGroupIDsMap."+nonAssignedEvalGroupIDs[i]);
            // get title from the map since it is faster
            UIOutput title = UIOutput.make(checkboxRow, "siteTitle", groupsMap.get(nonAssignedEvalGroupIDs[i]).title );
            UILabelTargetDecorator.targetLabel(title, checkbox); // make title a label for checkbox
         }
      } else {
         // TODO tell user there are no groups to assign to
      }
      
      /*
       * Selection GUI for new and existing ad-hoc groups.
       */
      Boolean useAdHocGroups = (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_GROUPS);
      if (useAdHocGroups) {
         UIBoundBoolean addhocGroupCheckbox = UIBoundBoolean.make(form, "use-adhocgroups-checkbox");
         UIMessage.make(form, "assign-adhocgroups-title", "assigneval.page.adhocgroups.title");
         UIOutput addhocGroupDiv = UIOutput.make(form, "newadhocgroup-assignment-area");
         initJS.append(HTMLUtil.emitJavascriptCall("EvalSystem.hideAndShowRegionWithCheckbox", 
               new String[] {addhocGroupDiv.getFullID(), addhocGroupCheckbox.getFullID()}));
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

      UICommand.make(form, "cancel-button", UIMessage.make("general.cancel.button"), "#{evaluationBean.cancelAssignAction}");
      UICommand.make(form, "editSettings", UIMessage.make("assigneval.edit.settings.button"), "#{evaluationBean.backToSettingsAction}");
      UICommand.make(form, "confirmAssignCourses", UIMessage.make("assigneval.save.assigned.button"), "#{evaluationBean.confirmAssignCoursesAction}");
   
      // Setup JavaScript for the collapse able sections
      //UIInitBlock.make(tofill, "initJavaScript", "EvalSystem.initEvalAssign", new Object[] {
      //      new String[] {"one","two"}, new String[] {"three","four"}
      //});
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
    * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
    */
   @SuppressWarnings("unchecked")
   public List reportNavigationCases() {
      List i = new ArrayList();
      i.add(new NavigationCase(SummaryProducer.VIEW_ID, new SimpleViewParameters(SummaryProducer.VIEW_ID)));
      i.add(new NavigationCase(EvaluationSettingsProducer.VIEW_ID, new SimpleViewParameters(EvaluationSettingsProducer.VIEW_ID)));
      i.add(new NavigationCase(EvaluationAssignConfirmProducer.VIEW_ID, new SimpleViewParameters(EvaluationAssignConfirmProducer.VIEW_ID)));
      return i;
   }


}