/**
 * $Id: EvalJobLogic.java 1000 May 28, 2007 12:07:31 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
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

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * Handle the scheduling of jobs and taking action
 * when an EvalEvaluation changes state.
 * 
 * @author rwellis
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface EvalJobLogic {
	
	/**
	 * Check if a job of a given type for a given evaluation is scheduled. 
	 * At most, one job of a given type is scheduled per evaluation.
	 * 
    * @param evaluationId the unique id for an {@link EvalEvaluation}
    * @param jobType the job type from {@link EvalConstants}
	 * @return true if a job of this type is scheduled for this evaluation, false otherwise
	 * @deprecated this is only used in one place, put this logic inside the method which schedules reminders -AZ
	 */
	public boolean isJobTypeScheduled(Long evaluationId, String jobType);
	
	/**
	 * Handle job scheduling changes when a date changed 
	 * by editing and saving an Evaluation necessitates
	 * rescheduling a job.</br>
	 * Check the invocation dates of pending jobs for
	 * current EvalEvaluation state and change job
	 * start date to match EvalEvaluation date.
	 * 
	 * @param eval an {@link EvalEvaluation} persisted object
	 */
	public void processEvaluationChange(EvalEvaluation eval);
	
	/**
	 * Handle job scheduling when an EvalEvaluation is created.
	 * Send email notification of EvalEvaluation creation and 
	 * schedule a job to make the EvalEvaluation active when the 
	 * start date is reached.
	 *  
    * @param eval an {@link EvalEvaluation} persisted object
	 */
	public void processNewEvaluation(EvalEvaluation eval);
	
	/**
	 * Handle sending email and starting jobs when a scheduled job 
	 * calls this method. Dispatch to action(s) based on jobType.</br>
	 * 
    * @param evaluationId the unique id for an {@link EvalEvaluation}
	 * @param jobType the job type from {@link EvalConstants}
	 */
	public void jobAction(Long evaluationId, String jobType);
	
	/**
	 * Remove all outstanding scheduled job invocations for this evaluation
	 * 
	 * @param evaluationId the unique id for an {@link EvalEvaluation}
	 */
	public void removeScheduledInvocations(Long evaluationId);
	

	/**
	 * Schedule reminders to be run under the ScheduledInvocationManager, at the
	 * first reminder interval after the start date that is in the future
	 * 
    * @param evaluationId the unique id for an {@link EvalEvaluation}
	 */
	public void scheduleReminder(Long evaluationId);
}
