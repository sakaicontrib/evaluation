/******************************************************************************
 * EvaluationEntityProviderImpl.java - created by aaronz on 23 May 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.entity;

import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;

/**
 * Implementation for the entity provider for evaluationSetupService
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
