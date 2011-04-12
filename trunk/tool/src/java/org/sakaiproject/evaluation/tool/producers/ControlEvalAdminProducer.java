package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.model.EvalUser;
import org.sakaiproject.evaluation.model.EvalAdmin;

import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIAlternativeTextDecorator;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class ControlEvalAdminProducer implements ViewComponentProducer {
	
	public static String VIEW_ID = "control_eval_admin";
	public String getViewID() {
		return VIEW_ID;
	}
	
	private EvalCommonLogic commonLogic;
	public void setCommonLogic(EvalCommonLogic commonLogic) {
		this.commonLogic = commonLogic;
	}
	
	private EvalEvaluationService evaluationService;
	public void setEvaluationService(EvalEvaluationService evaluationService) {
		this.evaluationService = evaluationService;
	}
	
	private EvalSettings settings;
    public void setSettings(EvalSettings settings) {
        this.settings = settings;
    }

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		
		String currentUserId = commonLogic.getCurrentUserId();
		boolean isAdmin = commonLogic.isUserAdmin(currentUserId);
		boolean isEvalAdmin = commonLogic.isUserEvalAdmin(currentUserId);
		
		if (!isAdmin)
			throw new SecurityException("Users who are not assigned as eval admins may not access this page");
		
		// TOP LINKS
        UIInternalLink.make(tofill, "administrate-link",
                UIMessage.make("administrate.page.title"),
                new SimpleViewParameters(AdministrateProducer.VIEW_ID));

        UIInternalLink.make(tofill, "summary-link", 
                UIMessage.make("summary.page.title"), 
                new SimpleViewParameters(SummaryProducer.VIEW_ID));

        // only show "My Evaluations", "My Templates", "My Items", "My Scales" and "My Email Templates" links if enabled
        //boolean showMyToplinks = ((Boolean)evalSettings.get(EvalSettings.ENABLE_MY_TOPLINKS)).booleanValue(); // ALWAYS SHOW FOR ADMIN
        boolean beginEvaluation = evaluationService.canBeginEvaluation(currentUserId);
    	if (beginEvaluation) {
    		UIInternalLink.make(tofill, "control-evaluations-link",
    				UIMessage.make("controlevaluations.page.title"), 
    				new SimpleViewParameters(ControlEvaluationsProducer.VIEW_ID));
    	}
    	
    	UIInternalLink.make(tofill, "control-templates-link",
    			UIMessage.make("controltemplates.page.title"), 
    			new SimpleViewParameters(ControlTemplatesProducer.VIEW_ID));

    	if (!((Boolean) settings.get(EvalSettings.DISABLE_ITEM_BANK))) {
    		UIInternalLink.make(tofill, "control-items-link",
    				UIMessage.make("controlitems.page.title"),
    				new SimpleViewParameters(ControlItemsProducer.VIEW_ID));
    	}

    	UIInternalLink.make(tofill, "control-scales-link",
    			UIMessage.make("controlscales.page.title"),
    			new SimpleViewParameters(ControlScalesProducer.VIEW_ID));

    	UIInternalLink.make(tofill, "control-emailtemplates-link",
    			UIMessage.make("controlemailtemplates.page.title"),
    			new SimpleViewParameters(ControlEmailTemplatesProducer.VIEW_ID));
		
    	UIMessage.make(tofill, "control-eval-admin-title", "controlevaladmin.page.title");
    	UIMessage.make(tofill, "control-eval-admin-note", "controlevaladmin.page.note");
    	
		UIForm evalAdminForm = UIForm.make(tofill, "eval-admin-form");
		String evalAdminBean = "evalAdminBean.";
		
		// get the list of eval admins and their corresponding user information (in the form of EvalUser objects)
		List<EvalAdmin> evalAdminList = commonLogic.getEvalAdmins();
		int numEvalAdmins = evalAdminList.size();
		String[] evalAdminIds = new String[numEvalAdmins];
		String[] assignorIds = new String[numEvalAdmins];
		
		for (int i = 0; i < numEvalAdmins; i ++) {
			EvalAdmin evalAdmin = evalAdminList.get(i);
			evalAdminIds[i] = evalAdmin.getUserId();
			assignorIds[i] = evalAdmin.getAssignorUserId();
		}
		
		List<EvalUser> evalUserList = commonLogic.getEvalUsersByIds(evalAdminIds);
		List<EvalUser> assignorList = commonLogic.getEvalUsersByIds(assignorIds);
		
		// render table headers
		UIMessage.make(evalAdminForm, "user-info-header", "controlevaladmin.user.info.header");
		UIMessage.make(evalAdminForm, "assignor-info-header", "controlevaladmin.assignor.info.header");
		UIMessage.make(evalAdminForm, "actions-header", "controlevaladmin.actions.header");
		
		// display each eval admin's name, user eid, assignor, assign date, and a button to unassign them
		for (int i = 0; i < numEvalAdmins; i ++) {
			
			EvalAdmin evalAdmin = evalAdminList.get(i);
			EvalUser user = evalUserList.get(i);
			EvalUser assignor = assignorList.get(i);
			
			UIBranchContainer userBranch = UIBranchContainer.make(evalAdminForm, "eval-admin:", String.valueOf(i));
			UIMessage.make(userBranch, "user-info", "controlevaladmin.user.info", new Object[] { user.displayName, user.username });
			UIMessage.make(userBranch, "assignor-info", "controlevaladmin.assignor.info", new Object[] { assignor.username, evalAdmin.getAssignDate() });
			
			// display unassign button if it is not the current user (i.e. users cannot unassign themselves)
			// NOTE: in the case of only one admin, that admin is the current user. thus, unassign is made unavailable by this condition
			if (!currentUserId.equals(evalAdmin.getUserId())) {
				UICommand unassignButton = UICommand.make(userBranch, "unassign-button", UIMessage.make("controlevaladmin.unassign.button"), evalAdminBean + "unassignEvalAdmin");
				unassignButton.parameters.add(new UIELBinding(evalAdminBean + "userId", evalAdmin.getUserId()));
			}
			
		}
		
		// render an input and button to assign a user as an eval admin
		UIMessage.make(evalAdminForm, "user-eid-label", "controlevaladmin.user.eid.label");
		UIInput.make(evalAdminForm, "user-eid-input", evalAdminBean + "userEid");
		UICommand.make(evalAdminForm, "assign-button", UIMessage.make("controlevaladmin.assign.button"), evalAdminBean + "assignEvalAdmin");
		
		// sakai admins table
		UIMessage.make(evalAdminForm, "sakai-admins-header", "controlevaladmin.sakai.admins.header");
		
		// render sakai admin access status and toggle button, as well as sakai admin list
		if ((Boolean) settings.get(EvalSettings.ENABLE_SAKAI_ADMIN_ACCESS)) { // sakai admin access is enabled
			
			UIMessage.make(evalAdminForm, "sakai-admin-access-status", "controlevaladmin.sakai.admin.access.status.enabled");
			
			// only eval admins can disable sakai admin access
			if (isEvalAdmin)
				UICommand.make(evalAdminForm, "toggle-sakai-admin-access-button", UIMessage.make("controlevaladmin.disable.sakai.admin.button"), evalAdminBean + "toggleSakaiAdminAccess");
			
			List<EvalUser> sakaiAdminList = commonLogic.getSakaiAdmins();
			
			for (int i = 0; i < sakaiAdminList.size(); i ++) {
				
				EvalUser sakaiAdmin = sakaiAdminList.get(i);
				
				UIBranchContainer sakaiAdminRowBranch = UIBranchContainer.make(evalAdminForm, "sakai-admin:", String.valueOf(i));
				UIMessage.make(sakaiAdminRowBranch, "sakai-admin-info", "controlevaladmin.user.info", new Object[] { sakaiAdmin.displayName, sakaiAdmin.username });
				
			}
			
		}
		
		else { // sakai admin access is disabled
			UIMessage.make(evalAdminForm, "sakai-admin-access-status", "controlevaladmin.sakai.admin.access.status.disabled");
			UICommand.make(evalAdminForm, "toggle-sakai-admin-access-button", UIMessage.make("controlevaladmin.enable.sakai.admin.button"), evalAdminBean + "toggleSakaiAdminAccess");
		}
		
		UIInitBlock.make(tofill, "init-js", "EvalSystem.initEvalAdminView", new Object[] {});
		
	}
	
}
