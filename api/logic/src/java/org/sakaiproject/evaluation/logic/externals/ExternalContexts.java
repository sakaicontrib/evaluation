/******************************************************************************
 * ExternalContexts.java - created by aaronz@vt.edu
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

import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.logic.model.Context;

/**
 * This interface provides methods to get context (user collections) information
 * into the evaluation system and permissions related to those contexts
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface ExternalContexts {

	// CONTEXTS

	/**
	 * @return the current group context for the current session
	 * (represents the current location of the user in the system)
	 */
	public String getCurrentContext();

	/**
	 * Construct a Context object based on the context string id
	 * 
	 * @param context the internal context (represents a site or group)
	 * @return a Context object or null if not found
	 */
	public Context makeContextObject(String context);

	/**
	 * Get the title associated with a context
	 * 
	 * @param context the internal context (represents a site or group)
	 * @return the displayable title or warning text if it cannot be found
	 */
	public String getDisplayTitle(String context);


	// ENROLLMENTS

	/**
	 * Get a list of all user ids that have a specific permission in a context
	 * 
	 * @param context a context (group/site)
	 * @param permission a permission string constant
	 * @return a Set of Strings which represent the user Ids of all users in the site with that permission
	 */
	public Set getUserIdsForContext(String context, String permission);

	/**
	 * Get a count of all user ids that have a specific permission in a context
	 * 
	 * @param context a context (group/site)
	 * @param permission a permission string constant
	 * @return a count of the users
	 */
	public int countUserIdsForContext(String context, String permission);

	/**
	 * Get a list of all contexts that a user has a specific permission in
	 * (use {@link #countContextsForUser(String, String)} if you just need the number)
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string constant
	 * @return a List of {@link Context} objects
	 */
	public List getContextsForUser(String userId, String permission);

	/**
	 * Get a count of all contexts that a user has a specific permission in
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string constant
	 * @return the count of the contexts that the user has a permission in
	 */
	public int countContextsForUser(String userId, String permission);

}
