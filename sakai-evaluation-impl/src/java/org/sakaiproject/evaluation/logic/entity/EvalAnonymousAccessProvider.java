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
package org.sakaiproject.evaluation.logic.entity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.tool.api.SessionManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles direct (non-portal) access to {@code /direct/eval-evaluation/{id}} and
 * {@code /direct/eval-assigngroup/{id}}, which is how the "permanent link" shown in
 * Control Evaluations and the link embedded in evaluation-available notification
 * emails are built (see {@code EvalEmailsLogicImpl}/{@code ControlEvaluationsController}).
 * <p>
 * Without this, EntityBroker has no HTML output handler registered for these prefixes
 * and the request fails with a 406. Restoring it here (instead of going through the
 * Sakai portal's tool placement URL) is also what lets a {@code NONE}-auth-control
 * (anonymous) evaluation be taken without first hitting the portal's login wall, since
 * a {@code SecurityException} is only thrown for evaluations that actually require a
 * logged-in user.
 */
@Slf4j
public class EvalAnonymousAccessProvider implements EntityViewAccessProvider {

    /** Context path of the evaluation tool's own standalone webapp (not the portal). */
    private static final String TOOL_WEBAPP_PATH = "/sakai-evaluation-tool";

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private SessionManager sessionManager;
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    private ServerConfigurationService serverConfigurationService;
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    private EntityViewAccessProviderManager accessProviderManager;
    public void setAccessProviderManager(EntityViewAccessProviderManager accessProviderManager) {
        this.accessProviderManager = accessProviderManager;
    }

    /** Spring init-method: register this provider for both entity prefixes it handles. */
    public void init() {
        accessProviderManager.registerProvider(AssignGroupEntityProvider.ENTITY_PREFIX, this);
        accessProviderManager.registerProvider(EvaluationEntityProvider.ENTITY_PREFIX, this);
    }

    public void handleAccess(EntityView view, HttpServletRequest req, HttpServletResponse res) {
        String prefix = view.getEntityReference().getPrefix();
        String id = view.getEntityReference().getId();

        EvalEvaluation eval;
        String evalGroupId = null;
        try {
            if (AssignGroupEntityProvider.ENTITY_PREFIX.equals(prefix)) {
                EvalAssignGroup assignGroup = evaluationService.getAssignGroupById(Long.valueOf(id));
                if (assignGroup == null) {
                    sendNotFound(res);
                    return;
                }
                // Resolve via a fresh getEvaluationById() call rather than the lazy
                // assignGroup.getEvaluation() proxy, which may not be safely
                // initializable here (this runs outside the usual Hibernate session
                // scope of a Spring MVC request).
                eval = evaluationService.getEvaluationById(assignGroup.getEvaluationId());
                evalGroupId = assignGroup.getEvalGroupId();
            } else {
                eval = evaluationService.getEvaluationById(Long.valueOf(id));
            }
        } catch (NumberFormatException e) {
            sendNotFound(res);
            return;
        }

        if (eval == null) {
            sendNotFound(res);
            return;
        }

        // Only NONE-auth-control evaluations may be accessed without an existing Sakai
        // session. Throwing here lets EntityBroker send the user to login and re-invoke
        // this method afterward (per EntityViewAccessProvider's contract).
        boolean loggedIn = sessionManager.getCurrentSessionUserId() != null;
        if (!loggedIn && !EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(eval.getAuthControl())) {
            throw new SecurityException("Login required to access evaluation " + eval.getId());
        }

        String target = buildTakeEvalUrl(eval.getId(), evalGroupId);
        try {
            res.sendRedirect(target);
        } catch (IOException e) {
            log.error("Could not redirect direct access for evaluation {} to {}", eval.getId(), target, e);
        }
    }

    private String buildTakeEvalUrl(Long evaluationId, String evalGroupId) {
        StringBuilder url = new StringBuilder(serverConfigurationService.getServerUrl())
                .append(TOOL_WEBAPP_PATH)
                .append("/take_eval?evaluationId=").append(evaluationId)
                .append("&external=true");
        if (evalGroupId != null) {
            url.append("&evalGroupId=").append(urlEncode(evalGroupId));
        }
        return url.toString();
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private void sendNotFound(HttpServletResponse res) {
        try {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException ignored) {
            // response already committed or client gone, nothing more we can do
        }
    }
}
