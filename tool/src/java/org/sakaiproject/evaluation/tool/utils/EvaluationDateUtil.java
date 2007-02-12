/******************************************************************************
 * EvaluationDateUtil.java - created by kahuja@vt.edu
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
package org.sakaiproject.evaluation.tool.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * Utility class for date manipulations.  
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 */
public class EvaluationDateUtil {
	
	// Spring injection 
	private EvalSettings evalSettings;
	public void setEvalSettings(EvalSettings evalSettings) {
		this.evalSettings = evalSettings;
	}

	/**
	 * Ensures that there is minimum number of hours difference between
	 * the start date of the evaluation and due date of the evaluation.
	 * This minimum value is picked from system setting.
	 * 
	 * @param eval -  EvalEvaluation object that contains both start 
	 * 				  and due date.
	 */	
	public void updateDueDate(EvalEvaluation eval) {
		
		/*
		 * Getting the system setting that tells what should be the 
		 * minimum time difference between start date and due date.
		 */
		int mimTime = ((Integer)evalSettings.get(EvalSettings.EVAL_USE_STOP_DATE)).intValue();
		
		/*
		 * If the difference between start date and due date is less than
		 * the minmum value set as system settings, then update the due date
		 * to reflect this minimum time difference.
		 * Else update the due date to have time as 23:59 PM of that day.
		 */

		//TODO remaining tasks for this class are:
		//- Complete the this method for comparion and adding.
		//- Email Aaron to create property with some initialization.
		//- Work on administrative side of this.
		//- Move the other method from evaluationBean also here and remove two from Bean.
		//- Look if the two methods can be done without the elaborate Calendar stuff. 
		//- Test whole thing
		//- Verify the logic with Aaron.
		if ( (eval.getStartDate().getHours() - eval.getDueDate().getHours()) < mimTime ) {
		
		}
		else {

			/*
			 * The due date is of the form 17th, Jan 2007 12:00 AM
			 * Thus adding a 23 hours and 59 minutes to get:
			 * 17th, Jan 2007 11:59 PM.
			 */ 
			Calendar calendarDue = new GregorianCalendar();
			calendarDue.setTime(eval.getDueDate());
			calendarDue.add(Calendar.HOUR, 23);
			calendarDue.add(Calendar.MINUTE, 59);
			eval.setDueDate(calendarDue.getTime());
		}
	}
}
