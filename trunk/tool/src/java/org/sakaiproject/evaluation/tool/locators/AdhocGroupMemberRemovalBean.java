package org.sakaiproject.evaluation.tool.locators;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

/**
 * We haven't yet gone down the path of creating full blown bean locators for
 * adhoc groups (and not sure if we will), so we have a few small beans to 
 * service our adhoc groups page.  This is for removing
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
   private EvalCommonLogic commonLogic;
   public void setCommonLogic(EvalCommonLogic commonLogic) {
      this.commonLogic = commonLogic;
   }
   private TargettedMessageList messages;

   /**
    * This is destined to be bound via EL to a button.
    */
   public void removeUser() { 
      EvalAdhocGroup adhocGroup = commonLogic.getAdhocGroupById(adhocGroupId);

      List<String> participants = new ArrayList<String>();
      for (String partId: adhocGroup.getParticipantIds()) {
         if (!partId.equals(adhocUserId)) {
            participants.add(partId);
         }
      }
      adhocGroup.setParticipantIds(participants.toArray(new String[] {}));
      commonLogic.saveAdhocGroup(adhocGroup);

      EvalUser user = commonLogic.getEvalUserById(adhocUserId);

      messages.addMessage(new TargettedMessage("modifyadhocgroup.message.removeduser",
            new String[] { user.displayName }, TargettedMessage.SEVERITY_INFO));
   }

   /*
    * Boiler Plate Getter/Setters
    */
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

   public void setMessages(TargettedMessageList messages) {
      this.messages = messages;
   }
}
