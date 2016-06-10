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

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * Implementation for the entity provider for evaluations
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalReportsEntityProviderImpl implements EvalReportsEntityProvider, CoreEntityProvider, Describeable, AutoRegisterEntityProvider, ActionsExecutable, Outputable {

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

	@EntityCustomAction(action = "CSVReport", viewKey = EntityView.VIEW_LIST) 
    public String exportCSVReport(EntityView view, Map<String,Object> params) {
		Long evaluationId = null;
		try {
			evaluationId = Long.parseLong(view.getPathSegment(2));
		} 
		catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					"Invalid evaluationId. Must include evaluationId and an (optional) comma separated list of groupIds in the path ("
							+ view
							+ "): e.g. /direct/"+ENTITY_PREFIX+"/{evaluationId}/{groupIds}");
		}

		if (evaluationId == null) {
			// format of the view should be in a standard assignment reference
			throw new IllegalArgumentException(
					"Must include evaluationId and an (optional) comma separated list of groupIds in the path ("
							+ view
							+ "): e.g. /direct/"+ENTITY_PREFIX+"/{evaluationId}/{groupIds}");
		}
					
		//This can be null, if not set it will get all groups
		String groupIds = view.getPathSegment(3);
		EvalEvaluation evaluation = evaluationService.getEvaluationById(evaluationId);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		evaluationService.exportReport(evaluation,groupIds,outputStream,EvalEvaluationService.CSV_RESULTS_REPORT);
		return outputStream.toString();
	}

	@Override
	public String[] getHandledOutputFormats() {
		// TODO Auto-generated method stub
        return new String[] {Formats.JSON};
	}
	
}
