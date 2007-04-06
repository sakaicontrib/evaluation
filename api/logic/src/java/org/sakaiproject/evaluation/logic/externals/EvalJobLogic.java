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

import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * Handle the scheduling of jobs and taking action
 * when an EvalEvaluation changes state.
 * 
 * @author rwellis
 *
 */
public interface EvalJobLogic {
	
	/**
	 * Handle job scheduling changes when a date changed 
	 * by editing and saving an Evaluation necessitates
	 * rescheduling a job.
	 * 
	 * @param eval the EvalEvaluation
	 * @throws Exception
	 */
	public void processEvaluationChange(EvalEvaluation eval) throws Exception;
	
	/**
	 * Handle job scheduling when an EvalEvaluation is created.
	 * Send email notification of EvalEvaluation creation and 
	 * schedule a job to make the EvalEvaluation active when the 
	 * start date is reached.
	 *  
	 * @param eval the EvalEvaluation
	 * @throws Exception
	 */
	public void processNewEvaluation(EvalEvaluation eval) throws Exception;
	
	/**
	 * Handle sending email and starting jobs when a scheduled job 
	 * calls this method.
	 * 
	 * @param evaluationId the id of the EvalEvaluation
	 */
	public void jobAction(Long evaluationId);
}
