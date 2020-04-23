/**
 * Copyright (c) 2005-2020 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.evaluation.tool.wrapper;

import org.sakaiproject.evaluation.dao.EvalDaoInvoker;
import org.springframework.transaction.UnexpectedRollbackException;

import lombok.extern.slf4j.Slf4j;
import uk.org.ponder.util.RunnableInvoker;

/**
 * This wraps our dao so that there is a transaction wrapped around requests,
 * This also allows us to lazy load things via the model in the tool
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Slf4j
public class ModelAccessWrapperInvoker implements RunnableInvoker {

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
            log.info("Eval: Caught transaction rollback exception: " + e.getCause());
        }
    }

}
