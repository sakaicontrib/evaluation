/******************************************************************************
 * ScaleBeanLocator.java - created by kahuja@vt.edu on Mar 04, 2007
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.locators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * This is the OTP bean used to locate scales.
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ScaleBeanLocator implements BeanLocator {

	public static final String NEW_PREFIX = "new ";
	public static String NEW_1 = NEW_PREFIX + "1";

	private LocalTemplateLogic localTemplateLogic;
	public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
		this.localTemplateLogic = localTemplateLogic;
	}

	private Map delivered = new HashMap();

	public Object locateBean(String name) {
		Object togo = delivered.get(name);
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
		for (Iterator it = delivered.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			EvalScale scale = (EvalScale) delivered.get(key);
			if (key.startsWith(NEW_PREFIX)) {
				// could do stuff here
			}
			localTemplateLogic.saveScale(scale);
		}
	}

	public void deleteScale(Long scaleId) {
		localTemplateLogic.deleteScale(scaleId);
	}

}