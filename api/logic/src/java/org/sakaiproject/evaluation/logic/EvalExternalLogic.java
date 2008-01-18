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

import java.io.Serializable;
import java.util.Map;

import org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups;
import org.sakaiproject.evaluation.logic.externals.ExternalUsers;
import org.sakaiproject.evaluation.model.EvalEvaluation;


/**
 * Handles getting and sending of information that is external to the evaluation system,
 * includes user information, group/course/site information, and security checks<br/>
 * <b>Note:</b> Meant to be used internally in the evaluation system app only, please
 * do not call these methods outside the evaluation tool or logic layer
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalExternalLogic extends ExternalUsers, ExternalEvalGroups {

	// PERMISSIONS

	/**
	 * Check if this user has super admin access in the evaluation system
	 * @param userId the internal user id (not username)
	 * @return true if the user has admin access, false otherwise
	 */
	public boolean isUserAdmin(String userId);

	/**
	 * Check if a user has a specified permission within a evalGroupId, primarily
	 * a convenience method and passthrough
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string constant
	 * @param evalGroupId the internal unique eval group ID (represents a site or group)
	 * @return true if allowed, false otherwise
	 */
	public boolean isUserAllowedInEvalGroup(String userId, String permission, String evalGroupId);


	// EMAIL
	
	/**
	 *Get the email address for the user identified by this Sakai id
	 *
	 * @param the user's Sakai id
	 * @return the User's email address
	 */
	public String getUserEmail(String userId);

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
	
	/**
	 * Get the URL to a user's My Workspace Evaluation Dashboard
	 * @param id The user's Sakai id
	 * @return the URL
	 */
	public String getMyWorkspaceUrl(String id);
	
	/**
	 * Get a map of email notification setting where key is a
	 * EvalConstants _PROPERTY and value is the current EvalSettings
	 * value unless overriden in sakai.properties.
	 * 
	 * @return property, value map
	 */
	public Map<String, String> getNotificationSettings();

	// ENTITIES

	/**
	 * @return the URL directly to the main server portal this tool is installed in
	 */
	public String getServerUrl();

	/**
	 * Get a full URL to a specific entity inside our system,
	 * if this entity has no direct URL then just provide a URL to the sakai server
	 * 
	 * @param evaluationEntity any entity inside the evaluation tool (e.g. {@link EvalEvaluation})
	 * @return a full URL to the entity (e.g. http://sakai.server:8080/access/eval-evaluation/123/)
	 */
	public String getEntityURL(Serializable evaluationEntity);

	/**
	 * Get a full URL to a specific entity inside our system using just the class type and id,
	 * if this entity has no direct URL then just provide a URL to the sakai server
	 * 
	 * @param entityPrefix an ENTITY_PREFIX constant from an entity provider
	 * @param entityId the unique id of this entity (from getId() or similar) (e.g. 123)
	 * @return a full URL to the entity (e.g. http://sakai.server:8080/access/eval-evaluation/123/)
	 */
	public String getEntityURL(String entityPrefix, String entityId);

	/**
	 * Creates a Sakai entity event for any internal entity which is registered with Sakai,
	 * does notthing if the passed in entity type is not registered
	 * 
	 * @param eventName any string representing an event name (e.g. evaluation.created)
	 * @param evaluationEntity any entity inside the evaluation tool (e.g. {@link EvalEvaluation})
	 */
	public void registerEntityEvent(String eventName, Serializable evaluationEntity);

}
