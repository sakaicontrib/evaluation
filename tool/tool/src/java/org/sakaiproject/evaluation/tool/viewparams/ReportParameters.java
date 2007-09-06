/******************************************************************************
 * ReportParameters.java - created by whumphri on 20 Mar 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Will Humphries (whumphri@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.viewparams;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * Pass the chosen groups to the view reports page when coming via bread crumbs from essay responses page. 
 * For going staightforward from choose report groups to view reports page, the report bean takes care of it. 
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ReportParameters extends SimpleViewParameters {

	// Removed extends TemplateViewParameters
	// this should never have extended the TemplateViewParameters since it is checked on by an interceptor,
	// furthermore, this used templateId when it was actually passing an evaluation id which is just wrong,
	// removed former authors for doing this so incorrectly -AZ

	public Long evaluationId;
	public String[] groupIds;

	public ReportParameters() {}

	public ReportParameters(String viewID, Long evaluationId){
		this.viewID = viewID;
		this.evaluationId = evaluationId;
	}

	public ReportParameters(String viewID, Long evaluationId, String[] groupIds) {
		this.viewID = viewID;
		this.evaluationId = evaluationId;
		this.groupIds = groupIds;
	}

}
