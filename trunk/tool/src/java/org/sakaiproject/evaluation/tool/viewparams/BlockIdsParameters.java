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
 * Antranig Basman (antranig@caret.cam.ac.uk)
 * Rui Feng (fengr@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.viewparams;

public class BlockIdsParameters extends TemplateViewParameters {
	public String templateItemIds;
	public String templateItemId;

	public BlockIdsParameters() {
	}
	
	public BlockIdsParameters(String viewID, Long templateId) {
		this.viewID = viewID;
		this.templateId = templateId;
	}

	public BlockIdsParameters(String viewID, Long templateId, String templateItemIds) {
		this.viewID = viewID;
		this.templateId = templateId;
		this.templateItemIds = templateItemIds;
	}
	
	public BlockIdsParameters(String viewID, Long templateId, String templateItemIds, String templateItemId) {
		this.viewID = viewID;
		this.templateId = templateId;
		this.templateItemId = templateItemId;
		this.templateItemIds = templateItemIds;
	}

}
