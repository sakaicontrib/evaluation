/******************************************************************************
 * class EvalScaleParameters.java - created by kahuja@vt.edu on Mar 02, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool.viewparams;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * This is a view parameters class which defines the scale related variables 
 * that are passed from one page to another.
 * 
 * @author kahuja@vt.edu
 */
public class EvalScaleParameters extends SimpleViewParameters {	
	public Long scaleId; 
	
	public EvalScaleParameters() {
	}
	
    public EvalScaleParameters(String viewID, Long scaleId) {
        this.viewID = viewID;
        this.scaleId = scaleId;
    }
   
}
