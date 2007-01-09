/*
 * Created on 03-Jan-2007
 */
package org.sakaiproject.evaluation.logic.impl.interceptors;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.sakaiproject.evaluation.logic.impl.EvalEvaluationsLogicImpl;
import org.sakaiproject.evaluation.model.EvalEvaluation;


public class EvaluationInterceptor implements MethodInterceptor {
  private EvalEvaluationsLogicImpl evalEvaluationsLogicImpl;
  
  public EvaluationInterceptor(EvalEvaluationsLogicImpl evalEvaluationsLogicImpl) {
    this.evalEvaluationsLogicImpl = evalEvaluationsLogicImpl;
  }
  
  public Object invoke(MethodInvocation arg0) throws Throwable {
    String method = arg0.getMethod().getName();
    EvalEvaluation eval = (EvalEvaluation) arg0.getThis();
    if (method.equals("equals")) {
      return Boolean.valueOf(testEquals(eval, (EvalEvaluation) arg0.getArguments()[0]));
    }
    if (eval.getId() != null) {
      evalEvaluationsLogicImpl.modifyEvaluation(eval, arg0.getMethod().getName());
    }
    return arg0.proceed();
  }

  private boolean testEquals(EvalEvaluation eval, EvalEvaluation eval2) {
    if (eval.getClass() == EvalEvaluation.class ^ eval2.getClass() == EvalEvaluation.class) {
      throw new IllegalArgumentException("Illegal comparison of EvalEvaluation - " +
            " can only compare persistent entities with other persistent entities");
    }
    return eval.getId().equals(eval2.getId());
  }




}
