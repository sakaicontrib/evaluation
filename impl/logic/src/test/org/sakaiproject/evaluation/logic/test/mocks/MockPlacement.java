/******************************************************************************
 * TestPlacement.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2007 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.test.mocks;

import java.util.Properties;

import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;

/**
 * Test class for the Sakai Placement object<br/>
 * This has to be here since I cannot create a Placement object in Sakai for some 
 * reason... sure would be nice if I could though -AZ
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class MockPlacement implements Placement {

	private String id = "PLACE12345";
	private String context; // a.k.a. siteId
	private String title;
	private Tool tool;
	private String toolId;
	
	/**
	 * Construct a test Placement object with a evalGroupId (siteId) set
	 * @param evalGroupId a String representing a site evalGroupId (siteId)
	 */
	public MockPlacement(String context) {
		this.context = context;
	}


	public Properties getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getContext() {
		return context;
	}

	public String getId() {
		return id;
	}

	public Properties getPlacementConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitle() {
		return this.title;
	}

	public Tool getTool() {
		return tool;
	}

	public String getToolId() {
		return toolId;
	}

	public void save() {
		// TODO Auto-generated method stub
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTool(String toolId, Tool tool) {
		this.tool = tool;
		this.toolId = toolId;
	}

	public boolean equals(Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof MockPlacement)) return false;
		else {
			MockPlacement test = (MockPlacement) obj;
			if (null == this.getId() || null == test.getId()) return false;
			else return (this.getId().equals(test.getId()));
		}
	}
}
