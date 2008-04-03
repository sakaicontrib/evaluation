package org.sakaiproject.evaluation.tool.locators;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.dao.EvalAdhocSupport;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;

/**
 * We haven't yet gone down the path of creating full blown bean locators for
 * adhoc groups (and not sure if we will), so we have a few small beans to 
 * service our UVB calls from the assign page adhoc ajax.  This is for removing
 * members from an adhoc group you have created.
 * 
 * @author sgithens
 */
public class AdhocGroupMemberRemovalBean {

   /*
    * Members destined for EL bindings.
    */
   private Long adhocGroupId;
   private String adhocUserId;
   
   /*
    * Stuff we need injected.
    */
   private EvalAdhocSupport evalAdhocSupport;
   private EvalExternalLogic externalLogic;
   
   
   public void removeUser() { 
      String currentUserId = externalLogic.getCurrentUserId();
      EvalAdhocGroup adhocGroup = evalAdhocSupport.getAdhocGroupById(adhocGroupId);
      
      /*
       * You can only change the adhoc group if you are the owner.
       */
      if (!currentUserId.equals(adhocGroup.getOwner())) {
         throw new SecurityException("Only EvalAdhocGroup owners can change their groups: " + adhocGroup.getId() + " , " + currentUserId);
      }
      
      List<String> participants = new ArrayList<String>();
      for (String partId: adhocGroup.getParticipantIds()) {
         if (!partId.equals(adhocUserId)) {
            participants.add(partId);
         }
      }
      adhocGroup.setParticipantIds(participants.toArray(new String[] {}));
      evalAdhocSupport.saveAdhocGroup(adhocGroup);
   }

   /*
    * Boiler Plate Getter/Setters
    */
   public void setEvalAdhocSupport(EvalAdhocSupport bean) {
      this.evalAdhocSupport = bean;
   }

   public Long getAdhocGroupId() {
      return adhocGroupId;
   }

   public void setAdhocGroupId(Long adhocGroupId) {
      this.adhocGroupId = adhocGroupId;
   }

   public String getAdhocUserId() {
      return adhocUserId;
   }

   public void setAdhocUserId(String adhocUserId) {
      this.adhocUserId = adhocUserId;
   }
   
   public void setEvalExternalLogic(EvalExternalLogic logic) {
      this.externalLogic = logic;
   }
   
}
