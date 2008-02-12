package org.sakaiproject.evaluation.tool.reporting;

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

   public boolean canViewEvaluationResponses(EvalEvaluation eval, String[] groupIds) {
      return false;
   }
}
