/**
 * $Id$
 * $URL$
 * EvalBeanUtils.java - evaluation - Feb 21, 2008 11:06:08 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.beans;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;


/**
 * Utils which depend on some of the basic eval beans<br/>
 * <b>NOTE:</b> These utils require other spring beans and thus this class must be injected,
 * attempting to access this without injecting it will cause failures
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EvalBeanUtils {

   private EvalExternalLogic externalLogic;
   public void setExternalLogic(EvalExternalLogic external) {
      this.externalLogic = external;
   }

   private EvalSettings settings;
   public void setSettings(EvalSettings settings) {
      this.settings = settings;
   }


   /**
    * Determines if evaluation results can be viewed based on the minimum count of responses for the system
    * and the inputs, also checks if user is an admin (they can always view results)
    * 
    * @param responsesCount the current number of responses for an evaluation
    * @param enrollmentsCount the count of enrollments (can be 0 if anonymous or unknown)
    * @return number of responses needed before viewing is allowed, 0 indicates viewable now
    */
   public int getResponsesNeededToViewForResponseRate(int responsesCount, int enrollmentsCount) {
      int responsesNeeded = 1;
      if ( externalLogic.isUserAdmin( externalLogic.getCurrentUserId() ) ) {
         responsesNeeded = 0;
      } else {
         int minResponses = ((Integer) settings.get(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS)).intValue();
         responsesNeeded = minResponses - responsesCount;
         if (responsesCount >= enrollmentsCount) {
            // special check to make sure the cases where there is a very small enrollment count is still ok
            responsesNeeded = 0;
         }
         if (responsesNeeded < 0) {
            responsesNeeded = 0;
         }
      }
      return responsesNeeded;
   }

   /**
    * General check for admin/owner permissions,
    * this will check to see if the provided userId is an admin and
    * also check if they are equal to the provided ownerId
    * 
    * @param userId internal user id
    * @param ownerId an internal user id
    * @return true if this user is admin or matches the owner user id
    */
   public boolean checkUserPermission(String userId, String ownerId) {
      boolean allowed = false;
      if ( externalLogic.isUserAdmin(userId) ) {
         allowed = true;
      } else if ( ownerId.equals(userId) ) {
         allowed = true;
      }
      return allowed;
   }

}
