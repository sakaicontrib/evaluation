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

package org.sakaiproject.evaluation.logic.impl.scheduling;

import org.quartz.Scheduler;
import org.sakaiproject.evaluation.logic.externals.JobSchedulerFacade;
import org.sakaiproject.api.app.scheduler.SchedulerManager;

/**
 * An implementation of JobSchedulerFacade using Sakaia's ScheduleManager.
 * This allows us to keep Evaluation job state in the QRTZ_* tables used
 * for the jobscheduler project and handle Spring injection more easily.
 * 
 * @author rwellis
 *
 */
public class JobSchedulerSakaiImpl implements JobSchedulerFacade {
	
	private SchedulerManager schedulerManager = null;
	
	//Spring injection
	public SchedulerManager getSchedulerManager()
	{
		return this.schedulerManager;
	}
	public void setSchedulerManager(SchedulerManager scheduleManager) {
		this.schedulerManager = scheduleManager;
	}
	
	public void init() {
	}
	
	/**
	 * No argument constructor
	 */
	public JobSchedulerSakaiImpl() {
		
	}
	
	/**
	 * Get the ScheduleManager's Quartz scheduler.
	 * @return the Sakai job scheduler.
	 */
	public Scheduler getScheduler() {
		return schedulerManager.getScheduler();
	}
}
