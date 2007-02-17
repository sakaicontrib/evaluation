/******************************************************************************
 * LocalResponsesLogic.java - created on Jan 16, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Will Humphries (whumphri@vt.edu)
 * Aaron Zeckoski (aaronz@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplateItem;


public class LocalResponsesLogic {

	private static Log log = LogFactory.getLog(LocalResponsesLogic.class);

	private EvalExternalLogic external;
	public void setExternal(EvalExternalLogic external) {
		this.external = external;
	}

	private EvalResponsesLogic responsesLogic;
	public void setResponsesLogic(EvalResponsesLogic responsesLogic) {
		this.responsesLogic = responsesLogic;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic(EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	public EvalResponse newResponse() {
		log.debug("Creating a new response");
		EvalResponse togo = new EvalResponse(new Date(), external
				.getCurrentUserId(), new String(), new Date(), null);
		togo.setEndTime(new Date());
		return togo;
	}

	public EvalAnswer newAnswer(EvalResponse response, Long templateItemId) {
		log.debug("Response: " + response.getId() + ", templateItemId: " + templateItemId);
		EvalTemplateItem templateItem = itemsLogic.getTemplateItemById(templateItemId);
		EvalAnswer answer = new EvalAnswer(new Date(), templateItem, response);
		return answer;
	}

	public EvalResponse fetchResponseById(String evalIdstring) {
		Long evalId = Long.valueOf(evalIdstring);
		log.debug("Evaluation: " + evalId );

		String userId = external.getCurrentUserId();
		List responses = responsesLogic.getEvaluationResponses(userId, new Long[] {evalId});
		String context = external.getCurrentContext();
		for (int i = 0; i < responses.size(); ++ i) {
			EvalResponse response = (EvalResponse) responses.get(i);
			if (response.getContext().equals(context)) return response;
		}
		throw new IllegalArgumentException("Could not locate response for eval id "
				+ evalId + " userID " + userId +" in context " + context); 

	}

	/**
	 * This function takes a string containing a response id, and returns
	 * the EvalResponse object corresponding to that id.
	 * @param responseIdString - string containing the id of the desired response
	 * @return The EvalResponse object corresponding to the given id.
	 */
	public EvalResponse getResponseById(String responseIdString) {
		Long responseId = Long.valueOf(responseIdString);
		log.info("attempting to fetch response with id:"+responseId);

		EvalResponse response = responsesLogic.getResponseById(responseId);
		return response;	       
	}

	public void saveResponse(EvalResponse response) {
		log.debug("Response: " + response.getId() );
		//strip out responses with no value set for numeric or text
		Set answers=response.getAnswers();
		Set newAnswers=new HashSet(0);
		for (Iterator it = answers.iterator(); it.hasNext();) {
			EvalAnswer answer = (EvalAnswer) it.next();
			if(answer.getNumeric()!=null || answer.getText()!=null)
				newAnswers.add(answer);
		}
		response.setAnswers(newAnswers);
		responsesLogic.saveResponse(response, external.getCurrentUserId());
	}
}
