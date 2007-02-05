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

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplate;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * This is the OTP bean used to locate templates items
 * 
 * @author
 */

public class AnswersBeanLocator implements BeanLocator {
  public static final String NEW_PREFIX = "new";

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private EvalResponsesLogic responsesLogic;	
	public void setResponsesLogic(EvalResponsesLogic responsesLogic) {
		this.responsesLogic = responsesLogic;
	}

  private Map delivered = new HashMap();

  public Object locateBean(String path) {
    Object togo = delivered.get(path);
    if (togo == null) {
      if (path.startsWith(NEW_PREFIX)){
    	  //make new answer
    	  togo=new EvalAnswer();
      }
      else {
    	//fetch response, and all associated answers?
        //togo = localTemplateLogic.fetchResponse(Long.valueOf(path));
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
		EvalResponse response = new EvalResponse(new Date(), 
			external.getCurrentUserId(), external.getCurrentContext(), new Date(), eval);
		response.setEndTime(new Date()); 
		for (Iterator it = delivered.keySet().iterator(); it.hasNext();) {
	        String key = (String) it.next();
	        EvalAnswer answer = (EvalAnswer) delivered.get(key);
	        answer.setLastModified(new Date());
	        answer.setResponse(response);
	        response.getAnswers().add(answer);
	    }
		responsesLogic.saveResponse(response,external.getCurrentUserId());
	}
}