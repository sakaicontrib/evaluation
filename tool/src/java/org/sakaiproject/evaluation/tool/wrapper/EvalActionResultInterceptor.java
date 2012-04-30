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
