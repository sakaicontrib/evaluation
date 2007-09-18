/******************************************************************************
 * class EvalTakeViewParameters.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.viewparams;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * This is a view parameters class which defines the variables that are
 * passed to the take evaluation page (also preview evaluation)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalTakeViewParameters extends SimpleViewParameters {

	public Long evaluationId; 
	public String evalGroupId;
	public Long responseId;
	public String evalCategory;

	public EvalTakeViewParameters() {
	}

	public EvalTakeViewParameters(String viewID, Long evaluationId) {
		this.viewID = viewID;
		this.evaluationId = evaluationId;
	}

	public EvalTakeViewParameters(String viewID, Long evaluationId, String evalGroupId) {
		this.viewID = viewID;
		this.evaluationId = evaluationId;
		this.evalGroupId = evalGroupId;
	}

	public EvalTakeViewParameters(String viewID, Long evaluationId, String evalGroupId, Long responseId) {
		this.viewID = viewID;
		this.evaluationId = evaluationId;
		this.evalGroupId = evalGroupId;
		this.responseId = responseId;
	}

	public EvalTakeViewParameters(String viewID, Long evaluationId, String evalGroupId, String evalCategory) {
		this.viewID = viewID;
		this.evaluationId = evaluationId;
		this.evalGroupId = evalGroupId;
		this.evalCategory = evalCategory;
	}

	public EvalTakeViewParameters(String viewID, Long evaluationId, Long responseId, String evalGroupId, String evalCategory) {
		this.viewID = viewID;
		this.evaluationId = evaluationId;
		this.evalGroupId = evalGroupId;
		this.responseId = responseId;
		this.evalCategory = evalCategory;
	}

}
