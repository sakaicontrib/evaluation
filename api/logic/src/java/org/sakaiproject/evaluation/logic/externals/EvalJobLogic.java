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
 * Handle the scheduling of jobs taking action at transitions points in EvalEvaulation
 * state.
 * 
 * @author rwellis
 *
 */
public interface EvalJobLogic {
	
	/**
	 * Handle job scheduling when an EvalEvaluation is changed. EvalEvaluation
	 * change might arise from the editing and saving an Evaluation or a
	 * job that fixes state.
	 * 
	 * @param eval the Evaluation.
	 * @throws Exception
	 */
	public void processEvaluationChange(EvalEvaluation eval) throws Exception;
	
	/**
	 * Handle job schedluing when an EvalEvaulation is created. Send email
	 * notification of EvalEvaulation creation and schedule a job to
	 * fix state when start date is reached.
	 *  
	 * @param eval the Evaluation.
	 * @throws Exception
	 */
	public void processNewEvaluation(EvalEvaluation eval) throws Exception;
}
