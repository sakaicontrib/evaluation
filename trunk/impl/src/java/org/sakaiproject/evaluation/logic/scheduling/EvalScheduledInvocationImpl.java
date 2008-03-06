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
package org.sakaiproject.evaluation.logic.scheduling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.externals.EvalJobLogic;
import org.sakaiproject.evaluation.logic.externals.EvalScheduledInvocation;

/**
 * This class simply calls a method in EvalJobLogic 
 * when it is run by the ScheduledInvocationManager.
 * 
 * @author rwellis
 *
 */
public class EvalScheduledInvocationImpl implements EvalScheduledInvocation {
	
	private static Log log = LogFactory.getLog(EvalScheduledInvocationImpl.class);
	
	private EvalJobLogic evalJobLogic;
	public void setEvalJobLogic(EvalJobLogic evalJobLogic) {
		this.evalJobLogic = evalJobLogic;
	}
	
	public EvalScheduledInvocationImpl() {
	}
	
	public void init() {
	}

	/**
	 * execute is the only method of a ScheduledInvocationCommand
	 * 
	 * @param opaqueContext a String that can be decoded to do determine what to do
	 */
	public void execute(String opaqueContext) {
		
		try {
		
			if(opaqueContext == null || opaqueContext.equals("")) {
				log.warn(this + " opaqueContext is null or empty");
				return;
			}
			
			if(log.isDebugEnabled())
				log.debug("EvalScheduledInvocationImpl.execute(" + opaqueContext + ")");
			
			/*
			 *	opaqueContext provides evaluation id and job type.
			 */
			String[] parts = opaqueContext.split("/");
			if(parts.length != 2) {
				log.warn(this + " opaqueContext parts != 2 " + opaqueContext);
			}
			String id = parts[0];
			Long evalId = Long.valueOf(id);
			String jobType = parts[1];
			
			//call method to fix state, send email and/or schedule a job
			evalJobLogic.jobAction(evalId, jobType);
		}
		catch(Exception e) {
			log.error(this + ".execute(" + opaqueContext + ") " + e);
		}
	}
}

