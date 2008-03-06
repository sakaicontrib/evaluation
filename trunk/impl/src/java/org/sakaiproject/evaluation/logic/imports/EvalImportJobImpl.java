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

package org.sakaiproject.evaluation.logic.imports;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.evaluation.logic.imports.EvalImportJob;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * Process an XML ContentResource and save/update evaluation data using Quartz.
 * 
 * @author rwellis
 *
 */
public class EvalImportJobImpl implements EvalImportJob{
	
	private static final Log log = LogFactory.getLog(EvalImportJobImpl.class);
	
	//on demand injection of services
	private org.sakaiproject.evaluation.logic.imports.EvalImport evalImport = 
		(org.sakaiproject.evaluation.logic.imports.EvalImport) ComponentManager.get(org.sakaiproject.evaluation.logic.imports.EvalImport.class);
	private org.sakaiproject.tool.api.SessionManager sessionManager = 
		(org.sakaiproject.tool.api.SessionManager) ComponentManager.get(org.sakaiproject.tool.api.SessionManager.class);
	 // TODO Use actual injection here -AZ
	
	List results = new ArrayList(); // TODO NOT threadsafe, fix this -AZ
	String currentUserId = null; // TODO NOT threadsafe, fix this -AZ
	String jobName = null; // TODO NOT threadsafe, fix this -AZ
	 
	public void init() {
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		//TODO lock while executing?
		String id = null;
		try
		{
			Session s = sessionManager.getCurrentSession();
			if (s != null)
				s.setUserId(UserDirectoryService.ADMIN_ID);
			
			//job details 
			JobDetail jobDetail = context.getJobDetail();
			jobName = jobDetail.getName();
			JobDataMap dataMap = jobDetail.getJobDataMap();
			
			//job execution parameters
			id = (String)dataMap.get("ID");
			currentUserId = (String)dataMap.getString("CURRENT_USER");
			if(id == null || currentUserId == null) {
				//TODO add to audit trail
				throw new JobExecutionException("ContentResource id and/or current User id null");
			}
			
			//parse and persist the XML data
			results = evalImport.process(id, currentUserId);
			
			//TODO email results to current user
		}
		catch(Exception e) {
			//TODO add to audit trail
			log.error("job execution " + e);
		}
	}
}