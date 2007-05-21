/******************************************************************************
 * TemplateItemWBL.java - created by whumphri@vt.edu on Jan 23, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Will Humphries (whumphri@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;

import uk.org.ponder.beanutil.WriteableBeanLocator;

/**
 * This is the OTP bean used to locate {@link EvalTemplateItem}s
 * 
 * @author Will Humphries
 * @author Aaron Zeckoski (aaronz@vt.edu) - made this writeable
 */
public class TemplateItemWBL implements WriteableBeanLocator {

	public static final String NEW_PREFIX = "new";
	public static String NEW_1 = NEW_PREFIX + "1";

	private LocalTemplateLogic localTemplateLogic;
	public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
		this.localTemplateLogic = localTemplateLogic;
	}

	// keep track of all template items that have been delivered during this request
	private Map delivered = new HashMap();

	/* (non-Javadoc)
	 * @see uk.org.ponder.beanutil.BeanLocator#locateBean(java.lang.String)
	 */
	public Object locateBean(String path) {
		Object togo = delivered.get(path);
		if (togo == null) {
			if (path.startsWith(NEW_PREFIX)) {
				togo = localTemplateLogic.newTemplateItem();
			}
			else {
				togo = localTemplateLogic.fetchTemplateItem(Long.valueOf(path));
			}
			delivered.put(path, togo);
		}
		return togo;
	}

	 public Map getDeliveredBeans() {
		 return delivered;
	 }

	/* (non-Javadoc)
	 * @see uk.org.ponder.beanutil.WriteableBeanLocator#remove(java.lang.String)
	 */
	public boolean remove(String beanname) {
		Long templateItemId = Long.valueOf(beanname);
		localTemplateLogic.deleteTemplateItem(templateItemId);
		delivered.remove(beanname);
		return true;
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.beanutil.WriteableBeanLocator#set(java.lang.String, java.lang.Object)
	 */
	public void set(String beanname, Object toset) {
		throw new UnsupportedOperationException("Not implemented");
	}

}