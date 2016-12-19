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
package org.sakaiproject.evaluation.logic;

import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.externals.ExternalContent;
import org.sakaiproject.evaluation.logic.externals.ExternalEntities;
import org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups;
import org.sakaiproject.evaluation.logic.externals.ExternalScheduler;
import org.sakaiproject.evaluation.logic.externals.ExternalSecurity;
import org.sakaiproject.evaluation.logic.externals.ExternalTextUtils;
import org.sakaiproject.evaluation.logic.externals.ExternalUsers;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.model.EvalAdmin;
import org.sakaiproject.evaluation.providers.EvalGroupsProvider;
import org.sakaiproject.evaluation.model.EvalAssignGroup;


/**
 * Handles all basic internal operations for the evaluation system,
 * includes user information, group/course/site information, and security checks<br/>
 * <b>Note:</b> Meant to be used internally in the evaluation system app only, please
 * do not call these methods outside the evaluation tool or logic layer
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalCommonLogic extends ExternalUsers, ExternalEvalGroups, ExternalEntities, 
    ExternalSecurity, ExternalContent, ExternalScheduler, ExternalTextUtils {

    // Users

    /**
     * Get a populated user object for the given userId,
     * will not return null, always returns an object though it may be marked as invalid
     * 
     * @param userId the internal user id (not username)
     * @return a populated {@link EvalUser} object (may be marked as invalid)
     */
    public EvalUser getEvalUserById(String userId);

    /**
     * Get user object populated with data based on the input email,
     * will not return null, always returns an object though it may be marked as invalid,
     * guarantees to return one {@link EvalUser} object only
     * 
     * @param email the email address for a user
     * @return a populated {@link EvalUser} object (may be marked as invalid)
     */
    public EvalUser getEvalUserByEmail(String email);

    /**
     * Get user objects populated with data based on the input set of userIds,
     * guarantees to return one {@link EvalUser} object per input id and in the
     * same order as the inputs,<br/>
     * Note that some users may be marked as invalid users<br/>
     * multiple inputs version of {@link #getEvalUserById(String)}
     * 
     * @param userIds an array of the internal user ids (not usernames) for users
     * @return a list of {@link EvalUser} objects which match with the input userIds and are in the same order
     */
    public List<EvalUser> getEvalUsersByIds(String[] userIds);

    // GROUPS

    /**
     * Get the title associated with an evalGroup
     * 
     * @param evalGroupId the internal unique ID for an evalGroup
     * @return the displayable title or default text "--------" if it cannot be found
     */
    public String getDisplayTitle(String evalGroupId);

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
     * @param deliveryOption a delivery option constant from the eval constants,
     * determines what to do with the emails, null is the default: {@link EvalConstants#EMAIL_DELIVERY_SEND}
     * @return an array of email addresses that this message was sent to
     */
    public String[] sendEmailsToUsers(String from, String[] toUserIds, String subject, String message, boolean deferExceptions, String deliveryOption);

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
     * @param deliveryOption a delivery option constant from the eval constants,
     * determines what to do with the emails, null is the default: {@link EvalConstants#EMAIL_DELIVERY_SEND}
     * @return an array of email addresses that this message was sent to
     * @throws IllegalArgumentException if necessary params are not included
     */
    public String[] sendEmailsToAddresses(String from, String[] to, String subject, String message, boolean deferExceptions, String deliveryOption);

    // SERVER

    /**
     * @param settingName the name of the setting to retrieve,
     * Can be a string name: e.g. auto.ddl, etc. or one of the special SETTING_* constants in this file
     * 
     * @param defaultValue a specified default value to return if this setting cannot be found,
     * <b>NOTE:</b> You can set the default value to null but you must specify the class in parens
     * @return the value of the configuration setting or the default value if none can be found
     */
    public <T> T getConfigurationSetting(String settingName, T defaultValue);


    // ADHOC methods

    /**
     * Get the adhoc group by the unique id (not the evalGroupId)
     * 
     * @param adhocGroupId the unique id for an {@link EvalAdhocGroup}
     * @return the adhoc group or null if not found
     */
    public EvalAdhocGroup getAdhocGroupById(Long adhocGroupId);

    /**
     * Delete the adhoc group by the unique id,
     * will not fail if the group cannot be found
     * 
     * @param adhocGroupId the unique id for an {@link EvalAdhocGroup}
     */
    public void deleteAdhocGroup(Long adhocGroupId);

    /**
     * Save this adhoc user,
     * owner and email must be set<br/>
     * This will check to see if this user already exists and will update the existing one
     * rather than saving more copies of the same user<br/>
     * Permissions checks will be done based on the current user,
     * only the owner or an admin can modify an adhoc user,
     * anonymous users cannot create adhoc users
     * 
     * @param user
     */
    public void saveAdhocUser(EvalAdhocUser user);

    /**
     * Save this adhoc group,
     * owner and title must be set<br/>
     * Permissions checks will be done based on the current user,
     * only the owner or an admin can modify an adhoc group,
     * anonymous users cannot create adhoc groups
     * 
     * @param group 
     */
    public void saveAdhocGroup(EvalAdhocGroup group);

    /**
     * Get the list of adhoc groups for the user that created them,
     * ordered by title
     * 
     * @param userId internal user id (not username)
     * @return the list of all adhoc groups that this user owns
     */
    public List<EvalAdhocGroup> getAdhocGroupsForOwner(String userId);
    
    /**
     * Check if this user has admin access in the evaluation system (whether
     * as a sakai admin or an eval admin).
     * 
     * @param userId the internal user id (not username)
     * @return true if the user has admin access, false otherwise
     */
    public boolean isUserAdmin(String userId);
    
    /**
     * Gets the list of all eval admins.
     * 
     * @return a list of EvalAdmin objects
     */
    public List<EvalAdmin> getEvalAdmins();
    
    /**
     * Gets the EvalAdmin object representing the user with the specified user id.
     * Returns null if no user was found.
     * 
     * @param userId internal user id (not username)
     * @return an EvalAdmin object or null if not found
     */
	public EvalAdmin getEvalAdmin(String userId);
	
	/**
	 * calculateViewability() takes a state and determines if the state
	 * should be viewable, based on system settings
	 * <p>EvalSettings.VIEW_SURVEY_RESULTS_IGNORE, if set, will promote some
	 * states to EvalConstants.EVALUATION_STATE_VIEWABLE
	 * @param state one of the states listed in EvalConstants, such as EVALUATION_STATE_ACTIVE
	 * @return the original state or EVALUATION_STATE_VIEWABLE if the view can be promoted
	 */
	public String calculateViewability(String state);
	
	/**
	 * Assigns a user as an eval admin.
	 * 
	 * @param userId internal user id (not username)
	 * @param assignorUserId internal user id (not username) of the assignor (most likely the current user)
	 */
	public void assignEvalAdmin(String userId, String assignorUserId);
	
	/**
	 * Removes a user from the list of eval admins. 
	 * 
	 * @param userId internal user id (not username)
	 */
	public void unassignEvalAdmin(String userId);
	
	/**
	 * Checks if this user has eval admin rights in the evaluation system.
	 * 
	 * @param userId internal user id (not username)
	 * @return true if the user is an eval admin, false otherwise
	 */
	public boolean isUserEvalAdmin(String userId);
    
    /**
     * Get a list of evaluation groups that contain the search term.
     * SUPPORTED: Search assign groups of type Sakai Site
     * <br><br><i>
     * NOT YET SUPPORTED: This will search all supported group types by attribute: 
     * <ul><li>{@link EvalAdhocGroup#getTitle()}</li> 
     * <li>{@link EvalAssignGroup} searched by corresponding title (eg: sakai site title)</li>
     * <li>{@link EvalHierarchyNode#title}</li></ul></i>
     * 
     * @param searchString Text to match. If empty no results are sent back
     * @param order Field to order the groups
     * @param startResult Results set will start at this minimum count
     * @param maxResults Results set will end at this limit count
     * 
     * @return An ordered list of eval group ids
     */
    public List<String> searchForEvalGroupIds(String searchString, String order, int startResult, int maxResults);


    // PROVIDERS

    /**
     * The provider will auto-register if it can, but this will allow it to also be registered
     * or unregistered manually, calling this will override any previously registered eval groups provider
     * and calling it with a null will clear the currently registered provider
     * 
     * @param provider the EGP to register for the system to use,
     * if NULL then unregister the provider
     */
    public void registerEvalGroupsProvider(EvalGroupsProvider provider);

    /**
     * Check if this user has readonly admin access in the evaluation system
     * 
     * @param userId the internal user id (not username)
     * @return true if the user has readonly admin access, false otherwise
     */
    public boolean isUserReadonlyAdmin(String userId);
}
