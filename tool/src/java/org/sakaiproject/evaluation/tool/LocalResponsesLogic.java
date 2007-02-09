/*
 * Created on 8 Feb 2007
 */
package org.sakaiproject.evaluation.tool;

import java.util.Date;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;


public class LocalResponsesLogic {
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
    EvalResponse togo = new EvalResponse(new Date(), external
        .getCurrentUserId(), new String(), new Date(), null);
    togo.setEndTime(new Date());
    return togo;
  }
  
  public EvalAnswer newAnswer(EvalResponse response, Long evalItemId) {
    EvalItem item = itemsLogic.getItemById(evalItemId);
    EvalAnswer answer = new EvalAnswer(new Date(), item, response);
    return answer;
  }
  
  public EvalResponse fetchResponseById(String evalIdstring) {
    Long evalId = Long.valueOf(evalIdstring);
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
	    System.out.println("attempting to fetch response with id:"+responseId);
	    EvalResponse response = responsesLogic.getResponseById(responseId);
	    return response;	       
  }

  public void saveResponse(EvalResponse response) {
    responsesLogic.saveResponse(response, external.getCurrentUserId());
  }
}
