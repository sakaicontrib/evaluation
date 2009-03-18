/**
 * $Id$
 * $URL$
 * EvalActionResultInterceptor.java - evaluation - Mar 19, 2008 5:04:23 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool.wrapper;

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.tool.locators.EvaluationBeanLocator;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;

import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * An interceptor for the outgoing URLs from action cycles that applies the
 * IDs from freshly saved entities to the {@link EvalViewParameters#evaluationId} field
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalActionResultInterceptor implements ActionResultInterceptor {

    private EvaluationBeanLocator evaluationBeanLocator;
    public void setEvaluationBeanLocator(EvaluationBeanLocator evaluationBeanLocator) {
        this.evaluationBeanLocator = evaluationBeanLocator;
    }


    /* (non-Javadoc)
     * @see uk.org.ponder.rsf.flow.ActionResultInterceptor#interceptActionResult(uk.org.ponder.rsf.flow.ARIResult, uk.org.ponder.rsf.viewstate.ViewParameters, java.lang.Object)
     */
    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        // for evaluation creation
        if (incoming instanceof EvalViewParameters
                && result.resultingView instanceof EvalViewParameters) {
            EvalViewParameters in = (EvalViewParameters) incoming;
            EvalViewParameters outgoing = (EvalViewParameters) result.resultingView;
            if (in.evaluationId != null) {
                outgoing.evaluationId = in.evaluationId;
            } else {
                EvalEvaluation eval = (EvalEvaluation) evaluationBeanLocator.locateBean(EvaluationBeanLocator.NEW_1);
                if (eval != null) {
                    outgoing.evaluationId = eval.getId();
                }
            }
        }
    }

}
