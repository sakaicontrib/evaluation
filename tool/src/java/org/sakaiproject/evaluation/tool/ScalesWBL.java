/******************************************************************************
 * ScalesWBL.java - created by kahuja@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 *****************************************************************************/
package org.sakaiproject.evaluation.tool;

import org.sakaiproject.evaluation.logic.EvalScalesLogic;
import org.sakaiproject.evaluation.logic.utils.SettingsLogicUtils;
import org.sakaiproject.evaluation.model.EvalScale;

import uk.org.ponder.beanutil.WriteableBeanLocator;
import uk.org.ponder.conversion.StaticLeafParser;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * Obviates the need for a backing bean for add/modify/delete scales 
 * functionality (this is a WritableBeanLocator). 
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 */
public class ScalesWBL implements WriteableBeanLocator {
	
	// Spring injection 
	private EvalScalesLogic evalScalesLogic;
	public void setEvalScalesLogic(EvalScalesLogic evalScalesLogic) {
		this.evalScalesLogic = evalScalesLogic;
	}
	
	// Spring injection 
	/*private StaticLeafParser leafParser;
	public void setLeafParser(StaticLeafParser leafParser) {
		this.leafParser = leafParser;
	}*/
	
	/**
	 * Simply tells the user that remove functionality is not 
	 * supported (this is done by throwing an exception).
	 * 
	 * @param beanname -  Name of the property that has to be removed    
	 * @return throws a new UnsupportedOperationException exception
	 */
	public boolean remove(String beanname) {
		
		//evalScalesLogic.deleteScale(null, null);
		
		System.out.println(" Trying to remove a scale !!");
		
		return true;
	}
	  
	/**
	 * Sets the data from producer to EvalSettings (database). 
	 * 
	 * @param beanname -  Name of the property to be set    
	 * @param toset -  Value of the property to be set    
	 */
	public void set(String beanname, Object toset) {
		evalScalesLogic.saveScale(null, null);
	}

	/**
	 * Gets the data from EvalSettings (database) and returns to producer. 
	 * 
	 * @param path -  Name of the property whose value has to be fetched 
	 * 				  from database
	 * @return Value of the property obtained from database
	 */
	public Object locateBean(String path) {
		
		EvalScale scale = evalScalesLogic.getScaleById(
				new Long(Long.parseLong(path)));  

		System.out.println("testing for locate Bean, path = " + scale.getTitle());
		
		return scale.getTitle();
	}
	
	/*
	 * (non-javadoc)
	 * Uses java.lang.Class to find the class for a given property. 
	 */
	/*private static Class getPropertyType(String propname) {
		String typename = SettingsLogicUtils.getType(propname);
		try {
			return Class.forName(typename);
		}
		catch (Exception e) {
			throw UniversalRuntimeException.accumulate(e, "Could not look up " + typename + " to a class");
		}
	}*/
}
