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

import org.sakaiproject.evaluation.model.EvalEvaluation;

public interface ReportingPermissions {

   /**
    * Signature variation for convenience (especially if you only have the 
    * information from a ViewParams). See {@link #chooseGroupsPartialCheck(EvalEvaluation)}
    * for complete details on the return value
    * 
    * @param evalId unique ID of an {@link EvalEvaluation}
    * @return The array of groupIds we can choose from for viewing responses in 
    * this evaluation.  If you cannot view the responses from any groups in this
    * evaluation, this will return an empty list.
    */
   public String[] chooseGroupsPartialCheck(Long evalId);

   /**
    * This is a sort of partial security check based off of the full 
    * {@link #canViewEvaluationResponses(EvalEvaluation, String[])} method.
    * 
    * This is primarily needed for the Choose Groups page in reporting. In this
    * case, we want to genuinely check most of the permissions, except we don't
    * actually know what Group ID's we are looking at (because we are about to 
    * choose them).  Instead, we want to check almost all of the items in the 
    * rules, and if they pass successfully, return the Groups that we are able
    * to choose from for report viewing.
    *
    * @param evaluation an {@link EvalEvaluation} (must have been saved)
    * @return The array of groupIds we can choose from for viewing responses in 
    * this evaluation.  If you cannot view the responses from any groups in this
    * evaluation, this will return an empty list.<br/>
    * <b>NOTE:</b> If the survey is anonymous the returned array will be empty.
    * You should not rely on this has the sole permission check, mostly 
    * just for populating the Choose Groups page, and redirecting if the length
    * of the returned groups is 0 or 1.
    */
   public String[] chooseGroupsPartialCheck(EvalEvaluation evaluation);

   /**
    * Decide whether the current user can view the responses for an evaluation
    * and set of groups that participated in it.
    * 
    * @param evaluation The EvalEvaluation object we are looking at responses for.
    * @param groupIds The String array of Group IDs we want to view results for.
    * This usually look like "/site/mysite".
    * @return Yes or no answer if you can view the responses.
    */
   public boolean canViewEvaluationResponses(EvalEvaluation evaluation, String[] groupIds);

}