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

/**
 * Handle the processing of email notification and Evaluation state 
 * changes when an Evaluation is saved - or updated with different dates,
 * which might affect the time scheduled jobs should run.
 * 
 * @author rwellis
 *
 */
public interface EvalNotificationLogic {
	
	/**
	 * Handle email notification and Evaluation state change, re-scheduling 
	 * a job if necessary when an Evaluation date is changed. Evaluation
	 * change might arise from the editing and saving an Evaluation or a
	 * Quartz job running. 
	 * @param evalId the Evaluation identifier.
	 * @param calledFrom A required parameter used to disambiguate the cause
	 * of the Evaluation change. Its value is the name of class from which 
	 * the call was made.
	 * @throws Exception an unhandled exception.
	 */
	public void processEvaluationChange(Long evalId, String calledFrom) throws Exception;
	
	/**
	 * Handle email notification and state change scheduling when a new Evaluation is created.
	 * @param evalId the Evaluation identifier.
	 * @throws Exception an unhandled exception.
	 */
	public void processNewEvaluation(Long evalId) throws Exception;
}
