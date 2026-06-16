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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalGroupNodes;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Hibernate-backed implementation methods for the matching evaluation DAO port.
 */
@Slf4j
abstract class EvaluationDaoQueryMethods extends EvaluationDaoAssignmentMethods {

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

    public int countTemplateById(Long templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("templateId cannot be null");
        }
        Long count = currentSession().createQuery(
                "select count(template.id) from EvalTemplate template where template.id = :templateId",
                Long.class)
                .setParameter("templateId", templateId)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public int countEvaluationsByTemplateId(Long templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("templateId cannot be null");
        }
        Long count = currentSession().createQuery(
                "select count(evaluation.id) from EvalEvaluation evaluation "
                + "where evaluation.template.id = :templateId "
                + "and evaluation.state != :partialState "
                + "and evaluation.state != :deletedState",
                Long.class)
                .setParameter("templateId", templateId)
                .setParameter("partialState", EvalConstants.EVALUATION_STATE_PARTIAL)
                .setParameter("deletedState", EvalConstants.EVALUATION_STATE_DELETED)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public List<EvalEvaluation> getEvaluationsByTemplateId(Long templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("templateId cannot be null");
        }
        return currentSession().createQuery(
                "select evaluation from EvalEvaluation evaluation "
                + "where evaluation.template.id = :templateId "
                + "and evaluation.state != :partialState "
                + "and evaluation.state != :deletedState",
                EvalEvaluation.class)
                .setParameter("templateId", templateId)
                .setParameter("partialState", EvalConstants.EVALUATION_STATE_PARTIAL)
                .setParameter("deletedState", EvalConstants.EVALUATION_STATE_DELETED)
                .list();
    }

    public List<EvalEvaluation> getEvaluationsByTermId(String termId) {
        if (termId == null) {
            throw new IllegalArgumentException("termId cannot be null");
        }
        return currentSession().createQuery(
                "select evaluation from EvalEvaluation evaluation "
                + "where evaluation.termId = :termId "
                + "and evaluation.state != :partialState "
                + "and evaluation.state != :deletedState",
                EvalEvaluation.class)
                .setParameter("termId", termId)
                .setParameter("partialState", EvalConstants.EVALUATION_STATE_PARTIAL)
                .setParameter("deletedState", EvalConstants.EVALUATION_STATE_DELETED)
                .list();
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
        return currentSession().createQuery(
                "select evaluation from EvalEvaluation evaluation "
                + "where evaluation.state != :viewableState "
                + "and evaluation.state != :deletedState",
                EvalEvaluation.class)
                .setParameter("viewableState", EvalConstants.EVALUATION_STATE_VIEWABLE)
                .setParameter("deletedState", EvalConstants.EVALUATION_STATE_DELETED)
                .list();
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


    public List<EvalEvaluation> getEvaluationsByEvalGroups(String[] evalGroupIds, Boolean activeOnly,
            Boolean approvedOnly, Boolean includeAnonymous, int startResult, int maxResults) {

        HashMap<Long, List<EvalAssignGroup>> evalToAGList = new HashMap<>();

        boolean emptyReturn = false;

        String groupsHQL = "";
        if (evalGroupIds != null && evalGroupIds.length > 0) {

            StringBuilder assignGroupHql = new StringBuilder(
                    "select assignGroup from EvalAssignGroup assignGroup where assignGroup.evalGroupId in (:evalGroupIds)");
            if (approvedOnly != null) {
                assignGroupHql.append(" and assignGroup.instructorApproval = :instructorApproval");
            }
            Query<EvalAssignGroup> assignGroupQuery = currentSession().createQuery(assignGroupHql.toString(), EvalAssignGroup.class)
                    .setParameterList("evalGroupIds", evalGroupIds);
            if (approvedOnly != null) {
                assignGroupQuery.setParameter("instructorApproval", approvedOnly);
            }
            List<EvalAssignGroup> eags = assignGroupQuery.list();
            for (EvalAssignGroup evalAssignGroup : eags) {
                Long evalId = evalAssignGroup.getEvaluation().getId();
                if (! evalToAGList.containsKey(evalId)) {
                    List<EvalAssignGroup> l = new ArrayList<>();
                    evalToAGList.put(evalId, l);
                }
                evalToAGList.get(evalId).add(evalAssignGroup);
            }

            if (eags.isEmpty()) {
                groupsHQL = " and (eval.id = -1) "; // this will never match, that's the point
            } else {
                String anonymousHQL = "";
                if (includeAnonymous != null) {
                    if (includeAnonymous) {
                        anonymousHQL += "or eval.authControl = :authControl";
                    } else {
                        anonymousHQL += "and eval.authControl <> :authControl";            
                    }
                }
    
                groupsHQL = " and (eval.id in (:evalIds)" + anonymousHQL + ")";
                Set<Long> s = evalToAGList.keySet();
            }
        } else {
            // no groups but we want to get anonymous evals
            if (includeAnonymous != null
                    && includeAnonymous) {
                groupsHQL = " and eval.authControl = :authControl ";
            } else {
                // no groups and no anonymous evals means nothing to return
                emptyReturn = true;
            }
        }

        List<EvalEvaluation> evals;
        if (emptyReturn) {
            evals = new ArrayList<>();
        } else {
            String activeHQL;
            if (activeOnly != null) {
                if (activeOnly) {
                	activeHQL = " and ( eval.state = :activeState or eval.state = :graceState) ";
                } else {
                    activeHQL = " and (eval.state = :queueState or eval.state = :graceState or eval.state = :closedState or eval.state = :viewState) ";
                }
            } else {
                // need to filter out the partial and deleted state evals
                activeHQL = " and eval.state <> :partialState and eval.state <> :deletedState ";
            }

            String hql = "select eval from EvalEvaluation as eval "
                + " where 1=1 " + activeHQL + groupsHQL
                + " order by eval.dueDate, eval.title, eval.id";
            Query<EvalEvaluation> query = currentSession().createQuery(hql, EvalEvaluation.class);
            if (evalGroupIds != null && evalGroupIds.length > 0 && !evalToAGList.isEmpty()) {
                query.setParameterList("evalIds", evalToAGList.keySet());
            }
            if (includeAnonymous != null) {
                query.setParameter("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
            }
            if (activeOnly != null) {
                if (activeOnly) {
                    query.setParameter("activeState", EvalConstants.EVALUATION_STATE_ACTIVE);
                    query.setParameter("graceState", EvalConstants.EVALUATION_STATE_GRACEPERIOD);
                } else {
                    query.setParameter("queueState", EvalConstants.EVALUATION_STATE_INQUEUE);
                    query.setParameter("graceState", EvalConstants.EVALUATION_STATE_GRACEPERIOD);
                    query.setParameter("closedState", EvalConstants.EVALUATION_STATE_CLOSED);
                    query.setParameter("viewState", EvalConstants.EVALUATION_STATE_VIEWABLE);
                }
            } else {
                query.setParameter("partialState", EvalConstants.EVALUATION_STATE_PARTIAL);
                query.setParameter("deletedState", EvalConstants.EVALUATION_STATE_DELETED);
            }
            query.setFirstResult(startResult);
            if (maxResults > 0) {
                query.setMaxResults(maxResults);
            }
            evals = query.list();
            Collections.sort(evals, new ComparatorsUtils.EvaluationDateTitleIdComparator());
        }
        // add in the filtered assign groups which we retrieved earlier
        for (EvalEvaluation eval : evals) {
            Long evalId = eval.getId();
            List<EvalAssignGroup> l = evalToAGList.get(evalId);
            if (l == null) {
                l = new ArrayList<>(0);
            }
            eval.setEvalAssignGroups(l);
        }
        return evals;

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
    @SuppressWarnings("unchecked")
    public List<EvalEvaluation> getEvalsUserCanTake(String userId, Boolean activeOnly,
            Boolean approvedOnly, Boolean includeAnonymous, int startResult, int maxResults) {
        if (userId == null || "".equals(userId)) {
            throw new IllegalArgumentException("userId cannot be null or blank");
        }


        String activeHQL;
        if (activeOnly != null) {
            if (activeOnly) {
                activeHQL = " and eval.state = :activeState";
            } else {
                activeHQL = " and (eval.state = :queueState or eval.state = :graceState or eval.state = :closedState or eval.state = :viewState)";
            }
        } else {
            // need to filter out the partial and deleted state evals
            activeHQL = " and eval.state <> :partialState and eval.state <> :deletedState";
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
            + "where 1=1 "+activeHQL+userAssignAuthHQL
            + " order by eval.dueDate, eval.title, eval.id";

        Query<EvalEvaluation> query = currentSession().createQuery(hql, EvalEvaluation.class);
        if (activeOnly != null) {
            if (activeOnly) {
                query.setParameter("activeState", EvalConstants.EVALUATION_STATE_ACTIVE);
            } else {
                query.setParameter("queueState", EvalConstants.EVALUATION_STATE_INQUEUE);
                query.setParameter("graceState", EvalConstants.EVALUATION_STATE_GRACEPERIOD);
                query.setParameter("closedState", EvalConstants.EVALUATION_STATE_CLOSED);
                query.setParameter("viewState", EvalConstants.EVALUATION_STATE_VIEWABLE);
            }
        } else {
            query.setParameter("partialState", EvalConstants.EVALUATION_STATE_PARTIAL);
            query.setParameter("deletedState", EvalConstants.EVALUATION_STATE_DELETED);
        }
        query.setParameter("authControl", EvalConstants.EVALUATION_AUTHCONTROL_NONE);
        if (includeAnonymous == null || !includeAnonymous) {
            query.setParameter("userId", userId);
            query.setParameter("assignUserType", EvalAssignUser.TYPE_EVALUATOR);
        }
        query.setFirstResult(startResult);
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        List<EvalEvaluation> evals = query.list();

        // TODO populate the assign groups


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
    @SuppressWarnings("unchecked")
    public List<EvalEvaluation> getEvaluationsForOwnerAndGroups(String userId,
            String[] evalGroupIds, Date recentClosedDate, int startResult, int maxResults, boolean includePartial) {

        String recentHQL = "";
        if (recentClosedDate != null) {
            recentHQL = " and ( (eval.viewDate is not null and eval.viewDate >= :recentClosedDate) "
                + "or (eval.stopDate is not null and eval.stopDate >= :recentClosedDate) "
                + "or (eval.dueDate is not null and eval.dueDate >= :recentClosedDate) ) ";
        }

        String ownerHQL = "";
        if (userId != null && userId.length() > 0) {
            ownerHQL = " eval.owner = :ownerId ";
        }

        String groupsHQL = "";
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            groupsHQL = " eval.id in (select distinct assign.evaluation.id "
                + "from EvalAssignGroup as assign where assign.nodeId is null "
                + "and assign.evalGroupId in (:evalGroupIds) ) ";
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

        // need to filter out the partial and deleted state evals
        String stateHQL = " and eval.state <> :deletedState ";
        if (! includePartial) {
            stateHQL = stateHQL + " and eval.state <> :partialState ";
        }

        List<EvalEvaluation> evals;
        String hql = "select eval from EvalEvaluation as eval " 
            + " where 1=1 " + stateHQL + recentHQL + ownerGroupHQL 
            + " order by eval.dueDate, eval.title, eval.id";
        Query<EvalEvaluation> query = currentSession().createQuery(hql, EvalEvaluation.class);
        query.setParameter("deletedState", EvalConstants.EVALUATION_STATE_DELETED);
        if (!includePartial) {
            query.setParameter("partialState", EvalConstants.EVALUATION_STATE_PARTIAL);
        }
        if (recentClosedDate != null) {
            query.setParameter("recentClosedDate", recentClosedDate);
        }
        if (userId != null && userId.length() > 0) {
            query.setParameter("ownerId", userId);
        }
        if (evalGroupIds != null && evalGroupIds.length > 0) {
            query.setParameterList("evalGroupIds", evalGroupIds);
        }
        query.setFirstResult(startResult);
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        evals = query.list();
        Collections.sort(evals, new ComparatorsUtils.EvaluationDateTitleIdComparator());
        return evals;
    }


    /**
     * Returns all answers to the given item associated with 
     * responses which are associated with the given evaluation,
     * only returns the answers for completed responses
     * 
     * @param evalId the id of the evaluation you want answers from
     * @param evalGroupIds an array of eval group IDs to return answers for,
     * if null then just return answers for all groups
     * @param templateItemIds the ids of the template items you want answers for,
     * if null then return answers for all template items
     * @return a list of EvalAnswer objects or empty list if none found
     */
    @SuppressWarnings("unchecked")

    public List<String> getEvalCategories(String userId) {
        StringBuilder hql = new StringBuilder("select distinct eval.evalCategory from EvalEvaluation eval where eval.evalCategory is not null");
        if (StringUtils.isNotBlank(userId)) {
            hql.append(" and eval.owner = :userid");
        }
        hql.append(" order by eval.evalCategory");
        return getHibernateTemplate().execute(session -> {
                Query query = session.createQuery(hql.toString());
                if (StringUtils.contains(hql.toString(), ":userid")) {
                    query.setParameter("userid", userId);
                }
                return query.list();
        });
    }

    /**
     * Get the node which contains this evalgroup,
     * Note: this will always only return a single node so if an evalgroup is assigned to multiple
     * nodes then only the first one will be returned
     * @param evalGroupId a unique id for an eval group
     * @return a unique id for the containing node or null if none found
     */
    @SuppressWarnings("unchecked")
    public String getNodeIdForEvalGroup(String evalGroupId) {
        List<String> nodeIds = (List<String>) getHibernateTemplate().execute(session -> session
                .createQuery("select egn.nodeId from EvalGroupNodes egn join egn.evalGroups egrps where egrps.id = :groupid order by egn.nodeId")
                .setParameter("groupid", evalGroupId)
                .list());

        if (!nodeIds.isEmpty()) {
            return nodeIds.get(0);
        }
        return null;
    }

}
