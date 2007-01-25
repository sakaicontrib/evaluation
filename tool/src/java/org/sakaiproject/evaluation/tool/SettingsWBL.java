/******************************************************************************
 * SettingsWBL.java - created by antranig@caret.cam.ac.uk
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Antranig Basman (antranig@caret.cam.ac.uk)
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.EvaluationSettingsParse;

import uk.org.ponder.beanutil.WriteableBeanLocator;
import uk.org.ponder.conversion.StaticLeafParser;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * Obviates the need for a backing bean for administrative functionality.
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 */
public class SettingsWBL implements WriteableBeanLocator {
  private StaticLeafParser leafParser;
  private EvalSettings evalSettings;
  private MessageLocator messageLocator;
  
  public void setEvalSettings(EvalSettings evalSettings) {
    this.evalSettings = evalSettings;
  }

  public void setLeafParser(StaticLeafParser leafParser) {
    this.leafParser = leafParser;
  }

  public void setMessageLocator(MessageLocator messageLocator) {
	this.messageLocator = messageLocator;
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

  /*
   * Sets the data from producer to EvalSettings. 
   */
  public void set(String beanname, Object toset) {
	  
    /*
     *  The UI has already converted Booleans.
     *  this is primarily to catch Integers and strings. 
     */
    if (toset instanceof String) {
    	Class proptype = getPropertyType(beanname);

	  /*
	   * Fields inside the if block are not directly mapped to those in database.
	   * Thus parsing them.  
	   */
      if (  beanname.equals(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS) ||
			beanname.equals(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE) ||
			beanname.equals(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED) ||
			beanname.equals(EvalSettings.STUDENT_MODIFY_RESPONSES) ||
			beanname.equals(EvalSettings.STUDENT_VIEW_RESULTS) ||
			beanname.equals(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY) ) {
    	  
        if ( ((String)toset).equals(messageLocator.getMessage("administrate.configurable.label")) ) 
          toset = null;
        else if ( ((String)toset).equals(messageLocator.getMessage("administrate.true.label")) ) 
          toset = "true";
        else if ( ((String)toset).equals(messageLocator.getMessage("administrate.false.label")) ) 
          toset = "false";
        else {
          //do nothing
        }
	  }

      /*
       * If the value of field is null it means that this particular value should be
       * deleted from the table. This parsing using leaf parser is not needed. 
       */ 
	  if (toset != null)
        toset = leafParser.parse(proptype, (String) toset);
	  
    }
    
    evalSettings.set(beanname, toset);
  }

  /*
   * Gets the data from EvalSettings and returns to producer. 
   */
  public Object locateBean(String path) {
	  
    Object toget = evalSettings.get(path);

    /*
     * Fields inside the if block are not directly mapped to those in database.
     * Thus parsing them.  
     */
    if ( path.equals(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS) ||
    	 path.equals(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE) ||
    	 path.equals(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED) ||
    	 path.equals(EvalSettings.STUDENT_MODIFY_RESPONSES) ||
    	 path.equals(EvalSettings.STUDENT_VIEW_RESULTS) ||
    	 path.equals(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY)) {

      if (toget == null)
        toget = messageLocator.getMessage("administrate.configurable.label");
      else if ( (toget instanceof Boolean) && ((Boolean)toget).booleanValue() )
        toget = messageLocator.getMessage("administrate.true.label");
      else if ( (toget instanceof Boolean) && !(((Boolean)toget).booleanValue()) )
        toget = messageLocator.getMessage("administrate.false.label");
      else {
      	//do nothing
      }
    }
    return toget;
  }
  
}
