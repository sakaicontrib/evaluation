/******************************************************************************
 * Context.java - created by aaronz@vt.edu on Dec 24, 2006
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

package org.sakaiproject.evaluation.logic.model;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.model.constant.EvalConstants;


/**
 * This pea represents a context (often a site or group),
 * the context is the location of the user
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class Context {

	/**
	 * The context string which is the unique identifier for this
	 */
	public String context = "defaultContext";
	/**
	 * The displayable title of this context
	 */
	public String title = "Unknown";
	/**
	 * The type of this context (use the constants in {@link EvalExternalLogic})
	 */
	public int type = EvalConstants.CONTEXT_TYPE_UNKNOWN;


	/**
	 * Empty Constructor
	 */
	public Context() {}

	/**
	 * Standard Constructor
	 */
	public Context(String context, String title, int type) {
		this.context = context;
		this.title = title;
		this.type = type;
	}

}
