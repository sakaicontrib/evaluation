/******************************************************************************
 * CSVReportViewParams.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * 
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class CSVReportViewParams extends SimpleViewParameters {

	public Long templateId; 
	public Long evalId;

	public CSVReportViewParams() {}

	public CSVReportViewParams(String viewID, Long templateId, Long evalId) {
		this.viewID = viewID;
		this.templateId = templateId;
		this.evalId=evalId;
	}

	public String getParseSpec() {
		// include a comma delimited list of the public properties in this class
		return super.getParseSpec() + ",templateId,evalId";
	}
}
