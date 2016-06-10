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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Collections;

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
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.utils.RenderingUtils;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Show the currently assigned courses or confirm the assignment and create the evaluation
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationAssignmentsProducer extends EvalCommonProducer implements ViewParamsReporter {

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

   private NavBarRenderer navBarRenderer;
   public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
   }

   private Locale locale;
   public void setLocale( Locale locale )
   {
      this.locale = locale;
   }

   private RenderingUtils renderingUtils;
   public void setRenderingUtils(RenderingUtils renderingUtils) {
     this.renderingUtils = renderingUtils;
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      EvalViewParameters evalViewParams = (EvalViewParameters) viewparams;
      if (evalViewParams.evaluationId == null) {
         throw new IllegalArgumentException("Cannot access this view unless the evaluationId is set");
      }

      /**
       * This is the evaluation we are working with on this page,
       * this should ONLY be read from, do not change any of these fields
       */
      Long evaluationId = evalViewParams.evaluationId;

      /*
       * top links here
       */
      navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());
      UIInternalLink.make(tofill, "eval-settings-link",
              UIMessage.make("evalsettings.page.title"),
              new EvalViewParameters(EvaluationSettingsProducer.VIEW_ID, evalViewParams.evaluationId) );


      // normal page content

      // show modify assignments link as long as the eval is active or earlier
      String evalState = evaluationService.updateEvaluationState(evaluationId);
      if (EvalUtils.checkStateBefore(evalState, EvalConstants.EVALUATION_STATE_ACTIVE, true)) {
         UIInternalLink.make(tofill, "modifyAssignmentsLink", UIMessage.make("evaluationassignments.add.assigns.link"), 
               new EvalViewParameters(EvaluationAssignProducer.VIEW_ID, evaluationId) );

         DateFormat df = DateFormat.getDateInstance( DateFormat.MEDIUM, locale );
         EvalEvaluation eval = evaluationService.getEvaluationById( evaluationId );
         UIMessage.make( tofill, "modifyAssignmentsInfoMsg", "evaluationassignconfirm.eval.assign.instructions", 
               new Object[] { df.format( eval.getStartDate() ) } );
      }

      Map<Long, List<EvalAssignGroup>> groupsMap = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, null);
      List<EvalAssignGroup> assignGroups = groupsMap.get(evalViewParams.evaluationId);
      // get all evaluator user assignments to count the total enrollments
      HashMap<String, List<EvalAssignUser>> groupIdToEAUList = new HashMap<>();
      List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(evaluationId, null, null, 
              EvalAssignUser.TYPE_EVALUATOR, null, null, null);
      for (EvalAssignUser evalAssignUser : userAssignments) {
          String groupId = evalAssignUser.getEvalGroupId();
          if (! groupIdToEAUList.containsKey(groupId)) {
              groupIdToEAUList.put(groupId, new ArrayList<>());
          }
          groupIdToEAUList.get(groupId).add(evalAssignUser);
      }

      // show the assigned groups
      int countUnpublishedGroups = 0;
      if (assignGroups.size() > 0) {
         //find out is this evaluation will contain any Instructor/TA questions based in it's template
         List<String> validItemCategories = renderingUtils.extractCategoriesInTemplate(
           evaluationService.getEvaluationById(evaluationId).getTemplate().getId());
         boolean hasInstructorQuestions = validItemCategories.contains(EvalConstants.ITEM_CATEGORY_INSTRUCTOR);
         boolean hasAssistantQuestions = validItemCategories.contains(EvalConstants.ITEM_CATEGORY_ASSISTANT);

         if(hasInstructorQuestions){
           UIMessage.make(tofill, "title-instructors", "evaluationassignments.groups.instructors.header");
         }
         if(hasAssistantQuestions){
           UIMessage.make(tofill, "title-assistants", "evaluationassignments.groups.assistants.header");
         }

         Collections.sort(assignGroups, (Object arg0, Object arg1) ->
         {
             EvalAssignGroup ag1 = (EvalAssignGroup) arg0;
             EvalAssignGroup ag2 = (EvalAssignGroup) arg1;
             EvalGroup g1 = commonLogic.makeEvalGroupObject(ag1.getEvalGroupId());
             EvalGroup g2 = commonLogic.makeEvalGroupObject(ag2.getEvalGroupId());
             return g1.title.compareToIgnoreCase(g2.title);
         });
		 
         UIBranchContainer groupsBranch = UIBranchContainer.make(tofill, "showSelectedGroups:");
         for (EvalAssignGroup assignGroup : assignGroups) {
            if (assignGroup.getNodeId() == null) {
               // only include directly added groups (i.e. nodeId is null)
               String evalGroupId = assignGroup.getEvalGroupId();
           	   boolean isPublished = commonLogic.isEvalGroupPublished(evalGroupId);
               EvalGroup group = commonLogic.makeEvalGroupObject(evalGroupId);
               UIBranchContainer groupRow = UIBranchContainer.make(groupsBranch, "groups:", evalGroupId);
               UIOutput title = UIOutput.make(groupRow, "groupTitle", group.title);
               if(! isPublished){
               	title.decorate( new UIStyleDecorator("elementAlertFront") );
               	countUnpublishedGroups ++;
               }
               UIOutput.make(groupRow, "groupType", group.type);
               // direct link to the group eval
               UILink.make(groupRow, "directGroupLink", UIMessage.make("evaluationassignconfirm.direct.link"), 
                     commonLogic.getEntityURL(AssignGroupEntityProvider.ENTITY_PREFIX, assignGroup.getId().toString()));
               
               //Add user selection info as a result of changes in EVALSYS-660
                 List<EvalAssignUser> selectedUsers = new ArrayList<>();

                 if(hasInstructorQuestions){
              
                   selectedUsers = evaluationService.getParticipantsForEval(evalViewParams.evaluationId, null, new String[]{evalGroupId}, EvalAssignUser.TYPE_EVALUATEE, EvalAssignUser.STATUS_LINKED, null, null);
                   int enrollmentCountInstructors = selectedUsers == null ? 0 : selectedUsers.size();
                   UIOutput.make(groupRow, "enrollment-instructors", enrollmentCountInstructors + "");
                 }
                 if(hasAssistantQuestions){
                   selectedUsers = evaluationService.getParticipantsForEval(evalViewParams.evaluationId, null, new String[]{evalGroupId}, EvalAssignUser.TYPE_ASSISTANT, EvalAssignUser.STATUS_LINKED, null, null);
                   int enrollmentCountAssistants = selectedUsers == null ? 0 : selectedUsers.size();
                    UIOutput.make(groupRow, "enrollment-assistants", enrollmentCountAssistants + "");
                 }

                 // calculate the enrollments count
                 int enrollmentCount = groupIdToEAUList.get(evalGroupId) == null ? 0 : groupIdToEAUList.get(evalGroupId).size();
                 UIOutput.make(groupRow, "enrollment", enrollmentCount + "");
                
            }
         }
      } else {
         UIMessage.make(tofill, "noGroupsSelected", "evaluationassignments.no.groups");
      }
      
      if (countUnpublishedGroups > 0){
      	UIMessage.make(tofill, "eval-instructions-group-notpublished", "assigneval.assign.instructions.notpublished");
      }

      // show the assigned hierarchy nodes
      Boolean showHierarchy = (Boolean) settings.get(EvalSettings.DISPLAY_HIERARCHY_OPTIONS);
      if (showHierarchy) {
         UIBranchContainer hierarchyBranch = UIBranchContainer.make(tofill, "showHierarchy:");
         List<EvalAssignHierarchy> assignHierarchies = evaluationService.getAssignHierarchyByEval(evaluationId);
         if (assignHierarchies.size() > 0) {
            List<String> populatedNodes = new ArrayList<>();
            UIBranchContainer nodesBranch = UIBranchContainer.make(tofill, "showSelectedNodes:");
            for (EvalAssignHierarchy assignHierarchy : assignHierarchies) {
               if (!populatedNodes.contains(assignHierarchy.getNodeId())) {
                  EvalHierarchyNode node = hierLogic.getNodeById(assignHierarchy.getNodeId());

                  UIBranchContainer nodeRow = UIBranchContainer.make(nodesBranch, "nodes:node");
                  UIOutput.make(nodeRow, "nodeTitle", node.title);
                  UIOutput.make(nodeRow, "nodeAbbr", node.description);

                  // now get the list of groups related to this node
                  List<EvalAssignGroup> nodeAssignGroups = new ArrayList<>();
                  for (EvalAssignGroup assignGroup : assignGroups) {
                     if (assignGroup.getNodeId() != null
                        && assignHierarchy.getNodeId().equals(assignGroup.getNodeId()) ) {
                        nodeAssignGroups.add(assignGroup);
                     }
                  }

                  // now render the list of groups related to this node
                  UIBranchContainer nodeRowGroups = UIBranchContainer.make(nodesBranch, "nodes:groups");
                  if (nodeAssignGroups.isEmpty()) {
                     UIMessage.make(nodeRowGroups, "noGroupsForNode", "evaluationassignments.no.groups");
                  } else {
                     UIBranchContainer groupsTable = UIBranchContainer.make(nodeRowGroups, "nodeGroupTable:");
            	      //UIBranchContainer groupsTable = UIBranchContainer.make(tofill, "nodeGroupTable:");
                     for (EvalAssignGroup assignGroup : nodeAssignGroups) {
                        String evalGroupId = assignGroup.getEvalGroupId();
                        EvalGroup group = commonLogic.makeEvalGroupObject(evalGroupId);
                        UIBranchContainer groupRow = UIBranchContainer.make(groupsTable, "hierGroups:", evalGroupId);
                        UIOutput.make(groupRow, "hierGroupTitle", group.title);
                        UIOutput.make(groupRow, "hierGroupType", group.type);
                        // direct link to the group eval
                        UILink.make(groupRow, "directHierGroupLink", UIMessage.make("evaluationassignconfirm.direct.link"), 
                           commonLogic.getEntityURL(AssignGroupEntityProvider.ENTITY_PREFIX, assignGroup.getId().toString()));
                     
                        // calculate the enrollments count
                        int enrollmentCount = groupIdToEAUList.get(evalGroupId) == null ? 0 : groupIdToEAUList.get(evalGroupId).size();
                        UIOutput.make(groupRow, "hierGrpEnrollment", enrollmentCount + "");
                     }
                  }
                  
                  populatedNodes.add(assignHierarchy.getNodeId());
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
