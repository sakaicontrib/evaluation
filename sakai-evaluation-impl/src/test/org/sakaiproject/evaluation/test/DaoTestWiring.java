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

import org.sakaiproject.evaluation.dao.EvalAdhocSupportImpl;
import org.sakaiproject.evaluation.dao.EvaluationAdminSupportDao;
import org.sakaiproject.evaluation.dao.EvaluationAssignmentDao;
import org.sakaiproject.evaluation.dao.EvaluationAuthoringDao;
import org.sakaiproject.evaluation.dao.EvaluationConsolidatedEmailDao;
import org.sakaiproject.evaluation.dao.EvaluationDaoBase;
import org.sakaiproject.evaluation.dao.EvaluationEmailTemplateDao;
import org.sakaiproject.evaluation.dao.EvaluationLockDao;
import org.sakaiproject.evaluation.dao.EvaluationQueryDao;
import org.sakaiproject.evaluation.dao.EvaluationResponseDao;
import org.sakaiproject.evaluation.dao.EvaluationSettingsDao;
import org.sakaiproject.evaluation.logic.EvalAuthoringServiceImpl;
import org.sakaiproject.evaluation.logic.EvalDeliveryServiceImpl;
import org.sakaiproject.evaluation.logic.EvalEvaluationServiceImpl;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupServiceImpl;
import org.sakaiproject.evaluation.logic.EvalSettingsImpl;
import org.sakaiproject.evaluation.logic.ReportingPermissionsImpl;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogicImpl;

/**
 * Wires narrow DAO ports into manually constructed service beans in unit tests.
 */
public final class DaoTestWiring {

    private DaoTestWiring() {
    }

    public static void wireEvalEvaluationService(EvalEvaluationServiceImpl service, DaoPorts ports) {
        service.setPersistence(ports.persistence);
        service.setQueryDao(ports.queryDao);
        service.setAssignmentDao(ports.assignmentDao);
        service.setResponseDao(ports.responseDao);
        service.setEmailTemplateDao(ports.emailTemplateDao);
        service.setConsolidatedEmailDao(ports.consolidatedEmailDao);
        service.setAuthoringDao(ports.authoringDao);
    }

    public static void wireEvalAuthoringService(EvalAuthoringServiceImpl service, DaoPorts ports) {
        service.setPersistence(ports.persistence);
        service.setAuthoringDao(ports.authoringDao);
        service.setLockDao(ports.lockDao);
    }

    public static void wireEvalEvaluationSetupService(EvalEvaluationSetupServiceImpl service, DaoPorts ports) {
        service.setPersistence(ports.persistence);
        service.setQueryDao(ports.queryDao);
        service.setAssignmentDao(ports.assignmentDao);
        service.setResponseDao(ports.responseDao);
        service.setEmailTemplateDao(ports.emailTemplateDao);
        service.setLockDao(ports.lockDao);
    }

    public static void wireEvalDeliveryService(EvalDeliveryServiceImpl service, DaoPorts ports) {
        service.setPersistence(ports.persistence);
        service.setQueryDao(ports.queryDao);
        service.setResponseDao(ports.responseDao);
        service.setLockDao(ports.lockDao);
    }

    public static void wireEvalSettings(EvalSettingsImpl settings, DaoPorts ports) {
        settings.setPersistence(ports.persistence);
        settings.setSettingsDao(ports.settingsDao);
    }

    public static void wireReportingPermissions(ReportingPermissionsImpl reportingPermissions, DaoPorts ports) {
        reportingPermissions.setAssignmentDao(ports.assignmentDao);
    }

    public static void wireExternalHierarchyLogic(ExternalHierarchyLogicImpl hierarchyLogic, DaoPorts ports) {
        hierarchyLogic.setPersistence(ports.persistence);
        hierarchyLogic.setAuthoringDao(ports.authoringDao);
        hierarchyLogic.setConsolidatedEmailDao(ports.consolidatedEmailDao);
        hierarchyLogic.setQueryDao(ports.queryDao);
        hierarchyLogic.setAssignmentDao(ports.assignmentDao);
    }

    public static void wireEvalAdhocSupport(EvalAdhocSupportImpl adhocSupport, DaoPorts ports) {
        adhocSupport.setPersistence(ports.persistence);
        adhocSupport.setAdminSupportDao(ports.adminSupportDao);
    }

    public static final class DaoPorts {
        public EvaluationDaoBase persistence;
        public EvaluationSettingsDao settingsDao;
        public EvaluationEmailTemplateDao emailTemplateDao;
        public EvaluationAuthoringDao authoringDao;
        public EvaluationAdminSupportDao adminSupportDao;
        public EvaluationResponseDao responseDao;
        public EvaluationAssignmentDao assignmentDao;
        public EvaluationQueryDao queryDao;
        public EvaluationLockDao lockDao;
        public EvaluationConsolidatedEmailDao consolidatedEmailDao;
    }
}
