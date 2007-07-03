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
	public static String[] SHARING_VALUES = new String[] {
		EvalConstants.SHARING_PRIVATE, 
		EvalConstants.SHARING_PUBLIC
//		EvalConstants.SHARING_VISIBLE,
//		EvalConstants.SHARING_SHARED
	};

	// should match with SHARING_VALUES
	public static String[] SHARING_LABELS_PROPS = new String[] {
		"sharing.private",
		"sharing.public"
//		"sharing.visible",
//		"sharing.shared"
	};

	private static Map classToView = new HashMap();
	static {
		classToView.put(EvalConstants.ITEM_TYPE_SCALED, ModifyScaledProducer.VIEW_ID);
		classToView.put(EvalConstants.ITEM_TYPE_TEXT, ModifyEssayProducer.VIEW_ID);
		classToView.put(EvalConstants.ITEM_TYPE_HEADER, ModifyHeaderProducer.VIEW_ID);
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

    // should match the images
	public static String BLUE_COLOR = "#d7ebf6";
	public static String GREEN_COLOR = "#8be8a2";
	public static String RED_COLOR = "#ff8ba0";

	public static String LIGHT_BLUE_COLOR = "#CCCCFF";
	public static String LIGHT_GRAY_COLOR = "#E1E1E1";

	// For pulldowns which show the scale display settings
	public static String[] SCALE_DISPLAY_SETTING_VALUES = new String[]{
		EvalConstants.ITEM_SCALE_DISPLAY_COMPACT,
		EvalConstants.ITEM_SCALE_DISPLAY_COMPACT_COLORED,
		EvalConstants.ITEM_SCALE_DISPLAY_FULL,
		EvalConstants.ITEM_SCALE_DISPLAY_FULL_COLORED,
		EvalConstants.ITEM_SCALE_DISPLAY_STEPPED,
		EvalConstants.ITEM_SCALE_DISPLAY_STEPPED_COLORED,
		EvalConstants.ITEM_SCALE_DISPLAY_VERTICAL
	};

	// should match the order of the array above, should include the properties strings only (no real labels)
	public static String[] SCALE_DISPLAY_SETTING_LABELS_PROPS = new String[] {
			"templateitem.scale.select.compact",
			"templateitem.scale.select.compactc",
			"templateitem.scale.select.full",
			"templateitem.scale.select.fullc",
			"templateitem.scale.select.stepped",
			"templateitem.scale.select.steppedc",
			"templateitem.scale.select.vertical"
		};

	public static String[] ITEM_CATEGORY_VALUES = new String[] {
			EvalConstants.ITEM_CATEGORY_COURSE,
			EvalConstants.ITEM_CATEGORY_INSTRUCTOR
		};

	// should match ITEM_CATEGORY_VALUES
	public static String[] ITEM_CATEGORY_LABELS_PROPS = {
			"modifyitem.course.category",
			"modifyitem.instructor.category"
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
			"2", 
			"3", 
			"4", 
			"5"
		};

	public static final String[] RESPONSE_SIZE_LABELS_PROPS = new String[] {
			"modifyessay.response.select.size.2",
			"modifyessay.response.select.size.3",
			"modifyessay.response.select.size.4",
			"modifyessay.response.select.size.5"
		};

	/**
	 * The default number of rows to use when displaying a textarea type input box
	 */
	public static final Integer DEFAULT_ROWS = new Integer(2);

	// For main administrative page
	/**
	 * Defines the allowed values for the Integer constants in pulldowns
	 */
	public static final String[] PULLDOWN_INTEGER_VALUES = new String[] {
		"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "12", "15", "18", "20", "25", "50"};	

	/**
	 * Defines the allowed values for minimum time difference (in hours) 
	 * between start and due date of an evaluation.
	 */
	public static final String[] MINIMUM_TIME_DIFFERENCE = new String[] {
		"0", "1", "2", "4", "8", "12", "16", "20", "24", "36", "48", "96"};	

	/**
	 * Defines the allowed values for the auth control pulldown in the evaluation settings
	 */
	public static final String[] AUTHCONTROL_VALUES = new String[] {
		EvalConstants.EVALUATION_AUTHCONTROL_AUTH_REQ,
		EvalConstants.EVALUATION_AUTHCONTROL_KEY,
		EvalConstants.EVALUATION_AUTHCONTROL_NONE
	};

	/**
	 * Administrative (system settings) page, 
	 * values corresponding to "Yes", "No", "Configurable"  
	 */
	public static final String ADMIN_BOOLEAN_YES = "1";
	public static final String ADMIN_BOOLEAN_NO = "0";
	public static final String ADMIN_BOOLEAN_CONFIGURABLE = "-1";

	/**
	 * The value stored when a student responds N/A
	 */
	public static final String NA_VALUE = "-1";

	/**
	 * Ideal scale values radio buttons (scale add/modify)
	 */
	public static final String[] scaleIdealValues = { 
			EvalConstants.SCALE_IDEAL_NONE, 
			EvalConstants.SCALE_IDEAL_LOW, 
			EvalConstants.SCALE_IDEAL_HIGH,
			EvalConstants.SCALE_IDEAL_MID,
            EvalConstants.SCALE_IDEAL_OUTSIDE
		};

	/**
	 * Ideal scale values radio button labels (scale add/modify)
	 */
	public static final String[] scaleIdealLabels = {
			"scalecontrol.ideal.scale.option.label.none", 
			"scalecontrol.ideal.scale.option.label.low",
			"scalecontrol.ideal.scale.option.label.high", 
			"scalecontrol.ideal.scale.option.label.mid",
            "scalecontrol.ideal.scale.option.label.outside" 
		};

	public static final String[] ITEM_CLASSIFICATION_VALUES = new String[] {
			EvalConstants.ITEM_TYPE_SCALED,
			EvalConstants.ITEM_TYPE_TEXT,
			EvalConstants.ITEM_TYPE_HEADER
		};
	
	public static final String[] ITEM_CLASSIFICATION_LABELS_PROPS = new String[] {
			"item.classification.scaled", 
			"item.classification.text",
			"item.classification.header"
		};
	/**
	 * values for item results sharing.
	 */
	public static String[] ITEM_RESULTS_SHARING_VALUES = new String[] {
		EvalConstants.ITEM_RESULTS_SHARING_PUBLIC,
		EvalConstants.ITEM_RESULTS_SHARING_PRIVATE
	};

	// should match ITEM_RESULTS_SHARING_VALUES
	public static String[] ITEM_RESULTS_SHARING_LABELS_PROPS = {
		"item.results.sharing.public",
		"item.results.sharing.private"
	};

}

