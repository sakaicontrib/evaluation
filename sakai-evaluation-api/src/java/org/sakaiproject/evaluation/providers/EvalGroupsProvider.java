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
package org.sakaiproject.evaluation.providers;

import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.logic.model.EvalGroup;

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
 * <bean id="org.sakaiproject.evaluation.providers.EvalGroupsProvider"
 * 		class="org.sakaiproject.yourproject.impl.EvalGroupsProviderImpl">
 * </bean>
 * </xmp>
 * <br/>
 * The 4 permissions this provider has to deal with 
 * are {@link #PERM_BE_EVALUATED} (roughly equivalent to instructor role) 
 * and {@link #PERM_TAKE_EVALUATION} (roughly equivalent to student role) 
 * and {@link #PERM_TA_ROLE} (roughly equivalent to teaching assistants role)
 * and {@link #PERM_ADMIN_READONLY} (an admin read only role) 
 * compare the incoming permission to the constant
 * and only handle the cases indicated (do not try to handle all possible permissions)<br/>
 * <b>Note</b>: Specifically this allows us to reference eval groups and enrollments which
 * are not stored in something like Sakai
 * <br/>
 * <b>Note</b>: If the type is set for the {@link EvalGroup} object then it should be EvalConstants.GROUP_TYPE_PROVIDED,
 * however, this should not be necessary
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalGroupsProvider {

	/**
	 * Permission: User will be evaluated for any eval group they have this permission in,
	 * they will appear in the listing as an instructor and will be able to view report results
	 */
	public final static String PERM_BE_EVALUATED = "provider.be.evaluated";
	/**
	 * Permission: User can take an evaluation for any eval group they have this permission in
	 */
	public final static String PERM_TAKE_EVALUATION = "provider.take.evaluation";
    /**
     * Permission: User can assign evaluations to any groups they have this permission in,
     * it means they can see the groups in the listing when assigning the eval to users via groups
     */
    public final static String PERM_ASSIGN_EVALUATION = "provider.assign.eval";
	
    /**
     * Permission: User can view responders status of an evaluation
     */
    public final static String PERM_VIEW_RESPONDERS = "provider.view.responders";

    public final static String PERM_ADMIN_READONLY = "provider.admin.readonly";
    
    /**
	 * Permission: User is marked as a TA in a section/course and will be treated as such,
	 * this is a special case permission
	 * http://bugs.sakaiproject.org/jira/browse/EVALSYS-345
	 */
	public final static String PERM_TA_ROLE = "provider.role.ta";

	/**
	 * Get a list of all user ids that can take an evaluation or be evaluated in
	 * eval group(s) or course(s) identified by a unique identifier (groupId)<br/>
    * <b>Note</b>: this should be user ids (internal Sakai ids) and NOT the user eid 
    * (external user id from your provider source) 
	 * 
	 * @param groupIds eval groups identified by unique strings
	 * @param permission a permission string PERM constant (from this API),
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @return a Set of Strings which represent the user Ids of all users in the course with that permission
	 */
	public Set<String> getUserIdsForEvalGroups(String[] groupIds, String permission);

	/**
    * <b>Note</b>: This is simply here for the sake of efficiency<br/>
	 * Get a list of all user ids that can take an evaluation or be evaluated in
	 * eval group(s) or course(s) identified by a unique identifier (groupId)<br/>
    * <b>Note</b>: this should be user ids (internal Sakai ids) and NOT the user eid 
    * (external user id from your provider source)
	 * 
	 * @param groupIds eval groups identified by unique strings
	 * @param permission a permission string PERM constant (from this API),
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @return the count of the users
	 */
	public int countUserIdsForEvalGroups(String[] groupIds, String permission);

	/**
	 * Get a list of all eval groups that a user can take an evaluation or be evaluated in,
	 * eval groups are identified by a unique identifier (groupId)
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string PERM constant (from this API),
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @return a List of {@link EvalGroup} objects representing eval groups
	 */
	public List<EvalGroup> getEvalGroupsForUser(String userId, String permission);

	/**
    * <b>Note</b>: This is simply here for the sake of efficiency<br/>
	 * Get a count of all eval groups that a user can take an evaluation or be evaluated in,
	 * eval groups are identified by a unique identifier (groupId)
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string PERM constant (from this API),
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @return the count of the eval groups
	 */
	public int countEvalGroupsForUser(String userId, String permission);

	/**
	 * Return a group object (which represents a collection of users, a course or 
	 * group or collection in this case) based on the unique group id, this allows the
	 * system to obtain the title and other meta data related to a group using the
	 * unique id of the group
	 * 
	 * @param groupId the unique id of an external eval group
	 * @return a {@link EvalGroup} object or null if not found
	 */
	public EvalGroup getGroupByGroupId(String groupId);

	/**
	 * Check if a user has a specified permission/role within an eval group
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string PERM constant (from this API),
	 * <b>Note</b>: only take evaluation and be evaluated are supported
	 * @param groupId the unique id of an external eval group
	 * @return true if allowed, false otherwise
	 */
	public boolean isUserAllowedInGroup(String userId, String permission, String groupId);

}
