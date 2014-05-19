package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Map;

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
	
	private Map<String, EvalAssignGroupSelection> localStore = new HashMap<String, EvalAssignGroupSelection>();
	
	public Object locateBean(String name) {
		String checkName = "/site/" + name;
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
