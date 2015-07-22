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
package org.sakaiproject.evaluation.logic.entity;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;

/**
 * Implementation for the entity provider for evaluations
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationEntityProviderImpl implements EvaluationEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider {

    private EvalEvaluationService evaluationService;
    public void setEvaluationService(EvalEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }

    public boolean entityExists(String id) {
        Long evaluationId;
        try {
            evaluationId = new Long(id);
            if (evaluationService.checkEvaluationExists(evaluationId)) {
                return true;
            }
        } catch (NumberFormatException e) {
            // invalid number so roll through to the false
        }
        return false;
    }

}
