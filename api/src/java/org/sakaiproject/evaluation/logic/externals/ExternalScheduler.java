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

}
