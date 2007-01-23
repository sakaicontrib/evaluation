/*
 * Created on 23 Jan 2007
 */
package org.sakaiproject.evaluation.tool;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvaluationSettingsParse;

import uk.org.ponder.beanutil.WriteableBeanLocator;
import uk.org.ponder.conversion.StaticLeafParser;
import uk.org.ponder.util.UniversalRuntimeException;

public class SettingsWBL implements WriteableBeanLocator {
  private StaticLeafParser leafParser;
  private EvalSettings evalSettings;
  
  public void setEvalSettings(EvalSettings evalSettings) {
    this.evalSettings = evalSettings;
  }

  public void setLeafParser(StaticLeafParser leafParser) {
    this.leafParser = leafParser;
  }

  public boolean remove(String beanname) {
    throw new UnsupportedOperationException("Removal not supported from SettingsWBL");
  }
  
  private static Class getPropertyType(String propname) {
    String typename = EvaluationSettingsParse.getType(propname);
    try {
      return Class.forName(typename);
    }
    catch (Exception e) {
      throw UniversalRuntimeException.accumulate(e, "Could not look up " + typename + " to a class");
    }
  }

  public void set(String beanname, Object toset) {
    // The UI has already converted Booleans - this is primarily to catch Integers
    if (toset instanceof String) {
      Class proptype = getPropertyType(beanname);
      toset = leafParser.parse(proptype, (String) toset);
    }
    evalSettings.set(beanname, toset);
  }

  public Object locateBean(String path) {
    return evalSettings.get(path);
  }

}
