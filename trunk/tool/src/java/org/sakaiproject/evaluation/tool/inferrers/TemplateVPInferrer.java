/******************************************************************************
 * TemplateVPInferrer.java - created by aaronz on 29 May 2007
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

package org.sakaiproject.evaluation.tool.inferrers;

import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalAuthoringService;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.entity.TemplateEntityProvider;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.producers.PreviewEvalProducer;
import org.sakaiproject.evaluation.tool.viewparams.EvalViewParameters;

import uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles the redirection of incoming template entity URLs to the proper views with the proper view params added
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
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
	 * @see uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer#getHandledPrefixes()
	 */
	public String[] getHandledPrefixes() {
		return new String[] { TemplateEntityProvider.ENTITY_PREFIX };
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer#inferDefaultViewParameters(java.lang.String)
	 */
	public ViewParameters inferDefaultViewParameters(String reference) {
		IdEntityReference ep = new IdEntityReference(reference);
		Long templateId = new Long(ep.id);
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
