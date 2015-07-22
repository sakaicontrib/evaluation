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
package org.sakaiproject.evaluation.logic.externals;


/**
 * Handles external security checks
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ExternalSecurity {

   // PERMISSIONS

   /**
    * Check if this user has super admin access in the evaluation system
    * @param userId the internal user id (not username)
    * @return true if the user has admin access, false otherwise
    */
   public boolean isUserSakaiAdmin(String userId);

   /**
    * Check if a user has a specified permission within a evalGroupId, primarily
    * a convenience method and passthrough
    * 
    * @param userId the internal user id (not username)
    * @param permission a permission string constant
    * @param evalGroupId the internal unique eval group ID (represents a site or group)
    * @return true if allowed, false otherwise
    */
   public boolean isUserAllowedInEvalGroup(String userId, String permission, String evalGroupId);
   
   /**
    * Check if this user has the readonly admin permission
    * @param userId the internal user id (not username)
    * @return true if the user has readonly admin access, false otherwise
    */
   public boolean isUserReadonlyAdmin(String userId);

}
