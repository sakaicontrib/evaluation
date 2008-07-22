/**
 * ImportBean.java - evaluation - 9 Mar 2007 11:35:56 AM - rwellis
 * $URL$
 * $Id$
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.imports.EvalImportLogic;

/**
 * This is the backing bean of the XML data import process.
 * 
 * @author Dick Ellis (rwellis@umich.edu)
 */
public class ImportBean {
	
	private static Log log = LogFactory.getLog(ImportBean.class);
	
	//injection
	private EvalImportLogic evalImportLogic;
	public void setEvalImportLogic(EvalImportLogic evalImportLogic) {
		this.evalImportLogic = evalImportLogic;
	}
	
	private EvalExternalLogic externalLogic;
	public void setExternalLogic(EvalExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}
	
	/**
	 * Parse and load selected XML content resource
	 * 
	 * @return String that is used to determine the place where control is to be sent
	 * 			in ControlImportProducer (reportNavigationCases method)
	 * @throws SecurityException 
	 */
	public String process() throws SecurityException {

		// get content resource id
		String id = externalLogic.getImportedResourceId();
		
		// process the content in a separate thread
		String navigationCase = evalImportLogic.load(id);

		return navigationCase; //"importing" or "exception"
	}
	
	/*
	 * INITIALIZATION
	 */
	public void init() {
		log.debug("INIT");
	}
}

