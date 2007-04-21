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
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 */
public class CSVReportViewParams extends SimpleViewParameters {

	public Long templateId; 
	public Long evalId;

	// See the comment in EssayResponseParams.java
	public String[] groupIds;

	public CSVReportViewParams() {}

	public CSVReportViewParams(String viewID, Long templateId, Long evalId, String[] groupIds) {
		this.viewID = viewID;
		this.templateId = templateId;
		this.evalId=evalId;
		this.groupIds = groupIds;
	}

	public String getParseSpec() {
		// include a comma delimited list of the public properties in this class
		return super.getParseSpec() + ",templateId,evalId,groupIds";
	}
}
