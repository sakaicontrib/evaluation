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

import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;

import uk.org.ponder.beanutil.WriteableBeanLocator;

/**
 * This is the OTP bean used to locate scales.
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ScaleBeanLocator implements WriteableBeanLocator {
   public static final String NEW_PREFIX = "new";
   public static String NEW_1 = NEW_PREFIX +"1";

	private LocalTemplateLogic localTemplateLogic;
	public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
		this.localTemplateLogic = localTemplateLogic;
	}

	private Map<String, EvalScale> delivered = new HashMap<>();

	public Object locateBean(String name) {
	   EvalScale togo = delivered.get(name);
		if (togo == null) {
			if (name.startsWith(NEW_PREFIX)) {
				togo = localTemplateLogic.newScale();
			} else {
				togo = localTemplateLogic.fetchScale(new Long(name));
			}
			delivered.put(name, togo);
		}
		return togo;
	}

	public void saveAll() {
		for( String key : delivered.keySet() ) {
			EvalScale scale = delivered.get(key);
			if (key.startsWith(NEW_PREFIX)) {
				// could do stuff here
			}
			localTemplateLogic.saveScale(scale);
		}
	}

	public void deleteScale(Long scaleId) {
		localTemplateLogic.deleteScale(scaleId);
	}

   public boolean remove(String beanname) {
      Long scaleId = Long.valueOf(beanname);
      deleteScale(scaleId);
      delivered.remove(beanname);
      return true;
   }

   public void set(String beanname, Object toset) {
      throw new UnsupportedOperationException();
   }

}