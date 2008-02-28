/**
 * $Id$
 * $URL$
 * EvalJobLogic.java - evaluation - May 28, 2007 12:07:31 AM - rwellis
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.externals;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * Handle the scheduling of jobs and taking action
 * when an EvalEvaluation changes state.
 * 
 * @author rwellis
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface EvalJobLogic {

   public static String ACTION_CREATE = "Create";
   public static String ACTION_UPDATE = "Update";
   public static String ACTION_DELETE = "Delete";

   /**
    * Handle job scheduling changes when a date changed 
    * by editing and saving an Evaluation necessitates
    * rescheduling a job.</br>
    * Check the invocation dates of pending jobs for
    * current EvalEvaluation state and change job
    * start date to match EvalEvaluation date.<br/>
    * Handle job scheduling when an EvalEvaluation is created.
    * Send email notification of EvalEvaluation creation and 
    * schedule a job to make the EvalEvaluation active when the 
    * start date is reached.<br/>
    * Remove all outstanding scheduled job invocations for this evaluation
    * if the state is deleted<br/>
    * 
    * @param evaluationId the unique id for an {@link EvalEvaluation}
    * @param actionState the state constant representing the change in the evaluation, 
    * constants are {@link #ACTION_CREATE},{@link #ACTION_DELETE},{@link #ACTION_UPDATE}
    */
   public void processEvaluationStateChange(Long evaluationId, String actionState);

   /**
    * Contains all valid job types
    */
   public String[] JOB_TYPES = {
         EvalConstants.JOB_TYPE_CREATED,
         EvalConstants.JOB_TYPE_ACTIVE,
         EvalConstants.JOB_TYPE_REMINDER,
         EvalConstants.JOB_TYPE_DUE,
         EvalConstants.JOB_TYPE_CLOSED,
         EvalConstants.JOB_TYPE_VIEWABLE,
         EvalConstants.JOB_TYPE_VIEWABLE_INSTRUCTORS,
         EvalConstants.JOB_TYPE_VIEWABLE_STUDENTS
   };

   /**
    * Handle sending email and starting jobs when a scheduled job 
    * calls this method. Dispatch to action(s) based on jobType.</br>
    * 
    * @param evaluationId the unique id for an {@link EvalEvaluation}
    * @param jobType the job type from {@link EvalConstants}
    */
   public void jobAction(Long evaluationId, String jobType);


}
