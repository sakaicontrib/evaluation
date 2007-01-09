/*
 * Created on 03-Jan-2007
 */
package org.sakaiproject.evaluation.logic.impl.interceptors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.evaluation.model.constant.EvalConstants;

public class EvaluationModificationRegistry {
  // map of evaluation states to Set of permitted changes, or no entry if all
  // modifications are permitted
  private static Map permittedChanges = new HashMap();
  private static void addItem(String key, String permittedlist) {
    String[] permitteds = permittedlist.split(",");
    if (permitteds.length == 1 && permitteds[0].equals("*")) return;
    Set permset = new HashSet();
    for (int i = 0; i < permitteds.length; ++ i) {
      permset.add(permitteds[i]);
    }
    permittedChanges.put(key, permset);
  }
  static {
    addItem(EvalConstants.EVALUATION_STATE_INQUEUE, "*");
    addItem(EvalConstants.EVALUATION_STATE_ACTIVE, 
        "dueDate,stopDate,viewDate,reminderDays,resultsPrivate,instructorsDate,studentsDate");
    addItem(EvalConstants.EVALUATION_STATE_DUE,
        "stopDate,viewDate,resultsPrivate,instructorsDate,studentsDate");
    addItem(EvalConstants.EVALUATION_STATE_CLOSED, 
        "viewDate,resultsPrivate,instructorsDate,studentsDate");
    addItem(EvalConstants.EVALUATION_STATE_VIEWABLE, "");
  }
  
  public static boolean isPermittedModification(String state, String property) {
    Set permitteds = (Set) permittedChanges.get(state);
    if (permitteds == null) return true;
    else return permitteds.contains(property); 
  }
}
