/**
 * $Id$
 * $URL$
 * ReportingPermissions.java - evaluation - Mar 7, 2008 2:14:20 PM - azeckoski
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