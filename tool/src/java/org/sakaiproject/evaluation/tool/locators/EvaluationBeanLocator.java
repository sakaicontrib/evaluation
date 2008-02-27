/**
 * EvaluationBeanLocator.java - evaluation - Jan 20, 2007 11:35:56 AM - whumphri
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

import org.sakaiproject.evaluation.logic.EvalEvaluationService;
import org.sakaiproject.evaluation.logic.EvalEvaluationSetupService;
import org.sakaiproject.evaluation.logic.externals.EvalExternalLogic;
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

   private EvalExternalLogic external;
   public void setExternal(EvalExternalLogic external) {
      this.external = external;
   }

   private EvalEvaluationService evaluationService;
   public void setEvaluationService(EvalEvaluationService evaluationService) {
      this.evaluationService = evaluationService;
   }

   private EvalEvaluationSetupService evaluationSetupService;
   public void setEvaluationSetupService(EvalEvaluationSetupService evaluationSetupService) {
      this.evaluationSetupService = evaluationSetupService;
   }


   private Map<String, EvalEvaluation> delivered = new HashMap<String, EvalEvaluation>();

   public Object locateBean(String name) {
      EvalEvaluation togo = delivered.get(name);
      if (togo == null) {
         if (name.startsWith(NEW_PREFIX)) {
            togo = new EvalEvaluation();
         } else {
            togo = evaluationService.getEvaluationById(new Long(name));
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
         evaluationSetupService.saveEvaluation(Evaluation, external.getCurrentUserId());
      }
   }
}