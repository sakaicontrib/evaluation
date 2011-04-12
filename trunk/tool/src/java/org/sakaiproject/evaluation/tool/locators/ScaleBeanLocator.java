/**
 * ScaleBeanLocator.java - evaluation - Mar 04, 2007 11:35:56 AM - kahuja@vt.edu
 * $URL$
 * $Id$
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

	private Map<String, EvalScale> delivered = new HashMap<String, EvalScale>();

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
		for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
			String key = it.next();
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