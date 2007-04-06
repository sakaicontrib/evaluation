/******************************************************************************
 * EvalEmailsLogicImpl.java - created by aaronz@vt.edu on Dec 29, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.impl;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.impl.utils.TextTemplateLogicUtils;
import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.model.utils.EvalUtils;


/**
 * EvalEmailsLogic implementation
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalEmailsLogicImpl implements EvalEmailsLogic {

	private static Log log = LogFactory.getLog(EvalEmailsLogicImpl.class);

	private EvaluationDao dao;
	public void setDao(EvaluationDao dao) {
		this.dao = dao;
	}

	private EvalExternalLogic external;
	public void setExternalLogic(EvalExternalLogic external) {
		this.external = external;
	}

	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
	}

	private EvalEvaluationsLogic evaluationLogic;
	public void setEvaluationLogic(EvalEvaluationsLogic evaluationLogic) {
		this.evaluationLogic = evaluationLogic;
	}
	
	private EvalResponsesLogic evalResponsesLogic;
	public void setEvalResponsesLogic(EvalResponsesLogic evalResponsesLogic) {
		this.evalResponsesLogic = evalResponsesLogic;
	}

	// INIT method
	public void init() {
		log.debug("Init");
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#saveEmailTemplate(org.sakaiproject.evaluation.model.EvalEmailTemplate, java.lang.String)
	 */
	public void saveEmailTemplate(EvalEmailTemplate emailTemplate, String userId) {
		log.debug("userId: " + userId + ", emailTemplate: " + emailTemplate.getId());

		// set the date modified
		emailTemplate.setLastModified( new Date() );

		// check user permissions
		if (! canUserControlEmailTemplate(userId, emailTemplate)) {
			throw new SecurityException("User ("+userId+
					") cannot control email template ("+emailTemplate.getId()+
					") without permissions");
		}

		// checks to keeps someone from overwriting the default templates
		if (emailTemplate.getId() == null) {
			// null out the defaultType for new templates 
			emailTemplate.setDefaultType(null);

		} else {
			// existing template
			if (emailTemplate.getDefaultType() != null) {
				throw new IllegalArgumentException("Cannot modify default templates or set existing templates to be default");
			}

			// check if there are evaluation this is used in and if the user can modify this based on them
			// check available templates
			List l = dao.findByProperties(EvalEvaluation.class, 
					new String[] {"availableEmailTemplate.id"}, 
					new Object[] {emailTemplate.getId()} );
			for (int i = 0; i < l.size(); i++) {
				EvalEvaluation eval = (EvalEvaluation) l.get(i);
				// check eval/template permissions
				checkEvalTemplateControl(userId, eval, emailTemplate);
			}
			// check reminder templates
			l = dao.findByProperties(EvalEvaluation.class, 
					new String[] {"reminderEmailTemplate.id"}, 
					new Object[] {emailTemplate.getId()} );
			for (int i = 0; i < l.size(); i++) {
				EvalEvaluation eval = (EvalEvaluation) l.get(i);
				// check eval/template permissions
				checkEvalTemplateControl(userId, eval, emailTemplate);
			}
		}

		// save the template if allowed
		dao.save(emailTemplate);
		log.info("User ("+userId+") saved email template ("+emailTemplate.getId()+")");
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#getDefaultEmailTemplate(int)
	 */
	public EvalEmailTemplate getDefaultEmailTemplate(String emailTemplateTypeConstant) {
		log.debug("emailTemplateTypeConstant: " + emailTemplateTypeConstant);

		// check and get type
		String templateType;
		if (EvalConstants.EMAIL_TEMPLATE_CREATED.equals(emailTemplateTypeConstant)) {
			templateType = EvalConstants.EMAIL_TEMPLATE_DEFAULT_CREATED;	
		} else if (EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailTemplateTypeConstant)) {
			templateType = EvalConstants.EMAIL_TEMPLATE_DEFAULT_AVAILABLE;			
		} else if (EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailTemplateTypeConstant)) {
			templateType = EvalConstants.EMAIL_TEMPLATE_DEFAULT_REMINDER;
		} else if (EvalConstants.EMAIL_TEMPLATE_RESULTS.equals(emailTemplateTypeConstant)) {
				templateType = EvalConstants.EMAIL_TEMPLATE_DEFAULT_RESULTS;	
		} else {
			throw new IllegalArgumentException("Invalid emailTemplateTypeConstant: " + emailTemplateTypeConstant);			
		}

		// fetch template by type
		List l = dao.findByProperties(EvalEmailTemplate.class, 
				new String[] {"defaultType"}, 
				new Object[] {templateType} );
		if (l.isEmpty()) {
			throw new IllegalStateException("Could not find any default template for type constant: " +
					emailTemplateTypeConstant);
		}
		return (EvalEmailTemplate) l.get(0);
	}


	// PERMISSIONS

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#canControlEmailTemplate(java.lang.String, java.lang.Long, int)
	 */
	public boolean canControlEmailTemplate(String userId, Long evaluationId, String emailTemplateTypeConstant) {
		log.debug("userId: " + userId + ", evaluationId: " + evaluationId + ", emailTemplateTypeConstant: " + emailTemplateTypeConstant);

		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}

		// check the type constant
		EvalEmailTemplate emailTemplate;
		if (EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailTemplateTypeConstant)) {
			emailTemplate = eval.getAvailableEmailTemplate();
		} else if (EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailTemplateTypeConstant)) {
			emailTemplate = eval.getReminderEmailTemplate();
		} else {
			throw new IllegalArgumentException("Invalid emailTemplateTypeConstant: " + emailTemplateTypeConstant);			
		}

		// check the permissions and state
		try {
			return checkEvalTemplateControl(userId, eval, emailTemplate);
		} catch (RuntimeException e) {
			log.info( e.getMessage() );
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#canControlEmailTemplate(java.lang.String, java.lang.Long, java.lang.Long)
	 */
	public boolean canControlEmailTemplate(String userId, Long evaluationId, Long emailTemplateId) {
		log.debug("userId: " + userId + ", evaluationId: " + evaluationId + ", emailTemplateId: " + emailTemplateId);

		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}

		// get the email template
		EvalEmailTemplate emailTemplate = (EvalEmailTemplate) dao.findById(EvalEmailTemplate.class, emailTemplateId);
		if (emailTemplate == null) {
			throw new IllegalArgumentException("Cannot find email template with this id: " + emailTemplateId);
		}

		// make sure this template is associated with this evaluation
		if ( emailTemplate.equals( eval.getAvailableEmailTemplate() ) ) {
			log.debug("template matches available template from eval ("+eval.getId()+")");
		} else if ( emailTemplate.equals( eval.getReminderEmailTemplate() ) ) {
			log.debug("template matches reminder template from eval ("+eval.getId()+")");
		} else {
			throw new IllegalArgumentException("email template ("+
					emailTemplate.getId()+") does not match any template from eval ("+eval.getId()+")");
		}

		// check the permissions and state
		try {
			return checkEvalTemplateControl(userId, eval, emailTemplate);
		} catch (RuntimeException e) {
			log.info( e.getMessage() );
		}
		return false;
	}

	// INTERNAL METHODS

	/**
	 * Check if user can control an email template
	 * @param userId
	 * @param emailTemplate
	 * @return true if they can
	 */
	protected boolean canUserControlEmailTemplate(String userId, EvalEmailTemplate emailTemplate) {
		if ( external.isUserAdmin(userId) ) {
			return true;
		} else if ( emailTemplate.getOwner().equals(userId) ) {
			return true;
		}
		return false;
	}

	/**
	 * Check if user can control evaluation and template combo
	 * @param userId
	 * @param eval
	 * @param emailTemplate
	 * @return true if they can, throw exceptions otherwise
	 */
	protected boolean checkEvalTemplateControl(String userId, EvalEvaluation eval, EvalEmailTemplate emailTemplate) {
		log.debug("userId: " + userId + ", evaluationId: " + eval.getId());

		if ( EvalUtils.getEvaluationState(eval) == EvalConstants.EVALUATION_STATE_INQUEUE ) {
			if (emailTemplate == null) {
				// currently using the default templates so check eval perms

				// check eval user permissions (just owner and super at this point)
				// TODO - find a way to centralize this check
				if (userId.equals(eval.getOwner()) ||
						external.isUserAdmin(userId)) {
					return true;
				} else {
					throw new SecurityException("User ("+userId+") cannot control email template in evaluation ("+eval.getId()+"), do not have permission");
				}
			} else {
				// check email template perms
				if (canUserControlEmailTemplate(userId, emailTemplate)) {
					return true;
				} else {
					throw new SecurityException("User ("+userId+") cannot control email template ("+emailTemplate.getId()+") without permissions");
				}
			}
		} else {
			throw new IllegalStateException("Cannot modify email template in running evaluation ("+eval.getId()+")");
		}
	}


	// sending emails

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalCreatedNotifications(java.lang.Long, boolean)
	 */
	public String[] sendEvalCreatedNotifications(Long evaluationId, boolean includeOwner) {
		log.debug("evaluationId: " + evaluationId + ", includeOwner: " + includeOwner);

		String from = (String) settings.get( EvalSettings.FROM_EMAIL_ADDRESS );

		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}

		// get the email template
		EvalEmailTemplate emailTemplate = getDefaultEmailTemplate( EvalConstants.EMAIL_TEMPLATE_CREATED );
		if (emailTemplate == null) {
			throw new IllegalStateException("Cannot find email template: " + EvalConstants.EMAIL_TEMPLATE_CREATED);
		}

		// get the associated contexts for this evaluation
		Map evalGroups = evaluationLogic.getEvaluationGroups(new Long[] {evaluationId}, true);
		// only one possible map key so we can assume evaluationId
		List groups = (List) evalGroups.get(evaluationId);
		log.debug("Found " + groups.size() + " contexts for new evaluation: " + evaluationId);

		List sentMessages = new ArrayList();
		// loop through contexts and send emails to correct users in each context
		for (int i=0; i<groups.size(); i++) {
			EvalGroup group = (EvalGroup) groups.get(i);
			Set userIdsSet = external.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_BE_EVALUATED);
			if (! includeOwner && userIdsSet.contains(eval.getOwner())) {
				userIdsSet.remove(eval.getOwner());
			}

			// skip ahead if there is no one to send to
			if (userIdsSet.size() == 0) continue;

			// turn the set into an array
			String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
			log.debug("Found " + toUserIds.length + " users (" + toUserIds + 
					") to send " + EvalConstants.EMAIL_TEMPLATE_CREATED + 
					" notification to for new evaluation (" + evaluationId + 
					") and context (" + group.evalGroupId + ")");

			// replace the text of the template with real values
			Map replacementValues = new HashMap();
			replacementValues.put("HelpdeskEmail", from);
			String message = makeEmailMessage(emailTemplate.getMessage(), 
					eval, group, replacementValues);

			// store sent messages to return
			sentMessages.add(message);

			// send the actual emails for this context
			external.sendEmails(from, 
					toUserIds, 
					"New evaluation created: " + eval.getTitle(), 
					message);
			log.info("Sent evaluation created message to " + toUserIds.length + " users");
		}

		return (String[]) sentMessages.toArray( new String[] {} );
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.evaluation.logic.EvalEmailsLogic#sendEvalAvailableNotifications(java.lang.Long, boolean)
	 */
	public String[] sendEvalAvailableNotifications(Long evaluationId, boolean includeEvaluatees) {
		log.debug("evaluationId: " + evaluationId + ", includeEvaluatees: " + includeEvaluatees);

		String from = (String) settings.get( EvalSettings.FROM_EMAIL_ADDRESS );

		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}

		// get the email template
		EvalEmailTemplate emailTemplate = getDefaultEmailTemplate( EvalConstants.EMAIL_TEMPLATE_AVAILABLE );
		if (emailTemplate == null) {
			throw new IllegalStateException("Cannot find email template: " + EvalConstants.EMAIL_TEMPLATE_AVAILABLE);
		}

		// get the associated eval groups for this evaluation
		Map evalGroupIds = evaluationLogic.getEvaluationGroups(new Long[] {evaluationId}, false);
		// only one possible map key so we can assume evaluationId
		List groups = (List) evalGroupIds.get(evaluationId);
		log.debug("Found " + groups.size() + " groups for available evaluation: " + evaluationId);

		List sentMessages = new ArrayList();
		// loop through contexts and send emails to correct users in each context
		for (int i=0; i<groups.size(); i++) {
			EvalGroup group = (EvalGroup) groups.get(i);
			Set userIdsSet = external.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_TAKE_EVALUATION);

			// skip ahead if there is no one to send to
			if (userIdsSet.size() == 0) continue;

			// turn the set into an array
			String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
			log.debug("Found " + toUserIds.length + " users (" + toUserIds + 
					") to send " + EvalConstants.EMAIL_TEMPLATE_CREATED + 
					" notification to for available evaluation (" + evaluationId + 
					") and group (" + group.evalGroupId + ")");

			// replace the text of the template with real values
			Map replacementValues = new HashMap();
			replacementValues.put("HelpdeskEmail", from);
			String message = makeEmailMessage(emailTemplate.getMessage(), 
					eval, group, replacementValues);

			// store sent messages to return
			sentMessages.add(message);

			// send the actual emails for this context
			external.sendEmails(from, 
					toUserIds, 
					"New evaluation created: " + eval.getTitle(), 
					message);
			log.info("Sent evaluation available message to " + toUserIds.length + " users");

			if (includeEvaluatees) {
				// TODO Not done yet
				log.error("includeEvaluatees Not implemented");
			}
		}

		return (String[]) sentMessages.toArray( new String[] {} );
	}
	
	public String[] sendEvalReminderNotifications(Long evaluationId, String includeConstant) {
		log.debug("evaluationId: " + evaluationId + ", includeConstant: " + includeConstant);
		if(includeConstant == null || 
				!(includeConstant == EvalConstants.EMAIL_INCLUDE_NONTAKERS || includeConstant ==  EvalConstants.EMAIL_INCLUDE_ALL)) {
			log.error("includeConstant null or unknown");
			return null;
		}

		String from = (String) settings.get( EvalSettings.FROM_EMAIL_ADDRESS );
		
		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}
		
		// get the email template
		EvalEmailTemplate emailTemplate = getDefaultEmailTemplate( EvalConstants.EMAIL_TEMPLATE_REMINDER );

		if (emailTemplate == null) {
			throw new IllegalStateException("Cannot find email template: " + EvalConstants.EMAIL_TEMPLATE_REMINDER);
		}

		List sentMessages = new ArrayList();

		// get the associated eval groups for this evaluation
		Map evalGroupIds = evaluationLogic.getEvaluationGroups(new Long[] {evaluationId}, false);

		// only one possible map key so we can assume evaluationId
		List groups = (List) evalGroupIds.get(evaluationId);
		log.debug("Found " + groups.size() + " groups for available evaluation: " + evaluationId);
		Set userIdsSet = new HashSet();

		// loop through contexts and send emails to correct users in each context
		for (int i=0; i<groups.size(); i++) {
			EvalGroup group = (EvalGroup) groups.get(i);
			userIdsSet.clear();
			if(EvalConstants.EMAIL_INCLUDE_NONTAKERS.equals(includeConstant)) {
				userIdsSet.addAll(evalResponsesLogic.getNonResponders(evaluationId, group));
			}
			else if(EvalConstants.EMAIL_INCLUDE_ALL.equals(includeConstant)) {
				userIdsSet.addAll(evalResponsesLogic.getNonResponders(evaluationId, group));
				userIdsSet.addAll(external.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_BE_EVALUATED));
			}

			// skip ahead if there is no one to send to
			if (userIdsSet.size() == 0) continue;

			// turn the set into an array
			String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
			log.debug("Found " + toUserIds.length + " users (" + toUserIds + 
					") to send " + EvalConstants.EMAIL_TEMPLATE_REMINDER + 
					" notification to for available evaluation (" + evaluationId + 
					") and group (" + group.evalGroupId + ")");

			// replace the text of the template with real values
			Map replacementValues = new HashMap();
			replacementValues.put("HelpdeskEmail", from);
			String message = makeEmailMessage(emailTemplate.getMessage(), 
					eval, group, replacementValues);
			
			// store sent messages to return
			sentMessages.add(message);

			// send the actual emails for this context
			external.sendEmails(from, 
					toUserIds, 
					"New evaluation created: " + eval.getTitle(), 
					message);
			log.info("Sent evaluation available message to " + toUserIds.length + " users");
		}
		return (String[]) sentMessages.toArray( new String[] {} );
	}
	
	public String[] sendEvalResultsNotifications(Long evaluationId, boolean includeEvaluatees, boolean includeAdmins) {
		log.debug("evaluationId: " + evaluationId + ", includeEvaluatees: " + includeEvaluatees + ", includeAdmins: " + includeAdmins);
		String from = (String) settings.get( EvalSettings.FROM_EMAIL_ADDRESS );

		// get evaluation
		EvalEvaluation eval = (EvalEvaluation) dao.findById(EvalEvaluation.class, evaluationId);
		if (eval == null) {
			throw new IllegalArgumentException("Cannot find evaluation with this id: " + evaluationId);
		}

		// get the email template
		EvalEmailTemplate emailTemplate = getDefaultEmailTemplate(EvalConstants.EMAIL_TEMPLATE_RESULTS);

		if (emailTemplate == null) {
			throw new IllegalStateException("Cannot find email template: " + EvalConstants.EMAIL_TEMPLATE_RESULTS);
		}

		List sentMessages = new ArrayList();

		// get the associated eval groups for this evaluation
		Map evalGroupIds = evaluationLogic.getEvaluationGroups(new Long[] {evaluationId}, false);

		// only one possible map key so we can assume evaluationId
		List groups = (List) evalGroupIds.get(evaluationId);
		log.debug("Found " + groups.size() + " groups for available evaluation: " + evaluationId);
		Set userIdsSet = new HashSet();

		// loop through contexts and send emails to correct users in each context
		for (int i=0; i<groups.size(); i++) {
			EvalGroup group = (EvalGroup) groups.get(i);
			userIdsSet.clear();
			if(includeEvaluatees) {
				userIdsSet.addAll(external.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_TAKE_EVALUATION));
			}
			if(includeAdmins) {
				userIdsSet.addAll(external.getUserIdsForEvalGroup(group.evalGroupId, EvalConstants.PERM_BE_EVALUATED));
			}

			// skip ahead if there is no one to send to
			if (userIdsSet.size() == 0) continue;

			// turn the set into an array
			String[] toUserIds = (String[]) userIdsSet.toArray(new String[] {});
			log.debug("Found " + toUserIds.length + " users (" + toUserIds + 
					") to send " + EvalConstants.EMAIL_TEMPLATE_REMINDER + 
					" notification to for available evaluation (" + evaluationId + 
					") and group (" + group.evalGroupId + ")");

			// replace the text of the template with real values
			Map replacementValues = new HashMap();
			replacementValues.put("HelpdeskEmail", from);
			String message = makeEmailMessage(emailTemplate.getMessage(), 
					eval, group, replacementValues);

			// store sent messages to return
			sentMessages.add(message);

			// send the actual emails for this context
			external.sendEmails(from, 
					toUserIds, 
					"New evaluation created: " + eval.getTitle(), 
					message);
			log.info("Sent evaluation available message to " + toUserIds.length + " users");

			/*TODO 
			if(EvalConstants.EMAIL_INCLUDE_ALL.equals(includeConstant)) {
			}
			boolean includeEvaluatees = true;
			if (includeEvaluatees) {
				// TODO Not done yet
				log.error("includeEvaluatees Not implemented");
			}
		}
		return (String[]) sentMessages.toArray( new String[] {} );
		*/
		}
		return null;
	}



	/**
	 * Builds the email message from a template and a bunch of variables
	 * (passed in and otherwise)
	 * 
	 * @param messageTemplate
	 * @param eval
	 * @param group
	 * @param replacementValues a map of String -> String representing $keys in the template to replace with text values
	 * @return the processed message template with replacements and logic handled
	 */
	private String makeEmailMessage(String messageTemplate, EvalEvaluation eval, EvalGroup group, Map replacementValues) {
		// replace the text of the template with real values
		if (replacementValues == null) {
			replacementValues = new HashMap();
		}
		replacementValues.put("EvalTitle", eval.getTitle() );

		// TODO - use a date which is related to the current users locale
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM); //, locale);

		replacementValues.put("EvalStartDate", df.format(eval.getStartDate()) );
		replacementValues.put("EvalDueDate", df.format(eval.getDueDate()) );
		replacementValues.put("EvalResultsDate", df.format(eval.getViewDate()) );
		replacementValues.put("EvalGroupTitle", group.title);

		// TODO check these URLS once I can get the right tool url
		replacementValues.put("URLtoAddItems", 
				external.getToolUrl() + "/instructor_add?evaluationId=" + eval.getId());
		replacementValues.put("URLtoTakeEval", 
				external.getToolUrl() + "/take_eval?evaluationId=" + eval.getId() +
				"&evalGroupId=" + group.evalGroupId);
		replacementValues.put("URLtoViewResults", 
				external.getToolUrl() + "/view_report?evaluationId=" + eval.getId());
		replacementValues.put("URLtoSystem", external.getServerUrl());

		return TextTemplateLogicUtils.processTextTemplate(messageTemplate, replacementValues);
	}

}
