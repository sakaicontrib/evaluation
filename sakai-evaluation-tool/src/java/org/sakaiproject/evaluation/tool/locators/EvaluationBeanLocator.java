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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.evaluation.beans.EvalBeanUtils;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.model.EvalEvaluation;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * This is the OTP bean used to locate Evaluations.
 * 
 * @author Will Humphries (whumphri@vt.edu)
 */
public class EvaluationBeanLocator implements BeanLocator {

    public static final String NEW_PREFIX = "new";
    public static String NEW_1 = NEW_PREFIX + "1";

    private EvalCommonLogic commonLogic;
    public void setCommonLogic(EvalCommonLogic commonLogic) {
        this.commonLogic = commonLogic;
    }

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    private EvalEvaluationSetupService evaluationSetupService;
    public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
        this.evaluationSetupService = evaluationSetupService;
    }

    private EvalBeanUtils evalBeanUtils;
    public void setEvalBeanUtils(EvalBeanUtils evalBeanUtils) {
        this.evalBeanUtils = evalBeanUtils;
    }

    public Map<String, String> selectionSettings;

    private Map<String, EvalEvaluation> delivered = new HashMap<>();

    public Object locateBean(String name) {
        EvalEvaluation togo = delivered.get(name);
        if (togo == null) {
            if (name.startsWith(NEW_PREFIX)) {
                togo = new EvalEvaluation(EvalConstants.EVALUATION_TYPE_EVALUATION, commonLogic.getCurrentUserId(),
                        null, null, EvalConstants.EVALUATION_STATE_PARTIAL, null, null, null);
                // set the defaults for this newly created evaluation
                evalBeanUtils.setEvaluationDefaults(togo, EvalConstants.EVALUATION_TYPE_EVALUATION);
            } else {
                togo = evaluationService.getEvaluationById(new Long(name));
            }
            delivered.put(name, togo);
        }
        return togo;
    }

    public void saveAll() {
        for( String key : delivered.keySet() )
        {
            EvalEvaluation evaluation = delivered.get(key);
            if (key.startsWith(NEW_PREFIX)) {
                // set eval defaults here
                evalBeanUtils.setEvaluationDefaults(evaluation, EvalConstants.EVALUATION_TYPE_EVALUATION);
            }
            // validate the eval category before saving, throws exception
            // and returns to page if it contains invalid characters
            evalBeanUtils.validateEvalCategory(evaluation.getEvalCategory());
            
            // fix selection settings too
            fixUpSelections(evaluation);
            evaluationSetupService.saveEvaluation(evaluation, commonLogic.getCurrentUserId(), false);
        }
    }

    /**
     * Get the first evaluation that is currently being worked with in this locator,
     * if there are none then return null, otherwise return the first one
     * @return an evaluation or null if none
     */
    public EvalEvaluation getCurrentEval() {
        EvalEvaluation eval = null;
        if (delivered.size() > 0) {
            eval = delivered.values().iterator().next();
        }
        return eval;
    }

    /**
     * Takes the selection settings map and loads in into the evaluation
     * @param evaluation an evaluation
     */
    private void fixUpSelections(EvalEvaluation evaluation) {
        if (selectionSettings != null && selectionSettings.size() > 0) {
            for (Entry<String, String> selection : selectionSettings.entrySet()) {
                evaluation.setSelectionOption(selection.getKey(), selection.getValue());
            }
        }
    }

}