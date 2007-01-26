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

import java.util.HashMap;
import java.util.Map;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * This is the OTP bean used to locate templates items
 * 
 * @author
 */

public class TemplateItemBeanLocator implements BeanLocator {
  public static final String NEW_PREFIX = "new";
  public static String NEW_1 = NEW_PREFIX + "1";

  private LocalTemplateLogic localTemplateLogic;
  
  public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
    this.localTemplateLogic = localTemplateLogic;
  }

  private Map delivered = new HashMap();

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

  /** Package-protected access to "dead" list of delivered beans */
  Map getDeliveredBeans() {
    return delivered;
  }
  
}