/**
 * $Id$
 * $URL$
 * EvalGroup.java - evaluation - Dec 24, 2006 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.model;

import java.io.Serializable;

import org.sakaiproject.evaluation.constant.EvalConstants;


/**
 * This pea represents an evalGroup (often a site or group),
 * the eval group is a collection of users
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalGroup implements Serializable {

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
    
    @Override
    public String toString() {
        return this.evalGroupId + ":" + this.title;
    }

}
