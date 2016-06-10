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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvalDaoInvoker;
import org.springframework.transaction.UnexpectedRollbackException;

import uk.org.ponder.util.RunnableInvoker;

/**
 * This wraps our dao so that there is a transaction wrapped around requests,
 * This also allows us to lazy load things via the model in the tool
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ModelAccessWrapperInvoker implements RunnableInvoker {

    private static final Log LOG = LogFactory.getLog(ModelAccessWrapperInvoker.class);

    public EvalDaoInvoker daoInvoker;
    public void setDaoInvoker(EvalDaoInvoker daoInvoker) {
        this.daoInvoker = daoInvoker;
    }

    /* (non-Javadoc)
     * @see uk.org.ponder.util.RunnableInvoker#invokeRunnable(java.lang.Runnable)
     */
    public void invokeRunnable(Runnable toinvoke) {
        try {
            daoInvoker.invokeTransactionalAccess(toinvoke);
        } catch (UnexpectedRollbackException e) {
            // this will stop the exceptions from reaching the portal
            LOG.info("Eval: Caught transaction rollback exception: " + e.getCause());
        }
    }

}
