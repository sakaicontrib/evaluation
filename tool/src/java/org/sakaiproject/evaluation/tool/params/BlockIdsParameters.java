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
package org.sakaiproject.evaluation.tool.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;


public class BlockIdsParameters extends SimpleViewParameters {
	public Long templateId; 
	public String templateItemIds;

	
	public BlockIdsParameters() {
	}
	
	public BlockIdsParameters(String viewID, Long templateId) {
		this.viewID = viewID;
		this.templateId = templateId;


	}
/*
	public BlockIdsParameters(String viewID, Long templateId, String templateItemIds) {
		this.viewID = viewID;
		this.templateId = templateId;
		this.templateItemIds = templateItemIds;

	}
*/
	public String getParseSpec() {
		// include a comma delimited list of the public properties in this class
		return super.getParseSpec() + ",templateId,templateItemIds";
	}
}
