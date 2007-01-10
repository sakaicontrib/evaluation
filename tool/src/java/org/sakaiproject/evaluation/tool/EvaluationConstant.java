/******************************************************************************
 * EvaluationConstant.java - created by fengr@vt.edu on Aug 21, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu)
 * Rui Feng (fengr@vt.edu)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool;

import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * This class holds the tool constants only, application data constants come from
 * the EvalConstants class in the model<br/>
 * NOTE: Make sure the constants are not already in the EvalConstants class
 * before adding them here
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationConstant {

	// TODO - remove all deprecated values BEFORE release -AZ

	//For template_title_desc.html
	public static String[] MODIFIER_VALUES = new String[] {
		EvalConstants.SHARING_PRIVATE, 
		EvalConstants.SHARING_VISIBLE,
		EvalConstants.SHARING_SHARED,
		EvalConstants.SHARING_PUBLIC
	};

	//For template_modify.html
	public static final String ITEM_TYPE_EXPERT = "Expert";
	
	/**
	 * Note that this only includes the types for items which are stored in
	 * the item, expert and the like should be added on to this and simply
	 * should use constants defined in this file somewhere as the VALUES
	 */
	public static String[] ITEM_CLASSIFICATION_VALUES = new String[] {
		EvalConstants.ITEM_TYPE_SCALED, 
		EvalConstants.ITEM_TYPE_TEXT, 
		EvalConstants.ITEM_TYPE_HEADER, 
		EvalConstants.ITEM_TYPE_BLOCK,
		ITEM_TYPE_EXPERT
	};
	

	
	//For template_modify and preview_item.html
	public static String[] STEPPED_IMAGE_URLS = new String[] {
		"$context/content/images/corner.gif",
		"$context/content/images/down-line.gif",
		"$context/content/images/down-arrow.gif" };

	//For preview_item.html
	public static String[] COLORED_IMAGE_URLS = new String[] {
		"$context/content/images/ideal-none.jpg",
		"$context/content/images/ideal-low.jpg",
		"$context/content/images/ideal-mid.jpg",
		"$context/content/images/ideal-high.jpg"};

	public static String BLUE_COLOR = "#6699ff";
	public static String GREEN_COLOR = "#00d600";
	public static String RED_COLOR = "#ff0000";
	public static String LIGHT_GRAY_COLOR = "#CCCCFF";

	//For template_item.html
	public static String[] SCALE_DISPLAY_SETTING_VALUES = new String[]{
		EvalConstants.ITEM_SCALE_DISPLAY_COMPACT,
		EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED,
		EvalConstants.ITEM_SCALE_DISPLAY_FULL,
		EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED,
		EvalConstants.ITEM_SCALE_DISPLAY_STEPPED,
		EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED,
		EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL
	};

	public static String[] ITEM_CATEGORY_VALUES = new String[] {
		EvalConstants.ITEM_CATEGORY_COURSE,
		EvalConstants.ITEM_CATEGORY_INSTRUCTOR
	};

	//Helpdesk email id (basically the id from which the emails are sent) - for evaluation_settings.html
	/**
	 * This should be using the value from EvaluationSettings
	 * @deprecated
	 */
	public static final String HELP_DESK_ID = "ocs@vt.edu";

	//For evaluation_settings.html
	public static final String[] REMINDER_EMAIL_DAYS_VALUES = new String[] {
		"0", "1", "2", "3", "4", "5", "6", "7" };

	public static final String[] INSTRUCTOR_OPT_VALUES = new String[] {
		EvalConstants.INSTRUCTOR_OPT_IN, 
		EvalConstants.INSTRUCTOR_OPT_OUT, 
		EvalConstants.INSTRUCTOR_REQUIRED
	};

	/**
	 * The default number of rows to use when displaying a textarea type input box
	 */
	public static final Integer DEFAULT_ROWS = new Integer(2);
}
