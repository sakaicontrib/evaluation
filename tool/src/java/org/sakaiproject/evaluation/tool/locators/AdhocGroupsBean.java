package org.sakaiproject.evaluation.tool.locators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvalAdhocSupport;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.model.EvalAdhocUser;
import org.sakaiproject.evaluation.tool.producers.AdhocGroupParticipantsDiv;
import org.sakaiproject.evaluation.tool.viewparams.AdhocGroupParams;
import org.sakaiproject.evaluation.utils.EvalUtils;

import uk.org.ponder.rsf.builtin.UVBBean;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;

/**
 * This is not a true Bean Locator. It's primary purpose is
 * for handling the UVB Ajax calls for adhoc groups from the
 * evaluation_assign page.
 * 
 * @author Steven Githens
 */
public class AdhocGroupsBean {
   private static Log log = LogFactory.getLog(AdhocGroupsBean.class);
    
   private Long adhocGroupId;
   private String adhocGroupTitle;
   private String newAdhocGroupUsers;
   private String userId; // Used for binding users to remove
   private UVBBean uvbBean;
   
   public Map<String,String> acceptedInternalUsers = new HashMap<String,String>();
   public Map<String,String> acceptedAdhocUsers = new HashMap<String,String>();
   public List<String> rejectedUsers = new ArrayList<String>();
   public String participantDivUrl;
   
   /*
    * Adds more users to an existing adhocgroup using the data entered with
    * adhocGroupId and newAdhocGroupUsers
    */
   public void addUsersToAdHocGroup() {
       String currentUserId = externalLogic.getCurrentUserId();
       EvalAdhocGroup group = evalAdhocSupport.getAdhocGroupById(new Long(adhocGroupId));
       adhocGroupId = group.getId();
       
       /*
        * You can only change the adhoc group if you are the owner.
        */
       if (!currentUserId.equals(group.getOwner())) {
          throw new SecurityException("Only EvalAdhocGroup owners can change their groups: " + group.getId() + " , " + currentUserId);
       }

       
   }
   
   /*
    * Adds a new Adhoc Group using the data entered into newAdhocGroupUsers.
    */
   public void addNewAdHocGroup() {
      String currentUserId = externalLogic.getCurrentUserId();
      /*
       * At the we allow any registered user to create adhoc groups.
       */
      if (externalLogic.isUserAnonymous(currentUserId)) {
         throw new SecurityException("Anonymous users cannot create EvalAdhocGroups: " + currentUserId);
      }
      
      EvalAdhocGroup group = new EvalAdhocGroup(currentUserId, adhocGroupTitle);

      log.info("About to save Adhoc group: " + adhocGroupTitle);
      
      List<String> participants = new ArrayList<String>();
      checkAndAddToParticipantsList(newAdhocGroupUsers, participants);
     
      group.setParticipantIds(participants.toArray(new String[] {}));
      
      evalAdhocSupport.saveAdhocGroup(group);
      //participantDivUrl = vsh.getFullURL(
      //        new AdhocGroupParams(AdhocGroupParticipantsDiv.VIEW_ID, group.getId()));
      adhocGroupId = group.getId();
      uvbBean.populate();
      log.info("Saved adhoc group");
   }
   
   /*
    * Adds folks to the participants list and does validation and stuff.
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
       for (String potentialId: potentialMembers) {
           if (externalLogic.getUserId(potentialId) != null) {
               participants.add(potentialId);
               acceptedInternalUsers.put(potentialId, potentialId + ", " + externalLogic.getUserUsername(potentialId));  
           }
           else if (useAdhocusers && EvalUtils.isValidEmail(potentialId)) {
               EvalAdhocUser newuser = new EvalAdhocUser(externalLogic.getCurrentUserId(), potentialId);
               evalAdhocSupport.saveAdhocUser(newuser);
               participants.add(newuser.getUserId());
               acceptedAdhocUsers.put(newuser.getUserId(), "Adhoc user, " + potentialId);
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

   public UVBBean getUvbBean() {
       return uvbBean;
   }

   public void setUvbBean(UVBBean uvbBean) {
       this.uvbBean = uvbBean;
   }
   
   private EvalAdhocSupport evalAdhocSupport;
   public void setEvalAdhocSupport(EvalAdhocSupport bean) {
      this.evalAdhocSupport = bean;
   }
   
   private EvalExternalLogic externalLogic;
   public void setEvalExternalLogic(EvalExternalLogic logic) {
      this.externalLogic = logic;
   }
   
   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }
   
   private ViewStateHandler vsh;
   public void setViewStateHandler(ViewStateHandler vsh) {
       this.vsh = vsh;
   }

   public String getUserId() {
       return userId;
   }

   public void setUserId(String userId) {
       this.userId = userId;
   }
   
}
