package org.sakaiproject.evaluation.tool.reporting;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/** 
 * This is a central place for the code to check whether or not someone can view
 * the results of an evaluation.
 * 
 * The rules are:
 * 1) Is this user an admin
 * 2) Is this user the evaluation owner
 * 3) Is evaluation.resultsPrivate true? Then no one below can view the results.
 * 4) Do the system settings allow instructors to view the evaluation?
 * 4b) Is this user an instructor in a group assigned to this evaluation
 * 5) Do the system settings allow students to view the evaluation?
 * 5b) Is this user a student who is allowed to view results
 * 6) Is this user a hierarchical admin
 * 
 * @author Steven Githens
 */
public class ReportingPermissions {

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvalSettings evalSettings;
   public void setEvalSettings(EvalSettings evalSettings) {
      this.evalSettings = evalSettings;
   }

   /**
    * Decide whether the current user can view the responses for an evaluation
    * and set of groups that participated in it.
    * 
    * @param evaluation The EvalEvaluation object we are looking at responses for.
    * @param groupIds The String array of Group IDs we want to view results for.
    * This usually look like "/site/mysite".
    * @return Yes or no answer if you can view the responses.
    */
   public boolean canViewEvaluationResponses(EvalEvaluation evaluation, String[] groupIds) {
      String currentUserId = externalLogic.getCurrentUserId();
      boolean canViewResponses;
      
      // TODO 1 and 2 will be replaced by ExternalLogic.checkUserPermission(String userId, String ownerId)
      
      // 1) Is this user an admin?
      if (externalLogic.isUserAdmin(currentUserId)) {
         canViewResponses = true;
      }
      // 2) Is this user the evaluation owner?
      else if (currentUserId.equals(evaluation.getOwner())) {
         canViewResponses = true;
      }
      // 3
      else if (evaluation.getResultsPrivate()) {
         canViewResponses = false;
      }
      // 4
      else if (canViewEvaluationResponsesAsInstructor(evaluation, currentUserId, groupIds)) {
         canViewResponses = true;
      }
      // 5
      else if (canViewEvaluationResponsesAsStudent(evaluation, currentUserId, groupIds)) {
         canViewResponses = true;
      }
      else {
         canViewResponses = false;
      }

      // 6 Is this user a hierarchical admin? 
      // TODO Not all the infrastructure is ready for this yet.

      return canViewResponses;
   }

   /**
    * This does the following steps from above.
    * 5) Do the system settings allow students to view the evaluation?
    * 5b) Is this user a student who is allowed to view results
    * 
    * @param eval
    * @param currentUserId
    * @param groupIds
    * @return
    */
   private boolean canViewEvaluationResponsesAsStudent(EvalEvaluation eval, String currentUserId, String[] groupIds) {
      Boolean studentAllowedViewResults = 
         (Boolean) evalSettings.get(EvalSettings.STUDENT_VIEW_RESULTS);
      boolean allowedToView = true;
      if (studentAllowedViewResults) {
         for (String groupId: groupIds) {
            if (!externalLogic.isUserAllowedInEvalGroup(currentUserId, 
                  EvalConstants.PERM_TAKE_EVALUATION, groupId)) {
               allowedToView = false;
               break;
            }
         }
      }
      else {
         allowedToView = false;
      }
      
      return allowedToView;
   }

   /**
    * This completes the following checks from above:
    * 4) Do the system settings allow instructors to view the evaluation?
    * 4b) Is this user an instructor in a group assigned to this evaluation
    * 
    * @param eval
    * @param currentUserId
    * @param groupIds
    * @return
    */
   private boolean canViewEvaluationResponsesAsInstructor(EvalEvaluation eval, String currentUserId, String[] groupIds) {
      Boolean instructorAllowedViewResults = 
         (Boolean) evalSettings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
      boolean allowedToView = true;
      if (instructorAllowedViewResults) {
         for (String groupId: groupIds) {
            if (!externalLogic.isUserAllowedInEvalGroup(currentUserId, 
                  EvalConstants.PERM_BE_EVALUATED, groupId)) {
               allowedToView = false;
               break;
            }
         }
      }
      else {
         allowedToView = false;
      }
      
      return allowedToView;
   }
}
