/******************************************************************************
 * ItemBeanWBL.java - created by aaronz on 21 May 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;

import uk.org.ponder.beanutil.WriteableBeanLocator;

/**
 * OTP bean used to locate {@link EvalItem}s
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ItemBeanWBL implements WriteableBeanLocator {

	public static final String NEW_PREFIX = "new";
	public static String NEW_1 = NEW_PREFIX + "1";

	private LocalTemplateLogic localTemplateLogic;
	public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
		this.localTemplateLogic = localTemplateLogic;
	}

	// keep track of all items that have been delivered during this request
	private Map delivered = new HashMap();

	/* (non-Javadoc)
	 * @see uk.org.ponder.beanutil.BeanLocator#locateBean(java.lang.String)
	 */
	public Object locateBean(String name) {
		Object togo = delivered.get(name);
		if (togo == null) {
			if (name.startsWith(NEW_PREFIX)) {
				togo = localTemplateLogic.newItem();
			} else {
				togo = localTemplateLogic.fetchItem(Long.valueOf(name));
			}
			delivered.put(name, togo);
		}
		return togo;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.beanutil.WriteableBeanLocator#remove(java.lang.String)
	 */
	public boolean remove(String beanname) {
		Long itemId = Long.valueOf(beanname);
		localTemplateLogic.deleteItem(itemId);
		delivered.remove(beanname);
		return true;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.beanutil.WriteableBeanLocator#set(java.lang.String, java.lang.Object)
	 */
	public void set(String beanname, Object toset) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void saveAll() {
		for (Iterator it = delivered.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			EvalItem item = (EvalItem) delivered.get(key);
			if (key.startsWith(NEW_PREFIX)) {
				// add in extra logic needed for new items here
			}
			localTemplateLogic.saveItem(item);
		}
	}

}
