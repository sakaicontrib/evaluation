package org.sakaiproject.evaluation.tool.reporting;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
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
 * DateRule-A) We have to be past the eval.viewDate if it is set, if null then skip forward
 * DateRule-B) If the eval.studentsDate is null students cannot view, otherwise it
 *             must be past the current date.
 * DateRule-C) If the eval.instructorsDate is null instructors cannot view, 
 *             otherwise it must be past the current date
 * @author Steven Githens
 */
public class ReportingPermissions {
   private static Log log = LogFactory.getLog(ReportingPermissions.class);

   private EvaluationDao dao;
   public void setEvaluationDao(EvaluationDao dao) {
      this.dao = dao;
   }

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic externalLogic) {
      this.externalLogic = externalLogic;
   }

   private EvalSettings evalSettings;
   public void setEvalSettings(EvalSettings evalSettings) {
      this.evalSettings = evalSettings;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalBeanUtils evalBeanUtils;
   public void setEvalBeanUtils(EvalBeanUtils evalBeanUtils) {
      this.evalBeanUtils = evalBeanUtils;
   }


   /**
    * Signature variation for convenience (especially if you only have the 
    * information from a ViewParams). See {@link #chooseGroupsPartialCheck(EvalEvaluation)}
    * for complete details on the return value
    * 
    * @param evalId unique ID of an {@link EvalEvaluation}
    * @return The array of groupIds we can choose from for viewing responses in 
    * this evaluation.  If you cannot view the responses from any groups in this
    * evaluation, this will return an empty list.
    */
   public String[] chooseGroupsPartialCheck(Long evalId) {
      return chooseGroupsPartialCheck(evaluationService.getEvaluationById(evalId));
   }
   
   /**
    * This is a sort of partial security check based off of the full 
    * {@link #canViewEvaluationResponses(EvalEvaluation, String[])} method.
    * 
    * This is primarily needed for the Choose Groups page in reporting. In this
    * case, we want to genuinely check most of the permissions, except we don't
    * actually know what Group ID's we are looking at (because we are about to 
    * choose them).  Instead, we want to check almost all of the items in the 
    * rules, and if they pass successfully, return the Groups that we are able
    * to choose from for report viewing.
    *
    * @param evaluation an {@link EvalEvaluation} (must have been saved)
    * @return The array of groupIds we can choose from for viewing responses in 
    * this evaluation.  If you cannot view the responses from any groups in this
    * evaluation, this will return an empty list.<br/>
    * <b>NOTE:</b> If the survey is anonymous the returned array will be empty.
    * You should not rely on this has the sole permission check, mostly 
    * just for populating the Choose Groups page, and redirecting if the length
    * of the returned groups is 0 or 1.
    */
   public String[] chooseGroupsPartialCheck(EvalEvaluation evaluation) {
      String currentUserId = externalLogic.getCurrentUserId();
      Set<String> groupIdsTogo = new HashSet<String>();

      boolean canViewResponses = false; // is user able to view any results/responses at all
      boolean checkBasedOnRole = false; // get the groups based on the current user role

      // 1) Is this user an admin or evaluation owner
      // 2) Is this user the evaluation owner?
      if (evalBeanUtils.checkUserPermission(currentUserId, evaluation.getOwner())) {
         canViewResponses = true;
         checkBasedOnRole = false;
      }
      // if view date is set and it is in the future then no groups
      else if (evaluation.getViewDate() != null &&
            evaluation.getViewDate().after(new Date())) {
         canViewResponses = false;
      }
      // 3
      else if (EvalConstants.SHARING_PUBLIC.equals(evaluation.getResultsSharing())) {
         canViewResponses = true;
         checkBasedOnRole = false;
      }
      else if (EvalConstants.SHARING_PRIVATE.equals(evaluation.getResultsSharing())) {
         canViewResponses = false;
      }
      else {
         canViewResponses = true;
         checkBasedOnRole = true;
      }
      // 6 TODO Infrastructure isn't available for this yet.

      if (canViewResponses) {
         /*
          * If we can view the responses, based on the preliminary checks above,
          * we will create a Set of the groups we are allowed to view based on the
          * possibility of us having the instructor and student oriented permission
          * locks.
          */
         if (checkBasedOnRole) {
            // FIXME A1 - REDUCE DUPLICATION - This code can fairly easily be combined with the code below at A2 and A3
            Boolean instructorAllowedViewResults = 
               (Boolean) evalSettings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
            if (instructorAllowedViewResults == null) {
               instructorAllowedViewResults = evaluation.getInstructorsDate() != null;
            }
            if (instructorAllowedViewResults 
                  && evaluation.getInstructorsDate() != null 
                  && evaluation.getInstructorsDate().before(new Date())) {
               Set<String> tempGroupIds = 
                     dao.getViewableEvalGroupIds(evaluation.getId(), EvalConstants.PERM_BE_EVALUATED, null);
               for (String tempGroupId: tempGroupIds) {
                  if (externalLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_BE_EVALUATED, tempGroupId)) {
                     groupIdsTogo.add(tempGroupId);
                  }
               }
            }
   
            Boolean studentAllowedViewResults = 
               (Boolean) evalSettings.get(EvalSettings.STUDENT_VIEW_RESULTS);
            if (studentAllowedViewResults == null) {
               studentAllowedViewResults = evaluation.getStudentsDate() != null;
            }
            if (studentAllowedViewResults 
                  && evaluation.getStudentsDate() != null
                  && evaluation.getStudentsDate().before(new Date())) {
               Set<String> tempGroupIds =
                     dao.getViewableEvalGroupIds(evaluation.getId(), EvalConstants.PERM_TAKE_EVALUATION, null);
               for (String tempGroupId: tempGroupIds) {
                  if (externalLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_TAKE_EVALUATION, tempGroupId)) {
                     groupIdsTogo.add(tempGroupId);
                  }
               }
            }
         } else {
            // user can view all groups

            // TODO FIXME Should the includeUnapproved be true or false for this
            // use case??
            Map<Long, List<EvalAssignGroup>> evalAssignGroupMap = 
               evaluationService.getEvaluationAssignGroups(new Long[] {evaluation.getId()}, false);

            for (EvalAssignGroup evalAssignGroup: evalAssignGroupMap.get(evaluation.getId())) {
               groupIdsTogo.add(evalAssignGroup.getEvalGroupId());
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

      // At the moment, YOU MUST specify a set of groupIds to view an evaluation with.
      if (groupIds == null || groupIds.length == 0) {
         canViewResponses = false;
      }
      // 1) Is this user an admin or evaluation owner
      // 2) Is this user the evaluation owner?
      else if (evalBeanUtils.checkUserPermission(currentUserId, evaluation.getOwner())) {
         canViewResponses = true;
      }
      // if view date is set and it is in the future then no groups
      else if (evaluation.getViewDate() != null &&
            evaluation.getViewDate().after(new Date())) {
         canViewResponses = false;
      }
      // 3
      else if (EvalConstants.SHARING_PUBLIC.equals(evaluation.getResultsSharing())) {
         canViewResponses = true;
      }
      else if (EvalConstants.SHARING_PRIVATE.equals(evaluation.getResultsSharing())) {
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
      // FIXME A2 - REDUCE DUPLICATION - This code can fairly easily be combined with the code above at A1,
      // TIP: use one central method which returns the groups accessible by the user, then compare the size to the
      // total size of all groups for this case (if it is smaller then return false)
      Boolean studentAllowedViewResults = 
         (Boolean) evalSettings.get(EvalSettings.STUDENT_VIEW_RESULTS); // FIXME - this can be null, the if below will throw NPE
      boolean allowedToView = false;
      if (eval.getStudentsDate() == null || eval.getStudentsDate().after(new Date())) {
         allowedToView = false;
      }
      else if (studentAllowedViewResults) {
         Set<String> viewableIds = dao.getViewableEvalGroupIds(eval.getId(), EvalConstants.PERM_TAKE_EVALUATION, groupIds);
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
      // FIXME A3 - REDUCE DUPLICATION - This code can fairly easily be combined with the code above at A1
      Boolean instructorAllowedViewResults = 
         (Boolean) evalSettings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
      boolean allowedToView = false;
      if (eval.getInstructorsDate() == null || eval.getInstructorsDate().after(new Date())) {
         allowedToView = false;
      }
      else if (instructorAllowedViewResults) {
         Set<String> viewableIds = dao.getViewableEvalGroupIds(eval.getId(), EvalConstants.PERM_BE_EVALUATED, groupIds);
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
