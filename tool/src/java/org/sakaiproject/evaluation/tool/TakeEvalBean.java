/******************************************************************************
 * TakeEvalBean.java - created by whumphri@vt.edu on Jan 16, 2007
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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * This request-scope bean handles template creation and modification.
 * 
 * @author Will Humphries (whumphri@vt.edu)
 */

public class TakeEvalBean {

	private static Log log = LogFactory.getLog(TemplateBean.class);

	public EvalEvaluation eval;
	
	private ResponseBeanLocator answersBeanLocator;
	public void setAnswersBeanLocator(ResponseBeanLocator answersBeanLocator) {
		this.answersBeanLocator = answersBeanLocator;
	}

	/**
	 * 
	 */
	public String submitEvaluation() {
		log.debug("create response");
		answersBeanLocator.saveAll(eval);
		return "success";
	}
}