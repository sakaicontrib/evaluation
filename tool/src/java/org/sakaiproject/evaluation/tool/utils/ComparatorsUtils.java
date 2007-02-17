/******************************************************************************
 * TemplateItemUtils.java - created by fengr@vt.edu on Feb 11, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.utils;

import java.util.Comparator;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

/**
 * Utilities for sorting templateItem objects
 *
 * @author Rui Feng (fengr@vt.edu)
 */

public class ComparatorsUtils {

	/**
	 * static class to sort EvalTemplateItem objects by DisplayOrder
	 */
	public static class TemplateItemComparatorByOrder implements Comparator {
		public int compare(Object eval0, Object eval1) {
			// expects to get EvalTemplateItem objects, compare by displayOrder
			return ((EvalTemplateItem)eval0).getDisplayOrder().
				compareTo(((EvalTemplateItem)eval1).getDisplayOrder());
					
		}
	}
	
	
	/**
	 * static class to sort EvalTemplateItem objects by Id
	 */
	public static class TemplateItemComparatorById implements Comparator {
		public int compare(Object eval0, Object eval1) {
			// expects to get EvalTemplateItem objects, compare by Id
			return ((EvalTemplateItem)eval0).getId().compareTo(((EvalTemplateItem)eval1).getId());
			
		}
	}
	
}
