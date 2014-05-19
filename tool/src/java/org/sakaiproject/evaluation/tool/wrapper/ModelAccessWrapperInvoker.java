/******************************************************************************
 * ModelAccessWrapperInvoker.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

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

    private static Log log = LogFactory.getLog(ModelAccessWrapperInvoker.class);

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
