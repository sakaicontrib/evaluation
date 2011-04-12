/******************************************************************************
 * DownloadReportViewParams.java - created by aaronz@vt.edu
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

package org.sakaiproject.evaluation.tool.viewparams;

/**
 * This is meant to serve as a base for ViewParameters of different download
 * types that may require their own custom parameters. Ex. CSV, Excel, PDF etc.
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Steven Githens
 */
public class DownloadReportViewParams extends BaseViewParameters {

	public Long templateId; 
	public Long evalId;
	public String filename;

	// See the comment in EssayResponseParams.java
	public String[] groupIds;

	public DownloadReportViewParams() {}

	public DownloadReportViewParams(String viewID, Long templateId, Long evalId, String[] groupIds, String filename) {
		this.viewID = viewID;
		this.templateId = templateId;
		this.evalId = evalId;
		this.groupIds = groupIds;
		this.filename = filename;
	}

	public String getParseSpec() {
		// include a comma delimited list of the public properties in this class
		return super.getParseSpec() + ",templateId,evalId,groupIds,filename";
	}
}
