
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
 * Work carried out at an EvalEvaulation transition.
 * 
 * @author rwellis
 *
 */
public interface EvalTransition {
	
	/**
	 * Call EvalEvaluationsLogic to fix an EvalEvaluation's state 
	 * based on the current date and EvalEvaluation dates.
	 *
	 * @param id The EvalEvaluation id
	 */
	public void fixState(Long id);
	
	/**
	 * Send email to responders saying an EvalEvaluation is available,
	 * providing a link to click to take the EvalEvaluation.
	 *
	 * @param id The EvalEvaluation id
	 */
	public void sendActive(Long id);
	
	/**
	 * Send email saying an EvalEvaluation has been created.
	 * 
	 * @param id The EvalEvaluation id
	 */
	public void sendCreated(Long id);
	
	/**
	 * Send an email reminder to those who have not responded to an
	 * EvalEvaluation.
	 * 
	 * @param id The EvalEvaluation id
	 */
	public void sendReminder(Long id);
	
	/**
	 * Send email saying that an EvalEvaluation's results are viewable. 
	 * 
	 * @param id The EvalEvaluation id
	 *
	 */
	public void sendViewable(Long id);
	
}
