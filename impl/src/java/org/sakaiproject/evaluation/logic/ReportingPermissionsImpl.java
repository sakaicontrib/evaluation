package org.sakaiproject.evaluation.logic;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
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
public class ReportingPermissionsImpl implements ReportingPermissions {
//   private static Log log = LogFactory.getLog(ReportingPermissionsImpl.class);

   private EvaluationDao dao;
   public void setDao(EvaluationDao dao) {
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


   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.ReportingPermissions#chooseGroupsPartialCheck(java.lang.Long)
    */
   public String[] chooseGroupsPartialCheck(Long evalId) {
      return chooseGroupsPartialCheck(evaluationService.getEvaluationById(evalId));
   }
   
   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.ReportingPermissions#chooseGroupsPartialCheck(org.sakaiproject.evaluation.model.EvalEvaluation)
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

            //Should the includeUnapproved be true or false for this use case?? - false -AZ
            Map<Long, List<EvalAssignGroup>> evalAssignGroupMap = 
               evaluationService.getAssignGroupsForEvals(new Long[] {evaluation.getId()}, false, null);
            for (EvalAssignGroup evalAssignGroup: evalAssignGroupMap.get(evaluation.getId())) {
               groupIdsTogo.add(evalAssignGroup.getEvalGroupId());
            }
         }
      }
      
      return groupIdsTogo.toArray(new String[] {});
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.ReportingPermissions#canViewEvaluationResponses(org.sakaiproject.evaluation.model.EvalEvaluation, java.lang.String[])
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
