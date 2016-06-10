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
package org.sakaiproject.evaluation.tool.locators;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.tool.LocalResponsesLogic;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * This is the OTP bean used to locate responses
 * 
 * @author whumphri@vt.edu
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ResponseBeanLocator implements BeanLocator {

    public static final String NEW_PREFIX = "new";
    public static String NEW_1 = NEW_PREFIX + "1";

    private LocalResponsesLogic localResponsesLogic;
    public void setLocalResponsesLogic(LocalResponsesLogic localResponsesLogic) {
        this.localResponsesLogic = localResponsesLogic;
    }

    private Map<String, EvalResponse> delivered = new HashMap<>();

    public Object locateBean(String path) {
        EvalResponse togo = delivered.get(path);
        if (togo == null) {
            if (path.startsWith(NEW_PREFIX)) {
                togo = localResponsesLogic.newResponse();
            } else {
                togo = localResponsesLogic.getResponseById(path);
            }
            delivered.put(path, togo);
        }
        return togo;
    }

    /** Package-protected access to "dead" list of delivered beans */
    Map<String, EvalResponse> getDeliveredBeans() {
        return delivered;
    }
    
    public void saveAll(EvalEvaluation eval, String evalGroupId, Date startDate, Map<String, String[]> selectionOptions) {
        boolean isEvalComplete = true;
        saveAll(eval, evalGroupId, startDate, selectionOptions, isEvalComplete);
        
    }

    public void saveAll(EvalEvaluation eval, String evalGroupId, Date startDate, Map<String, String[]> selectionOptions, boolean isEvalComplete) {
        for( String key : delivered.keySet() )
        {
            EvalResponse response = (EvalResponse) delivered.get(key);
            if (response.getId() == null) {
                // response is new
                response.setEvaluation(eval);
                response.setEvalGroupId(evalGroupId);
            }
            // fix selection options
            if (selectionOptions != null) {
                for (Entry<String, String[]> selection : selectionOptions.entrySet()) {
                    response.setSelections(selection.getKey(), selection.getValue());
                }
            }
            if (startDate != null) {
                // we have a passed in start date so set the response start date
                response.setStartTime(startDate);
            }
            
            // store this state; once the date is set, the save function will need to 
            // know if this evaluation has been submitted before.
            response.isSubmitted(response.complete);            	
            // saving so set the endTime to now
            if (isEvalComplete) {
                response.setEndTime(new Date());
            }
            localResponsesLogic.saveResponse(response);
        }
    }

}