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
package org.sakaiproject.evaluation.logic.scheduling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.logic.externals.EvalScheduledInvocation;
import org.sakaiproject.evaluation.logic.model.EvalScheduledJob;
import org.sakaiproject.evaluation.logic.model.EvalScheduledJob.EvalIdType;

/**
 * This class simply calls a method in EvalJobLogic 
 * when it is run by the ScheduledInvocationManager.
 * 
 * @author rwellis
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk) - fixed and simplified
 */
public class EvalScheduledInvocationImpl implements EvalScheduledInvocation {

    private static final Log LOG = LogFactory.getLog(EvalScheduledInvocationImpl.class);

    private EvalJobLogic evalJobLogic;
    public void setEvalJobLogic(EvalJobLogic evalJobLogic) {
        this.evalJobLogic = evalJobLogic;
    }

    /**
     * This executes the scheduled job and is called by the scheduler service,
     * there is no execution logic here, it simply calls the jobs logic method 
     * and that handles all the execution
     * 
     * @param opaqueContext a String that can be decoded to do determine what to do
     */
    public void execute(String opaqueContext) {
        if (opaqueContext == null || opaqueContext.equals("")) {
            throw new IllegalStateException("Invalid opaqueContext (null or empty), something has failed in the job scheduler");
        }

        if(LOG.isDebugEnabled()) {
            LOG.debug("EvalScheduledInvocationImpl.execute(" + opaqueContext + ")");
        }

        // opaqueContext provides evaluation id and job type.
        EvalIdType eit = EvalScheduledJob.decodeContextId(opaqueContext);
        Long evalId = eit.evaluationId;
        String jobType = eit.jobType;
        if (evalId == null || jobType == null) {
            throw new NullPointerException("EvalScheduledInvocationImpl.execute: both evaluationId ("+evalId+") and jobType ("+jobType+") must be set, opaqueContext=" + opaqueContext);
        }

        // call method to fix state, send email and/or schedule a job
        evalJobLogic.jobAction(evalId, jobType);
    }
}

