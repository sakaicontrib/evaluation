/******************************************************************************
 * AnswersBeanLocator.java - created by whumphri@vt.edu on Feb 02, 2007
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.model.EvalAnswer;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * This is the OTP bean used to locate templates items
 * 
 * @author
 */

public class AnswersBeanLocator implements BeanLocator {
  public static final String NEW_PREFIX = "new";

  private Map delivered = new HashMap();

  public Object locateBean(String path) {
    Object togo = delivered.get(path);
    //no answer has been created for this item-response pairing, so we'll make one. 
    //we don't use the new prefix, because the producer has no way of knowing if 
    //an answer has been created for the given item, even if a response exists.
    if (togo == null) {
      togo=new EvalAnswer();
      delivered.put(path, togo);
    }
    return togo;
  }

  /** Package-protected access to "dead" list of delivered beans */
  Map getDeliveredBeans() {
    return delivered;
  }
  
  /** returns a HashSet containing the EvalAnswers in delivered*/
  public Set fetchSet(){
	  return delivered.entrySet();
  }
    
  /** loads a HashMap with the answers provided. The key used to
   * access an answer will be of the form <responseNum>.<itemNum>.<field>
   * @param answers - HashSet of answers
   * @param evalID - id of evaluation associated with current resposne
   */
  public void loadMap(Set answers, Long evalId){
		for (Iterator it = answers.iterator(); it.hasNext();) {
	        String key = (String) it.next();
	        EvalAnswer answer = (EvalAnswer) delivered.get(key);
	        delivered.put(evalId+"."+answer.getItem().getId(), answer);
	    }
  }
  
	/*This method is not needed, ResponseBeanLocator saves for us.
	 public void saveAll(EvalEvaluation eval) {
		
		//outer for loop - iterate through hashmap
		//inner for loop - find the other one and replace numeric and text
		
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
	}*/
}