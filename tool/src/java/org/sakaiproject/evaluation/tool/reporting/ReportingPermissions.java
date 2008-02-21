package org.sakaiproject.evaluation.tool.reporting;

import java.util.Set;
import java.util.HashSet;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
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
   
   private EvaluationDao evalDao;
   public void setEvaluationDao(EvaluationDao dao) {
      this.evalDao = dao;
   }
   
   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }
   
   /**
    * This is a sort of partial security check based off of the full 
    * canViewEvaluationResponses method.
    * 
    * This is primarily needed for the Choose Groups page in reporting. In this
    * case, we want to genuinely check most of the permissions, except we don't
    * actually know what Group ID's we are looking at (because we are about to 
    * choose them).  Instead, we want to check almost all of the items in the 
    * rules, and if they pass successfully, return the Groups that we are able
    * to choose from for report viewing.
    *
    * @param evaluation
    * @return The array of groupIds we can choose from for viewing responses in 
    * this evaluation.  If you cannot view the responses from any groups in this
    * evaluation, this will return an empty list.
    */
   public String[] chooseGroupsPartialCheck(EvalEvaluation evaluation) {
      String currentUserId = externalLogic.getCurrentUserId();
      Set<String> groupIdsTogo = new HashSet<String>();
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
      else {
         canViewResponses = true;
      }
      // 6 TODO Insfrustructure isn't available for this yet.

      /*
       * If we can view the responses, based on the preliminary checks above,
       * we will create a Set of the groups we are allowed to view based on the
       * possibility of us having the instructor and student oriented permission
       * locks.
       */
      if (canViewResponses) {
         Boolean instructorAllowedViewResults = 
            (Boolean) evalSettings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
         if (instructorAllowedViewResults) {
            Set<String> tempGroupIds = 
                  evalDao.getViewableEvalGroupIds(evaluation.getId(), EvalConstants.PERM_BE_EVALUATED, null);
            for (String tempGroupId: tempGroupIds) {
               if (externalLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_BE_EVALUATED, tempGroupId)) {
                  groupIdsTogo.add(tempGroupId);
               }
            }
         }

         Boolean studentAllowedViewResults = 
            (Boolean) evalSettings.get(EvalSettings.STUDENT_VIEW_RESULTS);
         if (studentAllowedViewResults) {
            Set<String> tempGroupIds =
                  evalDao.getViewableEvalGroupIds(evaluation.getId(), EvalConstants.PERM_TAKE_EVALUATION, null);
            for (String tempGroupId: tempGroupIds) {
               if (externalLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_TAKE_EVALUATION, tempGroupId)) {
                  groupIdsTogo.add(tempGroupId);
               }
            }
         }
      }
      
      return groupIdsTogo.toArray(new String[] {});
   }

   /**
    * Decide whether the current user can view the responses for an evaluation
    * and set of groups that participated in it.
    * 
    * TODO FIXME I think 4 and 5 may need to be combined. It seems possible that
    * you could have Instructor viewing permissions in one of the groups, and 
    * Student viewing permissions in another, so the Set of Group IDs from both
    * need to be combined to be checked.
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
      boolean allowedToView = false;
      if (studentAllowedViewResults) {
         Set<String> viewableIds = evalDao.getViewableEvalGroupIds(eval.getId(), EvalConstants.PERM_TAKE_EVALUATION, groupIds);
         if (viewableIds.size() == groupIds.length) {
            allowedToView = true;
         }
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
      boolean allowedToView = false;
      if (instructorAllowedViewResults) {
         Set<String> viewableIds = evalDao.getViewableEvalGroupIds(eval.getId(), EvalConstants.PERM_BE_EVALUATED, groupIds);
         if (viewableIds.size() == groupIds.length) {
            allowedToView = true;
         }
      }

      return allowedToView;
   }
}
