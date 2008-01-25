/******************************************************************************
 * ChooseItemViewParameters.java - created by aaronz on 12 Mar 2007
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
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
 * View params for passing template id and search information for choosing items to insert into templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ChooseItemViewParameters extends TemplateViewParameters {

	public String searchString;

	public ChooseItemViewParameters() { }

	public ChooseItemViewParameters(String viewID, Long templateId, String searchString) {
		this.viewID = viewID;
		this.templateId = templateId;
		this.searchString = searchString;
	}

}
