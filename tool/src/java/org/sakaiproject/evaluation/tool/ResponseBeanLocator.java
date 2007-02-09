/******************************************************************************
 * ResponseBeanLocator.java - created by whumphri@vt.edu on Feb 02, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Will Humphries (whumphri@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * This is the OTP bean used to locate responses
 * 
 * @author
 */

public class ResponseBeanLocator implements BeanLocator {
  public static final String NEW_PREFIX = "new";

  private LocalResponsesLogic localResponsesLogic;

  private Map delivered = new HashMap();

  public void setLocalResponsesLogic(LocalResponsesLogic localResponsesLogic) {
    this.localResponsesLogic = localResponsesLogic;
  }

  public Object locateBean(String path) {
    Object togo = delivered.get(path);
    if (togo == null) {
      if (path.startsWith(NEW_PREFIX)) {
        togo = localResponsesLogic.newResponse();
      }
      else {
        togo = localResponsesLogic.fetchResponseById(path);
      }
      delivered.put(path, togo);
    }
    return togo;
  }

  /** Package-protected access to "dead" list of delivered beans */
  Map getDeliveredBeans() {
    return delivered;
  }

  public void saveAll(EvalEvaluation eval) {
    for (Iterator it = delivered.keySet().iterator(); it.hasNext();) {
      String key = (String) it.next();
      EvalResponse response = (EvalResponse) delivered.get(key);
      if(response.getId()==null)response.setEvaluation(eval);
      response.setEndTime(new Date());
      localResponsesLogic.saveResponse(response); 
    }
  }
}