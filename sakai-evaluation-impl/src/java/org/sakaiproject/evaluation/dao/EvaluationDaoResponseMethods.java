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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.query.Query;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.utils.ArrayUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate-backed implementation methods for the matching evaluation DAO port.
 */
@Slf4j
abstract class EvaluationDaoResponseMethods extends EvaluationDaoAdminSupportMethods {

    public List<EvalResponse> getEvaluationResponsesForUserAndGroup(Long evaluationId, String userId, String evalGroupId) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        if (evalGroupId == null) {
            throw new IllegalArgumentException("evalGroupId cannot be null");
        }
        return currentSession().createQuery(
                "select response from EvalResponse response "
                + "where response.owner = :userId "
                + "and response.evaluation.id = :evaluationId "
                + "and response.evalGroupId = :evalGroupId",
                EvalResponse.class)
                .setParameter("userId", userId)
                .setParameter("evaluationId", evaluationId)
                .setParameter("evalGroupId", evalGroupId)
                .list();
    }

    public List<EvalResponse> getEvaluationResponsesForUser(Long[] evaluationIds, String ownerUserId, Boolean completed) {
        if (evaluationIds == null) {
            throw new IllegalArgumentException("evaluationIds cannot be null");
        }
        if (evaluationIds.length == 0) {
            return new ArrayList<>(0);
        }
        StringBuilder hql = new StringBuilder("select response from EvalResponse response where response.evaluation.id in (:evaluationIds)");
        if (ownerUserId != null) {
            hql.append(" and response.owner = :ownerUserId");
        }
        appendResponseCompletedClause(hql, completed);
        hql.append(" order by response.id");

        Query<EvalResponse> query = currentSession().createQuery(hql.toString(), EvalResponse.class)
                .setParameterList("evaluationIds", evaluationIds);
        if (ownerUserId != null) {
            query.setParameter("ownerUserId", ownerUserId);
        }
        return query.list();
    }

    public int countResponses(Long evaluationId, String evalGroupId, Boolean completed) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }
        StringBuilder hql = new StringBuilder("select count(response.id) from EvalResponse response where response.evaluation.id = :evaluationId");
        if (evalGroupId != null) {
            hql.append(" and response.evalGroupId = :evalGroupId");
        }
        appendResponseCompletedClause(hql, completed);

        Query<Long> query = currentSession().createQuery(hql.toString(), Long.class)
                .setParameter("evaluationId", evaluationId);
        if (evalGroupId != null) {
            query.setParameter("evalGroupId", evalGroupId);
        }
        Long count = query.uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public List<EvalResponse> getEvaluationResponses(Long evaluationId, String[] evalGroupIds, Boolean completed) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId cannot be null");
        }
        StringBuilder hql = new StringBuilder("select response from EvalResponse response where response.evaluation.id = :evaluationId");
        boolean restrictGroups = evalGroupIds != null && evalGroupIds.length > 0;
        if (restrictGroups) {
            hql.append(" and response.evalGroupId in (:evalGroupIds)");
        }
        appendResponseCompletedClause(hql, completed);
        hql.append(" order by response.id");

        Query<EvalResponse> query = currentSession().createQuery(hql.toString(), EvalResponse.class)
                .setParameter("evaluationId", evaluationId);
        if (restrictGroups) {
            query.setParameterList("evalGroupIds", evalGroupIds);
        }
        return query.list();
    }

    public List<EvalResponse> getEvaluationResponses(Long[] evaluationIds, String ownerUserId, String[] evalGroupIds, Boolean completed) {
        StringBuilder hql = new StringBuilder("select response from EvalResponse response");
        Query<EvalResponse> query = buildEvaluationResponsesQuery(hql, evaluationIds, ownerUserId, evalGroupIds, completed, EvalResponse.class);
        return query.list();
    }

    public int countEvaluationResponses(Long[] evaluationIds, String ownerUserId, String[] evalGroupIds, Boolean completed) {
        StringBuilder hql = new StringBuilder("select count(response.id) from EvalResponse response");
        Query<Long> query = buildEvaluationResponsesQuery(hql, evaluationIds, ownerUserId, evalGroupIds, completed, Long.class);
        Long count = query.uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    private <T> Query<T> buildEvaluationResponsesQuery(StringBuilder hql, Long[] evaluationIds, String ownerUserId,
            String[] evalGroupIds, Boolean completed, Class<T> resultClass) {
        if (evaluationIds == null || evaluationIds.length == 0) {
            throw new IllegalArgumentException("evaluationIds cannot be null or empty");
        }

        hql.append(" where response.evaluation.id in (:evaluationIds)");
        if (ownerUserId != null && ownerUserId.length() > 0) {
            hql.append(" and response.owner = :ownerUserId");
        }
        boolean restrictGroups = evalGroupIds != null && evalGroupIds.length > 0;
        if (restrictGroups) {
            hql.append(" and response.evalGroupId in (:evalGroupIds)");
        }
        appendResponseCompletedClause(hql, completed);

        Query<T> query = currentSession().createQuery(hql.toString(), resultClass)
                .setParameterList("evaluationIds", evaluationIds);
        if (ownerUserId != null && ownerUserId.length() > 0) {
            query.setParameter("ownerUserId", ownerUserId);
        }
        if (restrictGroups) {
            query.setParameterList("evalGroupIds", evalGroupIds);
        }
        return query;
    }

    /**
     * Persist a response and its answers in the current Hibernate session.
     * Callers must run this inside a transaction so partial saves roll back together.
     */
    public void saveResponseAndAnswers(EvalResponse response, Set<EvalAnswer> answers) {
        if (response == null) {
            throw new IllegalArgumentException("response cannot be null");
        }
        currentSession().saveOrUpdate(response);
        if (answers != null && !answers.isEmpty()) {
            for (EvalAnswer answer : answers) {
                if (answer != null) {
                    currentSession().saveOrUpdate(answer);
                }
            }
        }
    }

    private void appendResponseCompletedClause(StringBuilder hql, Boolean completed) {
        if (completed != null) {
            hql.append(completed ? " and response.endTime is not null" : " and response.endTime is null");
        }
    }

    public List<EvalAnswer> getAnswers(Long evalId, String[] evalGroupIds, Long[] templateItemIds) {

        String groupsHQL = "";
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            groupsHQL = " and ansswerresp.evalGroupId in (:evalGroupIds) ";
        }

        String itemsHQL = "";
        if (templateItemIds != null && templateItemIds.length > 0) {
            itemsHQL = " and answer.templateItem.id in (:templateItemIds) ";
        }

        String hql = "select answer from EvalAnswer as answer join answer.response as ansswerresp"
            + " where ansswerresp.evaluation.id = :evalId and ansswerresp.endTime is not null " + groupsHQL + itemsHQL
            + " order by ansswerresp.id, answer.id";
        // TODO optimize this once we are using a newer version of hibernate that supports "with"

        Query<EvalAnswer> query = currentSession().createQuery(hql, EvalAnswer.class);
        query.setParameter("evalId", evalId);
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            query.setParameterList("evalGroupIds", evalGroupIds);
        }
        if (templateItemIds != null && templateItemIds.length > 0) {
            query.setParameterList("templateItemIds", templateItemIds);
        }
        return query.list();
    }

    public List<Long> getResponseIds(Long evalId, String[] evalGroupIds, String[] userIds, Boolean completed) {
        String groupsHQL = "";
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            groupsHQL = " and response.evalGroupId in (:evalGroupIds) ";
        }
        String usersHQL = "";
        if (userIds != null && userIds.length > 0) {
            usersHQL = " and response.owner in (:userIds) ";
        }
        String completedHQL = "";
        if (completed != null) {
            // if endTime is null then the response is incomplete, if not null then it is complete
            if (completed) {
                completedHQL = " and response.endTime is not null ";
            } else {
                completedHQL = " and response.endTime is null ";
            }
        }
        String hql = "SELECT response.id from EvalResponse as response where response.evaluation.id = :evalId "
            + groupsHQL + usersHQL + completedHQL + " order by response.id";
        Query<Long> query = currentSession().createQuery(hql, Long.class);
        query.setParameter("evalId", evalId);
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            query.setParameterList("evalGroupIds", evalGroupIds);
        }
        if (userIds != null && userIds.length > 0) {
            query.setParameterList("userIds", userIds);
        }
        return query.list();
    }


    /**
     * Removes an array of responses and all their associated answers at
     * the same time (in a single transaction)<br/>
     * Use {@link #getResponseIds(Long, String[], String[], Boolean)} to get the set of responseIds to remove<br/>
     * <b>WARNING:</b> This does not check permissions for removal of responses so you should
     * be sure to check that responses can be removed (system setting) and that they can be removed for this evaluation and user
     * 
     * @param responseIds the array of ids for {@link EvalResponse} objects to remove
     */
    public void removeResponses(Long[] responseIds) {
        if (responseIds != null && responseIds.length > 0) {
            String rids = "(" + ArrayUtils.arrayToString(responseIds) + ")";
            // purge out the answers first
            String hql = "delete EvalAnswer answer where answer.response.id in " + rids;
            log.debug("delete EvalAnswer HQL:" + hql);
            int results = getHibernateTemplate().bulkUpdate(hql);
            log.info("Remove " + results + " answers that were associated with the following responses: " + rids);

            // purge out the responses
            hql = "delete EvalResponse response where response.id in " + rids;
            log.debug("delete EvalResponse HQL:" + hql);
            results = getHibernateTemplate().bulkUpdate(hql);
            log.info("Remove " + results + " responses with the following ids: " + rids);
        }
    }


    public Set<String> getResponseUserIds(Long evaluationId, String[] evalGroupIds, Boolean completed) {
        String groupsHQL = "";
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            groupsHQL = " and response.evalGroupId in (:evalGroupIds) ";
        }
        String completeHQL = "";
        if (completed != null) {
            completeHQL = " and response.endTime is "+(completed ? "not" : "")+" null ";
        }
        String hql = "SELECT response.owner from EvalResponse as response where response.evaluation.id = :evaluationId "
            + completeHQL + groupsHQL + " order by response.id";
        Query<String> query = currentSession().createQuery(hql, String.class);
        query.setParameter("evaluationId", evaluationId);
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            query.setParameterList("evalGroupIds", evalGroupIds);
        }
        List<String> results = query.list();
        // put the results into a set and convert them to strings
        Set<String> responseUsers = new HashSet<>();
        responseUsers.addAll(results);
        log.debug("ResponseUserIds(eval:{}, groups:{}, completed={}): users={}", evaluationId, ArrayUtils.arrayToString(evalGroupIds), completed, responseUsers);
        return responseUsers;
    }

    /** getResponsesSavedInProgress returns a List of EvalResponses that have been saved
     * but not submitted, meaning that they will not be included in any statistics.
     * @param activeEvaluationsOnly If true, only include responses associated with evaluations
     * that are still open. If false, only include responses associated with evaluations that are closed
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#getResponsesSavedInProgress()
     */
    public List<EvalResponse> getResponsesSavedInProgress(boolean activeEvaluationsOnly) {
        StringBuilder hql = new StringBuilder("SELECT response from EvalResponse as response where response.endTime is null");
        Map<String, Object> params = new HashMap<>();
        if (activeEvaluationsOnly) {
            appendEvaluationStateFilter(hql, "response.evaluation",
                    new String[] {EvalConstants.EVALUATION_STATE_ACTIVE}, null, params);
        } else {
            appendEvaluationStateFilter(hql, "response.evaluation", null,
                    new String[] {EvalConstants.EVALUATION_STATE_ACTIVE}, params);
        }
        Query<EvalResponse> query = currentSession().createQuery(hql.toString(), EvalResponse.class);
        bindQueryParameters(query, params);
        return query.list();
    }
}
