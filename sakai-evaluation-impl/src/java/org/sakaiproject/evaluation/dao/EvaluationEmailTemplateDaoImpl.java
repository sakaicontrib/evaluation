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

import java.util.List;
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
        StringBuilder hql = new StringBuilder("select emailTemplate from EvalEmailTemplate emailTemplate where 1 = 1");
        if (emailTemplateType != null) {
            hql.append(" and emailTemplate.type = :emailTemplateType");
        }
        if (ownerUserId != null) {
            hql.append(" and emailTemplate.owner = :ownerUserId");
        }
        if (includeDefaultsOnly != null) {
            hql.append(includeDefaultsOnly ? " and emailTemplate.defaultType is not null" : " and emailTemplate.defaultType is null");
        }

        Query<EvalEmailTemplate> query = currentSession().createQuery(hql.toString(), EvalEmailTemplate.class);
        if (emailTemplateType != null) {
            query.setParameter("emailTemplateType", emailTemplateType);
        }
        if (ownerUserId != null) {
            query.setParameter("ownerUserId", ownerUserId);
        }
        return query.list();
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
