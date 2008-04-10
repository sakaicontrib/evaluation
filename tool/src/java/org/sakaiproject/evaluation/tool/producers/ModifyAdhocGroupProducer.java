package org.sakaiproject.evaluation.tool.producers;

import java.util.List;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.dao.EvalAdhocSupport;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAdhocGroup;
import org.sakaiproject.evaluation.tool.locators.AdhocGroupsBean;
import org.sakaiproject.evaluation.tool.viewparams.AdhocGroupParams;

import uk.org.ponder.rsf.components.ParameterList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;


/**
 * This view is for creating or modifying adhoc groups. If the Group ID in the incoming
 * ViewParams is null, we assume we are creating a new adhoc group.
 * 
 * @author sgithens
 */
public class ModifyAdhocGroupProducer implements ViewComponentProducer, ViewParamsReporter,
ActionResultInterceptor {
    public static final String VIEW_ID = "modify_adhoc_group";
    
    private EvalAdhocSupport evalAdhocSupport;
    public void setEvalAdhocSupport(EvalAdhocSupport bean) {
       this.evalAdhocSupport = bean;
    }
    
    private EvalExternalLogic externalLogic;
    public void setEvalExternalLogic(EvalExternalLogic logic) {
       this.externalLogic = logic;
    }
    
    private AdhocGroupsBean adhocGroupsBean;
    public void setAdhocGroupsBean(AdhocGroupsBean adhocGroupsBean) {
		this.adhocGroupsBean = adhocGroupsBean;
	}

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
    	AdhocGroupParams params = (AdhocGroupParams) viewparams;
    	String curUserId = externalLogic.getCurrentUserId();
    	
    	boolean newGroup = false;
    	EvalAdhocGroup evalAdhocGroup = null;
    	if (params.adhocGroupId == null) {
    		newGroup = true;
    	}
    	else { 
    		evalAdhocGroup = evalAdhocSupport.getAdhocGroupById(params.adhocGroupId);
    		if (!curUserId.equals(evalAdhocGroup.getOwner())) {
    			throw new SecurityException("Only owners can modify adhocgroups: " + curUserId
    					+ " , " + evalAdhocGroup.getId());
    		}
    	}
    	
    	String adhocGroupTitle = "";
    	if (!newGroup) {
    		adhocGroupTitle = evalAdhocGroup.getTitle();
    	}
    	
    	// Page Title
    	if (newGroup) {
    		UIMessage.make(tofill, "page-title", "modifyadhocgroup.page.title.new");
    	}
    	else { 
    		UIMessage.make(tofill, "page-title", "modifyadhocgroup.page.title.existing", new String[] {evalAdhocGroup.getTitle()} );
    	}
    	
    	UIForm form = UIForm.make(tofill, "adhoc-group-form");
    	UIInput.make(form, "group-name-input", "adhocGroupsBean.adhocGroupTitle", adhocGroupTitle);
    	
    	if (!newGroup) {
    		UIOutput.make(form, "existing-members");
    		String[] participants = evalAdhocGroup.getParticipantIds();
    		List<EvalUser> evalUsers = externalLogic.getEvalUsersByIds(participants);
    		for (EvalUser evalUser : evalUsers) {
    			UIBranchContainer row = UIBranchContainer.make(form, "member-row:");
    			if (EvalConstants.USER_TYPE_INTERNAL.equals(evalUser.type)) {
    				UIMessage.make(row, "user-id", "modifyadhocgroup.adhocuser.label");
    			}
    			else {
    				UIOutput.make(row, "user-id", evalUser.username);
    			}
    			UIOutput.make(row, "user-display", evalUser.displayName);
    			// Remove Button
    			UICommand removeButton = UICommand.make(row, "remove-member", "adhocGroupMemberRemovalBean.removeUser");
    			removeButton.parameters = new ParameterList();
    			removeButton.parameters.add(new UIELBinding("adhocGroupMemberRemovalBean.adhocGroupId", evalAdhocGroup.getId()));
    			removeButton.parameters.add(new UIELBinding("adhocGroupMemberRemovalBean.adhocUserId", evalUser.userId));
    		}
    	}
    	
    	// Place to add more users via email address
    	UIInput.make(form, "add-members-input", "adhocGroupsBean.newAdhocGroupUsers");
    	
    	if (newGroup) {
    		UICommand saveButton = UICommand.make(form, "save-button", "adhocGroupsBean.addNewAdHocGroup");
    	}
    	else {
    		UICommand saveButton = UICommand.make(form, "save-button", "adhocGroupsBean.addUsersToAdHocGroup");
    		saveButton.parameters = new ParameterList(new UIELBinding("adhocGroupsBean.adhocGroupId", evalAdhocGroup.getId()));
    	}
    	
    	// Handler return URL
    	if (params.returnURL != null) {
    		UILink.make(tofill, "return-link", params.returnURL);
    	}
    }

    public String getViewID() {
        return VIEW_ID;
    }

    public ViewParameters getViewParameters() {
        return new AdhocGroupParams();
    }

	public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
		
		if (AdhocGroupsBean.SAVED_NEW_ADHOCGROUP.equals(actionReturn) &&
				incoming instanceof AdhocGroupParams) {
			AdhocGroupParams params = (AdhocGroupParams) incoming.copyBase();
			params.adhocGroupId = adhocGroupsBean.getAdhocGroupId();
			result.resultingView = params;
		}
		
	}

}
