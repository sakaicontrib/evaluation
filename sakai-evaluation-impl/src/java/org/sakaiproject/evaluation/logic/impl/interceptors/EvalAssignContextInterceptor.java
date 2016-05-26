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
			return testEquals(eac, (EvalAssignGroup) invocation.getArguments()[0]);
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