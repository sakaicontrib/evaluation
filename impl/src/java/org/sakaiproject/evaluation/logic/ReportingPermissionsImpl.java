package org.sakaiproject.evaluation.logic;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.EvalUtils;

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
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ReportingPermissionsImpl implements ReportingPermissions {

    private EvaluationDao dao;
    public void setDao(EvaluationDao dao) {
        this.dao = dao;
    }

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
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
    public Set<String> getResultsViewableEvalGroupIdsForCurrentUser(Long evalId) {
        return getResultsViewableEvalGroupIdsForCurrentUser(evaluationService.getEvaluationById(evalId));
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.ReportingPermissions#chooseGroupsPartialCheck(org.sakaiproject.evaluation.model.EvalEvaluation)
     */
    public Set<String> getResultsViewableEvalGroupIdsForCurrentUser(EvalEvaluation evaluation) {
        String currentUserId = commonLogic.getCurrentUserId();
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
        else if ( evaluation.getViewDate() != null 
                && evaluation.getViewDate().after( new Date() ) ) {
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
                groupIdsTogo = getViewableGroupsForEvalAndUserByRole(evaluation, currentUserId, null);
            } else {
                // user can view all groups
                // Should the includeUnapproved be true or false for this use case?? (swg) - false -AZ
                Map<Long, List<EvalAssignGroup>> evalAssignGroupMap = 
                    evaluationService.getAssignGroupsForEvals(new Long[] {evaluation.getId()}, false, null);
                for (EvalAssignGroup evalAssignGroup: evalAssignGroupMap.get(evaluation.getId())) {
                    groupIdsTogo.add(evalAssignGroup.getEvalGroupId());
                }
            }
        }

        return new HashSet<String>(groupIdsTogo);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.logic.ReportingPermissions#canViewEvaluationResponses(org.sakaiproject.evaluation.model.EvalEvaluation, java.lang.String[])
     */
    public boolean canViewEvaluationResponses(EvalEvaluation evaluation, String[] groupIds) {
        String currentUserId = commonLogic.getCurrentUserId();

        boolean canViewResponses = false;

        // 0 groups to view is a special case false (no groups to check)
        if (groupIds != null && groupIds.length == 0) {
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
        // 4/5 (combined check)
        else if ( checkGroupsForEvalUserGroups(evaluation, groupIds, currentUserId) ) {
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
     * Convenience method to make writing the canViewEvaluationResponses method easier,
     * allows us make this a single line check
     */
    private boolean checkGroupsForEvalUserGroups(EvalEvaluation evaluation, String[] groupIds, String userId) {
        boolean allowed = false;
        FlagHashSet<String> fhs = getViewableGroupsForEvalAndUserByRole(evaluation, userId, groupIds);
        if ( groupIds == null) {
            // checked all groups so compare to the totalCount
            if (fhs.size() == fhs.totalCount) {
                allowed = true;
            }
        } else {
            // only checked some groups so see if the size matches the groups sent in
            if (fhs.size() == groupIds.length) {
                allowed = true;
            }
        }
        return allowed;
    }

    /**
     * Get the list of viewable evalGroupIds for a specific user,
     * does complete settings and security check by roles
     * <br/>
     * Does the following checks:<br/>
     * 4/5) Do the system settings allow students to view the evaluation?<br/>
     * 4/5b) DateRule<br/>
     * 4/5c) Is this user a student who is allowed to view results<br/>
     * <br/>
     * DateRule) check the setting and if true then check if the date is set,
     * if null then the eval just has to be closed,
     * if not null then it has to be after now
     * 
     * @param eval the evaluation
     * @param userId the user of the user to get the viewable groups
     * @param groupIds (OPTIONAL) evalGroupIds of the groups to check,
     * if null then check all groups for this evaluation
     * @param isUserInstructor if true this check as if this user were an instructor,
     * if false check as if they were a student, if null then check for both cases
     * @return the set of evalGroupIds that can be viewed by this user
     */
    protected FlagHashSet<String> getViewableGroupsForEvalAndUserByRole(EvalEvaluation eval, String userId, String[] groupIds) {
        if (eval == null || userId == null || "".equals(userId)) {
            throw new IllegalArgumentException("eval and userId must be set");
        }
        Long evaluationId = eval.getId();
        // use one central method which returns the groups accessible by the user, then compare the size to the
        // total size of all groups for this case (if it is smaller then return false)

        FlagHashSet<String> viewableGroupIds = new FlagHashSet<String>();

        // generate a hashmap of the assign types for this user to the groups for those types
        HashMap<String, Set<String>> typeToEvalGroupId = new HashMap<String, Set<String>>();
        List<EvalAssignUser> userAssignments = evaluationService.getParticipantsForEval(evaluationId, userId, 
                groupIds, null, null, null, null);
        for (EvalAssignUser eau : userAssignments) {
            String type = eau.getType();
            if (! typeToEvalGroupId.containsKey(type)) {
                typeToEvalGroupId.put(type, new HashSet<String>());
            }
            typeToEvalGroupId.get(type).add(eau.getEvalGroupId());
        }

        boolean allowedInstructor = false;
        if ( typeToEvalGroupId.containsKey(EvalAssignUser.TYPE_EVALUATEE) ) {
            Boolean instructorAllowedViewResults = (Boolean) evalSettings.get(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS);
            if (instructorAllowedViewResults == null) {
                if (eval.getInstructorViewResults()) {
                    Date checkDate = eval.getInstructorsDate();
                    if ( (checkDate == null && EvalUtils.checkStateAfter(eval.getState(), EvalConstants.EVALUATION_STATE_VIEWABLE, true))
                            || (checkDate != null && checkDate.after( new Date() )) ) {
                        // user is allowed to view based on state and settings so check the groups below
                        allowedInstructor = true;
                    }
                }
            } else {
                allowedInstructor = instructorAllowedViewResults;
            }

            if (allowedInstructor) {
                // only do this part if the user is allowed to view some groups at all
                Set<String> gidSet = typeToEvalGroupId.get(EvalAssignUser.TYPE_EVALUATEE);
                String[] gids = gidSet.toArray(new String[gidSet.size()]);
                viewableGroupIds.addAll( getEvalGroupIdsForUserRole(evaluationId, userId, gids, true) );
            }
        }

        boolean allowedStudent = false;
        if ( typeToEvalGroupId.containsKey(EvalAssignUser.TYPE_EVALUATOR) ) {
            Boolean studentAllowedViewResults = (Boolean) evalSettings.get(EvalSettings.STUDENT_ALLOWED_VIEW_RESULTS);
            if (studentAllowedViewResults == null) {
                if (eval.getStudentViewResults()) {
                    Date checkDate = eval.getStudentsDate();
                    if ( (checkDate == null && EvalUtils.checkStateAfter(eval.getState(), EvalConstants.EVALUATION_STATE_VIEWABLE, true))
                            || (checkDate != null && checkDate.after( new Date() )) ) {
                        // user is allowed to view based on state and settings so check the groups below
                        allowedStudent = true;
                    }
                }
            } else {
                allowedStudent = studentAllowedViewResults;
            }

            if (allowedStudent) {
                // only do this part if the user is allowed to view some groups at all
                Set<String> gidSet = typeToEvalGroupId.get(EvalAssignUser.TYPE_EVALUATOR);
                String[] gids = gidSet.toArray(new String[gidSet.size()]);
                viewableGroupIds.addAll( getEvalGroupIdsForUserRole(evaluationId, userId, gids, false) );
            }
        }

        if (groupIds == null 
                && ! viewableGroupIds.isEmpty()) {
            viewableGroupIds.totalCount = evaluationService.countEvaluationGroups(eval.getId(), false);
        }

        return viewableGroupIds;
    }


    /**
     * Gets the set of all evalGroupIds that are viewable by this user in this subset of groupIds for this evaluation
     * given the role setting (boolean), DOES NOT do settings checks
     * 
     * @param evaluationId the id of the eval
     * @param userId the internal user id
     * @param groupIds (OPTIONAL) evalGroupIds of the groups to check,
     * if null then check all groups for this evaluationId,
     * if empty then check none
     * @param isUserInstructor if true then this role is treated and the instructor/leader/evaluatee,
     * otherwise the role is treated as the student/learner/evaluator
     * @return the set of viewable evalGroupIds
     */
    protected FlagHashSet<String> getEvalGroupIdsForUserRole(Long evaluationId, String userId, String[] groupIds, boolean isUserInstructor) {
        FlagHashSet<String> viewableGroupIds = new FlagHashSet<String>();
        String type = EvalAssignUser.TYPE_EVALUATOR;
        if (isUserInstructor) {
            type = EvalAssignUser.TYPE_EVALUATEE;
        }
        if (groupIds == null || groupIds.length > 0) {
            Set<String> gids = dao.getViewableEvalGroupIds(evaluationId, type, groupIds);
            viewableGroupIds.addAll( gids );
        }
        return viewableGroupIds;
    }

    protected class FlagHashSet<E> extends HashSet<E> {
        /**
         * Used to track a counter value 
         */
        public int totalCount = 0;
    }

}
