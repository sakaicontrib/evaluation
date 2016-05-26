/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.jobmonitor;

/**
 * JobStatusReporter 
 *
 */
public interface JobStatusReporter 
{
	/**
	 * @param jobName
	 * @return jobId
	 */
	public String reportStarted(String jobName);
	
	/**
	 * @param jobId
	 * @param milestone
	 * @param detail
	 */
	public void reportProgress(String jobId, String milestone, String detail);
	
	/**
	 * @param jobId
	 * @param jobFailed TODO
	 * @param milestone
	 * @param detail
	 */
	public void reportError(String jobId, boolean jobFailed, String milestone, String detail);
	
	/**
	 * @param jobId
	 * @param jobFailed TODO
	 * @param milestone
	 * @param detail TODO
	 */
	public void reportFinished(String jobId, boolean jobFailed, String milestone, String detail);

}
