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
public class EssayResponseParams extends SimpleViewParameters {

	public Long evalId; 
	public Long itemId;

	public EssayResponseParams() {}

	public EssayResponseParams(String viewID, Long evalId) {
		this.viewID = viewID;
		this.evalId = evalId;
		this.itemId = null;
	}

	
	public EssayResponseParams(String viewID, Long evalId, Long itemId) {
		this.viewID = viewID;
		this.evalId = evalId;
		this.itemId = itemId;
	}
	
	public String getParseSpec() {
		// include a comma delimited list of the public properties in this class
		return super.getParseSpec() + ",evalId,itemId";
	}
}
