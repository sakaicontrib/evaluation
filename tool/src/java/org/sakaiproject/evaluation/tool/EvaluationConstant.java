/******************************************************************************
 * EvaluationConstant.java - created on Aug 21, 2006
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

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.producers.ModifyEssayProducer;
import org.sakaiproject.evaluation.tool.producers.ModifyHeaderProducer;
import org.sakaiproject.evaluation.tool.producers.ModifyScaledProducer;

/**
 * This class holds the tool constants only, application data constants come from
 * the EvalConstants class in the model<br/>
 * NOTE: Make sure the constants are not already in the EvalConstants class
 * before adding them here
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationConstant {

	/**
	 * The values for all sharing menus
	 */
	public static String[] MODIFIER_VALUES = new String[] {
		EvalConstants.SHARING_PRIVATE, 
		EvalConstants.SHARING_PUBLIC
//		EvalConstants.SHARING_VISIBLE,
//		EvalConstants.SHARING_SHARED
	};

	//For template_modify.html
	public static final String ITEM_TYPE_EXPERT = "Expert";
	public static final String ITEM_TYPE_EXISTING = "Existing";

	/**
	 * The values for the Add Item pulldown
	 * <b>Note:</b> This only includes the types for items which are stored in
	 * the item, expert and the like should be added on to this and simply
	 * should use constants defined in this file somewhere as the VALUES
	 */
	public static String[] ITEM_CLASSIFICATION_VALUES = new String[] {
		EvalConstants.ITEM_TYPE_SCALED, 
		EvalConstants.ITEM_TYPE_TEXT, 
		EvalConstants.ITEM_TYPE_HEADER, 
		EvalConstants.ITEM_TYPE_BLOCK_PARENT,
		ITEM_TYPE_EXPERT,
		ITEM_TYPE_EXISTING
	};

    private static Map classToView = new HashMap();
    
    static {
      classToView.put(EvalConstants.ITEM_TYPE_TEXT, ModifyEssayProducer.VIEW_ID);
      classToView.put(EvalConstants.ITEM_TYPE_HEADER, ModifyHeaderProducer.VIEW_ID);
      classToView.put(EvalConstants.ITEM_TYPE_SCALED, ModifyScaledProducer.VIEW_ID);
      // TODO: add remaining views here
    }
    /** For a given item classification, return the ID of the view which
     * deals with it.
     */
    public static String classificationToView(String classVal) {
      return(String) classToView.get(classVal);
    }

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
		"$context/content/images/ideal-high.jpg",
		"$context/content/images/ideal-outside.jpg"};

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

	/**
	 * Evaluation Settings: Page pulldown constants for reminder interval
	 */
	public static final String[] REMINDER_EMAIL_DAYS_VALUES = new String[] {
		"0", "1", "2", "3", "4", "5", "6", "7" };

	/**
	 * Evaluation settings: Values for instructor options for using evaluations from above
	 */
	public static final String[] INSTRUCTOR_OPT_VALUES = new String[] {
		EvalConstants.INSTRUCTOR_OPT_OUT, 
		EvalConstants.INSTRUCTOR_OPT_IN, 
		EvalConstants.INSTRUCTOR_REQUIRED
	};
	
	/**
	 * Modify Essay: Page pulldown constants for reponse size
	 */
	public static final String[] RESPONSE_SIZE_VALUES = new String[] {
		"2", "3", "4", "5" };
	
	/**
	 * The default number of rows to use when displaying a textarea type input box
	 */
	public static final Integer DEFAULT_ROWS = new Integer(2);
	
	// For main administrative page
	/**
	 * Defines the allowed values for the Integer constants in pulldowns
	 */
	public static final String[] PULLDOWN_INTEGER_VALUES = new String[] {
		"0", "1", "2", "3", "4", "5", "6", "7", "8", 
		"9", "10", "12", "15", "18", "20", "25", "50"};	

	/**
	 * Defines the allowed values for minimum time difference (in hours) 
	 * between start and due date of an evaluation.
	 */
	public static final String[] MINIMUM_TIME_DIFFERENCE = new String[] {
		"4", "8", "12", "16", "20", "24", "36", "48"};	
	
	/**
	 * Administrative (system settings) page, 
	 * values corresponding to "Yes", "No", "Configurable"  
	 */
	public static final String ADMIN_BOOLEAN_YES = "1";
	public static final String ADMIN_BOOLEAN_NO = "0";
	public static final String ADMIN_BOOLEAN_CONFIGURABLE = "-1";
	
	/**
	 * Used to pass the flag that this is a new scale between the 
	 * scale add modify and scale control pages. 
	 */ 
	public static final Long NEW_SCALE = new Long("-1");
}

