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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;

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

	/**
	 * Construct a List of {@link EvalGroup} objects (child sections) based on the unique string id (parent site),
	 * group will have a special type {@link EvalConstants#GROUP_TYPE_INVALID} if data cannot be found
	 * 
	 * @param evalGroupId the internal unique ID for an evalGroup
	 * @return a List of {@link EvalGroup} objects (special return if not found)
	 */
	public List<EvalGroup> makeEvalGroupObjectsForSectionAwareness( String evalGroupId );

	// ENROLLMENTS

	/**
	 * Get a list of all user ids that have a specific permission in a evalGroupId
	 * 
	 * @param evalGroupId the internal unique ID for an evalGroup
	 * @param permission a permission string constant
	 * @param sectionAware if returning users for one section of a site/group or all sections
	 * @return a Set of Strings which represent the user Ids of all users in the site with that permission
	 */
	public Set<String> getUserIdsForEvalGroup(String evalGroupId, String permission, Boolean sectionAware);

	/**
	 * Get a count of all user ids that have a specific permission in a evalGroupId
	 * 
	 * @param evalGroupId the internal unique ID for an evalGroup
	 * @param permission a permission string constant
	 * @param sectionAware if returning count of users for one section of a site/group or all sections
	 * @return a count of the users
	 */
	public int countUserIdsForEvalGroup(String evalGroupId, String permission, Boolean sectionAware);

	/**
	 * Get a count of all user ids that have a specific permission, for a batch of eval groups at once.
	 * Use this instead of calling {@link #countUserIdsForEvalGroup(String, String, Boolean)} in a loop:
	 * that method resolves membership one group at a time, which does not scale to a page showing
	 * hundreds of groups. This resolves all of them with a single lookup where possible.
	 *
	 * @param evalGroupIds the internal unique IDs for the evalGroups
	 * @param permission a permission string constant
	 * @param sectionAware if returning count of users for one section of a site/group or all sections
	 * @return a Map (evalGroupId -&gt; user count) with one entry per input evalGroupId
	 */
	public Map<String, Integer> countUserIdsForEvalGroups(Collection<String> evalGroupIds, String permission, Boolean sectionAware);

	/**
	 * Check, for a batch of eval groups at once, whether each one has at least one user with a specific
	 * permission. Use this instead of {@link #countUserIdsForEvalGroups(Collection, String, Boolean)}
	 * when only the yes/no answer is needed (e.g. "does this group have anyone eligible to evaluate").
	 * <p>
	 * The admin user is always excluded (present in every group) with no lookup needed. Role-view
	 * ("View Site As") accounts are resolved one candidate at a time per group, stopping as soon as a
	 * real (non role-view) user is found - so it never resolves more users per group than strictly
	 * necessary to answer yes/no, unlike {@link #countUserIdsForEvalGroups(Collection, String, Boolean)}
	 * which must resolve every candidate to return an exact count.
	 *
	 * @param evalGroupIds the internal unique IDs for the evalGroups
	 * @param permission a permission string constant
	 * @param sectionAware if checking users for one section of a site/group or all sections
	 * @return a Map (evalGroupId -&gt; true if at least one eligible user was found) with one entry per input evalGroupId
	 */
	public Map<String, Boolean> hasUserIdsForEvalGroups(Collection<String> evalGroupIds, String permission, Boolean sectionAware);

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
	 * Get a list of all eval groups that a user has a specific permission in that are of the same as this site
	 * (use {@link #countEvalGroupsForUser(String, String)} if you just need the number)
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string constant
	 * @param currentSiteId the site id of the site to use as a filtering measure
	 * @return a List of {@link EvalGroup} objects
	 */
	public List<EvalGroup> getFilteredEvalGroupsForUser(String userId, String permission, String currentSiteId);

	/**
	 * Get a count of all contexts that a user has a specific permission in
	 * 
	 * @param userId the internal user id (not username)
	 * @param permission a permission string constant
	 * @return the count of the eval groups that the user has a permission in
	 */
	public int countEvalGroupsForUser(String userId, String permission);
	
	/**
	 * Check if this group/site is published since some operations must be done on published groups/sites.
	 * 
	 * @param evalGroupId
	 * @return TRUE: Group exists and is published. FALSE: Group may not exist or is unpublished
	 */
	public boolean isEvalGroupPublished(String evalGroupId);

	/**
     * Get a list of evaluation group Ids that contain the search term.
     * Currently, this will only search assign groups of type Sakai Site 
     * 
     * @param searchString Text to match. If empty no results are sent back
     * @param order Field to order the groups
     * @param startResult Results set will start at this minimum count
     * @param maxResults Results set will end at this limit count
     */
	public List<String> searchForEvalGroupIds(String searchString, String order, int startResult, int maxResults);

}
