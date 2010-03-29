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

import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.taskstream.domain.TaskStatusStandardValues;


/**
 * Handles getting and sending of information that is external to the evaluation system,
 * includes user information, group/course/site information, and security checks<br/>
 * <b>Note:</b> Meant to be used internally in the evaluation system app only, please
 * do not call these methods outside the evaluation tool or logic layer
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalExternalLogic extends ExternalUsers, ExternalEvalGroups, ExternalEmail, ExternalSecurity, 
      ExternalContent, ExternalScheduler, ExternalTextUtils, ExternalComponents, ExternalEntities {

   public static final String ADMIN_USER_ID = "admin";
   
   /**
    * Clear ThreadLocal bindings after import.
    */
   public void clearBindings();
    
   /**
    * Get the URL to the user's tool placement on the My Worksite site
    * e.g., https://testctools.ds.itd.umich.edu/portal/site/~rwellis/page/866dd4e6-0323-43a1-807c-9522bb3167b7
    * 
    * @param userId the user
    * @return the URL
    */
   public String getMyWorkspaceUrl(String userId);
   
   /**
    * Get the title of the course section associated with the provider id.
    * 
    * @param providerId the provider identifier for a course section
    * @return the section title
    */
   public String getSectionTitle(String providerId);
   
   /**
    * Set the current session to active.
    */
   public void setSessionActive();
   
   /**
    * If the current user is in the admin group, 
    * set the current session user id to admin.
    * 
    * @parameter uid The identity of the current user
    * @return true if successful, false otherwise
    */
   public boolean setSessionUserIdAdmin(String uid);

   // EVAL USER retrieval

   /**
    * Get a populated user object for the given userId
    * 
    * @param userId the internal user id (not username)
    * @return a populated {@link EvalUser} object OR null if none found
    */
   public EvalUser getEvalUserById(String userId);

   /**
    * Get user object populated with data based on the input email,
    * guarantees to return one {@link EvalUser} object only
    * 
    * @param email the email address for a user
    * @return a populated {@link EvalUser} object OR null if none found
    */
   public EvalUser getEvalUserByEmail(String email);

   /**
    * Get user objects populated with data based on the input set of userIds,
    * guarantees to return one {@link EvalUser} object per input id and in the
    * same order as the inputs,<br/>
    * multiple inputs version of {@link #getEvalUserById(String)}
    * 
    * @param userIds an array of the internal user ids (not usernames) for users
    * @return a map of userId to {@link EvalUser} objects which match with the input ids
    */
   public Map<String, EvalUser> getEvalUsersByIds(String[] userIds);
   
   /**
    * Get  a populated user object for the given external id,
    * will not return null, always returns an object though it may be marked as invalid
    * 
    * @param eid the user's external identifier
    * @return the user
    */
   public EvalUser getEvalUserByEid(String eid);

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
    * Can be a string name: e.g. auto.ddl, etc. or one of the special SETTING_* constants in {@link EvalCommonLogic}
    * 
    * @param defaultValue a specified default value to return if this setting cannot be found,
    * <b>NOTE:</b> You can set the default value to null but you must specify the class in parens
    * @return the value of the configuration setting or the default value if none can be found
    */
   public <T> T getConfigurationSetting(String settingName, T defaultValue);
   
   /**
    * Get the title given to the evaluation tool
    * @return tool title
    */
   public String getEvalToolTitle();
   
  // TASK STATUS methods
   
   /**
    * Get the Url to the task status service from the server configuration.
    * 
    * @return the Url string or "" if not found.
    */
   public String getTaskStatusUrl();
   
   //public String getTaskStatus(String params);
   
   //public boolean updateTaskStatus(String params);
   
   public String getTaskStatusContainer(String params);
   
   public String getTaskStreamCount(String params);
   
   /**
    * Create a new stream with a particular tag
    * 
    * @param streamTag a tag used to identity the stream
    * @return the stream id/name
    */
   public String newTaskStatusStream(String streamTag);
   
   /**
    * Add entry to stream
    * 
    * @param streamUrl the Url of the stream
    * @param entryTag
    * @param status a value in the enum TaskStatusStandardValues
    * @param payload if included add to entry, may be null
    * @return the new entry's Url
    */
   public String newTaskStatusEntry(String streamUrl, String entryTag, TaskStatusStandardValues status, String payload);
   
   /**
    * Get a count of streams having stream tag, entry tag, and status since date/time
    * 
    * @param streamTag
    * @param entryTag
    * @param status
    * @param since the start of the range of date/time ending with current time, expressed at yyyy-MM-ddTHH:mm:ss
    * 			The T is a literal to separate date and time.  The rest of the letters represent the obvious values.
    * 			This is compatible with ISO 8601 and fits easily into a URL.
    * 
    
   public int getTaskStreamCount(String streamTag, String entryTag, TaskStatusStandardValues status, String since);
   */
   
 
   
   /**
    * Get an entry in a stream
    * 
    * @param streamId
    * @param entryTag
    * @param status
    * @param start
    * @param end
    
   public void getTaskStreamEntries(String streamTag, String entryTag, String status, String start, String end);
   */

}
