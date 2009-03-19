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

package org.sakaiproject.evaluation.logic;

import java.util.List;

import org.sakaiproject.evaluation.logic.externals.ExternalContent;
import org.sakaiproject.evaluation.logic.externals.ExternalEmail;
import org.sakaiproject.evaluation.logic.externals.ExternalEntities;
import org.sakaiproject.evaluation.logic.externals.ExternalEvalGroups;
import org.sakaiproject.evaluation.logic.externals.ExternalScheduler;
import org.sakaiproject.evaluation.logic.externals.ExternalSecurity;
import org.sakaiproject.evaluation.logic.externals.ExternalTextUtils;
import org.sakaiproject.evaluation.logic.externals.ExternalUsers;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;


/**
 * Handles all basic internal operations for the evaluation system,
 * includes user information, group/course/site information, and security checks<br/>
 * <b>Note:</b> Meant to be used internally in the evaluation system app only, please
 * do not call these methods outside the evaluation tool or logic layer
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalCommonLogic extends ExternalUsers, ExternalEvalGroups, ExternalEntities, 
ExternalEmail, ExternalSecurity, ExternalContent, ExternalScheduler, ExternalTextUtils {

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
     * @return an array of email addresses that this message was sent to
     */
    public String[] sendEmailsToUsers(String from, String[] toUserIds, String subject, String message, boolean deferExceptions);

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

}
