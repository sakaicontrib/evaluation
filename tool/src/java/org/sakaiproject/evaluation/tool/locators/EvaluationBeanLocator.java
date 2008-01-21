/**
 * EvaluationBeanLocator.java - evaluation - Jan 20, 2007 11:35:56 AM - whumphri
 * $URL: https://source.sakaiproject.org/contrib $
 * $Id: Locator.java 11234 Oct 29, 2007 11:35:56 AM azeckoski $
 **************************************************************************
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
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

import org.sakaiproject.evaluation.logic.EvalEvaluationsLogic;
import org.sakaiproject.evaluation.logic.EvalExternalLogic;
import org.sakaiproject.evaluation.model.EvalEvaluation;

import uk.org.ponder.beanutil.BeanLocator;

/**
 * This is the OTP bean used to locate Evaluations.
 * 
 * @author Will Humphries (whumphri@vt.edu)
 */
public class EvaluationBeanLocator implements BeanLocator {

   public static final String NEW_PREFIX = "new";
   public static String NEW_1 = NEW_PREFIX + "1";

   private EvalEvaluationsLogic evalsLogic;
   public void setEvalsLogic(EvalEvaluationsLogic evalsLogic) {
      this.evalsLogic = evalsLogic;
   }

   private EvalExternalLogic external;
   public void setExternal(EvalExternalLogic external) {
      this.external = external;
   }

   private Map<String, EvalEvaluation> delivered = new HashMap<String, EvalEvaluation>();

   public Object locateBean(String name) {
      EvalEvaluation togo = delivered.get(name);
      if (togo == null) {
         if (name.startsWith(NEW_PREFIX)) {
            togo = new EvalEvaluation();
         } else {
            togo = evalsLogic.getEvaluationById(new Long(name));
         }
         delivered.put(name, togo);
      }
      return togo;
   }

   public void saveAll() {
      for (Iterator<String> it = delivered.keySet().iterator(); it.hasNext();) {
         String key = it.next();
         EvalEvaluation Evaluation = delivered.get(key);
         if (key.startsWith(NEW_PREFIX)) {
            // could do stuff here
         }
         evalsLogic.saveEvaluation(Evaluation, external.getCurrentUserId());
      }
   }
}