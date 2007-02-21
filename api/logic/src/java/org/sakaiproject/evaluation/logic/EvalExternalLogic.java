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

import org.sakaiproject.evaluation.logic.externals.ExternalContexts;
import org.sakaiproject.evaluation.logic.externals.ExternalUsers;


/**
 * Handles getting and sending of information that is external to the evaluation system,
 * includes user information, group/course/site information, and security checks<br/>
 * <b>Note:</b> Meant to be used internally in the evaluation system app only, please
 * do not call these methods outside the evaluation tool or logic layer
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalExternalLogic extends ExternalUsers, ExternalContexts {

	// PERMISSIONS

	/**
	 * Check if this user has super admin access in the evaluation system
	 * @param userId the internal user id (not username)
	 * @return true if the user has admin access, false otherwise
	 */
	public boolean isUserAdmin(String userId);

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

	// URLS

	/**
	 * @return the URL directly to the main server portal this tool is installed in
	 */
	public String getServerUrl();

	/**
	 * @return the URL directly to the tool itself (the root of the tool)
	 */
	public String getToolUrl();

}
