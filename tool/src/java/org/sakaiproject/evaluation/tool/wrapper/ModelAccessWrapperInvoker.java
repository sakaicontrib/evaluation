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

import org.sakaiproject.evaluation.dao.EvaluationDao;

import uk.org.ponder.util.RunnableInvoker;

/**
 * This wraps our dao so that it can be accessed lazily
 * The purpose of this is to adapt the DAO interface to the RSF interface
 * as required By ClassLoader Separation
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ModelAccessWrapperInvoker implements RunnableInvoker {

	private EvaluationDao evaluationDao;
	public void setEvaluationDao(EvaluationDao evaluationDao) {
		this.evaluationDao = evaluationDao;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.util.RunnableInvoker#invokeRunnable(java.lang.Runnable)
	 */
	public void invokeRunnable(Runnable toinvoke) {
		evaluationDao.invokeTransactionalAccess(toinvoke);
	}

}
