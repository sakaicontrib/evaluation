/******************************************************************************
 * EvaluationBeanLocator.java - created by whumphri@vt.edu on Jan 20, 2007
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
package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;

import uk.org.ponder.beanutil.BeanLocator;


/**
 * This is the OTP bean used to locate Evaluations.
 * 
 * @author 
 */

public class EvaluationBeanLocator implements BeanLocator {
	public static final String NEW_PREFIX = "new";
    public static String NEW_1 = NEW_PREFIX +"1";
	
	private EvalEvaluationsLogic evalsLogic;
	public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
		this.evalsLogic = evalsLogic;
	}
	
	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}
    
	private Map delivered = new HashMap();
	
	public Object locateBean(String path) {
		Object togo=delivered.get(path);
		if (togo == null){
			if(path.startsWith(NEW_PREFIX)){
				togo = new EvalEvaluation();
			}
            else { 
				togo = evalsLogic.getEvaluationById(new Long(Long.parseLong(path.trim())));
			}
			delivered.put(path, togo);
		}
		return togo;
	}

	public void saveAll() {
      for (Iterator it = delivered.keySet().iterator(); it.hasNext();) {
        String key = (String) it.next();
        EvalEvaluation Evaluation = (EvalEvaluation) delivered.get(key);
        if (key.startsWith(NEW_PREFIX)) {
         // could do stuff here
        }
        evalsLogic.saveEvaluation(Evaluation, external.getCurrentUserId());
      }
    }
}