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

import org.sakaiproject.evaluation.providers.EvalGroupsProvider;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * View params for use with the {@link EvalGroupsProvider} test page
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class AdminTestEGViewParameters extends SimpleViewParameters {

	public String username;
	public String evalGroupId;

    public AdminTestEGViewParameters() {}

	public AdminTestEGViewParameters(String viewID, String username, String evalGroupId) {
        this.viewID = viewID;
        this.username = username;
        this.evalGroupId = evalGroupId;
    }
	
}
