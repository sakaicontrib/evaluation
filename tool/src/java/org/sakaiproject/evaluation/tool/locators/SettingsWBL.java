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

package org.sakaiproject.evaluation.tool.locators;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.utils.SettingsLogicUtils;
import org.sakaiproject.evaluation.tool.EvaluationConstant;

import uk.org.ponder.beanutil.WriteableBeanLocator;
import uk.org.ponder.conversion.StaticLeafParser;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * Obviates the need for a backing bean for administrative functionality 
 * (this is a WritableBeanLocator). 
 * 
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * @author Kapil Ahuja (kahuja@caret.cam.ac.uk)
 */
public class SettingsWBL implements WriteableBeanLocator {
	
	// Spring injection 
	private EvalSettings evalSettings;
	public void setEvalSettings(EvalSettings evalSettings) {
		this.evalSettings = evalSettings;
	}
	
	// Spring injection 
	private StaticLeafParser leafParser;
	public void setLeafParser(StaticLeafParser leafParser) {
		this.leafParser = leafParser;
	}
	
	/**
	 * Simply tells the user that remove functionality is not 
	 * supported (this is done by throwing an exception).
	 * 
	 * @param beanname -  Name of the property that has to be removed    
	 * @return throws a new UnsupportedOperationException exception
	 */
	public boolean remove(String beanname) {
		throw new UnsupportedOperationException("Removal not supported from SettingsWBL");
	}
	  
	/**
	 * Sets the data from producer to EvalSettings (database). 
	 * 
	 * @param beanname -  Name of the property to be set    
	 * @param toset -  Value of the property to be set    
	 */
	public void set(String beanname, Object toset) {
	  
		/*
		 * Fields inside isFieldToBeParsed are not directly mapped to those in database.
		 * Thus parsing them.  
		 *       
		 * Note: If the value of field is null it means that this particular value should be
		 * deleted from the table. 
		 */ 
		if ( isFieldToBeParsed(beanname) ) {
			if ( ((String)toset).equals(EvaluationConstant.ADMIN_BOOLEAN_CONFIGURABLE) ) 
				toset = null;
			else if ( ((String)toset).equals(EvaluationConstant.ADMIN_BOOLEAN_YES) ) 
				toset = Boolean.TRUE;
			else if ( ((String)toset).equals(EvaluationConstant.ADMIN_BOOLEAN_NO) ) 
				toset = Boolean.FALSE;
			else {
				//do nothing
			}
		}
		else {
			/*
			 *  The UI has already converted Booleans.
			 *  This is primarily to catch Integers and Strings. 
			 */
			if (toset instanceof String) {
				Class proptype = getPropertyType(beanname);
				toset = leafParser.parse(proptype, (String) toset);
			}
		}
		evalSettings.set(beanname, toset);
		//single email settings kept in sync
		if(beanname.equals(EvalSettings.CONSOLIDATED_REMINDER_INTERVAL)) {
			evalSettings.set(EvalSettings.DAYS_UNTIL_REMINDER, toset);
		}
	}

	/**
	 * Gets the data from EvalSettings (database) and returns to producer.
	 * 
	 * @param path -  Name of the property whose value has to be fetched
	 * 				  from database
	 * @return Value of the property obtained from database
	 */
	public Object locateBean(String path) {
		  
		Object toget = evalSettings.get(path);
		
		/*
		 * Fields inside isFieldToBeParsed are not directly mapped to those in database.
		 * Thus parsing them.  
		 */
		if ( isFieldToBeParsed(path) ) {
			if (toget == null)
				toget = EvaluationConstant.ADMIN_BOOLEAN_CONFIGURABLE;
			else if (toget instanceof Boolean) {
				if ( ((Boolean)toget).booleanValue() )
					toget = EvaluationConstant.ADMIN_BOOLEAN_YES;
				else 
					toget = EvaluationConstant.ADMIN_BOOLEAN_NO;
			}
			else {
				//do nothing
			}
		}
		return toget;
	}
	
	/*
	 * (non-javadoc)
	 * Uses java.lang.Class to find the class for a given property. 
	 */
	private static Class getPropertyType(String propname) {
		String typename = SettingsLogicUtils.getType(propname);
		try {
			return Class.forName(typename);
		}
		catch (Exception e) {
			throw UniversalRuntimeException.accumulate(e, "Could not look up " + typename + " to a class");
		}
	}
	
	/*
	 * (non-javadoc)
	 * Removing duplication of code in "get" and "set" methods 
	 * by moving "if" block in this method. 
	 */
	private boolean isFieldToBeParsed (String path) {

		if ( path.equals(EvalSettings.INSTRUCTOR_ALLOWED_VIEW_RESULTS) ||
				path.equals(EvalSettings.INSTRUCTOR_MUST_USE_EVALS_FROM_ABOVE) ||
				path.equals(EvalSettings.STUDENT_ALLOWED_LEAVE_UNANSWERED) ||
				path.equals(EvalSettings.STUDENT_MODIFY_RESPONSES) ||
				path.equals(EvalSettings.STUDENT_VIEW_RESULTS) ||
				path.equals(EvalSettings.ITEM_USE_COURSE_CATEGORY_ONLY) ) 
			return true;
		else
			return false;
	}
}
