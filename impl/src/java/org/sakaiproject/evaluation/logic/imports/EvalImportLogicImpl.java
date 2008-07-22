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

import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.imports.EvalImport;
import org.sakaiproject.evaluation.logic.imports.EvalImportLogic;
import org.sakaiproject.tool.api.Session;

/**
* Importing external data into the Evaluation System.
* Process in a thread in the background and periodically 
* set the thread's session active to avoid timing out.
* 
* @author rwellis
*/
public class EvalImportLogicImpl implements EvalImportLogic {
	
	private static final Log log = LogFactory.getLog(EvalImportLogicImpl.class);
	
	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	private EvalImport evalImport;
	public void setEvalImport(EvalImport evalImport) {
		this.evalImport = evalImport;
	}

	public void init() {
	}
	
	public EvalImportLogicImpl() {
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.externals.EvalImportLogic#load(java.lang.String)
	 */
	public String load(String id) {
		List<String> messages = new ArrayList<String>();
		String currentUserId = externalLogic.getCurrentUserId();
		// check for permission to read the resource
		if(externalLogic.checkResource(id)) {
			try {
				//process in a new thread
				processInThread(id, currentUserId);
			}
			catch(Exception e) {
				if(log.isWarnEnabled())
					log.warn(e);
				return "exception";
			}
			return "importing";
		}
		else return "exception";
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
		    	externalLogic.setSessionUserIdAdmin(m_currentUserId);
				/* set the current user to admin
				Session s = sessionManager.getCurrentSession();
				if (s != null)
				{
					s.setUserId(userDirectoryService.ADMIN_ID);
				}
				else
				{
					Log.warn("chef", this + ".run() - Session is null, cannot set user id to ADMIN_ID user");
				}
		    	*/
		    	
				if(m_id != null) {
					evalImport.process(m_id, m_currentUserId);
				}
			}
		    catch(Exception e) {
		    	throw new RuntimeException("Import exception " + e);
		    }
		    finally
			{
				//clear any current bindings
		    	externalLogic.clearBindings();
			}
		}
	}
}

