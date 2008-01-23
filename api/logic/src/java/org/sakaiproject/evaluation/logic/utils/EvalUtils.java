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

package org.sakaiproject.evaluation.logic.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.sakaiproject.evaluation.logic.model.EvalGroup;
import org.sakaiproject.evaluation.model.EvalAssignGroup;
import org.sakaiproject.evaluation.model.EvalEvaluation;
import org.sakaiproject.evaluation.model.constant.EvalConstants;


/**
 * This class contains various model related utils needed in many areas
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

	/**
	 * Remove all duplicate objects from a list
	 * 
	 * @param list
	 * @return the original list with the duplicate objects removed
	 */
	public static <T extends Object> List<T> removeDuplicates(List<T> list) {
		Set<T> s = new HashSet<T>();
		for (Iterator<T> iter = list.iterator(); iter.hasNext();) {
			T element = (T) iter.next();
			if (! s.add(element)) {
				iter.remove();
			}
		}
		return list;
	}

	/**
	 * Takes 2 lists of group types, {@link EvalGroup} and {@link EvalAssignGroup}, and
	 * merges the groups in common and then returns an array of the common groups,
	 * comparison is on the evalGroupId
	 * 
	 * @param evalGroups a list of {@link EvalGroup}
	 * @param assignGroups a list of {@link EvalAssignGroup}
	 * @return an array of the groups that in common between the 2 lists
	 */
	public static EvalGroup[] getGroupsInCommon(List<EvalGroup> evalGroups, List<EvalAssignGroup> assignGroups) {
		List<EvalGroup> groups = new ArrayList<EvalGroup>();
		for (int i=0; i<evalGroups.size(); i++) {
			EvalGroup group = (EvalGroup) evalGroups.get(i);
			for (int j=0; j<assignGroups.size(); j++) {
				EvalAssignGroup assignGroup = (EvalAssignGroup) assignGroups.get(j);
				if (group.evalGroupId.equals(assignGroup.getEvalGroupId())) {
					groups.add(group);
					break;
				}
			}
		}
		return (EvalGroup[]) groups.toArray(new EvalGroup[] {});
	}

	/**
	 * Creates a unique title for an adhoc scale
	 * @return 
	 */
	public static String makeUniqueIdentifier(int maxLength) {
      String newTitle = UUID.randomUUID().toString();
      if (newTitle.length() > maxLength) {
         newTitle = newTitle.substring(0, maxLength);
      }
      return newTitle;
	}

}
