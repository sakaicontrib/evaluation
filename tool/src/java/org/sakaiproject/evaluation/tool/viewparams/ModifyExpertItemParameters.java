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
 * EVALSYS-1026
 * Allows for passing of information needed for working with Expert Item Groups
 *
 * @author Rick Moyer (rmoyer@umd.edu)
 */

public class ModifyExpertItemParameters extends SimpleViewParameters {
	public Long categoryId;
	public Long objectiveId;
	public String type;
	public boolean isNew;
    
    public ModifyExpertItemParameters() {
    }
    
    public ModifyExpertItemParameters(String viewID, Long categoryId, Long objectiveId, String type, boolean isNew) {
        this.viewID = viewID;
        this.categoryId = categoryId;
    	this.objectiveId = objectiveId;
    	this.type = type;
    	this.isNew = isNew;
    }
}
