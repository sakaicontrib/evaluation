/******************************************************************************
 * EvalCategoryVPInferrer.java - created by aaronz on 29 May 2007
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
import org.sakaiproject.evaluation.logic.entity.EvalCategoryEntityProvider;
import org.sakaiproject.evaluation.tool.producers.ShowEvalCategoryProducer;
import org.sakaiproject.evaluation.tool.viewparams.EvalCategoryViewParameters;

import uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * Handles the redirection of incoming eval category entity URLs to the proper views with the proper view params added
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalCategoryVPInferrer implements EntityViewParamsInferrer {

	/* (non-Javadoc)
	 * @see uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer#getHandledPrefixes()
	 */
	public String[] getHandledPrefixes() {
		return new String[] { EvalCategoryEntityProvider.ENTITY_PREFIX };
	}

	/* (non-Javadoc)
	 * @see uk.ac.cam.caret.sakai.rsf.entitybroker.EntityViewParamsInferrer#inferDefaultViewParameters(java.lang.String)
	 */
	public ViewParameters inferDefaultViewParameters(String reference) {
		IdEntityReference ep = new IdEntityReference(reference);
		String category = ep.id;
		// TODO - should this possibly authenticate first?
		EvalCategoryViewParameters vp = new EvalCategoryViewParameters(ShowEvalCategoryProducer.VIEW_ID, category);
		vp.external = true;
		return vp;
	}

}
