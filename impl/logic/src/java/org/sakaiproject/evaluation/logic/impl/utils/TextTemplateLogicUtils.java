/******************************************************************************
 * TextTemplateLogicUtils.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.logic.impl.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * This is a weird location but it will have to do for now,
 * this handles processing of velocity templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TextTemplateLogicUtils {

	/**
	 * Handles the replacement of the variable strings within textual templates and
	 * also allows the setting of variables for the control of logical branching within
	 * the text template as well<br/>
	 * Uses and expects velocity (http://velocity.apache.org/) style templates<br/>
	 * 
	 * @param textTemplate a velocity style text template
	 * @param replacementValues a set of replacement values which are in the map like so:<br/>
	 * key => value (String => Object)<br/>
	 * username => aaronz<br/>
	 * course_title => Math 1001 Differential Equations<br/>
	 * @return the processed template
	 */
	public static String processTextTemplate(String textTemplate, Map<String, String> replacementValues) {
		if (replacementValues == null) {
			return textTemplate;
		}

		// setup velocity
		VelocityEngine ve = null;
		try {
			// trying out creating a new instance of velocity -AZ
			ve = new VelocityEngine(); //getting Could not initialize velocity with Velocity 1.5
			ve.init();
			
		} catch (Exception e) {
			throw new RuntimeException("Could not initialize velocity", e);
		}

		// load in the passed in replacement values
		VelocityContext context = new VelocityContext(replacementValues);

		Writer output = new StringWriter();
		boolean result = false;
		try {
			result = ve.evaluate(context, output, "textProcess", textTemplate);
		} catch (ParseErrorException e) {
			throw new RuntimeException("Velocity parsing error: ", e);
		} catch (MethodInvocationException e) {
			throw new RuntimeException("Velocity method invocation error: ", e);
		} catch (ResourceNotFoundException e) {
			throw new RuntimeException("Velocity resource not found error: ", e);
		} catch (IOException e) {
			throw new RuntimeException("Velocity IO error: ", e);
		}

		if ( result ) {
			return output.toString();
		} else {
			throw new RuntimeException("Failed to process velocity text template");
		}
	}

}
