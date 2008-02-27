/******************************************************************************
 * PreviewEvalParameters.java - created by aaronz on 31 May 2007
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

package org.sakaiproject.evaluation.tool.viewparams;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * Allows for passing of information needed for previewing templates or evaluationSetupService
 * (rewrite of original)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalViewParameters extends SimpleViewParameters {

	public Long evaluationId;
	public Long templateId; 
	public String evalGroupId;

	public EvalViewParameters() { }

	public EvalViewParameters(String viewID, Long evaluationId, Long templateId) {
		this.viewID = viewID;
		this.evaluationId = evaluationId;	
		this.templateId = templateId;
	}

	public EvalViewParameters(String viewID, Long evaluationId, Long templateId, String evalGroupId) {
		this.viewID = viewID;
		this.evaluationId = evaluationId;	
		this.templateId = templateId;
		this.evalGroupId = evalGroupId;
	}

}
