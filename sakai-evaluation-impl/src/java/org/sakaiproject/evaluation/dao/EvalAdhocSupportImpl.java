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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.genericdao.api.search.Order;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;


/**
 * This class handles the adhoc users and groups in a central way,
 * use this rather than attempting to save/fetch/remove adhoc
 * users and groups directly using the dao<br/>
 * <b>WARNING:</b> Does not do any permissions checking and thus is not accessible from
 * anywhere but inside the logic layer<br/>
 * All these methods will be disabled (i.e. return empty sets, false, etc.) by 
 * the {@link EvalSettings#ENABLE_ADHOC_GROUPS} and {@link EvalSettings#ENABLE_ADHOC_USERS} settings,
 * the exception here are the save methods which will throw exceptions if they are used when the settings are disabled
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalAdhocSupportImpl implements EvalAdhocSupport {

   private static final Log LOG = LogFactory.getLog(EvalAdhocSupportImpl.class);

   private EvaluationDao dao;
   public void setDao(EvaluationDao dao) {
      this.dao = dao;
   }
   
   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }


   // USERS

   /**
    * Get the adhoc user by the unique id (not the userId)
    * 
    * @param adhocUserId the unique id for an {@link EvalAdhocUser}
    * @return the adhoc user or null if not found
    */
   public EvalAdhocUser getAdhocUserById(Long adhocUserId) {
      EvalAdhocUser user = null;
      if ( (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_USERS) ) {
         if (adhocUserId != null) {
            user = (EvalAdhocUser) dao.findById(EvalAdhocUser.class, adhocUserId);
         }
      }
      return user;
   }

   /**
    * Get the adhoc user by the unique username (login name)
    * 
    * @param username the unique login name
    * @return the adhoc user or null if not found
    */
   public EvalAdhocUser getAdhocUserByUsername(String username) {
      EvalAdhocUser user = null;
      if ( (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_USERS) ) {
         List<EvalAdhocUser> users = dao.findBySearch(EvalAdhocUser.class,
                 new Search("username", username) );
         if (users.size() > 0) {
            user = users.get(0);
         }
      }
      return user;      
   }

   /**
    * Get the adhoc user by the unique email address
    * 
    * @param email the unique email address
    * @return the adhoc user or null if not found
    */
   public EvalAdhocUser getAdhocUserByEmail(String email) {
      EvalAdhocUser user = null;
      if ( (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_USERS) ) {
         List<EvalAdhocUser> users = dao.findBySearch(EvalAdhocUser.class, 
                 new Search("email", email) );
         if (users.size() > 0) {
            user = users.get(0);
         }
      }
      return user;      
   }

   /**
    * Method which will allow for fairly efficient fetching of large numbers of internal users
    * 
    * @param userIds an array of internal user ids
    * @return a map of userId -> {@link EvalAdhocUser}
    */
   public Map<String, EvalAdhocUser> getAdhocUsersByUserIds(String[] userIds) {
      Map<String, EvalAdhocUser> m = new HashMap<>();
      if ( (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_USERS) ) {
         if (userIds.length > 0) {
            List<Long> adhocIds = new ArrayList<>();
             for( String userId : userIds )
             {
                 Long id = EvalAdhocUser.getIdFromAdhocUserId( userId );
                 if (id != null) {
                     adhocIds.add(id);
                 }
             }
            if (adhocIds.size() > 0) {
               Long[] ids = adhocIds.toArray(new Long[adhocIds.size()]);
               List<EvalAdhocUser> adhocUsers = getAdhocUsersByIds(ids);
               for (EvalAdhocUser adhocUser : adhocUsers) {
                  m.put(adhocUser.getUserId(), adhocUser);
               }
            }
         }
      }
      return m;
   }

   /**
    * Find a set of users based on the input array of ids
    * 
    * @param ids the persistent ids of {@link EvalAdhocUser}s,
    * empty set return no users, null returns all users
    * @return a list of adhoc users which match the ids
    */
   public List<EvalAdhocUser> getAdhocUsersByIds(Long[] ids) {
      List<EvalAdhocUser> users = new ArrayList<>();
      if ( (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_USERS) ) {
         if (ids == null) {
            users = dao.findAll(EvalAdhocUser.class);
         } else {
            if (ids.length > 0) {
               users = dao.findBySearch(EvalAdhocUser.class, new Search("id", ids) );
            }
         }
      }
      return users;
   }

   /**
    * Save this adhoc user,
    * owner and email must be set<br/>
    * This will check to see if this user already exists and will fetch the existing one
    * rather than saving more copies of the same user
    * 
    * @param user
    */
   public void saveAdhocUser(EvalAdhocUser user) {
      if (user.getType() == null) {
         user.setType(EvalAdhocUser.TYPE_EVALUATOR);
      }
      if (user.getOwner() == null) {
         throw new IllegalArgumentException("owner must be set for adhoc users");
      }
      if (user.getEmail() == null) {
         throw new IllegalArgumentException("email address must be set for adhoc users");
      }

      if (! (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_USERS) ) {
         throw new IllegalStateException(EvalSettings.ENABLE_ADHOC_USERS + " is currently disalbed, you cannot save any adhoc users");
      }

      boolean existingFound = false;
      // check that we did not use the email of an existing user
      EvalAdhocUser existing = getAdhocUserByEmail(user.getEmail());
      if (existing != null) {
         // this matches an existing user
         existingFound = true;

         if (user.getId() != null) {
            // this is not a new user
            if (user.getId().equals(existing.getId())) {
               // this is the same user as the existing one so don't do anything to it but save it
               existingFound = false;
            } else {
               // this user is different from the existing one
               /* 
                * the existing user was changed to have an email address that matches an existing one 
                * so we remove this user and defer to the existing one
                */
               dao.delete(user);
   
               // now we update the existing user if nulls are there (treat this like an update without loading the persistent user)
               boolean updated = false;
               if (existing.getUsername() == null &&
                     user.getUsername() != null) {
                  existing.setUsername( user.getUsername() );
                  updated = true;
               }
               if (existing.getDisplayName() == null &&
                     user.getDisplayName() != null) {
                  existing.setDisplayName( user.getDisplayName() );
                  updated = true;
               }
               if (updated) {
                  dao.save(existing);
               }
            }
         }
      }

      if (existingFound) {
         // copy over the field values
         copyAdhocUser(existing, user);
      } else {
         dao.save(user);
         LOG.info("Saved adhoc user: " + user.getEmail());
      }
   }

   /**
    * Copy the field values from the master to the copy
    * 
    * @param master
    * @param copy
    */
   private void copyAdhocUser(EvalAdhocUser master, EvalAdhocUser copy) {
      copy.setId( master.getId() );
      copy.setDisplayName( master.getDisplayName() );
      copy.setEmail( master.getEmail() );
      copy.setLastModified( master.getLastModified() );
      copy.setOwner( master.getOwner() );
      copy.setType( master.getType() );
      copy.setUsername( master.getUsername() );
   }


   // GROUPS

   /**
    * Get the adhoc group by the unique id (not the evalGroupId)
    * 
    * @param adhocGroupId the unique id for an {@link EvalAdhocGroup}
    * @return the adhoc group or null if not found
    */
   public EvalAdhocGroup getAdhocGroupById(Long adhocGroupId) {
      EvalAdhocGroup group = null;
      if ( (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_GROUPS) ) {
         if (adhocGroupId != null) {
            group = (EvalAdhocGroup) dao.findById(EvalAdhocGroup.class, adhocGroupId);
         }
      }
      return group;
   }

   public void deleteAdhocGroup(Long adhocGroupId) {
       if (adhocGroupId != null) {
           dao.delete(EvalAdhocGroup.class, adhocGroupId);
       }
    }

   /**
    * Save this adhoc group,
    * owner and title must be set
    * 
    * @param group 
    */
   public void saveAdhocGroup(EvalAdhocGroup group) {
      if (group.getOwner() == null || "".equals(group.getOwner())) {
         throw new IllegalArgumentException("owner must be set for adhoc group");
      }
      if (group.getTitle() == null || "".equals(group.getTitle())) {
         throw new IllegalArgumentException("title must be set for adhoc group");
      }
      if (! (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_GROUPS) ) {
         throw new IllegalStateException(EvalSettings.ENABLE_ADHOC_GROUPS + " is currently disabled, you cannot save any adhoc groups");
      }

      dao.save(group);
      LOG.info("Saved adhoc group: " + group.getEvalGroupId());
   }

   /**
    * Get the list of adhoc groups for this user,
    * ordered by title
    * 
    * @param userId internal user id (not username)
    * @return the list of all adhoc groups that this user owns
    */
   public List<EvalAdhocGroup> getAdhocGroupsForOwner(String userId) {
      List<EvalAdhocGroup> groups = new ArrayList<>(0);
      if ( (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_GROUPS) ) {
         groups = dao.findBySearch(EvalAdhocGroup.class, new Search(
                 new Restriction("owner", userId), 
                 new Order("title") ) );
      }
      return groups;
   }

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
   public List<EvalAdhocGroup> getAdhocGroupsByUserAndPerm(String userId, String permissionConstant) {
      List<EvalAdhocGroup> groups = new ArrayList<>(0);
      if ( (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_GROUPS) ) {
         // passthrough to the dao method
         groups = dao.getEvalAdhocGroupsByUserAndPerm(userId, permissionConstant);
      }
      return groups;
   }

   /**
    * Check if a user has a specified permission/role within an adhoc group
    * 
    * @param userId the internal user id (not username)
    * @param permissionConstant a permission string PERM constant (from this API),
    * <b>Note</b>: only take evaluation and be evaluated are supported
    * @param evalGroupId the unique id of an adhoc eval group (not the persistent id)
    * @return true if allowed, false otherwise
    */
   public boolean isUserAllowedInAdhocGroup(String userId, String permissionConstant, String evalGroupId) {
      boolean allowed = false;
      if ( (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_GROUPS) ) {
         // passthrough to the dao method
         allowed = dao.isUserAllowedInAdhocGroup(userId, permissionConstant, evalGroupId);
      }
      return allowed;
   }

}
