/******************************************************************************
 * AdministrativeBean.java - created by kahuja@vt.edu on Jan 19, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.tool.producers.AdministrateProducer;

/**
 * This is the backing bean of administrative functionality
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 */

public class AdministrativeBean {
	
	/*
	 * VARIABLE DECLARATIONS 
	 */
	//Instructor specific
	public Boolean instCreateTemplate;
	public String instViewResults;
	public Boolean instSendEmail;
	public String instUseEvalFromAbove;
	public String instQuestionAdd;
	
	//Student specific
	public String stuQuesUnanswered;
	public String stuModifyResponses;
	public String stuViewResults;

	//Admin specific
	public String adminsBelowAddQuestions;
	public Boolean adminViewInstQuestion;
	public Boolean adminSuperModifyQues;
	
	//General
	public String genHelpdeskEmail;
	public String genResponsesBeforeViewResult;
	public Boolean genNaAllowed;
	public String genMaxQuestionsInBlock;
	public String genTemplateSharing;
	public String genDefaultQuestionCategory;
	public Boolean genUseStopDate;
	public Boolean genUseExpertTemplate;
	public Boolean genUseExpertQuestion;
	public Boolean genUseSameViewDate;
	
	private static Log log = LogFactory.getLog(EvaluationBean.class);
	
	private EvalSettings settings;
	public void setSettings(EvalSettings settings) {
		this.settings = settings;
	}
	
	//method binding to the "Continue to Settings" button on evaluation_start.html
	public String saveAdminSettingsAction() {
		
		/*
		 * If checkboxes not selected then their default value is null instead of FALSE.
		 * This making those null values as FALSE.
		 */
		if (instCreateTemplate == null)
			instCreateTemplate = Boolean.FALSE;

		if (instSendEmail == null)
			instSendEmail = Boolean.FALSE;

		if (adminViewInstQuestion == null)
			adminViewInstQuestion = Boolean.FALSE;

		if (adminSuperModifyQues == null)
			adminSuperModifyQues = Boolean.FALSE;

		if (genNaAllowed == null)
			genNaAllowed = Boolean.FALSE;

		if (genUseStopDate == null)
			genUseStopDate = Boolean.FALSE;

		if (genUseExpertTemplate == null)
			genUseExpertTemplate = Boolean.FALSE;

		if (genUseExpertQuestion == null)
			genUseExpertQuestion = Boolean.FALSE;

		if (genUseSameViewDate == null)
			genUseSameViewDate = Boolean.FALSE;

		/*
		 * Note: The commented below are need to be fixed on EvalSettings.java.
		 *       To be checked with Aaron whether I could modify it - Kapil.
		 */
		
		//Instructor specific
		settings.set(EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS, instCreateTemplate);
		//settings.set(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, instViewResults);
		settings.set(EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS, instSendEmail);
		//settings.set(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE, instUseEvalFromAbove);
		settings.set(EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER, new Integer(instQuestionAdd));
		
		//Student specific
		//settings.set(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED, stuQuesUnanswered);
		//settings.set(EvalSettings.STUDENT_MODIFY_RESPONSES, stuModifyResponses);
		//settings.set(EvalSettings.STUDENT_VIEW_RESULTS, stuViewResults);
		
		//Admin specific
		settings.set(EvalSettings.ADMIN_ADD_ITEMS_NUMBER, new Integer(adminsBelowAddQuestions));
		//settings.set(EvalSettings.??????, adminViewInstQuestion);
		//settings.set(EvalSettings.??????, adminSuperModifyQues);
		
		//General
		settings.set(EvalSettings.FROM_EMAIL_ADDRESS, genHelpdeskEmail);
		settings.set(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS, new Integer(genResponsesBeforeViewResult));
		settings.set(EvalSettings.NOT_AVAILABLE_ALLOWED, genNaAllowed);
		settings.set(EvalSettings.ITEMS_ALLOWED_IN_QUESTION_BLOCK, new Integer(genMaxQuestionsInBlock));
		settings.set(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY, genTemplateSharing);
		//settings.set(EvalSettings.?????, genDefaultQuestionCategory);
		//settings.set(EvalSettings.?????, genUseStopDate);
		settings.set(EvalSettings.USE_EXPERT_TEMPLATES, genUseExpertTemplate);
		settings.set(EvalSettings.USE_EXPERT_ITEMS, genUseExpertQuestion);
		//settings.set(EvalSettings.?????, genUseSameViewDate);
		
		return AdministrateProducer.VIEW_ID;
	}
}

