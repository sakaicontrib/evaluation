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
package org.sakaiproject.evaluation.dao;

import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalUsers;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;

/**
 * This is for dealing with Adhoc users and groups<br/>,
 * these are stored in evaluation itself and more details notes are here:
 * http://confluence.sakaiproject.org/confluence/display/EVALSYS/Evaluation+Implementation<br/>
 * <br/>
 * This is the primary interface for dealing with adhoc users and groups (writes/updates).
 * This interface should be used when fetching adhoc groups (especially for getting the list of existing adhoc groups).
 * <br/>
 * When fetching user information about a large group of users (e.g. when displaying the adhoc group members to the user) 
 * you should use the methods in {@link EvalCommonLogic} (in particular {@link ExternalUsers}).
 * <br/>
 * {@link ExternalUsers#getEvalUsersByIds(String[])} in particular is the method to fetch a large list of users and 
 * it does it as efficiently as possible. All user interaction should be done with {@link EvalUser} objects
 * <br/>
 * <b>WARNING</b>: Do NOT use this interface if outside the logic layer<br/>
 * This is just needed because we cannot proxy properly in Sakai without it</br>
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface EvalAdhocSupport {

   /**
    * Get the adhoc user by the unique id (not the userId)
    * 
    * @param adhocUserId the unique id for an {@link EvalAdhocUser}
    * @return the adhoc user or null if not found
    */
   public EvalAdhocUser getAdhocUserById(Long adhocUserId);

   /**
    * Get the adhoc user by the unique username (login name)
    * 
    * @param username the unique login name
    * @return the adhoc user or null if not found
    */
   public EvalAdhocUser getAdhocUserByUsername(String username);

   /**
    * Get the adhoc user by the unique email address
    * 
    * @param email the unique email address
    * @return the adhoc user or null if not found
    */
   public EvalAdhocUser getAdhocUserByEmail(String email);

   /**
    * Method which will allow for fairly efficient fetching of large numbers of internal users
    * 
    * @param userIds an array of internal user ids
    * @return a map of userId -> {@link EvalAdhocUser}
    */
   public Map<String, EvalAdhocUser> getAdhocUsersByUserIds(String[] userIds);

   /**
    * Find a set of users based on the input array of ids
    * 
    * @param ids the persistent ids of {@link EvalAdhocUser}s,
    * empty set return no users, null returns all users
    * @return a list of adhoc users which match the ids
    */
   public List<EvalAdhocUser> getAdhocUsersByIds(Long[] ids);

   /**
    * Save this adhoc user,
    * owner and email must be set<br/>
    * This will check to see if this user already exists and will update the existing one
    * rather than saving more copies of the same user
    * 
    * @param user
    */
   public void saveAdhocUser(EvalAdhocUser user);

   /**
    * Get the adhoc group by the unique id (not the evalGroupId)
    * 
    * @param adhocGroupId the unique id for an {@link EvalAdhocGroup}
    * @return the adhoc group or null if not found
    */
   public EvalAdhocGroup getAdhocGroupById(Long adhocGroupId);

   /**
    * Save this adhoc group,
    * owner and title must be set
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
    * Get adhoc groups for a user and permission, 
    * this is a way to check the perms for a user
    * 
    * @param userId the internal user id (not username)
    * @param permissionConstant a permission constant which is 
    * {@link EvalConstants#PERM_BE_EVALUATED} for instructors/evaluatees OR
    * {@link EvalConstants#PERM_TAKE_EVALUATION} for students/evaluators,
    * other permissions will return no results
    * @return a list of adhoc groups for which this user has this permission
    */
   public List<EvalAdhocGroup> getAdhocGroupsByUserAndPerm(String userId, String permissionConstant);

   /**
    * Check if a user has a specified permission/role within an adhoc group
    * 
    * @param userId the internal user id (not username)
    * @param permissionConstant a permission string PERM constant (from this API),
    * <b>Note</b>: only take evaluation and be evaluated are supported
    * @param evalGroupId the unique id of an adhoc eval group (not the persistent id)
    * @return true if allowed, false otherwise
    */
   public boolean isUserAllowedInAdhocGroup(String userId, String permissionConstant, String evalGroupId);

   /**
    * Delete the adhoc group by the unique id,
    * will not fail if the group cannot be found
    * 
    * @param adhocGroupId the unique id for an {@link EvalAdhocGroup}
    */
   public void deleteAdhocGroup(Long adhocGroupId);

}