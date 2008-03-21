package org.sakaiproject.evaluation.tool.locators;

import org.sakaiproject.evaluation.dao.EvalAdhocSupport;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;

/**
 * This is not a true Bean Locator. It's primary purpose is
 * for handling the UVB Ajax calls for adhoc groups from the
 * evaluation_assign page.
 * 
 * @author Steven Githens
 */
public class AdhocGroupsBean {
   private String adhocGroupId;
   private String adhocGroupTitle;
   private String[] newAdhocGroupUsers;
   
   private EvalAdhocSupport evalAdhocSupport;
   public void setEvalAdhocSupport(EvalAdhocSupport bean) {
      this.evalAdhocSupport = bean;
   }
   
   private EvalExternalLogic externalLogic;
   public void setEvalExternalLogic(EvalExternalLogic logic) {
      this.externalLogic = logic;
   }
   
   public void adNewAdHocGroup() {
      EvalAdhocGroup group = new EvalAdhocGroup(externalLogic.getCurrentUserId(),
            adhocGroupTitle);

      evalAdhocSupport.saveAdhocGroup(group);
   }
   
   /*
    * Boilerplate Getters and Setters below.
    */
   
   public String getAdhocGroupId() {
      return adhocGroupId;
   }
   public void setAdhocGroupId(String adhocGroupId) {
      this.adhocGroupId = adhocGroupId;
   }
   public String[] getNewAdhocGroupUsers() {
      return newAdhocGroupUsers;
   }
   public void setNewAdhocGroupUsers(String[] newAdhocGroupUsers) {
      this.newAdhocGroupUsers = newAdhocGroupUsers;
   }

   public String getAdhocGroupTitle() {
      return adhocGroupTitle;
   }

   public void setAdhocGroupTitle(String adhocGroupTitle) {
      this.adhocGroupTitle = adhocGroupTitle;
   }
   
   
}
