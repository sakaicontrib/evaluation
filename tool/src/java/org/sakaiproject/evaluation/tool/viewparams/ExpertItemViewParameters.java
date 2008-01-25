/******************************************************************************
 * ExpertItemViewParameters.java - created by aaronz on 8 Mar 2007
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
 * View params for use with the expert items views
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ExpertItemViewParameters extends TemplateItemViewParameters {

	public Long categoryId;
	public Long objectiveId;

	public ExpertItemViewParameters() {}

	public ExpertItemViewParameters(String viewID, Long templateId, Long categoryId, Long objectiveId) {
		this.viewID = viewID;
		this.templateId = templateId;
		this.categoryId = categoryId;
		this.objectiveId = objectiveId;
	}
	
}
