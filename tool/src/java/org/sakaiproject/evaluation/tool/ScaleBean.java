/******************************************************************************
 * ScaleBean.java - created by kahuja@vt.edu on Mar 04, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This request-scope bean handles scale creation and modification.
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 */
public class ScaleBean {

	private static Log log = LogFactory.getLog(ScaleBean.class);
	public Long scaleId;

	private ScaleBeanLocator scaleBeanLocator;
	public void setScaleBeanLocator(ScaleBeanLocator scaleBeanLocator) {
		this.scaleBeanLocator = scaleBeanLocator;
	}

	public String saveScale() {
		log.debug("create scale");
		scaleBeanLocator.saveAll();
		return "success";
	}
	
	public String deleteScale() {
		log.debug("delete scale");
		scaleBeanLocator.deleteScale(scaleId);
		return "success";
	}
}