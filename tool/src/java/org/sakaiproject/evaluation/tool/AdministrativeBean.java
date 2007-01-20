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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import java.util.List;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.logic.EvalAssignsLogic;
import org.sakaiproject.evaluation.logic.EvalEmailsLogic;
import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalResponsesLogic;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvalTemplatesLogic;
import org.sakaiproject.evaluation.model.EvalAnswer;
import org.sakaiproject.evaluation.model.EvalAssignContext;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.producers.AdministrateProducer;
import org.sakaiproject.evaluation.tool.producers.ControlPanelProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationAssignConfirmProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationAssignProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationSettingsProducer;
import org.sakaiproject.evaluation.tool.producers.EvaluationStartProducer;
import org.sakaiproject.evaluation.tool.producers.PreviewEvalProducer;
import org.sakaiproject.evaluation.tool.producers.SummaryProducer;

import sun.util.logging.resources.logging;



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
		
		//log.warn("Inside adminBeam!!, instViewResults = " + instViewResults);
		
		settings.set(EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER, new Integer(instQuestionAdd));
		
		return AdministrateProducer.VIEW_ID;
	}
}

