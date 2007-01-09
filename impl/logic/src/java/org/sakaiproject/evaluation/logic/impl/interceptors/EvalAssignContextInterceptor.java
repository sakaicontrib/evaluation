/******************************************************************************
 * EvalAssignContextInterceptor.java - created by aaronz@vt.edu on Dec 28, 2006
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

package org.sakaiproject.evaluation.logic.impl.interceptors;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.model.EvalAssignContext;


/**
 * 
 * @author Antranig Basman (so you should blame him mostly)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalAssignContextInterceptor implements MethodInterceptor {
	private final Log log = LogFactory.getLog(getClass());

	public Object invoke(MethodInvocation arg0) throws Throwable {
		log.info("In the EvalAssignContextInterceptor");
		EvalAssignContext target = (EvalAssignContext) arg0.getThis();
		if (arg0.getMethod().getName().equals("setContext")) {
			String newContext = (String)arg0.getArguments()[0];
			if (! target.getContext().equals(newContext)) {
				throw new IllegalArgumentException("Cannot update context ("+target.getContext()+
						") for an existing AC, context ("+target.getContext()+")");
			}
		}
		return arg0.proceed();
	}
}