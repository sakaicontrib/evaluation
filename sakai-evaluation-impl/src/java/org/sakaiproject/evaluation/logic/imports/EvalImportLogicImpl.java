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
package org.sakaiproject.evaluation.logic.imports;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.utils.EvalUtils;

/**
 * Handle the importing of external data into the Evaluation System.
 * 
 * By default, processing occurs in the current interactive session. The
 * session is periodically set active to avoid timing out during a long-running
 * import. Property eval.qrtzImport=true in sakai.properties causes processing
 * in a Quartz job rather than the current interactive session.
 * 
 * @author rwellis
 */
public class EvalImportLogicImpl implements EvalImportLogic {
	
	private static final Log LOG = LogFactory.getLog(EvalImportLogicImpl.class);
	
	//Spring injection
	private EvalCommonLogic commonLogic;
   public void setCommonLogic(EvalCommonLogic commonLogic) {
      this.commonLogic = commonLogic;
   }

	private EvalImport evalImport;
	public void setEvalImport(EvalImport evalImport) {
		this.evalImport = evalImport;
	}

	private EvalImportJob evalImportJob;
	public void setEvalImportJob(EvalImportJob evalImportJob) {
		this.evalImportJob = evalImportJob;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalImportLogic#load(java.lang.String)
	 */
	public List<String> load(String id) {
		List<String> messages = new ArrayList<>();
		String currentUserId = commonLogic.getCurrentUserId(); //sessionManager.getCurrentSessionUserId();
		try
		{
		   Boolean qrtzImport = commonLogic.getConfigurationSetting(EvalExternalLogic.SETTING_EVAL_QUARTZ_IMPORT, Boolean.FALSE);
         if (qrtzImport) {
            processInQuartz(id);
         } else {
            messages = evalImport.process(id, currentUserId);
         }
		}
		catch(Exception e)
		{
			if(LOG.isWarnEnabled()) {
				LOG.warn(e);
			}
			messages.add("There was a problem loading the data: " + e.toString());
			
		}
		return messages;
	}
	
	/**
	 * Start a Quartz job to process the XML ContentResource data
	 * 
	 * @param id the Reference id that identifies the ContentResource 
	 * @throws JobExecutionException
	 */
	protected void processInQuartz(String id) throws JobExecutionException {
		
		Scheduler scheduler;
		
		//pass Reference's id in job detail and schedule job to run
		JobDetail jobDetail = JobBuilder.newJob(evalImportJob.getClass())
				.withIdentity("EvalImportJob", Scheduler.DEFAULT_GROUP)
				.usingJobData("ID", id)
				.usingJobData("CURRENT_USER", commonLogic.getCurrentUserId())
				.build();
		
		//job name + group should be unique
		String jobGroup = EvalUtils.makeUniqueIdentifier(20); //idManager.createUuid();
		
		//associate a trigger with the job
		Trigger trigger = TriggerBuilder.newTrigger()
				.withIdentity("EvalImportTrigger", jobGroup)
				.startNow()
				.build();
		try
		{
			//get a scheduler instance from the factory
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			
			//associate job with schedule
			scheduler.scheduleJob(jobDetail, trigger);
			
			//start the scheduler
			scheduler.start();
		}
		catch(SchedulerException e)
		{
			throw new JobExecutionException(e);
		}
	}
}
