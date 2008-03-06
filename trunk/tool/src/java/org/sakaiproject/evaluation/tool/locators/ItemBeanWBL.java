/**
 * $Id$
 * $URL$
 * ItemBeanWBL.java - evaluation - July 25, 2007 4:08:52 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

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
	private Map<String, EvalItem> delivered = new HashMap<String, EvalItem>();

	/* (non-Javadoc)
	 * @see uk.org.ponder.beanutil.BeanLocator#locateBean(java.lang.String)
	 */
	public Object locateBean(String name) {
        EvalItem togo = delivered.get(name);
		if (togo == null) {
			if (name.startsWith(NEW_PREFIX)) {
				togo = localTemplateLogic.newItem();
			} else {
				togo = localTemplateLogic.fetchItem(new Long(name));
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
		for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			EvalItem item = (EvalItem) delivered.get(key);
			if (key.startsWith(NEW_PREFIX)) {
				// add in extra logic needed for new items here
			}
			localTemplateLogic.saveItem(item);
		}
	}

}
