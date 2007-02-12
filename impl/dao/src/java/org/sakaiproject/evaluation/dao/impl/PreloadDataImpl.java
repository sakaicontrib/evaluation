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
 * Antranig Basman (antranig@caret.cam.ac.uk)
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

	private static Log log = LogFactory.getLog(PreloadDataImpl.class);

	private static final String ADMIN_OWNER = "admin";

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
	 * Preload the default system configuration settings<br/> <b>Note:</b> If
	 * you attempt to save a null value here in the preload it will cause this
	 * to fail, just comment out or do not include the setting you want to
	 * "save" as null to have the effect without causing a failure
	 */
	public void preloadEvalConfig() {
		// check if there are any EvalConfig items present
		if (evaluationDao.findAll(EvalConfig.class).isEmpty()) {
			// Default Instructor system settings
			saveConfig(EvalSettings.INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS, true);
			saveConfig(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS, true);
			saveConfig(EvalSettings.INSTRUCTOR_ALLOWED_EMAIL_STUDENTS, true);
			// leave this out to allow setting in the evals
			// saveConfig(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE),
			// EvalConstants.INSTRUCTOR_REQUIRED));

			saveConfig(EvalSettings.INSTRUCTOR_ADD_ITEMS_NUMBER, 5);

			// Default Student settings
			saveConfig(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED, true);
			saveConfig(EvalSettings.STUDENT_MODIFY_RESPONSES, false);
			saveConfig(EvalSettings.STUDENT_VIEW_RESULTS, false);

			// Default Admin settings
			saveConfig(EvalSettings.ADMIN_ADD_ITEMS_NUMBER, 5);
			saveConfig(EvalSettings.ADMIN_VIEW_BELOW_RESULTS, false);
			saveConfig(EvalSettings.ADMIN_VIEW_INSTRUCTOR_ADDED_RESULTS, false);

			// Default general settings
			saveConfig(EvalSettings.FROM_EMAIL_ADDRESS, "helpdesk@institution.edu");
			saveConfig(EvalSettings.RESPONSES_REQUIRED_TO_VIEW_RESULTS, 5);
			saveConfig(EvalSettings.NOT_AVAILABLE_ALLOWED, true);
			saveConfig(EvalSettings.ITEMS_ALLOWED_IN_QUESTION_BLOCK, 10);
			saveConfig(EvalSettings.TEMPLATE_SHARING_AND_VISIBILITY, EvalConstants.SHARING_OWNER);
			saveConfig(EvalSettings.USE_EXPERT_TEMPLATES, true);
			saveConfig(EvalSettings.USE_EXPERT_ITEMS, true);
			saveConfig(EvalSettings.REQUIRE_COMMENTS_BLOCK, true);
			saveConfig(EvalSettings.EVAL_RECENTLY_CLOSED_DAYS, 10);

			// default is configurable (unset)
			//saveConfig(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY, false);
			saveConfig(EvalSettings.EVAL_USE_STOP_DATE, false);
			saveConfig(EvalSettings.EVAL_USE_SAME_VIEW_DATES, true);

			log.info("Preloaded " + evaluationDao.countAll(EvalConfig.class) + " evaluation system EvalConfig items");
		}
	}

	private void saveConfig(String key, boolean value) {
		saveConfig(key, value ? "true" : "false");
	}

	private void saveConfig(String key, int value) {
		saveConfig(key, Integer.toString(value));
	}

	private void saveConfig(String key, String value) {
		evaluationDao.save(new EvalConfig(new Date(), EvaluationSettingsParse.getName(key), value));
	}


	/**
	 * Preload the default email template
	 */
	public void preloadEmailTemplate() {

		// check if there are any emailTemplates present
		if (evaluationDao.findAll(EvalEmailTemplate.class).isEmpty()) {

			evaluationDao.save(new EvalEmailTemplate(new Date(), ADMIN_OWNER,
					EvalConstants.EMAIL_AVAILABLE_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_DEFAULT_AVAILABLE));
			evaluationDao.save(new EvalEmailTemplate(new Date(), ADMIN_OWNER,
					EvalConstants.EMAIL_REMINDER_DEFAULT_TEXT, EvalConstants.EMAIL_TEMPLATE_DEFAULT_REMINDER));

			log.info("Preloaded " + evaluationDao.countAll(EvalEmailTemplate.class) + " evaluation EmailTemplates");
		}
	}


	/**
	 * Preload the default expert built scales into the database
	 */
	public void preloadScales() {

		// check if there are any scales present
		if (evaluationDao.findAll(EvalScale.class).isEmpty()) {
			// NOTE: If you change the number of hidden scales on this page then
			// you will need to change the testFindByExample test in
			// EvaluationDaoImplTest also

			// initial VT scales
			saveScale("Agree disagree scale", new String[] { "Strongly Disagree", "Disagree", "Uncertain", "Agree",
					"Strongly agree" });
			saveScale("Frequency scale", new String[] { "Hardly ever", "Occasionally", "Sometimes", "Frequently",
					"Always" });
			saveScale("Relative rating scale", new String[] { "Poor", "Fair", "Good", "Excellent" });
			saveScale("Averages scale", new String[] { "Less than Average", "Average", "More than Average" });
			// initial demographic scales
			saveScale("Gender scale", new String[] { "Female", "Male" });
			saveScale("Class requirements scale", new String[] { "Req. in Major", "Req. out of Major",
					"Elective filling Req.", "Free Elec. in Major", "Free Elec. out of Major" });
			saveScale("Student year scale", new String[] { "Fresh", "Soph", "Junior", "Senior", "Master", "Doctoral" });
			saveScale("Student grade scale", new String[] { "F", "D", "C", "B", "A", "Pass" });
			saveScale("Business major scale", new String[] { "MGT", "MSCI", "MKTG", "FIN", "ACIS", "ECON", "OTHER" });
			saveScale("Business student yr scale", new String[] { "Freshman", "Sophomore", "Junior", "Senior",
					"Graduate" });
			// initial expert scales
			saveScale("Effectiveness scale", new String[] { "Not effective", "Somewhat effective",
					"Moderately effective", "Effective", "Very effective" });
			saveScale("Adequacy scale",
					new String[] { "Unsatisfactory", "Inadequate", "Adequate", "Good", "Excellent" });
			saveScale("Relationships scale", new String[] { "Much less", "Less", "Some", "More", "Much more" });
			saveScale("Low high scale", new String[] { "Very low", "High", "Moderately high", "High", "Very high" });
			saveScale("Speed scale", new String[] { "Too slow", "Appropriate", "Too fast" });

			log.info("Preloaded " + evaluationDao.countAll(EvalScale.class) + " evaluation scales");
		}
	}

	private void saveScale(String title, String[] options) {
		evaluationDao.save(new EvalScale(new Date(), ADMIN_OWNER, title, EvalConstants.SHARING_PUBLIC, Boolean.TRUE,
				"", EvalConstants.SCALE_IDEAL_HIGH, options, Boolean.TRUE));
	}

}
