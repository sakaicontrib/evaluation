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
package org.sakaiproject.evaluation.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.query.Query;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.EvalUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate-backed implementation methods for the matching evaluation DAO port.
 */
@Slf4j
public class EvaluationAssignmentDaoImpl extends EvaluationDaoHibernateSupport implements EvaluationAssignmentDao {

    private EvaluationResponseDao responseDao;
    public void setResponseDao(EvaluationResponseDao responseDao) {
        this.responseDao = responseDao;
    }

    public EvalAssignUser getAssignUserByEid(String eid) {
        return findOneByEid(EvalAssignUser.class, eid, "assignUser");
    }

    public int countEvaluationGroups(Long evaluationId, boolean includeUnApproved) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }
        StringBuilder hql = new StringBuilder("select count(assignGroup.id) from EvalAssignGroup assignGroup where assignGroup.evaluation.id = :evaluationId");
        if (!includeUnApproved) {
            hql.append(" and assignGroup.instructorApproval = true");
        }
        Long count = currentSession().createQuery(hql.toString(), Long.class)
                .setParameter("evaluationId", evaluationId)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public EvalAssignGroup getAssignGroupByEid(String eid) {
        return findOneByEid(EvalAssignGroup.class, eid, "assignGroup");
    }

    public int countParticipantsForEval(Long evaluationId, String[] evalGroupIds) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }
        StringBuilder hql = new StringBuilder(
                "select count(assignUser.id) from EvalAssignUser assignUser "
                + "where assignUser.evaluation.id = :evaluationId "
                + "and assignUser.type = :assignType "
                + "and assignUser.status != :removedStatus");
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            hql.append(" and assignUser.evalGroupId in (:evalGroupIds)");
        }
        Query<Long> query = currentSession().createQuery(hql.toString(), Long.class)
                .setParameter("evaluationId", evaluationId)
                .setParameter("assignType", EvalAssignUser.TYPE_EVALUATOR)
                .setParameter("removedStatus", EvalAssignUser.STATUS_REMOVED);
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            query.setParameterList("evalGroupIds", evalGroupIds);
        }
        Long count = query.uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public List<EvalAssignGroup> getApprovedAssignGroupsForEvaluation(Long evaluationId, String evalGroupId) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }
        StringBuilder hql = new StringBuilder(
                "select assignGroup from EvalAssignGroup assignGroup "
                + "where assignGroup.evaluation.id = :evaluationId "
                + "and assignGroup.instructorApproval = true");
        if (evalGroupId != null) {
            hql.append(" and assignGroup.evalGroupId = :evalGroupId");
        }
        Query<EvalAssignGroup> query = currentSession().createQuery(hql.toString(), EvalAssignGroup.class)
                .setParameter("evaluationId", evaluationId);
        if (evalGroupId != null) {
            query.setParameter("evalGroupId", evalGroupId);
        }
        return query.list();
    }

    public int countApprovedAssignGroupsForEvaluation(Long evaluationId, String[] evalGroupIds) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }
        if (evalGroupIds == null || evalGroupIds.length == 0) {
            return 0;
        }
        Long count = currentSession().createQuery(
                "select count(assignGroup.id) from EvalAssignGroup assignGroup "
                + "where assignGroup.evaluation.id = :evaluationId "
                + "and assignGroup.instructorApproval = true "
                + "and assignGroup.evalGroupId in (:evalGroupIds)",
                Long.class)
                .setParameter("evaluationId", evaluationId)
                .setParameterList("evalGroupIds", evalGroupIds)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public EvalAssignGroup getAssignGroupByEvalAndGroupId(Long evaluationId, String evalGroupId) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }
        if (evalGroupId == null) {
            throw new IllegalArgumentException("evalGroupId cannot be null");
        }
        List<EvalAssignGroup> assignGroups = currentSession().createQuery(
                "select assignGroup from EvalAssignGroup assignGroup "
                + "where assignGroup.evaluation.id = :evaluationId "
                + "and assignGroup.evalGroupId = :evalGroupId",
                EvalAssignGroup.class)
                .setParameter("evaluationId", evaluationId)
                .setParameter("evalGroupId", evalGroupId)
                .setMaxResults(1)
                .list();
        return assignGroups.isEmpty() ? null : assignGroups.get(0);
    }

    public List<EvalAssignHierarchy> getAssignHierarchyByEval(Long evaluationId) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }
        return currentSession().createQuery(
                "select assignHierarchy from EvalAssignHierarchy assignHierarchy "
                + "where assignHierarchy.evaluation.id = :evaluationId "
                + "and assignHierarchy.nodeId is not null "
                + "order by assignHierarchy.id",
                EvalAssignHierarchy.class)
                .setParameter("evaluationId", evaluationId)
                .list();
    }

    public List<EvalAssignGroup> getAssignGroupsForEvals(Long[] evaluationIds, boolean includeUnApproved, Boolean includeHierarchyGroups) {
        if (evaluationIds == null) {
            throw new IllegalArgumentException("evaluationIds cannot be null");
        }
        if (evaluationIds.length == 0) {
            return new ArrayList<>(0);
        }
        StringBuilder hql = new StringBuilder(
                "select assignGroup from EvalAssignGroup assignGroup "
                + "where assignGroup.evaluation.id in (:evaluationIds)");
        if (!includeUnApproved) {
            hql.append(" and assignGroup.instructorApproval = true");
        }
        if (includeHierarchyGroups != null) {
            hql.append(includeHierarchyGroups ? " and assignGroup.nodeId is not null" : " and assignGroup.nodeId is null");
        }
        hql.append(" order by assignGroup.evalGroupId");
        return currentSession().createQuery(hql.toString(), EvalAssignGroup.class)
                .setParameterList("evaluationIds", evaluationIds)
                .list();
    }

    public int countAssignGroupsByEvalAndGroupId(Long evaluationId, String evalGroupId) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }
        if (evalGroupId == null) {
            throw new IllegalArgumentException("evalGroupId cannot be null");
        }
        Long count = currentSession().createQuery(
                "select count(assignGroup.id) from EvalAssignGroup assignGroup "
                + "where assignGroup.evaluation.id = :evaluationId "
                + "and assignGroup.evalGroupId = :evalGroupId",
                Long.class)
                .setParameter("evaluationId", evaluationId)
                .setParameter("evalGroupId", evalGroupId)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public void deleteAssignmentsForEvaluation(Long evaluationId) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }
        int deletedAssignUsers = deleteAssignUsersForEvaluation(evaluationId);
        int deletedAssignGroups = deleteAssignGroupsForEvaluation(evaluationId);
        int deletedAssignHierarchies = deleteAssignHierarchiesForEvaluation(evaluationId);
        log.info("Deleted assignments for evaluation {}: users={}, groups={}, hierarchies={}",
                evaluationId, deletedAssignUsers, deletedAssignGroups, deletedAssignHierarchies);
    }

    private int deleteAssignUsersForEvaluation(Long evaluationId) {
        return currentSession().createQuery(
                "delete from EvalAssignUser assignUser where assignUser.evaluation.id = :evaluationId")
                .setParameter("evaluationId", evaluationId)
                .executeUpdate();
    }

    private int deleteAssignGroupsForEvaluation(Long evaluationId) {
        return currentSession().createQuery(
                "delete from EvalAssignGroup assignGroup where assignGroup.evaluation.id = :evaluationId")
                .setParameter("evaluationId", evaluationId)
                .executeUpdate();
    }

    private int deleteAssignHierarchiesForEvaluation(Long evaluationId) {
        return currentSession().createQuery(
                "delete from EvalAssignHierarchy assignHierarchy where assignHierarchy.evaluation.id = :evaluationId")
                .setParameter("evaluationId", evaluationId)
                .executeUpdate();
    }

    public void saveAssignHierarchyAndGroups(Set<EvalAssignHierarchy> assignHierarchies, Set<EvalAssignGroup> assignGroups) {
        if (assignHierarchies == null) {
            throw new IllegalArgumentException("assignHierarchies cannot be null");
        }
        if (assignGroups == null) {
            throw new IllegalArgumentException("assignGroups cannot be null");
        }
        saveOrUpdateAll(assignHierarchies);
        saveOrUpdateAll(assignGroups);
    }

    public List<EvalAssignHierarchy> getAssignHierarchiesByIds(Long[] assignHierarchyIds) {
        if (assignHierarchyIds == null) {
            throw new IllegalArgumentException("assignHierarchyIds cannot be null");
        }
        if (assignHierarchyIds.length == 0) {
            return new ArrayList<>(0);
        }
        return currentSession().createQuery(
                "select assignHierarchy from EvalAssignHierarchy assignHierarchy "
                + "where assignHierarchy.id in (:assignHierarchyIds)",
                EvalAssignHierarchy.class)
                .setParameterList("assignHierarchyIds", assignHierarchyIds)
                .list();
    }

    public List<EvalAssignGroup> getAssignGroupsByEvalAndNodeIds(Long evaluationId, Set<String> nodeIds) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }
        if (nodeIds == null) {
            throw new IllegalArgumentException("nodeIds cannot be null");
        }
        if (nodeIds.isEmpty()) {
            return new ArrayList<>(0);
        }
        return currentSession().createQuery(
                "select assignGroup from EvalAssignGroup assignGroup "
                + "where assignGroup.evaluation.id = :evaluationId "
                + "and assignGroup.nodeId in (:nodeIds)",
                EvalAssignGroup.class)
                .setParameter("evaluationId", evaluationId)
                .setParameterList("nodeIds", nodeIds)
                .list();
    }

    public void deleteAssignHierarchyAndGroups(Set<EvalAssignHierarchy> assignHierarchies, Set<EvalAssignGroup> assignGroups) {
        if (assignHierarchies == null) {
            throw new IllegalArgumentException("assignHierarchies cannot be null");
        }
        if (assignGroups == null) {
            throw new IllegalArgumentException("assignGroups cannot be null");
        }
        deleteAll(assignGroups);
        deleteAll(assignHierarchies);
    }

    public void saveAssignUsers(Collection<EvalAssignUser> assignUsers) {
        if (assignUsers == null) {
            throw new IllegalArgumentException("assignUsers cannot be null");
        }
        saveOrUpdateAll(assignUsers);
    }

    public void deleteAssignUsersByIds(Long[] assignUserIds) {
        if (assignUserIds == null) {
            throw new IllegalArgumentException("assignUserIds cannot be null");
        }
        if (assignUserIds.length == 0) {
            return;
        }
        List<EvalAssignUser> assignUsers = currentSession().createQuery(
                "select assignUser from EvalAssignUser assignUser where assignUser.id in (:assignUserIds)",
                EvalAssignUser.class)
                .setParameterList("assignUserIds", assignUserIds)
                .list();
        for (EvalAssignUser assignUser : assignUsers) {
            currentSession().delete(assignUser);
        }
    }

    private int deleteAssignUsersByAssignGroupIdExcludingStatus(Long assignGroupId, String excludedStatus) {
        if (assignGroupId == null) {
            throw new IllegalArgumentException("assignGroupId cannot be null");
        }
        if (excludedStatus == null) {
            throw new IllegalArgumentException("excludedStatus cannot be null");
        }
        List<EvalAssignUser> assignUsers = currentSession().createQuery(
                "select assignUser from EvalAssignUser assignUser "
                + "where assignUser.assignGroupId = :assignGroupId "
                + "and assignUser.status <> :excludedStatus",
                EvalAssignUser.class)
                .setParameter("assignGroupId", assignGroupId)
                .setParameter("excludedStatus", excludedStatus)
                .list();
        for (EvalAssignUser assignUser : assignUsers) {
            currentSession().delete(assignUser);
        }
        return assignUsers.size();
    }


    public List<EvalAssignUser> getParticipantsForEval(Long evaluationId, String userId,
            String[] evalGroupIds, String assignTypeConstant, String assignStatusConstant, 
            String includeConstant, String evalStateConstant) {
        // validate arguments
        if (evaluationId == null && (userId == null || "".equals(userId)) ) {
            throw new IllegalArgumentException("At least one of the following must be set: evaluationId, userId");
        }

        String joinHQL = "";

        String evalHQL = "";
        if (evaluationId != null) {
            evalHQL = " and eau.evaluation.id = :evalId";
        }
        String evalStateHQL = "";
        if (evalStateConstant != null) {
            EvalUtils.validateStateConstant(evalStateConstant);
            evalStateHQL = " and eval.state = :evalStateConstant";
            joinHQL = " join eau.evaluation eval";
        }
        String groupsHQL = "";
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            groupsHQL = " and eau.evalGroupId in (:evalGroupIds)";
        }
        String assignTypeHQL = "";
        if (assignTypeConstant != null 
                && includeConstant == null) {
            // only set this if the includeConstant is not set
            EvalAssignUser.validateType(assignTypeConstant);
            assignTypeHQL = " and eau.type = :assignType";
        }
        String assignStatusHQL = "";
        if (assignStatusConstant == null) {
            assignStatusHQL = " and eau.status <> :assignStatus";
        } else if (EvalEvaluationService.STATUS_ANY.equals(assignStatusConstant)) {
            // no restriction needed in this case
        } else {
            EvalAssignUser.validateStatus(assignStatusConstant);
            assignStatusHQL = " and eau.status = :assignStatus";
        }
        String userHQL = "";
        if (userId != null && ! "".equals(userId)) {
            userHQL = " and eau.userId = :userId";
        }
        boolean includeFilterUsers = false;
        Set<String> userFilter = null;
        if (includeConstant != null) {
            EvalUtils.validateEmailIncludeConstant(includeConstant);
            String[] groupIds = new String[] {};
            if (evalGroupIds != null && evalGroupIds.length > 0) {
                groupIds = evalGroupIds;
            }
            // force the results to only include eval takers
            assignTypeHQL = " and eau.type = :assignType";
            // now set up the filter
            if (EvalConstants.EVAL_INCLUDE_NONTAKERS.equals(includeConstant)) {
                // get all users who have responded either way
                userFilter = responseDao.getResponseUserIds(evaluationId, groupIds, null); // exclude
                includeFilterUsers = false; // INVERT the search
            } else if (EvalConstants.EVAL_INCLUDE_RESPONDENTS.equals(includeConstant)) {
                // get all users who have responded
                userFilter = responseDao.getResponseUserIds(evaluationId, groupIds, true);
                includeFilterUsers = true;
            } else if (EvalConstants.EVAL_INCLUDE_IN_PROGRESS.equals(includeConstant)) {
                // get all users who have saved
                userFilter = responseDao.getResponseUserIds(evaluationId, groupIds, false);
                includeFilterUsers = true;
            } else if (EvalConstants.EVAL_INCLUDE_ALL.equals(includeConstant)) {
                // do nothing
            } else {
                throw new IllegalArgumentException("Unknown includeConstant: " + includeConstant);
            }
        }

        // get the assignments based on the search/HQL
        String hql = "select eau from EvalAssignUser eau "+joinHQL+" where 1=1 "+evalHQL+userHQL+evalStateHQL+assignStatusHQL+assignTypeHQL+groupsHQL
        +" order by eau.id";
        Query<EvalAssignUser> query = currentSession().createQuery(hql, EvalAssignUser.class);
        if (evaluationId != null) {
            query.setParameter("evalId", evaluationId);
        }
        if (evalStateConstant != null) {
            query.setParameter("evalStateConstant", evalStateConstant);
        }
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            query.setParameterList("evalGroupIds", evalGroupIds);
        }
        if (assignTypeConstant != null && includeConstant == null) {
            query.setParameter("assignType", assignTypeConstant);
        }
        if (includeConstant != null) {
            query.setParameter("assignType", EvalAssignUser.TYPE_EVALUATOR);
        }
        if (assignStatusConstant == null) {
            query.setParameter("assignStatus", EvalAssignUser.STATUS_REMOVED);
        } else if (!EvalEvaluationService.STATUS_ANY.equals(assignStatusConstant)) {
            query.setParameter("assignStatus", assignStatusConstant);
        }
        if (userId != null && ! "".equals(userId)) {
            query.setParameter("userId", userId);
        }
        List<EvalAssignUser> results = query.list();
        List<EvalAssignUser> assignments = new ArrayList<>( results );

        // This code is potentially expensive but there is not really a better way to handle it -AZ
        if (userFilter != null) {
            if (userFilter.isEmpty()) {
                // employ shortcuts when the filter set is empty
                if (includeFilterUsers) {
                    // no one to include so just wipe the set
                    assignments.clear();
                } else {
                    // no one to exclude to just return the complete set
                }
            } else {
                // filter the results based on the userFilter
                for (Iterator<EvalAssignUser> iterator = assignments.iterator(); iterator.hasNext();) {
                    EvalAssignUser evalAssignUser = iterator.next();
                    String uid = evalAssignUser.getUserId();
                    if (includeFilterUsers) {
                        // only include users in the filter
                        if (! userFilter.contains(uid)) {
                            iterator.remove();
                        }
                    } else {
                        // exclude all users in the filter
                        if (userFilter.contains(uid)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }

        return assignments;
    }

    /*  SELECT * FROM eval_evaluation as EVAL
        LEFT join eval_assign_user as AU on EVAL.ID = AU.EVALUATION_FK 
        WHERE AU.ID IS NULL
     */
    public List<EvalEvaluation> getEvalsWithoutUserAssignments() {
        String hql = "select eval from EvalAssignUser eau right join eau.evaluation eval where eau.id is null";
        return currentSession().createQuery(hql, EvalEvaluation.class).list();
    }

    public Set<String> getViewableEvalGroupIds(Long evaluationId, String assignTypeConstant, String[] evalGroupIds) {
        if (evaluationId == null || assignTypeConstant == null) {
            throw new IllegalArgumentException("evaluationId and assignTypeConstant both must not be null");
        }
        EvalAssignUser.validateType(assignTypeConstant);
        String permCheck = null;
        if (EvalAssignUser.TYPE_EVALUATEE.equals(assignTypeConstant)) {
            permCheck = "instructorsViewResults";
        } else if (EvalAssignUser.TYPE_EVALUATOR.equals(assignTypeConstant)) {
            permCheck = "studentsViewResults";
        }

        Set<String> viewableEvalGroupIds = new HashSet<>();
        if (permCheck != null) {
            String groupsHQL = "";
            if (evalGroupIds != null && evalGroupIds.length > 0) {
                groupsHQL = " and eag.evalGroupId in (:evalGroupIds) ";
            }
            String hql = "SELECT eag.evalGroupId from EvalAssignGroup eag where eag.evaluation.id = :evaluationId "
                + " and eag.evalGroupId in (select distinct eau.evalGroupId from EvalAssignUser eau " +
                		"where eau.evaluation.id = :evaluationId and eau.type = :assignTypeConstant)"
                + " and eag."+permCheck+" = true " + groupsHQL;
            Query<String> query = currentSession().createQuery(hql, String.class);
            query.setParameter("evaluationId", evaluationId);
            query.setParameter("assignTypeConstant", assignTypeConstant);
            if (evalGroupIds != null && evalGroupIds.length > 0) {
                query.setParameterList("evalGroupIds", evalGroupIds);
            }
            List<String> results = query.list();
            // put the results into a set and convert them to strings
            viewableEvalGroupIds.addAll(results);
        }
        return viewableEvalGroupIds;
    }

    public EvalAssignGroup getAssignGroupById(Long assignGroupId) {
        return findById(EvalAssignGroup.class, assignGroupId);
    }

    public EvalAssignUser getAssignUserById(Long assignUserId) {
        return findById(EvalAssignUser.class, assignUserId);
    }

    public void saveAssignGroup(EvalAssignGroup assignGroup) {
        save(assignGroup);
    }

    public int deleteAssignGroupAndLinkedUsers(EvalAssignGroup assignGroup, String excludedUserStatus) {
        if (assignGroup == null) {
            throw new IllegalArgumentException("assignGroup cannot be null");
        }
        Long assignGroupId = assignGroup.getId();
        delete(assignGroup);
        return deleteAssignUsersByAssignGroupIdExcludingStatus(assignGroupId, excludedUserStatus);
    }

}
