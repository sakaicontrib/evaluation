/******************************************************************************
 * TemplateItemBeanLocator.java - created by whumphri@vt.edu on Jan 23, 2007
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
package org.sakaiproject.evaluation.tool;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.logic.EvalScalesLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalScale;
import org.sakaiproject.evaluation.model.EvalTemplateItem;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * This is the OTP bean used to locate templates items
 * 
 * @author
 */

public class TemplateItemBeanLocator implements BeanLocator {
  public static final String NEW_PREFIX = "new";
  public static String NEW_1 = NEW_PREFIX + "1";

  private EvalItemsLogic itemsLogic;

  public void setItemsLogic(EvalItemsLogic itemsLogic) {
    this.itemsLogic = itemsLogic;
  }

  private EvalScalesLogic scalesLogic;
  public void setScalesLogic(EvalScalesLogic scalesLogic) {
		this.scalesLogic = scalesLogic;
	}
  
  private EvalExternalLogic external;

  public void setExternal(EvalExternalLogic external) {
    this.external = external;
  }

  private Map delivered = new HashMap();

  public Object locateBean(String path) {
    Object togo = delivered.get(path);
    if (togo == null) {
      if (path.startsWith(NEW_PREFIX)) {
        EvalItem newItem=new EvalItem(new Date(), external.getCurrentUserId(), "", "",
                "", new Boolean(false));
    	  togo = new EvalTemplateItem(new Date(), external.getCurrentUserId(),
            null, newItem, null, "");
        
      }
      else {
        togo = itemsLogic.getTemplateItemById(new Long(Long.parseLong(path
            .trim())));
      }
      delivered.put(path, togo);
    }
    return togo;
  }



}