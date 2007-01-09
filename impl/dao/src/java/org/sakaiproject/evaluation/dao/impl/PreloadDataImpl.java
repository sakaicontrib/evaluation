/******************************************************************************
 * PreloadDataImpl.java - created by aaronz@vt.edu
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

package org.sakaiproject.evaluation.dao.impl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvaluationSettingsParse;
import org.sakaiproject.evaluation.model.EvalConfig;
import org.sakaiproject.evaluation.model.EvalEmailTemplate;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;


/**
 * This checks and preloads any data that is needed for the evaluation app
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class PreloadDataImpl {

	private static final String ADMIN_OWNER = "admin";
	private static Log log = LogFactory.getLog(PreloadDataImpl.class);

	private EvaluationDao evaluationDao;

	public void setEvaluationDao(EvaluationDao evaluationDao) {
		this.evaluationDao = evaluationDao;
	}

	public void init() {
		preloadEvalConfig();
		preloadEmailTemplate();
		preloadScales();
	}

	/**
	 * Preload the default system configuration settings
	 */
	public void preloadEvalConfig(){

		//check if there are any EvalConfig items present
		if(evaluationDao.findAll(EvalConfig.class).isEmpty()) {

			// Default Instructor system settings
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS),
					Boolean.TRUE.toString()) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS),
					Boolean.TRUE.toString()) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS),
					Boolean.TRUE.toString()) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE),
					Boolean.FALSE.toString()) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER),
					Integer.valueOf(5).toString()) );

			// Default Student settings
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED),
					Boolean.TRUE.toString()) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.STUDENT_MODIFY_RESPONSES),
					Boolean.FALSE.toString()) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.STUDENT_VIEW_RESULTS),
					Boolean.FALSE.toString()) );

			// Default Admin settings
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.ADMIN_ADD_ITEMS_NUMBER),
					Integer.valueOf(5).toString()) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.ADMIN_VIEW_BELOW_RESULTS),
					Boolean.FALSE.toString()) );

			// Default general settings
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.STANDARD_EVALUATION_TERM),
					"Evaluation") );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.FROM_EMAIL_ADDRESS),
					"helpdesk@institution.edu") );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS),
					Integer.valueOf(5).toString()) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.NOT_AVAILABLE_ALLOWED),
					Boolean.TRUE.toString()) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.ITEMS_ALLOWED_IN_QUESTION_BLOCK),
					Integer.valueOf(10).toString()) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY),
					EvalConstants.SHARING_OWNER) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.USE_EXPERT_TEMPLATES),
					Boolean.TRUE.toString()) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.USE_EXPERT_ITEMS),
					Boolean.TRUE.toString()) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.REQUIRE_COMMENTS_BLOCK),
					Boolean.TRUE.toString()) );
			evaluationDao.save( new EvalConfig( new Date(), 
					EvaluationSettingsParse.getName(
						EvalSettings.EVAL_RECENTLY_CLOSED_DAYS),
					Integer.valueOf(10).toString()) );

			log.info("Preloaded " + evaluationDao.countAll(EvalConfig.class) + " evaluation system EvalConfig items");
		}
	}

	/**
	 * Preload the default email template
	 */
	public void preloadEmailTemplate(){

		//check if there are any emailTemplates present
		if(evaluationDao.findAll(EvalEmailTemplate.class).isEmpty()) {

			evaluationDao.save(new EvalEmailTemplate(new Date(), ADMIN_OWNER,
					EvalConstants.EMAIL_AVAILABLE_DEFAULT_TEXT, 
					EvalConstants.EMAIL_TEMPLATE_DEFAULT_AVAILABLE));
			evaluationDao.save(new EvalEmailTemplate(new Date(), ADMIN_OWNER,
					EvalConstants.EMAIL_REMINDER_DEFAULT_TEXT, 
					EvalConstants.EMAIL_TEMPLATE_DEFAULT_REMINDER));

			log.info("Preloaded " + evaluationDao.countAll(EvalEmailTemplate.class) + " evaluation EmailTemplates");
		}
	}
	
	/**
	 * Preload the default expert built scales into the database
	 */
	public void preloadScales() {

		// check if there are any scales present
		if(evaluationDao.findAll(EvalScale.class).isEmpty()) {

			// NOTE: If you change the number of hidden scales on this page then
			// you will need to change the testFindByExample test in EvaluationDaoImplTest also

			// initial VT scales
			String[] options1 = {"Strongly Disagree","Disagree","Uncertain","Agree","Strongly agree"};
			evaluationDao.save(new EvalScale(new Date(), 
					ADMIN_OWNER, "Agree disagree scale", 
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_HIGH,	options1, Boolean.TRUE) );

			String[] options2 = {"Hardly ever","Occasionally","Sometimes","Frequently","Always"};
			evaluationDao.save(new EvalScale(new Date(), 
					ADMIN_OWNER, "Frequency scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_NONE, options2, Boolean.TRUE) );

			String[] options3 = {"Poor","Fair","Good","Excellent"};
			evaluationDao.save(new EvalScale(new Date(),
					ADMIN_OWNER, "Relative rating scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_HIGH, options3, Boolean.TRUE) );

			String[] options4 = {"Less than Average","Average","More than Average"};
			evaluationDao.save(new EvalScale(new Date(),
					ADMIN_OWNER, "Averages scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_HIGH, options4, Boolean.TRUE) );

			// initial demographic scales
			String[] options5 = {"Female","Male"};
			evaluationDao.save(new EvalScale(new Date(),
					ADMIN_OWNER, "Gender scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_NONE, options5, Boolean.TRUE) );

			String[] options6 = {"Req. in Major","Req. out of Major","Elective filling Req.","Free Elec. in Major","Free Elec. out of Major"};
			evaluationDao.save(new EvalScale(new Date(),
					ADMIN_OWNER, "Class requirements scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_NONE, options6, Boolean.TRUE) );

			String[] options7 = {"Fresh","Soph","Junior","Senior","Master","Doctoral"};
			evaluationDao.save(new EvalScale(new Date(),
					ADMIN_OWNER, "Student year scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_NONE, options7, Boolean.TRUE) );

			String[] options8 = {"F","D","C","B","A","Pass"};
			evaluationDao.save(new EvalScale(new Date(),
					ADMIN_OWNER, "Student grade scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_HIGH, options8, Boolean.TRUE) );

			String[] options9 = {"MGT","MSCI","MKTG","FIN","ACIS","ECON","OTHER"};
			evaluationDao.save(new EvalScale(new Date(),
					ADMIN_OWNER, "Business major scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_HIGH, options9, Boolean.TRUE) );

			String[] options10 = {"Freshman","Sophomore","Junior","Senior","Graduate"};
			evaluationDao.save(new EvalScale(new Date(),
					ADMIN_OWNER, "Business student yr scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_HIGH, options10, Boolean.TRUE) );
			
			// initial expert scales
			String[] optionsA = {"Not effective","Somewhat effective","Moderately effective","Effective","Very effective"};
			evaluationDao.save(new EvalScale(new Date(),
					ADMIN_OWNER, "Effectiveness scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_HIGH, optionsA, Boolean.TRUE) );

			String[] optionsB = {"Unsatisfactory","Inadequate","Adequate","Good","Excellent"};
			evaluationDao.save(new EvalScale(new Date(),
					ADMIN_OWNER, "Adequate scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_HIGH, optionsB, Boolean.TRUE) );

			String[] optionsC = {"Much less","Less","Some","More","Much more"};
			evaluationDao.save(new EvalScale(new Date(),
					ADMIN_OWNER, "Relationships scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_NONE, optionsC, Boolean.TRUE) );

			String[] optionsD = {"Very low","High","Moderately high","High","Very high"};
			evaluationDao.save(new EvalScale(new Date(),
					ADMIN_OWNER, "Low high scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_HIGH, optionsD, Boolean.TRUE) );

			String[] optionsE = {"Too slow", "Appropriate", "Too fast"};
			evaluationDao.save(new EvalScale(new Date(),
					ADMIN_OWNER, "Speed scale",
					EvalConstants.SHARING_PUBLIC, Boolean.TRUE, 
					"", 
					EvalConstants.SCALE_IDEAL_MID, optionsE, Boolean.TRUE) );
			
			log.info("Preloaded " + evaluationDao.countAll(EvalScale.class) + " evaluation scales");
		}
	}
}
