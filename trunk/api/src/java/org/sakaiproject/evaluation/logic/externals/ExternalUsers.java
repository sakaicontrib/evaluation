/**
 * $Id$
 * $URL$
 * ExternalUsers.java - evaluation - Dec 24, 2006 12:07:31 AM - azeckoski
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

import java.util.List;
import java.util.Locale;

import org.sakaiproject.evaluation.logic.model.EvalUser;


/**
 * This interface provides methods to get user information into the evaluation system,
 * @see EvalExternalLogic
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
    * @param username the login name for the user
    * @return the internal user id (not username) or null if not found
    */
   public String getUserId(String username);

   /**
    * @param userId the internal user id (not username)
    * @return the username or default text "------" if it cannot be found
    */
   public String getUserUsername(String userId);


   // EVAL USER retrieval

   /**
    * Get a populated user object for the given userId<br/>
    * Convenience method for {@link #getEvalUsersByIds(String[])}
    * 
    * @param userId the internal user id (not username)
    * @return a populated {@link EvalUser} object
    */
   public EvalUser getEvalUserById(String userId);

   /**
    * Get user object populated with data based on the input email,
    * guarantees to return one {@link EvalUser} object
    * 
    * @param email the email address for a user
    * @return a populated {@link EvalUser} object
    */
   public EvalUser getEvalUserByEmail(String email);

   /**
    * Get user objects populated with data based on the input set of userIds,
    * guarantees to return one {@link EvalUser} object per input id and in the
    * same order as the inputs,<br/>
    * multiple inputs version of {@link #getEvalUserById(String)}
    * 
    * @param userIds an array of the internal user ids (not usernames) for users
    * @return a list of {@link EvalUser} objects which match with the input ids
    */
   public List<EvalUser> getEvalUsersByIds(String[] userIds);


   // LOCALE

   /**
    * Get the locale for a user
    * 
    * @param userId the internal user id (not username)
    * @return the Locale for this user based on their preferences
    */
   public Locale getUserLocale(String userId);

}
