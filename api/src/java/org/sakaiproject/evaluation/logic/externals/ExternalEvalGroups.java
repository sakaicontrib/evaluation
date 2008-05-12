/**
 * $Id$
 * $URL$
 * ExternalEvalGroups.java - evaluation - Dec 24, 2006 12:07:31 AM - azeckoski
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
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;

/**
 * This interface provides methods to get EvalGroups (user collections) information
 * into the evaluation system and permissions related to those EvalGroups,
 * @see EvalCommonLogic
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface ExternalEvalGroups {

	public static final String NO_LOCATION = "noLocationFound";

	// EvalGroups

	/**
	 * @return the current group evalGroupId for the current session
	 * (represents the current group location of the user in the system)
	 */
	public String getCurrentEvalGroup();

	/**
	 * Construct an {@link EvalGroup} object based on the unique string id,
	 * group will have a special type {@link EvalConstants#GROUP_TYPE_INVALID} if data cannot be found
	 * 
	 * @param evalGroupId the internal unique ID for an evalGroup
	 * @return an {@link EvalGroup} object (special return if not found)
	 */
	public EvalGroup makeEvalGroupObject(String evalGroupId);


	// ENROLLMENTS

	/**
	 * Get a list of all user ids that have a specific permission in a evalGroupId
	 * 
	 * @param evalGroupId the internal unique ID for an evalGroup
	 * @param permission a permission string constant
	 * @return a Set of Strings which represent the user Ids of all users in the site with that permission
	 */
	public Set<String> getUserIdsForEvalGroup(String evalGroupId, String permission);

	/**
	 * Get a count of all user ids that have a specific permission in a evalGroupId
	 * 
	 * @param evalGroupId the internal unique ID for an evalGroup
	 * @param permission a permission string constant
	 * @return a count of the users
	 */
	public int countUserIdsForEvalGroup(String evalGroupId, String permission);

	/**
	 * Get a list of all eval groups that a user has a specific permission in
	 * (use {@link #countEvalGroupsForUser(String, String)} if you just need the number)
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string constant
	 * @return a List of {@link EvalGroup} objects
	 */
	public List<EvalGroup> getEvalGroupsForUser(String userId, String permission);

	/**
	 * Get a count of all contexts that a user has a specific permission in
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string constant
	 * @return the count of the eval groups that the user has a permission in
	 */
	public int countEvalGroupsForUser(String userId, String permission);

}
