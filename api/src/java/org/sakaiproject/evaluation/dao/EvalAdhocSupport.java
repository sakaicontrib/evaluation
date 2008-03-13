/**
 * $Id$
 * $URL$
 * EvalAdhocSupport.java - evaluation - Mar 10, 2008 5:52:29 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.dao;

import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;

/**
 * Do NOT use this interface if outside the logic layer<br/>
 * This is just needed because we cannot proxy properly in Sakai without it
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
    * @param permission a permission string PERM constant (from this API),
    * <b>Note</b>: only take evaluation and be evaluated are supported
    * @param evalGroupId the unique id of an adhoc eval group (not the persistent id)
    * @return true if allowed, false otherwise
    */
   public boolean isUserAllowedInAdhocGroup(String userId, String permissionConstant, String evalGroupId);

}