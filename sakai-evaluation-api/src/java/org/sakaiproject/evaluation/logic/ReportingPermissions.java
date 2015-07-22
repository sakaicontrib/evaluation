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
package org.sakaiproject.evaluation.logic;

import java.util.Set;

import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * This interface allows for permission checking for 
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ReportingPermissions {

   /**
    * Signature variation for convenience (especially if you only have the 
    * information from a ViewParams). See {@link #getResultsViewableEvalGroupIdsForCurrentUser(EvalEvaluation)}
    * for complete details on the return value
    * 
    * @param evalId unique ID of an {@link EvalEvaluation}
    * @return the set of evalGroupIds the current user can choose from for viewing responses in 
    * this evaluation.  If the current user cannot view the responses from any groups in this
    * evaluation, this will return an empty set
    */
   public Set<String> getResultsViewableEvalGroupIdsForCurrentUser(Long evalId);

   /**
    * This is a sort of partial security check based off of the full 
    * {@link #canViewEvaluationResponses(EvalEvaluation, String[])} method
    * for the current user<br/>
    * This is primarily needed for the Choose Groups page in reporting. In this
    * case, we want to genuinely check most of the permissions, except we don't
    * actually know what Group ID's we are looking at (because we are about to 
    * choose them).  Instead, we want to check almost all of the items in the 
    * rules, and if they pass successfully, return the Groups that we are able
    * to choose from for report viewing.<br/>
    * <b>NOTE:</b> If the survey is anonymous the returned array will be empty.
    * You should not rely on this has the sole permission check, mostly 
    * just for populating the Choose Groups page, and redirecting if the length
    * of the returned groups is 0 or 1.<br/>
    *
    * @param evaluation an {@link EvalEvaluation} (must have been saved)
    * @return the set of evalGroupIds the current user can choose from for viewing responses in 
    * this evaluation.  If the current user cannot view the responses from any groups in this
    * evaluation, this will return an empty set
    */
   public Set<String> getResultsViewableEvalGroupIdsForCurrentUser(EvalEvaluation evaluation);

   /**
    * Decide whether the current user can view the responses for an evaluation
    * and set of groups that participated in it.
    * 
    * @param evaluation The EvalEvaluation object we are looking at responses for.
    * @param groupIds array of evalGroupIds for the groups we want to check,
    * if this is null then check all groups, if empty set then check no groups
    * @return true if you the current user can view results, false otherwise
    */
   public boolean canViewEvaluationResponses(EvalEvaluation evaluation, String[] groupIds);

}