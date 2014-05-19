/**
 * ResponseBeanLocator.java - evaluation - Feb 02, 2007 11:35:56 AM - whumphri@vt.edu
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

package org.sakaiproject.evaluation.tool.locators;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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

    private Map<String, EvalResponse> delivered = new HashMap<String, EvalResponse>();

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
        for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
            String key = it.next();
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
            // saving so set the endTime to now
            response.setEndTime(new Date());
            localResponsesLogic.saveResponse(response);
        }
    }

}