/**
 * ResponseAnswersBeanLocator.java - evaluation - Feb 08, 2007 11:35:56 AM - antranig
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
import java.util.Map;

import org.sakaiproject.evaluation.model.EvalResponse;
import org.sakaiproject.evaluation.tool.LocalResponsesLogic;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * Special bean locator which is used to locate other bean locators, sneaky huh?
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class ResponseAnswersBeanLocator implements BeanLocator {
   public static final String NEW_PREFIX = "new";
   public static String NEW_1 = NEW_PREFIX +"1";

   private LocalResponsesLogic localResponsesLogic;
   public void setLocalResponsesLogic(LocalResponsesLogic localResponsesLogic) {
      this.localResponsesLogic = localResponsesLogic;
   }

   private ResponseBeanLocator responseBeanLocator;
   public void setResponseBeanLocator(ResponseBeanLocator responseBeanLocator) {
      this.responseBeanLocator = responseBeanLocator;
   }

   private Map<String, BeanLocator> delivered = new HashMap<String, BeanLocator>();

   public Object locateBean(String path) {
      BeanLocator togo = delivered.get(path);
      if (togo == null) {
         EvalResponse parent = (EvalResponse) responseBeanLocator.locateBean(path);
         togo = new AnswersBeanLocator(parent, localResponsesLogic);
         delivered.put(path, togo);
      }
      return togo;
   }
}
