/******************************************************************************
 * ReportsBean.java - created by whumphri@vt.edu on Jan 16, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Will Humphries (whumphri@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This request-scope bean handles report generation.
 * 
 * @author Will Humphries (whumphri@vt.edu)
 */
public class ReportsBean {

	private static Log log = LogFactory.getLog(ReportsBean.class);

	public Map groupIds = new HashMap();
	public Long evalId;

	public ReportsBean() {	}

	public void init() {
		log.debug("INIT");
	}

	public String chooseGroupsAction() {
		return "success";
	}

}