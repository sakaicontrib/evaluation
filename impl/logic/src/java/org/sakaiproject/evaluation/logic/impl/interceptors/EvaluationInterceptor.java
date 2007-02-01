/******************************************************************************
 * EvalAssignContextInterceptor.java - created on Jan 03, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Antranig Basman
 * Aaron Zeckoski (aaronz@vt.edu)
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.impl.interceptors;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl;
import org.sakaiproject.evaluation.model.EvalEvaluation;


/**
 * This interceptor uses methods in the other logic APIs to enforce "modify"
 * rules for persistent evaluation objects by doing checks whenever any method on the 
 * intercepted object is accessed
 * (modifyEvaluation filters to only look at set methods)
 * 
 * @author Antranig Basman
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationInterceptor implements MethodInterceptor {
	private EvalEvaluationsLogicImpl evalEvaluationsLogicImpl;

	public EvaluationInterceptor(EvalEvaluationsLogicImpl evalEvaluationsLogicImpl) {
		this.evalEvaluationsLogicImpl = evalEvaluationsLogicImpl;
	}

	public Object invoke(MethodInvocation invocation) throws Throwable {
		String method = invocation.getMethod().getName();
		EvalEvaluation eval = (EvalEvaluation) invocation.getThis();
		if (method.equals("equals")) {
			return Boolean.valueOf(testEquals(eval, (EvalEvaluation) invocation.getArguments()[0]));
		}
		if (eval.getId() != null) {
			evalEvaluationsLogicImpl.modifyEvaluation(eval, invocation.getMethod().getName());
		}
		return invocation.proceed();
	}

	private boolean testEquals(EvalEvaluation eval, EvalEvaluation eval2) {
		if (eval.getClass() == EvalEvaluation.class ^ eval2.getClass() == EvalEvaluation.class) {
			throw new IllegalArgumentException("Illegal comparison of EvalEvaluation - " +
			" can only compare persistent entities with other persistent entities");
		}
		return eval.getId().equals(eval2.getId());
	}

}
