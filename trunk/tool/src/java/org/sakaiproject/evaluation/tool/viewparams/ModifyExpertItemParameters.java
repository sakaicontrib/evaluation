/******************************************************************************
 * ModifyExpertItemParameters.java - created by rmoyer on 9/27/2010
 * 
 * Copyright (c) 2010 University of Maryland, College Park
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rick Moyer (rmoyer@umd.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.viewparams;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * EVALSYS-1026
 * Allows for passing of information needed for working with Expert Item Groups
 *
 * @author Rick Moyer (rmoyer@umd.edu)
 */

public class ModifyExpertItemParameters extends SimpleViewParameters {
	public Long categoryId;
	public Long objectiveId;
	public String type;
	public boolean isNew;
    
    public ModifyExpertItemParameters() {
    }
    
    public ModifyExpertItemParameters(String viewID, Long categoryId, Long objectiveId, String type, boolean isNew) {
        this.viewID = viewID;
        this.categoryId = categoryId;
    	this.objectiveId = objectiveId;
    	this.type = type;
    	this.isNew = isNew;
    }
}
