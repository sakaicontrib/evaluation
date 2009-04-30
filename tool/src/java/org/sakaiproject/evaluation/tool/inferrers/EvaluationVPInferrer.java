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
import org.sakaiproject.evaluation.tool.producers.TakeEvalProducer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;
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
    	//log.warn("Note: Routing user to view based on reference: " + reference);
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
            evaluationId = assignGroup.getEvaluation().getId();
            evaluation = evaluationService.getEvaluationById(evaluationId);
        }

        if ( EvalConstants.EVALUATION_AUTHCONTROL_NONE.equals(evaluation.getAuthControl()) ) {
            // anonymous evaluation URLs ALWAYS go to the take_eval page
        	log.info("User taking anonymous evaluation: " + evaluationId + " for group: " + evalGroupId);
        	EvalViewParameters vp = new EvalViewParameters(TakeEvalProducer.VIEW_ID, evaluationId, evalGroupId);
        	vp.external = true;
        	return vp;
        } else {
            // authenticated evaluation URLs depend on the state of the evaluation and the users permissions
            String currentUserId = commonLogic.getCurrentUserId();
            log.info("Note: User ("+currentUserId+") accessing authenticated evaluation: " + evaluationId + " in state ("+EvalUtils.getEvaluationState(evaluation, false)+") for group: " + evalGroupId);

            // eval has not started
            if ( EvalUtils.checkStateBefore(EvalUtils.getEvaluationState(evaluation, false), EvalConstants.EVALUATION_STATE_INQUEUE, true) ) {
                // go to the add instructor items view if permission
                if (evalGroupId == null) {
                   Map<Long, List<EvalAssignGroup>> m = evaluationService.getAssignGroupsForEvals(new Long[] {evaluationId}, true, null);
                    EvalGroup[] evalGroups = EvalUtils.getGroupsInCommon(
                            commonLogic.getEvalGroupsForUser(currentUserId, EvalConstants.PERM_BE_EVALUATED), 
                            m.get(evaluationId) );
                    if (evalGroups.length > 0) {
                        // if we are being evaluated in at least one group in this eval then we can add items
                        // TODO - except we do not have a view yet so go to the preview eval page 
                        EvalViewParameters vp = new EvalViewParameters(PreviewEvalProducer.VIEW_ID, evaluationId);
                        vp.external = true;
                        return vp;
                    }
                } else {
                    if (commonLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_BE_EVALUATED, evalGroupId)) {
                        // those being evaluated get to go to add their own questions
                        // TODO - except we do not have a view yet so go to the preview eval page 
                        EvalViewParameters vp = new EvalViewParameters(PreviewEvalProducer.VIEW_ID, evaluationId);
                        vp.external = true;
                        return vp;
                    }
                }
                // else just require auth
                throw new SecurityException("User must be authenticated to access this page");
            }

            // finally, try to go to the take evals view
            if (! commonLogic.isUserAnonymous(currentUserId) ) {
                // check perms if not anonymous
                // switched to take check first
                if ( evaluationService.canTakeEvaluation(currentUserId, evaluationId, evalGroupId) ) {
                	log.info("User ("+currentUserId+") taking authenticated evaluation: " + evaluationId + " for group: " + evalGroupId);
                	EvalViewParameters vp = new EvalViewParameters(TakeEvalProducer.VIEW_ID, evaluationId, evalGroupId);
                    vp.external = true;
                    return vp;
                } else if (currentUserId.equals(evaluation.getOwner()) ||
                        commonLogic.isUserAllowedInEvalGroup(currentUserId, EvalConstants.PERM_BE_EVALUATED, evalGroupId)) {
                    // cannot take, but can preview
                    EvalViewParameters vp = new EvalViewParameters(PreviewEvalProducer.VIEW_ID, evaluationId);
                    vp.external = true;
                    return vp;
                } else {
                    // no longer want to show security exceptions - https://bugs.caret.cam.ac.uk/browse/CTL-1548
                    //throw new SecurityException("User ("+currentUserId+") does not have permission to take or preview this evaluation ("+evaluationId+")");
                    return new EvalViewParameters(TakeEvalProducer.VIEW_ID, evaluationId, evalGroupId);
                }
            }

            throw new SecurityException("User must be authenticated to access this page");
        }
    }
}
