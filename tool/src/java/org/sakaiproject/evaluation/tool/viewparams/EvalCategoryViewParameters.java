/******************************************************************************
 * EvalCategoryViewParameters.java - created by aaronz on 29 May 2007
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
 * used for passing a category to the show eval categories view
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalCategoryViewParameters extends SimpleViewParameters {

	/**
	 * An evaluation category - should match with an evalCategory from an {@link EvalEvaluation} object
	 */
	public String evalCategory;

	public EvalCategoryViewParameters() {}

	public EvalCategoryViewParameters(String viewID, String evalCategory) {
		this.viewID = viewID;
		this.evalCategory = evalCategory;
	}

}
