/******************************************************************************
 * class EvalViewParameters.java - created by fengr@vt.edu on Oct 23, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.viewparams;

/**
 * This is a view parameters class which defines the variables that are passed
 * from one page to another, for a simple view which is centered on a particular
 * EvalTemplate object within the evaluation system.
 */
public class TemplateViewParameters extends BaseViewParameters {

	public Long templateId;
	public Long templateItemId;

	public TemplateViewParameters() {
	}

	public TemplateViewParameters(String viewID, Long templateId) {
		this.viewID = viewID;
		this.templateId = templateId;
	}
	
	public TemplateViewParameters(String viewID, Long templateId, Long templateItemId) {
		this.viewID = viewID;
		this.templateId = templateId;
		this.templateItemId = templateItemId;
	}

}
