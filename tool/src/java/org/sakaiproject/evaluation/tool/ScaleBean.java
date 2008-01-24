/**
 * ScaleBean.java - evaluation - Mar 04, 2007 11:35:56 AM - kahuja
 * $URL: https://source.sakaiproject.org/contrib $
 * $Id: Locator.java 11234 Jan 21, 2008 11:35:56 AM azeckoski $
 **************************************************************************
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
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
import org.sakaiproject.evaluation.tool.locators.ScaleBeanLocator;

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

	public String saveScaleAction() {
		log.debug("create scale");
		scaleBeanLocator.saveAll();
		return "success";
	}
	
	public String deleteScaleAction() {
		log.debug("delete scale");
		scaleBeanLocator.deleteScale(scaleId);
		return "success";
	}
}