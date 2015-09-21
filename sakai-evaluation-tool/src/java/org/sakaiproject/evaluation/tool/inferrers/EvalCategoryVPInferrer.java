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
package org.sakaiproject.evaluation.tool.inferrers;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.evaluation.logic.entity.EvalCategoryEntityProvider;
import org.sakaiproject.evaluation.tool.producers.ShowEvalCategoryProducer;
import org.sakaiproject.evaluation.tool.viewparams.EvalCategoryViewParameters;

import org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles the redirection of incoming eval category entity URLs to the proper views with the proper view params added
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net)
 */
public class EvalCategoryVPInferrer implements EntityViewParamsInferrer {

	/* (non-Javadoc)
	 * @see org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer#getHandledPrefixes()
	 */
	public String[] getHandledPrefixes() {
		return new String[] { EvalCategoryEntityProvider.ENTITY_PREFIX };
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer#inferDefaultViewParameters(java.lang.String)
	 */
	public ViewParameters inferDefaultViewParameters(String reference) {
		String category = EntityReference.getIdFromRef(reference);
		// NOTE - should this possibly authenticate first?
		EvalCategoryViewParameters vp = new EvalCategoryViewParameters(ShowEvalCategoryProducer.VIEW_ID, category);
		vp.external = true;
		return vp;
	}

}
