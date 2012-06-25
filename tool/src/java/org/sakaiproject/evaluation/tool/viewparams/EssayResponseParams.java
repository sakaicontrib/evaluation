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
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu)
 */
public class EssayResponseParams extends BaseViewParameters {

	public Long evalId; 
	public Long itemId;
	
	/* Note:
	 * Made the groupsIds as a string array as currently view params
	 * only supports bounded collections. From:
	 * http://www2.caret.cam.ac.uk/rsfwiki/Wiki.jsp?page=ViewParameters
	 * 
	 * "ViewParameters are currently capable of dealing with serialisation 
	 * of a bounded collection of leaf types within its object tree 
	 * (Strings, Integers, Longs etc.). Before 1.0, the ViewParameters 
	 * system will be upgraded to support unbounded collections (Lists, 
	 * Maps, etc.) to allow use cases such as a dynamically sized set of 
	 * sorting table controls within a view, etc. "
	 * - kahuja (Apr 19th 2007)
	 */
	public String[] groupIds;

	public EssayResponseParams() {}

	public EssayResponseParams(String viewID, Long evalId, String[] groupIds) {
		this.viewID = viewID;
		this.evalId = evalId;
		this.groupIds = groupIds;
	}

	public EssayResponseParams(String viewID, Long evalId, Long itemId,  String[] groupIds) {
		this.viewID = viewID;
		this.evalId = evalId;
		this.itemId = itemId;
		this.groupIds = groupIds;
	}

}
