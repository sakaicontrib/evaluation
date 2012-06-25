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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 */
public class LoggingJobStatusReporter implements JobStatusReporter {
	
	Log log = LogFactory.getLog(LoggingJobStatusReporter.class);

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.jobmonitor.JobStatusReporter#reportStarted(java.lang.String)
	 */
	public String reportStarted(String jobName) {
		log.info("JobStatus. Job: " + jobName + " Starting.");
		return jobName;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.jobmonitor.JobStatusReporter#reportProgress(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void reportProgress(String jobId, String milestone, String detail) {
		StringBuilder buf = new StringBuilder();
		buf.append("JobStatus. Job: ");
		buf.append(jobId);
		buf.append(". Milestone: ");
		buf.append(milestone);
		buf.append(", Detail: ");
		buf.append(detail);
		log.info(buf.toString());

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.jobmonitor.JobStatusReporter#reportError(java.lang.String, java.lang.String)
	 */
	public void reportError(String jobId, boolean jobFailed, String milestone, String detail) {
		StringBuilder buf = new StringBuilder();
		buf.append("JobStatus. Job: ");
		buf.append(jobId);
		buf.append(", ERROR. Job-failed: ");
		buf.append(jobFailed);
		buf.append(", Milestone: ");
		buf.append(", Detail: ");
		buf.append(detail);
		log.info(buf.toString());

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.jobmonitor.JobStatusReporter#reportFinished(java.lang.String)
	 */
	public void reportFinished(String jobId, boolean jobFailed, String milestone, String detail) {
		StringBuilder buf = new StringBuilder();
		buf.append("JobStatus. Job: ");
		buf.append(jobId);
		buf.append(", FINISHED. Job-failed: ");
		buf.append(jobFailed);
		buf.append(", Milestone: ");
		buf.append(", Detail: ");
		buf.append(detail);
		log.info(buf.toString());
	}

}
