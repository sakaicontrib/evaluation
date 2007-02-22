/******************************************************************************
 * EvalGroup.java - created by aaronz@vt.edu on Dec 24, 2006
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

import org.sakaiproject.evaluation.model.constant.EvalConstants;


/**
 * This pea represents an evalGroup (often a site or group),
 * the eval group is a collection of users
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalGroup {

	/**
	 * The evalGroup ID string which is the unique identifier for this
	 */
	public String evalGroupId;
	/**
	 * The displayable title of this evalGroup
	 */
	public String title = "Unknown";
	/**
	 * The type of this evalGroup (use the GROUP_TYPE constants in {@link EvalConstants})
	 */
	public String type = EvalConstants.GROUP_TYPE_UNKNOWN;


	/**
	 * Empty Constructor
	 */
	public EvalGroup() {}

	/**
	 * Full Constructor
	 * 
	 * @param evalGroupId unique id for this evalGroup
	 * @param title the display title of this group
	 * @param type the type of this group
	 * (use the GROUP_TYPE constants in {@link EvalConstants})
	 */
	public EvalGroup(String evalGroupId, String title, String type) {
		this.evalGroupId = evalGroupId;
		this.title = title;
		this.type = type;
	}

}
