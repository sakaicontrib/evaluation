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
package org.sakaiproject.evaluation.tool.producers;

import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.externals.ExternalHierarchyLogic;
import org.sakaiproject.evaluation.logic.model.EvalHierarchyNode;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.tool.EvalToolConstants;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.HierarchyNodeParameters;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class ModifyHierarchyNodePermsProducer extends EvalCommonProducer implements ViewParamsReporter {
	
	public static final String VIEW_ID = "modify_hierarchy_node_perms";
	public String getViewID() {
		return VIEW_ID;
	}
	
	private EvalCommonLogic commonLogic;
	public void setCommonLogic(EvalCommonLogic commonLogic) {
		this.commonLogic = commonLogic;
	}
	
	private ExternalHierarchyLogic hierarchyLogic;
	public void setHierarchyLogic(ExternalHierarchyLogic hierarchyLogic) {
		this.hierarchyLogic = hierarchyLogic;
	}
	
	private NavBarRenderer navBarRenderer;
	public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}
	
	private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator messageLocator) {
    	this.messageLocator = messageLocator;
    }

	public void fill(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		
		String currentUserId = commonLogic.getCurrentUserId();
		boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
		
		if (!userAdmin) {
			// Security check and denial
			throw new SecurityException("Non-admin users may not access this locator");
		}

		/*
		 * top links here
		 */
		navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());
		
		HierarchyNodeParameters params = (HierarchyNodeParameters) viewparams;
		EvalHierarchyNode evalNode = hierarchyLogic.getNodeById(params.nodeId);
		
		// only getting user perms for one node, so just immediately get the entry for the one nodeId passed in
		Map<String, Set<String>> nodeUsersMap = hierarchyLogic.getUsersAndPermsForNodes(params.nodeId).get(params.nodeId);
		
		/*
		 * Page titles and instructions, top menu links and bread crumbs here
		 */
		UIInternalLink.make(tofill, "hierarchy-toplink", UIMessage.make("controlhierarchy.breadcrumb.title"), new SimpleViewParameters(ControlHierarchyProducer.VIEW_ID));
		UIMessage.make(tofill, "page-title", "modifynodeperms.breadcrumb.title");
		UIMessage.make(tofill, "node-info", "modifynodeperms.node.info", new Object[] { evalNode.title, evalNode.description });
		UIMessage.make(tofill, "instructions", "modifynodeperms.instructions");
		
		String actionBean = "hierarchyBean.";
		UIForm permsForm = UIForm.make(tofill, "perms-form");
		int curUserIndex = 0; // for the purpose of having unique branch ids

		// Rename cancel button; 'cancel' implies going back to previous interface, when in reality it's 'clearing changes'
		UICommand.make( permsForm, "cancel-changes-button1", UIMessage.make( "modifynodeperms.cancel.changes.button"  ));
		UICommand.make( permsForm, "cancel-changes-button2", UIMessage.make( "modifynodeperms.cancel.changes.button"  ));
		UIInternalLink.make( permsForm, "return-link1", UIMessage.make( "controlhierarchy.return.link" ),
				new HierarchyNodeParameters( ControlHierarchyProducer.VIEW_ID, null, params.expanded ) );
		UIInternalLink.make(tofill, "return-link2", UIMessage.make( "controlhierarchy.return.link" ),
				new HierarchyNodeParameters(ControlHierarchyProducer.VIEW_ID, null, params.expanded));

		UIInput.make(permsForm, "node-id", actionBean + "nodeId", evalNode.id);
		
		UIMessage.make(permsForm, "user-info-header", "modifynodeperms.user.info.header");
		UIMessage.make(permsForm, "perms-header", "modifynodeperms.perms.header");
		UIMessage.make(permsForm, "actions-header", "modifynodeperms.actions.header");
		
		for (Map.Entry<String, Set<String>> entry : nodeUsersMap.entrySet()) {
			
			String userId = entry.getKey();
			Set<String> permSet = entry.getValue();
			String sCurUserIndex = String.valueOf(curUserIndex);
			
			EvalUser user = commonLogic.getEvalUserById(userId);
			UIBranchContainer userBranch = UIBranchContainer.make(permsForm, "user:", sCurUserIndex);
			
			// render user info
			UIMessage.make(userBranch, "user-info", "modifynodeperms.user.info", new Object[] { user.displayName, user.username });
			//UIInput.make(userBranch, "user-id", actionBean + "userIds." + sCurUserIndex, user.userId);
			/*
			UISelect permsSelect = UISelect.makeMultiple(userBranch, "perms-select", new String[] {}, new String[] {}, actionBean + "perms.0", new String[] {});
			UIOutputMany.make(EvalToolConstants.HIERARCHY_PERM_LABELS);
			String permsSelectId = permsSelect.getFullID();
			List<String> permValues = new ArrayList<String>();
			List<String> permLabels = new ArrayList<String>();
			*/
			// render perm checkboxes
			for (int i = 0; i < EvalToolConstants.HIERARCHY_PERM_VALUES.length; i ++) {
				
				UIBranchContainer permBranch = UIBranchContainer.make(userBranch, "perm:", String.valueOf(i));
				String permValue = EvalToolConstants.HIERARCHY_PERM_VALUES[i];
				String permLabel = EvalToolConstants.HIERARCHY_PERM_LABELS[i];
				
				/*
				permValues.add(EvalToolConstants.HIERARCHY_PERM_VALUES[i]);
				permLabels.add(EvalToolConstants.HIERARCHY_PERM_LABELS[i]);
				
				UISelectChoice.make(permBranch, "perm-checkbox", permsSelectId, i);
				UISelectLabel.make(permBranch, "perm-label", permsSelectId, i);
				*/
				
				UIBoundBoolean permCheckbox = UIBoundBoolean.make(permBranch, "perm-checkbox", 
						actionBean + "permsMap." + permValue + "-" + sCurUserIndex, permSet.contains(permValue));
				permCheckbox.mustapply = true; // so that the value is forced through when there are no changes
				UIMessage.make(permBranch, "perm-label", permLabel);
				
			}
			
			//permsSelect.optionlist = UIOutputMany.make(permValues.toArray(new String[permValues.size()]));
			//permsSelect.optionnames = UIOutputMany.make(permLabels.toArray(new String[permLabels.size()]));
			
			// render action buttons
			UICommand saveButton = UICommand.make(userBranch, "save-changes-button", UIMessage.make("modifynodeperms.save.changes.button"), actionBean + "savePermissions");
			UICommand removeButton = UICommand.make(userBranch, "remove-user-button", UIMessage.make("modifynodeperms.remove.user.button"), actionBean + "removeUser");
			
			// add the hardcoded el binding to tell the backing bean which user this button corresponds to
			saveButton.parameters.add(new UIELBinding(actionBean + "selectedUserIndex", sCurUserIndex));
			saveButton.parameters.add(new UIELBinding(actionBean + "userId", userId));
			
			removeButton.parameters.add(new UIELBinding(actionBean + "selectedUserIndex", sCurUserIndex));
			removeButton.parameters.add(new UIELBinding(actionBean + "userId", userId));
			
			// increment the current user counter
			curUserIndex ++;
			
		}
		
		// render new user div
		UIBranchContainer newUserBranch = UIBranchContainer.make(permsForm, "new-user:");
		UIInput newUserEidInput = UIInput.make(newUserBranch, "user-eid", actionBean + "userEid");
		
		// perm checkboxes
		/*
		UISelect permsSelect = UISelect.makeMultiple(newUserBranch, "perms-select", new String[] {}, new String[] {}, actionBean + "perms.1", new String[] {});
		String permsSelectId = permsSelect.getFullID();
		List<String> permValues = new ArrayList<String>();
		List<String> permLabels = new ArrayList<String>();
		
		for (int i = 0; i < EvalToolConstants.HIERARCHY_PERM_VALUES.length; i ++) {
			
			UIBranchContainer permBranch = UIBranchContainer.make(newUserBranch, "perm:", String.valueOf(i));
			
			permValues.add(EvalToolConstants.HIERARCHY_PERM_VALUES[i]);
			permLabels.add(EvalToolConstants.HIERARCHY_PERM_LABELS[i]);
			
			UISelectChoice.make(permBranch, "perm-checkbox", permsSelectId, i);
			UISelectLabel.make(permBranch, "perm-label", permsSelectId, i);
			
		}
		*/
		
		String newUserConstant = "NEW_USER";
		
		for (int i = 0; i < EvalToolConstants.HIERARCHY_PERM_VALUES.length; i ++) {
			
			String permValue = EvalToolConstants.HIERARCHY_PERM_VALUES[i];
			String permLabel = EvalToolConstants.HIERARCHY_PERM_LABELS[i];
			
			UIBranchContainer permBranch = UIBranchContainer.make(newUserBranch, "perm:", permValue);
			UIBoundBoolean.make(permBranch, "perm-checkbox", actionBean + "permsMap." + permValue + "-" + newUserConstant, false);
			UIMessage.make(permBranch, "perm-label", permLabel);
			
		}
		
		//permsSelect.optionlist = UIOutputMany.make(permValues.toArray(new String[permValues.size()]));
		//permsSelect.optionnames = UIOutputMany.make(permLabels.toArray(new String[permLabels.size()]));
		
		UICommand addButton = UICommand.make(newUserBranch, "add-user-button", UIMessage.make("modifynodeperms.add.user.button"), actionBean + "addUser");
		addButton.parameters.add(new UIELBinding(actionBean + "selectedUserIndex", newUserConstant));

		// init js
		Object[] initParams = new Object[] {
				newUserEidInput.getFullID(),
				addButton.getFullID(),
				messageLocator.getMessage("modifynodeperms.error.no.user.eid"),
				messageLocator.getMessage("modifynodeperms.error.no.perms.selected")
			};
		
		UIInitBlock.make(tofill, "init-js", "EvalSystem.initModifyHierarchyNodePerms", initParams);
		
	}
	
	public ViewParameters getViewParameters() {
		return new HierarchyNodeParameters();
	}
	
}
