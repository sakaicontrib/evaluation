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
package org.sakaiproject.evaluation.logic.externals;

import java.util.Date;
import java.util.Map;

import org.quartz.Job;
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
    * Create and schedule a job using cron-job syntax for the timing of execution(s) of the job. 
    * The job to be executed is defined by the jobClass parameter along with any data included
    * in the dataMap parameter. A String representing a valid cron expression must be included 
    * in the dataMap with the key given by EvalConstants.CRON_SCEDULER_CRON_EXPRESSION.  Also, 
    * values for jobName, jobGroup, triggerName and triggerGroup must be included in the dataMap 
    * with keys of EvalConstants.CRON_SCHEDULER_JOB_GROUP, EvalConstants.CRON_SCHEDULER_JOB_NAME, 
    * EvalConstants.CRON_SCHEDULER_TRIGGER_GROUP, and EvalConstants.CRON_SCHEDULER_TRIGGER_NAME,
    * respectively.  Those key-value pairs will be removed from the dataMap, and all other 
    * key-value pairs in the dataMap will be provided to an instance of the jobClass each time 
    * the cron job is executed. 
	* @param jobClass The class which is to be executed when the cron job is executed.
	* @param dataMap The data to be provided to an instance of the jobClass whenever it 
	* 	executes, as well as key-value pairs as described above.
	* @return A string giving the full name of the scheduled job, or null if an error occurred 
	* 	while attempting to schedule the job.
	*/
   public String scheduleCronJob(Class<? extends Job> jobClass, Map<String, String> dataMap);

   /**
    * Create and schedule a job using cron-job syntax for the timing of execution(s) of the job. 
    * The job to be executed is defined by the jobClassBeanId parameter along with any data included
    * in the dataMap parameter. A String representing a valid cron expression must be included 
    * in the dataMap with the key given by EvalConstants.CRON_SCEDULER_CRON_EXPRESSION.  Also, 
    * values for jobName, jobGroup, triggerName and triggerGroup must be included in the dataMap 
    * with keys of EvalConstants.CRON_SCHEDULER_JOB_GROUP, EvalConstants.CRON_SCHEDULER_JOB_NAME, 
    * EvalConstants.CRON_SCHEDULER_TRIGGER_GROUP, and EvalConstants.CRON_SCHEDULER_TRIGGER_NAME,
    * respectively.  Those key-value pairs will be removed from the dataMap, and all other 
    * key-value pairs in the dataMap will be provided to an instance of the jobClass each time 
    * the cron job is executed. 
    * @param jobClassBeanId The full bean id to locate class which is to be executed when the cron job is executed.
	* @param dataMap The data to be provided to an instance of the jobClass whenever it 
	* 	executes, as well as key-value pairs as described above.
	* @return A string giving the full name of the scheduled job, or null if an error occurred 
	* 	while attempting to schedule the job.
    */
   public String scheduleCronJob(String jobClassBeanId, Map<String, String> dataMap);
   
   /**
    * Get a mapping of all cron jobs within a job group, containing info about their triggers and 
    * their properties.
    * @param jobGroup The name of the group for which information is requested.	
    * @return A mapping from the full name of the trigger to a Map containing data about the job and 
    * 	the trigger.  
    */
   public Map<String,Map<String, String>> getCronJobs(String jobGroup);

   /**
    * Delete a single cron job (and all triggers associated with it).
	* @param jobName
	* @param jobGroup
	* @return
	*/
   public boolean deleteCronJob(String jobName, String jobGroup);

}
