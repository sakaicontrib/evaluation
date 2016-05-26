/**
 * Copyright 2005 Sakai Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
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
	private Map<String, EvalItem> delivered = new HashMap<>();

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
		for( String key : delivered.keySet() ) {
			EvalItem item = (EvalItem) delivered.get(key);
			if (key.startsWith(NEW_PREFIX)) {
				// add in extra logic needed for new items here
			}
			localTemplateLogic.saveItem(item);
		}
	}

}
