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
 
package org.sakaiproject.evaluation.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.evaluation.logic.externals.EvalExportLogic;
import org.sakaiproject.evaluation.tool.producers.EvaluationSettingsProducer;
import org.sakaiproject.evaluation.tool.producers.SummaryProducer;

/**
 * This is the backing bean of the response export process.
 * 
 * @author rwellis
 */
public class ExportBean {
	
	private static Log log = LogFactory.getLog(ExportBean.class);
	
	//injection
	private EvalExportLogic evalExportLogic;
	public void setEvalExportLogic(EvalExportLogic evalExportLogic) {
		this.evalExportLogic = evalExportLogic;
	}
	/* TODO use ContentHostingService through EvalExternalLogic */
	private ContentHostingService contentHostingService;
	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}
	
	public void init() {
		log.debug("INIT");
	}
	
	/**
	 * Email a PDF ContentResource to each instructor
	 * 
	 * @return String that is used to determine the place where control is to be sent
	 * 			in ControlExportProducer (reportNavigationCases method)
	 * @throws SecurityException 
	 */
	public String emailContentResource() {
		//TODO is the current user an admin?
		if(log.isDebugEnabled())
			log.debug("ExportBean.emailContentResource()");
		
		//TODO add selection criteria for qualifying which responses
		evalExportLogic.emailAllResponses();
		return EvaluationSettingsProducer.VIEW_ID;
	}
	
	/**
	 * Write an XML ContentResource sans comments to Resources
	 * suitable for data transfer and analysis elsewhere.
	 * 
	 * @return String that is used to determine the place where control is to be sent
	 * 			in ControlExportProducer (reportNavigationCases method)
	 * @throws SecurityException 
	 */
	public String writeContentResource() throws SecurityException {
		
		//TODO is the current user an admin?
		if(log.isDebugEnabled())
			log.debug("ExportBean.writeContentResource()");
		
		//TODO add selection criteria for qualifying which responses
		evalExportLogic.writeAllResponses();
		return EvaluationSettingsProducer.VIEW_ID;
	}
	/**
	 * Clear the exclusive lock on export processing held in EvalExportLogicImpl
	 * to prevent re-posting during a long-running export.
	 * 
	 * @return String that is used to determine the place where control is to be sent
	 * 			in ControlExportProducer (reportNavigationCases method)
	 * @throws SecurityException 
	 */
	public String clearLock() throws SecurityException {
		//TODO is the current user an admin?
		if(log.isDebugEnabled())
			log.debug("ExportBean.clearLock()");
		
		evalExportLogic.setLock(false);
		return EvaluationSettingsProducer.VIEW_ID;
	}
}
