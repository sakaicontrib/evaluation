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
import java.util.Date;
import java.util.GregorianCalendar;

import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.model.EvalEvaluation;

/**
 * Utility class for date manipulations.  
 * 
 * @author Kapil Ahuja (kahuja@vt.edu)
 * @author Aaron Zeckoski (aaronz@vt.edu)
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
		int minHours = ((Integer)evalSettings.
				get(EvalSettings.EVAL_MIN_TIME_DIFF_BETWEEN_START_DUE)).intValue();
		
		/*
		 * Find the difference between view date and due date so that after 
		 * due date is updated, add the same difference to view date again 
		 * (precision is miliseconds). Also casting to 'int' is fine as the 
		 * difference between view and due dates is not expected to be very 
		 * large.
		 */  
		int diffBetViewDue = (int) (eval.getViewDate().getTime() 
				- eval.getDueDate().getTime());
		
		/*
		 * If the difference between start date and due date is less than
		 * the minmum value set as system settings, then update the due date
		 * to reflect this minimum time difference. After that update the view
		 * date also.
		 * 
		 * Note: Arguments to getHoursDifference() method should have due date as 
		 *       first date and start date as second date.
		 */
		if (getHoursDifference(eval.getDueDate(), eval.getStartDate()) < minHours) {

			// Update due date
			Calendar calendarDue = new GregorianCalendar();
			calendarDue.setTime(eval.getStartDate());
			calendarDue.add(Calendar.HOUR, minHours);
			eval.setDueDate(calendarDue.getTime());
			
			// Update view date
			Calendar calendarView = new GregorianCalendar();
			calendarView.setTime(eval.getDueDate());
			calendarView.add(Calendar.MILLISECOND, diffBetViewDue);
			eval.setViewDate(calendarView.getTime());
		}
	}

	/**
	 * Find the number of hours between 2 dates
	 * 
	 * @param date1
	 * @param date2
	 * @return number of hours (can be negative, will round)
	 */
	public int getHoursDifference(Date date1, Date date2) {
		long millisecondsDifference = date1.getTime() - date2.getTime();
		return (int) millisecondsDifference / (60*60*1000);
	}

}
