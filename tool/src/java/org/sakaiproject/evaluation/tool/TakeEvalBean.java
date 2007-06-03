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
import org.sakaiproject.evaluation.tool.locators.ResponseBeanLocator;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * This request-scope bean handles taking evaluations
 * 
 * @author Will Humphries (whumphri@vt.edu)
 */
public class TakeEvalBean {

	private static Log log = LogFactory.getLog(TakeEvalBean.class);

	public EvalEvaluation eval;
	public String context;

	private ResponseBeanLocator responseBeanLocator;
	public void setResponseBeanLocator(ResponseBeanLocator responseBeanLocator) {
		this.responseBeanLocator = responseBeanLocator;
	}

	private TargettedMessageList messages;
	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}

	public String submitEvaluation() {
		log.debug("create response");
		responseBeanLocator.saveAll(eval, context);
		messages.addMessage( new TargettedMessage("evaluations.take.message",
                new Object[] { eval.getTitle() }, 
                TargettedMessage.SEVERITY_INFO));
		return "success";
	}

}