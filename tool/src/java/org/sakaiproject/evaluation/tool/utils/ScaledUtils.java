/*
 * Created on 19 Feb 2007
 */
package org.sakaiproject.evaluation.tool.utils;

import java.awt.Color;

import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.constant.EvalConstants;
import org.sakaiproject.evaluation.tool.EvaluationConstant;

import uk.org.ponder.arrayutil.ArrayUtil;

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
		EvaluationConstant.RED_COLOR,
		EvaluationConstant.RED_COLOR,
		EvaluationConstant.GREEN_COLOR,
		EvaluationConstant.GREEN_COLOR};
	
	public static String[] endColours = {
		EvaluationConstant.BLUE_COLOR, 
		EvaluationConstant.GREEN_COLOR,
		EvaluationConstant.RED_COLOR,
		EvaluationConstant.RED_COLOR,
		EvaluationConstant.GREEN_COLOR};
	
	public static int idealToIndex(String ideal) {
		return ideal == null? 0 : ArrayUtil.indexOf(idealKeys, ideal);
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
	
}
