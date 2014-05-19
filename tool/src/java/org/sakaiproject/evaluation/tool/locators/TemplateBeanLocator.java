/**
 * TemplateBeanLocator.java - evaluation - Jan 20, 2007 11:35:56 AM - whumphri@vt.edu
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

import org.sakaiproject.evaluation.model.EvalTemplate;
import org.sakaiproject.evaluation.tool.LocalTemplateLogic;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * This is the OTP bean used to locate evaluation templates.
 * 
 * @author whumphri@vt.edu
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TemplateBeanLocator implements BeanLocator {

   public static final String NEW_PREFIX = "new";
   public static String NEW_1 = NEW_PREFIX + "1";

   private LocalTemplateLogic localTemplateLogic;
   public void setLocalTemplateLogic(LocalTemplateLogic localTemplateLogic) {
      this.localTemplateLogic = localTemplateLogic;
   }

   private Map<String, EvalTemplate> delivered = new HashMap<String, EvalTemplate>();

   public Object locateBean(String name) {
      EvalTemplate togo = delivered.get(name);
      if (togo == null) {
         if (name.startsWith(NEW_PREFIX)) {
            togo = localTemplateLogic.newTemplate();
         } else {
            togo = localTemplateLogic.fetchTemplate(new Long(name));
         }
         delivered.put(name, togo);
      }
      return togo;
   }

   public void saveAll() {
      for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
         String key = it.next();
         EvalTemplate template = delivered.get(key);
         if (key.startsWith(NEW_PREFIX)) {
            // could do stuff here
         }
         localTemplateLogic.saveTemplate(template);
      }
   }
}