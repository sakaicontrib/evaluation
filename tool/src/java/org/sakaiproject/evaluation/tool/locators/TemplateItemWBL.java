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
import java.util.Iterator;
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

	/**
	 * saves all delivered template items in this request, 
	 * also saves the associated new items (does not save any associated existing items)
	 */
	public void saveAll() {
		for (Iterator it = delivered.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			EvalTemplateItem templateItem = (EvalTemplateItem) delivered.get(key);
			if (key.startsWith(NEW_PREFIX)) {
				// add in extra logic needed for new template items here
				if (templateItem.getItem().getId() == null) {
					// new item with our new template item so set the values in the new item
					templateItem.getItem().setScaleDisplaySetting(templateItem.getScaleDisplaySetting());
					templateItem.getItem().setUsesNA(templateItem.getUsesNA());
					templateItem.getItem().setSharing(templateItem.getTemplate().getSharing());
					templateItem.getItem().setCategory(templateItem.getItemCategory());
					// then save the item
					localTemplateLogic.saveItem( templateItem.getItem() );
				}
			}
			localTemplateLogic.saveTemplateItem(templateItem);
		}
	}

}