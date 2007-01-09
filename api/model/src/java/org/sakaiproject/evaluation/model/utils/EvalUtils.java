/******************************************************************************
 * EvalUtils.java - created by aaronz@vt.edu on Dec 28, 2006
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

package org.sakaiproject.evaluation.model.utils;

import java.util.Date;

import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;


/**
 * This class contains various model related utils needed in many areas
 * <b>WARNING:</b> do NOT use these methods outside the logic or DAO layers
 *
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalUtils {

	/**
	 * Get the state of an evaluation object<br/>
	 * <b>WARNING:</b> do NOT use this outside the logic or DAO layers
	 * 
	 * @param eval the evaluation object
	 * @return the EVALUATION_STATE constant
	 */
	public static String getEvaluationState(EvalEvaluation eval) {
		Date today = new Date();
		try {
			if ( eval.getStartDate().after(today) ) {
				return EvalConstants.EVALUATION_STATE_INQUEUE;
			} else if ( eval.getDueDate().after(today) ) {
				return EvalConstants.EVALUATION_STATE_ACTIVE;
			} else if ( eval.getStopDate().after(today) ) {
				return EvalConstants.EVALUATION_STATE_DUE;
			} else if ( eval.getViewDate().after(today) ) {
				return EvalConstants.EVALUATION_STATE_CLOSED;
			} else {
				return EvalConstants.EVALUATION_STATE_VIEWABLE;
			}
		} catch (NullPointerException e) {
			return EvalConstants.EVALUATION_STATE_UNKNOWN;
		}
	}

	/**
	 * Checks if a sharing constant is valid
	 * @param sharingConstant
	 * @return true if this is a valid sharing constant, false otherwise
	 */
	public static boolean checkSharingConstant(String sharingConstant) {
		if ( EvalConstants.SHARING_OWNER.equals(sharingConstant) ||
				EvalConstants.SHARING_PRIVATE.equals(sharingConstant) ||
				EvalConstants.SHARING_PUBLIC.equals(sharingConstant) ||
				EvalConstants.SHARING_SHARED.equals(sharingConstant) ||
				EvalConstants.SHARING_VISIBLE.equals(sharingConstant)) {
			return true;
		}
		return false;
	}

}
