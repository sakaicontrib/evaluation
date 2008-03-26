/**
 * $Id$
 * $URL$
 * EvalExternalLogic.java - evaluation - Dec 24, 2006 12:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.externals;

import java.io.Serializable;

import org.sakaiproject.evaluation.model.EvalEvaluation;


/**
 * Handles getting and sending of information that is external to the evaluation system,
 * includes user information, group/course/site information, and security checks<br/>
 * <b>Note:</b> Meant to be used internally in the evaluation system app only, please
 * do not call these methods outside the evaluation tool or logic layer
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalExternalLogic extends ExternalUsers, ExternalEvalGroups, ExternalContent, ExternalScheduler {

   public static final String ADMIN_USER_ID = "admin";

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
    * Send emails to a set of users (can send to a single user
    * by specifying an array with one item only), gets the email addresses
    * for the users ids
    * 
    * @param from the email address this email appears to come from
    * @param toUserIds the userIds this message should be sent to
    * @param subject the message subject
    * @param message the message to send
    * @param deferExceptions if true, then exceptions are logged and then thrown after sending as many emails as possible,
    * if false then exceptions are thrown immediately
    * @return an array of email addresses that this message was sent to
    */
   public String[] sendEmailsToUsers(String from, String[] toUserIds, String subject, String message, boolean deferExceptions);

   /**
    * Send emails to a set of email addresses (can send to a single address
    * by specifying an array with one item only)
    * NOTE: Use {@link #sendEmailsToUsers(String, String[], String, String, boolean)} if you know who the users are
    * 
    * @param from the email address this email appears to come from
    * @param to the email address(es) this message should be sent to
    * @param subject the message subject
    * @param message the message to send
    * @param deferExceptions if true, then exceptions are logged and then thrown after sending as many emails as possible,
    * if false then exceptions are thrown immediately
    * @return an array of email addresses that this message was sent to
    * @throws 
    */
   public String[] sendEmailsToAddresses(String from, String[] to, String subject, String message, boolean deferExceptions);


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
    * does nothing if the passed in entity type is not registered
    * 
    * @param eventName any string representing an event name (e.g. evaluation.created)
    * @param evaluationEntity any entity inside the evaluation tool (e.g. {@link EvalEvaluation})
    */
   public void registerEntityEvent(String eventName, Serializable evaluationEntity);

   /**
    * Creates a Sakai entity event for any internal entity which is registered with Sakai,
    * does nothing if the passed in entity class is not registered
    * 
    * @param eventName any string representing an event name (e.g. evaluation.created)
    * @param entityClass class type of the entity which this event pertains to
    * @param entityId unique id for the entity which this event pertains to
    */
   public void registerEntityEvent(String eventName, Class<? extends Serializable> entityClass, String entityId);


   // SERVER

   /**
    * String type: gets the printable name of this server 
    */
   public static String SETTING_SERVER_NAME = "server.name";
   /**
    * String type: gets the unique id of this server (safe for clustering if used)
    */
   public static String SETTING_SERVER_ID = "server.cluster.id";
   /**
    * String type: gets the URL to this server
    */
   public static String SETTING_SERVER_URL = "server.main.URL";
   /**
    * String type: gets the URL to the portal on this server (or just returns the server URL if no portal in use)
    */
   public static String SETTING_PORTAL_URL = "server.portal.URL";
   /**
    * Boolean type: if true then there will be data preloads and DDL creation,
    * if false then data preloads are disabled (and will cause exceptions if preload data is missing)
    */
   public static String SETTING_AUTO_DDL = "auto.ddl";
   /**
    * Boolean type: if true then quartz imports are enabled, false (default) is disabled
    */
   public static String SETTING_EVAL_QUARTZ_IMPORT = "eval.qrtzImport";

   /**
    * @param settingName the name of the setting to retrieve,
    * Can be a string name: e.g. auto.ddl, etc. or one of the special SETTING_* constants in this file
    * 
    * @param defaultValue a specified default value to return if this setting cannot be found,
    * <b>NOTE:</b> You can set the default value to null but you must specify the class in parens
    * @return the value of the configuration setting or the default value if none can be found
    */
   public <T> T getConfigurationSetting(String settingName, T defaultValue);


   // TEXT

   /**
    * Cleans up the users submitted strings to protect us from XSS
    * 
    * @param userSubmittedString any string from the user which could be dangerous
    * @return a cleaned up string which is now safe
    */
   public String cleanupUserStrings(String userSubmittedString);

}
