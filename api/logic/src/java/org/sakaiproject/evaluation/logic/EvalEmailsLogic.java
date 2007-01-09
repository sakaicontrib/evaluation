/******************************************************************************
 * EvalEmailLogic.java - created by aaronz@vt.edu on Dec 27, 2006
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

package org.sakaiproject.evaluation.logic;

import org.sakaiproject.evaluation.model.EvalEmailTemplate;

/**
 * Handles all logic associated with processing email and email templates
 * (Note for developers - do not modify this without permission from the project lead)
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalEmailsLogic {

	// EMAIL TEMPLATES

	/**
	 * Save or update an email template, don't forget to associate it
	 * with the evaluation and save that separately<br/> 
	 * <b>Note:</b> cannot update template if used in at least one 
	 * evaluation that is not in queue<br/>
	 * Use {@link #canControlEmailTemplate(String, Long, Long)} or
	 * {@link #canControlEmailTemplate(String, Long, String)} to check
	 * if user can update this template and avoid possible exceptions
	 * 
	 * @param EmailTemplate emailTemplate object to be saved
	 * @param userId the internal user id (not username)
	 */
	public void saveEmailTemplate(EvalEmailTemplate emailTemplate, String userId);

	/**
	 * Get a default email template by type, use the defaults as the basis for all
	 * new templates that are created by users
	 * 
	 * @param emailTemplateTypeConstant a constant, use the EMAIL_TEMPLATE constants from 
	 * {@link org.sakaiproject.evaluation.model.constant.EvalConstants} to indicate the type
	 * @return the default email template matching the supplied type
	 */
	public EvalEmailTemplate getDefaultEmailTemplate(int emailTemplateTypeConstant);


	// PERMISSIONS

	/**
	 * Check if a user can control (create, modify, or delete) an email template for the
	 * given evaluation of the given template type, takes into account the permissions and 
	 * current state of the evaluation
	 * 
	 * @param userId the internal user id (not username)
	 * @param evaluationId the id of an EvalEvaluation object
	 * @param emailTemplateTypeConstant a constant, use the EMAIL_TEMPLATE constants from 
	 * {@link org.sakaiproject.evaluation.model.constant.EvalConstants} to indicate the type
	 * @return true if the user can control the email template at this time, false otherwise
	 */
	public boolean canControlEmailTemplate(String userId, Long evaluationId, int emailTemplateTypeConstant);

	/**
	 * Check if a user can control (modify or delete) a given 
	 * email template for the given evaluation,
	 * takes into account the ownership, permissions, and current state of the evaluation
	 * 
	 * @param userId the internal user id (not username)
	 * @param evaluationId the id of an EvalEvaluation object
	 * @param emailTemplateId the id of an EvalEmailTemplate object
	 * @return true if the user can control the email template at this time, false otherwise
	 */
	public boolean canControlEmailTemplate(String userId, Long evaluationId, Long emailTemplateId);
}
