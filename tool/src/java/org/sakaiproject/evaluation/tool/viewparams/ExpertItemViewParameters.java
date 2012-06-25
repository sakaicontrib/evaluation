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

/**
 * View params for use with the expert items views
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ExpertItemViewParameters extends TemplateItemViewParameters {

	public Long categoryId;
	public Long objectiveId;

	public ExpertItemViewParameters() {}

	public ExpertItemViewParameters(String viewID, Long templateId, Long categoryId, Long objectiveId) {
		this.viewID = viewID;
		this.templateId = templateId;
		this.categoryId = categoryId;
		this.objectiveId = objectiveId;
	}
	
}
