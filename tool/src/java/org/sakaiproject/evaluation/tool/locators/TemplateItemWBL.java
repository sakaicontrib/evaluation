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
import org.sakaiproject.evaluation.model.constant.EvalConstants;
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
	private Map<String, EvalTemplateItem> delivered = new HashMap<String, EvalTemplateItem>();

	/* (non-Javadoc)
	 * @see uk.org.ponder.beanutil.BeanLocator#locateBean(java.lang.String)
	 */
	public Object locateBean(String name) {
	   EvalTemplateItem togo = delivered.get(name);
		if (togo == null) {
			if (name.startsWith(NEW_PREFIX)) {
				togo = localTemplateLogic.newTemplateItem();
			}
			else {
				togo = localTemplateLogic.fetchTemplateItem(new Long(name));
			}
			delivered.put(name, togo);
		}
		return togo;
	}

	 public Map<String, EvalTemplateItem> getDeliveredBeans() {
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
		for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
			String key = it.next();
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
			/* This is a temporary hack that is only good while we are only using TOP LEVEL and NODE LEVEL.
			 * Basically, we're putting everything in one combo box and this is a good way to check to see if
			 * it's the top node.  Otherwise the user selected a node id so it must be at the NODE LEVEL since
			 * we don't support the other levels yet.
			 */
			if (templateItem.getHierarchyNodeId() != null && !templateItem.getHierarchyNodeId().equals("")
		                && !templateItem.getHierarchyNodeId().equals(EvalConstants.HIERARCHY_NODE_ID_NONE)) {
		            templateItem.setHierarchyLevel(EvalConstants.HIERARCHY_LEVEL_NODE);
		        }
		        else if (templateItem.getHierarchyNodeId() != null && !templateItem.getHierarchyNodeId().equals("")
		                && templateItem.getHierarchyNodeId().equals(EvalConstants.HIERARCHY_NODE_ID_NONE)) {
		            templateItem.setHierarchyLevel(EvalConstants.HIERARCHY_LEVEL_TOP);
		        }
			localTemplateLogic.saveTemplateItem(templateItem);
		}
	}

}