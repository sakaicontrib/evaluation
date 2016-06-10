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
   private static Map<String, Set<String>> permittedChanges = new HashMap<>();

   private static void addItem(String key, String permittedlist) {
      String[] permitteds = permittedlist.split(",");
      if (permitteds.length == 1 && permitteds[0].equals("*")) return;
      Set<String> permset = new HashSet<>();
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
