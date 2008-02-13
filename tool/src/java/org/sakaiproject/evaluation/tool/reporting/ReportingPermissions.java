package org.sakaiproject.evaluation.tool.reporting;

import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/* 
 * This is a central place for the code to check whether or not someone can view
 * the results of an evaluation.
 * 
 * The rules are:
 * 1) Is this user an admin
 * 2) Is this user the evaluation owner
 * 3) Is this user an instructor in a group assigned to this evaluation
 * 4) Is this user a hierarchical admin
 * 5) Is this user a student who is allowed to view results (UMD)
 */
public class ReportingPermissions {

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   
   /*
    * Decide whether the current user can view the responses for an evaluation
    * and set of groups that participated in it.
    */
   public boolean canViewEvaluationResponses(EvalEvaluation evaluation, String[] groupIds) {
    //FIXME handle above cases in class comments
      String currentUserId = externalLogic.getCurrentUserId();
      if (!currentUserId.equals(evaluation.getOwner()) && 
            !externalLogic.isUserAdmin(currentUserId)) { // TODO - this check is no good, we need a real one -AZ
         return false;
      }
      else {
         return true;
      }
   }
}
