/**
 * $Id$
 * $URL$
 * EvaluationAssignmentsProducer.java - evaluation - Oct 05, 2006 11:32:44 AM - kahuja
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
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.entity.AssignGroupEntityProvider;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
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
public class EvaluationAssignmentsProducer implements ViewComponentProducer, ViewParamsReporter {

   public static final String VIEW_ID = "evaluation_assignments";
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

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;
      if (evalViewParams.evaluationId == null) {
         throw new IllegalArgumentException("Cannot access this view unless the evaluationId is set");
      }

      /**
       * This is the evaluation we are working with on this page,
       * this should ONLY be read from, do not change any of these fields
       */
      EvalEvaluation evaluation = evaluationService.getEvaluationById(evalViewParams.evaluationId);
      Long evaluationId = evalViewParams.evaluationId;

      /*
       * top links here
       */
      UIInternalLink.make(tofill, "summary-link", 
            UIMessage.make("summary.page.title"), 
            new SimpleViewParameters(SummaryProducer.VIEW_ID));

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

      // show modify assignments link as long as the eval is active or earlier
      String evalState = evaluationService.updateEvaluationState(evaluationId);
      if (EvalUtils.checkStateBefore(evalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
         UIInternalLink.make(tofill, "modifyAssignmentsLink", UIMessage.make("evaluationassignments.add.assigns.link"), 
               new EvalViewParameters(EvaluationAssignProducer.VIEW_ID, evaluationId) );
      }

      Map<Long, List<EvalAssignGroup>> groupsMap = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, null);
      List<EvalAssignGroup> assignGroups = groupsMap.get(evalViewParams.evaluationId);

      // get all evaluator user assignments to count the total enrollments
      HashMap<String, List<EvalAssignUser>> groupIdToEAUList = new HashMap<String, List<EvalAssignUser>>();
      List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(evaluationId, null, null, 
              EvalAssignUser.TYPE_EVALUATOR, null, null, null);
      for (EvalAssignUser evalAssignUser : userAssignments) {
          String groupId = evalAssignUser.getEvalGroupId();
          if (groupIdToEAUList.containsKey(groupId)) {
              groupIdToEAUList.put(groupId, new ArrayList<EvalAssignUser>());
          }
          groupIdToEAUList.get(groupId).add(evalAssignUser);
      }

      // show the assigned groups
      if (assignGroups.size() > 0) {
         UIBranchContainer groupsBranch = UIBranchContainer.make(tofill, "showSelectedGroups:");
         for (EvalAssignGroup assignGroup : assignGroups) {
            if (assignGroup.getNodeId() == null) {
               // only include directly added groups (i.e. nodeId is null)
               String evalGroupId = assignGroup.getEvalGroupId();
               EvalGroup group = commonLogic.makeEvalGroupObject(evalGroupId);
               UIBranchContainer groupRow = UIBranchContainer.make(groupsBranch, "groups:", evalGroupId);
               UIOutput.make(groupRow, "groupTitle", group.title);
               UIOutput.make(groupRow, "groupType", group.type);
               // direct link to the group eval
               UILink.make(groupRow, "directGroupLink", UIMessage.make("evaluationassignconfirm.direct.link"), 
                     commonLogic.getEntityURL(AssignGroupEntityProvider.ENTITY_PREFIX, assignGroup.getId().toString()));
               // calculate the enrollments count
               int enrollmentCount = groupIdToEAUList.get(evalGroupId) == null ? 0 : groupIdToEAUList.get(evalGroupId).size();
               UIOutput.make(groupRow, "enrollment", enrollmentCount + "");
            }
         }
      } else {
         UIMessage.make(tofill, "noGroupsSelected", "evaluationassignments.no.groups");
      }

      // show the assigned hierarchy nodes
      Boolean showHierarchy = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
      if (showHierarchy) {
         UIBranchContainer hierarchyBranch = UIBranchContainer.make(tofill, "showHierarchy:");
         List<EvalAssignHierarchy> assignHierarchies = evaluationService.getAssignHierarchyByEval(evaluationId);
         if (assignHierarchies.size() > 0) {
            UIBranchContainer nodesBranch = UIBranchContainer.make(tofill, "showSelectedNodes:");
            for (EvalAssignHierarchy assignHierarchy : assignHierarchies) {
               EvalHierarchyNode node = hierLogic.getNodeById(assignHierarchy.getNodeId());

               UIBranchContainer nodeRow = UIBranchContainer.make(nodesBranch, "nodes:node");
               UIOutput.make(nodeRow, "nodeTitle", node.title);
               UIOutput.make(nodeRow, "nodeAbbr", node.description);

               // now get the list of groups related to this node
               List<EvalAssignGroup> nodeAssignGroups = new ArrayList<EvalAssignGroup>();
               for (EvalAssignGroup assignGroup : assignGroups) {
                  if (assignGroup.getNodeId() != null
                        && assignHierarchy.getNodeId().equals(assignGroup.getNodeId()) ) {
                     nodeAssignGroups.add(assignGroup);
                  }
               }

               // now render the list of groups related to this node
               UIBranchContainer nodeRowGroups = UIBranchContainer.make(hierarchyBranch, "nodes:groups");
               if (nodeAssignGroups.size() == 0) {
                  UIMessage.make(nodeRowGroups, "noGroupsForNode", "evaluationassignments.no.groups");
               } else {
                  UIBranchContainer groupsTable = UIBranchContainer.make(nodeRowGroups, "nodeGroupTable:");
                  for (EvalAssignGroup assignGroup : nodeAssignGroups) {
                     String evalGroupId = assignGroup.getEvalGroupId();
                     EvalGroup group = commonLogic.makeEvalGroupObject(evalGroupId);
                     UIBranchContainer groupRow = UIBranchContainer.make(groupsTable, "groups:", evalGroupId);
                     UIOutput.make(groupRow, "groupTitle", group.title);
                     UIOutput.make(groupRow, "groupType", group.type);
                     // direct link to the group eval
                     UILink.make(groupRow, "directGroupLink", UIMessage.make("evaluationassignconfirm.direct.link"), 
                           commonLogic.getEntityURL(AssignGroupEntityProvider.ENTITY_PREFIX, assignGroup.getId().toString()));
                     // calculate the enrollments count
                     int enrollmentCount = groupIdToEAUList.get(evalGroupId) == null ? 0 : groupIdToEAUList.get(evalGroupId).size();
                     UIOutput.make(groupRow, "enrollment", enrollmentCount + "");
                  }
               }
               
            }
         } else {
            UIMessage.make(hierarchyBranch, "noNodesSelected", "evaluationassignments.no.nodes");
         }
      }

      // show the back button
      UIMessage.make(tofill, "cancel-button", "general.back.button");

   }


   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
    */
   public ViewParameters getViewParameters() {
      return new EvalViewParameters();
   }

}
