/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.tool.viewparams;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * 
 * AdminSearchViewParameters
 *
 */
public class AdminSearchViewParameters extends SimpleViewParameters 
{
	public String searchString = "";
	public int page = 0;
	public boolean searchGroups = false;

	public AdminSearchViewParameters()
	{
		super();
	}
	
	public AdminSearchViewParameters(String viewId) 
	{
		super(viewId);
	}

	public AdminSearchViewParameters(String viewId, String searchString, int page) 
	{
		super(viewId);
		this.searchString = searchString;
		this.page = page;
		this.searchGroups = false;
	}
	

	public AdminSearchViewParameters(String viewId, String searchString, int page, boolean searchGroups) 
	{
		super(viewId);
		this.searchString = searchString;
		this.page = page;
		this.searchGroups = searchGroups;
	}

}
