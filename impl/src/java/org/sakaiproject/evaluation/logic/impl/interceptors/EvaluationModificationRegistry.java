/******************************************************************************
 * EvaluationModificationRegistry.java - created on Jan 03, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Antranig Basman
 * Aaron Zeckoski (aaronz@vt.edu)
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.impl.interceptors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.constant.EvalConstants;

/**
 * Registry of the properties on the evaluation object that can be modified and
 * the states that allow those modifications
 * 
 * @author Antranig Basman
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvaluationModificationRegistry {

   /**
    * map of evaluation states to Set of permitted changes, or no entry if all
    * modifications are permitted
    */
   private static Map<String, Set<String>> permittedChanges = new HashMap<String, Set<String>>();

   private static void addItem(String key, String permittedlist) {
      String[] permitteds = permittedlist.split(",");
      if (permitteds.length == 1 && permitteds[0].equals("*")) return;
      Set<String> permset = new HashSet<String>();
      for (int i = 0; i < permitteds.length; ++ i) {
         permset.add(permitteds[i]);
      }
      permittedChanges.put(key, permset);
   }

   static {
      addItem(EvalConstants.EVALUATION_STATE_INQUEUE, "*");
      addItem(EvalConstants.EVALUATION_STATE_ACTIVE, 
      "dueDate,stopDate,viewDate,reminderDays,resultsPrivate,instructorsDate,studentsDate");
      addItem(EvalConstants.EVALUATION_STATE_GRACEPERIOD,
      "stopDate,viewDate,resultsPrivate,instructorsDate,studentsDate");
      addItem(EvalConstants.EVALUATION_STATE_CLOSED, 
      "viewDate,resultsPrivate,instructorsDate,studentsDate");
      addItem(EvalConstants.EVALUATION_STATE_VIEWABLE, "");
   }

   /**
    * Check if the property can be modified while the evaluation is in the following
    * state for an evaluation persistent object
    * 
    * @param state an evaluation state constant from EvalConstants
    * @param property the name of an evaluation property
    * @return true if can be modified now, false otherwise
    */
   public static boolean isPermittedModification(String state, String property) {
      Set<String> permitteds = permittedChanges.get(state);
      if (permitteds == null) return true;
      else return permitteds.contains(property); 
   }

}
