/******************************************************************************
 * ItemBlockUtils.java - created on Jan 18, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 * Aaron Zeckoski (aaronz@vt.edu) - project lead
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.utils;

import java.util.ArrayList;
import java.util.List;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

// TODO - commenting and format corrected, still need to add error checking and ordering (items should be returned in sorted order)

/**
 * Includes static methods to handle BLOCK Child items
 *  
 * @author Rui Feng (fengr@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ItemBlockUtils {

	/**
	 * filter out the Block child items, and only return non-child items
	 * 
	 * @param tempItemsList a List of {@link EvalTemplateItem} objects in a template
	 * @return a List of {@link EvalTemplateItem} objects without any block child objects
	 */
	public static List getNonChildItems(List tempItemsList){		
		List nonChildItemsList = new ArrayList();

		for(int i= 0; i< tempItemsList.size(); i++){
			EvalTemplateItem tempItem1 = (EvalTemplateItem)tempItemsList.get(i);		
			if(tempItem1.getBlockId()== null)
				nonChildItemsList.add(tempItem1);
		}
		
		return nonChildItemsList;
	}

	/**
	 * return the child items which are associated with a block parent Id
	 * 
	 * @param tempItemsList a List of {@link EvalTemplateItem} objects in a template
	 * @param blockParentId a unique identifier for an {@link EvalTemplateItem} which is a block parent
	 * @return a List of {@link EvalTemplateItem} objects
	 */
	public static List getChildItems(List tempItemsList, Long blockParentId){
		List childItemsList = new ArrayList();
		
		for(int i= 0; i< tempItemsList.size(); i++){
			EvalTemplateItem tempItem1 = (EvalTemplateItem)tempItemsList.get(i);		
			if(tempItem1.getBlockId()!= null && tempItem1.getBlockId().equals(blockParentId))
				childItemsList.add(tempItem1);
		}
		
		return childItemsList;
	}

}
