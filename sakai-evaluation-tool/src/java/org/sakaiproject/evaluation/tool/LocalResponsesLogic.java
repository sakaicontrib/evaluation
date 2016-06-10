/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
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
     * @param responseId the response we want to get the answers for
     * @return a hashmap of answers, where an answer's key is created using {@link TemplateItemUtils#makeTemplateItemAnswerKey(Long, String, String)}
     */	
    public Map<String, EvalAnswer> getAnswersMapByTempItemAndAssociated(Long responseId) {
        EvalResponse response = responsesLogic.getResponseById(responseId);
        Map<String, EvalAnswer> map;
        if (response.getAnswers() == null || response.getAnswers().isEmpty()) {
            map = new HashMap<>();
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
