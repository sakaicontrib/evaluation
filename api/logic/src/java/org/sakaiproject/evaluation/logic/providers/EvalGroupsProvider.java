/******************************************************************************
 * ExternalEvalGroups.java - created by aaronz@vt.edu
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

package org.sakaiproject.evaluation.logic.providers;

import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.logic.model.Context;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * This interface provides methods to get external eval groups and enrollments into the
 * evaluation system<br/>
 * This interface can and should be implemented and then spring loaded to allow
 * an institution to bring in external eval groups without needing to load them all
 * into Sakai or a similar system<br/>
 * <br/>
 * The spring bean must have an id that matches the fully qualified classname for this interface<br/>
 * Example:
 * <xmp>
 * <bean id="org.sakaiproject.evaluation.logic.providers.EvalGroupsProvider"
 * 		class="org.sakaiproject.yourproject.impl.EvalGroupsProviderImpl">
 * </bean>
 * </xmp>
 * <br/>
 * The 2 permissions this provider has to deal with are {@link EvalConstants#PERM_BE_EVALUATED}
 * (roughly equivalent to instructor role) and {@link EvalConstants#PERM_TAKE_EVALUATION}
 * (roughly equivalent to student role), compare the incoming permission to the constant
 * and only handle the cases indicated (do not try to handle all possible permissions)<br/>
 * <b>Note</b>: Specifically this allows us to reference eval groups and enrollments which
 * are not stored in something like Sakai
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalGroupsProvider {

	/**
	 * Get a list of all user ids that can take an evaluation or be evaluated in
	 * eval group(s) or course(s) identified by a unique identifier (groupId)
	 * 
	 * @param groupIds eval groups identified by unique strings
	 * @param permission a permission string PERM constant from {@link EvalConstants},
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @return a Set of Strings which represent the user Ids of all users in the course with that permission
	 */
	public Set getUserIdsForEvalGroups(String[] groupIds, String permission);

	/**
	 * Get a list of all user ids that can take an evaluation or be evaluated in
	 * eval group(s) or course(s) identified by a unique identifier (groupId),
	 * <b>Note</b>: This is simply here for the sake of efficiency
	 * 
	 * @param groupIds eval groups identified by unique strings
	 * @param permission a permission string PERM constant from {@link EvalConstants},
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @return the count of the users
	 */
	public int countUserIdsForEvalGroups(String[] groupIds, String permission);

	/**
	 * Get a list of all eval groups that a user can take an evaluation or be evaluated in,
	 * eval groups are identified by a unique identifier (groupId)
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string PERM constant from {@link EvalConstants},
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @return a List of {@link Context} objects representing eval groups
	 */
	public List getEvalGroupsForUser(String userId, String permission);

	/**
	 * Get a count of all eval groups that a user can take an evaluation or be evaluated in,
	 * eval groups are identified by a unique identifier (groupId),
	 * <b>Note</b>: This is simply here for the sake of efficiency
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string PERM constant from {@link EvalConstants},
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @return the count of the eval groups
	 */
	public int countEvalGroupsForUser(String userId, String permission);

	/**
	 * Return a Context object which represents a collection of users (a course or 
	 * group or collection in this case) based on the unique group id
	 * 
	 * @param groupId the unique id of an external eval group
	 * @return a Context object or null if not found
	 */
	public Context getContextByGroupId(String groupId);

	/**
	 * Check if a user has a specified permission/role within an eval group
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string PERM constant from {@link EvalConstants},
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @param groupId the unique id of an external eval group
	 * @return true if allowed, false otherwise
	 */
	public boolean isUserAllowedInGroup(String userId, String permission, String groupId);

}
