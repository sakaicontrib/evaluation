/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.logic.externals;

import java.util.List;
import java.util.Locale;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;


/**
 * This interface provides methods to get user information into the evaluation system,
 * @see EvalCommonLogic
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
     * This allows us to get a super admin userID for special permissions checks or admin operations
     * @return the userId of the super admin
     */
    public String getAdminUserId();
    
    /**
     * Gets the list of users (in the form of EvalUser objects) that have admin rights for the !admin worksite.
     * 
     * @return a list of EvalUser objects representing the sakai admins
     */
    public List<EvalUser> getSakaiAdmins();

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


    // LOCALE

    /**
     * Get the locale for a user
     * 
     * @param userId the internal user id (not username)
     * @return the Locale for this user based on their preferences
     */
    public Locale getUserLocale(String userId);

	/**
	 * Get the URL to go to a the evaluation tool in a user's MyWorkspace.
	 * 
	 * @param userId
	 * @return
	 */
	public String getMyWorkspaceDashboard(String userId);

}
