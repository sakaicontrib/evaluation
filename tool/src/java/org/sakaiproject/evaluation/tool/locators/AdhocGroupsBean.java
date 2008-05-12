package org.sakaiproject.evaluation.tool.locators;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvalAdhocSupport;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.stringutil.StringList;

/**
 * This is not a true Bean Locator. It's primary purpose is
 * for handling the calls for adhoc groups from the
 * modify_adhoc_groups page.
 * 
 * @author Steven Githens
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
   public List<String> acceptedInternalUsers = new ArrayList<String>();
   public List<String> acceptedAdhocUsers = new ArrayList<String>();
   public List<String> rejectedUsers = new ArrayList<String>();
   
   /**
    * Adds more users to an existing adhocgroup using the data entered with
    * adhocGroupId and newAdhocGroupUsers.
    * 
    * @return The UPDATED_ADHOCGROUP constant that could be used in ARI2 or
    * other action return mechanisms.
 	*/
   public String addUsersToAdHocGroup() {
       String currentUserId = externalLogic.getCurrentUserId();
       EvalAdhocGroup group = evalAdhocSupport.getAdhocGroupById(new Long(adhocGroupId));
       adhocGroupId = group.getId();
       
       /*
        * You can only change the adhoc group if you are the owner.
        */
       if (!currentUserId.equals(group.getOwner())) {
          throw new SecurityException("Only EvalAdhocGroup owners can change their groups: " + group.getId() + " , " + currentUserId);
       }
       
       updateAdHocGroup(group);
       
       return UPDATED_ADHOCGROUP;
   
   }
   
   /**
    * Primarily for EL Button Binding when creating new adhoc groups.
    * 
    * @return The action return SAVED_NEW_ADHOCGROUP for use in ARI2 primarily.
 	*/
   public String addNewAdHocGroup() {
	   String currentUserId = externalLogic.getCurrentUserId();
	      /*
	       * At the moment we allow any registered user to create adhoc groups.
	       */
	      if (externalLogic.isUserAnonymous(currentUserId)) {
	         throw new SecurityException("Anonymous users cannot create EvalAdhocGroups: " + currentUserId);
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
      String currentUserId = externalLogic.getCurrentUserId();
      /*
       * At the moment we allow any registered user to create adhoc groups.
       */
      if (externalLogic.isUserAnonymous(currentUserId)) {
         throw new SecurityException("Anonymous users cannot create EvalAdhocGroups: " + currentUserId);
      }
      
      Boolean useAdhocusers = (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_USERS);
      
      List<String> participants = new ArrayList<String>();
      checkAndAddToParticipantsList(newAdhocGroupUsers, participants);
     
      String[] existingParticipants = group.getParticipantIds();
      if (existingParticipants == null) {
    	  existingParticipants = new String[] {};
      }
      
      List<String> allParticipants = new ArrayList<String>();
      for (String particpant: existingParticipants) {
    	  allParticipants.add(particpant);
      }
      
      allParticipants.addAll(participants);
      
      group.setParticipantIds(allParticipants.toArray(new String[] {}));
      
      evalAdhocSupport.saveAdhocGroup(group);
      adhocGroupId = group.getId();

      log.info("Saved adhoc group: " + adhocGroupId);
	
      messages.addMessage(new TargettedMessage("modifyadhocgroup.message.savednewgroup",
    		  new String[] { group.getTitle() }, TargettedMessage.SEVERITY_INFO));
      
      StringList rejectedStringList = new StringList(rejectedUsers.toArray(new String[]{}));
      String rejectedUsersDisplay = rejectedStringList.toString();
      if (rejectedUsers.size() > 0 && useAdhocusers) {
    	  messages.addMessage(new TargettedMessage("modifyadhocgroup.message.badusers",
        		  new String[] { rejectedUsersDisplay }, TargettedMessage.SEVERITY_ERROR));
      }
      else if (rejectedUsers.size() > 0) {
    	  messages.addMessage(new TargettedMessage("modifyadhocgroup.message.badusers.noadhocusers",
        		  new String[] { rejectedUsersDisplay }, TargettedMessage.SEVERITY_ERROR));
      }
      else {
    	  log.info("Add entries added succesfully to new adhocGroup: " + adhocGroupId);
      }
      
   }
   
   /**
    * Adds folks to the participants list and does validation.
    * 
    * @param data The newline seperated list of adhoc users.
    * @param participants The existing list we are adding more participants to.
    */
   private void checkAndAddToParticipantsList(String data, List<String> participants) {
       String[] potentialMembers = data.split("\n");
       
       Boolean useAdhocusers = (Boolean) settings.get(EvalSettings.ENABLE_ADHOC_USERS);
       /*
        * As we go through the newline seperated list of folks we look for 2 things.
        * 1. Is the id for an existing user in the system?
        * 2. If Adhoc users are allowed, is this a valid email address?
        * 3. Otherwise add it to the garbage list.
        */
       for (String next: potentialMembers) {
    	   String potentialId = next.trim();
           if (externalLogic.getUserId(potentialId) != null) {
               participants.add(externalLogic.getUserId(potentialId));
               acceptedInternalUsers.add(potentialId);  
           }
           else if (useAdhocusers && EvalUtils.isValidEmail(potentialId)) {
               EvalAdhocUser newuser = new EvalAdhocUser(externalLogic.getCurrentUserId(), potentialId);
               evalAdhocSupport.saveAdhocUser(newuser);
               participants.add(newuser.getUserId());
               acceptedAdhocUsers.add(potentialId);
           }
           else {
               rejectedUsers.add(potentialId);
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
   
   private EvalAdhocSupport evalAdhocSupport;
   public void setEvalAdhocSupport(EvalAdhocSupport bean) {
      this.evalAdhocSupport = bean;
   }
   
   private EvalCommonLogic externalLogic;
   public void setEvalExternalLogic(EvalCommonLogic logic) {
      this.externalLogic = logic;
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
