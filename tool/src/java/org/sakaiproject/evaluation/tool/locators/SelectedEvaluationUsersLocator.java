package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Map;

import uk.org.ponder.beanutil.BeanLocator;
/**
 * 
* This is not a true locator of a persistant object but exists to resolve
* Multiple potential mappings between Instructors and TA's to be evaluated and evaluation groups 
*
* @author dhorwitz
*
*/
public class SelectedEvaluationUsersLocator implements BeanLocator {
	
	private Map<String, EvaluationUserSelection> localStore = new HashMap<String, EvaluationUserSelection>();
	
	public Object locateBean(String name) {
		String checkName = "/site/" + name;
		if (localStore.containsKey(checkName)) {
			return localStore.get(checkName); 
		} else {
			//these should always exist
			EvaluationUserSelection ret = new EvaluationUserSelection();
			localStore.put(checkName, ret);
			return ret;
		}
		
	}

	public String[] getOrderingForInstructors(String groupId) {
		if (localStore.containsKey(groupId)) {
			EvaluationUserSelection thisSelection = localStore.get(groupId);
			return thisSelection.orderingInstructors;
		}
		return null;
	}
	

	public String[] getOrderingForAssistants(String groupId) {
		if (localStore.containsKey(groupId)) {
			EvaluationUserSelection thisSelection = localStore.get(groupId);
			return thisSelection.orderingAssistants;
		} 
		return null;
	}
	
	public String[] getDeselectedInstructors(String groupId) {
		if (localStore.containsKey(groupId)) {
			EvaluationUserSelection thisSelection = localStore.get(groupId);
			return thisSelection.deselectedInstructors;
		}
		return null;
	}
	

	public String[] getDeselectedAssistants(String groupId) {
		if (localStore.containsKey(groupId)) {
			EvaluationUserSelection thisSelection = localStore.get(groupId);
			return thisSelection.deselectedAssistants;
		} 
		return null;
	}
	
	public class EvaluationUserSelection {
		
		private String id;
		public String[] deselectedInstructors;
		public String[] deselectedAssistants;
		
		// Save ordering for selected roles EVALSYS-822
		public String[] orderingInstructors;
		public String[] orderingAssistants;
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		
		
		
	}
	
}
