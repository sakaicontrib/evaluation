/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2010 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.tool.renderers.NavBarRenderer;
import org.sakaiproject.evaluation.tool.viewparams.ProviderSyncParams;

import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * AdministrateProviderSyncProducer
 *
 */
public class AdministrateProviderSyncProducer implements ViewComponentProducer, ViewParamsReporter, NavigationCaseReporter {
	
	public static final String VIEW_ID = "administrate_provider_sync";
	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ViewIDReporter#getViewID()
	 */
	@Override
	public String getViewID() {
		return VIEW_ID;
	}

	// Spring injection 
	private EvalCommonLogic commonLogic;
	public void setCommonLogic(EvalCommonLogic commonLogic) {
		this.commonLogic = commonLogic;
	}

    private EvalExternalLogic externalLogic;
    public void setExternalLogic(EvalExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }

    private EvalSettings evalSettings;
    public void setEvalSettings(EvalSettings settings) {
        this.evalSettings = settings;
    }

    private NavBarRenderer navBarRenderer;
    public void setNavBarRenderer(NavBarRenderer navBarRenderer) {
		this.navBarRenderer = navBarRenderer;
	}

    private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
        this.messages = messages;
    }

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	@Override
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		// 
		String currentUserId = commonLogic.getCurrentUserId();
		boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
		if (! userAdmin) {
			// Security check and denial
			throw new SecurityException("Non-admin users may not access this page");
		}
		
		
		String triggerName = null;
		if(viewparams instanceof ProviderSyncParams) {
			triggerName = ((ProviderSyncParams) viewparams).triggerName;
		}

		Map<String,Map<String, String>> cronJobs = this.externalLogic.getCronJobs("org.sakaiproject.evaluation.tool.ProviderSyncBean", new String[]{"StatusList"});
        List<String> serverIds = this.commonLogic.getServers();
        String syncServerId = (String) this.evalSettings.get(EvalSettings.SYNC_SERVER);
        
		UIMessage.make(tofill, "search-page-title", "administrate.sync.page.title");

		// TOP LINKS
		navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());
		
		UIForm syncEventForm = UIForm.make(tofill, "sync-event-form");

        Boolean syncUnassignedOnStartup = (Boolean) this.evalSettings.get(EvalSettings.SYNC_UNASSIGNED_GROUPS_ON_STARTUP);
        if(syncUnassignedOnStartup == null) {
        	// if setting is null, use default, true
        	syncUnassignedOnStartup = new Boolean(true);
        }        
        UIBoundBoolean.make(syncEventForm, "sync-unassigned-on-startup", "#{providerSyncBean.syncUnassignedOnStartup}", syncUnassignedOnStartup);
        UIMessage.make(syncEventForm, "sync-unassigned-on-startup-note", "administrate.sync.on_server_startup");

        Boolean syncUserAssignmentsOnStateChange = (Boolean) this.evalSettings.get(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_STATE_CHANGE);
        if(syncUserAssignmentsOnStateChange == null) {
        	// if setting is null, use default, false
        	syncUserAssignmentsOnStateChange = new Boolean(false);
        }        
        UIBoundBoolean.make(syncEventForm, "sync-on-state-change", "#{providerSyncBean.syncOnStateChange}", syncUserAssignmentsOnStateChange);
        UIMessage.make(syncEventForm, "sync-on-state-change-note", "administrate.sync.on_state_change");

        Boolean syncUserAssignmentsOnGroupSave = (Boolean) this.evalSettings.get(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_GROUP_SAVE);
        if(syncUserAssignmentsOnGroupSave == null) {
        	// if setting is null, use default, true
        	syncUserAssignmentsOnGroupSave = new Boolean(true);
        }        
        UIBoundBoolean.make(syncEventForm, "sync-on-group-save", "#{providerSyncBean.syncOnGroupSave}", syncUserAssignmentsOnGroupSave);
        UIMessage.make(syncEventForm, "sync-on-group-save-note", "administrate.sync.on_group_save");

        Boolean syncUserAssignmentsOnGroupUpdate = (Boolean) this.evalSettings.get(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_GROUP_UPDATE);
        if(syncUserAssignmentsOnGroupUpdate == null) {
        	// if setting is null, use default, false
        	syncUserAssignmentsOnGroupUpdate = new Boolean(false);
        }        
        UIBoundBoolean.make(syncEventForm, "sync-on-group-update", "#{providerSyncBean.syncOnGroupUpdate}", syncUserAssignmentsOnGroupUpdate);
        UIMessage.make(syncEventForm, "sync-on-group-update-note", "administrate.sync.on_group_update");
        
        if(serverIds != null && serverIds.size() > 1) {
        	UIBranchContainer selectServerGraf = UIBranchContainer.make(syncEventForm, "sync-select-server-graf:");
        	UIMessage.make(selectServerGraf, "sync-select-server-note", "administrate.sync.select.server");
        	UISelect.make(selectServerGraf, "sync-select-server", (String[]) serverIds.toArray(new String[serverIds.size()]), "#{providerSyncBean.syncServerId}",(String) syncServerId);
        }

        UICommand.make(syncEventForm, "sync-event-submit", UIMessage.make("administrate.sync.event.submit"), "#{providerSyncBean.updateSyncEvents}");

		// frequency 
		
		UIForm syncCronForm = UIForm.make(tofill, "sync-cron-form");

		Map<String,String> reviseCronJob = null;
		if(triggerName != null) {
			reviseCronJob = cronJobs.get(triggerName);
		}
		
		String cronExpression = null;
		String stateList = "";

		if(reviseCronJob != null) {
			// select time(s) for this cron job
			cronExpression = reviseCronJob.get("trigger.cronExpression");
			stateList = reviseCronJob.get("StatusList");
		}
		
		UIMessage.make(syncCronForm, "sync-time-note", "administrate.sync.cronExpression");
		UIInput.make(syncCronForm, "sync-time", "#{providerSyncBean.cronExpression}", cronExpression);
					
		// state partial
		UIBoundBoolean.make(syncCronForm, "sync-state-partial", "#{providerSyncBean.partial}", stateList.contains(EvalConstants.EVALUATION_STATE_PARTIAL));
		UIMessage.make(syncCronForm, "sync-state-partial-note", "state.label.partial");
		
		// state inqueue
		UIBoundBoolean.make(syncCronForm, "sync-state-inqueue", "#{providerSyncBean.inqueue}", stateList.contains(EvalConstants.EVALUATION_STATE_INQUEUE));
		UIMessage.make(syncCronForm, "sync-state-inqueue-note", "state.label.inqueue");
		
		// state active
		UIBoundBoolean.make(syncCronForm, "sync-state-active", "#{providerSyncBean.active}", stateList.contains(EvalConstants.EVALUATION_STATE_ACTIVE));
		UIMessage.make(syncCronForm, "sync-state-active-note", "state.label.active");
		
		// state graceperiod
		UIBoundBoolean.make(syncCronForm, "sync-state-graceperiod", "#{providerSyncBean.graceperiod}", stateList.contains(EvalConstants.EVALUATION_STATE_GRACEPERIOD));
		UIMessage.make(syncCronForm, "sync-state-graceperiod-note", "state.label.graceperiod");
		
		// state closed
		UIBoundBoolean.make(syncCronForm, "sync-state-closed", "#{providerSyncBean.closed}", stateList.contains(EvalConstants.EVALUATION_STATE_CLOSED));
		UIMessage.make(syncCronForm, "sync-state-closed-note", "state.label.closed");
		
        if(serverIds != null && serverIds.size() > 1) {
        	UIBranchContainer selectServerDiv = UIBranchContainer.make(syncCronForm, "sync-select-server-div:");
        	UIMessage.make(selectServerDiv, "sync-select-server-note", "administrate.sync.select.server");
        	UISelect.make(selectServerDiv, "sync-select-server", (String[]) serverIds.toArray(new String[serverIds.size()]), "#{providerSyncBean.syncServerId}",(String) syncServerId);
        }

		UICommand.make(syncCronForm, "sync-cron-submit", UIMessage.make("administrate.sync.cron.save"), "providerSyncBean.updateSync");	

		if(! cronJobs.isEmpty()) {
			UIBranchContainer triggerTable = UIBranchContainer.make(tofill, "trigger-table:");
			for(String item : cronJobs.keySet()) {
				Map<String, String> cronJob = cronJobs.get(item);
				UIBranchContainer triggerRow = UIBranchContainer.make(triggerTable, "trigger-row:");
				UIOutput.make(triggerRow, "job-stateList", cronJob.get("StatusList"));
				UIOutput.make(triggerRow, "trigger-cronExpression", cronJob.get("trigger.cronExpression"));
				UIInternalLink.make(triggerRow, "delete-job", UIMessage.make("administrate.sync.delete.job"), new ProviderSyncParams(AdministrateProviderSyncProducer.VIEW_ID, item));
				UIInternalLink.make(triggerRow, "revise-job", UIMessage.make("administrate.sync.revise.job"), new ProviderSyncParams(AdministrateProviderSyncProducer.VIEW_ID, item));
				
				
				UIOutput.make(triggerRow, "job-name", cronJob.get("job.group") + " " + cronJob.get("job.name"));
				UIOutput.make(triggerRow, "trigger-name", cronJob.get("trigger.group") + " " + cronJob.get("trigger.name"));
				
			}
		}
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List<NavigationCase> list = new ArrayList<NavigationCase>();
		list.add(new NavigationCase("success", new ProviderSyncParams(AdministrateProviderSyncProducer.VIEW_ID, null)));
		list.add(new NavigationCase("failure", new ProviderSyncParams(AdministrateProviderSyncProducer.VIEW_ID, null)));
		return list;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters() {
		return new ProviderSyncParams(VIEW_ID, null);
	}


}
