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
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.entity.TemplateEntityProvider;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.producers.PreviewEvalProducer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;

import org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles the redirection of incoming template entity URLs to the proper views with the proper view params added
 * 
 * @author Aaron Zeckoski (azeckoski @ unicon.net)
 */
public class TemplateVPInferrer implements EntityViewParamsInferrer {

	private EvalAuthoringService authoringService;
	
	private EvalCommonLogic commonLogic;

	public void setAuthoringService(EvalAuthoringService authoringService) {
		this.authoringService = authoringService;
	}

	public void setCommonLogic(EvalCommonLogic commonLogic) {
		this.commonLogic = commonLogic;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer#getHandledPrefixes()
	 */
	public String[] getHandledPrefixes() {
		return new String[] { TemplateEntityProvider.ENTITY_PREFIX };
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.rsf.entitybroker.EntityViewParamsInferrer#inferDefaultViewParameters(java.lang.String)
	 */
	public ViewParameters inferDefaultViewParameters(String reference) {
		EntityReference ep = new EntityReference(reference);
		Long templateId = new Long(ep.getId());
		EvalTemplate template = authoringService.getTemplateById(templateId); 
		if (EvalConstants.SHARING_PUBLIC.equals(template.getSharing()) ||
				Boolean.TRUE.equals(template.getExpert())) {
		} else {
			String userId = commonLogic.getCurrentUserId();
			if (!authoringService.canModifyTemplate(userId, templateId)) {
				throw new SecurityException("You are not a maintainer on this template.");
			}
		}
		EvalViewParameters vp = new EvalViewParameters(PreviewEvalProducer.VIEW_ID, null, templateId);
		vp.external = true;
		return vp;
	}

}
