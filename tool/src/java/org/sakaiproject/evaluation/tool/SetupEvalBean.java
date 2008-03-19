/**
 * $Id$
 * $URL$
 * SetupEvalBean.java - evaluation - Mar 18, 2008 4:38:20 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.locators.EvaluationBeanLocator;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;


/**
 * This action bean helps with the evaluation setup process where needed,
 * this is a pea
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SetupEvalBean {

   /**
    * This should be set to true while we are creating an evaluation
    */
   public boolean creatingEval = false;
   /**
    * This should be set to the evalId we are currently working with
    */
   public Long evaluationId;

   // These are for the Assign screen, which is now bound by UIBoundBooleans
   public Map<String, Boolean> selectedEvalGroupIDsMap = new HashMap<String, Boolean>();
   public Map<String, Boolean> selectedEvalHierarchyNodeIDsMap = new HashMap<String, Boolean>();

   // TODO are these still needed?
// public String[] selectedEvalGroupIds;
// public String[] selectedEvalHierarchyNodeIds;


   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private ExternalHierarchyLogic hierarchyLogic;
   public void setExternalHierarchyLogic(ExternalHierarchyLogic logic) {
      this.hierarchyLogic = logic;
   }

   private EvalEvaluationSetupService evaluationSetupService;
   public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
      this.evaluationSetupService = evaluationSetupService;
   }

   private EvaluationBeanLocator evaluationBeanLocator;
   public void setEvaluationBeanLocator(EvaluationBeanLocator evaluationBeanLocator) {
      this.evaluationBeanLocator = evaluationBeanLocator;
   }

   private TargettedMessageList messages;
   public void setMessages(TargettedMessageList messages) {
      this.messages = messages;
   }

   private Locale locale;
   public void setLocale(Locale locale){
      this.locale=locale;
   }


   DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
   public void init() {
      df = DateFormat.getDateInstance(DateFormat.LONG, locale);
   }

   
   // Action bindings

   /**
    * Handles removal action from the remove eval view
    */
   public String removeEvalAction(){
      EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
      evaluationSetupService.deleteEvaluation(evaluationId, externalLogic.getCurrentUserId());
      messages.addMessage( new TargettedMessage("controlevaluations.delete.user.messagee",
            new Object[] { eval.getTitle() }, TargettedMessage.SEVERITY_INFO));
      return "success";
   }


   // NOTE: these are the simple navigation methods
   // 4 steps to create an evaluation: 1) Create -> 2) Settings -> 3) Assign -> 4) Confirm/Save

   /**
    * Completed the initial creation page where the template is chosen
    */
   public String completeCreateAction() {
      evaluationBeanLocator.saveAll();
      return "evalSettings";
   }

   /**
    * Updated or initially set the evaluation settings
    */
   public String completeSettingsAction() {
      EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);
      evaluationBeanLocator.saveAll();

      String destination = "controlEvals";
      if (creatingEval) {
         destination = "evalAssign";
      } else {
         messages.addMessage( new TargettedMessage("evalsettings.updated.message",
               new Object[] { eval.getTitle() }, TargettedMessage.SEVERITY_INFO));
      }
      return destination;
   }

   // NOTE: There is no action for the 3) assign step because that one just passes the data straight to the confirm view

   // TODO - how do we handle removing assignments? (Currently not supported)

   /**
    * Complete the creation process for an evaluation (view all the current settings and assignments and create eval/assignments)
    */
   public String completeConfirmAction() {
      // TODO this check is identical to the one above -AZ
      // make sure that the submitted nodes are valid and populate the nodes list
      Set<EvalHierarchyNode> nodes = null;
      if (! selectedEvalHierarchyNodeIDsMap.isEmpty()) {
         String[] selectedHierarchyNodeIds = makeArrayFromBooleanMap(selectedEvalHierarchyNodeIDsMap);
         nodes = hierarchyLogic.getNodesByIds(selectedHierarchyNodeIds);
         if (nodes.size() != selectedHierarchyNodeIds.length) {
            throw new IllegalArgumentException("Invalid set of hierarchy node ids submitted which "
                  + "includes node Ids which are not in the hierarchy: " + ArrayUtils.arrayToString(selectedHierarchyNodeIds));
         }
      } else {
         nodes = new HashSet<EvalHierarchyNode>();
      }

      // at least 1 node or group must be selected
      if (selectedEvalGroupIDsMap.isEmpty() 
            && nodes.isEmpty() ) {
         messages.addMessage( new TargettedMessage("assigneval.invalid.selection",
               new Object[] {}, TargettedMessage.SEVERITY_ERROR));
         return "fail";
      }

      if (creatingEval) {
         // save eval and assign groups
         EvalEvaluation eval = evaluationService.getEvaluationById(evaluationId);

         // save the new evaluation state (moving from partial)
         eval.setState( EvalUtils.getEvaluationState(eval, true) ); // set the state according to dates only
         evaluationSetupService.saveEvaluation(eval, externalLogic.getCurrentUserId());

         // NOTE - this allows the evaluation to be saved with zero assign groups if this fails

         // expand the hierarchy to include all nodes below this one
         Set<String> allNodeIds = hierarchyLogic.getAllChildrenNodes(nodes, true);

         // save all the assignments (hierarchy and group)
         String[] selectedGroupIds = makeArrayFromBooleanMap(selectedEvalGroupIDsMap);
         List<EvalAssignHierarchy> assignedHierList = 
            evaluationSetupService.addEvalAssignments(evaluationId, 
                  allNodeIds.toArray(new String[allNodeIds.size()]), selectedGroupIds);

         // failsafe check (to make sure we are not creating an eval with no assigned groups)
         if (assignedHierList.isEmpty()) {
            evaluationSetupService.deleteEvaluation(evaluationId, externalLogic.getCurrentUserId());
            throw new IllegalStateException("Invalid evaluation created with no assignments! Destroying evaluation: " + evaluationId);
         }

         messages.addMessage( new TargettedMessage("controlevaluations.create.user.message",
               new Object[] { eval.getTitle(), df.format(eval.getStartDate()) }, 
               TargettedMessage.SEVERITY_INFO));
      } else {
         // just assigning groups
         // expand the hierarchy to include all nodes below this one
         Set<String> allNodeIds = hierarchyLogic.getAllChildrenNodes(nodes, true);

         // save all the assignments (hierarchy and group)
         String[] selectedGroupIds = makeArrayFromBooleanMap(selectedEvalGroupIDsMap);
         evaluationSetupService.addEvalAssignments(evaluationId, 
                  allNodeIds.toArray(new String[allNodeIds.size()]), selectedGroupIds);
      }
      return "controlEvals";
   }


   // NOTE: these are the support methods

   /**
    * Turn a boolean selection map into an array of the keys
    * 
    * @param booleanSelectionMap a map of string -> boolean (from RSF bound booleans)
    * @return an array of the keys where boolean is true
    */
   private String[] makeArrayFromBooleanMap(Map<String, Boolean> booleanSelectionMap) {
      List<String> hierNodeIdList = new ArrayList<String>();
      for (String hierNodeID: booleanSelectionMap.keySet()) {
         if (booleanSelectionMap.get(hierNodeID) == true) {
            hierNodeIdList.add(hierNodeID);
         }
      }
      String[] selectedHierarchyNodeIds = hierNodeIdList.toArray(new String[] {});
      return selectedHierarchyNodeIds;
   }


}
