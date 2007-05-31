/*
 * Created on 19 Feb 2007
 */
package org.sakaiproject.evaluation.tool.utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;

import uk.org.ponder.stringutil.StringUtil;

/**
 * Utilities for manipulating scales.
 * 
 * @author Antranig Basman (amb26@ponder.org.uk)
 */

public class ScaledUtils {

	public static String[] idealKeys = {
		EvalConstants.SCALE_IDEAL_NONE,
		EvalConstants.SCALE_IDEAL_LOW, 
		EvalConstants.SCALE_IDEAL_MID, 
		EvalConstants.SCALE_IDEAL_HIGH, 
		EvalConstants.SCALE_IDEAL_OUTSIDE};

	public static String[] startColours = {
		EvaluationConstant.BLUE_COLOR, 
		EvaluationConstant.GREEN_COLOR,
		EvaluationConstant.RED_COLOR,
		EvaluationConstant.RED_COLOR,
		EvaluationConstant.GREEN_COLOR};

	public static String[] endColours = {
		EvaluationConstant.BLUE_COLOR, 
		EvaluationConstant.RED_COLOR,
		EvaluationConstant.RED_COLOR,
		EvaluationConstant.GREEN_COLOR,
		EvaluationConstant.GREEN_COLOR};

	public static int idealToIndex(String ideal) {
		for (int i = 0; i < idealKeys.length; ++ i) {
			if (StringUtil.equals(ideal, idealKeys[i])) return i;
		}
		return -1;
	}

	public static int idealIndex(EvalScale scale) {
		return idealToIndex(scale.getIdeal());
	}

	public static String getIdealImageURL(EvalScale scale) {
		return EvaluationConstant.COLORED_IMAGE_URLS[idealIndex(scale)];
	}

	public static Color getStartColor(EvalScale scale) {
		return Color.decode(startColours[idealIndex(scale)]);
	}

	public static Color getEndColor(EvalScale scale) {
		return Color.decode(endColours[idealIndex(scale)]);
	}

	/**
	 * Produce scale labels for a list of scales that can be used in a pulldown menu
	 * 
	 * @param scales a list of {@link EvalScale}
	 * @return an array of labels for the passed in scales
	 */
	public static String[] getScaleLabels(List scales) {
		List scaleLabels = new ArrayList();
		for (int i = 0; i < scales.size(); i++) {
			EvalScale scale = (EvalScale) scales.get(i);
			String scaleOptionsStr = "";
			String[] scaleOptionsArr = scale.getOptions();
			for (int j = 0; j < scaleOptionsArr.length; j++) {
				if (scaleOptionsStr.equals("")) {
					scaleOptionsStr = scaleOptionsArr[j];
				} else {
					scaleOptionsStr = scaleOptionsStr + ", " + scaleOptionsArr[j];
				}
			}
			scaleLabels.add(scaleOptionsArr.length + " pt - " + scale.getTitle() + " (" + scaleOptionsStr + ")");
		}
		return (String[]) scaleLabels.toArray(new String[] {});
	}

	/**
	 * Produce values for a list of scales that can be used in a pulldown menu
	 * 
	 * @param scales a list of {@link EvalScale}
	 * @return an array of values for the passed in scales
	 */
	public static String[] getScaleValues(List scales) {
		List scaleValues = new ArrayList();
		for (int i = 0; i < scales.size(); i++) {
			EvalScale scale = (EvalScale) scales.get(i);
			scaleValues.add(scale.getId().toString());
		}
		return (String[]) scaleValues.toArray(new String[] {});
	}

}
