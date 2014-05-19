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
import org.sakaiproject.evaluation.model.EvalAssignGroup;


/**
 * This interceptor uses methods in the other logic APIs to enforce "modify"
 * rules for persistent objects by doing checks whenever any method on the 
 * intercepted object is accessed
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalAssignContextInterceptor implements MethodInterceptor {

	public Object invoke(MethodInvocation invocation) throws Throwable {
		String method = invocation.getMethod().getName();
		EvalAssignGroup eac = (EvalAssignGroup) invocation.getThis();
		if (method.equals("equals")) {
			return Boolean.valueOf(testEquals(eac, (EvalAssignGroup) invocation.getArguments()[0]));
		}
		if (eac.getId() != null) {
			// check if this eac can be modified in this way
			//evalEvaluationsLogicImpl.modifyEvaluation(eval, invocation.getMethod().getName());
		}
		return invocation.proceed();
	}

	private boolean testEquals(EvalAssignGroup i1, EvalAssignGroup i2) {
		if (i1.getClass() == EvalAssignGroup.class ^ i2.getClass() == EvalAssignGroup.class) {
			throw new IllegalArgumentException("Illegal comparison of EvalAssignContext - " +
			" can only compare persistent entities with other persistent entities");
		}
		return i1.getId().equals(i2.getId());
	}

}