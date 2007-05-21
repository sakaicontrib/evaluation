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

/**
 * View params which pass chosen groups to the view reports page
 * when coming via bread crumbs from essay responses page. 
 * For going staightforward from choose report groups to view reports 
 * page, the report bean takes care of it. 
 * 
 * @author Will Humphries (whumphri@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 */
public class ReportParameters extends TemplateViewParameters {

	public String[] groupIds;

	public ReportParameters() {}

	public ReportParameters(String viewId, Long templateId){
		super.viewID = viewID;
		super.templateId=templateId;
	}
	
	public ReportParameters(String viewID, Long templateId, String[] groupIds) {
		super.viewID = viewID;
		super.templateId = templateId;
		this.groupIds = groupIds;
	}
	
}
