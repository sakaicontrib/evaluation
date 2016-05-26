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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sakaiproject.evaluation.constant.EvalConstants;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.scheduling.GroupMembershipSync;
import org.sakaiproject.evaluation.tool.ProviderSyncBean;
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
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.util.RSFUtil;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * AdministrateProviderSyncProducer
 *
 */
public class AdministrateProviderSyncProducer extends EvalCommonProducer implements ViewParamsReporter, NavigationCaseReporter {
	
	public static final String PROPNAME_STATE_LIST = GroupMembershipSync.GROUP_MEMBERSHIP_SYNC_PROPNAME_STATE_LIST;
	public static final String VIEW_ID = "administrate_provider_sync";

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ViewIDReporter#getViewID()
	 */
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
	public void fill(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		// 
		String currentUserId = commonLogic.getCurrentUserId();
		boolean userAdmin = commonLogic.isUserAdmin(currentUserId);
		if (! userAdmin) {
			// Security check and denial
			throw new SecurityException("Non-admin users may not access this page");
		}
		
		Integer tab = null;
		String fullJobName = null;
		if(viewparams instanceof ProviderSyncParams) {
			fullJobName = ((ProviderSyncParams) viewparams).fullJobName;
			if(((ProviderSyncParams) viewparams).tab != null) {
				tab = ((ProviderSyncParams) viewparams).tab;
			}
		}
		if(tab == null) {
			tab = 0;
		}
		
		Map<String,Map<String, String>> cronJobs = this.externalLogic.getCronJobs(ProviderSyncBean.JOB_GROUP_NAME);
        List<String> serverIds = this.commonLogic.getServers();
        String syncServerId = (String) this.evalSettings.get(EvalSettings.SYNC_SERVER);
        
		// TOP LINKS
		navBarRenderer.makeNavBar(tofill, NavBarRenderer.NAV_ELEMENT, this.getViewID());
		
		UIOutput.make(tofill, "initial-tab", tab.toString());
		
		UIForm byEventForm = UIForm.make(tofill, "by_event-form");
		
		UIInput.make(byEventForm, "currentTab", "#{providerSyncBean.tab}", tab.toString());
		RSFUtil.addResultingViewBinding(byEventForm, "tab", "#{providerSyncBean.tab}");
		
        Boolean syncUnassignedOnStartup = (Boolean) this.evalSettings.get(EvalSettings.SYNC_UNASSIGNED_GROUPS_ON_STARTUP);
        if(syncUnassignedOnStartup == null) {
        	// if setting is null, use default, true
        	syncUnassignedOnStartup = true;
        }        
        UIBoundBoolean.make(byEventForm, "sync-unassigned-on-startup", "#{providerSyncBean.syncUnassignedOnStartup}", syncUnassignedOnStartup);
        UIMessage.make(byEventForm, "sync-unassigned-on-startup-note", "administrate.sync.on_server_startup");

        Boolean syncUserAssignmentsOnStateChange = (Boolean) this.evalSettings.get(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_STATE_CHANGE);
        if(syncUserAssignmentsOnStateChange == null) {
        	// if setting is null, use default, false
        	syncUserAssignmentsOnStateChange = false;
        }        
        UIBoundBoolean.make(byEventForm, "sync-on-state-change", "#{providerSyncBean.syncOnStateChange}", syncUserAssignmentsOnStateChange);
        UIMessage.make(byEventForm, "sync-on-state-change-note", "administrate.sync.on_state_change");

        Boolean syncUserAssignmentsOnGroupSave = (Boolean) this.evalSettings.get(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_GROUP_SAVE);
        if(syncUserAssignmentsOnGroupSave == null) {
        	// if setting is null, use default, true
        	syncUserAssignmentsOnGroupSave = true;
        }        
        UIBoundBoolean.make(byEventForm, "sync-on-group-save", "#{providerSyncBean.syncOnGroupSave}", syncUserAssignmentsOnGroupSave);
        UIMessage.make(byEventForm, "sync-on-group-save-note", "administrate.sync.on_group_save");

        Boolean syncUserAssignmentsOnGroupUpdate = (Boolean) this.evalSettings.get(EvalSettings.SYNC_USER_ASSIGNMENTS_ON_GROUP_UPDATE);
        if(syncUserAssignmentsOnGroupUpdate == null) {
        	// if setting is null, use default, false
        	syncUserAssignmentsOnGroupUpdate = false;
        }        
        UIBoundBoolean.make(byEventForm, "sync-on-group-update", "#{providerSyncBean.syncOnGroupUpdate}", syncUserAssignmentsOnGroupUpdate);
        UIMessage.make(byEventForm, "sync-on-group-update-note", "administrate.sync.on_group_update");
        
        if(serverIds != null && serverIds.size() > 0) {
        	String[] serverNames = new String[serverIds.size()];
        	for(int i = 0; i < serverIds.size(); i++) {
        		serverNames[i] = serverIds.get(i).substring(0, serverIds.get(i).lastIndexOf('-'));
        	}
        	UIBranchContainer selectServerGraf = UIBranchContainer.make(byEventForm, "sync-select-server-graf:");
        	UIMessage.make(selectServerGraf, "sync-select-server-note", "administrate.sync.select.server");
        	UISelect.make(selectServerGraf, "sync-select-server", serverNames, "#{providerSyncBean.syncServerId}",(String) syncServerId);
        }

        UICommand.make(byEventForm, "sync-event-submit", UIMessage.make("administrate.sync.event.submit"), "#{providerSyncBean.updateSyncEvents}");

		UIForm byTimeForm = UIForm.make(tofill, "by_time-form");
		UIInput.make(byTimeForm, "by_time-fullJobName", "#{providerSyncBean.fullJobName}", fullJobName);
		UIInput.make(byTimeForm, "currentTab", "#{providerSyncBean.tab}", tab.toString());
		RSFUtil.addResultingViewBinding(byTimeForm, "tab", "#{providerSyncBean.tab}");
		
		Map<String,String> reviseCronJob = null;
		if(fullJobName != null) {
			reviseCronJob = cronJobs.get(fullJobName);
		}
		
		String cronExpression = null;
		String stateList = "";

		if(reviseCronJob != null) {
			// select time(s) for this cron job
			cronExpression = reviseCronJob.get(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION);
			stateList = reviseCronJob.get(PROPNAME_STATE_LIST);
		}
		
		UIMessage.make(byTimeForm, "by_time-cronExpression-note", "administrate.sync.by_time.cronExpression");
		UIInput.make(byTimeForm, "by_time-cronExpression", "#{providerSyncBean.cronExpression}", cronExpression);
		UILink tutorialLink = UILink.make(byTimeForm, "by_time-cronTutorial", UIMessage.make("administrate.sync.by_time.cronTutorial.label"), UIMessage.make("administrate.sync.by_time.cronTutorial").getValue());
		tutorialLink.target = UIMessage.make("administrate.sync.by_time.cronTutorial");
					
		// state partial
		UIBoundBoolean.make(byTimeForm, "by_time-state-partial", "#{providerSyncBean.partial}", stateList.contains(EvalConstants.EVALUATION_STATE_PARTIAL));
		UIMessage.make(byTimeForm, "by_time-state-partial-note", "state.label.partial");
		UIOutput.make(byTimeForm, "by_time-state-name-partial", EvalConstants.EVALUATION_STATE_PARTIAL);
		
		// state inqueue
		UIBoundBoolean.make(byTimeForm, "by_time-state-inqueue", "#{providerSyncBean.inqueue}", stateList.contains(EvalConstants.EVALUATION_STATE_INQUEUE));
		UIMessage.make(byTimeForm, "by_time-state-inqueue-note", "state.label.inqueue");
		UIOutput.make(byTimeForm, "by_time-state-name-inqueue", EvalConstants.EVALUATION_STATE_INQUEUE);
		
		// state active
		UIBoundBoolean.make(byTimeForm, "by_time-state-active", "#{providerSyncBean.active}", stateList.contains(EvalConstants.EVALUATION_STATE_ACTIVE));
		UIMessage.make(byTimeForm, "by_time-state-active-note", "state.label.active");
		UIOutput.make(byTimeForm, "by_time-state-name-active", EvalConstants.EVALUATION_STATE_ACTIVE);
		
		// state graceperiod
		UIBoundBoolean.make(byTimeForm, "by_time-state-graceperiod", "#{providerSyncBean.graceperiod}", stateList.contains(EvalConstants.EVALUATION_STATE_GRACEPERIOD));
		UIMessage.make(byTimeForm, "by_time-state-graceperiod-note", "state.label.graceperiod");
		UIOutput.make(byTimeForm, "by_time-state-name-graceperiod", EvalConstants.EVALUATION_STATE_GRACEPERIOD);
		
		// state closed
		UIBoundBoolean.make(byTimeForm, "by_time-state-closed", "#{providerSyncBean.closed}", stateList.contains(EvalConstants.EVALUATION_STATE_CLOSED));
		UIMessage.make(byTimeForm, "by_time-state-closed-note", "state.label.closed");
		UIOutput.make(byTimeForm, "by_time-state-name-closed", EvalConstants.EVALUATION_STATE_CLOSED);
		
        if(serverIds != null && serverIds.size() > 0) {
        	String[] serverNames = new String[serverIds.size()];
        	for(int i = 0; i < serverIds.size(); i++) {
        		serverNames[i] = serverIds.get(i).substring(0, serverIds.get(i).lastIndexOf('-'));
        	}
        	UIBranchContainer selectServerDiv = UIBranchContainer.make(byTimeForm, "by_time-server-div:");
        	UIMessage.make(selectServerDiv, "by_time-server-note", "administrate.sync.select.server");
        	UISelect.make(selectServerDiv, "by_time-server", serverNames, "#{providerSyncBean.syncServerId}",(String) syncServerId);
        }

		UICommand.make(byTimeForm, "by_time-create-submit", UIMessage.make("administrate.sync.by_time.save"), "providerSyncBean.scheduleSync");
		UICommand.make(byTimeForm, "by_time-revise-submit", UIMessage.make("administrate.sync.by_time.revise"), "providerSyncBean.updateSync");

		if(cronJobs.isEmpty()) {
			UIMessage.make(tofill, "by_time-no_triggers", "administrate.sync.triggers.none");
		} else {
			UIBranchContainer triggerTable = UIBranchContainer.make(tofill, "trigger-table:");
			for(String item : cronJobs.keySet()) {
				Map<String, String> cronJob = cronJobs.get(item);
				UIBranchContainer triggerRow = UIBranchContainer.make(triggerTable, "trigger-row:");
				UIOutput.make(triggerRow, "trigger-cronExpression", cronJob.get(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION));
				UIOutput.make(triggerRow, "trigger-stateList", cronJob.get(PROPNAME_STATE_LIST));
				UIOutput.make(triggerRow, "trigger-name", item);
				UIOutput.make(triggerRow, "trigger-revise-cronExpression", cronJob.get(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION));
				UIOutput.make(triggerRow, "trigger-revise-stateList", cronJob.get(PROPNAME_STATE_LIST));
				UIMessage.make(triggerRow, "trigger-delete-confirm", "administrate.sync.triggers.delete.confirm", new String[]{cronJob.get(EvalConstants.CRON_SCHEDULER_CRON_EXPRESSION), cronJob.get(PROPNAME_STATE_LIST)});
				UIInternalLink.make(triggerRow, "trigger-delete", UIMessage.make("administrate.sync.triggers.delete"), new ProviderSyncParams(AdministrateProviderSyncProducer.VIEW_ID, item));
				UIInternalLink.make(triggerRow, "trigger-revise", UIMessage.make("administrate.sync.triggers.revise"), new ProviderSyncParams(AdministrateProviderSyncProducer.VIEW_ID, item));
			}
		}
		
		UIForm byTimeDeleteForm = UIForm.make(tofill, "by_time-delete-form");
		UIInput.make(byTimeDeleteForm, "currentTab", "#{providerSyncBean.tab}", tab.toString());
		RSFUtil.addResultingViewBinding(byTimeDeleteForm, "tab", "#{providerSyncBean.tab}");
		UIInput.make(byTimeDeleteForm, "by_time-delete-fullJobName", "#{providerSyncBean.fullJobName}");
		UICommand.make(byTimeDeleteForm, "by_time-delete-submit", "#{providerSyncBean.deleteSync}");
		
		UIForm quickSyncForm = UIForm.make(tofill, "quick_sync-form");
		UIInput.make(quickSyncForm, "currentTab", "#{providerSyncBean.tab}", tab.toString());
		RSFUtil.addResultingViewBinding(quickSyncForm, "tab", "#{providerSyncBean.tab}");

		// state partial
		UIBoundBoolean.make(quickSyncForm, "quick_sync-state-partial", "#{providerSyncBean.partial}", stateList.contains(EvalConstants.EVALUATION_STATE_PARTIAL));
		UIMessage.make(quickSyncForm, "quick_sync-state-partial-note", "state.label.partial");
		
		// state inqueue
		UIBoundBoolean.make(quickSyncForm, "quick_sync-state-inqueue", "#{providerSyncBean.inqueue}", stateList.contains(EvalConstants.EVALUATION_STATE_INQUEUE));
		UIMessage.make(quickSyncForm, "quick_sync-state-inqueue-note", "state.label.inqueue");
		
		// state active
		UIBoundBoolean.make(quickSyncForm, "quick_sync-state-active", "#{providerSyncBean.active}", stateList.contains(EvalConstants.EVALUATION_STATE_ACTIVE));
		UIMessage.make(quickSyncForm, "quick_sync-state-active-note", "state.label.active");
		
		// state graceperiod
		UIBoundBoolean.make(quickSyncForm, "quick_sync-state-graceperiod", "#{providerSyncBean.graceperiod}", stateList.contains(EvalConstants.EVALUATION_STATE_GRACEPERIOD));
		UIMessage.make(quickSyncForm, "quick_sync-state-graceperiod-note", "state.label.graceperiod");
		
		// state closed
		UIBoundBoolean.make(quickSyncForm, "quick_sync-state-closed", "#{providerSyncBean.closed}", stateList.contains(EvalConstants.EVALUATION_STATE_CLOSED));
		UIMessage.make(quickSyncForm, "quick_sync-state-closed-note", "state.label.closed");
		
        if(serverIds != null && serverIds.size() > 0) {
        	String[] serverNames = new String[serverIds.size()];
        	for(int i = 0; i < serverIds.size(); i++) {
        		serverNames[i] = serverIds.get(i).substring(0, serverIds.get(i).lastIndexOf('-'));
        	}
        	UIBranchContainer selectServerDiv = UIBranchContainer.make(quickSyncForm, "quick_sync-server-div:");
        	UIMessage.make(selectServerDiv, "quick_sync-server-note", "administrate.sync.quick_sync.server");
        	UISelect.make(selectServerDiv, "quick_sync-server", serverNames, "#{providerSyncBean.syncServerId}",(String) syncServerId);
        }

		UICommand.make(quickSyncForm, "quick_sync-submit", UIMessage.make("administrate.sync.quick_sync.save"), "providerSyncBean.quickSync");	

	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter#reportNavigationCases()
	 */
	public List reportNavigationCases() {
		List<NavigationCase> list = new ArrayList<>();
		list.add(new NavigationCase(ProviderSyncBean.RESULT_SUCCESS, new ProviderSyncParams(AdministrateProviderSyncProducer.VIEW_ID, null)));
		list.add(new NavigationCase(ProviderSyncBean.RESULT_FAILURE, new ProviderSyncParams(AdministrateProviderSyncProducer.VIEW_ID, null)));
		return list;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters() {
		return new ProviderSyncParams(VIEW_ID, null);
	}


}
