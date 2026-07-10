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

import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;

public interface EvaluationEmailTemplateDao {

    public int countDefaultEmailTemplates();

    public List<EvalEmailTemplate> getDefaultEmailTemplates();

    public List<EvalEmailTemplate> getEmailTemplates(String ownerUserId, String emailTemplateType, Boolean includeDefaultsOnly);

    public EvalEmailTemplate getDefaultEmailTemplate(String emailTemplateType);

    public EvalEmailTemplate getEmailTemplateByEid(String eid);

    public List<EvalEvaluation> getEvaluationsUsingEmailTemplate(Long emailTemplateId, String emailTemplateType);

    public int countEvaluationsUsingEmailTemplate(Long emailTemplateId, String emailTemplateType);

    public void deleteEmailTemplates(Set<EvalEmailTemplate> emailTemplates);

    public EvalEmailTemplate getEmailTemplateById(Long emailTemplateId);

    public void saveEmailTemplate(EvalEmailTemplate emailTemplate);

    public void deleteEmailTemplate(EvalEmailTemplate emailTemplate);
}
