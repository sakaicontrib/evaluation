/******************************************************************************
 * EvaluationVPInferrer.java - created by aaronz on 28 May 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.inferrers;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.evaluation.logic.EvalAssignsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.entity.AssignGroupEntityProvider;
import org.sakaiproject.evaluation.logic.entity.EvaluationEntityProvider;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.utils.EvalUtils;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.producers.PreviewEvalProducer;
import org.sakaiproject.evaluation.tool.producers.TakeEvalProducer;
import org.sakaiproject.evaluation.tool.producers.ViewReportProducer;
import org.sakaiproject.evaluation.tool.viewparams.EvalTakeViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.PreviewEvalParameters;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.tool.wrapper.ModelAccessWrapperInvoker;

import uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles the redirection of incoming evaluation and assign group entity URLs to the proper views with the proper view params added
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationVPInferrer implements EntityViewParamsInferrer {

    private static Log log = LogFactory.getLog(EvaluationVPInferrer.class);

    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }

    private EvalAssignsLogic assignsLogic;
    public void setAssignsLogic(EvalAssignsLogic assignsLogic) {
        this.assignsLogic = assignsLogic;
    }

    private EvalEvaluationsLogic evaluationsLogic;
    public void setEvaluationsLogic(EvalEvaluationsLogic evaluationsLogic) {
        this.evaluationsLogic = evaluationsLogic;
    }

    private ModelAccessWrapperInvoker wrapperInvoker;
    public void setWrapperInvoker(ModelAccessWrapperInvoker wrapperInvoker) {
        this.wrapperInvoker = wrapperInvoker;
    }


    public void init() {
        log.info("VP init");
    }

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer#getHandledPrefixes()
     */
    public String[] getHandledPrefixes() {
        return new String[] {
                EvaluationEntityProvider.ENTITY_PREFIX,
                AssignGroupEntityProvider.ENTITY_PREFIX
        };
    }

    /* (non-Javadoc)
     * @see uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer#inferDefaultViewParameters(java.lang.String)
     */
    public ViewParameters inferDefaultViewParameters(String reference) {
        final String ref = reference;
        final ViewParameters[] togo = new ViewParameters[1];
        // this is needed to provide transactional protection
        wrapperInvoker.invokeRunnable( new Runnable() {
            public void run() {
                togo [0] = inferDefaultViewParametersImpl(ref);
            }
        });
        return togo[0];
    }

    /**
     * @param reference
     * @return
     */
    private ViewParameters inferDefaultViewParametersImpl(String reference) {
        IdEntityReference ep = new IdEntityReference(reference);
        EvalEvaluation evaluation = null;
        Long evaluationId = null;
        String evalGroupId = null;

        if (EvaluationEntityProvider.ENTITY_PREFIX.equals(ep.prefix)) {
            // we only know the evaluation
            evaluationId = new Long(ep.id);
            evaluation = evaluationsLogic.getEvaluationById(evaluationId);
        } else if (AssignGroupEntityProvider.ENTITY_PREFIX.equals(ep.prefix)) {
            // we know the evaluation and the group
            Long AssignGroupId = new Long(ep.id);
            EvalAssignGroup assignGroup = assignsLogic.getAssignGroupById(AssignGroupId);
            evalGroupId = assignGroup.getEvalGroupId();
            evaluation = assignGroup.getEvaluation();
            evaluationId = evaluation.getId();
        }

        if ( EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(evaluation.getAuthControl()) ) {
            // anonymous evaluation URLs ALWAYS go to the take_eval page
            return new EvalTakeViewParameters(TakeEvalProducer.VIEW_ID, evaluationId, evalGroupId);
        } else {
            // authenticated evaluation URLs depend on the state of the evaluation and the users permissions,
            // failsafe goes to take eval when it cannot determine where else to go
            String currentUserId = externalLogic.getCurrentUserId();
            if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals( EvalUtils.getEvaluationState(evaluation) )) {
                // go to the reports view
                if (currentUserId.equals(evaluation.getOwner()) ||
                        externalLogic.isUserAdmin(currentUserId)) { // TODO - make this a better check -AZ
                    return new ReportParameters(ViewReportProducer.VIEW_ID, evaluationId, new String[] {evalGroupId});
                } else {
                    // require auth
                    throw new SecurityException("User must be authenticated to access this page");
                }
            }

            if (EvalConstants.EVALUATION_STATE_INQUEUE.equals( EvalUtils.getEvaluationState(evaluation) )) {
                // go to the add instructor items view if permission
                if (evalGroupId == null) {
                    Map m = evaluationsLogic.getEvaluationAssignGroups(new Long[] {evaluationId}, true);
                    EvalGroup[] evalGroups = EvalUtils.getGroupsInCommon(
                            externalLogic.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_BE_EVALUATED), 
                            (List) m.get(evaluationId) );
                    if (evalGroups.length > 0) {
                        // if we are being evaluated in at least one group in this eval then we can add items
                        // TODO - except we do not have a view yet so go to the preview eval page 
                        return new PreviewEvalParameters(PreviewEvalProducer.VIEW_ID, evaluationId, null);
                    }
                } else {
                    if (externalLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_BE_EVALUATED, evalGroupId)) {
                        // those being evaluated get to go to add their own questions
                        // TODO - except we do not have a view yet so go to the preview eval page 
                        return new PreviewEvalParameters(PreviewEvalProducer.VIEW_ID, evaluationId, null);
                    }
                }
                // else just require auth
                throw new SecurityException("User must be authenticated to access this page");
            }

            // finally, try to go to the take evals view
            if (! externalLogic.isUserAnonymous(currentUserId) ) {
                // check perms if not anonymous
                if (currentUserId.equals(evaluation.getOwner()) ||
                        externalLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_BE_EVALUATED, evalGroupId)) {
                    return new PreviewEvalParameters(PreviewEvalProducer.VIEW_ID, evaluationId, null);
                } else {
                    if ( evaluationsLogic.canTakeEvaluation(currentUserId, evaluationId, evalGroupId) ) {
                        return new EvalTakeViewParameters(TakeEvalProducer.VIEW_ID, evaluationId, evalGroupId);
                    }
                }
            }

            throw new SecurityException("User must be authenticated to access this page");
        }
    }
}
