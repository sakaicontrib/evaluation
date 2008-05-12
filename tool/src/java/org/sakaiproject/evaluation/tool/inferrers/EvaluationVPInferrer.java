/******************************************************************************
 * EvaluationVPInferrer.java - created by aaronz on 28 May 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
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
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.entity.AssignGroupEntityProvider;
import org.sakaiproject.evaluation.logic.entity.EvaluationEntityProvider;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.producers.PreviewEvalProducer;
import org.sakaiproject.evaluation.tool.producers.ReportsViewingProducer;
import org.sakaiproject.evaluation.tool.producers.TakeEvalProducer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
import org.sakaiproject.evaluation.tool.viewparams.ReportParameters;
import org.sakaiproject.evaluation.tool.wrapper.ModelAccessWrapperInvoker;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles the redirection of incoming evaluation and assign group entity URLs to the proper views with the proper view params added
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationVPInferrer implements EntityViewParamsInferrer {

    private static Log log = LogFactory.getLog(EvaluationVPInferrer.class);

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
       this.evaluationService = evaluationService;
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
    	log.warn("Note: Routing user to view based on reference: " + reference);
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
            evaluation = evaluationService.getEvaluationById(evaluationId);
        } else if (AssignGroupEntityProvider.ENTITY_PREFIX.equals(ep.prefix)) {
            // we know the evaluation and the group
            Long AssignGroupId = new Long(ep.id);
            EvalAssignGroup assignGroup = evaluationService.getAssignGroupById(AssignGroupId);
            evalGroupId = assignGroup.getEvalGroupId();
            evaluation = assignGroup.getEvaluation();
            evaluationId = evaluation.getId();
        }

        if ( EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(evaluation.getAuthControl()) ) {
            // anonymous evaluation URLs ALWAYS go to the take_eval page
        	log.info("User taking anonymous evaluation: " + evaluationId + " for group: " + evalGroupId);
            return new EvalViewParameters(TakeEvalProducer.VIEW_ID, evaluationId, evalGroupId);
        } else {
            // authenticated evaluation URLs depend on the state of the evaluation and the users permissions,
            // failsafe goes to take eval when it cannot determine where else to go
            String currentUserId = commonLogic.getCurrentUserId();
            log.warn("Note: User ("+currentUserId+") taking authenticated evaluation: " + evaluationId + " in state ("+EvalUtils.getEvaluationState(evaluation, false)+") for group: " + evalGroupId);
            if (EvalConstants.EVALUATION_STATE_VIEWABLE.equals( EvalUtils.getEvaluationState(evaluation, false) )) {
                // go to the reports view
                if (currentUserId.equals(evaluation.getOwner()) ||
                        commonLogic.isUserAdmin(currentUserId)) { // TODO - make this a better check -AZ
                    return new ReportParameters(ReportsViewingProducer.VIEW_ID, evaluationId, new String[] {evalGroupId});
                } else {
                    // require auth
                    throw new SecurityException("User must be authenticated to access this page");
                }
            }

            if (EvalConstants.EVALUATION_STATE_INQUEUE.equals( EvalUtils.getEvaluationState(evaluation, false) )) {
                // go to the add instructor items view if permission
                if (evalGroupId == null) {
                   Map<Long, List<EvalAssignGroup>> m = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, null);
                    EvalGroup[] evalGroups = EvalUtils.getGroupsInCommon(
                            commonLogic.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_BE_EVALUATED), 
                            m.get(evaluationId) );
                    if (evalGroups.length > 0) {
                        // if we are being evaluated in at least one group in this eval then we can add items
                        // TODO - except we do not have a view yet so go to the preview eval page 
                        return new EvalViewParameters(PreviewEvalProducer.VIEW_ID, evaluationId);
                    }
                } else {
                    if (commonLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_BE_EVALUATED, evalGroupId)) {
                        // those being evaluated get to go to add their own questions
                        // TODO - except we do not have a view yet so go to the preview eval page 
                        return new EvalViewParameters(PreviewEvalProducer.VIEW_ID, evaluationId);
                    }
                }
                // else just require auth
                throw new SecurityException("User must be authenticated to access this page");
            }

            // finally, try to go to the take evals view
            if (! commonLogic.isUserAnonymous(currentUserId) ) {
                // check perms if not anonymous
                if (currentUserId.equals(evaluation.getOwner()) ||
                        commonLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_BE_EVALUATED, evalGroupId)) {
                    return new EvalViewParameters(PreviewEvalProducer.VIEW_ID, evaluationId);
                } else {
                    if ( evaluationService.canTakeEvaluation(currentUserId, evaluationId, evalGroupId) ) {
                    	log.info("User ("+currentUserId+") taking authenticated evaluation: " + evaluationId + " for group: " + evalGroupId);
                        return new EvalViewParameters(TakeEvalProducer.VIEW_ID, evaluationId, evalGroupId);
                    }
                }
            }

            throw new SecurityException("User must be authenticated to access this page");
        }
    }
}
