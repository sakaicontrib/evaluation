/******************************************************************************
 * EvaluationBean.java - created by kahuja@vt.edu on Jan 18, 2007
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

import org.sakaiproject.evaluation.model.EvalItem;

public class ItemBlockUtils {
	
//	to filter out the Block child items, and only return non-child items
	public static List getNonChildItems(List itemsList){
		
		List nonChildItemsList = new ArrayList();
		for(int i= 0; i< itemsList.size(); i++){
			EvalItem item1 = (EvalItem)itemsList.get(i);		
			if(item1.getBlockId()== null)
				nonChildItemsList.add(item1);
		}
		
		return nonChildItemsList;
	}

//	return the child items which assocaited with the BlockParentId
	public static List getChildItmes(List itemsList, Integer blockParentId){
		List childItemsList = new ArrayList();
		
		for(int i= 0; i< itemsList.size(); i++){
			EvalItem item1 = (EvalItem)itemsList.get(i);		
			if(item1.getBlockId()!= null && item1.getBlockId().equals(blockParentId))
				childItemsList.add(item1);
		}
		
		return childItemsList;
	}
}
