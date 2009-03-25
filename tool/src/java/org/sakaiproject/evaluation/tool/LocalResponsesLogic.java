/**
 * LocalResponsesLogic.java - evaluation - Jan 16, 2007 11:35:56 AM - whumphri
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalDeliveryService;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.utils.EvalUtils;
import org.sakaiproject.evaluation.utils.TemplateItemUtils;


/**
 * Handles extra logic related to saving and dealing with responses
 * 
 * @author Will Humphries (whumphri@vt.edu)
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class LocalResponsesLogic {

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalDeliveryService responsesLogic;
    public void setResponsesLogic(EvalDeliveryService responsesLogic) {
        this.responsesLogic = responsesLogic;
    }


    /**
     * Create an empty new Response for the current user
     * @return the new response for the current user
     */
    public EvalResponse newResponse() {
        EvalResponse togo = new EvalResponse(commonLogic.getCurrentUserId(), 
                new String(), null, new Date());
        //togo.setEndTime(new Date()); // TODO - I don't think this will work
        return togo;
    }

    /**
     * Create a new Answer associated with this response (not associated with a template Item though)
     * @param response
     * @return a new answer
     */
    public EvalAnswer newAnswer(EvalResponse response) {
        EvalAnswer answer = new EvalAnswer(response, null, null);
        return answer;
    }

    /**
     * Get a map of answers for the given response, where the key to
     * access a given response answer is the unique set of templateItemId and
     * the associated field/id (instructor id, environment key, etc.)
     * 
     * @param response the response we want to get the answers for
     * @return a hashmap of answers, where an answer's key is created using {@link TemplateItemUtils#makeTemplateItemAnswerKey(Long, String, String)}
     */	
    public Map<String, EvalAnswer> getAnswersMapByTempItemAndAssociated(Long responseId) {
        EvalResponse response = responsesLogic.getResponseById(responseId);
        Map<String, EvalAnswer> map;
        if (response.getAnswers() == null || response.getAnswers().isEmpty()) {
            map = new HashMap<String, EvalAnswer>();
        } else {
            map = EvalUtils.getAnswersMapByTempItemAndAssociated(response);
        }
        return map;
    }

    /**
     * Create a new response for this 
     * @param evaluationId
     * @param userId
     * @param evalGroupId
     * @return the id of the newly created response
     */
    public Long createResponse(Long evaluationId, String userId, String evalGroupId) {
        EvalResponse response = responsesLogic.getEvaluationResponseForUserAndGroup(evaluationId, userId, evalGroupId);
        return response.getId();
    }

    /**
     * This function takes a string containing a response id, and returns
     * the EvalResponse object corresponding to that id.
     * @param responseIdString string containing the id of the desired response
     * @return The EvalResponse object corresponding to the given id.
     */
    public EvalResponse getResponseById(String responseIdString) {
        Long responseId = Long.valueOf(responseIdString);
        EvalResponse response = responsesLogic.getResponseById(responseId);
        return response;	       
    }

    /**
     * Saves the current response, handles some fixup to the fields before saving
     * @param response
     */
    public void saveResponse(EvalResponse response) {
        responsesLogic.saveResponse(response, commonLogic.getCurrentUserId());
    }

}
