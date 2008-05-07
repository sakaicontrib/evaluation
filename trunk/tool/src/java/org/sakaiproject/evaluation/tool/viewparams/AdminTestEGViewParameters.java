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

import org.sakaiproject.evaluation.providers.EvalGroupsProvider;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * View params for use with the {@link EvalGroupsProvider} test page
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class AdminTestEGViewParameters extends SimpleViewParameters {

	public String username;
	public String evalGroupId;

    public AdminTestEGViewParameters() {}

	public AdminTestEGViewParameters(String viewID, String username, String evalGroupId) {
        this.viewID = viewID;
        this.username = username;
        this.evalGroupId = evalGroupId;
    }
	
}
