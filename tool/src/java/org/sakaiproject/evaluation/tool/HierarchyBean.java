package org.sakaiproject.evaluation.tool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class HierarchyBean {
	
	public String nodeId;
	public String userId;
	public String userEid;
	public String selectedUserIndex;
	public Map<String, Boolean> permsMap = new HashMap<String, Boolean>();
	
	private EvalCommonLogic commonLogic;
	public void setCommonLogic(EvalCommonLogic commonLogic) {
		this.commonLogic = commonLogic;
	}
	
	private ExternalHierarchyLogic hierarchyLogic;
	public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
		this.hierarchyLogic = hierarchyLogic;
	}
	
	private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
        this.messages = messages;
    }
	
	public void addUser() {
		
		// check that both nodeId and userEid are not null or empty
		if ((this.nodeId == null) || (this.nodeId.trim().length() == 0))
			throw new IllegalArgumentException("nodeId must be specified");
		
		if ((this.userEid == null) || (this.userEid.trim().length() == 0)) {
			messages.addMessage(new TargettedMessage("modifynodeperms.error.no.user.eid", new Object[] {}, TargettedMessage.SEVERITY_ERROR));
			return;
		}
		
		// get userId for the specified userEid (i.e. username)
		String newUserId = commonLogic.getUserId(this.userEid);
		
		if (newUserId == null) {
			
			// check to see if it's an email address
			EvalUser user = commonLogic.getEvalUserByEmail(this.userEid);
			this.userEid = user.username;
			newUserId = user.userId;
			
			// if invalid or anonymous, display error message and return
			if ((user.type.equals(EvalUser.USER_TYPE_INVALID)) || (user.type.equals(EvalUser.USER_TYPE_ANONYMOUS))) {
				messages.addMessage(new TargettedMessage("modifynodeperms.error.no.user.eid", new Object[] {}, TargettedMessage.SEVERITY_ERROR));
				return;
			}
			
		}
		
		// check that user does not already have permission for this node
		Set<String> existingUserPerms = hierarchyLogic.getPermsForUserNodes(newUserId, new String[] { this.nodeId });
		
		if (!existingUserPerms.isEmpty()) {
			messages.addMessage(new TargettedMessage("modifynodeperms.error.user.has.perm", new Object[] { userEid }, TargettedMessage.SEVERITY_ERROR));
			return;
		}
		
		Map<String, Boolean> selectedUserPermsMap = this.getSelectedUserPermsMap();
		
		// create a set of the values (will contain at most two elements) and check if at least one perm was selected (i.e. true is in set)
		Set<Boolean> permValueSet = new HashSet<Boolean>(selectedUserPermsMap.values());
		
		if (!permValueSet.contains(Boolean.TRUE)) {
			messages.addMessage(new TargettedMessage("modifynodeperms.error.no.perms.selected", new Object[] {}, TargettedMessage.SEVERITY_ERROR));
			return;
		}
		
		// set permissions for user
		for (Map.Entry<String, Boolean> entry : selectedUserPermsMap.entrySet()) {
			
			String permKey = entry.getKey();
			Boolean permValue = entry.getValue();
			
			// Note: we know that other permissions are not already assigned being as it is a new user
			if (permValue)
				hierarchyLogic.assignUserNodePerm(newUserId, this.nodeId, permKey, false);
			
		}
		
		// display success message
		messages.addMessage(new TargettedMessage("modifynodeperms.success.user.added", new Object[] { this.userEid }, TargettedMessage.SEVERITY_INFO));
		
	}
	
	public void removeUser() {

		// check that both nodeId and userId are not null or empty
		if ((this.nodeId == null) || (this.nodeId.trim().length() == 0))
			throw new IllegalArgumentException("nodeId must be specified");
		
		if ((this.selectedUserIndex == null) || (this.selectedUserIndex.trim().length() == 0))
			throw new IllegalArgumentException("selectedUserIndex must be specified");
		
		String userEid = commonLogic.getUserUsername(this.userId);
		
		// get all permissions this user has for this node and remove them
		Set<String> userPerms = hierarchyLogic.getPermsForUserNodes(this.userId, new String[] { this.nodeId });
		
		for (String perm : userPerms) {
			hierarchyLogic.removeUserNodePerm(this.userId, this.nodeId, perm, false);
		}
		
		// display success message
		messages.addMessage(new TargettedMessage("modifynodeperms.success.user.removed", new Object[] { userEid }, TargettedMessage.SEVERITY_INFO));
		
	}
	
	public void savePermissions() {
		
		// check that both nodeId and userId are not null or empty
		if ((this.nodeId == null) || (this.nodeId.trim().length() == 0))
			throw new IllegalArgumentException("nodeId must be specified");
		
		if ((this.userId == null) || (this.userId.trim().length() == 0))
			throw new IllegalArgumentException("userId must be specified");
		
		if ((this.selectedUserIndex == null) || (this.selectedUserIndex.trim().length() == 0))
			throw new IllegalArgumentException("selectedUserIndex must be specified");
		
		this.userEid = commonLogic.getUserUsername(this.userId);
		Map<String, Boolean> selectedUserPermsMap = this.getSelectedUserPermsMap();
		
		// create a set of the values (will contain at most two elements) and check if at least one perm was selected (i.e. true is in set)
		Set<Boolean> permValueSet = new HashSet<Boolean>(selectedUserPermsMap.values());
		
		if (!permValueSet.contains(Boolean.TRUE)) {
			messages.addMessage(new TargettedMessage("modifynodeperms.error.no.perms.selected", new Object[] {}, TargettedMessage.SEVERITY_ERROR));
			return;
		}
		
		// set permissions for user
		for (Map.Entry<String, Boolean> entry : selectedUserPermsMap.entrySet()) {
			
			String permKey = entry.getKey();
			Boolean permValue = entry.getValue();
			
			// if true, assign permission; if false, remove permission
			if (permValue)
				hierarchyLogic.assignUserNodePerm(userId, this.nodeId, permKey, false);
			else
				hierarchyLogic.removeUserNodePerm(userId, this.nodeId, permKey, false);
			
		}
		
		// display success message
		messages.addMessage(new TargettedMessage("modifynodeperms.success.perms.saved", new Object[] { this.userEid }, TargettedMessage.SEVERITY_INFO));
		
	}
	
	private Map<String, Boolean> getSelectedUserPermsMap() {
		
		Map<String, Boolean> selectedUserPermsMap = new HashMap<String, Boolean>();
		
		for (String perm : EvalToolConstants.HIERARCHY_PERM_VALUES) {
			String permKey = perm + "-" + this.selectedUserIndex;
			selectedUserPermsMap.put(perm, this.permsMap.get(permKey));
		}
		
		return selectedUserPermsMap;
		
	}
	
}
