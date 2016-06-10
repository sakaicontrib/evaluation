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
package org.sakaiproject.evaluation.tool;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class HierarchyBean {

	private static final Log LOG = LogFactory.getLog( HierarchyBean.class );

	public String nodeId;
	public String userId;
	public String userEid;
	public String selectedUserIndex;
	public Map<String, Boolean> permsMap = new HashMap<>();

	// Hierarchy rule bindings
	public Map<String, String> existingQualifierSelections = new HashMap<>();
	public Map<String, String> existingOptionSelections = new HashMap<>();
	public Map<String, String> existingRuleTexts = new HashMap<>();
	public String ruleID;
	public String newRuleText;
	public String newQualifierSelection;
	public String newOptionSelection;

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

	/**
	 * Utility method to check form fields
	 * 
	 * @param checkRuleIndexAndRuleID - toggle to check rule index and rule ID from the form
	 */
	private void checkRuleParams( boolean checkRuleID )
	{
		// Check that the node ID is supplied
		if( StringUtils.isBlank( nodeId ) )
		{
			throw new IllegalArgumentException( "nodeId must be specified" );
		}

		// Check that the ruleID is supplied, if desired
		if( checkRuleID )
		{
			if( StringUtils.isBlank( ruleID ) )
			{
				throw new IllegalArgumentException( "ruleID must be specified" );
			}
		}
	}

	/**
	 * Utility method to check binding values and form fields
	 * 
	 * @param optionSelection - option bound value
	 * @param qualifierSelection - qualifier bound value
	 * @param ruleText - rule text bound value
	 * @param checkRuleID - rule ID bound value
	 * @return true/false (true=missing values/fields, false=everything is good)
	 */
	private boolean checkRuleValues( String optionSelection, String qualifierSelection, String ruleText, boolean checkRuleID )
	{
		// Check all rule form params
		checkRuleParams( checkRuleID );

		// Check that the siteSectionSelection is supplied
		boolean msgsAdded = false;
		if( StringUtils.isBlank( optionSelection ) )
		{
			messages.addMessage( new TargettedMessage( "modifynoderules.error.no.siteOrSectionSelection.text", new Object[] {}, TargettedMessage.SEVERITY_ERROR ) );
			msgsAdded = true;
		}

		// Check that the qualifierSelection is supplied
		if( StringUtils.isBlank( qualifierSelection ) )
		{
			messages.addMessage( new TargettedMessage( "modifynoderules.error.no.qualifier.text", new Object[] {}, TargettedMessage.SEVERITY_ERROR ) );
			msgsAdded = true;
		}

		// Check that the ruleText is supplied if it's a check on existing rule, or newRuleText if it's a check on a new rule
		if( StringUtils.isBlank( ruleText ) )
		{
			messages.addMessage( new TargettedMessage( "modifynoderules.error.no.rule.text", new Object[] {}, TargettedMessage.SEVERITY_ERROR ) );
			msgsAdded = true;
		}

		return msgsAdded;
	}

	/**
	 * This method handles the remove rule request from the form
	 */
	public void removeRule()
	{
		// Check all rule form params; check all rule form values
		checkRuleParams( true );

		// Get the text of the rule to be removed
		String existingRuleText = existingRuleTexts.get( ruleID );

		// Make the call to remove the rule from the DB, display a success or fail message
		try
		{
			hierarchyLogic.removeNodeRule( Long.parseLong( ruleID ) );
		}
		catch( Exception ex )
		{
			messages.addMessage( new TargettedMessage( "modifynoderules.fail.rule.removed", new Object[] { existingRuleText }, TargettedMessage.SEVERITY_INFO ) );
			LOG.warn( ex );
		}

		messages.addMessage( new TargettedMessage( "modifynoderules.success.rule.removed", new Object[] { existingRuleText }, TargettedMessage.SEVERITY_INFO ) );
	}

	/**
	 * This method handles the save (update) rule request from the form
	 */
	public void saveRule()
	{
		// Check all rule form params; check all rule form values
		checkRuleParams( true );

		// Get/check the values the user wants to change
		String existingOptionSelection = existingOptionSelections.get( ruleID );
		String existingQualifierSelection = existingQualifierSelections.get( ruleID );
		String existingRuleText = existingRuleTexts.get( ruleID );
		if( checkRuleValues( existingOptionSelection, existingQualifierSelection, existingRuleText, false ) )
		{
			return;
		}

		// Make the call to update the rule in the DB, display a success or fail message
		try
		{
			hierarchyLogic.updateNodeRule( Long.parseLong( ruleID ), existingRuleText, existingQualifierSelection, existingOptionSelection, Long.parseLong( nodeId ) );
		}
		catch( Exception ex )
		{
			messages.addMessage( new TargettedMessage( "modifynoderules.fail.rule.updated", new Object[] { existingRuleText }, TargettedMessage.SEVERITY_INFO ) );
			LOG.warn( ex );
		}

		messages.addMessage( new TargettedMessage( "modifynoderules.success.rule.updated", new Object[] { existingRuleText }, TargettedMessage.SEVERITY_INFO ) );
	}

	/**
	 * This method handles the add (new) rule request from the form
	 */
	public void addRule()
	{
		// Check all rule form params except ruleID and selectedRuleIndex; check all rule form values
		if( checkRuleValues( newOptionSelection, newQualifierSelection, newRuleText, false ) )
		{
			return;
		}

		// Check to see if this rule is already in place for the given node
		if( hierarchyLogic.isRuleAlreadyAssignedToNode( newRuleText, newQualifierSelection, newOptionSelection, Long.parseLong( nodeId ) ) )
		{
			messages.addMessage( new TargettedMessage( "modifynoderules.error.rule.exists.text", new Object[] { newRuleText }, TargettedMessage.SEVERITY_INFO ) );
		}
		else
		{
			// Make the call to create the rule in the DB, display a success or fail message
			try
			{
				hierarchyLogic.assignNodeRule( newRuleText, newQualifierSelection, newOptionSelection, Long.parseLong( nodeId ) );
			}
			catch( Exception ex )
			{
				messages.addMessage( new TargettedMessage( "modifynoderules.fail.rule.added", new Object[] { newRuleText }, TargettedMessage.SEVERITY_INFO ) );
				LOG.warn( ex );
			}

			messages.addMessage( new TargettedMessage( "modifynoderules.success.rule.added", new Object[] { newRuleText }, TargettedMessage.SEVERITY_INFO ) );
		}
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
		Set<Boolean> permValueSet = new HashSet<>(selectedUserPermsMap.values());
		
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
		Set<Boolean> permValueSet = new HashSet<>(selectedUserPermsMap.values());
		
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
		
		Map<String, Boolean> selectedUserPermsMap = new HashMap<>();
		
		for (String perm : EvalToolConstants.HIERARCHY_PERM_VALUES) {
			String permKey = perm + "-" + this.selectedUserIndex;
			selectedUserPermsMap.put(perm, this.permsMap.get(permKey));
		}
		
		return selectedUserPermsMap;
		
	}
	
}
