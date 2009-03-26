package org.sakaiproject.evaluation.tool.locators;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * This is not a true Bean Locator. It's primary purpose is
 * for handling the calls for adhoc groups from the
 * modify_adhoc_groups page.
 * 
 * @author Steven Githens
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class AdhocGroupsBean {
   private static Log log = LogFactory.getLog(AdhocGroupsBean.class);

   public static final String SAVED_NEW_ADHOCGROUP = "added-adhoc-group";
   public static final String UPDATED_ADHOCGROUP = "updated-adhoc-group";

   // These three variables are for EL form binding.
   private Long adhocGroupId;
   private String adhocGroupTitle;
   private String newAdhocGroupUsers;

   // These are for keeping track of users. They may not all be used at the moment
   // but would be needed if we want more detailed error/confirm dialogs.
   public List<String> acceptedUsers = new ArrayList<String>();
   public List<String> rejectedUsers = new ArrayList<String>();
   public List<String> alreadyInGroupUsers = new ArrayList<String>();

   /**
    * Adds more users to an existing adhocgroup using the data entered with
    * adhocGroupId and newAdhocGroupUsers.
    * 
    * @return The UPDATED_ADHOCGROUP constant that could be used in ARI2 or
    * other action return mechanisms.
    */
   public String addUsersToAdHocGroup() {
      String currentUserId = commonLogic.getCurrentUserId();
      EvalAdhocGroup group = commonLogic.getAdhocGroupById(new Long(adhocGroupId));
      adhocGroupId = group.getId();

      /*
       * You can only change the adhoc group if you are the owner.
       */
      if (!currentUserId.equals(group.getOwner())) {
         throw new SecurityException("Only EvalAdhocGroup owners can change their groups: " + group.getId() + " , " + currentUserId);
      }

      // put the new title in the adhoc group - https://bugs.caret.cam.ac.uk/browse/CTL-1305
      if (adhocGroupTitle != null && ! "".equals(adhocGroupTitle)) {
          group.setTitle(adhocGroupTitle);
      }
      // save the adhoc group
      updateAdHocGroup(group);

      return UPDATED_ADHOCGROUP;
   }

   /**
    * Primarily for EL Button Binding when creating new adhoc groups.
    * 
    * @return The action return SAVED_NEW_ADHOCGROUP for use in ARI2 primarily.
    */
   public String addNewAdHocGroup() {
      String currentUserId = commonLogic.getCurrentUserId();
      /*
       * At the moment we allow any registered user to create adhoc groups.
       */
      if (commonLogic.isUserAnonymous(currentUserId)) {
         throw new SecurityException("Anonymous users cannot create EvalAdhocGroups: " + currentUserId);
      }
      if (adhocGroupTitle == null || "".equals(adhocGroupTitle)) {
          messages.addMessage(new TargettedMessage("modifyadhocgroup.message.notitle",
                  new Object[] {}, TargettedMessage.SEVERITY_ERROR));
          return "noTitle";
      }

      EvalAdhocGroup group = new EvalAdhocGroup(currentUserId, adhocGroupTitle);

      updateAdHocGroup(group);

      return SAVED_NEW_ADHOCGROUP;
   }

   /**
    * Updates the Group by adding the newline seperated users in member
    * variable newAdhocGroupUsers.
    * 
    */
   private void updateAdHocGroup(EvalAdhocGroup group) {
      String currentUserId = commonLogic.getCurrentUserId();
      /*
       * At the moment we allow any registered user to create adhoc groups.
       */
      if (commonLogic.isUserAnonymous(currentUserId)) {
         throw new SecurityException("Anonymous users cannot create EvalAdhocGroups: " + currentUserId);
      }

      Boolean useAdhocusers = (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_USERS);

      String[] existingParticipants = group.getParticipantIds();
      if (existingParticipants == null) {
         existingParticipants = new String[] {};
      }

      List<String> existingParticipantsList = new ArrayList<String>();
      for (String particpant: existingParticipants) {
         existingParticipantsList.add(particpant);
      }

      List<String> participants = new ArrayList<String>();
      checkAndAddToParticipantsList(newAdhocGroupUsers, participants, existingParticipantsList);

      List<String> allParticipants = new ArrayList<String>();
      for (String particpant: existingParticipants) {
         allParticipants.add(particpant);
      }

      allParticipants.addAll(participants);

      group.setParticipantIds(allParticipants.toArray(new String[] {}));

      commonLogic.saveAdhocGroup(group);
      adhocGroupId = group.getId();

      messages.addMessage(new TargettedMessage("modifyadhocgroup.message.savednewgroup",
            new Object[] { group.getTitle() }, TargettedMessage.SEVERITY_INFO));

      // Build the rejected users with no trailing commas
      //String[] rejectedStringList = rejectedUsers.toArray(new String[]{});
      StringBuilder rejectedUsersDisplayBuilder = new StringBuilder();
      for (int i = 0; i < rejectedUsers.size(); i++) {
         if (i == rejectedUsers.size()-1) {
            rejectedUsersDisplayBuilder.append(rejectedUsers.get(i));
         }
         else {
            rejectedUsersDisplayBuilder.append(rejectedUsers.get(i));
            rejectedUsersDisplayBuilder.append(", ");
         }
      }
      String rejectedUsersDisplay = rejectedUsersDisplayBuilder.toString();

      if (rejectedUsers.size() > 0 && useAdhocusers) {
         messages.addMessage(new TargettedMessage("modifyadhocgroup.message.badusers",
               new Object[] { rejectedUsersDisplay }, TargettedMessage.SEVERITY_ERROR));
      }
      else if (rejectedUsers.size() > 0) {
         messages.addMessage(new TargettedMessage("modifyadhocgroup.message.badusers.noadhocusers",
               new Object[] { rejectedUsersDisplay }, TargettedMessage.SEVERITY_ERROR));
      }
      else {
         log.info("Add entries added succesfully to new adhocGroup: " + adhocGroupId);
      }

      // Message for any users already in the group
      if (alreadyInGroupUsers.size() > 0) {
         StringBuilder alreadyInGroupUsersBuilder = new StringBuilder();
         for (int i = 0; i < alreadyInGroupUsers.size(); i++) {
            if (i == alreadyInGroupUsers.size()-1) {
               alreadyInGroupUsersBuilder.append(alreadyInGroupUsers.get(i));
            }
            else {
               alreadyInGroupUsersBuilder.append(alreadyInGroupUsers.get(i));
               alreadyInGroupUsersBuilder.append(", ");
            }
         }
         messages.addMessage(new TargettedMessage("modifyadhocgroup.message.existingusers",
               new Object[] { alreadyInGroupUsersBuilder.toString() }, TargettedMessage.SEVERITY_INFO ));
      }
   }

   /**
    * Adds folks to the participants list and does validation.
    * 
    * @param data The newline seperated list of adhoc users.
    * @param participants The existing list we are adding more participants to.
    */
   private void checkAndAddToParticipantsList(String data, List<String> participants, 
         List<String> existingParticipants) {
      // If they didn't actually type anything in the window, don't throw up any
      // errors or anything. Same if it's just all whitespace.
      if ("".equals(data)
            || data == null
            || EvalUtils.isBlank(data.trim())
            || data.matches("[ \t\r\n]+")) {
         messages.addMessage(new TargettedMessage("modifyadhocgroup.message.badusers",
               new Object[] { "NONE" }, TargettedMessage.SEVERITY_ERROR ));
         return;
      }

      String[] potentialMembers = data.split("\n");

      Boolean useAdhocusers = (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_USERS);
      /*
       * As we go through the newline separated list of users we look for these things:
       * 1. Is this person already in the adhoc group?
       * 2. Is the id for an existing user in the system?
       * 3. If Adhoc users are allowed, is this a valid email address?
       * 4. Otherwise add it to the garbage list.
       */
      for (String next: potentialMembers) {
         String potentialId = next.trim();
         if (EvalUtils.isBlank(potentialId)) {
            continue; // skip blank ones
         }

         String userId = null;

         // check if this is a valid username for an existing user
         String internalUserId = commonLogic.getUserId(potentialId);
         if (internalUserId == null) {
            internalUserId = potentialId;
         }
         // look up the username by their internal id
         EvalUser user = commonLogic.getEvalUserById(internalUserId);
         if (EvalConstants.USER_TYPE_EXTERNAL.equals(user.type)
               || EvalConstants.USER_TYPE_INTERNAL.equals(user.type)) {
            userId = user.userId;
            potentialId = user.displayName;
         } else {
            // check if this is an email belonging to an existing user
            user = commonLogic.getEvalUserByEmail(potentialId);
            if (EvalConstants.USER_TYPE_EXTERNAL.equals(user.type)
                  || EvalConstants.USER_TYPE_INTERNAL.equals(user.type)) {
               userId = user.userId;
               potentialId = user.displayName;
            } else {
               // check if the email is valid and we are using adhoc users
               if (useAdhocusers 
                     && EvalUtils.isValidEmail(potentialId)) {
                  EvalAdhocUser newUser = new EvalAdhocUser(commonLogic.getCurrentUserId(), potentialId);
                  commonLogic.saveAdhocUser(newUser);
                  userId = newUser.getUserId();
               }
            }
         }

         if (userId == null) {
            // invalid entry
            rejectedUsers.add(potentialId);
         } else {
            if (existingParticipants.contains(userId)) {
               // check if user is already in the group
               alreadyInGroupUsers.add(potentialId);
            } else {
               // add user to participants and put this user in the accepted list
               participants.add(userId);
               acceptedUsers.add(potentialId);
            }
         }
      }
   }

   /*
    * Boilerplate Getters and Setters below.
    */

   public Long getAdhocGroupId() {
      return adhocGroupId;
   }
   public void setAdhocGroupId(Long adhocGroupId) {
      this.adhocGroupId = adhocGroupId;
   }

   public String getAdhocGroupTitle() {
      return adhocGroupTitle;
   }

   public void setAdhocGroupTitle(String adhocGroupTitle) {
      this.adhocGroupTitle = adhocGroupTitle;
   }

   public String getNewAdhocGroupUsers() {
      return newAdhocGroupUsers;
   }

   public void setNewAdhocGroupUsers(String newAdhocGroupUsers) {
      this.newAdhocGroupUsers = newAdhocGroupUsers;
   }

   private EvalCommonLogic commonLogic;
   public void setCommonLogic(EvalCommonLogic bean) {
      this.commonLogic = bean;
   }

   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }

   private TargettedMessageList messages;
   public void setMessages(TargettedMessageList messages) {
      this.messages = messages;
   }

}
