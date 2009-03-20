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


}
