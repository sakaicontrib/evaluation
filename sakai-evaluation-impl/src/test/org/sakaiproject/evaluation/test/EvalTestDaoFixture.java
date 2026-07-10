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
package org.sakaiproject.evaluation.test;

import org.sakaiproject.evaluation.dao.EvaluationAdminSupportDao;
import org.sakaiproject.evaluation.dao.EvaluationAssignmentDao;
import org.sakaiproject.evaluation.dao.EvaluationAuthoringDao;
import org.sakaiproject.evaluation.dao.EvaluationConsolidatedEmailDao;
import org.sakaiproject.evaluation.dao.EvaluationDaoBase;
import org.sakaiproject.evaluation.dao.EvaluationEmailTemplateDao;
import org.sakaiproject.evaluation.dao.EvaluationGroupNodeDao;
import org.sakaiproject.evaluation.dao.EvaluationLockDao;
import org.sakaiproject.evaluation.dao.EvaluationQueryDao;
import org.sakaiproject.evaluation.dao.EvaluationResponseDao;
import org.sakaiproject.evaluation.dao.EvaluationSettingsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Shared DAO port wiring for evaluation integration tests.
 */
@Component
public class EvalTestDaoFixture {

    @Autowired
    @Qualifier("org.sakaiproject.evaluation.dao.EvaluationDaoBase")
    public EvaluationDaoBase persistence;
    @Autowired
    @Qualifier("org.sakaiproject.evaluation.dao.EvaluationSettingsDao")
    public EvaluationSettingsDao settingsDao;
    @Autowired
    @Qualifier("org.sakaiproject.evaluation.dao.EvaluationEmailTemplateDao")
    public EvaluationEmailTemplateDao emailTemplateDao;
    @Autowired
    @Qualifier("org.sakaiproject.evaluation.dao.EvaluationAuthoringDao")
    public EvaluationAuthoringDao authoringDao;
    @Autowired
    @Qualifier("org.sakaiproject.evaluation.dao.EvaluationGroupNodeDao")
    public EvaluationGroupNodeDao groupNodeDao;
    @Autowired
    @Qualifier("org.sakaiproject.evaluation.dao.EvaluationAdminSupportDao")
    public EvaluationAdminSupportDao adminSupportDao;
    @Autowired
    @Qualifier("org.sakaiproject.evaluation.dao.EvaluationResponseDao")
    public EvaluationResponseDao responseDao;
    @Autowired
    @Qualifier("org.sakaiproject.evaluation.dao.EvaluationAssignmentDao")
    public EvaluationAssignmentDao assignmentDao;
    @Autowired
    @Qualifier("org.sakaiproject.evaluation.dao.EvaluationQueryDao")
    public EvaluationQueryDao queryDao;
    @Autowired
    @Qualifier("org.sakaiproject.evaluation.dao.EvaluationLockDao")
    public EvaluationLockDao lockDao;
    @Autowired
    @Qualifier("org.sakaiproject.evaluation.dao.EvaluationConsolidatedEmailDao")
    public EvaluationConsolidatedEmailDao consolidatedEmailDao;
}
