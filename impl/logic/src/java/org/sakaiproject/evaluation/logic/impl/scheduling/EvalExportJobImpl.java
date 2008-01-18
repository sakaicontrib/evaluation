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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.evaluation.logic.externals.EvalExport;
import org.sakaiproject.tool.api.SessionManager;

import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Deprecated after U-M Pilot I, replaced by Oracle DBLINK in Pilot II
 * @author rwellis
 *
 */
public class EvalExportJobImpl extends QuartzJobBean{
	
	/*
	 * 
	 * Note that as of Spring 2.0 and Quartz 1.5, the preferred way to apply dependency injection to Job instances 
	 * is via a JobFactory:  that is, to specify SpringBeanJobFactory as Quartz JobFactory (typically via 
	 * SchedulerFactoryBean.setJobFactory(org.quartz.spi.JobFactory) SchedulerFactoryBean's "jobFactory" property}). 
	 * This allows to implement dependency-injected Quartz Jobs without a dependency on Spring base classes.
	 */
	
	private static final Log log = LogFactory.getLog(EvalExportJobImpl.class);
	
	//Spring injection
	private EvalExport evalExport;
	public void setEvalExport(EvalExport evalExport) {
		this.evalExport = evalExport;
	}
	private SessionManager sessionManager;
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	public void init() {
		
	}

	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		
	}

}
