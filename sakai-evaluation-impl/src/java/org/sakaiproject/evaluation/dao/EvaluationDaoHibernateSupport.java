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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.DateType;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.model.EvalAdmin;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalAssignHierarchy;
import org.sakaiproject.evaluation.model.EvalAssignUser;
import org.sakaiproject.evaluation.model.EvalConfig;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalGroupNodes;
import org.sakaiproject.evaluation.model.EvalHierarchyRule;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalItemGroup;
import org.sakaiproject.evaluation.model.EvalLock;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.utils.ArrayUtils;
import org.sakaiproject.evaluation.utils.ComparatorsUtils;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate5.HibernateObjectRetrievalFailureException;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;

/**
 * Focused slice of the Hibernate-backed evaluation DAO implementation.
 */
@Slf4j
abstract class EvaluationDaoHibernateSupport extends HibernateDaoSupport {

    protected static final int MAX_UPDATE_SIZE = 999;

    protected static final String SQL_SELECT_SITE_IDS_MATCHING_SECTION_TITLE = "SELECT DISTINCT realm.realm_id "
            + "FROM SAKAI_REALM realm "
            + "JOIN SAKAI_REALM_PROVIDER provider "
            + "ON realm.realm_key = provider.realm_key "
            + "JOIN CM_MEMBER_CONTAINER_T section "
            + "ON provider.provider_id = section.enterprise_id "
            + "WHERE section.class_discr = 'org.sakaiproject.coursemanagement.impl.SectionCmImpl' "
            + "AND section.title LIKE :title "
            + "AND realm.realm_id NOT LIKE '%/group/%'";
    protected static final String SQL_SELECT_SITE_IDS_MATCHING_SITE_TITLE = "SELECT site_id FROM SAKAI_SITE WHERE title LIKE :title";

    public void init() {
        log.debug("init");
    }

    public <T> T findById(Class<T> type, Serializable id) {
        if (id == null) {
            throw new IllegalArgumentException("id must be set to find persistent object");
        }
        try {
            return getHibernateTemplate().get(type, id);
        } catch (HibernateObjectRetrievalFailureException e) {
            return null;
        }
    }

    public <T> List<T> findAll(Class<T> type) {
        return currentSession().createQuery("from " + type.getName(), type).list();
    }

    public <T> int countAll(Class<T> type) {
        Long total = currentSession().createQuery("select count(entity) from " + type.getName() + " entity", Long.class)
                .uniqueResult();
        return total == null ? 0 : total.intValue();
    }

    protected <T> T findOneByEid(Class<T> type, String eid, String alias) {
        if (eid == null) {
            throw new IllegalArgumentException("eid cannot be null");
        }
        String entityName = type.getSimpleName();
        List<T> results = currentSession().createQuery(
                "select " + alias + " from " + entityName + " " + alias + " where " + alias + ".eid = :eid",
                type)
                .setParameter("eid", eid)
                .setMaxResults(1)
                .list();
        return results.isEmpty() ? null : results.get(0);
    }

    protected <T> void saveOrUpdateAll(Collection<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        for (T entity : entities) {
            if (entity != null) {
                currentSession().saveOrUpdate(entity);
            }
        }
    }

    protected <T> void deleteAll(Set<T> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        for (T entity : entities) {
            if (entity != null) {
                currentSession().delete(entity);
            }
        }
    }

    public void create(Object object) {
        if (getPersistentId(object) != null) {
            throw new IllegalArgumentException("This object is already persistent with id: " + getPersistentId(object)
                    + " - you must use update to save this object and not create");
        }
        getHibernateTemplate().save(object);
    }

    public void save(Object object) {
        if (getPersistentId(object) == null) {
            create(object);
        } else {
            update(object);
        }
    }

    public void update(Object object) {
        if (getPersistentId(object) == null) {
            throw new IllegalArgumentException("Could not get an id value from the supplied object, cannot update without an id: " + object);
        }
        getHibernateTemplate().update(object);
    }

    public void delete(Object object) {
        Serializable id = getPersistentId(object);
        if (id == null) {
            getHibernateTemplate().delete(object);
        } else {
            // Re-fetch by id so deleting a detached instance still works when the
            // current session already contains another instance for the same row.
            delete(Hibernate.getClass(object), id);
        }
    }

    public <T> boolean delete(Class<T> entityClass, Serializable id) {
        Object object = findById(entityClass, id);
        if (object == null) {
            return false;
        }
        getHibernateTemplate().delete(object);
        return true;
    }

    private Serializable getPersistentId(Object object) {
        ClassMetadata metadata = getSessionFactory().getClassMetadata(Hibernate.getClass(object));
        if (metadata == null) {
            throw new IllegalArgumentException("Could not get class metadata for this object, it may not be persistent: " + object);
        }
        return metadata.getIdentifier(object);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#forceCommit()
     */
    public void forceCommit() {
        getHibernateTemplate().flush(); // this should sync the data immediately
        // do a commit using the current transaction or make a new one
        if (currentSession().getTransaction() != null) {
            currentSession().getTransaction().commit();
            currentSession().beginTransaction(); // start a new one
        } else {
            // establish a transaction and then force the commit
            currentSession().beginTransaction().commit();
        }
        // should probably use the org.springframework.transaction.PlatformTransactionManager
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.evaluation.dao.EvaluationDao#forceRollback()
     */
    public void forceRollback() {
        getHibernateTemplate().clear(); // clear pending data
        // do a rollback using the current transaction or make a new one
        if (currentSession().getTransaction() != null) {
            currentSession().getTransaction().rollback();
            currentSession().beginTransaction(); // start a new one
        } else {
            // establish a transaction and then force the rollback
            currentSession().beginTransaction().rollback();
        }
    }

    /**
     * This really does not work for most cases so be very careful with it
     * @param object
     */
    protected void forceEvict(Serializable object) {
        boolean active = false;
        try {
            Session session = currentSession();
            if (session.isOpen() && session.isConnected()) {
                if (session.contains(object)) {
                    active = true;
                    session.evict(object);
                }
            } else {
                log.warn("Session is not open OR not connected, cannot evict objects");
            }
            if (!active) {
                log.info("Unable to evict object ("+object.getClass().getName()+") from session, it is not persistent: "+object);
            }
        } catch (DataAccessResourceFailureException | IllegalStateException | HibernateException e) {
            log.warn("Failure while attempting to evict object ("+object.getClass().getName()+") from session", e);
        }
    }

    public void fixupDatabase() {
        // fix up some of the null fields
        long count;
        count = countEvaluationsWithNullProperty("studentViewResults");
        if (count > 0) {
            int counter = 0;
            counter += getHibernateTemplate().bulkUpdate("update EvalEvaluation eval set eval.studentViewResults = false where eval.studentsDate is null");
            counter += getHibernateTemplate().bulkUpdate("update EvalEvaluation eval set eval.studentViewResults = true where eval.studentsDate is not null");
            log.info("Updated " + counter + " EvalEvaluation.studentViewResults fields from null to boolean values based on studentsDate values");
        }
        count = countEvaluationsWithNullProperty("instructorViewResults");
        if (count > 0) {
            int counter = 0;
            counter += getHibernateTemplate().bulkUpdate("update EvalEvaluation eval set eval.instructorViewResults = false where eval.instructorsDate is null");
            counter += getHibernateTemplate().bulkUpdate("update EvalEvaluation eval set eval.instructorViewResults = true where eval.instructorsDate is not null");
            log.info("Updated " + counter + " EvalEvaluation.instructorViewResults fields from null to boolean values based on instructorsDate values");
        }
        count = countEvaluationsWithNullProperty("modifyResponsesAllowed");
        if (count > 0) {
            int counter = 0;
            counter += getHibernateTemplate().bulkUpdate("update EvalEvaluation eval set eval.modifyResponsesAllowed = false where eval.modifyResponsesAllowed is null");
            log.info("Updated " + counter + " EvalEvaluation.modifyResponsesAllowed fields from null to default");
        }
        count = countEvaluationsWithNullProperty("blankResponsesAllowed");
        if (count > 0) {
            int counter = 0;
            counter += getHibernateTemplate().bulkUpdate("update EvalEvaluation eval set eval.blankResponsesAllowed = false where eval.blankResponsesAllowed is null");
            log.info("Updated " + counter + " EvalEvaluation.blankResponsesAllowed fields from null to default");
        }
    }

    private long countEvaluationsWithNullProperty(String propertyName) {
        Long count = currentSession().createQuery(
                "select count(eval.id) from EvalEvaluation eval where eval." + propertyName + " is null",
                Long.class)
                .uniqueResult();
        return count == null ? 0 : count;
    }

    protected void appendSharingPredicate(StringBuilder hql, String alias, String userId,
            String[] sharingConstants, Map<String, Object> params) {
        if (sharingConstants == null || sharingConstants.length == 0) {
            throw new IllegalArgumentException("No sharing constants specified, you must specify at least one");
        }
        hql.append("and (");
        for (int i = 0; i < sharingConstants.length; i++) {
            String sharingConstant = sharingConstants[i];
            if (i > 0) {
                hql.append(" or ");
            }
            String sharingParam = "sharing" + i;
            if (EvalConstants.SHARING_PRIVATE.equals(sharingConstant)
                    || EvalConstants.SHARING_OWNER.equals(sharingConstant)) {
                params.put(sharingParam, EvalConstants.SHARING_PRIVATE);
                if (userId == null) {
                    hql.append(alias).append(".sharing = :").append(sharingParam);
                } else {
                    hql.append("(").append(alias).append(".sharing = :").append(sharingParam)
                            .append(" and ").append(alias).append(".owner = :sharingOwner)");
                    params.put("sharingOwner", userId);
                }
            } else {
                params.put(sharingParam, sharingConstant);
                hql.append(alias).append(".sharing = :").append(sharingParam);
            }
        }
        hql.append(") ");
    }

    protected void appendEvaluationStateFilter(StringBuilder hql, String alias,
            String[] includedStates, String[] excludedStates, Map<String, Object> params) {
        String paramPrefix = alias.replace('.', '_');
        if (includedStates != null && includedStates.length > 0) {
            String includedParam = paramPrefix + "IncludedStates";
            hql.append(" and ").append(alias).append(".state in (:").append(includedParam).append(")");
            params.put(includedParam, includedStates);
        }
        if (excludedStates != null && excludedStates.length > 0) {
            String excludedParam = paramPrefix + "ExcludedStates";
            hql.append(" and ").append(alias).append(".state not in (:").append(excludedParam).append(")");
            params.put(excludedParam, excludedStates);
        }
    }

    protected void bindQueryParameters(Query<?> query, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Collection<?>) {
                query.setParameterList(entry.getKey(), (Collection<?>) value);
            } else if (value instanceof Object[]) {
                query.setParameterList(entry.getKey(), (Object[]) value);
            } else {
                query.setParameter(entry.getKey(), value);
            }
        }
    }

}
