/******************************************************************************
 * EvaluationSettings.java - created by aaronz@vt.edu
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

import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * This allows access to and control of all system settings for the evaluation
 * system<br/>
 * <p>
 * INSTRUCTOR is a user with the be.evaluated permission set<br/>
 * STUDENT is a user with the take.evaluation permission set<br/>
 * ADMIN is a user with the adminstrate permission set (or Sakai super admin)<br/>
 * </p>
 * The public static final variables should be used when doing gets or sets of values (use the ones marked CONSTANT).<br/>
 * Example:<br/>
 * String s = (String) evalSettings.get(EvaluationSettings.FROM_EMAIL_ADDRESS);<br/>
 * <br/>
 * <b>Note:</b> Meant to be used internally in the evaluation system app only
 * (Note for developers - do not modify this without permission from the project lead)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public interface EvalSettings {

	/**
	 * CONSTANT: Is the instructor allowed to create evaluations - {@see java.lang.Boolean}, default True
	 */
	public static final String INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS = "INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS:java.lang.Boolean";
	/**
	 * CONSTANT: Is the instructor allowed to view the results of evaluations - {@see java.lang.Boolean}, default True
	 */
	public static final String INSTRUCTOR_ALLOWED_VIEW_RESULTS = "INSTRUCTOR_ALLOWED_VIEW_RESULTS:java.lang.Boolean";
	/**
	 * CONSTANT: Is the instructor allowed to send email reminders to students - {@see java.lang.Boolean}, default True
	 */
	public static final String INSTRUCTOR_ALLOWED_EMAIL_STUDENTS = "INSTRUCTOR_ALLOWED_EMAIL_STUDENTS:java.lang.Boolean";
	/**
	 * CONSTANT: Does the instructor have to use evaluations from above in the hierarchy - {@see java.lang.Boolean}, default False
	 */
	public static final String INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE = "INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE:java.lang.Boolean";
	/**
	 * CONSTANT: How many items is the instructor allowed to add to an evaluation from above in the hierarchy - {@see java.lang.Integer}, default 5
	 */
	public static final String INSTRUCTOR_ADD_ITEMS_NUMBER = "INSTRUCTOR_ADD_ITEMS_NUMBER:java.lang.Integer";

	/**
	 * CONSTANT: Student is allowed to leave questions unanswered (this only affects multiple choice items) - {@see java.lang.Boolean}, default True
	 */
	public static final String STUDENT_ALLOWED_LEAVE_UNANSWERED = "STUDENT_ALLOWED_LEAVE_UNANSWERED:java.lang.Boolean";
	/**
	 * CONSTANT: Student is allowed to modify their responses after they have submitted the evaluation but before the due date - {@see java.lang.Boolean}, default False
	 */
	public static final String STUDENT_MODIFY_RESPONSES = "STUDENT_MODIFY_RESPONSES:java.lang.Boolean";
	/**
	 * CONSTANT: Student is allowed to view the results of the evaluation - {@see java.lang.Boolean}, default False
	 */
	public static final String STUDENT_VIEW_RESULTS = "STUDENT_VIEW_RESULTS:java.lang.Boolean";

	/**
	 * CONSTANT: Admin is allowed to add this many items to an evaluation from above in the hierarchy - {@see java.lang.Integer}, default 5
	 */
	public static final String ADMIN_ADD_ITEMS_NUMBER = "ADMIN_ADD_ITEMS_NUMBER:java.lang.Integer";
	/**
	 * CONSTANT: Admin is allowed to view results from items added below them in the hierarchy - {@see java.lang.Boolean}, default False
	 */
	public static final String ADMIN_VIEW_BELOW_RESULTS = "ADMIN_VIEW_BELOW_RESULTS:java.lang.Boolean";

	/**
	 * CONSTANT: This is the standard term used within the system to refer to an Evaluation - {@see java.lang.String}, default "Evaluation"
	 */
	public static final String STANDARD_EVALUATION_TERM = "STANDARD_EVALUATION_TERM:java.lang.String";
	/**
	 * CONSTANT: This is the standard from email address used when sending reminders - {@see java.lang.String}, default "helpdesk@institution.edu"
	 */
	public static final String FROM_EMAIL_ADDRESS = "FROM_EMAIL_ADDRESS:java.lang.String";
	/**
	 * CONSTANT: Number of responses required before results are visible - {@see java.lang.Integer}, default 5
	 */
	public static final String RESPONSES_REQUIRED_TO_VIEW_RESULTS = "RESPONSES_REQUIRED_TO_VIEW_RESULTS:java.lang.Integer";
	/**
	 * CONSTANT: Are users allowed to use Not Available in templates and evaluations - {@see java.lang.Boolean}, default True
	 */
	public static final String NOT_AVAILABLE_ALLOWED = "NOT_AVAILABLE_ALLOWED:java.lang.Boolean";
	/**
	 * CONSTANT: Require a comments block to be included in every evaluation - {@see java.lang.Boolean}, default True
	 */
	public static final String REQUIRE_COMMENTS_BLOCK = "REQUIRE_COMMENTS_BLOCK:java.lang.Boolean";
	/**
	 * CONSTANT: How many items are allowed to be used with a question block - {@see java.lang.Integer}, default 10
	 */
	public static final String ITEMS_ALLOWED_IN_QUESTION_BLOCK = "ITEMS_ALLOWED_IN_QUESTION_BLOCK:java.lang.Integer";
	/**
	 * CONSTANT: What is the standard setting for sharing templates across the system - {@see java.lang.String}, default SHARING_OWNER<br/>
	 * <b>Note:</b> Use the EvalConstants.SHARING constants when setting or comparing this value
	 */
	public static final String TEMPLATE_SHARING_AND_VISIBILITY = "TEMPLATE_SHARING_AND_VISIBILITY:java.lang.String";
	/**
	 * CONSTANT: Allow users to use expert templates - {@see java.lang.Boolean}, default True
	 */
	public static final String USE_EXPERT_TEMPLATES = "USE_EXPERT_TEMPLATES:java.lang.Boolean";
	/**
	 * CONSTANT: Allow users to use expert items - {@see java.lang.Boolean}, default True
	 */
	public static final String USE_EXPERT_ITEMS = "USE_EXPERT_ITEMS:java.lang.Boolean";
	/**
	 * CONSTANT: How many days old can an eval be and still be recently closed - {@see java.lang.Integer}, default 10<br/>
	 * It must be less than or equal to this many days old to count as recent
	 */
	public static final String EVAL_RECENTLY_CLOSED_DAYS = "EVAL_RECENTLY_CLOSED_DAYS:java.lang.Integer";

	/**
	 * Defines the allowed values for the Integer constants in pulldowns
	 */
	public static final Integer[] PULLDOWN_INTEGER_VALUES = 
		new Integer[] {Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2),
			Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5),
			Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8),
			Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(15),
			Integer.valueOf(20), Integer.valueOf(25), Integer.valueOf(50)};

	/**
	 * Values to be used for the template sharing pulldown (sets order)<br/>
	 * Corresponds to the following:<br/>
	 * Owner - Visiblity set by owner (default)<br/>
	 * Private - Visible to owner only<br/>
	 * Visible - Visible to any admin<br/>
	 * Shared - Editable by same level<br/>
	 * Public - Editable by any admin
	 */
	public static final String[] PULLDOWN_TEMPLATE_SHARING_VALUES =
		new String[] {
			EvalConstants.SHARING_OWNER, 
			EvalConstants.SHARING_PRIVATE,
			EvalConstants.SHARING_VISIBLE, 
			EvalConstants.SHARING_SHARED, 
			EvalConstants.SHARING_PUBLIC
		};


	/**
	 * Allows for getting the value of a system setting based on the constant,
	 * do not forget to cast the returned Object based on the constant comment<br/>
	 * Example:<br/>
	 * String s = (String) evalSettings.get(EvaluationSettings.FROM_EMAIL_ADDRESS);<br/>
	 * (evalSettings is an injected {@see EvaluationSettings} bean)
	 * 
	 * @param settingConstant a constant from this interface
	 * @return the actual value of the setting referred to by this constant, 
	 * you should typecast the returned object (type will be the same as the constant),
	 * null if the object cannot be found
	 */
	public Object get(String settingConstant);

	/**
	 * Allows for setting the value of a system setting based on the constant<br/>
	 * <b>Note:</b> Setting a value to null will remove it and effectively set it to unspecified<br/>
	 * Example:<br/>
	 * boolean b = evalSettings.set(EvaluationSettings.FROM_EMAIL_ADDRESS, "aaronz@vt.edu");<br/>
	 * (evalSettings is an injected {@see EvaluationSettings} bean)
	 * 
	 * @param settingConstant a constant from this interface
	 * @param settingValue the value of the setting referred to by this constant (type must match the constant)
	 * @return true if the setting could be saved, false if not, throws runtime exceptions if invalid input
	 */
	public boolean set(String settingConstant, Object settingValue);

}
