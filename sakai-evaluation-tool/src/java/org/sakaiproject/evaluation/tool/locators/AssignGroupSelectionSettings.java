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
package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Map;
import org.sakaiproject.evaluation.constant.EvalConstants;

import uk.org.ponder.beanutil.BeanLocator;
/**
 * 
* This is not a true locator of a persistant object but exists to resolve
* Multiple potential mappings between eval assign groups and their corresponding selection settings
*
* @author dhorwitz
* @author lovemorenalube
*
*/
public class AssignGroupSelectionSettings implements BeanLocator {
	
	private Map<String, EvalAssignGroupSelection> localStore = new HashMap<>();
	
	public Object locateBean(String name) {
		String checkName = EvalConstants.GROUP_ID_SITE_PREFIX + name;
		if (localStore.containsKey(checkName)) {
			return localStore.get(checkName); 
		} else {
			//these should always exist
			EvalAssignGroupSelection ret = new EvalAssignGroupSelection();
			localStore.put(checkName, ret);
			return ret;
		}
		
	}

	public String getInstructorSetting(String groupId) {
		if (localStore.containsKey(groupId)) {
			EvalAssignGroupSelection thisSelection = localStore.get(groupId);
			return thisSelection.instructor;
		}
		return null;
	}
	

	public String getAssistantSetting(String groupId) {
		if (localStore.containsKey(groupId)) {
			EvalAssignGroupSelection thisSelection = localStore.get(groupId);
			return thisSelection.assistant;
		} 
		return null;
	}
	
	public class EvalAssignGroupSelection {
		
		private String id;
		public String instructor;
		public String assistant;
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		
		
		
	}
	
}
