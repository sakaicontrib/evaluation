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
package org.sakaiproject.evaluation.tool.inferrers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.entity.EvalReportsEntityProvider;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.producers.ReportsViewingProducer;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;


import org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * This will handle the case of reports viewing redirection
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net)
 */
public class ReportsVPInferrer implements EntityViewParamsInferrer {

    private static final Log LOG = LogFactory.getLog(ReportsVPInferrer.class);

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    public void init() {
        LOG.info("VP init");
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer#getHandledPrefixes()
     */
    public String[] getHandledPrefixes() {
        return new String[] {
                EvalReportsEntityProvider.ENTITY_PREFIX
        };
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer#inferDefaultViewParameters(java.lang.String)
     */
    public ViewParameters inferDefaultViewParameters(String reference) {
        String refId = EntityReference.getIdFromRef(reference);
        Long evaluationId = Long.valueOf(refId);
        EvalEvaluation evaluation = evaluationService.getEvaluationById(evaluationId);
        if (evaluation == null) {
            throw new IllegalArgumentException("Received an invalid evaluation id ("+evaluationId+") which cannot be resolved to an eval for the reports inferrer");
        }

        String currentUserId = commonLogic.getCurrentUserId();
        if (EvalConstants.SHARING_PUBLIC.equals(evaluation.getResultsSharing())) {
            // for public results, no login check is needed
        } else {
            // for all others we will require a login
            if (commonLogic.isUserAnonymous(currentUserId) ) {
                throw new SecurityException("User must be authenticated to access this page");
            }
        }

        String[] groupIds = new String[] {};
        /** Can only do this when we can get more info in the inferrer (like an EV)
        String evalGroupId = null;
        Long AssignGroupId = new Long(ep.id);
        EvalAssignGroup assignGroup = evaluationService.getAssignGroupById(AssignGroupId);
        evalGroupId = assignGroup.getEvalGroupId();
        evaluationId = assignGroup.getEvaluation().getId();
        evaluation = evaluationService.getEvaluationById(evaluationId);
        if (evalGroupId != null) {
            groupIds = new String[] {evalGroupId};
        }
        **/

        // just send the user to the reporting page, permissions are handled there
        ReportParameters vp = new ReportParameters(ReportsViewingProducer.VIEW_ID, evaluationId, groupIds);
        vp.external = true;
        return vp;
    }

}
