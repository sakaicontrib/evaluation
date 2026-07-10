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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate-backed implementation methods for the matching evaluation DAO port.
 */
@Slf4j
public class EvaluationQueryDaoImpl extends EvaluationDaoHibernateSupport implements EvaluationQueryDao {

    private static final String[] INCLUDED_ACTIVE_STATES = {
            EvalConstants.EVALUATION_STATE_ACTIVE
    };
    private static final String[] INCLUDED_ACTIVE_AND_GRACE_PERIOD_STATES = {
            EvalConstants.EVALUATION_STATE_ACTIVE,
            EvalConstants.EVALUATION_STATE_GRACEPERIOD
    };
    private static final String[] INCLUDED_INQUEUE_GRACE_CLOSED_AND_VIEWABLE_STATES = {
            EvalConstants.EVALUATION_STATE_INQUEUE,
            EvalConstants.EVALUATION_STATE_GRACEPERIOD,
            EvalConstants.EVALUATION_STATE_CLOSED,
            EvalConstants.EVALUATION_STATE_VIEWABLE
    };
    private static final String[] EXCLUDED_DELETED_STATES = {
            EvalConstants.EVALUATION_STATE_DELETED
    };
    private static final String[] EXCLUDED_PARTIAL_AND_DELETED_STATES = {
            EvalConstants.EVALUATION_STATE_PARTIAL,
            EvalConstants.EVALUATION_STATE_DELETED
    };
    private static final String[] EXCLUDED_VIEWABLE_AND_DELETED_STATES = {
            EvalConstants.EVALUATION_STATE_VIEWABLE,
            EvalConstants.EVALUATION_STATE_DELETED
    };

    public int countEvaluationsByIds(Long[] evaluationIds) {
        if (evaluationIds == null) {
            throw new IllegalArgumentException("evaluationIds cannot be null");
        }
        if (evaluationIds.length == 0) {
            return 0;
        }
        Long count = currentSession().createQuery(
                "select count(evaluation.id) from EvalEvaluation evaluation where evaluation.id in (:evaluationIds)",
                Long.class)
                .setParameterList("evaluationIds", evaluationIds)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public int countEvaluationById(Long evaluationId) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }
        Long count = currentSession().createQuery(
                "select count(evaluation.id) from EvalEvaluation evaluation where evaluation.id = :evaluationId",
                Long.class)
                .setParameter("evaluationId", evaluationId)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public EvalEvaluation getEvaluationByEid(String eid) {
        return findOneByEid(EvalEvaluation.class, eid, "evaluation");
    }

    public int countEvaluationsByTemplateId(Long templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("templateId cannot be null");
        }
        StringBuilder hql = new StringBuilder(
                "select count(evaluation.id) from EvalEvaluation evaluation "
                + "where evaluation.template.id = :templateId");
        Map<String, Object> params = new HashMap<>();
        params.put("templateId", templateId);
        appendEvaluationStateFilter(hql, "evaluation", null,
                EXCLUDED_PARTIAL_AND_DELETED_STATES, params);

        Query<Long> query = currentSession().createQuery(hql.toString(), Long.class);
        bindQueryParameters(query, params);
        Long count = query.uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public List<EvalEvaluation> getEvaluationsByTemplateId(Long templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("templateId cannot be null");
        }
        StringBuilder hql = new StringBuilder(
                "select evaluation from EvalEvaluation evaluation "
                + "where evaluation.template.id = :templateId");
        Map<String, Object> params = new HashMap<>();
        params.put("templateId", templateId);
        appendEvaluationStateFilter(hql, "evaluation", null,
                EXCLUDED_PARTIAL_AND_DELETED_STATES, params);

        Query<EvalEvaluation> query = currentSession().createQuery(hql.toString(), EvalEvaluation.class);
        bindQueryParameters(query, params);
        return query.list();
    }

    public List<EvalEvaluation> getEvaluationsByTermId(String termId) {
        if (termId == null) {
            throw new IllegalArgumentException("termId cannot be null");
        }
        StringBuilder hql = new StringBuilder(
                "select evaluation from EvalEvaluation evaluation "
                + "where evaluation.termId = :termId");
        Map<String, Object> params = new HashMap<>();
        params.put("termId", termId);
        appendEvaluationStateFilter(hql, "evaluation", null,
                EXCLUDED_PARTIAL_AND_DELETED_STATES, params);

        Query<EvalEvaluation> query = currentSession().createQuery(hql.toString(), EvalEvaluation.class);
        bindQueryParameters(query, params);
        return query.list();
    }

    public List<EvalEvaluation> getEvaluationsByState(String state) {
        if (state == null) {
            throw new IllegalArgumentException("state cannot be null");
        }
        return currentSession().createQuery(
                "select evaluation from EvalEvaluation evaluation where evaluation.state = :state",
                EvalEvaluation.class)
                .setParameter("state", state)
                .list();
    }

    public List<EvalEvaluation> getEvaluationsNotViewableOrDeleted() {
        StringBuilder hql = new StringBuilder("select evaluation from EvalEvaluation evaluation where 1=1");
        Map<String, Object> params = new HashMap<>();
        appendEvaluationStateFilter(hql, "evaluation", null,
                EXCLUDED_VIEWABLE_AND_DELETED_STATES, params);

        Query<EvalEvaluation> query = currentSession().createQuery(hql.toString(), EvalEvaluation.class);
        bindQueryParameters(query, params);
        return query.list();
    }

    public List<EvalEvaluation> getEvaluationsByCategory(String evalCategory) {
        if (evalCategory == null) {
            throw new IllegalArgumentException("evalCategory cannot be null");
        }
        return currentSession().createQuery(
                "select evaluation from EvalEvaluation evaluation "
                + "where evaluation.evalCategory = :evalCategory "
                + "order by evaluation.startDate",
                EvalEvaluation.class)
                .setParameter("evalCategory", evalCategory)
                .list();
    }


    public List<EvalEvaluation> getEvaluationsByEvalGroups(EvaluationGroupQuery query, int startResult, int maxResults) {
        if (query == null) {
            throw new IllegalArgumentException("query cannot be null");
        }

        String[] evalGroupIds = query.getEvalGroupIds();
        EvaluationGroupQuery.ActiveFilter activeFilter = query.getActiveFilter();
        EvaluationGroupQuery.ApprovalFilter approvalFilter = query.getApprovalFilter();
        EvaluationGroupQuery.AnonymousFilter anonymousFilter = query.getAnonymousFilter();

        if ((evalGroupIds == null || evalGroupIds.length == 0)
                && anonymousFilter != EvaluationGroupQuery.AnonymousFilter.ANONYMOUS_ONLY) {
            return new ArrayList<>();
        }

        Map<Long, List<EvalAssignGroup>> evalToAssignGroups = new HashMap<>();
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            StringBuilder assignGroupHql = new StringBuilder(
                    "select assignGroup from EvalAssignGroup assignGroup where assignGroup.evalGroupId in (:evalGroupIds)");
            if (approvalFilter == EvaluationGroupQuery.ApprovalFilter.APPROVED_ONLY) {
                assignGroupHql.append(" and assignGroup.instructorApproval = true");
            } else if (approvalFilter == EvaluationGroupQuery.ApprovalFilter.UNAPPROVED_ONLY) {
                assignGroupHql.append(" and assignGroup.instructorApproval = false");
            }
            List<EvalAssignGroup> assignGroups = currentSession()
                    .createQuery(assignGroupHql.toString(), EvalAssignGroup.class)
                    .setParameterList("evalGroupIds", evalGroupIds)
                    .list();
            if (assignGroups.isEmpty()
                    && anonymousFilter != EvaluationGroupQuery.AnonymousFilter.ANONYMOUS_ONLY) {
                return new ArrayList<>();
            }
            for (EvalAssignGroup assignGroup : assignGroups) {
                Long evalId = assignGroup.getEvaluation().getId();
                evalToAssignGroups.computeIfAbsent(evalId, ignored -> new ArrayList<>()).add(assignGroup);
            }
        }

        StringBuilder stateHql = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        if (activeFilter == EvaluationGroupQuery.ActiveFilter.ACTIVE_ONLY) {
            appendEvaluationStateFilter(stateHql, "eval",
                    INCLUDED_ACTIVE_AND_GRACE_PERIOD_STATES, null, params);
        } else if (activeFilter == EvaluationGroupQuery.ActiveFilter.INACTIVE_ONLY) {
            appendEvaluationStateFilter(stateHql, "eval",
                    INCLUDED_INQUEUE_GRACE_CLOSED_AND_VIEWABLE_STATES, null, params);
        } else {
            appendEvaluationStateFilter(stateHql, "eval", null,
                    EXCLUDED_PARTIAL_AND_DELETED_STATES, params);
        }

        StringBuilder groupsHql = new StringBuilder();
        if (!evalToAssignGroups.isEmpty()) {
            groupsHql.append(" and (eval.id in (:evalIds)");
            params.put("evalIds", evalToAssignGroups.keySet());
            if (anonymousFilter == EvaluationGroupQuery.AnonymousFilter.ANONYMOUS_ONLY) {
                groupsHql.append(" or eval.authControl = :authControl");
                params.put("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
            } else if (anonymousFilter == EvaluationGroupQuery.AnonymousFilter.NON_ANONYMOUS_ONLY) {
                groupsHql.append(" and eval.authControl <> :authControl");
                params.put("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
            }
            groupsHql.append(")");
        } else if (anonymousFilter == EvaluationGroupQuery.AnonymousFilter.ANONYMOUS_ONLY) {
            groupsHql.append(" and eval.authControl = :authControl");
            params.put("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
        } else if (anonymousFilter == EvaluationGroupQuery.AnonymousFilter.NON_ANONYMOUS_ONLY) {
            groupsHql.append(" and eval.authControl <> :authControl");
            params.put("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
        }

        String hql = "select eval from EvalEvaluation eval where 1=1 "
                + stateHql + groupsHql
                + " order by eval.dueDate, eval.title, eval.id";
        Query<EvalEvaluation> evalQuery = currentSession().createQuery(hql, EvalEvaluation.class);
        bindQueryParameters(evalQuery, params);
        evalQuery.setFirstResult(startResult);
        if (maxResults > 0) {
            evalQuery.setMaxResults(maxResults);
        }
        List<EvalEvaluation> evaluations = evalQuery.list();
        Collections.sort(evaluations, new ComparatorsUtils.EvaluationDateTitleIdComparator());

        for (EvalEvaluation evaluation : evaluations) {
            List<EvalAssignGroup> groups = evalToAssignGroups.get(evaluation.getId());
            evaluation.setEvalAssignGroups(groups == null ? new ArrayList<>(0) : groups);
        }
        return evaluations;
    }


    /**
     * Returns all evaluations which the given user can take,
     * can also include anonymous evaluations and filter on active/approved
     * 
     * @param userId the internal user id for the user who we are checking evals they can take
     * @param activeOnly if true, only include active evaluations, 
     * if false only include inactive (inqueue, graceperiod, closed, viewable), 
     * if null, include all evaluations (except partial and deleted)
     * @param approvedOnly if true, include the evaluations for groups which have been instructor approved only,
     * if false, include evals for groups which have not been approved only,
     * if null, include approved and unapproved,
     * NOTE: you should not include unapproved when displaying evaluations to users to take or sending emails
     * @param includeAnonymous if true, only include evaluations authcontrol = anon, 
     * if false, include any evaluations with authcontrol != anon,
     * if null, include all evaluations regardless of authcontrol
     * @return a List of EvalEvaluation objects sorted by due date, title, and id
     */
    public List<EvalEvaluation> getEvalsUserCanTake(String userId, Boolean activeOnly,
            Boolean approvedOnly, Boolean includeAnonymous, int startResult, int maxResults) {
        if (userId == null || "".equals(userId)) {
            throw new IllegalArgumentException("userId cannot be null or blank");
        }

        StringBuilder stateHQL = new StringBuilder();
        Map<String, Object> params = new HashMap<>();
        if (activeOnly != null) {
            if (activeOnly) {
                appendEvaluationStateFilter(stateHQL, "eval",
                        INCLUDED_ACTIVE_STATES, null, params);
            } else {
                appendEvaluationStateFilter(stateHQL, "eval",
                        INCLUDED_INQUEUE_GRACE_CLOSED_AND_VIEWABLE_STATES, null, params);
            }
        } else {
            // need to filter out the partial and deleted state evals
            appendEvaluationStateFilter(stateHQL, "eval", null,
                    EXCLUDED_PARTIAL_AND_DELETED_STATES, params);
        }

        String userAssignHQL = " eau.type = :assignUserType and eau.userId = :userId";

        String userAssignAuthHQL;
        if (includeAnonymous == null) {
            // include all
            userAssignAuthHQL = " and (("+userAssignHQL+" and eval.authControl <> :authControl) or eval.authControl = :authControl)";
        } else {
            if (includeAnonymous) {
                // only anon
                userAssignAuthHQL = " and eval.authControl = :authControl";
            } else {
                // only not anon
                userAssignAuthHQL = " and " + userAssignHQL + " and eval.authControl <> :authControl";            
            }
        }

        String hql = "select distinct eval from EvalAssignUser eau "
            + "right join eau.evaluation eval "
            + "where 1=1 "+stateHQL+userAssignAuthHQL
            + " order by eval.dueDate, eval.title, eval.id";

        Query<EvalEvaluation> query = currentSession().createQuery(hql, EvalEvaluation.class);
        params.put("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
        if (includeAnonymous == null || !includeAnonymous) {
            params.put("userId", userId);
            params.put("assignUserType", EvalAssignUser.TYPE_EVALUATOR);
        }
        bindQueryParameters(query, params);
        query.setFirstResult(startResult);
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        List<EvalEvaluation> evals = query.list();

        // sort the evals remaining
        Collections.sort(evals, new ComparatorsUtils.EvaluationDateTitleIdComparator());

        return evals;
    }




    /**
     * Get a set of evaluations based on the owner and their groups
     * 
     * @param userId internal user id, owner of the evaluations, if null then do not filter on the owner id
     * @param evalGroupIds an array of eval group IDs to get associated evals for, can be empty or null to get all evals
     * @param recentClosedDate only return evaluations which closed after this date
     * @param startResult 0 to start with the first result, otherwise start with this result number
     * @param maxResults 0 to return all results, otherwise limit the number of evals returned to this
     * @return a List of EvalEvaluation objects sorted by stop date, title, and id
     */
    public List<EvalEvaluation> getEvaluationsForOwnerAndGroups(String userId,
            String[] evalGroupIds, Date recentClosedDate, int startResult, int maxResults, boolean includePartial) {
        Map<String, Object> params = new HashMap<>();

        String recentHQL = "";
        if (recentClosedDate != null) {
            recentHQL = " and ( (eval.viewDate is not null and eval.viewDate >= :recentClosedDate) "
                + "or (eval.stopDate is not null and eval.stopDate >= :recentClosedDate) "
                + "or (eval.dueDate is not null and eval.dueDate >= :recentClosedDate) ) ";
            params.put("recentClosedDate", recentClosedDate);
        }

        String ownerHQL = "";
        if (userId != null && userId.length() > 0) {
            ownerHQL = " eval.owner = :ownerId ";
            params.put("ownerId", userId);
        }

        String groupsHQL = "";
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            groupsHQL = " eval.id in (select distinct assign.evaluation.id "
                + "from EvalAssignGroup as assign where assign.nodeId is null "
                + "and assign.evalGroupId in (:evalGroupIds) ) ";
            params.put("evalGroupIds", evalGroupIds);
        }

        // merge the owner and groups HQL if needed
        String ownerGroupHQL = "";
        if (ownerHQL.length() > 0 && groupsHQL.length() > 0) {
            ownerGroupHQL = " and (" + ownerHQL + " or " + groupsHQL + ") ";
        } else if (ownerHQL.length() > 0) {
            ownerGroupHQL = " and " + ownerHQL;
        } else if (groupsHQL.length() > 0) {
            ownerGroupHQL = " and " + groupsHQL;
        }

        String[] excludedStates = includePartial
                ? EXCLUDED_DELETED_STATES
                : EXCLUDED_PARTIAL_AND_DELETED_STATES;
        StringBuilder stateHQL = new StringBuilder();
        appendEvaluationStateFilter(stateHQL, "eval", null, excludedStates, params);

        List<EvalEvaluation> evals;
        String hql = "select eval from EvalEvaluation as eval " 
            + " where 1=1 " + stateHQL + recentHQL + ownerGroupHQL 
            + " order by eval.dueDate, eval.title, eval.id";
        Query<EvalEvaluation> query = currentSession().createQuery(hql, EvalEvaluation.class);
        bindQueryParameters(query, params);
        query.setFirstResult(startResult);
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        evals = query.list();
        Collections.sort(evals, new ComparatorsUtils.EvaluationDateTitleIdComparator());
        return evals;
    }


    public List<String> getEvalCategories(String userId) {
        StringBuilder hql = new StringBuilder("select distinct eval.evalCategory from EvalEvaluation eval where eval.evalCategory is not null");
        if (StringUtils.isNotBlank(userId)) {
            hql.append(" and eval.owner = :userid");
        }
        hql.append(" order by eval.evalCategory");
        Query<String> query = currentSession().createQuery(hql.toString(), String.class);
        if (StringUtils.isNotBlank(userId)) {
            query.setParameter("userid", userId);
        }
        return query.list();
    }

    /**
     * Get the node which contains this evalgroup,
     * Note: this will always only return a single node so if an evalgroup is assigned to multiple
     * nodes then only the first one will be returned
     * @param evalGroupId a unique id for an eval group
     * @return a unique id for the containing node or null if none found
     */
    public String getNodeIdForEvalGroup(String evalGroupId) {
        List<String> nodeIds = currentSession().createQuery(
                "select egn.nodeId from EvalGroupNodes egn join egn.evalGroups egrps where egrps.id = :groupid order by egn.nodeId",
                String.class)
                .setParameter("groupid", evalGroupId)
                .list();

        if (!nodeIds.isEmpty()) {
            return nodeIds.get(0);
        }
        return null;
    }

    public EvalEvaluation getEvaluationById(Long evaluationId) {
        return findById(EvalEvaluation.class, evaluationId);
    }

    public void saveEvaluation(EvalEvaluation evaluation) {
        save(evaluation);
    }

    public void deleteEvaluation(EvalEvaluation evaluation) {
        delete(evaluation);
    }

}
