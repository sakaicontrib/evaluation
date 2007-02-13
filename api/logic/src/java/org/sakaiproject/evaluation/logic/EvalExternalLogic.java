/******************************************************************************
 * EvalExternal.java - created by aaronz@vt.edu on Dec 24, 2006
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

package org.sakaiproject.evaluation.logic;

import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.logic.model.Context;


/**
 * Handles getting and sending of information that is external to the evaluation system,
 * includes user information, group/course/site information, and security checks<br/>
 * <b>Note:</b> Meant to be used internally in the evaluation system app only, please
 * do not call these methods outside the evaluation tool or logic layer
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalExternalLogic {

	// USERS

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

	/**
	 * Check if this user has super admin access in the evaluation system
	 * @param userId the internal user id (not username)
	 * @return true if the user has admin access, false otherwise
	 */
	public boolean isUserAdmin(String userId);


	// PERMISSIONS

	/**
	 * Check if a user has a specified permission within a context, primarily
	 * a convenience method and passthrough
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string constant
	 * @param context the internal context (represents a site or group)
	 * @return true if allowed, false otherwise
	 */
	public boolean isUserAllowedInContext(String userId, String permission, String context);


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
	 * @return a List of Strings which represent the user Ids of all users in the site with that permission
	 */
	public Set getUserIdsForContext(String context, String permission);

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

	// EMAIL

	/**
	 * Send emails to a set of users (can send to a single user
	 * by specifying an array with one item only), gets the email addresses
	 * for the users ids
	 * 
	 * @param from the email address this email appears to come from
	 * @param toUserIds the userIds this message should be sent to
	 * @param subject the message subject
	 * @param message the message to send
	 */
	public void sendEmails(String from, String[] toUserIds, String subject, String message);

}
