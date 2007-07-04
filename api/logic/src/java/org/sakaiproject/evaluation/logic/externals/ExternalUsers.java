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
	 * Get the user id (not username) of the current user if there is one,
	 * if not then return an anonymous user id generated with a timestamp and prefix
	 * 
	 * @return the internal unique user id of the current user (not username) or anon id
	 */
	public String getCurrentUserId();

	/**
	 * Check if a user is anonymous or identified
	 * @param userId the internal user id (not username)
	 * @return true if we know who this user is, false otherwise
	 */
	public boolean isUserAnonymous(String userId);

	/**
	 * @param userId the internal user id (not username)
	 * @return the username or default text "------" if it cannot be found
	 */
	public String getUserUsername(String userId);
    
    /**
     * @param username the login name for the user
     * @return the internal user id (not username) or null if not found
     */
    public String getUserId(String username);

	/**
	 * Gets the displayable name for a user id<br/>
	 * <b>Warning</b>: This method is expensive so be careful when calling it
	 * 
	 * @param userId the internal user id (not username)
	 * @return the user display name or default text "--------" if it cannot be found
	 */
	public String getUserDisplayName(String userId);

	
	/**
	 * Get the locale for a user
	 * 
	 * @param userId the internal user id (not username)
	 * @return the Locale for this user based on their preferences
	 */
	public Locale getUserLocale(String userId);

}
