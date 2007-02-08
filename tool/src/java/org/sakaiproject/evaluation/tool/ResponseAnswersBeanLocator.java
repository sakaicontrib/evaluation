/*
 * Created on 8 Feb 2007
 */
package org.sakaiproject.evaluation.tool;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalResponse;

import uk.org.ponder.beanutil.BeanLocator;

public class ResponseAnswersBeanLocator implements BeanLocator {
  public static final String NEW_PREFIX = "new";

  private ResponseBeanLocator responseBeanLocator;

  private Map delivered = new HashMap();

  private LocalResponsesLogic localResponsesLogic;

  public void setLocalResponsesLogic(LocalResponsesLogic localResponsesLogic) {
    this.localResponsesLogic = localResponsesLogic;
  }
  
  public void setResponseBeanLocator(ResponseBeanLocator responseBeanLocator) {
    this.responseBeanLocator = responseBeanLocator;
  }

  public Object locateBean(String path) {
    BeanLocator togo = (BeanLocator) delivered.get(path);
    if (togo == null) {
      
      EvalResponse parent = (EvalResponse) responseBeanLocator.locateBean(path);
      //togo = new AnswersBeanLocator(parent, localResponsesLogic);
      delivered.put(path, togo);
  }
  return togo;
}
}
