/******************************************************************************
 * ExternalUsers.java - created by aaronz@vt.edu
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

package org.sakaiproject.evaluation.logic.externals;

import java.util.Locale;

/**
 * This interface provides methods to get user information into the evaluation system
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface ExternalUsers {

	/**
	 * @return the internal unique user id of the current user (not username)
	 */
	public String getCurrentUserId();

	/**
	 * @param userId the internal user id (not username)
	 * @return the username or warning text if it cannot be found
	 */
	public String getUserUsername(String userId);

	/**
	 * Gets the displayable name for a user id<br/>
	 * <b>Warning</b>: This method is expensive so be careful when calling it
	 * 
	 * @param userId the internal user id (not username)
	 * @return the user display name or warning text if it cannot be found
	 */
	public String getUserDisplayName(String userId);

	
	public Locale getUserLocale(String userId);
}
