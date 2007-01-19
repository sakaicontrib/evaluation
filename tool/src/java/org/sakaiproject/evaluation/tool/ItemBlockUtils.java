/******************************************************************************
 * ItemBlockUtils.java - created by fengr@vt.edu on Jan 18, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Rui Feng (fengr@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.evaluation.model.EvalTemplateItem;
/**
 *  includes static methods to handle BLOCK Child ietms

 * @author Rui Feng (fengr@vt.edu)
 */
public class ItemBlockUtils {
	
//	to filter out the Block child items, and only return non-child items
	public static List getNonChildItems(List tempItemsList){
		
		List nonChildItemsList = new ArrayList();
		for(int i= 0; i< tempItemsList.size(); i++){
			EvalTemplateItem tempItem1 = (EvalTemplateItem)tempItemsList.get(i);		
			if(tempItem1.getBlockId()== null)
				nonChildItemsList.add(tempItem1);
		}
		
		return nonChildItemsList;
	}

//	return the child items which assocaited with the BlockParentId
	public static List getChildItmes(List tempItemsList, Integer blockParentId){
		List childItemsList = new ArrayList();
		
		for(int i= 0; i< tempItemsList.size(); i++){
			EvalTemplateItem tempItem1 = (EvalTemplateItem)tempItemsList.get(i);		
			if(tempItem1.getBlockId()!= null && tempItem1.getBlockId().equals(blockParentId))
				childItemsList.add(tempItem1);
		}
		
		return childItemsList;
	}
}
