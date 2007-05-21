/******************************************************************************
 * PreviewItemViewParameters.java - created by aaronz on 21 Mar 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
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

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * View params for passing item/templateItem ids to allow previewing of single items,
 * only one of these should be populated, any page that uses this VP should know how to handle
 * both types
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class PreviewItemViewParameters extends SimpleViewParameters {

	public Long itemId;
	public Long templateItemId;

	public PreviewItemViewParameters() { }

	public PreviewItemViewParameters(String viewID, Long itemId, Long templateItemId) {
		this.viewID = viewID;
		this.itemId = itemId;
		this.templateItemId = templateItemId;
	}

}
