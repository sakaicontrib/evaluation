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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalCommonLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalSettings;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class EvalAdminBean {
	
	private static Log log = LogFactory.getLog(EvalAdminBean.class);
	
	public String userId;
	public String userEid;
	
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
	
	private TargettedMessageList messages;
	public void setMessages(TargettedMessageList messages) {
		this.messages = messages;
	}

	public String assignEvalAdmin() {
		
		String currentUserId = commonLogic.getCurrentUserId();
		
		if (!commonLogic.isUserAdmin(currentUserId))
			throw new SecurityException("Users who are not eval admins cannot assign other users as eval admins");
		
		String assigneeUserId = commonLogic.getUserId(this.userEid);
		
		if (assigneeUserId == null) {
			messages.addMessage(new TargettedMessage("controlevaladmin.message.error.not.found", new Object[] { this.userEid }, TargettedMessage.SEVERITY_ERROR));
			return "error";
		}
		
		try {
			commonLogic.assignEvalAdmin(assigneeUserId, currentUserId);
		} catch (IllegalArgumentException ex) {
			messages.addMessage(new TargettedMessage("controlevaladmin.message.error.already.assigned", new Object[] { this.userEid }, TargettedMessage.SEVERITY_ERROR));
			log.error(ex);
			return "error";
		}
		
		messages.addMessage(new TargettedMessage("controlevaladmin.message.success.assigning", new Object[] { this.userEid }, TargettedMessage.SEVERITY_INFO));
		return "success";
		
	}
	
	public String unassignEvalAdmin() {
		
		String currentUserId = commonLogic.getCurrentUserId();
		
		if (!commonLogic.isUserAdmin(currentUserId))
			throw new SecurityException("Users who are not eval admins cannot unassign other users as eval admins");
			
		
		this.userEid = commonLogic.getUserUsername(this.userId);
		
		try {
			commonLogic.unassignEvalAdmin(this.userId);
		} catch (IllegalArgumentException ex) {
			messages.addMessage(new TargettedMessage("controlevaladmin.message.error.unassigning", new Object[] { this.userEid }, TargettedMessage.SEVERITY_ERROR));
			log.error(ex);
			return "error";
		}
		
		messages.addMessage(new TargettedMessage("controlevaladmin.message.success.unassigning", new Object[] { this.userEid }, TargettedMessage.SEVERITY_INFO));
		return "success";
		
	}
	
	public void toggleSakaiAdminAccess() {
		
		String currentUserId = commonLogic.getCurrentUserId();
		
		// only eval admins can enable/disable sakai admin access
		if (!commonLogic.isUserEvalAdmin(currentUserId))
			throw new SecurityException("Only eval admins can enable/disable sakai admin access");
		
		// if enabled, user is disabling access
		if ((Boolean) settings.get(EvalSettings.ENABLE_SAKAI_ADMIN_ACCESS)) {
			log.info("sakai admin access to evaluation system is disabled");
			messages.addMessage(new TargettedMessage("controlevaladmin.message.sakai.admin.access.disabled", new Object[] {}, TargettedMessage.SEVERITY_INFO));
			settings.set(EvalSettings.ENABLE_SAKAI_ADMIN_ACCESS, false);
		}
		
		// if disabled, user is enabling access
		else {
			log.info("sakai admin access to evaluation system is enabled");
			messages.addMessage(new TargettedMessage("controlevaladmin.message.sakai.admin.access.enabled", new Object[] {}, TargettedMessage.SEVERITY_INFO));
			settings.set(EvalSettings.ENABLE_SAKAI_ADMIN_ACCESS, true);
		}
		
	}
	
}
