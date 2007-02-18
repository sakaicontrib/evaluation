/******************************************************************************
 * class EvalViewParameters.java - created by fengr@vt.edu on Oct 23, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * This is a view parameters class which defines the variables that are
 * passed from one page to another
 * @author Sakai App Builder -AZ
 */
public class EvalViewParameters extends SimpleViewParameters {	
	public Long templateId; 
	
	public EvalViewParameters() {
	}
   /** NB - only two views left (ModifyEmailProducer and PreviewEmailProducer) that
    * are still relying on the "originalPage" system, use this constructor by
    * default - AMB. */
    public EvalViewParameters(String viewID, Long templateId) {
        this.viewID = viewID;
        this.templateId = templateId;
    }
   
}
