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
package org.sakaiproject.evaluation.tool.controllers;

import javax.annotation.Resource;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.dao.EvalDaoInvoker;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.ReportingPermissions;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;

/**
 * Base class for Sakai Evaluation controllers.
 *
 * Provides shared service injections and helper methods for user identity,
 * admin checks, and evaluation permission enforcement. Controllers that need
 * one-off collaborators declare those locally with their own {@code @Resource}
 * fields.
 *
 * All Spring MVC controllers should extend this class unless they do not use
 * evaluation services, such as exception handlers or static message endpoints.
 */
public abstract class EvalControllerSupport {

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalCommonLogic")
    protected EvalCommonLogic commonLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationService")
    protected EvalEvaluationService evaluationService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalAuthoringService")
    protected EvalAuthoringService authoringService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalSettings")
    protected EvalSettings settings;

    @Resource(name = "org.sakaiproject.evaluation.dao.EvalDaoInvoker")
    protected EvalDaoInvoker daoInvoker;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalEvaluationSetupService")
    protected EvalEvaluationSetupService evaluationSetupService;

    @Resource(name = "org.sakaiproject.evaluation.logic.EvalDeliveryService")
    protected EvalDeliveryService deliveryService;

    @Resource(name = "org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic")
    protected ExternalHierarchyLogic hierarchyLogic;

    @Resource(name = "org.sakaiproject.evaluation.beans.EvalBeanUtils")
    protected EvalBeanUtils evalBeanUtils;

    @Resource(name = "localTemplateLogic")
    protected LocalTemplateLogic localTemplateLogic;

    @Resource(name = "org.sakaiproject.evaluation.logic.ReportingPermissions")
    protected ReportingPermissions reportingPermissions;

    /** Returns the current user's internal ID. */
    protected String currentUserId() {
        return commonLogic.getCurrentUserId();
    }

    /** Returns true if the current user has global admin rights. */
    protected boolean isCurrentUserAdmin() {
        return commonLogic.isUserAdmin(currentUserId());
    }

    /**
     * Throws {@link SecurityException} if {@code userId} cannot control the
     * given evaluation.  Call this at the top of any POST handler that mutates
     * evaluation data.
     */
    protected void requireControlEvaluation(String userId, Long evaluationId) {
        if (!evaluationService.canControlEvaluation(userId, evaluationId)) {
            throw new SecurityException(
                    "User does not have permission to control evaluation: " + evaluationId);
        }
    }
}
