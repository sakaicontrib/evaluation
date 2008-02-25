package org.sakaiproject.evaluation.tool.reporting;

import java.util.Date;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/** 
 * This is a central place for the code to check whether or not someone can view
 * the results of an evaluation.
 * 
 * The rules are:
 * 1) Is this user an admin
 * 1.5) DateRule-A
 * 2) Is this user the evaluation owner
 * 3) Is evaluation.resultsPrivate true? Then no one below can view the results.
 * 4) Do the system settings allow instructors to view the evaluation?
 * 4b) DateRule-B
 * 4c) Is this user an instructor in a group assigned to this evaluation
 * 5) Do the system settings allow students to view the evaluation?
 * 5b) DateRule-C
 * 5c) Is this user a student who is allowed to view results
 * 6) Is this user a hierarchical admin
 * 
 * There are also date rules:
 * DateRule-A) We have to be past the eval.viewDate (this is liable to change in the future)
 * DateRule-B) If the eval.studentsDate is null students cannot view, otherwise it
 *             must be past the current date.
 * DateRule-C) If the eval.instructorsDate is null instructors cannot view, 
 *             otherwise it must be past the current date
 * @author Steven Githens
 */
public class ReportingPermissions {
   private static Log log = LogFactory.getLog(ReportingPermissions.class);

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
   
   /*
    * Signature variation for convenience (especially if you only have the 
    * information from a ViewParams). See chooseGroupsPartialCheck(EvalEvaluation)
    * for full description)
    */
   public String[] chooseGroupsPartialCheck(Long evalId) {
      return chooseGroupsPartialCheck(evaluationService.getEvaluationById(evalId));
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
    * If the survey is anonymous the returned array will be empty.  This means
    * that you should not rely on this has the sole permission check, mostly 
    * just for populating the Choose Groups page, and redirecting if the length
    * of the returned groups is 0 or 1.
    */
   public String[] chooseGroupsPartialCheck(EvalEvaluation evaluation) {
      String currentUserId = externalLogic.getCurrentUserId();
      Set<String> groupIdsTogo = new HashSet<String>();
      boolean checkBasedOnRole;
      
      // TODO 1 and 2 will be replaced by ExternalLogic.checkUserPermission(String userId, String ownerId)

      // 1) Is this user an admin or evaluation owner
      // 2) Is this user the evaluation owner?
      if (evaluation.getViewDate().after(new Date())) {
         checkBasedOnRole = false;
      }
      else if (externalLogic.isUserAdmin(currentUserId) ||
            currentUserId.equals(evaluation.getOwner())) {
         checkBasedOnRole = false;
         groupIdsTogo.addAll(
               evalDao.getViewableEvalGroupIds(evaluation.getId(), 
                     EvalConstants.PERM_BE_EVALUATED, null));
         groupIdsTogo.addAll(
               evalDao.getViewableEvalGroupIds(evaluation.getId(), 
                     EvalConstants.PERM_TAKE_EVALUATION, null));
      }
      // 3
      else if (evaluation.getResultsPrivate()) {
         checkBasedOnRole = false;
      }
      else {
         checkBasedOnRole = true;
      }
      // 6 TODO Insfrustructure isn't available for this yet.

      /*
       * If we can view the responses, based on the preliminary checks above,
       * we will create a Set of the groups we are allowed to view based on the
       * possibility of us having the instructor and student oriented permission
       * locks.
       */
      if (checkBasedOnRole) {
         Boolean instructorAllowedViewResults = 
            (Boolean) evalSettings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
         if (instructorAllowedViewResults && evaluation.getInstructorsDate() != null 
               && evaluation.getInstructorsDate().before(new Date())) {
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
         if (studentAllowedViewResults && evaluation.getStudentsDate() != null
               && evaluation.getStudentsDate().before(new Date())) {
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
      // 1.5) Are we past the view date?
      else if (evaluation.getViewDate().after(new Date())) {
         canViewResponses = false;
      }
      // 2) Is this user the evaluation owner?
      else if (currentUserId.equals(evaluation.getOwner())) {
         canViewResponses = true;
      }
      // 3
      else if (evaluation.getResultsPrivate()) {
         canViewResponses = false;
      }
      // TODO FIXME 4 and 5 need to be combined, because it may not be mutually
      // exclusive. For example, if you are trying to view groups A, B, C, you
      // may be able to view A and B as an Instructor and C as a student. (But
      // *not* C as an Instructor.
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
    * 5b) DateRule-C
    * 5c) Is this user a student who is allowed to view results
    * 
    * DateRule-C) If the eval.instructorsDate is null instructors cannot view, 
    *             otherwise it must be past the current date
    * @param eval
    * @param currentUserId
    * @param groupIds
    * @return
    */
   private boolean canViewEvaluationResponsesAsStudent(EvalEvaluation eval, String currentUserId, String[] groupIds) {
      Boolean studentAllowedViewResults = 
         (Boolean) evalSettings.get(EvalSettings.STUDENT_VIEW_RESULTS);
      boolean allowedToView = false;
      if (eval.getStudentsDate() == null || eval.getStudentsDate().after(new Date())) {
         allowedToView = false;
      }
      else if (studentAllowedViewResults) {
         Set<String> viewableIds = evalDao.getViewableEvalGroupIds(eval.getId(), EvalConstants.PERM_TAKE_EVALUATION, groupIds);
         if (viewableIds.size() == groupIds.length) {
            allowedToView = true;
         }
         for (String groupId: viewableIds) {
            if (!externalLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_TAKE_EVALUATION, groupId)) {
               allowedToView = false;
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
    * 4b) DateRule-B
    * 4c) Is this user an instructor in a group assigned to this evaluation
    * 
    * DateRule-B) If the eval.studentsDate is null students cannot view, otherwise it
    *             must be past the current date.
    * @param eval
    * @param currentUserId
    * @param groupIds
    * @return
    */
   private boolean canViewEvaluationResponsesAsInstructor(EvalEvaluation eval, String currentUserId, String[] groupIds) {
      Boolean instructorAllowedViewResults = 
         (Boolean) evalSettings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
      boolean allowedToView = false;
      if (eval.getInstructorsDate() == null || eval.getInstructorsDate().after(new Date())) {
         allowedToView = false;
      }
      else if (instructorAllowedViewResults) {
         Set<String> viewableIds = evalDao.getViewableEvalGroupIds(eval.getId(), EvalConstants.PERM_BE_EVALUATED, groupIds);
         if (viewableIds.size() == groupIds.length) {
            allowedToView = true;
         }
         for (String groupId: viewableIds) {
            if (!externalLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_BE_EVALUATED, groupId)) {
               allowedToView = false;
            }
         }
      }
      else {
         allowedToView = false;
      }

      return allowedToView;
   }
   
   /*
    * In progress. sgithens
   private boolean canViewEvaluationResponsesByRole(EvalEvaluation eval, String currentUserId, String[] groupIds) {
      Boolean instructorAllowedViewResults = 
         (Boolean) evalSettings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
      Boolean studentAllowedViewResults = 
         (Boolean) evalSettings.get(EvalSettings.STUDENT_VIEW_RESULTS);
      
      boolean allowedToView = false;
      
      Set<String> viewableAsInstructorGroupIds = 
         evalDao.getViewableEvalGroupIds(eval.getId(), EvalConstants.PERM_BE_EVALUATED, groupIds);
      
      Set<String> viewableAsStudentGroupIds = 
         evalDao.getViewableEvalGroupIds(eval.getId(), EvalConstants.PERM_TAKE_EVALUATION, groupIds);

      //
      // If both of the above are false, we don't need to do expensive permission
      // checks.
      //
      if (instructorAllowedViewResults || studentAllowedViewResults) {
         allowedToView = true;
         for (String groupId: groupIds) {
            boolean tempcheck = false;
            if (instructorAllowedViewResults) {
               if (externalLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_BE_EVALUATED, groupId)) {
                  tempcheck = true;
               }
            }
            if (studentAllowedViewResults) {
               if (externalLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_TAKE_EVALUATION, groupId)) {
                  tempcheck = true;
               }
            }
            if (!tempcheck) {
               allowedToView = false;
               break;
            }
         }
      }
      
      return allowedToView;
   }
   */
}
