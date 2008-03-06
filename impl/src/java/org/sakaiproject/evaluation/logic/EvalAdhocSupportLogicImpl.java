/**
 * $Id$
 * $URL$
 * AdhocSupportLogicImpl.java - evaluation - Mar 5, 2008 3:04:55 PM - azeckoski
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;


/**
 * This class handles the adhoc users and groups in a central way,
 * use this rather than attempting to save/fetch/remove adhoc
 * users and groups directly using the dao<br/>
 * <b>WARNING:</b> Does not do any permissions checking and thus is not accessible from
 * anywhere but inside the logic layer
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalAdhocSupportLogicImpl {

   private static Log log = LogFactory.getLog(EvalAdhocSupportLogicImpl.class);

   private EvaluationDao dao;
   public void setDao(EvaluationDao dao) {
      this.dao = dao;
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
      if (adhocUserId != null) {
         user = (EvalAdhocUser) dao.findById(EvalAdhocUser.class, adhocUserId);
      }
      return user;
   }

   /**
    * Get the adhoc user by the unique username (login name)
    * 
    * @param username the unique login name
    * @return the adhoc user or null if not found
    */
   @SuppressWarnings("unchecked")
   public EvalAdhocUser getAdhocUserByUsername(String username) {
      EvalAdhocUser user = null;
      List<EvalAdhocUser> users = dao.findByProperties(EvalAdhocUser.class, 
            new String[] {"username"}, 
            new Object[] {username});
      if (users.size() > 0) {
         user = users.get(0);
      }
      return user;      
   }

   /**
    * Get the adhoc user by the unique email address
    * 
    * @param email the unique email address
    * @return the adhoc user or null if not found
    */
   @SuppressWarnings("unchecked")
   public EvalAdhocUser getAdhocUserByEmail(String email) {
      EvalAdhocUser user = null;
      List<EvalAdhocUser> users = dao.findByProperties(EvalAdhocUser.class, 
            new String[] {"email"}, 
            new Object[] {email});
      if (users.size() > 0) {
         user = users.get(0);
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
      Map<String, EvalAdhocUser> m = new HashMap<String, EvalAdhocUser>();
      if (userIds.length > 0) {
         List<Long> adhocIds = new ArrayList<Long>();
         for (int i = 0; i < userIds.length; i++) {
            Long id = EvalAdhocUser.getIdFromAdhocUserId(userIds[i]);
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
      return m;
   }

   /**
    * Find a set of users based on the input array of ids
    * 
    * @param ids the persistent ids of {@link EvalAdhocUser}s,
    * empty set return no users, null returns all users
    * @return a list of adhoc users which match the ids
    */
   @SuppressWarnings("unchecked")
   public List<EvalAdhocUser> getAdhocUsersByIds(Long[] ids) {
      List<EvalAdhocUser> users = new ArrayList<EvalAdhocUser>();
      if (ids == null) {
         users = dao.findAll(EvalAdhocUser.class);
      } else {
         if (ids.length > 0) {
            users = dao.findByProperties(EvalAdhocUser.class, 
                  new String[] {"id"}, 
                  new Object[] {ids});
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
         log.info("Saved adhoc user: " + user.getEmail());
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
      if (adhocGroupId != null) {
         group = (EvalAdhocGroup) dao.findById(EvalAdhocGroup.class, adhocGroupId);
      }
      return group;
   }

   /**
    * Save this adhoc group,
    * owner and title must be set
    * 
    * @param group 
    */
   public void saveAdhocGroup(EvalAdhocGroup group) {
      if (group.getOwner() == null) {
         throw new IllegalArgumentException("owner must be set for adhoc group");
      }
      if (group.getTitle() == null) {
         throw new IllegalArgumentException("title must be set for adhoc group");
      }
      dao.save(group);
      log.info("Saved adhoc group: " + group.getEvalGroupId());
   }

   /**
    * Get the list of adhoc groups for this user,
    * ordered by title
    * 
    * @param userId internal user id (not username)
    * @return the list of all adhoc groups that this user owns
    */
   @SuppressWarnings("unchecked")
   public List<EvalAdhocGroup> getAdhocGroupsForUser(String userId) {
      List<EvalAdhocGroup> groups = dao.findByProperties(EvalAdhocGroup.class, 
            new String[] {"owner"}, 
            new Object[] {userId},
            new int[] {EvaluationDao.EQUALS},
            new String[] {"title"});
      return groups;
   }



}
