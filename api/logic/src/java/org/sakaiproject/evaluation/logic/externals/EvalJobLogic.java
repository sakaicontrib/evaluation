/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.evaluation.logic.externals;

import java.util.Map;

import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * Handle the scheduling of jobs and taking action
 * when an EvalEvaluation changes state.
 * 
 * @author rwellis
 */
public interface EvalJobLogic {
	
	/**
	 * Check if a job of a given type for a given evaluation is scheduled. 
	 * At most, one job of a given type is scheduled per evaluation.
	 * 
	 * @param evaluationId
	 * @param jobType
	 * @return true if a job of this type is scheduled for this evaluation, false otherwise
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
	 * @param eval the EvalEvaluation
	 */
	public void processEvaluationChange(EvalEvaluation eval);
	
	/**
	 * Handle job scheduling when an EvalEvaluation is created.
	 * Send email notification of EvalEvaluation creation and 
	 * schedule a job to make the EvalEvaluation active when the 
	 * start date is reached.
	 *  
	 * @param eval the EvalEvaluation
	 */
	public void processNewEvaluation(EvalEvaluation eval);
	
	/**
	 * Handle sending email and starting jobs when a scheduled job 
	 * calls this method. Dispatch to action(s) based on jobType.</br>
	 * 
	 * @param evaluationId the id of the EvalEvaluation
	 * @param jobType the job type from EvalConstants
	 */
	public void jobAction(Long evaluationId, String jobType);
	
	/**
	 * Remove all outstanding scheduled job invocations for this EvalEvaluation.
	 * 
	 * @param evalId the EvalEvaluation identifier
	 */
	public void removeScheduledInvocations(Long evalId);
	

	/**
	 * Schedule reminders to be run under the ScheduledInvocationManager, at the
	 * first reminder interval after the start date that is in the future
	 * 
	 * @param evaluationId the EvalEvaluation id
	 */
	public void scheduleReminder(Long evaluationId);
	
	/**
	 * Schedule sending email to the instructor with responses to the evaluation.
	 * 
	 * @param idMap a map of evaluation id and instructor eid. TODO Use external logic to handle case
	 * where instructor is not in the Sakai user id map table.
	 */
	public void scheduleResponsesEmail(Map idMap);
}
