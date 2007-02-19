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

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
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
		if (EvalConstants.EMAIL_TEMPLATE_AVAILABLE.equals(emailTemplateTypeConstant)) {
			templateType = EvalConstants.EMAIL_TEMPLATE_DEFAULT_AVAILABLE;			
		} else if (EvalConstants.EMAIL_TEMPLATE_REMINDER.equals(emailTemplateTypeConstant)) {
			templateType = EvalConstants.EMAIL_TEMPLATE_DEFAULT_REMINDER;			
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

	public String[] sendEvalCreatedNotifications(Long evaluationId, boolean includeOwner) {
		log.debug("evaluationId: " + evaluationId + ", includeOwner: " + includeOwner);
		// TODO Auto-generated method stub

		

		log.error("Method not implemented yet!");
		return null;

	}

	public String[] sendEvalAvailableNotifications(Long evaluationId, boolean includeEvaluatees) {
		log.debug("evaluationId: " + evaluationId + ", includeEvaluatees: " + includeEvaluatees);
		// TODO Auto-generated method stub
		
		log.error("Method not implemented yet!");
		return null;

	}

	public String[] sendEvalReminderNotifications(Long evaluationId, String includeConstant) {
		log.debug("evaluationId: " + evaluationId + ", includeConstant: " + includeConstant);
		// TODO Auto-generated method stub
		
		log.error("Method not implemented yet!");
		return null;

	}

	public String[] sendEvalResultsNotifications(Long evaluationId, boolean includeEvaluatees, boolean includeAdmins) {
		log.debug("evaluationId: " + evaluationId + ", includeEvaluatees: " + includeEvaluatees + ", includeAdmins: " + includeAdmins);
		// TODO Auto-generated method stub
		
		log.error("Method not implemented yet!");
		return null;

	}
}
