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

import java.util.List;

public interface EvalExportLogic {
	
	/**
	 * Email all responses to instructors
	 * 
	 * @return a List of messages to display
	 */
	public List<String> emailAllResponses();
	
	/**
	 * Write all responses to a ContentResource
	 * 
	 * @return a List of messages to display
	 */
	public List<String> writeAllResponses();
	
	/**
	 * Get the identity of the instructor being evaluated
	 * in this evaluation.
	 * 
	 * TODO figure out a general approach to evaluating 
	 * multiple instructors in a course
	 * 
	 * @param the external identity of the evaluation
	 * @return the identity (eid) of the instructor 
	 * 
	 * TODO use external logic to get id
	 * if user not in Sakai user id map table
	 */
	public String getInstructorByEvaluationEid(String evalEid);
	
	/**
	 * Get the responses to an evaluation formatted as text.
	 * 
	 * @param id the identity of the evaluation
	 * @return the text responses
	 */
	public String getFormattedResponsesByEvaluationId(Long id);
	
	/**
	 * Get the value of the lock indicating than export processing is occurring
	 * 
	 * @return
	 */
	public boolean getLock();
	
	/**
	 * Set the value of the lock indicating export processing is occurring
	 * 
	 * @param lock true indicates an exclusive lock is held, false that no lock is held
	 */
	public void setLock(boolean lock);
	
	/**
	 * Is the lock set excluding concurrent export
	 * 
	 * @return true indicating the lock is set, false if the lock is not set
	 */
	public boolean isLocked();
}
