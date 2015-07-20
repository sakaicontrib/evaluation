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
 * This is a view parameters class which defines the variables that are passed
 * from one page to another, for a simple view which is centered on a particular
 * EvalTemplate object within the evaluation system.
 */
public class TemplateViewParameters extends BaseViewParameters {

	public Long templateId;
	public Long templateItemId;

	public TemplateViewParameters() {
	}

	public TemplateViewParameters(String viewID, Long templateId) {
		this.viewID = viewID;
		this.templateId = templateId;
	}
	
	public TemplateViewParameters(String viewID, Long templateId, Long templateItemId) {
		this.viewID = viewID;
		this.templateId = templateId;
		this.templateItemId = templateItemId;
	}

}
