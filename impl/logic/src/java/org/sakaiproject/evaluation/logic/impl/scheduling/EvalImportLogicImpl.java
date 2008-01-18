/**********************************************************************************
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

package org.sakaiproject.evaluation.logic.impl.scheduling;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.evaluation.logic.externals.EvalImportJob;
import org.sakaiproject.evaluation.logic.externals.EvalImportLogic;
import org.sakaiproject.evaluation.logic.externals.EvalImport;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;

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
	
	private static final Log log = LogFactory.getLog(EvalImportLogicImpl.class);
	
	private EvalImport evalImport;
	public void setEvalImport(EvalImport evalImport) {
		this.evalImport = evalImport;
	}
	
	private EvalImportJob evalImportJob;
	public void setEvalImportJob(EvalImportJob evalImportJob) {
		this.evalImportJob = evalImportJob;
	}
	
	private IdManager idManager;
	public void setIdManager(IdManager idManager) {
		this.idManager = idManager;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
	
	String currentUserId = null;
	String jobName = null;
	String qrtzImport;
	String threadImport;
	
	public void init() {
		
	}
	
	public EvalImportLogicImpl() {
		
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalImportLogic#load(java.lang.String)
	 */
	public List<String> load(String id) {
		List<String> messages = new ArrayList<String>();
		currentUserId = sessionManager.getCurrentSessionUserId();
		try
		{
			threadImport = serverConfigurationService.getString("eval.threadImport", "false");
			qrtzImport = serverConfigurationService.getString("eval.qrtzImport", "false");
			if(qrtzImport.equalsIgnoreCase("true")) {
				processInQuartz(id);
			}
			else if(threadImport.equalsIgnoreCase("true")) {
				
				//process in a new thread
				processInThread(id, currentUserId);
			}
			else {
				//process in interactive thread
				messages = evalImport.process(id, currentUserId);
			}
		}
		catch(Exception e)
		{
			if(log.isWarnEnabled())
				log.warn(e);
			messages.add("There was a problem loading the data: " + e.toString());
			
		}
		return messages;
	}
	
	protected boolean processInThread(String id, String currentUserId) {
		boolean retVal = false;
		try {
			Import i = new Import(id, currentUserId);
			Thread t = new Thread(i);
			t.start();
			retVal = true;
		}
		catch (Exception e) {
			throw new RuntimeException("error importing XML data");
		}
		return retVal;
	}
	
	/** Class that starts a session to import XML data. */
	protected class Import implements Runnable
	{
		public void init(){}
		public void start(){}
		
		private String m_id = null;
		private String m_currentUserId = null;
		
		//constructor
		Import(String id, String currentUserId)
		{
			m_id = id;
			m_currentUserId = currentUserId;
		}

		public void run()
		{
		    try
			{
				// set the current user to admin
				Session s = sessionManager.getCurrentSession();
				if (s != null)
				{
					s.setUserId(userDirectoryService.ADMIN_ID);
				}
				else
				{
					log.error("Import: Session s is null, cannot set user id to ADMIN_ID user");
				}

				if(m_id != null)
				{
					evalImport.process(m_id, m_currentUserId);
				}
			}
		    catch(Exception e) {
		
		    }
		    finally
			{
				//clear any current bindings
				ThreadLocalManager.clear();
			}
		}
		
	}//Snapshot
	
	/**
	 * Start a Quartz job to process the XML ContentResource data
	 * 
	 * @param id the Reference id that identifies the ContentResource 
	 * @throws JobExecutionException
	 */
	protected void processInQuartz(String id) throws JobExecutionException {
		
		Scheduler scheduler = null;
		
		//pass Reference's id in job detail and schedule job to run
		JobDetail jobDetail = new JobDetail("EvalImportJob",
				Scheduler.DEFAULT_GROUP, evalImportJob.getClass());
		JobDataMap jobDataMap = jobDetail.getJobDataMap();
		jobDataMap.put("ID", (String)id);
		jobDataMap.put("CURRENT_USER", sessionManager.getCurrentSessionUserId());
		
		//job name + group should be unique
		String jobGroup = idManager.createUuid();
		
		//associate a trigger with the job
		SimpleTrigger trigger = new SimpleTrigger("EvalImportTrigger", jobGroup, new Date());
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
