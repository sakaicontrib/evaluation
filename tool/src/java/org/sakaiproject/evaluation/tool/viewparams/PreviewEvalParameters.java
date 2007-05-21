/******************************************************************************
 * class PreviewEvalParameters.java - created by fengr@vt.edu on Nov 30, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.viewparams;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class PreviewEvalParameters extends SimpleViewParameters {
	public Long evaluationId;
	public Long templateId; 
	public String context;
	public String originalPage;
	
	public PreviewEvalParameters() {
	}

	public PreviewEvalParameters(String viewID, Long evaluationId, Long templateId, String context, String originalPage) {
		this.viewID = viewID;
		this.evaluationId = evaluationId;	
		this.templateId = templateId;
		this.context = context;
		this.originalPage = originalPage;
	}

}
