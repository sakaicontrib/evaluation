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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.query.Query;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * Hibernate-backed implementation methods for the matching evaluation DAO port.
 */
public class EvaluationEmailTemplateDaoImpl extends EvaluationDaoHibernateSupport implements EvaluationEmailTemplateDao {

    public int countDefaultEmailTemplates() {
        Long count = currentSession().createQuery(
                "select count(emailTemplate.id) from EvalEmailTemplate emailTemplate "
                + "where emailTemplate.defaultType is not null",
                Long.class)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public List<EvalEmailTemplate> getDefaultEmailTemplates() {
        return currentSession().createQuery(
                "select emailTemplate from EvalEmailTemplate emailTemplate "
                + "where emailTemplate.defaultType is not null",
                EvalEmailTemplate.class)
                .list();
    }

    public List<EvalEmailTemplate> getEmailTemplates(String ownerUserId, String emailTemplateType, Boolean includeDefaultsOnly) {
        Query<EvalEmailTemplate> query = currentSession().createQuery(
                "select emailTemplate from EvalEmailTemplate emailTemplate "
                + emailTemplatesWhereClause(ownerUserId, emailTemplateType, includeDefaultsOnly),
                EvalEmailTemplate.class);
        bindEmailTemplatesWhereParams(query, ownerUserId, emailTemplateType);
        return query.list();
    }

    public List<EvalEmailTemplate> getEmailTemplates(String ownerUserId, String emailTemplateType, Boolean includeDefaultsOnly,
            int firstResult, int maxResults) {
        Query<EvalEmailTemplate> query = currentSession().createQuery(
                "select emailTemplate from EvalEmailTemplate emailTemplate "
                + emailTemplatesWhereClause(ownerUserId, emailTemplateType, includeDefaultsOnly)
                + " order by emailTemplate.lastModified desc",
                EvalEmailTemplate.class);
        bindEmailTemplatesWhereParams(query, ownerUserId, emailTemplateType);
        query.setFirstResult(firstResult);
        query.setMaxResults(maxResults);
        return query.list();
    }

    public int countEmailTemplates(String ownerUserId, String emailTemplateType, Boolean includeDefaultsOnly) {
        Query<Long> query = currentSession().createQuery(
                "select count(emailTemplate) from EvalEmailTemplate emailTemplate "
                + emailTemplatesWhereClause(ownerUserId, emailTemplateType, includeDefaultsOnly),
                Long.class);
        bindEmailTemplatesWhereParams(query, ownerUserId, emailTemplateType);
        Long count = query.uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    private String emailTemplatesWhereClause(String ownerUserId, String emailTemplateType, Boolean includeDefaultsOnly) {
        StringBuilder hql = new StringBuilder("where 1 = 1");
        if (emailTemplateType != null) {
            hql.append(" and emailTemplate.type = :emailTemplateType");
        }
        if (ownerUserId != null) {
            hql.append(" and emailTemplate.owner = :ownerUserId");
        }
        if (includeDefaultsOnly != null) {
            hql.append(includeDefaultsOnly ? " and emailTemplate.defaultType is not null" : " and emailTemplate.defaultType is null");
        }
        return hql.toString();
    }

    private void bindEmailTemplatesWhereParams(Query<?> query, String ownerUserId, String emailTemplateType) {
        if (emailTemplateType != null) {
            query.setParameter("emailTemplateType", emailTemplateType);
        }
        if (ownerUserId != null) {
            query.setParameter("ownerUserId", ownerUserId);
        }
    }

    public EvalEmailTemplate getDefaultEmailTemplate(String emailTemplateType) {
        if (emailTemplateType == null) {
            throw new IllegalArgumentException("emailTemplateType cannot be null");
        }
        List<EvalEmailTemplate> templates = currentSession().createQuery(
                "select emailTemplate from EvalEmailTemplate emailTemplate where emailTemplate.defaultType = :emailTemplateType",
                EvalEmailTemplate.class)
                .setParameter("emailTemplateType", emailTemplateType)
                .setMaxResults(1)
                .list();
        return templates.isEmpty() ? null : templates.get(0);
    }

    public EvalEmailTemplate getEmailTemplateByEid(String eid) {
        return findOneByEid(EvalEmailTemplate.class, eid, "emailTemplate");
    }

    public List<EvalEvaluation> getEvaluationsUsingEmailTemplate(Long emailTemplateId, String emailTemplateType) {
        if (emailTemplateId == null) {
            throw new IllegalArgumentException("emailTemplateId cannot be null");
        }
        String property = getEvaluationEmailTemplateProperty(emailTemplateType);
        return currentSession().createQuery(
                "select evaluation from EvalEvaluation evaluation where evaluation." + property + ".id = :emailTemplateId",
                EvalEvaluation.class)
                .setParameter("emailTemplateId", emailTemplateId)
                .list();
    }

    public Map<Long, List<EvalEvaluation>> getEvaluationsUsingEmailTemplates(Collection<Long> emailTemplateIds, String emailTemplateType) {
        Map<Long, List<EvalEvaluation>> result = new HashMap<>();
        if (emailTemplateIds == null || emailTemplateIds.isEmpty()) {
            return result;
        }
        String property = getEvaluationEmailTemplateProperty(emailTemplateType);
        // Single query for the whole batch of ids instead of one round trip per template
        List<EvalEvaluation> evals = currentSession().createQuery(
                "select evaluation from EvalEvaluation evaluation where evaluation." + property + ".id in (:emailTemplateIds)",
                EvalEvaluation.class)
                .setParameterList("emailTemplateIds", emailTemplateIds)
                .list();
        for (EvalEvaluation eval : evals) {
            Long templateId = extractAssignedTemplateId(eval, property);
            if (templateId != null) {
                result.computeIfAbsent(templateId, k -> new ArrayList<>()).add(eval);
            }
        }
        return result;
    }

    private Long extractAssignedTemplateId(EvalEvaluation eval, String property) {
        EvalEmailTemplate template;
        switch (property) {
            case "availableEmailTemplate":
                template = eval.getAvailableEmailTemplate();
                break;
            case "reminderEmailTemplate":
                template = eval.getReminderEmailTemplate();
                break;
            case "submissionConfirmationEmailTemplate":
                template = eval.getSubmissionConfirmationEmailTemplate();
                break;
            default:
                throw new IllegalArgumentException("Unsupported email template property: " + property);
        }
        return template != null ? template.getId() : null;
    }

    public int countEvaluationsUsingEmailTemplate(Long emailTemplateId, String emailTemplateType) {
        if (emailTemplateId == null) {
            throw new IllegalArgumentException("emailTemplateId cannot be null");
        }
        String property = getEvaluationEmailTemplateProperty(emailTemplateType);
        Long count = currentSession().createQuery(
                "select count(evaluation.id) from EvalEvaluation evaluation where evaluation." + property + ".id = :emailTemplateId",
                Long.class)
                .setParameter("emailTemplateId", emailTemplateId)
                .uniqueResult();
        return count == null ? 0 : count.intValue();
    }

    public void deleteEmailTemplates(Set<EvalEmailTemplate> emailTemplates) {
        deleteAll(emailTemplates);
    }

    public EvalEmailTemplate getEmailTemplateById(Long emailTemplateId) {
        return findById(EvalEmailTemplate.class, emailTemplateId);
    }

    public void saveEmailTemplate(EvalEmailTemplate emailTemplate) {
        save(emailTemplate);
    }

    public void deleteEmailTemplate(EvalEmailTemplate emailTemplate) {
        delete(emailTemplate);
    }

    private String getEvaluationEmailTemplateProperty(String emailTemplateType) {
        if (EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailTemplateType)
                || EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_AVAILABLE.equals(emailTemplateType)
                || EvalConstants.EMAIL_TEMPLATE_AVAILABLE_EVALUATEE.equals(emailTemplateType)) {
            return "availableEmailTemplate";
        }
        if (EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailTemplateType)
                || EvalConstants.EMAIL_TEMPLATE_CONSOLIDATED_REMINDER.equals(emailTemplateType)) {
            return "reminderEmailTemplate";
        }
        if (EvalConstants.EMAIL_TEMPLATE_SUBMITTED.equals(emailTemplateType)) {
            return "submissionConfirmationEmailTemplate";
        }
        throw new IllegalArgumentException("Unsupported email template type: " + emailTemplateType);
    }
}
