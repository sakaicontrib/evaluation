/**
 * $Id$
 * $URL$
 * ExternalScheduler.java - evaluation - Mar 26, 2008 3:31:07 PM - azeckoski
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

import java.util.Date;
import java.util.Map;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.sakaiproject.evaluation.logic.model.EvalScheduledJob;


/**
 * Handles the external scheduling of jobs
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public interface ExternalScheduler {

   /**
    * Find all jobs of this type which are running for the evaluation system
    * 
    * @param evaluationId a unique id for the evaluation associated with this job
    * @param jobType a job type constant from EvalConstants.JOB_TYPE_
    * @return an array of jobs
    */
   public EvalScheduledJob[] findScheduledJobs(Long evaluationId, String jobType);

   /**
    * Delete this job if it can be found
    * 
    * @param jobID the uuid of an {@link EvalScheduledJob}
    */
   public void deleteScheduledJob(String jobID);

   /**
    * Create a new scheduled job to execute on the date specified
    * 
    * @param executionDate the date to execute the job
    * @param evaluationId a unique id for the evaluation associated with this job
    * @param jobType a job type constant from EvalConstants.JOB_TYPE_
    * @return the jobId for the newly created job
    */
   public String createScheduledJob(Date executionDate, Long evaluationId, String jobType);
   
   /**
    * Create and schedule a job using cron-job syntax. 
    * 
	* @param cronTrigger
	* @param jobDetail
	* @return
	*/
   public String scheduleCronJob(CronTrigger cronTrigger, JobDetail jobDetail);

   /**
    * Get a mapping of all cron jobs within a group, containing info about their triggers and their properties.
    * @param jobGroup The name of the group for which information is requested.	
    * @param propertyNames The names of properties whose values should be included.
    * @return
    */
   public abstract Map<String,Map<String, String>> getCronJobs(String jobGroup, String[] propertyNames);

   /**
	* @param jobName
	* @param groupName
	* @return
	*/
   public abstract boolean deleteCronJob(String jobName, String groupName);

}
