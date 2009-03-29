/******************************************************************************
 * EvalCategoryViewParameters.java - created by aaronz on 29 May 2007
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
 * used for passing a category to the show eval categories view
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalCategoryViewParameters extends BaseViewParameters {

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
