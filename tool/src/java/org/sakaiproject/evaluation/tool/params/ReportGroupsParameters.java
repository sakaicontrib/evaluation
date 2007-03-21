/******************************************************************************
 * ReportGroupsParameters.java - created by aaronz on 20 Mar 2007
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

package org.sakaiproject.evaluation.tool.params;

import java.util.HashMap;
import java.util.Map;

/**
 * View params which pass chosen groups to the reports page
 * 
 * @author Will Humphries (aaronz@vt.edu)
 */
public class ReportGroupsParameters extends TemplateViewParameters {

	public Map groupIds = new HashMap();

	public ReportGroupsParameters() {}

	public ReportGroupsParameters(String viewId, Long templateId){
		this.viewID = viewID;
		this.templateId=templateId;
	}
	
	public ReportGroupsParameters(String viewID, Long templateId, Map groupIds) {
		this.viewID = viewID;
		this.templateId = templateId;
		this.groupIds = groupIds;
	}
	
}
