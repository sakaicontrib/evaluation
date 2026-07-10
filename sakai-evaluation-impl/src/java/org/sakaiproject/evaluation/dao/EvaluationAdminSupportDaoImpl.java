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
import java.util.List;
import java.util.Set;

import org.hibernate.query.Query;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.model.EvalAdmin;
import org.sakaiproject.evaluation.model.EvalHierarchyRule;

/**
 * Hibernate-backed implementation methods for the matching evaluation DAO port.
 */
public class EvaluationAdminSupportDaoImpl extends EvaluationDaoHibernateSupport implements EvaluationAdminSupportDao {

    public List<EvalAdmin> getAllEvalAdmins() {
        return currentSession().createQuery(
                "select ea from EvalAdmin ea",
                EvalAdmin.class)
                .list();
    }

    public EvalAdmin getEvalAdminByUserId(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        List<EvalAdmin> admins = currentSession().createQuery(
                "select ea from EvalAdmin ea where ea.userId = :userId",
                EvalAdmin.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .list();
        return admins.isEmpty() ? null : admins.get(0);
    }

    public List<EvalHierarchyRule> getAllHierarchyRules() {
        return currentSession().createQuery(
                "select rule from EvalHierarchyRule rule",
                EvalHierarchyRule.class)
                .list();
    }

    public EvalHierarchyRule getHierarchyRuleById(Long ruleId) {
        if (ruleId == null) {
            throw new IllegalArgumentException("ruleId cannot be null");
        }
        List<EvalHierarchyRule> rules = currentSession().createQuery(
                "select rule from EvalHierarchyRule rule where rule.id = :ruleId",
                EvalHierarchyRule.class)
                .setParameter("ruleId", ruleId)
                .setMaxResults(1)
                .list();
        return rules.isEmpty() ? null : rules.get(0);
    }

    public List<EvalHierarchyRule> getHierarchyRulesByNodeId(Long nodeId) {
        if (nodeId == null) {
            throw new IllegalArgumentException("nodeId cannot be null");
        }
        return currentSession().createQuery(
                "select rule from EvalHierarchyRule rule where rule.nodeID = :nodeId",
                EvalHierarchyRule.class)
                .setParameter("nodeId", nodeId)
                .list();
    }

    public void deleteHierarchyRules(Set<EvalHierarchyRule> rules) {
        deleteAll(rules);
    }

    public EvalAdhocUser getAdhocUserByUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("username cannot be null");
        }
        List<EvalAdhocUser> users = currentSession().createQuery(
                "select adhocUser from EvalAdhocUser adhocUser where adhocUser.username = :username",
                EvalAdhocUser.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .list();
        return users.isEmpty() ? null : users.get(0);
    }

    public EvalAdhocUser getAdhocUserByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("email cannot be null");
        }
        List<EvalAdhocUser> users = currentSession().createQuery(
                "select adhocUser from EvalAdhocUser adhocUser where adhocUser.email = :email",
                EvalAdhocUser.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .list();
        return users.isEmpty() ? null : users.get(0);
    }

    public List<EvalAdhocUser> getAllAdhocUsers() {
        return currentSession().createQuery(
                "select adhocUser from EvalAdhocUser adhocUser",
                EvalAdhocUser.class)
                .list();
    }

    public List<EvalAdhocUser> getAdhocUsersByIds(Long[] ids) {
        if (ids == null) {
            throw new IllegalArgumentException("ids cannot be null");
        }
        if (ids.length == 0) {
            return new ArrayList<>(0);
        }
        return currentSession().createQuery(
                "select adhocUser from EvalAdhocUser adhocUser where adhocUser.id in (:ids)",
                EvalAdhocUser.class)
                .setParameterList("ids", ids)
                .list();
    }

    public List<EvalAdhocGroup> getAdhocGroupsForOwner(String userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }
        return currentSession().createQuery(
                "select grp from EvalAdhocGroup grp where grp.owner = :userId order by grp.title",
                EvalAdhocGroup.class)
                .setParameter("userId", userId)
                .list();
    }

    public List<EvalAdhocGroup> getEvalAdhocGroupsByUserAndPerm(String userId, String permissionConstant) {

        String permCheck = null;
        if (EvalConstants.PERM_BE_EVALUATED.equals(permissionConstant)) {
            permCheck = "evaluateeIds";
        } else if (EvalConstants.PERM_TAKE_EVALUATION.equals(permissionConstant)) {
            permCheck = "participantIds";
        }

        List<EvalAdhocGroup> results;
        if (permCheck != null) {
            // select b.baz from Foo f join f.bars b"
            // select g.* from EVAL_ADHOC_GROUP g join EVAL_ADHOC_PARTICIPANTS p on p.ID = g.ID and p.USER_ID = 'aaronz' order by g.ID
            String hql = "from EvalAdhocGroup ag join ag." + permCheck + " userIds  where userIds.id = :userId order by ag.id";
            Query<EvalAdhocGroup> query = currentSession().createQuery(hql, EvalAdhocGroup.class);
            query.setParameter("userId", userId);
            results = query.list();
        } else {
            results = new ArrayList<>();
        }
        return results;
    }

    /**
     * Check if a user has a specified permission/role within an adhoc group
     * 
     * @param userId the internal user id (not username)
     * @param permissionConstant a permission string PERM constant (from this API),
     * <b>Note</b>: only take evaluation and be evaluated are supported
     * @param evalGroupId the unique id of an eval group
     * @return true if allowed, false otherwise
     */
    public boolean isUserAllowedInAdhocGroup(String userId, String permissionConstant, String evalGroupId) {
        boolean allowed = false;
        if (userId == null || evalGroupId == null) {
            throw new IllegalArgumentException("userId and evalGroupId must not be null");
        }

        String permCheck = null;
        if (EvalConstants.PERM_BE_EVALUATED.equals(permissionConstant)) {
            permCheck = "evaluateeIds";
        } else if (EvalConstants.PERM_TAKE_EVALUATION.equals(permissionConstant)) {
            permCheck = "participantIds";
        }

        if (permCheck != null) {
            Long id = EvalAdhocGroup.getIdFromAdhocEvalGroupId(evalGroupId);
            if (id != null) {
                // from EvalAdhocGroup ag join ag." + permCheck + " userIds where userIds.id = :userId
                String hql = "select count(ag) from EvalAdhocGroup ag join ag." + permCheck + " userIds "
                + " where ag.id = :groupId and userIds.id = :userId";
                Long count = currentSession().createQuery(hql, Long.class)
                        .setParameter("groupId", id)
                        .setParameter("userId", userId)
                        .uniqueResult();
                allowed = count != null && count >= 1;
            }
        }

        return allowed;
    }
}
